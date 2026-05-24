/*
 * Friend Circle Size Finder
 * Difficulty: Easy
 * Topic: Graphs
 *
 * Problem Description:
 * You are given a social network of `n` users, numbered from 0 to n-1.
 * You are also given a list of connections, where connections[i] = [a, b]
 * means user a and user b are direct friends. Friendship is bidirectional
 * and transitive — if A is friends with B and B is friends with C, then
 * A, B, and C all belong to the same friend circle.
 *
 * A friend circle is a group of users who are directly or indirectly
 * connected to each other.
 *
 * Return a list of integers representing the sizes of all friend circles,
 * sorted in descending order.
 *
 * Example 1:
 *   Input:  n = 6, connections = [[0,1],[1,2],[3,4]]
 *   Output: [3, 2, 1]
 *
 * Example 2:
 *   Input:  n = 4, connections = [[0,1],[2,3],[1,3]]
 *   Output: [4]
 */

// ─────────────────────────────────────────────────────────────────────────────
// Solution class using Union-Find (Disjoint Set Union) data structure.
//
// WHY Union-Find?
//   The core challenge is grouping users into connected components (circles).
//   Union-Find is the classic, efficient structure for exactly this task:
//   it lets us "union" two users into the same group and "find" which group
//   any user belongs to — both operations run in nearly O(1) amortized time
//   with path compression + union by rank optimizations.
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    // ── Union-Find helper arrays ──────────────────────────────────────────────
    // parent[i] = the parent of node i in the Union-Find tree.
    //             If parent[i] == i, then i is the root of its component.
    private int[] parent = [];

    // rank[i]  = an upper bound on the height of the subtree rooted at i.
    //            Used to keep trees shallow (union by rank optimization).
    private int[] rank = [];

    // ─────────────────────────────────────────────────────────────────────────
    // Find(x) — returns the root representative of the component containing x.
    //
    // PATH COMPRESSION: while walking up to the root we set every visited
    // node's parent directly to the root. This flattens the tree so future
    // Find calls are faster (amortized nearly O(1)).
    // ─────────────────────────────────────────────────────────────────────────
    private int Find(int x)
    {
        // If x is not its own parent, keep climbing toward the root.
        // At the same time, compress the path by pointing x directly at
        // whatever Find returns (the ultimate root).
        if (parent[x] != x)
            parent[x] = Find(parent[x]); // recursive path compression

        return parent[x]; // x is now pointing directly at the root
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Union(a, b) — merges the components containing a and b.
    //
    // UNION BY RANK: we attach the shorter tree under the taller tree.
    // This prevents the tree from becoming a long chain (which would make
    // Find slow). The rank is only an approximation of height but it works.
    // ─────────────────────────────────────────────────────────────────────────
    private void Union(int a, int b)
    {
        // Step 1: Find the root representatives of both nodes.
        int rootA = Find(a);
        int rootB = Find(b);

        // Step 2: If they already share the same root, they are already in
        //         the same component — nothing to do.
        if (rootA == rootB) return;

        // Step 3: Attach the smaller-rank tree under the larger-rank tree.
        if (rank[rootA] < rank[rootB])
        {
            // rootA's tree is shorter → make rootB the parent of rootA
            parent[rootA] = rootB;
        }
        else if (rank[rootA] > rank[rootB])
        {
            // rootB's tree is shorter → make rootA the parent of rootB
            parent[rootB] = rootA;
        }
        else
        {
            // Both trees have the same rank → arbitrarily pick rootB as parent,
            // then increment rootB's rank because the merged tree is now taller.
            parent[rootA] = rootB;
            rank[rootB]++;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FriendCircleSizes
    //
    // Time Complexity : O(n + E · α(n))  where E = number of connections and
    //                   α is the inverse Ackermann function (practically ≤ 5).
    //                   The final sort adds O(C log C) where C = # of circles.
    //                   Overall: O((n + E) · α(n) + n log n)
    //
    // Space Complexity: O(n) for the parent and rank arrays plus the output list.
    // ─────────────────────────────────────────────────────────────────────────
    public List<int> FriendCircleSizes(int n, int[][] connections)
    {
        // ── Step 1: Initialise Union-Find ─────────────────────────────────────
        // Every user starts as their own parent (each is their own circle).
        // Every rank starts at 0 (single-node trees have height 0).
        parent = new int[n];
        rank   = new int[n];

        for (int i = 0; i < n; i++)
        {
            parent[i] = i; // user i is the root of their own component
            rank[i]   = 0;
        }

        // ── Step 2: Process every connection ─────────────────────────────────
        // For each friendship [a, b], union the two users' components.
        // After all unions, users in the same friend circle share the same root.
        foreach (int[] connection in connections)
        {
            int a = connection[0];
            int b = connection[1];

            // Merge the circles that contain a and b.
            Union(a, b);
        }

        // ── Step 3: Count the size of each component ──────────────────────────
        // We iterate over every user, find their root, and tally how many users
        // share that root. A Dictionary maps root → count.
        Dictionary<int, int> componentSize = new Dictionary<int, int>();

        for (int user = 0; user < n; user++)
        {
            // Find the root representative for this user.
            // (Path compression also happens here, keeping future calls fast.)
            int root = Find(user);

            // Increment the count for this root's component.
            if (componentSize.ContainsKey(root))
                componentSize[root]++;
            else
                componentSize[root] = 1;
        }

        // ── Step 4: Collect sizes and sort descending ─────────────────────────
        // We only care about the sizes (not which root they belong to),
        // so extract just the values from the dictionary.
        List<int> sizes = new List<int>(componentSize.Values);

        // Sort in descending order so the largest circle appears first.
        sizes.Sort((x, y) => y.CompareTo(x));

        return sizes;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Test Code
// ─────────────────────────────────────────────────────────────────────────────

Solution sol = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// n = 6, connections = [[0,1],[1,2],[3,4]]
// Expected output: [3, 2, 1]
//
// Trace:
//   Initial:  parent = [0,1,2,3,4,5]
//   Union(0,1): root0=0, root1=1 → parent[0]=1, rank[1]=1  → parent=[1,1,2,3,4,5]
//   Union(1,2): root1=1, root2=2 → parent[2]=1             → parent=[1,1,1,3,4,5]
//   Union(3,4): root3=3, root4=4 → parent[3]=4, rank[4]=1  → parent=[1,1,1,4,4,5]
//   Roots: user0→1, user1→1, user2→1, user3→4, user4→4, user5→5
//   Counts: {1:3, 4:2, 5:1}  → sizes = [3,2,1] ✓
int n1 = 6;
int[][] connections1 = [[0, 1], [1, 2], [3, 4]];
List<int> result1 = sol.FriendCircleSizes(n1, connections1);
Console.WriteLine("Example 1:");
Console.WriteLine($"  Input : n={n1}, connections=[[0,1],[1,2],[3,4]]");
Console.WriteLine($"  Output: [{string.Join(", ", result1)}]");
Console.WriteLine($"  Expected: [3, 2, 1]");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// n = 4, connections = [[0,1],[2,3],[1,3]]
// Expected output: [4]
//
// Trace:
//   Initial:  parent = [0,1,2,3]
//   Union(0,1): root0=0, root1=1 → parent[0]=1, rank[1]=1  → parent=[1,1,2,3]
//   Union(2,3): root2=2, root3=3 → parent[2]=3, rank[3]=1  → parent=[1,1,3,3]
//   Union(1,3): root1=1, root3=3 → both rank=1 → parent[1]=3, rank[3]=2
//               → parent=[1,3,3,3]
//   Roots: user0→Find(0)=Find(1)=3, user1→3, user2→3, user3→3
//   Counts: {3:4}  → sizes = [4] ✓
int n2 = 4;
int[][] connections2 = [[0, 1], [2, 3], [1, 3]];
List<int> result2 = sol.FriendCircleSizes(n2, connections2);
Console.WriteLine("Example 2:");
Console.WriteLine($"  Input : n={n2}, connections=[[0,1],[2,3],[1,3]]");
Console.WriteLine($"  Output: [{string.Join(", ", result2)}]");
Console.WriteLine($"  Expected: [4]");
Console.WriteLine();

// ── Edge Case: No connections ─────────────────────────────────────────────────
// Every user is their own circle → n circles of size 1.
int n3 = 3;
int[][] connections3 = [];
List<int> result3 = sol.FriendCircleSizes(n3, connections3);
Console.WriteLine("Edge Case (no connections):");
Console.WriteLine($"  Input : n={n3}, connections=[]");
Console.WriteLine($"  Output: [{string.Join(", ", result3)}]");
Console.WriteLine($"  Expected: [1, 1, 1]");
Console.WriteLine();

// ── Edge Case: Single user ────────────────────────────────────────────────────
int n4 = 1;
int[][] connections4 = [];
List<int> result4 = sol.FriendCircleSizes(n4, connections4);
Console.WriteLine("Edge Case (single user):");
Console.WriteLine($"  Input : n={n4}, connections=[]");
Console.WriteLine($"  Output: [{string.Join(", ", result4)}]");
Console.WriteLine($"  Expected: [1]");