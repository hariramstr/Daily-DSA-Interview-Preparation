/*
 * Title: Rainfall Accumulation Between Sensors
 * Difficulty: Medium
 * Topic: Prefix Sum
 *
 * Problem Description:
 * A weather monitoring system has placed sensors at various positions along a straight road
 * of length `n` meters. You are given an integer array `rainfall` of length `n`, where
 * `rainfall[i]` represents the amount of rainfall (in millimeters) recorded at position `i`.
 *
 * You are also given a 2D array `queries` where each query `queries[j] = [left, right, threshold]`
 * asks: How many contiguous subarrays within the range [left, right] (inclusive) have a total
 * rainfall strictly greater than `threshold`?
 * A contiguous subarray means any subarray rainfall[a..b] where left <= a <= b <= right.
 *
 * Return an integer array `results` where `results[j]` is the answer to the j-th query.
 *
 * Constraints:
 * - 1 <= n <= 1000
 * - 0 <= rainfall[i] <= 100
 * - 1 <= queries.length <= 500
 * - 0 <= left <= right < n
 * - 0 <= threshold <= 10^5
 *
 * Example 1:
 * Input: rainfall = [3, 1, 4, 1, 5], queries = [[0, 3, 5], [1, 4, 7]]
 * Output: [4, 5]
 *
 * Example 2:
 * Input: rainfall = [2, 2, 2, 2], queries = [[0, 3, 4], [0, 2, 6]]
 * Output: [6, 0]
 */

import java.util.Arrays;

/**
 * Solution class for the Rainfall Accumulation Between Sensors problem.
 *
 * <p>Approach: We use a prefix sum array to efficiently compute the sum of any subarray
 * in O(1) time. For each query [left, right, threshold], we iterate over all possible
 * starting indices `a` and ending indices `b` within [left, right], compute the subarray
 * sum using the prefix sum array, and count how many sums are strictly greater than threshold.
 */
public class Solution {

    /**
     * Computes the number of contiguous subarrays within each query range [left, right]
     * whose sum is strictly greater than the given threshold.
     *
     * <p>Algorithm Steps:
     * 1. Build a prefix sum array from the rainfall data.
     * 2. For each query, iterate over all (a, b) pairs where left <= a <= b <= right.
     * 3. Use the prefix sum to get the subarray sum in O(1).
     * 4. Count subarrays where sum > threshold.
     *
     * @param rainfall the array of rainfall values at each position
     * @param queries  a 2D array where each row is [left, right, threshold]
     * @return an integer array where each element is the count of qualifying subarrays
     *         for the corresponding query
     *
     * Time Complexity:  O(n^2 * q) where n = rainfall.length and q = queries.length
     *                   For each query we examine O(n^2) subarray pairs.
     * Space Complexity: O(n) for the prefix sum array, plus O(q) for the results array.
     */
    public int[] rainfallAccumulation(int[] rainfall, int[][] queries) {

        // -----------------------------------------------------------------------
        // STEP 1: Build the prefix sum array.
        // prefix[i] = rainfall[0] + rainfall[1] + ... + rainfall[i-1]
        // prefix[0] = 0 (empty prefix, no elements summed yet)
        // prefix[i+1] = prefix[i] + rainfall[i]
        //
        // With this definition, the sum of rainfall[a..b] (inclusive) is:
        //   prefix[b+1] - prefix[a]
        // -----------------------------------------------------------------------
        int n = rainfall.length;

        // Allocate prefix array of size n+1 so that prefix[n] holds the total sum
        int[] prefix = new int[n + 1];

        // prefix[0] is already 0 by default in Java
        for (int i = 0; i < n; i++) {
            // Each prefix entry accumulates the previous total plus the current rainfall
            prefix[i + 1] = prefix[i] + rainfall[i];
        }
        // After this loop:
        // prefix = [0, 3, 4, 8, 9, 14] for rainfall = [3, 1, 4, 1, 5]

        // -----------------------------------------------------------------------
        // STEP 2: Prepare the results array — one answer per query
        // -----------------------------------------------------------------------
        int q = queries.length;
        int[] results = new int[q];

        // -----------------------------------------------------------------------
        // STEP 3: Process each query independently
        // -----------------------------------------------------------------------
        for (int j = 0; j < q; j++) {

            // Extract the three components of this query
            int left      = queries[j][0];  // leftmost allowed start index
            int right     = queries[j][1];  // rightmost allowed end index
            int threshold = queries[j][2];  // we need sum STRICTLY GREATER than this

            // Counter for qualifying subarrays in this query
            int count = 0;

            // -------------------------------------------------------------------
            // STEP 4: Enumerate all subarrays [a, b] with left <= a <= b <= right
            // -------------------------------------------------------------------
            // Outer loop: choose the starting index `a`
            for (int a = left; a <= right; a++) {

                // Inner loop: choose the ending index `b` (must be >= a)
                for (int b = a; b <= right; b++) {

                    // ---------------------------------------------------------
                    // STEP 5: Compute the sum of rainfall[a..b] using prefix sums
                    // sum(a, b) = prefix[b+1] - prefix[a]
                    // ---------------------------------------------------------
                    int subarraySum = prefix[b + 1] - prefix[a];

                    // ---------------------------------------------------------
                    // STEP 6: Check the strictly-greater-than condition
                    // ---------------------------------------------------------
                    if (subarraySum > threshold) {
                        count++;
                    }
                }
            }

            // Store the answer for this query
            results[j] = count;
        }

        // Return the fully populated results array
        return results;
    }

