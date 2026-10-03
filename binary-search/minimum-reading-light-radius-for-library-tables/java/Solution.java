import java.util.*;

/*
 * Title: Minimum Reading Light Radius for Library Tables
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A long library hallway is modeled as a number line. Some positions contain reading lights,
 * and some positions contain study tables. Each light illuminates every table within distance r
 * from its position, where r is the same for all lights. You are given two integer arrays:
 * lights and tables, representing their positions along the hallway. Your task is to find the
 * minimum integer radius r such that every table is illuminated by at least one light.
 *
 * A table at position t is illuminated if there exists a light at position l with |l - t| <= r.
 *
 * Return the smallest possible radius.
 *
 * This problem is intended to be solved efficiently for large inputs. A brute-force comparison
 * of every table with every light will be too slow. Think about how sorted positions and binary
 * search can help determine whether a given radius is sufficient, or how to directly find the
 * nearest light for each table.
 *
 * Constraints:
 * - 1 <= lights.length, tables.length <= 2 * 10^5
 * - -10^9 <= lights[i], tables[i] <= 10^9
 * - Positions are not guaranteed to be distinct
 * - The answer fits in a 32-bit signed integer
 *
 * Example 1:
 * Input: lights = [2, 10], tables = [1, 5, 11]
 * Output: 3
 * Explanation: With radius 3, the light at 2 covers table 1 and 5, and the light at 10 covers
 * table 11. Radius 2 is not enough because table 5 would be too far from both lights.
 *
 * Example 2:
 * Input: lights = [-4, 0, 8], tables = [-7, -1, 3, 10]
 * Output: 3
 * Explanation: Table -7 is 3 units from light -4, table -1 is 1 unit from light 0, table 3 is
 * 3 units from light 0, and table 10 is 2 units from light 8. Therefore, radius 3 is sufficient
 * and minimal.
 */

public class Solution {

    /**
     * Computes the minimum integer radius needed so that every table is illuminated
     * by at least one light.
     *
     * The main idea is:
     * 1. Sort the light positions.
     * 2. For each table, find the nearest light using binary search.
     * 3. The distance from that table to its nearest light is the minimum radius needed
     *    for that specific table.
     * 4. The final answer is the maximum of those minimum distances across all tables.
     *
     * Why this works:
     * - Every table must be covered.
     * - For a single table, the best possible light is simply the nearest one.
     * - Therefore, the smallest global radius must be large enough to cover the
     *   "hardest" table, meaning the table whose nearest light is farthest away.
     *
     * @param lights the positions of reading lights along the hallway
     * @param tables the positions of study tables along the hallway
     * @return the smallest integer radius that illuminates every table
     * Time complexity: O(L log L + T log L), where L = lights.length and T = tables.length
     * Space complexity: O(1) extra space beyond the sorting implementation details
     */
    public int findMinimumRadius(int[] lights, int[] tables) {
        // We sort the lights so that binary search can be used efficiently.
        Arrays.sort(lights);

        // This variable will store the final answer.
        // It represents the largest "nearest-light distance" among all tables.
        int requiredRadius = 0;

        // Process each table independently.
        for (int table : tables) {
            // For the current table, compute the distance to its nearest light.
            int nearestDistance = distanceToNearestLight(lights, table);

            // The global radius must be at least this large.
            // We keep the maximum over all tables.
            requiredRadius = Math.max(requiredRadius, nearestDistance);
        }

        return requiredRadius;
    }

