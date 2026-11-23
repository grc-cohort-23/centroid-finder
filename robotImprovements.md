Kelley's AI Suggestions:
- VideoExperiment is a prototype: no validation, hardcoded -1,-1, a redundant stop(), and it duplicates the real pipeline. Delete it or move it out of the shipped sources.
- main calls System.exit directly. Move the logic into a method that returns an exit code, and let main just exit with it. That makes the whole CLI testable.
- Tests to add (building on the earlier list)
VideoProcessor: Use a fake FrameSource and ResultWriter.
An empty source produces zero results.
N frames produce N writes, in order.
A source that throws midway, and a writer that throws, each propagate the exception.
Check that the processor doesn't close either collaborator.
JavaCvFrameSource:
Use a tiny checked-in video fixture, for example a short clip with a solid-colored square at a known position.
Test nonexistent and corrupt files, a zero or tiny step, NaN, and a step larger than the video.
- JavaCvFrameSource, VideoProcessorApp, and Group have no tests. These hold the infinite-loop risk, the silent-truncation behavior, and the System.exit problem from last round.

Kate's AI Suggestions: