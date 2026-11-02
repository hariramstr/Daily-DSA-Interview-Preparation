/*
Title: Longest Alternating Turn Path in a Binary Tree

Problem Description:
You are given the root of a binary tree. A path may start at any node and must move only downward,
following parent-to-child edges. For every step in the path, you choose either the left child or
the right child of the current node. A path is called an alternating turn path if the direction of
every move alternates: left, right, left, right, ... or right, left, right, left, ... .
The path does not need to end at a leaf, and it may contain just one node.

Return the length of the longest alternating turn path in the tree, measured in number of edges.

A single node has length 0.

The expected solution should run in O(n) time.
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
    Space Complexity: O(h)

    Explanation:
    - We visit each node exactly once, so the total work is linear in the number of nodes.
    - The extra space comes from the recursion stack, which in the worst case is the tree height h.
    */
    public int LongestAlternatingTurnPath(TreeNode? root)
    {
        // If the tree is empty, there is no path.
        // The problem constraints say there is at least one node, but handling null makes the method safer.
        if (root == null)
        {
            return 0;
        }

        // This variable stores the best answer found anywhere in the tree.
        // We update it while processing every node.
        _best = 0;

        // Start a post-order depth-first search.
        // Post-order is useful here because the answer for a node depends on answers from its children.
        Dfs(root);

        // After processing the entire tree, _best contains the maximum alternating path length in edges.
        return _best;
    }

    private (int startLeft, int startRight) Dfs(TreeNode? node)
    {
        // Base case:
        // If the node does not exist, then there is no valid path starting from it.
        //
        // We return (-1, -1) instead of (0, 0) for an important reason:
        // - Suppose a real node has a left child.
        // - If that left child is a leaf, then from the current node we can take exactly one left edge.
        // - We want that to evaluate to 1.
        // - The formula we use is: 1 + child.startRight
        // - For a leaf child, child.startRight should be 0.
        // - That leaf's missing children return -1, so the leaf itself computes 0 correctly.
        //
        // This trick makes the edge-counting logic clean and consistent.
        if (node == null)
        {
            return (-1, -1);
        }

        // Recursively solve the left subtree first.
        // leftInfo.startLeft  = longest alternating path starting at node.left if first move is left
        // leftInfo.startRight = longest alternating path starting at node.left if first move is right
        var leftInfo = Dfs(node.left);

        // Recursively solve the right subtree.
        var rightInfo = Dfs(node.right);

        // We now compute two values for the current node:
        //
        // 1) startLeft:
        //    The longest alternating path that STARTS at this node and whose FIRST move is to the LEFT child.
        //
        //    If there is no left child, then we cannot make a first left move, so the length is 0.
        //    If there is a left child, then we use one edge to go left.
        //    After that, the next move must be RIGHT to keep alternating.
        //    Therefore we add:
        //      1 + (best path starting at left child with first move RIGHT)
        int startLeft = node.left == null ? 0 : 1 + leftInfo.startRight;

        // 2) startRight:
        //    The longest alternating path that STARTS at this node and whose FIRST move is to the RIGHT child.
        //
        //    If there is no right child, then we cannot make a first right move, so the length is 0.
        //    If there is a right child, then we spend one edge to go right.
        //    After that, the next move must be LEFT.
        int startRight = node.right == null ? 0 : 1 + rightInfo.startLeft;

        // Update the global best answer.
        //
        // Why is this necessary?
        // The longest valid path can start at ANY node, not just the root.
        // Since we compute the best "start here and go left first" and "start here and go right first"
        // for every node, checking both values at every node guarantees we do not miss the true answer.
        _best = Math.Max(_best, Math.Max(startLeft, startRight));

        // Return both values to the parent.
        //
        // The parent needs these exact two numbers because:
        // - If the parent goes left into this node, then the next required direction is right,
        //   so it will use this node's startRight.
        // - If the parent goes right into this node, then the next required direction is left,
        //   so it will use this node's startLeft.
        return (startLeft, startRight);
    }
}

static TreeNode? BuildTree(params int?[] values)
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

// Example 1 from the prompt:
// root = [1,2,3,null,4,5,null,null,null,6]
//
// Tree shape:
//         1
//       /   \
//      2     3
//       \   /
//        4 5
//          /
//         6
//
// One longest alternating path is 1 -> 2 -> 4
// Directions: left, right
// Length = 2
var root1 = BuildTree(1, 2, 3, null, 4, 5, null, null, null, 6);
Console.WriteLine(solution.LongestAlternatingTurnPath(root1)); // Expected: 2

// Example 2 from the prompt:
// root = [7,9,8,4,null,null,10,null,5]
//
// Tree shape:
//         7
//       /   \
//      9     8
//     /       \
//    4         10
//     \
//      5
//
// A valid alternating path is 7 -> 9 -> 4 -> 5
// Directions: left, left, right => not alternating from the start, so that specific full path is invalid.
// But the best valid path in this actual structure is 7 -> 9 -> 4 -> 5 if interpreted as starting where alternation holds
// only after choosing a proper start node; from node 9, path 9 -> 4 -> 5 is left then right = 2.
// For this exact tree, the algorithm computes the true maximum alternating path length.
var root2 = BuildTree(7, 9, 8, 4, null, null, 10, null, 5);
Console.WriteLine(solution.LongestAlternatingTurnPath(root2)); // Computed result for this structure

// Additional demo:
// Single node tree => answer is 0 because there are no edges.
var root3 = BuildTree(42);
Console.WriteLine(solution.LongestAlternatingTurnPath(root3)); // Expected: 0

// Additional demo:
// A perfect alternating chain:
//       1
//      /
//     2
//      \
//       3
//      /
//     4
// Path 1 -> 2 -> 3 -> 4 uses left, right, left => length 3
var root4 = new TreeNode(1,
    new TreeNode(2,
        null,
        new TreeNode(3,
            new TreeNode(4),
            null)),
    null);
Console.WriteLine(solution.LongestAlternatingTurnPath(root4)); // Expected: 3