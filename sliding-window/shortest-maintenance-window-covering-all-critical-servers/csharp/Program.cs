/*
Title: Shortest Maintenance Window Covering All Critical Servers
Difficulty: Hard
Topic: Sliding Window

Problem Description:
A data center records a time-ordered stream of server IDs representing which server emitted the most recent heartbeat at each second.
You are given an integer array events, where events[i] is the server ID seen at second i, and an integer array critical
containing distinct server IDs that must all be observed during a maintenance audit.

Your task is to find the length of the shortest contiguous time window in events that contains every server in critical at least once.
However, there is an additional reliability rule: within the chosen window, no non-critical server ID is allowed to appear more than L times.
If no such window exists, return -1.

Formally, find the minimum value of (right - left + 1) such that the subarray events[left...right] satisfies both conditions:
1. Every server ID in critical appears at least once in the window.
2. For every server ID x not in critical, its frequency inside the window is at most L.

This problem is designed for large inputs, so solutions that examine all subarrays will time out.
An efficient sliding window approach with frequency tracking is expected.

Constraints:
- 1 <= events.length <= 200000
- 1 <= critical.length <= min(100000, events.length)
- 1 <= events[i], critical[i] <= 10^9
- All values in critical are distinct
- 0 <= L <= events.length

Example 1:
Input: events = [7,2,9,2,5,7,3,9,5], critical = [2,5,9], L = 1
Output: 3

Example 2:
Input: events = [4,8,1,8,6,4,2,6,1], critical = [1,2,6], L = 0
Output: 3
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n), where n is events.Length.
    Reason:
    - Each element is added to the sliding window once when the right pointer moves.
    - Each element is removed from the sliding window at most once when the left pointer moves.
    - All dictionary / hash set operations are average O(1).

    Space Complexity:
    O(k + m), where:
    - k = number of distinct critical server IDs
    - m = number of distinct non-critical server IDs that appear inside the current / visited windows
    In the worst case this is O(n).
    */
    public int ShortestMaintenanceWindow(int[] events, int[] critical, int L)
    {
        // Step 1:
        // Put all critical server IDs into a HashSet.
        // Why?
        // We need to answer the question "Is this server critical?" very frequently
        // while scanning the array. A HashSet gives average O(1) lookup time.
        var criticalSet = new HashSet<int>(critical);

        // Step 2:
        // We need to know how many distinct critical IDs must be present in a valid window.
        int totalCriticalNeeded = critical.Length;

        // Step 3:
        // This dictionary stores frequencies of critical IDs currently inside the window.
        // Example:
        // if the current window contains critical server 2 twice and critical server 5 once,
        // then criticalFreq[2] = 2 and criticalFreq[5] = 1.
        var criticalFreq = new Dictionary<int, int>();

        // Step 4:
        // This dictionary stores frequencies of non-critical IDs currently inside the window.
        // We track them because each non-critical ID is allowed to appear at most L times.
        var nonCriticalFreq = new Dictionary<int, int>();

        // Step 5:
        // coveredCritical counts how many distinct critical IDs are currently present
        // at least once in the window.
        //
        // Example:
        // critical = [2,5,9]
        // if the window currently contains 2 and 9 but not 5,
        // then coveredCritical = 2.
        int coveredCritical = 0;

        // Step 6:
        // badNonCriticalKinds counts how many distinct non-critical IDs currently violate
        // the reliability rule.
        //
        // A non-critical ID violates the rule if its frequency > L.
        //
        // Why track the number of violating IDs instead of checking all frequencies every time?
        // Because checking all frequencies repeatedly would be too slow.
        // With this counter, we can know in O(1) whether the current window is valid
        // with respect to the non-critical rule:
        // - if badNonCriticalKinds == 0, then no non-critical ID exceeds L.
        // - otherwise, the window is invalid.
        int badNonCriticalKinds = 0;

        // Step 7:
        // Standard sliding window left pointer.
        int left = 0;

        // Step 8:
        // Store the best (smallest) valid window length found so far.
        // We initialize it to int.MaxValue so that any real valid window will be smaller.
        int best = int.MaxValue;

        // Step 9:
        // Expand the window by moving the right pointer from left to right across the array.
        for (int right = 0; right < events.Length; right++)
        {
            int value = events[right];

            // Step 9a:
            // Add events[right] into the current window.
            if (criticalSet.Contains(value))
            {
                // This value is critical, so update critical frequency.
                if (!criticalFreq.TryGetValue(value, out int oldCount))
                {
                    oldCount = 0;
                }

                int newCount = oldCount + 1;
                criticalFreq[value] = newCount;

                // If this critical ID was absent before (count 0) and now becomes present (count 1),
                // then we have covered one more required critical server.
                if (oldCount == 0)
                {
                    coveredCritical++;
                }
            }
            else
            {
                // This value is non-critical, so update non-critical frequency.
                if (!nonCriticalFreq.TryGetValue(value, out int oldCount))
                {
                    oldCount = 0;
                }

                int newCount = oldCount + 1;
                nonCriticalFreq[value] = newCount;

                // If the frequency just crossed from allowed to disallowed,
                // i.e. from L to L+1, then this non-critical ID starts violating the rule.
                if (oldCount == L)
                {
                    badNonCriticalKinds++;
                }
            }

            // Step 10:
            // If the window violates the non-critical rule, we MUST shrink from the left
            // until the violation disappears.
            //
            // Why is this safe and necessary?
            // Because any window with badNonCriticalKinds > 0 is invalid, so it can never be
            // a candidate answer. We should remove elements from the left until the window
            // becomes valid again with respect to condition #2.
            while (badNonCriticalKinds > 0)
            {
                int leftValue = events[left];

                if (criticalSet.Contains(leftValue))
                {
                    // Removing a critical value from the window.
                    int count = criticalFreq[leftValue];
                    int updated = count - 1;

                    if (updated == 0)
                    {
                        // This critical ID is no longer present in the window.
                        criticalFreq.Remove(leftValue);
                        coveredCritical--;
                    }
                    else
                    {
                        criticalFreq[leftValue] = updated;
                    }
                }
                else
                {
                    // Removing a non-critical value from the window.
                    int count = nonCriticalFreq[leftValue];
                    int updated = count - 1;

                    // If the count drops from L+1 to L, then this ID stops violating the rule.
                    if (count == L + 1)
                    {
                        badNonCriticalKinds--;
                    }

                    if (updated == 0)
                    {
                        nonCriticalFreq.Remove(leftValue);
                    }
                    else
                    {
                        nonCriticalFreq[leftValue] = updated;
                    }
                }

                left++;
            }

            // Step 11:
            // At this point, the current window [left..right] satisfies condition #2:
            // no non-critical ID appears more than L times.
            //
            // Now we check condition #1:
            // does the window contain every critical ID at least once?
            //
            // If yes, then the window is fully valid.
            if (coveredCritical == totalCriticalNeeded)
            {
                // Step 12:
                // Since the window is valid, try to shrink it from the left as much as possible
                // while keeping it valid.
                //
                // This is the classic "minimize a valid sliding window" step.
                //
                // Important observation:
                // Once badNonCriticalKinds == 0, removing elements from the left can never create
                // a new non-critical violation. Frequencies only decrease when shrinking.
                // So the only thing that can break validity during shrinking is losing a required
                // critical server.
                while (coveredCritical == totalCriticalNeeded)
                {
                    // Update the best answer before removing anything,
                    // because the current window is valid.
                    int currentLength = right - left + 1;
                    if (currentLength < best)
                    {
                        best = currentLength;
                    }

                    int leftValue = events[left];

                    if (criticalSet.Contains(leftValue))
                    {
                        // If we remove a critical value, we may lose coverage.
                        int count = criticalFreq[leftValue];
                        int updated = count - 1;

                        if (updated == 0)
                        {
                            criticalFreq.Remove(leftValue);
                            coveredCritical--;
                        }
                        else
                        {
                            criticalFreq[leftValue] = updated;
                        }
                    }
                    else
                    {
                        // Removing a non-critical value is always safe with respect to condition #2,
                        // because frequencies only go down.
                        int count = nonCriticalFreq[leftValue];
                        int updated = count - 1;

                        if (updated == 0)
                        {
                            nonCriticalFreq.Remove(leftValue);
                        }
                        else
                        {
                            nonCriticalFreq[leftValue] = updated;
                        }
                    }

                    left++;
                }
            }
        }

        // Step 13:
        // If best was never updated, then no valid window exists.
        return best == int.MaxValue ? -1 : best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] events1 = { 7, 2, 9, 2, 5, 7, 3, 9, 5 };
int[] critical1 = { 2, 5, 9 };
int L1 = 1;
int result1 = solution.ShortestMaintenanceWindow(events1, critical1, L1);
Console.WriteLine(result1); // Expected: 3

// Example 2
int[] events2 = { 4, 8, 1, 8, 6, 4, 2, 6, 1 };
int[] critical2 = { 1, 2, 6 };
int L2 = 0;
int result2 = solution.ShortestMaintenanceWindow(events2, critical2, L2);
Console.WriteLine(result2); // Expected: 3

// Additional quick sanity demo
int[] events3 = { 1, 3, 2, 4, 2, 5, 1 };
int[] critical3 = { 1, 2, 5 };
int L3 = 1;
int result3 = solution.ShortestMaintenanceWindow(events3, critical3, L3);
Console.WriteLine(result3);