/*
Problem Title: Sum of Cousin Nodes in a Binary Tree

Problem Description:
You are given the root of a binary tree and a target value x that is guaranteed to appear exactly once in the tree.
Two nodes are considered cousins if they are on the same depth level but have different parents.
Your task is to return the sum of all cousin node values of the node whose value is x.

If the target node has no cousins, return 0.

The tree is not necessarily complete or balanced. Node values may be positive, negative, or zero,
but all values in the tree are unique so the target node can be identified by value alone.

A straightforward way to solve this is to traverse the tree level by level.
Once you find the level containing the target node, you should sum all nodes on that same level
except the target node itself and any siblings that share the same parent as the target node.

Constraints:
- The number of nodes in the tree is in the range [1, 1000].
- -10^4 <= Node.val <= 10^4
- All node values are unique.
- x is the value of exactly one node in the tree.

Example 1:
Input: root = [5,3,8,1,4,7,9], x = 4
Output: 16
Explanation:
The node with value 4 is at depth 2. Its parent is 3.
The other nodes at depth 2 are 1, 7, and 9.
Node 1 is a sibling, so it is not a cousin.
The cousins are 7 and 9, and their sum is 16.

Example 2:
Input: root = [10,6,15,3,null,12,18], x = 12
Output: 3
Explanation:
The node with value 12 is at depth 2 and its parent is 15.
The only node at the same depth with a different parent is 3, so the answer is 3.
*/

