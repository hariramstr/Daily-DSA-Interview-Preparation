/*
 * Shopping Cart Checkout Queue
 * ============================
 * Difficulty: Easy
 * Topic: Linked Lists
 *
 * Problem Description:
 * You are building a shopping cart system for an online store. Each customer's cart
 * is represented as a singly linked list where each node contains the price of one item.
 * When a customer proceeds to checkout, the system needs to determine if their cart is
 * "balanced" — meaning the total price of items in the first half of the cart equals
 * the total price of items in the second half of the cart.
 *
 * Given the head of a singly linked list representing a customer's cart, return true
 * if the sum of node values in the first half equals the sum of node values in the
 * second half, and false otherwise.
 *
 * If the list has an odd number of nodes, the middle node is excluded from both halves.
 *
 * Constraints:
 * - The number of nodes in the list is in the range [1, 10^4]
 * - 1 <= Node.val <= 1000
 * - You must solve it in O(n) time and O(1) extra space (excluding the output).
 *
 * Example 1:
 * Input: head = [3, 7, 2, 7, 3]
 * Output: true
 * Explanation: First half = [3, 7], sum = 10. Middle node = [2] (excluded).
 *              Second half = [7, 3], sum = 10. Sums are equal.
 *
 * Example 2:
 * Input: head = [1, 4, 9, 2]
 * Output: false
 * Explanation: First half = [1, 4], sum = 5. Second half = [9, 2], sum = 11.
 *              Sums are not equal.
 */

public class Solution {

