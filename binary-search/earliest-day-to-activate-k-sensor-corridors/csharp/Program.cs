/*
Title: Earliest Day to Activate K Sensor Corridors

Problem Description:
A research facility has deployed sensors along a long hallway. There are n sensors in a fixed left-to-right order, and sensor i becomes operational on day activationDay[i]. A corridor is defined as a contiguous block of sensors in this order. A corridor is considered valid on day D if every sensor inside that block is operational by day D, and the length of the block is at least minLen and at most maxLen.

The facility wants to activate at least k non-overlapping valid corridors as early as possible. Two corridors are non-overlapping if they do not share any sensor index. You may choose any corridor lengths within the allowed range [minLen, maxLen], and you do not need to use all operational sensors.

Return the earliest day D such that it is possible to select at least k non-overlapping valid corridors. If it is impossible even after all sensors have become operational, return -1.

This problem is designed to reward a binary search on the answer. For a fixed day D, each sensor can be treated as either active or inactive, and the challenge is to determine whether at least k disjoint valid segments can be formed from the active runs. A correct solution must handle large inputs efficiently.

Constraints:
- 1 <= n <= 200000
- 1 <= activationDay[i] <= 1000000000
- 1 <= k <= n
- 1 <= minLen <= maxLen <= n
- activationDay.length == n

Example 1:
Input: activationDay = [4, 2, 5, 3, 3, 6, 1], k = 2, minLen = 2, maxLen = 3
Output: 4

Example 2 (corrected):
Input: activationDay = [7, 7, 7], k = 2, minLen = 2, maxLen = 2
Output: -1
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Binary search over the answer: O(log M), where M is the range of activation days
    - Feasibility check for one day: O(n)
    - Total: O(n log M)

    Space Complexity:
    - O(1) extra space beyond the input array

    Beginner-friendly intuition:
    1. If a certain day D works, then any later day also works, because more sensors become active.
       That "works / does not work" pattern is monotonic, so binary search is a perfect fit.
    2. For a fixed day D, each sensor is either active or inactive.
    3. Active sensors form runs of consecutive active positions.
    4. Inside one active run of length L, the maximum number of non-overlapping valid corridors is floor(L / minLen),
       as long as L >= minLen. Why?
       - Every chosen corridor must use at least minLen sensors.
       - So no solution can use more than floor(L / minLen) corridors.
       - This upper bound is achievable by simply cutting the run into pieces of exactly minLen,
         because minLen is allowed and also minLen <= maxLen.
    5. Therefore, for a fixed day D, we only need to sum floor(runLength / minLen) over all active runs.
       If the total is at least k, then day D is feasible.
    */
    public int EarliestDayToActivateKCorridors(int[] activationDay, int k, int minLen, int maxLen)
    {
        int n = activationDay.Length;

        // Quick impossible check:
        // Even if every sensor is active, each corridor needs at least minLen sensors.
        // So k corridors need at least k * minLen total sensors.
        // If we do not even have that many sensors overall, the answer must be -1.
        if ((long)k * minLen > n)
        {
            return -1;
        }

        // We binary search on the day.
        // The smallest possible candidate day is the minimum activation day in the array.
        // The largest possible candidate day is the maximum activation day in the array.
        int low = int.MaxValue;
        int high = int.MinValue;

        foreach (int day in activationDay)
        {
            if (day < low) low = day;
            if (day > high) high = day;
        }

        // Before binary searching, it is useful to verify that the problem is feasible at all
        // on the latest day when every sensor that will ever activate is active.
        // If even that day cannot produce k corridors, then the answer is -1.
        if (!CanMakeAtLeastK(activationDay, k, minLen, high))
        {
            return -1;
        }

        // Standard binary search for the first feasible day.
        while (low < high)
        {
            int mid = low + (high - low) / 2;

            // If mid is feasible, then the earliest answer is <= mid,
            // so we keep the left half including mid.
            if (CanMakeAtLeastK(activationDay, k, minLen, mid))
            {
                high = mid;
            }
            else
            {
                // If mid is not feasible, the answer must be after mid.
                low = mid + 1;
            }
        }

        return low;
    }

    private bool CanMakeAtLeastK(int[] activationDay, int k, int minLen, int day)
    {
        // This method answers:
        // "By this specific day, can we form at least k non-overlapping valid corridors?"

        long corridorsFormed = 0;
        int currentActiveRunLength = 0;

        // We scan from left to right and group consecutive active sensors into runs.
        for (int i = 0; i < activationDay.Length; i++)
        {
            // Step 1: Determine whether the current sensor is active by 'day'.
            // A sensor is active if its activation day is <= the candidate day.
            if (activationDay[i] <= day)
            {
                // This sensor extends the current active run.
                currentActiveRunLength++;
            }
            else
            {
                // We hit an inactive sensor, so the current active run ends here.
                // Now we compute how many non-overlapping valid corridors we can extract
                // from the run that just ended.

                if (currentActiveRunLength >= minLen)
                {
                    // Why divide by minLen?
                    // - Every corridor must have length at least minLen.
                    // - Therefore, from a run of length L, we can never get more than floor(L / minLen) corridors.
                    // - We can always achieve exactly that many by taking corridors of length exactly minLen.
                    //   This is valid because minLen is inside the allowed range [minLen, maxLen].
                    corridorsFormed += currentActiveRunLength / minLen;

                    // Early exit optimization:
                    // As soon as we already have enough corridors, we can stop scanning.
                    if (corridorsFormed >= k)
                    {
                        return true;
                    }
                }

                // Reset because the inactive sensor breaks contiguity.
                currentActiveRunLength = 0;
            }
        }

        // After the loop, there may still be one final active run that reaches the end of the array.
        if (currentActiveRunLength >= minLen)
        {
            corridorsFormed += currentActiveRunLength / minLen;
        }

        return corridorsFormed >= k;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] activationDay1 = { 4, 2, 5, 3, 3, 6, 1 };
int k1 = 2;
int minLen1 = 2;
int maxLen1 = 3;
int result1 = solution.EarliestDayToActivateKCorridors(activationDay1, k1, minLen1, maxLen1);
Console.WriteLine(result1); // Expected: 4

// Example 2 (corrected)
int[] activationDay2 = { 7, 7, 7 };
int k2 = 2;
int minLen2 = 2;
int maxLen2 = 2;
int result2 = solution.EarliestDayToActivateKCorridors(activationDay2, k2, minLen2, maxLen2);
Console.WriteLine(result2); // Expected: -1

// Additional sanity check based on the description's earlier mistaken example
int[] activationDay3 = { 7, 7, 7, 7, 7 };
int k3 = 2;
int minLen3 = 2;
int maxLen3 = 2;
int result3 = solution.EarliestDayToActivateKCorridors(activationDay3, k3, minLen3, maxLen3);
Console.WriteLine(result3); // Expected: 7