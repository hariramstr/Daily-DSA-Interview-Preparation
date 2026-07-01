/*
Title: Nearest Exit Gate in an Office Floor

Problem Description:
You are given an office floor represented as an undirected graph. Each room is numbered from 0 to n - 1, and hallways connect pairs of rooms. Some rooms contain emergency exit gates. Starting from a given room, you must determine the minimum number of hallways an employee needs to walk through to reach any exit gate.

Return the smallest number of edges from the start room to any exit room. If the starting room is itself an exit room, return 0. If no exit gate is reachable, return -1.

The graph may be disconnected, and there may be multiple possible exit rooms. All hallways can be used in both directions, and every hallway has the same cost of 1 step. This makes the problem a shortest-path search in an unweighted graph.

Your task is to write a function that takes the number of rooms n, a list of hallways edges where each edge is [u, v], a list exits containing all rooms with exit gates, and an integer start representing the employee's current room.

Constraints:
- 1 <= n <= 10^5
- 0 <= edges.length <= 2 * 10^5
- edges[i].length == 2
- 0 <= u, v < n
- u != v
- 1 <= exits.length <= n
- 0 <= exits[i] < n
- 0 <= start < n
- There are no duplicate hallways.

Example 1:
Input: n = 7, edges = [[0,1],[1,2],[2,3],[1,4],[4,5],[5,6]], exits = [3,6], start = 0
Output: 3
Explanation: The shortest path from room 0 to an exit is 0 -> 1 -> 2 -> 3, which uses 3 hallways. The other exit at room 6 takes 4 hallways.

Example 2:
Input: n = 5, edges = [[0,1],[1,2],[3,4]], exits = [4], start = 0
Output: -1
Explanation: Room 4 is in a different connected component, so no exit is reachable from room 0.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building the adjacency list takes O(n + m), where m is the number of edges.
    - Breadth-First Search (BFS) visits each room at most once and checks each hallway at most twice
      (once from each endpoint in an undirected graph), so BFS also takes O(n + m).
    - Overall time complexity: O(n + m)

    Space Complexity:
    - The adjacency list stores all rooms and hallways: O(n + m)
    - The visited array / distance tracking and queue use O(n)
    - The exit lookup structure uses O(n) in the worst case
    - Overall space complexity: O(n + m)
    */
    public int NearestExit(int n, int[][] edges, int[] exits, int start)
    {
        // Step 1:
        // Create a fast lookup structure to quickly answer the question:
        // "Is this room an exit room?"
        //
        // Why this is necessary:
        // During BFS, we will visit many rooms. For each room, we want to know
        // immediately whether it is an exit. A HashSet gives average O(1) lookup time.
        //
        // Data structure choice:
        // HashSet<int> is ideal here because:
        // - We only care whether a room is in the exits list
        // - We do not need ordering
        // - Membership checks are very fast
        var exitSet = new HashSet<int>(exits);

        // Step 2:
        // Handle the simplest possible case first:
        // If the starting room already contains an exit gate, then the employee
        // does not need to walk through any hallway.
        //
        // Why this is necessary:
        // This is explicitly required by the problem statement.
        if (exitSet.Contains(start))
        {
            return 0;
        }

        // Step 3:
        // Build the graph as an adjacency list.
        //
        // Why this is necessary:
        // The input gives hallways as edge pairs [u, v].
        // BFS needs to quickly find all neighboring rooms connected to the current room.
        //
        // Data structure choice:
        // List<int>[] adjacency list:
        // - Efficient for sparse graphs, which is common in graph problems
        // - Much better than an adjacency matrix for large n up to 100,000
        // - Easy to iterate through neighbors
        var graph = new List<int>[n];

        // Initialize each room's neighbor list.
        for (int i = 0; i < n; i++)
        {
            graph[i] = new List<int>();
        }

        // Since the graph is undirected, each hallway [u, v] means:
        // - u connects to v
        // - v connects to u
        //
        // We add both directions to the adjacency list.
        foreach (var edge in edges)
        {
            int u = edge[0];
            int v = edge[1];

            graph[u].Add(v);
            graph[v].Add(u);
        }

        // Step 4:
        // Prepare for Breadth-First Search (BFS).
        //
        // Why BFS?
        // In an unweighted graph, BFS guarantees that the first time we reach a room,
        // we have found the shortest path to that room in terms of number of edges.
        //
        // Since every hallway costs exactly 1 step, BFS is the correct shortest-path algorithm.
        var queue = new Queue<int>();

        // visited[i] tells us whether room i has already been added to the BFS process.
        //
        // Why this is necessary:
        // Without visited tracking, we could revisit the same room many times,
        // causing unnecessary work and possibly infinite loops in cyclic graphs.
        var visited = new bool[n];

        // distance[i] stores the minimum number of hallways needed to reach room i from start.
        //
        // Why this is useful:
        // When we reach an exit room, we can immediately return its distance.
        var distance = new int[n];

        // Step 5:
        // Start BFS from the starting room.
        queue.Enqueue(start);
        visited[start] = true;
        distance[start] = 0;

        // Step 6:
        // Process rooms level by level using BFS.
        //
        // Important BFS idea:
        // Rooms reached in fewer steps are processed before rooms reached in more steps.
        // Therefore, the first exit room we encounter will be the nearest reachable exit.
        while (queue.Count > 0)
        {
            // Remove the next room to process from the front of the queue.
            int currentRoom = queue.Dequeue();

            // Step 7:
            // Explore every neighboring room connected by one hallway.
            foreach (int neighbor in graph[currentRoom])
            {
                // If we have already visited this neighbor, skip it.
                //
                // Why this is necessary:
                // The first time we visit a room in BFS is always via the shortest path.
                // Visiting it again later would only produce an equal or longer path.
                if (visited[neighbor])
                {
                    continue;
                }

                // Mark the neighbor as visited the moment we enqueue it.
                //
                // Why mark here instead of later?
                // This prevents the same room from being enqueued multiple times
                // from different parents before it is processed.
                visited[neighbor] = true;

                // The neighbor is exactly one hallway farther than the current room.
                distance[neighbor] = distance[currentRoom] + 1;

                // Step 8:
                // Check whether this newly reached room is an exit.
                //
                // Why check here?
                // Because BFS explores in increasing distance order, the first exit we find
                // is guaranteed to be the closest reachable exit.
                if (exitSet.Contains(neighbor))
                {
                    return distance[neighbor];
                }

                // If it is not an exit, continue BFS from this room later.
                queue.Enqueue(neighbor);
            }
        }

        // Step 9:
        // If BFS finishes and we never found an exit, then no exit is reachable
        // from the starting room.
        return -1;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// Graph:
// 0 - 1 - 2 - 3
//     |
//     4 - 5 - 6
//
// Exits: 3 and 6
// Start: 0
//
// Shortest exit path:
// 0 -> 1 -> 2 -> 3 = 3 hallways
int n1 = 7;
int[][] edges1 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 2, 3 },
    new[] { 1, 4 },
    new[] { 4, 5 },
    new[] { 5, 6 }
};
int[] exits1 = { 3, 6 };
int start1 = 0;

int result1 = solution.NearestExit(n1, edges1, exits1, start1);
Console.WriteLine(result1); // Expected: 3

// Example 2:
// Graph has two disconnected components:
// 0 - 1 - 2     3 - 4
//
// Exit: 4
// Start: 0
//
// Room 4 is unreachable from room 0, so answer is -1.
int n2 = 5;
int[][] edges2 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 3, 4 }
};
int[] exits2 = { 4 };
int start2 = 0;

int result2 = solution.NearestExit(n2, edges2, exits2, start2);
Console.WriteLine(result2); // Expected: -1

// Additional quick check:
// Start room is already an exit, so answer should be 0.
int n3 = 3;
int[][] edges3 =
{
    new[] { 0, 1 },
    new[] { 1, 2 }
};
int[] exits3 = { 1 };
int start3 = 1;

int result3 = solution.NearestExit(n3, edges3, exits3, start3);
Console.WriteLine(result3); // Expected: 0