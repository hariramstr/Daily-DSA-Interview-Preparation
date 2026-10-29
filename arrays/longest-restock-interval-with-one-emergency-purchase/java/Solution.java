import java.util.*;

/*
 * Title: Longest Restock Interval with One Emergency Purchase
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * A store records its daily inventory change for a single product in an integer array `changes`,
 * where a positive value means items were added and a negative value means items were sold.
 * The manager wants to study the longest contiguous time interval that can be treated as a
 * non-negative inventory trend after using at most one emergency purchase.
 *
 * For any chosen subarray, define its running balance as the prefix sums of that subarray.
 * The interval is considered stable if, after optionally adding one emergency purchase of any
 * size on exactly one day inside the interval, every running balance in that interval becomes
 * at least 0. In other words, you may pick at most one index in the subarray and increase that
 * day's value by any non-negative amount, and after that adjustment, no prefix sum of the
 * subarray may be negative.
 *
 * Return the maximum possible length of a stable contiguous interval.
 *
 * Notes:
 * - The emergency purchase can be used once or not used at all.
 * - The purchase amount is unbounded, but it affects only the chosen day.
 * - The chosen interval must remain contiguous.
 *
 * Constraints:
 * - 1 <= changes.length <= 2 * 10^5
 * - -10^9 <= changes[i] <= 10^9
 *
 * Example 1:
 * Input: changes = [4, -6, 3, 2]
 * Output: 4
 * Explanation:
 * Choose the whole array. Without help, the running balances are [4, -2, 1, 3].
 * Use the emergency purchase on day 2 (the value -6) and add 2.
 * The adjusted array becomes [4, -4, 3, 2], and the running balances are [4, 0, 3, 5],
 * so all prefixes are non-negative.
 *
 * Example 2:
 * Input: changes = [-3, 5, -2, 1, -4]
 * Output: 4
 * Explanation:
 * The whole array cannot be fixed with one emergency purchase because the first prefix is already
 * negative and any later purchase cannot repair it retroactively. One optimal interval is
 * [5, -2, 1, -4]. Its running balances are [5, 3, 4, 0], so it is already stable and has length 4.
 */

public class Solution {

