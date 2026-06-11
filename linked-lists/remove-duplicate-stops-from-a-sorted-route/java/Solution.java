import java.util.*;

/*
Problem Title: Remove Duplicate Stops from a Sorted Route

Problem Description:
A city transit system stores the stops of a bus route in a singly linked list. The stop IDs are sorted in non-decreasing order because nearby route planning software groups identical stop IDs together. Sometimes duplicate stop entries appear due to data import issues, and you need to clean the route.

Given the head of a singly linked list where each node contains an integer stop ID, remove duplicate nodes so that each stop ID appears only once in the final list. Since the list is already sorted, all duplicates of the same value will appear next to each other. You must modify the linked list in place by updating next pointers and return the head of the cleaned list.

If the list is empty, return null. If the list has only one node, it should be returned unchanged.

Your goal is to keep the first occurrence of each stop ID and remove any immediately repeated copies that follow it.

Constraints:
- The number of nodes in the list is in the range [0, 300].
- -1000 <= Node.val <= 1000
- The linked list is sorted in non-decreasing order.
- Use O(1) extra space, excluding the input list.

Example 1:
Input: head = [4,4,7,7,7,9,12,12]
Output: [4,7,9,12]
Explanation: Repeated stop IDs are adjacent, so each group is reduced to a single node.

Example 2:
Input: head = [1,2,2,3,5,5,8]
Output: [1,2,3,5,8]
Explanation: The first copy of each stop ID is kept, and later duplicates are skipped.

This problem tests careful pointer manipulation on linked lists and the ability to take advantage of sorted input.
*/

public class Solution {

    /**
     * Definition for singly-linked list.
     */
    public static class ListNode {
        int val;
        ListNode next;

        /**
         * Creates a node with the given value.
         *
         * @param val the integer value to store in the node
         */
        ListNode(int val) {
            this.val = val;
        }

        /**
         * Creates a node with the given value and next reference.
         *
         * @param val the integer value to store in the node
         * @param next the next node in the linked list
         */
        ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
        }
    }

    /**
     * Removes duplicate values from a sorted singly linked list in place.
     *
     * Because the list is sorted, any duplicates of a value will always appear
     * directly next to each other. That means we only need to compare each node
     * with its next node:
     * - If both values are the same, we skip the next node by changing pointers.
     * - If the values are different, we move forward.
     *
     * This keeps the first occurrence of each value and removes later adjacent duplicates.
     *
     * @param head the head of the sorted singly linked list; may be null
     * @return the head of the modified list with duplicates removed
     *
     * Time complexity: O(n), where n is the number of nodes in the list,
     * because each node is visited at most once.
     * Space complexity: O(1), because the list is modified in place and no
     * extra data structure proportional to input size is used.
     */
    public ListNode deleteDuplicates(ListNode head) {
        // If the list is empty, there is nothing to clean.
        // Returning null is correct.
        if (head == null) {
            return null;
        }

        // Start a pointer at the head of the list.
        // This pointer will walk through the list and remove duplicates as it goes.
        ListNode current = head;

        // Continue while there is a current node and also a next node to compare with.
        // We need current.next to exist because we compare current.val with current.next.val.
        while (current != null && current.next != null) {

            // Case 1:
            // If the current node and the next node have the same value,
            // then the next node is a duplicate and should be removed.
            if (current.val == current.next.val) {

                // Remove the duplicate node by skipping it.
                // Instead of pointing to the duplicate node,
                // current.next will now point to the node after the duplicate.
                //
                // Example:
                // current -> [7] -> [7] -> [7] -> [9]
                // After one removal:
                // current -> [7] -------> [7] -> [9]
                current.next = current.next.next;

                // Important:
                // We do NOT move current forward here.
                // Why?
                // Because there may be more duplicates immediately after it.
                //
                // Example:
                // [7,7,7,9]
                // After removing one duplicate, we still need to compare
                // the same current node [7] with its new next node [7].
            } else {

                // Case 2:
                // If the values are different, then current is already unique
                // relative to the next node, so we safely move forward.
                current = current.next;
            }
        }

        // The head of the cleaned list remains the same as the original head.
        return head;
    }

    /**
     * Builds a linked list from an integer array.
     *
     * @param values the array of integers to convert into a linked list
     * @return the head of the created linked list, or null if the array is empty
     *
     * Time complexity: O(n), where n is the number of array elements.
     * Space complexity: O(n), due to the newly created linked list nodes.
     */
    public static ListNode buildList(int[] values) {
        // If the input array is null or empty, the linked list is empty.
        if (values == null || values.length == 0) {
            return null;
        }

        // Create the head node from the first value.
        ListNode head = new ListNode(values[0]);

        // Tail will always point to the last node in the list built so far.
        ListNode tail = head;

        // Add the remaining values one by one.
        for (int i = 1; i < values.length; i++) {
            tail.next = new ListNode(values[i]);
            tail = tail.next;
        }

        return head;
    }

    /**
     * Converts a linked list into a string in array-like format.
     *
     * Example output: [1,2,3]
     *
     * @param head the head of the linked list
     * @return a string representation of the linked list
     *
     * Time complexity: O(n), where n is the number of nodes in the list.
     * Space complexity: O(n), for the string builder output.
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
     * Demonstrates the solution using the sample inputs and a few additional edge cases.
     *
     * @param args command-line arguments; not used
     * @return nothing
     *
     * Time complexity: O(n) per demonstration case, where n is the list length.
     * Space complexity: O(n) for building demonstration lists.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Input: [4,4,7,7,7,9,12,12]
        // Expected output: [4,7,9,12]
        ListNode example1 = buildList(new int[]{4, 4, 7, 7, 7, 9, 12, 12});
        ListNode result1 = solution.deleteDuplicates(example1);
        System.out.println("Example 1 Output: " + listToString(result1));

        // Example 2:
        // Input: [1,2,2,3,5,5,8]
        // Expected output: [1,2,3,5,8]
        ListNode example2 = buildList(new int[]{1, 2, 2, 3, 5, 5, 8});
        ListNode result2 = solution.deleteDuplicates(example2);
        System.out.println("Example 2 Output: " + listToString(result2));

        // Edge case: empty list
        ListNode example3 = buildList(new int[]{});
        ListNode result3 = solution.deleteDuplicates(example3);
        System.out.println("Empty List Output: " + listToString(result3));

        // Edge case: single node
        ListNode example4 = buildList(new int[]{42});
        ListNode result4 = solution.deleteDuplicates(example4);
        System.out.println("Single Node Output: " + listToString(result4));

        // Edge case: all duplicates
        ListNode example5 = buildList(new int[]{5, 5, 5, 5, 5});
        ListNode result5 = solution.deleteDuplicates(example5);
        System.out.println("All Duplicates Output: " + listToString(result5));

        // Edge case: no duplicates
        ListNode example6 = buildList(new int[]{1, 2, 3, 4, 5});
        ListNode result6 = solution.deleteDuplicates(example6);
        System.out.println("No Duplicates Output: " + listToString(result6));
    }
}