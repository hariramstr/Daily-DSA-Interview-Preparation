/*
Title: Find the Smallest Shared Category Subtree
Difficulty: Medium
Topic: Trees

Problem Description:
You are given the root of a binary tree representing a product category hierarchy. Each node contains an integer category ID, and IDs are unique within the tree. You are also given a list of distinct target category IDs that correspond to categories selected by different teams. Your task is to return the category ID of the smallest subtree that contains all target categories. A subtree is considered rooted at some node and includes that node and all of its descendants.

If all target categories appear in the tree, return the ID of the lowest node whose subtree contains every target ID. If one or more target IDs do not exist in the tree, return -1.

This problem is not asking for the path between nodes. Instead, you must identify the single subtree root that covers all requested categories as tightly as possible. The selected node may itself be one of the target categories.

You should design an algorithm that works efficiently for large trees.

Constraints:
- The number of nodes in the tree is in the range [1, 100000].
- Node values are unique integers in the range [1, 10^9].
- The number of target IDs is in the range [1, min(100000, n)].
- All target IDs are distinct.
- The tree is a binary tree and may be unbalanced.

Example 1:
Input: root = [10,5,15,3,7,12,18,null,4], targets = [4,7]
Output: 5

Example 2:
Input: root = [8,4,11,2,6,9,13,1,3,5,7], targets = [1,7,9]
Output: 8
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
    private TreeNode? _answerNode;
    private HashSet<int> _targetSet = new();

    /*
    Time Complexity: O(n + k)
    - n = number of nodes in the tree
    - k = number of target IDs
    We build a HashSet of targets in O(k), then visit each tree node exactly once in O(n).

    Space Complexity: O(h + k)
    - h = height of the tree due to recursion stack
    - k = number of target IDs stored in the HashSet
    In the worst case of a very unbalanced tree, h can be O(n).
    */
    public int FindSmallestSharedCategorySubtree(TreeNode? root, IList<int> targets)
    {
        // Step 1:
        // Convert the target list into a HashSet.
        //
        // What this does:
        // - Stores all target category IDs in a structure that supports very fast lookup.
        //
        // Why this is necessary:
        // - During traversal, for every node we need to quickly answer:
        //   "Is this node one of the targets?"
        // - A HashSet gives average O(1) lookup time, which is much faster than scanning a list.
        //
        // Data structure choice:
        // - HashSet<int> is ideal here because:
        //   1. target IDs are distinct
        //   2. we only need membership checks
        _targetSet = new HashSet<int>(targets);

        // Step 2:
        // Reset the stored answer before starting a new computation.
        //
        // What this does:
        // - Ensures previous runs do not affect the current result.
        //
        // Why this is necessary:
        // - If the same Solution object is reused, stale state could produce incorrect answers.
        _answerNode = null;

        // Step 3:
        // Perform a post-order DFS traversal.
        //
        // What this does:
        // - For each node, we compute how many target IDs exist inside its subtree.
        //
        // Why post-order is necessary:
        // - We must know the counts from the left and right subtrees before deciding
        //   whether the current node's subtree contains all targets.
        //
        // The returned value from DFS(node) means:
        // - "How many target IDs are found in the subtree rooted at node?"
        int foundCount = Dfs(root);

        // Step 4:
        // Verify that every target was actually found in the tree.
        //
        // What this does:
        // - Compares the total number of found target IDs with the number of requested targets.
        //
        // Why this is necessary:
        // - The problem explicitly says to return -1 if one or more target IDs do not exist in the tree.
        if (foundCount != _targetSet.Count)
        {
            return -1;
        }

        // Step 5:
        // At this point, all targets exist in the tree.
        //
        // What this means:
        // - _answerNode must be the lowest node whose subtree contains all targets.
        //
        // Why this is correct:
        // - During DFS, we set the answer only the first time we encounter a node whose subtree
        //   contains all targets.
        // - Because we use post-order traversal, deeper nodes are processed before their ancestors.
        // - Therefore, the first such node is the smallest / lowest valid subtree root.
        return _answerNode!.val;
    }

    private int Dfs(TreeNode? node)
    {
        // Base case:
        // If the current node is null, its subtree contains zero targets.
        //
        // Why this is necessary:
        // - Null children are valid endpoints in a binary tree.
        // - Returning 0 allows parent calculations to work naturally.
        if (node == null)
        {
            return 0;
        }

        // Step 1:
        // Recursively count how many target IDs appear in the left subtree.
        //
        // Why this is necessary:
        // - The current node's subtree includes everything in its left subtree.
        int leftCount = Dfs(node.left);

        // Step 2:
        // Recursively count how many target IDs appear in the right subtree.
        //
        // Why this is necessary:
        // - The current node's subtree also includes everything in its right subtree.
        int rightCount = Dfs(node.right);

        // Step 3:
        // Check whether the current node itself is one of the target IDs.
        //
        // What this does:
        // - Adds 1 if this node is a target, otherwise adds 0.
        //
        // Why this is necessary:
        // - The subtree rooted at this node includes the node itself, not just its children.
        int selfCount = _targetSet.Contains(node.val) ? 1 : 0;

        // Step 4:
        // Combine the counts from:
        // - left subtree
        // - right subtree
        // - current node itself
        //
        // What this total means:
        // - totalCount = number of target IDs found anywhere inside this node's subtree.
        int totalCount = leftCount + rightCount + selfCount;

        // Step 5:
        // If this subtree contains all target IDs, and we have not already chosen an answer,
        // then this node is the lowest valid answer.
        //
        // Why "first time" works:
        // - Because this is post-order traversal, children are processed before parents.
        // - So the first node whose subtree contains all targets must be the deepest one.
        // - Any ancestor would also contain all targets, but it would not be smaller.
        if (totalCount == _targetSet.Count && _answerNode == null)
        {
            _answerNode = node;
        }

        // Step 6:
        // Return the total count upward so the parent can compute its own subtree count.
        return totalCount;
    }
}

static TreeNode? BuildTreeFromLevelOrder(int?[] values)
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

// Demo 1:
// Tree: [10,5,15,3,7,12,18,null,4]
// Targets: [4,7]
// Expected output: 5
var root1 = BuildTreeFromLevelOrder(new int?[] { 10, 5, 15, 3, 7, 12, 18, null, 4 });
var targets1 = new List<int> { 4, 7 };

var solution = new Solution();
int result1 = solution.FindSmallestSharedCategorySubtree(root1, targets1);
Console.WriteLine(result1);

// Demo 2:
// Tree: [8,4,11,2,6,9,13,1,3,5,7]
// Targets: [1,7,9]
// Expected output: 8
var root2 = BuildTreeFromLevelOrder(new int?[] { 8, 4, 11, 2, 6, 9, 13, 1, 3, 5, 7 });
var targets2 = new List<int> { 1, 7, 9 };

int result2 = solution.FindSmallestSharedCategorySubtree(root2, targets2);
Console.WriteLine(result2);

// Demo 3:
// Missing target example
// Tree: [10,5,15,3,7,12,18,null,4]
// Targets: [4,999]
// Expected output: -1
var root3 = BuildTreeFromLevelOrder(new int?[] { 10, 5, 15, 3, 7, 12, 18, null, 4 });
var targets3 = new List<int> { 4, 999 };

int result3 = solution.FindSmallestSharedCategorySubtree(root3, targets3);
Console.WriteLine(result3);