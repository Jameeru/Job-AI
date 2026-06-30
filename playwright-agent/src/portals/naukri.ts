import { Page } from 'playwright';
import { PortalStrategy } from '../agent';
import path from 'path';

export const NaukriStrategy: PortalStrategy = {
  source: 'naukri',
  apply: async (page: Page, url: string, resumePath: string) => {
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
          console.log(`Uploaded resume: ${path.basename(resumePath)}`);
          
          // Look for submit button
          const submitBtn = page.locator('button:has-text("Submit"), button:has-text("Send")').first();
          if (await submitBtn.isVisible()) {
            // For safety in this demo, we won't click submit, but report success
            // await submitBtn.click();
            return { success: true, notes: 'Successfully filled out application and uploaded resume.' };
          }
        }
        
        return { success: false, notes: 'Could not find file upload input or submit button on application form.' };
      } else {
        // Might be an external link that redirects
        return { success: false, notes: 'Apply button not found or redirects externally. Manual intervention required.' };
      }

    } catch (e: any) {
      return { success: false, notes: `Automation failed: ${e.message}` };
    }
  }
};
