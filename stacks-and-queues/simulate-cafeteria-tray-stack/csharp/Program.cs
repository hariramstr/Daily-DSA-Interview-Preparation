/*
 * Title: Simulate a Cafeteria Tray Stack
 *
 * Problem Description:
 * A cafeteria uses a spring-loaded tray dispenser that works like a stack —
 * trays are added and removed from the top. You are given a list of operations
 * to simulate. Each operation is one of the following:
 *
 *   - "push X" — Place a tray with label X (an integer) onto the top of the stack.
 *   - "pop"    — Remove and record the tray from the top of the stack.
 *                If the stack is empty, record -1 instead.
 *   - "peek"   — Record the value of the tray on top without removing it.
 *                If the stack is empty, record -1.
 *
 * Return a list of integers representing the results of all "pop" and "peek"
 * operations in the order they appear.
 *
 * Constraints:
 *   - 1 <= operations.length <= 1000
 *   - Each "push" operation has a valid integer X where -10^4 <= X <= 10^4
 *   - There will be no malformed operations
 *
 * Example 1:
 *   Input:  ["push 5", "push 3", "peek", "pop", "pop", "pop"]
 *   Output: [3, 3, 5, -1]
 *
 * Example 2:
 *   Input:  ["pop", "push 10", "push 20", "peek", "pop"]
 *   Output: [-1, 20, 20]
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class containing the algorithm
// ─────────────────────────────────────────────────────────────────────────────
public class CafeteriaTrayStack
{
    /// <summary>
    /// Simulates a spring-loaded tray dispenser (a stack) given a list of
    /// string operations and returns the recorded results of every "pop" and
    /// "peek" operation.
    ///
    /// Time Complexity:  O(n)  — we process each operation exactly once.
    ///                           push / pop / peek on a Stack<T> are all O(1).
    /// Space Complexity: O(n)  — in the worst case every operation is a "push",
    ///                           so the stack can hold up to n trays.
    /// </summary>
    public List<int> SimulateTrays(string[] operations)
    {
        // ── Step 1: Create the data structures we need ────────────────────────
        //
        // We use C#'s built-in Stack<int> because it models a LIFO (Last-In,
        // First-Out) structure perfectly — exactly how a spring-loaded tray
        // dispenser works.  The most recently pushed item is always on "top".
        //
        // We also create a results list to collect the answers for every
        // "pop" and "peek" operation in the order they are encountered.
        Stack<int> trayStack = new Stack<int>();
        List<int> results    = new List<int>();

        // ── Step 2: Iterate over every operation in the input array ───────────
        //
        // We go through each operation one by one.  The order matters because
        // the state of the stack changes with every push/pop.
        foreach (string operation in operations)
        {
            // ── Step 3: Determine which command this operation represents ─────
            //
            // We check whether the operation string *starts with* a keyword
            // rather than doing an exact equality check, because "push X"
            // contains extra data (the tray label) after the keyword.
            //
            // Using StartsWith is safe and avoids splitting the string
            // unnecessarily for the "pop" and "peek" branches.

            if (operation.StartsWith("push"))
            {
                // ── Step 3a: Handle "push X" ──────────────────────────────────
                //
                // The format is always "push X" where X is an integer.
                // We need to extract X from the string.
                //
                // operation.Split(' ') produces ["push", "X"].
                // Index [1] gives us the numeric part as a string.
                // int.Parse converts it to an integer so we can store it.
                //
                // Why push? We are placing a new tray on TOP of the dispenser.
                // Stack.Push adds the item to the top — O(1) operation.
                string[] parts  = operation.Split(' ');
                int trayLabel   = int.Parse(parts[1]);

                trayStack.Push(trayLabel);
                // "push" produces no recorded result, so we do NOT add to results.
            }
            else if (operation == "pop")
            {
                // ── Step 3b: Handle "pop" ─────────────────────────────────────
                //
                // "pop" means: remove the tray on top and record its label.
                // If the stack is empty there is no tray to remove, so we
                // record -1 as a sentinel value indicating "empty dispenser".
                //
                // Stack.Count tells us how many items are currently in the stack.
                // Stack.Pop() removes AND returns the top item — O(1).
                if (trayStack.Count == 0)
                {
                    // Stack is empty — record the sentinel value -1.
                    results.Add(-1);
                }
                else
                {
                    // Stack has at least one tray — remove it and record it.
                    int removedTray = trayStack.Pop();
                    results.Add(removedTray);
                }
            }
            else if (operation == "peek")
            {
                // ── Step 3c: Handle "peek" ────────────────────────────────────
                //
                // "peek" means: look at the top tray WITHOUT removing it, and
                // record its label.  If the stack is empty, record -1.
                //
                // Stack.Peek() returns the top item WITHOUT removing it — O(1).
                // This is the key difference from "pop": the tray stays in place.
                if (trayStack.Count == 0)
                {
                    // Stack is empty — record the sentinel value -1.
                    results.Add(-1);
                }
                else
                {
                    // Stack has at least one tray — read the top without removing.
                    int topTray = trayStack.Peek();
                    results.Add(topTray);
                }
            }
            // Note: the problem guarantees no malformed operations, so we do
            // not need an else/error branch here.
        }

        // ── Step 4: Return the collected results ──────────────────────────────
        //
        // results now contains one integer for every "pop" or "peek" operation
        // encountered, in the exact order they appeared in the input.
        return results;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / driver code  (top-level statements — no explicit Main needed in .NET 6+)
// ─────────────────────────────────────────────────────────────────────────────

CafeteriaTrayStack solution = new CafeteriaTrayStack();

// ── Example 1 ─────────────────────────────────────────────────────────────────
// Trace:
//   "push 5"  → stack: [5]          (no output)
//   "push 3"  → stack: [5, 3]       (no output)
//   "peek"    → top = 3             → results: [3]
//   "pop"     → remove 3, stack:[5] → results: [3, 3]
//   "pop"     → remove 5, stack:[]  → results: [3, 3, 5]
//   "pop"     → empty → -1          → results: [3, 3, 5, -1]
// Expected output: [3, 3, 5, -1]
string[] ops1 = { "push 5", "push 3", "peek", "pop", "pop", "pop" };
List<int> result1 = solution.SimulateTrays(ops1);
Console.WriteLine("Example 1:");
Console.WriteLine("Input:    [\"push 5\", \"push 3\", \"peek\", \"pop\", \"pop\", \"pop\"]");
Console.WriteLine("Output:   [" + string.Join(", ", result1) + "]");
Console.WriteLine("Expected: [3, 3, 5, -1]");
Console.WriteLine();

// ── Example 2 ─────────────────────────────────────────────────────────────────
// Trace:
//   "pop"     → empty → -1           → results: [-1]
//   "push 10" → stack: [10]          (no output)
//   "push 20" → stack: [10, 20]      (no output)
//   "peek"    → top = 20             → results: [-1, 20]
//   "pop"     → remove 20, stack:[10]→ results: [-1, 20, 20]
// Expected output: [-1, 20, 20]
string[] ops2 = { "pop", "push 10", "push 20", "peek", "pop" };
List<int> result2 = solution.SimulateTrays(ops2);
Console.WriteLine("Example 2:");
Console.WriteLine("Input:    [\"pop\", \"push 10\", \"push 20\", \"peek\", \"pop\"]");
Console.WriteLine("Output:   [" + string.Join(", ", result2) + "]");
Console.WriteLine("Expected: [-1, 20, 20]");
Console.WriteLine();

// ── Additional edge-case test ─────────────────────────────────────────────────
// All operations on an empty stack should return -1.
string[] ops3 = { "pop", "peek", "pop" };
List<int> result3 = solution.SimulateTrays(ops3);
Console.WriteLine("Edge Case (all ops on empty stack):");
Console.WriteLine("Input:    [\"pop\", \"peek\", \"pop\"]");
Console.WriteLine("Output:   [" + string.Join(", ", result3) + "]");
Console.WriteLine("Expected: [-1, -1, -1]");
Console.WriteLine();

// ── Additional test with negative tray labels ─────────────────────────────────
string[] ops4 = { "push -5", "push -3", "peek", "pop", "pop" };
List<int> result4 = solution.SimulateTrays(ops4);
Console.WriteLine("Negative Labels Test:");
Console.WriteLine("Input:    [\"push -5\", \"push -3\", \"peek\", \"pop\", \"pop\"]");
Console.WriteLine("Output:   [" + string.Join(", ", result4) + "]");
Console.WriteLine("Expected: [-3, -3, -5]");