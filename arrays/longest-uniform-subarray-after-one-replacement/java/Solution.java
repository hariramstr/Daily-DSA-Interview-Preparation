/*
 * Find the Longest Uniform Subarray After One Replacement
 *
 * Given an integer array nums, you are allowed to replace at most one element
 * in the array with any value of your choice. Return the length of the longest
 * subarray where all elements are equal after performing at most one such replacement.
 *
 * A subarray is a contiguous part of the array.
 *
 * Example 1:
 *   Input: nums = [1, 1, 2, 1, 1]
 *   Output: 5
 *   Explanation: Replace index 2 (value 2) with 1 → [1,1,1,1,1], length = 5
 *
 * Example 2:
 *   Input: nums = [3, 3, 5, 5, 5, 3]
 *   Output: 4
 *   Explanation: Replace index 3 (value 5) with 5 → [5,5,5,5], length = 4
 *
 * Constraints:
 *   1 <= nums.length <= 10^5
 *   1 <= nums[i] <= 10^4
 *   You may replace at most one element with any integer value.
 *   The replacement is optional.
 */

import java.util.*;

/**
 * Solution class for finding the longest uniform subarray after at most one replacement.
 *
 * <p>Core Insight:
 * For each possible "target value" we want the window to be filled with,
 * we use a sliding window that allows at most 1 "bad" element (one that differs
 * from the target). When we see more than 1 bad element, we shrink from the left.
 *
 * <p>However, since we can replace with ANY value, we don't need to fix a target
 * in advance. Instead, we observe:
 * A valid window is one where all elements are the same EXCEPT possibly one.
 * That means: (window length) - (count of the most frequent element in window) <= 1.
 *
 * <p>But for this specific problem (uniform = all equal), the sliding window approach
 * works cleanly by tracking the dominant value in the current window.
 */
public class Solution {

    /**
     * Finds the length of the longest subarray where all elements are equal,
     * after replacing at most one element with any value.
     *
     * <p>Algorithm: Sliding Window
     * We iterate over every possible "target value" that we want the window to contain.
     * For each target value, we maintain a window [left, right] where at most 1 element
     * differs from the target. When more than 1 element differs, we advance the left pointer.
     *
     * <p>Since we don't know the target in advance, we try all distinct values present
     * in the array. The answer is the maximum window size across all targets.
     *
     * @param nums the input integer array
     * @return the length of the longest uniform subarray after at most one replacement
     *
     * @implNote Time Complexity: O(n * k) where k = number of distinct values.
     *           In the worst case O(n^2), but typically much better.
     *           Space Complexity: O(k) for storing distinct values.
     */
    public int longestUniformSubarray(int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }

        // Step 1: Collect all distinct values in the array.
        // We will try each distinct value as the "target" for our uniform window.
        Set<Integer> distinctValues = new HashSet<>();
        for (int num : nums) {
            distinctValues.add(num);
        }

        int maxLength = 1; // At minimum, a single element is always a valid subarray.

        // Step 2: For each distinct target value, run a sliding window.
        for (int target : distinctValues) {
            // 'left' is the left boundary of our sliding window.
            int left = 0;
            // 'replacementsUsed' counts how many elements in [left, right] differ from target.
            // We allow at most 1 replacement (1 "bad" element).
            int replacementsUsed = 0;

            // Step 3: Expand the window by moving 'right' one step at a time.
            for (int right = 0; right < nums.length; right++) {

                // Step 3a: If the current element is NOT the target, we need a replacement.
                if (nums[right] != target) {
                    replacementsUsed++;
                }

                // Step 3b: If we've used more than 1 replacement, shrink from the left
                // until we're back to at most 1 replacement.
                while (replacementsUsed > 1) {
                    // If the element at 'left' was a non-target (a replacement), free it up.
                    if (nums[left] != target) {
                        replacementsUsed--;
                    }
                    left++; // Shrink the window from the left.
                }

                // Step 3c: The current window [left, right] has at most 1 non-target element.
                // Update the maximum length.
                int currentWindowLength = right - left + 1;
                if (currentWindowLength > maxLength) {
                    maxLength = currentWindowLength;
                }
            }
        }

