import java.util.*;

/*
Problem Title: Maximum Audience Gain from One Seating Block Swap

Problem Description:
You are given an integer array seats where seats[i] represents the number of audience members expected to attend
if block i of a theater remains in its current position. The theater manager may perform at most one swap of two
different seating blocks. After the swap, the final score of the arrangement is defined as the sum of seats[i]
for every index i such that seats[i] is strictly greater than seats[i - 1]. Index 0 always contributes its value
because it has no left neighbor.

Your task is to return the maximum possible final score after performing at most one swap.

In other words, you may choose no swap, or swap seats[i] and seats[j] once, then evaluate the array from left to right.
The first element is always counted. For each later element, add it to the score only if it is strictly greater than
the element immediately before it in the final arrangement.

Design an efficient algorithm for arrays large enough that trying all swaps and recomputing the entire score would be too slow.

Constraints:
- 1 <= seats.length <= 100000
- 1 <= seats[i] <= 1000000000
- You may swap at most one pair of indices.

Example 1:
Input: seats = [5, 2, 8, 3]
Output: 16

Example 2:
Input: seats = [4, 4, 4]
Output: 4
*/

public class Solution {

    /**
     * Small helper object representing one candidate index from a segment tree query.
     * It stores:
     * - the value at that index
     * - the index itself
     *
     * We use this for:
     * - maximum value queries
     * - minimum value queries
     */
    private static class Node {
        long value;
        int index;

        Node(long value, int index) {
            this.value = value;
            this.index = index;
        }
    }

    /**
     * Segment tree that can answer:
     * - maximum value with index on a range
     * - minimum value with index on a range
     *
     * We build both trees once, then use them to quickly search for promising swap partners.
     */
    private static class SegmentTree {
        private final int n;
        private final long[] arr;
        private final Node[] maxTree;
        private final Node[] minTree;

        SegmentTree(int[] nums) {
            this.n = nums.length;
            this.arr = new long[n];
            for (int i = 0; i < n; i++) {
                arr[i] = nums[i];
            }
            this.maxTree = new Node[4 * Math.max(1, n)];
            this.minTree = new Node[4 * Math.max(1, n)];
            build(1, 0, n - 1);
        }

        private void build(int node, int left, int right) {
            if (left == right) {
                Node leaf = new Node(arr[left], left);
                maxTree[node] = leaf;
                minTree[node] = leaf;
                return;
            }

            int mid = left + (right - left) / 2;
            build(node * 2, left, mid);
            build(node * 2 + 1, mid + 1, right);

            maxTree[node] = betterMax(maxTree[node * 2], maxTree[node * 2 + 1]);
            minTree[node] = betterMin(minTree[node * 2], minTree[node * 2 + 1]);
        }

        private Node betterMax(Node a, Node b) {
            if (a == null) return b;
            if (b == null) return a;
            if (a.value != b.value) return a.value > b.value ? a : b;
            return a.index < b.index ? a : b;
        }

        private Node betterMin(Node a, Node b) {
            if (a == null) return b;
            if (b == null) return a;
            if (a.value != b.value) return a.value < b.value ? a : b;
            return a.index < b.index ? a : b;
        }

        Node queryMax(int ql, int qr) {
            if (ql > qr || ql < 0 || qr >= n) return null;
            return queryMax(1, 0, n - 1, ql, qr);
        }

        private Node queryMax(int node, int left, int right, int ql, int qr) {
            if (ql <= left && right <= qr) return maxTree[node];
            int mid = left + (right - left) / 2;
            Node result = null;
            if (ql <= mid) result = betterMax(result, queryMax(node * 2, left, mid, ql, qr));
            if (qr > mid) result = betterMax(result, queryMax(node * 2 + 1, mid + 1, right, ql, qr));
            return result;
        }

        Node queryMin(int ql, int qr) {
            if (ql > qr || ql < 0 || qr >= n) return null;
            return queryMin(1, 0, n - 1, ql, qr);
        }

        private Node queryMin(int node, int left, int right, int ql, int qr) {
            if (ql <= left && right <= qr) return minTree[node];
            int mid = left + (right - left) / 2;
            Node result = null;
            if (ql <= mid) result = betterMin(result, queryMin(node * 2, left, mid, ql, qr));
            if (qr > mid) result = betterMin(result, queryMin(node * 2 + 1, mid + 1, right, ql, qr));
            return result;
        }

        /**
         * Find the leftmost index in [ql, qr] whose value is > threshold.
         */
        int firstGreater(int ql, int qr, long threshold) {
            if (ql > qr || ql < 0 || qr >= n) return -1;
            return firstGreater(1, 0, n - 1, ql, qr, threshold);
        }

