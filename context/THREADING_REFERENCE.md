# Threading Reference

## Active Threads

| Thread | Purpose | Daemon |
|--------|---------|---------|
| JavaFX UI Thread | UI rendering | No |
| Login Thread | Background authentication | Yes |
| Timer Thread | Countdown timer | Yes |
| AutoSave Thread | Save answers periodically | Yes |
| TCP Client Handler | Handle client requests | No |

---

## Thread Safety Rules

### UI Updates
Always use:

```java
Platform.runLater(() -> {
    statusLabel.setText("Updated");
});