import java.util.*;

/*
Problem Title: Maximum Score from Choosing a Guarded Middle Segment

Problem Description:
You are given an integer array nums of length n and two non-negative integers L and R, where 0 <= L, R < n.
You must choose exactly one non-empty contiguous subarray nums[i..j] as your active segment.
The segment is considered valid only if there are at least L elements strictly to its left and at least R elements strictly to its right.
In other words, the chosen segment must satisfy i >= L and j <= n - 1 - R.

The score of a valid segment is defined as:

(minimum value inside the segment) * (sum of all values inside the segment)

Your task is to return the maximum possible score among all valid segments.

This is not simply a maximum-subarray problem, because the score depends on both the segment sum and the segment minimum.
Negative numbers are allowed, so extending a segment may increase or decrease the score in non-obvious ways.
An O(n^2) solution will not pass.

Constraints:
- 1 <= n <= 2 * 10^5
- -10^9 <= nums[i] <= 10^9
- 0 <= L, R < n
- There is guaranteed to be at least one valid segment

Notes:
- The answer may exceed 32-bit integer range, so use 64-bit arithmetic.
- A strong solution typically combines monotonic structure ideas with prefix sums and range optimization.

Core idea of this solution:
1) For every index k, treat nums[k] as the minimum of the chosen segment.
2) Using monotonic stacks, compute the maximal interval [leftBound[k], rightBound[k]] in which nums[k]
   is the unique chosen representative minimum:
      - all values inside that interval are >= nums[k]
      - ties are broken consistently using:
            previous strictly smaller
            next smaller-or-equal
3) Any valid segment where nums[k] is the minimum must satisfy:
      left in [max(L, leftBound[k]), min(k, n-1-R)]
      right in [max(k, L), min(rightBound[k], n-1-R)]
   and left <= k <= right.
4) Let prefix sums be P, so sum(left..right) = P[right+1] - P[left].
   For fixed k:
      score = nums[k] * (P[right+1] - P[left])
   Therefore:
      - if nums[k] >= 0, maximize the segment sum
        => choose maximum P[right+1] and minimum P[left]
      - if nums[k] < 0, minimize the segment sum
        => choose minimum P[right+1] and maximum P[left]
5) So for each k we only need range min/max queries on prefix sums.
   We build segment trees over prefix sums to answer:
      min / max on P indices corresponding to allowed left endpoints
      min / max on P indices corresponding to allowed right+1 endpoints
6) This yields O(n log n) time.

Important note about Example 1 in the prompt:
The textual explanation is inconsistent with the formal rule.
This implementation follows the formal validity rule exactly, as required.
*/

public class Solution {

