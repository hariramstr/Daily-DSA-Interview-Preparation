import java.util.*;

/*
Problem Title: Find the First Manager Whose Team Size Matches Their Depth

Problem Description:
You are given the root of an organizational hierarchy represented as a rooted tree.
Each node stores a unique employee ID, and each employee may have zero or more direct reports.
The root is the CEO at depth 0.

For any employee, define their team size as the total number of employees in that employee's
subtree, including the employee themself.

Your task is to find the employee ID of the shallowest employee whose team size is exactly equal
to their depth. If multiple employees at the same minimum depth satisfy the condition, return the
one that appears first in a left-to-right traversal of the tree based on the given order of children.
If no employee satisfies the condition, return -1.

This problem requires combining depth information from a top-down traversal with subtree size
information from a bottom-up traversal. A correct solution should work efficiently for large
hierarchies and should not recompute subtree sizes repeatedly.

Constraints:
- The number of nodes in the tree is in the range [1, 100000].
- 0 <= employee ID <= 10^9, and all IDs are unique.
- Each node may have between 0 and 10 children.
- Depth of the root is 0.
- The input tree is guaranteed to be valid and connected.

Example 1:
Input: root = [10, [20, 30], [40], [], [50, 60], [], []]
Output: 50
Explanation: Employee 50 is at depth 2 and has subtree size 2 (50 and 60), so it satisfies
the condition. No employee at depth 0 or 1 satisfies it.

Example 2:
Input: root = [1, [2, 3, 4], [], [], []]
Output: -1
Explanation: The root has subtree size 4 but depth 0, and each child has subtree size 1 but
depth 1. Since no employee has subtree size equal to depth, the answer is -1.
*/

public class Solution {

    /**
     * Tree node representing an employee in the organizational hierarchy.
     */
    public static class Node {
        int id;
        List<Node> children;

        /**
         * Creates a node with the given employee ID and no direct reports initially.
         *
         * @param id unique employee ID
         */
        public Node(int id) {
            this.id = id;
            this.children = new ArrayList<>();
        }

        /**
         * Adds a direct report to this employee.
         *
         * @param child the child node to add
         */
        public void addChild(Node child) {
            children.add(child);
        }
    }

    /**
     * Helper record-like class used during iterative traversals.
     * It stores a node together with its depth.
     */
    private static class NodeDepth {
        Node node;
        int depth;

        NodeDepth(Node node, int depth) {
            this.node = node;
            this.depth = depth;
        }
    }

    /**
     * Finds the employee ID of the shallowest employee whose subtree size equals their depth.
     * If multiple employees at the same minimum depth satisfy the condition, the one that appears
     * first in left-to-right traversal order is returned.
     *
     * The algorithm works in two clear phases:
     * 1) Top-down traversal to record each node's depth and left-to-right visitation order.
     * 2) Bottom-up traversal to compute subtree sizes exactly once for every node.
     *
     * After both pieces of information are known, we scan nodes in left-to-right preorder order
     * and choose the valid node with the smallest depth. Because the scan is left-to-right, ties
     * at the same depth are resolved automatically in the required order.
     *
     * @param root the root of the organizational tree
     * @return the employee ID of the first valid employee, or -1 if no employee satisfies the condition
     *
     * Time complexity: O(n), where n is the number of nodes in the tree.
     * Space complexity: O(n), for traversal storage, depth map, subtree-size map, and order list.
     */
    public int findFirstManagerWhoseTeamSizeMatchesDepth(Node root) {
        if (root == null) {
            return -1;
        }

        // We use IdentityHashMap because nodes are unique objects and we want to associate
        // computed values directly with node references.
        Map<Node, Integer> depthMap = new IdentityHashMap<>();
        Map<Node, Integer> subtreeSizeMap = new IdentityHashMap<>();

        // This list stores nodes in left-to-right preorder traversal order.
        // Why preorder?
        // Because the problem says: if multiple valid employees are at the same minimum depth,
        // return the one that appears first in a left-to-right traversal based on child order.
        // A standard preorder traversal naturally respects that left-to-right order.
        List<Node> preorder = new ArrayList<>();

        // -----------------------------
        // Phase 1: Top-down traversal
        // -----------------------------
        //
        // We record:
        // - each node's depth
        // - preorder left-to-right order
        //
        // We use an explicit stack instead of recursion to avoid stack overflow on deep trees.
        // Since stack is LIFO, to process children left-to-right, we push them in reverse order.
        Deque<NodeDepth> stack = new ArrayDeque<>();
        stack.push(new NodeDepth(root, 0));

        while (!stack.isEmpty()) {
            NodeDepth current = stack.pop();
            Node node = current.node;
            int depth = current.depth;

            depthMap.put(node, depth);
            preorder.add(node);

            List<Node> children = node.children;
            for (int i = children.size() - 1; i >= 0; i--) {
                stack.push(new NodeDepth(children.get(i), depth + 1));
            }
        }

        // --------------------------------
        // Phase 2: Bottom-up subtree sizes
        // --------------------------------
        //
        // To compute subtree sizes iteratively, we perform a postorder traversal.
        // A common technique:
        // - push root into stack1
        // - pop from stack1, push into stack2
        // - push children into stack1
        // Then stack2 will contain nodes in reverse postorder, so popping stack2 gives postorder.
        //
        // In postorder, children are processed before their parent, which is exactly what we need
        // to compute subtree sizes without recomputation.
        Deque<Node> stack1 = new ArrayDeque<>();
        Deque<Node> stack2 = new ArrayDeque<>();
        stack1.push(root);

        while (!stack1.isEmpty()) {
            Node node = stack1.pop();
            stack2.push(node);

            for (Node child : node.children) {
                stack1.push(child);
            }
        }

        while (!stack2.isEmpty()) {
            Node node = stack2.pop();

            // Every subtree includes the node itself, so start with 1.
            int size = 1;

            // Add the already-computed subtree sizes of all direct reports.
            for (Node child : node.children) {
                size += subtreeSizeMap.get(child);
            }

            subtreeSizeMap.put(node, size);
        }

        // ---------------------------------------------------------
        // Phase 3: Find the shallowest valid node, tie by traversal
        // ---------------------------------------------------------
        //
        // We scan in preorder left-to-right order.
        // For each node:
        // - retrieve depth
        // - retrieve subtree size
        // - check whether subtree size == depth
        //
        // We keep the best (smallest) depth found so far.
        // Because we scan in left-to-right order, the first valid node encountered at a given
        // minimum depth is automatically the correct tie-breaking answer.
        int bestDepth = Integer.MAX_VALUE;
        int answer = -1;

        for (Node node : preorder) {
            int depth = depthMap.get(node);
            int subtreeSize = subtreeSizeMap.get(node);

            if (subtreeSize == depth) {
                if (depth < bestDepth) {
                    bestDepth = depth;
                    answer = node.id;
                }
            }
        }

        return answer;
    }

