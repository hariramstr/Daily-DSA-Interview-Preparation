/*
Title: Find the First Unbalanced Review Folder
Difficulty: Easy
Topic: Trees

Problem Description:
You are given the root of a binary tree representing a document review folder structure.
Each node stores an integer number of pending files in that folder.

A folder is considered balanced if the total number of pending files in its left subtree
is exactly equal to the total number of pending files in its right subtree.
If a child does not exist, its subtree sum is 0.

Your task is to return the value stored in the first unbalanced folder found during a
level-order traversal (top to bottom, left to right). If every folder is balanced, return -1.

The value of a folder is not guaranteed to be unique, so you should return the node's
stored integer value exactly as it appears when the first unbalanced node is encountered
in level order.

Input uses the usual array representation of a binary tree in level order, where null
means the child is missing.

Example 1:
Input: root = [8,4,4,2,2,2,2]
Output: -1

Example 2:
Input: root = [10,3,5,1,null,2,1]
Output: 10
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
    // Time Complexity: O(n)
    // - We visit each node once to compute subtree sums.
    // - We visit each node once again in breadth-first order to find the first unbalanced node.
    //
    // Space Complexity: O(n)
    // - We store one subtree sum per node in a dictionary.
    // - The recursion stack can grow up to O(h), where h is the tree height.
    // - The queue for breadth-first traversal can hold up to O(n) nodes in the worst case.
    public int FindFirstUnbalancedFolder(TreeNode? root)
    {
        // If the tree is somehow empty, there is no folder to inspect.
        // The problem says there is at least 1 node, but handling null makes the method safer.
        if (root == null)
        {
            return -1;
        }

        // We need subtree sums for every node before we can correctly decide whether a node is balanced.
        // Why?
        // Because a node is balanced based on:
        //   sum(left subtree) == sum(right subtree)
        //
        // A very convenient approach is:
        // 1. Do a post-order DFS (left, right, node) so children are processed before parent.
        // 2. Store each node's total subtree sum in a dictionary.
        //
        // We use the node reference itself as the dictionary key.
        // This is important because values are not unique, so we cannot safely key by node.val.
        var subtreeSum = new Dictionary<TreeNode, int>();

        ComputeSubtreeSum(root, subtreeSum);

        // Now that every node's subtree sum is known, we can perform a standard level-order traversal.
        // Level-order traversal uses a queue and visits nodes:
        // top to bottom, left to right
        //
        // This exactly matches the order required by the problem statement.
        var queue = new Queue<TreeNode>();
        queue.Enqueue(root);

        while (queue.Count > 0)
        {
            // Remove the next node in level-order.
            TreeNode current = queue.Dequeue();

            // If a child is missing, its subtree sum is defined as 0.
            // Otherwise, we look up the precomputed subtree sum from the dictionary.
            int leftSum = current.left == null ? 0 : subtreeSum[current.left];
            int rightSum = current.right == null ? 0 : subtreeSum[current.right];

            // This is the exact balance condition from the problem:
            // balanced <=> left subtree sum == right subtree sum
            //
            // The moment we find the first node that does NOT satisfy this,
            // we return its stored value immediately.
            // Because we are traversing in level-order, this is guaranteed to be
            // the first unbalanced folder in the required order.
            if (leftSum != rightSum)
            {
                return current.val;
            }

            // Continue the breadth-first traversal by adding children from left to right.
            // Left child is enqueued first so nodes on the same level are processed left to right.
            if (current.left != null)
            {
                queue.Enqueue(current.left);
            }

            if (current.right != null)
            {
                queue.Enqueue(current.right);
            }
        }

        // If we finish the entire level-order traversal without finding any unbalanced node,
        // then every folder is balanced.
        return -1;
    }

    private int ComputeSubtreeSum(TreeNode? node, Dictionary<TreeNode, int> subtreeSum)
    {
        // Base case:
        // An empty subtree contributes 0 to the sum.
        // This matches the problem statement for missing children.
        if (node == null)
        {
            return 0;
        }

        // Step 1: Recursively compute the sum of the left subtree.
        // We do this first because the current node's total subtree sum depends on its children.
        int left = ComputeSubtreeSum(node.left, subtreeSum);

        // Step 2: Recursively compute the sum of the right subtree.
        int right = ComputeSubtreeSum(node.right, subtreeSum);

        // Step 3: Compute the total subtree sum rooted at the current node.
        // This includes:
        // - the current node's own value
        // - everything in the left subtree
        // - everything in the right subtree
        int total = node.val + left + right;

        // Step 4: Store the result so it can be reused later in O(1) time.
        // This avoids recomputing subtree sums during the breadth-first traversal.
        subtreeSum[node] = total;

        // Step 5: Return the total so the parent call can use it.
        return total;
    }
}

static TreeNode? BuildTree(int?[] values)
{
    // This helper builds a binary tree from the usual level-order array representation.
    // Example:
    // [10, 3, 5, 1, null, 2, 1]
    //
    // Means:
    //         10
    //        /  \
    //       3    5
    //      /    / \
    //     1    2   1
    //
    // We use a queue to attach children level by level.
    if (values.Length == 0 || values[0] == null)
    {
        return null;
    }

    TreeNode root = new TreeNode(values[0]!.Value);
    var queue = new Queue<TreeNode>();
    queue.Enqueue(root);

    int index = 1;

    while (queue.Count > 0 && index < values.Length)
    {
        TreeNode current = queue.Dequeue();

        // Attach left child if present.
        if (index < values.Length && values[index] != null)
        {
            current.left = new TreeNode(values[index]!.Value);
            queue.Enqueue(current.left);
        }
        index++;

        // Attach right child if present.
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
// Tree: [8,4,4,2,2,2,2]
// Left subtree sum of root = 4+2+2 = 8
// Right subtree sum of root = 4+2+2 = 8
// Every internal node also has equal left/right subtree sums.
// Expected output: -1
TreeNode? root1 = BuildTree(new int?[] { 8, 4, 4, 2, 2, 2, 2 });
int result1 = solution.FindFirstUnbalancedFolder(root1);
Console.WriteLine(result1);

// Example 2:
// Tree: [10,3,5,1,null,2,1]
// Left subtree sum of root = 3+1 = 4
// Right subtree sum of root = 5+2+1 = 8
// Root is unbalanced, and it is visited first in level-order.
// Expected output: 10
TreeNode? root2 = BuildTree(new int?[] { 10, 3, 5, 1, null, 2, 1 });
int result2 = solution.FindFirstUnbalancedFolder(root2);
Console.WriteLine(result2);

// Additional demo:
// Single node tree: both left and right subtree sums are 0, so it is balanced.
// Expected output: -1
TreeNode? root3 = BuildTree(new int?[] { 7 });
int result3 = solution.FindFirstUnbalancedFolder(root3);
Console.WriteLine(result3);