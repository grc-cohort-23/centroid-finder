import { useEffect, useState } from 'react';
import { getJobStatus } from '../api.js';

const POLL_MS = 1000;

// Polls GET /process/:jobId/status until the job is done or failed.
// Returns { status, result, error, notFound }.
export default function useJob(jobId) {
  const [state, setState] = useState({ status: jobId ? 'processing' : 'idle' });

  useEffect(() => {
    if (!jobId) {
      setState({ status: 'idle' });
      return undefined;
    }

    let cancelled = false;
    let timer;
    setState({ status: 'processing' });

    async function poll() {
      try {
        const body = await getJobStatus(jobId);
        if (cancelled) return;
        setState(body);
        if (body.status === 'processing') timer = setTimeout(poll, POLL_MS);
      } catch (err) {
        if (cancelled) return;
        const notFound = /not found/i.test(err.message);
        setState({ status: 'error', error: err.message, notFound });
      }
    }

    poll();
    return () => {
      cancelled = true;
      clearTimeout(timer);
    };
  }, [jobId]);

  return state;
}
