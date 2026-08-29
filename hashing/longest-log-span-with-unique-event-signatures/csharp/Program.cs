/*
Title: Longest Log Span With Unique Event Signatures
Difficulty: Medium
Topic: Hashing

Problem Description:
You are given an array events where events[i] is a string representing the signature of the i-th system log entry in chronological order.
A monitoring team wants to extract the longest contiguous span of logs such that no event signature appears more than once inside that span.

Return the length of the longest contiguous subarray of events that contains only unique strings.

Two log entries are considered the same if their signature strings are exactly equal.
The span must be contiguous, meaning you may only choose entries between some left index and right index without skipping any logs.

This problem models a common production debugging task: analysts often want the longest time window without repeated event types
so that they can study a "clean" sequence of unique failures, warnings, and state changes.

Constraints:
- 1 <= events.length <= 100000
- 1 <= events[i].length <= 50
- events[i] consists of lowercase English letters, digits, underscores, and hyphens
- The answer must be computed in O(n) or O(n log n) time for full credit

Example 1:
Input: events = ["auth_ok", "cache_miss", "db_retry", "cache_miss", "email_sent"]
Output: 3
Explanation: The longest valid span is ["auth_ok", "cache_miss", "db_retry"] or ["db_retry", "cache_miss", "email_sent"], both of length 3.

Example 2:
Input: events = ["x1", "x2", "x3", "x2", "x4", "x5"]
Output: 4
Explanation: One longest valid span is ["x3", "x2", "x4", "x5"]. No signature repeats within this contiguous segment.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each event is processed once as the right pointer moves from left to right.
    - The left boundary only moves forward, never backward.
    - Dictionary lookups and updates are O(1) on average.

    Space Complexity: O(n)
    - In the worst case, all event signatures are unique, so the dictionary stores up to n entries.

    Core idea:
    We use the "sliding window" technique.
    The window is the current contiguous segment [left..right] that contains only unique event signatures.

    We also use a dictionary:
    - Key   = event signature string
    - Value = the most recent index where that signature appeared

    Why this works:
    - When we see a signature for the first time, we can safely extend the window.
    - When we see a duplicate signature, we must move the left side of the window forward
      so that the duplicate is removed from the current window.
    - To do that efficiently, we jump left directly to one position after the previous occurrence.
    */
    public int LengthOfLongestUniqueSpan(string[] events)
    {
        // This dictionary remembers the most recent index where each event signature appeared.
        // Example:
        // if lastSeen["cache_miss"] == 3, that means the latest "cache_miss" we have processed
        // was at index 3.
        var lastSeen = new Dictionary<string, int>();

        // "left" marks the start of the current valid window.
        // The window always represents a contiguous subarray with all unique signatures.
        int left = 0;

        // "maxLength" stores the best answer found so far.
        int maxLength = 0;

        // We expand the window by moving "right" from left to right across the array.
        for (int right = 0; right < events.Length; right++)
        {
            // Read the current event signature at the right edge of the window.
            string currentEvent = events[right];

            // Step 1:
            // Check whether this event signature has been seen before.
            //
            // Why this matters:
            // If the signature was seen before, it may create a duplicate inside the current window.
            // But it only causes a problem if that previous occurrence is still inside the current window,
            // meaning its index is >= left.
            if (lastSeen.TryGetValue(currentEvent, out int previousIndex))
            {
                // Step 2:
                // If the previous occurrence is inside the current window,
                // we must move "left" to remove the duplicate.
                //
                // Example:
                // events = ["a", "b", "c", "b"]
                // when right = 3 ("b"), previousIndex = 1
                // current window before adjustment is [0..3]
                // that window contains two "b" values, so it is invalid
                //
                // To fix it, move left to previousIndex + 1 = 2
                // new window becomes [2..3] => ["c", "b"], which is valid
                //
                // We use Math.Max because left should never move backward.
                // If previousIndex is before the current left boundary, then that old duplicate
                // is already outside the window and does not matter anymore.
                if (previousIndex >= left)
                {
                    left = previousIndex + 1;
                }
            }

            // Step 3:
            // Update the most recent index of the current event signature to "right".
            //
            // Why this is necessary:
            // Future duplicates need to know the latest position of this signature.
            // We always overwrite with the newest index because that is the one that matters most.
            lastSeen[currentEvent] = right;

            // Step 4:
            // Compute the current window length.
            //
            // Since the current valid window is [left..right], its size is:
            // right - left + 1
            int currentLength = right - left + 1;

            // Step 5:
            // Update the best answer if the current valid window is larger.
            //
            // This ensures that by the end of the loop, maxLength stores the length
            // of the longest contiguous subarray with all unique signatures.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After processing all events, return the best length found.
        return maxLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] events1 = { "auth_ok", "cache_miss", "db_retry", "cache_miss", "email_sent" };
int result1 = solution.LengthOfLongestUniqueSpan(events1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 3

// Example 2
string[] events2 = { "x1", "x2", "x3", "x2", "x4", "x5" };
int result2 = solution.LengthOfLongestUniqueSpan(events2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 4

// Additional demo 1: all unique
string[] events3 = { "boot", "auth", "db", "cache", "email" };
int result3 = solution.LengthOfLongestUniqueSpan(events3);
Console.WriteLine("All Unique Result: " + result3); // Expected: 5

// Additional demo 2: all same
string[] events4 = { "repeat", "repeat", "repeat", "repeat" };
int result4 = solution.LengthOfLongestUniqueSpan(events4);
Console.WriteLine("All Same Result: " + result4); // Expected: 1

// Additional demo 3: duplicate appears after window has moved
string[] events5 = { "a", "b", "c", "a", "d", "e" };
int result5 = solution.LengthOfLongestUniqueSpan(events5);
Console.WriteLine("Mixed Result: " + result5); // Expected: 5