/*
Title: Minimum Chargers for Deadline-Constrained Drone Deliveries

Problem Description:
A company operates a fleet of identical drones from a single depot. Each delivery request is described by three integers:
start[i], end[i], and charge[i].

The drone assigned to request i must occupy one charging dock continuously from time start[i] until time end[i]
(inclusive of start, exclusive of end), and the dock must provide at least charge[i] units of charging capacity while
that request is active.

A charging dock can serve at most one drone at a time, but its installed capacity is fixed for the entire day and may
be reused by multiple non-overlapping requests.

You may install any number of docks. The cost of a dock equals its capacity. Your goal is to schedule all requests and
choose dock capacities so that every request is assigned to some dock whose capacity is at least the request's required
charge, while minimizing the total installation cost across all docks.

Return the minimum possible total cost.

Key insight:
This is equivalent to partitioning intervals into non-overlapping chains (each chain is one dock), and the cost of a
chain is the maximum charge among all requests assigned to that dock. We want to minimize the sum of chain maxima.

A greedy strategy works:
- Process requests in increasing start time.
- Keep track of docks currently busy, ordered by end time.
- When a dock becomes free, it moves into a pool of available reusable docks, identified only by its fixed capacity.
- For a new request with required charge c:
  * If there exists a free dock with capacity >= c, reuse the smallest such capacity.
    This is optimal because it preserves larger-capacity docks for future larger requests.
  * Otherwise, install a new dock of capacity c.

This greedy is optimal by an exchange argument and is the same structure as interval partitioning with weighted reusable
resources.

Example 1:
requests = [[1,4,5],[2,6,3],[4,7,5]]
Output: 8

Example 2:
requests = [[1,5,8],[2,3,2],[3,6,6],[5,8,2]]
Output: 10
*/

using System;
using System.Collections.Generic;

public class Solution
{
    private readonly struct Request
    {
        public readonly int Start;
        public readonly int End;
        public readonly int Charge;

        public Request(int start, int end, int charge)
        {
            Start = start;
            End = end;
            Charge = charge;
        }
    }

    private readonly struct BusyDock
    {
        public readonly int End;
        public readonly int Capacity;

        public BusyDock(int end, int capacity)
        {
            End = end;
            Capacity = capacity;
        }
    }

    private sealed class BusyDockComparer : IComparer<BusyDock>
    {
        public int Compare(BusyDock x, BusyDock y)
        {
            int cmp = x.End.CompareTo(y.End);
            if (cmp != 0) return cmp;
            return x.Capacity.CompareTo(y.Capacity);
        }
    }

    private sealed class IntComparer : IComparer<int>
    {
        public int Compare(int x, int y) => x.CompareTo(y);
    }

