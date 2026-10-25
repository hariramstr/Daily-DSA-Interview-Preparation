import java.util.*;

/*
 * Title: Minimum Oven Capacity for Batch Cookies
 *
 * Problem Description:
 * A bakery receives cookie orders in a fixed sequence. The i-th order requires trays[i]
 * cookie trays to be baked, and all trays assigned to the same baking session must be
 * processed together in the original order. The bakery has exactly d baking sessions
 * available for the day. In one session, the oven can bake a contiguous group of orders
 * as long as the total number of trays in that session does not exceed the oven capacity.
 *
 * Your task is to find the minimum oven capacity needed so that all orders can be completed
 * within at most d sessions.
 *
 * You may not reorder orders, split a single order across multiple sessions, or skip any
 * order. A larger oven capacity always makes the schedule at least as feasible as a smaller
 * one, so an efficient solution should take advantage of this monotonic property.
 *
 * Return the smallest integer oven capacity that allows all orders to be baked within d sessions.
 *
 * Constraints:
 * - 1 <= trays.length <= 100000
 * - 1 <= trays[i] <= 1000000
 * - 1 <= d <= trays.length
 * - The answer fits in a 64-bit signed integer.
 *
 * Example 1:
 * Input: trays = [8, 3, 5, 7, 2], d = 3
 * Output: 10
 * Explanation:
 * One optimal grouping is [8], [3, 5], [7, 2]. The maximum trays baked in any session is 10.
 *
 * Example 2:
 * Input: trays = [4, 4, 4, 4], d = 2
 * Output: 8
 * Explanation:
 * We can bake [4, 4] and [4, 4] in two sessions.
 */

public class Solution {

    /**
     * Finds the minimum oven capacity required to bake all orders within at most d sessions.
     *
     * The key idea is:
     * 1. If the oven capacity is too small, we will need too many sessions.
     * 2. If the oven capacity is large enough, we can finish within d sessions.
     * 3. This creates a monotonic search space, which is perfect for binary search.
     *
     * Search range:
     * - Lower bound = largest single order, because one order cannot be split.
     * - Upper bound = sum of all orders, because one huge session could bake everything.
     *
     * @param trays the array where trays[i] is the number of trays required by the i-th order
     * @param d the maximum number of baking sessions allowed
     * @return the smallest oven capacity that allows all orders to be baked within at most d sessions
     *
     * Time complexity: O(n log S), where n is trays.length and S is the range between max(trays) and sum(trays)
     * Space complexity: O(1), excluding input storage
     */
    public long minimumOvenCapacity(int[] trays, int d) {
        // The minimum possible capacity cannot be smaller than the largest single order,
        // because we are not allowed to split an order across multiple sessions.
        long left = 0;

        // The maximum possible capacity is the sum of all orders,
        // which means we could bake everything in one session.
        long right = 0;

        // Compute both bounds in one pass:
        // - left becomes the maximum element
        // - right becomes the total sum
        for (int tray : trays) {
            left = Math.max(left, tray);
            right += tray;
        }

        // Binary search for the smallest feasible capacity.
        // Invariant:
        // - Any capacity < answer is infeasible
        // - Any capacity >= answer is feasible
        while (left < right) {
            // Use this form to avoid overflow:
            // mid = left + (right - left) / 2
            long mid = left + (right - left) / 2;

            // Check whether this candidate capacity is enough.
            if (canBakeWithinDays(trays, d, mid)) {
                // If mid works, try to find an even smaller feasible capacity.
                right = mid;
            } else {
                // If mid does not work, we must increase the capacity.
                left = mid + 1;
            }
        }

        // At the end of binary search, left == right and points to the minimum feasible capacity.
        return left;
    }

    /**
     * Checks whether all orders can be baked within at most d sessions using the given oven capacity.
     *
     * Greedy strategy:
     * - Process orders from left to right.
     * - Keep adding orders to the current session while they fit.
     * - As soon as the next order would exceed capacity, start a new session.
     *
     * Why greedy works:
     * - To minimize the number of sessions for a fixed capacity, we should pack each session
     *   as much as possible before starting the next one.
     * - Because orders must remain contiguous and in order, this greedy approach gives the
     *   minimum number of sessions needed for that capacity.
     *
     * @param trays the array of order tray counts
     * @param d the maximum number of sessions allowed
     * @param capacity the candidate oven capacity to test
     * @return true if all orders can be baked within at most d sessions, otherwise false
     *
     * Time complexity: O(n), where n is trays.length
     * Space complexity: O(1)
     */
    public boolean canBakeWithinDays(int[] trays, int d, long capacity) {
        // We start with one session already in use,
        // because if there is at least one order, we need at least one session.
        int sessionsUsed = 1;

        // This stores the total trays currently assigned to the ongoing session.
        long currentLoad = 0;

        // Process each order in the original fixed order.
        for (int tray : trays) {
            // If adding this order would exceed the oven capacity,
            // we must start a new session.
            if (currentLoad + tray > capacity) {
                sessionsUsed++;
                currentLoad = tray;

                // Early exit:
                // If we already need more than d sessions, this capacity is not feasible.
                if (sessionsUsed > d) {
                    return false;
                }
            } else {
                // Otherwise, safely add this order to the current session.
                currentLoad += tray;
            }
        }

        // If we finished processing all orders without exceeding d sessions,
        // then this capacity is feasible.
        return true;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It prints:
     * - the input arrays
     * - the number of sessions
     * - the computed minimum oven capacity
     * - the expected answer for easy comparison
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n log S) for each demonstration case
     * Space complexity: O(1), excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] trays1 = {8, 3, 5, 7, 2};
        int d1 = 3;
        long result1 = solution.minimumOvenCapacity(trays1, d1);
        System.out.println("Example 1:");
        System.out.println("trays = " + Arrays.toString(trays1) + ", d = " + d1);
        System.out.println("Minimum oven capacity = " + result1);
        System.out.println("Expected = 10");
        System.out.println();

        int[] trays2 = {4, 4, 4, 4};
        int d2 = 2;
        long result2 = solution.minimumOvenCapacity(trays2, d2);
        System.out.println("Example 2:");
        System.out.println("trays = " + Arrays.toString(trays2) + ", d = " + d2);
        System.out.println("Minimum oven capacity = " + result2);
        System.out.println("Expected = 8");
        System.out.println();

        // Additional quick sanity checks for beginners:
        // 1) If d == trays.length, each order can be baked alone,
        //    so the answer should be the largest single order.
        int[] trays3 = {5, 1, 9, 2};
        int d3 = 4;
        long result3 = solution.minimumOvenCapacity(trays3, d3);
        System.out.println("Additional Test 1:");
        System.out.println("trays = " + Arrays.toString(trays3) + ", d = " + d3);
        System.out.println("Minimum oven capacity = " + result3);
        System.out.println("Expected = 9");
        System.out.println();

        // 2) If d == 1, everything must be baked in one session,
        //    so the answer should be the total sum.
        int[] trays4 = {2, 3, 4};
        int d4 = 1;
        long result4 = solution.minimumOvenCapacity(trays4, d4);
        System.out.println("Additional Test 2:");
        System.out.println("trays = " + Arrays.toString(trays4) + ", d = " + d4);
        System.out.println("Minimum oven capacity = " + result4);
        System.out.println("Expected = 9");
    }
}