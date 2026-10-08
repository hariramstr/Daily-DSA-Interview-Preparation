/*
Title: Count Warehouse Aisle Pairs Within Walking Distance
Difficulty: Medium
Topic: Two Pointers

Problem Description:
A warehouse stores picking stations along one long aisle. You are given an integer array positions where positions[i] is the location of the i-th station measured in meters from the start of the aisle. You are also given an integer maxDistance.

Two stations form a valid pair if the absolute difference between their positions] is less than or equal to maxDistance. Your task is to return the total number of distinct valid pairs (i, j) such that i < j.

The input array is not guaranteed to be sorted. An efficient solution is expected for large inputs, so a brute-force O(n^2) approach may time out. This problem is intended to test whether you can combine sorting with a two-pointers scanning strategy to count many pairs at once.

Return the number of valid pairs.

Constraints:
- 1 <= positions.length <= 200000
- -10^9 <= positions[i] <= 10^9
- 0 <= maxDistance <= 10^9
- The answer can be as large as n * (n - 1) / 2, so use a 64-bit integer type where needed.

Example 1:
Input: positions = [8, 1, 4, 10, 6], maxDistance = 3
Output: 4
Explanation:
After sorting, positions = [1, 4, 6, 8, 10].
Valid pairs are:
(1,4) -> distance 3
(4,6) -> distance 2
(6,8) -> distance 2
(8,10) -> distance 2
So the answer is 4.

Example 2:
Input: positions = [5, 5, 5, 9], maxDistance = 0
Output: 3
Explanation:
Only stations at exactly the same location can be paired.
The three stations at position 5 produce 3 pairs:
choose any 2 out of the 3 identical positions.
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
    - O(1) extra space beyond the sort implementation details
    - We sort the input array in place
    */
    public long CountValidPairs(int[] positions, int maxDistance)
    {
        // Step 1:
        // Sort the positions array.
        //
        // Why do we sort?
        // Because once the values are in increasing order, the absolute difference
        // between positions[right] and positions[left] for left < right becomes:
        //
        //     positions[right] - positions[left]
        //
        // We no longer need Math.Abs because the right value is always greater than
        // or equal to the left value after sorting.
        //
        // Sorting is the key step that makes the two-pointer technique possible.
        Array.Sort(positions);

        // This variable will store the final number of valid pairs.
        //
        // We use long instead of int because the number of pairs can be very large.
        // For example, if n = 200000, then n * (n - 1) / 2 is much larger than
        // what a 32-bit int can safely hold.
        long totalPairs = 0;

        // We will use a sliding window / two-pointer approach.
        //
        // left  = start of the current valid window
        // right = end of the current valid window
        //
        // For each right, we want to find the smallest left such that:
        //
        //     positions[right] - positions[left] <= maxDistance
        //
        // Once we have that, every index from left to right - 1 forms a valid pair
        // with right, because the array is sorted.
        int left = 0;

        // Step 2:
        // Move the right pointer from left to right across the array.
        //
        // At each step, we treat positions[right] as the second element of the pair.
        for (int right = 0; right < positions.Length; right++)
        {
            // Step 3:
            // Shrink the window from the left while the current distance is too large.
            //
            // If positions[right] - positions[left] > maxDistance,
            // then the pair (left, right) is invalid.
            //
            // Because the array is sorted, if the leftmost element is too far away,
            // then we must move left forward to try a closer element.
            //
            // We keep doing this until the window becomes valid again.
            while (positions[right] - positions[left] > maxDistance)
            {
                left++;
            }

            // Step 4:
            // Count how many valid pairs end at index "right".
            //
            // After the while loop finishes, the window [left ... right] satisfies:
            //
            //     positions[right] - positions[left] <= maxDistance
            //
            // Since the array is sorted, every index between left and right also works:
            //
            //     positions[right] - positions[left]     <= maxDistance
            //     positions[right] - positions[left + 1] <= maxDistance
            //     ...
            //     positions[right] - positions[right - 1] <= maxDistance
            //
            // Therefore, the number of valid pairs with "right" as the second element is:
            //
            //     right - left
            //
            // Why not right - left + 1?
            // Because a pair must use two distinct indices i < j.
            // We do NOT pair right with itself.
            totalPairs += right - left;
        }

        // Step 5:
        // Return the total number of valid pairs found.
        return totalPairs;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] positions1 = { 8, 1, 4, 10, 6 };
int maxDistance1 = 3;
long result1 = solution.CountValidPairs(positions1, maxDistance1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 4

// Example 2
int[] positions2 = { 5, 5, 5, 9 };
int maxDistance2 = 0;
long result2 = solution.CountValidPairs(positions2, maxDistance2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 3

// Additional quick checks

int[] positions3 = { 1 };
int maxDistance3 = 100;
long result3 = solution.CountValidPairs(positions3, maxDistance3);
Console.WriteLine("Single Element Result: " + result3); // Expected: 0

int[] positions4 = { -5, -2, 0, 3, 4 };
int maxDistance4 = 3;
long result4 = solution.CountValidPairs(positions4, maxDistance4);
Console.WriteLine("Additional Test Result: " + result4);