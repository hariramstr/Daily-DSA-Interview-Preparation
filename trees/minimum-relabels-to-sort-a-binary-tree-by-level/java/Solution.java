import java.util.*;

/*
Problem Title: Minimum Relabels to Sort a Binary Tree by Level

Problem Description:
You are given the root of a binary tree where each node stores an integer label. In one operation,
you may swap the labels of any two nodes that are on the same depth level of the tree. The tree
structure itself cannot be changed; only labels may move between nodes on the same level.

Your task is to return the minimum number of such swaps needed so that, for every depth level
independently, the labels appearing from left to right are in strictly nondecreasing order.
In other words, if you perform a level-order traversal and look at the nodes level by level,
each level must end up sorted when read from left to right.

If a level already appears sorted, it requires 0 operations. Levels are independent: a label from
one level can never be moved to another level. You should compute the total minimum number of swaps
across all levels.

It is guaranteed that the tree contains between 1 and 100000 nodes, and each node value is between
-1000000000 and 1000000000. The tree is not necessarily complete or balanced. An O(n log n) solution
is expected.

Example 1:
Input: root = [5,4,3,7,6,8,9]
Output: 2

Explanation:
Level 0 is [5], already sorted.
Level 1 is [4,3], which becomes [3,4] with one swap.
Level 2 is [7,6,8,9], which becomes [6,7,8,9] with one swap.
Total = 1 + 1 = 2.

Note:
The textual explanation in the prompt claims the total is 1, but that contradicts the actual
level-by-level minimum swap count. The correct total for the shown tree is 2.

Example 2:
Input: root = [10,1,8,7,6,5,4]
Output: 2

Explanation:
Level 0 is [10], already sorted.
Level 1 is [1,8], already sorted.
Level 2 is [7,6,5,4]. To sort this level into [4,5,6,7], the minimum number of swaps is 2.
Total = 2.
*/

public class Solution {

