import java.util.*;

/*
Problem Title: Minimum Relay Hops to Synchronize Field Sensors

Problem Description:
A company deploys wireless sensors across a large farm. Each sensor can directly relay data to some other sensors,
forming a directed graph of communication links. Sensor 0 is the central base station. A sensor is considered
synchronized if it can send a message to sensor 0 by following one or more directed relay links.

You are given an integer n and a list edges where edges[i] = [u, v] means sensor u can directly send data to sensor v.
You may install additional one-way relay boosters. Installing a booster from sensor a to sensor b creates a new
directed edge a -> b. Your goal is to make every sensor synchronized using the minimum number of new boosters.

Return the minimum number of boosters required.

Important details:
- You may choose any pair of sensors when adding a booster.
- Existing edges remain unchanged.
- A sensor already synchronized does not need any extra work.
- If several unsynchronized regions exist, it may be optimal to connect one region to another before reaching sensor 0.

Constraints:
- 1 <= n <= 200000
- 0 <= edges.length <= 300000
- 0 <= u, v < n
- u != v
- Multiple edges between the same pair may appear

Key idea:
1. Compress the graph into Strongly Connected Components (SCCs).
   Inside one SCC, every node can reach every other node, so the whole SCC behaves like one super-node.
2. The SCC graph is a DAG.
3. Let target be the SCC containing node 0.
4. Any SCC that can already reach target needs no new booster.
5. Among the remaining SCCs, each source SCC in that remaining DAG portion needs one booster.
   Why? Because:
   - A source SCC has no outgoing edge to another remaining SCC that could eventually lead to target.
   - So nothing else in the remaining bad region can help it reach target unless we add a booster from it (or from
     some node inside it).
   - One booster from each such source SCC is sufficient, because after connecting that source to target (or to any
     good SCC), all SCCs that can reach that source are still irrelevant; what matters is every bad SCC has some path
     to a connected source in the reversed perspective. In the original DAG, counting zero in-degree among bad SCCs
     gives the minimum number of entry points needed to make all bad SCCs reach target by chaining through the DAG.

More concretely:
- Build SCC DAG.
- Mark all SCCs that can reach target by traversing the reversed SCC DAG starting from target.
- Consider only SCCs not marked good.
- In that induced subgraph of bad SCCs, count how many SCCs have indegree 0.
- That count is the minimum number of boosters required.

This matches the examples:
Example 1:
n = 6, edges = [[1,0],[2,1],[3,4],[4,5]]
SCCs are single nodes. Good nodes are {0,1,2}. Bad nodes are {3,4,5} with edges 3->4->5.
Bad indegrees: 3 has 0, 4 has 1, 5 has 1. Answer = 1.

Example 2:
n = 7, edges = [[1,2],[2,3],[3,1],[4,5],[5,6]]
SCCs: {0}, {1,2,3}, {4}, {5}, {6}
Only SCC {0} is good.
Bad DAG has edges {4}->{5}->{6}; SCC {1,2,3} isolated.
Bad indegree 0 SCCs are {1,2,3} and {4}. Answer = 2.
*/

public class Solution {

    /**
     * Computes the minimum number of one-way boosters needed so that every sensor can reach sensor 0.
     *
     * Algorithm overview:
     * 1. Build adjacency lists for the original graph and the reversed graph.
     * 2. Run Kosaraju's algorithm iteratively (no recursion, to avoid stack overflow on large inputs):
     *    - First pass: compute finishing order on the original graph.
     *    - Second pass: assign SCC ids on the reversed graph in reverse finishing order.
     * 3. Build the SCC DAG.
     * 4. Find the SCC containing node 0.
     * 5. On the reversed SCC DAG, mark every SCC that can reach the SCC of node 0.
     *    These SCCs are already "good" and need no new booster.
     * 6. Among the remaining "bad" SCCs, count how many have indegree 0 considering only edges between bad SCCs.
     *    Each such source bad SCC requires one booster, and this number is minimal.
     *
     * @param n the number of sensors/nodes
     * @param edges directed communication links, where each element is [u, v] meaning u -> v
     * @return the minimum number of boosters required
     * Time complexity: O(n + m), where m = edges.length
     * Space complexity: O(n + m)
     */
    public int minimumBoosters(int n, int[][] edges) {
        // Build adjacency lists for:
        // 1) the original graph
        // 2) the reversed graph
        //
        // We use ArrayList<Integer>[] because it is simple and efficient enough for the given constraints.
        @SuppressWarnings("unchecked")
        ArrayList<Integer>[] graph = new ArrayList[n];
        @SuppressWarnings("unchecked")
        ArrayList<Integer>[] reverseGraph = new ArrayList[n];

        for (int i = 0; i < n; i++) {
            graph[i] = new ArrayList<>();
            reverseGraph[i] = new ArrayList<>();
        }

        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            graph[u].add(v);
            reverseGraph[v].add(u);
        }

