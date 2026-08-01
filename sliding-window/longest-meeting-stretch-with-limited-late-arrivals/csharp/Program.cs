/*
Title: Longest Meeting Stretch With Limited Late Arrivals

Problem Description:
A company tracks a day of back-to-back meeting slots using a binary array arrivals,
where arrivals[i] = 1 means the attendee arrived on time for slot i, and arrivals[i] = 0
means they arrived late for that slot.

Management wants to identify the longest contiguous stretch of meeting slots that can still
be treated as a "reliable attendance block" if they are willing to excuse at most k late
arrivals inside that stretch.

Your task is to return the length of the longest contiguous subarray containing at most k zeros.

In other words, find the maximum number of consecutive meeting slots such that no more than k
of them are late arrivals. The chosen block must be contiguous, and you may excuse any late
arrivals already inside the block, but you cannot reorder slots.

This problem should be solved efficiently for large inputs, so solutions that check every
possible subarray will be too slow.

Constraints:
- 1 <= arrivals.length <= 200000
- arrivals[i] is either 0 or 1
- 0 <= k <= arrivals.length

Example 1:
Input: arrivals = [1,1,0,1,0,1,1,1], k = 1
Output: 5
Explanation: The longest valid block is [1,0,1,1,1], which contains exactly one late arrival.

Example 2:
Input: arrivals = [0,0,1,1,1,0,1,1], k = 2
Output: 7
Explanation: The subarray [0,1,1,1,0,1,1] has length 7 and contains two late arrivals, which is allowed.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is processed by the right pointer once.
    - Each element is removed from the window by the left pointer at most once.
    - Because both pointers only move forward, the total work is linear.

    Space Complexity: O(1)
    - We only store a few integer variables.
    - No extra array, list, queue, or dictionary is needed.
    */
    public int LongestReliableAttendanceBlock(int[] arrivals, int k)
    {
        // We will use the "sliding window" technique.
        //
        // Idea:
        // - We maintain a window [left..right] that represents a contiguous subarray.
        // - Inside this window, we count how many zeros (late arrivals) exist.
        // - As long as the number of zeros is at most k, the window is valid.
        // - If the number of zeros becomes greater than k, the window is invalid,
        //   so we move the left side forward until the window becomes valid again.
        //
        // Why sliding window works well here:
        // - We need a contiguous subarray.
        // - The condition "at most k zeros" can be maintained incrementally.
        // - This avoids checking every possible subarray, which would be too slow.

        // 'left' is the starting index of the current window.
        int left = 0;

        // 'zeroCount' stores how many late arrivals (0s) are currently inside the window.
        int zeroCount = 0;

        // 'maxLength' stores the best answer found so far.
        int maxLength = 0;

        // We expand the window one element at a time by moving 'right' from left to right.
        for (int right = 0; right < arrivals.Length; right++)
        {
            // Step 1:
            // Include arrivals[right] in the current window.
            //
            // If this new element is 0, then the number of late arrivals in the window increases.
            // We must track that because the rule says we can excuse at most k late arrivals.
            if (arrivals[right] == 0)
            {
                zeroCount++;
            }

            // Step 2:
            // If the window now contains too many zeros, it is invalid.
            //
            // We must shrink the window from the left until it becomes valid again.
            // This is necessary because only windows with zeroCount <= k are allowed.
            while (zeroCount > k)
            {
                // Before moving 'left' forward, check whether the element leaving the window
                // is a zero. If it is, then the number of zeros in the window decreases.
                if (arrivals[left] == 0)
                {
                    zeroCount--;
                }

                // Move the left boundary forward to shrink the window.
                left++;
            }

            // Step 3:
            // At this point, the window [left..right] is guaranteed to be valid,
            // because zeroCount <= k.
            //
            // So we compute its length and compare it with the best answer seen so far.
            int currentLength = right - left + 1;

            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After scanning the entire array, maxLength is the length of the longest
        // contiguous subarray containing at most k zeros.
        return maxLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] arrivals1 = { 1, 1, 0, 1, 0, 1, 1, 1 };
int k1 = 1;
int result1 = solution.LongestReliableAttendanceBlock(arrivals1, k1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 5

// Example 2
int[] arrivals2 = { 0, 0, 1, 1, 1, 0, 1, 1 };
int k2 = 2;
int result2 = solution.LongestReliableAttendanceBlock(arrivals2, k2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 7

// Additional demo 1: no late arrivals can be excused
int[] arrivals3 = { 1, 0, 1, 1, 0, 1 };
int k3 = 0;
int result3 = solution.LongestReliableAttendanceBlock(arrivals3, k3);
Console.WriteLine("Additional Demo 1 Result: " + result3); // Expected: 2

// Additional demo 2: all slots can be included because k is large enough
int[] arrivals4 = { 0, 1, 0, 1, 1, 0 };
int k4 = 3;
int result4 = solution.LongestReliableAttendanceBlock(arrivals4, k4);
Console.WriteLine("Additional Demo 2 Result: " + result4); // Expected: 6