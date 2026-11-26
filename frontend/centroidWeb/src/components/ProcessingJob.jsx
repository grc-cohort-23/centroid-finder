import { useEffect, useRef, useState } from 'react'
import { Alert, Button, Stack } from '@mui/material'
import { requestJson, submitSettings } from '../api'

const pollInterval = 1000

/**
 * Controls for starting a full processing job on the selected video using the
 * current settings, and (presumably) showing the job's progress/result.
 *
 * TODO: document states (e.g. idle / submitting / running / done / error) and
 * any callbacks once confirmed from the implementation.
 *
 * @param {Object} props
 * @param {ProcessingSettings} props.settings - Video filename, target color, and tolerance to process with.
 * @returns {JSX.Element}
 */
export default function ProcessingJob({ settings }) {
  const [job, setJob] = useState({ status: 'idle' })
  const submission = useRef(null)
  const activeJobId = job.status === 'processing' ? job.jobId : null
  const isBusy = job.status === 'submitting' || job.status === 'processing'

  useEffect(() => () => submission.current?.abort(), [])

  useEffect(() => {
    if (!activeJobId) return
    const controller = new AbortController()
    let timer

    async function poll() {
      try {
        const result = await requestJson(`/jobs/${encodeURIComponent(activeJobId)}`, { signal: controller.signal })
        if (controller.signal.aborted) return

        if (result.status === 'processing') {
          timer = setTimeout(poll, pollInterval)
        } else {
          setJob((current) => ({ ...current, ...result }))
        }
      } catch (error) {
        if (!controller.signal.aborted) {
          setJob((current) => ({ ...current, status: 'failed', error: error.message }))
        }
      }
    }

    timer = setTimeout(poll, pollInterval)
    return () => {
      controller.abort()
      clearTimeout(timer)
    }
  }, [activeJobId])

  async function startJob() {
    const controller = new AbortController()
    submission.current = controller
    setJob({ status: 'submitting', settings })
    try {
      const result = await submitSettings('/jobs', settings, controller.signal)
      if (!controller.signal.aborted) setJob({ ...result, settings })
    } catch (error) {
      if (!controller.signal.aborted) setJob({ status: 'failed', error: error.message, settings })
    }
  }

  return (
    <section aria-label="Video processing">
      <Stack spacing={1.5} alignItems="center">
        <Button variant="contained" disabled={isBusy} onClick={startJob}>Process Video with These Settings</Button>
        {job.settings && (
          <small>
            Job settings · {job.settings.hexColor} · Threshold {job.settings.tolerance}
          </small>
        )}
        {isBusy && <p role="status">{job.status === 'submitting' ? 'Starting job…' : 'Processing video…'}</p>}
        {job.status === 'failed' && <Alert severity="error">{job.error}</Alert>}
        {job.status === 'completed' && (
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} alignItems={{ sm: 'center' }}>
            <p role="status">Processing complete.</p>
            <Button color="inherit" href={job.downloadUrl} download>Download CSV</Button>
          </Stack>
        )}
      </Stack>
    </section>
  )
}
