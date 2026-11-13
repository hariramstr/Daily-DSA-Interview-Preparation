import java.util.*;

/*
Problem Title: Maximum Score from Picking a Protected Middle Pair

Problem Description:
You are given an integer array nums representing values placed in a line. You want to choose two indices i and j such that i < j and there is at least one element strictly between them. The score of choosing this pair is defined as:

    nums[i] + nums[j] + min(nums[i+1..j-1])

In other words, the two chosen endpoints contribute their values, and the interior segment contributes its smallest value as a protection bonus.

Your task is to return the maximum possible score over all valid pairs.

A pair is valid only if j - i >= 2, because the middle segment must contain at least one element. The array may contain negative values, so the best answer is not necessarily formed by the largest endpoints alone. An efficient solution is expected for large inputs.

Constraints:
- 3 <= nums.length <= 200000
- -1000000000 <= nums[i] <= 1000000000

Example 1:
Input: nums = [5, 1, 4, 3]
Output: 10
Explanation:
Choose i = 0 and j = 2.
Score = nums[0] + nums[2] + min(nums[1]) = 5 + 4 + 1 = 10.

Example 2:
Input: nums = [2, 7, -3, 6, 1]
Output: 10
Explanation:
Choose i = 1 and j = 3.
Score = nums[1] + nums[3] + min(nums[2]) = 7 + 6 + (-3) = 10.

Goal:
Design an algorithm better than O(n^2).

Key Insight:
For any chosen pair (i, j), let k be the position of the minimum value inside the interior segment [i+1, j-1].
Then:
- k must satisfy i < k < j
- nums[k] is the minimum on that interior segment
- all values between i+1..j-1 are >= nums[k]

This means:
- on the left side, i must lie inside the maximal consecutive region ending at k-1 where every value is >= nums[k]
- on the right side, j must lie inside the maximal consecutive region starting at k+1 where every value is >= nums[k]

So if we treat nums[k] as the interior minimum, the best score using k as that minimum is:
    nums[k] + (best endpoint value on the left region) + (best endpoint value on the right region)

We can compute the maximal valid left/right regions for each k using monotonic stack boundaries:
- prevLess[k] = nearest index to the left with value < nums[k]
- nextLessOrEqual[k] = nearest index to the right with value <= nums[k]

Then valid endpoints are:
- i in [prevLess[k], k-1]
- j in [k+1, nextLessOrEqual[k]]

But because i and j must be outside k and the interior must contain k, the actual endpoint ranges become:
- left endpoint i in [prevLess[k], k-1]
  while ensuring all elements from i+1..k are >= nums[k]
  equivalently i >= prevLess[k]
- right endpoint j in [k+1, nextLessOrEqual[k]]
  while ensuring all elements from k..j-1 are >= nums[k]
  equivalently j <= nextLessOrEqual[k]

More precisely with sentinel boundaries:
- let L = index of previous element strictly less than nums[k]
- let R = index of next element less than or equal to nums[k]
Then:
- valid left endpoints are i in [L, k-1], but since k must be interior, i <= k-1 and i >= 0
- valid right endpoints are j in [k+1, R], but j < n

To maximize score for fixed k, we need:
- maximum nums[i] over i in [L, k-1]
- maximum nums[j] over j in [k+1, R]

This can be answered efficiently offline using a segment tree for range maximum queries.

The implementation below runs in O(n log n), which is efficient for n up to 200000.
*/
public class Solution {

