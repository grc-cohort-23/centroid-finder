import { useCallback, useEffect, useRef, useState } from 'react'
import { Alert } from '@mui/material'

/**
 * Displays one video frame with an optional live-binarization canvas overlay
 * and an optional centroid marker.
 *
 * Live overlay: when both `originalUrl` and `liveSettings` are provided, the
 * original image is loaded into memory and thresholded on a canvas using
 * `liveSettings`. The canvas is visible only while `isUpdating` is true,
 * covering the stale server image until the new preview arrives. The original
 * image is requested with `crossOrigin="anonymous"`, so the server must send
 * CORS headers or `getImageData` will throw.
 *
 * Image states (internal):
 * - `loading` – initial; the image is requesting.
 * - `ready`   – image loaded; the centroid marker may render.
 * - `error`   – image failed; an error Alert replaces the frame.
 * Callers should change `key` (e.g. to the image URL) to reset these states.
 *
 * @param {Object} props
 * @param {string} props.title - Heading text; also used as the image `alt`.
 * @param {string} props.imageUrl - URL of the image to display.
 * @param {string} [props.originalUrl] - Source image for the live canvas. Omit to disable the overlay.
 * @param {ProcessingSettings} [props.liveSettings] - Settings for the live canvas (uses `hexColor` and `tolerance`).
 * @param {boolean} [props.isUpdating] - Show the live canvas over the image when true.
 * @param {number} props.width - Image width in pixels (used for layout and marker positioning).
 * @param {number} props.height - Image height in pixels.
 * @param {Centroid|null} [props.centroid] - Marker position in pixels; null/undefined hides the marker.
 * @returns {JSX.Element}
 */
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

