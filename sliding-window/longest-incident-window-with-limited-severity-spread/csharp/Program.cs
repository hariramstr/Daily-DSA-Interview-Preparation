/*
Title: Longest Incident Window With Limited Severity Spread
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are given an array `severity` where `severity[i]` is the severity score of the `i`-th incident in a time-ordered monitoring stream, and an integer `limit`. A contiguous incident window is considered stable if the difference between the maximum severity and the minimum severity inside that window is at most `limit`.

Your task is to return the length of the longest stable contiguous window.

Unlike simple fixed-size window problems, the valid window size is not known in advance and the maximum and minimum values must be maintained efficiently as the window expands and shrinks. An `O(n^2)` solution that checks every subarray will not pass.

Formally, find the maximum value of `r - l + 1` such that for some indices `0 <= l <= r < n`, we have:
`max(severity[l..r]) - min(severity[l..r]) <= limit`.

Constraints:
- `1 <= severity.length <= 200000`
- `0 <= severity[i] <= 1000000000`
- `0 <= limit <= 1000000000`

Example 1:
Input: severity = [4, 7, 5, 6, 8, 3, 4], limit = 3
Output: 5
Explanation: The narrative in the prompt is inconsistent, but checking all windows shows the true longest valid windows are
`[4, 7, 5, 6]` and `[7, 5, 6, 8]`, both of length 4. Therefore the correct answer is 4.

Example 2:
Input: severity = [10, 10, 9, 8, 7, 7, 8], limit = 2
Output: 6
Explanation: The narrative in the prompt is inconsistent again. The true longest valid window is
`[9, 8, 7, 7, 8]`, which has max = 9, min = 7, spread = 2, and length = 5.
Therefore the correct answer is 5.

Return only the maximum length of such a stable window.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Why O(n)?
    - Each array index is added to each deque exactly once.
    - Each array index is removed from each deque at most once.
    - So the total work across the full scan is linear.

    Why O(n) space?
    - In the worst case, the deques can together hold a linear number of indices.
    */
    public int LongestStableWindow(int[] severity, int limit)
    {
        // We will use the classic variable-size sliding window technique.
        //
        // The window is [left..right].
        // We expand "right" one step at a time.
        // If the window becomes invalid (max - min > limit),
        // we move "left" forward until the window becomes valid again.
        //
        // The hard part is getting the current window's maximum and minimum quickly.
        // If we recomputed max and min by scanning the window every time,
        // that would be too slow.
        //
        // To solve that efficiently, we use TWO monotonic deques:
        //
        // 1) maxDeque:
        //    - Stores indices
        //    - Values are kept in decreasing order
        //    - The front always points to the maximum value in the current window
        //
        // 2) minDeque:
        //    - Stores indices
        //    - Values are kept in increasing order
        //    - The front always points to the minimum value in the current window
        //
        // We store indices instead of values because:
        // - We need to know when an element falls out of the window
        // - That is easy to detect by comparing the stored index with "left"

        int n = severity.Length;

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int best = 0;

        // Deque for tracking maximum values in the current window.
        // The values corresponding to these indices are in decreasing order.
        LinkedList<int> maxDeque = new LinkedList<int>();

        // Deque for tracking minimum values in the current window.
        // The values corresponding to these indices are in increasing order.
        LinkedList<int> minDeque = new LinkedList<int>();

        // Expand the window by moving "right" from 0 to n - 1.
        for (int right = 0; right < n; right++)
        {
            // ------------------------------------------------------------
            // STEP 1: Insert severity[right] into maxDeque
            // ------------------------------------------------------------
            //
            // maxDeque must remain decreasing by value.
            // That means:
            // - While the last element in maxDeque has a value smaller than
            //   the new value severity[right], it can never be the maximum
            //   for this or any future window that also includes severity[right].
            //
            // Why can we safely remove it?
            // Because the new value is:
            // - to the right of it, so it stays in the window at least as long
            // - greater than or equal to it for max purposes
            //
            // Therefore the older smaller value is useless for future maximum queries.
            while (maxDeque.Count > 0 && severity[maxDeque.Last!.Value] < severity[right])
            {
                maxDeque.RemoveLast();
            }

            // After removing all smaller trailing values, append the new index.
            maxDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 2: Insert severity[right] into minDeque
            // ------------------------------------------------------------
            //
            // minDeque must remain increasing by value.
            // That means:
            // - While the last element in minDeque has a value larger than
            //   the new value severity[right], it can never be the minimum
            //   for this or any future window that also includes severity[right].
            //
            // Why can we safely remove it?
            // Because the new value is:
            // - to the right of it, so it survives at least as long in future windows
            // - smaller than or equal to it for min purposes
            //
            // Therefore the older larger value is useless for future minimum queries.
            while (minDeque.Count > 0 && severity[minDeque.Last!.Value] > severity[right])
            {
                minDeque.RemoveLast();
            }

            // Append the new index after removing all larger trailing values.
            minDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 3: Shrink the window while it is invalid
            // ------------------------------------------------------------
            //
            // The current maximum is at the front of maxDeque.
            // The current minimum is at the front of minDeque.
            //
            // If max - min > limit, the window is not stable,
            // so we must move "left" forward until it becomes stable again.
            //
            // Important:
            // We use a while loop, not an if statement,
            // because moving left by one step may still leave the window invalid.
            while (severity[maxDeque.First!.Value] - severity[minDeque.First!.Value] > limit)
            {
                // If the index at the front of maxDeque is exactly "left",
                // that means this maximum element is leaving the window.
                // So we must remove it from the deque.
                if (maxDeque.First!.Value == left)
                {
                    maxDeque.RemoveFirst();
                }

                // Similarly, if the index at the front of minDeque is exactly "left",
                // that minimum element is leaving the window and must be removed.
                if (minDeque.First!.Value == left)
                {
                    minDeque.RemoveFirst();
                }

                // Move the left boundary rightward by one.
                left++;
            }

            // ------------------------------------------------------------
            // STEP 4: Update the best answer
            // ------------------------------------------------------------
            //
            // At this point, the window [left..right] is guaranteed valid.
            // So its length is a candidate answer.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        return best;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1 from the prompt.
// True correct answer after checking all windows is 4.
int[] severity1 = { 4, 7, 5, 6, 8, 3, 4 };
int limit1 = 3;
int result1 = solution.LongestStableWindow(severity1, limit1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2 from the prompt.
// True correct answer after checking all windows is 5.
int[] severity2 = { 10, 10, 9, 8, 7, 7, 8 };
int limit2 = 2;
int result2 = solution.LongestStableWindow(severity2, limit2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional quick sanity checks.
int[] severity3 = { 8 };
int limit3 = 0;
Console.WriteLine($"Single element Result: {solution.LongestStableWindow(severity3, limit3)}");

int[] severity4 = { 1, 1, 1, 1 };
int limit4 = 0;
Console.WriteLine($"All equal Result: {solution.LongestStableWindow(severity4, limit4)}");

int[] severity5 = { 1, 5, 6, 7, 8, 10, 6, 5, 6 };
int limit5 = 4;
Console.WriteLine($"LeetCode-style sample Result: {solution.LongestStableWindow(severity5, limit5)}");