/*
Problem Title:
Longest Chat Streak With At Most One Silent Minute

Problem Description:
You are given a binary array `messages` representing a minute-by-minute chat activity log
for a support agent. Each element is either `1` or `0`, where:

- `1` means the agent sent at least one message during that minute
- `0` means the minute was silent

A chat streak is defined as a contiguous block of minutes that is considered active if you
are allowed to ignore at most one silent minute inside the block.

Your task is to return the length of the longest possible active chat streak.

In other words, find the maximum length of a contiguous subarray containing at most one `0`.

This models a real analytics scenario where a single short pause should not necessarily break
an otherwise continuous support conversation. The solution should be efficient enough for large
logs, so a sliding window approach is expected.

Constraints:
- 1 <= messages.length <= 100000
- messages[i] is either 0 or 1

Example 1:
Input: messages = [1,1,0,1,1,1,0,1]
Output: 6

Explanation:
The best valid window is [1,1,0,1,1,1], which has length 6 and contains only one 0.
A longer window that includes two 0 values is not allowed.

Example 2:
Input: messages = [0,1,1,1,0,1,1]
Output: 5

Explanation:
The longest valid streak is [1,1,1,0,1] or [1,1,0,1,1].
Each contains exactly one 0, so the answer is 5.
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
    public int LongestChatStreak(int[] messages)
    {
        // `left` marks the beginning of the current sliding window.
        // The window will always represent a valid subarray that contains
        // at most one silent minute (at most one 0).
        int left = 0;

        // `zeroCount` stores how many 0 values are currently inside the window.
        // This is the key piece of information we need because the rule says:
        // "the window is valid only if it contains at most one 0."
        int zeroCount = 0;

        // `bestLength` stores the maximum valid window length we have seen so far.
        // We update this whenever the current window is valid and larger than
        // all previously seen valid windows.
        int bestLength = 0;

        // We expand the window one element at a time using `right`.
        // `right` is the end index of the current window.
        for (int right = 0; right < messages.Length; right++)
        {
            // Step 1:
            // Include messages[right] into the current window.
            //
            // Why this is necessary:
            // We are trying every possible window ending position by moving `right`
            // from left to right across the array.
            //
            // If the new element is 0, then the number of silent minutes in the
            // current window increases by one.
            if (messages[right] == 0)
            {
                zeroCount++;
            }

            // Step 2:
            // If the window has become invalid (more than one 0),
            // we must shrink it from the left until it becomes valid again.
            //
            // Why this is necessary:
            // The problem allows at most one silent minute in the streak.
            // So any window with two or more 0 values cannot be considered.
            //
            // We use a `while` loop instead of `if` because removing one element
            // from the left may still leave more than one 0 in the window.
            while (zeroCount > 1)
            {
                // Before moving `left` forward, check whether the element leaving
                // the window is a 0.
                //
                // If it is, then the number of 0 values inside the window decreases.
                if (messages[left] == 0)
                {
                    zeroCount--;
                }

                // Move the left boundary one step to the right.
                //
                // This effectively removes messages[left] from the window.
                left++;
            }

            // Step 3:
            // At this point, the window [left..right] is guaranteed to be valid
            // because it contains at most one 0.
            //
            // So we can safely compute its length.
            int currentLength = right - left + 1;

            // Step 4:
            // Update the best answer if the current valid window is larger.
            //
            // Why this is necessary:
            // We want the maximum length over all valid windows.
            if (currentLength > bestLength)
            {
                bestLength = currentLength;
            }
        }

        // After scanning the entire array, `bestLength` contains the answer.
        return bestLength;
    }
}

// Demo code:
// Creates sample inputs, calls the solution, and prints the results.

var solution = new Solution();

int[] messages1 = { 1, 1, 0, 1, 1, 1, 0, 1 };
int result1 = solution.LongestChatStreak(messages1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 6

int[] messages2 = { 0, 1, 1, 1, 0, 1, 1 };
int result2 = solution.LongestChatStreak(messages2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 5

// Additional quick checks for learning and confidence.

int[] messages3 = { 1 };
int result3 = solution.LongestChatStreak(messages3);
Console.WriteLine("Single active minute: " + result3); // Expected: 1

int[] messages4 = { 0 };
int result4 = solution.LongestChatStreak(messages4);
Console.WriteLine("Single silent minute: " + result4); // Expected: 1

int[] messages5 = { 1, 1, 1, 1 };
int result5 = solution.LongestChatStreak(messages5);
Console.WriteLine("All active minutes: " + result5); // Expected: 4

int[] messages6 = { 0, 0, 0, 0 };
int result6 = solution.LongestChatStreak(messages6);
Console.WriteLine("All silent minutes: " + result6); // Expected: 1