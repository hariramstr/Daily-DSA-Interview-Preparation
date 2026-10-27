import java.util.*;

/*
 * Title: Find the Deepest Leftmost Incomplete Department
 * Difficulty: Medium
 * Topic: Trees
 *
 * Problem Description:
 * A company models its department structure as a binary tree. Each node represents a department
 * and stores a unique integer department ID. The left child is the first sub-department and the
 * right child is the second sub-department. A department is considered incomplete if it has
 * exactly one direct sub-department (either left or right, but not both).
 *
 * Your task is to return the department ID of the incomplete department that appears at the
 * greatest depth in the tree. If multiple incomplete departments exist at the same deepest depth,
 * return the leftmost one among them. The root is at depth 0, its children are at depth 1, and so on.
 *
 * If there is no incomplete department in the tree, return -1.
 *
 * This problem tests careful tree traversal and tie-breaking logic. A correct solution should
 * identify incompleteness locally at each node, but compare candidates globally by depth first
 * and horizontal order second. The tree can be large, so your solution should run efficiently.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [0, 100000].
 * - Node values are unique integers in the range [-1000000000, 1000000000].
 * - The input tree is a binary tree and may be empty.
 *
 * Example 1:
 * Input: root = [10,5,20,3,null,15,30,null,4]
 * Output: 3
 * Explanation:
 * Node 5 is incomplete because it has only a left child.
 * Node 3 is also incomplete because it has only a right child.
 * Node 3 is deeper than node 5, so the answer is 3.
 *
 * Example 2:
 * Input: root = [8,4,12,2,6,null,14,1]
 * Output: 2
 * Explanation:
 * Node 12 is incomplete because it has only a right child.
 * Node 2 is also incomplete because it has only a left child.
 * Both are at depth 2, so we return the leftmost one at that depth.
 * Therefore, the correct output is 2.
 */

public class Solution {

