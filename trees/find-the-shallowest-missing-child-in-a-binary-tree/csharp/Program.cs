/*
Title: Find the Shallowest Missing Child in a Binary Tree

Problem Description:
You are given the root of a binary tree representing a hierarchy of checkpoints.
A checkpoint is considered incomplete if it is missing at least one direct child:
its left child, its right child, or both.

Your task is to return the value of the incomplete checkpoint that appears at the
smallest depth from the root. If multiple incomplete checkpoints exist at the same
depth, return the one that appears first from left to right.

In other words, scan the tree level by level. The first node you encounter that
does not have two children is the answer. If the tree is empty, return -1.

This problem is designed to test basic tree traversal skills and understanding of
level-order processing. A breadth-first search works naturally because it visits
nodes in increasing depth order and preserves left-to-right order within each level.

Constraints:
- The number of nodes is in the range [0, 1000]
- Node values are integers in the range [-10000, 10000]
- All node values are unique

Examples:
1) root = [7,4,9,2,6,null,11]
   Output: 9
   Explanation:
   - Node 7 has both children
   - Node 4 has both children
   - Node 9 is missing a left child
   - Therefore, 9 is the first incomplete node in level-order traversal

2) root = [5,3,8,1,null,7,10]
   Output: 3
   Explanation:
   - At depth 1, node 3 is missing its right child
   - Node 8 has two children
   - Since node 3 appears first from left to right, the answer is 3
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
    - In the worst case, we may visit each node once.

    Space Complexity: O(n)
    - In the worst case, the queue used for breadth-first search can hold many nodes,
      up to an entire level of the tree.
    */
    public int FindShallowestMissingChild(TreeNode? root)
    {
        // Step 1:
        // First, handle the simplest possible case: the tree is empty.
        // If there is no root node, then there are no checkpoints at all.
        // The problem explicitly tells us to return -1 in this situation.
        if (root == null)
        {
            return -1;
        }

        // Step 2:
        // We use a Queue<TreeNode> because we want to perform a breadth-first search (BFS).
        // BFS is the correct traversal strategy here because:
        // - It visits nodes level by level
        // - Within the same level, it visits from left to right
        //
        // That exactly matches the problem statement:
        // "Return the first node encountered in level-order traversal that is missing
        // at least one child."
        Queue<TreeNode> queue = new Queue<TreeNode>();

        // Step 3:
        // Start the BFS by placing the root node into the queue.
        // This means the root will be processed first.
        queue.Enqueue(root);

        // Step 4:
        // Continue processing until there are no more nodes left to examine.
        while (queue.Count > 0)
        {
            // Step 4a:
            // Remove the node at the front of the queue.
            // Because this is a queue (FIFO: First-In, First-Out),
            // nodes come out in the same order they were added.
            // That preserves the correct level-order traversal.
            TreeNode current = queue.Dequeue();

            // Step 4b:
            // Check whether the current node is incomplete.
            // A node is incomplete if:
            // - left child is missing, OR
            // - right child is missing
            //
            // In code, that means:
            // current.left == null || current.right == null
            //
            // Why do we check this immediately when we dequeue the node?
            // Because BFS guarantees this is the shallowest node seen so far,
            // and among nodes at the same depth, it is the leftmost unprocessed one.
            // Therefore, the first incomplete node we find is the correct answer.
            if (current.left == null || current.right == null)
            {
                return current.val;
            }

            // Step 4c:
            // If we reach this point, the current node has BOTH children.
            // Since it is complete, it is not the answer.
            //
            // We now add its children to the queue so they can be checked later.
            //
            // Important:
            // We enqueue the left child first, then the right child.
            // This preserves left-to-right order within each level.
            queue.Enqueue(current.left);
            queue.Enqueue(current.right);
        }

        // Step 5:
        // If we finish the entire BFS without finding any incomplete node,
        // then every node in the tree had two children.
        //
        // In a finite binary tree, this can only happen for an empty tree,
        // because leaf nodes normally have zero children and are therefore incomplete.
        // Still, returning -1 here is a safe fallback.
        return -1;
    }
}

static TreeNode? BuildTree(int?[] values)
{
    // This helper builds a binary tree from a level-order array representation.
    // Example: [7,4,9,2,6,null,11]
    //
    // The meaning is:
    // - values[0] is the root
    // - For a node at index i:
    //   - left child is at index 2*i + 1
    //   - right child is at index 2*i + 2
    //
    // Null means "no node exists here".

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

// Demo code
Solution solution = new Solution();

// Example 1:
// Input: [7,4,9,2,6,null,11]
// Expected output: 9
TreeNode? root1 = BuildTree(new int?[] { 7, 4, 9, 2, 6, null, 11 });
int result1 = solution.FindShallowestMissingChild(root1);
Console.WriteLine(result1);

// Example 2:
// Input: [5,3,8,1,null,7,10]
// Expected output: 3
TreeNode? root2 = BuildTree(new int?[] { 5, 3, 8, 1, null, 7, 10 });
int result2 = solution.FindShallowestMissingChild(root2);
Console.WriteLine(result2);

// Extra demo: empty tree
// Expected output: -1
TreeNode? root3 = BuildTree(Array.Empty<int?>());
int result3 = solution.FindShallowestMissingChild(root3);
Console.WriteLine(result3);

// Extra demo: single node tree
// The root has no children, so it is incomplete.
// Expected output: 42
TreeNode? root4 = BuildTree(new int?[] { 42 });
int result4 = solution.FindShallowestMissingChild(root4);
Console.WriteLine(result4);