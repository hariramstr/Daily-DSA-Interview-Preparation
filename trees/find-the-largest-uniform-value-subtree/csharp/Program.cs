/*
Title: Find the Largest Uniform-Value Subtree
Difficulty: Medium
Topic: Trees

Problem Description:
You are given the root of a binary tree where each node stores an integer category code.
A subtree is called uniform if every node inside that subtree has the same value.
A subtree may consist of a single node, which is always considered uniform.

Return the size (number of nodes) of the largest uniform subtree in the tree.

Your task is to examine every possible subtree rooted at each node and determine whether
all nodes below that root, including the root itself, share the same value. If they do,
that subtree is uniform and contributes its total node count as a candidate answer.
If not, you must continue checking smaller subtrees.

This problem is intended to test recursive tree reasoning and bottom-up aggregation of
information from child subtrees. An efficient solution should avoid re-checking the same
nodes many times.

Constraints:
- The number of nodes in the tree is in the range [1, 10^5].
- Node values are in the range [-10^9, 10^9].
- The tree is binary, and may be unbalanced.
- Your solution should run in O(n) time.

Example 1:
Input: root = [5,5,5,5,5,null,5]
Output: 6
Explanation: Every existing node has value 5, so the entire tree is a uniform subtree with 6 nodes.

Example 2:
Input: root = [3,3,4,3,null,4,4]
Output: 3
Explanation: The subtree rooted at the right child of the root contains three nodes, all with value 4,
so its size is 3. The whole tree is not uniform because it contains both 3 and 4.
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
    private int _best;

    private struct SubtreeInfo
    {
        public bool IsUniform;
        public int Size;

        public SubtreeInfo(bool isUniform, int size)
        {
            IsUniform = isUniform;
            Size = size;
        }
    }

    /*
    Time Complexity: O(n)
    - We visit each node exactly once.
    - At each node, we do only constant-time work after processing its children.

    Space Complexity: O(h)
    - h is the height of the tree due to recursion stack.
    - In the worst case of a completely unbalanced tree, h can be O(n).
    - In a balanced tree, h is O(log n).
    */
    public int LargestUniformSubtree(TreeNode? root)
    {
        // This variable stores the largest uniform subtree size found anywhere in the tree.
        // We keep it as a field so every recursive call can update the same answer.
        _best = 0;

        // Start a post-order traversal:
        // 1. Solve left subtree
        // 2. Solve right subtree
        // 3. Use those results to decide whether the current subtree is uniform
        //
        // Post-order is the natural choice here because whether the current node forms
        // a uniform subtree depends on complete information from both children.
        Dfs(root);

        // After the full traversal, _best contains the answer.
        return _best;
    }

    private SubtreeInfo Dfs(TreeNode? node)
    {
        // Base case:
        // An empty subtree is treated as "uniform" with size 0.
        //
        // Why is this useful?
        // Because it makes the parent logic much simpler:
        // - If a child is missing, it should not prevent the parent from being uniform.
        // - A null child contributes 0 nodes to the subtree size.
        if (node == null)
        {
            return new SubtreeInfo(true, 0);
        }

        // Recursively gather information from the left subtree.
        // This tells us:
        // - whether the left subtree is uniform
        // - how many nodes it contains
        SubtreeInfo leftInfo = Dfs(node.left);

        // Recursively gather information from the right subtree.
        SubtreeInfo rightInfo = Dfs(node.right);

        // Start by computing the total size of the subtree rooted at the current node.
        // This is always:
        // size(left subtree) + size(right subtree) + 1 for the current node itself.
        int currentSubtreeSize = leftInfo.Size + rightInfo.Size + 1;

        // Now we determine whether the current subtree is uniform.
        //
        // A subtree rooted at "node" is uniform if ALL of the following are true:
        // 1. The left subtree is uniform.
        // 2. The right subtree is uniform.
        // 3. If the left child exists, its value must equal node.val.
        // 4. If the right child exists, its value must equal node.val.
        //
        // Why is checking only the child values enough?
        // Because if a child subtree is already known to be uniform, then every node in that
        // child subtree has the same value as the child root. So matching the child root value
        // with the current node value guarantees the entire child subtree matches too.
        bool isUniform = leftInfo.IsUniform
                         && rightInfo.IsUniform
                         && (node.left == null || node.left.val == node.val)
                         && (node.right == null || node.right.val == node.val);

        // If the current subtree is uniform, then its full size is a valid candidate answer.
        // We compare it against the best answer seen so far.
        if (isUniform)
        {
            _best = Math.Max(_best, currentSubtreeSize);

            // Return that this subtree is uniform, along with its full size.
            return new SubtreeInfo(true, currentSubtreeSize);
        }

        // If the current subtree is NOT uniform, we still return its total size because the parent
        // needs subtree sizes to compute its own total size.
        //
        // However, IsUniform is false, which tells the parent that this subtree cannot be merged
        // into a larger uniform subtree through the current node.
        return new SubtreeInfo(false, currentSubtreeSize);
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

// Demo code:
// We create the sample trees from the problem statement,
// call the solution, and print the results.

// Example 1:
// Input: [5,5,5,5,5,null,5]
// Tree structure:
//         5
//       /   \
//      5     5
//     / \     \
//    5   5     5
//
// Every node has value 5, so the whole tree is uniform.
// Total nodes = 6, so expected output is 6.
TreeNode? root1 = BuildTreeFromLevelOrder(new int?[] { 5, 5, 5, 5, 5, null, 5 });
Solution solution = new Solution();
int result1 = solution.LargestUniformSubtree(root1);
Console.WriteLine(result1); // Expected: 6

// Example 2:
// Input: [3,3,4,3,null,4,4]
// Tree structure:
//         3
//       /   \
//      3     4
//     /     / \
//    3     4   4
//
// Left subtree rooted at left child:
//      3
//     /
//    3
// This is uniform with size 2.
//
// Right subtree rooted at right child:
//      4
//     / \
//    4   4
// This is uniform with size 3.
//
// Whole tree is not uniform because root is 3 but right child is 4.
// So expected output is 3.
TreeNode? root2 = BuildTreeFromLevelOrder(new int?[] { 3, 3, 4, 3, null, 4, 4 });
int result2 = solution.LargestUniformSubtree(root2);
Console.WriteLine(result2); // Expected: 3

// Additional small demo:
// Single node tree is always uniform, so answer should be 1.
TreeNode? root3 = BuildTreeFromLevelOrder(new int?[] { 42 });
int result3 = solution.LargestUniformSubtree(root3);
Console.WriteLine(result3); // Expected: 1