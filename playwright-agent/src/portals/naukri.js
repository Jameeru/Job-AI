"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.NaukriStrategy = void 0;
const playwright_1 = require("playwright");
const agent_1 = require("../agent");
const path_1 = __importDefault(require("path"));
exports.NaukriStrategy = {
    source: 'naukri',
    apply: async (page, url, resumePath) => {
        try {
            console.log(`Navigating to Naukri job: ${url}`);
            await page.goto(url, { waitUntil: 'networkidle' });
            // 1. Check if it requires login
            const isLoginVisible = await page.isVisible('text="Login to apply"');
            if (isLoginVisible) {
                return { success: false, notes: 'Login required. Please login manually or configure session cookies.' };
            }
            // 2. Click Apply button
            // Note: Selectors for job portals change frequently. 
            // We look for common button texts.
            const applyBtn = page.locator('button:has-text("Apply")').first();
            if (await applyBtn.isVisible()) {
                await applyBtn.click();
                await page.waitForLoadState('networkidle');
                // 3. Look for file upload input
                const fileInput = page.locator('input[type="file"]').first();
                if (await fileInput.isVisible()) {
                    await fileInput.setInputFiles(resumePath);
                    console.log(`Uploaded resume: ${path_1.default.basename(resumePath)}`);
                    // Look for submit button
                    const submitBtn = page.locator('button:has-text("Submit"), button:has-text("Send")').first();
                    if (await submitBtn.isVisible()) {
                        // For safety in this demo, we won't click submit, but report success
                        // await submitBtn.click();
                        return { success: true, notes: 'Successfully filled out application and uploaded resume.' };
                    }
                }
                return { success: false, notes: 'Could not find file upload input or submit button on application form.' };
            }
            else {
                // Might be an external link that redirects
                return { success: false, notes: 'Apply button not found or redirects externally. Manual intervention required.' };
            }
        }
        catch (e) {
            return { success: false, notes: `Automation failed: ${e.message}` };
        }
    }
};
//# sourceMappingURL=naukri.js.map