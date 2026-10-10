/*
Title: Longest Alarm Timeline With Limited Snooze Resets
Difficulty: Hard
Topic: Sliding Window

Problem Description:
A productivity app records a user's wake-up behavior over several days. For each day, the app stores an integer in an array alarms, where alarms[i] is the alarm label used on day i. Equal values mean the same exact alarm sound was used again.

The app considers a contiguous block of days to be a valid timeline if no alarm label appears more than limit times inside that block. However, the app is allowed to apply up to k snooze resets inside the chosen block. A snooze reset can be assigned to any single day in the block and makes that day exempt from the frequency rule, meaning its alarm label does not count toward the limit for that label. Each day can use at most one reset.

Return the length of the longest contiguous subarray that can be made valid using at most k snooze resets.

In other words, for a chosen window, if an alarm label appears f times, then at least max(0, f - limit) of those occurrences must be covered by snooze resets. The total number of required resets across all labels in the window must be at most k.

Design an algorithm efficient enough for large inputs.

Constraints:
- 1 <= alarms.length <= 200000
- 1 <= alarms[i] <= 10^9
- 0 <= k <= alarms.length
- 1 <= limit <= alarms.length

Example 1:
Input: alarms = [5, 1, 5, 2, 5, 1, 1], limit = 2, k = 1
Output: 5
Explanation: The subarray [5, 1, 5, 2, 5] has counts {5: 3, 1: 1, 2: 1}. Since label 5 exceeds the limit by 1, one snooze reset is enough, so this window is valid. No longer valid window exists.

Example 2:
Input: alarms = [4, 4, 4, 3, 3, 4, 3, 3], limit = 1, k = 3
Output: 5
Explanation: Consider [4, 4, 3, 3, 4]. The counts are {4: 3, 3: 2}. To make every label appear at most once, we need (3 - 1) + (2 - 1) = 3 snooze resets, which is allowed. A length-6 window would require at least 4 resets, so the answer is 5.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n), where n is alarms.Length.

    Why O(n)?
    - Each element enters the sliding window once when the right pointer moves forward.
    - Each element leaves the sliding window at most once when the left pointer moves forward.
    - Dictionary operations are O(1) average time.

    Space Complexity:
    O(m), where m is the number of distinct alarm labels currently tracked.
    In the worst case, m can be O(n).

    Core idea:
    For any window, if a label appears f times, then it contributes max(0, f - limit) required resets.
    So the total resets needed for the window is:

        sum over all labels of max(0, frequency[label] - limit)

    We maintain this value incrementally while expanding/shrinking the sliding window.

    Important observation:
    When we add one occurrence of a label:
    - If its old count was already at least limit, then this new occurrence increases required resets by 1.
    - Otherwise, required resets do not change.

    When we remove one occurrence of a label:
    - If its old count was greater than limit, then removing it decreases required resets by 1.
    - Otherwise, required resets do not change.

    This lets us maintain the exact number of resets needed in O(1) per pointer move.
    */
    public int LongestAlarmTimeline(int[] alarms, int limit, int k)
    {
        // This dictionary stores the frequency of each alarm label inside the current window.
        // Key   = alarm label
        // Value = how many times that label appears between left and right inclusive
        var frequency = new Dictionary<int, int>();

        // left is the start index of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // requiredResets stores the exact number of snooze resets needed
        // to make the current window valid.
        //
        // For each label with frequency f:
        // contribution = max(0, f - limit)
        //
        // So the whole window is valid if and only if:
        // requiredResets <= k
        int requiredResets = 0;

        // Move the right boundary of the window from left to right, one step at a time.
        for (int right = 0; right < alarms.Length; right++)
        {
            int value = alarms[right];

            // Read the old count before inserting the new element.
            // If the value is not present yet, its old count is 0.
            frequency.TryGetValue(value, out int oldCount);

            // We are adding alarms[right] into the window, so its frequency increases by 1.
            int newCount = oldCount + 1;
            frequency[value] = newCount;

            // Step explanation:
            // If oldCount was already >= limit, then after adding this new occurrence,
            // the number of "extra" occurrences for this label increases by 1.
            //
            // Example with limit = 2:
            // oldCount = 2 -> contribution was max(0, 2 - 2) = 0
            // newCount = 3 -> contribution becomes max(0, 3 - 2) = 1
            // So requiredResets increases by 1.
            //
            // oldCount = 3 -> contribution was 1
            // newCount = 4 -> contribution becomes 2
            // Again, requiredResets increases by 1.
            //
            // But if oldCount < limit, then we are still not exceeding the allowed count yet,
            // so requiredResets does not change.
            if (oldCount >= limit)
            {
                requiredResets++;
            }

            // Now the window may have become invalid because it might need too many resets.
            // While the current window requires more than k resets, we must shrink it from the left.
            while (requiredResets > k)
            {
                int leftValue = alarms[left];

                // Get the current count of the value that is about to leave the window.
                int countBeforeRemoval = frequency[leftValue];

                // Step explanation:
                // If countBeforeRemoval > limit, then this label currently contributes
                // at least 1 extra occurrence beyond the limit.
                //
                // Removing one occurrence reduces that excess by exactly 1,
                // so requiredResets must decrease by 1.
                //
                // Example with limit = 2:
                // countBeforeRemoval = 4 -> contribution is 2
                // after removal count becomes 3 -> contribution is 1
                // requiredResets decreases by 1
                //
                // If countBeforeRemoval <= limit, then this label was not contributing
                // any excess resets, so removing one occurrence does not change requiredResets.
                if (countBeforeRemoval > limit)
                {
                    requiredResets--;
                }

                int countAfterRemoval = countBeforeRemoval - 1;

                // Update the dictionary after removing the leftmost element.
                if (countAfterRemoval == 0)
                {
                    // If the count becomes zero, we remove the key entirely.
                    // This is not strictly required for correctness, but it keeps the dictionary cleaner.
                    frequency.Remove(leftValue);
                }
                else
                {
                    frequency[leftValue] = countAfterRemoval;
                }

                // Move the left boundary rightward because that element is no longer in the window.
                left++;
            }

            // At this point, the window [left..right] is guaranteed valid:
            // requiredResets <= k
            //
            // So we can safely update the best answer.
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
int[] alarms1 = { 5, 1, 5, 2, 5, 1, 1 };
int limit1 = 2;
int k1 = 1;
int result1 = solution.LongestAlarmTimeline(alarms1, limit1, k1);
Console.WriteLine(result1); // Expected: 5

// Example 2
int[] alarms2 = { 4, 4, 4, 3, 3, 4, 3, 3 };
int limit2 = 1;
int k2 = 3;
int result2 = solution.LongestAlarmTimeline(alarms2, limit2, k2);
Console.WriteLine(result2); // Expected: 5

// Additional quick checks
int[] alarms3 = { 1, 2, 3, 4 };
int limit3 = 1;
int k3 = 0;
int result3 = solution.LongestAlarmTimeline(alarms3, limit3, k3);
Console.WriteLine(result3); // Expected: 4

int[] alarms4 = { 7, 7, 7, 7 };
int limit4 = 2;
int k4 = 1;
int result4 = solution.LongestAlarmTimeline(alarms4, limit4, k4);
Console.WriteLine(result4); // Expected: 3

int[] alarms5 = { 9, 9, 8, 8, 8, 9 };
int limit5 = 1;
int k5 = 2;
int result5 = solution.LongestAlarmTimeline(alarms5, limit5, k5);
Console.WriteLine(result5); // One valid expected answer length: 4