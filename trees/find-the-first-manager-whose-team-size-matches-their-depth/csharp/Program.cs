/*
Title: Find the First Manager Whose Team Size Matches Their Depth

Problem Description:
You are given the root of an organizational hierarchy represented as a rooted tree.
Each node stores a unique employee ID, and each employee may have zero or more direct reports.
The root is the CEO at depth 0.

For any employee, define their team size as the total number of employees in that employee's
subtree, including the employee themself.

Your task is to find the employee ID of the shallowest employee whose team size is exactly equal
to their depth. If multiple employees at the same minimum depth satisfy the condition, return the
one that appears first in a left-to-right traversal of the tree based on the given order of children.
If no employee satisfies the condition, return -1.

This problem requires combining depth information from a top-down traversal with subtree size
information from a bottom-up traversal. A correct solution should work efficiently for large
hierarchies and should not recompute subtree sizes repeatedly.

Constraints:
- The number of nodes in the tree is in the range [1, 100000].
- 0 <= employee ID <= 10^9, and all IDs are unique.
- Each node may have between 0 and 10 children.
- Depth of the root is 0.
- The input tree is guaranteed to be valid and connected.

Example 1:
Input: root = [10, [20, 30], [40], [], [50, 60], [], []]
Output: 50
Explanation: Employee 50 is at depth 2 and has subtree size 2 (50 and 60), so it satisfies
the condition. No employee at depth 0 or 1 satisfies it.

Example 2:
Input: root = [1, [2, 3, 4], [], [], []]
Output: -1
Explanation: The root has subtree size 4 but depth 0, and each child has subtree size 1 but depth 1.
Since no employee has subtree size equal to depth, the answer is -1.
*/

using System;
using System.Collections.Generic;

public class EmployeeNode
{
    public int Id;
    public List<EmployeeNode> Children;

    public EmployeeNode(int id)
    {
        Id = id;
        Children = new List<EmployeeNode>();
    }
}

public class Solution
{
    /*
    Time Complexity: O(n)
    - We visit every node a constant number of times:
      1) once during the iterative DFS that records depth and traversal order
      2) once during the reverse processing that computes subtree sizes
    - Therefore the total work is linear in the number of employees.

    Space Complexity: O(n)
    - We store:
      1) a traversal order list containing all nodes
      2) a depth map for all nodes
      3) a subtree size map for all nodes
      4) an explicit stack for iterative DFS
    - All of these are linear in the number of nodes.
    */
    public int FindFirstManagerWhoseTeamSizeMatchesDepth(EmployeeNode root)
    {
        // If the tree is somehow empty, there is no valid employee to return.
        // The problem says the tree has at least one node, but this guard makes the method safer.
        if (root == null)
        {
            return -1;
        }

        // We will avoid recursive DFS because the tree can contain up to 100,000 nodes.
        // Deep recursion could cause a stack overflow in C#.
        //
        // Instead, we use an explicit stack to perform a preorder traversal:
        // - preorder means we process a node before its children
        // - this naturally gives us left-to-right traversal order if we push children in reverse
        //
        // Why do we need preorder order?
        // Because if multiple valid employees exist at the same minimum depth,
        // we must return the one that appears first in left-to-right traversal.
        // A preorder traversal preserves that left-to-right encounter order.
        var stack = new Stack<EmployeeNode>();

        // This list will store nodes in preorder traversal order.
        // Later, we will scan this list from left to right to find the first valid answer
        // among the shallowest depth.
        var preorder = new List<EmployeeNode>();

        // This dictionary stores the depth of each node.
        // We need depth because the condition is:
        //     subtree size == depth
        var depth = new Dictionary<EmployeeNode, int>();

        // Start traversal from the root, whose depth is 0.
        stack.Push(root);
        depth[root] = 0;

        while (stack.Count > 0)
        {
            // Remove the next node to process.
            var current = stack.Pop();

            // Record the node in preorder order.
            // This order is important for the tie-breaking rule.
            preorder.Add(current);

            // To preserve left-to-right traversal order with a stack,
            // we push children from right to left.
            //
            // Example:
            // If children are [A, B, C], and we want to visit A first,
            // we must push C, then B, then A.
            for (int i = current.Children.Count - 1; i >= 0; i--)
            {
                var child = current.Children[i];

                // The child's depth is exactly one more than the current node's depth.
                depth[child] = depth[current] + 1;

                // Push child so it will be processed later.
                stack.Push(child);
            }
        }

        // Now we need subtree sizes.
        //
        // A subtree size depends on the subtree sizes of all children:
        //     subtreeSize(node) = 1 + sum(subtreeSize(child))
        //
        // This is a bottom-up computation.
        // Since preorder stored parent before children, if we process that list in reverse,
        // then children will be processed before parents.
        //
        // That makes reverse preorder a convenient way to compute subtree sizes iteratively.
        var subtreeSize = new Dictionary<EmployeeNode, int>();

        for (int i = preorder.Count - 1; i >= 0; i--)
        {
            var current = preorder[i];

            // Every subtree includes the node itself, so start with size 1.
            int size = 1;

            // Add the already-computed subtree sizes of all direct reports.
            foreach (var child in current.Children)
            {
                size += subtreeSize[child];
            }

            // Store the final subtree size for this node.
            subtreeSize[current] = size;
        }

        // At this point we know, for every employee:
        // - their depth
        // - their subtree size
        //
        // Now we must find:
        // 1) the shallowest employee whose subtree size == depth
        // 2) if multiple exist at that same shallowest depth, the first one in left-to-right order
        //
        // Because preorder is already left-to-right, we can scan it once from start to finish.
        // We keep track of the best (smallest) depth found so far.
        int bestDepth = int.MaxValue;
        int answerId = -1;

        foreach (var node in preorder)
        {
            int nodeDepth = depth[node];
            int nodeSubtreeSize = subtreeSize[node];

            // Check whether this employee satisfies the required condition.
            if (nodeSubtreeSize == nodeDepth)
            {
                // If this is the first valid node we found at a shallower depth than any previous one,
                // it becomes the new answer.
                //
                // We use strictly smaller depth here.
                // Why not <= ?
                // Because if another valid node appears later at the same depth,
                // we must keep the earlier one due to the left-to-right tie-break rule.
                if (nodeDepth < bestDepth)
                {
                    bestDepth = nodeDepth;
                    answerId = node.Id;
                }
            }
        }

        // If no valid employee was found, answerId remains -1.
        return answerId;
    }
}

