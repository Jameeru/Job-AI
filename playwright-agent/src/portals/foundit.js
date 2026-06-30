"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.FounditStrategy = void 0;
const playwright_1 = require("playwright");
const agent_1 = require("../agent");
const path_1 = __importDefault(require("path"));
exports.FounditStrategy = {
    source: 'foundit',
    apply: async (page, url, resumePath) => {
        try {
            console.log(`Navigating to Foundit job: ${url}`);
            await page.goto(url, { waitUntil: 'networkidle' });
            // Look for apply button
            const applyBtn = page.locator('button:has-text("Apply"), button:has-text("Apply Now")').first();
            if (await applyBtn.isVisible()) {
                await applyBtn.click();
                await page.waitForLoadState('networkidle');
                // Similar pattern to Naukri...
                const fileInput = page.locator('input[type="file"]').first();
                if (await fileInput.isVisible()) {
                    await fileInput.setInputFiles(resumePath);
                    console.log(`Uploaded resume: ${path_1.default.basename(resumePath)}`);
                    return { success: true, notes: 'Successfully mocked application for Foundit.' };
                }
                return { success: false, notes: 'Could not find file upload input.' };
            }
            else {
                return { success: false, notes: 'Apply button not found or redirects externally.' };
            }
        }
        catch (e) {
            return { success: false, notes: `Automation failed: ${e.message}` };
        }
    }
};
//# sourceMappingURL=foundit.js.map