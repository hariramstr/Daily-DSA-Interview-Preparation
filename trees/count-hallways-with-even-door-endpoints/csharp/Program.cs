/*
Title: Count Hallways With Even Door Endpoints

Problem Description:
You are given the root of a binary tree representing a building map. Each node stores an integer room number.
A hallway is defined as any parent node together with its existing direct child connections.
A hallway is considered valid if the parent room number is even and every existing child of that parent also has an even room number.
Leaf nodes do not count as hallways because they have no child connections.

Return the number of valid hallways in the tree.

In other words, for every non-leaf node, check whether its value is even and whether all of its present children
(left child, right child, or both) also have even values. Count how many nodes satisfy this rule.

This is an easy tree traversal problem. A simple DFS or BFS is enough because each node only needs to be checked once.

Constraints:
- The number of nodes in the tree is in the range [1, 1000].
- -10^4 <= Node.val <= 10^4
- The tree is a binary tree.

Example 1:
Input: root = [8,4,6,2,7,null,10]
Output: 2
Explanation:
- Node 8 forms a valid hallway because 8, 4, and 6 are all even.
- Node 6 also forms a valid hallway because its existing child 10 is even.
- Node 4 does not qualify because one of its children is 7, which is odd.

Example 2:
Input: root = [5,2,8,4,6]
Output: 1
Explanation:
- Node 5 is odd, so it cannot form a valid hallway.
- Node 2 forms a valid hallway because both of its children, 4 and 6, are even.
- The other nodes are leaves.
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
    - We visit each node exactly once during the depth-first traversal.
    - At each node, we do only constant-time work:
      checking whether it has children and whether the involved values are even.

    Space Complexity: O(h)
    - h is the height of the tree.
    - This space is used by the recursion call stack.
    - In the worst case of a completely skewed tree, h can be n.
    - In a balanced tree, h is about log n.
    */
    public int CountEvenHallways(TreeNode? root)
    {
        // We will store the total number of valid hallways we find while traversing the tree.
        // This variable is updated whenever we discover a node that satisfies the hallway rule.
        int count = 0;

        // We define a local DFS function so the traversal logic stays close to the main method.
        // DFS is a natural fit for tree problems because:
        // 1. Trees are recursive structures.
        // 2. We want to inspect every node exactly once.
        // 3. We do not need any special ordering beyond "visit all nodes".
        void Dfs(TreeNode? node)
        {
            // Step 1: Handle the base case.
            // If the current node is null, there is nothing to process.
            // This happens when we move past a leaf's missing child.
            if (node == null)
            {
                return;
            }

            // Step 2: Determine whether the current node is a leaf.
            // A hallway must include a parent and at least one existing direct child connection.
            // Therefore, leaf nodes do NOT count and should not be checked as hallways.
            bool hasLeftChild = node.left != null;
            bool hasRightChild = node.right != null;
            bool isNonLeaf = hasLeftChild || hasRightChild;

            // Step 3: Only non-leaf nodes can possibly form a valid hallway.
            // So we perform the hallway validation only when the current node has at least one child.
            if (isNonLeaf)
            {
                // Step 4: Check whether the parent node's value is even.
                // This is required by the problem statement.
                bool parentIsEven = node.val % 2 == 0;

                // Step 5: Check whether every EXISTING child is even.
                // Important detail:
                // - If a child does not exist, we simply ignore it.
                // - If a child exists, its value must be even.
                //
                // We start by assuming the child condition is true,
                // then invalidate it if we find any existing odd child.
                bool allExistingChildrenAreEven = true;

                // Step 6: If the left child exists, verify its value is even.
                // This check is necessary because one odd child is enough to make the hallway invalid.
                if (hasLeftChild && node.left!.val % 2 != 0)
                {
                    allExistingChildrenAreEven = false;
                }

                // Step 7: If the right child exists, verify its value is even.
                // Again, any existing odd child breaks the rule.
                if (hasRightChild && node.right!.val % 2 != 0)
                {
                    allExistingChildrenAreEven = false;
                }

                // Step 8: If both conditions are satisfied:
                // - the parent is even
                // - all existing children are even
                // then this node forms a valid hallway, so we increment the answer.
                if (parentIsEven && allExistingChildrenAreEven)
                {
                    count++;
                }
            }

            // Step 9: Continue traversing the left subtree.
            // We must do this because valid hallways can appear anywhere in the tree,
            // not just near the root.
            Dfs(node.left);

            // Step 10: Continue traversing the right subtree for the same reason.
            Dfs(node.right);
        }

        // Step 11: Start the DFS traversal from the root of the tree.
        Dfs(root);

        // Step 12: After visiting every node, return the total number of valid hallways found.
        return count;
    }
}

static TreeNode? BuildTree(params int?[] values)
{
    if (values.Length == 0 || values[0] == null)
    {
        return null;
    }

    TreeNode root = new TreeNode(values[0]!.Value);
    Queue<TreeNode> queue = new Queue<TreeNode>();
    queue.Enqueue(root);

    int index = 1;

    while (queue.Count > 0 && index < values.Length)
    {
        TreeNode current = queue.Dequeue();

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
// Tree: [8,4,6,2,7,null,10]
// Valid hallways:
// - Node 8: even, children 4 and 6 are even => valid
// - Node 4: child 7 is odd => invalid
// - Node 6: even, existing child 10 is even => valid
// Expected output: 2
TreeNode? root1 = BuildTree(8, 4, 6, 2, 7, null, 10);
int result1 = solution.CountEvenHallways(root1);
Console.WriteLine(result1);

// Example 2:
// Tree: [5,2,8,4,6]
// Valid hallways:
// - Node 5: odd => invalid
// - Node 2: even, children 4 and 6 are even => valid
// - Node 8, 4, 6 are leaves => do not count
// Expected output: 1
TreeNode? root2 = BuildTree(5, 2, 8, 4, 6);
int result2 = solution.CountEvenHallways(root2);
Console.WriteLine(result2);

// Additional demo:
// Single-node tree: [2]
// Root is a leaf, so there are no hallways.
// Expected output: 0
TreeNode? root3 = BuildTree(2);
int result3 = solution.CountEvenHallways(root3);
Console.WriteLine(result3);