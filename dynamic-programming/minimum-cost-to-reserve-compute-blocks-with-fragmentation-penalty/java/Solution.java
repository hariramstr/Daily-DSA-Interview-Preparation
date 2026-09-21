import java.util.*;

/*
Problem Title: Minimum Cost to Reserve Compute Blocks with Fragmentation Penalty

Problem Description:
A cloud platform sells compute capacity for the next n hours. For hour i, you must reserve exactly demand[i] units of compute.
Instead of buying capacity hour by hour, you may purchase reservation blocks.

A block is defined by a contiguous interval of hours [l, r] and a fixed capacity c, meaning that for every hour from l to r
you receive exactly c units from that block. Multiple blocks may overlap, and the total reserved capacity at each hour must
equal the required demand exactly.

The cost of one block [l, r] with capacity c is:
setupCost + c * (r - l + 1) + fragmentationPenalty * max(0, c - min(demand[l..r]))

The first term is a fixed fee for creating a block, the second term is the usage cost, and the third term penalizes
over-provisioning relative to the minimum demand inside the covered interval. Intuitively, a block that spans a low-demand
hour cannot cheaply carry a high capacity.

Your task is to compute the minimum total cost needed to satisfy the full demand array exactly.

Constraints:
- 1 <= n <= 200
- 0 <= demand[i] <= 10^6
- 1 <= setupCost <= 10^6
- 0 <= fragmentationPenalty <= 10^6
- The answer fits in a 64-bit signed integer.

Key observation:
Any feasible solution can be viewed as repeatedly choosing one "bottom" block on an interval [l, r] with capacity equal to
the minimum remaining demand on that interval, then recursively solving the positive residual sub-intervals created after
subtracting that minimum. This is the same structural idea used in interval DP / histogram-style decompositions.

For a subproblem on interval [l, r] with a baseline height "base", we need to build exactly demand[i] - base additional
capacity at each hour i in [l, r]. Let mn = min(demand[l..r]). Then one natural option is:
- buy one block [l, r] of capacity (mn - base), which has no fragmentation penalty because mn is the interval minimum,
- then recursively solve every maximal sub-interval where demand[i] > mn.

We may also split [l, r] into smaller independent intervals without placing a spanning block. Therefore:
dp(l, r, base) = min(
    direct hourly blocks on each hour,
    min over splits,
    spanning block up to mn + recursive residuals
)

A crucial simplification:
The only base values that ever matter are 0 or some demand value, and because n <= 200, memoized interval DP over
(l, r, baseIndex) is practical. We compress all distinct demand values plus 0.

Additionally, for a single-hour interval [i, i], the optimal way to provide x units above base is one block of capacity x
if x > 0, costing setupCost + x. This is naturally handled by the general recurrence.
*/

public class Solution {

    private int[] demand;
    private long setupCost;
    private long fragmentationPenalty;

    private long[][] minDemand;
    private long[][][] memo;
    private boolean[][][] seen;

    private long[] values;
    private Map<Long, Integer> valueToIndex;

    /**
     * Computes the minimum total cost to satisfy the exact demand profile.
     *
     * The algorithm uses memoized interval dynamic programming.
     * A state solve(l, r, baseIdx) means:
     * "What is the minimum extra cost needed to raise every hour in [l, r]
     * from already-covered baseline values[baseIdx] up to demand[i]?"
     *
     * Transition options:
     * 1) Split the interval into two independent parts.
     * 2) Add one spanning block from [l, r] that raises the whole interval from base to the interval minimum,
     *    then recursively solve the higher residual segments.
     *
     * Because the spanning block only raises to the interval minimum, its fragmentation penalty is always zero.
     * This is enough for optimality: any block with higher capacity can be decomposed into a minimum-level block
     * plus additional blocks on sub-intervals, never increasing cost.
     *
     * @param demand the exact required capacity for each hour
     * @param setupCost fixed cost for creating one block
     * @param fragmentationPenalty penalty coefficient for over-provisioning above interval minimum
     * @return the minimum total cost as a 64-bit signed integer
     * Time complexity: O(n^4 * m) in the worst case with memoization, where n <= 200 and m <= n + 1 distinct base values.
     * In practice, the number of reachable states is much smaller; with n = 200 this is acceptable.
     * Space complexity: O(n^2 * m) for memoization plus O(n^2) for interval minima.
     */
    public long minimumCost(int[] demand, int setupCost, int fragmentationPenalty) {
        this.demand = demand;
        this.setupCost = setupCost;
        this.fragmentationPenalty = fragmentationPenalty;

        int n = demand.length;

        buildDistinctValues();
        buildMinDemandTable();

        int m = values.length;
        memo = new long[n][n][m];
        seen = new boolean[n][n][m];

        int zeroIdx = valueToIndex.get(0L);
        return solve(0, n - 1, zeroIdx);
    }

