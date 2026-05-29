/*
 * Burning Tree Spread Timer
 * ========================
 * You are given the root of a binary tree where each node has a unique integer value.
 * A fire starts at a specific node (identified by its value) at time 0.
 * Every second, the fire spreads to all adjacent nodes — the left child, right child,
 * and parent of any currently burning node.
 *
 * Return the minimum number of seconds it takes for the entire tree to be on fire.
 *
 * Key Insight:
 * -----------
 * A binary tree is normally traversed top-down (parent → children), but fire also
 * spreads UPWARD (child → parent). To handle upward spread, we need to know each
 * node's parent. We can convert the tree into an undirected graph (adjacency list)
 * where each node connects to its left child, right child, AND parent.
 * Then we do a simple BFS (Breadth-First Search) from the start node, counting
 * the number of "waves" (seconds) until all nodes are visited.
 *
 * Example 1: root = [1,2,3,4,5,null,6], start = 2 → Output: 3
 * Example 2: root = [1,2,3], start = 1 → Output: 1
 */

// ─── TreeNode Definition ───────────────────────────────────────────────────────
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

// ─── Solution Class ────────────────────────────────────────────────────────────
public class Solution
{
    /*
     * Method: AmountOfTime
     *
     * Time Complexity:  O(N) — We visit every node exactly once during the graph
     *                   build (DFS) and exactly once during the BFS spread.
     *                   N = number of nodes in the tree.
     *
     * Space Complexity: O(N) — We store an adjacency list with N entries, a visited
     *                   set of up to N nodes, and a BFS queue of up to N nodes.
     *
     * High-Level Plan:
     *   Step 1 — Build an undirected graph (adjacency list) from the binary tree.
     *            Each node's value maps to a list of its neighbors (parent + children).
     *   Step 2 — BFS from the start node, layer by layer.
     *            Each layer = 1 second of fire spreading.
     *   Step 3 — Return the number of layers processed minus 1
     *            (the first layer is time 0, not time 1).
     */
    public int AmountOfTime(TreeNode root, int start)
    {
        // ── Step 1: Build the undirected adjacency list ──────────────────────
        // We use a Dictionary<int, List<int>> where:
        //   Key   = a node's value
        //   Value = list of neighboring node values (parent, left child, right child)
        //
        // Why a dictionary? Node values can be up to 10,000 and are unique,
        // so a dictionary gives O(1) average lookup by node value.
        var graph = new Dictionary<int, List<int>>();

        // Helper: ensure a node value has an entry in the graph
        // (avoids null-reference errors when adding neighbors)
        void EnsureEntry(int nodeVal)
        {
            if (!graph.ContainsKey(nodeVal))
                graph[nodeVal] = new List<int>();
        }

        // ── DFS to populate the graph ────────────────────────────────────────
        // We perform a Depth-First Search on the original binary tree.
        // For every parent→child edge we encounter, we add BOTH directions:
        //   parent's neighbor list gets child, AND child's neighbor list gets parent.
        // This makes the directed tree into an undirected graph so fire can travel up.
        void BuildGraph(TreeNode? node, int parentVal)
        {
            if (node == null) return; // Base case: nothing to process

            // Make sure this node has an entry in the graph
            EnsureEntry(node.val);

            // If this node has a parent (i.e., it's not the root),
            // connect this node ↔ parent bidirectionally.
            if (parentVal != -1)
            {
                EnsureEntry(parentVal);
                graph[node.val].Add(parentVal);   // child can spread fire to parent
                graph[parentVal].Add(node.val);   // parent can spread fire to child
            }

            // Recurse into left subtree; this node becomes the parent
            BuildGraph(node.left, node.val);

            // Recurse into right subtree; this node becomes the parent
            BuildGraph(node.right, node.val);
        }

        // Kick off the DFS from the root; root has no parent so we pass -1
        BuildGraph(root, -1);

        // ── Step 2: BFS from the start node ─────────────────────────────────
        // BFS naturally processes nodes level by level.
        // Each "level" of BFS corresponds to one second of fire spreading.
        //
        // We use:
        //   queue   — holds the nodes currently on fire (current BFS frontier)
        //   visited — tracks nodes already burned so we don't revisit them
        var queue = new Queue<int>();
        var visited = new HashSet<int>();

        // The fire starts at the 'start' node at time 0
        queue.Enqueue(start);
        visited.Add(start);

        // 'seconds' counts how many full BFS levels we have completed.
        // We start at -1 because we increment BEFORE processing each level,
        // so after the first level (time 0) it becomes 0.
        int seconds = -1;

        // ── BFS loop ─────────────────────────────────────────────────────────
        // Continue as long as there are burning nodes that might spread fire
        while (queue.Count > 0)
        {
            // Each iteration of this outer loop = 1 second passing
            seconds++;

            // How many nodes are currently on fire (this second's frontier)?
            int levelSize = queue.Count;

            // Process every node burning THIS second simultaneously
            for (int i = 0; i < levelSize; i++)
            {
                int current = queue.Dequeue();

                // Look at all neighbors of the current burning node
                // (could be parent, left child, or right child)
                foreach (int neighbor in graph[current])
                {
                    // Only spread fire to nodes that haven't burned yet
                    if (!visited.Contains(neighbor))
                    {
                        visited.Add(neighbor);       // mark as burning
                        queue.Enqueue(neighbor);     // will spread fire next second
                    }
                }
            }
        }

        // ── Step 3: Return the result ────────────────────────────────────────
        // 'seconds' now equals the index of the last BFS level processed.
        // Because we started at -1 and incremented once per level:
        //   - A single-node tree: 1 level processed → seconds = 0  ✓
        //   - Example 2 (3 nodes, start=1): 2 levels → seconds = 1 ✓
        //   - Example 1 (7 nodes, start=2): 4 levels → seconds = 3 ✓
        return seconds;
    }
}

