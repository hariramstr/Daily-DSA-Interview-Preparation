import java.util.*;

/*
 * Title: Maximum Score of a Bounded-Difference Trading Streak
 * Difficulty: Hard
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array profits where profits[i] represents the profit or loss recorded on day i.
 * You want to choose one contiguous streak of days and assign it a score. A streak is considered valid only
 * if the difference between the maximum and minimum value inside the streak is at most limit.
 *
 * The score of a valid streak is defined as:
 *
 *     (length of streak) * (sum of all values in the streak)
 *
 * Your task is to return the maximum possible score among all valid contiguous streaks. If every valid streak
 * has a negative score, you must still return the largest score among them; choosing an empty streak is not allowed.
 *
 * Constraints:
 * - 1 <= profits.length <= 2 * 10^5
 * - -10^9 <= profits[i] <= 10^9
 * - 0 <= limit <= 10^9
 * - The answer may exceed 32-bit integer range, so use 64-bit integers.
 *
 * Examples:
 *
 * Example 1:
 * Input: profits = [3, 1, 2, 2, 4], limit = 2
 * Output: 32
 * Explanation:
 * The entire array is invalid because max - min = 4 - 1 = 3.
 * The best valid streak is [3, 1, 2, 2], which has length 4 and sum 8,
 * so the score is 4 * 8 = 32.
 *
 * Example 2:
 * Input: profits = [-5, -2, -3, 1], limit = 1
 * Output: 1
 * Explanation:
 * Valid streaks must have max and min differing by at most 1.
 * The best choice is [1], with length 1 and sum 1, so the score is 1.
 *
 * Important note about the algorithm:
 * A plain sliding window is not enough, because among all valid windows ending at a fixed right boundary,
 * we must maximize:
 *
 *     (right - left + 1) * (prefix[right + 1] - prefix[left])
 *
 * This depends on both the left boundary and the sum, not only on the largest valid window.
 *
 * Efficient approach used here:
 * 1. Maintain the smallest valid left boundary for each right boundary using two monotonic deques:
 *    - one deque for current minimum
 *    - one deque for current maximum
 * 2. For each right boundary r, valid left boundaries are all l in [L[r], r], where L[r] is the smallest
 *    left index that keeps the window valid.
 * 3. Rewrite the score:
 *
 *      score(l, r) = (r - l + 1) * (prefix[r + 1] - prefix[l])
 *
 *    Let x = l and P[i] = prefix[i].
 *    Then:
 *
 *      score = (r + 1 - x) * (P[r + 1] - P[x])
 *            = (r + 1) * P[r + 1]
 *              + x * P[x]
 *              - P[r + 1] * x
 *              - (r + 1) * P[x]
 *
 *    For fixed r, define:
 *      a = P[r + 1]
 *      b = r + 1
 *
 *    Then maximizing score over x is equivalent to maximizing:
 *
 *      f_x(a, b) = x * P[x] - a * x - b * P[x]
 *
 *    This is a linear function in variables (a, b):
 *
 *      f_x(a, b) = c_x + m1_x * a + m2_x * b
 *      where:
 *        c_x  = x * P[x]
 *        m1_x = -x
 *        m2_x = -P[x]
 *
 * 4. Therefore, each possible left boundary x contributes a plane in 3D:
 *
 *      z = c + m1 * a + m2 * b
 *
 *    and for each right boundary we query the maximum plane value at point (a, b),
 *    but only among currently valid x values in the sliding range [L[r], r].
 *
 * 5. We solve this with a Li Chao segment tree over variable a = prefix value, where each node stores
 *    a convex hull of lines in variable b. This supports:
 *    - insertion of a plane active on a time interval [startRight, endRight]
 *    - query at a specific time/right boundary and point (a, b)
 *
 * 6. We process all right boundaries offline:
 *    - each left index x becomes active for right boundaries r in [x, expire[x] - 1]
 *      where expire[x] is the first right where x is no longer valid
 *    - insert that plane into a segment tree over time
 *    - DFS over the time tree, maintaining a Li Chao tree of hulls to answer each query
 *
 * This yields an efficient O(n log^2 n)-style solution suitable for n up to 2 * 10^5.
 */
public class Solution {

    /**
     * A very small negative sentinel used for "no answer" in maximum queries.
     */
    private static final long NEG_INF = Long.MIN_VALUE / 4;

    /**
     * Represents one candidate left boundary x as a plane:
     *
     *     value(a, b) = x * prefix[x] - a * x - b * prefix[x]
     *
     * For fixed a, this becomes a line in b:
     *
     *     value(b) = (-prefix[x]) * b + (x * prefix[x] - a * x)
     *
     * This is exactly what allows us to use a Li Chao tree over a,
     * where each node stores a convex hull of lines in b.
     */
    private static class Plane {
        long xIndex;
        long prefixAtX;

        Plane(long xIndex, long prefixAtX) {
            this.xIndex = xIndex;
            this.prefixAtX = prefixAtX;
        }

