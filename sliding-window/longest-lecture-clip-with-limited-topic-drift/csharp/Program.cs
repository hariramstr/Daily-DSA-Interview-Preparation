/*
Title: Longest Lecture Clip With Limited Topic Drift
Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given an array `topics` representing the topic label of each consecutive minute in a recorded lecture.
The lecture platform wants to extract the longest contiguous clip that still feels focused.

A clip is considered focused if it contains at most `k` topic transitions, where a transition happens
between two adjacent minutes `i - 1` and `i` when `topics[i] != topics[i - 1]`.

Return the length of the longest contiguous subarray of `topics` that contains at most `k` transitions.

Important:
This is NOT the same as limiting the number of distinct topic labels.
For example, the clip [2, 2, 3, 3, 2] has only 2 distinct labels, but it has 2 transitions:
2 -> 3 and 3 -> 2.

We need an efficient algorithm because the input can be large.

Examples:
1)
topics = [4, 4, 1, 1, 1, 3, 3, 4], k = 2
Output: 7
Explanation:
[4, 4, 1, 1, 1, 3, 3] has exactly 2 transitions:
4 -> 1
1 -> 3
Length = 7

2)
topics = [5, 6, 5, 6, 5], k = 1
Output: 2
Explanation:
Every adjacent pair changes topic, so any length-3 subarray has 2 transitions.
So the best valid answer is 2.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is processed by the right pointer once.
    - Each element is removed by the left pointer at most once.
    - Therefore the total work is linear.

    Space Complexity: O(1)
    - We only store a few integer variables.
    - No extra data structures proportional to input size are used.
    */
    public int LongestFocusedClip(int[] topics, int k)
    {
        // If the array has at least one element, the minimum valid answer is 1,
        // because a single minute has no adjacent pair inside it, so it has 0 transitions.
        // The constraints guarantee topics.length >= 1, but writing code this way
        // also makes the method more robust and easier to understand.
        if (topics == null || topics.Length == 0)
        {
            return 0;
        }

        // "left" is the starting index of our current sliding window.
        // The window will always represent a contiguous subarray topics[left..right].
        int left = 0;

        // "transitions" stores how many topic changes currently exist INSIDE the window.
        //
        // Very important idea:
        // A transition is not attached to a single element.
        // It is attached to a boundary between two adjacent elements.
        //
        // For example, in:
        // [4, 4, 1, 1, 3]
        // the transitions happen at:
        // between index 1 and 2 (4 -> 1)
        // between index 3 and 4 (1 -> 3)
        //
        // So when we expand or shrink the window, we must carefully update
        // the count based on which adjacent boundary enters or leaves the window.
        int transitions = 0;

        // This will store the best (maximum) valid window length found so far.
        int best = 1;

        // We move "right" from left to right across the array.
        // At each step, we include topics[right] into the current window.
        for (int right = 0; right < topics.Length; right++)
        {
            // STEP 1: Expand the window to include topics[right].
            //
            // If right > 0, then adding topics[right] introduces a new adjacent boundary:
            // between topics[right - 1] and topics[right].
            //
            // But that boundary only matters if those two values are different.
            // If they are different, then we have added one new transition to the window.
            //
            // Example:
            // topics = [4, 4, 1, ...]
            // when right moves from 1 to 2, we add boundary (1,2):
            // 4 != 1, so transitions increases by 1.
            if (right > 0 && topics[right] != topics[right - 1])
            {
                transitions++;
            }

            // STEP 2: If the window now has too many transitions, shrink it from the left
            // until it becomes valid again.
            //
            // The rule says the window is valid only when transitions <= k.
            while (transitions > k)
            {
                // We are about to move "left" one step to the right,
                // which means topics[left] will leave the window.
                //
                // Before incrementing left, we must check whether the boundary
                // between topics[left] and topics[left + 1] is currently inside the window
                // and whether removing topics[left] will remove a transition.
                //
                // Why do we check left < right?
                // Because the boundary (left, left + 1) only exists if the window
                // has at least two elements.
                //
                // If topics[left] != topics[left + 1], then the boundary between them
                // is a transition currently counted inside the window.
                // Once topics[left] is removed, that boundary is no longer inside the window,
                // so we must subtract 1 from transitions.
                //
                // Example:
                // Window = [4, 4, 1, 1], left = 0
                // Removing the first 4 does NOT remove a transition because 4 == 4.
                //
                // Example:
                // Window = [4, 1, 1], left = 0
                // Removing the first 4 DOES remove a transition because 4 != 1.
                if (left < right && topics[left] != topics[left + 1])
                {
                    transitions--;
                }

                // Actually move the left edge of the window rightward by one position.
                left++;
            }

            // STEP 3: At this point, the window topics[left..right] is valid
            // because transitions <= k.
            //
            // So we compute its length and update the best answer if needed.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // After scanning the whole array, "best" contains the maximum valid window length.
        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] topics1 = { 4, 4, 1, 1, 1, 3, 3, 4 };
int k1 = 2;
int result1 = solution.LongestFocusedClip(topics1, k1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 7

// Example 2
int[] topics2 = { 5, 6, 5, 6, 5 };
int k2 = 1;
int result2 = solution.LongestFocusedClip(topics2, k2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 2

// Additional demo 1: no transitions allowed
int[] topics3 = { 2, 2, 2, 3, 3, 3, 3 };
int k3 = 0;
int result3 = solution.LongestFocusedClip(topics3, k3);
Console.WriteLine("Additional Demo 1 Result: " + result3); // Expected: 4

// Additional demo 2: entire array valid
int[] topics4 = { 1, 1, 2, 2, 3, 3 };
int k4 = 2;
int result4 = solution.LongestFocusedClip(topics4, k4);
Console.WriteLine("Additional Demo 2 Result: " + result4); // Expected: 6

// Additional demo 3: alternating values with larger k
int[] topics5 = { 7, 8, 7, 8, 7, 8 };
int k5 = 3;
int result5 = solution.LongestFocusedClip(topics5, k5);
Console.WriteLine("Additional Demo 3 Result: " + result5); // Expected: 4