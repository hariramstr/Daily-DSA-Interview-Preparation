import java.util.*;

/*
Problem Title: Maximum Knowledge Gain from Unlocking Course Modules

Problem Description:
You are designing a personalized learning path on an online platform. There are n course modules labeled from 0 to n - 1. Each module i has a knowledge value gain[i]. Some modules are locked behind prerequisites: to study module i, you must have already completed every module in prereq[i]. The prerequisite graph is guaranteed to be a directed acyclic graph (DAG).

You are also given an integer k. You may complete at most k modules in total. Your goal is to maximize the sum of knowledge values of the completed modules, subject to the prerequisite rules.

Return the maximum total knowledge value that can be achieved.

A module can only be taken once, and completing a module counts toward the k-module limit. You do not need to complete exactly k modules; taking fewer is allowed if that leads to a better or only feasible plan.

This problem is intentionally designed so that greedy choices do not work in general. A high-value module may require spending several picks on low-value prerequisites, while a smaller independent set may produce a better total under the same budget.

Constraints:
- 1 <= n <= 100
- 0 <= gain[i] <= 10^6
- 0 <= k <= n
- 0 <= total number of prerequisite edges <= 300
- prereq[i] contains distinct module indices
- The prerequisite graph is a DAG

Example 1:
Input:
n = 5
gain = [3, 4, 8, 5, 10]
prereq = [[], [0], [0], [1,2], []]
k = 3
Output:
21

Explanation:
One optimal plan is to take modules 0, 2, and 4.
- Module 2 requires module 0, which is included.
- Module 4 has no prerequisites.
Total gain = 3 + 8 + 10 = 21.

Example 2:
Input:
n = 6
gain = [5, 1, 7, 4, 6, 20]
prereq = [[], [0], [1], [], [3], [2,4]]
k = 4
Output:
17

Explanation:
To take module 5, you must also take 0, 1, 2, 3, and 4, which exceeds the budget.
A valid optimal plan is modules 0, 1, 2, and 3 for total gain 17.
*/

public class Solution {

