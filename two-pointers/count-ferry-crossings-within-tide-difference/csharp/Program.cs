/*
Title: Count Ferry Crossings Within Tide Difference
Difficulty: Hard
Topic: Two Pointers

Problem Description:
A coastal logistics company records the tide level at each scheduled ferry departure during a day.
You are given an integer array tides of length n, where tides[i] is the tide level at the i-th
departure slot, and an integer limit.

Two departure slots i < j form a valid crossing pair if the absolute difference between their tide
levels is at most limit, that is, |tides[i] - tides[j]| <= limit.

Your task is to count how many distinct index pairs (i, j) are valid.

Because the number of pairs can be large, return the answer as a 64-bit integer.

A brute-force O(n^2) solution will time out for the largest inputs. You are expected to design an
efficient solution using sorting and a two-pointers strategy.

Constraints:
- 1 <= n <= 200000
- -10^9 <= tides[i] <= 10^9
- 0 <= limit <= 10^9

Example 1:
Input: tides = [4, 1, 7, 3, 5], limit = 2
Output: 4
Explanation: After sorting, the tide levels are [1, 3, 4, 5, 7].
Valid pairs are (1,3), (3,4), (3,5), and (4,5) by value, for a total of 4 pairs.

Example 2:
Input: tides = [8, 8, 8, 8], limit = 0
Output: 6
Explanation: Every pair of equal tide levels is valid. With 4 departures, the number of pairs is
4 * 3 / 2 = 6.

Notes:
- Pairs are based on original indices, but only the tide values matter for validity.
- Duplicate tide levels are allowed.
- An optimal solution should run in O(n log n) time due to sorting, with a linear two-pointer scan afterward.
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
    - If we sort the input array in place, the extra algorithmic space is O(1)
      (ignoring the internal stack/details used by the sorting implementation)
    */
    public long CountValidCrossingPairs(int[] tides, int limit)
    {
        // Step 1:
        // We sort the tide levels.
        //
        // Why sorting helps:
        // The condition we care about is:
        //     |tides[i] - tides[j]| <= limit
        //
        // After sorting, for any pair where left < right, we know:
        //     sorted[right] >= sorted[left]
        //
        // So the absolute value is no longer needed, because:
        //     |sorted[right] - sorted[left]| = sorted[right] - sorted[left]
        //
        // This transforms the problem into:
        // "For each left position, how far right can we go while keeping
        //  sorted[right] - sorted[left] <= limit?"
        //
        // That is exactly the kind of structure where two pointers work very well.
        Array.Sort(tides);

        // This will store the total number of valid pairs.
        // We use long because n can be as large as 200,000,
        // and the number of pairs can be about n * (n - 1) / 2,
        // which does not safely fit in int.
        long count = 0;

        // We use two pointers:
        // - left  = start of the current window
        // - right = first index that is NOT valid for the current left
        //
        // Important interpretation:
        // At any moment, we try to maintain a window [left, right)
        // such that every index from left+1 up to right-1 forms a valid pair with left.
        //
        // In other words:
        // - indices in [left + 1, right - 1] are valid partners for left
        // - index right is either out of bounds OR invalid
        int right = 0;

        // Step 2:
        // Move left from the beginning to the end of the sorted array.
        for (int left = 0; left < tides.Length; left++)
        {
            // Step 2a:
            // Ensure right is never behind left.
            //
            // This can happen at the very beginning or after left moves forward.
            // Since a pair must satisfy i < j, the right pointer should be at least left + 1
            // when we start searching for valid partners of left.
            if (right < left + 1)
            {
                right = left + 1;
            }

            // Step 2b:
            // Expand the right pointer as long as the pair (left, right) is valid.
            //
            // Because the array is sorted:
            // - if tides[right] - tides[left] <= limit, then right is valid
            // - we keep moving right to include as many valid elements as possible
            //
            // Why this is efficient:
            // We never move right backward. Across the whole algorithm, right moves
            // from 0 to n at most once. That is why the scan is linear after sorting.
            while (right < tides.Length && (long)tides[right] - tides[left] <= limit)
            {
                right++;
            }

            // Step 2c:
            // Now right is the first index that is invalid for this left
            // (or right == tides.Length if we reached the end).
            //
            // Therefore, the valid partner indices for this left are:
            //     left + 1, left + 2, ..., right - 1
            //
            // How many are there?
            //     (right - 1) - (left + 1) + 1 = right - left - 1
            //
            // We add that number to the answer.
            count += right - left - 1;
        }

        // Step 3:
        // Return the total number of valid pairs.
        return count;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] tides1 = { 4, 1, 7, 3, 5 };
int limit1 = 2;
long result1 = solution.CountValidCrossingPairs((int[])tides1.Clone(), limit1);
Console.WriteLine(result1); // Expected: 4

// Example 2
int[] tides2 = { 8, 8, 8, 8 };
int limit2 = 0;
long result2 = solution.CountValidCrossingPairs((int[])tides2.Clone(), limit2);
Console.WriteLine(result2); // Expected: 6

// Additional quick demo
int[] tides3 = { -2, 0, 1, 4, 6 };
int limit3 = 3;
long result3 = solution.CountValidCrossingPairs((int[])tides3.Clone(), limit3);
Console.WriteLine(result3); // One more sample output for demonstration