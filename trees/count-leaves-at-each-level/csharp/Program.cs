/*
 * Title: Count Leaves at Each Level
 * Difficulty: Easy
 * Topic: Trees
 *
 * Problem Description:
 * Given the root of a binary tree, return a list where each element represents
 * the number of leaf nodes at that level of the tree. Levels are 0-indexed,
 * starting from the root at level 0.
 *
 * A leaf node is defined as a node with no left or right children.
 *
 * For example, if the root itself has no children, it is a leaf at level 0,
 * so the result would be [1].
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [1, 1000].
 * - -1000 <= Node.val <= 1000
 * - The tree may be skewed (i.e., all nodes on one side).
 */

using System;
using System.Collections.Generic;

// ─── Tree Node Definition ───────────────────────────────────────────────────

/// <summary>
/// Represents a single node in a binary tree.
/// Each node holds an integer value and optional references to left/right children.
/// </summary>
public class TreeNode
{
    public int Val;
    public TreeNode? Left;
    public TreeNode? Right;

    public TreeNode(int val = 0, TreeNode? left = null, TreeNode? right = null)
    {
        Val = val;
        Left = left;
        Right = right;
    }
}

// ─── Solution Class ─────────────────────────────────────────────────────────

/// <summary>
/// Contains the algorithm to count leaf nodes at each level of a binary tree.
/// </summary>
public class Solution
{
    /// <summary>
    /// Returns a list where each index i contains the count of leaf nodes at level i.
    ///
    /// Time Complexity:  O(n) — we visit every node exactly once during BFS traversal.
    /// Space Complexity: O(n) — in the worst case (a complete binary tree), the queue
    ///                          holds up to n/2 nodes at the last level, which is O(n).
    ///                          The result list also holds one entry per level, O(h)
    ///                          where h is the height of the tree.
    /// </summary>
    /// <param name="root">The root of the binary tree.</param>
    /// <returns>A list of leaf counts indexed by level (0-indexed).</returns>
    public List<int> CountLeavesAtEachLevel(TreeNode? root)
    {
        // ── Step 1: Handle the edge case of an empty tree ──────────────────
        // If the root is null, there are no nodes and therefore no levels.
        // We return an empty list immediately to avoid null-reference errors below.
        var result = new List<int>();
        if (root == null)
            return result;

        // ── Step 2: Set up a queue for Breadth-First Search (BFS) ──────────
        // BFS (also called level-order traversal) processes nodes level by level,
        // which is exactly what we need — we want to count leaves PER level.
        //
        // Why a Queue?
        //   A queue is a FIFO (First-In, First-Out) structure. When we enqueue
        //   all children of the current level, they will be dequeued (processed)
        //   before any of their own children, naturally giving us level-by-level order.
        var queue = new Queue<TreeNode>();

        // Seed the queue with the root node so we start at level 0.
        queue.Enqueue(root);

        // ── Step 3: Process the tree level by level ─────────────────────────
        // We loop as long as there are nodes waiting to be processed.
        // Each iteration of this outer while-loop handles ONE complete level.
        while (queue.Count > 0)
        {
            // ── Step 3a: Determine how many nodes are on the current level ──
            // At the start of each iteration, every node currently in the queue
            // belongs to the SAME level. We capture this count so we know exactly
            // how many nodes to dequeue before moving to the next level.
            int levelSize = queue.Count;

            // This counter tracks how many of those nodes are leaves.
            int leafCount = 0;

            // ── Step 3b: Process every node at the current level ────────────
            for (int i = 0; i < levelSize; i++)
            {
                // Remove the front node from the queue for processing.
                TreeNode current = queue.Dequeue();

                // ── Step 3c: Check if this node is a leaf ───────────────────
                // A leaf node has NO left child AND NO right child.
                // If both children are null, this node contributes 1 to leafCount.
                if (current.Left == null && current.Right == null)
                {
                    leafCount++;
                    // Leaf nodes have no children to enqueue, so we continue
                    // to the next node at this level.
                    continue;
                }

                // ── Step 3d: Enqueue children for the NEXT level ────────────
                // If the current node is NOT a leaf, it has at least one child.
                // We enqueue any non-null children so they will be processed
                // in the next iteration of the outer while-loop (i.e., next level).
                if (current.Left != null)
                    queue.Enqueue(current.Left);

                if (current.Right != null)
                    queue.Enqueue(current.Right);
            }

            // ── Step 3e: Record the leaf count for this level ───────────────
            // After processing all nodes at the current level, we append the
            // leaf count to our result list. The index in the list naturally
            // corresponds to the level number (0-indexed) because we add one
            // entry per level in order.
            result.Add(leafCount);
        }

        // ── Step 4: Return the completed result list ─────────────────────────
        // result[0] = leaf count at level 0
        // result[1] = leaf count at level 1
        // ... and so on up to the deepest level.
        return result;
    }
}

// ─── Helper: Build Tree from Level-Order Array ──────────────────────────────