    /**
     * Solves the problem by reducing the prerequisite-closure selection problem on a DAG
     * to a minimum s-t cut / maximum flow problem.
     *
     * Core idea:
     * A chosen set of modules must be "closed under prerequisites":
     * if module v is chosen, then every prerequisite u of v must also be chosen.
     *
     * We model this as a maximum weight closure problem:
     * - Each module has profit = gain[i].
     * - We want the best closed subset with size at most k.
     *
     * To enforce the size limit, we use Lagrangian relaxation:
     * for a penalty lambda per chosen module, each chosen module contributes
     * adjusted profit = gain[i] - lambda.
     *
     * For a fixed lambda, the best closed subset can be found by max-weight closure,
     * which is solvable by min-cut:
     * - Positive adjusted profit nodes connect from source.
     * - Negative adjusted profit nodes connect to sink.
     * - For every prerequisite edge (u is prerequisite of v), add infinite-capacity edge v -> u.
     *   This forces: if v is selected, u must also be selected.
     *
     * Let:
     *   F(lambda) = max over closed sets S of (sum(gain[i] for i in S) - lambda * |S|)
     *
     * As lambda increases, the optimal chosen size is non-increasing.
     * We binary search lambda to find the smallest lambda such that the optimal size <= k.
     *
     * Important subtlety:
     * Because gains are integers, the optimal closure for a fixed lambda may not be unique.
     * Among all closures maximizing adjusted profit, we need the one with the largest size
     * when lambda is just below the threshold, and the one with the smallest size when above.
     *
     * To make the binary search and final reconstruction robust, we slightly perturb profits:
     * - For "prefer larger size among equal adjusted profit", use adjusted profit:
     *       gain[i] * SCALE - lambdaScaled + 1
     *   so every selected node gets a tiny +1 tie-break reward.
     * - For "prefer smaller size among equal adjusted profit", use:
     *       gain[i] * SCALE - lambdaScaled - 1
     *   so every selected node gets a tiny -1 tie-break penalty.
     *
     * After finding the threshold, we evaluate both sides and take the best feasible true gain.
     *
     * @param n      number of modules
     * @param gain   gain[i] is the knowledge value of module i
     * @param prereq prereq[i] contains all prerequisites of module i
     * @param k      maximum number of modules allowed
     * @return maximum achievable total knowledge value
     *
     * Time complexity:
     * Let V = n + 2 and E = total prerequisite edges.
     * Each max-flow run is roughly O(V^2 * E) with Dinic in practice much faster on these constraints.
     * We perform O(log C) flow computations where C depends on gain range.
     * Overall practical complexity is acceptable for n <= 100 and edges <= 300.
     *
     * Space complexity:
     * O(V + E) for the flow graph per run.
     */
    public long maximumKnowledgeGain(int n, int[] gain, List<List<Integer>> prereq, int k) {
        if (k <= 0 || n == 0) {
            return 0L;
        }

        long totalGain = 0L;
        int maxGain = 0;
        for (int g : gain) {
            totalGain += g;
            maxGain = Math.max(maxGain, g);
        }

        // If we can take all modules, the whole set is always feasible because the graph is a DAG
        // and taking all modules trivially satisfies all prerequisites.
        if (k >= n) {
            return totalGain;
        }

        // We work with scaled integer lambdas to avoid floating point issues.
        // SCALE must be larger than the tie-break magnitude (which is 1).
        final long SCALE = 1_000L;

        // Search range for lambda:
        // Since gains are in [0, 1e6], a lambda above maxGain makes every adjusted profit non-positive,
        // so the optimal set becomes empty.
        long low = -1L * SCALE; // definitely can produce size >= k in many cases
        long high = ((long) maxGain + 1L) * SCALE; // definitely produces size 0

        // Binary search for the smallest lambda such that
        // the optimal closure size (with "prefer larger size" tie-break) is <= k.
        while (low + 1 < high) {
            long mid = low + (high - low) / 2;
            ClosureResult result = solveClosure(n, gain, prereq, mid, 1L, SCALE);

            if (result.count <= k) {
                high = mid;
            } else {
                low = mid;
            }
        }

        long answer = 0L;

        // Candidate 1:
        // Use lambda = high and prefer smaller size among equal adjusted-profit solutions.
        // This gives a feasible solution with count <= k.
        ClosureResult candidateHighSmall = solveClosure(n, gain, prereq, high, -1L, SCALE);
        if (candidateHighSmall.count <= k) {
            answer = Math.max(answer, candidateHighSmall.trueGain);
        }

        // Candidate 2:
        // Use lambda = low and prefer larger size among equal adjusted-profit solutions.
        // This may have count > k, but if it is <= k we can use it.
        ClosureResult candidateLowLarge = solveClosure(n, gain, prereq, low, 1L, SCALE);
        if (candidateLowLarge.count <= k) {
            answer = Math.max(answer, candidateLowLarge.trueGain);
        }

        // Candidate 3:
        // Also evaluate lambda = high with "prefer larger size".
        ClosureResult candidateHighLarge = solveClosure(n, gain, prereq, high, 1L, SCALE);
        if (candidateHighLarge.count <= k) {
            answer = Math.max(answer, candidateHighLarge.trueGain);
        }

        // Candidate 4:
        // Evaluate lambda = low with "prefer smaller size".
        ClosureResult candidateLowSmall = solveClosure(n, gain, prereq, low, -1L, SCALE);
        if (candidateLowSmall.count <= k) {
            answer = Math.max(answer, candidateLowSmall.trueGain);
        }

        return answer;
    }

