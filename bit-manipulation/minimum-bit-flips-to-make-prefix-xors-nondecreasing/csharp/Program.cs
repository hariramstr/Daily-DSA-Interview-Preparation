/*
Minimum Bit Flips to Make Prefix XORs Nondecreasing

You are given an array nums of n non-negative integers. You may perform the following
operation any number of times: choose an index i and flip exactly one bit in nums[i]
(changing a 0 bit to 1 or a 1 bit to 0). Each such single-bit change costs 1.

Define the prefix XOR array px where:
    px[i] = nums[0] ^ nums[1] ^ ... ^ nums[i]

Your task is to determine the minimum total number of bit flips needed so that the
prefix XOR array becomes nondecreasing, meaning:
    px[0] <= px[1] <= ... <= px[n - 1]

You are allowed to modify the values in nums before evaluating the prefix XORs.
Return the minimum number of single-bit flips required.

Key idea:
- Let y[i] be the final prefix XOR values after modification.
- Then the final modified array values are:
      a[0] = y[0]
      a[i] = y[i - 1] ^ y[i]   for i >= 1
- The total number of bit flips is exactly the sum of bit differences between nums[i]
  and a[i], i.e. the sum of Hamming distances.

So the problem becomes:
- Choose a nondecreasing sequence y[0..n-1]
- Minimize:
      popcount(nums[0] ^ y[0]) +
      sum_{i=1..n-1} popcount(nums[i] ^ (y[i-1] ^ y[i]))

The values are below 2^20, so only 20 bits matter.

Important structural observation:
For two numbers u and v, the condition u <= v is determined by the highest bit where
they differ. Therefore we can build the answer bit-by-bit from the most significant bit
to the least significant bit, while tracking which adjacent pairs are already known to be:
- strictly increasing at a higher bit ("free": lower bits no longer matter), or
- still equal on all processed higher bits ("tight": lower bits must preserve nondecreasing).

This leads to a divide-and-conquer / dynamic programming over segments:
- At each bit b, inside every segment where all adjacent comparisons are still tight,
  the chosen bit sequence for y over that segment must be nondecreasing, so it has the form:
      0 0 0 ... 0 1 1 1 ... 1
  i.e. one split position.
- Choosing that split contributes a cost for bit b and recursively creates subproblems
  on the left and right parts for lower bits.

This yields an O(n * B^2)-style recursive DP with prefix sums, which is easily fast enough
for B = 20 and n <= 100000.
*/

using System;
using System.Collections.Generic;

class Solution
{
    private int[] _nums = Array.Empty<int>();
    private int _n;
    private const int MaxBit = 19;

    // Prefix sums for each bit:
    // pref[bit][i] = number of nums[0..i-1] whose 'bit' is 1
    private int[][] _pref = Array.Empty<int[]>();

    // Memoization for recursive DP:
    // key = (l, r, bit, leftPrevBit)
    // We only need leftPrevBit because inside a segment all higher bits of y are equal,
    // and the previous prefix XOR value just outside the segment affects the first element cost.
    private readonly Dictionary<long, int> _memo = new();

    /*
    Time Complexity:
    - Building prefix sums: O(n * 20)
    - Recursive DP: O(n * 20 * 20) in practice / amortized, because each level processes
      segment splits with O(1) cost queries using prefix sums.
    Overall: O(n * 20 * 20), which is safe for n <= 100000.

    Space Complexity:
    - Prefix sums: O(n * 20)
    - Memoization / recursion stack: O(n * 20) in practice
    */
    public int MinBitFlips(int[] nums)
    {
        _nums = nums;
        _n = nums.Length;

        _pref = new int[20][];
        for (int b = 0; b < 20; b++)
        {
            _pref[b] = new int[_n + 1];
            for (int i = 0; i < _n; i++)
            {
                _pref[b][i + 1] = _pref[b][i] + (((nums[i] >> b) & 1) != 0 ? 1 : 0);
            }
        }

        _memo.Clear();

        // We solve for the whole array, processing from the most significant bit down.
        // There is no previous prefix XOR before y[0], so for cost formulas we treat
        // the "previous y" outside the segment as 0.
        return SolveSegment(0, _n - 1, MaxBit, 0);
    }

