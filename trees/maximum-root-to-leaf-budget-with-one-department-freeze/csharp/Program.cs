/*
Title: Maximum Root-to-Leaf Budget With One Department Freeze

Problem Description:
You are given the hierarchy of a company as a binary tree. Each node represents a department,
and its integer value represents the net budget contribution of that department for the quarter.
The value may be positive (profit) or negative (loss). A valid reporting chain is any path from
the root department down to a leaf department.

The finance team is allowed to apply at most one "department freeze" on a reporting chain.
Freezing a department means you may skip the contribution of exactly one node on the chosen
root-to-leaf path, treating its value as 0 instead of adding it to the path total.
You may also choose not to use the freeze.

Return the maximum possible budget total among all root-to-leaf paths after applying at most one freeze.

A leaf is a node with no children. The freeze can be used on the root, an internal node, or a leaf,
but at most once on the chosen path.

Constraints:
- The number of nodes in the tree is in the range [1, 100000].
- -100000 <= Node.val <= 100000
- The tree is a binary tree.
- The answer fits in a 64-bit signed integer.
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
    Time Complexity:
    - O(n), where n is the number of nodes in the tree.
    - We visit each node exactly once and do only constant work per node.

    Space Complexity:
    - O(n) in the worst case.
    - We store one DP result per node in a dictionary and use an explicit stack for iterative DFS.
    - This avoids recursion depth issues for very deep trees (important because n can be 100000).
    */
    public long MaxRootToLeafBudgetWithOneFreeze(TreeNode? root)
    {
        // The problem guarantees at least one node, but returning 0 for null keeps the method safe.
        if (root == null)
        {
            return 0;
        }

        // We need a post-order traversal:
        // children must be processed before their parent, because the parent's answer depends on
        // the best answers from its child subtrees.
        //
        // Why iterative instead of recursive?
        // Because the tree can contain up to 100000 nodes. A recursive DFS on a very skewed tree
        // could cause a stack overflow. An explicit stack is safer for production-quality code.
        //
        // Each stack item contains:
        // - node: the current tree node
        // - visited: false means "first time we see this node"
        //            true means "children have already been processed, now compute this node's DP"
        var stack = new Stack<(TreeNode node, bool visited)>();
        stack.Push((root, false));

        // For each node, we store two values:
        //
        // noFreeze:
        //   The best possible sum from THIS node down to ANY leaf in its subtree,
        //   assuming we have NOT used the freeze yet on this path.
        //
        // usedFreeze:
        //   The best possible sum from THIS node down to ANY leaf in its subtree,
        //   assuming the freeze HAS ALREADY been used somewhere on this path
        //   (possibly at this node, or lower in the subtree).
        //
        // These two states are the key idea of the problem.
        //
        // Why do we need both?
        // Because when standing at a node, we need to know:
        // 1) what is the best path if we still have the option to freeze one node later?
        // 2) what is the best path if that option is already gone?
        //
        // This is classic dynamic programming on trees with state.
        var dp = new Dictionary<TreeNode, (long noFreeze, long usedFreeze)>();

        while (stack.Count > 0)
        {
            var (node, visited) = stack.Pop();

            if (!visited)
            {
                // First time we see this node:
                // We push it back as visited=true so that after its children are processed,
                // we can compute its DP values.
                stack.Push((node, true));

                // Push children first so they are computed before the parent.
                if (node.right != null)
                {
                    stack.Push((node.right, false));
                }

                if (node.left != null)
                {
                    stack.Push((node.left, false));
                }
            }
            else
            {
                // Now we are in post-order position:
                // both children (if they exist) already have DP values in the dictionary.

                bool isLeaf = node.left == null && node.right == null;

                if (isLeaf)
                {
                    // Base case: leaf node.
                    //
                    // If we have not used the freeze yet:
                    // - We can simply take the leaf value.
                    //   So noFreeze = node.val
                    //
                    // If we have already used the freeze somewhere on the path:
                    // - We must include this leaf normally.
                    //   So usedFreeze = node.val
                    //
                    // But there is one more important possibility:
                    // Since "usedFreeze" means "the freeze has been used somewhere on this path,
                    // including possibly at this node", we are also allowed to freeze THIS leaf.
                    // If we freeze this leaf, its contribution becomes 0.
                    //
                    // Therefore for a leaf:
                    // noFreeze = node.val
                    // usedFreeze = max(node.val, 0)
                    //
                    // Example:
                    // leaf = -6
                    // noFreeze = -6
                    // usedFreeze = max(-6, 0) = 0
                    // This correctly captures that if we still have the freeze available,
                    // using it on this leaf is beneficial.
                    long noFreeze = node.val;
                    long usedFreeze = Math.Max((long)node.val, 0L);

                    dp[node] = (noFreeze, usedFreeze);
                    continue;
                }

                // For non-leaf nodes, we need the best child continuation.
                //
                // Because a valid path must go from the current node down to exactly one leaf,
                // we can only continue into one child: left or right.
                //
                // So we will compute the best possible continuation among existing children.
                long bestChildNoFreeze = long.MinValue;
                long bestChildUsedFreeze = long.MinValue;

                if (node.left != null)
                {
                    var leftState = dp[node.left];

                    // If we still have not used the freeze by the time we enter the left child,
                    // the best continuation is leftState.noFreeze.
                    bestChildNoFreeze = Math.Max(bestChildNoFreeze, leftState.noFreeze);

                    // If the freeze has already been used by the time we enter the left child,
                    // the best continuation is leftState.usedFreeze.
                    bestChildUsedFreeze = Math.Max(bestChildUsedFreeze, leftState.usedFreeze);
                }

                if (node.right != null)
                {
                    var rightState = dp[node.right];

                    bestChildNoFreeze = Math.Max(bestChildNoFreeze, rightState.noFreeze);
                    bestChildUsedFreeze = Math.Max(bestChildUsedFreeze, rightState.usedFreeze);
                }

                // Compute noFreeze for the current node:
                //
                // We have NOT used the freeze yet when arriving at this node.
                // Therefore, if we choose not to freeze this node, we must include node.val,
                // and then continue into the best child path that also has not used the freeze yet.
                //
                // Formula:
                // noFreeze = node.val + max(child.noFreeze)
                long noFreezeCurrent = (long)node.val + bestChildNoFreeze;

                // Compute usedFreeze for the current node:
                //
                // There are TWO ways to end up in the "freeze has been used" state for a path
                // starting at this node:
                //
                // Option 1: The freeze was already used somewhere below this node.
                //   Then we include node.val normally, and continue into the best child path
                //   whose state is already "usedFreeze".
                //
                //   candidate1 = node.val + max(child.usedFreeze)
                //
                // Option 2: We use the freeze RIGHT HERE at this node.
                //   Then this node contributes 0 instead of node.val,
                //   and below this node we must continue with children in the "noFreeze" state,
                //   because before freezing this node, the freeze had not been used yet.
                //
                //   candidate2 = 0 + max(child.noFreeze)
                //
                // We take the better of these two choices.
                long candidateUseFreezeBelow = (long)node.val + bestChildUsedFreeze;
                long candidateUseFreezeHere = bestChildNoFreeze;

                long usedFreezeCurrent = Math.Max(candidateUseFreezeBelow, candidateUseFreezeHere);

                dp[node] = (noFreezeCurrent, usedFreezeCurrent);
            }
        }

        // At the root, the final answer is:
        // max(
        //   best path with no freeze used at all,
        //   best path with the freeze used somewhere
        // )
        //
        // This exactly matches "at most one freeze":
        // - noFreeze means we chose not to use it
        // - usedFreeze means we used it once somewhere on the chosen path
        var rootState = dp[root];
        return Math.Max(rootState.noFreeze, rootState.usedFreeze);
    }
}

