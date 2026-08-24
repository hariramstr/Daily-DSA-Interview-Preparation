/*
Minimum Launch Window for Satellite Image Batches

Problem Description:
A space imaging company must upload satellite photos to a ground station. The photos must be transmitted in the given order, and each photo batch has a size stored in the array batches, where batches[i] is the number of megabytes in the i-th batch. The company has exactly d launch windows left before weather conditions become too unstable. In one launch window, the ground station can transmit any consecutive sequence of batches, as long as the total size sent in that window does not exceed the chosen window capacity.

Your task is to find the minimum integer launch window capacity needed so that all batches can be transmitted within at most d launch windows.

Every batch must be sent completely within a single window. Batches cannot be split across windows, and the order of batches cannot be changed.

Return the smallest possible capacity that makes the schedule feasible.

This problem is designed to be solved efficiently using binary search on the answer. A candidate capacity can be checked greedily by simulating how many launch windows are required if each window can carry at most that much data.

Constraints:
- 1 <= batches.length <= 100000
- 1 <= batches[i] <= 1000000000
- 1 <= d <= batches.length
- The answer fits in a 64-bit signed integer

Example 1:
Input: batches = [12, 7, 15, 6, 9], d = 3
Output: 21
Explanation: With capacity 21, one valid schedule is [12, 7], [15, 6], [9]. Capacity 20 is not enough because it would require 4 windows.

Example 2:
Input: batches = [5, 5, 5, 5, 5, 5], d = 2
Output: 15
Explanation: A capacity of 15 allows [5, 5, 5] and [5, 5, 5]. Any smaller capacity would need more than 2 windows.
*/

import java.util.*;

public class Solution {

    /**
     * Computes the minimum launch window capacity needed to transmit all batches
     * in order using at most d launch windows.
     *
     * Core idea:
     * 1. The answer must be at least the largest single batch, because no batch can be split.
     * 2. The answer can be at most the sum of all batches, which means sending everything in one window.
     * 3. We binary search this capacity range.
     * 4. For each candidate capacity, we greedily count how many windows are needed.
     * 5. If the candidate works within d windows, we try a smaller capacity.
     * 6. Otherwise, we need a larger capacity.
     *
     * @param batches the array of batch sizes, where batches[i] is the size of the i-th batch
     * @param d the maximum number of launch windows allowed
     * @return the smallest capacity that allows all batches to be transmitted within at most d windows
     * Time complexity: O(n log S), where n is batches.length and S is the search range of capacities
     * Space complexity: O(1), excluding input storage
     */
    public long minimumLaunchWindowCapacity(int[] batches, int d) {
        // The smallest possible valid capacity cannot be less than the largest batch,
        // because each batch must fit entirely into a single launch window.
        long left = 0;

        // The largest possible capacity we ever need is the sum of all batches,
        // which corresponds to sending everything in one launch window.
        long right = 0;

        // Build the binary search boundaries.
        for (int batch : batches) {
            left = Math.max(left, batch);
            right += batch;
        }

        // Binary search for the minimum feasible capacity.
        // Invariant:
        // - Any capacity < answer is infeasible.
        // - Any capacity >= answer may be feasible.
        while (left < right) {
            // Use this form to avoid overflow:
            // mid = left + (right - left) / 2
            long mid = left + (right - left) / 2;

            // Check whether this candidate capacity is enough.
            if (canTransmitWithinDays(batches, d, mid)) {
                // If mid works, it might be the answer,
                // but there could still be a smaller feasible capacity.
                right = mid;
            } else {
                // If mid does not work, every smaller capacity also fails.
                // So we must search the larger half.
                left = mid + 1;
            }
        }

        // At the end, left == right and points to the minimum feasible capacity.
        return left;
    }

    /**
     * Checks whether all batches can be transmitted in order within at most d launch windows
     * if each window has the given capacity.
     *
     * Greedy strategy:
     * - Put as many consecutive batches as possible into the current window.
     * - When the next batch would exceed capacity, start a new window.
     * - This greedy approach minimizes the number of windows used for a fixed capacity.
     *
     * Why greedy is correct here:
     * - For a fixed capacity, delaying a split as long as possible can never increase
     *   the number of windows compared with splitting earlier.
     * - Therefore, this simulation gives the minimum number of windows required.
     *
     * @param batches the array of batch sizes in fixed order
     * @param d the maximum number of launch windows allowed
     * @param capacity the candidate capacity for each launch window
     * @return true if all batches can be transmitted within at most d windows, otherwise false
     * Time complexity: O(n), where n is batches.length
     * Space complexity: O(1)
     */
    public boolean canTransmitWithinDays(int[] batches, int d, long capacity) {
        // Start with one launch window already in use,
        // because if there is at least one batch, we need at least one window.
        int windowsUsed = 1;

        // Tracks the total size currently placed in the active launch window.
        long currentLoad = 0;

        // Process batches in the required order.
        for (int batch : batches) {
            // Safety check:
            // If a single batch is larger than capacity, this capacity is impossible.
            // In our binary search, capacity is always at least max(batch),
            // but keeping this check makes the method robust and self-contained.
            if (batch > capacity) {
                return false;
            }

            // Try to place the current batch into the current launch window.
            if (currentLoad + batch <= capacity) {
                // It fits, so we keep extending the current consecutive segment.
                currentLoad += batch;
            } else {
                // It does not fit.
                // Therefore, we must start a new launch window beginning with this batch.
                windowsUsed++;
                currentLoad = batch;

                // Important optimization:
                // If we already exceeded the allowed number of windows,
                // there is no need to continue.
                if (windowsUsed > d) {
                    return false;
                }
            }
        }

        // If we finished processing all batches without exceeding d windows,
        // then this capacity is feasible.
        return true;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Also includes a few extra examples to help beginners see how the method behaves.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding the called algorithm runs
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] batches1 = {12, 7, 15, 6, 9};
        int d1 = 3;
        long result1 = solution.minimumLaunchWindowCapacity(batches1, d1);
        System.out.println("Example 1:");
        System.out.println("batches = " + Arrays.toString(batches1) + ", d = " + d1);
        System.out.println("Minimum launch window capacity = " + result1);
        System.out.println("Expected = 21");
        System.out.println();

        // Example 2
        int[] batches2 = {5, 5, 5, 5, 5, 5};
        int d2 = 2;
        long result2 = solution.minimumLaunchWindowCapacity(batches2, d2);
        System.out.println("Example 2:");
        System.out.println("batches = " + Arrays.toString(batches2) + ", d = " + d2);
        System.out.println("Minimum launch window capacity = " + result2);
        System.out.println("Expected = 15");
        System.out.println();

        // Extra example: each batch in its own window
        int[] batches3 = {8, 1, 4, 10};
        int d3 = 4;
        long result3 = solution.minimumLaunchWindowCapacity(batches3, d3);
        System.out.println("Extra Example 1:");
        System.out.println("batches = " + Arrays.toString(batches3) + ", d = " + d3);
        System.out.println("Minimum launch window capacity = " + result3);
        System.out.println("Expected = 10");
        System.out.println();

        // Extra example: all batches in one window
        int[] batches4 = {3, 6, 2, 7};
        int d4 = 1;
        long result4 = solution.minimumLaunchWindowCapacity(batches4, d4);
        System.out.println("Extra Example 2:");
        System.out.println("batches = " + Arrays.toString(batches4) + ", d = " + d4);
        System.out.println("Minimum launch window capacity = " + result4);
        System.out.println("Expected = 18");
    }
}