    /**
     * Recursive memoized interval DP.
     *
     * State meaning:
     * We already have "base" units reserved on every hour in [l, r].
     * We must add exactly demand[i] - base more units at each hour i.
     *
     * Important invariant:
     * base <= min(demand[l..r]), otherwise the state would be invalid/unreachable.
     *
     * Transition details:
     * - If base equals demand everywhere in [l, r], cost is 0.
     * - We may split at any midpoint k:
     *     solve(l, k, base) + solve(k+1, r, base)
     * - Or we may place one block [l, r] of capacity add = mn - base, where mn is the interval minimum.
     *   This block cost is:
     *     setupCost + add * len + fragmentationPenalty * max(0, add - (mn - base?))
     *   But since the block's absolute capacity increment is exactly mn - base and it is placed on top of baseline,
     *   in original absolute terms this corresponds to raising all hours to mn. Relative to the interval minimum mn,
     *   there is no over-provisioning, so fragmentation penalty is 0.
     *   After that, only positions with demand > mn still need extra capacity, forming independent sub-intervals.
     *
     * @param l left index of interval
     * @param r right index of interval
     * @param baseIdx index of the current baseline value in the compressed value array
     * @return minimum extra cost for this state
     * Time complexity: Each state tries O(n) splits and scans the interval, so O(n^2) per state in the worst case.
     * Space complexity: O(1) auxiliary beyond recursion stack and memo storage.
     */
    public long solve(int l, int r, int baseIdx) {
        if (l > r) {
            return 0L;
        }

        if (seen[l][r][baseIdx]) {
            return memo[l][r][baseIdx];
        }
        seen[l][r][baseIdx] = true;

        long base = values[baseIdx];
        long mn = minDemand[l][r];

        // If baseline already exceeds the minimum demand in this interval,
        // this state is invalid and should never be chosen.
        if (base > mn) {
            memo[l][r][baseIdx] = Long.MAX_VALUE / 4;
            return memo[l][r][baseIdx];
        }

        // If the interval is a single hour, the remaining demand above base is easy to satisfy:
        // either nothing is needed, or one block of exactly the remaining capacity is optimal.
        if (l == r) {
            long need = demand[l] - base;
            if (need == 0) {
                memo[l][r][baseIdx] = 0L;
            } else {
                // Single-hour block:
                // cost = setupCost + need * 1 + penalty * max(0, need - need) = setupCost + need
                memo[l][r][baseIdx] = setupCost + need;
            }
            return memo[l][r][baseIdx];
        }

        long best = Long.MAX_VALUE / 4;

        // Option 1: split the interval into two independent parts.
        // This is always valid because blocks are contiguous, so any solution can be partitioned at a cut
        // if no spanning block across the cut is used. The DP takes the minimum over all such possibilities.
        for (int mid = l; mid < r; mid++) {
            long left = solve(l, mid, baseIdx);
            long right = solve(mid + 1, r, baseIdx);
            long candidate = left + right;
            if (candidate < best) {
                best = candidate;
            }
        }

        // Option 2: place one spanning block that raises the whole interval from "base" up to the interval minimum "mn".
        // This is the canonical "bottom layer" of an optimal decomposition.
        if (mn > base) {
            long add = mn - base;
            long len = r - l + 1L;

            // Because this block's absolute capacity layer ends exactly at the interval minimum,
            // there is no fragmentation penalty.
            long costOfBottomBlock = setupCost + add * len;

            long total = costOfBottomBlock;

            // After removing the bottom layer up to mn, only positions with demand > mn remain unsatisfied.
            // Those positions form maximal contiguous segments, each solved independently with new baseline = mn.
            int newBaseIdx = valueToIndex.get(mn);
            int i = l;
            while (i <= r) {
                if (demand[i] == mn) {
                    i++;
                    continue;
                }
                int start = i;
                while (i <= r && demand[i] > mn) {
                    i++;
                }
                int end = i - 1;
                total += solve(start, end, newBaseIdx);
            }

            if (total < best) {
                best = total;
            }
        } else {
            // If mn == base, we cannot add a positive spanning block.
            // In that case, the interval must be handled entirely by the higher-demand subsegments.
            long total = 0L;
            int i = l;
            while (i <= r) {
                if (demand[i] == mn) {
                    i++;
                    continue;
                }
                int start = i;
                while (i <= r && demand[i] > mn) {
                    i++;
                }
                int end = i - 1;
                total += solve(start, end, baseIdx);
            }
            if (total < best) {
                best = total;
            }
        }

        memo[l][r][baseIdx] = best;
        return best;
    }

