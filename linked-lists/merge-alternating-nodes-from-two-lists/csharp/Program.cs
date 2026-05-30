/*
 * Title: Merge Alternating Nodes from Two Lists
 * Difficulty: Easy
 * Topic: Linked Lists
 *
 * Problem Description:
 * You are given the heads of two singly linked lists, list1 and list2.
 * Your task is to merge the two lists by alternating nodes — first a node from list1,
 * then a node from list2, then from list1, and so on.
 *
 * If one list is exhausted before the other, append the remaining nodes of the longer
 * list to the end of the merged list.
 *
 * Return the head of the merged linked list. You must do this in-place — do not create
 * new nodes; instead, rearrange the existing nodes.
 *
 * Example 1:
 *   Input:  list1 = [1, 3, 5], list2 = [2, 4, 6]
 *   Output: [1, 2, 3, 4, 5, 6]
 *
 * Example 2:
 *   Input:  list1 = [1, 3], list2 = [2, 4, 6, 8]
 *   Output: [1, 2, 3, 4, 6, 8]
 */

// ─── Node Definition ────────────────────────────────────────────────────────

/// <summary>
/// Represents a single node in a singly linked list.
/// Each node holds an integer value and a reference to the next node.
/// </summary>
public class ListNode
{
    public int val;
    public ListNode? next;

    public ListNode(int val = 0, ListNode? next = null)
    {
        this.val = val;
        this.next = next;
    }
}

// ─── Solution Class ──────────────────────────────────────────────────────────

