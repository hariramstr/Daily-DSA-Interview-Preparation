import java.util.*;

/*
Problem Title: Find the Largest Uniform-Value Subtree

Problem Description:
You are given the root of a binary tree where each node stores an integer category code.
A subtree is called uniform if every node inside that subtree has the same value.
A subtree may consist of a single node, which is always considered uniform.

Return the size (number of nodes) of the largest uniform subtree in the tree.

Your task is to examine every possible subtree rooted at each node and determine whether
all nodes below that root, including the root itself, share the same value. If they do,
that subtree is uniform and contributes its total node count as a candidate answer.
If not, you must continue checking smaller subtrees.

This problem is intended to test recursive tree reasoning and bottom-up aggregation of
information from child subtrees. An efficient solution should avoid re-checking the same
nodes many times.

Constraints:
- The number of nodes in the tree is in the range [1, 10^5].
- Node values are in the range [-10^9, 10^9].
- The tree is binary, and may be unbalanced.
- Your solution should run in O(n) time.

Example 1:
Input: root = [5,5,5,5,5,null,5]
Output: 6
Explanation: Every existing node has value 5, so the entire tree is a uniform subtree with 6 nodes.

Example 2:
Input: root = [3,3,4,3,null,4,4]
Output: 3
Explanation: The subtree rooted at the right child of the root contains three nodes, all with value 4,
so its size is 3. The whole tree is not uniform because it contains both 3 and 4.
*/

public class Solution {