    /**
     * Computes the best closure for a fixed penalized objective:
     *
     * maximize:
     *   sum over chosen i of (gain[i] * SCALE - lambdaScaled + tieBias)
     *
     * subject to prerequisite closure:
     *   if i is chosen, then every prerequisite p in prereq[i] must also be chosen.
     *
     * The returned result includes:
     * - number of chosen modules
     * - true unpenalized gain sum
     *
     * Construction details:
     * - If adjusted profit > 0, connect source -> i with that capacity.
     * - If adjusted profit < 0, connect i -> sink with -adjusted profit.
     * - For each prerequisite p of i, add infinite edge i -> p.
     *   This means selecting i while not selecting p would cut an infinite edge, impossible.
     *
     * After max-flow, the chosen set is exactly the set of nodes reachable from source
     * in the residual graph.
     *
     * @param n            number of modules
     * @param gain         module gains
     * @param prereq       prerequisite lists
     * @param lambdaScaled scaled penalty per chosen module
     * @param tieBias      +1 to prefer larger chosen size on ties, -1 to prefer smaller size on ties
     * @param scale        scaling factor used for lambda and gains
     * @return closure result containing chosen count and true gain
     *
     * Time complexity:
     * One max-flow computation on O(n) nodes and O(n + edges) graph size.
     *
     * Space complexity:
     * O(n + edges).
     */
    public ClosureResult solveClosure(int n, int[] gain, List<List<Integer>> prereq,
                                      long lambdaScaled, long tieBias, long scale) {
        int source = n;
        int sink = n + 1;
        Dinic dinic = new Dinic(n + 2);

        long positiveSum = 0L;

        // A safely large "infinite" capacity.
        // It only needs to exceed any possible finite cut value.
        long inf = 1L;
        for (int g : gain) {
            inf += (long) g * scale + Math.abs(lambdaScaled) + 5L;
        }

        // Build profit edges.
        for (int i = 0; i < n; i++) {
            long adjusted = (long) gain[i] * scale - lambdaScaled + tieBias;

            if (adjusted > 0) {
                dinic.addEdge(source, i, adjusted);
                positiveSum += adjusted;
            } else if (adjusted < 0) {
                dinic.addEdge(i, sink, -adjusted);
            }
        }

        // Build prerequisite implication edges:
        // if i is selected, every p in prereq[i] must also be selected.
        // Therefore add i -> p with infinite capacity.
        for (int i = 0; i < n; i++) {
            for (int p : prereq.get(i)) {
                dinic.addEdge(i, p, inf);
            }
        }

        dinic.maxFlow(source, sink);

        boolean[] reachable = dinic.minCutReachableFromSource(source);

        int count = 0;
        long trueGain = 0L;

        // Nodes reachable from source in the residual graph are exactly the selected closure.
        for (int i = 0; i < n; i++) {
            if (reachable[i]) {
                count++;
                trueGain += gain[i];
            }
        }

        return new ClosureResult(count, trueGain);
    }

