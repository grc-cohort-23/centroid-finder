
/**
 * HIGHER ORDER COMPONENT 
 * Higher-Order Component that handles
 * loading and error states.
 *
 * Accepts a component and returns an enhanced component.
 */
export default function withRequestState(WrappedComponent) {

  return function WithRequestState({ loading, error, ...props }) {

    if (loading) {
      return <p>Loading...</p>;
    }

    if (error) {
      return <p role="alert">Error: {error}</p>;
    }

    return <WrappedComponent {...props} />;
  };
}