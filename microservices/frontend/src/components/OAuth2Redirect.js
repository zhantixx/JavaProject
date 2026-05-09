import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

function OAuth2Redirect() {
  const navigate = useNavigate();

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    console.log('OAuth2Redirect token:', token);
    if (token) {
      localStorage.setItem('token', token);
      navigate('/tasks');
    } else {
      navigate('/login');
    }
  }, [navigate]);

  return <div className="text-center mt-10">Жүктелуде...</div>;
}

export default OAuth2Redirect;