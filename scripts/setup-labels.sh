#!/bin/bash

# GitHub Labels Setup Script for Story Point Management
# This script creates all necessary labels for story point management
# Requires GitHub CLI (gh) to be installed and authenticated

set -e

echo "🏷️  Setting up GitHub labels for story point management..."

# Story Point Labels
echo "Creating story point labels..."
gh label create "sp-1" --description "1 story point" --color "d4edda" --force
gh label create "sp-2" --description "2 story points" --color "c3e6cb" --force
gh label create "sp-3" --description "3 story points" --color "b7dfbb" --force
gh label create "sp-5" --description "5 story points" --color "a8d8ea" --force
gh label create "sp-8" --description "8 story points" --color "98d8c8" --force
gh label create "sp-13" --description "13 story points" --color "f7dc6f" --force
gh label create "sp-21" --description "21 story points" --color "f8c471" --force

# Size Labels
echo "Creating size labels..."
gh label create "size/small" --description "1-2 story points" --color "d4edda" --force
gh label create "size/medium" --description "3-5 story points" --color "fff3cd" --force
gh label create "size/large" --description "8-13 story points" --color "f8d7da" --force
gh label create "size/extra-large" --description "21+ story points" --color "d1ecf1" --force

# Priority Labels
echo "Creating priority labels..."
gh label create "priority/critical" --description "Critical priority" --color "d73a49" --force
gh label create "priority/high" --description "High priority" --color "fd7e14" --force
gh label create "priority/medium" --description "Medium priority" --color "ffd33d" --force
gh label create "priority/low" --description "Low priority" --color "28a745" --force

# Type Labels (complementing existing ones)
echo "Creating type labels..."
gh label create "user-story" --description "User story" --color "7057ff" --force
gh label create "task" --description "Development task" --color "008672" --force

echo "✅ All labels created successfully!"
echo ""
echo "📋 Next steps:"
echo "1. Create your GitHub Project at: https://github.com/orgs/Elysion-UG/projects"
echo "2. Follow the Quick Start Guide: docs/project-management-quickstart.md"
echo "3. Start creating issues with story point estimation!"