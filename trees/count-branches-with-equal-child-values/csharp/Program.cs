/*
Title: Count Branches With Equal Child Values

Problem Description:
You are given the root of a binary tree representing a simple rule hierarchy. Each node stores an integer value.
A node is called a balanced branch if it has both a left child and a right child, and the values of those two
children are exactly the same. The value of the current node does not matter for this check.

Your task is to return the number of balanced branch nodes in the tree.

This is a structural tree traversal problem. You should inspect every node once and count how many nodes satisfy
the condition. Nodes with only one child or no children are not counted.

Constraints:
- The number of nodes in the tree is in the range [0, 1000].
- Node values are in the range [-1000, 1000].
- The tree is a binary tree.

Example 1:
Input: root = [8,4,4,3,3,null,3]
Output: 2

Explanation:
- The root node has left child value 4 and right child value 4, so it is a balanced branch.
- The node with value 4 on the left has children 3 and 3, so it is also a balanced branch.
- The other nodes do not have two children with equal values.

Example 2:
Input: root = [5,2,7,2,null,7,7]
Output: 1

Explanation:
- The root is not counted because its child values are 2 and 7.
- The left child of the root does not have two children.
- The node with value 7 on the right has two children with values 7 and 7, so exactly one balanced branch exists.
*/

using System;
using System.Collections.Generic;

// Definition for a binary tree node.
// This is the standard structure used in many tree problems:
// - Val stores the integer value at the current node
// - Left points to the left child
// - Right points to the right child
public class TreeNode
{
    public int Val;
    public TreeNode? Left;
    public TreeNode? Right;

    public TreeNode(int val = 0, TreeNode? left = null, TreeNode? right = null)
    {
        Val = val;
        Left = left;
        Right = right;
    }
}

public class Solution
{
    /*
    Time Complexity: O(n)
    - We visit each node exactly once during the depth-first traversal.
    - At each node, we do only constant-time work:
      checking whether left and right children exist and comparing their values.

    Space Complexity: O(h)
    - h is the height of the tree.
    - This space is used by the recursive call stack during DFS.
    - In the worst case of a completely skewed tree, h can be n.
    - In a balanced tree, h is about log n.
    */
    public int CountBalancedBranches(TreeNode? root)
    {
        // We use a local variable to store the running total of balanced branch nodes.
        // Why use a variable outside the helper method?
        // Because every recursive call can contribute to the same final answer,
        // and this shared counter makes the logic easy to follow for beginners.
        int count = 0;

        // Start a depth-first traversal from the root.
        // DFS is a natural fit for trees because:
        // 1. A tree is recursively defined
        // 2. We want to inspect every node
        // 3. At each node, the check is local and simple
        Dfs(root);

        // After the traversal finishes, count contains the total number
        // of nodes whose left and right children both exist and have equal values.
        return count;

        void Dfs(TreeNode? node)
        {
            // Step 1: Handle the base case.
            // If the current node is null, there is nothing to inspect.
            // This happens when we move past a leaf node's children.
            if (node == null)
            {
                return;
            }

            // Step 2: Check whether the current node has BOTH children.
            // This is necessary because the problem only counts nodes that have:
            // - a left child
            // - a right child
            // Nodes with only one child or no children must not be counted.
            if (node.Left != null && node.Right != null)
            {
                // Step 3: Compare the values of the two children.
                // Important detail:
                // We compare node.Left.Val and node.Right.Val,
                // NOT the current node's value.
                // The problem specifically says the current node's own value does not matter.
                if (node.Left.Val == node.Right.Val)
                {
                    // Step 4: If the child values are equal, this node is a balanced branch.
                    // Increase the answer by 1.
                    count++;
                }
            }

            // Step 5: Continue traversing the left subtree.
            // We must do this because balanced branches can appear anywhere in the tree,
            // not just near the root.
            Dfs(node.Left);

            // Step 6: Continue traversing the right subtree for the same reason.
            Dfs(node.Right);
        }
    }
}

