/*
Title: Find the First Shelf With Missing Books

Problem Description:
You are given the root of a binary tree representing shelves in a library. Each node stores an integer shelf ID.
A shelf is considered complete if it has either 0 children or exactly 2 children.
A shelf is considered missing books if it has exactly one child, meaning one side of the shelf structure exists
and the other side is empty.

Return the shelf ID of the first shelf with missing books when the tree is scanned level by level from top to bottom,
and from left to right within each level. If no such shelf exists, return -1.

In other words, you must perform a breadth-first search and find the earliest node that has only one child.

Constraints:
- The number of nodes in the tree is in the range [0, 1000].
- Node values are in the range [1, 100000].
- All shelf IDs are unique.
- The input tree is a binary tree and may be empty.

Example 1:
Input: root = [10,5,20,3,null,15,30]
Output: 5

Explanation:
Shelf 5 has a left child (3) but no right child. During level-order traversal, it is the first node
with exactly one child.

Example 2:
Input: root = [8,4,12,2,6,10,14]
Output: -1

Explanation:
Every non-leaf shelf has either two children or none, so there is no shelf with exactly one child.
*/

using System;
using System.Collections.Generic;

// Definition for a binary tree node.
// This is the standard structure used in many tree problems.
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
    - We may visit every node in the tree once.
    - Each node is added to and removed from the queue at most one time.

    Space Complexity: O(n)
    - In the worst case, the queue used for breadth-first search can hold many nodes from one level.
    - This can be up to O(n) in a wide tree.
    */
    public int FindFirstShelfWithMissingBooks(TreeNode? root)
    {
        // Step 1:
        // If the tree is empty, there are no shelves at all.
        // That means there cannot be any shelf with exactly one child.
        // So we immediately return -1.
        if (root == null)
        {
            return -1;
        }

        // Step 2:
        // We use a Queue<TreeNode> because the problem specifically asks us to scan
        // the tree level by level, from left to right.
        //
        // A queue is the perfect data structure for Breadth-First Search (BFS):
        // - We add nodes to the back of the queue.
        // - We remove nodes from the front of the queue.
        // This naturally processes nodes in the exact order required by the problem.
        Queue<TreeNode> queue = new Queue<TreeNode>();

        // Step 3:
        // Start the BFS by placing the root node into the queue.
        // The root is the first node we must inspect.
        queue.Enqueue(root);

        // Step 4:
        // Continue processing until there are no more nodes left to inspect.
        while (queue.Count > 0)
        {
            // Step 4a:
            // Remove the next node from the front of the queue.
            // Because this is BFS, this node is the next one in level-order traversal.
            TreeNode current = queue.Dequeue();

            // Step 4b:
            // Determine whether the current node has a left child and/or a right child.
            // We store these checks in variables to make the code easier to read
            // and easier for beginners to follow.
            bool hasLeftChild = current.left != null;
            bool hasRightChild = current.right != null;

            // Step 4c:
            // A node has "missing books" if it has exactly one child.
            //
            // There are only two ways that can happen:
            // - left child exists, right child does not
            // - right child exists, left child does not
            //
            // The XOR-like condition below checks exactly that:
            // one is true and the other is false.
            //
            // This is the key condition of the problem.
            if (hasLeftChild != hasRightChild)
            {
                // Because BFS visits nodes in top-to-bottom, left-to-right order,
                // the first node we find with exactly one child is the correct answer.
                return current.val;
            }

            // Step 4d:
            // If the current node has a left child, add it to the queue.
            // We do this so it will be processed later in the correct BFS order.
            if (current.left != null)
            {
                queue.Enqueue(current.left);
            }

            // Step 4e:
            // If the current node has a right child, add it to the queue.
            // We add left first and right second so that nodes on the same level
            // are processed from left to right, exactly as required.
            if (current.right != null)
            {
                queue.Enqueue(current.right);
            }
        }

        // Step 5:
        // If we finish the entire BFS without finding any node with exactly one child,
        // then every node had either:
        // - 0 children, or
        // - 2 children
        //
        // Therefore, no shelf with missing books exists.
        return -1;
    }
}

// Demo code starts here.
// The following examples are built manually to match the problem statement exactly.

Solution solution = new Solution();

// Example 1:
// Input: [10,5,20,3,null,15,30]
// Tree structure:
//         10
//        /  \
//       5    20
//      /    /  \
//     3    15  30
//
// Node 5 has exactly one child (left child 3, no right child).
// BFS order: 10, 5, 20, 3, 15, 30
// First node with exactly one child is 5.
// Expected output: 5
TreeNode example1 =
    new TreeNode(10,
        new TreeNode(5,
            new TreeNode(3),
            null),
        new TreeNode(20,
            new TreeNode(15),
            new TreeNode(30)));

int result1 = solution.FindFirstShelfWithMissingBooks(example1);
Console.WriteLine(result1);

// Example 2:
// Input: [8,4,12,2,6,10,14]
// Tree structure:
//         8
//       /   \
//      4     12
//     / \   /  \
//    2   6 10  14
//
// Every non-leaf node has exactly two children.
// No node has exactly one child.
// Expected output: -1
TreeNode example2 =
    new TreeNode(8,
        new TreeNode(4,
            new TreeNode(2),
            new TreeNode(6)),
        new TreeNode(12,
            new TreeNode(10),
            new TreeNode(14)));

int result2 = solution.FindFirstShelfWithMissingBooks(example2);
Console.WriteLine(result2);

// Additional demo 1:
// Empty tree
// Expected output: -1
TreeNode? example3 = null;
int result3 = solution.FindFirstShelfWithMissingBooks(example3);
Console.WriteLine(result3);

// Additional demo 2:
// Single node tree
// Tree: [42]
// The root has 0 children, so it is complete.
// Expected output: -1
TreeNode example4 = new TreeNode(42);
int result4 = solution.FindFirstShelfWithMissingBooks(example4);
Console.WriteLine(result4);

// Additional demo 3:
// Tree:
//      1
//       \
//        2
//
// Root has exactly one child, so it should be returned immediately.
// Expected output: 1
TreeNode example5 =
    new TreeNode(1,
        null,
        new TreeNode(2));

int result5 = solution.FindFirstShelfWithMissingBooks(example5);
Console.WriteLine(result5);