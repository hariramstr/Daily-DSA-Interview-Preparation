/*
Title: Minimum Reading Light Radius for Library Tables
Difficulty: Medium
Topic: Binary Search

Problem Description:
A long library hallway is modeled as a number line. Some positions contain reading lights, and some positions contain study tables. Each light illuminates every table within distance r from its position, where r is the same for all lights. You are given two integer arrays: lights and tables, representing their positions along the hallway. Your task is to find the minimum integer radius r such that every table is illuminated by at least one light.

A table at position t is illuminated if there exists a light at position l with |l - t| <= r.

Return the smallest possible radius.

This problem is intended to be solved efficiently for large inputs. A brute-force comparison of every table with every light will be too slow. Think about how sorted positions and binary search can help determine whether a given radius is sufficient, or how to directly find the nearest light for each table.

Constraints:
- 1 <= lights.length, tables.length <= 2 * 10^5
- -10^9 <= lights[i], tables[i] <= 10^9
- Positions are not guaranteed to be distinct
- The answer fits in a 32-bit signed integer

Example 1:
Input: lights = [2, 10], tables = [1, 5, 11]
Output: 3
Explanation: With radius 3, the light at 2 covers table 1 and 5, and the light at 10 covers table 11. Radius 2 is not enough because table 5 would be too far from both lights.

Example 2:
Input: lights = [-4, 0, 8], tables = [-7, -1, 3, 10]
Output: 3
Explanation: Table -7 is 3 units from light -4, table -1 is 1 unit from light 0, table 3 is 3 units from light 0, and table 10 is 2 units from light 8. Therefore, radius 3 is sufficient and minimal.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the lights array takes O(m log m), where m = lights.Length
    - For each table, we perform a binary search on the sorted lights array, which takes O(log m)
    - Doing that for all tables takes O(n log m), where n = tables.Length
    - Total: O(m log m + n log m)

    Space Complexity:
    - O(1) extra space if we ignore the sorting implementation details used by the runtime
    - We sort the lights array in place and only use a few variables
    */
    public int FindMinimumRadius(int[] lights, int[] tables)
    {
        // Step 1:
        // Sort the light positions.
        //
        // Why do we do this?
        // Because binary search only works on sorted data.
        // Once the lights are sorted, for any table position we can quickly find:
        // - the first light that is at or to the right of the table
        // - and then also check the light just to the left
        //
        // Those two lights are the only candidates that can be the nearest light.
        // We do NOT need to compare against every light.
        Array.Sort(lights);

        // This variable will store the final answer.
        //
        // Interpretation:
        // For each table, we compute the distance to its nearest light.
        // The minimum radius that covers ALL tables must be at least the largest
        // of those nearest distances.
        //
        // So the answer is:
        // max over all tables of (distance to nearest light)
        int requiredRadius = 0;

        // Step 2:
        // Process each table one by one.
        //
        // For every table, we want to know:
        // "How far is the nearest light?"
        //
        // Once we know that distance, we update the global answer if needed.
        foreach (int table in tables)
        {
            // Step 3:
            // Use binary search to find the insertion position of this table
            // in the sorted lights array.
            //
            // Array.BinarySearch behavior:
            // - If it finds the exact value, it returns the index (>= 0)
            // - If it does not find the value, it returns a negative number
            //   that encodes the insertion index using bitwise complement:
            //   insertionIndex = ~result
            //
            // The insertion index tells us where this table would go to keep
            // the array sorted.
            int index = Array.BinarySearch(lights, table);

            // If index >= 0, that means there is already a light exactly at the table position.
            // In that case, the nearest distance is 0, so this table is already covered
            // even with radius 0. No need to update requiredRadius.
            if (index >= 0)
            {
                continue;
            }

            // Step 4:
            // Decode the insertion position.
            //
            // After this:
            // - rightIndex is the index of the first light strictly greater than the table
            //   (or equal, but exact matches were already handled above)
            // - leftIndex is the light immediately before that
            int rightIndex = ~index;
            int leftIndex = rightIndex - 1;

            // We will compute the distance to the nearest candidate light.
            //
            // Start with a very large value so that taking minimum works correctly.
            int nearestDistance = int.MaxValue;

            // Step 5:
            // If there is a light on the left side, compute its distance to the table.
            //
            // Why check left?
            // Because the nearest light might be just before the table.
            if (leftIndex >= 0)
            {
                // Use long during subtraction to be extra safe with integer ranges,
                // then cast back to int because the problem guarantees the answer fits in int.
                int leftDistance = (int)Math.Abs((long)table - lights[leftIndex]);
                nearestDistance = Math.Min(nearestDistance, leftDistance);
            }

            // Step 6:
            // If there is a light on the right side, compute its distance too.
            //
            // Why check right?
            // Because the nearest light might be just after the table.
            if (rightIndex < lights.Length)
            {
                int rightDistance = (int)Math.Abs((long)lights[rightIndex] - table);
                nearestDistance = Math.Min(nearestDistance, rightDistance);
            }

            // Step 7:
            // Update the global answer.
            //
            // Why take the maximum here?
            // Because one single radius must work for ALL tables.
            // If a particular table needs distance 3 to reach its nearest light,
            // then the final radius must be at least 3.
            //
            // So across all tables, we keep the largest nearest-light distance.
            requiredRadius = Math.Max(requiredRadius, nearestDistance);
        }

        // Step 8:
        // Return the smallest radius that can illuminate every table.
        return requiredRadius;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// lights = [2, 10], tables = [1, 5, 11]
// Nearest distances:
// table 1  -> light 2  => 1
// table 5  -> min(|5-2|=3, |10-5|=5) => 3
// table 11 -> light 10 => 1
// Maximum of [1, 3, 1] is 3
int[] lights1 = { 2, 10 };
int[] tables1 = { 1, 5, 11 };
int result1 = solution.FindMinimumRadius(lights1, tables1);
Console.WriteLine(result1); // Expected: 3

// Example 2:
// lights = [-4, 0, 8], tables = [-7, -1, 3, 10]
// Nearest distances:
// table -7 -> light -4 => 3
// table -1 -> light 0  => 1
// table 3  -> min(|3-0|=3, |8-3|=5) => 3
// table 10 -> light 8  => 2
// Maximum of [3, 1, 3, 2] is 3
int[] lights2 = { -4, 0, 8 };
int[] tables2 = { -7, -1, 3, 10 };
int result2 = solution.FindMinimumRadius(lights2, tables2);
Console.WriteLine(result2); // Expected: 3

// Additional quick demo
int[] lights3 = { 5 };
int[] tables3 = { 5, 6, 7, 8 };
int result3 = solution.FindMinimumRadius(lights3, tables3);
Console.WriteLine(result3); // Expected: 3