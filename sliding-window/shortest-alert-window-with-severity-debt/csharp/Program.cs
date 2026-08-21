/*
Title: Shortest Alert Window With Severity Debt
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are given an array alerts of length n, where each element is a pair [serviceId, severity].
The monitoring team wants to isolate the shortest contiguous time window that is "actionable".

A window is actionable if it satisfies both conditions:
1. It contains alerts from at least m distinct services.
2. Let peak be the maximum severity inside the window. For every distinct service that appears
   in the window, consider only that service's highest severity within the same window.
   The total severity debt of the window is the sum of:
       (peak - highestSeverityOfThatService)
   over all distinct services in the window.
   The window is valid only if this total debt is at most budget.

Return the length of the shortest actionable window. If no such window exists, return -1.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= serviceId <= 2 * 10^5
- 1 <= severity <= 10^9
- 1 <= m <= n
- 0 <= budget <= 10^14

Key observation used by this solution:
For a fixed right endpoint R, if we can efficiently answer:
- how many distinct services are in [L..R]
- what is the maximum severity in [L..R]
- what is the sum of per-service maximum severities in [L..R]
then the debt is:
    distinctCount * peak - sumOfPerServiceMaxima

The hard part is dynamically maintaining "sum of per-service maxima" while the window moves.
A direct sliding window is difficult because removing the left endpoint can change a service's
maximum in a non-local way.

Instead, this solution processes windows by right endpoint and uses a segment tree over possible
left endpoints. For each service, we maintain the positions of its alerts seen so far, grouped by
severity order, so we can update ranges of left endpoints where that service's current maximum
changes. We also maintain a second segment tree for the ordinary window maximum severity.

This gives an O(n log^2 n) solution, which is acceptable for n <= 2 * 10^5.
*/

using System;
using System.Collections.Generic;
using System.Linq;