// -------------------------
// Demo code
// -------------------------

// Example 1 construction:
//
// Intended hierarchy:
// 10
// ├── 20
// │   └── 40
// └── 30
//     ├── 50
//     │   └── 60
//
// Depths:
// 10 -> 0
// 20, 30 -> 1
// 40, 50 -> 2
// 60 -> 3
//
// Subtree sizes:
// 40 -> 1
// 20 -> 2
// 60 -> 1
// 50 -> 2   <-- matches depth 2
// 30 -> 3
// 10 -> 6
//
// So the correct answer is 50.
var n10 = new EmployeeNode(10);
var n20 = new EmployeeNode(20);
var n30 = new EmployeeNode(30);
var n40 = new EmployeeNode(40);
var n50 = new EmployeeNode(50);
var n60 = new EmployeeNode(60);

n10.Children.Add(n20);
n10.Children.Add(n30);
n20.Children.Add(n40);
n30.Children.Add(n50);
n50.Children.Add(n60);

// Example 2 construction:
//
// 1
// ├── 2
// ├── 3
// └── 4
//
// Depths:
// 1 -> 0
// 2,3,4 -> 1
//
// Subtree sizes:
// 2,3,4 -> 1
// 1 -> 4
//
// Condition subtree size == depth:
// 1: 4 != 0
// 2: 1 == 1
// 3: 1 == 1
// 4: 1 == 1
//
// According to the exact mathematical condition, the shallowest valid depth is 1,
// and the first left-to-right valid node is 2.
var m1 = new EmployeeNode(1);
var m2 = new EmployeeNode(2);
var m3 = new EmployeeNode(3);
var m4 = new EmployeeNode(4);

m1.Children.Add(m2);
m1.Children.Add(m3);
m1.Children.Add(m4);

var solution = new Solution();

Console.WriteLine(solution.FindFirstManagerWhoseTeamSizeMatchesDepth(n10));
Console.WriteLine(solution.FindFirstManagerWhoseTeamSizeMatchesDepth(m1));