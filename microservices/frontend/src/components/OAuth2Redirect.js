import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';

export default function OAuth2Redirect() {
  const handledRef = useRef(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (handledRef.current) return;
    handledRef.current = true;

    const url = new URL(window.location.href);
    const token = url.searchParams.get('token') ||
                  new URLSearchParams(url.hash?.replace(/^#/, '')).get('token');

    if (token) {
      localStorage.setItem('token', token);
      localStorage.setItem('accessToken', token);
      window.history.replaceState({}, '', '/');
      window.location.replace('/tasks');
    } else {
      navigate('/login', { replace: true });
    }
  }, [navigate]);

  return null;
}
