# 10 Quality Requirements

## Quality Tree
- Reliability
- Usability
- Performance
- Maintainability
- Testability

## Quality Scenarios
### Reliability
- **Scenario:** Metadata is unavailable for a subset of files.
- **Expected:** App falls back to file timestamp and continues processing.

### Usability
- **Scenario:** First-time user starts app without prior setup.
- **Expected:** User can complete one sorting run with source/target selection and mode choice in a short guided flow.

### Performance
- **Scenario:** Sorting a large camera folder.
- **Expected:** Continuous progress updates and no app freeze; measured with macrobenchmarks.

### Maintainability
- **Scenario:** Add a new folder schema later.
- **Expected:** Introduce new schema strategy without changing core sorting orchestration.

### Testability
- **Scenario:** Validate fallback and conflict rules.
- **Expected:** Domain tests run in JVM CI without device dependencies.