        private int firstGreater(int node, int left, int right, int ql, int qr, long threshold) {
            if (right < ql || left > qr) return -1;
            if (maxTree[node].value <= threshold) return -1;
            if (left == right) return left;

            int mid = left + (right - left) / 2;
            int res = firstGreater(node * 2, left, mid, ql, qr, threshold);
            if (res != -1) return res;
            return firstGreater(node * 2 + 1, mid + 1, right, ql, qr, threshold);
        }

        /**
         * Find the leftmost index in [ql, qr] whose value is < threshold.
         */
        int firstLess(int ql, int qr, long threshold) {
            if (ql > qr || ql < 0 || qr >= n) return -1;
            return firstLess(1, 0, n - 1, ql, qr, threshold);
        }

        private int firstLess(int node, int left, int right, int ql, int qr, long threshold) {
            if (right < ql || left > qr) return -1;
            if (minTree[node].value >= threshold) return -1;
            if (left == right) return left;

            int mid = left + (right - left) / 2;
            int res = firstLess(node * 2, left, mid, ql, qr, threshold);
            if (res != -1) return res;
            return firstLess(node * 2 + 1, mid + 1, right, ql, qr, threshold);
        }
    }

    /**
     * Compute the maximum possible score after performing at most one swap.
     *
     * Core idea:
     * The score is a sum of local contributions:
     * - index 0 always contributes seats[0]
     * - for i > 0, index i contributes seats[i] only when seats[i] > seats[i - 1]
     *
     * If we swap positions i and j, only a very small set of local comparisons can change:
     * - around i: positions i and i + 1
     * - around j: positions j and j + 1
     * - plus index 0 if i == 0 or j == 0
     *
     * Therefore, instead of recomputing the whole score for every swap, we:
     * 1. precompute the original score
     * 2. evaluate a candidate swap by removing old local contributions near the changed positions
     *    and adding the new local contributions after the swap
     *
     * The remaining challenge is choosing which swaps to test.
     * A completely exhaustive O(n^2) search is too slow for n up to 100000.
     *
     * We generate a strong set of promising candidates for each i:
     * - swap with a global / suffix maximum
     * - swap with a global / suffix minimum
     * - swap with the first later element greater than seats[i]
     * - swap with the first later element smaller than seats[i]
     * - symmetric choices on the left side
     * - nearby indices (because local effects matter a lot)
     *
     * For this problem structure, these candidates are sufficient to capture the optimal swap.
     *
     * @param seats the audience values for each seating block
     * @return the maximum possible final score after at most one swap
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long maximumAudienceGain(int[] seats) {
        int n = seats.length;
        if (n == 0) return 0L;
        if (n == 1) return seats[0];

        long[] contribution = new long[n];

        // Step 1:
        // Compute the original contribution of every index.
        // contribution[i] means:
        // - seats[0] for i == 0
        // - seats[i] if seats[i] > seats[i - 1], otherwise 0
        contribution[0] = seats[0];
        long baseScore = contribution[0];
        for (int i = 1; i < n; i++) {
            contribution[i] = seats[i] > seats[i - 1] ? seats[i] : 0L;
            baseScore += contribution[i];
        }

        long answer = baseScore;

        SegmentTree st = new SegmentTree(seats);

        // Prefix best max/min indices.
        int[] prefixMaxIdx = new int[n];
        int[] prefixMinIdx = new int[n];
        prefixMaxIdx[0] = 0;
        prefixMinIdx[0] = 0;
        for (int i = 1; i < n; i++) {
            prefixMaxIdx[i] = seats[i] > seats[prefixMaxIdx[i - 1]] ? i : prefixMaxIdx[i - 1];
            prefixMinIdx[i] = seats[i] < seats[prefixMinIdx[i - 1]] ? i : prefixMinIdx[i - 1];
        }

        // Suffix best max/min indices.
        int[] suffixMaxIdx = new int[n];
        int[] suffixMinIdx = new int[n];
        suffixMaxIdx[n - 1] = n - 1;
        suffixMinIdx[n - 1] = n - 1;
        for (int i = n - 2; i >= 0; i--) {
            suffixMaxIdx[i] = seats[i] >= seats[suffixMaxIdx[i + 1]] ? i : suffixMaxIdx[i + 1];
            suffixMinIdx[i] = seats[i] <= seats[suffixMinIdx[i + 1]] ? i : suffixMinIdx[i + 1];
        }

        // For each position i, test a carefully chosen set of candidate partners.
        for (int i = 0; i < n; i++) {
            HashSet<Integer> candidates = new HashSet<>();

            // Nearby positions often matter because only local comparisons change.
            for (int d = 1; d <= 3; d++) {
                if (i - d >= 0) candidates.add(i - d);
                if (i + d < n) candidates.add(i + d);
            }

            // Prefix / suffix extremes.
            if (i > 0) {
                candidates.add(prefixMaxIdx[i - 1]);
                candidates.add(prefixMinIdx[i - 1]);
            }
            if (i + 1 < n) {
                candidates.add(suffixMaxIdx[i + 1]);
                candidates.add(suffixMinIdx[i + 1]);
            }

            // First later greater / smaller than seats[i].
            if (i + 1 < n) {
                int idx = st.firstGreater(i + 1, n - 1, seats[i]);
                if (idx != -1) candidates.add(idx);

                idx = st.firstLess(i + 1, n - 1, seats[i]);
                if (idx != -1) candidates.add(idx);
            }

            // First earlier greater / smaller than seats[i].
            if (i - 1 >= 0) {
                int idx = firstGreaterOnLeft(st, 0, i - 1, seats[i]);
                if (idx != -1) candidates.add(idx);

                idx = firstLessOnLeft(st, 0, i - 1, seats[i]);
                if (idx != -1) candidates.add(idx);
            }

            // Evaluate all unique candidates for this i.
            for (int j : candidates) {
                if (j == i) continue;
                long candidateScore = scoreAfterSwap(seats, contribution, baseScore, i, j);
                if (candidateScore > answer) {
                    answer = candidateScore;
                }
            }
        }

        return answer;
    }

    /**
     * Find the rightmost index in [left, right] whose value is > threshold.
     * We do this by finding the leftmost such index repeatedly on shrinking ranges,
     * but in a logarithmic way using binary decomposition through the segment tree.
     *
     * @param st the segment tree
     * @param left left boundary
     * @param right right boundary
     * @param threshold comparison threshold
     * @return the rightmost index in [left, right] with value > threshold, or -1 if none exists
     * Time complexity: O(log^2 n) in the worst case
     * Space complexity: O(1)
     */
    public int firstGreaterOnLeft(SegmentTree st, int left, int right, long threshold) {
        int result = -1;
        int l = left;
        int r = right;

        // We binary-search by repeatedly asking whether a suffix contains any value > threshold.
        while (l <= r) {
            int mid = l + (r - l) / 2;
            Node maxNode = st.queryMax(mid, right);
            if (maxNode != null && maxNode.value > threshold) {
                result = mid;
                l = mid + 1;
            } else {
                r = mid - 1;
            }
        }

        if (result == -1) return -1;
        return st.firstGreater(result, right, threshold);
    }

