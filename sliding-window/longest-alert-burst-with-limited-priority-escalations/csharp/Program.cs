/*
Title: Longest Alert Burst With Limited Priority Escalations
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A monitoring system records a stream of alert priorities over time as a string alerts, where each character is either 'L' (low priority) or 'H' (high priority). The operations team wants to identify the longest contiguous burst of alerts that can be treated as a mostly low-priority incident window.

You are allowed to escalate at most k high-priority alerts inside a chosen contiguous window, meaning those 'H' alerts can be treated as if they were 'L' for reporting purposes. Return the length of the longest contiguous substring that can be made entirely low priority after at most k such escalations.

In other words, find the maximum window length containing at most k occurrences of 'H'.

This problem models real incident analysis, where a team may tolerate a small number of severe alerts inside an otherwise routine burst. An efficient solution is expected because the alert stream can be very large.

Constraints:
- 1 <= alerts.length <= 200000
- alerts[i] is either 'L' or 'H'
- 0 <= k <= alerts.length

Example 1:
Input: alerts = "LLHLLHLLL", k = 1
Output: 5
Explanation: The window "LHLLL" contains exactly one 'H', so it can be fully treated as low priority after one escalation. No longer valid window exists.

Example 2:
Input: alerts = "HHLLLHLH", k = 2
Output: 6
Explanation: One optimal window is "HLLLHL", which contains two 'H' characters. After escalating both, the entire window is considered low priority, giving length 6.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each character is processed by the right pointer once.
    - Each character is removed from the window by the left pointer at most once.
    - Therefore, the total work is linear in the length of the string.

    Space Complexity: O(1)
    - We only store a few integer variables.
    - No extra data structures proportional to input size are used.
    */
    public int LongestAlertBurst(string alerts, int k)
    {
        // This pointer marks the beginning of our current sliding window.
        // The window will always represent a contiguous substring alerts[left..right].
        int left = 0;

        // This variable counts how many 'H' characters currently exist inside the window.
        // Why do we track this?
        // Because the rule says a valid window can contain at most k high-priority alerts.
        // If the count becomes greater than k, the window is invalid and must be shrunk.
        int highCount = 0;

        // This variable stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // We expand the window one character at a time using the right pointer.
        // right moves from the start of the string to the end.
        for (int right = 0; right < alerts.Length; right++)
        {
            // Step 1: Include alerts[right] in the current window.
            // If this new character is 'H', then the number of high-priority alerts
            // inside the window increases by 1.
            if (alerts[right] == 'H')
            {
                highCount++;
            }

            // Step 2: If the window has too many 'H' characters, it is invalid.
            // We are allowed to escalate at most k high-priority alerts.
            // So while highCount > k, we must move the left boundary to the right
            // until the window becomes valid again.
            while (highCount > k)
            {
                // Before moving left forward, check whether the character leaving
                // the window is an 'H'. If it is, then the number of 'H' characters
                // inside the window decreases by 1.
                if (alerts[left] == 'H')
                {
                    highCount--;
                }

                // Shrink the window from the left.
                left++;
            }

            // Step 3: At this point, the window alerts[left..right] is guaranteed valid,
            // because it contains at most k 'H' characters.
            // So we can safely compute its length.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is longer
            // than any valid window we have seen before.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After scanning the entire string, maxLength contains the answer.
        return maxLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string alerts1 = "LLHLLHLLL";
int k1 = 1;
int result1 = solution.LongestAlertBurst(alerts1, k1);
Console.WriteLine($"Input: alerts = \"{alerts1}\", k = {k1}");
Console.WriteLine($"Output: {result1}");
Console.WriteLine("Expected: 5");
Console.WriteLine();

// Example 2
string alerts2 = "HHLLLHLH";
int k2 = 2;
int result2 = solution.LongestAlertBurst(alerts2, k2);
Console.WriteLine($"Input: alerts = \"{alerts2}\", k = {k2}");
Console.WriteLine($"Output: {result2}");
Console.WriteLine("Expected: 6");
Console.WriteLine();

// Additional demo cases for learning

string alerts3 = "LLLLL";
int k3 = 0;
int result3 = solution.LongestAlertBurst(alerts3, k3);
Console.WriteLine($"Input: alerts = \"{alerts3}\", k = {k3}");
Console.WriteLine($"Output: {result3}");
Console.WriteLine("Expected: 5");
Console.WriteLine();

string alerts4 = "HHHHH";
int k4 = 2;
int result4 = solution.LongestAlertBurst(alerts4, k4);
Console.WriteLine($"Input: alerts = \"{alerts4}\", k = {k4}");
Console.WriteLine($"Output: {result4}");
Console.WriteLine("Expected: 2");
Console.WriteLine();

string alerts5 = "LHLHLHLH";
int k5 = 3;
int result5 = solution.LongestAlertBurst(alerts5, k5);
Console.WriteLine($"Input: alerts = \"{alerts5}\", k = {k5}");
Console.WriteLine($"Output: {result5}");
Console.WriteLine("Expected: 7");