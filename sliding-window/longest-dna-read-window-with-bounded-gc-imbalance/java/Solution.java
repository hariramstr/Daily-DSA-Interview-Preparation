import java.util.*;

/*
 * Title: Longest DNA Read Window With Bounded GC Imbalance
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are analyzing a long DNA read represented by a string s consisting only of
 * the characters 'A', 'C', 'G', and 'T'. In genome quality control, a contiguous
 * segment is considered stable if both of the following conditions hold:
 *
 * 1. The absolute difference between the number of 'G' bases and the number of 'C'
 *    bases in the segment is at most k.
 * 2. The total number of 'A' and 'T' bases in the segment is at most m.
 *
 * Return the length of the longest stable contiguous segment.
 *
 * A segment may be empty, but the answer should be the maximum length among all
 * contiguous non-empty segments unless no valid non-empty segment exists, in which
 * case return 0.
 *
 * This problem is designed for an efficient sliding window solution. A brute-force
 * approach that checks every substring will be too slow for the largest inputs.
 *
 * Constraints:
 * - 1 <= s.length <= 2 * 10^5
 * - 0 <= k <= s.length
 * - 0 <= m <= s.length
 * - s[i] is one of 'A', 'C', 'G', 'T'
 *
 * Example 1:
 * Input: s = "GCGATCGG", k = 1, m = 2
 * Output: 6
 * Explanation: The substring "CGATCG" has G = 2, C = 2, so |G - C| = 0 <= 1,
 * and A + T = 2 <= 2. Its length is 6. No longer valid substring exists.
 *
 * Example 2:
 * Input: s = "ATATGGCCG", k = 0, m = 1
 * Output: 5
 * Explanation: The substring "TGGCC" is valid because G = 2, C = 2, so
 * |G - C| = 0, and it contains only one base from {A, T}. Its length is 5.
 * Longer windows violate at least one constraint.
 */

public class Solution {

