import java.util.*;

/*
Problem Title: Minimum Refill Rate for a Timed Irrigation Plan

Problem Description:
A farm manager needs to water fields in a fixed order using a mobile tank. The tank is refilled to the same amount at the start of each day, and the manager waters consecutive fields from left to right. Each field i requires water[i] liters, and splitting a single field across multiple days is not allowed. If the next field does not fit in the remaining water for the current day, the manager must stop for that day and continue from the next field on the following day with a freshly refilled tank.

Given an array water where water[i] is the amount of water needed for the i-th field, and an integer days, return the minimum tank refill capacity needed so that all fields can be watered within at most days days.

The order of fields cannot be changed. Every day starts with a full tank of the chosen capacity. A capacity is feasible if the full watering plan can be completed in days or fewer.

This problem is designed for an efficient solution using binary search on the answer. The key observation is that if a certain tank capacity works, then any larger capacity will also work.

Constraints:
- 1 <= water.length <= 100000
- 1 <= water[i] <= 1000000000
- 1 <= days <= water.length

Example 1:
Input: water = [7,2,5,10,8], days = 2
Output: 18
Explanation: With capacity 18, day 1 can water fields [7,2,5] and day 2 can water [10,8]. Any capacity smaller than 18 would require more than 2 days.

Example 2:
Input: water = [3,1,4,1,5,9], days = 3
Output: 9
Explanation: One optimal schedule is [3,1,4], [1,5], [9]. Capacity 8 is not enough because the field requiring 9 liters cannot be split across days.
*/

public class Solution {

    /**
     * Computes the minimum tank refill capacity needed to water all fields
     * in order within at most the given number of days.
     *
     * The algorithm uses binary search on the answer:
     * - The minimum possible capacity must be at least the largest single field requirement,
     *   because one field cannot be split across days.
     * - The maximum possible capacity is the sum of all field requirements,
     *   which means everything can be watered in one day.
     * - For each candidate capacity, we greedily simulate how many days are needed.
     *
     * @param water the array where water[i] is the liters needed for the i-th field
     * @param days the maximum number of days allowed to finish watering all fields
     * @return the minimum feasible tank capacity
     *
     * Time complexity: O(n log S), where n is water.length and S is the search range
     * between max(water) and sum(water)
     * Space complexity: O(1), excluding input storage
     */
    public long minimumRefillCapacity(int[] water, int days) {
        // The lower bound of the answer:
        // At minimum, the tank must be able to handle the largest single field,
        // because fields cannot be split across multiple days.
        long left = 0;

        // The upper bound of the answer:
        // If the tank capacity equals the total sum, then all fields can be watered in one day.
        long right = 0;

        // Build the binary search boundaries.
        for (int requirement : water) {
            left = Math.max(left, requirement);
            right += requirement;
        }

        // Binary search for the smallest feasible capacity.
        while (left < right) {
            // Use this form to avoid overflow:
            // mid = left + (right - left) / 2
            long mid = left + (right - left) / 2;

            // Check whether this candidate capacity is enough.
            if (canFinishWithinDays(water, days, mid)) {
                // If mid works, try to find an even smaller feasible capacity.
                right = mid;
            } else {
                // If mid does not work, we must increase the capacity.
                left = mid + 1;
            }
        }

        // When the loop ends, left == right and points to the minimum feasible capacity.
        return left;
    }

    /**
     * Determines whether all fields can be watered within the allowed number of days
     * using the given tank capacity.
     *
     * The simulation is greedy and optimal for this feasibility check:
     * - Start a day with a full tank.
     * - Keep watering consecutive fields while they fit.
     * - As soon as the next field does not fit, start a new day.
     *
     * This greedy strategy minimizes the number of days used for a fixed capacity,
     * because it always packs as many consecutive fields as possible into each day.
     *
     * @param water the array of field water requirements
     * @param days the maximum allowed number of days
     * @param capacity the candidate tank capacity to test
     * @return true if all fields can be watered within at most days days; false otherwise
     *
     * Time complexity: O(n), where n is water.length
     * Space complexity: O(1)
     */
    public boolean canFinishWithinDays(int[] water, int days, long capacity) {
        // We start using the first day immediately.
        int usedDays = 1;

        // This variable tracks how much water has already been used in the current day.
        long currentLoad = 0;

        // Process fields strictly from left to right, as required by the problem.
        for (int requirement : water) {
            // If adding this field would exceed today's tank capacity,
            // we must stop the current day and start a new one.
            if (currentLoad + requirement > capacity) {
                usedDays++;
                currentLoad = 0;

                // Early exit:
                // If we already need more than the allowed number of days,
                // then this capacity is not feasible.
                if (usedDays > days) {
                    return false;
                }
            }

            // Put the current field into the current day.
            // This is always safe because capacity is guaranteed to be at least
            // the maximum single field requirement in the binary search.
            currentLoad += requirement;
        }

        // If we finish processing all fields without exceeding the day limit,
        // then the capacity works.
        return true;
    }

    /**
     * Runs a demonstration of the solution on sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(total input size for demonstrated examples)
     * Space complexity: O(1), excluding example arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] water1 = {7, 2, 5, 10, 8};
        int days1 = 2;
        long result1 = solution.minimumRefillCapacity(water1, days1);
        System.out.println("Example 1:");
        System.out.println("Input: water = " + Arrays.toString(water1) + ", days = " + days1);
        System.out.println("Output: " + result1);
        System.out.println("Expected: 18");
        System.out.println();

        int[] water2 = {3, 1, 4, 1, 5, 9};
        int days2 = 3;
        long result2 = solution.minimumRefillCapacity(water2, days2);
        System.out.println("Example 2:");
        System.out.println("Input: water = " + Arrays.toString(water2) + ", days = " + days2);
        System.out.println("Output: " + result2);
        System.out.println("Expected: 9");
        System.out.println();

        int[] water3 = {1, 2, 3, 4, 5};
        int days3 = 5;
        long result3 = solution.minimumRefillCapacity(water3, days3);
        System.out.println("Additional Example 3:");
        System.out.println("Input: water = " + Arrays.toString(water3) + ", days = " + days3);
        System.out.println("Output: " + result3);
        System.out.println("Expected: 5");
        System.out.println();

        int[] water4 = {1, 2, 3, 4, 5};
        int days4 = 1;
        long result4 = solution.minimumRefillCapacity(water4, days4);
        System.out.println("Additional Example 4:");
        System.out.println("Input: water = " + Arrays.toString(water4) + ", days = " + days4);
        System.out.println("Output: " + result4);
        System.out.println("Expected: 15");
    }
}