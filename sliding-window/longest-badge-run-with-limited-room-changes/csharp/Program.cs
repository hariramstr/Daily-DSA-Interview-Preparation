/*
Title: Longest Badge Run With Limited Room Changes
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A security team records the sequence of room IDs visited by an employee during a single day.
The sequence is stored in an integer array rooms, where rooms[i] is the room entered at time i.

For auditing, the team wants to find the longest contiguous time interval during which the
employee visited at most k distinct rooms.

Your task is to return the length of the longest contiguous subarray of rooms that contains
no more than k distinct values.

This models a real monitoring scenario where frequent movement across too many different rooms
may indicate unusual behavior, while a long interval with only a few room types may represent
normal work patterns.

A contiguous interval means you may only choose consecutive entries from the log.
If k is 0, then no room can be included, so the answer is 0.

Constraints:
- 1 <= rooms.length <= 200000
- 0 <= rooms[i] <= 1000000000
- 0 <= k <= rooms.length
- The expected solution should run in O(n) time using a sliding window and a frequency map.

Example 1:
Input: rooms = [4, 2, 2, 7, 2, 4, 4, 7], k = 2
Output: 4
Explanation: The longest valid interval is [2, 2, 7, 2], which contains only the distinct
room IDs {2, 7}. Its length is 4.

Example 2:
Input: rooms = [9, 9, 1, 3, 1, 1, 3, 9], k = 3
Output: 8
Explanation: The entire array contains exactly 3 distinct room IDs: {9, 1, 3}.
Therefore the whole log is valid, and the answer is 8.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(k) in the sliding window map in the typical valid-window sense,
    and O(min(n, number of distinct values)) overall due to the frequency dictionary.

    Explanation:
    - Each element is added to the window once by moving the right pointer.
    - Each element is removed from the window at most once by moving the left pointer.
    - Therefore, the total amount of work done by both pointers is linear.
    */
    public int LongestSubarrayWithAtMostKDistinct(int[] rooms, int k)
    {
        // If k is 0, the problem states that no room can be included.
        // That means the longest valid contiguous subarray has length 0.
        if (k == 0 || rooms.Length == 0)
        {
            return 0;
        }

        // This dictionary stores:
        // key   -> room ID
        // value -> how many times that room ID currently appears inside the sliding window
        //
        // Why use a dictionary?
        // Because room IDs can be very large (up to 1,000,000,000), so we cannot efficiently
        // use a direct indexing array by room ID.
        // A dictionary lets us count frequencies only for the room IDs that actually appear.
        Dictionary<int, int> frequency = new Dictionary<int, int>();

        // left marks the beginning of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // We expand the window by moving right from left to right across the array.
        for (int right = 0; right < rooms.Length; right++)
        {
            int currentRoom = rooms[right];

            // STEP 1: Add the new room at index 'right' into the window.
            //
            // Why is this necessary?
            // Because the sliding window represents the current contiguous interval [left..right].
            // Every time right moves forward, the new element must be included in our counts.
            if (!frequency.ContainsKey(currentRoom))
            {
                frequency[currentRoom] = 0;
            }

            frequency[currentRoom]++;

            // STEP 2: If the window now contains too many distinct room IDs,
            // shrink it from the left until it becomes valid again.
            //
            // Why do we do this in a while loop?
            // Because adding one new room can make the window invalid, and we may need to remove
            // multiple elements from the left before the number of distinct room IDs is <= k again.
            while (frequency.Count > k)
            {
                int leftRoom = rooms[left];

                // Remove one occurrence of the room at the left edge of the window.
                frequency[leftRoom]--;

                // If its count becomes zero, that room ID is no longer present in the window.
                // We must remove it from the dictionary so that frequency.Count correctly reflects
                // the number of distinct room IDs currently inside the window.
                if (frequency[leftRoom] == 0)
                {
                    frequency.Remove(leftRoom);
                }

                // Move the left boundary rightward to complete the shrink step.
                left++;
            }

            // STEP 3: At this point, the window [left..right] is guaranteed to be valid:
            // it contains at most k distinct room IDs.
            //
            // So we compute its length and update the best answer if this window is longer.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // After scanning the entire array, best holds the length of the longest valid window.
        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] rooms1 = { 4, 2, 2, 7, 2, 4, 4, 7 };
int k1 = 2;
int result1 = solution.LongestSubarrayWithAtMostKDistinct(rooms1, k1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 4

// Example 2
int[] rooms2 = { 9, 9, 1, 3, 1, 1, 3, 9 };
int k2 = 3;
int result2 = solution.LongestSubarrayWithAtMostKDistinct(rooms2, k2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 8

// Additional demo: k = 0
int[] rooms3 = { 1, 2, 3 };
int k3 = 0;
int result3 = solution.LongestSubarrayWithAtMostKDistinct(rooms3, k3);
Console.WriteLine("Additional Demo Result (k = 0): " + result3); // Expected: 0

// Additional demo: all same room
int[] rooms4 = { 5, 5, 5, 5, 5 };
int k4 = 1;
int result4 = solution.LongestSubarrayWithAtMostKDistinct(rooms4, k4);
Console.WriteLine("Additional Demo Result (all same room): " + result4); // Expected: 5