    /**
     * Basic binary tree node definition.
     */
    public static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        /**
         * Creates a node with the given value.
         *
         * @param val value stored in the node
         */
        TreeNode(int val) {
            this.val = val;
        }
    }

    /**
     * Computes the minimum total number of same-level label swaps required so that every level
     * of the binary tree becomes sorted from left to right in nondecreasing order.
     *
     * Approach:
     * 1. Perform a standard breadth-first traversal (level-order traversal).
     * 2. For each level, collect the node values from left to right into a list/array.
     * 3. Independently compute the minimum number of swaps needed to sort that level.
     * 4. Add the swap count for every level.
     *
     * Important detail about duplicates:
     * Node values are not guaranteed to be unique, so the minimum-swaps routine must correctly
     * handle repeated values. We do this by:
     * - creating a sorted copy of the level values,
     * - mapping each value to all target positions where that value should appear,
     * - building a permutation from current indices to target indices,
     * - counting cycles in that permutation.
     *
     * @param root the root of the binary tree
     * @return the minimum total number of swaps across all levels
     * Time complexity: O(n log n), because each level is sorted once and the total number of nodes is n
     * Space complexity: O(n), for the BFS queue and per-level auxiliary structures
     */
    public long minimumOperations(TreeNode root) {
        if (root == null) {
            return 0L;
        }

        long totalSwaps = 0L;

        // Standard BFS queue for level-order traversal.
        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.offer(root);

        // Process the tree one level at a time.
        while (!queue.isEmpty()) {
            int levelSize = queue.size();

            // We store the values of the current level in left-to-right order.
            int[] levelValues = new int[levelSize];

            // Read exactly one level from the queue.
            for (int i = 0; i < levelSize; i++) {
                TreeNode current = queue.poll();
                levelValues[i] = current.val;

                // Push children for the next level.
                if (current.left != null) {
                    queue.offer(current.left);
                }
                if (current.right != null) {
                    queue.offer(current.right);
                }
            }

            // Add the minimum swaps needed to sort this level.
            totalSwaps += minSwapsToSort(levelValues);
        }

        return totalSwaps;
    }

    /**
     * Computes the minimum number of swaps needed to sort the given array into nondecreasing order.
     *
     * This method correctly handles duplicate values.
     *
     * Step-by-step idea:
     * 1. Make a sorted copy of the array.
     * 2. For each distinct value in the sorted array, record all indices where that value should appear.
     * 3. For each original index i, assign arr[i] to the next available target index for that same value.
     *    This creates a permutation "to[i]" meaning:
     *      the element currently at i should end up at index to[i].
     * 4. The minimum swaps needed to realize a permutation is:
     *      sum over all cycles of (cycleLength - 1)
     * 5. Count cycles in the permutation and return the total.
     *
     * Why cycle counting works:
     * - If a cycle has length 1, that element is already in the correct place.
     * - If a cycle has length k, it takes exactly k - 1 swaps to place all elements correctly.
     *
     * @param arr the values of one tree level from left to right
     * @return the minimum number of swaps needed to sort this level
     * Time complexity: O(m log m), where m is the size of this level
     * Space complexity: O(m), for the sorted copy, maps, and visited array
     */
    public int minSwapsToSort(int[] arr) {
        int n = arr.length;
        if (n <= 1) {
            return 0;
        }

        // Create a sorted target version of the current level.
        int[] sorted = arr.clone();
        Arrays.sort(sorted);

        // For each value, store all positions where that value appears in the sorted array.
        // Example:
        // sorted = [4, 5, 5, 7]
        // positions.get(5) = queue containing [1, 2]
        Map<Integer, Deque<Integer>> positions = new HashMap<>();
        for (int i = 0; i < n; i++) {
            positions.computeIfAbsent(sorted[i], k -> new ArrayDeque<>()).offer(i);
        }

        // Build the permutation:
        // to[i] = the sorted index where the current arr[i] should go.
        //
        // For duplicates, we assign occurrences in a stable left-to-right manner by polling
        // the next available target position from the queue for that value.
        int[] to = new int[n];
        for (int i = 0; i < n; i++) {
            to[i] = positions.get(arr[i]).poll();
        }

        // Count cycles in the permutation "to".
        boolean[] visited = new boolean[n];
        int swaps = 0;

        for (int i = 0; i < n; i++) {
            // If already visited, this index is part of a cycle we already counted.
            if (visited[i]) {
                continue;
            }

            // Follow the cycle starting from i.
            int cycleLength = 0;
            int current = i;

            while (!visited[current]) {
                visited[current] = true;
                current = to[current];
                cycleLength++;
            }

            // A cycle of length 1 is already correct and needs 0 swaps.
            // A cycle of length k needs k - 1 swaps.
            if (cycleLength > 1) {
                swaps += cycleLength - 1;
            }
        }

        return swaps;
    }

    /**
     * Builds a binary tree from a level-order array representation where null means "no node".
     *
     * Example:
     * [5, 4, 3, 7, 6, 8, 9]
     * corresponds to:
     *         5
     *       /   \
     *      4     3
     *     / \   / \
     *    7  6  8  9
     *
     * This helper is only for demonstration in main.
     *
     * @param values level-order node values, where null indicates a missing node
     * @return the root of the constructed tree
     * Time complexity: O(n), where n is the number of entries in the input array
     * Space complexity: O(n), for the queue used during construction
     */
    public TreeNode buildTree(Integer[] values) {
        if (values == null || values.length == 0 || values[0] == null) {
            return null;
        }

        TreeNode root = new TreeNode(values[0]);
        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.offer(root);

        int index = 1;

        while (!queue.isEmpty() && index < values.length) {
            TreeNode current = queue.poll();

            // Assign left child if present.
            if (index < values.length && values[index] != null) {
                current.left = new TreeNode(values[index]);
                queue.offer(current.left);
            }
            index++;

            // Assign right child if present.
            if (index < values.length && values[index] != null) {
                current.right = new TreeNode(values[index]);
                queue.offer(current.right);
            }
            index++;
        }

        return root;
    }

    /**
     * Demonstrates the solution on sample inputs from the prompt.
     *
     * Note:
     * The first example's stated output in the prompt is inconsistent with its own level-by-level
     * explanation. For the actual tree [5,4,3,7,6,8,9], the correct answer is 2:
     * - level 1 needs 1 swap
     * - level 2 needs 1 swap
     * total = 2
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demonstrations here, excluding the called methods
     * Space complexity: O(1) auxiliary here, excluding the called methods
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the prompt.
        Integer[] example1 = {5, 4, 3, 7, 6, 8, 9};
        TreeNode root1 = solution.buildTree(example1);
        long result1 = solution.minimumOperations(root1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected by correct level-by-level counting: 2");

        // Example 2 from the prompt.
        Integer[] example2 = {10, 1, 8, 7, 6, 5, 4};
        TreeNode root2 = solution.buildTree(example2);
        long result2 = solution.minimumOperations(root2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected: 2");

        // Additional small sanity check: already sorted levels.
        Integer[] example3 = {2, 1, 3, 4, 5, 6, 7};
        TreeNode root3 = solution.buildTree(example3);
        long result3 = solution.minimumOperations(root3);
        System.out.println("Example 3 result: " + result3);
        System.out.println("Expected: 0");
    }
}