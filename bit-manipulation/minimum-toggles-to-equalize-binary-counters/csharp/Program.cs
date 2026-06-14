/*
Title: Minimum Toggles to Equalize Binary Counters
Difficulty: Medium
Topic: Bit Manipulation

Problem Description:
You are given an array `counters` of `n` non-negative integers. Each integer represents the current value of a hardware counter.
In one operation, you may choose a bit position `b` (0-indexed) and toggle that bit in exactly two different counters.
Toggling means changing a `0` to `1` or a `1` to `0` at that bit position.

Your task is to determine the minimum number of operations required to make all counters equal.
If it is impossible, return `-1`.

This operation models a paired electrical pulse: each pulse must affect the same bit position in two different devices at the same time.
Because of this restriction, you cannot freely change bits one counter at a time.

Return the smallest number of such paired toggles needed so that every value in the array becomes identical.

Important observations:
- You may perform operations on different bit positions independently.
- The final common value does not need to be one of the original values.
- Two counters chosen in an operation must be distinct.

Constraints:
- 1 <= n <= 10^5
- 0 <= counters[i] <= 10^9

Example 1:
Input: counters = [1, 0, 1, 0]
Output: 1

Example 2:
Input: counters = [3, 3, 1]
Output: -1
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * B), where:
      n = number of counters
      B = number of bit positions we inspect
    - Since counters[i] <= 1e9, 31 bits are enough (bits 0 through 30).
    - Therefore this is effectively O(n).

    Space Complexity:
    - O(1) extra space
    - We only use a few integer variables, independent of input size.

    Core idea:
    For each bit position, we only care about how many counters currently have a 1 at that bit.

    Let:
    - ones = number of counters with bit b = 1
    - zeros = n - ones

    In one operation at bit b, we toggle that bit in exactly two different counters.
    This means:
    - toggling two 1s decreases ones by 2
    - toggling two 0s increases ones by 2
    - toggling one 1 and one 0 keeps ones unchanged

    So the parity (odd/even) of ones never changes.

    To make all counters equal, for every bit position b, all counters must end with the same bit:
    - either all 0 at bit b  => final ones count = 0
    - or all 1 at bit b      => final ones count = n

    Therefore:
    - If n is even:
      both 0 and n are even, so ones must be even to be reachable.
    - If n is odd:
      0 is even and n is odd, so any parity is acceptable:
        * if ones is even, we can make all 0
        * if ones is odd,  we can make all 1

    Minimum operations for one bit:
    - If target is all 0, we must eliminate all current 1s.
      Each operation can remove at most two 1s by toggling two counters that currently have 1.
      So cost = ones / 2.
    - If target is all 1, we must convert all current 0s to 1.
      Each operation can remove at most two 0s by toggling two counters that currently have 0.
      So cost = zeros / 2.

    Since bit positions are independent, total answer is the sum of the minimum cost for each bit.
    */
    public long MinimumOperationsToEqualize(int[] counters)
    {
        // Step 1:
        // Read the number of counters.
        // We will need this often, especially to know whether n is odd or even.
        int n = counters.Length;

        // Special case:
        // If there is only one counter, then all counters are already equal
        // because there is only one value in the array.
        // No operation is needed.
        if (n == 1)
        {
            return 0;
        }

        // This variable will accumulate the minimum number of operations
        // across all bit positions.
        long totalOperations = 0;

        // Since counters[i] <= 1e9, the highest relevant bit is bit 30.
        // We inspect bits 0 through 30 inclusive.
        for (int bit = 0; bit <= 30; bit++)
        {
            // Step 2:
            // Count how many numbers currently have a 1 at this bit position.
            //
            // Why do we count this?
            // Because the entire behavior for a bit position depends only on
            // how many 1s and 0s exist there, not on which exact counters hold them.
            int ones = 0;

            foreach (int value in counters)
            {
                // Shift the desired bit to the least significant position,
                // then mask with 1 to extract that bit.
                //
                // Example:
                // value = 5 (binary 101), bit = 2
                // (5 >> 2) = 1, and 1 & 1 = 1
                //
                // If the result is 1, then this counter contributes one "1"
                // to the current bit position.
                ones += (value >> bit) & 1;
            }

            // Number of counters with a 0 at this bit.
            int zeros = n - ones;

            // Step 3:
            // Decide whether this bit can be made uniform across all counters.
            //
            // Important invariant:
            // Every operation toggles exactly two counters at the SAME bit.
            // Therefore the count of 1s changes by:
            // -2, 0, or +2
            //
            // That means the parity of "ones" never changes.
            //
            // To finish with all counters equal at this bit, the final count of 1s
            // must be either:
            // - 0   (all counters have bit 0)
            // - n   (all counters have bit 1)
            //
            // If n is even:
            // - both 0 and n are even
            // - so ones must be even, otherwise impossible
            //
            // If n is odd:
            // - 0 is even, n is odd
            // - so either parity is acceptable, and one of the two targets is reachable
            if (n % 2 == 0 && ones % 2 == 1)
            {
                // Impossible to fix this bit, so impossible overall.
                return -1;
            }

            // Step 4:
            // Compute the minimum number of operations needed for this bit.
            //
            // Case A: Make all bits 0 at this position.
            // We need to remove all current 1s.
            // One operation can flip two 1s to 0s, so cost is ones / 2.
            long costMakeAllZero = ones / 2;

            // Case B: Make all bits 1 at this position.
            // We need to remove all current 0s.
            // One operation can flip two 0s to 1s, so cost is zeros / 2.
            long costMakeAllOne = zeros / 2;

            // Step 5:
            // Choose the valid minimum cost for this bit.
            //
            // If n is even:
            // - only one parity is possible
            // - since ones must be even, zeros is also even
            // - both all-0 and all-1 are reachable
            // - choose the cheaper one
            //
            // If n is odd:
            // - if ones is even, only all-0 is reachable
            // - if ones is odd, only all-1 is reachable
            if (n % 2 == 0)
            {
                totalOperations += Math.Min(costMakeAllZero, costMakeAllOne);
            }
            else
            {
                if (ones % 2 == 0)
                {
                    totalOperations += costMakeAllZero;
                }
                else
                {
                    totalOperations += costMakeAllOne;
                }
            }
        }

        // After processing every bit independently,
        // totalOperations is the minimum number of paired toggles needed.
        return totalOperations;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// counters = [1, 0, 1, 0]
// Bit 0 has two 1s and two 0s.
// Minimum is 1 operation.
int[] counters1 = { 1, 0, 1, 0 };
Console.WriteLine(solution.MinimumOperationsToEqualize(counters1)); // Expected: 1

// Example 2:
// counters = [3, 3, 1]
// At bit 1, values are [1, 1, 0], so ones = 2? Let's verify carefully:
// 3 = binary 11
// 3 = binary 11
// 1 = binary 01
// Bit 1 values are [1, 1, 0], so ones = 2, zeros = 1.
// n = 3 (odd), so odd/even parity itself is not impossible.
// For odd n, if ones is even, target all 0 is reachable with ones/2 = 1 operation.
// Also bit 0 values are [1,1,1], already uniform.
// Therefore the correct minimum is actually 1, not -1 under the stated operation rules.
int[] counters2 = { 3, 3, 1 };
Console.WriteLine(solution.MinimumOperationsToEqualize(counters2)); // Under the described rules: 1

// Additional demo where answer is impossible:
// n is even, and at bit 0 there is an odd number of 1s.
int[] counters3 = { 1, 0 };
Console.WriteLine(solution.MinimumOperationsToEqualize(counters3)); // Expected: -1

// Additional demo:
// Already equal, so answer is 0.
int[] counters4 = { 7, 7, 7 };
Console.WriteLine(solution.MinimumOperationsToEqualize(counters4)); // Expected: 0

// Additional demo:
// [2, 0, 2, 0]
// bit 1 has two 1s and two 0s => 1 operation
int[] counters5 = { 2, 0, 2, 0 };
Console.WriteLine(solution.MinimumOperationsToEqualize(counters5)); // Expected: 1