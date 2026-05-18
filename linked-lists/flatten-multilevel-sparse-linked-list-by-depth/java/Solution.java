/*
 * Title: Flatten a Multilevel Sparse Linked List by Depth
 *
 * Problem Description:
 * You are given the head of a multilevel linked list. Each node contains an integer value,
 * a `next` pointer to the next node at the same level, and a `child` pointer that may point
 * to the head of another linked list (creating a new sub-level). Your task is to flatten the
 * multilevel linked list into a single-level linked list such that nodes are ordered
 * **by their depth first, then by their original left-to-right order within each depth level**.
 *
 * In other words, collect all nodes at depth 1 (the top level) in order, then all nodes at
 * depth 2 in order (left-to-right across all sub-lists at that depth), then depth 3, and so on.
 * Return the head of the resulting flattened list. The `child` and `next` pointers of the
 * returned list should all be set to null except for `next` which chains the flattened result.
 *
 * Constraints:
 * - The number of nodes in the list is in the range [1, 10^4].
 * - Node values are in the range [-10^5, 10^5].
 * - The depth of nesting is at most 1000.
 * - No cycles exist in the list.
 *
 * Example 1:
 * Input: 1 - 2 - 3, where node 2 has a child list 4 - 5, and node 5 has a child list 6 - 7.
 * Depth 1: [1, 2, 3]
 * Depth 2: [4, 5]
 * Depth 3: [6, 7]
 * Output: 1 -> 2 -> 3 -> 4 -> 5 -> 6 -> 7
 *
 * Example 2:
 * Input: 10 - 20 - 30, where node 10 has child 11 - 12 and node 20 has child 21 - 22.
 * Node 12 has child 100.
 * Depth 1: [10, 20, 30]
 * Depth 2: [11, 12, 21, 22]
 * Depth 3: [100]
 * Output: 10 -> 20 -> 30 -> 11 -> 12 -> 21 -> 22 -> 100
 */

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Solution class for flattening a multilevel linked list by depth (BFS level-order traversal).
 *
 * <p>The key insight is that this is essentially a Breadth-First Search (BFS) problem:
 * we process all nodes at depth 1 first, then depth 2, then depth 3, etc.
 * Within each depth level, we maintain left-to-right order.
 */
public class Solution {

    /**
     * Node class representing each element in the multilevel linked list.
     * Each node has a value, a next pointer (same level), and a child pointer (sub-level).
     */
    static class Node {
        int val;
        Node next;
        Node child;

        /**
         * Constructor for creating a new Node.
         *
         * @param val the integer value stored in this node
         */
        Node(int val) {
            this.val = val;
            this.next = null;
            this.child = null;
        }
    }

