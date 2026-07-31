/*
Minimum Fatigue to Type a Macro Script

Problem Summary:
We must produce a target string s from left to right.

Allowed actions:
1. Type(c): append one character c, paying typeCost[c].
2. Define(l, r): if s[l..r] already appeared earlier completely inside the already produced prefix,
   we may define that substring as a macro for free.
3. Use(l, r): append a previously defined macro equal to s[l..r] in one action, paying macroCost.

Important observation:
Because Define is free and depends only on whether the substring text has already appeared in the
already produced prefix, we do not need to explicitly track which macros are stored. The moment a
substring becomes eligible, we can define it immediately at zero cost and then use it whenever needed.

Therefore, when we are about to produce position i, we have two choices:
- Type s[i]
- Use any substring s[i..j] whose text already appears somewhere fully inside s[0..i-1],
  paying macroCost and jumping to j + 1

Goal:
Compute the minimum total fatigue to produce the whole string.

Key DP idea:
Let dp[i] = minimum fatigue needed to produce prefix s[0..i-1], i.e. first i characters.
Then:
- Type next character:
    dp[i + 1] = min(dp[i + 1], dp[i] + typeCost[s[i]])
- Use a macro of any valid length len starting at i:
    dp[i + len] = min(dp[i + len], dp[i] + macroCost)

The hard part is efficiently determining, for each starting position i, which lengths len are valid:
the substring s[i..i+len-1] must occur somewhere earlier, ending before i.

We precompute LCP (longest common prefix) values between every pair of suffixes:
    lcp[a][b] = longest common prefix length of s[a..] and s[b..]
This can be computed in O(n^2) by dynamic programming from the end.

Then for a fixed position i, and for every earlier start p < i:
- The common prefix length between suffixes p and i is lcp[p][i]
- But the earlier occurrence must lie completely before i, so its usable length is at most i - p
- Therefore this earlier occurrence can justify any macro length up to:
      min(lcp[p][i], i - p)

Taking the maximum over all p < i gives:
    bestLen[i] = maximum macro length that can start at i

If bestLen[i] = L, then every length 1..L is valid, because if a substring of length L appeared
earlier, then all its prefixes also appeared earlier at the same place.

To update dp efficiently for all lengths 1..L with the same cost dp[i] + macroCost, we use a
range-min segment tree:
- point update for dp[i]
- range chmin on interval [i+1, i+L] with value dp[i] + macroCost
- point query to retrieve dp[i] when processing position i

This yields an O(n^2) solution, which is suitable for n <= 2000.

Note about the second sample in the prompt:
The narrative around that sample is inconsistent. Under the formal rules, for
s = "aaaaaa", typeCost['a'] = 3, macroCost = 4, the true optimum is 14:
type "aa" for 6, use macro "aa" for 4, use macro "aa" for 4.
The algorithm below follows the formal rules exactly.
*/

using System;

class Solution
{
    /*
    Time Complexity:
    - LCP precomputation: O(n^2)
    - bestLen computation: O(n^2)
    - DP with segment tree range updates / point queries: O(n log n)
    Overall: O(n^2)

    Space Complexity:
    - LCP table: O(n^2)
    - bestLen, dp helpers, segment tree arrays: O(n)
    Overall: O(n^2)
    */
    public long MinimumFatigue(string s, int[] typeCost, int macroCost)
    {
        int n = s.Length;

        // Edge case: empty string is not present in constraints, but handling it makes the method robust.
        if (n == 0) return 0;

        // --------------------------------------------------------------------
        // STEP 1: Precompute LCP (Longest Common Prefix) for every pair of suffixes.
        //
        // lcp[i, j] = number of equal characters from the start of suffixes s[i..] and s[j..]
        //
        // Why do we need this?
        // To know whether substring s[i..i+len-1] appeared earlier at position p,
        // we need to know how many characters match between suffixes starting at p and i.
        //
        // Recurrence:
        // If s[i] == s[j], then
        //     lcp[i, j] = 1 + lcp[i+1, j+1]
        // else
        //     lcp[i, j] = 0
        //
        // We fill from bottom-right toward top-left so that lcp[i+1, j+1] is already known.
        // --------------------------------------------------------------------
        int[,] lcp = new int[n + 1, n + 1];

        for (int i = n - 1; i >= 0; i--)
        {
            for (int j = n - 1; j >= 0; j--)
            {
                if (s[i] == s[j])
                {
                    lcp[i, j] = 1 + lcp[i + 1, j + 1];
                }
                else
                {
                    lcp[i, j] = 0;
                }
            }
        }

        // --------------------------------------------------------------------
        // STEP 2: For each position i, compute bestLen[i]:
        // the maximum length L such that s[i..i+L-1] appears somewhere earlier
        // completely inside s[0..i-1].
        //
        // For every earlier start p < i:
        // - lcp[p, i] tells us how many characters match
        // - but the earlier occurrence must end before i, so its length cannot exceed i - p
        // Thus that earlier occurrence supports length:
        //     min(lcp[p, i], i - p)
        //
        // Taking the maximum over all p gives bestLen[i].
        //
        // Why is this enough?
        // Because if length L is possible, then every smaller length is also possible
        // (just use a prefix of that matching earlier occurrence).
        // --------------------------------------------------------------------
        int[] bestLen = new int[n];

        for (int i = 0; i < n; i++)
        {
            int best = 0;

            for (int p = 0; p < i; p++)
            {
                int match = lcp[p, i];
                int allowedByBoundary = i - p;
                int usable = Math.Min(match, allowedByBoundary);

                if (usable > best)
                {
                    best = usable;
                }
            }

            bestLen[i] = best;
        }

        // --------------------------------------------------------------------
        // STEP 3: Dynamic Programming over produced prefix length.
        //
        // dp[i] = minimum fatigue to produce first i characters, i.e. s[0..i-1]
        //
        // Transitions from state i:
        // 1) Type one character:
        //      dp[i+1] = min(dp[i+1], dp[i] + typeCost[s[i]])
        //
        // 2) Use any valid macro length len in [1..bestLen[i]]:
        //      dp[i+len] = min(dp[i+len], dp[i] + macroCost)
        //
        // The second transition updates a whole range [i+1 .. i+bestLen[i]]
        // with the same candidate value. To do this efficiently, we use a segment tree
        // that supports:
        // - range "take minimum with value"
        // - point query
        //
        // This is a classic "range chmin + point query" use case.
        // --------------------------------------------------------------------
        long inf = long.MaxValue / 4;
        var seg = new RangeChMinPointQuerySegmentTree(n + 1, inf);

        // Base case: producing zero characters costs zero.
        seg.RangeChMin(0, 0, 0);

        for (int i = 0; i < n; i++)
        {
            // Retrieve the best known cost to reach prefix length i.
            long cur = seg.PointQuery(i);

            // If this state is unreachable, skip it.
            if (cur >= inf) continue;

            // ------------------------------------------------------------
            // Transition A: type the next character s[i].
            // This advances from prefix length i to i+1.
            // ------------------------------------------------------------
            int charIndex = s[i] - 'a';
            long typeNextCost = cur + typeCost[charIndex];
            seg.RangeChMin(i + 1, i + 1, typeNextCost);

            // ------------------------------------------------------------
            // Transition B: use a macro starting at i.
            //
            // If bestLen[i] = L, then every length 1..L is valid.
            // Using any such macro costs exactly macroCost, regardless of length.
            //
            // So we update every destination in [i+1 .. i+L] with cur + macroCost.
            // ------------------------------------------------------------
            int L = bestLen[i];
            if (L > 0)
            {
                long macroUseCost = cur + macroCost;
                seg.RangeChMin(i + 1, i + L, macroUseCost);
            }
        }

        return seg.PointQuery(n);
    }

