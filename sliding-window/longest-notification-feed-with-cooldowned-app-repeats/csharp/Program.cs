/*
Title: Longest Notification Feed With Cooldowned App Repeats
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are given an array apps of length n, where apps[i] is the app ID that generated the i-th notification
in a user's chronological feed, and an integer cooldown. A contiguous segment of the feed is called valid
if, for every app ID, any two occurrences of that same app inside the segment are more than cooldown
positions apart. In other words, if apps[i] == apps[j] and both indices belong to the chosen segment,
then |i - j| must be greater than cooldown.

Your task is to return the length of the longest valid contiguous segment.

This models a notification system where repeated alerts from the same app must be sufficiently spaced apart
to avoid overwhelming the user. The segment must remain contiguous; you are not allowed to reorder or
delete notifications.

A segment of length 0 is allowed only implicitly, but the answer will always be at least 1 when n > 0.

Constraints:
- 1 <= n <= 200000
- 1 <= apps[i] <= 1000000000
- 0 <= cooldown <= n

Example 1:
Input: apps = [4, 1, 2, 4, 3, 1, 5], cooldown = 2
Output: 5
Explanation: The segment [1, 2, 4, 3, 1] is valid. The two 1s are 4 positions apart, which is greater than 2.
No app repeats within distance 2 in this segment. No longer valid segment exists.

Example 2:
Input: apps = [7, 7, 8, 9, 7, 8, 10], cooldown = 3
Output: 4
Explanation:
A valid optimal contiguous segment is [9, 7, 8, 10], which has length 4.
Any segment containing two equal app IDs whose positions differ by 3 or less is invalid.

Key Observation:
A window is valid if, for every app ID, the distance between consecutive occurrences inside the window
is greater than cooldown. That is enough, because if every consecutive pair of equal values is spaced
more than cooldown apart, then any farther pair is also spaced more than cooldown apart.

This leads to a classic sliding window approach:
- Expand the right end one notification at a time.
- Track the most recent index where each app ID appeared.
- If the current app was seen too recently (distance <= cooldown), move the left end just past that
  previous occurrence.
- Keep the maximum window length seen.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan the array once from left to right.
    - Each dictionary lookup/update is O(1) on average.

    Space Complexity: O(k)
    - k is the number of distinct app IDs stored in the dictionary.
    - In the worst case, k can be O(n).
    */
    public int LongestValidSegment(int[] apps, int cooldown)
    {
        // If the array is empty, the longest valid segment is 0.
        // The problem guarantees n >= 1, but handling this makes the method robust.
        if (apps == null || apps.Length == 0)
        {
            return 0;
        }

        // Fast path:
        // If cooldown is 0, then repeated equal values are allowed as long as their distance is > 0.
        // Since any two different positions have distance at least 1, every contiguous segment is valid.
        // Therefore the whole array is the answer.
        if (cooldown == 0)
        {
            return apps.Length;
        }

        // This dictionary stores:
        // app ID -> most recent index where this app appeared
        //
        // Why only the most recent index?
        // Because when we process apps[right], the only occurrence that can create the tightest violation
        // is the nearest previous same app. If that nearest previous one is already more than cooldown away,
        // then all earlier ones are even farther away and therefore also safe.
        var lastSeenIndex = new Dictionary<int, int>();

        // left marks the start of the current sliding window.
        // The current window is always [left .. right].
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // Expand the window by moving right from left to right across the array.
        for (int right = 0; right < apps.Length; right++)
        {
            // Current app ID entering the window.
            int currentApp = apps[right];

            // Step 1:
            // Check whether this app has appeared before.
            if (lastSeenIndex.TryGetValue(currentApp, out int previousIndex))
            {
                // Step 2:
                // Determine whether the previous occurrence is too close to the current one.
                //
                // The segment is valid only if equal app IDs are MORE than cooldown positions apart.
                // So if distance <= cooldown, we have a violation.
                //
                // Distance between previousIndex and right is:
                // right - previousIndex
                if (right - previousIndex <= cooldown)
                {
                    // Step 3:
                    // To fix the violation, the window must exclude the previous occurrence.
                    // The smallest valid new left boundary is previousIndex + 1.
                    //
                    // However, left should never move backward.
                    // So we take the maximum of the current left and previousIndex + 1.
                    left = Math.Max(left, previousIndex + 1);
                }
            }

            // Step 4:
            // Update the most recent index of the current app to the current position.
            // This is necessary because future occurrences should compare against the nearest previous one.
            lastSeenIndex[currentApp] = right;

            // Step 5:
            // Now [left .. right] is guaranteed to be valid.
            // Compute its length and update the best answer if needed.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        return best;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] apps1 = { 4, 1, 2, 4, 3, 1, 5 };
int cooldown1 = 2;
int result1 = solution.LongestValidSegment(apps1, cooldown1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 5

// Example 2
int[] apps2 = { 7, 7, 8, 9, 7, 8, 10 };
int cooldown2 = 3;
int result2 = solution.LongestValidSegment(apps2, cooldown2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 4

// Additional quick checks

// cooldown = 0 => entire array is valid
int[] apps3 = { 1, 1, 1, 1 };
int cooldown3 = 0;
int result3 = solution.LongestValidSegment(apps3, cooldown3);
Console.WriteLine("Additional Check 1 Result: " + result3); // Expected: 4

// all unique => entire array is valid
int[] apps4 = { 5, 6, 7, 8, 9 };
int cooldown4 = 10;
int result4 = solution.LongestValidSegment(apps4, cooldown4);
Console.WriteLine("Additional Check 2 Result: " + result4); // Expected: 5

// repeated values with tight cooldown restriction
int[] apps5 = { 1, 2, 1, 2, 1, 2 };
int cooldown5 = 2;
int result5 = solution.LongestValidSegment(apps5, cooldown5);
Console.WriteLine("Additional Check 3 Result: " + result5); // Expected: 2 or 3? Let's verify: [1,2,1] has equal 1s distance 2, invalid. So max is 2.