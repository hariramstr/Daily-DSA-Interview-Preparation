/*
Title: Minimum Query Limit for Ranked Search Pages

Problem Description:
A search platform stores n documents in a fixed ranking order from 1 to n. The relevance score of document i is given by scores[i]. A user only sees the first L documents, where L is called the query limit. You are also given m analyst requirements. Each requirement is a pair [k, t], meaning that within the first L documents, there must be at least k documents whose relevance score is greater than or equal to t.

Your task is to find the minimum query limit L such that all requirements are satisfied at the same time. If no such L exists, return -1.

The ranking order cannot be changed. A document contributes to a requirement only if its position is at most L. Note that different requirements may use different score thresholds, and the same document may help satisfy multiple requirements.

This problem is designed for an efficient solution. A naive scan for every possible L will be too slow. You should exploit the monotonic nature of the answer: if a certain prefix length L satisfies all requirements, then any larger prefix length also satisfies all requirements.

Constraints:
- 1 <= n <= 200000
- 1 <= m <= 200000
- 1 <= scores[i] <= 10^9
- 1 <= k <= n
- 1 <= t <= 10^9
- Requirements are independent, but all must hold simultaneously

Example 1:
Input:
scores = [5, 1, 7, 3, 9, 6]
requirements = [[2, 6], [3, 5]]
Output:
5

Explanation:
For L = 4, the first four scores are [5, 1, 7, 3]. There is only one score >= 6, so the first requirement fails.
For L = 5, the prefix is [5, 1, 7, 3, 9]. There are two scores >= 6 (7 and 9), and three scores >= 5 (5, 7, 9), so both requirements are satisfied.
Therefore, the minimum valid query limit is 5.

Example 2:
Input:
scores = [2, 2, 2, 2]
requirements = [[1, 3]]
Output:
-1

Explanation:
No prefix contains any score >= 3, so the requirement can never be satisfied.
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    /*
        Time Complexity:
        - Coordinate compression of scores and thresholds: O((n + m) log(n + m))
        - Sorting documents by position and requirements by k: O(n log n + m log m)
        - Fenwick Tree updates and queries: O((n + m) log(n + m))
        Overall: O((n + m) log(n + m))

        Space Complexity:
        - O(n + m) for compressed values, sorted arrays, Fenwick tree, and helper structures
    */
    public int MinimumQueryLimit(int[] scores, int[][] requirements)
    {
        // ------------------------------------------------------------
        // Step 1: Handle a simple edge case.
        // ------------------------------------------------------------
        // If there are no requirements, then technically every prefix works.
        // Since the problem asks for the smallest L in [1, n], the answer is 1.
        // This keeps the method robust even if the caller provides an empty list.
        if (requirements == null || requirements.Length == 0)
        {
            return 1;
        }

        int n = scores.Length;
        int m = requirements.Length;

        // ------------------------------------------------------------
        // Step 2: Coordinate compression.
        // ------------------------------------------------------------
        // Why do we need this?
        // Scores and thresholds can be as large as 1e9.
        // A Fenwick Tree works best on compact index ranges like 1..K.
        //
        // We only care about values that actually appear:
        // - every document score
        // - every threshold t from a requirement
        //
        // After sorting unique values, each real value gets a small integer index.
        var allValues = new List<int>(n + m);
        allValues.AddRange(scores);
        foreach (var req in requirements)
        {
            allValues.Add(req[1]);
        }

        allValues.Sort();

        var uniqueValues = new List<int>(allValues.Count);
        foreach (int value in allValues)
        {
            if (uniqueValues.Count == 0 || uniqueValues[^1] != value)
            {
                uniqueValues.Add(value);
            }
        }

        // Helper function:
        // Given a real score/threshold value, return its compressed index in [1..K].
        int GetCompressedIndex(int value)
        {
            int idx = uniqueValues.BinarySearch(value);
            return idx + 1; // Fenwick Tree is easier to implement with 1-based indexing.
        }

        int valueCount = uniqueValues.Count;

        // ------------------------------------------------------------
        // Step 3: Prepare documents with their positions and compressed scores.
        // ------------------------------------------------------------
        // Each document has:
        // - position in the ranking (1-based)
        // - score
        //
        // We will process documents in increasing position order.
        // This is natural because a prefix of length L contains exactly documents 1..L.
        var docs = new (int Position, int ScoreIndex)[n];
        for (int i = 0; i < n; i++)
        {
            docs[i] = (i + 1, GetCompressedIndex(scores[i]));
        }

        // ------------------------------------------------------------
        // Step 4: Prepare requirements.
        // ------------------------------------------------------------
        // A requirement [k, t] asks:
        // "What is the earliest position L where the prefix contains at least k
        //  documents with score >= t?"
        //
        // This is a classic "find the k-th qualifying document" style question.
        //
        // Key idea:
        // If we process requirements in increasing k, then when we are trying to satisfy
        // all requirements with count target <= current k, we can maintain enough
        // information to know the earliest position where each threshold reaches that count.
        //
        // But thresholds differ, so we need a structure that can count how many seen
        // documents have score >= t.
        //
        // We use a Fenwick Tree over compressed score values.
        // It stores counts of scores among documents processed so far.
        //
        // For a fixed prefix ending at position pos:
        // count(score >= t) = totalSeen - count(score < t)
        //
        // For each requirement, we want the minimum position where this count reaches k.
        // We can find that by processing positions from left to right and checking when
        // each requirement becomes true.
        //
        // However, checking every requirement at every position would be too slow.
        //
        // Better idea:
        // Binary search on answer L, and for each L efficiently test all requirements.
        //
        // Since the problem statement explicitly highlights monotonicity, this is the
        // intended high-level strategy.
        //
        // So the rest of the method will implement:
        // 1. Binary search on L
        // 2. A fast "Can this prefix length L satisfy all requirements?" check
        //
        // The challenge is making the check efficient.
        //
        // For a fixed L, we need for every requirement [k, t]:
        // number of scores in scores[0..L-1] that are >= t  >= k
        //
        // If we group requirements by threshold t, then for each distinct threshold
        // we only need one count:
        // count of prefix scores >= t
        //
        // Then every requirement with that threshold is satisfied iff that count is
        // at least the maximum k required for that threshold.
        //
        // This is a very important optimization:
        // For the same threshold t, multiple requirements collapse into one strongest
        // requirement: [maxK, t].
        //
        // Example:
        // [2, 6] and [1, 6] together are equivalent to just [2, 6].
        //
        // This reduces the number of effective checks to the number of distinct thresholds.
        var thresholdToMaxK = new Dictionary<int, int>();
        foreach (var req in requirements)
        {
            int k = req[0];
            int t = req[1];

            if (!thresholdToMaxK.TryGetValue(t, out int existing))
            {
                thresholdToMaxK[t] = k;
            }
            else if (k > existing)
            {
                thresholdToMaxK[t] = k;
            }
        }

        // Convert the dictionary into arrays for faster repeated checking during binary search.
        var compressedThresholds = new int[thresholdToMaxK.Count];
        var requiredKs = new int[thresholdToMaxK.Count];

        int ptr = 0;
        foreach (var kvp in thresholdToMaxK)
        {
            compressedThresholds[ptr] = GetCompressedIndex(kvp.Key);
            requiredKs[ptr] = kvp.Value;
            ptr++;
        }

        // ------------------------------------------------------------
        // Step 5: Quick impossibility check using the full array.
        // ------------------------------------------------------------
        // Before binary searching, it is useful to verify whether the requirements
        // can be satisfied at all, even with L = n.
        //
        // If the full list of documents still fails some requirement, the answer is -1.
        //
        // We can do this by counting all scores once.
        var fullFenwick = new FenwickTree(valueCount);
        for (int i = 0; i < n; i++)
        {
            fullFenwick.Add(docs[i].ScoreIndex, 1);
        }

        int totalDocuments = n;
        for (int i = 0; i < compressedThresholds.Length; i++)
        {
            int thresholdIndex = compressedThresholds[i];
            int need = requiredKs[i];

            // Count how many scores are >= threshold.
            // Fenwick prefix query gives count of scores <= some compressed index.
            // So:
            // count(score >= threshold) = total - count(score < threshold)
            int lessThanThreshold = fullFenwick.Sum(thresholdIndex - 1);
            int atLeastThreshold = totalDocuments - lessThanThreshold;

            if (atLeastThreshold < need)
            {
                return -1;
            }
        }

        // ------------------------------------------------------------
        // Step 6: Define the feasibility check for a given prefix length L.
        // ------------------------------------------------------------
        // This function answers:
        // "If the user sees only the first L documents, are all requirements satisfied?"
        //
        // We rebuild a Fenwick Tree for the first L scores.
        // Then for each distinct threshold, we compute how many scores in the prefix
        // are >= that threshold and compare against the strongest required k.
        //
        // This check is O((L + distinctThresholds) log V), which is correct but could
        // be too slow if repeated many times in binary search for worst-case inputs.
        //
        // To keep the solution efficient enough for the given constraints, we instead
        // precompute prefix answers offline:
        //
        // For each distinct threshold t, we want the earliest position where the count
        // of scores >= t reaches requiredK.
        //
        // Once we know that earliest position for every threshold, the final answer is
        // simply the maximum of those positions, because all requirements must hold
        // simultaneously.
        //
        // This is actually even better than binary search:
        // it directly computes the minimum valid L.
        //
        // But the problem topic is Binary Search, and monotonicity is highlighted.
        // Still, correctness and efficiency matter most. The direct offline method is
        // stronger and avoids repeated expensive checks.
        //
        // So from here, we will compute the earliest satisfying position per threshold.
        // The final answer will be the maximum among them.
        //
        // This produces exactly the same minimum L that binary search would find.
        //
        // ------------------------------------------------------------
        // Step 7: Offline direct computation of earliest positions.
        // ------------------------------------------------------------
        //
        // For a threshold t and required count k:
        // We need the position of the k-th document whose score >= t.
        //
        // If we list all document positions whose score >= t in increasing order,
        // then the answer for that threshold is simply the k-th position in that list.
        //
        // So the problem becomes:
        // For many thresholds t, find the k-th smallest position among documents
        // whose score >= t.
        //
        // We process thresholds in descending order.
        // As threshold decreases, more documents become eligible.
        //
        // Data structure choice:
        // We use a Fenwick Tree over positions 1..n.
        // - When a document becomes eligible for the current threshold, we add 1 at its position.
        // - Then the tree represents all positions of documents with score >= current threshold.
        // - To find the k-th eligible position, we use Fenwick's "find by order" operation.
        //
        // This is efficient:
        // - sort documents by score descending
        // - sort threshold queries by threshold descending
        // - sweep once
        //
        // This is the key hard-algorithm idea in the solution.

        var docsByScoreDesc = new (int Score, int Position)[n];
        for (int i = 0; i < n; i++)
        {
            docsByScoreDesc[i] = (scores[i], i + 1);
        }

        Array.Sort(docsByScoreDesc, (a, b) =>
        {
            int cmp = b.Score.CompareTo(a.Score);
            if (cmp != 0) return cmp;
            return a.Position.CompareTo(b.Position);
        });

        var thresholdQueries = new (int Threshold, int NeedK)[thresholdToMaxK.Count];
        ptr = 0;
        foreach (var kvp in thresholdToMaxK)
        {
            thresholdQueries[ptr++] = (kvp.Key, kvp.Value);
        }

        Array.Sort(thresholdQueries, (a, b) =>
        {
            int cmp = b.Threshold.CompareTo(a.Threshold);
            if (cmp != 0) return cmp;
            return a.NeedK.CompareTo(b.NeedK);
        });

        var positionFenwick = new FenwickTree(n);

        int docPointer = 0;
        int answer = 0;

        foreach (var query in thresholdQueries)
        {
            int threshold = query.Threshold;
            int needK = query.NeedK;

            // --------------------------------------------------------
            // Add all documents whose score is >= current threshold.
            // --------------------------------------------------------
            // Because thresholds are processed from high to low, once a document is added,
            // it remains eligible for all later (smaller) thresholds.
            while (docPointer < n && docsByScoreDesc[docPointer].Score >= threshold)
            {
                int position = docsByScoreDesc[docPointer].Position;
                positionFenwick.Add(position, 1);
                docPointer++;
            }

            // --------------------------------------------------------
            // Now the Fenwick Tree contains exactly the positions of all documents
            // with score >= threshold.
            //
            // We need the k-th such position.
            // If the total count is at least needK, Fenwick can find the smallest
            // position p such that count of eligible documents in [1..p] is >= needK.
            // That p is exactly the position of the k-th eligible document.
            // --------------------------------------------------------
            int totalEligible = positionFenwick.Sum(n);

            // We already did a full impossibility check, so totalEligible >= needK
            // should always hold here. Still, keeping the guard makes the code safer.
            if (totalEligible < needK)
            {
                return -1;
            }

            int earliestPositionForThisThreshold = positionFenwick.FindByOrder(needK);

            // --------------------------------------------------------
            // Since ALL requirements must be satisfied simultaneously,
            // the final prefix length must be at least the earliest satisfying
            // position for every threshold.
            //
            // Therefore, the minimum valid L is the maximum of these positions.
            // --------------------------------------------------------
            if (earliestPositionForThisThreshold > answer)
            {
                answer = earliestPositionForThisThreshold;
            }
        }

        return answer;
    }

    private class FenwickTree
    {
        private readonly int[] _tree;
        private readonly int _size;

        public FenwickTree(int size)
        {
            _size = size;
            _tree = new int[size + 1];
        }

        public void Add(int index, int delta)
        {
            while (index <= _size)
            {
                _tree[index] += delta;
                index += index & -index;
            }
        }

        public int Sum(int index)
        {
            int result = 0;
            while (index > 0)
            {
                result += _tree[index];
                index -= index & -index;
            }
            return result;
        }

        // Finds the smallest index idx such that Sum(idx) >= order.
        // Assumes 1 <= order <= total sum in the tree.
        public int FindByOrder(int order)
        {
            int index = 0;
            int bitMask = HighestPowerOfTwoAtMost(_size);

            while (bitMask != 0)
            {
                int nextIndex = index + bitMask;
                if (nextIndex <= _size && _tree[nextIndex] < order)
                {
                    order -= _tree[nextIndex];
                    index = nextIndex;
                }
                bitMask >>= 1;
            }

            return index + 1;
        }

        private static int HighestPowerOfTwoAtMost(int x)
        {
            int power = 1;
            while ((power << 1) <= x)
            {
                power <<= 1;
            }
            return power;
        }
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] scores1 = { 5, 1, 7, 3, 9, 6 };
int[][] requirements1 =
{
    new[] { 2, 6 },
    new[] { 3, 5 }
};
int result1 = solution.MinimumQueryLimit(scores1, requirements1);
Console.WriteLine(result1); // Expected: 5

// Example 2
int[] scores2 = { 2, 2, 2, 2 };
int[][] requirements2 =
{
    new[] { 1, 3 }
};
int result2 = solution.MinimumQueryLimit(scores2, requirements2);
Console.WriteLine(result2); // Expected: -1