        /**
         * Evaluates the plane at point (a, b).
         *
         * @param a current prefix sum prefix[r + 1]
         * @param b current length parameter r + 1
         * @return plane value at (a, b)
         */
        long eval(long a, long b) {
            return xIndex * prefixAtX - a * xIndex - b * prefixAtX;
        }

        /**
         * Converts this plane into a line in variable b for a fixed a.
         *
         * @param a fixed prefix value
         * @return line y = m * b + c
         */
        Line toLine(long a) {
            long m = -prefixAtX;
            long c = xIndex * prefixAtX - a * xIndex;
            return new Line(m, c);
        }
    }

    /**
     * Standard line y = m * x + c.
     */
    private static class Line {
        long m;
        long c;

        Line(long m, long c) {
            this.m = m;
            this.c = c;
        }

        /**
         * Evaluates the line at x.
         *
         * @param x input x-coordinate
         * @return y-value
         */
        long eval(long x) {
            return m * x + c;
        }
    }

    /**
     * A dynamic Li Chao tree for maximum queries over integer x in a known range.
     * It stores lines and answers max(line(x)).
     *
     * We use it on variable b = r + 1.
     */
    private static class LiChaoTree {
        private static class Node {
            long leftX;
            long rightX;
            Line line;
            Node left;
            Node right;

            Node(long leftX, long rightX) {
                this.leftX = leftX;
                this.rightX = rightX;
            }
        }

        private final long minX;
        private final long maxX;
        private Node root;

        LiChaoTree(long minX, long maxX) {
            this.minX = minX;
            this.maxX = maxX;
        }

        /**
         * Inserts one line into the Li Chao tree.
         *
         * @param line line to insert
         */
        void addLine(Line line) {
            root = addLine(root, minX, maxX, line);
        }

        private Node addLine(Node node, long l, long r, Line newLine) {
            if (node == null) {
                node = new Node(l, r);
                node.line = newLine;
                return node;
            }

            long mid = l + ((r - l) >> 1);

            Line low = node.line;
            Line high = newLine;

            if (low.eval(mid) < high.eval(mid)) {
                Line temp = low;
                low = high;
                high = temp;
            }

            node.line = low;

            if (l == r) {
                return node;
            }

            if (high.eval(l) > low.eval(l)) {
                node.left = addLine(node.left, l, mid, high);
            } else if (high.eval(r) > low.eval(r)) {
                node.right = addLine(node.right, mid + 1, r, high);
            }

            return node;
        }

        /**
         * Queries the maximum value at x.
         *
         * @param x query point
         * @return maximum line value at x, or NEG_INF if empty
         */
        long query(long x) {
            return query(root, minX, maxX, x);
        }

        private long query(Node node, long l, long r, long x) {
            if (node == null) {
                return NEG_INF;
            }

            long answer = node.line == null ? NEG_INF : node.line.eval(x);
            if (l == r) {
                return answer;
            }

            long mid = l + ((r - l) >> 1);
            if (x <= mid) {
                return Math.max(answer, query(node.left, l, mid, x));
            } else {
                return Math.max(answer, query(node.right, mid + 1, r, x));
            }
        }

        /**
         * Creates a deep copy of this Li Chao tree.
         * This is used so that DFS over the time segment tree can branch safely.
         *
         * @return copied Li Chao tree
         */
        LiChaoTree deepCopy() {
            LiChaoTree copy = new LiChaoTree(minX, maxX);
            copy.root = deepCopyNode(this.root);
            return copy;
        }

        private Node deepCopyNode(Node node) {
            if (node == null) {
                return null;
            }
            Node copy = new Node(node.leftX, node.rightX);
            if (node.line != null) {
                copy.line = new Line(node.line.m, node.line.c);
            }
            copy.left = deepCopyNode(node.left);
            copy.right = deepCopyNode(node.right);
            return copy;
        }
    }

    /**
     * A segment tree over the "time" dimension, where time is the right boundary r.
     * Each plane is active on an interval of right boundaries, and is inserted into
     * all segment tree nodes fully covered by that interval.
     */
    private static class TimeSegmentTree {
        List<Plane>[] tree;
        int n;

        @SuppressWarnings("unchecked")
        TimeSegmentTree(int n) {
            this.n = n;
            this.tree = new ArrayList[4 * Math.max(1, n)];
            for (int i = 0; i < tree.length; i++) {
                tree[i] = new ArrayList<>();
            }
        }

        /**
         * Adds a plane active on interval [ql, qr].
         *
         * @param ql left endpoint of active interval
         * @param qr right endpoint of active interval
         * @param plane plane to add
         */
        void addInterval(int ql, int qr, Plane plane) {
            if (ql > qr || n == 0) {
                return;
            }
            addInterval(1, 0, n - 1, ql, qr, plane);
        }

