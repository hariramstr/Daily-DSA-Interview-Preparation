/*
Title: Longest Focus Session Under Noise Budget
Difficulty: Easy
Topic: Sliding Window

Problem Description:
You are given an array noise where noise[i] represents the noise level recorded during the i-th minute of a student's study session. The student wants to choose one contiguous block of minutes to study without taking a break. However, the total noise during that chosen block must not exceed a given integer budget.

Your task is to return the maximum number of consecutive minutes the student can study such that the sum of the noise levels in that contiguous block is less than or equal to budget.

This is a practical scheduling problem: the student can tolerate some background noise, but only up to a fixed limit. You need to find the longest valid time window.

Return 0 if no single minute can be included without exceeding the budget.

Constraints:
- 1 <= noise.length <= 100000
- 0 <= noise[i] <= 10000
- 0 <= budget <= 1000000000
- The solution should run efficiently for large inputs.

Example 1:
Input: noise = [2, 1, 3, 2, 1], budget = 5
Output: 2
Explanation:
The longest valid contiguous block has length 2.
Examples:
- [2,1] has sum 3, valid
- [1,3] has sum 4, valid
- [3,2] has sum 5, valid
- [2,1] at the end has sum 3, valid
Any length 3 window exceeds the budget:
- [2,1,3] = 6
- [1,3,2] = 6
- [3,2,1] = 6
So the answer is 2.

Example 2:
Input: noise = [0, 2, 0, 1, 1, 0], budget = 3
Output: 4
Explanation:
Check some contiguous windows:
- [0,2,0,1] has sum 3, valid, length 4
- [2,0,1,1] has sum 4, invalid
- [0,1,1,0] has sum 2, valid, length 4
No valid contiguous window of length 5 exists within budget.
So the answer is 4.

Goal:
Compute the maximum length of a contiguous subarray whose sum is at most budget.

Why sliding window works:
Because all noise values are non-negative, when we expand the window to the right,
the sum never decreases. If the sum becomes too large, we can safely move the left
side forward until the sum is valid again. This property makes the sliding window
technique both correct and efficient.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the window once when the right pointer moves.
    - Each element is removed from the window at most once when the left pointer moves.
    - Therefore, the total amount of work is linear in the size of the array.

    Space Complexity: O(1)
    - We only use a few extra variables: pointers, running sum, and answer.
    - No extra arrays or data structures proportional to input size are needed.
    */
    public int LongestFocusSession(int[] noise, int budget)
    {
        // The left pointer marks the beginning of the current window.
        // The right pointer will be controlled by the for-loop and marks the end of the current window.
        int left = 0;

        // We keep a running sum of the current window [left..right].
        // We use long instead of int for extra safety, even though the constraints fit in int.
        // This is a common defensive programming habit when summing many values.
        long currentSum = 0;

        // This variable stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // We expand the window one element at a time by moving "right" from left to right.
        for (int right = 0; right < noise.Length; right++)
        {
            // STEP 1: Include the new element at index "right" into the current window.
            // Why?
            // We are trying to consider every possible window that ends at "right".
            // By adding noise[right], the window becomes [left..right].
            currentSum += noise[right];

            // STEP 2: If the window is invalid (sum > budget), shrink it from the left.
            // Why?
            // The problem requires the total noise to be <= budget.
            // Since all values are non-negative, adding more elements can only keep the sum the same or increase it.
            // Therefore, once the sum is too large, the only way to fix it is to remove elements from the left side.
            while (currentSum > budget && left <= right)
            {
                // Remove the leftmost element from the running sum,
                // because we are about to move the left boundary one step to the right.
                currentSum -= noise[left];

                // Move the left boundary rightward, making the window smaller.
                left++;
            }

            // STEP 3: At this point, the window [left..right] is guaranteed to be valid.
            // Why?
            // The while-loop above stops only when currentSum <= budget.
            // So now we can safely measure its length.
            int currentLength = right - left + 1;

            // STEP 4: Update the best answer if this valid window is longer than any previous one.
            // Why?
            // The problem asks for the maximum length among all valid contiguous windows.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // If no single element fits within the budget, maxLength will remain 0.
        // Otherwise, it will contain the length of the longest valid window.
        return maxLength;
    }
}

// Demo code:
// Creates sample inputs, calls the solution, and prints the results.

var solution = new Solution();

// Example 1
int[] noise1 = { 2, 1, 3, 2, 1 };
int budget1 = 5;
int result1 = solution.LongestFocusSession(noise1, budget1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 2

// Example 2
int[] noise2 = { 0, 2, 0, 1, 1, 0 };
int budget2 = 3;
int result2 = solution.LongestFocusSession(noise2, budget2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 4

// Additional demo: no minute can be included
int[] noise3 = { 6, 7, 8 };
int budget3 = 5;
int result3 = solution.LongestFocusSession(noise3, budget3);
Console.WriteLine("Example 3 Result: " + result3); // Expected: 0

// Additional demo: all zeros
int[] noise4 = { 0, 0, 0, 0 };
int budget4 = 0;
int result4 = solution.LongestFocusSession(noise4, budget4);
Console.WriteLine("Example 4 Result: " + result4); // Expected: 4