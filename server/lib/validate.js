import path from 'path';

/**
 * Returns true only if `name` is a plain filename with no path separators or
 * directory-traversal sequences. Guards against inputs like `../../etc/passwd`.
 *
 * @param {unknown} name
 * @returns {boolean}
 */
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

/**
 * Returns true if `color` is a 6-digit hex string (RRGGBB, no leading `#`).
 *
 * @param {unknown} color
 * @returns {boolean}
 */
export function isValidColor(color) {
  return typeof color === 'string' && /^[0-9a-fA-F]{6}$/.test(color);
}

/**
 * Returns true if `threshold` is a string containing only digits (whole number).
 *
 * @param {unknown} threshold
 * @returns {boolean}
 */
export function isValidThreshold(threshold) {
  return typeof threshold === 'string' && /^\d+$/.test(threshold);
}

/**
 * Returns true if `id` matches the standard UUID v4 format.
 * Used by jobStore to reject obviously invalid jobIds before hitting the
 * filesystem.
 *
 * @param {unknown} id
 * @returns {boolean}
 */
export function isUuid(id) {
  return (
    typeof id === 'string' &&
    /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(id)
  );
}
