import java.util.*;

/*
Problem Title: Minimum Review Time for Parallel Code Audits

Problem Description:
A company needs to review a large set of code changes before a release. There are n code changes, and the i-th change requires reviewWork[i] units of work. The company has m senior reviewers. Reviewer j can review at speed speed[j], meaning they can complete speed[j] units of work per hour. A single code change cannot be split across multiple reviewers, but each reviewer may review any number of code changes, one after another.

You may assign the code changes in any order to any reviewers. The total release review time is the maximum total time spent by any single reviewer. Return the minimum possible release review time needed to finish reviewing all code changes.

Formally, if a reviewer is assigned changes with total work W, that reviewer needs ceil(W / speed[j]) hours. You want to partition all code changes among the m reviewers so that the maximum reviewer completion time is minimized.

This is a decision-and-optimization problem: for a candidate time T, determine whether it is possible to assign every code change to some reviewer such that each assigned reviewer finishes within T hours. Then compute the minimum feasible T.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= m <= 20
- 1 <= reviewWork[i] <= 10^9
- 1 <= speed[j] <= 10^9
- m <= n is not guaranteed
- Every code change must be assigned to exactly one reviewer

Important note about the examples:
The second example text in the prompt is internally inconsistent. For reviewWork = [9, 9, 9, 9, 9] and speed = [3, 3, 3],
time 6 is feasible because each reviewer has capacity 18 and we can assign [9,9], [9,9], [9].
Therefore the correct minimum answer for that input is 6, not 9.

We solve the problem correctly according to the formal statement.
*/

public class Solution {

    /**
     * Computes the minimum possible release review time.
     *
     * Core idea:
     * 1. Binary search on the answer T (hours).
     * 2. For a fixed T, reviewer j has capacity speed[j] * T units of work.
     * 3. We must decide whether all indivisible code changes can be assigned to reviewers
     *    so that no reviewer exceeds their capacity.
     *
     * Because m <= 20 is very small while n can be very large, we do not do assignment over tasks directly.
     * Instead, for a candidate T:
     * - Any task larger than the largest reviewer capacity makes T impossible immediately.
     * - Tasks that can fit only into one reviewer are forced onto that reviewer.
     * - The remaining "flexible" tasks are grouped by the first reviewer index that can take them
     *   when capacities are sorted increasingly.
     * - Then we use a subset DP over reviewers to compute the maximum total flexible work that can be packed.
     *
     * This yields an efficient feasibility test suitable for binary search.
     *
     * @param reviewWork array where reviewWork[i] is the work units required by the i-th code change
     * @param speed array where speed[j] is the review speed of the j-th reviewer in units per hour
     * @return the minimum feasible maximum completion time in hours
     * Time complexity: O((n log n + m * 2^m) * log AnswerRange)
     * Space complexity: O(2^m + n)
     */
    public long minimumReviewTime(int[] reviewWork, int[] speed) {
        int n = reviewWork.length;
        int m = speed.length;

        long[] works = new long[n];
        long totalWork = 0L;
        long maxWork = 0L;
        for (int i = 0; i < n; i++) {
            works[i] = reviewWork[i];
            totalWork += works[i];
            maxWork = Math.max(maxWork, works[i]);
        }

        long[] speeds = new long[m];
        long maxSpeed = 0L;
        for (int i = 0; i < m; i++) {
            speeds[i] = speed[i];
            maxSpeed = Math.max(maxSpeed, speeds[i]);
        }

        // Lower bound:
        // - At least enough time for the largest single task on the fastest reviewer.
        // - Also at least ceil(totalWork / sum(speed)) in a divisible-world lower bound.
        long sumSpeed = 0L;
        for (long s : speeds) {
            sumSpeed += s;
        }

        long low = Math.max(ceilDiv(maxWork, maxSpeed), ceilDiv(totalWork, sumSpeed));

        // Upper bound:
        // Put everything on the fastest reviewer.
        long high = ceilDiv(totalWork, maxSpeed);

        while (low < high) {
            long mid = low + ((high - low) >>> 1);
            if (canFinishInTime(works, speeds, mid)) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }

        return low;
    }

