/*
 * Title: Maximum Delay Between Ancestor and Descendant Checkpoints
 * Difficulty: Medium
 * Topic: Trees
 *
 * Problem Description:
 * You are given the root of a tree representing checkpoint events in a monitoring system.
 * Each node stores an integer timestamp. A checkpoint A is considered an ancestor of
 * checkpoint B if A lies on the path from the root to B, and A and B may be the same node.
 * The delay between an ancestor and a descendant is defined as:
 *
 *     descendant.timestamp - ancestor.timestamp
 *
 * Your task is to return the maximum possible delay over all valid ancestor-descendant
 * pairs in the tree.
 *
 * This is not the same as finding the maximum difference between any two nodes anywhere
 * in the tree. The earlier checkpoint must be an ancestor of the later checkpoint.
 * Because timestamps may decrease or increase as you move down the tree, the best answer
 * may come from tracking the smallest timestamp seen so far on each root-to-node path.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [1, 100000].
 * - Each node value is an integer timestamp in the range [-1000000000, 1000000000].
 * - The tree is not necessarily balanced.
 * - Since a node can be paired with itself, the minimum possible answer is 0.
 *
 * Example 1:
 * Input: root = [8,3,10,1,6,null,14,null,null,4,7,13]
 * Output: 11
 * Explanation: The best pair is ancestor 3 and descendant 14, giving 14 - 3 = 11.
 * Although 1 and 14 differ by 13, they are not in an ancestor-descendant relationship.
 *
 * Example 2:
 * Input: root = [5,9,2,12,null,-1,8]
 * Output: 7
 * Explanation: One optimal pair is ancestor 5 and descendant 12, so the maximum delay
 * is 12 - 5 = 7. The path through node 2 also contains values -1 and 8, giving delays
 * 0 and 6 respectively, but neither exceeds 7.
 */

import java.util.*;

public class Solution {

    /**
     * Basic binary tree node definition used for the checkpoint tree.
     */
    public static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        /**
         * Creates a node with the given timestamp value.
         *
         * @param val the timestamp stored in this node
         */
        TreeNode(int val) {
            this.val = val;
        }
    }

    /**
     * Computes the maximum possible delay between an ancestor checkpoint and a descendant
     * checkpoint in the tree.
     *
     * The key idea is:
     * - While walking from the root down to any node, keep track of the smallest timestamp
     *   seen so far on that current path.
     * - If the current node has value currentValue, then the best ancestor for this node
     *   is simply the minimum value seen on the path so far.
     * - Therefore, the best delay ending at this node is:
     *
     *       currentValue - minimumValueOnPath
     *
     * - We evaluate this for every node exactly once and keep the global maximum.
     *
     * This method uses an iterative depth-first traversal to safely handle very deep trees
     * without risking recursive stack overflow when the tree has up to 100000 nodes.
     *
     * @param root the root of the checkpoint tree
     * @return the maximum delay over all valid ancestor-descendant pairs
     * Time complexity: O(n), where n is the number of nodes in the tree
     * Space complexity: O(h) on average for the traversal stack, O(n) in the worst case
     *                   for a highly skewed tree
     */
    public int maxAncestorDelay(TreeNode root) {
        if (root == null) {
            return 0;
        }

        int answer = 0;

        // Each stack entry stores:
        // 1) the current node we are visiting
        // 2) the minimum value seen from the root down to this node's parent/path
        //
        // For the root, the minimum on its path is simply its own value.
        Deque<NodeState> stack = new ArrayDeque<>();
        stack.push(new NodeState(root, root.val));

        while (!stack.isEmpty()) {
            NodeState state = stack.pop();
            TreeNode node = state.node;
            int minSoFar = state.minSoFar;

            // Step 1:
            // Compute the best delay where the current node acts as the descendant.
            //
            // Why does this work?
            // Because any valid ancestor of this node must lie on the current root-to-node path.
            // Among all those ancestors, the one that maximizes:
            //
            //     node.val - ancestor.val
            //
            // is the ancestor with the SMALLEST value on that path.
            //
            // That smallest value is exactly minSoFar.
            int currentDelay = node.val - minSoFar;

            // Step 2:
            // Update the global answer if this node gives a better delay.
            //
            // Note:
            // Since a node can pair with itself, the answer can never be negative.
            // For the root, currentDelay will be root.val - root.val = 0.
            if (currentDelay > answer) {
                answer = currentDelay;
            }

            // Step 3:
            // Update the minimum value seen on the path to include the current node itself.
            //
            // This updated minimum will be passed down to the children, because for them,
            // the current node becomes part of their ancestor path.
            int newMinSoFar = Math.min(minSoFar, node.val);

            // Step 4:
            // Continue DFS traversal by pushing children with the updated path minimum.
            if (node.right != null) {
                stack.push(new NodeState(node.right, newMinSoFar));
            }
            if (node.left != null) {
                stack.push(new NodeState(node.left, newMinSoFar));
            }
        }

        return answer;
    }

    /**
     * Builds a binary tree from a level-order array representation where null means
     * "no node at this position".
     *
     * Example:
     * [8, 3, 10, 1, 6, null, 14, null, null, 4, 7, 13]
     *
     * This helper is used only for demonstration in main.
     *
     * @param values the level-order representation of the tree
     * @return the root of the constructed binary tree, or null if the input is empty
     * Time complexity: O(n), where n is the number of array entries
     * Space complexity: O(n), due to the queue used during construction
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
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * This method:
     * - builds the sample trees,
     * - runs the algorithm,
     * - prints the results,
     * - and shows the expected outputs for easy verification.
     *
     * Verified examples:
     * Example 1:
     *   Tree: [8,3,10,1,6,null,14,null,null,4,7,13]
     *   Best valid pair: ancestor 3, descendant 14
     *   Delay = 14 - 3 = 11
     *
     * Example 2:
     *   Tree: [5,9,2,12,null,-1,8]
     *   Best valid pair: ancestor 5, descendant 12
     *   Delay = 12 - 5 = 7
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) total for the demonstrated examples
     * Space complexity: O(n) total for the constructed trees and traversal
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        Integer[] example1 = {8, 3, 10, 1, 6, null, 14, null, null, 4, 7, 13};
        TreeNode root1 = solution.buildTree(example1);
        int result1 = solution.maxAncestorDelay(root1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected: 11");

        Integer[] example2 = {5, 9, 2, 12, null, -1, 8};
        TreeNode root2 = solution.buildTree(example2);
        int result2 = solution.maxAncestorDelay(root2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected: 7");

        Integer[] example3 = {-5};
        TreeNode root3 = solution.buildTree(example3);
        int result3 = solution.maxAncestorDelay(root3);
        System.out.println("Single node example result: " + result3);
        System.out.println("Expected: 0");

        Integer[] example4 = {7, 6, 5, 4, 3, 2, 1};
        TreeNode root4 = solution.buildTree(example4);
        int result4 = solution.maxAncestorDelay(root4);
        System.out.println("Decreasing values example result: " + result4);
        System.out.println("Expected: 0");
    }

    /**
     * Small helper class representing one DFS stack state.
     */
    private static class NodeState {
        TreeNode node;
        int minSoFar;

        /**
         * Creates a traversal state for a node and the minimum value seen on its path.
         *
         * @param node the current tree node
         * @param minSoFar the minimum timestamp seen from the root to this node
         */
        NodeState(TreeNode node, int minSoFar) {
            this.node = node;
            this.minSoFar = minSoFar;
        }
    }
}