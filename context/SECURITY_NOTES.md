# Security Notes

## Password Handling
- Never store plain-text passwords
- Use BCrypt hashing

## SQL Injection Prevention
Always use PreparedStatement

```java
PreparedStatement ps =
    conn.prepareStatement(
        "SELECT * FROM users WHERE email=?"
    );