    /**
     * Checks whether all code changes can be assigned within time T.
     *
     * Detailed strategy:
     *
     * Step 1: Convert each reviewer into a capacity:
     *         capacity[j] = speed[j] * T
     *
     * Step 2: Sort capacities in nondecreasing order.
     *         This is useful because for any task of size w, the set of reviewers that can take it
     *         becomes a suffix of the sorted capacity array.
     *
     * Step 3: Process every task:
     *         - If w > largest capacity, impossible immediately.
     *         - Find the first reviewer index idx with capacity[idx] >= w.
     *           Then the task can only go to reviewers idx, idx+1, ..., m-1.
     *         - If idx == m-1, the task is forced onto the largest-capacity reviewer.
     *         - More generally, tasks with larger idx are more constrained.
     *
     * Step 4: Convert the problem on flexible tasks into subset DP.
     *         Let group[idx] be the total work of tasks whose first feasible reviewer is idx.
     *         Such tasks must be assigned to some subset of reviewers chosen from idx..m-1.
     *
     *         We process groups from right to left.
     *         DP state:
     *           dp[mask] = maximum total flexible work from already processed groups
     *                      that can be packed into the set of reviewers represented by mask.
     *
     *         Transition for group i:
     *           choose any non-empty subset sub of available reviewers among i..m-1,
     *           as long as total capacity of sub is at least group[i].
     *           Then we can place all work of this group into those reviewers somehow in aggregate.
     *
     *         Why aggregate capacity is enough here:
     *           all tasks in group i individually fit into any reviewer in suffix i..m-1,
     *           because each such reviewer has capacity >= every task in the group.
     *           Therefore, within a chosen subset of that suffix, if total capacity is enough,
     *           the tasks can be greedily packed one by one. Since every item fits into every bin in the subset,
     *           only total capacity matters.
     *
     * Step 5: If all groups can be packed, T is feasible.
     *
     * @param works array of task sizes as long values
     * @param speeds array of reviewer speeds as long values
     * @param time candidate maximum allowed time
     * @return true if assignment is feasible within {@code time}, otherwise false
     * Time complexity: O(n log m + m * 2^m)
     * Space complexity: O(2^m)
     */
    public boolean canFinishInTime(long[] works, long[] speeds, long time) {
        int m = speeds.length;

        long[] capacity = new long[m];
        for (int i = 0; i < m; i++) {
            capacity[i] = speeds[i] * time;
        }
        Arrays.sort(capacity);

        long largestCapacity = capacity[m - 1];

        // group[i] = total work of tasks that can only be assigned to reviewers in suffix [i..m-1]
        // because i is the first reviewer whose capacity is large enough for those tasks.
        long[] group = new long[m];

        // First, classify every task by the first reviewer that can take it.
        for (long w : works) {
            if (w > largestCapacity) {
                return false;
            }
            int idx = lowerBound(capacity, w);
            group[idx] += w;
        }

        // Prefix sums of capacities are useful for quick suffix capacity checks.
        long[] prefixCap = new long[m + 1];
        for (int i = 0; i < m; i++) {
            prefixCap[i + 1] = prefixCap[i] + capacity[i];
        }

        // Quick necessary checks:
        // For every suffix [i..m-1], the total work that must be placed into that suffix
        // cannot exceed the total capacity of that suffix.
        long suffixRequired = 0L;
        for (int i = m - 1; i >= 0; i--) {
            suffixRequired += group[i];
            long suffixCapacity = prefixCap[m] - prefixCap[i];
            if (suffixRequired > suffixCapacity) {
                return false;
            }
        }

        // Precompute subset capacities for DP.
        int totalMasks = 1 << m;
        long[] subsetCapacity = new long[totalMasks];
        for (int mask = 1; mask < totalMasks; mask++) {
            int bit = Integer.numberOfTrailingZeros(mask);
            int prev = mask & (mask - 1);
            subsetCapacity[mask] = subsetCapacity[prev] + capacity[bit];
        }

        // DP over reviewer subsets.
        // dp[mask] = whether we can assign processed groups using exactly reviewers in mask.
        boolean[] dp = new boolean[totalMasks];
        dp[0] = true;

        // Process groups from most constrained to least constrained.
        for (int i = m - 1; i >= 0; i--) {
            long need = group[i];
            if (need == 0L) {
                continue;
            }

            boolean[] next = new boolean[totalMasks];

            // Allowed reviewers for this group are suffix [i..m-1].
            int allowedMask = 0;
            for (int j = i; j < m; j++) {
                allowedMask |= (1 << j);
            }

            for (int used = 0; used < totalMasks; used++) {
                if (!dp[used]) {
                    continue;
                }

                int available = allowedMask & (~used);

                // Enumerate all subsets of available reviewers.
                // If a subset has enough total capacity, we can dedicate it to this group.
                for (int sub = available; sub > 0; sub = (sub - 1) & available) {
                    if (subsetCapacity[sub] >= need) {
                        next[used | sub] = true;
                    }
                }
            }

            dp = next;
        }

        for (boolean ok : dp) {
            if (ok) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the first index i such that arr[i] >= target.
     * Assumes arr is sorted in nondecreasing order and that such an index exists.
     *
     * @param arr sorted array
     * @param target target value
     * @return first index with arr[index] >= target
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public int lowerBound(long[] arr, long target) {
        int left = 0;
        int right = arr.length - 1;
        int answer = arr.length - 1;

        while (left <= right) {
            int mid = left + ((right - left) >>> 1);
            if (arr[mid] >= target) {
                answer = mid;
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        return answer;
    }

    /**
     * Computes ceil(a / b) for positive long values.
     *
     * @param a numerator
     * @param b denominator
     * @return ceiling of a divided by b
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long ceilDiv(long a, long b) {
        return (a + b - 1) / b;
    }

    /**
     * Demonstrates the solution on sample inputs and a few extra checks.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(sample input work)
     * Space complexity: O(1) excluding algorithm internals
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] reviewWork1 = {6, 8, 5, 3};
        int[] speed1 = {4, 2};
        long result1 = solution.minimumReviewTime(reviewWork1, speed1);
        System.out.println(result1); // Expected: 4

        int[] reviewWork2 = {9, 9, 9, 9, 9};
        int[] speed2 = {3, 3, 3};
        long result2 = solution.minimumReviewTime(reviewWork2, speed2);
        System.out.println(result2); // Correct according to the formal problem: 6

        int[] reviewWork3 = {10};
        int[] speed3 = {2, 5, 1};
        long result3 = solution.minimumReviewTime(reviewWork3, speed3);
        System.out.println(result3); // Expected: 2

        int[] reviewWork4 = {7, 7, 7, 7};
        int[] speed4 = {1, 1};
        long result4 = solution.minimumReviewTime(reviewWork4, speed4);
        System.out.println(result4); // Expected: 14
    }
}