import java.util.*;

/*
 * Title: Find the First Unbalanced Review Folder
 * Difficulty: Easy
 * Topic: Trees
 *
 * Problem Description:
 * You are given the root of a binary tree representing a document review folder structure.
 * Each node stores an integer number of pending files in that folder.
 * A folder is considered balanced if the total number of pending files in its left subtree
 * is exactly equal to the total number of pending files in its right subtree.
 * If a child does not exist, its subtree sum is 0.
 *
 * Your task is to return the value stored in the first unbalanced folder found during a
 * level-order traversal (top to bottom, left to right). If every folder is balanced, return -1.
 *
 * The value of a folder is not guaranteed to be unique, so you should return the node's stored
 * integer value exactly as it appears when the first unbalanced node is encountered in level order.
 * To solve this problem efficiently, you will likely need to compute subtree sums and then examine
 * nodes in breadth-first order.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [1, 1000].
 * - Node values are in the range [0, 10^4].
 * - The tree is a binary tree and may be incomplete.
 *
 * Example 1:
 * Input: root = [8,4,4,2,2,2,2]
 * Output: -1
 * Explanation: For every node, the sum of values in the left subtree matches the sum of values
 * in the right subtree.
 *
 * Example 2:
 * Input: root = [10,3,5,1,null,2,1]
 * Output: 10
 * Explanation: The root's left subtree sum is 4 and right subtree sum is 8, so the root is
 * unbalanced. Since level-order traversal visits the root first, the answer is 10.
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
         * Creates a tree node with the given value.
         *
         * @param val the integer value stored in this node
         */
        TreeNode(int val) {
            this.val = val;
        }
    }

    /**
     * Returns the value stored in the first unbalanced node encountered during level-order traversal.
     * A node is balanced when the sum of values in its left subtree equals the sum of values in its
     * right subtree. Missing children contribute a subtree sum of 0.
     *
     * Strategy:
     * 1. First compute the total subtree sum for every node using a post-order traversal.
     * 2. Then perform a breadth-first search (level-order traversal).
     * 3. For each visited node, compare the left subtree sum and right subtree sum.
     * 4. Return the node's value immediately when the first mismatch is found.
     * 5. If no mismatch exists, return -1.
     *
     * @param root the root of the binary tree
     * @return the stored value of the first unbalanced node in level-order, or -1 if all nodes are balanced
     * Time complexity: O(n), where n is the number of nodes in the tree
     * Space complexity: O(n), due to the map storing subtree sums and the recursion/queue usage
     */
    public int findFirstUnbalancedFolder(TreeNode root) {
        // If the tree is empty, there is no unbalanced node.
        // The problem constraints say at least 1 node exists, but handling this makes the method safer.
        if (root == null) {
            return -1;
        }

        // This map will store:
        // key   -> a tree node
        // value -> the total sum of the subtree rooted at that node
        //
        // Example:
        // If a node has value 4, left subtree sum 2, right subtree sum 2,
        // then its stored subtree sum will be 4 + 2 + 2 = 8.
        Map<TreeNode, Integer> subtreeSums = new HashMap<>();

        // Step 1:
        // Compute subtree sums for every node before doing level-order traversal.
        // We do this first because when we inspect a node during BFS,
        // we want to instantly know the sum of its left subtree and right subtree.
        computeSubtreeSums(root, subtreeSums);

        // Step 2:
        // Perform a standard breadth-first traversal using a queue.
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            // Remove the next node in level-order.
            TreeNode current = queue.poll();

            // Determine the left subtree sum.
            // If there is no left child, the left subtree sum is 0.
            int leftSum = current.left == null ? 0 : subtreeSums.get(current.left);

            // Determine the right subtree sum.
            // If there is no right child, the right subtree sum is 0.
            int rightSum = current.right == null ? 0 : subtreeSums.get(current.right);

            // A node is balanced only when left subtree sum == right subtree sum.
            // The moment we find the first node where they differ,
            // we return that node's stored value immediately.
            if (leftSum != rightSum) {
                return current.val;
            }

            // Continue normal BFS order:
            // first enqueue left child, then right child,
            // so traversal remains top-to-bottom, left-to-right.
            if (current.left != null) {
                queue.offer(current.left);
            }
            if (current.right != null) {
                queue.offer(current.right);
            }
        }

        // If BFS finishes without finding any unbalanced node,
        // then every node in the tree is balanced.
        return -1;
    }

    /**
     * Computes the total subtree sum for every node and stores it in the provided map.
     *
     * This is a post-order traversal:
     * - First compute the left subtree sum
     * - Then compute the right subtree sum
     * - Then compute the current node's total subtree sum
     *
     * Why post-order?
     * Because to know the sum of the current node's subtree, we must already know
     * the sums of its left and right subtrees.
     *
     * Formula:
     * subtreeSum(node) = node.val + subtreeSum(node.left) + subtreeSum(node.right)
     *
     * @param node the current node whose subtree sum should be computed
     * @param subtreeSums a map where each node is associated with its total subtree sum
     * @return the total sum of the subtree rooted at the given node
     * Time complexity: O(n), because each node is visited exactly once
     * Space complexity: O(n), for the map and recursion stack in the worst case
     */
    public int computeSubtreeSums(TreeNode node, Map<TreeNode, Integer> subtreeSums) {
        // Base case:
        // A null node contributes 0 to the subtree sum.
        if (node == null) {
            return 0;
        }

        // Recursively compute the total sum of the left subtree.
        int leftSum = computeSubtreeSums(node.left, subtreeSums);

        // Recursively compute the total sum of the right subtree.
        int rightSum = computeSubtreeSums(node.right, subtreeSums);

        // The total subtree sum rooted at this node includes:
        // 1. the node's own value
        // 2. the entire left subtree sum
        // 3. the entire right subtree sum
        int total = node.val + leftSum + rightSum;

        // Store the computed total in the map so it can be reused later in O(1) time.
        subtreeSums.put(node, total);

        // Return the computed total to the caller.
        return total;
    }

    /**
     * Builds a binary tree from a level-order array representation where null means
     * the child is missing.
     *
     * Example:
     * [10, 3, 5, 1, null, 2, 1]
     *
     * Tree:
     *         10
     *        /  \
     *       3    5
     *      /    / \
     *     1    2   1
     *
     * This method is intended for demonstration in main.
     *
     * @param values the level-order array representation of the binary tree
     * @return the root of the constructed binary tree, or null if the input is empty or starts with null
     * Time complexity: O(n), where n is the number of array elements
     * Space complexity: O(n), due to the queue used during construction
     */
    public TreeNode buildTree(Integer[] values) {
        // Handle empty input or invalid root.
        if (values == null || values.length == 0 || values[0] == null) {
            return null;
        }

        // Create the root from the first element.
        TreeNode root = new TreeNode(values[0]);

        // Queue used to assign children level by level.
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        // Index points to the next array element to process.
        int index = 1;

        while (!queue.isEmpty() && index < values.length) {
            // Take the next parent node whose children we need to assign.
            TreeNode current = queue.poll();

            // Assign left child if there is still an element available.
            if (index < values.length) {
                Integer leftValue = values[index++];
                if (leftValue != null) {
                    current.left = new TreeNode(leftValue);
                    queue.offer(current.left);
                }
            }

            // Assign right child if there is still an element available.
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
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> -1
     * Example 2 -> 10
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstrated test case
     * Space complexity: O(n) per demonstrated test case
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Input: [8,4,4,2,2,2,2]
        // Tree:
        //         8
        //       /   \
        //      4     4
        //     / \   / \
        //    2  2  2  2
        //
        // Check:
        // - Each leaf has leftSum=0 and rightSum=0 => balanced
        // - Left child 4 has subtree sums 2 and 2 => balanced
        // - Right child 4 has subtree sums 2 and 2 => balanced
        // - Root 8 has left subtree total 8 and right subtree total 8 => balanced
        // Therefore answer is -1.
        Integer[] example1 = {8, 4, 4, 2, 2, 2, 2};
        TreeNode root1 = solution.buildTree(example1);
        System.out.println(solution.findFirstUnbalancedFolder(root1)); // Expected: -1

        // Example 2:
        // Input: [10,3,5,1,null,2,1]
        // Tree:
        //         10
        //        /  \
        //       3    5
        //      /    / \
        //     1    2   1
        //
        // Root:
        // left subtree sum = 3 + 1 = 4
        // right subtree sum = 5 + 2 + 1 = 8
        // Since 4 != 8, root is unbalanced.
        // Root is visited first in level-order, so answer is 10.
        Integer[] example2 = {10, 3, 5, 1, null, 2, 1};
        TreeNode root2 = solution.buildTree(example2);
        System.out.println(solution.findFirstUnbalancedFolder(root2)); // Expected: 10
    }
}