/*
Title: Find the Nearest Shared Backup Folder

Problem Description:
A company stores project backups in a rooted tree of folders. Each folder has a unique integer ID from 0 to n - 1, and folder 0 is always the root. You are given the tree as a parent array where parent[i] is the parent of folder i, and parent[0] = -1.

For several restore operations, two backup snapshots are selected from folders a and b. The restore system needs to locate the nearest folder that is an ancestor of both a and b. This folder is the deepest folder that lies on both paths from a and b to the root.

Your task is to answer all queries and return the shared backup folder for each pair.

A folder is considered an ancestor of itself.

Constraints:
- 1 <= n <= 2 * 10^5
- parent.length == n
- parent[0] == -1
- For every i > 0, 0 <= parent[i] < n
- The input parent array always forms a valid tree rooted at 0
- 1 <= queries.length <= 2 * 10^5
- queries[i].length == 2
- 0 <= a, b < n

Examples:
1)
parent = [-1,0,0,1,1,2,2]
queries = [[3,4],[3,5],[2,6],[0,6]]
Output = [1,0,2,0]

2)
parent = [-1,0,1,2,3,1,5]
queries = [[4,6],[2,4],[6,6],[3,5]]
Output = [1,2,6,1]

Core idea:
This is the classic Lowest Common Ancestor (LCA) problem on a rooted tree.
Because there can be up to 200,000 queries, we must preprocess the tree so each query can be answered quickly.
A standard and efficient approach is Binary Lifting:
- Precompute depth of every node
- Precompute the 2^j-th ancestor of every node
- For each query:
  1. Move the deeper node upward until both nodes are at the same depth
  2. Move both upward together from large jumps to small jumps
  3. The parent right above where they diverge is the LCA
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building children list: O(n)
    - BFS/DFS to compute depths and immediate parents: O(n)
    - Building binary lifting table: O(n log n)
    - Answering each query: O(log n)
    - Total for q queries: O((n + q) log n)

    Space Complexity:
    - Children adjacency list: O(n)
    - Depth array: O(n)
    - Binary lifting table: O(n log n)
    - Result array: O(q)
    */
    public int[] FindNearestSharedBackupFolder(int[] parent, int[][] queries)
    {
        int n = parent.Length;

        // We need enough levels in the binary lifting table so that 2^maxLog is at least n.
        // Example:
        // If n = 7, then levels for jumps 1,2,4 are enough.
        // If n = 200000, around 18 levels are enough because 2^18 = 262144.
        int maxLog = 1;
        while ((1 << maxLog) <= n)
        {
            maxLog++;
        }

        // Build a children adjacency list from the parent array.
        // Why?
        // The input gives us parent -> child relationships indirectly.
        // To compute depth for every node starting from root, it is convenient to know
        // all children of a node so we can traverse downward.
        List<int>[] children = new List<int>[n];
        for (int i = 0; i < n; i++)
        {
            children[i] = new List<int>();
        }

        for (int node = 1; node < n; node++)
        {
            children[parent[node]].Add(node);
        }

        // up[node, j] will store the 2^j-th ancestor of "node".
        // For example:
        // up[node, 0] = direct parent
        // up[node, 1] = grandparent (2 steps up)
        // up[node, 2] = 4 steps up
        // and so on.
        int[,] up = new int[n, maxLog];

        // depth[node] = distance from root to node in number of edges.
        int[] depth = new int[n];

        // Initialize the root.
        // Root has no parent, so for convenience we point its ancestors to itself.
        // This avoids special-case checks during lifting.
        for (int j = 0; j < maxLog; j++)
        {
            up[0, j] = 0;
        }
        depth[0] = 0;

        // We will do an iterative BFS from the root to fill:
        // - depth of each node
        // - up[node, 0] = direct parent
        //
        // Why iterative instead of recursive DFS?
        // Because n can be as large as 200,000, and deep recursion could cause stack overflow.
        Queue<int> queue = new Queue<int>();
        queue.Enqueue(0);

        while (queue.Count > 0)
        {
            int current = queue.Dequeue();

            foreach (int child in children[current])
            {
                depth[child] = depth[current] + 1;
                up[child, 0] = current;
                queue.Enqueue(child);
            }
        }

        // Build the rest of the binary lifting table.
        // Formula:
        // up[node, j] = up[ up[node, j - 1], j - 1 ]
        //
        // Meaning:
        // The 2^j-th ancestor is found by first jumping 2^(j-1) steps up,
        // then another 2^(j-1) steps up from there.
        for (int j = 1; j < maxLog; j++)
        {
            for (int node = 0; node < n; node++)
            {
                up[node, j] = up[up[node, j - 1], j - 1];
            }
        }

        int[] result = new int[queries.Length];

        for (int i = 0; i < queries.Length; i++)
        {
            int a = queries[i][0];
            int b = queries[i][1];

            result[i] = Lca(a, b, depth, up, maxLog);
        }

        return result;
    }

    private int Lca(int a, int b, int[] depth, int[,] up, int maxLog)
    {
        // Step 1:
        // Ensure "a" is the deeper node (or equal depth).
        // Why?
        // It is easier to always lift one known deeper node upward first.
        if (depth[a] < depth[b])
        {
            int temp = a;
            a = b;
            b = temp;
        }

        // Step 2:
        // Lift "a" upward until both nodes are at the same depth.
        //
        // Suppose depth[a] = 10 and depth[b] = 6.
        // Then diff = 4, so we need to move "a" up by 4 levels.
        // Binary lifting lets us do this in O(log n) by checking bits of diff.
        int diff = depth[a] - depth[b];
        for (int j = 0; j < maxLog; j++)
        {
            if (((diff >> j) & 1) == 1)
            {
                a = up[a, j];
            }
        }

        // Step 3:
        // If after equalizing depth they are already the same node,
        // then that node is the LCA.
        //
        // This correctly handles cases like:
        // - one node is ancestor of the other
        // - both nodes are identical
        if (a == b)
        {
            return a;
        }

        // Step 4:
        // Lift both nodes upward together, from the largest jump size down to the smallest.
        //
        // Why from large to small?
        // We want to move them upward as much as possible without making them equal too early.
        // If up[a, j] != up[b, j], then their LCA is strictly above those ancestors,
        // so it is safe and useful to lift both by 2^j.
        //
        // After this loop finishes, a and b will be immediate children of the LCA.
        for (int j = maxLog - 1; j >= 0; j--)
        {
            if (up[a, j] != up[b, j])
            {
                a = up[a, j];
                b = up[b, j];
            }
        }

        // Step 5:
        // Now a and b are different, but their direct parent is the same.
        // That shared parent is the LCA.
        return up[a, 0];
    }
}

static void PrintArray(int[] arr)
{
    Console.WriteLine("[" + string.Join(",", arr) + "]");
}

// Demo code

var solution = new Solution();

// Example 1
int[] parent1 = { -1, 0, 0, 1, 1, 2, 2 };
int[][] queries1 =
{
    new[] { 3, 4 },
    new[] { 3, 5 },
    new[] { 2, 6 },
    new[] { 0, 6 }
};

int[] answer1 = solution.FindNearestSharedBackupFolder(parent1, queries1);
Console.WriteLine("Example 1 Output:");
PrintArray(answer1);
// Expected: [1,0,2,0]

// Example 2
int[] parent2 = { -1, 0, 1, 2, 3, 1, 5 };
int[][] queries2 =
{
    new[] { 4, 6 },
    new[] { 2, 4 },
    new[] { 6, 6 },
    new[] { 3, 5 }
};

int[] answer2 = solution.FindNearestSharedBackupFolder(parent2, queries2);
Console.WriteLine("Example 2 Output:");
PrintArray(answer2);
// Expected: [1,2,6,1]