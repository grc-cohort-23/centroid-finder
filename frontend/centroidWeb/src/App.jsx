import { useState } from 'react'
import { Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { Alert } from '@mui/material'
import VideoPicker from './components/VideoPicker'
import PreviewPage from './components/PreviewPage'
import './App.css'

function PreviewRoute(props) {
  const { pathname } = useLocation()
  let filename
  try {
    // Decode the URL segment once so literal percent escapes in filenames are preserved.
    filename = decodeURIComponent(pathname.split('/').filter(Boolean).at(-1))
  } catch {
    return <Alert severity="error">The video link contains an invalid filename.</Alert>
  }
  return <PreviewPage key={filename} filename={filename} {...props} />
}

export default function App() {
  const navigate = useNavigate()
  const [threshold, setThreshold] = useState(30)
  const [targetColor, setTargetColor] = useState('#FFA200')

  return (
    <main>
      <Routes>
        <Route path="/" element={<Navigate to="/videos" replace />} />
        <Route path="/videos" element={
          <section id="videos">
            <h1>Available Videos</h1>
            <VideoPicker onSelect={(filename) => navigate(`/preview/${encodeURIComponent(filename)}`)} />
          </section>
        } />
        <Route path="/preview/:filename" element={
          <PreviewRoute {...{ threshold, setThreshold, targetColor, setTargetColor }} />
        } />
        <Route path="*" element={<Navigate to="/videos" replace />} />
      </Routes>
    </main>
  )
}
