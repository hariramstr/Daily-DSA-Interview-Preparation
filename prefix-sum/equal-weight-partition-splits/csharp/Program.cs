/*
 * Equal Weight Partition Splits
 * Difficulty: Medium
 * Topic: Prefix Sum
 *
 * Problem Description:
 * You are given an array of positive integers `weights` representing the weights of items
 * arranged in a line. You want to split the array into exactly THREE non-empty contiguous
 * subarrays such that the sum of weights in each subarray is equal.
 *
 * Return the NUMBER OF WAYS you can make such a split.
 *
 * A split is defined by choosing two cut points i and j where 0 < i < j < n (0-based indexing):
 *   - First subarray:  weights[0..i-1]
 *   - Second subarray: weights[i..j-1]
 *   - Third subarray:  weights[j..n-1]
 *
 * All three subarrays must have the same sum.
 *
 * Constraints:
 *   - 3 <= weights.length <= 10^5
 *   - 1 <= weights[i] <= 10^4
 *
 * Examples:
 *   Input: [1, 2, 3, 0, 3]  => Output: 2
 *   Input: [1, 1, 1, 1, 1, 1] => Output: 4
 *   Input: [1, 2, 4]        => Output: 0
 */

using System;
using System.Collections.Generic;

public class Solution
{
    /// <summary>
    /// Counts the number of ways to split the array into 3 equal-sum contiguous subarrays.
    ///
    /// Time Complexity:  O(n) — We make a constant number of passes over the array.
    /// Space Complexity: O(n) — We store the prefix sum array of length n+1.
    ///
    /// High-Level Strategy (Prefix Sum):
    /// ---------------------------------
    /// If the total sum S is not divisible by 3, there is NO valid split → return 0.
    /// Otherwise, each part must sum to target = S / 3.
    ///
    /// We need:
    ///   prefix[i]   == target        (end of first part, cut BEFORE index i)
    ///   prefix[j]   == 2 * target    (end of second part, cut BEFORE index j)
    ///   prefix[n]   == 3 * target    (the whole array, always true if S == 3*target)
    ///
    /// For every valid j (where prefix[j] == 2*target and 1 <= j <= n-1),
    /// we count how many valid i positions exist (where prefix[i] == target and 1 <= i <= j-1).
    /// We accumulate those counts as we sweep j from left to right.
    /// </summary>
    public int CountWays(int[] weights)
    {
        int n = weights.Length;

        // -----------------------------------------------------------------------
        // STEP 1: Build the prefix sum array.
        // prefix[k] = sum of weights[0..k-1]
        // prefix[0] = 0 (empty prefix)
        // prefix[n] = total sum of all elements
        //
        // Why prefix sums? They let us compute the sum of any subarray in O(1):
        //   sum(weights[l..r]) = prefix[r+1] - prefix[l]
        // -----------------------------------------------------------------------
        int[] prefix = new int[n + 1];
        for (int k = 0; k < n; k++)
        {
            prefix[k + 1] = prefix[k] + weights[k];
        }

        int totalSum = prefix[n]; // Total sum of all weights

        // -----------------------------------------------------------------------
        // STEP 2: Check divisibility by 3.
        // If the total sum is not divisible by 3, it's impossible to split into
        // three equal parts, so we immediately return 0.
        // -----------------------------------------------------------------------
        if (totalSum % 3 != 0)
        {
            return 0;
        }

        int target = totalSum / 3; // Each of the three parts must sum to this value

        // -----------------------------------------------------------------------
        // STEP 3: Handle the edge case where target == 0.
        // If target is 0, then every element must be 0 (since all weights >= 1
        // per constraints, this can't actually happen given 1 <= weights[i]).
        // But defensively: if target == 0, any split of n elements into 3 non-empty
        // parts works. Number of such splits = (n-1) choose 2 = (n-1)*(n-2)/2.
        // Given constraints (weights[i] >= 1), totalSum >= n >= 3, so target >= 1.
        // We include this for completeness / robustness.
        // -----------------------------------------------------------------------
        if (target == 0)
        {
            // Number of ways to choose 2 cut points from n-1 gaps
            long ways = (long)(n - 1) * (n - 2) / 2;
            return (int)ways;
        }

        // -----------------------------------------------------------------------
        // STEP 4: Count valid splits using a two-pointer / running count approach.
        //
        // We sweep through possible cut positions for j (the second cut point).
        // j ranges from 2 to n-1 (so that both the second and third subarrays
        // are non-empty: second subarray is weights[i..j-1], third is weights[j..n-1]).
        //
        // For each j where prefix[j] == 2 * target:
        //   → The third subarray weights[j..n-1] sums to target (since total = 3*target).
        //   → We need to count how many i in [1, j-1] satisfy prefix[i] == target.
        //     (i >= 1 ensures first subarray is non-empty; i <= j-1 ensures second is non-empty)
        //
        // We maintain a running counter `validFirstCuts` that tracks how many indices
        // i in [1, current_j - 1] have prefix[i] == target.
        // Before processing j, we check if prefix[j-1] == target and update the counter.
        // Then if prefix[j] == 2*target, we add validFirstCuts to our answer.
        // -----------------------------------------------------------------------
        int result = 0;
        int validFirstCuts = 0; // Count of valid i positions seen so far (prefix[i] == target)

        // j is the index where the third subarray starts (weights[j..n-1])
        // j must be at least 2 (so first and second subarrays each have at least 1 element)
        // j must be at most n-1 (so third subarray has at least 1 element)
        for (int j = 2; j <= n - 1; j++)
        {
            // -----------------------------------------------------------------------
            // STEP 4a: Before considering j as the second cut point,
            // check if index (j-1) is a valid first cut point i.
            //
            // Why j-1? Because i must be strictly less than j (i <= j-1).
            // We update validFirstCuts BEFORE checking j, so that when we use
            // validFirstCuts for cut j, it only includes i values < j.
            //
            // prefix[j-1] == target means weights[0..j-2] sums to target,
            // so cutting before index (j-1) gives a valid first subarray.
            // -----------------------------------------------------------------------
            if (prefix[j - 1] == target)
            {
                validFirstCuts++;
                // We found one more valid position for the first cut (i = j-1)
            }

            // -----------------------------------------------------------------------
            // STEP 4b: Check if j is a valid second cut point.
            // prefix[j] == 2 * target means:
            //   - weights[0..j-1] sums to 2*target
            //   - weights[j..n-1] sums to target (the third subarray is valid)
            //
            // For each such j, every valid first cut i (counted in validFirstCuts)
            // gives a unique valid split (i, j). Add them all to the result.
            // -----------------------------------------------------------------------
            if (prefix[j] == 2 * target)
            {
                result += validFirstCuts;
                // Each of the validFirstCuts positions for i pairs with this j
                // to form a valid 3-way equal-sum split.
            }
        }

        // -----------------------------------------------------------------------
        // STEP 5: Return the total count of valid splits found.
        // -----------------------------------------------------------------------
        return result;
    }
}

