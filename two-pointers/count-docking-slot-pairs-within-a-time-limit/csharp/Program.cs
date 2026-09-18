/*
Title: Count Docking Slot Pairs Within a Time Limit
Difficulty: Medium
Topic: Two Pointers

Problem Description:
A shipping terminal records the available docking duration of each open slot during the next hour.
You are given an integer array durations where durations[i] is the number of minutes that slot i
will remain available, and an integer limit.

Two different slots can be assigned to a dual-berth vessel only if their combined available time
is less than or equal to limit.

Your task is to return the number of distinct pairs of slots (i, j) such that:
- i < j
- durations[i] + durations[j] <= limit

The input array is not guaranteed to be sorted. A brute-force O(n^2) approach may be too slow for
large terminals, so we design an efficient solution using sorting and a two-pointer strategy.

A pair is counted by indices, not by values. This means if the same duration appears multiple times,
different index combinations are considered different valid pairs.

Constraints:
- 1 <= durations.length <= 200000
- 0 <= durations[i] <= 1000000000
- 0 <= limit <= 2000000000
- The answer can be large, so use a 64-bit integer type if needed.

Example 1:
Input: durations = [4, 1, 3, 2], limit = 5
Output: 4

Explanation:
After sorting, durations become [1, 2, 3, 4].
Valid pairs are:
(1,2) => 3
(1,3) => 4
(1,4) => 5
(2,3) => 5
Total = 4

Example 2:
Input: durations = [6, 2, 2, 5, 1], limit = 7
Output: 6

Explanation:
After sorting, durations become [1, 2, 2, 5, 6].
Valid pairs by values are:
(1,2), (1,2), (1,5), (1,6), (2,2), (2,5)
Total = 6
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the array takes O(n log n)
    - The two-pointer scan takes O(n)
    - Total: O(n log n)

    Space Complexity:
    - If we sort the input array in place, the extra space used by our algorithm logic is O(1)
      not counting the internal implementation details of the sorting routine.
    */
    public long CountValidPairs(int[] durations, int limit)
    {
        // Step 1:
        // Sort the array so that we can use the two-pointer technique efficiently.
        //
        // Why sorting helps:
        // Once the values are in non-decreasing order, we gain a very useful property:
        // if durations[left] + durations[right] is small enough to satisfy the limit,
        // then durations[left] paired with ANY element between left+1 and right will also
        // satisfy the limit, because those elements are <= durations[right].
        //
        // This is the key idea that lets us count many pairs at once instead of checking
        // every possible pair individually.
        Array.Sort(durations);

        // Step 2:
        // Create two pointers:
        // - left starts at the beginning (smallest duration)
        // - right starts at the end (largest duration)
        //
        // We will move these pointers toward each other while counting valid pairs.
        int left = 0;
        int right = durations.Length - 1;

        // Step 3:
        // Use a 64-bit integer (long) for the answer.
        //
        // Why long is necessary:
        // In the worst case, if every pair is valid and n = 200000,
        // the number of pairs is n * (n - 1) / 2 = 19,999,900,000,
        // which does not fit in a 32-bit int.
        long count = 0;

        // Step 4:
        // Continue while left is strictly less than right.
        //
        // Why:
        // A pair requires two different indices, so once left meets right,
        // there are no more valid distinct pairs to consider.
        while (left < right)
        {
            // Step 4a:
            // Compute the sum of the current smallest remaining value and
            // the current largest remaining value.
            //
            // We cast to long before adding to be extra safe, even though the
            // given constraints still fit within int for the sum.
            long currentSum = (long)durations[left] + durations[right];

            // Step 4b:
            // If the current pair fits within the limit, then we can count
            // multiple pairs at once.
            if (currentSum <= limit)
            {
                // Why all pairs from left with indices (left+1 ... right) are valid:
                //
                // The array is sorted, so:
                // durations[left + 1], durations[left + 2], ..., durations[right]
                // are all <= durations[right].
                //
                // Since durations[left] + durations[right] <= limit,
                // then durations[left] + durations[k] <= limit for every k in [left+1, right].
                //
                // That means the following pairs are all valid:
                // (left, left+1), (left, left+2), ..., (left, right)
                //
                // Number of such pairs = right - left
                count += right - left;

                // After counting all pairs that start with 'left', we move left forward.
                //
                // Why move left:
                // We have already counted every valid pair involving the current left index.
                // There is no need to keep it anymore.
                left++;
            }
            else
            {
                // If the sum is too large, then the current largest value at 'right'
                // is too big to pair with durations[left].
                //
                // Because the array is sorted, durations[right] is also too big to pair
                // with any index greater than left and less than right if we keep the same right?
                // More precisely, to reduce the sum, we need a smaller right-side value.
                //
                // So we move 'right' one step left to try a smaller duration.
                right--;
            }
        }

        // Step 5:
        // Return the total number of valid pairs found.
        return count;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] durations1 = { 4, 1, 3, 2 };
int limit1 = 5;
long result1 = solution.CountValidPairs((int[])durations1.Clone(), limit1);
Console.WriteLine("Example 1 Result: " + result1);

// Example 2
int[] durations2 = { 6, 2, 2, 5, 1 };
int limit2 = 7;
long result2 = solution.CountValidPairs((int[])durations2.Clone(), limit2);
Console.WriteLine("Example 2 Result: " + result2);

// Additional demo cases

int[] durations3 = { 1, 1, 1, 1 };
int limit3 = 2;
long result3 = solution.CountValidPairs((int[])durations3.Clone(), limit3);
Console.WriteLine("All pairs valid Result: " + result3);

int[] durations4 = { 10, 20, 30 };
int limit4 = 5;
long result4 = solution.CountValidPairs((int[])durations4.Clone(), limit4);
Console.WriteLine("No pairs valid Result: " + result4);

int[] durations5 = { 0, 7, 3, 4, 2 };
int limit5 = 7;
long result5 = solution.CountValidPairs((int[])durations5.Clone(), limit5);
Console.WriteLine("Mixed values Result: " + result5);