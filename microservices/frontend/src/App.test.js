import { render, screen } from '@testing-library/react';
import App from './App';

test('renders login screen', () => {
  render(<App />);
  expect(screen.getAllByText(/Жеке Көмекші/i).length).toBeGreaterThan(0);
  expect(screen.getByRole('button', { name: /^Кіру$/i })).toBeInTheDocument();
  expect(screen.getByText(/Кіру \(Google\)/i)).toBeInTheDocument();
});
