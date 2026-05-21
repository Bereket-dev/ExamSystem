# Phase 1 Project Setup Guide

## What was created in Phase 1:

### 1. Maven Project Structure
- `pom.xml` - Complete Maven configuration with all dependencies:
  - JavaFX 21.0.2
  - MySQL Connector 8.2.0
  - HikariCP 5.1.0 (connection pooling)
  - SLF4J for logging
  - JUnit and TestFX for testing
  - Gson for JSON processing

### 2. Java Package Structure
```
src/
├── main/
│   ├── java/com/examsystem/
│   │   ├── controller/      (UI Controllers - Phase 3 onwards)
│   │   ├── model/           (Data Models)
│   │   ├── service/         (Business Logic - Phase 2 onwards)
│   │   ├── db/              (Database Operations)
│   │   ├── network/         (Networking - Phase 6 onwards)
│   │   ├── util/            (Utility Classes)
│   │   └── App.java         (Main Entry Point)
│   └── resources/
│       ├── com/examsystem/fxml/  (FXML UI Files)
│       ├── css/                  (Stylesheets)
│       └── sql/                  (Database scripts)
└── test/
    └── java/com/examsystem/     (Unit Tests)
```

### 3. Core Java Classes Created

#### App.java
- Main JavaFX application entry point
- Basic UI setup for application launch

#### DatabaseConnection.java
- HikariCP connection pool management
- MySQL database configuration
- Connection testing utility
- Thread-safe connection retrieval

#### User.java
- User model class with roles (Student, Teacher, Admin)
- Properties: userId, username, email, fullName, role, isActive

#### ConfigManager.java
- Configuration property loader
- Type-safe property access (String, Integer, Boolean)
- Application-wide settings management

### 4. Database Setup Files

#### schema.sql
Complete MySQL database schema including:
- Users table (with roles: student, teacher, admin)
- Students table (with enrollment details)
- Teachers table (with department info)
- Exams table (with exam details)
- Questions table (with question types)
- Options table (for MCQ answers)
- Student exam assignments
- Exam attempts tracking
- Student answers recording
- Audit log table
- Proper indexes for performance

#### test_data.sql
Sample test data including:
- 6 test users (admin, 2 teachers, 3 students)
- Teacher profiles
- Student profiles
- Sample exams and questions
- Student exam assignments

### 5. Configuration Files

#### application.properties
- Database connection parameters
- JavaFX window settings
- Network configuration placeholders
- Session management settings
- Logging configuration

### 6. Version Control
#### .gitignore
Configured to ignore:
- Maven build artifacts
- IDE files (.idea/, .vscode/)
- Java compiled files
- Log files
- Database files

## Next Steps:

### Phase 2 - Database Layer
- Create UserRepository
- Create ExamRepository
- Create QuestionRepository
- Implement CRUD operations
- Test database connectivity

### Phase 3 - Authentication System
- Create login UI (FXML)
- Implement authentication logic
- Session management
- Role-based navigation

### Phase 4 - Student Module
- Student dashboard
- Exam listing
- Exam interface with timer
- Auto-save functionality

## Setup Instructions:

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL 8.0 or higher

### Installation

1. **Build the project:**
   ```bash
   mvn clean install
   ```

2. **Set up MySQL Database:**
   ```bash
   # Login to MySQL
   mysql -u root -p
   
   # Run schema
   source src/main/resources/sql/schema.sql
   
   # Run test data
   source src/main/resources/sql/test_data.sql
   ```

3. **Configure Database Connection:**
   - Edit `src/main/java/com/examsystem/db/DatabaseConnection.java`
   - Update DB_USER, DB_PASSWORD, and other connection parameters

4. **Run the Application:**
   ```bash
   mvn javafx:run
   ```

## Project Configuration

### Database Configuration
Located in: `DatabaseConnection.java`
```
DB_HOST = localhost
DB_PORT = 3306
DB_NAME = exam_system
DB_USER = root
DB_PASSWORD = (configure as needed)
MAX_POOL_SIZE = 10
MIN_IDLE = 5
```

### JavaFX Configuration
Located in: `application.properties`
```
app.window.width=1024
app.window.height=768
app.title=ExamSystem
```

## Build Commands

```bash
# Clean and install
mvn clean install

# Run application
mvn javafx:run

# Run tests
mvn test

# Create executable JAR
mvn package shade:shade
```

## Architecture Notes

- **Connection Pooling:** HikariCP for efficient database connection management
- **Logging:** SLF4J with Simple implementation
- **JavaFX:** MVVM pattern to be implemented in Phase 3
- **Database:** MySQL with proper normalization
- **Threading:** Will be addressed in Phase 8

## Checklist - Phase 1 Complete ✓

- [x] Maven project initialized
- [x] JavaFX configured
- [x] MySQL configuration ready
- [x] Package structure created
- [x] Database schema created
- [x] Test data prepared
- [x] Git repository configured
- [x] Logging setup
- [x] Configuration management
- [x] HikariCP connection pooling
