/*
Title: Longest Annotation Span With Limited Reviewer Handoffs
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A document review platform records which reviewer handled each consecutive annotation in a long editing session.
You are given an array reviewers where reviewers[i] is the reviewer ID responsible for the i-th annotation,
in chronological order.

A handoff happens between two adjacent annotations when their reviewer IDs are different.

Your task is to find the length of the longest contiguous span of annotations that contains at most k handoffs.
In other words, within the chosen subarray, count how many indices j satisfy reviewers[j] != reviewers[j - 1]
for adjacent elements inside that subarray. That count must be less than or equal to k.

Return the maximum possible length of such a contiguous span.

This problem models finding the longest stable review segment where ownership changes are limited.
A span with repeated reviewer IDs may still contain handoffs if the reviewer changes and later changes back.

Constraints:
- 1 <= reviewers.length <= 200000
- 1 <= reviewers[i] <= 1000000000
- 0 <= k < reviewers.length

Example 1:
Input: reviewers = [5,5,2,2,2,7,7,2], k = 2
Output: 7
Explanation: The span [5,5,2,2,2,7,7] has exactly 2 handoffs: 5->2 and 2->7.
Its length is 7. Any longer span would include the final 7->2 handoff, making 3 handoffs.

Example 2:
Input: reviewers = [1,3,3,4,4,4,2,2,5], k = 1
Output: 5
Explanation: One valid longest span is [3,3,4,4,4], which contains a single handoff: 3->4.
No contiguous span of length 6 or more has at most 1 handoff.

Key Sliding Window Observation:
- For any current window [left..right], the number of handoffs inside that window is simply the number of
  adjacent positions i in that range where reviewers[i] != reviewers[i - 1].
- When we extend the window to the right by one element, only one new adjacent pair can be added:
  the pair (right - 1, right).
- When we move the left boundary to the right by one element, only one adjacent pair can be removed:
  the pair (left, left + 1), which was previously inside the window.
- Because of this, we can maintain the handoff count in O(1) time per movement of either pointer,
  giving an overall O(n) solution.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each index is visited by the right pointer once.
    - Each index is also moved past by the left pointer at most once.
    - Therefore, the total amount of work is linear in the size of the array.

    Space Complexity: O(1)
    - We only store a few integer variables.
    - No extra data structures proportional to input size are used.
    */
    public int LongestSpanWithLimitedHandoffs(int[] reviewers, int k)
    {
        // The array length is at least 1 by the problem constraints,
        // but this guard makes the method more robust and beginner-friendly.
        if (reviewers == null || reviewers.Length == 0)
        {
            return 0;
        }

        // "left" marks the start of our current sliding window.
        int left = 0;

        // "handoffs" stores how many reviewer changes exist inside the current window [left..right].
        // More precisely, it counts how many indices j in the window satisfy:
        // reviewers[j] != reviewers[j - 1], where both j and j - 1 are inside the window.
        int handoffs = 0;

        // "best" stores the maximum valid window length we have seen so far.
        int best = 1;

        // We expand the window by moving "right" from left to right across the array.
        for (int right = 0; right < reviewers.Length; right++)
        {
            // STEP 1: Add the new element at position "right" into the window.
            //
            // Why do we check right > 0?
            // Because a handoff is defined between adjacent elements.
            // The first element has no previous neighbor, so it cannot create a handoff by itself.
            //
            // When we extend the window to include reviewers[right], the only NEW adjacent pair
            // that could affect the handoff count is (right - 1, right).
            //
            // If those two reviewer IDs are different, then we have introduced one additional handoff
            // into the current window.
            if (right > 0 && reviewers[right] != reviewers[right - 1])
            {
                handoffs++;
            }

            // STEP 2: If the window now has too many handoffs, shrink it from the left
            // until it becomes valid again.
            //
            // Why is this necessary?
            // The problem requires at most k handoffs.
            // If handoffs > k, then the current window is invalid and cannot be considered.
            //
            // Why can we safely move left forward?
            // Because we are looking for a contiguous subarray, and shrinking from the left
            // is the standard way to restore validity in a sliding window.
            while (handoffs > k)
            {
                // Before we move "left" forward, we need to remove the effect of the adjacent pair
                // that starts at "left", namely (left, left + 1), because that pair will no longer
                // be fully inside the window after left is incremented.
                //
                // Example:
                // Current window is [left..right].
                // If we move left to left + 1, then the pair (left, left + 1) leaves the window.
                // If that pair represented a handoff, we must subtract it.
                //
                // We check left < right because if left == right, the window has only one element,
                // so there is no adjacent pair to remove.
                if (left < right && reviewers[left] != reviewers[left + 1])
                {
                    handoffs--;
                }

                // Now actually shrink the window.
                left++;
            }

            // STEP 3: At this point, the window [left..right] is guaranteed to be valid
            // because handoffs <= k.
            //
            // We compute its length and update the best answer if this window is longer
            // than any valid window we have seen before.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // After scanning the entire array, "best" holds the maximum valid span length.
        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// reviewers = [5,5,2,2,2,7,7,2], k = 2
// Expected output: 7
int[] reviewers1 = { 5, 5, 2, 2, 2, 7, 7, 2 };
int k1 = 2;
int result1 = solution.LongestSpanWithLimitedHandoffs(reviewers1, k1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2:
// reviewers = [1,3,3,4,4,4,2,2,5], k = 1
// Expected output: 5
int[] reviewers2 = { 1, 3, 3, 4, 4, 4, 2, 2, 5 };
int k2 = 1;
int result2 = solution.LongestSpanWithLimitedHandoffs(reviewers2, k2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional quick sanity checks

// All same reviewer, no handoffs at all.
// Entire array should be valid for any k >= 0.
int[] reviewers3 = { 9, 9, 9, 9 };
int k3 = 0;
int result3 = solution.LongestSpanWithLimitedHandoffs(reviewers3, k3);
Console.WriteLine($"Sanity Check 1 Result: {result3}");

// Alternating reviewers with k = 0.
// Longest valid span should be 1 because every adjacent pair changes reviewer.
int[] reviewers4 = { 1, 2, 1, 2, 1 };
int k4 = 0;
int result4 = solution.LongestSpanWithLimitedHandoffs(reviewers4, k4);
Console.WriteLine($"Sanity Check 2 Result: {result4}");