import java.util.*;

/*
 * Title: Minimum Oven Temperature for Batch Baking
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A bakery needs to finish several trays of pastries before the shop opens.
 * You are given an array batches, where batches[i] is the number of pastries
 * in the ith tray, and an integer hours representing the total number of whole
 * hours available. The bakery uses a programmable oven that can be set to a
 * single integer temperature level t for the entire night.
 *
 * If the oven is set to temperature t, then tray i takes ceil(batches[i] / t)
 * hours to finish because higher temperature bakes more pastries per hour.
 * Trays are baked one after another, not in parallel. Your task is to return
 * the minimum integer temperature t such that all trays can be completed within
 * hours hours.
 *
 * It is guaranteed that some temperature can finish the work within the given time.
 *
 * This problem is designed to reward recognizing a monotonic condition:
 * if a temperature t is sufficient, then any temperature greater than t is also
 * sufficient. That makes binary search over the answer space the intended approach.
 *
 * Constraints:
 * - 1 <= batches.length <= 100000
 * - 1 <= batches[i] <= 1000000000
 * - batches.length <= hours <= 1000000000
 * - 1 <= t <= max(batches)
 *
 * Example 1:
 * Input: batches = [12, 7, 18, 5], hours = 10
 * Output: 5
 * Explanation:
 * At temperature 5, total time is:
 * ceil(12/5) + ceil(7/5) + ceil(18/5) + ceil(5/5)
 * = 3 + 2 + 4 + 1
 * = 10
 * So it fits exactly. Any lower temperature needs more than 10 hours.
 *
 * Example 2:
 * Input: batches = [30, 11, 23, 4, 20], hours = 6
 * Output: 23
 * Explanation:
 * At temperature 23, total time is:
 * ceil(30/23) + ceil(11/23) + ceil(23/23) + ceil(4/23) + ceil(20/23)
 * = 2 + 1 + 1 + 1 + 1
 * = 6
 * At temperature 22, the third tray needs ceil(23/22) = 2 hours,
 * so the total becomes 7, which is too slow.
 */

public class Solution {

    /**
     * Finds the minimum integer oven temperature needed to finish all trays
     * within the given number of hours.
     *
     * The key idea is binary search on the answer:
     * - A lower temperature means slower baking, so total required hours increase.
     * - A higher temperature means faster baking, so total required hours decrease.
     * - Therefore, "can finish within hours" is a monotonic condition.
     *
     * @param batches an array where batches[i] is the number of pastries in the ith tray
     * @param hours the maximum total whole hours allowed to finish all trays
     * @return the minimum integer temperature that allows all trays to finish within hours
     * Time complexity: O(n log m), where n is batches.length and m is max value in batches
     * Space complexity: O(1), ignoring input storage
     */
    public int minOvenTemperature(int[] batches, int hours) {
        // The minimum possible temperature is 1.
        int left = 1;

        // The maximum necessary temperature is the size of the largest tray.
        // If temperature equals max(batches), every tray finishes in at most 1 hour.
        int right = getMaxBatch(batches);

        // This variable will store the best valid answer found so far.
        // Since the problem guarantees an answer exists, this will definitely be updated.
        int answer = right;

        // Standard binary search over the answer space [left, right].
        while (left <= right) {
            // Compute middle carefully to avoid overflow.
            int mid = left + (right - left) / 2;

            // Check whether this temperature is sufficient.
            if (canFinish(batches, hours, mid)) {
                // If mid works, it is a valid candidate answer.
                answer = mid;

                // But we want the MINIMUM valid temperature,
                // so continue searching on the left half.
                right = mid - 1;
            } else {
                // If mid does not work, it is too slow.
                // We must increase the temperature.
                left = mid + 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether all trays can be completed within the allowed hours
     * using the given oven temperature.
     *
     * For each tray with size batch:
     * required hours = ceil(batch / temperature)
     *
     * Instead of using floating-point math, we use the integer formula:
     * ceil(a / b) = (a + b - 1) / b
     *
     * We also use a long variable for total hours because the sum can exceed
     * the range of int during intermediate computation.
     *
     * @param batches an array of tray sizes
     * @param hours the allowed total hours
     * @param temperature the oven temperature being tested
     * @return true if all trays can finish within hours at this temperature; false otherwise
     * Time complexity: O(n), where n is batches.length
     * Space complexity: O(1)
     */
    public boolean canFinish(int[] batches, int hours, int temperature) {
        long totalHours = 0L;

        // Process trays one by one because baking is sequential.
        for (int batch : batches) {
            // Compute ceil(batch / temperature) using integer arithmetic.
            totalHours += (batch + (long) temperature - 1) / temperature;

            // Early stopping optimization:
            // As soon as total hours exceed the allowed limit,
            // we already know this temperature is not sufficient.
            if (totalHours > hours) {
                return false;
            }
        }

        // If we never exceeded the limit, this temperature works.
        return totalHours <= hours;
    }

    /**
     * Returns the maximum value in the batches array.
     * This is used as the upper bound for binary search.
     *
     * @param batches the array of tray sizes
     * @return the maximum tray size in the array
     * Time complexity: O(n), where n is batches.length
     * Space complexity: O(1)
     */
    public int getMaxBatch(int[] batches) {
        int max = 0;

        for (int batch : batches) {
            if (batch > max) {
                max = batch;
            }
        }

        return max;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It prints:
     * - the input arrays
     * - the allowed hours
     * - the computed minimum temperature
     *
     * Expected outputs:
     * Example 1 -> 5
     * Example 2 -> 23
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log m) per demonstration call
     * Space complexity: O(1), ignoring input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] batches1 = {12, 7, 18, 5};
        int hours1 = 10;
        int result1 = solution.minOvenTemperature(batches1, hours1);
        System.out.println("Example 1:");
        System.out.println("batches = " + Arrays.toString(batches1));
        System.out.println("hours = " + hours1);
        System.out.println("Minimum oven temperature = " + result1);
        System.out.println("Expected = 5");
        System.out.println();

        int[] batches2 = {30, 11, 23, 4, 20};
        int hours2 = 6;
        int result2 = solution.minOvenTemperature(batches2, hours2);
        System.out.println("Example 2:");
        System.out.println("batches = " + Arrays.toString(batches2));
        System.out.println("hours = " + hours2);
        System.out.println("Minimum oven temperature = " + result2);
        System.out.println("Expected = 23");
        System.out.println();

        // Additional quick verification prints.
        System.out.println("Verification:");
        System.out.println("Example 1 correct? " + (result1 == 5));
        System.out.println("Example 2 correct? " + (result2 == 23));
    }
}