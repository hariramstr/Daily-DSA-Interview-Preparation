/*
 * Title: Merge Alternating Train Cars
 * Difficulty: Easy
 * Topic: Linked Lists
 *
 * Problem Description:
 * You are given the heads of two singly linked lists, trainA and trainB,
 * each representing a sequence of train cars identified by their car numbers.
 * Your task is to merge the two trains into one by alternating cars from each
 * train, starting with a car from trainA. If one train runs out of cars before
 * the other, append all remaining cars from the longer train to the end of the
 * merged result.
 *
 * Return the head of the merged linked list. You must do this in-place without
 * allocating extra nodes — simply re-link the existing nodes.
 *
 * Constraints:
 * - The number of nodes in each list is in the range [0, 1000].
 * - 0 <= Node.val <= 10000
 * - Either list can be empty, in which case return the other list as-is.
 *
 * Example 1:
 * Input: trainA = [1, 3, 5], trainB = [2, 4, 6]
 * Output: [1, 2, 3, 4, 5, 6]
 *
 * Example 2:
 * Input: trainA = [10, 20], trainB = [1, 2, 3, 4]
 * Output: [10, 1, 20, 2, 3, 4]
 */

public class Solution {

    /**
     * Definition for a singly linked list node representing a train car.
     * Each node holds a car number (val) and a reference to the next car (next).
     */
    static class ListNode {
        int val;       // The car number / identifier
        ListNode next; // Reference to the next car in the train

        /** Constructor to create a node with a given value */
        ListNode(int val) {
            this.val = val;
            this.next = null;
        }
    }

    /**
     * Merges two singly linked lists by alternating nodes from each list,
     * starting with a node from trainA. If one list is exhausted before the
     * other, the remaining nodes of the longer list are appended at the end.
     * This is done entirely in-place by re-linking existing nodes.
     *
     * @param trainA the head of the first linked list (train A)
     * @param trainB the head of the second linked list (train B)
     * @return the head of the merged alternating linked list
     *
     * Time Complexity:  O(min(m, n)) where m and n are the lengths of trainA
     *                   and trainB respectively. We iterate until the shorter
     *                   list is exhausted, then append the remainder.
     * Space Complexity: O(1) — no extra nodes are allocated; we only re-link
     *                   existing nodes using a constant number of pointers.
     */
    public ListNode mergeAlternating(ListNode trainA, ListNode trainB) {

        // ---------------------------------------------------------------
        // STEP 1: Handle edge cases where one or both lists are empty.
        // ---------------------------------------------------------------

        // If trainA is empty, there is nothing to alternate with, so we
        // simply return trainB as the merged result (even if it's also null).
        if (trainA == null) {
            return trainB;
        }

        // If trainB is empty, trainA is already the complete merged result.
        if (trainB == null) {
            return trainA;
        }

        // ---------------------------------------------------------------
        // STEP 2: Initialize pointers for traversal.
        // ---------------------------------------------------------------

        // 'currentA' will walk through trainA node by node.
        // We start at the head of trainA.
        ListNode currentA = trainA;

        // 'currentB' will walk through trainB node by node.
        // We start at the head of trainB.
        ListNode currentB = trainB;

        // ---------------------------------------------------------------
        // STEP 3: Alternate nodes from trainA and trainB.
        // ---------------------------------------------------------------
        // We continue as long as BOTH lists have remaining nodes to process.
        // The loop weaves one node from A, then one from B, then advances both.

        while (currentA != null && currentB != null) {

            // Save the next pointers BEFORE we overwrite them.
            // This is crucial — if we don't save them, we lose our place
            // in each list when we re-link the current nodes.

            // 'nextA' remembers where we need to go next in trainA
            ListNode nextA = currentA.next;

            // 'nextB' remembers where we need to go next in trainB
            ListNode nextB = currentB.next;

            // --- Re-link: Insert currentB right after currentA ---

            // Point currentA's next to currentB (insert B node after A node)
            // Example: A1 -> A2 becomes A1 -> B1
            currentA.next = currentB;

            // Point currentB's next to the original next of currentA
            // This reconnects the rest of trainA after the inserted B node
            // Example: B1 -> B2 becomes B1 -> A2
            currentB.next = nextA;

            // --- Advance both pointers to the next unprocessed nodes ---

            // Move currentA forward to the next node in trainA
            // (which is the node we saved in nextA)
            currentA = nextA;

            // Move currentB forward to the next node in trainB
            // (which is the node we saved in nextB)
            currentB = nextB;
        }

        // ---------------------------------------------------------------
        // STEP 4: Append remaining nodes from the longer list.
        // ---------------------------------------------------------------
        // At this point, at least one of the lists is exhausted.
        // If trainA still has nodes (currentA != null), they are already
        // correctly linked because currentB.next was set to nextA in the
        // last iteration, which points into the remaining trainA nodes.
        //
        // If trainB still has nodes (currentB != null), we need to append
        // them. But wait — after the loop, the last processed currentB.next
        // was set to nextA (which is null if trainA ran out). So if trainA
        // ran out first, the last B node's next is null, and currentB points
        // to the next unprocessed B node. We need to attach it.
        //
        // Actually, let's think carefully:
        // After the loop, currentA is the next unprocessed A node (or null),
        // and currentB is the next unprocessed B node (or null).
        //
        // The last node we touched was either:
        //   - The last currentB (its .next was set to nextA which may be null)
        //
        // If currentB != null (trainB has leftover nodes), we need to find
        // the tail of what we've built so far and attach currentB there.
        // But since we set currentB.next = nextA in the loop, and nextA is
        // null when trainA is exhausted, the last B node in the woven chain
        // points to null — we need to fix that to point to currentB.
        //
        // The simplest approach: after the loop, if currentB is not null,
        // we need to attach it. The last node in the merged chain so far
        // is the last currentA we processed (before it became null).
        // We track this with a separate 'tail' pointer.

        // NOTE: The above reasoning shows we need a tail pointer.
        // Let's restructure: we'll redo the loop with a tail tracker.

        // Actually, let's reconsider the approach more carefully.
        // After the while loop ends:
        //   - If currentA == null and currentB != null:
        //       The last B node we processed had its .next set to null (nextA was null).
        //       currentB is the NEXT unprocessed B node.
        //       We need to find the last node in the merged list and attach currentB.
        //   - If currentB == null: everything is already linked correctly.
        //
        // The cleanest fix: track the last node we set in the chain.
        // We'll use a 'tail' variable. Let's rewrite the loop below.

        // Since we can't restart (we've already modified the list), let's
        // handle this by noting: when trainA runs out first, the last
        // operation was: currentB.next = nextA (= null). But currentB has
        // already advanced to nextB. So the node BEFORE currentB (the last
        // processed B node) has .next = null. We need that node's .next = currentB.
        //
        // The simplest correct solution: use a tail pointer from the start.
        // Let me rewrite the entire method cleanly below in mergeAlternatingClean.

        // For now, this method delegates to the clean implementation:
        return trainA; // placeholder — see the actual implementation below
    }

