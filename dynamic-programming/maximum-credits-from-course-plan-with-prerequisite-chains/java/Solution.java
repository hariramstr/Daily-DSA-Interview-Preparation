import java.util.*;

/*
 * Title: Maximum Credits from Course Plan with Prerequisite Chains
 * Difficulty: Medium
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are given a list of university courses numbered from 0 to n - 1. Each course i has a credit value credits[i]
 * and may depend on at most one prerequisite course prereq[i]. If prereq[i] = -1, then course i has no prerequisite.
 * A course can only be taken if its prerequisite, and that course's prerequisite, and so on, have all been taken.
 * In other words, selecting a course requires selecting the entire chain leading to it.
 *
 * You are also given an integer maxCourses representing the maximum number of courses a student can take this semester.
 * Your task is to return the maximum total credits the student can earn by choosing at most maxCourses courses while
 * satisfying all prerequisite rules.
 *
 * The prerequisite graph is guaranteed to contain no cycles. Because each course has at most one prerequisite, the graph
 * forms a collection of rooted chains and trees. A valid selection must be closed under prerequisites: if a course is
 * chosen, every ancestor on its prerequisite path must also be chosen.
 *
 * Design an algorithm that computes the best achievable total credits.
 *
 * Constraints:
 * - 1 <= n <= 1000
 * - 1 <= credits[i] <= 10^4
 * - -1 <= prereq[i] < n
 * - prereq[i] != i
 * - The prerequisite graph is acyclic
 * - 1 <= maxCourses <= n
 *
 * Example 1:
 * Input: credits = [3, 5, 4, 8], prereq = [-1, 0, 1, -1], maxCourses = 3
 * Output: 16
 * Explanation:
 * Course 2 requires course 1, which requires course 0.
 * Valid selections with at most 3 courses include:
 * - [3] => 8
 * - [0, 1] => 8
 * - [0, 1, 2] => 12
 * - [0, 3] => 11
 * - [0, 1, 3] => 16
 * Therefore the best answer is 16.
 *
 * Example 2:
 * Input: credits = [2, 7, 6, 4, 5], prereq = [-1, 0, 0, 1, 1], maxCourses = 3
 * Output: 15
 * Explanation:
 * One optimal choice is [0, 1, 2], which is valid and gives 2 + 7 + 6 = 15.
 *
 * Core Idea:
 * This is a tree-DP / knapsack-on-tree problem.
 *
 * Because each node has at most one prerequisite, edges naturally go from prerequisite -> dependent course.
 * So each root is a course with no prerequisite, and every valid chosen set inside a rooted tree must be
 * "ancestor-closed": if we choose a node, we must also choose its parent, grandparent, etc.
 *
 * We solve this by:
 * 1. Building the forest of prerequisite trees.
 * 2. Running DFS on each tree.
 * 3. For each node u, computing dp[u][k]:
 *      maximum credits obtainable by selecting exactly k courses from the subtree of u,
 *      under the rule that if k > 0 then u must be selected.
 * 4. Merging children one by one using knapsack transitions.
 * 5. Since there can be multiple roots, we add a virtual super-root with credit 0 and make every real root
 *    its child. Then the answer is the best value among selecting 1..maxCourses+1 nodes from the super-root
 *    state, subtracting the super-root itself conceptually by indexing carefully.
 *
 * A cleaner equivalent implementation used below:
 * - Add super-root n with credit 0.
 * - Every original root becomes a child of super-root.
 * - Compute dp for super-root as well.
 * - If we select t nodes in the super-root subtree, one of them is the super-root itself.
 *   So selecting at most maxCourses real courses corresponds to selecting exactly 1..maxCourses+1 total nodes
 *   including the super-root.
 * - Therefore answer = max(dp[superRoot][t]) for t = 1..maxCourses+1.
 *
 * This guarantees correctness for forests and handles combining multiple independent prerequisite trees.
 */

public class Solution {

    /**
     * A large negative value used to represent an impossible DP state.
     * We avoid Integer.MIN_VALUE to prevent overflow when adding credits.
     */
    private static final long NEG_INF = Long.MIN_VALUE / 4;

    /**
     * Adjacency list of the prerequisite forest after adding a virtual super-root.
     * children[u] contains all courses that directly depend on u.
     */
    private List<Integer>[] children;

    /**
     * Credit values for all nodes, including the virtual super-root at index n with credit 0.
     */
    private int[] values;

    /**
     * Maximum number of real courses allowed.
     * The DP will work up to limit = maxCourses + 1 because of the virtual super-root.
     */
    private int limit;

    /**
     * subtreeSize[u] = number of nodes in the subtree rooted at u,
     * counting u itself and including the virtual super-root if u is that node.
     */
    private int[] subtreeSize;

