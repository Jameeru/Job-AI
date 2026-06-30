import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Briefcase, FileText, User, LogOut, Menu, X, LayoutDashboard } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import clsx from 'clsx';

export const Navbar: React.FC = () => {
  const { user, logout } = useAuth();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const navItems = [
    { name: 'Dashboard', path: '/', icon: LayoutDashboard },
    { name: 'Applications', path: '/applications', icon: Briefcase },
    { name: 'Resumes', path: '/resumes', icon: FileText },
    { name: 'Profile', path: '/profile', icon: User },
  ];

  return (
    <nav className="glass-panel sticky top-0 z-50 border-b border-gray-200">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 justify-between">
          <div className="flex">
            <div className="flex flex-shrink-0 items-center">
              <span className="bg-gradient-to-r from-primary-600 to-blue-400 bg-clip-text text-xl font-bold text-transparent">
                JOB AI
              </span>
            </div>
            <div className="hidden sm:ml-6 sm:flex sm:space-x-8">
              {navItems.map((item) => (
                <Link
                  key={item.name}
                  to={item.path}
                  className={clsx(
                    'inline-flex items-center border-b-2 px-1 pt-1 text-sm font-medium transition-colors',
                    location.pathname === item.path
                      ? 'border-primary-500 text-gray-900'
                      : 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700'
                  )}
                >
                  <item.icon className="mr-2 h-4 w-4" />
                  {item.name}
                </Link>
              ))}
            </div>
          </div>
          <div className="hidden sm:ml-6 sm:flex sm:items-center">
            {user && (
              <div className="flex items-center space-x-4">
                <span className="text-sm text-gray-500">{user.email}</span>
                <button
                  onClick={logout}
                  className="rounded-full bg-white p-2 text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 hover:bg-gray-50 transition-colors"
                >
                  <span className="sr-only">Log out</span>
                  <LogOut className="h-5 w-5" />
                </button>
              </div>
            )}
          </div>
          
          <div className="-mr-2 flex items-center sm:hidden">
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="inline-flex items-center justify-center rounded-md bg-white p-2 text-gray-400 hover:bg-gray-100 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2"
            >
              <span className="sr-only">Open main menu</span>
              {mobileMenuOpen ? (
                <X className="block h-6 w-6" aria-hidden="true" />
              ) : (
                <Menu className="block h-6 w-6" aria-hidden="true" />
              )}
            </button>
          </div>
        </div>
      </div>

      {mobileMenuOpen && (
        <div className="sm:hidden bg-white shadow-lg absolute w-full">
          <div className="space-y-1 pb-3 pt-2">
            {navItems.map((item) => (
              <Link
                key={item.name}
                to={item.path}
                onClick={() => setMobileMenuOpen(false)}
                className={clsx(
                  'block border-l-4 py-2 pl-3 pr-4 text-base font-medium',
                  location.pathname === item.path
                    ? 'border-primary-500 bg-primary-50 text-primary-700'
                    : 'border-transparent text-gray-500 hover:border-gray-300 hover:bg-gray-50 hover:text-gray-700'
                )}
              >
                <div className="flex items-center">
                  <item.icon className="mr-3 h-5 w-5" />
                  {item.name}
                </div>
              </Link>
            ))}
            <button
              onClick={() => {
                setMobileMenuOpen(false);
                logout();
              }}
              className="block w-full border-l-4 border-transparent py-2 pl-3 pr-4 text-left text-base font-medium text-gray-500 hover:border-gray-300 hover:bg-gray-50 hover:text-gray-700"
            >
              <div className="flex items-center">
                <LogOut className="mr-3 h-5 w-5" />
                Log out
              </div>
            </button>
          </div>
        </div>
      )}
    </nav>
  );
};
