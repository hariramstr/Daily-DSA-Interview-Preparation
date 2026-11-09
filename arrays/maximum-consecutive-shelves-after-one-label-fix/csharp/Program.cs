/*
Title: Maximum Consecutive Shelves After One Label Fix
Difficulty: Medium
Topic: Arrays

Problem Description:
A warehouse stores boxes on shelves labeled with positive integers. Ideally, the labels should form a consecutive run such as [7, 8, 9, 10]. However, one shelf label may have been printed incorrectly.

You are given an integer array labels, where labels[i] is the label currently printed on the i-th shelf in order from left to right. You may change the value of at most one element in the array to any positive integer you want.

Return the maximum possible length of a contiguous subarray that can be made strictly consecutive with step 1 after performing at most one change. A subarray is valid if, after the optional change, its values can become [x, x+1, x+2, ..., x+len-1] for some positive integer x, while keeping the same order of positions.

In other words, within the chosen contiguous segment, every adjacent difference must be exactly 1 after changing at most one element in that segment.

Your task is to compute the longest such segment.

Constraints:
- 1 <= labels.length <= 200000
- 1 <= labels[i] <= 1000000000
- You may modify at most one element in the chosen subarray
- The replacement value must be a positive integer

Example 1:
Input: labels = [4, 5, 9, 7, 8]
Output: 5
Explanation: Change the third value 9 to 6. The entire array becomes [4, 5, 6, 7, 8], which is consecutive.

Example 2:
Input: labels = [10, 11, 13, 14, 20]
Output: 4
Explanation: The subarray [10, 11, 13, 14] can be fixed by changing 13 to 12, giving [10, 11, 12, 13]. No length-5 segment can be made consecutive with only one change.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n), where n is the length of the array.
    We build a few helper arrays in linear time and then test each possible "changed index"
    in O(1), so the total work is linear.

    Space Complexity:
    O(n), for the helper arrays that store lengths of already-consecutive runs and
    the lengths of segments that can be fixed by changing the left or right endpoint.

    Core idea:
    A subarray can become perfectly consecutive after changing at most one value if and only if
    one of these is true:

    1) It is already consecutive. Then no change is needed.
    2) There exists exactly one position inside the subarray whose value we can replace so that
       all adjacent differences become 1.

    Instead of checking every subarray, we precompute:
    - leftRun[i]: length of the longest consecutive-by-+1 suffix ending at i
    - rightRun[i]: length of the longest consecutive-by-+1 prefix starting at i

    Then for each index i, we imagine "changing labels[i]".
    The valid segment around i is built from:
    - a consecutive run immediately to the left of i
    - a consecutive run immediately to the right of i
    and we check whether one replacement value can connect them.

    There are three possibilities for a segment centered at i:
    - use only left side + i
    - use i + only right side
    - connect both sides through i

    The positivity requirement for the replacement value matters only when i is the first
    element of the chosen segment, because then the chosen value becomes the starting value x.
    We explicitly enforce x >= 1 in those cases.
    */
    public int MaximumConsecutiveShelvesAfterOneLabelFix(int[] labels)
    {
        int n = labels.Length;
        if (n == 0) return 0;
        if (n == 1) return 1;

        // leftRun[i]:
        // length of the longest contiguous segment ending at i
        // whose adjacent differences are already exactly +1.
        //
        // Example:
        // labels = [4, 5, 6, 10, 11]
        // leftRun = [1, 2, 3, 1, 2]
        //
        // Why we need this:
        // If we decide to change labels[i], then the segment immediately to the left of i
        // must already be consecutive, otherwise one change at i cannot repair multiple
        // broken places on the left.
        int[] leftRun = new int[n];
        leftRun[0] = 1;
        for (int i = 1; i < n; i++)
        {
            if ((long)labels[i] - labels[i - 1] == 1)
            {
                leftRun[i] = leftRun[i - 1] + 1;
            }
            else
            {
                leftRun[i] = 1;
            }
        }

        // rightRun[i]:
        // length of the longest contiguous segment starting at i
        // whose adjacent differences are already exactly +1.
        //
        // Example:
        // labels = [4, 5, 6, 10, 11]
        // rightRun = [3, 2, 1, 2, 1]
        //
        // Why we need this:
        // Symmetric to leftRun. If we change labels[i], then the segment immediately to the
        // right of i must already be consecutive.
        int[] rightRun = new int[n];
        rightRun[n - 1] = 1;
        for (int i = n - 2; i >= 0; i--)
        {
            if ((long)labels[i + 1] - labels[i] == 1)
            {
                rightRun[i] = rightRun[i + 1] + 1;
            }
            else
            {
                rightRun[i] = 1;
            }
        }

        // Start with the best segment that is already consecutive and needs no change.
        int answer = 1;
        for (int i = 0; i < n; i++)
        {
            if (leftRun[i] > answer)
            {
                answer = leftRun[i];
            }
        }

        // Now try every index as the one element we may change.
        for (int i = 0; i < n; i++)
        {
            // ------------------------------------------------------------
            // Case A: Build a segment that ends at i, using the consecutive
            // run on the left plus the changed value at i.
            //
            // If the left side has length L, then the value at i must become:
            // labels[i - 1] + 1
            //
            // This is always positive because labels are positive.
            // So any left consecutive run can be extended by 1 through changing i.
            // ------------------------------------------------------------
            int leftOnly = 1;
            if (i > 0)
            {
                leftOnly = leftRun[i - 1] + 1;
                if (leftOnly > answer) answer = leftOnly;
            }
            else
            {
                if (1 > answer) answer = 1;
            }

            // ------------------------------------------------------------
            // Case B: Build a segment that starts at i, using the changed
            // value at i plus the consecutive run on the right.
            //
            // If the right side starts at i+1 and has length R, then the value
            // at i must become labels[i + 1] - 1.
            //
            // This is valid only if that replacement value is positive.
            // For example, if labels[i+1] == 1, then labels[i] would need to become 0,
            // which is not allowed.
            // ------------------------------------------------------------
            int rightOnly = 1;
            if (i < n - 1)
            {
                long needed = (long)labels[i + 1] - 1;
                if (needed >= 1)
                {
                    rightOnly = rightRun[i + 1] + 1;
                    if (rightOnly > answer) answer = rightOnly;
                }
            }

            // ------------------------------------------------------------
            // Case C: Connect both sides through the changed index i.
            //
            // Let:
            // - left side end at i-1 and already be consecutive
            // - right side start at i+1 and already be consecutive
            //
            // To connect everything with one changed value at i, that value must satisfy:
            // changedValue = labels[i - 1] + 1
            // changedValue = labels[i + 1] - 1
            //
            // Therefore we need:
            // labels[i + 1] - labels[i - 1] == 2
            //
            // If that holds, then one value can bridge both sides perfectly.
            //
            // The total length becomes:
            // leftRun[i - 1] + 1 + rightRun[i + 1]
            // ------------------------------------------------------------
            if (i > 0 && i < n - 1)
            {
                if ((long)labels[i + 1] - labels[i - 1] == 2)
                {
                    int both = leftRun[i - 1] + 1 + rightRun[i + 1];
                    if (both > answer) answer = both;
                }
            }
        }

        return answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

int[] labels1 = { 4, 5, 9, 7, 8 };
int result1 = solution.MaximumConsecutiveShelvesAfterOneLabelFix(labels1);
Console.WriteLine(result1); // Expected: 5

int[] labels2 = { 10, 11, 13, 14, 20 };
int result2 = solution.MaximumConsecutiveShelvesAfterOneLabelFix(labels2);
Console.WriteLine(result2); // Expected: 4

int[] labels3 = { 1 };
int result3 = solution.MaximumConsecutiveShelvesAfterOneLabelFix(labels3);
Console.WriteLine(result3); // Expected: 1

int[] labels4 = { 5, 6, 7, 8 };
int result4 = solution.MaximumConsecutiveShelvesAfterOneLabelFix(labels4);
Console.WriteLine(result4); // Expected: 4

int[] labels5 = { 2, 100, 4 };
int result5 = solution.MaximumConsecutiveShelvesAfterOneLabelFix(labels5);
Console.WriteLine(result5); // Expected: 3 (change 100 to 3)

int[] labels6 = { 2, 3, 10, 11, 12 };
int result6 = solution.MaximumConsecutiveShelvesAfterOneLabelFix(labels6);
Console.WriteLine(result6); // Expected: 3