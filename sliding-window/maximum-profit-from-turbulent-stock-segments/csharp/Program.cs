/*
 * Maximum Profit from Turbulent Stock Segments
 * =============================================
 * 
 * Problem:
 * Given an integer array `prices` and two integers `k` and `threshold`,
 * find the maximum total profit by selecting at most k non-overlapping
 * turbulent subarrays.
 * 
 * A subarray prices[l..r] is TURBULENT if for every consecutive pair,
 * |prices[i+1] - prices[i]| > threshold.
 * 
 * The PROFIT of a turbulent subarray is max(prices[l..r]) - min(prices[l..r]).
 * 
 * We want to pick at most k non-overlapping turbulent subarrays to maximize
 * total profit.
 * 
 * Key Insight:
 * - First, identify all maximal turbulent segments (contiguous runs where
 *   every consecutive pair differs by more than threshold).
 * - Within each maximal turbulent segment, the best subarray to pick is
 *   the entire segment (since extending a turbulent subarray can only
 *   increase or maintain the range max-min).
 * - Then use DP to pick at most k non-overlapping segments for max profit.
 * 
 * Wait — we need to be careful. Within a maximal turbulent segment, we
 * could pick any sub-segment. But since profit = max - min, and a larger
 * turbulent segment contains all values of smaller ones (it's a superset),
 * the profit of the full maximal segment >= any sub-segment's profit.
 * 
 * Also, two non-overlapping turbulent subarrays from the SAME maximal
 * turbulent segment: we need to consider splitting a maximal segment into
 * two parts. This complicates things.
 * 
 * Revised Approach:
 * - Enumerate all maximal turbulent segments.
 * - For each maximal turbulent segment, we can either:
 *   a) Take the whole segment as one selection
 *   b) Split it into two or more non-overlapping sub-segments
 * - This is a complex optimization problem.
 * 
 * Simpler correct approach: DP over the array.
 * dp[i][j] = maximum profit using exactly j selections, considering prices[0..i]
 * 
 * For each position i and count j, we try all possible last turbulent
 * subarray ending at or before i.
 * 
 * Given n=10^5 and k=100, we need an efficient approach.
 */

using System;
using System.Collections.Generic;

/*
 * ALGORITHM OVERVIEW:
 * 
 * Step 1: Find all maximal turbulent segments.
 *         A maximal turbulent segment is a maximal contiguous subarray where
 *         every consecutive pair has |diff| > threshold.
 * 
 * Step 2: For each maximal turbulent segment, compute the best profit for
 *         taking 1, 2, ..., k sub-selections from it.
 *         
 *         Key observation: Within a maximal turbulent segment of length L,
 *         taking the whole segment gives profit = max - min of entire segment.
 *         Taking two non-overlapping sub-segments: we'd split at some point.
 *         But the profit of two sub-segments <= profit of the whole segment
 *         (since max-min of whole >= max-min of any part, and we're adding
 *         two non-negative values... actually this isn't necessarily true).
 *         
 *         Example: [1, 10, 2, 9] threshold=0
 *         Whole segment profit = 10-1 = 9
 *         Split into [1,10] profit=9 and [2,9] profit=7, total=16 > 9
 *         
 *         So splitting CAN be better! We must consider splits.
 * 
 * Step 3: Use DP across segments to pick at most k total selections.
 * 
 * For the within-segment optimization with splits, we use another DP:
 * For a maximal turbulent segment, compute best[j] = max profit from
 * taking exactly j non-overlapping sub-segments from this segment.
 * 
 * Since segments can be large (up to 10^5), and k up to 100, we need
 * an efficient within-segment DP.
 * 
 * Within a turbulent segment, every sub-segment of length >= 2 is also
 * turbulent. The profit of sub-segment [l,r] = max(l..r) - min(l..r).
 * 
 * For within-segment DP with j picks, this is still complex.
 * We'll use a segment-level DP where we consider all possible sub-segments.
 */

public class Solution
{
    // Time Complexity: O(n^2 * k) in worst case, but with maximal segment
    //                 structure it's more efficient in practice.
    //                 For the within-segment DP: O(L^2 * k) per segment
    //                 where L is segment length. Total O(n^2 * k).
    // Space Complexity: O(n * k) for DP tables.
    
