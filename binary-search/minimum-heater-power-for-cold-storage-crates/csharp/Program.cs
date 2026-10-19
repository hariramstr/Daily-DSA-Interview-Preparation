/*
Title: Minimum Heater Power for Cold Storage Crates
Difficulty: Medium
Topic: Binary Search

Problem Description:
A warehouse stores temperature-sensitive crates along a straight hallway. The position of each crate and each portable heater is given as an integer coordinate on the same number line. Every heater uses the same power setting, and a heater with power r can keep all crates within distance r from its position safe.

You are given two integer arrays, crates and heaters, where crates[i] is the position of the i-th crate and heaters[j] is the position of the j-th heater. Your task is to find the minimum integer power r such that every crate is covered by at least one heater.

A crate at position x is covered if there exists some heater at position h with |x - h| <= r.

The answer must be the smallest possible integer r. The input arrays are not guaranteed to be sorted. A typical efficient solution sorts the heater positions and, for each crate, uses binary search to find the nearest heater on the left and right.

Constraints:
- 1 <= crates.length, heaters.length <= 2 * 10^5
- -10^9 <= crates[i], heaters[j] <= 10^9
- Positions may contain duplicates
- The expected solution should run in O((n + m) log m) or better after sorting

Example 1:
Input: crates = [2, 8, 14], heaters = [4, 10]
Output: 4
Explanation: Crate 2 is 2 units from heater 4, crate 8 is 2 units from heater 10 or 4 units from heater 4, and crate 14 is 4 units from heater 10. So power 4 is enough, and no smaller power covers crate 14.

Example 2:
Input: crates = [1, 5, 9, 15], heaters = [6]
Output: 9
Explanation: With only one heater at position 6, the farthest crate is at position 15, which is 9 units away. Therefore the minimum required power is 9.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting heaters takes O(m log m), where m = heaters.Length
    - For each crate, we perform one binary search on the heaters array, which takes O(log m)
    - Doing that for all n crates takes O(n log m)
    - Total: O(m log m + n log m)

    Space Complexity:
    - O(1) extra space beyond the sorting implementation details
    - We sort the heaters array in place
    */
    public int FindMinimumHeaterPower(int[] crates, int[] heaters)
    {
        // Step 1:
        // Sort the heater positions.
        //
        // Why do we do this?
        // Binary search only works on sorted data.
        // Once heaters are sorted, for any crate position we can quickly find:
        // - the first heater that is at or to the right of the crate
        // - the heater immediately to the left of that position
        //
        // Those two heaters are the only candidates that can be the nearest heater.
        Array.Sort(heaters);

        // This variable will store the final answer.
        //
        // Important idea:
        // For each crate, we compute the distance to its nearest heater.
        // The heater power must be large enough to cover EVERY crate.
        // Therefore, the minimum valid global power is:
        // maximum over all crates of (distance to nearest heater)
        long requiredPower = 0;

        // Step 2:
        // Process each crate independently.
        //
        // For every crate, we want the distance to the closest heater.
        // Then we update the global answer with the maximum such distance.
        foreach (int crate in crates)
        {
            // Step 2a:
            // Use binary search to find the insertion position of the crate in the sorted heaters array.
            //
            // Array.BinarySearch behavior:
            // - If exact value is found, it returns a non-negative index.
            // - If not found, it returns a negative number.
            //   The insertion index can be recovered by applying bitwise complement: ~index
            //
            // The insertion index is the first heater position that is >= crate.
            int index = Array.BinarySearch(heaters, crate);

            // If the crate position exactly matches a heater position,
            // then this crate needs distance 0 from a heater.
            //
            // That means it is already covered even with power 0.
            if (index >= 0)
            {
                // Since nearest distance is 0, it cannot increase requiredPower.
                continue;
            }

            // Recover the insertion point.
            //
            // After this:
            // - rightIndex is the first heater with position > crate (or >= crate if exact existed, but exact case already handled)
            // - leftIndex is the heater immediately before rightIndex
            int rightIndex = ~index;
            int leftIndex = rightIndex - 1;

            // Step 2b:
            // Compute distance to the nearest heater on the left, if it exists.
            //
            // If there is no left heater, we treat the distance as "infinite"
            // so it will never be chosen as the minimum.
            long leftDistance = long.MaxValue;
            if (leftIndex >= 0)
            {
                // Cast to long before subtraction to be completely safe,
                // even though int range here would still fit.
                leftDistance = Math.Abs((long)crate - heaters[leftIndex]);
            }

            // Step 2c:
            // Compute distance to the nearest heater on the right, if it exists.
            //
            // If there is no right heater, we again treat it as "infinite".
            long rightDistance = long.MaxValue;
            if (rightIndex < heaters.Length)
            {
                rightDistance = Math.Abs((long)heaters[rightIndex] - crate);
            }

            // Step 2d:
            // The nearest heater for this crate must be either:
            // - the closest heater on the left
            // - or the closest heater on the right
            //
            // Because the array is sorted, no other heater can be closer than these two neighbors.
            long nearestHeaterDistance = Math.Min(leftDistance, rightDistance);

            // Step 2e:
            // Update the global required power.
            //
            // Why maximum?
            // Suppose one crate needs distance 2, another needs distance 4.
            // A power of 2 is not enough for the second crate.
            // So the final power must be at least the largest nearest-heater distance among all crates.
            requiredPower = Math.Max(requiredPower, nearestHeaterDistance);
        }

        // The problem asks for the minimum integer power.
        // Our computed value is exactly that.
        return (int)requiredPower;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// crates = [2, 8, 14], heaters = [4, 10]
// Expected output: 4
int[] crates1 = { 2, 8, 14 };
int[] heaters1 = { 4, 10 };
int result1 = solution.FindMinimumHeaterPower(crates1, heaters1);
Console.WriteLine(result1);

// Example 2:
// crates = [1, 5, 9, 15], heaters = [6]
// Expected output: 9
int[] crates2 = { 1, 5, 9, 15 };
int[] heaters2 = { 6 };
int result2 = solution.FindMinimumHeaterPower(crates2, heaters2);
Console.WriteLine(result2);