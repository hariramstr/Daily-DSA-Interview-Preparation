/*
Title: Maximum Score from Choosing a Centered Triple
Difficulty: Medium
Topic: Arrays

Problem Description:
You are given an integer array nums representing signal strengths recorded over time. A valid centered triple is a choice of three indices (i, j, k) such that:

    i < j < k
    nums[i] < nums[j]
    nums[k] < nums[j]

So the middle element must be strictly greater than one element on its left and one element on its right.

The score of such a triple is:

    nums[i] + nums[j] + nums[k]

Return the maximum possible score among all valid centered triples.
If no valid centered triple exists, return -1.

Key idea:
For each possible center index j, we want:
1. The largest value on the left side that is still strictly smaller than nums[j]
2. The largest value on the right side that is still strictly smaller than nums[j]

If both exist, then center j can form a valid triple, and its best score is:

    bestLeftSmaller + nums[j] + bestRightSmaller

We must do this efficiently for arrays up to length 200000, so trying all triples is far too slow.
*/

using System;
using System.Collections.Generic;

class Solution
{
    /*
    Time Complexity:
        O(n log n)
        - We process each element a constant number of times.
        - Each query/update on the Fenwick Tree takes O(log M),
          where M is the number of distinct values after coordinate compression.
        - Since M <= n, total complexity is O(n log n).

    Space Complexity:
        O(n)
        - We store compressed values, left-best array, right-best array,
          and Fenwick Tree structures.

    Beginner-friendly summary:
        We evaluate every index as the possible center of the triple.
        For each center, we need the best smaller value on the left and on the right.
        To find those quickly, we use:
        1. Coordinate compression, so large values become small ranks.
        2. A Fenwick Tree (Binary Indexed Tree) that stores maximum values seen so far.
    */
    public long MaximumScore(int[] nums)
    {
        int n = nums.Length;

        // ------------------------------------------------------------
        // STEP 1: Coordinate compression
        // ------------------------------------------------------------
        // Why do we need this?
        // The values in nums can be as large as 1,000,000,000.
        // A Fenwick Tree works best with indices in a small range like 1..M.
        //
        // Coordinate compression maps each distinct number to a rank:
        // Example:
        // nums = [4, 9, 6, 3, 8]
        // sorted distinct = [3, 4, 6, 8, 9]
        // ranks:
        //   3 -> 1
        //   4 -> 2
        //   6 -> 3
        //   8 -> 4
        //   9 -> 5
        //
        // This preserves ordering:
        // if a < b in original values, then rank(a) < rank(b).
        int[] sorted = new int[n];
        Array.Copy(nums, sorted, n);
        Array.Sort(sorted);

        List<int> unique = new List<int>();
        foreach (int value in sorted)
        {
            if (unique.Count == 0 || unique[^1] != value)
            {
                unique.Add(value);
            }
        }

        int m = unique.Count;

        // rank[i] will store the compressed rank (1-based) of nums[i].
        int[] rank = new int[n];
        for (int i = 0; i < n; i++)
        {
            // BinarySearch returns the index of nums[i] in the sorted unique list.
            // We add 1 because Fenwick Trees are usually implemented as 1-based.
            rank[i] = unique.BinarySearch(nums[i]) + 1;
        }

        // ------------------------------------------------------------
        // STEP 2: Find the best smaller value on the LEFT for every index
        // ------------------------------------------------------------
        // leftBest[j] should be:
        // the largest value among nums[0..j-1] that is strictly smaller than nums[j]
        //
        // If no such value exists, we store -1.
        //
        // Why largest?
        // Because for a fixed center nums[j], the score is:
        // left + nums[j] + right
        // To maximize the score, we want the largest valid left and right values.
        long[] leftBest = new long[n];
        Array.Fill(leftBest, -1);

        // FenwickMaxPrefix supports:
        // - Update(rank, value): record that we've seen this value
        // - Query(rank - 1): get the maximum value among all seen values
        //   whose rank is <= rank - 1, meaning strictly smaller than nums[j]
        FenwickMaxPrefix leftFenwick = new FenwickMaxPrefix(m);

        for (int j = 0; j < n; j++)
        {
            // Query all values with rank strictly less than rank[j].
            // This gives the largest seen value on the left that is smaller than nums[j].
            long bestSmallerOnLeft = leftFenwick.Query(rank[j] - 1);
            leftBest[j] = bestSmallerOnLeft;

            // After processing index j as a center candidate,
            // we insert nums[j] into the Fenwick Tree so future positions
            // can use it as a left-side candidate.
            leftFenwick.Update(rank[j], nums[j]);
        }

        // ------------------------------------------------------------
        // STEP 3: Find the best smaller value on the RIGHT for every index
        // ------------------------------------------------------------
        // rightBest[j] should be:
        // the largest value among nums[j+1..n-1] that is strictly smaller than nums[j]
        //
        // We do the same idea as the left pass, but scan from right to left.
        long[] rightBest = new long[n];
        Array.Fill(rightBest, -1);

        FenwickMaxPrefix rightFenwick = new FenwickMaxPrefix(m);

        for (int j = n - 1; j >= 0; j--)
        {
            // Query values strictly smaller than nums[j] that have appeared on the right.
            long bestSmallerOnRight = rightFenwick.Query(rank[j] - 1);
            rightBest[j] = bestSmallerOnRight;

            // Insert nums[j] so positions further left can use it as a right-side candidate.
            rightFenwick.Update(rank[j], nums[j]);
        }

        // ------------------------------------------------------------
        // STEP 4: Try every index as the center and compute the best score
        // ------------------------------------------------------------
        // A valid center j must have:
        // - some smaller value on the left
        // - some smaller value on the right
        //
        // If both exist, the best score using j as center is:
        // leftBest[j] + nums[j] + rightBest[j]
        long answer = -1;

        for (int j = 0; j < n; j++)
        {
            if (leftBest[j] != -1 && rightBest[j] != -1)
            {
                long score = leftBest[j] + nums[j] + rightBest[j];
                if (score > answer)
                {
                    answer = score;
                }
            }
        }

        return answer;
    }

