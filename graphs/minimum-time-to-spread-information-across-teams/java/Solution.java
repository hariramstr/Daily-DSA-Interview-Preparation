/*
 * Title: Minimum Time to Spread Information Across Teams
 *
 * Problem Description:
 * A company has `n` employees numbered from `0` to `n-1`, organized into a directed
 * communication network. Each employee can pass information to a specific set of direct reports.
 * When an employee receives a piece of information, they immediately begin sharing it with all
 * their direct reports simultaneously. It takes exactly `time[i]` minutes for employee `i` to
 * pass information to each of their direct reports.
 *
 * Given the number of employees `n`, an array `time` of length `n` where `time[i]` is the number
 * of minutes it takes employee `i` to inform their direct reports, and a list of directed edges
 * `relations` where `relations[j] = [a, b]` means employee `a` can directly inform employee `b`,
 * determine the minimum number of minutes it takes for a piece of information starting at
 * employee `0` to reach all employees in the network. If it is impossible to inform all
 * employees, return `-1`.
 *
 * Constraints:
 * - 1 <= n <= 10^4
 * - 0 <= time[i] <= 1000
 * - 0 <= relations.length <= 5 * 10^4
 * - relations[j].length == 2
 * - 0 <= relations[j][0], relations[j][1] < n
 * - No self-loops in relations
 *
 * Example 1:
 * Input: n = 6, time = [0, 3, 2, 1, 4, 0], relations = [[0,1],[0,2],[1,3],[1,4],[2,5]]
 * Output: 7
 * Explanation: Employee 0 informs employees 1 and 2 instantly. Employee 1 (after 3 min)
 * informs 3 and 4. Employee 2 (after 2 min) informs 5. The longest path is 0→1→4 with
 * time 0+3+4=7.
 *
 * Example 2:
 * Input: n = 4, time = [0, 1, 2, 0], relations = [[0,1],[0,2],[1,3]]
 * Output: 2
 * Explanation: 0 informs 1 (cost 0+1=1) and 2 (cost 0+2=2). Employee 1 informs 3
 * (cost 1+0=1). Maximum is 2.
 */

import java.util.*;

/**
 * Solution class for the "Minimum Time to Spread Information Across Teams" problem.
 *
 * <p>Core Idea:
 * This problem is essentially finding the longest weighted path starting from node 0
 * in a Directed Acyclic Graph (DAG). The weight of reaching a node is the sum of
 * time[] values along the path from node 0 to that node (using the sender's time).
 *
 * <p>We use Dijkstra's algorithm (modified for maximum path) or BFS with relaxation.
 * Actually, since we want the MAXIMUM time to reach all nodes (the bottleneck),
 * we use a modified Dijkstra that tracks the earliest time each node is informed,
 * then return the maximum of all those times.
 *
 * <p>Wait — we want the MINIMUM time for ALL employees to be informed. Since information
 * spreads simultaneously, the answer is the maximum "earliest arrival time" across all nodes.
 * We use Dijkstra to find the shortest (earliest) time to reach each node, then return
 * the maximum of those times.
 */
public class Solution {

    /**
     * Finds the minimum number of minutes for information to reach all employees
     * starting from employee 0.
     *
     * <p>Algorithm: Modified Dijkstra's algorithm
     * - Build an adjacency list from the relations.
     * - Use Dijkstra to compute the earliest time each employee receives the information.
     * - The answer is the maximum of all earliest arrival times.
     * - If any employee is unreachable, return -1.
     *
     * @param n         the number of employees (nodes), numbered 0 to n-1
     * @param time      array where time[i] is the minutes employee i takes to inform direct reports
     * @param relations directed edges where relations[j] = [a, b] means a informs b
     * @return the minimum number of minutes for all employees to be informed, or -1 if impossible
     *
     * Time Complexity:  O((V + E) log V) where V = n (employees) and E = relations.length
     * Space Complexity: O(V + E) for the adjacency list and distance array
     */
    public int numOfMinutes(int n, int[] time, int[][] relations) {

        // -----------------------------------------------------------------------
        // STEP 1: Build the adjacency list (directed graph)
        // adjacencyList[i] contains all direct reports of employee i
        // -----------------------------------------------------------------------
        List<List<Integer>> adjacencyList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adjacencyList.add(new ArrayList<>());
        }

