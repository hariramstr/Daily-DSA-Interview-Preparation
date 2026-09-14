import java.util.*;

/*
Problem Title: Maximum Alternating Level Sum in a Binary Tree

Problem Description:
You are given the root of a binary tree where each node contains an integer value, which may be positive, zero, or negative.
For any node, define its alternating level sum as the sum of values in its subtree where nodes at even distance from that
node are added and nodes at odd distance from that node are subtracted. In other words, for a chosen node x, include x.val,
subtract the values of its children, add the values of its grandchildren, and so on for the entire subtree of x.

Your task is to return the maximum alternating level sum among all nodes in the tree.

This is not the same as choosing a path. For every candidate node, you must evaluate its entire subtree with alternating signs
by depth relative to that node, then take the largest result over all nodes.

The tree can contain up to 100000 nodes, so an O(n^2) solution that recomputes each subtree independently will time out.
You should design an O(n) solution using tree traversal and dynamic programming on trees.

Constraints:
- The number of nodes is in the range [1, 100000].
- -100000 <= Node.val <= 100000
- The tree is a valid binary tree.

Examples:
1) root = [5,2,4,1,3,null,6]
   Alternating sums:
   - Node 5: 5 - (2 + 4) + (1 + 3 + 6) = 9
   - Node 2: 2 - (1 + 3) = -2
   - Node 4: 4 - 6 = -2
   - Node 1: 1
   - Node 3: 3
   - Node 6: 6
   Maximum = 9

2) root = [-3,7,2,-5,1]
   Alternating sums:
   - Node -3: -3 - (7 + 2) + (-5 + 1) = -16
   - Node 7: 7 - (-5 + 1) = 11
   - Node 2: 2
   - Node -5: -5
   - Node 1: 1
   Maximum = 11
*/

/**
 * Complete runnable solution for computing the maximum alternating level sum in a binary tree.
 *
 * The key dynamic programming idea:
 * Let alt(node) be the alternating level sum of the subtree rooted at node.
 *
 * Then:
 * alt(node) = node.val - alt(node.left rooted one level below?) Not exactly.
 *
 * More carefully:
 * For a node x:
 * alt(x) = x.val - (sum over depth 1) + (sum over depth 2) - ...
 *
 * If alt(left) is computed relative to left itself, then:
 * alt(left) = left.val - grandchildren-of-x-through-left? ...
 *
 * The contribution of the entire left subtree to alt(x) is:
 * -(left.val) + (left's children) - (left's grandchildren) + ...
 * which is exactly -alt(left).
 *
 * Therefore:
 * alt(x) = x.val - alt(x.left) - alt(x.right)
 *
 * This gives a simple postorder DP in O(n).
 *
 * While computing alt(x) for every node, we track the maximum value seen.
 */
public class Solution {

    /**
     * Definition for a binary tree node.
     */
    public static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        /**
         * Constructs a node with the given value.
         *
         * @param val value stored in the node
         */
        TreeNode(int val) {
            this.val = val;
        }

