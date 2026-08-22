/*
Title: Longest Reading Sprint With Limited Bookmark Moves
Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given an array `pages` where `pages[i]` is the number of pages in the `i`th chapter of an online course, in the order they must be read. A student wants to complete the longest possible contiguous reading sprint. However, switching between chapters with very different lengths is mentally expensive.

For any contiguous sprint `pages[l..r]`, define its effort as `max(pages[l..r]) - min(pages[l..r])`. The student can only maintain focus if this effort is at most `limit`.

Return the length of the longest contiguous subarray whose effort does not exceed `limit`.

In other words, find the maximum number of consecutive chapters such that the difference between the largest and smallest chapter lengths in that window is at most `limit`.

A correct solution is expected to use a sliding window approach efficiently, since the input size can be large.

Constraints:
- 1 <= pages.length <= 100000
- 1 <= pages[i] <= 1000000000
- 0 <= limit <= 1000000000

Example 1:
Input: pages = [12, 15, 14, 10, 13, 18], limit = 5
Output: 5
Explanation: The longest valid sprint is [12, 15, 14, 10, 13]. Its maximum is 15 and minimum is 10, so the effort is 5, which is allowed. Extending to include 18 makes the effort 8, which is too large.

Example 2:
Input: pages = [7, 7, 7, 20, 21, 22], limit = 2
Output: 3
Explanation: Valid longest sprints include [7, 7, 7] and [20, 21, 22]. Each has max - min <= 2, so the answer is 3.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each chapter index is added to and removed from each deque at most one time.
    - The left and right pointers each move from left to right across the array once.

    Space Complexity: O(n)
    - In the worst case, the deques can together store up to O(n) indices.

    Beginner-friendly idea:
    We use a sliding window [left..right].
    For every new right position, we expand the window.
    We must quickly know:
    - the maximum value in the current window
    - the minimum value in the current window

    If max - min > limit, the window is invalid, so we move left forward until it becomes valid again.

    To get max and min efficiently, we use two monotonic deques:
    - maxDeque keeps values in decreasing order, so the front is the maximum
    - minDeque keeps values in increasing order, so the front is the minimum

    We store indices, not values, because:
    - we need to know whether an element has moved out of the window
    - indices let us compare with the current left boundary
    */
    public int LongestSubarray(int[] pages, int limit)
    {
        // This deque will store indices of elements in decreasing value order.
        // That means pages[maxDeque[0]] is always the maximum value in the current window.
        var maxDeque = new LinkedList<int>();

        // This deque will store indices of elements in increasing value order.
        // That means pages[minDeque[0]] is always the minimum value in the current window.
        var minDeque = new LinkedList<int>();

        // left marks the start of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // We move right one step at a time, expanding the window.
        for (int right = 0; right < pages.Length; right++)
        {
            // ------------------------------------------------------------
            // STEP 1: Insert pages[right] into the max deque
            // ------------------------------------------------------------
            // We want maxDeque to remain decreasing by value.
            // So while the last element in the deque has a value smaller than
            // the current value, it can never become the maximum for any future
            // window that also includes the current element.
            //
            // Why can we remove it?
            // Because the current element is:
            // - to the right of it, so it stays in the window longer
            // - larger than it, so it dominates it for maximum queries
            while (maxDeque.Count > 0 && pages[maxDeque.Last!.Value] < pages[right])
            {
                maxDeque.RemoveLast();
            }

            // Now append the current index.
            maxDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 2: Insert pages[right] into the min deque
            // ------------------------------------------------------------
            // We want minDeque to remain increasing by value.
            // So while the last element in the deque has a value larger than
            // the current value, it can never become the minimum for any future
            // window that also includes the current element.
            //
            // Why can we remove it?
            // Because the current element is:
            // - to the right of it, so it stays in the window longer
            // - smaller than it, so it dominates it for minimum queries
            while (minDeque.Count > 0 && pages[minDeque.Last!.Value] > pages[right])
            {
                minDeque.RemoveLast();
            }

            // Now append the current index.
            minDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 3: Shrink the window while it is invalid
            // ------------------------------------------------------------
            // The current maximum is at the front of maxDeque.
            // The current minimum is at the front of minDeque.
            //
            // If max - min > limit, the window is invalid and we must move left.
            while (pages[maxDeque.First!.Value] - pages[minDeque.First!.Value] > limit)
            {
                // If the element leaving the window is exactly the one at the front
                // of maxDeque, we must remove it because it is no longer inside [left..right].
                if (maxDeque.First!.Value == left)
                {
                    maxDeque.RemoveFirst();
                }

                // Similarly, if the element leaving the window is the front
                // of minDeque, remove it as well.
                if (minDeque.First!.Value == left)
                {
                    minDeque.RemoveFirst();
                }

                // Move the left boundary rightward to make the window smaller.
                left++;
            }

            // ------------------------------------------------------------
            // STEP 4: Update the best answer
            // ------------------------------------------------------------
            // At this point, the window [left..right] is guaranteed valid.
            // So we compute its length and compare it with the best seen so far.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // After scanning the entire array, best is the answer.
        return best;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] pages1 = { 12, 15, 14, 10, 13, 18 };
int limit1 = 5;
int result1 = solution.LongestSubarray(pages1, limit1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 5

// Example 2
int[] pages2 = { 7, 7, 7, 20, 21, 22 };
int limit2 = 2;
int result2 = solution.LongestSubarray(pages2, limit2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 3

// Additional demo
int[] pages3 = { 8, 2, 4, 7 };
int limit3 = 4;
int result3 = solution.LongestSubarray(pages3, limit3);
Console.WriteLine($"Additional Demo Result: {result3}"); // Expected: 2