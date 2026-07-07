/*
Title: Minimum Approval Steps in a Delegation Graph

Problem Description:
In a company workflow system, each employee may delegate approval authority to several other employees.
You are given a directed graph with n employees labeled from 0 to n - 1, where each directed edge [u, v]
means employee u can forward an approval request directly to employee v in one step.

You are also given:
- a list of employees who are initially authorized to start the request
- a list of target employees whose signatures are considered acceptable final approvals

Your task is to compute the minimum number of delegation steps needed for any starting employee
to reach any target employee. If no target can be reached from any starting employee, return -1.

This is a multi-source to multi-target shortest path problem on an unweighted directed graph.
Because every edge has the same cost (1 step), Breadth-First Search (BFS) is the correct tool.

Key idea:
- Start BFS from all start nodes at once
- Expand level by level
- The first time we reach any target node, that distance is guaranteed to be the minimum number of steps

Why this works:
- BFS explores nodes in increasing order of distance
- Multi-source BFS is equivalent to adding a "super source" connected to every start node with 0-cost edges
- Therefore, the first target encountered gives the shortest path from any start to any target
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building the adjacency list takes O(n + m), where m = edges.Length
    - BFS visits each node at most once and each edge at most once, so O(n + m)
    - Total: O(n + m)

    Space Complexity:
    - Adjacency list: O(n + m)
    - Visited/dist arrays and queue: O(n)
    - Target lookup set: O(t), where t = targets.Length
    - Total: O(n + m)
    */
    public int MinimumApprovalSteps(int n, int[][] edges, int[] starts, int[] targets)
    {
        // Step 1:
        // Create an adjacency list for the directed graph.
        //
        // Why we need this:
        // We want to quickly find all employees that a given employee can delegate to.
        // An adjacency list is the standard efficient representation for sparse graphs,
        // especially when n can be as large as 200,000 and edges can be as large as 300,000.
        //
        // graph[u] will contain all nodes v such that there is a directed edge u -> v.
        var graph = new List<int>[n];
        for (int i = 0; i < n; i++)
        {
            graph[i] = new List<int>();
        }

        // Fill the adjacency list using the input edges.
        foreach (var edge in edges)
        {
            int u = edge[0];
            int v = edge[1];
            graph[u].Add(v);
        }

        // Step 2:
        // Put all target employees into a HashSet.
        //
        // Why we need this:
        // During BFS, we will repeatedly ask:
        // "Is the current node one of the acceptable final approval employees?"
        //
        // A HashSet allows O(1) average-time membership checks,
        // which is much faster than scanning the targets array every time.
        var targetSet = new HashSet<int>(targets);

        // Step 3:
        // Handle the special case where a start node is already a target node.
        //
        // Why this matters:
        // If an employee is both in starts and targets, then we need 0 delegation steps,
        // because we are already at an acceptable final approver.
        foreach (int start in starts)
        {
            if (targetSet.Contains(start))
            {
                return 0;
            }
        }

        // Step 4:
        // Prepare BFS data structures.
        //
        // visited[node] tells us whether we have already discovered this node.
        // This prevents revisiting nodes and avoids infinite loops in graphs with cycles.
        //
        // distance[node] stores the minimum number of edges from any start node to this node.
        // Because BFS explores in increasing distance order, the first assigned distance is optimal.
        //
        // queue stores the frontier of BFS.
        var visited = new bool[n];
        var distance = new int[n];
        var queue = new Queue<int>();

        // Step 5:
        // Initialize BFS with ALL start nodes.
        //
        // Why multi-source BFS:
        // We are not looking for the shortest path from one fixed source.
        // We want the shortest path from ANY start to ANY target.
        //
        // The correct way to do this in an unweighted graph is to enqueue all starts initially
        // with distance 0. BFS will then naturally expand outward from all of them together.
        foreach (int start in starts)
        {
            if (!visited[start])
            {
                visited[start] = true;
                distance[start] = 0;
                queue.Enqueue(start);
            }
        }

        // Step 6:
        // Standard BFS loop.
        //
        // At each step:
        // - Remove the next node from the queue
        // - Explore all outgoing neighbors
        // - If a neighbor has not been visited, mark it visited, assign its distance,
        //   and add it to the queue
        //
        // Because BFS processes nodes level by level, the first time we reach a target,
        // that distance is the minimum possible answer.
        while (queue.Count > 0)
        {
            int current = queue.Dequeue();

            // Explore every employee that current can directly delegate to.
            foreach (int next in graph[current])
            {
                // If we have already visited this node, skip it.
                //
                // Why:
                // The first time BFS reaches a node is always via the shortest path.
                // Any later visit would be equal or longer, so it is unnecessary.
                if (visited[next])
                {
                    continue;
                }

                // Mark the node as discovered immediately when enqueuing.
                //
                // Why mark now instead of later:
                // This prevents the same node from being enqueued multiple times
                // from different parents in the same BFS layer.
                visited[next] = true;

                // The neighbor is exactly one edge farther than the current node.
                distance[next] = distance[current] + 1;

                // Important optimization and correctness point:
                // As soon as we discover a target node, we can return its distance.
                //
                // Why this is correct:
                // BFS explores nodes in non-decreasing order of distance.
                // Therefore, the first target discovered is guaranteed to have
                // the minimum number of delegation steps among all possible start-target paths.
                if (targetSet.Contains(next))
                {
                    return distance[next];
                }

                // If it is not a target, continue BFS from it later.
                queue.Enqueue(next);
            }
        }

        // Step 7:
        // If BFS finishes without reaching any target, then no valid path exists.
        return -1;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// n = 7
// edges = [[0,1],[1,3],[2,3],[3,4],[4,6],[2,5]]
// starts = [0,2]
// targets = [6,5]
//
// Shortest valid path:
// 2 -> 5
// Number of steps = 1
int n1 = 7;
int[][] edges1 =
{
    new[] { 0, 1 },
    new[] { 1, 3 },
    new[] { 2, 3 },
    new[] { 3, 4 },
    new[] { 4, 6 },
    new[] { 2, 5 }
};
int[] starts1 = { 0, 2 };
int[] targets1 = { 6, 5 };

int result1 = solution.MinimumApprovalSteps(n1, edges1, starts1, targets1);
Console.WriteLine(result1); // Expected: 1

// Example 2:
// n = 6
// edges = [[0,1],[1,2],[2,3],[4,5]]
// starts = [0,4]
// targets = [3]
//
// Shortest valid path:
// 0 -> 1 -> 2 -> 3
// Number of steps = 3
int n2 = 6;
int[][] edges2 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 2, 3 },
    new[] { 4, 5 }
};
int[] starts2 = { 0, 4 };
int[] targets2 = { 3 };

int result2 = solution.MinimumApprovalSteps(n2, edges2, starts2, targets2);
Console.WriteLine(result2); // Expected: 3

// Additional demo:
// No target reachable
int n3 = 5;
int[][] edges3 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 3, 4 }
};
int[] starts3 = { 0 };
int[] targets3 = { 4 };

int result3 = solution.MinimumApprovalSteps(n3, edges3, starts3, targets3);
Console.WriteLine(result3); // Expected: -1