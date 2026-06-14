/*
Title: Shortest Error Burst Covering All Failure Codes

Problem Description:
You are given a chronological stream of application error reports represented by an integer array reports,
where reports[i] is the failure code produced at time i. You are also given an integer array required
containing the set of distinct failure codes that an incident investigation must observe at least once.
In addition, some failure codes may appear many times in the stream, and the same required code may be
scattered far apart.

Your task is to find the length of the shortest contiguous subarray of reports that contains every code
in required at least once and also contains at least k total occurrences of critical codes, where critical
is another integer array of distinct codes. A report whose code belongs to critical contributes 1 toward
this count even if that code is not in required. If no such subarray exists, return -1.

Formally, find the minimum length of a window reports[l..r] such that:
1. Every value in required appears at least once in reports[l..r].
2. The number of indices j in [l, r] with reports[j] in critical is at least k.

The arrays required and critical may overlap partially, completely, or not at all. All codes are positive integers.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n + r + c), where:
      n = reports.Length
      r = required.Length
      c = critical.Length
    Explanation:
    - We build two hash sets in O(r + c).
    - We use a sliding window where each pointer (left and right) moves at most n times total.
    - Therefore the main loop is O(n).

    Space Complexity:
    - O(r + c)
    Explanation:
    - HashSet for required values
    - HashSet for critical values
    - Dictionary for counts of required values currently inside the window
    */
    public int ShortestErrorBurstCoveringAllFailureCodes(int[] reports, int[] required, int[] critical, int k)
    {
        // Convert required codes into a HashSet so we can check membership in O(1) average time.
        // We need to know whether a report value is one of the required codes.
        var requiredSet = new HashSet<int>(required);

        // Convert critical codes into a HashSet for the same reason:
        // fast membership checks while expanding/shrinking the sliding window.
        var criticalSet = new HashSet<int>(critical);

        // This dictionary stores how many times each required code appears in the current window.
        // We only track counts for required codes because non-required values do not matter for
        // the "contains every required code" condition.
        var requiredCountsInWindow = new Dictionary<int, int>();

        // This tells us how many distinct required codes are currently satisfied in the window.
        // A required code is considered satisfied if its count in the window is at least 1.
        int satisfiedRequiredDistinctCount = 0;

        // Total number of distinct required codes we must cover.
        int totalRequiredDistinctCount = requiredSet.Count;

        // This counts how many report positions inside the current window contain a critical code.
        // Important: this is total occurrences, not distinct critical values.
        int criticalOccurrencesInWindow = 0;

        // Standard sliding window left pointer.
        int left = 0;

        // Best answer found so far. Start with "infinity" style value.
        int bestLength = int.MaxValue;

        // Expand the window by moving the right pointer from left to right across the array.
        for (int right = 0; right < reports.Length; right++)
        {
            int currentValue = reports[right];

            // Step 1: include reports[right] into the window.
            // If it is a required code, update its count.
            if (requiredSet.Contains(currentValue))
            {
                // Get the old count if present; otherwise treat it as 0.
                requiredCountsInWindow.TryGetValue(currentValue, out int oldCount);
                int newCount = oldCount + 1;
                requiredCountsInWindow[currentValue] = newCount;

                // If the count changed from 0 to 1, this required code has now become satisfied.
                // That means the window now covers one more distinct required value.
                if (oldCount == 0)
                {
                    satisfiedRequiredDistinctCount++;
                }
            }

            // Step 2: if the current value is critical, it contributes one occurrence.
            // This is independent of whether it is also required.
            if (criticalSet.Contains(currentValue))
            {
                criticalOccurrencesInWindow++;
            }

            // Step 3: while the current window is valid, try to shrink it from the left.
            // A window is valid if:
            // - it contains all distinct required codes at least once
            // - it contains at least k total critical occurrences
            //
            // Why do we shrink here?
            // Because once a window is valid, any larger window ending at the same right index
            // is not better than the smallest valid one. So we greedily remove unnecessary items
            // from the left to minimize the length.
            while (satisfiedRequiredDistinctCount == totalRequiredDistinctCount &&
                   criticalOccurrencesInWindow >= k)
            {
                // The current [left..right] window is valid, so record its length.
                int currentLength = right - left + 1;
                if (currentLength < bestLength)
                {
                    bestLength = currentLength;
                }

                // Now we attempt to remove reports[left] and see whether the window remains valid.
                int leftValue = reports[left];

                // If the outgoing value is required, we must reduce its count.
                if (requiredSet.Contains(leftValue))
                {
                    int oldCount = requiredCountsInWindow[leftValue];
                    int newCount = oldCount - 1;

                    if (newCount == 0)
                    {
                        // Once the count drops to zero, this required code is no longer present
                        // in the window, so the window loses one satisfied required code.
                        requiredCountsInWindow.Remove(leftValue);
                        satisfiedRequiredDistinctCount--;
                    }
                    else
                    {
                        requiredCountsInWindow[leftValue] = newCount;
                    }
                }

                // If the outgoing value is critical, we must reduce the critical occurrence count.
                if (criticalSet.Contains(leftValue))
                {
                    criticalOccurrencesInWindow--;
                }

                // Finally move the left boundary rightward by one.
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
int[] reports1 = { 7, 4, 9, 4, 2, 8, 9, 2, 5 };
int[] required1 = { 4, 2, 5 };
int[] critical1 = { 9, 8, 5 };
int k1 = 2;

int result1 = solution.ShortestErrorBurstCoveringAllFailureCodes(reports1, required1, critical1, k1);
Console.WriteLine(result1); // Expected: 6

// Example 2
int[] reports2 = { 1, 3, 1, 6, 7, 3, 6 };
int[] required2 = { 1, 6 };
int[] critical2 = { 3 };
int k2 = 2;

int result2 = solution.ShortestErrorBurstCoveringAllFailureCodes(reports2, required2, critical2, k2);
Console.WriteLine(result2); // Expected: 5

// Additional quick check: k = 0
int[] reports3 = { 10, 20, 30, 40, 20 };
int[] required3 = { 20, 40 };
int[] critical3 = { 99 };
int k3 = 0;

int result3 = solution.ShortestErrorBurstCoveringAllFailureCodes(reports3, required3, critical3, k3);
Console.WriteLine(result3); // Expected: 2 ([40,20] or [20,30,40] shortest is 2 with indices 3..4? actually [40,20])

// Additional quick check: impossible
int[] reports4 = { 1, 2, 3 };
int[] required4 = { 1, 4 };
int[] critical4 = { 2 };
int k4 = 1;

int result4 = solution.ShortestErrorBurstCoveringAllFailureCodes(reports4, required4, critical4, k4);
Console.WriteLine(result4); // Expected: -1