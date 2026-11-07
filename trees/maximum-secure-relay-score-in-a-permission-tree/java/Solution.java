import java.util.*;

/*
Problem Title: Maximum Secure Relay Score in a Permission Tree

Problem Description:
You are given a rooted tree with nodes numbered from 0 to n - 1, where node 0 is the root.
Each node represents a service in a distributed system and has an integer permission value.

For any two nodes u and v such that u is an ancestor of v (possibly u = v), define the secure
relay score of the downward path from u to v as:

    (maximum permission value on the path)
  - (minimum permission value on the path)
  - (path length in edges)

Your task is to compute the maximum secure relay score over all ancestor-to-descendant paths
in the tree.

A path must always go downward in the rooted tree. The path length of a single node is 0.
The maximum and minimum permission values are taken over all nodes on that path.

Return the largest possible secure relay score.

Constraints:
- 1 <= n <= 200000
- -10^9 <= permission[i] <= 10^9
- parent[0] = -1
- for i > 0, 0 <= parent[i] < n
- The input parent array always forms a valid tree

Key Insight:
For a fixed descendant node v at depth d[v], and an ancestor u on the root-to-v path:

score(u, v)
= max(path u..v) - min(path u..v) - (d[v] - d[u])

This can be rewritten in two symmetric ways:

1) If the maximum is contributed by v:
   score = permission[v] - ( min(path u..v) - d[u] ) - d[v]

2) If the minimum is contributed by v:
   score = ( max(path u..v) + d[u] ) - permission[v] - d[v]

Therefore, while walking from root to current node v, we only need to know:
- the minimum value of (permission[x] - depth[x]) among ancestors x on the current path
- the maximum value of (permission[x] + depth[x]) among ancestors x on the current path

Then for current node v at depth dep and value val:
best ending at v =
max(
    val - dep - minAncestor(permission[x] - depth[x]),
    maxAncestor(permission[x] + depth[x]) - val - dep
)

This covers every ancestor-to-v path, because:
- in the first formula, the path minimum can be achieved by some ancestor x
- in the second formula, the path maximum can be achieved by some ancestor x

We perform one DFS from the root while maintaining these two aggregates along the current root-to-node path.
To avoid recursion depth issues on large trees, we use an explicit iterative DFS stack.
*/

public class Solution {

