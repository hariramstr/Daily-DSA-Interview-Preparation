import java.util.*;

/*
Problem Title: Find the Deepest Complete Team Subtree

Problem Description:
A company stores its reporting structure as a binary tree where each node represents a team lead.
A node may have a left direct report, a right direct report, both, or neither.

A subtree rooted at some node is called complete if every node in that subtree has either exactly
0 children or exactly 2 children. In other words, no node inside that subtree is allowed to have
only one child.

Given the root of a binary tree, return the value of the root node of the deepest complete subtree.
If multiple complete subtrees have the same maximum depth, return the one with the smallest node value.
The depth of a subtree is defined by the depth of its root in the original tree, where the overall
root has depth 0.

You should design an algorithm that visits each node efficiently and determines whether the subtree
rooted at that node is complete under the definition above.

Constraints:
- The number of nodes in the tree is in the range [1, 100000].
- Node values are integers in the range [-1000000000, 1000000000].
- All node values are unique.
- The tree is not necessarily balanced.

Important correctness note:
A subtree is valid if every node inside that subtree has either 0 or 2 children.
That means:
- Every leaf is a valid complete subtree.
- Any internal node is a valid complete subtree root only if:
  1) it has both left and right children, and
  2) both child subtrees are themselves complete.
- A node with exactly one child is invalid.

Therefore, among all valid subtree roots, we choose:
1) the one with greatest depth in the original tree
2) if depths tie, the one with the smallest node value

This interpretation is the one implemented below because it matches the formal definition.
*/

public class Solution {

