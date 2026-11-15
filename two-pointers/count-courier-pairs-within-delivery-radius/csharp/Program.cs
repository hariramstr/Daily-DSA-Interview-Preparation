/*
Title: Count Courier Pairs Within Delivery Radius

Problem Description:
A logistics company stores the home coordinates of its couriers along a straight road.
You are given an integer array positions, where positions[i] is the location of the ith courier on that road,
and an integer radius.

Two couriers can be assigned to the same local dispatch zone if the absolute difference between their positions
is less than or equal to radius.

Your task is to return the total number of distinct courier pairs (i, j) such that:
- i < j
- |positions[i] - positions[j]| <= radius

The input array is not guaranteed to be sorted. Because the number of couriers can be large,
an O(n^2) solution may time out. Design an efficient algorithm using sorting and the two-pointers technique.

Constraints:
- 1 <= positions.length <= 200000
- -10^9 <= positions[i] <= 10^9
- 0 <= radius <= 10^9
- Return the answer as a 64-bit integer if your language requires it, since the number of valid pairs may exceed 32-bit range.

Example 1:
Input: positions = [4, 1, 7, 3, 9], radius = 3
Output: 4
Explanation:
After sorting, positions = [1, 3, 4, 7, 9].
Valid pairs are:
(1,3), (1,4), (3,4), and (7,9)

Example 2:
Input: positions = [10, 10, 10, 15], radius = 0
Output: 3
Explanation:
Only couriers at the exact same location can form a valid pair.
The three couriers at position 10 create 3 pairs.
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
    - If we sort the input array in place, the extra space used by the algorithm itself is O(1)
      (ignoring any internal implementation details of the sorting routine)
    */
    public long CountCourierPairsWithinRadius(int[] positions, int radius)
    {
        // Step 1:
        // We sort the positions array so that nearby values become adjacent.
        //
        // Why is this necessary?
        // The condition we care about is:
        //     |positions[i] - positions[j]| <= radius
        //
        // If the array is sorted, then for any pair where left < right:
        //     positions[right] - positions[left]
        // is already non-negative, so we no longer need absolute value.
        //
        // This sorted order is what makes the two-pointers technique possible.
        // Without sorting, we would have no efficient way to expand/shrink a valid range.
        Array.Sort(positions);

        // This variable stores the final answer.
        // We use long because the number of valid pairs can be very large.
        //
        // Example:
        // If all 200,000 couriers are within the radius of each other,
        // the number of pairs is n * (n - 1) / 2,
        // which is about 20 billion, too large for int.
        long totalPairs = 0;

        // Step 2:
        // We use a sliding window / two-pointer approach.
        //
        // left  = start of the current valid window
        // right = end of the current valid window
        //
        // At every step, we want the window [left, right] to satisfy:
        //     positions[right] - positions[left] <= radius
        //
        // If it does not, we move left forward until it does.
        int left = 0;

        // Step 3:
        // Move right from left to right across the sorted array.
        //
        // For each right, we determine how many earlier elements can pair with positions[right].
        // Those valid earlier elements will be exactly the indices from left to right - 1.
        for (int right = 0; right < positions.Length; right++)
        {
            // Step 3a:
            // If the current window is too wide, shrink it from the left.
            //
            // Because the array is sorted:
            // - positions[right] is the largest value in the current window
            // - positions[left] is the smallest value in the current window
            //
            // So if:
            //     positions[right] - positions[left] > radius
            // then the pair (left, right) is invalid,
            // and therefore left cannot stay in the valid window for this right.
            //
            // We keep moving left forward until the window becomes valid again.
            while ((long)positions[right] - positions[left] > radius)
            {
                left++;
            }

            // Step 3b:
            // Now the window [left, right] is valid, meaning:
            //     positions[right] - positions[left] <= radius
            //
            // Since the array is sorted, every index k in [left, right - 1] also satisfies:
            //     positions[right] - positions[k] <= radius
            //
            // Therefore, positions[right] can form a valid pair with every element before it
            // inside this window.
            //
            // Number of such elements:
            //     right - left
            //
            // Why not include right itself?
            // Because pairs must have i < j, so an element cannot pair with itself.
            totalPairs += right - left;
        }

        // Step 4:
        // Return the total number of valid pairs found.
        return totalPairs;
    }
}

// Demo code:
// We create sample inputs from the problem statement,
// call the solution method, and print the results.

// Example 1
int[] positions1 = { 4, 1, 7, 3, 9 };
int radius1 = 3;

var solution = new Solution();
long result1 = solution.CountCourierPairsWithinRadius((int[])positions1.Clone(), radius1);
Console.WriteLine(result1); // Expected: 4

// Example 2
int[] positions2 = { 10, 10, 10, 15 };
int radius2 = 0;

long result2 = solution.CountCourierPairsWithinRadius((int[])positions2.Clone(), radius2);
Console.WriteLine(result2); // Expected: 3

// Additional demo to help beginners see another case
int[] positions3 = { -5, -2, 0, 1, 6 };
int radius3 = 2;

long result3 = solution.CountCourierPairsWithinRadius((int[])positions3.Clone(), radius3);
Console.WriteLine(result3); // Valid pairs after sorting [-5,-2,0,1,6]: (-2,0), (0,1) => Expected: 2