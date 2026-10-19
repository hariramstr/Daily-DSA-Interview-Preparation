import java.util.*;

/*
 * Title: Count Branches With Missing Siblings
 * Difficulty: Easy
 * Topic: Trees
 *
 * Problem Description:
 * You are given the root of a binary tree that represents a simple organizational chart.
 * Each node stores an integer ID, but the ID values do not affect the answer.
 * Your task is to count how many nodes in the tree have exactly one child.
 *
 * In other words, count every node where either:
 * - the left child exists and the right child does not, or
 * - the right child exists and the left child does not.
 *
 * Leaf nodes do not count, because they have zero children.
 * Nodes with two children also do not count.
 * Only nodes with exactly one child should be included in the result.
 *
 * Return the total number of such nodes.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [0, 1000].
 * - -10^4 <= Node.val <= 10^4
 * - The tree is a binary tree.
 *
 * Example 1:
 * Input: root = [5,3,8,1,null,null,9]
 * Output: 2
 * Explanation:
 * Node 3 has only one child (1), and node 8 has only one child (9).
 * No other node has exactly one child.
 *
 * Example 2:
 * Input: root = [7,4,10,2,6,null,null]
 * Output: 0
 * Explanation:
 * Node 4 has two children, node 10 has no children, and nodes 2 and 6 are leaves.
 * There are no nodes with exactly one child.
 *
 * A correct solution should visit each node once and count the nodes
 * that satisfy the condition.
 */

public class Solution {

    /**
     * Basic binary tree node definition used by the solution.
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
     * Counts how many nodes in the binary tree have exactly one child.
     *
     * This method uses depth-first search (DFS) recursion.
     * For each node, it checks whether exactly one of its two child references is non-null.
     * If yes, that node contributes 1 to the answer.
     * Then it recursively counts the qualifying nodes in the left subtree
     * and in the right subtree, and adds everything together.
     *
     * @param root the root of the binary tree; may be null for an empty tree
     * @return the total number of nodes that have exactly one child
     *
     * Time complexity: O(n), where n is the number of nodes in the tree,
     * because each node is visited exactly once.
     * Space complexity: O(h), where h is the height of the tree,
     * due to the recursion call stack. In the worst case, this can be O(n).
     */
    public int countNodesWithExactlyOneChild(TreeNode root) {
        // Base case:
        // If the current node is null, there is no node here,
        // so it cannot contribute anything to the count.
        if (root == null) {
            return 0;
        }

        // We will compute whether the current node has exactly one child.
        // A node has exactly one child if:
        // - left is not null and right is null, OR
        // - left is null and right is not null
        //
        // The XOR-like condition below captures that idea clearly.
        int currentNodeContribution = 0;
        if ((root.left == null && root.right != null) || (root.left != null && root.right == null)) {
            currentNodeContribution = 1;
        }

        // Recursively count qualifying nodes in the left subtree.
        int leftCount = countNodesWithExactlyOneChild(root.left);

        // Recursively count qualifying nodes in the right subtree.
        int rightCount = countNodesWithExactlyOneChild(root.right);

        // The total for this subtree is:
        // contribution from current node
        // + contribution from left subtree
        // + contribution from right subtree
        return currentNodeContribution + leftCount + rightCount;
    }

    /**
     * Counts how many nodes in the binary tree have exactly one child
     * using an iterative breadth-first search (BFS).
     *
     * This method is included as an alternative beginner-friendly approach.
     * It uses a queue to visit nodes level by level.
     * For each visited node, it checks whether exactly one child exists.
     * Then it adds any non-null children to the queue for later processing.
     *
     * @param root the root of the binary tree; may be null for an empty tree
     * @return the total number of nodes that have exactly one child
     *
     * Time complexity: O(n), where n is the number of nodes in the tree,
     * because each node is enqueued and dequeued once.
     * Space complexity: O(w), where w is the maximum width of the tree,
     * because the queue may hold many nodes from one level at the same time.
     */
    public int countNodesWithExactlyOneChildBFS(TreeNode root) {
        // If the tree is empty, there are no nodes to inspect.
        if (root == null) {
            return 0;
        }

        // This variable will store the final answer.
        int count = 0;

        // Standard queue for BFS traversal.
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        // Continue until every reachable node has been processed.
        while (!queue.isEmpty()) {
            // Remove the next node from the front of the queue.
            TreeNode current = queue.poll();

            // Check whether the current node has exactly one child.
            // If exactly one child exists, increment the count.
            boolean hasExactlyOneChild =
                    (current.left == null && current.right != null) ||
                    (current.left != null && current.right == null);

            if (hasExactlyOneChild) {
                count++;
            }

            // If the left child exists, add it to the queue
            // so we can inspect it later.
            if (current.left != null) {
                queue.offer(current.left);
            }

            // If the right child exists, add it to the queue
            // so we can inspect it later.
            if (current.right != null) {
                queue.offer(current.right);
            }
        }

        // After BFS completes, count contains the number of nodes
        // with exactly one child.
        return count;
    }

