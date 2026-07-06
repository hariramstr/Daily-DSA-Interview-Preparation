/*
Title: Longest Transcript Stretch With Limited Speaker Interruptions
Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given a conversation transcript represented by an array `speakers`, where `speakers[i]` is the ID of the person speaking at second `i`. A continuous segment of the transcript is considered smooth if the number of speaker interruptions inside that segment is at most `k`. An interruption occurs whenever two adjacent seconds in the segment are spoken by different people. For example, in the segment [2, 2, 5, 5, 5, 2], there are 2 interruptions: one between the second and third elements, and one between the fifth and sixth elements.

Your task is to return the length of the longest smooth contiguous segment.

This problem models finding the longest portion of a meeting where the conversation remains relatively stable, allowing only a limited number of speaker changes. The segment may start and end anywhere, but it must be contiguous.

Constraints:
- 1 <= speakers.length <= 200000
- 1 <= speakers[i] <= 1000000000
- 0 <= k < speakers.length

Example 1:
Input: speakers = [1, 1, 2, 2, 2, 3, 3], k = 1
Output: 5
Explanation: The segment [1, 1, 2, 2, 2] has exactly 1 interruption, so its length is 5. Any longer valid segment would include the change from 2 to 3, creating 2 interruptions.

Example 2:
Input: speakers = [4, 7, 7, 4, 4, 4, 9], k = 2
Output: 6
Explanation: The segment [4, 7, 7, 4, 4, 4] has 2 interruptions: 4 -> 7 and 7 -> 4. Its length is 6, which is the longest valid smooth segment.

Key Observation:
For any window [left..right], the number of interruptions inside that window is simply the count of indices i
such that left < i <= right and speakers[i] != speakers[i - 1].

That means we can maintain a sliding window and keep track of how many adjacent changes are currently inside it.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each pointer (left and right) moves from left to right at most once.
    - So the total amount of work is linear in the size of the array.

    Space Complexity: O(1)
    - We only use a few integer variables.
    - No extra arrays, hash sets, or other data structures are needed.
    */
    public int LongestSmoothSegment(int[] speakers, int k)
    {
        // If the array has only one element, the answer is obviously 1,
        // because a single second of transcript has no adjacent pair,
        // and therefore no interruptions.
        if (speakers.Length == 1)
        {
            return 1;
        }

        // `left` marks the start of the current sliding window.
        int left = 0;

        // `interruptions` stores how many speaker changes currently exist
        // inside the window [left..right].
        //
        // Important detail:
        // A change is counted at position i when speakers[i] != speakers[i - 1].
        // That change belongs to a window [left..right] if and only if:
        // left < i <= right
        //
        // In other words, the "boundary" between i-1 and i is inside the window.
        int interruptions = 0;

        // `best` stores the maximum valid window length found so far.
        int best = 1;

        // We expand the window one element at a time by moving `right`.
        for (int right = 0; right < speakers.Length; right++)
        {
            // Step 1:
            // When we extend the window to include `right`,
            // we may be introducing one new adjacent boundary:
            // the boundary between (right - 1) and right.
            //
            // This boundary exists only when right > 0.
            // If the speaker changed there, then the number of interruptions
            // inside the current window increases by 1.
            if (right > 0 && speakers[right] != speakers[right - 1])
            {
                interruptions++;
            }

            // Step 2:
            // If the window now has too many interruptions,
            // we must shrink it from the left until it becomes valid again.
            //
            // Why shrinking works:
            // - The window is contiguous.
            // - We want the longest valid one ending at `right`.
            // - If the current window is invalid, the only way to fix it
            //   while keeping `right` fixed is to move `left` forward.
            while (interruptions > k)
            {
                // Before moving `left` forward, we need to check whether
                // the boundary between `left` and `left + 1` is currently
                // contributing one interruption to the window.
                //
                // That boundary is inside the current window if left < right.
                // If speakers[left] != speakers[left + 1], then removing
                // speakers[left] from the window will also remove that boundary,
                // so the interruption count must decrease by 1.
                if (left < right && speakers[left] != speakers[left + 1])
                {
                    interruptions--;
                }

                // Now we actually move the left edge forward by one position.
                left++;
            }

            // Step 3:
            // At this point, the window [left..right] is guaranteed valid
            // because interruptions <= k.
            //
            // So we compute its length and update the best answer.
            int currentLength = right - left + 1;
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
int[] speakers1 = { 1, 1, 2, 2, 2, 3, 3 };
int k1 = 1;
int result1 = solution.LongestSmoothSegment(speakers1, k1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 5

// Example 2
int[] speakers2 = { 4, 7, 7, 4, 4, 4, 9 };
int k2 = 2;
int result2 = solution.LongestSmoothSegment(speakers2, k2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 6

// Additional demo 1: no interruptions allowed
int[] speakers3 = { 2, 2, 2, 3, 3, 2, 2 };
int k3 = 0;
int result3 = solution.LongestSmoothSegment(speakers3, k3);
Console.WriteLine("Additional Demo 1 Result: " + result3); // Expected: 3

// Additional demo 2: whole array is valid
int[] speakers4 = { 5, 5, 1, 1, 2, 2, 2 };
int k4 = 2;
int result4 = solution.LongestSmoothSegment(speakers4, k4);
Console.WriteLine("Additional Demo 2 Result: " + result4); // Expected: 7

// Additional demo 3: single element
int[] speakers5 = { 42 };
int k5 = 0;
int result5 = solution.LongestSmoothSegment(speakers5, k5);
Console.WriteLine("Additional Demo 3 Result: " + result5); // Expected: 1