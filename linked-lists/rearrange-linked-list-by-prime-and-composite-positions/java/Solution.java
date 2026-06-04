/*
 * Title: Rearrange Linked List by Prime and Composite Positions
 *
 * Problem Description:
 * You are given the head of a singly linked list containing n nodes.
 * Each node has a 1-based position index (i.e., the first node is at position 1,
 * the second at position 2, etc.).
 *
 * Your task is to rearrange the linked list such that:
 * 1. All nodes at prime-numbered positions appear first, in their original relative order.
 * 2. All nodes at composite-numbered positions appear next, in their original relative order.
 * 3. The node at position 1 (which is neither prime nor composite) is placed at the very end.
 *
 * Return the head of the rearranged linked list. You must perform the rearrangement
 * in-place using O(1) extra space (not counting the output list itself).
 *
 * Constraints:
 * - The number of nodes in the list is in the range [1, 10^5].
 * - 1 <= Node.val <= 10^9
 * - You may not allocate new ListNode objects; only pointer manipulation is allowed.
 *
 * Example 1:
 * Input: head = [10, 20, 30, 40, 50, 60, 70]
 * Positions: 1→10, 2→20, 3→30, 4→40, 5→50, 6→60, 7→70
 * Prime positions: 2, 3, 5, 7 → values: 20, 30, 50, 70
 * Composite positions: 4, 6 → values: 40, 60
 * Position 1: value 10
 * Output: [20, 30, 50, 70, 40, 60, 10]
 *
 * Example 2:
 * Input: head = [5, 15, 25]
 * Positions: 1→5, 2→15, 3→25
 * Prime positions: 2, 3 → values: 15, 25
 * Composite positions: none
 * Position 1: value 5
 * Output: [15, 25, 5]
 */

public class Solution {

    /**
     * Definition for singly-linked list node.
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
     * Determines whether a given integer is a prime number.
     *
     * A prime number is a natural number greater than 1 that has no positive
     * divisors other than 1 and itself.
     *
     * @param n the integer to check
     * @return true if n is prime, false otherwise
     * Time complexity: O(sqrt(n))
     * Space complexity: O(1)
     */
    public boolean isPrime(int n) {
        // Numbers less than 2 are not prime (includes 1, which is neither prime nor composite)
        if (n < 2) return false;

        // 2 is the only even prime number
        if (n == 2) return true;

        // All other even numbers are not prime
        if (n % 2 == 0) return false;

        // Check odd divisors from 3 up to sqrt(n)
        // If n has a factor larger than sqrt(n), it must also have one smaller than sqrt(n)
        for (int i = 3; (long) i * i <= n; i += 2) {
            if (n % i == 0) return false;
        }

        return true;
    }

    /**
     * Determines whether a given integer is a composite number.
     *
     * A composite number is a positive integer greater than 1 that is not prime
     * (i.e., it has at least one positive divisor other than 1 and itself).
     * Note: 1 is neither prime nor composite.
     *
     * @param n the integer to check
     * @return true if n is composite, false otherwise
     * Time complexity: O(sqrt(n))
     * Space complexity: O(1)
     */
    public boolean isComposite(int n) {
        // 1 is neither prime nor composite
        // Numbers less than 2 are not composite
        if (n < 2) return false;

        // A number is composite if and only if it is not prime (and >= 2)
        return !isPrime(n);
    }

