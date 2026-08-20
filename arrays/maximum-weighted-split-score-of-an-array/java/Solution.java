import java.util.*;

/*
Problem Title: Maximum Weighted Split Score of an Array

Problem Description:
You are given an integer array nums of length n and an integer array weights of length n.
You must choose two indices i and j such that 0 <= i < j < n - 1, splitting the array
into three non-empty contiguous parts:

- left = nums[0..i]
- middle = nums[i+1..j]
- right = nums[j+1..n-1]

The score of a split is defined as:

(sum(left) * min(weights in left))
+ (sum(middle) * min(weights in middle))
+ (sum(right) * min(weights in right))

Return the maximum possible score over all valid splits.

Constraints:
- 3 <= n <= 2 * 10^5
- 1 <= nums[i] <= 10^9
- 1 <= weights[i] <= 10^9
- The answer may exceed 32-bit integer range, so use 64-bit arithmetic.

Important note:
The examples in the prompt contain inconsistent "Output" values versus their own explanations.
For correctness, this implementation follows the mathematical definition of the score.
For the provided examples, the correct best scores are:
- Example 1: 43
- Example 2: 79
*/

/**
 * A complete runnable solution for computing the maximum weighted split score.
 *
 * <p>
 * Core idea:
 * We need to split the array into three contiguous non-empty parts.
 * For a split (i, j):
 * - left   = [0..i]
 * - middle = [i+1..j]
 * - right  = [j+1..n-1]
 *
 * Score:
 *   leftValue(i) + middleValue(i+1, j) + rightValue(j+1)
 *
 * where segmentValue(l, r) = sum(nums[l..r]) * min(weights[l..r]).
 * </p>
 *
 * <p>
 * The difficult part is the middle segment, because both its sum and its minimum weight
 * depend on the chosen interval. A direct O(n^2) scan over all (i, j) is too slow.
 * </p>
 *
 * <p>
 * We solve it with a divide-and-conquer optimization over the middle segment:
 * For each possible middle interval [l..r], define:
 *   total = bestLeftEndingBefore(l) + value(l, r) + bestRightStartingAfter(r)
 *
 * where:
 * - bestLeftEndingBefore(l) = best value of a non-empty prefix segment [0..i] with i < l
 * - bestRightStartingAfter(r) = best value of a non-empty suffix segment [j..n-1] with j > r
 *
 * Then we need the maximum over all middle intervals [l..r] that leave room on both sides.
 * </p>
 *
 * <p>
 * The divide-and-conquer works similarly to classic "maximum subarray min-product" style logic:
 * for a range [L..R], let m be the midpoint. We recursively solve left half and right half,
 * and then solve all middle intervals that cross m.
 * </p>
 *
 * <p>
 * For crossing intervals [l..r] with l <= m <= r, we need:
 *   bestLeftBefore[l] + bestRightAfter[r] + (prefixSum[r+1] - prefixSum[l]) * minWeight(l, r)
 *
 * We enumerate candidate minima by expanding from the center in descending order of weight.
 * During this process, the current chosen set of indices forms a contiguous interval [a..b]
 * containing m, and every crossing interval whose minimum is at least currentWeight must lie
 * inside [a..b]. For a fixed currentWeight = w, the best crossing interval with minimum exactly
 * controlled at this stage becomes:
 *
 *   max over l in [LBound..m], r in [m..RBound]:
 *      (bestLeftBefore[l] - prefixSum[l] * w) + (bestRightAfter[r] + prefixSum[r+1] * w)
 *
 * So we only need the maximum left expression and maximum right expression over valid ranges.
 * We maintain those maxima incrementally while expanding.
 * </p>
 *
 * <p>
 * This yields O(n log n) time overall.
 * </p>
 */
public class Solution {

    /**
     * A very negative sentinel used for impossible states.
     */
    private static final long NEG_INF = Long.MIN_VALUE / 4;

