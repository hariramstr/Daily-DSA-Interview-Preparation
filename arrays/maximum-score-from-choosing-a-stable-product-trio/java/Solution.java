import java.util.*;

/*
Problem Title: Maximum Score from Choosing a Stable Product Trio

Problem Description:
You are given an integer array ratings where ratings[i] is the quality rating of the i-th product in a catalog.
A merchandising team wants to highlight exactly three products in increasing index order: left, middle, and right
such that left < middle < right.

The middle product is considered stable only if its rating is strictly greater than the rating of the left product
and strictly less than the rating of the right product. In other words, the chosen ratings must form a strictly
increasing trio when read from left to right.

The score of a valid trio is defined as:
    ratings[left] + ratings[middle] + ratings[right]

Your task is to return the maximum possible score among all valid trios.
If no valid trio exists, return -1.

Constraints:
- 3 <= ratings.length <= 2 * 10^5
- 1 <= ratings[i] <= 10^9

Examples:
1) ratings = [4, 2, 6, 3, 8]
   Valid increasing trios include:
   - (4, 6, 8) with score 18
   - (2, 3, 8) with score 13
   Maximum score = 18

2) ratings = [9, 7, 5, 4]
   No strictly increasing trio exists
   Answer = -1

Efficient Idea:
For every possible middle index i, we want:
- the largest value on the left side that is strictly smaller than ratings[i]
- the largest value on the right side that is strictly greater than ratings[i]

Then the best trio using i as the middle is:
    bestLeft + ratings[i] + bestRight

To support this efficiently for large arrays:
1. Coordinate-compress the values.
2. Use a Fenwick Tree (Binary Indexed Tree) for prefix maximums from the left.
3. Use another Fenwick Tree from the right to query suffix maximums by reversing indices.

This gives an O(n log n) solution, which is efficient for n up to 2 * 10^5.
*/

public class Solution {

