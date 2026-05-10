```csharp
/*
 * Title: Balancing Bracket Distances
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * You are given a string `s` consisting only of characters '(' and ')'.
 * The string is guaranteed to be a valid bracket sequence.
 *
 * For each matched pair of brackets (s[i], s[j]) where s[i]='(' and s[j]=')',
 * define the distance of that pair as j - i.
 * Find the minimum possible sum of distances across all matched pairs after
 * you are allowed to swap any two characters in the string at most once.
 *
 * After the swap, the resulting string must still be a valid bracket sequence.
 * If no beneficial swap exists, return the original sum of distances.
 *
 * Constraints:
 * - 2 <= s.length <= 10^5
 * - s.length is even
 * - s consists only of '(' and ')'
 * - s is a valid bracket sequence
 *
 * Examples:
 * Input: s = "(())"  => Output: 4
 * Input: s = "()(())" => Output: 6
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// KEY INSIGHT (read this before diving into the code):
//
// For a valid bracket sequence, the sum of distances equals:
//   sum of distances = sum over all positions i of: (contribution of position i)
//
// There is a beautiful observation:
//   For every character at position i:
//     - If s[i] == '(', it contributes  +1 to every pair that "contains" it
//       (i.e., pairs whose open index <= i and close index >= i).
//     - Actually, the simplest way to see it:
//
//   sum of (j - i) for all matched pairs (i,j)
//   = sum of j  -  sum of i   (over all matched pairs)
//
// Even simpler: scan left to right, keep a running "depth" counter.
// Each '(' increments depth; each ')' decrements depth.
// The sum of distances equals the sum of depth values at every position
// (where depth is measured BEFORE processing the character at that position,
//  using the convention that depth counts open-but-unmatched brackets).
//
// Actually the cleanest known identity is:
//   sum of distances = sum_{i=0}^{n-1} balance(i)
// where balance(i) = (number of '(' in s[0..i]) - (number of ')' in s[0..i])
// and we sum the balance AFTER processing each character.
//
// Wait — let me re-derive carefully so the comments are correct.
//
// Let depth[i] = running open-bracket count after processing index i.
// depth starts at 0.
// For '(' at position i: depth increases by 1.
// For ')' at position i: depth decreases by 1.
//
// Claim: sum of all pair distances = sum of depth[i] for i = 0..n-1
//        where depth[i] is computed BEFORE processing position i
//        (i.e., the depth when we ARRIVE at position i).
//
// Proof sketch: each matched pair (open=a, close=b) contributes (b-a) to the
// distance sum. Equivalently, it contributes +1 to every position k with a<=k<b.
// The depth-before-processing at position k counts exactly how many open pairs
// are currently "active" (opened but not yet closed), which equals the number
// of matched pairs (a,b) with a <= k < b. Summing over all k gives the total
// distance sum. ✓
//
// So: originalSum = sum of depth[i] for i in [0, n-1], depth measured before
//     processing s[i].
//
// Now, what happens when we swap positions p and q (p < q)?
// We only need to consider swaps where s[p] != s[q] (otherwise nothing changes).
// Valid swaps that keep the sequence valid:
//   Case A: s[p]=')', s[q]='(' — swapping ')' at p with '(' at q.
//           This turns a ')' earlier and a '(' later, which generally
//           INCREASES distances (bad). We skip these.
//   Case B: s[p]='(', s[q]=')' — swapping '(' at p with ')' at q.
//           This moves an open bracket to the right and a close bracket to
//           the left, which can DECREASE distances.
//
// For Case B (s[p]='(', s[q]=')'), after the swap:
//   - Positions strictly between p and q: their depth changes.
//     Specifically, depth at each position k (p < k < q) decreases by 2
//     (because we removed a '(' before k and added a ')' before k... wait,
//      we need to think carefully).
//
// Let me think about the depth change more carefully.
// Original: s[p]='(', s[q]=')'
// After swap: s[p]=')', s[q]='('
//
// For positions k in [0, p-1]: no change (neither p nor q has been processed).
// For position k = p: depth-before = same (p not yet processed).
//   But now s[p]=')' instead of '(', so depth-after-p decreases by 2.
// For positions k in (p, q): depth-before-k decreases by 2
//   (because the '(' at p is gone, replaced by ')').
// For position k = q: depth-before-q decreases by 2.
//   Now s[q]='(' instead of ')', so depth-after-q increases by 2, restoring.
// For positions k > q: no net change.
//
// Change in sum = sum of (new_depth[k] - old_depth[k]) for all k
//   = 0 (for k <= p)
//   + (-2) * (number of positions k with p < k <= q)   [positions p+1 .. q]
//   + 0 (for k > q)
//
// Wait, let me redo. depth[k] = depth BEFORE processing position k.
//
// depth[0] = 0 always (nothing processed yet).
// depth[k] = depth[k-1] + (s[k-1]=='(' ? 1 : -1)
//
// After swapping p and q (s[p]='(' -> ')', s[q]=')' -> '('):
// For k <= p: depth[k] unchanged (positions 0..p-1 not affected).
// For k = p+1: depth[p+1] = depth[p] + (new s[p] == ')' ? -1 : +1)
//                          = depth[p] + (-1)  [was +1, now -1, delta = -2]
// For p+1 < k <= q: each depth[k] decreases by 2.
// For k = q+1: depth[q+1] = depth[q] + (new s[q] == '(' ? +1 : -1)
//                          = (depth[q]-2) + (+1)  [was -1, now +1, delta = +2]
//                          = depth[q+1]  (net change = 0)
// For k > q+1: unchanged.
//
// So the change in sum of depths =
//   sum_{k=p+1}^{q} (-2) = -2 * (q - p)
//
// But wait! We also need to check that the swap results in a valid bracket
// sequence. After swapping '(' at p with ')' at q, the new string has ')' at p
// and '(' at q. For validity, we need the prefix sums to never go negative.
//
// The depth at position p+1 (after the swap) = depth[p] - 1.
// For this to be >= 0, we need depth[p] >= 1.
// Also, for all positions k in (p, q], the depth decreases by 2, so we need
// depth[k] - 2 >= 0 for all k in [p+1, q], i.e., min(depth[p+1..q]) >= 2.
//
// Actually the minimum depth in [p+1, q] after the swap is min(depth[p+1..q]) - 2.
// For validity: min(depth[p+1..q]) - 2 >= 0, i.e., min(depth[p+1..q]) >= 2.
//
// Also at position p+1: depth[p+1] (original) = depth[p] + 1 (since s[p]='(').
// After swap, depth at p+1 = depth[p] - 1. For >= 0: depth[p] >= 1.
// But depth[p+1] = depth[p]+1 >= 2 means depth[p] >= 1. So the condition
// min(depth[p+1..q]) >= 2 covers this (since depth[p+1] is in that range).
//
// Summary:
//   - Only consider swaps where s[p]='(' and s[q]=')' (p < q).
//   - The reduction in sum = 2*(q-p).
//   - The swap is valid iff min(depth[k] for k in [p+1, q]) >= 2.
//   - We want to maximize 2*(q-p) subject to validity.
//   - Equivalently, maximize (q-p) subject to min_depth[p+1..q] >= 2.
//
// To maximize q-p, we want p as small as possible and q as large as possible.
// Use a two-pointer / greedy approach:
//   - Try the leftmost '(' as p and rightmost ')' as q.
//   - Check if min depth in (p, q] >= 2.
//   - If yes, that's our best swap.
//   - If no, try next candidates.
//
// But we need to be careful: we want to maximize q-p over all valid (p,q) pairs
// where s[p]='(' and s[q]=')' and min_depth[p+1..q] >= 2.
//
// The greedy insight: to maximize q-p, try the outermost possible pair first.
// The outermost '(' is at the leftmost index where s[i]='(' AND depth[i+1] >= 2
// (so that after the swap the depth at i+1 is still >= 0).
// Actually the condition is min(depth[p+1..q]) >= 2.
//
// Let's think differently with a two-pointer sweep:
//   left pointer l starts at 0, right pointer r starts at n-1.
//   Move l right until s[l]='(' and depth[l+1] >= 2 (meaning there's a nested
//   structure that can absorb the swap).
//   Move r left until s[r]=')' and depth[r] >= 2.
//   If l < r and both conditions met, check min depth in [l+1, r].
//   If min >= 2, we found our best swap with reduction 2*(r-l).
//
// Actually, let me think about this more carefully with a range-minimum approach.
//
// SIMPLER APPROACH:
// Since we want to maximize q - p with min_depth[p+1..q] >= 2:
//   - The best p is the smallest index where s[p]='(' and there exists a valid q.
//   - The best q is the largest index where s[q]=')' and there exists a valid p.
//
// Key observation: if we fix p as the leftmost '(' with depth[p] >= 1 (i.e.,
// depth[p+1] >= 2 after the swap... wait depth[p+1] = depth[p]+1 originally,
// and after swap it's depth[p]-1, so we need depth[p] >= 1, i.e., depth[p+1] >= 2).
//
// And fix q as the rightmost ')' such that depth[q] >= 2.
//
// Then check if min_depth[p+1..q] >= 2. If yes, done.
// If not, we need to find the best valid pair.
//
// For the purposes of this problem (competitive programming style), let's
// implement a clean O(n) or O(n log n) solution:
//
// 1. Compute depth array.
// 2. Compute prefix minimum of depth.
// 3. For each candidate p (where s[p]='(' and depth[p+1] >= 2), find the
//    rightmost q > p where s[q]=')' and depth[q] >= 2 and
//    min_depth[p+1..q] >= 2.
//    The min_depth[p+1..q] >= 2 condition means: the minimum depth in [p+1,q]
//    is >= 2. We can use a sparse table or segment tree for range min queries.
//    But for simplicity, we can iterate.
//
// Actually, let me think about what "min_depth[p+1..q] >= 2" means geometrically.
// The depth array for a valid bracket sequence goes up and down. The minimum
// depth in a range [a,b] can be found with a sparse table in O(1) after O(n log n)
// preprocessing.
//
// For this problem size (n <= 10^5), O(n^2) might be too slow in worst case,
// but let's think about the structure.
//
// FINAL APPROACH:
// - Compute depth array (depth[i] = depth before processing s[i]).
// - Compute originalSum = sum of depth[i].
// - Find the best swap: maximize 2*(q-p) where s[p]='(', s[q]=')', p<q,
//   and min(depth[p+1..q]) >= 2.
// - Use two pointers: start with l=0, r=n-1.
//   - Advance l to the first index where s[l]='(' and depth[l+1] >= 2.
//   - Retreat r to the last index where s[r]=')' and depth[r] >= 2.
//   - If l < r, check if min_depth[l+1..r] >= 2 using a precomputed range min.
//   - If valid, answer = originalSum - 2*(r-l).
//   - Otherwise, try next candidates (advance l or retreat r).
//
// For the range minimum, we'll use a sparse table for O(1) queries.
// ─────────────────────────────────────────────────────────────────────────────

public class Solution
{
    // Time Complexity:  O(n log n) for sparse table construction + O(n) for sweep
    //                   = O(n log n) overall
    // Space Complexity: O(n log n) for sparse table
    public long MinSumOfDistances(string s)
    {
        int n = s.Length;

        // ── Step 1: Compute the depth array ──────────────────────────────────
        // depth[i] = number of unmatched '(' brackets when we ARRIVE at position i
        // (i.e., before processing s[i]).
        // This is the running balance of '(' minus ')' seen so far.
        //
        // Why do we need this?
        // As proven in the header comment, originalSum = sum of depth[i].
        // Also, the validity condition for a swap (p,q) requires
        // min(depth[p+1..q]) >= 2.
        int[] depth = new int[n + 1]; // depth[0]=0, depth[i] = depth after processing s[i-1]
        // We'll use depth[i] to mean depth BEFORE processing s[i], so depth[0]=0.
        // depth[i+1] = depth[i] + (s[i]=='(' ? 1 : -1)
        depth[0] = 0;
        for (int i = 0; i < n; i++)
        {
            depth[i + 1] = depth[i] + (s[i] == '(' ? 1 : -1);
        }
        // Now depth[i] (for i in 0..n) represents depth after processing s[0..i-1].