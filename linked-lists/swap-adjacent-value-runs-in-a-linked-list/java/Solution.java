import java.util.*;

/*
Problem Title: Swap Adjacent Value Runs in a Linked List

Problem Description:
You are given the head of a singly linked list representing a stream of event codes.
Consecutive nodes with the same value form a run. Your task is to rearrange the list
by swapping every two adjacent runs, while preserving the internal order of nodes
inside each run.

A run is a maximal contiguous block of nodes with equal values. For example, in the list
4 -> 4 -> 1 -> 1 -> 1 -> 3 -> 2 -> 2, the runs are [4,4], [1,1,1], [3], and [2,2].
After swapping adjacent runs, the result becomes [1,1,1] -> [4,4] -> [2,2] -> [3].

If the list contains an odd number of runs, the final run stays in its original position.
You must relink existing nodes and should not create a new list of copied values.
The goal is to return the head of the modified list.

This problem tests careful pointer manipulation on linked lists, especially when group
boundaries are determined by values rather than by fixed sizes.

Constraints:
- The number of nodes in the list is in the range [0, 2 * 10^5].
- Node values are in the range [-10^9, 10^9].
- The list is singly linked.
- Your solution should run in O(n) time.
- Extra space should be O(1), excluding recursion stack and input storage.

Example 1:
Input: head = [4,4,1,1,1,3,2,2]
Output: [1,1,1,4,4,2,2,3]
Explanation: The runs are [4,4], [1,1,1], [3], and [2,2]. Swapping adjacent runs gives
[1,1,1], [4,4], [2,2], [3].

Example 2:
Input: head = [7,7,5,6,6,6,9]
Output: [5,7,7,9,6,6,6]
Explanation: The runs are [7,7], [5], [6,6,6], [9]. After swapping adjacent runs, the
first two runs become [5], [7,7], and the next two become [9], [6,6,6].
*/
public class Solution {

