/*
 * Title: Count Reachable Nodes from Each Capital
 * 
 * Problem Description:
 * You are given a country represented as an undirected tree with n cities numbered
 * from 0 to n-1. City 0 is the national capital. Some cities are marked as regional
 * capitals in the array 'capitals' (capitals[i] = 1 means city i is a regional capital).
 *
 * For each regional capital, determine how many cities (including itself) are reachable
 * from it WITHOUT passing through any other regional capital or the national capital (city 0).
 *
 * Return an array 'result' where result[i] is the count of reachable cities for the
 * i-th regional capital (in the order they appear in the capitals array).
 *
 * Example 1:
 *   n=6, edges=[[0,1],[1,2],[1,3],[3,4],[3,5]], capitals=[0,1,0,1,0,0]
 *   Output: [2, 3]
 *   - City 1 (regional capital): can reach cities 1, 2 (blocked by city 0 and city 3) → 2
 *   - City 3 (regional capital): can reach cities 3, 4, 5 (blocked by city 0 and city 1) → 3
 *
 * Example 2:
 *   n=4, edges=[[0,1],[1,2],[2,3]], capitals=[0,1,0,0]
 *   Output: [3]
 *   - City 1 (regional capital): can reach cities 1, 2, 3 (blocked only by city 0) → 3
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution Class
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    // Time Complexity:  O(R * N)  where R = number of regional capitals, N = number of cities
    //                  For each regional capital we do a BFS/DFS that visits at most N nodes.
    // Space Complexity: O(N)  for the adjacency list representation and the BFS/DFS visited set.
    //
    // Approach:
    //   Build an adjacency list for the undirected tree.
    //   For every city that is a regional capital, run a BFS starting from that city.
    //   During BFS, we do NOT cross into:
    //     - City 0 (the national capital)
    //     - Any other regional capital (capitals[neighbor] == 1 AND neighbor != startCity)
    //   Count every city we successfully visit (including the starting regional capital itself).

    public int[] CountReachable(int n, int[][] edges, int[] capitals)
    {
        // ── Step 1: Build the adjacency list ──────────────────────────────────
        // An adjacency list stores, for each city, the list of cities directly
        // connected to it by a road.  We use List<int>[] (an array of lists) for
        // O(1) lookup by city index.
        // Why adjacency list instead of a matrix?  With up to 1000 nodes the matrix
        // would be 1000×1000 = 1,000,000 entries; the list uses only O(N + E) space.
        List<int>[] adj = new List<int>[n];
        for (int i = 0; i < n; i++)
            adj[i] = new List<int>();

        // Fill the adjacency list.  Each edge [u, v] is bidirectional, so we add
        // v to u's list AND u to v's list.
        foreach (int[] edge in edges)
        {
            int u = edge[0];
            int v = edge[1];
            adj[u].Add(v);
            adj[v].Add(u);
        }

        // ── Step 2: Collect all regional capitals in order ────────────────────
        // We need to return results in the order the regional capitals appear in
        // the capitals array, so we iterate once and record their city indices.
        List<int> regionalCapitals = new List<int>();
        for (int i = 0; i < n; i++)
        {
            if (capitals[i] == 1)
                regionalCapitals.Add(i);
        }

        // ── Step 3: For each regional capital, run a BFS ──────────────────────
        // The result list will hold one count per regional capital.
        List<int> result = new List<int>();

        foreach (int startCity in regionalCapitals)
        {
            // BFS uses a queue (FIFO).  We start by enqueuing the regional capital itself.
            Queue<int> queue = new Queue<int>();
            queue.Enqueue(startCity);

            // 'visited' tracks which cities we have already added to the queue so
            // we never process the same city twice (the graph is a tree so there are
            // no cycles, but being explicit prevents bugs if the input ever has
            // parallel edges).
            HashSet<int> visited = new HashSet<int>();
            visited.Add(startCity);

            // 'count' will accumulate the number of reachable cities for this capital.
            int count = 0;

            // ── BFS loop ──────────────────────────────────────────────────────
            while (queue.Count > 0)
            {
                // Dequeue the next city to process.
                int current = queue.Dequeue();

                // Count this city as reachable.
                count++;

                // Explore all neighbors of 'current'.
                foreach (int neighbor in adj[current])
                {
                    // Skip if already visited (avoids infinite loops / double-counting).
                    if (visited.Contains(neighbor))
                        continue;

                    // ── Blocking rule 1: national capital ─────────────────────
                    // City 0 is the national capital.  We must NOT travel through it.
                    if (neighbor == 0)
                        continue;

                    // ── Blocking rule 2: another regional capital ─────────────
                    // If the neighbor is itself a regional capital (and it is NOT
                    // the city we started from — which is already handled by the
                    // visited check), we must NOT travel through it.
                    // Note: startCity is already in 'visited', so this check only
                    // fires for OTHER regional capitals.
                    if (capitals[neighbor] == 1)
                        continue;

                    // The neighbor is a normal city we haven't visited yet.
                    // Mark it visited and add it to the queue for future processing.
                    visited.Add(neighbor);
                    queue.Enqueue(neighbor);
                }
            }

            // Store the count for this regional capital.
            result.Add(count);
        }

        // Convert the list to an array and return.
        return result.ToArray();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Test Code  (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

Solution sol = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// Tree structure:
//        0
//        |
//        1  ← regional capital
//       / \
//      2   3  ← regional capital
//         / \
//        4   5
//
// Expected output: [2, 3]
//   City 1 → reaches {1, 2}  (blocked by city 0 and city 3)
//   City 3 → reaches {3, 4, 5} (blocked by city 0 and city 1)

int n1 = 6;
int[][] edges1 = new int[][] {
    new int[] {0, 1},
    new int[] {1, 2},
    new int[] {1, 3},
    new int[] {3, 4},
    new int[] {3, 5}
};
int[] capitals1 = new int[] { 0, 1, 0, 1, 0, 0 };

int[] result1 = sol.CountReachable(n1, edges1, capitals1);
Console.Write("Example 1 Output: [");
Console.Write(string.Join(", ", result1));
Console.WriteLine("]");
Console.WriteLine("Expected:         [2, 3]");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// Tree structure:
//   0 — 1 — 2 — 3
//       ↑
//  regional capital
//
// Expected output: [3]
//   City 1 → reaches {1, 2, 3} (blocked only by city 0)

int n2 = 4;
int[][] edges2 = new int[][] {
    new int[] {0, 1},
    new int[] {1, 2},
    new int[] {2, 3}
};
int[] capitals2 = new int[] { 0, 1, 0, 0 };

int[] result2 = sol.CountReachable(n2, edges2, capitals2);
Console.Write("Example 2 Output: [");
Console.Write(string.Join(", ", result2));
Console.WriteLine("]");
Console.WriteLine("Expected:         [3]");
Console.WriteLine();

// ── Extra edge-case: no regional capitals ────────────────────────────────────
// If no city is a regional capital, the result should be an empty array.
int n3 = 3;
int[][] edges3 = new int[][] {
    new int[] {0, 1},
    new int[] {1, 2}
};
int[] capitals3 = new int[] { 0, 0, 0 };

int[] result3 = sol.CountReachable(n3, edges3, capitals3);
Console.Write("Edge-case (no capitals) Output: [");
Console.Write(string.Join(", ", result3));
Console.WriteLine("]");
Console.WriteLine("Expected:                       []");