    /**
     * Definition for singly-linked list node.
     * Each node holds an integer value (item price) and a reference to the next node.
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
     * Determines if a singly linked list representing a shopping cart is "balanced",
     * meaning the sum of the first half equals the sum of the second half.
     * If the list has an odd number of nodes, the middle node is excluded.
     *
     * Algorithm Overview:
     * 1. Use the slow/fast pointer technique to find the middle of the list.
     *    - slow pointer moves 1 step at a time
     *    - fast pointer moves 2 steps at a time
     *    - When fast reaches the end, slow is at the middle
     * 2. While moving the slow pointer to the middle, accumulate the sum of the first half.
     * 3. After finding the middle, accumulate the sum of the second half.
     * 4. Compare the two sums.
     *
     * @param head The head node of the singly linked list (shopping cart).
     * @return true if the sum of the first half equals the sum of the second half,
     *         false otherwise.
     *
     * Time Complexity:  O(n) — we traverse the list at most twice (once to find middle,
     *                   once to sum the second half), where n is the number of nodes.
     * Space Complexity: O(1) — we only use a constant number of extra variables
     *                   (slow, fast, firstHalfSum, secondHalfSum).
     */
    public boolean isBalancedCart(ListNode head) {

        // -----------------------------------------------------------------------
        // EDGE CASE: If the list is empty or has only one node, it's trivially
        // balanced (no halves to compare, or single node is the middle).
        // -----------------------------------------------------------------------
        if (head == null || head.next == null) {
            return true;
        }

        // -----------------------------------------------------------------------
        // STEP 1: Initialize the slow and fast pointers.
        //
        // - 'slow' will advance one node at a time.
        // - 'fast' will advance two nodes at a time.
        // - When 'fast' can no longer advance (reaches end), 'slow' will be at
        //   the middle of the list.
        //
        // We also track 'firstHalfSum' as we move 'slow' forward.
        // -----------------------------------------------------------------------
        ListNode slow = head;  // slow pointer starts at head
        ListNode fast = head;  // fast pointer starts at head
        int firstHalfSum = 0;  // accumulates sum of first half nodes

        // -----------------------------------------------------------------------
        // STEP 2: Move slow and fast pointers until fast reaches the end.
        //
        // Loop condition explained:
        //   - fast != null          → fast hasn't gone past the last node
        //   - fast.next != null     → fast can still take two steps
        //
        // For EVEN length lists (e.g., [1, 4, 9, 2], length=4):
        //   Iteration 1: slow=1, fast=1 → slow moves to 4, fast moves to 9
        //                firstHalfSum = 1
        //   Iteration 2: slow=4, fast=9 → slow moves to 9, fast moves to null
        //                firstHalfSum = 1 + 4 = 5
        //   Loop ends because fast.next == null (fast is at node 2, fast.next=null)
        //   Wait — let me re-trace:
        //
        //   List: 1 -> 4 -> 9 -> 2
        //   Start: slow=node(1), fast=node(1)
        //
        //   Before adding to firstHalfSum, we add slow.val THEN move slow.
        //   Iteration 1: fast=node(1), fast.next=node(4) → both non-null, enter loop
        //                firstHalfSum += slow.val (1) → firstHalfSum = 1
        //                slow = slow.next → slow = node(4)
        //                fast = fast.next.next → fast = node(9)
        //   Iteration 2: fast=node(9), fast.next=node(2) → both non-null, enter loop
        //                firstHalfSum += slow.val (4) → firstHalfSum = 5
        //                slow = slow.next → slow = node(9)
        //                fast = fast.next.next → fast = null
        //   Loop ends: fast == null
        //   slow is now at node(9), which is the start of the second half. ✓
        //
        // For ODD length lists (e.g., [3, 7, 2, 7, 3], length=5):
        //   List: 3 -> 7 -> 2 -> 7 -> 3
        //   Start: slow=node(3), fast=node(3)
        //
        //   Iteration 1: fast=node(3), fast.next=node(7) → enter loop
        //                firstHalfSum += 3 → firstHalfSum = 3
        //                slow = node(7)
        //                fast = node(2)
        //   Iteration 2: fast=node(2), fast.next=node(7) → enter loop
        //                firstHalfSum += 7 → firstHalfSum = 10
        //                slow = node(2)
        //                fast = node(3) [last node]
        //   Loop ends: fast.next == null
        //   slow is now at node(2), which is the MIDDLE node (excluded). ✓
        //   We need to skip slow one more step to get to the second half start.
        // -----------------------------------------------------------------------

        while (fast != null && fast.next != null) {
            // Add the current slow node's value to the first half sum
            firstHalfSum += slow.val;

            // Move slow one step forward
            slow = slow.next;

            // Move fast two steps forward
            fast = fast.next.next;
        }

        // -----------------------------------------------------------------------
        // STEP 3: Determine if the list length is ODD or EVEN.
        //
        // After the loop:
        // - If 'fast' is NOT null (fast points to the last node), the list has
        //   ODD length. The 'slow' pointer is currently at the MIDDLE node,
        //   which should be excluded. We skip it by moving slow one more step.
        //
        // - If 'fast' IS null, the list has EVEN length. The 'slow' pointer is
        //   already at the start of the second half. No adjustment needed.
        //
        // Example (ODD, length=5): After loop, fast=node(3)[last], slow=node(2)[middle]
        //   → fast != null, so we skip the middle: slow = slow.next = node(7)
        //
        // Example (EVEN, length=4): After loop, fast=null, slow=node(9)[second half start]
        //   → fast == null, no adjustment needed.
        // -----------------------------------------------------------------------
        if (fast != null) {
            // ODD length: skip the middle node
            slow = slow.next;
        }

        // -----------------------------------------------------------------------
        // STEP 4: Sum up the second half of the list.
        //
        // 'slow' now points to the first node of the second half.
        // We traverse from 'slow' to the end, accumulating the second half sum.
        // -----------------------------------------------------------------------
        int secondHalfSum = 0;

        while (slow != null) {
            // Add current node's value to the second half sum
            secondHalfSum += slow.val;

            // Move to the next node
            slow = slow.next;
        }

        // -----------------------------------------------------------------------
        // STEP 5: Compare the two sums and return the result.
        //
        // Example 1: firstHalfSum=10, secondHalfSum=10 → 10 == 10 → true ✓
        // Example 2: firstHalfSum=5,  secondHalfSum=11 → 5 == 11  → false ✓
        // -----------------------------------------------------------------------
        return firstHalfSum == secondHalfSum;
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Creates a singly linked list from an array of integers.
     * This is a utility method used in the main method for testing.
     *
     * @param values An array of integers representing node values.
     * @return The head node of the newly created linked list,
     *         or null if the array is empty.
     *
     * Time Complexity:  O(n) — we create one node per element.
     * Space Complexity: O(n) — we allocate n new ListNode objects.
     */
    public static ListNode createList(int[] values) {
        // If the input array is empty, return null (empty list)
        if (values == null || values.length == 0) {
            return null;
        }

        // Create a dummy head node to simplify list construction
        ListNode dummy = new ListNode(0);
        ListNode current = dummy;

        // Iterate through the values array and create nodes
        for (int val : values) {
            current.next = new ListNode(val);
            current = current.next;
        }

        // Return the actual head (skip the dummy node)
        return dummy.next;
    }

    /**
     * Converts a linked list to a readable string format for display.
     *
     * @param head The head node of the linked list.
     * @return A string representation like "[3 -> 7 -> 2 -> 7 -> 3]".
     *
     * Time Complexity:  O(n) — we visit each node once.
     * Space Complexity: O(n) — for the string builder.
     */
    public static String listToString(ListNode head) {
        if (head == null) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        ListNode current = head;

        while (current != null) {
            sb.append(current.val);
            if (current.next != null) {
                sb.append(" -> ");
            }
            current = current.next;
        }

        sb.append("]");
        return sb.toString();
    }

    // =========================================================================
    // MAIN METHOD — Demonstrates the solution with sample inputs
    // =========================================================================

    /**
     * Main method to demonstrate and test the Shopping Cart Checkout Queue solution.
     * Traces through all provided examples and additional edge cases.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        System.out.println("==============================================");
        System.out.println("   Shopping Cart Checkout Queue - Solution   ");
        System.out.println("==============================================");
        System.out.println();

        // ------------------------------------------------------------------
        // Example 1: [3, 7, 2, 7, 3] — ODD length, balanced
        // Expected Output: true
        // First half = [3, 7], sum = 10
        // Middle = [2] (excluded)
        // Second half = [7, 3], sum = 10
        // ------------------------------------------------------------------
        System.out.println("--- Example 1 ---");
        int[] values1 = {3, 7, 2, 7, 3};
        ListNode head1 = createList(values1);
        System.out.println("Input:    " + listToString(head1));
        boolean result1 = solution.isBalancedCart(head1);
        System.out.println("Output:   " + result1);
        System.out.println("Expected: true");
        System.out.println("Explanation: First half [3,7] sum=10, Middle [2] excluded, " +
                           "Second half [7,3] sum=10. Equal → true");
        System.out.println("PASS: " + (result1 == true));
        System.out.println();

        // ------------------------------------------------------------------
        // Example 2: [1, 4, 9, 2] — EVEN length, not balanced
        // Expected Output: false
        // First half = [1, 4], sum = 5
        // Second half = [9, 2], sum = 11
        // ------------------------------------------------------------------
        System.out.println("--- Example 2 ---");
        int[] values2 = {1, 4, 9, 2};
        ListNode head2 = createList(values2);
        System.out.println("Input:    " + listToString(head2));
        boolean result2 = solution.isBalancedCart(head2);
        System.out.println("Output:   " + result2);
        System.out.println("Expected: false");
        System.out.println("Explanation: First half [1,4] sum=5, Second half [9,2] sum=11. " +
                           "Not equal → false");
        System.out.println("PASS: " + (result2 == false));
        System.out.println();

        // ------------------------------------------------------------------
        // Additional Test 1: Single node [5]
        // Expected Output: true (trivially balanced, single node is the middle)
        // ------------------------------------------------------------------
        System.out.println("--- Additional Test 1: Single Node ---");
        int[] values3 = {5};
        ListNode head3 = createList(values3);
        System.out.println("Input:    " + listToString(head3));
        boolean result3 = solution.isBalancedCart(head3);
        System.out.println("Output:   " + result3);
        System.out.println("Expected: true");
        System.out.println("Explanation: Single node is the middle, no halves to compare → true");
        System.out.println("PASS: " + (result3 == true));
        System.out.println();

        // ------------------------------------------------------------------
        // Additional Test 2: Two nodes [5, 5] — EVEN length, balanced
        // Expected Output: true
        // First half = [5], sum = 5
        // Second half = [5], sum = 5
        // ------------------------------------------------------------------
        System.out.println("--- Additional Test 2: Two Equal Nodes ---");
        int[] values4 = {5, 5};