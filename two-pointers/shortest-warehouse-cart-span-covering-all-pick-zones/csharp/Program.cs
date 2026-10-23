/*
Title: Shortest Warehouse Cart Span Covering All Pick Zones
Difficulty: Medium
Topic: Two Pointers

Problem Description:
A warehouse management system records the sequence of pick zones visited by a cart during a single route.
Each zone is represented by a string such as "A1", "Cold", or "Fragile".

You are given:
- an array zones, where zones[i] is the zone visited at step i
- another array requiredZones listing distinct zones that must all appear in a valid segment

Your task is to find the length of the shortest contiguous segment of the route that contains every
zone in requiredZones at least once. If no such segment exists, return -1.

The segment must be contiguous, and extra zones not in requiredZones are allowed inside the segment.

Return only the minimum length, not the segment itself.

Constraints:
- 1 <= zones.length <= 200000
- 1 <= requiredZones.length <= 5000
- requiredZones contains distinct strings
- 1 <= zones[i].length, requiredZones[i].length <= 20
- Strings consist of letters, digits, underscores, or hyphens

Example 1:
Input:
zones = ["Inbound","A1","Cold","B2","Fragile","Cold","A1","Packing"]
requiredZones = ["A1","Cold","Fragile"]
Output: 3

Explanation:
The shortest valid segment is ["A1","Cold","Fragile"], which has length 3.

Example 2:
Input:
zones = ["Dock","A1","B2","Dock","Packing"]
requiredZones = ["A1","Cold"]
Output: -1

Explanation:
The zone "Cold" never appears in the route, so no valid contiguous segment exists.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n + m), where:
      n = zones.Length
      m = requiredZones.Length
    Why:
    - We build a set/dictionary for required zones in O(m)
    - We move the right pointer from left to right once across the array: O(n)
    - We move the left pointer from left to right at most once across the array: O(n)
    - Total is linear overall

    Space Complexity:
    - O(m)
    Why:
    - We only store counts for required zones, not for every zone in the route
    */
    public int MinSpanCoveringAllZones(string[] zones, string[] requiredZones)
    {
        // Defensive handling:
        // The problem constraints guarantee valid lengths, but adding this makes the method safer
        // and easier to understand in real-world code.
        if (zones == null || requiredZones == null || zones.Length == 0 || requiredZones.Length == 0)
        {
            return -1;
        }

        // Step 1:
        // Put all required zones into a HashSet for O(1)-average membership checks.
        //
        // Why this is necessary:
        // During sliding window expansion and contraction, we will inspect many zone values.
        // We need to quickly answer:
        // "Is this zone one of the required ones?"
        //
        // A HashSet is ideal for this because it gives fast lookups and stores only distinct values.
        var requiredSet = new HashSet<string>(requiredZones);

        // Step 2:
        // Create a dictionary to count how many times each required zone appears
        // inside the current sliding window [left..right].
        //
        // Why this is necessary:
        // We are not just checking presence globally; we need presence inside the CURRENT window.
        // When the left pointer moves forward, counts can decrease, so we must track exact counts.
        var windowCounts = new Dictionary<string, int>();

        // Step 3:
        // "formed" tells us how many distinct required zones are currently satisfied in the window.
        //
        // A required zone is considered satisfied when its count in the current window is at least 1.
        //
        // Since requiredZones contains distinct strings, the total number we need is simply:
        int need = requiredSet.Count;
        int formed = 0;

        // Step 4:
        // Standard sliding window pointers.
        // left = start of current window
        // right = end of current window (expanded in the loop below)
        int left = 0;

        // Step 5:
        // Track the best (smallest) valid window length found so far.
        // We initialize with int.MaxValue so any real valid window will be smaller.
        int bestLength = int.MaxValue;

        // Step 6:
        // Expand the window by moving the right pointer one step at a time.
        //
        // At each step, zones[right] is the new element entering the window.
        for (int right = 0; right < zones.Length; right++)
        {
            string currentZone = zones[right];

            // Step 6a:
            // Only required zones matter for satisfying the condition.
            // Non-required zones can stay in the window, but we do not need to count them.
            if (requiredSet.Contains(currentZone))
            {
                // Increase the count for this required zone in the current window.
                if (!windowCounts.ContainsKey(currentZone))
                {
                    windowCounts[currentZone] = 0;
                }

                windowCounts[currentZone]++;

                // If the count became exactly 1, that means this required zone
                // has just become satisfied in the current window.
                //
                // Example:
                // If "Cold" count changes from 0 to 1, we now cover "Cold".
                // If it changes from 1 to 2, formed should NOT increase again,
                // because "Cold" was already satisfied.
                if (windowCounts[currentZone] == 1)
                {
                    formed++;
                }
            }

            // Step 7:
            // If formed == need, then the current window contains ALL required zones.
            //
            // Now we try to shrink the window from the left to make it as short as possible
            // while still remaining valid.
            //
            // This is the heart of the sliding-window optimization:
            // - Expand right until valid
            // - Shrink left while still valid
            while (formed == need)
            {
                // The current window [left..right] is valid.
                // Compute its length and update the best answer if this one is smaller.
                int currentLength = right - left + 1;
                if (currentLength < bestLength)
                {
                    bestLength = currentLength;
                }

                // We are about to move left forward, so zones[left] will leave the window.
                string leftZone = zones[left];

                // If the leaving zone is required, we must update its count.
                if (requiredSet.Contains(leftZone))
                {
                    windowCounts[leftZone]--;

                    // If the count drops to 0, then this required zone is no longer covered
                    // by the window after moving left.
                    //
                    // That means the window becomes invalid, so formed must decrease.
                    if (windowCounts[leftZone] == 0)
                    {
                        formed--;
                    }
                }

                // Actually shrink the window by moving left one step to the right.
                left++;
            }
        }

        // Step 8:
        // If bestLength was never updated, then no valid window was found.
        // That means at least one required zone never appeared in any contiguous segment
        // covering all required zones, which in practice means it never appeared at all.
        if (bestLength == int.MaxValue)
        {
            return -1;
        }

        return bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] zones1 = { "Inbound", "A1", "Cold", "B2", "Fragile", "Cold", "A1", "Packing" };
string[] required1 = { "A1", "Cold", "Fragile" };
int result1 = solution.MinSpanCoveringAllZones(zones1, required1);
Console.WriteLine(result1); // Expected: 3

// Example 2
string[] zones2 = { "Dock", "A1", "B2", "Dock", "Packing" };
string[] required2 = { "A1", "Cold" };
int result2 = solution.MinSpanCoveringAllZones(zones2, required2);
Console.WriteLine(result2); // Expected: -1

// Additional quick sanity check
string[] zones3 = { "A1", "X", "Cold", "Y", "Fragile", "A1" };
string[] required3 = { "A1", "Cold", "Fragile" };
int result3 = solution.MinSpanCoveringAllZones(zones3, required3);
Console.WriteLine(result3); // Expected: 5