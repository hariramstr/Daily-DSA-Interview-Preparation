/*
Title: Count Container Pairs Within Fill Difference

Problem Description:
A shipping company records the current fill level of each storage container as an integer percentage from 0 to 100.
For a balancing operation, two containers are considered compatible if the absolute difference between their fill
levels is at most k.

You are given an integer array fills, where fills[i] is the fill level of the i-th container, and an integer k.
Return the number of index pairs (i, j) such that i < j and |fills[i] - fills[j]| <= k.

Because the number of valid pairs can be large, your algorithm should be efficient enough for large inputs.
A brute-force O(n^2) solution will not pass the largest test cases.

Your task is to count all compatible pairs, not to list them.

Constraints:
- 1 <= fills.length <= 2 * 10^5
- 0 <= fills[i] <= 10^9
- 0 <= k <= 10^9
- The answer may exceed 32-bit integer range, so use a 64-bit integer type where needed.

Example 1:
fills = [10, 13, 15, 19], k = 3
Sorted fills = [10, 13, 15, 19]

Pair differences:
- |10 - 13| = 3  -> valid
- |10 - 15| = 5  -> invalid
- |10 - 19| = 9  -> invalid
- |13 - 15| = 2  -> valid
- |13 - 19| = 6  -> invalid
- |15 - 19| = 4  -> invalid

Output: 2

Example 2:
fills = [7, 7, 8, 12, 13], k = 1
Valid pairs:
- (7 at index 0, 7 at index 1)
- (7 at index 0, 8)
- (7 at index 1, 8)
- (12, 13)

Output: 4
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the array takes O(n log n)
    - The two-pointer scan takes O(n) because each pointer only moves forward
    - Total: O(n log n)

    Space Complexity:
    - If we sort the input array in place, the extra working space used by the algorithm itself is O(1)
      not counting the internal implementation details of Array.Sort
    */
    public long CountCompatiblePairs(int[] fills, int k)
    {
        // Step 1:
        // Sort the array so that values are in non-decreasing order.
        //
        // Why this is necessary:
        // The condition we care about is:
        //     |fills[i] - fills[j]| <= k
        //
        // After sorting, for any pair where left < right, we know:
        //     fills[right] >= fills[left]
        //
        // That means the absolute difference becomes simply:
        //     fills[right] - fills[left]
        //
        // This is much easier to work with than checking all unordered pairs in the original order.
        // Sorting also allows us to use the two-pointer technique efficiently.
        Array.Sort(fills);

        // This variable stores the final answer.
        //
        // We use long instead of int because the number of pairs can be very large.
        // For example, if every pair is valid and n = 200,000, then the number of pairs is:
        //     n * (n - 1) / 2
        // which is about 20 billion, larger than the 32-bit int range.
        long totalPairs = 0;

        // We will use a sliding window / two-pointer approach.
        //
        // Idea:
        // For each right pointer position, we want to know the smallest left pointer position
        // such that every element from left to right forms a valid pair with fills[right].
        //
        // Because the array is sorted:
        // - If fills[right] - fills[left] <= k, then every index between left and right also works
        //   with fills[right], since those values are between fills[left] and fills[right].
        //
        // So once we find the valid window [left ... right], the number of new valid pairs ending at right is:
        //     right - left
        //
        // Why?
        // Because fills[right] can pair with:
        //     fills[left], fills[left + 1], ..., fills[right - 1]
        // which is exactly (right - left) elements.
        int left = 0;

        // Move the right pointer from left to right across the array.
        for (int right = 0; right < fills.Length; right++)
        {
            // Step 2:
            // Shrink the window from the left while the current window is invalid.
            //
            // The window is invalid when:
            //     fills[right] - fills[left] > k
            //
            // Since the array is sorted, fills[left] is the smallest value in the current window.
            // If even the smallest value is too far from fills[right], then that left index cannot
            // participate in any valid pair with this right index.
            //
            // So we move left forward until the difference becomes <= k.
            while (fills[right] - fills[left] > k)
            {
                left++;
            }

            // Step 3:
            // At this point, the window [left ... right] is the largest valid window ending at right.
            //
            // That means:
            // - fills[right] - fills[left] <= k
            // - and for every index x where left <= x < right,
            //   fills[right] - fills[x] <= k as well
            //
            // Therefore, fills[right] forms a valid pair with every earlier element in this window.
            //
            // Number of such earlier elements:
            //     right - left
            //
            // We add that count to the answer.
            totalPairs += right - left;
        }

        // Step 4:
        // Return the total number of valid pairs found.
        return totalPairs;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] fills1 = { 10, 13, 15, 19 };
int k1 = 3;
long result1 = solution.CountCompatiblePairs((int[])fills1.Clone(), k1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 2

// Example 2
int[] fills2 = { 7, 7, 8, 12, 13 };
int k2 = 1;
long result2 = solution.CountCompatiblePairs((int[])fills2.Clone(), k2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 4

// Additional demo 1: all equal values
int[] fills3 = { 5, 5, 5, 5 };
int k3 = 0;
long result3 = solution.CountCompatiblePairs((int[])fills3.Clone(), k3);
Console.WriteLine("Additional Demo 1 Result: " + result3); // Expected: 6

// Additional demo 2: no valid pairs
int[] fills4 = { 1, 10, 20, 30 };
int k4 = 2;
long result4 = solution.CountCompatiblePairs((int[])fills4.Clone(), k4);
Console.WriteLine("Additional Demo 2 Result: " + result4); // Expected: 0

// Additional demo 3: mixed values
int[] fills5 = { 4, 1, 6, 2, 3 };
int k5 = 2;
long result5 = solution.CountCompatiblePairs((int[])fills5.Clone(), k5);
Console.WriteLine("Additional Demo 3 Result: " + result5); // Expected: 6