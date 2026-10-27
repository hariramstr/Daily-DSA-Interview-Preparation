/*
Title: Find the Deepest Leftmost Incomplete Department

Problem Description:
A company models its department structure as a binary tree. Each node represents a department
and stores a unique integer department ID. The left child is the first sub-department and the
right child is the second sub-department. A department is considered incomplete if it has
exactly one direct sub-department (either left or right, but not both).

Your task is to return the department ID of the incomplete department that appears at the
greatest depth in the tree. If multiple incomplete departments exist at the same deepest depth,
return the leftmost one among them. The root is at depth 0, its children are at depth 1, and so on.

If there is no incomplete department in the tree, return -1.

This problem tests careful tree traversal and tie-breaking logic. A correct solution should
identify incompleteness locally at each node, but compare candidates globally by depth first
and horizontal order second. The tree can be large, so your solution should run efficiently.

Constraints:
- The number of nodes in the tree is in the range [0, 100000].
- Node values are unique integers in the range [-1000000000, 1000000000].
- The input tree is a binary tree and may be empty.

Important correctness note:
To satisfy "deepest first, and if tied choose the leftmost", a level-order traversal (BFS)
is the most natural approach. BFS visits nodes level by level from left to right. Therefore:
- The first incomplete node we encounter at a given depth is the leftmost incomplete node at that depth.
- If we continue processing deeper levels, any incomplete node found later at a deeper depth
  should replace the previous answer.
- If we find another incomplete node at the same depth later, we should NOT replace the answer,
  because the earlier one is more leftmost.

Example 1:
Input: root = [10,5,20,3,null,15,30,null,4]
Output: 3

Example 2:
Input: root = [8,4,12,2,6,null,14,1]
Correct Output: 2
Reason:
- Node 12 is incomplete (only right child).
- Node 2 is incomplete (only left child).
- Both are at depth 2.
- The leftmost incomplete node at that deepest depth is 2.
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
    - We visit each node exactly once.

    Space Complexity: O(w)
    - The queue used by BFS stores at most one level of the tree at a time.
    - In the worst case, this can be O(n), where w is the maximum width of the tree.

    Beginner-friendly idea:
    We use Breadth-First Search (BFS), also called level-order traversal.

    Why BFS is a great fit here:
    1. We care about depth, and BFS naturally processes nodes depth by depth.
    2. We care about "leftmost" among nodes at the same depth, and BFS naturally visits
       nodes from left to right within each level if we enqueue left child before right child.

    Core rule:
    - If a node is incomplete (exactly one child), it is a candidate.
    - If this candidate is deeper than our current best candidate, it becomes the new answer.
    - If it is at the same depth as our current best candidate, we keep the old answer,
      because the old one was seen earlier in left-to-right order and is therefore more leftmost.
    */
    public int FindDeepestLeftmostIncompleteDepartment(TreeNode? root)
    {
        // If the tree is empty, there are no departments at all,
        // so there cannot be any incomplete department.
        if (root == null)
        {
            return -1;
        }

        // This queue will drive our BFS traversal.
        // Each queue entry stores:
        // - the current node
        // - the depth of that node
        //
        // We store depth explicitly so that we can compare candidates easily.
        var queue = new Queue<(TreeNode node, int depth)>();

        // Start BFS from the root at depth 0.
        queue.Enqueue((root, 0));

        // This will store the best answer found so far.
        // We initialize to -1 because that is the required return value
        // when no incomplete node exists.
        int answerValue = -1;

        // This tracks the depth of the best incomplete node found so far.
        // We start with -1 so that any real node depth (0 or more) is deeper.
        int bestDepth = -1;

        // Standard BFS loop:
        // Keep processing until there are no more nodes left to visit.
        while (queue.Count > 0)
        {
            // Remove the next node from the front of the queue.
            // Because this is BFS, nodes come out in level-order:
            // top to bottom, and left to right within each level.
            var (node, depth) = queue.Dequeue();

            // A node is incomplete if it has exactly one child.
            //
            // Let's compute that carefully:
            // - node.left == null and node.right != null  => one child
            // - node.left != null and node.right == null  => one child
            //
            // The XOR-like check below means "exactly one is null".
            bool isIncomplete = (node.left == null) != (node.right == null);

            // If this node is incomplete, it is a candidate answer.
            if (isIncomplete)
            {
                // We only replace the current answer if this node is deeper.
                //
                // Why only deeper?
                // Because if another incomplete node appears later at the same depth,
                // it is to the right of the earlier one in BFS order, so it is NOT leftmost.
                if (depth > bestDepth)
                {
                    bestDepth = depth;
                    answerValue = node.val;
                }
            }

            // For BFS left-to-right correctness, we must enqueue the left child first,
            // then the right child.
            //
            // This ordering is essential:
            // it guarantees that within the same depth, nodes are processed from left to right.
            if (node.left != null)
            {
                queue.Enqueue((node.left, depth + 1));
            }

            if (node.right != null)
            {
                queue.Enqueue((node.right, depth + 1));
            }
        }

        // If no incomplete node was ever found, answerValue is still -1.
        // Otherwise, it contains the deepest leftmost incomplete department ID.
        return answerValue;
    }
}

static TreeNode? BuildTree(params int?[] values)
{
    // This helper builds a binary tree from a level-order array representation.
    //
    // Example:
    // [10, 5, 20, 3, null, 15, 30, null, 4]
    //
    // Means:
    //               10
    //             /    \
    //            5      20
    //           /      /  \
    //          3      15  30
    //           \
    //            4
    //
    // This is only demo/support code so we can easily create sample inputs.

    if (values == null || values.Length == 0 || values[0] == null)
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
// Input: [10,5,20,3,null,15,30,null,4]
// Incomplete nodes:
// - 5 has only left child -> depth 1
// - 3 has only right child -> depth 2
// Deepest is 3, so answer should be 3.
var root1 = BuildTree(10, 5, 20, 3, null, 15, 30, null, 4);
Console.WriteLine(solution.FindDeepestLeftmostIncompleteDepartment(root1)); // Expected: 3

// Example 2:
// Input: [8,4,12,2,6,null,14,1]
// Incomplete nodes:
// - 12 has only right child -> depth 2
// - 2 has only left child -> depth 2
// Same deepest depth, choose leftmost -> 2
var root2 = BuildTree(8, 4, 12, 2, 6, null, 14, 1);
Console.WriteLine(solution.FindDeepestLeftmostIncompleteDepartment(root2)); // Expected: 2

// Demo 3: no incomplete nodes
// Perfect small tree: [1,2,3]
// Every non-leaf has either 2 children or 0 children, so answer is -1.
var root3 = BuildTree(1, 2, 3);
Console.WriteLine(solution.FindDeepestLeftmostIncompleteDepartment(root3)); // Expected: -1

// Demo 4: empty tree
TreeNode? root4 = null;
Console.WriteLine(solution.FindDeepestLeftmostIncompleteDepartment(root4)); // Expected: -1

// Demo 5: root itself is incomplete
// [42, 7, null] -> root has only left child, so answer is 42
var root5 = BuildTree(42, 7, null);
Console.WriteLine(solution.FindDeepestLeftmostIncompleteDepartment(root5)); // Expected: 42