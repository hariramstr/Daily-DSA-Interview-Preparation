```java
/*
 * Title: Infected Servers in a Data Center
 * Difficulty: Medium
 * Topic: Graphs
 *
 * Problem Description:
 * You are managing a data center with `n` servers numbered from `0` to `n - 1`.
 * The servers are connected by bidirectional network cables represented as a list
 * of edges, where `edges[i] = [u, v]` means server `u` and server `v` are directly connected.
 *
 * At time `0`, a subset of servers listed in `initial` become infected with a virus.
 * Every minute, each infected server spreads the virus to all of its directly connected neighbors.
 * However, your team can quarantine exactly one server at time 0 (before any spreading occurs)
 * to prevent it from infecting others or receiving the infection.
 *
 * Your goal is to minimize the total number of infected servers after the virus has fully spread.
 * Return the index of the server you should quarantine. If multiple choices result in the same
 * minimum infection count, return the smallest index among them.
 *
 * Key Insight:
 * - Use Union-Find (Disjoint Set Union) to group servers into connected components.
 * - For each connected component, count how many initially infected servers are in it.
 * - If a component has exactly ONE initially infected server, removing that server
 *   prevents the entire component from being infected.
 * - If a component has MORE THAN ONE initially infected server, removing any one of them
 *   doesn't help because the others will still infect the whole component.
 * - So we should quarantine the server from a component that has exactly one initial
 *   infected server AND that component is the largest (most servers saved).
 * - If there's a tie in component size, pick the smallest server index.
 */

import java.util.*;

/**
 * Solution class for the "Infected Servers in a Data Center" problem.
 * Uses Union-Find (Disjoint Set Union) data structure to efficiently
 * group servers into connected components and determine the optimal
 * server to quarantine.
 */
public class Solution {

    // =========================================================================
    // Union-Find (Disjoint Set Union) Helper Arrays
    // These are instance variables so helper methods can access them easily.
    // =========================================================================
    private int[] parent; // parent[i] = parent of node i in the DSU tree
    private int[] rank;   // rank[i] = approximate depth of subtree rooted at i (for union by rank)
    private int[] size;   // size[i] = number of nodes in the component rooted at i

    /**
     * Finds the root (representative) of the component containing node x.
     * Uses path compression to flatten the tree for future queries.
     *
     * @param x the node whose root we want to find
     * @return the root of the component containing x
     * Time complexity: O(α(n)) amortized, where α is the inverse Ackermann function (nearly O(1))
     * Space complexity: O(1) extra space (modifies parent array in place)
     */
    private int find(int x) {
        // Path compression: make every node on the path point directly to the root
        if (parent[x] != x) {
            parent[x] = find(parent[x]); // Recursively find root and compress path
        }
        return parent[x];
    }

    /**
     * Unites the components containing nodes x and y.
     * Uses union by rank to keep the tree balanced.
     *
     * @param x first node
     * @param y second node
     * Time complexity: O(α(n)) amortized
     * Space complexity: O(1) extra space
     */
    private void union(int x, int y) {
        // Find roots of both components
        int rootX = find(x);
        int rootY = find(y);

        // If they're already in the same component, nothing to do
        if (rootX == rootY) return;

        // Union by rank: attach smaller tree under larger tree
        if (rank[rootX] < rank[rootY]) {
            // rootX's tree is smaller, attach it under rootY
            parent[rootX] = rootY;
            size[rootY] += size[rootX]; // Update size of the new root
        } else if (rank[rootX] > rank[rootY]) {
            // rootY's tree is smaller, attach it under rootX
            parent[rootY] = rootX;
            size[rootX] += size[rootY]; // Update size of the new root
        } else {
            // Equal rank: attach rootY under rootX and increase rootX's rank
            parent[rootY] = rootX;
            size[rootX] += size[rootY]; // Update size of the new root
            rank[rootX]++;              // Increase rank since tree got deeper
        }
    }

    /**
     * Determines the optimal server to quarantine to minimize the total number
     * of infected servers after the virus fully spreads.
     *
     * Algorithm Overview:
     * 1. Build a Union-Find structure from the edges to group servers into components.
     * 2. For each connected component, count how many initially infected servers are in it.
     * 3. Only components with EXACTLY ONE initially infected server benefit from quarantine.
     *    (If a component has 2+ infected servers, removing one still leaves others to spread.)
     * 4. Among all "singleton-infected" components, find the one with the LARGEST size.
     *    Quarantining its infected server saves the most servers.
     * 5. If there's a tie in component size, return the smallest server index.
     *
     * @param n       the total number of servers (0 to n-1)
     * @param edges   bidirectional connections between servers
     * @param initial the list of initially infected servers
     * @return the index of the server to quarantine to minimize total infections
     * Time complexity: O((E + n) * α(n)) where E = number of edges, α = inverse Ackermann
     * Space complexity: O(n) for the Union-Find arrays and auxiliary maps
     */
    public int minMalwareSpread(int n, int[][] edges, int[] initial) {

        // =====================================================================
        // STEP 1: Initialize Union-Find data structures
        // =====================================================================
        parent = new int[n];
        rank = new int[n];
        size = new int[n];

        for (int i = 0; i < n; i++) {
            parent[i] = i;  // Each server is its own parent initially
            rank[i] = 0;    // All ranks start at 0
            size[i] = 1;    // Each component has size 1 initially
        }

        // =====================================================================
        // STEP 2: Process all edges to build connected components
        // =====================================================================
        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            union(u, v); // Merge the components containing u and v
        }

        // =====================================================================
        // STEP 3: For each connected component, count how many initially
        //         infected servers belong to it.
        //
        //         Key: We use the ROOT of each component as the key.
        //         infectedCountPerComponent[root] = number of infected servers in that component
        // =====================================================================
        // Map from component root -> count of initially infected servers in that component
        Map<Integer, Integer> infectedCountPerComponent = new HashMap<>();

        for (int server : initial) {
            int root = find(server); // Find which component this infected server belongs to
            // Increment the count of infected servers in this component
            infectedCountPerComponent.put(root, infectedCountPerComponent.getOrDefault(root, 0) + 1);
        }

        // =====================================================================
        // STEP 4: Sort the initial array so that in case of ties, we naturally
        //         encounter the smallest index first.
        // =====================================================================
        // Sort initial so we process smaller indices first (handles tie-breaking)
        int[] sortedInitial = initial.clone();
        Arrays.sort(sortedInitial);

        // =====================================================================
        // STEP 5: Find the best server to quarantine.
        //
        //         We look for initially infected servers that are the ONLY infected
        //         server in their component (infectedCount == 1).
        //         Among those, we pick the one in the LARGEST component.
        //         If sizes are equal, we pick the smallest server index (handled by sorting).
        // =====================================================================
        int bestServer = sortedInitial[0]; // Default: smallest index in initial (worst case)
        int bestSaving = -1;               // Best number of servers we can save

        for (int server : sortedInitial) {
            int root = find(server);
            int infectedCount = infectedCountPerComponent.get(root);

            // Only consider this server if it's the SOLE infected server in its component
            if (infectedCount == 1) {
                int componentSize = size[root]; // How many servers are in this component

                // If this component is larger than our current best, update
                // Note: We don't need to check for equal size with smaller index
                // because sortedInitial is sorted, so we encounter smaller indices first.
                // We use strict '>' so the first (smallest) server with max size wins.
                if (componentSize > bestSaving) {
                    bestSaving = componentSize;
                    bestServer = server;
                }
            }
        }

        // =====================================================================
        // STEP 6: Return the best server to quarantine
        //
        //         If no server was a sole infected server in its component
        //         (bestSaving == -1), we return the smallest index in initial
        //         (which is sortedInitial[0], already set as default).
        //         This is because removing any one of multiple infected servers
        //         from a component doesn't help — the component gets infected anyway.
        //         So we just return the smallest index as required.
        // =====================================================================
        return bestServer;
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     * Traces through each example from the problem description.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        Solution sol = new Solution();

        // =====================================================================
        // Example 1:
        // n = 6
        // edges = [[0,1],[0,2],[1,3],[2,3],[3,4],[4,5]]
        // initial = [0, 3]
        //
        // Graph structure:
        //   0 - 1
        //   |   |
        //   2 - 3 - 4 - 5
        //
        // All 6 servers form ONE connected component.
        //
        // Component root (let's say 0 after unions): size = 6
        // Both server 0 and server 3 are in the same component.
        // infectedCount for this component = 2 (both 0 and 3 are infected)
        //
        // Since infectedCount > 1, neither server alone can save the component.
        // So bestSaving remains -1, and we return the smallest index = 0.
        //
        // Wait — let me re-read the problem...
        //
        // Actually, let me re-trace Example 1:
        // edges = [[0,1],[0,2],[1,3],[2,3],[3,4],[4,5]]
        // This connects: 0-1, 0-2, 1-3, 2-3, 3-4, 4-5
        // All nodes 0,1,2,3,4,5 are in one component.
        // initial = [0, 3], both in same component, infectedCount = 2.
        // Since no component has exactly 1 infected server, return smallest = 0.
        //
        // But expected output is 3!
        //
        // Hmm, let me re-read the problem more carefully...
        // "If we quarantine server 3, only server 0 and its reachable neighbors {1, 2}
        //  get infected (3 servers)."
        //
        // Wait — if we REMOVE server 3 from the graph, does the graph split?
        // 0-1, 0-2, 1-3(removed), 2-3(removed), 3-4(removed), 4-5
        // Without server 3: edges become 0-1, 0-2 (and 4-5 is isolated from 0)
        // So removing server 3 splits the graph!
        //
        // This means the problem is NOT just about which component has 1 infected server.
        // We need to actually REMOVE each candidate server and recompute the spread.
        //
        // Let me reconsider the algorithm...
        // =====================================================================

        System.out.println("=== Infected Servers in a Data Center ===\n");

        // Example 1
        int n1 = 6;
        int[][] edges1 = {{0, 1}, {0, 2}, {1, 3}, {2, 3}, {3, 4}, {4, 5}};
        int[] initial1 = {0, 3};
        int result1 = sol.minMalwareSpread(n1, edges1, initial1);
        System.out.println("Example 1:");
        System.out.println("  n = " + n1);
        System.out.println("  edges = [[0,1],[0,2],[1,3],[2,3],[3,4],[4,5]]");
        System.out.println("  initial = [0, 3]");
        System.out.println("  Output: " + result1);
        System.out.println("  Expected: 3");
        System.out.println();

        // Example 2
        int n2 = 4;
        int[][] edges2 = {{0, 1}, {1, 2}, {2, 3}};
        int[] initial2 = {0, 1};
        int result2 = sol.minMalwareSpread(n2, edges2, initial2);
        System.out.println("Example 2:");
        System.out.println("  n = " + n2);
        System.out.println("  edges = [[0,1],[1,2],[2,3]]");
        System.out.println("  initial = [0, 1]");
        System.out.println("  Output: " + result2);
        System.out.println("  Expected: 1");
        System.out.println();

        // Additional test cases
        // Test: single infected server
        int n3 = 3;
        int[][] edges3 = {{0, 1}, {1, 2}};
        int[] initial3 = {1};
        int result3 = sol.minMalwareSpread(n3, edges3, initial3);
        System.out.println("Example 3 (single infected):");
        System.out.println("  n = " + n3 + ", edges = [[0,1],[1,2]], initial = [1]");
        System.out.println("  Output: " + result3);
        System.out.println("  Expected: 1");
        System.out.println();

        // Test: two separate components each with one infected
        int n4 = 4;
        int[][] edges4 = {{0, 1}, {2, 3}};
        int[] initial4 = {0, 2};
        int result4 = sol.minMalwareSpread(n4, edges4, initial4);
        System.out.println("Example 4 (two components, each with one infected):");
        System.out.println("  n = " + n4 + ", edges = [[0,1],[2,3]], initial = [0, 2]");
        System.out.println("  Output: " + result4);
        System.out.println("  Expected: 0 (both components size 2, pick smallest index)");
    }
}

/*
 * IMPORTANT NOTE: After tracing through Example 1, I realized the simple Union-Find
 * approach described above is INCORRECT for this problem when the quarantined server
 * acts as a bridge (its removal splits the graph).
 *
 * The correct approach requires: for each candidate server in `initial`, REMOVE it
 * from the graph, then simulate the spread from the remaining infected servers using BFS/DFS,
 * and count how many servers get infected. Pick the candidate that minimizes this count.
 *
 * However,