    /**
     * Definition for singly-linked list.
     */
    public static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }

        ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
        }
    }

    /**
     * Small helper structure describing one maximal run of equal values.
     * It stores:
     * - the first node of the run
     * - the last node of the run
     * - the node immediately after the run
     *
     * This is only a constant-size helper object used during traversal.
     */
    private static class RunInfo {
        ListNode start;
        ListNode end;
        ListNode nextAfterRun;

        RunInfo(ListNode start, ListNode end, ListNode nextAfterRun) {
            this.start = start;
            this.end = end;
            this.nextAfterRun = nextAfterRun;
        }
    }

    /**
     * Rearranges the linked list by swapping every two adjacent value-runs.
     *
     * Detailed idea:
     * 1. Walk through the list from left to right.
     * 2. Identify the first run.
     * 3. Identify the second run immediately after it.
     * 4. Re-link pointers so the second run comes before the first run.
     * 5. Move forward and repeat.
     * 6. If one final run remains without a partner, leave it unchanged.
     *
     * Important:
     * - We never copy node values.
     * - We only change next pointers.
     * - Internal order inside each run stays exactly the same.
     *
     * @param head the head of the singly linked list
     * @return the head of the modified list after swapping every two adjacent runs
     * Time complexity: O(n), because each node is visited a constant number of times.
     * Space complexity: O(1), because only a few pointers are used.
     */
    public ListNode swapAdjacentRuns(ListNode head) {
        // If the list is empty or has only one node, there is nothing meaningful to swap.
        if (head == null || head.next == null) {
            return head;
        }

        // Dummy node simplifies edge cases:
        // when the very first pair of runs is swapped, the new head changes.
        // By connecting everything through dummy, we avoid special-case code.
        ListNode dummy = new ListNode(0);
        dummy.next = head;

        // prevTail always points to the node that should connect to the next processed part.
        // Initially, that is the dummy node before the real head.
        ListNode prevTail = dummy;

        // current points to the first node of the yet-unprocessed suffix of the list.
        ListNode current = head;

        // Process the list run by run.
        while (current != null) {
            // -----------------------------
            // Step 1: Find the first run.
            // -----------------------------
            RunInfo firstRun = getRun(current);

            // If there is no second run after the first one,
            // then the number of runs remaining is odd (exactly one run left).
            // In that case, we leave this final run in place and we are done.
            if (firstRun.nextAfterRun == null) {
                prevTail.next = firstRun.start;
                break;
            }

            // -----------------------------
            // Step 2: Find the second run.
            // -----------------------------
            RunInfo secondRun = getRun(firstRun.nextAfterRun);

            // At this point we have:
            // prevTail -> firstRun.start ... firstRun.end -> secondRun.start ... secondRun.end -> secondRun.nextAfterRun
            //
            // We want to transform it into:
            // prevTail -> secondRun.start ... secondRun.end -> firstRun.start ... firstRun.end -> secondRun.nextAfterRun

            // -----------------------------
            // Step 3: Connect previous part to the second run.
            // -----------------------------
            prevTail.next = secondRun.start;

            // -----------------------------
            // Step 4: Connect the end of the second run to the start of the first run.
            // This places the first run after the second run.
            // -----------------------------
            secondRun.end.next = firstRun.start;

            // -----------------------------
            // Step 5: Connect the end of the first run to the remainder of the list.
            // -----------------------------
            firstRun.end.next = secondRun.nextAfterRun;

            // -----------------------------
            // Step 6: Advance pointers for the next iteration.
            // After swapping, the first run is now the second run in the pair,
            // so its end becomes the tail of the processed prefix.
            // -----------------------------
            prevTail = firstRun.end;

            // Continue processing from the node after the swapped pair.
            current = secondRun.nextAfterRun;
        }

        return dummy.next;
    }

    /**
     * Finds the maximal contiguous run of equal values starting at the given node.
     *
     * Example:
     * If start points to the first '1' in 1 -> 1 -> 1 -> 3 -> 3,
     * this method returns:
     * - start = first 1
     * - end = third 1
     * - nextAfterRun = first 3
     *
     * @param start the first node of a run
     * @return a RunInfo object describing the run starting at {@code start}
     * Time complexity: O(k), where k is the length of this run.
     * Space complexity: O(1).
     */
    private RunInfo getRun(ListNode start) {
        // The run value is determined by the first node.
        int value = start.val;

        // Move end forward while the next node exists and has the same value.
        ListNode end = start;
        while (end.next != null && end.next.val == value) {
            end = end.next;
        }

        // The node after the run may be null if this run reaches the list end.
        return new RunInfo(start, end, end.next);
    }

    /**
     * Builds a linked list from an integer array.
     *
     * @param values the array of values to place into the linked list
     * @return the head of the constructed linked list
     * Time complexity: O(n), where n is the array length.
     * Space complexity: O(n), for the created list nodes.
     */
    public ListNode buildList(int[] values) {
        ListNode dummy = new ListNode(0);
        ListNode tail = dummy;

        for (int value : values) {
            tail.next = new ListNode(value);
            tail = tail.next;
        }

        return dummy.next;
    }

    /**
     * Converts a linked list into a readable string like [1, 2, 3].
     *
     * @param head the head of the linked list
     * @return a string representation of the list
     * Time complexity: O(n), where n is the number of nodes.
     * Space complexity: O(n), due to the output string construction.
     */
    public String listToString(ListNode head) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

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
     * Demonstrates the solution on sample and additional test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total number of nodes across demonstrated examples).
     * Space complexity: O(total number of nodes created for demonstration).
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Input: [4,4,1,1,1,3,2,2]
        // Runs: [4,4], [1,1,1], [3], [2,2]
        // After swapping adjacent runs:
        // [1,1,1], [4,4], [2,2], [3]
        int[] example1 = {4, 4, 1, 1, 1, 3, 2, 2};
        ListNode head1 = solution.buildList(example1);
        ListNode result1 = solution.swapAdjacentRuns(head1);
        System.out.println("Example 1 Output: " + solution.listToString(result1));
        System.out.println("Expected: [1, 1, 1, 4, 4, 2, 2, 3]");

        // Example 2:
        // Input: [7,7,5,6,6,6,9]
        // Runs: [7,7], [5], [6,6,6], [9]
        // After swapping adjacent runs:
        // [5], [7,7], [9], [6,6,6]
        int[] example2 = {7, 7, 5, 6, 6, 6, 9};
        ListNode head2 = solution.buildList(example2);
        ListNode result2 = solution.swapAdjacentRuns(head2);
        System.out.println("Example 2 Output: " + solution.listToString(result2));
        System.out.println("Expected: [5, 7, 7, 9, 6, 6, 6]");

        // Additional test: empty list
        int[] example3 = {};
        ListNode head3 = solution.buildList(example3);
        ListNode result3 = solution.swapAdjacentRuns(head3);
        System.out.println("Empty List Output: " + solution.listToString(result3));
        System.out.println("Expected: []");

        // Additional test: single run only, should remain unchanged
        int[] example4 = {2, 2, 2, 2};
        ListNode head4 = solution.buildList(example4);
        ListNode result4 = solution.swapAdjacentRuns(head4);
        System.out.println("Single Run Output: " + solution.listToString(result4));
        System.out.println("Expected: [2, 2, 2, 2]");

        // Additional test: odd number of runs
        // Runs: [1,1], [2], [3,3]
        // Swap first two => [2], [1,1], [3,3]
        int[] example5 = {1, 1, 2, 3, 3};
        ListNode head5 = solution.buildList(example5);
        ListNode result5 = solution.swapAdjacentRuns(head5);
        System.out.println("Odd Runs Output: " + solution.listToString(result5));
        System.out.println("Expected: [2, 1, 1, 3, 3]");
    }
}