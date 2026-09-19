import java.util.*;

/*
Title: Maximum Score from Choosing a Guarded Middle Segment

Problem Description:
You are given an integer array nums of length n and a non-negative integer penalty.
You must choose one contiguous subarray nums[l..r] as your final segment.

The score of a chosen segment is defined as:

(minimum value inside the segment) * (length of the segment)
- penalty * (number of elements outside the segment that are strictly smaller than that minimum value).

In other words, the segment earns a base score equal to its minimum multiplied by its length,
but it is penalized for every value outside the segment that is smaller than the segment minimum.
The outside elements may appear on either side of the segment.

Return the maximum possible score over all non-empty contiguous subarrays.

Constraints:
- 1 <= n <= 200000
- 1 <= nums[i] <= 1000000000
- 0 <= penalty <= 1000000000
- The answer fits in a signed 64-bit integer.

Key Insight:
For any chosen segment, let its minimum be m.
Among all segments whose minimum is exactly m and that contain a particular occurrence of m,
the best one is the largest segment where every value is >= m, because:
- enlarging the segment increases length,
- enlarging the segment does not change the minimum below m,
- and any element added from inside that maximal valid span is not smaller than m,
  so it does not increase the outside-smaller count.

Therefore, for each index i, if nums[i] is treated as the controlling minimum,
we only need to evaluate the maximal span [L+1, R-1] where every element is >= nums[i].
This is the classic "largest span where nums[i] is the minimum" idea using previous/next strictly smaller elements.

Then the score for index i becomes:
nums[i] * spanLength - penalty * (count of all array elements strictly smaller than nums[i])

Why is the penalty term global?
Because every element strictly smaller than nums[i] cannot lie inside the maximal span:
if it did, nums[i] would not be the minimum there.
So every globally smaller element is automatically outside that segment.

Thus the problem reduces to:
1. For each index i, find the maximal span where nums[i] is the minimum.
2. For each value nums[i], count how many array elements are globally strictly smaller than it.
3. Compute the score and take the maximum.

This yields an O(n log n) solution:
- O(n) for monotonic stack boundaries,
- O(n log n) for coordinate compression + Fenwick tree / sorted unique values.
*/
public class Solution {