    /**
     * Computes the maximum possible score:
     * nums[i] + nums[j] + min(nums[i+1..j-1])
     * over all valid pairs (i, j) such that j - i >= 2.
     *
     * The algorithm treats each index k as the chosen minimum inside the middle segment.
     * For that to be valid:
     * - there must be at least one endpoint on the left of k
     * - there must be at least one endpoint on the right of k
     * - every element between the chosen left endpoint and k must be >= nums[k]
     * - every element between k and the chosen right endpoint must be >= nums[k]
     *
     * We compute the maximal region where nums[k] can serve as the minimum using:
     * - previous strictly smaller element
     * - next smaller-or-equal element
     *
     * Then for each k, we query the maximum endpoint value on the left valid range
     * and the maximum endpoint value on the right valid range.
     *
     * @param nums the input array of values
     * @return the maximum score among all valid pairs
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long maximumScore(int[] nums) {
        int n = nums.length;

        // Step 1:
        // For each index k, compute the nearest index to the left whose value is strictly smaller.
        //
        // Why "strictly smaller" on the left?
        // We want a consistent tie-breaking rule for equal values so that every interval minimum
        // is assigned to exactly one representative index k.
        //
        // If there is no such index, we store -1.
        int[] prevLess = previousStrictlyLess(nums);

        // Step 2:
        // For each index k, compute the nearest index to the right whose value is smaller OR equal.
        //
        // Why "smaller or equal" on the right?
        // This complements the left rule and avoids double-counting intervals when equal values exist.
        //
        // If there is no such index, we store n.
        int[] nextLessOrEqual = nextLessOrEqual(nums);

        // Step 3:
        // Build a segment tree that supports range maximum queries on nums.
        //
        // We need this because for each k we must quickly find:
        // - the best left endpoint value in a valid left range
        // - the best right endpoint value in a valid right range
        //
        // A segment tree answers each such query in O(log n).
        SegmentTreeMax segTree = new SegmentTreeMax(nums);

        // Step 4:
        // Try every index k as the minimum element inside the middle segment.
        //
        // For fixed k:
        //   left endpoint i must satisfy:
        //     prevLess[k] <= i <= k - 1
        //   right endpoint j must satisfy:
        //     k + 1 <= j <= nextLessOrEqual[k]
        //
        // But array indices must remain within [0, n-1], so the actual valid ranges are:
        //   left range  = [max(0, prevLess[k]), k - 1]
        //   right range = [k + 1, min(n - 1, nextLessOrEqual[k])]
        //
        // Why is i allowed to be exactly prevLess[k]?
        // Because the interior segment is [i+1..j-1], so index i itself is NOT required to be >= nums[k].
        // Only the elements strictly between i and j matter for the minimum.
        //
        // Example:
        // If prevLess[k] is an index with a smaller value, choosing i = prevLess[k] is still valid,
        // because that smaller value is an endpoint, not part of the interior.
        long answer = Long.MIN_VALUE;

        for (int k = 1; k <= n - 2; k++) {
            // There must be at least one possible left endpoint and one possible right endpoint.
            int leftStart = Math.max(0, prevLess[k]);
            int leftEnd = k - 1;

            int rightStart = k + 1;
            int rightEnd = Math.min(n - 1, nextLessOrEqual[k]);

            // If either side has no valid endpoint, then k cannot be the interior minimum
            // of any valid pair.
            if (leftStart > leftEnd || rightStart > rightEnd) {
                continue;
            }

            // Query the best possible endpoint value on the left valid range.
            long bestLeft = segTree.query(leftStart, leftEnd);

            // Query the best possible endpoint value on the right valid range.
            long bestRight = segTree.query(rightStart, rightEnd);

            // Score when nums[k] is the chosen interior minimum.
            long candidate = bestLeft + bestRight + nums[k];

            // Update the global best answer.
            answer = Math.max(answer, candidate);
        }

        return answer;
    }

    /**
     * Computes, for every index i, the nearest index to the left with a strictly smaller value.
     *
     * If no such index exists, the result is -1 for that position.
     *
     * We use a monotonic increasing stack of indices.
     * While the top of the stack has value >= nums[i], it cannot be the previous strictly smaller
     * element for i, so we remove it.
     *
     * @param nums the input array
     * @return an array prevLess where prevLess[i] is the nearest index j < i with nums[j] < nums[i], or -1
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] previousStrictlyLess(int[] nums) {
        int n = nums.length;
        int[] prevLess = new int[n];
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            // Remove all indices whose values are >= nums[i],
            // because they cannot be strictly smaller than nums[i].
            while (!stack.isEmpty() && nums[stack.peek()] >= nums[i]) {
                stack.pop();
            }

            // After removals:
            // - top of stack is the nearest index to the left with value < nums[i]
            // - or no such index exists
            prevLess[i] = stack.isEmpty() ? -1 : stack.peek();

            // Push current index for future positions.
            stack.push(i);
        }

        return prevLess;
    }

    /**
     * Computes, for every index i, the nearest index to the right with a value less than or equal to nums[i].
     *
     * If no such index exists, the result is nums.length for that position.
     *
     * We use a monotonic increasing stack from right to left.
     * While the top of the stack has value > nums[i], it cannot be the next less-or-equal element
     * for i, so we remove it.
     *
     * @param nums the input array
     * @return an array nextLessOrEqual where nextLessOrEqual[i] is the nearest index j > i with nums[j] <= nums[i], or n
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] nextLessOrEqual(int[] nums) {
        int n = nums.length;
        int[] nextLessOrEqual = new int[n];
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = n - 1; i >= 0; i--) {
            // Remove all indices whose values are > nums[i],
            // because they are not <= nums[i].
            while (!stack.isEmpty() && nums[stack.peek()] > nums[i]) {
                stack.pop();
            }

            // After removals:
            // - top of stack is the nearest index to the right with value <= nums[i]
            // - or no such index exists
            nextLessOrEqual[i] = stack.isEmpty() ? n : stack.peek();

            // Push current index for future positions to the left.
            stack.push(i);
        }

        return nextLessOrEqual;
    }

    /**
     * A segment tree for range maximum queries on an integer array.
     *
     * This structure allows us to quickly ask:
     * "What is the maximum value in nums[left..right]?"
     *
     * That is exactly what we need when selecting the best endpoint value
     * from a valid left or right range.
     */
    static class SegmentTreeMax {
        private final long[] tree;
        private final int n;

