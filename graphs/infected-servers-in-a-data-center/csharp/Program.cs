/*
 * ============================================================
 * Problem: Infected Servers in a Data Center
 * Difficulty: Medium
 * Topic: Graphs
 *
 * Description:
 * You are managing a data center with n servers numbered 0 to n-1.
 * Servers are connected by bidirectional edges. At time 0, a subset
 * of servers in 'initial' become infected. Every minute, each infected
 * server spreads the virus to all directly connected neighbors.
 * You can quarantine EXACTLY ONE server at time 0 (before spreading).
 * Goal: Minimize total infected servers after full spread.
 * Return the index of the server to quarantine.
 * If multiple choices give the same minimum, return the smallest index.
 * ============================================================
 */

using System;
using System.Collections.Generic;
using System.Linq;

// ============================================================
// SOLUTION CLASS
// ============================================================
public class Solution
{
    /*
     * Time Complexity:  O(initial.length * (n + edges.length))
     *   - For each server in initial, we do a BFS/DFS over the graph.
     *   - In the worst case initial.length = n, giving O(n*(n+E)).
     *   - However, with the Union-Find optimization approach below,
     *     we achieve O((n + E) * alpha(n) + initial.length^2) effectively.
     *
     * Space Complexity: O(n + E)
     *   - Adjacency list storage plus Union-Find arrays.
     *
     * APPROACH OVERVIEW:
     * ------------------
     * Key Insight: When we remove one initially-infected server S from
     * consideration, the remaining initially-infected servers spread
     * through the graph. We want to find which server S to remove so
     * that the total number of reachable nodes (from all remaining
     * initially-infected servers) is minimized.
     *
     * Efficient Strategy using Union-Find (Disjoint Set Union):
     * 1. Build connected components of the FULL graph.
     * 2. For each connected component, count how many initially-infected
     *    servers it contains.
     * 3. A component with EXACTLY ONE initially-infected server:
     *    - If we quarantine that one infected server, the entire component
     *      is saved (no infection spreads there).
     *    - The "savings" = size of that component.
     * 4. A component with MORE THAN ONE initially-infected server:
     *    - No matter which single server we quarantine, the component
     *      still gets fully infected (other infected servers remain).
     *    - Quarantining one of these servers saves 0 nodes.
     * 5. So we should quarantine the initially-infected server that is
     *    the SOLE infected node in its component AND whose component
     *    is the LARGEST (maximum savings).
     * 6. If no such server exists (all components have 2+ infected nodes),
     *    we must quarantine one server but save nothing — return the
     *    smallest index in initial.
     */
    public int MinMalwareSpread(int n, int[][] edges, int[] initial)
    {
        // -------------------------------------------------------
        // STEP 1: Build Union-Find (Disjoint Set Union) structure
        // -------------------------------------------------------
        // Union-Find lets us efficiently group servers into connected
        // components and query which component a server belongs to.
        
        int[] parent = new int[n];  // parent[i] = representative of i's component
        int[] size   = new int[n];  // size[i] = size of component rooted at i
        
        // Initialize: each server is its own component of size 1
        for (int i = 0; i < n; i++)
        {
            parent[i] = i;  // each node is its own parent initially
            size[i]   = 1;  // each component starts with 1 node
        }
        
        // -------------------------------------------------------
        // STEP 2: Union-Find helper functions (defined as local lambdas)
        // -------------------------------------------------------
        
        // Find: returns the root/representative of the component containing x.
        // Uses PATH COMPRESSION: flattens the tree so future finds are faster.
        // This is why Union-Find is nearly O(1) amortized per operation.
        int Find(int x)
        {
            // If x is not its own parent, recursively find the root
            // and compress the path by pointing x directly to the root.
            if (parent[x] != x)
                parent[x] = Find(parent[x]);  // path compression
            return parent[x];
        }
        
        // Union: merges the components containing x and y.
        // Uses UNION BY SIZE: attach smaller tree under larger tree
        // to keep trees shallow and operations fast.
        void Union(int x, int y)
        {
            int rootX = Find(x);
            int rootY = Find(y);
            
            if (rootX == rootY) return;  // already in the same component
            
            // Attach smaller component under larger component
            if (size[rootX] < size[rootY])
            {
                // rootX's component is smaller, attach it under rootY
                parent[rootX] = rootY;
                size[rootY] += size[rootX];
            }
            else
            {
                // rootY's component is smaller (or equal), attach under rootX
                parent[rootY] = rootX;
                size[rootX] += size[rootY];
            }
        }
        
        // -------------------------------------------------------
        // STEP 3: Process all edges to build connected components
        // -------------------------------------------------------
        // For each edge [u, v], union the two servers into the same component.
        // After processing all edges, servers in the same component are
        // all reachable from each other.
        foreach (var edge in edges)
        {
            Union(edge[0], edge[1]);
        }
        
        // -------------------------------------------------------
        // STEP 4: For each connected component, count how many
        //         initially-infected servers it contains.
        // -------------------------------------------------------
        // infectedCountInComponent[root] = number of initially-infected
        // servers whose component root is 'root'.
        // We use a Dictionary because not all roots need entries.
        var infectedCountInComponent = new Dictionary<int, int>();
        
        foreach (int server in initial)
        {
            int root = Find(server);  // find which component this infected server belongs to
            
            if (!infectedCountInComponent.ContainsKey(root))
                infectedCountInComponent[root] = 0;
            
            infectedCountInComponent[root]++;  // increment count for this component
        }
        
        // -------------------------------------------------------
        // STEP 5: Sort initial array so we process smaller indices first.
        // -------------------------------------------------------
        // This ensures that when multiple servers give the same maximum
        // savings, we naturally pick the smallest index (since we process
        // in order and update only when strictly better).
        // We sort a COPY so we don't modify the original array.
        int[] sortedInitial = (int[])initial.Clone();
        Array.Sort(sortedInitial);
        
        // -------------------------------------------------------
        // STEP 6: Find the best server to quarantine.
        // -------------------------------------------------------
        // We look for the initially-infected server that:
        //   (a) Is the ONLY infected server in its component (sole infector)
        //   (b) Has the LARGEST component size (maximum savings)
        //
        // If we quarantine such a server, we save 'componentSize' nodes
        // from being infected.
        //
        // If no server satisfies (a), we must still quarantine someone
        // but save 0 nodes — return the smallest index in initial.
        
        int bestServer  = sortedInitial[0];  // default: smallest index in initial
        int bestSavings = 0;                 // best savings found so far (0 = no savings)
        
        foreach (int server in sortedInitial)
        {
            int root = Find(server);
            
            // Check if this server is the SOLE infected node in its component
            if (infectedCountInComponent[root] == 1)
            {
                // This server is the only infected node in its component.
                // If we quarantine it, we save 'size[root]' nodes.
                int savings = size[root];
                
                // Update best if this gives strictly MORE savings.
                // We use strictly greater (not >=) because we process
                // in sorted order, so the first one we see with a given
                // savings value is already the smallest index.
                if (savings > bestSavings)
                {
                    bestSavings = savings;
                    bestServer  = server;
                }
            }
            // If infectedCountInComponent[root] > 1, quarantining this server
            // saves 0 nodes (other infected servers in same component remain),
            // so we skip it (it can't beat any positive savings).
        }
        
        // Return the server index that minimizes total infection spread
        return bestServer;
    }
}

