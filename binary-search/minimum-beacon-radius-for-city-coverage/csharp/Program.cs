/*
Title: Minimum Beacon Radius for City Coverage
Difficulty: Medium
Topic: Binary Search

Problem Description:
A city has several fixed beacon towers placed along a straight highway. Each tower can broadcast a signal equally in both directions, and all towers must use the same broadcast radius R. You are given two integer arrays: houses, where houses[i] is the position of the i-th house on the highway, and beacons, where beacons[j] is the position of the j-th beacon tower. A house is considered covered if there exists at least one beacon whose distance to that house is at most R.

Your task is to return the minimum integer radius R such that every house is covered.

The input arrays are not guaranteed to be sorted. Positions may be negative, zero, or positive. Multiple houses or beacons may share the same position. You should design an efficient solution that works for large inputs.

A common approach is to sort the beacon positions and, for each house, determine the nearest beacon using binary search. The answer is the maximum among these nearest distances. Equivalent binary-search-on-answer solutions are also acceptable if implemented efficiently.

Constraints:
- 1 <= houses.length <= 200000
- 1 <= beacons.length <= 200000
- -10^9 <= houses[i], beacons[j] <= 10^9
- The result fits in a 32-bit signed integer

Example 1:
Input: houses = [1, 5, 9], beacons = [2, 8]
Output: 3
Explanation: House 1 is covered by beacon 2 with radius 1. House 5 is distance 3 from beacon 2 and distance 3 from beacon 8. House 9 is distance 1 from beacon 8. The smallest radius that covers all houses is 3.

Example 2:
Input: houses = [-4, 0, 7, 15], beacons = [-2, 10]
Output: 5
Explanation: Distances to the nearest beacon are 2, 2, 3, and 5 respectively. Therefore, the minimum radius required is 5.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the beacon array takes O(m log m), where m = beacons.Length
    - For each house, we perform one binary search on the sorted beacon array, which takes O(log m)
    - Doing that for all houses takes O(n log m), where n = houses.Length
    - Total: O(m log m + n log m)

    Space Complexity:
    - O(1) extra space beyond the sorting implementation details
    - We sort the beacon array in place
    */
    public int FindMinimumRadius(int[] houses, int[] beacons)
    {
        // Step 1:
        // Sort the beacon positions.
        //
        // Why do we do this?
        // Binary search only works on sorted data.
        // Once the beacons are sorted, for any house position we can quickly find:
        // - the first beacon that is >= house
        // - and therefore also the beacon just before it
        //
        // Those two beacons are the only candidates that can be the nearest beacon.
        Array.Sort(beacons);

        // This variable will store the final answer.
        //
        // Meaning:
        // For every house, we compute the distance to its nearest beacon.
        // The minimum radius that covers ALL houses must be at least the largest
        // of those nearest distances.
        int requiredRadius = 0;

        // Step 2:
        // Process each house independently.
        //
        // For each house:
        // - find the nearest beacon
        // - compute the distance to that nearest beacon
        // - update the global maximum distance
        foreach (int house in houses)
        {
            // Step 2a:
            // Use binary search to find the insertion position of the house
            // in the sorted beacon array.
            //
            // Array.BinarySearch behavior:
            // - If the exact value exists, it returns its index (>= 0)
            // - If it does not exist, it returns a negative number.
            //   The insertion index can be recovered by applying bitwise complement: ~index
            //
            // Example:
            // beacons = [2, 8], house = 5
            // 5 is not found
            // insertion index is 1, because 5 would be inserted before 8
            int index = Array.BinarySearch(beacons, house);

            // If the house is exactly at a beacon position, distance is 0.
            // That means this house is already covered even with radius 0.
            if (index >= 0)
            {
                // Since distance is 0, requiredRadius does not need to increase.
                continue;
            }

            // Recover the insertion position.
            //
            // After this:
            // - rightIndex is the first beacon position greater than the house
            // - leftIndex is the beacon immediately before that
            int rightIndex = ~index;
            int leftIndex = rightIndex - 1;

            // We will compute the nearest distance from this house to either:
            // - the closest beacon on the left
            // - the closest beacon on the right
            //
            // Start with a very large value so any real distance will be smaller.
            int nearestDistance = int.MaxValue;

            // Step 2b:
            // Check the left candidate beacon, if it exists.
            //
            // Why only the immediate left beacon?
            // Because in a sorted array, any beacon further left would be even farther away.
            if (leftIndex >= 0)
            {
                // Use long during subtraction to avoid any risk of overflow in intermediate math,
                // even though the final answer is guaranteed to fit in int.
                long distanceToLeft = Math.Abs((long)house - beacons[leftIndex]);

                if (distanceToLeft < nearestDistance)
                {
                    nearestDistance = (int)distanceToLeft;
                }
            }

            // Step 2c:
            // Check the right candidate beacon, if it exists.
            //
            // Why only the immediate right beacon?
            // Because any beacon further right would be even farther away.
            if (rightIndex < beacons.Length)
            {
                long distanceToRight = Math.Abs((long)beacons[rightIndex] - house);

                if (distanceToRight < nearestDistance)
                {
                    nearestDistance = (int)distanceToRight;
                }
            }

            // Step 2d:
            // Update the answer with the worst-case house seen so far.
            //
            // Why maximum?
            // Suppose one house needs radius 2, another needs radius 5.
            // Then a global radius of 2 is not enough for the second house.
            // So the final radius must be the maximum nearest distance over all houses.
            if (nearestDistance > requiredRadius)
            {
                requiredRadius = nearestDistance;
            }
        }

        // Step 3:
        // Return the smallest radius that covers every house.
        return requiredRadius;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// houses = [1, 5, 9], beacons = [2, 8]
// Distances to nearest beacon:
// house 1 -> 1
// house 5 -> 3
// house 9 -> 1
// answer = max(1, 3, 1) = 3
int[] houses1 = { 1, 5, 9 };
int[] beacons1 = { 2, 8 };
int result1 = solution.FindMinimumRadius(houses1, beacons1);
Console.WriteLine(result1);

// Example 2:
// houses = [-4, 0, 7, 15], beacons = [-2, 10]
// Distances to nearest beacon:
// house -4 -> 2
// house 0  -> 2
// house 7  -> 3
// house 15 -> 5
// answer = max(2, 2, 3, 5) = 5
int[] houses2 = { -4, 0, 7, 15 };
int[] beacons2 = { -2, 10 };
int result2 = solution.FindMinimumRadius(houses2, beacons2);
Console.WriteLine(result2);