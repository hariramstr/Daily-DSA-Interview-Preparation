import java.util.*;

/*
 * Title: Reverse Nodes in Even-Length ID Groups
 * Difficulty: Medium
 * Topic: Linked Lists
 *
 * Problem Description:
 * You are given the head of a singly linked list representing a stream of record IDs.
 * Starting from the head, split the list into consecutive groups whose intended sizes
 * are 1, 2, 3, 4, and so on. The last group may contain fewer nodes than its intended
 * size if the list runs out.
 *
 * Your task is to reverse the nodes inside every group whose actual length is even,
 * while leaving odd-length groups unchanged. The groups must remain in the same overall
 * order, and only the node links should be modified. You may not create a second list
 * of all values and rebuild the answer from scratch.
 *
 * For example, if the list is grouped as [a], [b, c], [d, e, f], [g, h, i, j], then
 * the second and fourth groups should be reversed because their lengths are 2 and 4,
 * both even. If the final group has fewer nodes than expected, use its actual size
 * when deciding whether to reverse it.
 *
 * Return the head of the modified linked list.
 *
 * Constraints:
 * - The number of nodes in the list is in the range [1, 100000].
 * - Node values are integers in the range [-1000000000, 1000000000].
 * - The list is singly linked.
 * - Aim for O(n) time complexity.
 * - Extra space should be O(1), excluding recursion stack and input storage.
 *
 * Example 1:
 * Input: head = [5,8,3,9,1,4]
 * Groups: [5], [8,3], [9,1,4]
 * Output: [5,3,8,9,1,4]
 * Explanation: The first group has length 1 (odd), so it stays the same.
 * The second group has length 2 (even), so [8,3] becomes [3,8].
 * The third group has length 3 (odd), so it remains unchanged.
 *
 * Example 2:
 * Input: head = [1,2,3,4,5,6,7,8,9]
 * Groups: [1], [2,3], [4,5,6], [7,8,9]
 * Output: [1,3,2,4,5,6,9,8,7]
 * Explanation: The second group has even length 2, so it is reversed.
 * The third group has odd length 3, so it remains unchanged.
 * The final group was intended to have size 4, but only 3 nodes remain,
 * so its actual length is 3 and it stays in place.
 */

public class Solution {

    /**
     * Definition for singly-linked list.
     */
    public static class ListNode {
        int val;
        ListNode next;

        ListNode() {
        }

