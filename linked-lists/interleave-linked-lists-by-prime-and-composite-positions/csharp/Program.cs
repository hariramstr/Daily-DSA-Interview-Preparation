/*
 * Title: Interleave Linked Lists by Prime and Composite Positions
 *
 * Problem Description:
 * You are given a singly linked list of integers. Your task is to restructure
 * the list in-place such that nodes originally at prime-indexed positions (1-indexed)
 * form the first half of the new list, and nodes at composite (non-prime, non-one)
 * positions form the second half — but the two groups must be interleaved by
 * alternating one node from each group, starting with the prime-indexed group.
 * If one group is exhausted before the other, append the remaining nodes of the
 * longer group to the end.
 *
 * Position 1 is considered neither prime nor composite. Nodes at position 1 should
 * be placed at the very beginning of the resulting list before any interleaving begins.
 *
 * For a list of 7 nodes:
 *   Position 1 → neither
 *   Positions 2, 3, 5, 7 → prime
 *   Positions 4, 6 → composite
 *
 * Result: [node1] → [node2] → [node4] → [node3] → [node6] → [node5] → [node7]
 *
 * Example 1:
 *   Input:  [10, 20, 30, 40, 50, 60, 70]
 *   Output: [10, 20, 40, 30, 60, 50, 70]
 *
 * Example 2:
 *   Input:  [5, 3, 8, 1, 9]
 *   Output: [5, 3, 1, 8, 9]
 */

using System;
using System.Collections.Generic;
using System.Text;

// ─────────────────────────────────────────────────────────────────────────────
// Node definition for a singly linked list
// ─────────────────────────────────────────────────────────────────────────────
public class ListNode
{
    public int Val;
    public ListNode? Next;
    public ListNode(int val) { Val = val; Next = null; }
}

