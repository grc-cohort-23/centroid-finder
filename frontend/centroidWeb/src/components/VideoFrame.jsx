import { useCallback, useEffect, useRef, useState } from 'react'
import { Alert } from '@mui/material'

export default function VideoFrame({
  title,
  imageUrl,
  originalUrl,
  liveSettings,
  isUpdating,
  width,
  height,
  centroid,
}) {
  const [imageStatus, setImageStatus] = useState('loading')
  const canvasRef = useRef(null)
  const sourceImageRef = useRef(null)

  const renderLiveCanvas = useCallback(() => {
    if (!canvasRef.current || !sourceImageRef.current || !liveSettings) return
    const { hexColor, tolerance } = liveSettings
    const img = sourceImageRef.current
    const canvas = canvasRef.current
    const ctx = canvas.getContext('2d')
    if (!ctx) return

    canvas.width = img.width
    canvas.height = img.height
    ctx.drawImage(img, 0, 0)
    const imgData = ctx.getImageData(0, 0, img.width, img.height)
    const data = imgData.data

    const cleanHex = hexColor.replace('#', '')
    const targetR = parseInt(cleanHex.slice(0, 2), 16) || 0
    const targetG = parseInt(cleanHex.slice(2, 4), 16) || 0
    const targetB = parseInt(cleanHex.slice(4, 6), 16) || 0
    const thresholdSq = tolerance * tolerance

    for (let i = 0; i < data.length; i += 4) {
      const dr = data[i] - targetR
      const dg = data[i + 1] - targetG
      const db = data[i + 2] - targetB
      const distSq = dr * dr + dg * dg + db * db
      const val = distSq < thresholdSq ? 255 : 0
      data[i] = val
      data[i + 1] = val
      data[i + 2] = val
      data[i + 3] = 255
    }
    ctx.putImageData(imgData, 0, 0)
  }, [liveSettings])

  useEffect(() => {
    if (!originalUrl) return
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.onload = () => {
      sourceImageRef.current = img
      renderLiveCanvas()
    }
    img.src = originalUrl
  }, [originalUrl, renderLiveCanvas])

  useEffect(() => {
    if (isUpdating && sourceImageRef.current) {
      renderLiveCanvas()
    }
  }, [isUpdating, renderLiveCanvas])

  return (
    <section className="frame">
      <h2>{title}</h2>
      {imageStatus === 'error' ? (
        <Alert severity="error">The image could not load. Refresh the page to request a new preview.</Alert>
      ) : (
        <div className="frame-image" style={{ position: 'relative' }}>
          <img
            src={imageUrl}
            alt={title}
            width={width}
            height={height}
            onLoad={() => setImageStatus('ready')}
            onError={() => setImageStatus('error')}
          />
          {originalUrl && liveSettings && (
            <canvas
              ref={canvasRef}
              width={width}
              height={height}
              style={{
                position: 'absolute',
                top: 0,
                left: 0,
                width: '100%',
                height: '100%',
                display: isUpdating ? 'block' : 'none',
                pointerEvents: 'none',
              }}
            />
          )}
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