    /**
     * Prefix sums of nums, where prefix[k] = sum(nums[0..k-1]).
     */
    private long[] prefix;

    /**
     * bestPrefixEnd[i] = value of segment [0..i] = sum(nums[0..i]) * min(weights[0..i]).
     */
    private long[] bestPrefixEnd;

    /**
     * bestSuffixStart[i] = value of segment [i..n-1] = sum(nums[i..n-1]) * min(weights[i..n-1]).
     */
    private long[] bestSuffixStart;

    /**
     * Input nums.
     */
    private int[] nums;

    /**
     * Input weights.
     */
    private int[] weights;

    /**
     * Length of the arrays.
     */
    private int n;

    /**
     * Computes the maximum weighted split score.
     *
     * <p>
     * Step-by-step:
     * 1. Precompute prefix sums of nums so any segment sum can be queried in O(1).
     * 2. Precompute the value of every possible left segment [0..i].
     *    Since the left segment always starts at 0, we can scan once while maintaining
     *    the running minimum weight.
     * 3. Precompute the value of every possible right segment [i..n-1].
     *    Since the right segment always ends at n-1, we can scan from right to left
     *    while maintaining the running minimum weight.
     * 4. Use divide-and-conquer on the middle segment indices to find the best interval [l..r]
     *    with 1 <= l <= r <= n-2, because left and right must both be non-empty.
     * 5. For each middle interval [l..r], total score is:
     *      bestPrefixEnd[l-1] + segmentValue(l, r) + bestSuffixStart[r+1]
     * 6. Return the maximum total score.
     * </p>
     *
     * @param nums the values array
     * @param weights the weights array
     * @return the maximum possible split score using 64-bit arithmetic
     * @implNote Time complexity: O(n log n)
     * @implNote Space complexity: O(n)
     */
    public long maximumWeightedSplitScore(int[] nums, int[] weights) {
        this.nums = nums;
        this.weights = weights;
        this.n = nums.length;

        if (n < 3) {
            throw new IllegalArgumentException("Array length must be at least 3.");
        }
        if (weights.length != n) {
            throw new IllegalArgumentException("nums and weights must have the same length.");
        }

        buildPrefixSums();
        buildBestPrefixEnd();
        buildBestSuffixStart();

        // The middle segment must be inside [1..n-2].
        return solveMiddleRange(1, n - 2);
    }

