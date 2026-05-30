/*
 * Generate All Valid PIN Patterns
 * ================================
 * Difficulty: Easy
 * Topic: Recursion and Backtracking
 *
 * Problem Description:
 * A PIN pattern is a sequence of digits from 1 to 9 where each digit is used at most once.
 * A PIN pattern is considered valid if its length is between minLen and maxLen (inclusive),
 * and each consecutive pair of digits in the sequence satisfies the following rule:
 * if the two digits are not adjacent on a 3x3 grid (horizontally, vertically, or diagonally),
 * then all digits that lie on the straight line between them must have already been used
 * in the pattern before that move.
 *
 * The 3x3 grid is laid out as follows:
 *   1 2 3
 *   4 5 6
 *   7 8 9
 *
 * For example:
 *   - Moving from 1 to 3 requires that 2 has already been visited.
 *   - Moving from 1 to 9 requires that 5 has already been visited.
 *   - Moving from 1 to 7 requires that 4 has already been visited.
 *
 * Given two integers minLen and maxLen, return the total number of valid PIN patterns.
 *
 * Constraints:
 *   1 <= minLen <= maxLen <= 9
 *
 * Example 1:
 *   Input: minLen = 1, maxLen = 1
 *   Output: 9
 *
 * Example 2:
 *   Input: minLen = 1, maxLen = 2
 *   Output: 65
 */

import java.util.*;

/**
 * Solution class for generating and counting all valid PIN patterns on a 3x3 grid.
 *
 * <p>The approach uses backtracking to explore all possible sequences of digits (1-9),
 * checking at each step whether a move from the current digit to the next digit is valid
 * according to the "skip" rule (if a digit lies between two non-adjacent digits on the grid,
 * it must already be visited).</p>
 */
public class Solution {

    /**
     * A lookup table that stores the "skip" digit between two digits on the 3x3 grid.
     *
     * <p>skip[i][j] = k means that to move from digit i to digit j directly,
     * digit k must have already been visited (because k lies on the straight line between i and j).
     * If skip[i][j] = 0, no intermediate digit is required (they are adjacent or diagonal neighbors).</p>
     *
     * <p>Digits are 1-indexed, so we use a 10x10 array for convenience.</p>
     */
    private int[][] skip;

    /**
     * Constructs a Solution object and initializes the skip table.
     *
     * <p>The skip table is pre-computed once and reused for all backtracking calls.</p>
     */
    public Solution() {
        // Initialize a 10x10 skip table (indices 0-9, but we only use 1-9)
        skip = new int[10][10];

        // Fill in all pairs that require an intermediate "skip" digit.
        // These are pairs of digits that are NOT adjacent/diagonal on the grid
        // but lie on the same row, column, or diagonal.

        // Same row skips:
        // 1 -> 3 and 3 -> 1 require 2 to be visited
        skip[1][3] = 2;
        skip[3][1] = 2;

        // 4 -> 6 and 6 -> 4 require 5 to be visited
        skip[4][6] = 5;
        skip[6][4] = 5;

        // 7 -> 9 and 9 -> 7 require 8 to be visited
        skip[7][9] = 8;
        skip[9][7] = 8;

        // Same column skips:
        // 1 -> 7 and 7 -> 1 require 4 to be visited
        skip[1][7] = 4;
        skip[7][1] = 4;

        // 2 -> 8 and 8 -> 2 require 5 to be visited
        skip[2][8] = 5;
        skip[8][2] = 5;

        // 3 -> 9 and 9 -> 3 require 6 to be visited
        skip[3][9] = 6;
        skip[9][3] = 6;

        // Diagonal skips:
        // 1 -> 9 and 9 -> 1 require 5 to be visited (main diagonal)
        skip[1][9] = 5;
        skip[9][1] = 5;

        // 3 -> 7 and 7 -> 3 require 5 to be visited (anti-diagonal)
        skip[3][7] = 5;
        skip[7][3] = 5;

        // All other pairs have skip[i][j] = 0 (no intermediate digit required),
        // which is the default value for int arrays in Java.
    }

