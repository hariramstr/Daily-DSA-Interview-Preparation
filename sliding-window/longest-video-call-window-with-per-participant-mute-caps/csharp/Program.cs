/*
Title: Longest Video Call Window With Per-Participant Mute Caps
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are given a video call activity log represented by an array participants, where participants[i] is the participant ID speaking during second i of a recorded meeting. You are also given an integer array muteCap of length m, where muteCap[id] indicates the maximum number of times participant id is allowed to appear inside a valid contiguous window. Participant IDs are in the range [0, m - 1].

A contiguous time window is considered stable if, for every participant id, the number of occurrences of id inside the window is at most muteCap[id]. Your task is to return the length of the longest stable window.

This is harder than a standard frequency-limited sliding window because muteCap may be different for every participant, some caps may be zero, and the input size is large enough that any O(n * m) or repeated full-map validation will time out. You need to design an algorithm that updates validity as the window expands and shrinks.

Return only the maximum possible length of a stable contiguous subarray.

Constraints:
- 1 <= participants.length <= 200000
- 1 <= m <= 200000
- 0 <= participants[i] < m
- 0 <= muteCap[id] <= participants.length
- The sum of all values does not need to match the array length

Example 1:
Input: participants = [1,2,1,3,2,1,2], muteCap = [0,2,2,1]
Output: 5
Explanation: The window [2,1,3,2,1] has counts: participant 1 -> 2, participant 2 -> 2, participant 3 -> 1, all within their caps. No longer valid window exists.

Example 2:
Input: participants = [4,4,2,4,1,2,1,1,3], muteCap = [0,1,2,2,1]
Output: 4
Explanation: One optimal window is [4,1,2,1]. Its counts are: participant 1 -> 2? No, that would exceed cap 1, so it is invalid. A valid optimal window is [2,4,1,2], where participant 2 appears twice, participant 4 appears once, and participant 1 appears once. Its length is 4.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n), where n is participants.Length.
    - Each element enters the sliding window once when the right pointer expands,
      and each element leaves the sliding window at most once when the left pointer shrinks.
      Therefore, the total amount of work is linear.

    Space Complexity:
    - O(m) for the frequency array, where m is muteCap.Length.
    - This is efficient because participant IDs are guaranteed to be in the range [0, m - 1],
      so an array is faster and simpler than a dictionary here.
    */
    public int LongestStableWindow(int[] participants, int[] muteCap)
    {
        // Frequency array:
        // freq[id] tells us how many times participant 'id' currently appears
        // inside the active sliding window [left..right].
        int[] freq = new int[muteCap.Length];

        // 'left' is the start of the current window.
        int left = 0;

        // 'best' stores the maximum valid window length found so far.
        int best = 0;

        // We expand the window one element at a time using 'right'.
        for (int right = 0; right < participants.Length; right++)
        {
            // Step 1: Include the new participant at index 'right' into the window.
            // This means we increase that participant's frequency.
            int addedId = participants[right];
            freq[addedId]++;

            // Step 2: If adding this participant caused the window to become invalid,
            // we must shrink the window from the left until it becomes valid again.
            //
            // Why is checking only 'addedId' enough?
            // ------------------------------------
            // Before we added participants[right], the window was already valid.
            // That means every participant count was within its cap.
            // After adding one new element, only the count of that specific participant
            // can increase. No other participant's count changed.
            // Therefore, only 'addedId' can possibly violate its cap.
            //
            // So instead of scanning all participant IDs every time, we only check:
            //     freq[addedId] > muteCap[addedId]
            //
            // This is the key optimization that keeps the algorithm linear.
            while (freq[addedId] > muteCap[addedId])
            {
                // Remove the participant at the left edge from the window.
                int removedId = participants[left];
                freq[removedId]--;

                // Move the left boundary rightward, making the window smaller.
                left++;
            }

            // Step 3: At this point, the window [left..right] is guaranteed valid.
            // So we can safely compute its length and update the best answer.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // After processing all positions, 'best' is the longest valid window length.
        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] participants1 = { 1, 2, 1, 3, 2, 1, 2 };
int[] muteCap1 = { 0, 2, 2, 1 };
int result1 = solution.LongestStableWindow(participants1, muteCap1);
Console.WriteLine(result1); // Expected: 5

// Example 2
int[] participants2 = { 4, 4, 2, 4, 1, 2, 1, 1, 3 };
int[] muteCap2 = { 0, 1, 2, 2, 1 };
int result2 = solution.LongestStableWindow(participants2, muteCap2);
Console.WriteLine(result2); // Expected: 4

// Additional quick sanity checks

// If a participant has cap 0, they cannot appear in any valid window.
int[] participants3 = { 0, 1, 2 };
int[] muteCap3 = { 0, 1, 1 };
int result3 = solution.LongestStableWindow(participants3, muteCap3);
Console.WriteLine(result3); // Expected: 2 -> window [1,2]

// Entire array valid
int[] participants4 = { 2, 2, 1, 3 };
int[] muteCap4 = { 0, 1, 2, 1 };
int result4 = solution.LongestStableWindow(participants4, muteCap4);
Console.WriteLine(result4); // Expected: 4