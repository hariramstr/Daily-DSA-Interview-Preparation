/*
Title: Count Apartment Pairs Within Noise Difference
Difficulty: Medium
Topic: Two Pointers

Problem Description:
You are given an array `noise` where `noise[i]` represents the measured nighttime noise level of the `i`th apartment in a building.
You are also given an integer `limit`.

Two apartments form a compatible pair if the absolute difference between their noise levels is less than or equal to `limit`.

Your task is to return the total number of distinct compatible pairs `(i, j)` such that:
- `0 <= i < j < n`
- `|noise[i] - noise[j]| <= limit`

Because the input size can be large, an efficient solution is required.
A brute-force approach that checks every pair would be too slow for up to 200,000 apartments.

Key idea:
- Sort the array
- Use a two-pointer / sliding window technique
- Count many valid pairs at once instead of checking every pair individually

Constraints:
- `1 <= noise.length <= 200000`
- `0 <= noise[i] <= 1000000000`
- `0 <= limit <= 1000000000`
- The answer may be large, so return it as a 64-bit integer

Example 1:
Input: noise = [12, 7, 10, 15], limit = 3
Output: 4

Verification:
Original pairs by indices:
(0,1) => |12-7| = 5  -> invalid
(0,2) => |12-10| = 2 -> valid
(0,3) => |12-15| = 3 -> valid
(1,2) => |7-10| = 3  -> valid
(1,3) => |7-15| = 8  -> invalid
(2,3) => |10-15| = 5 -> invalid
So the correct total is 3.

Important note:
The problem statement's listed output says 4, but its own direct pair checking only supports 3 valid pairs.
Therefore, the mathematically correct answer for Example 1 is 3, and the algorithm below returns the correct count.

Example 2:
Input: noise = [4, 4, 4, 9], limit = 0
Output: 3

Verification:
Valid pairs are among the three 4s:
(0,1), (0,2), (1,2) => total 3
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
    - If we sort a copy of the input array, that requires O(n) extra space
    - Aside from that, only a few variables are used
    - Total: O(n)
    */
    public long CountCompatiblePairs(int[] noise, int limit)
    {
        // Step 1:
        // Handle the simplest edge case.
        // If there are fewer than 2 apartments, then no pair can exist.
        // We return 0 immediately.
        if (noise == null || noise.Length < 2)
        {
            return 0L;
        }

        // Step 2:
        // We create a copy of the input array before sorting.
        // Why?
        // - Sorting helps us use the two-pointer technique efficiently.
        // - Copying avoids modifying the caller's original array.
        // This is often a good habit unless the problem explicitly allows in-place changes.
        int[] sorted = new int[noise.Length];
        Array.Copy(noise, sorted, noise.Length);

        // Step 3:
        // Sort the noise levels in non-decreasing order.
        // Why is sorting useful?
        // After sorting, if sorted[right] - sorted[left] <= limit,
        // then every value between left and right is also close enough to sorted[right]
        // in a structured way that we can count many pairs at once.
        Array.Sort(sorted);

        // Step 4:
        // This variable stores the final answer.
        // We use long because the number of pairs can be very large.
        // For example, if all 200,000 apartments are compatible with each other,
        // the number of pairs is n * (n - 1) / 2, which does not fit in int.
        long totalPairs = 0L;

        // Step 5:
        // We use two pointers:
        // - left marks the beginning of the current valid window
        // - right expands the window one apartment at a time
        //
        // The idea:
        // For each right, we want the smallest left such that:
        // sorted[right] - sorted[left] <= limit
        //
        // Then all indices from left to right - 1 form valid pairs with right.
        int left = 0;

        // Step 6:
        // Move right from the first element to the last.
        // Each iteration asks:
        // "How many earlier apartments can pair with apartment at index right?"
        for (int right = 0; right < sorted.Length; right++)
        {
            // Step 6a:
            // Shrink the window from the left while the difference is too large.
            //
            // Why does this work?
            // Because the array is sorted.
            // If sorted[right] - sorted[left] > limit, then left is too far away
            // (too small compared to sorted[right]) to form a valid pair.
            // So we move left forward until the window becomes valid again.
            //
            // Since both pointers only move forward, this loop is efficient overall.
            while (sorted[right] - sorted[left] > limit)
            {
                left++;
            }

            // Step 6b:
            // At this point, the window [left, right] is valid in the sense that:
            // sorted[right] - sorted[left] <= limit
            //
            // Because the array is sorted, every element between left and right
            // is also within limit of sorted[right].
            //
            // Therefore, the apartment at index right can form valid pairs with:
            // left, left+1, ..., right-1
            //
            // Number of such indices = right - left
            //
            // We add that count to the answer.
            totalPairs += (right - left);
        }

        // Step 7:
        // Return the total number of compatible pairs.
        return totalPairs;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print results.

var solution = new Solution();

// Example 1 from the prompt.
// Careful verification shows the correct answer is 3, not 4.
int[] noise1 = { 12, 7, 10, 15 };
int limit1 = 3;
long result1 = solution.CountCompatiblePairs(noise1, limit1);
Console.WriteLine(result1); // Correct result: 3

// Example 2 from the prompt.
int[] noise2 = { 4, 4, 4, 9 };
int limit2 = 0;
long result2 = solution.CountCompatiblePairs(noise2, limit2);
Console.WriteLine(result2); // Expected: 3

// Additional quick sanity check:
// All pairs valid because max difference is within limit.
int[] noise3 = { 1, 2, 3, 4 };
int limit3 = 10;
long result3 = solution.CountCompatiblePairs(noise3, limit3);
Console.WriteLine(result3); // 6 pairs total in 4 elements

// Additional quick sanity check:
// No pairs valid when limit is too small and all values differ.
int[] noise4 = { 1, 5, 9 };
int limit4 = 0;
long result4 = solution.CountCompatiblePairs(noise4, limit4);
Console.WriteLine(result4); // 0