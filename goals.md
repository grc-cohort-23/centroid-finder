# Goals

Theme: **Motion analysis.** Right now our project finds where one colored object is in each frame. We want to turn that into understanding how things move: how fast, which way, and where several objects are heading.

## Easy: Speed and direction
For a tracked object, figure out how fast it is moving and which direction it is going in each frame.
- New Java class that takes the `seconds,x,y` results and adds `speed` (pixels per second) and `direction` columns
- Skip frames where the object wasn't found (no speed across a gap)
- The API returns the new columns with the job results
- The frontend shows the speed next to the video

## Goal: Track multiple objects at once
Track up to 3 objects of the same color in one video, and keep the same ID for each object from frame to frame while the objects stay apart.
- New Java tracker class that keeps the 3 largest groups `DfsBinaryGroupFinder` already finds, instead of only the largest one
- Ignore very small groups (a minimum size) so a few noisy pixels don't count as an object
- After all frames are processed, go through them in order and give each object the ID of the closest object in the previous frame
- If the closest object is too far away (a set distance), treat it as a new object, so objects that show up partway through or leave the screen get handled
- Known limit: when two objects touch or cross, they merge into one group and their IDs might swap afterwards
- Backup plan: if matching IDs gets stuck, track two objects of different colors instead (each color is one object, so no matching is needed)
- The API returns each object's ID and position with the job results
- The frontend draws each object's path in its own color

## Stretch: Predict paths and warn about collisions
Use each object's speed and direction to guess where it will be in the next few seconds, and warn when two objects are going to get too close.
- Project each object's position forward using its current speed and direction
- Flag a warning when two predicted positions come closer than a set distance
- The API returns the warnings (time and which objects) with the job results
- The frontend shows predicted paths and highlights warnings in red

## Impossible: Real air traffic control
A live system that tracks hundreds of planes from radar or camera feeds in real time, predicts every flight path, and sends safety alerts before planes get too close.
