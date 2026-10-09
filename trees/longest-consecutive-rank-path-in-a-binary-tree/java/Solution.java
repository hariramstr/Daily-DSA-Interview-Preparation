import java.util.*;

/*
 * Title: Longest Consecutive Rank Path in a Binary Tree
 * Difficulty: Medium
 * Topic: Trees
 *
 * Problem Description:
 * You are given the root of a binary tree where each node stores an integer rank.
 * A valid rank chain is any path that moves only through parent-child edges, and at every
 * step the rank must change by exactly 1. The path may be strictly increasing, strictly
 * decreasing, or switch direction once at a middle node by combining a decreasing side and
 * an increasing side. In other words, the path does not need to start at the root, does not
 * need to end at a leaf, and may go from one descendant up through a node and down to another
 * descendant, as long as every adjacent pair differs by exactly 1.
 *
 * Return the length of the longest valid rank chain in the tree, measured as the number of
 * nodes in the path.
 *
 * For example, if one side of a node forms 7 -> 6 -> 5 and the other side forms 5 -> 6,
 * then the combined path 7 -> 6 -> 5 -> 6 has differences of 1 at every step and is valid.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [1, 100000].
 * - -1000000000 <= Node.val <= 1000000000
 * - The tree is a binary tree.
 * - Your solution should run in O(n) time.
 *
 * Example 1:
 * Input: root = [4,3,5,2,null,null,6,1]
 * Output: 6
 * Explanation: The longest valid path is 1 -> 2 -> 3 -> 4 -> 5 -> 6, which uses parent-child
 * edges and changes by exactly 1 at each step.
 *
 * Example 2:
 * Input: root = [10,9,11,8,10,null,12]
 * Output: 5
 * Explanation: One longest valid path is 8 -> 9 -> 10 -> 11 -> 12. The path passes through
 * the root and every adjacent pair differs by exactly 1.
 *
 * A single node is always a valid rank chain of length 1.
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
     * Small helper object returned by DFS.
     *
     * inc = length of the longest consecutive path starting at this node and going downward
     *       where values increase by 1 as we move away from this node.
     *       Example: node=4, child=5, grandchild=6 gives inc=3 for node 4.
     *
     * dec = length of the longest consecutive path starting at this node and going downward
     *       where values decrease by 1 as we move away from this node.
     *       Example: node=4, child=3, grandchild=2 gives dec=3 for node 4.
     */
    private static class Result {
        int inc;
        int dec;

        Result(int inc, int dec) {
            this.inc = inc;
            this.dec = dec;
        }
    }

    /**
     * Stores the best answer found anywhere in the tree.
     */
    private int best;

    /**
     * Computes the length of the longest valid rank chain in the binary tree.
     *
     * The path may:
     * - go only downward on one side,
     * - be strictly increasing,
     * - be strictly decreasing,
     * - or pass through a middle node by combining a decreasing chain from one side
     *   and an increasing chain from the other side.
     *
     * Core idea:
     * For every node, compute:
     * - inc: longest downward path starting at this node with +1 each step
     * - dec: longest downward path starting at this node with -1 each step
     *
     * Then the best path passing through this node is:
     * dec + inc - 1
     *
     * Why subtract 1?
     * Because the current node is counted in both dec and inc.
     *
     * @param root the root of the binary tree
     * @return the number of nodes in the longest valid rank chain
     * Time complexity: O(n), where n is the number of nodes, because each node is processed once.
     * Space complexity: O(h), where h is the height of the tree due to recursion stack;
     * in the worst case of a skewed tree this becomes O(n).
     */
    public int longestConsecutive(TreeNode root) {
        best = 0;
        dfs(root);
        return best;
    }

    /**
     * Performs a post-order DFS and returns the increasing/decreasing chain lengths
     * starting from the current node and going downward.
     *
     * Detailed logic:
     * 1. Recursively solve left subtree and right subtree first.
     * 2. Start with inc = 1 and dec = 1 because the node alone is always a valid path.
     * 3. If a child differs by exactly +1 from the current node, that child can extend
     *    the current node's increasing chain.
     * 4. If a child differs by exactly -1 from the current node, that child can extend
     *    the current node's decreasing chain.
     * 5. Since there may be both left and right candidates, take the maximum extension.
     * 6. Update the global answer using dec + inc - 1, which forms a path that can go:
     *      decreasing side -> current node -> increasing side
     *    This also naturally covers purely increasing or purely decreasing paths.
     *
     * Example:
     * If at node 4:
     * - left contributes dec = 4 via 4 -> 3 -> 2 -> 1
     * - right contributes inc = 3 via 4 -> 5 -> 6
     * then the path through 4 has length 4 + 3 - 1 = 6,
     * corresponding to 1 -> 2 -> 3 -> 4 -> 5 -> 6.
     *
     * @param node the current node being processed
     * @return a Result containing:
     *         - inc: longest downward increasing-by-1 chain starting at node
     *         - dec: longest downward decreasing-by-1 chain starting at node
     * Time complexity: O(n) over the full recursion, because each node is visited once.
     * Space complexity: O(h) recursion stack, where h is the tree height.
     */
    private Result dfs(TreeNode node) {
        if (node == null) {
            return new Result(0, 0);
        }

        Result left = dfs(node.left);
        Result right = dfs(node.right);

        int inc = 1;
        int dec = 1;

        /*
         * Check whether the left child can extend either chain.
         *
         * If left child value is exactly current + 1:
         * current -> left is an increasing-by-1 step when moving downward.
         * So we can extend the current node's increasing chain using left.inc.
         *
         * If left child value is exactly current - 1:
         * current -> left is a decreasing-by-1 step when moving downward.
         * So we can extend the current node's decreasing chain using left.dec.
         */
        if (node.left != null) {
            if (node.left.val == node.val + 1) {
                inc = Math.max(inc, 1 + left.inc);
            } else if (node.left.val == node.val - 1) {
                dec = Math.max(dec, 1 + left.dec);
            }
        }

        /*
         * Do the same check for the right child.
         * We again take the maximum because either side might provide the longer chain.
         */
        if (node.right != null) {
            if (node.right.val == node.val + 1) {
                inc = Math.max(inc, 1 + right.inc);
            } else if (node.right.val == node.val - 1) {
                dec = Math.max(dec, 1 + right.dec);
            }
        }

        /*
         * Combine both directions through the current node.
         *
         * dec counts a chain like: node -> node-1 -> node-2 ...
         * inc counts a chain like: node -> node+1 -> node+2 ...
         *
         * When viewed as one full path, this becomes:
         * smaller ... -> node-1 -> node -> node+1 -> larger ...
         *
         * The current node is included in both counts, so subtract 1 once.
         */
        best = Math.max(best, dec + inc - 1);

        return new Result(inc, dec);
    }

    /**
     * Builds a binary tree from a level-order array representation.
     * Null values represent missing children.
     *
     * Example:
     * [4, 3, 5, 2, null, null, 6, 1]
     *
     * This helper is used only for demonstration in main.
     *
     * @param values level-order node values, where null means no node
     * @return the root of the constructed binary tree
     * Time complexity: O(n), where n is the number of array entries.
     * Space complexity: O(n), due to the queue and created nodes.
     */
    public TreeNode buildTree(Integer[] values) {
        if (values == null || values.length == 0 || values[0] == null) {
            return null;
        }

        TreeNode root = new TreeNode(values[0]);
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        int index = 1;

        while (!queue.isEmpty() && index < values.length) {
            TreeNode current = queue.poll();

            if (index < values.length && values[index] != null) {
                current.left = new TreeNode(values[index]);
                queue.offer(current.left);
            }
            index++;

            if (index < values.length && values[index] != null) {
                current.right = new TreeNode(values[index]);
                queue.offer(current.right);
            }
            index++;
        }

        return root;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * It builds the trees, runs the algorithm, and prints the results.
     *
     * Expected outputs:
     * Example 1 -> 6
     * Example 2 -> 5
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstrated test case.
     * Space complexity: O(n) for tree construction plus recursion/queue usage.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        Integer[] example1 = {4, 3, 5, 2, null, null, 6, 1};
        TreeNode root1 = solution.buildTree(example1);
        int answer1 = solution.longestConsecutive(root1);
        System.out.println("Example 1 Output: " + answer1);
        System.out.println("Expected: 6");

        Integer[] example2 = {10, 9, 11, 8, 10, null, 12};
        TreeNode root2 = solution.buildTree(example2);
        int answer2 = solution.longestConsecutive(root2);
        System.out.println("Example 2 Output: " + answer2);
        System.out.println("Expected: 5");

        /*
         * Additional tiny sanity check:
         * Single node tree should return 1.
         */
        Integer[] example3 = {42};
        TreeNode root3 = solution.buildTree(example3);
        int answer3 = solution.longestConsecutive(root3);
        System.out.println("Single Node Output: " + answer3);
        System.out.println("Expected: 1");
    }
}