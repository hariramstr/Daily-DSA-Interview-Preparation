import java.util.*;

/*
Problem Title: Count Hallways With Even Door Endpoints

Problem Description:
You are given the root of a binary tree representing a building map. Each node stores an integer room number.
A hallway is defined as any parent node together with its existing direct child connections.
A hallway is considered valid if the parent room number is even and every existing child of that parent
also has an even room number. Leaf nodes do not count as hallways because they have no child connections.

Return the number of valid hallways in the tree.

In other words, for every non-leaf node, check whether its value is even and whether all of its present
children (left child, right child, or both) also have even values. Count how many nodes satisfy this rule.

This is an easy tree traversal problem. A simple DFS or BFS is enough because each node only needs to be checked once.

Constraints:
- The number of nodes in the tree is in the range [1, 1000].
- -10^4 <= Node.val <= 10^4
- The tree is a binary tree.

Example 1:
Input: root = [8,4,6,2,7,null,10]
Output: 2
Explanation:
Node 8 forms a valid hallway because 8, 4, and 6 are all even.
Node 6 also forms a valid hallway because its existing child 10 is even.
Node 4 does not qualify because one of its children is 7, which is odd.

Example 2:
Input: root = [5,2,8,4,6]
Output: 1
Explanation:
Node 5 is odd, so it cannot form a valid hallway.
Node 2 forms a valid hallway because both of its children, 4, and 6, are even.
The other nodes are leaves.
*/

public class Solution {

    /**
     * Basic binary tree node definition used for this problem.
     */
    public static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        /**
         * Creates a tree node with the given value.
         *
         * @param val the integer value stored in the node
         */
        TreeNode(int val) {
            this.val = val;
        }