        // -----------------------------
        // Step 1: First pass of Kosaraju
        // -----------------------------
        //
        // We need nodes ordered by finishing time in a DFS of the original graph.
        // Because n can be as large as 200000, recursive DFS is risky in Java due to stack overflow.
        // So we implement iterative DFS manually using an explicit stack.
        boolean[] visited = new boolean[n];
        int[] order = new int[n];
        int orderSize = 0;

        for (int start = 0; start < n; start++) {
            if (visited[start]) {
                continue;
            }
            orderSize = fillFinishOrderIterative(start, graph, visited, order, orderSize);
        }

        // ------------------------------
        // Step 2: Second pass of Kosaraju
        // ------------------------------
        //
        // Process nodes in reverse finishing order on the reversed graph.
        // Every DFS/BFS in this pass identifies one SCC.
        int[] component = new int[n];
        Arrays.fill(component, -1);

        int componentCount = 0;

        for (int i = n - 1; i >= 0; i--) {
            int node = order[i];
            if (component[node] != -1) {
                continue;
            }
            assignComponentIterative(node, componentCount, reverseGraph, component);
            componentCount++;
        }

        // If the whole graph is one SCC, then every node already reaches every other node,
        // including node 0, so no booster is needed.
        if (componentCount == 1) {
            return 0;
        }

        int targetComponent = component[0];

        // -----------------------------------------
        // Step 3: Build SCC DAG and reversed SCC DAG
        // -----------------------------------------
        //
        // Each original edge u -> v becomes:
        // component[u] -> component[v] if the components differ.
        //
        // We do not need to deduplicate edges for correctness.
        // Multiple parallel edges only cause repeated processing, but total is still O(m).
        @SuppressWarnings("unchecked")
        ArrayList<Integer>[] componentGraph = new ArrayList[componentCount];
        @SuppressWarnings("unchecked")
        ArrayList<Integer>[] reverseComponentGraph = new ArrayList[componentCount];

        for (int i = 0; i < componentCount; i++) {
            componentGraph[i] = new ArrayList<>();
            reverseComponentGraph[i] = new ArrayList<>();
        }

        for (int[] edge : edges) {
            int cu = component[edge[0]];
            int cv = component[edge[1]];
            if (cu != cv) {
                componentGraph[cu].add(cv);
                reverseComponentGraph[cv].add(cu);
            }
        }

        // ---------------------------------------------------------
        // Step 4: Mark all SCCs that can already reach targetComponent
        // ---------------------------------------------------------
        //
        // In the SCC DAG, an SCC X can reach targetComponent iff in the reversed SCC DAG,
        // targetComponent can reach X.
        //
        // So we start from targetComponent in reverseComponentGraph and mark all reachable SCCs as good.
        boolean[] good = new boolean[componentCount];
        markReachableComponents(targetComponent, reverseComponentGraph, good);

        // ------------------------------------------------------------
        // Step 5: Among bad SCCs, count those with indegree 0 in bad DAG
        // ------------------------------------------------------------
        //
        // We only care about SCCs that are NOT good.
        // For every edge badA -> badB, we increase indegree of badB.
        // Then every bad SCC with indegree 0 is a source in the bad subgraph.
        // Each such source needs one booster.
        int[] badIndegree = new int[componentCount];

        for (int from = 0; from < componentCount; from++) {
            if (good[from]) {
                continue;
            }
            for (int to : componentGraph[from]) {
                if (!good[to]) {
                    badIndegree[to]++;
                }
            }
        }

        int answer = 0;
        for (int c = 0; c < componentCount; c++) {
            if (!good[c] && badIndegree[c] == 0) {
                answer++;
            }
        }

