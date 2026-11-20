/*
Title: Longest Consecutive Approval Chain in an Org Tree
Difficulty: Medium
Topic: Trees

Problem Description:
A company stores its approval hierarchy as a binary tree. Each node represents a manager and contains an integer approval level.
A valid approval chain is any path that moves only between parent and child nodes, where every next node in the path has
an approval level exactly 1 greater than the previous node. The path must follow tree edges, but it does not need to start
at the root, and it may end at any node. You may only move downward from parent to child while building the chain.

Your task is to return the length of the longest valid approval chain in the tree.

For example, if a manager with level 4 has a child with level 5, and that child has a child with level 6, then these three
nodes form a valid chain of length 3. If a child differs by more than 1, less than 1, or is equal, the chain is broken and
a new chain may start from that child.

Implement a function that, given the root of the binary tree, computes the maximum chain length.

Constraints:
- The number of nodes in the tree is in the range [0, 100000].
- Node values are in the range [-1000000000, 1000000000].
- The tree is not necessarily balanced.
- An empty tree should return 0.

Example 1:
Input: root = [3,4,2,5,null,null,3,6]
Output: 4
Explanation: The longest downward consecutive chain is 3 -> 4 -> 5 -> 6, so the answer is 4.

Example 2:
Input: root = [10,11,9,12,10,null,null,13]
Output: 4
Explanation: The longest valid chain is 10 -> 11 -> 12 -> 13. The node with value 10 under 11 breaks the chain because it is not 12.
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
    Time Complexity: O(n)
    - We visit each node exactly once.
    - At each node, we do only constant-time work.

    Space Complexity: O(h) on the explicit stack used by the iterative DFS,
    where h is the height of the tree in the best balanced interpretation,
    but in the worst case of a completely skewed tree it can become O(n).

    Why iterative instead of recursive?
    - The tree can contain up to 100000 nodes.
    - A recursive DFS on a very deep tree could cause a stack overflow.
    - Using our own stack is safer for large inputs and still keeps the logic simple.
    */
    public int LongestConsecutive(TreeNode? root)
    {
        // If the tree is empty, there are no nodes and therefore no valid chain.
        // The problem statement explicitly says an empty tree should return 0.
        if (root == null)
        {
            return 0;
        }

        // This variable stores the best answer found anywhere in the tree.
        // We start at 0, and as we process nodes we will update it.
        int maxLength = 0;

        // We use an explicit stack for depth-first traversal.
        // Each stack entry stores:
        // 1. The current node we are visiting
        // 2. The length of the valid consecutive chain that ends at this node
        //
        // Why store the chain length together with the node?
        // Because the chain length depends on the relationship between a node and its parent.
        // Once we compute the chain length for the current node, we can pass the correct value
        // down to its children.
        var stack = new Stack<(TreeNode node, int currentChainLength)>();

        // The root always starts a chain of length 1 by itself.
        // Even if no child continues the sequence, a single node is still a valid chain.
        stack.Push((root, 1));

        // Standard iterative DFS:
        // Keep processing until there are no more nodes left to visit.
        while (stack.Count > 0)
        {
            // Remove the next node to process from the stack.
            var (node, currentChainLength) = stack.Pop();

            // Update the global best answer.
            // This is necessary because the longest chain may end at any node,
            // not necessarily at a leaf.
            if (currentChainLength > maxLength)
            {
                maxLength = currentChainLength;
            }

            // -------------------------
            // Process the left child
            // -------------------------
            if (node.left != null)
            {
                // If the left child's value is exactly 1 greater than the current node's value,
                // then the consecutive chain continues.
                //
                // Example:
                // current node = 4, left child = 5
                // then a chain ending at 4 of length L becomes a chain ending at 5 of length L + 1.
                if ((long)node.left.val == (long)node.val + 1)
                {
                    stack.Push((node.left, currentChainLength + 1));
                }
                else
                {
                    // Otherwise, the chain is broken.
                    //
                    // This child can still be the start of a brand-new chain of length 1.
                    // We do not discard it, because the longest chain in the whole tree might
                    // begin here rather than above.
                    stack.Push((node.left, 1));
                }
            }

            // -------------------------
            // Process the right child
            // -------------------------
            if (node.right != null)
            {
                // Same logic as for the left child:
                // continue the chain only if child value == parent value + 1.
                if ((long)node.right.val == (long)node.val + 1)
                {
                    stack.Push((node.right, currentChainLength + 1));
                }
                else
                {
                    // If the consecutive rule fails, start a new chain from this child.
                    stack.Push((node.right, 1));
                }
            }
        }

        // After visiting every node, maxLength contains the longest valid downward
        // parent-to-child consecutive chain in the tree.
        return maxLength;
    }
}

static TreeNode? BuildTree(int?[] values)
{
    // This helper builds a binary tree from a level-order array representation.
    //
    // Example:
    // [3,4,2,5,null,null,3,6]
    //
    // Means:
    //         3
    //       /   \
    //      4     2
    //     /     / \
    //    5    null 3
    //   /
    //  6
    //
    // This is useful for demo/testing because the examples in the problem statement
    // are given in this common array format.

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
// Input: [3,4,2,5,null,null,3,6]
// Longest valid chain: 3 -> 4 -> 5 -> 6
// Expected output: 4
var root1 = BuildTree(new int?[] { 3, 4, 2, 5, null, null, 3, 6 });
var result1 = solution.LongestConsecutive(root1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2:
// Input: [10,11,9,12,10,null,null,13]
// Longest valid chain: 10 -> 11 -> 12 -> 13
// Expected output: 4
var root2 = BuildTree(new int?[] { 10, 11, 9, 12, 10, null, null, 13 });
var result2 = solution.LongestConsecutive(root2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional demo: empty tree
// Expected output: 0
TreeNode? root3 = null;
var result3 = solution.LongestConsecutive(root3);
Console.WriteLine($"Empty Tree Result: {result3}");

// Additional demo: chain breaks and restarts
// Tree: [5,6,7,7,null,8]
// Paths:
// 5 -> 6 is valid
// 6 -> 7 (left child) is valid, so chain length 3 on left side
// 5 -> 7 (right child) is not valid, so that branch restarts
// right child 7 -> 8 is valid, chain length 2 on that side
// Expected longest = 3
var root4 = BuildTree(new int?[] { 5, 6, 7, 7, null, 8 });
var result4 = solution.LongestConsecutive(root4);
Console.WriteLine($"Additional Demo Result: {result4}");