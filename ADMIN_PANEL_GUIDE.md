# Admin Panel Documentation

## Overview

The Admin Panel is a comprehensive management system that provides administrators with full control over teachers, students, and courses. It allows admins to perform CRUD operations (Create, Read, Update, Delete) on all these entities, as well as manage teacher-course assignments.

## Features

### 1. Teachers Management

#### Add New Teacher
- Navigate to the **Teachers Management** tab
- Click **"Add New Teacher"** button
- Fill in the teacher details:
  - **Username**: Unique identifier (required)
  - **Password**: Authentication password (required for new teachers)
  - **Full Name**: Teacher's full name (required)
  - **Email**: Email address (required, must be valid)
  - **Department**: Department assignment (required)
  - **Qualification**: Educational qualification (e.g., M.Tech, Ph.D)
  - **Experience (Years)**: Years of experience (0-50)
  - **Active**: Toggle to set account status
- Click **Save** to create the account

#### Edit Teacher Information
- Click the **Edit** button next to a teacher in the table
- Modify the teacher details (password is optional for edits)
- Click **Save** to apply changes

#### Remove Teacher
- Click the **Delete** button next to a teacher in the table
- Confirm the deletion when prompted
- Teacher account and profile will be removed

#### Search Teachers
- Enter search query in the **"Search Teacher"** field
- Search by: name, email, or username
- Click **Search** to filter results
- Click **Reset** to view all teachers

### 2. Students Management

#### Add New Student
- Navigate to the **Students Management** tab
- Click **"Add New Student"** button
- Fill in the student details:
  - **Username**: Unique identifier (required)
  - **Password**: Authentication password (required for new students)
  - **Full Name**: Student's full name (required)
  - **Email**: Email address (required, must be valid)
  - **Enrollment Number**: Unique enrollment ID (required)
  - **Department**: Department assignment (required)
  - **Semester**: Current semester (1-8)
  - **Active**: Toggle to set account status
- Click **Save** to create the account

#### Edit Student Information
- Click the **Edit** button next to a student in the table
- Modify the student details (password is optional for edits)
- Click **Save** to apply changes

#### Remove Student
- Click the **Delete** button next to a student in the table
- Confirm the deletion when prompted
- Student account and profile will be removed

#### Search Students
- Enter search query in the **"Search Student"** field
- Search by: name, email, enrollment number, or username
- Click **Search** to filter results
- Click **Reset** to view all students

### 3. Courses Management

#### Create New Course
- Navigate to the **Courses Management** tab
- Click **"Add New Course"** button
- Fill in the course details:
  - **Course Code**: Unique course identifier (e.g., CS101)
  - **Course Name**: Full name of the course (required)
  - **Description**: Course description (required)
  - **Department**: Department offering the course (required)
  - **Credits**: Credit hours (1-10, default 3)
  - **Semester**: Offering semester (1-8)
  - **Active**: Toggle to set course status
- Click **Save** to create the course

#### Edit Course Information
- Click the **Edit** button next to a course in the table
- Modify the course details (code cannot be changed)
- Click **Save** to apply changes

#### Delete Course
- Click the **Delete** button next to a course in the table
- Confirm the deletion when prompted
- Course and all related assignments will be removed

#### Search Courses
- Enter search query in the **"Search Course"** field
- Search by: course code or course name
- Click **Search** to filter results
- Click **Reset** to view all courses

### 4. Teacher-Course Assignments

#### Assign Teacher to Course
1. Navigate to the **"Assign Teachers to Courses"** tab
2. **Step 1**: Select a teacher from the dropdown
   - The system will display courses already assigned to this teacher below
3. **Step 2**: Select a course from the dropdown
4. Click **"Assign Teacher to Course"** button
   - The system will verify that the teacher is not already assigned to this course
   - If successful, the assignment will be created

#### View Teacher Assignments
- Select a teacher from the dropdown in Step 1
- The table below will display all courses assigned to that teacher
- Shows: Course ID, Code, Name, Department, and Assignment Date

#### Remove Teacher from Course
- Select the teacher from the dropdown
- In the courses list below, click the **"Remove"** button next to the course
- Confirm the removal when prompted
- OR: Select both teacher and course, then click **"Remove Assignment"** button

## User Interface Overview

### Main Components

