# PulmoCare App

A comprehensive mobile application for pulmonary healthcare management, featuring:

- Doctor consultations and appointment scheduling
- Personal health profile management
- Symptom assessment and tracking
- Medical records integration

## Project Structure

- `mob-frontend/` - Android mobile application (Kotlin)
- `web-frontend/` - Web application (Next.js, TypeScript)
- `backend/` - Spring Boot backend server with MongoDB

## Prerequisites

- For Mobile:
  - Android Studio Electric Eel or newer
  - JDK 11+
  - Android SDK 24+
- For Web:
  - Node.js 18+
  - pnpm or npm
- For Backend:
  - JDK 11+
  - MongoDB database
  - Gradle

## Setup Instructions

### Mobile Frontend
1. Open the `mob-frontend` directory in Android Studio
2. Sync Gradle files
3. Run the application on an emulator or physical device

### Web Frontend
1. Navigate to the `web-frontend` directory
2. Run `pnpm install` (or `npm install`)
3. Run `pnpm dev` (or `npm run dev`) to start the development server

### Backend
1. Create a `.env` file in the project root with required environment variables
   - `MONGODB_URI`: MongoDB connection string
   - `MONGODB_DATABASE`: Database name
   - `GEMINI_API_KEY`: Google Gemini API key (if using AI features)
2. Navigate to the `backend` directory
3. Run `./gradlew bootRun` to start the Spring Boot server

## Features

- **User Authentication**: Secure login and registration
- **Doctor Profiles**: Browse and connect with pulmonary specialists
- **Appointment Scheduling**: Book and manage medical appointments
- **Health Tracking**: Record and monitor respiratory symptoms
- **Personal Profile**: Manage personal and medical information
- **AI-Assisted Diagnosis**: Preliminary symptom analysis and recommendations
- **Medical Records**: Store and access past appointment details and prescriptions

## Technology Stack

- **Mobile Frontend**: Kotlin, Jetpack Compose
- **Web Frontend**: Next.js, TypeScript, Tailwind CSS
- **Backend**: Spring Boot, MongoDB
- **Authentication**: JWT
- **AI Integration**: Google Gemini API, ONNX

## Security Notice

⚠️ **Important**: Never commit sensitive information like API keys, database credentials, or JWT secrets to your repository. Use environment variables instead.
