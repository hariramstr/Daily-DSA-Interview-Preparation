import java.util.*;

/*
Problem Title: Count Full Managers in an Org Tree

Problem Description:
A company stores its reporting structure as a binary tree. Each node represents one employee,
and the left and right child pointers represent up to two direct reports. A manager is called
full if they have exactly two direct reports. Employees with zero or one direct report are not
considered full managers.

Given the root of the org tree, return the number of full managers in the company.

This is a basic tree traversal problem. You may solve it using either depth-first search
(recursive or iterative) or breadth-first search. The tree can be empty, in which case
the answer is 0.

Constraints:
- The number of nodes in the tree is in the range [0, 1000].
- Node values are integers in the range [-10^4, 10^4].
- All node values are irrelevant to the counting logic; only the tree structure matters.

Example 1:
Input: root = [10,5,20,3,7,null,30]
Output: 2
Explanation: Employee 10 has two direct reports (5 and 20), and employee 5 has two direct
reports (3 and 7). Employee 20 has only one direct report (30). So the total number of
full managers is 2.

Example 2:
Input: root = [1,2,3,4,null,null,null]
Output: 1
Explanation: Employee 1 has exactly two direct reports, so they count as a full manager.
Employee 2 has only one direct report, and all other nodes have none. Therefore the answer is 1.
*/

public class Solution {

    /**
     * Basic binary tree node used to represent an employee in the org tree.
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
    }

    /**
     * Counts how many nodes in the binary tree have exactly two children.
     *
     * This method uses a recursive depth-first traversal:
     * 1. If the current node is null, there is nothing to count.
     * 2. Check whether the current node has both a left child and a right child.
     * 3. Recursively count full managers in the left subtree.
     * 4. Recursively count full managers in the right subtree.
     * 5. Return the total.
     *
     * @param root the root of the org tree; may be null for an empty tree
     * @return the number of full managers (nodes with exactly two direct reports)
     *
     * Time complexity: O(n), where n is the number of nodes in the tree,
     * because every node is visited exactly once.
     * Space complexity: O(h), where h is the height of the tree due to recursion stack;
     * in the worst case this can be O(n), and in a balanced tree it is O(log n).
     */
    public int countFullManagers(TreeNode root) {
        // Base case:
        // If the current node does not exist, then this subtree contributes 0 full managers.
        if (root == null) {
            return 0;
        }

        // Determine whether the current employee is a full manager.
        // A full manager must have BOTH a left direct report and a right direct report.
        int currentCount = 0;
        if (root.left != null && root.right != null) {
            currentCount = 1;
        }

        // Recursively count full managers in the left subtree.
        int leftCount = countFullManagers(root.left);

        // Recursively count full managers in the right subtree.
        int rightCount = countFullManagers(root.right);

        // The total for this subtree is:
        // current node's contribution + left subtree contribution + right subtree contribution.
        return currentCount + leftCount + rightCount;
    }

    /**
     * Counts how many nodes in the binary tree have exactly two children using
     * an iterative breadth-first search.
     *
     * This method is included as an alternative beginner-friendly approach:
     * 1. If the tree is empty, return 0.
     * 2. Use a queue to process nodes level by level.
     * 3. For each node, check whether both children exist.
     * 4. Add non-null children to the queue.
     * 5. Return the final count.
     *
     * @param root the root of the org tree; may be null for an empty tree
     * @return the number of full managers (nodes with exactly two direct reports)
     *
     * Time complexity: O(n), where n is the number of nodes in the tree,
     * because each node is enqueued and dequeued once.
     * Space complexity: O(w), where w is the maximum width of the tree;
     * in the worst case this can be O(n).
     */
    public int countFullManagersBFS(TreeNode root) {
        // If the tree is empty, there are no employees and therefore no full managers.
        if (root == null) {
            return 0;
        }

        // This variable will store the final answer.
        int fullManagers = 0;

        // Queue for level-order traversal.
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        // Continue until all reachable nodes have been processed.
        while (!queue.isEmpty()) {
            // Remove the next node from the queue.
            TreeNode current = queue.poll();

            // Check whether this employee has exactly two direct reports.
            if (current.left != null && current.right != null) {
                fullManagers++;
            }

            // If the left child exists, it should also be processed later.
            if (current.left != null) {
                queue.offer(current.left);
            }

            // If the right child exists, it should also be processed later.
            if (current.right != null) {
                queue.offer(current.right);
            }
        }

        // Return the total number of full managers found.
        return fullManagers;
    }

