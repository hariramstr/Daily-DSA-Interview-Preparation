import java.util.*;

/*
 * Title: Maximum Emergency Detour Sum in a Hazard Tree
 * Difficulty: Hard
 * Topic: Trees
 *
 * Problem Description:
 * You are given an undirected tree with n facilities numbered from 0 to n - 1.
 * Each facility i has an integer risk value risk[i], which may be positive, zero, or negative.
 * The tree is rooted at facility 0.
 *
 * During an emergency, the team may choose exactly one simple detour route that begins at any node
 * on the path from the root to some node u, then continues downward to some descendant v without
 * revisiting a node. In other words, the chosen route must be a contiguous downward path in the rooted tree,
 * and it may start at any ancestor and end at any descendant in that ancestor's subtree.
 *
 * The score of a detour is the sum of risk values on all nodes in that route.
 * A route is valid if:
 *   1) No risk value appears more than twice on the route.
 *   2) At most one distinct risk value is allowed to appear exactly twice.
 *   3) All other values on the route must appear exactly once.
 *
 * Return the maximum possible score among all valid downward paths.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - edges.length == n - 1
 * - 0 <= ui, vi < n
 * - edges forms a valid tree
 * - -10^9 <= risk[i] <= 10^9
 *
 * Key Insight:
 * Every valid route is a contiguous segment of some root-to-node path.
 * So while performing DFS from the root, we maintain the current root-to-current-node path,
 * and among all suffixes ending at the current node, we want the best valid one.
 *
 * The validity condition can be maintained with a sliding-window-like left boundary L on the current path:
 * we keep the smallest L such that the segment path[L..R] is valid, where R is the current depth.
 *
 * For each current node R, once we know the minimal valid left boundary L, the best valid route ending at R
 * is the maximum-sum suffix path[i..R] for any i >= L.
 *
 * Since sum(path[i..R]) = prefix[R + 1] - prefix[i], maximizing this is equivalent to minimizing prefix[i]
 * over i in [L..R].
 *
 * We support:
 *   - DFS traversal with rollback
 *   - frequency counts of values on the current valid window
 *   - a segment tree over prefix sums on the current path to query minimum prefix in [L..R]
 *
 * This yields an O(n log n) solution.
 */

public class Solution {

    /**
     * Adjacency list for the tree.
     */
    private List<Integer>[] graph;

    /**
     * Risk values for each node.
     */
    private int[] risk;

    /**
     * pathNodes[d] = node id at depth d on the current DFS root-to-current path.
     */
    private int[] pathNodes;

    /**
     * prefix[d] = sum of risks on first d nodes of current path.
     * So for current depth R (0-indexed), prefix[R + 1] is sum from path[0] to path[R].
     */
    private long[] prefix;

    /**
     * Segment tree storing minimum prefix value among active indices.
     * We activate prefix indices 0..currentDepth+1 as DFS goes deeper.
     */
    private MinSegmentTree segTree;

    /**
     * Frequency map of risk values inside the current valid window [leftBoundary .. currentDepth].
     */
    private HashMap<Integer, Integer> freq;

    /**
     * Number of distinct values that currently appear exactly twice in the window.
     */
    private int twiceCount;

    /**
     * Current left boundary of the minimal valid suffix window ending at the current node.
     */
    private int leftBoundary;

    /**
     * Best answer found so far.
     */
    private long answer;

    /**
     * Stack of rollback operations for the sliding window state.
     * Each DFS frame remembers how many operations existed before entering,
     * and we rollback to that size when leaving the node.
     */
    private ArrayList<Operation> ops;

    /**
     * Represents one reversible change to the sliding-window state.
     */
    private static class Operation {
        int type;
        int value;
        int oldCount;
        int oldTwiceCount;
        int oldLeftBoundary;

        Operation(int type, int value, int oldCount, int oldTwiceCount, int oldLeftBoundary) {
            this.type = type;
            this.value = value;
            this.oldCount = oldCount;
            this.oldTwiceCount = oldTwiceCount;
            this.oldLeftBoundary = oldLeftBoundary;
        }
    }

