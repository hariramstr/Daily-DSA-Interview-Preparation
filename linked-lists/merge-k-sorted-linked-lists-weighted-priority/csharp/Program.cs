/*
 * Title: Merge K Sorted Linked Lists with Weighted Priority
 * 
 * Problem Description:
 * You are given k sorted singly linked lists and an integer array weights of length k,
 * where weights[i] represents the priority weight of the i-th linked list.
 * Your task is to merge all k linked lists into a single sorted linked list, but with a twist:
 * when two nodes from different lists have equal values, the node from the list with the
 * HIGHER weight should appear first. If two equal-valued nodes come from lists with equal
 * weights, the node from the list with the SMALLER index should appear first.
 *
 * Additionally, after merging, you must REMOVE every node whose value appears more than
 * `threshold` times in the final merged list, where threshold is a given integer.
 *
 * Return the head of the resulting linked list.
 *
 * Constraints:
 * - 1 <= k <= 10^4
 * - 0 <= weights[i] <= 10^9
 * - Each linked list has at most 500 nodes.
 * - Node values are in the range [-10^5, 10^5]
 * - 1 <= threshold <= 10^4
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Node definition for a singly linked list
// ─────────────────────────────────────────────────────────────────────────────
public class ListNode
{
    public int Val;
    public ListNode? Next;
    public ListNode(int val = 0, ListNode? next = null)
    {
        Val = val;
        Next = next;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helper struct that we'll store in the priority queue.
// It bundles together: the node itself, which list it came from (listIndex),
// and that list's weight. This lets us break ties correctly.
// ─────────────────────────────────────────────────────────────────────────────
public readonly struct HeapEntry
{
    public readonly ListNode Node;      // the actual linked-list node
    public readonly int ListIndex;      // which list (0-based) this node belongs to
    public readonly int Weight;         // the weight of that list

    public HeapEntry(ListNode node, int listIndex, int weight)
    {
        Node = node;
        ListIndex = listIndex;
        Weight = weight;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Custom comparer for the min-heap (PriorityQueue in .NET uses a min-heap by
// default when you supply a comparer that returns negative for "higher priority").
//
// Ordering rules (ascending in the merged list means SMALLEST value first):
//   1. Smaller node value  → higher priority (comes out of heap first)
//   2. Tie on value        → LARGER weight   → higher priority
//   3. Tie on value & weight → SMALLER list index → higher priority
// ─────────────────────────────────────────────────────────────────────────────
public class HeapEntryComparer : IComparer<HeapEntry>
{
    public int Compare(HeapEntry x, HeapEntry y)
    {
        // Step 1: Compare by node value (ascending order in merged list)
        int cmp = x.Node.Val.CompareTo(y.Node.Val);
        if (cmp != 0) return cmp;   // different values → smaller value wins

        // Step 2: Tie-break by weight (DESCENDING — higher weight comes first)
        cmp = y.Weight.CompareTo(x.Weight);
        if (cmp != 0) return cmp;

        // Step 3: Tie-break by list index (ASCENDING — smaller index comes first)
        return x.ListIndex.CompareTo(y.ListIndex);
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Main solution class
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /*
     * Time Complexity:  O(N log k)  where N = total number of nodes across all lists,
     *                               k = number of lists.
     *                   Each node is pushed/popped from the heap exactly once.
     *                   Each heap operation costs O(log k).
     *
     * Space Complexity: O(k) for the heap (at most one node per list at any time),
     *                   plus O(U) for the frequency dictionary where U = unique values.
     *                   The output list itself is O(N) but that's required output space.
     */
    public ListNode? MergeKListsWeighted(ListNode?[] lists, int[] weights, int threshold)
    {
        // ── PHASE 1: Seed the min-heap ────────────────────────────────────────
        // We use .NET's PriorityQueue<TElement, TPriority> with a custom comparer.
        // The "priority" IS the HeapEntry itself (we use the same type for both
        // element and priority, which is a common pattern when the element IS the key).
        //
        // Why a heap? Because at each step we need the globally smallest (with
        // tie-breaking) node among all k list fronts. A heap gives us that in O(log k).

        var heap = new PriorityQueue<HeapEntry, HeapEntry>(new HeapEntryComparer());

        int k = lists.Length;

        for (int i = 0; i < k; i++)
        {
            // Only add non-null list heads to the heap.
            // We store the list index and weight so we can apply tie-breaking rules
            // and so we know which list to advance after popping.
            if (lists[i] != null)
            {
                var entry = new HeapEntry(lists[i]!, i, weights[i]);
                heap.Enqueue(entry, entry);   // element == priority (comparer drives order)
            }
        }

        // ── PHASE 2: Extract nodes in sorted (weighted) order ─────────────────
        // We'll build the merged list by repeatedly popping the heap's minimum entry,
        // appending it to our result, and pushing that node's successor (if any).
        //
        // A "dummy head" node simplifies list construction — we never have to special-
        // case the very first node.

        var dummy = new ListNode(0);   // sentinel / dummy head
        var tail  = dummy;             // tail always points to the last node added

        // We also need to count how many times each VALUE appears in the merged list
        // so we can apply the threshold filter in Phase 3.
        // Key = node value, Value = count of occurrences.
        var frequency = new Dictionary<int, int>();

        while (heap.Count > 0)
        {
            // Pop the entry with the highest priority (smallest value, then tie-breaks)
            var entry = heap.Dequeue();
            ListNode current = entry.Node;

            // Append this node to the merged list
            tail.Next = current;
            tail = current;

            // Record the frequency of this value
            int val = current.Val;
            frequency[val] = frequency.TryGetValue(val, out int cnt) ? cnt + 1 : 1;

            // Advance to the next node in the same list and push it onto the heap
            // (if it exists). This is the key step that keeps the heap size ≤ k.
            if (current.Next != null)
            {
                var next = new HeapEntry(current.Next, entry.ListIndex, entry.Weight);
                heap.Enqueue(next, next);
            }
        }

        // Terminate the merged list properly (tail.Next might still point somewhere
        // from the original lists, so we null it out).
        tail.Next = null;

        // ── PHASE 3: Remove nodes whose value exceeds the threshold ───────────
        // We now walk the merged list and skip any node whose value appears
        // more than `threshold` times.
        //
        // Why do this as a separate pass? Because we need the complete frequency
        // count before we can decide which values to remove. We can't know during
        // Phase 2 whether a value will eventually exceed the threshold.

        // Re-use the dummy head; rebuild the list in-place by relinking.
        var prev = dummy;          // prev trails behind current
        var node = dummy.Next;     // current node being examined

        while (node != null)
        {
            if (frequency[node.Val] > threshold)
            {
                // This value appears too many times — skip (unlink) this node.
                // prev.Next stays pointing to whatever comes after node.
                prev.Next = node.Next;
                // We do NOT advance prev — it stays where it is because the next
                // node (node.Next) also needs to be checked.
            }
            else
            {
                // This node survives — advance prev to it.
                prev = node;
            }

            node = node.Next;   // always advance the scanning pointer
        }

        // dummy.Next is the head of the final merged, filtered list.
        return dummy.Next;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helper utilities for the demo
// ─────────────────────────────────────────────────────────────────────────────
static class Helpers
{
    // Build a linked list from an array of integers (for easy test setup)
    public static ListNode? BuildList(int[] values)
    {
        if (values == null || values.Length == 0) return null;
        var dummy = new ListNode(0);
        var cur = dummy;
        foreach (int v in values)
        {
            cur.Next = new ListNode(v);
            cur = cur.Next;
        }
        return dummy.Next;
    }

    // Convert a linked list to a readable string like "[1,2,3]"
    public static string ListToString(ListNode? head)
    {
        var parts = new List<string>();
        while (head != null)
        {
            parts.Add(head.Val.ToString());
            head = head.Next;
        }
        return "[" + string.Join(",", parts) + "]";
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / driver code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

var solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// lists = [[1,4,7],[2,4,6],[1,3,5]], weights = [3,1,2], threshold = 2
// Expected output: [1,1,2,3,4,4,5,6,7]
//
// Trace:
//   Heap initially: {1(list0,w=3), 2(list1,w=1), 1(list2,w=2)}
//   Pop 1(list0,w=3) → value 1, weight 3 wins over 1(list2,w=2)
//   Pop 1(list2,w=2) → value 1
//   Pop 2(list1,w=1)
//   Pop 3(list2,w=2)
//   Pop 4(list0,w=3) → value 4, weight 3 wins over 4(list1,w=1)
//   Pop 4(list1,w=1)
//   Pop 5(list2,w=2)
//   Pop 6(list1,w=1)
//   Pop 7(list0,w=3)
//   Merged: [1,1,2,3,4,4,5,6,7]
//   Frequencies: {1:2, 2:1, 3:1, 4:2, 5:1, 6:1, 7:1} — none exceed threshold=2
//   Result: [1,1,2,3,4,4,5,6,7]  ✓

Console.WriteLine("=== Example 1 ===");
var lists1 = new ListNode?[]
{
    Helpers.BuildList(new[] {1, 4, 7}),
    Helpers.BuildList(new[] {2, 4, 6}),
    Helpers.BuildList(new[] {1, 3, 5})
};
int[] weights1 = {3, 1, 2};
int threshold1 = 2;

var result1 = solution.MergeKListsWeighted(lists1, weights1, threshold1);
Console.WriteLine("Input lists : [[1,4,7],[2,4,6],[1,3,5]]");
Console.WriteLine("Weights     : [3,1,2]");
Console.WriteLine("Threshold   : 2");
Console.WriteLine("Output      : " + Helpers.ListToString(result1));
Console.WriteLine("Expected    : [1,1,2,3,4,4,5,6,7]");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// lists = [[1,1,3],[1,2,3],[3,4,5]], weights = [2,2,1], threshold = 2
// Expected output: [2,4,5]
//
// Trace:
//   Merge produces: 1(list0,w=2), 1(list1,w=2 but list0 index smaller so list0 first),
//                   1(list0 second node), 2(list1), 3(list0), 3(list1), 3(list2), 4, 5
//   Wait — let's be careful:
//   list0 = [1,1,3], list1 = [1,2,3], list2 = [3,4,5]
//   Initial heap heads: 1(list0,w=2), 1(list1,w=2), 3(list2,w=1)
//   Pop 1(list0,w=2) — same value 1, same weight 2, smaller index 0 wins
//   Push list0's next: 1(list0,w=2)
//   Heap: {1(list0,w=2), 1(list1,w=2), 3(list2,w=1)}
//   Pop 1(list0,w=2) — again list0 index 0 < list1 index 1
//   Push list0's next: 3(list0,w=2)
//   Heap: {1(list1,w=2), 3(list0,w=2), 3(list2,w=1)}
//   Pop 1(list1,w=2)
//   Push list1's next: 2(list1,w=2)
//   Heap: {2(list1,w=2), 3(list0,w=2), 3(list2,w=1)}
//   Pop 2(list1,w=2)
//   Push list1's next: 3(list1,w=2)
//   Heap: {3(list0,w=2), 3(list1,w=2), 3(list2,w=1)}
//   Pop 3(list0,w=2) — weight 2 > weight 1, and list0 index < list1 index
//   Push nothing (list0 exhausted)
//   Pop 3(list1,w=2) — weight 2 > weight 1
//   Push nothing (list1 exhausted)
//   Pop 3(list2,w=1)
//   Push 4(list2,w=1)
//   Pop 4(list2,w=1)
//   Push 5(list2,w=1)
//   Pop 5(list2,w=1)
//   Merged: [1,1,1,2,3,3,3,4,5]
//   Frequencies: {1:3, 2:1, 3:3, 4:1, 5:1}
//   threshold=2 → remove values with count > 2 → remove 1 (count 3