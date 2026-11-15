/*
Title: Longest Sensor Window With Coverage Gaps Budget
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are given a strictly increasing array `times` of length `n`, where `times[i]` is the timestamp (in seconds) of the `i`-th sensor report from a remote device. A monitoring dashboard wants to display the longest contiguous block of reports that can be considered a "continuous observation window". However, the device may occasionally go silent between two consecutive reports.

For any contiguous subarray `times[l..r]`, define its coverage gap cost as the sum of all missing seconds between adjacent reports inside the window:

cost(l, r) = sum(max(0, times[i] - times[i-1] - 1)) for all i from l+1 to r.

In other words, if two adjacent reports happen at seconds 10 and 13, then seconds 11 and 12 are missing, contributing 2 to the cost. You are given an integer `budget`, representing the maximum total number of missing seconds that may be tolerated inside the chosen window.

Return the maximum number of reports in any contiguous window whose coverage gap cost is at most `budget`.

A window containing only one report has cost 0.

Constraints:
- 1 <= n <= 200000
- 0 <= times[i] <= 10^18
- times is strictly increasing
- 0 <= budget <= 10^18

Example 1:
Input: times = [2, 3, 7, 8, 10], budget = 3
Output: 4
Explanation: The window [2, 3, 7, 8] has gaps (0 + 3 + 0) = 3, so its cost fits the budget and its length is 4. The full window [2, 3, 7, 8, 10] has cost 0 + 3 + 0 + 1 = 4, which exceeds the budget.

Example 2:
Input: times = [1, 5, 6, 9, 15, 16], budget = 5
Output: 3
Explanation: Window [1, 5, 6] has cost 3 + 0 = 3, and window [5, 6, 9] has cost 0 + 2 = 2. Any valid window of length 4 or more exceeds the allowed total missing seconds. An O(n) or O(n log n) solution is expected.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Why O(n)?
    - We use the classic sliding window / two-pointer technique.
    - The right pointer moves from left to right exactly once.
    - The left pointer also only moves from left to right, never backward.
    - Therefore, each element is processed a constant number of times overall.

    Why O(1) extra space?
    - We only store a few variables: left pointer, current cost, and best answer.
    - We do not build any extra arrays proportional to n.
    */
    public int LongestSensorWindow(long[] times, long budget)
    {
        // If the array is empty, there are no reports.
        // The problem constraints say n >= 1, but this guard makes the method safer.
        if (times == null || times.Length == 0)
        {
            return 0;
        }

        // "left" is the starting index of our current sliding window.
        int left = 0;

        // "currentCost" stores the total coverage gap cost of the current window times[left..right].
        //
        // Important detail:
        // For a window [left..right], the cost is the sum of missing seconds between every pair
        // of adjacent reports inside that window.
        //
        // Example:
        // times = [2, 3, 7, 8]
        // gaps are:
        // between 2 and 3 -> 3 - 2 - 1 = 0
        // between 3 and 7 -> 7 - 3 - 1 = 3
        // between 7 and 8 -> 8 - 7 - 1 = 0
        // total = 3
        long currentCost = 0;

        // "best" stores the maximum valid window length found so far.
        int best = 1;

        // We expand the window by moving "right" from 0 to n - 1.
        for (int right = 0; right < times.Length; right++)
        {
            // STEP 1: Add the new adjacent gap introduced by including times[right].
            //
            // Why only one new gap?
            // Because when we extend a window from [left..right-1] to [left..right],
            // the only newly added adjacency is between times[right-1] and times[right].
            //
            // If right == 0, the window has only one element, so there is no adjacent pair yet.
            if (right > 0)
            {
                // Since times is strictly increasing, times[right] - times[right - 1] >= 1.
                // Missing seconds between them = difference - 1.
                long addedGap = times[right] - times[right - 1] - 1;
                currentCost += addedGap;
            }

            // STEP 2: If the window cost exceeds the budget, shrink from the left.
            //
            // Why do we shrink?
            // Because we need the window to satisfy:
            // currentCost <= budget
            //
            // Since all gaps are non-negative, expanding the window can only keep the cost same
            // or increase it. So if we exceed the budget, the only way to recover is to move left forward.
            while (currentCost > budget && left < right)
            {
                // When removing times[left] from the window, we also remove the adjacent gap
                // between times[left] and times[left + 1], because that pair is no longer inside the window.
                //
                // This is the exact opposite of how we added a gap when expanding right.
                long removedGap = times[left + 1] - times[left] - 1;
                currentCost -= removedGap;

                // Move the left boundary forward by one.
                left++;
            }

            // STEP 3: At this point, the window [left..right] is valid:
            // currentCost <= budget
            //
            // So we can compute its length and update the best answer.
            int windowLength = right - left + 1;
            if (windowLength > best)
            {
                best = windowLength;
            }
        }

        return best;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
long[] times1 = { 2, 3, 7, 8, 10 };
long budget1 = 3;
int result1 = solution.LongestSensorWindow(times1, budget1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 4

// Example 2
long[] times2 = { 1, 5, 6, 9, 15, 16 };
long budget2 = 5;
int result2 = solution.LongestSensorWindow(times2, budget2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 3

// Additional small sanity checks

// Single report: always valid with cost 0
long[] times3 = { 42 };
long budget3 = 0;
int result3 = solution.LongestSensorWindow(times3, budget3);
Console.WriteLine("Single Report Result: " + result3); // Expected: 1

// Fully continuous timestamps: all adjacent gaps are 0
long[] times4 = { 10, 11, 12, 13, 14 };
long budget4 = 0;
int result4 = solution.LongestSensorWindow(times4, budget4);
Console.WriteLine("Fully Continuous Result: " + result4); // Expected: 5

// Large gaps with zero budget: only windows with no missing seconds inside are allowed
long[] times5 = { 1, 2, 10, 11, 12 };
long budget5 = 0;
int result5 = solution.LongestSensorWindow(times5, budget5);
Console.WriteLine("Zero Budget Result: " + result5); // Expected: 3