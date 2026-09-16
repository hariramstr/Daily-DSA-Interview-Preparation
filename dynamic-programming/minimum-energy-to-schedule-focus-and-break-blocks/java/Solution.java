import java.util.*;

/*
Problem Title: Minimum Energy to Schedule Focus and Break Blocks

Problem Description:
A productivity app plans a day as a sequence of tasks. For each task i, you are given two energy costs:
focusCost[i] and breakCost[i]. You must assign every task to exactly one mode: Focus or Break.
The total energy spent is the sum of the chosen costs, but there is an additional rule:
no more than k consecutive tasks may be assigned to Focus mode, because long uninterrupted
focus sessions are not allowed.

Your goal is to compute the minimum total energy needed to schedule all tasks while respecting
the consecutive-focus limit.

Formally, given two integer arrays focusCost and breakCost of length n and an integer k,
choose for each index i either focusCost[i] or breakCost[i]. If you choose Focus for task i,
it contributes focusCost[i] to the total. If you choose Break for task i, it contributes
breakCost[i] to the total. In the final assignment, every maximal consecutive run of Focus
tasks must have length at most k.

Return the minimum possible total energy.

Constraints:
- 1 <= n <= 100000
- 1 <= k <= n
- focusCost.length == breakCost.length == n
- 1 <= focusCost[i], breakCost[i] <= 1000000000

Example 1:
Input: focusCost = [3, 2, 5, 1], breakCost = [4, 6, 1, 7], k = 2
Output: 7
Explanation: Choose Focus, Focus, Break, Focus.
Total energy = 3 + 2 + 1 + 1 = 7.
This is valid because the longest consecutive Focus run has length 2.

Example 2:
Input: focusCost = [1, 1, 1, 1], breakCost = [10, 10, 10, 10], k = 2
Output: 13
Explanation: Choosing all tasks as Focus would violate the rule because it creates a run of length 4.
At least one Break is required. One optimal assignment is Focus, Focus, Break, Focus with total
energy 1 + 1 + 10 + 1 = 13. Another is Focus, Break, Focus, Focus with the same total.
So the correct minimum for this input is 13.

Efficient Idea:
Let dp[s] mean:
- after processing some prefix of tasks,
- the minimum total energy,
- where the current suffix ends with exactly s consecutive Focus tasks.

Then:
- If current task is assigned Break, the streak resets to 0.
- If current task is assigned Focus, the previous streak s-1 becomes s.

A direct O(n * k) DP is too slow when both n and k are large.

Optimization:
At each step, the Break transition needs the minimum over all previous dp states.
So we maintain:
- dp[0] = best cost ending with Break
- dp[1..k] = best cost ending with a Focus streak of that length
- global minimum of previous dp values

Each new row can be built in O(k), but that is still too slow in worst case.

A better reformulation:
Let prefixBreakMin[i] be the minimum cost to schedule tasks[0..i] with task i assigned Break.
Then any valid schedule is composed of blocks:
- a Break at position i, or
- a Focus block of length len (1 <= len <= k) ending at i, preceded by either start of array or a Break.

This leads to:
dp[i] = minimum cost for tasks[0..i]
dp[i] = min(
    dp[i-1] + breakCost[i],                                  // choose Break at i
    min over len in [1..k] of (cost before block + sumFocus block)
)

For a Focus block [j..i]:
- length = i - j + 1 <= k
- cost = (j == 0 ? 0 : dp[j-1 with task j-1 forced Break]) + sumFocus(j..i)

To express this cleanly, define:
breakDp[i] = minimum cost for tasks[0..i] with task i = Break
focusBlock ending at i and starting at j requires previous position j-1 to be Break (or j=0).

Then:
breakDp[i] = minAll[i-1] + breakCost[i]
focusEnd[i] = min over j in [max(0, i-k+1)..i] of (base(j) + sumFocus(j..i))
where base(j) = 0 if j==0, else breakDp[j-1]

Using prefix sums:
base(j) + sumFocus(j..i)
= base(j) - prefixFocus[j] + prefixFocus[i+1]

So for each i, we need the minimum value of:
base(j) - prefixFocus[j]
over a sliding window of valid starts j.

This can be maintained with a monotonic deque in O(n).

Final answer for each i:
minAll[i] = min(breakDp[i], focusEnd[i])

This yields O(n) time and O(n) space, suitable for n up to 100000.
*/
public class Solution {