    private int SolveSegment(int l, int r, int bit, int prevYBit)
    {
        // Base case:
        // If the segment is empty or has one element, lower bits can be chosen independently
        // just to minimize local Hamming distance costs.
        if (l > r) return 0;

        // If we have processed all bits, there is nothing left to pay.
        if (bit < 0) return 0;

        long key = Encode(l, r, bit, prevYBit);
        if (_memo.TryGetValue(key, out int cached))
            return cached;

        // Inside this segment, all prefix XOR values y[l..r] are still equal on all bits
        // higher than 'bit'. Therefore, to keep y nondecreasing, the current bit sequence
        // across y[l..r] must itself be nondecreasing:
        //
        //    0 0 0 ... 0 1 1 1 ... 1
        //
        // So we try every possible split:
        // - indices [l .. k] get bit 0
        // - indices [k+1 .. r] get bit 1
        //
        // where k can range from l-1 (all ones) to r (all zeros).
        //
        // For each split, we compute:
        // 1) the bit-cost contributed by this bit to all array elements nums[i]
        // 2) recursively solve lower bits on the left block and right block
        //
        // Why recursion on both blocks?
        // - Adjacent y values inside the left block have equal current bit (all 0),
        //   so their lower bits still must preserve nondecreasing order.
        // - Same for the right block (all 1).
        // - But the boundary between left and right is already strictly increasing at this bit,
        //   so lower bits no longer constrain across that boundary. That is exactly why the
        //   problem splits into two independent subproblems.
        int best = int.MaxValue;

        for (int k = l - 1; k <= r; k++)
        {
            int costThisBit = CostForBitWithSplit(l, r, bit, prevYBit, k);

            // Left block: y[l..k] all have current bit 0.
            // The previous y bit before index l is prevYBit.
            int leftCost = 0;
            if (l <= k)
            {
                leftCost = SolveSegment(l, k, bit - 1, prevYBit);
            }

            // Right block: y[k+1..r] all have current bit 1.
            // For the first element in the right block, the previous prefix XOR y[k]
            // has current bit:
            // - 0 if left block exists
            // - otherwise prevYBit if the right block starts at l
            //
            // But for lower-bit recursion, only the "outside previous y bit at current level"
            // matters for computing the first array element in that subproblem.
            //
            // Since all higher bits are fixed and equal inside the right block, the previous
            // y just before the right block has current bit:
            // - 0 if there is a left block
            // - prevYBit if there is no left block
            //
            // However, once we recurse to lower bits, the current bit is no longer relevant;
            // what matters is the previous y's lower-bit context. Because higher bits already
            // made the boundary strict when left block exists, the right block becomes fully
            // independent from the left in lower bits. Therefore we can safely start the right
            // recursion with previous y bit = 1 for its own segment's current bit convention.
            int rightCost = 0;
            if (k + 1 <= r)
            {
                rightCost = SolveSegment(k + 1, r, bit - 1, 1);
            }

            int total = costThisBit + leftCost + rightCost;
            if (total < best) best = total;
        }

        _memo[key] = best;
        return best;
    }

    private int CostForBitWithSplit(int l, int r, int bit, int prevYBit, int k)
    {
        // We now compute the contribution of ONE specific bit to the total number of flips.
        //
        // Let yBit[i] be the chosen bit of prefix XOR y[i] at this bit position.
        // Under split k:
        // - yBit[i] = 0 for i in [l..k]
        // - yBit[i] = 1 for i in [k+1..r]
        //
        // The modified array bit aBit[i] is:
        // - aBit[l] = prevOutsideYBit XOR yBit[l]          if this segment starts a fresh subproblem
        // - more generally for i > l inside the segment:
        //       aBit[i] = yBit[i-1] XOR yBit[i]
        //
        // Since yBit is monotone 0...01...1, transitions happen only:
        // - possibly at the first element (depends on prevYBit and yBit[l])
        // - possibly once at boundary k -> k+1
        //
        // Then we compare aBit[i] with the original nums[i]'s bit.
        // If they differ, we must flip that bit in nums[i], costing 1.
        int cost = 0;

        // Handle i = l separately because it depends on the previous prefix XOR outside the segment.
        int firstYBit = (l <= k) ? 0 : 1;
        int aBitL = prevYBit ^ firstYBit;
        int numBitL = ((_nums[l] >> bit) & 1);
        if (aBitL != numBitL) cost++;

        if (l == r) return cost;

        // For i in [l+1 .. r], aBit[i] = yBit[i-1] XOR yBit[i].
        //
        // Because yBit is monotone:
        // - inside constant regions, XOR is 0
        // - exactly at the split boundary (if both sides exist), XOR is 1
        //
        // So:
        // - positions [l+1 .. k] have aBit = 0
        // - position k+1 has aBit = 1 if l <= k < r
        // - positions [k+2 .. r] have aBit = 0
        //
        // Therefore all positions except the boundary transition want final bit 0,
        // and the boundary transition wants final bit 1.
        //
        // Using prefix sums:
        // - cost to force a range to 0 = number of ones in nums on that range
        // - cost to force a single position to 1 = 1 if nums bit is 0, else 0

        // Range [l+1 .. k] forced to 0
        if (l + 1 <= k)
        {
            cost += CountOnes(bit, l + 1, k);
        }

        // Boundary position k+1 forced to 1, only if both left and right parts exist
        if (l <= k && k + 1 <= r)
        {
            int numBit = ((_nums[k + 1] >> bit) & 1);
            if (numBit == 0) cost++;
        }

        // Range [max(l+1, k+2) .. r] forced to 0
        int start = Math.Max(l + 1, k + 2);
        if (start <= r)
        {
            cost += CountOnes(bit, start, r);
        }

        return cost;
    }

    private int CountOnes(int bit, int l, int r)
    {
        if (l > r) return 0;
        return _pref[bit][r + 1] - _pref[bit][l];
    }

    private static long Encode(int l, int r, int bit, int prevYBit)
    {
        long key = l;
        key = (key << 17) ^ r;
        key = (key << 6) ^ bit;
        key = (key << 1) ^ prevYBit;
        return key;
    }
}

// Demo code
var solution = new Solution();

int[] nums1 = { 3, 1, 2 };
int result1 = solution.MinBitFlips(nums1);
Console.WriteLine($"nums = [{string.Join(", ", nums1)}], minimum flips = {result1}");

int[] nums2 = { 0, 7, 7 };
int result2 = solution.MinBitFlips(nums2);
Console.WriteLine($"nums = [{string.Join(", ", nums2)}], minimum flips = {result2}");

int[] nums3 = { 1 };
int result3 = solution.MinBitFlips(nums3);
Console.WriteLine($"nums = [{string.Join(", ", nums3)}], minimum flips = {result3}");

int[] nums4 = { 1, 1, 1 };
int result4 = solution.MinBitFlips(nums4);
Console.WriteLine($"nums = [{string.Join(", ", nums4)}], minimum flips = {result4}");