    /**
     * Computes the maximum score of any strictly increasing trio:
     * ratings[left] < ratings[middle] < ratings[right] with left < middle < right.
     *
     * Strategy:
     * - For each index i as the middle element:
     *   1) Find the maximum value to the left that is strictly smaller than ratings[i].
     *   2) Find the maximum value to the right that is strictly greater than ratings[i].
     * - If both exist, compute the trio sum and keep the maximum.
     *
     * We use coordinate compression because ratings can be as large as 1e9.
     * Then we use Fenwick Trees that store maximum values instead of sums.
     *
     * @param ratings the array of product ratings
     * @return the maximum possible score of a valid strictly increasing trio, or -1 if none exists
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long maximumScore(int[] ratings) {
        int n = ratings.length;

        // Step 1:
        // Coordinate compression.
        //
        // Why do we need this?
        // Ratings can be up to 1e9, which is too large to use directly as Fenwick Tree indices.
        // So we sort unique values and map each rating to a compact rank in [1..m].
        int[] sorted = ratings.clone();
        Arrays.sort(sorted);

        int[] unique = new int[n];
        int m = 0;
        for (int value : sorted) {
            if (m == 0 || unique[m - 1] != value) {
                unique[m++] = value;
            }
        }

        // rank[i] will store the compressed 1-based rank of ratings[i].
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) {
            rank[i] = lowerBound(unique, m, ratings[i]) + 1;
        }

        // Step 2:
        // leftBest[i] = largest value among ratings[0..i-1] that is strictly smaller than ratings[i].
        //
        // We process from left to right.
        // Fenwick tree stores maximum actual rating value for each compressed rank.
        // Querying prefix rank[i]-1 gives the best smaller value seen so far.
        long[] leftBest = new long[n];
        FenwickMax leftFenwick = new FenwickMax(m);

        for (int i = 0; i < n; i++) {
            int r = rank[i];

            // Query all ranks strictly smaller than current rank.
            leftBest[i] = leftFenwick.query(r - 1);

            // Insert current value into the Fenwick tree.
            leftFenwick.update(r, ratings[i]);
        }

        // Step 3:
        // rightBest[i] = largest value among ratings[i+1..n-1] that is strictly greater than ratings[i].
        //
        // A normal Fenwick tree supports prefix queries, not suffix queries.
        // To query "greater than current rank", we reverse the rank space:
        //
        // reversedRank = m - rank + 1
        //
        // Then all original ranks > current rank become reversed ranks < reversedRank.
        // So querying prefix(reversedRank - 1) gives the maximum value on the right
        // that is strictly greater than ratings[i].
        long[] rightBest = new long[n];
        FenwickMax rightFenwick = new FenwickMax(m);

        for (int i = n - 1; i >= 0; i--) {
            int r = rank[i];
            int reversedRank = m - r + 1;

            // Query values strictly greater than current value on the right side.
            rightBest[i] = rightFenwick.query(reversedRank - 1);

            // Insert current value for future elements to the left.
            rightFenwick.update(reversedRank, ratings[i]);
        }

        // Step 4:
        // Try every index as the middle element.
        // A valid trio exists only if:
        // - there is a smaller value on the left
        // - there is a greater value on the right
        long answer = -1;

        for (int i = 0; i < n; i++) {
            if (leftBest[i] > 0 && rightBest[i] > 0) {
                long score = leftBest[i] + ratings[i] + rightBest[i];
                answer = Math.max(answer, score);
            }
        }

        return answer;
    }

    /**
     * Finds the first index in the sorted array segment [0, length) whose value is
     * greater than or equal to target.
     *
     * This is the standard lower bound operation used for coordinate compression.
     *
     * @param arr the sorted array containing unique values in its first {@code length} positions
     * @param length the number of valid elements in arr to search
     * @param target the value whose insertion position we want
     * @return the first index where arr[index] >= target
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public int lowerBound(int[] arr, int length, int target) {
        int left = 0;
        int right = length;

        while (left < right) {
            int mid = left + (right - left) / 2;

            if (arr[mid] < target) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }

        return left;
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding called method costs
     * Space complexity: O(1) excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] ratings1 = {4, 2, 6, 3, 8};
        long result1 = solution.maximumScore(ratings1);
        System.out.println("Input: " + Arrays.toString(ratings1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 18");
        System.out.println();

        int[] ratings2 = {9, 7, 5, 4};
        long result2 = solution.maximumScore(ratings2);
        System.out.println("Input: " + Arrays.toString(ratings2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: -1");
        System.out.println();

        int[] ratings3 = {1, 2, 3};
        long result3 = solution.maximumScore(ratings3);
        System.out.println("Input: " + Arrays.toString(ratings3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 6");
        System.out.println();

        int[] ratings4 = {5, 1, 5, 2, 6};
        long result4 = solution.maximumScore(ratings4);
        System.out.println("Input: " + Arrays.toString(ratings4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 13");
    }

    /**
     * Fenwick Tree / Binary Indexed Tree specialized for prefix maximum queries.
     *
     * Instead of storing sums, this structure stores the maximum value seen so far.
     * Supported operations:
     * - update(index, value): tree[index] = max(tree[index], value)
     * - query(index): maximum value in range [1..index]
     *
     * This is perfect for:
     * - "best smaller value on the left" via prefix maximum on compressed ranks
     * - "best greater value on the right" via reversed-rank prefix maximum
     */
    static class FenwickMax {
        private final long[] tree;

        /**
         * Creates a Fenwick tree with indices from 1 to size.
         *
         * @param size the number of usable indices
         */
        FenwickMax(int size) {
            this.tree = new long[size + 2];
        }

        /**
         * Updates the Fenwick tree at a given index with a candidate value.
         * The tree stores maximums, so we keep the larger of existing and new value.
         *
         * @param index the 1-based compressed index to update
         * @param value the candidate value to merge into the structure
         * @return nothing
         * Time complexity: O(log n)
         * Space complexity: O(1)
         */
        public void update(int index, long value) {
            while (index < tree.length) {
                tree[index] = Math.max(tree[index], value);
                index += index & -index;
            }
        }

        /**
         * Returns the maximum value stored in the prefix range [1..index].
         *
         * If no value has been inserted into that prefix, this returns 0.
         * In this problem, ratings are always positive, so 0 safely means "not found".
         *
         * @param index the 1-based prefix end
         * @return the maximum value in the prefix [1..index], or 0 if none exists
         * Time complexity: O(log n)
         * Space complexity: O(1)
         */
        public long query(int index) {
            long result = 0;

            while (index > 0) {
                result = Math.max(result, tree[index]);
                index -= index & -index;
            }

            return result;
        }
    }
}