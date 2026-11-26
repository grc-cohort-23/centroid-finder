import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { Alert, Button, FormControlLabel, Switch } from '@mui/material'
import { submitSettings } from '../api'
import ColorPicker from './ColorPicker'
import ProcessingJob from './ProcessingJob'
import ThresholdPicker from './ThresholdPicker'
import VideoFrame, { LiveMask } from './VideoFrame'
import withRequestStatus from './withRequestStatus'

/**
 * @typedef {Object} Centroid
 * @property {number} x - Pixel column of the centroid.
 * @property {number} y - Pixel row of the centroid.
 */

/**
 * @typedef {Object} ProcessingSettings
 * @property {string} videoFilename - Name of the selected video on the server.
 * @property {string} hexColor - Target color as a hex string, e.g. "#ff0000".
 * @property {number} tolerance - Max RGB Euclidean distance from the target for a pixel to count as a match.
 */

/**
 * @typedef {Object} PreviewData
 * @property {string} originalUrl - URL of the unprocessed frame.
 * @property {string} previewUrl - URL of the server-binarized frame.
 * @property {number} width - Frame width in pixels.
 * @property {number} height - Frame height in pixels.
 * @property {Centroid} centroid - Centroid of matching pixels.
 */

const previewDelay = 300

/**
 * Renders the original and binarized frames side by side plus a centroid
 * status line. Intended to be wrapped by {@link withRequestStatus}, which
 * supplies `isUpdating`.
 *
 * @param {Object} props
 * @param {PreviewData} props.preview - Server preview response.
 * @param {boolean} props.showMarker - Whether to draw the centroid marker.
 * @param {ProcessingSettings} props.settings - Current settings.
 * @param {boolean} props.isUpdating - True while preview is stale relative to settings.
 * @returns {JSX.Element}
 */
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
        {hasCentroid ? `Centroid (${centroid.x}, ${centroid.y})` : 'No centroid found with these settings.'}
      </p>
    </>
  )
}

const PreviewWithStatus = withRequestStatus(PreviewFrames)


/**
 * Preview screen for tuning color-threshold settings against a single frame
 * of a video, before launching a full processing job.
 *
 * Behavior:
 * - Any change to `filename`, `targetColor`, or `threshold` triggers a
 *   debounced (300 ms) POST to `/api/preview`. In-flight requests are aborted
 *   when settings change again or the component unmounts.
 * - While a new request is pending, the previous result stays on screen and
 *   the binarized frame is re-rendered client-side (see {@link VideoFrame})
 *   so the user gets instant feedback.
 *
 * States (derived from the latest request result):
 * - `loading`  – no result yet; shows "Generating preview…".
 * - `ready`    – preview received for the current settings.
 * - `updating` – a result exists but for older settings (`isUpdating`);
 *                a progress bar is shown over the stale preview.
 * - `error`    – the request failed; shows the error message.
 *
 * @param {Object} props
 * @param {string} props.filename - Selected video filename.
 * @param {number} props.threshold - Color-match tolerance (see ProcessingSettings.tolerance).
 * @param {(value: number) => void} props.setThreshold - Called when the threshold changes.
 * @param {string} props.targetColor - Target color as a hex string (e.g. "#aabbcc").
 * @param {(value: string) => void} props.setTargetColor - Called when the color changes.
 * @returns {JSX.Element}
 */
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