    /**
     * Computes the minimum total energy needed to assign every task to either Focus or Break,
     * while ensuring that no run of consecutive Focus tasks has length greater than k.
     *
     * Core idea:
     * We process tasks from left to right and use dynamic programming plus a monotonic deque.
     *
     * Definitions:
     * - prefixFocus[t] = sum of focusCost[0..t-1]
     * - breakDp[i] = minimum total energy for tasks 0..i, with task i assigned Break
     * - minDp[i] = minimum total energy for tasks 0..i, regardless of how task i is assigned
     *
     * For a Focus block ending at i and starting at j:
     * - its length is i - j + 1, which must be <= k
     * - the cost of the block is prefixFocus[i + 1] - prefixFocus[j]
     * - if j > 0, then task j - 1 must be Break, so the previous cost is breakDp[j - 1]
     * - if j == 0, previous cost is 0
     *
     * Therefore:
     * focusEnd(i, j) = base(j) + prefixFocus[i + 1] - prefixFocus[j]
     * where:
     * base(j) = 0 if j == 0, else breakDp[j - 1]
     *
     * Rearranging:
     * focusEnd(i, j) = prefixFocus[i + 1] + (base(j) - prefixFocus[j])
     *
     * So for each i, we need the minimum value of:
     * base(j) - prefixFocus[j]
     * over all valid starts j in [max(0, i - k + 1), i].
     *
     * We maintain these candidate values in a monotonic deque so each index enters and leaves
     * the deque at most once, giving linear time overall.
     *
     * @param focusCost the energy cost of assigning each task to Focus
     * @param breakCost the energy cost of assigning each task to Break
     * @param k the maximum allowed number of consecutive Focus tasks
     * @return the minimum possible total energy satisfying the consecutive-Focus constraint
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long minimumEnergy(int[] focusCost, int[] breakCost, int k) {
        validateInput(focusCost, breakCost, k);

        int n = focusCost.length;

        // prefixFocus[i] stores the sum of focusCost from index 0 to index i - 1.
        // So:
        // prefixFocus[0] = 0
        // prefixFocus[1] = focusCost[0]
        // prefixFocus[2] = focusCost[0] + focusCost[1]
        // ...
        //
        // This allows us to compute the sum of any Focus block [l..r] in O(1):
        // sumFocus(l..r) = prefixFocus[r + 1] - prefixFocus[l]
        long[] prefixFocus = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefixFocus[i + 1] = prefixFocus[i] + focusCost[i];
        }

        // breakDp[i] = minimum total cost for tasks 0..i if task i is assigned Break.
        long[] breakDp = new long[n];

        // minDp[i] = minimum total cost for tasks 0..i regardless of whether task i is Focus or Break.
        long[] minDp = new long[n];

        // We use a deque of candidate starting positions j for Focus blocks.
        //
        // For each start position j, define:
        // candidateValue(j) = base(j) - prefixFocus[j]
        // where:
        //   base(j) = 0 if j == 0
        //   base(j) = breakDp[j - 1] otherwise
        //
        // Then the best Focus block ending at i is:
        // prefixFocus[i + 1] + minimum candidateValue(j) over valid j
        //
        // The deque stores start indices j in increasing order,
        // and their candidate values are kept in nondecreasing order.
        Deque<Integer> deque = new ArrayDeque<>();

        // Initially, before processing any task, start position j = 0 is a valid candidate.
        // It means a Focus block may start at the beginning of the array.
        deque.addLast(0);

        for (int i = 0; i < n; i++) {
            // Step 1:
            // Remove start positions that would create a Focus block longer than k.
            //
            // If a Focus block ends at i and starts at j, then:
            // length = i - j + 1 <= k
            // which means:
            // j >= i - k + 1
            //
            // So any j < i - k + 1 is no longer valid and must be removed from the front.
            int minValidStart = i - k + 1;
            while (!deque.isEmpty() && deque.peekFirst() < minValidStart) {
                deque.pollFirst();
            }

            // Step 2:
            // Compute the best cost if task i is assigned Break.
            //
            // If i == 0:
            //   breakDp[0] = breakCost[0]
            // because there are no previous tasks.
            //
            // Otherwise:
            //   breakDp[i] = minDp[i - 1] + breakCost[i]
            // because assigning Break at i resets the Focus streak,
            // and the previous tasks can end in any valid way.
            if (i == 0) {
                breakDp[i] = breakCost[i];
            } else {
                breakDp[i] = minDp[i - 1] + breakCost[i];
            }

            // Step 3:
            // Compute the best cost if task i is the end of a Focus block.
            //
            // The deque front always stores the valid start j with the smallest candidate value.
            // So:
            // bestFocusEnd = prefixFocus[i + 1] + candidateValue(best j)
            long bestCandidate = candidateValue(deque.peekFirst(), prefixFocus, breakDp);
            long focusEnd = prefixFocus[i + 1] + bestCandidate;

            // Step 4:
            // The overall best cost for tasks 0..i is the better of:
            // - ending with Break at i
            // - ending with a valid Focus block at i
            minDp[i] = Math.min(breakDp[i], focusEnd);

            // Step 5:
            // Prepare start position j = i + 1 for future Focus blocks.
            //
            // Why j = i + 1?
            // Because a future Focus block might start immediately after task i.
            //
            // If a Focus block starts at j = i + 1, then task i must be Break.
            // So:
            // base(i + 1) = breakDp[i]
            //
            // Its candidate value is:
            // breakDp[i] - prefixFocus[i + 1]
            int nextStart = i + 1;

            // We only need to add nextStart if it is within array bounds as a possible start.
            // Since starts can range from 0 to n - 1 for actual blocks, adding n is unnecessary.
            if (nextStart < n) {
                long nextValue = candidateValue(nextStart, prefixFocus, breakDp);

                // Maintain monotonicity:
                // Remove from the back while the new candidate is smaller or equal,
                // because the new one is at least as good and will remain valid longer.
                while (!deque.isEmpty()
                        && candidateValue(deque.peekLast(), prefixFocus, breakDp) >= nextValue) {
                    deque.pollLast();
                }

                deque.addLast(nextStart);
            }
        }

        return minDp[n - 1];
    }

    /**
     * Computes the candidate value for a Focus block start position j.
     *
     * candidateValue(j) = base(j) - prefixFocus[j]
     * where:
     * - base(j) = 0 if j == 0
     * - base(j) = breakDp[j - 1] otherwise
     *
     * This value is used so that the cost of a Focus block [j..i] becomes:
     * prefixFocus[i + 1] + candidateValue(j)
     *
     * @param start the start index j of a Focus block
     * @param prefixFocus prefix sums of focus costs
     * @param breakDp DP array where breakDp[x] is the best cost ending with Break at x
     * @return the transformed candidate value for this start position
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long candidateValue(int start, long[] prefixFocus, long[] breakDp) {
        long base = (start == 0) ? 0L : breakDp[start - 1];
        return base - prefixFocus[start];
    }

    /**
     * Validates the input according to the problem requirements.
     *
     * @param focusCost the Focus costs array
     * @param breakCost the Break costs array
     * @param k the maximum allowed consecutive Focus length
     * @return nothing
     * Time complexity: O(1)
     * Space complexity: O(1)
     * @throws IllegalArgumentException if the input is invalid
     */
    public void validateInput(int[] focusCost, int[] breakCost, int k) {
        if (focusCost == null || breakCost == null) {
            throw new IllegalArgumentException("Input arrays must not be null.");
        }
        if (focusCost.length != breakCost.length) {
            throw new IllegalArgumentException("focusCost and breakCost must have the same length.");
        }
        if (focusCost.length == 0) {
            throw new IllegalArgumentException("Input arrays must have at least one element.");
        }
        if (k < 1 || k > focusCost.length) {
            throw new IllegalArgumentException("k must satisfy 1 <= k <= n.");
        }
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total n across demonstrations)
     * Space complexity: O(total n across demonstrations)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] focusCost1 = {3, 2, 5, 1};
        int[] breakCost1 = {4, 6, 1, 7};
        int k1 = 2;
        long result1 = solution.minimumEnergy(focusCost1, breakCost1, k1);
        System.out.println("Example 1 Result: " + result1); // Expected: 7

        int[] focusCost2 = {1, 1, 1, 1};
        int[] breakCost2 = {10, 10, 10, 10};
        int k2 = 2;
        long result2 = solution.minimumEnergy(focusCost2, breakCost2, k2);
        System.out.println("Example 2 Result: " + result2); // Expected: 13

        int[] focusCost3 = {5};
        int[] breakCost3 = {2};
        int k3 = 1;
        long result3 = solution.minimumEnergy(focusCost3, breakCost3, k3);
        System.out.println("Single Task Result: " + result3); // Expected: 2

        int[] focusCost4 = {2, 2, 2};
        int[] breakCost4 = {100, 100, 100};
        int k4 = 3;
        long result4 = solution.minimumEnergy(focusCost4, breakCost4, k4);
        System.out.println("All Focus Allowed Result: " + result4); // Expected: 6
    }
}