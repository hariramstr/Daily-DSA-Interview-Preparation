/*
Title: Detect Circular Package Dependencies

Problem Description:
You are given a set of software packages and a list of dependency relationships between them.
Each dependency [a, b] means package a requires package b to be installed first.

A deployment is considered invalid if there exists any cycle in the dependency graph,
because at least one package would indirectly depend on itself.

Write a function that determines whether the dependency configuration is valid.
Return true if all packages can be installed in some order, and false if there is at least
one circular dependency.

The packages are labeled from 0 to n - 1. Not every package must appear in the dependency list.
A package with no incoming or outgoing edges is still considered part of the system.

This problem models a directed graph. You need to detect whether the graph contains a cycle.
A valid installation order exists if and only if the graph is acyclic.

Constraints:
- 1 <= n <= 100000
- 0 <= dependencies.length <= 200000
- dependencies[i].length == 2
- 0 <= a, b < n
- a != b
- There may be duplicate dependency pairs

Examples:
1)
Input: n = 4, dependencies = [[1,0],[2,1],[3,2]]
Output: true
Explanation:
A valid order is [0,1,2,3]. There is no cycle.

2)
Input: n = 4, dependencies = [[1,0],[2,1],[0,2],[3,1]]
Output: false
Explanation:
0 depends on 2, 2 depends on 1, and 1 depends on 0 through a chain.
That creates a cycle, so installation is impossible.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n + m)
    - n = number of packages
    - m = number of dependency pairs
    We build the graph once and process each node and edge at most once.

    Space Complexity:
    O(n + m)
    - adjacency list stores all edges
    - indegree array stores one value per package
    - queue can hold up to n packages
    */
    public bool CanInstallAllPackages(int n, int[][] dependencies)
    {
        // We will use Kahn's Algorithm for topological sorting.
        //
        // Core idea:
        // A directed graph has no cycle if we can repeatedly remove nodes
        // that currently have indegree 0.
        //
        // In this problem:
        // dependency [a, b] means "a requires b first"
        // so the direction is:
        // b -> a
        //
        // Why?
        // Because if b must be installed before a, then b comes earlier in the order.
        //
        // Example:
        // [2, 1] means package 2 depends on package 1
        // so edge is 1 -> 2

        // Step 1:
        // Create an adjacency list for the graph.
        //
        // adjacencyList[x] will contain all packages that become available
        // after package x is installed.
        //
        // Example:
        // if dependency is [3, 2], then edge is 2 -> 3
        // so we add 3 to adjacencyList[2]
        var adjacencyList = new List<int>[n];
        for (int i = 0; i < n; i++)
        {
            adjacencyList[i] = new List<int>();
        }

        // Step 2:
        // Create an indegree array.
        //
        // indegree[p] = number of prerequisites package p still has.
        //
        // If indegree[p] == 0, it means package p currently has no unmet dependencies
        // and can be installed right now.
        var indegree = new int[n];

        // Step 3:
        // Build the graph and fill indegree counts.
        //
        // For each dependency [a, b]:
        // - a depends on b
        // - directed edge is b -> a
        // - therefore:
        //   adjacencyList[b].Add(a)
        //   indegree[a]++
        //
        // Duplicate dependency pairs are allowed by the problem statement.
        // This algorithm still works correctly with duplicates:
        // each duplicate adds one edge and one indegree count,
        // and later when processing the source node, we remove that count as well.
        foreach (var dependency in dependencies)
        {
            int package = dependency[0];
            int prerequisite = dependency[1];

            adjacencyList[prerequisite].Add(package);
            indegree[package]++;
        }

        // Step 4:
        // Initialize a queue with all packages that have indegree 0.
        //
        // Why is this necessary?
        // These are the packages that do not depend on anything else,
        // so they can be installed immediately.
        //
        // We use a queue because Kahn's algorithm processes nodes in layers:
        // take an available node, "install" it, then update its neighbors.
        var queue = new Queue<int>();

        for (int package = 0; package < n; package++)
        {
            if (indegree[package] == 0)
            {
                queue.Enqueue(package);
            }
        }

        // Step 5:
        // Process the queue.
        //
        // installedCount will track how many packages we were able to install.
        //
        // If the graph has no cycle, eventually every package will become installable,
        // and installedCount will reach n.
        //
        // If there is a cycle, the packages inside that cycle will never reach indegree 0,
        // so they will never enter the queue.
        int installedCount = 0;

        while (queue.Count > 0)
        {
            // Remove one currently installable package.
            int currentPackage = queue.Dequeue();

            // Count it as installed.
            installedCount++;

            // Step 5a:
            // Look at every package that depends on currentPackage.
            //
            // Since currentPackage is now considered installed,
            // each dependent package has one fewer unmet prerequisite.
            foreach (int dependentPackage in adjacencyList[currentPackage])
            {
                indegree[dependentPackage]--;

                // Step 5b:
                // If a dependent package now has indegree 0,
                // that means all of its prerequisites have been satisfied.
                // So it becomes installable and should be added to the queue.
                if (indegree[dependentPackage] == 0)
                {
                    queue.Enqueue(dependentPackage);
                }
            }
        }

        // Step 6:
        // Final decision.
        //
        // If we installed exactly n packages, then every package was reachable
        // through this "remove indegree 0 nodes" process.
        // That means there was no cycle.
        //
        // If installedCount < n, then some packages were stuck forever with
        // indegree > 0, which can only happen if there is a cycle.
        return installedCount == n;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// n = 4, dependencies = [[1,0],[2,1],[3,2]]
// Graph:
// 0 -> 1 -> 2 -> 3
// No cycle, so answer should be true.
int n1 = 4;
int[][] dependencies1 =
{
    new[] { 1, 0 },
    new[] { 2, 1 },
    new[] { 3, 2 }
};

bool result1 = solution.CanInstallAllPackages(n1, dependencies1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: True

// Example 2:
// n = 4, dependencies = [[1,0],[2,1],[0,2],[3,1]]
// Edges:
// 0 -> 1
// 1 -> 2
// 2 -> 0
// 1 -> 3
// There is a cycle among 0, 1, 2, so answer should be false.
int n2 = 4;
int[][] dependencies2 =
{
    new[] { 1, 0 },
    new[] { 2, 1 },
    new[] { 0, 2 },
    new[] { 3, 1 }
};

bool result2 = solution.CanInstallAllPackages(n2, dependencies2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: False

// Additional demo:
// No dependencies at all.
// Every package is independent, so installation is always possible.
int n3 = 5;
int[][] dependencies3 = Array.Empty<int[]>();

bool result3 = solution.CanInstallAllPackages(n3, dependencies3);
Console.WriteLine($"Example 3 Result: {result3}"); // Expected: True

// Additional demo with duplicate dependencies:
// Package 1 depends on 0 twice in the input.
// This still does not create a cycle.
// The algorithm correctly handles duplicates.
int n4 = 2;
int[][] dependencies4 =
{
    new[] { 1, 0 },
    new[] { 1, 0 }
};

bool result4 = solution.CanInstallAllPackages(n4, dependencies4);
Console.WriteLine($"Example 4 Result: {result4}"); // Expected: True