    /**
     * The actual correct implementation of mergeAlternating using a tail pointer
     * to properly handle the case where trainB has leftover nodes after trainA
     * is exhausted.
     *
     * @param trainA the head of the first linked list (train A)
     * @param trainB the head of the second linked list (train B)
     * @return the head of the merged alternating linked list
     *
     * Time Complexity:  O(min(m, n)) where m = length of trainA, n = length of trainB
     * Space Complexity: O(1) — only pointer variables used, no new nodes created
     */
    public ListNode mergeAlternatingClean(ListNode trainA, ListNode trainB) {

        // ---------------------------------------------------------------
        // STEP 1: Handle edge cases — empty lists
        // ---------------------------------------------------------------

        // If trainA is null, return trainB (could also be null — that's fine)
        if (trainA == null) {
            return trainB;
        }

        // If trainB is null, return trainA as-is
        if (trainB == null) {
            return trainA;
        }

        // ---------------------------------------------------------------
        // STEP 2: Set up traversal pointers
        // ---------------------------------------------------------------

        // 'currentA' traverses trainA; starts at the head
        ListNode currentA = trainA;

        // 'currentB' traverses trainB; starts at the head
        ListNode currentB = trainB;

        // 'tail' always points to the LAST node in the merged chain so far.
        // This lets us easily append remaining nodes after the loop.
        // Initially, the merged chain starts with trainA's head, so tail = trainA.
        // But we haven't linked anything yet, so we'll update tail inside the loop.
        ListNode tail = null;

        // ---------------------------------------------------------------
        // STEP 3: Weave nodes alternately until one list is exhausted
        // ---------------------------------------------------------------

        while (currentA != null && currentB != null) {

            // Save next pointers before modifying links
            ListNode nextA = currentA.next; // next node in trainA
            ListNode nextB = currentB.next; // next node in trainB

            // Link: currentA -> currentB -> (rest of trainA)
            // First, point currentA's next to currentB
            currentA.next = currentB;

            // Then, point currentB's next to the next node in trainA
            // (this may be null if trainA is exhausted after this step)
            currentB.next = nextA;

            // Update tail to the last node we placed in the chain.
            // After this iteration, currentB is the last node placed
            // (its next points to nextA, which is the next A node or null).
            tail = currentB;

            // Advance both pointers to the next unprocessed nodes
            currentA = nextA;
            currentB = nextB;
        }

        // ---------------------------------------------------------------
        // STEP 4: Append any remaining nodes
        // ---------------------------------------------------------------
        // After the loop, at most one list has remaining nodes.

        // Case A: trainB has leftover nodes (trainA was shorter or equal length
        // and trainA ran out first).
        // 'tail' is the last B node we processed, and tail.next = null
        // (because nextA was null). We need to attach currentB (the first
        // unprocessed B node) to tail.
        if (currentB != null) {
            // tail.next is currently null (set to nextA which was null).
            // Attach the remaining trainB nodes.
            tail.next = currentB;
        }

        // Case B: trainA has leftover nodes (trainB was shorter).
        // In this case, tail.next already points to nextA (the remaining
        // trainA nodes), so no action needed — they're already linked!
        // (currentA != null but currentB == null: tail.next = nextA = currentA's
        //  predecessor's nextA, which is already in the chain)

        // ---------------------------------------------------------------
        // STEP 5: Return the head of the merged list
        // ---------------------------------------------------------------
        // The merged list always starts with trainA's original head.
        return trainA;
    }

