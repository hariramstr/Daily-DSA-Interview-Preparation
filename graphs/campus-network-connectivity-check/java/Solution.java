/*
 * Campus Network Connectivity Check
 * Difficulty: Easy
 * Topic: Graphs
 *
 * Problem Description:
 * A university campus has `n` buildings numbered from `0` to `n - 1`. The IT department
 * has laid down direct network cables between certain pairs of buildings. You are given
 * an integer `n` and a list of connections `cables`, where `cables[i] = [a, b]` means
 * there is a direct network cable between building `a` and building `b`. Network
 * connectivity is bidirectional — if building `a` can reach building `b`, then building
 * `b` can also reach building `a`.
 *
 * Your task is to determine how many isolated network clusters exist on campus. A cluster
 * is a group of one or more buildings that are all reachable from each other (directly or
 * indirectly). Buildings with no cables connected to them form their own cluster of size 1.
 *
 * Return the number of connected clusters in the campus network.
 *
 * Constraints:
 * - 1 <= n <= 1000
 * - 0 <= cables.length <= 5000
 * - cables[i].length == 2
 * - 0 <= cables[i][0], cables[i][1] < n
 * - cables[i][0] != cables[i][1]
 * - There are no duplicate cables.
 *
 * Example 1:
 * Input: n = 6, cables = [[0,1],[1,2],[3,4]]
 * Output: 3
 * Explanation: Cluster 1: {0,1,2}, Cluster 2: {3,4}, Cluster 3: {5}. Total = 3 clusters.
 *
 * Example 2:
 * Input: n = 4, cables = [[0,1],[1,2],[2,3]]
 * Output: 1
 * Explanation: All buildings are connected in one cluster: {0,1,2,3}.
 */

import java.util.*;

/**
 * Solution class for the Campus Network Connectivity Check problem.
 *
 * <p>This solution uses the Union-Find (Disjoint Set Union) data structure to
 * efficiently group buildings into connected clusters and count the total number
 * of distinct clusters.</p>
 *
 * <p>Union-Find is ideal here because:
 * <ul>
 *   <li>It efficiently merges two groups (union operation)</li>
 *   <li>It quickly checks if two elements belong to the same group (find operation)</li>
 *   <li>It naturally tracks the number of distinct groups</li>
 * </ul>
 * </p>
 */
public class Solution {

    /**
     * parent array for Union-Find.
     * parent[i] stores the parent of node i.
     * If parent[i] == i, then i is a root (representative) of its cluster.
     */
    private int[] parent;

    /**
     * rank array for Union-Find with union by rank optimization.
     * rank[i] is an upper bound on the height of the subtree rooted at i.
     * This keeps the tree flat, making find() faster.
     */
    private int[] rank;

    /**
     * Tracks the current number of distinct connected clusters.
     * Starts at n (each building is its own cluster) and decreases
     * each time two different clusters are merged.
     */
    private int clusterCount;

    /**
     * Initializes the Union-Find data structure for n buildings.
     * Each building starts as its own cluster (self-loop in parent array).
     *
     * @param n the number of buildings on campus
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    private void initUnionFind(int n) {
        // Allocate arrays of size n
        parent = new int[n];
        rank = new int[n];

        // Initially, every building is its own cluster
        // parent[i] = i means building i is the root of its own cluster
        for (int i = 0; i < n; i++) {
            parent[i] = i;  // each node is its own parent (root)
            rank[i] = 0;    // all trees start with height 0
        }

        // At the start, there are n clusters (one per building)
        clusterCount = n;
    }

    /**
     * Finds the root (representative) of the cluster containing building x.
     * Uses path compression: after finding the root, all nodes along the path
     * are directly connected to the root, flattening the tree for future calls.
     *
     * @param x the building whose cluster root we want to find
     * @return the root representative of x's cluster
     * Time complexity: O(α(n)) amortized, where α is the inverse Ackermann function (nearly O(1))
     * Space complexity: O(α(n)) for the recursion stack (nearly O(1))
     */
    private int find(int x) {
        // Base case: if x is its own parent, x is the root
        if (parent[x] != x) {
            // Path compression: recursively find the root,
            // then set parent[x] directly to the root
            // This flattens the tree so future find() calls are faster
            parent[x] = find(parent[x]);
        }
        // Return the root of x's cluster
        return parent[x];
    }

