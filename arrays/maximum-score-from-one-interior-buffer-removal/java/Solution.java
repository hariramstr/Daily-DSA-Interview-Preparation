import java.util.*;

/*
 * Title: Maximum Score from One Interior Buffer Removal
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array nums representing the value of blocks in a processing pipeline.
 * You may remove exactly one contiguous subarray that is strictly inside the array, meaning the
 * removed segment cannot include the first or the last element. After the removal, the remaining
 * left part and right part are concatenated. The score of the final array is the sum of its elements.
 *
 * Your task is to return the maximum possible score after removing one valid interior subarray.
 * Since removing a subarray decreases the total sum, the goal is equivalent to removing an interior
 * subarray with the minimum possible sum.
 *
 * A valid removed subarray must satisfy 1 <= l <= r <= n - 2 using 0-based indexing, where
 * nums[l..r] is removed and both nums[0] and nums[n-1] remain in the final array.
 *
 * Write a function that returns the maximum score obtainable.
 *
 * Constraints:
 * - 3 <= nums.length <= 200000
 * - -1000000000 <= nums[i] <= 1000000000
 * - The answer fits in a signed 64-bit integer
 *
 * Example 1:
 * Input: nums = [5, -2, 3, -4, 6]
 * Output: 12
 * Explanation:
 * The total sum is 8.
 * Valid interior subarrays are inside indices [1..3]:
 *   [-2] -> sum = -2, remaining score = 10
 *   [-2, 3] -> sum = 1, remaining score = 7
 *   [-2, 3, -4] -> sum = -3, remaining score = 11
 *   [3] -> sum = 3, remaining score = 5
 *   [3, -4] -> sum = -1, remaining score = 9
 *   [-4] -> sum = -4, remaining score = 12
 * The minimum removable sum is -4, so the maximum score is 8 - (-4) = 12.
 *
 * Example 2:
 * Input: nums = [4, 7, 2, 9]
 * Output: 20
 * Explanation:
 * The total sum is 22.
 * Valid interior subarrays are [7], [2], and [7, 2] with sums 7, 2, and 9.
 * The minimum removable sum is 2, so the maximum score is 22 - 2 = 20.
 *
 * Linear-time idea:
 * 1. Compute the total sum of the entire array.
 * 2. Find the minimum-sum contiguous subarray only within the interior range [1..n-2].
 * 3. Answer = totalSum - minInteriorSubarraySum.
 *
 * To find the minimum-sum subarray in linear time, we use a "minimum Kadane's algorithm":
 * - Let currentMinEndingHere be the minimum sum of a subarray that must end at the current index.
 * - Let bestMinSoFar be the minimum sum seen anywhere so far.
 * - Transition:
 *     currentMinEndingHere = min(nums[i], currentMinEndingHere + nums[i])
 * - Update:
 *     bestMinSoFar = min(bestMinSoFar, currentMinEndingHere)
 *
 * This works because at each interior index we decide:
 * - either start a new removable subarray at i,
 * - or extend the previous removable subarray to include nums[i].
 *
 * Since we only iterate over indices 1 through n-2, the removed subarray is guaranteed to stay
 * strictly inside the array.
 */
public class Solution {

    /**
     * Returns the maximum possible score after removing exactly one contiguous interior subarray.
     *
     * The score after removal equals:
     * total sum of array - sum of removed subarray
     *
     * Therefore, to maximize the score, we must remove the valid interior subarray with the
     * minimum possible sum.
     *
     * This method:
     * 1. Computes the total sum of the array.
     * 2. Finds the minimum-sum contiguous subarray restricted to indices [1..n-2].
     * 3. Returns totalSum - minInteriorSubarraySum.
     *
     * @param nums the input array; length is at least 3, and exactly one strictly interior subarray must be removed
     * @return the maximum obtainable score after removing one valid interior contiguous subarray
     * Time complexity: O(n), where n is nums.length
     * Space complexity: O(1), ignoring input storage
     */
    public long maximumScoreAfterOneInteriorRemoval(int[] nums) {
        // Step 1:
        // Compute the total sum of the entire array.
        // We use long because:
        // - nums[i] can be as large as 1e9 in magnitude
        // - n can be up to 2e5
        // - int could overflow, but the problem guarantees the final answer fits in 64 bits
        long totalSum = 0L;
        for (int value : nums) {
            totalSum += value;
        }

        // Step 2:
        // We now need the minimum-sum contiguous subarray, but ONLY inside the valid removable range:
        // indices 1 through n - 2 inclusive.
        //
        // We apply a "minimum Kadane" algorithm over that interior range.
        //
        // Initialization:
        // The first possible removable subarray must start and end at index 1 initially.
        long currentMinEndingHere = nums[1];
        long bestMinSoFar = nums[1];

        // Step 3:
        // Process each remaining interior index.
        for (int i = 2; i <= nums.length - 2; i++) {
            // For a minimum-sum subarray ending at index i, there are exactly two choices:
            //
            // Choice A: Start a brand new subarray at i
            //           sum = nums[i]
            //
            // Choice B: Extend the previous minimum-sum subarray ending at i-1
            //           sum = currentMinEndingHere + nums[i]
            //
            // We choose the smaller of the two because we want the minimum possible sum.
            currentMinEndingHere = Math.min((long) nums[i], currentMinEndingHere + nums[i]);

            // Update the global best minimum removable sum found so far.
            bestMinSoFar = Math.min(bestMinSoFar, currentMinEndingHere);
        }

        // Step 4:
        // Remove the minimum-sum interior subarray.
        // Since removing a negative sum increases the final score, this formula handles all cases:
        // answer = totalSum - minimumRemovedSum
        return totalSum - bestMinSoFar;
    }

