/*
 * Title: Sum of Left Boundary Nodes in Binary Tree
 *
 * Problem Description:
 * Given the root of a binary tree, return the sum of all node values that lie
 * on the left boundary of the tree.
 *
 * The left boundary is defined as the path from the root down to the leftmost
 * leaf node, following only left children when available, and right children
 * only when no left child exists. The root itself is always included, but leaf
 * nodes should NOT be included in the sum (only the non-leaf nodes along the path).
 *
 * A leaf node is a node with no children.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [1, 100].
 * - -100 <= Node.val <= 100
 *
 * Example 1:
 * Input: root = [1, 2, 3, 4, 5, null, null, 7]
 * Tree structure:
 *         1
 *        / \
 *       2   3
 *      / \
 *     4   5
 *    /
 *   7
 * Output: 7
 * Explanation: Left boundary path is 1 -> 2 -> 4 -> 7. Leaf node 7 is excluded.
 * Sum = 1 + 2 + 4 = 7.
 *
 * Example 2:
 * Input: root = [10, 20, 30, 40, null, null, null]
 * Tree structure:
 *         10
 *        /  \
 *       20   30
 *      /
 *     40
 * Output: 30
 * Explanation: Left boundary path is 10 -> 20 -> 40. Leaf node 40 is excluded.
 * Sum = 10 + 20 = 30.
 */

public class Solution {

    /**
     * Definition for a binary tree node.
     * This inner static class represents each node in the binary tree.
     */
    static class TreeNode {
        int val;          // The value stored in this node
        TreeNode left;    // Reference to the left child node
        TreeNode right;   // Reference to the right child node

        /**
         * Constructor to create a new TreeNode with a given value.
         * @param val The integer value to store in this node
         */
        TreeNode(int val) {
            this.val = val;
            this.left = null;
            this.right = null;
        }
    }

    /**
     * Computes the sum of all non-leaf nodes along the left boundary of a binary tree.
     *
     * <p>The left boundary is the path from the root to the leftmost leaf,
     * always preferring the left child; if no left child exists, we follow
     * the right child instead. The root is always part of the boundary.
     * Leaf nodes are explicitly excluded from the sum.</p>
     *
     * <p>Algorithm walkthrough for Example 1 (root = [1,2,3,4,5,null,null,7]):
     * <pre>
     *   Start at node 1 (not a leaf) → add 1, sum = 1
     *   Node 1 has left child 2 → move to node 2
     *   Node 2 (not a leaf) → add 2, sum = 3
     *   Node 2 has left child 4 → move to node 4
     *   Node 4 (not a leaf, has left child 7) → add 4, sum = 7
     *   Node 4 has left child 7 → move to node 7
     *   Node 7 IS a leaf → do NOT add, stop traversal
     *   Final sum = 7 ✓
     * </pre>
     * </p>
     *
     * @param root The root node of the binary tree (may be null)
     * @return The integer sum of all non-leaf nodes on the left boundary;
     *         returns 0 if the tree is empty
     * @implNote Time complexity:  O(h) where h is the height of the tree,
     *           since we only traverse one path from root to leaf.
     *           Space complexity: O(1) extra space (iterative, no recursion stack).
     */
    public int sumOfLeftBoundary(TreeNode root) {

        // ── Step 1: Handle the edge case of an empty tree ──────────────────
        // If the root is null, there are no nodes at all, so the sum is 0.
        if (root == null) {
            return 0;
        }

        // ── Step 2: Initialize the running sum and the current pointer ──────
        // We will walk down the left boundary iteratively.
        int sum = 0;
        TreeNode current = root;

        // ── Step 3: Traverse the left boundary from root toward the leaf ────
        // We keep moving as long as 'current' is NOT a leaf node.
        // A leaf node has no left child AND no right child.
        // We stop BEFORE adding a leaf, because the problem says to exclude leaves.
        while (current != null) {

            // ── Step 3a: Check whether the current node is a leaf ───────────
            // A leaf has neither a left child nor a right child.
            boolean isLeaf = (current.left == null && current.right == null);

            if (isLeaf) {
                // This node is a leaf → do NOT add its value to the sum.
                // Also stop the loop because we've reached the end of the boundary.
                break;
            }

            // ── Step 3b: Current node is NOT a leaf → add its value ─────────
            // Non-leaf boundary nodes contribute to the sum.
            sum += current.val;

            // ── Step 3c: Decide which child to follow next ───────────────────
            // Left boundary rule:
            //   • If a left child exists  → always go left.
            //   • If no left child exists → go right (only option remaining).
            if (current.left != null) {
                // Prefer the left child (standard left-boundary direction).
                current = current.left;
            } else {
                // No left child available; follow the right child to continue
                // the boundary path downward.
                current = current.right;
            }
        }

        // ── Step 4: Return the accumulated sum ──────────────────────────────
        return sum;
    }

    // =========================================================================
    // Helper method: Build a binary tree from a level-order (BFS) array.
    // 'null' entries in the array represent missing nodes.
    // This makes it easy to construct test trees from the problem examples.
    // =========================================================================

