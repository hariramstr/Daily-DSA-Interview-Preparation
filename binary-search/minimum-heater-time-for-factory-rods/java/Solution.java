import java.util.*;

/*
 * Title: Minimum Heater Time for Factory Rods
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A factory needs to soften metal rods before cutting them. You are given an array rods where rods[i]
 * is the length of the i-th rod, and an integer machines representing the number of identical heating
 * machines available. In one minute, a machine can heat exactly one rod segment of length t, where t
 * is the chosen heating time for that minute. If a rod has length L and the factory uses heating time t,
 * that rod requires ceil(L / t) machine-minutes to finish because it can be processed in multiple equal-sized
 * segments over time. Different rods may be processed in parallel across machines, but the total number of
 * machine-minutes available is machines.
 *
 * Your task is to find the minimum positive integer heating time t such that all rods can be fully processed
 * using at most machines total machine-minutes.
 *
 * In other words, find the smallest integer t where:
 *     sum(ceil(rods[i] / t)) <= machines
 *
 * This problem is designed to be solved efficiently. A brute-force search over all possible t values may be
 * too slow for large inputs, but the feasibility condition is monotonic: if a given heating time works,
 * any larger heating time also works.
 *
 * Constraints:
 * - 1 <= rods.length <= 100000
 * - 1 <= rods[i] <= 1000000000
 * - rods.length <= machines <= 1000000000
 * - t must be a positive integer
 *
 * Example 1:
 * Input: rods = [8, 5, 10], machines = 7
 * Output: 4
 * Explanation:
 * With t = 4, the required machine-minutes are:
 * ceil(8/4) + ceil(5/4) + ceil(10/4) = 2 + 2 + 3 = 7
 * which fits exactly. Any smaller t needs more than 7 machine-minutes.
 *
 * Example 2:
 * Input: rods = [12, 15, 6], machines = 6
 * Output: 6
 * Explanation:
 * With t = 6, the total is:
 * ceil(12/6) + ceil(15/6) + ceil(6/6) = 2 + 3 + 1 = 6
 * With t = 5, the total becomes:
 * 3 + 3 + 2 = 8
 * so 5 is not enough.
 */

public class Solution {

    /**
     * Finds the minimum positive integer heating time t such that:
     * sum(ceil(rods[i] / t)) <= machines
     *
     * The key observation is that the feasibility condition is monotonic:
     * - If some heating time t works, then any larger heating time also works.
     * - If some heating time t does not work, then any smaller heating time also does not work.
     *
     * Because of this monotonic behavior, we can use binary search on the answer.
     *
     * @param rods the array of rod lengths
     * @param machines the maximum total machine-minutes available
     * @return the minimum valid heating time t
     * Time complexity: O(n log M), where n is rods.length and M is the maximum rod length
     * Space complexity: O(1), ignoring input storage
     */
    public int minimumHeaterTime(int[] rods, int machines) {
        // The smallest possible heating time is 1.
        int left = 1;

        // The largest necessary heating time is the maximum rod length.
        // Why?
        // If t is at least the largest rod length, then every rod needs only 1 machine-minute.
        // Since machines >= rods.length by constraint, that will always be feasible.
        int right = findMax(rods);

        // This variable will store the best (smallest feasible) answer found so far.
        int answer = right;

        // Standard binary search over the answer space [left, right].
        while (left <= right) {
            // Use this form to avoid overflow:
            int mid = left + (right - left) / 2;

            // Check whether this candidate heating time is feasible.
            if (canProcessAll(rods, machines, mid)) {
                // If mid works, it is a valid answer.
                // But we want the minimum valid t, so we continue searching on the left side.
                answer = mid;
                right = mid - 1;
            } else {
                // If mid does not work, we need a larger heating time.
                left = mid + 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether all rods can be processed using at most the given number of machine-minutes
     * when the heating time is fixed to t.
     *
     * For each rod of length L, the required machine-minutes are:
     * ceil(L / t)
     *
     * To compute ceil(L / t) using integer arithmetic safely:
     * ceil(L / t) = (L + t - 1) / t
     *
     * We use a long accumulator because the sum can exceed the range of int during computation.
     * We also stop early if the running total already exceeds machines.
     *
     * @param rods the array of rod lengths
     * @param machines the maximum total machine-minutes allowed
     * @param t the candidate heating time
     * @return true if all rods can be processed within machines machine-minutes, otherwise false
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean canProcessAll(int[] rods, int machines, int t) {
        long requiredMachineMinutes = 0L;

        // Process each rod independently and add its required machine-minutes.
        for (int rodLength : rods) {
            // Compute ceil(rodLength / t) using integer math.
            long minutesForThisRod = (rodLength + (long) t - 1) / t;
            requiredMachineMinutes += minutesForThisRod;

            // Very important optimization:
            // If we already exceed the allowed machine-minutes, we can return false immediately.
            if (requiredMachineMinutes > machines) {
                return false;
            }
        }

        // If total required machine-minutes never exceeded machines, then t is feasible.
        return true;
    }

    /**
     * Finds the maximum value in the rods array.
     *
     * This is used as the upper bound for binary search because a heating time equal to the
     * maximum rod length guarantees each rod needs at most one machine-minute.
     *
     * @param rods the array of rod lengths
     * @return the maximum rod length
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public int findMax(int[] rods) {
        int max = rods[0];

        for (int rodLength : rods) {
            if (rodLength > max) {
                max = rodLength;
            }
        }

        return max;
    }

    /**
     * Demonstrates the solution on the sample test cases from the problem statement.
     *
     * It also prints the expected values so the output can be visually verified.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log M) per demonstration call
     * Space complexity: O(1), ignoring input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] rods1 = {8, 5, 10};
        int machines1 = 7;
        int result1 = solution.minimumHeaterTime(rods1, machines1);
        System.out.println("Sample 1:");
        System.out.println("rods = " + Arrays.toString(rods1) + ", machines = " + machines1);
        System.out.println("Minimum heater time = " + result1);
        System.out.println("Expected = 4");
        System.out.println();

        // Manual verification for Sample 1:
        // t = 4
        // ceil(8/4) = 2
        // ceil(5/4) = 2
        // ceil(10/4) = 3
        // total = 7 -> feasible
        //
        // t = 3
        // ceil(8/3) = 3
        // ceil(5/3) = 2
        // ceil(10/3) = 4
        // total = 9 -> not feasible
        //
        // Therefore answer is 4.

        // Sample 2
        int[] rods2 = {12, 15, 6};
        int machines2 = 6;
        int result2 = solution.minimumHeaterTime(rods2, machines2);
        System.out.println("Sample 2:");
        System.out.println("rods = " + Arrays.toString(rods2) + ", machines = " + machines2);
        System.out.println("Minimum heater time = " + result2);
        System.out.println("Expected = 6");
        System.out.println();

        // Manual verification for Sample 2:
        // t = 6
        // ceil(12/6) = 2
        // ceil(15/6) = 3
        // ceil(6/6) = 1
        // total = 6 -> feasible
        //
        // t = 5
        // ceil(12/5) = 3
        // ceil(15/5) = 3
        // ceil(6/5) = 2
        // total = 8 -> not feasible
        //
        // Therefore answer is 6.

        // Additional quick demonstration
        int[] rods3 = {1, 1, 1, 1};
        int machines3 = 4;
        int result3 = solution.minimumHeaterTime(rods3, machines3);
        System.out.println("Additional Test:");
        System.out.println("rods = " + Arrays.toString(rods3) + ", machines = " + machines3);
        System.out.println("Minimum heater time = " + result3);
        System.out.println("Expected = 1");
    }
}