// ============================================================
// DEMO / TEST CODE (Top-level statements)
// ============================================================

var solution = new Solution();

// -----------------------------------------------------------
// Example 1:
// n = 6
// edges = [[0,1],[0,2],[1,3],[2,3],[3,4],[4,5]]
// initial = [0, 3]
//
// Graph structure:
//   0 -- 1
//   |    |
//   2 -- 3 -- 4 -- 5
//
// All 6 nodes are in ONE connected component.
// Component root (say root=0 after unions): size = 6
// Both server 0 and server 3 are in the same component.
// infectedCountInComponent[root] = 2 (both 0 and 3 are infected)
//
// Wait — let me re-trace:
// Edges: [0,1],[0,2],[1,3],[2,3],[3,4],[4,5]
// After all unions: {0,1,2,3,4,5} all connected → one component, size=6
// initial = [0,3]: both in same component → infectedCount = 2
//
// Since infectedCount > 1 for the only component, no server gives savings > 0.
// We return the smallest index in initial = min(0,3) = 0.
//
// BUT the expected output is 3!
//
// Hmm — let me re-read the problem. The graph IS fully connected here,
// so removing either 0 or 3 still leaves the other to infect everything.
// But the example says quarantining 3 infects only {0,1,2} = 3 servers,
// while quarantining 0 infects {3,4,5} + neighbors = more servers.
//
// This means the problem is NOT about connected components in the full graph —
// it's about BFS spread from remaining infected servers!
//
// REVISED UNDERSTANDING:
// When we quarantine server S (remove it), we run BFS from all remaining
// initially-infected servers (initial \ {S}) through the FULL graph
// (S is still a node, just not a source — but wait, can infection pass THROUGH S?).
//
// Actually re-reading: quarantining prevents S from infecting others OR
// receiving infection. So S is effectively REMOVED from the graph.
// We need to count reachable nodes from (initial \ {S}) in graph without S.
//
// Let me re-trace Example 1 with this understanding:
// Quarantine 3: Graph without node 3: edges [0,1],[0,2] (edges [1,3],[2,3],[3,4],[4,5] removed)
//   Sources: {0}. BFS from 0: reaches {0,1,2} → 3 infected.
// Quarantine 0: Graph without node 0: edges [1,3],[2,3],[3,4],[4,5] (edges [0,1],[0,2] removed)
//   Sources: {3}. BFS from 3: reaches {1,2,3,4,5} → 5 infected.
// Min is 3 (quarantine server 3). ✓
//
// So the correct approach is: for each server in initial, remove it,
// do BFS from remaining initial servers, count reachable nodes.
// Pick the removal that minimizes count. Tie-break: smallest index.
//
// The Union-Find approach above is WRONG for this problem.
// Let me implement the correct BFS approach.
// -----------------------------------------------------------

