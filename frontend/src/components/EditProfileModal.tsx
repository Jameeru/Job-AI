import React, { useState } from 'react';
import type { UserProfile, CandidateSkill, CandidateProject } from '../types';
import { X, Plus, Trash2 } from 'lucide-react';

interface EditProfileModalProps {
  profile: UserProfile;
  isOpen: boolean;
  onClose: () => void;
  onSave: (data: Partial<UserProfile>) => Promise<void>;
}

export const EditProfileModal: React.FC<EditProfileModalProps> = ({
  profile,
  isOpen,
  onClose,
  onSave,
}) => {
  const [formData, setFormData] = useState<Partial<UserProfile>>({
    fullName: profile.fullName || '',
    phone: profile.phone || '',
    city: profile.city || '',
    collegeName: profile.collegeName || '',
    branch: profile.branch || '',
    graduationYear: profile.graduationYear || new Date().getFullYear(),
    cgpa: profile.cgpa || 0,
    linkedinUrl: profile.linkedinUrl || '',
    githubUrl: profile.githubUrl || '',
  });

  const [skills, setSkills] = useState<Partial<CandidateSkill>[]>(profile.skills || []);
  const [projects, setProjects] = useState<Partial<CandidateProject>[]>(profile.projects || []);

  const [saving, setSaving] = useState(false);
  const [activeTab, setActiveTab] = useState<'basic' | 'skills' | 'projects'>('basic');

  if (!isOpen) return null;

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'number' ? Number(value) : value,
    }));
  };

  const handleSkillChange = (index: number, field: string, value: string) => {
    const newSkills = [...skills];
    newSkills[index] = { ...newSkills[index], [field]: value };
    setSkills(newSkills);
  };

  const handleProjectChange = (index: number, field: string, value: string) => {
    const newProjects = [...projects];
    newProjects[index] = { ...newProjects[index], [field]: value };
    setProjects(newProjects);
  };

  const addSkill = () => {
    setSkills([...skills, { skillName: '', category: 'BACKEND', proficiency: 'INTERMEDIATE' }]);
  };

  const removeSkill = (index: number) => {
    setSkills(skills.filter((_, i) => i !== index));
  };

  const addProject = () => {
    setProjects([...projects, { projectName: '', description: '', techStack: '' }]);
  };

  const removeProject = (index: number) => {
    setProjects(projects.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await onSave({
        ...formData,
        skills: skills as any,
        projects: projects as any,
      });
      onClose();
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-gray-900/50 backdrop-blur-sm">
      <div className="w-full max-w-3xl rounded-2xl bg-white shadow-xl flex flex-col max-h-[90vh]">
        <div className="flex items-center justify-between border-b border-gray-100 px-6 py-4">
          <h2 className="text-xl font-semibold text-gray-900">Edit Profile</h2>
          <button
            onClick={onClose}
            className="rounded-full p-2 text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="flex border-b border-gray-100 px-6">
          <button
            className={`py-3 px-4 font-medium text-sm ${activeTab === 'basic' ? 'border-b-2 border-primary-600 text-primary-600' : 'text-gray-500 hover:text-gray-700'}`}
            onClick={() => setActiveTab('basic')}
          >
            Basic Info
          </button>
          <button
            className={`py-3 px-4 font-medium text-sm ${activeTab === 'skills' ? 'border-b-2 border-primary-600 text-primary-600' : 'text-gray-500 hover:text-gray-700'}`}
            onClick={() => setActiveTab('skills')}
          >
            Skills
          </button>
          <button
            className={`py-3 px-4 font-medium text-sm ${activeTab === 'projects' ? 'border-b-2 border-primary-600 text-primary-600' : 'text-gray-500 hover:text-gray-700'}`}
            onClick={() => setActiveTab('projects')}
          >
            Projects
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-6">
          {activeTab === 'basic' && (
            <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700">Full Name</label>
                <input
                  type="text"
                  name="fullName"
                  value={formData.fullName}
                  onChange={handleChange}
                  className="w-full rounded-lg border border-gray-300 px-4 py-2 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500"
                />
              </div>
              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700">Phone Number</label>
                <input
                  type="text"
                  name="phone"
                  value={formData.phone}
                  onChange={handleChange}
                  className="w-full rounded-lg border border-gray-300 px-4 py-2 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500"
                />
              </div>
              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700">City</label>
                <input
                  type="text"
                  name="city"
                  value={formData.city}
                  onChange={handleChange}
                  className="w-full rounded-lg border border-gray-300 px-4 py-2 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500"
                />
              </div>
              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700">College Name</label>
                <input
                  type="text"
                  name="collegeName"
                  value={formData.collegeName}
                  onChange={handleChange}
                  className="w-full rounded-lg border border-gray-300 px-4 py-2 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500"
                />
              </div>
              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700">Branch / Major</label>
                <input
                  type="text"
                  name="branch"
                  value={formData.branch}
                  onChange={handleChange}
                  className="w-full rounded-lg border border-gray-300 px-4 py-2 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500"
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="mb-1 block text-sm font-medium text-gray-700">Grad. Year</label>
                  <input
                    type="number"
                    name="graduationYear"
                    value={formData.graduationYear}
                    onChange={handleChange}
                    className="w-full rounded-lg border border-gray-300 px-4 py-2 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500"
                  />
                </div>
                <div>
                  <label className="mb-1 block text-sm font-medium text-gray-700">CGPA</label>
                  <input
                    type="number"
                    step="0.01"
                    name="cgpa"
                    value={formData.cgpa}
                    onChange={handleChange}
                    className="w-full rounded-lg border border-gray-300 px-4 py-2 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500"
                  />
                </div>
              </div>
            </div>
          )}

          {activeTab === 'skills' && (
            <div className="space-y-4">
              {skills.map((skill, index) => (
                <div key={index} className="flex items-center space-x-3 bg-gray-50 p-3 rounded-lg border border-gray-200">
                  <input
                    type="text"
                    placeholder="Skill name"
                    value={skill.skillName || ''}
                    onChange={(e) => handleSkillChange(index, 'skillName', e.target.value)}
                    className="flex-1 rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-primary-500 focus:outline-none"
                  />
                  <select
                    value={skill.category || 'FRONTEND'}
                    onChange={(e) => handleSkillChange(index, 'category', e.target.value)}
                    className="rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-primary-500 focus:outline-none"
                  >
                    <option value="FRONTEND">Frontend</option>
                    <option value="BACKEND">Backend</option>
                    <option value="DATABASE">Database</option>
                    <option value="TOOL">Tool</option>
                    <option value="CLOUD">Cloud</option>
                    <option value="OTHER">Other</option>
                  </select>
                  <button onClick={() => removeSkill(index)} className="text-red-500 hover:text-red-700 p-2">
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              ))}
              <button onClick={addSkill} className="inline-flex items-center text-sm font-medium text-primary-600 hover:text-primary-700">
                <Plus className="w-4 h-4 mr-1" /> Add Skill
              </button>
            </div>
          )}

          {activeTab === 'projects' && (
            <div className="space-y-4">
              {projects.map((project, index) => (
                <div key={index} className="space-y-3 bg-gray-50 p-4 rounded-lg border border-gray-200 relative">
                  <button onClick={() => removeProject(index)} className="absolute top-4 right-4 text-red-500 hover:text-red-700">
                    <Trash2 className="w-4 h-4" />
                  </button>
                  <div>
                    <label className="mb-1 block text-xs font-medium text-gray-700">Project Name</label>
                    <input
                      type="text"
                      value={project.projectName || ''}
                      onChange={(e) => handleProjectChange(index, 'projectName', e.target.value)}
                      className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-primary-500 focus:outline-none"
                    />
                  </div>
                  <div>
                    <label className="mb-1 block text-xs font-medium text-gray-700">Description</label>
                    <input
                      type="text"
                      value={project.description || ''}
                      onChange={(e) => handleProjectChange(index, 'description', e.target.value)}
                      className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-primary-500 focus:outline-none"
                    />
                  </div>
                  <div>
                    <label className="mb-1 block text-xs font-medium text-gray-700">Tech Stack (comma separated)</label>
                    <input
                      type="text"
                      value={project.techStack || ''}
                      onChange={(e) => handleProjectChange(index, 'techStack', e.target.value)}
                      className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-primary-500 focus:outline-none"
                    />
                  </div>
                </div>
              ))}
              <button onClick={addProject} className="inline-flex items-center text-sm font-medium text-primary-600 hover:text-primary-700">
                <Plus className="w-4 h-4 mr-1" /> Add Project
              </button>
            </div>
          )}
        </div>

        <div className="border-t border-gray-100 p-6 flex justify-end space-x-3">
          <button
            type="button"
            onClick={onClose}
            disabled={saving}
            className="rounded-lg px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors"
          >
            Cancel
          </button>
          <button
            onClick={handleSubmit}
            disabled={saving}
            className="inline-flex items-center rounded-lg bg-primary-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-primary-700 disabled:opacity-70 transition-colors"
          >
            {saving ? 'Saving...' : 'Save Profile'}
          </button>
        </div>
      </div>
    </div>
  );
};
