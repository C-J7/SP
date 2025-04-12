# StudyPilot Backend

This is the backend service for the StudyPilot application, built with Spring Boot.

## Setup

### Prerequisites

- Java 17 or higher
- Maven
- Supabase account
- (Optional) Google Calendar API credentials

### Environment Variables

Create a `.env` file in the root directory with the following variables:

```
# Supabase Configuration
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_KEY=your-supabase-anon-key
SUPABASE_DB_URL=jdbc:postgresql://db.your-project-id.supabase.co:5432/postgres
SUPABASE_DB_USER=postgres
SUPABASE_DB_PASSWORD=your-database-password

# Google Calendar API Configuration (Optional)
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_REDIRECT_URI=http://localhost:8080/api/auth/google/callback

# Application Configuration
SPRING_PROFILES_ACTIVE=dev
```

### Supabase Setup

1. Create a new project in Supabase
2. Create a storage bucket named "syllabi"
3. Set the bucket permissions to allow file uploads
4. Get your project URL and anon key from the API settings
5. Get your database connection details from the Database settings

### Running the Application

```bash
# Build the application
mvn clean package

# Run the application
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### Syllabus Management

- `POST /api/syllabus/upload` - Upload a new syllabus
- `GET /api/syllabus` - Get all syllabi (with optional filters)
- `GET /api/syllabus/user/{userId}` - Get syllabi for a specific user

### Study Plan Management

- `POST /api/study-plan/generate` - Generate a new study plan
- `GET /api/study-plan/user/{userId}` - Get study plans for a specific user
- `PUT /api/study-plan/{studyPlanId}/session/{sessionId}` - Update session progress

## File Upload Limits

- Maximum file size: 1MB
- Supported file type: PDF only

## Error Handling

The application provides detailed error messages for:
- Invalid PDF files
- File size limits
- Storage service errors
- Missing required fields
- General errors 