import java.util.*;

/*
 * Title: Minimum Timeout Threshold for Batched API Retries
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A backend service needs to process a sequence of API calls in the given order.
 * The i-th call takes requestTimes[i] milliseconds if executed normally.
 * To avoid excessive retry overhead, the service groups consecutive calls into batches.
 * For any batch, the total time of all calls inside that batch must not exceed a chosen timeout threshold T.
 * If adding the next call would make the batch total exceed T, the current batch is closed
 * and a new batch starts with that call.
 *
 * You are given an array requestTimes and an integer maxBatches.
 * Find the minimum integer timeout threshold T such that all API calls can be processed
 * using at most maxBatches batches.
 *
 * You must preserve the original order of calls, and every call must belong to exactly one batch.
 * The threshold T must be at least as large as the longest single request time,
 * otherwise that call could never fit into any batch.
 *
 * Return the smallest possible T.
 *
 * Constraints:
 * - 1 <= requestTimes.length <= 100000
 * - 1 <= requestTimes[i] <= 1000000000
 * - 1 <= maxBatches <= requestTimes.length
 * - The answer fits in a 64-bit signed integer.
 *
 * Example 1:
 * Input: requestTimes = [7, 2, 5, 10, 8], maxBatches = 2
 * Output: 18
 * Explanation:
 * With T = 18, the calls can be grouped as [7, 2, 5] and [10, 8].
 * Using T = 17 would require 3 batches: [7, 2, 5], [10], [8].
 * So 18 is the minimum feasible threshold.
 *
 * Example 2:
 * Input: requestTimes = [4, 4, 4, 4], maxBatches = 3
 * Output: 8
 * Explanation:
 * With T = 8, one valid grouping is [4, 4], [4], [4], which uses 3 batches.
 * With T = 7, each batch can contain only one call, so 4 batches are required.
 * Therefore the minimum threshold is 8.
 *
 * Key Insight:
 * This problem is monotonic:
 * - If a timeout threshold T is sufficient, then any larger threshold is also sufficient.
 * - If a timeout threshold T is not sufficient, then any smaller threshold is also not sufficient.
 *
 * That monotonic behavior makes binary search a perfect fit.
 * For each candidate threshold, we greedily count how many batches are needed.
 * If the number of required batches is <= maxBatches, the threshold is feasible.
 * Otherwise, it is too small.
 */

public class Solution {

    /**
     * Finds the minimum timeout threshold such that the requests can be split into
     * at most maxBatches consecutive batches.
     *
     * The algorithm works in two phases:
     * 1. Determine the binary search range:
     *    - Lower bound = maximum single request time, because every request must fit somewhere.
     *    - Upper bound = sum of all request times, because one single batch containing everything
     *      is always possible with that threshold.
     * 2. Binary search within that range:
     *    - For each candidate threshold, greedily compute how many batches are needed.
     *    - If the candidate works, try smaller values.
     *    - If it does not work, try larger values.
     *
     * @param requestTimes the processing times of API calls, in the original required order
     * @param maxBatches the maximum number of allowed consecutive batches
     * @return the smallest feasible integer timeout threshold
     * Time complexity: O(n * log(sum(requestTimes))) where n is requestTimes.length
     * Space complexity: O(1) extra space
     */
    public long minimumTimeoutThreshold(int[] requestTimes, int maxBatches) {
        // The minimum possible threshold cannot be smaller than the largest single request,
        // because every request must fit into some batch by itself if necessary.
        long left = 0L;

        // The maximum possible threshold can be the sum of all request times,
        // which would allow placing every request into one single batch.
        long right = 0L;

        // Build the binary search boundaries.
        for (int time : requestTimes) {
            left = Math.max(left, time);
            right += time;
        }

        // This variable will store the best feasible answer found so far.
        long answer = right;

        // Standard binary search on the answer space.
        // We are searching for the smallest threshold that is feasible.
        while (left <= right) {
            // Use this form to avoid overflow:
            // mid = left + (right - left) / 2
            long mid = left + (right - left) / 2;

            // Check whether this candidate threshold is sufficient.
            if (canProcessWithinBatches(requestTimes, maxBatches, mid)) {
                // If mid works, it is a valid answer.
                // But we still want the minimum possible threshold,
                // so continue searching on the left half.
                answer = mid;
                right = mid - 1;
            } else {
                // If mid does not work, it is too small.
                // We must search larger thresholds.
                left = mid + 1;
            }
        }

        return answer;
    }

