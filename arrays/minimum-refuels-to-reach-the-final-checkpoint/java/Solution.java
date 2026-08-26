import java.util.*;

/*
 * Title: Minimum Refuels to Reach the Final Checkpoint
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * You are driving along a straight highway toward a final checkpoint at distance target miles
 * from your starting point. Your car starts with startFuel liters of fuel, and it uses exactly
 * 1 liter per mile. Along the way, there are fuel stations described by a 2D array stations,
 * where stations[i] = [position, fuel] means there is a station at mile position containing
 * fuel liters you may take if you stop there. The stations are sorted by position in strictly
 * increasing order.
 *
 * Whenever you reach a station, you may choose to refuel there and take all of its fuel,
 * or skip it. Refueling itself does not consume time, but each stop counts as one refuel.
 * Return the minimum number of refuels needed to reach the final checkpoint. If it is
 * impossible to reach the target, return -1.
 *
 * This problem asks you to make optimal decisions while moving through the array of stations.
 * A greedy strategy is often needed because stopping too early may be wasteful, while waiting
 * too long may make the trip impossible.
 *
 * Constraints:
 * - 1 <= target <= 10^9
 * - 0 <= startFuel <= 10^9
 * - 0 <= stations.length <= 10^5
 * - 0 < stations[i][0] < target
 * - 1 <= stations[i][1] <= 10^9
 * - stations is sorted by position in strictly increasing order
 *
 * Example 1:
 * Input: target = 100, startFuel = 25, stations = [[25,25],[50,25],[75,25]]
 * Output: 3
 * Explanation: You can reach mile 25, refuel to continue, then do the same at miles 50 and 75.
 * Three stops are required to reach mile 100.
 *
 * Example 2:
 * Input: target = 120, startFuel = 50, stations = [[25,30],[40,20],[70,40],[95,30]]
 * Output: 2
 * Explanation: One optimal plan is to stop at mile 40 and mile 70. Starting with 50 fuel,
 * you reach 40, refuel by 20, then reach 70, refuel by 40, and finally reach 120.
 * Fewer than two refuels is not enough.
 */

public class Solution {

    /**
     * Computes the minimum number of refuels needed to reach the target.
     *
     * Greedy idea:
     * We drive as far as our current fuel allows. While moving forward through the stations,
     * every station we can reach is a "candidate" for refueling later. We store the fuel amounts
     * of all reachable stations in a max-heap.
     *
     * Whenever we can no longer move forward enough to reach the target (or the next needed point),
     * we refuel from the previously passed station that gives the most fuel. This is optimal because
     * if we must stop, taking the largest available fuel extends our reach the most and minimizes
     * the number of stops.
     *
     * @param target the final checkpoint distance from the start
     * @param startFuel the initial amount of fuel in the car
     * @param stations the fuel stations, where each entry is [position, fuel]
     * @return the minimum number of refuels required to reach the target, or -1 if impossible
     * Time complexity: O(n log n), where n is the number of stations
     * Space complexity: O(n) for the priority queue
     */
    public int minRefuelStops(int target, int startFuel, int[][] stations) {
        // Max-heap:
        // We want to quickly retrieve the largest fuel amount among all stations
        // we have already passed and could have chosen to stop at.
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());

        // "fuelReach" means the farthest mile we can currently reach with the fuel we have,
        // after accounting for all refuels chosen so far.
        //
        // We use long to be extra safe, because repeated additions of large fuel values
        // can exceed int range in intermediate calculations.
        long fuelReach = startFuel;

        // Number of refueling stops we have made so far.
        int refuels = 0;

        // Index to scan through stations in order.
        int index = 0;
        int n = stations.length;

        // Continue until our reachable distance is at least the target.
        while (fuelReach < target) {
            // Step 1:
            // Add every station that is reachable with our current fuelReach into the max-heap.
            //
            // Why?
            // Because once a station is reachable, it becomes a valid option for refueling.
            // We may or may not use it immediately, but we want it available if needed later.
            while (index < n && stations[index][0] <= fuelReach) {
                maxHeap.offer(stations[index][1]);
                index++;
            }

            // Step 2:
            // If there are no reachable stations left in the heap, and we still have not
            // reached the target, then the journey is impossible.
            //
            // That means:
            // - We cannot move farther with current fuel
            // - There is no previously passed station we can refuel from
            if (maxHeap.isEmpty()) {
                return -1;
            }

            // Step 3:
            // We must refuel now. To minimize the number of stops, always choose the station
            // with the largest available fuel among the stations we have already passed.
            //
            // This greedy choice is optimal:
            // if we are forced to stop, taking the maximum fuel gives the greatest extension
            // of reach for exactly one stop.
            fuelReach += maxHeap.poll();
            refuels++;
        }

        // Once fuelReach >= target, we can reach the final checkpoint.
        return refuels;
    }

    /**
     * Helper method to print a test case and its result in a beginner-friendly format.
     *
     * @param target the final checkpoint distance
     * @param startFuel the initial fuel amount
     * @param stations the list of stations
     * @return the computed minimum number of refuels
     * Time complexity: O(n log n), because it delegates to minRefuelStops
     * Space complexity: O(n), because it delegates to minRefuelStops
     */
    public int runAndPrintExample(int target, int startFuel, int[][] stations) {
        int result = minRefuelStops(target, startFuel, stations);
        System.out.println("Target: " + target);
        System.out.println("Start Fuel: " + startFuel);
        System.out.println("Stations: " + stationsToString(stations));
        System.out.println("Minimum Refuels Needed: " + result);
        System.out.println();
        return result;
    }

    /**
     * Converts the stations array into a readable string.
     *
     * @param stations the 2D stations array
     * @return a string representation of the stations
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String stationsToString(int[][] stations) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < stations.length; i++) {
            sb.append(Arrays.toString(stations[i]));
            if (i < stations.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total n log n) across the demonstrated examples
     * Space complexity: O(max n) for the largest example's heap usage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // target = 100, startFuel = 25, stations = [[25,25],[50,25],[75,25]]
        //
        // Trace:
        // - Start reach = 25
        // - Reach station at 25, add fuel 25 to heap
        // - Need more fuel, take 25 => reach 50, stops = 1
        // - Reach station at 50, add fuel 25 to heap
        // - Need more fuel, take 25 => reach 75, stops = 2
        // - Reach station at 75, add fuel 25 to heap
        // - Need more fuel, take 25 => reach 100, stops = 3
        // Output should be 3
        int[][] stations1 = {
            {25, 25},
            {50, 25},
            {75, 25}
        };
        solution.runAndPrintExample(100, 25, stations1);

        // Example 2:
        // target = 120, startFuel = 50, stations = [[25,30],[40,20],[70,40],[95,30]]
        //
        // Trace:
        // - Start reach = 50
        // - Reach stations at 25 and 40, heap = [30, 20]
        // - Need more fuel, take 30 => reach 80, stops = 1
        // - Reach station at 70, heap now contains [40, 20]
        // - Need more fuel, take 40 => reach 120, stops = 2
        // Output should be 2
        int[][] stations2 = {
            {25, 30},
            {40, 20},
            {70, 40},
            {95, 30}
        };
        solution.runAndPrintExample(120, 50, stations2);

        // Additional quick checks for completeness.

        // Already enough fuel to reach target: answer should be 0
        int[][] stations3 = {
            {10, 60},
            {20, 30}
        };
        solution.runAndPrintExample(50, 50, stations3);

        // Impossible case: cannot reach first station or target
        int[][] stations4 = {
            {10, 100}
        };
        solution.runAndPrintExample(100, 1, stations4);
    }
}