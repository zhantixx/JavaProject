import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

function OAuth2Redirect() {
  const navigate = useNavigate();

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    if (token) {
      localStorage.setItem('token', token);
      navigate('/tasks');
    } else {
      navigate('/login');
    }
  }, [navigate]);

  return <div>Жүктеу...</div>;
}

export default OAuth2Redirect;