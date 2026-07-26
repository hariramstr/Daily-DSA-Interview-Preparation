import java.util.*;

/*
Problem Title: Maximum Score from Splitting an Array into Dominant Ranges

Problem Description:
You are given an integer array nums of length n and an integer k. You must split the array into exactly k non-empty contiguous subarrays. The score of one subarray is defined as the frequency of its most common value multiplied by the length of that subarray. The total score of a split is the sum of the scores of all k chosen subarrays.

Your task is to return the maximum possible total score.

More formally, if a subarray nums[l..r] contains some value x that appears f times, and no other value appears more than f times, then the subarray contributes f * (r - l + 1) to the total. If multiple values tie for maximum frequency, the frequency value is still used only once.

This problem is challenging because the best partition is not determined only by local choices. A longer segment may increase its length but reduce its dominant frequency, while a shorter segment may preserve a strong repeated value and produce a better global answer when combined with later cuts.

Return the maximum total score achievable by partitioning the entire array into exactly k contiguous parts.

Constraints:
- 1 <= n <= 350
- 1 <= k <= min(n, 50)
- 1 <= nums[i] <= 10^5
- Each split must use all elements of nums exactly once.

Example 1:
Input: nums = [1,2,2,1,2], k = 2
Output: 13

Example 2:
Input: nums = [4,4,3,3,3,2,2], k = 3
Output: 23
*/

public class Solution {

    /**
     * Computes the maximum total score obtainable by splitting the array into exactly k
     * non-empty contiguous subarrays.
     *
     * Core idea:
     * 1. Precompute the score of every subarray nums[l..r].
     *    The score is:
     *        (maximum frequency inside nums[l..r]) * (length of nums[l..r])
     * 2. Use dynamic programming:
     *    dp[p][i] = maximum score using exactly p parts to cover the first i elements
     *               (that is, nums[0..i-1]).
     * 3. Transition:
     *    If the last part starts at position j and ends at i-1, then:
     *        dp[p][i] = max(dp[p-1][j] + score[j][i-1])
     *
     * Because n <= 350 and k <= 50, an O(n^3 + k*n^2) or O(n^2 + k*n^2) style solution
     * is acceptable. We implement:
     * - score precomputation in O(n^2)
     * - DP in O(k * n^2)
     *
     * @param nums the input array
     * @param k the exact number of contiguous non-empty parts
     * @return the maximum total score achievable
     * Time complexity: O(n^2 + k * n^2)
     * Space complexity: O(n^2 + k * n)
     */
    public int maximumScore(int[] nums, int k) {
        int n = nums.length;

        // Step 1:
        // Compress values so we can use a compact frequency array instead of a HashMap
        // during subarray score precomputation.
        //
        // Why compress?
        // nums[i] can be as large as 100000, but n is only up to 350.
        // There are at most n distinct values in the array.
        // So we map each distinct value to an id in [0, distinctCount-1].
        int[] compressed = compressValues(nums);

        // Step 2:
        // Precompute score[l][r] for every subarray nums[l..r].
        //
        // score[l][r] = (max frequency in nums[l..r]) * (r - l + 1)
        long[][] score = buildScoreTable(compressed);

        // Step 3:
        // Dynamic programming.
        //
        // dp[parts][i] = best score for splitting first i elements into exactly "parts" groups.
        //
        // i ranges from 0..n
        // - dp[0][0] = 0  (zero elements split into zero parts)
        // - dp[0][i] = impossible for i > 0
        //
        // We use a very negative number to represent impossible states.
        long NEG = Long.MIN_VALUE / 4;
        long[][] dp = new long[k + 1][n + 1];
        for (int parts = 0; parts <= k; parts++) {
            Arrays.fill(dp[parts], NEG);
        }
        dp[0][0] = 0L;

        // Build the answer part by part.
        for (int parts = 1; parts <= k; parts++) {
            // To split first i elements into "parts" non-empty groups:
            // - at least "parts" elements are needed
            // - at most n elements
            for (int i = parts; i <= n; i++) {
                long best = NEG;

                // Let the last segment be nums[j..i-1].
                // Then the previous parts cover nums[0..j-1], i.e. first j elements.
                //
                // Since each part must be non-empty:
                // - previous "parts - 1" groups need at least "parts - 1" elements
                // - last group needs at least 1 element
                // Therefore j must be at least parts - 1 and at most i - 1.
                for (int j = parts - 1; j <= i - 1; j++) {
                    if (dp[parts - 1][j] == NEG) {
                        continue;
                    }

                    long candidate = dp[parts - 1][j] + score[j][i - 1];
                    if (candidate > best) {
                        best = candidate;
                    }
                }

                dp[parts][i] = best;
            }
        }

        return (int) dp[k][n];
    }

