/*
Title: Minimum Daily Build Quota for Staged Releases
Difficulty: Hard
Topic: Binary Search

Problem Description:
A software team must publish n features in the given order. Feature i requires builds[i] units of build effort. The team works over multiple days, but releases are staged: each day they may work on a contiguous suffix of the remaining effort for the current feature and then continue with later features, as long as the total effort completed that day does not exceed a fixed daily quota Q. A feature may be split across days, but a new rule applies: if a day starts working on feature i, then every earlier feature must already be fully completed. In other words, progress is always made from left to right, and partial work is allowed only on the current frontier feature.

You are also given an integer d, the maximum number of days allowed, and an integer k, the maximum number of features that may be split across more than one day. Your task is to find the minimum integer daily quota Q such that all features can be completed within at most d days while splitting at most k features.

Return that minimum Q.

A candidate quota Q is feasible if there exists a left-to-right schedule using at most d days and causing at most k split features.

Constraints:
- 1 <= n <= 200000
- 1 <= builds[i] <= 10^12
- 1 <= d <= 10^12
- 0 <= k <= n
- The answer fits in a 64-bit signed integer

Examples:
1) builds = [7, 2, 5, 10, 8], d = 3, k = 1 => 14
2) builds = [9, 9, 9], d = 2, k = 0 => 18
*/

using System;
using System.Collections.Generic;

class Solution
{
    /*
    Time Complexity:
    - Binary search over the answer range: O(log(sum(builds)))
    - Each feasibility check:
        * O(n) when k == 0
        * O(n) when k >= n (or effectively unrestricted splitting)
        * O(n log n) in the general case because we maintain a max-heap of "saved days"
    Overall:
    - O(n log n log(sum(builds))) in the general case

    Space Complexity:
    - O(n) in the worst case for the heap used inside the feasibility check
    */
    public long MinimumDailyQuota(long[] builds, long d, int k)
    {
        long low = 1;
        long high = 0;

        // The answer can never be larger than the total amount of work,
        // because with quota = total sum we can always finish in one day.
        foreach (long x in builds)
        {
            high += x;
            if (x > low) low = x == 0 ? low : 1; // low will be reset below more carefully
        }

        // A safe lower bound:
        // - If splitting is not allowed for a feature, that feature must fit in one day.
        // - Even with splitting allowed, every day can do at most Q work, so Q >= ceil(total / d) is also necessary.
        long maxBuild = 0;
        foreach (long x in builds) maxBuild = Math.Max(maxBuild, x);

        low = Math.Max(1, (high + d - 1) / d);

        // If no splitting is allowed at all, every single feature must fit entirely in one day.
        // So in that special case, Q must also be at least the maximum feature size.
        if (k == 0)
            low = Math.Max(low, maxBuild);

        while (low < high)
        {
            long mid = low + (high - low) / 2;

            if (IsFeasible(builds, d, k, mid))
                high = mid;
            else
                low = mid + 1;
        }

        return low;
    }

