import { useState } from 'react';
import VideoLibrary from './components/VideoLibrary.jsx';
import Workspace from './components/Workspace.jsx';

export default function App() {
  const [selected, setSelected] = useState(null);

  return (
    <div className="app">
      <header className="app-header">
        <button
          type="button"
          className="brand"
          onClick={() => setSelected(null)}
          title="Back to library"
        >
          <span className="brand-mark" aria-hidden="true">◎</span>
          Centroid Finder
        </button>
        {selected && (
          <nav className="crumbs" aria-label="Breadcrumb">
            <button type="button" className="link" onClick={() => setSelected(null)}>
              Library
            </button>
            <span aria-hidden="true">/</span>
            <span className="crumb-current">{selected}</span>
          </nav>
        )}
      </header>

      <main className="app-main">
        {selected ? (
          <Workspace key={selected} filename={selected} />
        ) : (
          <VideoLibrary onSelect={setSelected} />
        )}
      </main>
    </div>
  );
}
