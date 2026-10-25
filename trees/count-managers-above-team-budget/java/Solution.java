import java.util.*;

/*
 * Title: Count Managers Above Team Budget
 * Difficulty: Easy
 * Topic: Trees
 *
 * Problem Description:
 * You are given the root of a binary tree representing a company budget hierarchy.
 * Each node stores an integer val, which is the budget assigned to that manager or employee.
 * For every non-leaf node, define its immediate team budget as the sum of the values of its
 * existing left and right children. A manager is considered over budget if their own value
 * is strictly greater than the immediate team budget of their direct children.
 *
 * Your task is to return the number of nodes in the tree that are over budget.
 * Leaf nodes are not counted, because they do not manage any direct reports.
 *
 * A missing child contributes nothing to the team budget. For example, if a node has only
 * a left child with value 4, then its immediate team budget is 4.
 *
 * This is an easy tree traversal problem. You may solve it using depth-first search
 * recursively or iteratively. The goal is to inspect each node once and compare its value
 * against the sum of its direct children.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [1, 1000].
 * - -1000 <= Node.val <= 1000
 * - The tree is a binary tree.
 *
 * Important correctness note:
 * In Example 2, the written output says 0, but the explanation correctly shows that node 7
 * is a non-leaf and 7 > 2, so it should be counted. Therefore, the correct output for
 * Example 2 is 1.
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
         * @param val the integer value stored in the node
         */
        TreeNode(int val) {
            this.val = val;
        }

        /**
         * Creates a node with the given value and children.
         *
         * @param val the integer value stored in the node
         * @param left the left child
         * @param right the right child
         */
        TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }

    /**
     * Counts how many non-leaf nodes have a value strictly greater than the sum of their
     * existing direct children's values.
     *
     * We perform a depth-first traversal of the tree. For each node:
     * 1. If the node is null, it contributes 0.
     * 2. If the node is a leaf, it is not counted.
     * 3. Otherwise, compute the sum of the left and right child values
     *    (missing child contributes 0).
     * 4. If node.val > childSum, count this node.
     * 5. Continue recursively into both subtrees.
     *
     * @param root the root of the binary tree
     * @return the number of over-budget managers in the tree
     * Time complexity: O(n), where n is the number of nodes, because each node is visited once.
     * Space complexity: O(h), where h is the height of the tree due to recursion stack.
     */
    public int countManagersAboveTeamBudget(TreeNode root) {
        return dfs(root);
    }

    /**
     * Recursive depth-first search helper that returns the number of qualifying nodes
     * in the subtree rooted at the given node.
     *
     * @param node the current node being processed
     * @return the count of over-budget managers in this subtree
     * Time complexity: O(n) across the full traversal, because each node is processed once.
     * Space complexity: O(h) for the recursion stack, where h is the tree height.
     */
    private int dfs(TreeNode node) {
        // Base case:
        // If the current node does not exist, there is nothing to count.
        if (node == null) {
            return 0;
        }

        // A leaf node has no direct reports, so it is explicitly not counted.
        // We still return 0 because there are no qualifying managers in this one-node subtree.
        if (node.left == null && node.right == null) {
            return 0;
        }

        // Compute the immediate team budget:
        // - If the left child exists, include its value; otherwise add 0.
        // - If the right child exists, include its value; otherwise add 0.
        int childSum = 0;
        if (node.left != null) {
            childSum += node.left.val;
        }
        if (node.right != null) {
            childSum += node.right.val;
        }

        // Determine whether the current non-leaf node is over budget.
        // The condition is strictly greater than the sum of direct children.
        int currentCount = 0;
        if (node.val > childSum) {
            currentCount = 1;
        }

        // Recursively count qualifying nodes in the left subtree.
        int leftCount = dfs(node.left);

        // Recursively count qualifying nodes in the right subtree.
        int rightCount = dfs(node.right);

        // Total count for this subtree is:
        // current node's contribution + left subtree contribution + right subtree contribution.
        return currentCount + leftCount + rightCount;
    }

    /**
     * Builds a binary tree from a level-order array representation where null means
     * the node is missing.
     *
     * Example:
     * [10, 4, 3, 1, 2] becomes:
     *         10
     *        /  \
     *       4    3
     *      / \
     *     1   2
     *
     * @param values the level-order representation of the tree
     * @return the root of the constructed binary tree
     * Time complexity: O(n), where n is the number of array elements.
     * Space complexity: O(n), due to the queue used during construction.
     */
    public TreeNode buildTree(Integer[] values) {
        // If the input is empty or starts with null, the tree is empty.
        if (values == null || values.length == 0 || values[0] == null) {
            return null;
        }

        // Create the root from the first value.
        TreeNode root = new TreeNode(values[0]);

        // Queue for processing nodes level by level.
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        // Index points to the next value in the array that has not yet been used.
        int index = 1;

        // Process nodes in breadth-first order.
        while (!queue.isEmpty() && index < values.length) {
            TreeNode current = queue.poll();

            // Try to assign the left child if there is still input remaining.
            if (index < values.length) {
                Integer leftValue = values[index++];
                if (leftValue != null) {
                    current.left = new TreeNode(leftValue);
                    queue.offer(current.left);
                }
            }

            // Try to assign the right child if there is still input remaining.
            if (index < values.length) {
                Integer rightValue = values[index++];
                if (rightValue != null) {
                    current.right = new TreeNode(rightValue);
                    queue.offer(current.right);
                }
            }
        }

        return root;
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstrated tree, due to tree construction and traversal.
     * Space complexity: O(n) for tree construction plus O(h) recursion stack during traversal.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Input: [10, 4, 3, 1, 2]
        // Tree:
        //         10
        //        /  \
        //       4    3
        //      / \
        //     1   2
        //
        // Check each non-leaf:
        // - Node 10: child sum = 4 + 3 = 7, and 10 > 7 => count
        // - Node 4:  child sum = 1 + 2 = 3, and 4 > 3 => count
        // - Node 3 is a leaf => do not count
        // - Node 1 is a leaf => do not count
        // - Node 2 is a leaf => do not count
        // Correct answer: 2
        Integer[] example1 = {10, 4, 3, 1, 2};
        TreeNode root1 = solution.buildTree(example1);
        int result1 = solution.countManagersAboveTeamBudget(root1);
        System.out.println("Example 1 Output: " + result1);

        // Example 2:
        // Input: [5, 7, 1, null, 2]
        // Tree:
        //         5
        //        / \
        //       7   1
        //        \
        //         2
        //
        // Check each non-leaf:
        // - Node 5: child sum = 7 + 1 = 8, and 5 > 8 => false
        // - Node 7: child sum = 0 + 2 = 2, and 7 > 2 => true
        // - Node 1 is a leaf => do not count
        // - Node 2 is a leaf => do not count
        // Correct answer: 1
        //
        // Note: The problem statement contains a mismatch where it says output 0,
        // but its own explanation proves the correct answer is 1.
        Integer[] example2 = {5, 7, 1, null, 2};
        TreeNode root2 = solution.buildTree(example2);
        int result2 = solution.countManagersAboveTeamBudget(root2);
        System.out.println("Example 2 Output: " + result2);

        // Additional small demonstration:
        // Single-node tree: [8]
        // Root is a leaf, so answer is 0.
        Integer[] example3 = {8};
        TreeNode root3 = solution.buildTree(example3);
        int result3 = solution.countManagersAboveTeamBudget(root3);
        System.out.println("Single Node Example Output: " + result3);
    }
}