    /**
     * Solves the problem: maximum score among all valid guarded subarrays.
     *
     * Detailed meaning:
     * - We may only choose subarrays nums[i..j] such that i >= L and j <= n - 1 - R.
     * - Score = (minimum element in subarray) * (sum of subarray).
     *
     * Algorithm summary:
     * 1. Compute prefix sums.
     * 2. Compute, for each index k, the maximal interval where nums[k] can serve as the chosen minimum
     *    under a consistent tie-breaking rule.
     * 3. For each k, derive the allowed range of left endpoints and right endpoints.
     * 4. Depending on the sign of nums[k], either maximize or minimize the subarray sum.
     * 5. Use segment trees on prefix sums to query range min/max efficiently.
     *
     * @param nums the input array
     * @param L minimum number of elements strictly to the left of the chosen segment
     * @param R minimum number of elements strictly to the right of the chosen segment
     * @return the maximum possible score using 64-bit arithmetic
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long maximumScore(int[] nums, int L, int R) {
        int n = nums.length;

        // Prefix sums:
        // prefix[t] = sum of nums[0..t-1]
        // Therefore sum of nums[i..j] = prefix[j+1] - prefix[i]
        long[] prefix = buildPrefixSums(nums);

        // For each index i:
        // leftLess[i]  = index of previous element strictly smaller than nums[i], or -1 if none
        // rightLessEq[i] = index of next element smaller than or equal to nums[i], or n if none
        //
        // Then nums[i] is the designated minimum for any subarray [l..r] such that:
        // leftLess[i] < l <= i <= r < rightLessEq[i]
        int[] leftLess = previousStrictlySmaller(nums);
        int[] rightLessEq = nextSmallerOrEqual(nums);

        // Convert the above strict/open boundaries into inclusive allowed bounds for endpoints.
        int[] leftBound = new int[n];
        int[] rightBound = new int[n];
        for (int i = 0; i < n; i++) {
            leftBound[i] = leftLess[i] + 1;
            rightBound[i] = rightLessEq[i] - 1;
        }

        // Build segment trees over prefix sums.
        //
        // Why prefix sums?
        // For a subarray [l..r], sum = prefix[r+1] - prefix[l].
        //
        // So for each i we need:
        // - queries over prefix[l] where l is in some interval
        // - queries over prefix[r+1] where r is in some interval, meaning (r+1) is also an interval
        SegmentTreeMin segMin = new SegmentTreeMin(prefix);
        SegmentTreeMax segMax = new SegmentTreeMax(prefix);

        long answer = Long.MIN_VALUE;

        // Valid segment must satisfy:
        // start >= L
        // end   <= n - 1 - R
        int globalMinStart = L;
        int globalMaxEnd = n - 1 - R;

        // Guaranteed at least one valid segment exists by the problem statement.
        // We now evaluate every index as the designated minimum.
        for (int i = 0; i < n; i++) {
            // Allowed left endpoints for segments where nums[i] is the designated minimum:
            // left must be:
            //   - within the interval where nums[i] remains minimum: [leftBound[i], i]
            //   - valid under guard rule: left >= L
            int leftStart = Math.max(leftBound[i], globalMinStart);
            int leftEnd = i;

            // Allowed right endpoints:
            //   - within the interval where nums[i] remains minimum: [i, rightBound[i]]
            //   - valid under guard rule: right <= n - 1 - R
            int rightStart = i;
            int rightEnd = Math.min(rightBound[i], globalMaxEnd);

            // If either side has no feasible endpoint, then no valid segment uses i as designated minimum.
            if (leftStart > leftEnd || rightStart > rightEnd) {
                continue;
            }

            // We need prefix[left] where left in [leftStart, leftEnd]
            int prefixLeftL = leftStart;
            int prefixLeftR = leftEnd;

            // We need prefix[right+1] where right in [rightStart, rightEnd]
            // Therefore prefix index is in [rightStart + 1, rightEnd + 1]
            int prefixRightL = rightStart + 1;
            int prefixRightR = rightEnd + 1;

            long minLeftPrefix = segMin.query(prefixLeftL, prefixLeftR);
            long maxLeftPrefix = segMax.query(prefixLeftL, prefixLeftR);
            long minRightPrefix = segMin.query(prefixRightL, prefixRightR);
            long maxRightPrefix = segMax.query(prefixRightL, prefixRightR);

            long value = nums[i];
            long bestSumForThisMinimum;

            // score = value * (prefix[right+1] - prefix[left])
            //
            // If value >= 0:
            //   maximize sum => maximize prefix[right+1] and minimize prefix[left]
            //
            // If value < 0:
            //   maximize value * sum => since value is negative, minimize sum
            //   => minimize prefix[right+1] and maximize prefix[left]
            if (value >= 0) {
                bestSumForThisMinimum = maxRightPrefix - minLeftPrefix;
            } else {
                bestSumForThisMinimum = minRightPrefix - maxLeftPrefix;
            }

            long score = value * bestSumForThisMinimum;
            answer = Math.max(answer, score);
        }

        return answer;
    }

    /**
     * Builds prefix sums for the array.
     *
     * prefix[0] = 0
     * prefix[i+1] = nums[0] + nums[1] + ... + nums[i]
     *
     * @param nums the input array
     * @return prefix sum array of length nums.length + 1
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long[] buildPrefixSums(int[] nums) {
        int n = nums.length;
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + nums[i];
        }
        return prefix;
    }

    /**
     * Computes the previous index with a strictly smaller value for every position.
     *
     * Example:
     * nums = [5, 2, 4, 3]
     * result might be [-1, -1, 1, 1]
     *
     * We use a monotonic increasing stack.
     * While stack top >= current value, pop.
     * Then the remaining top, if any, is strictly smaller.
     *
     * @param nums the input array
     * @return array prev where prev[i] is the previous index j < i with nums[j] < nums[i], or -1
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] previousStrictlySmaller(int[] nums) {
        int n = nums.length;
        int[] prev = new int[n];
        Arrays.fill(prev, -1);

        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && nums[stack.peek()] >= nums[i]) {
                stack.pop();
            }
            prev[i] = stack.isEmpty() ? -1 : stack.peek();
            stack.push(i);
        }

        return prev;
    }

    /**
     * Computes the next index with a smaller-or-equal value for every position.
     *
     * This tie-breaking pairs with previousStrictlySmaller() so that equal values are assigned
     * consistently to exactly one representative minimum index.
     *
     * We scan from right to left with a monotonic increasing stack.
     * While stack top > current value, pop.
     * Then the remaining top, if any, is <= current value.
     *
     * @param nums the input array
     * @return array next where next[i] is the next index j > i with nums[j] <= nums[i], or n
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] nextSmallerOrEqual(int[] nums) {
        int n = nums.length;
        int[] next = new int[n];
        Arrays.fill(next, n);

        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = n - 1; i >= 0; i--) {
            while (!stack.isEmpty() && nums[stack.peek()] > nums[i]) {
                stack.pop();
            }
            next[i] = stack.isEmpty() ? n : stack.peek();
            stack.push(i);
        }

        return next;
    }

    /**
     * Brute-force verifier for small arrays.
     * This is useful for demonstration and sanity checking.
     *
     * It checks every valid subarray, computes its minimum and sum directly,
     * and returns the best score.
     *
     * @param nums the input array
     * @param L minimum number of elements strictly to the left
     * @param R minimum number of elements strictly to the right
     * @return the exact answer by brute force
     * Time complexity: O(n^3) in the worst case
     * Space complexity: O(1) extra space
     */
    public long maximumScoreBruteForce(int[] nums, int L, int R) {
        int n = nums.length;
        long ans = Long.MIN_VALUE;

        for (int i = L; i < n; i++) {
            for (int j = i; j <= n - 1 - R; j++) {
                long sum = 0;
                int min = Integer.MAX_VALUE;
                for (int k = i; k <= j; k++) {
                    sum += nums[k];
                    min = Math.min(min, nums[k]);
                }
                ans = Math.max(ans, sum * (long) min);
            }
        }

        return ans;
    }