    /**
     * Rearranges the linked list so that nodes at prime positions come first,
     * followed by nodes at composite positions, and finally the node at position 1.
     *
     * Algorithm Overview:
     * 1. Traverse the list once, categorizing each node by its position type.
     * 2. Maintain three separate sub-lists (using dummy heads):
     *    - primeList: nodes at prime positions (2, 3, 5, 7, 11, ...)
     *    - compositeList: nodes at composite positions (4, 6, 8, 9, 10, ...)
     *    - posOneNode: the single node at position 1
     * 3. Connect the three sub-lists in order: prime → composite → posOne → null
     *
     * @param head the head of the original singly linked list
     * @return the head of the rearranged linked list
     * Time complexity: O(n * sqrt(n)) where n is the number of nodes
     *                  (O(n) traversal, O(sqrt(position)) per node for primality check)
     *                  In practice, positions are bounded by n <= 10^5, so this is efficient.
     * Space complexity: O(1) extra space (only pointer variables, no new nodes allocated)
     */
    public ListNode rearrangeList(ListNode head) {
        // Edge case: empty list or single node — nothing to rearrange
        if (head == null || head.next == null) {
            return head;
        }

        // -----------------------------------------------------------------------
        // Step 1: Set up dummy head nodes for each of the three categories.
        // Using dummy nodes simplifies appending without null checks.
        // -----------------------------------------------------------------------

        // Dummy head for the sub-list of nodes at PRIME positions
        ListNode primeDummy = new ListNode(0);
        ListNode primeTail = primeDummy; // Tail pointer to efficiently append to prime list

        // Dummy head for the sub-list of nodes at COMPOSITE positions
        ListNode compositeDummy = new ListNode(0);
        ListNode compositeTail = compositeDummy; // Tail pointer for composite list

        // We'll store the node at position 1 directly (there's exactly one such node)
        ListNode posOneNode = null;

        // -----------------------------------------------------------------------
        // Step 2: Traverse the original list, categorizing each node by position.
        // -----------------------------------------------------------------------

        ListNode current = head; // Pointer to traverse the list
        int position = 1;        // 1-based position counter

        while (current != null) {
            // Save the next node before we modify current.next
            ListNode nextNode = current.next;

            // Detach current node from the original list to avoid stale links
            current.next = null;

            if (position == 1) {
                // Position 1 is neither prime nor composite — save it for the end
                posOneNode = current;

            } else if (isPrime(position)) {
                // This node is at a PRIME position — append to prime sub-list
                // Example: positions 2, 3, 5, 7, 11, 13, ...
                primeTail.next = current;
                primeTail = primeTail.next; // Advance the tail pointer

            } else {
                // This node is at a COMPOSITE position — append to composite sub-list
                // Example: positions 4, 6, 8, 9, 10, 12, ...
                compositeTail.next = current;
                compositeTail = compositeTail.next; // Advance the tail pointer
            }

            // Move to the next node in the original list
            current = nextNode;
            position++;
        }

        // -----------------------------------------------------------------------
        // Step 3: Connect the three sub-lists together.
        // Final order: [prime nodes] → [composite nodes] → [position-1 node] → null
        // -----------------------------------------------------------------------

        // Connect the end of the prime list to the start of the composite list
        // primeDummy.next is the actual first prime node (or null if no primes)
        // compositeDummy.next is the actual first composite node (or null if no composites)

        // We need to find where to attach composites:
        // If there are prime nodes, attach composites after the last prime node
        // If there are no prime nodes, the composite list starts the result

        // Connect prime tail → composite list head (skip composite dummy)
        primeTail.next = compositeDummy.next;

        // Connect composite tail → position-1 node
        compositeTail.next = posOneNode;

        // posOneNode.next is already null (we set current.next = null during traversal)
        // So the list is properly terminated.

        // -----------------------------------------------------------------------
        // Step 4: Determine the actual head of the rearranged list.
        // -----------------------------------------------------------------------

        // The result starts with prime nodes if any exist; otherwise composite nodes;
        // otherwise just the position-1 node.
        // primeDummy.next gives us the first prime node (or null if none).

        if (primeDummy.next != null) {
            // There are prime-position nodes — the list starts with them
            return primeDummy.next;
        } else if (compositeDummy.next != null) {
            // No prime-position nodes, but there are composite-position nodes
            return compositeDummy.next;
        } else {
            // Only the position-1 node exists (list had exactly 1 node)
            return posOneNode;
        }
    }

    // =========================================================================
    // Helper Utility Methods
    // =========================================================================

