```java
/*
 * Title: Linked List Time Travel Snapshots
 * Difficulty: Hard
 * Topic: Linked Lists
 *
 * Problem Description:
 * You are given a singly linked list where each node contains an integer value and a timestamp.
 * The list is sorted in ascending order by timestamp. You are also given a list of query operations,
 * each of which is one of the following:
 *
 * - INSERT t v: Insert a new node with timestamp t and value v into the correct sorted position.
 * - SNAPSHOT t: Return the sum of values of all nodes with timestamps <= t at the time of this query.
 * - ROLLBACK t: Remove all nodes with timestamps strictly greater than t from the list.
 *
 * After processing all operations, return an array of integers — one result for each SNAPSHOT query.
 *
 * Constraints:
 * - 1 <= number of operations <= 10^5
 * - 1 <= t <= 10^9
 * - -10^4 <= v <= 10^4
 * - Timestamps in INSERT operations are not necessarily unique
 * - The initial linked list may be empty
 * - ROLLBACK does not affect nodes with timestamps equal to t
 * - The answer for each SNAPSHOT query fits in a 32-bit signed integer
 */

import java.util.ArrayList;
import java.util.List;

/**
 * Solution class for the Linked List Time Travel Snapshots problem.
 *
 * <p>Approach:
 * We maintain a sorted singly linked list (sorted by timestamp).
 * - INSERT: find the correct position and insert (maintaining sort order by timestamp).
 * - SNAPSHOT: traverse from head, summing values of nodes with timestamp <= t.
 * - ROLLBACK: find the last node with timestamp <= t and set its next to null,
 *   effectively removing all nodes with timestamp > t.
 *
 * A sentinel (dummy) head node simplifies edge cases for insertion and rollback.
 */
public class Solution {

    /**
     * Inner class representing a node in the singly linked list.
     * Each node holds a timestamp and a value.
     */
    static class Node {
        int timestamp;
        int value;
        Node next;

        /**
         * Constructs a new Node with the given timestamp and value.
         *
         * @param timestamp the time at which this node was inserted
         * @param value     the integer value stored in this node
         */
        Node(int timestamp, int value) {
            this.timestamp = timestamp;
            this.value = value;
            this.next = null;
        }
    }

    /**
     * Processes a series of operations on a sorted linked list and returns
     * the results of all SNAPSHOT queries.
     *
     * <p>Algorithm overview:
     * 1. Build the initial linked list from the provided initial nodes.
     * 2. For each operation:
     *    - INSERT t v: insert a node with timestamp t and value v in sorted order.
     *    - SNAPSHOT t: sum all node values with timestamp <= t.
     *    - ROLLBACK t: truncate the list by removing all nodes with timestamp > t.
     * 3. Collect and return results of SNAPSHOT queries.
     *
     * @param initialTimestamps array of timestamps for the initial list nodes (sorted ascending)
     * @param initialValues     array of values corresponding to each initial timestamp
     * @param operations        array of operation strings like "INSERT 4 15", "SNAPSHOT 4", "ROLLBACK 3"
     * @return an int array containing the result of each SNAPSHOT query in order
     *
     * Time complexity:  O(Q * N) where Q = number of operations, N = current list size
     *                   (each INSERT, SNAPSHOT, ROLLBACK may traverse the entire list)
     * Space complexity: O(N) for the linked list nodes, O(S) for snapshot results
     *                   where S = number of SNAPSHOT operations
     */
    public int[] processOperations(int[] initialTimestamps, int[] initialValues, String[] operations) {

        // -----------------------------------------------------------------------
        // Step 1: Create a dummy (sentinel) head node.
        // Using a dummy head simplifies insertion and rollback at the front of the list
        // because we never need to handle the "update head pointer" special case.
        // -----------------------------------------------------------------------
        Node dummy = new Node(Integer.MIN_VALUE, 0); // sentinel with the smallest possible timestamp

        // -----------------------------------------------------------------------
        // Step 2: Build the initial linked list from the provided arrays.
        // Since the initial list is already sorted by timestamp, we simply append nodes.
        // -----------------------------------------------------------------------
        Node tail = dummy; // 'tail' always points to the last node in the list
        for (int i = 0; i < initialTimestamps.length; i++) {
            // Create a new node and attach it to the end of the list
            tail.next = new Node(initialTimestamps[i], initialValues[i]);
            tail = tail.next; // advance tail to the newly added node
        }

        // -----------------------------------------------------------------------
        // Step 3: Prepare a list to collect SNAPSHOT results.
        // -----------------------------------------------------------------------
        List<Integer> results = new ArrayList<>();

        // -----------------------------------------------------------------------
        // Step 4: Process each operation one by one.
        // -----------------------------------------------------------------------
        for (String op : operations) {
            // Split the operation string by spaces to extract the command and parameters
            String[] parts = op.split(" ");
            String command = parts[0]; // "INSERT", "SNAPSHOT", or "ROLLBACK"

            if (command.equals("INSERT")) {
                // -----------------------------------------------------------
                // INSERT t v: Insert a new node with timestamp t and value v
                // into the correct sorted position (by timestamp).
                // -----------------------------------------------------------
                int t = Integer.parseInt(parts[1]); // the timestamp for the new node
                int v = Integer.parseInt(parts[2]); // the value for the new node

                // Find the correct insertion point:
                // We want to insert AFTER the last node whose timestamp <= t.
                // This preserves the sorted order and handles duplicate timestamps
                // by appending after existing nodes with the same timestamp.
                Node prev = dummy;
                // Traverse while the NEXT node exists and its timestamp <= t
                // (we move prev forward as long as the next node's timestamp is <= t)
                while (prev.next != null && prev.next.timestamp <= t) {
                    prev = prev.next;
                }
                // At this point, prev is the last node with timestamp <= t
                // (or dummy if no such node exists).
                // Insert the new node between prev and prev.next.
                Node newNode = new Node(t, v);
                newNode.next = prev.next; // new node points to what was after prev
                prev.next = newNode;      // prev now points to the new node

                // Note: We do NOT update 'tail' here because tail is only used
                // during initial construction. For operations, we traverse as needed.

            } else if (command.equals("SNAPSHOT")) {
                // -----------------------------------------------------------
                // SNAPSHOT t: Sum all node values with timestamp <= t.
                // -----------------------------------------------------------
                int t = Integer.parseInt(parts[1]); // the snapshot timestamp threshold

                int sum = 0;
                Node current = dummy.next; // start from the first real node (skip dummy)

                // Traverse the list and accumulate values for nodes with timestamp <= t
                while (current != null && current.timestamp <= t) {
                    sum += current.value; // add this node's value to the running sum
                    current = current.next; // move to the next node
                }
                // Since the list is sorted by timestamp, once we see a timestamp > t,
                // all subsequent nodes also have timestamp > t, so we can stop.

                results.add(sum); // record this snapshot result

            } else if (command.equals("ROLLBACK")) {
                // -----------------------------------------------------------
                // ROLLBACK t: Remove all nodes with timestamp STRICTLY GREATER than t.
                // Nodes with timestamp == t are kept.
                // -----------------------------------------------------------
                int t = Integer.parseInt(parts[1]); // the rollback timestamp threshold

                // Find the last node with timestamp <= t.
                // We will set that node's 'next' to null, cutting off the rest of the list.
                Node prev = dummy;
                // Traverse while the NEXT node exists and its timestamp <= t
                while (prev.next != null && prev.next.timestamp <= t) {
                    prev = prev.next;
                }
                // At this point, prev is the last node with timestamp <= t.
                // Everything after prev has timestamp > t, so we cut it off.
                prev.next = null; // truncate the list here
                // All nodes after prev are now unreferenced and will be garbage collected.
            }
            // If the command is unrecognized, we simply skip it (defensive programming).
        }

        // -----------------------------------------------------------------------
        // Step 5: Convert the results list to an int array and return it.
        // -----------------------------------------------------------------------
        int[] answer = new int[results.size()];
        for (int i = 0; i < results.size(); i++) {
            answer[i] = results.get(i);
        }
        return answer;
    }

    /**
     * Helper method to print the current state of the linked list (for debugging).
     *
     * @param dummy the sentinel head node of the linked list
     *
     * Time complexity:  O(N) where N is the number of nodes
     * Space complexity: O(1)
     */
    private static void printList(Node dummy) {
        StringBuilder sb = new StringBuilder("[");
        Node current = dummy.next;
        while (current != null) {
            sb.append("(").append(current.timestamp).append(",").append(current.value).append(")");
            if (current.next != null) sb.append(", ");
            current = current.next;
        }
        sb.append("]");
        System.out.println("  List: " + sb);
    }

    /**
     * Main method demonstrating the solution with the provided examples.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // ===================================================================
        // Example 1:
        // Initial list: [(1, 10), (3, 20), (5, 30)]
        // Operations: ["INSERT 4 15", "SNAPSHOT 4", "ROLLBACK 3", "SNAPSHOT 4"]
        // Expected Output: [75, 30]
        // ===================================================================
        System.out.println("=== Example 1 ===");
        System.out.println("Initial list: [(1,10), (3,20), (5,30)]");
        System.out.println("Operations: [\"INSERT 4 15\", \"SNAPSHOT 4\", \"ROLLBACK 3\", \"SNAPSHOT 4\"]");
        System.out.println("Expected:    [75, 30]");

        int[] ts1 = {1, 3, 5};
        int[] vs1 = {10, 20, 30};
        String[] ops1 = {"INSERT 4 15", "SNAPSHOT 4", "ROLLBACK 3", "SNAPSHOT 4"};

        int[] result1 = solution.processOperations(ts1, vs1, ops1);

        System.out.print("Got:         [");
        for (int i = 0; i < result1.length; i++) {
            System.out.print(result1[i]);
            if (i < result1.length - 1) System.out.print(", ");
        }
        System.out.println("]");

        // Verify correctness
        boolean correct1 = result1.length == 2 && result1[0] == 75 && result1[1] == 30;
        System.out.println("Correct: " + correct1);
        System.out.println();

        // ===================================================================
        // Example 2:
        // Initial list: []
        // Operations: ["INSERT 2 5", "INSERT 2 3", "SNAPSHOT 2", "INSERT 4 10", "ROLLBACK 2", "SNAPSHOT 5"]
        // Expected Output: [8, 8]
        // ===================================================================
        System.out.println("=== Example 2 ===");
        System.out.println("Initial list: []");
        System.out.println("Operations: [\"INSERT 2 5\", \"INSERT 2 3\", \"SNAPSHOT 2\", \"INSERT 4 10\", \"ROLLBACK 2\", \"SNAPSHOT 5\"]");
        System.out.println("Expected:    [8, 8]");

        int[] ts2 = {};
        int[] vs2 = {};
        String[] ops2 = {"INSERT 2 5", "INSERT 2 3", "SNAPSHOT 2", "INSERT 4 10", "ROLLBACK 2", "SNAPSHOT 5"};

        int[] result2 = solution.processOperations(ts2, vs2, ops2);

        System.out.print("Got:         [");
        for (int i = 0; i < result2.length; i++) {
            System.out.print(result2[i]);
            if (i < result2.length - 1) System.out.print(", ");
        }
        System.out.println("]");

        // Verify correctness
        boolean correct2 = result2.length == 2 && result2[0] == 8 && result2[1] == 8;
        System.out.println("Correct: " + correct2);
        System.out.println();

        // ===================================================================
        // Additional Example 3: Edge case — ROLLBACK removes everything
        // Initial list: [(1, 5), (2, 10)]
        // Operations: ["ROLLBACK 0", "SNAPSHOT 10"]
        // Expected Output: [0]  (all nodes removed since all have timestamp > 0)
        // ===================================================================
        System.out.println("=== Example 3 (Edge Case: ROLLBACK removes all) ===");
        System.out.println("Initial list: [(1,5), (2,10)]");
        System.out.println("Operations: [\"ROLLBACK 0\", \"SNAPSHOT 10\"]");
        System.out.println("Expected:    [0]");

        int[] ts3 = {1, 2};
        int[] vs3 = {5, 10};
        String[] ops3 = {"ROLLBACK 0", "SNAPSHOT 10"};

        int[] result3 = solution.processOperations(ts3, vs3, ops3);

        System.out.print("Got:         [");
        for (int i = 0; i < result3.length; i++) {
            System.out.print(result3[i]);
            if (i < result3.length - 1) System.out.print(", ");
        }
        System.out.println("]");

        boolean correct3 = result3.length == 1 && result3[0] == 0;
        System.out.println("Correct: " + correct3);
        System.out.println();

        // ===================================================================
        // Additional Example 4: Negative values
        // Initial list: [(1, -5), (2, 10), (3, -3)]
        // Operations: ["SNAPSHOT 3", "INSERT 2 -2", "SNAPSHOT 3"]
        // Expected Output: [2, 0]
        //   SNAPSHOT 3 (before insert): -5 + 10 + (-3) = 2
        //   After INSERT 2 -2: list is [(1,-5),(2,10),(2,-2),(3,-3)]
        //   SNAPSHOT 3: -5 + 10 + (-2) + (-3) = 0
        // ===================================================================
        System.out.println("=== Example 4 (Negative values) ===");
        System.out.println("Initial list: [(1,-5), (2,10), (3,-3)]");
        System.out.println("Operations: [\"SNAPSHOT 3\", \"INSERT 2 -2\", \"SNAPSHOT 3\"]");
        System.out.println("Expected:    [2, 0]");

        int[] ts4 = {1, 2, 3};
        int[] vs4 = {-5, 10, -3};
        String[] ops4 = {"SNAPSHOT 3", "INSERT 2 -2", "SNAPSHOT 3"};

        int[] result4 = solution.processOperations(ts4, vs4, ops4);

        System.out.print("Got: