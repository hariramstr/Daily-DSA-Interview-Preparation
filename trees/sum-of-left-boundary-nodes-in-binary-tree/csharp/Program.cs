/*
 * Title: Sum of Left Boundary Nodes in Binary Tree
 *
 * Problem Description:
 * Given the root of a binary tree, return the sum of all node values that lie
 * on the left boundary of the tree.
 *
 * The left boundary is defined as the path from the root down to the leftmost
 * leaf node, following only left children when available, and right children
 * only when no left child exists. The root itself is always included, but leaf
 * nodes should NOT be included in the sum (only the non-leaf nodes along the path).
 *
 * A leaf node is a node with no children.
 *
 * Example 1:
 *   Input: root = [1, 2, 3, 4, 5, null, null, 7]
 *   Output: 7
 *   Explanation: Left boundary path is 1 -> 2 -> 4 -> 7.
 *                Leaf node 7 is excluded. Sum = 1 + 2 + 4 = 7.
 *
 * Example 2:
 *   Input: root = [10, 20, 30, 40, null, null, null]
 *   Output: 30
 *   Explanation: Left boundary path is 10 -> 20 -> 40.
 *                Leaf node 40 is excluded. Sum = 10 + 20 = 30.
 */

// ─── TreeNode Definition ────────────────────────────────────────────────────

/// <summary>
/// Standard binary tree node used throughout this solution.
/// Each node holds an integer value and optional left/right child references.
/// </summary>
public class TreeNode
{
    public int val;
    public TreeNode? left;
    public TreeNode? right;

    public TreeNode(int val = 0, TreeNode? left = null, TreeNode? right = null)
    {
        this.val   = val;
        this.left  = left;
        this.right = right;
    }
}

// ─── Solution Class ─────────────────────────────────────────────────────────

public class Solution
{
    /// <summary>
    /// Returns the sum of all NON-LEAF nodes on the left boundary of the tree.
    ///
    /// Time Complexity : O(H) where H is the height of the tree.
    ///                   In the worst case (a skewed tree) H = N, so O(N).
    ///                   We only traverse one path from root to a leaf.
    ///
    /// Space Complexity: O(1) extra space (iterative approach, no recursion stack).
    ///                   We use a single pointer that walks down the tree.
    /// </summary>
    public int SumOfLeftBoundary(TreeNode? root)
    {
        // ── Step 1: Handle the edge case of an empty tree ──────────────────
        // If the tree has no nodes at all, the boundary sum is 0.
        if (root == null)
            return 0;

        // ── Step 2: Initialise the running sum and set the current pointer ──
        // We start at the root. The root is ALWAYS part of the left boundary
        // (as stated in the problem), so we check whether it is a leaf before
        // deciding to add its value.
        int sum = 0;
        TreeNode? current = root;

        // ── Step 3: Walk down the left boundary iteratively ─────────────────
        // We keep moving downward until we reach a leaf node.
        // At each step we decide which child to follow:
        //   • Prefer the LEFT child (definition of "left boundary").
        //   • Fall back to the RIGHT child only when no left child exists.
        // We stop BEFORE adding a leaf node to the sum.
        while (current != null)
        {
            // ── Step 3a: Check whether the current node is a leaf ───────────
            // A leaf has neither a left child nor a right child.
            // The problem explicitly says leaf nodes must NOT be included.
            bool isLeaf = (current.left == null && current.right == null);

            if (!isLeaf)
            {
                // ── Step 3b: Add the value of this non-leaf boundary node ───
                // Only non-leaf nodes contribute to the sum.
                sum += current.val;
            }

            // ── Step 3c: Decide which child to visit next ───────────────────
            // Left boundary rule:
            //   1. If a left child exists  → move left.
            //   2. If no left child exists → move right (fallback).
            //   3. If neither child exists → we are at a leaf; the loop ends
            //      naturally because both children are null.
            if (current.left != null)
            {
                // Prefer left child — this is the primary direction of the
                // left boundary.
                current = current.left;
            }
            else
            {
                // No left child available; fall back to the right child.
                // This keeps us on the "leftmost" path possible.
                current = current.right;
            }
        }

        // ── Step 4: Return the accumulated sum ──────────────────────────────
        // At this point we have visited every non-leaf node on the left
        // boundary and added their values to `sum`.
        return sum;
    }
}

// ─── Helper: Build a tree from a level-order array ──────────────────────────

