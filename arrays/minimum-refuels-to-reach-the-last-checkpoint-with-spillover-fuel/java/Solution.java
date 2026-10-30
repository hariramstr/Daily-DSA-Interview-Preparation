import java.util.*;

/*
 * Title: Minimum Refuels to Reach the Last Checkpoint with Spillover Fuel
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * You are planning a road trip through a straight desert highway. The destination is at position
 * target, measured in miles from the start. Your car begins at position 0 with startFuel liters
 * of fuel, and it consumes exactly 1 liter per mile. Along the way, there are gas stations
 * represented by an array stations, where stations[i] = [position_i, fuel_i] means there is a
 * station at mile position_i containing fuel_i liters of fuel.
 *
 * Unlike a normal refueling problem, every time you stop at a station, you must take all of its
 * fuel. If your tank already contains some fuel, that remaining fuel stays in the tank, so the
 * station's fuel simply spills over into your current amount. You may stop at any subset of
 * stations that you can physically reach, and each station can be used at most once.
 *
 * Return the minimum number of refueling stops needed to reach target. If it is impossible,
 * return -1.
 *
 * Constraints:
 * - 1 <= target <= 10^9
 * - 0 <= startFuel <= 10^9
 * - 0 <= stations.length <= 10^5
 * - 0 < position_i < target
 * - 1 <= fuel_i <= 10^9
 * - stations is sorted by position_i in strictly increasing order
 *
 * Example 1:
 * Input: target = 100, startFuel = 25, stations = [[10,30],[20,20],[40,40],[70,25]]
 * Output: 3
 *
 * Example 2:
 * Input: target = 120, startFuel = 50, stations = [[25,25],[60,30],[90,20]]
 * Output: 2
 */

public class Solution {

    /**
     * Computes the minimum number of refueling stops needed to reach the target.
     *
     * Core idea:
     * We move forward conceptually and keep track of every station we have already passed.
     * Among those passed stations, if we ever realize we cannot continue farther, we should
     * refuel from the station with the largest fuel amount first. This greedy choice is optimal
     * because taking more fuel earlier maximizes how far we can extend our reach with each stop,
     * which helps minimize the total number of stops.
     *
     * Detailed strategy:
     * 1. Treat "current reachable distance" as the farthest mile we can reach with the fuel
     *    collected so far.
     * 2. Initially, that reachable distance is startFuel.
     * 3. Add into a max-heap all stations whose position is <= current reachable distance,
     *    because those are stations we can already reach.
     * 4. If target is already within reachable distance, we are done.
     * 5. Otherwise, if there are no reachable stations left to use, then the trip is impossible.
     * 6. Otherwise, take the largest fuel amount from the heap, add it to reachable distance,
     *    and count one refueling stop.
     * 7. Repeat until target becomes reachable or we get stuck.
     *
     * Why this works:
     * - At any point, if we need to refuel, choosing the largest available fuel among all
     *   reachable-but-not-yet-used stations gives the maximum possible extension of range.
     * - If any solution with k stops exists, this greedy process will not require more than
     *   necessary, because every stop is used only when needed and gives the best possible gain.
     *
     * Example 1 trace:
     * target = 100, startFuel = 25
     * stations = [ [10,30], [20,20], [40,40], [70,25] ]
     *
     * Reachable = 25
     * Reachable stations: 10(30), 20(20)
     * Need more to continue, take max = 30 -> Reachable = 55, stops = 1
     * Now station 40(40) becomes reachable
     * Need more to continue, take max = 40 -> Reachable = 95, stops = 2
     * Now station 70(25) becomes reachable
     * Need more to reach 100, take max = 25 -> Reachable = 120, stops = 3
     * Target reached, answer = 3
     *
     * Example 2 trace:
     * target = 120, startFuel = 50
     * stations = [ [25,25], [60,30], [90,20] ]
     *
     * Reachable = 50
     * Reachable stations: 25(25)
     * Need more, take 25 -> Reachable = 75, stops = 1
     * Now station 60(30) becomes reachable
     * Need more, take 30 -> Reachable = 105, stops = 2
     * Now station 90(20) becomes reachable
     * Need more, take 20 -> Reachable = 125, stops = 3
     *
     * Note:
     * For the stated input in Example 2, the mathematically correct minimum is 3, not 2,
     * because after stopping only at mile 25 and mile 60:
     * - Start with 50
     * - Reach 25: remaining 25, refuel +25 => 50
     * - Reach 60: remaining 15, refuel +30 => 45
     * - From 60 to 120 needs 60 fuel, but only 45 is available
     * Therefore two stops are not enough for that exact data.
     *
     * @param target the destination position in miles
     * @param startFuel the initial amount of fuel in the car
     * @param stations the gas stations, where each entry is [position, fuel]
     * @return the minimum number of refueling stops needed to reach the target, or -1 if impossible
     * Time complexity: O(n log n), where n is the number of stations
     * Space complexity: O(n), due to the priority queue
     */
    public int minRefuelStops(int target, int startFuel, int[][] stations) {
        // Max-heap storing fuel amounts of stations we have passed and are allowed to use.
        // We want the largest fuel first, so we use reverse order.
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());

