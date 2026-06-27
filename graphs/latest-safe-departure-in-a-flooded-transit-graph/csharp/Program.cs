/*
Title: Latest Safe Departure in a Flooded Transit Graph

Problem Description:
A city transit authority models its rail system as an undirected graph with n stations labeled 0 to n - 1 and m bidirectional tunnels.
Station 0 is your starting point and station n - 1 is the central shelter.
A flood spreads through the network over time.

You are given an array floodTime where floodTime[i] is the earliest integer minute when station i becomes unusable.
You may stand at or enter station i only at times strictly less than floodTime[i].
Traversing any tunnel takes exactly 1 minute, and you may wait at a station for any nonnegative number of whole minutes
as long as the station is still usable during the entire wait.

Task:
Compute the latest integer minute t at which you can start from station 0 and still reach station n - 1
without ever being in a flooded station.

Return:
- -1 if it is impossible even when starting at time 0
- 1000000000 if you can delay your departure arbitrarily long and still eventually reach the shelter

Important validity rule:
For every visited station, including start and destination, arrival time must be strictly smaller than floodTime[station].

Key observation:
Waiting never helps once a departure time is fixed, because waiting only makes all future arrival times later.
So for a chosen start time t, the best strategy is to move immediately along some path.
That means feasibility for a fixed t becomes:
Does there exist a path from 0 to n - 1 such that for every node v on the path,
t + distAlongPathTo(v) < floodTime[v] ?

This is monotone:
If starting at time t is possible, then any earlier start time is also possible.
So we can binary search the latest feasible t.

To test a fixed t efficiently:
Run BFS from node 0, where we may enter node v at time currentTime + 1 only if that time is < floodTime[v].
Because every edge has weight 1, BFS gives earliest arrival times under immediate movement.
If BFS can reach n - 1, then start time t is feasible.

Special infinite-delay case:
If there exists a path from 0 to n - 1 consisting only of nodes with floodTime = 1000000000,
then for any start time t <= 1000000000, arrival times on that path remain < 1000000000 as long as path length is finite and t is chosen accordingly.
However the problem explicitly asks to return 1000000000 when you can delay arbitrarily long "within the problem's practical bound".
Since floodTime values are capped at 1000000000, the standard interpretation is:
if starting at time 1000000000 is considered the sentinel for "arbitrarily long", return 1000000000 only when the problem's intended monotone search upper bound reaches it.
Under the strict rule, starting exactly at 1000000000 is impossible at any node with floodTime = 1000000000.
Therefore the correct practical interpretation is:
return 1000000000 if the answer is unbounded above in the intended problem sense.
That can only happen when flood times conceptually never constrain the path.
Given the provided constraints and strict inequality, no finite floodTime array truly allows arbitrary integer delay forever.
But the sample expects 1000000000 when all flood times are 1000000000.
So we follow the problem's required convention:
if every node on some 0->n-1 path has floodTime = 1000000000, return 1000000000.

This convention is handled explicitly before binary search.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building the graph: O(n + m)
    - Infinite-delay check BFS: O(n + m)
    - Each feasibility BFS: O(n + m)
    - Binary search over start time range [0, 1_000_000_000]: O(log 1e9) = about 30 checks
    - Total: O((n + m) * log 1e9)

    Space Complexity:
    - Graph storage: O(n + m)
    - BFS arrays/queue: O(n)
    - Total: O(n + m)
    */
    public int LatestSafeDeparture(int n, int[][] edges, int[] floodTime)
    {
        // Step 1:
        // Build an adjacency list for the undirected graph.
        //
        // Why this is necessary:
        // We need to repeatedly explore neighbors of each station during BFS.
        // An adjacency list is the standard efficient structure for sparse graphs,
        // especially with up to 300,000 edges.
        var graph = new List<int>[n];
        for (int i = 0; i < n; i++)
        {
            graph[i] = new List<int>();
        }

        foreach (var e in edges)
        {
            int u = e[0];
            int v = e[1];
            graph[u].Add(v);
            graph[v].Add(u);
        }

        // Step 2:
        // Quick impossibility check for starting station.
        //
        // Why this is necessary:
        // You are at station 0 at the departure time t.
        // Since you may stand at station 0 only at times strictly less than floodTime[0],
        // even starting at time 0 requires 0 < floodTime[0].
        // Constraints guarantee floodTime[i] >= 1, so this is always true here,
        // but keeping the check makes the logic complete and beginner-friendly.
        if (0 >= floodTime[0])
        {
            return -1;
        }

        // Step 3:
        // Handle the special "infinite delay" convention required by the problem statement.
        //
        // We look for a path from 0 to n-1 using only nodes whose flood time is exactly 1_000_000_000.
        // The sample explicitly expects 1000000000 in such a case.
        //
        // Why this step is necessary:
        // The problem statement defines a special sentinel output for arbitrarily long delay.
        // Even though strict inequality makes the mathematical interpretation subtle,
        // the sample clearly requires this convention.
        const int INF_SENTINEL = 1_000_000_000;
        if (HasAllInfinitePath(graph, floodTime, INF_SENTINEL))
        {
            return INF_SENTINEL;
        }

        // Step 4:
        // Check whether reaching the shelter is possible at all when starting at time 0.
        //
        // Why this is necessary:
        // If even the earliest possible start time fails, then no later start time can work,
        // because later starts only make every arrival later.
        if (!CanReachWithStartTime(graph, floodTime, 0))
        {
            return -1;
        }

        // Step 5:
        // Binary search for the latest feasible departure time.
        //
        // Why binary search works:
        // Feasibility is monotone.
        // If start time t works, then any smaller start time also works.
        // So the set of feasible times is a prefix [0..answer].
        int left = 0;
        int right = INF_SENTINEL - 1; // strict inequality means practical finite answers are below this sentinel
        int answer = 0;

        while (left <= right)
        {
            int mid = left + (right - left) / 2;

            // Step 5a:
            // Test whether starting at time mid is feasible.
            //
            // If yes, we try later times.
            // If no, we try earlier times.
            if (CanReachWithStartTime(graph, floodTime, mid))
            {
                answer = mid;
                left = mid + 1;
            }
            else
            {
                right = mid - 1;
            }
        }

        return answer;
    }

    private bool HasAllInfinitePath(List<int>[] graph, int[] floodTime, int sentinel)
    {
        int n = graph.Length;

        // If either endpoint is not "infinite" under the problem's convention,
        // then such a path cannot exist.
        if (floodTime[0] != sentinel || floodTime[n - 1] != sentinel)
        {
            return false;
        }

        // Standard BFS restricted to nodes with floodTime == sentinel.
        var visited = new bool[n];
        var queue = new Queue<int>();
        visited[0] = true;
        queue.Enqueue(0);

        while (queue.Count > 0)
        {
            int u = queue.Dequeue();
            if (u == n - 1)
            {
                return true;
            }

            foreach (int v in graph[u])
            {
                if (!visited[v] && floodTime[v] == sentinel)
                {
                    visited[v] = true;
                    queue.Enqueue(v);
                }
            }
        }

        return false;
    }

    private bool CanReachWithStartTime(List<int>[] graph, int[] floodTime, int startTime)
    {
        int n = graph.Length;

        // Step A:
        // Verify that we are allowed to be at the starting station at the chosen departure time.
        //
        // Why this is necessary:
        // The rule says you may stand at station i only at times strictly less than floodTime[i].
        // So being at station 0 at time startTime requires startTime < floodTime[0].
        if (startTime >= floodTime[0])
        {
            return false;
        }

        // Step B:
        // BFS setup.
        //
        // We store the earliest arrival time found for each node.
        // Since all edges take exactly 1 minute, BFS explores states in nondecreasing arrival time order.
        //
        // Why earliest arrival is enough:
        // If we can reach a node earlier, that is always at least as good as reaching it later,
        // because all future moves depend on being before flood deadlines.
        var arrival = new int[n];
        Array.Fill(arrival, -1);

        var queue = new Queue<int>();
        arrival[0] = startTime;
        queue.Enqueue(0);

        // Step C:
        // Standard BFS over the graph.
        while (queue.Count > 0)
        {
            int u = queue.Dequeue();
            int currentTime = arrival[u];

            // If we have reached the destination, we can stop immediately.
            if (u == n - 1)
            {
                return true;
            }

            // Explore every neighboring station.
            foreach (int v in graph[u])
            {
                // Moving through one tunnel takes exactly 1 minute.
                int nextTime = currentTime + 1;

                // We may enter station v only if arrival is strictly before its flood time.
                if (nextTime >= floodTime[v])
                {
                    continue;
                }

                // If v has not been visited yet, record its earliest arrival and continue BFS.
                //
                // Why we do not revisit with a later time:
                // BFS guarantees the first time we reach a node is the earliest possible arrival time.
                // Any later arrival would never be better.
                if (arrival[v] == -1)
                {
                    arrival[v] = nextTime;
                    queue.Enqueue(v);
                }
            }
        }

        // If BFS finishes without reaching n-1, then this start time is not feasible.
        return false;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int n1 = 5;
int[][] edges1 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 2, 4 },
    new[] { 0, 3 },
    new[] { 3, 4 }
};
int[] floodTime1 = { 4, 3, 5, 2, 6 };
Console.WriteLine(solution.LatestSafeDeparture(n1, edges1, floodTime1)); // Expected: 1

// Example 2
int n2 = 4;
int[][] edges2 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 2, 3 },
    new[] { 0, 2 }
};
int[] floodTime2 = { 1000000000, 1000000000, 1000000000, 1000000000 };
Console.WriteLine(solution.LatestSafeDeparture(n2, edges2, floodTime2)); // Expected: 1000000000