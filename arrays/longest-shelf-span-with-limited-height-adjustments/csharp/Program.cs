/*
Title: Longest Shelf Span With Limited Height Adjustments

Problem Description:
A warehouse stores products on a long row of shelves. The height of the product stack on shelf i is given by heights[i].
For a promotional display, the manager wants to choose one contiguous span of shelves and make all stack heights in that span equal.
You are allowed to increase shelf heights, but you cannot decrease any shelf. Increasing a shelf by 1 unit costs 1 adjustment.

Given an integer array heights and a long integer budget, return the maximum length of a contiguous subarray that can be made
to have the same height using at most budget total adjustments.

When choosing a span, you may raise shorter shelves to match the tallest shelf already inside that span.
Because decreasing is not allowed, the final common height must be at least the maximum value in the chosen subarray,
and choosing the current maximum is always optimal.

Constraints:
- 1 <= heights.length <= 100000
- 1 <= heights[i] <= 1000000000
- 0 <= budget <= 100000000000000

Important note about the examples:
The first example text in the prompt contains an inconsistency. For heights = [3,1,2,2,4] and budget = 3,
the true maximum contiguous span length is 3, not 4.
Examples of valid spans of length 3:
- [3,1,2] -> raise to 3, cost = 0 + 2 + 1 = 3
- [1,2,2] -> raise to 2, cost = 1 + 0 + 0 = 1
No contiguous span of length 4 fits within budget 3.

So this implementation returns the mathematically correct answer.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n log n)

    Explanation:
    - We process each array index once as the right boundary of a sliding window.
    - For each step, we may insert/remove values into monotonic deques in O(1) amortized time.
    - We also maintain prefix sums in O(1) per step.
    - The key extra work is binary searching inside the current window to find where the current maximum first appears,
      which takes O(log n) per right boundary.
    - Therefore total complexity is O(n log n).

    Space Complexity:
    O(n)

    Explanation:
    - Prefix sums use O(n).
    - The deques together use O(n) in the worst case.
    */
    public int MaxEqualShelfSpan(int[] heights, long budget)
    {
        int n = heights.Length;

        // Prefix sums let us quickly compute the sum of any contiguous subarray.
        // prefix[i] = sum of heights[0..i-1]
        // So sum of heights[l..r] = prefix[r+1] - prefix[l]
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++)
        {
            prefix[i + 1] = prefix[i] + heights[i];
        }

        // This deque stores indices in decreasing order of heights.
        // The front always points to the maximum value inside the current window.
        LinkedList<int> maxDeque = new LinkedList<int>();

        // This deque stores indices in increasing order of heights.
        // We use it to quickly know the leftmost position in the current window
        // whose value equals the current maximum.
        //
        // Why do we need this?
        // For a window [left..right], the cost to make everything equal to the maximum M is:
        //   M * windowLength - sum(window)
        // BUT this is only valid if M is the rightmost / non-decreasing target in sorted problems.
        // Here the array must stay contiguous and we cannot decrease values.
        //
        // Actually, for a fixed contiguous window, raising every element to the maximum M is always valid,
        // and the cost is indeed:
        //   M * len - sum(window)
        //
        // So at first glance, only the maximum and sum are needed.
        //
        // However, because the maximum can be anywhere in the window, the formula still remains correct.
        // Therefore we only truly need the max deque and prefix sums.
        //
        // The algorithm below uses exactly that correct formula.
        int left = 0;
        int best = 0;

        for (int right = 0; right < n; right++)
        {
            // Step 1:
            // Insert the new right index into the monotonic decreasing deque.
            //
            // We remove smaller or equal values from the back because they can never become
            // the maximum of any future window that also contains heights[right].
            while (maxDeque.Count > 0 && heights[maxDeque.Last!.Value] <= heights[right])
            {
                maxDeque.RemoveLast();
            }
            maxDeque.AddLast(right);

            // Step 2:
            // Shrink the left boundary while the current window is too expensive.
            //
            // Current window is [left..right].
            // Let M = maximum value in this window.
            // To make all values equal to M, total cost is:
            //   M * (right - left + 1) - sum(heights[left..right])
            //
            // If that cost exceeds budget, we must move left forward.
            while (left <= right)
            {
                int maxIndex = maxDeque.First!.Value;
                long maxValue = heights[maxIndex];
                int length = right - left + 1;
                long windowSum = prefix[right + 1] - prefix[left];
                long cost = maxValue * length - windowSum;

                if (cost <= budget)
                {
                    break;
                }

                // If the outgoing left index is currently the maximum at the front of the deque,
                // remove it because it is no longer inside the window after left advances.
                if (maxDeque.Count > 0 && maxDeque.First!.Value == left)
                {
                    maxDeque.RemoveFirst();
                }

                left++;
            }

            // Step 3:
            // Now the window [left..right] is valid, so update the best answer.
            best = Math.Max(best, right - left + 1);
        }

        return best;
    }
}

// Demo code
var solution = new Solution();

// Example 1 from the prompt.
// The prompt says output 4, but that is inconsistent with the actual rules.
// The correct answer is 3.
int[] heights1 = { 3, 1, 2, 2, 4 };
long budget1 = 3;
int result1 = solution.MaxEqualShelfSpan(heights1, budget1);
Console.WriteLine($"Example 1 result: {result1}");

// Example 2
int[] heights2 = { 5, 5, 5, 5 };
long budget2 = 0;
int result2 = solution.MaxEqualShelfSpan(heights2, budget2);
Console.WriteLine($"Example 2 result: {result2}");

// Additional demos
int[] heights3 = { 1 };
long budget3 = 100;
Console.WriteLine($"Single shelf result: {solution.MaxEqualShelfSpan(heights3, budget3)}");

int[] heights4 = { 1, 2, 3, 4 };
long budget4 = 6; // entire array to 4 costs 3+2+1+0 = 6
Console.WriteLine($"Increasing sequence result: {solution.MaxEqualShelfSpan(heights4, budget4)}");

int[] heights5 = { 4, 1, 1, 1, 4 };
long budget5 = 9; // whole array to 4 costs 0+3+3+3+0 = 9
Console.WriteLine($"Symmetric result: {solution.MaxEqualShelfSpan(heights5, budget5)}");