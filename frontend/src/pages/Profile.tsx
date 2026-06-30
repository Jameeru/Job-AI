import React, { useEffect, useState, useRef } from 'react';
import api from '../api/axios';
import type { UserProfile } from '../types';
import { User, Mail, Phone, MapPin, Briefcase, GraduationCap, Link2, Code, FileUp, Loader2 } from 'lucide-react';
import { EditProfileModal } from '../components/EditProfileModal';

export const Profile: React.FC = () => {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isExtracting, setIsExtracting] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      const response = await api.get<UserProfile>('/profile');
      setProfile(response.data);
    } catch (err: any) {
      if (err.response?.status === 404) {
        // Create initial profile if not found
        createInitialProfile();
      } else {
        setError('Failed to load profile.');
      }
    } finally {
      setLoading(false);
    }
  };

  const createInitialProfile = async () => {
    try {
      const response = await api.post<UserProfile>('/profile', {
        fullName: 'New User',
        email: 'user@example.com', // Will be overridden by backend using auth token
      });
      setProfile(response.data);
    } catch (err) {
      setError('Could not initialize profile.');
    }
  };

  const handleSaveProfile = async (data: Partial<UserProfile>) => {
    try {
      const response = await api.put<UserProfile>('/profile', data);
      setProfile(response.data);
    } catch (err) {
      console.error('Failed to update profile', err);
      alert('Failed to update profile. Please try again.');
    }
  };

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.type !== 'application/pdf') {
      alert('Please upload a PDF file.');
      return;
    }

    setIsExtracting(true);
    const formData = new FormData();
    formData.append('resume', file);

    try {
      const response = await api.post<UserProfile>('/profile/resume', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setProfile(response.data);
      alert('Profile updated successfully from resume!');
    } catch (err) {
      console.error('Failed to extract data from resume', err);
      alert('Failed to extract data from resume. Please try again.');
    } finally {
      setIsExtracting(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
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
    return <div className="rounded-xl bg-red-50 p-6 text-red-600 font-medium">{error}</div>;
  }

  if (!profile) return null;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-gray-900">Candidate Profile</h1>
        <div className="flex space-x-3">
          <input
            type="file"
            accept=".pdf"
            className="hidden"
            ref={fileInputRef}
            onChange={handleFileUpload}
          />
          <button
            onClick={() => fileInputRef.current?.click()}
            disabled={isExtracting}
            className="inline-flex items-center rounded-lg bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm border border-gray-300 hover:bg-gray-50 transition-colors disabled:opacity-70"
          >
            {isExtracting ? (
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            ) : (
              <FileUp className="mr-2 h-4 w-4" />
            )}
            {isExtracting ? 'Extracting Data...' : 'Upload Resume'}
          </button>
          <button
            onClick={() => setIsEditModalOpen(true)}
            disabled={isExtracting}
            className="rounded-lg bg-primary-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-primary-700 transition-colors disabled:opacity-70"
          >
            Edit Profile
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Basic Info */}
        <div className="glass-panel rounded-xl p-6 lg:col-span-1 space-y-6">
          <div className="flex items-center space-x-4">
            <div className="flex h-16 w-16 items-center justify-center rounded-full bg-primary-100 text-primary-600">
              <User className="h-8 w-8" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-gray-900">{profile.fullName || 'Anonymous'}</h2>
              <p className="text-sm text-gray-500">{profile.degree || 'Degree not set'}</p>
            </div>
          </div>

          <div className="space-y-3 pt-4 border-t border-gray-100">
            <div className="flex items-center text-sm text-gray-600">
              <Mail className="mr-3 h-5 w-5 text-gray-400" />
              {profile.email}
            </div>
            <div className="flex items-center text-sm text-gray-600">
              <Phone className="mr-3 h-5 w-5 text-gray-400" />
              {profile.phone || 'Phone not set'}
            </div>
            <div className="flex items-center text-sm text-gray-600">
              <MapPin className="mr-3 h-5 w-5 text-gray-400" />
              {profile.city || 'City not set'}
            </div>
          </div>

          <div className="space-y-3 pt-4 border-t border-gray-100">
            <h3 className="text-sm font-semibold uppercase tracking-wide text-gray-500">Links</h3>
            {profile.linkedinUrl && (
              <a 
                href={profile.linkedinUrl.startsWith('http') ? profile.linkedinUrl : `https://${profile.linkedinUrl}`} 
                target="_blank" 
                rel="noopener noreferrer" 
                className="flex items-center text-sm text-blue-600 hover:underline"
              >
                <Link2 className="mr-3 h-5 w-5" /> LinkedIn
              </a>
            )}
            {profile.githubUrl && (
              <a 
                href={profile.githubUrl.startsWith('http') ? profile.githubUrl : `https://${profile.githubUrl}`} 
                target="_blank" 
                rel="noopener noreferrer" 
                className="flex items-center text-sm text-gray-800 hover:underline"
              >
                <Link2 className="mr-3 h-5 w-5" /> GitHub
              </a>
            )}
          </div>
        </div>

        {/* Details */}
        <div className="space-y-6 lg:col-span-2">
          
          {/* Education */}
          <div className="glass-panel rounded-xl p-6">
            <h3 className="flex items-center text-lg font-semibold text-gray-900 border-b border-gray-100 pb-3">
              <GraduationCap className="mr-2 h-5 w-5 text-primary-500" /> Education
            </h3>
            <div className="mt-4 grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm font-medium text-gray-500">College</p>
                <p className="mt-1 text-gray-900">{profile.collegeName || '-'}</p>
              </div>
              <div>
                <p className="text-sm font-medium text-gray-500">Branch</p>
                <p className="mt-1 text-gray-900">{profile.branch || '-'}</p>
              </div>
              <div>
                <p className="text-sm font-medium text-gray-500">Graduation Year</p>
                <p className="mt-1 text-gray-900">{profile.graduationYear || '-'}</p>
              </div>
              <div>
                <p className="text-sm font-medium text-gray-500">CGPA</p>
                <p className="mt-1 text-gray-900">{profile.cgpa || '-'}</p>
              </div>
            </div>
          </div>

          {/* Skills */}
          <div className="glass-panel rounded-xl p-6">
            <h3 className="flex items-center text-lg font-semibold text-gray-900 border-b border-gray-100 pb-3">
              <Code className="mr-2 h-5 w-5 text-primary-500" /> Skills
            </h3>
            <div className="mt-4 flex flex-wrap gap-2">
              {profile.skills && profile.skills.length > 0 ? (
                profile.skills.map(skill => (
                  <span key={skill.id} className="inline-flex items-center rounded-full bg-blue-50 px-3 py-1 text-sm font-medium text-blue-700">
                    {skill.skillName} <span className="ml-1 opacity-60 text-xs">({skill.proficiency})</span>
                  </span>
                ))
              ) : (
                <p className="text-sm text-gray-500">No skills added yet.</p>
              )}
            </div>
          </div>

          {/* Projects */}
          <div className="glass-panel rounded-xl p-6">
            <h3 className="flex items-center text-lg font-semibold text-gray-900 border-b border-gray-100 pb-3">
              <Briefcase className="mr-2 h-5 w-5 text-primary-500" /> Projects
            </h3>
            <div className="mt-4 space-y-4">
              {profile.projects && profile.projects.length > 0 ? (
                profile.projects.map(project => (
                  <div key={project.id} className="rounded-lg border border-gray-100 p-4">
                    <h4 className="font-semibold text-gray-900">{project.projectName}</h4>
                    <p className="mt-1 text-sm text-gray-600">{project.description}</p>
                    <p className="mt-2 text-xs font-mono text-primary-600 bg-primary-50 inline-block px-2 py-1 rounded">
                      {project.techStack}
                    </p>
                  </div>
                ))
              ) : (
                <p className="text-sm text-gray-500">No projects added yet.</p>
              )}
            </div>
          </div>

        </div>
      </div>

      <EditProfileModal
        profile={profile}
        isOpen={isEditModalOpen}
        onClose={() => setIsEditModalOpen(false)}
        onSave={handleSaveProfile}
      />
    </div>
  );
};
