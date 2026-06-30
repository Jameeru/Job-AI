import React, { useEffect, useState } from 'react';
import api from '../api/axios';
import type { ApplicationResponse } from '../types';
import { Clock, CheckCircle, XCircle, RefreshCw } from 'lucide-react';
import clsx from 'clsx';

export const Applications: React.FC = () => {
  const [applications, setApplications] = useState<ApplicationResponse[]>([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchApplications();
  }, []);

  const fetchApplications = async () => {
    try {
      const response = await api.get<ApplicationResponse[]>('/applications');
      setApplications(response.data);
      
      // Ideally backend returns job details in ApplicationResponse, 
      // but if not, we fetch them or they would be nested.
      // Assuming for now we just show IDs or we'll fetch them individually 
      // (in a real app, we'd adjust the DTO to include JobSummary).
    } catch (err) {
      console.error(err);
      setError('Failed to fetch applications.');
    } finally {
      setLoading(false);
    }
  };

  const getStatusConfig = (status: string) => {
    switch (status) {
      case 'PENDING':
        return { color: 'bg-yellow-100 text-yellow-800', icon: Clock };
      case 'APPLIED':
        return { color: 'bg-blue-100 text-blue-800', icon: CheckCircle };
      case 'REJECTED':
        return { color: 'bg-red-100 text-red-800', icon: XCircle };
      case 'INTERVIEW_SCHEDULED':
      case 'INTERVIEWED':
      case 'OFFER_RECEIVED':
        return { color: 'bg-green-100 text-green-800', icon: CheckCircle };
      default:
        return { color: 'bg-gray-100 text-gray-800', icon: Clock };
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
        <h1 className="text-3xl font-bold text-gray-900">Application Tracker</h1>
        <button 
          onClick={fetchApplications}
          className="inline-flex items-center rounded-lg bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
        >
          <RefreshCw className="mr-2 h-4 w-4" /> Refresh
        </button>
      </div>

      {error && (
        <div className="rounded-xl bg-red-50 p-6 text-red-600 font-medium">{error}</div>
      )}

      <div className="glass-panel overflow-hidden rounded-xl bg-white shadow">
        {applications.length === 0 ? (
          <div className="p-12 text-center">
            <h3 className="text-lg font-medium text-gray-900">No applications yet</h3>
            <p className="mt-1 text-gray-500">
              Your AI agent will start applying to jobs soon!
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                    Job ID
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                    Status
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                    Applied At
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                    Notes
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 bg-white">
                {applications.map((app) => {
                  const StatusIcon = getStatusConfig(app.status).icon;
                  return (
                    <tr key={app.id} className="hover:bg-gray-50 transition-colors">
                      <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900">
                        {app.jobId.substring(0, 8)}...
                      </td>
                      <td className="whitespace-nowrap px-6 py-4">
                        <span className={clsx('inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium', getStatusConfig(app.status).color)}>
                          <StatusIcon className="mr-1.5 h-3 w-3 shrink-0" />
                          {app.status}
                        </span>
                      </td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                        {app.appliedAt ? new Date(app.appliedAt).toLocaleDateString() : 'Not applied yet'}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-500">
                        {app.notes || '-'}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
