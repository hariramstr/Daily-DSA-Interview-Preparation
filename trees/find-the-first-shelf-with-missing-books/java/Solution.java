import java.util.*;

/*
Problem Title: Find the First Shelf With Missing Books

Problem Description:
You are given the root of a binary tree representing shelves in a library. Each node stores an integer shelf ID.
A shelf is considered complete if it has either 0 children or exactly 2 children.
A shelf is considered missing books if it has exactly one child, meaning one side of the shelf structure exists
and the other side is empty.

Return the shelf ID of the first shelf with missing books when the tree is scanned level by level from top to bottom,
and from left to right within each level. If no such shelf exists, return -1.

In other words, you must perform a breadth-first search and find the earliest node that has only one child.
This problem is useful for checking whether a tree contains an incomplete branch near the top, which is often how
tree validation questions appear in interviews.

Constraints:
- The number of nodes in the tree is in the range [0, 1000].
- Node values are in the range [1, 100000].
- All shelf IDs are unique.
- The input tree is a binary tree and may be empty.

Example 1:
Input: root = [10,5,20,3,null,15,30]
Output: 5
Explanation: Shelf 5 has a left child (3) but no right child. During level-order traversal, it is the first node
with exactly one child.

Example 2:
Input: root = [8,4,12,2,6,10,14]
Output: -1
Explanation: Every non-leaf shelf has either two children or none, so there is no shelf with exactly one child.
*/

public class Solution {

    /**
     * Basic binary tree node definition used for the problem.
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
     * Finds the value of the first node that has exactly one child when the tree is traversed
     * in level-order (breadth-first search), from top to bottom and left to right.
     *
     * @param root the root of the binary tree; may be null for an empty tree
     * @return the value of the first node with exactly one child, or -1 if no such node exists
     * Time complexity: O(n), where n is the number of nodes in the tree, because each node is visited at most once.
     * Space complexity: O(w), where w is the maximum number of nodes stored in the queue at one time
     * (the maximum width of the tree); in the worst case this can be O(n).
     */
    public int findFirstShelfWithMissingBooks(TreeNode root) {
        // If the tree is empty, there are no shelves at all.
        // Therefore, there cannot be any shelf with exactly one child.
        if (root == null) {
            return -1;
        }

        // We use a queue to perform Breadth-First Search (BFS).
        // BFS is the correct traversal strategy here because the problem explicitly asks us
        // to scan the tree level by level, from left to right within each level.
        Queue<TreeNode> queue = new LinkedList<>();

        // Start BFS by placing the root into the queue.
        queue.offer(root);

        // Continue processing until there are no more nodes left to examine.
        while (!queue.isEmpty()) {
            // Remove the front node from the queue.
            // This is the next node in level-order traversal.
            TreeNode current = queue.poll();

            // Determine whether the current node has a left child.
            boolean hasLeftChild = current.left != null;

            // Determine whether the current node has a right child.
            boolean hasRightChild = current.right != null;

            // A node has exactly one child if:
            // - it has a left child but no right child, OR
            // - it has a right child but no left child.
            //
            // This is exactly the "missing books" condition described in the problem.
            if (hasLeftChild ^ hasRightChild) {
                // Because BFS visits nodes in the required order,
                // the first such node we encounter is the correct answer.
                return current.val;
            }

            // If the left child exists, add it to the queue so it will be processed later.
            if (hasLeftChild) {
                queue.offer(current.left);
            }

            // If the right child exists, add it to the queue so it will be processed later.
            if (hasRightChild) {
                queue.offer(current.right);
            }
        }

        // If we finish the entire BFS without finding any node with exactly one child,
        // then every node had either 0 or 2 children.
        return -1;
    }

    /**
     * Builds a binary tree from a level-order array representation where null values indicate
     * missing nodes.
     *
     * Example:
     * [10, 5, 20, 3, null, 15, 30]
     *
     * This method is included to make the main method easy to understand and beginner-friendly.
     *
     * @param values an array of Integer values in level-order, where null means no node at that position
     * @return the root of the constructed binary tree, or null if the array is null, empty, or starts with null
     * Time complexity: O(n), where n is the number of entries in the input array.
     * Space complexity: O(n), due to the queue used during tree construction.
     */
    public TreeNode buildTree(Integer[] values) {
        // If the input array is null, empty, or begins with null,
        // then the tree is empty.
        if (values == null || values.length == 0 || values[0] == null) {
            return null;
        }

        // Create the root node from the first value.
        TreeNode root = new TreeNode(values[0]);

        // Queue used to assign children level by level.
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        // Index points to the next value in the array that should be used
        // as a child node.
        int index = 1;

        // Process nodes in the queue until we run out of nodes to attach children to,
        // or until we consume all input values.
        while (!queue.isEmpty() && index < values.length) {
            // Take the next parent node from the queue.
            TreeNode current = queue.poll();

            // Try to assign the left child if there is still an available value.
            if (index < values.length) {
                Integer leftValue = values[index];
                index++;

                // If the value is not null, create the left child and enqueue it.
                if (leftValue != null) {
                    current.left = new TreeNode(leftValue);
                    queue.offer(current.left);
                }
            }

            // Try to assign the right child if there is still an available value.
            if (index < values.length) {
                Integer rightValue = values[index];
                index++;

                // If the value is not null, create the right child and enqueue it.
                if (rightValue != null) {
                    current.right = new TreeNode(rightValue);
                    queue.offer(current.right);
                }
            }
        }

        return root;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem description.
     *
     * @param args command-line arguments; not used in this program
     * @return nothing
     * Time complexity: O(n) overall for each demonstrated tree build and search.
     * Space complexity: O(n) overall for each demonstrated tree build and search.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Tree represented by level-order array:
        //         10
        //        /  \
        //       5    20
        //      /    /  \
        //     3    15  30
        //
        // Node 5 has exactly one child (left child 3, no right child),
        // and it is the first such node encountered in BFS order.
        Integer[] example1 = {10, 5, 20, 3, null, 15, 30};
        TreeNode root1 = solution.buildTree(example1);
        int result1 = solution.findFirstShelfWithMissingBooks(root1);
        System.out.println(result1); // Expected: 5

        // Example 2:
        // Tree represented by level-order array:
        //         8
        //       /   \
        //      4     12
        //     / \   /  \
        //    2   6 10  14
        //
        // Every non-leaf node has exactly two children.
        // Therefore, the answer should be -1.
        Integer[] example2 = {8, 4, 12, 2, 6, 10, 14};
        TreeNode root2 = solution.buildTree(example2);
        int result2 = solution.findFirstShelfWithMissingBooks(root2);
        System.out.println(result2); // Expected: -1

        // Additional quick demonstration: empty tree.
        Integer[] example3 = {};
        TreeNode root3 = solution.buildTree(example3);
        int result3 = solution.findFirstShelfWithMissingBooks(root3);
        System.out.println(result3); // Expected: -1
    }
}