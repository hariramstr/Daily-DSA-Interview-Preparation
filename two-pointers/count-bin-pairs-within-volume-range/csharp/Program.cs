/*
Title: Count Bin Pairs Within Volume Range
Difficulty: Medium
Topic: Two Pointers

Problem Description:
A warehouse stores reusable bins, and each bin has a volume capacity represented by an integer in the array volumes.
You are also given two integers low and high.

A pair of bins (i, j) is considered compatible if:
- i < j
- volumes[i] + volumes[j] is within the inclusive range [low, high]

Your task is to return the total number of compatible pairs.

The input array is not guaranteed to be sorted. Because the warehouse may contain a large number of bins,
an O(n^2) solution may be too slow. Design an algorithm that efficiently counts all valid pairs.

Two bins are distinct if they come from different indices, even if they have the same volume.
Be careful not to double-count pairs. The expected solution should take advantage of sorting
and a two-pointer counting strategy.

Constraints:
- 1 <= volumes.length <= 100000
- 0 <= volumes[i] <= 1000000000
- 0 <= low <= high <= 2000000000
- The answer may not fit in a 32-bit integer, so use a 64-bit integer type where needed.

Example 1:
Input: volumes = [4, 1, 7, 3, 2], low = 5, high = 8
Output: 6

Example 2:
Input: volumes = [2, 2, 2, 2], low = 4, high = 4
Output: 6
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the array takes O(n log n)
    - Each two-pointer counting pass takes O(n)
    - We do the counting pass twice, so total is still O(n log n)

    Space Complexity:
    - O(1) extra space if we ignore the sorting implementation details
    - In practice, Array.Sort may use some internal stack space, but algorithmically this is considered O(1) extra space
    */
    public long CountCompatiblePairs(int[] volumes, int low, int high)
    {
        // Step 1:
        // Sort the array so we can use the two-pointer technique efficiently.
        //
        // Why sorting helps:
        // After sorting, if we know that volumes[left] + volumes[right] is small enough,
        // then every element between left and right can also form valid pairs with left
        // for the same upper-bound condition.
        //
        // Without sorting, we would have no structure to exploit and would likely need
        // to check every pair, which would be too slow for up to 100,000 elements.
        Array.Sort(volumes);

        // Step 2:
        // Instead of directly counting sums in [low, high], we use a very common trick:
        //
        // count(sum <= high) - count(sum <= low - 1)
        //
        // Why this works:
        // - count(sum <= high) includes every pair whose sum is at most high
        // - count(sum <= low - 1) includes every pair whose sum is strictly less than low
        // - subtracting removes the too-small pairs, leaving exactly those in [low, high]
        //
        // We use long for the answer because the number of pairs can be as large as
        // n * (n - 1) / 2, which can exceed the range of int.
        long atMostHigh = CountPairsWithSumAtMost(volumes, high);
        long belowLow = CountPairsWithSumAtMost(volumes, (long)low - 1);

        return atMostHigh - belowLow;
    }

    /*
    Time Complexity:
    - O(n), because each pointer only moves in one direction across the array once

    Space Complexity:
    - O(1) extra space

    This helper counts how many index pairs (i, j), with i < j, have:
    volumes[i] + volumes[j] <= limit
    */
    private long CountPairsWithSumAtMost(int[] volumes, long limit)
    {
        // Step 1:
        // Initialize two pointers:
        // - left starts at the beginning (smallest values)
        // - right starts at the end (largest values)
        //
        // Because the array is sorted, this lets us reason about sums efficiently.
        int left = 0;
        int right = volumes.Length - 1;

        // This will store the total number of valid pairs found.
        long count = 0;

        // Step 2:
        // Continue while left is strictly before right.
        // This ensures we only consider pairs of distinct indices and never pair an element with itself.
        while (left < right)
        {
            // Step 3:
            // Compute the current sum using long to avoid overflow.
            //
            // Even though each volume fits in int, adding two large ints can still be close to the int limit.
            // Using long is safer and clearer.
            long sum = (long)volumes[left] + volumes[right];

            // Step 4:
            // If the current sum is within the allowed upper bound...
            if (sum <= limit)
            {
                // ...then not only is (left, right) valid,
                // but also every pair (left, k) for k in [left + 1, right] is valid.
                //
                // Why?
                // Because the array is sorted:
                // volumes[left] + volumes[k] <= volumes[left] + volumes[right] <= limit
                //
                // So we can count all those pairs at once instead of one by one.
                //
                // Number of such pairs:
                // right - left
                count += right - left;

                // After counting all pairs that start with this left index,
                // we move left forward to explore the next starting value.
                left++;
            }
            else
            {
                // Step 5:
                // If the sum is too large, then pairing volumes[right] with volumes[left]
                // already exceeds the limit.
                //
                // Since the array is sorted, any pair using the same right index and a larger left index
                // would only make the sum even larger.
                //
                // Therefore, the only hope is to reduce the sum by moving right leftward
                // to a smaller value.
                right--;
            }
        }

        // Step 6:
        // Return the total number of pairs whose sum is at most the given limit.
        return count;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] volumes1 = { 4, 1, 7, 3, 2 };
int low1 = 5;
int high1 = 8;
long result1 = solution.CountCompatiblePairs(volumes1, low1, high1);
Console.WriteLine("Example 1 Result: " + result1);

// Example 2
int[] volumes2 = { 2, 2, 2, 2 };
int low2 = 4;
int high2 = 4;
long result2 = solution.CountCompatiblePairs(volumes2, low2, high2);
Console.WriteLine("Example 2 Result: " + result2);

// Additional quick sanity check
int[] volumes3 = { 0, 5, 10, 15 };
int low3 = 10;
int high3 = 15;
long result3 = solution.CountCompatiblePairs(volumes3, low3, high3);
Console.WriteLine("Additional Test Result: " + result3);