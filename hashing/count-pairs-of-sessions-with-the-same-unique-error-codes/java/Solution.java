import java.util.*;

/*
Title: Count Pairs of Sessions With the Same Unique Error Codes

Problem Description:
A monitoring system records application sessions, where each session contains a list of error codes
that occurred during that session. The same error code may appear multiple times inside one session
if the issue was triggered repeatedly. Two sessions are considered equivalent if the set of distinct
error codes seen in the two sessions is exactly the same, regardless of the order of codes and
regardless of how many times each code repeats.

Given an array sessions, where sessions[i] is a non-empty array of integers representing the error
codes seen in the i-th session, return the number of pairs of indices (i, j) such that i < j and
sessions[i] and sessions[j] are equivalent.

For example, the sessions [4, 7, 4, 9] and [9, 7, 4] are equivalent because both contain the unique
code set {4, 7, 9}. However, [4, 7] and [4, 7, 8] are not equivalent.

Constraints:
- 1 <= sessions.length <= 100000
- 1 <= sessions[i].length <= 100
- 0 <= sessions[i][j] <= 1000000000
- The total number of error codes across all sessions does not exceed 300000

Example 1:
Input: sessions = [[4,7,4,9],[9,4,7],[1,2,2],[2,1],[5]]
Output: 2

Example 2:
Input: sessions = [[8,8,8],[8],[1,3,1,3],[3,1],[2,2,4],[4,2,4],[2,4,5]]
Output: 4
*/

public class Solution {

    /**
     * Counts how many pairs of sessions have exactly the same set of distinct error codes.
     *
     * Strategy:
     * 1. For each session, create a canonical representation of its distinct values.
     * 2. The canonical representation is built by:
     *    - sorting the session
     *    - removing duplicates
     *    - converting the distinct sorted values into a string key
     * 3. Use a hash map to count how many times each canonical key has appeared before.
     * 4. If the current key has already appeared k times, then the current session forms
     *    exactly k new equivalent pairs with those previous sessions.
     *
     * This works because two sessions are equivalent if and only if their sorted distinct
     * values are identical.
     *
     * @param sessions a 2D array where sessions[i] contains the error codes of the i-th session
     * @return the number of pairs (i, j) with i < j such that sessions[i] and sessions[j]
     *         have the same set of distinct error codes
     * Time complexity: O(T * log M) in total, where T is the total number of error codes across
     * all sessions and M is the maximum length of a single session. More precisely, each session
     * of length k costs O(k log k) for sorting.
     * Space complexity: O(T) in the worst case for storing canonical keys in the hash map.
     */
    public long countEquivalentSessionPairs(int[][] sessions) {
        // This map stores:
        // key   -> canonical representation of the distinct error-code set
        // value -> how many previous sessions had exactly this same key
        Map<String, Integer> frequencyMap = new HashMap<>();

        // We use long because the number of pairs can be large.
        // For example, if all 100000 sessions are equivalent, the number of pairs is:
        // 100000 * 99999 / 2 = 4,999,950,000, which does not fit in int.
        long pairs = 0L;

        // Process each session one by one.
        for (int[] session : sessions) {
            // Convert the current session into a canonical key that represents
            // only its distinct values, ignoring order and duplicates.
            String key = buildCanonicalKey(session);

            // Find how many previous sessions had the same key.
            int seenSoFar = frequencyMap.getOrDefault(key, 0);

            // If this key has appeared 'seenSoFar' times before, then the current session
            // forms exactly 'seenSoFar' new valid pairs with those previous sessions.
            pairs += seenSoFar;

            // Record that we have now seen this key one more time.
            frequencyMap.put(key, seenSoFar + 1);
        }

        return pairs;
    }