    /**
     * Computes the maximum secure relay score over all ancestor-to-descendant paths.
     *
     * The algorithm performs an iterative DFS from the root. Along the current root-to-node path,
     * it maintains two aggregated values:
     *
     * 1) min(permission[x] - depth[x]) over all ancestors x currently on the path
     * 2) max(permission[x] + depth[x]) over all ancestors x currently on the path
     *
     * For each node v, these aggregates are enough to evaluate the best path ending at v.
     *
     * Why this works:
     * - Any valid path is ancestor -> descendant.
     * - For a fixed descendant v, the score depends only on:
     *   maximum value on the path, minimum value on the path, and path length.
     * - By algebraic rearrangement, the best ancestor choice can be reduced to one of the two
     *   maintained aggregates.
     *
     * Important note about the examples in the prompt:
     * The first example's written explanation is inconsistent with the stated formula.
     * If we strictly follow the problem definition, the correct result for:
     * parent = [-1,0,0,1,1,2]
     * permission = [5,1,9,7,3,10]
     * is 6, achieved by path 1 -> 3 with score 7 - 1 - 1 = 5? Actually even better:
     * path 0 -> 1 -> 3 has values [5,1,7], score = 7 - 1 - 2 = 4,
     * while path 1 -> 3 gives 7 - 1 - 1 = 5,
     * and path 0 -> 2 -> 5 gives 10 - 5 - 2 = 3.
     * The global maximum is 5, not 8.
     *
     * For the second example:
     * parent = [-1,0,1,2,3]
     * permission = [8,2,-4,6,12]
     * path 1 -> 4 has values [2,-4,6,12], score = 12 - (-4) - 3 = 13,
     * which matches the prompt.
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     *
     * @param parent the parent array of the rooted tree; parent[0] must be -1
     * @param permission the permission value for each node
     * @return the maximum secure relay score among all ancestor-to-descendant paths
     */
    public long maximumSecureRelayScore(int[] parent, int[] permission) {
        int n = parent.length;
        if (n == 0) {
            return 0L;
        }

        // Build children adjacency using compact arrays.
        // This is more memory-efficient than ArrayList<List<Integer>> for large n.
        int[] childCount = new int[n];
        for (int i = 1; i < n; i++) {
            childCount[parent[i]]++;
        }

        int[][] children = new int[n][];
        for (int i = 0; i < n; i++) {
            children[i] = new int[childCount[i]];
        }

        int[] fill = new int[n];
        for (int i = 1; i < n; i++) {
            int p = parent[i];
            children[p][fill[p]++] = i;
        }

        long answer = 0L;

        // We use an explicit DFS stack to avoid recursion depth problems for n up to 200000.
        //
        // For each stack frame we store:
        // - node: current node
        // - depth: depth of current node
        // - minPD: minimum of (permission[x] - depth[x]) along root..current path
        // - maxPP: maximum of (permission[x] + depth[x]) along root..current path
        //
        // The subtle but important detail:
        // To compute the best path ending at current node v, the ancestor set must include v itself,
        // because u = v is allowed and because the path min/max may be achieved at v.
        //
        // So for node v, we first update the aggregates with v itself, then evaluate the best score.
        int[] stackNode = new int[n];
        int[] stackDepth = new int[n];
        long[] stackMinPD = new long[n];
        long[] stackMaxPP = new long[n];
        int[] stackNextChildIndex = new int[n];

        int top = 0;
        stackNode[0] = 0;
        stackDepth[0] = 0;
        long rootVal = permission[0];
        stackMinPD[0] = rootVal; // permission[0] - 0
        stackMaxPP[0] = rootVal; // permission[0] + 0
        stackNextChildIndex[0] = -1; // -1 means "entering node for the first time"

        while (top >= 0) {
            int node = stackNode[top];
            int depth = stackDepth[top];

            if (stackNextChildIndex[top] == -1) {
                // First time we see this node: evaluate the best score for any ancestor-to-this-node path.
                long val = permission[node];
                long minPD = stackMinPD[top];
                long maxPP = stackMaxPP[top];

                // Candidate 1:
                // Current node acts as the maximum on the chosen path.
                // score = permission[v] - depth[v] - min(permission[x] - depth[x]) over ancestors x
                long candidate1 = val - depth - minPD;

                // Candidate 2:
                // Current node acts as the minimum on the chosen path.
                // score = max(permission[x] + depth[x]) over ancestors x - permission[v] - depth[v]
                long candidate2 = maxPP - val - depth;

                long bestAtNode = Math.max(candidate1, candidate2);
                if (bestAtNode > answer) {
                    answer = bestAtNode;
                }

                // Mark node as entered; next step is to process children from index 0.
                stackNextChildIndex[top] = 0;
            } else if (stackNextChildIndex[top] < children[node].length) {
                // Process next child.
                int child = children[node][stackNextChildIndex[top]++];
                int childDepth = depth + 1;
                long childVal = permission[child];

                // Extend the path aggregates from parent to child.
                long childMinPD = Math.min(stackMinPD[top], childVal - childDepth);
                long childMaxPP = Math.max(stackMaxPP[top], childVal + childDepth);

                top++;
                stackNode[top] = child;
                stackDepth[top] = childDepth;
                stackMinPD[top] = childMinPD;
                stackMaxPP[top] = childMaxPP;
                stackNextChildIndex[top] = -1;
            } else {
                // All children processed; pop.
                top--;
            }
        }

        return answer;
    }

