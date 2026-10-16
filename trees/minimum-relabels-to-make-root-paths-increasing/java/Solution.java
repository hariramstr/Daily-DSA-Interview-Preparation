import java.util.*;

/*
 * Minimum Relabels to Make Root Paths Increasing
 *
 * Problem Description:
 * You are given a rooted tree with nodes numbered from 0 to n - 1, rooted at node 0.
 * Each node i has an integer label value[i].
 *
 * You may relabel any node to any integer value, and changing the label of one node counts
 * as one relabel operation regardless of the new value chosen.
 *
 * A tree is called root-path increasing if for every node, the sequence of labels along the
 * path from the root to that node is strictly increasing. Equivalently, for every edge
 * parent -> child, the final label of the child must be strictly greater than the final label
 * of the parent, and this must hold consistently on all root-to-node paths.
 *
 * Return the minimum number of nodes that must be relabeled so the entire tree becomes
 * root-path increasing.
 *
 * Important notes:
 * - You are allowed to keep a node's original label if it already fits the final assignment.
 * - Relabeled values do not need to come from the original set of labels.
 * - The goal is to maximize how many original labels can remain unchanged while still allowing
 *   a strictly increasing assignment on every root-to-node path.
 *
 * Key Insight:
 * A node can keep its original value if and only if, among all kept nodes on the path from the
 * root to that node, the values are strictly increasing. Since changed nodes can always be
 * assigned arbitrary "filler" values between kept nodes (or below/above them), the only real
 * restriction is on the set of nodes we decide to keep unchanged:
 *
 * For every ancestor-descendant pair of kept nodes, ancestor.value < descendant.value.
 *
 * Therefore, the problem becomes:
 *   Find the largest subset of nodes that can be kept such that on every root-to-node path,
 *   kept values are strictly increasing.
 *
 * Then:
 *   answer = n - maximum number of keepable nodes
 *
 * This is exactly the maximum number of nodes we can select in the tree so that along each
 * root path the selected values are increasing. We solve it with dynamic programming over
 * values using a Fenwick tree (Binary Indexed Tree) on compressed coordinates, while traversing
 * the tree with DFS and rollback.
 *
 * DP meaning:
 *   Let dp[u] be the maximum number of kept nodes in a valid selected set ending at node u,
 *   where u itself is kept.
 *
 * Then:
 *   dp[u] = 1 + max(dp[a]) over kept ancestor nodes a with value[a] < value[u]
 *   or dp[u] = 1 if no such ancestor exists.
 *
 * During DFS, we maintain data for the current root-to-current-node path only.
 * A Fenwick tree stores, for each compressed value rank, the maximum dp among ancestors on
 * the current path having that value.
 *
 * For node u:
 *   best = maximum dp among ancestor values < value[u]
 *   dp[u] = best + 1
 *
 * We then "activate" u in the Fenwick tree before exploring children, and rollback afterward
 * so sibling subtrees are unaffected.
 *
 * Why rollback works:
 * The Fenwick tree must represent exactly the current DFS path. Since Fenwick tree updates are
 * monotonic max-updates, we cannot simply subtract on exit. Instead, we record every Fenwick
 * cell changed during an update and restore previous values after finishing the subtree.
 *
 * Complexity:
 *   Coordinate compression: O(n log n)
 *   DFS with Fenwick queries/updates: O(n log n)
 *   Total: O(n log n)
 *   Space: O(n)
 */
public class Solution {

    /**
     * Solves the problem: minimum number of relabel operations needed so that every root-to-node
     * path becomes strictly increasing.
     *
     * The method computes the maximum number of nodes that can keep their original values while
     * still allowing a globally valid strictly increasing assignment on every root path.
     * The answer is total nodes minus that maximum.
     *
     * @param n     number of nodes in the tree
     * @param edges undirected edges of the tree, length n - 1
     * @param value original labels of the nodes
     * @return minimum number of nodes that must be relabeled
     *
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int minimumRelabels(int n, int[][] edges, int[] value) {
        if (n <= 1) {
            return 0;
        }

        List<Integer>[] graph = buildGraph(n, edges);

        int[] compressed = compressValues(value);
        int m = 0;
        for (int x : compressed) {
            m = Math.max(m, x);
        }

        int[] parent = new int[n];
        int[] order = buildRootedOrder(graph, parent);

        FenwickMaxRollback fenwick = new FenwickMaxRollback(m);

        int[] dp = new int[n];
        int bestKeep = 0;

        /*
         * We perform an iterative DFS with explicit enter/exit states.
         *
         * Why iterative instead of recursive?
         * - n can be as large as 2 * 10^5
         * - recursive DFS may overflow the Java call stack
         *
         * Stack frame fields:
         *   node   = current node
         *   state  = 0 means "enter node", 1 means "exit node"
         */
        int[] stackNode = new int[2 * n];
        int[] stackState = new int[2 * n];
        int top = 0;

