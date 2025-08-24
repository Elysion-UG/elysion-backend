# GitHub Projects Setup Guide

This guide will help you set up GitHub Projects for managing story points across multiple repositories.

## Overview

GitHub Projects is a powerful project management tool that integrates directly with your repositories, allowing you to track issues, pull requests, and manage story points effectively.

## Setup Instructions

### 1. Create a GitHub Project

1. Navigate to your GitHub organization: https://github.com/Elysion-UG
2. Click on "Projects" tab
3. Click "New project"
4. Choose "Table" or "Board" view based on your preference
5. Name your project (e.g., "Elysion Development Backlog")

### 2. Configure Project Fields

Add these custom fields to track story points and other important metadata:

#### Essential Fields:
- **Story Points** (Number field)
  - Description: "Effort estimation using Fibonacci sequence"
  - Options: 1, 2, 3, 5, 8, 13, 21

- **Priority** (Single select)
  - Options: Critical, High, Medium, Low

- **Sprint** (Single select)
  - Options: Backlog, Sprint 1, Sprint 2, etc.

- **Component** (Single select)  
  - Options: Backend, Frontend, API, Database, DevOps, Documentation

- **Status** (Single select)
  - Options: 📋 Backlog, 🔍 Ready, 🏗️ In Progress, 👀 In Review, ✅ Done

#### Optional Fields:
- **Epic** (Text field)
- **Team** (Single select)
- **Iteration** (Iteration field)
- **Effort Remaining** (Number field)

### 3. Connect Repositories

1. In your project, click "Settings"
2. Under "Manage access", add repositories:
   - `Elysion-UG/elysion-backend` (this repository)
   - Add your other repository as needed

### 4. Configure Views

Create different views for different perspectives:

#### Backlog View (Table)
- Filter: Status = "📋 Backlog"
- Sort by: Priority (descending), then Story Points (ascending)
- Group by: Component

#### Sprint Board (Board)
- Group by: Status
- Filter: Sprint = "Current Sprint"
- Sort by: Priority

#### Story Points View (Table)
- Show all items
- Sort by: Story Points (descending)
- Group by: Component
- Include sum of story points

## Using Story Points

### Estimation Guidelines

- **1 Point**: Very small tasks (< 2 hours)
  - Small bug fixes
  - Minor documentation updates
  - Simple configuration changes

- **2 Points**: Small tasks (2-4 hours)
  - Small feature additions
  - Minor refactoring
  - Simple test additions

- **3 Points**: Medium-small tasks (4-8 hours)
  - Medium feature implementation
  - Moderate API changes
  - Database schema updates

- **5 Points**: Medium tasks (1-2 days)
  - Complex feature implementation
  - Significant refactoring
  - Integration work

- **8 Points**: Large tasks (2-3 days)
  - Major feature development
  - Complex integrations
  - Performance optimizations

- **13 Points**: Very large tasks (3-5 days)
  - Major architectural changes
  - Complex system integrations
  - Large-scale refactoring

- **21 Points**: Epic tasks (1+ weeks)
  - Should be broken down into smaller tasks
  - Architectural overhauls
  - Major system redesigns

### Sprint Planning

1. **Sprint Capacity**: Determine team capacity in story points
2. **Velocity Tracking**: Track completed story points per sprint
3. **Backlog Refinement**: Regularly estimate and re-estimate items
4. **Sprint Review**: Analyze actual vs. estimated effort

## Automation

### Linking Issues and PRs

The issue templates in this repository automatically include story point fields. When creating issues:

1. Use the appropriate template (User Story, Task, or Bug Report)
2. Fill in the story points field
3. The issue will automatically appear in your GitHub Project
4. Update the project status as work progresses

### Workflow Integration

Consider setting up automation rules in your GitHub Project:

1. **Auto-assign to project**: New issues/PRs automatically added
2. **Status transitions**: Move items when PRs are created/merged
3. **Notifications**: Alert team when high-priority items are created

## Best Practices

### Story Point Estimation
- Use relative sizing, not absolute time
- Include the whole team in estimation
- Re-estimate when scope changes
- Track actual vs. estimated for learning

### Project Management
- Keep the backlog prioritized
- Limit work in progress
- Regular sprint reviews and retrospectives
- Use labels consistently

### Cross-Repository Management
- Use consistent labeling across repositories
- Reference related issues with `org/repo#issue-number`
- Coordinate releases across repositories
- Share velocity metrics across teams

## Example Workflow

1. **Product Owner** creates user stories using the User Story template
2. **Development Team** breaks down stories into tasks using the Task template
3. **Team** estimates story points during planning meetings
4. **Sprint Planning**: Add items to current sprint in GitHub Project
5. **Development**: Move items through status columns as work progresses
6. **Sprint Review**: Analyze completed story points and adjust future planning

## Integration with Existing Tools

### With GitHub Features
- Link to pull requests and commits
- Use GitHub Milestones for release planning
- Integrate with GitHub Actions for automation

### With External Tools
- Export data for reporting
- Sync with time tracking tools
- Integration with Slack/Teams for notifications

## Support

For questions about GitHub Projects setup:
1. Check the [GitHub Projects documentation](https://docs.github.com/en/issues/planning-and-tracking-with-projects)
2. Contact the development team lead
3. Create a task using the Task template for project management improvements