/*
Title: Longest Quiet Study Stretch

Problem Description:
A university library records the noise level of each minute during the day as an integer array `noise`,
where `noise[i]` is the noise level at minute `i`.

A student wants to find the longest continuous time period they can study such that the total noise
during that period does not exceed a given limit `maxNoise`.

Your task is to return the length of the longest contiguous subarray whose sum is less than or equal to `maxNoise`.

This is a sliding window problem because all noise values are non-negative. That means:
- Expanding the window to the right can only keep the sum the same or increase it.
- If the sum becomes too large, we can safely move the left side forward until the sum is valid again.

Return `0` if no single minute can be included without exceeding the limit.

Constraints:
- 1 <= noise.length <= 100000
- 0 <= noise[i] <= 10000
- 0 <= maxNoise <= 1000000000

Examples:
1) noise = [2, 1, 3, 2, 1, 1], maxNoise = 5
   Output: 3

2) noise = [6, 2, 1], maxNoise = 5
   Output: 2
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the window once by moving the right pointer.
    - Each element is removed from the window at most once by moving the left pointer.
    - Therefore, the total work is linear in the size of the array.

    Space Complexity: O(1)
    - We only use a few variables for pointers, the running sum, and the answer.
    - No extra data structures that grow with input size are needed.
    */
    public int LongestQuietStudyStretch(int[] noise, int maxNoise)
    {
        // `left` marks the beginning of the current sliding window.
        // The window will always represent a contiguous subarray from `left` to `right`.
        int left = 0;

        // `bestLength` stores the longest valid window length we have found so far.
        // We start at 0 because it is possible that no valid minute exists,
        // for example if every single value is greater than maxNoise.
        int bestLength = 0;

        // `currentSum` stores the total noise inside the current window.
        // We use `long` instead of `int` as a safe habit when summing many values,
        // even though the given constraints would still fit in int.
        long currentSum = 0;

        // We move `right` from left to right across the array.
        // At each step, we try to include noise[right] in the current window.
        for (int right = 0; right < noise.Length; right++)
        {
            // Step 1: Expand the window by including the new rightmost element.
            // This means our current window is now from `left` to `right`.
            currentSum += noise[right];

            // Step 2: If the window is invalid (sum too large), shrink it from the left.
            // Why is this correct?
            // Because all numbers are non-negative.
            // That means adding more elements to the right can never reduce the sum.
            // So once the sum is too large, the only way to make it valid again
            // is to remove elements from the left side.
            while (currentSum > maxNoise && left <= right)
            {
                // Remove the leftmost element from the running sum,
                // because we are about to move the left boundary forward.
                currentSum -= noise[left];

                // Move the left boundary one step to the right.
                left++;
            }

            // Step 3: At this point, the window is guaranteed to be valid:
            // currentSum <= maxNoise
            //
            // So we can compute its length and compare it with the best answer seen so far.
            int currentLength = right - left + 1;

            // Update the best answer if this valid window is longer.
            if (currentLength > bestLength)
            {
                bestLength = currentLength;
            }
        }

        // After scanning the entire array, `bestLength` is the answer.
        return bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] noise1 = { 2, 1, 3, 2, 1, 1 };
int maxNoise1 = 5;
int result1 = solution.LongestQuietStudyStretch(noise1, maxNoise1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 3

// Example 2
int[] noise2 = { 6, 2, 1 };
int maxNoise2 = 5;
int result2 = solution.LongestQuietStudyStretch(noise2, maxNoise2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 2

// Additional demo: no valid single minute
int[] noise3 = { 7, 8, 9 };
int maxNoise3 = 5;
int result3 = solution.LongestQuietStudyStretch(noise3, maxNoise3);
Console.WriteLine($"Example 3 Result: {result3}"); // Expected: 0

// Additional demo: all zeros
int[] noise4 = { 0, 0, 0, 0 };
int maxNoise4 = 0;
int result4 = solution.LongestQuietStudyStretch(noise4, maxNoise4);
Console.WriteLine($"Example 4 Result: {result4}"); // Expected: 4