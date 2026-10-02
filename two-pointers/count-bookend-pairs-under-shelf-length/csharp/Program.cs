/*
Title: Count Bookend Pairs Under Shelf Length
Difficulty: Easy
Topic: Two Pointers

Problem Description:
A library is arranging decorative bookends in a display. You are given an integer array lengths where lengths[i] is the length of the i-th bookend, and an integer shelfLimit representing the maximum total length that can fit comfortably on one shelf section. Two different bookends can be placed together if their combined length is less than or equal to shelfLimit.

Return the number of distinct pairs of bookends (i, j) such that 0 <= i < j < lengths.length and lengths[i] + lengths[j] <= shelfLimit.

Your solution should be efficient enough for large inputs. A brute-force O(n^2) approach may work for very small arrays, but interviewers expect you to recognize that sorting the array and using two pointers can count valid pairs much faster.

Constraints:
- 1 <= lengths.length <= 100000
- 1 <= lengths[i] <= 1000000000
- 1 <= shelfLimit <= 2000000000
- The answer can be large, so use a 64-bit integer type if needed.

Example 1:
Input: lengths = [1, 3, 2, 2], shelfLimit = 4
Output: 4
Explanation: After sorting, lengths becomes [1, 2, 2, 3]. Valid pairs are (1,2), (1,2), (1,3), and the two 2s together. That gives 4 pairs in total.

Example 2:
Input: lengths = [5, 1, 4, 2], shelfLimit = 5
Output: 2
Explanation: Valid pairs are (1,4) and (1,2). No other pair has total length at most 5.
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
    - If we sort the input array in place, the extra working space used by our algorithm is O(1)
      not counting the internal space used by the sorting implementation.
    */
    public long CountBookendPairs(int[] lengths, int shelfLimit)
    {
        // Step 1:
        // Sort the array so that the bookend lengths are in non-decreasing order.
        //
        // Why this is necessary:
        // The two-pointer technique relies on order. Once the array is sorted,
        // we can make smart decisions:
        // - If the smallest remaining value plus the largest remaining value fits,
        //   then that smallest value will also fit with every value between them.
        // - If they do not fit, then the largest value is too large to pair with
        //   the current smallest value, and also too large to pair with anything larger
        //   than the current smallest value.
        //
        // This sorted structure is what allows us to count many pairs at once
        // instead of checking every possible pair one by one.
        Array.Sort(lengths);

        // Step 2:
        // Create two pointers:
        // - left starts at the beginning of the sorted array (smallest value)
        // - right starts at the end of the sorted array (largest value)
        //
        // Why this is necessary:
        // We want to efficiently explore which pairs are valid.
        // Using one pointer from each end lets us compare extremes and decide
        // whether to count a whole block of pairs or move inward.
        int left = 0;
        int right = lengths.Length - 1;

        // Step 3:
        // Use a 64-bit integer (long) for the answer.
        //
        // Why this is necessary:
        // The number of valid pairs can be very large.
        // In the worst case, if every pair is valid and n = 100000,
        // the number of pairs is n * (n - 1) / 2 = 4,999,950,000,
        // which does not fit inside a 32-bit int.
        long pairCount = 0;

        // Step 4:
        // Continue while there are at least two different positions left to consider.
        //
        // Why this condition:
        // A pair requires i < j, so left must stay strictly less than right.
        while (left < right)
        {
            // Step 5:
            // Compute the sum of the current smallest and current largest remaining lengths.
            //
            // We cast to long before adding to be extra safe and explicit,
            // even though the given constraints still keep the sum within int range.
            long currentSum = (long)lengths[left] + lengths[right];

            // Step 6:
            // If the current pair fits within the shelf limit...
            if (currentSum <= shelfLimit)
            {
                // This is the key observation of the two-pointer method:
                //
                // Because the array is sorted:
                // lengths[left] <= lengths[left + 1] <= ... <= lengths[right]
                //
                // If lengths[left] + lengths[right] <= shelfLimit,
                // then lengths[left] can pair with:
                // - lengths[left + 1]
                // - lengths[left + 2]
                // - ...
                // - lengths[right]
                //
                // Why is that true?
                // Because every value between left and right is <= lengths[right].
                // So if the largest one works with lengths[left], then all smaller ones
                // in that range also work with lengths[left].
                //
                // Number of such pairs:
                // right - left
                //
                // Example:
                // If left = 0 and right = 3, then index 0 can pair with 1, 2, and 3,
                // which is 3 pairs total.
                pairCount += right - left;

                // After counting all pairs that start with lengths[left],
                // we move left forward.
                //
                // Why this is correct:
                // We have already counted every valid pair involving the current left element
                // and any index up to right. There is nothing more to do with this left value.
                left++;
            }
            else
            {
                // If the current smallest + current largest is too large,
                // then the current largest cannot form a valid pair with the current smallest.
                //
                // More importantly, because the array is sorted, the current largest also cannot
                // form a valid pair with any element to the right of left (those are >= lengths[left]).
                //
                // So the current largest value is too large to be used in any valid pair
                // with the current search window. We must reduce the sum by moving right inward.
                right--;
            }
        }

        // Step 7:
        // Return the total number of valid distinct pairs.
        return pairCount;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] lengths1 = { 1, 3, 2, 2 };
int shelfLimit1 = 4;
long result1 = solution.CountBookendPairs(lengths1, shelfLimit1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2
int[] lengths2 = { 5, 1, 4, 2 };
int shelfLimit2 = 5;
long result2 = solution.CountBookendPairs(lengths2, shelfLimit2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional quick demo
int[] lengths3 = { 2, 2, 2 };
int shelfLimit3 = 4;
long result3 = solution.CountBookendPairs(lengths3, shelfLimit3);
Console.WriteLine($"Additional Demo Result: {result3}");