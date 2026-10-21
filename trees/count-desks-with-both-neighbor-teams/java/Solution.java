import java.util.*;

/*
 * Title: Count Desks with Both Neighbor Teams
 * Difficulty: Easy
 * Topic: Trees
 *
 * Problem Description:
 * A company stores its office layout as a binary tree. Each node represents a desk
 * and contains an integer team ID. The left and right child pointers represent the
 * two neighboring desks directly connected to that desk in the layout map.
 *
 * A desk is called a mixed-neighbor desk if:
 * 1. It has both a left child and a right child, and
 * 2. The team ID of the current desk is different from both children.
 *
 * In other words, for a desk with value x, left child value a, and right child value b,
 * the desk should be counted only if:
 * - x != a
 * - x != b
 *
 * The task is to return the number of such desks in the tree.
 *
 * Notes:
 * - If the tree is empty, return 0.
 * - The tree is a binary tree and is not necessarily complete or balanced.
 *
 * Example 1:
 * Input: root = [5,3,8,3,4,8,9]
 * Correct Output: 1
 * Explanation:
 * - Node 5 has children 3 and 8. Both are different from 5, so count it.
 * - Node 3 has children 3 and 4. Left child equals parent, so do not count it.
 * - Node 8 has children 8 and 9. Left child equals parent, so do not count it.
 * - Leaves are not counted because they do not have two children.
 *
 * Example 2:
 * Input: root = [7,2,9,1,2,null,10]
 * Output: 1
 * Explanation:
 * - Node 7 has children 2 and 9. Both are different from 7, so count it.
 * - Node 2 has children 1 and 2. Right child equals parent, so do not count it.
 * - Node 9 has only one child, so do not count it.
 */

public class Solution {

