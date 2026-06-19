/*
Title: Check if Two Users Belong to the Same Friend Circle
Difficulty: Easy
Topic: Graphs

Problem Description:
You are given an undirected social graph with n users labeled from 0 to n - 1.
Each friendship is represented by a pair [u, v], meaning user u and user v know
each other directly. Friendships are mutual, so the graph is undirected.

A friend circle is a connected group of users where every user can reach every
other user through one or more friendship links.

Your task is to determine, for a single query, whether two given users source
and target belong to the same friend circle. Return true if there is a path
between them, otherwise return false.

This problem tests basic graph traversal using Breadth-First Search (BFS) or
Depth-First Search (DFS). We will build an adjacency list from the friendship
pairs and explore from source until we either reach target or finish visiting
the connected component.

Constraints:
- 1 <= n <= 10^4
- 0 <= friendships.length <= 2 * 10^4
- friendships[i].length == 2
- 0 <= u, v < n
- u != v
- There are no duplicate friendship pairs.
- 0 <= source, target < n

Example 1:
Input: n = 5, friendships = [[0,1],[1,2],[3,4]], source = 0, target = 2
Output: true
Explanation: User 0 can reach user 2 through 1, so they are in the same friend circle.

Example 2:
Input: n = 6, friendships = [[0,1],[2,3],[3,4]], source = 1, target = 5
Output: false
Explanation: User 5 has no connection to user 1, so they are not in the same connected component.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building the adjacency list takes O(friendships.Length)
    - BFS traversal takes O(n + friendships.Length) in the worst case
    - Overall: O(n + friendships.Length)

    Space Complexity:
    - Adjacency list uses O(n + friendships.Length)
    - Visited array uses O(n)
    - Queue uses O(n) in the worst case
    - Overall: O(n + friendships.Length)
    */
    public bool AreInSameFriendCircle(int n, int[][] friendships, int source, int target)
    {
        // If source and target are the same user, then they are trivially in the same friend circle.
        // Why this matters:
        // A user is always reachable from themselves using a path of length 0.
        // This is the fastest possible answer and avoids unnecessary graph construction.
        if (source == target)
        {
            return true;
        }

        // We will represent the graph using an adjacency list.
        // What is an adjacency list?
        // For each user, we store a list of all directly connected friends.
        //
        // Why choose an adjacency list?
        // - It is memory-efficient for sparse graphs.
        // - The constraints allow up to 20,000 friendships, which is much smaller than n*n.
        // - It makes neighbor traversal easy during BFS.
        //
        // We create an array of lists where graph[i] contains all neighbors of user i.
        List<int>[] graph = new List<int>[n];

        // Initialize each user's neighbor list.
        // This ensures every user has a valid list, even if they have no friendships.
        for (int i = 0; i < n; i++)
        {
            graph[i] = new List<int>();
        }

        // Build the undirected graph from the friendship pairs.
        // Each friendship [u, v] means:
        // - u is connected to v
        // - v is connected to u
        //
        // Why add both directions?
        // Because friendships are mutual, and the graph is undirected.
        foreach (int[] friendship in friendships)
        {
            int u = friendship[0];
            int v = friendship[1];

            graph[u].Add(v);
            graph[v].Add(u);
        }

        // We need to avoid visiting the same user multiple times.
        // Why?
        // - Prevents infinite loops in graphs with cycles
        // - Avoids repeated work
        // - Ensures BFS remains efficient
        bool[] visited = new bool[n];

        // BFS uses a queue.
        // Why BFS?
        // - It explores the graph level by level
        // - It is simple and reliable for checking reachability
        // - For this problem, we only care whether target is reachable, not the exact path length
        Queue<int> queue = new Queue<int>();

        // Start BFS from the source user.
        // Mark source as visited immediately when enqueuing.
        // Why mark now instead of later?
        // This prevents the same node from being added to the queue multiple times.
        visited[source] = true;
        queue.Enqueue(source);

        // Continue exploring while there are users left to process.
        while (queue.Count > 0)
        {
            // Remove the next user from the front of the queue.
            // This is the current user whose friends we will inspect.
            int currentUser = queue.Dequeue();

            // Look at every direct friend of the current user.
            foreach (int neighbor in graph[currentUser])
            {
                // If this neighbor is the target, we found a path from source to target.
                // That means both users belong to the same connected component / friend circle.
                if (neighbor == target)
                {
                    return true;
                }

                // If this neighbor has not been visited yet, we should explore it later.
                if (!visited[neighbor])
                {
                    // Mark as visited first to avoid duplicate queue entries.
                    visited[neighbor] = true;

                    // Add the neighbor to the queue so BFS can continue from there.
                    queue.Enqueue(neighbor);
                }
            }
        }

        // If BFS finishes without finding the target, then target is not reachable from source.
        // Therefore, source and target are not in the same friend circle.
        return false;
    }
}

// Demo code:
// We will create the sample inputs from the problem statement,
// call the solution method, and print the results.

// Example 1:
// n = 5
// friendships = [[0,1],[1,2],[3,4]]
// source = 0
// target = 2
// Expected output: true
var solution = new Solution();

int n1 = 5;
int[][] friendships1 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 3, 4 }
};
int source1 = 0;
int target1 = 2;

bool result1 = solution.AreInSameFriendCircle(n1, friendships1, source1, target1);
Console.WriteLine(result1);

// Example 2:
// n = 6
// friendships = [[0,1],[2,3],[3,4]]
// source = 1
// target = 5
// Expected output: false
int n2 = 6;
int[][] friendships2 =
{
    new[] { 0, 1 },
    new[] { 2, 3 },
    new[] { 3, 4 }
};
int source2 = 1;
int target2 = 5;

bool result2 = solution.AreInSameFriendCircle(n2, friendships2, source2, target2);
Console.WriteLine(result2);