    /**
     * Computes the maximum possible score over all non-empty contiguous subarrays.
     *
     * Core idea:
     * 1. For each index i, determine the largest contiguous segment containing i
     *    in which nums[i] remains a minimum value.
     *    This is done by finding:
     *    - previous index with value strictly smaller than nums[i]
     *    - next index with value strictly smaller than nums[i]
     *
     * 2. The maximal valid segment for i is then:
     *      (prevSmaller[i] + 1) ... (nextSmaller[i] - 1)
     *    and its length is:
     *      nextSmaller[i] - prevSmaller[i] - 1
     *
     * 3. Every element in the whole array that is strictly smaller than nums[i]
     *    must lie outside that maximal segment, because such an element cannot be inside
     *    any segment whose minimum is nums[i].
     *
     * 4. Therefore the best score contributed by index i is:
     *      nums[i] * spanLength - penalty * countStrictlySmaller(nums[i])
     *
     * 5. Take the maximum over all i.
     *
     * @param nums the input array of positive integers
     * @param penalty the non-negative penalty applied per outside element strictly smaller than the segment minimum
     * @return the maximum score achievable by choosing one non-empty contiguous subarray
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long maximumScore(int[] nums, int penalty) {
        int n = nums.length;

        // Step 1:
        // For every index, compute the nearest strictly smaller element on the left.
        // If none exists, store -1.
        int[] prevSmaller = computePreviousStrictlySmaller(nums);

        // Step 2:
        // For every index, compute the nearest strictly smaller element on the right.
        // If none exists, store n.
        int[] nextSmaller = computeNextStrictlySmaller(nums);

        // Step 3:
        // For every value nums[i], compute how many elements in the entire array
        // are strictly smaller than nums[i].
        //
        // We do this efficiently using coordinate compression:
        // - sort unique values
        // - the compressed rank of nums[i] tells us how many unique values are smaller
        // - but we need total element count, not unique count
        // So we build frequency counts and prefix sums over compressed values.
        long[] smallerCount = computeGlobalStrictlySmallerCounts(nums);

        // Step 4:
        // Evaluate the score for each index using its maximal span.
        long answer = Long.MIN_VALUE;
        long p = penalty;

        for (int i = 0; i < n; i++) {
            // The maximal segment where nums[i] can serve as a minimum.
            long spanLength = (long) nextSmaller[i] - prevSmaller[i] - 1L;

            // Base score = minimum * length
            long base = (long) nums[i] * spanLength;

            // Penalty = penalty * number of globally smaller elements
            long cost = p * smallerCount[i];

            long score = base - cost;
            if (score > answer) {
                answer = score;
            }
        }

        return answer;
    }

    /**
     * Computes the previous index with a strictly smaller value for every position.
     *
     * Example:
     * nums = [5, 2, 4, 3]
     * prevSmaller = [-1, -1, 1, 1]
     *
     * Explanation:
     * - For 5, nothing smaller on the left.
     * - For 2, nothing smaller on the left.
     * - For 4, index 1 has value 2, which is strictly smaller.
     * - For 3, index 1 has value 2, which is strictly smaller.
     *
     * Monotonic stack invariant:
     * The stack stores indices in increasing order of values after popping all values >= current.
     * This guarantees the top is the nearest strictly smaller element.
     *
     * @param nums the input array
     * @return an array prev where prev[i] is the nearest index j < i with nums[j] < nums[i], or -1 if none exists
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] computePreviousStrictlySmaller(int[] nums) {
        int n = nums.length;
        int[] prev = new int[n];
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            // Remove all elements that are >= current value.
            // Why >= ?
            // Because we need the nearest STRICTLY smaller element.
            // Any equal value cannot serve as a boundary that proves current is the minimum.
            while (!stack.isEmpty() && nums[stack.peek()] >= nums[i]) {
                stack.pop();
            }

            prev[i] = stack.isEmpty() ? -1 : stack.peek();

            // Push current index for future elements.
            stack.push(i);
        }

        return prev;
    }

    /**
     * Computes the next index with a strictly smaller value for every position.
     *
     * Example:
     * nums = [5, 2, 4, 3]
     * nextSmaller = [1, 4, 3, 4]
     *
     * Explanation:
     * - For 5, index 1 has value 2, which is strictly smaller.
     * - For 2, no smaller value to the right.
     * - For 4, index 3 has value 3, which is strictly smaller.
     * - For 3, no smaller value to the right.
     *
     * We scan from right to left with the same monotonic stack idea.
     *
     * @param nums the input array
     * @return an array next where next[i] is the nearest index j > i with nums[j] < nums[i], or nums.length if none exists
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] computeNextStrictlySmaller(int[] nums) {
        int n = nums.length;
        int[] next = new int[n];
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = n - 1; i >= 0; i--) {
            // Remove all elements that are >= current value,
            // because we need the nearest STRICTLY smaller element.
            while (!stack.isEmpty() && nums[stack.peek()] >= nums[i]) {
                stack.pop();
            }

            next[i] = stack.isEmpty() ? n : stack.peek();

            stack.push(i);
        }

        return next;
    }

    /**
     * For each index i, computes how many elements in the entire array are strictly smaller than nums[i].
     *
     * Detailed approach:
     * 1. Copy and sort the array.
     * 2. Extract unique values.
     * 3. Count frequency of each unique value.
     * 4. Build prefix sums of frequencies.
     * 5. For each nums[i], locate its compressed position.
     *    The number of strictly smaller elements is the prefix sum before that position.
     *
     * Example:
     * nums = [7, 1, 6, 5, 2]
     * sorted unique = [1, 2, 5, 6, 7]
     * frequencies    [1, 1, 1, 1, 1]
     * prefix sums    [1, 2, 3, 4, 5]
     *
     * strictly smaller counts:
     * 7 -> 4
     * 1 -> 0
     * 6 -> 3
     * 5 -> 2
     * 2 -> 1
     *
     * @param nums the input array
     * @return an array smallerCount where smallerCount[i] is the number of array elements strictly smaller than nums[i]
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long[] computeGlobalStrictlySmallerCounts(int[] nums) {
        int n = nums.length;

        int[] sorted = nums.clone();
        Arrays.sort(sorted);

        // Build unique values and their frequencies.
        int[] unique = new int[n];
        int[] freq = new int[n];
        int m = 0;

        for (int value : sorted) {
            if (m == 0 || unique[m - 1] != value) {
                unique[m] = value;
                freq[m] = 1;
                m++;
            } else {
                freq[m - 1]++;
            }
        }

        // Prefix sums of frequencies:
        // prefix[k] = total number of elements among unique[0..k]
        long[] prefix = new long[m];
        prefix[0] = freq[0];
        for (int i = 1; i < m; i++) {
            prefix[i] = prefix[i - 1] + freq[i];
        }

        long[] result = new long[n];

        for (int i = 0; i < n; i++) {
            int pos = lowerBound(unique, m, nums[i]);

            // Number of strictly smaller elements is everything before pos.
            result[i] = (pos == 0) ? 0L : prefix[pos - 1];
        }

        return result;
    }

    /**
     * Standard lower bound:
     * returns the first index in arr[0..length) whose value is >= target.
     *
     * Since target is guaranteed to exist in the compressed unique array when used here,
     * this effectively returns the exact position of target.
     *
     * @param arr the sorted array of unique values
     * @param length the valid length inside arr
     * @param target the value to search
     * @return the first index with arr[index] >= target
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public int lowerBound(int[] arr, int length, int target) {
        int left = 0;
        int right = length;

        while (left < right) {
            int mid = left + (right - left) / 2;
            if (arr[mid] >= target) {
                right = mid;
            } else {
                left = mid + 1;
            }
        }

        return left;
    }

    /**
     * Demonstrates the solution on sample-style inputs.
     *
     * Note:
     * The second example in the prompt contains an inconsistent explanation.
     * This program prints the mathematically correct result according to the stated scoring rule.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding called method costs
     * Space complexity: O(1) for the demonstration itself, excluding called method costs
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] nums1 = {5, 2, 4, 3};
        int penalty1 = 2;
        long result1 = solution.maximumScore(nums1, penalty1);
        System.out.println("Example 1 result: " + result1);

        int[] nums2 = {7, 1, 6, 5, 2};
        int penalty2 = 3;
        long result2 = solution.maximumScore(nums2, penalty2);
        System.out.println("Example 2 result (according to the stated formula): " + result2);

        int[] extra1 = {7};
        int penaltyExtra1 = 3;
        System.out.println("Single element [7], penalty 3: " + solution.maximumScore(extra1, penaltyExtra1));

        int[] extra2 = {3, 3, 3};
        int penaltyExtra2 = 5;
        System.out.println("[3,3,3], penalty 5: " + solution.maximumScore(extra2, penaltyExtra2));

        int[] extra3 = {1, 2, 3, 4};
        int penaltyExtra3 = 1;
        System.out.println("[1,2,3,4], penalty 1: " + solution.maximumScore(extra3, penaltyExtra3));
    }
}