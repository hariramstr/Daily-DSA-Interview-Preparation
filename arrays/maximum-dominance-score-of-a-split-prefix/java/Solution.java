import java.util.*;

/*
 * Title: Maximum Dominance Score of a Split Prefix
 * Difficulty: Hard
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array nums of length n. For any split point i where 0 <= i < n - 1,
 * divide the array into a left part nums[0..i] and a right part nums[i+1..n-1].
 *
 * Define the dominance score of this split as:
 * 1) the absolute difference between the maximum subarray sum entirely contained in the left part
 *    and the minimum subarray sum entirely contained in the right part
 * 2) the absolute difference between the minimum subarray sum entirely contained in the left part
 *    and the maximum subarray sum entirely contained in the right part
 *
 * The score of split i is the larger of those two values.
 * Return the maximum score over all valid split points.
 *
 * A subarray must be non-empty and contiguous.
 * The maximum and minimum subarray sums are computed independently within each side of the split;
 * they do not need to touch the split boundary.
 *
 * Constraints:
 * - 2 <= n <= 200000
 * - -1000000000 <= nums[i] <= 1000000000
 * - The answer fits in a signed 64-bit integer
 *
 * Efficient idea:
 * Precompute for every index:
 * - best maximum subarray sum in prefix [0..i]
 * - best minimum subarray sum in prefix [0..i]
 * - best maximum subarray sum in suffix [i..n-1]
 * - best minimum subarray sum in suffix [i..n-1]
 *
 * Then for every split i:
 * - left side is [0..i]
 * - right side is [i+1..n-1]
 *
 * Candidate score is:
 * max(
 *     abs(prefixMax[i] - suffixMin[i + 1]),
 *     abs(prefixMin[i] - suffixMax[i + 1])
 * )
 *
 * The answer is the maximum candidate over all splits.
 */

public class Solution {

