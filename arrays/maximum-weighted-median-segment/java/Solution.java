import java.util.*;

/*
Problem Title: Maximum Weighted Median Segment

Problem Description:
You are given two integer arrays of equal length, values and weights, where values[i] is the score recorded at position i
and weights[i] is the importance of that position. You must choose one contiguous subarray values[l..r]. The score of a
chosen subarray is defined as the weighted median of its elements, using the corresponding weights weights[l..r].

A number x is a weighted median of the subarray if:
1. The total weight of elements strictly smaller than x is less than half of the subarray's total weight, and
2. The total weight of elements strictly greater than x is also less than half of the subarray's total weight.

If multiple values satisfy this condition, the weighted median of the subarray is the smallest such value.

Your task is to return the maximum possible weighted median among all contiguous subarrays.

Constraints:
- 1 <= n == values.length == weights.length <= 200000
- 1 <= values[i] <= 1000000000
- 1 <= weights[i] <= 1000000000
- The answer must fit in a 64-bit signed integer

Key Observation:
A single-element subarray [values[i]] always has weighted median equal to values[i], because:
- Weight of elements smaller than values[i] inside that subarray is 0
- Weight of elements greater than values[i] inside that subarray is 0
- Both are strictly less than half of the total weight of that one-element subarray

Therefore every array value is achievable as the weighted median of some contiguous subarray.
Hence the maximum possible weighted median among all contiguous subarrays is simply the maximum element in values.

So the problem reduces to finding max(values[i]).

Example 1:
values = [4, 1, 7, 3], weights = [2, 5, 3, 1]
Single-element subarray [7] has weighted median 7, so answer is 7.

Example 2:
values = [5, 2, 6, 2, 4], weights = [1, 4, 2, 3, 5]
Single-element subarray [6] has weighted median 6, so answer is 6.
*/

public class Solution {

    /**
     * Returns the maximum possible weighted median among all contiguous subarrays.
     *
     * Very important simplification:
     * Any single-element subarray [values[i]] has weighted median values[i].
     * Therefore every value in the array is achievable.
     * Since no subarray can have weighted median larger than the maximum value present in that subarray,
     * and every subarray value comes from the original array, the global answer is simply the maximum value
     * in the entire values array.
     *
     * @param values the array of values/scores
     * @param weights the array of weights/importances; same length as values
     * @return the maximum possible weighted median among all contiguous subarrays
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long maximumWeightedMedianSegment(int[] values, int[] weights) {
        validateInput(values, weights);

        // We scan once and keep the largest value seen so far.
        long answer = Long.MIN_VALUE;

        // Step-by-step:
        // 1. Every values[i] is achievable by choosing the subarray [i..i].
        // 2. So the best answer must be the largest values[i].
        // 3. We compute that maximum directly.
        for (int value : values) {
            if (value > answer) {
                answer = value;
            }
        }

        return answer;
    }

    /**
     * Validates the input arrays.
     *
     * @param values the values array
     * @param weights the weights array
     * @return nothing; throws exception if input is invalid
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
            throw new IllegalArgumentException("Input arrays must not be empty.");
        }
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments, unused
     * @return nothing
     * Time complexity: O(n) per demonstration call
     * Space complexity: O(1) excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] values1 = {4, 1, 7, 3};
        int[] weights1 = {2, 5, 3, 1};
        long result1 = solution.maximumWeightedMedianSegment(values1, weights1);
        System.out.println(result1); // Expected: 7

        int[] values2 = {5, 2, 6, 2, 4};
        int[] weights2 = {1, 4, 2, 3, 5};
        long result2 = solution.maximumWeightedMedianSegment(values2, weights2);
        System.out.println(result2); // Expected: 6

        // Additional quick sanity check:
        int[] values3 = {1};
        int[] weights3 = {100};
        long result3 = solution.maximumWeightedMedianSegment(values3, weights3);
        System.out.println(result3); // Expected: 1
    }
}