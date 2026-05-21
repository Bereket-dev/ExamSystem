# Core Classes Reference

## Main Classes

| Class | Responsibility |
|------|----------------|
| Main | Launch JavaFX application |
| DatabaseConnection | Handle JDBC connections |
| LoginController | Authentication logic |
| StudentDashboard | Student interface |
| TeacherDashboard | Teacher interface |
| ExamController | Exam operations |
| ResultController | Result calculations |
| ExamServer | TCP server |
| ExamClient | TCP client |
| RMIServer | RMI registry/server |
| RMIClient | RMI client |
| AutoSaveThread | Background save system |

---

# Relationships

User
├── Student
└── Teacher

Exam
├── Question
│   └── QuestionOption
└── ExamAttempt
    ├── Answer
    └── Result