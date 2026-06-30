import { chromium, Browser, Page } from 'playwright';
import { Application, JobApi } from './api';
import fs from 'fs';
import path from 'path';

export interface PortalStrategy {
  source: string;
  apply: (page: Page, url: string, resumePath: string) => Promise<{ success: boolean; notes: string }>;
}

export class AutomationAgent {
  private strategies: Map<string, PortalStrategy> = new Map();

  registerStrategy(strategy: PortalStrategy) {
    this.strategies.set(strategy.source.toLowerCase(), strategy);
  }

  async run() {
    console.log('Fetching pending applications...');
    const applications = await JobApi.getPendingApplications();
    
    if (applications.length === 0) {
      console.log('No pending applications. Sleeping...');
      return;
    }

    console.log(`Found ${applications.length} pending applications.`);

    const browser = await chromium.launch({
      headless: false, // Set to true in prod, false for debugging CAPTCHAs
      args: ['--start-maximized']
    });

    for (const app of applications) {
      await this.processApplication(browser, app);
    }

    await browser.close();
  }

  private async processApplication(browser: Browser, app: Application) {
    console.log(`Processing application ${app.id} for job at ${app.job?.source}`);
    
    if (!app.job || !app.job.applicationUrl) {
      await JobApi.updateStatus(app.id, 'ERROR', 'Missing job application URL');
      return;
    }

    const source = app.job.source.toLowerCase();
    const strategy = this.strategies.get(source);

    if (!strategy) {
      console.warn(`No strategy found for source: ${source}`);
      await JobApi.updateStatus(app.id, 'MANUAL_INTERVENTION_REQUIRED', `Unsupported portal: ${source}`);
      return;
    }

    // 1. Download Resume
    const resumePath = await JobApi.downloadResumePdf(app.resumeVersionId);
    if (!resumePath) {
      await JobApi.updateStatus(app.id, 'ERROR', 'Failed to download resume PDF');
      return;
    }

    // 2. Execute Strategy
    const context = await browser.newContext({ viewport: null });
    const page = await context.newPage();
    
    try {
      const result = await strategy.apply(page, app.job.applicationUrl, resumePath);
      
      if (result.success) {
        await JobApi.updateStatus(app.id, 'APPLIED', result.notes);
      } else {
        await JobApi.updateStatus(app.id, 'ACTION_REQUIRED', result.notes);
      }
    } catch (error: any) {
      console.error(`Error applying to ${source}:`, error);
      await JobApi.updateStatus(app.id, 'ERROR', `Playwright error: ${error.message}`);
    } finally {
      // Clean up temp resume
      if (fs.existsSync(resumePath)) {
        fs.unlinkSync(resumePath);
      }
      await context.close();
    }
  }
}
