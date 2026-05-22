/*
 * Title: Delete Every K-th Node in a Linked List
 * Difficulty: Easy
 * Topic: Linked Lists
 *
 * Problem Description:
 * You are given the head of a singly linked list and a positive integer k.
 * Your task is to delete every k-th node from the list and return the head
 * of the modified list.
 *
 * Specifically, keep the first k-1 nodes, delete the k-th node, keep the next
 * k-1 nodes, delete the next k-th node, and so on until the end of the list.
 *
 * If the total number of nodes is not a multiple of k, the remaining nodes at
 * the end (fewer than k nodes) should all be kept.
 *
 * Example 1:
 *   Input:  head = [1, 2, 3, 4, 5, 6, 7, 8], k = 3
 *   Output: [1, 2, 4, 5, 7, 8]
 *   Explanation: The 3rd node (value 3) and the 6th node (value 6) are deleted.
 *
 * Example 2:
 *   Input:  head = [10, 20, 30, 40, 50], k = 2
 *   Output: [10, 30, 50]
 *   Explanation: Nodes at positions 2 (value 20) and 4 (value 40) are deleted.
 */

// ─── Node definition ────────────────────────────────────────────────────────

/// <summary>
/// Represents a single node in a singly linked list.
/// Each node holds an integer value and a reference to the next node.
/// </summary>
public class ListNode
{
    public int Val;
    public ListNode? Next;

    public ListNode(int val, ListNode? next = null)
    {
        Val  = val;
        Next = next;
    }
}

// ─── Solution ───────────────────────────────────────────────────────────────

/// <summary>
/// Contains the algorithm for deleting every k-th node from a linked list.
/// </summary>
public class Solution
{
    /// <summary>
    /// Deletes every k-th node from the linked list and returns the new head.
    ///
    /// Time Complexity : O(n)  — we visit every node exactly once.
    /// Space Complexity: O(1)  — we only use a fixed number of pointer variables;
    ///                           no extra data structures are allocated.
    /// </summary>
    /// <param name="head">Head of the singly linked list.</param>
    /// <param name="k">Every k-th node will be removed.</param>
    /// <returns>Head of the modified list.</returns>
    public ListNode? DeleteEveryKthNode(ListNode? head, int k)
    {
        // ── Edge case: if k == 1, every node is a "k-th node", so the entire
        //    list gets deleted and we return null immediately.
        if (k == 1)
            return null;

        // ── We introduce a "dummy" (sentinel) node that sits just before the
        //    real head of the list.  This simplifies deletion logic because we
        //    never have to special-case removing the very first node — the dummy
        //    always acts as the predecessor of whatever the current head is.
        //
        //    dummy → node1 → node2 → node3 → ...
        ListNode dummy = new ListNode(0, head);

        // ── 'prev' always points to the node BEFORE the one we are currently
        //    examining.  We need it so we can "skip over" (delete) the k-th node
        //    by re-wiring prev.Next to skip the unwanted node.
        ListNode prev = dummy;

        // ── 'current' is the node we are currently visiting / counting.
        ListNode? current = head;

        // ── 'count' tracks our position within the current group of k nodes.
        //    When count reaches k we have found a node that must be deleted.
        int count = 0;

        // ── Traverse the entire list one node at a time.
        while (current != null)
        {
            // Step 1 – Increment the counter for the current node.
            //          This tells us "we have now seen 'count' nodes in the
            //          current group of k."
            count++;

            // Step 2 – Check whether the current node is the k-th in its group.
            if (count == k)
            {
                // ── We found a node that must be deleted.
                //
                //    Before deletion the list looks like:
                //      ... → prev → current → current.Next → ...
                //
                //    After deletion we want:
                //      ... → prev → current.Next → ...
                //
                //    We achieve this by making prev.Next jump over 'current'.
                prev.Next = current.Next;

                // ── Reset the counter so we start counting a fresh group of k
                //    nodes from the very next node onward.
                count = 0;

                // ── Move 'current' forward to the next node.
                //    Note: 'prev' does NOT move here because the node that was
                //    just deleted is gone; prev still correctly points to the
                //    last kept node.
                current = prev.Next;
            }
            else
            {
                // ── The current node is NOT the k-th, so we keep it.
                //    Advance both 'prev' and 'current' one step forward.
                prev    = current;
                current = current.Next;
            }
        }

        // ── The dummy node's Next now points to the (possibly modified) head
        //    of the list.  Return it as the new head.
        return dummy.Next;
    }
}

// ─── Helper utilities ───────────────────────────────────────────────────────

