import java.util.*;

/*
 * Title: Minimum Relays to Secure Every Root-to-Leaf Route
 * Difficulty: Hard
 * Topic: Trees
 *
 * Problem Description:
 * You are given the root of a binary tree representing a hierarchy of communication towers.
 * Every node is a tower, and every edge is a direct communication link between a tower and
 * one of its children. A security relay can be installed on any tower. If a relay is installed
 * on a tower, then that tower, its parent, and its immediate children are considered secured.
 *
 * Your task is to return the minimum number of relays needed so that every tower in the tree
 * is secured.
 *
 * A tower is secured if at least one of the following is true:
 * 1. A relay is installed on that tower.
 * 2. Its parent has a relay.
 * 3. One of its children has a relay.
 *
 * You must cover the entire tree, including the root and all leaves.
 *
 * The input tree can be assumed to be a standard binary tree where each node has at most two
 * children. Node values are unique integers but are only identifiers; they do not affect the answer.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [1, 100000].
 * - Each node has at most two children.
 * - Node values are in the range [-1000000000, 1000000000].
 * - The solution should run in O(n) time.
 * - Recursive solutions should be careful about stack depth on highly skewed trees.
 *
 * Follow-up:
 * Can you solve it using a postorder traversal with a small number of states per node,
 * instead of trying all placement combinations?
 *
 * Core Idea:
 * We use a postorder traversal and classify each node into one of three states:
 *
 * 0 -> This node is NOT covered.
 * 1 -> This node HAS a relay.
 * 2 -> This node is covered, but does NOT have a relay.
 *
 * Greedy rule during postorder:
 * - If any child is NOT covered (state 0), then we MUST place a relay at the current node.
 * - Else if any child HAS a relay (state 1), then the current node is covered (state 2).
 * - Else both children are covered without relays (or null), so current node is NOT covered (state 0).
 *
 * Important implementation detail:
 * To avoid recursion depth issues on a skewed tree with up to 100000 nodes, we implement
 * the postorder traversal iteratively using an explicit stack.
 */
public class Solution {

    /**
     * Standard binary tree node.
     */
    public static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        /**
         * Constructs a node with the given value.
         *
         * @param val the value stored in the node
         */
        TreeNode(int val) {
            this.val = val;
        }

        /**
         * Constructs a node with value and children.
         *
         * @param val the value stored in the node
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
     * Helper frame used to simulate recursive postorder traversal iteratively.
     * visited == false means:
     *   - first time we see this node
     *   - we still need to process its children first
     *
     * visited == true means:
     *   - both children have already been processed
     *   - now we can compute this node's state from child states
     */
    private static class Frame {
        TreeNode node;
        boolean visited;

        Frame(TreeNode node, boolean visited) {
            this.node = node;
            this.visited = visited;
        }
    }

    /**
     * Returns the minimum number of relays needed to secure every node in the binary tree.
     *
     * We perform an iterative postorder traversal so that each node is processed only after
     * its children have already been processed. For each node, we compute one of three states:
     *
     * 0 -> node is not covered
     * 1 -> node has a relay
     * 2 -> node is covered without having a relay
     *
     * Greedy logic:
     * - If a child is not covered, place a relay here.
     * - Else if a child has a relay, this node is covered.
     * - Else this node is not covered.
     *
     * After traversal, if the root is still not covered, we place one final relay at the root.
     *
     * @param root the root of the binary tree
     * @return the minimum number of relays required to secure the entire tree
     * Time complexity: O(n), where n is the number of nodes, because each node is pushed/popped a constant number of times.
     * Space complexity: O(n), due to the explicit stack and the map storing states.
     */
    public int minRelayCover(TreeNode root) {
        if (root == null) {
            return 0;
        }

        // This counter stores the total number of relays we decide to install.
        int relays = 0;

        // We need each node's computed state after its children are processed.
        // IdentityHashMap is a good fit because tree nodes are distinct objects and
        // we want object-identity-based lookup.
        Map<TreeNode, Integer> state = new IdentityHashMap<>();

        // Explicit stack for iterative postorder traversal.
        Deque<Frame> stack = new ArrayDeque<>();
        stack.push(new Frame(root, false));

        while (!stack.isEmpty()) {
            Frame current = stack.pop();
            TreeNode node = current.node;

            if (node == null) {
                continue;
            }

            if (!current.visited) {
                // First time we see this node.
                // Postorder means:
                //   1. process left subtree
                //   2. process right subtree
                //   3. process current node
                //
                // To simulate that with a stack:
                // - push current node back as "visited = true"
                // - then push right child
                // - then push left child
                //
                // Because stack is LIFO, left and right will be processed before the node itself.
                stack.push(new Frame(node, true));
                stack.push(new Frame(node.right, false));
                stack.push(new Frame(node.left, false));
            } else {
                // Now both children have already been processed, so their states are known.
                //
                // For null children, we treat them as "covered without relay" (state 2).
                // Why?
                // Because null does not need coverage and should not force a relay.
                int leftState = (node.left == null) ? 2 : state.get(node.left);
                int rightState = (node.right == null) ? 2 : state.get(node.right);

                // Detailed greedy decision:
                //
                // Case 1:
                // If either child is NOT covered (state 0),
                // then the current node must install a relay.
                //
                // This is necessary because the only nodes that can cover that child are:
                // - the child itself
                // - the child's parent (which is current node)
                // - the child's own children
                //
                // But since the child's subtree has already been optimally processed and the child
                // still ended up uncovered, placing a relay at current node is the best/necessary move.
                if (leftState == 0 || rightState == 0) {
                    state.put(node, 1);
                    relays++;
                }
                // Case 2:
                // If no child is uncovered, but at least one child has a relay,
                // then current node is covered by that child.
                else if (leftState == 1 || rightState == 1) {
                    state.put(node, 2);
                }
                // Case 3:
                // Otherwise, both children are covered but neither has a relay.
                // That means current node is currently NOT covered by children,
                // and we intentionally leave it uncovered for now so that its parent
                // may decide to place a relay if needed.
                else {
                    state.put(node, 0);
                }
            }
        }

        // After processing the whole tree, the root has no parent.
        // So if the root is still uncovered, we must place one final relay at the root.
        if (state.get(root) == 0) {
            relays++;
        }

        return relays;
    }

