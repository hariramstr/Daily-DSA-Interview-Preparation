/*
 * Minimum Capacity Shipping Containers Over D Days
 * ================================================
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A shipping company needs to transport a sequence of cargo containers across a route.
 * Each container has a weight, and containers must be loaded onto ships in the order
 * they appear (you cannot reorder them). Each day, a single ship makes one trip and
 * can carry at most `capacity` total weight. You must ship all containers within
 * exactly D days.
 *
 * Given an integer array `weights` where `weights[i]` is the weight of the i-th
 * container, and an integer D representing the number of days available, return the
 * minimum ship capacity needed to ship all containers within D days.
 *
 * Note: Each container must be placed on a ship, and the order of containers cannot
 * be changed. A ship cannot carry more than its capacity on any single day.
 *
 * Constraints:
 * - 1 <= D <= weights.length <= 50000
 * - 1 <= weights[i] <= 500
 *
 * Example 1:
 * Input: weights = [3, 2, 2, 4, 1, 4], D = 3
 * Output: 6
 * Explanation: With capacity 6, Day 1 ships [3,2], Day 2 ships [2,4], Day 3 ships [1,4].
 *
 * Example 2:
 * Input: weights = [1, 2, 3, 4, 5], D = 2
 * Output: 9
 * Explanation: With capacity 9, Day 1 ships [1,2,3], Day 2 ships [4,5].
 */

public class Solution {

    /**
     * Finds the minimum ship capacity required to ship all containers within D days.
     *
     * <p>Key Insight (Binary Search on Answer):
     * Instead of trying all possible capacities one by one, we use binary search.
     * - The minimum possible capacity is the weight of the heaviest single container
     *   (because every container must fit on the ship at least once).
     * - The maximum possible capacity is the sum of all weights
     *   (ship everything in one day).
     * - We binary search between these two bounds to find the smallest capacity
     *   that still allows shipping everything within D days.
     *
     * @param weights Array of container weights in order
     * @param D       Number of days available for shipping
     * @return        Minimum ship capacity to ship all containers within D days
     *
     * Time Complexity:  O(N * log(S)) where N = number of containers,
     *                   S = sum of all weights (the search space size).
     *                   Binary search runs O(log S) iterations, each checking
     *                   feasibility in O(N).
     * Space Complexity: O(1) — only a few integer variables used, no extra arrays.
     */
    public int shipWithinDays(int[] weights, int D) {

        // ---------------------------------------------------------------
        // STEP 1: Determine the binary search boundaries.
        // ---------------------------------------------------------------

        // 'low' = minimum possible capacity.
        // We must be able to carry the heaviest single container,
        // otherwise it can never be shipped at all.
        int low = 0;

        // 'high' = maximum possible capacity.
        // If we put everything on one ship in one day, that's the upper bound.
        int high = 0;

        // Calculate both bounds in a single pass through the weights array.
        for (int weight : weights) {
            // The ship must carry at least the heaviest item.
            if (weight > low) {
                low = weight;
            }
            // Sum of all weights = capacity needed to ship everything in 1 day.
            high += weight;
        }

        // At this point:
        // low  = max(weights)  — smallest valid capacity
        // high = sum(weights)  — largest we'd ever need

        // ---------------------------------------------------------------
        // STEP 2: Binary search for the minimum feasible capacity.
        // ---------------------------------------------------------------

        // We will narrow [low, high] until low == high, which is our answer.
        while (low < high) {

            // 'mid' is the candidate capacity we are testing.
            // Using (low + high) / 2 could overflow for very large numbers,
            // so we use low + (high - low) / 2 as a safe alternative.
            int mid = low + (high - low) / 2;

            // ---------------------------------------------------------------
            // STEP 3: Check if capacity 'mid' is feasible (can ship in <= D days).
            // ---------------------------------------------------------------
            if (canShip(weights, D, mid)) {
                // 'mid' works! But maybe something smaller also works.
                // Move the upper bound DOWN to 'mid' (keep mid as a candidate).
                high = mid;
            } else {
                // 'mid' is too small — we need more capacity.
                // Move the lower bound UP past 'mid'.
                low = mid + 1;
            }
        }

        // ---------------------------------------------------------------
        // STEP 4: When low == high, we've found the minimum valid capacity.
        // ---------------------------------------------------------------
        return low;
    }

