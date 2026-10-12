/*
Title: Maximum Consecutive Seats After Blocking One Broken Chair

Problem Description:
You are given an array seats of length n where each element is either 0 or 1.
A value of 1 means the seat is usable, and 0 means the seat is broken.

During a small event, the venue manager is allowed to place exactly one temporary
cover over at most one broken seat so that it can be used for the event.
In other words, you may change at most one 0 in the array into a 1.

Your task is to return the maximum number of consecutive usable seats that can be
formed after applying this operation optimally.

If the array already contains only usable seats, return the total number of seats.
If there are multiple broken seats, you may choose the best one to cover.
If no broken seat is covered, that is also allowed, since the operation is
"at most one" change.

Return the length of the longest contiguous subarray containing only 1s after
changing at most one 0 to 1.

Examples:
1) seats = [1,1,0,1,1,1,0,1]
   Output: 6
   Explanation:
   Cover the broken seat at index 2:
   [1,1,1,1,1,1,0,1]
   The longest consecutive usable block has length 6.

2) seats = [0,0,1,1,0,1]
   Correct Output: 4
   Explanation:
   Cover the broken seat at index 4:
   [0,0,1,1,1,1]
   The longest consecutive usable block is from index 2 to 5, length 4.

Constraints:
- 1 <= n <= 200000
- seats[i] is either 0 or 1
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is visited at most a constant number of times by the sliding window.
    - The left pointer only moves forward, and the right pointer also only moves forward.

    Space Complexity: O(1)
    - We only use a few integer variables.
    - No extra array or data structure proportional to input size is needed.

    Beginner-friendly idea:
    We want the longest contiguous segment that contains only 1s after changing
    at most one 0 into 1.

    That means:
    - A valid window can contain:
      - all 1s, or
      - exactly one 0, because we can convert that one 0 into 1
    - But it cannot contain two or more 0s, because we are allowed to fix only one broken seat.

    So we use a sliding window:
    - Expand the window to the right
    - Count how many zeros are inside the current window
    - If zeros become more than 1, move the left side forward until the window is valid again
    - Track the maximum valid window length
    */
    public int LongestUsableSeatsAfterOneCover(int[] seats)
    {
        // This pointer marks the beginning of our current window.
        // The window is the subarray seats[left..right].
        int left = 0;

        // This variable stores how many broken seats (0s) are currently inside the window.
        // Why do we need it?
        // Because the rule says we may change at most one 0 into 1.
        // So the window is valid only when zeroCount <= 1.
        int zeroCount = 0;

        // This will store the best answer found so far.
        // At every step, if the current window is valid, we compare its length
        // with the best length we have seen.
        int maxLength = 0;

        // Move the right pointer from the start of the array to the end.
        // Each step means: "Let's try to include one more seat in our current segment."
        for (int right = 0; right < seats.Length; right++)
        {
            // Step 1:
            // Include seats[right] into the current window.
            // If this seat is broken (0), then our window now contains one more broken seat.
            if (seats[right] == 0)
            {
                zeroCount++;
            }

            // Step 2:
            // If the window now contains more than one broken seat,
            // it is no longer valid.
            //
            // Why invalid?
            // Because we are allowed to cover at most one broken seat.
            // If there are two zeros in the window, we would need to fix two seats,
            // which is not allowed.
            //
            // So we must shrink the window from the left until it becomes valid again.
            while (zeroCount > 1)
            {
                // Before moving left forward, check whether the seat leaving the window
                // is a broken seat.
                //
                // If it is 0, then removing it decreases the number of broken seats
                // inside the window.
                if (seats[left] == 0)
                {
                    zeroCount--;
                }

                // Move the left boundary one step to the right.
                left++;
            }

            // Step 3:
            // At this point, the window seats[left..right] is valid.
            // That means it contains at most one zero.
            //
            // Therefore, by changing that one zero to one (or changing nothing if there is no zero),
            // the entire window can become all usable seats.
            int currentLength = right - left + 1;

            // Update the best answer if this valid window is larger.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After checking all possible windows, maxLength is the answer.
        return maxLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] seats1 = { 1, 1, 0, 1, 1, 1, 0, 1 };
int result1 = solution.LongestUsableSeatsAfterOneCover(seats1);
Console.WriteLine("Example 1:");
Console.WriteLine("Input: [1,1,0,1,1,1,0,1]");
Console.WriteLine("Output: " + result1);
Console.WriteLine("Expected: 6");
Console.WriteLine();

// Example 2
// Note: The problem statement text contains a contradiction.
// It says "Output: 3" but then explains a segment of length 4.
// The correct answer is 4.
int[] seats2 = { 0, 0, 1, 1, 0, 1 };
int result2 = solution.LongestUsableSeatsAfterOneCover(seats2);
Console.WriteLine("Example 2:");
Console.WriteLine("Input: [0,0,1,1,0,1]");
Console.WriteLine("Output: " + result2);
Console.WriteLine("Expected: 4");
Console.WriteLine();

// Additional demo: all seats already usable
int[] seats3 = { 1, 1, 1, 1, 1 };
int result3 = solution.LongestUsableSeatsAfterOneCover(seats3);
Console.WriteLine("Additional Example 3:");
Console.WriteLine("Input: [1,1,1,1,1]");
Console.WriteLine("Output: " + result3);
Console.WriteLine("Expected: 5");
Console.WriteLine();

// Additional demo: all seats broken
int[] seats4 = { 0, 0, 0, 0 };
int result4 = solution.LongestUsableSeatsAfterOneCover(seats4);
Console.WriteLine("Additional Example 4:");
Console.WriteLine("Input: [0,0,0,0]");
Console.WriteLine("Output: " + result4);
Console.WriteLine("Expected: 1");
Console.WriteLine();

// Additional demo: single seat
int[] seats5 = { 0 };
int result5 = solution.LongestUsableSeatsAfterOneCover(seats5);
Console.WriteLine("Additional Example 5:");
Console.WriteLine("Input: [0]");
Console.WriteLine("Output: " + result5);
Console.WriteLine("Expected: 1");