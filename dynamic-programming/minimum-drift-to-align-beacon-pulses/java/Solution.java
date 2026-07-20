import java.util.*;

/*
 * Title: Minimum Drift to Align Beacon Pulses
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A monitoring system receives pulse timestamps from two independent space beacons.
 * Due to clock drift and packet loss, the two timestamp sequences are not perfectly aligned.
 * You are given two integer arrays a and b, where a[i] and b[j] are pulse times in milliseconds,
 * sorted in non-decreasing order. You want to align the sequences by partitioning each array
 * into the same number of non-empty contiguous groups. The k-th group from a must be matched
 * with the k-th group from b.
 *
 * If a group from a spans indices l1..r1 and the matched group from b spans indices l2..r2,
 * the drift cost of that matched pair is:
 *
 * abs((sum of a[l1..r1]) - (sum of b[l2..r2]))
 *
 * Your task is to return the minimum possible total drift cost over all valid ways to partition
 * both arrays into the same number of contiguous non-empty groups.
 *
 * In other words, you may decide where to cut each sequence, but both sequences must end up
 * with exactly the same number of segments, and segments must be matched in order.
 *
 * Constraints:
 * - 1 <= a.length, b.length <= 200
 * - 1 <= a[i], b[j] <= 10^6
 * - Both arrays are sorted in non-decreasing order
 * - The answer fits in a 64-bit signed integer
 *
 * Key DP idea used in this solution:
 * Let dp[i][j] be the minimum total cost to align the first i elements of a
 * with the first j elements of b.
 *
 * The last matched group must end at a[i - 1] and b[j - 1].
 * Suppose that last group starts right after prefixes x and y, where 0 <= x < i and 0 <= y < j.
 * Then:
 *
 * dp[i][j] = min over all x < i and y < j of
 *            dp[x][y] + abs(sum(a[x..i-1]) - sum(b[y..j-1]))
 *
 * Prefix sums let us compute any segment sum in O(1).
 *
 * Since n, m <= 200, the O(n^2 * m^2) dynamic programming solution is fully acceptable.
 */

public class Solution {

    /**
     * Computes the minimum total drift cost by partitioning both arrays into the same number
     * of non-empty contiguous groups and matching groups in order.
     *
     * Dynamic programming definition:
     * dp[i][j] = minimum cost to align the first i elements of a with the first j elements of b.
     *
     * Transition:
     * We try every possible previous cut position x in a and y in b.
     * The final group is a[x..i-1] matched with b[y..j-1].
     *
     * dp[i][j] = min(dp[x][y] + abs(segmentSumA - segmentSumB))
     *
     * where:
     * segmentSumA = prefixA[i] - prefixA[x]
     * segmentSumB = prefixB[j] - prefixB[y]
     *
     * Base case:
     * dp[0][0] = 0
     * dp[i][0] and dp[0][j] for positive i or j are impossible, because every group must be
     * non-empty in both arrays and the number of groups must match.
     *
     * @param a the first sorted integer array of pulse timestamps
     * @param b the second sorted integer array of pulse timestamps
     * @return the minimum possible total drift cost as a long
     * Time complexity: O(n^2 * m^2), where n = a.length and m = b.length
     * Space complexity: O(n * m)
     */
    public long minimumDrift(int[] a, int[] b) {
        int n = a.length;
        int m = b.length;

        // Build prefix sums so that any contiguous segment sum can be computed in O(1).
        // prefixA[i] = sum of first i elements of a, i.e. a[0] + ... + a[i-1]
        // prefixB[j] = sum of first j elements of b, i.e. b[0] + ... + b[j-1]
        long[] prefixA = buildPrefixSums(a);
        long[] prefixB = buildPrefixSums(b);

        // Use a very large value as "infinity" for impossible/unreached states.
        long INF = Long.MAX_VALUE / 4;

        // dp[i][j] = minimum cost to align first i elements of a with first j elements of b.
        long[][] dp = new long[n + 1][m + 1];

        // Initialize all states as impossible.
        for (int i = 0; i <= n; i++) {
            Arrays.fill(dp[i], INF);
        }

        // Empty prefix aligned with empty prefix costs 0.
        dp[0][0] = 0L;

        // Fill DP table.
        // We compute states in increasing order of i and j so that all smaller prefix states
        // needed by transitions are already available.
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {

                long best = INF;

                // Try every possible previous cut x in array a.
                // This means the last matched group in a is a[x..i-1].
                for (int x = 0; x < i; x++) {
                    long sumA = prefixA[i] - prefixA[x];

                    // Try every possible previous cut y in array b.
                    // This means the last matched group in b is b[y..j-1].
                    for (int y = 0; y < j; y++) {

                        // If dp[x][y] is impossible, skip it.
                        if (dp[x][y] == INF) {
                            continue;
                        }

                        long sumB = prefixB[j] - prefixB[y];
                        long costOfLastPair = Math.abs(sumA - sumB);
                        long candidate = dp[x][y] + costOfLastPair;

                        if (candidate < best) {
                            best = candidate;
                        }
                    }
                }

                dp[i][j] = best;
            }
        }

