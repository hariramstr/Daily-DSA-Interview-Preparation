/*
 * ============================================================
 * Title: Cafeteria Tray Stack Simulator
 * ============================================================
 * Problem Description:
 * A cafeteria has a spring-loaded tray dispenser that works like a stack —
 * trays are pushed onto the top and popped from the top. However, the
 * cafeteria manager wants to serve trays in a specific order based on
 * customer requests.
 *
 * You are given a list of n trays numbered from 1 to n that arrive one-by-one
 * and are pushed onto the stack in order (tray 1 first, tray 2 second, etc.).
 * You are also given a requests array representing the order in which customers
 * want to receive trays.
 *
 * Determine if it is possible to serve all customers in the exact order
 * specified in requests using only push and pop operations on a single stack.
 * At each step, you may push the next arriving tray onto the stack, or pop the
 * top tray to serve a customer. Return true if the requested sequence is
 * achievable, otherwise return false.
 *
 * Constraints:
 *   - 1 <= n <= 1000
 *   - requests is a permutation of integers from 1 to n
 *   - You must use all n trays exactly once
 *
 * Example 1:
 *   Input:  n = 3, requests = [2, 1, 3]
 *   Output: true
 *   Explanation: Push 1, Push 2, Pop 2 (serve customer 1),
 *                Pop 1 (serve customer 2), Push 3, Pop 3 (serve customer 3).
 *
 * Example 2:
 *   Input:  n = 3, requests = [3, 1, 2]
 *   Output: false
 *   Explanation: To serve tray 3 first, we must push 1, 2, 3 then pop 3.
 *                Now tray 2 is on top but customer wants tray 1 next — impossible.
 * ============================================================
 */

using System;
using System.Collections.Generic;

// ---------------------------------------------------------------
// Solution Class
// Contains the algorithm to determine if the requested tray
// serving order is achievable using a single stack.
// ---------------------------------------------------------------
class Solution
{
    /// <summary>
    /// Determines whether the given requests sequence can be achieved
    /// by pushing trays 1..n onto a stack and popping them in order.
    ///
    /// Time Complexity:  O(n) — each tray is pushed and popped at most once.
    /// Space Complexity: O(n) — in the worst case, all trays sit on the stack
    ///                          before any are served (e.g., requests = [n, n-1, ..., 1]).
    /// </summary>
    /// <param name="n">Total number of trays (numbered 1 to n).</param>
    /// <param name="requests">The desired serving order for customers.</param>
    /// <returns>True if the sequence is achievable; false otherwise.</returns>
    public bool CanServe(int n, int[] requests)
    {
        // ---------------------------------------------------------------
        // STEP 1: Set up our simulation data structures.
        //
        // We need:
        //   - A stack to simulate the spring-loaded tray dispenser.
        //     A Stack<int> in C# gives us O(1) Push and Pop, which mirrors
        //     the physical stack behavior perfectly.
        //   - A pointer (nextTray) that tracks which tray arrives next.
        //     Trays arrive in order 1, 2, 3, ..., n.
        //   - A pointer (requestIndex) that tracks which customer we are
        //     currently trying to serve from the requests array.
        // ---------------------------------------------------------------
        Stack<int> stack = new Stack<int>();  // Simulates the physical tray dispenser
        int nextTray = 1;                     // The next tray number to push (starts at 1)
        int requestIndex = 0;                 // Index into the requests array

        // ---------------------------------------------------------------
        // STEP 2: Process each customer request one at a time.
        //
        // We loop until every customer has been served (requestIndex reaches
        // the end of the requests array).
        // ---------------------------------------------------------------
        while (requestIndex < requests.Length)
        {
            // The tray number the current customer wants to receive.
            int desiredTray = requests[requestIndex];

            // -----------------------------------------------------------
            // STEP 3: Push trays onto the stack until we have pushed the
            //         desired tray OR we have pushed all n trays.
            //
            // WHY? Because trays arrive in order 1, 2, 3, ... and we can
            // only serve a tray after it has been pushed. If the desired
            // tray hasn't arrived yet, we must push trays until it does.
            //
            // We keep pushing as long as:
            //   a) There are still trays left to push (nextTray <= n), AND
            //   b) The top of the stack is NOT yet the desired tray.
            //      (If the stack is empty, we definitely need to push more.)
            // -----------------------------------------------------------
            while (nextTray <= n && (stack.Count == 0 || stack.Peek() != desiredTray))
            {
                // Push the next arriving tray onto the stack.
                // This simulates a new tray being loaded into the dispenser.
                stack.Push(nextTray);

                // Advance the pointer so the next call pushes the tray after this one.
                nextTray++;
            }

            // -----------------------------------------------------------
            // STEP 4: Check if the top of the stack is the desired tray.
            //
            // After the pushing loop above, one of two things happened:
            //   A) The top of the stack IS the desired tray — great, we can
            //      pop it and serve this customer.
            //   B) We ran out of trays to push (nextTray > n) and the top
            //      of the stack is still NOT the desired tray. This means
            //      the desired tray was already popped earlier (served to
            //      a previous customer), which is impossible since requests
            //      is a permutation — OR the tray is buried under others
            //      with no way to reach it. Either way, the sequence is
            //      impossible, so we return false.
            // -----------------------------------------------------------
            if (stack.Count == 0 || stack.Peek() != desiredTray)
            {
                // The desired tray is not on top and we cannot push any more.
                // This requested order is NOT achievable.
                return false;
            }

            // -----------------------------------------------------------
            // STEP 5: Pop the desired tray from the stack to serve the customer.
            //
            // The top of the stack matches what the current customer wants.
            // We pop it (remove from dispenser) and serve it to the customer.
            // Then we move on to the next customer request.
            // -----------------------------------------------------------
            stack.Pop();          // Serve the tray to the current customer
            requestIndex++;       // Move to the next customer in the requests list
        }

        // ---------------------------------------------------------------
        // STEP 6: All customers have been served successfully.
        //
        // If we exit the while loop without returning false, it means every
        // customer received their desired tray in the correct order.
        // The sequence IS achievable — return true.
        // ---------------------------------------------------------------
        return true;
    }
}

