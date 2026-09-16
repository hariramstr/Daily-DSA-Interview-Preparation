/*
Title: Count Pairs of Photos Within Brightness Budget

Problem Description:
You are given an integer array brightness where brightness[i] is the brightness score of the i-th photo in a gallery,
and an integer budget. Two photos can be edited together if the absolute difference between their brightness scores
is less than or equal to budget.

Your task is to return the number of distinct pairs of indices (i, j) such that:
0 <= i < j < n
and
|brightness[i] - brightness[j]| <= budget

A brute-force solution that checks every pair takes O(n^2) time and is too slow for large galleries.
We need an efficient algorithm for up to 200,000 photos.

A strong approach is:
1. Sort the brightness values
2. Use two pointers to count, for each left position, how many right positions can pair with it

Important note:
Even though pairs are defined by original indices, sorting does not change the number of valid pairs.
Each photo value still appears exactly once in the sorted array, so counting valid value-pairs in sorted order
correctly counts the original index-pairs.

Constraints:
- 1 <= brightness.length <= 200000
- 0 <= brightness[i] <= 10^9
- 0 <= budget <= 10^9

Example 1:
Input: brightness = [4, 1, 7, 5], budget = 3
Output: 4
Explanation:
Valid pairs are:
(4,1) difference 3
(4,7) difference 3
(4,5) difference 1
(7,5) difference 2

Example 2:
Input: brightness = [2, 2, 2, 8, 9], budget = 1
Output: 4
Explanation:
Among the three photos with brightness 2, there are 3 valid pairs:
choose any 2 out of 3 => 3 pairs
Also (8,9) is valid => +1
Total = 4
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
    - If we clone the input to avoid modifying the caller's array, that uses O(n) extra space
    - Aside from that, the algorithm itself uses O(1) extra working space
    - Total: O(n) because of the copy
    */
    public long CountPairsWithinBudget(int[] brightness, int budget)
    {
        // We will return a long instead of int.
        // Why?
        // Because the number of valid pairs can be very large.
        //
        // For example, if all 200,000 photos can pair with each other,
        // the number of pairs is:
        // 200000 * 199999 / 2 = 19,999,900,000
        //
        // That value does NOT fit in a 32-bit int, but it DOES fit in a 64-bit long.
        long totalPairs = 0;

        // We make a copy of the input array before sorting.
        // Why?
        // Because sorting changes the order of elements.
        // In many coding problems this is acceptable, but making a copy is safer and clearer:
        // - it preserves the original input
        // - it makes the method easier to reuse without surprising side effects
        int[] sorted = (int[])brightness.Clone();

        // Sorting is the key step that makes the two-pointer technique possible.
        //
        // After sorting:
        // - values are in non-decreasing order
        // - for a fixed left index, as right moves to the right, differences only stay the same or increase
        //
        // That monotonic behavior is exactly what allows us to count efficiently.
        Array.Sort(sorted);

        // "right" will represent the first index that is TOO FAR from "left".
        //
        // More precisely:
        // For each left index:
        // - all indices in [left + 1, right - 1] are valid partners
        // - index right is either out of bounds, or it is the first invalid partner
        //
        // This "first invalid position" interpretation is very useful because then
        // the number of valid partners is simply:
        // (right - left - 1)
        int right = 0;

        // We iterate over every possible left endpoint of a pair.
        for (int left = 0; left < sorted.Length; left++)
        {
            // Important pointer maintenance:
            // right should never be behind left + 1, because pairs require j > i.
            //
            // If right is behind, we move it up to left + 1.
            // This can happen at the beginning or after left advances.
            if (right < left + 1)
            {
                right = left + 1;
            }

            // Now we expand the right pointer as far as possible while the pair remains valid.
            //
            // Condition:
            // sorted[right] - sorted[left] <= budget
            //
            // Why can we use subtraction without absolute value?
            // Because the array is sorted, so sorted[right] >= sorted[left] whenever right > left.
            // Therefore:
            // |sorted[right] - sorted[left]| = sorted[right] - sorted[left]
            //
            // Why is this loop efficient overall?
            // Because right only moves forward across the entire algorithm.
            // It never resets back to the left.
            while (right < sorted.Length && (long)sorted[right] - sorted[left] <= budget)
            {
                right++;
            }

            // At this point:
            // - every index from left + 1 up to right - 1 is valid
            // - right is the first invalid index, or right == sorted.Length
            //
            // So the number of valid partners for this specific left is:
            // (right - 1) - (left + 1) + 1
            // which simplifies to:
            // right - left - 1
            //
            // Example:
            // if left = 2 and right = 6,
            // then valid partners are indices 3, 4, 5
            // count = 3 = 6 - 2 - 1
            long validPartnersForThisLeft = right - left - 1;

            // Add this count into the global answer.
            totalPairs += validPartnersForThisLeft;
        }

        // After processing every left index, totalPairs contains the number of all distinct pairs.
        return totalPairs;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] brightness1 = { 4, 1, 7, 5 };
int budget1 = 3;
long result1 = solution.CountPairsWithinBudget(brightness1, budget1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 4

// Example 2
int[] brightness2 = { 2, 2, 2, 8, 9 };
int budget2 = 1;
long result2 = solution.CountPairsWithinBudget(brightness2, budget2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 4

// Additional demo 1: single element => no pairs
int[] brightness3 = { 10 };
int budget3 = 5;
long result3 = solution.CountPairsWithinBudget(brightness3, budget3);
Console.WriteLine("Additional Demo 1 Result: " + result3); // Expected: 0

// Additional demo 2: all values close enough
int[] brightness4 = { 1, 2, 3, 4 };
int budget4 = 10;
long result4 = solution.CountPairsWithinBudget(brightness4, budget4);
Console.WriteLine("Additional Demo 2 Result: " + result4); // Expected: 6

// Additional demo 3: no valid pairs
int[] brightness5 = { 1, 10, 20, 30 };
int budget5 = 2;
long result5 = solution.CountPairsWithinBudget(brightness5, budget5);
Console.WriteLine("Additional Demo 3 Result: " + result5); // Expected: 0