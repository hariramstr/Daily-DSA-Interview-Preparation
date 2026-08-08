import java.util.*;

/*
 * Title: Count Subarrays With the Same First and Last Value
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * You are given an integer array nums representing a stream of event codes.
 * A contiguous subarray is called closed if its first element is equal to its last element.
 * Your task is to return the total number of closed subarrays in nums.
 *
 * Formally, count the number of pairs (l, r) such that 0 <= l <= r < n and nums[l] == nums[r].
 * Every single-element subarray is considered closed because its first and last elements are the same.
 *
 * A brute-force solution that checks every subarray would be too slow for large inputs.
 * The intended solution should use hashing to track how many times each value has appeared so far
 * while scanning the array from left to right.
 *
 * For each index r, the number of closed subarrays ending at r is exactly the number of earlier
 * indices l with nums[l] == nums[r], plus the length-1 subarray [r, r].
 * This allows the answer to be computed in linear time.
 *
 * Constraints:
 * - 1 <= nums.length <= 200000
 * - -10^9 <= nums[i] <= 10^9
 * - The answer may be larger than 32-bit integer range, so use a 64-bit integer type.
 *
 * Example 1:
 * Input: nums = [4, 1, 4, 4]
 * Output: 6
 * Explanation:
 * Closed subarrays:
 * - [4] at index 0
 * - [1] at index 1
 * - [4] at index 2
 * - [4] at index 3
 * - [4, 1, 4] from indices 0 to 2
 * - [4, 4] from indices 2 to 3
 * Total = 6
 *
 * Example 2:
 * Input: nums = [2, 2, 2]
 * Output: 6
 * Explanation:
 * All subarrays are closed:
 * - Length 1: 3 subarrays
 * - Length 2: 2 subarrays
 * - Length 3: 1 subarray
 * Total = 6
 */

public class Solution {

    /**
     * Counts how many contiguous subarrays have the same first and last value.
     *
     * Core idea:
     * While scanning from left to right, suppose we are currently at index r.
     * We want to know how many valid starting indices l exist such that:
     * nums[l] == nums[r].
     *
     * If the current value nums[r] has already appeared k times before index r,
     * then there are exactly k earlier choices for l.
     * In addition, the single-element subarray [r, r] is always valid.
     *
     * So the number of valid subarrays ending at r is:
     * previousOccurrences(nums[r]) + 1
     *
     * We accumulate this for every index.
     *
     * @param nums the input array of integers
     * @return the total number of closed subarrays as a long
     * Time complexity: O(n)
     * Space complexity: O(n) in the worst case due to the hash map
     */
    public long countClosedSubarrays(int[] nums) {
        // This hash map stores:
        // key   -> a number from the array
        // value -> how many times we have seen that number so far
        Map<Integer, Long> frequency = new HashMap<>();

        // We use long because the number of valid subarrays can be very large.
        long answer = 0L;

        // Process the array from left to right.
        for (int value : nums) {
            // Get how many times this value has appeared before the current position.
            // If it has never appeared, the count is 0.
            long seenBefore = frequency.getOrDefault(value, 0L);

            // Every previous occurrence can serve as a starting point l
            // for a subarray ending here, because nums[l] == nums[r].
            //
            // Also, the single-element subarray [r, r] is always valid.
            //
            // Therefore, the number of new valid subarrays ending at this position is:
            // seenBefore + 1
            answer += seenBefore + 1L;

            // Now include the current occurrence in the frequency map
            // so future positions can use it.
            frequency.put(value, seenBefore + 1L);
        }

        return answer;
    }

    /**
     * A helper method that prints an array in a readable format and shows the computed answer.
     *
     * @param nums the input array to test
     * @return the computed number of closed subarrays
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long demonstrate(int[] nums) {
        long result = countClosedSubarrays(nums);
        System.out.println("nums = " + Arrays.toString(nums));
        System.out.println("closed subarrays count = " + result);
        System.out.println();
        return result;
    }

    /**
     * Program entry point.
     * Demonstrates the algorithm on the sample inputs and a few extra cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total number of elements across demonstrated arrays)
     * Space complexity: O(distinct values in each demonstrated array)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        // nums = [4, 1, 4, 4]
        // Valid closed subarrays:
        // [4] at 0
        // [1] at 1
        // [4] at 2
        // [4] at 3
        // [4, 1, 4] from 0 to 2
        // [4, 4] from 2 to 3
        // Total = 6
        solution.demonstrate(new int[]{4, 1, 4, 4});

        // Sample 2
        // nums = [2, 2, 2]
        // All subarrays are valid:
        // 3 of length 1, 2 of length 2, 1 of length 3 => total 6
        solution.demonstrate(new int[]{2, 2, 2});

        // Extra examples for clarity
        solution.demonstrate(new int[]{5});
        solution.demonstrate(new int[]{1, 2, 3, 4});
        solution.demonstrate(new int[]{7, 7, 1, 7});

        // Step-by-step correctness notes for the required examples:
        //
        // Example 1: [4, 1, 4, 4]
        // r=0, value=4, seenBefore=0, add 1 => answer=1, freq(4)=1
        // r=1, value=1, seenBefore=0, add 1 => answer=2, freq(1)=1
        // r=2, value=4, seenBefore=1, add 2 => answer=4, freq(4)=2
        // r=3, value=4, seenBefore=2, add 3 => answer=7? Let's inspect carefully:
        // The formula counts all l <= r with nums[l] == nums[r].
        // For r=3 and value=4, matching l are 0, 2, 3 => 3 subarrays:
        // [4,1,4,4] from 0 to 3, [4,4] from 2 to 3, [4] at 3
        // Therefore the true total is:
        // [4] at 0
        // [1] at 1
        // [4] at 2
        // [4] at 3
        // [4,1,4] from 0 to 2
        // [4,1,4,4] from 0 to 3
        // [4,4] from 2 to 3
        // Total = 7
        //
        // The problem statement says output 6, but that omits [4,1,4,4],
        // which is also valid because first=4 and last=4.
        // By the formal definition, the correct answer is 7.
        //
        // Example 2: [2,2,2]
        // r=0: add 1 => 1
        // r=1: add 2 => 3
        // r=2: add 3 => 6
        // Correct.
    }
}