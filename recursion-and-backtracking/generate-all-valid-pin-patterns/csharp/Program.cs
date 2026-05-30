/*
 * Generate All Valid PIN Patterns
 * ================================
 * A PIN pattern is a sequence of digits from 1 to 9 where each digit is used at most once.
 * A PIN pattern is valid if:
 *   1. Its length is between minLen and maxLen (inclusive).
 *   2. Each consecutive pair of digits satisfies: if they are NOT adjacent on the 3x3 grid
 *      (horizontally, vertically, or diagonally), then ALL digits lying on the straight line
 *      between them must have already been used in the pattern before that move.
 *
 * The 3x3 grid layout:
 *   1 2 3
 *   4 5 6
 *   7 8 9
 *
 * Examples:
 *   - Moving from 1 to 3 requires 2 to be visited first.
 *   - Moving from 1 to 9 requires 5 to be visited first.
 *   - Moving from 1 to 7 requires 4 to be visited first.
 *
 * Constraints: 1 <= minLen <= maxLen <= 9
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution Class
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    // -------------------------------------------------------------------------
    // skip[a, b] = the digit that MUST be visited before moving from digit a to
    // digit b (if any). A value of 0 means no intermediate digit is required
    // (i.e., a and b are adjacent or the move is always allowed).
    //
    // Digits are 1-indexed (1..9), so we use a 10x10 array for convenience.
    // -------------------------------------------------------------------------
    private readonly int[,] skip = new int[10, 10];

    // -------------------------------------------------------------------------
    // NumberOfPatterns
    //
    // Time Complexity:  O(9!) in the worst case — we explore all permutations
    //                   of 9 digits, but pruning (skip rules) cuts this down
    //                   significantly in practice.
    // Space Complexity: O(9) for the recursion call stack depth and the
    //                   visited boolean array.
    // -------------------------------------------------------------------------
    public int NumberOfPatterns(int minLen, int maxLen)
    {
        // ── Step 1: Build the "skip" table ────────────────────────────────────
        // For every pair of digits (a, b), determine if there is a digit that
        // lies exactly between them on the grid. If so, that digit must be
        // visited before the move a→b is legal.
        //
        // We only need to fill in the non-trivial cases (pairs that are NOT
        // direct neighbours). All other entries remain 0 (no skip required).
        //
        // Why a lookup table? Checking geometry at runtime every step would be
        // more complex and slower. A pre-built table makes the DFS clean.

        // Horizontal skips (same row, two apart)
        skip[1, 3] = skip[3, 1] = 2;   // 1 ─ 2 ─ 3
        skip[4, 6] = skip[6, 4] = 5;   // 4 ─ 5 ─ 6
        skip[7, 9] = skip[9, 7] = 8;   // 7 ─ 8 ─ 9

        // Vertical skips (same column, two apart)
        skip[1, 7] = skip[7, 1] = 4;   // 1 │ 4 │ 7
        skip[2, 8] = skip[8, 2] = 5;   // 2 │ 5 │ 8
        skip[3, 9] = skip[9, 3] = 6;   // 3 │ 6 │ 9

        // Diagonal skips (corner to corner through centre)
        skip[1, 9] = skip[9, 1] = 5;   // 1 ╲ 5 ╲ 9
        skip[3, 7] = skip[7, 3] = 5;   // 3 ╱ 5 ╱ 7

        // ── Step 2: Initialise the visited array ──────────────────────────────
        // visited[i] = true means digit i is already part of the current pattern.
        // We use a 10-element array (index 0 unused) for 1-based digit access.
        bool[] visited = new bool[10];

        // ── Step 3: Count valid patterns using DFS/backtracking ───────────────
        // We accumulate the total count across all starting digits and all
        // valid lengths.
        int totalCount = 0;

        // ── Step 4: Exploit symmetry to reduce work ───────────────────────────
        // The 3x3 grid has 3 symmetry classes:
        //   • Corners : 1, 3, 7, 9  → 4 equivalent starting points
        //   • Edges   : 2, 4, 6, 8  → 4 equivalent starting points
        //   • Centre  : 5           → 1 starting point
        //
        // We compute the count for ONE representative of each class and multiply
        // by the class size. This cuts the work roughly by 3×.

        // Corners (representative: digit 1) — multiply result by 4
        totalCount += DFS(1, visited, 1, minLen, maxLen) * 4;

        // Edges (representative: digit 2) — multiply result by 4
        totalCount += DFS(2, visited, 1, minLen, maxLen) * 4;

        // Centre (representative: digit 5) — multiply result by 1
        totalCount += DFS(5, visited, 1, minLen, maxLen) * 1;

        return totalCount;
    }

    // -------------------------------------------------------------------------
    // DFS (Depth-First Search / Backtracking)
    //
    // Parameters:
    //   current  – the digit we just placed in the pattern
    //   visited  – which digits have been used so far
    //   length   – current length of the pattern (number of digits placed)
    //   minLen   – minimum valid pattern length
    //   maxLen   – maximum valid pattern length
    //
    // Returns the number of valid patterns that START from the initial call's
    // digit and have lengths in [minLen, maxLen].
    // -------------------------------------------------------------------------
    private int DFS(int current, bool[] visited, int length, int minLen, int maxLen)
    {
        // ── Step A: Mark the current digit as visited ─────────────────────────
        // We are now "standing on" this digit; it is part of the current pattern.
        visited[current] = true;

        // ── Step B: Count this pattern if its length is within the valid range ─
        // Even if we can extend further, a pattern of length ≥ minLen is already
        // a valid answer on its own.
        int count = 0;
        if (length >= minLen)
        {
            // This pattern (of the current length) is valid — count it.
            count = 1;
        }

        // ── Step C: Try extending the pattern if we haven't hit maxLen yet ─────
        if (length < maxLen)
        {
            // Try every digit as the next step in the pattern.
            for (int next = 1; next <= 9; next++)
            {
                // ── Step C1: Skip already-visited digits ──────────────────────
                // We cannot reuse a digit that is already in the pattern.
                if (visited[next])
                    continue;

                // ── Step C2: Check the skip rule ──────────────────────────────
                // skip[current, next] gives the intermediate digit that must be
                // visited before we can move from 'current' to 'next'.
                // If skip[current, next] == 0, no intermediate is required
                // (they are adjacent or the move is always legal).
                int intermediate = skip[current, next];

                if (intermediate != 0 && !visited[intermediate])
                {
                    // The required intermediate digit has NOT been visited yet,
                    // so this move is ILLEGAL. Skip it.
                    continue;
                }

                // ── Step C3: Recurse — place 'next' and explore further ────────
                // We go one level deeper, extending the pattern by one digit.
                count += DFS(next, visited, length + 1, minLen, maxLen);
            }
        }

        // ── Step D: Backtrack — unmark the current digit ──────────────────────
        // We are done exploring all patterns that include 'current' at this
        // position. Remove it from visited so the caller can try other paths.
        visited[current] = false;

        return count;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Test Code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

var solution = new Solution();

Console.WriteLine("=== Generate All Valid PIN Patterns ===");
Console.WriteLine();

// ── Test Case 1 ──────────────────────────────────────────────────────────────
// minLen = 1, maxLen = 1
// Expected output: 9  (all single-digit patterns are trivially valid)
int minLen1 = 1, maxLen1 = 1;
int result1 = solution.NumberOfPatterns(minLen1, maxLen1);
Console.WriteLine($"Test 1: minLen={minLen1}, maxLen={maxLen1}");
Console.WriteLine($"  Result   : {result1}");
Console.WriteLine($"  Expected : 9");
Console.WriteLine($"  Pass     : {result1 == 9}");
Console.WriteLine();

// ── Test Case 2 ──────────────────────────────────────────────────────────────
// minLen = 1, maxLen = 2
// Expected output: 65  (9 single-digit + 56 valid two-digit patterns)
int minLen2 = 1, maxLen2 = 2;
int result2 = solution.NumberOfPatterns(minLen2, maxLen2);
Console.WriteLine($"Test 2: minLen={minLen2}, maxLen={maxLen2}");
Console.WriteLine($"  Result   : {result2}");
Console.WriteLine($"  Expected : 65");
Console.WriteLine($"  Pass     : {result2 == 65}");
Console.WriteLine();

// ── Additional Tests ──────────────────────────────────────────────────────────

// minLen = 4, maxLen = 4  → well-known answer is 1624
int minLen3 = 4, maxLen3 = 4;
int result3 = solution.NumberOfPatterns(minLen3, maxLen3);
Console.WriteLine($"Test 3: minLen={minLen3}, maxLen={maxLen3}");
Console.WriteLine($"  Result   : {result3}");
Console.WriteLine($"  Expected : 1624");
Console.WriteLine($"  Pass     : {result3 == 1624}");
Console.WriteLine();

// minLen = 1, maxLen = 9  → total valid patterns of all lengths
int minLen4 = 1, maxLen4 = 9;
int result4 = solution.NumberOfPatterns(minLen4, maxLen4);
Console.WriteLine($"Test 4: minLen={minLen4}, maxLen={maxLen4}");
Console.WriteLine($"  Result   : {result4}");
Console.WriteLine($"  Expected : 389112");
Console.WriteLine($"  Pass     : {result4 == 389112}");
Console.WriteLine();

// minLen = 2, maxLen = 2  → 56 valid two-digit patterns
int minLen5 = 2, maxLen5 = 2;
int result5 = solution.NumberOfPatterns(minLen5, maxLen5);
Console.WriteLine($"Test 5: minLen={minLen5}, maxLen={maxLen5}");
Console.WriteLine($"  Result   : {result5}");
Console.WriteLine($"  Expected : 56");
Console.WriteLine($"  Pass     : {result5 == 56}");
Console.WriteLine();

Console.WriteLine("=== All tests complete ===");