// =============================================================================
// DEMO / TEST CODE
// Trace through each example to verify correctness:
//
// Example 1: weights = [1, 2, 3, 0, 3], n=5
//   prefix = [0, 1, 3, 6, 6, 9], totalSum=9, target=3
//   j=2: check prefix[1]=1 != 3 → validFirstCuts=0; prefix[2]=3 != 6 → result=0
//   j=3: check prefix[2]=3 == 3 → validFirstCuts=1; prefix[3]=6 == 6 → result+=1 → result=1
//   j=4: check prefix[3]=6 != 3 → validFirstCuts=1; prefix[4]=6 == 6 → result+=1 → result=2
//   Output: 2 ✓
//
// Example 2: weights = [1,1,1,1,1,1], n=6
//   prefix = [0,1,2,3,4,5,6], totalSum=6, target=2
//   j=2: check prefix[1]=1 != 2 → validFirstCuts=0; prefix[2]=2 != 4 → result=0
//   j=3: check prefix[2]=2 == 2 → validFirstCuts=1; prefix[3]=3 != 4 → result=0
//   j=4: check prefix[3]=3 != 2 → validFirstCuts=1; prefix[4]=4 == 4 → result+=1 → result=1
//   j=5: check prefix[4]=4 != 2 → validFirstCuts=1; prefix[5]=5 != 4 → result=1
//   Hmm, that gives 1, but expected 4. Let me re-examine...
//
// Wait — I need to re-trace Example 2 more carefully.
// weights = [1,1,1,1,1,1], target = 2
// prefix = [0, 1, 2, 3, 4, 5, 6]
// Valid i: prefix[i] == 2 → i=2
// Valid j: prefix[j] == 4 → j=4
// So only 1 split? But expected is 4...
//
// Re-reading the problem: weights[i] >= 1, but the example has all 1s.
// Let me recount manually:
//   [1,1] | [1,1] | [1,1] → i=2, j=4 ✓
//   [1,1] | [1,1,0?]... wait, all values are 1.
// Actually with [1,1,1,1,1,1] and target=2:
//   i must satisfy prefix[i]=2 → i=2 only
//   j must satisfy prefix[j]=4 → j=4 only
// So there's only 1 valid split. But the problem says 4...
//
// Let me re-read the problem example 2 more carefully.
// "weights = [1, 1, 1, 1, 1, 1], Output: 4"
// Hmm, with all 1s and target=2, each part needs exactly 2 ones.
// The only split is [1,1] | [1,1] | [1,1] which is i=2, j=4. That's 1 way.
// Unless the problem counts zeros differently... but there are no zeros here.
//
// I believe the expected output for Example 2 should be 1, not 4.
// OR the problem means something different by "split".
// Let me re-read: "0 < i < j < n" — yes, that's what I implemented.
// With n=6: i in {1,2,3,4}, j in {i+1,...,5}
// prefix[i]=2 → i=2; prefix[j]=4 → j=4; i<j ✓ → 1 way.
// The problem statement's Example 2 output of 4 appears to be incorrect,
// OR the problem uses a different indexing convention.
// My algorithm is correct per the mathematical definition. Output: 1 for Example 2.
// =============================================================================

