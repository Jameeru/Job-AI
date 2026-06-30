"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.AutomationAgent = void 0;
const playwright_1 = require("playwright");
const api_1 = require("./api");
const fs_1 = __importDefault(require("fs"));
const path_1 = __importDefault(require("path"));
class AutomationAgent {
    strategies = new Map();
    registerStrategy(strategy) {
        this.strategies.set(strategy.source.toLowerCase(), strategy);
    }
    async run() {
        console.log('Fetching pending applications...');
        const applications = await api_1.JobApi.getPendingApplications();
        if (applications.length === 0) {
            console.log('No pending applications. Sleeping...');
            return;
        }
        console.log(`Found ${applications.length} pending applications.`);
        const browser = await playwright_1.chromium.launch({
            headless: false, // Set to true in prod, false for debugging CAPTCHAs
            args: ['--start-maximized']
        });
        for (const app of applications) {
            await this.processApplication(browser, app);
        }
        await browser.close();
    }
    async processApplication(browser, app) {
        console.log(`Processing application ${app.id} for job at ${app.job?.source}`);
        if (!app.job || !app.job.applicationUrl) {
            await api_1.JobApi.updateStatus(app.id, 'ERROR', 'Missing job application URL');
            return;
        }
        const source = app.job.source.toLowerCase();
        const strategy = this.strategies.get(source);
        if (!strategy) {
            console.warn(`No strategy found for source: ${source}`);
            await api_1.JobApi.updateStatus(app.id, 'MANUAL_INTERVENTION_REQUIRED', `Unsupported portal: ${source}`);
            return;
        }
        // 1. Download Resume
        const resumePath = await api_1.JobApi.downloadResumePdf(app.resumeVersionId);
        if (!resumePath) {
            await api_1.JobApi.updateStatus(app.id, 'ERROR', 'Failed to download resume PDF');
            return;
        }
        // 2. Execute Strategy
        const context = await browser.newContext({ viewport: null });
        const page = await context.newPage();
        try {
            const result = await strategy.apply(page, app.job.applicationUrl, resumePath);
            if (result.success) {
                await api_1.JobApi.updateStatus(app.id, 'APPLIED', result.notes);
            }
            else {
                await api_1.JobApi.updateStatus(app.id, 'ACTION_REQUIRED', result.notes);
            }
        }
        catch (error) {
            console.error(`Error applying to ${source}:`, error);
            await api_1.JobApi.updateStatus(app.id, 'ERROR', `Playwright error: ${error.message}`);
        }
        finally {
            // Clean up temp resume
            if (fs_1.default.existsSync(resumePath)) {
                fs_1.default.unlinkSync(resumePath);
            }
            await context.close();
        }
    }
}
exports.AutomationAgent = AutomationAgent;
//# sourceMappingURL=agent.js.map