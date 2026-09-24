import java.util.*;

/*
 * Title: Count Bin Pairs Within Volume Range
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * A warehouse stores reusable bins, and each bin has a volume capacity represented by an integer
 * in the array volumes. You are also given two integers low and high. A pair of bins (i, j) is
 * considered compatible if i < j and the combined volume volumes[i] + volumes[j] is within the
 * inclusive range [low, high].
 *
 * Your task is to return the total number of compatible pairs.
 *
 * The input array is not guaranteed to be sorted. Because the warehouse may contain a large number
 * of bins, an O(n^2) solution may be too slow. Design an algorithm that efficiently counts all
 * valid pairs.
 *
 * Two bins are distinct if they come from different indices, even if they have the same volume.
 * Be careful not to double-count pairs. The expected solution should take advantage of sorting and
 * a two-pointer counting strategy.
 *
 * Constraints:
 * - 1 <= volumes.length <= 100000
 * - 0 <= volumes[i] <= 1000000000
 * - 0 <= low <= high <= 2000000000
 * - The answer may not fit in a 32-bit integer, so use a 64-bit integer type where needed.
 *
 * Example 1:
 * Input: volumes = [4, 1, 7, 3, 2], low = 5, high = 8
 * Output: 6
 *
 * Correct pair verification for Example 1:
 * Original array indices and values:
 * index 0 -> 4
 * index 1 -> 1
 * index 2 -> 7
 * index 3 -> 3
 * index 4 -> 2
 *
 * All index pairs (i, j), i < j:
 * (0,1) = 4 + 1 = 5   -> valid
 * (0,2) = 4 + 7 = 11  -> invalid
 * (0,3) = 4 + 3 = 7   -> valid
 * (0,4) = 4 + 2 = 6   -> valid
 * (1,2) = 1 + 7 = 8   -> valid
 * (1,3) = 1 + 3 = 4   -> invalid
 * (1,4) = 1 + 2 = 3   -> invalid
 * (2,3) = 7 + 3 = 10  -> invalid
 * (2,4) = 7 + 2 = 9   -> invalid
 * (3,4) = 3 + 2 = 5   -> valid
 *
 * Total valid pairs = 5
 *
 * Note:
 * The originally stated output "6" is inconsistent with the listed sums and with the actual pair
 * enumeration. The correct answer for Example 1 is 5.
 *
 * Example 2:
 * Input: volumes = [2, 2, 2, 2], low = 4, high = 4
 * Output: 6
 * Explanation:
 * Every pair of bins has combined volume 4. Since there are 4 bins, the number of pairs is
 * 4 * 3 / 2 = 6.
 */

public class Solution {

    /**
     * Counts how many distinct index pairs (i, j), where i < j, have a sum within the inclusive
     * range [low, high].
     *
     * Core idea:
     * 1. Sort the array so that two-pointer counting becomes possible.
     * 2. Count how many pairs have sum <= high.
     * 3. Count how many pairs have sum <= low - 1.
     * 4. Subtract the two counts:
     *      pairs in [low, high] = pairs with sum <= high - pairs with sum <= (low - 1)
     *
     * This works because:
     * - "sum <= high" includes all pairs up to the upper bound.
     * - "sum <= low - 1" includes all pairs strictly below the lower bound.
     * - Their difference leaves exactly the pairs inside the desired inclusive range.
     *
     * @param volumes the array of bin volumes; bins are distinct by index
     * @param low the inclusive lower bound for a valid pair sum
     * @param high the inclusive upper bound for a valid pair sum
     * @return the total number of compatible pairs as a long
     * Time complexity: O(n log n) due to sorting, plus O(n) for the two-pointer scans
     * Space complexity: O(1) extra space beyond the sorting implementation details
     */
    public long countCompatiblePairs(int[] volumes, int low, int high) {
        // Sorting is the key preprocessing step.
        // Once the array is sorted, we can efficiently count how many pairs are <= a target sum
        // using a left pointer and a right pointer.
        Arrays.sort(volumes);

        // Count all pairs whose sum is <= high.
        long atMostHigh = countPairsWithSumAtMost(volumes, high);

        // Count all pairs whose sum is <= low - 1.
        // We cast to long before subtracting to avoid any accidental issues and to keep the logic
        // clear when working near integer boundaries.
        long atMostBelowLow = countPairsWithSumAtMost(volumes, (long) low - 1);

        // The difference gives the number of pairs whose sum lies in [low, high].
        return atMostHigh - atMostBelowLow;
    }

