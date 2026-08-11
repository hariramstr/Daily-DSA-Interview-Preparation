/*
Title: Longest Price Feed Window With Limited Direction Reversals

Problem Description:
You are given an integer array prices where prices[i] is the observed price of an asset at time i, and an integer k.
Consider any contiguous window prices[l..r]. For every adjacent pair inside the window, define its direction as:

- increasing if prices[i] < prices[i+1]
- decreasing if prices[i] > prices[i+1]
- flat if prices[i] == prices[i+1]

Flat steps do not contribute to direction changes.

A window is called smooth if, after ignoring all flat steps, the sequence of remaining directions changes
between increasing and decreasing at most k times.

Example:
If the non-flat directions are [+,+,-,-,+], then the window has 2 direction reversals.

Return the length of the longest smooth contiguous window.

Key idea:
Instead of reasoning directly on values, we reason on the comparison sequence between adjacent values:
- +1 for increasing
- -1 for decreasing
-  0 for flat

Then for any window in the original array, the number of direction reversals is exactly the number of times
consecutive NON-ZERO comparisons inside that window switch sign.

We solve this in linear time using a sliding window over the comparison array.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We build the comparison array in O(n).
    - We scan it once with a sliding window.
    - Each pointer only moves forward, so total work is linear.

    Space Complexity: O(n)
    - We store the comparison array of length n - 1.
    - We also store the indices of non-zero comparisons currently inside the window.
    */
    public int LongestSmoothWindow(int[] prices, int k)
    {
        // If there is only one price, the only possible window is that single element.
        // A single element has no adjacent pairs, so it is always valid.
        if (prices == null || prices.Length == 0)
        {
            return 0;
        }

        if (prices.Length == 1)
        {
            return 1;
        }

        int n = prices.Length;

        // Step 1:
        // Convert the original price array into a direction/comparison array.
        //
        // cmp[i] describes the relationship between prices[i] and prices[i + 1]:
        //   +1 => increasing
        //   -1 => decreasing
        //    0 => flat
        //
        // Why this helps:
        // The problem is about direction changes between adjacent comparisons.
        // That is much easier to track on this derived array than on the original prices.
        int[] cmp = new int[n - 1];
        for (int i = 0; i < n - 1; i++)
        {
            if (prices[i] < prices[i + 1])
            {
                cmp[i] = 1;
            }
            else if (prices[i] > prices[i + 1])
            {
                cmp[i] = -1;
            }
            else
            {
                cmp[i] = 0;
            }
        }

        // We will maintain a sliding window on cmp:
        // current window is cmp[left..right]
        //
        // This corresponds to the original prices window:
        // prices[left..right + 1]
        //
        // Therefore:
        // original window length = (right - left + 1 comparisons) + 1 prices
        //                        = right - left + 2
        //
        // We need to know how many direction reversals are inside cmp[left..right],
        // ignoring zero entries.
        //
        // To do that efficiently, we keep a list of indices of non-zero comparisons
        // currently inside the window.
        //
        // Example:
        // cmp window = [0, +1, 0, -1, -1, 0, +1]
        // non-zero indices might be [1, 3, 4, 6]
        //
        // Reversals happen between consecutive non-zero entries when their signs differ:
        // +1 to -1 => reversal
        // -1 to -1 => no reversal
        // -1 to +1 => reversal
        //
        // We store those non-zero indices in a queue-like structure using LinkedList<int>.
        // Why LinkedList?
        // - We append new non-zero indices at the end as right expands.
        // - We remove old non-zero indices from the front as left moves forward.
        //
        // We also maintain:
        // reversals = number of sign changes between consecutive non-zero comparisons
        // currently inside the window.
        int left = 0;
        int best = 1; // At minimum, any single element window is valid.
        int reversals = 0;

        LinkedList<int> nonZeroIndices = new LinkedList<int>();

        for (int right = 0; right < cmp.Length; right++)
        {
            // Step 2:
            // Expand the window by including cmp[right].
            //
            // If cmp[right] is zero, it does not affect direction reversals at all,
            // because flat steps are ignored by the problem statement.
            //
            // If cmp[right] is non-zero, we compare it with the previous non-zero
            // comparison already in the window (if one exists).
            //
            // If their signs differ, then adding this new non-zero comparison creates
            // exactly one new reversal at the boundary between those two non-zero steps.
            if (cmp[right] != 0)
            {
                if (nonZeroIndices.Count > 0)
                {
                    int previousNonZeroIndex = nonZeroIndices.Last!.Value;

                    // If the previous non-zero sign differs from the new sign,
                    // then we have introduced one additional reversal.
                    if (cmp[previousNonZeroIndex] != cmp[right])
                    {
                        reversals++;
                    }
                }

                nonZeroIndices.AddLast(right);
            }

            // Step 3:
            // If the window has too many reversals, shrink it from the left
            // until it becomes valid again.
            //
            // The tricky part:
            // When left moves forward, we may remove cmp[left] from the window.
            // If cmp[left] is zero, nothing changes.
            //
            // If cmp[left] is non-zero and it is the first non-zero comparison in the window,
            // then removing it may reduce the reversal count by 1, but only if there is
            // another non-zero comparison after it and their signs were different.
            //
            // Why only the first non-zero matters?
            // Because left moves one step at a time. The only comparison that can leave
            // the window at that moment is cmp[left]. If it is not non-zero, it cannot
            // affect the non-zero direction sequence.
            while (reversals > k)
            {
                if (cmp[left] != 0)
                {
                    // Since cmp[left] is leaving the window, it should be the first
                    // non-zero index currently stored.
                    //
                    // Before removing it, check whether it formed a reversal with the
                    // next non-zero comparison in the window.
                    //
                    // If yes, removing this first non-zero comparison removes exactly
                    // one reversal from the window.
                    LinkedListNode<int> firstNode = nonZeroIndices.First!;

                    if (firstNode.Value != left)
                    {
                        // This should never happen if our bookkeeping is correct.
                        // We keep this guard only as a sanity check for readability.
                        throw new InvalidOperationException("Internal bookkeeping error.");
                    }

                    LinkedListNode<int>? secondNode = firstNode.Next;
                    if (secondNode != null)
                    {
                        if (cmp[firstNode.Value] != cmp[secondNode.Value])
                        {
                            reversals--;
                        }
                    }

                    nonZeroIndices.RemoveFirst();
                }

                left++;
            }

            // Step 4:
            // At this point, cmp[left..right] is valid:
            // it contains at most k direction reversals.
            //
            // Convert this comparison-window length back to original prices-window length.
            //
            // If cmp window is from left to right inclusive, then it spans:
            // prices[left..right + 1]
            //
            // So the number of prices is:
            // (right + 1) - left + 1 = right - left + 2
            int currentLength = right - left + 2;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        return best;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] prices1 = { 5, 7, 9, 8, 6, 6, 10, 12 };
int k1 = 1;
int result1 = solution.LongestSmoothWindow(prices1, k1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2
int[] prices2 = { 4, 4, 4, 3, 2, 5, 7, 6, 1 };
int k2 = 2;
int result2 = solution.LongestSmoothWindow(prices2, k2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional quick checks
int[] prices3 = { 1 };
int k3 = 0;
Console.WriteLine($"Single element Result: {solution.LongestSmoothWindow(prices3, k3)}");

int[] prices4 = { 3, 3, 3, 3 };
int k4 = 0;
Console.WriteLine($"All flat Result: {solution.LongestSmoothWindow(prices4, k4)}");

int[] prices5 = { 1, 2, 3, 4, 5 };
int k5 = 0;
Console.WriteLine($"Always increasing Result: {solution.LongestSmoothWindow(prices5, k5)}");

int[] prices6 = { 1, 3, 2, 4, 3, 5 };
int k6 = 2;
Console.WriteLine($"Alternating with k=2 Result: {solution.LongestSmoothWindow(prices6, k6)}");