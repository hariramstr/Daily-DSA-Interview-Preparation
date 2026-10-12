/*
Title: Count Router Pairs With Coverage Overlap Limit
Difficulty: Medium
Topic: Two Pointers

Problem Description:
You are given a sorted array `positions` where `positions[i]` is the location of the `i`-th Wi-Fi router installed along a straight road. For network planning, two routers are considered a compatible pair if the distance between them is at most `maxGap`. Your task is to count how many index pairs `(i, j)` with `0 <= i < j < n` satisfy `positions[j] - positions[i] <= maxGap`.

Because the router locations are already sorted, a brute-force solution that checks all pairs would be too slow for large inputs. Design an efficient algorithm using the two-pointers technique.

Return the total number of compatible pairs.

Constraints:
- `1 <= positions.length <= 2 * 10^5`
- `0 <= positions[i] <= 10^9`
- `positions` is sorted in non-decreasing order
- `0 <= maxGap <= 10^9`
- The answer may exceed 32-bit integer range, so use a 64-bit integer type when needed.

Example 1:
Input: positions = [1, 3, 4, 9, 10], maxGap = 3
Output: 4
Explanation: Valid pairs are `(1,3)`, `(1,4)`, `(3,4)`, and `(9,10)` by value, corresponding to index pairs `(0,1)`, `(0,2)`, `(1,2)`, and `(3,4)`.

Example 2:
Input: positions = [2, 2, 5, 6, 8], maxGap = 2
Output: 3
Explanation:
- (0,1): 2 - 2 = 0, valid
- (0,2): 5 - 2 = 3, invalid
- (0,3): 6 - 2 = 4, invalid
- (0,4): 8 - 2 = 6, invalid
- (1,2): 5 - 2 = 3, invalid
- (1,3): 6 - 2 = 4, invalid
- (1,4): 8 - 2 = 6, invalid
- (2,3): 6 - 5 = 1, valid
- (2,4): 8 - 5 = 3, invalid
- (3,4): 8 - 6 = 2, valid
Total = 3
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Why O(n)?
    - We use two pointers: `left` and `right`.
    - The `right` pointer moves from left to right exactly once.
    - The `left` pointer also only moves forward, never backward.
    - Because each pointer advances at most n times, the total work is linear.

    Why O(1) extra space?
    - We only use a few variables for counting and pointer movement.
    - We do not allocate any extra arrays or collections proportional to input size.
    */
    public long CountCompatiblePairs(int[] positions, int maxGap)
    {
        // This variable stores the final answer.
        // We use `long` because the number of valid pairs can be very large.
        // For example, if all routers are close enough, the number of pairs is:
        // n * (n - 1) / 2
        // With n = 200,000, this is about 19,999,900,000, which does not fit in int.
        long totalPairs = 0;

        // `left` is the start of the current valid window.
        // The idea of the window:
        // For each `right`, we want the smallest `left` such that
        // positions[right] - positions[left] <= maxGap.
        int left = 0;

        // We expand the window by moving `right` from left to right.
        for (int right = 0; right < positions.Length; right++)
        {
            // At this moment, we are trying to count how many earlier indices `i`
            // can pair with the current index `right`.
            //
            // Because the array is sorted, if the distance from positions[left]
            // to positions[right] is too large, then any even earlier index would
            // also be too far away.
            //
            // So we move `left` forward until the window becomes valid again.
            while (positions[right] - positions[left] > maxGap)
            {
                left++;
            }

            // Now the subarray from `left` to `right` is valid in this sense:
            // positions[right] - positions[left] <= maxGap
            //
            // Since the array is sorted, this means:
            // for every index k in [left, right - 1],
            // positions[right] - positions[k] <= maxGap
            //
            // Therefore, the current `right` index can form valid pairs with
            // exactly these indices:
            // left, left + 1, ..., right - 1
            //
            // How many such indices are there?
            // right - left
            //
            // We add that count to the answer.
            totalPairs += right - left;
        }

        // After processing every index as the right endpoint of a pair,
        // `totalPairs` contains the total number of valid pairs.
        return totalPairs;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] positions1 = { 1, 3, 4, 9, 10 };
int maxGap1 = 3;
long result1 = solution.CountCompatiblePairs(positions1, maxGap1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 4

// Example 2
int[] positions2 = { 2, 2, 5, 6, 8 };
int maxGap2 = 2;
long result2 = solution.CountCompatiblePairs(positions2, maxGap2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 3

// Additional demo: all routers close enough
int[] positions3 = { 1, 2, 3, 4 };
int maxGap3 = 10;
long result3 = solution.CountCompatiblePairs(positions3, maxGap3);
Console.WriteLine("Additional Demo 1 Result: " + result3); // Expected: 6

// Additional demo: no valid pairs
int[] positions4 = { 1, 10, 20, 30 };
int maxGap4 = 5;
long result4 = solution.CountCompatiblePairs(positions4, maxGap4);
Console.WriteLine("Additional Demo 2 Result: " + result4); // Expected: 0