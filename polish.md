

Plan:
## 1
Readme:
Things a recruiter tends to look for:
one sentence on what the project does
the GIF, near the top
the tech stack (Java, JavaCV, Express, TypeScript, Neon, React)
how to run it
who did what
A real number is impressive, like "processing an 8-minute video went from 77 s to about 6.5 s".

## 2
A GIF of the frontend. On a Mac, you can record the screen with Cmd+Shift+5, then turn the recording into a GIF with a free tool. Keep it short (10–20 seconds) and under about 10 MB so GitHub shows it.
Removing unneeded files. From the earlier review, the candidates are:
JavaCVExperiment.java
processor/extractedFrame.png
the salamander-*.csv files in the root folder
the planning notes, if you don't want them public: notes.md, options.md, ideas.md
check with your instructor before deleting files they grade, like humanImprovements.md, robotImprovements.md and priorityImprovements.md

## 3 extra idea
Test badges and CI for the server: a green "tests passing" badge at the top of the README. It's a small amount of work and looks professional.

A live demo link: host the frontend and server somewhere free, so a recruiter can try it without installing anything. This is more work, because of the JAR size and ffmpeg.

A short "how it works" diagram: video → JAR → CSV → API → frontend. It shows you understand the whole system.

A before/after section: a frame with the tracked path drawn on top, or the speed-up numbers.

Clean commit history and PR links: link a few of your best PRs from the README to show teamwork and code review.

Consistent code style: add Prettier for the server, with a format script, so all the code looks the same.