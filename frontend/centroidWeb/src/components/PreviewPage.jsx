import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { Button, FormControlLabel, Switch } from '@mui/material'
import { submitSettings } from '../api'
import ColorPicker from './ColorPicker'
import ProcessingJob from './ProcessingJob'
import ThresholdPicker from './ThresholdPicker'
import VideoFrame from './VideoFrame'
import withRequestStatus from './withRequestStatus'

const previewDelay = 300

function PreviewFrames({ preview, showMarker }) {
  const { originalUrl, previewUrl, width, height, centroid } = preview
  const hasCentroid = centroid.x >= 0 && centroid.y >= 0
  const frameProps = { width, height, centroid: showMarker && hasCentroid ? centroid : null }

  return (
    <>
      <div className="frameContainer">
        <VideoFrame key={originalUrl} title="Original frame" imageUrl={originalUrl} {...frameProps} />
        <VideoFrame key={previewUrl} title="Binarized frame" imageUrl={previewUrl} {...frameProps} />
      </div>
      <p role="status">
        {hasCentroid ? `Centroid (${centroid.x}, ${centroid.y})` : 'No centroid found with these settings.'}
      </p>
    </>
  )
}

const PreviewWithStatus = withRequestStatus(PreviewFrames)

export default function PreviewPage({ filename, threshold, setThreshold, targetColor, setTargetColor }) {
  const [result, setResult] = useState(null)
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
        if (!controller.signal.aborted) setResult({ settings, status: 'ready', preview })
      } catch (error) {
        if (!controller.signal.aborted) setResult({ settings, status: 'error', error: error.message })
      }
    }, previewDelay)
    return () => {
      clearTimeout(timer)
      controller.abort()
    }
  }, [settings])

  const currentResult = result?.settings === settings ? result : { status: 'loading' }

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

      <PreviewWithStatus {...currentResult} showMarker={showMarker} loadingMessage="Generating preview…" />
      <ProcessingJob settings={settings} />
    </section>
  )
}