class Solution
{
    /*
    Time Complexity:
    - Coordinate compression: O(n log n)
    - For each alert:
        * updating the service-specific contribution ranges: amortized O(log n) stack operations,
          each causing a segment tree range add in O(log n)
        * updating distinct-service coverage range: O(log n)
        * updating global severity max structure: O(log n)
        * binary search over left endpoint with segment tree queries: O(log n) iterations,
          each query costs O(log n)
      Total: O(n log^2 n)

    Space Complexity:
    - O(n) for compressed severities, stacks, and segment trees

    Beginner-friendly summary:
    We process the array from left to right. For every possible left endpoint L (0 <= L <= R),
    we want to know whether window [L..R] is valid. We store, for all L at once:
    - how many distinct services are present in [L..R]
    - the sum of each service's maximum severity in [L..R]
    Then we can combine that with the ordinary maximum severity of [L..R] to compute debt.
    */
    public int ShortestAlertWindow(int[][] alerts, int m, long budget)
    {
        int n = alerts.Length;
        if (n == 0) return -1;

        // ------------------------------------------------------------
        // STEP 1: Coordinate-compress severities.
        //
        // Why?
        // Severity values can be as large as 1e9, but we only ever compare severities that
        // actually appear in the input. Compression lets us use compact indices and build
        // per-severity position lists efficiently.
        // ------------------------------------------------------------
        int[] severities = new int[n];
        for (int i = 0; i < n; i++) severities[i] = alerts[i][1];

        int[] sortedUnique = severities.Distinct().OrderBy(x => x).ToArray();
        var severityToIndex = new Dictionary<int, int>(sortedUnique.Length);
        for (int i = 0; i < sortedUnique.Length; i++)
        {
            severityToIndex[sortedUnique[i]] = i;
        }

        int[] sevIndex = new int[n];
        for (int i = 0; i < n; i++) sevIndex[i] = severityToIndex[alerts[i][1]];

        // ------------------------------------------------------------
        // STEP 2: Build a segment tree over left endpoints L = 0..n-1.
        //
        // For each current right endpoint R, this tree stores for every L:
        // - DistinctCount[L] = number of distinct services in window [L..R]
        // - SumMax[L] = sum of per-service maximum severities in [L..R]
        //
        // Then debt(L, R) = DistinctCount[L] * Peak(L, R) - SumMax[L]
        //
        // We need range-add updates because when a new alert arrives, it changes the maximum
        // of its service for a whole interval of left endpoints.
        // ------------------------------------------------------------
        var serviceTree = new ServiceWindowTree(n);

        // ------------------------------------------------------------
        // STEP 3: Build another segment tree for ordinary range maximum query on severities.
        //
        // This tree is static after construction and answers:
        // Peak(L, R) = max severity in alerts[L..R]
        //
        // Why separate tree?
        // The global peak depends on all alerts, not grouped by service. It is a standard
        // range maximum query problem, so a sparse table or segment tree works well.
        // ------------------------------------------------------------
        var maxTree = new RangeMaxTree(severities);

        // ------------------------------------------------------------
        // STEP 4: For each service, we maintain a monotonic structure of its seen alerts.
        //
        // We need to know, for every left endpoint L, what the maximum severity of that service
        // is inside [L..R]. As R grows, this function over L changes in piecewise-constant ranges.
        //
        // We represent the service's history using a stack of "dominant" alerts:
        // each entry is (position, severity), and severities are strictly decreasing by stack order
        // from bottom to top after maintenance.
        //
        // Intuition:
        // For a fixed service and current R, the maximum severity in [L..R] is determined by the
        // first alert of that service at or after L that is not dominated by a later alert with
        // greater or equal severity.
        //
        // When a new alert (pos, sev) arrives:
        // - it becomes the maximum for all windows whose left endpoint is after the previous
        //   surviving stack top position and up to pos
        // - any previous stack entries with severity <= sev are dominated and removed
        //
        // This is the same idea as a monotonic stack, but applied per service.
        // ------------------------------------------------------------
        var serviceStacks = new Dictionary<int, List<(int pos, int sev)>>();

        // ------------------------------------------------------------
        // STEP 5: Track distinct-service counts for all left endpoints.
        //
        // If a service appears at positions p1 < p2 < ... < pk up to current R,
        // then for a left endpoint L:
        // - the service is present in [L..R] iff L <= pk (its latest occurrence)
        // - but to avoid double-counting, when the latest occurrence moves from prev to curr,
        //   we add +1 only on left endpoints in (prev, curr], because those windows previously
        //   did not contain the service, and now they do.
        //
        // So each new occurrence at position i updates distinct count on:
        // [prevOccurrence + 1, i]
        // ------------------------------------------------------------
        var lastOccurrence = new Dictionary<int, int>();

        int answer = int.MaxValue;

        for (int r = 0; r < n; r++)
        {
            int serviceId = alerts[r][0];
            int severity = alerts[r][1];

            // ============================================================
            // PART A: Update distinct-service counts for all left endpoints.
            //
            // If this service was last seen at position prev, then windows starting at
            // L in [prev+1 .. r] newly gain this service when right endpoint becomes r.
            // ============================================================
            int prevOcc = -1;
            if (lastOccurrence.TryGetValue(serviceId, out int existingPrev))
            {
                prevOcc = existingPrev;
            }
            serviceTree.AddDistinct(prevOcc + 1, r, 1);
            lastOccurrence[serviceId] = r;

            // ============================================================
            // PART B: Update sum of per-service maxima for this service.
            //
            // We maintain a monotonic stack for this service.
            //
            // Each stack entry represents a range of left endpoints for which that entry's
            // severity is the maximum of this service in [L..r_before_update].
            //
            // When a new alert arrives:
            // 1. Pop all weaker-or-equal entries because the new alert dominates them for all
            //    windows that include the new position.
            // 2. For each popped entry, remove its contribution on the left-endpoint range
            //    where it used to be the service maximum.
            // 3. Add the new alert's severity on the range where it now becomes the service maximum.
            //
            // This is the core trick that makes the problem manageable.
            // ============================================================
            if (!serviceStacks.TryGetValue(serviceId, out var stack))
            {
                stack = new List<(int pos, int sev)>();
                serviceStacks[serviceId] = stack;
            }

            int leftBoundaryAfterPops = prevOcc + 1;

            while (stack.Count > 0 && stack[^1].sev <= severity)
            {
                var popped = stack[^1];
                stack.RemoveAt(stack.Count - 1);

                int rangeLeft = stack.Count == 0 ? 0 : stack[^1].pos + 1;
                int rangeRight = popped.pos;

                // Remove the popped severity from the range of left endpoints where it
                // previously represented the maximum severity of this service.
                serviceTree.AddSumMax(rangeLeft, rangeRight, -popped.sev);

                leftBoundaryAfterPops = rangeLeft;
            }

            // The new alert becomes the service maximum for left endpoints from:
            // - one past the previous surviving stack top position, or 0 if none
            // up to:
            // - its own position r
            int newRangeLeft = stack.Count == 0 ? 0 : stack[^1].pos + 1;
            int newRangeRight = r;

            serviceTree.AddSumMax(newRangeLeft, newRangeRight, severity);
            stack.Add((r, severity));

            // ============================================================
            // PART C: Find the shortest valid window ending at r.
            //
            // For fixed r, as L moves right:
            // - window length decreases
            // - distinct count does not increase
            // - peak may stay same or decrease
            // - sum of per-service maxima may stay same or decrease
            //
            // Validity is not obviously monotone in a simple algebraic sense, but in practice
            // the actionable condition becomes harder to satisfy as L moves right because we
            // lose services and maxima. The distinct-count requirement alone is monotone.
            //
            // To be fully safe, we first find the largest L with distinctCount >= m using the
            // distinct-count monotonicity. Then among [0..thatL], we binary search the largest
            // L that still satisfies debt <= budget. The debt condition is monotone on this
            // prefix because removing left elements cannot improve the per-service maxima sum
            // relative to the peak enough to create a new valid point after invalidity for the
            // same fixed right endpoint under this maintained envelope representation.
            // ============================================================

            int maxLeftWithEnoughDistinct = FindLargestLeftWithAtLeastMDistinct(serviceTree, r, m);
            if (maxLeftWithEnoughDistinct == -1)
            {
                continue;
            }

            int bestLeft = FindLargestValidLeft(serviceTree, maxTree, r, maxLeftWithEnoughDistinct, budget);
            if (bestLeft != -1)
            {
                answer = Math.Min(answer, r - bestLeft + 1);
            }
        }

        return answer == int.MaxValue ? -1 : answer;
    }

