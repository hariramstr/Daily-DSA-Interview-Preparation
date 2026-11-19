import java.util.*;

/*
 * Title: Pair Lifters for a Moving Truck
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * A moving company has a list of furniture items, where weights[i] is the weight of the ith item.
 * Each trip with a hand truck can carry either one item or two items, but the total weight on a
 * single trip cannot exceed limit. Every item must be transported exactly once.
 *
 * Your task is to return the minimum number of trips required to move all items.
 *
 * This is not just a counting problem: an optimal solution should decide when to pair a light item
 * with a heavy item and when an item must go alone. The input array is not guaranteed to be sorted.
 * A good solution is expected to use sorting together with a two-pointer strategy.
 *
 * Implement a function that takes an integer array weights and an integer limit, and returns the
 * minimum number of trips needed.
 *
 * Constraints:
 * - 1 <= weights.length <= 100000
 * - 1 <= weights[i] <= limit <= 1000000000
 * - Each trip may carry at most 2 items
 * - All items must be moved
 *
 * Example 1:
 * Input: weights = [4, 2, 3, 5], limit = 6
 * Output: 3
 * Explanation: One optimal plan is (4,2), (3), (5). It is impossible to do it in 2 trips because
 * the item weighing 5 cannot be paired with any other item.
 *
 * Example 2:
 * Input: weights = [1, 2, 2, 3, 4], limit = 4
 * Output: 3
 * Explanation: One optimal plan is (1,3), (2,2), (4). This uses 3 trips, which is the minimum possible.
 */

public class Solution {

    /**
     * Computes the minimum number of trips needed to move all items.
     *
     * Strategy:
     * 1. Sort the weights in non-decreasing order.
     * 2. Use two pointers:
     *    - left points to the lightest remaining item
     *    - right points to the heaviest remaining item
     * 3. Always try to place the heaviest remaining item on a trip.
     *    - If the lightest remaining item can ride with it without exceeding the limit,
     *      pair them together and move both pointers.
     *    - Otherwise, the heaviest item must go alone, so move only the right pointer.
     * 4. Every iteration uses exactly one trip.
     *
     * Why this greedy choice works:
     * - The heaviest item is the hardest one to place.
     * - If even the lightest item cannot pair with the heaviest, then no other item can pair with it.
     *   So sending the heaviest alone is forced.
     * - If the lightest item can pair with the heaviest, doing so is always safe and helps preserve
     *   room for the remaining items.
     *
     * @param weights the array of item weights; each item must be transported exactly once
     * @param limit the maximum total weight allowed in a single trip
     * @return the minimum number of trips required to move all items
     * Time complexity: O(n log n) due to sorting, where n is weights.length
     * Space complexity: O(1) extra space beyond the sorting implementation details used by Java
     */
    public int minTrips(int[] weights, int limit) {
        // Defensive handling for completeness.
        // According to the constraints, weights.length >= 1, but this makes the method safer.
        if (weights == null || weights.length == 0) {
            return 0;
        }

        // Sort the array so that we can efficiently try to pair the lightest and heaviest items.
        Arrays.sort(weights);

        // left  -> index of the lightest remaining item
        // right -> index of the heaviest remaining item
        int left = 0;
        int right = weights.length - 1;

        // This will count how many trips we use.
        int trips = 0;

        // Continue until all items have been assigned to trips.
        while (left <= right) {
            // We are definitely going to use one trip for the heaviest remaining item at index right.
            // The only question is:
            // Can we also include the lightest remaining item at index left?
            if (left == right) {
                // Only one item remains.
                // It must go alone on one final trip.
                trips++;
                break;
            }

            // Check whether the lightest and heaviest remaining items can share a trip.
            if ((long) weights[left] + weights[right] <= limit) {
                // They fit together.
                // This is optimal because:
                // - the heaviest item gets placed
                // - the lightest item is the best candidate to pair with it
                left++;
                right--;
            } else {
                // They do not fit together.
                // Since weights[left] is the lightest remaining item, no one can pair with weights[right].
                // Therefore the heaviest item must go alone.
                right--;
            }

            // In both cases above, exactly one trip has been used.
            trips++;
        }

        return trips;
    }

    /**
     * A helper method that demonstrates the sorted two-pointer process in a beginner-friendly way.
     * This method is not required for solving the problem, but it is useful for understanding
     * how the algorithm makes decisions step by step.
     *
     * @param weights the array of item weights
     * @param limit the maximum total weight allowed in a single trip
     * @return the minimum number of trips required to move all items
     * Time complexity: O(n log n) due to sorting
     * Space complexity: O(n) because this method copies the input array for demonstration clarity
     */
    public int minTripsWithTrace(int[] weights, int limit) {
        if (weights == null || weights.length == 0) {
            System.out.println("No items to move.");
            return 0;
        }

        // Copy the array so the demonstration does not modify the caller's original data.
        int[] sorted = Arrays.copyOf(weights, weights.length);
        Arrays.sort(sorted);

        System.out.println("Sorted weights: " + Arrays.toString(sorted));
        System.out.println("Limit: " + limit);

        int left = 0;
        int right = sorted.length - 1;
        int trips = 0;

        while (left <= right) {
            if (left == right) {
                trips++;
                System.out.println("Trip " + trips + ": (" + sorted[right] + ") -> only one item remains");
                break;
            }

            int light = sorted[left];
            int heavy = sorted[right];

            if ((long) light + heavy <= limit) {
                trips++;
                System.out.println("Trip " + trips + ": (" + light + ", " + heavy + ") -> paired");
                left++;
                right--;
            } else {
                trips++;
                System.out.println("Trip " + trips + ": (" + heavy + ") -> too heavy to pair with " + light);
                right--;
            }
        }

        System.out.println("Minimum trips = " + trips);
        return trips;
    }

    /**
     * Runs sample demonstrations from the problem statement and prints the results.
     *
     * @param args command-line arguments; not used
     * @return nothing
     * Time complexity: O(n log n) per demonstration call
     * Space complexity: O(1) extra for minTrips, O(n) extra for minTripsWithTrace
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] weights1 = {4, 2, 3, 5};
        int limit1 = 6;
        int result1 = solution.minTrips(weights1, limit1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected: 3");
        System.out.println();

        int[] weights2 = {1, 2, 2, 3, 4};
        int limit2 = 4;
        int result2 = solution.minTrips(weights2, limit2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected: 3");
        System.out.println();

        System.out.println("Detailed trace for Example 1:");
        solution.minTripsWithTrace(new int[]{4, 2, 3, 5}, 6);
        System.out.println();

        System.out.println("Detailed trace for Example 2:");
        solution.minTripsWithTrace(new int[]{1, 2, 2, 3, 4}, 4);
        System.out.println();

        int[] extra1 = {3};
        int extraResult1 = solution.minTrips(extra1, 3);
        System.out.println("Extra test 1 result: " + extraResult1 + " (Expected: 1)");

        int[] extra2 = {2, 2, 2, 2};
        int extraResult2 = solution.minTrips(extra2, 3);
        System.out.println("Extra test 2 result: " + extraResult2 + " (Expected: 4)");

        int[] extra3 = {1, 1, 1, 1};
        int extraResult3 = solution.minTrips(extra3, 2);
        System.out.println("Extra test 3 result: " + extraResult3 + " (Expected: 2)");
    }
}