    /**
     * Basic binary tree node definition used by the solution and the demo code.
     */
    public static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        /**
         * Creates a tree node with the given value.
         *
         * @param val value stored in the node
         */
        TreeNode(int val) {
            this.val = val;
        }
    }

    /**
     * Small helper object returned by the DFS.
     * It tells whether the subtree rooted at the current node is complete under the problem's rule.
     */
    private static class SubtreeInfo {
        boolean isComplete;

        /**
         * Creates a result object for one subtree.
         *
         * @param isComplete whether the subtree is complete
         */
        SubtreeInfo(boolean isComplete) {
            this.isComplete = isComplete;
        }
    }

    /**
     * Stores the best answer found so far while traversing the tree.
     * We keep:
     * - bestDepth: the deepest depth of any valid complete subtree root seen so far
     * - bestValue: among roots at that depth, the smallest node value
     */
    private int bestDepth;
    private int bestValue;

    /**
     * Finds the value of the root node of the deepest complete subtree.
     *
     * A subtree is complete here if every node in that subtree has either:
     * - exactly 0 children, or
     * - exactly 2 children
     *
     * If multiple valid subtree roots appear at the same deepest depth, the smallest node value
     * is returned.
     *
     * Time complexity: O(n), because each node is visited exactly once.
     * Space complexity: O(h) for recursion stack, where h is the tree height.
     * In the worst case of a skewed tree, h can be O(n).
     *
     * @param root the root of the binary tree
     * @return the value of the root node of the deepest complete subtree
     */
    public int findDeepestCompleteSubtree(TreeNode root) {
        bestDepth = -1;
        bestValue = Integer.MAX_VALUE;

        dfs(root, 0);

        return bestValue;
    }

    /**
     * Performs a post-order DFS.
     *
     * Why post-order?
     * Because whether the current subtree is complete depends on whether the left subtree
     * and right subtree are complete. So we must solve children first, then the parent.
     *
     * Step-by-step logic for one node:
     * 1) Recursively process the left child.
     * 2) Recursively process the right child.
     * 3) Determine whether the current node forms a valid complete subtree:
     *    - If it has no children, it is a leaf, so it is complete.
     *    - If it has both children, then it is complete only if both child subtrees are complete.
     *    - If it has exactly one child, it is not complete.
     * 4) If the current subtree is complete, compare its root depth with the best answer seen so far:
     *    - deeper depth wins
     *    - if same depth, smaller node value wins
     *
     * Time complexity: O(n), because each node is processed once.
     * Space complexity: O(h) recursion stack, where h is the tree height.
     *
     * @param node current node being processed
     * @param depth depth of the current node in the original tree
     * @return information describing whether the subtree rooted at this node is complete
     */
    private SubtreeInfo dfs(TreeNode node, int depth) {
        // This branch is only used internally for recursion convenience.
        // A null node is treated as "complete" so that a leaf's children do not break logic.
        if (node == null) {
            return new SubtreeInfo(true);
        }

        // Recursively solve left and right subtrees first.
        // This is the "post-order" pattern: children before parent.
        SubtreeInfo leftInfo = dfs(node.left, depth + 1);
        SubtreeInfo rightInfo = dfs(node.right, depth + 1);

        boolean isComplete;

        // Case 1: leaf node
        // A leaf has 0 children, which is allowed by the problem definition.
        if (node.left == null && node.right == null) {
            isComplete = true;
        }
        // Case 2: exactly two children
        // This is allowed only if BOTH child subtrees are themselves complete.
        else if (node.left != null && node.right != null) {
            isComplete = leftInfo.isComplete && rightInfo.isComplete;
        }
        // Case 3: exactly one child
        // This is forbidden by the problem definition.
        else {
            isComplete = false;
        }

        // If the current subtree is valid, it becomes a candidate answer.
        if (isComplete) {
            // Prefer greater depth.
            if (depth > bestDepth) {
                bestDepth = depth;
                bestValue = node.val;
            }
            // If depth ties, prefer smaller node value.
            else if (depth == bestDepth && node.val < bestValue) {
                bestValue = node.val;
            }
        }

        return new SubtreeInfo(isComplete);
    }

    /**
     * Builds a binary tree from a level-order array representation.
     *
     * Example:
     * [8, 4, 10, 2, 6, 9, null, null, null, 5, 7]
     *
     * This helper is used only for demonstration in main.
     *
     * Time complexity: O(n), where n is the number of array entries.
     * Space complexity: O(n), due to the queue and created nodes.
     *
     * @param values level-order representation of the tree, where null means no node
     * @return root of the constructed binary tree
     */
    public static TreeNode buildTree(Integer[] values) {
        if (values == null || values.length == 0 || values[0] == null) {
            return null;
        }

        TreeNode root = new TreeNode(values[0]);
        Queue<TreeNode> queue = new ArrayDeque<>();
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
     * Demonstrates the solution on sample-style inputs and prints the results.
     *
     * Note:
     * The implementation follows the formal problem definition exactly:
     * choose the valid complete subtree root at the greatest depth,
     * and if tied, choose the smallest node value.
     *
     * Time complexity: O(n) per demonstrated test case.
     * Space complexity: O(h) for DFS plus O(n) for tree construction in the demo.
     *
     * @param args command-line arguments, not used
     * @return nothing
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the prompt:
        // Tree: [8,4,10,2,6,9,null,null,null,5,7]
        //
        // Valid complete subtree roots:
        // - Leaves: 2, 5, 7, 9
        // - Node 6 is also valid because it has two children 5 and 7, both leaves
        //
        // Their depths:
        // - 2 at depth 2
        // - 6 at depth 2
        // - 9 at depth 2
        // - 5 at depth 3
        // - 7 at depth 3
        //
        // Deepest depth is 3, so among {5, 7}, smallest value is 5.
        Integer[] example1 = {8, 4, 10, 2, 6, 9, null, null, null, 5, 7};
        TreeNode root1 = buildTree(example1);
        System.out.println(solution.findDeepestCompleteSubtree(root1)); // Expected by formal definition: 5

        // Example 2 from the prompt:
        // Tree: [12,7,20,3,null,15,25]
        //
        // Valid complete subtree roots:
        // - Leaves: 3, 15, 25
        // - Node 20 is also valid at depth 1
        //
        // Deepest valid roots are 3, 15, 25 at depth 2, smallest value is 3.
        Integer[] example2 = {12, 7, 20, 3, null, 15, 25};
        TreeNode root2 = buildTree(example2);
        System.out.println(solution.findDeepestCompleteSubtree(root2)); // Expected by formal definition: 3

        // Additional custom test:
        // Perfect binary tree:
        //        1
        //      /   \
        //     2     3
        //    / \   / \
        //   4  5  6  7
        //
        // Deepest valid roots are leaves 4,5,6,7 at depth 2 -> smallest is 4.
        Integer[] example3 = {1, 2, 3, 4, 5, 6, 7};
        TreeNode root3 = buildTree(example3);
        System.out.println(solution.findDeepestCompleteSubtree(root3)); // Expected: 4

        // Additional custom test:
        // Single node tree.
        Integer[] example4 = {42};
        TreeNode root4 = buildTree(example4);
        System.out.println(solution.findDeepestCompleteSubtree(root4)); // Expected: 42
    }
}