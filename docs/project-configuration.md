# Elysion GitHub Project Configuration

## Project Details
- **Name**: Elysion Story Point Tracker
- **Description**: Cross-repository project management with story point estimation
- **Template**: Table view with custom fields

## Custom Fields Configuration

### Story Points (Number Field)
```yaml
name: "Story Points"
dataType: "NUMBER"
description: "Effort estimation using Fibonacci sequence (1,2,3,5,8,13,21)"
```

### Priority (Single Select Field)
```yaml
name: "Priority" 
dataType: "SINGLE_SELECT"
options:
  - name: "Critical"
    color: "red"
  - name: "High"
    color: "orange"  
  - name: "Medium"
    color: "yellow"
  - name: "Low"
    color: "green"
```

### Status (Single Select Field)
```yaml
name: "Status"
dataType: "SINGLE_SELECT" 
options:
  - name: "📋 Backlog"
    color: "gray"
  - name: "🔍 Ready"
    color: "blue"
  - name: "🏗️ In Progress"
    color: "yellow"
  - name: "👀 In Review"
    color: "orange"
  - name: "✅ Done"
    color: "green"
```

### Component (Single Select Field)
```yaml
name: "Component"
dataType: "SINGLE_SELECT"
options:
  - name: "Backend"
    color: "blue"
  - name: "Frontend" 
    color: "green"
  - name: "API"
    color: "purple"
  - name: "Database"
    color: "orange"
  - name: "DevOps"
    color: "red"
  - name: "Documentation"
    color: "gray"
```

### Sprint (Single Select Field)
```yaml
name: "Sprint"
dataType: "SINGLE_SELECT"
options:
  - name: "Backlog"
    color: "gray"
  - name: "Sprint 1"
    color: "blue"
  - name: "Sprint 2" 
    color: "green"
  - name: "Sprint 3"
    color: "orange"
  # Add more sprints as needed
```

## Recommended Views

### 1. Sprint Board (Board View)
- **Group by**: Status
- **Filter**: Sprint = "Current Sprint"
- **Sort**: Priority (High to Low)

### 2. Backlog (Table View)  
- **Filter**: Status = "📋 Backlog"
- **Sort**: Priority (High to Low), Story Points (Low to High)
- **Group by**: Component

### 3. Story Points Overview (Table View)
- **Show**: All items
- **Sort**: Story Points (High to Low)
- **Group by**: Component
- **Summary**: Sum of Story Points by Component

### 4. Current Sprint (Table View)
- **Filter**: Sprint = "Current Sprint" AND Status != "✅ Done"
- **Sort**: Priority (High to Low)
- **Show fields**: Title, Story Points, Priority, Status, Assignee

## Automation Rules (Optional)

### Auto-add items to project
```yaml
trigger: "item_added"
condition: "issue_or_pr_opened"
action: "set_field_value"
field: "Status"
value: "📋 Backlog"
```

### Move to "In Progress" when assigned
```yaml
trigger: "item_updated"
condition: "assignee_added"
action: "set_field_value"
field: "Status" 
value: "🏗️ In Progress"
```

### Move to "In Review" when PR opened
```yaml
trigger: "item_updated"
condition: "pr_opened_for_issue"
action: "set_field_value"
field: "Status"
value: "👀 In Review"
```

### Move to "Done" when PR merged
```yaml
trigger: "item_updated"
condition: "pr_merged"
action: "set_field_value"
field: "Status"
value: "✅ Done"
```

## Labels Configuration

### Story Point Labels
- `sp-1`, `sp-2`, `sp-3`, `sp-5`, `sp-8`, `sp-13`, `sp-21`

### Size Labels  
- `size/small` (1-2 points)
- `size/medium` (3-5 points)
- `size/large` (8-13 points)
- `size/extra-large` (21+ points)

### Priority Labels
- `priority/critical`
- `priority/high`
- `priority/medium`
- `priority/low`

### Type Labels
- `user-story`
- `task`
- `bug`
- `enhancement`
- `documentation`

## Setup Instructions

1. **Create Project**: Follow the [Quick Start Guide](project-management-quickstart.md)
2. **Import Configuration**: Use the field configurations above
3. **Set up Views**: Create the recommended views
4. **Configure Automation**: Set up the automation rules (optional)
5. **Add Labels**: Create the suggested labels in your repositories
6. **Update Workflow**: Edit `.github/workflows/project-automation.yml` with your project number

## Multi-Repository Setup

To manage story points across multiple repositories:

1. **Single Project**: Create one project for all repositories
2. **Connect Repos**: Add all relevant repositories to the project
3. **Consistent Templates**: Copy the issue templates to all repositories
4. **Cross-Reference**: Use `org/repo#issue-number` format for linking
5. **Shared Labels**: Use consistent labeling across repositories

## Repository-Specific Notes

### Elysion Backend (`elysion-backend`)
- Focus on backend API development
- Include database migration tasks
- Consider Quarkus-specific testing requirements
- Integration with user preference features

### Your Other Repository
- Copy the `.github/ISSUE_TEMPLATE/` folder to enable story points
- Adjust the templates to match your specific needs
- Ensure consistent labeling with this repository