    private bool IsFeasible(long[] builds, long d, int k, long q)
    {
        // Special case 1:
        // If no feature may be split, this becomes the classic "split array into at most d parts"
        // while preserving order and requiring each feature to stay whole.
        if (k == 0)
            return IsFeasibleNoSplit(builds, d, q);

        // Special case 2:
        // If we may split as many features as we want, then the only thing that matters is total work.
        // The minimum number of days is simply ceil(total / q).
        // This is because divisible work can be packed perfectly across days.
        if (k >= builds.Length)
            return IsFeasibleUnlimitedSplits(builds, d, q);

        // General case:
        //
        // Key idea:
        // We process features from left to right and imagine "cut positions" between days.
        //
        // For a fixed quota q:
        // - If a feature of size a is NOT split, it contributes ceil(a / q) days if placed alone across enough days?
        //   That interpretation is not the right one because unsplit means it must stay inside one day.
        //
        // A better viewpoint is this:
        // - If splitting were completely unrestricted, the minimum number of days is ceil(total / q).
        // - However, every time a day boundary falls strictly inside a feature, that feature becomes split.
        // - So the problem becomes:
        //     Can we place at most d-1 day boundaries in the total work line,
        //     such that at most k of those boundaries lie inside features,
        //     and every day segment has length at most q?
        //
        // Another equivalent and very useful formulation:
        // For each feature of size a:
        //   - If we allow splitting freely inside this feature, then this feature can be spread over ceil(a / q) day segments.
        //     That creates:
        //         mandatory internal boundaries = floor((a - 1) / q)
        //     and this feature is split iff a > q.
        //   - But if we decide to align some day boundaries with feature borders globally,
        //     we can "save" days compared to the unrestricted packing.
        //
        // The clean greedy formulation used below is:
        //
        // We scan features and greedily pack whole features into days as much as possible.
        // Whenever a feature does not fit in the remaining capacity of the current day:
        //   1) We may split it here, consuming one split-feature if this is the first time this feature is split.
        //   2) Or we may end the current day before this feature and start it fresh next day.
        //
        // The challenge is that local choices affect future days.
        //
        // A powerful way to optimize this is:
        // - Start from the schedule where every feature starts on a fresh day block arrangement with splitting allowed as needed.
        // - Count how many days are needed if each feature is handled independently:
        //       baseDays = sum(ceil(builds[i] / q))
        //   In this schedule, every feature with builds[i] > q is split.
        // - But adjacent features can share a day boundary:
        //   if feature i leaves some unused space in its last day and feature i+1 can use some space in its first day,
        //   then one day can be merged away.
        //
        // For feature size a:
        //   first-day usage if starting fresh = min(a, q)
        //   last-day usage if ending fresh = ((a - 1) % q) + 1
        //   if a % q == 0, last-day usage = q
        //
        // Between consecutive features i and i+1, we can merge one day iff:
        //   lastUsage(i) + firstUsage(i+1) <= q
        // Since firstUsage(i+1) = min(builds[i+1], q), and if builds[i+1] > q then firstUsage = q,
        // such a merge is impossible unless builds[i+1] <= q.
        //
        // More generally, choosing merges affects whether a feature is considered split:
        // - A feature is split iff it occupies more than one day in the final schedule.
        // - For builds[i] > q, splitting is unavoidable.
        // - For builds[i] <= q, it can still become split only if we intentionally cut inside it, which is never beneficial.
        //
        // Therefore:
        // - Unavoidable split count = number of features with builds[i] > q.
        // - If that already exceeds k, impossible.
        // - Minimum possible days:
        //     sum(ceil(builds[i] / q)) - maximum number of valid merges between consecutive features
        //   where a merge between i and i+1 is possible iff tail(i) + head(i+1) <= q.
        //
        // Importantly, merges are only between consecutive features and each boundary can be merged independently.
        // So the maximum merges is simply the count of boundaries where merging is possible.
        //
        // This yields an O(n) feasibility check.
        //
        // Let's verify on example 1 with q = 14:
        // builds: 7,2,5,10,8
        // ceil parts: 1,1,1,1,1 => baseDays = 5
        // unavoidable splits: 0 (since all <= 14)
        // merge possible:
        // 7+2<=14 yes
        // 2+5<=14 yes
        // 5+10<=14 no
        // 10+8<=14 no
        // max merges = 2
        // min days = 5-2 = 3, split count = 0 <= 1 => feasible
        //
        // Example 2 with q = 17:
        // 9,9,9 => baseDays=3
        // merges: 9+9<=17? no, no => min days=3 > 2 => infeasible
        // q = 18:
        // baseDays=3, merges yes at both boundaries => min days=1? That would be wrong.
        //
        // So the above simplistic merge model is NOT sufficient because merges are not independent:
        // if we merge (1,2) and (2,3), feature 2's single day cannot simultaneously share with both sides.
        //
        // Therefore we need a more careful DP/greedy.
        //
        // Correct model:
        // Each feature i contributes len[i] = ceil(builds[i] / q) "chunks of days" if isolated.
        // Between i and i+1, we may save one day by letting the last partial day of i and the first partial day of i+1 be the same day.
        // But a feature can participate in a left merge and a right merge simultaneously only if it spans at least 2 isolated days,
        // because then it has distinct first and last day. If len[i] == 1, it has only one day and cannot share both sides.
        //
        // This becomes a path DP:
        // choose merges on edges of a line graph,
        // with the restriction that adjacent chosen edges are allowed only when the middle feature has len >= 2.
        //
        // Also, split count in the final schedule:
        // a feature is split iff its occupied day count in final schedule is >= 2.
        // For builds[i] > q, len[i] >= 2, so split is unavoidable.
        // For builds[i] <= q, len[i] = 1, and such a feature can never be split in an optimal schedule because splitting it would only increase days.
        // So split count is exactly count(builds[i] > q), independent of merges.
        //
        // Thus:
        // 1) If unavoidable splits > k => impossible.
        // 2) Compute minimum days = sum(len[i]) - maximum number of chosen merges,
        //    where edge i (between i and i+1) is available iff tail(i)+head(i+1) <= q,
        //    and two adjacent edges i-1 and i can both be chosen only if len[i] >= 2.
        //
        // We solve maximum chosen merges on this path with a simple DP in O(n).
        long splitCount = 0;
        int n = builds.Length;
        long[] len = new long[n];
        long baseDays = 0;

        for (int i = 0; i < n; i++)
        {
            len[i] = (builds[i] + q - 1) / q;
            baseDays += len[i];
            if (builds[i] > q) splitCount++;
        }

        if (splitCount > k)
            return false;

        if (baseDays <= d)
            return true;

        bool[] canMerge = new bool[Math.Max(0, n - 1)];
        for (int i = 0; i + 1 < n; i++)
        {
            long tail = builds[i] % q;
            if (tail == 0) tail = q;

            long head = Math.Min(builds[i + 1], q);

            canMerge[i] = tail + head <= q;
        }

        // DP over edges.
        //
        // dp0 = best number of merges chosen up to previous edge, when previous edge is NOT chosen
        // dp1 = best number of merges chosen up to previous edge, when previous edge IS chosen
        //
        // Transition for current edge i (between feature i and i+1):
        // - Not choose it:
        //     newDp0 = max(dp0, dp1)
        // - Choose it (only if canMerge[i]):
        //     * from dp0 always allowed
        //     * from dp1 allowed only if the middle feature i has len[i] >= 2
        //
        // Careful with indexing:
        // edge i is between feature i and i+1
        // adjacent previous edge is i-1, and the middle feature is feature i
        long negInf = long.MinValue / 4;
        long dp0 = 0;
        long dp1 = negInf;

        for (int edge = 0; edge < n - 1; edge++)
        {
            long newDp0 = Math.Max(dp0, dp1);
            long newDp1 = negInf;

            if (canMerge[edge])
            {
                // Start choosing this edge after not choosing previous edge.
                newDp1 = Math.Max(newDp1, dp0 + 1);

                // If previous edge was also chosen, then the shared middle feature is feature 'edge'.
                // That is only possible when this feature spans at least two isolated days.
                if (edge > 0 && len[edge] >= 2)
                {
                    newDp1 = Math.Max(newDp1, dp1 + 1);
                }
            }

            dp0 = newDp0;
            dp1 = newDp1;
        }

        long maxMerges = Math.Max(dp0, dp1);
        long minDays = baseDays - maxMerges;

        return minDays <= d;
    }

