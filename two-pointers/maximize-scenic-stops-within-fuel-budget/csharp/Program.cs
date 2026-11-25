/*
Title: Maximize Scenic Stops Within Fuel Budget
Difficulty: Medium
Topic: Two Pointers

Problem Description:
You are planning a road trip along a straight highway. There are n scenic stops placed in increasing order of distance from the start, given by the array miles, where miles[i] is the mile marker of the i-th stop. Visiting a group of consecutive scenic stops is only practical if the distance between the farthest and nearest stop in that group does not exceed your extra sightseeing fuel budget budget.

Your task is to return the maximum number of consecutive scenic stops you can include in a single sightseeing segment such that:

miles[right] - miles[left] <= budget

for the chosen segment [left, right].

This models the idea that once you commit to a detour region, you only want stops clustered closely enough together to avoid wasting fuel driving back and forth. You may choose any contiguous block of stops from the sorted list, including a block of length 1.

Write a function that returns the largest possible number of stops in such a valid segment.

Constraints:
- 1 <= n <= 100000
- 0 <= miles[i] <= 10^9
- miles is sorted in non-decreasing order
- 0 <= budget <= 10^9

Example 1:
Input: miles = [2, 5, 6, 7, 11, 15], budget = 4
Output: 3
Explanation: The segment [5, 6, 7] fits because 7 - 5 = 2 <= 4. The segment [2, 5, 6, 7] does not fit because 7 - 2 = 5 > 4.

Example 2:
Input: miles = [1, 1, 3, 4, 8, 9, 10], budget = 2
Output: 3
Explanation: One optimal segment is [8, 9, 10], where 10 - 8 = 2. Another valid segment of length 3 is [1, 1, 3], where 3 - 1 = 2.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Why:
    - We use the classic "sliding window" / "two pointers" technique.
    - The right pointer moves from left to right exactly once.
    - The left pointer also only moves from left to right and never moves backward.
    - Because each pointer advances at most n times, the total work is linear.

    Beginner-friendly intuition:
    - We want the longest consecutive block where:
          miles[right] - miles[left] <= budget
    - Since the array is already sorted, the smallest value in the current block is at index left,
      and the largest value is at index right.
    - That means we can check whether the current block is valid using only those two endpoints.
    - If the block becomes too wide (difference > budget), we move left forward until it becomes valid again.
    - At every step, we record the largest valid window size we have seen.
    */
    public int MaxScenicStops(int[] miles, int budget)
    {
        // This variable stores the best answer found so far.
        // Since the problem allows choosing a block of length 1,
        // and n >= 1, the final answer will always be at least 1.
        int maxStops = 0;

        // "left" is the start index of our current sliding window.
        // The window will always represent a contiguous block [left..right].
        int left = 0;

        // We expand the window by moving "right" one step at a time.
        // For each right position, we try to include miles[right] in the current segment.
        for (int right = 0; right < miles.Length; right++)
        {
            // At this moment, the current window is [left..right].
            // Because the array is sorted, the smallest mile marker in this window is miles[left],
            // and the largest is miles[right].
            //
            // The problem says the segment is valid only if:
            //     miles[right] - miles[left] <= budget
            //
            // If this condition is violated, the window is too wide.
            // To fix it, we must move "left" forward.
            //
            // Why moving left works:
            // - The array is sorted.
            // - Increasing left means removing smaller values from the window.
            // - That can only reduce (or keep) the difference miles[right] - miles[left].
            while (miles[right] - miles[left] > budget)
            {
                // Move the left boundary to the right by one.
                // This shrinks the window from the left side.
                left++;
            }

            // After the while loop finishes, the window [left..right] is guaranteed to be valid.
            //
            // Its size is:
            //     right - left + 1
            //
            // We compare it with the best answer seen so far.
            int currentWindowSize = right - left + 1;
            if (currentWindowSize > maxStops)
            {
                maxStops = currentWindowSize;
            }
        }

        // Return the largest valid window size found.
        return maxStops;
    }
}

// Demo code:
// Creates sample inputs, calls the solution, and prints the results.

var solution = new Solution();

// Example 1:
// miles = [2, 5, 6, 7, 11, 15], budget = 4
// Expected output: 3
//
// Quick trace:
// right=0 -> [2], size=1
// right=1 -> [2,5], diff=3, valid, size=2
// right=2 -> [2,5,6], diff=4, valid, size=3
// right=3 -> [2,5,6,7], diff=5, invalid -> move left to 1
//            [5,6,7], diff=2, valid, size=3
// right=4 -> [5,6,7,11], diff=6, invalid -> move left until valid
//            [7,11], diff=4, size=2
// right=5 -> [7,11,15], diff=8, invalid -> move left until valid
//            [11,15], diff=4, size=2
// Best = 3
int[] miles1 = { 2, 5, 6, 7, 11, 15 };
int budget1 = 4;
int result1 = solution.MaxScenicStops(miles1, budget1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2:
// miles = [1, 1, 3, 4, 8, 9, 10], budget = 2
// Expected output: 3
//
// Quick trace:
// right=0 -> [1], size=1
// right=1 -> [1,1], diff=0, size=2
// right=2 -> [1,1,3], diff=2, size=3
// right=3 -> [1,1,3,4], diff=3, invalid -> move left
//            [1,3,4], diff=3, still invalid -> move left
//            [3,4], diff=1, valid, size=2
// right=4 -> [3,4,8], diff=5, invalid -> move left until [8], size=1
// right=5 -> [8,9], diff=1, size=2
// right=6 -> [8,9,10], diff=2, size=3
// Best = 3
int[] miles2 = { 1, 1, 3, 4, 8, 9, 10 };
int budget2 = 2;
int result2 = solution.MaxScenicStops(miles2, budget2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional small demo:
// Single stop always works, so answer should be 1.
int[] miles3 = { 42 };
int budget3 = 0;
int result3 = solution.MaxScenicStops(miles3, budget3);
Console.WriteLine($"Additional Demo Result: {result3}");