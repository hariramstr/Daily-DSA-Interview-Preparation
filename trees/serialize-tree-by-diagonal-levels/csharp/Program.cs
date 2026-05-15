/*
 * Title: Serialize Tree by Diagonal Levels
 * Difficulty: Hard
 * Topic: Trees
 *
 * Problem Description:
 * Given the root of a binary tree, serialize the tree by grouping nodes along their diagonals.
 * A diagonal is defined by the slope going from top-right to bottom-left:
 *   - Nodes reachable only by going RIGHT from their parent share the SAME diagonal as the parent.
 *   - Going LEFT increases the diagonal index by 1.
 *
 * Return a list of lists where each inner list contains the node values along a diagonal,
 * ordered from top to bottom within that diagonal, and diagonals are ordered from the
 * leftmost (highest index) to the rightmost (index 0).
 *
 * After serializing, also return the minimum number of diagonals needed such that
 * no two nodes in the same diagonal have a value difference <= K.
 *
 * Example 1:
 *   Input: root = [8, 3, 10, 1, 6, null, 14, null, null, 4, 7, 13], K = 2
 *   Output: diagonals = [[8,10,14],[3,6,7],[1,4,13]], min_splits = 2
 *
 * Example 2:
 *   Input: root = [1, 2, 3], K = 5
 *   Output: diagonals = [[1,3],[2]], min_splits = 1
 */

using System;
using System.Collections.Generic;
using System.Linq;

// ─── Tree node definition ────────────────────────────────────────────────────
public class TreeNode
{
    public int Val;
    public TreeNode? Left;
    public TreeNode? Right;
    public TreeNode(int val, TreeNode? left = null, TreeNode? right = null)
    {
        Val = val; Left = left; Right = right;
    }
}

// ─── Solution class ──────────────────────────────────────────────────────────
public class Solution
{
    /*
     * SerializeByDiagonal
     *
     * Time Complexity : O(N log N) per diagonal for the greedy coloring step,
     *                   O(N) overall for the BFS traversal.
     *                   Total: O(N log N) in the worst case.
     *
     * Space Complexity: O(N) for the dictionary that maps diagonal index → list of values,
     *                   plus O(N) for the BFS queue.
     *
     * High-level idea
     * ───────────────
     * 1. BFS (level-order) traversal of the tree, carrying a "diagonal index" with
     *    each node.  Going RIGHT keeps the same index; going LEFT increments it by 1.
     * 2. Collect all node values into a Dictionary<int, List<int>> keyed by diagonal index.
     * 3. Sort the keys so we can emit diagonals from index 0 (rightmost) to max (leftmost).
     *    The problem asks for leftmost-first, so we reverse the order.
     * 4. For each diagonal list, run a greedy interval-graph coloring to find the minimum
     *    number of "groups" (splits) such that no two values in the same group differ by <= K.
     *    We take the maximum such count across all diagonals.
     */
    public (List<List<int>> Diagonals, int MinSplits) SerializeByDiagonal(TreeNode? root, int K)
    {
        // ── Step 1: Handle the empty-tree edge case ───────────────────────────
        // If the root is null there are no nodes, so return empty results.
        if (root == null)
            return (new List<List<int>>(), 0);

        // ── Step 2: BFS queue stores (node, diagonalIndex) pairs ─────────────
        // We use a Queue so we process nodes level by level (breadth-first).
        // Carrying the diagonal index alongside each node avoids a separate
        // recursive pass and keeps the logic self-contained.
        var queue = new Queue<(TreeNode Node, int DiagIdx)>();
        queue.Enqueue((root, 0));

        // ── Step 3: Dictionary to accumulate values per diagonal ──────────────
        // Key   = diagonal index (0 = rightmost diagonal containing the root)
        // Value = list of node values encountered on that diagonal, top-to-bottom
        //         (BFS naturally gives us top-to-bottom order within a diagonal)
        var diagMap = new Dictionary<int, List<int>>();

        // ── Step 4: BFS traversal ─────────────────────────────────────────────
        while (queue.Count > 0)
        {
            var (node, diagIdx) = queue.Dequeue();

            // Record this node's value in the appropriate diagonal bucket.
            if (!diagMap.ContainsKey(diagIdx))
                diagMap[diagIdx] = new List<int>();
            diagMap[diagIdx].Add(node.Val);

            // Going LEFT → diagonal index increases by 1.
            // This is the core rule: a left edge "crosses" to the next diagonal.
            if (node.Left != null)
                queue.Enqueue((node.Left, diagIdx + 1));

            // Going RIGHT → diagonal index stays the same.
            // Right edges stay on the same diagonal as the parent.
            if (node.Right != null)
                queue.Enqueue((node.Right, diagIdx));
        }

        // ── Step 5: Sort diagonal indices and build the result list ───────────
        // The problem wants diagonals ordered from leftmost (highest index) to
        // rightmost (index 0), so we sort keys in DESCENDING order.
        var sortedKeys = diagMap.Keys.OrderByDescending(k => k).ToList();

        var diagonals = new List<List<int>>();
        foreach (var key in sortedKeys)
            diagonals.Add(diagMap[key]);

        // ── Step 6: Compute min_splits across all diagonals ───────────────────
        // For each diagonal we need the minimum number of groups such that
        // no two values in the same group have |a - b| <= K.
        //
        // Equivalently: two values CONFLICT if |a - b| <= K, i.e. they must be
        // in different groups.  This is an interval-graph coloring problem.
        //
        // Greedy approach (works optimally for interval graphs):
        //   Sort the values.  Maintain a list of "group maximums" (the largest
        //   value currently in each group).  For each new value v (ascending),
        //   try to place it in a group whose current max m satisfies v - m > K
        //   (i.e. they do NOT conflict).  If no such group exists, open a new group.
        //
        // Why sort ascending?  Because after sorting, v - m > K is the only
        // condition we need to check (v >= m always after sorting).
        //
        // The number of groups needed equals the maximum "clique size" in the
        // conflict graph, which equals the maximum number of values that all
        // mutually conflict (all within a window of size K of each other).

        int minSplits = 0;

        foreach (var diagList in diagonals)
        {
            int groupsNeeded = MinGroupsForDiagonal(diagList, K);
            // We want the MAXIMUM across all diagonals because we need every
            // diagonal to satisfy the constraint simultaneously.
            if (groupsNeeded > minSplits)
                minSplits = groupsNeeded;
        }

        return (diagonals, minSplits);
    }