    /**
     * Helper method: Checks whether a given ship capacity allows shipping
     * all containers within D days (greedy simulation).
     *
     * <p>Strategy (Greedy):
     * Load containers onto the current day's ship one by one (in order).
     * When adding the next container would exceed capacity, start a new day.
     * Count how many days are needed total; if it's <= D, the capacity works.
     *
     * @param weights  Array of container weights in order
     * @param D        Maximum number of days allowed
     * @param capacity The ship capacity being tested
     * @return         true if all containers can be shipped within D days,
     *                 false otherwise
     *
     * Time Complexity:  O(N) — single pass through the weights array
     * Space Complexity: O(1) — only counters used
     */
    private boolean canShip(int[] weights, int D, int capacity) {

        // 'daysNeeded' tracks how many days (trips) we've used so far.
        // We start on day 1.
        int daysNeeded = 1;

        // 'currentLoad' tracks the total weight loaded onto today's ship.
        int currentLoad = 0;

        // Iterate through each container in order (order cannot change).
        for (int weight : weights) {

            // Check if adding this container would exceed today's capacity.
            if (currentLoad + weight > capacity) {
                // This container doesn't fit today — start a new day.
                daysNeeded++;

                // Reset the load for the new day, starting with this container.
                currentLoad = 0;
            }

            // Load this container onto the current day's ship.
            currentLoad += weight;
        }

        // If the total days needed is within our budget D, this capacity works.
        return daysNeeded <= D;
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     * Traces through both examples from the problem description.
     *
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {

        // Create an instance of Solution to call the method.
        Solution solution = new Solution();

        // ---------------------------------------------------------------
        // Example 1:
        // weights = [3, 2, 2, 4, 1, 4], D = 3
        // Expected Output: 6
        //
        // Trace of binary search:
        //   low = max(3,2,2,4,1,4) = 4
        //   high = 3+2+2+4+1+4 = 16
        //
        //   Iteration 1: mid = 4 + (16-4)/2 = 10
        //     canShip([3,2,2,4,1,4], 3, 10)?
        //       Day1: 3+2+2=7, +4 would be 11>10 → new day
        //       Day2: 4+1+4=9 ≤ 10 → fits
        //       daysNeeded=2 ≤ 3 → true
        //     high = 10
        //
        //   Iteration 2: mid = 4 + (10-4)/2 = 7
        //     canShip([3,2,2,4,1,4], 3, 7)?
        //       Day1: 3+2+2=7, +4 would be 11>7 → new day
        //       Day2: 4+1=5, +4=9>7 → new day
        //       Day3: 4
        //       daysNeeded=3 ≤ 3 → true
        //     high = 7
        //
        //   Iteration 3: mid = 4 + (7-4)/2 = 5
        //     canShip([3,2,2,4,1,4], 3, 5)?
        //       Day1: 3+2=5, +2=7>5 → new day
        //       Day2: 2, +4=6>5 → new day
        //       Day3: 4+1=5, +4=9>5 → new day
        //       Day4: 4
        //       daysNeeded=4 > 3 → false
        //     low = 6
        //
        //   Iteration 4: mid = 6 + (7-6)/2 = 6
        //     canShip([3,2,2,4,1,4], 3, 6)?
        //       Day1: 3+2=5, +2=7>6 → new day
        //       Day2: 2+4=6, +1=7>6 → new day
        //       Day3: 1+4=5
        //       daysNeeded=3 ≤ 3 → true
        //     high = 6
        //
        //   low == high == 6 → Answer: 6 ✓
        // ---------------------------------------------------------------
        int[] weights1 = {3, 2, 2, 4, 1, 4};
        int D1 = 3;
        int result1 = solution.shipWithinDays(weights1, D1);
        System.out.println("Example 1:");
        System.out.println("  weights = [3, 2, 2, 4, 1, 4], D = 3");
        System.out.println("  Expected Output: 6");
        System.out.println("  Actual Output:   " + result1);
        System.out.println("  Correct: " + (result1 == 6));
        System.out.println();

        // ---------------------------------------------------------------
        // Example 2:
        // weights = [1, 2, 3, 4, 5], D = 2
        // Expected Output: 9
        //
        // Trace of binary search:
        //   low = max(1,2,3,4,5) = 5
        //   high = 1+2+3+4+5 = 15
        //
        //   Iteration 1: mid = 5 + (15-5)/2 = 10
        //     canShip([1,2,3,4,5], 2, 10)?
        //       Day1: 1+2+3=6, +4=10, +5=15>10 → new day
        //       Day2: 5
        //       daysNeeded=2 ≤ 2 → true
        //     high = 10
        //
        //   Iteration 2: mid = 5 + (10-5)/2 = 7
        //     canShip([1,2,3,4,5], 2, 7)?
        //       Day1: 1+2+3=6, +4=10>7 → new day
        //       Day2: 4+5=9>7 → new day
        //       Day3: 5
        //       daysNeeded=3 > 2 → false
        //     low = 8
        //
        //   Iteration 3: mid = 8 + (10-8)/2 = 9
        //     canShip([1,2,3,4,5], 2, 9)?
        //       Day1: 1+2+3=6, +4=10>9 → new day
        //       Day2: 4+5=9 ≤ 9 → fits
        //       daysNeeded=2 ≤ 2 → true
        //     high = 9
        //
        //   Iteration 4: mid = 8 + (9-8)/2 = 8
        //     canShip([1,2,3,4,5], 2, 8)?
        //       Day1: 1+2+3=6, +4=10>8 → new day
        //       Day2: 4, +5=9>8 → new day
        //       Day3: 5
        //       daysNeeded=3 > 2 → false
        //     low = 9
        //
        //   low == high == 9 → Answer: 9 ✓
        // ---------------------------------------------------------------
        int[] weights2 = {1, 2, 3, 4, 5};
        int D2 = 2;
        int result2 = solution.shipWithinDays(weights2, D2);
        System.out.println("Example 2:");
        System.out.println("  weights = [1, 2, 3, 4, 5], D = 2");
        System.out.println("  Expected Output: 9");
        System.out.println("  Actual Output:   " + result2);
        System.out.println("  Correct: " + (result2 == 9));
        System.out.println();

        // ---------------------------------------------------------------
        // Additional Edge Case: D equals number of containers
        // Each container gets its own day → capacity = max weight
        // weights = [3, 5, 2], D = 3
        // Expected: 5 (each container ships alone, need capacity for heaviest)
        // ---------------------------------------------------------------
        int[] weights3 = {3, 5, 2};
        int D3 = 3;
        int result3 = solution.shipWithinDays(weights3, D3);
        System.out.println("Edge Case (D = number of containers):");
        System.out.println("  weights = [3, 5, 2], D = 3");
        System.out.println("  Expected Output: 5");
        System.out.println("  Actual Output:   " + result3);
        System.out.println("  Correct: " + (result3 == 5));
        System.out.println();

        // ---------------------------------------------------------------
        // Additional Edge Case: D = 1 (must ship everything in one day)
        // capacity = sum of all weights
        // weights = [2, 4, 6], D = 1
        // Expected: 12
        // ---------------------------------------------------------------
        int[] weights4 = {2, 4, 6};
        int D4 = 1;
        int result4 = solution.shipWithinDays(weights4, D4);
        System.out.println("Edge Case (D = 1, ship all in one day):");
        System.out.println("  weights = [2, 4, 6], D = 1");
        System.out.println("  Expected Output: 12");
        System.out.println("  Actual Output:   " + result4);
        System.out.println("  Correct: " + (result4 == 12));
    }
}