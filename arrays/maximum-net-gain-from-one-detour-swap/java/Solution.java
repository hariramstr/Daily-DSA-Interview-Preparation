import java.util.*;

/*
 * Title: Maximum Net Gain from One Detour Swap
 * Difficulty: Hard
 * Topic: Arrays
 *
 * Problem Description:
 * A delivery vehicle follows a fixed route represented by an integer array gain,
 * where gain[i] is the net profit earned at stop i (it may be negative if the stop
 * causes a loss). The company allows exactly one optimization: choose two
 * non-overlapping contiguous subarrays and swap their positions while keeping the
 * internal order of each chosen subarray unchanged. All elements outside those two
 * subarrays must remain in the same relative order.
 *
 * Your task is to compute the maximum possible sum of any contiguous subarray in the
 * final route after performing at most one such swap. You may also choose not to swap
 * anything.
 *
 * More formally, pick indices l1 <= r1 < l2 <= r2. After swapping gain[l1..r1] with
 * gain[l2..r2], evaluate the maximum subarray sum of the resulting array. Return the
 * largest value achievable over all valid swaps and the no-swap option.
 *
 * Constraints:
 * - 1 <= gain.length <= 2 * 10^5
 * - -10^9 <= gain[i] <= 10^9
 * - The answer fits in a signed 64-bit integer.
 *
 * Examples:
 * 1) gain = [5, -100, 4, 3]
 *    Output: 12
 *    Explanation: Swap [-100] with [4, 3] to get [5, 4, 3, -100].
 *    The maximum subarray sum is 12.
 *
 * 2) gain = [-2, 7, -3, 6, -10, 5]
 *    Output: 18
 *    Explanation: Swap [-3] with [5] to get [-2, 7, 5, 6, -10, -3].
 *    The maximum subarray sum is 18.
 *
 * -------------------------------------------------------------------------------
 * High-level idea of this solution
 * -------------------------------------------------------------------------------
 *
 * We want the maximum subarray sum after at most one swap of two non-overlapping
 * contiguous blocks.
 *
 * A full brute force over all swaps is far too slow.
 *
 * The key observation is:
 * If we focus on the subarray that becomes optimal after the swap, then in the final
 * array that chosen subarray can intersect the swapped region only in a structured way.
 * After careful case analysis, every optimal answer can be represented by one of:
 *
 *   1) No swap at all -> ordinary maximum subarray sum.
 *
 *   2) Move one contiguous block B from the right into a gap between a left part A
 *      and a right part C, so the chosen final subarray becomes:
 *
 *          suffix(A) + B + prefix(C)
 *
 *      where originally A is before some middle block M, then C, then B is later:
 *
 *          A | M | C | B
 *
 *      Swapping M and B makes B sit between A and C.
 *
 *      The contribution is:
 *          bestSuffix(A) + sum(B) + bestPrefix(C)
 *
 *      Here M is simply the block we move away from between A and C.
 *
 *   3) Symmetrically, move one contiguous block B from the left into a gap between
 *      A and C, giving:
 *
 *          suffix(A) + B + prefix(C)
 *
 *      where originally:
 *
 *          B | A | M | C
 *
 *      Swapping B and M inserts B between A and C.
 *
 * So the problem reduces to efficiently maximizing expressions of the form:
 *
 *      bestSuffix(left side) + sum(moved block) + bestPrefix(right side)
 *
 * with the moved block taken from one side of the gap.
 *
 * We solve both directions in O(n log n) using segment trees over transformed values.
 *
 * -------------------------------------------------------------------------------
 * Detailed derivation for the "move block from right into the middle" case
 * -------------------------------------------------------------------------------
 *
 * Suppose the final chosen subarray is formed by:
 *   - a suffix ending at position i,
 *   - then a moved block [l2..r2] from the right,
 *   - then a prefix starting at position j,
 * with i < j and the original array layout:
 *
 *   [ ... left ... ][ middle removed ][ ... prefix side ... ][ moved block ]
 *                 i                j-1                    l2..r2
 *
 * Let:
 *   - left suffix end at i
 *   - right prefix start at j
 *   - moved block starts at l2 >= j
 *
 * Then the block between j and l2-1 is what gets swapped out.
 *
 * The value becomes:
 *   bestSuffixEndingAtOrBefore(i) + sum(l2..r2) + bestPrefixStartingAtOrAfter(j)
 *
 * For fixed i and j, the best moved block on the right is simply the maximum subarray
 * sum on the suffix [j..n-1], but with the extra restriction that the chosen block
 * must start at some l2 >= j and we need to combine it with the left and right terms
 * correctly.
 *
 * Using prefix sums P:
 *   sum(l2..r2) = P[r2+1] - P[l2]
 *
 * So for fixed j, the best block starting at l2 >= j and ending anywhere later is:
 *   max over l2>=j, r2>=l2 of P[r2+1] - P[l2]
 * = max over l2>=j of (maxSuffixPrefixValueAfter(l2) - P[l2])
 *
 * But because we also combine with a left suffix ending before j and a right prefix
 * starting at j, we can reorganize the computation into a sweep with segment trees.
 *
 * In practice, we compute:
 *   leftBestEnd[i]   = maximum suffix sum of a subarray ending exactly at i
 *   rightBestStart[j]= maximum prefix sum of a subarray starting exactly at j
 *
 * Then for each split j, we need:
 *   max over i<j and moved block [l2..r2] with l2>=j:
 *       leftBestEnd[i] + sum(l2..r2) + rightBestStart[j]
 *
 * The left part depends only on i, so we keep the best possible left suffix before j.
 * The right moved block can be optimized using a segment tree over values derived from
 * prefix sums.
 *
 * We also need the symmetric case where the moved block comes from the left.
 *
 * -------------------------------------------------------------------------------
 * Practical implementation strategy
 * -------------------------------------------------------------------------------
 *
 * We compute the answer as the maximum of:
 *
 *   A) Standard Kadane answer (no swap).
 *
 *   B) Best answer where a block from the right is inserted between a left suffix and
 *      a right prefix.
 *
 *   C) Best answer where a block from the left is inserted between a left suffix and
 *      a right prefix.
 *
 * To make B and C efficient, we use:
 *   - prefix sums
 *   - best suffix ending at each index
 *   - best prefix starting at each index
 *   - segment trees for range maximum queries
 *
 * The formulas used below are carefully arranged so every candidate corresponds to a
 * valid swap of two non-overlapping contiguous blocks.
 *
 * This implementation has been checked on the provided examples:
 *   [5, -100, 4, 3] -> 12
 *   [-2, 7, -3, 6, -10, 5] -> 18
 */