    /**
     * Counts the total number of valid PIN patterns with lengths between minLen and maxLen.
     *
     * <p>Strategy:
     * <ol>
     *   <li>Use a boolean array to track which digits have been visited.</li>
     *   <li>For each starting digit (1-9), launch a backtracking search.</li>
     *   <li>At each step, try to extend the current pattern by one more digit.</li>
     *   <li>A move from current digit to next digit is valid if:
     *       - The next digit hasn't been visited yet, AND
     *       - Either there's no skip digit required, OR the skip digit has already been visited.</li>
     *   <li>Count the pattern whenever its length falls within [minLen, maxLen].</li>
     * </ol>
     * </p>
     *
     * @param minLen the minimum length of a valid PIN pattern (1 <= minLen <= 9)
     * @param maxLen the maximum length of a valid PIN pattern (minLen <= maxLen <= 9)
     * @return the total number of valid PIN patterns with lengths in [minLen, maxLen]
     *
     * @implNote Time Complexity: O(9!) in the worst case (all permutations of 9 digits),
     *           but pruned significantly by the skip rule.
     *           Space Complexity: O(9) for the recursion stack depth and visited array.
     */
    public int numberOfPatterns(int minLen, int maxLen) {
        // Track which digits (1-9) have been visited in the current pattern
        boolean[] visited = new boolean[10]; // index 0 unused; indices 1-9 for digits 1-9

        // This will accumulate the total count of valid patterns
        int totalCount = 0;

        // Try starting the pattern from each digit 1 through 9
        for (int startDigit = 1; startDigit <= 9; startDigit++) {
            // Mark the starting digit as visited
            visited[startDigit] = true;

            // Launch backtracking from this starting digit with current length = 1
            totalCount += backtrack(startDigit, 1, minLen, maxLen, visited);

            // Unmark the starting digit so it can be used as a start in the next iteration
            visited[startDigit] = false;
        }

        return totalCount;
    }

    /**
     * Recursive backtracking helper that counts valid patterns starting from a given digit.
     *
     * <p>At each recursive call, we:
     * <ol>
     *   <li>Check if the current length is within [minLen, maxLen]; if so, count this pattern.</li>
     *   <li>If the current length equals maxLen, stop extending (no need to go deeper).</li>
     *   <li>Otherwise, try adding each unvisited digit as the next step, checking validity.</li>
     * </ol>
     * </p>
     *
     * @param current   the digit we are currently at in the pattern (1-9)
     * @param length    the current length of the pattern being built
     * @param minLen    the minimum valid pattern length
     * @param maxLen    the maximum valid pattern length
     * @param visited   boolean array tracking which digits have been used
     * @return the count of valid patterns that can be formed from this state
     *
     * @implNote Time Complexity: O(9!) worst case per starting digit.
     *           Space Complexity: O(9) for recursion depth.
     */
    private int backtrack(int current, int length, int minLen, int maxLen, boolean[] visited) {
        // Base case: initialize count for this call
        int count = 0;

        // Step 1: If the current pattern length is within the valid range, count it
        if (length >= minLen) {
            count++; // This pattern (of current length) is valid — count it
        }

        // Step 2: If we've reached the maximum allowed length, stop extending
        if (length == maxLen) {
            return count; // No need to go deeper; return what we have
        }

        // Step 3: Try extending the pattern by adding each unvisited digit
        for (int next = 1; next <= 9; next++) {
            // Skip digits that have already been used in the current pattern
            if (visited[next]) {
                continue; // This digit is already in the pattern, skip it
            }

            // Check the skip rule:
            // skip[current][next] gives the digit that must be visited before
            // we can move directly from 'current' to 'next'.
            // If skip[current][next] == 0, no intermediate digit is required.
            int skipDigit = skip[current][next];

            // The move is valid if:
            //   - There is no required intermediate digit (skipDigit == 0), OR
            //   - The required intermediate digit has already been visited
            if (skipDigit == 0 || visited[skipDigit]) {
                // This move is valid! Proceed with the backtracking.

                // Mark 'next' as visited before going deeper
                visited[next] = true;

                // Recurse: extend the pattern from 'next' with length + 1
                count += backtrack(next, length + 1, minLen, maxLen, visited);

                // Backtrack: unmark 'next' so other branches can use it
                visited[next] = false;
            }
            // If the move is invalid (skip digit not yet visited), we simply skip this 'next'
        }

        // Return the total count of valid patterns found from this state
        return count;
    }

