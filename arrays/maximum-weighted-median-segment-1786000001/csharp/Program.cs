/*
Title: Maximum Weighted Median Segment
Difficulty: Hard
Topic: Arrays

Problem Description:
You are given two integer arrays of equal length, values and weights, where values[i] is the score of the i-th event and weights[i] is its importance. For any contiguous subarray values[l..r], define its weighted median as the smallest number x such that the total weight of elements in the subarray with value <= x is at least half of the total subarray weight, rounded up. The score of a segment is this weighted median.

Your task is to find the maximum possible score among all contiguous subarrays.

In other words, among every non-empty subarray, compute its weighted median, and return the largest weighted median that can appear.

This problem is harder than simply taking the maximum element, because a very large value can fail to be the weighted median of a segment if too much total weight lies on smaller values inside that segment. An efficient solution should exploit the structure of medians and avoid enumerating all O(n^2) subarrays.

Constraints:
- 1 <= n <= 200000
- 1 <= values[i] <= 1000000000
- 1 <= weights[i] <= 1000000000
- values.length == weights.length

Example 1:
Input: values = [4, 1, 7, 3], weights = [2, 5, 4, 1]
Output: 7
Explanation: The subarray [7] has total weight 4, so its weighted median is 7. No segment can have weighted median larger than 7, so the answer is 7.

Example 2:
Input: values = [5, 2, 5, 1, 4], weights = [1, 10, 1, 1, 1]
Output: 5
Explanation: The single-element subarray [5] has weighted median 5. Although the heavy weight 10 on value 2 dominates many larger segments, we only need one segment whose weighted median is as large as possible. Therefore the answer is still 5.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Important observation:
    Because we are allowed to choose ANY non-empty contiguous subarray, a subarray of length 1 is always valid.
    For a single-element subarray [values[i]] with weight weights[i]:
      - total weight = weights[i]
      - half rounded up = weights[i]
      - the smallest x such that weight of elements <= x is at least weights[i] is exactly values[i]
    So every individual value can appear as a weighted median (by choosing its one-element segment).

    Therefore:
      - The answer is at least max(values), because the segment containing only that maximum value has weighted median equal to it.
      - No weighted median of any segment can ever exceed the maximum element inside that segment, and therefore cannot exceed max(values) overall.

    Combining both facts:
      answer = max(values)

    So the problem has a surprisingly simple correct solution.
    */
    public int MaximumWeightedMedianSegment(int[] values, int[] weights)
    {
        // Step 1:
        // We validate the input shape.
        // The problem guarantees equal lengths, but checking makes the method safer and clearer.
        if (values == null || weights == null || values.Length == 0 || values.Length != weights.Length)
        {
            throw new ArgumentException("values and weights must be non-null, non-empty, and have the same length.");
        }

        // Step 2:
        // We will scan through the values array and keep track of the largest value seen so far.
        //
        // Why is this enough?
        // Because any single element forms a contiguous subarray by itself.
        // The weighted median of a one-element subarray is that element's value.
        // So the largest value in the entire array is definitely achievable as a weighted median.
        //
        // Also, no weighted median can be larger than the largest value present in the chosen segment,
        // and therefore cannot be larger than the largest value in the whole array.
        //
        // This means the maximum value is both:
        //   1) achievable
        //   2) an upper bound
        // Hence it is the exact answer.
        int answer = values[0];

        // Step 3:
        // Standard linear scan to compute the maximum element.
        // This uses O(1) extra space and O(n) time.
        for (int i = 1; i < values.Length; i++)
        {
            // If the current value is larger than our best answer so far,
            // update the answer.
            if (values[i] > answer)
            {
                answer = values[i];
            }
        }

        // Step 4:
        // Return the largest value, which is the maximum possible weighted median segment score.
        return answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] values1 = { 4, 1, 7, 3 };
int[] weights1 = { 2, 5, 4, 1 };
int result1 = solution.MaximumWeightedMedianSegment(values1, weights1);
Console.WriteLine(result1); // Expected: 7

// Example 2
int[] values2 = { 5, 2, 5, 1, 4 };
int[] weights2 = { 1, 10, 1, 1, 1 };
int result2 = solution.MaximumWeightedMedianSegment(values2, weights2);
Console.WriteLine(result2); // Expected: 5

// Additional quick demo
int[] values3 = { 8 };
int[] weights3 = { 100 };
int result3 = solution.MaximumWeightedMedianSegment(values3, weights3);
Console.WriteLine(result3); // Expected: 8