    /**
     * Finds the minimum-sum contiguous subarray restricted to the interior indices [1..n-2].
     *
     * This helper is useful for educational tracing and validation.
     *
     * @param nums the input array
     * @return the minimum possible sum of any valid removable interior contiguous subarray
     * Time complexity: O(n), where n is nums.length
     * Space complexity: O(1)
     */
    public long minimumInteriorSubarraySum(int[] nums) {
        long currentMinEndingHere = nums[1];
        long bestMinSoFar = nums[1];

        for (int i = 2; i <= nums.length - 2; i++) {
            currentMinEndingHere = Math.min((long) nums[i], currentMinEndingHere + nums[i]);
            bestMinSoFar = Math.min(bestMinSoFar, currentMinEndingHere);
        }

        return bestMinSoFar;
    }

    /**
     * Computes the total sum of the array using long arithmetic.
     *
     * @param nums the input array
     * @return the total sum of all elements in nums
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long totalSum(int[] nums) {
        long sum = 0L;
        for (int value : nums) {
            sum += value;
        }
        return sum;
    }

    /**
     * Converts an int array to a readable string representation.
     *
     * @param nums the input array
     * @return a string like [1, 2, 3]
     * Time complexity: O(n)
     * Space complexity: O(n) for the produced string
     */
    public String arrayToString(int[] nums) {
        return Arrays.toString(nums);
    }

    /**
     * Demonstrates the solution on sample inputs and a few additional checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(k * n) across demonstrated test cases
     * Space complexity: O(1), excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1 from the problem statement
        int[] nums1 = {5, -2, 3, -4, 6};
        long result1 = solution.maximumScoreAfterOneInteriorRemoval(nums1);
        System.out.println("Input:  " + solution.arrayToString(nums1));
        System.out.println("Total sum: " + solution.totalSum(nums1));
        System.out.println("Minimum interior subarray sum: " + solution.minimumInteriorSubarraySum(nums1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 12");
        System.out.println();

        // Sample 2 from the problem statement
        int[] nums2 = {4, 7, 2, 9};
        long result2 = solution.maximumScoreAfterOneInteriorRemoval(nums2);
        System.out.println("Input:  " + solution.arrayToString(nums2));
        System.out.println("Total sum: " + solution.totalSum(nums2));
        System.out.println("Minimum interior subarray sum: " + solution.minimumInteriorSubarraySum(nums2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 20");
        System.out.println();

        // Additional beginner-friendly checks

        // Only one interior element can be removed when n = 3
        int[] nums3 = {10, -5, 7};
        long result3 = solution.maximumScoreAfterOneInteriorRemoval(nums3);
        System.out.println("Input:  " + solution.arrayToString(nums3));
        System.out.println("Total sum: " + solution.totalSum(nums3));
        System.out.println("Minimum interior subarray sum: " + solution.minimumInteriorSubarraySum(nums3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 17");
        System.out.println();

        // All positive interior values: we still must remove exactly one subarray,
        // so we remove the smallest positive interior subarray
        int[] nums4 = {8, 3, 5, 2, 10};
        long result4 = solution.maximumScoreAfterOneInteriorRemoval(nums4);
        System.out.println("Input:  " + solution.arrayToString(nums4));
        System.out.println("Total sum: " + solution.totalSum(nums4));
        System.out.println("Minimum interior subarray sum: " + solution.minimumInteriorSubarraySum(nums4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 26");
        System.out.println();

        // Mixed values
        int[] nums5 = {1, -3, 4, -2, 5};
        long result5 = solution.maximumScoreAfterOneInteriorRemoval(nums5);
        System.out.println("Input:  " + solution.arrayToString(nums5));
        System.out.println("Total sum: " + solution.totalSum(nums5));
        System.out.println("Minimum interior subarray sum: " + solution.minimumInteriorSubarraySum(nums5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 8");
    }
}