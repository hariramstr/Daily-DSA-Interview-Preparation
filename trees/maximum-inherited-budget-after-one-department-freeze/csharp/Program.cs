/*
Title: Maximum Inherited Budget After One Department Freeze

Problem Description:
A company is organized as a rooted tree with node 0 as the CEO. Each node represents a department, and edges connect a department to its direct sub-departments. Every department i has an integer budget value budget[i], which may be positive, zero, or negative.

For any department u, define its inherited budget as the sum of budget values on the simple path from the CEO to u, inclusive.

Before the next quarter begins, the company may freeze at most one department x. Freezing x removes the entire subtree rooted at x from consideration. The CEO's department cannot be frozen. After the freeze, only departments not in the removed subtree remain active.

Your task is to compute the maximum inherited budget among all active departments after applying at most one freeze operation. You may also choose not to freeze any department.

Return that maximum possible inherited budget.

Important details:
- The tree is rooted at node 0.
- If a subtree is frozen, every node in that subtree becomes inactive.
- The inherited budget of an active node is still computed using the original root-to-node path values, since all ancestors outside the frozen subtree remain unchanged.
- It is guaranteed that at least one active node always remains because node 0 cannot be frozen.

Constraints:
- 1 <= n <= 200000
- edges.length == n - 1
- 0 <= parent, child < n
- The input edges form a valid tree rooted at 0
- -10^9 <= budget[i] <= 10^9
- The answer fits in a signed 64-bit integer
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n), where n is the number of departments/nodes.
      We build the tree once, perform one iterative DFS to compute:
      1) Euler tour entry/exit positions
      2) root-to-node inherited budget for every node
      Then we build prefix and suffix maximum arrays in O(n),
      and finally test every possible frozen subtree in O(1) each.

    Space Complexity:
    - O(n)
      We store:
      1) adjacency list for the tree
      2) inherited budget for each node
      3) Euler order arrays
      4) prefix/suffix maximum arrays
      5) iterative DFS stack
    */
    public long MaximumInheritedBudgetAfterOneFreeze(int n, int[][] edges, int[] budget)
    {
        // Special case:
        // If there is only the CEO, we cannot freeze node 0.
        // Therefore the only active department is node 0 itself,
        // and its inherited budget is simply budget[0].
        if (n == 1)
        {
            return budget[0];
        }

        // ------------------------------------------------------------
        // STEP 1: Build the rooted tree as an adjacency list.
        // ------------------------------------------------------------
        // Why adjacency list?
        // - Trees are sparse: exactly n - 1 edges.
        // - Adjacency list is memory-efficient and fast for DFS/BFS.
        //
        // The problem states edges form a valid tree rooted at 0.
        // The examples use directed parent->child edges.
        // So we can directly store child lists.
        var children = new List<int>[n];
        for (int i = 0; i < n; i++)
        {
            children[i] = new List<int>();
        }

        foreach (var e in edges)
        {
            int parent = e[0];
            int child = e[1];
            children[parent].Add(child);
        }

        // ------------------------------------------------------------
        // STEP 2: Compute inherited budget for every node and also
        //         flatten the tree using an Euler tour.
        // ------------------------------------------------------------
        // Key idea:
        // In a rooted tree, every subtree becomes a contiguous segment
        // in Euler tour order (specifically in preorder entry order).
        //
        // If tin[x] = first position of node x in preorder
        // and tout[x] = last position covered by x's subtree,
        // then subtree(x) corresponds exactly to the interval:
        // [tin[x], tout[x]]
        //
        // This is extremely useful because "freeze subtree x" means
        // "remove one contiguous segment from the Euler array".
        //
        // Then the best remaining inherited budget is simply the maximum
        // value outside that interval:
        // max(prefix before tin[x], suffix after tout[x])
        //
        // We use iterative DFS instead of recursive DFS because n can be
        // as large as 200000, and recursion depth could overflow the stack.
        long[] inherited = new long[n]; // inherited budget for each node
        int[] tin = new int[n];         // entry time / preorder index
        int[] tout = new int[n];        // last index inside subtree
        int[] eulerNodeAt = new int[n]; // which node appears at each preorder position

        // Stack entries:
        // (node, nextChildIndex)
        //
        // nextChildIndex tells us how many children of this node have already
        // been processed. This lets us simulate recursive DFS exactly.
        var stack = new Stack<(int node, int nextChildIndex)>();

        // Initialize root.
        inherited[0] = budget[0];
        int timer = 0;

        // Enter root.
        tin[0] = timer;
        eulerNodeAt[timer] = 0;
        timer++;

        stack.Push((0, 0));

        while (stack.Count > 0)
        {
            var top = stack.Pop();
            int node = top.node;
            int nextChildIndex = top.nextChildIndex;

            // If there are still children left to process,
            // we put the current node back with nextChildIndex + 1,
            // then process that child.
            if (nextChildIndex < children[node].Count)
            {
                // Put current node back so we can continue later.
                stack.Push((node, nextChildIndex + 1));

                int child = children[node][nextChildIndex];

                // Inherited budget of child:
                // inherited[child] = inherited[node] + budget[child]
                inherited[child] = inherited[node] + budget[child];

                // Record child's preorder entry.
                tin[child] = timer;
                eulerNodeAt[timer] = child;
                timer++;

                // Start processing child.
                stack.Push((child, 0));
            }
            else
            {
                // All children of this node are done.
                // Therefore the subtree of this node ends at timer - 1.
                tout[node] = timer - 1;
            }
        }

        // ------------------------------------------------------------
        // STEP 3: Build an array of inherited budgets in Euler order.
        // ------------------------------------------------------------
        // Why?
        // Because subtree intervals are contiguous in Euler order.
        // So if we freeze subtree x, we remove a contiguous segment
        // from this array.
        long[] eulerInherited = new long[n];
        for (int i = 0; i < n; i++)
        {
            int node = eulerNodeAt[i];
            eulerInherited[i] = inherited[node];
        }

        // ------------------------------------------------------------
        // STEP 4: Build prefix maximum and suffix maximum arrays.
        // ------------------------------------------------------------
        // prefixMax[i] = maximum inherited budget among eulerInherited[0..i]
        // suffixMax[i] = maximum inherited budget among eulerInherited[i..n-1]
        //
        // Then for any frozen subtree interval [L, R]:
        // - best on the left side is prefixMax[L - 1] if L > 0
        // - best on the right side is suffixMax[R + 1] if R + 1 < n
        // - answer after freezing that subtree is max(leftBest, rightBest)
        //
        // This turns each freeze evaluation into O(1).
        long[] prefixMax = new long[n];
        long[] suffixMax = new long[n];

        prefixMax[0] = eulerInherited[0];
        for (int i = 1; i < n; i++)
        {
            prefixMax[i] = Math.Max(prefixMax[i - 1], eulerInherited[i]);
        }

        suffixMax[n - 1] = eulerInherited[n - 1];
        for (int i = n - 2; i >= 0; i--)
        {
            suffixMax[i] = Math.Max(suffixMax[i + 1], eulerInherited[i]);
        }

        // ------------------------------------------------------------
        // STEP 5: Consider "no freeze" as one valid option.
        // ------------------------------------------------------------
        // If we do not freeze any subtree, the answer is simply the maximum
        // inherited budget among all nodes.
        long answer = prefixMax[n - 1];

        // ------------------------------------------------------------
        // STEP 6: Try freezing every possible non-root subtree.
        // ------------------------------------------------------------
        // We cannot freeze the CEO (node 0), so we test nodes 1..n-1.
        //
        // For each node x:
        // - Its subtree occupies Euler interval [tin[x], tout[x]]
        // - We want the maximum inherited budget outside that interval
        //
        // Since at least node 0 remains active for every allowed freeze,
        // there is always at least one valid remaining node.
        for (int x = 1; x < n; x++)
        {
            int left = tin[x];
            int right = tout[x];

            long bestRemaining = long.MinValue;

            // Best value strictly before the removed interval.
            if (left > 0)
            {
                bestRemaining = Math.Max(bestRemaining, prefixMax[left - 1]);
            }

            // Best value strictly after the removed interval.
            if (right + 1 < n)
            {
                bestRemaining = Math.Max(bestRemaining, suffixMax[right + 1]);
            }

            answer = Math.Max(answer, bestRemaining);
        }

        return answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int n1 = 5;
int[][] edges1 =
{
    new[] { 0, 1 },
    new[] { 0, 2 },
    new[] { 1, 3 },
    new[] { 1, 4 }
};
int[] budget1 = { 4, -2, 3, 5, -1 };
long result1 = solution.MaximumInheritedBudgetAfterOneFreeze(n1, edges1, budget1);
Console.WriteLine(result1); // Expected: 7

// Example 2
int n2 = 6;
int[][] edges2 =
{
    new[] { 0, 1 },
    new[] { 0, 2 },
    new[] { 1, 3 },
    new[] { 2, 4 },
    new[] { 2, 5 }
};
int[] budget2 = { 5, 4, -10, 8, 20, 1 };
long result2 = solution.MaximumInheritedBudgetAfterOneFreeze(n2, edges2, budget2);
Console.WriteLine(result2); // Expected: 17

// Additional small sanity check
int n3 = 1;
int[][] edges3 = Array.Empty<int[]>();
int[] budget3 = { -42 };
long result3 = solution.MaximumInheritedBudgetAfterOneFreeze(n3, edges3, budget3);
Console.WriteLine(result3); // Expected: -42