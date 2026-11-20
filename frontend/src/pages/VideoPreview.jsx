
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import PreviewCanvases from '../components/PreviewCanvases';
import ProcessingControls from '../components/ProcessingControls';
import { useVideoProcessing } from '../hooks/useVideoProcessing';
import JobStatus from '../components/JobStatus';
import { Button } from '@mui/material';
export default function VideoPreview() {
    const { filename } = useParams();
    const [thumbnail, setThumbnail] = useState(null);
    const [error, setError] = useState(null);
    const [targetColor, setTargetColor] = useState('#ffffff');
    const [threshold, setThreshold] = useState(50);

  // Fetch the selected video's thumbnail.
  useEffect(() => {
    let imageUrl = null;
    const controller = new AbortController();

    async function getThumbnail() {
      try {
        setThumbnail(null);
        setError(null);

        const response = await fetch(
          `${import.meta.env.VITE_API_URL}/thumbnail/${encodeURIComponent(filename)}`,
          { signal: controller.signal }
        );

        if (!response.ok) {
          throw new Error('Failed to fetch thumbnail');
        }

        const imageBlob = await response.blob();

        if (controller.signal.aborted) return;

        imageUrl = URL.createObjectURL(imageBlob);
        setThumbnail(imageUrl);

      } catch (err) {
        if (!controller.signal.aborted) {
          setError(err.message);
        }
      }
    }

    getThumbnail();

        return () => {
        controller.abort();

        if (imageUrl) {
            URL.revokeObjectURL(imageUrl);
        }
        };
    }, [filename]);

    const { submitVideo,
        isSubmitting,
        jobId,
        jobStatus,
        csvUrl,
        processError
    } = useVideoProcessing(filename);


  return (
    <div>


      <h2>Preview: {filename}</h2>

      {error && <p role="alert">{error}</p>}

      <ProcessingControls
            targetColor={targetColor}
            setTargetColor={setTargetColor}
            threshold={threshold}
            setThreshold={setThreshold}
        />
        {thumbnail && (
        <PreviewCanvases
            thumbnail={thumbnail}
            targetColor={targetColor}
            threshold={threshold}
            onImageError={setError}
        />
        )}

      <div>
          <Button
            variant="contained"
            size="large"
            onClick={() => submitVideo(targetColor, threshold)}
            disabled={isSubmitting || !thumbnail}
            sx={{ mt: 3, mb: 2 }}
          >
            {isSubmitting
              ? 'Submitting...'
              : 'Process Video with These Settings'}
          </Button>
            <JobStatus
                jobId={jobId}
                jobStatus={jobStatus}
                csvUrl={csvUrl}
                processError={processError}
            />
        </div>
    </div>
    
  );
}
