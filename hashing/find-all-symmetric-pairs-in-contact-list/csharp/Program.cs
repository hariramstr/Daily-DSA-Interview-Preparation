/*
 * Title: Find All Symmetric Pairs in a Contact List
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * You are given a list of n contact pairs where each pair [caller, receiver]
 * represents a phone call from caller to receiver. A symmetric pair exists when
 * both [A, B] and [B, A] appear in the list. Your task is to return all unique
 * symmetric pairs found in the contact list.
 *
 * A pair [A, B] and its symmetric counterpart [B, A] should only be reported
 * ONCE in the output (report the version where the smaller value comes first).
 * If the same pair appears multiple times in the input, it should still only
 * generate one symmetric result.
 *
 * Constraints:
 *   1 <= n <= 10^5
 *   1 <= caller, receiver <= 10^6
 *   caller != receiver
 *   The input list may contain duplicate pairs.
 *
 * Example 1:
 *   Input:  contacts = [[1,2],[3,4],[2,1],[5,6],[4,3],[7,8]]
 *   Output: [[1,2],[3,4]]
 *
 * Example 2:
 *   Input:  contacts = [[10,20],[20,10],[10,20],[30,40]]
 *   Output: [[10,20]]
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class containing the core algorithm
// ─────────────────────────────────────────────────────────────────────────────
class Solution
{
    /// <summary>
    /// Finds all symmetric pairs in a contact list using a HashSet for O(1) lookups.
    ///
    /// Time Complexity:  O(n)  — we iterate through the list once; each HashSet
    ///                           operation (Add / Contains) is O(1) on average.
    /// Space Complexity: O(n)  — in the worst case we store every pair in the
    ///                           'seen' HashSet before finding any symmetric match.
    /// </summary>
    public List<int[]> FindSymmetricPairs(int[][] contacts)
    {
        // ── Step 1: Create a HashSet to remember every unique pair we have seen ──
        // We use a HashSet<(int, int)> (value tuples) because:
        //   • Tuples implement structural equality by default, so (1,2) == (1,2).
        //   • HashSet gives O(1) average-case lookup, which keeps the whole
        //     algorithm linear in the number of contacts.
        // We also need a second HashSet to track symmetric pairs we have already
        // reported, so that duplicate input pairs don't produce duplicate output.
        HashSet<(int, int)> seen = new HashSet<(int, int)>();

        // This set stores the "canonical" form of every symmetric pair we have
        // already added to the result list.  The canonical form always puts the
        // smaller number first, e.g. (1,2) rather than (2,1).
        // This prevents reporting the same symmetric pair more than once even if
        // the input contains [1,2] twice and [2,1] twice.
        HashSet<(int, int)> reported = new HashSet<(int, int)>();

        // The list we will return to the caller.
        List<int[]> result = new List<int[]>();

        // ── Step 2: Iterate over every contact pair exactly once ──────────────
        foreach (int[] pair in contacts)
        {
            // Extract caller (A) and receiver (B) for readability.
            int A = pair[0];
            int B = pair[1];

            // ── Step 3: Build the "reverse" tuple we are looking for ──────────
            // If the current pair is (A, B), its symmetric counterpart is (B, A).
            // We check whether (B, A) was already stored in 'seen'.
            (int, int) reverse = (B, A);

            // ── Step 4: Check if the reverse pair has been seen before ─────────
            // If 'seen' already contains (B, A), then we have found a symmetric
            // pair: both (A,B) and (B,A) exist in the input.
            if (seen.Contains(reverse))
            {
                // ── Step 5: Build the canonical (smaller-first) representation ─
                // We always report the pair with the smaller value first so that
                // (1,2) and (2,1) are both reported as [1,2].
                // Math.Min / Math.Max give us the correct ordering in O(1).
                int lo = Math.Min(A, B);
                int hi = Math.Max(A, B);
                (int, int) canonical = (lo, hi);

                // ── Step 6: Guard against duplicate reporting ─────────────────
                // Even if the input has [1,2] three times and [2,1] twice, we
                // should output [1,2] only once.  We use the 'reported' set to
                // skip pairs we have already added to the result.
                if (!reported.Contains(canonical))
                {
                    reported.Add(canonical);          // mark as reported
                    result.Add(new int[] { lo, hi }); // add to output
                }
            }

            // ── Step 7: Record the current pair so future pairs can find it ───
            // We add (A, B) to 'seen' AFTER the lookup above.
            // Why after?  Because we only want to match pairs that were seen
            // BEFORE the current one; adding it first could cause a pair to
            // "match itself" if the same pair appeared twice (but since A != B
            // the reverse is always different, so order doesn't matter for
            // correctness here — it is still good practice to be explicit).
            // We use Add (not TryAdd) because HashSet.Add simply returns false
            // for duplicates without throwing, which is exactly what we want:
            // duplicates in the input are silently ignored in 'seen'.
            seen.Add((A, B));
        }

        // ── Step 8: Return the collected symmetric pairs ──────────────────────
        return result;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / driver code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

// Helper: pretty-print a list of int[] pairs
static void PrintResult(string label, List<int[]> pairs)
{
    Console.Write($"{label}: [");
    for (int i = 0; i < pairs.Count; i++)
    {
        Console.Write($"[{pairs[i][0]}, {pairs[i][1]}]");
        if (i < pairs.Count - 1) Console.Write(", ");
    }
    Console.WriteLine("]");
}

Solution sol = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// Input:  [[1,2],[3,4],[2,1],[5,6],[4,3],[7,8]]
// Expected output: [[1,2],[3,4]]  (order may vary)
int[][] contacts1 = new int[][]
{
    new int[] { 1, 2 },
    new int[] { 3, 4 },
    new int[] { 2, 1 },
    new int[] { 5, 6 },
    new int[] { 4, 3 },
    new int[] { 7, 8 }
};

List<int[]> result1 = sol.FindSymmetricPairs(contacts1);
PrintResult("Example 1 Output", result1);
// Trace:
//   Process (1,2): reverse=(2,1) not in seen → add (1,2) to seen
//   Process (3,4): reverse=(4,3) not in seen → add (3,4) to seen
//   Process (2,1): reverse=(1,2) IS in seen  → canonical=(1,2) → report [1,2]
//   Process (5,6): reverse=(6,5) not in seen → add (5,6) to seen
//   Process (4,3): reverse=(3,4) IS in seen  → canonical=(3,4) → report [3,4]
//   Process (7,8): reverse=(8,7) not in seen → add (7,8) to seen
//   Result: [[1,2],[3,4]] ✓

Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// Input:  [[10,20],[20,10],[10,20],[30,40]]
// Expected output: [[10,20]]
int[][] contacts2 = new int[][]
{
    new int[] { 10, 20 },
    new int[] { 20, 10 },
    new int[] { 10, 20 },
    new int[] { 30, 40 }
};

List<int[]> result2 = sol.FindSymmetricPairs(contacts2);
PrintResult("Example 2 Output", result2);
// Trace:
//   Process (10,20): reverse=(20,10) not in seen → add (10,20) to seen
//   Process (20,10): reverse=(10,20) IS in seen  → canonical=(10,20) → report [10,20]
//   Process (10,20): reverse=(20,10) IS in seen  → canonical=(10,20) already reported → skip
//   Process (30,40): reverse=(40,30) not in seen → add (30,40) to seen
//   Result: [[10,20]] ✓

Console.WriteLine();

// ── Additional edge-case: no symmetric pairs ──────────────────────────────────
int[][] contacts3 = new int[][]
{
    new int[] { 1, 2 },
    new int[] { 3, 4 },
    new int[] { 5, 6 }
};

List<int[]> result3 = sol.FindSymmetricPairs(contacts3);
PrintResult("No Symmetric Pairs Output", result3);
// Expected: []

Console.WriteLine();

// ── Additional edge-case: all pairs are symmetric ─────────────────────────────
int[][] contacts4 = new int[][]
{
    new int[] { 100, 200 },
    new int[] { 200, 100 },
    new int[] { 300, 400 },
    new int[] { 400, 300 }
};

List<int[]> result4 = sol.FindSymmetricPairs(contacts4);
PrintResult("All Symmetric Output", result4);
// Expected: [[100,200],[300,400]]