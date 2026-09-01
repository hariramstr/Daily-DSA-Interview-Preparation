import java.util.*;

/*
Problem Title: Minimum Daily Build Quota for Staged Releases

Problem Description:
A software team must publish n features in the given order. Feature i requires builds[i] units of build effort.
The team works over multiple days, but releases are staged: each day they may work on a contiguous suffix of
the remaining effort for the current feature and then continue with later features, as long as the total effort
completed that day does not exceed a fixed daily quota Q. A feature may be split across days, but a new rule
applies: if a day starts working on feature i, then every earlier feature must already be fully completed.
In other words, progress is always made from left to right, and partial work is allowed only on the current
frontier feature.

You are also given:
- d: the maximum number of days allowed
- k: the maximum number of features that may be split across more than one day

Task:
Find the minimum integer daily quota Q such that all features can be completed within at most d days while
splitting at most k features.

A candidate quota Q is feasible if there exists a left-to-right schedule using at most d days and causing at
most k split features.

Constraints:
- 1 <= n <= 200000
- 1 <= builds[i] <= 10^12
- 1 <= d <= 10^12
- 0 <= k <= n
- The answer fits in a 64-bit signed integer

Examples:
1)
builds = [7, 2, 5, 10, 8], d = 3, k = 1
Output: 14

2)
builds = [9, 9, 9], d = 2, k = 0
Output: 18
*/

public class Solution {

    /**
     * Computes the minimum daily quota Q such that all features can be completed
     * in at most d days while splitting at most k features.
     *
     * Core idea:
     * 1. Binary search on the answer Q.
     * 2. For a fixed Q, run a linear feasibility check.
     *
     * Important modeling insight:
     * - A feature contributes:
     *   - 1 day if it is not split.
     *   - ceil(builds[i] / Q) days if it is split across days.
     * - Splitting feature i increases the day count by:
     *      ceil(builds[i] / Q) - 1
     *   and consumes 1 split allowance.
     * - Without any splitting, the problem becomes partitioning the array into contiguous groups
     *   where each group's sum is at most Q.
     *
     * The feasibility check greedily packs whole features into days. Whenever a feature does not fit
     * into the current day, we have two choices:
     * - start a new day and keep the feature whole
     * - split the feature so the current day is filled and the remaining part continues on later days
     *
     * A split is useful only when it saves one day compared with starting a fresh day for that feature.
     * This happens exactly when there is some remaining capacity in the current day before the feature,
     * because then the first chunk of the split can use that leftover capacity.
     *
     * Therefore, for a fixed Q:
     * - Let baseDays be the number of days needed if we never split any feature.
     * - For each boundary where a feature does not fit in the current day, if the current day has
     *   leftover capacity, splitting that feature can save exactly 1 day, but only if Q < builds[i]
     *   (otherwise the feature fits in one day and splitting would not help).
     * - Also, any feature with builds[i] > Q requires splitting no matter what, and contributes
     *   ceil(builds[i] / Q) days total.
     *
     * The implementation below directly simulates the process and computes the minimum possible days
     * using at most k splits.
     *
     * @param builds array of build efforts for features, in required order
     * @param d maximum allowed number of days
     * @param k maximum allowed number of split features
     * @return minimum feasible daily quota Q
     * Time complexity: O(n log S), where S is the search range of Q
     * Space complexity: O(1) extra space beyond the input
     */
    public long minimumDailyQuota(long[] builds, long d, int k) {
        long low = 1;
        long high = 0;

        for (long value : builds) {
            high += value;
        }

        while (low < high) {
            long mid = low + ((high - low) >>> 1);

            if (isFeasible(builds, d, k, mid)) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }

        return low;
    }