/// <summary>
/// Utility class to build a binary tree from an array representation
/// (the same format LeetCode uses, where null means "no node").
/// </summary>
public static class TreeBuilder
{
    /// <summary>
    /// Builds a binary tree from a level-order (BFS) integer array.
    /// Null entries in the array represent missing nodes.
    /// </summary>
    public static TreeNode? Build(int?[] values)
    {
        if (values == null || values.Length == 0 || values[0] == null)
            return null;

        // Create the root from the first element.
        var root = new TreeNode(values[0]!.Value);
        var queue = new Queue<TreeNode>();
        queue.Enqueue(root);

        int index = 1; // Points to the next value in the array to assign.

        while (queue.Count > 0 && index < values.Length)
        {
            TreeNode node = queue.Dequeue();

            // Assign left child if the next value exists and is not null.
            if (index < values.Length)
            {
                if (values[index] != null)
                {
                    node.Left = new TreeNode(values[index]!.Value);
                    queue.Enqueue(node.Left);
                }
                index++;
            }

            // Assign right child if the next value exists and is not null.
            if (index < values.Length)
            {
                if (values[index] != null)
                {
                    node.Right = new TreeNode(values[index]!.Value);
                    queue.Enqueue(node.Right);
                }
                index++;
            }
        }

        return root;
    }
}

// ─── Demo / Test Code ───────────────────────────────────────────────────────

var solution = new Solution();

// ── Example 1 ───────────────────────────────────────────────────────────────
// Tree structure:
//         3
//        / \
//       9  20
//          / \
//         15   7
//
// Level 0: node 3  → not a leaf (has children)  → 0 leaves
// Level 1: node 9  → leaf (no children)          → 1 leaf
//          node 20 → not a leaf (has children)
// Level 2: node 15 → leaf                        → 2 leaves
//          node 7  → leaf
// Expected output: [0, 1, 2]

Console.WriteLine("=== Example 1 ===");
Console.WriteLine("Input tree: [3, 9, 20, null, null, 15, 7]");

TreeNode? tree1 = TreeBuilder.Build(new int?[] { 3, 9, 20, null, null, 15, 7 });
List<int> result1 = solution.CountLeavesAtEachLevel(tree1);

Console.Write("Output: [");
Console.Write(string.Join(", ", result1));
Console.WriteLine("]");
Console.WriteLine("Expected: [0, 1, 2]");
Console.WriteLine();

// ── Example 2 ───────────────────────────────────────────────────────────────
// Tree structure:
//         1
//        / \
//       2   3
//      /
//     4
//
// Level 0: node 1 → not a leaf (has children)   → 0 leaves
// Level 1: node 2 → not a leaf (has left child)
//          node 3 → leaf (no children)           → 1 leaf
// Level 2: node 4 → leaf                         → 1 leaf
// Expected output: [0, 1, 1]

Console.WriteLine("=== Example 2 ===");
Console.WriteLine("Input tree: [1, 2, 3, 4, null, null, null]");

TreeNode? tree2 = TreeBuilder.Build(new int?[] { 1, 2, 3, 4, null, null, null });
List<int> result2 = solution.CountLeavesAtEachLevel(tree2);

Console.Write("Output: [");
Console.Write(string.Join(", ", result2));
Console.WriteLine("]");
Console.WriteLine("Expected: [0, 1, 1]");
Console.WriteLine();

// ── Example 3: Single node (root is a leaf) ─────────────────────────────────
// Tree structure:
//     42
//
// Level 0: node 42 → leaf (no children) → 1 leaf
// Expected output: [1]

Console.WriteLine("=== Example 3: Single Node ===");
Console.WriteLine("Input tree: [42]");

TreeNode? tree3 = TreeBuilder.Build(new int?[] { 42 });
List<int> result3 = solution.CountLeavesAtEachLevel(tree3);

Console.Write("Output: [");
Console.Write(string.Join(", ", result3));
Console.WriteLine("]");
Console.WriteLine("Expected: [1]");
Console.WriteLine();

// ── Example 4: Skewed (right-only) tree ─────────────────────────────────────
// Tree structure:
//     1
//      \
//       2
//        \
//         3
//
// Level 0: node 1 → not a leaf → 0 leaves
// Level 1: node 2 → not a leaf → 0 leaves
// Level 2: node 3 → leaf       → 1 leaf
// Expected output: [0, 0, 1]

Console.WriteLine("=== Example 4: Right-Skewed Tree ===");
Console.WriteLine("Input tree: [1, null, 2, null, 3]");

// Build manually for clarity with a skewed tree.
var skewedRoot = new TreeNode(1);
skewedRoot.Right = new TreeNode(2);
skewedRoot.Right.Right = new TreeNode(3);

List<int> result4 = solution.CountLeavesAtEachLevel(skewedRoot);

Console.Write("Output: [");
Console.Write(string.Join(", ", result4));
Console.WriteLine("]");
Console.WriteLine("Expected: [0, 0, 1]");
Console.WriteLine();

// ── Example 5: Null root ─────────────────────────────────────────────────────
Console.WriteLine("=== Example 5: Null Root ===");
Console.WriteLine("Input tree: null");

List<int> result5 = solution.CountLeavesAtEachLevel(null);

Console.Write("Output: [");
Console.Write(string.Join(", ", result5));
Console.WriteLine("]");
Console.WriteLine("Expected: []");