/*
Title: Maximum Score from One Interior Buffer Removal
Difficulty: Medium
Topic: Arrays

Problem Description:
You are given an integer array nums representing the value of blocks in a processing pipeline.
You may remove exactly one contiguous subarray that is strictly inside the array, meaning the
removed segment cannot include the first or the last element. After the removal, the remaining
left part and right part are concatenated. The score of the final array is the sum of its elements.

Your task is to return the maximum possible score after removing one valid interior subarray.
Since removing a subarray decreases the total sum, the goal is equivalent to removing an interior
subarray with the minimum possible sum.

A valid removed subarray must satisfy:
1 <= l <= r <= n - 2
using 0-based indexing, where nums[l..r] is removed and both nums[0] and nums[n-1] remain
in the final array.

Write a function that returns the maximum score obtainable.

Constraints:
- 3 <= nums.length <= 200000
- -1000000000 <= nums[i] <= 1000000000
- The answer fits in a signed 64-bit integer

Example 1:
Input: nums = [5, -2, 3, -4, 6]
Output: 12

Reasoning:
Total sum = 5 + (-2) + 3 + (-4) + 6 = 8

All valid removed subarrays must lie inside indices 1..3:
- [-2] => removed sum = -2, remaining score = 8 - (-2) = 10
- [3] => removed sum = 3, remaining score = 8 - 3 = 5
- [-4] => removed sum = -4, remaining score = 8 - (-4) = 12
- [-2, 3] => removed sum = 1, remaining score = 7
- [3, -4] => removed sum = -1, remaining score = 9
- [-2, 3, -4] => removed sum = -3, remaining score = 11

Minimum removable interior subarray sum is -4, so the maximum final score is 12.

Example 2:
Input: nums = [4, 7, 2, 9]
Output: 20

Reasoning:
Total sum = 22
Valid interior subarrays are inside indices 1..2:
- [7] => remaining score = 15
- [2] => remaining score = 20
- [7, 2] => remaining score = 13

Minimum removable interior subarray sum is 2, so answer = 22 - 2 = 20.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    We do one pass to compute the total sum, and one pass over the interior portion
    of the array to find the minimum-sum contiguous subarray using a Kadane-style approach.
    */
    public long MaximumScoreAfterOneInteriorRemoval(int[] nums)
    {
        // Step 1:
        // Compute the total sum of the entire array.
        //
        // Why this is necessary:
        // The final score after removing a subarray is:
        //     totalSum - removedSubarraySum
        //
        // So if we want to maximize the final score, we should minimize the sum of the
        // removed interior subarray.
        //
        // We use long because:
        // - nums[i] can be as large as 1e9 in magnitude
        // - n can be up to 2e5
        // - the total can exceed the 32-bit int range
        long totalSum = 0;
        foreach (int value in nums)
        {
            totalSum += value;
        }

        // Step 2:
        // We must remove exactly one contiguous subarray fully inside the array.
        // That means the removable region is only indices [1 .. n-2].
        //
        // So the problem becomes:
        // "Find the minimum-sum contiguous subarray inside nums[1..n-2]."
        //
        // This is a classic variation of Kadane's algorithm.
        // Standard Kadane finds a maximum-sum subarray.
        // Here, we adapt it to find a minimum-sum subarray.
        //
        // Definitions:
        // currentMinEndingHere = minimum sum of a valid subarray that MUST end at the current index
        // bestMinSoFar        = minimum sum of ANY valid subarray seen so far in the interior range
        //
        // Initialization:
        // Since the interior range always has at least one element (because n >= 3),
        // index 1 is guaranteed to exist and is a valid starting point.
        long currentMinEndingHere = nums[1];
        long bestMinSoFar = nums[1];

        // Step 3:
        // Process each interior element from index 2 to index n-2.
        //
        // At each position i, we decide:
        // - Should the minimum-sum subarray ending at i start fresh at nums[i]?
        // - Or should it extend the previous minimum-sum subarray ending at i-1?
        //
        // Recurrence:
        // currentMinEndingHere = min(nums[i], currentMinEndingHere + nums[i])
        //
        // Why this works:
        // Any minimum-sum subarray ending at i has only two possibilities:
        // 1. It consists of just nums[i]
        // 2. It extends the best minimum-sum subarray ending at i-1
        //
        // Then we update the global best:
        // bestMinSoFar = min(bestMinSoFar, currentMinEndingHere)
        for (int i = 2; i <= nums.Length - 2; i++)
        {
            long value = nums[i];

            // Decide whether to start a new subarray at i,
            // or extend the previous one.
            currentMinEndingHere = Math.Min(value, currentMinEndingHere + value);

            // Record the best (smallest) interior subarray sum seen so far.
            bestMinSoFar = Math.Min(bestMinSoFar, currentMinEndingHere);
        }

        // Step 4:
        // The best answer is total sum minus the minimum removable interior subarray sum.
        //
        // Important intuition:
        // - If the minimum interior subarray sum is negative, removing it increases the final score.
        // - If all interior subarrays are positive, we still must remove exactly one,
        //   so we remove the smallest positive one.
        // - If zero exists as the minimum, removing it keeps the score unchanged.
        long answer = totalSum - bestMinSoFar;

        return answer;
    }
}

// Demo code

var solution = new Solution();

int[] nums1 = { 5, -2, 3, -4, 6 };
long result1 = solution.MaximumScoreAfterOneInteriorRemoval(nums1);
Console.WriteLine(result1); // Expected: 12

int[] nums2 = { 4, 7, 2, 9 };
long result2 = solution.MaximumScoreAfterOneInteriorRemoval(nums2);
Console.WriteLine(result2); // Expected: 20

int[] nums3 = { 1, 2, 3 };
long result3 = solution.MaximumScoreAfterOneInteriorRemoval(nums3);
Console.WriteLine(result3); // Only removable subarray is [2], expected: 4

int[] nums4 = { 10, -5, -6, 7 };
long result4 = solution.MaximumScoreAfterOneInteriorRemoval(nums4);
Console.WriteLine(result4); // Remove [-5, -6], total 6 => answer 17

int[] nums5 = { 8, 1, 2, 3, 4, 9 };
long result5 = solution.MaximumScoreAfterOneInteriorRemoval(nums5);
Console.WriteLine(result5); // All interior sums positive, remove [1], expected: 26