    /**
     * Determines whether all requests can be processed using at most maxBatches batches
     * when each batch sum must be <= threshold.
     *
     * Greedy strategy:
     * - Traverse requests from left to right.
     * - Keep adding the current request to the current batch while it fits.
     * - If it would exceed the threshold, close the current batch and start a new one.
     *
     * Why greedy is correct here:
     * - Since order must be preserved and batches must be consecutive,
     *   the best way to minimize the number of batches for a fixed threshold
     *   is to pack each batch as much as possible before starting a new one.
     * - Any earlier split would only increase or keep the same number of batches,
     *   never reduce it.
     *
     * @param requestTimes the processing times of API calls
     * @param maxBatches the maximum number of allowed batches
     * @param threshold the candidate timeout threshold being tested
     * @return true if the requests can be split into at most maxBatches batches, otherwise false
     * Time complexity: O(n) where n is requestTimes.length
     * Space complexity: O(1) extra space
     */
    public boolean canProcessWithinBatches(int[] requestTimes, int maxBatches, long threshold) {
        // Start with one batch, because if there is at least one request,
        // we need at least one batch to hold it.
        int batchesUsed = 1;

        // This stores the running sum of the current batch.
        long currentBatchSum = 0L;

        // Process requests in the required original order.
        for (int time : requestTimes) {
            // Safety check:
            // If a single request is larger than the threshold,
            // then it can never fit into any batch.
            if (time > threshold) {
                return false;
            }

            // If adding this request still keeps the batch sum within the threshold,
            // we greedily place it into the current batch.
            if (currentBatchSum + time <= threshold) {
                currentBatchSum += time;
            } else {
                // Otherwise, we must start a new batch with this request.
                batchesUsed++;
                currentBatchSum = time;

                // Early exit optimization:
                // If we already exceeded the allowed number of batches,
                // there is no need to continue.
                if (batchesUsed > maxBatches) {
                    return false;
                }
            }
        }

        // If we finished without exceeding maxBatches, the threshold is feasible.
        return true;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * This method also prints the expected outputs so that the result can be visually verified.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n * log(sum(requestTimes))) across the demonstrated examples
     * Space complexity: O(1) extra space excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] requestTimes1 = {7, 2, 5, 10, 8};
        int maxBatches1 = 2;
        long result1 = solution.minimumTimeoutThreshold(requestTimes1, maxBatches1);
        System.out.println("Example 1 Result: " + result1);
        System.out.println("Expected: 18");

        // Example 2
        int[] requestTimes2 = {4, 4, 4, 4};
        int maxBatches2 = 3;
        long result2 = solution.minimumTimeoutThreshold(requestTimes2, maxBatches2);
        System.out.println("Example 2 Result: " + result2);
        System.out.println("Expected: 8");

        // Additional quick sanity checks

        // If only one batch is allowed, answer must be the total sum.
        int[] requestTimes3 = {1, 2, 3, 4};
        int maxBatches3 = 1;
        long result3 = solution.minimumTimeoutThreshold(requestTimes3, maxBatches3);
        System.out.println("Additional Test 1 Result: " + result3);
        System.out.println("Expected: 10");

        // If maxBatches equals number of requests, answer can be the maximum element.
        int[] requestTimes4 = {9, 1, 7, 3};
        int maxBatches4 = 4;
        long result4 = solution.minimumTimeoutThreshold(requestTimes4, maxBatches4);
        System.out.println("Additional Test 2 Result: " + result4);
        System.out.println("Expected: 9");
    }
}