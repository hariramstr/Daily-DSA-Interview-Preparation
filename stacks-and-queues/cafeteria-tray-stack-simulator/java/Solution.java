/*
 * Cafeteria Tray Stack Simulator
 * ================================
 * A cafeteria has a spring-loaded tray dispenser that works like a stack —
 * trays are pushed onto the top and popped from the top. However, the cafeteria
 * manager wants to serve trays in a specific order based on customer requests.
 *
 * You are given a list of n trays numbered from 1 to n that arrive one-by-one
 * and are pushed onto the stack in order (tray 1 first, tray 2 second, etc.).
 * You are also given a requests array representing the order in which customers
 * want to receive trays.
 *
 * Determine if it is possible to serve all customers in the exact order specified
 * in requests using only push and pop operations on a single stack. At each step,
 * you may push the next arriving tray onto the stack, or pop the top tray to serve
 * a customer. Return true if the requested sequence is achievable, otherwise false.
 *
 * Constraints:
 * - 1 <= n <= 1000
 * - requests is a permutation of integers from 1 to n
 * - You must use all n trays exactly once
 *
 * Example 1:
 * Input: n = 3, requests = [2, 1, 3]
 * Output: true
 * Explanation: Push 1, Push 2, Pop 2 (serve customer 1), Pop 1 (serve customer 2),
 *              Push 3, Pop 3 (serve customer 3).
 *
 * Example 2:
 * Input: n = 3, requests = [3, 1, 2]
 * Output: false
 * Explanation: To serve tray 3 first, we must push 1, 2, 3 then pop 3. Now tray 2
 *              is on top but customer wants tray 1 next — impossible.
 */

import java.util.Stack;

/**
 * Solution class for the Cafeteria Tray Stack Simulator problem.
 *
 * <p>Core Idea:
 * We simulate the push/pop operations on a stack. Trays arrive in order 1..n.
 * At each moment, we can either push the next arriving tray OR pop the top tray
 * to fulfill the next customer request. We greedily try to satisfy each request
 * in order: keep pushing trays until the top of the stack matches the current
 * request, then pop it. If at any point the top doesn't match and we've already
 * pushed past the needed tray, it's impossible.
 */
public class Solution {