    public long MaxProfit(int[] prices, int k, int threshold)
    {
        int n = prices.Length;
        
        // ---------------------------------------------------------------
        // STEP 1: Identify all maximal turbulent segments.
        // A maximal turbulent segment is a contiguous range [l, r] such that:
        //   - For all i in [l, r-1]: |prices[i+1] - prices[i]| > threshold
        //   - It cannot be extended further (boundary conditions)
        // We store segments as (startIndex, endIndex) pairs.
        // ---------------------------------------------------------------
        var segments = new List<(int start, int end)>();
        
        int segStart = 0;
        for (int i = 0; i < n; i++)
        {
            // Check if we can extend the current segment to include edge (i, i+1)
            if (i + 1 < n && Math.Abs((long)prices[i + 1] - prices[i]) > threshold)
            {
                // Edge (i, i+1) is turbulent, continue extending
                continue;
            }
            else
            {
                // Edge (i, i+1) is NOT turbulent (or we're at the end)
                // The current segment ends at i
                if (i > segStart)
                {
                    // Segment has length >= 2, it's a valid turbulent segment
                    segments.Add((segStart, i));
                }
                // Start a new potential segment from i+1
                // (but actually from i, since i+1 might start a new turbulent run)
                segStart = i + 1;
            }
        }
        // Handle the last segment if it extends to the end
        // (already handled by the loop when i+1 >= n)
        
        // ---------------------------------------------------------------
        // STEP 2: For each maximal turbulent segment, precompute the best
        // profit achievable by selecting exactly j non-overlapping 
        // turbulent sub-segments (j = 1, 2, ..., k).
        //
        // We'll use a DP within each segment:
        // innerDP[j] = max profit from j non-overlapping sub-segments
        //              within this maximal turbulent segment.
        //
        // For a segment [l, r], any sub-range [a, b] with l<=a<=b<=r and b-a>=1
        // is also turbulent (since all consecutive pairs in [l,r] are turbulent).
        // Profit of [a,b] = max(prices[a..b]) - min(prices[a..b]).
        //
        // We use DP: 
        // segDP[i][j] = max profit using j selections from prices[l..l+i]
        //               where the last selection ends at exactly l+i.
        // This is O(L^2 * k) per segment.
        // ---------------------------------------------------------------
        
        // segmentBest[s][j] = best profit from segment s using j picks (j=0..k)
        // We'll store these and then do a global DP across segments.
        
        // Actually, let's do a global DP directly.
        // globalDP[j] = max profit using at most j selections from all segments
        //               processed so far.
        // We process segments left to right (they're non-overlapping by definition).
        // For each segment, we decide how many picks (0 to k) to use from it.
        
        // globalDP[j] = best profit using exactly j picks from segments seen so far
        long[] globalDP = new long[k + 1];
        // Initialize: 0 picks = 0 profit
        // All others start at 0 (we want "at most k", so we track exactly j and take max)
        
        foreach (var (segL, segR) in segments)
        {
            int L = segR - segL + 1; // Length of this segment
            
            // Compute innerDP for this segment:
            // innerDP[j] = max profit from exactly j non-overlapping sub-segments
            //              within prices[segL..segR]
            // We only need j up to min(k, L/2) since each sub-segment needs length >= 2
            int maxPicks = Math.Min(k, L / 2);
            
            // innerDP[j] for j = 0..maxPicks
            // We'll compute this using a 2D DP within the segment.
            // 
            // Let dp[i][j] = max profit using j picks from prices[segL..segL+i]
            //                where the j-th pick ends at segL+i (mandatory end here)
            // 
            // Transition: dp[i][j] = max over all a <= i-1 of:
            //   profit(a, i) + max(dp[a-1][j-1], dp[a][j-1], ...)
            //
            // This is complex. Let's use a cleaner formulation:
            // 
            // best[i][j] = max profit using j picks from prices[segL..segL+i]
            //              (the j-th pick can end anywhere <= segL+i)
            //
            // best[i][j] = max(best[i-1][j],   // don't end a pick at i
            //                  max over a<=i-1 of (profit(segL+a, segL+i) + best[a-1][j-1]))
            //                  // end a pick at [segL+a .. segL+i]
            //
            // profit(segL+a, segL+i) = max(prices[segL+a..segL+i]) - min(prices[segL+a..segL+i])
            //
            // For efficiency, we precompute prefix max and min for the segment.
            
            // Precompute prefix max and min for the segment
            // prefMax[i] = max of prices[segL..segL+i]
            // prefMin[i] = min of prices[segL..segL+i]
            // rangeMax[a][b] = max of prices[segL+a..segL+b]
            // rangeMin[a][b] = min of prices[segL+a..segL+b]
            // We'll use sparse table or just O(L^2) precomputation.
            
            // For L up to 10^5 and k up to 100, O(L^2) is too slow if L is large.
            // However, the key insight is: for j picks from a segment of length L,
            // the optimal strategy is to take j non-overlapping sub-segments.
            // 
            // IMPORTANT INSIGHT: For a turbulent segment, the profit of any
            // sub-segment [a,b] = max(a..b) - min(a..b).
            // The maximum profit from j picks is achieved by taking j non-overlapping
            // sub-segments that together cover the most "range".
            //
            // Actually, for j=1: take the whole segment (max profit = max-min of whole)
            // For j=2: split into two parts optimally
            // ...
            //
            // For large segments, we need an efficient approach.
            // 
            // PRACTICAL OBSERVATION: Since k <= 100, and we want at most k picks
            // total across ALL segments, from any single segment we take at most k picks.
            // If a segment has length L >> k, we only need to consider O(k) "boundary"
            // positions within it.
            //
            // For now, let's implement O(L^2 * k) and see if it's fast enough.
            // With n=10^5 and one big segment, this would be 10^10 * 100 = too slow.
            //
            // We need a smarter approach for large segments.
            //
            // SMARTER APPROACH for within-segment DP:
            // For a turbulent segment, when we pick j non-overlapping sub-segments,
            // we want to maximize the sum of (max-min) for each picked sub-segment.
            //
            // Key: profit(a,b) = max(a..b) - min(a..b)
            // 
            // For j picks, we can think of it as: choose 2j indices
            // l1 <= r1 < l2 <= r2 < ... < lj <= rj (all within segment)
            // to maximize sum of (max(li..ri) - min(li..ri))
            //
            // This is a hard combinatorial problem in general.
            // 
            // ALTERNATIVE: Since the segment is turbulent (all consecutive diffs > threshold),
            // and threshold >= 0, the prices alternate in a "zigzag" pattern of large jumps.
            // 
            // For a turbulent segment, the global max-min of the whole segment is the
            // best single pick. For multiple picks, we might do better by splitting.
            //
            // Let's think about it differently:
            // The profit of [a,b] = max(a..b) - min(a..b).
            // If we split [a,b] into [a,m] and [m+1,b]:
            // profit([a,m]) + profit([m+1,b]) 
            //   = (max(a..m)-min(a..m)) + (max(m+1..b)-min(m+1..b))
            // vs profit([a,b]) = max(a..b) - min(a..b)
            //
            // The split can be better when the two halves each have high range.
            //
            // For the DP, let's use a different state:
            // dp[j][i] = max profit using j picks from prices[segL..segL+i]
            //            (picks can end anywhere in [segL..segL+i])
            //
            // Transition:
            // dp[j][i] = max(dp[j][i-1],  // no pick ends at i
            //               max over a in [0..i-1] of (dp[j-1][a-1] + profit(a, i)))
            //               // a pick [a..i] is the j-th pick
            //               // (a-1 means "best j-1 picks from [0..a-1]", use -1 for empty)
            //
            // profit(a, i) = max(prices[segL+a..segL+i]) - min(prices[segL+a..segL+i])
            //
            // For fixed i and j, we need max over a of (dp[j-1][a-1] + profit(a,i))
            // = max over a of (dp[j-1][a-1] + rangeMax(a,i) - rangeMin(a,i))
            //
            // This is still O(L) per (i,j) pair, giving O(L^2 * k) total.
            //
            // For n=10^5 with one big segment and k=100, this is 10^12 ops. Too slow.
            //
            // PRACTICAL SOLUTION: 
            // Observe that for a turbulent segment, the optimal j picks will tend to
            // be "boundary-aligned" — they'll start and end at local extrema.
            // 
            // In a turbulent segment, local extrema occur at every other position
            // (since the sequence