import java.util.*;

/*
 * Title: Find the Deepest Fully Budgeted Project Node
 * Difficulty: Medium
 * Topic: Trees
 *
 * Problem Description:
 * You are given the root of a binary tree representing a hierarchy of project tasks.
 * Each node stores an integer budget value. A project node is called fully budgeted
 * if the sum of all values in its left subtree is exactly equal to the sum of all
 * values in its right subtree. An empty subtree has sum 0.
 *
 * Your task is to return the value of the deepest fully budgeted node in the tree.
 * If multiple fully budgeted nodes exist at the same deepest depth, return the one
 * that appears leftmost in the tree. If no such node exists, return -1.
 *
 * Depth is measured from the root, where the root has depth 0. A leaf node is fully
 * budgeted because both its left and right subtree sums are 0.
 *
 * You should design an algorithm that works efficiently for trees with up to 100,000
 * nodes, so repeated subtree-sum recomputation from every node will be too slow.
 * A postorder traversal is useful because it allows us to compute subtree sums while
 * also tracking candidate answers by depth.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [1, 100000]
 * - -100000 <= Node.val <= 100000
 * - The tree is binary and may be unbalanced
 * - Return the node value, not the node reference
 *
 * Example 1:
 * Input: root = [8,4,6,2,2,3,3]
 * Output: 2
 *
 * Example 2:
 * Input: root = [5,1,7,null,null,4,3]
 * Output: 4
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
         * @param val value stored in the node
         */
        TreeNode(int val) {
            this.val = val;
        }
    }

    /**
     * Small helper object used by the recursive postorder traversal.
     * It stores:
     * 1) the total sum of the subtree rooted at the current node
     * 2) the best answer found inside that subtree
     *
     * "Best" means:
     * - deepest fully budgeted node
     * - if depths tie, the leftmost one
     */
    private static class Result {
        long subtreeSum;
        int bestDepth;
        int bestValue;
        boolean hasAnswer;

        /**
         * Constructs a result object.
         *
         * @param subtreeSum total sum of the current subtree
         * @param bestDepth deepest depth of a fully budgeted node found so far
         * @param bestValue value of that node
         * @param hasAnswer whether a valid fully budgeted node exists in this result
         */
        Result(long subtreeSum, int bestDepth, int bestValue, boolean hasAnswer) {
            this.subtreeSum = subtreeSum;
            this.bestDepth = bestDepth;
            this.bestValue = bestValue;
            this.hasAnswer = hasAnswer;
        }
    }

    /**
     * Returns the value of the deepest fully budgeted node.
     * If multiple such nodes exist at the same deepest depth, the leftmost one is returned.
     * If no such node exists, returns -1.
     *
     * The algorithm performs one postorder traversal:
     * - First compute left subtree information
     * - Then compute right subtree information
     * - Then decide whether the current node is fully budgeted
     * - Then combine the best candidate from left subtree, right subtree, and current node
     *
     * The leftmost tie-breaking rule is handled naturally:
     * - When left and right subtree candidates have the same depth, we keep the left one
     * - This works because postorder processes the left subtree before the right subtree
     *
     * @param root root of the binary tree
     * @return value of the deepest fully budgeted node, or -1 if none exists
     * Time complexity: O(n), where n is the number of nodes
     * Space complexity: O(h) for recursion stack, where h is tree height; worst case O(n)
     */
    public int deepestFullyBudgetedNode(TreeNode root) {
        if (root == null) {
            return -1;
        }

        Result result = dfs(root, 0);
        return result.hasAnswer ? result.bestValue : -1;
    }

    /**
     * Recursive postorder traversal that computes subtree sums and the best answer
     * inside the current subtree.
     *
     * Step-by-step idea:
     * 1. Recursively solve the left subtree.
     * 2. Recursively solve the right subtree.
     * 3. Compute the current subtree sum:
     *      current sum = left subtree sum + right subtree sum + current node value
     * 4. Check whether the current node is fully budgeted:
     *      left subtree sum == right subtree sum
     * 5. Among three possible candidates:
     *      - best from left subtree
     *      - best from right subtree
     *      - current node itself (if fully budgeted)
     *    choose the deepest one.
     * 6. If depths tie, prefer the leftmost one:
     *      - left subtree candidate beats right subtree candidate on equal depth
     *      - current node can never tie with a deeper descendant because its depth is smaller
     *
     * @param node current node being processed
     * @param depth depth of the current node from the root
     * @return a Result containing subtree sum and best fully budgeted node in this subtree
     * Time complexity: O(size of subtree rooted at node)
     * Space complexity: O(h) recursion stack for subtree height h
     */
    private Result dfs(TreeNode node, int depth) {
        if (node == null) {
            return new Result(0L, -1, -1, false);
        }

        // Postorder traversal:
        // We must solve children first because the current node's "fully budgeted" status
        // depends on the sums of its left and right subtrees.
        Result leftResult = dfs(node.left, depth + 1);
        Result rightResult = dfs(node.right, depth + 1);

        // Compute the total sum of the subtree rooted at this node.
        long currentSubtreeSum = leftResult.subtreeSum + rightResult.subtreeSum + node.val;

        // Start with "no answer yet" for this subtree.
        boolean hasBest = false;
        int bestDepth = -1;
        int bestValue = -1;

        // First, consider the best candidate from the left subtree.
        // We consider left first intentionally because if left and right have the same depth,
        // the left subtree node is more leftmost in the tree and must win.
        if (leftResult.hasAnswer) {
            hasBest = true;
            bestDepth = leftResult.bestDepth;
            bestValue = leftResult.bestValue;
        }

        // Next, consider the best candidate from the right subtree.
        // We only replace the current best if the right candidate is strictly deeper.
        // If depths are equal, we DO NOT replace, because the left candidate is leftmost.
        if (rightResult.hasAnswer) {
            if (!hasBest || rightResult.bestDepth > bestDepth) {
                hasBest = true;
                bestDepth = rightResult.bestDepth;
                bestValue = rightResult.bestValue;
            }
        }

        // Now check whether the current node itself is fully budgeted.
        // A node is fully budgeted if the sum of values in its left subtree
        // equals the sum of values in its right subtree.
        // This automatically makes every leaf fully budgeted because both sums are 0.
        if (leftResult.subtreeSum == rightResult.subtreeSum) {
            // The current node is a valid candidate at the current depth.
            // Replace the best candidate only if:
            // - we do not have one yet, or
            // - the current node is deeper
            //
            // We do not need special tie handling here:
            // a node cannot tie in depth with any node inside its own subtree,
            // because descendants are always deeper.
            if (!hasBest || depth > bestDepth) {
                hasBest = true;
                bestDepth = depth;
                bestValue = node.val;
            }
        }

        return new Result(currentSubtreeSum, bestDepth, bestValue, hasBest);
    }

    /**
     * Builds a binary tree from a level-order array representation.
     * Null values represent missing children.
     *
     * Example:
     * [8, 4, 6, 2, 2, 3, 3]
     *
     * This helper is used only for demonstration in main.
     *
     * @param values level-order node values, where null means no node
     * @return root of the constructed binary tree
     * Time complexity: O(n)
     * Space complexity: O(n)
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
     * Expected outputs:
     * Example 1 -> 2
     * Example 2 -> 4
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstrated test case
     * Space complexity: O(n) for tree construction
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        Integer[] example1 = {8, 4, 6, 2, 2, 3, 3};
        TreeNode root1 = solution.buildTree(example1);
        System.out.println(solution.deepestFullyBudgetedNode(root1)); // Expected: 2

        Integer[] example2 = {5, 1, 7, null, null, 4, 3};
        TreeNode root2 = solution.buildTree(example2);
        System.out.println(solution.deepestFullyBudgetedNode(root2)); // Expected: 4

        Integer[] extraExample = {1};
        TreeNode root3 = solution.buildTree(extraExample);
        System.out.println(solution.deepestFullyBudgetedNode(root3)); // Expected: 1
    }
}