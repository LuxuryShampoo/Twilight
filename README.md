# Twilight Calendar

A smart calendar application built with [Kobweb](https://github.com/varabyte/kobweb) that helps you manage your time efficiently with intelligent task scheduling.

## Features

### ⚡ Quick Add Task
Simplified task creation with:
- **Task Types**: Assignment, Homework, Project, SAT Study, Exam Prep, Reading, Practice
- **Flexible Time Input**: Question-based (# questions × time per question) or manual hours
- **Urgency Levels**: Low, Medium, High, Critical with color coding
- **Smart Auto-Scheduling**: Automatically organizes tasks into available FREE time blocks

### 🧠 Smart Scheduling Algorithm
- Detects available FREE time blocks in your schedule
- Prioritizes tasks by urgency level
- Breaks projects into manageable work sessions (max 2 hours)
- Optimizes SAT study sessions (75-minute blocks)
- Schedules one-off assignments in single sittings

### 📅 Calendar Features
- Weekly view with 30-minute time slots
- Drag-and-drop event rescheduling
- Event color coding by urgency
- Dark mode support
- Recurring events
- Event editing and deletion

For detailed feature documentation, see [ENHANCED_FEATURES.md](./ENHANCED_FEATURES.md)

## Getting Started

First, run the development server by typing the following command in a terminal under the `site` folder:

```bash
$ cd site
$ kobweb run
```

Open [http://localhost:8080](http://localhost:8080) with your browser to see the result.

You can use any editor you want for the project, but we recommend using **IntelliJ IDEA Community Edition** downloaded
using the [Toolbox App](https://www.jetbrains.com/toolbox-app/).

Press `Q` in the terminal to gracefully stop the server.

### Live Reload

Feel free to edit / add / delete new components, pages, and API endpoints! When you make any changes, the site will
indicate the status of the build and automatically reload when ready.

## Usage Example

1. Open the application
2. Click "⚡ Quick Add Task"
3. Enter your task details:
   - Task name: "Math homework"
   - Type: Assignment
   - Questions: 20
   - Time per question: 3 minutes
   - Urgency: High
4. Click "Create Task"
5. Task appears in "Unscheduled Tasks" buffer
6. Click "🧠 Smart Schedule" to automatically place it in your calendar

The algorithm will find the best available FREE time block based on task urgency and type!

## Exporting the Project

When you are ready to ship, you should shutdown the development server and then export the project using:

```bash
kobweb export
```

When finished, you can run a Kobweb server in production mode:

```bash
kobweb run --env prod
```

If you want to run this command in the Cloud provider of your choice, consider disabling interactive mode since nobody
is sitting around watching the console in that case anyway. To do that, use:

```bash
kobweb run --env prod --notty
```

Kobweb also supports exporting to a static layout which is compatible with static hosting providers, such as GitHub
Pages, Netlify, Firebase, any presumably all the others. You can read more about that approach here:
https://bitspittle.dev/blog/2022/staticdeploy

## Technology Stack

- **Framework**: Kobweb (Kotlin/JS + Compose for Web)
- **Language**: Kotlin
- **UI**: Compose for Web
- **Build System**: Gradle

## Contributing

This is a student scheduling application designed to help manage time effectively across different types of tasks and commitments.