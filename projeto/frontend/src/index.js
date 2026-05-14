import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';

// Esse arquivo é o ponto de entrada do React.
// Ele pega o <div id="root"> do index.html e coloca o App dentro.
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);
