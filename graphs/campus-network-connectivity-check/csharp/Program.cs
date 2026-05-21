/*
 * Campus Network Connectivity Check
 * ==================================
 * A university campus has n buildings numbered from 0 to n-1.
 * Direct network cables connect certain pairs of buildings.
 * Network connectivity is bidirectional.
 *
 * Task: Determine how many isolated network clusters exist on campus.
 * A cluster is a group of one or more buildings all reachable from each other.
 * Buildings with no cables form their own cluster of size 1.
 *
 * Return the number of connected clusters in the campus network.
 *
 * Example 1: n=6, cables=[[0,1],[1,2],[3,4]] => Output: 3
 *   Cluster 1: {0,1,2}, Cluster 2: {3,4}, Cluster 3: {5}
 *
 * Example 2: n=4, cables=[[0,1],[1,2],[2,3]] => Output: 1
 *   All buildings connected: {0,1,2,3}
 */

// ─────────────────────────────────────────────────────────────────────────────
// Solution Class using Union-Find (Disjoint Set Union) data structure
// ─────────────────────────────────────────────────────────────────────────────

/// <summary>
/// Solves the Campus Network Connectivity Check problem using Union-Find.
/// Union-Find is ideal here because it efficiently tracks which elements
/// belong to the same connected component (cluster).
/// </summary>
public class Solution
{
    // ─────────────────────────────────────────────────────────────────────────
    // Time Complexity:  O(n + m * α(n))
    //   - n = number of buildings, m = number of cables
    //   - α(n) is the inverse Ackermann function, effectively O(1) in practice
    //   - We iterate over all n buildings once and process each cable once
    //
    // Space Complexity: O(n)
    //   - We store parent[] and rank[] arrays of size n
    // ─────────────────────────────────────────────────────────────────────────