1. **Top Navigation Bar**
   - Admin name display
   - Logout button

2. **Tab Navigation**
   - Teachers Management
   - Students Management
   - Courses Management
   - Assign Teachers to Courses

3. **Status Bar (Bottom)**
   - Total count of teachers
   - Total count of students
   - Total count of courses
   - Current system status

### Search and Filter
- Each tab includes search functionality
- Real-time search across multiple fields
- Reset button to clear search filters

### Data Display
- Tables with relevant columns for each entity type
- Quick action buttons (Edit, Delete) on each row
- Color-coded buttons for different actions

## Input Validation

The Admin Panel includes comprehensive validation:

### Teachers
- Username must be unique
- Password required for new teachers
- Email must be in valid format
- All required fields must be filled

### Students
- Username must be unique
- Password required for new students
- Email must be in valid format
- Enrollment number must be unique
- All required fields must be filled

### Courses
- Course code must be unique
- Course code cannot be changed after creation
- All required fields must be filled

### Assignments
- Cannot assign same teacher to a course twice
- Both teacher and course must be selected
- Teacher and course must exist in the system

## Database Tables

### courses
```sql
- course_id: Primary key
- course_code: Unique course identifier
- course_name: Full name of the course
- description: Course description
- department: Department offering the course
- credits: Number of credit hours
- semester: Semester when offered
- is_active: Boolean flag for active status
- created_at: Timestamp of creation
- updated_at: Timestamp of last update
```

### teacher_courses
```sql
- teacher_course_id: Primary key
- teacher_id: Reference to teacher
- course_id: Reference to course
- assigned_date: When assignment was made
- removed_date: When assignment was removed (if applicable)
- is_active: Boolean flag for assignment status
- created_at: Timestamp of creation
- updated_at: Timestamp of last update
```

## Workflow Examples

### Example 1: Setting up a new course with teachers

1. Create a new course in **Courses Management** tab
   - Enter: CS201, Data Structures, CS department, 4 credits, Semester 2
   
2. Go to **Assign Teachers to Courses** tab
   - Select Teacher 1 and assign them to CS201
   - Select Teacher 2 and assign them to CS201
   
3. The course is now set up with two teachers

### Example 2: Managing a teacher's profile

1. Search for teacher in **Teachers Management** tab
2. Click **Edit** to modify their information
   - Update department, qualifications, or experience
3. Save changes
4. Go to **Assign Teachers to Courses** to manage their course assignments

### Example 3: Removing a course

1. Search for the course in **Courses Management** tab
2. Click **Delete**
3. Confirm deletion
   - All teacher assignments are automatically removed
   - The course is deleted from the system

## Error Handling

The Admin Panel provides clear error messages for:
- Duplicate usernames or emails
- Duplicate course codes
- Invalid email formats
- Missing required fields
- Database connection errors
- File loading errors

## Best Practices

1. **Regular Backups**: Ensure database backups before bulk deletions
2. **Username Conventions**: Use consistent naming conventions (e.g., t001 for teachers, s001 for students)
3. **Email Verification**: Use institutional email addresses
4. **Course Planning**: Plan course codes before creation (e.g., CS-100 series for first year)
5. **Teacher Assignment**: Assign appropriate teachers to courses based on qualifications
6. **Status Management**: Regularly review and update active/inactive status

## Troubleshooting

### Cannot add teacher/student
- Check if username already exists
- Verify email format is valid
- Ensure all required fields are filled

### Cannot assign teacher to course
- Verify teacher exists in the system
- Verify course exists and is active
- Check if assignment already exists

### Search not working
- Ensure search field is not empty for partial match
- Try resetting filters and searching again

### Changes not saving
- Check database connection
- Verify all required fields have values
- Look for validation error messages

## Access Control

The Admin Panel is only accessible to users with the **ADMIN** role. When an admin logs in:
1. They are directed to the Admin Panel instead of their dashboard
2. They have full access to all management features
3. They can modify any user account or course
4. All actions are logged (if audit logging is enabled)

## Next Steps

After setting up your admin panel:
1. Create courses for your institution
2. Register all teachers and students
3. Assign teachers to courses
4. Review the system's functionality
5. Set up regular admin workflows for ongoing management

---

**Last Updated**: Phase 9 - Admin Panel Implementation
**Version**: 1.0.0
