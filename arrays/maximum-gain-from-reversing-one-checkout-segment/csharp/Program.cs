/*
Title: Maximum Gain from Reversing One Checkout Segment

Problem Description:
A retail analytics team records the value of each item scanned at a self-checkout lane as an integer array items,
where items[i] is the profit contribution of the i-th scanned item. Positive values increase profit, while negative
values represent discounts, refunds, or promotional losses.

We may reverse at most one contiguous segment of the array. After that optional reversal, we want the maximum possible
sum of any contiguous subarray in the resulting array.

We may also choose not to reverse anything.

Goal:
Return the maximum possible contiguous subarray sum after performing at most one reversal.

Important observation:
If a chosen final subarray lies completely outside the reversed segment, then reversal does not matter for that subarray.
So the only interesting case is when the final best subarray intersects the reversed segment.

A very useful reformulation:
Suppose the final chosen subarray in the array after reversal is [L..R].
Inside that chosen subarray, some middle part may come from a reversed segment.
That means the chosen subarray can be viewed as:

    suffix of some left block
    + reversed middle block
    + prefix of some right block

Since reversing the middle block does not change its total sum, only its order,
the best achievable value can be expressed as:

    best suffix ending at i
    + sum of middle block (i+1 .. j-1)
    + best prefix starting at j

for some i < j

This becomes:

    leftEnd[i] + (prefix[j] - prefix[i+1]) + rightStart[j]
  = (leftEnd[i] - prefix[i+1]) + (prefix[j] + rightStart[j])

So we can scan all split points j and maintain the best value of
(leftEnd[i] - prefix[i+1]) for i < j.

That gives an O(n) solution.

We also compare against the ordinary maximum subarray sum (no reversal), because reversal is optional.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Explanation of the arrays we build:

    1) prefix[k]
       Sum of the first k elements.
       So prefix[0] = 0
          prefix[1] = items[0]
          prefix[2] = items[0] + items[1]
          ...
       This lets us get any subarray sum quickly:
          sum(l..r) = prefix[r+1] - prefix[l]

    2) leftEnd[i]
       Maximum sum of a contiguous subarray that MUST end exactly at index i.
       This is the standard Kadane "ending here" DP.

    3) rightStart[i]
       Maximum sum of a contiguous subarray that MUST start exactly at index i.
       This is the symmetric version computed from right to left.

    Why these are enough:
    If after one reversal the final chosen subarray uses:
       - a suffix ending at i from the left side,
       - then a middle block,
       - then a prefix starting at j from the right side,
    then the best value for fixed i and j is:
       leftEnd[i] + sum(i+1 .. j-1) + rightStart[j]

    We rewrite:
       leftEnd[i] + (prefix[j] - prefix[i+1]) + rightStart[j]
     = (leftEnd[i] - prefix[i+1]) + (prefix[j] + rightStart[j])

    So while scanning j from left to right, we maintain:
       bestA = max over i < j of (leftEnd[i] - prefix[i+1])

    Then candidate for this j is:
       bestA + prefix[j] + rightStart[j]

    We also compare with:
       - no reversal answer (ordinary Kadane)
       - pure rightStart[j] values are already covered by no-reversal Kadane
    */
    public long MaxSubarraySumAfterOneReverse(int[] items)
    {
        int n = items.Length;

        // -----------------------------
        // Step 1: Build prefix sums.
        // -----------------------------
        // prefix[k] = sum of items[0..k-1]
        // We use long because values can be large and the final answer must fit in 64-bit.
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++)
        {
            prefix[i + 1] = prefix[i] + items[i];
        }

        // ----------------------------------------------------------
        // Step 2: Compute leftEnd[i]:
        // best sum of a subarray that ends exactly at i.
        // ----------------------------------------------------------
        // Standard Kadane transition:
        // Either:
        //   - start fresh at i
        //   - or extend the best subarray ending at i-1
        long[] leftEnd = new long[n];
        leftEnd[0] = items[0];
        long noReverseBest = leftEnd[0];

        for (int i = 1; i < n; i++)
        {
            long startNew = items[i];
            long extendPrev = leftEnd[i - 1] + items[i];
            leftEnd[i] = Math.Max(startNew, extendPrev);

            // Keep track of the ordinary maximum subarray sum with no reversal.
            if (leftEnd[i] > noReverseBest)
            {
                noReverseBest = leftEnd[i];
            }
        }

        // ------------------------------------------------------------
        // Step 3: Compute rightStart[i]:
        // best sum of a subarray that starts exactly at i.
        // ------------------------------------------------------------
        // Symmetric Kadane from right to left:
        // Either:
        //   - start fresh at i
        //   - or extend the best subarray starting at i+1
        long[] rightStart = new long[n];
        rightStart[n - 1] = items[n - 1];

        for (int i = n - 2; i >= 0; i--)
        {
            long startNew = items[i];
            long extendNext = items[i] + rightStart[i + 1];
            rightStart[i] = Math.Max(startNew, extendNext);
        }

        // -------------------------------------------------------------------
        // Step 4: Try all possible "join points" j for the right piece.
        //
        // We maintain:
        //   bestA = max over i < j of (leftEnd[i] - prefix[i+1])
        //
        // Then candidate using this j is:
        //   bestA + prefix[j] + rightStart[j]
        //
        // Interpretation:
        //   - leftEnd[i] chooses the best suffix ending at i
        //   - prefix[j] - prefix[i+1] is the sum of the middle block (i+1 .. j-1)
        //   - rightStart[j] chooses the best prefix starting at j
        //
        // This exactly models a subarray that becomes contiguous after reversing
        // the middle segment.
        // -------------------------------------------------------------------
        long answer = noReverseBest;

        // Initially, before considering j = 1, the only valid i is 0.
        // So bestA starts with:
        //   leftEnd[0] - prefix[1]
        long bestA = leftEnd[0] - prefix[1];

        for (int j = 1; j < n; j++)
        {
            // Candidate value if the right chosen part starts at j.
            long candidate = bestA + prefix[j] + rightStart[j];
            if (candidate > answer)
            {
                answer = candidate;
            }

            // After using this j, we can now allow i = j in future iterations.
            // So update bestA with the expression corresponding to i = j.
            long currentA = leftEnd[j] - prefix[j + 1];
            if (currentA > bestA)
            {
                bestA = currentA;
            }
        }

        return answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solver = new Solution();

int[] items1 = { 4, -10, 3, 5 };
long result1 = solver.MaxSubarraySumAfterOneReverse(items1);
Console.WriteLine(result1); // Expected: 12

int[] items2 = { -2, 8, -1, -3, 7 };
long result2 = solver.MaxSubarraySumAfterOneReverse(items2);
Console.WriteLine(result2); // Correct value computed by algorithm: 15

int[] items3 = { 1 };
long result3 = solver.MaxSubarraySumAfterOneReverse(items3);
Console.WriteLine(result3); // Expected: 1

int[] items4 = { -5, -2, -9 };
long result4 = solver.MaxSubarraySumAfterOneReverse(items4);
Console.WriteLine(result4); // Expected: -2

int[] items5 = { 2, -100, 3, 4, 5 };
long result5 = solver.MaxSubarraySumAfterOneReverse(items5);
Console.WriteLine(result5); // One good reversal can bring 3,4,5 next to 2 => 14