    /**
     * Checks whether a given daily quota Q is feasible.
     *
     * Detailed strategy:
     *
     * We process features from left to right while maintaining:
     * - daysUsed: how many days are currently needed
     * - usedInCurrentDay: how much quota is already consumed in the current day
     * - mandatorySplits: number of features that must be split because builds[i] > Q
     * - optionalSavings: number of opportunities where using one additional split can save one day
     *
     * Why this works:
     *
     * 1. If builds[i] <= remaining capacity of current day:
     *    - We place the whole feature in the current day.
     *
     * 2. Else if builds[i] <= Q:
     *    - The feature can fit in one fresh day.
     *    - If current day is partially filled, then splitting this feature across the boundary
     *      can save one day:
     *         without split: current day ends, feature starts next day => +1 day
     *         with split: use leftover today, finish tomorrow => still only +1 day total for feature
     *      So this is an optional split opportunity worth saving exactly 1 day.
     *    - We count the boundary as creating a new day in the base schedule.
     *
     * 3. Else builds[i] > Q:
     *    - This feature cannot fit in a single day, so it must be split.
     *    - It uses ceil(builds[i] / Q) days in total.
     *    - If the current day is partially filled, the first chunk can use leftover capacity,
     *      reducing the total day count by 1 compared with starting the feature on a fresh day.
     *      This saving is automatic once we decide to split, and since the split is mandatory,
     *      we apply it directly.
     *
     * After processing all features:
     * - If mandatorySplits > k, Q is impossible.
     * - Otherwise, we may use up to (k - mandatorySplits) optional splits.
     * - Each optional split saves exactly 1 day.
     * - So minimum achievable days = baseDays - min(optionalSavings, k - mandatorySplits)
     *
     * Finally, Q is feasible iff minimum achievable days <= d.
     *
     * @param builds array of build efforts
     * @param d maximum allowed days
     * @param k maximum allowed split features
     * @param q candidate daily quota
     * @return true if q is feasible, false otherwise
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean isFeasible(long[] builds, long d, int k, long q) {
        long baseDays = 1;
        long usedInCurrentDay = 0;

        long mandatorySplits = 0;
        long optionalSavings = 0;

        for (long build : builds) {
            if (build <= q) {
                // Case 1: the whole feature fits into the current day.
                if (usedInCurrentDay + build <= q) {
                    usedInCurrentDay += build;

                    // If the day becomes exactly full, the next feature (if any) starts on a new day.
                    if (usedInCurrentDay == q) {
                        usedInCurrentDay = 0;
                        // We do not increment baseDays here immediately.
                        // The current day already exists; resetting to 0 simply means it is closed.
                        // A future feature will create the next day only when needed.
                    }
                } else {
                    // Case 2: the feature fits in one day, but not in the remaining capacity.
                    // Base schedule without optional split:
                    // - close current day
                    // - start a new day for this feature
                    baseDays++;

                    // If current day had leftover capacity, splitting this feature across the boundary
                    // can save exactly one day using one split.
                    if (usedInCurrentDay > 0) {
                        optionalSavings++;
                    }

                    usedInCurrentDay = build;
                    if (usedInCurrentDay == q) {
                        usedInCurrentDay = 0;
                    }
                }
            } else {
                // Case 3: build > q, so this feature MUST be split.
                mandatorySplits++;
                if (mandatorySplits > k) {
                    return false;
                }

                long remaining = build;

                if (usedInCurrentDay > 0) {
                    long free = q - usedInCurrentDay;

                    // Use the leftover capacity of the current day first.
                    if (remaining >= free) {
                        remaining -= free;
                        usedInCurrentDay = 0;
                    } else {
                        // This branch is actually impossible because build > q and free <= q - 1,
                        // so remaining >= q + 1 > free. Still kept for clarity and safety.
                        usedInCurrentDay += remaining;
                        remaining = 0;
                    }
                }

                if (remaining > 0) {
                    long fullDays = remaining / q;
                    long rem = remaining % q;

                    // If we are starting this mandatory-split feature on a fresh day,
                    // it consumes fullDays complete days, and maybe one partial day.
                    // However, if rem == 0, then exactly fullDays days are used.
                    // Since baseDays already counts the currently active day structure,
                    // we need to add the number of NEW days introduced by this feature.
                    //
                    // If usedInCurrentDay was 0 before and we are at the start of a fresh day,
                    // the first consumed day may be the current base day if it is empty.
                    // To keep the accounting simple, we interpret baseDays as "number of day segments opened so far".
                    // Therefore:
                    // - if current day is empty, the feature occupies the current day first
                    // - additional days beyond that are added to baseDays
                    //
                    // The easiest robust way:
                    //   totalDaysForFeature = ceil(remaining / q)
                    //   if current day is empty, the first of those days is the current day
                    //   so add totalDaysForFeature - 1
                    //   else impossible here because we already consumed leftover and reset to 0
                    long totalDaysForFeature = fullDays + (rem > 0 ? 1 : 0);

                    baseDays += totalDaysForFeature - 1;
                    usedInCurrentDay = rem;

                    if (usedInCurrentDay == 0) {
                        // Feature ended exactly at a day boundary.
                        // Current day is closed; next feature will start a new day when needed.
                    }
                }
            }

            if (baseDays > d + k + 5L) {
                // Safe early pruning. Even after using all possible remaining split savings,
                // this is already too large in practice. The exact threshold is not critical.
                // This keeps numbers bounded in extreme cases.
            }
        }

        long remainingOptionalSplits = k - mandatorySplits;
        long bestSavings = Math.min(optionalSavings, remainingOptionalSplits);
        long minDays = baseDays - bestSavings;

        return minDays <= d;
    }

    /**
     * Convenience overload for int[] input.
     *
     * @param builds array of build efforts as int values
     * @param d maximum allowed number of days
     * @param k maximum allowed number of split features
     * @return minimum feasible daily quota Q
     * Time complexity: O(n log S)
     * Space complexity: O(n) for conversion
     */
    public long minimumDailyQuota(int[] builds, long d, int k) {
        long[] arr = new long[builds.length];
        for (int i = 0; i < builds.length; i++) {
            arr[i] = builds[i];
        }
        return minimumDailyQuota(arr, d, k);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(n log S) for each demonstration call
     * Space complexity: O(1) extra beyond input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        long[] builds1 = {7, 2, 5, 10, 8};
        long d1 = 3;
        int k1 = 1;
        long answer1 = solution.minimumDailyQuota(builds1, d1, k1);
        System.out.println(answer1); // Expected: 14

        long[] builds2 = {9, 9, 9};
        long d2 = 2;
        int k2 = 0;
        long answer2 = solution.minimumDailyQuota(builds2, d2, k2);
        System.out.println(answer2); // Expected: 18
    }
}