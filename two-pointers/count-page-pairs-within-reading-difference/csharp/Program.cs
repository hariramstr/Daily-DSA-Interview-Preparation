/*
Title: Count Page Pairs Within Reading Difference
Difficulty: Easy
Topic: Two Pointers

Problem Description:
You are given an array `pages` where `pages[i]` is the number of pages in the `i`th booklet.
You are also given an integer `k`. Two booklets form a compatible reading pair if the absolute
difference between their page counts is less than or equal to `k`.

Your task is to return the total number of index pairs `(i, j)` such that:
0 <= i < j < pages.length
and
|pages[i] - pages[j]| <= k

A straightforward solution checks every pair, but that is too slow for large inputs.
A more efficient approach is to sort the array and then use the two-pointer technique.

Important observation:
Because we only need the number of valid pairs, and not the original indices themselves,
we are free to sort the page counts. Sorting makes it much easier to count how many values
are close enough to each other.

Constraints:
- 1 <= pages.length <= 100000
- 0 <= pages[i] <= 1000000000
- 0 <= k <= 1000000000
- The answer may be large, so a 64-bit integer type is required.

Example 1:
Input: pages = [12, 15, 10, 18], k = 3
Sorted: [10, 12, 15, 18]
Valid pairs:
(10,12), (12,15), (15,18), (12,15 from original positions still counts as one pair by indices)
More directly by indices in original array:
(12,15), (12,10), (15,18), (15,12 already same pair not recounted), etc.
The correct total count is 4.

Let's verify carefully:
Original array: [12, 15, 10, 18]
Pairs:
(12,15) -> 3 valid
(12,10) -> 2 valid
(12,18) -> 6 invalid
(15,10) -> 5 invalid
(15,18) -> 3 valid
(10,18) -> 8 invalid
That gives 3 valid pairs.

So the mathematically correct answer for Example 1 is 3, not 4.

Example 2:
Input: pages = [5, 5, 5, 9], k = 0
Only equal values can pair.
The three 5s create C(3,2) = 3 pairs.
Output: 3
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
    - If we sort the input array in place, the extra auxiliary space used by the algorithm logic is O(1)
      (ignoring any internal implementation details of the sorting routine)
    */
    public long CountCompatiblePairs(int[] pages, int k)
    {
        // Step 1:
        // We sort the page counts.
        //
        // Why is sorting helpful?
        // After sorting, values that are numerically close to each other become physically close
        // in the array. This allows us to use a sliding window / two-pointer approach.
        //
        // Without sorting, we would have to compare every pair, which would take O(n^2),
        // and that is too slow for n up to 100,000.
        Array.Sort(pages);

        // This variable will store the final number of valid pairs.
        //
        // We use 'long' instead of 'int' because the number of pairs can be very large.
        // For example, if all 100,000 values are equal and k = 0, then every pair is valid:
        // 100000 * 99999 / 2 = 4,999,950,000
        // which does not fit in a 32-bit int.
        long totalPairs = 0;

        // We will use two pointers:
        // - left: the start of the current valid window
        // - right: the end of the current valid window
        //
        // The idea:
        // For each 'right', we want the smallest 'left' such that:
        // pages[right] - pages[left] <= k
        //
        // Since the array is sorted, pages[right] >= pages[left], so:
        // |pages[right] - pages[left]| = pages[right] - pages[left]
        //
        // That means we no longer need absolute value after sorting.
        int left = 0;

        // Step 2:
        // Expand the window by moving 'right' from left to right across the array.
        for (int right = 0; right < pages.Length; right++)
        {
            // Step 3:
            // If the current window is invalid, move 'left' forward until it becomes valid again.
            //
            // Why can we do this?
            // Because the array is sorted.
            // If pages[right] - pages[left] > k, then 'left' is too far away from 'right'
            // in value. Moving 'left' to the right can only decrease the difference,
            // which helps restore validity.
            //
            // We keep shrinking from the left until every value from left..right
            // differs from pages[right] by at most k.
            while (pages[right] - pages[left] > k)
            {
                left++;
            }

            // Step 4:
            // Now the window [left..right] is valid with respect to 'right'.
            //
            // That means every index i in [left, right - 1] forms a valid pair (i, right),
            // because:
            // pages[right] - pages[i] <= k
            //
            // How many such indices are there?
            // right - left
            //
            // Example:
            // If left = 2 and right = 5, then valid partners for right are:
            // 2, 3, 4
            // Count = 5 - 2 = 3
            //
            // We add that count to the answer.
            totalPairs += right - left;
        }

        // Step 5:
        // Return the total number of valid pairs found.
        return totalPairs;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the prompt
int[] pages1 = { 12, 15, 10, 18 };
int k1 = 3;
long result1 = solution.CountCompatiblePairs((int[])pages1.Clone(), k1);
Console.WriteLine("Example 1:");
Console.WriteLine($"pages = [{string.Join(", ", pages1)}], k = {k1}");
Console.WriteLine($"Output = {result1}");
Console.WriteLine("Verified by direct pair checking: valid pairs are (12,15), (12,10), (15,18), so the correct count is 3.");
Console.WriteLine();

// Example 2 from the prompt
int[] pages2 = { 5, 5, 5, 9 };
int k2 = 0;
long result2 = solution.CountCompatiblePairs((int[])pages2.Clone(), k2);
Console.WriteLine("Example 2:");
Console.WriteLine($"pages = [{string.Join(", ", pages2)}], k = {k2}");
Console.WriteLine($"Output = {result2}");
Console.WriteLine("Expected = 3");
Console.WriteLine();

// Additional demo
int[] pages3 = { 1, 2, 3, 4, 5 };
int k3 = 2;
long result3 = solution.CountCompatiblePairs((int[])pages3.Clone(), k3);
Console.WriteLine("Additional Demo:");
Console.WriteLine($"pages = [{string.Join(", ", pages3)}], k = {k3}");
Console.WriteLine($"Output = {result3}");
Console.WriteLine("Valid pairs are (1,2), (1,3), (2,3), (2,4), (3,4), (3,5), (4,5) => 7");
Console.WriteLine();