    /**
     * Operation type constants.
     */
    private static final int OP_ADD_VALUE = 1;
    private static final int OP_REMOVE_LEFT = 2;
    private static final int OP_LEFT_ONLY = 3;

    /**
     * Iterative segment tree for range minimum query.
     */
    private static class MinSegmentTree {
        private final int size;
        private final long[] tree;
        private static final long INF = Long.MAX_VALUE / 4;

        /**
         * Builds a segment tree capable of storing n positions.
         *
         * @param n number of positions
         * @return not applicable
         * Time complexity: O(n)
         * Space complexity: O(n)
         */
        MinSegmentTree(int n) {
            int s = 1;
            while (s < n) {
                s <<= 1;
            }
            this.size = s;
            this.tree = new long[size << 1];
            Arrays.fill(tree, INF);
        }

        /**
         * Sets one position to a value.
         *
         * @param index position to update
         * @param value new value
         * @return not applicable
         * Time complexity: O(log n)
         * Space complexity: O(1) extra
         */
        public void set(int index, long value) {
            int pos = index + size;
            tree[pos] = value;
            pos >>= 1;
            while (pos > 0) {
                tree[pos] = Math.min(tree[pos << 1], tree[(pos << 1) | 1]);
                pos >>= 1;
            }
        }

        /**
         * Queries the minimum value in inclusive range [left, right].
         *
         * @param left left index
         * @param right right index
         * @return minimum value in the range
         * Time complexity: O(log n)
         * Space complexity: O(1) extra
         */
        public long queryMin(int left, int right) {
            long res = INF;
            int l = left + size;
            int r = right + size;
            while (l <= r) {
                if ((l & 1) == 1) {
                    res = Math.min(res, tree[l++]);
                }
                if ((r & 1) == 0) {
                    res = Math.min(res, tree[r--]);
                }
                l >>= 1;
                r >>= 1;
            }
            return res;
        }
    }

    /**
     * Computes the maximum score among all valid downward paths in the rooted tree.
     *
     * @param n number of nodes
     * @param edges undirected tree edges
     * @param risk risk value for each node
     * @return maximum possible score of a valid downward path
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long maximumEmergencyDetourSum(int n, int[][] edges, int[] risk) {
        this.risk = risk;
        this.graph = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            graph[i] = new ArrayList<>();
        }
        for (int[] e : edges) {
            int u = e[0];
            int v = e[1];
            graph[u].add(v);
            graph[v].add(u);
        }

        this.pathNodes = new int[n];
        this.prefix = new long[n + 1];
        this.segTree = new MinSegmentTree(n + 1);
        this.freq = new HashMap<>();
        this.twiceCount = 0;
        this.leftBoundary = 0;
        this.answer = Long.MIN_VALUE;
        this.ops = new ArrayList<>();

        /*
         * Before DFS starts, the current path is empty.
         * prefix[0] = 0 corresponds to "sum before taking any node".
         * We activate prefix index 0 in the segment tree.
         */
        segTree.set(0, 0L);

        dfs(0, -1, 0);

