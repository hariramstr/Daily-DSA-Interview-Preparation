/*
Title: Shortest Maintenance Span Covering All Tool Classes

Problem Description:
A factory records the sequence of tools used during a long maintenance session.
Each tool use is represented by an integer tool class ID in the array tools,
where tools[i] is the class of the i-th tool used.

You are also given an integer array required, where required[j] is a tool class
that must appear at least once inside a valid contiguous span.

Important detail:
The required array may contain duplicates, meaning the span must include that many
occurrences of the corresponding class.
Example:
required = [2, 2, 5]
This means a valid span must contain:
- at least two occurrences of tool class 2
- at least one occurrence of tool class 5

Goal:
Return the length of the shortest contiguous subarray of tools that satisfies all
requirements. If no such span exists, return -1.

Constraints:
- 1 <= tools.length <= 200000
- 1 <= required.length <= 200000
- 1 <= tools[i], required[i] <= 10^9
- The answer fits in a 32-bit signed integer

Examples:
1)
tools = [7,2,3,2,5,2,1,5]
required = [2,5,2]
Output: 3
Explanation:
The shortest valid span is [2,5,2], which contains:
- two 2s
- one 5

2)
tools = [4,1,4,3,6,1,3]
required = [1,3,3]
Output: -1
Explanation:
A valid span would need two occurrences of tool class 3, but the array only has one 3,
so no valid contiguous span exists.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n + m)
    - n = tools.Length
    - m = required.Length
    We build the requirement counts in O(m), then process the tools array with a sliding
    window in O(n). Each index moves forward at most once as the right pointer expands
    and the left pointer shrinks.

    Space Complexity:
    O(k)
    - k = number of distinct tool classes appearing in required, plus the matching window counts
    We store frequency maps for required counts and current window counts.
    */
    public int ShortestMaintenanceSpan(int[] tools, int[] required)
    {
        // Step 1:
        // Build a frequency map for the required tool classes.
        //
        // Why this is necessary:
        // The required array may contain duplicates. That means we cannot simply check whether
        // a tool class appears at least once. We must know exactly how many times each class
        // is needed.
        //
        // Example:
        // required = [2, 2, 5]
        // Then we need:
        // requiredCount[2] = 2
        // requiredCount[5] = 1
        //
        // Data structure choice:
        // We use Dictionary<int, int> because tool class IDs can be as large as 10^9,
        // so using an array indexed by tool class would be wasteful or impossible.
        var requiredCount = new Dictionary<int, int>();

        foreach (int toolClass in required)
        {
            if (!requiredCount.TryAdd(toolClass, 1))
            {
                requiredCount[toolClass]++;
            }
        }

        // Step 2:
        // Count how many total required occurrences must be satisfied.
        //
        // Why this is necessary:
        // We want a simple way to know when the current window is valid.
        // Instead of repeatedly checking every key in the dictionary, we track how many
        // individual required occurrences have been matched so far.
        //
        // For example:
        // required = [2, 2, 5]
        // totalNeeded = 3
        //
        // If the current window contains:
        // - one 2 and one 5, then matched = 2 (not enough)
        // - two 2s and one 5, then matched = 3 (valid window)
        int totalNeeded = required.Length;

        // Step 3:
        // Prepare the sliding window state.
        //
        // windowCount:
        // Stores how many times each relevant tool class appears inside the current window.
        //
        // left:
        // Left boundary of the current window.
        //
        // matched:
        // Number of required occurrences currently satisfied.
        //
        // bestLength:
        // The shortest valid window length found so far.
        var windowCount = new Dictionary<int, int>();
        int left = 0;
        int matched = 0;
        int bestLength = int.MaxValue;

        // Step 4:
        // Expand the window by moving the right pointer from left to right across the tools array.
        //
        // Why this is necessary:
        // We want to consider every possible ending position of a window, while maintaining
        // counts efficiently.
        for (int right = 0; right < tools.Length; right++)
        {
            int currentTool = tools[right];

            // Step 4a:
            // Only update window counts if this tool class is actually relevant to the requirement.
            //
            // Why this is necessary:
            // Tool classes not present in required do not help satisfy the condition.
            // Ignoring them keeps the dictionary smaller and the logic clearer.
            if (requiredCount.ContainsKey(currentTool))
            {
                if (!windowCount.TryAdd(currentTool, 1))
                {
                    windowCount[currentTool]++;
                }

                // Step 4b:
                // If adding this tool does not exceed the required amount for this class,
                // then it contributes to satisfying one more required occurrence.
                //
                // Why this is correct:
                // Suppose requiredCount[2] = 2.
                // - First 2 in the window helps: matched++
                // - Second 2 in the window helps: matched++
                // - Third 2 in the window does NOT help further, because we already have enough 2s
                //
                // So we only increase matched when:
                // windowCount[currentTool] <= requiredCount[currentTool]
                if (windowCount[currentTool] <= requiredCount[currentTool])
                {
                    matched++;
                }
            }

            // Step 5:
            // If matched == totalNeeded, then the current window [left..right] is valid.
            //
            // Why this is important:
            // Once the window is valid, we should try to shrink it from the left to remove
            // unnecessary elements and possibly find a shorter valid window.
            while (matched == totalNeeded)
            {
                // Step 5a:
                // Update the best answer using the current valid window length.
                int currentLength = right - left + 1;
                if (currentLength < bestLength)
                {
                    bestLength = currentLength;
                }

                // Step 5b:
                // Try to remove tools[left] from the window and move left forward.
                //
                // Why this is necessary:
                // This is the heart of the sliding window optimization:
                // - expand right until valid
                // - shrink left while still valid
                // This guarantees we find the shortest valid window for each right boundary.
                int leftTool = tools[left];

                if (requiredCount.ContainsKey(leftTool))
                {
                    // We are about to remove one occurrence of leftTool from the window.
                    windowCount[leftTool]--;

                    // Step 5c:
                    // If after removing it, the count falls below what is required,
                    // then the window is no longer valid.
                    //
                    // Why this is correct:
                    // Example:
                    // requiredCount[2] = 2
                    // If window had exactly two 2s and we remove one, then now it has only one 2,
                    // so one required occurrence is no longer satisfied.
                    if (windowCount[leftTool] < requiredCount[leftTool])
                    {
                        matched--;
                    }
                }

                left++;
            }
        }

        // Step 6:
        // If bestLength was never updated, no valid window exists.
        //
        // Example 2:
        // tools = [4,1,4,3,6,1,3]
        // required = [1,3,3]
        // We need two 3s, but only one 3 exists in the array.
        // Therefore matched can never reach totalNeeded, and bestLength remains int.MaxValue.
        return bestLength == int.MaxValue ? -1 : bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] tools1 = { 7, 2, 3, 2, 5, 2, 1, 5 };
int[] required1 = { 2, 5, 2 };
int result1 = solution.ShortestMaintenanceSpan(tools1, required1);
Console.WriteLine(result1); // Expected: 3

// Example 2
int[] tools2 = { 4, 1, 4, 3, 6, 1, 3 };
int[] required2 = { 1, 3, 3 };
int result2 = solution.ShortestMaintenanceSpan(tools2, required2);
Console.WriteLine(result2); // Expected: -1

// Additional quick demo
int[] tools3 = { 5, 2, 2, 8, 5 };
int[] required3 = { 2, 5 };
int result3 = solution.ShortestMaintenanceSpan(tools3, required3);
Console.WriteLine(result3); // One shortest valid span is [5,2] or [2,8,5], expected shortest length: 2