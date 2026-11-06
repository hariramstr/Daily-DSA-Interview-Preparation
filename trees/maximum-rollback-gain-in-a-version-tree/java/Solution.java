import java.util.*;

/*
 * Title: Maximum Rollback Gain in a Version Tree
 * Difficulty: Hard
 * Topic: Trees
 *
 * Problem Description:
 * You are given a rooted tree representing the history of a large software project.
 * Node 0 is the initial release, and every other node represents a version created
 * from exactly one parent version. Each node i has an integer value score[i], which
 * may be positive or negative, representing the net business impact of keeping that
 * version and all features introduced exactly at that node.
 *
 * A rollback operation may be applied to exactly one non-root node x. Rolling back at x
 * removes the entire subtree rooted at x from the active history. After the rollback,
 * the remaining tree must still contain the root and all nodes not in x's subtree.
 *
 * Your task is to compute the maximum possible "stability gain" obtainable by choosing
 * at most one rollback. The stability gain of rolling back node x is defined as:
 *
 *     (sum of scores in the remaining tree) - (minimum root-to-leaf path sum in the remaining tree)
 *
 * If you choose not to roll back any node, the original tree is used.
 *
 * Return the maximum stability gain over all valid choices.
 *
 * A leaf is any node with no children in the remaining tree after the rollback.
 * Note that rolling back one subtree can turn an internal node into a leaf.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - edges.length == n - 1
 * - 0 <= parent[i] < n for every non-root node
 * - score[i] is in the range [-10^9, 10^9]
 * - The input forms a valid rooted tree with root 0
 * - The answer may not fit in 32-bit signed integer
 *
 * Key idea of the solution:
 *
 * Let:
 * - total = sum of all scores
 * - pref[u] = sum of scores on the path root -> u
 *
 * If we remove subtree x:
 * - remaining total = total - subtreeSum[x]
 * - the new minimum root-to-leaf path sum must come from one of two sources:
 *   1) a leaf that was already outside subtree x
 *   2) an ancestor of x that becomes a new leaf because all of its children got removed
 *
 * Since only one subtree is removed, case (2) can happen only for parent[x], and only when x
 * was the parent's only child.
 *
 * Therefore for every non-root x:
 *   newMinLeafPathAfterRemoving(x) =
 *       min(
 *           minimum original leaf path sum among leaves outside subtree x,
 *           pref[parent[x]] if parent[x] becomes a leaf after removal
 *       )
 *
 * So we need:
 * - subtree sums
 * - Euler tour intervals [tin, tout] so "inside subtree" becomes an interval
 * - for each node x, the minimum leaf path sum outside x's subtree
 *   This can be answered with prefix/suffix minima over the Euler order of leaves
 *
 * The whole algorithm is O(n).
 */
public class Solution {

    /**
     * Computes the maximum stability gain obtainable by removing at most one subtree.
     *
     * @param parent parent[i] is the parent of node i, with parent[0] = -1
     * @param score score[i] is the value associated with node i
     * @return the maximum possible stability gain
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long maximumRollbackGain(int[] parent, int[] score) {
        int n = parent.length;
        if (n == 1) {
            return 0L;
        }

        int[] childCount = new int[n];
        int[] head = new int[n];
        Arrays.fill(head, -1);

        int[] to = new int[n - 1];
        int[] next = new int[n - 1];
        int edgePtr = 0;

        for (int i = 1; i < n; i++) {
            int p = parent[i];
            to[edgePtr] = i;
            next[edgePtr] = head[p];
            head[p] = edgePtr;
            edgePtr++;
            childCount[p]++;
        }

        long totalSum = 0L;
        for (int v : score) {
            totalSum += v;
        }

        int[] tin = new int[n];
        int[] tout = new int[n];
        int[] euler = new int[n];
        long[] pref = new long[n];
        long[] subtreeSum = new long[n];

        int[] stack = new int[n];
        int[] iter = new int[n];
        int top = 0;
        stack[top] = 0;
        iter[top] = head[0];
        pref[0] = score[0];

        int timer = 0;
        tin[0] = timer;
        euler[timer] = 0;
        timer++;

        while (top >= 0) {
            int u = stack[top];
            int ei = iter[top];

            if (ei != -1) {
                iter[top] = next[ei];
                int v = to[ei];

                top++;
                stack[top] = v;
                iter[top] = head[v];
                pref[v] = pref[u] + score[v];
                tin[v] = timer;
                euler[timer] = v;
                timer++;
            } else {
                long sum = score[u];
                for (int e = head[u]; e != -1; e = next[e]) {
                    sum += subtreeSum[to[e]];
                }
                subtreeSum[u] = sum;
                tout[u] = timer - 1;
                top--;
            }
        }

        long INF = Long.MAX_VALUE / 4;

        long[] leafPathAtEuler = new long[n];
        Arrays.fill(leafPathAtEuler, INF);

        for (int u = 0; u < n; u++) {
            if (childCount[u] == 0) {
                leafPathAtEuler[tin[u]] = pref[u];
            }
        }

        long[] prefixMin = new long[n];
        long[] suffixMin = new long[n];

        prefixMin[0] = leafPathAtEuler[0];
        for (int i = 1; i < n; i++) {
            prefixMin[i] = Math.min(prefixMin[i - 1], leafPathAtEuler[i]);
        }

        suffixMin[n - 1] = leafPathAtEuler[n - 1];
        for (int i = n - 2; i >= 0; i--) {
            suffixMin[i] = Math.min(suffixMin[i + 1], leafPathAtEuler[i]);
        }

        long originalMinLeaf = prefixMin[n - 1];
        long answer = totalSum - originalMinLeaf;

        for (int x = 1; x < n; x++) {
            long outsideMin = INF;

            if (tin[x] > 0) {
                outsideMin = Math.min(outsideMin, prefixMin[tin[x] - 1]);
            }
            if (tout[x] + 1 < n) {
                outsideMin = Math.min(outsideMin, suffixMin[tout[x] + 1]);
            }

            int p = parent[x];

            if (childCount[p] == 1) {
                outsideMin = Math.min(outsideMin, pref[p]);
            }

            long remainingTotal = totalSum - subtreeSum[x];
            long gain = remainingTotal - outsideMin;
            if (gain > answer) {
                answer = gain;
            }
        }

        return answer;
    }

    /**
     * Convenience overload that accepts scores as long[].
     *
     * @param parent parent array
     * @param score score array as long values
     * @return the maximum possible stability gain
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long maximumRollbackGain(int[] parent, long[] score) {
        int[] converted = new int[score.length];
        for (int i = 0; i < score.length; i++) {
            converted[i] = (int) score[i];
        }
        return maximumRollbackGain(parent, converted);
    }

    /**
     * Demonstrates the solution on sample-style inputs.
     *
     * @param args command-line arguments, not used
     * @return nothing
     *
     * Time complexity: O(1) for the demonstration itself, excluding the called solver
     * Space complexity: O(1) for the demonstration itself, excluding the called solver
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] parent1 = {-1, 0, 0, 1, 1, 2};
        int[] score1 = {5, -2, 4, -3, 6, -1};
        System.out.println(solution.maximumRollbackGain(parent1, score1));

        int[] parent2 = {-1, 0, 0, 1, 2, 2, 4};
        int[] score2 = {7, -5, 3, 10, -4, 6, -8};
        System.out.println(solution.maximumRollbackGain(parent2, score2));

        int[] parent3 = {-1};
        int[] score3 = {42};
        System.out.println(solution.maximumRollbackGain(parent3, score3));
    }
}