/*
Title: Minimum Swaps to Sort Values Within Each Tree Level

Problem Description:
You are given the root of a binary tree where each node contains an integer value.
In one operation, you may choose any two nodes that are on the same depth level and swap their values.
The structure of the tree cannot be changed; only the values stored in nodes may be swapped.

Your task is to return the minimum total number of such swaps required so that, for every depth level
independently, the values of the nodes on that level appear in strictly increasing order from left to right.

If a level already appears in strictly increasing order, it contributes 0 to the answer.
Each level is processed independently, but the final answer is the sum of the minimum swaps needed across all levels.
You may assume that for every level, all values are distinct, so a strictly increasing order is well-defined.

A common approach is to perform a breadth-first traversal to collect values level by level,
then compute the minimum number of swaps needed to sort each level.
This turns the tree problem into a repeated array-reordering problem.

Constraints:
- The number of nodes in the tree is in the range [1, 100000].
- -1000000000 <= Node.val <= 1000000000
- All node values on the same level are distinct.
- The tree is a binary tree.
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class TreeNode
{
    public int val;
    public TreeNode? left;
    public TreeNode? right;

    public TreeNode(int val = 0, TreeNode? left = null, TreeNode? right = null)
    {
        this.val = val;
        this.left = left;
        this.right = right;
    }
}

public class Solution
{
    /*
    Time Complexity:
    - Breadth-first traversal visits each node exactly once: O(n)
    - For each level, we sort that level's values. Across all levels, the total sorting cost is O(n log n) in the worst case.
    - Computing minimum swaps for each level is linear after sorting that level.
    - Overall: O(n log n)

    Space Complexity:
    - Queue for BFS can hold up to one full level: O(w), where w is the maximum width of the tree
    - Temporary arrays/lists for one level: O(w)
    - Overall auxiliary space: O(w), and in the worst case O(n)
    */
    public int MinimumOperations(TreeNode? root)
    {
        // If the tree is empty, there are no levels to process and therefore no swaps are needed.
        if (root == null)
        {
            return 0;
        }

        // This variable will accumulate the answer across all levels.
        // Each level is independent, so we simply add the minimum swaps needed for each level.
        int totalSwaps = 0;

        // We use a queue to perform a standard breadth-first search (BFS).
        // BFS is the natural choice here because it processes the tree level by level,
        // which matches the problem requirement exactly.
        Queue<TreeNode> queue = new Queue<TreeNode>();
        queue.Enqueue(root);

        // Continue until every node has been processed.
        while (queue.Count > 0)
        {
            // The current number of nodes in the queue is exactly the number of nodes
            // on the current depth level.
            int levelSize = queue.Count;

            // We collect the values of the current level in left-to-right order.
            // This order matters because the goal is to make the values strictly increasing
            // from left to right on this exact level.
            int[] levelValues = new int[levelSize];

            // Process exactly one level.
            for (int i = 0; i < levelSize; i++)
            {
                // Remove the next node from the queue.
                TreeNode current = queue.Dequeue();

                // Store its value in the level array.
                levelValues[i] = current.val;

                // Add children to the queue so they will be processed on the next level.
                if (current.left != null)
                {
                    queue.Enqueue(current.left);
                }

                if (current.right != null)
                {
                    queue.Enqueue(current.right);
                }
            }

            // Now that we have all values for this level, compute the minimum number of swaps
            // needed to sort this array into strictly increasing order.
            totalSwaps += GetMinimumSwapsToSort(levelValues);
        }

        return totalSwaps;
    }

    private int GetMinimumSwapsToSort(int[] values)
    {
        // If a level has 0 or 1 node, it is already sorted by definition.
        if (values.Length <= 1)
        {
            return 0;
        }

        // We need to know where each current element should go in the sorted order.
        // A common and correct way to compute the minimum swaps needed to sort an array
        // with distinct values is:
        // 1. Pair each value with its original index
        // 2. Sort by value
        // 3. Detect cycles in the permutation
        // 4. A cycle of length L requires L - 1 swaps
        //
        // Because values on the same level are guaranteed distinct, this method works cleanly.

        // Create an array of pairs: (value, originalIndex)
        (int Value, int OriginalIndex)[] paired = new (int Value, int OriginalIndex)[values.Length];
        for (int i = 0; i < values.Length; i++)
        {
            paired[i] = (values[i], i);
        }

        // Sort the pairs by value so that position i in this sorted array tells us
        // which original element should end up at sorted position i.
        Array.Sort(paired, (a, b) => a.Value.CompareTo(b.Value));

        // visited[i] tells us whether sorted position i has already been included
        // in a cycle we processed.
        bool[] visited = new bool[values.Length];

        int swaps = 0;

        // Walk through each sorted position and find cycles.
        for (int i = 0; i < paired.Length; i++)
        {
            // If already visited, this position is part of a cycle we already counted.
            if (visited[i])
            {
                continue;
            }

            // If the element currently associated with sorted position i originally came from i,
            // then it is already effectively in the correct place and forms a cycle of length 1.
            if (paired[i].OriginalIndex == i)
            {
                visited[i] = true;
                continue;
            }

            // Start following the cycle beginning at i.
            int cycleLength = 0;
            int currentIndex = i;

            while (!visited[currentIndex])
            {
                visited[currentIndex] = true;

                // Move to the next index in the cycle.
                // Why does this work?
                // paired[currentIndex].OriginalIndex tells us where the current sorted-position element
                // originally came from. That original index corresponds to the next position we must inspect
                // in the permutation mapping.
                currentIndex = paired[currentIndex].OriginalIndex;
                cycleLength++;
            }

            // A cycle with k elements needs exactly k - 1 swaps to place all elements correctly.
            if (cycleLength > 1)
            {
                swaps += cycleLength - 1;
            }
        }

        return swaps;
    }
}

