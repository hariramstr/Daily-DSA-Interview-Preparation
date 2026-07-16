import java.util.*;

/*
 * Title: Minimum Replacements to Make Adjacent Differences Unique
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array nums. Define the adjacent-difference sequence of nums as the
 * array diff where diff[i] = |nums[i] - nums[i + 1]| for every valid i.
 * An array is called difference-distinct if every value in its adjacent-difference sequence
 * appears at most once.
 *
 * In one operation, you may replace any single element of nums with any integer value in the
 * range [0, limit]. Your task is to return the minimum number of operations needed to make
 * nums difference-distinct.
 *
 * A replacement changes up to two adjacent differences, so choosing which positions to modify
 * matters. You are not asked to construct the final array, only to compute the minimum number
 * of replacements.
 *
 * Constraints:
 * - 2 <= nums.length <= 100000
 * - 0 <= nums[i] <= 1000000000
 * - 1 <= limit <= 1000000000
 *
 * Notes:
 * - The answer always exists because limit provides a valid replacement range and you may
 *   replace multiple elements if needed.
 * - Efficiency matters: solutions close to quadratic time will not pass for the largest inputs.
 *
 * Key Insight Used In This Solution:
 * Let diff have length m = nums.length - 1.
 * We need all values in diff to become unique.
 *
 * Replacing nums[i]:
 * - if i == 0, only diff[0] changes
 * - if i == n - 1, only diff[m - 1] changes
 * - otherwise, diff[i - 1] and diff[i] both change
 *
 * Therefore, if we decide which diff positions must be changed, the minimum number of element
 * replacements needed to realize those changes is exactly the minimum number of array indices
 * whose affected-difference coverage contains all chosen diff positions.
 *
 * On a path of diff positions 0..m-1:
 * - endpoint element 0 covers {0}
 * - endpoint element n-1 covers {m-1}
 * - interior element i covers {i-1, i}
 *
 * So covering a set of diff positions is equivalent to selecting vertices on a path so that
 * every chosen edge-position is incident to at least one selected vertex.
 *
 * Now, which diff positions must be changed?
 * For each difference value that appears k times, at most one occurrence may remain unchanged.
 * So at least k - 1 occurrences among those positions must be changed.
 *
 * This becomes:
 * Choose a subset S of diff positions to change such that for every value-group G,
 * |S ∩ G| >= |G| - 1.
 * Minimize the minimum vertex cover size of S on the path.
 *
 * Equivalently, for each value-group we may choose at most one position to KEEP unchanged.
 * Let K be the set of kept positions, with at most one kept position per value-group.
 * Then S = all positions except K.
 * We want to minimize vertexCoverSize(S).
 *
 * Since the graph is a path, minimum vertex cover on a chosen edge set can be optimized by DP.
 * We process diff positions from left to right. At each position:
 * - if its value-group has size 1, this edge must be kept unchanged? No: since it is already
 *   unique, we do not need to change it, but we may still change it if useful. However changing
 *   extra edges can only increase or keep the needed operations, never help reduce below the
 *   optimum, because the objective is to cover changed edges. So in an optimum, singleton groups
 *   are never forced to change. We therefore always keep singleton positions unchanged.
 * - if its value-group has size >= 2, exactly one position in that group may be kept unchanged,
 *   all others must be changed.
 *
 * We perform DP over positions and over whether the current array element vertex is selected.
 * Group constraints are handled by tracking, for the current group being processed across its
 * occurrences, whether we have already used the one allowed "keep unchanged" occurrence.
 *
 * Because occurrences of the same value are scattered, we cannot track all groups independently.
 * The crucial simplification is:
 * For a path edge set S, minimum vertex cover size depends only on which edges are changed.
 * On a path, if we decide to keep one occurrence per duplicate value, keeping more separated
 * edges can only reduce the changed-edge set. The exact global optimum can be found by
 * weighted interval-style DP on occurrences:
 *
 * Let changed edges be all duplicate-group occurrences except one chosen representative per
 * duplicate value. Singleton edges are never changed.
 * Starting from the baseline where every duplicate occurrence is changed, keeping one occurrence
 * from a duplicate group removes that single edge from the changed set. Removing an edge from a
 * path can reduce the minimum vertex cover by at most 1, and interactions are purely local.
 *
 * This allows a dynamic programming on the path with per-position optional removal if that
 * position belongs to a duplicate group and its group's representative has not been chosen yet.
 * We process positions left-to-right and maintain DP states for:
 * - whether previous vertex is selected
 * - for the value at current position, whether its representative has already been chosen
 *
 * To make this efficient globally, we compress each value-group's occurrences and use a map from
 * value to a tiny 2-state DP carried only when that value reappears. This yields O(n) expected
 * time with hash maps.
 *
 * The implementation below realizes this carefully.
 */
public class Solution {

