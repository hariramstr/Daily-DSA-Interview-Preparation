import java.util.*;

/*
 * Title: Minimum Dock Bays for Delayed Cargo Unloading
 * Difficulty: Hard
 * Topic: Heaps and Priority Queues
 *
 * Problem Description:
 * A shipping terminal receives cargo vessels, each with a scheduled arrival time and a fixed unloading duration.
 * However, the terminal follows a strict operational rule: if multiple ships are waiting, the next available dock bay
 * must always be assigned to the waiting ship with the smallest original arrival time; if there is still a tie,
 * assign the ship with the smaller input index. Once a ship starts unloading, it occupies its dock bay continuously
 * for its full duration. If no dock bay is free when a ship arrives, that ship waits until some bay becomes available.
 *
 * You are given two integer arrays arrival and unload, where arrival[i] is the scheduled arrival time of ship i and
 * unload[i] is the time required to unload it. You are also given an integer T. Determine the minimum number of dock
 * bays needed so that every ship can begin unloading no later than T time units after its scheduled arrival time.
 *
 * Formally, if ship i starts at time start[i], then start[i] must satisfy start[i] - arrival[i] <= T.
 * Ships cannot be reordered arbitrarily; whenever a dock becomes free, the terminal must choose the eligible waiting
 * ship according to the rule above. Your task is to compute the smallest number of dock bays that makes the schedule
 * feasible, or return -1 if it is impossible under the dispatch rule.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - 0 <= arrival[i] <= 10^9
 * - 1 <= unload[i] <= 10^9
 * - 0 <= T <= 10^9
 * - arrival is not guaranteed to be sorted
 *
 * Example 1:
 * Input: arrival = [1, 2, 4], unload = [5, 2, 3], T = 2
 * Output: 2
 *
 * Example 2:
 * Input: arrival = [0, 1, 1, 3], unload = [4, 2, 5, 1], T = 1
 * Output: 3
 */

public class Solution {

    /**
     * Small immutable record-like container for a ship after sorting by operational priority:
     * first by arrival time, then by original input index.
     */
    private static class Ship {
        int arrival;
        int unload;
        int index;

        Ship(int arrival, int unload, int index) {
            this.arrival = arrival;
            this.unload = unload;
            this.index = index;
        }
    }

