/*
Title: Find First Day With At Least Target Signups

Problem Description:
You are given a non-decreasing integer array `signups`, where `signups[i]` represents
the total number of users who have signed up for a product by the end of day `i`.
Because this is a cumulative total, the array is sorted in non-decreasing order.

You are also given an integer `target` representing a milestone number of total signups.

Your task is to return the earliest day index at which the total number of signups is
greater than or equal to `target`. If the milestone is never reached, return `-1`.

Return the smallest index `i` such that `signups[i] >= target`.

Example 1:
Input: signups = [3, 5, 5, 9, 14], target = 6
Output: 3
Explanation: The first day where total signups are at least 6 is day index 3, where the value is 9.

Example 2:
Input: signups = [1, 2, 4, 4, 7], target = 4
Output: 2
Explanation: Although 4 appears more than once, the earliest day index with at least 4 signups is 2.

Goal:
Solve this in O(log n) time using binary search.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(log n)
    Space Complexity: O(1)

    We use binary search because the input array is already sorted in non-decreasing order.
    That sorted property allows us to eliminate half of the remaining search space on each step.

    We are not just looking for any index where signups[i] >= target.
    We specifically need the FIRST such index.
    That means when we find a valid value, we do not stop immediately.
    Instead, we record it as a possible answer and continue searching on the left side
    to see if an earlier valid day exists.
    */
    public int FindFirstDayAtLeastTarget(int[] signups, int target)
    {
        // "left" points to the beginning of the current search range.
        int left = 0;

        // "right" points to the end of the current search range.
        int right = signups.Length - 1;

        // We store the best answer found so far here.
        // Start with -1, which means "no valid day found yet".
        int answer = -1;

        // Continue searching while the current range is valid.
        // A valid range means left has not crossed right.
        while (left <= right)
        {
            // Compute the middle index safely.
            // We use this form instead of (left + right) / 2 to avoid integer overflow
            // in general binary search patterns, even though the constraints here are safe.
            int mid = left + (right - left) / 2;

            // If the middle value is greater than or equal to the target,
            // then mid is a valid candidate answer.
            if (signups[mid] >= target)
            {
                // Record this index as a possible answer.
                // We do this because it satisfies the condition signups[mid] >= target.
                answer = mid;

                // But we are not done yet.
                // We need the EARLIEST such index, not just any valid one.
                // So we continue searching in the LEFT half to see whether there is
                // another valid index earlier than mid.
                right = mid - 1;
            }
            else
            {
                // If signups[mid] < target, then mid is too small.
                // Because the array is sorted, every index to the LEFT of mid
                // must also be less than or equal to signups[mid], and therefore
                // also less than target.
                //
                // That means none of those positions can be the answer.
                // So we safely discard the entire left half including mid,
                // and continue searching only in the RIGHT half.
                left = mid + 1;
            }
        }

        // If we found at least one valid index, "answer" holds the earliest one.
        // Otherwise it remains -1, meaning the target was never reached.
        return answer;
    }
}

// Demo code:
// Create a solution object, run the sample test cases, and print the results.

var solution = new Solution();

// Example 1:
// signups = [3, 5, 5, 9, 14], target = 6
// Expected output: 3
int[] signups1 = { 3, 5, 5, 9, 14 };
int target1 = 6;
int result1 = solution.FindFirstDayAtLeastTarget(signups1, target1);
Console.WriteLine(result1);

// Example 2:
// signups = [1, 2, 4, 4, 7], target = 4
// Expected output: 2
int[] signups2 = { 1, 2, 4, 4, 7 };
int target2 = 4;
int result2 = solution.FindFirstDayAtLeastTarget(signups2, target2);
Console.WriteLine(result2);

// Additional demo:
// Target is never reached, so expected output: -1
int[] signups3 = { 1, 2, 3, 3, 3 };
int target3 = 5;
int result3 = solution.FindFirstDayAtLeastTarget(signups3, target3);
Console.WriteLine(result3);

// Additional demo:
// Target is 0, and since all values are >= 0 in a non-decreasing cumulative array,
// the earliest valid day should be index 0 if the array is non-empty.
int[] signups4 = { 0, 0, 2, 5 };
int target4 = 0;
int result4 = solution.FindFirstDayAtLeastTarget(signups4, target4);
Console.WriteLine(result4);