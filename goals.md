# Goals

Theme: **Motion analysis.** Right now our project finds where one colored object is in each frame. We want to turn that into understanding how things move: how fast, which way, and where several objects are heading.

## Easy: Speed and direction
For a tracked object, figure out how fast it is moving and which direction it is going in each frame.
- New Java class that takes the `seconds,x,y` results and adds `speed` (pixels per second) and `direction` columns
- Skip frames where the object wasn't found (no speed across a gap)
- The API returns the new columns with the job results
- The frontend shows the speed next to the video

## Goal: Track multiple objects at once
Track several objects of the same color in one video and keep the same ID for each object from frame to frame.
- Use the groups `DfsBinaryGroupFinder` already finds, instead of only keeping the largest one
- Match each object to the closest object in the previous frame so its ID stays the same
- Handle objects that leave the screen or show up partway through the video
- Limit the number of objects (for example the top 3) to keep the CSV simple
- The frontend draws each object's path in its own color

## Stretch: Predict paths and warn about collisions
Use each object's speed and direction to guess where it will be in the next few seconds, and warn when two objects are going to get too close.
- Project each object's position forward using its current speed and direction
- Flag a warning when two predicted positions come closer than a set distance
- The API returns the warnings (time and which objects) with the job results
- The frontend shows predicted paths and highlights warnings in red

## Impossible: Real air traffic control
A live system that tracks hundreds of planes from radar or camera feeds in real time, predicts every flight path, and sends safety alerts before planes get too close.
