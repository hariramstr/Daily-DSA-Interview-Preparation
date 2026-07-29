/*
Title: Minimum Channel Switches to Broadcast a Live Event
Difficulty: Medium
Topic: Graphs

Problem Description:
A media company needs to send a live video stream from studio 0 to studio n - 1 through a network of relay stations.
Each directed connection between stations is labeled with a channel ID, representing the transmission channel used on that link.
Moving along an edge is always possible, but every time the stream uses a link whose channel ID is different from the previous
link's channel ID, engineers must perform a costly channel switch.

Your task is to compute the minimum number of channel switches required to send the stream from node 0 to node n - 1.
The first chosen edge does not count as a switch, because no channel is active before transmission begins.
If it is impossible to reach node n - 1, return -1.

Formally, you are given an integer n and a list edges where each element is [u, v, c], meaning there is a directed edge from u to v
using channel c. A path may revisit nodes, but you want the minimum possible number of channel changes along any valid path from 0
to n - 1.

This is a graph shortest-path problem where the state must include both the current node and the channel used to arrive there.
A solution that only tracks nodes may miss the optimal answer.

Constraints:
- 2 <= n <= 100000
- 1 <= edges.length <= 200000
- 0 <= u, v < n
- u != v
- 1 <= c <= 1000000000
- Multiple edges between the same pair of nodes may exist with different channel IDs.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    private readonly struct Edge
    {
        public readonly int To;
        public readonly int Channel;

        public Edge(int to, int channel)
        {
            To = to;
            Channel = channel;
        }
    }

    private readonly struct State : IEquatable<State>
    {
        public readonly int Node;
        public readonly int Channel;

        public State(int node, int channel)
        {
            Node = node;
            Channel = channel;
        }

        public bool Equals(State other) => Node == other.Node && Channel == other.Channel;
        public override bool Equals(object? obj) => obj is State other && Equals(other);
        public override int GetHashCode() => HashCode.Combine(Node, Channel);
    }

    private readonly struct HeapItem : IComparable<HeapItem>
    {
        public readonly int Cost;
        public readonly int Node;
        public readonly int Channel;

        public HeapItem(int cost, int node, int channel)
        {
            Cost = cost;
            Node = node;
            Channel = channel;
        }

        public int CompareTo(HeapItem other)
        {
            int cmp = Cost.CompareTo(other.Cost);
            if (cmp != 0) return cmp;

            cmp = Node.CompareTo(other.Node);
            if (cmp != 0) return cmp;

            return Channel.CompareTo(other.Channel);
        }
    }

    /*
    Time Complexity:
    O((V_state + E_state) log V_state) in the Dijkstra-style view.

    More concretely:
    - We create graph adjacency in O(m), where m = edges.Length.
    - Each reachable state is (node, lastChannel).
    - From each processed state, we scan outgoing edges of that node.
    - In the worst case, the number of states can be proportional to the number of distinct (node, channel) combinations
      that actually appear while traversing the graph, which is at most O(m).
    - Therefore, the practical upper bound is O(m log m + transitions log m), and transitions are bounded by the total
      outgoing-edge scans from processed states.

    Space Complexity:
    O(m + V_state)
    - Adjacency list stores all edges: O(m)
    - Distance dictionary stores best known cost for reachable (node, channel) states: O(V_state), at most O(m)
    - Priority queue stores pending states: O(V_state)
    */
    public int MinimumChannelSwitches(int n, int[][] edges)
    {
        // Special case:
        // If the start node is already the destination node, no travel is needed,
        // therefore no channel switches are needed.
        // The given constraints say n >= 2, so this is not required for correctness here,
        // but keeping it makes the method more complete and easier to understand.
        if (n == 1)
        {
            return 0;
        }

        // Step 1: Build the directed graph as an adjacency list.
        //
        // Why adjacency list?
        // - The graph can be large: up to 100,000 nodes and 200,000 edges.
        // - For shortest path style algorithms, we frequently need "all outgoing edges from a node".
        // - Adjacency lists are memory-efficient and fast for that purpose.
        //
        // graph[u] will contain every directed edge leaving node u.
        var graph = new List<Edge>[n];
        for (int i = 0; i < n; i++)
        {
            graph[i] = new List<Edge>();
        }

        foreach (var e in edges)
        {
            int u = e[0];
            int v = e[1];
            int c = e[2];
            graph[u].Add(new Edge(v, c));
        }

        // Step 2: We will run Dijkstra on an expanded state space.
        //
        // Important idea:
        // A normal shortest path that only remembers "which node am I at?" is NOT enough.
        // Why?
        // Because the future cost depends on the channel used to arrive at the current node.
        //
        // Example:
        // If we are at the same node X:
        // - arriving with channel 7 may let us continue on channel 7 with no extra switch
        // - arriving with channel 2 may force a switch on the next edge
        //
        // Therefore, the true state is:
        //   (currentNode, lastUsedChannel)
        //
        // dist[(node, channel)] = minimum number of switches needed to reach this exact state.
        var dist = new Dictionary<State, int>();

        // Step 3: Use a priority queue for Dijkstra.
        //
        // We always want to process the currently known state with the smallest number of switches.
        // .NET's PriorityQueue is a max/min structure based on priority, but for educational clarity
        // and explicit ordering, we use SortedSet-like behavior through PriorityQueue<TElement, TPriority>.
        //
        // Here, the element and priority are both HeapItem-compatible values.
        // We store:
        // - cost so far
        // - node
        // - last channel
        //
        // Since the first edge does NOT count as a switch, we handle the start carefully:
        // Instead of inventing a fake "no channel" state and special-casing every transition,
        // we initialize the search by taking every outgoing edge from node 0:
        //   reaching (neighbor, edgeChannel) with cost 0
        //
        // This exactly models:
        // - first chosen edge activates a channel
        // - no switch is counted yet
        var pq = new PriorityQueue<HeapItem, HeapItem>();

        foreach (var edge in graph[0])
        {
            var startState = new State(edge.To, edge.Channel);

            // If multiple edges from node 0 lead to the same (node, channel),
            // we only need to keep the best cost, which is 0 anyway.
            if (!dist.TryGetValue(startState, out int existing) || 0 < existing)
            {
                dist[startState] = 0;
                var item = new HeapItem(0, edge.To, edge.Channel);
                pq.Enqueue(item, item);
            }
        }

        // Step 4: If node 0 has no outgoing edges, then no path can start.
        // In that case, reaching node n - 1 is impossible.
        if (pq.Count == 0)
        {
            return -1;
        }

        // Step 5: Run Dijkstra over the expanded state graph.
        //
        // Each pop gives us the currently cheapest known state.
        // If this state is stale (meaning we have already found a better cost for it),
        // we skip it.
        //
        // Transition rule:
        // From state (u, lastChannel) and outgoing edge (u -> v, edgeChannel):
        // - additional cost = 0 if edgeChannel == lastChannel
        // - additional cost = 1 otherwise
        //
        // This is exactly the number of channel switches.
        while (pq.Count > 0)
        {
            var current = pq.Dequeue();
            var currentState = new State(current.Node, current.Channel);

            // This check removes stale entries.
            // Why can stale entries exist?
            // Because we may push a state into the priority queue, and later discover
            // an even better way to reach the same state before the old one is popped.
            //
            // Dijkstra implementations commonly use this pattern.
            if (!dist.TryGetValue(currentState, out int bestKnownCost) || bestKnownCost != current.Cost)
            {
                continue;
            }

            // Early exit:
            // Since Dijkstra processes states in nondecreasing cost order,
            // the first time we pop any state whose node is the destination,
            // that cost is the global minimum number of switches needed.
            //
            // We do NOT care what the last channel is at the destination.
            // Any channel is acceptable, so the first popped destination state is optimal.
            if (current.Node == n - 1)
            {
                return current.Cost;
            }

            // Explore all outgoing edges from the current node.
            foreach (var nextEdge in graph[current.Node])
            {
                // If the next edge uses the same channel, no switch is needed.
                // Otherwise, exactly one switch is needed.
                int extraCost = nextEdge.Channel == current.Channel ? 0 : 1;
                int newCost = current.Cost + extraCost;

                var nextState = new State(nextEdge.To, nextEdge.Channel);

                // Relaxation step:
                // If this route gives a better switch count for the next state,
                // record it and push it into the priority queue.
                if (!dist.TryGetValue(nextState, out int oldCost) || newCost < oldCost)
                {
                    dist[nextState] = newCost;
                    var nextItem = new HeapItem(newCost, nextEdge.To, nextEdge.Channel);
                    pq.Enqueue(nextItem, nextItem);
                }
            }
        }

        // If the queue becomes empty, every reachable state has been processed,
        // and none reached the destination.
        return -1;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int n1 = 5;
int[][] edges1 =
{
    new[] { 0, 1, 7 },
    new[] { 1, 2, 7 },
    new[] { 2, 4, 7 },
    new[] { 0, 3, 2 },
    new[] { 3, 4, 2 },
    new[] { 1, 3, 5 }
};
int result1 = solution.MinimumChannelSwitches(n1, edges1);
Console.WriteLine(result1); // Expected: 0

// Example 2
int n2 = 6;
int[][] edges2 =
{
    new[] { 0, 1, 1 },
    new[] { 1, 2, 2 },
    new[] { 2, 5, 2 },
    new[] { 0, 3, 3 },
    new[] { 3, 4, 4 },
    new[] { 4, 5, 5 },
    new[] { 1, 4, 1 }
};
int result2 = solution.MinimumChannelSwitches(n2, edges2);
Console.WriteLine(result2); // Expected: 1

// Additional unreachable example
int n3 = 4;
int[][] edges3 =
{
    new[] { 0, 1, 10 },
    new[] { 1, 2, 10 }
};
int result3 = solution.MinimumChannelSwitches(n3, edges3);
Console.WriteLine(result3); // Expected: -1