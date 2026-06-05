/*
 * ============================================================
 * Title: Generate All Valid Locker Combinations
 * ============================================================
 * Problem Description:
 * You are managing a secure storage facility where each locker
 * is protected by a numeric combination lock. A valid combination
 * is a sequence of exactly n digits where:
 *
 *   1. Each digit is between 1 and k (inclusive).
 *   2. No two adjacent digits in the combination are the same.
 *
 * Given two integers n (the length of the combination) and k
 * (the maximum digit value), return ALL valid combinations in
 * lexicographically ascending order.
 *
 * Example 1:
 *   Input : n = 2, k = 3
 *   Output: ["12", "13", "21", "23", "31", "32"]
 *
 * Example 2:
 *   Input : n = 1, k = 4
 *   Output: ["1", "2", "3", "4"]
 *
 * Constraints:
 *   1 <= n <= 6
 *   1 <= k <= 9
 *   Total valid combinations will not exceed 10,000.
 * ============================================================
 */

using System;
using System.Collections.Generic;
using System.Text;

// ---------------------------------------------------------------
// Solution class encapsulates the algorithm cleanly so it can be
// reused or tested independently of the demo code below.
// ---------------------------------------------------------------
class Solution
{
    // ===========================================================
    // Method: GenerateCombinations
    // ===========================================================
    // Time Complexity : O(k * (k-1)^(n-1))
    //   - The first position has k choices.
    //   - Every subsequent position has (k-1) choices (any digit
    //     except the one just placed).
    //   - We visit every valid leaf, so the total work is
    //     proportional to the number of valid combinations.
    //
    // Space Complexity: O(n) for the recursion call stack depth
    //   plus O(k * (k-1)^(n-1)) to store the results.
    // ===========================================================
    public List<string> GenerateCombinations(int n, int k)
    {
        // -------------------------------------------------------
        // STEP 1 – Prepare the results container.
        // We use a List<string> to collect every valid combination
        // we discover during the backtracking search.
        // -------------------------------------------------------
        List<string> results = new List<string>();

        // -------------------------------------------------------
        // STEP 2 – Prepare a mutable "current combination" buffer.
        // StringBuilder is preferred over plain string concatenation
        // inside recursion because it avoids creating a new string
        // object on every append/remove operation, making the
        // backtracking (undo) step O(1) instead of O(n).
        // -------------------------------------------------------
        StringBuilder current = new StringBuilder();

        // -------------------------------------------------------
        // STEP 3 – Kick off the recursive backtracking.
        // We pass:
        //   current  – the combination built so far (starts empty)
        //   results  – the shared list we will add valid combos to
        //   n        – target length we must reach
        //   k        – upper bound on each digit (digits are 1..k)
        //   lastDigit – the digit placed in the previous position
        //               (0 means "no previous digit" since valid
        //               digits start at 1, so 0 never conflicts)
        // -------------------------------------------------------
        Backtrack(current, results, n, k, lastDigit: 0);

        // -------------------------------------------------------
        // STEP 4 – Return the fully populated results list.
        // Because we always try digits 1, 2, … k in ascending
        // order, the results are naturally in lexicographic order.
        // -------------------------------------------------------
        return results;
    }

    // ===========================================================
    // Private helper: Backtrack
    // ===========================================================
    // This is the heart of the algorithm. It builds the combination
    // one digit at a time. At each call we decide which digit to
    // place at the CURRENT position, then recurse to fill the rest.
    //
    // Parameters:
    //   current   – digits chosen so far (mutable, shared across calls)
    //   results   – accumulator for completed valid combinations
    //   n         – desired total length of the combination
    //   k         – digits range from 1 to k
    //   lastDigit – the digit placed at the previous position
    //               (used to enforce the "no two adjacent equal" rule)
    // ===========================================================
    private void Backtrack(
        StringBuilder current,
        List<string> results,
        int n,
        int k,
        int lastDigit)
    {
        // -------------------------------------------------------
        // BASE CASE – Have we placed exactly n digits?
        // If current.Length == n, the combination is complete and
        // satisfies all constraints (we enforced them during
        // construction), so we record it and return.
        // -------------------------------------------------------
        if (current.Length == n)
        {
            // Convert the StringBuilder to a string snapshot and
            // add it to our results list.
            results.Add(current.ToString());

            // Return to the caller so it can try the next digit.
            return;
        }

        // -------------------------------------------------------
        // RECURSIVE CASE – Try placing each digit 1 through k at
        // the current position.
        //
        // We iterate in ascending order (1, 2, … k) so that the
        // results list ends up in lexicographic order automatically
        // — no sorting step is needed afterwards.
        // -------------------------------------------------------
        for (int digit = 1; digit <= k; digit++)
        {
            // ---------------------------------------------------
            // PRUNING – Skip this digit if it equals the digit we
            // placed in the immediately preceding position.
            //
            // Why? Because the problem forbids two adjacent digits
            // from being the same. Skipping here avoids exploring
            // an entire subtree of invalid combinations, which is
            // the "pruning" part of backtracking.
            // ---------------------------------------------------
            if (digit == lastDigit)
            {
                // This digit would create an adjacent duplicate.
                // Move on to the next candidate digit.
                continue;
            }

            // ---------------------------------------------------
            // CHOOSE – Append the current digit to our combination.
            // We convert the integer digit to its character
            // representation by adding '0' (e.g., 3 + '0' = '3').
            // ---------------------------------------------------
            current.Append((char)('0' + digit));

            // ---------------------------------------------------
            // EXPLORE – Recurse to fill the next position.
            // We pass `digit` as the new `lastDigit` so the next
            // level knows what digit was just placed and can avoid
            // placing the same digit immediately after it.
            // ---------------------------------------------------
            Backtrack(current, results, n, k, lastDigit: digit);

            // ---------------------------------------------------
            // UN-CHOOSE (Backtrack) – Remove the digit we just
            // appended so we can try the next candidate digit in
            // the loop.
            //
            // This is the "backtrack" step that gives the technique
            // its name. By restoring `current` to its previous
            // state, we reuse the same StringBuilder object across
            // all recursive calls without interference.
            // ---------------------------------------------------
            current.Remove(current.Length - 1, count: 1);
        }

        // After the loop, all digits 1..k have been tried at this
        // position. We return to the previous call level, which
        // will then backtrack its own last choice and try the next
        // digit at that level.
    }
}

