/*
Title: Minimum Relabels to Sort a Binary Tree by Level
Difficulty: Medium
Topic: Trees

Problem Description:
You are given the root of a binary tree where each node stores an integer label. In one operation, you may swap the labels of any two nodes that are on the same depth level of the tree. The tree structure itself cannot be changed; only labels may move between nodes on the same level.

Your task is to return the minimum number of such swaps needed so that, for every depth level independently, the labels appearing from left to right are in strictly nondecreasing order. In other words, if you perform a level-order traversal and look at the nodes level by level, each level must end up sorted when read from left to right.

If a level already appears sorted, it requires 0 operations. Levels are independent: a label from one level can never be moved to another level. You should compute the total minimum number of swaps across all levels.

It is guaranteed that the tree contains between 1 and 100000 nodes, and each node value is between -1000000000 and 1000000000. The tree is not necessarily complete or balanced. An O(n log n) solution is expected.

Example 1:
Input: root = [5,4,3,7,6,8,9]
Output: 1

Example 2:
Input: root = [10,1,8,7,6,5,4]
Output: 2
*/

using System;
using System.Collections.Generic;

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
        - Breadth-first traversal visits every node exactly once: O(n)
        - For each level, we sort that level's values. Across all levels, the total sorting cost is O(n log n) in the worst case.
        - Therefore, total time complexity is O(n log n)

        Space Complexity:
        - Queue for BFS can hold up to O(w) nodes, where w is the maximum width of the tree
        - Additional arrays/lists for one level at a time also use O(w)
        - Therefore, auxiliary space is O(w), which is O(n) in the worst case
    */
    public int MinimumOperations(TreeNode? root)
    {
        // If the tree is empty, there are no levels to sort and therefore no swaps are needed.
        if (root == null)
        {
            return 0;
        }

        // We will perform a standard breadth-first search (BFS).
        // BFS is the natural choice here because it processes the tree level by level,
        // which matches the problem statement exactly.
        Queue<TreeNode> queue = new Queue<TreeNode>();
        queue.Enqueue(root);

        // This variable accumulates the answer across all levels.
        int totalSwaps = 0;

        // Continue until every node has been processed.
        while (queue.Count > 0)
        {
            // The number of nodes currently in the queue is exactly the number of nodes
            // on the current depth level.
            int levelSize = queue.Count;

            // We will collect the values of the current level from left to right.
            // This order matters because the problem asks for each level to be sorted
            // when read from left to right.
            int[] levelValues = new int[levelSize];

            // Process exactly one level.
            for (int i = 0; i < levelSize; i++)
            {
                TreeNode node = queue.Dequeue();

                // Store the current node's value in left-to-right order.
                levelValues[i] = node.val;

                // Add children to the queue so they will be processed on the next level.
                if (node.left != null)
                {
                    queue.Enqueue(node.left);
                }

                if (node.right != null)
                {
                    queue.Enqueue(node.right);
                }
            }

            // For this one level, compute the minimum number of swaps needed
            // to transform levelValues into sorted order.
            totalSwaps += MinSwapsToSort(levelValues);
        }

        return totalSwaps;
    }

    private int MinSwapsToSort(int[] values)
    {
        // A level with 0 or 1 element is already sorted.
        if (values.Length <= 1)
        {
            return 0;
        }

        // We need the minimum number of swaps required to sort this array.
        //
        // Important note about duplicates:
        // The problem statement allows integer labels in a wide range and does not forbid duplicates.
        // Because of that, we must handle repeated values correctly.
        //
        // A common "value -> target index" trick works only when all values are distinct.
        // To support duplicates safely, we instead:
        // 1. Create pairs: (value, originalIndex)
        // 2. Sort those pairs by value, then by originalIndex
        // 3. Build a permutation that tells us where each original position should go
        // 4. Count cycles in that permutation
        //
        // Why cycle counting works:
        // If a cycle has length L, then it takes exactly L - 1 swaps to place all elements
        // in their correct positions. Summing this over all cycles gives the minimum number of swaps.

        int n = values.Length;

        // Each entry stores:
        // - Value: the number at this position
        // - OriginalIndex: where it came from in the current level
        (int Value, int OriginalIndex)[] paired = new (int Value, int OriginalIndex)[n];

        for (int i = 0; i < n; i++)
        {
            paired[i] = (values[i], i);
        }

        // Sort by value first so the final order is nondecreasing.
        // Break ties by original index to make the target arrangement deterministic.
        Array.Sort(paired, (a, b) =>
        {
            int compareValue = a.Value.CompareTo(b.Value);
            if (compareValue != 0)
            {
                return compareValue;
            }

            return a.OriginalIndex.CompareTo(b.OriginalIndex);
        });

        // targetPosition[originalIndex] = sortedIndex
        //
        // After sorting, paired[sortedIndex] tells us which original element belongs there.
        // So we can build a mapping from each original position to its destination position.
        int[] targetPosition = new int[n];
        for (int sortedIndex = 0; sortedIndex < n; sortedIndex++)
        {
            int originalIndex = paired[sortedIndex].OriginalIndex;
            targetPosition[originalIndex] = sortedIndex;
        }

        // visited[i] tells us whether original position i has already been included in a cycle.
        bool[] visited = new bool[n];
        int swaps = 0;

        // Walk through every position and count permutation cycles.
        for (int i = 0; i < n; i++)
        {
            // If already visited, this position is already part of a processed cycle.
            if (visited[i])
            {
                continue;
            }

            // If targetPosition[i] == i, then the element at position i is already
            // in a valid sorted position and contributes no swaps.
            if (targetPosition[i] == i)
            {
                visited[i] = true;
                continue;
            }

            // Follow the cycle starting from i.
            int cycleLength = 0;
            int current = i;

            while (!visited[current])
            {
                visited[current] = true;
                current = targetPosition[current];
                cycleLength++;
            }

            // A cycle of length L needs L - 1 swaps.
            if (cycleLength > 1)
            {
                swaps += cycleLength - 1;
            }
        }

        return swaps;
    }
}

// Demo code

TreeNode sample1 =
    new TreeNode(5,
        new TreeNode(4,
            new TreeNode(7),
            new TreeNode(6)),
        new TreeNode(3,
            new TreeNode(8),
            new TreeNode(9)));

TreeNode sample2 =
    new TreeNode(10,
        new TreeNode(1,
            new TreeNode(7),
            new TreeNode(6)),
        new TreeNode(8,
            new TreeNode(5),
            new TreeNode(4)));

Solution solution = new Solution();

int result1 = solution.MinimumOperations(sample1);
int result2 = solution.MinimumOperations(sample2);

Console.WriteLine(result1);
Console.WriteLine(result2);