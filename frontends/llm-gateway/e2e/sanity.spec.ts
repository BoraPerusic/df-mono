import { test, expect } from '@playwright/test';

test.describe('LLM Gateway Frontend', () => {

    test('should navigate to Models and Dashboard with mocked APIs', async ({ page }) => {
        // 1. Mock API Responses
        await page.route('/api/v1/models', async route => {
            await route.fulfill({
                contentType: 'application/json',
                body: JSON.stringify([
                    { id: 1, name: 'e2e-model-gpt4', provider: 'openai', modelType: 'chat', tags: ['e2e'] }
                ])
            });
        });

        await page.route('/api/v1/observability/logs*', async route => {
            await route.fulfill({
                contentType: 'application/json',
                body: JSON.stringify([
                    {
                        id: 101,
                        modelName: 'e2e-model-gpt4',
                        promptText: 'Hello E2E',
                        durationMs: 123,
                        status: 'SUCCESS',
                        createdAt: new Date().toISOString()
                    }
                ])
            });
        });

        // 2. Visit Home (Default) which redirects or we click navigation
        // App.vue has links. Let's assume we start at root.
        await page.goto('/');

        // 3. Verify Navigation Links exist
        await expect(page.getByText('Models')).toBeVisible();
        await expect(page.getByText('Dashboard')).toBeVisible();

        // 4. Test Models View
        await page.click('text=Models');
        await expect(page.getByText('Model Registry')).toBeVisible();
        await expect(page.getByText('e2e-model-gpt4')).toBeVisible();

        // 5. Test Dashboard View
        await page.click('text=Dashboard');
        await expect(page.getByText('Observability Dashboard')).toBeVisible();
        // Check for the mocked log entry
        await expect(page.getByText('Hello E2E')).toBeVisible();
        await expect(page.getByText('SUCCESS')).toBeVisible();

        // 6. Visual Snapshot (Optional, saves to test-results)
        await page.screenshot({ path: 'test-results/dashboard-screenshot.png' });
    });

});
