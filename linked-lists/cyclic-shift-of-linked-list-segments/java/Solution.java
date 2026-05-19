```java
/*
 * Title: Cyclic Shift of Linked List Segments
 * Difficulty: Hard
 * Topic: Linked Lists
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
 *
 * Example 1:
 * Input: head = [1, 2, 3, 4, 5, 6, 7], descriptors = [[2, 5, 2], [1, 7, 3]]
 * - After rotating positions 2–5 ([2,3,4,5]) right by 2: list becomes [1, 4, 5, 2, 3, 6, 7]
 * - After rotating positions 1–7 ([1,4,5,2,3,6,7]) right by 3: list becomes [2, 3, 6, 7, 1, 4, 5]
 * Output: [2, 3, 6, 7, 1, 4, 5]
 *
 * Example 2:
 * Input: head = [10, 20, 30, 40, 50], descriptors = [[1, 3, 4], [3, 5, 1]]
 * - After rotating positions 1–3 ([10,20,30]) right by 4 (4 mod 3 = 1): list becomes [30, 10, 20, 40, 50]
 * - After rotating positions 3–5 ([20,40,50]) right by 1: list becomes [30, 10, 50, 20, 40]
 * Output: [30, 10, 50, 20, 40]
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Solution class for the Cyclic Shift of Linked List Segments problem.
 * This solution applies a series of right rotations on specified sublists
 * of a singly linked list.
 */
public class Solution {

    /**
     * Definition for a singly-linked list node.
     */
    static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
            this.next = null;
        }
    }

    /**
     * Applies a series of segment rotations to a linked list.
     *
     * <p>Algorithm Overview:
     * For each descriptor (left, right, k):
     * 1. Navigate to the node just before position 'left' (the "pre-left" node).
     * 2. Navigate to the node at position 'right' (the "right" node).
     * 3. Calculate the segment length = right - left + 1.
     * 4. Compute effective rotation = k % segmentLength.
     * 5. If effective rotation == 0, skip (no change needed).
     * 6. Otherwise, find the new tail of the segment (node at position right - effectiveK).
     *    The new head of the segment is the node right after the new tail.
     * 7. Rewire the pointers:
     *    - preLeft.next = newHead (the node that was at position right - effectiveK + 1)
     *    - rightNode.next = segmentStart (the original first node of the segment)
     *    - newTail.next = null (temporarily, then it points to what comes after rightNode)
     *    Wait — more carefully:
     *    - Save afterRight = rightNode.next
     *    - preLeft.next = newHead
     *    - rightNode.next = segmentStart (original left node)
     *    - newTail.next = afterRight
     *
     * @param head        the head of the singly linked list
     * @param descriptors a list of [left, right, k] arrays describing each rotation
     * @return the head of the modified linked list after all rotations
     *
     * Time Complexity:  O(D * N) where D = number of descriptors, N = number of nodes
     * Space Complexity: O(1) extra space (only pointer manipulation, no auxiliary data structures)
     */
    public ListNode rotateSegments(ListNode head, int[][] descriptors) {
        // Edge case: if the list is empty or has only one node, no rotation is possible
        if (head == null || head.next == null) {
            return head;
        }

        // Use a dummy node to simplify edge cases where left == 1
        // The dummy node points to the original head
        ListNode dummy = new ListNode(0);
        dummy.next = head;

        // Process each descriptor one by one
        for (int[] descriptor : descriptors) {
            int left = descriptor[0];   // 1-indexed start of segment
            int right = descriptor[1];  // 1-indexed end of segment
            int k = descriptor[2];      // number of right rotations

            // Calculate the length of the segment
            int segmentLength = right - left + 1;

            // Compute the effective rotation (k mod segmentLength)
            // If effectiveK == 0, the segment doesn't change
            int effectiveK = k % segmentLength;
            if (effectiveK == 0) {
                // No rotation needed for this descriptor
                continue;
            }

            // Step 1: Find the node just BEFORE position 'left'
            // We start from dummy (position 0) and move (left - 1) steps
            // After this loop, preLeft.next is the node at position 'left'
            ListNode preLeft = dummy;
            for (int i = 0; i < left - 1; i++) {
                preLeft = preLeft.next;
            }

            // The node at position 'left' (start of the segment)
            ListNode segmentStart = preLeft.next;

            // Step 2: Find the node at position 'right'
            // We start from segmentStart and move (segmentLength - 1) steps
            ListNode rightNode = segmentStart;
            for (int i = 0; i < segmentLength - 1; i++) {
                rightNode = rightNode.next;
            }

            // Step 3: Find the new tail of the rotated segment
            // After a right rotation by effectiveK:
            //   - The last effectiveK nodes move to the front
            //   - The new tail is the node at position (right - effectiveK) within the segment
            //   - That means we move (segmentLength - effectiveK - 1) steps from segmentStart
            //
            // Example: segment = [A, B, C, D, E], effectiveK = 2
            //   New arrangement: [D, E, A, B, C]
            //   New tail = C (position 3 from start, i.e., segmentLength - effectiveK - 1 = 5 - 2 - 1 = 2 steps from A)
            ListNode newTail = segmentStart;
            for (int i = 0; i < segmentLength - effectiveK - 1; i++) {
                newTail = newTail.next;
            }

            // The new head of the rotated segment is the node right after newTail
            ListNode newHead = newTail.next;

            // Step 4: Save the node that comes after the segment (after rightNode)
            ListNode afterRight = rightNode.next;

            // Step 5: Rewire the pointers to perform the rotation
            //
            // Before: ... -> preLeft -> [segmentStart -> ... -> newTail -> newHead -> ... -> rightNode] -> afterRight -> ...
            // After:  ... -> preLeft -> [newHead -> ... -> rightNode -> segmentStart -> ... -> newTail] -> afterRight -> ...
            //
            // (a) Connect preLeft to the new head of the rotated segment
            preLeft.next = newHead;

            // (b) Connect the end of the rotated segment (rightNode) to the original segment start
            rightNode.next = segmentStart;

            // (c) Connect the new tail to whatever came after the segment
            newTail.next = afterRight;
        }

        // The dummy node's next is the (possibly new) head of the list
        return dummy.next;
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /**
     * Builds a linked list from an array of integer values.
     *
     * @param values the array of integer values to build the list from
     * @return the head node of the newly created linked list
     *
     * Time Complexity:  O(N) where N = values.length
     * Space Complexity: O(N) for the nodes created
     */
    public static ListNode buildList(int[] values) {
        if (values == null || values.length == 0) {
            return null;
        }
        // Create a dummy head to simplify building the list
        ListNode dummy = new ListNode(0);
        ListNode current = dummy;
        for (int val : values) {
            current.next = new ListNode(val);
            current = current.next;
        }
        return dummy.next;
    }

    /**
     * Converts a linked list to a readable string representation.
     *
     * @param head the head node of the linked list
     * @return a string in the format "[v1, v2, v3, ...]"
     *
     * Time Complexity:  O(N) where N = number of nodes
     * Space Complexity: O(N) for the string builder
     */
    public static String listToString(ListNode head) {
        StringBuilder sb = new StringBuilder("[");
        ListNode current = head;
        while (current != null) {
            sb.append(current.val);
            if (current.next != null) {
                sb.append(", ");
            }
            current = current.next;
        }
        sb.append("]");
        return sb.toString();
    }

    // =========================================================================
    // Main Method — Demonstrates the solution with sample inputs
    // =========================================================================

    /**
     * Main method to demonstrate the Cyclic Shift of Linked List Segments solution.
     * Traces through both examples from the problem description and prints results.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // Input: head = [1, 2, 3, 4, 5, 6, 7], descriptors = [[2, 5, 2], [1, 7, 3]]
        // Expected Output: [2, 3, 6, 7, 1, 4, 5]
        // -----------------------------------------------------------------------
        System.out.println("=== Example 1 ===");
        int[] values1 = {1, 2, 3, 4, 5, 6, 7};
        int[][] descriptors1 = {{2, 5, 2}, {1, 7, 3}};

        ListNode head1 = buildList(values1);
        System.out.println("Input list:          " + listToString(head1));
        System.out.println("Descriptors:         " + Arrays.deepToString(descriptors1));

        // Trace through manually:
        // Step 1: Rotate positions 2-5 ([2,3,4,5]) right by 2
        //   segmentLength = 4, effectiveK = 2 % 4 = 2
        //   newTail is at position 2 (segmentLength - effectiveK - 1 = 4 - 2 - 1 = 1 step from node 2) => node 3
        //   newHead = node 4
        //   Rewire: 1 -> [4, 5, 2, 3] -> 6 -> 7
        //   List becomes: [1, 4, 5, 2, 3, 6, 7]
        //
        // Step 2: Rotate positions 1-7 ([1,4,5,2,3,6,7]) right by 3
        //   segmentLength = 7, effectiveK = 3 % 7 = 3
        //   newTail is at position (7 - 3 - 1 = 3 steps from node 1) => node 2 (which is now at position 4)
        //   Wait, let's re-trace: list is [1, 4, 5, 2, 3, 6, 7]
        //   segmentStart = node(1), move 3 steps: node(1)->node(4)->node(5)->node(2) => newTail = node(2)
        //   newHead = node(3)
        //   rightNode = node(7)
        //   Rewire: dummy -> [3, 6, 7, 1, 4, 5, 2] -> null
        //   Wait, that gives [3, 6, 7, 1, 4, 5, 2] but expected is [2, 3, 6, 7, 1, 4, 5]
        //
        // Let me re-check: right rotation by 3 means last 3 nodes [3, 6, 7] move to front
        //   [1, 4, 5, 2, 3, 6, 7] -> last 3 = [3, 6, 7], rest = [1, 4, 5, 2]
        //   Result: [3, 6, 7, 1, 4, 5, 2]
        //   But expected is [2, 3, 6, 7, 1, 4, 5]
        //
        // Hmm, let me re-read the problem. "the last k nodes of that sublist move to the front"
        // For [1,4,5,2,3,6,7] rotated right by 3:
        //   last 3 nodes = [3, 6, 7]? No wait, the list after step 1 is [1, 4, 5, 2, 3, 6, 7]
        //   last 3 = [3, 6, 7]? No: positions 5,6,7 = nodes 3, 6, 7
        //   Result should be [3, 6, 7, 1, 4, 5, 2]
        //   But expected output is [2, 3, 6, 7, 1, 4, 5]
        //
        // Wait, let me re-read the example more carefully:
        // "After rotating positions 1–7 ([1,4,5,2,3,6,7]) right by 3: list becomes [2, 3, 6, 7, 1, 4, 5]"
        // [2, 3, 6, 7, 1, 4, 5] — last 3 of [1,4,5,2,3,6,7] would be [3,6,7], giving [3,6,7,1,4,5,2]
        // But the expected shows [2,3,6,7,1,4,5] which means last 4 moved? No...
        // [2,3,6,7] at front, [1,4,5] at back — that's last 4 moved to front? No...
        // Actually [1,4,5,2,3,6,7]