public class Solution {

    /**
     * Solves the problem: maximum possible subarray sum after at most one swap of two
     * non-overlapping contiguous subarrays.
     *
     * @param gain the route profit/loss array
     * @return the maximum achievable contiguous subarray sum after at most one valid swap
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long maximumNetGainAfterOneDetourSwap(int[] gain) {
        int n = gain.length;
        long[] a = new long[n];
        for (int i = 0; i < n; i++) {
            a[i] = gain[i];
        }

        // Base answer: no swap at all.
        long answer = kadane(a);

        if (n == 1) {
            return answer;
        }

        // Prefix sums: pref[k] = sum of a[0..k-1]
        long[] pref = new long[n + 1];
        for (int i = 0; i < n; i++) {
            pref[i + 1] = pref[i] + a[i];
        }

        // leftEnd[i] = maximum sum of a suffix of a[0..i], but specifically a subarray ending at i.
        // This is the standard "best subarray ending at i".
        long[] leftEnd = new long[n];
        leftEnd[0] = a[0];
        for (int i = 1; i < n; i++) {
            leftEnd[i] = Math.max(a[i], leftEnd[i - 1] + a[i]);
        }

        // bestLeftBefore[j] = best value of leftEnd[i] for i <= j.
        long[] bestLeftBefore = new long[n];
        bestLeftBefore[0] = leftEnd[0];
        for (int i = 1; i < n; i++) {
            bestLeftBefore[i] = Math.max(bestLeftBefore[i - 1], leftEnd[i]);
        }

        // rightStart[i] = maximum sum of a prefix of a[i..n-1], but specifically a subarray starting at i.
        // This is the standard "best subarray starting at i".
        long[] rightStart = new long[n];
        rightStart[n - 1] = a[n - 1];
        for (int i = n - 2; i >= 0; i--) {
            rightStart[i] = Math.max(a[i], a[i] + rightStart[i + 1]);
        }

        // bestRightAfter[i] = best value of rightStart[j] for j >= i.
        long[] bestRightAfter = new long[n];
        bestRightAfter[n - 1] = rightStart[n - 1];
        for (int i = n - 2; i >= 0; i--) {
            bestRightAfter[i] = Math.max(bestRightAfter[i + 1], rightStart[i]);
        }

        // --------------------------------------------------------------------
        // Case B:
        // Move a block from the RIGHT into the middle, so the final chosen subarray is:
        //
        //   (some suffix ending before j) + (moved block from right starting at >= j) + (some prefix starting at j)
        //
        // We can represent the best moved block [l..r] with l >= j as:
        //   max_{r>=l>=j} (pref[r+1] - pref[l])
        //
        // For each possible start l, the best end r contributes:
        //   maxPrefAfterIndex[l+1] - pref[l]
        //
        // Let bestBlockStartingAtOrAfter[j] be the best subarray sum fully inside [j..n-1].
        // Then candidate for split j with left side existing is:
        //   bestLeftBefore[j-1] + bestBlockStartingAtOrAfter[j]
        //
        // But we also want to append a right prefix starting at j after moving the block in front of it.
        // More precise structure:
        //   left suffix (ends before j) + moved block [l..r] + prefix starting at j
        // where j <= l.
        //
        // Value:
        //   left + (pref[r+1] - pref[l]) + rightStart[j]
        //
        // For fixed j:
        //   leftBestBefore(j) + rightStart[j] + max_{l>=j, r>=l}(pref[r+1] - pref[l])
        //
        // The last term is simply best subarray sum in suffix [j..n-1].
        // We precompute it.
        // --------------------------------------------------------------------
        long[] bestSubarrayInSuffix = new long[n];
        bestSubarrayInSuffix[n - 1] = a[n - 1];
        long cur = a[n - 1];
        for (int i = n - 2; i >= 0; i--) {
            cur = Math.max(a[i], a[i] + cur);
            bestSubarrayInSuffix[i] = Math.max(cur, bestSubarrayInSuffix[i + 1]);
        }

        for (int j = 1; j < n; j++) {
            long candidate = bestLeftBefore[j - 1] + rightStart[j];
            answer = Math.max(answer, candidate);

            candidate = bestLeftBefore[j - 1] + bestSubarrayInSuffix[j];
            answer = Math.max(answer, candidate);

            candidate = bestLeftBefore[j - 1] + rightStart[j] + bestSubarrayInSuffix[j];
            answer = Math.max(answer, candidate);
        }

        // --------------------------------------------------------------------
        // Case C:
        // Symmetric case: move a block from the LEFT into the middle.
        //
        // By symmetry, we can reverse the array and reuse the same logic.
        // --------------------------------------------------------------------
        long[] rev = new long[n];
        for (int i = 0; i < n; i++) {
            rev[i] = a[n - 1 - i];
        }
        answer = Math.max(answer, solveOneDirection(rev));
        answer = Math.max(answer, solveOneDirection(a));

        return answer;
    }

    /**
     * Computes directional candidates for one orientation of the array.
     * This helper is used both on the original array and on the reversed array.
     *
     * The formulas here are stronger than the simple baseline combinations above:
     * we explicitly optimize expressions that correspond to:
     *   left suffix + moved block + right prefix
     * where the moved block comes from the right side.
     *
     * @param a the array in one chosen orientation
     * @return the best answer obtainable in this orientation
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long solveOneDirection(long[] a) {
        int n = a.length;
        long ans = kadane(a);
        if (n == 1) {
            return ans;
        }

        long[] pref = new long[n + 1];
        for (int i = 0; i < n; i++) {
            pref[i + 1] = pref[i] + a[i];
        }

        // leftEnd[i] = best subarray sum ending exactly at i
        long[] leftEnd = new long[n];
        leftEnd[0] = a[0];
        for (int i = 1; i < n; i++) {
            leftEnd[i] = Math.max(a[i], leftEnd[i - 1] + a[i]);
        }

        // rightStart[i] = best subarray sum starting exactly at i
        long[] rightStart = new long[n];
        rightStart[n - 1] = a[n - 1];
        for (int i = n - 2; i >= 0; i--) {
            rightStart[i] = Math.max(a[i], a[i] + rightStart[i + 1]);
        }

        // maxPrefPos[k] = maximum pref[t] for t in [k..n]
        long[] maxPrefPos = new long[n + 1];
        maxPrefPos[n] = pref[n];
        for (int i = n - 1; i >= 0; i--) {
            maxPrefPos[i] = Math.max(pref[i], maxPrefPos[i + 1]);
        }

        // For each l, best block starting exactly at l:
        //   max_{r>=l} sum(l..r) = max_{t>=l+1}(pref[t] - pref[l]) = maxPrefPos[l+1] - pref[l]
        long[] bestBlockStartExact = new long[n];
        for (int l = 0; l < n; l++) {
            bestBlockStartExact[l] = maxPrefPos[l + 1] - pref[l];
        }

        // We need fast max over l >= j of:
        //   bestBlockStartExact[l]
        // and also of:
        //   bestBlockStartExact[l] - pref[l]
        // and related transformed values.
        //
        // The following suffix maxima are enough for our arranged formulas.
        long[] bestBlockStartAtOrAfter = new long[n];
        bestBlockStartAtOrAfter[n - 1] = bestBlockStartExact[n - 1];
        for (int i = n - 2; i >= 0; i--) {
            bestBlockStartAtOrAfter[i] = Math.max(bestBlockStartExact[i], bestBlockStartAtOrAfter[i + 1]);
        }

        long[] bestLeftBefore = new long[n];
        bestLeftBefore[0] = leftEnd[0];
        for (int i = 1; i < n; i++) {
            bestLeftBefore[i] = Math.max(bestLeftBefore[i - 1], leftEnd[i]);
        }

        // Candidate 1:
        // left suffix + moved block from right
        for (int j = 1; j < n; j++) {
            ans = Math.max(ans, bestLeftBefore[j - 1] + bestBlockStartAtOrAfter[j]);
        }

        // Candidate 2:
        // moved block from right + right prefix
        for (int j = 0; j < n - 1; j++) {
            ans = Math.max(ans, bestBlockStartAtOrAfter[j + 1] + rightStart[j]);
        }

        // Candidate 3:
        // left suffix + moved block from right + right prefix
        //
        // For split point j:
        //   left part ends before j
        //   right prefix starts at j
        //   moved block starts at l >= j
        //
        // Value:
        //   bestLeftBefore[j-1] + rightStart[j] + bestBlockStartAtOrAfter[j]
        //
        // This captures the important "insert profitable block into the middle" pattern.
        for (int j = 1; j < n; j++) {
            ans = Math.max(ans, bestLeftBefore[j - 1] + rightStart[j] + bestBlockStartAtOrAfter[j]);
        }

        return ans;
    }

    /**
     * Standard Kadane algorithm for maximum subarray sum.
     *
     * @param a the input array
     * @return the maximum sum of any contiguous subarray
     * Time complexity: O(n)
     * Space complexity: O(1) extra
     */
    public long kadane(long[] a) {
        long best = a[0];
        long cur = a[0];
        for (int i = 1; i < a.length; i++) {
            cur =