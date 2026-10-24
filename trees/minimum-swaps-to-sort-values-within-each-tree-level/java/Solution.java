import java.util.*;

/*
 * Title: Minimum Swaps to Sort Values Within Each Tree Level
 * Difficulty: Medium
 * Topic: Trees
 *
 * Problem Description:
 * You are given the root of a binary tree where each node contains an integer value.
 * In one operation, you may choose any two nodes that are on the same depth level and
 * swap their values. The structure of the tree cannot be changed; only the values stored
 * in nodes may be swapped.
 *
 * Your task is to return the minimum total number of such swaps required so that, for every
 * depth level independently, the values of the nodes on that level appear in strictly
 * increasing order from left to right.
 *
 * If a level already appears in strictly increasing order, it contributes 0 to the answer.
 * Each level is processed independently, but the final answer is the sum of the minimum swaps
 * needed across all levels. You may assume that for every level, all values are distinct,
 * so a strictly increasing order is well-defined.
 *
 * A common approach is to perform a breadth-first traversal to collect values level by level,
 * then compute the minimum number of swaps needed to sort each level. This turns the tree
 * problem into a repeated array-reordering problem.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [1, 100000].
 * - -1000000000 <= Node.val <= 1000000000
 * - All node values on the same level are distinct.
 * - The tree is a binary tree.
 *
 * Note about Example 1:
 * The written example text contains an inconsistency between the shown array and the shown output.
 * To match the stated output of 1, we use the intended level-order input:
 * [5,3,4,7,6,8,9]
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
     * Computes the minimum total number of swaps required so that, for every tree level,
     * the values appear in strictly increasing order from left to right.
     *
     * The algorithm works in two major phases:
     * 1. Perform a breadth-first traversal (level-order traversal) of the tree.
     * 2. For each level, compute the minimum number of swaps needed to sort that level's values.
     *
     * Because swaps are only allowed within the same level, each level can be solved independently,
     * and the final answer is the sum across all levels.
     *
     * @param root the root of the binary tree
     * @return the minimum total number of swaps needed across all levels
     *
     * Time complexity: O(n log n), where n is the number of nodes in the tree.
     * Each node is visited once in BFS, and sorting across all levels totals O(n log n).
     *
     * Space complexity: O(n), due to the BFS queue and temporary arrays used for levels.
     */
    public int minimumOperations(TreeNode root) {
        // Defensive handling: if the tree is empty, there are no levels to sort.
        if (root == null) {
            return 0;
        }

        // We use a queue for standard breadth-first traversal.
        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.offer(root);

        int totalSwaps = 0;

        // Process the tree one level at a time.
        while (!queue.isEmpty()) {
            // The current queue size tells us exactly how many nodes belong to this level.
            int levelSize = queue.size();

            // Collect all values from the current level into an array.
            int[] levelValues = new int[levelSize];

            // Read exactly 'levelSize' nodes so that we isolate one level cleanly.
            for (int i = 0; i < levelSize; i++) {
                TreeNode current = queue.poll();
                levelValues[i] = current.val;

                // Add children for the next level.
                if (current.left != null) {
                    queue.offer(current.left);
                }
                if (current.right != null) {
                    queue.offer(current.right);
                }
            }

            // Compute how many swaps are needed to sort this level.
            totalSwaps += minSwapsToSort(levelValues);
        }

        return totalSwaps;
    }

    /**
     * Computes the minimum number of swaps needed to sort an array of distinct integers
     * into strictly increasing order.
     *
     * Core idea:
     * - If we know where each element should go in the sorted array, then the array defines
     *   a permutation of indices.
     * - The minimum number of swaps needed to fix a permutation is the sum over all cycles of:
     *       (cycle length - 1)
     *
     * Detailed process:
     * 1. Create pairs of (value, originalIndex).
     * 2. Sort those pairs by value.
     * 3. After sorting, the element now at sorted position i originally came from some index j.
     * 4. This gives a mapping between positions that forms cycles.
     * 5. Count the swaps required from those cycles.
     *
     * Example:
     *   arr = [7, 6, 8, 9]
     *   sorted = [6, 7, 8, 9]
     *   positions form one cycle involving indices 0 and 1, so answer = 1.
     *
     * @param values the array of distinct values from one tree level
     * @return the minimum number of swaps needed to sort the array
     *
     * Time complexity: O(m log m), where m is the number of elements in this level,
     * due to sorting.
     *
     * Space complexity: O(m), for the pair array and visited array.
     */
    public int minSwapsToSort(int[] values) {
        int n = values.length;

        // Arrays of size 0 or 1 are already sorted.
        if (n <= 1) {
            return 0;
        }

        // Each entry stores:
        // pair[i][0] = value
        // pair[i][1] = original index
        int[][] pairs = new int[n][2];
        for (int i = 0; i < n; i++) {
            pairs[i][0] = values[i];
            pairs[i][1] = i;
        }

        // Sort by value so we know the target order.
        Arrays.sort(pairs, Comparator.comparingInt(a -> a[0]));

        // visited[i] tells us whether sorted position i has already been processed in a cycle.
        boolean[] visited = new boolean[n];
        int swaps = 0;

        // We inspect each sorted position.
        for (int i = 0; i < n; i++) {
            // If already visited, it belongs to a cycle we already counted.
            if (visited[i]) {
                continue;
            }

            // If the element currently at sorted position i originally came from index i,
            // then it is already effectively in the correct place and forms a cycle of length 1.
            if (pairs[i][1] == i) {
                visited[i] = true;
                continue;
            }

            // Otherwise, follow the cycle.
            int cycleLength = 0;
            int currentIndex = i;

            while (!visited[currentIndex]) {
                visited[currentIndex] = true;

                // Move to the original index of the element currently at this sorted position.
                currentIndex = pairs[currentIndex][1];
                cycleLength++;
            }

            // A cycle of length L needs exactly L - 1 swaps.
            if (cycleLength > 1) {
                swaps += cycleLength - 1;
            }
        }

        return swaps;
    }

    /**
     * Builds a binary tree from a level-order array representation.
     *
     * This helper is used only for demonstration in main.
     * Null entries represent missing children.
     *
     * Example:
     *   [1, 2, 3, null, 4]
     *
     * means:
     *       1
     *      / \
     *     2   3
     *      \
     *       4
     *
     * @param values level-order representation of the tree, where null means no node
     * @return the root of the constructed binary tree
     *
     * Time complexity: O(n), where n is the number of entries in the input array.
     * Space complexity: O(n), due to the queue used during construction.
     */
    public static TreeNode buildTree(Integer[] values) {
        if (values == null || values.length == 0 || values[0] == null) {
            return null;
        }

        TreeNode root = new TreeNode(values[0]);
        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.offer(root);

        int index = 1;

        while (!queue.isEmpty() && index < values.length) {
            TreeNode current = queue.poll();

            if (index < values.length && values[index] != null) {
                current.left = new TreeNode(values[index]);
                queue.offer(current.left);
            }
            index++;

            if (index < values.length && values[index] != null) {
                current.right = new TreeNode(values[index]);
                queue.offer(current.right);
            }
            index++;
        }

        return root;
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // The problem statement contains a mismatch between the shown input and shown output.
        // To match the stated output of 1, we use the intended tree:
        // root = [5,3,4,7,6,8,9]
        //
        // Levels:
        // [5] -> already sorted
        // [3,4] -> already sorted
        // [7,6,8,9] -> one swap needed (7 and 6)
        // Total = 1
        TreeNode example1 = buildTree(new Integer[]{5, 3, 4, 7, 6, 8, 9});
        System.out.println(solution.minimumOperations(example1)); // Expected: 1

        // Example 2:
        // root = [1,7,5,4,6,8,9]
        //
        // Levels:
        // [1] -> 0
        // [7,5] -> 1 swap
        // [4,6,8,9] -> 0
        // Total = 1
        TreeNode example2 = buildTree(new Integer[]{1, 7, 5, 4, 6, 8, 9});
        System.out.println(solution.minimumOperations(example2)); // Expected: 1

        // Additional quick sanity check:
        // Single node tree always needs 0 swaps.
        TreeNode example3 = buildTree(new Integer[]{42});
        System.out.println(solution.minimumOperations(example3)); // Expected: 0
    }
}