/*
 * Title: Longest Consecutive Approval Chain in an Org Tree
 * Difficulty: Medium
 * Topic: Trees
 *
 * Problem Description:
 * A company stores its approval hierarchy as a binary tree. Each node represents a manager
 * and contains an integer approval level. A valid approval chain is any path that moves only
 * between parent and child nodes, where every next node in the path has an approval level
 * exactly 1 greater than the previous node. The path must follow tree edges, but it does not
 * need to start at the root, and it may end at any node. You may only move downward from
 * parent to child while building the chain.
 *
 * Your task is to return the length of the longest valid approval chain in the tree.
 *
 * For example, if a manager with level 4 has a child with level 5, and that child has a child
 * with level 6, then these three nodes form a valid chain of length 3. If a child differs by
 * more than 1, less than 1, or is equal, the chain is broken and a new chain may start from
 * that child.
 *
 * Implement a function that, given the root of the binary tree, computes the maximum chain length.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [0, 100000].
 * - Node values are in the range [-1000000000, 1000000000].
 * - The tree is not necessarily balanced.
 * - An empty tree should return 0.
 *
 * Example 1:
 * Input: root = [3,4,2,5,null,null,3,6]
 * Output: 4
 * Explanation: The longest downward consecutive chain is 3 -> 4 -> 5 -> 6, so the answer is 4.
 *
 * Example 2:
 * Input: root = [10,11,9,12,10,null,null,13]
 * Output: 4
 * Explanation: The longest valid chain is 10 -> 11 -> 12 -> 13. The node with value 10 under 11
 * breaks the chain because it is not 12.
 *
 * Expected complexity:
 * - A solution that runs in O(n) time is expected, where n is the number of nodes in the tree.
 */

import java.util.*;

public class Solution {

    /**
     * Basic binary tree node definition used by the solution.
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
     * Stores the best answer found so far while traversing the tree.
     * We keep it as a field so every recursive call can update the same shared maximum.
     */
    private int longestChain;

    /**
     * Computes the length of the longest downward parent-to-child consecutive increasing chain.
     * A valid chain requires each child value to be exactly parent value + 1.
     *
     * Algorithm idea:
     * 1. Traverse the tree once using depth-first search.
     * 2. At each node, determine the length of the valid chain ending at that node.
     * 3. If the current node continues the parent's chain (current value == parent value + 1),
     *    extend the chain by 1.
     * 4. Otherwise, start a new chain of length 1 from the current node.
     * 5. Update the global maximum answer at every node.
     *
     * This works because every valid chain is a downward path, so the best chain ending at a node
     * depends only on its parent relationship and the chain length reaching the parent.
     *
     * @param root the root of the binary tree; may be null
     * @return the length of the longest valid approval chain; returns 0 for an empty tree
     * Time complexity: O(n), where n is the number of nodes, because each node is visited once.
     * Space complexity: O(h), where h is the height of the tree due to recursion stack;
     * in the worst case of a skewed tree, this can be O(n).
     */
    public int longestConsecutive(TreeNode root) {
        // If the tree is empty, there are no chains at all.
        if (root == null) {
            return 0;
        }

        // Reset the global answer before starting a fresh computation.
        longestChain = 0;

        // Start DFS from the root.
        // Since the root has no parent, we can pass any value as "parent value" and a previous
        // chain length of 0. The DFS logic will correctly start a new chain at the root.
        dfs(root, 0L, 0);

        return longestChain;
    }

    /**
     * Depth-first traversal that computes the length of the valid consecutive chain ending at
     * the current node.
     *
     * Detailed logic:
     * - We arrive at a node knowing:
     *   1. the parent's value
     *   2. the chain length that ended at the parent
     * - If current node value is exactly parent value + 1:
     *   - the chain continues, so current length = previous length + 1
     * - Otherwise:
     *   - the chain is broken, so a new chain starts here with length = 1
     * - Then we update the global maximum and recurse into both children, passing the current
     *   node's value and the current chain length.
     *
     * We use long for parentValue to safely compare with node values near integer boundaries
     * when adding 1.
     *
     * @param node the current node being processed
     * @param parentValue the value of the parent node
     * @param previousLength the length of the valid chain ending at the parent
     * @return nothing; the method updates the shared global maximum
     * Time complexity: O(n), considering the full traversal across all calls.
     * Space complexity: O(h) recursion stack, where h is the tree height.
     */
    public void dfs(TreeNode node, long parentValue, int previousLength) {
        // Base case: if there is no node, there is nothing to process.
        if (node == null) {
            return;
        }

        int currentLength;

        // Step 1:
        // Check whether the current node continues the consecutive increasing chain.
        // The rule is strict: current value must be exactly parent value + 1.
        if ((long) node.val == parentValue + 1L) {
            // The chain continues, so extend the previous chain by one node.
            currentLength = previousLength + 1;
        } else {
            // The chain is broken here.
            // Even if the parent-child relation is invalid, the current node itself can always
            // start a new valid chain of length 1.
            currentLength = 1;
        }

        // Step 2:
        // Update the best answer seen anywhere in the tree so far.
        // This is necessary because the longest chain may end at any node, not necessarily a leaf.
        longestChain = Math.max(longestChain, currentLength);

        // Step 3:
        // Recurse into the left child.
        // We pass:
        // - current node's value as the new parent value
        // - currentLength as the chain length ending at this node
        dfs(node.left, node.val, currentLength);

        // Step 4:
        // Recurse into the right child with the same updated context.
        dfs(node.right, node.val, currentLength);
    }