    /**
     * Demonstrates the solution on sample-style inputs and prints the results.
     *
     * Note:
     * The first example text in the prompt is internally inconsistent.
     * This main method prints the result according to the formal rule only.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(1) for the fixed demonstrations, excluding the solver calls
     * Space complexity: O(1) extra space, excluding the solver internals
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        int[] nums1 = {5, 2, 4, 3, 6};
        int L1 = 1, R1 = 1;
        long ans1 = sol.maximumScore(nums1, L1, R1);
        long brute1 = sol.maximumScoreBruteForce(nums1, L1, R1);
        System.out.println("Example 1 (formal rule) result: " + ans1);
        System.out.println("Example 1 brute-force check: " + brute1);

        int[] nums2 = {4, -1, 2, -2, 5};
        int L2 = 0, R2 = 0;
        long ans2 = sol.maximumScore(nums2, L2, R2);
        long brute2 = sol.maximumScoreBruteForce(nums2, L2, R2);
        System.out.println("Example 2 result: " + ans2);
        System.out.println("Example 2 brute-force check: " + brute2);

        int[] nums3 = {1};
        int L3 = 0, R3 = 0;
        System.out.println("Single element result: " + sol.maximumScore(nums3, L3, R3));

        int[] nums4 = {-3, -2, -5, -1};
        int L4 = 0, R4 = 0;
        System.out.println("All negative result: " + sol.maximumScore(nums4, L4, R4));
        System.out.println("All negative brute-force: " + sol.maximumScoreBruteForce(nums4, L4, R4));
    }

    /**
     * Segment tree supporting range minimum queries on a long array.
     */
    static class SegmentTreeMin {
        private final int n;
        private final long[] tree;

        /**
         * Builds a segment tree for range minimum queries.
         *
         * @param arr source array
         * @return nothing
         * Time complexity: O(n)
         * Space complexity: O(n)
         */
        SegmentTreeMin(long[] arr) {
            int size = 1;
            while (size < arr.length) {
                size <<= 1;
            }
            this.n = size;
            this.tree = new long[n << 1];
            Arrays.fill(tree, Long.MAX_VALUE);

            for (int i = 0; i < arr.length; i++) {
                tree[n + i] = arr[i];
            }
            for (int i = n - 1; i >= 1; i--) {
                tree[i] = Math.min(tree[i << 1], tree[i << 1 | 1]);
            }
        }

        /**
         * Returns the minimum value in arr[left..right], inclusive.
         *
         * @param left left index, inclusive
         * @param right right index, inclusive
         * @return minimum value in the range
         * Time complexity: O(log n)
         * Space complexity: O(1)
         */
        long query(int left, int right) {
            long res = Long.MAX_VALUE;
            int l = left + n;
            int r = right + n;

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
     * Segment tree supporting range maximum queries on a long array.
     */
    static class SegmentTreeMax {
        private final int n;
        private final long[] tree;

        /**
         * Builds a segment tree for range maximum queries.
         *
         * @param arr source array
         * @return nothing
         * Time complexity: O(n)
         * Space complexity: O(n)
         */
        SegmentTreeMax(long[] arr) {
            int size = 1;
            while (size < arr.length) {
                size <<= 1;
            }
            this.n = size;
            this.tree = new long[n << 1];
            Arrays.fill(tree, Long.MIN_VALUE);

            for (int i = 0; i < arr.length; i++) {
                tree[n + i] = arr[i];
            }
            for (int i = n - 1; i >= 1; i--) {
                tree[i] = Math.max(tree[i << 1], tree[i << 1 | 1]);
            }
        }

        /**
         * Returns the maximum value in arr[left..right], inclusive.
         *
         * @param left left index, inclusive
         * @param right right index, inclusive
         * @return maximum value in the range
         * Time complexity: O(log n)
         * Space complexity: O(1)
         */
        long query(int left, int right) {
            long res =