    /**
     * Converts a linked list to a readable string format for printing.
     *
     * @param head the head of the linked list
     * @return a string representation like "[10, 20, 30]"
     * Time complexity: O(n)
     * Space complexity: O(n) for the string builder
     */
    public static String listToString(ListNode head) {
        if (head == null) return "[]";

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

    /**
     * Builds a linked list from an integer array.
     *
     * @param values the array of integer values for the list nodes
     * @return the head of the newly created linked list
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public static ListNode buildList(int[] values) {
        if (values == null || values.length == 0) return null;

        // Create a dummy head to simplify building the list
        ListNode dummy = new ListNode(0);
        ListNode tail = dummy;

        for (int val : values) {
            tail.next = new ListNode(val);
            tail = tail.next;
        }

        return dummy.next;
    }

    // =========================================================================
    // Main Method — Demonstrates the solution with sample inputs
    // =========================================================================

    /**
     * Entry point for demonstrating the rearrangeList solution.
     * Traces through both provided examples and prints results.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        System.out.println("=== Rearrange Linked List by Prime and Composite Positions ===");
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 1: [10, 20, 30, 40, 50, 60, 70]
        // Positions:   1   2   3   4   5   6   7
        // Position 1 (neither): 10
        // Prime positions (2,3,5,7): 20, 30, 50, 70
        // Composite positions (4,6): 40, 60
        // Expected output: [20, 30, 50, 70, 40, 60, 10]
        // -----------------------------------------------------------------------
        System.out.println("--- Example 1 ---");
        int[] values1 = {10, 20, 30, 40, 50, 60, 70};
        ListNode head1 = buildList(values1);
        System.out.println("Input:    " + listToString(head1));
        System.out.println("Positions breakdown:");
        System.out.println("  Position 1 (neither prime nor composite): 10");
        System.out.println("  Prime positions (2,3,5,7): 20, 30, 50, 70");
        System.out.println("  Composite positions (4,6): 40, 60");
        System.out.println("Expected: [20, 30, 50, 70, 40, 60, 10]");

        ListNode result1 = solution.rearrangeList(head1);
        System.out.println("Output:   " + listToString(result1));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2: [5, 15, 25]
        // Positions:   1   2   3
        // Position 1 (neither): 5
        // Prime positions (2,3): 15, 25
        // Composite positions: none
        // Expected output: [15, 25, 5]
        // -----------------------------------------------------------------------
        System.out.println("--- Example 2 ---");
        int[] values2 = {5, 15, 25};
        ListNode head2 = buildList(values2);
        System.out.println("Input:    " + listToString(head2));
        System.out.println("Positions breakdown:");
        System.out.println("  Position 1 (neither prime nor composite): 5");
        System.out.println("  Prime positions (2,3): 15, 25");
        System.out.println("  Composite positions: none");
        System.out.println("Expected: [15, 25, 5]");

        ListNode result2 = solution.rearrangeList(head2);
        System.out.println("Output:   " + listToString(result2));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 3: Single node [42]
        // Only position 1 exists — output is just [42]
        // -----------------------------------------------------------------------
        System.out.println("--- Example 3: Single Node ---");
        int[] values3 = {42};
        ListNode head3 = buildList(values3);
        System.out.println("Input:    " + listToString(head3));
        System.out.println("Expected: [42]");

        ListNode result3 = solution.rearrangeList(head3);
        System.out.println("Output:   " + listToString(result3));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 4: Two nodes [100, 200]
        // Position 1 (neither): 100
        // Prime position (2): 200
        // Composite positions: none
        // Expected output: [200, 100]
        // -----------------------------------------------------------------------
        System.out.println("--- Example 4: Two Nodes ---");
        int[] values4 = {100, 200};
        ListNode head4 = buildList(values4);
        System.out.println("Input:    " + listToString(head4));
        System.out.println("Positions breakdown:");
        System.out.println("  Position 1 (neither): 100");
        System.out.println("  Prime position (2): 200");
        System.out.println("Expected: [200, 100]");

        ListNode result4 = solution.rearrangeList(head4);
        System.out.println("Output:   " + listToString(result4));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 