        return answer;
    }

    /**
     * Performs the first pass of Kosaraju's algorithm iteratively from a given start node,
     * appending nodes to the finishing-order array when their DFS exploration completes.
     *
     * This method simulates recursive DFS using two stacks:
     * - one stack for nodes
     * - one stack for the next neighbor index to process for each node
     *
     * @param start the starting node for DFS
     * @param graph the original directed graph
     * @param visited marks whether a node has been visited in the first pass
     * @param order array where nodes are written in finishing order
     * @param orderSize current number of filled positions in order
     * @return updated orderSize after processing the DFS tree rooted at start
     * Time complexity: O(size of explored subgraph)
     * Space complexity: O(size of DFS stack)
     */
    public int fillFinishOrderIterative(int start, List<Integer>[] graph, boolean[] visited, int[] order, int orderSize) {
        Deque<Integer> nodeStack = new ArrayDeque<>();
        Deque<Integer> indexStack = new ArrayDeque<>();

        visited[start] = true;
        nodeStack.push(start);
        indexStack.push(0);

        while (!nodeStack.isEmpty()) {
            int node = nodeStack.peek();
            int nextIndex = indexStack.pop();

            if (nextIndex < graph[node].size()) {
                // We still have neighbors left to process for this node.
                // Put back the incremented nextIndex so that when we return to this node,
                // we continue from the following neighbor.
                indexStack.push(nextIndex + 1);

                int neighbor = graph[node].get(nextIndex);
                if (!visited[neighbor]) {
                    visited[neighbor] = true;
                    nodeStack.push(neighbor);
                    indexStack.push(0);
                }
            } else {
                // All neighbors have been processed.
                // This is the "finish time" moment for the node.
                nodeStack.pop();
                order[orderSize++] = node;
            }
        }

        return orderSize;
    }

    /**
     * Assigns the same SCC id to all nodes reachable from the given start node in the reversed graph.
     * This is the second pass of Kosaraju's algorithm, implemented iteratively.
     *
     * @param start the node from which to start the SCC traversal
     * @param componentId the SCC id to assign
     * @param reverseGraph the reversed original graph
     * @param component array storing SCC id for each node; -1 means unassigned
     * @return nothing; component array is updated in place
     * Time complexity: O(size of the discovered SCC and traversed edges)
     * Space complexity: O(size of traversal stack)
     */
    public void assignComponentIterative(int start, int componentId, List<Integer>[] reverseGraph, int[] component) {
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(start);
        component[start] = componentId;

        while (!stack.isEmpty()) {
            int node = stack.pop();
            for (int neighbor : reverseGraph[node]) {
                if (component[neighbor] == -1) {
                    component[neighbor] = componentId;
                    stack.push(neighbor);
                }
            }
        }
    }

    /**
     * Marks all SCCs reachable from the given start SCC in the provided component graph.
     *
     * In this problem we call it with the reversed SCC DAG and the SCC containing node 0.
     * Therefore, every marked SCC is one that can reach node 0 in the original graph.
     *
     * @param startComponent the SCC from which traversal begins
     * @param graph the graph of SCCs to traverse
     * @param marked output array; marked[c] becomes true if component c is reachable
     * @return nothing; marked array is updated in place
     * Time complexity: O(number of reachable SCCs + edges among them)
     * Space complexity: O(number of reachable SCCs)
     */
    public void markReachableComponents(int startComponent, List<Integer>[] graph, boolean[] marked) {
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(startComponent);
        marked[startComponent] = true;

        while (!stack.isEmpty()) {
            int current = stack.pop();
            for (int next : graph[current]) {
                if (!marked[next]) {
                    marked[next] = true;
                    stack.push(next);
                }
            }
        }
    }

    /**
     * Convenience overload that accepts edges as a List of int arrays.
     *
     * @param n the number of sensors/nodes
     * @param edgesList list of directed edges [u, v]
     * @return the minimum number of boosters required
     * Time complexity: O(n + m)
     * Space complexity: O(n + m)
     */
    public int minimumBoosters(int n, List<int[]> edgesList) {
        int[][] edges = new int[edgesList.size()][2];
        for (int i = 0; i < edgesList.size(); i++) {
            edges[i] = edgesList.get(i);
        }
        return minimumBoosters(n, edges);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(total input size of the demo cases)
     * Space complexity: O(total input size of the demo cases)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int n1 = 6;
        int[][] edges1 = {
            {1, 0},
            {2, 1},
            {3, 4},
            {4, 5}
        };
        int result1 = solution.minimumBoosters(n1, edges1);
        System.out.println("Example 1 result: " + result1);
        // Expected: 1

        int n2 = 7;
        int[][] edges2 = {
            {1, 2},
            {2, 3},
            {3, 1},
            {4, 5},
            {5, 6}
        };
        int result2 = solution.minimumBoosters(n2, edges2);
        System.out.println("Example 2 result: " + result2);
        // Expected: 2

        // Additional quick sanity checks:

        int n3 = 1;
        int[][] edges3 = {};
        int result3 = solution.minimumBoosters(n3, edges3);
        System.out.println("Single node result: " + result3);
        // Expected: 0

        int n4 = 4;
        int[][] edges4 = {
            {1, 0},
            {2, 0},
            {3, 0}
        };
        int result4 = solution.minimumBoosters(n4, edges4);
        System.out.println("All already synchronized result: " + result4);
        // Expected: 0

        int n5 = 4;
        int[][] edges5 = {
            {1, 2},
            {2, 3}
        };
        int result5 = solution.minimumBoosters(n5, edges5);
        System.out.println("Chain away from 0 result: " + result5);
        // Expected: 1 (add 3 -> 0)
    }
}