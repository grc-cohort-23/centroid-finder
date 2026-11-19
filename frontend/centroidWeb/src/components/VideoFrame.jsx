import { useState } from 'react'
import { Alert } from '@mui/material'

export default function VideoFrame({ title, imageUrl, width, height, centroid }) {
  const [imageStatus, setImageStatus] = useState('loading')

  return (
    <section className="frame">
      <h2>{title}</h2>
      {imageStatus === 'error' ? (
        <Alert severity="error">The image could not load. Refresh the page to request a new preview.</Alert>
      ) : (
        <div className="frame-image">
          <img
            src={imageUrl}
            alt={title}
            width={width}
            height={height}
            onLoad={() => setImageStatus('ready')}
            onError={() => setImageStatus('error')}
          />
          {imageStatus === 'ready' && centroid && (
            <span
              className="centroid-marker"
              aria-hidden="true"
              style={{
                left: `${((centroid.x + 0.5) / width) * 100}%`,
                top: `${((centroid.y + 0.5) / height) * 100}%`,
              }}
            />
          )}
        </div>
      )}
    </section>
  )
}
