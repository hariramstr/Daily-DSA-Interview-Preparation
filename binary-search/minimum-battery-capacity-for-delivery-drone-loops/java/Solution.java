import java.util.*;

/*
Problem Title: Minimum Battery Capacity for Delivery Drone Loops

Problem Description:
A delivery company operates a drone that must complete a fixed sequence of package stops in order.
The drone starts each day fully charged at the warehouse, visits the stops from left to right, and
may return to the warehouse to recharge whenever needed. A single trip from the warehouse can cover
a consecutive block of stops, but the total energy needed for that block must not exceed the drone's
battery capacity. After recharging, the drone resumes with the next unserved stop.

You are given an array energy where energy[i] is the energy required to serve stop i, and an integer
maxTrips representing the maximum number of warehouse departures allowed for the day. Your task is to
find the minimum battery capacity needed so that all stops can be served in order using at most
maxTrips trips.

Each trip must serve at least one stop, and stops cannot be reordered or split across trips. The
answer is the smallest integer capacity C such that the array can be partitioned into at most
maxTrips contiguous groups, where the sum of each group is at most C.

Constraints:
- 1 <= energy.length <= 100000
- 1 <= energy[i] <= 1000000000
- 1 <= maxTrips <= energy.length
- The result fits in a 64-bit signed integer

Example 1:
Input: energy = [7, 2, 5, 10, 8], maxTrips = 2
Output: 18
Explanation: With capacity 18, the drone can take trips [7, 2, 5] and [10, 8].
Any smaller capacity would require more than 2 trips.

Example 2:
Input: energy = [4, 4, 4, 4, 4], maxTrips = 3
Output: 8
Explanation: One valid plan is [4, 4], [4, 4], [4].
Capacity 7 is not enough because some trip would need to hold two stops totaling 8.
*/

public class Solution {

    /**
     * Computes the minimum battery capacity needed so that all stops can be served
     * in order using at most maxTrips trips.
     *
     * The key idea:
     * 1. If we guess a battery capacity C, we can greedily determine how many trips
     *    are required:
     *    - Keep adding stops to the current trip while the total does not exceed C.
     *    - If adding the next stop would exceed C, start a new trip.
     * 2. This greedy strategy gives the minimum number of trips needed for that capacity.
     * 3. If a capacity works, then any larger capacity also works.
     *    This monotonic behavior allows binary search on the answer.
     *
     * Search range:
     * - Lower bound = maximum single stop energy, because every stop must fit in one trip.
     * - Upper bound = total sum of all energies, because one trip could serve everything.
     *
     * @param energy the energy required for each stop, in fixed order
     * @param maxTrips the maximum number of trips allowed
     * @return the smallest battery capacity that allows serving all stops in at most maxTrips trips
     * Time complexity: O(n log S), where n is energy.length and S is the sum of all energy values
     * Space complexity: O(1)
     */
    public long minimumBatteryCapacity(int[] energy, int maxTrips) {
        // The minimum possible capacity cannot be smaller than the largest single stop,
        // because every trip must contain whole stops and each stop must fit by itself.
        long left = 0L;

        // The maximum possible capacity is the sum of all stops,
        // which corresponds to serving everything in one trip.
        long right = 0L;

        // Build the binary search bounds carefully using long,
        // because sums can exceed the 32-bit int range.
        for (int value : energy) {
            left = Math.max(left, value);
            right += value;
        }

        // Binary search for the smallest feasible capacity.
        // Invariant:
        // - Any capacity < answer is infeasible
        // - Any capacity >= answer is feasible
        while (left < right) {
            // Midpoint calculation written this way to avoid overflow.
            long mid = left + (right - left) / 2;

            // Check whether this guessed capacity is enough.
            if (canServeWithinTrips(energy, maxTrips, mid)) {
                // If mid works, try to find an even smaller working capacity.
                right = mid;
            } else {
                // If mid does not work, we must increase the capacity.
                left = mid + 1;
            }
        }

        // At the end, left == right and points to the minimum feasible capacity.
        return left;
    }

