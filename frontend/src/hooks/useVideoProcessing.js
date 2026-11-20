
import { useEffect, useRef, useState } from 'react';

const API_URL = import.meta.env.VITE_API_URL.replace(/\/$/, '');

export function useVideoProcessing(filename) {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [jobId, setJobId] = useState(null);
  const [jobStatus, setJobStatus] = useState(null);
  const [csvUrl, setCsvUrl] = useState(null);
  const [processError, setProcessError] = useState(null);

  const submitControllerRef = useRef(null);

  // Clear previous job state when selecting a different video.
  useEffect(() => {
    setIsSubmitting(false);
    setJobId(null);
    setJobStatus(null);
    setCsvUrl(null);
    setProcessError(null);

    return () => {
      submitControllerRef.current?.abort();
    };
  }, [filename]);

  // Start a processing job.
  async function submitVideo(targetColor, threshold) {
    if (isSubmitting) return;

    //refernece the abort controller in useRef to be able to acces this controller outside the specific useEffect later if needed
    const controller = new AbortController();
    submitControllerRef.current = controller;

    setIsSubmitting(true);
    setProcessError(null);
    setJobId(null);
    setJobStatus(null);
    setCsvUrl(null);

    //build url
    const params = new URLSearchParams({
      targetColor: targetColor.replace('#', ''),
      threshold: String(threshold)
    });

    try {
      const response = await fetch(
        `${API_URL}/process/${encodeURIComponent(filename)}?${params}`,
        {
          method: 'POST',
          signal: controller.signal
        }
      );

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || 'Processing failed');
      }

      if (!data.jobId) {
        throw new Error('No job ID returned by the server');
      }

      //early exit/abort
      if (controller.signal.aborted) return;

      setJobId(data.jobId);
      setJobStatus('processing');

    } catch (error) {
      if (!controller.signal.aborted) {
        setProcessError(error.message);
      }
    } finally {
      if (!controller.signal.aborted) {
        setIsSubmitting(false);
      }
    }
  }

  // Poll the server for the current job's status.
  useEffect(() => {
    if (!jobId) return;

    const controller = new AbortController();
    let timeoutId;
    let stopped = false;

    async function checkJobStatus() {
      try {
        const response = await fetch(
          `${API_URL}/process/${encodeURIComponent(jobId)}/status`,
          { signal: controller.signal }
        );

        const data = await response.json();

        if (!response.ok) {
          throw new Error(data.message || 'Status check failed');
        }

        if (stopped) return;

        switch (data.status) {
          case 'completed':
            if (!data.csvUrl) {
              throw new Error('Completed job has no CSV URL');
            }

            setJobStatus('completed');
            setCsvUrl(new URL(data.csvUrl, `${API_URL}/`).href);
            break;

          case 'failed':
            setJobStatus('failed');
            setProcessError(
              data.error || 'Video processing failed'
            );
            break;

          case 'processing':
            setJobStatus('processing');

            timeoutId = setTimeout(checkJobStatus, 2000);
            break;

          default:
            throw new Error('Unknown job status');
        }

      } catch (error) {
        if (!stopped && !controller.signal.aborted) {
          setJobStatus('failed');
          setProcessError(error.message);
        }
      }
    }

    checkJobStatus();

    return () => {
      stopped = true;
      controller.abort();
      clearTimeout(timeoutId);
    };
  }, [jobId]);

  return {
    submitVideo,
    isSubmitting,
    jobId,
    jobStatus,
    csvUrl,
    processError
  };
}