// ===============================================================
// DEMO CODE – Exercises the solution with the provided examples
// and prints results so you can verify correctness at a glance.
// ===============================================================

Solution solver = new Solution();

// ---------------------------------------------------------------
// Example 1: n = 2, k = 3
// Expected output: ["12", "13", "21", "23", "31", "32"]
// ---------------------------------------------------------------
Console.WriteLine("=== Example 1: n=2, k=3 ===");
List<string> result1 = solver.GenerateCombinations(n: 2, k: 3);
Console.WriteLine($"Count : {result1.Count}");
Console.WriteLine($"Output: [{string.Join(", ", result1)}]");
Console.WriteLine();

// Manual trace for Example 1:
//   Position 0 tries digit 1:
//     Position 1 tries digit 1 → SKIP (same as lastDigit=1)
//     Position 1 tries digit 2 → append → "12" ✓ add to results
//     Position 1 tries digit 3 → append → "13" ✓ add to results
//   Position 0 tries digit 2:
//     Position 1 tries digit 1 → append → "21" ✓
//     Position 1 tries digit 2 → SKIP
//     Position 1 tries digit 3 → append → "23" ✓
//   Position 0 tries digit 3:
//     Position 1 tries digit 1 → append → "31" ✓
//     Position 1 tries digit 2 → append → "32" ✓
//     Position 1 tries digit 3 → SKIP
//   Final: ["12","13","21","23","31","32"] ✓

// ---------------------------------------------------------------
// Example 2: n = 1, k = 4
// Expected output: ["1", "2", "3", "4"]
// ---------------------------------------------------------------
Console.WriteLine("=== Example 2: n=1, k=4 ===");
List<string> result2 = solver.GenerateCombinations(n: 1, k: 4);
Console.WriteLine($"Count : {result2.Count}");
Console.WriteLine($"Output: [{string.Join(", ", result2)}]");
Console.WriteLine();

// Manual trace for Example 2:
//   n=1, so the base case fires as soon as one digit is appended.
//   lastDigit starts at 0, so no digit is ever skipped.
//   digit=1 → "1" ✓, digit=2 → "2" ✓, digit=3 → "3" ✓, digit=4 → "4" ✓
//   Final: ["1","2","3","4"] ✓

// ---------------------------------------------------------------
// Extra test: n = 3, k = 2
// Valid combos: digits 1 and 2, length 3, no adjacent repeats.
// Expected: ["121", "212"]
// ---------------------------------------------------------------
Console.WriteLine("=== Extra Test: n=3, k=2 ===");
List<string> result3 = solver.GenerateCombinations(n: 3, k: 2);
Console.WriteLine($"Count : {result3.Count}");
Console.WriteLine($"Output: [{string.Join(", ", result3)}]");
Console.WriteLine();

// ---------------------------------------------------------------
// Extra test: n = 2, k = 1
// Only one digit available (1), so no valid 2-digit combo exists
// (the only option "11" is forbidden). Expected: []
// ---------------------------------------------------------------
Console.WriteLine("=== Edge Case: n=2, k=1 ===");
List<string> result4 = solver.GenerateCombinations(n: 2, k: 1);
Console.WriteLine($"Count : {result4.Count}");
Console.WriteLine($"Output: [{string.Join(", ", result4)}]");
Console.WriteLine();

// ---------------------------------------------------------------
// Extra test: n = 3, k = 3
// Expected count: 3 * 2 * 2 = 12 combinations
// ---------------------------------------------------------------
Console.WriteLine("=== Extra Test: n=3, k=3 ===");
List<string> result5 = solver.GenerateCombinations(n: 3, k: 3);
Console.WriteLine($"Count : {result5.Count}  (expected 12)");
Console.WriteLine($"Output: [{string.Join(", ", result5)}]");