        return maxLength;
    }

    /**
     * Alternative O(n) solution using a two-pointer approach that doesn't require
     * iterating over distinct values separately.
     *
     * <p>Key Insight: Instead of fixing a target value upfront, we observe that
     * in any optimal window, the "target" is the majority element (the most frequent one).
     * We track consecutive runs and merge adjacent runs of the same value separated
     * by exactly one different element.
     *
     * <p>This approach works by:
     * 1. Tracking the current run of equal elements ending at position i.
     * 2. Tracking the previous run of equal elements (before a single different element).
     * 3. If the previous run has the same value as the current run, they can be merged
     *    (with the one different element replaced), giving prevCount + 1 + currCount.
     * 4. If they differ, only the current run + 1 replacement is possible.
     *
     * @param nums the input integer array
     * @return the length of the longest uniform subarray after at most one replacement
     *
     * @implNote Time Complexity: O(n) — single pass through the array.
     *           Space Complexity: O(1) — only a constant number of variables.
     */
    public int longestUniformSubarrayOptimal(int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }

        int n = nums.length;
        int maxLength = 1;

        // 'currCount' = length of the current consecutive run of equal elements ending at i.
        int currCount = 1;
        // 'prevCount' = length of the previous consecutive run of equal elements
        //               (the run just before the most recent "break").
        int prevCount = 0;
        // 'prevValue' = the value of the previous run (to check if it matches the current run).
        int prevValue = -1;

        // Step 1: Iterate from index 1 to n-1, comparing each element to the previous one.
        for (int i = 1; i < n; i++) {

            if (nums[i] == nums[i - 1]) {
                // Step 2a: Same value as previous — extend the current run.
                currCount++;
            } else {
                // Step 2b: Different value — a "break" occurred at position i.
                // The previous run (prevCount, prevValue) and current run (currCount, nums[i-1])
                // are now separated by this single break point.

                // The new "previous run" becomes the old "current run".
                prevCount = currCount;
                prevValue = nums[i - 1];
                // Reset current run for the new value starting at i.
                currCount = 1;
            }

            // Step 3: Calculate the best window ending at index i.
            // We can always replace one element, so:
            // - If prevValue == nums[i]: the previous run and current run have the same value,
            //   so we can bridge them with 1 replacement → prevCount + 1 + currCount.
            // - Otherwise: we can only extend the current run with 1 replacement → currCount + 1.
            //   (But we must not exceed n.)
            int candidate;
            if (prevValue == nums[i]) {
                // Merge previous run + 1 replaced element + current run.
                candidate = prevCount + 1 + currCount;
            } else {
                // Just current run + 1 replacement (the single break before it).
                candidate = currCount + 1;
            }

            // Clamp to array length (can't exceed total elements).
            candidate = Math.min(candidate, n);

            if (candidate > maxLength) {
                maxLength = candidate;
            }
        }

        // Edge case: also consider just the first element alone (handled by maxLength = 1 init).
        // Also consider the full array if it's already uniform (currCount == n at end).
        // The loop handles this naturally.

        return maxLength;
    }

    /**
     * Main method to demonstrate and verify the solution with sample inputs.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        System.out.println("=== Find the Longest Uniform Subarray After One Replacement ===");
        System.out.println();

        // -----------------------------------------------------------------------
        // Test Case 1: nums = [1, 1, 2, 1, 1]
        // Expected Output: 5
        // Explanation: Replace index 2 (value 2) with 1 → entire array is [1,1,1,1,1]
        // -----------------------------------------------------------------------
        int[] nums1 = {1, 1, 2, 1, 1};
        int result1 = solution.longestUniformSubarray(nums1);
        int result1Opt = solution.longestUniformSubarrayOptimal(nums1);
        System.out.println("Test Case 1: nums = " + Arrays.toString(nums1));
        System.out.println("  Expected Output : 5");
        System.out.println("  Sliding Window  : " + result1);
        System.out.println("  Optimal O(n)    : " + result1Opt);
        System.out.println("  PASS: " + (result1 == 5 && result1Opt == 5));
        System.out.println();

        // -----------------------------------------------------------------------
        // Test Case 2: nums = [3, 3, 5, 5, 5, 3]
        // Expected Output: 4
        // Explanation: Replace index 3 (value 5) with 5 → [5,5,5,5] has length 4
        // -----------------------------------------------------------------------
        int[] nums2 = {3, 3, 5, 5, 5, 3};
        int result2 = solution.longestUniformSubarray(nums2);
        int result2Opt = solution.longestUniformSubarrayOptimal(nums2);
        System.out.println("Test Case 2: nums = " + Arrays.toString(nums2));
        System.out.println("  Expected Output : 4");
        System.out.println("  Sliding Window  : " + result2);
        System.out.println("  Optimal O(n)    : " + result2Opt);
        System.out.println("  PASS: " + (result2 == 4 && result2Opt == 4));
        System.out.println();

        // -----------------------------------------------------------------------
        // Test Case 3: Single element array
        // nums = [7]
        // Expected Output: 1
        // -----------------------------------------------------------------------
        int[] nums3 = {7};
        int result3 = solution.longestUniformSubarray(nums3);
        int result3Opt = solution.longestUniformSubarrayOptimal(nums3);
        System.out.println("Test Case 3: nums = " + Arrays.toString(nums3));
        System.out.println("  Expected Output : 1");
        System.out.println("  Sliding Window  : " + result3);
        System.out.println("  Optimal O(n)    : " + result3Opt);
        System.out.println("  PASS: " + (result3 == 1 && result3Opt == 1));
        System.out.println();

        // -----------------------------------------------------------------------
        // Test Case 4: Already uniform array
        // nums = [4, 4, 4, 4]
        // Expected Output: 4
        // -----------------------------------------------------------------------
        int[] nums4 = {4, 4, 4, 4};
        int result4 = solution.longestUniformSubarray(nums4);
        int result4Opt = solution.longestUniformSubarrayOptimal(nums4);
        System.out.println("Test Case 4: nums = " + Arrays.toString(nums4));
        System.out.println("  Expected Output : 4");
        System.out.println("  Sliding Window  : " + result4);
        System.out.println("  Optimal O(n)    : " + result4Opt);
        System.out.println("  PASS: " + (result4 == 4 && result4Opt == 4));
        System.out.println();

        // -----------------------------------------------------------------------
        // Test Case 5: All different elements
        // nums = [1, 2, 3, 4, 5]
        // Expected Output: 2 (replace any one element to match its neighbor)
        // -----------------------------------------------------------------------
        int[] nums5 = {1, 2, 3, 4, 5};
        int result5 = solution.longestUniformSubarray(nums5);
        int result5Opt = solution.longestUniformSubarrayOptimal(nums5);
        System.out.println("Test Case 5: nums = " + Arrays.toString(nums5));
        System.out.println("  Expected Output : 2");
        System.out.println("  Sliding Window  : " + result5);
        System.out.println("  Optimal O(n)    : " + result5Opt);
        System.out.println("  PASS: " + (result5 == 2 && result5Opt == 2));
        System.out.println();

        // -----------------------------------------------------------------------
        // Test Case 6: Two elements, different
        // nums = [1, 2]
        // Expected Output: 2 (replace one to match the other)
        // -----------------------------------------------------------------------
        int[] nums6 = {1, 2};
        int result6 = solution.longestUniformSubarray(nums6);
        int result6Opt = solution.longestUniformSubarrayOptimal(nums6);
        System.out.println("Test Case 6: nums = " + Arrays.toString(nums6));
        System.out.println("  Expected Output : 2");
        System.out.println("  Sliding Window  : " + result6);
        System.out.println("  Optimal O(n)    : " + result6Opt);
        System.out.println("  PASS: " + (result6 == 2 && result6Opt == 2));
        System.out.println();

        // -----------------------------------------------------------------------
        // Test Case 7: Replacement in the middle of a long run
        // nums = [2, 2, 2, 1, 2, 2, 2, 2]
        // Expected Output: 8 (replace index 3 with 2 → all 2s)
        // -----------------------------------------------------------------------
        int[] nums7 = {2, 2, 2, 1, 2, 2, 2, 2};
        int result7 = solution.longestUniformSubarray(nums7);
        int result7Opt = solution.longestUniformSubarrayOptimal(nums7);
        System.out.println("Test Case 7: nums = " + Arrays.toString(nums7));
        System.out.println("  Expected Output : 8");
        System.out.