    /**
     * Computes the minimum number of dock bays required so that every ship starts unloading
     * within T time units of its scheduled arrival, while strictly following the dispatch rule.
     *
     * Core idea:
     * 1. Sort ships by (arrival time, index). This is exactly the priority order among waiting ships.
     * 2. For a fixed number of bays k, simulate the terminal:
     *    - Maintain currently busy bays by their finish times in a min-heap.
     *    - Process ships in sorted order.
     *    - If a bay is free by the ship's arrival, the ship starts immediately at its arrival.
     *    - Otherwise, the ship must wait for the earliest finishing bay, because all earlier-priority
     *      waiting ships are exactly the ships we process before it in sorted order.
     * 3. Feasibility is monotonic:
     *    - If k bays are enough, then any larger number of bays is also enough.
     *    - Therefore, binary search the minimum feasible k.
     *
     * Why processing ships in sorted order is correct:
     * - The dispatch rule says that among waiting ships, the one with smallest original arrival time
     *   (and then smaller index) must go next.
     * - After sorting by (arrival, index), this order is fixed.
     * - A later ship can never start before an earlier-priority waiting ship.
     * - Therefore, when simulating in sorted order, each ship either:
     *   a) starts at its own arrival if some bay is already free, or
     *   b) starts at the earliest future time any bay becomes free after all earlier-priority ships
     *      have already been assigned.
     *
     * @param arrival the scheduled arrival times of ships
     * @param unload the unloading durations of ships
     * @param T the maximum allowed waiting time for every ship
     * @return the minimum number of dock bays needed, or -1 if impossible
     * @implNote Time complexity: O(n log n + n log n log n) = O(n log^2 n) in the worst case
     *           because we sort once and then binary search over bay count, each feasibility check
     *           using a heap.
     * @implNote Space complexity: O(n) for the sorted ship array and heap.
     */
    public int minimumDockBays(int[] arrival, int[] unload, int T) {
        int n = arrival.length;
        Ship[] ships = buildAndSortShips(arrival, unload);

        int left = 1;
        int right = n;
        int answer = -1;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (canScheduleWithKBays(ships, T, mid)) {
                answer = mid;
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        return answer;
    }

    /**
     * Builds ship objects and sorts them by the exact waiting priority:
     * first by arrival time, then by original input index.
     *
     * @param arrival the scheduled arrival times
     * @param unload the unloading durations
     * @return a sorted array of ships
     * @implNote Time complexity: O(n log n)
     * @implNote Space complexity: O(n)
     */
    public Ship[] buildAndSortShips(int[] arrival, int[] unload) {
        int n = arrival.length;
        Ship[] ships = new Ship[n];

        for (int i = 0; i < n; i++) {
            ships[i] = new Ship(arrival[i], unload[i], i);
        }

        Arrays.sort(ships, (a, b) -> {
            if (a.arrival != b.arrival) {
                return Integer.compare(a.arrival, b.arrival);
            }
            return Integer.compare(a.index, b.index);
        });

        return ships;
    }

    /**
     * Checks whether k dock bays are sufficient.
     *
     * Detailed simulation logic:
     * - We use a min-heap of finish times of currently occupied bays.
     * - The heap size is at most k.
     *
     * For each ship in sorted priority order:
     * 1. Release all bays that have already finished by the ship's arrival time.
     *    These bays are immediately available.
     * 2. If fewer than k bays are busy now, at least one bay is free, so the ship starts at arrival.
     * 3. Otherwise, all k bays are busy at arrival:
     *    - The ship must wait for the earliest finishing bay.
     *    - Its start time becomes that earliest finish time.
     * 4. If start - arrival > T, then k bays are not enough.
     * 5. Insert the ship's new finish time into the heap.
     *
     * Important correctness intuition:
     * - Because ships are processed in the exact dispatch priority order, any ship that is waiting
     *   must be served before all later ships.
     * - Thus, when all bays are busy, the current ship can only start when the earliest bay frees up
     *   after all earlier ships have already been assigned.
     *
     * @param ships ships sorted by (arrival, index)
     * @param T maximum allowed waiting time
     * @param k number of dock bays to test
     * @return true if all ships can start within T waiting time using k bays; false otherwise
     * @implNote Time complexity: O(n log k)
     * @implNote Space complexity: O(k)
     */
    public boolean canScheduleWithKBays(Ship[] ships, int T, int k) {
        PriorityQueue<Long> busyUntil = new PriorityQueue<>();

        for (Ship ship : ships) {
            long arrivalTime = ship.arrival;
            long unloadTime = ship.unload;

            // Step 1:
            // Remove every bay that has already become free by this ship's arrival time.
            // After this loop, the heap contains only bays still busy strictly after arrivalTime.
            while (!busyUntil.isEmpty() && busyUntil.peek() <= arrivalTime) {
                busyUntil.poll();
            }

            long startTime;

            // Step 2:
            // If we currently use fewer than k bays, then at least one bay is free right now.
            // So this ship starts immediately at its arrival time.
            if (busyUntil.size() < k) {
                startTime = arrivalTime;
            } else {
                // Step 3:
                // All k bays are busy at arrivalTime.
                // The ship must wait for the earliest bay to become free.
                long earliestFree = busyUntil.poll();
                startTime = earliestFree;
            }

            // Step 4:
            // Enforce the waiting-time constraint.
            if (startTime - arrivalTime > T) {
                return false;
            }

            // Step 5:
            // Occupy one bay until startTime + unloadTime.
            busyUntil.offer(startTime + unloadTime);
        }

        return true;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * @implNote Time complexity: O(1) for the fixed demo inputs, excluding the called algorithm
     * @implNote Space complexity: O(1), excluding the called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] arrival1 = {1, 2, 4};
        int[] unload1 = {5, 2, 3};
        int T1 = 2;
        System.out.println(solution.minimumDockBays(arrival1, unload1, T1)); // Expected: 2

        int[] arrival2 = {0, 1, 1, 3};
        int[] unload2 = {4, 2, 5, 1};
        int T2 = 1;
        System.out.println(solution.minimumDockBays(arrival2, unload2, T2)); // Expected: 3

        int[] arrival3 = {0};
        int[] unload3 = {10};
        int T3 = 0;
        System.out.println(solution.minimumDockBays(arrival3, unload3, T3)); // Expected: 1

        int[] arrival4 = {0, 0, 0};
        int[] unload4 = {5, 5, 5};
        int T4 = 0;
        System.out.println(solution.minimumDockBays(arrival4, unload4, T4)); // Expected: 3
    }
}