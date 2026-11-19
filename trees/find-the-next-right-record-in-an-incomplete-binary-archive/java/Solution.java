import java.util.*;

/*
 * Title: Find the Next Right Record in an Incomplete Binary Archive
 * Difficulty: Medium
 * Topic: Trees
 *
 * Problem Description:
 * You are given the root of a binary tree representing a partially filled archive index.
 * Each node stores a unique integer recordId. The tree is not necessarily perfect or complete,
 * and some children may be missing. You are also given an integer target representing the
 * recordId of an existing node in the tree.
 *
 * For the node whose value is target, return the recordId of the nearest node to its right
 * on the same depth level. If no such node exists, return -1.
 *
 * Two nodes are on the same depth level if they have the same distance from the root.
 * The "next right" node is the first node that would appear after the target when scanning
 * that level from left to right.
 *
 * Your solution should work efficiently for large trees. A common interview follow-up is
 * whether you can solve this without storing every level explicitly, although any solution
 * with linear time complexity is acceptable.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [1, 100000]
 * - 1 <= recordId <= 1000000000
 * - All recordId values are unique
 * - target is guaranteed to exist in the tree
 *
 * Example 1:
 * Input: root = [10,5,15,2,7,null,20], target = 5
 * Output: 15
 * Explanation: At depth 1, the nodes are [5, 15]. The node immediately to the right of 5 is 15.
 *
 * Example 2:
 * Input: root = [8,3,11,1,6,9,null,null,null,4,7], target = 6
 * Output: 9
 * Explanation: At depth 2, the nodes from left to right are [1, 6, 9].
 * The next node to the right of 6 is 9. If target were 9 in this tree, the answer would be -1.
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
     * Finds the nearest node to the right of the target node on the same depth level.
     *
     * This method uses a standard breadth-first search (level-order traversal).
     * The key idea is:
     * 1. Process the tree one level at a time.
     * 2. For each level, keep track of how many nodes belong to that level.
     * 3. When the target node is found:
     *    - If it is not the last node in that level, then the next node in the queue
     *      is exactly the nearest node to its right.
     *    - If it is the last node in that level, then there is no node to its right,
     *      so return -1.
     *
     * This is efficient and beginner-friendly, and it works correctly for incomplete trees.
     *
     * @param root the root of the binary tree
     * @param target the value of the existing target node
     * @return the value of the nearest node to the right on the same level, or -1 if none exists
     * Time complexity: O(n), where n is the number of nodes in the tree
     * Space complexity: O(w), where w is the maximum number of nodes in any level of the tree
     */
    public int findNextRightRecord(TreeNode root, int target) {
        // Defensive check.
        // According to the problem, root is non-null and target exists in the tree,
        // but adding this check makes the method safer and easier to understand.
        if (root == null) {
            return -1;
        }

        // We use a queue for level-order traversal (BFS).
        // BFS naturally visits nodes level by level from left to right,
        // which is exactly the order needed by this problem.
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        // Continue until all nodes have been processed.
        while (!queue.isEmpty()) {

            // The current size of the queue tells us how many nodes belong
            // to the current depth level.
            int levelSize = queue.size();

            // Process exactly 'levelSize' nodes so that we stay within one level.
            for (int i = 0; i < levelSize; i++) {

                // Remove the next node from the queue.
                TreeNode current = queue.poll();

                // Check whether this is the target node.
                if (current.val == target) {
                    // If the target is not the last node in this level,
                    // then the very next node in the queue is the answer.
                    if (i < levelSize - 1) {
                        return queue.peek().val;
                    }

                    // Otherwise, the target is the rightmost node in its level.
                    return -1;
                }

                // Add the left child first because level-order traversal
                // must preserve left-to-right ordering within the level.
                if (current.left != null) {
                    queue.offer(current.left);
                }

                // Then add the right child.
                if (current.right != null) {
                    queue.offer(current.right);
                }
            }
        }

        // The problem guarantees that target exists, so this line should never be reached.
        return -1;
    }

    /**
     * Builds a binary tree from a level-order array representation where null means
     * "no node at this position".
     *
     * Example:
     * [10, 5, 15, 2, 7, null, 20]
     *
     * means:
     *         10
     *        /  \
     *       5    15
     *      / \     \
     *     2   7     20
     *
     * This helper is used only for demonstration in main.
     *
     * @param values an array of Integer values in level-order, where null represents a missing node
     * @return the root of the constructed binary tree, or null if the array is empty or starts with null
     * Time complexity: O(n), where n is the number of entries in the array
     * Space complexity: O(n), due to the queue and created nodes
     */
    public TreeNode buildTreeFromLevelOrder(Integer[] values) {
        // If the input is empty or the first value is null,
        // there is no valid tree to build.
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

        // Continue while there are parent nodes waiting in the queue
        // and there are still values left in the input array.
        while (!queue.isEmpty() && index < values.length) {
            TreeNode current = queue.poll();

            // Try to assign the left child.
            if (index < values.length && values[index] != null) {
                current.left = new TreeNode(values[index]);
                queue.offer(current.left);
            }
            index++;

            // Try to assign the right child.
            if (index < values.length && values[index] != null) {
                current.right = new TreeNode(values[index]);
                queue.offer(current.right);
            }
            index++;
        }

        return root;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It builds the sample trees, runs the algorithm, and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) overall for the demonstrated examples
     * Space complexity: O(n) overall for the demonstrated examples
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Tree: [10,5,15,2,7,null,20]
        // Target: 5
        // Level 1 is [5, 15], so the next right of 5 is 15.
        Integer[] example1 = {10, 5, 15, 2, 7, null, 20};
        TreeNode root1 = solution.buildTreeFromLevelOrder(example1);
        int result1 = solution.findNextRightRecord(root1, 5);
        System.out.println(result1); // Expected: 15

        // Example 2:
        // Tree: [8,3,11,1,6,9,null,null,null,4,7]
        // Target: 6
        // At the relevant depth, nodes are [1, 6, 9], so the next right of 6 is 9.
        Integer[] example2 = {8, 3, 11, 1, 6, 9, null, null, null, 4, 7};
        TreeNode root2 = solution.buildTreeFromLevelOrder(example2);
        int result2 = solution.findNextRightRecord(root2, 6);
        System.out.println(result2); // Expected: 9

        // Additional demonstration:
        // Same tree as Example 2, but target = 9.
        // Since 9 is the rightmost node on its level, the answer is -1.
        int result3 = solution.findNextRightRecord(root2, 9);
        System.out.println(result3); // Expected: -1
    }
}