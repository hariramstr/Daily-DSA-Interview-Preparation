import java.util.*;

/*
 * Maximum Toll Collected by Closing One Ancestral Gate
 *
 * Problem Description:
 * You are given a rooted tree with n checkpoints numbered from 0 to n - 1, where node 0 is the capital.
 * Each checkpoint i has a non-negative toll value toll[i]. A tourist route is any simple path from the
 * capital to a leaf, and the revenue of a route is the sum of toll values on the nodes along that path.
 *
 * The city may permanently close exactly one checkpoint x, with the restriction that x cannot be the capital.
 * Closing x removes x and every node in the subtree rooted at x, so every route passing through x becomes
 * unavailable. After the closure, the city collects revenue only from the remaining capital-to-leaf routes.
 * Your task is to choose the checkpoint to close so that the total remaining revenue across all surviving
 * capital-to-leaf routes is maximized.
 *
 * More formally, let S be the sum of revenues of all root-to-leaf paths in the original tree. If subtree(x)
 * is removed, every root-to-leaf path that enters x disappears entirely. Return the maximum possible total
 * revenue of the remaining routes.
 *
 * The answer can be large, so use 64-bit integers.
 *
 * Constraints:
 * - 2 <= n <= 2 * 10^5
 * - 0 <= toll[i] <= 10^9
 * - parent[0] = -1
 * - for i > 0, 0 <= parent[i] < n
 * - The input always forms a valid rooted tree
 *
 * Key Idea:
 * Every root-to-leaf route that passes through node x disappears if x is closed.
 * So for each node x (except root), we need:
 *
 * removedRevenue[x] = sum of revenues of all root-to-leaf paths that pass through x
 *
 * Then:
 * remainingRevenue[x] = totalRevenueAllPaths - removedRevenue[x]
 *
 * We want the maximum remainingRevenue[x].
 *
 * To compute removedRevenue[x] efficiently:
 * 1) leafCount[x] = number of leaves in subtree(x)
 *    Because exactly those root-to-leaf paths passing through x end at leaves inside subtree(x).
 *
 * 2) Let prefixSum[x] = sum of toll values from root to x.
 *
 * 3) For every ancestor a of a leaf in subtree(x), toll[a] contributes once to each such leaf-path.
 *    This can be reorganized into a DP:
 *
 *    removedRevenue[root] = totalRevenueAllPaths
 *    removedRevenue[child] = removedRevenue[parent]
 *                            - leafCount[child] * (sum of tolls on ancestors before child not on child-path exclusion form)
 *
 * A simpler and cleaner derivation is:
 *
 * Let upPrefix[x] = sum of tolls from root to x.
 * For node x, every leaf in subtree(x) contributes upPrefix[x] once,
 * plus the extra suffix from x's descendants to that leaf.
 *
 * Define downSum[x] = sum, over all leaves in subtree(x), of the sum of tolls on the path from x to that leaf.
 * Then removedRevenue[x] = downSum[x] + (upPrefix[parent[x]]) * leafCount[x]
 *
 * But we can compute an even simpler DP directly:
 *
 * If x is a leaf:
 *   removedRevenue[x] = prefixSum[x]
 *
 * If x is internal:
 *   removedRevenue[x] = leafCount[x] * toll[x] + sum over children c of (removedRevenue[c] + leafCount[c] * prefixBeforeX)
 *
 * However, the cleanest implementation is:
 *
 * removedRevenue[x] = leafCount[x] * prefixSum[x] + suffixContribution[x]
 *
 * where suffixContribution[x] is the sum over leaves in subtree(x) of
 * (path sum from child of x down to that leaf).
 *
 * This suffixContribution can be computed bottom-up with:
 *   if x is leaf: suffixContribution[x] = 0
 *   else suffixContribution[x] = sum over children c of (suffixContribution[c] + leafCount[c] * toll[c])
 *
 * Then:
 *   removedRevenue[x] = leafCount[x] * prefixSum[x] + suffixContribution[x]
 *
 * Finally:
 *   answer = max over x != 0 of (totalRevenueAllPaths - removedRevenue[x])
 *
 * This runs in O(n).
 */
public class Solution {

