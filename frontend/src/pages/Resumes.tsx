import React, { useEffect, useState } from 'react';
import api from '../api/axios';
import type { ResumeResponse } from '../types';
import { FileText, Download } from 'lucide-react';

export const Resumes: React.FC = () => {
  const [resumes, setResumes] = useState<ResumeResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchResumes();
  }, []);

  const fetchResumes = async () => {
    try {
      const response = await api.get<ResumeResponse[]>('/resumes');
      setResumes(response.data);
    } catch (err) {
      console.error(err);
      setError('Failed to fetch resumes.');
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async (id: string, label: string) => {
    try {
      const response = await api.get(`/resumes/${id}/pdf`, { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `resume_${label.replace(/\s+/g, '_')}.pdf`);
      document.body.appendChild(link);
      link.click();
      if (link.parentNode) link.parentNode.removeChild(link);
    } catch (err) {
      console.error('Download failed', err);
    }
  };

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary-500 border-t-transparent"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-gray-900">Generated Resumes</h1>
        <p className="mt-1 text-sm text-gray-500">
          Tailored PDFs optimized for ATS
        </p>
      </div>

      {error && (
        <div className="rounded-xl bg-red-50 p-6 text-red-600 font-medium">{error}</div>
      )}

      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {resumes.length === 0 ? (
          <div className="col-span-full glass-panel p-12 text-center rounded-xl">
            <h3 className="text-lg font-medium text-gray-900">No resumes generated</h3>
            <p className="mt-1 text-gray-500">
              When the AI agent finds matching jobs, tailored resumes will appear here.
            </p>
          </div>
        ) : (
          resumes.map((resume) => (
            <div key={resume.id} className="glass-panel overflow-hidden rounded-xl bg-white shadow hover-lift transition-all flex flex-col">
              <div className="p-5 flex-1">
                <div className="flex items-center justify-center h-24 bg-gray-50 rounded-lg mb-4">
                   <FileText className="h-12 w-12 text-primary-400" />
                </div>
                <h3 className="text-lg font-bold text-gray-900 line-clamp-2" title={resume.versionLabel}>
                  {resume.versionLabel}
                </h3>
                <div className="mt-2 text-sm text-gray-500 flex flex-col space-y-1">
                  <span>ATS Score: <span className="font-medium text-green-600">{resume.atsScore}%</span></span>
                  <span>Generated: {new Date(resume.generatedAt).toLocaleDateString()}</span>
                </div>
              </div>
              <div className="border-t border-gray-100 bg-gray-50 p-4 flex gap-2">
                <button 
                  onClick={() => handleDownload(resume.id, resume.versionLabel)}
                  className="flex-1 inline-flex items-center justify-center rounded-lg bg-primary-600 px-3 py-2 text-sm font-medium text-white shadow-sm hover:bg-primary-700 transition-colors"
                >
                  <Download className="mr-2 h-4 w-4" /> Download PDF
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