        stackNode[top] = 0;
        stackState[top] = 0;
        top++;

        while (top > 0) {
            top--;
            int u = stackNode[top];
            int state = stackState[top];

            if (state == 0) {
                /*
                 * ENTER NODE u
                 *
                 * Step 1:
                 *   Query the best dp among ancestors on the current path whose value is
                 *   strictly smaller than value[u].
                 *
                 * Since values are coordinate-compressed to 1..m:
                 *   query(compressed[u] - 1) gives the maximum dp among ranks < rank[u].
                 */
                int rank = compressed[u];
                int bestBefore = fenwick.query(rank - 1);

                /*
                 * Step 2:
                 *   If we keep node u unchanged, then the best chain ending at u has length:
                 *     dp[u] = bestBefore + 1
                 */
                dp[u] = bestBefore + 1;
                bestKeep = Math.max(bestKeep, dp[u]);

                /*
                 * Step 3:
                 *   Add node u to the current path structure.
                 *   This means future descendants may use u as a previous kept node.
                 *
                 * The Fenwick tree stores maximum dp by value rank.
                 * We use rollback-aware update because after finishing u's subtree,
                 * we must restore the structure to the exact state before entering u.
                 */
                fenwick.beginScope();
                fenwick.update(rank, dp[u]);

                /*
                 * Step 4:
                 *   Schedule EXIT of u after all children are processed.
                 */
                stackNode[top] = u;
                stackState[top] = 1;
                top++;

                /*
                 * Step 5:
                 *   Push children for DFS.
                 *
                 * The tree is rooted at 0. We skip the parent to avoid going back upward.
                 */
                List<Integer> adj = graph[u];
                for (int i = adj.size() - 1; i >= 0; i--) {
                    int v = adj.get(i);
                    if (v == parent[u]) {
                        continue;
                    }
                    stackNode[top] = v;
                    stackState[top] = 0;
                    top++;
                }
            } else {
                /*
                 * EXIT NODE u
                 *
                 * Roll back all Fenwick modifications that were made when entering u.
                 * This removes u from the active root-to-current-path structure, so siblings
                 * do not incorrectly see u as an ancestor.
                 */
                fenwick.rollbackScope();
            }
        }

