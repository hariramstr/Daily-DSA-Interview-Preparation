/*
Title: Maximum Viable Cache TTL
Difficulty: Hard
Topic: Binary Search

Problem Description:
You are designing a cache for a backend service. There are n requests arriving in chronological order, and request i arrives at time times[i] and would cost costs[i] units to recompute if it is not already in cache. The cache uses a single global TTL (time-to-live) value T. If two consecutive requests for the same key happen within T seconds of each other, then every request after the first within that TTL window is served from cache and contributes 0 recomputation cost. Otherwise, the request is a cache miss and its full recomputation cost is paid.

You are given three arrays of equal length: keys, times, and costs, where keys[i] is the resource requested by the i-th request. The arrays are sorted by nondecreasing times. You are also given a budget B, representing the maximum total recomputation cost the system can afford.

Return the maximum integer TTL T such that the total recomputation cost is at most B. If even T = 0 satisfies the budget, you may return 0 or a larger valid TTL if possible. If no TTL can make the total cost at most B, return -1.

A request is considered a cache hit only if there exists an earlier request for the same key whose cached value has not expired yet. More formally, for a fixed key, if the previous request for that key happened at time p and the current request happens at time c, then the current request is a hit iff c - p <= T; otherwise it is a miss and refreshes the cached value.

Constraints:
- 1 <= n <= 200000
- 1 <= keys[i] <= 10^9
- 0 <= times[i] <= 10^18
- times is sorted in nondecreasing order
- 1 <= costs[i] <= 10^9
- 0 <= B <= 10^18

Important note about the answer domain:
As T increases, total recomputation cost never increases. Therefore, if some T is valid, then every larger T is also valid.
In the real-number/integer sense, the answer could be unbounded once all relevant request gaps are covered.
So for this problem we search only over TTL values induced by actual consecutive same-key gaps.
That means:
- The answer can only "change" when T reaches one of those gaps.
- If T = 0 is valid but there are no repeated keys, we return 0.
- Otherwise, among all induced gap values that satisfy the budget, return the largest one.
- If no induced gap works but T = 0 works, return 0.
- If even the best possible TTL cannot make total cost <= B, return -1.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building the list of candidate TTL gap values: O(n)
    - Each feasibility check: O(n)
    - Binary search over distinct candidate gaps: O(log n)
    - Total: O(n log n)

    Space Complexity:
    - Candidate gaps list + sorting support: O(n)
    - Hash map used in feasibility check: O(number of distinct keys), worst-case O(n)
    - Total: O(n)

    Beginner-friendly high-level idea:
    1. The total recomputation cost only changes when T crosses a gap between two consecutive requests of the same key.
    2. So instead of searching all integers from 0 to 10^18, we only search those meaningful gap values.
    3. For a fixed T, we simulate the requests from left to right:
       - If the same key was seen recently enough, it is a cache hit and costs 0.
       - Otherwise it is a miss and we pay its recomputation cost.
    4. Because larger T can only create more hits, feasibility is monotonic:
       - If some T works, then any larger T also works.
       This lets us binary search for the largest valid candidate gap.
    */
    public long MaximumViableCacheTTL(int[] keys, long[] times, int[] costs, long budget)
    {
        int n = keys.Length;

        // Step 1:
        // Collect all TTL values where the answer can possibly change.
        //
        // Why this works:
        // A request changes from "miss" to "hit" exactly when T becomes at least
        // the gap between this request and the immediately previous request of the same key.
        // Between two such gap values, the set of hits/misses stays identical.
        //
        // Therefore, we only need to consider:
        // - T = 0
        // - every consecutive same-key gap
        //
        // Data structure choice:
        // We use a dictionary from key -> last seen time while scanning once.
        // This lets us compute each consecutive same-key gap in O(1) average time.
        var lastSeenForGapBuild = new Dictionary<int, long>();
        var candidateGaps = new List<long>();

        for (int i = 0; i < n; i++)
        {
            int key = keys[i];
            long currentTime = times[i];

            if (lastSeenForGapBuild.TryGetValue(key, out long previousTime))
            {
                long gap = currentTime - previousTime;
                candidateGaps.Add(gap);
            }

            lastSeenForGapBuild[key] = currentTime;
        }

        // Step 2:
        // Check whether even the "best possible" TTL among induced thresholds can satisfy the budget.
        //
        // If we choose a TTL at least as large as every relevant consecutive same-key gap,
        // then every non-first request for each key becomes a hit.
        // This is the minimum achievable recomputation cost under the problem's induced-threshold interpretation.
        //
        // If even that minimum cost is still above budget, answer is -1.
        long minimumPossibleCost = ComputeCostWithInfiniteLikeTTL(keys, costs);
        if (minimumPossibleCost > budget)
        {
            return -1;
        }

        // Step 3:
        // If there are no repeated keys, there are no induced gap values at all.
        //
        // In that case, TTL never changes anything:
        // every request is always a miss because no key repeats.
        // Since we already know the minimum possible cost is within budget,
        // returning 0 is the natural answer in the induced-threshold search space.
        if (candidateGaps.Count == 0)
        {
            return 0;
        }

        // Step 4:
        // Sort candidate gaps and remove duplicates.
        //
        // Why sorting is necessary:
        // Binary search requires an ordered search space.
        //
        // Why deduplication is useful:
        // If the same gap appears many times, checking it repeatedly is wasteful.
        candidateGaps.Sort();
        var distinctCandidates = new List<long>(candidateGaps.Count);

        foreach (long gap in candidateGaps)
        {
            if (distinctCandidates.Count == 0 || distinctCandidates[^1] != gap)
            {
                distinctCandidates.Add(gap);
            }
        }

        // Step 5:
        // It is possible that no positive induced gap is needed because T = 0 already works.
        //
        // We keep 0 as a special baseline candidate even if it is not present in the gap list.
        // Then we search for the largest valid value among:
        // [0] union distinct induced gaps.
        //
        // Instead of physically inserting 0 and re-sorting, we handle it explicitly:
        // - first test T = 0
        // - then binary search the sorted positive/zero gap candidates
        long answer = IsFeasible(keys, times, costs, budget, 0) ? 0 : -1;

        // Step 6:
        // Binary search for the largest candidate gap whose total cost is <= budget.
        //
        // Monotonicity:
        // If a TTL value T is feasible, then any larger TTL is also feasible,
        // because increasing TTL can only turn misses into hits, never the reverse.
        int left = 0;
        int right = distinctCandidates.Count - 1;
        int bestIndex = -1;

        while (left <= right)
        {
            int mid = left + (right - left) / 2;
            long ttl = distinctCandidates[mid];

            // For this candidate TTL, simulate the cache behavior.
            if (IsFeasible(keys, times, costs, budget, ttl))
            {
                // This TTL works, so try to go larger.
                bestIndex = mid;
                left = mid + 1;
            }
            else
            {
                // This TTL does not work, so any smaller search must be on the left side.
                right = mid - 1;
            }
        }

        if (bestIndex != -1)
        {
            answer = Math.Max(answer, distinctCandidates[bestIndex]);
        }

        return answer;
    }

    private long ComputeCostWithInfiniteLikeTTL(int[] keys, int[] costs)
    {
        // This helper computes the minimum possible total cost:
        // only the first request for each distinct key is a miss,
        // and every later request for that key is a hit.
        //
        // Why this is correct:
        // If TTL is at least every consecutive same-key gap, then once a key appears,
        // every next request for that key is connected by a chain of non-expired cache hits.
        //
        // We only need to pay the first occurrence cost for each key.
        var seen = new HashSet<int>();
        long total = 0;

        for (int i = 0; i < keys.Length; i++)
        {
            if (seen.Add(keys[i]))
            {
                total += costs[i];
            }
        }

        return total;
    }

    private bool IsFeasible(int[] keys, long[] times, int[] costs, long budget, long ttl)
    {
        // This method answers:
        // "If the cache uses TTL = ttl, is the total recomputation cost <= budget?"
        //
        // We simulate requests in chronological order.
        //
        // Data structure choice:
        // Dictionary<int, long> lastSeenTime
        // - key   = resource key
        // - value = time of the immediately previous request for that key
        //
        // Why only the immediately previous request matters:
        // The problem definition says the current request is a hit iff the previous request
        // for the same key happened within TTL.
        // If the previous request is too old, then the current one is a miss and refreshes the cache.
        //
        // Important subtle point:
        // Even if an older request was within TTL of some earlier request, that does not matter directly.
        // The cache state for a key is fully represented by the most recent request time for that key.
        var lastSeenTime = new Dictionary<int, long>();
        long totalCost = 0;

        for (int i = 0; i < keys.Length; i++)
        {
            int key = keys[i];
            long currentTime = times[i];
            int currentCost = costs[i];

            // Step A:
            // Determine whether this request is a hit or a miss.
            //
            // If we have never seen this key before, it must be a miss.
            // Otherwise, compare the time gap to TTL.
            bool isHit = false;

            if (lastSeenTime.TryGetValue(key, out long previousTime))
            {
                long gap = currentTime - previousTime;

                // A request is a hit exactly when the previous same-key request
                // happened within ttl seconds.
                if (gap <= ttl)
                {
                    isHit = true;
                }
            }

            // Step B:
            // If this is a miss, we must pay its recomputation cost.
            if (!isHit)
            {
                totalCost += currentCost;

                // Early exit optimization:
                // As soon as we exceed budget, there is no need to continue.
                if (totalCost > budget)
                {
                    return false;
                }
            }

            // Step C:
            // Update the most recent request time for this key.
            //
            // This is necessary whether the request was a hit or a miss,
            // because future requests compare against the immediately previous request.
            lastSeenTime[key] = currentTime;
        }

        return totalCost <= budget;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] keys1 = { 1, 2, 1, 1, 2 };
