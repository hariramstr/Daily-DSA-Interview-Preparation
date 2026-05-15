/*
 * Cousin Nodes at Same Depth
 * ==========================
 * Given the root of a binary tree and two node values x and y,
 * determine whether the nodes with these values are COUSINS.
 *
 * Two nodes are cousins if:
 *   1. They are at the SAME DEPTH in the tree.
 *   2. They have DIFFERENT PARENT nodes.
 *
 * Note: Siblings (same parent, same depth) are NOT cousins.
 *
 * Example 1:
 *   Tree: [1, 2, 3, 4, 5, null, null], x=4, y=5
 *   Output: false  (4 and 5 share parent 2 → siblings, not cousins)
 *
 * Example 2:
 *   Tree: [1, 2, 3, null, 4, null, 5], x=4, y=5
 *   Output: true   (4's parent=2, 5's parent=3, both at depth 2 → cousins)
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// TreeNode definition — the standard binary tree node structure
// ─────────────────────────────────────────────────────────────────────────────
public class TreeNode
{
    public int Val;
    public TreeNode? Left;
    public TreeNode? Right;

    public TreeNode(int val = 0, TreeNode? left = null, TreeNode? right = null)
    {
        Val   = val;
        Left  = left;
        Right = right;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Solution class containing the cousin-check algorithm
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /*
     * IsCousins — BFS (level-order traversal) approach
     *
     * Time Complexity : O(N)  — we visit every node at most once.
     * Space Complexity: O(N)  — the queue can hold up to N/2 nodes (widest level).
     *
     * Strategy:
     *   We perform a Breadth-First Search (BFS) level by level.
     *   For each node we process, we check its LEFT and RIGHT children.
     *   If a child matches x or y, we record:
     *     • The DEPTH at which the match was found.
     *     • The PARENT of the matched node.
     *   After scanning the entire tree, we return true only if:
     *     • Both x and y were found at the SAME depth, AND
     *     • They have DIFFERENT parents.
     */
    public bool IsCousins(TreeNode root, int x, int y)
    {
        // ── Step 1: Initialise tracking variables ─────────────────────────────
        // We need to remember, for each of x and y:
        //   • Which depth (level) it lives on.
        //   • Who its parent is (stored as the parent's value for easy comparison).
        // Using -1 as a sentinel meaning "not found yet".
        int depthX  = -1, depthY  = -1;
        int parentX = -1, parentY = -1;

        // ── Step 2: Set up the BFS queue ──────────────────────────────────────
        // A Queue<TreeNode> gives us FIFO order, which is exactly what BFS needs
        // so that we process all nodes on level k before any node on level k+1.
        Queue<TreeNode> queue = new Queue<TreeNode>();

        // Enqueue the root to kick off the traversal.
        // The root has no parent, so we'll handle it as a special case below.
        queue.Enqueue(root);

        // ── Step 3: Track the current depth ───────────────────────────────────
        // We start at depth 0 (the root's level).
        int currentDepth = 0;

        // ── Step 4: BFS loop — process one LEVEL at a time ───────────────────
        // We use the "snapshot the queue size" trick:
        //   At the start of each iteration, levelSize = number of nodes on
        //   the current level. We dequeue exactly that many nodes, then
        //   increment the depth counter before moving to the next level.
        while (queue.Count > 0)
        {
            // How many nodes are on the current level?
            int levelSize = queue.Count;

            // Process every node on this level.
            for (int i = 0; i < levelSize; i++)
            {
                // Dequeue the next node to examine.
                TreeNode node = queue.Dequeue();

                // ── Step 5: Check the node's children for x and y ────────────
                // We inspect children HERE (not the node itself) so that we
                // naturally know the parent of any match we find.

                // Check the LEFT child.
                if (node.Left != null)
                {
                    // If the left child's value is x, record its depth and parent.
                    if (node.Left.Val == x)
                    {
                        depthX  = currentDepth + 1; // child is one level deeper
                        parentX = node.Val;          // current node is its parent
                    }
                    // If the left child's value is y, record its depth and parent.
                    if (node.Left.Val == y)
                    {
                        depthY  = currentDepth + 1;
                        parentY = node.Val;
                    }

                    // Enqueue the left child so we can later inspect ITS children.
                    queue.Enqueue(node.Left);
                }

                // Check the RIGHT child (same logic as left child above).
                if (node.Right != null)
                {
                    if (node.Right.Val == x)
                    {
                        depthX  = currentDepth + 1;
                        parentX = node.Val;
                    }
                    if (node.Right.Val == y)
                    {
                        depthY  = currentDepth + 1;
                        parentY = node.Val;
                    }

                    // Enqueue the right child for future processing.
                    queue.Enqueue(node.Right);
                }
            }

            // ── Step 6: Move to the next depth level ─────────────────────────
            currentDepth++;

            // ── Step 7: Early-exit optimisation ──────────────────────────────
            // If we have already found BOTH nodes, there is no need to continue
            // scanning deeper levels — we have all the information we need.
            if (depthX != -1 && depthY != -1)
                break;
        }

        // ── Step 8: Handle the edge case where x or y is the ROOT ────────────
        // The root has no parent and no sibling, so it can never be a cousin.
        // Our loop above never sets depthX/depthY for the root (we only check
        // children), so if either value equals root.Val, depthX or depthY
        // remains -1, and the final condition below will correctly return false.
        // (No extra code needed — the sentinel value handles it automatically.)

        // ── Step 9: Final cousin check ────────────────────────────────────────
        // Cousins must satisfy TWO conditions simultaneously:
        //   Condition A — same depth  : depthX == depthY
        //   Condition B — diff parent : parentX != parentY
        // Both must be true; if either fails, they are not cousins.
        return depthX == depthY && parentX != parentY;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / test code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

Solution sol = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// Tree layout (level-order): [1, 2, 3, 4, 5, null, null]
//
//         1          ← depth 0
//        / \
//       2   3        ← depth 1
//      / \
//     4   5          ← depth 2
//
// x=4, y=5 → both at depth 2, but SAME parent (node 2) → NOT cousins → false
TreeNode root1 = new TreeNode(1,
    new TreeNode(2,
        new TreeNode(4),
        new TreeNode(5)),
    new TreeNode(3));

bool result1 = sol.IsCousins(root1, 4, 5);
Console.WriteLine("Example 1:");
Console.WriteLine($"  x=4, y=5 → IsCousins = {result1}");
Console.WriteLine($"  Expected : False");
Console.WriteLine($"  Pass     : {result1 == false}");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// Tree layout (level-order): [1, 2, 3, null, 4, null, 5]
//
//         1          ← depth 0
//        / \
//       2   3        ← depth 1
//        \    \
//         4    5     ← depth 2
//
// x=4, y=5 → both at depth 2, DIFFERENT parents (2 and 3) → cousins → true
TreeNode root2 = new TreeNode(1,
    new TreeNode(2,
        null,
        new TreeNode(4)),
    new TreeNode(3,
        null,
        new TreeNode(5)));

bool result2 = sol.IsCousins(root2, 4, 5);
Console.WriteLine("Example 2:");
Console.WriteLine($"  x=4, y=5 → IsCousins = {result2}");
Console.WriteLine($"  Expected : True");
Console.WriteLine($"  Pass     : {result2 == true}");
Console.WriteLine();

// ── Extra test: siblings at depth 1 ──────────────────────────────────────────
// Tree: [1, 2, 3]
//         1
//        / \
//       2   3
// x=2, y=3 → same depth (1), same parent (1) → NOT cousins → false
TreeNode root3 = new TreeNode(1,
    new TreeNode(2),
    new TreeNode(3));

bool result3 = sol.IsCousins(root3, 2, 3);
Console.WriteLine("Extra Test (siblings at depth 1):");
Console.WriteLine($"  x=2, y=3 → IsCousins = {result3}");
Console.WriteLine($"  Expected : False");
Console.WriteLine($"  Pass     : {result3 == false}");
Console.WriteLine();

// ── Extra test: different depths ─────────────────────────────────────────────
// Tree:
//         1
//        / \
//       2   3
//      /
//     4
// x=3, y=4 → depth 1 vs depth 2 → NOT cousins → false
TreeNode root4 = new TreeNode(1,
    new TreeNode(2,
        new TreeNode(4),
        null),
    new TreeNode(3));

bool result4 = sol.IsCousins(root4, 3, 4);
Console.WriteLine("Extra Test (different depths):");
Console.WriteLine($"  x=3, y=4 → IsCousins = {result4}");
Console.WriteLine($"  Expected : False");
Console.WriteLine($"  Pass     : {result4 == false}");