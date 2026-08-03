/*
Title: Longest Call Streak Within Roaming Budget
Difficulty: Easy
Topic: Sliding Window

Problem Description:
You are given an array costs where costs[i] represents the roaming charge for the i-th phone call made during a trip.
A traveler wants to look at one continuous streak of calls and keep the total roaming charge of that streak within a fixed budget.
Your task is to return the length of the longest contiguous subarray whose sum is less than or equal to budget.

In other words, choose indices l and r such that 0 <= l <= r < costs.length, and the sum of costs[l] through costs[r]
does not exceed budget. Among all such valid choices, find the maximum possible number of calls in the streak.

This is an interview-style sliding window problem: because all roaming charges are non-negative, you can expand the right end
of the window and shrink the left end whenever the total exceeds the budget.

Constraints:
- 1 <= costs.length <= 100000
- 0 <= costs[i] <= 10000
- 0 <= budget <= 1000000000
- All values are integers

Example 1:
Input: costs = [4, 2, 1, 3, 2], budget = 6
Output: 3
Explanation: The longest valid streak is [2, 1, 3], which has total cost 6 and length 3.

Example 2:
Input: costs = [7, 1, 2, 1, 1], budget = 4
Output: 3
Explanation:
The subarray [1, 2, 1, 1] has total cost 5, so it is invalid.
Valid longest streaks include [1, 2, 1] and [2, 1, 1], each with total cost 4 and length 3.
Therefore the answer is 3.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the window once when the right pointer moves forward.
    - Each element is removed from the window at most once when the left pointer moves forward.
    - Because both pointers only move from left to right, the total work is linear.

    Space Complexity: O(1)
    - We only use a few extra variables: left pointer, running sum, and best length.
    - No extra arrays or collections are needed.
    */
    public int LongestCallStreakWithinBudget(int[] costs, int budget)
    {
        // "left" marks the beginning of our current sliding window.
        // The window will always represent a contiguous subarray from left to right.
        int left = 0;

        // We keep a running total of the values currently inside the window.
        // We use long for extra safety, even though int would fit under the given constraints.
        long currentSum = 0;

        // This stores the best (maximum) valid window length we have seen so far.
        int maxLength = 0;

        // Move the right boundary of the window from left to right across the array.
        // At every step, we try to include costs[right] in the current window.
        for (int right = 0; right < costs.Length; right++)
        {
            // Step 1: Expand the window by including the current call cost at index "right".
            // Why?
            // We want to consider every possible ending position of a subarray.
            // By adding costs[right], we are asking:
            // "What is the longest valid subarray that ends at this index?"
            currentSum += costs[right];

            // Step 2: If the window is too expensive, shrink it from the left.
            // Why is this valid?
            // Because all costs are non-negative.
            // That means adding more elements can only keep the sum the same or increase it.
            // So if currentSum is too large, the only way to make it valid again is to remove
            // elements from the left side of the current window.
            while (currentSum > budget)
            {
                // Remove the leftmost element from the running sum,
                // because that element is no longer part of the window.
                currentSum -= costs[left];

                // Move the left boundary one step to the right.
                // This effectively shrinks the window.
                left++;
            }

            // Step 3: At this point, the window [left..right] is guaranteed to be valid,
            // meaning its sum is <= budget.
            // So we can safely measure its length.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is longer than any previous one.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After checking all possible right endpoints, maxLength contains the answer.
        return maxLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] costs1 = { 4, 2, 1, 3, 2 };
int budget1 = 6;
int result1 = solution.LongestCallStreakWithinBudget(costs1, budget1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 3

// Example 2
int[] costs2 = { 7, 1, 2, 1, 1 };
int budget2 = 4;
int result2 = solution.LongestCallStreakWithinBudget(costs2, budget2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 3

// Additional demo cases

// Entire array fits within budget
int[] costs3 = { 1, 1, 1, 1 };
int budget3 = 10;
int result3 = solution.LongestCallStreakWithinBudget(costs3, budget3);
Console.WriteLine("Demo 3 Result: " + result3); // Expected: 4

// Budget is zero, only zero-cost calls can be included
int[] costs4 = { 0, 0, 1, 0, 0 };
int budget4 = 0;
int result4 = solution.LongestCallStreakWithinBudget(costs4, budget4);
Console.WriteLine("Demo 4 Result: " + result4); // Expected: 2

// Single element too large
int[] costs5 = { 5 };
int budget5 = 3;
int result5 = solution.LongestCallStreakWithinBudget(costs5, budget5);
Console.WriteLine("Demo 5 Result: " + result5); // Expected: 0