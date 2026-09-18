/*
Title: Longest Moderation Queue With Bounded Toxicity Spread
Difficulty: Hard
Topic: Sliding Window

Problem Description:
A social platform stores the toxicity score of each newly posted comment in chronological order.
You are given an integer array scores, where scores[i] is the toxicity score of the i-th comment,
and an integer limit.

A contiguous block of comments is considered reviewable if the difference between the maximum
toxicity score and the minimum toxicity score inside that block is at most limit.

Your task is to return the length of the longest reviewable contiguous block.

Formally, find the maximum value of (r - l + 1) such that for some 0 <= l <= r < scores.length:
max(scores[l..r]) - min(scores[l..r]) <= limit.

Constraints:
- 1 <= scores.length <= 200000
- 0 <= scores[i] <= 1000000000
- 0 <= limit <= 1000000000

Examples:
1)
Input: scores = [4, 7, 5, 6, 8, 3, 4], limit = 3
Output: 4

2)
Input: scores = [10, 10, 10, 1, 2, 3, 4], limit = 2
Correct Output: 3
Important note:
The statement text contains a contradiction where it first says Output: 4, but the explanation
correctly shows that the longest valid block length is 3. The algorithm below follows the formal
definition and therefore returns 3 for this example.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Why O(n)?
    - Each array element is added to each deque at most once.
    - Each array element is removed from each deque at most once.
    - The left and right pointers each move only forward.
    Therefore, the total amount of work is linear in the number of elements.

    Why O(n) space?
    - In the worst case, the deques can together store a linear number of indices.
    */
    public int LongestReviewableBlock(int[] scores, int limit)
    {
        // We will use the classic sliding window technique:
        // - "left" marks the beginning of the current window
        // - "right" expands the window one element at a time
        //
        // The challenge is that for every window we need to know:
        // - the minimum value inside the window
        // - the maximum value inside the window
        //
        // Recomputing min and max from scratch for every window would be too slow.
        // Instead, we maintain them efficiently using two monotonic deques:
        //
        // 1) minDeque:
        //    - stores indices of elements in increasing order of their values
        //    - the front always points to the minimum value in the current window
        //
        // 2) maxDeque:
        //    - stores indices of elements in decreasing order of their values
        //    - the front always points to the maximum value in the current window
        //
        // We store indices instead of values because:
        // - we need to know when an element falls out of the window
        // - indices let us compare against "left" directly

        int n = scores.Length;

        // Left boundary of the current sliding window.
        int left = 0;

        // Best answer found so far.
        int bestLength = 0;

        // Deque for tracking minimum values.
        LinkedList<int> minDeque = new LinkedList<int>();

        // Deque for tracking maximum values.
        LinkedList<int> maxDeque = new LinkedList<int>();

        // Expand the window by moving "right" from left to right across the array.
        for (int right = 0; right < n; right++)
        {
            // ------------------------------------------------------------
            // STEP 1: Insert scores[right] into the minDeque
            // ------------------------------------------------------------
            //
            // Goal:
            // Keep minDeque values increasing from front to back.
            //
            // Why?
            // The smallest value should always be at the front, so we can read
            // the current window minimum in O(1) time.
            //
            // How?
            // While the last element in the deque has a value greater than the
            // current value, it can never become the minimum for this or any
            // future window that includes the current element.
            //
            // Example:
            // If deque ends with value 7 and current value is 5,
            // then 7 is useless behind 5 because:
            // - 5 is smaller
            // - 5 is newer, so it stays in the window at least as long as 7
            while (minDeque.Count > 0 && scores[minDeque.Last!.Value] > scores[right])
            {
                minDeque.RemoveLast();
            }

            // Add the current index to the back after removing worse candidates.
            minDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 2: Insert scores[right] into the maxDeque
            // ------------------------------------------------------------
            //
            // Goal:
            // Keep maxDeque values decreasing from front to back.
            //
            // Why?
            // The largest value should always be at the front, so we can read
            // the current window maximum in O(1) time.
            //
            // How?
            // While the last element in the deque has a value smaller than the
            // current value, it can never become the maximum for this or any
            // future window that includes the current element.
            //
            // Example:
            // If deque ends with value 6 and current value is 8,
            // then 6 is useless behind 8 because:
            // - 8 is larger
            // - 8 is newer, so it remains available at least as long as 6
            while (maxDeque.Count > 0 && scores[maxDeque.Last!.Value] < scores[right])
            {
                maxDeque.RemoveLast();
            }

            // Add the current index to the back after removing worse candidates.
            maxDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 3: Shrink the window while it is invalid
            // ------------------------------------------------------------
            //
            // A window is valid if:
            // max(window) - min(window) <= limit
            //
            // Since:
            // - maxDeque.First gives the index of the current maximum
            // - minDeque.First gives the index of the current minimum
            //
            // We can check validity in O(1).
            //
            // If invalid, we move "left" forward until the window becomes valid again.
            while (scores[maxDeque.First!.Value] - scores[minDeque.First!.Value] > limit)
            {
                // Before increasing "left", we must remove indices from the fronts
                // of the deques if they are exactly leaving the window.
                //
                // Why only the front?
                // Because only the front can represent the current min or max.
                // Also, indices are stored in increasing order of time, so any
                // expired index must appear at the front before later ones.

                if (minDeque.First!.Value == left)
                {
                    minDeque.RemoveFirst();
                }

                if (maxDeque.First!.Value == left)
                {
                    maxDeque.RemoveFirst();
                }

                // Move the left boundary rightward by one position.
                left++;
            }

            // ------------------------------------------------------------
            // STEP 4: Update the best answer
            // ------------------------------------------------------------
            //
            // At this point, the window [left..right] is guaranteed valid.
            // So we compute its length and compare it with the best seen so far.
            int currentLength = right - left + 1;
            if (currentLength > bestLength)
            {
                bestLength = currentLength;
            }
        }

        // After processing all right endpoints, bestLength is the answer.
        return bestLength;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] scores1 = { 4, 7, 5, 6, 8, 3, 4 };
int limit1 = 3;
int result1 = solution.LongestReviewableBlock(scores1, limit1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 4

// Example 2
// The formal definition and the explanation imply the correct answer is 3.
int[] scores2 = { 10, 10, 10, 1, 2, 3, 4 };
int limit2 = 2;
int result2 = solution.LongestReviewableBlock(scores2, limit2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 3

// Additional quick sanity checks
int[] scores3 = { 1 };
int limit3 = 0;
Console.WriteLine("Single Element Result: " + solution.LongestReviewableBlock(scores3, limit3)); // Expected: 1

int[] scores4 = { 1, 2, 3, 4, 5 };
int limit4 = 4;
Console.WriteLine("Whole Array Valid Result: " + solution.LongestReviewableBlock(scores4, limit4)); // Expected: 5

int[] scores5 = { 8, 2, 4, 7 };
int limit5 = 4;
Console.WriteLine("Classic Test Result: " + solution.LongestReviewableBlock(scores5, limit5)); // Expected: 2