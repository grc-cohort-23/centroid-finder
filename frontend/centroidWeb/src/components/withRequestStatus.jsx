import { Alert, Stack, Typography } from '@mui/material'

export default function withRequestStatus(Component) {
  return function WithRequestStatus({ status, error, loadingMessage, isUpdating, ...props }) {
    if (status === 'loading') {
      return (
        <Stack spacing={1} role="status" alignItems="center" sx={{ py: 3 }}>
          <Typography color="text.secondary">{loadingMessage}</Typography>
        </Stack>
      )
    }
    if (status === 'error') return <Alert severity="error">{error}</Alert>
    return <Component {...props} isUpdating={isUpdating} />
  }
}
