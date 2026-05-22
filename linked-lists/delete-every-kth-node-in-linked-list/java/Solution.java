/*
 * Title: Delete Every K-th Node in a Linked List
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
 * Constraints:
 * - The number of nodes in the list is in the range [1, 10^4].
 * - 1 <= Node.val <= 1000
 * - 1 <= k <= 10^4
 *
 * Example 1:
 * Input: head = [1, 2, 3, 4, 5, 6, 7, 8], k = 3
 * Output: [1, 2, 4, 5, 7, 8]
 * Explanation: The 3rd node (value 3) and the 6th node (value 6) are deleted.
 *
 * Example 2:
 * Input: head = [10, 20, 30, 40, 50], k = 2
 * Output: [10, 30, 50]
 * Explanation: Nodes at positions 2 (value 20) and 4 (value 40) are deleted.
 */

import java.util.ArrayList;
import java.util.List;

/**
 * Solution class for the "Delete Every K-th Node in a Linked List" problem.
 *
 * <p>Approach: We traverse the linked list while maintaining a counter. When the
 * counter reaches k, we skip (delete) that node by adjusting the previous node's
 * next pointer, then reset the counter to 0 and continue.</p>
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
     * Deletes every k-th node from the linked list.
     *
     * <p>Algorithm:
     * 1. Use a dummy/sentinel node before the head to simplify edge cases
     *    (e.g., when the head itself needs to be deleted, i.e., k=1).
     * 2. Maintain a "previous" pointer that always points to the node just
     *    before the current node being examined.
     * 3. Maintain a counter that increments with each node visited.
     * 4. When the counter equals k, delete the current node by setting
     *    prev.next = current.next, then reset the counter to 0.
     * 5. Otherwise, advance the "previous" pointer to the current node.
     * 6. Always advance the current pointer to the next node.
     * </p>
     *
     * @param head the head of the singly linked list
     * @param k    the interval at which nodes should be deleted (every k-th node)
     * @return the head of the modified linked list after deletions
     *
     * Time Complexity:  O(n) — we traverse each node exactly once, where n is
     *                   the number of nodes in the list.
     * Space Complexity: O(1) — we only use a constant amount of extra space
     *                   (a few pointer variables and a counter).
     */
    public ListNode deleteKthNodes(ListNode head, int k) {

        // Step 1: Create a dummy (sentinel) node that points to the head.
        // This simplifies deletion logic, especially when k=1 (deleting the head).
        // dummy -> head -> node2 -> node3 -> ...
        ListNode dummy = new ListNode(0);
        dummy.next = head;

        // Step 2: Initialize pointers.
        // 'prev' starts at the dummy node — it always lags one step behind 'current'.
        // 'current' starts at the head — this is the node we are currently examining.
        ListNode prev = dummy;
        ListNode current = head;

        // Step 3: Initialize a counter to track position within each group of k nodes.
        // We count from 1 upward; when count == k, we delete the current node.
        int count = 0;

        // Step 4: Traverse the entire linked list until we reach the end.
        while (current != null) {

            // Step 4a: Increment the counter for the current node.
            count++;

            // Step 4b: Check if this is the k-th node (time to delete it).
            if (count == k) {
                // DELETE the current node:
                // By setting prev.next = current.next, we "skip over" current,
                // effectively removing it from the list.
                // Example: prev -> [current] -> next  becomes  prev -> next
                prev.next = current.next;

                // Reset the counter to 0 so we start counting the next group of k.
                count = 0;

                // Note: 'prev' does NOT move forward here because the node after
                // the deleted one is now at prev.next, and we haven't examined it yet.
                // We will examine it in the next iteration via 'current = current.next'.

            } else {
                // NOT the k-th node — keep this node and advance 'prev' to it.
                // 'prev' moves forward to 'current' because 'current' is being kept.
                prev = current;
            }

            // Step 4c: Always advance 'current' to the next node in the original list.
            // If we deleted 'current', then current.next is the node after the deleted one.
            // If we kept 'current', then current.next is simply the next node to examine.
            current = current.next;
        }

        // Step 5: Return the head of the modified list.
        // dummy.next is the new head (which may differ from the original head if k=1).
        return dummy.next;
    }

    // =========================================================================
    // Helper Methods for Building and Printing Linked Lists
    // =========================================================================

    /**
     * Builds a linked list from an array of integer values.
     *
     * @param values an array of integers to convert into a linked list
     * @return the head of the newly created linked list, or null if array is empty
     *
     * Time Complexity:  O(n) — iterates through all n values once.
     * Space Complexity: O(n) — creates n new ListNode objects.
     */
    public ListNode buildList(int[] values) {
        // Handle the edge case of an empty array
        if (values == null || values.length == 0) {
            return null;
        }

        // Create a dummy node to simplify building the list
        ListNode dummy = new ListNode(0);
        ListNode current = dummy;

        // Iterate through each value and append a new node to the list
        for (int val : values) {
            current.next = new ListNode(val);
            current = current.next;
        }

        // Return the actual head (skip the dummy node)
        return dummy.next;
    }

    /**
     * Converts a linked list to a readable string format like "[1, 2, 3]".
     *
     * @param head the head of the linked list to convert
     * @return a string representation of the linked list
     *
     * Time Complexity:  O(n) — visits each node once.
     * Space Complexity: O(n) — stores all node values in a list for formatting.
     */
    public String listToString(ListNode head) {
        // Collect all node values into a Java List for easy formatting
        List<Integer> values = new ArrayList<>();
        ListNode current = head;

        while (current != null) {
            values.add(current.val);
            current = current.next;
        }

        // Format the list as "[val1, val2, val3, ...]"
        return values.toString();
    }

    // =========================================================================
    // Main Method — Demonstrates the solution with sample inputs
    // =========================================================================

    /**
     * Main method to demonstrate and test the deleteKthNodes solution.
     *
     * <p>Traces through both examples from the problem description and verifies
     * that the output matches the expected results.</p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        System.out.println("=== Delete Every K-th Node in a Linked List ===\n");

        // -----------------------------------------------------------------
        // Example 1: head = [1, 2, 3, 4, 5, 6, 7, 8], k = 3
        // Expected Output: [1, 2, 4, 5, 7, 8]
        // Trace:
        //   count=1: node(1), keep  -> prev=node(1)
        //   count=2: node(2), keep  -> prev=node(2)
        //   count=3: node(3), DELETE -> prev stays at node(2), count reset to 0
        //   count=1: node(4), keep  -> prev=node(4)
        //   count=2: node(5), keep  -> prev=node(5)
        //   count=3: node(6), DELETE -> prev stays at node(5), count reset to 0
        //   count=1: node(7), keep  -> prev=node(7)
        //   count=2: node(8), keep  -> prev=node(8)
        //   Result: [1, 2, 4, 5, 7, 8] ✓
        // -----------------------------------------------------------------
        System.out.println("--- Example 1 ---");
        int[] values1 = {1, 2, 3, 4, 5, 6, 7, 8};
        int k1 = 3;
        ListNode head1 = solution.buildList(values1);
        System.out.println("Input:    " + solution.listToString(head1));
        System.out.println("k:        " + k1);
        ListNode result1 = solution.deleteKthNodes(head1, k1);
        System.out.println("Output:   " + solution.listToString(result1));
        System.out.println("Expected: [1, 2, 4, 5, 7, 8]");
        System.out.println();

        // -----------------------------------------------------------------
        // Example 2: head = [10, 20, 30, 40, 50], k = 2
        // Expected Output: [10, 30, 50]
        // Trace:
        //   count=1: node(10), keep  -> prev=node(10)
        //   count=2: node(20), DELETE -> prev stays at node(10), count reset to 0
        //   count=1: node(30), keep  -> prev=node(30)
        //   count=2: node(40), DELETE -> prev stays at node(30), count reset to 0
        //   count=1: node(50), keep  -> prev=node(50)
        //   Result: [10, 30, 50] ✓
        // -----------------------------------------------------------------
        System.out.println("--- Example 2 ---");
        int[] values2 = {10, 20, 30, 40, 50};
        int k2 = 2;
        ListNode head2 = solution.buildList(values2);
        System.out.println("Input:    " + solution.listToString(head2));
        System.out.println("k:        " + k2);
        ListNode result2 = solution.deleteKthNodes(head2, k2);
        System.out.println("Output:   " + solution.listToString(result2));
        System.out.println("Expected: [10, 30, 50]");
        System.out.println();

        // -----------------------------------------------------------------
        // Additional Test: k = 1 (delete every node -> empty list)
        // -----------------------------------------------------------------
        System.out.println("--- Additional Test: k = 1 (delete all nodes) ---");
        int[] values3 = {1, 2, 3, 4, 5};
        int k3 = 1;
        ListNode head3 = solution.buildList(values3);
        System.out.println("Input:    " + solution.listToString(head3));
        System.out.println("k:        " + k3);
        ListNode result3 = solution.deleteKthNodes(head3, k3);
        System.out.println("Output:   " + solution.listToString(result3));
        System.out.println("Expected: []");
        System.out.println();

        // -----------------------------------------------------------------
        // Additional Test: k larger than list size (no deletions)
        // -----------------------------------------------------------------
        System.out.println("--- Additional Test: k > list size (no deletions) ---");
        int[] values4 = {5, 10, 15};
        int k4 = 10;
        ListNode head4 = solution.buildList(values4);
        System.out.println("Input:    " + solution.listToString(head4));
        System.out.println("k:        " + k4);
        ListNode result4 = solution.deleteKthNodes(head4, k4);
        System.out.println("Output:   " + solution.listToString(result4));
        System.out.println("Expected: [5, 10, 15]");
        System.out.println();

        // -----------------------------------------------------------------
        // Additional Test: Single node list with k = 1
        // -----------------------------------------------------------------
        System.out.println("--- Additional Test: Single node, k = 1 ---");
        int[] values5 = {42};
        int k5 = 1;
        ListNode head5 = solution.buildList(values5);
        System.out.println("Input:    " + solution.listToString(head5));
        System.out.println("k:        " + k5);
        ListNode result5 = solution.deleteKthNodes(head5, k5);
        System.out.println("Output:   " + solution.listToString(result5));
        System.out.println("Expected: []");
        System.out.println();

        // -----------------------------------------------------------------
        // Additional Test: Single node list with k = 2
        // -----------------------------------------------------------------
        System.out.println("--- Additional Test: Single node, k = 2 ---");
        int[] values6 = {42};
        int k6 = 2;
        ListNode head6 = solution.buildList(values6);
        System.out.println("Input:    " + solution.listToString(head6));
        System.out.println("k:        " + k6);
        ListNode result6 = solution.deleteKthNodes(head6, k6);
        System.out.println("Output:   " + solution.listToString(result6));
        System.out.println("Expected: [42]");
    }
}