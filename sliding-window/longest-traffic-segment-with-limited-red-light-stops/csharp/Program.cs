/*
Title: Longest Traffic Segment With Limited Red-Light Stops
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are given a road represented by an array `lights` of length `n`, where `lights[i]` is either `0` or `1`.
A value of `1` means the traffic light at intersection `i` is green when you arrive, and `0` means it is red.
You are also given an integer `k` and an array `penalty` of length `n`, where `penalty[i]` is the delay cost
of stopping at intersection `i` if that light is red.

A driver wants to choose one contiguous road segment `[l, r]` to travel through.

A segment is considered feasible if:
1. It contains at most `k` red lights, and
2. Among the red lights inside the segment, the maximum penalty minus the minimum penalty is at most `limit`.

Return the length of the longest feasible contiguous segment.

In other words, you must find the largest window where the number of red lights is bounded, and the red-light
penalties inside that same window stay tightly clustered. Green lights do not affect the penalty range and should
be ignored for the second condition.

If a segment contains no red lights, it is always valid as long as condition 1 holds.

Constraints:
- `1 <= n <= 2 * 10^5`
- `lights.length == penalty.length == n`
- `lights[i]` is `0` or `1`
- `0 <= penalty[i] <= 10^9`
- `0 <= k <= n`
- `0 <= limit <= 10^9`

Example 1:
Input: lights = [1,0,0,1,0,1,0], penalty = [5,8,10,3,9,4,11], k = 3, limit = 2
Output: 5

Example 2:
Input: lights = [0,1,0,0,1,0], penalty = [7,2,20,9,1,10], k = 2, limit = 3
Output: 3
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n), where n is the length of the arrays.
      Each index is added to and removed from each deque at most one time.

    Space Complexity:
    - O(n) in the worst case for the deques, although in practice they only store red-light indices
      that are still relevant to the current sliding window.
    */
    public int LongestTrafficSegment(int[] lights, int[] penalty, int k, int limit)
    {
        int n = lights.Length;

        // This pointer marks the left boundary of our current sliding window.
        // The window we are considering at any moment is [left, right].
        int left = 0;

        // This counts how many red lights (lights[i] == 0) are currently inside the window.
        // We need this because one of the rules says the window may contain at most k red lights.
        int redCount = 0;

        // This will store the best (maximum) valid window length found so far.
        int best = 0;

        // We need to efficiently know the minimum and maximum penalty among ONLY the red lights
        // inside the current window.
        //
        // A classic sliding-window trick for "window min/max" is to use monotonic deques.
        //
        // minDeque:
        // - Stores indices of red lights.
        // - Their penalties are kept in increasing order.
        // - Therefore, the front always gives the minimum red penalty in the current window.
        //
        // maxDeque:
        // - Stores indices of red lights.
        // - Their penalties are kept in decreasing order.
        // - Therefore, the front always gives the maximum red penalty in the current window.
        //
        // Important:
        // We only insert indices where lights[i] == 0, because green lights do not affect
        // the penalty-range condition at all.
        LinkedList<int> minDeque = new LinkedList<int>();
        LinkedList<int> maxDeque = new LinkedList<int>();

        // Expand the window one position at a time by moving "right" from 0 to n - 1.
        for (int right = 0; right < n; right++)
        {
            // Step 1:
            // If the new element at position "right" is a red light, it affects both constraints:
            // - It increases the number of red lights in the window.
            // - Its penalty participates in the min/max range among red lights.
            if (lights[right] == 0)
            {
                redCount++;

                // Maintain minDeque in increasing order of penalty.
                //
                // Why remove from the back while the new penalty is smaller?
                // Because if penalty[right] is smaller or equal, then the older larger/equal values
                // can never become the minimum while this new index remains in the window.
                while (minDeque.Count > 0 && penalty[minDeque.Last!.Value] >= penalty[right])
                {
                    minDeque.RemoveLast();
                }
                minDeque.AddLast(right);

                // Maintain maxDeque in decreasing order of penalty.
                //
                // Why remove from the back while the new penalty is larger?
                // Because if penalty[right] is larger or equal, then the older smaller/equal values
                // can never become the maximum while this new index remains in the window.
                while (maxDeque.Count > 0 && penalty[maxDeque.Last!.Value] <= penalty[right])
                {
                    maxDeque.RemoveLast();
                }
                maxDeque.AddLast(right);
            }

            // Step 2:
            // If the window violates any rule, we must shrink it from the left until it becomes valid again.
            //
            // The two rules are:
            // 1. redCount <= k
            // 2. among red lights in the window, maxPenalty - minPenalty <= limit
            //
            // If there are no red lights in the window, then minDeque and maxDeque are empty,
            // and the penalty-range condition is automatically satisfied.
            while (IsInvalid(redCount, k, minDeque, maxDeque, penalty, limit))
            {
                // Before moving left forward, we must remove the contribution of index "left"
                // if it is currently part of the window and if it is a red light.
                if (lights[left] == 0)
                {
                    redCount--;

                    // If the outgoing red-light index is currently at the front of minDeque,
                    // it means that exact index is leaving the window, so we must pop it.
                    if (minDeque.Count > 0 && minDeque.First!.Value == left)
                    {
                        minDeque.RemoveFirst();
                    }

                    // Same logic for maxDeque.
                    if (maxDeque.Count > 0 && maxDeque.First!.Value == left)
                    {
                        maxDeque.RemoveFirst();
                    }
                }

                // Actually shrink the window by moving the left boundary one step right.
                left++;
            }

            // Step 3:
            // At this point, the window [left, right] is guaranteed to be valid.
            // So we can safely use its length to update the answer.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        return best;
    }

    private static bool IsInvalid(
        int redCount,
        int k,
        LinkedList<int> minDeque,
        LinkedList<int> maxDeque,
        int[] penalty,
        int limit)
    {
        // First invalid condition: too many red lights.
        if (redCount > k)
        {
            return true;
        }

        // If there are no red lights, then there is no penalty spread to check.
        // The window is valid as far as the second condition is concerned.
        if (minDeque.Count == 0)
        {
            return false;
        }

        // The smallest red penalty in the current window is at the front of minDeque.
        int minPenalty = penalty[minDeque.First!.Value];

        // The largest red penalty in the current window is at the front of maxDeque.
        int maxPenalty = penalty[maxDeque.First!.Value];

        // Second invalid condition: penalty spread is too large.
        return (long)maxPenalty - minPenalty > limit;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] lights1 = { 1, 0, 0, 1, 0, 1, 0 };
int[] penalty1 = { 5, 8, 10, 3, 9, 4, 11 };
int k1 = 3;
int limit1 = 2;
int result1 = solution.LongestTrafficSegment(lights1, penalty1, k1, limit1);
Console.WriteLine(result1); // Expected: 5

// Example 2
int[] lights2 = { 0, 1, 0, 0, 1, 0 };
int[] penalty2 = { 7, 2, 20, 9, 1, 10 };
int k2 = 2;
int limit2 = 3;
int result2 = solution.LongestTrafficSegment(lights2, penalty2, k2, limit2);
Console.WriteLine(result2); // Expected: 3

// Additional quick sanity checks

// All green lights: entire array is valid.
int[] lights3 = { 1, 1, 1, 1 };
int[] penalty3 = { 10, 20, 30, 40 };
int result3 = solution.LongestTrafficSegment(lights3, penalty3, 0, 0);
Console.WriteLine(result3); // Expected: 4

// k = 0 means no red lights allowed in the chosen segment.
int[] lights4 = { 1, 0, 1, 1, 0, 1 };
int[] penalty4 = { 5, 7, 2, 3, 9, 1 };
int result4 = solution.LongestTrafficSegment(lights4, penalty4, 0, 100);
Console.WriteLine(result4); // Expected: 2