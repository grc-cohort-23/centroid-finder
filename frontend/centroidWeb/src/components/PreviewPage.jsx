import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { Alert, Button, FormControlLabel, Switch } from '@mui/material'
import { submitSettings } from '../api'
import ColorPicker from './ColorPicker'
import ProcessingJob from './ProcessingJob'
import ThresholdPicker from './ThresholdPicker'
import VideoFrame, { LiveMask } from './VideoFrame'
import withRequestStatus from './withRequestStatus'

const previewDelay = 300

function PreviewFrames({ preview, displayed, pending, showMarker, settings, isUpdating, onPendingLoad, onPendingError }) {
  const [sourceImage, setSourceImage] = useState(null)
  const { previewUrl, width, height, centroid } = preview
  // Keep the first decoded original frame as the source for all settings on this video.
  const originalUrl = sourceImage?.getAttribute('src') ?? preview.originalUrl
  const isCurrent = displayed?.settings === settings
  const hasCentroid = centroid.x >= 0 && centroid.y >= 0
  const frameProps = { width, height, centroid: isCurrent && showMarker && hasCentroid ? centroid : null }

  return (
    <>
      <div className="frameContainer">
        <VideoFrame key={originalUrl} title="Original frame" imageUrl={originalUrl} onImageLoad={setSourceImage} {...frameProps} />
        <VideoFrame
          title="Binarized frame"
          imageUrl={displayed ? previewUrl : null}
          pendingImageUrl={pending?.preview.previewUrl}
          onPendingLoad={onPendingLoad}
          onPendingError={onPendingError}
          {...frameProps}
        >
          {sourceImage && <LiveMask sourceImage={sourceImage} settings={settings} isUpdating={Boolean(displayed && isUpdating)} />}
        </VideoFrame>
      </div>
      <p role="status">
        {isUpdating && 'Updating preview…'}
        {isCurrent && (hasCentroid ? `Centroid (${centroid.x}, ${centroid.y})` : 'No centroid found with these settings.')}
      </p>
    </>
  )
}

const PreviewWithStatus = withRequestStatus(PreviewFrames)

export default function PreviewPage({ filename, threshold, setThreshold, targetColor, setTargetColor }) {
  const [{ displayed, result }, setPreviewState] = useState({ displayed: null, result: null })
  const [showMarker, setShowMarker] = useState(true)
  const settings = useMemo(
    () => ({ videoFilename: filename, hexColor: targetColor, tolerance: threshold }),
    [filename, targetColor, threshold],
  )

  useEffect(() => {
    const controller = new AbortController()
    const timer = setTimeout(async () => {
      try {
        const preview = await submitSettings('/api/preview', settings, controller.signal)
        if (!controller.signal.aborted) setPreviewState((state) => ({ ...state, result: { settings, preview } }))
      } catch (error) {
        if (!controller.signal.aborted) {
          setPreviewState((state) => ({ ...state, result: { settings, error: error.message } }))
        }
      }
    }, previewDelay)
    return () => {
      clearTimeout(timer)
      controller.abort()
    }
  }, [settings])

  const currentResult = result?.settings === settings ? result : null
  const error = currentResult?.error
  const pending = currentResult?.preview ? currentResult : null
  const preview = displayed?.preview ?? pending?.preview
  const status = preview ? 'ready' : error ? 'error' : 'loading'

  function showPendingPreview() {
    setPreviewState((state) => {
      if (state.result !== pending) return state
      return { result: null, displayed: pending }
    })
  }

  function reportPendingError() {
    setPreviewState((state) => {
      if (state.result !== pending) return state
      const error = 'The binarized image could not load. Change the settings to try again.'
      return { ...state, result: { settings, error } }
    })
  }

  return (
    <section id="center">
      <Button color="inherit" component={Link} to="/videos">All videos</Button>
      <h1>Preview Processing</h1>
      <p style={{ overflowWrap: 'anywhere', maxWidth: '100%' }}>{filename}</p>
      <div className="controls">
        <div className="control">
          <label htmlFor="target-color">Target Color</label>
          <ColorPicker value={targetColor} onChange={setTargetColor} />
        </div>
        <div className="control">
          <label htmlFor="threshold">Threshold</label>
          <ThresholdPicker value={threshold} onChange={setThreshold} />
        </div>
        <FormControlLabel
          control={<Switch checked={showMarker} onChange={(event) => setShowMarker(event.target.checked)} />}
          label="Show centroid marker"
        />
      </div>

      <PreviewWithStatus
        status={status}
        error={error}
        preview={preview}
        displayed={displayed}
        pending={pending}
        showMarker={showMarker}
        settings={settings}
        isUpdating={!error && displayed?.settings !== settings}
        onPendingLoad={showPendingPreview}
        onPendingError={reportPendingError}
        loadingMessage="Generating preview…"
      />
      {displayed && error && <Alert severity="error">{error} Showing the previous preview.</Alert>}
      <ProcessingJob settings={settings} />
    </section>
  )
}
