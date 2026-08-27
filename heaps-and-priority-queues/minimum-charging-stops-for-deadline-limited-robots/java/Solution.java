import java.util.*;

/*
Problem Title: Minimum Charging Stops for Deadline-Limited Robots

Problem Description:
A warehouse robot starts at position 0 with initial battery startCharge and must reach
position target on a straight track. Moving 1 unit of distance consumes 1 unit of battery.
Along the track there are charging stations, where station i is described by
[position_i, charge_i, expiry_i].

If the robot arrives at that station at time t (time equals total distance already traveled),
it may collect the full charge_i only if t <= expiry_i. Otherwise that station has already
shut down and provides nothing. Charging itself takes no extra time, and the robot may choose
whether or not to use an available station when it passes it. The robot cannot move backward.

Return the minimum number of charging stops needed to reach target, or -1 if it is impossible.

This is harder than the classic refueling setup because a station that is physically reachable
may still become unusable if the robot delays too much, so the algorithm must decide which
previously seen but still valid stations to activate in order to extend the route with as few
stops as possible. A priority queue based strategy is expected.
*/

public class Solution {

    /**
     * Computes the minimum number of charging stops needed to reach the target.
     *
     * Core idea:
     * 1. Sort stations by position because the robot moves only forward.
     * 2. A station is usable if and only if the robot reaches its position no later than its expiry.
     *    Since time equals distance traveled, arriving at position p always happens at time p.
     *    Therefore station i is usable exactly when position_i <= expiry_i.
     * 3. After that observation, the problem becomes the classic minimum refueling stops problem:
     *    among all stations already passed and usable, whenever we cannot move farther, we greedily
     *    take the largest available charge first. This greedy choice minimizes the number of stops.
     *
     * Very important simplification:
     * Because charging takes zero time and the robot cannot move backward, "using a previously seen
     * station later" is equivalent to having collected its charge when passing it. The only thing that
     * matters is whether the station was valid at the moment it was passed, which depends only on its
     * own position and expiry, not on later decisions.
     *
     * @param target the destination position the robot must reach
     * @param startCharge the initial battery charge available at position 0
     * @param stations each station is [position, charge, expiry]
     * @return the minimum number of charging stops needed, or -1 if reaching target is impossible
     * Time complexity: O(n log n), where n is the number of stations
     * Space complexity: O(n)
     */
    public int minChargingStops(int target, int startCharge, int[][] stations) {
        // Defensive handling: if target is already at or before start, no stops are needed.
        if (target <= 0) {
            return 0;
        }

        // Sort stations by position so we process them in the exact order the robot encounters them.
        Arrays.sort(stations, Comparator.comparingInt(a -> a[0]));

        // Max-heap storing charges of all stations that:
        // 1) are at positions already reachable/passed, and
        // 2) are actually usable when passed (position <= expiry).
        //
        // Why max-heap?
        // Whenever we are stuck, taking the largest available charge gives the biggest extension
        // of reachable distance per stop, which is the standard greedy strategy for minimizing
        // the number of stops.
        PriorityQueue<Integer> availableCharges = new PriorityQueue<>(Collections.reverseOrder());

        // "reachable" means the farthest position we can currently get to with the charge collected so far.
        long reachable = startCharge;

        // Number of charging stops used.
        int stops = 0;

        // Index of the next station not yet processed.
        int i = 0;
        int n = stations.length;

        // Keep expanding the reachable distance until we can reach target or prove impossible.
        while (reachable < target) {

            // Add every station whose position is already within current reachable range.
            // These are the stations we can pass before running out.
            while (i < n && stations[i][0] <= reachable) {
                int position = stations[i][0];
                int charge = stations[i][1];
                int expiry = stations[i][2];

                // A station is usable exactly when the robot reaches it no later than expiry.
                // Since arrival time equals traveled distance and the station is at "position",
                // arrival time there is exactly "position".
                //
                // Therefore:
                // usable <=> position <= expiry
                //
                // If this condition fails, the station is already shut down when the robot arrives,
                // so it contributes nothing and must be ignored.
                if (position <= expiry) {
                    availableCharges.offer(charge);
                }

                i++;
            }

            // If no usable passed station is available and target is still out of reach,
            // then the robot is stuck forever.
            if (availableCharges.isEmpty()) {
                return -1;
            }

            // Greedily use the largest available charge among all passed usable stations.
            // This is the key step that minimizes the number of stops.
            reachable += availableCharges.poll();
            stops++;
        }

        return stops;
    }

    /**
     * Helper method to print a station list in a readable form.
     *
     * @param stations the station array
     * @return a string representation of the stations
     * Time complexity: O(n)
     * Space complexity: O(n) due to string construction
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
     * Demonstrates the solution on the sample inputs and a few additional checks.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total input size log total input size) across the demonstrations
     * Space complexity: O(total input size)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int target1 = 25;
        int startCharge1 = 10;
        int[][] stations1 = {
                {5, 8, 7},
                {9, 7, 12},
                {14, 10, 20}
        };
        int result1 = solution.minChargingStops(target1, startCharge1, stations1);
        System.out.println("Example 1");
        System.out.println("target = " + target1);
        System.out.println("startCharge = " + startCharge1);
        System.out.println("stations = " + solution.stationsToString(stations1));
        System.out.println("Output = " + result1);
        System.out.println("Expected = 2");
        System.out.println();

        int target2 = 30;
        int startCharge2 = 8;
        int[][] stations2 = {
                {6, 5, 5},
                {7, 20, 6},
                {10, 10, 15}
        };
        int result2 = solution.minChargingStops(target2, startCharge2, stations2);
        System.out.println("Example 2");
        System.out.println("target = " + target2);
        System.out.println("startCharge = " + startCharge2);
        System.out.println("stations = " + solution.stationsToString(stations2));
        System.out.println("Output = " + result2);
        System.out.println("Expected = -1");
        System.out.println();

        int target3 = 100;
        int startCharge3 = 10;
        int[][] stations3 = {
                {10, 60, 100},
                {20, 30, 100},
                {30, 30, 100},
                {60, 40, 100}
        };
        int result3 = solution.minChargingStops(target3, startCharge3, stations3);
        System.out.println("Additional Example 3");
        System.out.println("target = " + target3);
        System.out.println("startCharge = " + startCharge3);
        System.out.println("stations = " + solution.stationsToString(stations3));
        System.out.println("Output = " + result3);
        System.out.println("Expected = 2");
        System.out.println();

        int target4 = 50;
        int startCharge4 = 50;
        int[][] stations4 = {
                {10, 10, 10},
                {20, 20, 20}
        };
        int result4 = solution.minChargingStops(target4, startCharge4, stations4);
        System.out.println("Additional Example 4");
        System.out.println("target = " + target4);
        System.out.println("startCharge = " + startCharge4);
        System.out.println("stations = " + solution.stationsToString(stations4));
        System.out.println("Output = " + result4);
        System.out.println("Expected = 0");
    }
}