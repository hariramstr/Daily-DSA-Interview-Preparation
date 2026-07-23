/*
Title: Maximum Score from Picking a Centered Product Trio
Difficulty: Medium
Topic: Arrays

Problem Description:
You are given an integer array ratings where ratings[i] is the popularity score of the i-th product in a catalog.
A valid centered product trio is formed by choosing three indices (l, c, r) such that:

    l < c < r
    ratings[l] < ratings[c]
    ratings[r] < ratings[c]

In other words, the center product must have a strictly higher rating than one product on its left
and one product on its right.

The score of such a trio is:
    ratings[l] + ratings[c] + ratings[r]

Return the maximum possible score among all valid centered product trios.
If no valid trio exists, return -1.

Key Insight:
For each index c used as the center, we want:
- the largest value on the left that is still strictly smaller than ratings[c]
- the largest value on the right that is still strictly smaller than ratings[c]

Why "largest smaller"?
Because the center value is fixed for a chosen c, so to maximize the total score we should choose
the biggest valid left value and the biggest valid right value.

Efficient Strategy:
1. Process from left to right and, for every index, compute the best left candidate:
   the maximum value seen so far that is < ratings[i].
2. Process from right to left and, for every index, compute the best right candidate:
   the maximum value seen so far on the right that is < ratings[i].
3. Combine both arrays:
   if both sides exist for index i, compute leftBest[i] + ratings[i] + rightBest[i].

Because values can be as large as 1e9, we coordinate-compress them and use a Fenwick Tree
(Binary Indexed Tree) configured for prefix maximum queries.

Example 1:
ratings = [4, 9, 2, 7, 3]

For center 9:
- best smaller on left = 4
- best smaller on right = 3
score = 4 + 9 + 3 = 16

For center 7:
- best smaller on left = 4 or 2, best is 4
- best smaller on right = 3
score = 4 + 7 + 3 = 14

Maximum = 16

Example 2:
ratings = [1, 2, 3, 4]

No element has a smaller value on both left and right.
Answer = -1
*/

using System;

public class Solution
{
    /*
    Time Complexity:
        O(n log n)
        - O(n log n) for coordinate compression lookups and Fenwick operations
        - done twice (left pass and right pass), still O(n log n)

    Space Complexity:
        O(n)
        - compressed copy / sorted unique helper arrays
        - leftBest and rightBest arrays
        - Fenwick tree arrays

    Beginner-friendly summary:
    We cannot try every triple because that would be too slow.
    Instead, for each possible center, we quickly ask:
    "What is the biggest number smaller than me on the left?"
    "What is the biggest number smaller than me on the right?"
    Then we combine those answers.
    */
    public long MaximumScore(int[] ratings)
    {
        int n = ratings.Length;

        // These arrays will store, for every position i:
        // - leftBest[i]  = the largest value on the LEFT side of i that is strictly smaller than ratings[i]
        // - rightBest[i] = the largest value on the RIGHT side of i that is strictly smaller than ratings[i]
        //
        // If no such value exists, we store -1.
        long[] leftBest = new long[n];
        long[] rightBest = new long[n];
        Array.Fill(leftBest, -1);
        Array.Fill(rightBest, -1);

        // ------------------------------------------------------------
        // STEP 1: Coordinate compression
        // ------------------------------------------------------------
        // Why do we need this?
        // ratings[i] can be as large as 1,000,000,000.
        // A Fenwick Tree works best when indices are small and dense, like 1..m.
        //
        // So we:
        // 1. copy the values
        // 2. sort them
        // 3. remove duplicates
        // 4. map each original value to its rank in the sorted unique list
        //
        // Then:
        // - smaller values correspond to smaller ranks
        // - querying "largest value strictly smaller than x" becomes:
        //   query Fenwick prefix up to rank(x) - 1
        int[] sorted = new int[n];
        Array.Copy(ratings, sorted, n);
        Array.Sort(sorted);

        int uniqueCount = 0;
        for (int i = 0; i < n; i++)
        {
            if (i == 0 || sorted[i] != sorted[i - 1])
            {
                sorted[uniqueCount++] = sorted[i];
            }
        }

        // ------------------------------------------------------------
        // STEP 2: Left-to-right pass
        // ------------------------------------------------------------
        // Goal:
        // For each index i, find the largest value among ratings[0..i-1]
        // that is strictly smaller than ratings[i].
        //
        // Data structure:
        // Fenwick tree storing prefix maximums.
        //
        // What does the Fenwick tree store?
        // At compressed rank r, we store the maximum actual rating value
        // that has appeared so far with that rank.
        //
        // Query:
        // query(rank - 1) gives the maximum actual value among all seen values
        // whose rank is smaller than current rank, which means strictly smaller value.
        var leftFenwick = new FenwickMax(uniqueCount);

        for (int i = 0; i < n; i++)
        {
            int rank = LowerBound(sorted, uniqueCount, ratings[i]) + 1;

            // Query all strictly smaller ranks.
            // If result is -1, then no valid left candidate exists.
            leftBest[i] = leftFenwick.Query(rank - 1);

            // After using current index as a center candidate, we insert its value
            // so it can help future indices on the right.
            leftFenwick.Update(rank, ratings[i]);
        }

        // ------------------------------------------------------------
        // STEP 3: Right-to-left pass
        // ------------------------------------------------------------
        // This is symmetric to the left pass.
        //
        // Goal:
        // For each index i, find the largest value among ratings[i+1..n-1]
        // that is strictly smaller than ratings[i].
        //
        // We scan from right to left so that the Fenwick tree contains exactly
        // the values to the right of the current index.
        var rightFenwick = new FenwickMax(uniqueCount);

        for (int i = n - 1; i >= 0; i--)
        {
            int rank = LowerBound(sorted, uniqueCount, ratings[i]) + 1;

            // Again, query only strictly smaller values.
            rightBest[i] = rightFenwick.Query(rank - 1);

            // Insert current value so it becomes available to indices further left.
            rightFenwick.Update(rank, ratings[i]);
        }

        // ------------------------------------------------------------
        // STEP 4: Try every index as the center
        // ------------------------------------------------------------
        // A valid trio centered at i exists only if:
        // - there is a smaller value on the left
        // - there is a smaller value on the right
        //
        // Since leftBest[i] and rightBest[i] were chosen as the largest valid values,
        // the score we compute here is the best possible score for center i.
        long answer = -1;

        for (int i = 0; i < n; i++)
        {
            if (leftBest[i] != -1 && rightBest[i] != -1)
            {
                long score = leftBest[i] + ratings[i] + rightBest[i];
                if (score > answer)
                {
                    answer = score;
                }
            }
        }

        return answer;
    }

