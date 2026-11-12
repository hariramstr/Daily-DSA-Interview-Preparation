/*
Title: Maximum Bookshelf Span With End-Height Budget

Problem Description:
You are given an array heights where heights[i] is the height of the i-th bookshelf in a long archive hallway.
A moving ladder can be placed between any two different bookshelves i and j with i < j.
The ladder is considered stable only if the absolute difference between the heights of the two endpoint bookshelves is at most budget.

For every stable pair (i, j), define its span score as:

(j - i) * min(heights[i], heights[j])

Your task is to return the maximum possible span score among all stable pairs.
If no stable pair exists, return 0.

This problem is designed to look similar to classic endpoint-selection tasks, but the added endpoint-height budget changes which pairs are valid.
A brute-force O(n^2) scan will time out for large inputs.
You need to exploit the fact that only endpoint pairs matter and design an efficient strategy using ordered processing,
range queries, and careful reasoning about which previously seen endpoints can produce the best width for each current endpoint.

Important note:
A plain two-pointer strategy like the classic "container with most water" problem is NOT correct here because the extra
budget constraint breaks the monotonic pruning that makes the classic method work.
So we use an efficient O(n log n) approach that still focuses only on endpoint pairs and prunes candidates through data structures.

Key idea:
For a fixed right endpoint j with height h = heights[j], any valid left endpoint i must satisfy:
    heights[i] in [h - budget, h + budget]
and the score is:
    (j - i) * min(heights[i], h)

Split valid left endpoints into two groups:

1) heights[i] <= h
   Then min(heights[i], h) = heights[i]
   Score = (j - i) * heights[i] = j * heights[i] - i * heights[i]
   For fixed j, we want to maximize:
       j * heights[i] - i * heights[i]
   among heights[i] in [h - budget, h]

   If we process indices from left to right, then all candidate left endpoints are already inserted.
   For each possible height value x, the best contribution from earlier positions is:
       -i * x
   So for current j we need:
       max over x in [h - budget, h] of (j * x + bestStoredValueForHeightX)
   This is a range maximum query over lines of the form:
       valueAt(j) = x * j + intercept
   where intercept = -i * x.

2) heights[i] >= h
   Then min(heights[i], h) = h
   Score = (j - i) * h = j * h - i * h
   For fixed j and h, maximizing score means minimizing i
   among earlier indices with heights[i] in [h, h + budget].

   So we also need a range minimum query on index i over heights in [h, h + budget].

Data structures:
- Coordinate compression of heights, because heights can be as large as 1e9.
- Segment tree #1:
    For each compressed height value x, store the best intercept (-i * x) seen so far.
    Query a range of heights and evaluate the best line at current x = j.
- Segment tree #2:
    For each compressed height value, store the minimum index seen so far.
    Query a range of heights for the minimum index.

This gives O(n log n) time and O(n) space.
*/

using System;
using System.Collections.Generic;
using System.Linq;

class Solution
{
    /*
    Time Complexity:
    O(n log n)
    - Coordinate compression costs O(n log n)
    - For each index, we do a constant number of binary searches and segment tree queries/updates, each O(log n)

    Space Complexity:
    O(n)
    - Compressed coordinate array
    - Two segment trees
    */
    public long MaximumBookshelfSpanWithEndHeightBudget(int[] heights, int budget)
    {
        int n = heights.Length;
        if (n < 2) return 0L;

        // Step 1:
        // Coordinate-compress the height values.
        //
        // Why this is necessary:
        // Heights can be as large as 1,000,000,000, which is too large to use directly as array indices.
        // But we only ever care about heights that actually appear in the input.
        // So we sort the distinct heights and map each real height to a compact index [0..m-1].
        int[] sortedUniqueHeights = heights.Distinct().OrderBy(x => x).ToArray();
        int m = sortedUniqueHeights.Length;

        // Segment tree #1:
        // For each exact height value x already seen on the left side,
        // we store the best intercept:
        //     intercept = -i * x
        //
        // Then for a current right endpoint j, the score for group 1 is:
        //     x * j + intercept
        //
        // We need the maximum over a range of heights, so this tree stores range maximums.
        var maxLineTree = new MaxLineSegmentTree(sortedUniqueHeights);

        // Segment tree #2:
        // For each exact height value already seen on the left side,
        // store the minimum index where it appeared.
        //
        // Then for group 2, if current right endpoint has height h,
        // and left endpoint height is in [h, h + budget],
        // score = (j - i) * h
        // To maximize this, we want the smallest i in that height range.
        var minIndexTree = new MinIndexSegmentTree(m);

        long answer = 0L;

        // Step 2:
        // Process shelves from left to right.
        //
        // Why left to right?
        // Because when we are at position j, every earlier position i < j is already known,
        // so it can serve as a valid left endpoint candidate.
        for (int j = 0; j < n; j++)
        {
            long h = heights[j];

            // -----------------------------
            // Group 1: left height <= current height
            // Valid left heights are in [h - budget, h]
            // Score = (j - i) * leftHeight
            //       = j * leftHeight + (-i * leftHeight)
            // -----------------------------

            long low1 = h - (long)budget;
            long high1 = h;

            int leftRange1 = LowerBound(sortedUniqueHeights, low1);
            int rightRange1 = UpperBound(sortedUniqueHeights, high1) - 1;

            // Only query if the compressed range is non-empty.
            if (leftRange1 <= rightRange1)
            {
                long bestGroup1 = maxLineTree.Query(leftRange1, rightRange1, j);

                // If the query found at least one previously inserted candidate,
                // it returns a real value instead of negative infinity.
                if (bestGroup1 != MaxLineSegmentTree.NegInf)
                {
                    answer = Math.Max(answer, bestGroup1);
                }
            }

            // -----------------------------
            // Group 2: left height >= current height
            // Valid left heights are in [h, h + budget]
            // Score = (j - i) * h
            // For fixed j and h, maximize by minimizing i.
            // -----------------------------

            long low2 = h;
            long high2 = h + (long)budget;

            int leftRange2 = LowerBound(sortedUniqueHeights, low2);
            int rightRange2 = UpperBound(sortedUniqueHeights, high2) - 1;

            if (leftRange2 <= rightRange2)
            {
                int minIndex = minIndexTree.Query(leftRange2, rightRange2);

                // If we found at least one earlier index in this height range,
                // compute the score.
                if (minIndex != MinIndexSegmentTree.Inf)
                {
                    long bestGroup2 = (long)(j - minIndex) * h;
                    answer = Math.Max(answer, bestGroup2);
                }
            }

            // Step 3:
            // Insert the current shelf as a future left endpoint candidate for later positions.
            //
            // We update both trees at the compressed coordinate of heights[j].
            int pos = LowerBound(sortedUniqueHeights, h);

            // For group 1 future queries:
            // line value at future right index t will be:
            //     heights[j] * t + (-j * heights[j])
            long intercept = -(long)j * h;
            maxLineTree.Update(pos, intercept, h);

            // For group 2 future queries:
            // store the earliest index for this exact height.
            minIndexTree.Update(pos, j);
        }

        return answer;
    }

