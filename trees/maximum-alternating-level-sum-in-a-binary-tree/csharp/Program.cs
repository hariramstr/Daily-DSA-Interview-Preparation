/*
Title: Maximum Alternating Level Sum in a Binary Tree
Difficulty: Medium
Topic: Trees

Problem Description:
You are given the root of a binary tree where each node contains an integer value, which may be positive, zero, or negative.
For any node, define its alternating level sum as the sum of values in its subtree where nodes at even distance from that node
are added and nodes at odd distance from that node are subtracted.

In other words, for a chosen node x:
- include x.val
- subtract the values of its children
- add the values of its grandchildren
- subtract the values of its great-grandchildren
- and so on for the entire subtree of x

Your task is to return the maximum alternating level sum among all nodes in the tree.

This is not the same as choosing a path. For every candidate node, you must evaluate its entire subtree with alternating signs
by depth relative to that node, then take the largest result over all nodes.

Constraints:
- The number of nodes is in the range [1, 100000].
- -100000 <= Node.val <= 100000
- The tree is a valid binary tree.

Key Insight:
Let alt(node) be the alternating level sum of the subtree rooted at node.

Then:
alt(node) = node.val - alt(left) - alt(right)

Why?
- node itself is at distance 0 from itself, so it is added
- every node in the left subtree is one level deeper relative to node than it is relative to left
  so the sign flips, which means we subtract alt(left)
- same logic for the right subtree

This gives a direct tree DP relation.
If we compute alt(node) for every node exactly once using postorder traversal, then the answer is simply
the maximum alt(node) over all nodes.

Example 1:
root = [5,2,4,1,3,null,6]

alt(1) = 1
alt(3) = 3
alt(2) = 2 - 1 - 3 = -2
alt(6) = 6
alt(4) = 4 - 0 - 6 = -2
alt(5) = 5 - (-2) - (-2) = 9

Maximum = 9

Example 2:
root = [-3,7,2,-5,1]

alt(-5) = -5
alt(1) = 1
alt(7) = 7 - (-5) - 1 = 11
alt(2) = 2
alt(-3) = -3 - 11 - 2 = -16

Maximum = 11
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
    private long _best;

    /*
    Time Complexity: O(n)
    - We visit each node exactly once.
    - At each node, we do only constant extra work.

    Space Complexity: O(h)
    - h is the height of the tree due to recursion stack.
    - In the worst case of a completely skewed tree, h can be O(n).
    - In a balanced tree, h is O(log n).

    Note:
    We use long internally because:
    - up to 100000 nodes
    - each value can be as large as 100000 in magnitude
    - subtree sums can exceed int range
    */
    public long MaximumAlternatingLevelSum(TreeNode? root)
    {
        // Initialize the global best answer to the smallest possible long value.
        // We do this because node values may be negative, so the answer could also be negative.
        _best = long.MinValue;

        // Start a postorder DFS.
        // Postorder means:
        // 1. solve left subtree
        // 2. solve right subtree
        // 3. solve current node using children's results
        //
        // This order is necessary because alt(node) depends on alt(left) and alt(right).
        Dfs(root);

        // After processing the whole tree, _best contains the maximum alternating level sum
        // among all subtree roots.
        return _best;
    }

    private long Dfs(TreeNode? node)
    {
        // Base case:
        // An empty subtree contributes 0.
        //
        // This is correct because if a child does not exist, there are no nodes to add or subtract.
        if (node == null)
        {
            return 0L;
        }

        // Recursively compute the alternating sum for the left subtree.
        // We must do this first so that when we compute the current node's value,
        // we already know the left subtree's alternating sum.
        long leftAlt = Dfs(node.left);

        // Recursively compute the alternating sum for the right subtree.
        long rightAlt = Dfs(node.right);

        // Core DP formula:
        //
        // alt(node) = node.val - alt(left) - alt(right)
        //
        // Why subtraction?
        // Because every node inside a child subtree is one level deeper relative to the current node,
        // so all signs flip compared to that child's own alternating sum.
        //
        // Example:
        // If alt(left) = left - grandchildren + great-grandchildren - ...
        // then relative to current node it becomes:
        // -(left) + grandchildren - great-grandchildren + ...
        // which is exactly subtracting alt(left).
        long currentAlt = (long)node.val - leftAlt - rightAlt;

        // Update the global maximum answer.
        // Every node is a valid candidate root for the problem,
        // so after computing currentAlt for this node, we compare it with the best seen so far.
        if (currentAlt > _best)
        {
            _best = currentAlt;
        }

        // Return the alternating sum for this subtree to the parent.
        // The parent will use it in its own formula.
        return currentAlt;
    }
}

static TreeNode? BuildTree(int?[] values)
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

// Demo 1:
// Input: [5,2,4,1,3,null,6]
// Expected output: 9
var root1 = BuildTree(new int?[] { 5, 2, 4, 1, 3, null, 6 });
var solution = new Solution();
long result1 = solution.MaximumAlternatingLevelSum(root1);
Console.WriteLine(result1);

// Demo 2:
// Input: [-3,7,2,-5,1]
// Expected output: 11
var root2 = BuildTree(new int?[] { -3, 7, 2, -5, 1 });
long result2 = solution.MaximumAlternatingLevelSum(root2);
Console.WriteLine(result2);