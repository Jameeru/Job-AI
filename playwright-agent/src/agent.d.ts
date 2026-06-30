import { Page } from 'playwright';
export interface PortalStrategy {
    source: string;
    apply: (page: Page, url: string, resumePath: string) => Promise<{
        success: boolean;
        notes: string;
    }>;
}
export declare class AutomationAgent {
    private strategies;
    registerStrategy(strategy: PortalStrategy): void;
    run(): Promise<void>;
    private processApplication;
}
//# sourceMappingURL=agent.d.ts.map