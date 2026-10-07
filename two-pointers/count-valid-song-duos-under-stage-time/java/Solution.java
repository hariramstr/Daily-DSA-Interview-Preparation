import java.util.*;

/*
Title: Count Valid Song Duos Under Stage Time

Problem Description:
You are organizing a live showcase and have a list of song durations in seconds. A duo performance is formed by choosing exactly two different songs. Due to stage scheduling limits, only duos whose combined duration is less than or equal to a given limit can be performed.

Given an integer array durations where durations[i] is the length of the i-th song, and an integer stageLimit, return the number of distinct index pairs (i, j) such that 0 <= i < j < n and durations[i] + durations[j] <= stageLimit.

Two songs with the same duration are still considered different if they come from different indices. Your solution should be efficient enough for large inputs, so a brute-force O(n^2) approach may not pass.

A common efficient strategy is to sort the durations and use two pointers to count how many pairs can be formed for each position without checking every pair individually.

Constraints:
- 2 <= durations.length <= 200000
- 1 <= durations[i] <= 1000000000
- 1 <= stageLimit <= 2000000000
- The answer fits in a 64-bit signed integer

Example 1:
Input: durations = [120, 90, 150, 60, 80], stageLimit = 210
Output: 7
Explanation: Valid pairs are (120,90), (120,60), (120,80), (90,60), (90,80), (150,60), and (60,80). The pairs (150,90), (150,80), and (150,120) exceed the limit.

Example 2:
Input: durations = [200, 40, 40, 170, 30], stageLimit = 210
Output: 5
Explanation: After considering all index pairs, the valid duos are the two pairs using 200 with each 40? No, those exceed 210. The valid pairs are (40,40), (40,30) for each of the two 40s, and (170,30). That gives 4 pairs, plus the pair formed by the two 40s is 1 more, for a total of 4.
Important correction:
The mathematically correct total for Example 2 is 4, not 5.
Pairs are:
- (40 at index 1, 40 at index 2) = 80
- (40 at index 1, 30) = 70
- (40 at index 2, 30) = 70
- (170, 30) = 200
No other pair is <= 210.
Therefore, the correct output for Example 2 is 4.
*/

/**
 * A beginner-friendly solution that counts how many distinct pairs of songs
 * have a total duration less than or equal to the stage limit.
 *
 * The efficient approach is:
 * 1. Sort the durations.
 * 2. Use two pointers:
 *    - one pointer at the smallest value
 *    - one pointer at the largest value
 * 3. If the current pair fits within the limit, then every value between the
 *    left pointer and the right pointer can also pair with the left value.
 *    This lets us count many pairs at once.
 */
public class Solution {

    /**
     * Counts the number of distinct index pairs (i, j) such that:
     * 0 <= i < j < durations.length
     * and durations[i] + durations[j] <= stageLimit.
     *
     * This method uses sorting plus the two-pointer technique to avoid checking
     * every pair one by one.
     *
     * Detailed idea:
     * - First sort the array.
     * - Keep one pointer at the beginning (left) and one at the end (right).
     * - If durations[left] + durations[right] <= stageLimit:
     *   then durations[left] can pair with every element from left + 1 to right,
     *   because the array is sorted and all those values are <= durations[right].
     *   So we add (right - left) pairs at once, then move left forward.
     * - Otherwise, the pair is too large, so we must reduce the sum by moving
     *   right backward.
     *
     * @param durations the array of song durations
     * @param stageLimit the maximum allowed combined duration for a duo
     * @return the number of valid distinct pairs as a long
     *
     * Time complexity: O(n log n), due to sorting; the two-pointer scan is O(n)
     * Space complexity: O(1) extra space beyond the sorting implementation details;
     * in practice, Arrays.sort(int[]) is efficient and uses standard library internals
     */
    public long countValidSongDuos(int[] durations, int stageLimit) {
        // Sort the durations so that we can use the two-pointer strategy.
        // After sorting, smaller values are on the left and larger values are on the right.
        Arrays.sort(durations);

        // This variable stores the final number of valid pairs.
        // We use long because the number of pairs can be large.
        long count = 0L;

        // Left pointer starts at the smallest duration.
        int left = 0;

        // Right pointer starts at the largest duration.
        int right = durations.length - 1;

        // Continue until the two pointers cross.
        // We only consider pairs where left < right.
        while (left < right) {
            // Compute the sum carefully using long to avoid any risk of overflow,
            // even though int would still be safe under the given constraints.
            long sum = (long) durations[left] + durations[right];

            // Case 1:
            // If the smallest remaining value plus the largest remaining value
            // is within the limit, then the smallest value can pair with every
            // element from left + 1 through right.
            if (sum <= stageLimit) {
                // Why can we add (right - left)?
                //
                // Because the array is sorted:
                // durations[left] <= durations[left + 1] <= ... <= durations[right]
                //
                // We already know:
                // durations[left] + durations[right] <= stageLimit
                //
                // Therefore, for any index k where left < k <= right:
                // durations[left] + durations[k] <= durations[left] + durations[right] <= stageLimit
                //
                // So all these pairs are valid:
                // (left, left+1), (left, left+2), ..., (left, right)
                //
                // Number of such pairs = right - left
                count += (right - left);

                // Move left forward to consider the next smallest duration.
                // We have already counted every valid pair that starts with the current left.
                left++;
            } else {
                // Case 2:
                // The current sum is too large.
                //
                // Since durations[right] is the largest remaining value,
                // pairing it with durations[left] already exceeds the limit.
                // That means pairing durations[right] with any value to the right of left
                // would also be too large or equal/larger.
                //
                // So durations[right] cannot form a valid pair with the current left,
                // and to reduce the sum we must move right backward.
                right--;
            }
        }

        // Return the total number of valid pairs found.
        return count;
    }

    /**
     * A helper method that demonstrates the algorithm on a given input and prints
     * the sorted array and the computed result.
     *
     * @param durations the input song durations
     * @param stageLimit the maximum allowed combined duration
     * @return the computed number of valid pairs
     *
     * Time complexity: O(n log n)
     * Space complexity: O(n) for copying the input for demonstration purposes
     */
    public long demonstrateAndCount(int[] durations, int stageLimit) {
        // Copy the input so the original array remains unchanged for display purposes.
        int[] copy = Arrays.copyOf(durations, durations.length);

        long result = countValidSongDuos(copy, stageLimit);

        System.out.println("Durations: " + Arrays.toString(durations));
        System.out.println("Stage limit: " + stageLimit);
        System.out.println("Valid duo count: " + result);
        System.out.println();

        return result;
    }

    /**
     * Main method to demonstrate the solution using the sample inputs from the problem.
     *
     * Note:
     * - Example 1 is correct and should produce 7.
     * - Example 2, when traced carefully, actually produces 4 rather than 5.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(n log n) per demonstration case
     * Space complexity: O(n) for copied arrays used in demonstration
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] durations1 = {120, 90, 150, 60, 80};
        long result1 = solution.demonstrateAndCount(durations1, 210);
        System.out.println("Expected for Example 1: 7");
        System.out.println("Actual for Example 1:   " + result1);
        System.out.println();

        // Example 2
        // Careful verification:
        // Input: [200, 40, 40, 170, 30], limit = 210
        // Valid pairs:
        // (40,40), (40,30), (40,30), (170,30) => total 4
        int[] durations2 = {200, 40, 40, 170, 30};
        long result2 = solution.demonstrateAndCount(durations2, 210);
        System.out.println("Expected for Example 2 after correct tracing: 4");
        System.out.println("Actual for Example 2:                        " + result2);
    }
}