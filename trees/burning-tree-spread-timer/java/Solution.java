/*
 * Burning Tree Spread Timer
 * ========================
 * You are given the root of a binary tree where each node has a unique integer value.
 * A fire starts at a specific node (identified by its value) at time 0.
 * Every second, the fire spreads to all adjacent nodes — the left child, right child,
 * and parent of any currently burning node.
 *
 * Return the minimum number of seconds it takes for the entire tree to be on fire.
 *
 * Problem Details:
 * - The fire starts at the node with value `start`.
 * - At each second, fire spreads simultaneously to all unburned neighbors (parent, left child, right child).
 * - All nodes must be burning for the process to complete.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [1, 10^4].
 * - 1 <= Node.val <= 10^4
 * - All node values are unique.
 * - The start node is guaranteed to exist in the tree.
 * - 1 <= start <= 10^4
 *
 * Example 1:
 *   Input: root = [1,2,3,4,5,null,6], start = 2
 *   Output: 3
 *
 * Example 2:
 *   Input: root = [1,2,3], start = 1
 *   Output: 1
 */

import java.util.*;

/**
 * Solution for the Burning Tree Spread Timer problem.
 *
 * <p>Approach:
 * 1. Convert the binary tree into an undirected graph (adjacency list) so we can
 *    traverse parent edges as well as child edges.
 * 2. Perform a BFS (Breadth-First Search) starting from the `start` node.
 * 3. The BFS level count gives us the minimum time for the fire to reach every node.
 */
public class Solution {

    /**
     * Definition for a binary tree node.
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

    /**
     * Builds an undirected graph from the binary tree by recording adjacency
     * (parent <-> child relationships) for every node.
     *
     * @param node    the current tree node being processed
     * @param parent  the parent of the current node (null if root)
     * @param graph   the adjacency list map being populated (node value -> list of neighbor values)
     *
     * Time complexity:  O(N) — visits every node exactly once
     * Space complexity: O(N) — stores up to 2*N edges total (each edge stored twice)
     */
    private void buildGraph(TreeNode node, TreeNode parent, Map<Integer, List<Integer>> graph) {
        // Base case: if the node is null, there is nothing to process
        if (node == null) {
            return;
        }

        // Ensure this node has an entry in the graph even if it has no neighbors
        graph.putIfAbsent(node.val, new ArrayList<>());

        // If there is a parent, add a bidirectional edge between parent and current node
        if (parent != null) {
            // Current node can reach its parent
            graph.get(node.val).add(parent.val);
            // Parent can reach the current node
            graph.putIfAbsent(parent.val, new ArrayList<>());
            graph.get(parent.val).add(node.val);
        }

        // Recursively build the graph for the left subtree
        buildGraph(node.left, node, graph);

        // Recursively build the graph for the right subtree
        buildGraph(node.right, node, graph);
    }