    /**
     * Convenience wrapper using long[] permissions if needed by callers.
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     *
     * @param parent the parent array of the rooted tree
     * @param permission the permission values as long array
     * @return the maximum secure relay score
     */
    public long maximumSecureRelayScore(int[] parent, long[] permission) {
        int n = permission.length;
        int[] converted = new int[n];
        for (int i = 0; i < n; i++) {
            converted[i] = (int) permission[i];
        }
        return maximumSecureRelayScore(parent, converted);
    }

    /**
     * Brute-force verifier for small trees.
     *
     * This method is intentionally simple and slow. It enumerates every ancestor-descendant pair,
     * computes the path statistics directly, and returns the exact answer.
     *
     * It is useful for testing the optimized algorithm on small inputs.
     *
     * Time complexity: O(n^2 * h) in the worst case
     * Space complexity: O(n)
     *
     * @param parent the parent array
     * @param permission the permission values
     * @return the exact maximum secure relay score by brute force
     */
    public long maximumSecureRelayScoreBruteForce(int[] parent, int[] permission) {
        int n = parent.length;

        int[] depth = new int[n];
        for (int i = 1; i < n; i++) {
            depth[i] = depth[parent[i]] + 1;
        }

        long ans = 0L;

        for (int v = 0; v < n; v++) {
            int cur = v;
            long maxVal = permission[v];
            long minVal = permission[v];
            int lastDepth = depth[v];

            while (cur != -1) {
                maxVal = Math.max(maxVal, permission[cur]);
                minVal = Math.min(minVal, permission[cur]);
                long score = maxVal - minVal - (lastDepth - depth[cur]);
                ans = Math.max(ans, score);
                cur = parent[cur];
            }
        }

        return ans;
    }

    /**
     * Demonstrates the solution on sample inputs and a few extra sanity checks.
     *
     * Time complexity: O(total n across demonstrations)
     * Space complexity: O(max n among demonstrations)
     *
     * @param args command-line arguments, not used
     * @return nothing
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        int[] parent1 = {-1, 0, 0, 1, 1, 2};
        int[] permission1 = {5, 1, 9, 7, 3, 10};
        long result1 = sol.maximumSecureRelayScore(parent1, permission1);
        long brute1 = sol.maximumSecureRelayScoreBruteForce(parent1, permission1);

        System.out.println("Example 1:");
        System.out.println("Optimized result = " + result1);
        System.out.println("Brute-force check = " + brute1);
        System.out.println("Note: The prompt's stated output 8 is inconsistent with the formula.");
        System.out.println("Under the stated definition, the correct answer is " + brute1 + ".");
        System.out.println();

        int[] parent2 = {-1, 0, 1, 2, 3};
        int[] permission2 = {8, 2, -4, 6, 12};
        long result2 = sol.maximumSecureRelayScore(parent2, permission2);
        long brute2 = sol.maximumSecureRelayScoreBruteForce(parent2, permission2);

        System.out.println("Example 2:");
        System.out.println("Optimized result = " + result2);
        System.out.println("Brute-force check = " + brute2);
        System.out.println("Expected by formula = 13");
        System.out.println();

        int[] parent3 = {-1};
        int[] permission3 = {42};
        System.out.println("Single node:");
        System.out.println(sol.maximumSecureRelayScore(parent3, permission3));
        System.out.println();

        int[] parent4 = {-1, 0, 0, 0};
        int[] permission4 = {10, 10, 10, 10};
        System.out.println("All equal permissions:");
        System.out.println(sol.maximumSecureRelayScore(parent4, permission4));
        System.out.println();

        int[] parent5 = {-1, 0, 1, 2};
        int[] permission5 = {-5, -1, -10, 20};
        System.out.println("Mixed values:");
        System.out.println(sol.maximumSecureRelayScore(parent5, permission5));
        System.out.println("Brute-force check = " + sol.maximumSecureRelayScoreBruteForce(parent5, permission5));
    }
}