/*
 * Title: Linked List Time Travel Snapshots
 * Difficulty: Hard
 * Topic: Linked Lists
 *
 * Problem Description:
 * You are given a singly linked list where each node contains an integer value and a timestamp.
 * The list is sorted in ascending order by timestamp. You are also given a list of query operations,
 * each of which is one of the following:
 *
 * - INSERT t v: Insert a new node with timestamp t and value v into the correct sorted position.
 * - SNAPSHOT t: Return the sum of values of all nodes with timestamps <= t at the time of this query.
 * - ROLLBACK t: Remove all nodes with timestamps strictly greater than t from the list.
 *
 * After processing all operations, return an array of integers — one result for each SNAPSHOT query.
 *
 * Constraints:
 * - 1 <= number of operations <= 10^5
 * - 1 <= t <= 10^9
 * - -10^4 <= v <= 10^4
 * - Timestamps in INSERT operations are not necessarily unique
 * - The initial linked list may be empty
 * - ROLLBACK does not affect nodes with timestamps equal to t
 * - The answer for each SNAPSHOT query fits in a 32-bit signed integer
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Node definition for our singly linked list
// Each node stores a timestamp and a value, plus a pointer to the next node.
// ─────────────────────────────────────────────────────────────────────────────
public class ListNode
{
    public int Timestamp;   // When this data point was recorded
    public int Value;       // The data value at that timestamp
    public ListNode? Next;  // Pointer to the next node (null if tail)

    public ListNode(int timestamp, int value)
    {
        Timestamp = timestamp;
        Value = value;
        Next = null;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// The main solution class
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /*
     * Method: ProcessOperations
     *
     * Time Complexity:
     *   - INSERT: O(n) per operation in the worst case (we must find the correct
     *     sorted position by traversing the list).
     *   - SNAPSHOT: O(n) per operation (we traverse nodes with timestamp <= t).
     *   - ROLLBACK: O(n) per operation (we traverse to find the cut-off point).
     *   Overall: O(q * n) where q = number of operations, n = list length.
     *
     * Space Complexity:
     *   - O(n) for the linked list itself (n = total nodes at any point).
     *   - O(q) for the results list.
     *   - We use a sentinel (dummy) head node to simplify edge cases — O(1) extra.
     *
     * Why a linked list instead of an array/list?
     *   The problem explicitly asks us to model a linked list. Insertions in the
     *   middle of a linked list are O(1) once you have the predecessor node
     *   (no shifting needed as with arrays). Rollback (truncation) is also O(1)
     *   once you find the cut point — just set predecessor.Next = null.
     *
     * Design choice — sentinel (dummy) head node:
     *   A dummy head node with timestamp = 0 (before any real timestamp) sits
     *   permanently at the front. This eliminates special-case code for inserting
     *   at the very beginning or rolling back to an empty list, because we always
     *   have at least one node to start our traversal from.
     */
    public int[] ProcessOperations(
        (int timestamp, int value)[] initialNodes,
        string[] operations)
    {
        // ── Step 1: Build the initial linked list ──────────────────────────────
        // We create a sentinel (dummy) head node. Its timestamp is 0, which is
        // less than any valid timestamp (constraints say t >= 1). This means the
        // sentinel will never be removed by a ROLLBACK and never counted by a
        // SNAPSHOT (we only count nodes with timestamp <= query_t, and the
        // sentinel's value is 0 anyway).
        ListNode dummy = new ListNode(0, 0);
        ListNode tail = dummy; // 'tail' tracks the last node for fast appending

        // Insert each initial node at the end. The problem states the initial
        // list is already sorted by timestamp, so we can simply append.
        foreach (var (ts, val) in initialNodes)
        {
            // Create a new node and link it after the current tail
            tail.Next = new ListNode(ts, val);
            tail = tail.Next; // Advance tail to the newly added node
        }

        // ── Step 2: Process each operation ────────────────────────────────────
        // We collect results only for SNAPSHOT operations.
        List<int> results = new List<int>();

        foreach (string op in operations)
        {
            // Split the operation string into tokens.
            // e.g. "INSERT 4 15" → ["INSERT", "4", "15"]
            // e.g. "SNAPSHOT 4"  → ["SNAPSHOT", "4"]
            // e.g. "ROLLBACK 3"  → ["ROLLBACK", "3"]
            string[] tokens = op.Split(' ');
            string command = tokens[0];

            if (command == "INSERT")
            {
                // ── INSERT t v ─────────────────────────────────────────────
                // Parse the timestamp and value from the tokens.
                int insertTime = int.Parse(tokens[1]);
                int insertVal  = int.Parse(tokens[2]);

                // We need to find the correct position to insert so that the
                // list remains sorted by timestamp (ascending).
                //
                // Strategy: Walk from the dummy head. Keep moving forward as
                // long as the NEXT node's timestamp is <= insertTime.
                // When we stop, 'current' is the last node whose timestamp is
                // <= insertTime, so the new node goes right after 'current'.
                //
                // Why <= and not <? Because multiple nodes can share the same
                // timestamp. We insert after all existing nodes with the same
                // timestamp (append to the group), which keeps relative order
                // stable and is consistent with the problem examples.
                ListNode current = dummy;
                while (current.Next != null && current.Next.Timestamp <= insertTime)
                {
                    current = current.Next;
                }

                // Now insert the new node between 'current' and 'current.Next'.
                ListNode newNode = new ListNode(insertTime, insertVal);
                newNode.Next = current.Next; // New node points to what was after current
                current.Next = newNode;      // Current now points to new node

                // IMPORTANT: If we inserted at the very end (current.Next was null
                // before insertion, meaning newNode.Next is null), we must update
                // 'tail' so future appends remain O(1).
                // However, since ROLLBACK can shorten the list and invalidate 'tail',
                // we'll recompute 'tail' lazily only when needed, OR we can just
                // update it here when we detect we inserted at the end.
                if (newNode.Next == null)
                {
                    tail = newNode;
                }
            }
            else if (command == "SNAPSHOT")
            {
                // ── SNAPSHOT t ─────────────────────────────────────────────
                // Sum the values of all nodes with timestamp <= t.
                //
                // We traverse from the first real node (dummy.Next) forward,
                // accumulating the sum as long as the node's timestamp <= t.
                // Because the list is sorted, once we see a timestamp > t we
                // can stop immediately (all subsequent nodes will also be > t).
                int queryTime = int.Parse(tokens[1]);
                int sum = 0;

                ListNode cur = dummy.Next; // Start at the first real node
                while (cur != null && cur.Timestamp <= queryTime)
                {
                    sum += cur.Value; // Add this node's value to the running sum
                    cur = cur.Next;   // Move to the next node
                }

                // Record this snapshot result
                results.Add(sum);
            }
            else if (command == "ROLLBACK")
            {
                // ── ROLLBACK t ─────────────────────────────────────────────
                // Remove all nodes with timestamp STRICTLY GREATER than t.
                // Nodes with timestamp == t are kept (per the problem statement).
                //
                // Strategy: Walk from the dummy head. Keep moving forward as
                // long as the NEXT node's timestamp is <= t (those nodes stay).
                // When we stop, 'current' is the last node to keep.
                // We then set current.Next = null, severing the rest of the list.
                //
                // The garbage collector will reclaim the removed nodes since
                // nothing references them anymore.
                int rollbackTime = int.Parse(tokens[1]);

                ListNode current = dummy;
                while (current.Next != null && current.Next.Timestamp <= rollbackTime)
                {
                    current = current.Next;
                }

                // 'current' is now the last node with timestamp <= rollbackTime.
                // Cut off everything after it.
                current.Next = null;

                // Update 'tail' to reflect the new end of the list.
                // This is important so that future INSERT operations that check
                // newNode.Next == null correctly update 'tail'.
                tail = current;
            }
            // (No other command types per the problem specification)
        }

        // ── Step 3: Return results as an array ────────────────────────────────
        return results.ToArray();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Test Code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

Solution sol = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// Initial list: [(1,10), (3,20), (5,30)]
// Operations:   ["INSERT 4 15", "SNAPSHOT 4", "ROLLBACK 3", "SNAPSHOT 4"]
// Expected:     [75, 30]
//
// Trace:
//   Start:        dummy(0,0) -> (1,10) -> (3,20) -> (5,30)
//   INSERT 4 15:  dummy(0,0) -> (1,10) -> (3,20) -> (4,15) -> (5,30)
//   SNAPSHOT 4:   sum of t<=4: 10+20+15 = 75  ✓
//   ROLLBACK 3:   remove t>3: dummy(0,0) -> (1,10) -> (3,20)
//   SNAPSHOT 4:   sum of t<=4: 10+20 = 30  ✓

Console.WriteLine("=== Example 1 ===");
var initial1 = new (int, int)[] { (1, 10), (3, 20), (5, 30) };
var ops1 = new string[] { "INSERT 4 15", "SNAPSHOT 4", "ROLLBACK 3", "SNAPSHOT 4" };
int[] result1 = sol.ProcessOperations(initial1, ops1);
Console.WriteLine("Output:   [" + string.Join(", ", result1) + "]");
Console.WriteLine("Expected: [75, 30]");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// Initial list: []
// Operations:   ["INSERT 2 5", "INSERT 2 3", "SNAPSHOT 2",
//                "INSERT 4 10", "ROLLBACK 2", "SNAPSHOT 5"]
// Expected:     [8, 8]
//
// Trace:
//   Start:          dummy(0,0)
//   INSERT 2 5:     dummy(0,0) -> (2,5)
//   INSERT 2 3:     dummy(0,0) -> (2,5) -> (2,3)   [appended after existing t=2]
//   SNAPSHOT 2:     sum of t<=2: 5+3 = 8  ✓
//   INSERT 4 10:    dummy(0,0) -> (2,5) -> (2,3) -> (4,10)
//   ROLLBACK 2:     remove t>2: dummy(0,0) -> (2,5) -> (2,3)
//   SNAPSHOT 5:     sum of t<=5: 5+3 = 8  ✓

Console.WriteLine("=== Example 2 ===");
var initial2 = new (int, int)[] { };
var ops2 = new string[]
{
    "INSERT 2 5", "INSERT 2 3", "SNAPSHOT 2",
    "INSERT 4 10", "ROLLBACK 2", "SNAPSHOT 5"
};
int[] result2 = sol.ProcessOperations(initial2, ops2);
Console.WriteLine("Output:   [" + string.Join(", ", result2) + "]");
Console.WriteLine("Expected: [8, 8]");
Console.WriteLine();

// ── Additional Edge Case: Empty list, only snapshots ─────────────────────────
Console.WriteLine("=== Edge Case: Empty list, SNAPSHOT on empty ===");
var initial3 = new (int, int)[] { };
var ops3 = new string[] { "SNAPSHOT 100" };
int[] result3 = sol.ProcessOperations(initial3, ops3);
Console.WriteLine("Output:   [" + string.Join(", ", result3) + "]");
Console.WriteLine("Expected: [0]");
Console.WriteLine();

// ── Additional Edge Case: Rollback to before all nodes ───────────────────────
Console.WriteLine("=== Edge Case: Rollback removes all real nodes ===");
var initial4 = new (int, int)[] { (5, 100), (10, 200) };
var ops4 = new string[] { "ROLLBACK 1", "SNAPSHOT 10" };
int[] result4 = sol.ProcessOperations(initial4, ops4);
Console.WriteLine("Output:   [" + string.Join(", ", result4) + "]");
Console.WriteLine("Expected: [0]");
Console.WriteLine();

// ── Additional Edge Case: Negative values ────────────────────────────────────
Console.WriteLine("=== Edge Case: Negative values ===");
var initial5 = new (int, int)[] { (1, -100), (2, 50) };
var ops5 = new string[] { "INSERT 3 -200", "SNAPSHOT 3" };
int[] result5 = sol.ProcessOperations(initial5, ops5);
Console.WriteLine("Output:   [" + string.Join(", ", result5) + "]");
Console.WriteLine("Expected: [-250]");  // -100 + 50 + (-200) = -250