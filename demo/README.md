# StudyPilot Backend

This is the backend service for the StudyPilot application, built with Spring Boot(Maven). It provides APIs for managing study plans, syllabi, study progress, and PDF parsing.

## Table of Contents

- [Setup](#setup)
  - [Prerequisites](#prerequisites)
  - [Environment Variables](#environment-variables)
  - [Neon Setup](#neon-setup)
  - [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
  - [Syllabus Management](#syllabus-management)
  - [Study Plan Management](#study-plan-management)
  - [PDF Parsing](#pdf-parsing)
- [File Upload Limits](#file-upload-limits)
- [Error Handling](#error-handling)
- [Contributing](#contributing)

## Setup

### Prerequisites

- Java 17 or higher
- Maven
- Neon Account
- (Optional) Google Calendar API credentials

### Environment Variables

Create a `.env` file in the root directory with the following variables:

```
# Neon Configuration
NEON_DB_URL=your _db_url
NEON_DB_USER=postgres
NEON_DB_PASSWORD=your-database-password

# Google Calendar API Configuration (Optional)
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_REDIRECT_URI=https://studypilot.vercel.app/api/auth/google/callback

# Application Configuration
SPRING_PROFILES_ACTIVE=dev
```

### Neon Setup

1. Create a new project in Neon.
2. Create a storage bucket named "syllabi".
3. Set the bucket permissions to allow file uploads.
4. Get your project URL  from the API settings.
5. Get your database connection details from the Database settings.

### Running the Application

```bash
# Build the application
mvn clean package

# Run the application
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### Syllabus Management

- `POST /api/syllabus/upload` - Upload a new syllabus.
- `GET /api/syllabus` - Get all syllabi (with optional filters).
- `GET /api/syllabus/user/{userId}` - Get syllabi for a specific user.

### Study Plan Management

- `POST /api/study-plan/generate` - Generate a new study plan.
- `GET /api/study-plan/user/{userId}` - Get study plans for a specific user.
- `PUT /api/study-plan/{studyPlanId}/session/{sessionId}` - Update session progress.

### PDF Parsing

- `POST /api/pdf/upload` - Upload and parse a PDF.
- `GET /api/pdf/{id}` - Retrieve a PDF by its ID.

## File Upload Limits

- Maximum file size: 1MB (For Speed)
- Supported file type: PDF only (Common Case).

## Error Handling

The application provides detailed error messages for:

- Invalid PDF files.
- File size limits.
- Storage service errors.
- Missing required fields.
- General errors.

## Contributing

Helpful Contributions are welcome! Please fork the repository and submit a pull request and it will be attended to as soon as possible.