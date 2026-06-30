import axios from 'axios';
import dotenv from 'dotenv';
import fs from 'fs';
import path from 'path';

dotenv.config();

const API_URL = process.env.API_URL || 'http://localhost:8080/api';
// In a real scenario, this agent would have a service API key to authenticate
const AGENT_API_KEY = process.env.AGENT_API_KEY || 'dev-agent-secret';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
    'X-Agent-Key': AGENT_API_KEY, // Assume the backend can accept this
  },
});

export interface Application {
  id: string;
  jobId: string;
  resumeVersionId: string;
  status: string;
  // job details we need
  job: {
    applicationUrl: string;
    source: string;
  };
}

export const JobApi = {
  getPendingApplications: async (): Promise<Application[]> => {
    try {
      const response = await api.get<Application[]>('/agent/applications/pending');
      return response.data;
    } catch (error) {
      console.error('Error fetching pending applications:', error);
      return [];
    }
  },

  updateStatus: async (applicationId: string, status: string, notes?: string) => {
    try {
      await api.patch(`/agent/applications/${applicationId}/status`, { status, notes });
      console.log(`Successfully updated application ${applicationId} to ${status}`);
    } catch (error) {
      console.error(`Error updating application ${applicationId}:`, error);
    }
  },

  downloadResumePdf: async (resumeId: string): Promise<string | null> => {
    try {
      const response = await api.get(`/agent/resumes/${resumeId}/pdf`, {
        responseType: 'arraybuffer'
      });
      
      const tempDir = path.join(__dirname, '..', 'temp');
      if (!fs.existsSync(tempDir)) {
        fs.mkdirSync(tempDir, { recursive: true });
      }
      
      const filePath = path.join(tempDir, `resume_${resumeId}.pdf`);
      fs.writeFileSync(filePath, Buffer.from(response.data, 'binary'));
      return filePath;
    } catch (error) {
      console.error(`Error downloading resume ${resumeId}:`, error);
      return null;
    }
  }
};
