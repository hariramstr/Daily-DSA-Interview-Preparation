/*
Title: Longest Chat Window With Bounded Emoji Variety
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A messaging platform stores the sequence of emoji reactions added to a live chat as an array of strings,
where each string is a single emoji code such as ":smile:" or ":fire:".

Product analysts want to identify the longest contiguous time window in which the conversation stayed
focused enough that no more than k distinct emoji types were used.

Given an array reactions and an integer k, return the length of the longest contiguous subarray
that contains at most k distinct emoji strings.

A window is contiguous, so you may only choose reactions that appear next to each other in the original array.
If k is 0, the answer is 0 because no emoji types are allowed.
If the array is empty, return 0.

Your solution should be efficient enough for large chat logs, so an approach that checks every possible
subarray will be too slow.

Constraints:
- 0 <= reactions.length <= 200000
- 0 <= k <= reactions.length
- Each reactions[i] is a non-empty string of length 1 to 20
- reactions[i] consists of visible ASCII characters

Example 1:
Input: reactions = [":smile:",":fire:",":smile:",":heart:",":fire:",":fire:"], k = 2
Output: 3
Explanation:
The longest valid window is [":smile:",":fire:",":smile:"].
Any length-4 window contains 3 distinct emoji types, so 3 is the correct answer.

Example 2:
Input: reactions = [":ok:",":ok:",":wave:",":wave:",":wave:",":star:"], k = 1
Output: 3
Explanation:
The longest contiguous window with at most 1 distinct emoji type is
[":wave:",":wave:",":wave:"], which has length 3.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each reaction enters the sliding window once when the right pointer moves forward.
    - Each reaction leaves the sliding window at most once when the left pointer moves forward.
    - Therefore, the total amount of work is linear in the number of reactions.

    Space Complexity: O(k) in the typical sliding-window sense, or more precisely O(d)
    - We store counts of the distinct emoji strings currently inside the window.
    - d is the number of distinct emoji types in the current window, which is never more than k after adjustment.
    - In the worst case during processing, the dictionary size is bounded by the number of distinct strings encountered.
    */
    public int LengthOfLongestWindowWithAtMostKDistinct(string[] reactions, int k)
    {
        // If no emoji types are allowed, then no non-empty window can be valid.
        // The problem explicitly states that when k is 0, the answer is 0.
        if (k == 0)
        {
            return 0;
        }

        // If the input array is empty, there is no window to examine.
        if (reactions == null || reactions.Length == 0)
        {
            return 0;
        }

        // This dictionary maps:
        //   emoji string -> how many times that emoji appears in the current window
        //
        // Why do we need counts instead of just a set?
        // Because when we move the left side of the window forward, we need to know whether
        // removing one occurrence still leaves that emoji inside the window.
        // A set alone cannot tell us how many copies remain.
        var frequency = new Dictionary<string, int>();

        // left marks the beginning of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // We expand the window by moving right from left to right across the array.
        for (int right = 0; right < reactions.Length; right++)
        {
            // Step 1: Include reactions[right] in the current window.
            //
            // The current window is conceptually reactions[left..right].
            // We are growing the window by one element on the right side.
            string currentEmoji = reactions[right];

            // If this emoji is already in the window, increase its count.
            // Otherwise, add it with count 1.
            if (frequency.ContainsKey(currentEmoji))
            {
                frequency[currentEmoji]++;
            }
            else
            {
                frequency[currentEmoji] = 1;
            }

            // Step 2: If the window now contains too many distinct emoji types,
            // shrink it from the left until it becomes valid again.
            //
            // Why is this necessary?
            // The problem requires "at most k distinct" emoji types.
            // After adding the new right-side emoji, we may have exceeded that limit.
            //
            // We use a while loop, not an if statement, because removing just one element
            // may still leave the window invalid. We keep shrinking until the condition is restored.
            while (frequency.Count > k)
            {
                // Identify the emoji that is leaving the window from the left side.
                string leftEmoji = reactions[left];

                // Decrease its count because it is no longer inside the window.
                frequency[leftEmoji]--;

                // If its count becomes zero, that means this emoji type is no longer present
                // anywhere in the current window, so we remove it from the dictionary.
                //
                // This is very important because frequency.Count is how we track the number
                // of distinct emoji types currently in the window.
                if (frequency[leftEmoji] == 0)
                {
                    frequency.Remove(leftEmoji);
                }

                // Move the left boundary one step to the right,
                // making the window smaller.
                left++;
            }

            // Step 3: At this point, the window is guaranteed to be valid:
            // it contains at most k distinct emoji types.
            //
            // So we can safely compute its length and compare it with the best answer seen so far.
            int currentWindowLength = right - left + 1;

            if (currentWindowLength > best)
            {
                best = currentWindowLength;
            }
        }

        // After scanning the entire array, best holds the length of the longest valid window.
        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] reactions1 = { ":smile:", ":fire:", ":smile:", ":heart:", ":fire:", ":fire:" };
int k1 = 2;
int result1 = solution.LengthOfLongestWindowWithAtMostKDistinct(reactions1, k1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 3

// Example 2
string[] reactions2 = { ":ok:", ":ok:", ":wave:", ":wave:", ":wave:", ":star:" };
int k2 = 1;
int result2 = solution.LengthOfLongestWindowWithAtMostKDistinct(reactions2, k2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 3

// Additional edge case: k = 0
string[] reactions3 = { ":smile:", ":smile:" };
int k3 = 0;
int result3 = solution.LengthOfLongestWindowWithAtMostKDistinct(reactions3, k3);
Console.WriteLine($"Edge Case (k=0) Result: {result3}"); // Expected: 0

// Additional edge case: empty array
string[] reactions4 = Array.Empty<string>();
int k4 = 2;
int result4 = solution.LengthOfLongestWindowWithAtMostKDistinct(reactions4, k4);
Console.WriteLine($"Edge Case (empty array) Result: {result4}"); // Expected: 0