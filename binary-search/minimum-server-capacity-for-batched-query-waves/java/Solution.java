import java.util.*;

/*
 * Title: Minimum Server Capacity for Batched Query Waves
 * Difficulty: Hard
 * Topic: Binary Search
 *
 * Problem Description:
 * You are given an array requests where requests[i] is the number of queries arriving in the i-th minute,
 * in chronological order. A backend service processes traffic in fixed consecutive deployment waves.
 * Each wave must cover a contiguous block of minutes, and the total number of queries assigned to any
 * single wave cannot exceed the chosen server capacity C. Minutes cannot be split across waves:
 * all queries from one minute must stay in the same wave. You are also given an integer k,
 * the maximum number of waves your operations team is willing to create.
 *
 * Your task is to compute the minimum integer server capacity C such that the entire traffic timeline
 * can be partitioned into at most k contiguous waves, where the sum of requests in every wave is at most C.
 *
 * Return the smallest possible capacity.
 *
 * This is a decision-optimization problem: for a candidate capacity C, determine whether the requests
 * can be grouped into at most k valid contiguous waves. Then use that monotonic property to find the
 * minimum feasible capacity efficiently.
 *
 * Constraints:
 * - 1 <= requests.length <= 200000
 * - 1 <= requests[i] <= 1000000000
 * - 1 <= k <= requests.length
 * - The answer fits in a 64-bit signed integer.
 *
 * Example 1:
 * Input: requests = [7,2,5,10,8], k = 2
 * Output: 18
 * Explanation: One optimal partition is [7,2,5] and [10,8]. The wave sums are 14 and 18,
 * so capacity 18 is enough. Any capacity smaller than 18 fails because the last two minutes
 * already require 18 if kept together, and using more than 2 waves is not allowed.
 *
 * Example 2:
 * Input: requests = [1,4,4,3,2], k = 3
 * Output: 5
 * Explanation: A valid partition is [1,4], [4], [3,2]. The maximum wave sum is 5.
 * Capacity 4 is impossible because then [1,4] and [3,2] would each exceed the limit if grouped,
 * forcing more than 3 waves.
 */

public class Solution {

    /**
     * Computes the minimum server capacity needed so that the requests array can be split into
     * at most k contiguous waves, with each wave sum not exceeding that capacity.
     *
     * The key idea:
     * 1. If a capacity C works, then any larger capacity also works.
     * 2. That monotonic behavior allows binary search over the answer.
     * 3. For each candidate capacity, greedily build waves from left to right and count how many
     *    waves are required.
     *
     * @param requests the number of queries arriving each minute; each minute must remain whole in one wave
     * @param k the maximum number of contiguous waves allowed
     * @return the smallest possible capacity that allows partitioning into at most k waves
     * Time complexity: O(n log S), where n is requests.length and S is the search range of capacities
     * Space complexity: O(1), excluding input storage
     */
    public long minimumServerCapacity(int[] requests, int k) {
        // The minimum possible capacity cannot be smaller than the largest single minute,
        // because a minute cannot be split across waves.
        long left = 0L;

        // The maximum possible capacity is the sum of all requests,
        // which corresponds to putting everything into one wave.
        long right = 0L;

        // Build the binary search bounds.
        for (int request : requests) {
            left = Math.max(left, request);
            right += request;
        }

        // Standard binary search on the answer space:
        // We want the smallest feasible capacity.
        while (left < right) {
            // Use this form to avoid overflow:
            // mid = left + (right - left) / 2
            long mid = left + (right - left) / 2;

            // If mid is enough, try to find an even smaller feasible capacity.
            if (canPartition(requests, k, mid)) {
                right = mid;
            } else {
                // If mid is not enough, all capacities <= mid are also not enough.
                // So we must search the larger half.
                left = mid + 1;
            }
        }

        // At the end, left == right and points to the smallest feasible capacity.
        return left;
    }

