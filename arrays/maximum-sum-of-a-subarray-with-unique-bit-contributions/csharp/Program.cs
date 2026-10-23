/*
Title: Maximum Sum of a Subarray With Unique Bit Contributions
Difficulty: Hard
Topic: Arrays

Problem Description:
You are given an integer array nums of length n, where each value represents a feature mask stored as a non-negative integer.
A contiguous subarray is called clean if no bit position is set in more than one element inside that subarray.
In other words, for every pair of different indices i and j in the chosen subarray, nums[i] & nums[j] must be 0.

Your task is to return the maximum possible sum of values over all clean contiguous subarrays.

This is not the same as requiring all numbers to be distinct. Two equal numbers may both appear only if their bitwise AND is 0,
which is impossible unless both are 0. Likewise, different numbers may still conflict if they share any set bit.
You must find the best contiguous range under this bit-overlap rule.

Return the maximum sum of any clean subarray.

Constraints:
- 1 <= nums.length <= 2 * 10^5
- 0 <= nums[i] <= 10^9
- The answer fits in a 64-bit signed integer.

Example 1:
Input: nums = [5,2,8,1]
Output: 11
Explanation: Binary forms are 0101, 0010, 1000, 0001. The subarray [2,8,1] is clean because no bit is shared between any two elements,
and its sum is 11. The full array is not clean because 5 and 1 both use the lowest bit.

Example 2:
Input: nums = [3,8,48,10]
Output: 59
Explanation: The entire array is not clean because 8 and 10 share bit 3.
The best clean subarray is [3,8,48] with sum 59. Here 3 uses bits 0 and 1, 8 uses bit 3, and 48 uses bits 4 and 5,
so there is no overlap.

Key idea:
Use a sliding window.
Because the window must stay "clean", each bit can appear at most once inside the current window.
We maintain:
- a left pointer
- a right pointer
- a bitmask representing the OR of all numbers currently in the window
- the current sum of the window

When adding nums[right]:
- If it shares any bit with the current window mask, the window becomes invalid.
- So we repeatedly remove numbers from the left until there is no overlap.
- Then we add nums[right].

Why XOR works when removing:
Inside a valid clean window, no bit is shared by two different elements.
So every set bit in an element belongs uniquely to that element in the window.
That means removing an element can safely be done with:
    mask ^= nums[left]
because those bits are not present in any other element currently in the window.

This gives an O(n) solution because each element enters and leaves the window at most once.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the window once and removed from the window at most once.
    - The while-loop across the whole run still performs at most n removals total.

    Space Complexity: O(1)
    - We only store a few variables: pointers, a running sum, and a bitmask.
    - No extra data structure proportional to input size is used.
    */
    public long MaximumCleanSubarraySum(int[] nums)
    {
        // This variable stores the left boundary of our sliding window.
        // The current window will always be nums[left..right].
        int left = 0;

        // This variable stores the bitwise OR of all numbers currently inside the window.
        // Because the window is always maintained as "clean",
        // no bit is present in more than one number in the window.
        //
        // That property is extremely important:
        // - It lets us detect conflicts quickly using (windowMask & nums[right]) != 0
        // - It lets us remove numbers using XOR safely
        int windowMask = 0;

        // This stores the sum of the current window.
        // We use long because the total sum can exceed int range.
        long currentSum = 0;

        // This stores the best answer found so far.
        long bestSum = 0;

        // Expand the window one element at a time using the right pointer.
        for (int right = 0; right < nums.Length; right++)
        {
            int valueToAdd = nums[right];

            // Before we add nums[right], we must ensure the window remains clean.
            //
            // Conflict test:
            // If windowMask & valueToAdd is non-zero, then at least one bit is already used
            // by some number inside the current window.
            // That means adding valueToAdd right now would violate the rule.
            //
            // So we shrink the window from the left until the conflict disappears.
            while ((windowMask & valueToAdd) != 0)
            {
                int valueToRemove = nums[left];

                // Remove nums[left] from the running sum because it is leaving the window.
                currentSum -= valueToRemove;

                // Remove nums[left]'s bits from the window mask.
                //
                // Why XOR is correct here:
                // In a clean window, each set bit belongs to exactly one element.
                // Therefore, toggling those bits off with XOR removes them correctly.
                //
                // Example:
                // windowMask = bits from [2,8] = 0010 | 1000 = 1010
                // remove 2 (0010):
                // 1010 ^ 0010 = 1000
                windowMask ^= valueToRemove;

                // Move the left boundary rightward because that element is no longer in the window.
                left++;
            }

            // At this point, there is no overlap between valueToAdd and the current window.
            // So adding it keeps the window clean.

            // Add the new value into the running sum.
            currentSum += valueToAdd;

            // Add its bits into the window mask.
            // Since there is no overlap, OR is safe and natural here.
            windowMask |= valueToAdd;

            // Update the best answer.
            if (currentSum > bestSum)
            {
                bestSum = currentSum;
            }
        }

        return bestSum;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] nums1 = { 5, 2, 8, 1 };
long result1 = solution.MaximumCleanSubarraySum(nums1);
Console.WriteLine(result1); // Expected: 11

// Example 2
int[] nums2 = { 3, 8, 48, 10 };
long result2 = solution.MaximumCleanSubarraySum(nums2);
Console.WriteLine(result2); // Expected: 59

// Additional quick checks
int[] nums3 = { 0, 0, 0 };
Console.WriteLine(solution.MaximumCleanSubarraySum(nums3)); // Expected: 0

int[] nums4 = { 1, 2, 4, 8 };
Console.WriteLine(solution.MaximumCleanSubarraySum(nums4)); // Expected: 15

int[] nums5 = { 7, 7, 7 };
Console.WriteLine(solution.MaximumCleanSubarraySum(nums5)); // Expected: 7