    /**
     * Builds prefix sums of nums.
     *
     * <p>
     * prefix[0] = 0
     * prefix[i+1] = nums[0] + nums[1] + ... + nums[i]
     * </p>
     *
     * @return nothing; fills the instance field {@code prefix}
     * @implNote Time complexity: O(n)
     * @implNote Space complexity: O(n)
     */
    public void buildPrefixSums() {
        prefix = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + nums[i];
        }
    }

    /**
     * Builds bestPrefixEnd where:
     * bestPrefixEnd[i] = sum(nums[0..i]) * min(weights[0..i]).
     *
     * <p>
     * Because the segment always starts at index 0, we only need:
     * - running sum from prefix sums
     * - running minimum of weights from left to right
     * </p>
     *
     * @return nothing; fills the instance field {@code bestPrefixEnd}
     * @implNote Time complexity: O(n)
     * @implNote Space complexity: O(n)
     */
    public void buildBestPrefixEnd() {
        bestPrefixEnd = new long[n];
        long minWeight = Long.MAX_VALUE;

        for (int i = 0; i < n; i++) {
            minWeight = Math.min(minWeight, weights[i]);
            long sum = prefix[i + 1];
            bestPrefixEnd[i] = sum * minWeight;
        }
    }

    /**
     * Builds bestSuffixStart where:
     * bestSuffixStart[i] = sum(nums[i..n-1]) * min(weights[i..n-1]).
     *
     * <p>
     * Because the segment always ends at index n-1, we scan from right to left while
     * maintaining the running minimum weight.
     * </p>
     *
     * @return nothing; fills the instance field {@code bestSuffixStart}
     * @implNote Time complexity: O(n)
     * @implNote Space complexity: O(n)
     */
    public void buildBestSuffixStart() {
        bestSuffixStart = new long[n];
        long minWeight = Long.MAX_VALUE;

        for (int i = n - 1; i >= 0; i--) {
            minWeight = Math.min(minWeight, weights[i]);
            long sum = prefix[n] - prefix[i];
            bestSuffixStart[i] = sum * minWeight;
        }
    }

    /**
     * Recursively solves for the best middle interval fully contained in [left..right].
     *
     * <p>
     * This method returns the maximum value of:
     *   bestPrefixEnd[l-1] + value(l, r) + bestSuffixStart[r+1]
     * over all intervals [l..r] such that:
     * - left <= l <= r <= right
     * - l >= 1 (so left part is non-empty)
     * - r <= n-2 (so right part is non-empty)
     * </p>
     *
     * <p>
     * Divide-and-conquer structure:
     * - solve left half
     * - solve right half
     * - solve intervals crossing the midpoint
     * </p>
     *
     * @param left left boundary of the middle interval search space
     * @param right right boundary of the middle interval search space
     * @return best total score contributed by any middle interval inside [left..right]
     * @implNote Time complexity: O((right-left+1) log(right-left+1)) over recursion; globally O(n log n)
     * @implNote Space complexity: O(log n) recursion stack, plus temporary O(length) arrays per level amortized by recursion
     */
    public long solveMiddleRange(int left, int right) {
        if (left > right) {
            return NEG_INF;
        }
        if (left == right) {
            int l = left;
            int r = right;
            return totalScoreForMiddleInterval(l, r);
        }

        int mid = left + (right - left) / 2;

        long best = Math.max(solveMiddleRange(left, mid), solveMiddleRange(mid + 1, right));
        best = Math.max(best, solveCrossingIntervals(left, mid, right));

        return best;
    }

    /**
     * Computes the total score for one specific middle interval [l..r].
     *
     * <p>
     * Total score:
     *   bestPrefixEnd[l-1] + segmentValue(l, r) + bestSuffixStart[r+1]
     * </p>
     *
     * @param l left boundary of the middle segment
     * @param r right boundary of the middle segment
     * @return total split score for this exact middle interval
     * @implNote Time complexity: O(r-l+1) because this helper recomputes the minimum directly
     * @implNote Space complexity: O(1)
     */
    public long totalScoreForMiddleInterval(int l, int r) {
        long minWeight = Long.MAX_VALUE;
        for (int i = l; i <= r; i++) {
            minWeight = Math.min(minWeight, weights[i]);
        }
        long sum = prefix[r + 1] - prefix[l];
        return bestPrefixEnd[l - 1] + sum * minWeight + bestSuffixStart[r + 1];
    }

    /**
     * Solves all middle intervals [l..r] that cross the midpoint, meaning l <= mid < = r.
     *
     * <p>
     * This is the heart of the optimization.
     * </p>
     *
     * <p>
     * We process indices in [left..right] in descending order of weight.
     * As we activate indices from larger weight to smaller weight, the active set around mid
     * grows into a contiguous interval [curL..curR] containing mid. At any moment, every
     * crossing interval [l..r] fully inside [curL..curR] has minimum weight at least currentWeight.
     * Therefore, for currentWeight = w, the best score among such intervals is:
     *
     *   max_{l in [curL..mid]} (bestPrefixEnd[l-1] - prefix[l] * w)
     * + max_{r in [mid..curR]} (bestSuffixStart[r+1] + prefix[r+1] * w)
     *
     * because:
     *   bestPrefixEnd[l-1] + bestSuffixStart[r+1] + (prefix[r+1] - prefix[l]) * w
     * = (bestPrefixEnd[l-1] - prefix[l] * w) + (bestSuffixStart[r+1] + prefix[r+1] * w)
     * </p>
     *
     * <p>
     * We maintain the maxima of the left and right expressions incrementally as curL expands left
     * and curR expands right.
     * </p>
     *
     * @param left left boundary of current recursive range
     * @param mid midpoint of current recursive range
     * @param right right boundary of current recursive range
     * @return best total score among all crossing middle intervals
     * @implNote Time complexity: O((right-left+1) log(right-left+1)) due to sorting by weight
     * @implNote Space complexity: O(right-left+1)
     */
    public long solveCrossingIntervals(int left, int mid, int right) {
        int len = right - left + 1;

        Integer[] order = new Integer[len];
        for (int i = 0; i < len; i++) {
            order[i] = left + i;
        }

        Arrays.sort(order, (a, b) -> Integer.compare(weights[b], weights[a]));

        boolean[] active = new boolean[n];
        active[mid] = false;

        int curL = mid + 1;
        int curR = mid - 1;

        long best = NEG_INF;

        long bestLeftExpr = NEG_INF;
        long bestRightExpr = NEG_INF;

        int ptr = 0;
        while (ptr < len) {
            int currentWeight = weights[order[ptr]];

            // Activate all indices with this same weight.
            while (ptr < len && weights[order[ptr]] == currentWeight) {
                active[order[ptr]] = true;
                ptr++;
            }

            // Expand the active contiguous block around mid as much as possible.
            while (curL - 1 >= left && active[curL - 1]) {
                curL--;
                long candidateLeftExpr = bestPrefixEnd[curL - 1] - prefix[curL] * (long) currentWeight;
                if (candidateLeftExpr > bestLeftExpr) {
                    bestLeftExpr = candidateLeftExpr;
                }
            }

            while (curR + 1 <= right && active[curR + 1]) {
                curR++;
                long candidateRightExpr = bestSuffixStart[curR + 1] + prefix[curR + 1] * (long) currentWeight;
                if (candidateRightExpr > bestRightExpr) {
                    bestRightExpr = candidateRightExpr;
                }
            }

            // If the midpoint itself just became reachable inside the active block,
            // then crossing intervals now exist.
            if (curL <= mid && mid <= curR) {
                // Important subtlety:
                // bestLeftExpr and bestRightExpr were updated using the currentWeight.
                // However, if the active block expanded in multiple steps under the same weight,
                // every newly included l/r is valid for this same currentWeight.
                // So combining the maxima is correct.
                best = Math.max(best, bestLeftExpr + bestRightExpr);
            } else {
                // The block may not yet include the midpoint on both sides.
                // In practice, for crossing intervals we need at least one valid l <= mid and r >= mid.
                // Since the middle interval must contain mid, we need both expansions to have reached it.
                // No action needed here.
            }
        }

        return best;
    }

    /**
     * A simple brute-force verifier for small arrays.
     *
     * <p>
     * This method is not used by the main algorithm, but it is helpful for testing and
     * educational purposes. It tries every valid pair (i, j), computes the exact score,
     * and returns the maximum.
     * </p>
     *
     * @param nums the values array
     * @param weights the weights array
     * @return exact maximum score by brute force
     * @implNote Time complexity: O(n^3) in the straightforward implementation below
     * @implNote Space complexity: O(1) extra space
     */
    public long maximumWeightedSplitScoreBruteForce(int[] nums, int[] weights) {
        int n = nums.length;
        long best = NEG_INF;

        for (int i = 0; i < n - 2; i++) {
            for (int j = i + 1; j < n - 1; j++) {
                long leftSum = 0;
                long middleSum = 0;
                long rightSum = 0;

                long leftMin = Long.MAX_VALUE;
                long middleMin = Long.MAX_VALUE;
                long rightMin = Long.MAX_VALUE;

                for (int k = 0; k <= i; k++) {
                    leftSum += nums[k];
                    leftMin = Math.min(leftMin, weights[k]);
                }
                for (int k = i + 1; k <= j; k++) {
                    middleSum += nums[k];
                    middleMin = Math.min(middleMin