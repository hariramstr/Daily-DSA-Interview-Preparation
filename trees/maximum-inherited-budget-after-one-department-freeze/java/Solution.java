import java.util.*;

/*
 * Title: Maximum Inherited Budget After One Department Freeze
 * Difficulty: Hard
 * Topic: Trees
 *
 * Problem Description:
 * A company is organized as a rooted tree with node 0 as the CEO. Each node represents a department,
 * and edges connect a department to its direct sub-departments. Every department i has an integer
 * budget value budget[i], which may be positive, zero, or negative.
 *
 * For any department u, define its inherited budget as the sum of budget values on the simple path
 * from the CEO to u, inclusive.
 *
 * Before the next quarter begins, the company may freeze at most one department x. Freezing x removes
 * the entire subtree rooted at x from consideration. The CEO's department cannot be frozen. After the
 * freeze, only departments not in the removed subtree remain active.
 *
 * Your task is to compute the maximum inherited budget among all active departments after applying at
 * most one freeze operation. You may also choose not to freeze any department.
 *
 * Important details:
 * - The tree is rooted at node 0.
 * - If a subtree is frozen, every node in that subtree becomes inactive.
 * - The inherited budget of an active node is still computed using the original root-to-node path values,
 *   since all ancestors outside the frozen subtree remain unchanged.
 * - It is guaranteed that at least one active node always remains because node 0 cannot be frozen.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - edges.length == n - 1
 * - 0 <= parent, child < n
 * - The input edges form a valid tree rooted at 0
 * - -10^9 <= budget[i] <= 10^9
 * - The answer fits in a signed 64-bit integer
 *
 * Key Insight:
 * Let pathSum[u] be the inherited budget of node u.
 *
 * If we freeze subtree x, then every node inside x disappears, and every node outside x remains.
 * Therefore, after freezing x, the best remaining inherited budget is simply:
 *
 *     max(pathSum[u]) over all nodes u that are NOT in subtree(x)
 *
 * So the problem becomes:
 * - compute path sums for all nodes,
 * - flatten the tree with an Euler tour so every subtree becomes one contiguous interval,
 * - for each possible frozen node x != 0, compute the maximum path sum outside x's Euler interval,
 * - also compare with the option of not freezing anything.
 *
 * If tin[x]..tout[x] is the Euler interval of subtree x, then the maximum outside that interval is:
 *
 *     max(prefixMax[tin[x] - 1], suffixMax[tout[x] + 1])
 *
 * where prefixMax and suffixMax are built over the Euler-order array of path sums.
 *
 * This gives an O(n) solution after building the tree.
 */

public class Solution {

