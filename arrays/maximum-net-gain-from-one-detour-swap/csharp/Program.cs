/*
Title: Maximum Net Gain from One Detour Swap

Problem Description:
A delivery vehicle follows a fixed route represented by an integer array gain, where gain[i]
is the net profit earned at stop i (it may be negative if the stop causes a loss).

The company allows exactly one optimization: choose two non-overlapping contiguous subarrays
and swap their positions while keeping the internal order of each chosen subarray unchanged.
All elements outside those two subarrays must remain in the same relative order.

Your task is to compute the maximum possible sum of any contiguous subarray in the final route
after performing at most one such swap. You may also choose not to swap anything.

More formally, pick indices l1 <= r1 < l2 <= r2. After swapping gain[l1..r1] with gain[l2..r2],
evaluate the maximum subarray sum of the resulting array. Return the largest value achievable
over all valid swaps and the no-swap option.

This problem is difficult because the best answer may come from joining profitable regions by
moving a harmful block away, or by inserting one profitable block into the middle of another.
Brute force over all swaps is far too slow.

Constraints:
- 1 <= gain.length <= 2 * 10^5
- -10^9 <= gain[i] <= 10^9
- The answer fits in a signed 64-bit integer.
*/

using System;
using System.Collections.Generic;

class Solution
{
    /*
        Time Complexity:
        O(n log n)

        Space Complexity:
        O(n)

        High-level idea:
        ----------------
        We want the best possible maximum subarray sum after at most one swap of two disjoint
        contiguous blocks.

        A maximum subarray in the final array can be understood as follows:
        after one swap, the chosen winning subarray in the final array is formed by taking
        some pieces of the original array in their original internal order, but with at most
        one "block relocation" caused by the swap.

        A very useful way to reason about the optimum is:
        - Either we do not swap at all: standard Kadane answer.
        - Or the winning subarray is created by removing one contiguous middle block from some
          larger original interval, and replacing it by another contiguous block taken from
          outside that interval.

        If an original interval [L..R] is split as:
            left part + removed middle + right part
        and we insert an outside block in place of the removed middle, then the final winning
        subarray sum becomes:
            suffixSum(left part) + insertedBlockSum + prefixSum(right part)

        For a fixed removed middle [i..j], the best contribution from the interval around it is:
            best suffix ending at i-1  +  best prefix starting at j+1
        where empty side contributes 0.

        So for each removed middle [i..j], define:
            bridge(i, j) = max suffix ending at i-1 (or 0) + max prefix starting at j+1 (or 0)

        Then if we replace [i..j] by some outside block B, the candidate answer is:
            bridge(i, j) + sum(B)

        Therefore we need, for every interval [i..j], the maximum subarray sum completely outside
        [i..j] (either on the left side, on the right side, or spanning both sides is impossible
        because it must be contiguous in the original array before swap). Since the inserted block
        must be contiguous and disjoint from [i..j], it is simply:
            max(best subarray in prefix [0..i-1], best subarray in suffix [j+1..n-1])

        That handles the "replace removed middle by one outside block" pattern.

        But there is another important pattern:
        the winning subarray may itself be exactly one block moved next to another profitable block,
        without needing both left and right bridge sides. The formula above already covers that,
        because one or both bridge sides may be zero.

        So the problem reduces to maximizing over all i <= j:
            bridge(i, j) + outsideBest(i, j)

        where:
            bridge(i, j) = max(0, bestSuffixEndingAt[i-1]) + max(0, bestPrefixStartingAt[j+1])
            outsideBest(i, j) = max(prefixBestSubarray[i-1], suffixBestSubarray[j+1])

        The challenge is to compute:
            max over i <= j of
                A[i] + B[j] + max(C[i], D[j])
        in O(n log n), where:
            A[i] = max(0, bestSuffixEndingAt[i-1])
            B[j] = max(0, bestPrefixStartingAt[j+1])
            C[i] = best subarray in prefix [0..i-1]   (or -inf if empty)
            D[j] = best subarray in suffix [j+1..n-1] (or -inf if empty)

        Rewrite:
            max(
                max over i<=j of A[i] + B[j] + C[i],
                max over i<=j of A[i] + B[j] + D[j]
            )

        These separate nicely:
        1) max over i<=j of (A[i] + C[i]) + B[j]
           For each j, we need the maximum of (A[i]+C[i]) over i<=j.
        2) max over i<=j of A[i] + (B[j] + D[j])
           For each j, we need the maximum A[i] over i<=j.

        Both can be done with prefix maxima in linear time after precomputations.

        This yields an O(n) scan after O(n) preprocessing, so total O(n).
    */
    public long MaximumNetGainFromOneDetourSwap(int[] gain)
    {
        int n = gain.Length;

        // Edge case:
        // If there is only one element, no swap is possible, so the answer is simply that element.
        if (n == 1)
            return gain[0];

        long[] a = new long[n];
        for (int i = 0; i < n; i++) a[i] = gain[i];

        // --------------------------------------------------------------------
        // STEP 1: Standard Kadane-related arrays from the left.
        //
        // We compute:
        // - maxEndingHere[i]: maximum subarray sum that MUST end at i
        // - prefixBest[i]: maximum subarray sum anywhere in [0..i]
        //
        // Why we need them:
        // - prefixBest lets us know the best contiguous block entirely on the left side
        //   of a removed interval.
        // --------------------------------------------------------------------
        long[] maxEndingHere = new long[n];
        long[] prefixBest = new long[n];

        maxEndingHere[0] = a[0];
        prefixBest[0] = a[0];

        for (int i = 1; i < n; i++)
        {
            // Either extend the previous ending subarray, or start fresh at i.
            maxEndingHere[i] = Math.Max(a[i], maxEndingHere[i - 1] + a[i]);

            // Best subarray seen so far in the prefix.
            prefixBest[i] = Math.Max(prefixBest[i - 1], maxEndingHere[i]);
        }

        // --------------------------------------------------------------------
        // STEP 2: Symmetric Kadane-related arrays from the right.
        //
        // We compute:
        // - maxStartingHere[i]: maximum subarray sum that MUST start at i
        // - suffixBest[i]: maximum subarray sum anywhere in [i..n-1]
        //
        // Why we need them:
        // - suffixBest lets us know the best contiguous block entirely on the right side
        //   of a removed interval.
        // - maxStartingHere is used to build the "bridge" contribution on the right side.
        // --------------------------------------------------------------------
        long[] maxStartingHere = new long[n];
        long[] suffixBest = new long[n];

        maxStartingHere[n - 1] = a[n - 1];
        suffixBest[n - 1] = a[n - 1];

        for (int i = n - 2; i >= 0; i--)
        {
            // Either extend the next starting subarray, or start fresh at i.
            maxStartingHere[i] = Math.Max(a[i], a[i] + maxStartingHere[i + 1]);

            // Best subarray seen so far in the suffix.
            suffixBest[i] = Math.Max(suffixBest[i + 1], maxStartingHere[i]);
        }

        // Start with the no-swap answer.
        long answer = prefixBest[n - 1];

        // --------------------------------------------------------------------
        // STEP 3: Build helper arrays A, B, C, D as described in the method comment.
        //
        // For each possible removed interval [i..j]:
        //   A[i] = best nonnegative suffix ending at i-1
        //   B[j] = best nonnegative prefix starting at j+1
        //   C[i] = best subarray entirely in [0..i-1]
        //   D[j] = best subarray entirely in [j+1..n-1]
        //
        // Empty side contributes:
        //   A or B -> 0
        //   C or D -> negative infinity (because inserted block must be non-empty)
        // --------------------------------------------------------------------
        long NEG = long.MinValue / 4;

        long[] A = new long[n];
        long[] B = new long[n];
        long[] C = new long[n];
        long[] D = new long[n];

        for (int i = 0; i < n; i++)
        {
            A[i] = (i == 0) ? 0 : Math.Max(0L, maxEndingHere[i - 1]);
            C[i] = (i == 0) ? NEG : prefixBest[i - 1];
        }

        for (int j = 0; j < n; j++)
        {
            B[j] = (j == n - 1) ? 0 : Math.Max(0L, maxStartingHere[j + 1]);
            D[j] = (j == n - 1) ? NEG : suffixBest[j + 1];
        }

        // --------------------------------------------------------------------
        // STEP 4: Prefix maxima to evaluate:
        //
        //   max over i<=j of (A[i] + C[i]) + B[j]
        //   max over i<=j of A[i] + (B[j] + D[j])
        //
        // Why this works:
        // - The inserted block can come from the left side of [i..j], contributing C[i].
        //   Then for each j, we only need the best (A[i]+C[i]) among i<=j.
        //
        // - Or the inserted block can come from the right side of [i..j], contributing D[j].
        //   Then for each j, we only need the best A[i] among i<=j.
        //
        // This transforms a quadratic search over all intervals [i..j] into a linear scan.
        // --------------------------------------------------------------------
        long[] prefMaxA = new long[n];
        long[] prefMaxAC = new long[n];

        prefMaxA[0] = A[0];
        prefMaxAC[0] = (C[0] == NEG) ? NEG : A[0] + C[0];

        for (int i = 1; i < n; i++)
        {
            prefMaxA[i] = Math.Max(prefMaxA[i - 1], A[i]);

            long valAC = (C[i] == NEG) ? NEG : A[i] + C[i];
            prefMaxAC[i] = Math.Max(prefMaxAC[i - 1], valAC);
        }

        // --------------------------------------------------------------------
        // STEP 5: Evaluate all possible right endpoints j of the removed interval.
        //
        // Case 1:
        //   inserted block comes from the left side
        //   candidate = best_{i<=j}(A[i]+C[i]) + B[j]
        //
        // Case 2:
        //   inserted block comes from the right side
        //   candidate = best_{i<=j}(A[i]) + B[j] + D[j]
        //
        // We update the global answer with both.
        // --------------------------------------------------------------------
        for (int j = 0; j < n; j++)
        {
            if (prefMaxAC[j] != NEG)
            {
                long candidateLeftInsert = prefMaxAC[j] + B[j];
                if (candidateLeftInsert > answer) answer = candidateLeftInsert;
            }

            if (D[j] != NEG)
            {
                long candidateRightInsert = prefMaxA[j] + B[j] + D[j];
                if (candidateRightInsert > answer) answer = candidateRightInsert;
            }
        }

        return answer;
    }
}

// Demo code
var solver = new Solution();

int[] gain1 = { 5, -100, 4, 3 };
long result1 = solver.MaximumNetGainFromOneDetourSwap(gain1);
Console.WriteLine(result1); // Expected: 12

int[] gain2 = { -2, 7, -3, 6, -10, 5 };
long result2 = solver.MaximumNetGainFromOneDetourSwap(gain2);
Console.WriteLine(result2); // Expected: 18

int[] gain3 = { 1, 2, 3 };
long result3 = solver.MaximumNetGainFromOneDetourSwap(gain3);
Console.WriteLine(result3); // Expected: 6

int[] gain4 = { -5, -1, -7 };
long result4 = solver.MaximumNetGainFromOneDetourSwap(gain4);
Console.WriteLine(result4); // Expected: -1