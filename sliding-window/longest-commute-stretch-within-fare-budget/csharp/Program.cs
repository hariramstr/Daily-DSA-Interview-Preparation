/*
Title: Longest Commute Stretch Within Fare Budget
Difficulty: Easy
Topic: Sliding Window

Problem Description:
You are given an array fares where fares[i] is the transit fare paid on the i-th ride of a commuter's travel history, and an integer budget. A commute stretch is any contiguous group of rides. Your task is to find the maximum number of consecutive rides whose total fare is less than or equal to budget.

In other words, among all subarrays of fares, return the length of the longest one whose sum does not exceed budget.

This models a common analytics problem: given a daily or weekly ride log, determine the longest uninterrupted sequence of rides that could have been covered by a fixed reimbursement limit.

Return 0 if no single ride can fit within the budget.

Constraints:
- 1 <= fares.length <= 100000
- 1 <= fares[i] <= 10000
- 1 <= budget <= 1000000000
- All fares are positive integers

Because all fare values are positive, a sliding window solution can efficiently expand and shrink a window while tracking the running sum.

Example 1:
Input: fares = [2, 1, 3, 2, 1], budget = 5
Output: 2
Explanation: Valid stretches include [2,1], [3,2], and [2,1]. Any stretch of length 3 has total fare greater than 5, so the answer is 2.

Example 2:
Input: fares = [4, 2, 1, 1, 3], budget = 6
Output: 3
Explanation: The stretch [2,1,1] has total fare 4 and length 3. Another valid stretch is [1,1,3] with total fare 5 and length 3. No valid stretch of length 4 fits within the budget.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each fare is added to the running sum once when the right pointer moves forward.
    - Each fare is removed from the running sum at most once when the left pointer moves forward.
    - Because both pointers only move from left to right across the array, the total work is linear.

    Space Complexity: O(1)
    - We only use a few extra variables: left pointer, running sum, and best length.
    - No extra arrays or data structures are needed.
    */
    public int LongestCommuteStretchWithinBudget(int[] fares, int budget)
    {
        // This pointer marks the beginning of our current window (current contiguous stretch of rides).
        // We will move it to the right whenever the current window becomes too expensive.
        int left = 0;

        // This stores the total fare of the current window from index left to index right.
        // We use long for extra safety, even though the given constraints fit in int.
        long currentSum = 0;

        // This keeps track of the longest valid window length we have seen so far.
        int maxLength = 0;

        // We expand the window one ride at a time by moving the right pointer from left to right.
        for (int right = 0; right < fares.Length; right++)
        {
            // Step 1: Include the new ride at index "right" into the current window.
            // Why?
            // We are trying to explore all possible contiguous stretches.
            // Expanding to the right lets us consider longer stretches.
            currentSum += fares[right];

            // Step 2: If the total fare is now above the budget, the window is invalid.
            // Because all fares are positive integers, adding more rides can only increase the sum.
            // So the only way to make the window valid again is to remove rides from the left side.
            while (currentSum > budget && left <= right)
            {
                // Remove the fare at the left edge from the running total.
                // Why?
                // This shrinks the window and reduces the total fare.
                currentSum -= fares[left];

                // Move the left edge one step to the right.
                // This means the old leftmost ride is no longer part of the current window.
                left++;
            }

            // Step 3: At this point, the window [left..right] is guaranteed to have sum <= budget,
            // or it may be empty in edge cases after shrinking.
            // So this is a valid commute stretch, and we can measure its length.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is longer than any previous one.
            // Why?
            // The problem asks for the maximum length among all valid contiguous stretches.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // If no ride could fit within the budget, maxLength will remain 0.
        // Otherwise, it will contain the length of the longest valid stretch.
        return maxLength;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] fares1 = { 2, 1, 3, 2, 1 };
int budget1 = 5;
int result1 = solution.LongestCommuteStretchWithinBudget(fares1, budget1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 2

// Example 2
int[] fares2 = { 4, 2, 1, 1, 3 };
int budget2 = 6;
int result2 = solution.LongestCommuteStretchWithinBudget(fares2, budget2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 3

// Additional demo: no single ride fits within budget
int[] fares3 = { 7, 8, 9 };
int budget3 = 5;
int result3 = solution.LongestCommuteStretchWithinBudget(fares3, budget3);
Console.WriteLine($"Additional Example Result: {result3}"); // Expected: 0