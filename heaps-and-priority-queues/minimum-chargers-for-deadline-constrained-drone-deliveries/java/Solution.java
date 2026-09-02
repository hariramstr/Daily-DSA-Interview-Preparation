import java.util.*;

/*
 * Title: Minimum Chargers for Deadline-Constrained Drone Deliveries
 * Difficulty: Hard
 * Topic: Heaps and Priority Queues
 *
 * Problem Description:
 * A company operates a fleet of identical drones from a single depot. Each delivery request
 * is described by three integers: start[i], end[i], and charge[i]. The drone assigned to
 * request i must occupy one charging dock continuously from time start[i] until time end[i]
 * (inclusive of start, exclusive of end), and the dock must provide at least charge[i] units
 * of charging capacity while that request is active. A charging dock can serve at most one
 * drone at a time, but its installed capacity is fixed for the entire day and may be reused
 * by multiple non-overlapping requests.
 *
 * You may install any number of docks. The cost of a dock equals its capacity. Your goal is
 * to schedule all requests and choose dock capacities so that every request is assigned to
 * some dock whose capacity is at least the request's required charge, while minimizing the
 * total installation cost across all docks.
 *
 * Return the minimum possible total cost.
 *
 * This is not the same as minimizing the number of docks: sometimes using fewer high-capacity
 * docks is more expensive than using more lower-capacity docks, and vice versa. You must
 * optimize the sum of capacities of the installed docks.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - 1 <= start[i] < end[i] <= 10^9
 * - 1 <= charge[i] <= 10^9
 * - All values are integers.
 *
 * Key Insight:
 * We process requests in increasing start time.
 *
 * At any moment, some docks are currently occupied until certain end times, and some docks
 * have already been freed and are available for reuse. Every dock has a fixed capacity.
 *
 * For a new request with required charge c:
 * - If there exists a freed dock with capacity >= c, reusing one of them adds no new cost.
 * - To preserve larger docks for future larger requests, we should reuse the smallest freed
 *   dock whose capacity is still >= c.
 * - If no such freed dock exists, we must install a new dock of capacity c, increasing the
 *   answer by c.
 *
 * This greedy rule is optimal:
 * - Reusing a dock is always at least as good as creating a new one, because reuse adds zero cost.
 * - Among reusable docks, taking the smallest sufficient capacity is optimal by the standard
 *   exchange argument: using a larger dock when a smaller sufficient dock exists can only make
 *   future assignments harder, never easier.
 *
 * Data structures:
 * 1) A min-heap of currently occupied docks ordered by end time.
 *    Each entry stores: (endTime, capacity).
 *    Before processing a request starting at time s, we release all occupied docks with endTime <= s.
 *
 * 2) A TreeMap<Long, Integer> representing capacities of freed docks currently available for reuse.
 *    We need "smallest capacity >= required", which is exactly ceilingKey in TreeMap.
 *
 * Overall complexity:
 * - Sorting: O(n log n)
 * - Each request enters/leaves the occupied heap once: O(n log n)
 * - Each reuse/new allocation touches TreeMap in O(log n)
 * Total: O(n log n)
 */

public class Solution {

    /**
     * Small immutable holder for one delivery request.
     */
    private static final class Request {
        long start;
        long end;
        long charge;

        Request(long start, long end, long charge) {
            this.start = start;
            this.end = end;
            this.charge = charge;
        }
    }

    /**
     * Small holder for a dock that is currently occupied by some request.
     * We only need to know when it becomes free and what its fixed capacity is.
     */
    private static final class ActiveDock {
        long end;
        long capacity;

        ActiveDock(long end, long capacity) {
            this.end = end;
            this.capacity = capacity;
        }
    }

