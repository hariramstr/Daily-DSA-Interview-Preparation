/*
Title: Sum of Cousin Nodes in a Binary Tree
Difficulty: Easy
Topic: Trees

Problem Description:
You are given the root of a binary tree and a target value x that is guaranteed to appear exactly once in the tree.
Two nodes are considered cousins if they are on the same depth level but have different parents.

Your task is to return the sum of all cousin node values of the node whose value is x.

If the target node has no cousins, return 0.

The tree is not necessarily complete or balanced. Node values may be positive, negative, or zero,
but all values in the tree are unique so the target node can be identified by value alone.

A straightforward way to solve this is to traverse the tree level by level.
Once you find the level containing the target node, you should sum all nodes on that same level
except the target node itself and any siblings that share the same parent as the target node.

Constraints:
- The number of nodes in the tree is in the range [1, 1000]
- -10^4 <= Node.val <= 10^4
- All node values are unique
- x is the value of exactly one node in the tree

Example 1:
Input: root = [5,3,8,1,4,7,9], x = 4
Output: 16
Explanation:
The node with value 4 is at depth 2. Its parent is 3.
The other nodes at depth 2 are 1, 7, and 9.
Node 1 is a sibling, so it is not a cousin.
The cousins are 7 and 9, and their sum is 16.

Example 2:
Input: root = [10,6,15,3,null,12,18], x = 12
Output: 3
Explanation:
The node with value 12 is at depth 2 and its parent is 15.
The only node at the same depth with a different parent is 3, so the answer is 3.
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
    - In the worst case, we may visit each node in the tree once during the level-order traversal.

    Space Complexity: O(w)
    - The queue stores nodes level by level.
    - In the worst case, it can hold up to the maximum width of the tree.
    - In the worst case for a very wide tree, this can be O(n).

    Beginner-friendly idea:
    We use Breadth-First Search (BFS), also called level-order traversal.

    Why BFS is a great fit here:
    - Cousins are defined by being on the same depth level.
    - BFS naturally processes the tree one level at a time.
    - That means when we find the target node's parent at one level,
      we can immediately examine the next level to compute the cousin sum.

    Core observation:
    - The target node x itself is on some level L.
    - Its parent is on level L - 1.
    - If, while processing a level, we detect that one of the current nodes is the parent of x,
      then the children of all OTHER nodes on that same level are exactly the possible cousins.
    - We should sum the children of every node on that level except the children of the target's parent.
    */
    public int SumOfCousins(TreeNode? root, int x)
    {
        // If the tree is empty, there are no nodes at all.
        // The problem guarantees a valid tree with x present, but this guard keeps the method safe.
        if (root == null)
        {
            return 0;
        }

        // Special case:
        // If the root itself is the target, then it has no parent and therefore cannot have cousins.
        // Cousins must be on the same level with different parents, and the root has no peers at depth 0.
        if (root.val == x)
        {
            return 0;
        }

        // We use a queue for BFS (level-order traversal).
        // Each iteration of the outer loop processes exactly one depth level of the tree.
        Queue<TreeNode> queue = new Queue<TreeNode>();
        queue.Enqueue(root);

        // Continue until there are no more levels to process.
        while (queue.Count > 0)
        {
            // This tells us how many nodes are in the current level.
            // We must capture this count before processing the level,
            // because we will enqueue children for the next level during the loop.
            int levelSize = queue.Count;

            // This variable will store the parent node of the target x if we find it on this level.
            // Important detail:
            // We are not looking for x directly among the current nodes.
            // Instead, we are checking whether any current node has x as a child.
            // If yes, then that current node is the parent of x.
            TreeNode? targetParent = null;

            // We also keep a list of all nodes in the current level.
            // Why store them?
            // Because if we discover the target's parent somewhere in this level,
            // we then need to examine ALL nodes in this same level to sum the children
            // of every node except the target's parent.
            List<TreeNode> currentLevelNodes = new List<TreeNode>(levelSize);

            // Process every node in the current level.
            for (int i = 0; i < levelSize; i++)
            {
                // Remove the next node from the queue.
                TreeNode current = queue.Dequeue();

                // Save it so we can revisit this level after we know whether the target's parent is here.
                currentLevelNodes.Add(current);

                // Check whether the current node is the parent of the target.
                // A node is the parent of x if either its left child or right child has value x.
                // Since all values are unique, this identification is safe and unambiguous.
                bool leftIsTarget = current.left != null && current.left.val == x;
                bool rightIsTarget = current.right != null && current.right.val == x;

                if (leftIsTarget || rightIsTarget)
                {
                    targetParent = current;
                }

                // Standard BFS step:
                // enqueue children so the next level can be processed later.
                if (current.left != null)
                {
                    queue.Enqueue(current.left);
                }

                if (current.right != null)
                {
                    queue.Enqueue(current.right);
                }
            }

            // After processing the entire current level, we know whether this level contains the parent of x.
            if (targetParent != null)
            {
                // Now we compute the sum of cousins.
                // Cousins are nodes on the next level whose parent is NOT the target's parent.
                int cousinSum = 0;

                // Examine every node in the current level.
                foreach (TreeNode parent in currentLevelNodes)
                {
                    // Skip the target's parent entirely.
                    // Why?
                    // Because the children of the target's parent are siblings of x (including x itself),
                    // and siblings are explicitly NOT cousins.
                    if (parent == targetParent)
                    {
                        continue;
                    }

                    // For every other parent on this level,
                    // all of its children are on the same depth as x and have a different parent.
                    // Therefore, they are cousins and should be included in the sum.
                    if (parent.left != null)
                    {
                        cousinSum += parent.left.val;
                    }

                    if (parent.right != null)
                    {
                        cousinSum += parent.right.val;
                    }
                }

                // We found the correct level and computed the answer, so return it immediately.
                return cousinSum;
            }

            // If targetParent is still null after this level,
            // then the target is not among the children of this level.
            // We continue BFS to the next level.
        }

        // The problem guarantees x exists exactly once in the tree,
        // so in valid input we should always return earlier.
        // This fallback keeps the method complete and safe.
        return 0;
    }
}