long[] times1 = { 1, 2, 4, 8, 10 };
int[] costs1 = { 5, 7, 5, 5, 7 };
long budget1 = 17;

// Under the induced-gap interpretation described in the prompt,
// the meaningful thresholds are gaps {3,4,8}.
// Costs:
// T=0 -> 29
// T=3 -> 24
// T=4 -> 19
// T=8 -> 12
// Largest valid induced threshold for budget 17 is 8.
Console.WriteLine(solution.MaximumViableCacheTTL(keys1, times1, costs1, budget1)); // Expected: 8

// Example 2
int[] keys2 = { 3, 3, 3, 4 };
long[] times2 = { 5, 9, 15, 20 };
int[] costs2 = { 4, 4, 4, 10 };
long budget2 = 13;

// Meaningful thresholds are gaps {4,6}.
// T=0 -> 22
// T=4 -> 18
// T=6 -> 14
// None satisfy budget 13, so answer is -1.
Console.WriteLine(solution.MaximumViableCacheTTL(keys2, times2, costs2, budget2)); // Expected: -1

// Extra demo where T=0 already works
int[] keys3 = { 1, 2, 3 };
long[] times3 = { 1, 2, 3 };
int[] costs3 = { 2, 2, 2 };
long budget3 = 10;
Console.WriteLine(solution.MaximumViableCacheTTL(keys3, times3, costs3, budget3)); // Expected: 0

// Extra demo where a positive induced TTL works
int[] keys4 = { 3, 3, 3, 4 };
long[] times4 = { 5, 9, 15, 20 };
int[] costs4 = { 4, 4, 4, 10 };
long budget4 = 14;
Console.WriteLine(solution.MaximumViableCacheTTL(keys4, times4, costs4, budget4)); // Expected: 6