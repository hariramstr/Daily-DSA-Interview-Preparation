/*
Title: Maximum Delay Between Ancestor and Descendant Checkpoints

Problem Description:
You are given the root of a tree representing checkpoint events in a monitoring system.
Each node stores an integer timestamp.

A checkpoint A is considered an ancestor of checkpoint B if A lies on the path from the root to B,
and A and B may be the same node.

The delay between an ancestor and a descendant is defined as:
    descendant.timestamp - ancestor.timestamp

Your task is to return the maximum possible delay over all valid ancestor-descendant pairs in the tree.

Important note:
This is NOT the same as finding the maximum difference between any two nodes anywhere in the tree.
The earlier checkpoint must be an ancestor of the later checkpoint.

Because timestamps may go up or down as you move down the tree, the best answer can be found by
tracking the smallest timestamp seen so far on each root-to-node path.

Constraints:
- Number of nodes: [1, 100000]
- Node values: [-1000000000, 1000000000]
- The tree is not necessarily balanced
- Since a node can be paired with itself, the minimum possible answer is 0

Example 1:
Input:  root = [8,3,10,1,6,null,14,null,null,4,7,13]
Output: 11
Explanation:
The best pair is ancestor 3 and descendant 14, giving 14 - 3 = 11.
Even though 1 and 14 differ by 13, they are not in an ancestor-descendant relationship.

Example 2:
Input:  root = [5,9,2,12,null,-1,8]
Output: 7
Explanation:
One optimal pair is ancestor 5 and descendant 12, so the maximum delay is 12 - 5 = 7.
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

    Space Complexity: O(h)
    - h is the height of the tree due to recursion stack.
    - In the worst case of a completely skewed tree, h can be O(n).
    */
    public int MaxAncestorDelay(TreeNode? root)
    {
        // The problem guarantees at least one node, but handling null makes the method safer.
        // If the tree is empty, there are no pairs, so we return 0.
        if (root == null)
        {
            return 0;
        }

        // We start a depth-first traversal from the root.
        // At the root, the minimum value seen so far on the current path is simply root.val,
        // because the path currently contains only the root itself.
        return Dfs(root, root.val);
    }

    private int Dfs(TreeNode node, int minSoFar)
    {
        // STEP 1:
        // Compute the best delay that uses the CURRENT node as the descendant.
        //
        // Why this works:
        // For any descendant node, the best ancestor to pair with it is the smallest value
        // seen on the path from the root to this node.
        //
        // So if:
        //   minSoFar = smallest ancestor value on the current path
        //   node.val  = current descendant value
        //
        // then the best delay ending at this node is:
        //   node.val - minSoFar
        //
        // This automatically respects the ancestor-descendant rule because minSoFar only comes
        // from nodes on the current root-to-node path.
        int bestUsingCurrentNode = node.val - minSoFar;

        // STEP 2:
        // Update the minimum value seen so far before going deeper.
        //
        // Why this is necessary:
        // Any child of the current node can use the current node as an ancestor.
        // Therefore, when we continue downward, the path minimum should include node.val too.
        //
        // Example:
        // If minSoFar was 5 and current node value is 2, then for all descendants below this node,
        // the smallest ancestor value is now 2.
        int newMinSoFar = Math.Min(minSoFar, node.val);

        // STEP 3:
        // Recursively solve the left subtree.
        //
        // We pass newMinSoFar because every node in the left subtree has all nodes on the current
        // path as ancestors, including the current node.
        //
        // If there is no left child, we use 0 because:
        // - A missing subtree contributes no valid pair
        // - The answer can never be negative since a node can pair with itself, producing 0
        int leftBest = node.left != null ? Dfs(node.left, newMinSoFar) : 0;

        // STEP 4:
        // Recursively solve the right subtree using the same logic as the left subtree.
        int rightBest = node.right != null ? Dfs(node.right, newMinSoFar) : 0;

        // STEP 5:
        // The best answer in the subtree rooted at this node is the maximum of:
        // 1. The best delay ending exactly at this node
        // 2. The best delay found somewhere in the left subtree
        // 3. The best delay found somewhere in the right subtree
        //
        // Why taking the maximum is correct:
        // Every valid ancestor-descendant pair in this subtree must fall into one of those cases.
        return Math.Max(bestUsingCurrentNode, Math.Max(leftBest, rightBest));
    }
}

static TreeNode? BuildTree(params int?[] values)
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

// Example 1:
// Tree: [8,3,10,1,6,null,14,null,null,4,7,13]
// Expected: 11
TreeNode? root1 = BuildTree(8, 3, 10, 1, 6, null, 14, null, null, 4, 7, 13);
int result1 = solution.MaxAncestorDelay(root1);
Console.WriteLine(result1);

// Example 2:
// Tree: [5,9,2,12,null,-1,8]
// Expected: 7
TreeNode? root2 = BuildTree(5, 9, 2, 12, null, -1, 8);
int result2 = solution.MaxAncestorDelay(root2);
Console.WriteLine(result2);

// Additional small sanity check:
// Single node tree: best pair is the node with itself, so answer is 0
TreeNode? root3 = BuildTree(42);
int result3 = solution.MaxAncestorDelay(root3);
Console.WriteLine(result3);