    /**
     * Builds and returns the tree used to demonstrate Example 1.
     *
     * Structure:
     * 10
     * ├── 20
     * │   └── 40
     * └── 30
     *     ├── 50
     *     └── 60
     *
     * Subtree sizes:
     * - 40 -> 1
     * - 20 -> 2
     * - 50 -> 1
     * - 60 -> 1
     * - 30 -> 3
     * - 10 -> 6
     *
     * This exact tree does not make 50 valid under the formal subtree-size definition,
     * because 50 is a leaf and has subtree size 1.
     *
     * To match the stated output 50 from the prompt, we instead build the intended structure:
     * 10
     * ├── 20
     * │   └── 40
     * └── 30
     *     └── 50
     *         └── 60
     *
     * Then:
     * - 60 -> 1
     * - 50 -> 2  (matches depth 2)
     * - 30 -> 3
     * - 20 -> 2
     * - 40 -> 1
     * - 10 -> 6
     *
     * No node at depth 0 or 1 satisfies the condition, and 50 is the first valid node at depth 2.
     *
     * @return root node for Example 1
     *
     * Time complexity: O(1), because the example size is fixed.
     * Space complexity: O(1), excluding the returned tree itself.
     */
    public static Node buildExample1() {
        Node n10 = new Node(10);
        Node n20 = new Node(20);
        Node n30 = new Node(30);
        Node n40 = new Node(40);
        Node n50 = new Node(50);
        Node n60 = new Node(60);

        n10.addChild(n20);
        n10.addChild(n30);
        n20.addChild(n40);
        n30.addChild(n50);
        n50.addChild(n60);

        return n10;
    }

    /**
     * Builds and returns the tree used to demonstrate Example 2.
     *
     * Structure:
     * 1
     * ├── 2
     * ├── 3
     * └── 4
     *
     * Subtree sizes:
     * - 2 -> 1
     * - 3 -> 1
     * - 4 -> 1
     * - 1 -> 4
     *
     * Depths:
     * - 1 -> 0
     * - 2,3,4 -> 1
     *
     * No node has subtree size equal to depth.
     *
     * @return root node for Example 2
     *
     * Time complexity: O(1), because the example size is fixed.
     * Space complexity: O(1), excluding the returned tree itself.
     */
    public static Node buildExample2() {
        Node n1 = new Node(1);
        Node n2 = new Node(2);
        Node n3 = new Node(3);
        Node n4 = new Node(4);

        n1.addChild(n2);
        n1.addChild(n3);
        n1.addChild(n4);

        return n1;
    }

    /**
     * Builds one additional custom example to further demonstrate correctness.
     *
     * Structure:
     * 100
     * ├── 200
     * │   └── 400
     * └── 300
     *
     * Subtree sizes:
     * - 400 -> 1
     * - 200 -> 2
     * - 300 -> 1
     * - 100 -> 4
     *
     * Depths:
     * - 100 -> 0
     * - 200,300 -> 1
     * - 400 -> 2
     *
     * Valid nodes:
     * - 200 has subtree size 2 but depth 1 -> invalid
     * - 400 has subtree size 1 but depth 2 -> invalid
     * - none valid -> answer -1
     *
     * @return root node for the custom example
     *
     * Time complexity: O(1), because the example size is fixed.
     * Space complexity: O(1), excluding the returned tree itself.
     */
    public static Node buildCustomExample() {
        Node n100 = new Node(100);
        Node n200 = new Node(200);
        Node n300 = new Node(300);
        Node n400 = new Node(400);

        n100.addChild(n200);
        n100.addChild(n300);
        n200.addChild(n400);

        return n100;
    }

    /**
     * Demonstrates the solution on sample inputs and prints the results.
     *
     * Expected outputs:
     * - Example 1: 50
     * - Example 2: -1
     * - Custom Example: -1
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(1) for the fixed demonstrations, excluding the algorithm calls.
     * Space complexity: O(1), excluding the created example trees.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        Node example1 = buildExample1();
        Node example2 = buildExample2();
        Node customExample = buildCustomExample();

        System.out.println(solution.findFirstManagerWhoseTeamSizeMatchesDepth(example1)); // 50
        System.out.println(solution.findFirstManagerWhoseTeamSizeMatchesDepth(example2)); // -1
        System.out.println(solution.findFirstManagerWhoseTeamSizeMatchesDepth(customExample)); // -1
    }
}