/*
Title: Count Single-Child Checkpoints in a Binary Tree
Difficulty: Easy
Topic: Trees

Problem Description:
You are given the root of a binary tree representing checkpoints in a monitoring system.
Each node stores an integer checkpoint ID. A checkpoint is called a single-child checkpoint
if it has exactly one direct child: either a left child or a right child, but not both.

Your task is to return the number of single-child checkpoints in the tree.

This is a structural tree problem: the checkpoint values themselves do not affect the answer.
You only need to examine whether each node has zero, one, or two children. A leaf node has
no children, so it does not count. A node with both left and right children also does not count.

The tree can be empty. In that case, the answer is 0.

Constraints:
- The number of nodes in the tree is in the range [0, 1000].
- Node values are in the range [-10^4, 10^4].
- The tree is a standard binary tree and is not necessarily balanced or complete.

Example 1:
Input: root = [5,3,8,1,null,null,9]
Output: 2
Explanation:
- Node 3 has only a left child (1), so it counts.
- Node 8 has only a right child (9), so it counts.
- Node 5 has two children, so it does not count.
- Nodes 1 and 9 are leaves, so they do not count.
Final answer = 2.

Example 2:
Input: root = [10,4,12,2,6,null,null]
Output: 0
Explanation:
- Node 10 has two children, so it does not count.
- Node 4 has two children, so it does not count.
- Nodes 2, 6, and 12 are leaves, so they do not count.
Final answer = 0.

Approach:
We can solve this by traversing every node in the tree exactly once.
For each node, we check:
- Does it have a left child?
- Does it have a right child?
If exactly one of those is true, then this node is a single-child checkpoint.

We will use Depth-First Search (DFS) recursively because it is simple and beginner-friendly.
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
    - We visit each node exactly one time.
    - At each node, we do only constant-time work:
      checking whether left and/or right child exists.

    Space Complexity: O(h)
    - This is the recursion stack space used by DFS.
    - h is the height of the tree.
    - In the worst case (a completely skewed tree), h can be n.
    - In a balanced tree, h is about log n.
    */
    public int CountSingleChildNodes(TreeNode? root)
    {
        // We start the recursive depth-first traversal from the root.
        // If the tree is empty (root is null), the helper method will return 0,
        // which matches the problem requirement.
        return Dfs(root);
    }

    private int Dfs(TreeNode? node)
    {
        // Step 1: Handle the base case.
        // If the current node is null, there is no actual tree node here.
        // That means there is nothing to count, so we return 0.
        // This base case is necessary to stop recursion when we move past leaf nodes.
        if (node == null)
        {
            return 0;
        }

        // Step 2: Determine whether the current node has exactly one child.
        // We do this by checking the presence of the left and right child references.
        //
        // A node is a "single-child checkpoint" if:
        // - left exists and right does not exist, OR
        // - right exists and left does not exist
        //
        // In boolean logic, this is exactly an XOR condition:
        // one is true, the other is false.
        bool hasLeftChild = node.left != null;
        bool hasRightChild = node.right != null;

        // We convert the boolean result into an integer count:
        // - 1 if this node qualifies
        // - 0 if it does not
        //
        // Cases:
        // - no children  -> false -> 0
        // - two children -> false -> 0
        // - exactly one  -> true  -> 1
        int currentNodeCount = (hasLeftChild ^ hasRightChild) ? 1 : 0;

        // Step 3: Recursively count qualifying nodes in the left subtree.
        // This explores every node reachable through the left child.
        int leftCount = Dfs(node.left);

        // Step 4: Recursively count qualifying nodes in the right subtree.
        // This explores every node reachable through the right child.
        int rightCount = Dfs(node.right);

        // Step 5: Combine the results.
        // The total number of single-child nodes in this subtree is:
        // - the count from the current node
        // - plus the count from the left subtree
        // - plus the count from the right subtree
        //
        // This works because these three parts do not overlap:
        // each node belongs to exactly one of them.
        return currentNodeCount + leftCount + rightCount;
    }
}

static TreeNode? BuildTreeFromLevelOrder(int?[] values)
{
    // This helper builds a binary tree from a level-order array representation.
    // Example:
    // [5, 3, 8, 1, null, null, 9]
    //
    // Means:
    //         5
    //       /   \
    //      3     8
    //     /       \
    //    1         9
    //
    // We use a queue because level-order construction naturally processes nodes
    // from top to bottom and left to right.

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

        // Try to assign the left child.
        if (index < values.Length && values[index] != null)
        {
            current.left = new TreeNode(values[index]!.Value);
            queue.Enqueue(current.left);
        }
        index++;

        // Try to assign the right child.
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
// Input: [5,3,8,1,null,null,9]
// Tree structure:
//         5
//       /   \
//      3     8
//     /       \
//    1         9
//
// Single-child nodes:
// - 3 has only left child -> count
// - 8 has only right child -> count
// Total = 2
TreeNode? root1 = BuildTreeFromLevelOrder(new int?[] { 5, 3, 8, 1, null, null, 9 });
int result1 = solution.CountSingleChildNodes(root1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 2

// Example 2:
// Input: [10,4,12,2,6,null,null]
// Tree structure:
//         10
//        /  \
//       4    12
//      / \
//     2   6
//
// Single-child nodes:
// - 10 has two children -> no
// - 4 has two children -> no
// - 12, 2, 6 are leaves -> no
// Total = 0
TreeNode? root2 = BuildTreeFromLevelOrder(new int?[] { 10, 4, 12, 2, 6, null, null });
int result2 = solution.CountSingleChildNodes(root2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 0

// Additional demo: empty tree
TreeNode? root3 = BuildTreeFromLevelOrder(Array.Empty<int?>());
int result3 = solution.CountSingleChildNodes(root3);
Console.WriteLine($"Empty Tree Result: {result3}"); // Expected: 0

// Additional demo: one-node tree
TreeNode? root4 = BuildTreeFromLevelOrder(new int?[] { 42 });
int result4 = solution.CountSingleChildNodes(root4);
Console.WriteLine($"Single Node Tree Result: {result4}"); // Expected: 0

// Additional demo: skewed tree
// [1,2,null,3,null,4]
// Structure:
//     1
//    /
//   2
//  /
// 3
///
//4
//
// Nodes 1, 2, and 3 each have exactly one child.
// Total = 3
TreeNode? root5 = BuildTreeFromLevelOrder(new int?[] { 1, 2, null, 3, null, 4 });
int result5 = solution.CountSingleChildNodes(root5);
Console.WriteLine($"Skewed Tree Result: {result5}"); // Expected: 3