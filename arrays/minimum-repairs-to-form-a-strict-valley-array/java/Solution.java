import java.util.*;

/*
 * Title: Minimum Repairs to Form a Strict Valley Array
 * Difficulty: Hard
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array nums of length n. A strict valley array is an array for which
 * there exists an index p, where 0 < p < n - 1, such that values strictly decrease from the left
 * up to p and then strictly increase after p.
 *
 * Formally:
 * nums[0] > nums[1] > ... > nums[p] < nums[p+1] < ... < nums[n-1]
 *
 * The index p is called the valley position.
 *
 * In one repair operation, you may change any single element to any integer value.
 * Your task is to return the minimum number of repair operations needed to transform nums into
 * a strict valley array.
 *
 * You are not asked to construct the final array, only to compute the minimum number of elements
 * that must be modified.
 *
 * A position can remain unchanged only if its original value is compatible with some valid strict
 * valley configuration. Because changed values may be set arbitrarily, the main challenge is to
 * keep the largest possible set of original elements while preserving order and strict inequalities
 * around one valley position.
 *
 * Constraints:
 * - 3 <= n <= 200000
 * - -10^9 <= nums[i] <= 10^9
 * - The answer must be computed in O(n log n) time or better.
 *
 * Examples:
 * 1) nums = [9, 7, 5, 6, 8]
 *    Output: 0
 *    Explanation: Already a strict valley with p = 2.
 *
 * 2) nums = [4, 4, 3, 2, 5, 5]
 *    Output: 2
 *    Explanation: One optimal repaired array is [6, 4, 3, 2, 5, 7].
 */

public class Solution {