    private bool IsFeasibleNoSplit(long[] builds, long d, long q)
    {
        long daysUsed = 1;
        long currentDayLoad = 0;

        foreach (long x in builds)
        {
            // If one whole feature is larger than the daily quota,
            // then it cannot fit into any day without splitting, so impossible.
            if (x > q)
                return false;

            // If the current feature still fits into the current day, place it there.
            if (currentDayLoad + x <= q)
            {
                currentDayLoad += x;
            }
            else
            {
                // Otherwise, start a new day for this feature.
                daysUsed++;
                currentDayLoad = x;

                // Early exit if we already exceeded the allowed number of days.
                if (daysUsed > d)
                    return false;
            }
        }

        return daysUsed <= d;
    }

    private bool IsFeasibleUnlimitedSplits(long[] builds, long d, long q)
    {
        long total = 0;
        foreach (long x in builds) total += x;

        long neededDays = (total + q - 1) / q;
        return neededDays <= d;
    }
}

// Demo code
var solution = new Solution();

long[] builds1 = { 7, 2, 5, 10, 8 };
long d1 = 3;
int k1 = 1;
Console.WriteLine(solution.MinimumDailyQuota(builds1, d1, k1)); // Expected: 14

long[] builds2 = { 9, 9, 9 };
long d2 = 2;
int k2 = 0;
Console.WriteLine(solution.MinimumDailyQuota(builds2, d2, k2)); // Expected: 18