    /**
     * Builds a canonical representation for one session.
     *
     * The canonical representation must satisfy:
     * - sessions with the same distinct values produce the same key
     * - sessions with different distinct values produce different keys
     *
     * We achieve this by:
     * 1. Copying the session array so we do not modify the caller's input
     * 2. Sorting the copy
     * 3. Scanning the sorted array and keeping only distinct values
     * 4. Appending those distinct values into a string with separators
     *
     * Example:
     * session = [4, 7, 4, 9]
     * sorted  = [4, 4, 7, 9]
     * distinct values = [4, 7, 9]
     * key = "4#7#9#"
     *
     * Example:
     * session = [9, 4, 7]
     * sorted  = [4, 7, 9]
     * distinct values = [4, 7, 9]
     * key = "4#7#9#"
     *
     * Since both produce the same key, they are considered equivalent.
     *
     * @param session the array of error codes for one session
     * @return a canonical string key representing the set of distinct error codes
     * Time complexity: O(k log k), where k is the length of the session
     * Space complexity: O(k) for the copied array and the generated key
     */
    public String buildCanonicalKey(int[] session) {
        // Make a copy so that sorting does not mutate the original input.
        int[] copy = Arrays.copyOf(session, session.length);

        // Sort so equal values become adjacent, and the final representation
        // becomes independent of the original order.
        Arrays.sort(copy);

        // StringBuilder is efficient for repeated appends.
        StringBuilder keyBuilder = new StringBuilder();

        // Walk through the sorted array and append only the first occurrence
        // of each distinct value.
        for (int i = 0; i < copy.length; i++) {
            // If this is the first element, it is definitely distinct.
            // Otherwise, it is distinct only if it differs from the previous element.
            if (i == 0 || copy[i] != copy[i - 1]) {
                // Append the value followed by a separator.
                // The separator prevents ambiguity.
                // For example, without separators:
                // [1, 23] and [12, 3] would both look like "123"
                // With separators:
                // "1#23#" vs "12#3#" are clearly different.
                keyBuilder.append(copy[i]).append('#');
            }
        }

        return keyBuilder.toString();
    }

    /**
     * Utility method to print a 2D int array in a readable format.
     *
     * @param sessions the 2D array to print
     * @return a string representation of the sessions array
     * Time complexity: O(T), where T is the total number of integers printed
     * Space complexity: O(T) for the resulting string
     */
    public String sessionsToString(int[][] sessions) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');

        for (int i = 0; i < sessions.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(Arrays.toString(sessions[i]));
        }

        sb.append(']');
        return sb.toString();
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * Verified expected outputs:
     * Example 1 -> 2
     * Example 2 -> 4
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo setup, excluding the called algorithm
     * Space complexity: O(1) excluding the called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] sessions1 = {
            {4, 7, 4, 9},
            {9, 4, 7},
            {1, 2, 2},
            {2, 1},
            {5}
        };

        int[][] sessions2 = {
            {8, 8, 8},
            {8},
            {1, 3, 1, 3},
            {3, 1},
            {2, 2, 4},
            {4, 2, 4},
            {2, 4, 5}
        };

        long result1 = solution.countEquivalentSessionPairs(sessions1);
        long result2 = solution.countEquivalentSessionPairs(sessions2);

        System.out.println("Example 1 Input: " + solution.sessionsToString(sessions1));
        System.out.println("Example 1 Output: " + result1);
        System.out.println("Expected: 2");
        System.out.println();

        System.out.println("Example 2 Input: " + solution.sessionsToString(sessions2));
        System.out.println("Example 2 Output: " + result2);
        System.out.println("Expected: 4");
        System.out.println();

        // Additional quick sanity checks for beginner-friendly demonstration.
        int[][] sessions3 = {
            {4, 7},
            {4, 7, 8}
        };
        System.out.println("Sanity Check Input: " + solution.sessionsToString(sessions3));
        System.out.println("Sanity Check Output: " + solution.countEquivalentSessionPairs(sessions3));
        System.out.println("Expected: 0");
        System.out.println();

        int[][] sessions4 = {
            {1, 1, 1},
            {1},
            {1, 1},
            {2}
        };
        System.out.println("Sanity Check Input: " + solution.sessionsToString(sessions4));
        System.out.println("Sanity Check Output: " + solution.countEquivalentSessionPairs(sessions4));
        System.out.println("Expected: 3");
    }
}