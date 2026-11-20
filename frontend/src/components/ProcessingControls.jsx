
import {
  Paper,
  Stack,
  Typography,
  Slider
} from '@mui/material';
import {useColorStore} from '../utils/useColorStore.js';

export default function ProcessingControls({
  targetColor,
  setTargetColor,
  threshold,
  setThreshold
  
}) {

  const centroidColor = useColorStore((state) => state.centroidColor);
  const changeCentroidColor = useColorStore( (state) => state.changeCentroidColor );

  return (
  <Paper sx={{ p: 3, my: 3 }}>
    <Typography variant="h6" gutterBottom>
      Detection Settings
    </Typography>

    <Stack spacing={3}>
      {/* Color controls */}
      <Stack direction="row" spacing={3} alignItems="center">

        {/* Target color */}
        <Stack direction="row" spacing={1} alignItems="center">
          <Typography component="label" htmlFor="targetColor">
            Target Color:
          </Typography>

          <input
            id="targetColor"
            type="color"
            value={targetColor}
            onChange={(e) => setTargetColor(e.target.value)}
          />

          <Typography variant="body2">
            {targetColor.toUpperCase()}
          </Typography>
        </Stack>

        {/* Centroid color */}
        <Stack direction="row" spacing={1} alignItems="center">
          <Typography component="label" htmlFor="centroidColor">
            Centroid Color:
          </Typography>

          <input
            id="centroidColor"
            type="color"
            value={centroidColor}
            onChange={(e) => changeCentroidColor(e.target.value)}
          />

          <Typography variant="body2">
            {centroidColor.toUpperCase()}
          </Typography>
        </Stack>
      </Stack>

      {/* Threshold control */}
      <div>
        <Typography id="threshold-label" gutterBottom>
          Threshold: {threshold}
        </Typography>

        <Slider
          aria-labelledby="threshold-label"
          value={threshold}
          min={0}
          max={442}
          step={1}
          onChange={(_, newValue) => setThreshold(newValue)}
          valueLabelDisplay="auto"
        />
      </div>
    </Stack>
  </Paper>
  );
}
