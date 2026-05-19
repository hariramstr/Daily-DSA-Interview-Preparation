/*
 * Title: Cyclic Shift of Linked List Segments
 * 
 * Problem Description:
 * You are given the head of a singly linked list and a list of segment descriptors.
 * Each descriptor is a tuple (left, right, k) meaning: rotate the sublist from position
 * left to position right (1-indexed, inclusive) to the right by k positions.
 * You must apply all segment rotations in the given order and return the head of the
 * modified linked list.
 *
 * A right rotation by k on a sublist means the last k nodes of that sublist move to the
 * front of the sublist, while the rest shift right.
 *
 * If k is greater than the length of the segment, use k modulo the segment length.
 * If k modulo segment length equals 0, the segment remains unchanged.
 *
 * Constraints:
 * - The number of nodes in the list is in the range [1, 10^4]
 * - Node values are in the range [-10^5, 10^5]
 * - 1 <= number of descriptors <= 500
 * - For each descriptor: 1 <= left <= right <= n, 0 <= k <= 10^9
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
    public ListNode(int val = 0, ListNode? next = null)
    {
        Val = val;
        Next = next;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Solution class containing the main algorithm
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /// <summary>
    /// Applies a series of right-rotation operations on segments of a linked list.
    ///
    /// Time Complexity:  O(D * N) where D = number of descriptors, N = number of nodes.
    ///                   Each descriptor requires at most two passes over at most N nodes.
    /// Space Complexity: O(1) extra space — we only manipulate pointers; no auxiliary
    ///                   arrays are allocated (beyond a tiny number of local variables).
    /// </summary>
    public ListNode RotateSegments(ListNode head, int[][] descriptors)
    {
        // ── STEP 1: Guard against an empty list ──────────────────────────────
        // If the list is null or has only one node, no rotation can change it.
        if (head == null || head.Next == null)
            return head;

        // ── STEP 2: Create a sentinel (dummy) head node ──────────────────────
        // A sentinel node placed just before the real head simplifies edge cases
        // where the rotation affects the very first node of the list.
        // Instead of special-casing "the segment starts at position 1", we can
        // always refer to "the node just before the segment start" (prevLeft).
        ListNode sentinel = new ListNode(0, head);

        // ── STEP 3: Process each descriptor one at a time ────────────────────
        // Descriptors must be applied in order because each rotation changes the
        // list structure that the next rotation will operate on.
        foreach (int[] desc in descriptors)
        {
            int left  = desc[0]; // 1-indexed start of the segment
            int right = desc[1]; // 1-indexed end   of the segment
            int k     = desc[2]; // number of right-rotation steps requested

            // ── STEP 3a: Compute the segment length ──────────────────────────
            // The segment spans positions [left, right], so its length is:
            int segLen = right - left + 1;

            // ── STEP 3b: Reduce k modulo segment length ──────────────────────
            // Rotating a segment of length L by L positions brings it back to
            // its original order.  So only k % L matters.
            // If the effective rotation is 0, skip this descriptor entirely.
            k = k % segLen;
            if (k == 0)
                continue;

            // ── STEP 3c: Walk to the node just BEFORE the segment start ──────
            // We need a pointer to the node at position (left - 1) so we can
            // re-attach the rotated segment.  Starting from the sentinel means
            // position 0 = sentinel, position 1 = first real node, etc.
            ListNode prevLeft = sentinel;
            for (int i = 0; i < left - 1; i++)
                prevLeft = prevLeft.Next!;
            // Now prevLeft.Next is the first node of the segment (position `left`).

            // ── STEP 3d: Walk to the last node of the segment ────────────────
            // We also need a pointer to the node at position `right` (the tail
            // of the segment) and to the node at position (right - k), which
            // will become the new tail after rotation.
            //
            // Layout before rotation (example: left=2, right=5, k=2, segLen=4):
            //
            //   sentinel → [1] → [2] → [3] → [4] → [5] → [6] → [7] → null
            //                     ↑                   ↑
            //                  segHead             segTail
            //              prevLeft.Next         (walk right-left steps from segHead)
            //
            // After a RIGHT rotation by k=2, the last k=2 nodes ([4],[5]) move
            // to the front of the segment:
            //   [4] → [5] → [2] → [3]
            //
            // So we need:
            //   newTail  = node at position (right - k)   → becomes the new segment tail
            //   newHead  = node at position (right - k+1) → becomes the new segment head
            //   segTail  = node at position right          → its Next stays as afterSeg

            // segHead is the current first node of the segment
            ListNode segHead = prevLeft.Next!;

            // Walk (segLen - 1) steps from segHead to reach segTail
            ListNode segTail = segHead;
            for (int i = 0; i < segLen - 1; i++)
                segTail = segTail.Next!;
            // segTail is now the last node of the segment (position `right`).

            // afterSeg is the node immediately after the segment (could be null)
            ListNode? afterSeg = segTail.Next;

            // ── STEP 3e: Find the new tail (split point) ─────────────────────
            // For a right rotation by k, the last k nodes move to the front.
            // The split happens after (segLen - k) nodes from the segment start.
            // newTail is at position (segLen - k - 1) steps from segHead (0-indexed).
            // newHead is newTail.Next — the first of the k nodes that move forward.
            //
            // Example: segLen=4, k=2 → split after node index 1 (0-based)
            //   segHead=[2] → [3] → [4] → [5]=segTail
            //                  ↑newTail   ↑newHead
            ListNode newTail = segHead;
            for (int i = 0; i < segLen - k - 1; i++)
                newTail = newTail.Next!;
            // newTail is now the last node of the "stay" portion.

            ListNode newHead = newTail.Next!;
            // newHead is the first node of the "rotate-to-front" portion.

            // ── STEP 3f: Rewire the pointers to perform the rotation ──────────
            // We need to perform three pointer changes:
            //
            //  1. prevLeft.Next  = newHead
            //     (the node before the segment now points to the new segment head)
            //
            //  2. newTail.Next   = afterSeg
            //     (the new segment tail points to whatever came after the old segment)
            //     Wait — actually newTail should point to segHead (the old front part).
            //     Let me re-examine:
            //
            // Before:
            //   prevLeft → segHead → ... → newTail → newHead → ... → segTail → afterSeg
            //
            // After right rotation by k (last k nodes move to front):
            //   prevLeft → newHead → ... → segTail → segHead → ... → newTail → afterSeg
            //
            // So the three rewires are:
            //   A) prevLeft.Next = newHead          (attach new front)
            //   B) segTail.Next  = segHead           (old tail points to old front)
            //   C) newTail.Next  = afterSeg          (new tail points to rest of list)

            prevLeft.Next = newHead;   // A: connect before-segment to new segment head
            segTail.Next  = segHead;   // B: old segment tail wraps around to old head
            newTail.Next  = afterSeg;  // C: new segment tail connects to rest of list
        }

        // ── STEP 4: Return the (possibly new) head of the list ───────────────
        // sentinel.Next is always the current first real node of the list.
        return sentinel.Next!;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helper utilities (build list from array, convert list to string)
// ─────────────────────────────────────────────────────────────────────────────
static ListNode BuildList(int[] values)
{
    // Create a sentinel to simplify building the chain
    ListNode dummy = new ListNode(0);
    ListNode cur   = dummy;
    foreach (int v in values)
    {
        cur.Next = new ListNode(v);
        cur = cur.Next;
    }
    return dummy.Next!;
}

static string ListToString(ListNode? head)
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

// ─────────────────────────────────────────────────────────────────────────────
// Demo / test code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────
var solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// Input : [1, 2, 3, 4, 5, 6, 7]
// Ops   : rotate [2,5] right by 2  → [1, 4, 5, 2, 3, 6, 7]
//         rotate [1,7] right by 3  → [2, 3, 6, 7, 1, 4, 5]
// Expected output: [2, 3, 6, 7, 1, 4, 5]
Console.WriteLine("=== Example 1 ===");
ListNode head1 = BuildList(new[] { 1, 2, 3, 4, 5, 6, 7 });
int[][] descriptors1 = new int[][]
{
    new[] { 2, 5, 2 },
    new[] { 1, 7, 3 }
};
ListNode result1 = solution.RotateSegments(head1, descriptors1);
Console.WriteLine($"Input      : [1, 2, 3, 4, 5, 6, 7]");
Console.WriteLine($"Descriptors: [[2,5,2], [1,7,3]]");
Console.WriteLine($"Output     : {ListToString(result1)}");
Console.WriteLine($"Expected   : [2, 3, 6, 7, 1, 4, 5]");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// Input : [10, 20, 30, 40, 50]
// Ops   : rotate [1,3] right by 4 (4 mod 3 = 1) → [30, 10, 20, 40, 50]
//         rotate [3,5] right by 1               → [30, 10, 50, 20, 40]
// Expected output: [30, 10, 50, 20, 40]
Console.WriteLine("=== Example 2 ===");
ListNode head2 = BuildList(new[] { 10, 20, 30, 40, 50 });
int[][] descriptors2 = new int[][]
{
    new[] { 1, 3, 4 },
    new[] { 3, 5, 1 }
};
ListNode result2 = solution.RotateSegments(head2, descriptors2);
Console.WriteLine($"Input      : [10, 20, 30, 40, 50]");
Console.WriteLine($"Descriptors: [[1,3,4], [3,5,1]]");
Console.WriteLine($"Output     : {ListToString(result2)}");
Console.WriteLine($"Expected   : [30, 10, 50, 20, 40]");
Console.WriteLine();

// ── Edge case: k = 0 (no rotation) ───────────────────────────────────────────
Console.WriteLine("=== Edge Case: k=0 ===");
ListNode head3 = BuildList(new[] { 5, 10, 15 });
int[][] descriptors3 = new int[][] { new[] { 1, 3, 0 } };
ListNode result3 = solution.RotateSegments(head3, descriptors3);
Console.WriteLine($"Input      : [5, 10, 15]");
Console.WriteLine($"Descriptors: [[1,3,0]]");
Console.WriteLine($"Output     : {ListToString(result3)}");
Console.WriteLine($"Expected   : [5, 10, 15]");
Console.WriteLine();

// ── Edge case: single node ────────────────────────────────────────────────────
Console.WriteLine("=== Edge Case: single node ===");
ListNode head4 = BuildList(new[] { 42 });
int[][] descriptors4 = new int[][] { new[] { 1, 1, 7 } };
ListNode result4 = solution.RotateSegments(head4, descriptors4);
Console.WriteLine($"Input      : [42]");
Console.WriteLine($"Descriptors: [[1,1,7]]");
Console.WriteLine($"Output     : {ListToString(result4)}");
Console.WriteLine($"Expected   : [42]");
Console.WriteLine();

// ── Edge case: k equals segment length (full rotation = no change) ────────────
Console.WriteLine("=== Edge Case: k = segment length ===");
ListNode head5 = BuildList(new[] { 1, 2, 3, 4 });
int[][] descriptors5 = new int[][] { new[] { 1, 4, 4 } };
ListNode result5 = solution.RotateSegments(head5, descriptors5);
Console.WriteLine($"Input      : [1, 2, 3, 4]");
Console.WriteLine($"Descriptors: [[1,4,4]]");
Console.WriteLine($"Output     : {ListToString(result5)}");
Console.WriteLine($"Expected   : [1, 2, 3, 4]");