    /**
     * Checks whether the requests can be partitioned into at most k contiguous waves such that
     * each wave sum is at most the given capacity.
     *
     * Greedy strategy:
     * - Keep adding minutes to the current wave while the sum stays within capacity.
     * - As soon as adding the next minute would exceed capacity, start a new wave.
     * - This greedy approach minimizes the number of waves needed for that capacity.
     *
     * Why greedy is correct here:
     * - For a fixed capacity, delaying a split as long as possible can never increase the number
     *   of waves compared with splitting earlier.
     * - Therefore, the greedy count is the minimum number of waves needed for that capacity.
     *
     * @param requests the number of queries arriving each minute
     * @param k the maximum number of waves allowed
     * @param capacity the candidate server capacity being tested
     * @return true if the requests can be split into at most k valid contiguous waves; false otherwise
     * Time complexity: O(n), where n is requests.length
     * Space complexity: O(1)
     */
    public boolean canPartition(int[] requests, int k, long capacity) {
        // Start with one wave, because if the array is non-empty,
        // we need at least one wave to hold the first minute.
        int wavesUsed = 1;

        // Running sum of the current wave.
        long currentWaveSum = 0L;

        // Process each minute in chronological order.
        for (int request : requests) {
            // Safety check:
            // If a single minute exceeds capacity, partitioning is impossible.
            // In practice, our binary search lower bound already prevents this,
            // but keeping this check makes the method robust and self-contained.
            if (request > capacity) {
                return false;
            }

            // If adding this minute stays within capacity,
            // keep it in the current wave.
            if (currentWaveSum + request <= capacity) {
                currentWaveSum += request;
            } else {
                // Otherwise, we must start a new wave beginning with this minute.
                wavesUsed++;
                currentWaveSum = request;

                // Early exit:
                // If we already need more than k waves, this capacity fails.
                if (wavesUsed > k) {
                    return false;
                }
            }
        }

        // If we finished using at most k waves, the capacity is feasible.
        return true;
    }

    /**
     * Helper method to print an integer array in a readable format.
     *
     * @param arr the array to convert to a string
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n) due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * It also prints the expected outputs so the results can be visually verified.
     *
     * Example 1 trace:
     * requests = [7,2,5,10,8], k = 2
     * Minimum capacity = 18
     *
     * Example 2 trace:
     * requests = [1,4,4,3,2], k = 3
     * Minimum capacity = 5
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log S) across the demonstrated examples
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] requests1 = {7, 2, 5, 10, 8};
        int k1 = 2;
        long result1 = solution.minimumServerCapacity(requests1, k1);
        System.out.println("Example 1");
        System.out.println("requests = " + solution.arrayToString(requests1));
        System.out.println("k = " + k1);
        System.out.println("Minimum server capacity = " + result1);
        System.out.println("Expected = 18");
        System.out.println();

        int[] requests2 = {1, 4, 4, 3, 2};
        int k2 = 3;
        long result2 = solution.minimumServerCapacity(requests2, k2);
        System.out.println("Example 2");
        System.out.println("requests = " + solution.arrayToString(requests2));
        System.out.println("k = " + k2);
        System.out.println("Minimum server capacity = " + result2);
        System.out.println("Expected = 5");
        System.out.println();

        // Additional quick sanity checks for beginners:
        // 1) If k equals the number of minutes, each minute can be its own wave.
        //    Then the answer is simply the maximum single request.
        int[] requests3 = {3, 1, 9, 2};
        int k3 = 4;
        long result3 = solution.minimumServerCapacity(requests3, k3);
        System.out.println("Additional Check 1");
        System.out.println("requests = " + solution.arrayToString(requests3));
        System.out.println("k = " + k3);
        System.out.println("Minimum server capacity = " + result3);
        System.out.println("Expected = 9");
        System.out.println();

        // 2) If k is 1, everything must be in one wave.
        //    Then the answer is the total sum.
        int[] requests4 = {5, 6, 7};
        int k4 = 1;
        long result4 = solution.minimumServerCapacity(requests4, k4);
        System.out.println("Additional Check 2");
        System.out.println("requests = " + solution.arrayToString(requests4));
        System.out.println("k = " + k4);
        System.out.println("Minimum server capacity = " + result4);
        System.out.println("Expected = 18");
    }
}