/*
Title: Longest Upgrade Window With One Rollback

Problem Description:
A deployment team records the result of each software upgrade attempt in chronological order using an integer array status,
where 1 means the upgrade at that minute succeeded and 0 means it failed.

The team is allowed to perform at most one rollback operation on a single failed attempt, turning one 0 into a 1.

Task:
Find the length of the longest contiguous time window that can consist entirely of successful upgrades
after applying at most one rollback.

Return the maximum possible length of such a contiguous window.

Key idea:
We need the longest contiguous subarray that contains at most one 0,
because that one 0 can be "rolled back" (flipped) into a 1.

Examples:
1) status = [1,1,0,1,1,1,0,1]
   Output = 6
   Explanation:
   Flip the 0 at index 2, and the window [1,1,0,1,1,1] becomes six 1s.

2) status = [0,1,1,0,1,0,1,1]
   Output = 4
   Explanation:
   Flip the 0 at index 3, and the window [1,1,0,1] becomes four 1s.

Constraints:
- 1 <= status.length <= 100000
- status[i] is either 0 or 1
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is processed by the right pointer once.
    - The left pointer also only moves forward, at most n times total.
    - Therefore, the total work is linear.

    Space Complexity: O(1)
    - We only use a few integer variables.
    - No extra arrays, lists, or hash-based structures are needed.

    Beginner-friendly explanation:
    We use a sliding window.
    The window is the current contiguous segment from left to right.
    We allow the window to contain at most one 0, because we can flip only one failed attempt.
    If the window ever contains more than one 0, it is invalid, so we move the left side forward
    until the window becomes valid again.
    While doing this, we keep track of the largest valid window length seen so far.
    */
    public int LongestUpgradeWindowWithOneRollback(int[] status)
    {
        // "left" marks the beginning of the current sliding window.
        // The window will always be the range [left, right].
        int left = 0;

        // This counts how many failed attempts (0s) are currently inside the window.
        // We are allowed at most one 0, because we can roll back only one failed attempt.
        int zeroCount = 0;

        // This stores the best answer found so far:
        // the maximum length of any valid window encountered.
        int maxLength = 0;

        // Expand the window one element at a time by moving "right" from left to right.
        for (int right = 0; right < status.Length; right++)
        {
            // Step 1: Include status[right] in the current window.
            // If it is a failed attempt (0), we increase zeroCount.
            // Why?
            // Because the validity of the window depends on how many 0s it contains.
            if (status[right] == 0)
            {
                zeroCount++;
            }

            // Step 2: If the window has more than one 0, it is invalid.
            // Why invalid?
            // Because we are allowed to roll back at most one failed attempt.
            // A window with 2 or more zeros cannot be turned into all 1s with only one rollback.
            //
            // So we must shrink the window from the left until it becomes valid again.
            while (zeroCount > 1)
            {
                // Before moving left forward, check whether the element leaving the window is 0.
                // If it is, then one failed attempt is no longer inside the window,
                // so we decrease zeroCount.
                if (status[left] == 0)
                {
                    zeroCount--;
                }

                // Move the left boundary one step to the right.
                // This shrinks the window.
                left++;
            }

            // Step 3: At this point, the window [left, right] is valid.
            // It contains at most one 0, so it can be made entirely successful
            // by rolling back that one 0 (or doing nothing if there are no 0s).
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is longer than any previous one.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After scanning the whole array, maxLength is the answer.
        return maxLength;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] status1 = [1, 1, 0, 1, 1, 1, 0, 1];
int result1 = solution.LongestUpgradeWindowWithOneRollback(status1);
Console.WriteLine("Example 1:");
Console.WriteLine($"Input: [{string.Join(",", status1)}]");
Console.WriteLine($"Output: {result1}");
Console.WriteLine("Expected: 6");
Console.WriteLine();

// Example 2
int[] status2 = [0, 1, 1, 0, 1, 0, 1, 1];
int result2 = solution.LongestUpgradeWindowWithOneRollback(status2);
Console.WriteLine("Example 2:");
Console.WriteLine($"Input: [{string.Join(",", status2)}]");
Console.WriteLine($"Output: {result2}");
Console.WriteLine("Expected: 4");
Console.WriteLine();

// Additional demo: all successes already
int[] status3 = [1, 1, 1, 1];
int result3 = solution.LongestUpgradeWindowWithOneRollback(status3);
Console.WriteLine("Additional Example 3:");
Console.WriteLine($"Input: [{string.Join(",", status3)}]");
Console.WriteLine($"Output: {result3}");
Console.WriteLine("Expected: 4");
Console.WriteLine();

// Additional demo: single failure
int[] status4 = [0];
int result4 = solution.LongestUpgradeWindowWithOneRollback(status4);
Console.WriteLine("Additional Example 4:");
Console.WriteLine($"Input: [{string.Join(",", status4)}]");
Console.WriteLine($"Output: {result4}");
Console.WriteLine("Expected: 1");
Console.WriteLine();

// Additional demo: multiple failures
int[] status5 = [0, 0, 0, 1, 1];
int result5 = solution.LongestUpgradeWindowWithOneRollback(status5);
Console.WriteLine("Additional Example 5:");
Console.WriteLine($"Input: [{string.Join(",", status5)}]");
Console.WriteLine($"Output: {result5}");
Console.WriteLine("Expected: 3");