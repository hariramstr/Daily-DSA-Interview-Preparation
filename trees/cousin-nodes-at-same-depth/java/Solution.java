/*
 * Problem: Cousin Nodes at Same Depth
 * Difficulty: Easy
 * Topic: Trees
 *
 * Problem Description:
 * Given the root of a binary tree and two node values x and y, determine whether
 * the nodes with these values are cousins. Two nodes are considered cousins if they
 * are at the same depth in the tree but have different parent nodes.
 *
 * You may assume that both values x and y exist exactly once in the tree, and the
 * tree contains at least 2 nodes.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [2, 100].
 * - Node values are unique integers in the range [1, 100].
 * - Both x and y exist in the tree.
 *
 * Example 1:
 * Input: root = [1, 2, 3, 4, 5, null, null], x = 4, y = 5
 * Output: false
 * Explanation: Nodes 4 and 5 are both at depth 2, but they share the same parent
 * (node 2). So they are siblings, not cousins.
 *
 * Example 2:
 * Input: root = [1, 2, 3, null, 4, null, 5], x = 4, y = 5
 * Output: true
 * Explanation: Node 4 is a child of node 2 (depth 2), and node 5 is a child of
 * node 3 (depth 2). They are at the same depth and have different parents, so
 * they are cousins.
 */

import java.util.LinkedList;
import java.util.Queue;

/**
 * Solution class for the "Cousin Nodes at Same Depth" problem.
 * Uses BFS (Breadth-First Search) to find depth and parent of each target node.
 */
public class Solution {

