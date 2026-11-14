import path from 'path';

export function isSafeFilename(name) {
  return (
    typeof name === 'string' &&
    name.length > 0 &&
    !/[\\/]/.test(name) &&
    name !== '.' &&
    name !== '..' &&
    name === path.basename(name)
  );
}

export function isValidColor(color) {
  return typeof color === 'string' && /^[0-9a-fA-F]{6}$/.test(color);
}

export function isValidThreshold(threshold) {
  return typeof threshold === 'string' && /^\d+$/.test(threshold);
}

export function isUuid(id) {
  return (
    typeof id === 'string' &&
    /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(id)
  );
}