    /**
     * Counts how many distinct index pairs (i, j), where i < j, have sum <= target.
     *
     * Detailed two-pointer reasoning:
     * - Because the array is sorted, if volumes[left] + volumes[right] <= target,
     *   then volumes[left] + volumes[k] <= target for every k in [left + 1, right].
     *   Why? Because volumes[k] <= volumes[right] in a sorted array.
     * - That means once a valid pair is found for a fixed left and current right,
     *   we can count all pairs (left, left+1), (left, left+2), ..., (left, right)
     *   in one step. That contributes (right - left) pairs.
     * - Then we move left forward, because we have already counted every valid pair
     *   that starts with the current left.
     *
     * On the other hand:
     * - If volumes[left] + volumes[right] > target, then the sum is too large.
     * - Since the array is sorted, using the current right with any index >= left
     *   will not help if we keep right fixed and increase left slowly in the wrong direction.
     * - The correct move is to decrease right, making the sum smaller.
     *
     * @param volumes the sorted array of bin volumes
     * @param target the maximum allowed pair sum
     * @return the number of pairs with sum <= target
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long countPairsWithSumAtMost(int[] volumes, long target) {
        // If target is negative, and all volumes are non-negative per constraints,
        // then no pair can possibly have sum <= target.
        if (target < 0) {
            return 0L;
        }

        long count = 0L;

        // Start with the widest possible pair range.
        int left = 0;
        int right = volumes.length - 1;

        // Continue while there are at least two distinct indices available.
        while (left < right) {
            // Use long for the sum to be completely safe.
            long sum = (long) volumes[left] + volumes[right];

            if (sum <= target) {
                // Very important observation:
                // Since the array is sorted, for this fixed 'left',
                // every index from left+1 up to right forms a valid pair with 'left'.
                //
                // Number of such pairs:
                //   right - left
                //
                // Example:
                // sorted = [1, 2, 3, 4, 7], left = 0 (value 1), right = 3 (value 4)
                // sum = 5 <= target
                // Then pairs:
                // (0,1), (0,2), (0,3) are all valid if target >= 5
                // because 1+2 <= 1+4, and 1+3 <= 1+4.
                count += (right - left);

                // We have now counted every valid pair that starts with this 'left',
                // so move left forward to consider the next starting element.
                left++;
            } else {
                // Sum is too large, so we must reduce it.
                // Because the array is sorted, moving 'right' leftward is the correct way
                // to try a smaller partner value.
                right--;
            }
        }

        return count;
    }

    /**
     * Demonstrates the solution on sample inputs and prints the results.
     *
     * This main method also explicitly shows the corrected interpretation of Example 1:
     * the actual valid pair count is 5, not 6.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log n) across the demonstrations
     * Space complexity: O(1) extra space beyond sorting implementation details
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the prompt.
        // The prompt states output 6, but careful verification shows the correct answer is 5.
        int[] volumes1 = {4, 1, 7, 3, 2};
        int low1 = 5;
        int high1 = 8;
        long result1 = solution.countCompatiblePairs(volumes1.clone(), low1, high1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected after correct verification: 5");

        // Example 2 from the prompt.
        int[] volumes2 = {2, 2, 2, 2};
        int low2 = 4;
        int high2 = 4;
        long result2 = solution.countCompatiblePairs(volumes2.clone(), low2, high2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected: 6");

        // Additional quick demonstration.
        int[] volumes3 = {0, 5, 10, 15};
        int low3 = 10;
        int high3 = 15;
        long result3 = solution.countCompatiblePairs(volumes3.clone(), low3, high3);
        System.out.println("Additional example result: " + result3);
        System.out.println("Expected: 2");
    }
}