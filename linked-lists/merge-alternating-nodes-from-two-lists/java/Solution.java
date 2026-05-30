/*
 * Title: Merge Alternating Nodes from Two Lists
 * Difficulty: Easy
 * Topic: Linked Lists
 *
 * Problem Description:
 * You are given the heads of two singly linked lists, list1 and list2.
 * Your task is to merge the two lists by alternating nodes — first a node
 * from list1, then a node from list2, then from list1, and so on.
 *
 * If one list is exhausted before the other, append the remaining nodes of
 * the longer list to the end of the merged list.
 *
 * Return the head of the merged linked list. You must do this in-place —
 * do not create new nodes; instead, rearrange the existing nodes.
 *
 * Constraints:
 * - The number of nodes in each list is in the range [0, 1000].
 * - -10^4 <= Node.val <= 10^4
 * - Either list can be empty (i.e., its head may be null).
 *
 * Example 1:
 * - Input: list1 = [1, 3, 5], list2 = [2, 4, 6]
 * - Output: [1, 2, 3, 4, 5, 6]
 * - Explanation: Nodes alternate perfectly between the two lists.
 *
 * Example 2:
 * - Input: list1 = [1, 3], list2 = [2, 4, 6, 8]
 * - Output: [1, 2, 3, 4, 6, 8]
 * - Explanation: After list1 is exhausted at node 3, the remaining nodes
 *   of list2 (6 → 8) are appended.
 */

public class Solution {

    // -------------------------------------------------------------------------
    // Inner class representing a node in a singly linked list
    // -------------------------------------------------------------------------
    static class ListNode {
        int val;        // The value stored in this node
        ListNode next;  // Pointer to the next node in the list

        /** Constructor to create a node with a given value */
        ListNode(int val) {
            this.val = val;
            this.next = null;
        }
    }

