
import { useEffect, useRef } from 'react';

import { binarizeImage } from '../utils/imageProcessing';
import { findConnectedGroups } from '../utils/groupFinder';
import { useColorStore } from '../utils/useColorStore';



// Draw the largest group's centroid.
function drawCentroid(context, group, centroidColor) {
  if (!group) return;

  //zustand stuff for centroid color Personal touch

  
  context.beginPath();
  context.arc(group.x, group.y, 22, 0, Math.PI * 2);
  context.fillStyle = centroidColor;
  context.fill();
}

export default function PreviewCanvases({
  thumbnail,
  targetColor,
  threshold,
  onImageError
}) {
  const originalCanvasRef = useRef(null);
  const canvasRef = useRef(null);
  const centroidColor = useColorStore((state) => state.centroidColor);


  useEffect(() => {
    if (!thumbnail) return;

    const originalCanvas = originalCanvasRef.current;
    const canvas = canvasRef.current;
    
    //if either canvas is null return
    if (!originalCanvas || !canvas) return;

    const originalContext = originalCanvas.getContext('2d');
    const context = canvas.getContext('2d');

    if (!originalContext || !context) return;

    const image = new Image();
    let cancelled = false;

    image.onload = () => {
      if (cancelled) return;

      const width = image.naturalWidth;
      const height = image.naturalHeight;

      originalCanvas.width = width;
      originalCanvas.height = height;

      canvas.width = width;
      canvas.height = height;

      // Draw original image and retrieve its pixels.
      originalContext.drawImage(image, 0, 0);

      const imageData = originalContext.getImageData(
        0, 0, width, height
      );

      // Run binarization and find connected groups.
      const { binaryArray, binaryImage } = binarizeImage(
        imageData,
        targetColor,
        threshold
      );

      const groups = findConnectedGroups(binaryArray);
      const largestGroup = groups[0];

      // Render the processed image.
      context.putImageData(binaryImage, 0, 0);

      // Draw the same centroid on both images.
      drawCentroid(context, largestGroup,centroidColor);
      drawCentroid(originalContext, largestGroup,centroidColor);
    };

    image.onerror = () => {
      if (!cancelled) {
        onImageError('Failed to load thumbnail image');
      }
    };

    image.src = thumbnail;

    return () => {
      cancelled = true;
      image.onload = null;
      image.onerror = null;
    };
  }, [thumbnail, targetColor, threshold, onImageError, centroidColor]);

  return (
    <div
      style={{
        display: 'flex',
        gap: '16px',
        flexWrap: 'wrap'
      }}
    >
      <div>
        <h3>Original Thumbnail</h3>
        <canvas
          ref={originalCanvasRef}
          style={{
            width: '360px',
            maxWidth: '100%',
            height: 'auto'
          }}
        />
      </div>

      <div>
        <h3>Binarized Preview</h3>
        <canvas
          ref={canvasRef}
          style={{
            width: '360px',
            maxWidth: '100%',
            height: 'auto'
          }}
        />
      </div>
    </div>
  );
}