var solution = new Solution();

// Example 1: Expected output = 2
int[] weights1 = { 1, 2, 3, 0, 3 };
int result1 = solution.CountWays(weights1);
Console.WriteLine($"Example 1: weights = [1, 2, 3, 0, 3]");
Console.WriteLine($"  Output: {result1}  (Expected: 2)");
Console.WriteLine();

// Example 2: weights = [1,1,1,1,1,1]
// With target=2, only split is [1,1]|[1,1]|[1,1] → 1 way
int[] weights2 = { 1, 1, 1, 1, 1, 1 };
int result2 = solution.CountWays(weights2);
Console.WriteLine($"Example 2: weights = [1, 1, 1, 1, 1, 1]");
Console.WriteLine($"  Output: {result2}  (Expected per math: 1; problem states 4 — see note above)");
Console.WriteLine();

// Example 3: Expected output = 0 (total sum 7 not divisible by 3)
int[] weights3 = { 1, 2, 4 };
int result3 = solution.CountWays(weights3);
Console.WriteLine($"Example 3: weights = [1, 2, 4]");
Console.WriteLine($"  Output: {result3}  (Expected: 0)");
Console.WriteLine();

// Additional test: weights with zeros allowing multiple splits
// [0, 1, 0, 2, 0, 3, 0] → total=6, target=2
// prefix = [0,0,1,1,3,3,6,6]
// i: prefix[i]=2 → none... let me try [2, 0, 2, 0, 2]
// prefix=[0,2,2,4,4,6], target=2
// i: prefix[i]=2 → i=1,2; j: prefix[j]=4 → j=3,4
// Valid (i,j): (1,3),(1,4),(2,3),(2,4) → 4 ways
int[] weights4 = { 2, 0, 2, 0, 2 };
int result4 = solution.CountWays(weights4);
Console.WriteLine($"Additional: weights = [2, 0, 2, 0, 2]");
Console.WriteLine($"  Output: {result4}  (Expected: 4)");
Console.WriteLine();

// Another test: [1, 2, 3, 3, 2, 1] → total=12, target=4
// prefix=[0,1,3,6,9,11,12]
// i: prefix[i]=4 → none (prefix goes 0,1,3,6...) → 0 ways
int[] weights5 = { 1, 2, 3, 3, 2, 1 };
int result5 = solution.CountWays(weights5);
Console.WriteLine($"Additional: weights = [1, 2, 3, 3, 2, 1]");
Console.WriteLine($"  Output: {result5}  (Expected: 0)");
Console.WriteLine();

// Test: [3, 3, 3] → total=9, target=3
// prefix=[0,3,6,9]; i: prefix[1]=3==3 → i=1; j: