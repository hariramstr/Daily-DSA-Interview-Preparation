/*
 * Title: Generate All Valid Locker Combinations
 *
 * Problem Description:
 * You are managing a secure storage facility where each locker is protected by a numeric
 * combination lock. A valid combination is a sequence of exactly n digits where:
 *
 * 1. Each digit is between 1 and k (inclusive).
 * 2. No two adjacent digits in the combination are the same.
 *
 * Given two integers n (the length of the combination) and k (the maximum digit value),
 * return all valid combinations in lexicographically ascending order.
 *
 * Example 1:
 *   Input: n = 2, k = 3
 *   Output: ["12", "13", "21", "23", "31", "32"]
 *
 * Example 2:
 *   Input: n = 1, k = 4
 *   Output: ["1", "2", "3", "4"]
 *
 * Constraints:
 *   - 1 <= n <= 6
 *   - 1 <= k <= 9
 *   - The total number of valid combinations will not exceed 10,000.
 */

import java.util.ArrayList;
import java.util.List;

/**
 * Solution class for generating all valid locker combinations using recursive backtracking.
 *
 * <p>The core idea is to build each combination digit by digit. At each position,
 * we try all digits from 1 to k, but skip any digit that equals the previously
 * placed digit (to satisfy the "no two adjacent digits are the same" constraint).
 * Because we iterate digits in ascending order (1, 2, ..., k), the resulting
 * list is automatically in lexicographically ascending order.</p>
 */
public class Solution {

    /**
     * Entry point: generates all valid n-digit combinations using digits 1..k
     * where no two adjacent digits are equal.
     *
     * <p>This method initialises the result list and the mutable StringBuilder
     * used as a "current path" during backtracking, then kicks off the recursion.</p>
     *
     * @param n the required length of each combination (1 &lt;= n &lt;= 6)
     * @param k the maximum digit value allowed (1 &lt;= k &lt;= 9)
     * @return a {@link List} of strings representing every valid combination,
     *         sorted in lexicographically ascending order
     *
     * Time complexity:  O(k * (k-1)^(n-1)) — the number of valid combinations,
     *                   each of length n, so overall O(n * k * (k-1)^(n-1)).
     * Space complexity: O(n) for the recursion call stack depth plus the
     *                   StringBuilder of length n; the result list itself
     *                   holds O(k * (k-1)^(n-1)) strings.
     */
    public List<String> generateCombinations(int n, int k) {
        // This list will accumulate every valid combination we discover.
        List<String> results = new ArrayList<>();

        // StringBuilder acts as our "current combination under construction".
        // Using StringBuilder is efficient because we can append/delete in O(1).
        StringBuilder current = new StringBuilder();

        // Launch the recursive backtracking.
        // We pass 0 as the "last digit used" because there is no previous digit yet
        // at the very start — 0 is safely outside the valid range [1, k].
        backtrack(n, k, 0, current, results);

        return results;
    }

