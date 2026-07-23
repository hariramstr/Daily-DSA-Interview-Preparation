/*
Problem Title: Maximum Score from Picking a Centered Product Trio

Problem Description:
You are given an integer array ratings where ratings[i] is the popularity score of the i-th product in a catalog.
A valid centered product trio is formed by choosing three indices (l, c, r) such that:

- l < c < r
- ratings[l] < ratings[c]
- ratings[r] < ratings[c]

In other words, the center product must have a strictly higher rating than one product on its left
and one product on its right.

The score of such a trio is:
ratings[l] + ratings[c] + ratings[r]

Return the maximum possible score among all valid centered product trios.
If no valid trio exists, return -1.

Constraints:
- 3 <= ratings.length <= 100000
- 1 <= ratings[i] <= 1000000000
- All values fit in 64-bit signed integers when summed

Examples:
1)
Input: ratings = [4, 9, 2, 7, 3]
Output: 16

Explanation:
Valid choices include:
- (0, 1, 2) => 4 + 9 + 2 = 15
- (0, 1, 4) => 4 + 9 + 3 = 16
- (2, 3, 4) => 2 + 7 + 3 = 12
Maximum is 16.

2)
Input: ratings = [1, 2, 3, 4]
Output: -1

Explanation:
No index has a smaller value on both left and right, so no valid trio exists.
*/

import java.util.*;

public class Solution {