        /**
         * Constructs a node with value and children.
         *
         * @param val value stored in the node
         * @param left left child
         * @param right right child
         */
        TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }

    /**
     * Stores the best alternating level sum found anywhere in the tree during DFS.
     * We use long because:
     * - up to 100000 nodes
     * - each value can be up to 100000 in magnitude
     * - total alternating sum can be around 1e10, which does not fit in int
     */
    private long maxAnswer;

    /**
     * Returns the maximum alternating level sum among all nodes in the tree.
     *
     * The algorithm performs one postorder traversal:
     * 1. Compute the alternating sum for the left subtree.
     * 2. Compute the alternating sum for the right subtree.
     * 3. Use the recurrence:
     *      alt(node) = node.val - alt(node.left) - alt(node.right)
     * 4. Update the global maximum with alt(node).
     *
     * Why this recurrence is correct:
     * - In alt(node), all nodes in the left subtree are one level deeper relative to node
     *   than they are relative to left child.
     * - That flips every sign in the left subtree contribution.
     * - So the left subtree contributes exactly -alt(node.left).
     * - Same for the right subtree.
     *
     * @param root the root of the binary tree
     * @return the maximum alternating level sum over all subtree roots
     * Time complexity: O(n), where n is the number of nodes
     * Space complexity: O(h) for recursion stack, where h is the tree height
     */
    public long maximumAlternatingLevelSum(TreeNode root) {
        maxAnswer = Long.MIN_VALUE;
        dfs(root);
        return maxAnswer;
    }

    /**
     * Postorder DFS that computes the alternating level sum for the subtree rooted at the given node.
     *
     * Detailed step-by-step behavior:
     * - If node is null, its subtree contributes 0.
     * - Recursively compute the alternating sum of the left subtree.
     * - Recursively compute the alternating sum of the right subtree.
     * - Build the current node's alternating sum using:
     *      current = node.val - leftAlt - rightAlt
     * - Update the global maximum answer.
     * - Return current so the parent can use it.
     *
     * Example:
     * For node 5 with left alt = -2 and right alt = -2:
     * current = 5 - (-2) - (-2) = 9
     *
     * @param node current subtree root
     * @return alternating level sum of the subtree rooted at node
     * Time complexity: O(1) work per node, so O(n) total across the traversal
     * Space complexity: O(h) recursion stack
     */
    public long dfs(TreeNode node) {
        if (node == null) {
            return 0L;
        }

        // First solve the left subtree completely.
        long leftAlt = dfs(node.left);

        // Then solve the right subtree completely.
        long rightAlt = dfs(node.right);

        // Now compute the alternating sum for the current node.
        //
        // Explanation:
        // - node.val is at distance 0 from itself, so it is added.
        // - Every node in the left subtree is one level farther from the current node
        //   than from node.left, so all signs flip -> subtract alt(left).
        // - Same logic for the right subtree -> subtract alt(right).
        long currentAlt = (long) node.val - leftAlt - rightAlt;

        // This node is one candidate answer.
        // We keep the largest alternating sum seen anywhere in the tree.
        if (currentAlt > maxAnswer) {
            maxAnswer = currentAlt;
        }

        // Return the DP value for this subtree to the parent.
        return currentAlt;
    }

    /**
     * Builds a binary tree from a level-order array representation where null means no node.
     *
     * Example:
     * [5, 2, 4, 1, 3, null, 6]
     *
     * This helper is used only for demonstration in main.
     *
     * @param values level-order node values, with null representing missing nodes
     * @return root of the constructed binary tree
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public TreeNode buildTree(Integer[] values) {
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
     * Prints a simple level-order traversal of the tree.
     * This is optional utility output for demonstration.
     *
     * @param root root of the tree
     * @return level-order representation as a string
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String levelOrderToString(TreeNode root) {
        if (root == null) {
            return "[]";
        }

        List<String> result = new ArrayList<>();
        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();

            if (node == null) {
                result.add("null");
                continue;
            }

            result.add(String.valueOf(node.val));

            // We only enqueue non-null children if at least one child exists,
            // or if we want to preserve structure for visible nodes.
            if (node.left != null || node.right != null) {
                queue.offer(node.left);
                queue.offer(node.right);
            }
        }

        return result.toString();
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs after careful verification:
     * Example 1:
     * - root = [5,2,4,1,3,null,6]
     * - answer = 9
     *
     * Example 2:
     * - root = [-3,7,2,-5,1]
     * - answer = 11
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(n) per demonstrated test case
     * Space complexity: O(n)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        Integer[] example1 = {5, 2, 4, 1, 3, null, 6};
        TreeNode root1 = solution.buildTree(example1);
        long answer1 = solution.maximumAlternatingLevelSum(root1);
        System.out.println("Example 1 tree: " + Arrays.toString(example1));
        System.out.println("Maximum alternating level sum: " + answer1);
        System.out.println("Expected: 9");
        System.out.println();

        // Example 2
        Integer[] example2 = {-3, 7, 2, -5, 1};
        TreeNode root2 = solution.buildTree(example2);
        long answer2 = solution.maximumAlternatingLevelSum(root2);
        System.out.println("Example 2 tree: " + Arrays.toString(example2));
        System.out.println("Maximum alternating level sum: " + answer2);
        System.out.println("Expected: 11");
        System.out.println();

        // Additional small sanity check
        Integer[] example3 = {1};
        TreeNode root3 = solution.buildTree(example3);
        long answer3 = solution.maximumAlternatingLevelSum(root3);
        System.out.println("Example 3 tree: " + Arrays.toString(example3));
        System.out.println("Maximum alternating level sum: " + answer3);
        System.out.println("Expected: 1");
    }
}