    /**
     * Builds a binary tree from a level-order array representation where null means
     * "no node at this position".
     *
     * Example:
     * [3, 4, 2, 5, null, null, 3, 6]
     *
     * This helper is used only for demonstration in main, so sample inputs can be created
     * in a beginner-friendly way.
     *
     * @param values the level-order node values, where null represents a missing node
     * @return the root of the constructed binary tree, or null if the input is empty or starts with null
     * Time complexity: O(n), where n is the number of entries in the array.
     * Space complexity: O(n), due to the queue used during construction.
     */
    public TreeNode buildTree(Integer[] values) {
        // If the array is empty, there is no tree.
        if (values == null || values.length == 0 || values[0] == null) {
            return null;
        }

        // Create the root from the first element.
        TreeNode root = new TreeNode(values[0]);

        // Queue for level-order construction.
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        // Index points to the next array element to consume.
        int index = 1;

        // Process nodes level by level.
        while (!queue.isEmpty() && index < values.length) {
            TreeNode current = queue.poll();

            // Assign left child if there is still input left.
            if (index < values.length) {
                Integer leftValue = values[index++];
                if (leftValue != null) {
                    current.left = new TreeNode(leftValue);
                    queue.offer(current.left);
                }
            }

            // Assign right child if there is still input left.
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
     * Demonstrates the solution on the sample inputs from the problem statement and prints results.
     *
     * @param args command-line arguments; not used
     * @return nothing
     * Time complexity: O(n) per demonstrated test case due to tree construction and traversal.
     * Space complexity: O(n) for tree construction plus O(h) recursion stack during solving.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Tree from level order: [3,4,2,5,null,null,3,6]
        //
        // Structure:
        //         3
        //       /   \
        //      4     2
        //     /       \
        //    5         3
        //   /
        //  6
        //
        // Longest valid downward consecutive chain:
        // 3 -> 4 -> 5 -> 6
        // Length = 4
        Integer[] example1 = {3, 4, 2, 5, null, null, 3, 6};
        TreeNode root1 = solution.buildTree(example1);
        int result1 = solution.longestConsecutive(root1);
        System.out.println("Example 1 Output: " + result1); // Expected: 4

        // Example 2:
        // Tree from level order: [10,11,9,12,10,null,null,13]
        //
        // Structure:
        //         10
        //       /    \
        //     11      9
        //    /  \
        //   12  10
        //  /
        // 13
        //
        // Longest valid downward consecutive chain:
        // 10 -> 11 -> 12 -> 13
        // Length = 4
        //
        // The node 10 under 11 does not continue the chain because it should be 12.
        Integer[] example2 = {10, 11, 9, 12, 10, null, null, 13};
        TreeNode root2 = solution.buildTree(example2);
        int result2 = solution.longestConsecutive(root2);
        System.out.println("Example 2 Output: " + result2); // Expected: 4

        // Additional quick sanity checks:

        // Empty tree -> 0
        TreeNode empty = solution.buildTree(new Integer[]{});
        System.out.println("Empty Tree Output: " + solution.longestConsecutive(empty)); // Expected: 0

        // Single node -> 1
        TreeNode single = solution.buildTree(new Integer[]{42});
        System.out.println("Single Node Output: " + solution.longestConsecutive(single)); // Expected: 1

        // Chain breaks and restarts:
        // [5,6,7,7,null,null,8]
        // Valid chains include:
        // 5 -> 6 (length 2)
        // 7 -> 8 (length 2) on the right side
        // Left child 7 under 6 breaks because expected 7 actually continues, so 5->6->7 is 3
        Integer[] extra = {5, 6, 7, 7, null, null, 8};
        TreeNode rootExtra = solution.buildTree(extra);
        System.out.println("Extra Example Output: " + solution.longestConsecutive(rootExtra)); // Expected: 3
    }
}