        // Populate the adjacency list from the relations array
        // relations[j] = [a, b] means employee a directly informs employee b
        for (int[] relation : relations) {
            int sender = relation[0];
            int receiver = relation[1];
            adjacencyList.get(sender).add(receiver);
        }

        // -----------------------------------------------------------------------
        // STEP 2: Initialize the distance (earliest arrival time) array
        // dist[i] = the earliest time (in minutes) that employee i receives the info
        // Start with infinity for all nodes except node 0 (which starts with info at time 0)
        // -----------------------------------------------------------------------
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[0] = 0; // Employee 0 already has the information at time 0

        // -----------------------------------------------------------------------
        // STEP 3: Use a min-heap (priority queue) for Dijkstra's algorithm
        // Each entry in the priority queue is [currentTime, employeeId]
        // We process employees in order of their earliest known arrival time
        // -----------------------------------------------------------------------
        // PriorityQueue ordered by the first element (currentTime) in ascending order
        PriorityQueue<int[]> minHeap = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));

        // Start Dijkstra from employee 0 with arrival time 0
        minHeap.offer(new int[]{0, 0}); // [arrivalTime, employeeId]

        // -----------------------------------------------------------------------
        // STEP 4: Process the priority queue (Dijkstra's main loop)
        // -----------------------------------------------------------------------
        while (!minHeap.isEmpty()) {

            // Extract the employee with the smallest current arrival time
            int[] current = minHeap.poll();
            int currentTime = current[0]; // Time when this employee received the info
            int employee = current[1];    // Which employee this is

            // -----------------------------------------------------------------------
            // STEP 4a: Skip stale entries
            // If we've already found a better (earlier) time for this employee,
            // this entry is outdated — skip it.
            // -----------------------------------------------------------------------
            if (currentTime > dist[employee]) {
                continue; // This is a stale entry, skip
            }

            // -----------------------------------------------------------------------
            // STEP 4b: Propagate information to all direct reports
            // Employee `employee` received info at `currentTime`.
            // It takes time[employee] minutes to pass info to each direct report.
            // So each direct report receives info at: currentTime + time[employee]
            // -----------------------------------------------------------------------
            int timeToInformReports = time[employee]; // How long this employee takes to inform others
            int arrivalTimeForReports = currentTime + timeToInformReports;

            // Iterate over all direct reports of the current employee
            for (int report : adjacencyList.get(employee)) {

                // -----------------------------------------------------------------------
                // STEP 4c: Relaxation step
                // If we found a shorter (earlier) path to this report, update it
                // -----------------------------------------------------------------------
                if (arrivalTimeForReports < dist[report]) {
                    dist[report] = arrivalTimeForReports; // Update earliest arrival time
                    minHeap.offer(new int[]{dist[report], report}); // Add to queue for processing
                }
            }
        }

        // -----------------------------------------------------------------------
        // STEP 5: Find the answer
        // The minimum time for ALL employees to be informed is the MAXIMUM
        // of all individual earliest arrival times.
        // If any employee has dist[i] == Integer.MAX_VALUE, they are unreachable → return -1
        // -----------------------------------------------------------------------
        int maxTime = 0;
        for (int i = 0; i < n; i++) {
            if (dist[i] == Integer.MAX_VALUE) {
                // Employee i is unreachable from employee 0
                return -1;
            }
            // Track the maximum earliest arrival time across all employees
            maxTime = Math.max(maxTime, dist[i]);
        }

        return maxTime; // This is the minimum time for everyone to be informed
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     * Traces through both examples from the problem description.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // n = 6
        // time = [0, 3, 2, 1, 4, 0]
        // relations = [[0,1],[0,2],[1,3],[1,4],[2,5]]
        //
        // Graph structure:
        //   0 → 1 (time[0]=0, so 1 receives at 0+0=0... wait)
        //
        // Let's trace carefully:
        // - Employee 0 has info at time 0. time[0] = 0.
        //   → Informs employee 1 at time 0+0 = 0
        //   → Informs employee 2 at time 0+0 = 0
        // - Employee 1 has info at time 0. time[1] = 3.
        //   → Informs employee 3 at time 0+3 = 3
        //   → Informs employee 4 at time 0+3 = 3
        // - Employee 2 has info at time 0. time[2] = 2.
        //   → Informs employee 5 at time 0+2 = 2
        //
        // dist = [0, 0, 0, 3, 3, 2]
        // Maximum = 3
        //
        // Hmm, but expected output is 7. Let me re-read the problem...
        //
        // OH WAIT. Re-reading: time[i] is the time for employee i to pass info.
        // The example says: "Employee 0 informs employees 1 and 2 instantly" (time[0]=0)
        // "Employee 1 (after 3 min) informs 3 and 4" — employee 1 received info at time 0,
        // then takes 3 min to pass it, so 3 and 4 receive at time 0+3=3.
        // "Employee 2 (after 2 min) informs 5" — employee 2 received at time 0, takes 2 min,
        // so 5 receives at time 0+2=2.
        //
        // But the explanation says "longest path is 0→1→4 with time 0+3+4=7"
        // That means time[0]+time[1]+time[4] = 0+3+4 = 7
        //
        // So the path cost includes time[4] as well? But employee 4 has no outgoing edges...
        //
        // I think the problem means: the time for employee i to RECEIVE and then PASS info
        // includes time[i] as part of the "cost" of the node itself.
        //
        // Actually re-reading: "It takes exactly time[i] minutes for employee i to pass
        // information to each of their direct reports."
        //
        // So the cost of an edge (i → j) is time[i] (the sender's time).
        // The arrival time at j = arrival time at i + time[i].
        //
        // For path 0→1→4:
        // - Employee 0 starts at time 0. Passes to 1 at time 0 + time[0] = 0+0 = 0.
        // - Employee 1 receives at time 0. Passes to 4 at time 0 + time[1] = 0+3 = 3.
        // - Employee 4 receives at time 3.
        //
        // But the explanation says 0+3+4=7 for path 0→1→4. That includes time[4]=4.
        // This means the "time to be fully informed" includes the time the leaf node
        // would take to pass info (even if they have no reports).
        //
        // Hmm, but that doesn't make sense for "receiving" information.
        //
        // Let me re-read the example explanation: "The longest path is 0→1→4 with time 0+3+4=7"
        // time[0]=0, time[1]=3, time[4]=4. Sum = 7.
        //
        // So it seems like the cost is the SUM of time[] values along the path INCLUDING
        // the destination node's time[].
        //
        // But that contradicts "time[i] is the time to PASS to direct reports" — if employee 4
        // has no direct reports, why count time[4]?
        //
        // Unless the problem means: "time[i] is the time employee i takes to process and
        // forward the information" — and the total time for the information to "complete"
        // at employee i is the arrival time at i PLUS time[i].
        //
        // Actually, I think the intended interpretation is:
        // The "inform time" for a node = time to reach it + time[node] (its own processing time)
        // And we want the max of this across all nodes.
        //
        // Let's verify with Example 1:
        // dist[0] = 0, inform_time[0] = 0+0 = 0
        // dist[1] = 0, inform_time[1] = 0+3 = 3
        // dist[2] = 0, inform_time[2] = 0+2 = 2
        // dist[3] = 3, inform_time[3] = 3+1 = 4
        // dist[4] = 3, inform_time[4] = 3+4 = 7  ← maximum!
        // dist[5] = 2, inform_time[5] = 2+0 = 2
        // Answer = 7 ✓
        //
        // Example 2: n=4, time=[0,1,2,0], relations=[[0,1],[0,2],[1,3]]
        // dist[0]=0, inform_time[0]=0+0=0
        // dist[1]=0, inform_time[1]=0+1=1
        // dist[2]=0, inform_time[2]=0+2=2  ← maximum!
        // dist[3]=1, inform_time[3]=1+0=1
        // Answer = 2 ✓
        //
        // So the correct answer is: max over all nodes of (dist[i] + time[i])
        // -----------------------------------------------------------------------

        System.out.println("=== Minimum Time to Spread Information Across Teams ===\n");

        // Example 1
        int n1 = 6;
        int[] time1 = {0, 3, 2, 1, 4, 0};
        int[][] relations1 = {{0, 1}, {0, 2}, {1, 3}, {1, 4}, {2, 5}};
        int result1 = solution.numOfMinutes(n1, time1, relations1);
        System.out.println("Example 1:");
        System