        return dp[n][m];
    }

    /**
     * Builds a standard prefix sum array for the given integer array.
     *
     * The returned array prefix has length arr.length + 1 and satisfies:
     * prefix[0] = 0
     * prefix[i] = arr[0] + arr[1] + ... + arr[i - 1] for i >= 1
     *
     * This allows any contiguous subarray sum arr[l..r] to be computed as:
     * prefix[r + 1] - prefix[l]
     *
     * @param arr the input integer array
     * @return a long[] prefix sum array
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long[] buildPrefixSums(int[] arr) {
        long[] prefix = new long[arr.length + 1];
        for (int i = 0; i < arr.length; i++) {
            prefix[i + 1] = prefix[i] + arr[i];
        }
        return prefix;
    }

    /**
     * A small helper method to print an array in a readable form.
     *
     * @param arr the array to convert to string
     * @return the string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n) due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * The expected outputs are:
     * Example 1 -> 1
     * Example 2 -> 2
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding the called algorithm
     * Space complexity: O(1) for the demonstration itself, excluding the called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] a1 = {2, 5, 9};
        int[] b1 = {4, 6, 7};
        long result1 = solution.minimumDrift(a1, b1);

        System.out.println("Example 1");
        System.out.println("a = " + solution.arrayToString(a1));
        System.out.println("b = " + solution.arrayToString(b1));
        System.out.println("Minimum drift = " + result1);
        System.out.println("Expected = 1");
        System.out.println();

        int[] a2 = {1, 3, 8, 10};
        int[] b2 = {2, 4, 6, 12};
        long result2 = solution.minimumDrift(a2, b2);

        System.out.println("Example 2");
        System.out.println("a = " + solution.arrayToString(a2));
        System.out.println("b = " + solution.arrayToString(b2));
        System.out.println("Minimum drift = " + result2);
        System.out.println("Expected = 2");
        System.out.println();

        // Additional quick sanity checks for beginners:
        // If both arrays have one element, the only possible partition is one group each.
        int[] a3 = {10};
        int[] b3 = {13};
        long result3 = solution.minimumDrift(a3, b3);

        System.out.println("Sanity Check 1");
        System.out.println("a = " + solution.arrayToString(a3));
        System.out.println("b = " + solution.arrayToString(b3));
        System.out.println("Minimum drift = " + result3);
        System.out.println("Expected = 3");
        System.out.println();

        // Another check where perfect alignment is possible with one group.
        int[] a4 = {1, 2, 3};
        int[] b4 = {6};
        long result4 = solution.minimumDrift(a4, b4);

        System.out.println("Sanity Check 2");
        System.out.println("a = " + solution.arrayToString(a4));
        System.out.println("b = " + solution.arrayToString(b4));
        System.out.println("Minimum drift = " + result4);
        System.out.println("Expected = 0");
    }
}