    /**
     * Computes the maximum length of a contiguous interval that can be made stable
     * using at most one emergency purchase.
     *
     * Core observation:
     * For a subarray [l..r], let its internal prefix sums be:
     *   changes[l], changes[l] + changes[l+1], ..., changes[l] + ... + changes[r]
     *
     * We may increase exactly one element inside the subarray by any non-negative amount.
     * If we choose position k, then only prefixes ending at k or later are increased.
     * Therefore:
     * - Any negative prefix before k cannot be repaired.
     * - All prefixes from k onward can be lifted by a sufficiently large amount.
     *
     * So a subarray is fixable iff among its internal prefix sums, all negative values
     * (if any) start from some point onward. Equivalently, the sequence of internal prefix sums
     * has at most one transition from "non-negative" to "negative".
     *
     * Rewriting with global prefix sums P:
     * Internal prefix sums of subarray [l..r] are:
     *   P[l+1] - P[l], P[l+2] - P[l], ..., P[r+1] - P[l]
     *
     * Thus we need the sequence P[l+1..r+1] relative to baseline P[l].
     * The subarray is valid iff in P[l+1..r+1], all values < P[l] appear only after all values >= P[l].
     * In other words, there must not exist indices i < j in [l+1..r+1] such that:
     *   P[i] < P[l] and P[j] >= P[l]
     *
     * That means once we go below the baseline P[l], we are not allowed to come back to
     * the baseline or above.
     *
     * For each starting position l, define:
     * - firstBad = first index t > l such that P[t] < P[l]
     * - nextRecover = first index u > firstBad such that P[u] >= P[l]
     *
     * Then the longest valid subarray starting at l ends at:
     * - n - 1, if there is no recovery
     * - (nextRecover - 1) - 1 = nextRecover - 2, if recovery exists
     *
     * So its length is:
     * - n - l, if no recovery
     * - nextRecover - l - 1, if recovery exists
     *
     * We answer this efficiently by:
     * 1) Building prefix sums P[0..n]
     * 2) Coordinate-compressing P values
     * 3) Scanning from right to left while maintaining, for every prefix value rank:
     *    - the nearest future index with value < current baseline
     *    - the nearest future index with value >= current baseline
     *    via segment trees for range minimum queries
     *
     * @param changes the daily inventory changes
     * @return the maximum stable interval length
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int longestStableInterval(int[] changes) {
        int n = changes.length;

        // ------------------------------------------------------------
        // Step 1: Build global prefix sums.
        //
        // P[0] = 0
        // P[i] = changes[0] + changes[1] + ... + changes[i-1]
        //
        // Then for a subarray [l..r], its internal prefix sums are:
        // P[l+1]-P[l], P[l+2]-P[l], ..., P[r+1]-P[l]
        // ------------------------------------------------------------
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + changes[i];
        }

        // ------------------------------------------------------------
        // Step 2: Coordinate compression of prefix sums.
        //
        // We need to compare prefix values with < and >= very often.
        // Compression maps each distinct prefix sum to a rank in [0..m-1].
        // This lets us query ranges of values using segment trees.
        // ------------------------------------------------------------
        long[] sorted = prefix.clone();
        Arrays.sort(sorted);

        int m = 0;
        long[] unique = new long[n + 1];
        for (long value : sorted) {
            if (m == 0 || unique[m - 1] != value) {
                unique[m++] = value;
            }
        }

        int[] rank = new int[n + 1];
        for (int i = 0; i <= n; i++) {
            rank[i] = lowerBound(unique, m, prefix[i]);
        }

        // ------------------------------------------------------------
        // Step 3: Prepare two segment trees that store minimum index.
        //
        // We scan indices t from right to left.
        // At any moment, trees contain indices strictly greater than current l.
        //
        // lessTree:
        //   For each prefix value rank, stores the nearest future index having that exact rank.
        //   Query over ranks [0 .. rank[l)-1] gives the first future index with P[t] < P[l].
        //
        // geTree:
        //   Same storage idea, but query over ranks [rank[l] .. m-1] gives the first future
        //   index with P[t] >= P[l].
        //
        // Why two trees if both store the same point updates?
        // Because conceptually we use them for two different range conditions.
        // Keeping both makes the logic very explicit and beginner-friendly.
        // ------------------------------------------------------------
        SegmentTreeMin lessTree = new SegmentTreeMin(m);
        SegmentTreeMin geTree = new SegmentTreeMin(m);

        int answer = 1;

        // ------------------------------------------------------------
        // Initially, when l = n-1, future prefix indices are only {n}.
        // More generally, before processing l, trees must contain indices in [l+1 .. n].
        //
        // We achieve this by scanning l from n-1 down to 0 and inserting prefix index l+1
        // before evaluating start position l.
        // ------------------------------------------------------------
        for (int l = n - 1; l >= 0; l--) {
            int futurePrefixIndex = l + 1;
            int futureRank = rank[futurePrefixIndex];

            lessTree.update(futureRank, futurePrefixIndex);
            geTree.update(futureRank, futurePrefixIndex);

            int currentRank = rank[l];

            // --------------------------------------------------------
            // firstBelow:
            // earliest future prefix index t in [l+1..n] with P[t] < P[l]
            //
            // If none exists, then every internal prefix sum is already >= 0,
            // so the whole suffix [l..n-1] is valid.
            // --------------------------------------------------------
            int firstBelow = lessTree.query(0, currentRank - 1);

            if (firstBelow == SegmentTreeMin.INF) {
                answer = Math.max(answer, n - l);
                continue;
            }

            // --------------------------------------------------------
            // nextRecover:
            // earliest future prefix index u after firstBelow with P[u] >= P[l]
            //
            // We need "after firstBelow", not just any >= baseline.
            // The segment tree gives the earliest >= baseline among all future indices,
            // but that might be before firstBelow.
            //
            // So we need a way to find the first >= baseline strictly after firstBelow.
            // To do this correctly, we process with per-rank ordered index lists.
            // --------------------------------------------------------
            int nextRecover = firstIndexAtLeast(prefix, rank, unique, m, l, firstBelow);

            if (nextRecover == -1) {
                answer = Math.max(answer, n - l);
            } else {
                answer = Math.max(answer, nextRecover - l - 1);
            }
        }

        return answer;
    }

    /**
     * Finds the first future prefix index u > firstBelow such that prefix[u] >= prefix[l].
     *
     * This helper performs a direct scan over prefix indices after firstBelow.
     * Although simple, using it naively would be too slow in the worst case.
     * Therefore this method is not used as a linear scan; instead, it delegates to
     * a preprocessed structure built on demand in the overloaded version below.
     *
     * @param prefix the global prefix sums
     * @param rank compressed ranks of prefix sums
     * @param unique sorted unique prefix values
     * @param m number of unique prefix values
     * @param l start index of the subarray
     * @param firstBelow first future prefix index where prefix goes below the baseline
     * @return the first index u > firstBelow with prefix[u] >= prefix[l], or -1 if none exists
     * Time complexity: O(log^2 n) amortized through preprocessing in the actual implementation
     * Space complexity: O(n)
     */
    public int firstIndexAtLeast(long[] prefix, int[] rank, long[] unique, int m, int l, int firstBelow) {
        return recoveryFinder.find(prefix, rank, unique, m, l, firstBelow);
    }

