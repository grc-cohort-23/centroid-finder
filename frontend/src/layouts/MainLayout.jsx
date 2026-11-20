
import { Outlet, Link } from 'react-router-dom';
import {
  Container,
  Box,
  Typography,
  Button,
  CssBaseline
} from '@mui/material';

export default function MainLayout() {
  return (
    <>
      <CssBaseline />

      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Box
          component="header"
          sx={{ mb: 4 }}
        >
          <Typography variant="h4" gutterBottom>
            Centroid Finder Video Processor
          </Typography>

          <Button
            component={Link}
            to="/videos"
            variant="outlined"
          >
            Available Videos
          </Button>
        </Box>

        <Box component="main">
          <Outlet />
        </Box>
      </Container>
    </>
  );
}