    /**
     * Flattens a multilevel linked list by depth (BFS order).
     *
     * <p>Algorithm Overview:
     * We use a BFS (Breadth-First Search) approach with a queue. We process nodes
     * level by level. For each node at the current level, we add it to our result list
     * and enqueue its child (if any) for the next level's processing.
     *
     * <p>Step-by-step:
     * 1. Start with the head node in a queue representing depth-1 nodes.
     * 2. Process each "level" by iterating through nodes at that depth.
     * 3. For each node, record its value and check if it has a child.
     * 4. If it has a child, add that child's entire chain to the next level's queue.
     * 5. After processing all nodes at the current depth, move to the next depth.
     * 6. Build the flattened linked list from the collected nodes in order.
     *
     * @param head the head node of the multilevel linked list
     * @return the head of the flattened single-level linked list, or null if input is null
     *
     * @implNote Time Complexity: O(N) where N is the total number of nodes.
     *           Each node is visited exactly once.
     *           Space Complexity: O(N) for the queue and result list storage.
     */
    public Node flatten(Node head) {
        // Edge case: if the list is empty, return null immediately
        if (head == null) {
            return null;
        }

        // -----------------------------------------------------------------------
        // Step 1: Collect all nodes in BFS (depth-level) order
        // We use a list to store nodes in the final flattened order
        // -----------------------------------------------------------------------
        List<Node> flattenedNodes = new ArrayList<>();

        // We use a queue to process nodes level by level (BFS).
        // Each element in the queue is the HEAD of a sub-list at the next depth level.
        // Initially, we add the head of the top-level list.
        Queue<Node> levelQueue = new LinkedList<>();
        levelQueue.offer(head);

        // -----------------------------------------------------------------------
        // Step 2: BFS loop — process one "depth level" per outer iteration
        // -----------------------------------------------------------------------
        while (!levelQueue.isEmpty()) {
            // We need to process exactly the nodes currently in the queue
            // (these all belong to the same depth level).
            // Any children we discover will be added to the queue for the NEXT level.
            int levelSize = levelQueue.size();

            // This temporary queue collects the child-list heads found at this level,
            // in left-to-right order, to be processed in the next BFS iteration.
            Queue<Node> nextLevelQueue = new LinkedList<>();

            // -----------------------------------------------------------------------
            // Step 3: Process all sub-lists at the current depth level
            // -----------------------------------------------------------------------
            for (int i = 0; i < levelSize; i++) {
                // Dequeue the head of one sub-list at the current depth
                Node currentListHead = levelQueue.poll();

                // -----------------------------------------------------------------------
                // Step 4: Traverse the entire sub-list horizontally (left to right)
                // -----------------------------------------------------------------------
                Node current = currentListHead;
                while (current != null) {
                    // Add this node to our flattened result
                    flattenedNodes.add(current);

                    // If this node has a child, that child's list belongs to the NEXT depth.
                    // We enqueue the child's head so it gets processed in the next BFS round.
                    if (current.child != null) {
                        nextLevelQueue.offer(current.child);
                    }

                    // Move to the next node in the current sub-list
                    current = current.next;
                }
            }

            // -----------------------------------------------------------------------
            // Step 5: After processing all sub-lists at this depth,
            // the next level's sub-lists become the new queue
            // -----------------------------------------------------------------------
            levelQueue = nextLevelQueue;
        }

        // -----------------------------------------------------------------------
        // Step 6: Build the flattened linked list from the collected nodes
        // We chain them together using the 'next' pointer and clear all 'child' pointers
        // -----------------------------------------------------------------------
        for (int i = 0; i < flattenedNodes.size(); i++) {
            Node node = flattenedNodes.get(i);

            // Clear the child pointer — the flattened list has no sub-levels
            node.child = null;

            // Set the next pointer:
            // - If this is not the last node, point to the next node in the flattened list
            // - If this is the last node, set next to null (end of list)
            if (i < flattenedNodes.size() - 1) {
                node.next = flattenedNodes.get(i + 1);
            } else {
                node.next = null;
            }
        }

        // The head of the flattened list is the first node we collected (depth-1, leftmost)
        return flattenedNodes.get(0);
    }

    // =========================================================================
    // Helper Methods for Building Test Cases and Printing Results
    // =========================================================================

    /**
     * Converts a flattened linked list to a readable string representation.
     *
     * @param head the head of the linked list to convert
     * @return a string like "1 -> 2 -> 3 -> null"
     *
     * @implNote Time Complexity: O(N) — visits each node once.
     *           Space Complexity: O(N) — for the string builder.
     */
    public static String listToString(Node head) {
        StringBuilder sb = new StringBuilder();
        Node current = head;
        while (current != null) {
            sb.append(current.val);
            if (current.next != null) {
                sb.append(" -> ");
            }
            current = current.next;
        }
        return sb.toString();
    }

