/*
Title: Find the Nearest Open Seat in a Venue Tree

Problem Description:
A concert venue stores its seating layout as a binary tree. Each node represents a seat section
and contains a unique integer id. The root is the main entrance section. Some sections are
temporarily closed for cleaning, while the rest are open.

You are given the root of the binary tree, an integer target representing the id of a section
where a guest is currently standing, and a set closedSections containing the ids of closed sections.
Return the id of the open section that is closest to the target section by number of edges traveled.

Distance is measured along tree edges, and movement is allowed both downward to children and upward
to parents. The target section itself may be open or closed. If the target section is open, then it
is considered a valid answer with distance 0. If multiple open sections are tied for the minimum
distance, return the one with the smallest section id. If the target id does not exist in the tree,
return -1. If every section is closed, return -1.

This problem asks you to search a tree as if it were an undirected graph. A common approach is to
first record parent pointers, then run a breadth-first search starting from the target node while
skipping visited nodes.

Constraints:
- The number of nodes in the tree is in the range [1, 100000].
- Node ids are unique integers in the range [1, 10^9].
- closedSections contains between 0 and n ids.
- target is an integer in the range [1, 10^9].
- Your solution should run in O(n) time and use O(n) extra space.
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
    - We visit each node at most once while building parent links and locating the target.
    - We visit each node at most once again during BFS from the target.
    - Overall this is linear in the number of nodes.

    Space Complexity: O(n)
    - We store parent pointers for nodes.
    - We store a map from id to node so we can find the target quickly.
    - We store visited nodes and the BFS queue.
    - In the worst case, all of these can hold O(n) items.
    */
    public int FindNearestOpenSeat(TreeNode? root, int target, HashSet<int> closedSections)
    {
        // Step 1:
        // Handle the simplest invalid input case first.
        // If the tree is empty, there are no sections at all, so the answer must be -1.
        if (root == null)
        {
            return -1;
        }

        // Step 2:
        // We need to move through the tree like an undirected graph:
        // from a node to its left child, right child, and parent.
        //
        // A normal binary tree node does not store a parent pointer,
        // so we build that information ourselves.
        //
        // parentMap:
        //   key   = a node
        //   value = that node's parent
        //
        // idToNode:
        //   key   = node id
        //   value = the actual TreeNode reference
        //
        // Why do we need idToNode?
        // The input gives us a target id, not a direct node reference.
        // So we need a fast O(1)-average lookup from id to node.
        var parentMap = new Dictionary<TreeNode, TreeNode?>();
        var idToNode = new Dictionary<int, TreeNode>();

        // Step 3:
        // Traverse the tree once to fill both dictionaries.
        //
        // We use an iterative traversal with a stack instead of recursion.
        // This is important because the tree can contain up to 100000 nodes.
        // A recursive DFS could risk stack overflow on a very deep tree.
        var stack = new Stack<TreeNode>();
        stack.Push(root);
        parentMap[root] = null;

        while (stack.Count > 0)
        {
            var current = stack.Pop();

            // Record the mapping from section id to the actual node.
            // Node ids are unique, so each id appears exactly once.
            idToNode[current.val] = current;

            // If there is a left child:
            // - record its parent
            // - push it for later processing
            if (current.left != null)
            {
                parentMap[current.left] = current;
                stack.Push(current.left);
            }

            // If there is a right child:
            // - record its parent
            // - push it for later processing
            if (current.right != null)
            {
                parentMap[current.right] = current;
                stack.Push(current.right);
            }
        }

        // Step 4:
        // If the target id does not exist anywhere in the tree,
        // the problem says we must return -1.
        if (!idToNode.TryGetValue(target, out var targetNode))
        {
            return -1;
        }

        // Step 5:
        // Now we perform BFS starting from the target node.
        //
        // Why BFS?
        // BFS explores nodes in order of increasing distance:
        // - first all nodes at distance 0
        // - then all nodes at distance 1
        // - then all nodes at distance 2
        // and so on.
        //
        // That means the first distance level containing any open section
        // is guaranteed to be the minimum possible distance.
        //
        // However, there is one extra rule:
        // if multiple open sections are tied at the same minimum distance,
        // return the smallest section id.
        //
        // To handle that correctly, we process BFS level by level.
        // For each level, we examine every node in that level and track
        // the smallest open id found there.
        // If we find at least one open node in the current level,
        // we return the smallest id from this level immediately.
        var queue = new Queue<TreeNode>();
        var visited = new HashSet<TreeNode>();

        queue.Enqueue(targetNode);
        visited.Add(targetNode);

        while (queue.Count > 0)
        {
            // Step 6:
            // Process exactly one BFS "layer" (all nodes at the same distance).
            //
            // queue.Count right now is the number of nodes at the current distance.
            int levelSize = queue.Count;

            // This will store the best answer found in the current distance layer.
            // We use int.MaxValue as a sentinel meaning "no open node found yet".
            int bestOpenIdAtThisDistance = int.MaxValue;

            for (int i = 0; i < levelSize; i++)
            {
                var current = queue.Dequeue();

                // Step 7:
                // Check whether the current section is open.
                //
                // A section is open if its id is NOT in closedSections.
                // If it is open, it is a candidate answer for this distance.
                //
                // We do not return immediately on the first open node,
                // because another open node in the same BFS layer might have
                // a smaller id, and the problem requires the smallest id in a tie.
                if (!closedSections.Contains(current.val))
                {
                    if (current.val < bestOpenIdAtThisDistance)
                    {
                        bestOpenIdAtThisDistance = current.val;
                    }
                }

                // Step 8:
                // Add all unvisited neighbors of the current node.
                //
                // Since we are treating the tree like an undirected graph,
                // each node can have up to 3 neighbors:
                // - left child
                // - right child
                // - parent
                //
                // We must use a visited set because once parent links exist,
                // the structure behaves like a graph with two-way connections.
                // Without visited, we could bounce back and forth forever.
                if (current.left != null && visited.Add(current.left))
                {
                    queue.Enqueue(current.left);
                }

                if (current.right != null && visited.Add(current.right))
                {
                    queue.Enqueue(current.right);
                }

                var parent = parentMap[current];
                if (parent != null && visited.Add(parent))
                {
                    queue.Enqueue(parent);
                }
            }

            // Step 9:
            // After processing the entire current distance layer,
            // if we found any open section(s), then this is the minimum distance.
            // Return the smallest id among them.
            if (bestOpenIdAtThisDistance != int.MaxValue)
            {
                return bestOpenIdAtThisDistance;
            }
        }

        // Step 10:
        // If BFS finishes without finding any open section,
        // then every section in the tree is closed.
        return -1;
    }
}

