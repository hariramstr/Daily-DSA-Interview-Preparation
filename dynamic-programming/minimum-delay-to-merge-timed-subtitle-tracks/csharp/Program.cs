/*
Title: Minimum Delay to Merge Timed Subtitle Tracks

Problem Description:
A streaming platform receives a spoken sentence split across two subtitle tracks, A and B.
Each track is a string of lowercase letters, where each character represents one subtitle token in order.

We must build a final merged subtitle by repeatedly taking the next unused character from either A or B
until both tracks are exhausted. The relative order of characters taken from the same track must remain unchanged.

Cost rules for every adjacent pair in the merged string:
1. Add 1 if the source track changes between the two adjacent chosen characters.
2. Add 1 if the two adjacent characters are equal.

Goal:
Compute the minimum total cost over all valid interleavings of A and B.

Constraints:
- 1 <= |A|, |B| <= 2000
- A and B contain only lowercase English letters
- Expected solution: O(|A| * |B|) dynamic programming
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(n * m), where n = A.Length and m = B.Length

    Space Complexity:
    O(n * m)

    Beginner-friendly idea:
    -----------------------
    We build the merged string one character at a time.

    At any moment, suppose:
    - we have already used the first i characters of A
    - we have already used the first j characters of B

    Then the merged string length so far is i + j.

    The important extra detail is:
    What track did the LAST chosen character come from?

    Why do we need that?
    Because the next adjacent-pair cost depends on:
    - whether we switch tracks
    - whether the new character equals the previous character

    So we use dynamic programming with two states:
    - dpA[i, j] = minimum cost after using i chars from A and j chars from B,
                 with the last chosen character coming from A
    - dpB[i, j] = same idea, but the last chosen character comes from B

    Then we try to append either:
    - the next character from A
    - the next character from B

    and compute the extra adjacent cost caused by that append.
    */
    public int MinCostMerge(string A, string B)
    {
        int n = A.Length;
        int m = B.Length;

        // A very large number used as "infinity".
        // We use a safe value so that adding 1 or 2 will not overflow.
        const int INF = 1_000_000_000;

        // dpA[i, j]:
        // Minimum cost after consuming:
        // - A[0..i-1]  => i characters from A
        // - B[0..j-1]  => j characters from B
        // and the LAST chosen character is A[i-1].
        //
        // This state only makes sense when i > 0.
        int[,] dpA = new int[n + 1, m + 1];

        // dpB[i, j]:
        // Minimum cost after consuming i chars from A and j chars from B,
        // and the LAST chosen character is B[j-1].
        //
        // This state only makes sense when j > 0.
        int[,] dpB = new int[n + 1, m + 1];

        // Initialize all states to INF because we are minimizing.
        for (int i = 0; i <= n; i++)
        {
            for (int j = 0; j <= m; j++)
            {
                dpA[i, j] = INF;
                dpB[i, j] = INF;
            }
        }

        // Base cases:
        //
        // If we take the very first character from A, there is no previous character,
        // so there is no adjacent pair yet, hence cost = 0.
        if (n > 0)
        {
            dpA[1, 0] = 0;
        }

        // Similarly, if we take the very first character from B, cost = 0.
        if (m > 0)
        {
            dpB[0, 1] = 0;
        }

        // We now iterate over all reachable states.
        //
        // For each state, we try to append the next character from A or B.
        // The extra cost depends on:
        // - whether the source changes
        // - whether the character equals the previous character
        for (int i = 0; i <= n; i++)
        {
            for (int j = 0; j <= m; j++)
            {
                // ------------------------------------------------------------
                // Case 1: current state ends with a character from A
                // ------------------------------------------------------------
                if (i > 0 && dpA[i, j] < INF)
                {
                    // The previous character in the merged string is A[i - 1].
                    char prev = A[i - 1];

                    // Option 1a: append next character from A, if available.
                    //
                    // Source does NOT change, so switch cost = 0.
                    // We only pay noise cost if the new character equals prev.
                    if (i < n)
                    {
                        char next = A[i];
                        int extraCost = 0;

                        // Same adjacent character => noise penalty
                        if (prev == next)
                        {
                            extraCost += 1;
                        }

                        // Update dpA because after appending from A,
                        // the last chosen character is still from A.
                        if (dpA[i + 1, j] > dpA[i, j] + extraCost)
                        {
                            dpA[i + 1, j] = dpA[i, j] + extraCost;
                        }
                    }

                    // Option 1b: append next character from B, if available.
                    //
                    // Source DOES change, so switch cost = 1.
                    // Also pay noise cost if the new character equals prev.
                    if (j < m)
                    {
                        char next = B[j];
                        int extraCost = 1; // switching from A to B

                        if (prev == next)
                        {
                            extraCost += 1;
                        }

                        // Update dpB because after appending from B,
                        // the last chosen character is now from B.
                        if (dpB[i, j + 1] > dpA[i, j] + extraCost)
                        {
                            dpB[i, j + 1] = dpA[i, j] + extraCost;
                        }
                    }
                }

                // ------------------------------------------------------------
                // Case 2: current state ends with a character from B
                // ------------------------------------------------------------
                if (j > 0 && dpB[i, j] < INF)
                {
                    // The previous character in the merged string is B[j - 1].
                    char prev = B[j - 1];

                    // Option 2a: append next character from B, if available.
                    //
                    // Source does NOT change, so switch cost = 0.
                    // Only possible extra cost is repeated adjacent character.
                    if (j < m)
                    {
                        char next = B[j];
                        int extraCost = 0;

                        if (prev == next)
                        {
                            extraCost += 1;
                        }

                        if (dpB[i, j + 1] > dpB[i, j] + extraCost)
                        {
                            dpB[i, j + 1] = dpB[i, j] + extraCost;
                        }
                    }

                    // Option 2b: append next character from A, if available.
                    //
                    // Source changes from B to A => switch cost = 1.
                    // Also add noise cost if characters are equal.
                    if (i < n)
                    {
                        char next = A[i];
                        int extraCost = 1; // switching from B to A

                        if (prev == next)
                        {
                            extraCost += 1;
                        }

                        if (dpA[i + 1, j] > dpB[i, j] + extraCost)
                        {
                            dpA[i + 1, j] = dpB[i, j] + extraCost;
                        }
                    }
                }
            }
        }

        // Final answer:
        // After consuming all characters from both strings,
        // the merged string must end either with a character from A or from B.
        return Math.Min(dpA[n, m], dpB[n, m]);
    }
}

// Demo code
var solution = new Solution();

string a1 = "ab";
string b1 = "ac";
int result1 = solution.MinCostMerge(a1, b1);
Console.WriteLine($"A = \"{a1}\", B = \"{b1}\" => Minimum Cost = {result1}");

string a2 = "aaa";
string b2 = "aa";
int result2 = solution.MinCostMerge(a2, b2);
Console.WriteLine($"A = \"{a2}\", B = \"{b2}\" => Minimum Cost = {result2}");

// Additional quick checks
string a3 = "a";
string b3 = "b";
int result3 = solution.MinCostMerge(a3, b3);
Console.WriteLine($"A = \"{a3}\", B = \"{b3}\" => Minimum Cost = {result3}");

string a4 = "abc";
string b4 = "def";
int result4 = solution.MinCostMerge(a4, b4);
Console.WriteLine($"A = \"{a4}\", B = \"{b4}\" => Minimum Cost = {result4}");