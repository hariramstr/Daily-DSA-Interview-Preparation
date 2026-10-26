/*
Title: Maximum Score from Choosing a Stable Product Trio

Problem Description:
You are given an integer array ratings where ratings[i] is the quality rating of the i-th product in a catalog.
A merchandising team wants to highlight exactly three products in increasing index order: left, middle, and right
such that left < middle < right.

The middle product is considered stable only if:
    ratings[left] < ratings[middle] < ratings[right]

So the chosen values must form a strictly increasing trio.

The score of a valid trio is:
    ratings[left] + ratings[middle] + ratings[right]

Return the maximum possible score among all valid trios.
If no valid trio exists, return -1.

Constraints:
- 3 <= ratings.length <= 2 * 10^5
- 1 <= ratings[i] <= 10^9

Key idea:
For every possible middle index j, we want:
1. The best value on the left that is strictly smaller than ratings[j]
2. The best value on the right that is strictly greater than ratings[j]

If both exist, then:
    score = bestLeftSmaller + ratings[j] + bestRightGreater

We maximize this over all j.

To do this efficiently:
- We coordinate-compress the values because ratings can be as large as 1e9.
- We use a Fenwick Tree (Binary Indexed Tree) for prefix maximum queries from the left.
- We use another Fenwick Tree on reversed ranks to efficiently get "maximum among values greater than current" from the right.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Coordinate compression: O(n log n)
    - Left-to-right Fenwick operations: O(n log n)
    - Right-to-left Fenwick operations: O(n log n)
    Overall: O(n log n)

    Space Complexity:
    - Compressed arrays / sorted unique values / Fenwick trees: O(n)

    This is efficient enough for n up to 2 * 10^5.
    */
    public long MaximumScore(int[] ratings)
    {
        int n = ratings.Length;

        // ------------------------------------------------------------
        // STEP 1: Coordinate compression
        // ------------------------------------------------------------
        // Why do we need this?
        // The ratings can be as large as 1e9, which is too large to use directly
        // as Fenwick Tree indices.
        //
        // Coordinate compression maps each distinct rating value to a small rank:
        // for example:
        //   values: [2, 3, 6, 8]
        //   ranks :  1  2  3  4
        //
        // Important:
        // - The relative order is preserved.
        // - If a < b, then rank(a) < rank(b).
        //
        // This lets us query:
        // - values strictly smaller than current => ranks < currentRank
        // - values strictly greater than current => ranks > currentRank
        int[] sorted = new int[n];
        Array.Copy(ratings, sorted, n);
        Array.Sort(sorted);

        List<int> uniqueValues = new List<int>();
        foreach (int value in sorted)
        {
            if (uniqueValues.Count == 0 || uniqueValues[^1] != value)
            {
                uniqueValues.Add(value);
            }
        }

        int m = uniqueValues.Count;

        // Build a map from original value to compressed rank (1-based rank).
        Dictionary<int, int> rankMap = new Dictionary<int, int>(m);
        for (int i = 0; i < m; i++)
        {
            rankMap[uniqueValues[i]] = i + 1;
        }

        int[] ranks = new int[n];
        for (int i = 0; i < n; i++)
        {
            ranks[i] = rankMap[ratings[i]];
        }

        // ------------------------------------------------------------
        // STEP 2: Compute bestLeftSmaller for every index
        // ------------------------------------------------------------
        // For each position j, we want the maximum rating value on the left side
        // that is strictly smaller than ratings[j].
        //
        // Why maximum?
        // Because the total score is a sum, so among all valid left choices,
        // the largest one always gives the best score for this middle.
        //
        // Data structure:
        // Fenwick Tree storing maximum value seen so far for each compressed rank.
        //
        // Query:
        //   query(rank - 1)
        // gives the maximum value among all previously seen values with smaller rank,
        // which means strictly smaller rating.
        long[] bestLeftSmaller = new long[n];
        FenwickMax leftFenwick = new FenwickMax(m);

        for (int i = 0; i < n; i++)
        {
            int currentRank = ranks[i];
            int currentValue = ratings[i];

            // Query all ranks strictly smaller than currentRank.
            // This gives the best possible left value for index i.
            bestLeftSmaller[i] = leftFenwick.Query(currentRank - 1);

            // After processing index i as a possible middle,
            // insert it so it can serve as a left candidate for future positions.
            leftFenwick.Update(currentRank, currentValue);
        }

        // ------------------------------------------------------------
        // STEP 3: Compute bestRightGreater for every index
        // ------------------------------------------------------------
        // For each position j, we want the maximum rating value on the right side
        // that is strictly greater than ratings[j].
        //
        // Again, why maximum?
        // Because for a fixed middle, the largest valid right value gives the
        // largest total score.
        //
        // Fenwick Trees naturally answer prefix queries, not suffix queries.
        // We need "greater than current rank", which is a suffix in normal rank order.
        //
        // Trick:
        // Reverse the rank numbering:
        //   reversedRank = m - rank + 1
        //
        // Then:
        //   original ranks > currentRank
        // becomes
        //   reversed ranks < reversedCurrentRank
        //
        // So we can again use a prefix maximum query:
        //   query(reversedRank - 1)
        //
        // We scan from right to left so that the Fenwick Tree contains exactly
        // the elements to the right of the current index.
        long[] bestRightGreater = new long[n];
        FenwickMax rightFenwick = new FenwickMax(m);

        for (int i = n - 1; i >= 0; i--)
        {
            int currentRank = ranks[i];
            int currentValue = ratings[i];

            int reversedRank = m - currentRank + 1;

            // Query all reversed ranks strictly smaller than reversedRank,
            // which corresponds to original ranks strictly greater than currentRank.
            bestRightGreater[i] = rightFenwick.Query(reversedRank - 1);

            // Insert current value so it can serve as a right candidate
            // for positions further to the left.
            rightFenwick.Update(reversedRank, currentValue);
        }

        // ------------------------------------------------------------
        // STEP 4: Try every index as the middle of the trio
        // ------------------------------------------------------------
        // A valid trio centered at i exists only if:
        // - there is some smaller value on the left
        // - there is some greater value on the right
        //
        // Since ratings are positive (>= 1), we use 0 as "not found".
        long answer = -1;

        for (int i = 0; i < n; i++)
        {
            if (bestLeftSmaller[i] > 0 && bestRightGreater[i] > 0)
            {
                long score = bestLeftSmaller[i] + ratings[i] + bestRightGreater[i];
                if (score > answer)
                {
                    answer = score;
                }
            }
        }

        return answer;
    }

    // ------------------------------------------------------------
    // Fenwick Tree (Binary Indexed Tree) for prefix maximum
    // ------------------------------------------------------------
    // Standard Fenwick Trees are often used for sums.
    // Here we adapt it for maximum values.
    //
    // Operations:
    // - Update(index, value): tree[index] = max(tree[index], value)
    // - Query(index): maximum value in range [1..index]
    //
    // This works because "max" is associative in the way Fenwick needs
    // for prefix aggregation.
    private class FenwickMax
    {
        private readonly long[] tree;

        public FenwickMax(int size)
        {
            tree = new long[size + 2];
        }

        public void Update(int index, long value)
        {
            while (index < tree.Length)
            {
                if (value > tree[index])
                {
                    tree[index] = value;
                }
                index += index & -index;
            }
        }

        public long Query(int index)
        {
            long result = 0;
            while (index > 0)
            {
                if (tree[index] > result)
                {
                    result = tree[index];
                }
                index -= index & -index;
            }
            return result;
        }
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

// Example 1 from the prompt:
// ratings = [4, 2, 6, 3, 8]
//
// Valid increasing trios include:
// (4, 6, 8) => 18
// (2, 3, 8) => 13
//
// Maximum should be 18.
var solution = new Solution();

int[] ratings1 = { 4, 2, 6, 3, 8 };
long result1 = solution.MaximumScore(ratings1);
Console.WriteLine(result1); // Expected: 18

// Example 2 from the prompt:
// ratings = [9, 7, 5, 4]
//
// No strictly increasing trio exists.
// Expected: -1
int[] ratings2 = { 9, 7, 5, 4 };
long result2 = solution.MaximumScore(ratings2);
Console.WriteLine(result2); // Expected: -1

// Additional quick sanity check:
// [1, 2, 3] => only trio is (1,2,3), score = 6
int[] ratings3 = { 1, 2, 3 };
long result3 = solution.MaximumScore(ratings3);
Console.WriteLine(result3); // Expected: 6