        return n - bestKeep;
    }

    /**
     * Builds an undirected adjacency list for the tree.
     *
     * @param n     number of nodes
     * @param edges undirected edges
     * @return adjacency list representation of the tree
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public List<Integer>[] buildGraph(int n, int[][] edges) {
        @SuppressWarnings("unchecked")
        List<Integer>[] graph = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            graph[i] = new ArrayList<>();
        }
        for (int[] e : edges) {
            int u = e[0];
            int v = e[1];
            graph[u].add(v);
            graph[v].add(u);
        }
        return graph;
    }

    /**
     * Builds parent information and a traversal order from the root (node 0).
     *
     * This method performs an iterative DFS/BFS-style traversal to determine the parent of each
     * node in the rooted tree. The returned order is simply a rooted traversal order and is not
     * directly required by the final DFS, but it is often useful and keeps the solution modular.
     *
     * @param graph adjacency list of the tree
     * @param parent output array where parent[u] will be filled
     * @return traversal order starting from root 0
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] buildRootedOrder(List<Integer>[] graph, int[] parent) {
        int n = graph.length;
        Arrays.fill(parent, -1);

        int[] order = new int[n];
        int[] stack = new int[n];
        int top = 0;
        int idx = 0;

        stack[top++] = 0;
        parent[0] = -1;

        while (top > 0) {
            int u = stack[--top];
            order[idx++] = u;
            for (int v : graph[u]) {
                if (v == parent[u]) {
                    continue;
                }
                parent[v] = u;
                stack[top++] = v;
            }
        }
        return order;
    }

    /**
     * Compresses the values into ranks 1..m while preserving order.
     *
     * Coordinate compression is necessary because original values can be as small as -1e9 and
     * as large as 1e9, but Fenwick trees require compact indices.
     *
     * Example:
     *   values = [8, 3, 4, 10]
     *   sorted unique = [3, 4, 8, 10]
     *   compressed = [3, 1, 2, 4]
     *
     * @param value original node values
     * @return compressed ranks, 1-indexed
     *
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int[] compressValues(int[] value) {
        int n = value.length;
        int[] sorted = value.clone();
        Arrays.sort(sorted);

        int[] unique = new int[n];
        int m = 0;
        for (int x : sorted) {
            if (m == 0 || unique[m - 1] != x) {
                unique[m++] = x;
            }
        }

        int[] compressed = new int[n];
        for (int i = 0; i < n; i++) {
            compressed[i] = lowerBound(unique, m, value[i]) + 1;
        }
        return compressed;
    }

    /**
     * Finds the first index in arr[0..len) such that arr[index] >= target.
     *
     * @param arr    sorted array
     * @param len    valid length of the array prefix
     * @param target target value
     * @return lower bound index
     *
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public int lowerBound(int[] arr, int len, int target) {
        int left = 0;
        int right = len;
        while (left < right) {
            int mid = (left + right) >>> 1;
            if (arr[mid] < target) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }
        return left;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (unused)
     *
     * Time complexity: O(1) for the fixed demo size, excluding the called solver
     * Space complexity: O(1), excluding the called solver
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        int n1 = 5;
        int[][] edges1 = {
                {0, 1},
                {0, 2},
                {1, 3},
                {1, 4}
        };
        int[] value1 = {3, 4, 2, 5, 6};
        int ans1 = sol.minimumRelabels(n1, edges1, value1);
        System.out.println(ans1); // Expected: 1

        int n2 = 6;
        int[][] edges2 = {
                {0, 1},
                {1, 2},
                {2, 3},
                {1, 4},
                {4, 5}
        };
        int[] value2 = {8, 3, 4, 10, 9, 11};
        int ans2 = sol.minimumRelabels(n2, edges2, value2);
        System.out.println(ans2); // Expected: 2
    }

    /**
     * Fenwick tree (Binary Indexed Tree) supporting:
     * - prefix maximum query
     * - point-like "max update" propagated through Fenwick structure
     * - rollback of updates grouped by DFS scope
     *
     * This data structure is the heart of the O(n log n) solution.
     *
     * Standard Fenwick trees are often used for sums, but they also work for prefix maximums
     * when updates are monotonic. Here, because DFS requires us to remove updates when leaving
     * a subtree, we add rollback support by recording every changed tree cell and its old value.
     */
    static class FenwickMaxRollback {
        private final int size;
        private final int[] tree;

        /*
         * History stacks for rollback:
         *   changedIndex[k] = Fenwick cell index that was modified
         *   oldValue[k]     = previous value before modification
         *
         * scopeStart stores the history size at the beginning of each DFS scope.
         * On rollback, we restore all changes back to that size.
         */
        private final int[] changedIndex;
        private final int[] oldValue;
        private int historySize;

        private final int[] scopeStart;
        private int scopeSize;

        /**
         * Creates a rollback-capable Fenwick tree for indices 1..size.
         *
         * @param size maximum compressed coordinate
         *
         * Time complexity: O(1)
         * Space complexity: O(size)
         */
        FenwickMaxRollback(int size) {
            this.size = size;
            this.tree = new int[size + 2];

            /*
             * In one update, a Fenwick tree touches O(log size) cells.
             * Across n nodes, total changes are O(n log n).
             *
             * Since the outer solution uses n up to 2e5 and log2(n) < 18,
             * allocating about 25 * n is safely enough in practice.
             * To be extra safe, we use 40 * (size + 5).
             */
            int cap = Math.max(8, 40 * (size + 5));
            this.changedIndex = new int[cap];
            this.oldValue = new int[cap];
            this.historySize = 0;

            this.scopeStart = new int[Math.max(8, size + 5)];
            this.scopeSize = 0;
        }

        /**
         * Starts a new rollback scope.
         *
         * All updates after this call can later be undone by rollbackScope().
         *
         * Time complexity: O(1)
         * Space complexity: O(1)
         */
        void beginScope() {
            scopeStart[scopeSize++] = historySize;
        }

        /**
         * Rolls back all updates made since the most recent beginScope().
         *
         * Time complexity: O(number of reverted Fenwick cell changes)
         * Space complexity: O(1)
         */
        void rollbackScope() {
            int targetHistorySize = scopeStart[--scopeSize];
            while (historySize > targetHistorySize) {
                historySize--;
                int idx = changedIndex[historySize];
                tree[idx] = oldValue[historySize];
            }
        }

        /**
         * Applies:
         *   tree[pos] = max(tree[pos], value)
         * to all Fenwick cells covering pos.
         *
         * Every changed cell is recorded so it can be restored later.
         *
         * @param pos   compressed coordinate, 1-indexed
         * @param value dp value to merge
         *
         * Time complexity: O(log size)
         * Space complexity: O(log size) additional history entries
         */
        void update(int pos, int value) {
            int i = pos;
            while (i <= size) {
                if (tree[i] < value) {
                    changedIndex[historySize] = i;
                    oldValue[historySize] = tree[i];
                    historySize++;
                    tree[i] = value;
                }
                i += i & -i;
            }
        }

        /**
         * Returns the maximum value stored on prefix [1..pos].
         *
         * @param pos right endpoint of the prefix
         * @return maximum value on the prefix
         *
         * Time complexity: O(log size)
         * Space complexity: O(1)
         */
        int query(int pos) {
            int ans = 0;
            int i = pos;
            while (i > 0) {
                ans = Math.max(ans, tree[i]);
                i -= i & -i;
            }
            return ans;
        }
    }
}