    /**
     * Basic binary tree node definition used for this problem.
     */
    public static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        /**
         * Creates a tree node with the given value.
         *
         * @param val the integer value stored in this node
         */
        TreeNode(int val) {
            this.val = val;
        }
    }

    /**
     * Counts how many nodes in the binary tree have exactly two children and
     * have a value different from both child values.
     *
     * This method uses depth-first search (DFS) recursion.
     *
     * @param root the root of the binary tree; may be null for an empty tree
     * @return the number of mixed-neighbor desks in the tree
     * Time complexity: O(n), where n is the number of nodes in the tree,
     * because each node is visited exactly once.
     * Space complexity: O(h), where h is the height of the tree,
     * due to the recursion stack. In the worst case, this can be O(n).
     */
    public int countMixedNeighborDesks(TreeNode root) {
        return dfs(root);
    }

    /**
     * Recursively traverses the tree and counts valid nodes.
     *
     * A node is counted if:
     * - it has a left child
     * - it has a right child
     * - its value is different from the left child's value
     * - its value is different from the right child's value
     *
     * @param node the current node being processed
     * @return the number of valid nodes in the subtree rooted at {@code node}
     * Time complexity: O(n), where n is the number of nodes in this subtree.
     * Space complexity: O(h), where h is the height of this subtree.
     */
    private int dfs(TreeNode node) {
        // Base case:
        // If the current node is null, there is nothing to process,
        // so the count contributed by this subtree is 0.
        if (node == null) {
            return 0;
        }

        // Start with 0 for the current node.
        int currentCount = 0;

        // We only consider the current node if it has BOTH children.
        if (node.left != null && node.right != null) {
            // Check the exact condition from the problem:
            // the current node's value must be different from both child values.
            if (node.val != node.left.val && node.val != node.right.val) {
                currentCount = 1;
            }
        }

        // Recursively count valid nodes in the left subtree.
        int leftCount = dfs(node.left);

        // Recursively count valid nodes in the right subtree.
        int rightCount = dfs(node.right);

        // Total count for this subtree =
        // count from current node + count from left subtree + count from right subtree.
        return currentCount + leftCount + rightCount;
    }

    /**
     * Builds a binary tree from a level-order array representation.
     *
     * The input list uses {@code null} to represent missing nodes.
     * For example:
     * [5, 3, 8, 3, 4, 8, 9]
     *
     * means:
     *          5
     *        /   \
     *       3     8
     *      / \   / \
     *     3   4 8   9
     *
     * @param values the level-order node values, where null means no node
     * @return the root of the constructed binary tree, or null if the input is empty
     * Time complexity: O(n), where n is the number of elements in the list.
     * Space complexity: O(n), due to the queue used during tree construction.
     */
    public TreeNode buildTreeFromLevelOrder(Integer[] values) {
        // If the array is empty, there is no tree to build.
        if (values == null || values.length == 0) {
            return null;
        }

        // If the first value is null, the root does not exist.
        if (values[0] == null) {
            return null;
        }

        // Create the root node from the first element.
        TreeNode root = new TreeNode(values[0]);

        // Queue for processing nodes level by level.
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        // Index points to the next value in the array that we have not used yet.
        int index = 1;

        // Continue while there are parent nodes waiting in the queue
        // and there are still values left in the input array.
        while (!queue.isEmpty() && index < values.length) {
            // Take the next parent node from the queue.
            TreeNode current = queue.poll();

            // Try to assign the left child if there is still an input value available.
            if (index < values.length) {
                Integer leftValue = values[index];
                index++;

                // Only create a child node if the value is not null.
                if (leftValue != null) {
                    current.left = new TreeNode(leftValue);
                    queue.offer(current.left);
                }
            }

            // Try to assign the right child if there is still an input value available.
            if (index < values.length) {
                Integer rightValue = values[index];
                index++;

                // Only create a child node if the value is not null.
                if (rightValue != null) {
                    current.right = new TreeNode(rightValue);
                    queue.offer(current.right);
                }
            }
        }

        return root;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments; not used in this program
     * @return nothing
     * Time complexity: O(n) per demonstration tree, due to tree construction and traversal.
     * Space complexity: O(n), due to the constructed tree and auxiliary queue/recursion.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Input: [5,3,8,3,4,8,9]
        // Tree:
        //         5
        //       /   \
        //      3     8
        //     / \   / \
        //    3   4 8   9
        //
        // Check each internal node carefully:
        // - Node 5: children are 3 and 8, both different from 5 -> count
        // - Node 3: children are 3 and 4, left child equals parent -> do not count
        // - Node 8: children are 8 and 9, left child equals parent -> do not count
        // Total = 1
        Integer[] example1 = {5, 3, 8, 3, 4, 8, 9};
        TreeNode root1 = solution.buildTreeFromLevelOrder(example1);
        int result1 = solution.countMixedNeighborDesks(root1);
        System.out.println("Example 1 Output: " + result1);

        // Example 2:
        // Input: [7,2,9,1,2,null,10]
        // Tree:
        //         7
        //       /   \
        //      2     9
        //     / \     \
        //    1   2     10
        //
        // Check each internal node:
        // - Node 7: children are 2 and 9, both different from 7 -> count
        // - Node 2: children are 1 and 2, right child equals parent -> do not count
        // - Node 9: only one child -> do not count
        // Total = 1
        Integer[] example2 = {7, 2, 9, 1, 2, null, 10};
        TreeNode root2 = solution.buildTreeFromLevelOrder(example2);
        int result2 = solution.countMixedNeighborDesks(root2);
        System.out.println("Example 2 Output: " + result2);

        // Additional demonstration: empty tree
        Integer[] example3 = {};
        TreeNode root3 = solution.buildTreeFromLevelOrder(example3);
        int result3 = solution.countMixedNeighborDesks(root3);
        System.out.println("Empty Tree Output: " + result3);

        // Additional demonstration: single node tree
        Integer[] example4 = {42};
        TreeNode root4 = solution.buildTreeFromLevelOrder(example4);
        int result4 = solution.countMixedNeighborDesks(root4);
        System.out.println("Single Node Output: " + result4);
    }
}