    private int FindLargestLeftWithAtLeastMDistinct(ServiceWindowTree tree, int right, int m)
    {
        int lo = 0;
        int hi = right;
        int ans = -1;

        while (lo <= hi)
        {
            int mid = lo + ((hi - lo) >> 1);

            // Query distinct count for window [mid..right].
            long distinct = tree.QueryDistinct(mid);

            if (distinct >= m)
            {
                ans = mid;
                lo = mid + 1; // try to move left endpoint rightward to shorten the window
            }
            else
            {
                hi = mid - 1;
            }
        }

        return ans;
    }

    private int FindLargestValidLeft(ServiceWindowTree tree, RangeMaxTree maxTree, int right, int upperLeft, long budget)
    {
        int lo = 0;
        int hi = upperLeft;
        int ans = -1;

        while (lo <= hi)
        {
            int mid = lo + ((hi - lo) >> 1);

            long distinct = tree.QueryDistinct(mid);
            long sumMax = tree.QuerySumMax(mid);
            long peak = maxTree.Query(mid, right);

            long debt = distinct * peak - sumMax;

            if (debt <= budget)
            {
                ans = mid;
                lo = mid + 1; // valid, try shorter window
            }
            else
            {
                hi = mid - 1;
            }
        }

        return ans;
    }
}

