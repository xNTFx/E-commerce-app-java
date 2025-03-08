import { useContext } from 'react';

import { NotificationContext } from '../context/NotificationContext';

const useShowNotification = () => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error(
      'useNotification must be used within a NotificationProvider',
    );
  }
  return context;
};

export default useShowNotification;
