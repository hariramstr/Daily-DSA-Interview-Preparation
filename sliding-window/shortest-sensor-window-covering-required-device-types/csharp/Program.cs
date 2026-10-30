/*
Title: Shortest Sensor Window Covering Required Device Types
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are given a stream of sensor events collected in chronological order. Each event belongs to a device type represented by a string in the array events, where events[i] is the type of the i-th event. You are also given a map required where required[t] is the minimum number of events of device type t that must appear inside a valid contiguous window.

Your task is to return the length of the shortest contiguous subarray of events that satisfies all required device-type quotas. A window [l, r] is valid if for every device type t in required, the number of occurrences of t inside events[l..r] is at least required[t]. If no such window exists, return -1.

This problem is harder than a standard minimum-cover window because the stream can be very large, device type names are arbitrary strings, and multiple quotas may need to be met simultaneously. An efficient solution is expected.

Constraints:
- 1 <= events.length <= 200000
- 1 <= events[i].length <= 20
- events[i] consists of lowercase English letters, digits, or underscores
- 1 <= required.size() <= 50000
- 1 <= required[t] <= 100000
- The sum of all required counts may be greater than events.length
- Only device types present in required matter for validity; other event types may appear anywhere in the window
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n + m), where:
      n = number of events
      m = number of required device types
    Explanation:
    - We scan the events array once with the right pointer.
    - We move the left pointer forward at most once per event overall.
    - Dictionary operations are O(1) average case.
    - Therefore the total work is linear.

    Space Complexity:
    - O(m)
    Explanation:
    - We store counts only for device types that appear in the required map.
    - That requires space proportional to the number of required device types.
    */
    public int MinWindowLength(string[] events, Dictionary<string, int> required)
    {
        // If there are no events, no valid window can exist.
        if (events == null || events.Length == 0)
        {
            return -1;
        }

        // If required is null or empty, then technically an empty set of requirements
        // would be satisfied by a window of length 0.
        // The problem constraints say required.size() >= 1, but we still guard for safety.
        if (required == null || required.Count == 0)
        {
            return 0;
        }

        // This dictionary stores how many times each required device type appears
        // inside the CURRENT sliding window.
        //
        // Important design choice:
        // We only care about device types that are present in "required".
        // Any other event type does not affect validity, so we do not need to track it.
        var windowCounts = new Dictionary<string, int>(required.Count);

        // "needToSatisfy" is the number of distinct required device types.
        // Example: if required = { cam:2, door:1 }, then needToSatisfy = 2
        int needToSatisfy = required.Count;

        // "satisfiedTypes" counts how many distinct required device types currently
        // meet or exceed their required quota inside the window.
        //
        // Example:
        // required = { cam:2, door:1 }
        // if window has cam=2 and door=0, then satisfiedTypes = 1
        // if window has cam=2 and door=1, then satisfiedTypes = 2 => valid window
        int satisfiedTypes = 0;

        // Standard sliding window left boundary.
        int left = 0;

        // Track the best (smallest) valid window length found so far.
        // We initialize to int.MaxValue so that any real valid window will be smaller.
        int bestLength = int.MaxValue;

        // Expand the window by moving "right" from left to right across the array.
        for (int right = 0; right < events.Length; right++)
        {
            string currentEvent = events[right];

            // Step 1: Include events[right] into the window.
            //
            // Why?
            // The sliding window grows by moving the right pointer one step at a time.
            // Every time we add a new event, the window may become valid for the first time,
            // or become "more valid" by increasing counts.
            if (required.TryGetValue(currentEvent, out int requiredCountForCurrent))
            {
                // If this event type matters, update its count in the current window.
                int newCount = windowCounts.GetValueOrDefault(currentEvent, 0) + 1;
                windowCounts[currentEvent] = newCount;

                // Step 2: Check whether this exact addition caused the device type
                // to become satisfied.
                //
                // Why compare equality instead of >= ?
                // Because we only want to increment "satisfiedTypes" ONCE per type,
                // at the moment it first reaches the required quota.
                //
                // Example:
                // required["cam"] = 2
                // counts go 0 -> 1 (not satisfied yet)
                // counts go 1 -> 2 (now satisfiedTypes++)
                // counts go 2 -> 3 (still satisfied, but do NOT increment again)
                if (newCount == requiredCountForCurrent)
                {
                    satisfiedTypes++;
                }
            }

            // Step 3: If all required device types are satisfied,
            // try to shrink the window from the left as much as possible.
            //
            // Why?
            // Once a window is valid, any larger version of it is not better than
            // the smallest valid version ending at the same "right".
            // So we greedily remove unnecessary elements from the left.
            while (satisfiedTypes == needToSatisfy)
            {
                // The current window [left, right] is valid.
                // Update the best answer.
                int currentLength = right - left + 1;
                if (currentLength < bestLength)
                {
                    bestLength = currentLength;
                }

                string leftEvent = events[left];

                // Step 4: Remove events[left] from the window and move left forward.
                //
                // This is the "shrink" phase of the sliding window.
                // We want to see whether the window can remain valid even after
                // discarding the leftmost event.
                if (required.TryGetValue(leftEvent, out int requiredCountForLeft))
                {
                    int updatedCount = windowCounts[leftEvent] - 1;
                    windowCounts[leftEvent] = updatedCount;

                    // If the count drops BELOW the required quota,
                    // then this device type is no longer satisfied.
                    //
                    // Why compare updatedCount < requiredCountForLeft ?
                    // Because the window was valid before removal.
                    // If removing this event makes the count too small,
                    // the window stops being valid and we must stop shrinking.
                    if (updatedCount < requiredCountForLeft)
                    {
                        satisfiedTypes--;
                    }
                }

                left++;
            }
        }

        // If bestLength was never updated, then no valid window exists.
        return bestLength == int.MaxValue ? -1 : bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
var events1 = new[] { "cam", "temp", "cam", "door", "temp", "cam", "door" };
var required1 = new Dictionary<string, int>
{
    ["cam"] = 2,
    ["door"] = 1
};
var result1 = solution.MinWindowLength(events1, required1);
Console.WriteLine(result1); // Expected: 4

// Example 2
var events2 = new[] { "a", "b", "c", "a", "b", "a", "d" };
var required2 = new Dictionary<string, int>
{
    ["a"] = 2,
    ["b"] = 2,
    ["d"] = 1
};
var result2 = solution.MinWindowLength(events2, required2);
Console.WriteLine(result2); // Expected: 6

// Additional demo: impossible case
var events3 = new[] { "cam", "temp", "door" };
var required3 = new Dictionary<string, int>
{
    ["cam"] = 2,
    ["door"] = 1
};
var result3 = solution.MinWindowLength(events3, required3);
Console.WriteLine(result3); // Expected: -1