/*
 * Title: Generate All Valid Bracket Colorings
 * 
 * Problem Description:
 * You are given a positive integer `n` representing the number of pairs of brackets.
 * Your task is to generate all valid combinations of `n` pairs of brackets, where each
 * bracket (both opening and closing) is also assigned one of two colors: Red or Blue.
 *
 * A bracket sequence is considered valid if:
 * 1. Every opening bracket has a corresponding closing bracket.
 * 2. The brackets are properly nested.
 *
 * For each valid bracket sequence, every individual bracket character (both `(` and `)`)
 * must be assigned a color. Two colorings of the same bracket sequence are considered
 * distinct if any bracket differs in color.
 *
 * Return a list of all distinct results as strings. Each bracket should be represented as:
 *   R(  -> Red opening bracket
 *   B(  -> Blue opening bracket
 *   R)  -> Red closing bracket
 *   B)  -> Blue closing bracket
 *
 * Constraints: 1 <= n <= 4
 *
 * Example 1:
 *   Input:  n = 1
 *   Output: ["R(R)", "R(B)", "B(R)", "B(B)"]
 *
 * Example 2:
 *   Input:  n = 2
 *   Output: 32 strings total (2 valid sequences × 2^4 colorings each)
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class containing the main algorithm
// ─────────────────────────────────────────────────────────────────────────────
class Solution
{
    // -------------------------------------------------------------------------
    // Method: GenerateColoredBrackets
    //
    // Time Complexity:  O(C(n) * 4^n)  where C(n) is the n-th Catalan number
    //                   (number of valid bracket sequences) and 4^n accounts for
    //                   all possible 2-color assignments to 2n brackets.
    //                   For n=4: C(4)=14, 4^4=256 → 14*256 = 3584 results.
    //
    // Space Complexity: O(n) for the recursion call stack depth (up to 2n deep),
    //                   plus O(C(n) * 4^n * n) to store all result strings.
    // -------------------------------------------------------------------------
    public List<string> GenerateColoredBrackets(int n)
    {
        // This list will accumulate every valid colored bracket string we find.
        // We pass it by reference through the recursion so all recursive calls
        // can add to the same collection.
        List<string> results = new List<string>();

        // We use a List<string> as a mutable "current path" (like a stack of
        // tokens). Each token is one colored bracket: "R(", "B(", "R)", or "B)".
        // Using a list lets us easily append a token and then remove it
        // (backtrack) after exploring that branch.
        List<string> current = new List<string>();

        // Start the recursive backtracking:
        //   openCount  = how many '(' we have placed so far (starts at 0)
        //   closeCount = how many ')' we have placed so far (starts at 0)
        //   n          = total pairs needed
        Backtrack(current, 0, 0, n, results);

        return results;
    }

    // -------------------------------------------------------------------------
    // Recursive helper: Backtrack
    //
    // Parameters:
    //   current    - the bracket tokens chosen so far in this path
    //   openCount  - number of opening brackets placed so far
    //   closeCount - number of closing brackets placed so far
    //   n          - total number of bracket pairs required
    //   results    - shared list where completed valid strings are stored
    // -------------------------------------------------------------------------
    private void Backtrack(
        List<string> current,
        int openCount,
        int closeCount,
        int n,
        List<string> results)
    {
        // ── BASE CASE ──────────────────────────────────────────────────────────
        // We have placed exactly n opening brackets AND n closing brackets.
        // That means we have a complete, valid bracket sequence of length 2n.
        // Convert the token list to a single string and record it.
        if (openCount == n && closeCount == n)
        {
            // string.Concat joins all tokens without any separator,
            // e.g. ["R(", "B(", "R)", "B)"] → "R(B(R)B)"
            results.Add(string.Concat(current));
            return; // Stop recursing; this branch is done.
        }

        // ── RECURSIVE CASE 1: Place an opening bracket ────────────────────────
        // We can place an opening bracket as long as we haven't used all n yet.
        // An opening bracket is always "safe" to add when openCount < n because
        // it can always be matched by future closing brackets.
        if (openCount < n)
        {
            // ── Color choice A: Red opening bracket ───────────────────────────
            // Add the token "R(" to the current path.
            current.Add("R(");

            // Recurse with one more open bracket placed.
            // openCount increases by 1; closeCount stays the same.
            Backtrack(current, openCount + 1, closeCount, n, results);

            // BACKTRACK: remove the token we just added so we can try the
            // next color. This is the heart of backtracking — undo the choice.
            current.RemoveAt(current.Count - 1);

            // ── Color choice B: Blue opening bracket ──────────────────────────
            current.Add("B(");
            Backtrack(current, openCount + 1, closeCount, n, results);
            current.RemoveAt(current.Count - 1);
        }

        // ── RECURSIVE CASE 2: Place a closing bracket ─────────────────────────
        // We can place a closing bracket only when closeCount < openCount.
        // This condition enforces proper nesting: we must never close a bracket
        // that hasn't been opened yet. If closeCount == openCount, adding a ')'
        // would create an unmatched closing bracket (invalid sequence).
        if (closeCount < openCount)
        {
            // ── Color choice A: Red closing bracket ───────────────────────────
            current.Add("R)");
            Backtrack(current, openCount, closeCount + 1, n, results);
            current.RemoveAt(current.Count - 1);

            // ── Color choice B: Blue closing bracket ──────────────────────────
            current.Add("B)");
            Backtrack(current, openCount, closeCount + 1, n, results);
            current.RemoveAt(current.Count - 1);
        }

        // When neither condition above applies (openCount == n AND closeCount == n),
        // we would have already returned in the base case, so we never reach here
        // in an invalid state.
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Driver Code  (top-level statements — no explicit Main needed in .NET 6+)
// ─────────────────────────────────────────────────────────────────────────────

Solution sol = new Solution();

// ── Example 1: n = 1 ──────────────────────────────────────────────────────────
Console.WriteLine("=== Example 1: n = 1 ===");
Console.WriteLine("Expected: 4 results (R(R), R(B), B(R), B(B))");

List<string> result1 = sol.GenerateColoredBrackets(1);
Console.WriteLine($"Got {result1.Count} results:");
foreach (string s in result1)
{
    Console.WriteLine("  " + s);
}

// Verify correctness for n=1
// The only valid bracket sequence is "()" and each of the 2 brackets can be
// R or B independently → 2^2 = 4 colorings. ✓
Console.WriteLine();

// ── Example 2: n = 2 ──────────────────────────────────────────────────────────
Console.WriteLine("=== Example 2: n = 2 ===");
Console.WriteLine("Expected: 32 results (2 valid sequences × 2^4 = 16 colorings each)");

List<string> result2 = sol.GenerateColoredBrackets(2);
Console.WriteLine($"Got {result2.Count} results:");
foreach (string s in result2)
{
    Console.WriteLine("  " + s);
}
Console.WriteLine();

// ── Example 3: n = 3 ──────────────────────────────────────────────────────────
Console.WriteLine("=== Example 3: n = 3 ===");
// Catalan(3) = 5 valid sequences, each with 2^6 = 64 colorings → 5 × 64 = 320
Console.WriteLine("Expected: 320 results (5 valid sequences × 2^6 = 64 colorings each)");

List<string> result3 = sol.GenerateColoredBrackets(3);
Console.WriteLine($"Got {result3.Count} results");
// Print just the first 10 to keep output manageable
Console.WriteLine("First 10 results:");
for (int i = 0; i < Math.Min(10, result3.Count); i++)
{
    Console.WriteLine("  " + result3[i]);
}
Console.WriteLine();

// ── Example 4: n = 4 ──────────────────────────────────────────────────────────
Console.WriteLine("=== Example 4: n = 4 ===");
// Catalan(4) = 14 valid sequences, each with 2^8 = 256 colorings → 14 × 256 = 3584
Console.WriteLine("Expected: 3584 results (14 valid sequences × 2^8 = 256 colorings each)");

List<string> result4 = sol.GenerateColoredBrackets(4);
Console.WriteLine($"Got {result4.Count} results");
Console.WriteLine();

// ── Summary verification ──────────────────────────────────────────────────────
Console.WriteLine("=== Verification Summary ===");
Console.WriteLine($"n=1 → {result1.Count} (expected  4) : {(result1.Count ==    4 ? "PASS" : "FAIL")}");
Console.WriteLine($"n=2 → {result2.Count} (expected 32) : {(result2.Count ==   32 ? "PASS" : "FAIL")}");
Console.WriteLine($"n=3 → {result3.Count} (expected 320) : {(result3.Count ==  320 ? "PASS" : "FAIL")}");
Console.WriteLine($"n=4 → {result4.Count} (expected 3584): {(result4.Count == 3584 ? "PASS" : "FAIL")}");