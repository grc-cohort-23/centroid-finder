import { Alert, LinearProgress, Stack, Typography } from '@mui/material'

/**
 * Higher-order component that adds loading, error, and "refreshing" UI to a
 * component whose data comes from an async request.
 *
 * The returned component accepts the wrapped component's props plus:
 * - `status`: `'loading' | 'error' | 'ready'` (any other value is treated as ready).
 *     - `loading` – renders `loadingMessage` and a progress bar; the wrapped component is NOT rendered.
 *     - `error`   – renders `error` in an Alert; the wrapped component is NOT rendered.
 *     - otherwise – renders the wrapped component.
 * - `error`: message shown when `status === 'error'`.
 * - `loadingMessage`: text shown (and used as the progress bar's aria-label) when loading.
 * - `isUpdating`: when true, a thin progress bar overlays the wrapped component to
 *   indicate a refresh. It is also forwarded to the wrapped component.
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

