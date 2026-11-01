/*
Title: Find the First Turn Point on a Binary Route
Difficulty: Easy
Topic: Trees

Problem Description:
You are given the root of a binary tree and a target value that is guaranteed to exist in the tree.
Imagine walking from the root down to the node containing the target value. Along this route, each move
is either to the left child or to the right child.

A node is called a turn point if the direction you take from that node is different from the direction
taken from its parent. In other words:
- if you moved left from one node and then right from the next node, that next node is a turn point
- if you moved right from one node and then left from the next node, that next node is a turn point

Your task is to return the value of the first turn point encountered on the path from the root to the target.
If the path never changes direction, return -1.

Important clarification based on the examples:
If the path is 8 -> 4 -> 6 -> 7, the moves are:
- 8 to 4 = Left
- 4 to 6 = Right
- 6 to 7 = Right

The first change happens when we leave node 4 using Right after previously using Left from node 8.
So the first turn point is node 4, and the answer is 4.

The root can never be considered a turn point because it has no parent.
If the target is the root, the answer is also -1.

Constraints:
- The number of nodes is in the range [1, 1000]
- Node values are unique integers in the range [-10^4, 10^4]
- The target value always exists in the tree
- The tree is a standard binary tree, not necessarily balanced or complete
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
    // Time Complexity: O(n)
    // We may need to visit every node once in the worst case while searching for the target.
    //
    // Space Complexity: O(h)
    // h is the height of the tree. This space is used by the recursion call stack.
    // In the worst case of a completely skewed tree, h can be n.
    public int FindFirstTurnPoint(TreeNode? root, int target)
    {
        // If the tree is empty, there is no path at all.
        // The problem guarantees a valid tree and target, but this check makes the method safer.
        if (root == null)
        {
            return -1;
        }

        // If the target is the root itself, then we do not move anywhere.
        // Since a turn requires at least two moves to compare directions,
        // the answer must be -1.
        if (root.val == target)
        {
            return -1;
        }

        // We will perform a depth-first search from the root to the target.
        //
        // To detect the first turn, we need to remember:
        // 1. The direction used to arrive at the current node's child search
        //    ('L' for left, 'R' for right, or '\0' when no previous direction exists yet)
        // 2. The first turn point found so far
        //
        // We return the answer through an out parameter because once we find the target,
        // we want to know whether a turn was already discovered earlier on that exact path.
        int answer = -1;

        // Start DFS from the root.
        // There is no previous direction yet because the root has no parent.
        Dfs(root, target, '\0', ref answer);

        return answer;
    }

    private bool Dfs(TreeNode? node, int target, char previousDirection, ref int answer)
    {
        // Base case:
        // If the current node is null, this path does not exist,
        // so it definitely cannot contain the target.
        if (node == null)
        {
            return false;
        }

        // If we reached the target node, we stop searching.
        // At this point, "answer" already contains the first turn point found on the path,
        // or -1 if no turn ever happened.
        if (node.val == target)
        {
            return true;
        }

        // ------------------------------------------------------------
        // Step 1: Try going to the LEFT child
        // ------------------------------------------------------------
        //
        // Why do we do this?
        // We are exploring whether the target exists in the left subtree.
        //
        // What direction would this move use?
        // Moving from the current node to its left child means current direction = 'L'.
        //
        // How do we detect a turn here?
        // A turn happens if:
        // - there was a previous direction already, and
        // - the previous direction is different from the current direction
        //
        // Example:
        // previousDirection = 'R'
        // currentDirection  = 'L'
        // Then the path changed direction at the CURRENT node.
        //
        // Important:
        // We only want the FIRST turn point on the path.
        // So if answer is still -1 and a direction change happens now,
        // we temporarily record the current node's value as the answer.
        //
        // However, this recording is only valid if the target is actually found in that subtree.
        // If the target is not there, we must undo that temporary answer before exploring other paths.
        if (node.left != null)
        {
            char currentDirection = 'L';

            // Save the old answer so we can restore it if this branch does not lead to the target.
            int savedAnswer = answer;

            // If we already had a previous direction and it differs from the new one,
            // then the current node is a turn point.
            // We only record it if no earlier turn has been recorded yet.
            if (previousDirection != '\0' && previousDirection != currentDirection && answer == -1)
            {
                answer = node.val;
            }

            // Recursively search the left subtree.
            // If it returns true, the target was found there, so the current answer is valid.
            if (Dfs(node.left, target, currentDirection, ref answer))
            {
                return true;
            }

            // If we get here, the left subtree did NOT contain the target.
            // Any answer changes made while exploring that subtree must be discarded,
            // because they belong to a path that is not the real root-to-target path.
            answer = savedAnswer;
        }

        // ------------------------------------------------------------
        // Step 2: Try going to the RIGHT child
        // ------------------------------------------------------------
        //
        // This is symmetric to the left-child logic above.
        // We explore the right subtree and detect whether moving right causes
        // the first change in direction.
        if (node.right != null)
        {
            char currentDirection = 'R';

            // Again, save the current answer so we can roll back if needed.
            int savedAnswer = answer;

            // If the previous direction exists and differs from 'R',
            // then direction changed at the current node.
            if (previousDirection != '\0' && previousDirection != currentDirection && answer == -1)
            {
                answer = node.val;
            }

            // Search the right subtree.
            if (Dfs(node.right, target, currentDirection, ref answer))
            {
                return true;
            }

            // If target was not found in the right subtree, undo temporary changes.
            answer = savedAnswer;
        }

        // If neither subtree contains the target, report failure to the caller.
        return false;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

// Example 1:
// Tree: [8,4,12,2,6,null,14,null,null,5,7]
// Target: 7
//
// Structure:
//         8
//       /   \
//      4     12
//     / \      \
//    2   6      14
//       / \
//      5   7
//
// Path to 7: 8 -> 4 -> 6 -> 7
// Directions: L, R, R
// First change: from L to R at node 4
// Expected answer: 4
var root1 = new TreeNode(8);
root1.left = new TreeNode(4);
root1.right = new TreeNode(12);
root1.left.left = new TreeNode(2);
root1.left.right = new TreeNode(6);
root1.right.right = new TreeNode(14);
root1.left.right.left = new TreeNode(5);
root1.left.right.right = new TreeNode(7);

var solution = new Solution();
int result1 = solution.FindFirstTurnPoint(root1, 7);
Console.WriteLine(result1);

// Example 2:
// Tree: [10,5,15,3,7]
// Target: 3
//
// Structure:
//       10
//      /  \
//     5    15
//    / \
//   3   7
//
// Path to 3: 10 -> 5 -> 3
// Directions: L, L
// No direction change
// Expected answer: -1
var root2 = new TreeNode(10);
root2.left = new TreeNode(5);
root2.right = new TreeNode(15);
root2.left.left = new TreeNode(3);
root2.left.right = new TreeNode(7);

int result2 = solution.FindFirstTurnPoint(root2, 3);
Console.WriteLine(result2);

// Additional demo:
// Target is the root itself, so answer should be -1
int result3 = solution.FindFirstTurnPoint(root2, 10);
Console.WriteLine(result3);

// Additional demo:
// Path: 10 -> 5 -> 7
// Directions: L, R
// First turn point is node 5
int result4 = solution.FindFirstTurnPoint(root2, 7);
Console.WriteLine(result4);