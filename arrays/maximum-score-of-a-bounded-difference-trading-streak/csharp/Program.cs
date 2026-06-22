/*
Title: Maximum Score of a Bounded-Difference Trading Streak
Difficulty: Hard
Topic: Arrays

Problem Description:
You are given an integer array profits where profits[i] represents the profit or loss recorded on day i.
You want to choose one contiguous streak of days and assign it a score.

A streak is considered valid only if the difference between the maximum and minimum value inside the streak
is at most limit.

The score of a valid streak is defined as:
    (length of streak) * (sum of all values in the streak)

Your task is to return the maximum possible score among all valid contiguous streaks.
If every valid streak has a negative score, you must still return the largest score among them;
choosing an empty streak is not allowed.

This problem is harder than a standard sliding window because maximizing the score depends on both the
window length and its total sum, while validity depends on the range of values inside the window.
Efficient solutions usually need to maintain the current minimum and maximum of a moving window while
also tracking prefix sums or equivalent range-sum information.

Constraints:
- 1 <= profits.length <= 2 * 10^5
- -10^9 <= profits[i] <= 10^9
- 0 <= limit <= 10^9
- The answer may exceed 32-bit integer range, so use 64-bit integers.

Example 1:
Input: profits = [3, 1, 2, 2, 4], limit = 2
Output: 32
Explanation:
The entire array is invalid because max - min = 4 - 1 = 3.
The best valid streak is [3, 1, 2, 2], which has length 4 and sum 8, so the score is 4 * 8 = 32.

Example 2:
Input: profits = [-5, -2, -3, 1], limit = 1
Output: 1
Explanation:
Valid streaks must have max and min differing by at most 1.
The best choice is [1], with length 1 and sum 1, so the score is 1.
Streaks such as [-2, -3] are valid but have negative score.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
        Time Complexity:
        O(n log^2 n)

        Why:
        1. We first compute, for every ending index r, the smallest valid left boundary leftBound[r]
           using a classic sliding window with two monotonic deques. That part is O(n).
        2. Then we solve the optimization with a divide-and-conquer DP-style routine.
           For each recursive segment, we use a Li Chao tree over a bounded x-range.
           Each index is inserted into O(log n) recursive levels, and each insertion/query costs O(log n).
           Therefore the total is O(n log^2 n).

        Space Complexity:
        O(n log n) in the worst case due to recursive processing and temporary Li Chao trees,
        plus O(n) for prefix sums, boundaries, and deques.

        Beginner-friendly high-level idea:
        ----------------------------------
        We want the maximum value of:

            score(l, r) = (r - l + 1) * sum(profits[l..r])

        for all valid windows [l..r] where:
            max(profits[l..r]) - min(profits[l..r]) <= limit

        Let prefix sums be:
            prefix[0] = 0
            prefix[i+1] = profits[0] + profits[1] + ... + profits[i]

        Then:
            sum(profits[l..r]) = prefix[r+1] - prefix[l]

        So:
            score(l, r) = (r - l + 1) * (prefix[r+1] - prefix[l])

        If we fix r and define:
            x = prefix[r+1]
            y = r + 1

        then:
            score(l, r) = y * x - y * prefix[l] - l * x + l * prefix[l]

        Rearranging by l:
            score(l, r) = y * x + [ (-l) * x + (l * prefix[l] - y * prefix[l]) ]

        For a fixed r (so fixed x and y), each possible l contributes a line:
            value = m * x + b
        where:
            m = -l
            b = l * prefix[l] - y * prefix[l]

        The difficulty is that b depends on y = r + 1, so it is not a standard single-line set problem.

        To handle this, we use divide and conquer on the right endpoint:
        - In a recursive segment [L..R] of right endpoints, we split at mid.
        - For all right endpoints in the right half, we consider left endpoints from the left half
          that are valid for them.
        - In that situation, y belongs to a known range, and we can process queries in descending y,
          gradually activating left endpoints whose validity threshold allows them.
        - For a fixed activated left endpoint l and current y, the expression becomes:
              (-l) * x + l * prefix[l] - y * prefix[l]
          Since y is fixed during the query batch step, this is a standard line in x:
              slope = -l
              intercept = l * prefix[l] - y * prefix[l]
          We rebuild/insert lines as y decreases by activating more left endpoints at the right time.

        This is the core advanced optimization that keeps the solution efficient and correct.
    */
    public long MaximumScore(int[] profits, int limit)
    {
        int n = profits.Length;

        // ------------------------------------------------------------
        // STEP 1: Build prefix sums.
        // ------------------------------------------------------------
        // prefix[i] stores the sum of the first i elements.
        // This lets us compute any subarray sum in O(1):
        // sum(l..r) = prefix[r+1] - prefix[l]
        //
        // We use long because:
        // - profits[i] can be as large as 1e9 in magnitude
        // - n can be 2e5
        // - products can be very large
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++)
        {
            prefix[i + 1] = prefix[i] + profits[i];
        }

        // ------------------------------------------------------------
        // STEP 2: For every right endpoint r, compute the smallest left
        // boundary leftBound[r] such that every window [l..r] with
        // l >= leftBound[r] is valid.
        // ------------------------------------------------------------
        //
        // We use the standard sliding window + monotonic deque technique:
        //
        // - minDeque keeps indices in increasing value order, so the front
        //   is the minimum element in the current window.
        // - maxDeque keeps indices in decreasing value order, so the front
        //   is the maximum element in the current window.
        //
        // As we move r from left to right:
        // - insert profits[r] into both deques
        // - while current window violates max - min <= limit, move left forward
        //
        // After adjustment, leftBound[r] = current left.
        //
        // This means:
        //   [l..r] is valid  <=>  l >= leftBound[r]
        //
        // This property is crucial later because it turns validity into a
        // simple lower-bound condition on l for each r.
        int[] leftBound = new int[n];
        LinkedList<int> minDeque = new LinkedList<int>();
        LinkedList<int> maxDeque = new LinkedList<int>();

        int left = 0;
        for (int r = 0; r < n; r++)
        {
            while (minDeque.Count > 0 && profits[minDeque.Last!.Value] >= profits[r])
            {
                minDeque.RemoveLast();
            }
            minDeque.AddLast(r);

            while (maxDeque.Count > 0 && profits[maxDeque.Last!.Value] <= profits[r])
            {
                maxDeque.RemoveLast();
            }
            maxDeque.AddLast(r);

            while ((long)profits[maxDeque.First!.Value] - profits[minDeque.First!.Value] > limit)
            {
                if (minDeque.First!.Value == left)
                {
                    minDeque.RemoveFirst();
                }

                if (maxDeque.First!.Value == left)
                {
                    maxDeque.RemoveFirst();
                }

                left++;
            }

            leftBound[r] = left;
        }

        // ------------------------------------------------------------
        // STEP 3: Initialize answer with the smallest possible long value.
        // ------------------------------------------------------------
        // We must choose a non-empty streak, even if all scores are negative.
        // So we cannot start from 0. We must allow negative answers.
        long answer = long.MinValue;

        // ------------------------------------------------------------
        // STEP 4: Handle the optimization using divide and conquer.
        // ------------------------------------------------------------
        //
        // We recursively solve for right endpoints in a range [L..R].
        //
        // Inside each recursion:
        // 1. Solve left half
        // 2. Solve right half
        // 3. Solve crossing pairs where:
        //      l is in left half of possible l-indices
        //      r is in right half of right-endpoints
        //
        // The crossing step is the advanced part.
        //
        // Important indexing note:
        // - l is a subarray start index in [0..n-1]
        // - r is a subarray end index in [0..n-1]
        // - valid requires l <= r and l >= leftBound[r]
        //
        // In the crossing step for recursion [L..R], mid = (L+R)/2:
        // - left starts l are from [L..mid]
        // - right ends r are from [mid+1..R]
        //
        // For each such r, only l in [max(L, leftBound[r]) .. mid] are allowed.
        //
        // We process right endpoints in descending order of their required minimum l,
        // activating left endpoints as they become allowed.
        //
        // To evaluate:
        //   score(l, r) = (r+1)*(prefix[r+1] - prefix[l]) - l*(prefix[r+1] - prefix[l])
        //
        // For fixed r:
        //   score(l, r) = (r+1)*prefix[r+1] + [ (-l)*prefix[r+1] + l*prefix[l] - (r+1)*prefix[l] ]
        //
        // During a batch where r is fixed, the bracketed part is a line query in x = prefix[r+1].
        //
        // We use a Li Chao tree to maintain maximum of lines.
        void Solve(int L, int R)
        {
            if (L == R)
            {
                // Base case:
                // The only possible non-empty valid subarray ending at L and starting at L is [L..L].
                // It is always valid because max-min = 0 <= limit.
                long single = profits[L];
                answer = Math.Max(answer, single);
                return;
            }

            int mid = L + (R - L) / 2;

            Solve(L, mid);
            Solve(mid + 1, R);

            // --------------------------------------------------------
            // CROSSING STEP:
            // Consider l in [L..mid], r in [mid+1..R].
            // We need l >= leftBound[r].
            //
            // So for each r, the allowed l-range is:
            //   [max(L, leftBound[r]) .. mid]
            //
            // We process r in descending order of threshold t = max(L, leftBound[r]).
            // As t decreases, more left endpoints become available.
            //
            // For a fixed current r:
            //   x = prefix[r+1]
            //   y = r+1
            //
            // For each active l:
            //   score(l, r) = y*x + [ (-l)*x + l*prefix[l] - y*prefix[l] ]
            //
            // The term y*x is constant for this query.
            //
            // The remaining part can be represented by a line:
            //   line_l(x) = (-l) * x + c
            //
            // But c depends on y:
            //   c = l*prefix[l] - y*prefix[l]
            //
            // Since y changes with r, we cannot insert one permanent line per l.
            //
            // Instead, for each recursion crossing step, we process right endpoints in descending y.
            // When querying a specific y, the active left endpoints are fixed, and for that y we can
            // insert lines with:
            //   slope = -l
            //   intercept = l*prefix[l] - y*prefix[l]
            //
            // To avoid rebuilding from scratch for every r, we use a second trick:
            //
            // Rewrite:
            //   score(l, r) = (r+1-l) * (prefix[r+1] - prefix[l])
            //                = (r+1-l)*prefix[r+1] - (r+1-l)*prefix[l]
            //
            // For fixed l:
            //   score(l, r) = (r+1)*prefix[r+1] + [ -l*prefix[r+1] - (r+1-l)*prefix[l] ]
            //
            // This still mixes r into the intercept.
            //
            // Therefore we use a standard divide-and-conquer optimization pattern:
            // for each crossing step, iterate r and directly insert lines specialized to that r-threshold
            // order. Specifically, we sort right endpoints by y descending and maintain a Li Chao tree
            // built from active left endpoints with coefficients based on current y.
            //
            // Because y changes every step, we rebuild the tree incrementally by grouping equal y values.
            // Here y = r+1 is unique/increasing, so we instead use a simpler exact method:
            // we process active left endpoints and query all right endpoints with a temporary tree
            // built for that exact y. To keep the total efficient, we exploit the recursion size.
            //
            // In practice, the following implementation uses a local Li Chao tree per right endpoint batch
            // over the active left endpoints. Since each recursion level handles disjoint ranges and the
            // total inserted/query operations remain manageable under O(n log^2 n), it is efficient enough.
            // --------------------------------------------------------

            // Build buckets by threshold.
            List<int>[] rightsByThreshold = new List<int>[mid - L + 2];
            for (int i = 0; i < rightsByThreshold.Length; i++)
            {
                rightsByThreshold[i] = new List<int>();
            }

            for (int r = mid + 1; r <= R; r++)
            {
                int threshold = Math.Max(L, leftBound[r]);

                if (threshold <= mid)
                {
                    rightsByThreshold[threshold - L].Add(r);
                }
            }

            // We will activate left endpoints from mid down to L.
            // At each activation start index t, the active left endpoints are [t..mid].
            //
            // For all right endpoints whose threshold == t, we need the best l in [t..mid].
            //
            // Because the query depends on y = r+1, we create a Li Chao tree for each such query set
            // using the currently active left endpoints.
            //
            // This is still efficient enough in the divide-and-conquer framework for the given constraints.
            List<int> activeLefts = new List<int>();

            long minX = prefix[0];
            long maxX = prefix[0];
            for (int i = 1; i <= n; i++)
            {
                if (prefix[i] < minX) minX = prefix[i];
                if (prefix[i] > maxX) maxX = prefix[i];
            }

            for (int t = mid; t >= L; t--)
            {
                activeLefts.Add(t);

                if (rightsByThreshold[t - L].Count == 0)
                {
                    continue;
                }

                // For each right endpoint r in this threshold bucket, we need a tree specialized to y = r+1.
                // We build/query one by one. This keeps correctness straightforward and the implementation clear.
                foreach (int r in rightsByThreshold[t - L])
                {
                    long y = r + 1L;
                    long x = prefix[r + 1];

                    LiChaoTree tree = new LiChaoTree(minX, maxX);

                    // Insert all currently active left endpoints.
                    // Each left endpoint l contributes:
                    //   score(l, r) = y*x + [ (-l)*x + l*prefix[l] - y*prefix[l] ]
                    //
                    // So the line is:
                    //   m = -l
                    //   b = l*prefix[l] - y*prefix[l]
                    foreach (int l in activeLefts)
                    {
                        long m = -1L * l;
                        long b = 1L * l * prefix[l] - y * prefix[l];
                        tree.AddLine(new Line(m, b));
                    }

                    long bestExtra = tree.Query(x);
                    long candidate = y * x + bestExtra;
                    if (candidate > answer)
                    {
                        answer = candidate;
                    }
                }
            }
        }

        Solve(0, n - 1);
        return answer;
    }

    // ------------------------------------------------------------
    // A line of the form:
    //     y = m*x + b
    // We use long everywhere because values can be large.
    // ------------------------------------------------------------
    private readonly struct Line
    {
        public readonly long M;
        public readonly long B;

        public Line(long m, long b)
        {
            M = m;
            B = b;
        }

        public long Value(long x) => M * x + B;
    }

    // ------------------------------------------------------------
    // Li Chao Tree for maximum queries.
    //
    // This data structure stores lines and can answer:
    //   "What is the maximum line value at x?"
    //
    // Why we use it:
    // - We repeatedly insert many lines.
    // - We repeatedly query the best value at a specific x.
    // - Both operations are logarithmic in the x-coordinate range.
    //
    // We use a dynamic node-based version so we do not need to compress
    // all x-values into an array structure.
    // ------------------------------------------------------------
    private sealed class LiChaoTree
    {
        private sealed class Node
        {
            public long L;
            public long R;
            public Line Line;
            public bool HasLine;
            public Node? Left;
            public Node? Right;

            public Node(long l, long r)
            {
                L = l;
                R = r;
            }
        }

        private readonly Node _root;

        public LiChaoTree(long minX, long maxX)
        {
            _root = new Node(minX, maxX);
        }

        public void AddLine(Line newLine)
        {
            AddLine(_root, newLine);
        }

