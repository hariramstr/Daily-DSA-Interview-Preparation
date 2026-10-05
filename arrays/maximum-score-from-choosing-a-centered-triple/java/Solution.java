import java.util.*;

/*
 * Maximum Score from Choosing a Centered Triple
 *
 * Problem Description:
 * You are given an integer array nums representing signal strengths recorded over time.
 * A valid centered triple is a choice of three indices (i, j, k) such that:
 *   - i < j < k
 *   - nums[i] < nums[j]
 *   - nums[k] < nums[j]
 *
 * In other words, the middle element must be strictly greater than one element on its left
 * and one element on its right. The score of such a triple is:
 *
 *   nums[i] + nums[j] + nums[k]
 *
 * Return the maximum possible score among all valid centered triples.
 * If no valid centered triple exists, return -1.
 *
 * Constraints:
 *   - 3 <= nums.length <= 200000
 *   - 1 <= nums[i] <= 1000000000
 *
 * Notes:
 * A brute-force approach that checks all triples would be far too slow.
 * We need an efficient way to determine, for each possible center j:
 *   1) the largest value on the left that is still strictly smaller than nums[j]
 *   2) the largest value on the right that is still strictly smaller than nums[j]
 *
 * Key Insight:
 * For a fixed center nums[j], to maximize:
 *   left + nums[j] + right
 * we should choose:
 *   - the maximum valid left value smaller than nums[j]
 *   - the maximum valid right value smaller than nums[j]
 *
 * This solution computes:
 *   - bestLeft[j]: best valid left value for center j
 *   - bestRight[j]: best valid right value for center j
 *
 * Then it combines them to find the maximum score.
 *
 * Efficient Technique Used:
 * Coordinate compression + Fenwick Tree (Binary Indexed Tree) for prefix maximum queries.
 *
 * Why this works:
 * We need "maximum value strictly less than current value" among elements seen so far.
 * A Fenwick Tree can maintain prefix maximums efficiently after compressing values.
 *
 * Time Complexity:
 *   O(n log n)
 *
 * Space Complexity:
 *   O(n)
 */
public class Solution {

    /**
     * Computes the maximum score of a valid centered triple.
     *
     * A valid centered triple is (i, j, k) such that:
     *   - i < j < k
     *   - nums[i] < nums[j]
     *   - nums[k] < nums[j]
     *
     * For each index j, this method finds:
     *   - the largest value on the left side that is strictly smaller than nums[j]
     *   - the largest value on the right side that is strictly smaller than nums[j]
     *
     * If both exist, then j can serve as the center of a valid triple, and we compute:
     *   bestLeft + nums[j] + bestRight
     *
     * The maximum over all such centers is returned.
     *
     * @param nums the input array of signal strengths
     * @return the maximum possible score of a valid centered triple, or -1 if none exists
     *
     * Time complexity: O(n log n), where n is nums.length
     * Space complexity: O(n)
     */
    public long maximumScore(int[] nums) {
        int n = nums.length;

        // Step 1:
        // Coordinate compression is necessary because nums[i] can be as large as 1e9.
        // Fenwick Tree indices must be compact, so we map each distinct value to a rank in [1..m].
        int[] sortedUnique = compressValues(nums);

        // Step 2:
        // For every position j, compute the best valid value on the left:
        // the maximum nums[i] with i < j and nums[i] < nums[j].
        long[] bestLeft = computeBestSmallerOnLeft(nums, sortedUnique);

        // Step 3:
        // For every position j, compute the best valid value on the right:
        // the maximum nums[k] with k > j and nums[k] < nums[j].
        long[] bestRight = computeBestSmallerOnRight(nums, sortedUnique);

        // Step 4:
        // Combine the two precomputed arrays.
        // A center j is valid only if both a left and a right smaller value exist.
        long answer = -1;

        for (int j = 0; j < n; j++) {
            if (bestLeft[j] != -1 && bestRight[j] != -1) {
                long score = bestLeft[j] + nums[j] + bestRight[j];
                answer = Math.max(answer, score);
            }
        }

        return answer;
    }

