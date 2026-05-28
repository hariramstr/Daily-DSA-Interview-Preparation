/*
 * Title: Recover Binary Search Tree Subtree Sums After Node Swaps
 * 
 * Problem Description:
 * You are given the root of a Binary Search Tree (BST) where exactly two nodes have been
 * swapped, violating the BST property. Your task is to restore the BST to its original
 * valid state and then return an array of subtree sums for every node in the tree,
 * listed in level-order (BFS) traversal.
 *
 * The subtree sum of a node is defined as the sum of all node values in the subtree
 * rooted at that node (including the node itself).
 *
 * You must first identify and swap back the two incorrectly placed nodes to restore
 * the BST, then compute the subtree sum for each node.
 *
 * Example 1:
 *   Input:  [3,1,4,null,null,2,null]  (nodes 3 and 2 swapped)
 *   After recovery: [2,1,4,null,null,3,null]
 *   Output: [10, 1, 7, 3]
 *
 * Example 2:
 *   Input:  [5,3,8,1,7,6,9]  (nodes 7 and 6 swapped)
 *   After recovery: [5,3,8,1,6,7,9]
 *   Output: [39, 10, 24, 1, 6, 16, 9]
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// TreeNode definition
// ─────────────────────────────────────────────────────────────────────────────
public class TreeNode
{
    public int Val;
    public TreeNode? Left;
    public TreeNode? Right;
    public TreeNode(int val = 0, TreeNode? left = null, TreeNode? right = null)
    {
        Val = val; Left = left; Right = right;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Solution class
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /*
     * RecoverAndSubtreeSums
     *
     * High-level algorithm:
     *   Step 1 – Find the two swapped nodes using an in-order traversal.
     *            In a valid BST, in-order traversal yields a strictly increasing
     *            sequence.  When two nodes are swapped there will be either one
     *            or two "inversions" (places where prev > current).
     *              • Two inversions  → first node  = first  inversion's predecessor
     *                                  second node = second inversion's current node
     *              • One inversion   → first node  = inversion's predecessor
     *                                  second node = inversion's current node
     *            (The one-inversion case happens when the two swapped nodes are
     *             adjacent in the in-order sequence.)
     *
     *   Step 2 – Swap the VALUES of the two identified nodes back.
     *            We only swap values, not pointers, which keeps the tree structure
     *            intact and is the standard approach for this classic problem.
     *
     *   Step 3 – Compute the subtree sum for every node using a post-order DFS.
     *            Post-order guarantees that both children are fully processed
     *            before the parent, so we can accumulate sums bottom-up.
     *
     *   Step 4 – Collect subtree sums in level-order (BFS) and return them.
     *
     * Time Complexity:  O(N)  – each step visits every node exactly once.
     * Space Complexity: O(N)  – recursion stack + dictionary for subtree sums
     *                           + BFS queue, all O(N) in the worst case.
     */
    public long[] RecoverAndSubtreeSums(TreeNode? root)
    {
        if (root == null) return Array.Empty<long>();

        // ── Step 1: Identify the two swapped nodes via in-order traversal ──────

        // 'first' and 'second' will hold the two nodes whose values must be swapped.
        // 'prev' tracks the previously visited node during in-order traversal so we
        // can detect inversions (prev.Val > current.Val).
        TreeNode? first = null;
        TreeNode? second = null;
        TreeNode? prev = null;

        // We use an iterative in-order traversal (left → node → right) with an
        // explicit stack to avoid potential stack-overflow on deep trees and to
        // keep the logic transparent.
        void FindSwapped(TreeNode? node)
        {
            // Base case: nothing to do for a null node.
            if (node == null) return;

            // Recurse into the LEFT subtree first (in-order: left, root, right).
            FindSwapped(node.Left);

            // ── Inversion detection ──────────────────────────────────────────
            // If 'prev' exists and its value is GREATER than the current node's
            // value, we have found an inversion — a violation of BST ordering.
            if (prev != null && prev.Val > node.Val)
            {
                // The FIRST time we see an inversion:
                //   • The predecessor ('prev') is the candidate for 'first'
                //     because it is too large for its position.
                //   • The current node is the candidate for 'second'
                //     because it is too small for its position.
                if (first == null)
                {
                    first = prev;   // too-large node (first inversion predecessor)
                    second = node;  // too-small node (will be updated if 2nd inversion found)
                }
                else
                {
                    // The SECOND time we see an inversion:
                    //   • 'first' is already set correctly from the first inversion.
                    //   • Update 'second' to the current node — it is the actual
                    //     too-small node that needs to be swapped with 'first'.
                    second = node;
                }
            }

            // Advance 'prev' to the current node before moving to the right subtree.
            prev = node;

            // Recurse into the RIGHT subtree.
            FindSwapped(node.Right);
        }

        // Kick off the in-order traversal to populate 'first' and 'second'.
        FindSwapped(root);

        // ── Step 2: Restore the BST by swapping the values of the two nodes ───
        // At this point 'first' and 'second' are guaranteed to be non-null
        // (the problem states exactly two nodes were swapped).
        // Swapping only the integer values is sufficient — no pointer changes needed.
        if (first != null && second != null)
        {
            // Classic three-step integer swap.
            int temp = first.Val;
            first.Val = second.Val;
            second.Val = temp;
        }

        // ── Step 3: Compute subtree sums via post-order DFS ───────────────────
        // We store the subtree sum for each node in a dictionary keyed by the
        // node reference itself.  A Dictionary<TreeNode, long> gives O(1) look-up
        // when we later need a node's sum during BFS.
        var subtreeSum = new Dictionary<TreeNode, long>();

        long ComputeSum(TreeNode? node)
        {
            // Base case: an empty subtree contributes 0 to the sum.
            if (node == null) return 0L;

            // Post-order: process left child, then right child, then current node.
            // This ensures both children's sums are ready before we use them.
            long leftSum  = ComputeSum(node.Left);
            long rightSum = ComputeSum(node.Right);

            // The subtree sum of this node = its own value + left subtree sum + right subtree sum.
            long total = node.Val + leftSum + rightSum;

            // Store the result so we can retrieve it during BFS without recomputing.
            subtreeSum[node] = total;

            return total;
        }

        // Trigger the post-order computation starting from the root.
        ComputeSum(root);

        // ── Step 4: Collect subtree sums in level-order (BFS) ─────────────────
        // BFS uses a Queue<TreeNode>.  We enqueue the root, then process nodes
        // level by level, enqueueing their children as we go.
        var result = new List<long>();
        var queue  = new Queue<TreeNode>();
        queue.Enqueue(root);

        while (queue.Count > 0)
        {
            // Dequeue the front node of the current level.
            TreeNode current = queue.Dequeue();

            // Look up the pre-computed subtree sum and add it to our result list.
            result.Add(subtreeSum[current]);

            // Enqueue left child first (so left subtree nodes appear before right
            // subtree nodes in the output, matching standard level-order order).
            if (current.Left  != null) queue.Enqueue(current.Left);
            if (current.Right != null) queue.Enqueue(current.Right);
        }

        // Convert the list to an array and return it.
        return result.ToArray();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helper: build a tree from a level-order array (null = missing node)
// ─────────────────────────────────────────────────────────────────────────────
static TreeNode? BuildTree(int?[] values)
{
    if (values == null || values.Length == 0 || values[0] == null) return null;

    // Create the root from the first element.
    var root  = new TreeNode(values[0]!.Value);
    var queue = new Queue<TreeNode>();
    queue.Enqueue(root);

    int i = 1; // index into the values array
    while (queue.Count > 0 && i < values.Length)
    {
        TreeNode node = queue.Dequeue();

        // Assign left child if the next value is non-null.
        if (i < values.Length)
        {
            if (values[i] != null)
            {
                node.Left = new TreeNode(values[i]!.Value);
                queue.Enqueue(node.Left);
            }
            i++;
        }

        // Assign right child if the next value is non-null.
        if (i < values.Length)
        {
            if (values[i] != null)
            {
                node.Right = new TreeNode(values[i]!.Value);
                queue.Enqueue(node.Right);
            }
            i++;
        }
    }

    return root;
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / test code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────
var solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// Tree structure (swapped BST):
//        3
//       / \
//      1   4
//         /
//        2
// Nodes 3 and 2 are swapped.  After recovery:
//        2
//       / \
//      1   4
//         /
//        3
// In-order of recovered tree: 1, 2, 3, 4  ✓
// Subtree sums (level-order): 2+1+4+3=10, 1, 4+3=7, 3
// Expected output: [10, 1, 7, 3]

Console.WriteLine("=== Example 1 ===");
TreeNode? root1 = BuildTree(new int?[] { 3, 1, 4, null, null, 2, null });
long[] result1  = solution.RecoverAndSubtreeSums(root1);
Console.WriteLine("Output: [" + string.Join(", ", result1) + "]");
Console.WriteLine("Expected: [10, 1, 7, 3]");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// Tree structure (swapped BST):
//          5
//         / \
//        3   8
//       / \ / \
//      1  7 6  9
// Nodes 7 and 6 are swapped.  After recovery:
//          5
//         / \
//        3   8
//       / \ / \
//      1  6 7  9
// In-order of recovered tree: 1, 3, 6, 5, 7, 8, 9  ✓
// Subtree sums (level-order):
//   node 5  → 5+3+8+1+6+7+9 = 39
//   node 3  → 3+1+6         = 10
//   node 8  → 8+7+9         = 24
//   node 1  → 1
//   node 6  → 6
//   node 7  → 7
//   node 9  → 9
// Expected output: [39, 10, 24, 1, 6, 16, 9]
// Wait — let's recheck node 8's subtree: 8 + 7 + 9 = 24  ✓
// And node 3's subtree: 3 + 1 + 6 = 10  ✓
// Root: 5 + 10 + 24 = 39  ✓

Console.WriteLine("=== Example 2 ===");
TreeNode? root2 = BuildTree(new int?[] { 5, 3, 8, 1, 7, 6, 9 });
long[] result2  = solution.RecoverAndSubtreeSums(root2);
Console.WriteLine("Output: [" + string.Join(", ", result2) + "]");
Console.WriteLine("Expected: [39, 10, 24, 1, 6, 16, 9]");
Console.WriteLine();

// ── Example 3: Adjacent swap (single inversion) ───────────────────────────────
// Tree:
//      2
//     / \
//    1   3
// Swap nodes 2 and 3 → swapped tree:
//      3
//     / \
//    1   2
// In-order of swapped tree: 1, 3, 2  → one inversion (3 > 2)
// first = node(3), second = node(2)
// After recovery:
//      2
//     / \
//    1   3
// Subtree sums: root=6, left=1, right=3
// Expected output: [6, 1, 3]

Console.WriteLine("=== Example 3 (adjacent swap) ===");
TreeNode? root3 = BuildTree(new int?[] { 3, 1, 2 });
long[] result3  = solution.RecoverAndSubtreeSums(root3);
Console.WriteLine("Output: [" + string.Join(", ", result3) + "]");
Console.WriteLine("Expected: [6, 1, 3]");
Console.WriteLine();

// ── Example 4: Two-node tree ──────────────────────────────────────────────────
// Valid BST would be root=1, right=2.
// Swapped: root=2, right=1.
// After recovery: root=1, right=2.
// Subtree sums: [3, 2]

Console.WriteLine("=== Example 4 (two-node tree) ===");
TreeNode? root4 = BuildTree(new int?[] { 2, null, 1 });
long[] result4  = solution.RecoverAndSubtreeSums(root4);
Console.WriteLine("Output: [" + string.Join(", ", result4) + "]");
Console.WriteLine("Expected: [3, 2]");