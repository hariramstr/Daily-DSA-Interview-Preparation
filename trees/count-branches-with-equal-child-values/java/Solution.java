import java.util.*;

/*
 * Title: Count Branches With Equal Child Values
 * Difficulty: Easy
 * Topic: Trees
 *
 * Problem Description:
 * You are given the root of a binary tree representing a simple rule hierarchy.
 * Each node stores an integer value. A node is called a balanced branch if it
 * has both a left child and a right child, and the values of those two children
 * are exactly the same. The value of the current node does not matter for this check.
 *
 * Your task is to return the number of balanced branch nodes in the tree.
 *
 * This is a structural tree traversal problem. You should inspect every node once
 * and count how many nodes satisfy the condition. Nodes with only one child or
 * no children are not counted.
 *
 * Implement a function that takes the root of the binary tree and returns an integer count.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [0, 1000].
 * - Node values are in the range [-1000, 1000].
 * - The tree is a binary tree.
 *
 * Example 1:
 * Input: root = [8,4,4,3,3,null,3]
 * Output: 2
 * Explanation:
 * The root node has left child value 4 and right child value 4, so it is a balanced branch.
 * The node with value 4 on the left has children 3 and 3, so it is also a balanced branch.
 * The other nodes do not have two children with equal values.
 *
 * Example 2:
 * Input: root = [5,2,7,2,null,7,7]
 * Output: 1
 * Explanation:
 * The root is not counted because its child values are 2 and 7.
 * The left child of the root has only one child configuration that does not satisfy the rule.
 * The node with value 7 on the right has two children with values 7 and 7, so exactly one balanced branch exists.
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
         * @param val the integer value stored in the node
         */
        TreeNode(int val) {
            this.val = val;
        }
    }

    /**
     * Counts how many nodes in the binary tree have both a left child and a right child,
     * and those two child values are equal.
     *
     * The algorithm performs a depth-first traversal and checks each node exactly once.
     *
     * @param root the root of the binary tree; may be null for an empty tree
     * @return the number of balanced branch nodes in the tree
     *
     * Time complexity: O(n), where n is the number of nodes in the tree,
     * because each node is visited exactly once.
     * Space complexity: O(h), where h is the height of the tree due to recursion stack;
     * in the worst case this can be O(n) for a skewed tree.
     */
    public int countBalancedBranches(TreeNode root) {
        return dfsCount(root);
    }

    /**
     * Recursively traverses the tree and returns the number of balanced branch nodes
     * found in the subtree rooted at the given node.
     *
     * @param node the current node being processed
     * @return the number of balanced branch nodes in this subtree
     *
     * Time complexity: O(n), across the full traversal, because each node is processed once.
     * Space complexity: O(h), where h is the recursion depth / tree height.
     */
    private int dfsCount(TreeNode node) {
        // Step 1:
        // If the current node is null, there is no subtree here.
        // That means there are zero balanced branches to count.
        if (node == null) {
            return 0;
        }

        // Step 2:
        // Recursively count balanced branches in the left subtree.
        int leftCount = dfsCount(node.left);

        // Step 3:
        // Recursively count balanced branches in the right subtree.
        int rightCount = dfsCount(node.right);

        // Step 4:
        // Determine whether the CURRENT node itself is a balanced branch.
        //
        // A node qualifies only if:
        //   1) it has a left child
        //   2) it has a right child
        //   3) the left child's value equals the right child's value
        //
        // Important:
        // The current node's own value does NOT matter.
        int currentNodeCount = 0;
        if (node.left != null && node.right != null && node.left.val == node.right.val) {
            currentNodeCount = 1;
        }

        // Step 5:
        // The total count for this subtree is:
        //   balanced branches in left subtree
        // + balanced branches in right subtree
        // + 1 if current node qualifies, otherwise +0
        return leftCount + rightCount + currentNodeCount;
    }

    /**
     * Builds a binary tree from a level-order array representation where null means
     * "no node at this position".
     *
     * Example:
     * [8, 4, 4, 3, 3, null, 3]
     *
     * This helper is used only for demonstration in main.
     *
     * @param values the level-order representation of the tree; null entries indicate missing nodes
     * @return the root of the constructed binary tree, or null if the input is empty or starts with null
     *
     * Time complexity: O(n), where n is the number of entries in the input array.
     * Space complexity: O(n), due to the queue used during construction.
     */
    public TreeNode buildTree(Integer[] values) {
        // If the array is empty, there is no tree to build.
        if (values == null || values.length == 0) {
            return null;
        }

        // If the first value is null, the root itself does not exist.
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
            // Take the next parent node whose children we want to assign.
            TreeNode current = queue.poll();

            // Try to assign the left child if there is still an available value.
            if (index < values.length) {
                Integer leftValue = values[index];
                index++;

                // Only create a child node if the value is not null.
                if (leftValue != null) {
                    current.left = new TreeNode(leftValue);
                    queue.offer(current.left);
                }
            }

            // Try to assign the right child if there is still an available value.
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
     * @param args command-line arguments; not used
     *
     * @return nothing
     *
     * Time complexity: O(n) per demonstrated test case for tree construction and counting.
     * Space complexity: O(n) for tree construction plus traversal stack/queue usage.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Tree: [8,4,4,3,3,null,3]
        //
        // Structure:
        //         8
        //       /   \
        //      4     4
        //     / \     \
        //    3   3     3
        //
        // Check nodes:
        // - Node 8: children are 4 and 4 -> equal -> count this node
        // - Left node 4: children are 3 and 3 -> equal -> count this node
        // - Right node 4: only one child -> do not count
        // - Leaves: no children -> do not count
        //
        // Expected answer: 2
        Integer[] example1 = {8, 4, 4, 3, 3, null, 3};
        TreeNode root1 = solution.buildTree(example1);
        int result1 = solution.countBalancedBranches(root1);
        System.out.println("Example 1 Output: " + result1); // Expected: 2

        // Example 2:
        // Tree: [5,2,7,2,null,7,7]
        //
        // Structure:
        //         5
        //       /   \
        //      2     7
        //     /     / \
        //    2     7   7
        //
        // Check nodes:
        // - Node 5: children are 2 and 7 -> not equal -> do not count
        // - Node 2: only one child -> do not count
        // - Node 7: children are 7 and 7 -> equal -> count this node
        // - Leaves: no children -> do not count
        //
        // Expected answer: 1
        Integer[] example2 = {5, 2, 7, 2, null, 7, 7};
        TreeNode root2 = solution.buildTree(example2);
        int result2 = solution.countBalancedBranches(root2);
        System.out.println("Example 2 Output: " + result2); // Expected: 1

        // Additional quick sanity checks for beginners:

        // Empty tree -> no nodes -> answer should be 0
        Integer[] example3 = {};
        TreeNode root3 = solution.buildTree(example3);
        int result3 = solution.countBalancedBranches(root3);
        System.out.println("Empty Tree Output: " + result3); // Expected: 0

        // Single node tree -> no children -> answer should be 0
        Integer[] example4 = {10};
        TreeNode root4 = solution.buildTree(example4);
        int result4 = solution.countBalancedBranches(root4);
        System.out.println("Single Node Output: " + result4); // Expected: 0
    }
}