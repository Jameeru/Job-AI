export interface Application {
    id: string;
    jobId: string;
    resumeVersionId: string;
    status: string;
    job: {
        applicationUrl: string;
        source: string;
    };
}
export declare const JobApi: {
    getPendingApplications: () => Promise<Application[]>;
    updateStatus: (applicationId: string, status: string, notes?: string) => Promise<void>;
    downloadResumePdf: (resumeId: string) => Promise<string | null>;
};
//# sourceMappingURL=api.d.ts.map