    // ==========================================================================
    // MAIN METHOD — Demonstrates the solution with the provided examples
    // ==========================================================================

    /**
     * Entry point. Runs the two provided examples and prints the results.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        Solution solution = new Solution();

        // ----------------------------------------------------------------------
        // Example 1
        // rainfall = [3, 1, 4, 1, 5]
        // queries  = [[0, 3, 5], [1, 4, 7]]
        // Expected output: [4, 5]
        //
        // Trace for query [0, 3, 5]:
        //   prefix = [0, 3, 4, 8, 9, 14]
        //   All (a,b) pairs within [0,3]:
        //     [0,0] sum=3  -> 3>5? No
        //     [0,1] sum=4  -> 4>5? No
        //     [0,2] sum=8  -> 8>5? YES  count=1
        //     [0,3] sum=9  -> 9>5? YES  count=2
        //     [1,1] sum=1  -> 1>5? No
        //     [1,2] sum=5  -> 5>5? No  (NOT strictly greater)
        //     [1,3] sum=6  -> 6>5? YES  count=3
        //     [2,2] sum=4  -> 4>5? No
        //     [2,3] sum=5  -> 5>5? No
        //     [3,3] sum=1  -> 1>5? No
        //   Total = 3  ... wait, let me re-check the expected answer of 4.
        //
        //   Re-reading the problem explanation: "[3,1,4]=8, [1,4,1]=6, [3,1,4,1]=9"
        //   and also mentions [4,1] is NOT > 5.  That gives 3 listed explicitly.
        //   The problem says "total 4 qualifying subarrays."
        //   Let me recount carefully:
        //     [0,0]=3 No, [0,1]=4 No, [0,2]=8 YES, [0,3]=9 YES
        //     [1,1]=1 No, [1,2]=5 No (not strictly), [1,3]=6 YES
        //     [2,2]=4 No, [2,3]=5 No
        //     [3,3]=1 No
        //   That gives count=3, not 4.
        //
        //   Hmm — the problem explanation says [3,1,4]=8 ✓, [1,4,1]=6 ✓, [3,1,4,1]=9 ✓
        //   and "total 4 qualifying subarrays."  There must be a 4th one.
        //   Looking again: rainfall=[3,1,4,1,5], range [0,3] means indices 0,1,2,3
        //   values are 3,1,4,1.
        //   All subarrays:
        //     [3]=3, [3,1]=4, [3,1,4]=8✓, [3,1,4,1]=9✓
        //     [1]=1, [1,4]=5 (not >5), [1,4,1]=6✓
        //     [4]=4, [4,1]=5 (not >5)
        //     [1]=1
        //   Count = 3.  The problem statement's explanation may have a minor error,
        //   but our algorithm correctly produces 3 for this query based on strict logic.
        //   We will trust our algorithm and verify example 2 as well.
        // ----------------------------------------------------------------------
        int[] rainfall1 = {3, 1, 4, 1, 5};
        int[][] queries1 = {{0, 3, 5}, {1, 4, 7}};
        int[] result1 = solution.rainfallAccumulation(rainfall1, queries1);

        System.out.println("Example 1:");
        System.out.println("rainfall = " + Arrays.toString(rainfall1));
        System.out.println("queries  = " + Arrays.deepToString(queries1));
        System.out.println("Output   = " + Arrays.toString(result1));
        // Our computed output: let's trace query [1,4,7]:
        //   prefix = [0,3,4,8,9,14]
        //   (a,b) in [1,4]:
        //     [1,1]=1 No, [1,2]=5 No, [1,3]=6 No, [1,4]=11 YES
        //     [2,2]=4 No, [2,3]=5 No, [2,4]=9 YES
        //     [3,3]=1 No, [3,4]=6 No
        //     [4,4]=5 No
        //   Count = 2.  Problem says 5.  There's a discrepancy.
        //
        // It seems the problem's examples may be inconsistent with strict interpretation.
        // Let me re-examine: maybe threshold comparison is >= not >.
        // "strictly greater than threshold" — with threshold=7:
        //   [1,4]=11>7 YES, [2,4]=9>7 YES, [1,3]=6 No, [2,3]=5 No ...
        //   Still only 2.
        //
        // Let me try threshold=5 for query1 with >= :
        //   [0,2]=8>=5 YES, [0,3]=9>=5 YES, [1,2]=5>=5 YES, [1,3]=6>=5 YES -> 4! That matches!
        // And threshold=7 for query2 with >=:
        //   [1,4]=11>=7 YES, [2,4]=9>=7 YES, [0..] wait range is [1,4]
        //   Hmm still only 2 with >=.
        //
        // Let me try threshold=7 strictly greater, range [1,4], rainfall=[3,1,4,1,5]:
        //   indices 1,2,3,4 -> values 1,4,1,5
        //   [1]=1,[1,4]=5,[1,4,1]=6,[1,4,1,5]=11>7 YES
        //   [4]=4,[4,1]=5,[4,1,5]=10>7 YES
        //   [1]=1,[1,5]=6,[5]=5
        //   Count=2. Still 2.
        //
        // The problem examples appear to have errors in the expected output.
        // Our algorithm is correct per the problem description (strictly greater than).
        // We will keep our correct implementation.
        System.out.println("(Note: problem example outputs may contain editorial errors;");
        System.out.println(" our algorithm strictly follows the problem statement.)");
        System.out.println();

        // ----------------------------------------------------------------------
        // Example 2
        // rainfall = [2, 2, 2, 2]
        // queries  = [[0, 3, 4], [0, 2, 6]]
        // Expected output: [6, 0]
        //
        // Trace for query [0, 3, 4]:
        //   prefix = [0, 2, 4, 6, 8]
        //   All (a,b) in [0,3]:
        //     [0,0]=2 No, [0,1]=4 No (not strictly >4), [0,2]=6>4 YES, [0,3]=8>4 YES
        //     [1,1]=2 No, [1,2]=4 No, [1,3]=6>4 YES
        //     [2,2]=2 No, [2,3]=4 No
        //     [3,3]=2 No
        //   Count = 3.  Expected 6.
        //
        //   With >= threshold=4:
        //     [0,1]=4>=4 YES, [0,2]=6>=4 YES, [0,3]=8>=4 YES
        //     [1,2]=4>=4 YES, [1,3]=6>=4 YES
        //     [2,3]=4>=4 YES
        //     Plus single elements: [0]=2 No, [1]=2 No, [2]=2 No, [3]=2 No
        //   Count = 6. That matches!
        //
        // So the problem actually wants sum >= threshold, not sum > threshold,
        // despite saying "strictly greater than". OR the threshold in the examples
        // is one less than stated. Let me re-read...
        //
        // "have a total rainfall strictly greater than threshold"
        // Query [0,3,4]: threshold=4, strictly greater means >4.
        // But expected=6 matches >=4.
        //
        // For query [0,2,6]: threshold=6, values [2,2,2]
        //   All sums: 2,4,6,2,4,2 — none >6, none >=6 except =6.
        //   With >6: count=0 ✓
        //   With >=6: [0,2]=6>=6 YES -> count=1, not 0.
        //
        // So for query2 of example2: >6 gives 0 ✓, >=6 gives 1 ✗.
        // For query1 of example2: >4 gives 3 ✗, >=4 gives 6 ✓.
        //
        // There's a contradiction. The examples themselves are incons