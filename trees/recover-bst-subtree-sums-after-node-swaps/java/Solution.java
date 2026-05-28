```java
/*
 * Title: Recover Binary Search Tree Subtree Sums After Node Swaps
 * Difficulty: Hard
 * Topic: Trees
 *
 * Problem Description:
 * You are given the root of a Binary Search Tree (BST) where exactly two nodes have been swapped,
 * violating the BST property. Your task is to restore the BST to its original valid state and then
 * return an array of subtree sums for every node in the tree, listed in level-order (BFS) traversal.
 *
 * The subtree sum of a node is defined as the sum of all node values in the subtree rooted at that
 * node (including the node itself).
 *
 * You must first identify and swap back the two incorrectly placed nodes to restore the BST,
 * then compute the subtree sum for each node.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [2, 1000].
 * - Each node value is a unique integer in the range [-10^6, 10^6].
 * - It is guaranteed that exactly two nodes have been swapped.
 * - The tree is not necessarily balanced.
 *
 * Example 1:
 * Input: root = [3,1,4,null,null,2,null] (BST with nodes 3 and 2 swapped)
 * After recovery: [2,1,4,null,null,3,null]
 * Output: [10, 1, 7, 3]
 *
 * Example 2:
 * Input: root = [5,3,8,1,7,6,9] (nodes 7 and 6 are swapped)
 * After recovery: [5,3,8,1,6,7,9]
 * Output: [39, 10, 24, 1, 6, 16, 9]
 */

import java.util.*;

/**
 * Solution class for recovering a BST with two swapped nodes and computing subtree sums.
 *
 * <p>The approach:
 * 1. Use in-order traversal to find the two swapped nodes (in a valid BST, in-order gives sorted order).
 * 2. Swap the values back to restore the BST.
 * 3. Use BFS (level-order traversal) to visit each node.
 * 4. For each node, compute the subtree sum using a recursive helper.
 * 5. Return the subtree sums in level-order.
 */
public class Solution {

    /**
     * TreeNode definition for a binary tree node.
     */
    static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode(int val) {
            this.val = val;
        }

        TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }

    // These two fields hold references to the two swapped nodes found during in-order traversal.
    // 'first' is the first violation node (larger value appearing before smaller in in-order).
    // 'second' is the second violation node (smaller value appearing after larger in in-order).
    private TreeNode first = null;
    private TreeNode second = null;

    // 'prev' keeps track of the previously visited node during in-order traversal.
    // In a valid BST, prev.val should always be less than current.val during in-order traversal.
    private TreeNode prev = null;

    /**
     * Main entry point: recovers the BST and returns subtree sums in level-order.
     *
     * <p>Algorithm Steps:
     * 1. Perform in-order traversal to detect the two swapped nodes.
     * 2. Swap their values to restore the BST.
     * 3. Perform BFS to get nodes in level-order.
     * 4. For each node in level-order, compute its subtree sum.
     * 5. Return the list of subtree sums.
     *
     * @param root The root of the corrupted BST (with exactly two nodes swapped).
     * @return A list of subtree sums for each node in level-order (BFS) traversal.
     *         Time complexity: O(n^2) in the worst case — O(n) for recovery + O(n) BFS
     *                          + O(n) per node for subtree sum = O(n^2) overall.
     *         Space complexity: O(n) for the recursion stack and BFS queue.
     */
    public List<Long> recoverAndComputeSubtreeSums(TreeNode root) {
        // -----------------------------------------------------------------------
        // STEP 1: Reset the state variables before starting the traversal.
        // This is important if the method is called multiple times on different inputs.
        // -----------------------------------------------------------------------
        first = null;
        second = null;
        prev = null;

        // -----------------------------------------------------------------------
        // STEP 2: Perform in-order traversal to find the two swapped nodes.
        // In a valid BST, in-order traversal yields values in strictly increasing order.
        // If two nodes are swapped, there will be one or two "inversions" (places where
        // prev.val > current.val).
        //
        // Case A (adjacent nodes swapped): Only one inversion occurs.
        //   - 'first' = the node with the larger value (prev at the inversion point)
        //   - 'second' = the node with the smaller value (current at the inversion point)
        //
        // Case B (non-adjacent nodes swapped): Two inversions occur.
        //   - 'first' = prev at the FIRST inversion
        //   - 'second' = current at the SECOND inversion
        // -----------------------------------------------------------------------
        inOrderTraversal(root);

        // -----------------------------------------------------------------------
        // STEP 3: Swap the values of the two identified nodes to restore the BST.
        // We only swap the VALUES, not the node references, to keep the tree structure intact.
        // -----------------------------------------------------------------------
        if (first != null && second != null) {
            // Swap the values of the two incorrectly placed nodes
            int temp = first.val;
            first.val = second.val;
            second.val = temp;
        }

        // -----------------------------------------------------------------------
        // STEP 4: Perform BFS (level-order traversal) to collect nodes in level-order.
        // We use a Queue (LinkedList) to process nodes level by level.
        // -----------------------------------------------------------------------
        List<Long> result = new ArrayList<>();

        if (root == null) {
            return result; // Edge case: empty tree
        }

        // BFS queue to traverse the tree level by level
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root); // Start BFS from the root

        // Process nodes level by level
        while (!queue.isEmpty()) {
            // Dequeue the front node
            TreeNode current = queue.poll();

            // -----------------------------------------------------------------------
            // STEP 5: For each node encountered in BFS order, compute its subtree sum.
            // The subtree sum = sum of all values in the subtree rooted at 'current'.
            // -----------------------------------------------------------------------
            long subtreeSum = computeSubtreeSum(current);
            result.add(subtreeSum);

            // Enqueue left child if it exists
            if (current.left != null) {
                queue.offer(current.left);
            }

            // Enqueue right child if it exists
            if (current.right != null) {
                queue.offer(current.right);
            }
        }

        // Return the list of subtree sums in level-order
        return result;
    }

    /**
     * Performs an in-order traversal of the BST to identify the two swapped nodes.
     *
     * <p>In-order traversal visits nodes in the order: left subtree → current node → right subtree.
     * For a valid BST, this produces values in strictly increasing order.
     * Any violation (prev.val > current.val) indicates a swapped node.
     *
     * @param node The current node being visited during traversal.
     *             Time complexity: O(n) where n is the number of nodes.
     *             Space complexity: O(h) where h is the height of the tree (recursion stack).
     */
    private void inOrderTraversal(TreeNode node) {
        // Base case: if the node is null, return immediately
        if (node == null) {
            return;
        }

        // Traverse the left subtree first (in-order: left → current → right)
        inOrderTraversal(node.left);

        // -----------------------------------------------------------------------
        // Check for a violation: in a valid BST, prev.val < node.val during in-order.
        // If prev.val >= node.val, we found an inversion (a swapped node pair).
        // -----------------------------------------------------------------------
        if (prev != null && prev.val > node.val) {
            // This is a violation point.

            if (first == null) {
                // FIRST violation found:
                // 'prev' is the node with the larger value that should be smaller.
                // We record 'prev' as the 'first' swapped node.
                // We also tentatively set 'second' to 'node' in case this is the only inversion
                // (adjacent nodes swapped case).
                first = prev;
                second = node;
            } else {
                // SECOND violation found:
                // 'node' is the node with the smaller value that should be larger.
                // We update 'second' to 'node'.
                // 'first' remains as set during the first violation.
                second = node;
            }
        }

        // Update 'prev' to the current node before moving to the right subtree
        prev = node;

        // Traverse the right subtree
        inOrderTraversal(node.right);
    }

    /**
     * Computes the subtree sum for a given node.
     *
     * <p>The subtree sum is the sum of all node values in the subtree rooted at the given node,
     * including the node's own value.
     *
     * @param node The root of the subtree for which to compute the sum.
     * @return The sum of all values in the subtree rooted at 'node'.
     *         Returns 0 if 'node' is null.
     *         Time complexity: O(k) where k is the number of nodes in the subtree.
     *         Space complexity: O(h) where h is the height of the subtree (recursion stack).
     */
    private long computeSubtreeSum(TreeNode node) {
        // Base case: an empty subtree has a sum of 0
        if (node == null) {
            return 0;
        }

        // The subtree sum = current node's value + sum of left subtree + sum of right subtree
        return (long) node.val
                + computeSubtreeSum(node.left)
                + computeSubtreeSum(node.right);
    }

    /**
     * Helper method to build a binary tree from a level-order array representation.
     * 'null' values in the array represent missing nodes.
     *
     * @param values An array of Integer values representing the tree in level-order.
     *               Null entries represent absent nodes.
     * @return The root of the constructed binary tree.
     *         Time complexity: O(n) where n is the length of the array.
     *         Space complexity: O(n) for the queue used during construction.
     */
    public static TreeNode buildTree(Integer[] values) {
        // Edge case: empty array or first element is null means no tree
        if (values == null || values.length == 0 || values[0] == null) {
            return null;
        }

        // Create the root node from the first element
        TreeNode root = new TreeNode(values[0]);

        // Use a queue to keep track of nodes whose children need to be assigned
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        int i = 1; // Index into the values array, starting from the second element

        // Process each node in the queue and assign its children
        while (!queue.isEmpty() && i < values.length) {
            TreeNode current = queue.poll();

            // Assign the left child if the next value is not null
            if (i < values.length) {
                if (values[i] != null) {
                    current.left = new TreeNode(values[i]);
                    queue.offer(current.left); // Enqueue for future child assignment
                }
                i++;
            }

            // Assign the right child if the next value is not null
            if (i < values.length) {
                if (values[i] != null) {
                    current.right = new TreeNode(values[i]);
                    queue.offer(current.right); // Enqueue for future child assignment
                }
                i++;
            }
        }

        return root;
    }

    /**
     * Helper method to print the tree structure for debugging purposes.
     * Prints the tree in level-order with node values.
     *
     * @param root The root of the tree to print.
     */
    public static void printTree(TreeNode root) {
        if (root == null) {
            System.out.println("Empty tree");
            return;
        }

        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        System.out.print("Tree (level-order): [");
        boolean first = true;

        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();

            if (!first) System.out.print(", ");
            first = false;

            if (node == null) {
                System.out.print("null");
            } else {
                System.out.print(node.val);
                queue.offer(node.left);
                queue.offer(node.right);
            }
        }
        System.out.println("]");
    }

    /**
     * Main method demonstrating the solution with sample inputs from the problem description.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // Input BST (corrupted): [3, 1, 4, null, null, 2, null]
        //
        //        3          <- should be 2 (swapped with 2)
        //       / \
        //      1   4
        //         /
        //        2          <- should be 3 (swapped with 3)
        //
        // After recovery: [2, 1, 4, null, null, 3, null]
        //        2
        //       / \
        //      1   4
        //         /
        //        3
        //
        // Expected subtree sums in level-order:
        //   node(2): 2+1+4+3 = 10
        //   node(1): 1
        //   node(4): 4+3 = 7
        //   node(3): 3
        // Expected Output: [10, 1, 7, 3]
        // -----------------------------------------------------------------------
        System.out.println("=== Example 1 ===");
        System.out.println("Input BST (corrupted): [3, 1, 4, null, null, 2, null]");

        // Build the corrupted BST
        // Level-order: 3 is root, 1 is left child of 3, 4 is right child of 3,
        // null is left child of 1, null is right child of 1, 2 is left child of 4
        TreeNode root1 = buildTree(new Integer[]{3, 1, 4, null, null, 2, null});

        System.out.print("Before recovery - ");
        printTree(root1);

        // Recover and compute subtree sums
        List<Long> result1 = solution.recoverAndComputeSubtreeSums(root1);

        System.out.print("After recovery - ");
        printTree(root1);

        System.out.println("Subtree sums (level-order): " + result1);
        System.out.println("Expected:                   [10, 1, 7, 3]");
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2:
        // Input BST (corrupted):