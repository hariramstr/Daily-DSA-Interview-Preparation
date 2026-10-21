/*
Title: Maximum Consecutive Seats After Filling One Gap
Difficulty: Medium
Topic: Arrays

Problem Description:
You are given an array seats where seats[i] is either 0 or 1. A value of 1 means the seat is already occupied, and 0 means it is empty. The event organizer is allowed to assign exactly one currently empty seat to a late guest. After doing so, the organizer wants to know the maximum possible length of a consecutive block of occupied seats anywhere in the row.

Return the largest number of consecutive 1s that can appear after changing at most one 0 into a 1. If the row is already fully occupied, return the length of the entire array.

This problem models a common interview scenario involving array scanning and window maintenance. A correct solution should work efficiently for large rows and should not try every possible seat independently.

Constraints:
- 1 <= seats.length <= 200000
- seats[i] is either 0 or 1
- You may change at most one 0 to 1

Example 1:
Input: seats = [1,1,0,1,1,1,0,1]
Output: 6
Explanation: Fill the empty seat at index 2. The row becomes [1,1,1,1,1,1,0,1], which contains a consecutive occupied block of length 6.

Example 2:
Input: seats = [0,0,1,0,1,1,0]
Output: 4
Explanation: Fill the empty seat at index 3. The row becomes [0,0,1,1,1,1,0], giving a longest consecutive occupied block of length 4.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    We use a sliding window.
    The window always represents a segment of the array that contains at most one 0.
    Why is that useful?
    Because changing at most one 0 into a 1 means any valid final consecutive block of 1s
    must come from some contiguous segment that contains:
    - all 1s already, or
    - exactly one 0 that we choose to flip.

    So the problem becomes:
    "Find the longest contiguous subarray containing at most one 0."

    That longest window length is exactly the answer.
    */
    public int LongestConsecutiveAfterOneFill(int[] seats)
    {
        // "left" is the starting index of our current sliding window.
        // The window will expand with "right" as we scan through the array.
        int left = 0;

        // This counts how many zeros are currently inside the window [left..right].
        // We must keep this value <= 1, because we are allowed to flip at most one zero.
        int zeroCount = 0;

        // This stores the best (largest) valid window length found so far.
        int best = 0;

        // Move "right" from the beginning of the array to the end.
        // At each step, we include seats[right] into the current window.
        for (int right = 0; right < seats.Length; right++)
        {
            // Step 1: Add the new element at the right side of the window.
            // If it is a zero, then our window now contains one more zero.
            if (seats[right] == 0)
            {
                zeroCount++;
            }

            // Step 2: If the window has more than one zero, it is no longer valid.
            // Why?
            // Because we can only flip at most one zero.
            //
            // So we must shrink the window from the left until it becomes valid again.
            while (zeroCount > 1)
            {
                // Before moving "left" forward, check whether the element leaving
                // the window is a zero. If yes, we must decrease zeroCount because
                // that zero is no longer inside the window.
                if (seats[left] == 0)
                {
                    zeroCount--;
                }

                // Actually remove the leftmost element by advancing "left".
                left++;
            }

            // Step 3: At this point, the window [left..right] is guaranteed valid.
            // It contains at most one zero, so it can be turned into all 1s
            // by flipping that one zero (or doing nothing if there is no zero).
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is larger
            // than anything we have seen before.
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // "best" is the maximum length of a contiguous segment with at most one zero.
        // That is exactly the maximum consecutive occupied block possible
        // after changing at most one 0 into a 1.
        return best;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] seats1 = { 1, 1, 0, 1, 1, 1, 0, 1 };
int result1 = solution.LongestConsecutiveAfterOneFill(seats1);
Console.WriteLine(result1); // Expected: 6

// Example 2
int[] seats2 = { 0, 0, 1, 0, 1, 1, 0 };
int result2 = solution.LongestConsecutiveAfterOneFill(seats2);
Console.WriteLine(result2); // Expected: 4

// Additional demo: already fully occupied
int[] seats3 = { 1, 1, 1, 1 };
int result3 = solution.LongestConsecutiveAfterOneFill(seats3);
Console.WriteLine(result3); // Expected: 4

// Additional demo: single empty seat
int[] seats4 = { 0 };
int result4 = solution.LongestConsecutiveAfterOneFill(seats4);
Console.WriteLine(result4); // Expected: 1

// Additional demo: multiple empty seats
int[] seats5 = { 0, 1, 1, 0, 1 };
int result5 = solution.LongestConsecutiveAfterOneFill(seats5);
Console.WriteLine(result5); // Expected: 4