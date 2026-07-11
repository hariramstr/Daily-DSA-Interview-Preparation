/*
Title: Last Safe Merge Before Road Closures

Problem Description:
A delivery company operates in a city with one-way roads. There are n intersections labeled from 0 to n - 1 and m directed roads.
Every road is described by three integers [u, v, closeTime], meaning a driver can travel from intersection u to intersection v
only if they start using that road at a time strictly less than closeTime. Traveling along any road always takes exactly 1 minute.

Two drivers start at the same time 0 from different intersections s1 and s2. They want to meet as early as possible at some
intersection x such that both drivers can reach x while respecting all road closing times. If multiple intersections can be used
for the earliest possible meeting time, return the smallest intersection index. If no meeting is possible, return -1.

Important details:
- A driver may wait at any intersection for any amount of time.
- A road with closeTime = t can be entered only at times 0, 1, ..., t - 1.
- Since each road takes 1 minute, arriving later can make some future roads unusable.
- The meeting happens when both drivers have arrived at the same intersection; one driver may arrive earlier and wait.

Key observation:
Because waiting is allowed but never helps with a "strictly less than closeTime" constraint, the best strategy for reaching any node
is always to arrive as early as possible. If you can reach a node at time d, then from that node you can use an outgoing road with
closeTime c iff d < c. Waiting only increases time, so it cannot make an unusable road become usable.

Therefore:
1. Compute the earliest arrival time from s1 to every node.
2. Compute the earliest arrival time from s2 to every node.
3. For every node reachable from both starts, the earliest meeting time at that node is:
      max(distFromS1[node], distFromS2[node])
   because the earlier driver can wait.
4. Choose the node with the smallest meeting time; break ties by smaller node index.

This is a shortest-path problem on a directed graph with a special edge usability rule:
from node u at time t, edge (u -> v, closeTime) can be used only if t < closeTime, and then arrival time becomes t + 1.

Since every usable move always costs exactly 1 minute, a standard BFS-style relaxation works:
- Distances are processed in nondecreasing order of time.
- The first time we finalize a node is its earliest possible arrival time.
- When exploring edges from a node reached at time t, we only traverse edges whose closeTime > t.

Note:
The second example in the prompt's explanation appears inconsistent. Under the stated rules, node 3 is a valid meeting point:
- Driver 1: 0 -> 2 at time 0, arrive 1; 2 -> 3 at time 1 (< 2), arrive 2
- Driver 2: 1 -> 3 at time 0 (< 1), arrive 1; wait until time 2
So they can meet at node 3 at time 2.
The algorithm below follows the formal problem statement exactly.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building the adjacency list takes O(m).
    - Each BFS-like traversal visits each node at most once and inspects each outgoing road at most once, so O(n + m).
    - We run the traversal twice, once from s1 and once from s2, so total O(n + m).
    - Final scan over all intersections takes O(n).
    Overall: O(n + m)

    Space Complexity:
    - Adjacency list stores all roads: O(n + m)
    - Distance arrays: O(n)
    - Queue for traversal: O(n)
    Overall: O(n + m)
    */
    public int LastSafeMergeBeforeRoadClosures(int n, int[][] roads, int s1, int s2)
    {
        // Step 1:
        // Build an adjacency list for the directed graph.
        //
        // Why?
        // We need fast access to all outgoing roads from any intersection while exploring reachable nodes.
        // An adjacency list is the standard efficient structure for sparse graphs and works well here because:
        // - n can be as large as 100,000
        // - m can be as large as 200,000
        //
        // Each entry graph[u] will contain all roads that start at intersection u.
        var graph = new List<Edge>[n];
        for (int i = 0; i < n; i++)
        {
            graph[i] = new List<Edge>();
        }

        foreach (var road in roads)
        {
            int u = road[0];
            int v = road[1];
            int closeTime = road[2];
            graph[u].Add(new Edge(v, closeTime));
        }

        // Step 2:
        // Compute earliest arrival times from each starting intersection.
        //
        // Why do we compute earliest arrival times?
        // Because if a driver can reach a node earlier, that is always at least as good as reaching it later:
        // - the driver can always wait
        // - but arriving later may cause some future roads to be closed already
        //
        // So the earliest arrival time fully captures the best possible state at each node.
        int[] dist1 = ComputeEarliestArrivalTimes(n, graph, s1);
        int[] dist2 = ComputeEarliestArrivalTimes(n, graph, s2);

        // Step 3:
        // Evaluate every intersection as a possible meeting point.
        //
        // If both drivers can reach node x:
        // - driver 1 arrives at dist1[x]
        // - driver 2 arrives at dist2[x]
        // Since waiting is allowed, they can both be present together at time:
        //      max(dist1[x], dist2[x])
        //
        // We want the earliest such meeting time.
        // If multiple nodes have the same earliest meeting time, choose the smallest node index.
        int answerNode = -1;
        int bestMeetingTime = int.MaxValue;

        for (int node = 0; node < n; node++)
        {
            // If either distance is still "infinity", that driver cannot reach this node.
            if (dist1[node] == int.MaxValue || dist2[node] == int.MaxValue)
            {
                continue;
            }

            int meetingTime = Math.Max(dist1[node], dist2[node]);

            if (meetingTime < bestMeetingTime)
            {
                bestMeetingTime = meetingTime;
                answerNode = node;
            }
            else if (meetingTime == bestMeetingTime && node < answerNode)
            {
                answerNode = node;
            }
        }

        return answerNode;
    }

    private int[] ComputeEarliestArrivalTimes(int n, List<Edge>[] graph, int start)
    {
        // Step 1:
        // Create and initialize the distance array.
        //
        // dist[i] = earliest known time we can arrive at intersection i.
        // We use int.MaxValue to mean "not reachable yet".
        int[] dist = new int[n];
        Array.Fill(dist, int.MaxValue);

        // The starting intersection is reached at time 0.
        dist[start] = 0;

        // Step 2:
        // Use a queue for BFS-style processing.
        //
        // Why is a simple queue enough here?
        // Every road traversal always costs exactly 1 minute.
        // That means when we first discover a node, it is discovered at the smallest possible time,
        // exactly like ordinary BFS on an unweighted graph.
        //
        // The only extra rule is that an edge can be used only if currentTime < closeTime.
        // This rule only filters which edges are available; it does not change the cost of a used edge.
        var queue = new Queue<int>();
        queue.Enqueue(start);

        // Step 3:
        // Process nodes in order of increasing arrival time.
        while (queue.Count > 0)
        {
            int u = queue.Dequeue();

            // currentTime is the earliest time we can be at node u.
            int currentTime = dist[u];

            // Step 4:
            // Try every outgoing road from u.
            foreach (var edge in graph[u])
            {
                // We may use this road only if we START traversing it before it closes.
                // Since we are currently at u at time currentTime, the condition is:
                //      currentTime < edge.CloseTime
                //
                // If this is false, then even the earliest possible arrival at u is too late,
                // and waiting would only make it worse, so this road is unusable from u.
                if (currentTime >= edge.CloseTime)
                {
                    continue;
                }

                // If the road is usable, arrival at the destination is exactly 1 minute later.
                int nextTime = currentTime + 1;
                int v = edge.To;

                // Step 5:
                // Relaxation step:
                // If we found an earlier way to reach v, record it and push v into the queue.
                //
                // Because all edges have equal travel time 1 and we process in BFS order,
                // the first time we assign dist[v] is already the earliest possible arrival time.
                // Still, writing the condition this way makes the logic explicit and safe.
                if (nextTime < dist[v])
                {
                    dist[v] = nextTime;
                    queue.Enqueue(v);
                }
            }
        }

        return dist;
    }

    private readonly record struct Edge(int To, int CloseTime);
}

