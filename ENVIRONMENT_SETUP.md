# PulmoCare Environment Configuration Guide

## Confidential Information

This project uses environment variables to securely manage confidential information like API keys and database credentials. All sensitive information has been removed from the codebase and moved to environment variables.

## Setup Instructions

### 1. Environment Variables

Create a `.env` file in the `backend` directory with the following variables:

```
# MongoDB Connection Settings
MONGODB_URI=mongodb+srv://username:password@host/
MONGODB_DATABASE=your_database_name

# Server Configuration 
SERVER_PORT=8081
GEMINI_API_KEY=your_api_key_here
```

### 2. Google Service Account Key

For Google Vertex AI integration, you need to create a service account key:

1. Copy the `vertex-key.json.template` file in the `backend/src/main/resources` directory
2. Rename it to `vertex-key.json`
3. Fill in your actual service account details

### 3. Application Properties

The `application.properties` file has been updated to use environment variables. You don't need to modify it, as it will automatically pick up values from your `.env` file.

## Important Security Notes

1. **Never commit** the following files to version control:
   - `.env` files
   - `application.properties` with actual credentials
   - `vertex-key.json` with actual service account details

2. The `.gitignore` file has been updated to exclude these sensitive files.

3. Reference templates are provided for all configuration files (with `.template` suffix).

## Development Workflow

When a new team member joins the project, they should:

1. Copy each `.template` file and remove the `.template` suffix
2. Update the copies with their actual credentials
3. Never commit these files back to the repository

This ensures that sensitive information remains private while allowing team members to correctly configure their development environment.
