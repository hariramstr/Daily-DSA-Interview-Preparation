```java
/*
 * Title: Reorder Linked List by Frequency Buckets
 * Difficulty: Medium
 * Topic: Linked Lists
 *
 * Problem Description:
 * You are given the head of a singly linked list where each node contains a positive integer value.
 * Your task is to reorder the linked list such that nodes are grouped by the frequency of their
 * values in ascending order of frequency. Within the same frequency group, nodes should appear
 * in the order their value was first encountered in the original list.
 *
 * Specifically:
 * - Count the frequency of each unique value in the linked list.
 * - Reconstruct the linked list by placing nodes with the lowest frequency first,
 *   and nodes with the highest frequency last.
 * - If two values have the same frequency, the value that appeared first in the original list
 *   should come first in the reordered list.
 * - The relative order of nodes sharing the same value must be preserved
 *   (all nodes of the same value are grouped together).
 *
 * Constraints:
 * - The number of nodes in the list is in the range [1, 10^4].
 * - 1 <= Node.val <= 10^5
 * - You must perform the reordering in-place by relinking nodes (do not create new nodes).
 *
 * Example 1:
 * - Input: head = [4, 2, 4, 1, 2, 4]
 * - Output: [1, 2, 2, 4, 4, 4]
 * - Explanation: Value 1 appears 1 time, value 2 appears 2 times, value 4 appears 3 times.
 *   Sorted by ascending frequency: 1 → 2 → 2 → 4 → 4 → 4.
 *
 * Example 2:
 * - Input: head = [3, 1, 3, 2, 1, 3, 2]
 * - Output: [1, 1, 2, 2, 3, 3, 3]
 * - Explanation: Value 2 appears 2 times (first seen at index 3), value 1 appears 2 times
 *   (first seen at index 1), value 3 appears 3 times. Values 1 and 2 share frequency 2,
 *   but 1 was first encountered before 2, so 1 comes before 2.
 *   Output = [1, 1, 2, 2, 3, 3, 3].
 */

import java.util.*;

/**
 * Solution class for reordering a linked list by frequency buckets.
 * Nodes with lower frequency values appear first; ties broken by first-seen order.
 */
public class Solution {

    /**
     * Definition for a singly-linked list node.
     * This is a static inner class so it can be used within Solution.
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
     * Reorders the linked list so that nodes are grouped by ascending frequency of their values.
     * Within the same frequency, values that appeared first in the original list come first.
     * All nodes of the same value are grouped together.
     *
     * Algorithm Overview:
     * 1. Traverse the list to count frequencies and record first-seen order of each value.
     * 2. Collect all unique values and sort them by (frequency ASC, firstSeenIndex ASC).
     * 3. For each value in sorted order, collect all nodes with that value into a sub-list.
     * 4. Chain all sub-lists together to form the reordered list.
     *
     * @param head The head of the original singly linked list.
     * @return The head of the reordered linked list.
     *         Time Complexity: O(N log N) where N is the number of nodes
     *                          (dominated by sorting unique values).
     *         Space Complexity: O(N) for storing nodes grouped by value,
     *                           plus O(U) for frequency/order maps where U = unique values.
     */
    public ListNode reorderByFrequency(ListNode head) {
        // -----------------------------------------------------------------------
        // STEP 1: Handle edge cases — empty list or single node needs no reordering
        // -----------------------------------------------------------------------
        if (head == null || head.next == null) {
            return head;
        }

        // -----------------------------------------------------------------------
        // STEP 2: First pass — count frequencies and record first-seen index
        //
        // We use a LinkedHashMap for frequencyMap to maintain insertion order,
        // though we'll use a separate firstSeenIndex map for tie-breaking.
        //
        // frequencyMap: value -> how many times it appears
        // firstSeenIndex: value -> the index (0-based) at which it first appeared
        // -----------------------------------------------------------------------
        Map<Integer, Integer> frequencyMap = new HashMap<>();
        Map<Integer, Integer> firstSeenIndex = new HashMap<>();

        ListNode current = head;
        int index = 0;

        while (current != null) {
            int val = current.val;

            // Count frequency: increment count for this value
            frequencyMap.put(val, frequencyMap.getOrDefault(val, 0) + 1);

            // Record first-seen index: only store if not already recorded
            if (!firstSeenIndex.containsKey(val)) {
                firstSeenIndex.put(val, index);
            }

            current = current.next;
            index++;
        }

        // -----------------------------------------------------------------------
        // STEP 3: Collect all unique values and sort them
        //
        // Sorting criteria (comparator):
        //   Primary:   ascending frequency (lower freq comes first)
        //   Secondary: ascending first-seen index (earlier appearance comes first)
        //
        // Example 1: values = {4, 2, 1}
        //   freq(4)=3, freq(2)=2, freq(1)=1
        //   sorted order: 1 (freq=1), 2 (freq=2), 4 (freq=3)
        //
        // Example 2: values = {3, 1, 2}
        //   freq(3)=3, freq(1)=2, freq(2)=2
        //   1 and 2 tie on freq=2; firstSeen(1)=1, firstSeen(2)=3
        //   sorted order: 1 (freq=2, idx=1), 2 (freq=2, idx=3), 3 (freq=3)
        // -----------------------------------------------------------------------
        List<Integer> uniqueValues = new ArrayList<>(frequencyMap.keySet());

        uniqueValues.sort((a, b) -> {
            int freqA = frequencyMap.get(a);
            int freqB = frequencyMap.get(b);

            // Primary sort: by frequency ascending
            if (freqA != freqB) {
                return Integer.compare(freqA, freqB);
            }

            // Secondary sort: by first-seen index ascending (tie-breaking)
            return Integer.compare(firstSeenIndex.get(a), firstSeenIndex.get(b));
        });

        // -----------------------------------------------------------------------
        // STEP 4: Group existing nodes by their value
        //
        // We create a map from value -> (head, tail) of the sub-list of nodes
        // with that value. We reuse existing nodes (in-place relinking).
        //
        // nodeGroups: value -> [headNode, tailNode] of that value's sub-chain
        //
        // We traverse the original list once more, appending each node to its
        // corresponding group's sub-list.
        // -----------------------------------------------------------------------
        // Map from value to a 2-element array: [groupHead, groupTail]
        Map<Integer, ListNode[]> nodeGroups = new HashMap<>();

        current = head;
        while (current != null) {
            int val = current.val;

            // Save next pointer before we modify current.next
            ListNode nextNode = current.next;

            // Detach current node from the original chain
            current.next = null;

            if (!nodeGroups.containsKey(val)) {
                // First node with this value: it becomes both head and tail of the group
                nodeGroups.put(val, new ListNode[]{current, current});
            } else {
                // Append current node to the tail of this value's group
                ListNode[] group = nodeGroups.get(val);
                ListNode groupTail = group[1]; // group[1] is the current tail
                groupTail.next = current;      // link old tail -> current node
                group[1] = current;            // update tail to current node
            }

            current = nextNode; // advance to the next original node
        }

        // -----------------------------------------------------------------------
        // STEP 5: Chain all groups together in sorted order
        //
        // We iterate through uniqueValues (already sorted by freq, then firstSeen)
        // and link each group's tail to the next group's head.
        //
        // We use a dummy node to simplify the head assignment.
        // -----------------------------------------------------------------------
        ListNode dummy = new ListNode(0); // sentinel/dummy node
        ListNode tail = dummy;            // 'tail' tracks the end of the result list

        for (int val : uniqueValues) {
            ListNode[] group = nodeGroups.get(val);
            ListNode groupHead = group[0]; // head of this value's sub-list
            ListNode groupTail = group[1]; // tail of this value's sub-list

            // Link the current result tail to this group's head
            tail.next = groupHead;

            // Advance our result tail to this group's tail
            tail = groupTail;
        }

        // Ensure the last node points to null (end of list)
        tail.next = null;

        // The actual head of the reordered list is dummy.next
        return dummy.next;
    }

    // =========================================================================
    // Helper methods for building and printing linked lists (for testing)
    // =========================================================================

    /**
     * Builds a linked list from an integer array.
     *
     * @param values Array of integer values to build the list from.
     * @return The head of the newly created linked list.
     *         Time Complexity: O(N)
     *         Space Complexity: O(N)
     */
    public static ListNode buildList(int[] values) {
        if (values == null || values.length == 0) {
            return null;
        }

        ListNode dummy = new ListNode(0);
        ListNode current = dummy;

        for (int val : values) {
            current.next = new ListNode(val);
            current = current.next;
        }

        return dummy.next;
    }

    /**
     * Converts a linked list to a readable string representation.
     *
     * @param head The head of the linked list.
     * @return A string like "[1, 2, 2, 4, 4, 4]" representing the list.
     *         Time Complexity: O(N)
     *         Space Complexity: O(N)
     */
    public static String listToString(ListNode head) {
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
    // Main method — demonstrates the solution with sample inputs
    // =========================================================================

    /**
     * Entry point for demonstrating the reorderByFrequency solution.
     * Traces through both examples from the problem description and prints results.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        System.out.println("=== Reorder Linked List by Frequency Buckets ===");
        System.out.println();

        // -----------------------------------------------------------------
        // Example 1: [4, 2, 4, 1, 2, 4]
        // Expected Output: [1, 2, 2, 4, 4, 4]
        //
        // Trace:
        //   freq(1) = 1, firstSeen(1) = 3
        //   freq(2) = 2, firstSeen(2) = 1
        //   freq(4) = 3, firstSeen(4) = 0
        //
        //   Sorted unique values by (freq ASC, firstSeen ASC):
        //     1 (freq=1, idx=3), 2 (freq=2, idx=1), 4 (freq=3, idx=0)
        //
        //   Result: [1] -> [2, 2] -> [4, 4, 4] = [1, 2, 2, 4, 4, 4] ✓
        // -----------------------------------------------------------------
        int[] input1 = {4, 2, 4, 1, 2, 4};
        ListNode head1 = buildList(input1);
        System.out.println("Example 1:");
        System.out.println("  Input:    " + listToString(head1));

        ListNode result1 = solution.reorderByFrequency(head1);
        System.out.println("  Output:   " + listToString(result1));
        System.out.println("  Expected: [1, 2, 2, 4, 4, 4]");
        System.out.println();

        // -----------------------------------------------------------------
        // Example 2: [3, 1, 3, 2, 1, 3, 2]
        // Expected Output: [1, 1, 2, 2, 3, 3, 3]
        //
        // Trace:
        //   freq(3) = 3, firstSeen(3) = 0
        //   freq(1) = 2, firstSeen(1) = 1
        //   freq(2) = 2, firstSeen(2) = 3
        //
        //   Sorted unique values by (freq ASC, firstSeen ASC):
        //     1 (freq=2, idx=1), 2 (freq=2, idx=3), 3 (freq=3, idx=0)
        //     (1 and 2 tie on freq=2; 1 was seen at idx=1, 2 at idx=3, so 1 first)
        //
        //   Result: [1, 1] -> [2, 2] -> [3, 3, 3] = [1, 1, 2, 2, 3, 3, 3] ✓
        // -----------------------------------------------------------------
        int[] input2 = {3, 1, 3, 2, 1, 3, 2};
        ListNode head2 = buildList(input2);
        System.out.println("Example 2:");
        System.out.println("  Input:    " + listToString(head2));

        ListNode result2 = solution.reorderByFrequency(head2);
        System.out.println("  Output:   " + listToString(result2));
        System.out.println("  Expected: [1, 1, 2, 2, 3, 3, 3]");
        System.out.println();

        // -----------------------------------------------------------------
        // Additional Example 3: Single element [5]
        // Expected Output: [5]
        // -----------------------------------------------------------------
        int[] input3 = {5};
        ListNode head3 = buildList(input3);
        System.out.println("Example 3 (single element):");
        System.out.println("  Input:    " + listToString(head3));

        ListNode result3 = solution.reorderByFrequency(head3);
        System.out.println("  Output:   " + listToString(result3));
        System.out.println("  Expected: [5]");
        System.out.println();

        // -----------------------------------------------------------------
        // Additional Example 4: All same values [7, 7, 7]
        // Expected Output: [7, 7, 7]
        // -----------------------------------------------------------------
        int[] input4 = {7, 7, 7};
        ListNode