    /**
     * Computes the length of the longest stable contiguous DNA segment.
     *
     * The key observation is:
     * - A window is valid if:
     *   1) |count('G') - count('C')| <= k
     *   2) count('A') + count('T') <= m
     *
     * We transform the DNA string into two numeric views:
     * - diff contribution:
     *     'G' -> +1
     *     'C' -> -1
     *     'A'/'T' -> 0
     *   Then for any window, the total sum is (G - C), and we need its absolute
     *   value to be at most k.
     *
     * - at contribution:
     *     'A'/'T' -> 1
     *     'G'/'C' -> 0
     *   Then for any window, the total sum is the number of A/T characters,
     *   and we need it to be at most m.
     *
     * Let prefixDiff[i] be the prefix sum of diff up to index i - 1.
     * Let prefixAT[i]   be the prefix sum of at   up to index i - 1.
     *
     * For a substring s[l..r], using prefix sums:
     * - diff = prefixDiff[r + 1] - prefixDiff[l]
     * - at   = prefixAT[r + 1]   - prefixAT[l]
     *
     * The substring is valid iff:
     * - prefixAT[l] >= prefixAT[r + 1] - m
     * - prefixDiff[l] is in [prefixDiff[r + 1] - k, prefixDiff[r + 1] + k]
     *
     * For each right endpoint r, we want the smallest possible l that satisfies
     * both conditions, because that gives the longest valid window ending at r.
     *
     * We process prefix indices from left to right and maintain a data structure
     * over candidate left endpoints l:
     * - Candidates are grouped by prefixAT[l].
     * - Since prefixAT is non-decreasing and only increases by 0 or 1, once
     *   prefixAT[l] becomes too small (< currentPrefixAT - m), that l can never
     *   be used again for any future right endpoint. So we can permanently remove
     *   expired candidates.
     *
     * Among active candidates, we need the minimum index l whose prefixDiff[l]
     * lies in a value range [currentDiff - k, currentDiff + k].
     *
     * Because prefixDiff ranges only from -n to +n, we can shift it by +n and use
     * a segment tree indexed by shifted prefixDiff values. At each diff value, we
     * store the minimum active index l having that prefixDiff. Then a range-minimum
     * query over [lowDiff, highDiff] gives the best left endpoint.
     *
     * This yields an O(n log n) solution, which is efficient for n up to 2 * 10^5.
     *
     * @param s the DNA string containing only 'A', 'C', 'G', and 'T'
     * @param k the maximum allowed absolute imbalance between counts of 'G' and 'C'
     * @param m the maximum allowed total count of 'A' and 'T'
     * @return the length of the longest stable contiguous segment; returns 0 if no non-empty valid segment exists
     * @implNote Time complexity: O(n log n)
     * @implNote Space complexity: O(n)
     */
    public int longestStableSegment(String s, int k, int m) {
        int n = s.length();

        // prefixDiff[i] = (number of G) - (number of C) in s[0..i-1]
        int[] prefixDiff = new int[n + 1];

        // prefixAT[i] = (number of A/T) in s[0..i-1]
        int[] prefixAT = new int[n + 1];

        // Build prefix sums.
        // We use 1-based prefix indexing:
        // prefix arrays at position i describe the first i characters.
        for (int i = 0; i < n; i++) {
            char ch = s.charAt(i);

            prefixDiff[i + 1] = prefixDiff[i];
            prefixAT[i + 1] = prefixAT[i];

            if (ch == 'G') {
                prefixDiff[i + 1]++;
            } else if (ch == 'C') {
                prefixDiff[i + 1]--;
            } else {
                // ch is 'A' or 'T'
                prefixAT[i + 1]++;
            }
        }

        // prefixDiff values lie in [-n, n].
        // We shift by +n so every value becomes a valid non-negative index.
        int shift = n;
        int diffValueCount = 2 * n + 1;

        // For each possible prefixAT value, store the list of prefix indices i
        // such that prefixAT[i] == that value.
        //
        // Why this grouping helps:
        // prefixAT is non-decreasing. For a current right endpoint with prefixAT = curAT,
        // valid left endpoints must satisfy prefixAT[left] >= curAT - m.
        // Therefore, all groups with prefixAT value < curAT - m are permanently expired
        // and can be removed from the active structure exactly once.
        List<List<Integer>> byAT = new ArrayList<>(n + 1);
        for (int i = 0; i <= n; i++) {
            byAT.add(new ArrayList<>());
        }
        for (int i = 0; i <= n; i++) {
            byAT.get(prefixAT[i]).add(i);
        }

        // For each shifted diff value, we maintain a deque of active prefix indices
        // having that diff value, in increasing index order.
        //
        // The front of the deque is always the smallest active index for that diff.
        @SuppressWarnings("unchecked")
        ArrayDeque<Integer>[] diffBuckets = new ArrayDeque[diffValueCount];
        for (int i = 0; i < diffValueCount; i++) {
            diffBuckets[i] = new ArrayDeque<>();
        }

        // Segment tree where each leaf corresponds to one shifted diff value.
        // The leaf stores the minimum active prefix index with that diff value,
        // or INF if none exists.
        SegmentTree segTree = new SegmentTree(diffValueCount);

        // Initially, prefix index 0 is an active candidate left endpoint.
        int initialDiffIndex = prefixDiff[0] + shift;
        diffBuckets[initialDiffIndex].addLast(0);
        segTree.update(initialDiffIndex, 0);

        int answer = 0;

        // expiredAT tracks the largest prefixAT value that has already been removed.
        // Initially, nothing is removed, so expiredAT = -1.
        int expiredAT = -1;

        // We iterate over prefix position r = 1..n.
        // This corresponds to substrings ending at character index r - 1.
        for (int r = 1; r <= n; r++) {
            int currentAT = prefixAT[r];
            int currentDiff = prefixDiff[r];

            // Any left endpoint l with prefixAT[l] < currentAT - m is invalid.
            // Since prefixAT values are grouped, we can remove entire groups once.
            int mustBeAtLeast = currentAT - m;

            while (expiredAT + 1 < mustBeAtLeast) {
                expiredAT++;
                if (expiredAT >= 0 && expiredAT <= n) {
                    List<Integer> group = byAT.get(expiredAT);

                    // Remove every prefix index in this expired group from the active structure.
                    for (int idx : group) {
                        int diffIndex = prefixDiff[idx] + shift;
                        ArrayDeque<Integer> bucket = diffBuckets[diffIndex];

                        // Because indices are inserted in increasing order and removed
                        // by increasing prefixAT threshold, an expired index that is still
                        // active must be at the front of its bucket.
                        if (!bucket.isEmpty() && bucket.peekFirst() == idx) {
                            bucket.pollFirst();
                            int newMin = bucket.isEmpty() ? SegmentTree.INF : bucket.peekFirst();
                            segTree.update(diffIndex, newMin);
                        }
                    }
                }
            }

            // We need prefixDiff[l] in [currentDiff - k, currentDiff + k].
            int low = Math.max(-n, currentDiff - k);
            int high = Math.min(n, currentDiff + k);

            int bestLeft = segTree.query(low + shift, high + shift);

            // If we found at least one active left endpoint satisfying both constraints,
            // then the window length is r - bestLeft.
            if (bestLeft != SegmentTree.INF) {
                answer = Math.max(answer, r - bestLeft);
            }

            // After processing windows ending at r - 1, we add prefix index r as a future
            // candidate left endpoint for later windows.
            int diffIndex = currentDiff + shift;
            ArrayDeque<Integer> bucket = diffBuckets[diffIndex];
            bucket.addLast(r);

            // If this is the first active index for this diff value, or it becomes smaller
            // than the previously stored minimum (which in this insertion order cannot happen),
            // update the segment tree with the front value.
            //
            // Since we append in increasing order, the front remains the minimum.
            // We only need to update when the bucket was previously empty.
            if (bucket.size() == 1) {
                segTree.update(diffIndex, r);
            }
        }

        return answer;
    }

