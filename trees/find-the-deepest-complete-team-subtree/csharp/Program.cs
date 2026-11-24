/*
Title: Find the Deepest Complete Team Subtree

Problem Description:
A company stores its reporting structure as a binary tree where each node represents a team lead.
A node may have a left direct report, a right direct report, both, or neither.

A subtree rooted at some node is called complete if every node in that subtree has either:
- exactly 0 children, or
- exactly 2 children

So, no node inside that subtree is allowed to have only one child.

Given the root of a binary tree, return the value of the root node of the deepest complete subtree.
If multiple complete subtrees have the same maximum depth, return the one with the smallest node value.

The depth of a subtree is defined by the depth of its root in the original tree, where the overall root has depth 0.

Important note about correctness:
A single node is a valid complete subtree because it has 0 children.
Therefore, every leaf is always a valid answer candidate.
The rule for choosing the answer is:
1. Prefer the greatest depth
2. If depths tie, prefer the smallest node value

This interpretation is the one implemented below because it matches the formal definition.
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
    private int _bestDepth;
    private int _bestValue;

    /*
    Time Complexity: O(n)
    - We visit each node exactly once.
    - At each node, we do only constant-time work after processing its children.

    Space Complexity: O(h)
    - h is the height of the tree due to recursion stack.
    - In the worst case of a completely skewed tree, h can be O(n).
    */
    public int FindDeepestCompleteSubtree(TreeNode root)
    {
        // We initialize the "best answer seen so far".
        //
        // Why use these fields?
        // We want to scan the tree once and update the answer whenever we find
        // a valid complete subtree root that is:
        // 1. deeper than the current best, or
        // 2. at the same depth but with a smaller value.
        //
        // Starting depth at -1 guarantees that even the root at depth 0 can replace it.
        _bestDepth = -1;
        _bestValue = int.MaxValue;

        // Start a depth-first traversal from the root at depth 0.
        Dfs(root, 0);

        // After the traversal finishes, _bestValue stores the answer.
        return _bestValue;
    }

    private bool Dfs(TreeNode? node, int depth)
    {
        // Base case:
        // A null node is treated as valid for recursive composition.
        //
        // Why?
        // When checking whether a parent has a complete subtree, we only allow:
        // - both children null  -> leaf
        // - both children non-null and both subtrees complete
        //
        // Returning true for null simplifies the recursion, but we still explicitly
        // check the actual child presence at the parent node to enforce the
        // "0 children or 2 children" rule.
        if (node == null)
        {
            return true;
        }

        // Recursively process the left subtree first.
        //
        // What this does:
        // It determines whether the entire subtree rooted at node.left is complete.
        //
        // Why this is necessary:
        // A subtree rooted at the current node can only be complete if both child
        // subtrees are themselves complete whenever both children exist.
        bool leftComplete = Dfs(node.left, depth + 1);

        // Recursively process the right subtree for the same reason.
        bool rightComplete = Dfs(node.right, depth + 1);

        // Determine whether the current node itself satisfies the local structural rule.
        //
        // There are only two allowed shapes for a node inside a complete subtree:
        // 1. It is a leaf: left == null and right == null
        // 2. It has exactly two children: left != null and right != null
        //
        // If exactly one child exists, this node immediately breaks the rule.
        bool hasNoChildren = node.left == null && node.right == null;
        bool hasTwoChildren = node.left != null && node.right != null;

        // The subtree rooted at the current node is complete if:
        // - the current node has 0 children, OR
        // - the current node has 2 children AND both child subtrees are complete
        //
        // Why this works:
        // This is a classic bottom-up tree property.
        // We first ask children whether they are valid complete subtrees.
        // Then we combine their answers with the current node's child-count rule.
        bool currentIsComplete =
            hasNoChildren ||
            (hasTwoChildren && leftComplete && rightComplete);

        // If the current subtree is complete, then the current node is a valid answer candidate.
        if (currentIsComplete)
        {
            // We update the global best answer using the required ordering:
            // 1. deeper depth is better
            // 2. if same depth, smaller node value is better
            //
            // Why compare here?
            // Because every valid complete subtree root should be considered exactly once,
            // and this is the moment we know the current node is valid.
            if (depth > _bestDepth || (depth == _bestDepth && node.val < _bestValue))
            {
                _bestDepth = depth;
                _bestValue = node.val;
            }
        }

        // Return whether the current subtree is complete so the parent can use this result.
        return currentIsComplete;
    }
}

static TreeNode? BuildTree(int?[] values)
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

// Demo code
var solution = new Solution();

// Example 1 from the prompt:
// root = [8,4,10,2,6,9,null,null,null,5,7]
//
// Tree shape:
//         8
//       /   \
//      4     10
//     / \    /
//    2   6  9
//       / \
//      5   7
//
// Valid complete subtree roots include leaves 2,5,7,9.
// Node 6 is also complete because it has exactly two children and both are leaves.
// Node 4 is complete too because it has two children (2 and 6), and both subtrees are complete.
// Root 8 is not complete because node 10 has only one child inside that subtree.
//
// According to the formal rule "deepest depth first, then smallest value",
// the deepest valid roots are 5 and 7 at depth 3, so the answer is 5.
TreeNode? root1 = BuildTree(new int?[] { 8, 4, 10, 2, 6, 9, null, null, null, 5, 7 });
int result1 = solution.FindDeepestCompleteSubtree(root1!);
Console.WriteLine(result1);

// Example 2 from the prompt:
// root = [12,7,20,3,null,15,25]
//
// Tree shape:
//        12
//       /  \
//      7    20
//     /    /  \
//    3    15  25
//
// Node 7 is not complete because it has exactly one child.
// Node 20 is complete because it has exactly two children and both are leaves.
// Leaves 3, 15, and 25 are also complete.
//
// The deepest valid roots are 3, 15, and 25 at depth 2.
// Smallest value among them is 3.
TreeNode? root2 = BuildTree(new int?[] { 12, 7, 20, 3, null, 15, 25 });
int result2 = solution.FindDeepestCompleteSubtree(root2!);
Console.WriteLine(result2);

// Additional demo:
// A perfect small tree where the deepest valid roots are leaves.
TreeNode? root3 = BuildTree(new int?[] { 1, 2, 3, 4, 5, 6, 7 });
int result3 = solution.FindDeepestCompleteSubtree(root3!);
Console.WriteLine(result3);

// Additional demo:
// Single node tree. The single node is a complete subtree by definition.
TreeNode? root4 = BuildTree(new int?[] { 42 });
int result4 = solution.FindDeepestCompleteSubtree(root4!);
Console.WriteLine(result4);