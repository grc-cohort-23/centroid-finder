import { Alert, Stack, Typography } from '@mui/material'

/**
 * Higher-order component that adds loading and error UI to a
 * component whose data comes from an async request.
 *
 * The returned component accepts the wrapped component's props plus:
 * - `status`: `'loading' | 'error' | 'ready'` (any other value is treated as ready).
 *     - `loading` – renders `loadingMessage`; the wrapped component is NOT rendered.
 *     - `error`   – renders `error` in an Alert; the wrapped component is NOT rendered.
 *     - otherwise – renders the wrapped component.
 * - `error`: message shown when `status === 'error'`.
 * - `loadingMessage`: text shown when loading.
 * - `isUpdating`: forwarded to the wrapped component for seamless live updating.
 *
 * All remaining props are passed through unchanged.
 *
 * @template P
 * @param {React.ComponentType<P & {isUpdating?: boolean}>} Component - Component to wrap.
 * @returns {React.ComponentType<P & {status: 'loading'|'error'|'ready', error?: string, loadingMessage?: string, isUpdating?: boolean}>}
 */
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