    /**
     * Determines whether all stops can be served in order using at most maxTrips trips
     * when each trip has battery capacity equal to capacity.
     *
     * Greedy rule:
     * - Put as many consecutive stops as possible into the current trip.
     * - When the next stop would exceed capacity, start a new trip.
     *
     * Why greedy is correct here:
     * - For a fixed capacity, delaying a trip split as long as possible never increases
     *   the number of trips compared with splitting earlier.
     * - Therefore, this method produces the minimum number of trips needed for that capacity.
     *
     * @param energy the energy required for each stop
     * @param maxTrips the maximum number of trips allowed
     * @param capacity the candidate battery capacity to test
     * @return true if all stops can be served in at most maxTrips trips, otherwise false
     * Time complexity: O(n), where n is energy.length
     * Space complexity: O(1)
     */
    public boolean canServeWithinTrips(int[] energy, int maxTrips, long capacity) {
        // Start with one trip, because if the array is non-empty,
        // we need at least one trip to serve the first stop.
        int tripsUsed = 1;

        // This stores the current total energy loaded into the ongoing trip.
        long currentTripSum = 0L;

        // Process stops from left to right, preserving order exactly as required.
        for (int value : energy) {
            // Safety check:
            // If a single stop exceeds the proposed capacity, then this capacity is impossible.
            // In practice, binary search starts from max(energy), so this should not happen,
            // but keeping this check makes the helper method robust and self-contained.
            if (value > capacity) {
                return false;
            }

            // If adding this stop still keeps the trip within capacity,
            // we greedily include it in the current trip.
            if (currentTripSum + value <= capacity) {
                currentTripSum += value;
            } else {
                // Otherwise, we must start a new trip beginning with this stop.
                tripsUsed++;
                currentTripSum = value;

                // Early exit optimization:
                // If we already exceeded the allowed number of trips,
                // there is no need to continue scanning.
                if (tripsUsed > maxTrips) {
                    return false;
                }
            }
        }

        // If we finished processing all stops without exceeding maxTrips,
        // then the capacity is feasible.
        return true;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * It also prints the expected outputs so a beginner can visually compare them.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log S) across the demonstrated test cases
     * Space complexity: O(1), excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] energy1 = {7, 2, 5, 10, 8};
        int maxTrips1 = 2;
        long result1 = solution.minimumBatteryCapacity(energy1, maxTrips1);
        System.out.println("Example 1 Result: " + result1);
        System.out.println("Expected: 18");

        // Quick reasoning trace for Example 1:
        // Capacity 18 works:
        // Trip 1 -> [7, 2, 5] = 14
        // Trip 2 -> [10, 8] = 18
        // Total trips = 2, so feasible.
        //
        // Capacity 17 fails:
        // Trip 1 -> [7, 2, 5] = 14
        // Trip 2 -> [10] = 10
        // Trip 3 -> [8] = 8
        // Total trips = 3, which exceeds 2.
        //
        // Therefore the minimum is 18.

        // Example 2
        int[] energy2 = {4, 4, 4, 4, 4};
        int maxTrips2 = 3;
        long result2 = solution.minimumBatteryCapacity(energy2, maxTrips2);
        System.out.println("Example 2 Result: " + result2);
        System.out.println("Expected: 8");

        // Quick reasoning trace for Example 2:
        // Capacity 8 works:
        // Trip 1 -> [4, 4] = 8
        // Trip 2 -> [4, 4] = 8
        // Trip 3 -> [4] = 4
        // Total trips = 3, so feasible.
        //
        // Capacity 7 fails:
        // No trip can hold two 4's because 4 + 4 = 8 > 7.
        // So each trip can hold only one stop.
        // That would require 5 trips, which exceeds 3.
        //
        // Therefore the minimum is 8.
    }
}