    /**
     * Computes the maximum dominance score over all valid split points.
     *
     * @param nums the input integer array
     * @return the maximum dominance score as a long
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long maximumDominanceScore(int[] nums) {
        int n = nums.length;

        // For safety and correctness with large values, all running sums are stored in long.
        long[] prefixMax = buildPrefixMaxSubarray(nums);
        long[] prefixMin = buildPrefixMinSubarray(nums);
        long[] suffixMax = buildSuffixMaxSubarray(nums);
        long[] suffixMin = buildSuffixMinSubarray(nums);

        long answer = 0L;

        // Try every split point i:
        // left  = [0..i]
        // right = [i+1..n-1]
        for (int i = 0; i < n - 1; i++) {
            // Option 1:
            // best positive dominance from left maximum versus right minimum
            long option1 = Math.abs(prefixMax[i] - suffixMin[i + 1]);

            // Option 2:
            // reverse direction: left minimum versus right maximum
            long option2 = Math.abs(prefixMin[i] - suffixMax[i + 1]);

            long splitScore = Math.max(option1, option2);
            answer = Math.max(answer, splitScore);
        }

        return answer;
    }

    /**
     * Builds an array where result[i] is the maximum subarray sum found anywhere in nums[0..i].
     *
     * @param nums the input integer array
     * @return prefix maximum subarray sums
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long[] buildPrefixMaxSubarray(int[] nums) {
        int n = nums.length;
        long[] result = new long[n];

        // Kadane's algorithm for maximum subarray sum on prefixes.
        // currentEndingHere = best max subarray sum that MUST end at current index
        // result[i]         = best max subarray sum seen anywhere in prefix [0..i]
        long currentEndingHere = nums[0];
        result[0] = nums[0];

        for (int i = 1; i < n; i++) {
            long value = nums[i];

            // Either:
            // 1) start a new subarray at i
            // 2) extend the previous best subarray that ended at i-1
            currentEndingHere = Math.max(value, currentEndingHere + value);

            // Best anywhere in the prefix is either:
            // - previous best
            // - the new subarray ending at i
            result[i] = Math.max(result[i - 1], currentEndingHere);
        }

        return result;
    }

    /**
     * Builds an array where result[i] is the minimum subarray sum found anywhere in nums[0..i].
     *
     * @param nums the input integer array
     * @return prefix minimum subarray sums
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long[] buildPrefixMinSubarray(int[] nums) {
        int n = nums.length;
        long[] result = new long[n];

        // Kadane-style algorithm for minimum subarray sum on prefixes.
        // currentEndingHere = best min subarray sum that MUST end at current index
        // result[i]         = best min subarray sum seen anywhere in prefix [0..i]
        long currentEndingHere = nums[0];
        result[0] = nums[0];

        for (int i = 1; i < n; i++) {
            long value = nums[i];

            // For minimum subarray:
            // Either start new at i, or extend previous minimum-ending subarray.
            currentEndingHere = Math.min(value, currentEndingHere + value);

            // Best minimum anywhere in prefix.
            result[i] = Math.min(result[i - 1], currentEndingHere);
        }

        return result;
    }

    /**
     * Builds an array where result[i] is the maximum subarray sum found anywhere in nums[i..n-1].
     *
     * @param nums the input integer array
     * @return suffix maximum subarray sums
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long[] buildSuffixMaxSubarray(int[] nums) {
        int n = nums.length;
        long[] result = new long[n];

        // Reverse-direction Kadane for suffixes.
        // currentStartingHere = best max subarray sum that MUST start at current index
        // result[i]           = best max subarray sum seen anywhere in suffix [i..n-1]
        long currentStartingHere = nums[n - 1];
        result[n - 1] = nums[n - 1];

        for (int i = n - 2; i >= 0; i--) {
            long value = nums[i];

            // Either:
            // 1) start new at i
            // 2) extend into the right side
            currentStartingHere = Math.max(value, value + currentStartingHere);

            // Best anywhere in suffix [i..n-1]
            result[i] = Math.max(result[i + 1], currentStartingHere);
        }

        return result;
    }

    /**
     * Builds an array where result[i] is the minimum subarray sum found anywhere in nums[i..n-1].
     *
     * @param nums the input integer array
     * @return suffix minimum subarray sums
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long[] buildSuffixMinSubarray(int[] nums) {
        int n = nums.length;
        long[] result = new long[n];

        // Reverse-direction Kadane for minimum subarray sums on suffixes.
        // currentStartingHere = best min subarray sum that MUST start at current index
        // result[i]           = best min subarray sum seen anywhere in suffix [i..n-1]
        long currentStartingHere = nums[n - 1];
        result[n - 1] = nums[n - 1];

        for (int i = n - 2; i >= 0; i--) {
            long value = nums[i];

            // Either start new at i, or extend rightward.
            currentStartingHere = Math.min(value, value + currentStartingHere);

            // Best minimum anywhere in suffix [i..n-1]
            result[i] = Math.min(result[i + 1], currentStartingHere);
        }

        return result;
    }

    /**
     * Utility method to print an int array.
     *
     * @param nums the array to print
     * @return a readable string representation
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String arrayToString(int[] nums) {
        return Arrays.toString(nums);
    }

    /**
     * Demonstrates the solution on sample inputs and a few extra checks.
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(n) per demonstrated test case
     * Space complexity: O(n) per demonstrated test case
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] nums1 = {2, -5, 4, -1, 3};
        long result1 = solution.maximumDominanceScore(nums1);
        System.out.println("Input:  " + solution.arrayToString(nums1));
        System.out.println("Output: " + result1);

        int[] nums2 = {7, -2, -6, 5, -1, 4};
        long result2 = solution.maximumDominanceScore(nums2);
        System.out.println("Input:  " + solution.arrayToString(nums2));
        System.out.println("Output: " + result2);

        int[] nums3 = {1, -2};
        long result3 = solution.maximumDominanceScore(nums3);
        System.out.println("Input:  " + solution.arrayToString(nums3));
        System.out.println("Output: " + result3);

        int[] nums4 = {-3, -1, -2, -4};
        long result4 = solution.maximumDominanceScore(nums4);
        System.out.println("Input:  " + solution.arrayToString(nums4));
        System.out.println("Output: " + result4);

        int[] nums5 = {5, 1, 2, 3};
        long result5 = solution.maximumDominanceScore(nums5);
        System.out.println("Input:  " + solution.arrayToString(nums5));
        System.out.println("Output: " + result5);
    }
}