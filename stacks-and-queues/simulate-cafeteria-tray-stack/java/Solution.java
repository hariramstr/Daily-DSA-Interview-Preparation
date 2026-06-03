/*
 * Title: Simulate a Cafeteria Tray Stack
 *
 * Problem Description:
 * A cafeteria uses a spring-loaded tray dispenser that works like a stack —
 * trays are added and removed from the top. You are given a list of operations
 * to simulate. Each operation is one of the following:
 *
 * - "push X" — Place a tray with label X (an integer) onto the top of the stack.
 * - "pop"    — Remove and record the tray from the top of the stack.
 *              If the stack is empty, record -1 instead.
 * - "peek"   — Record the value of the tray on top without removing it.
 *              If the stack is empty, record -1.
 *
 * Return a list of integers representing the results of all "pop" and "peek"
 * operations in the order they appear.
 *
 * Constraints:
 * - 1 <= operations.length <= 1000
 * - Each "push" operation has a valid integer X where -10^4 <= X <= 10^4
 * - There will be no malformed operations
 *
 * Example 1:
 * Input:  ["push 5", "push 3", "peek", "pop", "pop", "pop"]
 * Output: [3, 3, 5, -1]
 *
 * Example 2:
 * Input:  ["pop", "push 10", "push 20", "peek", "pop"]
 * Output: [-1, 20, 20]
 */

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * Solution class for simulating a cafeteria tray stack.
 *
 * <p>A stack follows Last-In-First-Out (LIFO) order — the last tray placed on
 * top is the first one to be removed, just like a real spring-loaded dispenser.</p>
 */
public class Solution {

    /**
     * Simulates a cafeteria tray stack given a list of string operations.
     *
     * <p>Processes each operation in order:
     * <ul>
     *   <li>"push X" — pushes integer X onto the stack (no output recorded)</li>
     *   <li>"pop"    — removes the top element and records it (or -1 if empty)</li>
     *   <li>"peek"   — reads the top element without removing it (or -1 if empty)</li>
     * </ul>
     * </p>
     *
     * @param operations a list of operation strings to process
     * @return a list of integers representing results of all pop and peek operations
     *         in the order they were encountered
     *
     * Time Complexity:  O(n) — we process each of the n operations exactly once;
     *                   each push/pop/peek on a Deque is O(1).
     * Space Complexity: O(n) — in the worst case all operations are pushes,
     *                   so the stack can hold up to n elements.
     */
    public List<Integer> simulateTrayStack(List<String> operations) {

        // ---------------------------------------------------------------
        // Step 1: Prepare our data structures.
        //
        // We use an ArrayDeque as our stack.  ArrayDeque is the preferred
        // Java stack implementation (faster than the legacy Stack class).
        // The "top" of our tray stack corresponds to the HEAD (front) of
        // the deque so that push/pop/peek are all O(1).
        // ---------------------------------------------------------------
        Deque<Integer> stack = new ArrayDeque<>();

        // This list will collect the integer results of every "pop" and
        // "peek" operation we encounter.
        List<Integer> results = new ArrayList<>();

        // ---------------------------------------------------------------
        // Step 2: Iterate through every operation string one by one.
        // ---------------------------------------------------------------
        for (String operation : operations) {

            // -----------------------------------------------------------
            // Step 3: Determine which type of operation this string is.
            //
            // We check the first characters of the string:
            //   "push ..." starts with "push"
            //   "pop"      is exactly "pop"
            //   "peek"     is exactly "peek"
            //
            // Using startsWith / equals keeps the logic clear and avoids
            // splitting the string unnecessarily for pop/peek.
            // -----------------------------------------------------------

            if (operation.startsWith("push")) {
                // -------------------------------------------------------
                // PUSH operation
                //
                // The operation string looks like "push 5" or "push -42".
                // We need to extract the integer after the space.
                //
                // operation.substring(5) skips the first 5 characters
                // ("push ") and gives us the number as a String, which
                // Integer.parseInt converts to an int.
                // -------------------------------------------------------
                String numberPart = operation.substring(5); // e.g. "5" or "-42"
                int trayLabel = Integer.parseInt(numberPart);

                // Push the tray label onto the top of the stack.
                // ArrayDeque.push() adds to the front (head), which we
                // treat as the top of our tray stack.
                stack.push(trayLabel);

                // "push" operations do NOT produce output — nothing added
                // to results.

            } else if (operation.equals("pop")) {
                // -------------------------------------------------------
                // POP operation
                //
                // Remove the tray from the top of the stack and record it.
                // If the stack is empty, record -1 (no tray to remove).
                // -------------------------------------------------------
                if (stack.isEmpty()) {
                    // Stack is empty — record the sentinel value -1.
                    results.add(-1);
                } else {
                    // stack.pop() removes and returns the element at the
                    // front (head) of the deque, which is our stack top.
                    int removedTray = stack.pop();
                    results.add(removedTray);
                }

            } else if (operation.equals("peek")) {
                // -------------------------------------------------------
                // PEEK operation
                //
                // Look at the top tray WITHOUT removing it, and record it.
                // If the stack is empty, record -1.
                // -------------------------------------------------------
                if (stack.isEmpty()) {
                    // Stack is empty — record the sentinel value -1.
                    results.add(-1);
                } else {
                    // stack.peek() returns the element at the front (head)
                    // of the deque without removing it.
                    int topTray = stack.peek();
                    results.add(topTray);
                }
            }
            // No else branch needed — the problem guarantees no malformed ops.
        }

        // ---------------------------------------------------------------
        // Step 4: Return the collected results list.
        // ---------------------------------------------------------------
        return results;
    }

