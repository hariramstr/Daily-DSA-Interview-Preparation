/*
Title: Find the First Day a Game Server Reaches Target Capacity
Difficulty: Easy
Topic: Binary Search

Problem Description:
A gaming platform records the total number of active players on a server at the end of each day.
Because the game is steadily growing, the daily totals are sorted in non-decreasing order.

You are given an integer array players, where players[i] is the active player count on day i,
and an integer target representing a required server capacity milestone.

Your task is to return the index of the first day on which the server reaches or exceeds target players.
If the server never reaches target, return -1.

This problem should be solved efficiently using binary search rather than scanning from left to right.
Since the array is already sorted, you can repeatedly discard half of the remaining search space.

Return the smallest index i such that players[i] >= target. If no such index exists, return -1.

Constraints:
- 1 <= players.length <= 100000
- 0 <= players[i] <= 1000000000
- players is sorted in non-decreasing order
- 0 <= target <= 1000000000

Example 1:
Input: players = [120, 150, 150, 210, 260], target = 151
Output: 3
Explanation: Day 3 is the first day with at least 151 active players, since players[3] = 210.

Example 2:
Input: players = [40, 80, 80, 95], target = 100
Output: -1
Explanation: The server never reaches 100 active players, so the answer is -1.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(log n)
    Space Complexity: O(1)

    We use binary search because the array is already sorted in non-decreasing order.
    That means once we find a value that is >= target, every value to its right is also
    a possible answer, but we still need to check whether there is an earlier valid index.

    Goal:
    Find the smallest index i such that players[i] >= target.
    If no such index exists, return -1.
    */
    public int FirstDayAtOrAboveTarget(int[] players, int target)
    {
        // "left" marks the beginning of the current search range.
        int left = 0;

        // "right" marks the end of the current search range.
        int right = players.Length - 1;

        // We store the best answer found so far in "answer".
        // We start with -1 to mean "no valid day found yet".
        int answer = -1;

        // Continue searching while the current range is valid.
        // A valid range means left has not crossed right.
        while (left <= right)
        {
            // Compute the middle index safely.
            // We use this form instead of (left + right) / 2 to avoid overflow in general.
            int mid = left + (right - left) / 2;

            // If players[mid] is large enough, then mid is a valid candidate.
            if (players[mid] >= target)
            {
                // Record mid as a possible answer.
                // We do this because it satisfies the condition players[mid] >= target.
                answer = mid;

                // But we are not done yet.
                // Since we need the FIRST such index, there might be another valid answer
                // somewhere to the LEFT of mid.
                //
                // So we discard the right half including everything after mid,
                // and continue searching only in the left portion.
                right = mid - 1;
            }
            else
            {
                // If players[mid] < target, then mid is definitely NOT a valid answer.
                // Also, because the array is sorted, every index to the LEFT of mid
                // must be <= players[mid], which means all of them are also < target.
                //
                // Therefore, none of those positions can be the answer.
                // We safely discard the left half up to and including mid.
                left = mid + 1;
            }
        }

        // If we found at least one valid index, answer holds the smallest one discovered.
        // If not, answer is still -1.
        return answer;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] players1 = { 120, 150, 150, 210, 260 };
int target1 = 151;
int result1 = solution.FirstDayAtOrAboveTarget(players1, target1);
Console.WriteLine(result1); // Expected: 3

// Example 2
int[] players2 = { 40, 80, 80, 95 };
int target2 = 100;
int result2 = solution.FirstDayAtOrAboveTarget(players2, target2);
Console.WriteLine(result2); // Expected: -1

// Additional demo cases for learning

// Target matches the first element
int[] players3 = { 50, 60, 70, 80 };
int target3 = 50;
int result3 = solution.FirstDayAtOrAboveTarget(players3, target3);
Console.WriteLine(result3); // Expected: 0

// Multiple equal values that meet the target
int[] players4 = { 10, 20, 20, 20, 30 };
int target4 = 20;
int result4 = solution.FirstDayAtOrAboveTarget(players4, target4);
Console.WriteLine(result4); // Expected: 1

// Target smaller than all values
int[] players5 = { 200, 300, 400 };
int target5 = 100;
int result5 = solution.FirstDayAtOrAboveTarget(players5, target5);
Console.WriteLine(result5); // Expected: 0