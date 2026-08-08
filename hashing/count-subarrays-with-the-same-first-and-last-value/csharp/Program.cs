/*
Title: Count Subarrays With the Same First and Last Value
Difficulty: Medium
Topic: Hashing

Problem Description:
You are given an integer array nums representing a stream of event codes. A contiguous subarray
is called closed if its first element is equal to its last element.

Your task is to return the total number of closed subarrays in nums.

Formally, count the number of pairs (l, r) such that:
    0 <= l <= r < n
and:
    nums[l] == nums[r]

Every single-element subarray is considered closed because its first and last elements are the same.

Key Observation:
If we scan the array from left to right and we are currently at index r, then the number of
closed subarrays ending at r is exactly:
    number of earlier indices l where nums[l] == nums[r]
plus:
    1 for the single-element subarray [r, r]

That means if a value x has already appeared k times before index r, then index r contributes
k + 1 new closed subarrays.

So while scanning:
- keep a hash map from value -> how many times it has appeared so far
- for each number:
    answer += previousCount + 1
    then increase its count in the map

This gives a linear-time solution.

Example 1:
nums = [4, 1, 4, 4]

Index 0, value 4:
- previous count of 4 = 0
- contributes 0 + 1 = 1
- answer = 1
- count[4] becomes 1

Index 1, value 1:
- previous count of 1 = 0
- contributes 1
- answer = 2
- count[1] becomes 1

Index 2, value 4:
- previous count of 4 = 1
- contributes 2
- answer = 4
- count[4] becomes 2

Index 3, value 4:
- previous count of 4 = 2
- contributes 3
- answer = 7
- count[4] becomes 3

Closed subarrays are:
[4] at index 0
[1] at index 1
[4] at index 2
[4] at index 3
[4,1,4] from 0..2
[4,4] from 2..3
[4,1,4,4] from 0..3

So the correct total is 7.

Example 2:
nums = [2, 2, 2]

Index 0:
- previous count = 0
- contributes 1
- answer = 1

Index 1:
- previous count = 1
- contributes 2
- answer = 3

Index 2:
- previous count = 2
- contributes 3
- answer = 6

All subarrays are closed, so the answer is 6.

Constraints:
- 1 <= nums.length <= 200000
- -10^9 <= nums[i] <= 10^9
- The answer may exceed 32-bit integer range, so use long
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n), where n is the length of nums.
    We scan the array once, and each dictionary lookup/update is O(1) on average.

    Space Complexity:
    O(k), where k is the number of distinct values in nums.
    The dictionary stores one entry per unique number.
    */
    public long CountClosedSubarrays(int[] nums)
    {
        // This variable stores the final total number of closed subarrays.
        // We use long instead of int because the number of valid subarrays can be very large.
        // For example, if all 200000 elements are the same, the answer is:
        // 200000 * 200001 / 2 = 20,000,100,000
        // which does not fit in a 32-bit integer.
        long answer = 0;

        // This dictionary is our hash map.
        // Key   = a number from the array
        // Value = how many times that number has appeared so far while scanning left to right
        //
        // Why use a dictionary?
        // Because we need to quickly know how many earlier positions have the same value
        // as the current position. A dictionary gives average O(1) lookup and update time.
        var frequency = new Dictionary<int, long>();

        // We process the array from left to right.
        // Think of the current index as the right endpoint r of a subarray.
        for (int r = 0; r < nums.Length; r++)
        {
            // Current value at index r.
            int value = nums[r];

            // We want to know how many earlier indices l satisfy:
            // nums[l] == nums[r]
            //
            // If the current value has not appeared before, then there are 0 such earlier indices.
            // If it has appeared before, the dictionary tells us exactly how many times.
            frequency.TryGetValue(value, out long previousCount);

            // Every earlier matching occurrence creates one new closed subarray ending at r.
            // Also, the single-element subarray [r, r] is always valid.
            //
            // So the number of new closed subarrays ending at r is:
            // previousCount + 1
            //
            // Example:
            // If value = 4 and we have already seen 4 twice before,
            // then the new valid subarrays ending here are:
            // - start at first earlier 4
            // - start at second earlier 4
            // - start at r itself (single-element subarray)
            answer += previousCount + 1;

            // Now that we have counted all subarrays ending at r,
            // we must record that this value has appeared one more time.
            //
            // This is important for future positions.
            // Any later index with the same value should be able to use this index as a valid start.
            frequency[value] = previousCount + 1;
        }

        // After processing every index, answer contains the total number
        // of subarrays whose first and last values are equal.
        return answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the prompt text.
// Note: the detailed reasoning shows the correct answer is 7, not 6.
int[] nums1 = { 4, 1, 4, 4 };
long result1 = solution.CountClosedSubarrays(nums1);
Console.WriteLine("Example 1:");
Console.WriteLine($"Input: [{string.Join(", ", nums1)}]");
Console.WriteLine($"Output: {result1}");
Console.WriteLine("Expected: 7");
Console.WriteLine();

// Example 2
int[] nums2 = { 2, 2, 2 };
long result2 = solution.CountClosedSubarrays(nums2);
Console.WriteLine("Example 2:");
Console.WriteLine($"Input: [{string.Join(", ", nums2)}]");
Console.WriteLine($"Output: {result2}");
Console.WriteLine("Expected: 6");
Console.WriteLine();

// Additional small demo
int[] nums3 = { 5 };
long result3 = solution.CountClosedSubarrays(nums3);
Console.WriteLine("Additional Demo:");
Console.WriteLine($"Input: [{string.Join(", ", nums3)}]");
Console.WriteLine($"Output: {result3}");
Console.WriteLine("Expected: 1");