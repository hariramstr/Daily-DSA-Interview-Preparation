/*
 * Friend Circle Size Finder
 * Difficulty: Easy
 * Topic: Graphs
 *
 * Problem Description:
 * You are given a social network of `n` users, numbered from `0` to `n - 1`.
 * You are also given a list of `connections`, where `connections[i] = [a, b]`
 * means user `a` and user `b` are direct friends. Friendship is bidirectional
 * and transitive — if A is friends with B and B is friends with C, then A, B,
 * and C all belong to the same friend circle.
 *
 * A **friend circle** is a group of users who are directly or indirectly
 * connected to each other.
 *
 * Return a list of integers representing the **sizes of all friend circles**,
 * sorted in **descending order**.
 *
 * Constraints:
 * - 1 <= n <= 1000
 * - 0 <= connections.length <= 5000
 * - connections[i].length == 2
 * - 0 <= connections[i][0], connections[i][1] < n
 * - connections[i][0] != connections[i][1]
 * - There are no duplicate connections.
 *
 * Example 1:
 * Input: n = 6, connections = [[0,1],[1,2],[3,4]]
 * Output: [3, 2, 1]
 * Explanation: Users {0,1,2} form a circle of size 3, users {3,4} form a
 * circle of size 2, and user {5} is alone with size 1.
 *
 * Example 2:
 * Input: n = 4, connections = [[0,1],[2,3],[1,3]]
 * Output: [4]
 * Explanation: All four users are connected transitively, forming one circle
 * of size 4.
 */

import java.util.*;

/**
 * Solution class for the Friend Circle Size Finder problem.
 *
 * <p>We use the <strong>Union-Find (Disjoint Set Union)</strong> data structure,
 * which is perfect for grouping elements into connected components and quickly
 * querying which component a node belongs to.
 *
 * <p>Key idea:
 * <ul>
 *   <li>Each user starts as their own "circle" (singleton component).</li>
 *   <li>For every friendship connection, we <em>union</em> the two users'
 *       components together.</li>
 *   <li>After processing all connections, we count how many users share the
 *       same root (representative) to get each circle's size.</li>
 *   <li>Finally, we sort the sizes in descending order and return them.</li>
 * </ul>
 */
public class Solution {

    // -----------------------------------------------------------------------
    // Union-Find (Disjoint Set Union) helper arrays
    // -----------------------------------------------------------------------

    /**
     * parent[i] holds the representative (root) of the component that user i
     * belongs to. Initially every user is their own representative.
     */
    private int[] parent;

    /**
     * rank[i] is used for "union by rank" — we attach the shorter tree under
     * the taller tree to keep the structure flat and operations fast.
     */
    private int[] rank;

    // -----------------------------------------------------------------------
    // Union-Find operations
    // -----------------------------------------------------------------------

    /**
     * Initialises the Union-Find structure for {@code n} users.
     * Each user starts as their own parent (isolated node).
     *
     * @param n the total number of users
     * Time complexity:  O(n)
     * Space complexity: O(n)
     */
    private void initUnionFind(int n) {
        parent = new int[n];
        rank   = new int[n];

        // Step: make every node its own root and give it rank 0
        for (int i = 0; i < n; i++) {
            parent[i] = i;   // user i is its own representative
            rank[i]   = 0;   // tree height starts at 0
        }
    }

    /**
     * Finds the root (representative) of the component that contains user
     * {@code x}, applying <em>path compression</em> so that future queries
     * for any node on this path are answered in nearly O(1).
     *
     * @param x the user whose root we want to find
     * @return  the root of x's component
     * Time complexity:  O(α(n)) amortised — effectively O(1)
     * Space complexity: O(α(n)) for the recursion stack (nearly O(1))
     */
    private int find(int x) {
        // Base case: x is its own parent, so x is the root
        if (parent[x] != x) {
            // Path compression: make every node on the path point directly
            // to the root, flattening the tree for future calls
            parent[x] = find(parent[x]);
        }
        return parent[x];
    }

    /**
     * Merges the components of users {@code a} and {@code b} using
     * <em>union by rank</em> to keep trees balanced.
     *
     * @param a first user
     * @param b second user
     * Time complexity:  O(α(n)) amortised
     * Space complexity: O(1)
     */
    private void union(int a, int b) {
        // Step 1: find the roots of both users
        int rootA = find(a);
        int rootB = find(b);

        // Step 2: if they already share the same root, they are already in
        //         the same component — nothing to do
        if (rootA == rootB) {
            return;
        }

        // Step 3: attach the smaller-rank tree under the larger-rank tree
        //         to keep the overall tree height as small as possible
        if (rank[rootA] < rank[rootB]) {
            // rootA's tree is shorter → hang it under rootB
            parent[rootA] = rootB;
        } else if (rank[rootA] > rank[rootB]) {
            // rootB's tree is shorter → hang it under rootA
            parent[rootB] = rootA;
        } else {
            // Both trees have the same height → pick one arbitrarily and
            // increase the winner's rank by 1
            parent[rootB] = rootA;
            rank[rootA]++;
        }
    }

    // -----------------------------------------------------------------------
    // Main algorithm
    // -----------------------------------------------------------------------