    /**
     * Builds a binary tree from a level-order integer array representation.
     * {@code null} elements in the array indicate absent nodes.
     *
     * @param values Array of Integer values in level-order (BFS) order;
     *               use {@code null} for missing nodes
     * @return The root {@link TreeNode} of the constructed tree,
     *         or {@code null} if the array is empty or its first element is null
     * @implNote Time complexity:  O(n) where n is the length of the array.
     *           Space complexity: O(n) for the queue used during construction.
     */
    public static TreeNode buildTree(Integer[] values) {
        // If the array is empty or the root value itself is null, return null.
        if (values == null || values.length == 0 || values[0] == null) {
            return null;
        }

        // Create the root node from the first element.
        TreeNode root = new TreeNode(values[0]);

        // Use a queue to keep track of nodes whose children still need to be assigned.
        java.util.Queue<TreeNode> queue = new java.util.LinkedList<>();
        queue.offer(root);

        // 'index' tracks our position in the values array.
        int index = 1;

        // Process nodes level by level.
        while (!queue.isEmpty() && index < values.length) {
            // Take the next node from the front of the queue.
            TreeNode node = queue.poll();

            // Assign the LEFT child if the next value is not null.
            if (index < values.length) {
                if (values[index] != null) {
                    node.left = new TreeNode(values[index]);
                    queue.offer(node.left); // This child may itself have children.
                }
                index++; // Move to the next value regardless.
            }

            // Assign the RIGHT child if the next value is not null.
            if (index < values.length) {
                if (values[index] != null) {
                    node.right = new TreeNode(values[index]);
                    queue.offer(node.right); // This child may itself have children.
                }
                index++; // Move to the next value regardless.
            }
        }

        return root;
    }

    // =========================================================================
    // Helper method: Print the left boundary path for visual verification.
    // =========================================================================

    /**
     * Prints the left boundary path of the tree (for debugging / demonstration).
     * Leaf nodes are marked with an asterisk (*) to show they are excluded.
     *
     * @param root The root of the binary tree
     */
    public static void printLeftBoundaryPath(TreeNode root) {
        if (root == null) {
            System.out.println("  (empty tree)");
            return;
        }

        System.out.print("  Left boundary path: ");
        TreeNode current = root;
        StringBuilder sb = new StringBuilder();

        while (current != null) {
            boolean isLeaf = (current.left == null && current.right == null);
            if (isLeaf) {
                sb.append(current.val).append("* (leaf, excluded)");
                break;
            } else {
                sb.append(current.val).append(" -> ");
                current = (current.left != null) ? current.left : current.right;
            }
        }

        System.out.println(sb);
    }

    // =========================================================================
    // Main method: Demonstrates the solution with the provided examples.
    // =========================================================================

    /**
     * Entry point — runs the two examples from the problem description and
     * prints the results to standard output.
     *
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {

        Solution solution = new Solution();

        // ── Example 1 ────────────────────────────────────────────────────────
        // Tree:
        //         1
        //        / \
        //       2   3
        //      / \
        //     4   5
        //    /
        //   7
        // Level-order array: [1, 2, 3, 4, 5, null, null, 7]
        // Left boundary: 1 -> 2 -> 4 -> 7*  (7 is a leaf, excluded)
        // Expected sum: 1 + 2 + 4 = 7
        System.out.println("=== Example 1 ===");
        Integer[] values1 = {1, 2, 3, 4, 5, null, null, 7};
        TreeNode root1 = buildTree(values1);
        printLeftBoundaryPath(root1);
        int result1 = solution.sumOfLeftBoundary(root1);
        System.out.println("  Sum of left boundary (non-leaf) nodes: " + result1);
        System.out.println("  Expected: 7");
        System.out.println("  PASS: " + (result1 == 7));
        System.out.println();

        // ── Example 2 ────────────────────────────────────────────────────────
        // Tree:
        //         10
        //        /  \
        //       20   30
        //      /
        //     40
        // Level-order array: [10, 20, 30, 40, null, null, null]
        // Left boundary: 10 -> 20 -> 40*  (40 is a leaf, excluded)
        // Expected sum: 10 + 20 = 30
        System.out.println("=== Example 2 ===");
        Integer[] values2 = {10, 20, 30, 40, null, null, null};
        TreeNode root2 = buildTree(values2);
        printLeftBoundaryPath(root2);
        int result2 = solution.sumOfLeftBoundary(root2);
        System.out.println("  Sum of left boundary (non-leaf) nodes: " + result2);
        System.out.println("  Expected: 30");
        System.out.println("  PASS: " + (result2 == 30));
        System.out.println();

        // ── Edge Case: Single node (root is also a leaf) ─────────────────────
        // Tree:  42
        // The root is a leaf, so it should be excluded → sum = 0
        System.out.println("=== Edge Case: Single node ===");
        Integer[] values3 = {42};
        TreeNode root3 = buildTree(values3);
        printLeftBoundaryPath(root3);
        int result3 = solution.sumOfLeftBoundary(root3);
        System.out.println("  Sum of left boundary (non-leaf) nodes: " + result3);
        System.out.println("  Expected: 0 (root is a leaf, excluded)");
        System.out.println("  PASS: " + (result3 == 0));
        System.out.println();

        // ── Edge Case: Right-skewed tree (boundary follows right children) ───
        // Tree:
        //   1
        //    \
        //     2
        //      \
        //       3
        // Left boundary: 1 -> 2 -> 3*  (3 is a leaf, excluded)
        // Expected sum: 1 + 2 = 3
        System.out.println("=== Edge Case: Right-skewed tree ===");
        TreeNode root4 = new TreeNode(1);
        root4.right = new TreeNode(2);
        root4.right.right = new TreeNode(3);
        printLeftBoundaryPath(root4);
        int result4 = solution.sumOfLeftBoundary(root4);
        System.out.println("  Sum of left boundary (non-leaf) nodes: " + result4);
        System.out.println("  Expected: 3");
        System.out.println("  PASS: " + (result4 == 3));
        System.out.println();

        // ── Edge Case: Null root ──────────────────────────────────────────────
        System.out.println("=== Edge Case: Null root ===");
        int result5 = solution.sumOfLeftBoundary(null);
        System.out.println("  Sum of left boundary (non-leaf) nodes: " + result5);
        System.out.println("  Expected: 0");
        System.out.println("  PASS: " + (result5 == 0));
    }
}