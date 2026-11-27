import java.util.*;

/*
Problem Title: Pair Dishes for a Shared Oven Slot

Problem Description:
A catering team has a list of dish preparation times, where times[i] is the number of minutes
needed to bake the i-th dish. The team wants to load exactly two dishes into a shared oven slot.
For safety and scheduling reasons, the combined baking time of the two chosen dishes must be at
least minTime and at most maxTime.

Your task is to return the number of distinct pairs of dishes (i, j) such that
0 <= i < j < n and minTime <= times[i] + times[j] <= maxTime.

Two dishes are considered distinct if they come from different indices, even if their baking
times are equal. You may reorder the array internally if needed, but the answer must count pairs
from the original set of dishes.

Design an algorithm that is efficient for large inputs. A brute-force O(n^2) solution will be
too slow under the given constraints.

Constraints:
- 2 <= times.length <= 2 * 10^5
- 1 <= times[i] <= 10^9
- 1 <= minTime <= maxTime <= 2 * 10^9
- The answer may be large, so use a 64-bit integer type where necessary.

Example 1:
Input: times = [2, 5, 3, 7, 4], minTime = 7, maxTime = 9
Output: 5

Example 2:
Input: times = [1, 1, 2, 2, 3], minTime = 3, maxTime = 4
Output: 8

Key Idea:
Count:
1) number of pairs with sum <= maxTime
2) number of pairs with sum <= (minTime - 1)

Then subtract:
pairs in [minTime, maxTime] = pairs <= maxTime - pairs <= (minTime - 1)

This can be done efficiently after sorting using a two-pointer technique.
*/

public class Solution {

    /**
     * Returns the number of distinct index pairs (i, j) such that:
     * i < j and minTime <= times[i] + times[j] <= maxTime.
     *
     * The method sorts a copy of the array and uses the standard trick:
     * answer = countPairsWithSumAtMost(maxTime) - countPairsWithSumAtMost(minTime - 1)
     *
     * @param times the array of dish preparation times
     * @param minTime the minimum allowed combined baking time
     * @param maxTime the maximum allowed combined baking time
     * @return the number of valid distinct pairs
     * Time complexity: O(n log n) due to sorting, where n = times.length
     * Space complexity: O(n) because we sort a copy of the input array
     */
    public long countValidPairs(int[] times, int minTime, int maxTime) {
        // We sort a copy so the original input remains unchanged.
        int[] sorted = Arrays.copyOf(times, times.length);
        Arrays.sort(sorted);

        // Count all pairs whose sum is at most maxTime.
        long atMostMax = countPairsWithSumAtMost(sorted, maxTime);

        // Count all pairs whose sum is strictly less than minTime,
        // which is equivalent to sum <= minTime - 1.
        long belowMin = countPairsWithSumAtMost(sorted, (long) minTime - 1);

        // Subtracting gives exactly the pairs whose sums lie in [minTime, maxTime].
        return atMostMax - belowMin;
    }