    /**
     * Basic binary tree node definition used for the department tree.
     */
    public static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        /**
         * Creates a tree node with the given department ID.
         *
         * @param val the unique department ID stored in this node
         */
        TreeNode(int val) {
            this.val = val;
        }
    }

    /**
     * Helper class used in breadth-first traversal.
     * It stores both the node and its depth so that we can evaluate candidates correctly.
     */
    private static class NodeDepth {
        TreeNode node;
        int depth;

        /**
         * Creates a traversal state containing a node and its depth.
         *
         * @param node  the current tree node
         * @param depth the depth of the current node
         */
        NodeDepth(TreeNode node, int depth) {
            this.node = node;
            this.depth = depth;
        }
    }

    /**
     * Finds the department ID of the incomplete node that is deepest in the tree.
     * If multiple incomplete nodes exist at the same deepest depth, the leftmost one is returned.
     *
     * Core idea:
     * - We perform a level-order traversal (BFS), which naturally visits nodes from left to right
     *   within each depth level.
     * - For every node, we check whether it is incomplete:
     *   exactly one child exists.
     * - Because BFS processes nodes level by level:
     *   1) deeper levels are encountered later,
     *   2) within the same level, leftmost nodes are encountered earlier.
     * - Therefore:
     *   - whenever we find an incomplete node at a strictly deeper depth, it becomes the new answer;
     *   - if we find another incomplete node at the same depth, we do NOT replace the answer,
     *     because the first one seen at that depth is the leftmost one.
     *
     * This gives the correct tie-breaking behavior automatically.
     *
     * @param root the root of the binary tree representing the department structure; may be null
     * @return the department ID of the deepest leftmost incomplete node, or -1 if no incomplete node exists
     *
     * Time complexity: O(n), where n is the number of nodes in the tree, because each node is visited once.
     * Space complexity: O(w), where w is the maximum width of the tree due to the BFS queue;
     * in the worst case this can be O(n).
     */
    public int findDeepestLeftmostIncompleteDepartment(TreeNode root) {
        // If the tree is empty, there are no departments at all,
        // so there cannot be any incomplete department.
        if (root == null) {
            return -1;
        }

        // Queue for standard breadth-first traversal.
        // BFS is ideal here because:
        // - it processes nodes level by level,
        // - and from left to right within each level.
        Queue<NodeDepth> queue = new ArrayDeque<>();
        queue.offer(new NodeDepth(root, 0));

        // bestDepth stores the deepest depth at which we have found
        // an incomplete node so far.
        int bestDepth = -1;

        // bestValue stores the department ID of the current best answer.
        int bestValue = -1;

        // Process nodes in BFS order.
        while (!queue.isEmpty()) {
            NodeDepth current = queue.poll();
            TreeNode node = current.node;
            int depth = current.depth;

            // A node is incomplete if exactly one child exists.
            // This is an XOR-style condition:
            // - left exists and right does not, OR
            // - right exists and left does not.
            boolean hasExactlyOneChild = (node.left == null) ^ (node.right == null);

            // If this node is incomplete, it is a candidate answer.
            if (hasExactlyOneChild) {
                // We only replace the answer if this node is deeper than
                // the best incomplete node seen so far.
                //
                // Important tie-breaking detail:
                // If another incomplete node appears at the same depth,
                // we do NOT replace the current answer, because BFS visits
                // nodes from left to right. That means the first incomplete
                // node seen at a given depth is the leftmost one.
                if (depth > bestDepth) {
                    bestDepth = depth;
                    bestValue = node.val;
                }
            }

            // Enqueue the left child first.
            // This preserves left-to-right order within each level.
            if (node.left != null) {
                queue.offer(new NodeDepth(node.left, depth + 1));
            }

            // Enqueue the right child second.
            if (node.right != null) {
                queue.offer(new NodeDepth(node.right, depth + 1));
            }
        }

        return bestValue;
    }

    /**
     * Builds a binary tree from a level-order array representation.
     * Null values represent missing nodes.
     *
     * Example:
     * [10, 5, 20, 3, null, 15, 30, null, 4]
     *
     * Construction rule:
     * - Read values level by level.
     * - For each non-null node removed from the queue:
     *   - the next array value becomes its left child,
     *   - the following array value becomes its right child.
     *
     * @param values the level-order representation of the tree; null entries mean "no node"
     * @return the root of the constructed binary tree, or null if the input is empty or starts with null
     *
     * Time complexity: O(n), where n is the number of entries in the input array.
     * Space complexity: O(n), due to the queue and created nodes.
     */
    public TreeNode buildTree(Integer[] values) {
        // Handle empty input or a tree whose root is missing.
        if (values == null || values.length == 0 || values[0] == null) {
            return null;
        }

        // Create the root from the first value.
        TreeNode root = new TreeNode(values[0]);

        // Queue used to attach children level by level.
        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.offer(root);

        // Index points to the next value in the level-order array
        // that has not yet been processed.
        int index = 1;

        while (!queue.isEmpty() && index < values.length) {
            TreeNode current = queue.poll();

            // Try to assign the left child if there is still input remaining.
            if (index < values.length) {
                Integer leftValue = values[index++];
                if (leftValue != null) {
                    current.left = new TreeNode(leftValue);
                    queue.offer(current.left);
                }
            }

            // Try to assign the right child if there is still input remaining.
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
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments; not used
     *
     * @return nothing
     *
     * Time complexity: O(n) per demonstrated test case due to tree construction and traversal.
     * Space complexity: O(n) per demonstrated test case.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Tree: [10,5,20,3,null,15,30,null,4]
        // Incomplete nodes:
        // - 5 has only left child (3)
        // - 3 has only right child (4)
        // Deepest incomplete node is 3.
        Integer[] example1 = {10, 5, 20, 3, null, 15, 30, null, 4};
        TreeNode root1 = solution.buildTree(example1);
        System.out.println(solution.findDeepestLeftmostIncompleteDepartment(root1)); // Expected: 3

        // Example 2:
        // Tree: [8,4,12,2,6,null,14,1]
        // Incomplete nodes:
        // - 12 has only right child (14)
        // - 2 has only left child (1)
        // Both are at depth 2, so choose the leftmost one: 2.
        Integer[] example2 = {8, 4, 12, 2, 6, null, 14, 1};
        TreeNode root2 = solution.buildTree(example2);
        System.out.println(solution.findDeepestLeftmostIncompleteDepartment(root2)); // Expected: 2

        // Additional demonstration:
        // Complete tree: no incomplete nodes.
        Integer[] example3 = {1, 2, 3, 4, 5, 6, 7};
        TreeNode root3 = solution.buildTree(example3);
        System.out.println(solution.findDeepestLeftmostIncompleteDepartment(root3)); // Expected: -1

        // Additional demonstration:
        // Single node tree: root has no children, so it is not incomplete.
        Integer[] example4 = {42};
        TreeNode root4 = solution.buildTree(example4);
        System.out.println(solution.findDeepestLeftmostIncompleteDepartment(root4)); // Expected: -1

        // Additional demonstration:
        // Root has only one child, so root itself is incomplete.
        Integer[] example5 = {9, 4, null};
        TreeNode root5 = solution.buildTree(example5);
        System.out.println(solution.findDeepestLeftmostIncompleteDepartment(root5)); // Expected: 9
    }
}