    /*
     * MinGroupsForDiagonal
     *
     * Greedy interval-graph coloring.
     *
     * Given a list of values and a threshold K, find the minimum number of groups
     * such that no two values in the same group differ by <= K (i.e. every pair
     * in the same group must differ by MORE than K).
     *
     * Algorithm:
     *   1. Sort the values ascending.
     *   2. Keep a sorted list of "group maximums" (one per open group).
     *   3. For each value v, find a group whose max m satisfies v - m > K.
     *      - Among all eligible groups, pick the one with the LARGEST m
     *        (greedy: leave groups with smaller max available for future values).
     *      - If no eligible group exists, open a new group.
     *   4. Return the total number of groups.
     *
     * Time: O(n log n) per diagonal (sorting + binary search per element).
     */
    private int MinGroupsForDiagonal(List<int> values, int K)
    {
        if (values.Count == 0) return 0;

        // Sort so we process values in ascending order.
        var sorted = values.OrderBy(x => x).ToList();

        // groupMaxes holds the current maximum value of each open group, kept sorted.
        // Using a sorted list lets us binary-search for eligible groups efficiently.
        var groupMaxes = new SortedList<int, int>(); // key = max value, value = count (for duplicates)

        // Helper: add a value to the sorted multiset
        void AddToMultiset(int val)
        {
            if (groupMaxes.ContainsKey(val))
                groupMaxes[val]++;
            else
                groupMaxes[val] = 1;
        }

        // Helper: remove one occurrence of a value from the sorted multiset
        void RemoveFromMultiset(int val)
        {
            groupMaxes[val]--;
            if (groupMaxes[val] == 0)
                groupMaxes.Remove(val);
        }

        foreach (int v in sorted)
        {
            // We need a group whose max m satisfies v - m > K, i.e. m < v - K.
            // Among all such groups, pick the one with the largest m (best fit).
            //
            // In the sorted list, all keys < (v - K) are eligible.
            // The largest eligible key is the one just below (v - K).

            int threshold = v - K; // m must be strictly less than threshold

            // Find the largest key strictly less than threshold.
            // SortedList keys are in ascending order; we scan from the end
            // of the eligible range.  For efficiency we use a simple approach:
            // find the index of the first key >= threshold, then look one before it.

            int bestKey = int.MinValue;
            bool found = false;

            // Binary search for the largest key < threshold
            // We iterate keys (they are sorted) to find the rightmost key < threshold.
            // For large inputs a proper sorted structure (SortedSet + lower_bound) would
            // be faster, but SortedList suffices here given n <= 10^4.
            foreach (var key in groupMaxes.Keys)
            {
                if (key < threshold)
                {
                    bestKey = key; // keep updating; last one will be the largest
                    found = true;
                }
                else
                {
                    break; // keys are sorted ascending; no need to continue
                }
            }

            if (found)
            {
                // Place v into the group that currently has max = bestKey.
                // Update that group's max to v.
                RemoveFromMultiset(bestKey);
                AddToMultiset(v);
            }
            else
            {
                // No eligible group found → open a new group with max = v.
                AddToMultiset(v);
            }
        }

        // The total number of groups is the sum of all counts in the multiset.
        return groupMaxes.Values.Sum();
    }
}

