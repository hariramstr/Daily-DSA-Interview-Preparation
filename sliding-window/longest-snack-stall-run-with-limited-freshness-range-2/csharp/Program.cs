/*
Title: Longest Snack Stall Run with Limited Freshness Range
Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given an array freshness where freshness[i] represents the freshness score of the i-th snack stall along a street.
A tourist wants to visit a contiguous run of stalls, but only if the stalls in that run are reasonably consistent in quality.
A run is considered valid if the difference between the maximum freshness score and the minimum freshness score inside the run is at most limit.

Return the length of the longest valid contiguous run of stalls.

In other words, find the maximum window size such that for some subarray freshness[l..r],
max(freshness[l..r]) - min(freshness[l..r]) <= limit.

This problem should be solved efficiently for large inputs. A brute-force solution that checks every subarray
will be too slow. Think about how to maintain the current window's minimum and maximum as you expand and shrink
a sliding window.

Constraints:
- 1 <= freshness.length <= 200000
- 0 <= freshness[i] <= 1000000000
- 0 <= limit <= 1000000000

Example 1:
Input: freshness = [8, 6, 7, 9, 10, 5], limit = 3
Output: 4
Explanation:
The longest valid run is length 4.
A valid run of length 4 is [8, 6, 7, 9], where max = 9 and min = 6, so max - min = 3 <= limit.

Example 2:
Input: freshness = [4, 4, 4, 2, 3, 5, 6], limit = 1
Output: 3
Explanation:
One longest valid run is [4, 4, 4]. Its maximum and minimum are both 4, so the difference is 0.
No longer valid run exists.

Approach Summary:
We use a sliding window with two monotonic deques:
1. One deque keeps indices of elements in decreasing order of values, so its front is always the maximum.
2. One deque keeps indices of elements in increasing order of values, so its front is always the minimum.

As we expand the right side of the window:
- We insert the new element into both deques while preserving monotonic order.
- If the current window becomes invalid (max - min > limit), we move the left side forward
  and remove outdated indices from the fronts of the deques.

This gives an efficient O(n) solution.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each index is added to and removed from each deque at most once.
    - Therefore, all deque operations across the full array are linear.

    Space Complexity: O(n)
    - In the worst case, the deques can store up to n indices.
    */
    public int LongestSubarray(int[] freshness, int limit)
    {
        // This deque will store indices of elements in decreasing order of freshness values.
        // Why decreasing?
        // Because then the front of the deque always points to the largest value in the current window.
        LinkedList<int> maxDeque = new LinkedList<int>();

        // This deque will store indices of elements in increasing order of freshness values.
        // Why increasing?
        // Because then the front of the deque always points to the smallest value in the current window.
        LinkedList<int> minDeque = new LinkedList<int>();

        // 'left' is the left boundary of our sliding window.
        // The window will always be freshness[left..right].
        int left = 0;

        // This will store the best (largest) valid window length we have seen so far.
        int bestLength = 0;

        // We expand the window one element at a time by moving 'right' from left to right.
        for (int right = 0; right < freshness.Length; right++)
        {
            // ------------------------------------------------------------
            // STEP 1: Insert freshness[right] into the max deque
            // ------------------------------------------------------------
            // We want maxDeque to remain in decreasing order of values.
            // So while the last element in the deque is smaller than the current value,
            // it can never become the maximum for this or any future window that includes
            // the current element, because the current element is larger and newer.
            while (maxDeque.Count > 0 && freshness[maxDeque.Last!.Value] < freshness[right])
            {
                maxDeque.RemoveLast();
            }

            // After removing all smaller trailing elements, add the current index.
            maxDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 2: Insert freshness[right] into the min deque
            // ------------------------------------------------------------
            // We want minDeque to remain in increasing order of values.
            // So while the last element in the deque is larger than the current value,
            // it can never become the minimum for this or any future window that includes
            // the current element, because the current element is smaller and newer.
            while (minDeque.Count > 0 && freshness[minDeque.Last!.Value] > freshness[right])
            {
                minDeque.RemoveLast();
            }

            // After removing all larger trailing elements, add the current index.
            minDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 3: Shrink the window while it is invalid
            // ------------------------------------------------------------
            // The current maximum is at the front of maxDeque.
            // The current minimum is at the front of minDeque.
            // If max - min > limit, the window is invalid and we must move 'left' forward.
            while (freshness[maxDeque.First!.Value] - freshness[minDeque.First!.Value] > limit)
            {
                // If the leftmost index is exactly the one at the front of maxDeque,
                // it means that maximum element is leaving the window, so remove it.
                if (maxDeque.First!.Value == left)
                {
                    maxDeque.RemoveFirst();
                }

                // If the leftmost index is exactly the one at the front of minDeque,
                // it means that minimum element is leaving the window, so remove it.
                if (minDeque.First!.Value == left)
                {
                    minDeque.RemoveFirst();
                }

                // Move the left boundary rightward by one to shrink the window.
                left++;
            }

            // ------------------------------------------------------------
            // STEP 4: Update the answer
            // ------------------------------------------------------------
            // At this point, the window freshness[left..right] is guaranteed to be valid.
            // So we compute its length and compare it with the best answer seen so far.
            int currentLength = right - left + 1;
            if (currentLength > bestLength)
            {
                bestLength = currentLength;
            }
        }

        // Return the largest valid window length found.
        return bestLength;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] freshness1 = { 8, 6, 7, 9, 10, 5 };
int limit1 = 3;
int result1 = solution.LongestSubarray(freshness1, limit1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 4

// Example 2
int[] freshness2 = { 4, 4, 4, 2, 3, 5, 6 };
int limit2 = 1;
int result2 = solution.LongestSubarray(freshness2, limit2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 3

// Additional quick checks
int[] freshness3 = { 1 };
int limit3 = 0;
int result3 = solution.LongestSubarray(freshness3, limit3);
Console.WriteLine("Additional Check 1 Result: " + result3); // Expected: 1

int[] freshness4 = { 1, 5, 6, 7, 8, 10, 6, 5, 6 };
int limit4 = 4;
int result4 = solution.LongestSubarray(freshness4, limit4);
Console.WriteLine("Additional Check 2 Result: " + result4); // Expected: 5