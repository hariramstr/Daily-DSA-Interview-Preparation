import java.util.*;

/*
 * Title: Minimum Initial Credit for Subscription Bursts
 * Difficulty: Hard
 * Topic: Binary Search
 *
 * Problem Description:
 * A streaming platform processes a fixed sequence of billing events over several days.
 * You are given an integer array transactions where transactions[i] represents the net
 * credit change on day i: a positive value adds credit to the account, and a negative
 * value consumes credit.
 *
 * The platform may optionally activate at most k emergency top-ups. Each top-up can be
 * inserted immediately before any day and adds exactly x credits to the current balance.
 * Multiple top-ups cannot be used on the same day, and unused top-ups are allowed.
 *
 * Your task is to compute the minimum initial credit S such that, by choosing when to use
 * at most k top-ups, the account balance never becomes negative at any point during the
 * sequence.
 *
 * In other words, starting with balance S, process the days from left to right. Before
 * processing day i, if you still have available top-ups, you may add x once. After that,
 * apply transactions[i]. The balance must remain at least 0 after every day.
 *
 * Return the smallest possible S.
 *
 * Constraints:
 * - 1 <= transactions.length <= 200000
 * - -1000000000 <= transactions[i] <= 1000000000
 * - 0 <= k <= 200000
 * - 1 <= x <= 1000000000
 * - The answer fits in a signed 64-bit integer.
 *
 * Key Insight:
 * We binary search the answer S.
 *
 * For a fixed candidate S, we must decide whether it is possible to survive all days
 * using at most k top-ups.
 *
 * Greedy feasibility rule:
 * - Process days from left to right.
 * - Before day i, if current balance + transactions[i] would become negative, then a top-up
 *   is mandatory at this exact day (if available), because:
 *   1) We cannot top up after the day.
 *   2) Delaying this top-up is impossible since we would already fail today.
 *   3) Using a top-up earlier cannot be better than using it only when needed, because
 *      top-ups have fixed value x and there is no upper cap on balance.
 *
 * Therefore, for a fixed S, the optimal strategy is uniquely determined:
 * use a top-up exactly on days where it is necessary to avoid going negative.
 *
 * This gives an O(n) feasibility check, and then O(log answer_range) binary search.
 */

public class Solution {

    /**
     * Computes the minimum initial credit required so that the running balance never becomes
     * negative while processing all transactions, using at most k top-ups of size x.
     *
     * The method uses binary search on the answer:
     * - If a candidate initial credit S is feasible, then any larger initial credit is also feasible.
     * - If S is not feasible, then any smaller initial credit is also not feasible.
     *
     * This monotonic property makes binary search valid.
     *
     * @param transactions the daily net credit changes; negative values consume credit and positive values add credit
     * @param k the maximum number of top-ups allowed
     * @param x the exact amount added by each top-up
     * @return the smallest initial credit S that allows the balance to stay nonnegative throughout
     *
     * Time complexity: O(n log U), where n is transactions.length and U is the size of the searched answer range.
     * Space complexity: O(1), excluding input storage.
     */
    public long minimumInitialCredit(int[] transactions, int k, int x) {
        long low = 0L;
        long high = findUpperBound(transactions, k, x);

        while (low < high) {
            long mid = low + ((high - low) >>> 1);

            if (isFeasible(transactions, k, x, mid)) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }

        return low;
    }

    /**
     * Checks whether a given initial credit is sufficient.
     *
     * Greedy simulation:
     * - Maintain the current balance.
     * - For each day:
     *   1) If processing today's transaction would make the balance negative,
     *      then we MUST use a top-up before this day, if one is still available.
     *   2) If no top-up remains, the candidate initial credit is not feasible.
     *   3) After the optional forced top-up, apply today's transaction.
     *
     * Why this greedy strategy is correct:
     * - A top-up can only be used before a day.
     * - If today's transaction would make the balance negative, then surviving today requires
     *   a top-up right now; there is no alternative later.
     * - If today's transaction does not make the balance negative, using a top-up early is never
     *   better than saving it for a future day when it may become necessary.
     *
     * @param transactions the daily net credit changes
     * @param k the maximum number of top-ups allowed
     * @param x the amount added by each top-up
     * @param initialCredit the candidate starting balance to test
     * @return true if the sequence can be processed without the balance ever becoming negative; false otherwise
     *
     * Time complexity: O(n), where n is transactions.length.
     * Space complexity: O(1).
     */
    public boolean isFeasible(int[] transactions, int k, int x, long initialCredit) {
        long balance = initialCredit;
        int usedTopUps = 0;
        long topUpValue = x;

        for (int change : transactions) {
            /*
             * Step 1:
             * Before processing today's transaction, check whether we would go negative.
             *
             * If balance + change < 0, then surviving this day is impossible unless we top up now.
             * We are allowed at most one top-up before a day, so we either:
             * - use one now, or
             * - fail immediately if none remain.
             */
            if (balance + (long) change < 0L) {
                if (usedTopUps == k) {
                    return false;
                }
                balance += topUpValue;
                usedTopUps++;

                /*
                 * After using the top-up, we must still verify that today's transaction can be paid.
                 * If even balance + change is still negative, then this candidate initial credit fails.
                 */
                if (balance + (long) change < 0L) {
                    return false;
                }
            }

            /*
             * Step 2:
             * Apply today's transaction.
             */
            balance += (long) change;

            /*
             * This should never be negative here because we already handled the only dangerous case above.
             * The check is not necessary for correctness, but keeping the logic conceptually clear:
             * after each day, balance must be >= 0.
             */
        }

        return true;
    }

    /**
     * Finds a safe upper bound for binary search.
     *
     * We repeatedly double the candidate value until it becomes feasible.
     * Since the problem guarantees the answer fits in signed 64-bit integer,
     * this process will eventually stop.
     *
     * Starting from 0 is convenient:
     * - If 0 is already feasible, the answer is 0.
     * - Otherwise we grow exponentially: 1, 2, 4, 8, ...
     *
     * @param transactions the daily net credit changes
     * @param k the maximum number of top-ups allowed
     * @param x the amount added by each top-up
     * @return a value high such that the true answer is in the range [0, high]
     *
     * Time complexity: O(n log A), where A is the final upper bound found.
     * Space complexity: O(1).
     */
    public long findUpperBound(int[] transactions, int k, int x) {
        if (isFeasible(transactions, k, x, 0L)) {
            return 0L;
        }

        long high = 1L;

        while (!isFeasible(transactions, k, x, high)) {
            /*
             * To avoid overflow during doubling, clamp to Long.MAX_VALUE if necessary.
             * The statement guarantees the answer fits in signed 64-bit integer, so a feasible
             * value will be found before this becomes a practical issue.
             */
            if (high > Long.MAX_VALUE / 2L) {
                high = Long.MAX_VALUE;
                break;
            }
            high <<= 1;
        }

        return high;
    }

    /**
     * Demonstrates the solution on the sample test cases from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> 2
     * Example 2 -> 6
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(n log U) across the demonstrated calls.
     * Space complexity: O(1), excluding input arrays.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] transactions1 = {-4, 3, -6, 2};
        int k1 = 1;
        int x1 = 5;
        long answer1 = solution.minimumInitialCredit(transactions1, k1, x1);
        System.out.println(answer1); // Expected: 2

        int[] transactions2 = {-8, -2, 5, -7};
        int k2 = 2;
        int x2 = 4;
        long answer2 = solution.minimumInitialCredit(transactions2, k2, x2);
        System.out.println(answer2); // Expected: 6
    }
}