// ------------------------------
// Demo Code
// ------------------------------

// Example 1:
// Tree: [5,3,8,1,4,7,9]
// Structure:
//         5
//       /   \
//      3     8
//     / \   / \
//    1   4 7   9
//
// x = 4
// Parent of 4 is 3.
// Nodes at same depth: 1, 4, 7, 9
// Sibling of 4 is 1, so exclude 1 and 4.
// Cousins are 7 and 9 => sum = 16
TreeNode root1 = new TreeNode(
    5,
    new TreeNode(
        3,
        new TreeNode(1),
        new TreeNode(4)
    ),
    new TreeNode(
        8,
        new TreeNode(7),
        new TreeNode(9)
    )
);

Solution solution = new Solution();
int result1 = solution.SumOfCousins(root1, 4);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 16

// Example 2:
// Tree: [10,6,15,3,null,12,18]
// Structure:
//         10
//       /    \
//      6      15
//     /      /  \
//    3      12  18
//
// x = 12
// Parent of 12 is 15.
// Nodes at same depth: 3, 12, 18
// Sibling of 12 is 18, so exclude 12 and 18.
// Cousin is 3 => sum = 3
TreeNode root2 = new TreeNode(
    10,
    new TreeNode(
        6,
        new TreeNode(3),
        null
    ),
    new TreeNode(
        15,
        new TreeNode(12),
        new TreeNode(18)
    )
);

int result2 = solution.SumOfCousins(root2, 12);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 3

// Additional demo:
// Target is the root, so it has no cousins.
TreeNode root3 = new TreeNode(42);
int result3 = solution.SumOfCousins(root3, 42);
Console.WriteLine($"Additional Example Result: {result3}"); // Expected: 0