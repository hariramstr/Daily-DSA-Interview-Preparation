/*
Title: Minimum XOR Patches to Reach Every Permission Mask

Problem Description:
A security platform stores user roles as bitmasks. You are given an array `roles` of length `n`,
where each value is an integer in the range `[0, 2^b - 1]` representing a currently deployed role
mask over `b` permission bits.

You may deploy additional role masks, called patches. After patching, the system is considered
fully expressive if every mask in `[0, 2^b - 1]` can be formed as the bitwise XOR of some subset
of the deployed masks (original roles plus patches). Each deployed mask may be used at most once
in a subset, and the empty subset produces `0`.

Return the minimum number of patches required to make the system fully expressive.

Key Insight:
The set of all XOR combinations of the deployed masks forms a vector space over GF(2)
(the field with two elements: 0 and 1). The number of distinct masks that can be generated
is determined by the number of linearly independent masks among the deployed masks.

If the current deployed masks have XOR-basis dimension `rank`, then they can generate exactly
`2^rank` distinct masks. To generate every possible `b`-bit mask, we need the span dimension
to be exactly `b`. Therefore, the minimum number of additional independent masks required is:

    answer = b - rank

So the task reduces to computing the rank of the given masks under XOR using Gaussian elimination
over bits.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(n * b)
    - For each role mask, we may inspect up to `b` bit positions while trying to insert it
      into the XOR basis.

    Space Complexity:
    O(b)
    - We store one basis value per bit position.

    Beginner-friendly explanation:
    We build an XOR basis, which is similar in spirit to Gaussian elimination.
    Each basis entry represents one independent mask whose highest set bit is unique.
    When a new mask arrives:
      1. We try to eliminate its highest set bits using masks already in the basis.
      2. If it becomes 0, it was redundant.
      3. Otherwise, it introduces a new independent direction in the XOR space.
    The number of independent directions is the rank.
    To span all `b` bits, we need rank = b, so patches needed = b - rank.
    */
    public int MinimumXorPatches(long[] roles, int b)
    {
        // `basis[bit]` will store one representative mask whose highest set bit is exactly `bit`.
        //
        // Why this structure works:
        // - In XOR linear algebra, each independent vector can be associated with a pivot bit.
        // - Just like row-reduction in Gaussian elimination, we want each pivot bit to be owned
        //   by exactly one basis vector.
        // - This lets us reduce future masks efficiently.
        //
        // Since b <= 60, a simple fixed-size array is perfect:
        // - very small
        // - very fast
        // - easy to understand
        long[] basis = new long[b];

        // `rank` counts how many independent masks we have found so far.
        int rank = 0;

        // Process every existing deployed role mask.
        foreach (long originalMask in roles)
        {
            // We will try to insert this mask into the basis.
            // `x` is a working copy because we will modify it during reduction.
            long x = originalMask;

            // We inspect bits from high to low.
            //
            // Why high to low?
            // - We want to eliminate the highest set bit first.
            // - This mirrors Gaussian elimination where we work with leading pivots.
            // - It guarantees a canonical and efficient basis structure.
            for (int bit = b - 1; bit >= 0; bit--)
            {
                // Check whether the current bit is set in x.
                //
                // If it is not set, this bit is irrelevant for the current reduction step,
                // so we continue to the next lower bit.
                if (((x >> bit) & 1L) == 0)
                {
                    continue;
                }

                // If we already have a basis vector with this bit as its pivot,
                // we can XOR it with x to eliminate that bit from x.
                //
                // Why is this valid?
                // - XOR is the addition operation in GF(2).
                // - Using an existing basis vector does not change whether x is in the span;
                //   it only rewrites x into a reduced form.
                if (basis[bit] != 0)
                {
                    x ^= basis[bit];
                }
                else
                {
                    // No basis vector currently owns this pivot bit.
                    // That means x cannot be formed from the existing basis,
                    // so it is a NEW independent vector.
                    //
                    // We store it as the basis vector for this pivot bit.
                    basis[bit] = x;

                    // Since we discovered one more independent direction,
                    // increase the rank.
                    rank++;

                    // Important:
                    // Once inserted, we stop processing this mask.
                    //
                    // Why?
                    // - We have already established that it adds one new dimension.
                    // - The exact lower-bit cleanup is unnecessary for computing rank.
                    // - For future reductions, having the pivot bit assigned is enough.
                    break;
                }
            }

            // If x was reduced all the way to 0, then it was redundant:
            // it can already be formed by XOR-ing previous masks.
            // In that case, rank does not change.
        }

        // To generate every b-bit mask, the XOR span must have dimension exactly b.
        // If current rank is smaller, each missing dimension requires one new independent patch.
        //
        // Minimum patches = missing dimensions.
        return b - rank;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1:
// roles = [1, 2], b = 3
// Current independent masks:
//   1 = 001
//   2 = 010
// They are independent, so rank = 2.
// Need full dimension 3, so answer = 1.
long[] roles1 = { 1, 2 };
int b1 = 3;
int result1 = solution.MinimumXorPatches(roles1, b1);
Console.WriteLine(result1); // Expected: 1

// Example 2:
// roles = [3, 5, 6], b = 3
// Binary:
//   3 = 011
//   5 = 101
//   6 = 110
// Note: 3 XOR 5 = 6, so only two are independent.
// rank = 2, so one patch is needed to reach dimension 3.
long[] roles2 = { 3, 5, 6 };
int b2 = 3;
int result2 = solution.MinimumXorPatches(roles2, b2);
Console.WriteLine(result2); // Expected: 1

// Additional quick sanity checks:

// Already fully expressive for b = 2:
// [1, 2] spans {0,1,2,3}, so no patch needed.
long[] roles3 = { 1, 2 };
int b3 = 2;
int result3 = solution.MinimumXorPatches(roles3, b3);
Console.WriteLine(result3); // Expected: 0

// Only zero mask present for b = 4:
// zero contributes no independence, so rank = 0.
// Need 4 independent patches.
long[] roles4 = { 0 };
int b4 = 4;
int result4 = solution.MinimumXorPatches(roles4, b4);
Console.WriteLine(result4); // Expected: 4