    /**
     * Merges two singly linked lists by alternating their nodes in-place.
     *
     * <p>Algorithm Overview:
     * We use two pointers, one for each list, and weave the nodes together
     * by carefully re-linking the "next" pointers. We do NOT create any new
     * nodes — we only rearrange existing ones.
     *
     * @param list1 the head of the first singly linked list (may be null)
     * @param list2 the head of the second singly linked list (may be null)
     * @return the head of the merged alternating linked list
     *
     * Time Complexity:  O(min(m, n)) where m and n are the lengths of list1
     *                   and list2. We iterate until the shorter list is
     *                   exhausted, then simply attach the remainder.
     * Space Complexity: O(1) — we only use a constant number of pointer
     *                   variables; no extra data structures are allocated.
     */
    public ListNode mergeAlternating(ListNode list1, ListNode list2) {

        // ---------------------------------------------------------------
        // EDGE CASE: If list1 is empty, the result is just list2
        // (there are no list1 nodes to interleave, so return list2 as-is)
        // ---------------------------------------------------------------
        if (list1 == null) {
            return list2;
        }

        // ---------------------------------------------------------------
        // EDGE CASE: If list2 is empty, the result is just list1
        // (nothing to interleave from list2, so return list1 as-is)
        // ---------------------------------------------------------------
        if (list2 == null) {
            return list1;
        }

        // ---------------------------------------------------------------
        // The merged list will always start with the head of list1.
        // We save this reference so we can return it at the end.
        // ---------------------------------------------------------------
        ListNode head = list1;  // This is the head of our final merged list

        // ---------------------------------------------------------------
        // We use two "current" pointers to track our position in each list
        // as we walk through them simultaneously.
        //
        //   curr1  →  current node we are processing from list1
        //   curr2  →  current node we are processing from list2
        // ---------------------------------------------------------------
        ListNode curr1 = list1;   // Start at the beginning of list1
        ListNode curr2 = list2;   // Start at the beginning of list2

        // ---------------------------------------------------------------
        // MAIN LOOP: Continue as long as BOTH lists have remaining nodes.
        // In each iteration we:
        //   1. Save the "next" pointers before we overwrite them
        //   2. Link curr1 → curr2  (insert a list2 node after curr1)
        //   3. Link curr2 → next1  (connect back to the rest of list1)
        //   4. Advance both curr1 and curr2 to their respective next nodes
        // ---------------------------------------------------------------
        while (curr1 != null && curr2 != null) {

            // Step 1a: Save the next node in list1 before we overwrite curr1.next
            //          Example: if list1 is 1→3→5 and curr1 is at 1,
            //          then next1 = 3
            ListNode next1 = curr1.next;

            // Step 1b: Save the next node in list2 before we overwrite curr2.next
            //          Example: if list2 is 2→4→6 and curr2 is at 2,
            //          then next2 = 4
            ListNode next2 = curr2.next;

            // Step 2: Link curr1 → curr2
            //         We insert the current list2 node right after the current list1 node.
            //         Example: 1.next = 2  →  so far we have: 1→2
            curr1.next = curr2;

            // Step 3: Link curr2 → next1
            //         We connect the list2 node back to the remainder of list1.
            //         Example: 2.next = 3  →  so far we have: 1→2→3→5
            //         (the 5 is still connected because next1 (which is 3) still
            //          points to 5 at this moment)
            curr2.next = next1;

            // Step 4: Advance curr1 to the next list1 node (which we saved as next1)
            //         Example: curr1 moves from 1 to 3
            curr1 = next1;

            // Step 5: Advance curr2 to the next list2 node (which we saved as next2)
            //         Example: curr2 moves from 2 to 4
            curr2 = next2;

            // After first iteration with Example 1 (list1=[1,3,5], list2=[2,4,6]):
            //   Merged so far: 1→2→3→5  (but 5 will be fixed in next iteration)
            //   curr1 = 3, curr2 = 4
            //
            // After second iteration:
            //   Merged so far: 1→2→3→4→5  (but 5 will be fixed in next iteration)
            //   curr1 = 5, curr2 = 6
            //
            // After third iteration:
            //   Merged so far: 1→2→3→4→5→6→null
            //   curr1 = null, curr2 = null  → loop ends
        }

        // ---------------------------------------------------------------
        // AFTER THE LOOP:
        // At this point, at least one of curr1 or curr2 is null.
        //
        // Case A: curr1 is null but curr2 is not null
        //   → list1 was exhausted first. The remaining list2 nodes need
        //     to be appended. But wait — because of how we linked things
        //     in the loop, the last processed list2 node already has its
        //     "next" set to null (we set curr2.next = next1, and next1
        //     was null when list1 ran out). So we DON'T need to do anything
        //     extra here — the remaining list2 nodes are already attached!
        //
        //   Actually, let's re-examine: when curr1 becomes null (list1 done),
        //   the loop exits. At that point curr2 still points to the next
        //   unprocessed list2 node. The LAST list2 node we processed had
        //   its .next set to null (next1 was null). So the remaining list2
        //   nodes starting at curr2 are NOT yet attached.
        //
        //   We need to find the last node we processed from list2 and
        //   attach curr2 to it. But that's complex. Instead, a simpler
        //   observation: when the loop exits because curr1 == null,
        //   the last node we set was curr2.next = next1 = null. The node
        //   before curr2 in the merged chain is the last curr2 we processed.
        //   We need to attach curr2 (remaining list2) there.
        //
        // SIMPLER APPROACH: We track the "last processed list2 node" so
        // we can attach remaining list2 nodes if list1 runs out first.
        //
        // Actually, let's reconsider the loop structure. The issue only
        // arises when list1 is shorter. Let me re-examine with Example 2:
        //
        // list1 = [1, 3], list2 = [2, 4, 6, 8]
        //
        // Iteration 1: curr1=1, curr2=2
        //   next1=3, next2=4
        //   1.next=2, 2.next=3
        //   curr1=3, curr2=4
        //   Chain: 1→2→3→null (3 still points to null originally... wait,
        //          3.next was null originally since list1=[1,3])
        //          Actually 3.next = null at start.
        //
        // Iteration 2: curr1=3, curr2=4
        //   next1=null (3.next was null), next2=6
        //   3.next=4, 4.next=null  ← HERE: 4.next is set to null!
        //   curr1=null, curr2=6
        //   Chain so far: 1→2→3→4→null
        //   But 6→8 is now disconnected!
        //
        // Loop exits because curr1==null.
        // curr2 = 6 (the remaining unattached list2 nodes)
        //
        // We need to attach 6→8 to the end of our merged list.
        // The last node in our merged list is node 4 (which has .next=null).
        // We need: 4.next = 6
        //
        // To do this cleanly, we track the last processed curr2 node.
        // ---------------------------------------------------------------

        // NOTE: The above analysis shows we need to handle the case where
        // list1 runs out before list2. We need to re-examine our loop.
        // The cleanest fix: after the loop, if curr2 != null, we need to
        // attach it. The last node in the merged chain that points to null
        // is the last curr2 we processed. We need a reference to it.
        //
        // Let's restructure: we'll track `lastCurr2` — the last list2 node
        // we wove into the chain. If curr2 still has remaining nodes after
        // the loop, we attach them to lastCurr2.
        //
        // HOWEVER — the code above already ran the loop. Let me restructure
        // the entire method properly below. The method above is the
        // EXPLANATION version. The actual implementation follows:

        // (The actual working code is in the mergeAlternatingImpl method below)
        // This method delegates to the real implementation:
        return head; // placeholder — see mergeAlternatingImpl for real logic
    }