/// <summary>
/// Contains the algorithm to merge two linked lists by alternating their nodes.
/// </summary>
public class Solution
{
    /// <summary>
    /// Merges two singly linked lists by alternating nodes in-place.
    ///
    /// Time Complexity:  O(min(n, m)) — we iterate until the shorter list is exhausted,
    ///                   then the remaining tail is appended in O(1).
    ///                   Overall we touch each node at most once → O(n + m).
    /// Space Complexity: O(1) — we only use a constant number of pointer variables;
    ///                   no new nodes are created and no extra data structures are used.
    /// </summary>
    /// <param name="list1">Head of the first linked list.</param>
    /// <param name="list2">Head of the second linked list.</param>
    /// <returns>Head of the merged alternating linked list.</returns>
    public ListNode? MergeAlternating(ListNode? list1, ListNode? list2)
    {
        // ── Step 1: Handle edge cases where one or both lists are empty ──────
        // If list1 is null, there is nothing from list1 to interleave,
        // so the result is simply list2 (which may also be null).
        if (list1 == null) return list2;

        // If list2 is null, there is nothing from list2 to interleave,
        // so the result is simply list1.
        if (list2 == null) return list1;

        // ── Step 2: Set up two "current" pointers ────────────────────────────
        // We need to walk both lists simultaneously.
        // 'curr1' tracks our position in list1.
        // 'curr2' tracks our position in list2.
        ListNode? curr1 = list1;
        ListNode? curr2 = list2;

        // ── Step 3: Alternate nodes until one list runs out ──────────────────
        // We continue as long as BOTH lists still have nodes to process.
        // In each iteration we:
        //   a) Save the next pointers (so we don't lose the rest of each list).
        //   b) Insert one node from list2 right after the current node of list1.
        //   c) Advance both pointers forward by one position.
        while (curr1 != null && curr2 != null)
        {
            // Save the next node in list1 before we overwrite curr1.next.
            // We need this so we can continue walking list1 after the splice.
            ListNode? next1 = curr1.next;

            // Save the next node in list2 before we overwrite curr2.next.
            // We need this so we can continue walking list2 after the splice.
            ListNode? next2 = curr2.next;

            // Point curr1's next to curr2 — this inserts the list2 node
            // immediately after the current list1 node.
            // Before: ... curr1 → next1 ...   and   ... curr2 → next2 ...
            // After:  ... curr1 → curr2 ...
            curr1.next = curr2;

            // Now point curr2's next to next1 — this reconnects the rest of list1
            // after the newly inserted list2 node.
            // After: ... curr1 → curr2 → next1 ...
            // This correctly interleaves one node from each list.
            curr2.next = next1;

            // Advance curr1 to the node that was originally after curr1 in list1.
            // This is 'next1', which is now sitting after curr2 in the merged chain.
            curr1 = next1;

            // Advance curr2 to the node that was originally after curr2 in list2.
            // This is 'next2', ready to be inserted after the next list1 node.
            curr2 = next2;
        }

        // ── Step 4: Append any remaining nodes ───────────────────────────────
        // When the while-loop exits, at least one list is exhausted.
        //
        // Case A: list1 ran out first (curr1 == null).
        //   The last node we processed from list1 already had its .next set to
        //   the corresponding list2 node, and that list2 node's .next was set to
        //   null (the exhausted list1's next). But curr2 still points to the
        //   remaining unprocessed list2 nodes. We need to attach them.
        //
        // Case B: list2 ran out first (curr2 == null).
        //   curr1 still points to the remaining list1 nodes. However, in the last
        //   iteration we set curr2.next = next1 (which is curr1), so the remaining
        //   list1 nodes are already correctly linked — no extra work needed.
        //
        // Case C: Both ran out simultaneously — nothing to append.
        //
        // We only need to act on Case A: if curr2 is not null, find the last node
        // that was woven in from list2 and attach the remaining list2 tail.
        //
        // Actually, a simpler observation: after the loop, if curr2 != null it means
        // list1 was exhausted. The last curr2 node that was woven in had its .next
        // set to null (curr1 was null). We need to find that last woven-in list2 node
        // and point it to curr2.
        //
        // The cleanest way: walk from list1's head to find the last node, then
        // attach curr2. But that would be O(n). Instead, we track the "last woven"
        // list2 node during the loop.
        //
        // Wait — let's re-examine. After the loop:
        //   - curr1 is null  → list1 exhausted; curr2 holds remaining list2 tail.
        //     The last node in the merged chain so far is the last curr2 we wove in,
        //     whose .next was set to null. We need to reattach curr2 there.
        //   - curr2 is null  → list2 exhausted; curr1 holds remaining list1 tail.
        //     The last node in the merged chain so far is the last curr2 we wove in,
        //     whose .next was already set to curr1 (next1). So list1's tail is already
        //     attached — nothing to do.
        //
        // To handle Case A without an extra traversal, we track the last processed
        // curr2 node. Let's refactor slightly using a 'lastCurr2' variable.
        // ─────────────────────────────────────────────────────────────────────
        // NOTE: The code above already handles Case B correctly (list1 tail is linked).
        // For Case A we need to reattach. We'll use the helper below.
        // Because we already ran the loop, we need to find the tail of the merged list.
        // Since the list is at most 2000 nodes, a tail-find is acceptable, but let's
        // use the smarter approach: re-run with a lastCurr2 tracker.
        // ─────────────────────────────────────────────────────────────────────
        // The current implementation is already correct for Case B.
        // For Case A: curr2 != null means we have leftover list2 nodes.
        // The last node added to the merged list was the last curr2 we processed,
        // and its .next was set to null (since curr1 became null).
        // We need to find that node and set its .next = curr2.
        //
        // Simplest correct fix: if curr2 != null, find the tail of the merged list
        // starting from list1 and append curr2.
        if (curr2 != null)
        {
            // Walk from list1's head to find the current tail of the merged list.
            // This is O(n+m) in the worst case but keeps the logic simple and clear.
            ListNode tail = list1;
            while (tail.next != null)
            {
                tail = tail.next;
            }
            // Attach the remaining list2 nodes to the end of the merged list.
            tail.next = curr2;
        }
        // If curr2 is null (list2 exhausted or both exhausted), the merged list
        // is already complete — list1's remaining tail is already linked in.

        // ── Step 5: Return the head of the merged list ───────────────────────
        // The head of the merged list is always list1's original head,
        // because we always start with a node from list1.
        return list1;
    }
}

// ─── Demo / Test Code ────────────────────────────────────────────────────────