    // ------------------------------------------------------------
    // Fenwick Tree / Binary Indexed Tree for prefix maximum
    // ------------------------------------------------------------
    // Standard Fenwick Trees often store sums.
    // Here, instead of sum, we store the maximum value.
    //
    // tree[i] represents information for a range of ranks.
    // Query(x) returns the maximum value among all inserted values
    // with compressed rank <= x.
    //
    // This is exactly what we need because:
    // "strictly smaller than nums[j]" means "rank < rank[j]",
    // so we query rank[j] - 1.
    private class FenwickMaxPrefix
    {
        private readonly long[] tree;

        public FenwickMaxPrefix(int size)
        {
            tree = new long[size + 2];

            // We use -1 as "no value exists yet".
            // Since nums[i] >= 1, -1 is a safe sentinel.
            Array.Fill(tree, -1);
        }

        public void Update(int index, long value)
        {
            // Move upward through Fenwick Tree nodes that cover this index.
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
            long result = -1;

            // Move downward through Fenwick Tree nodes collecting the maximum.
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

var solution = new Solution();

// Example 1
int[] nums1 = { 4, 9, 6, 3, 8 };
long result1 = solution.MaximumScore(nums1);
Console.WriteLine(result1); // Expected: 19

// Example 2
int[] nums2 = { 5, 5, 5, 5 };
long result2 = solution.MaximumScore(nums2);
Console.WriteLine(result2); // Expected: -1

// Additional quick checks
int[] nums3 = { 1, 3, 2 };
Console.WriteLine(solution.MaximumScore(nums3)); // Expected: 6

int[] nums4 = { 10, 5, 9, 4, 8, 3 };
Console.WriteLine(solution.MaximumScore(nums4)); // One valid best score should be 22 from (5,9,8) or similar valid choice