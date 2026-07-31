import java.util.*;

/*
 * Title: Visible Customers After Each Line Update
 * Difficulty: Medium
 * Topic: Stacks and Queues
 *
 * Problem Description:
 * A store manager tracks the heights of customers standing in a single checkout line from front to back.
 * For staffing analysis, the manager wants to know, for each customer, how many customers in front of
 * them are visible. A customer can see another customer in front if every customer standing between them
 * is strictly shorter than both of those two customers. If a taller or equal-height customer appears first,
 * visibility stops there, but that blocking customer is still visible.
 *
 * Given an array heights where heights[i] is the height of the i-th customer in line (0-indexed, from
 * front to back), return an array answer of the same length where answer[i] is the number of customers
 * in front of customer i that are visible to them.
 *
 * You should design an efficient solution using stack-based processing rather than checking every pair directly.
 *
 * Constraints:
 * - 1 <= heights.length <= 200000
 * - 1 <= heights[i] <= 1000000000
 * - The answer for each position fits in a 32-bit integer
 *
 * Example 1:
 * Input: heights = [10,6,8,5,11,9]
 * Output: [0,1,2,1,4,1]
 *
 * Example 2:
 * Input: heights = [5,5,4,7,6]
 * Output: [0,1,1,3,1]
 */

public class Solution {

    /**
     * Computes how many customers in front are visible for every customer in the line.
     *
     * Core idea:
     * We process customers from front to back while maintaining a monotonic stack of indices.
     * The stack represents a decreasing sequence of heights among customers that are still relevant
     * as potential blockers for future customers.
     *
     * For the current customer i:
     * 1. While the top of the stack is strictly shorter than heights[i], that shorter customer is visible
     *    to i, so we pop it and increase the visible count.
     * 2. After removing all strictly shorter customers, if the stack is not empty, then the customer now
     *    on top is the first taller-or-equal blocker. That blocker is also visible, so we add one more.
     * 3. Push the current customer onto the stack for future customers.
     *
     * This works because:
     * - Every shorter customer popped is visible and can never block any later customer once a taller
     *   current customer has arrived.
     * - The first remaining taller-or-equal customer is visible and blocks anything behind them.
     *
     * @param heights an array where heights[i] is the height of the i-th customer from front to back
     * @return an array answer where answer[i] is the number of customers in front visible to customer i
     * @implNote Time complexity: O(n), because each index is pushed once and popped at most once
     * @implNote Space complexity: O(n), for the stack and output array
     */
    public int[] visibleCustomersInFront(int[] heights) {
        int n = heights.length;
        int[] answer = new int[n];

        // We store indices instead of heights directly.
        // This is a common Java interview pattern because:
        // - it lets us access the original heights array
        // - it keeps the stack lightweight
        // - it is easy to extend if index-based logic is ever needed
        Deque<Integer> stack = new ArrayDeque<>();

        // We move from front to back.
        // For each customer i, we determine how many customers before i are visible.
        for (int i = 0; i < n; i++) {
            int visibleCount = 0;

            // Step 1:
            // Pop every strictly shorter customer from the top of the stack.
            //
            // Why are they visible?
            // Because the stack stores a decreasing sequence of candidate blockers.
            // If the top is shorter than the current customer, then the current customer can see that person.
            //
            // Why can we remove them permanently?
            // Because once a taller current customer appears, that shorter person can never be the first blocker
            // for any future customer behind i. The current customer is at least as good a blocker and stands closer.
            while (!stack.isEmpty() && heights[stack.peek()] < heights[i]) {
                stack.pop();
                visibleCount++;
            }

            // Step 2:
            // If the stack is still not empty, then the person on top is the first customer in front
            // who is taller than or equal to the current customer.
            //
            // That person is visible, but they also stop further visibility.
            if (!stack.isEmpty()) {
                visibleCount++;
            }

            // Store the result for the current customer.
            answer[i] = visibleCount;

            // Step 3:
            // Push the current customer onto the stack so they can act as a future visible person or blocker.
            stack.push(i);
        }

        return answer;
    }

    /**
     * Convenience wrapper that prints the input and the computed result.
     *
     * @param heights the input heights array
     * @return the computed visibility counts
     * @implNote Time complexity: O(n)
     * @implNote Space complexity: O(n)
     */
    public int[] runAndPrint(int[] heights) {
        int[] result = visibleCustomersInFront(heights);
        System.out.println("Heights: " + Arrays.toString(heights));
        System.out.println("Visible counts: " + Arrays.toString(result));
        System.out.println();
        return result;
    }

    /**
     * Program entry point.
     *
     * Demonstrates the algorithm on the sample inputs from the problem statement
     * and a few additional small sanity checks.
     *
     * @param args command-line arguments (not used)
     * @implNote Time complexity: O(total number of elements across demonstrated test cases)
     * @implNote Space complexity: O(max test case size)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        // Expected: [0, 1, 2, 1, 4, 1]
        solution.runAndPrint(new int[]{10, 6, 8, 5, 11, 9});

        // Sample 2
        // Expected: [0, 1, 1, 3, 1]
        solution.runAndPrint(new int[]{5, 5, 4, 7, 6});

        // Additional sanity checks

        // Strictly increasing:
        // Each customer sees everyone in front because each new customer is taller than all previous ones.
        // Expected: [0, 1, 2, 3, 4]
        solution.runAndPrint(new int[]{1, 2, 3, 4, 5});

        // Strictly decreasing:
        // Each customer sees only the immediate person in front, who blocks the rest.
        // Expected: [0, 1, 1, 1, 1]
        solution.runAndPrint(new int[]{5, 4, 3, 2, 1});

        // Equal heights:
        // Each customer sees exactly the nearest equal-height customer in front.
        // Expected: [0, 1, 1, 1]
        solution.runAndPrint(new int[]{7, 7, 7, 7});
    }
}