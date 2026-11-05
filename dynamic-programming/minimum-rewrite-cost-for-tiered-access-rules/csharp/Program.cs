/*
Title: Minimum Rewrite Cost for Tiered Access Rules

Problem Description:
A security team stores an access policy as a string s of length n, where each character is one of A, B, or C.
The company wants to rewrite the policy so that all rules appear in nondecreasing tier order:
first zero or more A rules, then zero or more B rules, then zero or more C rules.

Any of the three groups may be empty, but after rewriting the final string must have the form:
A...AB...BC...C

For each position i, changing s[i] to A, B, or C has different costs given by arrays costA, costB, and costC,
where costX[i] is the cost to rewrite character s[i] into tier X.

Keeping the original character is allowed and may still have cost 0 or a positive value depending on the input.

Task:
Compute the minimum total rewrite cost needed to make the entire policy valid.

Constraints:
- 1 <= n <= 200000
- s.length == n
- s[i] is one of A, B, or C
- 0 <= costA[i], costB[i], costC[i] <= 10^9
- Return the minimum possible total cost as a 64-bit integer
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(n)
    We process each position exactly once, and for each position we update only 3 DP states.

    Space Complexity:
    O(1)
    We keep only the previous values of 3 states instead of a full DP table.

    Beginner-friendly idea:
    We scan the string from left to right.
    At every position, we decide whether the rewritten character belongs to:
    - the A block
    - the B block
    - the C block

    Because the final string must be nondecreasing (all A's, then B's, then C's),
    once we move from A to B, we can never go back to A.
    Once we move from B to C, we can never go back to B or A.

    So we maintain 3 DP states for the prefix processed so far:
    - dpA: minimum cost if the rewritten prefix ends in the A phase
    - dpB: minimum cost if the rewritten prefix ends in the B phase
    - dpC: minimum cost if the rewritten prefix ends in the C phase

    "Ends in the A phase" means every processed character has been assigned to A.
    "Ends in the B phase" means we may have had some A's before, and now we are in B's.
    "Ends in the C phase" means we may have had A's and/or B's before, and now we are in C's.

    Since any group may be empty:
    - We are allowed to start directly in B (A block empty)
    - We are allowed to start directly in C (A and B blocks empty)
    - We are allowed to move A -> B -> C at any time
    */
    public long MinimumRewriteCost(string s, long[] costA, long[] costB, long[] costC)
    {
        int n = s.Length;

        // These three variables represent the minimum cost after processing the current prefix.
        //
        // dpA:
        //   Minimum cost if all processed characters are assigned to the A block.
        //
        // dpB:
        //   Minimum cost if the processed prefix is valid and we are currently in the B block.
        //   That means the prefix looks like A...AB...B, where the A part may be empty.
        //
        // dpC:
        //   Minimum cost if the processed prefix is valid and we are currently in the C block.
        //   That means the prefix looks like A...AB...BC...C, where A and/or B parts may be empty.
        //
        // Initialization for an empty prefix:
        // - Cost is 0 for all three states.
        //
        // Why is this correct?
        // Because empty groups are allowed.
        // Before processing any character:
        // - We can think of ourselves as still in A phase with cost 0.
        // - Or already in B phase with empty A and empty processed prefix, cost 0.
        // - Or already in C phase with empty A and B and empty processed prefix, cost 0.
        //
        // This elegantly handles cases where the optimal final string starts with B or C.
        long dpA = 0;
        long dpB = 0;
        long dpC = 0;

        for (int i = 0; i < n; i++)
        {
            // Cost of rewriting current position i into each possible target letter.
            long toA = costA[i];
            long toB = costB[i];
            long toC = costC[i];

            // We save previous state values because all three new states
            // must be computed from the states of the previous prefix.
            long prevA = dpA;
            long prevB = dpB;
            long prevC = dpC;

            // ------------------------------------------------------------
            // Compute new dpA
            // ------------------------------------------------------------
            //
            // If the current prefix ends in A phase, then the current character
            // must also be assigned to A.
            //
            // Also, if we are in A phase now, we must have already been in A phase before.
            // We cannot come from B or C back to A because that would violate nondecreasing order.
            //
            // Therefore:
            // new dpA = previous dpA + cost to make current char an A
            long newA = prevA + toA;

            // ------------------------------------------------------------
            // Compute new dpB
            // ------------------------------------------------------------
            //
            // If the current prefix ends in B phase, then the current character
            // must be assigned to B.
            //
            // We can arrive here in two valid ways:
            // 1. We were already in B phase before, and continue placing B.
            // 2. We were in A phase before, and now start the B phase at this position.
            //
            // We take the cheaper of those two possibilities.
            long newB = Math.Min(prevA, prevB) + toB;

            // ------------------------------------------------------------
            // Compute new dpC
            // ------------------------------------------------------------
            //
            // If the current prefix ends in C phase, then the current character
            // must be assigned to C.
            //
            // We can arrive here in three valid ways:
            // 1. We were already in C phase before, and continue placing C.
            // 2. We were in B phase before, and now start the C phase.
            // 3. We were in A phase before, and start C directly, meaning B block is empty.
            //
            // Because empty groups are allowed, all three are valid.
            long newC = Math.Min(prevA, Math.Min(prevB, prevC)) + toC;

            // Update DP states for the next iteration.
            dpA = newA;
            dpB = newB;
            dpC = newC;
        }

        // After processing all positions, any of the three ending phases is valid:
        // - all A's
        // - A's then B's
        // - A's then B's then C's
        //
        // Since empty groups are allowed, all of these represent valid final strings.
        return Math.Min(dpA, Math.Min(dpB, dpC));
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
string s1 = "CBAC";
long[] costA1 = { 3, 0, 2, 4 };
long[] costB1 = { 1, 5, 0, 2 };
long[] costC1 = { 0, 3, 4, 0 };

long result1 = solution.MinimumRewriteCost(s1, costA1, costB1, costC1);
Console.WriteLine(result1);

// Example 2
string s2 = "ACBAA";
long[] costA2 = { 0, 4, 3, 0, 0 };
long[] costB2 = { 2, 1, 1, 2, 2 };
long[] costC2 = { 5, 0, 0, 3, 4 };

long result2 = solution.MinimumRewriteCost(s2, costA2, costB2, costC2);
Console.WriteLine(result2);