        /**
         * Creates a tree node with the given value and children.
         *
         * @param val the integer value stored in the node
         * @param left the left child
         * @param right the right child
         */
        TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }

    /**
     * Counts how many non-leaf nodes form a valid hallway.
     *
     * A node forms a valid hallway if:
     * 1. It is not a leaf (it has at least one child),
     * 2. Its own value is even,
     * 3. Every existing child also has an even value.
     *
     * This method uses Depth-First Search (DFS) to visit every node exactly once.
     *
     * @param root the root of the binary tree
     * @return the number of valid hallways in the tree
     * Time complexity: O(n), where n is the number of nodes in the tree
     * Space complexity: O(h), where h is the height of the tree due to recursion stack
     */
    public int countEvenHallways(TreeNode root) {
        return dfs(root);
    }

    /**
     * Performs a recursive DFS traversal and counts valid hallways in the subtree.
     *
     * The key idea:
     * - First, determine whether the current node itself forms a valid hallway.
     * - Then, recursively count valid hallways in the left subtree.
     * - Then, recursively count valid hallways in the right subtree.
     * - Finally, add all these counts together.
     *
     * @param node the current node being processed
     * @return the number of valid hallways in the subtree rooted at this node
     * Time complexity: O(n), because each node is processed once
     * Space complexity: O(h), where h is the recursion depth
     */
    public int dfs(TreeNode node) {
        // Base case:
        // If the current node is null, there is nothing to process,
        // so it contributes 0 valid hallways.
        if (node == null) {
            return 0;
        }

        // We will store whether the current node itself forms a valid hallway.
        int currentCount = 0;

        // A hallway must have at least one child.
        // So first we check whether this node is a non-leaf node.
        boolean hasAtLeastOneChild = (node.left != null || node.right != null);

        if (hasAtLeastOneChild) {
            // Start by assuming the hallway is valid,
            // then invalidate it if any rule is broken.
            boolean isValidHallway = true;

            // Rule 1: The parent node's value must be even.
            // If it is odd, this node cannot form a valid hallway.
            if (!isEven(node.val)) {
                isValidHallway = false;
            }

            // Rule 2: If a left child exists, its value must also be even.
            if (node.left != null && !isEven(node.left.val)) {
                isValidHallway = false;
            }

            // Rule 3: If a right child exists, its value must also be even.
            if (node.right != null && !isEven(node.right.val)) {
                isValidHallway = false;
            }

            // If all rules passed, count this node as one valid hallway.
            if (isValidHallway) {
                currentCount = 1;
            }
        }

        // Recursively count valid hallways in the left subtree.
        int leftCount = dfs(node.left);

        // Recursively count valid hallways in the right subtree.
        int rightCount = dfs(node.right);

        // Total count for this subtree =
        // current node's contribution + left subtree + right subtree.
        return currentCount + leftCount + rightCount;
    }

    /**
     * Checks whether a number is even.
     *
     * @param value the integer to check
     * @return true if the value is even, otherwise false
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public boolean isEven(int value) {
        return value % 2 == 0;
    }

    /**
     * Builds a binary tree from a level-order array representation.
     *
     * The input format follows the common binary tree array style:
     * - Each element represents a node in level-order.
     * - null means the node is absent.
     *
     * Example:
     * [8, 4, 6, 2, 7, null, 10]
     *
     * This helper is used only for demonstration in main.
     *
     * @param values the level-order representation of the tree
     * @return the root of the constructed binary tree
     * Time complexity: O(n), where n is the number of array elements
     * Space complexity: O(n), due to the queue used during construction
     */
    public TreeNode buildTree(Integer[] values) {
        // If the array is empty or the first value is null,
        // the tree is empty.
        if (values == null || values.length == 0 || values[0] == null) {
            return null;
        }

        // Create the root node from the first value.
        TreeNode root = new TreeNode(values[0]);

        // Queue for level-order construction.
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        // Index points to the next value in the array that we need to process.
        int index = 1;

        // Continue while there are parent nodes waiting in the queue
        // and there are still values left in the input array.
        while (!queue.isEmpty() && index < values.length) {
            // Take the next parent node from the queue.
            TreeNode current = queue.poll();

            // Try to assign the left child if there is a value available.
            if (index < values.length) {
                Integer leftValue = values[index];
                index++;

                // Only create a child node if the value is not null.
                if (leftValue != null) {
                    current.left = new TreeNode(leftValue);
                    queue.offer(current.left);
                }
            }

            // Try to assign the right child if there is a value available.
            if (index < values.length) {
                Integer rightValue = values[index];
                index++;

                // Only create a child node if the value is not null.
                if (rightValue != null) {
                    current.right = new TreeNode(rightValue);
                    queue.offer(current.right);
                }
            }
        }

        return root;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It builds the trees, runs the algorithm, and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) overall for each demonstration tree
     * Space complexity: O(n) due to tree construction and traversal support
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Tree: [8,4,6,2,7,null,10]
        //
        // Structure:
        //         8
        //       /   \
        //      4     6
        //     / \     \
        //    2   7     10
        //
        // Valid hallways:
        // - Node 8: even, children 4 and 6 are even -> valid
        // - Node 4: even, but child 7 is odd -> invalid
        // - Node 6: even, child 10 is even -> valid
        // Total = 2
        Integer[] example1 = {8, 4, 6, 2, 7, null, 10};
        TreeNode root1 = solution.buildTree(example1);
        int result1 = solution.countEvenHallways(root1);
        System.out.println("Example 1 Output: " + result1); // Expected: 2

        // Example 2:
        // Tree: [5,2,8,4,6]
        //
        // Structure:
        //         5
        //       /   \
        //      2     8
        //     / \
        //    4   6
        //
        // Valid hallways:
        // - Node 5: odd -> invalid
        // - Node 2: even, children 4 and 6 are even -> valid
        // - Node 8: leaf -> does not count
        // Total = 1
        Integer[] example2 = {5, 2, 8, 4, 6};
        TreeNode root2 = solution.buildTree(example2);
        int result2 = solution.countEvenHallways(root2);
        System.out.println("Example 2 Output: " + result2); // Expected: 1

        // Additional small demonstration:
        // Single node tree: [2]
        // This node is a leaf, so it does not count as a hallway.
        Integer[] example3 = {2};
        TreeNode root3 = solution.buildTree(example3);
        int result3 = solution.countEvenHallways(root3);
        System.out.println("Single Node Output: " + result3); // Expected: 0
    }
}