    /**
     * Lower bound: first position i in unique[0..size) such that unique[i] >= target.
     *
     * @param unique sorted array of unique values
     * @param size number of valid elements in unique
     * @param target value to search
     * @return compressed rank of target
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public int lowerBound(long[] unique, int size, long target) {
        int left = 0;
        int right = size;
        while (left < right) {
            int mid = left + (right - left) / 2;
            if (unique[mid] < target) {
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
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding algorithm calls
     * Space complexity: O(1) excluding algorithm internals
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] changes1 = {4, -6, 3, 2};
        int[] changes2 = {-3, 5, -2, 1, -4};

        System.out.println(solution.longestStableInterval(changes1)); // Expected: 4
        System.out.println(solution.longestStableInterval(changes2)); // Expected: 4
    }

    /**
     * Segment tree supporting point update with minimum and range minimum query.
     *
     * We store, for each compressed prefix value rank, the smallest index currently present.
     * Since we scan from right to left and insert each prefix index exactly once, the stored
     * value for a rank is simply the nearest future occurrence of that rank.
     */
    static class SegmentTreeMin {
        static final int INF = 1_000_000_000;

        private final int size;
        private final int[] tree;

        /**
         * Creates a segment tree for indices [0..n-1].
         *
         * @param n number of positions
         */
        SegmentTreeMin(int n) {
            int s = 1;
            while (s < n) {
                s <<= 1;
            }
            this.size = s;
            this.tree = new int[size << 1];
            Arrays.fill(tree, INF);
        }

        /**
         * Updates one position with the minimum of current value and the provided value.
         *
         * @param pos position to update
         * @param value candidate minimum value
         */
        void update(int pos, int value) {
            int index = pos + size;
            tree[index] = Math.min(tree[index], value);
            index >>= 1;
            while (index > 0) {
                tree[index] = Math.min(tree[index << 1], tree[(index << 1) | 1]);
                index >>= 1;
            }
        }

        /**
         * Returns the minimum value in the inclusive range [left, right].
         *
         * @param left left boundary
         * @param right right boundary
         * @return minimum stored value, or INF if the range is empty / has no data
         */
        int query(int left, int right) {
            if (left > right) {
                return INF;
            }

            left += size;
            right += size;
            int result = INF;

            while (left <= right) {
                if ((left & 1) == 1) {
                    result = Math.min(result, tree[left++]);
                }
                if ((right & 1) == 0) {
                    result = Math.min(result, tree[right--]);
                }
                left >>= 1;
                right >>= 1;
            }

            return result;
        }
    }

    /**
     * Helper structure to answer:
     * "After position x, what is the first prefix index whose value is at least a threshold?"
     *
     * We solve this offline with a merge-sort tree over prefix indices.
     * Each node stores the sorted prefix values in its segment.
     * Query:
     *   find first index in [from..n] with prefix[index] >= threshold
     *
     * This is used to find the first recovery after the first dip below baseline.
     */
    private final RecoveryFinder recoveryFinder = new RecoveryFinder();

    /**
     * Internal helper that lazily builds a merge-sort tree the first time it is needed.
     */
    static class RecoveryFinder {
        private boolean built = false;
        private long[] prefix;
        private int n;
        private long[][] tree;

        /**
         * Finds the first index u > firstBelow such that prefix[u] >= prefix[l].
         *
         * @param prefix global prefix sums
         * @param rank compressed ranks (unused directly here but kept for signature clarity)
         * @param unique unique sorted values (unused directly here but kept for signature clarity)
         * @param m number of unique values (unused directly here but kept for signature clarity)
         * @param l start index
         * @param firstBelow first index where prefix dips below baseline
         * @return first recovery index, or -1 if none exists
         */
        int find(long[] prefix, int[] rank, long[] unique, int m, int l, int firstBelow) {
            if (!built || this.prefix != prefix) {
                build(prefix);
            }
            long threshold = prefix[l];
            int from = firstBelow + 1;
            if (from > n) {
                return -1;
            }
            return firstIndexWithAtLeast(1, 0, n, from, n, threshold);
        }

        /**
         * Builds the merge-sort tree over prefix indices [0..n].
         *
         * @param prefix global prefix sums
         */
        private void build(long[] prefix) {
            this.prefix = prefix;
            this.n = prefix.length - 1;
            this.tree = new long[(n + 1) << 2][];
            buildNode(1, 0, n);
            this.built = true;
        }

        /**
         * Recursively builds one node.
         *
         * @param node current node id
         * @param left left boundary
         * @param right right boundary
         */
        private void buildNode(int node, int left, int right) {
            if (left == right) {
                tree[node] = new long[]{prefix[left]};
                return;
            }

            int mid = left + (right - left) / 2;
            buildNode(node << 1, left, mid);
            buildNode((node << 1) | 1, mid + 1, right);

            long[] a = tree[node << 1];
            long[] b = tree[(node << 1) | 1];
            long[] merged = new long[a.length + b.length];

            int i = 0;
            int j = 0;
            int k = 0;

            while (i < a.length && j < b.length) {
                if (a[i] <= b[j]) {
                    merged[k++] = a[i++];
                } else {
                    merged[k++] = b[j++];
                }
            }
            while (i < a.length) {
                merged[k++] = a[i++];
            }
            while (j < b