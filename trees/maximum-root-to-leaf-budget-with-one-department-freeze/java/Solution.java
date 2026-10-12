import java.util.*;

/*
 * Title: Maximum Root-to-Leaf Budget With One Department Freeze
 * Difficulty: Medium
 * Topic: Trees
 *
 * Problem Description:
 * You are given the hierarchy of a company as a binary tree. Each node represents a department,
 * and its integer value represents the net budget contribution of that department for the quarter.
 * The value may be positive (profit) or negative (loss). A valid reporting chain is any path from
 * the root department down to a leaf department.
 *
 * The finance team is allowed to apply at most one "department freeze" on a reporting chain.
 * Freezing a department means you may skip the contribution of exactly one node on the chosen
 * root-to-leaf path, treating its value as 0 instead of adding it to the path total.
 * You may also choose not to use the freeze.
 *
 * Return the maximum possible budget total among all root-to-leaf paths after applying at most
 * one freeze.
 *
 * A leaf is a node with no children. The freeze can be used on the root, an internal node, or a
 * leaf, but at most once on the chosen path.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [1, 100000].
 * - -100000 <= Node.val <= 100000
 * - The tree is a binary tree.
 * - The answer fits in a 64-bit signed integer.
 *
 * Example 1:
 * Input: root = [5,4,8,-3,null,6,2]
 * Output: 19
 * Explanation:
 * Without a freeze, the best root-to-leaf path is 5 -> 8 -> 6 = 19.
 * Freezing node 8, that path becomes 5 -> 0 -> 6 = 11, which is worse.
 * Freezing node 4 on path 5 -> 4 -> -3, the total becomes 2.
 * Freezing node 2 on path 5 -> 8 -> 2 gives 13.
 * The best overall path is still 19, so the correct output is 19.
 *
 * Example 2:
 * Input: root = [7,-5,3,4,2,-6,9]
 * Output: 19
 * Explanation:
 * Path 7 -> -5 -> 4 has sum 6. Freezing -5 makes it 11.
 * Path 7 -> 3 -> 9 has sum 19 without using the freeze.
 * Path 7 -> 3 -> -6 has sum 4, or 10 if -6 is frozen.
 * The maximum achievable root-to-leaf total is 19.
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
         * @param val the department budget contribution stored in this node
         */
        TreeNode(int val) {
            this.val = val;
        }
    }

    /**
     * Small helper object that stores the two DP states for one node:
     *
     * noFreeze:
     *   Best possible sum from this node down to any leaf in its subtree
     *   when we have NOT used the freeze yet on this path.
     *
     * usedFreeze:
     *   Best possible sum from this node down to any leaf in its subtree
     *   when we HAVE ALREADY used the freeze somewhere on this path
     *   (possibly at this node, or deeper below).
     */
    private static class State {
        long noFreeze;
        long usedFreeze;

        State(long noFreeze, long usedFreeze) {
            this.noFreeze = noFreeze;
            this.usedFreeze = usedFreeze;
        }
    }

    /**
     * Computes the maximum root-to-leaf sum when we may freeze at most one node
     * on the chosen path, turning that node's contribution into 0.
     *
     * Core idea:
     * For every node, compute two values bottom-up:
     * 1) noFreeze  = best sum from this node to a leaf if freeze is still available
     * 2) usedFreeze = best sum from this node to a leaf if freeze has already been used
     *
     * Transition:
     * - If the node is a leaf:
     *     noFreeze   = max(node.val, 0)   // either keep it or freeze it here
     *     usedFreeze = node.val           // freeze already used earlier, so must take node.val
     *
     * - If the node is internal:
     *     usedFreeze = node.val + max(child.usedFreeze)
     *       because freeze is already gone, so we must include current node value.
     *
     *     noFreeze = max(
     *         node.val + max(child.noFreeze),   // do not freeze current node
     *         0 + max(child.usedFreeze)         // freeze current node now
     *     )
     *
     * We use an iterative postorder traversal to avoid stack overflow on deep trees,
     * because the tree can contain up to 100000 nodes.
     *
     * @param root the root of the company hierarchy tree
     * @return the maximum achievable root-to-leaf budget total using at most one freeze
     * Time complexity: O(n), where n is the number of nodes
     * Space complexity: O(n), for the traversal stack and DP map
     */
    public long maximumRootToLeafBudgetWithOneFreeze(TreeNode root) {
        if (root == null) {
            return 0L;
        }

        // We store computed DP states for each node after both children are processed.
        Map<TreeNode, State> dp = new IdentityHashMap<>();

        // Iterative postorder traversal:
        // visited = false  -> first time we see the node, push children first
        // visited = true   -> children already processed, now compute this node's DP
        Deque<Object[]> stack = new ArrayDeque<>();
        stack.push(new Object[]{root, false});

        while (!stack.isEmpty()) {
            Object[] entry = stack.pop();
            TreeNode node = (TreeNode) entry[0];
            boolean visited = (Boolean) entry[1];

            if (node == null) {
                continue;
            }

            if (!visited) {
                // Postorder means:
                // 1) push current node back as visited=true
                // 2) then push right child
                // 3) then push left child
                // So when we later pop current node with visited=true,
                // both children have already been processed and stored in dp.
                stack.push(new Object[]{node, true});
                stack.push(new Object[]{node.right, false});
                stack.push(new Object[]{node.left, false});
            } else {
                // At this point, left and right child states (if children exist)
                // are already available in the dp map.

                boolean isLeaf = node.left == null && node.right == null;

                if (isLeaf) {
                    // Leaf case:
                    // If freeze is still available, we can either:
                    // - keep this leaf: value = node.val
                    // - freeze this leaf: value = 0
                    long noFreeze = Math.max((long) node.val, 0L);

                    // If freeze has already been used earlier, we must include this leaf value.
                    long usedFreeze = node.val;

                    dp.put(node, new State(noFreeze, usedFreeze));
                    continue;
                }

                // We need the best child result among existing children only.
                // Use a very small sentinel for impossible states.
                long bestChildNoFreeze = Long.MIN_VALUE / 4;
                long bestChildUsedFreeze = Long.MIN_VALUE / 4;

                if (node.left != null) {
                    State leftState = dp.get(node.left);
                    bestChildNoFreeze = Math.max(bestChildNoFreeze, leftState.noFreeze);
                    bestChildUsedFreeze = Math.max(bestChildUsedFreeze, leftState.usedFreeze);
                }

                if (node.right != null) {
                    State rightState = dp.get(node.right);
                    bestChildNoFreeze = Math.max(bestChildNoFreeze, rightState.noFreeze);
                    bestChildUsedFreeze = Math.max(bestChildUsedFreeze, rightState.usedFreeze);
                }

                // State 1: freeze already used somewhere above or below.
                // Since it is already used, we must include current node's value.
                long usedFreeze = (long) node.val + bestChildUsedFreeze;

                // Option A: do not freeze current node.
                // Then current value is included, and children still have freeze available.
                long keepCurrent = (long) node.val + bestChildNoFreeze;

                // Option B: freeze current node right now.
                // Current node contributes 0, and from child onward the freeze is considered used.
                long freezeCurrent = bestChildUsedFreeze;

                // If freeze is still available at this node, choose the better of:
                // - keeping current node
                // - freezing current node
                long noFreeze = Math.max(keepCurrent, freezeCurrent);

                dp.put(node, new State(noFreeze, usedFreeze));
            }
        }

        // At the root, freeze is initially available, so answer is root.noFreeze.
        return dp.get(root).noFreeze;
    }

    /**
     * Builds a binary tree from a level-order array representation where null means
     * "no node at this position".
     *
     * Example:
     * [5, 4, 8, -3, null, 6, 2]
     *
     * corresponds to:
     *         5
     *       /   \
     *      4     8
     *     /     / \
     *   -3     6   2
     *
     * @param values level-order node values, where null represents a missing node
     * @return the root of the constructed binary tree
     * Time complexity: O(n), where n is the number of array entries
     * Space complexity: O(n), for the queue used during construction
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
     * Demonstrates the solution on the sample inputs from the problem statement
     * and a few extra sanity checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total nodes across demo trees)
     * Space complexity: O(total nodes across demo trees)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Tree: [5,4,8,-3,null,6,2]
        // Best path is 5 -> 8 -> 6 = 19, and using a freeze does not improve it.
        Integer[] example1 = {5, 4, 8, -3, null, 6, 2};
        TreeNode root1 = solution.buildTree(example1);
        long result1 = solution.maximumRootToLeafBudgetWithOneFreeze(root1);
        System.out.println("Example 1 Output: " + result1); // Expected: 19

        // Example 2:
        // Tree: [7,-5,3,4,2,-6,9]
        // Best path is 7 -> 3 -> 9 = 19.
        Integer[] example2 = {7, -5, 3, 4, 2, -6, 9};
        TreeNode root2 = solution.buildTree(example2);
        long result2 = solution.maximumRootToLeafBudgetWithOneFreeze(root2);
        System.out.println("Example 2 Output: " + result2); // Expected: 19

        // Extra test 1:
        // Single negative node. We can freeze it, so answer should be 0.
        Integer[] extra1 = {-10};
        TreeNode root3 = solution.buildTree(extra1);
        long result3 = solution.maximumRootToLeafBudgetWithOneFreeze(root3);
        System.out.println("Extra Test 1 Output: " + result3); // Expected: 0

        // Extra test 2:
        // Path 1 -> -100 -> 5
        // Without freeze = -94
        // Freeze -100 => 6
        Integer[] extra2 = {1, -100, null, 5};
        TreeNode root4 = solution.buildTree(extra2);
        long result4 = solution.maximumRootToLeafBudgetWithOneFreeze(root4);
        System.out.println("Extra Test 2 Output: " + result4); // Expected: 6

        // Extra test 3:
        // Tree:
        //      2
        //     / \
        //   -1   3
        // Best without freeze: 2 + 3 = 5
        // Freeze not needed.
        Integer[] extra3 = {2, -1, 3};
        TreeNode root5 = solution.buildTree(extra3);
        long result5 = solution.maximumRootToLeafBudgetWithOneFreeze(root5);
        System.out.println("Extra Test 3 Output: " + result5); // Expected: 5
    }
}