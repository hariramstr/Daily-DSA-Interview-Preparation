/*
Title: Longest Work Block With Limited App Switching

Problem Description:
You are given an array apps where apps[i] is the name of the application a user was focused on during the i-th minute of a work session. A contiguous block of minutes is called efficient if the user switched between at most k distinct applications inside that block. Your task is to return the length of the longest efficient block.

More formally, find the maximum length of a contiguous subarray of apps that contains no more than k distinct strings.

This problem models productivity analysis in desktop telemetry systems, where frequent switching across too many tools in a short period may indicate fragmented work. You must process the session efficiently because the log can be large.

Return 0 if the input array is empty. You may assume k is non-negative. If k = 0, no non-empty block is valid, so the answer is 0.

Constraints:
- 0 <= apps.length <= 200000
- 0 <= k <= apps.length
- 1 <= apps[i].length <= 20
- apps[i] consists of lowercase English letters, digits, underscores, or hyphens

Example 1:
Input: apps = ["mail","docs","mail","chat","docs","docs"], k = 2
Output: 3
Explanation: One longest valid block is ["docs","mail","chat"]? No, that has 3 distinct apps, so it is invalid. The longest valid blocks include ["mail","docs","mail"] and ["chat","docs","docs"], both of length 3.

Example 2:
Input: apps = ["ide","ide","browser","terminal","browser","terminal","music"], k = 3
Output: 6
Explanation: The subarray ["ide","browser","terminal","browser","terminal","music"] has 4 distinct apps, so it is invalid. A longest valid block is ["ide","ide","browser","terminal","browser","terminal"], which contains exactly 3 distinct apps and has length 6.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n), where n is the number of minutes / length of the apps array.
    - Each element is added to the sliding window once and removed from it at most once.

    Space Complexity:
    - O(k) in the typical valid-window sense, or more precisely O(min(n, number of distinct app names in input)).
    - We store counts of app names currently inside the window in a dictionary.
    */
    public int LengthOfLongestEfficientBlock(string[] apps, int k)
    {
        // If the input array is empty, there is no block at all,
        // so the longest valid block length must be 0.
        if (apps == null || apps.Length == 0)
        {
            return 0;
        }

        // If k is 0, we are allowed to have at most 0 distinct applications.
        // Any non-empty subarray would contain at least 1 distinct application,
        // so no non-empty block can be valid.
        if (k == 0)
        {
            return 0;
        }

        // This dictionary stores how many times each app name appears
        // inside the current sliding window.
        //
        // Why do we need counts instead of just a set?
        // Because when we move the left side of the window forward,
        // we need to know whether an app still exists somewhere else in the window.
        // If its count drops to 0, only then should it stop being considered distinct.
        var frequency = new Dictionary<string, int>();

        // left marks the beginning of the current window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // We expand the window by moving right from 0 to apps.Length - 1.
        for (int right = 0; right < apps.Length; right++)
        {
            string currentApp = apps[right];

            // Step 1: Include the new app at position right into the window.
            //
            // This means the current window is now apps[left..right].
            // We update its frequency count in the dictionary.
            if (!frequency.ContainsKey(currentApp))
            {
                frequency[currentApp] = 0;
            }

            frequency[currentApp]++;

            // Step 2: If the window now contains too many distinct apps,
            // it is invalid and must be shrunk from the left.
            //
            // We keep shrinking until the number of distinct apps is at most k again.
            while (frequency.Count > k)
            {
                string leftApp = apps[left];

                // We are removing apps[left] from the window,
                // because we are about to move left forward by one step.
                frequency[leftApp]--;

                // If the count becomes 0, that app no longer exists in the window.
                // Therefore, it should no longer count as one of the distinct apps.
                if (frequency[leftApp] == 0)
                {
                    frequency.Remove(leftApp);
                }

                // Move the left boundary to the right,
                // making the window smaller until it becomes valid again.
                left++;
            }

            // Step 3: At this point, the window is guaranteed to be valid:
            // it contains at most k distinct app names.
            //
            // So we can safely compute its length and compare it with the best answer.
            int currentLength = right - left + 1;

            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // After checking all possible windows formed during the sliding process,
        // best contains the length of the longest valid contiguous block.
        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] apps1 = { "mail", "docs", "mail", "chat", "docs", "docs" };
int k1 = 2;
int result1 = solution.LengthOfLongestEfficientBlock(apps1, k1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 3

// Example 2
string[] apps2 = { "ide", "ide", "browser", "terminal", "browser", "terminal", "music" };
int k2 = 3;
int result2 = solution.LengthOfLongestEfficientBlock(apps2, k2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 6

// Additional edge case: empty input
string[] apps3 = Array.Empty<string>();
int k3 = 2;
int result3 = solution.LengthOfLongestEfficientBlock(apps3, k3);
Console.WriteLine($"Empty Input Result: {result3}"); // Expected: 0

// Additional edge case: k = 0
string[] apps4 = { "mail", "docs", "mail" };
int k4 = 0;
int result4 = solution.LengthOfLongestEfficientBlock(apps4, k4);
Console.WriteLine($"k = 0 Result: {result4}"); // Expected: 0

// Additional test: all same app
string[] apps5 = { "editor", "editor", "editor", "editor" };
int k5 = 1;
int result5 = solution.LengthOfLongestEfficientBlock(apps5, k5);
Console.WriteLine($"All Same App Result: {result5}"); // Expected: 4