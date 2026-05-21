```java
/*
 * Title: Merge K Sorted Linked Lists with Weighted Priority
 * Difficulty: Hard
 * Topic: Linked Lists
 *
 * Problem Description:
 * You are given k sorted singly linked lists and an integer array weights of length k,
 * where weights[i] represents the priority weight of the i-th linked list. Your task is
 * to merge all k linked lists into a single sorted linked list, but with a twist: when
 * two nodes from different lists have equal values, the node from the list with the
 * higher weight should appear first. If two equal-valued nodes come from lists with equal
 * weights, the node from the list with the smaller index should appear first.
 *
 * Additionally, after merging, you must remove every node whose value appears more than
 * threshold times in the final merged list, where threshold is a given integer.
 *
 * Return the head of the resulting linked list.
 *
 * Constraints:
 * - 1 <= k <= 10^4
 * - 0 <= weights[i] <= 10^9
 * - Each linked list has at most 500 nodes.
 * - Node values are in the range [-10^5, 10^5]
 * - 1 <= threshold <= 10^4
 *
 * Example 1:
 * Input: lists = [[1,4,7],[2,4,6],[1,3,5]], weights = [3,1,2], threshold = 2
 * Output: [1,1,2,3,4,4,5,6,7]
 *
 * Example 2:
 * Input: lists = [[1,1,3],[1,2,3],[3,4,5]], weights = [2,2,1], threshold = 2
 * Output: [2,4,5]
 */

import java.util.*;

/**
 * Solution class for merging K sorted linked lists with weighted priority
 * and threshold-based filtering.
 */
public class Solution {

    /**
     * Definition for singly-linked list node.
     * Each node holds an integer value and a reference to the next node.
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
     * A helper class to wrap a ListNode along with its list index and weight.
     * This is used in the priority queue to track which list each node came from.
     */
    static class NodeEntry {
        ListNode node;    // The actual linked list node
        int listIndex;    // Which list (0-based) this node belongs to
        int weight;       // The weight of the list this node belongs to

        NodeEntry(ListNode node, int listIndex, int weight) {
            this.node = node;
            this.listIndex = listIndex;
            this.weight = weight;
        }
    }

    /**
     * Merges k sorted linked lists with weighted priority and removes nodes
     * whose values exceed the given threshold count.
     *
     * <p>Algorithm Overview:
     * 1. Use a min-heap (PriorityQueue) with a custom comparator that:
     *    - First compares by node value (ascending)
     *    - Then by weight (descending) for equal values
     *    - Then by list index (ascending) for equal values and equal weights
     * 2. Initialize the heap with the head of each non-null list
     * 3. Poll from the heap, add to result, push next node from same list
     * 4. Count occurrences of each value during merge
     * 5. Filter out nodes whose value count exceeds threshold
     *
     * @param lists     Array of head nodes of k sorted linked lists
     * @param weights   Array of weights corresponding to each linked list
     * @param threshold Maximum allowed occurrences of any value in the result
     * @return Head of the merged and filtered linked list
     *
     * Time Complexity: O(N log k) where N is total number of nodes across all lists
     *                  and k is the number of lists (heap operations)
     * Space Complexity: O(k) for the priority queue + O(N) for the result list
     */
    public ListNode mergeKListsWithWeights(ListNode[] lists, int[] weights, int threshold) {

        // -----------------------------------------------------------------------
        // STEP 1: Handle edge cases
        // -----------------------------------------------------------------------
        if (lists == null || lists.length == 0) {
            return null;
        }

        // -----------------------------------------------------------------------
        // STEP 2: Create a priority queue (min-heap) with custom comparator
        //
        // The comparator defines ordering:
        //   - Primary: node value ascending (smaller values come first)
        //   - Secondary: weight descending (higher weight comes first for equal values)
        //   - Tertiary: list index ascending (smaller index first for equal value & weight)
        // -----------------------------------------------------------------------
        PriorityQueue<NodeEntry> minHeap = new PriorityQueue<>((a, b) -> {
            // First compare by node value (ascending order)
            if (a.node.val != b.node.val) {
                return Integer.compare(a.node.val, b.node.val);
            }
            // If values are equal, compare by weight (descending - higher weight first)
            if (a.weight != b.weight) {
                return Integer.compare(b.weight, a.weight); // Note: b before a for descending
            }
            // If values and weights are equal, compare by list index (ascending)
            return Integer.compare(a.listIndex, b.listIndex);
        });

        // -----------------------------------------------------------------------
        // STEP 3: Initialize the heap with the first node from each non-null list
        //
        // We add the head of each list to the heap. The heap will automatically
        // order them according to our comparator.
        // -----------------------------------------------------------------------
        for (int i = 0; i < lists.length; i++) {
            if (lists[i] != null) {
                // Add the head node of list i with its corresponding weight
                minHeap.offer(new NodeEntry(lists[i], i, weights[i]));
            }
        }

        // -----------------------------------------------------------------------
        // STEP 4: Merge all lists using the heap
        //
        // We'll collect all merged nodes in order into a list first,
        // so we can count occurrences and then filter by threshold.
        // -----------------------------------------------------------------------

        // Use a list to collect all merged nodes in sorted order
        // We store the actual ListNode objects to preserve their values
        List<Integer> mergedValues = new ArrayList<>();

        // Also keep track of the merged nodes in order (we'll rebuild the list later)
        // Actually, let's just collect values and rebuild from scratch after filtering

        // Process the heap until it's empty
        while (!minHeap.isEmpty()) {
            // Poll the entry with the highest priority (smallest value, or tiebreak by weight/index)
            NodeEntry current = minHeap.poll();

            // Record the value of this node
            mergedValues.add(current.node.val);

            // If the current node has a next node in its list, add that to the heap
            if (current.node.next != null) {
                minHeap.offer(new NodeEntry(current.node.next, current.listIndex, current.weight));
            }
        }

        // -----------------------------------------------------------------------
        // STEP 5: Count occurrences of each value in the merged list
        //
        // We use a LinkedHashMap to preserve insertion order (though for counting
        // purposes, a regular HashMap works fine).
        // -----------------------------------------------------------------------
        Map<Integer, Integer> countMap = new LinkedHashMap<>();
        for (int val : mergedValues) {
            // Increment count for this value, defaulting to 0 if not present
            countMap.put(val, countMap.getOrDefault(val, 0) + 1);
        }

        // -----------------------------------------------------------------------
        // STEP 6: Build the result linked list, filtering out values that exceed threshold
        //
        // We iterate through the merged values in order and only include nodes
        // whose total count is <= threshold.
        // -----------------------------------------------------------------------

        // Dummy head node to simplify list construction
        ListNode dummyHead = new ListNode(0);
        ListNode tail = dummyHead; // Pointer to the last node in our result list

        for (int val : mergedValues) {
            // Check if this value's count is within the threshold
            if (countMap.get(val) <= threshold) {
                // Create a new node with this value and append to result
                tail.next = new ListNode(val);
                tail = tail.next; // Move tail pointer forward
            }
            // If count > threshold, we skip this node (it gets filtered out)
        }

        // Return the actual head (skip the dummy node)
        return dummyHead.next;
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Converts an array of integers to a linked list.
     *
     * @param arr Array of integers to convert
     * @return Head of the created linked list, or null if array is empty
     *
     * Time Complexity: O(n) where n is the length of the array
     * Space Complexity: O(n) for the created nodes
     */
    public static ListNode arrayToList(int[] arr) {
        if (arr == null || arr.length == 0) {
            return null;
        }

        // Create dummy head to simplify construction
        ListNode dummy = new ListNode(0);
        ListNode current = dummy;

        for (int val : arr) {
            current.next = new ListNode(val);
            current = current.next;
        }

        return dummy.next;
    }

    /**
     * Converts a linked list to a string representation for easy printing.
     *
     * @param head Head of the linked list to convert
     * @return String representation like "[1, 2, 3, 4]" or "[]" for empty list
     *
     * Time Complexity: O(n) where n is the number of nodes
     * Space Complexity: O(n) for the string builder
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
                sb.append(", ");
            }
            current = current.next;
        }

        sb.append("]");
        return sb.toString();
    }

    // =========================================================================
    // MAIN METHOD - Demonstrates the solution with examples
    // =========================================================================

    /**
     * Main method to demonstrate the solution with the provided examples.
     * Traces through each example and prints the results.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        System.out.println("=".repeat(60));
        System.out.println("Merge K Sorted Linked Lists with Weighted Priority");
        System.out.println("=".repeat(60));

        // -----------------------------------------------------------------------
        // Example 1:
        // Input: lists = [[1,4,7],[2,4,6],[1,3,5]], weights = [3,1,2], threshold = 2
        // Expected Output: [1,1,2,3,4,4,5,6,7]
        //
        // Trace:
        // List 0: 1->4->7 (weight=3)
        // List 1: 2->4->6 (weight=1)
        // List 2: 1->3->5 (weight=2)
        //
        // Initial heap: {(1,idx=0,w=3), (2,idx=1,w=1), (1,idx=2,w=2)}
        //
        // Poll (1,idx=0,w=3) -> value=1, push (4,idx=0,w=3)
        //   Heap: {(1,idx=2,w=2), (2,idx=1,w=1), (4,idx=0,w=3)}
        //   Note: Both 1s have same value, w=3 > w=2, so idx=0 comes first
        //
        // Poll (1,idx=2,w=2) -> value=1, push (3,idx=2,w=2)
        //   Heap: {(2,idx=1,w=1), (3,idx=2,w=2), (4,idx=0,w=3)}
        //
        // Poll (2,idx=1,w=1) -> value=2, push (4,idx=1,w=1)
        //   Heap: {(3,idx=2,w=2), (4,idx=0,w=3), (4,idx=1,w=1)}
        //
        // Poll (3,idx=2,w=2) -> value=3, push (5,idx=2,w=2)
        //   Heap: {(4,idx=0,w=3), (4,idx=1,w=1), (5,idx=2,w=2)}
        //
        // Poll (4,idx=0,w=3) -> value=4, push (7,idx=0,w=3)
        //   Note: Both 4s: w=3 > w=1, so idx=0 comes first
        //   Heap: {(4,idx=1,w=1), (5,idx=2,w=2), (7,idx=0,w=3)}
        //
        // Poll (4,idx=1,w=1) -> value=4, push (6,idx=1,w=1)
        //   Heap: {(5,idx=2,w=2), (6,idx=1,w=1), (7,idx=0,w=3)}
        //
        // Poll (5,idx=2,w=2) -> value=5, no next
        // Poll (6,idx=1,w=1) -> value=6, no next
        // Poll (7,idx=0,w=3) -> value=7, no next
        //
        // Merged: [1,1,2,3,4,4,5,6,7]
        // Counts: {1:2, 2:1, 3:1, 4:2, 5:1, 6:1, 7:1}
        // All counts <= threshold=2, so no filtering needed
        // Result: [1,1,2,3,4,4,5,6,7] ✓
        // -----------------------------------------------------------------------
        System.out.println("\nExample 1:");
        System.out.println("Input: lists = [[1,4,7],[2,4,6],[1,3,5]], weights = [3,1,2], threshold = 2");

        ListNode[] lists1 = {
            arrayToList(new int[]{1, 4, 7}),
            arrayToList(new int[]{2, 4, 6}),
            arrayToList(new int[]{1, 3, 5})
        };
        int[] weights1 = {3, 1, 2};
        int threshold1 = 2;

        ListNode result1 = solution.mergeKListsWithWeights(lists1, weights1, threshold1);
        System.out.println("Output: " + listToString(result1));
        System.out.println("Expected: [1, 1, 2, 3, 4, 4, 5, 6, 7]");

        // -----------------------------------------------------------------------
        // Example 2:
        // Input: lists = [[1,1,3],[1,2,3],[3,4,5]], weights = [2,2,1], threshold = 2
        // Expected Output: [2,4,5]
        //
        // Trace:
        // List 0: 1->1->3 (weight=2)
        // List 1: 1->2->3 (weight=2)
        // List 2: 3->4->5 (weight=1)
        //
        // Initial heap: {(1,idx=0,w=2), (1,idx=1,w=2), (3,idx=2,w=1)}
        //
        // Poll (1,idx=0,w=2) -> value=1 [equal values,