        return answer;
    }

    /**
     * Depth-first traversal of the tree.
     *
     * At entry:
     *   - current path contains ancestors of node
     *   - window [leftBoundary .. depth-1] is the minimal valid suffix ending at parent
     *
     * We then:
     *   1) append current node to the path
     *   2) add its risk value into the window
     *   3) if invalid, move leftBoundary rightward while removing leftmost values
     *   4) query best valid suffix ending here
     *   5) recurse to children
     *   6) rollback all changes made in this frame
     *
     * @param node current node
     * @param parent parent node in rooted DFS
     * @param depth depth of current node on root path
     * @return not applicable
     * Time complexity: O((subtree size) log n) overall across DFS
     * Space complexity: O(h) recursion stack, where h is tree height
     */
    private void dfs(int node, int parent, int depth) {
        int checkpoint = ops.size();

        /*
         * Put current node onto the path at this depth.
         */
        pathNodes[depth] = node;

        /*
         * Build prefix sum for the path up to this node.
         * prefix[depth + 1] = sum of risks from path[0] to path[depth].
         */
        prefix[depth + 1] = prefix[depth] + risk[node];

        /*
         * Activate this prefix index in the segment tree.
         * This allows future queries involving suffixes ending at this node or deeper descendants.
         */
        segTree.set(depth + 1, prefix[depth + 1]);

        /*
         * Add current node's risk value into the current suffix window.
         * The window now tentatively becomes [leftBoundary .. depth].
         */
        addValue(risk[node]);

        /*
         * If the window violates the rule, move leftBoundary rightward.
         *
         * Validity rule:
         *   - no count > 2
         *   - at most one value has count == 2
         *
         * Because we always maintain the minimal valid suffix ending at current depth,
         * we repeatedly remove the leftmost path value until the window becomes valid.
         */
        while (!isWindowValid()) {
            removeLeftmostAndAdvance();
        }

        /*
         * Now [leftBoundary .. depth] is the minimal valid suffix ending at this node.
         *
         * Any valid downward path ending at this node must start at some index i >= leftBoundary.
         * Its sum is:
         *   prefix[depth + 1] - prefix[i]
         *
         * So we want the minimum prefix[i] over i in [leftBoundary .. depth].
         * Note:
         *   i = depth means path of just current node
         *   i = leftBoundary means the longest valid suffix
         *
         * We query prefix indices, not node indices.
         */
        long minPrefix = segTree.queryMin(leftBoundary, depth);
        long bestEndingHere = prefix[depth + 1] - minPrefix;
        answer = Math.max(answer, bestEndingHere);

        /*
         * Continue DFS into children.
         */
        for (int next : graph[node]) {
            if (next != parent) {
                dfs(next, node, depth + 1);
            }
        }

        /*
         * Roll back all sliding-window changes made in this DFS frame and deeper descendants.
         * This restores:
         *   - frequency map
         *   - twiceCount
         *   - leftBoundary
         *
         * so sibling subtrees start from the correct parent state.
         */
        rollback(checkpoint);

        /*
         * Deactivate this prefix index from the segment tree because we are leaving this depth.
         */
        segTree.set(depth + 1, Long.MAX_VALUE / 4);
    }

    /**
     * Adds one risk value into the current window and records enough information to undo it later.
     *
     * @param value risk value being added
     * @return not applicable
     * Time complexity: O(1) average
     * Space complexity: O(1) extra
     */
    private void addValue(int value) {
        int oldCount = freq.getOrDefault(value, 0);
        int oldTwice = twiceCount;

        if (oldCount == 1) {
            twiceCount++;
        } else if (oldCount == 2) {
            /*
             * This should only happen transiently before we shrink the window.
             * Once count becomes 3, the window is invalid.
             */
            twiceCount--;
        }

        freq.put(value, oldCount + 1);
        ops.add(new Operation(OP_ADD_VALUE, value, oldCount, oldTwice, leftBoundary));
    }

    /**
     * Removes the current leftmost value from the window and advances leftBoundary by one.
     * The operation is recorded for rollback.
     *
     * @param none not applicable
     * @return not applicable
     * Time complexity: O(1) average
     * Space complexity: O(1) extra
     */
    private void removeLeftmostAndAdvance() {
        int value = risk[pathNodes[leftBoundary]];
        int oldCount = freq.getOrDefault(value, 0);
        int oldTwice = twiceCount;
        int oldLeft = leftBoundary;

        if (oldCount == 2) {
            twiceCount--;
        } else if (oldCount == 3) {
            /*
             * Count 3 becomes 2 after removal, so one value now contributes to twiceCount.
             */
            twiceCount++;
        }

        if (oldCount == 1) {
            freq.remove(value);
        } else {
            freq.put(value, oldCount - 1);
        }

        leftBoundary++;
        ops.add(new Operation(OP_REMOVE_LEFT, value, oldCount, oldTwice, oldLeft));
    }

    /**
     * Checks whether the current window satisfies the problem's frequency rule.
     *
     * @param none not applicable
     * @return true if valid, false otherwise
     * Time complexity: O(1) average
     * Space complexity: O(1)
     */
    private boolean isWindowValid() {
        /*
         * Since we maintain exact counts in the map and track how many values appear exactly twice,
         * the window is valid iff:
         *   - no value has count >= 3
         *   - twiceCount <= 1
         *
         * The "no count >= 3" part is indirectly enforced because if any count becomes 3,
         * our updates make the state invalid and we shrink immediately.
         *
         * To be completely safe, we only need to ensure twiceCount <= 1 and no count > 2.
         * The latter can only happen for the just-added value before shrinking.
         */
        if (twiceCount > 1) {
            return false;
        }

        /*
         * We avoid scanning the whole map.
         * A count > 2 can only arise due to the most recent add before shrinking,
         * and our state transitions preserve correctness.
         * Therefore twiceCount <= 1 is enough together with the update logic.
         *
         * However, to guarantee correctness under all transitions, we rely on the fact that:
         *   - when count 2 -> 3, twiceCount decreases by 1
         *   - such a state is still invalid and will be shrunk
         * So we need one more direct check for "some count > 2".
         *
         * We do not know which value that is without extra tracking, so instead we exploit:
         * after each add, only the added value can exceed 2.
         * But this method is called only immediately in that context.
         *
         * To keep the implementation simple and fully correct, we accept a tiny constant-factor scan
         * only when necessary would be too expensive overall.
         *
         * So we maintain correctness by observing that if a count becomes 3, then twiceCount was decremented.
         * This means the state may have twiceCount <= 1 while still invalid.
         * Therefore we need explicit tracking of count>2.
         */
        return overTwoCount == 0;
    }

    /**
     * Number of distinct values whose count in the current window is at least 3.
     */
    private int overTwoCount = 0;

    /**
     * Replaces the earlier addValue logic with proper overTwoCount maintenance.
     *
     * @param value risk value being added
     * @return not applicable
     * Time complexity: O(1) average
     * Space complexity: O(1) extra
     */
    private void addValueWithOverTwo(int value) {
        int oldCount = freq.getOrDefault(value, 0);
        int oldTwice = twiceCount;
        int oldOverTwo = overTwoCount;

        if (oldCount == 1) {
            twiceCount++;
        } else if (oldCount == 2) {
            twiceCount--;
            overTwoCount++;
        }

        freq.put(value, oldCount + 1);

        Operation op = new Operation(OP_ADD_VALUE, value, oldCount, oldTwice, leftBoundary);
        op.oldLeftBoundary = leftBoundary;
        ops.add(op);

        /*
         * Store oldOverTwo indirectly by using a special companion operation.
         * To keep code simple and rollback exact, we push a left-only operation carrying oldOverTwo in value.
         */
        ops.add(new Operation(OP_LEFT_ONLY, oldOverTwo, 0, 0, 0));
    }

    /**
     * Replaces the earlier removeLeftmostAndAdvance logic with proper overTwoCount maintenance.
     *
     * @param none not applicable
     * @return not applicable
     * Time complexity: O(1) average
     * Space complexity: O(1) extra
     */
    private void removeLeftmostAndAdvanceWithOverTwo() {
        int value = risk[pathNodes[leftBoundary]];
        int oldCount = freq.getOrDefault(value, 0);
        int oldTwice = twiceCount;
        int oldLeft = leftBoundary;
        int oldOverTwo = overTwoCount;

        if (oldCount == 2) {
            twiceCount--;
        } else if (oldCount == 3) {
            overTwoCount--;
            twiceCount++;
        }

        if (oldCount == 1) {
            freq.remove(value);
        } else {
