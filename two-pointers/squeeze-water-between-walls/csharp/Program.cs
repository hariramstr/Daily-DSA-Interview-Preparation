/*
 * Title: Squeeze Water Between Walls
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * You are given an integer array `heights` representing the heights of vertical walls
 * positioned at consecutive unit intervals along a horizontal axis. Between any two walls,
 * water can be trapped if the surrounding walls are tall enough. However, there is a twist:
 * each wall has a durability value given in a second integer array `durability`. A wall can
 * only hold water if its durability is strictly greater than the amount of water units
 * pressing against it (i.e., the water level at that wall's position).
 *
 * Your task is to find the maximum amount of water that can be trapped between exactly two
 * walls (not necessarily adjacent), such that both chosen walls satisfy the durability
 * constraint. The water trapped between wall i and wall j (where i < j) is calculated as:
 *
 *   water = min(heights[i], heights[j]) * (j - i)
 *
 * A wall at index k satisfies the durability constraint if durability[k] > min(heights[i], heights[j]).
 *
 * Return the maximum water that can be trapped. If no valid pair exists, return 0.
 *
 * Constraints:
 *   2 <= heights.length <= 10^5
 *   heights.length == durability.length
 *   1 <= heights[i] <= 10^4
 *   1 <= durability[i] <= 10^4
 *
 * Example 1:
 *   Input:  heights    = [1, 8, 6, 2, 5, 4, 8, 3, 7]
 *           durability = [10, 9, 7, 5, 6, 5, 9, 4, 8]
 *   Output: 49
 *   Explanation: Walls at index 1 (h=8, d=9) and index 8 (h=7, d=8).
 *                water = min(8,7)*(8-1) = 7*7 = 49. Both 9>7 and 8>7. Valid.
 *
 * Example 2:
 *   Input:  heights    = [3, 1, 2, 4]
 *           durability = [3, 5, 3, 4]
 *   Output: 4
 *   Explanation: Best valid pair is index 0 and index 2: min(3,2)*2=4,
 *                durability[0]=3>2 and durability[2]=3>2. Valid.
 */

