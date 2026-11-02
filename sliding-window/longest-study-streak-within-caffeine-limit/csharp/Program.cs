/*
Title: Longest Study Streak Within Caffeine Limit
Difficulty: Easy
Topic: Sliding Window

Problem Description:
You are given an array `cups` where `cups[i]` represents the amount of caffeine consumed
during the `i`-th study session of a day. A student wants to find the longest consecutive
stretch of sessions they can keep without their total caffeine intake exceeding a safety
limit `limit`.

Return the length of the longest contiguous subarray whose sum is less than or equal to `limit`.

A contiguous subarray means the sessions must be consecutive in the original order.
If no single session can fit within the limit, return `0`.

This problem is designed to be solved efficiently using the sliding window technique.
Since all values in `cups` are non-negative, once the current window exceeds the limit,
moving the left side of the window forward is enough to restore validity. An `O(n)` solution
is expected.

Constraints:
- 1 <= cups.length <= 100000
- 0 <= cups[i] <= 10000
- 0 <= limit <= 1000000000

Example 1:
Input: cups = [2, 1, 3, 2, 1], limit = 5
Output: 2
Explanation: Valid longest windows include [2,1], [3,2], and [2,1].
Any window of length 3 has sum greater than 5.

Example 2:
Input: cups = [0, 1, 1, 0, 2], limit = 2
Output: 4
Explanation: The window [0,1,1,0] has total caffeine 2, so its length is 4.
Extending it by the last value makes the sum 4, which is too large.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the window once by the right pointer.
    - Each element is removed from the window at most once by the left pointer.
    - Because both pointers only move forward, the total work is linear.

    Space Complexity: O(1)
    - We only use a few extra variables for pointers, sum, and answer.
    - No extra array or data structure proportional to input size is used.
    */
    public int LongestStudyStreak(int[] cups, int limit)
    {
        // `left` marks the beginning of the current sliding window.
        // The window will always represent a contiguous subarray from `left` to `right`.
        int left = 0;

        // `currentSum` stores the total caffeine inside the current window.
        // We use `long` instead of `int` for extra safety, even though the given constraints
        // still fit in int. Using long is a good habit when summing many values.
        long currentSum = 0;

        // `maxLength` stores the best (largest) valid window length found so far.
        int maxLength = 0;

        // We expand the window one session at a time using `right`.
        // At every step, we include cups[right] into the current window.
        for (int right = 0; right < cups.Length; right++)
        {
            // Step 1: Expand the window to the right.
            // We include the current session's caffeine amount in the running sum.
            // This means our current window is now from index `left` to index `right`.
            currentSum += cups[right];

            // Step 2: If the window is invalid (sum > limit), we must shrink it from the left.
            //
            // Why is shrinking from the left enough?
            // Because all values are non-negative.
            // That means adding more elements can never reduce the sum.
            // So if the sum is too large, the only way to make it valid again is to remove
            // elements from the left side of the current window.
            //
            // We keep shrinking until the window becomes valid again:
            // currentSum <= limit
            while (currentSum > limit && left <= right)
            {
                // Remove the leftmost element from the current window.
                currentSum -= cups[left];

                // Move the left boundary one step to the right.
                left++;
            }

            // Step 3: At this point, the window is valid:
            // sum of cups[left..right] <= limit
            //
            // So we can compute its length and compare it with the best answer so far.
            int currentLength = right - left + 1;

            // Update the maximum valid window length if this window is larger.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // If no single element fits within the limit, maxLength will remain 0.
        // Otherwise, it will contain the length of the longest valid contiguous subarray.
        return maxLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] cups1 = { 2, 1, 3, 2, 1 };
int limit1 = 5;
int result1 = solution.LongestStudyStreak(cups1, limit1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 2

// Example 2
int[] cups2 = { 0, 1, 1, 0, 2 };
int limit2 = 2;
int result2 = solution.LongestStudyStreak(cups2, limit2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 4

// Additional demo: no session fits
int[] cups3 = { 6, 7, 8 };
int limit3 = 5;
int result3 = solution.LongestStudyStreak(cups3, limit3);
Console.WriteLine($"Additional Demo Result: {result3}"); // Expected: 0

// Additional demo: all sessions fit
int[] cups4 = { 1, 1, 1, 1 };
int limit4 = 10;
int result4 = solution.LongestStudyStreak(cups4, limit4);
Console.WriteLine($"Additional Demo Result: {result4}"); // Expected: 4