import java.util.*;

/*
Problem Title: Find the First Turn Point on a Binary Route

Problem Description:
You are given the root of a binary tree and a target value that is guaranteed to exist in the tree.
Imagine walking from the root down to the node containing the target value. Along this route, each
move is either to the left child or to the right child.

A node is called a turn point if the direction you take from that node is different from the
direction taken from its parent. In other words:
- if you moved left from one node and then right from the next node, that next move represents a turn,
- similarly for right followed by left.

This solution returns the value of the first node on the root-to-target path where this direction
change first occurs.

Important clarification used by this implementation:
If the path contains directions d1, d2, d3, ...
and the first change happens between di and d(i+1),
then the turn point is the node from which d(i+1) is taken.
That matches the examples:
- Path 8 -> 4 -> 6 -> 7 has moves L, R, R
- The first change is from L to R
- The node from which the new direction R is taken is node 4
- Therefore the answer is 4

If the path never changes direction, return -1.
The root is never considered a turn point because it has no parent.
If the target is the root, the answer is also -1.

Constraints:
- Number of nodes: [1, 1000]
- Node values are unique
- Target always exists in the tree
- Tree is a standard binary tree
*/

public class Solution {

    /**
     * Basic binary tree node definition.
     */
    static class TreeNode {
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
     * Small helper object used to carry the answer out of the DFS recursion.
     * We use an object so recursive calls can update shared state cleanly.
     */
    static class Result {
        int firstTurnValue = -1;
    }

    /**
     * Finds the value of the first turn point on the path from the root to the target node.
     *
     * A "turn" happens when the direction used to move from a node to its child is different
     * from the direction used in the previous step on the same root-to-target path.
     *
     * Example:
     * root -> left -> right
     * The first change is from left to right, so the node from which "right" is taken
     * is the first turn point.
     *
     * @param root the root of the binary tree
     * @param target the target value guaranteed to exist in the tree
     * @return the value of the first turn point, or -1 if the path never changes direction
     *
     * Time complexity: O(n), because in the worst case we may visit each node once.
     * Space complexity: O(h), where h is the height of the tree due to recursion stack.
     */
    public int findFirstTurnPoint(TreeNode root, int target) {
        // If the tree is empty, there is no valid path.
        // The problem guarantees a valid tree and target, but this guard keeps the method safe.
        if (root == null) {
            return -1;
        }

        // If the target is the root, there are no moves at all.
        // Since a turn requires at least two moves to compare directions, answer is -1.
        if (root.val == target) {
            return -1;
        }

        // This object will store the first turn value once discovered.
        Result result = new Result();

        // Start DFS from the root.
        //
        // Parameters:
        // - current node: root
        // - target value
        // - previous direction: 'N' means "none yet", because from the root we have not moved
        // - first turn already found? false
        // - shared result object
        dfs(root, target, 'N', false, result);

        return result.firstTurnValue;
    }

