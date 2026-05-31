```java
/*
 * Title: Deepest Common Ancestor at Target Depth
 * Difficulty: Medium
 * Topic: Trees
 *
 * Problem Description:
 * Given the root of a binary tree and an integer `depth`, find the lowest common ancestor (LCA)
 * of all nodes that exist exactly at the given `depth` in the tree. The depth of the root node is 1.
 *
 * If there is only one node at the given depth, return that node itself.
 * If no nodes exist at the given depth, return null.
 *
 * The 'deepest common ancestor at target depth' is defined as the deepest node in the tree
 * that is an ancestor of every node found at exactly `depth` levels from the root.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [1, 1000].
 * - -10^5 <= Node.val <= 10^5
 * - All node values are unique.
 * - 1 <= depth <= 1000
 *
 * Example 1:
 * Input: root = [3,5,1,6,2,0,8,null,null,7,4], depth = 4
 * Output: 2
 * Explanation: Nodes at depth 4 are [7, 4]. Their LCA is node 2.
 *
 * Example 2:
 * Input: root = [1,2,3,4,5], depth = 3
 * Output: 2
 * Explanation: Nodes at depth 3 are [4, 5]. Their LCA is node 2.
 *
 * Example 3:
 * Input: root = [1,2,3], depth = 1
 * Output: 1
 * Explanation: Only the root exists at depth 1, so return the root.
 */

import java.util.*;

/**
 * Solution class for finding the Deepest Common Ancestor at a Target Depth.
 *
 * <p>Key Insight: We use a recursive post-order traversal. For each node, we determine
 * the maximum depth reachable in its subtree. If both left and right subtrees can reach
 * the target depth, then the current node is the LCA. If only one side reaches the target
 * depth, we propagate that side's result upward.</p>
 */
public class Solution {

    /**
     * Definition for a binary tree node.
     */
    static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode(int val) {
            this.val = val;
        }

        TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }

    /**
     * Helper class to carry two pieces of information back up the recursion:
     * 1. The LCA candidate node found so far in this subtree.
     * 2. The maximum depth reachable from this subtree.
     *
     * <p>This allows us to determine, at each node, whether both sides reach the target depth.</p>
     */
    static class Result {
        TreeNode node;   // The LCA candidate (or null if no target-depth node found)
        int maxDepth;    // The maximum depth reachable in this subtree

        Result(TreeNode node, int maxDepth) {
            this.node = node;
            this.maxDepth = maxDepth;
        }
    }

    /**
     * Finds the lowest common ancestor of all nodes at exactly the given target depth.
     *
     * <p>Algorithm Overview:
     * We perform a post-order DFS. At each node, we ask: "What is the deepest level
     * reachable in my left subtree? In my right subtree?"
     *
     * - If both left and right subtrees reach the target depth → current node is the LCA.
     * - If only left reaches the target depth → propagate left's LCA candidate upward.
     * - If only right reaches the target depth → propagate right's LCA candidate upward.
     * - If neither reaches the target depth → no target-depth nodes in this subtree.
     * </p>
     *
     * @param root  The root of the binary tree.
     * @param depth The target depth (1-indexed, root is depth 1).
     * @return The LCA node of all nodes at the given depth, or null if none exist.
     *
     * Time Complexity: O(N) where N is the number of nodes — we visit each node once.
     * Space Complexity: O(H) where H is the height of the tree — recursion stack depth.
     */
    public TreeNode lcaAtDepth(TreeNode root, int depth) {
        // Edge case: if the tree is empty, return null immediately
        if (root == null) {
            return null;
        }

        // Start the recursive DFS from the root at current depth = 1
        // The helper returns a Result containing the LCA candidate and max depth info
        Result result = dfs(root, 1, depth);

        // The result's node is our answer:
        // - It will be non-null only if at least one node at target depth was found
        // - It will be the LCA of all such nodes
        return result.node;
    }

    /**
     * Recursive DFS helper that returns a Result for the subtree rooted at `node`.
     *
     * <p>For each node, we:
     * 1. Check if we've reached the target depth (base case for target level).
     * 2. Recurse into left and right children.
     * 3. Combine results: if both sides reach target depth, current node is the LCA.
     * </p>
     *
     * @param node         The current node being processed.
     * @param currentDepth The depth of the current node (root = 1).
     * @param targetDepth  The depth we are searching for.
     * @return A Result containing the LCA candidate and the max depth in this subtree.
     *
     * Time Complexity: O(N) — visits each node exactly once.
     * Space Complexity: O(H) — recursion stack, H = height of tree.
     */
    private Result dfs(TreeNode node, int currentDepth, int targetDepth) {
        // BASE CASE 1: We've gone past a null node (leaf's child)
        // Return null node and depth = currentDepth - 1 (the leaf's depth)
        if (node == null) {
            // We return currentDepth - 1 because this null represents one level
            // below the last real node. The real node was at currentDepth - 1.
            return new Result(null, currentDepth - 1);
        }

        // BASE CASE 2: We've reached exactly the target depth
        // This node IS one of the nodes we're looking for
        if (currentDepth == targetDepth) {
            // Return this node as an LCA candidate, with maxDepth = targetDepth
            // (we don't need to go deeper; this node itself is at the target)
            return new Result(node, currentDepth);
        }

        // RECURSIVE CASE: We're above the target depth, so recurse into children

        // Step 1: Recurse into the LEFT subtree
        // The left child is at depth currentDepth + 1
        Result leftResult = dfs(node.left, currentDepth + 1, targetDepth);

        // Step 2: Recurse into the RIGHT subtree
        // The right child is at depth currentDepth + 1
        Result rightResult = dfs(node.right, currentDepth + 1, targetDepth);

        // Step 3: Analyze results from both subtrees

        // CASE A: Both left and right subtrees reach the target depth
        // → The current node is the LCA of all target-depth nodes in both subtrees
        if (leftResult.maxDepth == targetDepth && rightResult.maxDepth == targetDepth) {
            // Current node covers target-depth nodes on BOTH sides → it's the LCA
            return new Result(node, targetDepth);
        }

        // CASE B: Only the LEFT subtree reaches the target depth
        // → Propagate the left subtree's LCA candidate upward
        if (leftResult.maxDepth == targetDepth) {
            // The LCA candidate from the left side is the answer for this subtree
            return new Result(leftResult.node, targetDepth);
        }

        // CASE C: Only the RIGHT subtree reaches the target depth
        // → Propagate the right subtree's LCA candidate upward
        if (rightResult.maxDepth == targetDepth) {
            // The LCA candidate from the right side is the answer for this subtree
            return new Result(rightResult.node, targetDepth);
        }

        // CASE D: Neither subtree reaches the target depth
        // → No target-depth nodes exist in this subtree
        // Return null node and the maximum depth we did reach (for parent's information)
        int maxReachableDepth = Math.max(leftResult.maxDepth, rightResult.maxDepth);
        return new Result(null, maxReachableDepth);
    }

    // =========================================================================
    // UTILITY METHODS FOR BUILDING TEST TREES
    // =========================================================================

    /**
     * Builds a binary tree from a level-order array representation.
     * Null values in the array represent missing nodes.
     *
     * @param values Array of Integer values in level-order (null = missing node).
     * @return The root TreeNode of the constructed tree.
     *
     * Time Complexity: O(N) where N is the length of the values array.
     * Space Complexity: O(N) for the queue used during construction.
     */
    public static TreeNode buildTree(Integer[] values) {
        // If the array is empty or the root is null, return null
        if (values == null || values.length == 0 || values[0] == null) {
            return null;
        }

        // Create the root node from the first element
        TreeNode root = new TreeNode(values[0]);

        // Use a queue to keep track of nodes that need children assigned
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        // Index into the values array, starting at index 1 (after root)
        int i = 1;

        // Process nodes level by level
        while (!queue.isEmpty() && i < values.length) {
            // Get the next node that needs children
            TreeNode current = queue.poll();

            // Assign the LEFT child if the next value exists and is not null
            if (i < values.length) {
                if (values[i] != null) {
                    current.left = new TreeNode(values[i]);
                    queue.offer(current.left); // This child may also need children
                }
                i++; // Move to the next value
            }

            // Assign the RIGHT child if the next value exists and is not null
            if (i < values.length) {
                if (values[i] != null) {
                    current.right = new TreeNode(values[i]);
                    queue.offer(current.right); // This child may also need children
                }
                i++; // Move to the next value
            }
        }

        return root;
    }

    /**
     * Prints the tree in a simple level-order format for visualization.
     *
     * @param root The root of the tree to print.
     */
    public static void printTree(TreeNode root) {
        if (root == null) {
            System.out.println("(empty tree)");
            return;
        }

        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);
        System.out.print("Level-order: [");
        boolean first = true;

        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();
            if (!first) System.out.print(", ");
            first = false;

            if (node == null) {
                System.out.print("null");
            } else {
                System.out.print(node.val);
                queue.offer(node.left);
                queue.offer(node.right);
            }
        }
        System.out.println("]");
    }

    // =========================================================================
    // MAIN METHOD — DEMONSTRATES THE SOLUTION WITH SAMPLE INPUTS
    // =========================================================================

    /**
     * Main method demonstrating the solution with all provided examples.
     * Traces through each example and prints the results.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        System.out.println("=== Deepest Common Ancestor at Target Depth ===\n");

        // -----------------------------------------------------------------------
        // EXAMPLE 1:
        // Tree: [3,5,1,6,2,0,8,null,null,7,4], depth = 4
        //
        //         3          (depth 1)
        //        / \
        //       5   1        (depth 2)
        //      / \ / \
        //     6  2 0  8      (depth 3)
        //       / \
        //      7   4         (depth 4)
        //
        // Nodes at depth 4: [7, 4]
        // LCA of 7 and 4 is node 2
        // Expected Output: 2
        // -----------------------------------------------------------------------
        System.out.println("--- Example 1 ---");
        Integer[] values1 = {3, 5, 1, 6, 2, 0, 8, null, null, 7, 4};
        TreeNode root1 = buildTree(values1);
        int depth1 = 4;
        System.out.println("Tree: [3,5,1,6,2,0,8,null,null,7,4]");
        System.out.println("Target Depth: " + depth1);
        System.out.println("Nodes at depth 4: [7, 4]");
        System.out.println("Expected LCA: 2");
        TreeNode result1 = solution.lcaAtDepth(root1, depth1);
        System.out.println("Computed LCA: " + (result1 != null ? result1.val : "null"));
        System.out.println("PASS: " + (result1 != null && result1.val == 2));
        System.out.println();

        // -----------------------------------------------------------------------
        // EXAMPLE 2:
        // Tree: [1,2,3,4,5], depth = 3
        //
        //         1          (depth 1)
        //        / \
        //       2   3        (depth 2)
        //      / \
        //     4   5          (depth 3)
        //
        // Nodes at depth 3: [4, 5]
        // Node 3 has no children at depth 3.
        // LCA of 4 and 5 is node 2
        // Expected Output: 2
        // -----------------------------------------------------------------------
        System.out.println("--- Example 2 ---");
        Integer[] values2 = {1, 2, 3, 4, 5};
        TreeNode root2 = buildTree(values2);
        int depth2 = 3;
        System.out.println("Tree: [1,2,3,4,5]");
        System.out.println("Target Depth: " + depth2);
        System.out.println("Nodes at depth 3: [4, 5]");
        System.out.println("Expected LCA: 2");
        TreeNode result2 = solution.lcaAtDepth(root2, depth2);
        System.out.println("Computed LCA: " + (result2 != null ? result2.val : "null"));
        System.out.println("PASS: " + (result2 != null && result2.val == 2));
        System.out.println();

        // -----------------------------------------------------------------------
        // EXAMPLE 3:
        // Tree: [1,2,3], depth = 1
        //
        //         1          (depth 1)
        //        / \
        //       2   3        (depth 2)
        //
        // Only the root exists at depth 1.
        // Expected Output: 1
        // -----------------------------------------------------------------------
        System.out.println("--- Example 3 ---");
        Integer[] values3 = {1, 2, 3};
        TreeNode root3 = build