/*
 * Title: Reorder Linked List by Frequency Buckets
 * Difficulty: Medium
 * Topic: Linked Lists
 *
 * Problem Description:
 * You are given the head of a singly linked list where each node contains a positive integer value.
 * Your task is to reorder the linked list such that nodes are grouped by the frequency of their
 * values in ascending order of frequency. Within the same frequency group, nodes should appear
 * in the order their value was first encountered in the original list.
 *
 * Specifically:
 * - Count the frequency of each unique value in the linked list.
 * - Reconstruct the linked list by placing nodes with the lowest frequency first,
 *   and nodes with the highest frequency last.
 * - If two values have the same frequency, the value that appeared first in the original
 *   list should come first in the reordered list.
 * - The relative order of nodes sharing the same value must be preserved
 *   (all nodes of the same value are grouped together).
 *
 * Example 1:
 *   Input:  [4, 2, 4, 1, 2, 4]
 *   Output: [1, 2, 2, 4, 4, 4]
 *
 * Example 2:
 *   Input:  [3, 1, 3, 2, 1, 3, 2]
 *   Output: [1, 1, 2, 2, 3, 3, 3]
 *   (Value 1 first seen at index 1, value 2 first seen at index 3, both freq=2, so 1 before 2)
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

// ─────────────────────────────────────────────
// Node definition for a singly linked list
// ─────────────────────────────────────────────
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

// ─────────────────────────────────────────────
// Solution class
// ─────────────────────────────────────────────
public class Solution
{
    /// <summary>
    /// Reorders a linked list so that nodes are grouped by ascending frequency of their value.
    /// Within the same frequency, values that appeared first in the original list come first.
    ///
    /// Time Complexity : O(N log N)
    ///   - One O(N) pass to count frequencies and record first-seen order.
    ///   - One O(N) pass to collect nodes into per-value buckets.
    ///   - One O(K log K) sort of the K distinct values (K ≤ N).
    ///   - One O(N) pass to stitch the final list.
    ///   Overall: O(N log N) dominated by the sort.
    ///
    /// Space Complexity : O(N)
    ///   - Dictionaries and lists holding references to the N existing nodes.
    ///   - No new nodes are allocated; we only relink existing ones.
    /// </summary>
    public ListNode? ReorderByFrequency(ListNode? head)
    {
        // ── Step 1: Handle trivial edge cases ──────────────────────────────────
        // If the list is empty or has only one node, no reordering is needed.
        if (head == null || head.Next == null)
            return head;

        // ── Step 2: Single traversal — gather statistics ───────────────────────
        // We need three pieces of information per distinct value:
        //   a) frequency  : how many times it appears
        //   b) firstIndex : the position (0-based) where it was first seen
        //                   — this breaks ties when two values share a frequency
        //   c) node list  : the actual ListNode objects for that value,
        //                   in the order they appear in the original list
        //                   — we will relink these nodes directly (in-place requirement)

        // frequency[v]  → count of value v
        var frequency  = new Dictionary<int, int>();

        // firstIndex[v] → index of first occurrence of value v
        var firstIndex = new Dictionary<int, int>();

        // nodeGroups[v] → list of ListNode objects whose Val == v, in original order
        var nodeGroups = new Dictionary<int, List<ListNode>>();

        int index = 0;
        ListNode? current = head;

        while (current != null)
        {
            int v = current.Val;

            // If we have never seen this value before, record its first-seen index
            // and initialise its node-group list.
            if (!frequency.ContainsKey(v))
            {
                frequency[v]  = 0;
                firstIndex[v] = index;          // remember where we first met this value
                nodeGroups[v] = new List<ListNode>();
            }

            frequency[v]++;                     // increment the frequency counter
            nodeGroups[v].Add(current);         // store the node reference (no new node!)

            current = current.Next;
            index++;
        }

        // ── Step 3: Sort the distinct values by (frequency ASC, firstIndex ASC) ─
        // We collect all distinct values and sort them with a custom comparer:
        //   Primary key   : frequency ascending  → lower-frequency values come first
        //   Secondary key : firstIndex ascending → among equal-frequency values,
        //                   the one seen earlier in the original list comes first
        //
        // Why sort distinct values rather than nodes?
        // Because we want all nodes of the same value grouped together, and the
        // grouping order is determined by the value's (freq, firstIndex) tuple.

        List<int> distinctValues = new List<int>(frequency.Keys);

        distinctValues.Sort((a, b) =>
        {
            // Compare by frequency first
            int freqCmp = frequency[a].CompareTo(frequency[b]);
            if (freqCmp != 0) return freqCmp;

            // Frequencies are equal → compare by first-seen index
            return firstIndex[a].CompareTo(firstIndex[b]);
        });

        // ── Step 4: Relink nodes in the sorted order ───────────────────────────
        // We use a "dummy" sentinel node so we never have to special-case the head.
        // The dummy's Next will become the new head of the reordered list.
        //
        // For each value (in sorted order) we take every ListNode stored in
        // nodeGroups[value] and append it to the growing result list by adjusting
        // Next pointers — no new nodes are created, satisfying the in-place requirement.

        ListNode dummy = new ListNode(0);   // sentinel; Val is irrelevant
        ListNode tail  = dummy;             // 'tail' always points to the last linked node

        foreach (int v in distinctValues)
        {
            // Append every node that holds value v, in their original relative order
            foreach (ListNode node in nodeGroups[v])
            {
                tail.Next = node;   // link the previous tail to this node
                tail       = node;  // advance tail to the newly appended node
            }
        }

        // ── Step 5: Terminate the list ─────────────────────────────────────────
        // After the loop, 'tail' is the very last node.  Its Next pointer might
        // still point to some old node from the original list (because we only
        // updated Next pointers going forward, not backward).  Setting it to null
        // properly terminates the reordered list and prevents cycles.
        tail.Next = null;

        // The new head is whatever dummy.Next points to.
        return dummy.Next;
    }
}

// ─────────────────────────────────────────────
// Helper utilities (build list, print list)
// ─────────────────────────────────────────────
static ListNode? BuildList(int[] values)
{
    // Creates a linked list from an array and returns its head.
    // Example: [4,2,4,1,2,4] → 4→2→4→1→2→4→null
    if (values.Length == 0) return null;

    ListNode head = new ListNode(values[0]);
    ListNode cur  = head;
    for (int i = 1; i < values.Length; i++)
    {
        cur.Next = new ListNode(values[i]);
        cur = cur.Next;
    }
    return head;
}

static string ListToString(ListNode? head)
{
    // Converts a linked list to a readable string like "[1, 2, 2, 4, 4, 4]"
    var sb = new StringBuilder("[");
    ListNode? cur = head;
    while (cur != null)
    {
        sb.Append(cur.Val);
        if (cur.Next != null) sb.Append(", ");
        cur = cur.Next;
    }
    sb.Append("]");
    return sb.ToString();
}

// ─────────────────────────────────────────────
// Demo / test code (top-level statements)
// ─────────────────────────────────────────────
var solution = new Solution();

Console.WriteLine("=== Reorder Linked List by Frequency Buckets ===\n");

// ── Example 1 ──────────────────────────────────────────────────────────────
// Input : [4, 2, 4, 1, 2, 4]
// Frequencies: 4→3, 2→2, 1→1
// First-seen : 4@0, 2@1, 1@3
// Sorted by (freq ASC, firstIndex ASC): 1(freq=1,idx=3), 2(freq=2,idx=1), 4(freq=3,idx=0)
// Expected output: [1, 2, 2, 4, 4, 4]
{
    int[] input    = { 4, 2, 4, 1, 2, 4 };
    ListNode? head = BuildList(input);
    Console.WriteLine($"Example 1 Input : {ListToString(head)}");
    ListNode? result = solution.ReorderByFrequency(head);
    Console.WriteLine($"Example 1 Output: {ListToString(result)}");
    Console.WriteLine($"Expected        : [1, 2, 2, 4, 4, 4]");
    Console.WriteLine();
}

// ── Example 2 ──────────────────────────────────────────────────────────────
// Input : [3, 1, 3, 2, 1, 3, 2]
// Frequencies: 3→3, 1→2, 2→2
// First-seen : 3@0, 1@1, 2@3
// Sorted by (freq ASC, firstIndex ASC): 1(freq=2,idx=1), 2(freq=2,idx=3), 3(freq=3,idx=0)
// Expected output: [1, 1, 2, 2, 3, 3, 3]
{
    int[] input    = { 3, 1, 3, 2, 1, 3, 2 };
    ListNode? head = BuildList(input);
    Console.WriteLine($"Example 2 Input : {ListToString(head)}");
    ListNode? result = solution.ReorderByFrequency(head);
    Console.WriteLine($"Example 2 Output: {ListToString(result)}");
    Console.WriteLine($"Expected        : [1, 1, 2, 2, 3, 3, 3]");
    Console.WriteLine();
}

// ── Extra test: single element ──────────────────────────────────────────────
{
    int[] input    = { 7 };
    ListNode? head = BuildList(input);
    Console.WriteLine($"Single-node Input : {ListToString(head)}");
    ListNode? result = solution.ReorderByFrequency(head);
    Console.WriteLine($"Single-node Output: {ListToString(result)}");
    Console.WriteLine($"Expected          : [7]");
    Console.WriteLine();
}

// ── Extra test: all same value ──────────────────────────────────────────────
{
    int[] input    = { 5, 5, 5, 5 };
    ListNode? head = BuildList(input);
    Console.WriteLine($"All-same Input : {ListToString(head)}");
    ListNode? result = solution.ReorderByFrequency(head);
    Console.WriteLine($"All-same Output: {ListToString(result)}");
    Console.WriteLine($"Expected       : [5, 5, 5, 5]");
    Console.WriteLine();
}

// ── Extra test: all distinct values ────────────────────────────────────────
// Input : [9, 3, 7, 1]  — all freq=1, so order by first-seen index → unchanged
{
    int[] input    = { 9, 3, 7, 1 };
    ListNode? head = BuildList(input);
    Console.WriteLine($"All-distinct Input : {ListToString(head)}");
    ListNode? result = solution.ReorderByFrequency(head);
    Console.WriteLine($"All-distinct Output: {ListToString(result)}");
    Console.WriteLine($"Expected           : [9, 3, 7, 1]");
    Console.WriteLine();
}

// ── Extra test: larger mixed list ──────────────────────────────────────────
// Input : [2, 3, 2, 1, 3, 2, 1, 3, 1]
// Frequencies: 2→3, 3→3, 1→3  — all same freq=3
// First-seen : 2@0, 3@1, 1@3
// Sorted: 2(idx=0), 3(idx=1), 1(idx=3)
// Expected: [2, 2, 2, 3, 3, 3, 1, 1, 1]
{
    int[] input    = { 2, 3, 2, 1, 3, 2, 1, 3, 1 };
    ListNode? head = BuildList(input);
    Console.WriteLine($"Mixed-freq Input : {ListToString(head)}");
    ListNode? result = solution.ReorderByFrequency(head);
    Console.WriteLine($"Mixed-freq Output: {ListToString(result)}");
    Console.WriteLine($"Expected         : [2, 2, 2, 3, 3, 3, 1, 1, 1]");
    Console.WriteLine();
}