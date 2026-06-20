import java.util.*;

/*
Problem Title: Minimum Daily Charge to Finish Fleet Deliveries

Problem Description:
A logistics company operates a fleet of electric vans. Each van i must complete
deliveryDistance[i] kilometers of work. If the company sets the charging system
to provide a daily charge budget of X kilometers per van, then van i can complete
its route in ceil(deliveryDistance[i] / X) days, because it can recharge to cover
at most X kilometers per day. All vans work in parallel, and the company wants
every van to finish within at most maxDays total days.

Your task is to find the minimum integer value X such that the sum of days needed
by all vans is less than or equal to maxDays. If X is too small, the total required
days will exceed the limit. If X is large enough, the fleet can finish on time.

Return the smallest possible daily charge budget X.

Constraints:
- 1 <= deliveryDistance.length <= 100000
- 1 <= deliveryDistance[i] <= 1000000000
- deliveryDistance.length <= maxDays <= 1000000000000
- X must be a positive integer

Example 1:
Input: deliveryDistance = [12, 7, 25, 9], maxDays = 10
Output: 7
Explanation:
With X = 7, the required days are:
ceil(12/7)=2, ceil(7/7)=1, ceil(25/7)=4, ceil(9/7)=2
Total = 9 days, which is within 10.

With X = 6, the total becomes:
ceil(12/6)=2, ceil(7/6)=2, ceil(25/6)=5, ceil(9/6)=2
Total = 11 days, which is too many.

So 7 is the minimum valid answer.

Example 2:
Input: deliveryDistance = [3, 6, 14], maxDays = 8
Output: 3
Explanation:
With X = 4, the total days are 1 + 2 + 4 = 7, which fits within 8.
With X = 3, the total is 1 + 2 + 5 = 8, so 3 is also valid.
Therefore the correct answer is 3.

Note:
The answer is guaranteed to exist because choosing X equal to the maximum distance
makes each van finish in exactly 1 day, so the total number of days equals the number
of vans, which is at most maxDays.
*/

public class Solution {

    /**
     * Finds the minimum positive integer daily charge budget X such that
     * the total number of days required by all vans is at most maxDays.
     *
     * Core idea:
     * - If X is small, each van needs more days.
     * - If X is large, each van needs fewer days.
     * - This creates a monotonic condition:
     *   if some X works, then any larger X also works.
     * - Monotonic conditions are ideal for binary search.
     *
     * @param deliveryDistance array where deliveryDistance[i] is the total distance van i must complete
     * @param maxDays the maximum allowed total days across all vans
     * @return the smallest integer X that makes the total required days <= maxDays
     * Time complexity: O(n log M), where n is the number of vans and M is the maximum distance
     * Space complexity: O(1), ignoring input storage
     */
    public int minimumDailyCharge(int[] deliveryDistance, long maxDays) {
        // The smallest possible daily charge budget is 1,
        // because X must be a positive integer.
        int left = 1;

        // The largest necessary daily charge budget is the maximum distance in the array.
        // Why?
        // If X equals the maximum distance, then every van finishes in exactly 1 day,
        // because each van's distance is <= X.
        int right = findMax(deliveryDistance);

        // We will store the best valid answer found so far.
        // Since the problem guarantees an answer exists, this will definitely be updated.
        int answer = right;

        // Standard binary search on the answer space [left, right].
        while (left <= right) {
            // Compute middle carefully to avoid overflow.
            int mid = left + (right - left) / 2;

            // Calculate how many total days are needed if daily charge budget is mid.
            long requiredDays = calculateTotalDays(deliveryDistance, mid, maxDays);

            // If requiredDays <= maxDays, then mid is a valid answer.
            // But we want the minimum valid answer, so we try smaller values too.
            if (requiredDays <= maxDays) {
                answer = mid;
                right = mid - 1;
            } else {
                // If requiredDays > maxDays, then mid is too small.
                // We must increase X to reduce the number of days.
                left = mid + 1;
            }
        }

        return answer;
    }

    /**
     * Computes the total number of days needed for all vans if the daily charge budget is x.
     *
     * Important formula:
     * ceil(distance / x) can be computed using integer arithmetic as:
     * (distance + x - 1) / x
     *
     * We also use an early exit optimization:
     * - If the running total already exceeds maxDaysLimit, we can stop immediately,
     *   because we only care whether the total is within the limit or not.
     *
     * @param deliveryDistance array of van distances
     * @param x candidate daily charge budget
     * @param maxDaysLimit upper limit used for early stopping
     * @return total days required for all vans using daily charge budget x
     * Time complexity: O(n) in the worst case
     * Space complexity: O(1)
     */
    public long calculateTotalDays(int[] deliveryDistance, int x, long maxDaysLimit) {
        long totalDays = 0L;

        // Process each van independently and add its required days.
        for (int distance : deliveryDistance) {
            // Compute ceil(distance / x) without floating point arithmetic.
            totalDays += (distance + (long) x - 1) / x;

            // Early stopping:
            // As soon as totalDays exceeds the allowed limit, we can return immediately.
            // This can save time on large inputs.
            if (totalDays > maxDaysLimit) {
                return totalDays;
            }
        }

        return totalDays;
    }

    /**
     * Finds the maximum value in the deliveryDistance array.
     *
     * This is used as the upper bound for binary search.
     *
     * @param deliveryDistance array of van distances
     * @return the maximum distance in the array
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public int findMax(int[] deliveryDistance) {
        int max = 0;

        for (int distance : deliveryDistance) {
            if (distance > max) {
                max = distance;
            }
        }

        return max;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * Also includes a small trace-friendly output so beginners can verify results easily.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding method calls
     * Space complexity: O(1)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] deliveryDistance1 = {12, 7, 25, 9};
        long maxDays1 = 10;
        int result1 = solution.minimumDailyCharge(deliveryDistance1, maxDays1);
        System.out.println("Sample 1:");
        System.out.println("deliveryDistance = " + Arrays.toString(deliveryDistance1));
        System.out.println("maxDays = " + maxDays1);
        System.out.println("Minimum daily charge budget X = " + result1);
        System.out.println("Expected = 7");
        System.out.println();

        // Sample 2
        int[] deliveryDistance2 = {3, 6, 14};
        long maxDays2 = 8;
        int result2 = solution.minimumDailyCharge(deliveryDistance2, maxDays2);
        System.out.println("Sample 2:");
        System.out.println("deliveryDistance = " + Arrays.toString(deliveryDistance2));
        System.out.println("maxDays = " + maxDays2);
        System.out.println("Minimum daily charge budget X = " + result2);
        System.out.println("Expected = 3");
        System.out.println();

        // Additional quick sanity check
        int[] deliveryDistance3 = {1, 1, 1, 1};
        long maxDays3 = 4;
        int result3 = solution.minimumDailyCharge(deliveryDistance3, maxDays3);
        System.out.println("Additional Test:");
        System.out.println("deliveryDistance = " + Arrays.toString(deliveryDistance3));
        System.out.println("maxDays = " + maxDays3);
        System.out.println("Minimum daily charge budget X = " + result3);
        System.out.println("Expected = 1");
    }
}