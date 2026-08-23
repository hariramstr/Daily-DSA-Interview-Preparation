/*
Title: Longest Snack Stall Run With Freshness Range
Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given an array freshness where freshness[i] is the freshness score of the i-th snack stall along a street.
A tourist wants to visit a contiguous run of stalls, but only if the stalls in that run are reasonably consistent
in quality. A run is considered valid if the difference between the maximum freshness score and the minimum freshness
score inside the run is at most limit.

Return the length of the longest valid contiguous run of stalls.

In other words, find the maximum size of a subarray freshness[l..r] such that:
max(freshness[l..r]) - min(freshness[l..r]) <= limit

This problem is intended to be solved efficiently for large inputs. A brute-force solution that checks every
subarray will be too slow. Think about how to maintain the current window's minimum and maximum values while
expanding and shrinking a sliding window.

Constraints:
- 1 <= freshness.length <= 200000
- 0 <= freshness[i] <= 1000000000
- 0 <= limit <= 1000000000

Example 1:
Input: freshness = [4, 7, 6, 8, 5, 9], limit = 3
Output: 4
Explanation: One longest valid run is [7, 6, 8, 5]. Its maximum is 8 and minimum is 5, so the difference is 3.

Example 2:
Input: freshness = [10, 1, 2, 4, 7, 2], limit = 5
Output: 4
Explanation: The longest valid run is [2, 4, 7, 2]. Its maximum is 7 and minimum is 2, so the difference is 5.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each array element is added to each deque at most once.
    - Each array element is removed from each deque at most once.
    - Therefore, the total work across the full scan is linear.

    Space Complexity: O(n)
    - In the worst case, the deques can together hold up to O(n) indices.

    Beginner-friendly idea:
    We use a sliding window [left..right].
    As we move right forward, we include more stalls.
    If the window becomes invalid (max - min > limit), we move left forward until it becomes valid again.

    The challenge:
    We need to know the current window's minimum and maximum quickly.

    The solution:
    - A monotonic increasing deque stores indices of possible minimum values.
      The front always points to the smallest value in the current window.
    - A monotonic decreasing deque stores indices of possible maximum values.
      The front always points to the largest value in the current window.

    Why store indices instead of values?
    Because when the left side of the window moves forward, we need to know whether
    the element leaving the window is currently sitting at the front of a deque.
    Indices let us remove expired elements precisely.
    */
    public int LongestValidRun(int[] freshness, int limit)
    {
        // This deque will keep indices of elements in increasing order of their values.
        // That means:
        // - The smallest value in the current window will always be at the front.
        // - If a new value is smaller than values at the back, those larger values can never
        //   become the minimum while the new value remains in the window, so we remove them.
        LinkedList<int> minDeque = new();

        // This deque will keep indices of elements in decreasing order of their values.
        // That means:
        // - The largest value in the current window will always be at the front.
        // - If a new value is larger than values at the back, those smaller values can never
        //   become the maximum while the new value remains in the window, so we remove them.
        LinkedList<int> maxDeque = new();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int bestLength = 0;

        // Expand the window one element at a time by moving "right".
        for (int right = 0; right < freshness.Length; right++)
        {
            // ------------------------------------------------------------
            // STEP 1: Insert freshness[right] into the min deque.
            // ------------------------------------------------------------
            // We want minDeque to remain increasing by value.
            // So while the last element in the deque has a value greater than the new value,
            // it is no longer useful as a future minimum candidate.
            while (minDeque.Count > 0 && freshness[minDeque.Last!.Value] > freshness[right])
            {
                minDeque.RemoveLast();
            }

            // Add the current index at the back.
            minDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 2: Insert freshness[right] into the max deque.
            // ------------------------------------------------------------
            // We want maxDeque to remain decreasing by value.
            // So while the last element in the deque has a value smaller than the new value,
            // it is no longer useful as a future maximum candidate.
            while (maxDeque.Count > 0 && freshness[maxDeque.Last!.Value] < freshness[right])
            {
                maxDeque.RemoveLast();
            }

            // Add the current index at the back.
            maxDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 3: Shrink the window from the left while it is invalid.
            // ------------------------------------------------------------
            // The current minimum is at minDeque.First.
            // The current maximum is at maxDeque.First.
            //
            // If max - min > limit, the window is not allowed.
            // So we move "left" forward until the window becomes valid again.
            while (freshness[maxDeque.First!.Value] - freshness[minDeque.First!.Value] > limit)
            {
                // If the element leaving the window is exactly the current minimum,
                // remove it from the front of minDeque.
                if (minDeque.First!.Value == left)
                {
                    minDeque.RemoveFirst();
                }

                // If the element leaving the window is exactly the current maximum,
                // remove it from the front of maxDeque.
                if (maxDeque.First!.Value == left)
                {
                    maxDeque.RemoveFirst();
                }

                // Actually move the left boundary of the window forward.
                left++;
            }

            // ------------------------------------------------------------
            // STEP 4: Update the best answer.
            // ------------------------------------------------------------
            // At this point, the window [left..right] is guaranteed to be valid.
            // So we compute its length and compare it with the best seen so far.
            int currentLength = right - left + 1;
            if (currentLength > bestLength)
            {
                bestLength = currentLength;
            }
        }

        return bestLength;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] freshness1 = { 4, 7, 6, 8, 5, 9 };
int limit1 = 3;
int result1 = solution.LongestValidRun(freshness1, limit1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 4

// Example 2
int[] freshness2 = { 10, 1, 2, 4, 7, 2 };
int limit2 = 5;
int result2 = solution.LongestValidRun(freshness2, limit2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 4

// Additional quick checks
int[] freshness3 = { 8 };
int limit3 = 0;
int result3 = solution.LongestValidRun(freshness3, limit3);
Console.WriteLine("Single Stall Result: " + result3); // Expected: 1

int[] freshness4 = { 1, 1, 1, 1 };
int limit4 = 0;
int result4 = solution.LongestValidRun(freshness4, limit4);
Console.WriteLine("All Equal Result: " + result4); // Expected: 4