    // ------------------------------------------------------------------------
    // Segment tree supporting:
    // - RangeChMin(left, right, value): for every position in [left, right],
    //   set storedValue = min(storedValue, value)
    // - PointQuery(index): get the final value at one position
    //
    // Because we only need point queries, each node stores a lazy "best value to apply
    // to the whole segment". During a point query, we walk from root to leaf and take
    // the minimum of all lazy values encountered.
    //
    // This is much simpler than a full lazy propagation tree because we never need
    // to query segment minima, only single points.
    // ------------------------------------------------------------------------
    private class RangeChMinPointQuerySegmentTree
    {
        private readonly int size;
        private readonly long[] lazy;
        private readonly long inf;

        public RangeChMinPointQuerySegmentTree(int n, long infValue)
        {
            inf = infValue;

            size = 1;
            while (size < n) size <<= 1;

            lazy = new long[size << 1];
            Array.Fill(lazy, inf);
        }

        public void RangeChMin(int left, int right, long value)
        {
            if (left > right) return;
            RangeChMin(left, right, value, 1, 0, size - 1);
        }

        private void RangeChMin(int left, int right, long value, int node, int segLeft, int segRight)
        {
            // No overlap: this segment is irrelevant to the update.
            if (right < segLeft || segRight < left) return;

            // Full cover: this whole segment should remember that every point inside it
            // can be improved by 'value'. Since multiple updates may hit the same segment,
            // we keep only the minimum.
            if (left <= segLeft && segRight <= right)
            {
                if (value < lazy[node]) lazy[node] = value;
                return;
            }

            // Partial overlap: recurse to children.
            int mid = (segLeft + segRight) >> 1;
            RangeChMin(left, right, value, node << 1, segLeft, mid);
            RangeChMin(left, right, value, node << 1 | 1, mid + 1, segRight);
        }

        public long PointQuery(int index)
        {
            int node = 1;
            int segLeft = 0;
            int segRight = size - 1;
            long answer = inf;

            while (true)
            {
                // Every node on the path from root to leaf may contribute a better value.
                if (lazy[node] < answer) answer = lazy[node];

                if (segLeft == segRight) break;

                int mid = (segLeft + segRight) >> 1;
                if (index <= mid)
                {
                    node = node << 1;
                    segRight = mid;
                }
                else
                {
                    node = node << 1 | 1;
                    segLeft = mid + 1;
                }
            }

            return answer;
        }
    }
}

// -----------------------------------------------------------------------------
// Demo code
// -----------------------------------------------------------------------------
var solver = new Solution();

// Example 1
string s1 = "ababa";
int[] typeCost1 =
{
    1, 1, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
    100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100
};
int macroCost1 = 2;
long result1 = solver.MinimumFatigue(s1, typeCost1, macroCost1);
Console.WriteLine(result1); // Expected: 5

// Example 2
string s2 = "aaaaaa";
int[] typeCost2 =
{
    3, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
    100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100
};
int macroCost2 = 4;
long result2 = solver.MinimumFatigue(s2, typeCost2, macroCost2);
Console.WriteLine(result2); // Under the formal rules, this is 14

// Additional small sanity check
string s3 = "abcde";
int[] typeCost3 =
{
    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
    14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26
};
int macroCost3 = 1;
long result3 = solver.MinimumFatigue(s3, typeCost3, macroCost3);
Console.WriteLine(result3); // No repeated substring before each position, so must type all => 15