/*
Title: Minimum Model Accuracy to Pass Staged Benchmarks

Problem Description:
You are given the results of a machine learning model evaluated on a sequence of benchmark stages.
Stage i contains tasks with difficulty score difficulties[i] and contributes weight weights[i]
to the final certification score.

The model is deployed with a single accuracy threshold A.
A task is considered passed if difficulties[i] <= A.

For a fixed threshold A:
- Convert stage i into +weights[i] if difficulties[i] <= A
- Otherwise convert it into -weights[i]

The threshold A is feasible if the transformed array can be divided into at most k contiguous
non-empty segments, and every segment has sum >= 0.

Return the minimum integer threshold A that is feasible.
If no threshold can make the benchmark pass, return -1.

Key observation:
Because we are allowed to use "at most k" segments, using exactly 1 segment is always allowed
(as long as k >= 1, which the constraints guarantee). Therefore:
- If the total transformed sum is >= 0, then the whole array itself is a valid partition.
- If the total transformed sum is < 0, then no partition can work, because the sums of all
  segments add up to the total sum, and a sum of non-negative segment sums cannot be negative.

So feasibility for a threshold A is equivalent to:
    total transformed sum >= 0

That means we only need to find the minimum A such that:
    sum(weights[i] for difficulties[i] <= A) - sum(weights[i] for difficulties[i] > A) >= 0

Equivalently:
    2 * passedWeightSum >= totalWeight

This can be solved by sorting stages by difficulty and adding weights in that order until the
passed weight reaches at least half of the total weight.

This is also equivalent to binary searching on A, but the sorted sweep is simpler and optimal.

Example 1:
difficulties = [4, 2, 7, 3, 6]
weights      = [5, 2, 4, 3, 6]
totalWeight = 20
Need passedWeight >= 10

Sorted by difficulty:
(2,2), (3,3), (4,5), (6,6), (7,4)
Prefix passed weights:
2, 5, 10, 16, 20
First time reaching 10 is at difficulty 4, so answer = 4

Example 2:
difficulties = [8, 9, 10]
weights      = [3, 4, 5]
totalWeight = 12
Need passedWeight >= 6

Sorted by difficulty:
(8,3), (9,4), (10,5)
Prefix passed weights:
3, 7, 12
First time reaching 6 is at difficulty 9

Important note:
The problem statement's Example 2 claims the answer is 10, but that is inconsistent with the
formal definition. For A = 9, the transformed array is [3, 4, -5], whose total sum is 2 >= 0,
so using one segment (the whole array) is valid. Therefore the correct answer under the stated
rules is 9.

The implementation below follows the formal problem definition exactly.
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    - O(n log n), because we sort the stages by difficulty once.
    - The final sweep through the sorted list is O(n).

    Space Complexity:
    - O(n), to store the (difficulty, weight) pairs for sorting.

    Beginner-friendly summary:
    1. Compute the total weight of all stages.
    2. Sort stages by difficulty.
    3. Imagine increasing threshold A from small to large.
       Whenever A reaches a stage's difficulty, that stage flips from failed (-weight)
       to passed (+weight), which increases the total transformed sum by 2 * weight.
    4. The first difficulty where the total transformed sum becomes non-negative is the answer.
    */
    public int MinimumAccuracyThreshold(int[] difficulties, int[] weights, int k)
    {
        // Step 1:
        // Validate the most basic assumptions.
        // The problem guarantees valid input, but these checks make the method safer and clearer.
        if (difficulties == null || weights == null || difficulties.Length != weights.Length || difficulties.Length == 0)
        {
            return -1;
        }

        int n = difficulties.Length;

        // Step 2:
        // Build a list of (difficulty, weight) pairs so we can sort stages by difficulty.
        //
        // Why do we sort?
        // Because as threshold A increases, stages become "passed" exactly when A reaches
        // their difficulty. Processing stages in sorted difficulty order lets us simulate
        // that change efficiently.
        var stages = new (int Difficulty, long Weight)[n];
        long totalWeight = 0;

        for (int i = 0; i < n; i++)
        {
            stages[i] = (difficulties[i], weights[i]);
            totalWeight += weights[i];
        }

        // Step 3:
        // Sort by difficulty ascending.
        //
        // After sorting:
        // - all stages before the current position are considered passed
        // - all stages after the current position are still failed
        Array.Sort(stages, (a, b) => a.Difficulty.CompareTo(b.Difficulty));

        // Step 4:
        // We now sweep through the sorted stages and keep track of the total weight of
        // stages that have become passed.
        //
        // For a threshold A, the transformed total sum is:
        //   passedWeight - failedWeight
        //
        // Since:
        //   failedWeight = totalWeight - passedWeight
        //
        // The total transformed sum becomes:
        //   passedWeight - (totalWeight - passedWeight)
        // = 2 * passedWeight - totalWeight
        //
        // Feasibility is exactly:
        //   2 * passedWeight - totalWeight >= 0
        // which is the same as:
        //   2 * passedWeight >= totalWeight
        long passedWeight = 0;

        for (int i = 0; i < n; i++)
        {
            // This stage becomes passed once threshold reaches its difficulty.
            passedWeight += stages[i].Weight;

            // Check whether the whole transformed array now has non-negative total sum.
            //
            // Why is checking only the total sum enough?
            // Because the partition may use at most k segments, and one segment is allowed.
            // If the total sum is non-negative, the whole array as one segment is valid.
            // If the total sum is negative, no partition can make every segment non-negative,
            // because the sum of non-negative segment sums cannot be negative.
            if (passedWeight * 2 >= totalWeight)
            {
                // The first difficulty where this becomes true is the minimum feasible threshold.
                return stages[i].Difficulty;
            }
        }

        // Step 5:
        // If we somehow never reached non-negative total sum, then no threshold works.
        //
        // In practice, once A reaches the maximum difficulty, all stages are passed,
        // so passedWeight == totalWeight and the condition must hold.
        // Still, returning -1 is the correct fallback behavior.
        return -1;
    }
}

// Demo code
var solution = new Solution();

// Example 1 from the statement
int[] difficulties1 = { 4, 2, 7, 3, 6 };
int[] weights1 = { 5, 2, 4, 3, 6 };
int k1 = 2;
int result1 = solution.MinimumAccuracyThreshold(difficulties1, weights1, k1);
Console.WriteLine(result1); // Expected under the formal definition: 4

// Example 2 from the statement
// Important: under the formal definition, the correct answer is 9, not 10.
int[] difficulties2 = { 8, 9, 10 };
int[] weights2 = { 3, 4, 5 };
int k2 = 3;
int result2 = solution.MinimumAccuracyThreshold(difficulties2, weights2, k2);
Console.WriteLine(result2); // Correct under the formal definition: 9

// Additional quick sanity check:
// If all weights are already enough at the smallest difficulty, answer should be that difficulty.
int[] difficulties3 = { 5 };
int[] weights3 = { 10 };
int k3 = 1;
int result3 = solution.MinimumAccuracyThreshold(difficulties3, weights3, k3);
Console.WriteLine(result3); // 5