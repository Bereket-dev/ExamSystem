# Known Issues

| Issue | Status | Solution |
|------|--------|----------|
| RMI timeout on slow LAN | Fixed | Retry logic in `RMIClient` |
| Auto-save race condition | Fixed | `synchronized` save lock in `ExamScreenController` |
| JavaFX freeze during DB load | Fixed | `BackgroundLoader` + `ThreadPoolManager` |
| Socket disconnect handling | Fixed | Reconnection in `ExamClient` |

---

# Future Improvements

- SSL socket encryption
- Web-based version
- Webcam monitoring
- AI cheating detection
- Redis caching
- Docker deployment