    /**
     * Determines whether the given sequence of tray requests can be fulfilled
     * using a single stack, where trays are pushed in order from 1 to n.
     *
     * <p>Algorithm Overview:
     * <ol>
     *   <li>Use a stack to simulate the tray dispenser.</li>
     *   <li>Keep a pointer {@code nextTray} tracking the next tray to push (starts at 1).</li>
     *   <li>For each requested tray in the requests array:
     *     <ul>
     *       <li>Push trays onto the stack until the top equals the requested tray
     *           OR we've pushed all n trays.</li>
     *       <li>If the top of the stack equals the requested tray, pop it (serve the customer).</li>
     *       <li>Otherwise, it's impossible — return false.</li>
     *     </ul>
     *   </li>
     *   <li>If all requests are satisfied, return true.</li>
     * </ol>
     *
     * @param n        the total number of trays (trays are numbered 1 to n)
     * @param requests an array representing the desired serving order (a permutation of 1..n)
     * @return {@code true} if the requests can be fulfilled in order; {@code false} otherwise
     *
     * Time Complexity:  O(n) — each tray is pushed and popped at most once
     * Space Complexity: O(n) — the stack can hold at most n trays at once
     */
    public boolean canServe(int n, int[] requests) {

        // ---------------------------------------------------------------
        // Step 1: Create the simulation stack.
        // This stack represents the spring-loaded tray dispenser.
        // ---------------------------------------------------------------
        Stack<Integer> stack = new Stack<>();

        // ---------------------------------------------------------------
        // Step 2: Initialize the "next tray to push" pointer.
        // Trays arrive in order: 1, 2, 3, ..., n.
        // nextTray tells us which tray would be pushed next.
        // ---------------------------------------------------------------
        int nextTray = 1;

        // ---------------------------------------------------------------
        // Step 3: Process each customer request one by one.
        // requestIndex iterates over the requests array.
        // ---------------------------------------------------------------
        for (int requestIndex = 0; requestIndex < requests.length; requestIndex++) {

            // The tray this customer wants to receive
            int desiredTray = requests[requestIndex];

            // -----------------------------------------------------------
            // Step 4: Push trays onto the stack until either:
            //   (a) The top of the stack equals the desired tray, OR
            //   (b) We've pushed all n trays (nextTray > n)
            //
            // We MUST push trays in order; we cannot skip a tray.
            // So if the desired tray hasn't arrived yet, we keep pushing.
            // -----------------------------------------------------------
            while ((stack.isEmpty() || stack.peek() != desiredTray) && nextTray <= n) {
                // Push the next arriving tray onto the stack
                stack.push(nextTray);

                // Advance the pointer to the next tray in the arrival sequence
                nextTray++;
            }

            // -----------------------------------------------------------
            // Step 5: Check if the top of the stack is the desired tray.
            //
            // After the while loop above, one of two things happened:
            //   (a) stack.peek() == desiredTray  → we can serve this customer
            //   (b) nextTray > n and top != desired → the tray we need is
            //       buried under other trays — impossible to serve in this order
            // -----------------------------------------------------------
            if (!stack.isEmpty() && stack.peek() == desiredTray) {
                // The top tray matches what the customer wants — pop and serve it!
                stack.pop();
            } else {
                // The desired tray is not on top and we can't push any more trays.
                // This means the desired tray is buried in the stack and cannot
                // be reached without removing trays the customer doesn't want yet.
                // Therefore, this request sequence is IMPOSSIBLE.
                return false;
            }
        }

        // ---------------------------------------------------------------
        // Step 6: All customer requests were satisfied successfully.
        // Return true to indicate the sequence is achievable.
        // ---------------------------------------------------------------
        return true;
    }