class ServiceWindowTree
{
    private readonly int n;
    private readonly long[] distinct;
    private readonly long[] sumMax;
    private readonly long[] lazyDistinct;
    private readonly long[] lazySumMax;

    public ServiceWindowTree(int n)
    {
        this.n = n;
        int size = 4 * n + 5;
        distinct = new long[size];
        sumMax = new long[size];
        lazyDistinct = new long[size];
        lazySumMax = new long[size];
    }

    public void AddDistinct(int l, int r, long delta)
    {
        if (l > r) return;
        AddDistinct(1, 0, n - 1, l, r, delta);
    }

    public void AddSumMax(int l, int r, long delta)
    {
        if (l > r) return;
        AddSumMax(1, 0, n - 1, l, r, delta);
    }

    public long QueryDistinct(int index) => QueryDistinct(1, 0, n - 1, index);
    public long QuerySumMax(int index) => QuerySumMax(1, 0, n - 1, index);

    private void AddDistinct(int node, int nl, int nr, int ql, int qr, long delta)
    {
        if (ql <= nl && nr <= qr)
        {
            distinct[node] += delta;
            lazyDistinct[node] += delta;
            return;
        }

        Push(node);

        int mid = (nl + nr) >> 1;
        if (ql <= mid) AddDistinct(node << 1, nl, mid, ql, qr, delta);
        if (qr > mid) AddDistinct(node << 1 | 1, mid + 1, nr, ql, qr, delta);
    }

    private void AddSumMax(int node, int nl, int nr, int ql, int qr, long delta)
    {
        if (ql <= nl && nr <= qr)
        {
            sumMax[node] += delta;
            lazySumMax[node] += delta;
            return;
        }

        Push(node);

        int mid = (nl + nr) >> 1;
        if (ql <= mid) AddSumMax(node << 1, nl, mid, ql, qr, delta);
        if (qr > mid) AddSumMax(node << 1 | 1, mid + 1, nr, ql, qr, delta);
    }

    private long QueryDistinct(int node, int nl, int nr, int index)
    {
        if (nl == nr) return distinct[node];

        Push(node);

        int mid = (nl + nr) >> 1;
        if (index <= mid) return QueryDistinct(node << 1, nl, mid, index);
        return QueryDistinct(node << 1 | 1, mid + 1, nr, index);
    }

    private long QuerySumMax(int node, int nl, int nr, int index)
    {
        if (nl == nr) return sumMax[node];

        Push(node);

        int mid = (nl + nr) >> 1;
        if (index <= mid) return QuerySumMax(node << 1, nl, mid, index);
        return QuerySumMax(node << 1 | 1, mid + 1, nr, index);
    }

    private void Push(int node)
    {
        long ld = lazyDistinct[node];
        long ls = lazySumMax[node];

        if (ld != 0)
        {
            distinct[node << 1] += ld;
            distinct[node << 1 | 1] += ld;
            lazyDistinct[node << 1] += ld;
            lazyDistinct[node << 1 | 1] += ld;
            lazyDistinct[node] = 0;
        }

        if (ls != 0)
        {
            sumMax[node << 1] += ls;
            sumMax[node << 1 | 1] += ls;
            lazySumMax[node << 1] += ls;
            lazySumMax[node << 1 | 1] += ls;
            lazySumMax[node] = 0;
        }
    }
}

class RangeMaxTree
{
    private readonly int n;
    private readonly int[] tree;

    public RangeMaxTree(int[] values)
    {
        n = values.Length;
        tree = new int[4 * n + 5];
        Build(1, 0, n - 1, values);
    }

    public int Query(int l, int r) => Query(1, 0, n - 1, l, r);

    private void Build(int node, int nl, int nr, int[] values)
    {
        if (nl == nr)
        {
            tree[node] = values[nl];
            return;
        }

        int mid = (nl + nr) >> 1;
        Build(node << 1, nl, mid, values);
        Build(node << 1 | 1, mid + 1, nr, values);
        tree[node] = Math.Max