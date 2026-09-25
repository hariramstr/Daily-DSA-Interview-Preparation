/*
Title: Shortest Sorted Window to Merge Daily Rankings
Difficulty: Medium
Topic: Two Pointers

Problem Description:
A product team stores yesterday's leaderboard scores in a non-decreasing integer array `yesterday`
and today's newly processed score updates in another non-decreasing integer array `today`.

The two arrays are already sorted individually, but the team wants to publish a single combined
ranking stream without fully merging both arrays.

Your task is to find the length of the shortest contiguous window in the virtual merged array
(the array that would result from merging `yesterday` and `today` in sorted order) whose sum
is at least `target`.

You are not allowed to explicitly build the merged array if you want an efficient solution for
large inputs. Instead, design an algorithm that uses the sorted structure of both arrays and a
two-pointer / sliding window strategy over the virtual merge.

Return the minimum possible window length. If no contiguous window in the merged order has sum
at least `target`, return `-1`.

Notes:
- All values are positive integers, which guarantees that expanding the window increases or
  preserves its sum and shrinking it decreases or preserves its sum.
- The window must be contiguous in the merged sorted order, not separately inside one input array.
- The merged array is conceptual; an optimal solution should process elements as if they were
  being merged on the fly.

Constraints:
- 1 <= yesterday.length, today.length <= 10^5
- 1 <= yesterday[i], today[i] <= 10^4
- Both `yesterday` and `today` are sorted in non-decreasing order
- 1 <= target <= 10^9
- Expected time complexity: O(n + m) where n = yesterday.length and m = today.length
- Expected extra space: O(1) or O(log(n+m)) depending on implementation details

Example 1:
Input: yesterday = [1, 4, 7], today = [2, 3, 8], target = 11
Output: 2
Explanation:
The virtual merged array is [1, 2, 3, 4, 7, 8].
The shortest contiguous window with sum at least 11 is [3, 8] or [4, 7], both of length 2.

Example 2:
Input: yesterday = [2, 2, 5], today = [1, 6, 9], target = 15
Output: 2
Explanation:
The virtual merged array is [1, 2, 2, 5, 6, 9].
A shortest valid window is [6, 9], whose sum is 15, so the answer is 2.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    // Time Complexity:
    // O(n + m)
    // Explanation:
    // 1. We conceptually merge the two sorted arrays once from left to right.
    // 2. Each merged element is added to the sliding window exactly one time.
    // 3. Each merged element is removed from the sliding window at most one time.
    // Therefore, the total work is linear in the total number of elements.
    //
    // Space Complexity:
    // O(n + m) in this implementation
    // Explanation:
    // We do NOT build the fully merged array.
    // However, to support shrinking the left side of the sliding window while we continue
    // streaming elements from the right side, we store only the current window's values
    // inside a queue. In the worst case, the window may contain all elements.
    //
    // Note for learners:
    // If we were allowed random access to a fully merged array, shrinking would be easy,
    // but that would explicitly build the merged array. Here we process the merge on the fly
    // and keep only what the current window needs.
    public int ShortestSortedWindow(int[] yesterday, int[] today, int target)
    {
        // These two pointers are the classic merge pointers.
        // i walks through "yesterday"
        // j walks through "today"
        int i = 0;
        int j = 0;

        // This queue stores the actual values currently inside our sliding window.
        // Why do we need it?
        // Because when the window sum becomes large enough, we want to shrink the window
        // from the left side. To do that, we must know the leftmost value currently in
        // the window so we can subtract it from the running sum.
        //
        // Since we are reading the merged order as a stream, a queue is a natural fit:
        // - Enqueue when the right side expands
        // - Dequeue when the left side shrinks
        Queue<int> window = new Queue<int>();

        // We use long for the running sum because:
        // - target can be up to 1e9
        // - total window sum can exceed int range in larger cases
        long currentSum = 0;

        // This tracks the best (smallest) valid window length found so far.
        // We start with int.MaxValue as a sentinel meaning "no answer found yet".
        int bestLength = int.MaxValue;

        // We continue until both arrays are fully consumed.
        // At each step, we choose the next smallest element exactly like merge sort.
        while (i < yesterday.Length || j < today.Length)
        {
            int nextValue;

            // Step 1: Select the next value in the virtual merged order.
            //
            // Why this is necessary:
            // The problem defines contiguity with respect to the merged sorted array.
            // So we must process elements in exactly that order.
            //
            // Merge logic:
            // - If one array is exhausted, we must take from the other.
            // - Otherwise, take the smaller current value.
            // - If equal, taking from either first is fine because both values are the same
            //   and the merged order remains valid.
            if (i < yesterday.Length && j < today.Length)
            {
                if (yesterday[i] <= today[j])
                {
                    nextValue = yesterday[i];
                    i++;
                }
                else
                {
                    nextValue = today[j];
                    j++;
                }
            }
            else if (i < yesterday.Length)
            {
                nextValue = yesterday[i];
                i++;
            }
            else
            {
                nextValue = today[j];
                j++;
            }

            // Step 2: Expand the sliding window to the right by adding the new merged value.
            //
            // Why this is necessary:
            // We are searching for a contiguous window whose sum is at least target.
            // The standard sliding window approach for positive numbers is:
            // - Expand right until the sum becomes large enough
            // - Then shrink left as much as possible while keeping the sum >= target
            window.Enqueue(nextValue);
            currentSum += nextValue;

            // Step 3: While the current window is valid (sum >= target),
            // try to shrink it from the left to make it as short as possible.
            //
            // Why this works:
            // All numbers are positive.
            // That means:
            // - Expanding the window can only increase the sum
            // - Shrinking the window can only decrease the sum
            //
            // This monotonic behavior is exactly why sliding window is correct here.
            while (currentSum >= target)
            {
                // The current window is valid, so record its length if it is the best so far.
                if (window.Count < bestLength)
                {
                    bestLength = window.Count;
                }

                // Remove the leftmost element to see whether we can still remain valid
                // with an even shorter window.
                int removed = window.Dequeue();
                currentSum -= removed;
            }
        }

        // If bestLength was never updated, then no valid window exists.
        return bestLength == int.MaxValue ? -1 : bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] yesterday1 = { 1, 4, 7 };
int[] today1 = { 2, 3, 8 };
int target1 = 11;
int result1 = solution.ShortestSortedWindow(yesterday1, today1, target1);
Console.WriteLine(result1); // Expected: 2

// Example 2
int[] yesterday2 = { 2, 2, 5 };
int[] today2 = { 1, 6, 9 };
int target2 = 15;
int result2 = solution.ShortestSortedWindow(yesterday2, today2, target2);
Console.WriteLine(result2); // Expected: 2

// Additional quick sanity check
int[] yesterday3 = { 1, 1, 1 };
int[] today3 = { 1, 1 };
int target3 = 10;
int result3 = solution.ShortestSortedWindow(yesterday3, today3, target3);
Console.WriteLine(result3); // Expected: -1