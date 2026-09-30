/*
Title: Count Full Managers in an Org Tree
Difficulty: Easy
Topic: Trees

Problem Description:
A company stores its reporting structure as a binary tree. Each node represents one employee,
and the left and right child pointers represent up to two direct reports. A manager is called
full if they have exactly two direct reports. Employees with zero or one direct report are not
considered full managers.

Given the root of the org tree, return the number of full managers in the company.

This is a basic tree traversal problem. You may solve it using either depth-first search
(recursive or iterative) or breadth-first search. The tree can be empty, in which case the
answer is 0.

Constraints:
- The number of nodes in the tree is in the range [0, 1000].
- Node values are integers in the range [-10^4, 10^4].
- All node values are irrelevant to the counting logic; only the tree structure matters.

Example 1:
Input: root = [10,5,20,3,7,null,30]
Output: 2
Explanation:
- Employee 10 has two direct reports (5 and 20), so this node counts.
- Employee 5 has two direct reports (3 and 7), so this node counts.
- Employee 20 has only one direct report (30), so it does not count.
- Total full managers = 2.

Example 2:
Input: root = [1,2,3,4,null,null,null]
Output: 1
Explanation:
- Employee 1 has exactly two direct reports, so it counts.
- Employee 2 has only one direct report, so it does not count.
- All other nodes have zero direct reports, so they do not count.
- Total full managers = 1.
*/

using System;
using System.Collections.Generic;

// Definition for a binary tree node.
// Each node stores an integer value, but for this problem the value itself
// does not affect the answer. We only care about whether left and right children exist.
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
    - This is the extra space used by the recursive call stack,
      where h is the height of the tree.
    - In the worst case of a completely skewed tree, h can be n.
    - In a balanced tree, h is about log n.
    */
    public int CountFullManagers(TreeNode? root)
    {
        // Step 1:
        // Start a depth-first traversal from the root of the tree.
        //
        // Why this is necessary:
        // To know how many full managers exist, we must inspect every employee node,
        // because any node could potentially have exactly two direct reports.
        //
        // Why recursion is a good choice here:
        // A binary tree is naturally recursive:
        // - a node has a left subtree
        // - a node has a right subtree
        // So recursion lets us write a very clean and beginner-friendly traversal.
        return Dfs(root);
    }

    private int Dfs(TreeNode? node)
    {
        // Step 2:
        // Handle the base case: if the current node is null, there is no employee here.
        //
        // Why this is necessary:
        // During tree traversal, eventually we move past leaf nodes and reach null references.
        // A null node contributes 0 full managers.
        if (node == null)
        {
            return 0;
        }

        // Step 3:
        // Determine whether the current employee is a "full manager".
        //
        // A full manager must have:
        // - a left direct report
        // - a right direct report
        //
        // If both children are present, count this node as 1.
        // Otherwise, count it as 0.
        //
        // Why this is necessary:
        // The problem asks us to count exactly those nodes with two children.
        int currentNodeCount = (node.left != null && node.right != null) ? 1 : 0;

        // Step 4:
        // Recursively count full managers in the left subtree.
        //
        // Why this is necessary:
        // Any employee in the left reporting chain could also be a full manager,
        // so we must include that count.
        int leftCount = Dfs(node.left);

        // Step 5:
        // Recursively count full managers in the right subtree.
        //
        // Why this is necessary:
        // Just like the left subtree, the right subtree may contain additional full managers.
        int rightCount = Dfs(node.right);

        // Step 6:
        // Combine the results:
        // - current node's contribution
        // - left subtree contribution
        // - right subtree contribution
        //
        // Why this is necessary:
        // The total number of full managers in the subtree rooted at this node
        // is the sum of all three parts.
        return currentNodeCount + leftCount + rightCount;
    }
}

// -------------------------
// Demo Code
// -------------------------

// Example 1:
// Input: root = [10,5,20,3,7,null,30]
// Tree structure:
//         10
//        /  \
//       5    20
//      / \     \
//     3   7     30
//
// Full managers:
// - 10 has two children -> count
// - 5 has two children -> count
// - 20 has only one child -> do not count
// Expected answer: 2
var root1 = new TreeNode(
    10,
    new TreeNode(
        5,
        new TreeNode(3),
        new TreeNode(7)
    ),
    new TreeNode(
        20,
        null,
        new TreeNode(30)
    )
);

// Example 2:
// Input: root = [1,2,3,4,null,null,null]
// Tree structure:
//         1
//        / \
//       2   3
//      /
//     4
//
// Full managers:
// - 1 has two children -> count
// - 2 has one child -> do not count
// - 3 has zero children -> do not count
// - 4 has zero children -> do not count
// Expected answer: 1
var root2 = new TreeNode(
    1,
    new TreeNode(
        2,
        new TreeNode(4),
        null
    ),
    new TreeNode(3)
);

// Additional demo: empty tree
// Expected answer: 0
TreeNode? root3 = null;

var solution = new Solution();

int result1 = solution.CountFullManagers(root1);
int result2 = solution.CountFullManagers(root2);
int result3 = solution.CountFullManagers(root3);

Console.WriteLine($"Example 1 Output: {result1}");
Console.WriteLine($"Example 2 Output: {result2}");
Console.WriteLine($"Empty Tree Output: {result3}");