    /**
     * Builds the table minDemand[l][r] = minimum demand on interval [l, r].
     *
     * This allows O(1) retrieval of interval minima during DP transitions.
     *
     * @return nothing
     * Time complexity: O(n^2)
     * Space complexity: O(n^2)
     */
    public void buildMinDemandTable() {
        int n = demand.length;
        minDemand = new long[n][n];

        for (int l = 0; l < n; l++) {
            long currentMin = Long.MAX_VALUE;
            for (int r = l; r < n; r++) {
                currentMin = Math.min(currentMin, demand[r]);
                minDemand[l][r] = currentMin;
            }
        }
    }

    /**
     * Builds the compressed set of baseline values used by the DP.
     *
     * Only 0 and values appearing in demand are needed as possible baselines.
     * This keeps the third DP dimension small.
     *
     * @return nothing
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public void buildDistinctValues() {
        TreeSet<Long> set = new TreeSet<>();
        set.add(0L);
        for (int x : demand) {
            set.add((long) x);
        }

        values = new long[set.size()];
        valueToIndex = new HashMap<>();

        int idx = 0;
        for (long v : set) {
            values[idx] = v;
            valueToIndex.put(v, idx);
            idx++;
        }
    }

    /**
     * Convenience wrapper matching a common interview-style signature.
     *
     * @param demand the exact required capacity for each hour
     * @param setupCost fixed cost for creating one block
     * @param fragmentationPenalty penalty coefficient for over-provisioning above interval minimum
     * @return minimum total cost
     * Time complexity: same as minimumCost
     * Space complexity: same as minimumCost
     */
    public long minCostToReserveComputeBlocks(int[] demand, int setupCost, int fragmentationPenalty) {
        return minimumCost(demand, setupCost, fragmentationPenalty);
    }

    /**
     * Demonstrates the solution on the sample-style inputs and a few extra sanity checks.
     *
     * Note about Example 1:
     * The statement text contains an inconsistency: it says "Output: 15" but then explains a valid plan of cost 13
     * and concludes that 13 is minimal. The correct answer is 13, and this program prints 13.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(1) outside the invoked solver calls
     * Space complexity: O(1) outside the invoked solver calls
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] demand1 = {3, 1, 3};
        int setupCost1 = 2;
        int fragmentationPenalty1 = 4;
        long answer1 = solution.minimumCost(demand1, setupCost1, fragmentationPenalty1);
        System.out.println(answer1); // Expected correct value: 13

        int[] demand2 = {2, 2, 2, 2};
        int setupCost2 = 5;
        int fragmentationPenalty2 = 3;
        long answer2 = solution.minimumCost(demand2, setupCost2, fragmentationPenalty2);
        System.out.println(answer2); // Expected: 13

        int[] demand3 = {0, 0, 0};
        int setupCost3 = 7;
        int fragmentationPenalty3 = 10;
        long answer3 = solution.minimumCost(demand3, setupCost3, fragmentationPenalty3);
        System.out.println(answer3); // Expected: 0

        int[] demand4 = {5};
        int setupCost4 = 3;
        int fragmentationPenalty4 = 100;
        long answer4 = solution.minimumCost(demand4, setupCost4, fragmentationPenalty4);
        System.out.println(answer4); // Expected: 8
    }
}