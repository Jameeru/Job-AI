"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const agent_1 = require("./agent");
const naukri_1 = require("./portals/naukri");
const foundit_1 = require("./portals/foundit");
const dotenv_1 = __importDefault(require("dotenv"));
dotenv_1.default.config();
async function main() {
    console.log('Starting JOB AI Playwright Automation Agent...');
    const agent = new agent_1.AutomationAgent();
    agent.registerStrategy(naukri_1.NaukriStrategy);
    agent.registerStrategy(foundit_1.FounditStrategy);
    // Add other strategies here...
    const pollIntervalMs = parseInt(process.env.POLL_INTERVAL_MS || '60000', 10);
    // Initial run
    await agent.run();
    // Schedule regular polling
    setInterval(async () => {
        try {
            await agent.run();
        }
        catch (error) {
            console.error('Unhandled error during agent run:', error);
        }
    }, pollIntervalMs);
    console.log(`Agent is polling every ${pollIntervalMs / 1000} seconds for new applications...`);
}
main().catch(console.error);
//# sourceMappingURL=index.js.map