    /**
     * Merges the clusters containing buildings x and y.
     * Uses union by rank: the root with lower rank is attached under the root
     * with higher rank, keeping the tree as flat as possible.
     *
     * @param x one building
     * @param y another building
     * Time complexity: O(α(n)) amortized
     * Space complexity: O(1)
     */
    private void union(int x, int y) {
        // Step 1: Find the roots of both buildings' clusters
        int rootX = find(x);
        int rootY = find(y);

        // Step 2: If both buildings are already in the same cluster, do nothing
        if (rootX == rootY) {
            return; // already connected, no merge needed
        }

        // Step 3: Merge the two clusters using union by rank
        // Attach the smaller-rank tree under the larger-rank tree
        if (rank[rootX] < rank[rootY]) {
            // rootX's tree is shorter, so attach it under rootY
            parent[rootX] = rootY;
        } else if (rank[rootX] > rank[rootY]) {
            // rootY's tree is shorter, so attach it under rootX
            parent[rootY] = rootX;
        } else {
            // Both trees have the same rank; arbitrarily attach rootY under rootX
            // and increment rootX's rank since the tree grew taller
            parent[rootY] = rootX;
            rank[rootX]++;
        }

        // Step 4: Two clusters have been merged into one, so decrement the count
        clusterCount--;
    }

    /**
     * Counts the number of connected network clusters in the campus.
     *
     * <p>Algorithm overview:
     * <ol>
     *   <li>Initialize Union-Find with n buildings (n clusters initially)</li>
     *   <li>For each cable [a, b], union buildings a and b</li>
     *   <li>Each successful union reduces the cluster count by 1</li>
     *   <li>Return the final cluster count</li>
     * </ol>
     * </p>
     *
     * @param n      the number of buildings (nodes), numbered 0 to n-1
     * @param cables a 2D array where cables[i] = [a, b] means a direct cable between a and b
     * @return the number of isolated connected clusters in the campus network
     * Time complexity: O(n + m * α(n)), where m = number of cables and α is inverse Ackermann
     * Space complexity: O(n) for the parent and rank arrays
     */
    public int countClusters(int n, int[][] cables) {
        // Step 1: Initialize Union-Find for n buildings
        // After this, clusterCount = n (each building is its own cluster)
        initUnionFind(n);

        // Step 2: Process each cable and merge the clusters of the two endpoints
        for (int[] cable : cables) {
            int buildingA = cable[0]; // one end of the cable
            int buildingB = cable[1]; // other end of the cable

            // Union the two buildings:
            // If they are in different clusters, this merges them and decrements clusterCount
            // If they are already in the same cluster, nothing changes
            union(buildingA, buildingB);
        }

        // Step 3: Return the final number of distinct clusters
        // This is the number of buildings that are still roots of their own cluster
        return clusterCount;
    }

