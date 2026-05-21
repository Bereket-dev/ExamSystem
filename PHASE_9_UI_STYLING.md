# Phase 9 — UI Styling Complete

Per `context/PROJECT_ROADMAP.md` and `context/TESTING_GUIDE.md` (GUI: form validation, layout responsiveness).

## Implemented

### 1. CSS theme (`src/main/resources/css/examsystem.css`)

- Dark login gradient (`login-root`, `login-card`)
- App headers, panels, buttons (`btn-primary`, `btn-secondary`, etc.)
- Form controls, focus states, error/success status classes
- List selection, exam timer, results display styles

### 2. Responsive layouts

- All screens: `minWidth="720"` `minHeight="520"` on `dashboard-root`
- Login: `minWidth="640"` `minHeight="480"`
- `SplitPane`, `VBox.vgrow`, `HBox.hgrow` for resizable content
- Stage minimum sizes via `UiManager.configureStage()`

### 3. Animations (`UiManager`)

- Fade-in on scene navigation (`animateIn`)
- Shake on login validation errors (`shake`)
- Button hover scale in CSS

### 4. Form validation (`FormValidator`)

| Screen | Validated fields |
|--------|------------------|
| Login | Username, password required |
| Create Exam | Name, subject, numeric fields, date/time format, passing ≤ total |
| Question Manager | Question text, marks, MCQ options, T/F correct option |
| Assign Students | Exam combo, student list selection |

Invalid fields get `field-error` style; status labels use `status-error` / `status-success`.

## Utilities

- `com.examsystem.util.UiManager` — theme, scenes, navigation animations
- `com.examsystem.util.FormValidator` — shared validation

## Run

```bash
mvn clean compile exec:java
```
