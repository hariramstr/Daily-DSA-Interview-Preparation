/*
Title: Longest Reading Session With Limited Genre Switches
Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given an array genres where genres[i] is the genre ID of the i-th article a user reads in chronological order.
A reading session is defined as any contiguous segment of this array. Product analysts want to find the longest
session that feels focused, so they allow at most k genre switches inside the session.

A genre switch occurs between two adjacent articles when their genre IDs are different. For example, in the session
[2, 2, 5, 5, 3], there are 2 genre switches: one from 2 to 5 and one from 5 to 3.

Return the length of the longest contiguous reading session that contains at most k genre switches.

This is not the same as limiting the number of distinct genres. A session may contain many articles of the same genre
grouped together, and only changes between neighboring articles count toward the switch total.

Your task is to design an efficient algorithm that works for large inputs.

Constraints:
- 1 <= genres.length <= 200000
- 1 <= genres[i] <= 1000000000
- 0 <= k < genres.length

Example 1:
Input: genres = [1, 1, 2, 2, 2, 3, 3], k = 1
Output: 5
Explanation: The longest valid session is [1, 1, 2, 2, 2] or [2, 2, 2, 3, 3]. Each has exactly 1 genre switch.

Example 2:
Input: genres = [4, 7, 7, 4, 4, 9, 9, 9, 4], k = 2
Output: 7
Explanation: One optimal session is [7, 7, 4, 4, 9, 9, 9]. The genre switches are 7 -> 4 and 4 -> 9, so the session
is valid. No longer contiguous segment has at most 2 switches.

Notes:
- A session of length 1 always has 0 genre switches.
- If k = 0, the answer is the longest contiguous block of equal genre IDs.
- An O(n) sliding window solution is expected.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Explanation of complexity:
    - The right pointer moves from left to right exactly once across the array.
    - The left pointer also only moves from left to right and never moves backward.
    - Because each pointer advances at most n times, the total work is linear.
    - We only store a few integer variables, so extra space is constant.
    */
    public int LongestReadingSession(int[] genres, int k)
    {
        // Defensive handling:
        // The problem guarantees at least one element, but this check makes the method safer
        // if it is reused in another context.
        if (genres == null || genres.Length == 0)
        {
            return 0;
        }

        // "left" is the start index of our current sliding window.
        // The window will always represent a contiguous session: genres[left..right].
        int left = 0;

        // "switches" stores how many genre switches currently exist inside the window.
        //
        // Important idea:
        // A switch is not about distinct values in the whole window.
        // A switch happens only at a boundary between adjacent positions:
        // if genres[i] != genres[i - 1], then there is a switch at that boundary.
        //
        // So for a window [left..right], the number of switches is the count of boundaries
        // between left+1 and right where adjacent values differ.
        int switches = 0;

        // "best" stores the maximum valid window length found so far.
        int best = 1;

        // We expand the window by moving "right" one step at a time.
        for (int right = 0; right < genres.Length; right++)
        {
            // STEP 1: Add the new element at index "right" into the window.
            //
            // If right > 0, then a new adjacent boundary appears between:
            // genres[right - 1] and genres[right]
            //
            // If those two values are different, then this boundary contributes one genre switch.
            //
            // Why this is necessary:
            // We need to keep an accurate count of switches inside the current window.
            // Every time we extend the window to the right, only one new boundary can be added,
            // so we update the count in O(1) time.
            if (right > 0 && genres[right] != genres[right - 1])
            {
                switches++;
            }

            // STEP 2: If the window has too many switches, shrink it from the left.
            //
            // Our goal is to maintain the invariant:
            // the current window [left..right] must have at most k switches.
            //
            // If switches > k, the window is invalid, so we move "left" forward until
            // the window becomes valid again.
            while (switches > k)
            {
                // Before moving left forward, we need to determine whether we are removing
                // a boundary that currently contributes a switch.
                //
                // The boundary affected by moving left from "left" to "left + 1" is:
                // between genres[left] and genres[left + 1]
                //
                // If those two are different, then that boundary was counted as a switch
                // inside the old window. Once left moves past it, that boundary is no longer
                // inside the window, so we must subtract 1 from "switches".
                //
                // Why this is correct:
                // The only boundary removed when left increases by 1 is exactly the one
                // between left and left + 1.
                if (left < right && genres[left] != genres[left + 1])
                {
                    switches--;
                }

                // Now actually shrink the window by moving the left edge rightward.
                left++;
            }

            // STEP 3: At this point, the window [left..right] is valid:
            // it contains at most k switches.
            //
            // So we can compute its length and update the best answer.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // After scanning the whole array, "best" is the longest valid session length.
        return best;
    }
}

// Demo code:
// Creates sample inputs, calls the solution, and prints the results.

var solution = new Solution();

// Example 1
int[] genres1 = { 1, 1, 2, 2, 2, 3, 3 };
int k1 = 1;
int result1 = solution.LongestReadingSession(genres1, k1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 5

// Example 2
int[] genres2 = { 4, 7, 7, 4, 4, 9, 9, 9, 4 };
int k2 = 2;
int result2 = solution.LongestReadingSession(genres2, k2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 7

// Additional demo: k = 0 means longest block of equal values
int[] genres3 = { 5, 5, 5, 2, 2, 8, 8, 8, 8, 1 };
int k3 = 0;
int result3 = solution.LongestReadingSession(genres3, k3);
Console.WriteLine("Additional Demo Result: " + result3); // Expected: 4