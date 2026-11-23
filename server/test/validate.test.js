import { test } from 'node:test';
import assert from 'node:assert/strict';
import { isSafeFilename, isValidColor, isValidThreshold, isUuid } from '../lib/validate.js';

// --- isSafeFilename ---

test('isSafeFilename accepts a plain video filename', () => {
  assert.ok(isSafeFilename('ball.mp4'));
});

test('isSafeFilename accepts a filename with no extension', () => {
  assert.ok(isSafeFilename('video'));
});

test('isSafeFilename rejects path traversal with ..', () => {
  assert.ok(!isSafeFilename('../etc/passwd'));
});

test('isSafeFilename rejects bare ..', () => {
  assert.ok(!isSafeFilename('..'));
});

test('isSafeFilename rejects bare .', () => {
  assert.ok(!isSafeFilename('.'));
});

test('isSafeFilename rejects empty string', () => {
  assert.ok(!isSafeFilename(''));
});

test('isSafeFilename rejects path with forward slash', () => {
  assert.ok(!isSafeFilename('videos/ball.mp4'));
});

test('isSafeFilename rejects path with backslash', () => {
  assert.ok(!isSafeFilename('videos\\ball.mp4'));
});

// --- isValidColor ---

test('isValidColor accepts lowercase 6-char hex', () => {
  assert.ok(isValidColor('ff0000'));
});

test('isValidColor accepts uppercase 6-char hex', () => {
  assert.ok(isValidColor('FF0000'));
});

test('isValidColor accepts mixed case hex', () => {
  assert.ok(isValidColor('570D0E'));
});

test('isValidColor rejects 5-char string', () => {
  assert.ok(!isValidColor('ff000'));
});

test('isValidColor rejects non-hex characters', () => {
  assert.ok(!isValidColor('zz0000'));
});

test('isValidColor rejects empty string', () => {
  assert.ok(!isValidColor(''));
});

// --- isValidThreshold ---

test('isValidThreshold accepts digit-only string', () => {
  assert.ok(isValidThreshold('120'));
});

test('isValidThreshold accepts single digit', () => {
  assert.ok(isValidThreshold('0'));
});

test('isValidThreshold rejects decimal', () => {
  assert.ok(!isValidThreshold('12.5'));
});

test('isValidThreshold rejects letters', () => {
  assert.ok(!isValidThreshold('abc'));
});

test('isValidThreshold rejects empty string', () => {
  assert.ok(!isValidThreshold(''));
});

// --- isUuid ---

test('isUuid accepts a valid UUID', () => {
  assert.ok(isUuid('550e8400-e29b-41d4-a716-446655440000'));
});

test('isUuid rejects string without dashes', () => {
  assert.ok(!isUuid('550e8400e29b41d4a716446655440000'));
});

test('isUuid rejects a short random string', () => {
  assert.ok(!isUuid('abc123'));
});
