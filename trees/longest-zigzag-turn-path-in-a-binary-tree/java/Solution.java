import java.util.*;

/*
 * Title: Longest Zigzag Turn Path in a Binary Tree
 * Difficulty: Medium
 * Topic: Trees
 *
 * Problem Description:
 * You are given the root of a binary tree. A path is called a zigzag turn path if every move
 * alternates between going to a left child and going to a right child. The path may start at
 * any node in the tree, and it must always move downward from parent to child. The length of
 * a path is the number of edges used, not the number of nodes.
 *
 * Your task is to return the maximum possible length of any zigzag turn path in the tree.
 *
 * For example, if you move from a node to its left child, then the next step in the path must
 * go to a right child, then left again, and so on. A path consisting of a single node has
 * length 0.
 *
 * You should design an algorithm that runs efficiently for large trees.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [1, 10^5].
 * - Node values are in the range [-10^9, 10^9].
 * - All node values are arbitrary and do not affect the answer.
 * - The tree is not necessarily balanced.
 *
 * Example 1:
 * Input: root = [1,null,2,3,4,null,null,5,null]
 * Output: 2
 * Explanation: One longest zigzag path is 2 -> 3 -> 5. The moves are right-to-left, which is
 * valid because the direction alternates at each step. The path length is 2 edges.
 *
 * Example 2:
 * Input: root = [7,4,9,2,6,null,10,null,3,5,null]
 * Output: 3
 * Explanation: One longest zigzag path is 7 -> 4 -> 6 -> 5. The moves are left, then right,
 * then left, so the path alternates correctly. Its length is 3.
 *
 * Note:
 * The tree is represented in level-order array form, where null indicates a missing child.
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
         * @param val the value stored in the node
         */
        TreeNode(int val) {
            this.val = val;
        }
    }

    /**
     * Small helper object used during DFS.
     *
     * For each node, we compute:
     * - leftLen:
     *   The longest zigzag path length starting at this node if the FIRST move goes to the left child.
     * - rightLen:
     *   The longest zigzag path length starting at this node if the FIRST move goes to the right child.
     *
     * Example:
     * If we go from current node to left child, then the next move must go right,
     * so leftLen depends on the child's rightLen.
     */
    private static class ZigzagInfo {
        int leftLen;
        int rightLen;

        ZigzagInfo(int leftLen, int rightLen) {
            this.leftLen = leftLen;
            this.rightLen = rightLen;
        }
    }

    /**
     * Stores the best zigzag length found anywhere in the tree.
     */
    private int bestAnswer = 0;

    /**
     * Returns the maximum length of any zigzag turn path in the binary tree.
     *
     * The key idea:
     * For every node, compute two values:
     * 1. Longest zigzag path starting from this node if the first move is LEFT
     * 2. Longest zigzag path starting from this node if the first move is RIGHT
     *
     * Then update a global maximum using those values.
     *
     * We use a post-order DFS so that when we process a node, we already know the answers
     * for its children.
     *
     * @param root the root of the binary tree
     * @return the maximum zigzag path length measured in number of edges
     *
     * Time complexity: O(n), where n is the number of nodes in the tree, because each node is processed once.
     * Space complexity: O(h), where h is the height of the tree due to recursion stack; in the worst case O(n).
     */
    public int longestZigZag(TreeNode root) {
        bestAnswer = 0;
        dfs(root);
        return bestAnswer;
    }

    /**
     * Performs a post-order DFS and returns zigzag information for the current node.
     *
     * Detailed logic:
     * - If node is null:
     *   There is no valid starting edge from a null node.
     *   We return (-1, -1) instead of (0, 0).
     *
     * Why -1?
     * This is a very useful trick:
     * - Suppose a node has a left child.
     * - Then leftLen = 1 + leftChild.rightLen
     * - If leftChild.rightLen is -1 because the needed continuation does not exist,
     *   then leftLen becomes 0 only when the child itself is null? Let's reason carefully:
     *
     * For a real child:
     * - dfs(realChild) returns at least 0 for one-step possibilities from that child.
     *
     * For a missing child:
     * - We do not use 1 + (-1) directly unless the child is null.
     * - In that case, we explicitly set the corresponding length to 0 because no edge exists.
     *
     * More concretely:
     * - If node.left != null, then taking one left edge is valid, so:
     *   leftLen = 1 + leftInfo.rightLen
     * - If node.left == null, then leftLen = 0 because no left edge can be taken.
     *
     * Similarly for rightLen.
     *
     * After computing both values for the current node, we update the global answer.
     *
     * @param node the current node being processed
     * @return a ZigzagInfo object containing:
     *         - leftLen: longest zigzag starting here with first move left
     *         - rightLen: longest zigzag starting here with first move right
     *
     * Time complexity: O(1) work per node, so O(n) total over the whole traversal.
     * Space complexity: O(h) recursion stack, where h is the tree height.
     */
    private ZigzagInfo dfs(TreeNode node) {
        if (node == null) {
            return new ZigzagInfo(-1, -1);
        }

        // Recursively compute answers for the left subtree.
        ZigzagInfo leftInfo = dfs(node.left);

        // Recursively compute answers for the right subtree.
        ZigzagInfo rightInfo = dfs(node.right);

        // If we start from the current node and the first move is LEFT:
        // - We must have a left child to take that first edge.
        // - After moving left once, the next required move must be RIGHT.
        // - Therefore, we add 1 edge for the move to node.left,
        //   then continue with node.left's "start by going RIGHT" answer.
        int leftLen = 0;
        if (node.left != null) {
            leftLen = 1 + leftInfo.rightLen;
        }

        // If we start from the current node and the first move is RIGHT:
        // - We must have a right child.
        // - After moving right once, the next required move must be LEFT.
        // - Therefore, we add 1 edge for the move to node.right,
        //   then continue with node.right's "start by going LEFT" answer.
        int rightLen = 0;
        if (node.right != null) {
            rightLen = 1 + rightInfo.leftLen;
        }

        // Update the global best answer.
        // We consider both possibilities because the longest path starting at this node
        // could begin either by going left or by going right.
        bestAnswer = Math.max(bestAnswer, Math.max(leftLen, rightLen));

        return new ZigzagInfo(leftLen, rightLen);
    }

    /**
     * Builds a binary tree from a level-order array representation.
     *
     * Example:
     * [1, null, 2, 3, 4, null, null, 5, null]
     *
     * Rules:
     * - Each array element corresponds to a node in level-order.
     * - null means that position has no node.
     *
     * This helper is only for demonstration in main.
     *
     * @param values the level-order array representation of the tree
     * @return the root of the constructed binary tree, or null if the input is empty or starts with null
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
     * Prints a simple level-order traversal of the tree.
     * This is a utility method for demonstration purposes.
     *
     * @param root the root of the tree to print
     * @return nothing
     *
     * Time complexity: O(n), where n is the number of nodes in the tree.
     * Space complexity: O(n), due to the queue.
     */
    public void printLevelOrder(TreeNode root) {
        if (root == null) {
            System.out.println("[]");
            return;
        }

        List<String> result = new ArrayList<>();
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();

            if (node == null) {
                result.add("null");
            } else {
                result.add(String.valueOf(node.val));
                queue.offer(node.left);
                queue.offer(node.right);
            }
        }

        int last = result.size() - 1;
        while (last >= 0 && "null".equals(result.get(last))) {
            last--;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i <= last; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(result.get(i));
        }
        sb.append("]");

        System.out.println(sb);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * Verified manually:
     * Example 1:
     * Tree: [1,null,2,3,4,null,null,5,null]
     * One longest zigzag path is 2 -> 3 -> 5
     * Length = 2
     *
     * Example 2:
     * Tree: [7,4,9,2,6,null,10,null,3,5,null]
     * One longest zigzag path is 7 -> 4 -> 6 -> 5
     * Length = 3
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstrated test case for building + solving.
     * Space complexity: O(n) for tree construction and traversal helpers.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        Integer[] example1 = {1, null, 2, 3, 4, null, null, 5, null};
        TreeNode root1 = solution.buildTree(example1);
        System.out.println("Example 1 tree:");
        solution.printLevelOrder(root1);
        System.out.println("Longest Zigzag Path Length: " + solution.longestZigZag(root1));
        System.out.println("Expected: 2");
        System.out.println();

        Integer[] example2 = {7, 4, 9, 2, 6, null, 10, null, 3, 5, null};
        TreeNode root2 = solution.buildTree(example2);
        System.out.println("Example 2 tree:");
        solution.printLevelOrder(root2);
        System.out.println("Longest Zigzag Path Length: " + solution.longestZigZag(root2));
        System.out.println("Expected: 3");
        System.out.println();

        Integer[] singleNode = {42};
        TreeNode root3 = solution.buildTree(singleNode);
        System.out.println("Single node tree:");
        solution.printLevelOrder(root3);
        System.out.println("Longest Zigzag Path Length: " + solution.longestZigZag(root3));
        System.out.println("Expected: 0");
    }
}