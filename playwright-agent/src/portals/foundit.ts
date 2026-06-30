import { Page } from 'playwright';
import { PortalStrategy } from '../agent';
import path from 'path';

export const FounditStrategy: PortalStrategy = {
  source: 'foundit',
  apply: async (page: Page, url: string, resumePath: string) => {
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
          console.log(`Uploaded resume: ${path.basename(resumePath)}`);
          
          return { success: true, notes: 'Successfully mocked application for Foundit.' };
        }
        
        return { success: false, notes: 'Could not find file upload input.' };
      } else {
        return { success: false, notes: 'Apply button not found or redirects externally.' };
      }

    } catch (e: any) {
      return { success: false, notes: `Automation failed: ${e.message}` };
    }
  }
};
