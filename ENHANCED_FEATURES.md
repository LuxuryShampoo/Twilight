# Enhanced Event Creation and Smart Scheduling

## Overview

This implementation adds a much simpler and more intuitive way to create and schedule tasks in the Twilight calendar application. The new system allows users to quickly add tasks with specific parameters and automatically organize them into available time slots.

## New Features

### 1. Quick Add Task Button

A new green "⚡ Quick Add Task" button provides access to an enhanced event creation dialog that simplifies task input.

### 2. Enhanced Event Dialog

The new dialog includes:

#### Task Name
Simple text input for the task title (e.g., "Math homework", "SAT practice")

#### Task Types
Users can select from different task types that affect how they're scheduled:
- **Assignment**: One-off assignments completed in one sitting
- **Homework**: Regular homework tasks
- **Project**: Multi-session projects broken into multiple work sessions (max 2 hours per session)
- **SAT Study**: SAT studying scheduled in optimal time blocks (75-minute sessions)
- **Exam Prep**: Exam preparation tasks
- **Reading**: Reading assignments
- **Practice**: Practice problems/exercises

#### Time Estimation Methods

**Question-Based Mode** (Default):
- Number of Questions: Slider from 1-100 questions
- Time per Question: Slider from 1-30 minutes per question
- Automatically calculates total time needed

**Manual Time Mode**:
- Estimated Hours: Slider from 0.25-8 hours
- Direct time input for tasks not based on questions

#### Urgency Levels
Color-coded urgency levels affect scheduling priority:
- **Low** (Green #10b981): Scheduled when convenient
- **Medium** (Blue #3b82f6): Normal priority
- **High** (Orange #f59e0b): Scheduled as soon as possible, set as ACTIVE mode
- **Critical** (Red #dc2626): Highest priority, set as ACTIVE mode

#### Notes (Optional)
Free-form text area for additional task notes

### 3. Unscheduled Tasks Buffer

Tasks created with the Quick Add button go into an "Unscheduled Tasks" buffer where you can:
- Review all pending tasks
- See calculated time requirements
- Remove tasks if needed
- Manually schedule them or use auto-scheduling

### 4. Smart Scheduling Algorithm

The "🧠 Smart Schedule" button (purple, appears when unscheduled tasks exist) automatically organizes tasks using an intelligent algorithm:

#### How It Works

1. **Detects FREE Time Blocks**: Identifies all calendar events labeled "FREE" as available time
2. **Prioritizes by Urgency**: Schedules CRITICAL and HIGH urgency tasks first
3. **Handles Task Types Appropriately**:
   - **Projects**: Breaks into 2-hour maximum work sessions across multiple days
   - **SAT Study**: Creates optimal 75-minute study sessions
   - **Assignments/Homework**: Schedules in single blocks when possible
   - **Other Types**: Uses default single-block scheduling

4. **Respects Time Constraints**: Only schedules tasks in blocks that have sufficient time
5. **Color Codes Events**: Uses urgency-level colors for visual organization

#### Smart Features

- **Session Breaking**: Projects are automatically split into manageable work sessions
- **Time Optimization**: SAT study sessions are optimally timed for focus
- **Priority Scheduling**: Critical tasks get the first available time slots
- **Efficient Packing**: Fills FREE blocks efficiently, creating new smaller blocks if time remains

### 5. Display Features

#### Unscheduled Tasks Panel
Shows pending tasks with:
- Task title and description
- Task type
- Total time calculation
- Urgency level
- Quick remove button

#### Calendar Integration
Scheduled tasks appear on the calendar with:
- Color-coded by urgency
- Session numbers for multi-part tasks
- Proper time blocking
- Drag-and-drop support (inherited from existing features)

## Usage Example

### Scenario: Student with SAT homework

1. Click "⚡ Quick Add Task"
2. Enter task name: "SAT Math Practice"
3. Select task type: "SAT Study"
4. Choose "# of Questions" mode
5. Set questions: 50
6. Set time per question: 2 minutes
7. Total shows: 1.67 hours (100 minutes)
8. Select urgency: "High"
9. Add note: "Focus on algebra section"
10. Click "Create Task"

The task now appears in the Unscheduled Tasks buffer.

### Auto-Scheduling

1. Review unscheduled tasks
2. Click "🧠 Smart Schedule (1)" button
3. The algorithm:
   - Finds the next available FREE block
   - Creates a 75-minute SAT study session (optimal for SAT)
   - Schedules it with High urgency (orange, ACTIVE mode)
   - Places it in the calendar

## Technical Implementation

### New Data Fields

```kotlin
data class CalendarEvent(
    // ... existing fields ...
    var taskType: TaskType? = null,
    var numQuestions: Int? = null,
    var timePerQuestion: Double? = null, // in minutes
    var urgencyLevel: UrgencyLevel? = null
)
```

### New Enums

```kotlin
enum class TaskType {
    PROJECT, ASSIGNMENT, SAT_STUDY, HOMEWORK, 
    EXAM_PREP, READING, PRACTICE, OTHER
}

enum class UrgencyLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}
```

### Computed Properties

```kotlin
val calculatedTimeInMinutes: Double?
    get() = if (numQuestions != null && timePerQuestion != null) {
        numQuestions!! * timePerQuestion!!
    } else null

val calculatedTimeInHours: Double?
    get() = calculatedTimeInMinutes?.let { it / 60.0 }

val isFreeBlock: Boolean
    get() = title.contains("FREE", ignoreCase = true)
```

### SmartScheduler Algorithm

The `SmartScheduler` object provides:
- `findFreeBlocks()`: Identifies available time slots
- `scheduleTasksIntoFreeBlocks()`: Main scheduling algorithm
- Task-type-specific scheduling methods:
  - `scheduleProjectTask()`: Multi-session project scheduling
  - `scheduleSATStudy()`: Optimal SAT study session creation
  - `scheduleOneShotTask()`: Single-block task scheduling

## Benefits

1. **Simplified Input**: Question-based time estimation is much easier than estimating hours
2. **Automatic Organization**: No manual time-slot finding needed
3. **Smart Handling**: Different task types are scheduled appropriately
4. **Priority Awareness**: Urgent tasks get scheduled first
5. **Visual Clarity**: Color coding makes urgency obvious
6. **Flexible**: Both question-based and manual time input supported

## Future Enhancements

Possible future improvements:
- Due date support with deadline-aware scheduling
- Break time insertion between tasks
- Study session optimization based on subject
- Learning from user preferences
- Integration with external calendars
- Task templates for common assignments