    /**
     * Performs a depth-first search to locate the target while tracking movement directions.
     *
     * Detailed idea:
     * 1. We walk from the root toward the target.
     * 2. At each node, we try going left and right.
     * 3. When we choose a direction:
     *    - if there was no previous direction yet, we simply continue
     *    - otherwise, we compare the new direction with the previous one
     * 4. If they differ and we have not already recorded a turn, then the current node
     *    is the first turn point.
     * 5. We continue only along paths that can still lead to the target.
     *
     * Why the current node is the turn point:
     * Suppose the path directions are L then R.
     * The first move L was taken from the parent.
     * The new move R is taken from the current node.
     * Therefore the current node is where the path "turns".
     *
     * @param node the current node being explored
     * @param target the target value we are searching for
     * @param previousDirection the direction used to arrive at this node:
     *                          'L' for left, 'R' for right, 'N' for none/root
     * @param alreadyFoundTurn true if the first turn point has already been identified earlier on this path
     * @param result shared object storing the final answer
     * @return true if the target exists in the subtree rooted at this node; otherwise false
     *
     * Time complexity: O(n), because each node is visited at most once in the search.
     * Space complexity: O(h), where h is the tree height due to recursion.
     */
    public boolean dfs(TreeNode node, int target, char previousDirection, boolean alreadyFoundTurn, Result result) {
        // Base case:
        // If the current node is null, this path does not contain the target.
        if (node == null) {
            return false;
        }

        // If we reached the target, we return true to signal that the current recursive path
        // is indeed the correct root-to-target path.
        if (node.val == target) {
            return true;
        }

        /*
         * Try going LEFT first.
         *
         * If node.left is part of the path to target, then:
         * - the new direction is 'L'
         * - compare it with previousDirection
         * - if previousDirection exists and differs from 'L', then the current node is a turn point
         *   provided we have not already found an earlier turn on this path
         */
        if (node.left != null) {
            char newDirection = 'L';

            // Determine whether taking this left edge creates the first direction change.
            boolean turnHappensHere = previousDirection != 'N'
                    && previousDirection != newDirection
                    && !alreadyFoundTurn;

            // If a turn happens here, then this node is the first turn point.
            // We tentatively store it before exploring deeper.
            // This is safe because if the left subtree does not contain the target,
            // we will overwrite/reset as needed when exploring the correct path.
            int oldValue = result.firstTurnValue;
            if (turnHappensHere) {
                result.firstTurnValue = node.val;
            }

            // Recurse into the left subtree.
            // The previous direction for the child becomes 'L'.
            // The "already found turn" flag becomes true if it was already true before,
            // or if the turn is discovered at this current node.
            boolean foundInLeft = dfs(node.left, target, newDirection, alreadyFoundTurn || turnHappensHere, result);

            // If target was found in the left subtree, we immediately return true.
            // That means the stored answer corresponds to the real root-to-target path.
            if (foundInLeft) {
                return true;
            }

            // If target was NOT found in the left subtree, then any tentative answer recorded
            // while exploring left is invalid for the actual target path.
            // So we restore the previous answer before trying the right subtree.
            result.firstTurnValue = oldValue;
        }

        /*
         * Try going RIGHT next.
         *
         * The logic is symmetric to the left side:
         * - new direction is 'R'
         * - if previous direction exists and differs, current node is the first turn point
         *   if one has not already been found earlier
         */
        if (node.right != null) {
            char newDirection = 'R';

            boolean turnHappensHere = previousDirection != 'N'
                    && previousDirection != newDirection
                    && !alreadyFoundTurn;

            int oldValue = result.firstTurnValue;
            if (turnHappensHere) {
                result.firstTurnValue = node.val;
            }

            boolean foundInRight = dfs(node.right, target, newDirection, alreadyFoundTurn || turnHappensHere, result);

            if (foundInRight) {
                return true;
            }

            result.firstTurnValue = oldValue;
        }

        // If neither subtree contains the target, report failure to the caller.
        return false;
    }

    /**
     * Builds a binary tree from a level-order array representation.
     *
     * The array uses Integer values:
     * - a number means a real node
     * - null means no node at that position
     *
     * Example:
     * [8, 4, 12, 2, 6, null, 14, null, null, 5, 7]
     *
     * @param values level-order representation of the tree
     * @return the root of the constructed binary tree, or null if the input is empty
     *
     * Time complexity: O(n), where n is the number of array elements.
     * Space complexity: O(n), due to the queue used during construction.
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
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few extra checks.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(n) per demonstration tree operation.
     * Space complexity: O(n) for tree construction and O(h) for DFS recursion.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Tree: [8,4,12,2,6,null,14,null,null,5,7]
        // Target: 7
        // Path: 8 -> 4 -> 6 -> 7
        // Moves: L, R, R
        // First change: L to R, taken from node 4
        // Expected answer: 4
        Integer[] values1 = {8, 4, 12, 2, 6, null, 14, null, null, 5, 7};
        TreeNode root1 = solution.buildTree(values1);
        int answer1 = solution.findFirstTurnPoint(root1, 7);
        System.out.println("Example 1 Output: " + answer1);

        // Example 2:
        // Tree: [10,5,15,3,7]
        // Target: 3
        // Path: 10 -> 5 -> 3
        // Moves: L, L
        // No direction change
        // Expected answer: -1
        Integer[] values2 = {10, 5, 15, 3, 7};
        TreeNode root2 = solution.buildTree(values2);
        int answer2 = solution.findFirstTurnPoint(root2, 3);
        System.out.println("Example 2 Output: " + answer2);

        // Extra demonstration:
        // Target is the root, so no moves exist.
        // Expected answer: -1
        int answer3 = solution.findFirstTurnPoint(root2, 10);
        System.out.println("Target Is Root Output: " + answer3);

        // Extra demonstration:
        // Path to 7 in tree [10,5,15,3,7] is 10 -> 5 -> 7
        // Moves: L, R
        // First change occurs at node 5's parent? No.
        // The new direction R is taken from node 5, so answer is 5.
        int answer4 = solution.findFirstTurnPoint(root2, 7);
        System.out.println("Extra Example Output: " + answer4);
    }
}