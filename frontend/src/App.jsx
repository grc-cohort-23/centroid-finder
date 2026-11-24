import { Routes, Route, Navigate } from 'react-router-dom';
import VideoChooser from './pages/VideoChooser';
import VideoPreview from './pages/VideoPreview';
import MainLayout from './layouts/MainLayout';

function App() {
  return (
      <Routes>
        <Route element={<MainLayout />}>
          <Route path="/" element={<Navigate to="/videos" replace />} />
          <Route path="/videos" element={<VideoChooser />} />
          <Route path="/preview/:filename" element={<VideoPreview />} />
        </Route>

        <Route path="*" element={<h2>Page not found</h2>} />
      </Routes>
  );
}

export default App;