    /**
     * Main method demonstrating the solution with the provided examples.
     *
     * <p>Example 1 Trace:
     * Input structure:
     *   Level 1: 1 - 2 - 3
     *   Level 2: 4 - 5 (child of node 2)
     *   Level 3: 6 - 7 (child of node 5)
     *
     * BFS Queue processing:
     *   Initial queue: [head of (1-2-3)]
     *   Round 1 (depth 1): Process 1-2-3 → add [1,2,3] to result
     *                       Node 2 has child → enqueue head of (4-5)
     *                       Next queue: [head of (4-5)]
     *   Round 2 (depth 2): Process 4-5 → add [4,5] to result
     *                       Node 5 has child → enqueue head of (6-7)
     *                       Next queue: [head of (6-7)]
     *   Round 3 (depth 3): Process 6-7 → add [6,7] to result
     *                       No children found
     *                       Next queue: []
     *   Final order: [1,2,3,4,5,6,7] → "1 -> 2 -> 3 -> 4 -> 5 -> 6 -> 7" ✓
     *
     * <p>Example 2 Trace:
     * Input structure:
     *   Level 1: 10 - 20 - 30
     *   Level 2: 11 - 12 (child of node 10), 21 - 22 (child of node 20)
     *   Level 3: 100 (child of node 12)
     *
     * BFS Queue processing:
     *   Initial queue: [head of (10-20-30)]
     *   Round 1 (depth 1): Process 10-20-30 → add [10,20,30] to result
     *                       Node 10 has child → enqueue head of (11-12)
     *                       Node 20 has child → enqueue head of (21-22)
     *                       Next queue: [head of (11-12), head of (21-22)]
     *   Round 2 (depth 2): Process 11-12 → add [11,12] to result
     *                       Node 12 has child → enqueue head of (100)
     *                       Process 21-22 → add [21,22] to result
     *                       No children from 21-22
     *                       Next queue: [head of (100)]
     *   Round 3 (depth 3): Process 100 → add [100] to result
     *                       No children
     *                       Next queue: []
     *   Final order: [10,20,30,11,12,21,22,100] → "10 -> 20 -> 30 -> 11 -> 12 -> 21 -> 22 -> 100" ✓
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // =====================================================================
        // Example 1:
        // Level 1: 1 - 2 - 3
        // Level 2: 4 - 5 (child of node 2)
        // Level 3: 6 - 7 (child of node 5)
        // Expected Output: 1 -> 2 -> 3 -> 4 -> 5 -> 6 -> 7
        // =====================================================================
        System.out.println("=== Example 1 ===");
        System.out.println("Structure:");
        System.out.println("  Level 1: 1 - 2 - 3");
        System.out.println("  Level 2: 4 - 5  (child of node 2)");
        System.out.println("  Level 3: 6 - 7  (child of node 5)");

        // Build level 1: 1 -> 2 -> 3
        Node node1 = new Node(1);
        Node node2 = new Node(2);
        Node node3 = new Node(3);
        node1.next = node2;
        node2.next = node3;

        // Build level 2: 4 -> 5 (attached as child of node 2)
        Node node4 = new Node(4);
        Node node5 = new Node(5);
        node4.next = node5;
        node2.child = node4;  // node 2's child points to 4

        // Build level 3: 6 -> 7 (attached as child of node 5)
        Node node6 = new Node(6);
        Node node7 = new Node(7);
        node6.next = node7;
        node5.child = node6;  // node 5's child points to 6

        // Flatten and print
        Node result1 = solution.flatten(node1);
        System.out.println("Output:    " + listToString(result1));
        System.out.println("Expected:  1 -> 2 -> 3 -> 4 -> 5 -> 6 -> 7");
        System.out.println();

        // =====================================================================
        // Example 2:
        // Level 1: 10 - 20 - 30
        // Level 2: 11 - 12 (child of node 10), 21 - 22 (child of node 20)
        // Level 3: 100 (child of node 12)
        // Expected Output: 10 -> 20 -> 30 -> 11 -> 12 -> 21 -> 22 -> 100
        // =====================================================================
        System.out.println("=== Example 2 ===");
        System.out.println("Structure:");
        System.out.println("  Level 1: 10 - 20 - 30");
        System.out.println("  Level 2: 11 - 12 (child of 10), 21 - 22 (child of 20)");
        System.out.println("  Level 3: 100 (child of 12)");

        // Build level 1: 10 -> 20 -> 30
        Node n10 = new Node(10);
        Node n20 = new Node(20);
        Node n30 = new Node(30);
        n10.next = n20;
        n20.next = n30;

        // Build level 2 sub-list 1: 11 -> 12 (child of node 10)
        Node n11 = new Node(11);
        Node n12 = new Node(12);
        n11.next = n12;
        n10.