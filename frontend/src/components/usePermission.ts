import { useAuthStore } from '../store/authStore';

export function usePermission() {
  const user = useAuthStore(state => state.user);
  
  const hasPermission = (pageId: number | null, action: string): boolean => {
    if (!user) return false;
    if (user.roles.includes('ROLE_ULTRA_SUPER_ADMIN') || user.roles.includes('ROLE_SUPER_ADMIN')) {
      return true;
    }
    if (pageId === null) return true;
    const permissionKey = `PAGE_${pageId}_${action.toUpperCase()}`;
    return user.permissions.includes(permissionKey);
  };

  return { hasPermission };
}
