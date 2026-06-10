import React from 'react';
import { usePermission } from './usePermission';

interface PermissionGateProps {
  pageId: number;
  action: string;
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

export const PermissionGate: React.FC<PermissionGateProps> = ({
  pageId,
  action,
  children,
  fallback = null
}) => {
  const { hasPermission } = usePermission();
  
  if (hasPermission(pageId, action)) {
    return <>{children}</>;
  }
  
  return <>{fallback}</>;
};
