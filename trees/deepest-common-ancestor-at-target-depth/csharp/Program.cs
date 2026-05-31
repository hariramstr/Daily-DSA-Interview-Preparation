/*
 * Deepest Common Ancestor at Target Depth
 * ========================================
 * Given the root of a binary tree and an integer `depth`, find the lowest common
 * ancestor (LCA) of all nodes that exist exactly at the given `depth` in the tree.
 * The depth of the root node is 1.
 *
 * If there is only one node at the given depth, return that node itself.
 * If no nodes exist at the given depth, return null.
 *
 * The 'deepest common ancestor at target depth' is defined as the deepest node
 * in the tree that is an ancestor of every node found at exactly `depth` levels
 * from the root.
 *
 * Example 1: root = [3,5,1,6,2,0,8,null,null,7,4], depth = 4 => Output: 2
 * Example 2: root = [1,2,3,4,5], depth = 3 => Output: 2
 * Example 3: root = [1,2,3], depth = 1 => Output: 1
 */

// ─── TreeNode definition ────────────────────────────────────────────────────

/// <summary>
/// Standard binary tree node used throughout this solution.
/// </summary>
public class TreeNode
{
    public int val;
    public TreeNode? left;
    public TreeNode? right;

    public TreeNode(int val = 0, TreeNode? left = null, TreeNode? right = null)
    {
        this.val  = val;
        this.left  = left;
        this.right = right;
    }
}

// ─── Solution ───────────────────────────────────────────────────────────────

/// <summary>
/// Finds the deepest common ancestor of all nodes at exactly the given depth.
/// </summary>
public class Solution
{
    /*
     * High-level strategy
     * -------------------
     * We perform a single post-order DFS traversal.  For every node we ask:
     *   "How many nodes at the TARGET depth exist in my subtree,
     *    and what is the deepest ancestor (within my subtree) that covers
     *    ALL of them?"
     *
     * We return a pair  (count, lca)  from each recursive call:
     *   count – number of target-depth nodes found in this subtree
     *   lca   – the deepest node that is an ancestor of ALL those nodes
     *           (null if count == 0)
     *
     * Merging children results at a given node N (currently at depth d):
     *   • If d == targetDepth  → N itself is a target node; return (1, N)
     *   • If d  > targetDepth  → we've gone too deep; return (0, null)
     *   • Otherwise combine left and right results:
     *       – If both sides have target nodes → N is the LCA of both groups,
     *         so return (leftCount + rightCount, N)
     *       – If only one side has target nodes → bubble that side's result up
     *       – If neither side has target nodes → return (0, null)
     *
     * Time  Complexity: O(N) – every node is visited exactly once.
     * Space Complexity: O(H) – recursion stack depth equals tree height H
     *                          (worst case O(N) for a skewed tree).
     */

    // ── Public entry point ──────────────────────────────────────────────────

    public TreeNode? FindLCA(TreeNode? root, int depth)
    {
        // Kick off the recursive helper starting at depth level 1 (the root).
        // The helper returns a (count, lca) tuple; we only need the lca part.
        var (_, lca) = DFS(root, currentDepth: 1, targetDepth: depth);
        return lca;
    }

    // ── Recursive DFS helper ────────────────────────────────────────────────

    /// <summary>
    /// Returns (countOfTargetNodes, lcaNode) for the subtree rooted at <paramref name="node"/>.
    /// </summary>
    /// <param name="node">Current node being processed.</param>
    /// <param name="currentDepth">Depth of <paramref name="node"/> (root = 1).</param>
    /// <param name="targetDepth">The depth we are searching for.</param>
    private (int count, TreeNode? lca) DFS(TreeNode? node, int currentDepth, int targetDepth)
    {
        // ── Base case 1: null node ──────────────────────────────────────────
        // A null node contributes zero target-depth nodes and has no LCA.
        if (node == null)
            return (0, null);

        // ── Base case 2: we have reached the target depth ───────────────────
        // This node IS one of the nodes we are looking for.
        // It is trivially its own ancestor, so return (1, this node).
        if (currentDepth == targetDepth)
            return (1, node);

        // ── Base case 3: we have gone PAST the target depth ─────────────────
        // This can happen when the tree is deeper than targetDepth in some
        // branches but we are already below the target level.
        // (In practice this branch is only reached if targetDepth < currentDepth,
        //  which shouldn't happen given our traversal order, but it is a safe guard.)
        if (currentDepth > targetDepth)
            return (0, null);

        // ── Recursive step: explore left and right subtrees ─────────────────
        // We go one level deeper in each direction.
        var (leftCount,  leftLCA)  = DFS(node.left,  currentDepth + 1, targetDepth);
        var (rightCount, rightLCA) = DFS(node.right, currentDepth + 1, targetDepth);

        // ── Merge results ────────────────────────────────────────────────────

        // Case A: BOTH subtrees contain at least one target-depth node.
        // The current node 'node' is the shallowest point where both groups
        // meet, making it the LCA of all target-depth nodes in this subtree.
        if (leftCount > 0 && rightCount > 0)
        {
            // The total count is the sum from both sides.
            // The LCA is the current node itself because it bridges both sides.
            return (leftCount + rightCount, node);
        }

        // Case B: Only the LEFT subtree has target-depth nodes.
        // The right subtree contributes nothing, so we simply propagate
        // the left result upward unchanged.
        if (leftCount > 0)
            return (leftCount, leftLCA);

        // Case C: Only the RIGHT subtree has target-depth nodes.
        // Symmetric to Case B.
        if (rightCount > 0)
            return (rightCount, rightLCA);

        // Case D: Neither subtree has any target-depth nodes.
        // This subtree is irrelevant; return zero with no LCA.
        return (0, null);
    }
}

