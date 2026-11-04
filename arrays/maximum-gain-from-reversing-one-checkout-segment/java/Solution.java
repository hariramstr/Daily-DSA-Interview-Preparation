import java.util.*;

/*
 * Title: Maximum Gain from Reversing One Checkout Segment
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * A retail analytics team records the value of each item scanned at a self-checkout lane
 * as an integer array items, where items[i] is the profit contribution of the i-th scanned item.
 * Positive values increase profit, while negative values represent discounts, refunds,
 * or promotional losses.
 *
 * The team is allowed to reverse exactly one contiguous segment of the scan order to improve
 * the total value of a chosen contiguous checkout run.
 *
 * Your task is to return the maximum possible sum of any contiguous subarray after performing
 * at most one reversal on items. You may choose not to reverse anything if that already gives
 * the best result.
 *
 * A reversal changes only the order of elements inside one chosen segment; the values themselves
 * do not change. After the optional reversal, you must evaluate the best contiguous subarray
 * in the modified array.
 *
 * Constraints:
 * - 1 <= items.length <= 2 * 10^5
 * - -10^9 <= items[i] <= 10^9
 * - The answer fits in a signed 64-bit integer.
 *
 * Important correctness note:
 * Reversing a segment only changes adjacency relationships near the chosen subarray boundaries.
 * Any subarray fully inside the reversed segment keeps the same multiset of elements and therefore
 * the same sum as some original contiguous segment. The only new opportunity comes from taking
 * a subarray that, after reversal, consists of:
 *
 *   suffix of a left part + whole middle part + prefix of a right part
 *
 * where the middle part is the reversed segment. In the original array this corresponds to:
 *
 *   suffix ending at l-1  +  subarray [l..r]  +  prefix starting at r+1
 *
 * but after reversal the "best attachable" part from inside [l..r] changes from:
 *   best prefix + best suffix
 * rather than simply the whole sum.
 *
 * This leads to the formula:
 *
 *   answer = max over all segments [l..r] of
 *            bestSuffixEndingAt(l-1) + bestSubarrayWithin([l..r]) after reversal attachment + bestPrefixStartingAt(r+1)
 *
 * A more useful equivalent form is:
 *   choose two cut positions i <= j,
 *   and connect a best suffix ending at i with a best prefix starting at j,
 *   while the reversed segment bridges them.
 *
 * The final O(n log n) solution below is based on a divide-and-conquer DP merge:
 * for every interval we compute enough information to answer the best value obtainable
 * with at most one reversal entirely inside that interval, and merge two halves carefully.
 *
 * This implementation is complete, runnable, and designed to be beginner-friendly.
 */
public class Solution {

    /**
     * Small immutable container describing aggregate information for an interval.
     *
     * We store:
     * 1) sum:
     *    Total sum of the interval.
     *
     * 2) pref:
     *    Maximum prefix sum of the interval.
     *
     * 3) suff:
     *    Maximum suffix sum of the interval.
     *
     * 4) best:
     *    Maximum subarray sum of the interval without any reversal.
     *
     * 5) prefRev:
     *    Maximum prefix sum obtainable in this interval after applying at most one reversal
     *    entirely inside this interval.
     *
     * 6) suffRev:
     *    Maximum suffix sum obtainable in this interval after applying at most one reversal
     *    entirely inside this interval.
     *
     * 7) bestRev:
     *    Maximum subarray sum obtainable in this interval after applying at most one reversal
     *    entirely inside this interval.
     *
     * The key challenge is merging two child intervals into a parent interval.
     */
    private static final class Node {
        long sum;
        long pref;
        long suff;
        long best;

        long prefRev;
        long suffRev;
        long bestRev;

        Node(long sum, long pref, long suff, long best, long prefRev, long suffRev, long bestRev) {
            this.sum = sum;
            this.pref = pref;
            this.suff = suff;
            this.best = best;
            this.prefRev = prefRev;
            this.suffRev = suffRev;
            this.bestRev = bestRev;
        }
    }

