/*
Title: Count Team Pairings Within Experience Gap
Difficulty: Medium
Topic: Two Pointers

Problem Description:
You are given an integer array experience where experience[i] is the number of years of experience of the i-th engineer,
and an integer gap. A pair of engineers (i, j) is considered compatible if i < j and the absolute difference between
their experience values is less than or equal to gap.

Return the total number of compatible pairs.

A straightforward O(n^2) solution checks every pair, but that is too slow for large inputs. Your task is to design
an efficient algorithm using sorting and a two-pointers technique.

Because the pair condition depends only on the difference between two values, the array may be reordered during processing.
However, the final answer should count pairs from the original set of engineers, not based on their positions after sorting.

Constraints:
- 1 <= experience.length <= 200000
- 0 <= experience[i] <= 1000000000
- 0 <= gap <= 1000000000
- The answer may be large, so use a 64-bit integer type where needed.

Example 1:
Input: experience = [1, 3, 4, 7], gap = 3
Output: 4

Example 2:
Input: experience = [5, 5, 5, 8, 10], gap = 0
Output: 3
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the array takes O(n log n)
    - The two-pointer scan takes O(n), because each pointer only moves forward
    - Total: O(n log n)

    Space Complexity:
    - Aside from the sorting implementation details used by Array.Sort, the algorithm itself uses O(1) extra space
    - So the practical extra space of the algorithm logic is O(1)
    */
    public long CountCompatiblePairs(int[] experience, int gap)
    {
        // If there are fewer than 2 engineers, no pair can exist.
        // This is a quick safety check and also makes the method robust.
        if (experience == null || experience.Length < 2)
        {
            return 0L;
        }

        // We are allowed to reorder the array because the compatibility condition depends
        // only on the values, not on the original positions.
        //
        // Why sorting helps:
        // After sorting, for any fixed right endpoint, all values to its left are in nondecreasing order.
        // That means if the difference between experience[right] and experience[left] is small enough,
        // then every element between left and right also forms a valid pair with right.
        //
        // This is exactly what makes the two-pointer technique efficient here.
        Array.Sort(experience);

        // This will store the total number of valid pairs.
        // We use long because the number of pairs can be as large as n * (n - 1) / 2,
        // which does not always fit in a 32-bit int when n is large.
        long totalPairs = 0L;

        // "left" marks the smallest index in the current valid window.
        // For each "right", we will move "left" forward until the difference condition is satisfied.
        int left = 0;

        // We expand the window by moving "right" from left to right across the sorted array.
        for (int right = 0; right < experience.Length; right++)
        {
            // Current goal:
            // Ensure that the window [left, right] satisfies:
            // experience[right] - experience[left] <= gap
            //
            // Why this works:
            // Since the array is sorted, experience[right] >= experience[left].
            // Therefore absolute difference becomes:
            // |experience[right] - experience[left]| = experience[right] - experience[left]
            //
            // If the difference is too large, then the leftmost value is too far away from experience[right],
            // so we must move left forward to shrink the window.
            while (left < right && (long)experience[right] - experience[left] > gap)
            {
                left++;
            }

            // At this point, the window [left, right] is the largest valid window ending at "right"
            // such that every index k in [left, right - 1] forms a valid pair (k, right).
            //
            // Why are all those pairs valid?
            // Because the array is sorted:
            // experience[left] <= experience[k] <= experience[right]
            //
            // We already know:
            // experience[right] - experience[left] <= gap
            //
            // Therefore for any k between left and right:
            // experience[right] - experience[k] <= experience[right] - experience[left] <= gap
            //
            // So every engineer from left through right - 1 can pair with engineer right.
            //
            // Number of such engineers = right - left
            totalPairs += right - left;
        }

        return totalPairs;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1:
// Sorted: [1, 3, 4, 7]
// Valid pairs:
// (1,3), (1,4), (3,4), (4,7) => 4
int[] experience1 = { 1, 3, 4, 7 };
int gap1 = 3;
long result1 = solution.CountCompatiblePairs((int[])experience1.Clone(), gap1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2:
// Sorted: [5, 5, 5, 8, 10]
// With gap = 0, only equal values can pair.
// Three 5s produce 3 choose 2 = 3 pairs.
int[] experience2 = { 5, 5, 5, 8, 10 };
int gap2 = 0;
long result2 = solution.CountCompatiblePairs((int[])experience2.Clone(), gap2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional demo cases for learning and confidence:

// Single engineer => no pairs
int[] experience3 = { 42 };
int gap3 = 10;
long result3 = solution.CountCompatiblePairs((int[])experience3.Clone(), gap3);
Console.WriteLine($"Single Engineer Result: {result3}");

// All values close enough => every pair is valid
// 4 engineers => 4 * 3 / 2 = 6 pairs
int[] experience4 = { 2, 4, 3, 5 };
int gap4 = 3;
long result4 = solution.CountCompatiblePairs((int[])experience4.Clone(), gap4);
Console.WriteLine($"All Pairs Valid Result: {result4}");

// No values close enough => 0 pairs
int[] experience5 = { 1, 10, 20, 30 };
int gap5 = 2;
long result5 = solution.CountCompatiblePairs((int[])experience5.Clone(), gap5);
Console.WriteLine($"No Pairs Valid Result: {result5}");