// Demo code

var solution = new Solution();

// Example 1
int n1 = 5;
int[][] roads1 =
{
    new[] { 0, 2, 3 },
    new[] { 1, 2, 2 },
    new[] { 2, 3, 5 },
    new[] { 1, 4, 10 },
    new[] { 4, 3, 10 }
};
int s1_1 = 0;
int s2_1 = 1;
int result1 = solution.LastSafeMergeBeforeRoadClosures(n1, roads1, s1_1, s2_1);
Console.WriteLine(result1); // Expected: 2

// Example 2
// According to the formal rules in the statement, the correct answer is 3.
int n2 = 4;
int[][] roads2 =
{
    new[] { 0, 2, 1 },
    new[] { 2, 3, 2 },
    new[] { 1, 3, 1 }
};
int s1_2 = 0;
int s2_2 = 1;
int result2 = solution.LastSafeMergeBeforeRoadClosures(n2, roads2, s1_2, s2_2);
Console.WriteLine(result2); // Under the stated rules: 3

// Additional demo: no meeting possible
int n3 = 4;
int[][] roads3 =
{
    new[] { 0, 2, 1 },
    new[] { 1, 3, 1 }
};
int s1_3 = 0;
int s2_3 = 1;
int result3 = solution.LastSafeMergeBeforeRoadClosures(n3, roads3, s1_3, s2_3);
Console.WriteLine(result3); // Expected: -1