// ─────────────────────────────────────────────────────────────────────────────
// Solution class containing the main algorithm
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    // =========================================================================
    // Time Complexity:  O(n) — we traverse the list a constant number of times
    //                   (once to classify nodes, once to weave them together).
    //                   The primality check for each position index i is O(√i),
    //                   but since i ≤ n ≤ 10^5 the total work is still O(n)
    //                   in practice (and dominated by the linear traversal).
    //
    // Space Complexity: O(1) extra space — we only store a handful of pointer
    //                   variables; no auxiliary arrays or lists are allocated.
    //                   (The problem statement explicitly requires O(1) extra.)
    // =========================================================================
    public ListNode? Restructure(ListNode? head)
    {
        // ── Step 1: Handle trivial cases ──────────────────────────────────────
        // If the list is empty or has only one node there is nothing to rearrange.
        if (head == null || head.Next == null)
            return head;

        // ── Step 2: Declare dummy heads for the three groups ──────────────────
        // We will collect nodes into three separate chains:
        //   • "neither"   — position 1 only
        //   • "primes"    — positions 2, 3, 5, 7, 11, …
        //   • "composites"— positions 4, 6, 8, 9, 10, …
        //
        // Using a dummy (sentinel) node at the front of each chain lets us
        // avoid special-casing the "first node" insertion — we always attach
        // to dummyX.Next and advance a tail pointer.
        ListNode neitherDummy   = new ListNode(0);
        ListNode primesDummy    = new ListNode(0);
        ListNode compositesDummy = new ListNode(0);

        // Tail pointers so we can append in O(1) without traversing each chain.
        ListNode neitherTail    = neitherDummy;
        ListNode primesTail     = primesDummy;
        ListNode compositesTail = compositesDummy;

        // ── Step 3: Walk the original list and classify every node ────────────
        // We use a 1-based position counter that matches the problem statement.
        ListNode? current = head;
        int position = 1;

        while (current != null)
        {
            // Save the next pointer BEFORE we overwrite current.Next.
            // This is the standard "detach and re-link" pattern for in-place
            // linked-list manipulation.
            ListNode? nextNode = current.Next;

            // Detach the current node from the original chain so it becomes
            // a standalone node ready to be inserted into one of our groups.
            current.Next = null;

            if (position == 1)
            {
                // Position 1 is neither prime nor composite — goes to its own group.
                neitherTail.Next = current;
                neitherTail = current;
            }
            else if (IsPrime(position))
            {
                // Prime position → append to the primes chain.
                primesTail.Next = current;
                primesTail = current;
            }
            else
            {
                // Composite position (≥ 4, not prime) → append to composites chain.
                compositesTail.Next = current;
                compositesTail = current;
            }

            // Advance to the next original node and increment the position counter.
            current = nextNode;
            position++;
        }

        // At this point:
        //   neitherDummy.Next   → chain of "position-1" nodes (at most 1 node)
        //   primesDummy.Next    → chain of prime-position nodes
        //   compositesDummy.Next → chain of composite-position nodes

        // ── Step 4: Interleave the primes and composites chains ───────────────
        // The desired interleaving pattern is:
        //   prime[0] → composite[0] → prime[1] → composite[1] → …
        // followed by any leftover nodes from the longer group.
        //
        // We build this merged chain using another dummy head so the loop body
        // stays uniform (no special case for the very first merged node).
        ListNode mergedDummy = new ListNode(0);
        ListNode mergedTail  = mergedDummy;

        ListNode? p = primesDummy.Next;    // pointer into the primes chain
        ListNode? c = compositesDummy.Next; // pointer into the composites chain

        // Alternate: take one prime, then one composite, repeat.
        while (p != null || c != null)
        {
            if (p != null)
            {
                // Attach the current prime node to the merged chain.
                mergedTail.Next = p;
                mergedTail = p;

                // Advance the prime pointer BEFORE we might overwrite p.Next
                // in the next iteration.
                p = p.Next;

                // Detach the node we just appended from its old chain so the
                // merged chain doesn't accidentally loop back.
                mergedTail.Next = null;
            }

            if (c != null)
            {
                // Attach the current composite node to the merged chain.
                mergedTail.Next = c;
                mergedTail = c;

                c = c.Next;

                // Same detach trick as above.
                mergedTail.Next = null;
            }
        }

        // ── Step 5: Assemble the final list ───────────────────────────────────
        // Layout: [neither group] → [interleaved primes+composites]
        //
        // The "neither" group contains at most one node (position 1).
        // We connect its tail to the start of the merged interleaved chain.
        neitherTail.Next = mergedDummy.Next;

        // The true head of the result is the first real node after neitherDummy.
        return neitherDummy.Next;
    }

    // ── Helper: primality test ────────────────────────────────────────────────
    // Returns true if n is a prime number.
    // We only need to check divisors up to √n, which keeps this O(√n).
    // For n ≤ 10^5 this is fast enough; the total cost across all positions
    // is O(n) amortised.
    private static bool IsPrime(int n)
    {
        // Numbers less than 2 are not prime by definition.
        if (n < 2) return false;

        // 2 is the only even prime.
        if (n == 2) return true;

        // All other even numbers are composite.
        if (n % 2 == 0) return false;

        // Check odd divisors from 3 up to √n.
        for (int i = 3; (long)i * i <= n; i += 2)
        {
            if (n % i == 0) return false;
        }

        return true;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helper utilities (build a list from an array; convert a list to a string)
// ─────────────────────────────────────────────────────────────────────────────
static class LinkedListHelper
{
    /// <summary>Builds a linked list from an integer array and returns its head.</summary>
    public static ListNode? Build(int[] values)
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

    /// <summary>Converts a linked list to a human-readable string like [10, 20, 30].</summary>
    public static string ToString(ListNode? head)
    {
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
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / test code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────
var solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// Input:    [10, 20, 30, 40, 50, 60, 70]
// Positions:  1   2   3   4   5   6   7
//   pos 1 → neither  → 10
//   pos 2 → prime    → 20
//   pos 3 → prime    → 30
//   pos 4 → composite→ 40
//   pos 5 → prime    → 50
//   pos 6 → composite→ 60
//   pos 7 → prime    → 70
//
// Primes chain:    20 → 30 → 50 → 70
// Composites chain: 40 → 60
//
// Interleave (prime first):
//   20 → 40 → 30 → 60 → 50 → 70
//
// Prepend "neither" group:
//   10 → 20 → 40 → 30 → 60 → 50 → 70  ✓
Console.WriteLine("=== Example 1 ===");
ListNode? head1 = LinkedListHelper.Build(new[] { 10, 20, 30, 40, 50, 60, 70 });
Console.WriteLine($"Input:    {LinkedListHelper.ToString(head1)}");
ListNode? result1 = solution.Restructure(head1);
Console.WriteLine($"Output:   {LinkedListHelper.ToString(result1)}");
Console.WriteLine($"Expected: [10, 20, 40, 30, 60, 50, 70]");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// Input:    [5, 3, 8, 1, 9]
// Positions:  1  2  3  4  5
//   pos 1 → neither  → 5
//   pos 2 → prime    → 3
//   pos 3 → prime    → 8
//   pos 4 → composite→ 1
//   pos 5 → prime    → 9
//
// Primes chain:    3 → 8 → 9
// Composites chain: 1
//
// Interleave (prime first):
//   3 → 1 → 8 → 9
//
// Prepend "neither" group:
//   5 → 3 → 1 → 8 → 9  ✓
Console.WriteLine("=== Example 2 ===");
ListNode? head2 = LinkedListHelper.Build(new[] { 5, 3, 8, 1, 9 });
Console.WriteLine($"Input:    {LinkedListHelper.ToString(head2)}");
ListNode? result2 = solution.Restructure(head2);
Console.WriteLine($"Output:   {LinkedListHelper.ToString(result2)}");
Console.WriteLine($"Expected: [5, 3, 1, 8, 9]");
Console.WriteLine();

// ── Edge case: single node ────────────────────────────────────────────────────
Console.WriteLine("=== Edge Case: Single Node ===");
ListNode? head3 = LinkedListHelper.Build(new[] { 42 });
Console.WriteLine($"Input:    {LinkedListHelper.ToString(head3)}");
ListNode? result3 = solution.Restructure(head3);
Console.WriteLine($"Output:   {LinkedListHelper.ToString(result3)}");
Console.WriteLine($"Expected: [42]");
Console.WriteLine();

// ── Edge case: two nodes ──────────────────────────────────────────────────────
// pos 1 → neither → 100
// pos 2 → prime   → 200
// Primes: 200, Composites: (empty)
// Interleaved: 200
// Final: 100 → 200
Console.WriteLine("=== Edge Case: Two Nodes ===");
ListNode? head4 = LinkedListHelper.Build(new[] { 100, 200 });
Console.WriteLine($"Input:    {LinkedListHelper.ToString(head4)}");
ListNode? result4 = solution.Restructure(head4);
Console.WriteLine($"Output:   {LinkedListHelper.ToString(result4)}");
Console.WriteLine($"Expected: [100, 200]");
Console.WriteLine();

// ── Edge case: four nodes ─────────────────────────────────────────────────────
// pos 1 → neither  → 1
// pos 2 → prime    → 2
// pos 3 → prime    → 3
// pos 4 → composite→ 4
// Primes: 2 → 3, Composites: 4
// Interleaved: 2 → 4 → 3
// Final: 1 → 2 → 4 → 3
Console.WriteLine("=== Edge Case: Four Nodes ===");
ListNode? head5 = LinkedListHelper.Build(new[] { 1, 2, 3, 4 });
Console.WriteLine($"Input:    {LinkedListHelper.ToString(head5)}");
ListNode? result5 = solution.Restructure(head5);
Console.WriteLine($"Output:   {LinkedListHelper.ToString(result5)}");
Console.WriteLine($