    /**
     * Computes the maximum inherited budget among active departments after freezing at most one subtree.
     *
     * The algorithm works in four major phases:
     *
     * 1) Build the rooted tree from the directed edges.
     * 2) Perform an iterative DFS from root 0 to compute:
     *    - pathSum[u]: sum of budgets from root to u
     *    - tin[u], tout[u]: Euler tour entry/exit times
     *    - eulerValue[t]: path sum of the node visited at Euler time t
     * 3) Build prefix and suffix maximum arrays over eulerValue.
     * 4) Try every possible frozen subtree x (except root), and compute the best path sum outside
     *    subtree(x) using the prefix/suffix arrays. Also compare with the "freeze nothing" option.
     *
     * Why this is correct:
     * - In Euler tour order, every subtree is a contiguous segment.
     * - Removing subtree x means removing exactly the interval [tin[x], tout[x]].
     * - Therefore, the best remaining node must lie either:
     *   - before tin[x], or
     *   - after tout[x].
     * - prefixMax and suffixMax let us query those two regions in O(1).
     *
     * @param n the number of departments (nodes)
     * @param edges directed edges where each edge is [parent, child]
     * @param budget budget[i] is the budget value of department i
     * @return the maximum inherited budget achievable after freezing at most one non-root subtree
     * @implNote Time complexity: O(n)
     * @implNote Space complexity: O(n)
     */
    public long maximumInheritedBudgetAfterOneFreeze(int n, int[][] edges, int[] budget) {
        if (n == 1) {
            return budget[0];
        }

        // ---------------------------------------------------------------------
        // Step 1: Build adjacency lists for the rooted tree.
        //
        // Because the input edges are already parent -> child, we can directly
        // store children for each node.
        // ---------------------------------------------------------------------
        List<Integer>[] children = buildTree(n, edges);

        // ---------------------------------------------------------------------
        // Step 2: Compute:
        // - pathSum[u] = inherited budget of node u
        // - tin[u], tout[u] = Euler tour interval of subtree u
        // - eulerValue[t] = pathSum of the node appearing at Euler time t
        //
        // We use an iterative DFS instead of recursion to avoid stack overflow
        // for very deep trees (n can be up to 200000).
        // ---------------------------------------------------------------------
        long[] pathSum = new long[n];
        int[] tin = new int[n];
        int[] tout = new int[n];
        long[] eulerValue = new long[n];

        computeEulerAndPathSumsIterative(children, budget, pathSum, tin, tout, eulerValue);

        // ---------------------------------------------------------------------
        // Step 3: Build prefix maximum and suffix maximum arrays over the Euler
        // order values.
        //
        // prefixMax[i] = maximum eulerValue in range [0..i]
        // suffixMax[i] = maximum eulerValue in range [i..n-1]
        //
        // These arrays allow us to answer:
        // "What is the maximum path sum outside subtree x?"
        // in O(1) time.
        // ---------------------------------------------------------------------
        long[] prefixMax = new long[n];
        long[] suffixMax = new long[n];

        prefixMax[0] = eulerValue[0];
        for (int i = 1; i < n; i++) {
            prefixMax[i] = Math.max(prefixMax[i - 1], eulerValue[i]);
        }

        suffixMax[n - 1] = eulerValue[n - 1];
        for (int i = n - 2; i >= 0; i--) {
            suffixMax[i] = Math.max(suffixMax[i + 1], eulerValue[i]);
        }

        // ---------------------------------------------------------------------
        // Step 4: Evaluate all choices:
        // - no freeze at all
        // - freeze subtree x for each x != 0
        //
        // No freeze:
        //   answer starts as the maximum over all nodes, which is prefixMax[n-1].
        //
        // Freeze subtree x:
        //   subtree x corresponds to Euler interval [tin[x], tout[x]]
        //   so the remaining nodes are:
        //     [0 .. tin[x]-1] and [tout[x]+1 .. n-1]
        //
        //   bestOutside = max(
        //       prefixMax[tin[x]-1] if tin[x] > 0,
        //       suffixMax[tout[x]+1] if tout[x] + 1 < n
        //   )
        //
        // Since x != 0, root is never removed, so there is always at least one
        // remaining node.
        // ---------------------------------------------------------------------
        long answer = prefixMax[n - 1]; // option: freeze nothing

        for (int x = 1; x < n; x++) {
            long bestOutside = Long.MIN_VALUE;

            if (tin[x] > 0) {
                bestOutside = Math.max(bestOutside, prefixMax[tin[x] - 1]);
            }
            if (tout[x] + 1 < n) {
                bestOutside = Math.max(bestOutside, suffixMax[tout[x] + 1]);
            }

            answer = Math.max(answer, bestOutside);
        }

        return answer;
    }

    /**
     * Builds the children adjacency list for the rooted tree.
     *
     * The input edges are assumed to be directed from parent to child, exactly as stated
     * in the problem.
     *
     * @param n the number of nodes
     * @param edges directed edges [parent, child]
     * @return adjacency list where result[u] contains all direct children of u
     * @implNote Time complexity: O(n)
     * @implNote Space complexity: O(n)
     */
    public List<Integer>[] buildTree(int n, int[][] edges) {
        @SuppressWarnings("unchecked")
        List<Integer>[] children = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            children[i] = new ArrayList<>();
        }

        for (int[] edge : edges) {
            int parent = edge[0];
            int child = edge[1];
            children[parent].add(child);
        }

