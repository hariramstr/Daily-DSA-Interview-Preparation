/*
 * Title: Flatten a Multilevel Sparse Linked List by Depth
 *
 * Problem Description:
 * You are given the head of a multilevel linked list. Each node contains an integer value,
 * a `next` pointer to the next node at the same level, and a `child` pointer that may point
 * to the head of another linked list (creating a new sub-level).
 *
 * Your task is to flatten the multilevel linked list into a single-level linked list such that
 * nodes are ordered BY THEIR DEPTH FIRST, then by their original left-to-right order within
 * each depth level.
 *
 * In other words:
 *   - Collect all nodes at depth 1 (the top level) in order
 *   - Then all nodes at depth 2 in order (left-to-right across all sub-lists at that depth)
 *   - Then depth 3, and so on.
 *
 * Return the head of the resulting flattened list. The `child` pointers should be set to null.
 *
 * Example 1:
 *   Input: 1 - 2 - 3, node 2 has child 4 - 5, node 5 has child 6 - 7
 *   Depth 1: [1, 2, 3]
 *   Depth 2: [4, 5]
 *   Depth 3: [6, 7]
 *   Output: 1 -> 2 -> 3 -> 4 -> 5 -> 6 -> 7
 *
 * Example 2:
 *   Input: 10 - 20 - 30, node 10 has child 11 - 12, node 20 has child 21 - 22, node 12 has child 100
 *   Depth 1: [10, 20, 30]
 *   Depth 2: [11, 12, 21, 22]
 *   Depth 3: [100]
 *   Output: 10 -> 20 -> 30 -> 11 -> 12 -> 21 -> 22 -> 100
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Node definition for the multilevel linked list
// ─────────────────────────────────────────────────────────────────────────────
public class Node
{
    public int Val;
    public Node? Next;   // points to the next node at the same level
    public Node? Child;  // points to the head of a sub-list (one level deeper)

    public Node(int val)
    {
        Val = val;
        Next = null;
        Child = null;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Solution class
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /*
     * Method: Flatten
     *
     * Approach: Breadth-First Traversal (BFS) by depth level
     *
     * Key Insight:
     *   The problem asks us to order nodes by depth first, then left-to-right
     *   within each depth. This is exactly a Breadth-First Search (BFS) pattern!
     *
     *   We use a Queue<Node> where each entry is the HEAD of a sub-list at a
     *   particular depth. We process one depth level at a time:
     *     1. Dequeue all sub-list heads that belong to the current depth.
     *     2. Walk each sub-list left-to-right, collecting node values AND
     *        enqueuing any child sub-list heads for the next depth.
     *     3. Move to the next depth and repeat.
     *
     * Why a queue of sub-list heads (not individual nodes)?
     *   At each depth there can be MULTIPLE sub-lists (e.g., depth 2 in Example 2
     *   has two separate sub-lists: [11,12] and [21,22]). We need to process them
     *   in the order their parent nodes appear, which is exactly the order we
     *   discover them while walking left-to-right at the parent depth.
     *
     * Time Complexity:  O(N) — every node is visited exactly once.
     * Space Complexity: O(W) — where W is the maximum number of sub-list heads
     *                          alive at any one depth level (at most O(N) in the
     *                          worst case, but typically much smaller).
     */
    public Node? Flatten(Node? head)
    {
        // ── Edge case: empty list ──────────────────────────────────────────
        // If the input is null there is nothing to flatten; return null.
        if (head == null) return null;

        // ── Step 1: Set up the BFS queue ───────────────────────────────────
        // The queue stores the HEAD NODE of each sub-list we still need to
        // process. We seed it with the top-level list's head.
        //
        // Why Queue<Node>? A queue gives us FIFO ordering, which ensures we
        // process sub-lists in the exact left-to-right, depth-by-depth order
        // the problem requires.
        Queue<Node> subListQueue = new Queue<Node>();
        subListQueue.Enqueue(head);

        // ── Step 2: Build the result list using a dummy head ───────────────
        // A dummy (sentinel) node simplifies appending: we never have to
        // special-case the very first real node.
        Node dummy = new Node(0);
        Node tail = dummy; // 'tail' always points to the last node added so far

        // ── Step 3: BFS loop — process one depth level per outer iteration ─
        // We continue as long as there are sub-list heads waiting to be processed.
        while (subListQueue.Count > 0)
        {
            // ── Step 3a: Snapshot how many sub-lists belong to THIS depth ──
            // At the start of each depth level, the queue contains exactly the
            // sub-list heads for that level. We capture the count NOW so that
            // any child sub-lists we enqueue during this iteration are treated
            // as the NEXT depth level, not the current one.
            int subListsAtThisDepth = subListQueue.Count;

            // ── Step 3b: Process every sub-list at the current depth ───────
            for (int i = 0; i < subListsAtThisDepth; i++)
            {
                // Dequeue the head of the next sub-list at this depth.
                Node current = subListQueue.Dequeue();

                // Walk every node in this sub-list from left to right.
                while (current != null)
                {
                    // ── Step 3c: Append this node to the result list ───────
                    // Link the current node after 'tail' and advance 'tail'.
                    tail.Next = current;
                    tail = current;

                    // ── Step 3d: Capture child before we overwrite pointers ─
                    // If this node has a child sub-list, we need to remember it
                    // so we can enqueue it for the next depth level.
                    // We capture it NOW before we clear the child pointer.
                    Node? childHead = current.Child;

                    // ── Step 3e: Clear the child pointer ──────────────────
                    // The problem requires the flattened list to have no child
                    // pointers. We clear it here.
                    current.Child = null;

                    // ── Step 3f: Advance to the next node in this sub-list ─
                    // We move 'current' to current.Next BEFORE we potentially
                    // overwrite current.Next (we don't overwrite it here, but
                    // it's good practice to advance early).
                    current = current.Next!;

                    // ── Step 3g: Enqueue the child sub-list (if any) ───────
                    // If there was a child, add its head to the queue so it
                    // will be processed at the next depth level.
                    // The order of enqueuing matches left-to-right parent order,
                    // which is exactly what we want.
                    if (childHead != null)
                    {
                        subListQueue.Enqueue(childHead);
                    }
                }
                // After the while loop, we have processed all nodes in this
                // sub-list and enqueued all their children.
            }
            // After the for loop, we have processed all sub-lists at this depth.
            // The queue now contains only sub-list heads for the NEXT depth.
        }

        // ── Step 4: Terminate the result list ─────────────────────────────
        // 'tail' is the last real node. Its 'next' might still point somewhere
        // from the original structure, so we explicitly set it to null.
        tail.Next = null;

        // ── Step 5: Return the real head (skip the dummy node) ────────────
        return dummy.Next;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helper utilities (build lists, print lists)
// ─────────────────────────────────────────────────────────────────────────────
static class ListHelper
{
    /// <summary>
    /// Builds a simple horizontal linked list from an array of values.
    /// Returns the head node.
    /// </summary>
    public static Node BuildList(int[] values)
    {
        Node dummy = new Node(0);
        Node tail = dummy;
        foreach (int v in values)
        {
            tail.Next = new Node(v);
            tail = tail.Next;
        }
        return dummy.Next!;
    }

    /// <summary>
    /// Prints a flattened (single-level) linked list as "v1 -> v2 -> ... -> null".
    /// </summary>
    public static void PrintList(Node? head)
    {
        var parts = new List<string>();
        while (head != null)
        {
            parts.Add(head.Val.ToString());
            head = head.Next;
        }
        parts.Add("null");
        Console.WriteLine(string.Join(" -> ", parts));
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Test code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────
Solution solution = new Solution();

// ══════════════════════════════════════════════════════════════════════════════
// Example 1
// Structure:
//   Level 1:  1 - 2 - 3
//   Level 2:      4 - 5        (child of node 2)
//   Level 3:          6 - 7   (child of node 5)
//
// Expected output: 1 -> 2 -> 3 -> 4 -> 5 -> 6 -> 7 -> null
// ══════════════════════════════════════════════════════════════════════════════
Console.WriteLine("=== Example 1 ===");

// Build level-1 list: 1 - 2 - 3
Node e1_1 = new Node(1);
Node e1_2 = new Node(2);
Node e1_3 = new Node(3);
e1_1.Next = e1_2;
e1_2.Next = e1_3;

// Build level-2 list: 4 - 5  (attach as child of node 2)
Node e1_4 = new Node(4);
Node e1_5 = new Node(5);
e1_4.Next = e1_5;
e1_2.Child = e1_4;  // node 2's child -> 4

// Build level-3 list: 6 - 7  (attach as child of node 5)
Node e1_6 = new Node(6);
Node e1_7 = new Node(7);
e1_6.Next = e1_7;
e1_5.Child = e1_6;  // node 5's child -> 6

Console.Write("Input structure built. Flattened result: ");
Node? result1 = solution.Flatten(e1_1);
ListHelper.PrintList(result1);
// Expected: 1 -> 2 -> 3 -> 4 -> 5 -> 6 -> 7 -> null

Console.WriteLine();

// ══════════════════════════════════════════════════════════════════════════════
// Example 2
// Structure:
//   Level 1:  10 - 20 - 30
//   Level 2:  11 - 12        (child of node 10)
//             21 - 22        (child of node 20)
//   Level 3:       100       (child of node 12)
//
// Expected output: 10 -> 20 -> 30 -> 11 -> 12 -> 21 -> 22 -> 100 -> null
// ══════════════════════════════════════════════════════════════════════════════
Console.WriteLine("=== Example 2 ===");

// Build level-1 list: 10 - 20 - 30
Node e2_10 = new Node(10);
Node e2_20 = new Node(20);
Node e2_30 = new Node(30);
e2_10.Next = e2_20;
e2_20.Next = e2_30;

// Build level-2 sub-list under node 10: 11 - 12
Node e2_11 = new Node(11);
Node e2_12 = new Node(12);
e2_11.Next = e2_12;
e2_10.Child = e2_11;  // node 10's child -> 11

// Build level-2 sub-list under node 20: 21 - 22
Node e2_21 = new Node(21);
Node e2_22 = new Node(22);
e2_21.Next = e2_22;
e2_20.Child = e2_21;  // node 20's child -> 21

// Build level-3 sub-list under node 12: 100
Node e2_100 = new Node(100);
e2_12.Child = e2_100; // node 12's child -> 100

Console.Write("Input structure built. Flattened result: ");
Node? result2 = solution.Flatten(e2_10);
ListHelper.PrintList(result2);
// Expected: 10 -> 20 -> 30 -> 11 -> 12 -> 21 -> 22 -> 100 -> null

Console.WriteLine();

// ══════════════════════════════════════════════════════════════════════════════
// Example 3 — Single node (edge case)
// ══════════════════════════════════════════════════════════════════════════════
Console.WriteLine("=== Example 3: Single node ===");
Node single = new Node(42);
Console.Write("Flattened result: ");
ListHelper.PrintList(solution.Flatten(single));
// Expected: 42 -> null

Console.WriteLine();

// ══════════════════════════════════════════════════════════════════════════════
// Example 4 — Deeper nesting: 3 levels, multiple children at each level
// Structure:
//   Level 1:  A(1) - B(2)
//   Level 2:  C(3) - D(4)   (child of A)
//             E(5)           (child of B)
//   Level 3:  F(6)           (child of C)
//             G(7) - H(8)   (child of E)
//
// Depth 1: [1, 2]
// Depth 2: [3, 4, 5]
// Depth 3: [6, 7, 8]
// Expected: 1 -> 2 -> 3 -> 4 -> 5 -> 6 -> 7 -> 8 -> null
// ══════════════════════════════════════════════════════════════════════════════