    /**
     * Computes the maximum total credits obtainable by selecting at most maxCourses courses
     * while respecting prerequisite closure.
     *
     * The algorithm:
     * 1. Build a forest where an edge prereq[i] -> i means course i depends on prereq[i].
     * 2. Add a virtual super-root connected to every real root.
     * 3. Run tree DP:
     *      dp[u][k] = maximum credits from subtree(u) using exactly k selected nodes,
     *                 with the rule that if k > 0, node u must be selected.
     * 4. For a node u:
     *      - Start with selecting only u: dp[u][1] = credits[u]
     *      - Merge each child v using knapsack transitions
     *      - It is always allowed to take 0 nodes from a child subtree
     * 5. For the super-root, selecting at most maxCourses real courses means selecting
     *    between 1 and maxCourses + 1 total nodes including the super-root.
     *
     * @param credits the credit value of each course
     * @param prereq prereq[i] is the prerequisite of course i, or -1 if none
     * @param maxCourses the maximum number of real courses that may be selected
     * @return the maximum total credits achievable
     *
     * Time complexity note:
     * O(n * maxCourses^2) in the worst case due to knapsack-style subtree merges.
     *
     * Space complexity note:
     * O(n * maxCourses) in the worst case across recursion and DP arrays.
     */
    public int maxCredits(int[] credits, int[] prereq, int maxCourses) {
        int n = credits.length;
        int superRoot = n;
        this.limit = maxCourses + 1;

        values = new int[n + 1];
        System.arraycopy(credits, 0, values, 0, n);
        values[superRoot] = 0;

        children = new ArrayList[n + 1];
        for (int i = 0; i <= n; i++) {
            children[i] = new ArrayList<>();
        }

        for (int course = 0; course < n; course++) {
            if (prereq[course] == -1) {
                children[superRoot].add(course);
            } else {
                children[prereq[course]].add(course);
            }
        }

        subtreeSize = new int[n + 1];

        long[] superDp = dfs(superRoot);

        long answer = 0;
        for (int totalSelectedIncludingSuperRoot = 1;
             totalSelectedIncludingSuperRoot <= Math.min(limit, subtreeSize[superRoot]);
             totalSelectedIncludingSuperRoot++) {
            answer = Math.max(answer, superDp[totalSelectedIncludingSuperRoot]);
        }

        return (int) answer;
    }

    /**
     * Performs a DFS and computes the DP array for the subtree rooted at u.
     *
     * Meaning of returned array dp:
     * - dp[k] = maximum credits obtainable by selecting exactly k nodes from subtree(u)
     * - If k > 0, node u itself must be included
     * - dp[0] is impossible for ordinary nodes in this implementation of subtree DP,
     *   because this DP is "anchored" at u once we are inside the node.
     *   However, when merging a child into its parent, the parent is allowed to take
     *   0 nodes from that child by simply not using the child's DP at all.
     *
     * Detailed merge logic:
     * - Initially, before processing children, the only valid selection is taking u itself:
     *     dp[1] = values[u]
     * - Then for each child v:
     *     We already have current dp for processed children.
     *     We compute childDp for v.
     *     For every current count i and child count j:
     *       next[i + j] = max(next[i + j], dp[i] + childDp[j])
     *     Also, we can skip the child entirely:
     *       next[i] = max(next[i], dp[i])
     *
     * This works because if we choose anything from child subtree, child v must be chosen,
     * and since u is already chosen in dp[i], the prerequisite condition is satisfied.
     *
     * @param u the current node
     * @return a DP array for subtree(u)
     *
     * Time complexity note:
     * Across the whole tree/forest, the total complexity is O(n * maxCourses^2).
     *
     * Space complexity note:
     * O(maxCourses) auxiliary space per active recursion frame, plus recursion stack.
     */
    private long[] dfs(int u) {
        long[] dp = new long[limit + 1];
        Arrays.fill(dp, NEG_INF);

        dp[1] = values[u];
        subtreeSize[u] = 1;

        for (int v : children[u]) {
            long[] childDp = dfs(v);

            long[] next = new long[limit + 1];
            Arrays.fill(next, NEG_INF);

            int maxCurrent = Math.min(limit, subtreeSize[u]);
            int maxChild = Math.min(limit, subtreeSize[v]);

            for (int i = 1; i <= maxCurrent; i++) {
                if (dp[i] == NEG_INF) {
                    continue;
                }

                next[i] = Math.max(next[i], dp[i]);

                for (int j = 1; j <= maxChild && i + j <= limit; j++) {
                    if (childDp[j] == NEG_INF) {
                        continue;
                    }
                    next[i + j] = Math.max(next[i + j], dp[i] + childDp[j]);
                }
            }

            dp = next;
            subtreeSize[u] += subtreeSize[v];
        }

        return dp;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     *
     * Time complexity note:
     * O(1) for the demonstration itself, excluding the calls to maxCredits.
     *
     * Space complexity note:
     * O(1) extra space for the demonstration itself.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] credits1 = {3, 5, 4, 8};
        int[] prereq1 = {-1, 0, 1, -1};
        int maxCourses1 = 3;
        int result1 = solution.maxCredits(credits1, prereq1, maxCourses1);
        System.out.println("Example 1 Result: " + result1);
        System.out.println("Expected: 16");

        int[] credits2 = {2, 7, 6, 4, 5};
        int[] prereq2 = {-1, 0, 0, 1, 1};
        int maxCourses2 = 3;
        int result2 = solution.maxCredits(credits2, prereq2, maxCourses2);
        System.out.println("Example 2 Result: " + result2);
        System.out.println("Expected: 15");

        int[] credits3 = {10};
        int[] prereq3 = {-1};
        int maxCourses3 = 1;
        int result3 = solution.maxCredits(credits3, prereq3, maxCourses3);
        System.out.println("Single Course Result: " + result3);
        System.out.println("Expected: 10");

        int[] credits4 = {4, 9, 3, 7, 6};
        int[] prereq4 = {-1, -1, 0, 0, 1};
        int maxCourses4 = 2;
        int result4 = solution.maxCredits(credits4, prereq4, maxCourses4);
        System.out.println("Additional Test Result: " + result4);
        System.out.println("Expected: 16");
    }
}