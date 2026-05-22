/*
 * Title: Minimum Time to Spread Information Across Teams
 * Difficulty: Medium
 * Topic: Graphs
 *
 * Problem Description:
 * A company has `n` employees numbered from 0 to n-1, organized into a directed
 * communication network. Each employee can pass information to a specific set of
 * direct reports. When an employee receives information, they immediately begin
 * sharing it with all their direct reports simultaneously. It takes exactly time[i]
 * minutes for employee i to pass information to each of their direct reports.
 *
 * Given n, an array time[], and directed edges relations where relations[j] = [a, b]
 * means employee a can directly inform employee b, determine the minimum number of
 * minutes for information starting at employee 0 to reach ALL employees.
 * Return -1 if it is impossible to inform all employees.
 *
 * Example 1:
 *   n=6, time=[0,3,2,1,4,0], relations=[[0,1],[0,2],[1,3],[1,4],[2,5]]
 *   Output: 7  (path 0->1->4: 0+3+4=7 is the longest)
 *
 * Example 2:
 *   n=4, time=[0,1,2,0], relations=[[0,1],[0,2],[1,3]]
 *   Output: 2  (path 0->2 costs 0+2=2, path 0->1->3 costs 0+1+0=1, max=2)
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class containing the algorithm
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /// <summary>
    /// Finds the minimum time for information to spread from employee 0 to all employees.
    ///
    /// APPROACH: Modified Dijkstra's Algorithm (Shortest-Path / Longest-Arrival-Time)
    ///
    /// Key Insight:
    ///   - We want the EARLIEST time each employee can receive the information.
    ///   - Employee 0 starts with the information at time 0.
    ///   - When employee u receives info at time T, they start informing their direct
    ///     reports. Each direct report v receives info at time T + time[u].
    ///   - This is exactly Dijkstra's shortest-path problem where:
    ///       dist[v] = min over all paths from 0 to v of (sum of time[] along the path,
    ///                 NOT counting time[v] itself, because time[v] is the cost to
    ///                 SEND from v, not to RECEIVE at v).
    ///   - The answer is the MAXIMUM dist[] value across all employees (the last one
    ///     to be informed). If any employee is unreachable, return -1.
    ///
    /// Why Dijkstra?
    ///   - Edge weights (time[i]) are non-negative, so Dijkstra is optimal.
    ///   - We process employees in order of earliest arrival time, guaranteeing
    ///     that when we first pop a node from the priority queue, we have found
    ///     its shortest (earliest) arrival time.
    ///
    /// Time Complexity:  O((n + E) log n)  where E = number of relations
    ///   - Each node is processed once; each edge is relaxed once.
    ///   - Priority queue operations cost O(log n).
    ///
    /// Space Complexity: O(n + E)
    ///   - Adjacency list stores all edges.
    ///   - dist[] array of size n.
    ///   - Priority queue holds at most O(n + E) entries in the worst case.
    /// </summary>
    public int MinimumTime(int n, int[] time, int[][] relations)
    {
        // ── Step 1: Build the adjacency list ──────────────────────────────────
        // We represent the directed graph as a list of neighbors for each node.
        // adjacency[u] contains all employees v such that u can directly inform v.
        //
        // Why a List<List<int>>?
        //   - Fast O(1) access by index.
        //   - Dynamic size so we don't need to know the degree of each node upfront.

        var adjacency = new List<List<int>>(n);
        for (int i = 0; i < n; i++)
            adjacency.Add(new List<int>());

        foreach (var rel in relations)
        {
            int from = rel[0];
            int to   = rel[1];
            // Directed edge: 'from' can inform 'to'
            adjacency[from].Add(to);
        }

        // ── Step 2: Initialize the distance (earliest arrival time) array ─────
        // dist[i] = the earliest time (in minutes) at which employee i receives
        //           the information.
        //
        // We start with dist[0] = 0 (employee 0 already has the info at time 0).
        // All other employees start at "infinity" (not yet reachable).

        int[] dist = new int[n];
        for (int i = 0; i < n; i++)
            dist[i] = int.MaxValue;

        dist[0] = 0; // Employee 0 has the information at time 0

        // ── Step 3: Set up the min-heap (priority queue) ──────────────────────
        // We use a min-heap ordered by arrival time so that we always process
        // the employee who receives the information EARLIEST next.
        //
        // Each entry is (arrivalTime, employeeId).
        //
        // Why a min-heap?
        //   - Dijkstra's correctness relies on processing nodes in non-decreasing
        //     order of their tentative shortest distance.
        //   - .NET 8's PriorityQueue<TElement, TPriority> is a built-in min-heap.

        // PriorityQueue<element, priority> — lower priority value = dequeued first
        var pq = new PriorityQueue<int, int>(); // (employeeId, arrivalTime)

        // Enqueue employee 0 with arrival time 0
        pq.Enqueue(0, 0);

        // ── Step 4: Dijkstra's main loop ──────────────────────────────────────
        // We repeatedly extract the employee with the smallest known arrival time
        // and try to "relax" (improve) the arrival times of their direct reports.

        while (pq.Count > 0)
        {
            // Dequeue the employee with the earliest arrival time
            pq.Dequeue(out int u, out int arrivalTime);

            // ── Stale entry check ─────────────────────────────────────────────
            // Because we may enqueue the same employee multiple times with
            // different (improving) arrival times, we might pop an outdated entry.
            // If the popped arrival time is worse than what we already know, skip it.
            if (arrivalTime > dist[u])
                continue; // This is a stale entry; we already found a better path

            // ── Relax edges from u ────────────────────────────────────────────
            // Employee u received info at time dist[u].
            // It takes time[u] minutes for u to pass info to each direct report.
            // So each direct report v can receive info at time: dist[u] + time[u].

            int sendTime = dist[u] + time[u];
            // 'sendTime' is the time at which u FINISHES informing its direct reports,
            // i.e., the earliest time any direct report of u can receive the info.

            foreach (int v in adjacency[u])
            {
                // Check if we found a shorter (earlier) path to v
                if (sendTime < dist[v])
                {
                    // Update v's earliest arrival time
                    dist[v] = sendTime;

                    // Enqueue v with its new (improved) arrival time
                    // We may enqueue v multiple times; stale entries are skipped above
                    pq.Enqueue(v, sendTime);
                }
            }
        }

        // ── Step 5: Determine the answer ──────────────────────────────────────
        // After Dijkstra completes, dist[i] holds the earliest time employee i
        // receives the information (or int.MaxValue if unreachable).
        //
        // The answer is the MAXIMUM dist[i] across all employees, because we need
        // ALL employees to be informed — we must wait for the last one.
        //
        // If any employee is still at int.MaxValue, they are unreachable → return -1.

        int maxTime = 0;
        for (int i = 0; i < n; i++)
        {
            if (dist[i] == int.MaxValue)
                return -1; // Employee i is unreachable from employee 0

            maxTime = Math.Max(maxTime, dist[i]);
        }

        return maxTime;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Test Code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

var solution = new Solution();

// ── Example 1 ─────────────────────────────────────────────────────────────────
// n=6, time=[0,3,2,1,4,0]
// relations: 0->1, 0->2, 1->3, 1->4, 2->5
//
// Trace:
//   dist = [0, ∞, ∞, ∞, ∞, ∞]
//   Process 0 (arrival=0): sendTime = 0+0 = 0
//     → dist[1] = 0+0 = 0... wait, time[0]=0, so sendTime=0
//     Actually time=[0,3,2,1,4,0], so time[0]=0
//     sendTime for 0 = dist[0] + time[0] = 0 + 0 = 0
//     dist[1] = 0, dist[2] = 0
//   Process 1 (arrival=0): sendTime = 0 + time[1] = 0 + 3 = 3
//     dist[3] = 3, dist[4] = 3
//   Process 2 (arrival=0): sendTime = 0 + time[2] = 0 + 2 = 2
//     dist[5] = 2
//   Process 5 (arrival=2): no outgoing edges
//   Process 3 (arrival=3): no outgoing edges
//   Process 4 (arrival=3): no outgoing edges
//   dist = [0, 0, 0, 3, 3, 2]
//   max = 3... Hmm, that gives 3, but expected 7.
//
// Wait — re-reading: time[i] is the time for employee i to inform their reports.
// So employee 1 receives info at time dist[1]. Then it takes time[1]=3 minutes
// for employee 1 to inform 3 and 4. So dist[3] = dist[1] + time[1] = 0 + 3 = 3.
// And dist[4] = 0 + 3 = 3. But expected answer is 7 (path 0->1->4 with 0+3+4=7).
//
// Hmm, 0+3+4=7 means: time[0]=0 (0 informs 1), time[1]=3 (1 informs 4), time[4]=4.
// But time[4] is the time for employee 4 to inform ITS reports — employee 4 has no
// reports! So why is time[4]=4 counted?
//
// Re-reading problem: "It takes exactly time[i] minutes for employee i to pass
// information to each of their direct reports."
// The example says "0→1→4 with time 0+3+4=7". This means time[4]=4 IS counted.
//
// So the cost of a PATH includes time[last_node] as well?
// That means the arrival time at v = dist[u] + time[u], and the "completion" time
// for v = dist[v] + time[v] (even if v has no reports).
//
// Actually re-reading: the answer is when ALL employees have RECEIVED the info,
// not when they finish passing it. So dist[4] = dist[1] + time[1] = 0+3 = 3.
// But the example says 7...
//
// Let me re-read the example explanation:
// "Employee 1 (after 3 min) informs 3 and 4."
// So employee 1 receives info at time 0 (from employee 0 instantly since time[0]=0).
// Then employee 1 takes 3 minutes to inform 3 and 4. So 3 and 4 receive at time 3.
// But the answer is 7, not 3. The example says "0+3+4=7".
//
// This suggests time[4]=4 is also added. Maybe the problem means the time for the
// LAST employee to FINISH informing (even if they have no reports)?
// i.e., the answer = max over all employees i of (dist[i] + time[i])?
//
// Let's verify: dist=[0,0,0,3,3,2], time=[0,3,2,1,4,0]
// dist[i]+time[i] = [0,3,2,4,7,2] → max = 7. ✓
//
// Example 2: n=4, time=[0,1,2,0], relations=[[0,1],[0,2],[1,3]]
// dist[0]=0, dist[1]=0+0=0, dist[2]=0+0=0, dist[3]=0+1=1
// dist[i]+time[i] = [0,1,2,1] → max=2. ✓
//
// So the answer is max(dist[i] + time[i]) for all i!
// This makes sense: "informing" is complete when the last employee finishes
// passing the info (or receives it if they have no one to pass to — but time[i]
// still counts as the "processing" time).
//
// Actually wait — if an employee has no direct reports, time[i] shouldn't matter
// for the answer since they don't need to inform anyone. But the example counts it.
//
// Looking again at example 1: employee 4 has no outgoing edges (no direct reports).
// Yet time[4]=4 is counted. This is unusual. Let me re-read...
//
// "determine the minimum number of minutes it takes for a piece of information
// starting at employee 0 to reach all employees"
//
// "reach all employees" — so we just need everyone to RECEIVE it, not pass it on.
// dist[4]=3 means employee 4 receives at time 3. Answer should be max(dist)=3.
// But expected is 7. There's a contradiction with the problem statement vs example.
//
// The example explanation says "The longest path is 0→1→4 with time 0+3+4=7."
// This strongly implies we sum time[] for ALL nodes on the path including the last.
// So the model is: dist[v] = dist[u] + time[u] + time[v]? No, that double-counts.
//
// Alternative model: dist[v] = dist[u] + time[u], and the "cost to reach v" means
// the time when v STARTS informing its reports (i.e., after v has processed the info
// for time[v] duration). So the answer = max(dist[i] + time[i]).
//
// This is the only interpretation consistent with example 1 giving 7.
// Let me re-verify example 2 with this model:
// dist=[0,0,0,1], time=[0,1,2,0]
// dist+time=[0,1,2,1] → max=2. ✓ (matches expected output of 2)
//
// So the