static TreeNode? BuildTreeFromLevelOrder(int?[] values)
{
    if (values.Length == 0 || values[0] == null)
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
// root = [8,4,12,2,6,10,14], target = 4, closedSections = [4,2]
// Expected output: 6
var root1 = BuildTreeFromLevelOrder(new int?[] { 8, 4, 12, 2, 6, 10, 14 });
var closed1 = new HashSet<int> { 4, 2 };
int result1 = solution.FindNearestOpenSeat(root1, 4, closed1);
Console.WriteLine(result1);

// Example 2:
// root = [5,3,9,1,null,7,11], target = 3, closedSections = [1,3,5]
// Expected output: 7
//
// Note:
// In this tree, from node 3:
// - distance 0: 3 (closed)
// - distance 1: 1 (closed), 5 (closed)
// - distance 2: 9 and 7 are both open
// Tie is broken by smaller id => 7
var root2 = BuildTreeFromLevelOrder(new int?[] { 5, 3, 9, 1, null, 7, 11 });
var closed2 = new HashSet<int> { 1, 3, 5 };
int result2 = solution.FindNearestOpenSeat(root2, 3, closed2);
Console.WriteLine(result2);

// Additional demo 1:
// Target itself is open, so answer should be target with distance 0.
var root3 = BuildTreeFromLevelOrder(new int?[] { 8, 4, 12, 2, 6, 10, 14 });
var closed3 = new HashSet<int> { 2, 10 };
int result3 = solution.FindNearestOpenSeat(root3, 4, closed3);
Console.WriteLine(result3);

// Additional demo 2:
// Target does not exist in the tree, so answer should be -1.
var root4 = BuildTreeFromLevelOrder(new int?[] { 1, 2, 3 });
var closed4 = new HashSet<int>();
int result4 = solution.FindNearestOpenSeat(root4, 99, closed4);
Console.WriteLine(result4);

// Additional demo 3:
// Every section is closed, so answer should be -1.
var root5 = BuildTreeFromLevelOrder(new int?[] { 1, 2, 3, 4, 5 });
var closed5 = new HashSet<int> { 1, 2, 3, 4, 5 };
int result5 = solution.FindNearestOpenSeat(root5, 3, closed5);
Console.WriteLine(result5);