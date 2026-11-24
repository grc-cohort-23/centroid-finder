import { useLayoutEffect, useRef, useState } from 'react'
import { Alert } from '@mui/material'

export function LiveMask({ sourceImage, settings, isUpdating }) {
  const canvasRef = useRef(null)
  const pixelsRef = useRef(null)
  const { hexColor, tolerance } = settings
  const { naturalWidth: width, naturalHeight: height } = sourceImage

  useLayoutEffect(() => {
    if (!isUpdating) return
    const canvas = canvasRef.current
    const ctx = canvas.getContext('2d')
    if (!ctx) return

    if (pixelsRef.current?.sourceImage !== sourceImage) {
      ctx.drawImage(sourceImage, 0, 0)
      pixelsRef.current = {
        sourceImage,
        original: ctx.getImageData(0, 0, width, height).data,
        mask: ctx.createImageData(width, height),
      }
    }
    const { original, mask } = pixelsRef.current

    const target = Number.parseInt(hexColor.replace('#', ''), 16)
    const targetR = (target >> 16) & 255
    const targetG = (target >> 8) & 255
    const targetB = target & 255
    const thresholdSq = tolerance * tolerance

    for (let i = 0; i < original.length; i += 4) {
      const dr = original[i] - targetR
      const dg = original[i + 1] - targetG
      const db = original[i + 2] - targetB
      const distSq = dr * dr + dg * dg + db * db
      const val = distSq < thresholdSq ? 255 : 0
      mask.data[i] = val
      mask.data[i + 1] = val
      mask.data[i + 2] = val
      mask.data[i + 3] = 255
    }
    ctx.putImageData(mask, 0, 0)
  }, [sourceImage, width, height, hexColor, tolerance, isUpdating])

  return <canvas ref={canvasRef} className="live-mask" width={width} height={height} hidden={!isUpdating} aria-hidden="true" />
}

export default function VideoFrame({
  title,
  imageUrl,
  pendingImageUrl,
  onImageLoad,
  onPendingLoad,
  onPendingError,
  width,
  height,
  centroid,
  children,
}) {
  const [loadedUrl, setLoadedUrl] = useState(null)
  const [failedUrl, setFailedUrl] = useState(null)
  const visibleUrl = imageUrl ?? pendingImageUrl
  // Preserve the loaded image element when it moves from pending to visible.
  const urls = imageUrl && pendingImageUrl ? [imageUrl, pendingImageUrl] : [visibleUrl]

  return (
    <section className="frame">
      <h2>{title}</h2>
      {failedUrl === visibleUrl ? (
        <Alert severity="error">The image could not load. Refresh the page to request a new preview.</Alert>
      ) : (
        <div className="frame-image">
          {urls.map((url) => (
            <img
              key={url}
              src={url}
              alt={url === visibleUrl ? title : ''}
              width={width}
              height={height}
              hidden={url !== visibleUrl}
              onLoad={(event) => {
                setLoadedUrl(url)
                if (url === pendingImageUrl) onPendingLoad()
                else onImageLoad?.(event.currentTarget)
              }}
              onError={() => {
                if (url === pendingImageUrl) onPendingError()
                else setFailedUrl(url)
              }}
            />
          ))}
          {children}
          {loadedUrl === visibleUrl && centroid && (
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

