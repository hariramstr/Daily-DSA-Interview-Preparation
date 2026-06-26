/*
Title: Minimum Scanner Range for Warehouse Aisle Labels
Difficulty: Medium
Topic: Binary Search

Problem Description:
A warehouse is organized as a long straight line of aisles, numbered by their distance from the entrance.
Some aisles contain fixed barcode scanners, and some aisles contain labels that must be readable by at least one scanner.

You are given two integer arrays: scanners and labels.
- scanners[i] is the position of the i-th scanner
- labels[j] is the position of the j-th label

A scanner with range r can read every label whose position is within distance r from that scanner.

Your task is to find the minimum integer range r such that every label is readable by at least one scanner.

Positions may be unsorted and may include duplicates.
A scanner and a label can appear at the same position.
You must return the smallest possible range that covers all labels.

A common interview approach is to sort the positions and use binary search on the answer.
For a candidate range r, determine whether all labels can be covered efficiently.

Constraints:
- 1 <= scanners.length, labels.length <= 2 * 10^5
- 0 <= scanners[i], labels[j] <= 10^9
- The answer fits in a 32-bit signed integer.

Example 1:
Input: scanners = [2, 10], labels = [1, 5, 11]
Output: 3

Explanation:
With range 3, scanner 2 covers labels at 1 and 5, and scanner 10 covers label 11.
Range 2 is not enough because label 5 would be uncovered.

Example 2:
Input: scanners = [15, 4, 20], labels = [3, 8, 14, 21]
Output: 4

Explanation:
After sorting, the nearest scanner distances for the labels are 1, 4, 1, and 1.
Therefore the minimum range that covers every label is 4.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting scanners: O(S log S)
    - Sorting labels:   O(L log L)
    - Each feasibility check: O(S + L)
    - Binary search over answer range: O(log M), where M is the search space of the answer
    Total: O(S log S + L log L + (S + L) log M)

    Space Complexity:
    - If we sort the input arrays in place, the extra algorithmic space is O(1)
      excluding the internal stack/implementation details of sorting.
    */
    public int MinimumScannerRange(int[] scanners, int[] labels)
    {
        // Step 1:
        // Sort both arrays.
        //
        // Why is sorting necessary?
        // Because once both position lists are in increasing order, we can scan from left to right
        // and reason about coverage intervals in a very efficient way.
        //
        // For a fixed scanner position x and range r, that scanner covers the interval:
        // [x - r, x + r]
        //
        // If scanners are sorted, these intervals also appear in left-to-right order by center.
        // If labels are sorted too, we can greedily walk through labels and check whether each one
        // falls inside some scanner's coverage interval.
        Array.Sort(scanners);
        Array.Sort(labels);

        // Step 2:
        // Set up binary search on the answer.
        //
        // We are searching for the smallest integer r such that "all labels are covered" is true.
        //
        // This works because the feasibility condition is monotonic:
        // - If range r is enough, then any larger range is also enough.
        // - If range r is not enough, then any smaller range is also not enough.
        //
        // Therefore, the answer space looks like:
        // false false false ... false true true true ...
        // and binary search is the perfect tool for finding the first true value.
        int left = 0;

        // A safe upper bound:
        // Since positions are between 0 and 1e9, the maximum possible distance between a label
        // and a scanner is at most 1e9. The problem also guarantees the answer fits in 32-bit int.
        int right = 1_000_000_000;

        // Standard binary search for the first feasible value.
        while (left < right)
        {
            // Use this form to avoid overflow:
            int mid = left + (right - left) / 2;

            // Check whether range = mid is enough to cover every label.
            if (CanCoverAllLabels(scanners, labels, mid))
            {
                // mid works, so the answer is <= mid.
                // Keep searching on the left half to find the minimum possible working range.
                right = mid;
            }
            else
            {
                // mid does not work, so the answer must be > mid.
                left = mid + 1;
            }
        }

        // At the end, left == right and points to the smallest feasible range.
        return left;
    }

    private bool CanCoverAllLabels(int[] scanners, int[] labels, int range)
    {
        // This method answers:
        // "If every scanner has the given range, can all labels be covered?"
        //
        // We use a two-pointer technique:
        // - i points to the current scanner
        // - j points to the current label we still need to cover
        //
        // Because both arrays are sorted, we can process everything from left to right exactly once.
        int i = 0; // scanner pointer
        int j = 0; // label pointer

        while (i < scanners.Length && j < labels.Length)
        {
            // Compute the coverage interval of the current scanner.
            //
            // We use long here for safety when doing subtraction/addition,
            // even though the final answer fits in int.
            // This avoids any accidental overflow in intermediate arithmetic.
            long leftReach = (long)scanners[i] - range;
            long rightReach = (long)scanners[i] + range;

            // Case 1:
            // The current label is to the LEFT of this scanner's coverage interval.
            //
            // That means:
            // labels[j] < leftReach
            //
            // Since scanners are sorted, all future scanners are at the same position or farther right.
            // Their coverage intervals will not start earlier in a way that can rescue this label.
            //
            // Intuition:
            // If even the current scanner's left boundary is already to the right of the label,
            // then this label is too far left to be covered by this scanner.
            // And because future scanners are not to the left, they also cannot cover it.
            //
            // Therefore, coverage is impossible for this range.
            if (labels[j] < leftReach)
            {
                return false;
            }

            // Case 2:
            // The current label lies inside this scanner's coverage interval.
            //
            // That means:
            // leftReach <= labels[j] <= rightReach
            //
            // Since labels are sorted, not only this label but possibly several consecutive labels
            // may also be covered by the same scanner. We should consume all of them now.
            if (labels[j] <= rightReach)
            {
                // Move j forward while labels remain covered by the current scanner.
                //
                // This greedy step is correct because:
                // - Any label already covered does not need more work.
                // - It is always safe to let the current scanner cover as many labels as possible.
                while (j < labels.Length && labels[j] >= leftReach && labels[j] <= rightReach)
                {
                    j++;
                }

                // After exhausting this scanner's useful coverage, move to the next scanner.
                i++;
            }
            else
            {
                // Case 3:
                // labels[j] > rightReach
                //
                // The current label is to the RIGHT of this scanner's coverage interval.
                // So this scanner cannot help with that label or any later label.
                //
                // Therefore, we move to the next scanner and try again.
                i++;
            }
        }

        // If j reached the end, every label was covered.
        // Otherwise, some labels remain uncovered.
        return j == labels.Length;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// scanners = [2, 10], labels = [1, 5, 11]
// Expected output: 3
int[] scanners1 = { 2, 10 };
int[] labels1 = { 1, 5, 11 };
int result1 = solution.MinimumScannerRange(scanners1, labels1);
Console.WriteLine(result1);

// Example 2:
// scanners = [15, 4, 20], labels = [3, 8, 14, 21]
// Expected output: 4
int[] scanners2 = { 15, 4, 20 };
int[] labels2 = { 3, 8, 14, 21 };
int result2 = solution.MinimumScannerRange(scanners2, labels2);
Console.WriteLine(result2);