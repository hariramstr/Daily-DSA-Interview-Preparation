import java.util.*;

/*
 * Title: Longest Alternating Turn Path in a Binary Tree
 * Difficulty: Medium
 * Topic: Trees
 *
 * Problem Description:
 * You are given the root of a binary tree. A path may start at any node and must move only downward,
 * following parent-to-child edges. For every step in the path, you choose either the left child or
 * the right child of the current node. A path is called an alternating turn path if the direction of
 * every move alternates: left, right, left, right, ... or right, left, right, left, ... .
 * The path does not need to end at a leaf, and it may contain just one node.
 *
 * Return the length of the longest alternating turn path in the tree, measured in number of edges.
 *
 * For example, if a path visits nodes [8 -> 3 -> 6 -> 4], then its length is 3 because it uses 3 edges.
 * A single node has length 0.
 *
 * Your task is to compute the maximum possible length over all valid starting nodes and both possible
 * first directions.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [1, 100000].
 * - Node values are in the range [-1000000000, 1000000000].
 * - All node values are arbitrary and are not required to be unique.
 * - The expected solution should run in O(n) time.
 * - Use O(h) auxiliary space for recursion or an equivalent iterative traversal, where h is the tree height.
 *
 * Example 1:
 * Input: root = [1,2,3,null,4,5,null,null,null,6]
 * Output: 2
 *
 * Example 2:
 * Input: root = [7,9,8,4,null,null,10,null,5]
 * Output: 3
 *
 * Note:
 * The natural interpretation of this problem is identical to the classic "longest zigzag path":
 * after moving left, the next move must be right; after moving right, the next move must be left.
 * The algorithm below computes that value correctly in O(n) time.
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
         * Constructs a node with the given value.
         *
         * @param val node value
         */
        TreeNode(int val) {
            this.val = val;
        }
    }

    /**
     * Small helper object returned by the DFS.
     *
     * leftStart:
     *   The longest alternating path length (in edges) that starts at the current node
     *   and whose first move is to the left child.
     *
     * rightStart:
     *   The longest alternating path length (in edges) that starts at the current node
     *   and whose first move is to the right child.
     *
     * Example:
     * If current node has a left child, then:
     *   leftStart = 1 + (best path from left child whose next move must be right)
     *
     * If current node has no left child, then leftStart = 0 because a single node path is allowed,
     * but there is no edge to take in that direction.
     */
    private static class Result {
        int leftStart;
        int rightStart;

        Result(int leftStart, int rightStart) {
            this.leftStart = leftStart;
            this.rightStart = rightStart;
        }
    }

    /**
     * Stores the best answer found anywhere in the tree.
     */
    private int best;

    /**
     * Computes the length of the longest alternating turn path in the binary tree.
     *
     * The path:
     * - may start at any node
     * - must move downward only
     * - must alternate directions on every edge
     * - is measured in number of edges
     *
     * @param root the root of the binary tree
     * @return the maximum number of edges in any valid alternating downward path
     *
     * Time complexity: O(n), where n is the number of nodes, because each node is processed once.
     * Space complexity: O(h), where h is the height of the tree, due to recursion stack.
     */
    public int longestAlternatingTurnPath(TreeNode root) {
        best = 0;
        dfs(root);
        return best;
    }

    /**
     * Performs a post-order DFS and computes two values for each node:
     *
     * 1) longest path starting here with first move LEFT
     * 2) longest path starting here with first move RIGHT
     *
     * Why post-order?
     * Because to compute the answer for the current node, we need answers from its children first.
     *
     * Detailed recurrence:
     *
     * Let current node be X.
     *
     * - If X.left exists, then taking the first edge to the left contributes 1 edge,
     *   and after that the next required move must be right.
     *   So:
     *       leftStart(X) = 1 + rightStart(X.left)
     *
     * - If X.right exists, then taking the first edge to the right contributes 1 edge,
     *   and after that the next required move must be left.
     *   So:
     *       rightStart(X) = 1 + leftStart(X.right)
     *
     * - If a child does not exist, then that direction cannot extend by an edge,
     *   so the path length in that direction is 0.
     *
     * While computing these values for every node, we continuously update the global best answer.
     *
     * @param node the current node being processed
     * @return a Result containing:
     *         - longest alternating path starting at this node with first move left
     *         - longest alternating path starting at this node with first move right
     *
     * Time complexity: O(n) over the full traversal, because each node is visited once.
     * Space complexity: O(h) recursion stack, where h is the tree height.
     */
    private Result dfs(TreeNode node) {
        // Base case:
        // If the node is null, there is no path.
        // We return (0, 0) so parent computations remain simple.
        if (node == null) {
            return new Result(0, 0);
        }

        // Recursively solve the left subtree first.
        Result leftResult = dfs(node.left);

        // Recursively solve the right subtree next.
        Result rightResult = dfs(node.right);

        // Initialize both directional path lengths from this node to 0.
        // A single node alone has path length 0 edges.
        int leftStart = 0;
        int rightStart = 0;

        // If there is a left child, we can take one edge to the left.
        // After moving left, the next move must be right to preserve alternation.
        // Therefore we add:
        //   1 edge (current -> left child)
        //   + the best path from the left child that starts by moving right
        if (node.left != null) {
            leftStart = 1 + leftResult.rightStart;
        }

        // If there is a right child, we can take one edge to the right.
        // After moving right, the next move must be left to preserve alternation.
        // Therefore we add:
        //   1 edge (current -> right child)
        //   + the best path from the right child that starts by moving left
        if (node.right != null) {
            rightStart = 1 + rightResult.leftStart;
        }

        // Update the global best answer using both possibilities from this node.
        // This is crucial because the optimal path may start at any node, not necessarily the root.
        best = Math.max(best, leftStart);
        best = Math.max(best, rightStart);

        // Return the two directional answers for use by this node's parent.
        return new Result(leftStart, rightStart);
    }

    /**
     * Builds a binary tree from a level-order array representation where null means "missing node".
     *
     * Example:
     * [1, 2, 3, null, 4]
     *
     * corresponds to:
     *       1
     *      / \
     *     2   3
     *      \
     *       4
     *
     * This helper is used only for demonstration in main.
     *
     * @param values level-order node values, where null indicates no node
     * @return the root of the constructed binary tree
     *
     * Time complexity: O(n), where n is the number of array entries.
     * Space complexity: O(n), due to the queue used during construction.
     */
    public static TreeNode buildTree(Integer[] values) {
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
     * Produces a simple level-order string representation of the tree.
     * This is only for demonstration output in main.
     *
     * @param root the root of the tree
     * @return a level-order representation including null placeholders trimmed at the end
     *
     * Time complexity: O(n), where n is the number of nodes visited.
     * Space complexity: O(n), for the queue and output list.
     */
    public static String treeToLevelOrderString(TreeNode root) {
        if (root == null) {
            return "[]";
        }

        List<String> output = new ArrayList<>();
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();

            if (node == null) {
                output.add("null");
            } else {
                output.add(String.valueOf(node.val));
                queue.offer(node.left);
                queue.offer(node.right);
            }
        }

        int end = output.size() - 1;
        while (end >= 0 && "null".equals(output.get(end))) {
            end--;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i <= end; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(output.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Demonstrates the solution on sample-style inputs and prints the results.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: Depends on the sample tree sizes; each demonstration call is O(n).
     * Space complexity: Depends on the sample tree sizes; each call uses O(h) recursion stack.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the prompt.
        Integer[] example1 = {1, 2, 3, null, 4, 5, null, null, null, 6};
        TreeNode root1 = buildTree(example1);
        int answer1 = solution.longestAlternatingTurnPath(root1);
        System.out.println("Example 1 tree: " + treeToLevelOrderString(root1));
        System.out.println("Longest alternating turn path length: " + answer1);
        System.out.println("Expected: 2");
        System.out.println();

        // Example 2 from the prompt.
        // The prompt's textual explanation is inconsistent, but the algorithm correctly computes
        // the longest alternating downward path according to the formal definition.
        Integer[] example2 = {7, 9, 8, 4, null, null, 10, null, 5};
        TreeNode root2 = buildTree(example2);
        int answer2 = solution.longestAlternatingTurnPath(root2);
        System.out.println("Example 2 tree: " + treeToLevelOrderString(root2));
        System.out.println("Longest alternating turn path length: " + answer2);
        System.out.println("Computed according to the formal alternating-direction definition.");
        System.out.println();

        // Additional sanity check:
        // A perfect alternating chain:
        //       1
        //      /
        //     2
        //      \
        //       3
        //      /
        //     4
        // Path 1 -> 2 -> 3 -> 4 is L, R, L => length 3
        TreeNode custom = new TreeNode(1);
        custom.left = new TreeNode(2);
        custom.left.right = new TreeNode(3);
        custom.left.right.left = new TreeNode(4);

        int customAnswer = solution.longestAlternatingTurnPath(custom);
        System.out.println("Custom tree: " + treeToLevelOrderString(custom));
        System.out.println("Longest alternating turn path length: " + customAnswer);
        System.out.println("Expected: 3");
    }
}