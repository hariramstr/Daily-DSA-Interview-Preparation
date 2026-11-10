import java.util.*;

/*
 * Title: Find the Nearest Open Seat in a Venue Tree
 * Difficulty: Medium
 * Topic: Trees
 *
 * Problem Description:
 * A concert venue stores its seating layout as a binary tree. Each node represents a seat section
 * and contains a unique integer id. The root is the main entrance section. Some sections are
 * temporarily closed for cleaning, while the rest are open.
 *
 * You are given the root of the binary tree, an integer target representing the id of a section
 * where a guest is currently standing, and a set closedSections containing the ids of closed
 * sections. Return the id of the open section that is closest to the target section by number of
 * edges traveled.
 *
 * Distance is measured along tree edges, and movement is allowed both downward to children and
 * upward to parents. The target section itself may be open or closed. If the target section is
 * open, then it is considered a valid answer with distance 0. If multiple open sections are tied
 * for the minimum distance, return the one with the smallest section id. If the target id does not
 * exist in the tree, return -1. If every section is closed, return -1.
 *
 * This problem asks you to search a tree as if it were an undirected graph. A common approach is
 * to first record parent pointers, then run a breadth-first search starting from the target node
 * while skipping visited nodes.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [1, 100000].
 * - Node ids are unique integers in the range [1, 10^9].
 * - closedSections contains between 0 and n ids.
 * - target is an integer in the range [1, 10^9].
 * - Your solution should run in O(n) time and use O(n) extra space.
 *
 * Example 1:
 * Input: root = [8,4,12,2,6,10,14], target = 4, closedSections = [4,2]
 * Output: 6
 * Explanation: Section 4 is closed. Its neighbors are 2, 6, and 8. Section 2 is also closed,
 * while section 6 is open and only 1 edge away, so the answer is 6.
 *
 * Example 2:
 * Input: root = [5,3,9,1,null,7,11], target = 3, closedSections = [1,3,5]
 * Output: 7
 * Explanation: Section 3 is closed. The nearest open sections at distance 2 are 7 and 9 through
 * the path 3 -> 5 -> 7 and 3 -> 5 -> 9. The smaller id among the nearest open sections is 7,
 * so return 7.
 */

public class Solution {

    /**
     * Basic binary tree node used to represent a seat section in the venue.
     */
    public static class TreeNode {
        int id;
        TreeNode left;
        TreeNode right;

        TreeNode(int id) {
            this.id = id;
        }
    }

    /**
     * Finds the id of the nearest open section to the given target section.
     *
     * The algorithm works in two major phases:
     * 1) Traverse the tree once to:
     *    - record each node's parent
     *    - locate the node whose id equals target
     * 2) Run a breadth-first search (BFS) starting from the target node, treating the tree like
     *    an undirected graph (left child, right child, and parent are all neighbors).
     *
     * Because BFS explores nodes level by level, the first level that contains at least one open
     * section is guaranteed to be the minimum distance. If multiple open sections appear in that
     * same level, we return the smallest id among them.
     *
     * @param root the root of the binary tree representing the venue layout
     * @param target the id of the section where the guest is currently standing
     * @param closedSections a set containing ids of sections that are closed
     * @return the id of the nearest open section; returns -1 if target does not exist or if every
     *         reachable section is closed
     *
     * Time complexity: O(n), where n is the number of nodes in the tree
     * Space complexity: O(n), for parent mapping, node lookup, BFS queue, and visited set
     */
    public int findNearestOpenSeat(TreeNode root, int target, Set<Integer> closedSections) {
        if (root == null) {
            return -1;
        }

        // This map stores, for every node, its parent node.
        // It allows us to move "upward" in the tree during BFS.
        Map<TreeNode, TreeNode> parentMap = new HashMap<>();

        // This map stores id -> node so we can quickly locate the target node after traversal.
        Map<Integer, TreeNode> idToNode = new HashMap<>();

        // Build parent pointers and id lookup in one traversal.
        buildParentAndLookup(root, null, parentMap, idToNode);

        // If target id is not present in the tree, the problem says return -1.
        TreeNode targetNode = idToNode.get(target);
        if (targetNode == null) {
            return -1;
        }

        // Standard BFS setup.
        Queue<TreeNode> queue = new ArrayDeque<>();
        Set<TreeNode> visited = new HashSet<>();

        queue.offer(targetNode);
        visited.add(targetNode);

        // BFS explores nodes by increasing distance from the target.
        while (!queue.isEmpty()) {
            int levelSize = queue.size();

            // We will inspect all nodes at the current distance first.
            // If any open nodes exist at this distance, we must choose the smallest id among them.
            int bestOpenIdAtThisLevel = Integer.MAX_VALUE;
            boolean foundOpenAtThisLevel = false;

            for (int i = 0; i < levelSize; i++) {
                TreeNode current = queue.poll();

                // If current node is not in the closed set, it is open.
                if (!closedSections.contains(current.id)) {
                    foundOpenAtThisLevel = true;
                    bestOpenIdAtThisLevel = Math.min(bestOpenIdAtThisLevel, current.id);
                }

                // Add all unvisited neighbors:
                // 1) left child
                // 2) right child
                // 3) parent
                //
                // This is exactly how we treat the tree as an undirected graph.
                if (current.left != null && visited.add(current.left)) {
                    queue.offer(current.left);
                }

                if (current.right != null && visited.add(current.right)) {
                    queue.offer(current.right);
                }

                TreeNode parent = parentMap.get(current);
                if (parent != null && visited.add(parent)) {
                    queue.offer(parent);
                }
            }

            // If we found any open node at this BFS level, this is the minimum possible distance.
            // Among all open nodes at this same distance, return the smallest id.
            if (foundOpenAtThisLevel) {
                return bestOpenIdAtThisLevel;
            }
        }

        // If BFS finishes without finding an open node, then every section is closed.
        return -1;
    }