    // Standard lower_bound:
    // returns the first index in arr[0..length-1] where arr[index] >= target
    //
    // Because target is guaranteed to exist in the compressed sorted unique array,
    // this gives us the exact compressed position for target.
    private int LowerBound(int[] arr, int length, int target)
    {
        int left = 0;
        int right = length;

        while (left < right)
        {
            int mid = left + (right - left) / 2;

            if (arr[mid] < target)
            {
                left = mid + 1;
            }
            else
            {
                right = mid;
            }
        }

        return left;
    }

    // Fenwick Tree / Binary Indexed Tree for prefix maximum.
    //
    // Usually Fenwick trees are taught for sums, but the same index-jumping idea
    // also works for prefix maximum when we only do "update with max" and "query prefix max".
    //
    // tree[i] stores the maximum value for a certain range ending at i.
    private class FenwickMax
    {
        private readonly long[] tree;

        public FenwickMax(int size)
        {
            tree = new long[size + 1];

            // We use -1 as "no value seen yet".
            Array.Fill(tree, -1);
        }

        public void Update(int index, long value)
        {
            // Move upward through Fenwick structure.
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

            // Move downward through Fenwick structure,
            // collecting the maximum over the prefix [1..index].
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
int[] ratings1 = { 4, 9, 2, 7, 3 };
long result1 = solution.MaximumScore(ratings1);
Console.WriteLine(result1); // Expected: 16

// Example 2
int[] ratings2 = { 1, 2, 3, 4 };
long result2 = solution.MaximumScore(ratings2);
Console.WriteLine(result2); // Expected: -1

// Additional quick checks
int[] ratings3 = { 2, 5, 1, 4, 3 };
long result3 = solution.MaximumScore(ratings3);
Console.WriteLine(result3); // One valid best trio: 2 + 5 + 4? invalid because 4 is right of 5 and < 5 yes, so score 11

int[] ratings4 = { 5, 1, 5, 1, 5 };
long result4 = solution.MaximumScore(ratings4);
Console.WriteLine(result4); // Expected: 7 from center 5 with 1 on both sides