    /**
     * Returns the minimum number of seconds it takes for the entire binary tree
     * to be on fire, given that the fire starts at the node with value {@code start}.
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Build an undirected graph from the tree so parent edges are traversable.</li>
     *   <li>BFS from the start node, treating the problem like "shortest path to all nodes".</li>
     *   <li>The number of BFS levels minus 1 equals the answer (time in seconds).</li>
     * </ol>
     *
     * @param root  the root of the binary tree
     * @param start the value of the node where the fire begins
     * @return the minimum number of seconds for the entire tree to burn
     *
     * Time complexity:  O(N) — building the graph is O(N), BFS visits each node once O(N)
     * Space complexity: O(N) — graph storage O(N), BFS queue O(N), visited set O(N)
     */
    public int amountOfTime(TreeNode root, int start) {
        // -----------------------------------------------------------------------
        // Step 1: Build an undirected adjacency-list graph from the binary tree.
        //         This lets us move "upward" to a parent just like moving to a child.
        // -----------------------------------------------------------------------
        Map<Integer, List<Integer>> graph = new HashMap<>();
        buildGraph(root, null, graph);

        // -----------------------------------------------------------------------
        // Step 2: BFS from the start node.
        //         We use a queue for BFS and a visited set to avoid revisiting nodes.
        // -----------------------------------------------------------------------
        Queue<Integer> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();

        // Enqueue the starting node and mark it as visited (burning at time 0)
        queue.offer(start);
        visited.add(start);

        // `time` tracks how many seconds have elapsed.
        // We start at -1 so that after processing the first level (the start node itself)
        // the time becomes 0, which correctly represents "0 seconds to burn the start node".
        int time = -1;

        // -----------------------------------------------------------------------
        // Step 3: Process the BFS level by level.
        //         Each level corresponds to one second of fire spreading.
        // -----------------------------------------------------------------------
        while (!queue.isEmpty()) {
            // Increment time for each BFS level (each second of spreading)
            time++;

            // Process all nodes currently in the queue (they all burn at the same second)
            int levelSize = queue.size();
            for (int i = 0; i < levelSize; i++) {
                // Dequeue the next burning node
                int current = queue.poll();

                // Spread fire to all unvisited neighbors of the current node
                for (int neighbor : graph.getOrDefault(current, Collections.emptyList())) {
                    if (!visited.contains(neighbor)) {
                        // Mark neighbor as burning (visited) and add to queue
                        visited.add(neighbor);
                        queue.offer(neighbor);
                    }
                }
            }
        }

        // -----------------------------------------------------------------------
        // Step 4: Return the total time elapsed.
        //         `time` now holds the last BFS level index, which equals the
        //         number of seconds needed for the entire tree to burn.
        // -----------------------------------------------------------------------
        return time;
    }

    // =========================================================================
    // Helper method to build a tree from a level-order (BFS) array representation
    // where -1 represents null nodes.
    // =========================================================================

    /**
     * Builds a binary tree from a level-order array where -1 represents null.
     *
     * @param values array of node values in level-order (-1 means null)
     * @return the root of the constructed binary tree
     *
     * Time complexity:  O(N)
     * Space complexity: O(N)
     */
    public static TreeNode buildTree(int[] values) {
        if (values == null || values.length == 0 || values[0] == -1) {
            return null;
        }

        // Create the root node from the first element
        TreeNode root = new TreeNode(values[0]);
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        int i = 1; // index into the values array
        while (!queue.isEmpty() && i < values.length) {
            TreeNode current = queue.poll();

            // Assign left child if the next value is not -1
            if (i < values.length && values[i] != -1) {
                current.left = new TreeNode(values[i]);
                queue.offer(current.left);
            }
            i++;

            // Assign right child if the next value is not -1
            if (i < values.length && values[i] != -1) {
                current.right = new TreeNode(values[i]);
                queue.offer(current.right);
            }
            i++;
        }

        return root;
    }

    // =========================================================================
    // Main method: demonstrates the solution with the provided examples
    // =========================================================================

