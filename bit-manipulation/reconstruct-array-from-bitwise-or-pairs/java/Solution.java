```java
/*
 * Title: Reconstruct Array from Bitwise OR Pairs
 *
 * Problem Description:
 * You are given an integer n and a 2D array pairs where each pairs[i] = [index_i, value_i]
 * represents that the bitwise OR of the element at position index_i with some unknown base
 * value B equals value_i. In other words, arr[index_i] | B == value_i for all pairs.
 *
 * Your task is to reconstruct the array arr of length n such that:
 * 1. There exists a non-negative integer B consistent with all the given pairs.
 * 2. arr[index_i] | B == value_i for every pair.
 * 3. Each arr[i] is minimized (i.e., use the smallest valid value for positions not
 *    constrained by any pair).
 *
 * If no valid array exists that satisfies all constraints simultaneously, return an empty array.
 *
 * Note: Positions not referenced in any pair should be set to 0.
 *
 * Constraints:
 * - 1 <= n <= 1000
 * - 0 <= pairs.length <= 1000
 * - 0 <= index_i < n
 * - 0 <= value_i <= 10^6
 * - Multiple pairs may reference the same index; all must be satisfied.
 *
 * Example 1:
 * Input: n = 4, pairs = [[0, 7], [1, 5], [2, 6]]
 * Output: [3, 1, 2, 0]
 * Explanation: With B = 4 (binary 100): 3|4=7, 1|4=5, 2|4=6.
 *
 * Example 2:
 * Input: n = 3, pairs = [[0, 5], [0, 3]]
 * Output: []
 * Explanation: For index 0, we need arr[0] | B == 5 and arr[0] | B == 3 simultaneously,
 * which is impossible since 5 ≠ 3.
 */

import java.util.*;

/**
 * Solution class for the "Reconstruct Array from Bitwise OR Pairs" problem.
 *
 * <p>Key Insight:
 * If arr[i] | B = value_i, then:
 * - B must have all bits set that are in value_i but NOT in arr[i].
 * - arr[i] can only have bits that are in value_i (since OR can only set bits, not clear them).
 * - To minimize arr[i], we want arr[i] to have as few bits as possible.
 * - The bits in value_i that are NOT in B must be in arr[i].
 *
 * Algorithm Overview:
 * 1. Group all pairs by index. For each index, if multiple pairs exist, all values must be equal
 *    (otherwise impossible — same index can't OR with same B to give different results).
 * 2. Determine B: B must be a subset of bits of every value_i (since arr[i] | B = value_i means
 *    B's bits are a subset of value_i's bits). So B must be a subset of the AND of all values.
 *    To maximize B (which helps minimize arr[i] values), set B = AND of all value_i.
 *    Wait — actually B can be any value that is a subset of each value_i. The AND of all values
 *    gives us the bits that are present in ALL values, so B can be at most AND(all values).
 *    To minimize arr[i] = value_i & ~B, we want B to be as large as possible.
 *    The maximum valid B is AND of all value_i.
 * 3. With B determined, arr[i] = value_i & ~B (the bits in value_i not covered by B).
 * 4. Verify: arr[i] | B == value_i for all pairs.
 */
public class Solution {

    /**
     * Reconstructs the array from the given bitwise OR pair constraints.
     *
     * <p>Algorithm:
     * - Group pairs by index; detect conflicts (same index, different values).
     * - Compute B as the AND of all values (maximizes B, minimizes arr[i]).
     * - Set arr[i] = value_i & ~B for each constrained index.
     * - Verify all constraints hold.
     * - Unconstrained positions default to 0.
     *
     * @param n     the length of the array to reconstruct
     * @param pairs the constraint pairs where pairs[i] = {index, value} means arr[index] | B = value
     * @return the reconstructed array satisfying all constraints, or an empty array if impossible
     *
     * Time Complexity:  O(P + n) where P = pairs.length
     * Space Complexity: O(P + n) for the map and result array
     */
    public int[] reconstructArray(int n, int[][] pairs) {

        // Step 1: Group pairs by index using a HashMap.
        // Key = index, Value = the required value for that index.
        // If the same index appears with two different values, it's impossible.
        Map<Integer, Integer> indexToValue = new HashMap<>();

        for (int[] pair : pairs) {
            int idx = pair[0];
            int val = pair[1];

            if (indexToValue.containsKey(idx)) {
                // Same index already seen — check if the value matches
                if (indexToValue.get(idx) != val) {
                    // Contradiction: arr[idx] | B cannot equal two different values simultaneously
                    // because arr[idx] and B are fixed. Return empty array.
                    return new int[0];
                }
                // Same index, same value — no conflict, continue
            } else {
                indexToValue.put(idx, val);
            }
        }

        // Step 2: Determine the base value B.
        //
        // Key reasoning:
        // For each constraint: arr[i] | B = value_i
        // This means every bit in B must also be in value_i (since OR can only add bits).
        // So B is a subset of bits of EVERY value_i.
        // The set of bits common to all value_i is: AND of all value_i.
        // B can be any subset of AND(all values).
        //
        // To minimize arr[i] = value_i & ~B, we want B to be as LARGE as possible.
        // The largest valid B is AND(all values).
        //
        // Special case: if there are no pairs, B can be anything (we choose B = 0),
        // and all arr[i] = 0.

        int B;
        if (indexToValue.isEmpty()) {
            // No constraints at all — B = 0, all arr[i] = 0
            B = 0;
        } else {
            // Start with all bits set, then AND with each value to find common bits
            B = ~0; // all bits set (will be masked by values)
            for (int val : indexToValue.values()) {
                B &= val; // keep only bits present in ALL values
            }
            // B is now the AND of all values — the maximum valid B
            // (Since values are non-negative and up to 10^6, the high bits of B from ~0
            //  will be cleared by the AND with actual values, so B will be non-negative.)
        }

        // Step 3: Build the result array.
        // For constrained positions: arr[i] = value_i & ~B
        //   (bits in value_i that are NOT in B must come from arr[i])
        // For unconstrained positions: arr[i] = 0 (minimized)
        int[] arr = new int[n];

        for (Map.Entry<Integer, Integer> entry : indexToValue.entrySet()) {
            int idx = entry.getKey();
            int val = entry.getValue();

            // arr[idx] gets the bits of val that are NOT covered by B
            arr[idx] = val & ~B;
        }

        // Step 4: Verify all constraints.
        // Even though we derived arr[i] mathematically, let's double-check:
        // arr[i] | B should equal value_i for every pair.
        for (Map.Entry<Integer, Integer> entry : indexToValue.entrySet()) {
            int idx = entry.getKey();
            int val = entry.getValue();

            if ((arr[idx] | B) != val) {
                // This shouldn't happen with correct logic, but guard against edge cases
                return new int[0];
            }
        }

        return arr;
    }

    /**
     * Helper method to format an int array as a string for display.
     *
     * @param arr the array to format
     * @return a string representation like "[1, 2, 3]"
     *
     * Time Complexity:  O(n)
     * Space Complexity: O(n)
     */
    private static String arrayToString(int[] arr) {
        if (arr == null || arr.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i < arr.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Main method demonstrating the solution with sample inputs from the problem description.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1: n = 4, pairs = [[0, 7], [1, 5], [2, 6]]
        // Expected Output: [3, 1, 2, 0]
        // Trace:
        //   indexToValue = {0->7, 1->5, 2->6}
        //   B = 7 & 5 & 6 = 0b111 & 0b101 & 0b110 = 0b100 = 4
        //   arr[0] = 7 & ~4 = 0b111 & 0b...011 = 0b011 = 3
        //   arr[1] = 5 & ~4 = 0b101 & 0b...011 = 0b001 = 1
        //   arr[2] = 6 & ~4 = 0b110 & 0b...011 = 0b010 = 2
        //   arr[3] = 0 (unconstrained)
        //   Verify: 3|4=7✓, 1|4=5✓, 2|4=6✓
        // -----------------------------------------------------------------------
        System.out.println("=== Example 1 ===");
        int n1 = 4;
        int[][] pairs1 = {{0, 7}, {1, 5}, {2, 6}};
        int[] result1 = solution.reconstructArray(n1, pairs1);
        System.out.println("Input: n = " + n1 + ", pairs = [[0,7],[1,5],[2,6]]");
        System.out.println("Output: " + arrayToString(result1));
        System.out.println("Expected: [3, 1, 2, 0]");
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2: n = 3, pairs = [[0, 5], [0, 3]]
        // Expected Output: []
        // Trace:
        //   Index 0 appears with value 5 and value 3 — conflict! Return [].
        // -----------------------------------------------------------------------
        System.out.println("=== Example 2 ===");
        int n2 = 3;
        int[][] pairs2 = {{0, 5}, {0, 3}};
        int[] result2 = solution.reconstructArray(n2, pairs2);
        System.out.println("Input: n = " + n2 + ", pairs = [[0,5],[0,3]]");
        System.out.println("Output: " + arrayToString(result2));
        System.out.println("Expected: []");
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 3: n = 2, pairs = [] (no constraints)
        // Expected Output: [0, 0]
        // Trace:
        //   No pairs, B = 0, all arr[i] = 0
        // -----------------------------------------------------------------------
        System.out.println("=== Example 3 (No Constraints) ===");
        int n3 = 2;
        int[][] pairs3 = {};
        int[] result3 = solution.reconstructArray(n3, pairs3);
        System.out.println("Input: n = " + n3 + ", pairs = []");
        System.out.println("Output: " + arrayToString(result3));
        System.out.println("Expected: [0, 0]");
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 4: n = 3, pairs = [[1, 6], [1, 6]] (duplicate same pair)
        // Expected Output: valid array (not empty)
        // Trace:
        //   Index 1 appears twice with same value 6 — no conflict.
        //   indexToValue = {1->6}
        //   B = 6 (AND of just one value = 6 itself)
        //   arr[1] = 6 & ~6 = 0
        //   arr[0] = 0, arr[2] = 0
        //   Verify: 0|6=6✓
        // -----------------------------------------------------------------------
        System.out.println("=== Example 4 (Duplicate Same Pair) ===");
        int n4 = 3;
        int[][] pairs4 = {{1, 6}, {1, 6}};
        int[] result4 = solution.reconstructArray(n4, pairs4);
        System.out.println("Input: n = " + n4 + ", pairs = [[1,6],[1,6]]");
        System.out.println("Output: " + arrayToString(result4));
        System.out.println("Expected: [0, 0, 0] (B=6, arr[1]=6&~6=0)");
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 5: n = 1, pairs = [[0, 15]]
        // Trace:
        //   B = 15 (AND of single value)
        //   arr[0] = 15 & ~15 = 0
        //   Verify: 0|15=15✓
        // -----------------------------------------------------------------------
        System.out.println("=== Example 5 (Single Pair) ===");
        int n5 = 1;
        int[][] pairs5 = {{0, 15}};
        int[] result5 = solution.reconstructArray(n5, pairs5);
        System.out.println("Input: n = " + n5 + ", pairs = [[0,15]]");
        System.out.println("Output: " + arrayToString(result5));
        System.out.println("Expected: [0] (B=15, arr[0]=0)");
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 6: Verify with manual B check
        // n = 3, pairs = [[0, 12], [1, 10], [2, 14]]
        // 12 = 1100, 10 = 1010, 14 = 1110
        // B = 12 & 10 & 14 = 1100 & 1010 & 1110 = 1000 = 8
        // arr[0] = 12 & ~8 = 1100 & 0111 = 0100 = 4
        // arr[1] = 10 & ~8 = 1010 & 0111 = 0010 = 2
        // arr[2] = 14 & ~8 = 1110 & 0111 = 0110 = 6
        // Verify: 4|8=12✓, 2|8=10✓, 6