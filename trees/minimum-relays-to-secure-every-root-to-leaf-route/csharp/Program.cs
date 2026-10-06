/*
Title: Minimum Relays to Secure Every Root-to-Leaf Route

Problem Description:
You are given the root of a binary tree representing a hierarchy of communication towers.
Every node is a tower, and every edge is a direct communication link between a tower and one
of its children. A security relay can be installed on any tower. If a relay is installed on
a tower, then that tower, its parent, and its immediate children are considered secured.

Your task is to return the minimum number of relays needed so that every tower in the tree is secured.

A tower is secured if at least one of the following is true:
1. A relay is installed on that tower.
2. Its parent has a relay.
3. One of its children has a relay.

You must cover the entire tree, including the root and all leaves.

The input tree can be assumed to be a standard binary tree where each node has at most two children.
Node values are unique integers but are only identifiers; they do not affect the answer.

Constraints:
- The number of nodes in the tree is in the range [1, 100000].
- Each node has at most two children.
- Node values are in the range [-1000000000, 1000000000].
- The solution should run in O(n) time.
- Recursive solutions should be careful about stack depth on highly skewed trees.

Examples:
1) root = [0,0,null,0,0]
   Output: 1

2) root = [0,0,null,0,null,0,null,null,0]
   Output: 2

Follow-up:
Can you solve it using a postorder traversal with a small number of states per node,
instead of trying all placement combinations?
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
    // We use three small states for each node:
    //
    // 0 = This node is NOT covered by any relay yet.
    // 1 = This node HAS a relay.
    // 2 = This node is covered, but does NOT have a relay.
    //
    // The greedy rule in postorder is:
    // - If any child is state 0 (uncovered), we MUST place a relay here.
    // - Else if any child is state 1 (child has relay), this node is covered.
    // - Else both children are covered without relays affecting this node, so this node becomes uncovered.
    //
    // This is the classic optimal greedy strategy for minimum camera / relay placement,
    // adapted here to an iterative postorder traversal to avoid recursion depth issues.

    /*
    Time Complexity: O(n)
    - Every node is pushed/popped a constant number of times and processed once.

    Space Complexity: O(n)
    - We use stacks and a dictionary to store traversal state and computed node states.
    - In the worst case of a skewed tree, this can hold O(n) nodes.
    */
    public int MinRelayCover(TreeNode? root)
    {
        // Edge case:
        // If the tree is empty, no relays are needed.
        // The problem guarantees at least 1 node, but handling this makes the method robust.
        if (root == null)
        {
            return 0;
        }

        // This counter stores the total number of relays we decide to install.
        int relays = 0;

        // We want postorder traversal: left, right, node.
        // Recursive postorder is simple, but can overflow the call stack on a very deep tree.
        // So we simulate it manually with an explicit stack.
        //
        // Each stack item contains:
        // - the current node
        // - a boolean "visited"
        //
        // visited == false:
        //   We are seeing this node for the first time.
        //   We will push it back as visited == true, then push its children.
        //
        // visited == true:
        //   Both children have already been processed, so now we can compute this node's state.
        var stack = new Stack<(TreeNode node, bool visited)>();
        stack.Push((root, false));

        // We need to remember the computed state for each processed node,
        // because when we process a parent, we must inspect the states of its children.
        var state = new Dictionary<TreeNode, int>();

        while (stack.Count > 0)
        {
            var (node, visited) = stack.Pop();

            if (!visited)
            {
                // First time we see this node.
                // We schedule it to be processed AFTER its children.
                stack.Push((node, true));

                // Push right child first, then left child.
                // Because stack is LIFO, left child will be processed before right child.
                // The exact left/right order does not affect correctness here,
                // but this preserves standard postorder style.
                if (node.right != null)
                {
                    stack.Push((node.right, false));
                }

                if (node.left != null)
                {
                    stack.Push((node.left, false));
                }
            }
            else
            {
                // Now both children, if they exist, have already been processed.
                // So we can safely read their states and decide the best state for this node.

                // Important base rule for null children:
                // A null child is considered "covered without a relay" (state 2).
                //
                // Why?
                // Because null is not a real node that needs coverage.
                // Treating null as covered prevents us from placing unnecessary relays on leaves.
                int leftState = node.left == null ? 2 : state[node.left];
                int rightState = node.right == null ? 2 : state[node.right];

                // Case 1:
                // If ANY child is uncovered (state 0),
                // then this current node must get a relay.
                //
                // Why is this necessary?
                // Because the only ways to cover an uncovered child are:
                // - place a relay on that child itself,
                // - place a relay on its parent,
                // - place a relay on one of its children.
                //
                // But since we are processing in postorder, the child's subtree is already finalized.
                // If the child is still uncovered now, the best remaining option is to place a relay here.
                if (leftState == 0 || rightState == 0)
                {
                    relays++;
                    state[node] = 1; // This node has a relay.
                }
                // Case 2:
                // If no child is uncovered, but at least one child has a relay,
                // then this node is automatically covered by that child.
                else if (leftState == 1 || rightState == 1)
                {
                    state[node] = 2; // Covered, but no relay on this node.
                }
                // Case 3:
                // Otherwise, both children are covered and neither has a relay.
                // That means this node is currently NOT covered by any child.
                //
                // We do not place a relay here immediately, because maybe its parent
                // will place one and cover this node more efficiently.
                else
                {
                    state[node] = 0; // Uncovered for now.
                }
            }
        }

        // After processing the whole tree, the root has no parent.
        // So if the root is still uncovered, nobody above it can help.
        // Therefore we must place one final relay at the root.
        if (state[root] == 0)
        {
            relays++;
        }

        return relays;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1:
// Input: [0,0,null,0,0]
//
// Tree shape:
//       0
//      /
//     0
//    / \
//   0   0
//
// Optimal answer: 1
// Place one relay on the left child of the root.
var example1 = new TreeNode(0,
    new TreeNode(0,
        new TreeNode(0),
        new TreeNode(0)),
    null);

int result1 = solution.MinRelayCover(example1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 1

// Example 2:
// Input: [0,0,null,0,null,0,null,null,0]
//
// Interpreted as a left-leaning chain with one extra deeper child:
//       0
//      /
//     0
//    /
//   0
//  /
// 0
//  \
//   0
//
// One valid construction matching the intended chain-like difficulty.
// Optimal answer: 2
var example2 = new TreeNode(0,
    new TreeNode(0,
        new TreeNode(0,
            new TreeNode(0, null, new TreeNode(0)),
            null),
        null),
    null);

int result2 = solution.MinRelayCover(example2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 2

// Additional quick sanity checks for learning:

// Single node tree:
// One relay is needed because the root must be covered.
var singleNode = new TreeNode(42);
Console.WriteLine("Single Node Result: " + solution.MinRelayCover(singleNode)); // Expected: 1

// Perfect small tree:
//       1
//      / \
//     2   3
//
// Best is 1 relay at root.
var smallBalanced = new TreeNode(1, new TreeNode(2), new TreeNode(3));
Console.WriteLine("Small Balanced Result: " + solution.MinRelayCover(smallBalanced)); // Expected: 1

// Chain of 4 nodes:
// 1 - 2 - 3 - 4 (all as left children)
// Best is 2 relays.
var chain4 = new TreeNode(1,
    new TreeNode(2,
        new TreeNode(3,
            new TreeNode(4),
            null),
        null),
    null);

Console.WriteLine("Chain of 4 Result: " + solution.MinRelayCover(chain4)); // Expected: 2