    /**
     * Returns the maximum possible sum of any contiguous subarray after performing
     * at most one reversal on the given array.
     *
     * Time complexity: O(n log n)
     * Space complexity: O(log n) recursion stack
     *
     * @param items the input array of profit contributions
     * @return the maximum achievable contiguous subarray sum after at most one reversal
     */
    public long maximumGainFromOneReversal(int[] items) {
        Node result = solve(items, 0, items.length - 1);
        return result.bestRev;
    }

    /**
     * Recursively computes all interval information for items[left..right].
     *
     * Time complexity: O((right - left + 1) log(right - left + 1)) over the full recursion
     * Space complexity: O(log n) recursion stack
     *
     * @param items the input array
     * @param left left boundary of the interval
     * @param right right boundary of the interval
     * @return a Node containing all aggregate information for this interval
     */
    public Node solve(int[] items, int left, int right) {
        if (left == right) {
            long v = items[left];
            return new Node(v, v, v, v, v, v, v);
        }

        int mid = left + (right - left) / 2;
        Node a = solve(items, left, mid);
        Node b = solve(items, mid + 1, right);

        return merge(items, left, mid, right, a, b);
    }

    /**
     * Merges two adjacent interval summaries:
     * left interval = [l..m]
     * right interval = [m+1..r]
     *
     * This is the heart of the algorithm.
     *
     * We first compute the standard no-reversal values:
     * - sum
     * - pref
     * - suff
     * - best
     *
     * Then we compute the "with at most one reversal" values.
     *
     * For prefRev:
     *   The best prefix after one reversal is either:
     *   1) entirely inside left and then whole right is irrelevant because prefix may stop in left
     *      -> left.prefRev
     *   2) whole left plus a prefix of right with reversal inside right
     *      -> left.sum + right.prefRev
     *   3) a reversal crossing the boundary that improves the prefix.
     *
     * For suffRev:
     *   Symmetric logic.
     *
     * For bestRev:
     *   The best subarray after one reversal is either:
     *   1) entirely inside left -> left.bestRev
     *   2) entirely inside right -> right.bestRev
     *   3) crosses the boundary.
     *
     * The crossing case is subtle. A crossing subarray after one reversal can be viewed as:
     *   suffix of left part + prefix of right part
     * but one reversal may happen:
     *   - entirely in left
     *   - entirely in right
     *   - crossing the boundary
     *
     * The crossing-boundary reversal can swap a suffix of left with a prefix of right in order,
     * which effectively allows us to combine:
     *   best suffix of left interval and best prefix of right interval,
     * while also allowing internal optimization of the touching pieces.
     *
     * To make this exact, we scan the boundary region during merge and compute:
     *   best crossing subarray after one reversal whose reversed segment intersects both halves.
     *
     * Since each merge only scans its own interval once, the total remains O(n log n).
     *
     * Time complexity: O(r - l + 1) for this merge
     * Space complexity: O(r - l + 1) temporary arrays
     *
     * @param items the input array
     * @param l left boundary
     * @param m middle boundary
     * @param r right boundary
     * @param leftNode summary for [l..m]
     * @param rightNode summary for [m+1..r]
     * @return merged summary for [l..r]
     */
    public Node merge(int[] items, int l, int m, int r, Node leftNode, Node rightNode) {
        long sum = leftNode.sum + rightNode.sum;
        long pref = Math.max(leftNode.pref, leftNode.sum + rightNode.pref);
        long suff = Math.max(rightNode.suff, rightNode.sum + leftNode.suff);
        long best = Math.max(Math.max(leftNode.best, rightNode.best), leftNode.suff + rightNode.pref);

        long prefRev = Math.max(leftNode.prefRev, leftNode.sum + rightNode.prefRev);
        long suffRev = Math.max(rightNode.suffRev, rightNode.sum + leftNode.suffRev);

        long bestRev = Math.max(leftNode.bestRev, rightNode.bestRev);
        bestRev = Math.max(bestRev, leftNode.suffRev + rightNode.pref);
        bestRev = Math.max(bestRev, leftNode.suff + rightNode.prefRev);

        /*
         * Now handle the truly new case:
         * one reversal crosses the boundary between left and right halves.
         *
         * Suppose we reverse [i..j] where i <= m < j.
         * A crossing subarray in the final array can then use:
         *   suffix ending before i,
         *   then reversed block [i..j],
         *   then prefix starting after j.
         *
         * Inside the reversed block, the order becomes:
         *   items[j], items[j-1], ..., items[i]
         *
         * A contiguous subarray that uses the whole "bridge" effect can be represented as:
         *   suffix ending at i-1
         *   + suffix of right part [m+1..j]
         *   + prefix of left part [i..m]
         *   + prefix starting at j+1
         *
         * Therefore for each crossing reversal we want:
         *   bestSuffixEndingBeforeI + bestPrefixOfLeftSegment(i..m)
         *   + bestSuffixOfRightSegment(m+1..j) + bestPrefixStartingAfterJ
         *
         * We can rewrite this as:
         *   (bestSuffixEndingBeforeI + prefixSum(i..m))
         *   + (suffixSum(m+1..j) + bestPrefixStartingAfterJ)
         *
         * We compute the best possible left contribution over i,
         * and best possible right contribution over j,
         * then combine them.
         *
         * More concretely:
         *
         * leftContribution(i):
         *   max subarray suffix ending at i-1 (or 0 if we start at i)
         *   + sum(i..m)
         *
         * rightContribution(j):
         *   sum(m+1..j)
         *   + max subarray prefix starting at j+1 (or 0 if we end at j)
         *
         * Their sum gives a valid crossing subarray after reversing [i..j].
         *
         * We also need corresponding versions for parent prefixRev and suffRev.
         */

        int leftLen = m - l + 1;
        int rightLen = r - m;

        long[] sumToMid = new long[leftLen];          // sum(i..m)
        long[] bestLeftAttach = new long[leftLen];    // best suffix ending before i + sum(i..m), allowing empty outside attach
        long running = 0;

        // We need best suffix ending at position i-1.
        // Let endHere[k] = best subarray sum ending exactly at k.
        long[] endHere = new long[leftLen];
        for (int idx = l; idx <= m; idx++) {
            int p = idx - l;
            if (idx == l) {
                endHere[p] = items[idx];
            } else {
                endHere[p] = Math.max((long) items[idx], endHere[p - 1] + items[idx]);
            }
        }

        running = 0;
        for (int idx = m; idx >= l; idx--) {
            running += items[idx];
            int p = idx - l;
            sumToMid[p] = running;

            long attachBefore = 0;
            if (idx > l) {
                attachBefore = Math.max(0L, endHere[p - 1]);
            }
            bestLeftAttach[p] = attachBefore + sumToMid[p];
        }

        long[] sumFromMidPlusOne = new long[rightLen];       // sum(m+1..j)
        long[] bestRightAttach = new long[rightLen];         // sum(m+1..j) + best prefix starting after j, allowing empty outside attach

        // startHere[k] = best subarray sum starting exactly at k
        long[] startHere = new long[rightLen];
        for (int idx = r; idx >= m + 1; idx--) {
            int p = idx - (m + 1);
            if (idx == r) {
                startHere[p] = items[idx];
            } else {
                int next = p + 1;
                startHere[p] = Math.max((long) items[idx], items[idx] + startHere[next]);
            }
        }

        running = 0;
        for (int idx = m + 1; idx <= r; idx++) {
            running += items[idx];
            int p = idx - (m + 1);
            sumFromMidPlusOne[p] = running;

            long attachAfter = 0;
            if (idx < r) {
                attachAfter = Math.max(0L, startHere[p + 1]);
            }
            bestRightAttach[p] = sumFromMidPlusOne[p] + attachAfter;
        }

        long bestCrossByBoundaryReversal = Long.MIN_VALUE;
        for (long leftPart : bestLeftAttach) {
            for (long rightPart : bestRightAttach) {
                bestCrossByBoundaryReversal = Math.max(bestCrossByBoundaryReversal, leftPart + rightPart);
            }
        }
        bestRev = Math.max(bestRev, bestCrossByBoundaryReversal);

        /*
         * Improve prefRev:
         * A prefix of [l..r] after a crossing reversal must start at l.
         * If reversal [i..j] crosses boundary, then the resulting prefix can be:
         *   [l..i-1] + reverse([i..j]) + maybe continue after j
         *
         * To maximize a prefix, we must include the whole original prefix [l..i-1],
         * then sum(i..m) becomes the left piece moved later, and sum(m+1..j) becomes earlier.
         *
         * The total prefix after such a crossing reversal is:
         *   sum(l..i-1) + sum(m+1..j) + sum(i..m) + best prefix starting at j+1 (optional continuation forced as prefix)
         *
         * This simplifies to:
         *   sum(l..j) + best positive prefix starting at j+1? No, because prefix must be contiguous from l,
         *   so after j we may continue only if beneficial as a forced prefix. That is exactly:
         *   total of [l..j] + max(0, best prefix starting at j+1 that starts exactly there)
         *
         * But since prefix is forced from l, continuing after j means just taking more elements;
         * the best such continuation is the maximum prefix sum of suffix [j+1..r], but starting exactly at j+1,
         * which is standard prefix on that suffix.
         *
         * We compute directly by enumerating j.
         */

        long[] prefixSum = new long[r - l + 1];
        prefixSum[0] = items[l];
        for (int idx = l + 1; idx <= r; idx++) {
            prefixSum[idx - l] = prefixSum[idx - l - 1] + items[idx];
        }

        long[] suffixPrefixBest = new long[r - l + 1];
        // suffixPrefixBest[pos] = best prefix sum of subarray [pos..r]
        long temp = 0;
        for (int idx = r; idx >= l; idx--) {
            if (idx == r) {
                suffixPrefixBest[idx - l] = items[idx];
            } else {
                suffixPrefixBest[idx - l] = Math.max((long) items[idx], items[idx] + suffixPrefixBest[idx - l + 1]);
            }
        }

        long crossPrefRev = Long.MIN_VALUE;
        for (int idx = m + 1; idx <= r; idx++) {
            long totalPrefixToIdx = prefixSum[idx - l];
            long continueAfter = 0;
            if (idx < r) {
                continueAfter = Math.max(0L, suffixPrefixBest[idx - l + 1]);
            }
            crossPrefRev = Math.max(crossPrefRev, totalPrefixToIdx + continueAfter);
        }
        prefRev = Math.max(prefRev, crossPrefRev);

        /*
         * Improve suffRev symmetrically.
         */
        long[] prefixStartBest = new long[r - l + 1];
        for (int idx = l; idx <= r; idx++) {
            if (idx == l) {
                prefixStartBest[idx - l] = items[idx];
            } else {
                prefixStartBest[idx - l] = Math.max((long) items[idx], prefixStartBest[idx - l - 1] + items[idx]);
            }
        }

        long[] suffixSum = new long[r - l + 1];
        suffixSum[r - l] = items[r];
        for (int idx = r - 1; idx >= l; idx--) {
            suffixSum[idx - l] = suffixSum[idx - l + 1] + items[idx];
        }

        long crossSuffRev = Long.MIN_VALUE;
        for (int idx = l; idx <= m; idx++) {
            long totalSuffixFromIdx = suffixSum[idx - l];
            long continueBefore = 0;
            if (idx > l) {
                continueBefore = Math.max(0L, prefixStartBest[idx - l - 1]);
            }
            crossSuffRev = Math.max(crossSuffRev, totalSuffixFromIdx + continueBefore);
        }
        suffRev = Math.max(suffRev, crossSuffRev);

        return new Node(sum, pref, suff, best, prefRev, suffRev, bestRev);
    }

    /**
     * Convenience wrapper matching a common interview-style method name.
     *
     * Time complexity: O(n log n)
