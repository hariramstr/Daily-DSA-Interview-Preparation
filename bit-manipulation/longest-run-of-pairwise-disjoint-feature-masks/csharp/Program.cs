/*
Title: Longest Run of Pairwise Disjoint Feature Masks
Difficulty: Medium
Topic: Bit Manipulation

Problem Description:
You are given an array masks where masks[i] is a non-negative integer representing the enabled feature bits
of the i-th software build in chronological order.

A contiguous run of builds is called compatible if no bit position is enabled in more than one build inside
that run. In other words, for every pair of different indices a and b within the same run:

    masks[a] & masks[b] == 0

Your task is to return the length of the longest compatible contiguous run.

Important clarification:
This is NOT the same as checking whether the bitwise AND of the whole window is zero.
A run is valid only when every bit appears at most once across the entire window.

Example:
- [1, 2, 4] is compatible
- [3, 4, 1] is NOT compatible, because:
    3 = 011
    1 = 001
  bit 0 appears in both numbers

Constraints:
- 1 <= masks.length <= 100000
- 0 <= masks[i] <= 10^9
- masks[i] fits in a 32-bit signed integer
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n), where n is the length of the array.
    - Each element enters the sliding window once and leaves the sliding window at most once.

    Space Complexity:
    - O(1)
    - We only store a few integer variables regardless of input size.

    Beginner-friendly idea:
    We use a sliding window [left..right] that always stays valid.
    We also keep a bitmask named "usedBits" representing all bits currently present in the window.

    Why this works:
    - If the window is valid, then no bit is repeated across different numbers in the window.
    - That means the OR of all numbers in the window exactly tells us which bits are currently occupied.
    - Before adding a new number masks[right], we check whether it overlaps with usedBits:
          (usedBits & masks[right]) != 0
      If it does, then some bit would be repeated, so the window is invalid.
    - We then move left forward, removing numbers from the window until the overlap disappears.
    - Once there is no overlap, we safely add masks[right] into the window.

    Why removing with XOR is safe:
    - In a valid window, each bit appears in at most one number.
    - So when we remove masks[left], every bit set in that number belongs uniquely to it inside the window.
    - Therefore:
          usedBits ^= masks[left]
      correctly removes exactly those bits from usedBits.
    */
    public int LongestCompatibleRun(int[] masks)
    {
        // "left" is the start index of our current sliding window.
        int left = 0;

        // "best" stores the maximum valid window length we have seen so far.
        int best = 0;

        // "usedBits" stores the union of all bits currently present in the window.
        // Because the window is always maintained as valid, each bit appears at most once,
        // so this single integer is enough to represent the whole window's used bit positions.
        int usedBits = 0;

        // Expand the window one element at a time by moving "right" from left to right.
        for (int right = 0; right < masks.Length; right++)
        {
            // Current number we want to add into the window.
            int current = masks[right];

            // If current shares any bit with usedBits, then adding it would create a conflict.
            // Example:
            //   usedBits = 0101
            //   current  = 0001
            //   overlap  = 0001 != 0
            //
            // That means some bit is already used by another number in the window,
            // so the window would no longer be pairwise disjoint.
            //
            // We must shrink the window from the left until the conflict disappears.
            while ((usedBits & current) != 0)
            {
                // Remove masks[left] from the window.
                //
                // Why XOR?
                // In a valid window, each bit belongs to exactly one number.
                // So toggling those bits off with XOR removes them correctly.
                usedBits ^= masks[left];

                // Move the left boundary rightward because that element is no longer in the window.
                left++;
            }

            // At this point, current has no overlapping bits with the window.
            // So it is safe to include it.
            //
            // We use OR because we want to mark all bits from current as now occupied.
            usedBits |= current;

            // The current window [left..right] is valid.
            int windowLength = right - left + 1;

            // Update the best answer if this valid window is longer than any previous one.
            if (windowLength > best)
            {
                best = windowLength;
            }
        }

        return best;
    }
}

// Demo code:
// Creates sample inputs, calls the solution, and prints the results.

var solution = new Solution();

// Example 1:
// masks = [1, 2, 4, 3, 8]
// Longest compatible run is [1, 2, 4], length = 3
int[] masks1 = { 1, 2, 4, 3, 8 };
int result1 = solution.LongestCompatibleRun(masks1);
Console.WriteLine(result1);

// Example 2:
// masks = [5, 1, 2, 8, 4]
// Longest compatible run is [1, 2, 8, 4], length = 4
int[] masks2 = { 5, 1, 2, 8, 4 };
int result2 = solution.LongestCompatibleRun(masks2);
Console.WriteLine(result2);

// Additional quick checks:
int[] masks3 = { 0, 0, 0 };
Console.WriteLine(solution.LongestCompatibleRun(masks3)); // 3, because zero has no set bits and never conflicts

int[] masks4 = { 3, 4, 1 };
Console.WriteLine(solution.LongestCompatibleRun(masks4)); // 2, because [3,4] is valid, but [3,4,1] is not

int[] masks5 = { 7 };
Console.WriteLine(solution.LongestCompatibleRun(masks5)); // 1