        ListNode(int val) {
            this.val = val;
        }

        ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
        }
    }

    /**
     * Reverses every group in the linked list whose actual group length is even.
     *
     * The list is processed in intended group sizes 1, 2, 3, 4, ...
     * If the remaining nodes are fewer than the intended size, the final group uses
     * its actual remaining length. Only groups with even actual length are reversed.
     *
     * @param head the head of the singly linked list
     * @return the head of the modified linked list after reversing even-length groups
     * Time complexity: O(n), because each node is visited a constant number of times
     * Space complexity: O(1), because only a few pointers are used
     */
    public ListNode reverseEvenLengthGroups(ListNode head) {
        // A null list or a single-node list is already valid and needs no changes.
        if (head == null || head.next == null) {
            return head;
        }

        // We use a dummy node to simplify pointer handling, especially when reconnecting
        // groups after reversal. The dummy points to the real head.
        ListNode dummy = new ListNode(0, head);

        // prevGroupTail always points to the node immediately before the current group.
        // Initially, that is the dummy node before the first real node.
        ListNode prevGroupTail = dummy;

        // current points to the first node of the current group we are about to process.
        ListNode current = head;

        // The intended size of groups starts at 1 and increases by 1 each time.
        int intendedGroupSize = 1;

        // Continue until we have processed all nodes in the list.
        while (current != null) {
            // groupHead is the first node of the current group.
            ListNode groupHead = current;

            // We now determine the ACTUAL size of this group.
            // This matters because the last group may be shorter than intended.
            int actualGroupSize = 0;

            // walker is used to move through up to intendedGroupSize nodes.
            ListNode walker = current;

            // Advance walker through the current group to count how many nodes are actually present.
            while (actualGroupSize < intendedGroupSize && walker != null) {
                walker = walker.next;
                actualGroupSize++;
            }

            // At this point:
            // - groupHead is the first node of the group
            // - actualGroupSize is the number of nodes in this group
            // - walker is the first node AFTER the group (possibly null)

            // If the actual group size is even, we must reverse this group.
            if (actualGroupSize % 2 == 0) {
                // Reverse exactly actualGroupSize nodes starting from groupHead.
                // The helper returns:
                // - new head of reversed segment
                // - new tail of reversed segment
                // - next node after the segment
                ReverseResult reversed = reverseExactlyKNodes(groupHead, actualGroupSize);

                // Connect the previous group's tail to the new head of the reversed group.
                prevGroupTail.next = reversed.newHead;

                // Connect the new tail of the reversed group to the next part of the list.
                reversed.newTail.next = reversed.nextAfterGroup;

                // Move prevGroupTail to the tail of the processed group.
                prevGroupTail = reversed.newTail;

                // Move current to the first node of the next group.
                current = reversed.nextAfterGroup;
            } else {
                // If the group size is odd, we do NOT reverse it.
                // We simply move prevGroupTail to the last node of this group.

                // Since walker already points to the node after the group,
                // we can advance prevGroupTail actualGroupSize times from its current position.
                for (int i = 0; i < actualGroupSize; i++) {
                    prevGroupTail = prevGroupTail.next;
                }

                // current becomes the first node after this unchanged group.
                current = walker;
            }

            // Increase intended group size for the next iteration.
            intendedGroupSize++;
        }

        // The real head may have changed if the first processable group were reversed,
        // so we return dummy.next.
        return dummy.next;
    }

    /**
     * Reverses exactly k nodes starting from start.
     *
     * This method assumes that at least k nodes exist starting from start.
     * It reverses only those k nodes and returns enough information to reconnect
     * the reversed segment back into the main list.
     *
     * @param start the first node of the segment to reverse
     * @param k the exact number of nodes to reverse
     * @return a ReverseResult containing:
     *         - the new head of the reversed segment
     *         - the new tail of the reversed segment
     *         - the node immediately after the reversed segment
     * Time complexity: O(k), because exactly k nodes are reversed
     * Space complexity: O(1), because only pointer variables are used
     */
    public ReverseResult reverseExactlyKNodes(ListNode start, int k) {
        // prev will become the head of the reversed portion as we iterate.
        ListNode prev = null;

        // current traverses the nodes being reversed.
        ListNode current = start;

        // Reverse exactly k nodes using the standard iterative linked-list reversal pattern.
        for (int i = 0; i < k; i++) {
            // Save the next node before changing links.
            ListNode nextNode = current.next;

            // Reverse the current node's pointer.
            current.next = prev;

            // Move prev and current one step forward in the reversal process.
            prev = current;
            current = nextNode;
        }

        // After reversal:
        // - prev is the new head of the reversed segment
        // - start is now the tail of the reversed segment
        // - current is the first node after the reversed segment
        return new ReverseResult(prev, start, current);
    }

    /**
     * Builds a linked list from an integer array.
     *
     * @param values the array of values to place into the linked list
     * @return the head of the constructed linked list, or null if the array is empty
     * Time complexity: O(n), where n is the number of values
     * Space complexity: O(n), for the created list nodes
     */
    public static ListNode buildList(int[] values) {
        if (values == null || values.length == 0) {
            return null;
        }

        ListNode dummy = new ListNode(0);
        ListNode tail = dummy;

        for (int value : values) {
            tail.next = new ListNode(value);
            tail = tail.next;
        }

        return dummy.next;
    }

    /**
     * Converts a linked list to a readable string in array-like form.
     *
     * Example: [1,2,3]
     *
     * @param head the head of the linked list
     * @return a string representation of the list
     * Time complexity: O(n), where n is the number of nodes
     * Space complexity: O(n), due to the string builder output
     */
    public static String listToString(ListNode head) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        ListNode current = head;
        while (current != null) {
            sb.append(current.val);
            if (current.next != null) {
                sb.append(",");
            }
            current = current.next;
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstration case
     * Space complexity: O(n) for building demonstration lists
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] example1 = {5, 8, 3, 9, 1, 4};
        ListNode head1 = buildList(example1);
        ListNode result1 = solution.reverseEvenLengthGroups(head1);
        System.out.println("Input:  [5,8,3,9,1,4]");
        System.out.println("Output: " + listToString(result1));
        System.out.println("Expected: [5,3,8,9,1,4]");
        System.out.println();

        // Example 2
        int[] example2 = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNode head2 = buildList(example2);
        ListNode result2 = solution.reverseEvenLengthGroups(head2);
        System.out.println("Input:  [1,2,3,4,5,6,7,8,9]");
        System.out.println("Output: " + listToString(result2));
        System.out.println("Expected: [1,3,2,4,5,6,9,8,7]");
        System.out.println();

        // Additional quick sanity checks
        int[] example3 = {1};
        ListNode head3 = buildList(example3);
        ListNode result3 = solution.reverseEvenLengthGroups(head3);
        System.out.println("Input:  [1]");
        System.out.println("Output: " + listToString(result3));
        System.out.println("Expected: [1]");
        System.out.println();

        int[] example4 = {1, 2};
        ListNode head4 = buildList(example4);
        ListNode result4 = solution.reverseEvenLengthGroups(head4);
        System.out.println("Input:  [1,2]");
        System.out.println("Output: " + listToString(result4));
        System.out.println("Expected: [1,2]");
    }

    /**
     * Small helper object used to return multiple pieces of information after reversing
     * a fixed-size segment of the linked list.
     */
    public static class ReverseResult {
        ListNode newHead;
        ListNode newTail;
        ListNode nextAfterGroup;

        ReverseResult(ListNode newHead, ListNode newTail, ListNode nextAfterGroup) {
            this.newHead = newHead;
            this.newTail = newTail;
            this.nextAfterGroup = nextAfterGroup;
        }
    }
}