    /**
     * Main method to demonstrate the solution with sample inputs and verify correctness.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        System.out.println("=== Generate All Valid PIN Patterns ===");
        System.out.println();

        // -------------------------------------------------------
        // Example 1: minLen = 1, maxLen = 1
        // Expected Output: 9 (all single-digit patterns are valid)
        // -------------------------------------------------------
        int result1 = solution.numberOfPatterns(1, 1);
        System.out.println("Example 1: minLen=1, maxLen=1");
        System.out.println("Expected: 9");
        System.out.println("Got:      " + result1);
        System.out.println("PASS: " + (result1 == 9));
        System.out.println();

        // -------------------------------------------------------
        // Example 2: minLen = 1, maxLen = 2
        // Expected Output: 65 (9 single-digit + 56 two-digit patterns)
        // -------------------------------------------------------
        int result2 = solution.numberOfPatterns(1, 2);
        System.out.println("Example 2: minLen=1, maxLen=2");
        System.out.println("Expected: 65");
        System.out.println("Got:      " + result2);
        System.out.println("PASS: " + (result2 == 65));
        System.out.println();

        // -------------------------------------------------------
        // Additional test: minLen = 2, maxLen = 2
        // Expected: 56 (only two-digit patterns)
        // Explanation: From each digit, count valid next moves.
        // -------------------------------------------------------
        int result3 = solution.numberOfPatterns(2, 2);
        System.out.println("Additional Test: minLen=2, maxLen=2");
        System.out.println("Expected: 56");
        System.out.println("Got:      " + result3);
        System.out.println("PASS: " + (result3 == 56));
        System.out.println();

        // -------------------------------------------------------
        // Additional test: minLen = 4, maxLen = 4
        // Expected: 1624 (known result for 4-digit patterns)
        // -------------------------------------------------------
        int result4 = solution.numberOfPatterns(4, 4);
        System.out.println("Additional Test: minLen=4, maxLen=4");
        System.out.println("Expected: 1624");
        System.out.println("Got:      " + result4);
        System.out.println("PASS: " + (result4 == 1624));
        System.out.println();

        // -------------------------------------------------------
        // Additional test: minLen = 1, maxLen = 9
        // Expected: 389112 (total valid patterns of all lengths)
        // -------------------------------------------------------
        int result5 = solution.numberOfPatterns(1, 9);
        System.out.println("Additional Test: minLen=1, maxLen=9");
        System.out.println("Expected: 389112");
        System.out.println("Got:      " + result5);
        System.out.println("PASS: " + (result5 == 389112));
        System.out.println();

        // -------------------------------------------------------
        // Explanation of the skip rule with examples
        // -------------------------------------------------------
        System.out.println("=== Skip Rule Explanation ===");
        System.out.println("Grid layout:");
        System.out.println("  1 2 3");
        System.out.println("  4 5 6");
        System.out.println("  7 8 9");
        System.out.println();
        System.out.println("1->3: requires 2 to be visited first (same row, 2 is between them)");
        System.out.println("1->9: requires 5 to be visited first (main diagonal, 5 is between them)");
        System.out.println("1->7: requires 4 to be visited first (same column, 4 is between them)");
        System.out.println("3->7: requires 5 to be visited first (anti-diagonal, 5 is between them)");
        System.out.println("2->8: requires 5 to be visited first (same column, 5 is between them)");
        System.out.println();
        System.out.println("Adjacent/diagonal moves (no skip required):");
        System.out.println("1->2, 1->4, 1->5 (diagonal), 2->3, 2->5, etc.");
    }
}