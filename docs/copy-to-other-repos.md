# Copy This to Other Repositories

To enable story point management in your other repositories, copy these files:

## Required Files to Copy

### 1. Issue Templates
Copy the entire `.github/ISSUE_TEMPLATE/` directory:
```
.github/ISSUE_TEMPLATE/
├── bug_report.yml
├── config.yml
├── task.yml
└── user_story.yml
```

### 2. Pull Request Template
Copy the file:
```
.github/PULL_REQUEST_TEMPLATE.md
```

### 3. Documentation (Optional)
Copy the documentation if you want local copies:
```
docs/
├── github-projects-setup.md
├── project-configuration.md
└── project-management-quickstart.md
```

### 4. Automation Workflow (Optional)
Copy and customize:
```
.github/workflows/project-automation.yml
```

## Customization Guide

### For Different Repository Types

#### Frontend Repository
Adjust the "Component" options in issue templates:
- Replace "Backend" with "Components"
- Add "UI/UX", "Styling", "Routing" options
- Update story point estimates for frontend-specific work

#### Mobile Repository  
- Add "iOS", "Android", "Cross-platform" component options
- Include device-specific testing considerations
- Adjust story point estimates for mobile development

#### DevOps Repository
- Focus on "Infrastructure", "CI/CD", "Monitoring" components
- Include deployment and scalability considerations
- Adjust story points for infrastructure work

### Repository-Specific Labels

Create these labels in each repository for consistency:

```bash
# Story Point Labels
gh label create "sp-1" --description "1 story point" --color "d4edda"
gh label create "sp-2" --description "2 story points" --color "c3e6cb" 
gh label create "sp-3" --description "3 story points" --color "b7dfbb"
gh label create "sp-5" --description "5 story points" --color "a8d8ea"
gh label create "sp-8" --description "8 story points" --color "98d8c8"
gh label create "sp-13" --description "13 story points" --color "f7dc6f"
gh label create "sp-21" --description "21 story points" --color "f8c471"

# Size Labels
gh label create "size/small" --description "1-2 story points" --color "d4edda"
gh label create "size/medium" --description "3-5 story points" --color "fff3cd" 
gh label create "size/large" --description "8-13 story points" --color "f8d7da"
gh label create "size/extra-large" --description "21+ story points" --color "d1ecf1"

# Priority Labels  
gh label create "priority/critical" --description "Critical priority" --color "d73a49"
gh label create "priority/high" --description "High priority" --color "fd7e14"
gh label create "priority/medium" --description "Medium priority" --color "ffd33d"
gh label create "priority/low" --description "Low priority" --color "28a745"
```

## Quick Setup Commands

Run these commands in your other repository:

```bash
# 1. Copy issue templates (adjust source path as needed)
cp -r /path/to/elysion-backend/.github/ISSUE_TEMPLATE/ .github/
cp /path/to/elysion-backend/.github/PULL_REQUEST_TEMPLATE.md .github/

# 2. Copy documentation (optional)
mkdir -p docs
cp /path/to/elysion-backend/docs/project-* docs/

# 3. Copy automation workflow (optional) 
cp /path/to/elysion-backend/.github/workflows/project-automation.yml .github/workflows/

# 4. Create labels (requires GitHub CLI)
gh label create "sp-1" --description "1 story point" --color "d4edda"
# ... (repeat for all labels above)

# 5. Commit changes
git add .github/ docs/
git commit -m "Add GitHub Projects story point management templates"
git push
```

## Integration Notes

### Linking Across Repositories
- Reference issues in other repos: `Elysion-UG/elysion-backend#123`
- Use consistent story point scales across all repositories
- Coordinate sprint planning across repositories

### Project Management
- Use one GitHub Project for all repositories
- Or create separate projects per repository/team
- Ensure story point estimates are consistent across repos

---

**Need Help?**
- Check the [Quick Start Guide](project-management-quickstart.md)
- Review the [Full Setup Guide](github-projects-setup.md)
- Create an issue using the Task template for additional automation needs