    /**
     * Compresses the values of the input array into a dense range [0, distinctCount - 1].
     *
     * Example:
     * nums = [10, 50, 10, 7]
     * compressed might become [0, 1, 0, 2]
     *
     * This is useful because it allows us to store frequencies in a simple int[] array
     * instead of using a HashMap for every subarray expansion.
     *
     * @param nums the original input array
     * @return an array of the same length where equal original values have equal compressed ids
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] compressValues(int[] nums) {
        Map<Integer, Integer> map = new HashMap<>();
        int[] compressed = new int[nums.length];
        int nextId = 0;

        for (int i = 0; i < nums.length; i++) {
            Integer id = map.get(nums[i]);
            if (id == null) {
                id = nextId++;
                map.put(nums[i], id);
            }
            compressed[i] = id;
        }

        return compressed;
    }

    /**
     * Precomputes the score of every subarray.
     *
     * Detailed process:
     * For each starting index l:
     *   - create a fresh frequency array
     *   - extend the right boundary r from l to n-1
     *   - update the frequency of nums[r]
     *   - maintain the current maximum frequency in nums[l..r]
     *   - compute score[l][r] = maxFrequency * (r - l + 1)
     *
     * Because we only extend r one step at a time, we can update the frequency and
     * maxFrequency incrementally.
     *
     * @param compressed the value-compressed array
     * @return a 2D table score where score[l][r] is the score of subarray [l..r]
     * Time complexity: O(n^2)
     * Space complexity: O(n^2)
     */
    public long[][] buildScoreTable(int[] compressed) {
        int n = compressed.length;

        // Find how many distinct compressed ids exist.
        int distinct = 0;
        for (int value : compressed) {
            distinct = Math.max(distinct, value + 1);
        }

        long[][] score = new long[n][n];

        // For every possible left boundary...
        for (int l = 0; l < n; l++) {
            int[] freq = new int[distinct];
            int maxFrequency = 0;

            // Expand the right boundary one step at a time.
            for (int r = l; r < n; r++) {
                int id = compressed[r];
                freq[id]++;

                // Update the dominant frequency of the current subarray.
                if (freq[id] > maxFrequency) {
                    maxFrequency = freq[id];
                }

                int length = r - l + 1;
                score[l][r] = (long) maxFrequency * length;
            }
        }

        return score;
    }

    /**
     * Utility method to print an array in a readable format.
     *
     * @param nums the array to print
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String arrayToString(int[] nums) {
        return Arrays.toString(nums);
    }

    /**
     * Demonstrates the solution on sample inputs and a few additional checks.
     *
     * Note:
     * The problem statement's second example claims the answer is 23.
     * However, if we exhaustively evaluate all valid 3-part splits of:
     * [4,4,3,3,3,2,2]
     * the true maximum is 17.
     *
     * This program prints the computed results directly from the algorithm.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(1) for the demo itself, excluding the called algorithm
     * Space complexity: O(1) for the demo itself, excluding the called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] nums1 = {1, 2, 2, 1, 2};
        int k1 = 2;
        int result1 = solution.maximumScore(nums1, k1);
        System.out.println("Example 1:");
        System.out.println("nums = " + solution.arrayToString(nums1) + ", k = " + k1);
        System.out.println("Maximum total score = " + result1);
        System.out.println("Expected = 13");
        System.out.println();

        int[] nums2 = {4, 4, 3, 3, 3, 2, 2};
        int k2 = 3;
        int result2 = solution.maximumScore(nums2, k2);
        System.out.println("Example 2:");
        System.out.println("nums = " + solution.arrayToString(nums2) + ", k = " + k2);
        System.out.println("Maximum total score = " + result2);
        System.out.println("Note: exhaustive evaluation shows the true optimum is 17, not 23.");
        System.out.println();

        int[] nums3 = {5};
        int k3 = 1;
        int result3 = solution.maximumScore(nums3, k3);
        System.out.println("Additional Test 1:");
        System.out.println("nums = " + solution.arrayToString(nums3) + ", k = " + k3);
        System.out.println("Maximum total score = " + result3);
        System.out.println();

        int[] nums4 = {1, 1, 1, 1};
        int k4 = 2;
        int result4 = solution.maximumScore(nums4, k4);
        System.out.println("Additional Test 2:");
        System.out.println("nums = " + solution.arrayToString(nums4) + ", k = " + k4);
        System.out.println("Maximum total score = " + result4);
        System.out.println();

        int[] nums5 = {1, 2, 3, 4};
        int k5 = 2;
        int result5 = solution.maximumScore(nums5, k5);
        System.out.println("Additional Test 3:");
        System.out.println("nums = " + solution.arrayToString(nums5) + ", k = " + k5);
        System.out.println("Maximum total score = " + result5);
    }
}