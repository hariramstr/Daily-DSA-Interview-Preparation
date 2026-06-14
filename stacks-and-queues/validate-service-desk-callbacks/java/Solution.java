import java.util.*;

/*
Problem Title: Validate Service Desk Callbacks

Problem Description:
A customer support system receives calls identified by unique integer IDs. During the day, every incoming call is appended to a waiting line in the order it arrives. At certain times, the system records a callback log showing the order in which calls were actually handled. Because the support desk always serves the oldest waiting call first, except that some calls may still remain unhandled by the end of the day, the callback log should be a valid prefix of the queue's natural processing order.

You are given two integer arrays: arrivals and handled. The array arrivals lists call IDs in the exact order they entered the waiting line. The array handled lists call IDs in the exact order the support desk claims to have handled them. Determine whether handled could be a valid callback log.

A callback log is valid if each handled call appears at the front of the queue at the moment it is processed, and every handled call must have appeared earlier in arrivals. Calls are unique, and a handled call cannot appear more than once.

Return true if handled is valid, otherwise return false.

Constraints:
- 1 <= arrivals.length <= 100000
- 0 <= handled.length <= arrivals.length
- 1 <= arrivals[i], handled[i] <= 1000000000
- All values in arrivals are distinct
- All values in handled are distinct

Example 1:
Input: arrivals = [10, 20, 30, 40], handled = [10, 20]
Output: true
Explanation: The desk handles the first two calls in FIFO order, and the remaining calls are still waiting.

Example 2:
Input: arrivals = [10, 20, 30, 40], handled = [10, 30]
Output: false
Explanation: After handling call 10, call 20 is still at the front of the queue, so call 30 cannot be handled next.
*/

public class Solution {

    /**
     * Determines whether the handled array is a valid callback log for the given arrivals array.
     *
     * Core idea:
     * Since the support desk always processes calls in FIFO order, the handled sequence must be
     * exactly the prefix of arrivals with the same length.
     *
     * Example:
     * arrivals = [10, 20, 30, 40]
     * handled  = [10, 20]
     * This is valid because handled matches the first 2 elements of arrivals.
     *
     * arrivals = [10, 20, 30, 40]
     * handled  = [10, 30]
     * This is invalid because after 10, the next front call must be 20, not 30.
     *
     * @param arrivals the order in which calls entered the waiting line
     * @param handled the claimed order in which calls were handled
     * @return true if handled is a valid FIFO callback log; false otherwise
     * Time complexity: O(m), where m = handled.length
     * Space complexity: O(1)
     */
    public boolean isValidCallbackLog(int[] arrivals, int[] handled) {
        // If handled contains more calls than arrivals, it is impossible.
        // This check is defensive; according to constraints, handled.length <= arrivals.length.
        if (handled.length > arrivals.length) {
            return false;
        }

        // We compare each handled call with the call that should be at the front of the queue.
        // Because the queue is FIFO and no reordering is allowed, the first handled.length calls
        // must match the first handled.length arrivals exactly.
        for (int i = 0; i < handled.length; i++) {
            // Step-by-step reasoning:
            // - arrivals[i] is the i-th call that arrived.
            // - Since the desk always serves the oldest waiting call first,
            //   the i-th handled call must be exactly arrivals[i].
            // - If they differ at any position, then the callback log is invalid.
            if (arrivals[i] != handled[i]) {
                return false;
            }
        }

        // If every handled call matched the corresponding front-of-queue call,
        // then handled is a valid prefix of the natural queue processing order.
        return true;
    }

    /**
     * A second beginner-friendly method that validates the callback log by explicitly simulating
     * the queue behavior using indices.
     *
     * This method produces the same result as isValidCallbackLog, but the comments explain the
     * queue interpretation in a very direct way.
     *
     * @param arrivals the order in which calls entered the waiting line
     * @param handled the claimed order in which calls were handled
     * @return true if handled can be produced by repeatedly removing the front call from the queue;
     *         false otherwise
     * Time complexity: O(m), where m = handled.length
     * Space complexity: O(1)
     */
    public boolean isValidCallbackLogBySimulation(int[] arrivals, int[] handled) {
        // queueFront represents the current front position in the arrivals array.
        // At the beginning, the first arrival is at the front of the queue.
        int queueFront = 0;

        // Process each claimed handled call in order.
        for (int i = 0; i < handled.length; i++) {
            // If queueFront has moved beyond arrivals, then there are no calls left to handle.
            // In that case, handled is invalid.
            if (queueFront >= arrivals.length) {
                return false;
            }

            // The only call that can be handled now is the one currently at the front.
            int expectedFrontCall = arrivals[queueFront];
            int actualHandledCall = handled[i];

            // If the claimed handled call is not equal to the front call,
            // then the log violates FIFO order.
            if (expectedFrontCall != actualHandledCall) {
                return false;
            }

            // Since the front call was correctly handled, remove it from the queue
            // by advancing the front pointer.
            queueFront++;
        }

        // If we successfully matched every handled call with the queue front,
        // the callback log is valid.
        return true;
    }

    /**
     * Converts an integer array to a readable string for printing.
     *
     * @param array the input integer array
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Runs one demonstration test case and prints the result.
     *
     * @param arrivals the arrivals array for the test
     * @param handled the handled array for the test
     * @return nothing; this method prints directly to standard output
     * Time complexity: O(m), where m = handled.length
     * Space complexity: O(1), excluding output formatting
     */
    public void runDemo(int[] arrivals, int[] handled) {
        boolean result = isValidCallbackLog(arrivals, handled);

        System.out.println("Arrivals: " + arrayToString(arrivals));
        System.out.println("Handled : " + arrayToString(handled));
        System.out.println("Valid   : " + result);
        System.out.println();
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional examples.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total handled elements across demo cases)
     * Space complexity: O(1), excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Example 1
        // arrivals = [10, 20, 30, 40]
        // handled  = [10, 20]
        // Expected: true
        solution.runDemo(
                new int[]{10, 20, 30, 40},
                new int[]{10, 20}
        );

        // Sample Example 2
        // arrivals = [10, 20, 30, 40]
        // handled  = [10, 30]
        // Expected: false
        solution.runDemo(
                new int[]{10, 20, 30, 40},
                new int[]{10, 30}
        );

        // Additional Example 3
        // No calls handled yet, which is always a valid prefix.
        // Expected: true
        solution.runDemo(
                new int[]{5, 6, 7},
                new int[]{}
        );

        // Additional Example 4
        // Entire queue handled in exact FIFO order.
        // Expected: true
        solution.runDemo(
                new int[]{1, 2, 3},
                new int[]{1, 2, 3}
        );

        // Additional Example 5
        // First handled call is not the first arrival.
        // Expected: false
        solution.runDemo(
                new int[]{100, 200, 300},
                new int[]{200}
        );
    }
}