import java.util.*;

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
         * @param val the value to store in the node
         */
        TreeNode(int val) {
            this.val = val;
        }
    }

    /**
     * Helper class used during breadth-first traversal.
     * It stores both the current node and its parent so that we can easily
     * determine whether two nodes are siblings or cousins.
     */
    private static class NodeWithParent {
        TreeNode node;
        TreeNode parent;

        /**
         * Creates a pair of (node, parent).
         *
         * @param node the current tree node
         * @param parent the parent of the current node; null if this is the root
         */
        NodeWithParent(TreeNode node, TreeNode parent) {
            this.node = node;
            this.parent = parent;
        }
    }

    /**
     * Returns the sum of all cousin nodes of the node whose value is x.
     *
     * The algorithm performs a level-order traversal (BFS).
     * For each level:
     * 1. We inspect all nodes on that level.
     * 2. We check whether the target value x exists on this level.
     * 3. If the target is found, we remember its parent.
     * 4. We then sum all nodes on the same level whose parent is different from the target's parent.
     *    This automatically excludes:
     *    - the target node itself
     *    - any sibling of the target node
     *
     * Why this works:
     * Cousins are exactly the nodes at the same depth with different parents.
     * BFS naturally processes the tree one depth level at a time, which makes it ideal here.
     *
     * @param root the root of the binary tree
     * @param x the unique target value whose cousins' sum must be computed
     * @return the sum of all cousin node values of the target node; returns 0 if no cousins exist
     *
     * Time complexity: O(n), where n is the number of nodes in the tree, because each node is visited at most once.
     * Space complexity: O(w), where w is the maximum width of the tree, due to the BFS queue.
     */
    public int sumOfCousins(TreeNode root, int x) {
        // Defensive check.
        // According to the problem, root exists and x is guaranteed to be present,
        // but returning 0 here keeps the method safe and beginner-friendly.
        if (root == null) {
            return 0;
        }

        // Standard queue for breadth-first traversal.
        // Each queue entry stores:
        // - the current node
        // - the parent of that node
        Queue<NodeWithParent> queue = new LinkedList<>();
        queue.offer(new NodeWithParent(root, null));

        // Process the tree level by level.
        while (!queue.isEmpty()) {
            // The number of nodes currently in the queue equals the number of nodes
            // on the current depth level.
            int levelSize = queue.size();

            // We store all nodes of the current level in a temporary list.
            // This is useful because:
            // 1. We first need to determine whether x is on this level.
            // 2. If x is on this level, we then need to sum nodes on this same level
            //    based on parent comparison.
            List<NodeWithParent> currentLevel = new ArrayList<>(levelSize);

            // This will hold the parent of the target node if x is found on this level.
            TreeNode targetParent = null;

            // Step 1: Remove all nodes of the current level from the queue,
            // store them in currentLevel, and check whether x is present here.
            for (int i = 0; i < levelSize; i++) {
                NodeWithParent current = queue.poll();
                currentLevel.add(current);

                // If this node is the target, remember its parent.
                if (current.node.val == x) {
                    targetParent = current.parent;
                }
            }

            // Step 2: If the target node is on this level, compute the cousin sum.
            if (targetParent != null || (currentLevel.size() == 1 && currentLevel.get(0).node.val == x)) {
                int sum = 0;

                // Every node on this level with a different parent from targetParent
                // is a cousin. Nodes with the same parent are siblings (or the target itself),
                // so they must be excluded.
                for (NodeWithParent entry : currentLevel) {
                    if (entry.parent != targetParent) {
                        sum += entry.node.val;
                    }
                }

                return sum;
            }

            // Step 3: Since the target was not on this level, expand to the next level
            // by adding all children of the current level's nodes into the queue.
            for (NodeWithParent entry : currentLevel) {
                TreeNode node = entry.node;

                if (node.left != null) {
                    queue.offer(new NodeWithParent(node.left, node));
                }

                if (node.right != null) {
                    queue.offer(new NodeWithParent(node.right, node));
                }
            }
        }

        // The problem guarantees x exists exactly once, so normally execution never reaches here.
        return 0;
    }

    /**
     * Builds a binary tree from a level-order array representation.
     * Null values represent missing children.
     *
     * Example:
     * [5, 3, 8, 1, 4, 7, 9]
     * builds the tree:
     *         5
     *       /   \
     *      3     8
     *     / \   / \
     *    1   4 7   9
     *
     * @param values the level-order representation of the tree, where null means no node
     * @return the root of the constructed binary tree
     *
     * Time complexity: O(n), where n is the number of entries in the array.
     * Space complexity: O(n), due to the queue used during construction.
     */
    public TreeNode buildTree(Integer[] values) {
        // If the input array is empty or starts with null, the tree is empty.
        if (values == null || values.length == 0 || values[0] == null) {
            return null;
        }

        // Create the root from the first value.
        TreeNode root = new TreeNode(values[0]);

        // Queue used to assign children in level-order.
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        // Index points to the next value in the array that has not yet been used.
        int index = 1;

        // Continue until we either run out of nodes to process or run out of array values.
        while (!queue.isEmpty() && index < values.length) {
            TreeNode current = queue.poll();

            // Assign left child if available.
            if (index < values.length && values[index] != null) {
                current.left = new TreeNode(values[index]);
                queue.offer(current.left);
            }
            index++;

            // Assign right child if available.
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
     * @param args command-line arguments (not used)
     *
     * @return nothing
     *
     * Time complexity: O(1) for the demonstration logic itself, excluding tree construction and algorithm calls.
     * Space complexity: O(1) auxiliary space for the demonstration logic itself.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Input: root = [5,3,8,1,4,7,9], x = 4
        // Target node 4 is at depth 2 with parent 3.
        // Nodes at depth 2 are 1, 4, 7, 9.
        // Sibling 1 shares parent 3, so it is excluded.
        // Cousins are 7 and 9 => sum = 16.
        Integer[] values1 = {5, 3, 8, 1, 4, 7, 9};
        TreeNode root1 = solution.buildTree(values1);
        int result1 = solution.sumOfCousins(root1, 4);
        System.out.println("Example 1 Output: " + result1); // Expected: 16

        // Example 2:
        // Input: root = [10,6,15,3,null,12,18], x = 12
        // Target node 12 is at depth 2 with parent 15.
        // Nodes at depth 2 are 3, 12, 18.
        // Sibling 18 shares parent 15, so it is excluded.
        // Cousin is 3 => sum = 3.
        Integer[] values2 = {10, 6, 15, 3, null, 12, 18};
        TreeNode root2 = solution.buildTree(values2);
        int result2 = solution.sumOfCousins(root2, 12);
        System.out.println("Example 2 Output: " + result2); // Expected: 3

        // Additional demonstration:
        // If the target is the root, it has no cousins because no other node exists at depth 0.
        Integer[] values3 = {1, 2, 3, 4, 5};
        TreeNode root3 = solution.buildTree(values3);
        int result3 = solution.sumOfCousins(root3, 1);
        System.out.println("Additional Example Output: " + result3); // Expected: 0
    }
}