    /**
     * Counts how many distinct pairs (i, j), with i < j, have:
     * sortedTimes[i] + sortedTimes[j] <= limit
     *
     * This method assumes the input array is already sorted in non-decreasing order.
     *
     * Two-pointer idea:
     * - Start one pointer at the left end and one at the right end.
     * - If sortedTimes[left] + sortedTimes[right] <= limit, then every index from
     *   left+1 through right can pair with left, because the array is sorted.
     *   That contributes (right - left) pairs at once.
     * - Then move left forward to try more pairs.
     * - Otherwise, if the sum is too large, move right backward to reduce the sum.
     *
     * @param sortedTimes the sorted array of dish preparation times
     * @param limit the maximum allowed sum for a pair
     * @return the number of pairs whose sum is at most limit
     * Time complexity: O(n), where n = sortedTimes.length
     * Space complexity: O(1) extra space
     */
    public long countPairsWithSumAtMost(int[] sortedTimes, long limit) {
        // If the limit is smaller than the smallest possible positive sum,
        // there can be no valid pairs. This check is optional but makes intent clear.
        if (sortedTimes.length < 2) {
            return 0L;
        }

        long count = 0L;

        // left starts at the smallest value, right at the largest value.
        int left = 0;
        int right = sortedTimes.length - 1;

        // We only consider pairs where left < right.
        while (left < right) {
            // Use long for the sum to avoid any risk of integer overflow.
            long sum = (long) sortedTimes[left] + sortedTimes[right];

            if (sum <= limit) {
                // IMPORTANT OBSERVATION:
                // Because the array is sorted:
                // sortedTimes[left] + sortedTimes[k] <= sortedTimes[left] + sortedTimes[right]
                // for every k in [left + 1, right].
                //
                // So if the current pair (left, right) is valid, then ALL pairs:
                // (left, left+1), (left, left+2), ..., (left, right)
                // are also valid.
                //
                // That means we can add all of them in one step:
                count += (right - left);

                // After counting all pairs using this left index, move left forward
                // to look for new pairs starting from the next element.
                left++;
            } else {
                // If the sum is too large, we must reduce it.
                // Since the array is sorted, moving right leftward decreases the sum.
                right--;
            }
        }

        return count;
    }

    /**
     * Brute-force helper used only for demonstration/testing on small inputs.
     * It checks every pair and counts those whose sum lies in [minTime, maxTime].
     *
     * This is not efficient for large inputs, but it is useful to verify correctness
     * on examples and small custom tests.
     *
     * @param times the array of dish preparation times
     * @param minTime the minimum allowed combined baking time
     * @param maxTime the maximum allowed combined baking time
     * @return the number of valid pairs computed by brute force
     * Time complexity: O(n^2)
     * Space complexity: O(1)
     */
    public long countValidPairsBruteForce(int[] times, int minTime, int maxTime) {
        long count = 0L;

        for (int i = 0; i < times.length; i++) {
            for (int j = i + 1; j < times.length; j++) {
                long sum = (long) times[i] + times[j];
                if (sum >= minTime && sum <= maxTime) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: Depends on the demonstration inputs
     * Space complexity: Depends on the demonstration inputs
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] times1 = {2, 5, 3, 7, 4};
        int minTime1 = 7;
        int maxTime1 = 9;

        long result1 = solution.countValidPairs(times1, minTime1, maxTime1);
        long check1 = solution.countValidPairsBruteForce(times1, minTime1, maxTime1);

        System.out.println("Example 1:");
        System.out.println("times = " + Arrays.toString(times1));
        System.out.println("minTime = " + minTime1 + ", maxTime = " + maxTime1);
        System.out.println("Output = " + result1);
        System.out.println("Brute-force check = " + check1);
        System.out.println("Expected = 5");
        System.out.println();

        // Sample 2
        int[] times2 = {1, 1, 2, 2, 3};
        int minTime2 = 3;
        int maxTime2 = 4;

        long result2 = solution.countValidPairs(times2, minTime2, maxTime2);
        long check2 = solution.countValidPairsBruteForce(times2, minTime2, maxTime2);

        System.out.println("Example 2:");
        System.out.println("times = " + Arrays.toString(times2));
        System.out.println("minTime = " + minTime2 + ", maxTime = " + maxTime2);
        System.out.println("Output = " + result2);
        System.out.println("Brute-force check = " + check2);
        System.out.println("Expected = 8");
        System.out.println();

        // Additional quick sanity test
        int[] times3 = {10, 20, 30, 40};
        int minTime3 = 50;
        int maxTime3 = 60;

        long result3 = solution.countValidPairs(times3, minTime3, maxTime3);
        long check3 = solution.countValidPairsBruteForce(times3, minTime3, maxTime3);

        System.out.println("Additional Test:");
        System.out.println("times = " + Arrays.toString(times3));
        System.out.println("minTime = " + minTime3 + ", maxTime = " + maxTime3);
        System.out.println("Output = " + result3);
        System.out.println("Brute-force check = " + check3);
    }
}