    /**
     * Definition for a binary tree node.
     * This is a standard TreeNode structure used in binary tree problems.
     */
    static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode(int val) {
            this.val = val;
            this.left = null;
            this.right = null;
        }
    }

    /**
     * Determines whether nodes with values x and y are cousins in the binary tree.
     *
     * <p>Two nodes are cousins if:
     * 1. They are at the same depth (level) in the tree.
     * 2. They have different parent nodes.
     *
     * <p>Approach: BFS (Level-Order Traversal)
     * We traverse the tree level by level. For each node, we check if its children
     * are x or y, and record their depth and parent. At the end, we check if both
     * nodes have the same depth but different parents.
     *
     * @param root the root of the binary tree
     * @param x    the value of the first node
     * @param y    the value of the second node
     * @return true if nodes x and y are cousins, false otherwise
     *
     * Time Complexity: O(N) where N is the number of nodes in the tree,
     *                  since we visit each node at most once.
     * Space Complexity: O(N) for the BFS queue, which can hold at most N/2 nodes
     *                   at the widest level of the tree.
     */
    public boolean isCousins(TreeNode root, int x, int y) {
        // Step 1: Initialize variables to store depth and parent of x and y.
        // We use -1 as a sentinel value to indicate "not found yet".
        int depthX = -1, depthY = -1;
        TreeNode parentX = null, parentY = null;

        // Step 2: Set up BFS using a queue.
        // We'll store pairs of (node, parent) so we can track each node's parent.
        // Using a Queue of Object arrays: [node, parentNode, depth]
        Queue<Object[]> queue = new LinkedList<>();

        // Step 3: Add the root to the queue.
        // The root has no parent (null) and is at depth 0.
        queue.offer(new Object[]{root, null, 0});

        // Step 4: Process nodes level by level using BFS.
        while (!queue.isEmpty()) {
            // Dequeue the front element
            Object[] current = queue.poll();
            TreeNode node = (TreeNode) current[0];
            TreeNode parent = (TreeNode) current[1];
            int depth = (int) current[2];

            // Step 5: Check if the current node is x or y.
            // If it is, record its depth and parent.
            if (node.val == x) {
                depthX = depth;
                parentX = parent;
            }
            if (node.val == y) {
                depthY = depth;
                parentY = parent;
            }

            // Step 6: Early termination optimization.
            // If we've found both x and y, we can stop BFS early.
            if (depthX != -1 && depthY != -1) {
                break;
            }

            // Step 7: Enqueue left child if it exists.
            // The left child's parent is the current node, and its depth is depth + 1.
            if (node.left != null) {
                queue.offer(new Object[]{node.left, node, depth + 1});
            }

            // Step 8: Enqueue right child if it exists.
            // The right child's parent is the current node, and its depth is depth + 1.
            if (node.right != null) {
                queue.offer(new Object[]{node.right, node, depth + 1});
            }
        }

        // Step 9: Determine if x and y are cousins.
        // Cousins must:
        //   (a) Be at the same depth: depthX == depthY
        //   (b) Have different parents: parentX != parentY
        // Note: We compare parentX and parentY by reference (object identity),
        // which is correct since each TreeNode is a unique object.
        return depthX == depthY && parentX != parentY;
    }

    /**
     * Helper method to build a binary tree from a level-order array representation.
     * 'null' values in the array represent missing nodes.
     *
     * @param values an Integer array representing the tree in level-order
     *               (null means no node at that position)
     * @return the root TreeNode of the constructed binary tree
     *
     * Time Complexity: O(N) where N is the length of the values array.
     * Space Complexity: O(N) for the queue used during construction.
     */
    public static TreeNode buildTree(Integer[] values) {
        // If the array is empty or the root value is null, return null
        if (values == null || values.length == 0 || values[0] == null) {
            return null;
        }

        // Create the root node from the first element
        TreeNode root = new TreeNode(values[0]);

        // Use a queue to keep track of nodes that need children assigned
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        // Index to track our position in the values array
        int i = 1;

        // Process each node in the queue and assign its children
        while (!queue.isEmpty() && i < values.length) {
            TreeNode current = queue.poll();

            // Assign left child if the next value exists and is not null
            if (i < values.length) {
                if (values[i] != null) {
                    current.left = new TreeNode(values[i]);
                    queue.offer(current.left);
                }
                i++; // Move to the next value
            }

            // Assign right child if the next value exists and is not null
            if (i < values.length) {
                if (values[i] != null) {
                    current.right = new TreeNode(values[i]);
                    queue.offer(current.right);
                }
                i++; // Move to the next value
            }
        }

        return root;
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     * Traces through each example from the problem description.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // ---------------------------------------------------------------
        // Example 1:
        // Tree structure:
        //         1
        //        / \
        //       2   3
        //      / \
        //     4   5
        //
        // x = 4, y = 5
        // Node 4: depth=2, parent=2
        // Node 5: depth=2, parent=2
        // Same depth (2 == 2) but SAME parent (both have parent 2)
        // Expected Output: false (they are siblings, not cousins)
        // ---------------------------------------------------------------
        System.out.println("=== Example 1 ===");
        Integer[] values1 = {1, 2, 3, 4, 5, null, null};
        TreeNode root1 = buildTree(values1);
        int x1 = 4, y1 = 5;
        boolean result1 = solution.isCousins(root1, x1, y1);
        System.out.println("Tree: [1, 2, 3, 4, 5, null, null]");
        System.out.println("x = " + x1 + ", y = " + y1);
        System.out.println("Are they cousins? " + result1);
        System.out.println("Expected: false");
        System.out.println("Test " + (result1 == false ? "PASSED ✓" : "FAILED ✗"));
        System.out.println();

        // ---------------------------------------------------------------
        // Example 2:
        // Tree structure:
        //         1
        //        / \
        //       2   3
        //        \    \
        //         4    5
        //
        // x = 4, y = 5
        // Node 4: depth=2, parent=2
        // Node 5: depth=2, parent=3
        // Same depth (2 == 2) and DIFFERENT parents (2 != 3)
        // Expected Output: true (they are cousins)
        // ---------------------------------------------------------------
        System.out.println("=== Example 2 ===");
        Integer[] values2 = {1, 2, 3, null, 4, null, 5};
        TreeNode root2 = buildTree(values2);
        int x2 = 4, y2 = 5;
        boolean result2 = solution.isCousins(root2, x2, y2);
        System.out.println("Tree: [1, 2, 3, null, 4, null, 5]");
        System.out.println("x = " + x2 + ", y = " + y2);
        System.out.println("Are they cousins? " + result2);
        System.out.println("Expected: true");
        System.out.println("Test " + (result2 == true ? "PASSED ✓" : "FAILED ✗"));
        System.out.println();

        // ---------------------------------------------------------------
        // Additional Example 3:
        // Tree structure:
        //         1
        //        / \
        //       2   3
        //      /
        //     4
        //
        // x = 2, y = 3
        // Node 2: depth=1, parent=1
        // Node 3: depth=1, parent=1
        // Same depth (1 == 1) but SAME parent (both have parent 1)
        // Expected Output: false (they are siblings, not cousins)
        // ---------------------------------------------------------------
        System.out.println("=== Additional Example 3 ===");
        Integer[] values3 = {1, 2, 3, 4, null, null, null};
        TreeNode root3 = buildTree(values3);
        int x3 = 2, y3 = 3;
        boolean result3 = solution.isCousins(root3, x3, y3);
        System.out.println("Tree: [1, 2, 3, 4, null, null, null]");
        System.out.println("x = " + x3 + ", y = " + y3);
        System.out.println("Are they cousins? " + result3);
        System.out.println("Expected: false");
        System.out.println("Test " + (result3 == false ? "PASSED ✓" : "FAILED ✗"));
        System.out.println();

        // ---------------------------------------------------------------
        // Additional Example 4:
        // Tree structure:
        //         1
        //        / \
        //       2   3
        //      /     \
        //     4       5
        //    /
        //   6
        //
        // x = 6, y = 5
        // Node 6: depth=3, parent=4
        // Node 5: depth=2, parent=3
        // Different depths (3 != 2)
        // Expected Output: false (different depths)
        // ---------------------------------------------------------------
        System.out.println("=== Additional Example 4 ===");
        // Build tree manually for this case
        TreeNode root4 = new TreeNode(1);
        root4.left = new TreeNode(2);
        root4.right = new TreeNode(3);
        root4.left.left = new TreeNode(4);
        root4.right.right = new TreeNode(5);
        root4.left.left.left = new TreeNode(6);
        int x4 = 6, y4 = 5;
        boolean result4 = solution.isCousins(root4, x4, y4);
        System.out.println("Tree: 1->left=2, 1->right=3, 2->left=4, 3->right=5, 4->left=6");
        System.out.println("x = " + x4 + ", y = " + y4);
        System.out.println("Are they cousins? " + result4);
        System.out.println("Expected: false");
        System.out.println("Test " + (result4 == false ? "PASSED ✓" : "FAILED ✗"));
        System.out.println();

        // ---------------------------------------------------------------
        // Additional Example 5:
        // Tree structure:
        //         1
        //        / \
        //       2   3
        //      /     \
        //     4       5
        //    /         \
        //   6           7
        //
        // x = 6, y = 7
        // Node 6: depth=3, parent=4
        // Node 7: depth=3, parent=5
        // Same depth (3 == 3) and DIFFERENT parents (4 != 5)
        // Expected Output: true (they are cousins)
        // ---------------------------------------------------------------
        System.out.println("=== Additional Example 5 ===");
        TreeNode root5 = new TreeNode(1);
        root5.left = new TreeNode(2);
        root5.right = new TreeNode(3);
        root5.left.left = new TreeNode(4);
        root5.right.right = new TreeNode(5);
        root5.left.left.left = new TreeNode(6);
        root5.right.right.right = new TreeNode(7);
        int x5 = 6, y5 = 7;
        boolean result5 = solution.isCousins(root5, x5, y5);
        System.out.println("Tree: deep tree with nodes 6 and 7 at depth 3, different parents");
        System.out.println("x = " + x5 + ", y = " + y5);
        System.out.println("Are they cousins? " + result5);
        System.out.println("Expected: true");
        System.out.println("Test " + (result5 == true ? "PASSED ✓" : "FAILED ✗"));
    }
}