    /**
     * Entry point — runs sample test cases and prints results.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // ------------------------------------------------------------------
        // Example 1:
        //   Tree:       1
        //              / \
        //             2   3
        //            / \    \
        //           4   5    6
        //   start = 2
        //   Expected output: 3
        //
        //   Trace:
        //   t=0: node 2 burns
        //   t=1: nodes 1, 4, 5 burn (neighbors of 2)
        //   t=2: node 3 burns (neighbor of 1)
        //   t=3: node 6 burns (neighbor of 3)
        //   Answer: 3
        // ------------------------------------------------------------------
        System.out.println("=== Example 1 ===");
        // Level-order: [1, 2, 3, 4, 5, -1, 6]
        // -1 represents null (no left child for node 3)
        int[] values1 = {1, 2, 3, 4, 5, -1, 6};
        TreeNode root1 = buildTree(values1);
        int start1 = 2;
        int result1 = solution.amountOfTime(root1, start1);
        System.out.println("Input: root = [1,2,3,4,5,null,6], start = " + start1);
        System.out.println("Output: " + result1);          // Expected: 3
        System.out.println("Expected: 3");
        System.out.println("Correct: " + (result1 == 3));
        System.out.println();

        // ------------------------------------------------------------------
        // Example 2:
        //   Tree:   1
        //          / \
        //         2   3
        //   start = 1
        //   Expected output: 1
        //
        //   Trace:
        //   t=0: node 1 burns
        //   t=1: nodes 2 and 3 burn (children of 1)
        //   Answer: 1
        // ------------------------------------------------------------------
        System.out.println("=== Example 2 ===");
        int[] values2 = {1, 2, 3};
        TreeNode root2 = buildTree(values2);
        int start2 = 1;
        int result2 = solution.amountOfTime(root2, start2);
        System.out.println("Input: root = [1,2,3], start = " + start2);
        System.out.println("Output: " + result2);          // Expected: 1
        System.out.println("Expected: 1");
        System.out.println("Correct: " + (result2 == 1));
        System.out.println();

        // ------------------------------------------------------------------
        // Additional Test: Single node tree
        //   Tree:  5
        //   start = 5
        //   Expected output: 0  (the only node is already burning at t=0)
        // ------------------------------------------------------------------
        System.out.println("=== Additional Test: Single Node ===");
        int[] values3 = {5};
        TreeNode root3 = buildTree(values3);
        int start3 = 5;
        int result3 = solution.amountOfTime(root3, start3);
        System.out.println("Input: root = [5], start = " + start3);
        System.out.println("Output: " + result3);          // Expected: 0
        System.out.println("Expected: 0");
        System.out.println("Correct: " + (result3 == 0));
        System.out.println();

        // ------------------------------------------------------------------
        // Additional Test: Linear chain (like a linked list)
        //   Tree:  1 -> 2 -> 3 -> 4 -> 5  (right-skewed)
        //   start = 1
        //   Expected output: 4
        // ------------------------------------------------------------------
        System.out.println("=== Additional Test: Right-Skewed Tree ===");
        // Build manually: 1's right=2, 2's right=3, 3's right=4, 4's right=5
        TreeNode n5 = new TreeNode(5);
        TreeNode n4 = new TreeNode(4, null, n5);
        TreeNode n3 = new TreeNode(3, null, n4);
        TreeNode n2 = new TreeNode(2, null, n3);
        TreeNode n1 = new TreeNode(1, null, n2);
        int start4 = 1;
        int result4 = solution.amountOfTime(n1, start4);
        System.out.println("Input: root = [1,null,2,null,3,null,4,null,5], start = " + start4);
        System.out.println("Output: " + result4);          // Expected: 4
        System.out.println("Expected: 4");
        System.out.println("Correct: " + (result4 == 4));
        System.out.println();

        // ------------------------------------------------------------------
        // Additional Test: Fire starts at a leaf in a deeper tree
        //   Tree:       1
        //              / \
        //             2   3
        //            / \    \
        //           4   5    6
        //   start = 6
        //   Expected output: 4
        //   Trace:
        //   t=0: 6 burns
        //   t=1: 3 burns
        //   t=2: 1 burns
        //   t=3: 2 burns
        //   t=4: 4, 5 burn
        // ------------------------------------------------------------------
        System.out.println("=== Additional Test: Fire starts at leaf node 6 ===");
        int[] values5 = {1, 2, 3, 4, 5, -1, 6};
        TreeNode root5 = buildTree(values5);
        int start5 = 6;
        int result5 = solution.amountOfTime(root5, start5);
        System.out.println("Input: root = [1,2,3,4,5,null,6], start = " + start5);
        System.out.println("Output: " + result5);          // Expected: 4
        System.out.println("Expected: 4");
        System.out.println("Correct: " + (result5 == 4));
    }
}