        return children;
    }

    /**
     * Computes Euler tour entry/exit times and root-to-node path sums using an iterative DFS.
     *
     * Detailed behavior:
     * - pathSum[u] is the sum of budget values on the path from root 0 to u.
     * - tin[u] is the time when u is first entered.
     * - tout[u] is the last Euler time contained in u's subtree.
     * - eulerValue[tin[u]] = pathSum[u]
     *
     * Why iterative DFS?
     * - Recursive DFS on a chain-like tree of length 200000 would likely overflow the Java stack.
     * - Iterative DFS is safe for the full constraint range.
     *
     * Implementation idea:
     * We simulate recursion with an explicit stack of frames.
     * Each frame stores:
     * - node: current node
     * - nextChildIndex: which child to process next
     *
     * On first entering a node:
     * - assign tin[node] = currentTime
     * - write eulerValue[currentTime] = pathSum[node]
     * - increment currentTime
     *
     * After all children are processed:
     * - assign tout[node] = currentTime - 1
     *
     * @param children adjacency list of the rooted tree
     * @param budget budget values for each node
     * @param pathSum output array for inherited budgets
     * @param tin output array for Euler entry times
     * @param tout output array for Euler exit times
     * @param eulerValue output array where eulerValue[tin[u]] = pathSum[u]
     * @return nothing; results are written into the provided arrays
     * @implNote Time complexity: O(n)
     * @implNote Space complexity: O(n)
     */
    public void computeEulerAndPathSumsIterative(
            List<Integer>[] children,
            int[] budget,
            long[] pathSum,
            int[] tin,
            int[] tout,
            long[] eulerValue) {

        int n = children.length;

        // Root inherited budget is just its own budget.
        pathSum[0] = budget[0];

        // Stack arrays are faster and simpler than allocating many small objects.
        int[] stackNode = new int[n];
        int[] stackNextChildIndex = new int[n];
        boolean[] entered = new boolean[n];

        int top = 0;
        stackNode[0] = 0;
        stackNextChildIndex[0] = 0;

        int time = 0;

        while (top >= 0) {
            int node = stackNode[top];

            // -------------------------------------------------------------
            // First time we see this node:
            // - assign Euler entry time
            // - store its path sum in Euler order
            // -------------------------------------------------------------
            if (!entered[top]) {
                entered[top] = true;
                tin[node] = time;
                eulerValue[time] = pathSum[node];
                time++;
            }

            // -------------------------------------------------------------
            // If there are still children left to process, push the next one.
            // -------------------------------------------------------------
            if (stackNextChildIndex[top] < children[node].size()) {
                int child = children[node].get(stackNextChildIndex[top]);
                stackNextChildIndex[top]++;

                pathSum[child] = pathSum[node] + budget[child];

                top++;
                stackNode[top] = child;
                stackNextChildIndex[top] = 0;
                entered[top] = false;
            } else {
                // ---------------------------------------------------------
                // All children processed:
                // the subtree of 'node' occupies Euler interval
                // [tin[node], time - 1]
                // ---------------------------------------------------------
                tout[node] = time - 1;
                top--;
            }
        }
    }

    /**
     * Convenience wrapper used by the demo in main.
     *
     * @param n the number of nodes
     * @param edges directed edges [parent, child]
     * @param budget budget values
     * @return the computed answer
     * @implNote Time complexity: O(n)
     * @implNote Space complexity: O(n)
     */
    public long solve(int n, int[][] edges, int[] budget) {
        return maximumInheritedBudgetAfterOneFreeze(n, edges, budget);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> 7
     * Example 2 -> 17
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * @implNote Time complexity: O(1) for the fixed demo size, excluding solve calls
     * @implNote Space complexity: O(1) for the fixed demo size, excluding solve calls
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int n1 = 5;
        int[][] edges1 = {
                {0, 1},
                {0, 2},
                {1, 3},
                {1, 4}
        };
        int[] budget1 = {4, -2, 3, 5, -1};
        long result1 = solution.solve(n1, edges1, budget1);
        System.out.println(result1); // Expected: 7

        int n2 = 6;
        int[][] edges2 = {
                {0, 1},
                {0, 2},
                {1, 3},
                {2, 4},
                {2, 5}
        };
        int[] budget2 = {5, 4, -10, 8, 20, 1};
        long result2 = solution.solve(n2, edges2, budget2);
        System.out.println(result2); // Expected: 17
    }
}