static TreeNode? BuildTreeFromLevelOrder(int?[] values)
{
    if (values.Length == 0 || values[0] == null)
    {
        return null;
    }

    TreeNode root = new TreeNode(values[0]!.Value);
    Queue<TreeNode> queue = new Queue<TreeNode>();
    queue.Enqueue(root);

    int index = 1;

    while (queue.Count > 0 && index < values.Length)
    {
        TreeNode current = queue.Dequeue();

        if (index < values.Length && values[index] != null)
        {
            current.left = new TreeNode(values[index]!.Value);
            queue.Enqueue(current.left);
        }
        index++;

        if (index < values.Length && values[index] != null)
        {
            current.right = new TreeNode(values[index]!.Value);
            queue.Enqueue(current.right);
        }
        index++;
    }

    return root;
}

var solution = new Solution();

// Demo 1:
// The problem statement contains a contradiction in Example 1.
// It says to use the exact shown array but also says to assume the intended level order is [5,3,4,7,6,8,9].
// To match the stated output of 1, we use the intended level order.
TreeNode? root1 = BuildTreeFromLevelOrder(new int?[] { 5, 3, 4, 7, 6, 8, 9 });
Console.WriteLine(solution.MinimumOperations(root1)); // Expected: 1

// Demo 2:
// As written in the statement, [1,7,5,4,6,8,9] would actually require:
// level 1: [7,5] -> 1 swap
// level 2: [4,6,8,9] -> already sorted
// total = 1
TreeNode? root2 = BuildTreeFromLevelOrder(new int?[] { 1, 7, 5, 4, 6, 8, 9 });
Console.WriteLine(solution.MinimumOperations(root2)); // Expected: 1

// Additional demo using the literal first array shown in the statement.
// This demonstrates the algorithm's correctness on the actual values:
// level 1: [4,3] -> 1 swap
// level 2: [7,6,8,9] -> 1 swap
// total = 2
TreeNode? root3 = BuildTreeFromLevelOrder(new int?[] { 5, 4, 3, 7, 6, 8, 9 });
Console.WriteLine(solution.MinimumOperations(root3)); // Expected: 2