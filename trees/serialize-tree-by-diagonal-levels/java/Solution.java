```java
/*
 * Title: Serialize Tree by Diagonal Levels
 * Difficulty: Hard
 * Topic: Trees
 *
 * Problem Description:
 * Given the root of a binary tree, serialize the tree by grouping nodes along their diagonals.
 * A diagonal is defined by the slope going from top-right to bottom-left: nodes reachable only
 * by going right from their parent share the same diagonal as the parent, while going left
 * increases the diagonal index by 1.
 *
 * Return a list of lists where each inner list contains the node values along a diagonal,
 * ordered from top to bottom within that diagonal, and diagonals are ordered from the leftmost
 * (highest index) to the rightmost (index 0).
 *
 * After serializing, you must also return the minimum number of diagonals needed such that
 * no two nodes in the same diagonal have a value difference less than or equal to K.
 *
 * Constraints:
 * - The number of nodes in the tree is in the range [1, 10^4]
 * - -10^5 <= Node.val <= 10^5
 * - 0 <= K <= 10^5
 * - Node values may not be unique
 */

import java.util.*;

/**
 * Solution class for Serialize Tree by Diagonal Levels problem.
 * This solution groups binary tree nodes by their diagonal index and then
 * computes the minimum number of diagonals needed so that no two nodes
 * in the same diagonal have a value difference <= K.
 */
public class Solution {

    /**
     * Inner class representing a node in the binary tree.
     */
    static class TreeNode {
        int val;
        TreeNode left, right;

        TreeNode(int val) {
            this.val = val;
        }

        TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }

    /**
     * Serializes the binary tree by diagonal levels.
     *
     * A diagonal is defined such that:
     * - Moving RIGHT from a parent keeps the same diagonal index.
     * - Moving LEFT from a parent increases the diagonal index by 1.
     *
     * The result is ordered from the leftmost diagonal (highest index) to
     * the rightmost diagonal (index 0).
     *
     * @param root The root of the binary tree.
     * @return A list of lists, where each inner list contains node values along a diagonal,
     *         ordered from leftmost diagonal to rightmost diagonal.
     *         Time complexity: O(N) where N is the number of nodes.
     *         Space complexity: O(N) for storing all nodes in the map and queue.
     */
    public List<List<Integer>> serializeByDiagonals(TreeNode root) {
        // Result list to hold all diagonals
        List<List<Integer>> result = new ArrayList<>();

        // Edge case: if root is null, return empty list
        if (root == null) {
            return result;
        }

        // We use a TreeMap to store diagonal index -> list of node values
        // TreeMap keeps keys sorted in ascending order (diagonal 0, 1, 2, ...)
        // Diagonal 0 is the rightmost diagonal (root's diagonal)
        // Higher diagonal index = further left
        TreeMap<Integer, List<Integer>> diagonalMap = new TreeMap<>();

        // We use a Queue for BFS traversal
        // Each entry in the queue is a pair: (TreeNode, diagonalIndex)
        // We use a simple array of size 2 to represent the pair
        Queue<Object[]> queue = new LinkedList<>();

        // Start BFS from root at diagonal index 0
        queue.offer(new Object[]{root, 0});

        // BFS traversal
        while (!queue.isEmpty()) {
            // Dequeue the front element
            Object[] current = queue.poll();
            TreeNode node = (TreeNode) current[0];
            int diagIndex = (int) current[1];

            // Add this node's value to the appropriate diagonal list
            // computeIfAbsent creates a new ArrayList if the key doesn't exist
            diagonalMap.computeIfAbsent(diagIndex, k -> new ArrayList<>()).add(node.val);

            // If the node has a LEFT child, it goes to diagonal index + 1
            // (going left increases the diagonal index)
            if (node.left != null) {
                queue.offer(new Object[]{node.left, diagIndex + 1});
            }

            // If the node has a RIGHT child, it stays on the SAME diagonal index
            // (going right keeps the same diagonal)
            if (node.right != null) {
                queue.offer(new Object[]{node.right, diagIndex});
            }
        }

        // Now we need to order the result from leftmost (highest index) to rightmost (index 0)
        // Since TreeMap is sorted in ascending order, we need to reverse it
        // We iterate in descending order of keys
        for (Map.Entry<Integer, List<Integer>> entry : diagonalMap.descendingMap().entrySet()) {
            result.add(entry.getValue());
        }

        return result;
    }

    /**
     * Computes the minimum number of diagonals needed such that no two nodes
     * in the same diagonal have a value difference less than or equal to K.
     *
     * The approach:
     * 1. First, serialize the tree by diagonals.
     * 2. For each diagonal, check if any two nodes have |val1 - val2| <= K.
     * 3. If a diagonal needs splitting, we greedily assign nodes to sub-diagonals
     *    using a greedy interval graph coloring approach (similar to interval scheduling).
     *
     * The "minimum number of diagonals" means: across all original diagonals,
     * what is the maximum number of sub-groups needed for any single diagonal?
     * Because each original diagonal can be split independently.
     *
     * Wait - re-reading the problem: "minimum number of diagonals needed such that
     * no two nodes in the same diagonal have a value difference less than or equal to K"
     * This means we need to re-partition ALL nodes into new diagonals.
     *
     * Based on Example 1: diagonals = [[8,10,14],[3,6,7],[1,4,13]], K=2
     * |10-8|=2 which is NOT strictly greater than K=2, so it needs a split.
     * min_splits = 2.
     *
     * Based on Example 2: diagonals = [[1,3],[2]], K=5
     * |3-1|=2 <= 5, but min_splits = 1 (no extra split needed beyond natural diagonals).
     *
     * Looking at Example 2 more carefully: min_splits=1 means the natural diagonals
     * already work (1 set of diagonals). Example 1: min_splits=2 means we need 2 sets.
     *
     * Actually, re-reading: "minimum number of diagonals" - I think this means the
     * total count of diagonals after splitting. In Example 2, we have 2 diagonals
     * and no splitting needed, so min_splits=1? That doesn't match count=2.
     *
     * Let me re-read Example 1: min_splits=2. We have 3 diagonals. Diagonal 0 has
     * [8,10,14] where |10-8|=2 <= K=2. So diagonal 0 needs splitting into 2 groups.
     * The answer is 2, which seems to be the maximum splits needed for any single diagonal.
     *
     * Example 2: min_splits=1. Diagonal 0 has [1,3] where |3-1|=2 <= K=5.
     * But min_splits=1... This means diagonal 0 needs to be split into groups,
     * but the answer is 1? That means no additional splits needed?
     *
     * Wait - "no two nodes in the same diagonal have a value difference less than or equal to K"
     * means we want |val1 - val2| > K for all pairs in the same diagonal.
     * In Example 2, |3-1|=2 <= 5=K, so [1,3] violates the condition.
     * Yet min_splits=1. This is contradictory unless min_splits counts something else.
     *
     * Perhaps min_splits is the number of ADDITIONAL diagonals needed (splits beyond original).
     * Example 1: diagonal 0 needs 1 extra split -> min_splits = 2 total for that diagonal.
     * But the answer is 2, not 1 extra.
     *
     * Perhaps min_splits = max over all diagonals of (number of groups needed for that diagonal).
     * Example 1: diagonal 0 [8,10,14] needs 2 groups (e.g., [8,14] and [10] or similar).
     * min_splits = 2. That matches!
     * Example 2: diagonal 0 [1,3] needs... with K=5, |3-1|=2<=5, so they can't be together.
     * That would need 2 groups. But min_splits=1. Contradiction again.
     *
     * Let me reconsider Example 2: "min_splits = 1 (no extra split needed beyond the natural diagonals)"
     * The explanation says no extra split needed. So min_splits=1 means 1 split = no extra splits
     * (the original diagonals count as 1 "split" or partition).
     *
     * So min_splits = 1 means the original diagonal structure is sufficient.
     * min_splits = 2 means we need to split at least one diagonal into 2 parts.
     *
     * But in Example 2, diagonal 0 has [1,3] with K=5, and |3-1|=2 <= 5, which violates
     * the condition. Yet min_splits=1 (no extra split). This is still contradictory.
     *
     * Unless the condition is: no two nodes in the same diagonal have value difference
     * STRICTLY LESS THAN K (i.e., |val1-val2| < K, not <= K).
     * Example 1: |10-8|=2, K=2. 2 < 2 is false. But the explanation says "not strictly greater
     * than K=2, requiring a split". So the condition for needing a split is |diff| <= K.
     *
     * I think the problem statement might have an inconsistency in Example 2.
     * Let me just implement: for each diagonal, find the minimum number of groups
     * such that within each group, all pairs have |val1-val2| > K.
     * The answer (min_splits) = max over all diagonals of groups needed.
     * If all diagonals need only 1 group, min_splits = 1.
     *
     * For Example 2, diagonal [1,3] with K=5: |3-1|=2 <= 5, so they conflict.
     * We'd need 2 groups. But the expected answer is 1. So my interpretation is wrong.
     *
     * Alternative: min_splits = total number of extra diagonals created.
     * Example 1: diagonal 0 splits into 2 -> 1 extra. min_splits = 2 (total diagonals after split)?
     * Original: 3 diagonals. After splitting diagonal 0 into 2: 4 diagonals. But answer is 2.
     *
     * I'll go with: min_splits = max number of groups needed for any single diagonal.
     * For Example 2, maybe the check is |diff| < K (strictly less than), not <= K.
     * |3-1|=2, K=5. 2 < 5 is true... still needs split.
     *
     * OR maybe the condition is |diff| > K (strictly greater), meaning nodes are "too different"
     * and need to be in separate diagonals? That's the opposite.
     *
     * Let me try: condition for conflict = |val1-val2| <= K means they're "too similar"
     * and must be separated. For Example 2 with K=5: [1,3] has |3-1|=2<=5, conflict.
     * But answer is 1. Unless the problem means something different by "min_splits".
     *
     * Given the examples, I'll implement: min_splits = max over all diagonals of
     * (minimum number of groups to separate conflicting nodes), where conflict means
     * |val1-val2| <= K. For Example 2, this would give 2, not 1.
     *
     * Since Example 2 says min_splits=1 with explanation "no extra split needed",
     * perhaps min_splits counts the number of EXTRA splits (beyond 0).
     * Example 1: 1 diagonal needs splitting into 2 -> 1 extra split -> but answer is 2.
     *
     * I'll just implement the most logical interpretation: min_splits = max chromatic number
     * needed across all diagonals, where two nodes conflict if |val1-val2| <= K.
     * This gives Example 1 answer = 2 (correct) and Example 2 answer = 2 (may differ from expected).
     * But since Example 2's explanation seems inconsistent, I'll go with this.
     *
     * Actually wait - re-reading Example 2: "min_splits = 1 (no extra split needed beyond the
     * natural diagonals)". Maybe min_splits represents whether ANY splitting is needed at all:
     * 1 = no splitting needed (all natural diagonals are valid), 2 = splitting needed.
     * That would be a boolean essentially. Example 1 needs splitting -> 2. Example 2 doesn't -> 1.
     * But Example 2 DOES need splitting (|3-1|=2 <= K=5)...
     *
     * I think there might be an error in Example 2's expected output or explanation.
     * I'll implement the greedy graph coloring approach and return the max groups needed.
     *
     * @param root The root of the binary tree.
     * @param K    The threshold value.
     * @return An array where index 0 contains the serialized diagonals and index 1 contains
     *         the minimum number of diagonals (as an Integer).
     *         Time complexity: O(N log N) due to sorting within each diagonal for greedy coloring.
     *         Space complexity: O(N) for storing all node values.
     */
    public int computeMinSplits(List<List<Integer>> diagonals, int K) {
        // We need to find, for each diagonal, the minimum number of groups
        // such that no two nodes in the same group have |val1 - val2| <= K.
        // This is equivalent to graph coloring where edges connect nodes with |diff| <= K.
        //
        // For a sorted list of values, the conflict graph is an interval graph.
        // The chromatic number of an interval graph equals its clique number.
        // The clique number = maximum number of values that are all within K of each other.
        //
        // So for each diagonal, sort the values and find the maximum number of values
        // that fall within a window of size K (i.e., max - min <= K).
        // That maximum window size is the chromatic number for that diagonal.

        int maxGroups = 1; // At minimum, we need 1 group (the original diagonal)

        for (List<Integer> diagonal : diagonals) {
            if (diagonal.size() <= 1) {
                // A single node never conflicts with itself
                continue;
            }

            // Sort the values in this diagonal
            List<Integer> sorted = new ArrayList<>(diagonal);
            Collections.sort(sorted);

            // Use a sliding window to find the maximum number of values
            // within a range of size K (i.e., sorted[right] - sorted[left] <= K)
            int maxInWindow = 1;
            int left = 0;

            for (int right = 1; right < sorted.size(); right++) {
                // Shrink window from left while the range exceeds K
                while (sorted.get(right) - sorted.get(left) <= K) {
                    // All values from left to right are within K of each other
                    // They all conflict with each other
                    // We need at least (right - left + 1) groups for this window
                    maxInWindow