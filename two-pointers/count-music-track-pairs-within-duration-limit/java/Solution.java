import java.util.*;

/*
 * Title: Count Music Track Pairs Within Duration Limit
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * You are given an integer array durations where durations[i] is the length of the i-th music track in seconds,
 * and an integer limit. A pair of distinct tracks (i, j) is considered playable in one short session if
 * i < j and durations[i] + durations[j] <= limit.
 *
 * Return the total number of playable pairs.
 *
 * The order of tracks in the input array does not matter for pairing, and each pair should be counted at most once.
 * You are not asked to list the pairs, only to count them efficiently.
 *
 * A brute-force solution that checks every pair takes O(n^2) time and may be too slow for large inputs.
 * Think about how sorting the array can help you use two pointers to count many valid pairs at once.
 *
 * Constraints:
 * - 1 <= durations.length <= 2 * 10^5
 * - 1 <= durations[i] <= 10^9
 * - 1 <= limit <= 2 * 10^9
 * - The answer may be large, so use a 64-bit integer type if needed.
 *
 * Example 1:
 * Input: durations = [120, 90, 150, 60], limit = 210
 * Output: 4
 * Explanation: Valid pairs are (120,90), (120,60), (90,60), and (150,60). The pair (150,90) exceeds the limit.
 *
 * Example 2:
 * Input: durations = [40, 40, 40, 100], limit = 80
 * Output: 3
 * Explanation: Any pair formed by two 40-second tracks is valid. There are 3 such pairs, and no pair involving 100 fits within the limit.
 */

public class Solution {

    /**
     * Counts how many distinct pairs of tracks have a total duration less than or equal to the given limit.
     *
     * The method uses a classic two-pointer strategy:
     * 1. Sort the durations.
     * 2. Keep one pointer at the smallest value and one at the largest value.
     * 3. If the current pair fits within the limit, then every element between the left pointer and right pointer
     *    can also pair with the left pointer, because the array is sorted.
     * 4. Count all those pairs at once and move the left pointer forward.
     * 5. Otherwise, move the right pointer backward to reduce the sum.
     *
     * @param durations the array of track durations in seconds
     * @param limit the maximum allowed total duration for a playable pair
     * @return the total number of playable pairs as a long
     *
     * Time complexity: O(n log n), due to sorting the array. The two-pointer scan itself is O(n).
     * Space complexity: O(1) extra space beyond the sorting implementation details used by Java's library.
     */
    public long countPlayablePairs(int[] durations, int limit) {
        // Defensive handling is not strictly necessary because constraints guarantee at least one element,
        // but this makes the method safer and easier to reuse.
        if (durations == null || durations.length < 2) {
            return 0L;
        }

        // Sort the array so that we can reason about smaller and larger values efficiently.
        Arrays.sort(durations);

        // 'left' starts at the smallest duration.
        int left = 0;

        // 'right' starts at the largest duration.
        int right = durations.length - 1;

        // Use long because the number of valid pairs can be large.
        long count = 0L;

        // Continue until the two pointers cross.
        // We only consider pairs where left < right.
        while (left < right) {
            // Use long for the sum to be completely safe, even though int would still fit here.
            long sum = (long) durations[left] + durations[right];

            // Case 1:
            // If the smallest remaining value plus the largest remaining value is within the limit,
            // then the smallest value can pair with EVERY element from left+1 up to right.
            //
            // Why?
            // Because the array is sorted:
            // durations[left] + durations[k] <= durations[left] + durations[right] <= limit
            // for every k in [left+1, right].
            //
            // That means we can count all these pairs in one step instead of checking them one by one.
            if (sum <= limit) {
                count += (right - left);

                // Move left forward to consider the next smallest track.
                left++;
            } else {
                // Case 2:
                // If the current sum is too large, then durations[right] is too large to pair with durations[left].
                // Since durations[right] is the largest remaining value, it also cannot pair with any value
                // to the right of 'left' that is >= durations[left] and still improve the situation.
                //
                // So we decrease 'right' to try a smaller largest value.
                right--;
            }
        }

        return count;
    }

    /**
     * Creates a copy of the input array and counts playable pairs on the copy.
     *
     * This helper is useful in demonstrations when you want to preserve the original input order,
     * because the main algorithm sorts the array in-place.
     *
     * @param durations the original array of track durations
     * @param limit the maximum allowed total duration for a playable pair
     * @return the total number of playable pairs as a long
     *
     * Time complexity: O(n log n), because it copies the array in O(n) and sorts in O(n log n).
     * Space complexity: O(n), due to the copied array.
     */
    public long countPlayablePairsWithoutModifyingInput(int[] durations, int limit) {
        if (durations == null) {
            return 0L;
        }

        int[] copy = Arrays.copyOf(durations, durations.length);
        return countPlayablePairs(copy, limit);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(1) for the fixed demonstration setup, excluding the algorithm calls.
     * Space complexity: O(1), excluding the arrays created for demonstration.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // durations = [120, 90, 150, 60], limit = 210
        // Valid pairs:
        // (120,90) = 210
        // (120,60) = 180
        // (90,60) = 150
        // (150,60) = 210
        // Invalid:
        // (150,90) = 240
        // Total = 4
        int[] durations1 = {120, 90, 150, 60};
        int limit1 = 210;
        long result1 = solution.countPlayablePairsWithoutModifyingInput(durations1, limit1);
        System.out.println("Example 1 result: " + result1);

        // Example 2:
        // durations = [40, 40, 40, 100], limit = 80
        // Valid pairs are all pairs among the three 40s:
        // choose any 2 out of 3 => 3 pairs
        // Any pair involving 100 is too large.
        // Total = 3
        int[] durations2 = {40, 40, 40, 100};
        int limit2 = 80;
        long result2 = solution.countPlayablePairsWithoutModifyingInput(durations2, limit2);
        System.out.println("Example 2 result: " + result2);

        // Additional quick sanity checks for beginners:

        // No valid pair because the only possible pair exceeds the limit.
        int[] durations3 = {100, 200};
        int limit3 = 150;
        long result3 = solution.countPlayablePairsWithoutModifyingInput(durations3, limit3);
        System.out.println("Additional example 1 result: " + result3);

        // Every pair is valid.
        int[] durations4 = {10, 20, 30, 40};
        int limit4 = 100;
        long result4 = solution.countPlayablePairsWithoutModifyingInput(durations4, limit4);
        System.out.println("Additional example 2 result: " + result4);
    }
}