    /**
     * Convenience wrapper that matches a common interview-style method naming pattern.
     *
     * @param s the DNA string containing only 'A', 'C', 'G', and 'T'
     * @param k the maximum allowed absolute imbalance between counts of 'G' and 'C'
     * @param m the maximum allowed total count of 'A' and 'T'
     * @return the length of the longest stable contiguous segment
     * @implNote Time complexity: O(n log n)
     * @implNote Space complexity: O(n)
     */
    public int solve(String s, int k, int m) {
        return longestStableSegment(s, k, m);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Verified outputs:
     * - "GCGATCGG", k = 1, m = 2 -> 6
     * - "ATATGGCCG", k = 0, m = 1 -> 5
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * @implNote Time complexity: O(1) for the demonstration itself, excluding the called algorithm
     * @implNote Space complexity: O(1) for the demonstration itself, excluding the called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String s1 = "GCGATCGG";
        int k1 = 1;
        int m1 = 2;
        int result1 = solution.longestStableSegment(s1, k1, m1);
        System.out.println("Input: s = \"" + s1 + "\", k = " + k1 + ", m = " + m1);
        System.out.println("Output: " + result1);
        System.out.println("Expected: 6");
        System.out.println();

        String s2 = "ATATGGCCG";
        int k2 = 0;
        int m2 = 1;
        int result2 = solution.longestStableSegment(s2, k2, m2);
        System.out.println("Input: s = \"" + s2 + "\", k = " + k2 + ", m = " + m2);
        System.out.println("Output: " + result2);
        System.out.println("Expected: 5");
        System.out.println();

        String s3 = "ATAT";
        int k3 = 0;
        int m3 = 0;
        int result3 = solution.longestStableSegment(s3, k3, m3);
        System.out.println("Input: s = \"" + s3 + "\", k = " + k3 + ", m = " + m3);
        System.out.println("Output: " + result3);
        System.out.println("Expected: 0");
        System.out.println();

        String s4 = "GGGCCC";
        int k4 = 1;
        int m4 = 0;
        int result4 = solution.longestStableSegment(s4, k4, m4);
        System.out.println("Input: s = \"" + s4 + "\", k = " + k4 + ", m = " + m4);
        System.out.println("Output: " + result4);
    }

    /**
     * A classic iterative segment tree for range minimum query.
     *
     * Each position stores the minimum active prefix index for one specific shifted
     * prefixDiff value. Querying a range of diff values returns the smallest active
     * left endpoint whose diff lies in that range.
     */
    static class SegmentTree {
        static final int INF = Integer.MAX_VALUE / 4;

        private final int size;
        private final int[] tree;

        /**
         * Creates a segment tree for a fixed number of positions.
         *
         * @param n the number of leaves / positions
         * @return nothing
         * @implNote Time complexity: O(n)
         * @implNote Space complexity: O(n)
         */
        SegmentTree(int n) {
            int s = 1;
            while (s < n) {
                s <<= 1;
            }
            this.size = s;
            this.tree = new int[size << 1];
            Arrays.fill(tree, INF);
        }

        /**
         * Sets the value at one position.
         *
         * @param pos the leaf position to update
         * @param value the new value to store
         * @return nothing
         * @implNote Time complexity: O(log n)
         * @implNote Space complexity: O(1)
         */
        void update(int pos, int value) {
            int p = pos + size;
            tree[p] = value;
            p >>= 1;

            while (p > 0) {
                tree[p] = Math.min(tree[p << 1], tree[(p << 1) | 1]);
                p >>= 1;
            }
        }

        /**
         * Returns the minimum value in the inclusive range [left, right].
         *
         * @param left the left boundary of the query range, inclusive
         * @param right the right boundary of the query range, inclusive
         * @return the minimum value in the range, or INF if no active value exists there
         * @implNote Time complexity: O(log n)
         * @implNote Space complexity: O(1)
         */
        int query(int left, int right) {
            if (left > right) {
                return INF;
            }

            int l = left + size;
            int r = right + size;
            int result = INF;

            while (l <= r) {
                if ((l & 1) == 1) {
                    result = Math.min(result, tree[l]);
                    l++;
                }
                if ((r & 1) == 0) {
                    result = Math.min(result, tree[r]);
                    r--;
                }
                l >>= 1;
                r >>= 1;
            }

            return result;
        }
    }
}