    // Time Complexity:
    // - Sorting requests: O(n log n)
    // - Each request is inserted/removed from the busy heap once: O(n log n)
    // - Each dock capacity is inserted/removed from the available multiset once: O(n log n)
    // Overall: O(n log n)
    //
    // Space Complexity:
    // - Requests array: O(n)
    // - Busy heap + available capacities multiset: O(n)
    // Overall: O(n)
    public long MinimumTotalCost(int[][] requests)
    {
        int n = requests.Length;

        // Convert the raw input into a strongly-typed array.
        // This makes the code easier to read and avoids repeated indexing like requests[i][0].
        var arr = new Request[n];
        for (int i = 0; i < n; i++)
        {
            arr[i] = new Request(requests[i][0], requests[i][1], requests[i][2]);
        }

        // Step 1: Sort all requests by start time.
        //
        // Why?
        // We want to process time from left to right.
        // Before handling a request that starts at time S, we must know which docks have already become free by time S.
        //
        // Tie-breaking:
        // If two requests have the same start time, processing smaller end first is harmless.
        // We also tie-break by charge for determinism.
        Array.Sort(arr, (a, b) =>
        {
            int cmp = a.Start.CompareTo(b.Start);
            if (cmp != 0) return cmp;
            cmp = a.End.CompareTo(b.End);
            if (cmp != 0) return cmp;
            return a.Charge.CompareTo(b.Charge);
        });

        // Step 2: Maintain all currently occupied docks in a min-heap ordered by end time.
        //
        // Each heap entry represents one dock that is currently serving some request.
        // We only need:
        // - when it becomes free (End)
        // - what its fixed installed capacity is (Capacity)
        //
        // As soon as End <= current request's Start, that dock is reusable.
        var busy = new PriorityQueue<BusyDock, BusyDock>(new BusyDockComparer());

        // Step 3: Maintain all currently free reusable docks by capacity.
        //
        // We need to answer:
        // "What is the smallest available dock capacity that is >= required charge c?"
        //
        // In C#, SortedDictionary gives us sorted keys, but not direct lower_bound.
        // SortedSet also lacks a direct lower_bound method in a simple way.
        //
        // So we implement a classic coordinate-compressed segment tree over all charge values that appear in requests.
        // The tree stores, for each compressed capacity index, how many free docks of that exact capacity exist.
        //
        // Then for a request with charge c, we binary-search the first compressed index >= c,
        // and ask the segment tree for the leftmost index in [thatIndex .. end] whose count > 0.
        //
        // This gives us the smallest reusable dock that can satisfy the request.
        int[] allCharges = new int[n];
        for (int i = 0; i < n; i++) allCharges[i] = arr[i].Charge;
        Array.Sort(allCharges);
        int m = UniqueInPlace(allCharges);

        var counts = new int[m];
        var segTree = new SegmentTree(m);

        long totalCost = 0;

        // Step 4: Process each request in chronological order.
        foreach (var req in arr)
        {
            // 4a. Release every busy dock that has already finished by req.Start.
            //
            // Important interval rule:
            // Requests are active on [start, end), so if one ends at time T and another starts at time T,
            // they do NOT overlap and can use the same dock.
            //
            // Therefore we release while End <= Start.
            while (busy.Count > 0 && busy.Peek().End <= req.Start)
            {
                BusyDock freed = busy.Dequeue();

                // This dock is now available for reuse.
                // We add one free dock of this capacity into our multiset.
                int idx = LowerBound(allCharges, m, freed.Capacity);
                counts[idx]++;
                segTree.Add(idx, 1);
            }

            // 4b. Try to reuse an existing free dock.
            //
            // We need the smallest capacity >= req.Charge.
            // Why the smallest?
            // Because using a larger dock when a smaller sufficient dock exists can only reduce flexibility later.
            // Keeping larger capacities available for future larger requests is always at least as good.
            int firstPossible = LowerBound(allCharges, m, req.Charge);
            int chosenIdx = -1;

            if (firstPossible < m)
            {
                chosenIdx = segTree.FindFirstPositive(firstPossible);
            }

            int assignedCapacity;

            if (chosenIdx != -1)
            {
                // Reuse an existing dock.
                assignedCapacity = allCharges[chosenIdx];

                // Remove one free dock of that capacity from the multiset,
                // because it becomes busy again immediately.
                counts[chosenIdx]--;
                segTree.Add(chosenIdx, -1);
            }
            else
            {
                // No reusable dock can satisfy this request.
                // Therefore we must install a brand-new dock with exactly the required capacity.
                //
                // Why exactly req.Charge and not larger?
                // A larger new dock would cost more immediately and cannot improve feasibility for the current request.
                // Any future benefit is never worth paying extra now, because if a larger dock is ever needed later,
                // that later request can trigger its own installation then.
                assignedCapacity = req.Charge;
                totalCost += assignedCapacity;
            }

            // 4c. Mark the assigned dock as busy until req.End.
            busy.Enqueue(new BusyDock(req.End, assignedCapacity), new BusyDock(req.End, assignedCapacity));
        }

        return totalCost;
    }

    private static int UniqueInPlace(int[] arr)
    {
        if (arr.Length == 0) return 0;
        int write = 1;
        for (int read = 1; read < arr.Length; read++)
        {
            if (arr[read] != arr[write - 1])
            {
                arr[write++] = arr[read];
            }
        }
        return write;
    }

    private static int LowerBound(int[] arr, int length, int target)
    {
        int lo = 0;
        int hi = length;
        while (lo < hi)
        {
            int mid = lo + ((hi - lo) >> 1);
            if (arr[mid] < target) lo = mid + 1;
            else hi = mid;
        }
        return lo;
    }

    private sealed class SegmentTree
    {
        private readonly int _size;
        private readonly int[] _tree;

        public SegmentTree(int n)
        {
            _size = 1;
            while (_size < n) _size <<= 1;
            _tree = new int[_size << 1];
        }

        public void Add(int index, int delta)
        {
            int pos = index + _size;
            _tree[pos] += delta;
            pos >>= 1;
            while (pos > 0)
            {
                _tree[pos] = _tree[pos << 1] + _tree[(pos << 1) | 1];
                pos >>= 1;
            }
        }

        public int FindFirstPositive(int leftBound)
        {
            return FindFirstPositive(1, 0, _size - 1, leftBound);
        }

        private int FindFirstPositive(int node, int left, int right, int leftBound)
        {
            if (right < leftBound || _tree[node] == 0) return -1;
            if (left == right) return left;

            int mid = left + ((right - left) >> 1);

            int leftResult = FindFirstPositive(node << 1, left, mid, leftBound);
            if (leftResult != -1) return leftResult;

            return FindFirstPositive((node << 1) | 1, mid + 1, right, leftBound);
        }
    }
}

// Demo code
var solution = new Solution();

int[][] requests1 =
{
    new[] { 1, 4, 5 },
    new[] { 2, 6, 3 },
    new[] { 4, 7, 5 }
};

int[][] requests2 =
{
    new[] { 1, 5, 8 },
    new[] { 2, 3, 2 },
    new[] { 3, 6, 6 },
    new[] { 5, 8, 2 }
};

Console.WriteLine(solution.MinimumTotalCost(requests1)); // Expected: 8
Console.WriteLine(solution.MinimumTotalCost(requests2)); // Expected: 10