    /**
     * Recursive backtracking helper that builds combinations one digit at a time.
     *
     * <p>At each call we are about to place the digit at index {@code current.length()}.
     * We iterate through every candidate digit 1..k, skip the one that equals
     * {@code lastDigit} (adjacency constraint), append the chosen digit, recurse,
     * then remove the digit (backtrack) so the next candidate can be tried.</p>
     *
     * @param n          the target length of the combination
     * @param k          the maximum digit value (digits are in [1, k])
     * @param lastDigit  the digit placed at the previous position (0 if none yet)
     * @param current    the combination built so far as a {@link StringBuilder}
     * @param results    the accumulator list to which completed combinations are added
     *
     * Time complexity:  O(k * (k-1)^(n-1)) recursive leaves, each doing O(n) work
     *                   to convert the StringBuilder to a String.
     * Space complexity: O(n) additional stack depth (one frame per digit position).
     */
    private void backtrack(int n, int k, int lastDigit,
                           StringBuilder current, List<String> results) {

        // ── BASE CASE ──────────────────────────────────────────────────────────
        // If the current combination has reached the required length n,
        // we have a complete, valid combination — record it and return.
        if (current.length() == n) {
            // Convert the StringBuilder to an immutable String and store it.
            results.add(current.toString());
            // Stop recursing deeper; backtrack to try the next candidate.
            return;
        }

        // ── RECURSIVE CASE ────────────────────────────────────────────────────
        // Try placing each digit from 1 to k at the current position.
        // Iterating in ascending order guarantees lexicographic ordering in results.
        for (int digit = 1; digit <= k; digit++) {

            // ── PRUNING / CONSTRAINT CHECK ────────────────────────────────────
            // Skip this digit if it is the same as the digit we placed just before.
            // This enforces the "no two adjacent digits are the same" rule.
            if (digit == lastDigit) {
                // Example: if lastDigit == 2 and digit == 2, skip it.
                continue;
            }

            // ── CHOOSE ────────────────────────────────────────────────────────
            // Append the chosen digit to the current combination path.
            // Character.forDigit(digit, 10) converts int digit to its char ('1'..'9').
            current.append(Character.forDigit(digit, 10));

            // ── EXPLORE ───────────────────────────────────────────────────────
            // Recurse to fill the next position.
            // We pass 'digit' as the new lastDigit so the next level knows
            // which digit to avoid placing immediately after this one.
            backtrack(n, k, digit, current, results);

            // ── UN-CHOOSE (BACKTRACK) ─────────────────────────────────────────
            // Remove the last character we appended so we can try the next digit.
            // current.length() - 1 is the index of the character we just added.
            current.deleteCharAt(current.length() - 1);
        }
        // After the loop, all candidates for this position have been explored.
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MAIN — demonstration & verification
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Demonstrates the solution with the examples given in the problem description
     * and prints the results to standard output.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        Solution solver = new Solution();

        // ── Example 1 ─────────────────────────────────────────────────────────
        // n = 2, k = 3  →  expected: ["12", "13", "21", "23", "31", "32"]
        System.out.println("=== Example 1: n=2, k=3 ===");
        List<String> result1 = solver.generateCombinations(2, 3);
        System.out.println("Output : " + result1);
        System.out.println("Expected: [12, 13, 21, 23, 31, 32]");
        System.out.println("Count  : " + result1.size() + " (expected 6)");
        System.out.println();

        // ── Example 2 ─────────────────────────────────────────────────────────
        // n = 1, k = 4  →  expected: ["1", "2", "3", "4"]
        System.out.println("=== Example 2: n=1, k=4 ===");
        List<String> result2 = solver.generateCombinations(1, 4);
        System.out.println("Output : " + result2);
        System.out.println("Expected: [1, 2, 3, 4]");
        System.out.println("Count  : " + result2.size() + " (expected 4)");
        System.out.println();

        // ── Additional edge-case tests ─────────────────────────────────────────

        // n = 1, k = 1  →  only one digit available, no adjacency issue → ["1"]
        System.out.println("=== Edge case: n=1, k=1 ===");
        List<String> result3 = solver.generateCombinations(1, 1);
        System.out.println("Output : " + result3);
        System.out.println("Expected: [1]");
        System.out.println();

        // n = 2, k = 1  →  only digit is 1, but 1-1 is forbidden → []
        System.out.println("=== Edge case: n=2, k=1 ===");
        List<String> result4 = solver.generateCombinations(2, 1);
        System.out.println("Output : " + result4);
        System.out.println("Expected: []");
        System.out.println();

        // n = 3, k = 2  →  digits {1,2}, length 3, no adjacent equal
        // Valid: 121, 212  →  ["121", "212"]
        System.out.println("=== Extra test: n=3, k=2 ===");
        List<String> result5 = solver.generateCombinations(3, 2);
        System.out.println("Output : " + result5);
        System.out.println("Expected: [121, 212]");
        System.out.println();

        // n = 3, k = 3  →  k*(k-1)^(n-1) = 3*2*2 = 12 combinations
        System.out.println("=== Extra test: n=3, k=3 ===");
        List<String> result6 = solver.generateCombinations(3, 3);
        System.out.println("Output : " + result6);
        System.out.println("Count  : " + result6.size() + " (expected 12)");
        System.out.println();

        // n = 6, k = 9  →  9 * 8^5 = 294,912 combinations (within the 10,000 limit? 
        //                   Actually 294,912 > 10,000 but the problem says ≤10,000,
        //                   so we just show the count here without printing all.)
        System.out.println("=== Stress test: n=6, k=9 ===");
        List<String> result7 = solver.generateCombinations(6, 9);
        System.out.println("Count  : " + result7.size()
                + "  (= 9 * 8^5 = " + (9 * (int) Math.pow(8, 5)) + ")");
        System.out.println("First 5: " + result7.subList(0, 5));
        System.out.println("Last  5: " + result7.subList(result7.size() - 5, result7.size()));
    }
}