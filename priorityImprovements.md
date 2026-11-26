## Refactoring code
- **(high) [Frontend]** `Workspace.jsx` is 661 lines and does everything. Split it into pieces: the frame
  render loop (a hook), the keyboard shortcuts (a hook), the job card (`JobCard` and `previewHint` already
  live at the bottom of the file), and the settings panel. Move constants out too.

## Adding tests
- **(high) [Both]** CI only runs the Java tests, and the front-end repo has no CI at all. Add a server job
  (`npm test`) and a front-end job (`npm test`), and run on push as well as PRs.

## Improving error handling
- **(high) [Server]** The Java app exits with code 0 on failure, so the server scrapes stdout for a success
  message. Exit non-zero on failure, then simplify the success check in `runner.js`.

## Writing documentation
- **(high) [Server]** Replace the assignment text in the root README.md with: what the project is, prerequisites
  (JDK, Maven), how to build the JAR, .env variables, how to start the server, how to run tests. Link to the
  front-end repo, since the app needs both.
- **[Frontend]** The README references `docs/videos-page.png` and `docs/preview-page.png`, but the `docs/`
  folder is empty, so the images are broken. Add the screenshots or remove the links.

## Bug fixes
- **[Server]** DFS input validation does not match its Javadoc: ragged rows throw ArrayIndexOutOfBounds instead
  of IllegalArgumentException, and values other than 0/1 are accepted.
