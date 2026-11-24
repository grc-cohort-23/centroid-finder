import { Alert, LinearProgress, Stack, Typography } from '@mui/material'

export default function withRequestStatus(Component) {
  return function WithRequestStatus({ status, error, loadingMessage, isUpdating, ...props }) {
    if (status === 'loading') {
      return (
        <Stack spacing={1} role="status">
          <Typography>{loadingMessage}</Typography>
          <LinearProgress aria-label={loadingMessage} />
        </Stack>
      )
    }
    if (status === 'error') return <Alert severity="error">{error}</Alert>
    return (
      <div style={{ position: 'relative' }}>
        {isUpdating && (
          <LinearProgress
            style={{
              position: 'absolute',
              top: 0,
              left: 0,
              right: 0,
              height: 3,
              zIndex: 10,
              borderRadius: 2,
            }}
          />
        )}
        <Component {...props} isUpdating={isUpdating} />
      </div>
    )
  }
}