/// <summary>
/// Utility class to construct a binary tree from a level-order (BFS) array,
/// where null represents a missing node — exactly like LeetCode input format.
/// </summary>
static class TreeBuilder
{
    public static TreeNode? FromArray(int?[] values)
    {
        if (values == null || values.Length == 0 || values[0] == null)
            return null;

        // Create the root from the first element.
        TreeNode root = new TreeNode(values[0]!.Value);

        // Use a queue to assign children level by level.
        Queue<TreeNode> queue = new Queue<TreeNode>();
        queue.Enqueue(root);

        int i = 1; // index into the values array
        while (queue.Count > 0 && i < values.Length)
        {
            TreeNode node = queue.Dequeue();

            // Assign left child if the next value is not null.
            if (i < values.Length)
            {
                if (values[i] != null)
                {
                    node.left = new TreeNode(values[i]!.Value);
                    queue.Enqueue(node.left);
                }
                i++;
            }

            // Assign right child if the next value is not null.
            if (i < values.Length)
            {
                if (values[i] != null)
                {
                    node.right = new TreeNode(values[i]!.Value);
                    queue.Enqueue(node.right);
                }
                i++;
            }
        }

        return root;
    }
}

// ─── Demo / Test Code ───────────────────────────────────────────────────────

Solution solution = new Solution();

// ── Example 1 ───────────────────────────────────────────────────────────────
// Tree:
//         1
//        / \
//       2   3
//      / \
//     4   5
//    /
//   7
//
// Left boundary path: 1 → 2 → 4 → 7
// Leaf node 7 is excluded.
// Expected sum: 1 + 2 + 4 = 7
//
// Trace:
//   current=1  (non-leaf) → sum=1,  move left  → current=2
//   current=2  (non-leaf) → sum=3,  move left  → current=4
//   current=4  (non-leaf) → sum=7,  move left  → current=7
//   current=7  (leaf)     → skip,   left=null, right=null → current=null
//   Loop ends. Return 7. ✓

int?[] input1 = { 1, 2, 3, 4, 5, null, null, null, null, null, null, 7 };

// Build the tree manually to match the described structure exactly,
// because the level-order array for a node at depth 3 (child of node 4)
// requires padding nulls for the siblings at each level.
//
// It is clearer and safer to build this particular tree by hand:
TreeNode tree1 = new TreeNode(1,
    new TreeNode(2,
        new TreeNode(4,
            new TreeNode(7)),   // 7 is left child of 4
        new TreeNode(5)),
    new TreeNode(3));

int result1 = solution.SumOfLeftBoundary(tree1);
Console.WriteLine($"Example 1 — Expected: 7  |  Got: {result1}");

// ── Example 2 ───────────────────────────────────────────────────────────────
// Tree:
//         10
//        /  \
//       20   30
//      /
//     40
//
// Left boundary path: 10 → 20 → 40
// Leaf node 40 is excluded.
// Expected sum: 10 + 20 = 30
//
// Trace:
//   current=10  (non-leaf) → sum=10, move left  → current=20
//   current=20  (non-leaf) → sum=30, move left  → current=40
//   current=40  (leaf)     → skip,   left=null, right=null → current=null
//   Loop ends. Return 30. ✓

TreeNode tree2 = new TreeNode(10,
    new TreeNode(20,
        new TreeNode(40)),
    new TreeNode(30));

int result2 = solution.SumOfLeftBoundary(tree2);
Console.WriteLine($"Example 2 — Expected: 30 |  Got: {result2}");

// ── Edge Case: Single node (root is also a leaf) ─────────────────────────────
// Tree:  5
// Left boundary: just node 5, which is a leaf → excluded.
// Expected sum: 0
//
// Trace:
//   current=5 (leaf) → skip, left=null, right=null → current=null
//   Loop ends. Return 0. ✓

TreeNode tree3 = new TreeNode(5);
int result3 = solution.SumOfLeftBoundary(tree3);
Console.WriteLine($"Edge Case (single node) — Expected: 0  |  Got: {result3}");

// ── Edge Case: Right-skewed tree (no left children anywhere) ─────────────────
// Tree:
//   1
//    \
//     2
//      \
//       3
//
// Left boundary: 1 → 2 → 3 (always falling back to right child)
// Leaf node 3 is excluded.
// Expected sum: 1 + 2 = 3
//
// Trace:
//   current=1 (non-leaf) → sum=1, left=null → move right → current=2
//   current=2 (non-leaf) → sum=3, left=null → move right → current=3
//   current=3 (leaf)     → skip, left=null, right=null  → current=null
//   Loop ends. Return 3. ✓

TreeNode tree4 = new TreeNode(1,
    null,
    new TreeNode(2,
        null,
        new TreeNode(3)));

int result4 = solution.SumOfLeftBoundary(tree4);
Console.WriteLine($"Edge Case (right-skewed) — Expected: 3  |  Got: {result4}");