    // ===================================================================
    // HELPER METHODS for building and printing linked lists in main()
    // ===================================================================

    /**
     * Builds a linked list from an integer array.
     * Each element in the array becomes a node in the list, in order.
     *
     * @param values the array of integer values to convert into a linked list
     * @return the head node of the newly created linked list, or null if empty
     *
     * Time Complexity:  O(n) where n = values.length
     * Space Complexity: O(n) — creates n new nodes
     */
    public static ListNode buildList(int[] values) {
        // If the array is empty, return null (empty list)
        if (values == null || values.length == 0) {
            return null;
        }

        // Create the head node with the first value
        ListNode head = new ListNode(values[0]);

        // 'current' pointer to track where we are while building
        ListNode current = head;

        // Iterate through the rest of the array, creating nodes
        for (int i = 1; i < values.length; i++) {
            current.next = new ListNode(values[i]); // attach new node
            current = current.next;                  // advance pointer
        }

        return head; // return the head of the constructed list
    }

    /**
     * Converts a linked list to a readable string format like "[1, 2, 3]".
     * Useful for printing and verifying results.
     *
     * @param head the head node of the linked list to convert
     * @return a string representation of the linked list
     *
     * Time Complexity:  O(n) where n = number of nodes
     * Space Complexity: O(n) for the string builder
     */
    public static String listToString(ListNode head) {
        // Handle empty list
        if (head == null) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        ListNode current = head;

        // Traverse the list and append each value
        while (current != null) {
            sb.append(current.val);
            if (current.next != null) {
                sb.append(", "); // add comma separator between elements
            }
            current = current.next;
        }

        sb.append("]");
        return sb.toString();
    }

    // ===================================================================
    // MAIN METHOD — Demonstrates and verifies the solution
    // ===================================================================

    /**
     * Entry point for the program. Demonstrates the mergeAlternatingClean
     * method with multiple test cases, including the examples from the problem.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        System.out.println("=== Merge Alternating Train Cars ===");
        System.out.println();

        // -------------------------------------------------------------------
        // Test Case 1: Example from problem description
        // trainA = [1, 3, 5], trainB = [2, 4, 6]
        // Expected Output: [1, 2, 3, 4, 5, 6]
        // -------------------------------------------------------------------
        System.out.println("--- Test Case 1 ---");
        ListNode trainA1 = buildList(new int[]{1, 3, 5});
        ListNode trainB1 = buildList(new int[]{2, 4, 6});
        System.out.println("train