using System;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class containing the core algorithm
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /// <summary>
    /// Finds the maximum water that can be trapped between two walls,
    /// respecting the durability constraint on both chosen walls.
    ///
    /// Time Complexity : O(n)  — single pass with two pointers
    /// Space Complexity: O(1)  — only a handful of scalar variables
    /// </summary>
    public int MaxWater(int[] heights, int[] durability)
    {
        // ── Step 1: Initialise two pointers ──────────────────────────────────
        // We start one pointer at the very left end of the array and one at the
        // very right end.  The classic "container with most water" insight is:
        //   • The water level for a pair (i, j) is min(heights[i], heights[j]).
        //   • Moving the pointer that points to the SHORTER wall inward is the
        //     only move that can possibly increase the water level (moving the
        //     taller one can only keep or decrease the level while also shrinking
        //     the width — a guaranteed loss).
        // We extend this greedy strategy here by also checking the durability
        // constraint before recording a candidate answer.
        int left  = 0;
        int right = heights.Length - 1;

        // This will accumulate the best (maximum) valid water found so far.
        int maxWater = 0;

        // ── Step 2: Two-pointer loop ──────────────────────────────────────────
        // Keep iterating as long as the two pointers have not crossed.
        // When left == right we have a single wall — no pair is possible.
        while (left < right)
        {
            // ── Step 2a: Compute the water level for the current pair ─────────
            // The water level is capped by the shorter of the two walls.
            // A taller wall cannot raise the water above the shorter one because
            // water would simply spill over the shorter side.
            int waterLevel = Math.Min(heights[left], heights[right]);

            // ── Step 2b: Compute the raw water volume ─────────────────────────
            // Width of the container is (right - left) unit intervals.
            // Volume = water level × width.
            int width  = right - left;
            int volume = waterLevel * width;

            // ── Step 2c: Check the durability constraint on BOTH walls ────────
            // A wall is "durable enough" only when its durability value is
            // STRICTLY GREATER than the water level pressing against it.
            // If either wall fails this check the pair is invalid and we skip it.
            bool leftDurable  = durability[left]  > waterLevel;
            bool rightDurable = durability[right] > waterLevel;

            if (leftDurable && rightDurable)
            {
                // Both walls can withstand the water pressure — this is a valid
                // pair.  Update the running maximum if this volume is larger.
                maxWater = Math.Max(maxWater, volume);
            }

            // ── Step 2d: Move the pointer that points to the shorter wall ─────
            // Why?  Because the water level is limited by the shorter wall.
            // Moving the taller-wall pointer inward would only shrink the width
            // without any chance of raising the water level — strictly worse.
            // Moving the shorter-wall pointer inward gives us a chance to find
            // a taller wall that raises the water level enough to compensate for
            // the reduced width.
            //
            // Special case — equal heights: it does not matter which pointer we
            // move; moving either one is safe.  We move the left pointer here.
            if (heights[left] <= heights[right])
            {
                left++;
            }
            else
            {
                right--;
            }
        }

        // ── Step 3: Return the best valid volume found ────────────────────────
        // If no valid pair was ever found, maxWater remains 0 as required.
        return maxWater;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / verification code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

var solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// Expected output: 49
// Pair (index 1, index 8): waterLevel = min(8,7) = 7, width = 7, volume = 49
// durability[1]=9 > 7  ✓   durability[8]=8 > 7  ✓
int[] heights1    = [1, 8, 6, 2, 5, 4, 8, 3, 7];
int[] durability1 = [10, 9, 7, 5, 6, 5, 9, 4, 8];
int result1 = solution.MaxWater(heights1, durability1);
Console.WriteLine($"Example 1 — Expected: 49  |  Got: {result1}");

// ── Example 2 ────────────────────────────────────────────────────────────────
// Expected output: 4
// Let's trace the two-pointer walk manually:
//
//   left=0 (h=3,d=3), right=3 (h=4,d=4)
//     waterLevel = min(3,4) = 3, volume = 3*3 = 9
//     durability[0]=3 > 3? NO  → invalid, skip
//     heights[0]=3 <= heights[3]=4 → move left to 1
//
//   left=1 (h=1,d=5), right=3 (h=4,d=4)
//     waterLevel = min(1,4) = 1, volume = 1*2 = 2
//     durability[1]=5 > 1? YES  durability[3]=4 > 1? YES → valid, maxWater=2
//     heights[1]=1 <= heights[3]=4 → move left to 2
//
//   left=2 (h=2,d=3), right=3 (h=4,d=4)
//     waterLevel = min(2,4) = 2, volume = 2*1 = 2
//     durability[2]=3 > 2? YES  durability[3]=4 > 2? YES → valid, maxWater=2
//     heights[2]=2 <= heights[3]=4 → move left to 3
//
//   left=3 == right=3 → loop ends
//
// Hmm — the two-pointer pass gives 2, but the expected answer is 4 (pair 0,2).
// The pair (0,2) has waterLevel=2, width=2, volume=4.
// The two-pointer approach MISSES this pair because when left=0 and right=3
// the durability check fails and we advance left past index 0 without ever
// pairing index 0 with index 2.
//
// This means the pure two-pointer greedy is INSUFFICIENT for this problem
// because the durability constraint can invalidate a pair that would otherwise
// be chosen, and we might need to try other combinations.
//
// ── Revised approach: O(n²) brute-force with early-exit optimisation ─────────
// Given n ≤ 10^5 a naive O(n²) is too slow.  We need a smarter strategy.
//
// Key insight for a correct O(n log n) or O(n) solution:
//   For every pair (i,j) the water level L = min(h[i], h[j]).
//   The durability constraint requires d[i] > L AND d[j] > L.
//   Equivalently, the "effective height" of wall k for a given water level L is
//   h[k] if d[k] > L, otherwise 0 (the wall is unusable at that level).
//
// We iterate over all possible water levels L from high to low (1..10^4).
// For each level L we consider only walls whose height >= L AND durability > L.
// Among those walls we want the pair that maximises (j - i), i.e. the leftmost
// and rightmost eligible wall.  Volume = L * (rightmost_index - leftmost_index).
// We take the maximum over all L.
//
// This is O(max_height * n) in the worst case which is still 10^9 — too slow.
//
// Better: sort candidate walls by index; for each L the eligible set changes
// monotonically as L decreases.  We can use a sweep.
//
// ── Correct O(n log n) approach ──────────────────────────────────────────────
// 1. Collect all distinct water levels to try: the set of values min(h[i],h[j])
//    that matter is exactly the set of distinct heights present (since the water
//    level equals the smaller of the two chosen heights).
// 2. For each candidate level L (iterate heights in descending order):
//    a. Add all walls with height >= L to an "active" set (tracking min/max index).
//    b. Among active walls, further filter to those with durability > L.
//       Track the leftmost and rightmost index among durable-active walls.
//    c. Candidate volume = L * (rightmost - leftmost).
// 3. Return the maximum candidate volume.
//
// Step 2b requires knowing the leftmost/rightmost index among walls that are
// BOTH height>=L AND durability>L.  As L decreases, more walls become
// height-eligible, but the durability threshold also drops, so more walls
// become durability-eligible too.  We can maintain a sorted structure or
// simply scan — but scanning is O(n) per level.
//
// Practical O(n * H) where H = number of distinct heights ≤ n → O(n²) worst.
//
// ── Truly efficient approach ─────────────────────────────────────────────────
// Sort walls by height descending.  Use a pointer to add walls as the water
// level L decreases.  For each L we need the min and max INDEX among walls
// where durability > L.  Since L is decreasing, durability > L becomes easier
// to satisfy over time.  We can maintain a sorted multiset of indices for
// durable walls and query min/max in O(log n).
//
// Total: O(n log n).  Let's implement this.

// ─────────────────────────────────────────────────────────────────────────────
// Correct Solution class
// ─────────────────────────────────────────────────────────────────────────────
public class CorrectSolution
{
    /// <summary>
    /// Finds the maximum valid trapped water using a sweep over water levels.
    ///
    /// Algorithm outline
    /// -----------------
    /// We iterate over every possible water level L from the tallest wall down
    /// to 1.  For a given L, a wall at index k is "eligible" if:
    ///   (a) heights[k] >= L   — the wall is at least as tall as the water level
    ///   (b) durability[k] > L — the wall can withstand the water pressure
    ///
    /// The volume for a pair (i, j) at level L is L * (j - i), which is
    /// maximised by choosing the leftmost and rightmost eligible indices.
    ///
    /// To avoid re-scanning all walls for every L we:
    ///   1. Sort walls by height descending so we can add them with a pointer
    ///      as L decreases (condition a becomes satisfied incrementally).
    ///   2. Among added walls, track which ones satisfy condition b using a
    ///      SortedSet of indices so we can query min/max in O(log n).
    ///      As L decreases, previously ineligible walls (d[k] <= old L) may
    ///      become eligible (d[k] > new L), so we also maintain a pending
    ///      structure sorted by durability.
    ///
    /// Time Complexity : O(n log n)
    /// Space Complexity: O(n)
    /// </summary>
    public int MaxWater(int[] heights, int[] durability)
    {
        int n = heights.Length;

        // ── Step 1: Build wall descriptors and sort by height descending ──────
        // Each wall is represented as (height, durability, index).
        // Sorting by height descending lets us add walls to the "active" pool
        // as the water level L sweeps downward.
        var walls = new (int h, int d, int idx)[n];
        for (int i = 0; i < n; i++)
            walls[i] = (heights[i], durability[i], i);

        // Sort descending by height; ties broken arbitrarily.
        Array.Sort(walls, (a, b) => b.h.CompareTo(a.h));

        // ── Step 2: Collect all distinct water levels to evaluate ─────────────
        // The water level for any pair equals min(h[i], h[j]), which is always
        // one of the heights present in the array.  So we only need to check
        // each distinct height value as a candidate water level.
        // We iterate through the sorted walls; each time the height changes we
        // have a new candidate level.

        // ── Step 3: Data structures for the sweep ────────────────────────────
        // "byDurability": walls that are height-eligible (h >= L) but whose