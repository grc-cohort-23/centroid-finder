
export default function JobStatus({
  jobId,
  jobStatus,
  csvUrl,
  processError
}) {
  return (
    <section aria-live="polite">

      {processError && (
        <p role="alert">{processError}</p>
      )}

      {jobId && (
        <div>
          <p>Job ID: {jobId}</p>

          {jobStatus === "processing" && (
            <p>Processing video...</p>
          )}

          {jobStatus === "completed" && (
            <div>
              <p>Video processing completed!</p>

              {csvUrl && (
                <a href={csvUrl} download>
                  Download CSV Results
                </a>
              )}
            </div>
          )}

          {jobStatus === "failed" && (
            <p>Video processing failed.</p>
          )}
        </div>
      )}

    </section>
  );
}