    /**
     * Finds the distance from a given table to its nearest light using binary search
     * on the sorted lights array.
     *
     * Detailed idea:
     * - We locate the insertion position of the table in the sorted lights array.
     * - The nearest light can only be:
     *   1. the first light greater than or equal to the table, or
     *   2. the light immediately before that position.
     * - We compare both distances and return the smaller one.
     *
     * Example:
     * lights = [2, 10], table = 5
     * insertion point = 1
     * candidate lights are:
     * - lights[1] = 10 => distance 5
     * - lights[0] = 2  => distance 3
     * nearest distance = 3
     *
     * @param lights sorted array of light positions
     * @param table the position of the current table
     * @return the minimum distance from this table to any light
     * Time complexity: O(log L), where L = lights.length
     * Space complexity: O(1)
     */
    public int distanceToNearestLight(int[] lights, int table) {
        // Find the first index i such that lights[i] >= table.
        int index = lowerBound(lights, table);

        // We use long during distance calculations to avoid any risk of overflow
        // when subtracting values near the integer limits.
        long bestDistance = Long.MAX_VALUE;

        // Candidate 1:
        // If index is within bounds, then lights[index] is the first light
        // that is greater than or equal to the table.
        if (index < lights.length) {
            bestDistance = Math.min(bestDistance, Math.abs((long) lights[index] - table));
        }

        // Candidate 2:
        // If index - 1 is valid, then lights[index - 1] is the largest light
        // that is strictly less than the table.
        if (index > 0) {
            bestDistance = Math.min(bestDistance, Math.abs((long) lights[index - 1] - table));
        }

        return (int) bestDistance;
    }

    /**
     * Returns the first index at which target could be inserted in the sorted array
     * without violating the sorted order.
     *
     * In other words, this returns the first index i such that array[i] >= target.
     * If no such index exists, it returns array.length.
     *
     * This is the classic "lower bound" binary search.
     *
     * Example:
     * array = [2, 10], target = 5
     * result = 1
     *
     * Example:
     * array = [-4, 0, 8], target = -7
     * result = 0
     *
     * Example:
     * array = [-4, 0, 8], target = 10
     * result = 3
     *
     * @param array a sorted integer array
     * @param target the value to search insertion position for
     * @return the first index where array[index] >= target, or array.length if none exists
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public int lowerBound(int[] array, int target) {
        int left = 0;
        int right = array.length; // right is exclusive

        // Standard binary search on the half-open interval [left, right).
        while (left < right) {
            // Safe midpoint calculation.
            int mid = left + (right - left) / 2;

            // If array[mid] is smaller than target, then lower bound must be to the right.
            if (array[mid] < target) {
                left = mid + 1;
            } else {
                // Otherwise, mid could be the answer, so keep it in the search space.
                right = mid;
            }
        }

        // At loop end, left == right and points to the lower bound position.
        return left;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It prints:
     * - the input arrays
     * - the computed minimum radius
     * - expected values for easy verification
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(L log L + T log L) per demonstration call
     * Space complexity: O(1) extra space beyond sorting implementation details
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] lights1 = {2, 10};
        int[] tables1 = {1, 5, 11};
        int result1 = solution.findMinimumRadius(lights1, tables1);
        System.out.println("Example 1");
        System.out.println("lights = " + Arrays.toString(lights1));
        System.out.println("tables = " + Arrays.toString(tables1));
        System.out.println("Minimum radius = " + result1);
        System.out.println("Expected = 3");
        System.out.println();

        int[] lights2 = {-4, 0, 8};
        int[] tables2 = {-7, -1, 3, 10};
        int result2 = solution.findMinimumRadius(lights2, tables2);
        System.out.println("Example 2");
        System.out.println("lights = " + Arrays.toString(lights2));
        System.out.println("tables = " + Arrays.toString(tables2));
        System.out.println("Minimum radius = " + result2);
        System.out.println("Expected = 3");
        System.out.println();

        // Additional quick sanity check:
        // If a table is exactly at a light position, distance is 0.
        int[] lights3 = {5, 20, 30};
        int[] tables3 = {5, 6, 29};
        int result3 = solution.findMinimumRadius(lights3, tables3);
        System.out.println("Additional Example");
        System.out.println("lights = " + Arrays.toString(lights3));
        System.out.println("tables = " + Arrays.toString(tables3));
        System.out.println("Minimum radius = " + result3);
        System.out.println("Expected = 1");
    }
}