import java.util.*;

/*
 * Title: Count Users With Matching First and Last Action Sets
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * You are given an array logs where each element is a record [userId, action].
 * The records are listed in chronological order. For each user, consider the set
 * of distinct actions performed in that user's first contiguous session and the set
 * of distinct actions performed in that user's last contiguous session.
 *
 * A contiguous session for a user is a maximal consecutive block of records in the
 * global log belonging to that same user. In other words, if the same user appears
 * in several adjacent records, those records form one session, and the session ends
 * when a different user appears.
 *
 * Your task is to count how many users have exactly the same set of distinct actions
 * in their first session and in their last session. If a user appears in only one
 * session, that user should also be counted, because the first and last session are
 * the same session.
 *
 * Important details:
 * - Action order inside a session does not matter.
 * - Repeated occurrences of the same action within a session count only once.
 * - User IDs and actions are case-sensitive strings.
 *
 * Constraints:
 * - 1 <= logs.length <= 2 * 10^5
 * - logs[i].length == 2
 * - 1 <= userId.length, action.length <= 20
 * - logs[i][0] and logs[i][1] consist of English letters, digits, or underscores
 * - The number of distinct users is at most 2 * 10^5
 *
 * Example 1:
 * Input:
 * logs = [
 *   ["u1","open"],
 *   ["u1","click"],
 *   ["u2","open"],
 *   ["u1","click"],
 *   ["u1","open"],
 *   ["u2","open"]
 * ]
 * Output: 2
 *
 * Explanation:
 * - u1 has first session actions {open, click} and last session actions {click, open},
 *   which are equal.
 * - u2 has first session {open} and last session {open}. They are equal.
 * So the answer is 2.
 *
 * Example 2:
 * Input:
 * logs = [
 *   ["a","x"],
 *   ["a","x"],
 *   ["b","y"],
 *   ["a","z"],
 *   ["a","z"],
 *   ["c","m"],
 *   ["c","n"]
 * ]
 * Correct Output: 2
 *
 * Explanation:
 * - a has first session {x} and last session {z}, not equal.
 * - b appears in only one session with set {y}, so it counts.
 * - c appears in only one session with set {m, n}, so it counts.
 * Therefore the total is 2.
 */

public class Solution {

    /**
     * Small helper structure that stores, for one user:
     * 1) the set of distinct actions from the first session
     * 2) the set of distinct actions from the last session seen so far
     *
     * If a user appears in only one session, both references will point to equal sets
     * after processing is complete.
     */
    private static class UserSessions {
        Set<String> firstSessionActions;
        Set<String> lastSessionActions;

        UserSessions(Set<String> firstSessionActions, Set<String> lastSessionActions) {
            this.firstSessionActions = firstSessionActions;
            this.lastSessionActions = lastSessionActions;
        }
    }

