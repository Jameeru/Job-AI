export interface JobResponse {
  id: string;
  externalId: string;
  source: string;
  jobTitle: string;
  companyName: string;
  location: string;
  isRemote: boolean;
  jobDescription: string;
  requiredSkills: string;
  preferredSkills: string;
  experienceMin: number;
  experienceMax: number;
  salaryMin: number;
  salaryMax: number;
  salaryCurrency: string;
  applicationUrl: string;
  matchScore: number;
  isRejected: boolean;
  rejectionReason: string;
  aiAnalysis: string;
  scrapedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface CandidateSkill {
  id: string;
  skillName: string;
  category: string;
  proficiency: string;
}

export interface CandidateProject {
  id: string;
  projectName: string;
  description: string;
  techStack: string;
  githubUrl: string;
  liveUrl: string;
}

export interface CandidateCertification {
  id: string;
  certName: string;
  issuer: string;
}

export interface UserProfile {
  id: string;
  email: string;
  fullName: string;
  phone: string;
  linkedinUrl: string;
  githubUrl: string;
  portfolioUrl: string;
  collegeName: string;
  degree: string;
  branch: string;
  graduationYear: number;
  cgpa: number;
  city: string;
  expectedSalaryMin: number;
  expectedSalaryMax: number;
  isActive: boolean;
  skills: CandidateSkill[];
  projects: CandidateProject[];
  certifications: CandidateCertification[];
}

export interface ApplicationResponse {
  id: string;
  jobId: string;
  resumeVersionId: string;
  coverLetterId: string;
  status: string;
  appliedAt: string;
  interviewDate: string;
  offerAmount: number;
  notes: string;
  createdAt: string;
  updatedAt: string;
}

export interface ResumeResponse {
  id: string;
  jobId: string;
  versionLabel: string;
  htmlContent: string;
  pdfPath: string;
  keywordsUsed: string;
  atsScore: number;
  generatedAt: string;
}

export interface CoverLetterResponse {
  id: string;
  jobId: string;
  content: string;
  pdfPath: string;
  generatedAt: string;
}