    /**
     * Computes the sizes of all friend circles in the social network and
     * returns them sorted in descending order.
     *
     * <p>Algorithm outline:
     * <ol>
     *   <li>Initialise Union-Find with {@code n} isolated users.</li>
     *   <li>For each connection {@code [a, b]}, union the two users.</li>
     *   <li>After all unions, find the root of every user and tally how many
     *       users share each root — that tally is the circle size.</li>
     *   <li>Collect the tallies, sort descending, and return.</li>
     * </ol>
     *
     * @param n           total number of users (0 … n-1)
     * @param connections array of pairs {@code [a, b]} indicating friendships
     * @return            list of friend-circle sizes sorted in descending order
     * Time complexity:  O(n + E · α(n) + n log n) where E = connections.length
     *                   — effectively O(n log n + E)
     * Space complexity: O(n) for the Union-Find arrays and the frequency map
     */
    public List<Integer> friendCircleSizes(int n, int[][] connections) {

        // ── Step 1: Initialise Union-Find ──────────────────────────────────
        // Every user starts in their own singleton component.
        initUnionFind(n);

        // ── Step 2: Process every friendship connection ────────────────────
        for (int[] connection : connections) {
            int userA = connection[0];
            int userB = connection[1];

            // Merge the two users' components.  If they are already in the
            // same component, union() is a no-op.
            union(userA, userB);
        }

        // ── Step 3: Count how many users belong to each component ──────────
        // We use a HashMap where the key is the root of a component and the
        // value is the number of users whose root is that key.
        Map<Integer, Integer> componentSize = new HashMap<>();

        for (int user = 0; user < n; user++) {
            // find() with path compression gives us the canonical root
            int root = find(user);

            // Increment the count for this root (default 0 if not yet seen)
            componentSize.put(root, componentSize.getOrDefault(root, 0) + 1);
        }

        // ── Step 4: Collect the sizes into a list ─────────────────────────
        List<Integer> sizes = new ArrayList<>(componentSize.values());

        // ── Step 5: Sort in descending order ──────────────────────────────
        // Collections.sort with a reversed comparator puts the largest first.
        sizes.sort(Collections.reverseOrder());

        return sizes;
    }

    // -----------------------------------------------------------------------
    // Main — demonstration with the provided examples
    // -----------------------------------------------------------------------

    /**
     * Entry point.  Runs both examples from the problem description and prints
     * the results so you can verify correctness at a glance.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        Solution sol = new Solution();

        // ── Example 1 ──────────────────────────────────────────────────────
        // n = 6, connections = [[0,1],[1,2],[3,4]]
        // Expected output: [3, 2, 1]
        //
        // Trace:
        //   union(0,1)  → {0,1}, {2}, {3}, {4}, {5}
        //   union(1,2)  → {0,1,2}, {3}, {4}, {5}
        //   union(3,4)  → {0,1,2}, {3,4}, {5}
        // Component sizes: 3, 2, 1  → sorted desc: [3, 2, 1]  ✓
        int n1 = 6;
        int[][] connections1 = {{0, 1}, {1, 2}, {3, 4}};
        List<Integer> result1 = sol.friendCircleSizes(n1, connections1);
        System.out.println("Example 1:");
        System.out.println("  Input : n=" + n1 + ", connections=" + Arrays.deepToString(connections1));
        System.out.println("  Output: " + result1);
        System.out.println("  Expected: [3, 2, 1]");
        System.out.println();

        // ── Example 2 ──────────────────────────────────────────────────────
        // n = 4, connections = [[0,1],[2,3],[1,3]]
        // Expected output: [4]
        //
        // Trace:
        //   union(0,1)  → {0,1}, {2}, {3}
        //   union(2,3)  → {0,1}, {2,3}
        //   union(1,3)  → find(1)=root of {0,1}, find(3)=root of {2,3}
        //               → merge → {0,1,2,3}
        // Component sizes: 4  → sorted desc: [4]  ✓
        int n2 = 4;
        int[][] connections2 = {{0, 1}, {2, 3}, {1, 3}};
        List<Integer> result2 = sol.friendCircleSizes(n2, connections2);
        System.out.println("Example 2:");
        System.out.println("  Input : n=" + n2 + ", connections=" + Arrays.deepToString(connections2));
        System.out.println("  Output: " + result2);
        System.out.println("  Expected: [4]");
        System.out.println();

        // ── Extra edge case: no connections ───────────────────────────────
        // Every user is their own circle → n circles of size 1
        int n3 = 3;
        int[][] connections3 = {};
        List<Integer> result3 = sol.friendCircleSizes(n3, connections3);
        System.out.println("Edge case (no connections):");
        System.out.println("  Input : n=" + n3 + ", connections=[]");
        System.out.println("  Output: " + result3);
        System.out.println("  Expected: [1, 1, 1]");
        System.out.println();

        // ── Extra edge case: single user ──────────────────────────────────
        int n4 = 1;
        int[][] connections4 = {};
        List<Integer> result4 = sol.friendCircleSizes(n4, connections4);
        System.out.println("Edge case (single user):");
        System.out.println("  Input : n=" + n4 + ", connections=[]");
        System.out.println("  Output: " + result4);
        System.out.println("  Expected: [1]");
    }
}