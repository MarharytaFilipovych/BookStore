import React from 'react';
import ReactDOM from 'react-dom/client';
import {App} from './App';
import {BrowserRouter} from "react-router";
import {AppProvider} from "./AppProvider";

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);
root.render(
  <BrowserRouter>
      <AppProvider>
          <App />
      </AppProvider>
  </BrowserRouter>
);
