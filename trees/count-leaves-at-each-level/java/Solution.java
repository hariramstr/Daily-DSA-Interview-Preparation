/*
 * Title: Count Leaves at Each Level
 *
 * Problem Description:
 * Given the root of a binary tree, return a list where each element represents
 * the number of leaf nodes at that level of the tree. Levels are 0-indexed,
 * starting from the root at level 0.
 *
 * A leaf node is defined as a node with no left or right children.
 *
 * For example, if the root itself has no children, it is a leaf at level 0,
 * so the result would be [1].
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [1, 1000].
 * - -1000 <= Node.val <= 1000
 * - The tree may be skewed (i.e., all nodes on one side).
 *
 * Example 1:
 *   Input: root = [3, 9, 20, null, null, 15, 7]
 *   Output: [0, 1, 2]
 *
 * Example 2:
 *   Input: root = [1, 2, 3, 4, null, null, null]
 *   Output: [0, 1, 1]
 */

import java.util.*;

/**
 * Solution class for counting leaf nodes at each level of a binary tree.
 *
 * <p>Approach: We use Breadth-First Search (BFS) with a queue to traverse the tree
 * level by level. For each level, we count how many nodes are leaves (no children).
 * This is a classic level-order traversal problem.</p>
 */
public class Solution {

    /**
     * Inner static class representing a node in a binary tree.
     */
    static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        /** Constructs a TreeNode with the given value and no children. */
        TreeNode(int val) {
            this.val = val;
            this.left = null;
            this.right = null;
        }
    }

    /**
     * Counts the number of leaf nodes at each level of the binary tree.
     *
     * <p>Algorithm: Level-order BFS traversal using a Queue.
     * For each level, we process all nodes currently in the queue (which represent
     * exactly one level), count how many are leaves, and enqueue their children
     * for the next level.</p>
     *
     * @param root The root of the binary tree (may be null, but constraints say >= 1 node)
     * @return A List of integers where index i contains the count of leaf nodes at level i
     *
     * Time Complexity:  O(n) — we visit every node exactly once
     * Space Complexity: O(w) — where w is the maximum width of the tree (queue size),
     *                          which in the worst case is O(n) for a complete binary tree
     */
    public List<Integer> countLeavesAtEachLevel(TreeNode root) {
        // This list will hold the leaf count for each level (index = level number)
        List<Integer> result = new ArrayList<>();

        // Edge case: if the tree is empty, return an empty list
        if (root == null) {
            return result;
        }

        // Step 1: Initialize a queue for BFS traversal.
        // A queue processes nodes in FIFO order, which is perfect for level-by-level traversal.
        Queue<TreeNode> queue = new LinkedList<>();

        // Step 2: Start BFS by adding the root node to the queue.
        // The root is at level 0.
        queue.offer(root);

        // Step 3: Process the tree level by level until the queue is empty.
        // Each iteration of this while loop processes ONE complete level.
        while (!queue.isEmpty()) {

            // Step 3a: Determine how many nodes are at the current level.
            // At the start of each iteration, all nodes in the queue belong to the same level.
            int levelSize = queue.size();

            // Step 3b: Initialize a counter for leaf nodes at this level.
            int leafCount = 0;

            // Step 3c: Process every node at the current level.
            for (int i = 0; i < levelSize; i++) {

                // Remove the front node from the queue (dequeue).
                TreeNode currentNode = queue.poll();

                // Step 3d: Check if this node is a leaf.
                // A leaf has NO left child AND NO right child.
                boolean isLeaf = (currentNode.left == null && currentNode.right == null);

                if (isLeaf) {
                    // This node is a leaf — increment the leaf counter for this level.
                    leafCount++;
                } else {
                    // This node is NOT a leaf — enqueue its children for the next level.
                    // Only enqueue non-null children to avoid NullPointerException.

                    if (currentNode.left != null) {
                        // Enqueue the left child; it will be processed in the next level.
                        queue.offer(currentNode.left);
                    }

                    if (currentNode.right != null) {
                        // Enqueue the right child; it will be processed in the next level.
                        queue.offer(currentNode.right);
                    }
                }
            }

            // Step 3e: After processing all nodes at this level,
            // record the leaf count for this level in our result list.
            result.add(leafCount);
        }

        // Step 4: Return the completed list of leaf counts per level.
        return result;
    }

    /**
     * Helper method to build a binary tree from a level-order array representation.
     * 'null' values in the array represent missing nodes.
     *
     * <p>This is used to easily construct test trees from array notation like
     * [3, 9, 20, null, null, 15, 7].</p>
     *
     * @param values An Integer array representing the tree in level-order (null = no node)
     * @return The root TreeNode of the constructed binary tree
     *
     * Time Complexity:  O(n) — processes each element in the array once
     * Space Complexity: O(n) — queue holds up to n/2 nodes at a time
     */
    public TreeNode buildTree(Integer[] values) {
        // If the array is empty or the first element is null, there's no tree.
        if (values == null || values.length == 0 || values[0] == null) {
            return null;
        }

        // Create the root node from the first element.
        TreeNode root = new TreeNode(values[0]);

        // Use a queue to keep track of nodes whose children we need to assign.
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        // Index into the values array; start at 1 (index 0 is the root).
        int i = 1;

        // Process nodes level by level, assigning left and right children.
        while (!queue.isEmpty() && i < values.length) {
            TreeNode current = queue.poll();

            // Assign left child if the next value exists and is not null.
            if (i < values.length) {
                if (values[i] != null) {
                    current.left = new TreeNode(values[i]);
                    queue.offer(current.left); // Enqueue for future child assignment.
                }
                i++; // Move to the next value.
            }

            // Assign right child if the next value exists and is not null.
            if (i < values.length) {
                if (values[i] != null) {
                    current.right = new TreeNode(values[i]);
                    queue.offer(current.right); // Enqueue for future child assignment.
                }
                i++; // Move to the next value.
            }
        }

        return root;
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     * Traces through both examples from the problem description.
     *
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1: root = [3, 9, 20, null, null, 15, 7]
        // Expected Output: [0, 1, 2]
        //
        // Tree structure:
        //         3          <- Level 0: not a leaf (has children) → 0 leaves
        //        / \
        //       9   20       <- Level 1: 9 is a leaf, 20 is not → 1 leaf
        //          /  \
        //        15    7     <- Level 2: 15 and 7 are both leaves → 2 leaves
        // -----------------------------------------------------------------------
        System.out.println("=== Example 1 ===");
        System.out.println("Input: [3, 9, 20, null, null, 15, 7]");

        Integer[] values1 = {3, 9, 20, null, null, 15, 7};
        TreeNode root1 = solution.buildTree(values1);
        List<Integer> result1 = solution.countLeavesAtEachLevel(root1);

        System.out.println("Output: " + result1);
        System.out.println("Expected: [0, 1, 2]");
        System.out.println("Match: " + result1.equals(Arrays.asList(0, 1, 2)));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2: root = [1, 2, 3, 4, null, null, null]
        // Expected Output: [0, 1, 1]
        //
        // Tree structure:
        //         1          <- Level 0: not a leaf → 0 leaves
        //        / \
        //       2   3        <- Level 1: 2 is not a leaf (has child 4), 3 is a leaf → 1 leaf
        //      /
        //     4              <- Level 2: 4 is a leaf → 1 leaf
        // -----------------------------------------------------------------------
        System.out.println("=== Example 2 ===");
        System.out.println("Input: [1, 2, 3, 4, null, null, null]");

        Integer[] values2 = {1, 2, 3, 4, null, null, null};
        TreeNode root2 = solution.buildTree(values2);
        List<Integer> result2 = solution.countLeavesAtEachLevel(root2);

        System.out.println("Output: " + result2);
        System.out.println("Expected: [0, 1, 1]");
        System.out.println("Match: " + result2.equals(Arrays.asList(0, 1, 1)));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 3: Single node tree (root is a leaf)
        // Expected Output: [1]
        // -----------------------------------------------------------------------
        System.out.println("=== Example 3: Single Node ===");
        System.out.println("Input: [42]");

        Integer[] values3 = {42};
        TreeNode root3 = solution.buildTree(values3);
        List<Integer> result3 = solution.countLeavesAtEachLevel(root3);

        System.out.println("Output: " + result3);
        System.out.println("Expected: [1]");
        System.out.println("Match: " + result3.equals(Arrays.asList(1)));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 4: Skewed tree (all nodes on the left side)
        // Input: [1, 2, null, 3, null, 4]
        //
        // Tree structure:
        //   1        <- Level 0: not a leaf → 0 leaves
        //  /
        // 2          <- Level 1: not a leaf → 0 leaves
        //  \
        //   3        <- Level 2: not a leaf → 0 leaves (wait, let's use left-skewed)
        //
        // Let's use a purely left-skewed tree: [1, 2, null, 3]
        //   1        <- Level 0: not a leaf → 0 leaves
        //  /
        // 2          <- Level 1: not a leaf → 0 leaves
        //  /
        // 3          <- Level 2: leaf → 1 leaf
        // Expected: [0, 0, 1]
        // -----------------------------------------------------------------------
        System.out.println("=== Example 4: Left-Skewed Tree ===");
        System.out.println("Input: [1, 2, null, 3]");

        // Build manually for clarity
        TreeNode root4 = new TreeNode(1);
        root4.left = new TreeNode(2);
        root4.left.left = new TreeNode(3);

        List<Integer> result4 = solution.countLeavesAtEachLevel(root4);

        System.out.println("Output: " + result4);
        System.out.println("Expected: [0, 0, 1]");
        System.out.println("Match: " + result4.equals(Arrays.asList(0, 0, 1)));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 5: Perfect binary tree with 3 levels
        // Input: [1, 2, 3, 4, 5, 6, 7]
        //
        // Tree structure:
        //         1          <- Level 0: not a leaf → 0 leaves
        //        / \
        //       2   3        <- Level 1: neither is a leaf → 0 leaves
        //      / \ / \
        //     4  5 6  7      <- Level 2: all 4 are leaves → 4 leaves
        //
        // Expected: [0, 0, 4]
        // -----------------------------------------------------------------------
        System.out.println("=== Example 5: Perfect Binary Tree ===");
        System.out.println("Input: [1, 2, 3, 4, 5, 6, 7]");

        Integer[] values5 = {1, 2, 3, 4, 5, 6, 7};
        TreeNode root5 = solution.buildTree(values5);
        List<Integer> result5 = solution.countLeavesAtEachLevel(root5);

        System.out.println("Output: " + result5);
        System.out.println("Expected: [0, 0, 4]");
        System.out.println("Match: " + result5.equals(Arrays.asList(0, 0, 4)));
    }
}