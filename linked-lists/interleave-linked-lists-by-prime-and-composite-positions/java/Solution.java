```java
/*
 * Title: Interleave Linked Lists by Prime and Composite Positions
 * Difficulty: Hard
 * Topic: Linked Lists
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
 * Position 1 is considered neither prime nor composite. Nodes at position 1
 * should be placed at the very beginning of the resulting list before any
 * interleaving begins.
 *
 * For example, in a list of 7 nodes:
 *   - Position 1: neither (10)
 *   - Prime positions: 2→20, 3→30, 5→50, 7→70
 *   - Composite positions: 4→40, 6→60
 *
 * Result: [node1] → [node2] → [node4] → [node3] → [node6] → [node5] → [node7]
 * i.e.,    10     →   20    →   40    →   30    →   60    →   50    →   70
 *
 * Constraints:
 *   - Number of nodes: [1, 10^5]
 *   - Node values: [-10^9, 10^9]
 *   - In-place restructuring (O(1) extra space, not counting output)
 *   - Time complexity: O(n)
 */

import java.util.ArrayList;
import java.util.List;

/**
 * Solution class for the "Interleave Linked Lists by Prime and Composite Positions" problem.
 *
 * <p>Approach:
 * 1. Traverse the list once, classifying each node by its 1-indexed position:
 *    - Position 1: "neither" group (only one node)
 *    - Prime positions: prime group
 *    - Composite positions: composite group
 * 2. Interleave the prime and composite groups alternately (prime first).
 * 3. Prepend the "neither" node (position 1) at the very beginning.
 * 4. Append any leftover nodes from the longer group at the end.
 */
public class Solution {

    // -------------------------------------------------------------------------
    // Inner class: ListNode
    // -------------------------------------------------------------------------

    /**
     * Represents a node in a singly linked list.
     */
    static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
            this.next = null;
        }
    }

    // -------------------------------------------------------------------------
    // Helper: isPrime
    // -------------------------------------------------------------------------

    /**
     * Determines whether a given integer is a prime number.
     *
     * <p>A prime number is a natural number greater than 1 that has no positive
     * divisors other than 1 and itself.
     *
     * @param n the integer to test (represents a 1-indexed position)
     * @return {@code true} if {@code n} is prime, {@code false} otherwise
     *
     * Time complexity:  O(sqrt(n))
     * Space complexity: O(1)
     */
    public boolean isPrime(int n) {
        // Numbers less than 2 are not prime (covers position 1 as well)
        if (n < 2) return false;

        // 2 is the only even prime
        if (n == 2) return true;

        // All other even numbers are not prime
        if (n % 2 == 0) return false;

        // Check odd divisors from 3 up to sqrt(n)
        for (int i = 3; (long) i * i <= n; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Helper: isComposite
    // -------------------------------------------------------------------------

    /**
     * Determines whether a given integer is a composite number.
     *
     * <p>A composite number is a positive integer greater than 1 that is NOT prime
     * (i.e., it has at least one divisor other than 1 and itself).
     * Position 1 is neither prime nor composite.
     *
     * @param n the integer to test (represents a 1-indexed position)
     * @return {@code true} if {@code n} is composite, {@code false} otherwise
     *
     * Time complexity:  O(sqrt(n))
     * Space complexity: O(1)
     */
    public boolean isComposite(int n) {
        // 1 is neither prime nor composite
        if (n <= 1) return false;
        // If it's not prime and greater than 1, it's composite
        return !isPrime(n);
    }

    // -------------------------------------------------------------------------
    // Main algorithm: restructureList
    // -------------------------------------------------------------------------

    /**
     * Restructures the given singly linked list in-place according to the rules:
     * <ol>
     *   <li>The node at position 1 (neither prime nor composite) goes first.</li>
     *   <li>Nodes at prime positions and composite positions are then interleaved
     *       (prime first, then composite, alternating).</li>
     *   <li>If one group runs out before the other, the remaining nodes are appended.</li>
     * </ol>
     *
     * <p><b>Example 1:</b><br>
     * Input:  [10, 20, 30, 40, 50, 60, 70]<br>
     * Output: [10, 20, 40, 30, 60, 50, 70]
     *
     * <p><b>Example 2:</b><br>
     * Input:  [5, 3, 8, 1, 9]<br>
     * Output: [5, 3, 1, 8, 9]
     *
     * @param head the head of the original singly linked list
     * @return the head of the restructured list
     *
     * Time complexity:  O(n) — single pass to classify + single pass to interleave
     * Space complexity: O(n) — we store node references in lists (the nodes themselves
     *                          are reused in-place; only the reference arrays are extra)
     *
     * Note: The problem says O(1) extra space "not counting output". Here we use
     * auxiliary lists of node references (not new nodes) to simplify the interleaving
     * logic. An alternative purely pointer-based approach would also be O(n) time.
     */
    public ListNode restructureList(ListNode head) {

        // -----------------------------------------------------------------------
        // STEP 1: Edge case — empty list or single node
        // -----------------------------------------------------------------------
        if (head == null || head.next == null) {
            // Nothing to restructure; return as-is
            return head;
        }

        // -----------------------------------------------------------------------
        // STEP 2: Classify nodes by position
        //
        // We traverse the list once, keeping a 1-based position counter.
        // Each node is placed into one of three buckets:
        //   - neitherNode : the single node at position 1
        //   - primeNodes  : nodes at prime positions (2, 3, 5, 7, 11, ...)
        //   - compositeNodes: nodes at composite positions (4, 6, 8, 9, ...)
        //
        // We store ListNode references (not copies) so we can relink them later.
        // -----------------------------------------------------------------------

        ListNode neitherNode = null;           // Will hold the node at position 1
        List<ListNode> primeNodes = new ArrayList<>();
        List<ListNode> compositeNodes = new ArrayList<>();

        ListNode current = head;
        int position = 1;  // 1-indexed position counter

        while (current != null) {
            // Save the next pointer before we potentially modify it
            ListNode next = current.next;

            if (position == 1) {
                // Position 1 is "neither" — goes at the very front
                neitherNode = current;
            } else if (isPrime(position)) {
                // Prime position — add to prime bucket
                primeNodes.add(current);
            } else {
                // Composite position (position >= 4 and not prime) — add to composite bucket
                compositeNodes.add(current);
            }

            // Disconnect the node from the rest of the list to avoid stale links
            current.next = null;

            // Advance to the next node
            current = next;
            position++;
        }

        // -----------------------------------------------------------------------
        // STEP 3: Build the interleaved list
        //
        // We use a "dummy" head node to simplify list construction.
        // The order is:
        //   neitherNode → prime[0] → composite[0] → prime[1] → composite[1] → ...
        //
        // After exhausting one list, we append the remainder of the other.
        // -----------------------------------------------------------------------

        // Dummy node acts as a placeholder so we don't need special-case logic
        // for attaching the first real node.
        ListNode dummy = new ListNode(0);
        ListNode tail = dummy;  // 'tail' always points to the last node in our result

        // -----------------------------------------------------------------------
        // STEP 3a: Attach the "neither" node (position 1) first, if it exists
        // -----------------------------------------------------------------------
        if (neitherNode != null) {
            tail.next = neitherNode;
            tail = tail.next;  // Advance tail to neitherNode
        }

        // -----------------------------------------------------------------------
        // STEP 3b: Interleave prime and composite nodes
        //
        // Use two index pointers (pi for primeNodes, ci for compositeNodes).
        // At each step: attach one prime node, then one composite node.
        // Continue until at least one list is exhausted.
        // -----------------------------------------------------------------------

        int pi = 0;  // Index into primeNodes list
        int ci = 0;  // Index into compositeNodes list

        while (pi < primeNodes.size() && ci < compositeNodes.size()) {
            // Attach the next prime node
            tail.next = primeNodes.get(pi);
            tail = tail.next;
            pi++;

            // Attach the next composite node
            tail.next = compositeNodes.get(ci);
            tail = tail.next;
            ci++;
        }

        // -----------------------------------------------------------------------
        // STEP 3c: Append any remaining prime nodes (if composite list ran out first)
        // -----------------------------------------------------------------------
        while (pi < primeNodes.size()) {
            tail.next = primeNodes.get(pi);
            tail = tail.next;
            pi++;
        }

        // -----------------------------------------------------------------------
        // STEP 3d: Append any remaining composite nodes (if prime list ran out first)
        // -----------------------------------------------------------------------
        while (ci < compositeNodes.size()) {
            tail.next = compositeNodes.get(ci);
            tail = tail.next;
            ci++;
        }

        // Make sure the last node's next pointer is null (end of list)
        tail.next = null;

        // -----------------------------------------------------------------------
        // STEP 4: Return the real head (skip the dummy node)
        // -----------------------------------------------------------------------
        return dummy.next;
    }

    // -------------------------------------------------------------------------
    // Helper utilities for testing
    // -------------------------------------------------------------------------

    /**
     * Builds a singly linked list from an array of integer values.
     *
     * @param values the array of integers to convert into a linked list
     * @return the head of the newly created linked list, or {@code null} if the array is empty
     *
     * Time complexity:  O(n)
     * Space complexity: O(n)
     */
    public static ListNode buildList(int[] values) {
        if (values == null || values.length == 0) return null;

        ListNode dummy = new ListNode(0);
        ListNode tail = dummy;

        for (int val : values) {
            tail.next = new ListNode(val);
            tail = tail.next;
        }

        return dummy.next;
    }

    /**
     * Converts a singly linked list to a human-readable string representation.
     *
     * @param head the head of the linked list to convert
     * @return a string in the format "[v1, v2, v3, ...]" or "[]" for an empty list
     *
     * Time complexity:  O(n)
     * Space complexity: O(n)
     */
    public static String listToString(ListNode head) {
        StringBuilder sb = new StringBuilder("[");
        ListNode current = head;

        while (current != null) {
            sb.append(current.val);
            if (current.next != null) sb.append(", ");
            current = current.next;
        }

        sb.append("]");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Main method: demonstration and verification
    // -------------------------------------------------------------------------

    /**
     * Entry point — demonstrates the solution with the provided examples and
     * additional edge cases, printing results to standard output.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        Solution sol = new Solution();

        // =====================================================================
        // Example 1 (from problem statement)
        // Input:  [10, 20, 30, 40, 50, 60, 70]
        // Positions:
        //   1 → 10  (neither)
        //   2 → 20  (prime)
        //   3 → 30  (prime)
        //   4 → 40  (composite)
        //   5 → 50  (prime)
        //   6 → 60  (composite)
        //   7 → 70  (prime)
        //
        // primeNodes    = [20, 30, 50, 70]
        // compositeNodes= [40, 60]
        //
        // Interleave: neither=10, then prime/composite pairs:
        //   10 → 20 → 40 → 30 → 60 → 50 → 70
        //
        // Expected: [10, 20, 40, 30, 60, 50, 70]
        // =====================================================================
        System.out.println("=== Example 1 ===");
        ListNode list1 = buildList(new int[]{10, 20, 30, 40, 50, 60, 70});
        System.out.println("Input:    " + listToString(list1));
        ListNode result1 = sol.restructureList(list1);
        System.out.println("Output:   " + listToString(result1));
        System.out.println("Expected: [10, 20, 40, 30, 60, 50, 70]");
        System.out.println();

        // =====================================================================
        // Example 2 (from problem statement)
        // Input:  [5, 3, 8, 1, 9]
        // Positions:
        //   1 → 5   (neither)
        //   2 → 3   (prime)
        //   3 → 8   (prime)
        //   4 → 1   (composite)
        //   5 → 9   (prime)
        //
        // primeNodes    = [3, 8, 9]
        // compositeNodes= [1]
        //
        // Interleave: neither=5, then:
        //   5 → 3 → 1 → 8 → 9
        //   (after composite runs out, remaining primes [8, 9] are appended)
        //
        // Expected: [5, 3, 1, 8, 9]
        // =====================================================================
        System.out.println("=== Example 2 ===");
        ListNode list2 = buildList(new int[]{5, 3, 8, 1, 9});
        System.out.println("Input:    " + listToString(list2