/// <summary>
/// Builds a linked list from an array of integers (for easy test setup).
/// </summary>
static ListNode? BuildList(int[] values)
{
    if (values.Length == 0) return null;

    ListNode dummy = new ListNode(0);
    ListNode tail  = dummy;

    foreach (int v in values)
    {
        tail.Next = new ListNode(v);
        tail = tail.Next;
    }

    return dummy.Next;
}

/// <summary>
/// Converts a linked list to a readable string like "[1, 2, 4, 5, 7, 8]".
/// </summary>
static string ListToString(ListNode? head)
{
    var parts = new System.Collections.Generic.List<string>();
    ListNode? cur = head;

    while (cur != null)
    {
        parts.Add(cur.Val.ToString());
        cur = cur.Next;
    }

    return "[" + string.Join(", ", parts) + "]";
}

// ─── Demo / verification ────────────────────────────────────────────────────

Solution sol = new Solution();

// ── Example 1 ──────────────────────────────────────────────────────────────
// Input : [1, 2, 3, 4, 5, 6, 7, 8], k = 3
// Expected output: [1, 2, 4, 5, 7, 8]
//
// Trace:
//   count=1 (val=1) → keep   prev=1,  cur=2
//   count=2 (val=2) → keep   prev=2,  cur=3
//   count=3 (val=3) → DELETE prev=2,  cur=4, count reset to 0
//   count=1 (val=4) → keep   prev=4,  cur=5
//   count=2 (val=5) → keep   prev=5,  cur=6
//   count=3 (val=6) → DELETE prev=5,  cur=7, count reset to 0
//   count=1 (val=7) → keep   prev=7,  cur=8
//   count=2 (val=8) → keep   prev=8,  cur=null  → loop ends
//   Result: [1, 2, 4, 5, 7, 8]  ✓
ListNode? list1  = BuildList([1, 2, 3, 4, 5, 6, 7, 8]);
ListNode? result1 = sol.DeleteEveryKthNode(list1, 3);
Console.WriteLine("Example 1:");
Console.WriteLine($"  Input   : [1, 2, 3, 4, 5, 6, 7, 8], k = 3");
Console.WriteLine($"  Output  : {ListToString(result1)}");
Console.WriteLine($"  Expected: [1, 2, 4, 5, 7, 8]");
Console.WriteLine();

// ── Example 2 ──────────────────────────────────────────────────────────────
// Input : [10, 20, 30, 40, 50], k = 2
// Expected output: [10, 30, 50]
//
// Trace:
//   count=1 (val=10) → keep   prev=10, cur=20
//   count=2 (val=20) → DELETE prev=10, cur=30, count reset to 0
//   count=1 (val=30) → keep   prev=30, cur=40
//   count=2 (val=40) → DELETE prev=30, cur=50, count reset to 0
//   count=1 (val=50) → keep   prev=50, cur=null → loop ends
//   Result: [10, 30, 50]  ✓
ListNode? list2   = BuildList([10, 20, 30, 40, 50]);
ListNode? result2 = sol.DeleteEveryKthNode(list2, 2);
Console.WriteLine("Example 2:");
Console.WriteLine($"  Input   : [10, 20, 30, 40, 50], k = 2");
Console.WriteLine($"  Output  : {ListToString(result2)}");
Console.WriteLine($"  Expected: [10, 30, 50]");
Console.WriteLine();

// ── Edge case: k = 1 (delete every node) ───────────────────────────────────
ListNode? list3   = BuildList([5, 10, 15]);
ListNode? result3 = sol.DeleteEveryKthNode(list3, 1);
Console.WriteLine("Edge case (k=1, delete all):");
Console.WriteLine($"  Input   : [5, 10, 15], k = 1");
Console.WriteLine($"  Output  : {ListToString(result3)}");
Console.WriteLine($"  Expected: []");
Console.WriteLine();

// ── Edge case: k larger than list length (nothing deleted) ─────────────────
ListNode? list4   = BuildList([1, 2, 3]);
ListNode? result4 = sol.DeleteEveryKthNode(list4, 10);
Console.WriteLine("Edge case (k > list length, nothing deleted):");
Console.WriteLine($"  Input   : [1, 2, 3], k = 10");
Console.WriteLine($"  Output  : {ListToString(result4)}");
Console.WriteLine($"  Expected: [1, 2, 3]");
Console.WriteLine();

// ── Edge case: single node, k = 1 ─────────────────────────────────────────
ListNode? list5   = BuildList([42]);
ListNode? result5 = sol.DeleteEveryKthNode(list5, 1);
Console.WriteLine("Edge case (single node, k=1):");
Console.WriteLine($"  Input   : [42], k = 1");
Console.WriteLine($"  Output  : {ListToString(result5)}");
Console.WriteLine($"  Expected: []");