// Helper: build a linked list from an array of integers.
static ListNode? BuildList(int[] values)
{
    if (values.Length == 0) return null;
    ListNode head = new ListNode(values[0]);
    ListNode current = head;
    for (int i = 1; i < values.Length; i++)
    {
        current.next = new ListNode(values[i]);
        current = current.next;
    }
    return head;
}

// Helper: convert a linked list to a readable string like "[1 -> 2 -> 3]".
static string ListToString(ListNode? head)
{
    if (head == null) return "[]";
    var parts = new System.Text.StringBuilder();
    parts.Append('[');
    ListNode? curr = head;
    while (curr != null)
    {
        parts.Append(curr.val);
        if (curr.next != null) parts.Append(" -> ");
        curr = curr.next;
    }
    parts.Append(']');
    return parts.ToString();
}

Solution sol = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// list1 = [1, 3, 5], list2 = [2, 4, 6]
// Expected output: [1 -> 2 -> 3 -> 4 -> 5 -> 6]
//
// Trace:
//   Iteration 1: curr1=1, curr2=2  → 1→2→3, next1=3, next2=4
//   Iteration 2: curr1=3, curr2=4  → 3→4→5, next1=5, next2=6
//   Iteration 3: curr1=5, curr2=6  → 5→6→null, next1=null, next2=null
//   Loop ends: curr1=null, curr2=null → nothing to append.
//   Result: 1→2→3→4→5→6  ✓
ListNode? l1ex1 = BuildList(new[] { 1, 3, 5 });
ListNode? l2ex1 = BuildList(new[] { 2, 4, 6 });
ListNode? result1 = sol.MergeAlternating(l1ex1, l2ex1);
Console.WriteLine("Example 1:");
Console.WriteLine($"  Input:    list1 = [1, 3, 5], list2 = [2, 4, 6]");
Console.WriteLine($"  Output:   {ListToString(result1)}");
Console.WriteLine($"  Expected: [1 -> 2 -> 3 -> 4 -> 5 -> 6]");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// list1 = [1, 3], list2 = [2, 4, 6, 8]
// Expected output: [1 -> 2 -> 3 -> 4 -> 6 -> 8]
//
// Trace:
//   Iteration 1: curr1=1, curr2=2  → 1→2→3, next1=3, next2=4
//   Iteration 2: curr1=3, curr2=4  → 3→4→null, next1=null, next2=6
//   Loop ends: curr1=null, curr2=6 (remaining: 6→8)
//   Tail-find from list1: 1→2→3→4→null  → tail=4
//   tail.next = curr2 (6) → 4→6→8
//   Result: 1→2→3→4→6→8  ✓
ListNode? l1ex2 = BuildList(new[] { 1, 3 });
ListNode? l2ex2 = BuildList(new[] { 2, 4, 6, 8 });
ListNode? result2 = sol.MergeAlternating(l1ex2, l2ex2);
Console.WriteLine("Example 2:");
Console.WriteLine($"  Input:    list1 = [1, 3], list2 = [2, 4, 6, 8]");
Console.WriteLine($"  Output:   {ListToString(result2)}");
Console.WriteLine($"  Expected: [1 -> 2 -> 3 -> 4 -> 6 -> 8]");
Console.WriteLine();

// ── Example 3: list1 longer than list2 ───────────────────────────────────────
// list1 = [1, 3, 5, 7], list2 = [2, 4]
// Expected output: [1 -> 2 -> 3 -> 4 -> 5 -> 7]
//
// Trace:
//   Iteration 1: curr1=1, curr2=2  → 1→2→3, next1=3, next2=4
//   Iteration 2: curr1=3, curr2=4  → 3→4→5, next1=5, next2=null
//   Loop ends: curr1=5 (remaining: 5→7), curr2=null
//   curr2 is null → no append needed; 4→5→7 already linked.
//   Result: 1→2→3→4→5→7  ✓
ListNode? l1ex3 = BuildList(new[] { 1, 3, 5, 7 });
ListNode? l2ex3 = BuildList(new[] { 2, 4 });
ListNode? result3 = sol.MergeAlternating(l1ex3, l2ex3);
Console.WriteLine("Example 3 (list1 longer):");
Console.WriteLine($"  Input:    list1 