import { render, screen } from '@testing-library/react';
import App from './App';

test('renders the application header', () => {
  render(<App />);
  expect(screen.getByText(/saft-bndes/i)).toBeInTheDocument();
});
