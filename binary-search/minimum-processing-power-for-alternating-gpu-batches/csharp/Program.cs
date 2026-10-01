/*
Title: Minimum Processing Power for Alternating GPU Batches
Difficulty: Hard
Topic: Binary Search

Problem Description:
A machine learning platform must execute a sequence of training batches in the given order. The i-th batch requires work[i] units of computation. You are provisioning identical GPU nodes, each with the same processing power P. A single node can process a contiguous group of batches, and the time needed for that group is the sum of its work values divided by P.

Because of thermal balancing rules, nodes are assigned in alternating modes: the 1st used node is in "hot" mode, the 2nd in "cool" mode, the 3rd in "hot" mode again, and so on. A hot-mode node may be assigned batches whose total work is at most hotLimit, while a cool-mode node may be assigned batches whose total work is at most coolLimit. You may split the batch list into any number of contiguous groups, but the mode of each group is determined by its position among the groups. Every group must respect both its mode limit and the node's processing power deadline: its processing time must be at most T, meaning groupWork <= P * T.

Return the minimum integer processing power P such that all batches can be completed using at most m nodes.

If it is impossible for any processing power to satisfy the alternating mode limits, return -1.

Constraints:
- 1 <= n == work.length <= 200000
- 1 <= work[i] <= 10^9
- 1 <= m <= 200000
- 1 <= hotLimit, coolLimit <= 10^18
- 1 <= T <= 10^9
- P must be a positive integer

Notes:
- Each node handles a contiguous segment of the batch array.
- The first segment uses hot mode, the second cool mode, the third hot mode, etc.
- For a fixed P, feasibility is monotonic, so an O(n log answer) solution is expected.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Each feasibility check runs in O(n), because we scan the array once and maintain only a few variables.
    - We binary search the answer P over a range up to about 1e18, so that is O(log 1e18) ~= 60 iterations.
    - Total time complexity: O(n log answer)

    Space Complexity:
    - O(1) extra space, ignoring the input array.
    */
    public long MinimumProcessingPower(int[] work, int m, long hotLimit, long coolLimit, long T)
    {
        int n = work.Length;

        // ------------------------------------------------------------
        // Step 1: Quick impossibility checks that do NOT depend on P.
        // ------------------------------------------------------------
        //
        // Why this is necessary:
        // Even if processing power P becomes arbitrarily large, the time-based cap P*T
        // can become arbitrarily large too, so eventually only the alternating mode limits
        // matter: hotLimit for odd-numbered groups and coolLimit for even-numbered groups.
        //
        // If the very first batch cannot fit into a hot node, then no valid partition exists,
        // because the first group must always be hot and groups must be non-empty.
        //
        // Also, if m == 1, then the entire array must fit into one hot group.
        //
        // These checks are not strictly required for correctness because the later logic
        // would also discover impossibility, but they make the reasoning clearer.
        if (work[0] > hotLimit)
        {
            return -1;
        }

        if (m == 1)
        {
            long total = 0;
            foreach (int x in work) total += x;
            if (total > hotLimit) return -1;

            // Need smallest integer P such that total <= P * T.
            // That is P >= ceil(total / T).
            return CeilDiv(total, T);
        }

        // ------------------------------------------------------------
        // Step 2: Another impossibility check using "infinite power".
        // ------------------------------------------------------------
        //
        // Here we ask:
        // "If time were never the limiting factor, can the array be partitioned into
        // at most m alternating groups using only hotLimit / coolLimit?"
        //
        // If the answer is no, then no finite P can help, because increasing P only relaxes
        // the time cap, and cannot relax hotLimit or coolLimit.
        //
        // So we run the same feasibility logic with:
        // hotCap = hotLimit
        // coolCap = coolLimit
        //
        // If even that fails, return -1 immediately.
        if (!CanPartitionWithinM(work, m, hotLimit, coolLimit))
        {
            return -1;
        }

        // ------------------------------------------------------------
        // Step 3: Build binary search bounds for P.
        // ------------------------------------------------------------
        //
        // Lower bound:
        // P must be at least 1.
        //
        // Upper bound:
        // Since we already know a partition exists when only mode limits matter,
        // it is enough to make the time cap P*T at least max(hotLimit, coolLimit).
        // Then effective caps become exactly hotLimit and coolLimit.
        //
        // So any P satisfying P*T >= max(hotLimit, coolLimit) is definitely enough.
        // The smallest such upper candidate is ceil(maxLimit / T).
        //
        // We also ensure upper bound is at least 1.
        long left = 1;
        long right = Math.Max(1L, CeilDiv(Math.Max(hotLimit, coolLimit), T));

        // ------------------------------------------------------------
        // Step 4: Standard binary search on the answer.
        // ------------------------------------------------------------
        //
        // Why binary search works:
        // If a certain processing power P is feasible, then any larger power is also feasible,
        // because P*T only increases, so every group's time-based cap becomes weaker or equal.
        //
        // Therefore feasibility is monotonic:
        // false false false ... true true true
        //
        // We search for the first true.
        while (left < right)
        {
            long mid = left + (right - left) / 2;

            // Effective caps for this P:
            // A hot group must satisfy both:
            //   groupWork <= hotLimit
            //   groupWork <= P*T
            // so cap is min(hotLimit, P*T)
            //
            // Similarly for cool groups.
            long timeCap = SafeMultiply(mid, T);
            long hotCap = Math.Min(hotLimit, timeCap);
            long coolCap = Math.Min(coolLimit, timeCap);

            if (CanPartitionWithinM(work, m, hotCap, coolCap))
            {
                right = mid;
            }
            else
            {
                left = mid + 1;
            }
        }

        return left;
    }

    private static long CeilDiv(long a, long b)
    {
        return (a + b - 1) / b;
    }

    private static long SafeMultiply(long a, long b)
    {
        // We use saturation to long.MaxValue to avoid overflow.
        // For this problem, any value above both hotLimit and coolLimit behaves the same,
        // because later we take min(..., hotLimit/coolLimit).
        if (a == 0 || b == 0) return 0;
        if (a > long.MaxValue / b) return long.MaxValue;
        return a * b;
    }

    private bool CanPartitionWithinM(int[] work, int m, long hotCap, long coolCap)
    {
        // ------------------------------------------------------------
        // Core greedy feasibility check.
        // ------------------------------------------------------------
        //
        // We need to decide whether the array can be split into at most m contiguous groups
        // such that:
        // - group 1 sum <= hotCap
        // - group 2 sum <= coolCap
        // - group 3 sum <= hotCap
        // - ...
        //
        // A natural greedy idea is:
        // "For each current group, take as many consecutive batches as possible."
        //
        // Why greedy is correct here:
        // If we are building a group with a fixed cap, taking fewer elements than possible
        // can never help reduce the total number of groups needed later. It only leaves more
        // work for future groups. Therefore, the best way to minimize the number of groups
        // is always to extend the current group as far as allowed.
        //
        // Since we only need to know whether the minimum number of groups is <= m,
        // this greedy scan is exactly what we want.
        //
        // Important detail:
        // The first group is hot, the second cool, and so on. So the cap alternates
        // based on the group number, not based on the array index.
        int n = work.Length;
        int i = 0;
        int groupsUsed = 0;

        while (i < n)
        {
            groupsUsed++;

            // If we already exceeded the allowed number of groups, we can stop early.
            if (groupsUsed > m)
            {
                return false;
            }

            // Determine which cap applies to this group.
            // Group 1 => hot, Group 2 => cool, Group 3 => hot, ...
            long cap = (groupsUsed % 2 == 1) ? hotCap : coolCap;

            // Every group must be non-empty.
            // So if the next single batch already exceeds the current cap,
            // then this partitioning is impossible for this P.
            if (work[i] > cap)
            {
                return false;
            }

            // Greedily extend the current group as much as possible.
            long sum = 0;
            while (i < n && sum + work[i] <= cap)
            {
                sum += work[i];
                i++;
            }
        }

        return true;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] work1 = { 8, 5, 6, 4, 7 };
int m1 = 3;
long hotLimit1 = 13;
long coolLimit1 = 11;
long T1 = 2;
long result1 = solution.MinimumProcessingPower(work1, m1, hotLimit1, coolLimit1, T1);
Console.WriteLine(result1); // Expected: 6

// Example 2
int[] work2 = { 9, 9, 9 };
int m2 = 2;
long hotLimit2 = 8;
long coolLimit2 = 20;
long T2 = 3;
long result2 = solution.MinimumProcessingPower(work2, m2, hotLimit2, coolLimit2, T2);
Console.WriteLine(result2); // Expected: -1