    /**
     * Counts how many users have exactly the same set of distinct actions in their
     * first contiguous session and their last contiguous session.
     *
     * The key idea is:
     * - First, scan the log and split it into sessions.
     * - Each session is a maximal consecutive block of records belonging to the same user.
     * - For each session, build the set of distinct actions inside that session.
     * - For each user:
     *   - If this is the first session we have seen for that user, store it as both
     *     the first and last session.
     *   - Otherwise, update only the last session.
     * - Finally, compare first-session set and last-session set for every user.
     *
     * This works in linear time relative to the number of log records, aside from the
     * hashing cost of set operations, which is expected O(1) per insertion.
     *
     * @param logs the chronological log records, where each element is [userId, action]
     * @return the number of users whose first-session action set equals their last-session action set
     * Time complexity: O(n) expected, where n is the number of log records
     * Space complexity: O(u + a) expected overall, where u is the number of users and
     * a is the total number of distinct action entries stored across first/last sessions
     */
    public int countUsersWithMatchingFirstAndLastActionSets(String[][] logs) {
        // Map from userId -> stored information about that user's first and last sessions.
        Map<String, UserSessions> userToSessions = new HashMap<>();

        // We scan the array once and explicitly identify each contiguous session.
        int index = 0;

        while (index < logs.length) {
            // The current record starts a new session.
            String currentUser = logs[index][0];

            // This set will collect all DISTINCT actions performed in this one session.
            // Because it is a set, duplicates inside the same session are automatically ignored.
            Set<String> sessionActions = new HashSet<>();

            // Consume the entire maximal consecutive block for currentUser.
            // This is exactly the definition of one contiguous session.
            while (index < logs.length && logs[index][0].equals(currentUser)) {
                sessionActions.add(logs[index][1]);
                index++;
            }

            // Now we have fully built the action set for one complete session of currentUser.
            UserSessions existing = userToSessions.get(currentUser);

            if (existing == null) {
                // This is the FIRST session ever seen for this user.
                // Therefore:
                // - first session = this session
                // - last session  = this session (for now)
                userToSessions.put(currentUser, new UserSessions(sessionActions, sessionActions));
            } else {
                // We have seen this user before, so this session becomes the new LAST session.
                // The first session remains unchanged.
                existing.lastSessionActions = sessionActions;
            }
        }

        // Count users whose first and last session action sets are exactly equal.
        int answer = 0;

        for (UserSessions sessions : userToSessions.values()) {
            if (sessions.firstSessionActions.equals(sessions.lastSessionActions)) {
                answer++;
            }
        }

        return answer;
    }

    /**
     * Convenience wrapper that accepts logs as a List of String arrays.
     * This can be useful in interview-style testing or custom callers.
     *
     * @param logs the chronological log records as a list, where each element is [userId, action]
     * @return the number of users whose first-session action set equals their last-session action set
     * Time complexity: O(n) expected, where n is the number of log records
     * Space complexity: O(u + a) expected overall
     */
    public int countUsersWithMatchingFirstAndLastActionSets(List<String[]> logs) {
        String[][] array = new String[logs.size()][2];
        for (int i = 0; i < logs.size(); i++) {
            array[i] = logs.get(i);
        }
        return countUsersWithMatchingFirstAndLastActionSets(array);
    }

    /**
     * Builds a readable string representation of the logs for demonstration output.
     *
     * @param logs the log records
     * @return a human-readable string version of the input
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String logsToString(String[][] logs) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < logs.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(Arrays.toString(logs[i]));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Demonstrates the solution on sample inputs and prints the results.
     *
     * Important note:
     * The second example text in the prompt contains a mismatch:
     * it says "Output: 1" but the explanation clearly counts both b and c,
     * so the correct output is 2. This main method prints the correct result.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) across the demonstrated examples
     * Space complexity: O(n) for the example data
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[][] logs1 = {
            {"u1", "open"},
            {"u1", "click"},
            {"u2", "open"},
            {"u1", "click"},
            {"u1", "open"},
            {"u2", "open"}
        };

        String[][] logs2 = {
            {"a", "x"},
            {"a", "x"},
            {"b", "y"},
            {"a", "z"},
            {"a", "z"},
            {"c", "m"},
            {"c", "n"}
        };

        System.out.println("Example 1 logs: " + solution.logsToString(logs1));
        System.out.println("Example 1 result: " + solution.countUsersWithMatchingFirstAndLastActionSets(logs1));
        System.out.println("Expected: 2");
        System.out.println();

        System.out.println("Example 2 logs: " + solution.logsToString(logs2));
        System.out.println("Example 2 result: " + solution.countUsersWithMatchingFirstAndLastActionSets(logs2));
        System.out.println("Expected: 2");
        System.out.println();

        String[][] extraLogs = {
            {"alice", "login"},
            {"alice", "view"},
            {"bob", "login"},
            {"bob", "login"},
            {"alice", "view"},
            {"alice", "login"},
            {"charlie", "pay"},
            {"alice", "login"}
        };

        System.out.println("Extra example logs: " + solution.logsToString(extraLogs));
        System.out.println("Extra example result: " + solution.countUsersWithMatchingFirstAndLastActionSets(extraLogs));
    }
}