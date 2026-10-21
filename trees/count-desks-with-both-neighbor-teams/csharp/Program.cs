/*
Title: Count Desks with Both Neighbor Teams
Difficulty: Easy
Topic: Trees

Problem Description:
A company stores its office layout as a binary tree. Each node represents a desk and contains an integer team ID.
The left and right child pointers represent the two neighboring desks directly connected to that desk in the layout map.

A desk is called a mixed-neighbor desk if:
1. It has both a left child and a right child
2. The current desk's team ID is different from the left child's team ID
3. The current desk's team ID is different from the right child's team ID

So for a node with value x, left child value a, and right child value b,
we count it only when:
- left child exists
- right child exists
- x != a
- x != b

If the tree is empty, the answer is 0.

Important note about Example 1:
The original statement contains a contradiction and then corrects itself.
For input [5,3,8,3,4,8,9], the correct output is 1, because only the root node 5 qualifies.

We will implement a correct tree traversal that counts exactly those nodes
that have two children and differ from both child values.
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
    - We visit each node exactly once.

    Space Complexity: O(h)
    - h is the height of the tree due to recursion stack.
    - In the worst case of a skewed tree, h can be n.
    - In a balanced tree, h is about log n.
    */
    public int CountMixedNeighborDesks(TreeNode? root)
    {
        // We use a depth-first traversal because:
        // 1. It is simple to write recursively for trees.
        // 2. We need to inspect every node exactly once.
        // 3. At each node, the decision depends only on the node and its immediate children.
        return Dfs(root);
    }

    private int Dfs(TreeNode? node)
    {
        // Step 1: Handle the base case.
        // If the current node is null, there is no desk here,
        // so it cannot contribute anything to the count.
        // Returning 0 allows recursion to safely stop at empty branches.
        if (node == null)
        {
            return 0;
        }

        // Step 2: Start with the assumption that the current node does not qualify.
        // We will change this to 1 only if all required conditions are satisfied.
        int currentCount = 0;

        // Step 3: Check whether the current node has BOTH children.
        // This is necessary because the problem explicitly says a desk is counted
        // only if it has exactly two neighboring desks represented by left and right children.
        if (node.left != null && node.right != null)
        {
            // Step 4: Compare the current node's value with each child's value.
            // The node qualifies only when:
            // - node.val != node.left.val
            // - node.val != node.right.val
            //
            // Notice that the two children are allowed to be equal to each other.
            // The problem only requires the parent to be different from both children.
            if (node.val != node.left.val && node.val != node.right.val)
            {
                // Step 5: Mark this node as a qualifying mixed-neighbor desk.
                currentCount = 1;
            }
        }

        // Step 6: Recursively count qualifying nodes in the left subtree.
        // This is necessary because any descendant node may also satisfy the condition.
        int leftCount = Dfs(node.left);

        // Step 7: Recursively count qualifying nodes in the right subtree.
        // Again, we must inspect the entire tree to ensure no qualifying node is missed.
        int rightCount = Dfs(node.right);

        // Step 8: Combine results.
        // The total count for this subtree is:
        // - 1 if the current node qualifies, otherwise 0
        // - plus all qualifying nodes in the left subtree
        // - plus all qualifying nodes in the right subtree
        return currentCount + leftCount + rightCount;
    }
}

// -------------------------
// Demo code starts here
// -------------------------

var solution = new Solution();

// Example 1:
// Input: [5,3,8,3,4,8,9]
// Tree structure:
//         5
//       /   \
//      3     8
//     / \   / \
//    3   4 8   9
//
// Check each internal node:
// - 5 has children 3 and 8; 5 != 3 and 5 != 8 => qualifies
// - left 3 has children 3 and 4; left child matches parent => does not qualify
// - right 8 has children 8 and 9; left child matches parent => does not qualify
//
// Correct output: 1
var root1 = new TreeNode(
    5,
    new TreeNode(
        3,
        new TreeNode(3),
        new TreeNode(4)
    ),
    new TreeNode(
        8,
        new TreeNode(8),
        new TreeNode(9)
    )
);

int result1 = solution.CountMixedNeighborDesks(root1);
Console.WriteLine(result1); // Expected: 1

// Example 2:
// Input: [7,2,9,1,2,null,10]
// Tree structure:
//         7
//       /   \
//      2     9
//     / \     \
//    1   2     10
//
// Check each internal node:
// - 7 has children 2 and 9; both differ from 7 => qualifies
// - 2 has children 1 and 2; right child matches parent => does not qualify
// - 9 has only one child => does not qualify
//
// Correct output: 1
var root2 = new TreeNode(
    7,
    new TreeNode(
        2,
        new TreeNode(1),
        new TreeNode(2)
    ),
    new TreeNode(
        9,
        null,
        new TreeNode(10)
    )
);

int result2 = solution.CountMixedNeighborDesks(root2);
Console.WriteLine(result2); // Expected: 1

// Additional demo: empty tree
TreeNode? root3 = null;
int result3 = solution.CountMixedNeighborDesks(root3);
Console.WriteLine(result3); // Expected: 0

// Additional demo: one qualifying internal node below the root
//         4
//       /   \
//      4     6
//           / \
//          1   2
//
// - Root 4 does not qualify because left child is also 4
// - Node 6 qualifies because 6 != 1 and 6 != 2
// Expected: 1
var root4 = new TreeNode(
    4,
    new TreeNode(4),
    new TreeNode(
        6,
        new TreeNode(1),
        new TreeNode(2)
    )
);

int result4 = solution.CountMixedNeighborDesks(root4);
Console.WriteLine(result4); // Expected: 1