// ─── Demo / Test code (top-level statements) ─────────────────────────────────

var solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// Tree:
//          8
//        /   \
//       3    10
//      / \     \
//     1   6    14
//        / \   /
//       4   7 13
//
// Diagonal 0 (rightmost): 8 → right→10 → right→14          = [8, 10, 14]
// Diagonal 1             : 8→left→3, 3→right→6, 6→right→7  = [3, 6, 7]
// Diagonal 2             : 3→left→1, 6→left→4, 14→left→13  = [1, 4, 13]
//
// Output (leftmost first): [[1,4,13],[3,6,7],[8,10,14]]
// Wait — re-read: "diagonals ordered from leftmost (highest index) to rightmost (index 0)"
// So the order is: diag2=[1,4,13], diag1=[3,6,7], diag0=[8,10,14]
// But the expected output shows [[8,10,14],[3,6,7],[1,4,13]] which is index 0 first.
// Let's re-read: "diagonals are ordered from the leftmost (highest index) to the rightmost (index 0)"
// Hmm, the example output is [[8,10,14],[3,6,7],[1,4,13]] = index 0,1,2 order (rightmost first).
// The problem statement says leftmost first but the example shows rightmost first.
// We trust the EXAMPLE output: index 0 first (ascending key order).

// Re-checking: the problem says "leftmost (highest index) to rightmost (index 0)".
// Example output: [[8,10,14],[3,6,7],[1,4,13]] → diag0, diag1, diag2 → ascending index.
// That contradicts "leftmost first". We follow the example (ascending index = rightmost first).

// NOTE: We will output in ascending diagonal index order to match the examples.

// Let's rebuild with ascending order to match examples.
// (The solution above uses descending; we'll fix the demo by re-sorting.)

// Actually let's just re-implement the call and sort ascending for display:

TreeNode BuildExample1()
{
    //          8
    //        /   \
    //       3    10
    //      / \     \
    //     1   6    14
    //        / \   /
    //       4   7 13
    var n13 = new TreeNode(13);
    var n14 = new TreeNode(14, n13, null);
    var n4  = new TreeNode(4);
    var n7  = new TreeNode(7);
    var n6  = new TreeNode(6, n4, n7);
    var n1  = new TreeNode(1);
    var n3  = new TreeNode(3, n1, n6);
    var n10 = new TreeNode(10, null, n14);
    var n8  = new TreeNode(8, n3, n10);
    return n8;
}

TreeNode BuildExample2()
{
    //    1
    //   / \
    //  2   3
    return new TreeNode(1, new TreeNode(2), new TreeNode(3));
}

// ── Helper: reorder diagonals ascending (index 0 first) ──────────────────────
// The solution returns them descending (leftmost first per problem text).
// The examples show ascending order, so we reverse for display.
List<List<int>> AscendingOrder(List<List<int>> diags)
{
    var copy = new List<List<int>>(diags);
    copy.Reverse();
    return copy;
}

Console.WriteLine("=== Example 1 ===");
var (diags1, splits1) = solution.SerializeByDiagonal(BuildExample1(), K: 2);
var diags1Asc = AscendingOrder(diags1);
Console.WriteLine("Diagonals (index 0 first):");
foreach (var d in diags1Asc)
    Console.WriteLine("  [" + string.Join(", ", d) + "]");
Console.WriteLine($"min_splits = {splits1}");
Console.WriteLine("Expected: [[8,10,14],[3,6,7],[1,4,13]], min_splits = 2");
Console.WriteLine();

Console.WriteLine("=== Example