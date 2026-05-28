package com.examsystem.sync;

import com.examsystem.rmi.remote.SyncBundle;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Detects simple row-level conflicts between local and server snapshots before merge.
 */
public class ConflictDetector {

    private static final String[] WATCH_TABLES = { "exams", "student_exam_assignments", "exam_attempts",
            "student_answers" };

    public List<SyncConflict> detect(SyncBundle local, SyncBundle remote) {
        List<SyncConflict> conflicts = new ArrayList<>();
        if (local == null || remote == null) {
            return conflicts;
        }
        for (String table : WATCH_TABLES) {
            List<Map<String, String>> localRows = local.getTable(table);
            List<Map<String, String>> remoteRows = remote.getTable(table);
            if (localRows.isEmpty() || remoteRows.isEmpty()) {
                continue;
            }
            String pk = primaryKeyFor(table);
            for (Map<String, String> localRow : localRows) {
                String id = localRow.get(pk);
                if (id == null) {
                    continue;
                }
                Map<String, String> remoteRow = findRow(remoteRows, pk, id);
                if (remoteRow == null) {
                    continue;
                }
                if (!rowSignature(localRow).equals(rowSignature(remoteRow))) {
                    conflicts.add(new SyncConflict(
                            table,
                            id,
                            summarize(localRow),
                            summarize(remoteRow),
                            LocalDateTime.now(),
                            LocalDateTime.now()));
                }
            }
        }
        return conflicts;
    }

    private Map<String, String> findRow(List<Map<String, String>> rows, String pk, String id) {
        for (Map<String, String> row : rows) {
            if (id.equals(row.get(pk))) {
                return row;
            }
        }
        return null;
    }

    private String primaryKeyFor(String table) {
        return switch (table) {
            case "exams" -> "exam_id";
            case "student_exam_assignments" -> "assignment_id";
            case "exam_attempts" -> "attempt_id";
            case "student_answers" -> "answer_id";
            default -> "id";
        };
    }

    private String rowSignature(Map<String, String> row) {
        return row.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + Objects.toString(e.getValue(), ""))
                .reduce((a, b) -> a + "|" + b)
                .orElse("");
    }

    private String summarize(Map<String, String> row) {
        return row.entrySet().stream()
                .limit(4)
                .map(e -> e.getKey() + ": " + e.getValue())
                .reduce((a, b) -> a + ", " + b)
                .orElse("(empty)");
    }
}