    /**
     * Main method to demonstrate and test the {@link #canServe(int, int[])} method
     * with the examples provided in the problem description and additional edge cases.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        System.out.println("=== Cafeteria Tray Stack Simulator ===");
        System.out.println();

        // ------------------------------------------------------------------
        // Example 1: n=3, requests=[2,1,3]  → Expected: true
        //
        // Trace:
        //   Request 2: push 1, push 2 → stack=[1,2], top=2 → pop 2 ✓  stack=[1]
        //   Request 1: top=1 → pop 1 ✓  stack=[]
        //   Request 3: push 3 → stack=[3], top=3 → pop 3 ✓  stack=[]
        //   All served → true
        // ------------------------------------------------------------------
        int n1 = 3;
        int[] requests1 = {2, 1, 3};
        boolean result1 = solution.canServe(n1, requests1);
        System.out.println("Example 1:");
        System.out.println("  n = " + n1 + ", requests = [2, 1, 3]");
        System.out.println("  Expected: true");
        System.out.println("  Got:      " + result1);
        System.out.println("  PASS: " + (result1 == true));
        System.out.println();

        // ------------------------------------------------------------------
        // Example 2: n=3, requests=[3,1,2]  → Expected: false
        //
        // Trace:
        //   Request 3: push 1, push 2, push 3 → stack=[1,2,3], top=3 → pop 3 ✓  stack=[1,2]
        //   Request 1: top=2 ≠ 1, nextTray=4 > n → IMPOSSIBLE → false
        // ------------------------------------------------------------------
        int n2 = 3;
        int[] requests2 = {3, 1, 2};
        boolean result2 = solution.canServe(n2, requests2);
        System.out.println("Example 2:");
        System.out.println("  n = " + n2 + ", requests = [3, 1, 2]");
        System.out.println("  Expected: false");
        System.out.println("  Got:      " + result2);
        System.out.println("  PASS: " + (result2 == false));
        System.out.println();

        // ------------------------------------------------------------------
        // Additional Test 1: n=1, requests=[1]  → Expected: true
        // Only one tray, push it and pop it immediately.
        // ------------------------------------------------------------------
        int n3 = 1;
        int[] requests3 = {1};
        boolean result3 = solution.canServe(n3, requests3);
        System.out.println("Additional Test 1 (single tray):");
        System.out.println("  n = " + n3 + ", requests = [1]");
        System.out.println("  Expected: true");
        System.out.println("  Got:      " + result3);
        System.out.println("  PASS: " + (result3 == true));
        System.out.println();

        // ------------------------------------------------------------------
        // Additional Test 2: n=4, requests=[1,2,3,4]  → Expected: true
        // Push 1, pop 1, push 2, pop 2, push 3, pop 3, push 4, pop 4.
        // (FIFO order — always pop immediately after push)
        // ------------------------------------------------------------------
        int n4 = 4;
        int[] requests4 = {1, 2, 3, 4};
        boolean result4 = solution.canServe(n4, requests4);
        System.out.println("Additional Test 2 (FIFO order):");
        System.out.println("  n = " + n4 + ", requests = [1, 2, 3, 4]");
        System.out.println("  Expected: true");
        System.out.println("  Got:      " + result4);
        System.out.println("  PASS: " + (result4 == true));
        System.out.println();

        // ------------------------------------------------------------------
        // Additional Test 3: n=4, requests=[4,3,2,1]  → Expected: true
        // Push all 4 trays, then pop in reverse order (pure LIFO).
        // ------------------------------------------------------------------
        int n5 = 4;
        int[] requests5 = {4, 3, 2, 1};
        boolean result5 = solution.canServe(n5, requests5);
        System.out.println("Additional Test 3 (LIFO / reverse order):");
        System.out.println("  n = " + n5 + ", requests = [4, 3, 2, 1]");
        System.out.println("  Expected: true");
        System.out.println("  Got:      " + result5);
        System.out.println("  PASS: " + (result5 == true));
        System.out.println();

        // ------------------------------------------------------------------
        // Additional Test 4: n=5, requests=[2,4,3,5,1]  → Expected: true
        //
        // Trace:
        //   Request 2: push 1, push 2 → top=2 → pop 2  stack=[1]
        //   Request 4: push 3, push 4 → top=4 → pop 4  stack=[1,3]
        //   Request 3: top=3 → pop 3  stack=[1]
        //   Request 5: push 5 → top=5 → pop 5  stack=[1]
        //   Request 1: top=1 → pop 1  stack=[]
        //   → true
        // ------------------------------------------------------------------
        int n6 = 5;
        int[] requests6 = {2, 4, 3, 5, 1};
        boolean result6 = solution.canServe(n6, requests6);
        System.out.println("Additional Test 4 (mixed order):");
        System.out.println("  n = " + n6 + ", requests = [2, 4, 3, 5, 1]");
        System.out.println("  Expected: true");
        System.out.println("  Got:      " + result6);
        System.out.println("  PASS: " + (result6 == true));
        System.out.println();

        // ------------------------------------------------------------------
        // Additional Test 5: n=5, requests=[2,5,3,4,1]  → Expected: false
        //
        // Trace:
        //   Request 2: push 1, push 2 → top=2 → pop 2  stack=[1]
        //   Request 5: push 3, push 4, push 5 → top=5 → pop 5  stack=[1,3,4]
        //   Request 3: top=4 ≠ 3, nextTray=6 > n → IMPOSSIBLE → false
        // ------------------------------------------------------------------
        int n7 = 5;
        int[] requests7 = {2, 5, 3, 4, 1};
        boolean result7 = solution.canServe(n7, requests7);
        System.out.println("Additional Test 5 (impossible mixed order):");
        System.out.println("  n = " + n7 + ", requests = [2, 5, 3, 4, 1]");
        System.out.println("  Expected: false");
        System.out.println("  Got:      " + result7);
        System.out.println("  PASS: " + (result7 == false));
        System.out.println();

        System.out.println("=== All tests completed ===");
    }
}