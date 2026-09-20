/*
Title: Minimum Cameras to Monitor a Facility Tree

Problem Description:
A company models its facility layout as a binary tree. Each tree node represents a room, and each room may have up to two directly connected child rooms. You want to install security cameras so that every room is monitored.

A camera placed in a room monitors exactly three types of rooms: the room where it is installed, its parent room, and its immediate child rooms. A room is considered secure if it is monitored by at least one camera.

Given the root of the binary tree, return the minimum number of cameras needed to monitor all rooms.

Return the minimum number of cameras needed to monitor all rooms.

Key idea:
Use a postorder traversal (process children before parent) and assign one of three states to every node:
1. NeedsCoverage  -> this node is not covered by any camera yet
2. HasCamera      -> this node has a camera installed on it
3. Covered        -> this node does not have a camera, but it is covered by a child's camera

Why postorder works:
A parent's best decision depends on the status of its children.
- If any child still needs coverage, the parent must place a camera.
- If any child has a camera, the parent is already covered.
- Otherwise, the parent is not covered and asks its parent for help.

This greedy strategy is optimal and is the standard minimum-camera solution for this tree problem.
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
    // We use three integer constants to represent the state returned by DFS.
    // Using constants keeps the code readable and avoids "magic numbers".
    private const int NeedsCoverage = 0;
    private const int HasCamera = 1;
    private const int Covered = 2;

    private int _cameraCount;

    /*
    Time Complexity: O(n)
    - We visit each node exactly once during the postorder traversal.

    Space Complexity: O(h)
    - h is the height of the tree due to recursion stack.
    - In the worst case of a completely skewed tree, h can be O(n).
    */
    public int MinCameraCover(TreeNode? root)
    {
        // Start with zero cameras installed.
        // We will increment this count only when we are forced to place a camera.
        _cameraCount = 0;

        // Perform a postorder DFS.
        // The returned state tells us whether the root:
        // - needs coverage,
        // - has a camera,
        // - or is already covered.
        int rootState = Dfs(root);

        // Important final check:
        // If the root still needs coverage after processing all children,
        // there is no parent above it to cover it.
        // Therefore, we must place one final camera at the root.
        if (rootState == NeedsCoverage)
        {
            _cameraCount++;
        }

        // Return the minimum number of cameras found by the greedy traversal.
        return _cameraCount;
    }

    private int Dfs(TreeNode? node)
    {
        // Base case for null:
        // A null node does not need a camera and should not force its parent
        // to place one. So we treat null as already covered.
        //
        // Why is this important?
        // Consider a leaf node. Its left and right children are null.
        // If null children were treated as "needing coverage", every leaf
        // would force a camera on itself unnecessarily.
        if (node == null)
        {
            return Covered;
        }

        // Step 1: Process left subtree first.
        // We need the left child's state before deciding what to do at this node.
        int leftState = Dfs(node.left);

        // Step 2: Process right subtree.
        // Again, we need the right child's state before deciding for the current node.
        int rightState = Dfs(node.right);

        // At this point, both children have already been fully analyzed.
        // This is exactly why postorder traversal is the right choice:
        // children decide first, then parent reacts.

        // Step 3: If ANY child needs coverage, we must place a camera here.
        //
        // Why?
        // A child that "needs coverage" means:
        // - it does not have a camera,
        // - and none of its children cover it.
        //
        // The only remaining node that can cover that child efficiently is its parent,
        // which is the current node.
        //
        // So placing a camera here covers:
        // - the current node,
        // - its parent,
        // - its left child,
        // - its right child.
        //
        // This is the greedy choice, and it is optimal.
        if (leftState == NeedsCoverage || rightState == NeedsCoverage)
        {
            _cameraCount++;
            return HasCamera;
        }

        // Step 4: If any child has a camera, then this node is already covered.
        //
        // Why?
        // Because a camera monitors its parent.
        //
        // So if either left child or right child has a camera,
        // the current node does not need its own camera.
        if (leftState == HasCamera || rightState == HasCamera)
        {
            return Covered;
        }

        // Step 5: If we reach here, both children are covered,
        // but neither child has a camera.
        //
        // That means:
        // - the current node is NOT covered by a child camera,
        // - and placing a camera here right now would be premature.
        //
        // So we return "NeedsCoverage" and let the parent decide.
        //
        // This is what makes the solution minimal:
        // we only place cameras when they are truly necessary.
        return NeedsCoverage;
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
// Example 1 from the prompt:
// Input: [0,0,null,0,0]
// Structure:
//       0
//      /
//     0
//    / \
//   0   0
//
// Optimal answer: 1 camera on the second node from the top.
var solution = new Solution();
TreeNode? root1 = BuildTreeFromLevelOrder(new int?[] { 0, 0, null, 0, 0 });
int result1 = solution.MinCameraCover(root1);
Console.WriteLine($"Example 1 Result: {result1}");

// Demo 2:
// Example 2 from the prompt:
// Input: [0,0,null,0,null,0,null,null,0]
//
// This forms a deeper shape that requires 2 cameras.
TreeNode? root2 = BuildTreeFromLevelOrder(new int?[] { 0, 0, null, 0, null, 0, null, null, 0 });
int result2 = solution.MinCameraCover(root2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional small sanity check:
// Single node tree needs exactly 1 camera.
TreeNode? root3 = BuildTreeFromLevelOrder(new int?[] { 42 });
int result3 = solution.MinCameraCover(root3);
Console.WriteLine($"Single Node Result: {result3}");