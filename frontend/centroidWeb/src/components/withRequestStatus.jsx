import { Alert, LinearProgress, Stack, Typography } from '@mui/material'

export default function withRequestStatus(Component) {
  return function WithRequestStatus({ status, error, loadingMessage, ...props }) {
    if (status === 'loading') {
      return (
        <Stack spacing={1} role="status">
          <Typography>{loadingMessage}</Typography>
          <LinearProgress aria-label={loadingMessage} />
        </Stack>
      )
    }
    if (status === 'error') return <Alert severity="error">{error}</Alert>
    return <Component {...props} />
  }
}