        /**
         * Builds the segment tree from the given array.
         *
         * @param nums the input array
         * Time complexity: O(n)
         * Space complexity: O(n)
         */
        SegmentTreeMax(int[] nums) {
            this.n = nums.length;
            this.tree = new long[4 * n];
            build(1, 0, n - 1, nums);
        }

        /**
         * Recursively builds the segment tree.
         *
         * @param node current tree node index
         * @param left left boundary of the represented segment
         * @param right right boundary of the represented segment
         * @param nums original input array
         * Time complexity: O(n)
         * Space complexity: O(log n) recursion stack
         */
        private void build(int node, int left, int right, int[] nums) {
            if (left == right) {
                tree[node] = nums[left];
                return;
            }

            int mid = left + (right - left) / 2;

            build(node * 2, left, mid, nums);
            build(node * 2 + 1, mid + 1, right, nums);

            tree[node] = Math.max(tree[node * 2], tree[node * 2 + 1]);
        }

        /**
         * Returns the maximum value in the inclusive range [queryLeft, queryRight].
         *
         * @param queryLeft left boundary of query
         * @param queryRight right boundary of query
         * @return maximum value in nums[queryLeft..queryRight]
         * Time complexity: O(log n)
         * Space complexity: O(log n) recursion stack
         */
        public long query(int queryLeft, int queryRight) {
            return query(1, 0, n - 1, queryLeft, queryRight);
        }

        /**
         * Recursive helper for range maximum query.
         *
         * @param node current tree node index
         * @param left left boundary of current segment
         * @param right right boundary of current segment
         * @param queryLeft left boundary of query
         * @param queryRight right boundary of query
         * @return maximum value in the overlap between current segment and query range
         * Time complexity: O(log n)
         * Space complexity: O(log n) recursion stack
         */
        private long query(int node, int left, int right, int queryLeft, int queryRight) {
            // No overlap.
            if (right < queryLeft || left > queryRight) {
                return Long.MIN_VALUE;
            }

            // Full overlap.
            if (queryLeft <= left && right <= queryRight) {
                return tree[node];
            }

            // Partial overlap: split into children.
            int mid = left + (right - left) / 2;
            long leftMax = query(node * 2, left, mid, queryLeft, queryRight);
            long rightMax = query(node * 2 + 1, mid + 1, right, queryLeft, queryRight);

            return Math.max(leftMax, rightMax);
        }
    }

    /**
     * A simple brute-force method used only for small arrays or verification.
     *
     * This checks every valid pair (i, j), computes the interior minimum directly,
     * and returns the best score.
     *
     * This method is not efficient for large inputs, but it is useful for testing
     * and confirming correctness on examples.
     *
     * @param nums the input array
     * @return the maximum score by exhaustive search
     * Time complexity: O(n^3) in the straightforward implementation below
     * Space complexity: O(1)
     */
    public long maximumScoreBruteForce(int[] nums) {
        int n = nums.length;
        long answer = Long.MIN_VALUE;

        for (int i = 0; i < n; i++) {
            for (int j = i + 2; j < n; j++) {
                int minInside = Integer.MAX_VALUE;
                for (int k = i + 1; k <= j - 1; k++) {
                    minInside = Math.min(minInside, nums[k]);
                }
                long score = (long) nums[i] + nums[j] + minInside;
                answer = Math.max(answer, score);
            }
        }

        return answer;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and prints the results.
     *
     * Also prints brute-force results for the same samples so that the outputs
     * can be visually verified to match.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: Depends on the demonstration inputs
     * Space complexity: Depends on the demonstration inputs
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] nums1 = {5, 1, 4, 3};
        long result1 = solution.maximumScore(nums1);
        long brute1 = solution.maximumScoreBruteForce(nums1);

        System.out.println("Example 1:");
        System.out.println("Input: " + Arrays.toString(nums1));
        System.out.println("Output: " + result1);
        System.out.println("Brute-force check: " + brute1);
        System.out.println("Expected: 10");
        System.out.println();

        int[] nums2 = {2, 7, -3, 6, 1};
        long result2 = solution.maximumScore(nums2);
        long brute2 = solution.maximumScoreBruteForce(nums2);

        System.out.println("Example 2:");
        System.out.println("Input: " + Arrays.toString(nums2));
        System.out.println("Output: " + result2);
        System.out.println("Brute-force check: " + brute2);
        System.out.println("Expected: 10");
        System.out.println();

        int[] extra1 = {1, 2, 3};
        System.out.println("Extra Test 1:");
        System.out.println("Input: " + Arrays.toString(extra1));
        System.out.println("Output: " + solution.maximumScore(extra1));
        System.out.println("Brute-force check: " + solution.maximumScoreBruteForce(extra1));
        System.out.println();

        int[] extra2 = {-5, -2, -7, -1, -3};
        System.out.println