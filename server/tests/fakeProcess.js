const { EventEmitter } = require('events');

// A stand-in for the object child_process.spawn() returns, so tests can
// pretend to be Java or ffmpeg by emitting 'data', 'close' and 'error'.
function fakeProcess() {
  const proc = new EventEmitter();
  proc.stdout = new EventEmitter();
  proc.stderr = new EventEmitter();
  return proc;
}

module.exports = { fakeProcess };