// ---------------------------------------------------------------
// Demo / Test Code (Top-Level Statements)
// Traces through the examples from the problem description and
// prints results so you can verify correctness visually.
// ---------------------------------------------------------------

Solution solution = new Solution();

Console.WriteLine("=== Cafeteria Tray Stack Simulator ===");
Console.WriteLine();

// ---------------------------------------------------------------
// Example 1: n = 3, requests = [2, 1, 3]
// Expected Output: true
//
// Trace:
//   desiredTray = 2 → push 1, push 2 → top is 2 → pop 2 ✓
//   desiredTray = 1 → top is 1 → pop 1 ✓
//   desiredTray = 3 → push 3 → top is 3 → pop 3 ✓
//   All served → return true
// ---------------------------------------------------------------
int n1 = 3;
int[] requests1 = { 2, 1, 3 };
bool result1 = solution.CanServe(n1, requests1);
Console.WriteLine($"Example 1:");
Console.WriteLine($"  n = {n1}, requests = [{string.Join(", ", requests1)}]");
Console.WriteLine($"  Expected: true");
Console.WriteLine($"  Got:      {result1}");
Console.WriteLine($"  Pass: {result1 == true}");
Console.WriteLine();

// ---------------------------------------------------------------
// Example 2: n = 3, requests = [3, 1, 2]
// Expected Output: false
//
// Trace:
//   desiredTray = 3 → push 1, push 2, push 3 → top is 3 → pop 3 ✓
//   desiredTray = 1 → top is 2 (not 1), nextTray = 4 > n → return false ✗
// ---------------------------------------------------------------
int n2 = 3;
int[] requests2 = { 3, 1, 2 };
bool result2 = solution.CanServe(n2, requests2);
Console.WriteLine($"Example 2:");
Console.WriteLine($"  n = {n2}, requests = [{string.Join(", ", requests2)}]");
Console.WriteLine($"  Expected: false");
Console.WriteLine($"  Got:      {result2}");
Console.WriteLine($"  Pass: {result2 == false}");
Console.WriteLine();

// ---------------------------------------------------------------
// Additional Test Cases for robustness
// ---------------------------------------------------------------

// Test 3: Single tray — trivially true
int n3 = 1;
int[] requests3 = { 1 };
bool result3 = solution.CanServe(n3, requests3);
Console.WriteLine($"Example 3 (single tray):");
Console.WriteLine($"  n = {n3}, requests = [{string.Join(", ", requests3)}]");
Console.WriteLine($"  Expected: true");
Console.WriteLine($"  Got:      {result3}");
Console.WriteLine($"  Pass: {result3 == true}");
Console.WriteLine();

// Test 4: Reverse order — always achievable (push all, then pop all)
// requests = [5, 4, 3, 2, 1]: push 1-5, then pop in reverse order
int n4 = 5;
int[] requests4 = { 5, 4, 3, 2, 1 };
bool result4 = solution.CanServe(n4, requests4);
Console.WriteLine($"Example 4 (reverse order — push all then pop all):");
Console.WriteLine($"  n = {n4}, requests = [{string.Join(", ", requests4)}]");
Console.WriteLine($"  Expected: true");
Console.WriteLine($"  Got:      {result4}");
Console.WriteLine($"  Pass: {result4 == true}");
Console.WriteLine();

// Test 5: Natural order — always achievable (push one, pop one, repeat)
// requests = [1, 2, 3, 4, 5]: push 1 pop 1, push 2 pop 2, ...
int n5 = 5;
int[] requests5 = { 1, 2, 3, 4, 5 };
bool result5 = solution.CanServe(n5, requests5);
Console.WriteLine($"Example 5 (natural order — push one pop one):");
Console.WriteLine($"  n = {n5}, requests = [{string.Join(", ", requests5)}]");
Console.WriteLine($"  Expected: true");
Console.WriteLine($"  Got:      {result5}");
Console.WriteLine($"  Pass: {result5 == true}");
Console.WriteLine();

// Test 6: Impossible case with n=4
// requests = [4, 2, 3, 1]
// Push 1,2,3,4 → pop 4. Stack: [3,2,1] top=3. Want 2 → impossible.
int n6 = 4;
int[] requests6 = { 4, 2, 3, 1 };
bool result6 = solution.CanServe(n6, requests6);
Console.WriteLine($"Example 6 (impossible — buried tray):");
Console.WriteLine($"  n = {n6}, requests = [{string.Join(", ", requests6)}]");
Console.WriteLine($"  Expected: false");
Console.WriteLine($"  Got:      {result6}");
Console.WriteLine($"  Pass: {result6 == false}");
Console.WriteLine();

Console.WriteLine("=== All tests complete ===");