// ─── Demo / Test Code ─────────────────────────────────────────────────────────
var solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// Tree:
//         1
//        / \
//       2   3
//      / \    \
//     4   5    6
//
// Fire starts at node 2.
// t=0: {2} burns
// t=1: {1, 4, 5} burn  (neighbors of 2)
// t=2: {3} burns        (neighbor of 1 not yet burned)
// t=3: {6} burns        (neighbor of 3 not yet burned)
// Expected output: 3

TreeNode root1 = new TreeNode(1,
    new TreeNode(2,
        new TreeNode(4),
        new TreeNode(5)),
    new TreeNode(3,
        null,
        new TreeNode(6)));

int result1 = solution.AmountOfTime(root1, 2);
Console.WriteLine($"Example 1 — start=2, Expected: 3, Got: {result1}");
// Verify: should print 3

// ── Example 2 ────────────────────────────────────────────────────────────────
// Tree:
//     1
//    / \
//   2   3
//
// Fire starts at node 1.
// t=0: {1} burns
// t=1: {2, 3} burn  (both children of 1)
// Expected output: 1

TreeNode root2 = new TreeNode(1,
    new TreeNode(2),
    new TreeNode(3));

int result2 = solution.AmountOfTime(root2, 1);
Console.WriteLine($"Example 2 — start=1, Expected: 1, Got: {result2}");
// Verify: should print 1

// ── Extra Test: Single Node ───────────────────────────────────────────────────
// Tree: just node 1
// Fire starts at node 1.
// t=0: {1} burns — already done!
// Expected output: 0

TreeNode root3 = new TreeNode(1);
int result3 = solution.AmountOfTime(root3, 1);
Console.WriteLine($"Extra   — single node, start=1, Expected: 0, Got: {result3}");
// Verify: should print 0

// ── Extra Test: Linear chain (like a linked list) ─────────────────────────────
// Tree:
//   1
//    \
//     2
//      \
//       3
//        \
//         4
//
// Fire starts at node 1.
// t=0: {1}, t=1: {2}, t=2: {3}, t=3: {4}
// Expected output: 3

TreeNode root4 = new TreeNode(1,
    null,
    new TreeNode(2,
        null,
        new TreeNode(3,
            null,
            new TreeNode(4))));

int result4 = solution.AmountOfTime(root4, 1);
Console.WriteLine($"Extra   — chain 1→2→3→4, start=1, Expected: 3, Got: {result4}");
// Verify: should print 3

// ── Extra Test: Fire starts at a leaf, must travel up then down ───────────────
// Tree:
//         1
//        / \
//       2   3
//      / \    \
//     4   5    6
//
// Fire starts at node 4.
// t=0: {4}
// t=1: {2}          (parent of 4)
// t=2: {1, 5}       (parent of 2, sibling 5)
// t=3: {3}          (other child of 1)
// t=4: {6}          (child of 3)
// Expected output: 4

int result5 = solution.AmountOfTime(root1, 4);
Console.WriteLine($"Extra   — same tree, start=4, Expected: 4, Got: {result5}");
// Verify: should print 4