    /**
     * Basic binary tree node definition used by the solution.
     */
    public static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        /**
         * Creates a node with the given value.
         *
         * @param val the integer value stored in the node
         */
        TreeNode(int val) {
            this.val = val;
        }
    }

    /**
     * Helper object returned by the recursive post-order traversal.
     *
     * It stores:
     * 1. whether the subtree rooted at the current node is uniform
     * 2. how many nodes are in that subtree
     *
     * We do not need to store the subtree value separately because if a subtree is uniform,
     * then its root value already represents the value of the entire subtree.
     */
    private static class Info {
        boolean isUniform;
        int size;

        /**
         * Creates a result object for one subtree.
         *
         * @param isUniform whether the subtree is uniform
         * @param size total number of nodes in the subtree
         */
        Info(boolean isUniform, int size) {
            this.isUniform = isUniform;
            this.size = size;
        }
    }

    /**
     * Stores the best answer found so far while traversing the tree.
     * Specifically, this is the size of the largest uniform subtree encountered.
     */
    private int maxUniformSize;

    /**
     * Computes the size of the largest uniform-value subtree in the given binary tree.
     *
     * The algorithm works bottom-up:
     * - First solve the left subtree
     * - Then solve the right subtree
     * - Then decide whether the current node can form a larger uniform subtree together with its children
     *
     * A subtree rooted at a node is uniform if:
     * - its left subtree is uniform
     * - its right subtree is uniform
     * - if a left child exists, its value equals the current node's value
     * - if a right child exists, its value equals the current node's value
     *
     * If all these conditions hold, then the current subtree is uniform and its size is:
     * 1 + left subtree size + right subtree size
     *
     * We update the global maximum whenever we find such a subtree.
     *
     * @param root the root of the binary tree
     * @return the number of nodes in the largest uniform subtree
     * Time complexity: O(n), because each node is processed exactly once.
     * Space complexity: O(h), where h is the height of the tree due to recursion stack;
     * in the worst case of a skewed tree, this can be O(n).
     */
    public int largestUniformSubtree(TreeNode root) {
        maxUniformSize = 0;
        dfs(root);
        return maxUniformSize;
    }

    /**
     * Performs a post-order traversal and returns information about the subtree rooted at {@code node}.
     *
     * Step-by-step logic:
     * 1. Recursively gather information from the left child.
     * 2. Recursively gather information from the right child.
     * 3. Compute the total size of the current subtree.
     * 4. Check whether the current subtree is uniform:
     *    - left subtree must be uniform
     *    - right subtree must be uniform
     *    - if left child exists, left child value must equal current node value
     *    - if right child exists, right child value must equal current node value
     * 5. If uniform:
     *    - update the global answer with the current subtree size
     *    - return uniform=true and the subtree size
     * 6. Otherwise:
     *    - return uniform=false and the subtree size
     *
     * Important detail:
     * A null subtree is treated as uniform with size 0.
     * This makes leaf-node handling very clean:
     * - both children are null
     * - both null subtrees are uniform
     * - therefore the leaf itself is uniform with size 1
     *
     * @param node the current subtree root being processed
     * @return an {@code Info} object describing whether this subtree is uniform and how large it is
     * Time complexity: O(1) work per node, so O(n) overall across the full traversal.
     * Space complexity: O(h) recursion stack, where h is the tree height.
     */
    private Info dfs(TreeNode node) {
        // Base case:
        // An empty subtree is considered uniform.
        // Its size is 0 because it contains no nodes.
        if (node == null) {
            return new Info(true, 0);
        }

        // Recursively solve the left subtree first.
        // Because this is post-order traversal, we fully understand the left subtree
        // before trying to evaluate the current node.
        Info leftInfo = dfs(node.left);

        // Recursively solve the right subtree.
        // After this call returns, we also fully understand the right subtree.
        Info rightInfo = dfs(node.right);

        // The total number of nodes in the current subtree is:
        // current node itself (1)
        // + all nodes in the left subtree
        // + all nodes in the right subtree
        int currentSize = 1 + leftInfo.size + rightInfo.size;

        // Start by assuming the current subtree can be uniform.
        // We will invalidate this assumption if any rule is broken.
        boolean isUniform = true;

        // Rule 1:
        // The left subtree itself must already be uniform.
        // If it is not, then the current subtree cannot possibly be uniform.
        if (!leftInfo.isUniform) {
            isUniform = false;
        }

        // Rule 2:
        // The right subtree itself must already be uniform.
        if (!rightInfo.isUniform) {
            isUniform = false;
        }

        // Rule 3:
        // If a left child exists, then because the left subtree is uniform,
        // every node in that left subtree has the same value as node.left.val.
        // Therefore, to merge it with the current node into one larger uniform subtree,
        // node.left.val must equal node.val.
        if (node.left != null && node.left.val != node.val) {
            isUniform = false;
        }

        // Rule 4:
        // Same reasoning for the right child.
        if (node.right != null && node.right.val != node.val) {
            isUniform = false;
        }

        // If all conditions passed, then the subtree rooted at this node is uniform.
        if (isUniform) {
            // Update the best answer seen anywhere in the tree.
            maxUniformSize = Math.max(maxUniformSize, currentSize);

            // Return that this subtree is uniform, along with its total size.
            return new Info(true, currentSize);
        }

        // Otherwise, this subtree is not uniform.
        // We still return its size because the parent may need subtree sizes for counting,
        // even though the parent cannot merge through a non-uniform child.
        return new Info(false, currentSize);
    }

    /**
     * Builds a binary tree from a level-order array representation where {@code null}
     * means the corresponding child is absent.
     *
     * Example:
     * [5, 5, 5, 5, 5, null, 5]
     *
     * This method is only for demonstration in {@code main}; it is not required by the core algorithm.
     *
     * @param values the level-order representation of the tree
     * @return the root of the constructed binary tree, or {@code null} if the input is empty
     * Time complexity: O(n), where n is the number of array entries.
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
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It constructs the trees, runs the algorithm, and prints the results.
     * The expected outputs are:
     * - Example 1: 6
     * - Example 2: 3
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstrated test case.
     * Space complexity: O(n) for tree construction plus O(h) recursion stack during solving.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Tree: [5,5,5,5,5,null,5]
        // All existing nodes have value 5, so the entire tree is uniform.
        // Total nodes = 6, so expected answer = 6.
        Integer[] example1 = {5, 5, 5, 5, 5, null, 5};
        TreeNode root1 = solution.buildTree(example1);
        int result1 = solution.largestUniformSubtree(root1);
        System.out.println("Example 1 Output: " + result1); // Expected: 6

        // Example 2:
        // Tree: [3,3,4,3,null,4,4]
        //
        // Structure:
        //         3
        //       /   \
        //      3     4
        //     /     / \
        //    3     4   4
        //
        // The right subtree rooted at value 4 contains three nodes, all equal to 4.
        // So the largest uniform subtree size is 3.
        Integer[] example2 = {3, 3, 4, 3, null, 4, 4};
        TreeNode root2 = solution.buildTree(example2);
        int result2 = solution.largestUniformSubtree(root2);
        System.out.println("Example 2 Output: " + result2); // Expected: 3
    }
}