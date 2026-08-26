/*
Title: Minimum Review Team Size for Component Approval

Problem Description:
You are planning code reviews for a large release made up of n software components.
Component i requires reviews[i] independent review comments before it can be approved.

You have a pool of engineers, and every engineer can review a contiguous block of
components during the release window. Because of domain knowledge limits, a single
engineer can cover at most span consecutive components, but while assigned to that
block, the engineer contributes exactly 1 review to every component in the block.

You may choose any number of engineers and assign each engineer to any contiguous
block of length at most span. Multiple engineers may review overlapping blocks.
A component is approved if the total number of engineers whose assigned blocks include
that component is at least reviews[i].

Return the minimum number of engineers needed so that every component is approved.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= span <= n
- 0 <= reviews[i] <= 10^9
- It is always possible to approve all components with sufficiently many engineers.

Key Idea:
This is a classic "minimum number of range additions to satisfy lower bounds" problem.
The answer is monotonic:
- If x engineers are enough, then any number > x is also enough.
So we can binary search on the answer.

For a fixed candidate number of engineers x, we need to check whether it is possible
to satisfy all review requirements using at most x engineers.

Greedy feasibility check:
- Process components from left to right.
- Maintain how many currently active engineer assignments cover the current component.
- If current coverage is below reviews[i], we must add exactly the missing amount here.
- To maximize future benefit, every newly added engineer should start at i and extend
  as far right as possible, i.e. cover [i, min(n - 1, i + span - 1)].
- This greedy choice is optimal because it helps the current component and also helps
  the largest possible number of future components.

We implement active coverage efficiently using a difference-array style sweep:
- active = number of engineers currently covering index i
- endEvents[j] = how many engineer assignments stop contributing when we move to index j
- When we add k engineers starting at i, active += k immediately
- Their effect ends after index end = i + span - 1, so we schedule endEvents[end + 1] += k

This yields an O(n log U) solution, where U can be taken as sum(reviews).
*/

import java.util.*;

public class Solution {

    /**
     * Computes the minimum number of engineers needed so that every component
     * receives at least the required number of reviews.
     *
     * The method uses binary search on the answer:
     * - Lower bound = 0 engineers
     * - Upper bound = sum(reviews), because in the worst case we can assign
     *   reviews[i] engineers to a block containing only component i
     *   (a block of length 1 is allowed since length is at most span).
     *
     * For each candidate team size, we run a greedy feasibility check.
     *
     * @param reviews the required minimum review count for each component
     * @param span the maximum number of consecutive components one engineer can cover
     * @return the minimum number of engineers required
     * Time complexity: O(n log S), where S = sum(reviews)
     * Space complexity: O(n)
     */
    public long minimumEngineers(int[] reviews, int span) {
        long low = 0L;
        long high = 0L;

        for (int need : reviews) {
            high += need;
        }

        while (low < high) {
            long mid = low + (high - low) / 2;

            if (canApproveWithAtMost(reviews, span, mid)) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }

        return low;
    }

    /**
     * Checks whether it is possible to satisfy all review requirements using
     * at most maxEngineers engineers.
     *
     * Greedy strategy:
     * - Sweep from left to right.
     * - Maintain current active coverage at each component.
     * - If current coverage is less than reviews[i], we are forced to add
     *   exactly the missing number of engineers whose blocks start at i.
     * - To maximize usefulness, each such engineer covers the longest possible
     *   block: [i, min(n - 1, i + span - 1)].
     *
     * Why this is correct:
     * - When we are at component i, no future assignment that starts after i
     *   can help component i.
     * - Therefore, if coverage is short by delta, we must add at least delta
     *   engineers now.
     * - Extending them as far right as possible can only help future components,
     *   never hurt, so this is optimal for feasibility.
     *
     * Implementation details:
     * - active stores how many currently chosen engineers cover the current index.
     * - endEvents[pos] stores how many active engineers stop contributing when
     *   we arrive at index pos.
     * - Before processing i, subtract endEvents[i] from active.
     *
     * @param reviews the required minimum review count for each component
     * @param span the maximum number of consecutive components one engineer can cover
     * @param maxEngineers the candidate upper bound on how many engineers may be used
     * @return true if all components can be approved using at most maxEngineers engineers; false otherwise
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public boolean canApproveWithAtMost(int[] reviews, int span, long maxEngineers) {
        int n = reviews.length;

        /*
         * We use n + 1 so that if an assignment ends at the last component,
         * we can safely place its "removal event" at index n.
         */
        long[] endEvents = new long[n + 1];

        long active = 0L;
        long used = 0L;

        for (int i = 0; i < n; i++) {
            /*
             * Step 1:
             * Remove the effect of any engineer assignments whose coverage ended
             * before the current component.
             *
             * If endEvents[i] = k, that means k assignments were covering up to i - 1
             * and should no longer count for component i.
             */
            active -= endEvents[i];

            /*
             * Step 2:
             * Check whether current coverage is enough for component i.
             */
            long need = reviews[i];

            if (active < need) {
                /*
                 * We are short by "add" reviews at component i.
                 * Since no future-starting interval can cover i, these engineers
                 * must be added right now.
                 */
                long add = need - active;
                used += add;

                /*
                 * If we already exceed the candidate limit, feasibility fails early.
                 */
                if (used > maxEngineers) {
                    return false;
                }

                /*
                 * These newly added engineers start at i and cover as far right
                 * as possible, which is the best greedy choice.
                 */
                active += add;

                int endExclusive = Math.min(n, i + span);

                /*
                 * Their contribution should stop when we reach endExclusive.
                 * Until then, they remain part of "active".
                 */
                endEvents[endExclusive] += add;
            }
        }

        return true;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Example 1:
     * reviews = [1,2,2,1], span = 2
     * Expected answer = 3
     *
     * Example 2:
     * reviews = [3,0,1,4,2], span = 3
     * Expected answer = 6
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log S) across the demonstrated examples
     * Space complexity: O(n)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] reviews1 = {1, 2, 2, 1};
        int span1 = 2;
        long answer1 = solution.minimumEngineers(reviews1, span1);
        System.out.println("Example 1 result: " + answer1);
        System.out.println("Expected: 3");

        int[] reviews2 = {3, 0, 1, 4, 2};
        int span2 = 3;
        long answer2 = solution.minimumEngineers(reviews2, span2);
        System.out.println("Example 2 result: " + answer2);
        System.out.println("Expected: 6");

        /*
         * Additional quick sanity checks:
         *
         * 1) If all requirements are zero, answer should be zero.
         */
        int[] reviews3 = {0, 0, 0, 0};
        int span3 = 2;
        long answer3 = solution.minimumEngineers(reviews3, span3);
        System.out.println("All-zero requirement result: " + answer3);
        System.out.println("Expected: 0");

        /*
         * 2) If span = 1, each engineer can only cover one component.
         *    Then the answer is simply sum(reviews).
         */
        int[] reviews4 = {2, 1, 3};
        int span4 = 1;
        long answer4 = solution.minimumEngineers(reviews4, span4);
        System.out.println("Span-1 result: " + answer4);
        System.out.println("Expected: 6");
    }
}