    /**
     * Computes the minimum number of repair operations needed to transform the given array
     * into a strict valley array.
     *
     * Core idea:
     * We want to keep as many original positions unchanged as possible.
     * If we choose some index i as the valley position and keep nums[i] unchanged, then:
     *
     * 1) On the left side, the kept unchanged values must form a strictly decreasing sequence
     *    ending at nums[i].
     *    Equivalently, if we look at the original values in left-to-right order, we need a
     *    strictly decreasing subsequence that ends at i.
     *
     * 2) On the right side, the kept unchanged values must form a strictly increasing sequence
     *    starting at nums[i].
     *    Equivalently, we need a strictly increasing subsequence that starts at i.
     *
     * If for each index i we know:
     * - leftDecEnd[i]  = length of the longest strictly decreasing subsequence ending at i
     * - rightIncStart[i] = length of the longest strictly increasing subsequence starting at i
     *
     * then choosing i as the valley and keeping nums[i] unchanged allows us to keep:
     * leftDecEnd[i] + rightIncStart[i] - 1
     * positions unchanged (subtract 1 because i is counted in both parts).
     *
     * The best valley is the one maximizing that quantity, subject to:
     * - at least one element on the left side is kept, so leftDecEnd[i] >= 2
     * - at least one element on the right side is kept, so rightIncStart[i] >= 2
     *
     * Finally:
     * minimum repairs = n - maximum unchanged positions
     *
     * We compute both arrays in O(n log n) using Fenwick trees after coordinate compression.
     *
     * @param nums the input integer array
     * @return the minimum number of repair operations needed
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int minimumRepairs(int[] nums) {
        int n = nums.length;

        // Coordinate compression:
        // We only care about relative ordering of values, not their exact magnitudes.
        // Compression maps every distinct value to a rank in [1..m].
        int[] sorted = nums.clone();
        Arrays.sort(sorted);

        int m = 0;
        int[] unique = new int[n];
        for (int value : sorted) {
            if (m == 0 || value != unique[m - 1]) {
                unique[m++] = value;
            }
        }

        int[] rank = new int[n];
        for (int i = 0; i < n; i++) {
            rank[i] = lowerBound(unique, m, nums[i]) + 1; // Fenwick tree uses 1-based indexing
        }

        // leftDecEnd[i]:
        // Longest strictly decreasing subsequence ending at index i.
        //
        // A decreasing subsequence ending at nums[i] can extend from a previous value > nums[i].
        // If we reverse the value axis using transformedRank = m - rank[i] + 1,
        // then "previous value > nums[i]" becomes "previous transformedRank < current transformedRank".
        //
        // So this becomes a standard LIS-style DP with Fenwick prefix maximum queries.
        int[] leftDecEnd = new int[n];
        FenwickMax leftTree = new FenwickMax(m);

        for (int i = 0; i < n; i++) {
            int transformed = m - rank[i] + 1;

            // Query best subsequence among previous elements with strictly smaller transformed rank,
            // which corresponds to strictly larger original value.
            int bestBefore = leftTree.query(transformed - 1);

            leftDecEnd[i] = bestBefore + 1;

            // Update current transformed rank with the new best length.
            leftTree.update(transformed, leftDecEnd[i]);
        }

        // rightIncStart[i]:
        // Longest strictly increasing subsequence starting at index i.
        //
        // We process from right to left.
        // For nums[i], we want a later value > nums[i].
        // Again use transformed rank so that "later value > nums[i]" becomes
        // "later transformedRank < current transformedRank".
        int[] rightIncStart = new int[n];
        FenwickMax rightTree = new FenwickMax(m);

        for (int i = n - 1; i >= 0; i--) {
            int transformed = m - rank[i] + 1;

            int bestAfter = rightTree.query(transformed - 1);

            rightIncStart[i] = bestAfter + 1;

            rightTree.update(transformed, rightIncStart[i]);
        }

        // Try every index as the valley position.
        // It is valid only if we can keep at least one unchanged element on each side.
        int maxKept = 0;
        for (int i = 1; i <= n - 2; i++) {
            if (leftDecEnd[i] >= 2 && rightIncStart[i] >= 2) {
                int kept = leftDecEnd[i] + rightIncStart[i] - 1;
                if (kept > maxKept) {
                    maxKept = kept;
                }
            }
        }

        // Because changed values can be chosen arbitrarily, any positions not in the chosen
        // valley-shaped subsequence can always be repaired to fit between the kept values.
        return n - maxKept;
    }

    /**
     * Returns the first index in the sorted unique prefix [0, size) whose value is
     * greater than or equal to target.
     *
     * This is the standard lower_bound operation.
     *
     * @param arr the sorted array containing unique values in its prefix
     * @param size the number of valid elements in arr
     * @param target the value to search for
     * @return the first index where arr[index] >= target
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public int lowerBound(int[] arr, int size, int target) {
        int left = 0;
        int right = size;
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
     * Demonstrates the solution on sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size * log n) for the demonstrated examples
     * Space complexity: O(n)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] nums1 = {9, 7, 5, 6, 8};
        int[] nums2 = {4, 4, 3, 2, 5, 5};

        System.out.println("Input: " + Arrays.toString(nums1));
        System.out.println("Minimum repairs: " + solution.minimumRepairs(nums1));
        System.out.println("Expected: 0");
        System.out.println();

        System.out.println("Input: " + Arrays.toString(nums2));
        System.out.println("Minimum repairs: " + solution.minimumRepairs(nums2));
        System.out.println("Expected: 2");
        System.out.println();

        int[] nums3 = {3, 2, 1};
        System.out.println("Input: " + Arrays.toString(nums3));
        System.out.println("Minimum repairs: " + solution.minimumRepairs(nums3));
        System.out.println();

        int[] nums4 = {1, 2, 3};
        System.out.println("Input: " + Arrays.toString(nums4));
        System.out.println("Minimum repairs: " + solution.minimumRepairs(nums4));
        System.out.println();

        int[] nums5 = {5, 1, 4};
        System.out.println("Input: " + Arrays.toString(nums5));
        System.out.println("Minimum repairs: " + solution.minimumRepairs(nums5));
        System.out.println();
    }

    /**
     * Fenwick tree (Binary Indexed Tree) supporting prefix maximum queries.
     *
     * Unlike the more common Fenwick tree for sums, this version stores maximum values.
     * It supports:
     * - update(index, value): tree[index] = max(tree[index], value)
     * - query(index): maximum value in range [1..index]
     */
    static class FenwickMax {
        private final int[] tree;

        /**
         * Creates a Fenwick tree of the given size.
         *
         * @param size number of indices supported, using 1-based indexing
         * @return nothing
         * Time complexity: O(size)
         * Space complexity: O(size)
         */
        FenwickMax(int size) {
            this.tree = new int[size + 2];
        }

        /**
         * Applies a max-update at the given index.
         *
         * @param index the 1-based index to update
         * @param value the candidate value
         * @return nothing
         * Time complexity: O(log n)
         * Space complexity: O(1)
         */
        void update(int index, int value) {
            int i = index;
            while (i < tree.length) {
                if (value > tree[i]) {
                    tree[i] = value;
                }
                i += i & -i;
            }
        }

        /**
         * Returns the maximum value in the prefix [1..index].
         *
         * @param index the 1-based right endpoint of the prefix
         * @return the maximum stored value in that prefix
         * Time complexity: O(log n)
         * Space complexity: O(1)
         */
        int query(int index) {
            int result = 0;
            int i = index;
            while (i > 0) {
                if (tree[i] > result) {
                    result = tree[i];
                }
                i -= i & -i;
            }
            return result;
        }
    }
}