// ─── Demo / Test harness ─────────────────────────────────────────────────────

/*
 * Helper: build a binary tree from a level-order (BFS) array where
 * null entries represent missing nodes.  This mirrors LeetCode's
 * standard tree serialisation format.
 */
static TreeNode? BuildTree(int?[] values)
{
    if (values == null || values.Length == 0 || values[0] == null)
        return null;

    // Create the root from the first element.
    var root = new TreeNode(values[0]!.Value);

    // Use a queue to track nodes whose children still need to be assigned.
    var queue = new Queue<TreeNode>();
    queue.Enqueue(root);

    int i = 1; // index into the values array

    while (queue.Count > 0 && i < values.Length)
    {
        var current = queue.Dequeue();

        // Assign left child if the next value exists and is not null.
        if (i < values.Length)
        {
            if (values[i] != null)
            {
                current.left = new TreeNode(values[i]!.Value);
                queue.Enqueue(current.left);
            }
            i++;
        }

        // Assign right child if the next value exists and is not null.
        if (i < values.Length)
        {
            if (values[i] != null)
            {
                current.right = new TreeNode(values[i]!.Value);
                queue.Enqueue(current.right);
            }
            i++;
        }
    }

    return root;
}

// ── Example 1 ────────────────────────────────────────────────────────────────
// Tree: [3,5,1,6,2,0,8,null,null,7,4]
//
//            3          depth 1
//          /   \
//         5     1       depth 2
//        / \   / \
//       6   2 0   8     depth 3
//          / \
//         7   4         depth 4
//
// Nodes at depth 4: 7, 4  →  LCA = 2
var solution = new Solution();

var tree1 = BuildTree(new int?[] { 3, 5, 1, 6, 2, 0, 8, null, null, 7, 4 });
var result1 = solution.FindLCA(tree1, depth: 4);
Console.WriteLine($"Example 1 — depth=4 → LCA val = {result1?.val} (expected 2)");

// ── Example 2 ────────────────────────────────────────────────────────────────
// Tree: [1,2,3,4,5]
//
//          1            depth 1
//         / \
//        2   3          depth 2
//       / \
//      4   5            depth 3
//
// Nodes at depth 3: 4, 5  →  LCA = 2
var tree2 = BuildTree(new int?[] { 1, 2, 3, 4, 5 });
var result2 = solution.FindLCA(tree2, depth: 3);
Console.WriteLine($"Example 2 — depth=3 → LCA val = {result2?.val} (expected 2)");

// ── Example 3 ────────────────────────────────────────────────────────────────
// Tree: [1,2,3]
//
//          1            depth 1
//         / \
//        2   3          depth 2
//
// Only node at depth 1: 1  →  return root (val = 1)
var tree3 = BuildTree(new int?[] { 1, 2, 3 });
var result3 = solution.FindLCA(tree3, depth: 1);
Console.WriteLine($"Example 3 — depth=1 → LCA val = {result3?.val} (expected 1)");

// ── Extra: depth with no nodes ────────────────────────────────────────────────
// Asking for depth 5 in tree1 which only goes to depth 4 → null
var result4 = solution.FindLCA(tree1, depth: 5);
Console.WriteLine($"Extra   — depth=5 → LCA = {(result4 == null ? "null" : result4.val.ToString())} (expected null)");

// ── Extra: single node at target depth ───────────────────────────────────────
// Tree: [1,2,3], depth=2 → nodes are 2 and 3, LCA = 1
var result5 = solution.FindLCA(tree3, depth: 2);
Console.WriteLine($"Extra   — depth=2 on [1,2,3] → LCA val = {result5?.val} (expected 1)");