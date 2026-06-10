import React from 'react';
import { Navigate } from 'react-router-dom';
import { usePermission } from './usePermission';

interface PageGuardProps {
  pageId: number;
  children: React.ReactNode;
  fallbackPath?: string;
}

export const PageGuard: React.FC<PageGuardProps> = ({
  pageId,
  children,
  fallbackPath = '/app/dashboard'
}) => {
  const { hasPermission } = usePermission();
  
  if (hasPermission(pageId, 'VIEW')) {
    return <>{children}</>;
  }
  
  return <Navigate to={fallbackPath} replace />;
};
