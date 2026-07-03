/*
Title: Maximum Weighted Median Segment
Difficulty: Hard
Topic: Arrays

Problem Description:
You are given two integer arrays of equal length, values and weights, where values[i] is the score recorded at position i and weights[i] is the importance of that position. You must choose one contiguous subarray values[l..r]. The score of a chosen subarray is defined as the weighted median of its elements, using the corresponding weights weights[l..r]. A number x is a weighted median of the subarray if the total weight of elements strictly smaller than x is less than half of the subarray's total weight, and the total weight of elements strictly greater than x is also less than half of the subarray's total weight. If multiple values satisfy this condition, the weighted median of the subarray is the smallest such value.

Your task is to return the maximum possible weighted median among all contiguous subarrays.

In other words, among every possible subarray, compute its weighted median, and find the largest median value that can appear.

Constraints:
- 1 <= n == values.length == weights.length <= 200000
- 1 <= values[i] <= 1000000000
- 1 <= weights[i] <= 1000000000
- The answer must fit in a 64-bit signed integer

Key observation:
Because a single-element subarray [values[i]] always has weighted median equal to values[i],
the maximum possible weighted median among all contiguous subarrays is simply the maximum
element present in the array.

Why?
- Any weighted median of any subarray must be one of the values inside that subarray.
- Therefore no subarray can have weighted median larger than the global maximum value.
- The single-element subarray containing the global maximum achieves that value exactly.

So although the statement hints at a more advanced approach, the mathematically correct answer
for the problem exactly as written is just max(values).
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1) extra space

    We scan the array once and keep track of the largest value seen so far.
    That largest value is the answer because:
    1. No weighted median can exceed the maximum value present in the chosen subarray.
    2. A single-element subarray [values[i]] has weighted median values[i].
    3. Therefore choosing the position of the global maximum achieves the best possible answer.
    */
    public long MaximumWeightedMedianSegment(int[] values, int[] weights)
    {
        // Defensive check:
        // The problem guarantees valid input, but this makes the method safer and clearer.
        if (values == null || weights == null || values.Length != weights.Length || values.Length == 0)
            throw new ArgumentException("values and weights must be non-null, non-empty, and have the same length.");

        // Step 1:
        // Initialize the answer with the first value.
        // We use long as the return type because the statement says the answer must fit in 64-bit signed integer.
        long maxValue = values[0];

        // Step 2:
        // Walk through every position and update the running maximum.
        // We do not actually need the weights for the final result, because the existence of
        // a single-element subarray makes the answer depend only on the largest value.
        for (int i = 1; i < values.Length; i++)
        {
            // If the current value is larger than what we have seen before,
            // it becomes the new best candidate answer.
            if (values[i] > maxValue)
            {
                maxValue = values[i];
            }
        }

        // Step 3:
        // Return the global maximum value.
        // This is guaranteed achievable as the weighted median of the single-element subarray
        // containing that value.
        return maxValue;
    }
}

// Demo code requested by the problem statement.

// Example 1
int[] values1 = { 4, 1, 7, 3 };
int[] weights1 = { 2, 5, 3, 1 };

var solution = new Solution();
long result1 = solution.MaximumWeightedMedianSegment(values1, weights1);
Console.WriteLine(result1); // Expected: 7

// Example 2
int[] values2 = { 5, 2, 6, 2, 4 };
int[] weights2 = { 1, 4, 2, 3, 5 };

long result2 = solution.MaximumWeightedMedianSegment(values2, weights2);
Console.WriteLine(result2); // Expected: 6