    /**
     * Alias method matching a common naming style for this classic problem.
     *
     * @param root the root of the binary tree
     * @return the minimum number of relays required to secure the entire tree
     * Time complexity: O(n), where n is the number of nodes.
     * Space complexity: O(n), due to the explicit traversal stack and state map.
     */
    public int minCameraCover(TreeNode root) {
        return minRelayCover(root);
    }

    /**
     * Builds a binary tree from a level-order array representation.
     *
     * Example:
     * [0, 0, null, 0, 0]
     *
     * Means:
     * - index 0 is root
     * - for node at index i:
     *   left child is at index 2*i + 1
     *   right child is at index 2*i + 2
     *
     * Null entries represent missing nodes.
     *
     * This helper is only for demonstration in main.
     *
     * @param values the level-order array representation, where null means no node
     * @return the root of the constructed binary tree
     * Time complexity: O(n), where n is the array length.
     * Space complexity: O(n), for the intermediate node array.
     */
    public static TreeNode buildTree(Integer[] values) {
        if (values == null || values.length == 0 || values[0] == null) {
            return null;
        }

        TreeNode[] nodes = new TreeNode[values.length];

        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                nodes[i] = new TreeNode(values[i]);
            }
        }

        for (int i = 0; i < values.length; i++) {
            if (nodes[i] == null) {
                continue;
            }

            int leftIndex = 2 * i + 1;
            int rightIndex = 2 * i + 2;

            if (leftIndex < values.length) {
                nodes[i].left = nodes[leftIndex];
            }
            if (rightIndex < values.length) {
                nodes[i].right = nodes[rightIndex];
            }
        }

        return nodes[0];
    }

    /**
     * Demonstrates the solution on the examples from the problem statement.
     *
     * Example 1:
     * root = [0,0,null,0,0]
     * Expected output: 1
     *
     * Example 2:
     * root = [0,0,null,0,null,0,null,null,0]
     * Expected output: 2
     *
     * Note:
     * The second example's array is not a compact complete-array representation,
     * so for clarity and correctness we build that tree manually.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) total for the demonstrated examples.
     * Space complexity: O(n) for building and processing the example trees.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Tree shape:
        //       0
        //      /
        //     0
        //    / \
        //   0   0
        //
        // One relay on the left child secures all nodes.
        TreeNode example1 = new TreeNode(0);
        example1.left = new TreeNode(0);
        example1.left.left = new TreeNode(0);
        example1.left.right = new TreeNode(0);

        int result1 = solution.minRelayCover(example1);
        System.out.println("Example 1 Output: " + result1);
        System.out.println("Example 1 Expected: 1");

        // Example 2:
        // Chain-like structure matching the statement:
        //
        //     0
        //    /
        //   0
        //  /
        // 0
        // /
        // 0
        //  \
        //   0
        //
        // Minimum relays = 2
        TreeNode example2 = new TreeNode(0);
        example2.left = new TreeNode(0);
        example2.left.left = new TreeNode(0);
        example2.left.left.left = new TreeNode(0);
        example2.left.left.left.right = new TreeNode(0);

        int result2 = solution.minRelayCover(example2);
        System.out.println("Example 2 Output: " + result2);
        System.out.println("Example 2 Expected: 2");

        // Additional quick sanity checks:

        // Single node tree:
        // Must place one relay on the root.
        TreeNode single = new TreeNode(42);
        System.out.println("Single Node Output: " + solution.minRelayCover(single));
        System.out.println("Single Node Expected: 1");

        // Perfect small tree:
        //       1
        //      / \
        //     2   3
        // Relays on 2 and/or 3 are not both needed; one relay at root covers all three.
        TreeNode small = new TreeNode(1, new TreeNode(2), new TreeNode(3));
        System.out.println("Small Tree Output: " + solution.minRelayCover(small));
        System.out.println("Small Tree Expected: 1");
    }
}