    /**
     * Builds a binary tree from a level-order array representation where null values
     * indicate missing nodes.
     *
     * Example:
     * [10, 5, 20, 3, 7, null, 30]
     *
     * Construction logic:
     * - The first element is the root.
     * - For each node taken from the queue, assign the next array value as its left child
     *   and the following array value as its right child, skipping nulls.
     *
     * @param values the level-order representation of the tree; may be null or empty
     * @return the root of the constructed binary tree, or null if the input is empty
     *
     * Time complexity: O(n), where n is the number of elements in the input array.
     * Space complexity: O(n), due to the queue and created nodes.
     */
    public TreeNode buildTree(Integer[] values) {
        // If the input array is null, empty, or starts with null,
        // then there is no valid root node to create.
        if (values == null || values.length == 0 || values[0] == null) {
            return null;
        }

        // Create the root from the first element.
        TreeNode root = new TreeNode(values[0]);

        // Queue used to attach children in level-order.
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        // Index points to the next value in the array that has not yet been used.
        int index = 1;

        // Process nodes in the queue until we run out of nodes or input values.
        while (!queue.isEmpty() && index < values.length) {
            TreeNode current = queue.poll();

            // Try to assign the left child if there is still an unused value.
            if (index < values.length) {
                Integer leftValue = values[index];
                index++;

                // Only create a node when the value is not null.
                if (leftValue != null) {
                    current.left = new TreeNode(leftValue);
                    queue.offer(current.left);
                }
            }

            // Try to assign the right child if there is still an unused value.
            if (index < values.length) {
                Integer rightValue = values[index];
                index++;

                // Only create a node when the value is not null.
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
     * It builds the trees, runs the counting algorithm, and prints the results.
     *
     * @param args command-line arguments; not used
     * @return nothing
     *
     * Time complexity: O(n) per demonstrated example for building and traversing the tree.
     * Space complexity: O(n) for tree construction and traversal support structures.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Tree: [10,5,20,3,7,null,30]
        // Structure:
        //         10
        //        /  \
        //       5    20
        //      / \     \
        //     3   7     30
        //
        // Full managers:
        // - 10 has two children -> count
        // - 5 has two children -> count
        // - 20 has only one child -> do not count
        // Expected answer: 2
        Integer[] example1 = {10, 5, 20, 3, 7, null, 30};
        TreeNode root1 = solution.buildTree(example1);
        int result1 = solution.countFullManagers(root1);
        System.out.println("Example 1 Output: " + result1);

        // Example 2:
        // Tree: [1,2,3,4,null,null,null]
        // Structure:
        //       1
        //      / \
        //     2   3
        //    /
        //   4
        //
        // Full managers:
        // - 1 has two children -> count
        // - 2 has only one child -> do not count
        // - 3 and 4 have no children -> do not count
        // Expected answer: 1
        Integer[] example2 = {1, 2, 3, 4, null, null, null};
        TreeNode root2 = solution.buildTree(example2);
        int result2 = solution.countFullManagers(root2);
        System.out.println("Example 2 Output: " + result2);

        // Additional demonstration: empty tree
        // Expected answer: 0
        Integer[] example3 = {};
        TreeNode root3 = solution.buildTree(example3);
        int result3 = solution.countFullManagers(root3);
        System.out.println("Empty Tree Output: " + result3);

        // Optional demonstration using the BFS version on Example 1
        int bfsResult1 = solution.countFullManagersBFS(root1);
        System.out.println("Example 1 Output using BFS: " + bfsResult1);
    }
}