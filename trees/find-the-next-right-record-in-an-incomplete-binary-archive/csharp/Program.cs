/*
Title: Find the Next Right Record in an Incomplete Binary Archive
Difficulty: Medium
Topic: Trees

Problem Description:
You are given the root of a binary tree representing a partially filled archive index.
Each node stores a unique integer recordId. The tree is not necessarily perfect or complete,
and some children may be missing. You are also given an integer target representing the
recordId of an existing node in the tree.

For the node whose value is target, return the recordId of the nearest node to its right
on the same depth level. If no such node exists, return -1.

Two nodes are on the same depth level if they have the same distance from the root.
The "next right" node is the first node that would appear after the target when scanning
that level from left to right.

Your solution should work efficiently for large trees. A common interview follow-up is
whether you can solve this without storing every level explicitly, although any solution
with linear time complexity is acceptable.

Constraints:
- The number of nodes is in the range [1, 100000]
- 1 <= recordId <= 1000000000
- All recordId values are unique
- target is guaranteed to exist in the tree

Example 1:
Input: root = [10,5,15,2,7,null,20], target = 5
Output: 15

Example 2:
Input: root = [8,3,11,1,6,9,null,null,null,4,7], target = 6
Output: 9

Key idea:
Use Breadth-First Search (BFS), also called level-order traversal.
BFS naturally visits the tree one depth level at a time from left to right,
which matches the exact definition of "next right" in this problem.
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
    - In the worst case, we may visit every node in the tree once.

    Space Complexity: O(w)
    - The queue stores at most one level of the tree at a time.
    - In the worst case, this can be O(n), but more precisely it is O(w),
      where w is the maximum width of the tree.

    Beginner-friendly explanation:
    We perform a level-order traversal using a queue.
    For each level:
    1. We first record how many nodes belong to this level.
    2. We process exactly that many nodes from left to right.
    3. If we find the target:
       - If it is not the last node in this level, then the very next node
         removed from the queue in this same level is the answer.
       - If it is the last node in this level, then there is no node to its right,
         so the answer is -1.
    */
    public int FindNextRightRecord(TreeNode? root, int target)
    {
        // Defensive check:
        // The problem guarantees that target exists and the tree has at least one node,
        // but checking for null makes the method safer and easier to understand.
        if (root == null)
        {
            return -1;
        }

        // We use a queue because BFS needs "first in, first out" behavior.
        // This ensures we process nodes in the exact order they appear level by level,
        // from left to right.
        Queue<TreeNode> queue = new Queue<TreeNode>();

        // Start BFS from the root node.
        queue.Enqueue(root);

        // Continue until there are no more nodes left to process.
        while (queue.Count > 0)
        {
            // At the start of each loop, the queue contains all nodes for the current level
            // (plus possibly nodes from later levels only after we begin processing this level).
            //
            // We capture the current queue size now because that number tells us exactly
            // how many nodes belong to THIS level.
            //
            // This is extremely important:
            // - It lets us separate one depth level from the next.
            // - It allows us to know whether the target is the last node in its level.
            int levelSize = queue.Count;

            // Process every node in the current level from left to right.
            for (int i = 0; i < levelSize; i++)
            {
                // Remove the next node from the queue.
                // Because of BFS ordering, this is the next node in left-to-right order
                // within the current level.
                TreeNode current = queue.Dequeue();

                // Check whether this is the target node we are looking for.
                if (current.val == target)
                {
                    // If the target is NOT the last node in this level,
                    // then the next node to the right is simply the next node
                    // that will be processed in this same level.
                    //
                    // Since we are inside a for-loop over the current level,
                    // "i < levelSize - 1" means there is still at least one more node
                    // remaining in this level after the current one.
                    if (i < levelSize - 1)
                    {
                        // The next node in the queue is exactly the next right node
                        // on the same depth level.
                        return queue.Peek().val;
                    }

                    // Otherwise, the target is the last node in its level,
                    // so there is no node to its right.
                    return -1;
                }

                // Add the left child to the queue, if it exists.
                // We enqueue children after processing the current node so that
                // the next level is built in correct left-to-right order.
                if (current.left != null)
                {
                    queue.Enqueue(current.left);
                }

                // Add the right child to the queue, if it exists.
                // Left child is enqueued before right child so BFS preserves
                // natural left-to-right ordering within the next level.
                if (current.right != null)
                {
                    queue.Enqueue(current.right);
                }
            }
        }

        // The problem guarantees target exists, so normally execution never reaches here.
        // Returning -1 is still a safe fallback.
        return -1;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

// Example 1:
// Tree: [10,5,15,2,7,null,20]
// Structure:
//         10
//        /  \
//       5    15
//      / \     \
//     2   7     20
//
// Target = 5
// Level 1 is [5, 15], so the next right of 5 is 15.
TreeNode example1 =
    new TreeNode(10,
        new TreeNode(5,
            new TreeNode(2),
            new TreeNode(7)),
        new TreeNode(15,
            null,
            new TreeNode(20)));

int target1 = 5;

// Example 2:
// Tree: [8,3,11,1,6,9,null,null,null,4,7]
// Structure:
//           8
//         /   \
//        3     11
//       / \    /
//      1   6  9
//         / \
//        4   7
//
// Target = 6
// At the relevant depth, nodes are [1, 6, 9], so the next right of 6 is 9.
TreeNode example2 =
    new TreeNode(8,
        new TreeNode(3,
            new TreeNode(1),
            new TreeNode(6,
                new TreeNode(4),
                new TreeNode(7))),
        new TreeNode(11,
            new TreeNode(9),
            null));

int target2 = 6;

// Additional quick verification for the "no next right" case in Example 2:
// If target = 9, answer should be -1 because 9 is the last node on its level.
int target3 = 9;

Solution solution = new Solution();

int result1 = solution.FindNextRightRecord(example1, target1);
int result2 = solution.FindNextRightRecord(example2, target2);
int result3 = solution.FindNextRightRecord(example2, target3);

Console.WriteLine(result1); // Expected: 15
Console.WriteLine(result2); // Expected: 9
Console.WriteLine(result3); // Expected: -1