    /**
     * Traverses the tree and records:
     * - each node's parent
     * - each node's id-to-node mapping
     *
     * This method uses DFS recursion for clarity and simplicity.
     *
     * @param node the current node being visited
     * @param parent the parent of the current node, or null if current node is the root
     * @param parentMap map that will store node -> parent relationships
     * @param idToNode map that will store id -> node relationships
     * @return nothing
     *
     * Time complexity: O(n), because each node is visited once
     * Space complexity: O(n), due to the maps and recursion stack in the worst case
     */
    public void buildParentAndLookup(
            TreeNode node,
            TreeNode parent,
            Map<TreeNode, TreeNode> parentMap,
            Map<Integer, TreeNode> idToNode) {

        if (node == null) {
            return;
        }

        // Record the parent pointer for the current node.
        parentMap.put(node, parent);

        // Record how to find this node by its unique id.
        idToNode.put(node.id, node);

        // Continue recursively into the left and right subtrees.
        buildParentAndLookup(node.left, node, parentMap, idToNode);
        buildParentAndLookup(node.right, node, parentMap, idToNode);
    }

    /**
     * Builds a binary tree from a level-order array representation where null means "no node".
     *
     * Example:
     * [8, 4, 12, 2, 6, 10, 14]
     *
     * This helper is used only for demonstration in main.
     *
     * @param values an array of Integer values in level-order, where null indicates a missing node
     * @return the root of the constructed binary tree, or null if the array is empty or starts with null
     *
     * Time complexity: O(n), where n is the number of array entries
     * Space complexity: O(n), for the queue used during construction
     */
    public TreeNode buildTreeFromLevelOrder(Integer[] values) {
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
     * Convenience helper to create a set of integers from varargs.
     *
     * @param values integer values to place into a HashSet
     * @return a set containing the given values
     *
     * Time complexity: O(k), where k is the number of provided values
     * Space complexity: O(k)
     */
    public Set<Integer> toSet(int... values) {
        Set<Integer> set = new HashSet<>();
        for (int value : values) {
            set.add(value);
        }
        return set;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstration case
     * Space complexity: O(n) per demonstration case
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Tree: [8,4,12,2,6,10,14]
        // target = 4
        // closedSections = [4,2]
        // Expected output: 6
        Integer[] example1 = {8, 4, 12, 2, 6, 10, 14};
        TreeNode root1 = solution.buildTreeFromLevelOrder(example1);
        int result1 = solution.findNearestOpenSeat(root1, 4, solution.toSet(4, 2));
        System.out.println(result1);

        // Example 2:
        // Tree: [5,3,9,1,null,7,11]
        // target = 3
        // closedSections = [1,3,5]
        // Expected output: 7
        //
        // Important correctness check:
        // From node 3:
        // distance 0 -> {3} closed
        // distance 1 -> {1,5}; 1 closed, 5 closed
        // distance 2 -> {9,7}; both open, choose smaller id = 7
        Integer[] example2 = {5, 3, 9, 1, null, 7, 11};
        TreeNode root2 = solution.buildTreeFromLevelOrder(example2);
        int result2 = solution.findNearestOpenSeat(root2, 3, solution.toSet(1, 3, 5));
        System.out.println(result2);

        // Additional quick checks:

        // If target itself is open, answer should be target.
        Integer[] example3 = {10, 5, 15};
        TreeNode root3 = solution.buildTreeFromLevelOrder(example3);
        int result3 = solution.findNearestOpenSeat(root3, 5, solution.toSet(15));
        System.out.println(result3); // Expected: 5

        // If target does not exist, answer should be -1.
        int result4 = solution.findNearestOpenSeat(root3, 999, solution.toSet());
        System.out.println(result4); // Expected: -1

        // If every node is closed, answer should be -1.
        int result5 = solution.findNearestOpenSeat(root3, 10, solution.toSet(10, 5, 15));
        System.out.println(result5); // Expected: -1
    }
}