    // ===================================================================
    // Main method — demonstrates the solution with the provided examples
    // and prints results to the console.
    // ===================================================================

    /**
     * Entry point for demonstration purposes.
     *
     * <p>Runs both examples from the problem description and prints the
     * expected vs. actual output so correctness can be verified at a glance.</p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        Solution solution = new Solution();

        // -------------------------------------------------------------------
        // Example 1 trace:
        //   "push 5"  → stack: [5]           (no output)
        //   "push 3"  → stack: [3, 5]        (no output)
        //   "peek"    → top is 3             → record 3;  stack: [3, 5]
        //   "pop"     → remove 3             → record 3;  stack: [5]
        //   "pop"     → remove 5             → record 5;  stack: []
        //   "pop"     → stack empty          → record -1; stack: []
        //
        // Expected output: [3, 3, 5, -1]
        // -------------------------------------------------------------------
        List<String> ops1 = Arrays.asList(
                "push 5", "push 3", "peek", "pop", "pop", "pop"
        );
        List<Integer> result1 = solution.simulateTrayStack(ops1);
        System.out.println("Example 1:");
        System.out.println("  Operations : " + ops1);
        System.out.println("  Expected   : [3, 3, 5, -1]");
        System.out.println("  Actual     : " + result1);
        System.out.println("  Correct?   : " + result1.equals(Arrays.asList(3, 3, 5, -1)));
        System.out.println();

        // -------------------------------------------------------------------
        // Example 2 trace:
        //   "pop"     → stack empty          → record -1; stack: []
        //   "push 10" → stack: [10]          (no output)
        //   "push 20" → stack: [20, 10]      (no output)
        //   "peek"    → top is 20            → record 20; stack: [20, 10]
        //   "pop"     → remove 20            → record 20; stack: [10]
        //
        // Expected output: [-1, 20, 20]
        // -------------------------------------------------------------------
        List<String> ops2 = Arrays.asList(
                "pop", "push 10", "push 20", "peek", "pop"
        );
        List<Integer> result2 = solution.simulateTrayStack(ops2);
        System.out.println("Example 2:");
        System.out.println("  Operations : " + ops2);
        System.out.println("  Expected   : [-1, 20, 20]");
        System.out.println("  Actual     : " + result2);
        System.out.println("  Correct?   : " + result2.equals(Arrays.asList(-1, 20, 20)));
        System.out.println();

        // -------------------------------------------------------------------
        // Extra edge-case: negative tray labels and consecutive peeks.
        //   "push -7" → stack: [-7]
        //   "peek"    → record -7; stack: [-7]
        //   "peek"    → record -7; stack: [-7]  (still there)
        //   "pop"     → record -7; stack: []
        //   "peek"    → stack empty → record -1
        //
        // Expected output: [-7, -7, -7, -1]
        // -------------------------------------------------------------------
        List<String> ops3 = Arrays.asList(
                "push -7", "peek", "peek", "pop", "peek"
        );
        List<Integer> result3 = solution.simulateTrayStack(ops3);
        System.out.println("Extra Edge Case (negative labels & consecutive peeks):");
        System.out.println("  Operations : " + ops3);
        System.out.println("  Expected   : [-7, -7, -7, -1]");
        System.out.println("  Actual     : " + result3);
        System.out.println("  Correct?   : " + result3.equals(Arrays.asList(-7, -7, -7, -1)));
    }
}