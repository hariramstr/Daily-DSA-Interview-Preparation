/*
Title: Longest Consecutive Rank Path in a Binary Tree
Difficulty: Medium
Topic: Trees

Problem Description:
You are given the root of a binary tree where each node stores an integer rank. A valid rank chain is any path that moves only through parent-child edges, and at every step the rank must change by exactly 1. The path may be strictly increasing, strictly decreasing, or switch direction once at a middle node by combining a decreasing side and an increasing side. In other words, the path does not need to start at the root, does not need to end at a leaf, and may go from one descendant up through a node and down to another descendant, as long as every adjacent pair differs by exactly 1.

Return the length of the longest valid rank chain in the tree, measured as the number of nodes in the path.

For example, if one side of a node forms 7 -> 6 -> 5 and the other side forms 5 -> 6, then the combined path 7 -> 6 -> 5 -> 6 has differences of 1 at every step and is valid.

Constraints:
- The number of nodes in the tree is in the range [1, 100000].
- -1000000000 <= Node.val <= 1000000000
- The tree is a binary tree.
- Your solution should run in O(n) time.

Example 1:
Input: root = [4,3,5,2,null,null,6,1]
Output: 6
Explanation: The longest valid path is 1 -> 2 -> 3 -> 4 -> 5 -> 6, which uses parent-child edges and changes by exactly 1 at each step.

Example 2:
Input: root = [10,9,11,8,10,null,12]
Output: 5
Explanation: One longest valid path is 8 -> 9 -> 10 -> 11 -> 12. The path passes through the root and every adjacent pair differs by exactly 1.

A single node is always a valid rank chain of length 1.
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
    private int _best;

    /*
    Time Complexity: O(n)
    Space Complexity: O(h) for recursion stack, where h is the height of the tree.
    In the worst case of a completely skewed tree, h can be n.

    Beginner-friendly idea:
    For every node, we compute two values:
    1. inc = length of the longest path starting at this node and going downward
       where values increase by exactly 1 at each step
       Example: 4 -> 5 -> 6 gives inc = 3 at node 4

    2. dec = length of the longest path starting at this node and going downward
       where values decrease by exactly 1 at each step
       Example: 4 -> 3 -> 2 gives dec = 3 at node 4

    Why do we need both?
    Because the best overall path may pass through a middle node.
    For example:
        1 -> 2 -> 3 -> 4 -> 5 -> 6
    At node 4:
        left side contributes a decreasing chain when viewed from node 4 downward: 4 -> 3 -> 2 -> 1
        right side contributes an increasing chain: 4 -> 5 -> 6
    We can combine them as:
        dec + inc - 1
    We subtract 1 because the middle node is counted in both chains.

    This works because a valid path can:
    - be only increasing
    - be only decreasing
    - decrease into a middle node and then increase away from it
    */
    public int LongestConsecutive(TreeNode? root)
    {
        _best = 0;

        // We run a post-order DFS so that when we process a node,
        // we already know the best increasing/decreasing chains from its children.
        Dfs(root);

        return _best;
    }

    private (int inc, int dec) Dfs(TreeNode? node)
    {
        // Base case:
        // An empty node contributes no chain.
        // Returning (0, 0) is convenient because it means "nothing to extend".
        if (node == null)
        {
            return (0, 0);
        }

        // Recursively solve the left and right subtrees first.
        // This is necessary because the current node's answer depends on child answers.
        var left = Dfs(node.left);
        var right = Dfs(node.right);

        // Every single node by itself is a valid path of length 1.
        // So both increasing and decreasing chains start at 1.
        int inc = 1;
        int dec = 1;

        // -----------------------------
        // Step 1: Try to extend from the left child
        // -----------------------------
        if (node.left != null)
        {
            // If left child is exactly current + 1,
            // then current -> left is an increasing-by-1 step.
            // Example: current = 4, left = 5
            // If left has an increasing chain of length 3 (5 -> 6 -> 7),
            // then current can extend it to length 4 (4 -> 5 -> 6 -> 7).
            if (node.left.val == node.val + 1)
            {
                inc = Math.Max(inc, 1 + left.inc);
            }

            // If left child is exactly current - 1,
            // then current -> left is a decreasing-by-1 step.
            // Example: current = 4, left = 3
            // If left has a decreasing chain 3 -> 2 -> 1 of length 3,
            // then current can extend it to 4 -> 3 -> 2 -> 1 of length 4.
            if (node.left.val == node.val - 1)
            {
                dec = Math.Max(dec, 1 + left.dec);
            }
        }

        // -----------------------------
        // Step 2: Try to extend from the right child
        // -----------------------------
        if (node.right != null)
        {
            // Same logic as the left child, but now using the right subtree.
            if (node.right.val == node.val + 1)
            {
                inc = Math.Max(inc, 1 + right.inc);
            }

            if (node.right.val == node.val - 1)
            {
                dec = Math.Max(dec, 1 + right.dec);
            }
        }

        // -----------------------------
        // Step 3: Update the global best answer using this node as the "middle"
        // -----------------------------
        // Why combine dec and inc?
        // Because a longest valid path may come from one side into this node
        // and then continue out the other side.
        //
        // Example:
        // left side gives 1 -> 2 -> 3 -> 4  (viewed from node 4, this is dec = 4 downward as 4 -> 3 -> 2 -> 1)
        // right side gives 4 -> 5 -> 6      (inc = 3)
        // Combined path length = 4 + 3 - 1 = 6
        //
        // We subtract 1 because the current node is included in both chains.
        _best = Math.Max(_best, inc + dec - 1);

        // Return both values to the parent.
        // The parent may use this node as part of its own increasing or decreasing chain.
        return (inc, dec);
    }
}

static TreeNode? BuildTree(int?[] values)
{
    if (values.Length == 0 || values[0] == null)
    {
        return null;
    }

    var root = new TreeNode(values[0]!.Value);
    var queue = new Queue<TreeNode>();
    queue.Enqueue(root);

    int index = 1;

    while (queue.Count > 0 && index < values.Length)
    {
        var current = queue.Dequeue();

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

// Example 1:
// Tree from level-order: [4,3,5,2,null,null,6,1]
// Structure:
//         4
//       /   \
//      3     5
//     /       \
//    2         6
//   /
//  1
//
// Longest valid path: 1 -> 2 -> 3 -> 4 -> 5 -> 6
// Expected output: 6
var root1 = BuildTree(new int?[] { 4, 3, 5, 2, null, null, 6, 1 });
Console.WriteLine(solution.LongestConsecutive(root1));

// Example 2:
// Tree from level-order: [10,9,11,8,10,null,12]
// Structure:
//         10
//       /    \
//      9      11
//     / \       \
//    8  10       12
//
// One longest valid path: 8 -> 9 -> 10 -> 11 -> 12
// Expected output: 5
var root2 = BuildTree(new int?[] { 10, 9, 11, 8, 10, null, 12 });
Console.WriteLine(solution.LongestConsecutive(root2));

// Additional small demo:
// Single node tree
// Expected output: 1
var root3 = new TreeNode(42);
Console.WriteLine(solution.LongestConsecutive(root3));