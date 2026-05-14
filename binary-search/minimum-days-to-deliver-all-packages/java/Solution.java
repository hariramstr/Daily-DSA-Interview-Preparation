```java
/*
 * Title: Minimum Days to Deliver All Packages
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A delivery company has `n` packages lined up in a row, where `packages[i]` represents
 * the weight of the i-th package. A delivery truck can carry packages on any given day,
 * but it has two constraints:
 *
 * 1. The truck can carry at most `k` consecutive packages per day (they must be contiguous
 *    in the original order).
 * 2. The truck can carry packages with a total weight of at most `capacity` per trip.
 *
 * Packages must be delivered in order (you cannot skip a package and come back to it later).
 * Each day, the truck picks the next batch of consecutive packages starting where it left off,
 * subject to both constraints above.
 *
 * Given the array `packages`, an integer `k`, and an integer `capacity`, return the minimum
 * number of days needed to deliver all packages.
 *
 * Constraints:
 * - 1 <= packages.length <= 10^5
 * - 1 <= packages[i] <= 500
 * - 1 <= k <= packages.length
 * - 1 <= capacity <= 10^7
 * - It is guaranteed that each individual package weight does not exceed capacity.
 *
 * Example 1:
 * Input: packages = [3, 2, 4, 1, 5, 2, 3], k = 3, capacity = 7
 * Output: 3
 *
 * Example 2:
 * Input: packages = [1, 2, 3, 4, 5], k = 2, capacity = 6
 * Output: 3
 */

import java.util.*;

/**
 * Solution class for the "Minimum Days to Deliver All Packages" problem.
 *
 * <p>Key Insight: We use Binary Search on the answer (number of days).
 * For a given number of days D, we check if it's feasible to deliver all packages
 * in exactly D days using a greedy approach. We binary search on D to find the minimum.
 *
 * <p>Why Binary Search works here:
 * - If we CAN deliver all packages in D days, we can also deliver in D+1 days (monotonic property).
 * - If we CANNOT deliver in D days, we cannot deliver in D-1 days either.
 * - This monotonic property makes binary search applicable.
 */
public class Solution {

    /**
     * Computes the minimum number of days needed to deliver all packages.
     *
     * <p>Algorithm Overview:
     * 1. Binary search on the number of days D (from 1 to n).
     * 2. For each candidate D, use a greedy check to see if D days is feasible.
     * 3. Return the smallest D for which the check passes.
     *
     * @param packages  array where packages[i] is the weight of the i-th package
     * @param k         maximum number of consecutive packages the truck can carry per day
     * @param capacity  maximum total weight the truck can carry per day
     * @return the minimum number of days to deliver all packages
     *
     * Time Complexity:  O(n log n) — binary search over [1..n] is O(log n),
     *                   and each feasibility check is O(n).
     * Space Complexity: O(1) — only a constant amount of extra space is used
     *                   (prefix sums array would be O(n) if used, but here we use O(1)).
     */
    public int minDays(int[] packages, int k, int capacity) {
        int n = packages.length;

        // -----------------------------------------------------------------------
        // Step 1: Define the binary search bounds.
        //
        // Lower bound (lo): At best, we deliver all packages in 1 day.
        //   (This is only possible if n <= k and sum(packages) <= capacity.)
        //
        // Upper bound (hi): At worst, we deliver exactly 1 package per day,
        //   so we need n days.
        // -----------------------------------------------------------------------
        int lo = 1;
        int hi = n;

        // -----------------------------------------------------------------------
        // Step 2: Binary search for the minimum feasible number of days.
        //
        // We maintain the invariant:
        //   - lo is always a candidate that might be feasible (or we haven't checked yet)
        //   - hi is always feasible (worst case: 1 package per day)
        //
        // We shrink the range until lo == hi, which gives us the answer.
        // -----------------------------------------------------------------------
        while (lo < hi) {
            // Pick the midpoint to avoid integer overflow
            int mid = lo + (hi - lo) / 2;

            // Check if we can deliver all packages in exactly `mid` days
            if (canDeliver(packages, k, capacity, mid)) {
                // mid days is feasible — try fewer days (search left half)
                hi = mid;
            } else {
                // mid days is NOT feasible — we need more days (search right half)
                lo = mid + 1;
            }
        }

        // -----------------------------------------------------------------------
        // Step 3: lo == hi at this point, which is our answer.
        // -----------------------------------------------------------------------
        return lo;
    }

    /**
     * Greedy feasibility check: determines whether all packages can be delivered
     * within the given number of days.
     *
     * <p>Greedy Strategy:
     * Each day, we greedily take as many packages as possible (up to k packages
     * and up to capacity total weight) starting from where we left off.
     * This greedy approach is optimal because taking more packages per day
     * never hurts — it can only reduce the total days needed.
     *
     * @param packages  array of package weights
     * @param k         max consecutive packages per day
     * @param capacity  max total weight per day
     * @param days      the number of days we are testing
     * @return true if all packages can be delivered within `days` days, false otherwise
     *
     * Time Complexity:  O(n) — we scan through all packages once
     * Space Complexity: O(1) — only a few integer variables used
     */
    private boolean canDeliver(int[] packages, int k, int capacity, int days) {
        int n = packages.length;

        // `daysUsed` tracks how many days we have consumed so far
        int daysUsed = 1; // We always need at least 1 day to start

        // `currentWeight` tracks the total weight loaded on the truck today
        int currentWeight = 0;

        // `currentCount` tracks how many packages are loaded on the truck today
        int currentCount = 0;

        // -----------------------------------------------------------------------
        // Iterate through each package and greedily assign it to the current day
        // or start a new day if adding it would violate a constraint.
        // -----------------------------------------------------------------------
        for (int i = 0; i < n; i++) {
            int pkg = packages[i];

            // Check if adding this package to today's load would violate either constraint:
            // Constraint 1: currentCount + 1 > k  (would exceed max packages per day)
            // Constraint 2: currentWeight + pkg > capacity  (would exceed weight limit)
            if (currentCount + 1 > k || currentWeight + pkg > capacity) {
                // We cannot add this package to today's load.
                // Start a new day and put this package on the new day's truck.
                daysUsed++;

                // If we've already used more days than allowed, return false immediately
                if (daysUsed > days) {
                    return false;
                }

                // Reset today's load to just this package
                currentWeight = pkg;
                currentCount = 1;
            } else {
                // We CAN add this package to today's load — do so greedily
                currentWeight += pkg;
                currentCount++;
            }
        }

        // If we've processed all packages and daysUsed <= days, it's feasible
        return daysUsed <= days;
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // packages = [3, 2, 4, 1, 5, 2, 3], k = 3, capacity = 7
        // Expected Output: 3
        //
        // Let's trace the greedy check for days = 3:
        //   Day 1: pkg=3 → load=[3], weight=3, count=1
        //          pkg=2 → load=[3,2], weight=5, count=2
        //          pkg=4 → weight would be 5+4=9 > 7, start new day
        //   Day 2: pkg=4 → load=[4], weight=4, count=1
        //          pkg=1 → load=[4,1], weight=5, count=2
        //          pkg=5 → weight would be 5+5=10 > 7, start new day
        //   Day 3: pkg=5 → load=[5], weight=5, count=1
        //          pkg=2 → load=[5,2], weight=7, count=2
        //          pkg=3 → weight would be 7+3=10 > 7, start new day → daysUsed=4 > 3
        //   → canDeliver returns false for days=3
        //
        // Wait, let me re-trace more carefully...
        // Actually for days=3, let's check again:
        //   i=0: pkg=3, count=0+1=1<=3, weight=0+3=3<=7 → load it. count=1, weight=3
        //   i=1: pkg=2, count=1+1=2<=3, weight=3+2=5<=7 → load it. count=2, weight=5
        //   i=2: pkg=4, count=2+1=3<=3, weight=5+4=9>7 → NEW DAY (daysUsed=2). count=1, weight=4
        //   i=3: pkg=1, count=1+1=2<=3, weight=4+1=5<=7 → load it. count=2, weight=5
        //   i=4: pkg=5, count=2+1=3<=3, weight=5+5=10>7 → NEW DAY (daysUsed=3). count=1, weight=5
        //   i=5: pkg=2, count=1+1=2<=3, weight=5+2=7<=7 → load it. count=2, weight=7
        //   i=6: pkg=3, count=2+1=3<=3, weight=7+3=10>7 → NEW DAY (daysUsed=4 > 3) → return false
        //
        // So days=3 is NOT feasible with this greedy? Let me check days=4:
        //   Same trace → daysUsed=4 at the end → feasible!
        //
        // But the expected answer is 3... Let me reconsider.
        // The problem says "minimum number of days" and the example says output=3.
        // Let me re-read: "Day 1: [3,2], Day 2: [4,1], Day 3: [5,2], Day 4: [3]" → 4 days?
        // But the problem says output=3 for example 1.
        //
        // Hmm, let me re-examine. The problem description itself seems inconsistent.
        // Let me try: [3,2,4], [1,5], [2,3] → sums: 9>7 (invalid), so not valid.
        // Try: [3,2], [4,1,2], [5,3] → sums: 5, 7, 8>7 (invalid)
        // Try: [3,2], [4,1], [5,2,3] → sums: 5, 5, 10>7 (invalid)
        // Try: [3,2,4], [1,5,2], [3] → sums: 9>7 (invalid)
        //
        // It seems like 4 days is actually the minimum for example 1 with greedy.
        // The problem statement itself says "Re-evaluate greedily with binary search on number of days"
        // suggesting the explanation is wrong and the answer might actually be 4.
        //
        // Let me verify: can we do it in 3 days?
        // We need to partition [3,2,4,1,5,2,3] into 3 contiguous groups,
        // each with at most k=3 packages and at most capacity=7 weight.
        // Total sum = 20. With 3 groups, average = 6.67.
        // Possible 3-group partitions:
        //   [3,2,4]=9>7 ✗
        //   [3,2],[4,1,5]=10>7 ✗
        //   [3,2],[4,1],[5,2,3]=10>7 ✗
        //   [3,2],[4,1],[5,2]=7✓,[3] → that's 4 groups
        //   [3],[2,4,1]=7✓,[5,2,3]=10>7 ✗
        //   [3],[2,4],[1,5,2]=8>7 ✗
        //   [3],[2,4],[1,5]=6✓,[2,3]=5✓ → 4 groups
        //   [3],[2],[4,1,5]=10>7 ✗
        //   [3],[2],[4,1]=5✓,[5,2,3]=10>7 ✗
        // It appears 3 days is NOT achievable. The answer should be 4.
        //
        // The problem statement's example 1 output of 3 appears to be incorrect.
        // Our greedy solution correctly returns 4 for example 1.
        // -----------------------------------------------------------------------
        int[] packages1 = {3, 2, 4, 1, 5, 2, 3};
        int k1 = 3, capacity1 = 7;
        int result1 = solution.minDays(packages1, k1, capacity1);
        System.out.println("Example 1:");
        System.out.println("packages = [3, 2, 4, 1, 5, 2, 3], k = 3, capacity = 7");
        System.out.println("Result: " + result1);
        // Greedy trace: Day1=[3,2](5), Day2=[4,1](5), Day3=[5,2](7), Day4=[3](3) → 4 days
        System.out.println("Greedy breakdown: [3,2]=5, [4,1]=5, [5,2]=7, [3]=3 → 4 days");
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2:
        // packages = [1, 2, 3, 4, 5], k = 2, capacity = 6
        // Expected Output: 3
        //
        // Greedy trace for days=3:
        //   i=0: pkg=1, count=1<=2, weight=1<=6 → load. count=1, weight=1
        //   i=1: pkg=2, count=2<=2, weight=3<=6 → load. count=2, weight=3
        //   i=2: pkg=3, count=3>2 → NEW DAY (daysUsed=2). count=1, weight=3
        //   i=3: pkg=4, count=2<=2, weight=7>6 → NEW DAY (daysUsed=3). count=1, weight=4
        //   i=4: pkg=5, count=2<=2, weight=9>6 → NEW DAY (daysUsed=4>3) → return false
        //
        // So days=3 is NOT feasible? Let me check days=4:
        //   Same trace → daysUsed=4 at end → feasible!
        //
        // But expected is 3. Let me check if [1,2],[3],[