    /**
     * Computes, for each index j, the maximum value appearing before j that is strictly smaller than nums[j].
     *
     * Example:
     * nums = [4, 9, 6, 3, 8]
     * bestLeft becomes:
     *   index 0 -> no left elements -> -1
     *   index 1 -> max value < 9 on left is 4 -> 4
     *   index 2 -> max value < 6 on left is 4 -> 4
     *   index 3 -> no left value < 3 -> -1
     *   index 4 -> max value < 8 on left is 6 -> 6
     *
     * We process from left to right.
     * A Fenwick Tree stores the maximum value seen so far for each compressed rank.
     * To find the best value strictly smaller than nums[j], we query ranks < rank(nums[j]).
     *
     * @param nums the input array
     * @param sortedUnique sorted array of unique values used for coordinate compression
     * @return an array bestLeft where bestLeft[j] is the largest value before j that is strictly smaller than nums[j], or -1 if none exists
     *
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long[] computeBestSmallerOnLeft(int[] nums, int[] sortedUnique) {
        int n = nums.length;
        long[] bestLeft = new long[n];

        // Fenwick Tree for prefix maximum queries.
        FenwickMax fenwick = new FenwickMax(sortedUnique.length);

        for (int j = 0; j < n; j++) {
            int rank = rankOf(nums[j], sortedUnique);

            // Query all ranks strictly smaller than current rank.
            // This gives the maximum value seen so far that is < nums[j].
            bestLeft[j] = fenwick.query(rank - 1);

            // Insert current value into the Fenwick Tree so future positions can use it.
            fenwick.update(rank, nums[j]);
        }

        return bestLeft;
    }

    /**
     * Computes, for each index j, the maximum value appearing after j that is strictly smaller than nums[j].
     *
     * We process from right to left.
     * The logic is symmetric to the left-side computation:
     *   - Fenwick Tree stores values seen on the right
     *   - query ranks strictly smaller than nums[j]
     *
     * Example:
     * nums = [4, 9, 6, 3, 8]
     * bestRight becomes:
     *   index 4 -> no right elements -> -1
     *   index 3 -> no right value < 3 -> -1
     *   index 2 -> max value < 6 on right is 3 -> 3
     *   index 1 -> max value < 9 on right is 8 -> 8
     *   index 0 -> max value < 4 on right is 3 -> 3
     *
     * @param nums the input array
     * @param sortedUnique sorted array of unique values used for coordinate compression
     * @return an array bestRight where bestRight[j] is the largest value after j that is strictly smaller than nums[j], or -1 if none exists
     *
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long[] computeBestSmallerOnRight(int[] nums, int[] sortedUnique) {
        int n = nums.length;
        long[] bestRight = new long[n];

        // Fenwick Tree for prefix maximum queries over values seen to the right.
        FenwickMax fenwick = new FenwickMax(sortedUnique.length);

        for (int j = n - 1; j >= 0; j--) {
            int rank = rankOf(nums[j], sortedUnique);

            // Query all ranks strictly smaller than current rank.
            bestRight[j] = fenwick.query(rank - 1);

            // Insert current value so positions further left can use it.
            fenwick.update(rank, nums[j]);
        }

        return bestRight;
    }

    /**
     * Builds a sorted array of unique values from nums.
     * This is used for coordinate compression.
     *
     * Example:
     * nums = [4, 9, 6, 3, 8]
     * result = [3, 4, 6, 8, 9]
     *
     * @param nums the input array
     * @return sorted array containing each distinct value exactly once
     *
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int[] compressValues(int[] nums) {
        int[] copy = Arrays.copyOf(nums, nums.length);
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
     * Returns the 1-based compressed rank of a value in the sorted unique array.
     *
     * Since sortedUnique contains all distinct values from nums in ascending order,
     * binary search can locate the exact position of value.
     *
     * Example:
     * sortedUnique = [3, 4, 6, 8, 9]
     * rankOf(6) = 3
     *
     * @param value the original value whose rank is needed
     * @param sortedUnique sorted array of distinct values
     * @return 1-based rank of value in sortedUnique
     *
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public int rankOf(int value, int[] sortedUnique) {
        int index = Arrays.binarySearch(sortedUnique, value);
        return index + 1;
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement.
     *
     * Expected outputs:
     *   Example 1: 19
     *   Example 2: -1
     *
     * Verification:
     * For nums = [4, 9, 6, 3, 8]
     *   Center 9 can use left 4 and right 8, score = 21? No, because indices matter:
     *   8 is at index 4, so (0,1,4) = (4,9,8) is valid and score is 21.
     *   This is larger than 19.
     *
     * Therefore, the mathematically correct answer for Example 1 under the stated rules is 21.
     *
     * The problem statement's example says 19 using (4,9,6), but (4,9,8) is also valid and better.
     * This implementation follows the formal problem definition and returns the true maximum.
     *
     * For nums = [5, 5, 5, 5]
     *   No strict smaller values exist on either side for any center, so answer = -1.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(n log n) for each demonstration call
     * Space complexity: O(n)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] nums1 = {4, 9, 6, 3, 8};
        int[] nums2 = {5, 5, 5, 5};

        System.out.println(solution.maximumScore(nums1));
        System.out.println(solution.maximumScore(nums2));

        // Additional quick checks for beginner-friendly demonstration.
        int[] nums3 = {1, 3, 2};
        int[] nums4 = {10, 1, 9, 2, 8};

        System.out.println(solution.maximumScore(nums3)); // valid triple: 1 + 3 + 2 = 6
        System.out.println(solution.maximumScore(nums4)); // best centered triple should be 10? No, center must be larger than both chosen sides
    }

    /**
     * Fenwick Tree (Binary Indexed Tree) specialized for prefix maximum queries.
     *
     * Standard Fenwick Trees are often used for prefix sums.
     * Here, instead of storing sums, we store maximum values.
     *
     * Supported operations:
     *   - update(index, value): set tree positions so that this value contributes to future prefix maximum queries
     *   - query(index): return the maximum value in the prefix [1..index]
     *
     * We use -1 as the "no value present" sentinel because all nums[i] are positive.
     */
    static class FenwickMax {
        private final long[] tree;

        /**
         * Creates a Fenwick Tree capable of storing ranks from 1 to size.
         *
         * @param size number of compressed ranks
         */
        FenwickMax(int size) {
            this.tree = new long[size + 1];
            Arrays.fill(this.tree, -1);
        }

        /**
         * Updates the Fenwick Tree at a given index with a value.
         * Since this is a max Fenwick Tree, each affected node stores the maximum seen so far.
         *
         * @param index 1-based compressed rank
         * @param value original array value to insert
         */
        void update(int index, long value) {
            while (index < tree.length) {
                tree[index] = Math.max(tree[index], value);
                index += index & -index;
            }
        }

        /**
         * Returns the maximum value in the prefix [1..index].
         * If no value has been inserted in that prefix, returns -1.
         *
         * @param index 1-based prefix end
         * @return maximum value in ranks [1..index], or -1 if none exists
         */
        long query(int index) {
            long result = -1;
            while (index > 0) {
                result = Math.max(result, tree[index]);
                index -= index & -index;
            }
            return result;
        }
    }
}