        // "reachableDistance" means the farthest mile we can currently get to.
        // We use long to be extra safe when adding large fuel values many times.
        long reachableDistance = startFuel;

        // Number of refueling stops used so far.
        int stops = 0;

        // Index to scan through stations in sorted order.
        int index = 0;
        int n = stations.length;

        // Continue until our reachable distance is enough to get to the target.
        while (reachableDistance < target) {

            // Add every station that is at or before our current reachable distance.
            // These are stations we can physically reach, so they become candidates for refueling.
            while (index < n && stations[index][0] <= reachableDistance) {
                maxHeap.offer(stations[index][1]);
                index++;
            }

            // If there is no reachable station available to refuel from,
            // then we cannot move any farther and the trip is impossible.
            if (maxHeap.isEmpty()) {
                return -1;
            }

            // Greedy choice:
            // Use the station with the largest fuel among all reachable unused stations.
            // This gives the biggest possible extension of our range for one stop.
            int bestFuel = maxHeap.poll();
            reachableDistance += bestFuel;
            stops++;
        }

        // Once reachableDistance >= target, we can reach the destination.
        return stops;
    }

    /**
     * Helper method to print a station array in a readable format.
     *
     * @param stations the station array to print
     * @return a string representation of the stations
     * Time complexity: O(n)
     * Space complexity: O(n) for the built string
     */
    public String stationsToString(int[][] stations) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < stations.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(Arrays.toString(stations[i]));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Runs sample demonstrations of the algorithm.
     *
     * This method prints:
     * - the input values
     * - the computed answer
     * - a note about the second sample's stated output versus the actual arithmetic
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total stations across examples * log n)
     * Space complexity: O(n) for the priority queue in each call
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int target1 = 100;
        int startFuel1 = 25;
        int[][] stations1 = {
            {10, 30},
            {20, 20},
            {40, 40},
            {70, 25}
        };

        int result1 = solution.minRefuelStops(target1, startFuel1, stations1);
        System.out.println("Example 1:");
        System.out.println("target = " + target1);
        System.out.println("startFuel = " + startFuel1);
        System.out.println("stations = " + solution.stationsToString(stations1));
        System.out.println("Minimum refueling stops = " + result1);
        System.out.println("Expected from prompt = 3");
        System.out.println();

        int target2 = 120;
        int startFuel2 = 50;
        int[][] stations2 = {
            {25, 25},
            {60, 30},
            {90, 20}
        };

        int result2 = solution.minRefuelStops(target2, startFuel2, stations2);
        System.out.println("Example 2:");
        System.out.println("target = " + target2);
        System.out.println("startFuel = " + startFuel2);
        System.out.println("stations = " + solution.stationsToString(stations2));
        System.out.println("Minimum refueling stops = " + result2);
        System.out.println("Note: For the exact numbers in the prompt, the correct result is 3.");
        System.out.println("Stopping only at mile 25 and mile 60 leaves insufficient fuel to reach 120.");
        System.out.println();

        int target3 = 1;
        int startFuel3 = 1;
        int[][] stations3 = {};
        int result3 = solution.minRefuelStops(target3, startFuel3, stations3);
        System.out.println("Additional Example 3:");
        System.out.println("target = " + target3);
        System.out.println("startFuel = " + startFuel3);
        System.out.println("stations = " + solution.stationsToString(stations3));
        System.out.println("Minimum refueling stops = " + result3);
        System.out.println();

        int target4 = 100;
        int startFuel4 = 1;
        int[][] stations4 = {
            {10, 100}
        };
        int result4 = solution.minRefuelStops(target4, startFuel4, stations4);
        System.out.println("Additional Example 4:");
        System.out.println("target = " + target4);
        System.out.println("startFuel = " + startFuel4);
        System.out.println("stations = " + solution.stationsToString(stations4));
        System.out.println("Minimum refueling stops = " + result4);
    }
}