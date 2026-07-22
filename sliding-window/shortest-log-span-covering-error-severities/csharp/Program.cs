/*
Title: Shortest Log Span Covering Error Severities
Difficulty: Hard
Topic: Sliding Window

Problem Description:
A monitoring system records a stream of application logs as an array `logs`, where each element is an integer severity level.
You are also given another array `required`, where each integer represents a severity level that must appear in a chosen
contiguous log span. Duplicates in `required` matter: if `required = [2, 2, 5]`, then the chosen span must contain severity
`2` at least twice and severity `5` at least once.

Your task is to return the length of the shortest contiguous subarray of `logs` that satisfies all required severity counts.
If no such subarray exists, return `-1`.

This problem models alert triage, where investigators need the smallest time window containing all critical error patterns,
including repeated occurrences of the same severity. The arrays may be large, so solutions that check every possible subarray
will be too slow.

Constraints:
- 1 <= logs.length <= 2 * 10^5
- 1 <= required.length <= 2 * 10^5
- 1 <= logs[i], required[i] <= 10^9
- The answer should be computed in better than O(n^2) time.

Example 1:
Input: logs = [4, 2, 7, 2, 5, 1, 2, 5], required = [2, 5, 2]
Output: 4
Explanation: The shortest valid span is [2, 7, 2, 5], which contains severity 2 twice and severity 5 once.

Example 2:
Input: logs = [3, 1, 4, 1, 5, 9], required = [1, 1, 2]
Output: -1
Explanation: No contiguous span can satisfy the requirement because severity 2 never appears in logs.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
        Time Complexity:
        - O(n + m), where:
          n = logs.Length
          m = required.Length
        Why:
        1. We first count frequencies in `required` -> O(m)
        2. We then move a sliding window across `logs`
           - the right pointer moves from left to right once -> O(n)
           - the left pointer also moves from left to right at most once total -> O(n)
        So the total is linear.

        Space Complexity:
        - O(k), where k is the number of distinct severity values that appear in `required`
        Why:
        - We store required counts in one dictionary
        - We store current window counts in another dictionary
        Both are bounded by the number of distinct required values.
    */
    public int ShortestLogSpan(int[] logs, int[] required)
    {
        // This dictionary tells us exactly how many times each severity level
        // must appear in a valid window.
        //
        // Example:
        // required = [2, 5, 2]
        // needCount becomes:
        // 2 -> 2
        // 5 -> 1
        //
        // We use Dictionary<int, int> because severity values can be as large as 1e9,
        // so using an array indexed by severity would be impossible or wasteful.
        var needCount = new Dictionary<int, int>();

        // Build the frequency map for the required severities.
        foreach (int severity in required)
        {
            if (!needCount.TryAdd(severity, 1))
            {
                needCount[severity]++;
            }
        }

        // `requiredKinds` means:
        // how many distinct severity values must be satisfied.
        //
        // For required = [2, 2, 5], there are 2 distinct kinds:
        // - severity 2
        // - severity 5
        int requiredKinds = needCount.Count;

        // This dictionary tracks how many times each relevant severity
        // currently appears inside our sliding window.
        var windowCount = new Dictionary<int, int>();

        // `formedKinds` counts how many distinct severity values are currently satisfied.
        //
        // A severity is "satisfied" when:
        // windowCount[severity] >= needCount[severity]
        //
        // More precisely, we increment `formedKinds` exactly when a severity reaches
        // its required count for the first time.
        int formedKinds = 0;

        // `left` is the left boundary of the sliding window.
        int left = 0;

        // We will store the best (smallest) window length found so far.
        // Start with int.MaxValue to mean "no valid window found yet".
        int bestLength = int.MaxValue;

        // Expand the window by moving `right` from left to right.
        for (int right = 0; right < logs.Length; right++)
        {
            int currentSeverity = logs[right];

            // Step 1: Include logs[right] into the current window.
            //
            // We only care about counting values that are actually required.
            // If a severity is not in `needCount`, it can still sit inside the window,
            // but it does not help satisfy the requirement, so we do not need to track it.
            if (needCount.ContainsKey(currentSeverity))
            {
                if (!windowCount.TryAdd(currentSeverity, 1))
                {
                    windowCount[currentSeverity]++;
                }

                // Step 2: Check whether adding this severity made one requirement fully satisfied.
                //
                // Example:
                // needCount[2] = 2
                // If windowCount[2] becomes exactly 2 now, then severity 2 is satisfied.
                //
                // We only increment `formedKinds` when the count becomes EXACTLY equal,
                // not when it becomes greater, because each distinct severity should only
                // contribute once to `formedKinds`.
                if (windowCount[currentSeverity] == needCount[currentSeverity])
                {
                    formedKinds++;
                }
            }

            // Step 3: If all required severity kinds are satisfied,
            // try to shrink the window from the left to make it as short as possible.
            //
            // This is the heart of the sliding window technique:
            // - Expand right until the window becomes valid
            // - Then contract left while it stays valid
            // This guarantees we find the minimum-length valid window efficiently.
            while (formedKinds == requiredKinds)
            {
                // The current window is [left .. right], inclusive.
                int currentLength = right - left + 1;

                // Update the best answer if this valid window is smaller.
                if (currentLength < bestLength)
                {
                    bestLength = currentLength;
                }

                // We now try to remove logs[left] and see whether the window
                // can remain valid after shrinking.
                int leftSeverity = logs[left];

                // Again, only required severities matter for validity counts.
                if (needCount.ContainsKey(leftSeverity))
                {
                    // Before decrementing, if this severity is currently exactly satisfied,
                    // then removing one occurrence will make it unsatisfied.
                    //
                    // Example:
                    // needCount[5] = 1
                    // windowCount[5] = 1
                    // If we remove a 5, windowCount[5] becomes 0, so the window is no longer valid.
                    //
                    // Therefore, we must reduce `formedKinds` BEFORE or while we decrement.
                    if (windowCount[leftSeverity] == needCount[leftSeverity])
                    {
                        formedKinds--;
                    }

                    windowCount[leftSeverity]--;
                }

                // Move the left boundary rightward to shrink the window.
                left++;
            }
        }

        // If bestLength was never updated, no valid window exists.
        return bestLength == int.MaxValue ? -1 : bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] logs1 = { 4, 2, 7, 2, 5, 1, 2, 5 };
int[] required1 = { 2, 5, 2 };
int result1 = solution.ShortestLogSpan(logs1, required1);
Console.WriteLine(result1); // Expected: 4

// Example 2
int[] logs2 = { 3, 1, 4, 1, 5, 9 };
int[] required2 = { 1, 1, 2 };
int result2 = solution.ShortestLogSpan(logs2, required2);
Console.WriteLine(result2); // Expected: -1

// Additional quick sanity checks
int[] logs3 = { 2, 2, 5 };
int[] required3 = { 2, 2, 5 };
Console.WriteLine(solution.ShortestLogSpan(logs3, required3)); // Expected: 3

int[] logs4 = { 1, 2, 3, 2, 5, 2 };
int[] required4 = { 2, 5 };
Console.WriteLine(solution.ShortestLogSpan(logs4, required4)); // Expected: 2