    /**
     * Computes the minimum total installation cost needed to serve all requests.
     *
     * Algorithm:
     * 1. Sort requests by start time ascending.
     * 2. Maintain a min-heap of active docks ordered by end time.
     * 3. Maintain a TreeMap of freed dock capacities currently available for reuse.
     * 4. For each request in start-time order:
     *    a) Release every active dock whose end <= current start.
     *       Those docks become available in the TreeMap.
     *    b) Find the smallest available capacity >= request charge.
     *       - If found, reuse it.
     *       - Otherwise, create a new dock of exactly request charge and add that cost.
     *    c) Put the chosen dock into the active heap until this request's end.
     *
     * Why this is correct:
     * - Any freed dock can be reused at zero extra cost.
     * - If multiple freed docks can handle the request, using the smallest sufficient one
     *   preserves larger capacities for future larger requests, which is never worse.
     *
     * @param requests an array where each element is [start, end, charge]
     * @return the minimum possible total installation cost
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long minimumTotalCost(int[][] requests) {
        int n = requests.length;
        Request[] arr = new Request[n];

        for (int i = 0; i < n; i++) {
            arr[i] = new Request(requests[i][0], requests[i][1], requests[i][2]);
        }

        Arrays.sort(arr, (a, b) -> {
            if (a.start != b.start) {
                return Long.compare(a.start, b.start);
            }
            if (a.end != b.end) {
                return Long.compare(a.end, b.end);
            }
            return Long.compare(a.charge, b.charge);
        });

        // Min-heap of docks currently in use, ordered by the time they become free.
        PriorityQueue<ActiveDock> active = new PriorityQueue<>(Comparator.comparingLong(d -> d.end));

        // Multiset of capacities of docks that are already installed and currently free.
        // Key   = capacity
        // Value = how many free docks of that capacity exist
        TreeMap<Long, Integer> freeCapacities = new TreeMap<>();

        long totalCost = 0L;

        for (Request req : arr) {
            // Step 1:
            // Release every dock whose current job ends at or before req.start.
            // Because intervals are [start, end), a dock ending exactly at req.start
            // is free in time for this request.
            while (!active.isEmpty() && active.peek().end <= req.start) {
                ActiveDock finished = active.poll();
                addCapacity(freeCapacities, finished.capacity);
            }

            // Step 2:
            // Among all free docks, find the smallest capacity that can still satisfy req.charge.
            // This is the best greedy choice because it wastes the least "power" and leaves
            // larger docks available for future larger requests.
            Long chosenCapacity = freeCapacities.ceilingKey(req.charge);

            if (chosenCapacity == null) {
                // No existing free dock can handle this request.
                // Therefore we must install a brand-new dock of exactly req.charge.
                chosenCapacity = req.charge;
                totalCost += chosenCapacity;
            } else {
                // Reuse an already installed free dock at zero additional cost.
                removeCapacity(freeCapacities, chosenCapacity);
            }

            // Step 3:
            // The chosen dock is now occupied until req.end.
            active.offer(new ActiveDock(req.end, chosenCapacity));
        }

        return totalCost;
    }

    /**
     * Convenience overload that accepts a List of int arrays.
     *
     * @param requests list of requests, each request is [start, end, charge]
     * @return the minimum possible total installation cost
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long minimumTotalCost(List<int[]> requests) {
        int[][] arr = new int[requests.size()][3];
        for (int i = 0; i < requests.size(); i++) {
            arr[i] = requests.get(i);
        }
        return minimumTotalCost(arr);
    }

    /**
     * Adds one occurrence of a capacity into the TreeMap multiset.
     *
     * @param map multiset of free dock capacities
     * @param capacity capacity to add
     * @return nothing
     * Time complexity: O(log n)
     * Space complexity: O(1) auxiliary
     */
    public void addCapacity(TreeMap<Long, Integer> map, long capacity) {
        map.put(capacity, map.getOrDefault(capacity, 0) + 1);
    }

    /**
     * Removes one occurrence of a capacity from the TreeMap multiset.
     * Assumes the capacity exists in the multiset.
     *
     * @param map multiset of free dock capacities
     * @param capacity capacity to remove
     * @return nothing
     * Time complexity: O(log n)
     * Space complexity: O(1) auxiliary
     */
    public void removeCapacity(TreeMap<Long, Integer> map, long capacity) {
        int count = map.get(capacity);
        if (count == 1) {
            map.remove(capacity);
        } else {
            map.put(capacity, count - 1);
        }
    }

    /**
     * Demonstrates the solution on the sample inputs from the statement.
     *
     * @param args command-line arguments, unused
     * @return nothing
     * Time complexity: O(1) for the fixed demo size, excluding the called algorithm
     * Space complexity: O(1) auxiliary, excluding the called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] requests1 = {
            {1, 4, 5},
            {2, 6, 3},
            {4, 7, 5}
        };
        long result1 = solution.minimumTotalCost(requests1);
        System.out.println(result1); // Expected: 8

        int[][] requests2 = {
            {1, 5, 8},
            {2, 3, 2},
            {3, 6, 6},
            {5, 8, 2}
        };
        long result2 = solution.minimumTotalCost(requests2);
        System.out.println(result2); // Expected: 10

        int[][] extra1 = {
            {1, 2, 4},
            {2, 3, 4},
            {3, 4, 4}
        };
        System.out.println(solution.minimumTotalCost(extra1)); // Expected: 4

        int[][] extra2 = {
            {1, 10, 5},
            {2, 9, 4},
            {3, 8, 3}
        };
        System.out.println(solution.minimumTotalCost(extra2)); // Expected: 12
    }
}