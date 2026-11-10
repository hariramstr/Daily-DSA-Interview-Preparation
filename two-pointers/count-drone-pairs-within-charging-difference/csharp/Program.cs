/*
Title: Count Drone Pairs Within Charging Difference

Problem Description:
A robotics team maintains a sorted list of battery charge levels for idle delivery drones.
To form a temporary relay pair, two drones can be matched only if the absolute difference
between their charge levels is at most maxDiff.

Given an integer array charge sorted in non-decreasing order, return the number of index
pairs (i, j) such that 0 <= i < j < charge.length and charge[j] - charge[i] <= maxDiff.

Because the number of valid pairs can be large, the algorithm must be efficient for large inputs.
A brute-force O(n^2) solution is too slow under the full constraints.

Constraints:
- 1 <= charge.length <= 2 * 10^5
- 0 <= charge[i] <= 10^9
- charge is sorted in non-decreasing order
- 0 <= maxDiff <= 10^9
- The answer may exceed 32-bit integer range, so use a 64-bit integer type if needed

Example 1:
Input: charge = [10, 12, 13, 17], maxDiff = 3
Output: 3
Explanation: Valid pairs are (10,12), (10,13), and (12,13). Pairs involving 17 differ by more than 3.

Example 2:
Input: charge = [4, 4, 5, 8, 9], maxDiff = 1
Output: 4
Explanation: Valid pairs are the two drones with charge 4, both pairs (4,5), and (8,9).
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Why O(n)?
    - We use the two-pointer / sliding window technique.
    - The right pointer only moves forward from left to right.
    - The left pointer also moves forward from left to right.
    - Since each pointer advances at most n times total, the full algorithm is linear.

    Why O(1) extra space?
    - We only store a few variables such as pointers and the running answer.
    - We do not allocate any extra data structures proportional to input size.
    */
    public long CountPairsWithinDiff(int[] charge, int maxDiff)
    {
        // This variable will store the total number of valid pairs.
        // We use long instead of int because the number of pairs can be very large.
        // For example, if all elements are valid with each other and n = 200,000,
        // the number of pairs is n * (n - 1) / 2, which does not fit in a 32-bit int.
        long totalPairs = 0;

        // "right" is the end of our current valid window.
        // For each "left" index, we want to find the farthest "right" index such that:
        // charge[right] - charge[left] <= maxDiff
        //
        // Because the array is already sorted, once a value becomes too large for a given left,
        // any later value will also be too large. This sorted property is exactly what makes
        // the two-pointer approach efficient.
        int right = 0;

        // We iterate "left" from the beginning of the array to the end.
        // At each step, we count how many indices j > left can pair with left.
        for (int left = 0; left < charge.Length; left++)
        {
            // Important maintenance step:
            // Ensure right is never behind left.
            //
            // Why is this necessary?
            // - As left moves forward, right might still point to an earlier position.
            // - A valid pair requires i < j, so the window should start at least at left.
            // - Setting right = left when needed keeps the window well-formed.
            if (right < left)
            {
                right = left;
            }

            // Expand the right pointer as far as possible while the pair difference
            // remains within maxDiff.
            //
            // We check charge[right] - charge[left] <= maxDiff.
            // Since the array is sorted, all elements from left through right - 1
            // will also satisfy the condition once right stops.
            //
            // Note:
            // We advance right while it is valid.
            // When the loop ends, right is the first index that is invalid
            // OR right == charge.Length.
            while (right < charge.Length && (long)charge[right] - charge[left] <= maxDiff)
            {
                right++;
            }

            // At this point:
            // - The valid indices for pairing with "left" are from left + 1 up to right - 1.
            // - The count of indices in [left, right - 1] is (right - left).
            // - But index left itself cannot pair with itself.
            // Therefore, the number of valid pairs contributed by this left index is:
            // (right - left - 1)
            //
            // Example:
            // If left = 1 and right = 4, then valid indices are 1, 2, 3 in the window.
            // Excluding index 1 itself leaves indices 2 and 3 => 2 pairs.
            totalPairs += right - left - 1;
        }

        // After processing every left index, totalPairs contains the full answer.
        return totalPairs;
    }
}

// Demo code:
// We create sample inputs from the problem statement,
// call the solution method, and print the results.

var solution = new Solution();

// Example 1
int[] charge1 = { 10, 12, 13, 17 };
int maxDiff1 = 3;
long result1 = solution.CountPairsWithinDiff(charge1, maxDiff1);
Console.WriteLine(result1); // Expected: 3

// Quick manual trace for Example 1:
// left=0 (10): valid with 12,13 => 2
// left=1 (12): valid with 13 => 1
// left=2 (13): valid with none => 0
// left=3 (17): valid with none => 0
// total = 3

// Example 2
int[] charge2 = { 4, 4, 5, 8, 9 };
int maxDiff2 = 1;
long result2 = solution.CountPairsWithinDiff(charge2, maxDiff2);
Console.WriteLine(result2); // Expected: 4

// Quick manual trace for Example 2:
// left=0 (4): valid with index 1 (4), index 2 (5) => 2
// left=1 (4): valid with index 2 (5) => 1
// left=2 (5): valid with none => 0
// left=3 (8): valid with index 4 (9) => 1
// left=4 (9): valid with none => 0
// total = 4

// Additional small sanity check
int[] charge3 = { 1, 1, 1, 1 };
int maxDiff3 = 0;
long result3 = solution.CountPairsWithinDiff(charge3, maxDiff3);
Console.WriteLine(result3); // Expected: 6

// Explanation for sanity check:
// All values are equal, so every pair is valid.
// Number of pairs = 4 * 3 / 2 = 6