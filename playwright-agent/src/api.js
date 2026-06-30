"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.JobApi = void 0;
const axios_1 = __importDefault(require("axios"));
const dotenv_1 = __importDefault(require("dotenv"));
const fs_1 = __importDefault(require("fs"));
const path_1 = __importDefault(require("path"));
dotenv_1.default.config();
const API_URL = process.env.API_URL || 'http://localhost:8080/api';
// In a real scenario, this agent would have a service API key to authenticate
const AGENT_API_KEY = process.env.AGENT_API_KEY || 'dev-agent-secret';
const api = axios_1.default.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json',
        'X-Agent-Key': AGENT_API_KEY, // Assume the backend can accept this
    },
});
exports.JobApi = {
    getPendingApplications: async () => {
        try {
            const response = await api.get('/agent/applications/pending');
            return response.data;
        }
        catch (error) {
            console.error('Error fetching pending applications:', error);
            return [];
        }
    },
    updateStatus: async (applicationId, status, notes) => {
        try {
            await api.patch(`/agent/applications/${applicationId}/status`, { status, notes });
            console.log(`Successfully updated application ${applicationId} to ${status}`);
        }
        catch (error) {
            console.error(`Error updating application ${applicationId}:`, error);
        }
    },
    downloadResumePdf: async (resumeId) => {
        try {
            const response = await api.get(`/agent/resumes/${resumeId}/pdf`, {
                responseType: 'arraybuffer'
            });
            const tempDir = path_1.default.join(__dirname, '..', 'temp');
            if (!fs_1.default.existsSync(tempDir)) {
                fs_1.default.mkdirSync(tempDir, { recursive: true });
            }
            const filePath = path_1.default.join(tempDir, `resume_${resumeId}.pdf`);
            fs_1.default.writeFileSync(filePath, Buffer.from(response.data, 'binary'));
            return filePath;
        }
        catch (error) {
            console.error(`Error downloading resume ${resumeId}:`, error);
            return null;
        }
    }
};
//# sourceMappingURL=api.js.map