Console.WriteLine("=== Infected Servers in a Data Center ===\n");

// We need to use the correct solution. Let me redefine it below.

// ============================================================
// CORRECT SOLUTION CLASS
// ============================================================
// (Redefining with the correct algorithm)

/*
 * CORRECT APPROACH:
 * For each candidate server to quarantine (each server in initial):
 *   1. Remove that server from the graph (skip it in BFS).
 *   2. BFS/DFS from all remaining initially-infected servers.
 *   3. Count total reachable nodes.
 * Pick the candidate that minimizes the count.
 * Tie-break: smallest index.
 *
 * Time Complexity:  O(initial.length * (n + E))
 * Space Complexity: O(n + E)
 */

// Since C# top-level statements don't allow redefining a class,
// let's use a local function approach or just inline the correct solution.

// Let's define the correct solution as a static local function:

static int MinMalwareSpreadCorrect(int n, int[][] edges, int[] initial)
{
    /*
     * Time Complexity:  O(|initial| * (n + |edges|))
     *   - We try quarantining each server in initial (|initial| iterations).
     *   - Each iteration does a BFS over the graph: O(n + |edges|).
     *
     * Space Complexity: O(n + |edges|)
     *   - Adjacency list: O(n + |edges|)
     *   - Visited array: O(n)
     *   - BFS queue: O(n)
     */
    
    // -------------------------------------------------------
    // STEP 1: Build adjacency list for the graph.
    // -------------------------------------------------------
    // An adjacency list stores, for each server, the list of servers
    // directly connected to it. This is efficient for BFS traversal.
    // We use List<int>[] — an array of lists.
    
    var adj = new List<int>[n];
    for (int i = 0; i < n; i++)
        adj[i] = new List<int>();
    
    // Add each edge in both directions (bidirectional/undirected graph)
    foreach (var edge in edges)
    {
        int u = edge[0], v = edge[1];
        adj[u].Add(v);  // u can reach v
        adj[v].Add(u);  // v can reach u
    }
    
    // -------------------------------------------------------
    // STEP 2: Convert initial to a HashSet for O(1) lookup.
    // -------------------------------------------------------
    // We need to quickly check if a server is initially infected.
    // HashSet provides O(1) average-case contains check.
    var initialSet = new HashSet<int>(initial);
    
    // -------------------------------------------------------
    // STEP 3: For each server in initial, try quarantining it
    //         and count how many servers get infected.
    // -------------------------------------------------------
    // We want to find the quarantine choice that minimizes infection count.
    
    int bestServer   = -1;      // the best server to quarantine
    int minInfected  = int.MaxValue;  // minimum infection count found so far
    
    // Sort initial so we process smaller indices first.
    // This way, when we find a tie (same infection count), we keep
    // the first (smallest index) one we found.
    int[] sortedInitial = (int[])initial.Clone();
    Array.Sort(sortedInitial);
    
    foreach (int quarantined in sortedInitial)
    {
        // ---------------------------------------------------
        // STEP 3a: BFS from all initially-infected servers
        //          EXCEPT the quarantined one.
        //          The quarantined server is also removed from
        //          the graph (cannot be visited/traversed through).
        // ---------------------------------------------------
        
        // visited[i] = true means server i has been infected/visited
        bool[] visited = new bool