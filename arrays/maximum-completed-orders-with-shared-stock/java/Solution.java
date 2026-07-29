import java.util.*;

/*
Problem Title: Maximum Completed Orders with Shared Stock

Problem Description:
You are managing a warehouse that stores items of different product types. The array
`stock` represents how many units are currently available for each product type, where
`stock[i]` is the number of units of type `i`.

Each customer order must be fulfilled using units from exactly one product type, and every
fulfilled order must contain the same number of units, called the order size.

You are also given an integer `k`, the number of customer orders that must be fulfilled.
You may split the inventory of a single product type across multiple orders, but you cannot
combine units from different product types to form one order.

For example, if `stock[i] = 11` and the order size is `3`, that product type can support
`3` full orders, with `2` units left unused.

Return the maximum possible order size such that at least `k` orders can be fulfilled.

If it is impossible to fulfill `k` orders even with order size `1`, return `0`.

This problem asks you to determine the largest feasible uniform order size. A brute-force
search over all possible sizes may be too slow when stock counts are large, so an efficient
solution is expected.

Constraints:
- 1 <= stock.length <= 100000
- 1 <= stock[i] <= 1000000000
- 1 <= k <= 1000000000000

Example 1:
Input: stock = [8, 5, 6], k = 5
Output: 3
Explanation:
- Order size 3:
  - 8 / 3 = 2 orders
  - 5 / 3 = 1 order
  - 6 / 3 = 2 orders
  - Total = 5 orders, so size 3 works.
- Order size 4:
  - 8 / 4 = 2 orders
  - 5 / 4 = 1 order
  - 6 / 4 = 1 order
  - Total = 4 orders, so size 4 does not work.
Therefore, the maximum valid order size is 3.

Example 2:
Input: stock = [2, 3], k = 6
Output: 0
Explanation:
Even with order size 1, total possible orders = 2 + 3 = 5, which is less than 6.
So it is impossible to fulfill 6 orders, and the answer is 0.
*/

/**
 * A beginner-friendly solution using binary search on the answer.
 *
 * Core idea:
 * - If an order size x is feasible, then every smaller order size is also feasible.
 * - If an order size x is not feasible, then every larger order size is also not feasible.
 *
 * This monotonic behavior allows us to binary search for the largest feasible order size.
 */
public class Solution {

    /**
     * Finds the maximum possible uniform order size such that at least k orders can be fulfilled.
     *
     * Strategy:
     * 1. First check whether fulfilling k orders is possible even with order size 1.
     *    - If not, return 0 immediately.
     * 2. Otherwise, binary search the answer between 1 and max(stock).
     * 3. For each candidate size mid:
     *    - Count how many orders can be formed in total:
     *      sum(stock[i] / mid)
     *    - If total >= k, mid is feasible, so try larger sizes.
     *    - Otherwise, mid is too large, so try smaller sizes.
     *
     * @param stock an array where stock[i] is the number of units available for product type i
     * @param k the required number of customer orders to fulfill
     * @return the largest feasible uniform order size; returns 0 if even order size 1 cannot fulfill k orders
     * Time complexity: O(n log M), where n is stock.length and M is the maximum value in stock
     * Space complexity: O(1), ignoring input storage
     */
    public int maximumOrderSize(int[] stock, long k) {
        // Step 1:
        // Compute two useful values:
        // - totalUnits: total inventory across all product types
        // - maxStock: the largest single stock value, which becomes the upper bound for binary search
        long totalUnits = 0L;
        int maxStock = 0;

        for (int units : stock) {
            totalUnits += units;
            if (units > maxStock) {
                maxStock = units;
            }
        }

        // Step 2:
        // If even order size 1 cannot produce at least k orders, the answer is 0.
        // Why?
        // - With order size 1, every unit can become one order.
        // - So the maximum possible number of orders is exactly totalUnits.
        if (totalUnits < k) {
            return 0;
        }

        // Step 3:
        // Binary search for the largest feasible order size.
        //
        // Search space:
        // - Minimum possible valid size = 1
        // - Maximum possible valid size = maxStock
        int left = 1;
        int right = maxStock;
        int answer = 0;

        while (left <= right) {
            // Standard safe midpoint calculation to avoid overflow.
            int mid = left + (right - left) / 2;

            // Step 4:
            // Check whether order size = mid is feasible.
            if (canFulfill(stock, k, mid)) {
                // mid works, so record it as a candidate answer.
                answer = mid;

                // Since we want the MAXIMUM feasible size,
                // move right to search for a larger valid size.
                left = mid + 1;
            } else {
                // mid does not work, so any larger size also won't work.
                // Move left to search smaller sizes.
                right = mid - 1;
            }
        }

        // Step 5:
        // After binary search finishes, answer stores the largest feasible size found.
        return answer;
    }

    /**
     * Checks whether a given order size can fulfill at least k orders.
     *
     * For each product type:
     * - If stock[i] = units and orderSize = x,
     *   then that product type contributes units / x full orders.
     *
     * We sum these contributions across all product types.
     *
     * Important optimization:
     * - As soon as the running total reaches or exceeds k, we can stop early.
     * - This avoids unnecessary work and also keeps the running sum safely bounded.
     *
     * @param stock an array of available units for each product type
     * @param k the required number of orders
     * @param orderSize the candidate uniform order size being tested
     * @return true if at least k orders can be fulfilled with this order size; false otherwise
     * Time complexity: O(n) in the worst case
     * Space complexity: O(1)
     */
    public boolean canFulfill(int[] stock, long k, int orderSize) {
        // runningOrders must be long because:
        // - stock.length can be large
        // - total number of possible orders can exceed int range
        long runningOrders = 0L;

        for (int units : stock) {
            // Each product type contributes floor(units / orderSize) orders.
            runningOrders += units / orderSize;

            // Early exit:
            // Once we already have enough orders, there is no need to continue.
            if (runningOrders >= k) {
                return true;
            }
        }

        // If we finish the loop and still have fewer than k orders, this size is not feasible.
        return false;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding the called algorithm
     * Space complexity: O(1)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] stock1 = {8, 5, 6};
        long k1 = 5;
        int result1 = solution.maximumOrderSize(stock1, k1);
        System.out.println("Sample 1:");
        System.out.println("stock = " + Arrays.toString(stock1) + ", k = " + k1);
        System.out.println("Maximum order size = " + result1);
        System.out.println("Expected = 3");
        System.out.println();

        // Sample 2
        int[] stock2 = {2, 3};
        long k2 = 6;
        int result2 = solution.maximumOrderSize(stock2, k2);
        System.out.println("Sample 2:");
        System.out.println("stock = " + Arrays.toString(stock2) + ", k = " + k2);
        System.out.println("Maximum order size = " + result2);
        System.out.println("Expected = 0");
        System.out.println();

        // Additional quick checks
        int[] stock3 = {11};
        long k3 = 3;
        int result3 = solution.maximumOrderSize(stock3, k3);
        System.out.println("Additional Check 1:");
        System.out.println("stock = " + Arrays.toString(stock3) + ", k = " + k3);
        System.out.println("Maximum order size = " + result3);
        System.out.println("Expected = 3");
        System.out.println();

        int[] stock4 = {100, 1, 1};
        long k4 = 2;
        int result4 = solution.maximumOrderSize(stock4, k4);
        System.out.println("Additional Check 2:");
        System.out.println("stock = " + Arrays.toString(stock4) + ", k = " + k4);
        System.out.println("Maximum order size = " + result4);
        System.out.println("Expected = 50");
    }
}