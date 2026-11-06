/*
Title: Maximum Rollback Gain in a Version Tree

Problem Description:
You are given a rooted tree representing the history of a large software project. Node 0 is the initial release, and every other node represents a version created from exactly one parent version. Each node i has an integer value score[i], which may be positive or negative, representing the net business impact of keeping that version and all features introduced exactly at that node.

A rollback operation may be applied to exactly one non-root node x. Rolling back at x removes the entire subtree rooted at x from the active history. After the rollback, the remaining tree must still contain the root and all nodes not in x's subtree.

Your task is to compute the maximum possible "stability gain" obtainable by choosing at most one rollback. The stability gain of rolling back node x is defined as:

(sum of scores in the remaining tree) - (minimum root-to-leaf path sum in the remaining tree)

If you choose not to roll back any node, the original tree is used.

Return the maximum stability gain over all valid choices.

A leaf is any node with no children in the remaining tree after the rollback. Note that rolling back one subtree can turn an internal node into a leaf.

Constraints:
- 1 <= n <= 200000
- edges.length == n - 1
- 0 <= parent[i] < n for every non-root node
- score[i] is in the range [-10^9, 10^9]
- The answer may not fit in 32-bit signed integer
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n log n)
    Space Complexity: O(n)

    High-level idea:
    1. Compute:
       - subtreeSum[u]: sum of scores inside subtree u
       - prefix[u]: root-to-u path sum
       - tin/tout Euler tour times so every subtree becomes one contiguous interval
       - bestLeafInSubtree[u]: minimum root-to-leaf path sum among leaves inside subtree u

    2. If we remove subtree x:
       remainingTotal = totalSum - subtreeSum[x]

       The new minimum root-to-leaf path in the remaining tree can come from two sources:
       A) A leaf that was already outside subtree x.
          We need the minimum original leaf path sum outside x's Euler interval.
       B) An ancestor of x that becomes a new leaf because all of its children are removed.
          This can only happen on the path from parent[x] upward, but in fact only the parent of x
          can become a new leaf after removing exactly one subtree, and only if x was its only child.
          More generally, if parent[x] becomes leaf, its path sum is prefix[parent[x]].

       So:
       newMinLeaf = min(
           minimum original leaf path sum outside subtree x,
           prefix[parent[x]] if parent[x] exists and childCount[parent[x]] == 1
       )

       Then answer for x is:
       (totalSum - subtreeSum[x]) - newMinLeaf

    3. Also compare with "no rollback":
       totalSum - originalMinLeaf

    Important subtlety:
    We only need to consider the parent of x as a possible new leaf.
    Removing one subtree cannot disconnect siblings, so higher ancestors cannot suddenly lose all children
    unless the immediate parent itself was their only child and also got removed, but the parent is not removed.
    Therefore only parent[x] can newly become a leaf.

    To get "minimum original leaf path sum outside subtree x" efficiently:
    - Build an array leafValueAtEuler[tin[leaf]] = prefix[leaf], and INF elsewhere.
    - Prefix minima + suffix minima on Euler order let us query minimum outside any interval in O(1).
    */
    public long MaximumRollbackGain(int[] parent, int[] score)
    {
        int n = parent.Length;
        var children = new List<int>[n];
        for (int i = 0; i < n; i++) children[i] = new List<int>();

        for (int i = 1; i < n; i++)
        {
            children[parent[i]].Add(i);
        }

        int[] childCount = new int[n];
        for (int i = 0; i < n; i++) childCount[i] = children[i].Count;

        long[] prefix = new long[n];
        long[] subtreeSum = new long[n];
        long[] bestLeafInSubtree = new long[n];
        int[] tin = new int[n];
        int[] tout = new int[n];
        int[] eulerToNode = new int[n];

        const long INF = long.MaxValue / 4;

        long totalSum = 0;
        int timer = 0;

        // We use an explicit stack instead of recursion because n can be 200000,
        // and deep recursion could overflow the call stack.
        // state = 0 means "enter node"
        // state = 1 means "exit node"
        var stack = new Stack<(int node, int state)>();
        stack.Push((0, 0));
        prefix[0] = score[0];

        while (stack.Count > 0)
        {
            var (u, state) = stack.Pop();

            if (state == 0)
            {
                // Entering node u:
                // - assign Euler entry time
                // - schedule exit processing
                // - push children for DFS
                tin[u] = timer;
                eulerToNode[timer] = u;
                timer++;

                stack.Push((u, 1));

                // Push children in reverse order so traversal order is stable/readable.
                for (int i = children[u].Count - 1; i >= 0; i--)
                {
                    int v = children[u][i];
                    prefix[v] = prefix[u] + score[v];
                    stack.Push((v, 0));
                }
            }
            else
            {
                // Exiting node u:
                // Now all children have already been processed, so we can aggregate
                // subtree sums and best leaf values from children upward.
                long sum = score[u];
                long bestLeaf = INF;

                if (children[u].Count == 0)
                {
                    // If u is an original leaf, the minimum root-to-leaf path inside its subtree
                    // is simply the path sum to u itself.
                    bestLeaf = prefix[u];
                }
                else
                {
                    // Combine all children contributions.
                    foreach (int v in children[u])
                    {
                        sum += subtreeSum[v];
                        if (bestLeafInSubtree[v] < bestLeaf)
                            bestLeaf = bestLeafInSubtree[v];
                    }
                }

                subtreeSum[u] = sum;
                bestLeafInSubtree[u] = bestLeaf;
                totalSum += score[u];

                // Euler exit time is inclusive.
                tout[u] = timer - 1;
            }
        }

        // Build an Euler-indexed array that stores the root-to-leaf path sum at leaf positions,
        // and INF at non-leaf positions.
        // This allows "minimum leaf path sum outside subtree interval" queries using prefix/suffix minima.
        long[] leafValueAtEuler = new long[n];
        for (int i = 0; i < n; i++) leafValueAtEuler[i] = INF;

        for (int u = 0; u < n; u++)
        {
            if (childCount[u] == 0)
            {
                leafValueAtEuler[tin[u]] = prefix[u];
            }
        }

        long[] prefMin = new long[n];
        long[] suffMin = new long[n];

        for (int i = 0; i < n; i++)
        {
            if (i == 0) prefMin[i] = leafValueAtEuler[i];
            else prefMin[i] = Math.Min(prefMin[i - 1], leafValueAtEuler[i]);
        }

        for (int i = n - 1; i >= 0; i--)
        {
            if (i == n - 1) suffMin[i] = leafValueAtEuler[i];
            else suffMin[i] = Math.Min(suffMin[i + 1], leafValueAtEuler[i]);
        }

        // Case 1: no rollback at all.
        long answer = totalSum - bestLeafInSubtree[0];

        // Case 2: rollback exactly one non-root subtree x.
        for (int x = 1; x < n; x++)
        {
            long remainingTotal = totalSum - subtreeSum[x];

            // Minimum original leaf path sum that lies OUTSIDE subtree x.
            // Since subtree x corresponds to Euler interval [tin[x], tout[x]],
            // leaves outside it are in:
            // - [0, tin[x)-1]
            // - (tout[x]+1, n-1]
            long outsideMin = INF;

            if (tin[x] > 0)
                outsideMin = Math.Min(outsideMin, prefMin[tin[x] - 1]);

            if (tout[x] + 1 < n)
                outsideMin = Math.Min(outsideMin, suffMin[tout[x] + 1]);

            // Possible newly created leaf:
            // parent[x] becomes a leaf iff x was its only child.
            int p = parent[x];
            long newLeafCandidate = INF;
            if (p >= 0 && childCount[p] == 1)
            {
                newLeafCandidate = prefix[p];
            }

            long newMinLeaf = Math.Min(outsideMin, newLeafCandidate);

            // There is always at least one remaining leaf because root remains,
            // and if all descendants are removed from root's only child chain,
            // some ancestor (possibly root) becomes a leaf.
            // Since x is non-root, the remaining tree is non-empty.
            long gain = remainingTotal - newMinLeaf;
            if (gain > answer) answer = gain;
        }

        return answer;
    }
}

// Demo code
var solution = new Solution();

int[] parent1 = { -1, 0, 0, 1, 1, 2 };
int[] score1 = { 5, -2, 4, -3, 6, -1 };
Console.WriteLine(solution.MaximumRollbackGain(parent1, score1));

int[] parent2 = { -1, 0, 0, 1, 2, 2, 4 };
int[] score2 = { 7, -5, 3, 10, -4, 6, -8 };
Console.WriteLine(solution.MaximumRollbackGain(parent2, score2));