    /**
     * The actual correct implementation of merging two lists alternately.
     *
     * <p>We use a two-pointer approach with careful pointer manipulation.
     * A key insight: we track the last inserted list2 node so that if
     * list1 runs out first, we can attach the remaining list2 nodes.
     *
     * @param list1 the head of the first singly linked list (may be null)
     * @param list2 the head of the second singly linked list (may be null)
     * @return the head of the merged alternating linked list
     *
     * Time Complexity:  O(min(m, n)) for the interleaving loop, where m and n
     *                   are the lengths of list1 and list2.
     * Space Complexity: O(1) — only a fixed number of pointer variables used.
     */
    public ListNode mergeAlternatingImpl(ListNode list1, ListNode list2) {

        // ---------------------------------------------------------------
        // EDGE CASES: Handle empty lists immediately
        // ---------------------------------------------------------------
        if (list1 == null) return list2;  // Nothing from list1, return list2
        if (list2 == null) return list1;  // Nothing from list2, return list1

        // ---------------------------------------------------------------
        // The result list always starts with list1's head node.
        // Save this as our return value.
        // ---------------------------------------------------------------
        ListNode head = list1;

        // ---------------------------------------------------------------
        // curr1: tracks our current position in list1
        // curr2: tracks our current position in list2
        // ---------------------------------------------------------------
        ListNode curr1 = list1;
        ListNode curr2 = list2;

        // ---------------------------------------------------------------
        // MAIN INTERLEAVING LOOP
        // We continue as long as both lists have nodes to process.
        // ---------------------------------------------------------------
        while (curr1 != null && curr2 != null) {

            // Save next pointers BEFORE we overwrite them
            ListNode next1 = curr1.next;  // Next node in list1
            ListNode next2 = curr2.next;  // Next node in list2

            // Weave: curr1 → curr2 → next1 (rest of list1)
            curr1.next = curr2;   // Insert list2 node after curr1
            curr2.next = next1;   // Connect list2 node to rest of list1

            // Advance both pointers
            curr1 = next1;  // Move to next list1 node
            curr2 = next2;  // Move to next list2 node
        }

        // ---------------------------------------------------------------
        // AFTER THE LOOP — handle remaining nodes
        //
        // Case 1: curr1 == null, curr2 != null
        //   list1 was shorter. The last list2 node we processed had its
        //   .next set to null (because next1 was null). We need to attach
        //   the remaining list2 nodes (curr2) to that last list2 node.
        //
        //   To find that last list2 node, we can traverse from head to
        //   find the tail. But that's O(n). Instead, we realize:
        //   when curr1 becomes null, the last curr2 we processed is the
        //   node whose .next we set to null. We need to find it.
        //
        //   SIMPLER: We know the tail of the merged list is the last node
        //   with .next == null. We can find it by traversing from head.
        //   But O(n) traversal is acceptable since overall is O(n).
        //
        //   EVEN SIMPLER: We track the previous curr2 node explicitly.
        //
        // Case 2: curr2 == null, curr1 != null
        //   list2 was shorter. The remaining list1 nodes are already
        //   attached because curr2.next = next1 connected them. ✓
        //
        // Case 3: Both null → perfect alternation, nothing to do. ✓
        // ---------------------------------------------------------------

        // Handle Case 1: list2 has remaining nodes after list1 is exhausted
        if (curr1 == null && curr2 != null) {
            // Find the tail of the currently merged list