    public int CountClusters(int n, int[][] cables)
    {
        // ─────────────────────────────────────────────────────────────────────
        // STEP 1: Initialize the Union-Find (Disjoint Set Union) structure
        // ─────────────────────────────────────────────────────────────────────
        // WHY: We need a way to group buildings into clusters efficiently.
        //      Union-Find lets us "union" two buildings into the same cluster
        //      and "find" which cluster a building belongs to.
        //
        // parent[i] = the representative (root) of building i's cluster.
        //             Initially, each building is its own root (parent[i] = i).
        //
        // rank[i]   = an approximation of the depth of the tree rooted at i.
        //             Used to keep the tree shallow (union by rank optimization).
        //             Initially all ranks are 0.
        //
        // clusterCount = starts at n because initially every building is its
        //                own isolated cluster. We decrement it each time we
        //                successfully merge two different clusters.

        int[] parent = new int[n];
        int[] rank = new int[n];
        int clusterCount = n; // Start with n clusters (one per building)

        // Initialize: each building is its own parent (self-loop = root)
        for (int i = 0; i < n; i++)
        {
            parent[i] = i;  // Building i is the root of its own cluster
            rank[i] = 0;    // Depth/rank starts at 0
        }

        // ─────────────────────────────────────────────────────────────────────
        // STEP 2: Process each cable to union connected buildings
        // ─────────────────────────────────────────────────────────────────────
        // WHY: Each cable [a, b] means buildings a and b are directly connected.
        //      We call Union(a, b) to merge their clusters.
        //      If they were already in the same cluster, nothing changes.
        //      If they were in different clusters, we merge them and reduce
        //      the cluster count by 1.

        foreach (int[] cable in cables)
        {
            int buildingA = cable[0]; // First building endpoint of the cable
            int buildingB = cable[1]; // Second building endpoint of the cable

            // Attempt to union the two buildings' clusters.
            // Union returns true if they were in DIFFERENT clusters (a merge happened).
            // Union returns false if they were ALREADY in the same cluster.
            bool merged = Union(buildingA, buildingB, parent, rank);

            if (merged)
            {
                // Two separate clusters have been joined into one.
                // So the total number of distinct clusters decreases by 1.
                clusterCount--;
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // STEP 3: Return the final cluster count
        // ─────────────────────────────────────────────────────────────────────
        // WHY: After processing all cables, clusterCount holds the exact number
        //      of connected components (clusters) in the campus network.

        return clusterCount;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER METHOD: Find (with Path Compression)
    // ─────────────────────────────────────────────────────────────────────────
    // PURPOSE: Find the root (representative) of the cluster that building x belongs to.
    //
    // PATH COMPRESSION OPTIMIZATION:
    //   After finding the root, we set parent[x] = root directly.
    //   This "flattens" the tree so future Find calls are faster (nearly O(1)).
    //
    // Example: If the chain is 3 -> 2 -> 1 -> 0 (root),
    //          after Find(3), parent[3] = 0, parent[2] = 0, parent[1] = 0.
    //          Next time Find(3) is called, it returns 0 immediately.

    private int Find(int x, int[] parent)
    {
        // Base case: if x is its own parent, x is the root of its cluster
        if (parent[x] != x)
        {
            // Recursively find the root, and apply path compression:
            // Set parent[x] directly to the root to flatten the tree.
            parent[x] = Find(parent[x], parent);
        }

        // Return the root representative of x's cluster
        return parent[x];
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER METHOD: Union (with Union by Rank)
    // ─────────────────────────────────────────────────────────────────────────
    // PURPOSE: Merge the clusters of buildings a and b.
    //          Returns true if they were in different clusters (merge happened).
    //          Returns false if they were already in the same cluster.
    //
    // UNION BY RANK OPTIMIZATION:
    //   We attach the smaller-rank tree under the larger-rank tree.
    //   This keeps the overall tree height small, making Find() faster.
    //
    // WHY RETURN bool?
    //   The caller (CountClusters) needs to know if a real merge happened
    //   so it can decrement the cluster count appropriately.

    private bool Union(int a, int b, int[] parent, int[] rank)
    {
        // Find the root representative of each building's cluster
        int rootA = Find(a, parent);
        int rootB = Find(b, parent);

        // If both buildings share the same root, they're already in the same cluster.
        // No merge needed — return false to indicate no change.
        if (rootA == rootB)
        {
            return false; // Already connected, no new merge
        }

        // ── Union by Rank: attach smaller tree under larger tree ──────────────
        // WHY: Keeping the tree balanced (shallow) ensures Find() stays fast.

        if (rank[rootA] < rank[rootB])
        {
            // rootA's tree is shorter → attach it under rootB
            parent[rootA] = rootB;
        }
        else if (rank[rootA] > rank[rootB])
        {
            // rootB's tree is shorter → attach it under rootA
            parent[rootB] = rootA;
        }
        else
        {
            // Both trees have equal rank → arbitrarily attach rootB under rootA
            // Then increment rootA's rank because the tree just got one level deeper
            parent[rootB] = rootA;
            rank[rootA]++;
        }

        // A successful merge happened — return true so caller can decrement count
        return true;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DEMO / TEST CODE
// ─────────────────────────────────────────────────────────────────────────────
// We create sample inputs matching the problem examples and verify correctness.

Console.WriteLine("=== Campus Network Connectivity Check ===");
Console.WriteLine();

Solution solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// n = 6, cables = [[0,1],[1,2],[3,4]]
// Expected clusters:
//   {0, 1, 2} — connected via cables 0-1 and 1-2
//   {3, 4}    — connected via cable 3-4
//   {5}       — isolated building, no cables
// Expected Output: 3

int n1 = 6;
int[][] cables1 = new int[][]
{
    new int[] { 0, 1 },
    new int[] { 1, 2 },
    new int[] { 3, 4 }
};

int result1 = solution.CountClusters(n1, cables1);
Console.WriteLine($"Example 1:");
Console.WriteLine($"  n = {n1}");
Console.WriteLine($"  cables = [[0,1],[1,2],[3,4]]");
Console.WriteLine($"  Expected Output: 3");
Console.WriteLine($"  Actual Output:   {result1}");
Console.WriteLine($"  ✓ Correct: {result1 == 3}");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// n = 4, cables = [[0,1],[1,2],[2,3]]
// Expected clusters:
//   {0, 1, 2, 3} — all connected in a chain
// Expected Output: 1

int n2 = 4;
int[][] cables2 = new int[][]
{
    new int[] { 0, 1 },
    new int[] { 1, 2 },
    new int[] { 2, 3 }
};

int result2 = solution.CountClusters(n2, cables2);
Console.WriteLine($"Example 2:");
Console.WriteLine($"  n = {n2}");
Console.WriteLine($"  cables = [[0,1],[1,2],[2,3]]");
Console.WriteLine($"  Expected Output: 1");
Console.WriteLine($"  Actual Output:   {result2}");
Console.WriteLine($"  ✓ Correct: {result2 == 1}");
Console.WriteLine();

// ── Additional Edge Case: No cables ──────────────────────────────────────────
// n = 5, cables = []
// Every building is isolated → 5 clusters
// Expected Output: 5

int n3 = 5;
int[][] cables3 = new int[][] { };

int result3 = solution.CountClusters(n3, cables3);
Console.WriteLine($"Edge Case - No Cables:");
Console.WriteLine($"  n = {n3}");
Console.WriteLine($"  cables = []");
Console.WriteLine($"  Expected Output: 5");
Console.WriteLine($"  Actual Output:   {result3}");
Console.WriteLine($"  ✓ Correct: {result3 == 5}");
Console.WriteLine();

// ── Additional Edge Case: Single building ────────────────────────────────────
// n = 1, cables = []
// One building, no cables → 1 cluster
// Expected Output: 1

int n4 = 1;
int[][] cables4 = new int[][] { };

int result4 = solution.CountClusters(n4, cables4);
Console.WriteLine($"Edge Case - Single Building:");
Console.WriteLine($"  n = {n4}");
Console.WriteLine($"  cables = []");
Console.WriteLine($"  Expected Output: 1");
Console.WriteLine($"  Actual Output:   {result4}");
Console.WriteLine($"  ✓ Correct: {result4 == 1}");
Console.WriteLine();

// ── Additional Edge Case: All buildings fully connected ───────────────────────
// n = 4, cables = [[0,1],[0,2],[0,3],[1,2],[1,3],[2,3]]
// All buildings connected to each other → 1 cluster
// Expected Output: 1

int n5 = 4;
int[][] cables5 = new int[][]
{
    new int[] { 0, 1 },
    new int[] { 0, 2 },
    new int[] { 0, 3 },
    new int[] { 1, 2 },
    new int[] { 1, 3 },
    new int[] { 2, 3 }
};

int result5 = solution.CountClusters(n5, cables5);
Console.WriteLine($"Edge Case - Fully Connected:");
Console.WriteLine($"  n = {n5}");
Console.WriteLine($"  cables = [[0,1],[0,2],[0,3],[1,2],[1,3],[2,3]]");
Console.WriteLine($"  Expected Output: 1");
Console.WriteLine($"  Actual Output:   {result5}");
Console.WriteLine($"  ✓ Correct: {result5 == 1}");
Console.WriteLine();

Console.WriteLine("=== All Tests Complete ===");