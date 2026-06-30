import React, { useEffect, useState } from 'react';
import { Building2, MapPin, Briefcase, ExternalLink, ThumbsDown, FileText } from 'lucide-react';
import api from '../api/axios';
import type { JobResponse, PageResponse } from '../types';

export const Dashboard: React.FC = () => {
  const [jobs, setJobs] = useState<JobResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchJobs();
  }, []);

  const fetchJobs = async () => {
    try {
      const response = await api.get<PageResponse<JobResponse>>('/jobs');
      setJobs(response.data.content);
    } catch (err) {
      console.error(err);
      setError('Failed to fetch jobs. Make sure the backend is running.');
    } finally {
      setLoading(false);
    }
  };

  const handleReject = async (id: string) => {
    try {
      await api.post(`/jobs/${id}/reject`, { reason: 'User rejected manually' });
      setJobs(jobs.filter((j) => j.id !== id));
    } catch (err) {
      console.error('Failed to reject job', err);
    }
  };

  const parseAiAnalysis = (aiAnalysisStr: string) => {
    try {
      return JSON.parse(aiAnalysisStr);
    } catch (e) {
      return null;
    }
  };

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary-500 border-t-transparent"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="rounded-xl bg-red-50 p-6 text-red-600">
        <p className="font-medium">{error}</p>
      </div>
    );
  }

  return (
    <div>
      <div className="mb-8 flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Job Matches</h1>
          <p className="mt-1 text-sm text-gray-500">
            AI-curated fresher roles scoring 8.0 or higher
          </p>
        </div>
        <div className="text-sm font-medium text-gray-500">
          Showing {jobs.length} jobs
        </div>
      </div>

      <div className="space-y-6">
        {jobs.length === 0 ? (
          <div className="glass-panel p-12 text-center rounded-xl">
            <h3 className="text-lg font-medium text-gray-900">No jobs found</h3>
            <p className="mt-1 text-gray-500">
              The scraper hasn't found any matching jobs yet, or they were all rejected.
            </p>
          </div>
        ) : (
          jobs.map((job) => {
            const analysis = parseAiAnalysis(job.aiAnalysis);
            return (
              <div key={job.id} className="glass-panel overflow-hidden rounded-xl bg-white shadow hover-lift transition-all">
                <div className="p-6">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-3">
                        <h2 className="text-xl font-semibold text-gray-900">
                          {job.jobTitle}
                        </h2>
                        <span className="inline-flex items-center rounded-full bg-green-100 px-2.5 py-0.5 text-sm font-medium text-green-800">
                          Score: {job.matchScore}
                        </span>
                      </div>
                      <div className="mt-2 flex flex-wrap items-center gap-4 text-sm text-gray-500">
                        <div className="flex items-center">
                          <Building2 className="mr-1.5 h-4 w-4 shrink-0" />
                          {job.companyName}
                        </div>
                        <div className="flex items-center">
                          <MapPin className="mr-1.5 h-4 w-4 shrink-0" />
                          {job.location} {job.isRemote && '(Remote)'}
                        </div>
                        {job.experienceMax !== null && (
                          <div className="flex items-center">
                            <Briefcase className="mr-1.5 h-4 w-4 shrink-0" />
                            {job.experienceMin}-{job.experienceMax} Yrs
                          </div>
                        )}
                        <span className="rounded bg-gray-100 px-2 py-0.5 text-xs uppercase tracking-wider text-gray-600">
                          {job.source}
                        </span>
                      </div>
                    </div>
                    
                    <div className="ml-4 flex items-center space-x-2">
                      <button 
                        onClick={() => handleReject(job.id)}
                        className="inline-flex items-center justify-center rounded-lg bg-gray-50 p-2 text-gray-400 hover:bg-red-50 hover:text-red-600 transition-colors"
                        title="Reject Job"
                      >
                        <ThumbsDown className="h-5 w-5" />
                      </button>
                      <button 
                        className="inline-flex items-center justify-center rounded-lg bg-primary-50 px-4 py-2 text-sm font-medium text-primary-700 hover:bg-primary-100 transition-colors"
                      >
                        <FileText className="mr-2 h-4 w-4" />
                        Generate CV
                      </button>
                      <a
                        href={job.applicationUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="inline-flex items-center justify-center rounded-lg bg-primary-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-primary-700 transition-colors"
                      >
                        Apply <ExternalLink className="ml-2 h-4 w-4" />
                      </a>
                    </div>
                  </div>

                  {analysis && (
                    <div className="mt-6 border-t border-gray-100 pt-4">
                      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                        <div>
                          <h4 className="text-xs font-semibold uppercase tracking-wide text-gray-500">Why you match</h4>
                          <ul className="mt-2 list-inside list-disc space-y-1 text-sm text-green-700">
                            {analysis.strengths?.slice(0, 3).map((s: string, i: number) => (
                              <li key={i}>{s}</li>
                            ))}
                          </ul>
                        </div>
                        <div>
                          <h4 className="text-xs font-semibold uppercase tracking-wide text-gray-500">Skills</h4>
                          <div className="mt-2 flex flex-wrap gap-2">
                            {analysis.matchingSkills?.slice(0, 5).map((s: string, i: number) => (
                              <span key={i} className="rounded-full bg-blue-50 px-2.5 py-0.5 text-xs font-medium text-blue-700">
                                {s}
                              </span>
                            ))}
                            {analysis.missingSkills?.slice(0, 2).map((s: string, i: number) => (
                              <span key={i} className="rounded-full bg-yellow-50 px-2.5 py-0.5 text-xs font-medium text-yellow-700">
                                {s} (Missing)
                              </span>
                            ))}
                          </div>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};
