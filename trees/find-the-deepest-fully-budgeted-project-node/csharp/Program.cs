/*
Title: Find the Deepest Fully Budgeted Project Node

Problem Description:
You are given the root of a binary tree representing a hierarchy of project tasks. Each node stores an integer budget value.
A project node is called fully budgeted if the sum of all values in its left subtree is exactly equal to the sum of all
values in its right subtree. An empty subtree has sum 0.

Your task is to return the value of the deepest fully budgeted node in the tree. If multiple fully budgeted nodes exist
at the same deepest depth, return the one that appears leftmost in the tree. If no such node exists, return -1.

Depth is measured from the root, where the root has depth 0. A leaf node is fully budgeted because both its left and
right subtree sums are 0.

You should design an algorithm that works efficiently for trees with up to 100,000 nodes, so repeated subtree-sum
recomputation from every node will be too slow. A postorder traversal is useful because it allows us to compute subtree
sums while also tracking candidate answers by depth.

Constraints:
- The number of nodes in the tree is in the range [1, 100000]
- -100000 <= Node.val <= 100000
- The tree is binary and may be unbalanced
- Return the node value, not the node reference
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
    - Every node is pushed onto the traversal stack a constant number of times,
      and each node's subtree sum is computed exactly once.

    Space Complexity:
    - O(n) in the worst case.
    - We use:
      1. A stack for iterative postorder traversal
      2. A dictionary to store each node's subtree sum
    - In a highly unbalanced tree, these structures can grow to size n.

    Why iterative instead of recursive?
    - The tree can contain up to 100,000 nodes.
    - A recursive solution could cause a stack overflow on a very deep tree.
    - An explicit stack is safer for large inputs.
    */
    public int FindDeepestFullyBudgetedNode(TreeNode? root)
    {
        // If the tree is empty, there is no valid node to return.
        // The problem constraints say there is at least 1 node, but this guard
        // makes the method more robust and easier to understand.
        if (root == null)
        {
            return -1;
        }

        // This dictionary stores the total subtree sum for each node.
        // Key   -> a tree node
        // Value -> sum of all values in the subtree rooted at that node
        //
        // We need subtree sums because a node is "fully budgeted" when:
        // sum(left subtree) == sum(right subtree)
        //
        // Using a dictionary lets us compute each subtree sum once and then
        // reuse it when processing the parent.
        var subtreeSum = new Dictionary<TreeNode, long>();

        // We perform an iterative postorder traversal.
        //
        // Why postorder?
        // - To decide whether a node is fully budgeted, we need the sum of its left subtree
        //   and the sum of its right subtree.
        // - Those sums are only known after we have already processed the children.
        // - Postorder processes: left child -> right child -> current node
        //
        // The boolean flag means:
        // - false: this node is being visited for the first time, so we still need
        //          to process its children first
        // - true : both children have already been handled, so now we can compute
        //          this node's subtree sum and check whether it is fully budgeted
        var stack = new Stack<(TreeNode node, int depth, bool visited)>();
        stack.Push((root, 0, false));

        // These variables track the best answer found so far.
        //
        // bestDepth:
        // - deepest depth of any fully budgeted node seen so far
        //
        // bestValue:
        // - the value of the chosen node at bestDepth
        //
        // We initialize bestDepth to -1 so that even a leaf at depth 0 can replace it.
        int bestDepth = -1;
        int bestValue = -1;

        // Important detail about "leftmost" tie-breaking:
        // We only update the answer when we find a node at a STRICTLY deeper depth.
        // We do NOT update when depth == bestDepth.
        //
        // Why does that correctly preserve the leftmost node?
        // Because our traversal processes the left subtree before the right subtree.
        // Therefore, among nodes at the same depth, the leftmost one is encountered first.
        // By refusing to overwrite on equal depth, we keep that first (leftmost) node.
        while (stack.Count > 0)
        {
            var (node, depth, visited) = stack.Pop();

            if (!visited)
            {
                // First time we see this node.
                //
                // We are not ready to compute its subtree sum yet because we still need
                // the sums of its children.
                //
                // So we push the current node back with visited = true.
                // That means: "come back to this node after its children are done."
                stack.Push((node, depth, true));

                // Because the stack is LIFO (last in, first out), we push the right child first
                // and then the left child second.
                //
                // This ensures the left child is processed before the right child,
                // which is exactly what we want for postorder and for preserving the
                // leftmost tie-break rule.
                if (node.right != null)
                {
                    stack.Push((node.right, depth + 1, false));
                }

                if (node.left != null)
                {
                    stack.Push((node.left, depth + 1, false));
                }
            }
            else
            {
                // Second time we see this node.
                //
                // At this point, both children (if they exist) have already been processed,
                // so their subtree sums are available in the dictionary.
                //
                // For a missing child, the subtree sum is 0 because an empty subtree has sum 0.
                long leftSum = node.left != null ? subtreeSum[node.left] : 0L;
                long rightSum = node.right != null ? subtreeSum[node.right] : 0L;

                // Check whether the current node is fully budgeted.
                //
                // Definition:
                // A node is fully budgeted if the sum of values in its left subtree
                // equals the sum of values in its right subtree.
                //
                // Leaves automatically satisfy this because both sums are 0.
                if (leftSum == rightSum)
                {
                    // If this fully budgeted node is deeper than any one we have seen before,
                    // it becomes the new best answer.
                    //
                    // We intentionally use ">" and not ">=".
                    // That way, if another fully budgeted node appears later at the same depth,
                    // we keep the earlier one, which is the leftmost one.
                    if (depth > bestDepth)
                    {
                        bestDepth = depth;
                        bestValue = node.val;
                    }
                }

                // Now compute the subtree sum for the current node.
                //
                // subtree sum = current node value + left subtree sum + right subtree sum
                //
                // We store it so the parent can use it later.
                subtreeSum[node] = node.val + leftSum + rightSum;
            }
        }

        return bestValue;
    }
}

static TreeNode? BuildTree(params int?[] values)
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
// Input: [8,4,6,2,2,3,3]
// Fully budgeted nodes:
// - Leaves 2, 2, 3, 3 at depth 2
// - Node 4 at depth 1 because left subtree sum = 2 and right subtree sum = 2
// - Node 6 at depth 1 because left subtree sum = 3 and right subtree sum = 3
// Root is NOT fully budgeted because left subtree sum = 8 and right subtree sum = 12
// Deepest fully budgeted depth is 2, and the leftmost node there has value 2
var root1 = BuildTree(8, 4, 6, 2, 2, 3, 3);
Console.WriteLine(solution.FindDeepestFullyBudgetedNode(root1)); // Expected: 2

// Example 2:
// Input: [5,1,7,null,null,4,3]
// Leaves 1, 4, 3 are fully budgeted
// Node 7 is NOT fully budgeted because left subtree sum = 4 and right subtree sum = 3
// Deepest fully budgeted nodes are 4 and 3 at depth 2, leftmost is 4
var root2 = BuildTree(5, 1, 7, null, null, 4, 3);
Console.WriteLine(solution.FindDeepestFullyBudgetedNode(root2)); // Expected: 4

// Additional demo:
// Single-node tree: the root is also a leaf, so it is fully budgeted
var root3 = BuildTree(42);
Console.WriteLine(solution.FindDeepestFullyBudgetedNode(root3)); // Expected: 42

// Additional demo:
// Tree where a deeper leaf should beat a shallower fully budgeted internal node
var root4 = BuildTree(10, 5, 5, 2, 3, null, null, 1, 1);
Console.WriteLine(solution.FindDeepestFullyBudgetedNode(root4)); // Expected: 1