    /**
     * Main method to demonstrate the solution with sample inputs from the problem.
     *
     * <p>Traces through both examples to verify correctness:
     * <ul>
     *   <li>Example 1: n=6, cables=[[0,1],[1,2],[3,4]] → Expected output: 3</li>
     *   <li>Example 2: n=4, cables=[[0,1],[1,2],[2,3]] → Expected output: 1</li>
     * </ul>
     * </p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1: n = 6, cables = [[0,1],[1,2],[3,4]]
        // Expected Output: 3
        //
        // Trace:
        // Initial state: parent = [0,1,2,3,4,5], clusterCount = 6
        //
        // Process cable [0,1]:
        //   find(0)=0, find(1)=1 → different roots → union(0,1)
        //   parent[1]=0, rank[0]=1, clusterCount=5
        //   parent = [0,0,2,3,4,5]
        //
        // Process cable [1,2]:
        //   find(1): parent[1]=0, parent[0]=0 → root=0
        //   find(2): parent[2]=2 → root=2
        //   different roots → union(0,2)
        //   rank[0]=1 > rank[2]=0 → parent[2]=0, clusterCount=4
        //   parent = [0,0,0,3,4,5]
        //
        // Process cable [3,4]:
        //   find(3)=3, find(4)=4 → different roots → union(3,4)
        //   parent[4]=3, rank[3]=1, clusterCount=3
        //   parent = [0,0,0,3,3,5]
        //
        // Final clusterCount = 3
        // Clusters: {0,1,2} (root=0), {3,4} (root=3), {5} (root=5)
        // -----------------------------------------------------------------------
        int n1 = 6;
        int[][] cables1 = {{0, 1}, {1, 2}, {3, 4}};
        int result1 = solution.countClusters(n1, cables1);
        System.out.println("Example 1:");
        System.out.println("  Input: n = " + n1 + ", cables = [[0,1],[1,2],[3,4]]");
        System.out.println("  Output: " + result1);
        System.out.println("  Expected: 3");
        System.out.println("  Correct: " + (result1 == 3));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2: n = 4, cables = [[0,1],[1,2],[2,3]]
        // Expected Output: 1
        //
        // Trace:
        // Initial state: parent = [0,1,2,3], clusterCount = 4
        //
        // Process cable [0,1]:
        //   find(0)=0, find(1)=1 → union(0,1)
        //   parent[1]=0, rank[0]=1, clusterCount=3
        //   parent = [0,0,2,3]
        //
        // Process cable [1,2]:
        //   find(1): parent[1]=0 → root=0
        //   find(2)=2 → union(0,2)
        //   rank[0]=1 > rank[2]=0 → parent[2]=0, clusterCount=2
        //   parent = [0,0,0,3]
        //
        // Process cable [2,3]:
        //   find(2): parent[2]=0 → root=0
        //   find(3)=3 → union(0,3)
        //   rank[0]=1 > rank[3]=0 → parent[3]=0, clusterCount=1
        //   parent = [0,0,0,0]
        //
        // Final clusterCount = 1
        // Cluster: {0,1,2,3} (root=0)
        // -----------------------------------------------------------------------
        int n2 = 4;
        int[][] cables2 = {{0, 1}, {1, 2}, {2, 3}};
        int result2 = solution.countClusters(n2, cables2);
        System.out.println("Example 2:");
        System.out.println("  Input: n = " + n2 + ", cables = [[0,1],[1,2],[2,3]]");
        System.out.println("  Output: " + result2);
        System.out.println("  Expected: 1");
        System.out.println("  Correct: " + (result2 == 1));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Test: n = 1, cables = [] (single building, no cables)
        // Expected Output: 1
        // -----------------------------------------------------------------------
        int n3 = 1;
        int[][] cables3 = {};
        int result3 = solution.countClusters(n3, cables3);
        System.out.println("Additional Test (single building, no cables):");
        System.out.println("  Input: n = " + n3 + ", cables = []");
        System.out.println("  Output: " + result3);
        System.out.println("  Expected: 1");
        System.out.println("  Correct: " + (result3 == 1));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Test: n = 5, cables = [] (5 isolated buildings)
        // Expected Output: 5
        // -----------------------------------------------------------------------
        int n4 = 5;
        int[][] cables4 = {};
        int result4 = solution.countClusters(n4, cables4);
        System.out.println("Additional Test (5 isolated buildings, no cables):");
        System.out.println("  Input: n = " + n4 + ", cables = []");
        System.out.println("  Output: " + result4);
        System.out.println("  Expected: 5");
        System.out.println("  Correct: " + (result4 == 5));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Test: n = 3, cables = [[0,1],[1,2],[0,2]] (triangle, all connected)
        // Expected Output: 1
        // -----------------------------------------------------------------------
        int n5 = 3;
        int[][] cables5 = {{0, 1}, {1, 2}, {0, 2}};
        int result5 = solution.countClusters(n5, cables5);
        System.out.println("Additional Test (triangle graph):");
        System.out.println("  Input: n = " + n5 + ", cables = [[0,1],[1,2],[0,2]]");
        System.out.println("  Output: " + result5);
        System.out.println("  Expected: 1");
        System.out.println("  Correct: " + (result5 == 1));
    }
}