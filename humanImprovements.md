https://docs.google.com/document/d/1IlyKFAqe-aC9pzrdZfmNP48ut4Q_Jqeo9khovCuQX_Y/edit?usp=sharing




# Human Improvements

## Color Key

- ⚫ **Black** (no marker) = good under human review
- 🟣 **Purple** = backlog item
- 🔵 **Blue** = needs updates now
- 🔴 **Red** = delete

---

## Frontend Repository

- Delete dead code
- Reorganize all json files so our root repository doesn't look as cluttered
- Reformat and organize our routes and APIs to be more cohesive and use similar conventions taught in Josh's class
- Separation of concerns on larger files, maybe separating a few functions to be their own files
- Add javascript documentation
- CI/CD pipeline integration for making PR's to main when working agentically as an extra precaution
- Look into hosting services that would run our front and backend as well as allow for people to pick an mp4 from their local machine (backlog for additions and features not refactor)

---

## Centroid-finder-frontend

- `src`
  - `api`
    - `client.js`: looks good to us, we like how small and readable it is
    - `process.js`: looks great, very readable
    - `thumbnail.js`: small and simple
    - `videos.js`: small and readable
  - 🔴 `assets`
    - 🔴 Empty and a part of vite react scaffolding (can be deleted)
  - `components`
    - `hoc`
      - `withLoading.jsx`: clean and explains the usage of the MUI cmp
    - 🟣 `ColorPicker.jsx`: We need to rework the logic behind the visual box appearing, it covers part of the screen when trying to use the eyedrop colorpicker
    - `JobStatusPanel.jsx`: the statuses all in one place makes sense and is clean
    - 🔵 `ThresholdSlider.jsx`: could use a few more javascript docs/comments
    - 🔵 `ThumbnailCanvas.jsx`: add more comments and javascript docs so we can explain the full context of Thumbnail canvas. Possibly separate into 2 files
    - 🟣 `VideoListItem.jsx`: renders a single clickable video (rework to be upload from PC MP4 files only)
  - `lib`
    - `centroid`
      - `binarize.js`: a live preview of the java process, working on the browser's canvas's ImageData instead of Java BufferedImage
      - `colorDistance.js`: works with thresholdSlider to update the react state showing a live preview of the expected output
      - `groupFinder.js`: logic for the live previewing of the largest group
      - `index.js`: ties all the logic together, the public entry point which the UI actually imports
    - 🟣 `recentJobs.js`: looks clean (maybe look into serverless dbs we could use to store recentjobs rather than storing them in a variable)
  - 🔴 `mock`: used for testing but can be deleted
  - 🔵 `pages`
    - 🔵 `VideoChooser.jsx`: could add more javascript comments to explain better
    - 🔵 `VideoPreview.jsx`: add more comments to understand full implementation