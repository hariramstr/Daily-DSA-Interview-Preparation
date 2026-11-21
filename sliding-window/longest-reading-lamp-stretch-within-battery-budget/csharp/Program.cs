/*
Title: Longest Reading Lamp Stretch Within Battery Budget
Difficulty: Easy
Topic: Sliding Window

Problem Description:
A student uses a rechargeable reading lamp while studying late at night. You are given an integer array batteryUse where batteryUse[i] is the number of battery units consumed during minute i, and an integer budget representing the maximum total battery units the lamp can spend before it must be recharged.

Your task is to find the length of the longest contiguous block of minutes during which the total battery consumption is less than or equal to budget.

In other words, return the maximum size of a subarray whose sum does not exceed budget.

This problem is meant to be solved efficiently using a sliding window. Since all battery usage values are non-negative, once a window exceeds the budget, moving the left side forward is enough to restore validity.

Constraints:
- 1 <= batteryUse.length <= 100000
- 0 <= batteryUse[i] <= 10000
- 0 <= budget <= 1000000000

Example 1:
Input: batteryUse = [2, 1, 3, 2, 1], budget = 5
Output: 2
Explanation: Valid windows include [2,1], [3,2], and [2,1]. Any window of length 3 has sum greater than 5, so the answer is 2.

Example 2:
Input: batteryUse = [0, 1, 1, 0, 2, 1], budget = 3
Output: 4
Explanation: The subarray [0,1,1,0] has total consumption 2, which is within the budget. No contiguous subarray of length 5 or more stays within 3, so the answer is 4.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Why?
    - Each element is added to the window once when the right pointer moves forward.
    - Each element is removed from the window at most once when the left pointer moves forward.
    - Therefore, the total amount of work is linear in the size of the array.
    - We only use a few integer/long variables, so extra space is constant.
    */
    public int LongestReadingLampStretch(int[] batteryUse, int budget)
    {
        // This pointer marks the left boundary of our current sliding window.
        // The window always represents a contiguous subarray from left to right.
        int left = 0;

        // This variable stores the sum of all values currently inside the window.
        // We use long instead of int to be extra safe when summing many values,
        // even though the given constraints would still fit in int.
        long currentSum = 0;

        // This will store the best (maximum) valid window length found so far.
        int maxLength = 0;

        // We expand the window one element at a time by moving the right pointer.
        // For every position "right", we include batteryUse[right] into the window.
        for (int right = 0; right < batteryUse.Length; right++)
        {
            // Step 1: Add the new rightmost element into the current window sum.
            // This means our window now covers indices [left..right].
            currentSum += batteryUse[right];

            // Step 2: If the window sum is now too large, it is invalid.
            // Because all numbers are non-negative, adding more elements can never reduce the sum.
            // So the only way to make the window valid again is to move the left side forward.
            while (currentSum > budget)
            {
                // Remove the element at the current left boundary from the sum,
                // because we are about to exclude it from the window.
                currentSum -= batteryUse[left];

                // Move the left boundary one step to the right.
                // This shrinks the window and helps reduce the sum.
                left++;
            }

            // Step 3: At this point, the window [left..right] is guaranteed to be valid,
            // meaning its sum is <= budget.
            // So we compute its length.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is longer
            // than any valid window we have seen before.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After checking all possible right boundaries, maxLength contains
        // the length of the longest contiguous subarray with sum <= budget.
        return maxLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] batteryUse1 = { 2, 1, 3, 2, 1 };
int budget1 = 5;
int result1 = solution.LongestReadingLampStretch(batteryUse1, budget1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2
int[] batteryUse2 = { 0, 1, 1, 0, 2, 1 };
int budget2 = 3;
int result2 = solution.LongestReadingLampStretch(batteryUse2, budget2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional quick demo
int[] batteryUse3 = { 1, 2, 1, 1, 1 };
int budget3 = 4;
int result3 = solution.LongestReadingLampStretch(batteryUse3, budget3);
Console.WriteLine($"Additional Demo Result: {result3}");