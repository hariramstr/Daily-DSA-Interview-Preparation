import java.util.*;

/*
 * Title: Find the Shallowest Missing Child in a Binary Tree
 * Difficulty: Easy
 * Topic: Trees
 *
 * Problem Description:
 * You are given the root of a binary tree representing a hierarchy of checkpoints.
 * A checkpoint is considered incomplete if it is missing at least one direct child:
 * its left child, its right child, or both.
 *
 * Your task is to return the value of the incomplete checkpoint that appears at the
 * smallest depth from the root. If multiple incomplete checkpoints exist at the same
 * depth, return the one that appears first from left to right.
 *
 * In other words, scan the tree level by level. The first node you encounter that
 * does not have two children is the answer. If the tree is empty, return -1.
 *
 * This problem is designed to test basic tree traversal skills and understanding of
 * level-order processing. A breadth-first search works naturally because it visits
 * nodes in increasing depth order and preserves left-to-right order within each level.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [0, 1000].
 * - Node values are integers in the range [-10000, 10000].
 * - All node values are unique.
 *
 * Example 1:
 * Input: root = [7,4,9,2,6,null,11]
 * Output: 9
 * Explanation: Node 7 has both children. Node 4 has both children. Node 9 is missing
 * a left child, so it is the first incomplete checkpoint found in level-order traversal.
 *
 * Example 2:
 * Input: root = [5,3,8,1,null,7,10]
 * Output: 3
 * Explanation: At depth 1, node 3 is missing its right child. Node 8 has two children,
 * but node 3 appears first from left to right, so the answer is 3.
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
     * Finds the value of the first incomplete node encountered in level-order traversal.
     * A node is incomplete if it does not have both a left child and a right child.
     *
     * The algorithm uses Breadth-First Search (BFS):
     * 1. If the tree is empty, return -1.
     * 2. Visit nodes level by level using a queue.
     * 3. For each node, immediately check whether it is missing at least one child.
     * 4. The first such node is guaranteed to be:
     *    - at the smallest depth, because BFS processes shallower levels first
     *    - the leftmost among that depth, because children are enqueued left before right
     *
     * @param root the root of the binary tree; may be null
     * @return the value of the shallowest incomplete node, or -1 if the tree is empty
     *
     * Time complexity: O(n), where n is the number of nodes in the tree,
     * because each node is visited at most once.
     *
     * Space complexity: O(w), where w is the maximum number of nodes in the queue
     * at any time (the maximum width of the tree); in the worst case this is O(n).
     */
    public int findShallowestMissingChild(TreeNode root) {
        // If there is no tree at all, there is no checkpoint to inspect.
        // The problem explicitly says to return -1 for an empty tree.
        if (root == null) {
            return -1;
        }

        // We use a queue for Breadth-First Search (level-order traversal).
        // BFS is the perfect fit here because:
        // - it processes nodes by increasing depth
        // - within the same depth, it preserves left-to-right order
        Queue<TreeNode> queue = new LinkedList<>();

        // Start the traversal from the root.
        queue.offer(root);

        // Continue until all reachable nodes have been processed.
        while (!queue.isEmpty()) {
            // Remove the next node from the front of the queue.
            // This is the next node in level-order.
            TreeNode current = queue.poll();

            // Check whether the current node is incomplete.
            // A node is incomplete if:
            // - left child is missing, OR
            // - right child is missing
            //
            // This means the node does NOT have two children.
            //
            // Because BFS visits nodes in the exact order required by the problem,
            // the first incomplete node we find is immediately the correct answer.
            if (current.left == null || current.right == null) {
                return current.val;
            }

            // If we reach this point, the current node has BOTH children.
            // We enqueue them in left-to-right order.
            //
            // This ordering is important:
            // nodes on the next level must be processed from left to right
            // so that ties at the same depth are handled correctly.
            queue.offer(current.left);
            queue.offer(current.right);
        }

        // If we processed the entire tree and never found an incomplete node,
        // then every node had exactly two children.
        //
        // In a finite binary tree, this situation cannot happen unless the tree is empty,
        // because leaf nodes would be missing children. However, returning -1 here makes
        // the method safe and complete for all inputs.
        return -1;
    }

    /**
     * Builds a binary tree from a level-order array representation where null means
     * "no node at this position".
     *
     * Example:
     * [7, 4, 9, 2, 6, null, 11]
     *
     * Construction rules:
     * - The first element is the root.
     * - For each node taken from the queue, assign the next array value as its left child
     *   and the following array value as its right child.
     * - Null values represent missing children and are not added to the queue.
     *
     * @param values the level-order representation of the tree; may be null or empty
     * @return the root of the constructed binary tree, or null if the input is empty
     *
     * Time complexity: O(n), where n is the number of entries in the input array,
     * because each entry is processed at most once.
     *
     * Space complexity: O(n), due to the queue and the created tree nodes.
     */
    public TreeNode buildTree(Integer[] values) {
        // If the array is null, empty, or starts with null,
        // then the tree is empty.
        if (values == null || values.length == 0 || values[0] == null) {
            return null;
        }

        // Create the root from the first value.
        TreeNode root = new TreeNode(values[0]);

        // Queue used to assign children level by level.
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        // Index points to the next value in the array that has not yet been used.
        int index = 1;

        // Process nodes in the same order they appear in level-order.
        while (!queue.isEmpty() && index < values.length) {
            TreeNode current = queue.poll();

            // Try to assign the left child if there is still input remaining.
            if (index < values.length) {
                Integer leftValue = values[index];
                index++;

                // A non-null value means an actual child node exists.
                if (leftValue != null) {
                    current.left = new TreeNode(leftValue);
                    queue.offer(current.left);
                }
            }

            // Try to assign the right child if there is still input remaining.
            if (index < values.length) {
                Integer rightValue = values[index];
                index++;

                // A non-null value means an actual child node exists.
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
     * It builds trees from level-order arrays, runs the algorithm, and prints the results.
     *
     * @param args command-line arguments; not used
     * @return nothing
     *
     * Time complexity: O(n) per demonstrated test case, where n is the number of nodes
     * in that tree, due to tree construction and BFS traversal.
     *
     * Space complexity: O(n) per demonstrated test case for the constructed tree and queue.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Tree: [7,4,9,2,6,null,11]
        //
        // Level-order inspection:
        // - 7 has both children -> continue
        // - 4 has both children -> continue
        // - 9 is missing left child -> answer is 9
        Integer[] example1 = {7, 4, 9, 2, 6, null, 11};
        TreeNode root1 = solution.buildTree(example1);
        int result1 = solution.findShallowestMissingChild(root1);
        System.out.println(result1); // Expected: 9

        // Example 2:
        // Tree: [5,3,8,1,null,7,10]
        //
        // Level-order inspection:
        // - 5 has both children -> continue
        // - 3 is missing right child -> answer is 3
        // - 8 also has two children, but it comes later at the same depth
        Integer[] example2 = {5, 3, 8, 1, null, 7, 10};
        TreeNode root2 = solution.buildTree(example2);
        int result2 = solution.findShallowestMissingChild(root2);
        System.out.println(result2); // Expected: 3

        // Additional demonstration: empty tree
        Integer[] example3 = {};
        TreeNode root3 = solution.buildTree(example3);
        int result3 = solution.findShallowestMissingChild(root3);
        System.out.println(result3); // Expected: -1

        // Additional demonstration: single-node tree
        // The root has no children, so it is immediately incomplete.
        Integer[] example4 = {42};
        TreeNode root4 = solution.buildTree(example4);
        int result4 = solution.findShallowestMissingChild(root4);
        System.out.println(result4); // Expected: 42
    }
}