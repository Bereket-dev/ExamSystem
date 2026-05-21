# UI Navigation Flow

## Application Flow

LoginView
├── StudentDashboard
│   ├── ExamScreen
│   ├── ResultScreen
│   └── ProfileScreen
│
├── TeacherDashboard
│   ├── CreateExamScreen
│   ├── QuestionManager
│   ├── MonitoringScreen
│   └── ReportsScreen
│
└── AdminDashboard
    ├── UserManagement
    ├── SystemLogs
    └── Analytics

---

## Screen Responsibilities

| Screen | Responsibility |
|--------|----------------|
| LoginView | Authentication |
| StudentDashboard | Student home |
| ExamScreen | Taking exams |
| TeacherDashboard | Teacher operations |
| MonitoringScreen | Live exam tracking |
| ReportsScreen | Analytics |