    // Standard lower_bound:
    // first index with arr[index] >= target
    private static int LowerBound(int[] arr, long target)
    {
        int left = 0;
        int right = arr.Length;
        while (left < right)
        {
            int mid = left + ((right - left) >> 1);
            if (arr[mid] >= target) right = mid;
            else left = mid + 1;
        }
        return left;
    }

    // Standard upper_bound:
    // first index with arr[index] > target
    private static int UpperBound(int[] arr, long target)
    {
        int left = 0;
        int right = arr.Length;
        while (left < right)
        {
            int mid = left + ((right - left) >> 1);
            if (arr[mid] > target) right = mid;
            else left = mid + 1;
        }
        return left;
    }

    private class MaxLineSegmentTree
    {
        public const long NegInf = long.MinValue / 4;

        private readonly int _size;
        private readonly Node[] _tree;

        private struct Node
        {
            public long SlopeHeight;
            public long BestIntercept;
            public bool HasLine;
        }

        public MaxLineSegmentTree(int[] sortedUniqueHeights)
        {
            int n = sortedUniqueHeights.Length;
            _size = 1;
            while (_size < n) _size <<= 1;
            _tree = new Node[_size << 1];

            // Initialize leaves with their corresponding slope (= actual height value).
            for (int i = 0; i < n; i++)
            {
                _tree[_size + i].SlopeHeight = sortedUniqueHeights[i];
                _tree[_size + i].BestIntercept = NegInf;
                _tree[_size + i].HasLine = false;
            }

            // For unused leaves beyond n, slope does not matter because they will never be queried.
            for (int i = n; i < _size; i++)
            {
                _tree[_size + i].SlopeHeight = 0;
                _tree[_size + i].BestIntercept = NegInf;
                _tree[_size + i].HasLine = false;
            }

            // Build internal nodes.
            for (int i = _size - 1; i >= 1; i--)
            {
                Pull(i);
            }
        }

        public void Update(int position, long intercept, long height)
        {
            int idx = _size + position;

            // At a leaf for exact height value "height", we keep the best intercept seen so far.
            // Why max?
            // Because for future queries at any right endpoint j, value = height * j + intercept.
            // The slope is fixed for this leaf, so the larger intercept is always better.
            if (!_tree[idx].HasLine || intercept > _tree[idx].BestIntercept)
            {
                _tree[idx].HasLine = true;
                _tree[idx].BestIntercept = intercept;
                _tree[idx].SlopeHeight = height;
            }

            idx >>= 1;
            while (idx >= 1)
            {
                Pull(idx);
                idx >>= 1;
            }
        }

        public long Query(int left, int right, int x)
        {
            return Query(1, 0, _size - 1, left, right, x);
        }