    /**
     * Builds a binary tree from a level-order array representation where null values
     * indicate missing nodes.
     *
     * Example:
     * [5, 3, 8, 1, null, null, 9]
     *
     * means:
     *         5
     *       /   \
     *      3     8
     *     /       \
     *    1         9
     *
     * This helper is used only for demonstration in main.
     *
     * @param values the level-order representation of the tree, where null means no node
     * @return the root of the constructed binary tree, or null if the input is empty
     *
     * Time complexity: O(n), where n is the number of array entries,
     * because each entry is processed at most once.
     * Space complexity: O(n), due to the queue and created nodes.
     */
    public TreeNode buildTreeFromLevelOrder(Integer[] values) {
        // If the input array is empty, there is no tree to build.
        if (values == null || values.length == 0) {
            return null;
        }

        // If the first value is null, the root does not exist,
        // so the entire tree is empty.
        if (values[0] == null) {
            return null;
        }

        // Create the root node from the first array element.
        TreeNode root = new TreeNode(values[0]);

        // Queue used to attach children level by level.
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        // Index points to the next array element that should be used
        // as a child of some node currently in the queue.
        int index = 1;

        // Process nodes in level order until we run out of nodes or values.
        while (!queue.isEmpty() && index < values.length) {
            TreeNode current = queue.poll();

            // Try to assign the left child if there is still an unused value.
            if (index < values.length) {
                Integer leftValue = values[index];
                index++;

                // Only create a node if the array entry is not null.
                if (leftValue != null) {
                    current.left = new TreeNode(leftValue);
                    queue.offer(current.left);
                }
            }

            // Try to assign the right child if there is still an unused value.
            if (index < values.length) {
                Integer rightValue = values[index];
                index++;

                // Only create a node if the array entry is not null.
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
     * The printed outputs are verified to match the examples:
     * - Example 1 should print 2
     * - Example 2 should print 0
     *
     * @param args command-line arguments; not used
     * @return nothing
     *
     * Time complexity: O(n) per demonstration tree, because each tree is built
     * and traversed once.
     * Space complexity: O(n), due to tree construction and traversal support structures.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Input: [5,3,8,1,null,null,9]
        //
        // Tree structure:
        //         5
        //       /   \
        //      3     8
        //     /       \
        //    1         9
        //
        // Node 3 has exactly one child: left child 1
        // Node 8 has exactly one child: right child 9
        // Node 5 has two children -> does not count
        // Nodes 1 and 9 are leaves -> do not count
        //
        // Expected answer: 2
        Integer[] example1 = {5, 3, 8, 1, null, null, 9};
        TreeNode root1 = solution.buildTreeFromLevelOrder(example1);
        int result1 = solution.countNodesWithExactlyOneChild(root1);
        System.out.println("Example 1 Output: " + result1);

        // Example 2:
        // Input: [7,4,10,2,6,null,null]
        //
        // Tree structure:
        //         7
        //       /   \
        //      4     10
        //     / \
        //    2   6
        //
        // Node 7 has two children -> does not count
        // Node 4 has two children -> does not count
        // Node 10 is a leaf -> does not count
        // Nodes 2 and 6 are leaves -> do not count
        //
        // Expected answer: 0
        Integer[] example2 = {7, 4, 10, 2, 6, null, null};
        TreeNode root2 = solution.buildTreeFromLevelOrder(example2);
        int result2 = solution.countNodesWithExactlyOneChild(root2);
        System.out.println("Example 2 Output: " + result2);

        // Optional extra demonstration using the BFS version on the same examples
        // to show that both implementations produce the same correct results.
        System.out.println("Example 1 Output using BFS: " + solution.countNodesWithExactlyOneChildBFS(root1));
        System.out.println("Example 2 Output using BFS: " + solution.countNodesWithExactlyOneChildBFS(root2));
    }
}