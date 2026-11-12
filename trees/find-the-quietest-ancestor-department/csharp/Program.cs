/*
Title: Find the Quietest Ancestor Department

Problem Description:
A company models its organization as a rooted tree of departments. Department 0 is the root headquarters.
Each department has a non-negative noise score representing how disruptive its daily operations are.

For every department, we must find the ancestor department on the path from the root to that department
(including the department itself) that has the smallest noise score.

If multiple departments on that path have the same minimum noise score, we must return the one that is
closest to the root.

Input:
- n: number of departments
- parent: parent array of length n, where parent[0] = -1
- noise: noise score array of length n

Output:
- answer array of length n where answer[i] is the index of the quietest ancestor department for department i

Constraints:
- 1 <= n <= 100000
- parent.length == n
- noise.length == n
- parent[0] == -1
- 0 <= parent[i] < n for i > 0
- 0 <= noise[i] <= 1000000000
- The parent array represents a valid rooted tree

Examples:
1)
n = 7
parent = [-1,0,0,1,1,2,2]
noise  = [5,3,8,4,2,8,1]
Output = [0,1,0,1,4,0,6]

2)
n = 5
parent = [-1,0,1,2,3]
noise  = [4,4,6,2,2]
Output = [0,0,0,3,3]
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building the children adjacency list takes O(n)
    - DFS visits each node exactly once, so O(n)
    - Total: O(n)

    Space Complexity:
    - Children adjacency list stores all edges: O(n)
    - Answer array: O(n)
    - Explicit stack for iterative DFS: O(n)
    - Total: O(n)

    Beginner-friendly idea:
    Instead of, for every node, walking upward through all ancestors (which could be O(n^2) in a chain),
    we do one traversal from the root downward.

    While moving from parent to child, we carry along the best (quietest) ancestor seen so far on the current path.
    That way, each node can determine its answer in O(1) time from information already known at its parent.
    */
    public int[] FindQuietestAncestorDepartment(int n, int[] parent, int[] noise)
    {
        // Step 1:
        // Build a "children list" representation of the tree.
        //
        // Why do we do this?
        // The input gives us parent[i], which is great for moving from a node to its parent,
        // but our algorithm wants to traverse from the root downward to all descendants.
        // For that, it is much more convenient to know, for each node, which children it has.
        //
        // Data structure choice:
        // We use an array of List<int>, where children[u] contains all direct children of node u.
        var children = new List<int>[n];
        for (int i = 0; i < n; i++)
        {
            children[i] = new List<int>();
        }

        for (int i = 1; i < n; i++)
        {
            children[parent[i]].Add(i);
        }

        // Step 2:
        // Prepare the answer array.
        //
        // answer[i] will store the index of the quietest ancestor on the path from root to i.
        var answer = new int[n];

        // Step 3:
        // We will perform an iterative DFS starting from the root (node 0).
        //
        // Why iterative DFS instead of recursive DFS?
        // Because n can be as large as 100000.
        // A recursive DFS on a very deep tree (for example, a chain) could cause a stack overflow.
        // Using our own explicit stack avoids that problem and is safer for large inputs.
        //
        // Each stack entry stores:
        // - Node: the current department we are visiting
        // - BestAncestor: the index of the quietest ancestor seen on the path from root to this node
        //
        // Important tie-breaking rule:
        // If current node has the same noise as the best ancestor so far, we KEEP the old best ancestor,
        // because the old one is closer to the root.
        //
        // So we only replace the best ancestor when the current node has STRICTLY smaller noise.
        var stack = new Stack<(int Node, int BestAncestor)>();

        // For the root:
        // The path from root to root contains only itself, so the quietest ancestor is root itself.
        stack.Push((0, 0));

        // Step 4:
        // Standard DFS loop: process nodes until the stack is empty.
        while (stack.Count > 0)
        {
            var (node, bestAncestor) = stack.Pop();

            // Step 4a:
            // Record the answer for the current node.
            //
            // At this moment, bestAncestor already represents the quietest department
            // on the path from root to "node", with correct tie-breaking.
            answer[node] = bestAncestor;

            // Step 4b:
            // Visit each child of the current node.
            //
            // For every child, we must determine what the quietest ancestor is on the path
            // from root to that child.
            //
            // The path to the child is exactly:
            // path to current node + child itself
            //
            // So the quietest ancestor for the child is either:
            // - the current bestAncestor from the parent path, or
            // - the child itself, if the child's noise is strictly smaller
            foreach (int child in children[node])
            {
                // Start by assuming the best ancestor for the child is the same as for the parent path.
                int childBestAncestor = bestAncestor;

                // Compare the child's noise with the current best noise.
                //
                // If the child is strictly quieter, it becomes the new best ancestor.
                // If equal, we do NOT update, because the earlier node (already in bestAncestor)
                // is closer to the root and must win the tie.
                if (noise[child] < noise[bestAncestor])
                {
                    childBestAncestor = child;
                }

                // Push the child with its computed best ancestor onto the stack.
                stack.Push((child, childBestAncestor));
            }
        }

        return answer;
    }
}

static void PrintArray(string label, int[] array)
{
    Console.WriteLine(label + "[" + string.Join(",", array) + "]");
}

var solution = new Solution();

// Demo Example 1
int n1 = 7;
int[] parent1 = { -1, 0, 0, 1, 1, 2, 2 };
int[] noise1 = { 5, 3, 8, 4, 2, 8, 1 };
int[] result1 = solution.FindQuietestAncestorDepartment(n1, parent1, noise1);

Console.WriteLine("Example 1");
PrintArray("parent = ", parent1);
PrintArray("noise  = ", noise1);
PrintArray("result = ", result1);
Console.WriteLine("expected = [0,1,0,1,4,0,6]");
Console.WriteLine();

// Demo Example 2
int n2 = 5;
int[] parent2 = { -1, 0, 1, 2, 3 };
int[] noise2 = { 4, 4, 6, 2, 2 };
int[] result2 = solution.FindQuietestAncestorDepartment(n2, parent2, noise2);

Console.WriteLine("Example 2");
PrintArray("parent = ", parent2);
PrintArray("noise  = ", noise2);
PrintArray("result = ", result2);
Console.WriteLine("expected = [0,0,0,3,3]");
Console.WriteLine();

// Additional small demo
int n3 = 1;
int[] parent3 = { -1 };
int[] noise3 = { 10 };
int[] result3 = solution.FindQuietestAncestorDepartment(n3, parent3, noise3);

Console.WriteLine("Example 3");
PrintArray("parent = ", parent3);
PrintArray("noise  = ", noise3);
PrintArray("result = ", result3);
Console.WriteLine("expected = [0]");