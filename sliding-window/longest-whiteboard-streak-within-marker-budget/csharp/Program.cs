/*
Title: Longest Whiteboard Streak Within Marker Budget
Difficulty: Easy
Topic: Sliding Window

Problem Description:
A teacher is writing a sequence of lesson sections on a whiteboard. The array inkUse
represents how many units of marker ink are needed for each section, in order.
Because the teacher only has a limited amount of ink for one uninterrupted writing
session, we need to find the longest contiguous group of sections that can be written
without exceeding the available ink budget maxInk.

We must return the length of the longest contiguous subarray whose sum is less than
or equal to maxInk.

Why sliding window works:
- Every value in inkUse is non-negative.
- That means when we expand the right side of the window, the sum can only stay the
  same or increase.
- If the sum becomes too large, we can move the left side forward to reduce the sum.
- Because of non-negative values, once a window is too large, moving left forward is
  the correct way to restore validity.

Examples:
1) inkUse = [2, 1, 3, 2, 1], maxInk = 5
   Longest valid length = 2

2) inkUse = [1, 0, 2, 1, 1, 0, 1], maxInk = 4
   Longest valid length = 5
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the window once by the right pointer.
    - Each element is removed from the window at most once by the left pointer.
    - Therefore, the total work is linear.

    Space Complexity: O(1)
    - We only use a few variables for pointers, running sum, and answer.
    - No extra data structures proportional to input size are needed.
    */
    public int LongestWhiteboardStreak(int[] inkUse, int maxInk)
    {
        // The left pointer marks the beginning of the current sliding window.
        // The window will always represent a contiguous subarray from left to right.
        int left = 0;

        // currentSum stores the total ink usage of the current window.
        // We use long instead of int for extra safety, even though the given constraints
        // fit in int. This is a good habit when summing many values.
        long currentSum = 0;

        // bestLength stores the maximum valid window size found so far.
        int bestLength = 0;

        // We move the right pointer from the start of the array to the end.
        // At each step, we try to include inkUse[right] in the current window.
        for (int right = 0; right < inkUse.Length; right++)
        {
            // STEP 1: Expand the window to the right.
            // We include the current section's ink cost in the running total.
            // This means our current window is now [left..right].
            currentSum += inkUse[right];

            // STEP 2: If the window is invalid, shrink it from the left.
            // A window is invalid when its total ink usage exceeds maxInk.
            //
            // Why do we use a while loop instead of an if statement?
            // Because removing just one element from the left may still leave the
            // sum too large. We must continue shrinking until the window becomes valid.
            while (currentSum > maxInk && left <= right)
            {
                // Remove the leftmost element from the running sum because
                // that section is no longer part of the window.
                currentSum -= inkUse[left];

                // Move the left boundary one step to the right.
                left++;
            }

            // STEP 3: At this point, the window [left..right] is guaranteed valid.
            // Its sum is <= maxInk, so we can safely measure its length.
            int currentLength = right - left + 1;

            // STEP 4: Update the best answer if this valid window is longer
            // than any valid window we have seen before.
            if (currentLength > bestLength)
            {
                bestLength = currentLength;
            }
        }

        // After checking all possible right endpoints, bestLength contains
        // the length of the longest valid contiguous subarray.
        return bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] inkUse1 = { 2, 1, 3, 2, 1 };
int maxInk1 = 5;
int result1 = solution.LongestWhiteboardStreak(inkUse1, maxInk1);
Console.WriteLine(result1); // Expected: 2

// Example 2
int[] inkUse2 = { 1, 0, 2, 1, 1, 0, 1 };
int maxInk2 = 4;
int result2 = solution.LongestWhiteboardStreak(inkUse2, maxInk2);
Console.WriteLine(result2); // Expected: 5

// Additional quick checks
int[] inkUse3 = { 0, 0, 0 };
int maxInk3 = 0;
int result3 = solution.LongestWhiteboardStreak(inkUse3, maxInk3);
Console.WriteLine(result3); // Expected: 3

int[] inkUse4 = { 5, 1, 1 };
int maxInk4 = 2;
int result4 = solution.LongestWhiteboardStreak(inkUse4, maxInk4);
Console.WriteLine(result4); // Expected: 2