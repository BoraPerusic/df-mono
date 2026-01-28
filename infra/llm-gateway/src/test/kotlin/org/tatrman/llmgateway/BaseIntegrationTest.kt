package org.tatrman.llmgateway

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
abstract class BaseIntegrationTest {

    @org.springframework.boot.test.mock.mockito.MockBean
    lateinit var natsConnection: io.nats.client.Connection

    companion object {
        init {
            // Podman often needs Ryuk disabled to avoid "Container not found" errors during cleanup
            System.setProperty("testcontainers.ryuk.disabled", "true")
            println("Initializing Testcontainers configuration...")
        }

        // Manually managing lifecycle to ensure readiness before DynamicPropertySource
        val postgres =
                PostgreSQLContainer("postgres:15-alpine")
                        .withDatabaseName("llm_gateway")
                        .withUsername("test")
                        .withPassword("test")
                        .apply {
                            println("Starting Postgres Container...")
                            start()
                            println("Postgres started at $jdbcUrl")

                            // Run Flyway Manually to guarantee schema exists
                            println("Running Flyway migrations...")
                            org.flywaydb.core.Flyway.configure()
                                    .dataSource(jdbcUrl, username, password)
                                    .load()
                                    .migrate()
                            println("Flyway migrations completed.")
                        }

        val wireMockServer =
                WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort()).apply {
                    println("Starting WireMock Server...")
                    start()
                    WireMock.configureFor("localhost", port())
                    println("WireMock started at port ${port()}")
                }

        @JvmStatic
        @AfterAll
        fun teardownContainers() {
            println("Stopping containers...")
            wireMockServer.stop()
            postgres.stop()
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            println("Configuring Dynamic Properties...")
            try {
                // DB
                registry.add("spring.datasource.url", postgres::getJdbcUrl)
                registry.add("spring.datasource.username", postgres::getUsername)
                registry.add("spring.datasource.password", postgres::getPassword)

                // OpenAI Base URL override
                registry.add("spring.ai.openai.base-url") {
                    "http://localhost:${wireMockServer.port()}"
                }
                registry.add("spring.ai.openai.api-key") { "sk-test-key" }

                // OAuth2 / Security
                registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri") {
                    "http://localhost:${wireMockServer.port()}/.well-known/jwks.json"
                }
                registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri") {
                    "http://localhost:${wireMockServer.port()}"
                }
            } catch (e: Exception) {
                System.err.println("Error in configureProperties: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }

        // Key pair for signing tokens (static so we don't regenerate per test)
        private val rsaKey: com.nimbusds.jose.jwk.RSAKey =
                com.nimbusds.jose.jwk.gen.RSAKeyGenerator(2048).keyID("test-key-id").generate()

        fun stubJwks() {
            val jwksJson = com.nimbusds.jose.jwk.JWKSet(rsaKey).toPublicJWKSet().toString()
            wireMockServer.stubFor(
                    WireMock.get(WireMock.urlPathEqualTo("/.well-known/jwks.json"))
                            .willReturn(WireMock.okJson(jwksJson))
            )
        }

        fun createTestToken(subject: String = "test-user"): String {
            val signer = com.nimbusds.jose.crypto.RSASSASigner(rsaKey)
            val claimsSet =
                    com.nimbusds.jwt.JWTClaimsSet.Builder()
                            .subject(subject)
                            .issuer("http://localhost:${wireMockServer.port()}")
                            .expirationTime(
                                    java.util.Date(System.currentTimeMillis() + 1000 * 60 * 60)
                            )
                            .build()

            val signedJWT =
                    com.nimbusds.jwt.SignedJWT(
                            com.nimbusds.jose.JWSHeader.Builder(
                                            com.nimbusds.jose.JWSAlgorithm.RS256
                                    )
                                    .keyID(rsaKey.keyID)
                                    .build(),
                            claimsSet
                    )

            signedJWT.sign(signer)
            return signedJWT.serialize()
        }
    }

    @BeforeEach
    fun resetWireMock() {
        wireMockServer.resetAll()
        stubJwks()
    }
}