    /**
     * Computes the maximum total revenue remaining after closing exactly one non-root checkpoint.
     *
     * Step-by-step:
     * 1. Build the children list from the parent array.
     * 2. Traverse the tree iteratively from the root to compute:
     *    - a processing order
     *    - prefixSum[node] = sum of tolls from root to node
     * 3. Process nodes in reverse order (postorder effect) to compute:
     *    - leafCount[node] = number of leaves in its subtree
     *    - suffixContribution[node]
     *      where suffixContribution[node] is the total contribution below node
     *      after node itself, across all root-to-leaf paths inside its subtree.
     * 4. For each node x:
     *      removedRevenue[x] = leafCount[x] * prefixSum[x] + suffixContribution[x]
     *    This is exactly the total revenue of all root-to-leaf routes that pass through x.
     * 5. totalRevenue = removedRevenue[0], because every root-to-leaf path passes through the root.
     * 6. Try closing every non-root node and maximize:
     *      totalRevenue - removedRevenue[x]
     *
     * @param parent parent[i] is the parent of node i, with parent[0] = -1
     * @param toll toll[i] is the toll value at node i
     * @return the maximum total revenue remaining after closing exactly one non-root checkpoint
     * @implNote Time complexity: O(n)
     * @implNote Space complexity: O(n)
     */
    public long maximumRemainingRevenue(int[] parent, int[] toll) {
        int n = parent.length;

        List<Integer>[] children = buildChildren(parent);

        long[] prefixSum = new long[n];
        int[] order = buildTraversalOrder(children, toll, prefixSum);

        long[] leafCount = new long[n];
        long[] suffixContribution = new long[n];

        // Process in reverse traversal order so that children are handled before their parent.
        // This gives us a postorder-style DP without recursion.
        for (int i = n - 1; i >= 0; i--) {
            int node = order[i];

            // If a node has no children, it is a leaf.
            if (children[node].isEmpty()) {
                leafCount[node] = 1L;
                suffixContribution[node] = 0L;
                continue;
            }

            long leaves = 0L;
            long suffix = 0L;

            // For each child:
            // - all leaves in that child's subtree are also leaves in this node's subtree
            // - every such leaf-path contributes:
            //     toll[child] + everything below child
            //   to the "below current node" part
            for (int child : children[node]) {
                leaves += leafCount[child];
                suffix += suffixContribution[child] + leafCount[child] * (long) toll[child];
            }

            leafCount[node] = leaves;
            suffixContribution[node] = suffix;
        }

        long[] removedRevenue = new long[n];
        for (int node = 0; node < n; node++) {
            removedRevenue[node] = leafCount[node] * prefixSum[node] + suffixContribution[node];
        }

        long totalRevenue = removedRevenue[0];
        long answer = Long.MIN_VALUE;

        // We are not allowed to close the root (node 0).
        for (int node = 1; node < n; node++) {
            long remaining = totalRevenue - removedRevenue[node];
            answer = Math.max(answer, remaining);
        }

        return answer;
    }

    /**
     * Builds the children adjacency list from the parent array.
     *
     * @param parent parent array describing the rooted tree
     * @return adjacency list where children[u] contains all direct children of u
     * @implNote Time complexity: O(n)
     * @implNote Space complexity: O(n)
     */
    public List<Integer>[] buildChildren(int[] parent) {
        int n = parent.length;
        @SuppressWarnings("unchecked")
        List<Integer>[] children = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            children[i] = new ArrayList<>();
        }
        for (int i = 1; i < n; i++) {
            children[parent[i]].add(i);
        }
        return children;
    }

    /**
     * Builds an iterative root-to-node traversal order and computes prefix sums.
     *
     * prefixSum[node] = toll value sum along the unique path from root to node.
     *
     * We use an explicit stack to avoid recursion depth issues on large trees.
     *
     * @param children adjacency list of the tree
     * @param toll toll values on nodes
     * @param prefixSum output array to fill with root-to-node path sums
     * @return an order array containing nodes in traversal order from root
     * @implNote Time complexity: O(n)
     * @implNote Space complexity: O(n)
     */
    public int[] buildTraversalOrder(List<Integer>[] children, int[] toll, long[] prefixSum) {
        int n = toll.length;
        int[] order = new int[n];
        int idx = 0;

        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(0);
        prefixSum[0] = toll[0];

        while (!stack.isEmpty()) {
            int node = stack.pop();
            order[idx++] = node;

            // Push children. Any order is fine because we only need a valid parent-before-child order.
            for (int child : children[node]) {
                prefixSum[child] = prefixSum[node] + toll[child];
                stack.push(child);
            }
        }

        return order;
    }

    /**
     * Convenience wrapper matching the problem statement wording.
     *
     * @param parent parent array of the rooted tree
     * @param toll toll values on the nodes
     * @return maximum remaining revenue after closing one non-root checkpoint
     * @implNote Time complexity: O(n)
     * @implNote Space complexity: O(n)
     */
    public long solve(int[] parent, int[] toll) {
        return maximumRemainingRevenue(parent, toll);
    }

    /**
     * Demonstrates the algorithm on the sample-style inputs.
     *
     * Note:
     * The textual explanations in the prompt contain inconsistencies between the listed outputs
     * and the described best closures. This program prints the mathematically correct answers
     * according to the formal problem definition:
     * maximize total remaining revenue after removing one non-root subtree.
     *
     * Example 1:
     * parent = [-1,0,0,1,1,2]
     * toll   = [5,2,4,3,1,6]
     * Root-to-leaf sums: 10, 8, 15 => total = 33
     * Best closure is node 4, leaving 25
     *
     * Example 2:
     * parent = [-1,0,0,1,3,3,2]
     * toll   = [1,10,2,5,4,6,8]
     * Root-to-leaf sums: 20, 22, 11 => total = 53
     * Best closure is node 6, leaving 42
     *
     * @param args command-line arguments, unused
     * @implNote Time complexity: O(n) per demonstration case
     * @implNote Space complexity: O(n) per demonstration case
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] parent1 = {-1, 0, 0, 1, 1, 2};
        int[] toll1 = {5, 2, 4, 3, 1, 6};
        long result1 = solution.solve(parent1, toll1);
        System.out.println(result1); // Expected mathematically correct answer: 25

        int[] parent2 = {-1, 0, 0, 1, 3, 3, 2};
        int[] toll2 = {1, 10, 2, 5, 4, 6, 8};
        long result2 = solution.solve(parent2, toll2);
        System.out.println(result2); // Expected mathematically correct answer: 42
    }
}