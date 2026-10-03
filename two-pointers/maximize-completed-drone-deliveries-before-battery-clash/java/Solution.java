import java.util.*;

/*
Problem Title: Maximize Completed Drone Deliveries Before Battery Clash

Problem Description:
A warehouse operates two launch pads for delivery drones. Each outgoing drone requires exactly one battery pack,
and each battery pack can be used at most once. You are given two integer arrays: drones and batteries.
drones[i] is the minimum charge required for the i-th drone to complete its route, and batteries[j] is the
available charge in the j-th battery pack.

A drone can be launched only if it is assigned a battery pack with charge greater than or equal to its required
charge. Each battery can power at most one drone, and each drone can receive at most one battery. Your task is
to determine the maximum number of drones that can be launched successfully.

This is not an ordering simulation problem: you may pair drones and batteries in any way you want. Return the
largest possible number of valid pairings.

A correct solution should be efficient for large inputs, which strongly suggests sorting and a two-pointer
strategy rather than checking all pairings.

Constraints:
- 1 <= drones.length, batteries.length <= 2 * 10^5
- 1 <= drones[i], batteries[j] <= 10^9
- Arrays are not necessarily sorted.

Example 1:
Input: drones = [4, 2, 7], batteries = [3, 8, 5]
Output: 2
Explanation: One optimal assignment is battery 3 -> drone 2 and battery 8 -> drone 7.
The remaining battery 5 cannot satisfy drone 4 after those choices in a way that increases the total beyond 2.

Example 2:
Input: drones = [1, 3, 3, 6], batteries = [2, 3, 4]
Output: 3
Explanation: An optimal assignment is 2 -> 1, 3 -> 3, and 4 -> 3.
The drone requiring 6 cannot be launched because no remaining battery has enough charge.

Goal:
Compute only the maximum number of completed deliveries.
*/

public class Solution {

    /**
     * Computes the maximum number of drones that can be launched successfully.
     *
     * Strategy:
     * 1. Sort both arrays in non-decreasing order.
     * 2. Use two pointers:
     *    - One pointer scans drones from the smallest requirement to the largest.
     *    - One pointer scans batteries from the smallest charge to the largest.
     * 3. If the current battery can satisfy the current drone, pair them and move both pointers.
     * 4. Otherwise, the battery is too weak, so move only the battery pointer to try a stronger battery.
     *
     * Why this greedy strategy works:
     * - We always try to satisfy the least demanding remaining drone with the smallest battery that can handle it.
     * - This preserves larger batteries for more demanding drones later.
     *
     * @param drones the array where each value is the minimum required charge for a drone
     * @param batteries the array where each value is the available charge in a battery pack
     * @return the maximum number of valid drone-battery pairings
     *
     * Time complexity: O(n log n + m log m), where n = drones.length and m = batteries.length,
     * due to sorting both arrays. The two-pointer scan is O(n + m).
     * Space complexity: O(1) extra space beyond the sorting implementation details
     * (ignoring the internal space used by Java's sorting algorithm for primitives).
     */
    public int maxCompletedDeliveries(int[] drones, int[] batteries) {
        // Sort both arrays so we can greedily match the smallest possible valid battery
        // to the smallest remaining drone requirement.
        Arrays.sort(drones);
        Arrays.sort(batteries);

        // i points to the current drone we want to satisfy.
        int i = 0;

        // j points to the current battery we are considering.
        int j = 0;

        // This counts how many successful pairings we have made.
        int completed = 0;

        // Continue while there are still drones to consider and batteries to try.
        while (i < drones.length && j < batteries.length) {
            // If the current battery has enough charge for the current drone,
            // we should pair them immediately.
            //
            // Why is this safe?
            // Because:
            // - This is the smallest remaining drone.
            // - This is the smallest remaining battery that we are currently testing.
            // - If it works, using it now avoids wasting a larger battery on an easier drone.
            if (batteries[j] >= drones[i]) {
                completed++;
                i++;
                j++;
            } else {
                // Otherwise, this battery is too weak for the current drone.
                //
                // Since drones are sorted, if this battery cannot satisfy the current smallest
                // remaining drone, it also cannot satisfy any larger drone after it.
                //
                // Therefore, this battery is unusable for all remaining drones, so we skip it.
                j++;
            }
        }

        return completed;
    }

    /**
     * Convenience helper that preserves the caller's original arrays by working on copies.
     * This is useful in demonstrations or when the caller does not want the inputs sorted in place.
     *
     * @param drones the array where each value is the minimum required charge for a drone
     * @param batteries the array where each value is the available charge in a battery pack
     * @return the maximum number of valid drone-battery pairings
     *
     * Time complexity: O(n log n + m log m), where n = drones.length and m = batteries.length
     * Space complexity: O(n + m) for the copied arrays
     */
    public int maxCompletedDeliveriesWithoutModifyingInput(int[] drones, int[] batteries) {
        int[] dronesCopy = Arrays.copyOf(drones, drones.length);
        int[] batteriesCopy = Arrays.copyOf(batteries, batteries.length);
        return maxCompletedDeliveries(dronesCopy, batteriesCopy);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * It also prints the expected outputs so the results can be visually verified.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n log n + m log m) per demonstration call
     * Space complexity: O(n + m) for the demonstration copies
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] drones1 = {4, 2, 7};
        int[] batteries1 = {3, 8, 5};
        int result1 = solution.maxCompletedDeliveriesWithoutModifyingInput(drones1, batteries1);
        System.out.println("Example 1 Result: " + result1);
        System.out.println("Expected: 2");

        // Example 2
        int[] drones2 = {1, 3, 3, 6};
        int[] batteries2 = {2, 3, 4};
        int result2 = solution.maxCompletedDeliveriesWithoutModifyingInput(drones2, batteries2);
        System.out.println("Example 2 Result: " + result2);
        System.out.println("Expected: 3");

        // Additional small checks for clarity
        int[] drones3 = {5, 5, 5};
        int[] batteries3 = {1, 2, 3};
        int result3 = solution.maxCompletedDeliveriesWithoutModifyingInput(drones3, batteries3);
        System.out.println("Additional Example 3 Result: " + result3);
        System.out.println("Expected: 0");

        int[] drones4 = {2, 2, 2};
        int[] batteries4 = {2, 2, 2, 2};
        int result4 = solution.maxCompletedDeliveriesWithoutModifyingInput(drones4, batteries4);
        System.out.println("Additional Example 4 Result: " + result4);
        System.out.println("Expected: 3");
    }
}