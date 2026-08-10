/*
Title: Maximum Consecutive Days Within a Sleep Debt Budget
Difficulty: Medium
Topic: Arrays

Problem Description:
You are given an integer array sleepHours where sleepHours[i] represents how many hours a person slept on day i,
and an integer target representing the recommended number of sleep hours per day.

For any day, the sleep debt for that day is:
    max(0, target - sleepHours[i])

That means:
- If the person slept at least target hours, that day adds 0 debt.
- If the person slept less than target hours, that day adds the shortage as debt.

Your task is to find the length of the longest contiguous block of days whose total accumulated sleep debt
is at most budget.

Formally, for a subarray sleepHours[l..r], define its total debt as:
    sum of max(0, target - sleepHours[i]) for all i in [l, r]

Return the maximum possible value of:
    r - l + 1
such that the total debt is less than or equal to budget.

Important note:
Extra sleep on one day does NOT cancel debt from another day.
A day can only contribute either:
- 0 debt, or
- positive debt equal to the shortage

Constraints:
- 1 <= sleepHours.length <= 100000
- 0 <= sleepHours[i] <= 24
- 1 <= target <= 24
- 0 <= budget <= 1000000000

Example 1:
Input: sleepHours = [7, 5, 8, 4, 6, 7], target = 7, budget = 3
Debt array: [0, 2, 0, 3, 1, 0]
Valid longest length is 3.

Example 2:
Input: sleepHours = [6, 6, 7, 7, 5, 8, 6], target = 7, budget = 2
Debt array: [1, 1, 0, 0, 2, 0, 1]
Valid longest length is 4.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Why?
    - We use a sliding window with two pointers.
    - Each element enters the window once when the right pointer moves forward.
    - Each element leaves the window at most once when the left pointer moves forward.
    - Therefore, the total work is linear in the size of the array.
    - We do not build any extra array; we compute each day's debt on the fly.
    */
    public int LongestConsecutiveDaysWithinBudget(int[] sleepHours, int target, int budget)
    {
        // This pointer marks the beginning of the current sliding window.
        // The window will always represent a contiguous block of days.
        int left = 0;

        // This variable stores the total sleep debt inside the current window [left..right].
        // We use long for extra safety, even though int would also be enough under the given constraints.
        long currentDebt = 0;

        // This will store the best (maximum) valid window length found so far.
        int maxLength = 0;

        // We expand the window one day at a time by moving the right pointer from left to right.
        for (int right = 0; right < sleepHours.Length; right++)
        {
            // Compute the debt contributed by the new day at index 'right'.
            // If sleepHours[right] is at least target, debt is 0.
            // Otherwise, debt is the shortage: target - sleepHours[right].
            int debtForRightDay = Math.Max(0, target - sleepHours[right]);

            // Add this new day's debt to the running total for the current window.
            currentDebt += debtForRightDay;

            // At this point, the window [left..right] may have become invalid
            // if its total debt is now greater than the allowed budget.
            //
            // Because all daily debts are non-negative, the standard sliding window technique works:
            // - Expanding the window can only keep the debt the same or increase it.
            // - Shrinking the window from the left can only keep the debt the same or decrease it.
            //
            // So while the window is invalid, we keep moving 'left' forward
            // until the total debt is back within the budget.
            while (currentDebt > budget)
            {
                // Before removing the leftmost day from the window,
                // compute how much debt that day contributed.
                int debtForLeftDay = Math.Max(0, target - sleepHours[left]);

                // Remove that day's debt from the current window total,
                // because we are about to exclude it from the window.
                currentDebt -= debtForLeftDay;

                // Move the left boundary one step to the right,
                // effectively shrinking the window.
                left++;
            }

            // Now the window [left..right] is guaranteed to be valid:
            // its total debt is <= budget.
            //
            // So we can safely measure its length and compare it with the best answer so far.
            int currentLength = right - left + 1;

            // Update the maximum length if this valid window is longer.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After scanning all possible right endpoints,
        // maxLength contains the longest valid contiguous block of days.
        return maxLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] sleepHours1 = { 7, 5, 8, 4, 6, 7 };
int target1 = 7;
int budget1 = 3;
int result1 = solution.LongestConsecutiveDaysWithinBudget(sleepHours1, target1, budget1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 3

// Example 2
int[] sleepHours2 = { 6, 6, 7, 7, 5, 8, 6 };
int target2 = 7;
int budget2 = 2;
int result2 = solution.LongestConsecutiveDaysWithinBudget(sleepHours2, target2, budget2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 4

// Additional quick sanity checks
int[] sleepHours3 = { 8, 8, 8, 8 };
int target3 = 7;
int budget3 = 0;
int result3 = solution.LongestConsecutiveDaysWithinBudget(sleepHours3, target3, budget3);
Console.WriteLine("All days have zero debt: " + result3); // Expected: 4

int[] sleepHours4 = { 0, 0, 0 };
int target4 = 7;
int budget4 = 6;
int result4 = solution.LongestConsecutiveDaysWithinBudget(sleepHours4, target4, budget4);
Console.WriteLine("Very small budget: " + result4); // Expected: 0

int[] sleepHours5 = { 6, 7, 6, 7, 6 };
int target5 = 7;
int budget5 = 2;
int result5 = solution.LongestConsecutiveDaysWithinBudget(sleepHours5, target5, budget5);
Console.WriteLine("Alternating debt pattern: " + result5); // Expected: 4