    /**
     * Convenience helper that accepts prerequisites as a 2D int array.
     *
     * @param n          number of modules
     * @param gain       gain values
     * @param prereqArr  prereqArr[i] contains prerequisites of module i
     * @param k          maximum number of modules allowed
     * @return maximum achievable total knowledge value
     *
     * Time complexity:
     * Same as {@link #maximumKnowledgeGain(int, int[], List, int)} plus linear conversion time.
     *
     * Space complexity:
     * O(n + total edges) for converted lists.
     */
    public long maximumKnowledgeGain(int n, int[] gain, int[][] prereqArr, int k) {
        List<List<Integer>> prereq = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            List<Integer> list = new ArrayList<>();
            if (prereqArr[i] != null) {
                for (int p : prereqArr[i]) {
                    list.add(p);
                }
            }
            prereq.add(list);
        }
        return maximumKnowledgeGain(n, gain, prereq, k);
    }

    /**
     * Demonstrates the solution on the sample inputs from the statement.
     *
     * @param args command-line arguments (unused)
     *
     * @return nothing
     *
     * Time complexity:
     * Depends on the invoked solver calls.
     *
     * Space complexity:
     * Depends on the invoked solver calls.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int n1 = 5;
        int[] gain1 = {3, 4, 8, 5, 10};
        int[][] prereq1 = {
                {},
                {0},
                {0},
                {1, 2},
                {}
        };
        int k1 = 3;
        long result1 = solution.maximumKnowledgeGain(n1, gain1, prereq1, k1);
        System.out.println(result1); // Expected: 21

        // Example 2
        int n2 = 6;
        int[] gain2 = {5, 1, 7, 4, 6, 20};
        int[][] prereq2 = {
                {},
                {0},
                {1},
                {},
                {3},
                {2, 4}
        };
        int k2 = 4;
        long result2 = solution.maximumKnowledgeGain(n2, gain2, prereq2, k2);
        System.out.println(result2); // Expected: 17
    }

    /**
     * Simple immutable holder for a closure solution.
     */
    public static class ClosureResult {
        int count;
        long trueGain;

        /**
         * Creates a result object.
         *
         * @param count    number of selected modules
         * @param trueGain sum of original gains of selected modules
         */
        public ClosureResult(int count, long trueGain) {
            this.count = count;
            this.trueGain = trueGain;
        }
    }

    /**
     * Dinic's maximum flow implementation using long capacities.
     *
     * This is a standard and efficient flow algorithm for sparse graphs.
     * It repeatedly:
     * 1. Builds a BFS level graph from source.
     * 2. Sends blocking flow using DFS along level-respecting edges.
     */
    public static class Dinic {
        private final int n;
        private final List<Edge>[] graph;
        private int[] level;
        private int[] ptr;

        /**
         * Creates a flow graph with the given number of vertices.
         *
         * @param n number of vertices
         *
         * @return nothing
         *
         * Time complexity:
         * O(n)
         *
         * Space complexity:
         * O(n)
         */
        @SuppressWarnings("unchecked")
        public Dinic(int n) {
            this.n = n;
            this.graph = new ArrayList[n];
            for (int i = 0; i < n; i++) {
                graph[i] = new ArrayList<>();
            }
            this.level = new int[n];
            this.ptr = new int[n];
        }

        /**
         * Adds a directed edge u -> v with the given capacity.
         * Also adds the reverse edge with capacity 0.
         *
         * @param u   from vertex
         * @param v   to vertex
         * @param cap capacity
         *
         * @return nothing
         *
         * Time complexity:
         * O(1)
         *
         * Space complexity:
         * O(1) additional per edge
         */
        public void addEdge(int u, int v, long cap) {
            Edge a = new Edge(v, graph[v].size(), cap);
            Edge b = new Edge(u, graph[u].size(), 0L);
            graph[u].add(a);
            graph[v].add(b);
        }

        /**
         * Computes the maximum flow from source to sink.
         *
         * @param s source vertex
         * @param t sink vertex
         * @return maximum flow value
         *
         * Time complexity:
         * Standard Dinic complexity; very efficient in practice for these constraints.
         *
         * Space complexity:
         * O(n)
         */
        public long maxFlow(int s, int t) {
            long flow = 0L;

            while (bfs(s, t)) {
                Arrays.fill(ptr, 0);

                while (true) {
                    long pushed = dfs(s, t, Long.MAX_VALUE / 4);
                    if (pushed == 0L) {
                        break;
                    }
                    flow += pushed;
                }
            }

            return flow;
        }

        /**
         * After max-flow is computed, finds which vertices are reachable from source
         * in the residual graph. This identifies the source side of the minimum cut.
         *
         * @param s source vertex
         * @return boolean array where true means reachable from source in residual graph
         *
         * Time complexity:
         * O(V + E)
         *
         * Space complexity:
         * O(V)
         */
        public boolean[] minCutReachableFromSource(int s) {
            boolean[] visited = new boolean[n];
            Deque<Integer> queue = new ArrayDeque<>();
            queue.add(s);
            visited[s] = true;

            while (!queue.isEmpty()) {
                int u = queue.poll();
                for (Edge e : graph[u]) {
                    if (!visited[e.to] && e.cap > 0) {
                        visited[e.to] = true