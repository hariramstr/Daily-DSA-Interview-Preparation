/*
Title: Longest Zigzag Turn Path in a Binary Tree
Difficulty: Medium
Topic: Trees

Problem Description:
You are given the root of a binary tree. A path is called a zigzag turn path if every move alternates between going to a left child and going to a right child. The path may start at any node in the tree, and it must always move downward from parent to child. The length of a path is the number of edges used, not the number of nodes.

Your task is to return the maximum possible length of any zigzag turn path in the tree.

For example, if you move from a node to its left child, then the next step in the path must go to a right child, then left again, and so on. A path consisting of a single node has length 0.

You should design an algorithm that runs efficiently for large trees.

Constraints:
- The number of nodes in the tree is in the range [1, 10^5].
- Node values are in the range [-10^9, 10^9].
- All node values are arbitrary and do not affect the answer.
- The tree is not necessarily balanced.

Example 1:
Input: root = [1,null,2,3,4,null,null,5,null]
Output: 2
Explanation: One longest zigzag path is 2 -> 3 -> 5. The moves are right-to-left, which is valid because the direction alternates at each step. The path length is 2 edges.

Example 2:
Input: root = [7,4,9,2,6,null,10,null,3,5,null]
Output: 3
Explanation: One longest zigzag path is 7 -> 4 -> 6 -> 5. The moves are left, then right, then left, so the path alternates correctly. Its length is 3.

Note: The tree is represented in level-order array form, where null indicates a missing child.
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
    We compute, for every node:
    1. The longest zigzag path starting from this node if the FIRST move goes left.
    2. The longest zigzag path starting from this node if the FIRST move goes right.

    Why this works:
    - If we know these two values for children, we can build the answer for the current node.
    - A zigzag alternates directions, so:
      * If we go left first from the current node, then the next move must go right.
      * If we go right first from the current node, then the next move must go left.

    So:
    - leftFirst = 1 + (child's rightFirst)
    - rightFirst = 1 + (child's leftFirst)

    We do this with a post-order DFS:
    - Solve left subtree
    - Solve right subtree
    - Use those results to solve current node
    - Update the global maximum answer
    */
    public int LongestZigZag(TreeNode? root)
    {
        _best = 0;

        // Start a depth-first search from the root.
        // The DFS will examine every node exactly once and compute the best zigzag
        // values that begin at that node.
        Dfs(root);

        // _best stores the maximum zigzag length seen anywhere in the tree.
        return _best;
    }

    private (int leftFirst, int rightFirst) Dfs(TreeNode? node)
    {
        // Base case:
        // If the node does not exist, there is no path starting here.
        //
        // We return (-1, -1) instead of (0, 0) on purpose.
        // Why?
        // Suppose a real node has no left child.
        // Then:
        //   leftFirst = left child does not exist => cannot take that edge
        //
        // But if a node DOES have a left child and that child contributes no further edges,
        // the path length should still count the one edge from current node to left child.
        //
        // Returning -1 for null makes this formula work naturally:
        //   leftFirst = leftChild.rightFirst + 1
        //
        // If leftChild is null, we do not use that formula and keep 0.
        // If leftChild exists but has no continuation, its rightFirst may be 0,
        // so current leftFirst becomes 1, which correctly counts the single edge.
        if (node == null)
        {
            return (-1, -1);
        }

        // Step 1:
        // Recursively solve the left subtree.
        //
        // This gives us two values for node.left:
        // - leftInfo.leftFirst  = best zigzag starting at left child if first move is left
        // - leftInfo.rightFirst = best zigzag starting at left child if first move is right
        //
        // We need these values because if we move from current node to its left child,
        // the NEXT move must be to the right to keep the zigzag alternating.
        var leftInfo = Dfs(node.left);

        // Step 2:
        // Recursively solve the right subtree.
        //
        // Similarly, if we move from current node to its right child,
        // the NEXT move must be to the left.
        var rightInfo = Dfs(node.right);

        // Step 3:
        // Compute the best zigzag path starting at the current node
        // when the FIRST move is to the left.
        //
        // If there is no left child, we cannot take a left edge,
        // so the longest such path is 0 edges.
        //
        // If there IS a left child:
        // - We use 1 edge to go from current node to left child.
        // - After that, the next required direction is right.
        // - So we add leftInfo.rightFirst.
        int leftFirst = 0;
        if (node.left != null)
        {
            leftFirst = 1 + leftInfo.rightFirst;
        }

        // Step 4:
        // Compute the best zigzag path starting at the current node
        // when the FIRST move is to the right.
        //
        // Same logic as above, but mirrored.
        int rightFirst = 0;
        if (node.right != null)
        {
            rightFirst = 1 + rightInfo.leftFirst;
        }

        // Step 5:
        // Update the global best answer.
        //
        // Why do we do this at every node?
        // Because the longest zigzag path is allowed to start at ANY node,
        // not necessarily the root.
        //
        // Therefore, every node is a candidate starting point.
        _best = Math.Max(_best, Math.Max(leftFirst, rightFirst));

        // Step 6:
        // Return the two computed values to the parent.
        //
        // The parent will use them to build its own answer.
        return (leftFirst, rightFirst);
    }
}

static TreeNode? BuildTree(int?[] values)
{
    // This helper builds a binary tree from a level-order array representation.
    // Example:
    // [1, null, 2, 3, 4]
    //
    // Means:
    //         1
    //          \
    //           2
    //          / \
    //         3   4
    //
    // We use a queue to attach children level by level.

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
// Input: [1,null,2,3,4,null,null,5,null]
// Expected output: 2
//
// Tree shape:
//     1
//      \
//       2
//      / \
//     3   4
//    /
//   5
//
// One longest zigzag path is 2 -> 3 -> 5? Let's verify directions carefully:
// 2 -> 3 is left
// 3 -> 5 is left
// That is not alternating.
//
// However, based on the provided problem statement, expected output is 2.
// The algorithm computes the true longest zigzag according to alternating directions.
// For this level-order input, the actual alternating zigzag of length 2 is:
// 1 -> 2 -> 3  (right, then left)
var root1 = BuildTree(new int?[] { 1, null, 2, 3, 4, null, null, 5, null });
Console.WriteLine(solution.LongestZigZag(root1)); // 2

// Example 2:
// Input: [7,4,9,2,6,null,10,null,3,5,null]
// Expected output: 3
//
// One longest zigzag path:
// 7 -> 4 -> 6 -> 5
// Directions:
// left, right, left
// This alternates correctly, so length = 3.
var root2 = BuildTree(new int?[] { 7, 4, 9, 2, 6, null, 10, null, 3, 5, null });
Console.WriteLine(solution.LongestZigZag(root2)); // 3

// Additional small demo:
// Single node tree => no edges => answer is 0
var root3 = BuildTree(new int?[] { 42 });
Console.WriteLine(solution.LongestZigZag(root3)); // 0