/*
Title: Count Branches With Missing Siblings
Difficulty: Easy
Topic: Trees

Problem Description:
You are given the root of a binary tree that represents a simple organizational chart.
Each node stores an integer ID, but the ID values do not affect the answer.

Your task is to count how many nodes in the tree have exactly one child.
That means we count a node if:
- the left child exists and the right child does not, OR
- the right child exists and the left child does not.

Important details:
- Leaf nodes do NOT count, because they have zero children.
- Nodes with two children do NOT count.
- Only nodes with exactly one child should be included.

Return the total number of such nodes.

Constraints:
- The number of nodes in the tree is in the range [0, 1000].
- -10^4 <= Node.val <= 10^4
- The tree is a binary tree.

Example 1:
Input: root = [5,3,8,1,null,null,9]
Output: 2
Explanation:
- Node 3 has only one child (1)
- Node 8 has only one child (9)
So the answer is 2.

Example 2:
Input: root = [7,4,10,2,6,null,null]
Output: 0
Explanation:
- Node 4 has two children
- Node 10 has no children
- Nodes 2 and 6 are leaves
So no node has exactly one child.
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
    - We visit each node exactly one time.

    Space Complexity: O(h)
    - This is the extra space used by the recursion call stack.
    - h is the height of the tree.
    - In the worst case of a very skewed tree, h can be n.
    - In a balanced tree, h is about log n.

    Beginner-friendly idea:
    We will use Depth-First Search (DFS).
    For every node:
    1. Check whether it has exactly one child.
    2. Recursively do the same for the left subtree.
    3. Recursively do the same for the right subtree.
    4. Add all counts together.
    */
    public int CountNodesWithExactlyOneChild(TreeNode? root)
    {
        // Step 1:
        // If the current node is null, there is no actual node here.
        // That means there is nothing to count.
        // This is the base case that stops the recursion.
        if (root == null)
        {
            return 0;
        }

        // Step 2:
        // We need to determine whether the current node has exactly one child.
        //
        // A node has exactly one child if:
        // - left is not null and right is null, OR
        // - left is null and right is not null
        //
        // A very clean way to express "exactly one is present" is with XOR logic.
        // In C#, for booleans, ^ means exclusive OR:
        // - true ^ false  => true
        // - false ^ true  => true
        // - true ^ true   => false
        // - false ^ false => false
        //
        // That matches our requirement perfectly.
        bool hasExactlyOneChild = (root.left != null) ^ (root.right != null);

        // Step 3:
        // Convert the boolean result into a numeric count for the current node.
        // If this node qualifies, contribute 1 to the answer.
        // Otherwise, contribute 0.
        int currentNodeCount = hasExactlyOneChild ? 1 : 0;

        // Step 4:
        // Recursively count qualifying nodes in the left subtree.
        // This explores every node reachable through the left child.
        int leftCount = CountNodesWithExactlyOneChild(root.left);

        // Step 5:
        // Recursively count qualifying nodes in the right subtree.
        // This explores every node reachable through the right child.
        int rightCount = CountNodesWithExactlyOneChild(root.right);

        // Step 6:
        // The total answer for this subtree is:
        // - the contribution from the current node
        // - plus everything found in the left subtree
        // - plus everything found in the right subtree
        return currentNodeCount + leftCount + rightCount;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

// Example 1:
// Input: [5,3,8,1,null,null,9]
//
// Tree shape:
//         5
//       /   \
//      3     8
//     /       \
//    1         9
//
// Node 3 has exactly one child.
// Node 8 has exactly one child.
// Total = 2
TreeNode example1 =
    new TreeNode(5,
        new TreeNode(3,
            new TreeNode(1),
            null),
        new TreeNode(8,
            null,
            new TreeNode(9)));

var solution = new Solution();
int result1 = solution.CountNodesWithExactlyOneChild(example1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 2

// Example 2:
// Input: [7,4,10,2,6,null,null]
//
// Tree shape:
//         7
//       /   \
//      4     10
//     / \
//    2   6
//
// Node 7 has two children -> does not count
// Node 4 has two children -> does not count
// Node 10 has zero children -> does not count
// Nodes 2 and 6 are leaves -> do not count
// Total = 0
TreeNode example2 =
    new TreeNode(7,
        new TreeNode(4,
            new TreeNode(2),
            new TreeNode(6)),
        new TreeNode(10));

int result2 = solution.CountNodesWithExactlyOneChild(example2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 0

// Additional demo: empty tree
TreeNode? example3 = null;
int result3 = solution.CountNodesWithExactlyOneChild(example3);
Console.WriteLine($"Example 3 Result (empty tree): {result3}"); // Expected: 0

// Additional demo: single node tree
TreeNode example4 = new TreeNode(42);
int result4 = solution.CountNodesWithExactlyOneChild(example4);
Console.WriteLine($"Example 4 Result (single node): {result4}"); // Expected: 0

// Additional demo: skewed tree
//
//     1
//      \
//       2
//        \
//         3
//
// Node 1 has exactly one child
// Node 2 has exactly one child
// Node 3 has zero children
// Total = 2
TreeNode example5 =
    new TreeNode(1,
        null,
        new TreeNode(2,
            null,
            new TreeNode(3)));

int result5 = solution.CountNodesWithExactlyOneChild(example5);
Console.WriteLine($"Example 5 Result (skewed tree): {result5}"); // Expected: 2