// Helper method to build a binary tree from a level-order array representation.
// This makes it easy to create the sample inputs shown in the problem statement.
//
// Representation rules:
// - values[i] is the node value at position i in level order
// - null means there is no node at that position
//
// Example:
// [8,4,4,3,3,null,3]
//
// Tree:
//         8
//       /   \
//      4     4
//     / \     \
//    3   3     3
TreeNode? BuildTree(int?[] values)
{
    // If the input array is empty, the tree is empty.
    if (values.Length == 0)
    {
        return null;
    }

    // If the first value is null, there is no root node.
    if (values[0] == null)
    {
        return null;
    }

    // Create the root node from the first element.
    TreeNode root = new TreeNode(values[0]!.Value);

    // We use a queue for level-order construction.
    // Why a queue?
    // Because the input is given in level order, and a queue naturally processes
    // nodes in the same first-in-first-out order.
    Queue<TreeNode> queue = new Queue<TreeNode>();
    queue.Enqueue(root);

    // Index points to the next value in the array that we have not used yet.
    int index = 1;

    // Continue while there are parent nodes waiting in the queue
    // and there are still values left in the input array.
    while (queue.Count > 0 && index < values.Length)
    {
        // Take the next parent node whose children we want to assign.
        TreeNode current = queue.Dequeue();

        // Try to assign the left child.
        if (index < values.Length)
        {
            int? leftValue = values[index];
            index++;

            // Only create a child node if the array value is not null.
            if (leftValue != null)
            {
                current.Left = new TreeNode(leftValue.Value);

                // Add the new child to the queue so its own children
                // can be assigned later.
                queue.Enqueue(current.Left);
            }
        }

        // Try to assign the right child.
        if (index < values.Length)
        {
            int? rightValue = values[index];
            index++;

            // Only create a child node if the array value is not null.
            if (rightValue != null)
            {
                current.Right = new TreeNode(rightValue.Value);

                // Add the new child to the queue so its own children
                // can be assigned later.
                queue.Enqueue(current.Right);
            }
        }
    }

    return root;
}

// -------------------------
// Demo code
// -------------------------

Solution solution = new Solution();

// Example 1:
// Input: [8,4,4,3,3,null,3]
// Tree structure:
//         8
//       /   \
//      4     4
//     / \     \
//    3   3     3
//
// Balanced branches:
// - Node 8: children are 4 and 4 -> equal -> count
// - Left node 4: children are 3 and 3 -> equal -> count
// - Right node 4: only one child -> do not count
// Total = 2
TreeNode? root1 = BuildTree(new int?[] { 8, 4, 4, 3, 3, null, 3 });
int result1 = solution.CountBalancedBranches(root1);
Console.WriteLine(result1); // Expected: 2

// Example 2:
// Input: [5,2,7,2,null,7,7]
// Tree structure:
//         5
//       /   \
//      2     7
//     /     / \
//    2     7   7
//
// Balanced branches:
// - Node 5: children are 2 and 7 -> not equal
// - Node 2: does not have two children -> do not count
// - Node 7: children are 7 and 7 -> equal -> count
// Total = 1
TreeNode? root2 = BuildTree(new int?[] { 5, 2, 7, 2, null, 7, 7 });
int result2 = solution.CountBalancedBranches(root2);
Console.WriteLine(result2); // Expected: 1

// Additional quick sanity checks for learning:

// Empty tree -> no nodes -> answer is 0
TreeNode? root3 = BuildTree(Array.Empty<int?>());
Console.WriteLine(solution.CountBalancedBranches(root3)); // Expected: 0

// Single node tree -> no children -> answer is 0
TreeNode? root4 = BuildTree(new int?[] { 10 });
Console.WriteLine(solution.CountBalancedBranches(root4)); // Expected: 0

// Root with equal children only
TreeNode? root5 = BuildTree(new int?[] { 1, 9, 9 });
Console.WriteLine(solution.CountBalancedBranches(root5)); // Expected: 1