    /**
     * Computes the maximum score of a valid centered product trio.
     *
     * The key idea is:
     * For each index c used as the center, we want:
     * - the largest value on the left side that is still strictly smaller than ratings[c]
     * - the largest value on the right side that is still strictly smaller than ratings[c]
     *
     * Why the largest smaller values?
     * Because the center value is fixed once c is chosen, so to maximize:
     * left + center + right
     * we should choose the biggest valid left and biggest valid right.
     *
     * To do this efficiently for every center:
     * 1. Coordinate-compress the values, because ratings can be as large as 1e9.
     * 2. Use a Fenwick Tree (Binary Indexed Tree) that stores maximum values.
     * 3. Sweep from left to right to compute the best valid left value for each index.
     * 4. Sweep from right to left to compute the best valid right value for each index.
     * 5. Combine the results.
     *
     * @param ratings the array of product popularity scores
     * @return the maximum trio score, or -1 if no valid trio exists
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long maximumScore(int[] ratings) {
        int n = ratings.length;

        // Step 1:
        // Coordinate compression.
        //
        // We need to query:
        // "What is the maximum value seen so far that is strictly less than ratings[i]?"
        //
        // Fenwick Trees work naturally on indices 1..m, so we compress the actual rating values
        // into sorted ranks.
        int[] sortedUnique = buildSortedUnique(ratings);

        // These arrays will store, for each position i:
        // - bestLeft[i]  = largest value on the left of i that is < ratings[i]
        // - bestRight[i] = largest value on the right of i that is < ratings[i]
        //
        // If no such value exists, we store -1.
        long[] bestLeft = new long[n];
        long[] bestRight = new long[n];
        Arrays.fill(bestLeft, -1L);
        Arrays.fill(bestRight, -1L);

        // Step 2:
        // Left-to-right sweep.
        //
        // Fenwick tree stores the maximum actual rating value encountered so far for each compressed rank.
        // For current ratings[i], we query all ranks strictly smaller than its rank.
        FenwickMax leftFenwick = new FenwickMax(sortedUnique.length);

        for (int i = 0; i < n; i++) {
            int rank = rankOf(sortedUnique, ratings[i]);

            // Query the maximum value among all compressed ranks < current rank.
            // That means all actual values strictly smaller than ratings[i].
            long bestSmallerOnLeft = leftFenwick.query(rank - 1);
            bestLeft[i] = bestSmallerOnLeft;

            // Now insert the current value into the Fenwick tree so future positions can use it.
            leftFenwick.update(rank, ratings[i]);
        }

        // Step 3:
        // Right-to-left sweep.
        //
        // Same logic, but now we are looking at values to the right of each index.
        FenwickMax rightFenwick = new FenwickMax(sortedUnique.length);

        for (int i = n - 1; i >= 0; i--) {
            int rank = rankOf(sortedUnique, ratings[i]);

            // Again, query all strictly smaller values.
            long bestSmallerOnRight = rightFenwick.query(rank - 1);
            bestRight[i] = bestSmallerOnRight;

            // Insert current value for future indices to the left.
            rightFenwick.update(rank, ratings[i]);
        }

        // Step 4:
        // Try every index as the center.
        //
        // A valid center must have:
        // - some smaller value on the left
        // - some smaller value on the right
        //
        // If both exist, compute the score and keep the maximum.
        long answer = -1L;

        for (int c = 0; c < n; c++) {
            if (bestLeft[c] != -1L && bestRight[c] != -1L) {
                long score = bestLeft[c] + ratings[c] + bestRight[c];
                answer = Math.max(answer, score);
            }
        }

        return answer;
    }

    /**
     * Builds a sorted array of unique values from the input.
     *
     * This is used for coordinate compression so that large rating values
     * can be mapped into a compact range [1..m].
     *
     * @param ratings the original ratings array
     * @return a sorted array containing each distinct value exactly once
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int[] buildSortedUnique(int[] ratings) {
        int[] copy = Arrays.copyOf(ratings, ratings.length);
        Arrays.sort(copy);

        int uniqueCount = 0;
        for (int value : copy) {
            if (uniqueCount == 0 || copy[uniqueCount - 1] != value) {
                copy[uniqueCount++] = value;
            }
        }

        return Arrays.copyOf(copy, uniqueCount);
    }

    /**
     * Returns the 1-based compressed rank of a value inside the sorted unique array.
     *
     * Because the value is guaranteed to exist in the sorted unique array,
     * binarySearch will return a valid non-negative index.
     *
     * We add 1 because Fenwick Trees are typically implemented using 1-based indexing.
     *
     * @param sortedUnique sorted array of distinct values
     * @param value the value whose rank we want
     * @return the 1-based rank of the value
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public int rankOf(int[] sortedUnique, int value) {
        return Arrays.binarySearch(sortedUnique, value) + 1;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding called methods
     * Space complexity: O(1) excluding called methods
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] ratings1 = {4, 9, 2, 7, 3};
        long result1 = solution.maximumScore(ratings1);
        System.out.println("Input: " + Arrays.toString(ratings1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 16");
        System.out.println();

        int[] ratings2 = {1, 2, 3, 4};
        long result2 = solution.maximumScore(ratings2);
        System.out.println("Input: " + Arrays.toString(ratings2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: -1");
        System.out.println();

        int[] ratings3 = {5, 1, 6, 2, 7, 3};
        long result3 = solution.maximumScore(ratings3);
        System.out.println("Input: " + Arrays.toString(ratings3));
        System.out.println("Output: " + result3);
        System.out.println("One valid maximum example explanation: center 7 with left 6 and right 3 => 16");
    }

    /**
     * Fenwick Tree (Binary Indexed Tree) specialized for prefix maximum queries.
     *
     * Instead of storing sums, this structure stores the maximum value seen so far.
     *
     * Supported operations:
     * - update(index, value): tree[index] = max(tree[index], value)
     * - query(index): maximum value in range [1..index]
     *
     * We use -1 as the identity / "not found" value because all ratings are positive.
     */
    static class FenwickMax {
        private final long[] tree;

        /**
         * Creates a Fenwick tree capable of storing indices from 1 to size.
         *
         * @param size number of usable positions
         * @return nothing
         * Time complexity: O(size)
         * Space complexity: O(size)
         */
        public FenwickMax(int size) {
            this.tree = new long[size + 1];
            Arrays.fill(this.tree, -1L);
        }

        /**
         * Updates the Fenwick tree at a given index with a value.
         *
         * Since this is a max Fenwick tree, we keep the larger of the existing value
         * and the new value at all relevant Fenwick nodes.
         *
         * @param index 1-based index to update
         * @param value value to incorporate
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
         * Queries the maximum value in the prefix range [1..index].
         *
         * If no value has been inserted in that prefix, returns -1.
         *
         * @param index 1-based inclusive prefix end
         * @return maximum value in the prefix, or -1 if none exists
         * Time complexity: O(log n)
         * Space complexity: O(1)
         */
        public long query(int index) {
            long result = -1L;
            while (index > 0) {
                result = Math.max(result, tree[index]);
                index -= index & -index;
            }
            return result;
        }
    }
}