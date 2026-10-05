import java.util.*;

/*
Problem Title: Minimum Daily Packaging Rate for Sequential Orders

Problem Description:
A fulfillment center must prepare customer orders in the exact order they appear in an array `orders`,
where `orders[i]` is the number of items in the `i`-th order. The center operates for exactly `d` days.
Each day, it chooses a consecutive block of remaining orders and packages them from left to right.
However, the total number of items packaged on a single day cannot exceed a fixed daily rate `R`.
An order cannot be split across multiple days: if an order starts on a day, all of its items must be
packaged that same day.

Your task is to find the minimum integer daily rate `R` such that all orders can be completed within `d` days.

This is not simply a greedy partitioning problem unless the candidate rate is fixed. A correct solution
should exploit the monotonic nature of feasibility: if a rate `R` is sufficient, then any rate larger
than `R` is also sufficient. Use this property to design an efficient algorithm.

Return the smallest possible `R`.

Constraints:
- 1 <= orders.length <= 2 * 10^5
- 1 <= orders[i] <= 10^9
- 1 <= d <= 10^9
- Orders must remain in the original order.
- Each day must process a contiguous sequence of unsent orders.
- An individual order cannot be split.

Example 1:
Input: orders = [7, 2, 5, 10, 8], d = 2
Output: 18
Explanation: With rate 18, one valid schedule is [7, 2, 5] and [10, 8].
Any rate below 18 fails because the last two orders would require more than one day.

Example 2:
Input: orders = [3, 6, 7, 11], d = 4
Output: 11
Explanation: Since orders cannot be split, the rate must be at least the largest single order size,
which is 11. Using rate 11, the orders can be scheduled as [3], [6], [7], [11].
*/

public class Solution {

    /**
     * Computes the minimum daily packaging rate needed to finish all orders within at most d days.
     *
     * Core idea:
     * 1. If we fix a candidate rate R, we can greedily simulate how many days are needed:
     *    - Keep adding consecutive orders to the current day while the sum does not exceed R.
     *    - If the next order would exceed R, start a new day.
     * 2. This feasibility is monotonic:
     *    - If rate R works, then any larger rate also works.
     *    - If rate R does not work, then any smaller rate also does not work.
     * 3. Therefore, we can binary search the smallest feasible rate.
     *
     * Search range:
     * - Lower bound = maximum single order size
     *   Because no order can be split, the rate must be at least that large.
     * - Upper bound = sum of all orders
     *   Because with that rate, everything can be done in one day.
     *
     * @param orders the array of order sizes that must be processed in the given order
     * @param d the maximum number of days allowed
     * @return the smallest integer daily rate that allows all orders to be completed within d days
     * Time complexity: O(n log S), where n is orders.length and S is the search range of possible rates
     * Space complexity: O(1), excluding input storage
     */
    public long minimumDailyRate(int[] orders, int d) {
        long left = 0L;
        long right = 0L;

        // Build the binary search boundaries.
        // left  = largest single order
        // right = total sum of all orders
        for (int order : orders) {
            left = Math.max(left, order);
            right += order;
        }

        // Standard binary search on the answer.
        // We are looking for the smallest feasible rate.
        while (left < right) {
            long mid = left + (right - left) / 2;

            // If mid is enough to finish within d days,
            // then try to find an even smaller feasible rate.
            if (canFinishWithinDays(orders, d, mid)) {
                right = mid;
            } else {
                // Otherwise, mid is too small, so we must go larger.
                left = mid + 1;
            }
        }

        // At the end, left == right and points to the minimum feasible rate.
        return left;
    }

    /**
     * Checks whether all orders can be completed within at most d days using the given daily rate.
     *
     * Greedy simulation details:
     * - We process orders from left to right because the order must be preserved.
     * - For the current day, we keep adding orders as long as the total does not exceed rate.
     * - If adding the next order would exceed rate, we start a new day and place that order there.
     * - This greedy strategy minimizes the number of days for a fixed rate because each day is packed
     *   as much as possible before moving on.
     *
     * Important correctness note:
     * - If even one order is larger than rate, then the schedule is impossible immediately.
     *   In this problem, our binary search lower bound already avoids that, but we still keep the
     *   check here for completeness and safety.
     *
     * @param orders the array of order sizes
     * @param d the maximum allowed number of days
     * @param rate the candidate daily packaging rate to test
     * @return true if all orders can be completed within at most d days using this rate; false otherwise
     * Time complexity: O(n), where n is orders.length
     * Space complexity: O(1)
     */
    public boolean canFinishWithinDays(int[] orders, int d, long rate) {
        long currentDayLoad = 0L;
        int daysUsed = 1;

        // Traverse every order exactly once.
        for (int order : orders) {
            // Safety check: if a single order is larger than the daily rate,
            // it cannot fit into any day because splitting is forbidden.
            if (order > rate) {
                return false;
            }

            // Try to place this order into the current day.
            // If it fits, accumulate it.
            if (currentDayLoad + order <= rate) {
                currentDayLoad += order;
            } else {
                // Otherwise, we must start a new day.
                daysUsed++;
                currentDayLoad = order;

                // Early exit:
                // If we already need more than d days, this rate is not feasible.
                if (daysUsed > d) {
                    return false;
                }
            }
        }

        // If we never exceeded d days, the rate works.
        return true;
    }

    /**
     * Utility method to print an array in a beginner-friendly format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n) due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Verified examples:
     * 1. orders = [7, 2, 5, 10, 8], d = 2
     *    - Minimum rate is 18
     *    - One valid partition: [7, 2, 5] and [10, 8]
     *
     * 2. orders = [3, 6, 7, 11], d = 4
     *    - Minimum rate is 11
     *    - One valid partition: [3], [6], [7], [11]
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log S) per demonstration case
     * Space complexity: O(1), excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] orders1 = {7, 2, 5, 10, 8};
        int d1 = 2;
        long result1 = solution.minimumDailyRate(orders1, d1);
        System.out.println("Example 1");
        System.out.println("Orders: " + solution.arrayToString(orders1));
        System.out.println("Days: " + d1);
        System.out.println("Minimum daily rate: " + result1);
        System.out.println("Expected: 18");
        System.out.println();

        int[] orders2 = {3, 6, 7, 11};
        int d2 = 4;
        long result2 = solution.minimumDailyRate(orders2, d2);
        System.out.println("Example 2");
        System.out.println("Orders: " + solution.arrayToString(orders2));
        System.out.println("Days: " + d2);
        System.out.println("Minimum daily rate: " + result2);
        System.out.println("Expected: 11");
        System.out.println();

        // Additional quick sanity checks.
        int[] orders3 = {1, 2, 3, 4, 5};
        int d3 = 5;
        long result3 = solution.minimumDailyRate(orders3, d3);
        System.out.println("Additional Example 3");
        System.out.println("Orders: " + solution.arrayToString(orders3));
        System.out.println("Days: " + d3);
        System.out.println("Minimum daily rate: " + result3);
        System.out.println("Expected: 5");
        System.out.println();

        int[] orders4 = {1, 2, 3, 4, 5};
        int d4 = 1;
        long result4 = solution.minimumDailyRate(orders4, d4);
        System.out.println("Additional Example 4");
        System.out.println("Orders: " + solution.arrayToString(orders4));
        System.out.println("Days: " + d4);
        System.out.println("Minimum daily rate: " + result4);
        System.out.println("Expected: 15");
    }
}