```java
/*
 * Title: Minimum Days to Distribute K Types of Packages
 * Difficulty: Hard
 * Topic: Binary Search
 *
 * Problem Description:
 * A warehouse has `n` workers and `m` package types. Each package type `i` has a weight
 * `weights[i]` and a count `counts[i]` representing how many packages of that type exist.
 * Each worker can carry packages on a given day, but the total weight they carry must not
 * exceed a given capacity `C`. On each day, every worker makes exactly one trip, and workers
 * can only carry packages of the same type in a single trip.
 *
 * You are given an integer `k` — you must ensure that at least `k` distinct package types
 * are fully distributed (all packages of those types delivered) in the minimum number of days.
 * You want to find the minimum number of days required to fully distribute at least `k`
 * distinct package types.
 *
 * Formally, binary search on the number of days `d`. For a given `d` days, determine the
 * maximum number of distinct package types that can be fully distributed using `n` workers
 * each carrying up to `C` weight per day.
 *
 * Constraints:
 * - 1 <= n <= 10^4
 * - 1 <= m <= 10^5
 * - 1 <= k <= m
 * - 1 <= weights[i] <= C <= 10^6
 * - 1 <= counts[i] <= 10^9
 */

import java.util.Arrays;

/**
 * Solution class for the "Minimum Days to Distribute K Types of Packages" problem.
 *
 * <p>Core Idea:
 * We binary search on the answer `d` (number of days). For each candidate `d`,
 * we check: "How many package types can be fully distributed in exactly `d` days?"
 * If that count >= k, then `d` days is feasible.
 *
 * <p>For a single package type with weight `w` and count `cnt`:
 * - Each worker can carry floor(C / w) packages per trip (per day).
 * - With `n` workers, total packages delivered per day = n * floor(C / w).
 * - Days needed to finish this type = ceil(cnt / (n * floor(C / w))).
 *
 * <p>To maximize the number of types distributed in `d` days, we greedily pick
 * the types that require the fewest days (i.e., sort by days needed ascending
 * and pick the first k).
 */
public class Solution {

    /**
     * Computes the minimum number of days required to fully distribute
     * at least {@code k} distinct package types.
     *
     * <p>Algorithm Overview:
     * <ol>
     *   <li>For each package type, compute the minimum days needed to distribute it.</li>
     *   <li>Sort these day-counts in ascending order.</li>
     *   <li>Binary search on the answer `d`. For a given `d`, count how many types
     *       can be distributed in <= d days (using the sorted array, this is just
     *       the number of types whose required days <= d).</li>
     *   <li>Find the smallest `d` such that at least `k` types can be distributed.</li>
     * </ol>
     *
     * @param n       the number of workers
     * @param C       the maximum weight capacity per worker per trip
     * @param weights the weight of each package type (length m)
     * @param counts  the count of packages for each type (length m)
     * @param k       the minimum number of distinct types that must be fully distributed
     * @return the minimum number of days to distribute at least k distinct package types
     *
     * Time Complexity:  O(m log m + m log(maxDays)) where maxDays can be up to ~10^14
     *                   (counts[i] up to 10^9, n*floor(C/w) >= 1, so max days ~ 10^9)
     *                   Practically O(m log m) dominates for sorting.
     * Space Complexity: O(m) for storing the days array.
     */
    public long minDays(int n, int C, int[] weights, int[] counts, int k) {
        int m = weights.length; // number of package types

        // -----------------------------------------------------------------------
        // Step 1: For each package type, compute the minimum days needed to
        //         fully distribute all its packages.
        //
        //         For type i:
        //           packagesPerWorkerPerDay = floor(C / weights[i])
        //           totalPackagesPerDay     = n * packagesPerWorkerPerDay
        //           daysNeeded[i]           = ceil(counts[i] / totalPackagesPerDay)
        //
        //         Special case: if weights[i] > C, a worker cannot carry even one
        //         package of this type, so it can never be distributed.
        //         We represent this with Long.MAX_VALUE (infinity).
        // -----------------------------------------------------------------------
        long[] daysNeeded = new long[m];

        for (int i = 0; i < m; i++) {
            // How many packages of type i can one worker carry in one trip?
            long packagesPerWorkerPerDay = C / weights[i];

            if (packagesPerWorkerPerDay == 0) {
                // Worker cannot carry even one package — this type is impossible
                daysNeeded[i] = Long.MAX_VALUE;
            } else {
                // Total packages all n workers can deliver in one day for this type
                long totalPerDay = (long) n * packagesPerWorkerPerDay;

                // Days needed = ceil(counts[i] / totalPerDay)
                // ceil(a / b) = (a + b - 1) / b  for positive integers
                daysNeeded[i] = (counts[i] + totalPerDay - 1) / totalPerDay;
            }
        }

        // -----------------------------------------------------------------------
        // Step 2: Sort the daysNeeded array in ascending order.
        //
        //         After sorting, daysNeeded[0] <= daysNeeded[1] <= ... <= daysNeeded[m-1].
        //         The types that are easiest to distribute (fewest days) come first.
        //
        //         To distribute at least k types in d days, we greedily pick the
        //         k types with the smallest daysNeeded values. The answer is the
        //         k-th smallest value in daysNeeded (0-indexed: daysNeeded[k-1]).
        // -----------------------------------------------------------------------
        Arrays.sort(daysNeeded);

        // -----------------------------------------------------------------------
        // Step 3: The answer is simply daysNeeded[k-1] after sorting.
        //
        //         Why? After sorting ascending, the first k entries are the k types
        //         that can be distributed in the fewest days. To distribute ALL k of
        //         them, we need at least daysNeeded[k-1] days (the maximum among the
        //         k easiest types). This is the minimum d such that >= k types are
        //         feasible.
        //
        //         Note: If daysNeeded[k-1] == Long.MAX_VALUE, it means fewer than k
        //         types can ever be distributed (some types have weight > C). In a
        //         well-formed input per constraints (weights[i] <= C), this won't happen.
        // -----------------------------------------------------------------------

        // The k-th smallest days value (1-indexed k, so index k-1) is our answer.
        // With d = daysNeeded[k-1] days, exactly the first k types (sorted) can be
        // distributed, satisfying the requirement of >= k types.
        return daysNeeded[k - 1];
    }

    /**
     * Alternative explicit binary search approach for educational purposes.
     * Produces the same result as {@link #minDays}.
     *
     * <p>We binary search on `d` in range [1, maxPossibleDays].
     * For each `d`, we count how many types have daysNeeded[i] <= d.
     * We find the smallest `d` where this count >= k.
     *
     * @param n       the number of workers
     * @param C       the maximum weight capacity per worker per trip
     * @param weights the weight of each package type
     * @param counts  the count of packages for each type
     * @param k       the minimum number of distinct types to distribute
     * @return the minimum number of days
     *
     * Time Complexity:  O(m log m + m * log(maxDays)) — binary search over days
     * Space Complexity: O(m) for the daysNeeded array
     */
    public long minDaysBinarySearch(int n, int C, int[] weights, int[] counts, int k) {
        int m = weights.length;

        // Step 1: Compute days needed for each type (same as above)
        long[] daysNeeded = new long[m];
        for (int i = 0; i < m; i++) {
            long packagesPerWorkerPerDay = C / weights[i];
            if (packagesPerWorkerPerDay == 0) {
                daysNeeded[i] = Long.MAX_VALUE / 2; // effectively infinity
            } else {
                long totalPerDay = (long) n * packagesPerWorkerPerDay;
                daysNeeded[i] = (counts[i] + totalPerDay - 1) / totalPerDay;
            }
        }

        // Step 2: Sort ascending so we can binary search
        Arrays.sort(daysNeeded);

        // Step 3: Binary search on the answer d
        // Lower bound: 1 day (minimum possible)
        // Upper bound: daysNeeded[k-1] after sorting (we know the answer is at most this)
        // But for a pure binary search demo, let's use a wide range.
        long lo = 1;
        long hi = daysNeeded[k - 1]; // The answer cannot exceed this

        // We want the smallest d such that countFeasible(d) >= k
        while (lo < hi) {
            long mid = lo + (hi - lo) / 2;

            // Count how many types can be distributed in <= mid days
            // Since daysNeeded is sorted, use binary search to find the count
            int feasibleCount = countFeasibleTypes(daysNeeded, mid);

            if (feasibleCount >= k) {
                // mid days is enough — try fewer days
                hi = mid;
            } else {
                // mid days is not enough — need more days
                lo = mid + 1;
            }
        }

        return lo;
    }

    /**
     * Counts how many package types can be fully distributed in at most {@code d} days.
     *
     * <p>Since {@code daysNeeded} is sorted in ascending order, we use binary search
     * to find the rightmost index where daysNeeded[index] <= d.
     *
     * @param daysNeeded sorted array of days needed for each package type
     * @param d          the number of days available
     * @return the number of types that can be distributed in at most d days
     *
     * Time Complexity:  O(log m) — binary search on sorted array
     * Space Complexity: O(1)
     */
    private int countFeasibleTypes(long[] daysNeeded, long d) {
        // Binary search for the rightmost position where daysNeeded[pos] <= d
        int lo = 0, hi = daysNeeded.length;

        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (daysNeeded[mid] <= d) {
                lo = mid + 1; // this type is feasible, look for more
            } else {
                hi = mid; // this type needs more than d days
            }
        }

        // lo is the count of elements <= d (since we moved lo past each feasible element)
        return lo;
    }

    /**
     * Main method demonstrating the solution with the provided examples.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // n=3, C=10, weights=[4,6,3], counts=[12,6,9], k=2
        //
        // For type 0 (weight=4, count=12):
        //   packagesPerWorkerPerDay = floor(10/4) = 2
        //   totalPerDay = 3 * 2 = 6
        //   daysNeeded = ceil(12/6) = 2
        //
        // For type 1 (weight=6, count=6):
        //   packagesPerWorkerPerDay = floor(10/6) = 1
        //   totalPerDay = 3 * 1 = 3
        //   daysNeeded = ceil(6/3) = 2
        //
        // For type 2 (weight=3, count=9):
        //   packagesPerWorkerPerDay = floor(10/3) = 3
        //   totalPerDay = 3 * 3 = 9
        //   daysNeeded = ceil(9/9) = 1
        //
        // daysNeeded = [2, 2, 1]
        // After sorting: [1, 2, 2]
        // k=2, answer = daysNeeded[k-1] = daysNeeded[1] = 2
        //
        // Expected output: 2 ✓
        // -----------------------------------------------------------------------
        int n1 = 3, C1 = 10, k1 = 2;
        int[] weights1 = {4, 6, 3};
        int[] counts1 = {12, 6, 9};
        long result1 = sol.minDays(n1, C1, weights1, counts1, k1);
        System.out.println("Example 1:");
        System.out.println("  n=" + n1 + ", C=" + C1 + ", weights=" + Arrays.toString(weights1)
                + ", counts=" + Arrays.toString(counts1) + ", k=" + k1);
        System.out.println("  Minimum days = " + result1);
        System.out.println("  Expected: 2");
        System.out.println("  " + (result1 == 2 ? "PASS ✓" : "FAIL ✗"));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2:
        // n=2, C=5, weights=[5,3,4], counts=[10,15,8], k=3
        //
        // For type 0 (weight=5, count=10):
        //   packagesPerWorkerPerDay = floor(5/5) = 1
        //   totalPerDay = 2 * 1 = 2
        //   daysNeeded = ceil(10/2) = 5
        //
        // For type 1 (weight=3, count=15):
        //   packagesPerWorkerPerDay = floor(5/3) = 1
        //   totalPerDay = 2 * 1 = 2
        //   daysNeeded = ceil(15/2) = 8
        //
        // For type 2 (weight=4, count=8):
        //   packagesPerWorkerPerDay = floor(5/4) = 1
        //   totalPerDay = 2 * 1 = 2
        //   daysNeeded = ceil(8/2) = 4
        //
        // daysNeeded = [5, 8, 4]
        // After sorting: [4, 5, 8]
        // k=3, answer = daysNeeded[k-1] = daysNeeded[2] = 8
        //
        // Wait — the problem says the answer is 5. Let me re-read the problem.
        //
        // The problem explanation says:
        // "Type 0: ceil(10/(2*1))=5 days. Type 1: ceil(15/(2*1))=8 days.
        //  Type 2: ceil(8/(2*1))=4 days. To get 3 types, pick types 0,2 and one more;
        //  minimum days needed is 5."