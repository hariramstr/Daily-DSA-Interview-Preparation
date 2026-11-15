/*
Title: Longest Packing Streak With Limited Heavy Boxes
Difficulty: Medium
Topic: Arrays

Problem Description:
A warehouse packs boxes in the order they arrive. You are given an integer array weights, where weights[i] is the weight of the i-th box, and an integer limit. A box is considered heavy if its weight is strictly greater than limit.

Find the length of the longest contiguous packing streak such that the streak contains at most k heavy boxes. In other words, you may choose one continuous subarray of weights, and inside that chosen range, the number of values greater than limit must not exceed k.

Return the maximum possible length of such a streak.

This problem models a common operational constraint: the packing line can handle only a small number of heavy boxes in any uninterrupted run before requiring rebalancing. Your task is to determine the longest valid run without breaking the order of boxes.

Constraints:
- 1 <= weights.length <= 200000
- 0 <= weights[i] <= 1000000000
- 0 <= limit <= 1000000000
- 0 <= k <= weights.length

Example 1:
Input: weights = [4, 9, 3, 12, 7, 15, 2], limit = 8, k = 2
Output: 5
Explanation: Heavy boxes are 9, 12, and 15. The longest contiguous streak with at most 2 heavy boxes is [4, 9, 3, 12, 7], which has length 5.

Example 2:
Input: weights = [11, 5, 6, 13, 14, 2, 1], limit = 10, k = 1
The written explanation in the prompt is inconsistent. Let's evaluate correctly:
Heavy boxes are 11, 13, and 14.
Any valid contiguous streak may contain at most 1 heavy box.
The longest valid streaks are:
- [11, 5, 6] => length 3, heavy count 1
- [5, 6, 13] => length 3, heavy count 1
- [14, 2, 1] => length 3, heavy count 1
No contiguous streak of length 4 is valid because every such candidate contains 2 heavy boxes.
Therefore the correct output for Example 2 is 3.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is processed by the right pointer once.
    - Each element is removed from the window by the left pointer at most once.
    - Therefore, the total work is linear in the size of the array.

    Space Complexity: O(1)
    - We only use a few integer variables.
    - No extra data structures proportional to input size are needed.
    */
    public int LongestPackingStreak(int[] weights, int limit, int k)
    {
        // We will use the "sliding window" technique.
        //
        // Idea:
        // - Maintain a window [left..right] that always represents a contiguous subarray.
        // - Count how many heavy boxes are currently inside this window.
        // - Expand the window by moving "right" forward one step at a time.
        // - If the window becomes invalid (too many heavy boxes), shrink it from the left
        //   until it becomes valid again.
        // - At every step where the window is valid, record its length if it is the largest seen.
        //
        // Why sliding window works:
        // - The condition we care about is "at most k heavy boxes".
        // - When we extend the window to the right, the number of heavy boxes can only stay the same
        //   or increase by 1.
        // - If the condition breaks, the only way to fix it is to move the left boundary rightward.
        // - This gives us an efficient linear-time solution.

        int left = 0;

        // This variable stores how many heavy boxes are currently inside the window [left..right].
        int heavyCount = 0;

        // This stores the best (maximum) valid window length found so far.
        int bestLength = 0;

        // Move the right boundary from the beginning to the end of the array.
        for (int right = 0; right < weights.Length; right++)
        {
            // Step 1: Include weights[right] into the current window.
            //
            // We need to update heavyCount if this newly added box is heavy.
            // A box is heavy if its weight is strictly greater than limit.
            if (weights[right] > limit)
            {
                heavyCount++;
            }

            // Step 2: If the window is invalid, shrink it from the left.
            //
            // The window is invalid when it contains more than k heavy boxes.
            // We keep moving left forward until the window becomes valid again.
            while (heavyCount > k)
            {
                // Before removing weights[left] from the window,
                // check whether it is heavy.
                //
                // If it is heavy, removing it decreases heavyCount.
                if (weights[left] > limit)
                {
                    heavyCount--;
                }

                // Actually remove the leftmost element by advancing left.
                left++;
            }

            // Step 3: At this point, the window [left..right] is guaranteed to be valid.
            //
            // So we can compute its length and compare it with the best answer so far.
            int currentLength = right - left + 1;

            if (currentLength > bestLength)
            {
                bestLength = currentLength;
            }
        }

        // After scanning the whole array, bestLength contains the answer.
        return bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] weights1 = { 4, 9, 3, 12, 7, 15, 2 };
int limit1 = 8;
int k1 = 2;
int result1 = solution.LongestPackingStreak(weights1, limit1, k1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 5

// Example 2
// Important note:
// The prompt's stated output of 4 is inconsistent with the actual rule.
// The correct answer is 3 because any contiguous subarray of length 4 contains 2 heavy boxes.
int[] weights2 = { 11, 5, 6, 13, 14, 2, 1 };
int limit2 = 10;
int k2 = 1;
int result2 = solution.LongestPackingStreak(weights2, limit2, k2);
Console.WriteLine("Example 2 Result: " + result2); // Correct Expected: 3

// Additional quick checks
int[] weights3 = { 1, 2, 3, 4, 5 };
int limit3 = 10;
int k3 = 0;
int result3 = solution.LongestPackingStreak(weights3, limit3, k3);
Console.WriteLine("All boxes are light, Result: " + result3); // Expected: 5

int[] weights4 = { 20, 30, 40 };
int limit4 = 10;
int k4 = 1;
int result4 = solution.LongestPackingStreak(weights4, limit4, k4);
Console.WriteLine("All boxes are heavy, at most 1 allowed, Result: " + result4); // Expected: 1

int[] weights5 = { 20, 1, 20, 1, 20, 1, 1 };
int limit5 = 10;
int k5 = 2;
int result5 = solution.LongestPackingStreak(weights5, limit5, k5);
Console.WriteLine("Mixed boxes, Result: " + result5); // Expected: 5