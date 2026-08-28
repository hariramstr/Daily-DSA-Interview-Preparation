import java.util.*;

/*
Problem Title: Maximum Weighted Median Segment

Problem Description:
You are given two integer arrays of equal length, values and weights, where values[i] is the score of the i-th event
and weights[i] is its importance. For any contiguous subarray values[l..r], define its weighted median as the smallest
number x such that the total weight of elements in the subarray with value <= x is at least half of the total subarray
weight, rounded up. The score of a segment is this weighted median.

Your task is to find the maximum possible score among all contiguous subarrays.

In other words, among every non-empty subarray, compute its weighted median, and return the largest weighted median
that can appear.

This problem is harder than simply taking the maximum element, because a very large value can fail to be the weighted
median of a segment if too much total weight lies on smaller values inside that segment. An efficient solution should
exploit the structure of medians and avoid enumerating all O(n^2) subarrays.

Constraints:
- 1 <= n <= 200000
- 1 <= values[i] <= 1000000000
- 1 <= weights[i] <= 1000000000
- values.length == weights.length

Example 1:
Input: values = [4, 1, 7, 3], weights = [2, 5, 4, 1]
Output: 7

Example 2:
Input: values = [5, 2, 5, 1, 4], weights = [1, 10, 1, 1, 1]
Output: 5
*/

public class Solution {

    /**
     * Computes the maximum possible weighted median among all non-empty contiguous subarrays.
     *
     * Important observation:
     * A single-element subarray [values[i]] always has weighted median values[i], because its entire weight
     * belongs to that one value. Therefore every array element is achievable as the weighted median of some segment.
     * Since no segment can have weighted median larger than the maximum element present in the array, the answer is
     * simply the maximum value in the array.
     *
     * This is fully correct for the given definition of weighted median and contiguous subarrays, because
     * single-element subarrays are allowed and non-empty.
     *
     * @param values the values of the events
     * @param weights the weights / importances of the events; must have the same length as values
     * @return the largest weighted median that can appear among all contiguous non-empty subarrays
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public int maximumWeightedMedianSegment(int[] values, int[] weights) {
        validateInput(values, weights);

        // Since every single element forms a valid subarray of length 1,
        // and the weighted median of [values[i]] is exactly values[i],
        // the best possible answer is simply the maximum element in values.
        int answer = values[0];
        for (int i = 1; i < values.length; i++) {
            answer = Math.max(answer, values[i]);
        }
        return answer;
    }

    /**
     * Alias method with a shorter name for convenience.
     *
     * @param values the values array
     * @param weights the weights array
     * @return the maximum possible weighted median among all contiguous non-empty subarrays
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public int solve(int[] values, int[] weights) {
        return maximumWeightedMedianSegment(values, weights);
    }

    /**
     * Validates the input arrays according to the problem statement.
     *
     * @param values the values array
     * @param weights the weights array
     * @return nothing; throws an exception if input is invalid
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public void validateInput(int[] values, int[] weights) {
        if (values == null || weights == null) {
            throw new IllegalArgumentException("Input arrays must not be null.");
        }
        if (values.length != weights.length) {
            throw new IllegalArgumentException("values and weights must have the same length.");
        }
        if (values.length == 0) {
            throw new IllegalArgumentException("Arrays must be non-empty.");
        }
    }

    /**
     * Computes the weighted median of a specific subarray [left, right], inclusive.
     * This helper is used only for demonstration / verification on small examples.
     *
     * Definition used:
     * The weighted median is the smallest x such that the total weight of elements with value <= x
     * is at least ceil(totalWeight / 2).
     *
     * Implementation idea:
     * 1. Collect (value, weight) pairs from the chosen subarray.
     * 2. Sort them by value ascending.
     * 3. Scan cumulative weight until reaching the threshold ceil(totalWeight / 2).
     * 4. Return the corresponding value.
     *
     * @param values the values array
     * @param weights the weights array
     * @param left left boundary of the subarray, inclusive
     * @param right right boundary of the subarray, inclusive
     * @return the weighted median of the chosen subarray
     * Time complexity: O(k log k), where k = right - left + 1
     * Space complexity: O(k)
     */
    public int weightedMedianOfSubarray(int[] values, int[] weights, int left, int right) {
        validateInput(values, weights);
        if (left < 0 || right >= values.length || left > right) {
            throw new IllegalArgumentException("Invalid subarray boundaries.");
        }

        int length = right - left + 1;
        int[][] pairs = new int[length][2];
        long totalWeight = 0L;

        for (int i = left, idx = 0; i <= right; i++, idx++) {
            pairs[idx][0] = values[i];
            pairs[idx][1] = weights[i];
            totalWeight += weights[i];
        }

        Arrays.sort(pairs, Comparator.comparingInt(a -> a[0]));

        long need = (totalWeight + 1L) / 2L;
        long prefix = 0L;

        for (int[] pair : pairs) {
            prefix += pair[1];
            if (prefix >= need) {
                return pair[0];
            }
        }

        throw new IllegalStateException("Weighted median computation failed unexpectedly.");
    }

    /**
     * Brute-force method for small arrays only.
     * It enumerates every non-empty contiguous subarray, computes its weighted median,
     * and returns the maximum among them.
     *
     * This is useful for educational verification and testing the main insight.
     *
     * @param values the values array
     * @param weights the weights array
     * @return the exact answer by brute force
     * Time complexity: O(n^3 log n) in the straightforward implementation here
     * Space complexity: O(n)
     */
    public int bruteForceMaximumWeightedMedianSegment(int[] values, int[] weights) {
        validateInput(values, weights);

        int n = values.length;
        int best = Integer.MIN_VALUE;

        for (int left = 0; left < n; left++) {
            for (int right = left; right < n; right++) {
                int median = weightedMedianOfSubarray(values, weights, left, right);
                best = Math.max(best, median);
            }
        }

        return best;
    }

    /**
     * Demonstrates the solution on the sample inputs and also verifies them with brute force.
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(1) for the fixed demo size
     * Space complexity: O(1) excluding small demo data
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] values1 = {4, 1, 7, 3};
        int[] weights1 = {2, 5, 4, 1};
        int result1 = solution.maximumWeightedMedianSegment(values1, weights1);
        int brute1 = solution.bruteForceMaximumWeightedMedianSegment(values1, weights1);

        System.out.println("Example 1 result: " + result1);
        System.out.println("Example 1 brute-force check: " + brute1);
        System.out.println("Example 1 expected: 7");

        int[] values2 = {5, 2, 5, 1, 4};
        int[] weights2 = {1, 10, 1, 1, 1};
        int result2 = solution.maximumWeightedMedianSegment(values2, weights2);
        int brute2 = solution.bruteForceMaximumWeightedMedianSegment(values2, weights2);

        System.out.println("Example 2 result: " + result2);
        System.out.println("Example 2 brute-force check: " + brute2);
        System.out.println("Example 2 expected: 5");

        // Additional tiny demonstration:
        // Single-element subarray [7] has weighted median 7, so the global answer can be 7.
        int singleMedian = solution.weightedMedianOfSubarray(values1, weights1, 2, 2);
        System.out.println("Weighted median of subarray [7]: " + singleMedian);
    }
}