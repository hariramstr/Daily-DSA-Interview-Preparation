import java.util.*;

/*
 * Title: Minimum Cameras to Monitor a Facility Tree
 * Difficulty: Medium
 * Topic: Trees
 *
 * Problem Description:
 * A company models its facility layout as a binary tree. Each tree node represents a room,
 * and each room may have up to two directly connected child rooms. You want to install
 * security cameras so that every room is monitored.
 *
 * A camera placed in a room monitors exactly three types of rooms:
 * 1. The room where it is installed
 * 2. Its parent room
 * 3. Its immediate child rooms
 *
 * A room is considered secure if it is monitored by at least one camera.
 *
 * Given the root of the binary tree, return the minimum number of cameras needed to monitor
 * all rooms.
 *
 * The solution must work efficiently for large trees, so brute-force placement over all
 * subsets of nodes is not acceptable.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [1, 10^5].
 * - Node values are integers in the range [0, 10^9], but values are only identifiers and
 *   do not affect the answer.
 * - The input tree is a valid binary tree.
 *
 * Example 1:
 * Input: root = [0,0,null,0,0]
 * Output: 1
 * Explanation: Placing one camera on the second node from the top monitors that node,
 * its parent, and both of its children, covering the entire tree.
 *
 * Example 2:
 * Input: root = [0,0,null,0,null,0,null,null,0]
 * Output: 2
 * Explanation: One camera is not enough because the rooms form a deeper chain with a side
 * branch. An optimal placement uses two cameras to cover all rooms.
 *
 * Common interview follow-up:
 * Explain the state assigned to each node during a postorder traversal, such as whether
 * the node has a camera, is covered, or still needs coverage.
 */

public class Solution {

    /**
     * Basic binary tree node definition.
     */
    public static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode() {
        }

        TreeNode(int val) {
            this.val = val;
        }

        TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }

    /*
     * We use three states for each node after processing its children:
     *
     * NEEDS_CAMERA = This node is currently NOT covered by any camera and expects its parent
     *                to place a camera.
     *
     * HAS_CAMERA   = This node has a camera installed on it.
     *
     * COVERED      = This node does not have a camera, but it is already monitored by one
     *                of its children.
     *
     * Why this works:
     * We process the tree in postorder (left, right, node), so when we decide the state of
     * the current node, we already know the states of both children.
     */
    private static final int NEEDS_CAMERA = 0;
    private static final int HAS_CAMERA = 1;
    private static final int COVERED = 2;

    private int cameraCount;

    /**
     * Returns the minimum number of cameras needed to monitor every node in the tree.
     *
     * Strategy:
     * - Perform a postorder traversal.
     * - If any child needs a camera, place a camera at the current node.
     * - Else if any child has a camera, the current node is covered.
     * - Else the current node needs a camera.
     * - After traversal, if the root still needs a camera, place one more at the root.
     *
     * @param root the root of the binary tree representing the facility
     * @return the minimum number of cameras required to monitor all nodes
     * Time complexity: O(n), where n is the number of nodes, because each node is processed once.
     * Space complexity: O(h), where h is the height of the tree due to recursion stack;
     * in the worst case of a skewed tree, this can be O(n).
     */
    public int minCameraCover(TreeNode root) {
        cameraCount = 0;

        /*
         * Run the DFS that returns one of the three states for the root.
         */
        int rootState = dfs(root);

        /*
         * Important final check:
         * If the root itself still needs coverage after processing all descendants,
         * there is no parent above it to place a camera, so we must place one here.
         */
        if (rootState == NEEDS_CAMERA) {
            cameraCount++;
        }

        return cameraCount;
    }

    /**
     * Postorder DFS that determines the monitoring state of the current node.
     *
     * Detailed state rules:
     * 1. A null node is treated as COVERED.
     *    Reason:
     *    - We do not need to place cameras on missing children.
     *    - Treating null as covered avoids forcing unnecessary cameras on leaf parents.
     *
     * 2. If either child returns NEEDS_CAMERA:
     *    - The current node must place a camera.
     *    - This camera will cover:
     *      a) the current node
     *      b) its parent
     *      c) its immediate children
     *
     * 3. Else if either child returns HAS_CAMERA:
     *    - The current node is already covered by that child camera.
     *    - So current node returns COVERED.
     *
     * 4. Else:
     *    - Both children are COVERED, and neither has a camera.
     *    - Therefore current node is not covered by any child camera.
     *    - We do not place a camera immediately; instead we ask the parent to handle it.
     *    - So current node returns NEEDS_CAMERA.
     *
     * @param node the current tree node being processed
     * @return one of the three states: NEEDS_CAMERA, HAS_CAMERA, or COVERED
     * Time complexity: O(n), because across the full traversal each node is visited once.
     * Space complexity: O(h), where h is the recursion depth.
     */
    public int dfs(TreeNode node) {
        /*
         * Base case:
         * A null node does not need monitoring and does not need a camera.
         * We treat it as already covered.
         */
        if (node == null) {
            return COVERED;
        }

        /*
         * Step 1: Process left subtree first.
         * Because this is postorder traversal, we must know the left child's state
         * before deciding what to do at the current node.
         */
        int leftState = dfs(node.left);

        /*
         * Step 2: Process right subtree.
         * Again, we need the right child's state before deciding the current node's state.
         */
        int rightState = dfs(node.right);

        /*
         * Step 3: If at least one child needs a camera, then the current node is the
         * best place to install one.
         *
         * Why is this greedy choice optimal?
         * - A camera at the current node covers the needy child, the current node itself,
         *   and possibly the other child and parent as well.
         * - Placing the camera lower or higher would not cover as efficiently.
         */
        if (leftState == NEEDS_CAMERA || rightState == NEEDS_CAMERA) {
            cameraCount++;
            return HAS_CAMERA;
        }

        /*
         * Step 4: If no child needs a camera, but at least one child already has a camera,
         * then the current node is covered by that child camera.
         *
         * So we do not need to place a camera here.
         */
        if (leftState == HAS_CAMERA || rightState == HAS_CAMERA) {
            return COVERED;
        }

        /*
         * Step 5: If we reach here, both children are COVERED and neither has a camera.
         *
         * That means:
         * - The children are safe
         * - But the current node is not covered by any child camera
         *
         * Therefore, the current node asks its parent to place a camera if needed.
         */
        return NEEDS_CAMERA;
    }

    /**
     * Builds a binary tree from a level-order array representation where null means
     * "no node at this position".
     *
     * Example:
     * [0, 0, null, 0, 0]
     *
     * This helper is used only for demonstration in main.
     *
     * @param values level-order node values, where null represents a missing node
     * @return the root of the constructed binary tree
     * Time complexity: O(n), where n is the number of entries in the array.
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
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstration tree.
     * Space complexity: O(n) for tree construction plus recursion/queue usage.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        /*
         * Example 1:
         * Input: [0,0,null,0,0]
         *
         * Tree shape:
         *       0
         *      /
         *     0
         *    / \
         *   0   0
         *
         * Optimal answer: 1
         * Place one camera on the second node from the top.
         */
        Integer[] example1 = {0, 0, null, 0, 0};
        TreeNode root1 = buildTree(example1);
        int result1 = solution.minCameraCover(root1);
        System.out.println("Example 1 Output: " + result1);
        System.out.println("Expected: 1");

        /*
         * Example 2:
         * Input: [0,0,null,0,null,0,null,null,0]
         *
         * This forms a deeper structure where one camera is not enough.
         * Optimal answer: 2
         */
        Integer[] example2 = {0, 0, null, 0, null, 0, null, null, 0};
        TreeNode root2 = buildTree(example2);
        int result2 = solution.minCameraCover(root2);
        System.out.println("Example 2 Output: " + result2);
        System.out.println("Expected: 2");

        /*
         * Additional quick sanity checks for beginners:
         */

        /*
         * Single node tree:
         * One camera is required because the root must be covered.
         */
        Integer[] singleNode = {1};
        TreeNode root3 = buildTree(singleNode);
        int result3 = solution.minCameraCover(root3);
        System.out.println("Single Node Output: " + result3);
        System.out.println("Expected: 1");

        /*
         * Perfect small tree:
         *       1
         *      / \
         *     2   3
         *
         * One camera at root covers all three nodes.
         */
        Integer[] smallTree = {1, 2, 3};
        TreeNode root4 = buildTree(smallTree);
        int result4 = solution.minCameraCover(root4);
        System.out.println("Small Tree Output: " + result4);
        System.out.println("Expected: 1");
    }
}