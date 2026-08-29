import java.util.*;

/*
 * Title: Last Cart Item Before Budget Overflow
 * Difficulty: Easy
 * Topic: Heaps and Priority Queues
 *
 * Problem Description:
 * You are building a shopping app that processes item prices one by one in the order they were scanned.
 * A customer has a fixed budget, but the app is allowed to remove previously scanned expensive items if
 * that helps keep the current cart affordable. After each new item is scanned, the cart should contain
 * as many of the scanned items as possible while keeping the total cost less than or equal to the budget.
 * If removing items is necessary, always remove the most expensive item currently in the cart first.
 *
 * Your task is to return the number of items left in the cart after all prices have been processed.
 *
 * This problem is naturally solved with a heap or priority queue: keep track of the items currently in
 * the cart, and when the total cost exceeds the budget, repeatedly remove the highest-priced item until
 * the cart is valid again.
 *
 * Constraints:
 * - 1 <= prices.length <= 100000
 * - 1 <= prices[i] <= 1000000000
 * - 1 <= budget <= 1000000000
 * - The answer fits in a 32-bit integer.
 *
 * Example 1:
 * Input: prices = [4, 2, 7, 1, 3], budget = 10
 * Output: 3
 * Explanation:
 * Scan 4, 2, 7, 1, 3 in order.
 * After scanning 7, total becomes 13, so remove the most expensive item 7. The cart becomes [4, 2].
 * Then add 1 -> total 7, add 3 -> total 10.
 * Final cart size is 3.
 *
 * Example 2:
 * Input: prices = [8, 5, 2, 6], budget = 9
 * Output: 2
 * Explanation:
 * Add 8 -> total 8.
 * Add 5 -> total 13, remove 8, cart becomes [5].
 * Add 2 -> total 7.
 * Add 6 -> total 13, remove 6, cart remains [5, 2].
 * Final cart size is 2.
 */

public class Solution {

    /**
     * Computes how many items remain in the cart after processing all prices in order,
     * always removing the most expensive current item whenever the running total exceeds the budget.
     *
     * The key idea:
     * - We scan prices from left to right.
     * - Every scanned item is tentatively added to the cart.
     * - We maintain the current total cost of items in the cart.
     * - If the total becomes too large, we repeatedly remove the largest item currently in the cart.
     * - A max-heap lets us quickly find and remove that largest item.
     *
     * Why this works:
     * - When we must remove something to get back under budget, removing the most expensive item
     *   reduces the total as much as possible in one step.
     * - This greedy choice preserves as many items as possible in the cart.
     *
     * @param prices the array of item prices scanned in order
     * @param budget the maximum allowed total cost of items currently in the cart
     * @return the number of items left in the cart after all prices are processed
     * Time complexity: O(n log n), where n is prices.length, because each item is inserted once
     * and may be removed once from the priority queue.
     * Space complexity: O(n) in the worst case for the heap storing current cart items.
     */
    public int lastCartItemBeforeBudgetOverflow(int[] prices, int budget) {
        // We use a max-heap so that the most expensive item is always available at the top.
        // Java's PriorityQueue is a min-heap by default, so we reverse the order.
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());

        // Use long for the running total to be extra safe.
        // Even though budget fits in int, the sum of many prices can exceed int range.
        long currentTotal = 0L;

        // Process each scanned price in order.
        for (int price : prices) {
            // Step 1: Add the newly scanned item into the cart.
            maxHeap.offer(price);

            // Step 2: Update the running total to include this item.
            currentTotal += price;

            // Step 3: If the cart is now over budget, we must remove items.
            // The rule says to always remove the most expensive current item first.
            // So while the total is too large, keep removing from the max-heap.
            while (currentTotal > budget && !maxHeap.isEmpty()) {
                // Remove the most expensive item currently in the cart.
                int removed = maxHeap.poll();

                // Subtract its price from the running total.
                currentTotal -= removed;
            }

            // After this loop ends:
            // - currentTotal <= budget
            // - the heap contains exactly the items still in the cart
        }

        // The number of items left in the cart is simply the heap size.
        return maxHeap.size();
    }

    /**
     * A second public helper method that performs the same computation.
     * This is included to keep the solution beginner-friendly and to show
     * a clearly named method that can be called from main or tests.
     *
     * @param prices the array of scanned item prices
     * @param budget the maximum total allowed in the cart
     * @return the final number of items remaining in the cart
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int remainingCartItems(int[] prices, int budget) {
        return lastCartItemBeforeBudgetOverflow(prices, budget);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log n) across the demonstrated examples
     * Space complexity: O(n) for the heap used during each example
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] prices1 = {4, 2, 7, 1, 3};
        int budget1 = 10;
        int result1 = solution.remainingCartItems(prices1, budget1);
        System.out.println("Example 1 Result: " + result1); // Expected: 3

        // Example 2
        int[] prices2 = {8, 5, 2, 6};
        int budget2 = 9;
        int result2 = solution.remainingCartItems(prices2, budget2);
        System.out.println("Example 2 Result: " + result2); // Expected: 2

        // Additional quick demonstration
        int[] prices3 = {5};
        int budget3 = 5;
        int result3 = solution.remainingCartItems(prices3, budget3);
        System.out.println("Additional Example Result: " + result3); // Expected: 1
    }
}