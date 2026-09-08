import java.util.*;

/*
Problem Title: Minimum Warehouse Lift Strength for Stacked Crates

Problem Description:
A warehouse uses an automated lift to move crates in the given order from left to right.
The weight of the i-th crate is weights[i]. The lift has a strength limit S.
It may load consecutive crates into the same trip as long as the total weight of that trip
does not exceed S. Once a trip starts, crates cannot be reordered or skipped, and every
crate must be moved exactly once.

You are also given an integer maxTrips, the maximum number of trips the warehouse is willing
to allow in one shift. Your task is to compute the minimum lift strength S such that all
crates can be moved in at most maxTrips trips.

This is a decision/optimization problem: for any candidate strength S, you can check whether
it is possible to partition the array into at most maxTrips contiguous groups where each
group sum is at most S. The answer is the smallest such S.

Return that minimum possible strength.

Constraints:
- 1 <= weights.length <= 100000
- 1 <= weights[i] <= 1000000000
- 1 <= maxTrips <= weights.length
- The answer fits in a 64-bit signed integer

Example 1:
Input: weights = [7,2,5,10,8], maxTrips = 2
Output: 18

Example 2:
Input: weights = [4,4,4,4,4], maxTrips = 3
Output: 8
*/

public class Solution {

    /**
     * Computes the minimum lift strength needed so that all crates can be moved
     * in at most maxTrips trips while preserving the original order.
     *
     * Core idea:
     * 1. If the lift strength is too small, we will need too many trips.
     * 2. If the lift strength is large enough, we can finish within maxTrips trips.
     * 3. This creates a monotonic condition:
     *    - smaller strengths may fail
     *    - larger strengths will eventually succeed
     * 4. Therefore, we can binary search the answer.
     *
     * Search range:
     * - Lower bound = maximum single crate weight
     *   Because every crate must fit into some trip by itself if necessary.
     * - Upper bound = sum of all crate weights
     *   Because one trip carrying everything is always possible if allowed by strength.
     *
     * @param weights the array of crate weights in fixed left-to-right order
     * @param maxTrips the maximum number of trips allowed
     * @return the minimum possible lift strength as a long
     * Time complexity: O(n log(sum(weights)))
     * Space complexity: O(1)
     */
    public long minimumLiftStrength(int[] weights, int maxTrips) {
        long left = 0L;
        long right = 0L;

        // Step 1:
        // Build the binary search boundaries.
        //
        // left  = the heaviest single crate
        // right = the total weight of all crates
        //
        // Why?
        // - Any valid strength must be at least the maximum element.
        // - A strength equal to the total sum can always move everything in one trip.
        for (int weight : weights) {
            left = Math.max(left, weight);
            right += weight;
        }

        // Step 2:
        // Standard binary search on the answer.
        //
        // We are searching for the smallest strength S such that:
        // canMoveWithinTrips(weights, maxTrips, S) == true
        //
        // Invariant:
        // - The answer is always somewhere in [left, right].
        while (left < right) {
            // Use this form to avoid overflow:
            long mid = left + (right - left) / 2;

            // Step 3:
            // Check whether this candidate strength is sufficient.
            if (canMoveWithinTrips(weights, maxTrips, mid)) {
                // mid works, so try to find an even smaller valid strength.
                right = mid;
            } else {
                // mid does not work, so the answer must be larger.
                left = mid + 1;
            }
        }

        // When left == right, we have found the minimum valid strength.
        return left;
    }

    /**
     * Checks whether all crates can be moved in at most maxTrips trips
     * if the lift strength is limited to candidateStrength.
     *
     * Greedy strategy:
     * - Scan crates from left to right.
     * - Keep adding crates to the current trip while the total does not exceed candidateStrength.
     * - As soon as adding the next crate would exceed candidateStrength, start a new trip.
     *
     * Why this greedy approach is correct:
     * - For a fixed strength, packing each trip as full as possible minimizes the number of trips.
     * - Starting a new trip earlier can never reduce the total number of trips needed.
     *
     * @param weights the array of crate weights
     * @param maxTrips the maximum allowed number of trips
     * @param candidateStrength the lift strength being tested
     * @return true if all crates can be moved in at most maxTrips trips, otherwise false
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean canMoveWithinTrips(int[] weights, int maxTrips, long candidateStrength) {
        // We start with one trip because if there is at least one crate,
        // we need at least one trip to carry something.
        int tripsUsed = 1;

        // currentTripWeight stores the total weight currently loaded into the ongoing trip.
        long currentTripWeight = 0L;

        // Process crates in the required order.
        for (int weight : weights) {
            // If adding this crate would exceed the candidate strength,
            // we must start a new trip.
            if (currentTripWeight + weight > candidateStrength) {
                tripsUsed++;
                currentTripWeight = weight;

                // Early exit:
                // If we already exceeded the allowed number of trips,
                // there is no need to continue.
                if (tripsUsed > maxTrips) {
                    return false;
                }
            } else {
                // Otherwise, safely add this crate to the current trip.
                currentTripWeight += weight;
            }
        }

        // If we finished processing all crates without exceeding maxTrips,
        // then this candidate strength is sufficient.
        return true;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It also prints the expected outputs so the result can be visually verified.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log(sum(weights))) across the demonstrated examples
     * Space complexity: O(1) excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] weights1 = {7, 2, 5, 10, 8};
        int maxTrips1 = 2;
        long result1 = solution.minimumLiftStrength(weights1, maxTrips1);
        System.out.println("Example 1:");
        System.out.println("weights = " + Arrays.toString(weights1));
        System.out.println("maxTrips = " + maxTrips1);
        System.out.println("Minimum lift strength = " + result1);
        System.out.println("Expected = 18");
        System.out.println();

        int[] weights2 = {4, 4, 4, 4, 4};
        int maxTrips2 = 3;
        long result2 = solution.minimumLiftStrength(weights2, maxTrips2);
        System.out.println("Example 2:");
        System.out.println("weights = " + Arrays.toString(weights2));
        System.out.println("maxTrips = " + maxTrips2);
        System.out.println("Minimum lift strength = " + result2);
        System.out.println("Expected = 8");
        System.out.println();

        // Additional quick sanity checks for beginners:
        int[] weights3 = {10};
        int maxTrips3 = 1;
        long result3 = solution.minimumLiftStrength(weights3, maxTrips3);
        System.out.println("Additional Test 1:");
        System.out.println("weights = " + Arrays.toString(weights3));
        System.out.println("maxTrips = " + maxTrips3);
        System.out.println("Minimum lift strength = " + result3);
        System.out.println("Expected = 10");
        System.out.println();

        int[] weights4 = {1, 2, 3, 4, 5};
        int maxTrips4 = 5;
        long result4 = solution.minimumLiftStrength(weights4, maxTrips4);
        System.out.println("Additional Test 2:");
        System.out.println("weights = " + Arrays.toString(weights4));
        System.out.println("maxTrips = " + maxTrips4);
        System.out.println("Minimum lift strength = " + result4);
        System.out.println("Expected = 5");
        System.out.println();

        int[] weights5 = {1, 2, 3, 4, 5};
        int maxTrips5 = 1;
        long result5 = solution.minimumLiftStrength(weights5, maxTrips5);
        System.out.println("Additional Test 3:");
        System.out.println("weights = " + Arrays.toString(weights5));
        System.out.println("maxTrips = " + maxTrips5);
        System.out.println("Minimum lift strength = " + result5);
        System.out.println("Expected = 15");
    }
}