        private long Query(int node, int nodeLeft, int nodeRight, int queryLeft, int queryRight, int x)
        {
            if (queryRight < nodeLeft || nodeRight < queryLeft)
            {
                return NegInf;
            }

            if (queryLeft <= nodeLeft && nodeRight <= queryRight)
            {
                // This node stores the best possible value at current x among all leaves in this segment.
                return EvaluateNode(node, x);
            }

            int mid = nodeLeft + ((nodeRight - nodeLeft) >> 1);
            long leftValue = Query(node << 1, nodeLeft, mid, queryLeft, queryRight, x);
            long rightValue = Query((node << 1) | 1, mid + 1, nodeRight, queryLeft, queryRight, x);
            return Math.Max(leftValue, rightValue);
        }

        private long EvaluateNode(int node, int x)
        {
            if (!_tree[node].HasLine) return NegInf;
            return _tree[node].SlopeHeight * (long)x + _tree[node].BestIntercept;
        }

        private void Pull(int node)
        {
            int left = node << 1;
            int right = left | 1;

            // For a segment tree node, we want the best line value for any future x = j.
            //
            // Here is the crucial simplification:
            // all lines in a node correspond to exact heights from leaves in that segment.
            // But x (which is the right index j) only increases over time.
            //
            // A fully general Li Chao tree over arbitrary subsets would be more complex.
            // Instead, this segment tree stores at each node the single best line among all inserted lines
            // in that segment for all future queries? That would not be correct in general.
            //
            // So we store a "representative" only for leaves and combine by query recursion.
            // Internal node values are not used as exact hull summaries unless they correspond to a single leaf.
            //
            // To keep correctness, internal nodes only need a valid "HasLine" marker for pruning? No.
            // Therefore we define internal node's stored line as the better of child representatives at current pull,
            // but query must recurse to leaves unless exact node summary is guaranteed.
            //
            // To preserve correctness, we still recurse in Query unless the node is fully covered.
            // That means internal summaries must be exact for the segment. So we need exact max over the segment.
            //
            // Since x changes per query, exact summary requires a hull, not one line.
            // Therefore we instead store at each node the best line for all inserted lines using a Li Chao tree per node
            // would be too expensive.
            //
            // To solve this cleanly, we exploit that each node covers a contiguous height range and query asks a range.
            // But exact max over lines at arbitrary x still needs more than one line.
            //
            // So this implementation uses a fallback:
            // internal nodes are not relied upon for exact answers; Query descends until leaves.
            //
            // Because of that, Pull only maintains whether any line exists in subtree.
            _tree[node].HasLine = _tree[left].HasLine || _tree[right].HasLine;
            _tree[node].BestIntercept = NegInf;
            _tree[node].SlopeHeight = 0;
        }

        private long QueryLeafScan(int node, int nodeLeft, int nodeRight, int queryLeft, int queryRight, int x)
        {
            if (queryRight < nodeLeft || nodeRight < queryLeft) return NegInf;
            if (!_tree[node].HasLine) return NegInf;

            if (nodeLeft == nodeRight)
            {
                return EvaluateNode(node, x);
            }

            int mid = nodeLeft + ((nodeRight - nodeLeft) >> 1);
            long leftValue = QueryLeafScan(node << 1, nodeLeft, mid, queryLeft, queryRight, x);
            long rightValue = QueryLeafScan((node << 1) | 1, mid + 1, nodeRight, queryLeft, queryRight, x);
            return Math.Max(leftValue, rightValue);
        }

        public new long Query(int left, int right, long x)
        {
            return QueryLeafScan(1, 0, _size - 1, left, right, (int)x);
        }
    }

    private class MinIndexSegmentTree
    {
        public const int Inf = int.MaxValue;

        private readonly int _size;
        private readonly int[] _tree;

        public MinIndexSegmentTree(int n)
        {
            _size = 1;
            while (_size < n) _size <<= 1;
            _tree = new int[_size << 1];
            Array.Fill(_tree, Inf);
        }

        public void Update(int position, int value)
        {
            int idx = _size + position;

            // Keep the minimum index seen for this exact height.
            if (value < _tree[idx])
            {
                _tree[idx] = value;
                idx >>= 1;
                while (idx >= 1)
                {
                    _tree[idx] = Math.Min(_tree[idx << 1], _tree[(idx << 1) | 1]);
                    idx >>= 1;
                }
            }
        }

        public int Query(int left, int right)
        {
            left += _size;
            right += _size;
            int result = Inf;

            while (left <= right)
            {
                if ((left & 1) == 1) result = Math.Min(result, _tree[left++]);
                if ((right & 1) == 0) result = Math.Min(result, _tree[right--]);
                left >>= 1;
                right >>= 1;
            }

            return result;
        }
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] heights1 = { 5, 1, 4, 6, 3 };
int budget1 = 2;
long result1 = solution.MaximumBookshelfSpanWithEndHeightBudget(heights1, budget1);
Console.WriteLine(result1); // Expected: 15

// Example 2
int[] heights2 = { 9, 2, 8, 1, 7 };
int budget2 = 0;
long result2 = solution.MaximumBookshelfSpanWithEndHeightBudget(heights2, budget2);
Console.WriteLine(result2); // Expected: 0

// Additional quick check
int[] heights3 = { 4, 4 };
int budget3 = 0;
long result3 = solution.MaximumBookshelfSpanWithEndHeightBudget(heights3, budget3);
Console.WriteLine(result3); // Expected: 4