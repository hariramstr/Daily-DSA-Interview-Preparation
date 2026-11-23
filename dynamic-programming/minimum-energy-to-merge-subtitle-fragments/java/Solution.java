import java.util.*;

/*
Problem Title: Minimum Energy to Merge Subtitle Fragments

Problem Description:
You are given a list of subtitle fragments extracted from a long video. The fragments must be merged in their original order into a single final transcript. Merging fragments is expensive because each merge requires re-rendering and timing adjustments.

Suppose the length of fragment i is lengths[i]. You may only merge adjacent groups of fragments. When you merge one contiguous group of fragments into a single larger group, the energy cost of that operation is equal to the total number of characters in that group. After the merge, that group behaves like a single fragment and can be merged again later.

Your task is to compute the minimum total energy needed to merge all fragments into exactly one fragment.

For example, if the fragments have lengths [4, 2, 7], you could first merge 4 and 2 for cost 6, producing [6, 7], then merge them for cost 13, for a total of 19. A different order may produce a different total, so you must find the minimum possible total.

Return the minimum total energy.

Constraints:
- 1 <= lengths.length <= 300
- 1 <= lengths[i] <= 10^4
- The answer fits in a 32-bit signed integer

Example 1:
Input: lengths = [4,2,7]
Output: 19
Explanation: Merge [4,2] first for cost 6, then merge [6,7] for cost 13. Total = 19. Merging [2,7] first would cost 9, then merging [4,9] would cost 13, total = 22.

Example 2:
Input: lengths = [3,5,1,2]
Output: 22
Explanation: One optimal strategy is to merge [1,2] first for cost 3, giving [3,5,3]. Then merge the last two groups [5,3] for cost 8, giving [3,8]. Finally merge [3,8] for cost 11. Total = 3 + 8 + 11 = 22.

This is an interval dynamic programming problem. The order of merges matters, and greedy choices do not always lead to the optimal answer. Efficient solutions typically use prefix sums to compute the total length of any interval quickly, then apply DP over all subarray ranges.
*/

public class Solution {

    /**
     * Computes the minimum total energy required to merge all subtitle fragments
     * into exactly one fragment.
     *
     * Core idea:
     * - Let dp[i][j] represent the minimum energy needed to merge the subarray
     *   lengths[i..j] into one fragment.
     * - To compute dp[i][j], we try every possible final split point k where:
     *   - lengths[i..k] is merged into one fragment
     *   - lengths[k+1..j] is merged into one fragment
     *   - then those two resulting adjacent fragments are merged together
     * - Therefore:
     *   dp[i][j] = min over k in [i, j-1] of:
     *              dp[i][k] + dp[k+1][j] + sum(lengths[i..j])
     *
     * Why the interval sum is added:
     * - After both halves become single fragments, the final merge of those two
     *   adjacent fragments costs the total size of the whole interval.
     *
     * @param lengths an array where lengths[i] is the character length of fragment i
     * @return the minimum total energy needed to merge all fragments into one
     * Time complexity: O(n^3), where n is lengths.length
     * Space complexity: O(n^2)
     */
    public int minimumEnergy(int[] lengths) {
        int n = lengths.length;

        // If there is only one fragment, no merge is needed.
        if (n <= 1) {
            return 0;
        }

        // Build prefix sums so we can get the total length of any interval in O(1).
        // prefix[i] stores the sum of the first i elements:
        // prefix[0] = 0
        // prefix[1] = lengths[0]
        // prefix[2] = lengths[0] + lengths[1]
        // ...
        int[] prefix = buildPrefixSums(lengths);

        // dp[i][j] will store the minimum energy required to merge fragments
        // from index i to index j inclusive into a single fragment.
        int[][] dp = new int[n][n];

        // Base case:
        // dp[i][i] = 0 because a single fragment is already "merged".
        // Java initializes int arrays to 0 by default, so this is already satisfied.

        // We fill the DP table by increasing interval length.
        // This is crucial because dp[i][j] depends on smaller intervals:
        // dp[i][k] and dp[k+1][j].
        //
        // len = current interval size
        // Start from 2 because intervals of size 1 are base cases.
        for (int len = 2; len <= n; len++) {

            // Slide a window of size len across the array.
            for (int i = 0; i + len - 1 < n; i++) {
                int j = i + len - 1;

                // Initialize with a very large value because we want the minimum.
                dp[i][j] = Integer.MAX_VALUE;

                // Total size of the current interval lengths[i..j].
                // This is the cost of the FINAL merge step after both sides
                // have already been individually merged into one fragment each.
                int totalLength = rangeSum(prefix, i, j);

                // Try every possible place to split the interval into two parts:
                // [i..k] and [k+1..j]
                //
                // Each split represents the idea that the LAST operation for this
                // interval merges these two already-merged adjacent groups.
                for (int k = i; k < j; k++) {
                    int leftCost = dp[i][k];
                    int rightCost = dp[k + 1][j];

                    // Cost if we choose split point k:
                    // 1) merge left half optimally
                    // 2) merge right half optimally
                    // 3) merge the two resulting fragments together
                    int candidate = leftCost + rightCost + totalLength;

                    // Keep the best (minimum) among all possible split points.
                    if (candidate < dp[i][j]) {
                        dp[i][j] = candidate;
                    }
                }
            }
        }

        // The answer for the whole array is the minimum cost to merge [0..n-1].
        return dp[0][n - 1];
    }

    /**
     * Builds a prefix sum array for fast interval sum queries.
     *
     * Example:
     * lengths = [4, 2, 7]
     * prefix  = [0, 4, 6, 13]
     *
     * Then:
     * sum of lengths[l..r] = prefix[r + 1] - prefix[l]
     *
     * @param lengths the input fragment lengths
     * @return a prefix sum array of size lengths.length + 1
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] buildPrefixSums(int[] lengths) {
        int[] prefix = new int[lengths.length + 1];
        for (int i = 0; i < lengths.length; i++) {
            prefix[i + 1] = prefix[i] + lengths[i];
        }
        return prefix;
    }

    /**
     * Returns the sum of lengths from index left to index right inclusive,
     * using the prefix sum array.
     *
     * @param prefix the prefix sum array where prefix[i] is the sum of the first i elements
     * @param left the starting index of the interval (inclusive)
     * @param right the ending index of the interval (inclusive)
     * @return the sum of the original array values in the interval [left, right]
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int rangeSum(int[] prefix, int left, int right) {
        return prefix[right + 1] - prefix[left];
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement.
     *
     * Expected outputs:
     * - [4, 2, 7] -> 19
     * - [3, 5, 1, 2] -> 22
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding method calls
     * Space complexity: O(1), excluding method calls
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] lengths1 = {4, 2, 7};
        int result1 = solution.minimumEnergy(lengths1);
        System.out.println("Input: " + Arrays.toString(lengths1));
        System.out.println("Minimum total energy: " + result1);
        System.out.println("Expected: 19");
        System.out.println();

        int[] lengths2 = {3, 5, 1, 2};
        int result2 = solution.minimumEnergy(lengths2);
        System.out.println("Input: " + Arrays.toString(lengths2));
        System.out.println("Minimum total energy: " + result2);
        System.out.println("Expected: 22");
        System.out.println();

        int[] lengths3 = {1};
        int result3 = solution.minimumEnergy(lengths3);
        System.out.println("Input: " + Arrays.toString(lengths3));
        System.out.println("Minimum total energy: " + result3);
        System.out.println("Expected: 0");
    }
}