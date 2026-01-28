import com.google.protobuf.gradle.*

plugins {
    `kotlin`
    `java-library`
    alias(libs.plugins.protobuf)
}

dependencies {
    api(libs.protobuf.kotlin)
    api(libs.protobuf.protoc)
}

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                id("kotlin")
                id("python")
                id("js") {
                    option("import_style=commonjs,binary")
                }
            }
        }
    }
}

// --- Python Packaging (Source of Truth for 'uv') ---
val pythonPackageDir = layout.buildDirectory.dir("python-package")

val preparePythonPackage by tasks.registering {
    group = "python"
    dependsOn("generateProto")

    val generatedSource = layout.buildDirectory.dir("generated/source/proto/main/python")
    inputs.dir(generatedSource)
    outputs.dir(pythonPackageDir)

    doLast {
        val targetDir = pythonPackageDir.get().asFile
        targetDir.deleteRecursively()
        targetDir.mkdirs()

        val srcDir = targetDir.resolve("src")
        srcDir.mkdirs()

        if (generatedSource.get().asFile.exists()) {
            generatedSource.get().asFile.copyRecursively(srcDir, overwrite = true)
        }

        // Recursive __init__.py generation
        srcDir.walkTopDown().filter { it.isDirectory }.forEach { dir ->
            val initFile = dir.resolve("__init__.py")
            if (!initFile.exists()) initFile.createNewFile()
        }

        // Generate pyproject.toml for uv
        targetDir.resolve("pyproject.toml").writeText("""
            [project]
            name = "shared-proto"
            version = "0.1.0"
            dependencies = ["protobuf>=4.0.0"]
            [build-system]
            requires = ["hatchling"]
            build-backend = "hatchling.build"
            [tool.hatch.build.targets.wheel]
            packages = ["src/com"]
        """.trimIndent())
    }
}

// --- JS Packaging (Source of Truth for 'npm') ---
val jsPackageDir = layout.buildDirectory.dir("js-package")

val prepareJsPackage by tasks.registering {
    group = "javascript"
    dependsOn("generateProto")

    val generatedJs = layout.buildDirectory.dir("generated/source/proto/main/js")
    inputs.dir(generatedJs)
    outputs.dir(jsPackageDir)

    doLast {
        val target = jsPackageDir.get().asFile
        target.deleteRecursively()
        target.mkdirs()

        if (generatedJs.get().asFile.exists()) {
            generatedJs.get().asFile.copyRecursively(target.resolve("src"), overwrite = true)
        }

        target.resolve("package.json").writeText("""
            {
              "name": "shared-proto",
              "version": "0.1.0",
              "main": "src/index.js",
              "dependencies": { "google-protobuf": "^3.21.2" }
            }
        """.trimIndent())
    }
}

tasks.assemble {
    dependsOn(preparePythonPackage)
    dependsOn(prepareJsPackage)
}