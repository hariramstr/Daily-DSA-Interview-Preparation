/*
Title: Minimum Rewrites to Match a Layered Bit Template

Problem Description:
You are given two integers, n and target, and an array costs of length n. You must build a nonnegative integer x using exactly n bits, where bit i (0-indexed from the least significant bit) may be rewritten at a cost of costs[i]. Initially, x is equal to 0, so every bit is unset.

A security system checks x through a layered template rule. For every k from 1 to n, let low(k) be the integer formed by the lowest k bits of x. The template is satisfied if, for every k, the number of set bits in low(k) has the same parity as the k-th lowest bit of target. In other words, for each prefix of bits from the least significant side, the parity of that prefix in x must match a required parity sequence derived from target.

You may choose any bits of x to flip from 0 to 1, paying the corresponding rewrite costs. Return the minimum total cost needed to construct such an x. If no such x exists, return -1.

Key Observation:
Let p[k] be the required parity for the lowest k bits of x.
Then:
- p[k] = bit (k - 1) of target
- parity(low(k)) = parity(low(k - 1)) XOR x[k - 1]

So each bit of x is forced:
- x[0] = p[1]
- x[i] = p[i] XOR p[i + 1] for i >= 1

Equivalently, if t_i is bit i of target:
- x[0] = t_0
- x[i] = t_(i - 1) XOR t_i for 1 <= i <= n - 1

Therefore the valid x is unique. Since all costs are positive and we only pay for bits set to 1,
the minimum cost is simply the sum of costs[i] for all forced 1-bits in x.

Because target is guaranteed to be in [0, 2^n), the construction is always possible.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1) extra space

    We scan the bits from low to high exactly once.
    For each position i, we determine whether x[i] must be 1 using the parity-difference rule.
    If it must be 1, we add costs[i] to the answer.
    */
    public long MinimumRewriteCost(int n, long target, int[] costs)
    {
        // This variable will store the final minimum total cost.
        // We use long because:
        // - each individual cost can be as large as 1e9
        // - n can be as large as 200000
        // - the total sum can therefore exceed the range of int
        long totalCost = 0;

        // "previousRequiredParity" represents the required parity of the previous prefix.
        //
        // More concretely:
        // - before processing any bits, the prefix length is 0
        // - the number of set bits in an empty prefix is 0
        // - parity(0) = 0
        //
        // So the required parity for the "prefix of length 0" is naturally 0.
        // This lets us write one uniform transition formula:
        //
        // x[i] = requiredParityOfPrefix(i+1) XOR requiredParityOfPrefix(i)
        //
        // where requiredParityOfPrefix(i+1) is simply bit i of target.
        int previousRequiredParity = 0;

        // We process each bit position from least significant to most significant.
        // This direction is important because the problem defines prefix parities
        // over the lowest k bits, so each new bit extends the previous prefix by one bit.
        for (int i = 0; i < n; i++)
        {
            // Extract the i-th bit of target.
            //
            // This bit tells us the required parity of the lowest (i + 1) bits of x.
            // Example:
            // - if target's bit i is 1, then the lowest (i + 1) bits of x must contain
            //   an odd number of set bits
            // - if target's bit i is 0, then that prefix must contain an even number of set bits
            int currentRequiredParity = (int)((target >> i) & 1L);

            // Determine the forced value of x[i].
            //
            // Why XOR?
            // Suppose:
            // - parity of first i bits is previousRequiredParity
            // - parity of first i+1 bits must be currentRequiredParity
            //
            // Adding bit x[i] changes parity exactly when x[i] = 1.
            // Therefore:
            // currentRequiredParity = previousRequiredParity XOR x[i]
            //
            // Rearranging:
            // x[i] = previousRequiredParity XOR currentRequiredParity
            int forcedBit = previousRequiredParity ^ currentRequiredParity;

            // If the forced bit is 1, then we must flip this bit from 0 to 1,
            // so we must pay its rewrite cost.
            //
            // Since the valid assignment is unique, there is no choice here.
            if (forcedBit == 1)
            {
                totalCost += costs[i];
            }

            // Move forward:
            // the current required parity becomes the "previous" one for the next step.
            previousRequiredParity = currentRequiredParity;
        }

        // Because every target bit sequence defines exactly one x through parity differences,
        // and target is guaranteed to fit in n bits, a valid solution always exists.
        return totalCost;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the prompt.
// Important note for learners:
// The narrative in the prompt is internally inconsistent.
// The mathematically correct interpretation of the stated rule gives a unique x.
//
// n = 4, target = 11 = binary 1011
// target bits from low to high: [1, 1, 0, 1]
//
// Forced x bits:
// x[0] = 1
// x[1] = 1 XOR 1 = 0
// x[2] = 1 XOR 0 = 1
// x[3] = 0 XOR 1 = 1
//
// So x = bits [1,0,1,1], i.e. binary 1101.
// Cost = costs[0] + costs[2] + costs[3] = 5 + 7 + 1 = 13
int n1 = 4;
long target1 = 11;
int[] costs1 = { 5, 2, 7, 1 };
Console.WriteLine(solution.MinimumRewriteCost(n1, target1, costs1)); // Correct result under the stated rule: 13

// Example 2 from the prompt.
// Again, applying the rule exactly:
//
// n = 5, target = 6 = binary 00110
// target bits from low to high: [0, 1, 1, 0, 0]
//
// Forced x bits:
// x[0] = 0
// x[1] = 0 XOR 1 = 1
// x[2] = 1 XOR 1 = 0
// x[3] = 1 XOR 0 = 1
// x[4] = 0 XOR 0 = 0
//
// So x has set bits at positions 1 and 3.
// Cost = 9 + 3 = 12
int n2 = 5;
long target2 = 6;
int[] costs2 = { 4, 9, 1, 3, 8 };
Console.WriteLine(solution.MinimumRewriteCost(n2, target2, costs2)); // Correct result under the stated rule: 12

// Additional small sanity check:
// n = 3, target = 0 => all required prefix parities are 0
// Then x must be 000, so cost is 0.
int n3 = 3;
long target3 = 0;
int[] costs3 = { 10, 20, 30 };
Console.WriteLine(solution.MinimumRewriteCost(n3, target3, costs3)); // 0