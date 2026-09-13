/*
Title: Longest Session Window With Pairwise Latency Gap Limit
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are given an array `latency` where `latency[i]` is the measured response time of the `i`-th request in a production session, and an integer `limit`. A contiguous block of requests is called stable if for every pair of requests inside that block, the absolute difference between their latencies is at most `limit`. Equivalently, if `max(latency[l..r]) - min(latency[l..r]) <= limit`, then the window `[l, r]` is stable.

Your task is to return the length of the longest stable contiguous window.

This problem must be solved efficiently for very large input sizes. A brute-force approach that checks every subarray or recomputes the minimum and maximum for each candidate window will be too slow. The intended solution uses a sliding window together with data structures that can maintain the current window minimum and maximum as the window expands and shrinks.

Constraints:
- 1 <= latency.length <= 200000
- 0 <= latency[i] <= 10^9
- 0 <= limit <= 10^9

Example 1:
Input: latency = [8, 2, 4, 7], limit = 4
Output: 2
Explanation: The longest stable windows are [2, 4] and [4, 7]. Any window of length 3 has max - min greater than 4.

Example 2:
Input: latency = [10, 1, 2, 4, 7, 2], limit = 5
Output: 4
Explanation: The window [2, 4, 7, 2] has maximum 7 and minimum 2, so the difference is 5, which is allowed. No longer contiguous window satisfies the condition.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n), where n is the length of the latency array.

    Why O(n)?
    - Each index is added to each deque exactly once.
    - Each index is removed from each deque at most once.
    - The left and right pointers each move only forward.
    So the total amount of work is linear.

    Space Complexity:
    O(n) in the worst case.

    Why O(n)?
    - The two deques can together hold up to O(n) indices in the worst case.
    */
    public int LongestStableWindow(int[] latency, int limit)
    {
        // This deque will store indices of elements in NON-INCREASING value order.
        // That means:
        // - The values at these indices go from large to small.
        // - The front of the deque always holds the index of the current maximum value in the window.
        //
        // Why do we need this?
        // Because we must quickly know the maximum value inside the current sliding window.
        LinkedList<int> maxDeque = new LinkedList<int>();

        // This deque will store indices of elements in NON-DECREASING value order.
        // That means:
        // - The values at these indices go from small to large.
        // - The front of the deque always holds the index of the current minimum value in the window.
        //
        // Why do we need this?
        // Because we must quickly know the minimum value inside the current sliding window.
        LinkedList<int> minDeque = new LinkedList<int>();

        // 'left' is the left boundary of our sliding window.
        // The right boundary will be controlled by the loop variable 'right'.
        int left = 0;

        // This will store the best (largest) valid window length we have seen so far.
        int best = 0;

        // We expand the window one element at a time by moving 'right' from left to right.
        for (int right = 0; right < latency.Length; right++)
        {
            // ------------------------------------------------------------
            // STEP 1: Insert latency[right] into the max deque
            // ------------------------------------------------------------
            //
            // Goal:
            // Maintain maxDeque so that values are in decreasing order.
            //
            // Why remove from the back while the new value is larger?
            // Suppose the new value is latency[right].
            // If there is an index at the back whose value is smaller than latency[right],
            // that older smaller value can never become the maximum for any future window
            // that also includes latency[right].
            //
            // So it is useless and can be removed.
            while (maxDeque.Count > 0 && latency[maxDeque.Last!.Value] < latency[right])
            {
                maxDeque.RemoveLast();
            }

            // Now append the current index.
            // After removals, the deque still preserves decreasing order of values.
            maxDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 2: Insert latency[right] into the min deque
            // ------------------------------------------------------------
            //
            // Goal:
            // Maintain minDeque so that values are in increasing order.
            //
            // Why remove from the back while the new value is smaller?
            // If the new value is smaller than some older value at the back,
            // then that older larger value can never become the minimum for any future window
            // that also includes the new smaller value.
            //
            // So it is useless and can be removed.
            while (minDeque.Count > 0 && latency[minDeque.Last!.Value] > latency[right])
            {
                minDeque.RemoveLast();
            }

            // Append the current index.
            // After removals, the deque still preserves increasing order of values.
            minDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 3: Shrink the window from the left until it becomes valid
            // ------------------------------------------------------------
            //
            // The current window is [left, right].
            //
            // The maximum value in the current window is at:
            //   latency[maxDeque.First.Value]
            //
            // The minimum value in the current window is at:
            //   latency[minDeque.First.Value]
            //
            // The window is valid if:
            //   max - min <= limit
            //
            // If it is invalid, we must move 'left' forward.
            // While moving 'left', we also need to remove indices from the fronts of the deques
            // if those indices have fallen out of the window.
            while (latency[maxDeque.First!.Value] - latency[minDeque.First!.Value] > limit)
            {
                // If the leftmost index of the window is exactly the current maximum index,
                // then once we move left forward, that index is no longer inside the window.
                // So we must remove it from maxDeque.
                if (maxDeque.First!.Value == left)
                {
                    maxDeque.RemoveFirst();
                }

                // Similarly, if the leftmost index is exactly the current minimum index,
                // it also leaves the window and must be removed from minDeque.
                if (minDeque.First!.Value == left)
                {
                    minDeque.RemoveFirst();
                }

                // Actually shrink the window by moving the left boundary to the right.
                left++;
            }

            // ------------------------------------------------------------
            // STEP 4: Update the best answer
            // ------------------------------------------------------------
            //
            // At this point, the window [left, right] is guaranteed to be valid.
            // So its length is a candidate answer.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // After processing all positions, 'best' is the length of the longest valid window.
        return best;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] latency1 = { 8, 2, 4, 7 };
int limit1 = 4;
int result1 = solution.LongestStableWindow(latency1, limit1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 2

// Example 2
int[] latency2 = { 10, 1, 2, 4, 7, 2 };
int limit2 = 5;
int result2 = solution.LongestStableWindow(latency2, limit2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 4

// Additional demo
int[] latency3 = { 4, 2, 2, 2, 4, 4, 2, 2 };
int limit3 = 0;
int result3 = solution.LongestStableWindow(latency3, limit3);
Console.WriteLine("Additional Demo Result: " + result3); // Expected: 3