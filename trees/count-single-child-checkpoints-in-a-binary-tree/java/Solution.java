import java.util.*;

/*
 * Title: Count Single-Child Checkpoints in a Binary Tree
 * Difficulty: Easy
 * Topic: Trees
 *
 * Problem Description:
 * You are given the root of a binary tree representing checkpoints in a monitoring system.
 * Each node stores an integer checkpoint ID. A checkpoint is called a single-child checkpoint
 * if it has exactly one direct child: either a left child or a right child, but not both.
 *
 * Your task is to return the number of single-child checkpoints in the tree.
 *
 * This is a structural tree problem: the checkpoint values themselves do not affect the answer.
 * You only need to examine whether each node has zero, one, or two children. A leaf node has
 * no children, so it does not count. A node with both left and right children also does not count.
 *
 * The tree can be empty. In that case, the answer is 0.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [0, 1000].
 * - Node values are in the range [-10^4, 10^4].
 * - The tree is a standard binary tree and is not necessarily balanced or complete.
 *
 * Example 1:
 * Input: root = [5,3,8,1,null,null,9]
 * Output: 2
 * Explanation: Node 3 has only a left child (1), and node 8 has only a right child (9).
 * No other node has exactly one child.
 *
 * Example 2:
 * Input: root = [10,4,12,2,6,null,null]
 * Output: 0
 * Explanation: Node 10 has two children, node 4 has two children, and nodes 2, 6, and 12
 * are leaves. Therefore, there are no single-child checkpoints.
 *
 * You may solve this using either depth-first search or breadth-first search by visiting every
 * node once and counting the nodes whose number of children is exactly one.
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
    }

    /**
     * Counts how many nodes in the binary tree have exactly one child.
     *
     * This method uses a recursive depth-first traversal:
     * 1. Visit the current node.
     * 2. Check whether it has exactly one child.
     * 3. Recursively count in the left subtree.
     * 4. Recursively count in the right subtree.
     * 5. Add everything together.
     *
     * @param root the root of the binary tree; may be null for an empty tree
     * @return the number of nodes that have exactly one direct child
     *
     * Time complexity: O(n), where n is the number of nodes in the tree,
     * because every node is visited exactly once.
     * Space complexity: O(h), where h is the height of the tree due to recursion stack;
     * in the worst case, this can be O(n) for a skewed tree.
     */
    public int countSingleChildNodes(TreeNode root) {
        // Base case:
        // If the current node is null, there is no node here,
        // so it contributes 0 to the count.
        if (root == null) {
            return 0;
        }

        // Determine whether the current node has exactly one child.
        //
        // A node has exactly one child if:
        // - left is not null and right is null
        // OR
        // - left is null and right is not null
        //
        // This is exactly the logical XOR condition for "one but not both".
        int currentNodeCount = 0;
        if ((root.left == null && root.right != null) || (root.left != null && root.right == null)) {
            currentNodeCount = 1;
        }

        // Recursively count single-child nodes in the left subtree.
        int leftCount = countSingleChildNodes(root.left);

        // Recursively count single-child nodes in the right subtree.
        int rightCount = countSingleChildNodes(root.right);

        // Total count is:
        // count from current node
        // + count from left subtree
        // + count from right subtree
        return currentNodeCount + leftCount + rightCount;
    }

    /**
     * Counts how many nodes in the binary tree have exactly one child using
     * an iterative breadth-first search (level-order traversal).
     *
     * This method is included as an alternative beginner-friendly approach:
     * 1. If the tree is empty, return 0.
     * 2. Put the root into a queue.
     * 3. Repeatedly remove one node from the queue.
     * 4. Check whether that node has exactly one child.
     * 5. Add its non-null children to the queue.
     *
     * @param root the root of the binary tree; may be null for an empty tree
     * @return the number of nodes that have exactly one direct child
     *
     * Time complexity: O(n), where n is the number of nodes in the tree,
     * because each node is processed exactly once.
     * Space complexity: O(w), where w is the maximum width of the tree,
     * because the queue may hold up to one full level of nodes.
     */
    public int countSingleChildNodesBFS(TreeNode root) {
        // If the tree is empty, there are no nodes to inspect.
        if (root == null) {
            return 0;
        }

        // This variable will store the final answer.
        int count = 0;

        // Queue for standard breadth-first traversal.
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        // Continue until all nodes have been processed.
        while (!queue.isEmpty()) {
            // Remove the next node from the front of the queue.
            TreeNode current = queue.poll();

            // Check whether the current node has exactly one child.
            //
            // Case 1: only left child exists
            // Case 2: only right child exists
            //
            // If either case is true, increment the answer.
            if ((current.left == null && current.right != null) ||
                (current.left != null && current.right == null)) {
                count++;
            }

            // If the left child exists, add it to the queue
            // so it will also be processed later.
            if (current.left != null) {
                queue.offer(current.left);
            }

            // If the right child exists, add it to the queue
            // so it will also be processed later.
            if (current.right != null) {
                queue.offer(current.right);
            }
        }

        // After visiting every node, return the total count.
        return count;
    }

    /**
     * Builds a binary tree from a level-order array representation where null values
     * indicate missing nodes.
     *
     * Example:
     * [5, 3, 8, 1, null, null, 9]
     *
     * Means:
     *         5
     *       /   \
     *      3     8
     *     /       \
     *    1         9
     *
     * @param values the level-order representation of the tree, using null for missing nodes
     * @return the root of the constructed binary tree, or null if the input is empty or starts with null
     *
     * Time complexity: O(n), where n is the number of entries in the input array,
     * because each entry is processed at most once.
     * Space complexity: O(n), due to the queue and created nodes.
     */
    public TreeNode buildTree(Integer[] values) {
        // If the input array is empty, there is no tree to build.
        if (values == null || values.length == 0) {
            return null;
        }

        // If the first value is null, the root does not exist,
        // so the tree is empty.
        if (values[0] == null) {
            return null;
        }

        // Create the root node from the first array element.
        TreeNode root = new TreeNode(values[0]);

        // Queue used to assign children level by level.
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        // Index points to the next value in the array that has not yet been used.
        int index = 1;

        // Process nodes in level order until we run out of parents or input values.
        while (!queue.isEmpty() && index < values.length) {
            // Take the next parent node from the queue.
            TreeNode current = queue.poll();

            // Try to assign the left child if there is still an unused value.
            if (index < values.length) {
                Integer leftValue = values[index];
                index++;

                // Only create a node if the value is not null.
                if (leftValue != null) {
                    current.left = new TreeNode(leftValue);
                    queue.offer(current.left);
                }
            }

            // Try to assign the right child if there is still an unused value.
            if (index < values.length) {
                Integer rightValue = values[index];
                index++;

                // Only create a node if the value is not null.
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
     * @param args command-line arguments; not used in this program
     * @return nothing
     *
     * Time complexity: O(n) overall for each demonstrated tree build and traversal.
     * Space complexity: O(n) overall for tree construction and traversal support structures.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Input: [5,3,8,1,null,null,9]
        // Tree structure:
        //         5
        //       /   \
        //      3     8
        //     /       \
        //    1         9
        //
        // Node 3 has exactly one child (left child 1).
        // Node 8 has exactly one child (right child 9).
        // Therefore, expected answer = 2.
        Integer[] example1 = {5, 3, 8, 1, null, null, 9};
        TreeNode root1 = solution.buildTree(example1);
        int result1 = solution.countSingleChildNodes(root1);
        System.out.println("Example 1 Output: " + result1);

        // Example 2:
        // Input: [10,4,12,2,6,null,null]
        // Tree structure:
        //         10
        //       /    \
        //      4      12
        //     / \
        //    2   6
        //
        // Node 10 has two children -> does not count.
        // Node 4 has two children -> does not count.
        // Nodes 2, 6, 12 are leaves -> do not count.
        // Therefore, expected answer = 0.
        Integer[] example2 = {10, 4, 12, 2, 6, null, null};
        TreeNode root2 = solution.buildTree(example2);
        int result2 = solution.countSingleChildNodes(root2);
        System.out.println("Example 2 Output: " + result2);

        // Additional demonstration: empty tree
        // Expected answer = 0.
        Integer[] example3 = {};
        TreeNode root3 = solution.buildTree(example3);
        int result3 = solution.countSingleChildNodes(root3);
        System.out.println("Empty Tree Output: " + result3);

        // Additional demonstration using the BFS version on Example 1
        // to show that both implementations produce the same result.
        int bfsResult1 = solution.countSingleChildNodesBFS(root1);
        System.out.println("Example 1 Output using BFS: " + bfsResult1);
    }
}