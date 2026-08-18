/*
 * Title: Maximum Sum of a Distinct-Value Window
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array nums and an integer k. A window is any contiguous subarray
 * of length exactly k. A window is called valid if all k elements inside it are pairwise distinct,
 * meaning no value appears more than once in that window.
 *
 * Your task is to return the maximum possible sum among all valid windows of length k.
 * If there is no valid window of length k, return 0.
 *
 * This problem models a situation where you want to choose exactly k consecutive records,
 * but duplicate values in the chosen range are not allowed because they would represent
 * repeated IDs, repeated product codes, or duplicate events. The challenge is to evaluate
 * every length-k range efficiently without recomputing its sum and uniqueness from scratch.
 *
 * A correct solution should work efficiently for large inputs. In particular, iterating over
 * every window and checking duplicates naively may be too slow. Think about how to maintain
 * both the current window sum and the frequency of values as the window slides by one position.
 *
 * Constraints:
 * - 1 <= nums.length <= 200000
 * - 1 <= nums[i] <= 1000000000
 * - 1 <= k <= nums.length
 *
 * Example 1:
 * Input: nums = [5,2,3,5,4,6], k = 3
 * Output: 15
 * Explanation:
 * The length-3 windows are [5,2,3], [2,3,5], [3,5,4], and [5,4,6].
 * All of them contain distinct values. Their sums are 10, 10, 12, and 15.
 * The maximum valid sum is 15.
 *
 * Example 2:
 * Input: nums = [4,4,2,1,2], k = 3
 * Output: 7
 * Explanation:
 * The windows are [4,4,2], [4,2,1], and [2,1,2].
 * The first and third windows are invalid because they contain duplicates.
 * The only valid window is [4,2,1], whose sum is 7.
 *
 * Return the maximum sum of any valid contiguous subarray of length exactly k.
 */

import java.util.*;

public class Solution {

    /**
     * Computes the maximum sum among all contiguous subarrays of length exactly k
     * such that every value in the window is distinct.
     *
     * Approach:
     * We use a sliding window of fixed size k.
     * - Maintain the sum of the current window in O(1) update time.
     * - Maintain a frequency map for values currently inside the window.
     * - A window is valid exactly when the number of distinct keys in the map is k.
     *
     * Why this works:
     * - If the window length is k and the map contains exactly k distinct values,
     *   then every element appears once, so the window is valid.
     * - If the map size is smaller than k, then at least one value repeats, so the window is invalid.
     *
     * @param nums the input array of integers
     * @param k the exact required window length
     * @return the maximum sum of any valid window of length k; returns 0 if no valid window exists
     *
     * Time complexity: O(n), where n is nums.length, because each element is added to and removed
     * from the sliding window at most once.
     * Space complexity: O(k), because the frequency map stores at most k distinct values from the current window.
     */
    public long maximumSubarraySum(int[] nums, int k) {
        // Frequency map:
        // key   -> value from the array
        // value -> how many times that number appears in the current window
        Map<Integer, Integer> frequency = new HashMap<>();

        // Current sum of the sliding window.
        // We use long because:
        // - nums[i] can be as large as 1,000,000,000
        // - k can be large
        // Therefore the sum can exceed int range.
        long windowSum = 0L;

        // Best answer found so far.
        long maxSum = 0L;

        // We expand the window by moving "right" from left to right across the array.
        for (int right = 0; right < nums.length; right++) {
            int incoming = nums[right];

            // Step 1: Add the new element entering the window.
            windowSum += incoming;
            frequency.put(incoming, frequency.getOrDefault(incoming, 0) + 1);

            // Step 2: If the window became larger than k, shrink it from the left.
            // The desired invariant is:
            // after this block, the window size is always <= k.
            if (right >= k) {
                int outgoing = nums[right - k];

                // Remove the outgoing value from the running sum.
                windowSum -= outgoing;

                // Decrease its frequency in the map.
                int updatedCount = frequency.get(outgoing) - 1;

                // If the count becomes zero, remove the key completely.
                // This keeps map.size() equal to the number of distinct values in the window.
                if (updatedCount == 0) {
                    frequency.remove(outgoing);
                } else {
                    frequency.put(outgoing, updatedCount);
                }
            }

            // Step 3: Once we have a full window of size k, check whether it is valid.
            // The current window size is:
            // right - (right - k + 1) + 1 = k
            // whenever right >= k - 1.
            if (right >= k - 1) {
                // A length-k window is valid if and only if it has exactly k distinct values.
                if (frequency.size() == k) {
                    maxSum = Math.max(maxSum, windowSum);
                }
            }
        }

        return maxSum;
    }

    /**
     * Helper method to print an array in a beginner-friendly format.
     *
     * @param nums the array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is nums.length.
     * Space complexity: O(n), due to string construction.
     */
    public String arrayToString(int[] nums) {
        return Arrays.toString(nums);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * Verified examples:
     * 1) nums = [5,2,3,5,4,6], k = 3
     *    Valid windows:
     *    [5,2,3] -> 10
     *    [2,3,5] -> 10
     *    [3,5,4] -> 12
     *    [5,4,6] -> 15
     *    Answer = 15
     *
     * 2) nums = [4,4,2,1,2], k = 3
     *    Windows:
     *    [4,4,2] invalid
     *    [4,2,1] valid -> 7
     *    [2,1,2] invalid
     *    Answer = 7
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(1) for the fixed demonstrations here, excluding the called algorithm.
     * Space complexity: O(1), excluding the called algorithm.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] nums1 = {5, 2, 3, 5, 4, 6};
        int k1 = 3;
        long result1 = solution.maximumSubarraySum(nums1, k1);
        System.out.println("Example 1:");
        System.out.println("nums = " + solution.arrayToString(nums1) + ", k = " + k1);
        System.out.println("Output: " + result1);
        System.out.println("Expected: 15");
        System.out.println();

        int[] nums2 = {4, 4, 2, 1, 2};
        int k2 = 3;
        long result2 = solution.maximumSubarraySum(nums2, k2);
        System.out.println("Example 2:");
        System.out.println("nums = " + solution.arrayToString(nums2) + ", k = " + k2);
        System.out.println("Output: " + result2);
        System.out.println("Expected: 7");
        System.out.println();

        int[] nums3 = {1, 1, 1, 1};
        int k3 = 2;
        long result3 = solution.maximumSubarraySum(nums3, k3);
        System.out.println("Additional Example:");
        System.out.println("nums = " + solution.arrayToString(nums3) + ", k = " + k3);
        System.out.println("Output: " + result3);
        System.out.println("Expected: 0");
        System.out.println();

        int[] nums4 = {9, 8, 7, 6};
        int k4 = 4;
        long result4 = solution.maximumSubarraySum(nums4, k4);
        System.out.println("Additional Example:");
        System.out.println("nums = " + solution.arrayToString(nums4) + ", k = " + k4);
        System.out.println("Output: " + result4);
        System.out.println("Expected: 30");
    }
}