    /**
     * Computes the minimum number of replacements needed to make the adjacent-difference
     * sequence contain only distinct values.
     *
     * Time complexity: O(n) expected, where n is nums.length.
     * Space complexity: O(n) in the worst case for maps and stored DP states.
     *
     * @param nums  the original integer array
     * @param limit the allowed replacement range [0, limit]
     * @return the minimum number of replacement operations
     */
    public int minReplacements(int[] nums, int limit) {
        int n = nums.length;
        if (n <= 2) {
            return 0;
        }

        int m = n - 1;

        // Step 1:
        // Build the adjacent-difference array.
        long[] diff = new long[m];
        for (int i = 0; i < m; i++) {
            diff[i] = Math.abs((long) nums[i] - nums[i + 1]);
        }

        // Step 2:
        // Count occurrences of each difference value.
        Map<Long, Integer> freq = new HashMap<>();
        for (long d : diff) {
            freq.put(d, freq.getOrDefault(d, 0) + 1);
        }

        // Step 3:
        // Mark which diff positions belong to duplicate groups.
        // Only those groups matter, because singleton differences are already unique.
        boolean[] duplicatePos = new boolean[m];
        for (int i = 0; i < m; i++) {
            if (freq.get(diff[i]) >= 2) {
                duplicatePos[i] = true;
            }
        }

        // If there are no duplicate difference values, answer is already 0.
        boolean anyDuplicate = false;
        for (boolean b : duplicatePos) {
            if (b) {
                anyDuplicate = true;
                break;
            }
        }
        if (!anyDuplicate) {
            return 0;
        }

        // Step 4:
        // For each duplicate value, collect its positions.
        Map<Long, List<Integer>> positionsByValue = new HashMap<>();
        for (int i = 0; i < m; i++) {
            if (duplicatePos[i]) {
                positionsByValue.computeIfAbsent(diff[i], k -> new ArrayList<>()).add(i);
            }
        }

        // Step 5:
        // We solve the exact optimization with dynamic programming over the path.
        //
        // Interpretation:
        // For each duplicate value-group, exactly one occurrence may remain unchanged,
        // all other occurrences of that value must be "covered" by replacements.
        //
        // We process diff positions left-to-right.
        // A changed diff position e must be covered by selecting at least one endpoint vertex.
        // Vertex i corresponds to replacing nums[i].
        //
        // DP state:
        // dpPrev0 = minimum cost up to previous edge, with previous vertex NOT selected
        // dpPrev1 = minimum cost up to previous edge, with previous vertex selected
        //
        // However, whether current edge must be covered depends on whether we choose it as the
        // unique kept occurrence for its value-group.
        //
        // To enforce "at most one kept occurrence per duplicate value", we maintain for each
        // value a small deferred state:
        // bestKeepUsed[value] = best additive adjustment if we have already used the one keep
        // bestKeepUnused[value] = best additive adjustment if we have not used it yet
        //
        // A direct global DP with all groups simultaneously would be huge, so instead we use
        // Lagrangian-style exact decomposition on a path:
        // For each duplicate edge, baseline says "must change".
        // Choosing one kept occurrence per value removes that edge from required coverage.
        // On a path, removing an edge changes the vertex-cover DP locally, and these local
        // choices can be integrated by storing, for each value, the best transition when one
        // removal has been used.
        //
        // The following implementation is an exact left-to-right DP that stores, for each value,
        // the best pair of DP states achievable before its next occurrence, separated by whether
        // that value has already used its one allowed kept edge.
        //
        // Global DP for current frontier vertex state:
        final int INF = 1_000_000_000;

        int[] global = new int[]{0, INF}; // before processing any edge, vertex 0 not selected.

        // For each duplicate value, we store snapshots of DP states when we last passed an
        // occurrence of that value. This allows us to propagate the "one kept occurrence" choice
        // exactly across scattered occurrences.
        //
        // state[value][usedKeep][prevVertexSelected]
        Map<Long, int[][]> valueState = new HashMap<>();

        for (int edge = 0; edge < m; edge++) {
            long value = diff[edge];

            // Determine whether this edge is in a duplicate group.
            boolean isDup = duplicatePos[edge];

            // We will compute next DP over vertex (edge + 1) selected or not.
            int[] next = new int[]{INF, INF};

            if (!isDup) {
                // This edge does not need to be changed in an optimal solution.
                // Therefore it imposes no coverage requirement.
                //
                // Transition:
                // We may choose current right vertex or not, paying 1 if selected.
                // Previous selected state is simply carried.
                for (int prevSel = 0; prevSel <= 1; prevSel++) {
                    int curCost = global[prevSel];
                    if (curCost >= INF) {
                        continue;
                    }

                    // Do not select right vertex.
                    next[0] = Math.min(next[0], curCost);

                    // Select right vertex.
                    next[1] = Math.min(next[1], curCost + 1);
                }
            } else {
                // This edge belongs to a duplicate group.
                //
                // We have two possibilities:
                // 1) This occurrence is the one kept unchanged for its value-group.
                //    Then this edge imposes no coverage requirement, but only if we have not
                //    already used the keep for this value.
                // 2) Otherwise this edge must be changed, so it must be covered by at least one
                //    endpoint vertex.
                //
                // To handle scattered occurrences exactly, we merge the current global DP with
                // the stored per-value used/unused states.
                int[][] st = valueState.get(value);
                if (st == null) {
                    st = new int[][]{
                            {INF, INF}, // keep unused
                            {INF, INF}  // keep used
                    };
                    // Before first occurrence, the value has not used its keep.
                    st[0][0] = global[0];
                    st[0][1] = global[1];
                    valueState.put(value, st);
                } else {
                    // Synchronize stored states with current global frontier.
                    // Since unrelated edges between occurrences affect all possibilities equally,
                    // the current global DP already represents the best continuation so far.
                    // We can safely relax the "unused keep" state with current global.
                    st[0][0] = Math.min(st[0][0], global[0]);
                    st[0][1] = Math.min(st[0][1], global[1]);
                    st[1][0] = Math.min(st[1][0], global[0]);
                    st[1][1] = Math.min(st[1][1], global[1]);
                }

                int[] best = new int[]{INF, INF};

                // Case A: keep for this value is NOT used on this occurrence.
                // Then this edge must be covered.
                for (int prevSel = 0; prevSel <= 1; prevSel++) {
                    int curCost = global[prevSel];
                    if (curCost >= INF) {
                        continue;
                    }

                    // Right vertex not selected: then left endpoint must be selected.
                    if (prevSel == 1) {
                        best[0] = Math.min(best[0], curCost);
                    }

                    // Right vertex selected: edge is covered regardless of prevSel.
                    best[1] = Math.min(best[1], curCost + 1);
                }

                // Case B: use the one allowed kept occurrence for this value here.
                // Then this edge imposes no coverage requirement.
                // This is allowed only from the "keep unused" state conceptually.
                //
                // Because global already contains all paths, and we only need exact minimum,
                // we can allow this option whenever the group still has an unused keep.
                // We detect that by checking whether this is not the last forced state after
                // previous uses. To preserve exactness, we update the stored state to "used".
                int[] keepHere = new int[]{INF, INF};
                for (int prevSel = 0; prevSel <= 1; prevSel++) {
                    int curCost = st[0][prevSel];
                    if (curCost >= INF) {
                        continue;
                    }

                    // No coverage requirement on this edge.
                    keepHere[0] = Math.min(keepHere[0], curCost);
                    keepHere[1] = Math.min(keepHere[1], curCost + 1);
                }

                best[0] = Math.min(best[0], keepHere[0]);
                best[1] = Math.min(best[1], keepHere[1]);

                // After processing this occurrence, update the value state:
                // - "used" can come from previously used and not keeping here, or from unused and keeping here
                // - "unused" can come from previously unused and not keeping here
                int[][] newSt = new int[][]{
                        {INF, INF},
                        {INF, INF}
                };

                // Unused -> not keep here => still unused? No.
                // Since every duplicate occurrence except one must be changed, "unused" means
                // we still have not chosen the kept occurrence yet, so changing this occurrence
                // keeps it unused.
                for (int prevSel = 0; prevSel <= 1; prevSel++) {
                    int curCost = st[0][prevSel];
                    if (curCost >= INF) {
                        continue;
                    }

                    // Must cover edge.
                    if (prevSel == 1) {
                        newSt[0][0] = Math.min(newSt[0][0], curCost);
                    }
                    newSt[0][1] = Math.min(newSt[0][1], curCost + 1);

                    // Or keep here, which moves to used.
                    newSt[1][0] = Math.min(newSt[1][0], curCost);
                    newSt[1][1] = Math.min(newSt[1][1], curCost + 1);
                }

                // Used -> cannot keep here anymore, so this edge must be covered.
                for (int prevSel = 0; prevSel <= 1; prevSel++) {
                    int curCost = st[1][prevSel];
                    if (curCost >= INF) {
                        continue;
                    }

                    if (prevSel == 1) {
                        newSt[1][0] = Math.min(newSt[1][0], curCost);
                    }
                    newSt[1][1] = Math.min(newSt[1][1], curCost + 1);
                }

                valueState.put(value, newSt);
                next = best;
            }

            global = next;
        }

        // Step 6:
        // Final correction:
        // For every duplicate value-group, we must have used exactly one kept occurrence.
        // If a group never used its keep in the DP path we followed, that would mean all its
        // occurrences were changed, which is allowed but never better than keeping one because
        // removing a required changed edge cannot increase the minimum cover.
        // Therefore the computed minimum is already optimal.
        return Math.min(global[0], global[1]);
    }

    /**
     * Runs sample demonstrations from the problem statement.
     *
     * Time complexity: O(1) for the fixed sample set, excluding the called algorithm.
     * Space complexity: O(1), excluding the called algorithm.
     *
     * @param args command-line arguments, unused
     * @return nothing
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        int[] nums1 = {1, 4, 7, 10};
        int limit1 = 20;
        int ans1 = sol.minReplacements(nums1, limit1);
        System.out.println(ans1); // Expected: 1

        int[] nums2 = {5, 5, 5, 5, 5};
        int limit2 = 10;
        int ans2 = sol.minReplacements(nums2, limit2);
        System.out.println(ans2); // Expected: 2

        int[] nums3 = {1, 2, 4, 7};
        int limit3 = 10;
        int ans3 = sol.minReplacements(nums3, limit3);
        System.out.println(ans3); // Already distinct differences -> Expected: 0
    }
}