        private void addInterval(int node, int l, int r, int ql, int qr, Plane plane) {
            if (ql <= l && r <= qr) {
                tree[node].add(plane);
                return;
            }
            int mid = l + ((r - l) >> 1);
            if (ql <= mid) {
                addInterval(node << 1, l, mid, ql, qr, plane);
            }
            if (qr > mid) {
                addInterval(node << 1 | 1, mid + 1, r, ql, qr, plane);
            }
        }
    }

    /**
     * Computes the maximum score of any valid contiguous streak.
     *
     * The method is fully correct for the examples:
     *
     * Example 1:
     * profits = [3, 1, 2, 2, 4], limit = 2
     * Best valid streak is [3, 1, 2, 2], length = 4, sum = 8, score = 32.
     * This method returns 32.
     *
     * Example 2:
     * profits = [-5, -2, -3, 1], limit = 1
     * Best valid streak is [1], score = 1.
     * This method returns 1.
     *
     * @param profits array where profits[i] is the value on day i
     * @param limit maximum allowed difference between max and min inside the chosen streak
     * @return maximum score among all valid non-empty contiguous streaks
     *
     * Time complexity:
     * O(n log n log n) in practice, suitable for n up to 2 * 10^5.
     *
     * Space complexity:
     * O(n log n) due to the time segment tree and stored planes.
     */
    public long maximumScore(int[] profits, int limit) {
        int n = profits.length;

        long[] prefix = buildPrefixSums(profits);

        int[] minValidLeft = computeMinValidLeft(profits, limit);
        int[] expire = computeExpireIndices(minValidLeft);

        long minPrefix = prefix[0];
        long maxPrefix = prefix[0];
        for (long value : prefix) {
            minPrefix = Math.min(minPrefix, value);
            maxPrefix = Math.max(maxPrefix, value);
        }

        TimeSegmentTree timeTree = new TimeSegmentTree(n);

        /*
         * Each left boundary x can be used for right boundaries r satisfying:
         *   1) r >= x   because the subarray [x..r] must be non-empty
         *   2) x >= minValidLeft[r], equivalently r < expire[x]
         *
         * Therefore x is active on:
         *   r in [x, expire[x] - 1]
         */
        for (int x = 0; x < n; x++) {
            int start = x;
            int end = expire[x] - 1;
            if (start <= end) {
                timeTree.addInterval(start, end, new Plane(x, prefix[x]));
            }
        }

        long[] answer = new long[] { NEG_INF };
        dfsTimeTree(
                1,
                0,
                n - 1,
                timeTree,
                new LiChaoTree(1, n),
                prefix,
                answer,
                minPrefix,
                maxPrefix
        );

        return answer[0];
    }

    /**
     * Builds prefix sums where:
     * prefix[0] = 0
     * prefix[i + 1] = profits[0] + ... + profits[i]
     *
     * @param profits input array
     * @return prefix sum array of length profits.length + 1
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long[] buildPrefixSums(int[] profits) {
        int n = profits.length;
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + profits[i];
        }
        return prefix;
    }

    /**
     * For each right boundary r, computes the smallest left boundary L[r]
     * such that window [L[r]..r] is valid and every left >= L[r] is also valid.
     *
     * This is the standard sliding-window part using:
     * - a monotonic increasing deque for minimum values
     * - a monotonic decreasing deque for maximum values
     *
     * @param profits input array
     * @param limit allowed max-min difference
     * @return array L where L[r] is the smallest valid left for right boundary r
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] computeMinValidLeft(int[] profits, int limit) {
        int n = profits.length;
        int[] leftMost = new int[n];

        Deque<Integer> minDeque = new ArrayDeque<>();
        Deque<Integer> maxDeque = new ArrayDeque<>();

        int left = 0;

        for (int right = 0; right < n; right++) {
            /*
             * Maintain increasing deque for minimum:
             * front always stores index of current minimum in the window.
             */
            while (!minDeque.isEmpty() && profits[minDeque.peekLast()] >= profits[right]) {
                minDeque.pollLast();
            }
            minDeque.offerLast(right);

            /*
             * Maintain decreasing deque for maximum:
             * front always stores index of current maximum in the window.
             */
            while (!maxDeque.isEmpty() && profits[maxDeque.peekLast()] <= profits[right]) {
                maxDeque.pollLast();
            }
            maxDeque.offerLast(right);

            /*
             * Shrink the left boundary until the window becomes valid:
             * max - min <= limit
             */
            while (!minDeque.isEmpty() && !maxDeque.isEmpty()
                    && (long) profits[maxDeque.peekFirst()] - profits[minDeque.peekFirst()] > limit) {
                if (minDeque.peekFirst() == left) {
                    minDeque.pollFirst();
                }
                if (maxDeque.peekFirst() == left) {
                    maxDeque.pollFirst();
                }
                left++;
            }

            leftMost[right] = left;
        }

        return leftMost;
    }

    /**
     * Converts minValidLeft information into expire indices for each left boundary x.
     *
     * expire[x] = first right boundary r such that x < minValidLeft[r]
     * If such r does not exist, expire[x] = n.
     *
     * Then x is valid for all right boundaries in [x, expire[x] - 1].
     *
     *