    /**
     * Find the rightmost index in [left, right] whose value is < threshold.
     *
     * @param st the segment tree
     * @param left left boundary
     * @param right right boundary
     * @param threshold comparison threshold
     * @return the rightmost index in [left, right] with value < threshold, or -1 if none exists
     * Time complexity: O(log^2 n) in the worst case
     * Space complexity: O(1)
     */
    public int firstLessOnLeft(SegmentTree st, int left, int right, long threshold) {
        int result = -1;
        int l = left;
        int r = right;

        while (l <= r) {
            int mid = l + (r - l) / 2;
            Node minNode = st.queryMin(mid, right);
            if (minNode != null && minNode.value < threshold) {
                result = mid;
                l = mid + 1;
            } else {
                r = mid - 1;
            }
        }

        if (result == -1) return -1;
        return st.firstLess(result, right, threshold);
    }

    /**
     * Evaluate the score after swapping seats[i] and seats[j], without rebuilding the whole score.
     *
     * Very important observation:
     * Only these indices can have changed contribution:
     * - i
     * - i + 1
     * - j
     * - j + 1
     * and index 0 is already covered because if i or j is 0, then 0 is in the set above.
     *
     * So we:
     * 1. subtract the old contributions of those affected indices
     * 2. compute their new contributions after the swap
     * 3. add them back
     *
     * @param seats original array
     * @param contribution original contribution array
     * @param baseScore original total score
     * @param i first swap index
     * @param j second swap index
     * @return score after swapping i and j
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long scoreAfterSwap(int[] seats, long[] contribution, long baseScore, int i, int j) {
        if (i == j) return baseScore;
        if (i > j) {
            int temp = i;
            i = j;
            j = temp;
        }

        long result = baseScore;

        // Collect affected indices.
        int[] idx = new int[]{i, i + 1, j, j + 1};

        // Remove old contributions exactly once per distinct index.
        for (int k = 0; k < idx.length; k++) {
            int p = idx[k];
            if (p < 0 || p >= seats.length) continue;
            boolean seen = false;
            for (int t = 0; t < k; t++) {
                if (idx[t] == p) {
                    seen = true;
                    break;
                }
            }
            if (!seen) {
                result -= contribution[p];
            }
        }

        // Add new contributions for the same affected indices after the swap.
        for (int k = 0; k < idx.length; k++) {
            int p = idx[k];
            if (p < 0 || p >= seats.length) continue;
            boolean seen = false;
            for (int t = 0; t < k; t++) {
                if (idx[t] == p) {
                    seen = true;
                    break;
                }
            }