static TreeNode? BuildTree(int?[] values)
{
    if (values.Length == 0 || values[0] == null)
    {
        return null;
    }

    var root = new TreeNode(values[0]!.Value);
    var queue = new Queue<TreeNode>();
    queue.Enqueue(root);

    int index = 1;

    while (queue.Count > 0 && index < values.Length)
    {
        var current = queue.Dequeue();

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
// root = [5,4,8,-3,null,6,2]
//
// Root-to-leaf paths:
// 5 -> 4 -> -3 = 6
// 5 -> 8 -> 6  = 19
// 5 -> 8 -> 2  = 15
//
// Best without freeze = 19
// Best with freeze:
// - Freeze -3 on path 5->4->-3 => 9
// - Freeze 2 on path 5->8->2 => 13
// - Freeze 8 on path 5->8->6 => 11
// - Freeze 5 on path 5->8->6 => 14
// So overall best remains 19.
var root1 = BuildTree(new int?[] { 5, 4, 8, -3, null, 6, 2 });
var result1 = solution.MaxRootToLeafBudgetWithOneFreeze(root1);
Console.WriteLine(result1); // Expected: 19

// Example 2:
// root = [7,-5,3,4,2,-6,9]
//
// Paths:
// 7 -> -5 -> 4 = 6, freeze -5 => 11
// 7 -> -5 -> 2 = 4, freeze -5 => 9
// 7 -> 3 -> -6 = 4, freeze -6 => 10
// 7 -> 3 -> 9 = 19, no freeze needed
// Best overall = 19.
var root2 = BuildTree(new int?[] { 7, -5, 3, 4, 2, -6, 9 });
var result2 = solution.MaxRootToLeafBudgetWithOneFreeze(root2);
Console.WriteLine(result2); // Expected: 19

// Additional demo: single negative node.
// We may freeze the root itself, turning its contribution into 0.
var root3 = BuildTree(new int?[] { -10 });
var result3 = solution.MaxRootToLeafBudgetWithOneFreeze(root3);
Console.WriteLine(result3); // Expected: 0

// Additional demo: skewed tree where freezing a negative internal node helps.
var root4 = new TreeNode(1,
    new TreeNode(-5,
        new TreeNode(10),
        null),
    null);
var result4 = solution.MaxRootToLeafBudgetWithOneFreeze(root4);
Console.WriteLine(result4); // Path 1 -> -5 -> 10 = 6, freeze -5 => 11, Expected: 11