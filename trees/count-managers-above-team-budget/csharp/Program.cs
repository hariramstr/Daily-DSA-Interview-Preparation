/*
Title: Count Managers Above Team Budget

Problem Description:
You are given the root of a binary tree representing a company budget hierarchy. Each node stores an integer val,
which is the budget assigned to that manager or employee.

For every non-leaf node, define its immediate team budget as the sum of the values of its existing left and right children.
A manager is considered over budget if their own value is strictly greater than the immediate team budget of their direct children.

Your task is to return the number of nodes in the tree that are over budget.

Important details:
- Leaf nodes are NOT counted, because they do not manage any direct reports.
- A missing child contributes 0 to the immediate team budget.
- We only compare a node with the sum of its direct children, not all descendants.

Examples:
1) root = [10,4,3,1,2]
   - Node 10: child sum = 4 + 3 = 7, and 10 > 7, so count it.
   - Node 4: child sum = 1 + 2 = 3, and 4 > 3, so count it.
   - Node 3 is a leaf, so do not count it.
   - Node 1 and Node 2 are leaves, so do not count them.
   Answer = 2

2) root = [5,7,1,null,2]
   - Node 5: child sum = 7 + 1 = 8, and 5 > 8 is false, so do not count it.
   - Node 7: child sum = 0 + 2 = 2, and 7 > 2 is true, so count it.
   - Node 1 and Node 2 are leaves, so do not count them.
   Answer = 1

Approach:
We perform a depth-first traversal of the tree.
At each node:
- If it is a leaf, skip it.
- Otherwise, compute the sum of its existing left and right child values.
- If node.val > childSum, increase the answer.
- Continue traversing both children.

This visits each node exactly once, which is efficient and simple.
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
    - We visit each node exactly one time.

    Space Complexity: O(h)
    - h is the height of the tree due to recursive call stack.
    - In the worst case of a skewed tree, this can become O(n).
    */
    public int CountManagersAboveTeamBudget(TreeNode? root)
    {
        // This variable stores the final answer.
        // We keep it outside the helper function so every recursive call
        // can update the same running total.
        int count = 0;

        // We define a local recursive function to perform DFS traversal.
        // DFS is a natural choice for tree problems because:
        // 1. Trees are recursive structures.
        // 2. We want to inspect every node.
        // 3. At each node, the work is small and independent:
        //    just check its direct children and continue.
        void Dfs(TreeNode? node)
        {
            // Step 1: If the current node is null, there is nothing to process.
            // This happens when we move past a leaf's child.
            // Returning immediately prevents null reference errors.
            if (node == null)
            {
                return;
            }

            // Step 2: Determine whether this node is a leaf.
            // A leaf has no left child and no right child.
            // The problem explicitly says leaf nodes should NOT be counted,
            // because they do not manage any direct reports.
            bool isLeaf = node.left == null && node.right == null;

            // Step 3: Only non-leaf nodes should be checked.
            // If the node has at least one child, it represents a manager
            // with a direct team, so we compute the immediate team budget.
            if (!isLeaf)
            {
                // Step 4: Read the left child's value if it exists.
                // If the left child is missing, it contributes 0.
                // This follows the problem statement exactly.
                int leftValue = node.left?.val ?? 0;

                // Step 5: Read the right child's value if it exists.
                // If the right child is missing, it also contributes 0.
                int rightValue = node.right?.val ?? 0;

                // Step 6: Compute the immediate team budget.
                // Important: this is ONLY the sum of direct children,
                // not grandchildren or deeper descendants.
                int childSum = leftValue + rightValue;

                // Step 7: Compare the manager's own value against the
                // immediate team budget.
                // If the manager's value is strictly greater, this node
                // is considered "over budget" according to the problem.
                if (node.val > childSum)
                {
                    // Step 8: Increase the answer because we found one
                    // qualifying non-leaf node.
                    count++;
                }
            }

            // Step 9: Continue DFS traversal into the left subtree.
            // We do this so every node in the tree gets checked exactly once.
            Dfs(node.left);

            // Step 10: Continue DFS traversal into the right subtree.
            // Together with the left traversal, this guarantees full coverage
            // of the entire binary tree.
            Dfs(node.right);
        }

        // Step 11: Start the traversal from the root of the tree.
        Dfs(root);

        // Step 12: After traversal is complete, return the total number
        // of managers whose value is greater than the sum of their direct children.
        return count;
    }
}

static TreeNode? BuildTree(int?[] values)
{
    // This helper builds a binary tree from a level-order array representation.
    // Example: [10,4,3,1,2] means:
    //         10
    //        /  \
    //       4    3
    //      / \
    //     1   2
    //
    // null means the node is missing.

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

// Demo 1
// Tree: [10,4,3,1,2]
// Expected output: 2
TreeNode? root1 = BuildTree(new int?[] { 10, 4, 3, 1, 2 });
int result1 = solution.CountManagersAboveTeamBudget(root1);
Console.WriteLine(result1);

// Demo 2
// Tree: [5,7,1,null,2]
// Expected output: 1
TreeNode? root2 = BuildTree(new int?[] { 5, 7, 1, null, 2 });
int result2 = solution.CountManagersAboveTeamBudget(root2);
Console.WriteLine(result2);

// Additional demo
// Single node tree: [8]
// Leaf nodes are not counted, so expected output: 0
TreeNode? root3 = BuildTree(new int?[] { 8 });
int result3 = solution.CountManagersAboveTeamBudget(root3);
Console.WriteLine(result3);