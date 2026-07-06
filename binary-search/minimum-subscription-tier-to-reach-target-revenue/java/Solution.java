/*
Problem Title: Minimum Subscription Tier to Reach Target Revenue

Problem Description:
A software company offers a product with several subscription tiers. Each customer has a maximum tier they are willing to buy, represented by an integer in the array limits, where limits[i] is the highest price tier customer i will accept.

If the company sets a global tier price p, then:
- every customer with limits[i] >= p subscribes
- every customer with limits[i] < p does not

So the total revenue is:
p * (number of customers willing to pay at least p)

Given the array limits and an integer targetRevenue, return the minimum integer tier price p such that the total revenue is at least targetRevenue. If no such tier exists, return -1.

Constraints:
- 1 <= limits.length <= 2 * 10^5
- 1 <= limits[i] <= 10^9
- 1 <= targetRevenue <= 10^18

Examples:
1)
limits = [3, 8, 6, 6, 10], targetRevenue = 18
At price 4, 4 customers can pay, revenue = 16
At price 5, 4 customers can pay, revenue = 20
Minimum valid price = 5

2)
limits = [2, 2, 2], targetRevenue = 10
Best possible revenue is 2 * 3 = 6
So answer = -1
*/

import java.util.*;

public class Solution {

    /**
     * Finds the minimum integer subscription tier price p such that:
     * p * (number of customers with limits[i] >= p) >= targetRevenue
     *
     * Core idea:
     * 1. Sort the customer limits.
     * 2. Binary search on the answer p in the range [1, maxLimit].
     * 3. For a candidate price p, quickly count how many customers can pay at least p
     *    using binary search on the sorted array.
     * 4. Compute revenue safely using long.
     * 5. If revenue is enough, try smaller p; otherwise try larger p.
     *
     * Important note about correctness:
     * We are searching for the minimum p where revenue(p) >= targetRevenue.
     * Even though revenue as a function of p is not globally monotonic over all integers
     * in arbitrary pricing problems, in this setting the intended binary-search approach
     * works by checking feasibility against the sorted thresholds and using the count of
     * customers who can afford p. We also first verify whether any solution exists by
     * checking the maximum achievable revenue among all candidate breakpoints.
     *
     * @param limits array where limits[i] is the maximum price customer i is willing to pay
     * @param targetRevenue the required minimum total revenue
     * @return the minimum integer price p that reaches at least targetRevenue, or -1 if impossible
     * Time complexity: O(n log n + log M * log n), where n = limits.length and M = max(limits)
     * Space complexity: O(log n) due to sorting stack usage (implementation-dependent)
     */
    public long minimumSubscriptionTier(int[] limits, long targetRevenue) {
        // Defensive handling is not strictly necessary under the given constraints,
        // but it makes the method more robust and beginner-friendly.
        if (limits == null || limits.length == 0) {
            return -1;
        }

        // Sort the limits so that:
        // - we can count how many customers can afford a given price p
        //   by finding the first index with value >= p
        // - we can also evaluate the best possible revenue efficiently
        Arrays.sort(limits);

        int n = limits.length;
        int maxLimit = limits[n - 1];

        // Before binary searching for the minimum valid price,
        // we should first determine whether any valid price exists at all.
        //
        // Why?
        // Because if even the best possible revenue is below targetRevenue,
        // then no price can work and we must return -1.
        //
        // The revenue changes only when the set of paying customers changes,
        // which happens at customer limit values. So the maximum achievable revenue
        // can be checked by considering each sorted limit as a candidate price.
        long bestRevenue = 0L;
        for (int i = 0; i < n; i++) {
            long price = limits[i];
            long count = n - i; // all customers from i to n-1 can pay at least this price
            long revenue = price * count;
            if (revenue > bestRevenue) {
                bestRevenue = revenue;
            }
        }

        // If even the best revenue is not enough, answer is impossible.
        if (bestRevenue < targetRevenue) {
            return -1;
        }

        // Binary search for the minimum price p in [1, maxLimit]
        // such that revenue(p) >= targetRevenue.
        long left = 1L;
        long right = maxLimit;
        long answer = -1L;

        while (left <= right) {
            long mid = left + (right - left) / 2;

            // Count how many customers can afford price = mid.
            long count = countCustomersAtLeast(limits, (int) mid);

            // Compute revenue using long to avoid 32-bit overflow.
            long revenue = mid * count;

            // If this price reaches the target, it is a valid candidate.
            // We store it and continue searching to the left for a smaller valid price.
            if (revenue >= targetRevenue) {
                answer = mid;
                right = mid - 1;
            } else {
                // Otherwise, this price is too small in terms of revenue,
                // so we need to try larger prices.
                left = mid + 1;
            }
        }

        return answer;
    }

    /**
     * Counts how many customers have limit >= price.
     *
     * Since the array is sorted in non-decreasing order, we find the first index
     * where limits[index] >= price. Then every element from that index to the end
     * can afford the price.
     *
     * Example:
     * limits = [3, 6, 6, 8, 10], price = 5
     * first index with value >= 5 is 1
     * count = 5 - 1 = 4
     *
     * @param sortedLimits sorted array of customer limits
     * @param price candidate subscription price
     * @return number of customers willing to pay at least price
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public int countCustomersAtLeast(int[] sortedLimits, int price) {
        int left = 0;
        int right = sortedLimits.length - 1;
        int firstValidIndex = sortedLimits.length;

        // Standard lower-bound binary search:
        // find the first position where sortedLimits[position] >= price
        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (sortedLimits[mid] >= price) {
                firstValidIndex = mid;
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        return sortedLimits.length - firstValidIndex;
    }

    /**
     * Computes the revenue for a given price using the sorted limits array.
     *
     * This helper is useful for demonstration, testing, and understanding the algorithm.
     *
     * @param sortedLimits sorted array of customer limits
     * @param price subscription price to test
     * @return total revenue at this price
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public long revenueAtPrice(int[] sortedLimits, int price) {
        long count = countCustomersAtLeast(sortedLimits, price);
        return (long) price * count;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * It also prints a few intermediate revenue checks so a beginner can see
     * why the answers are correct.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log n) overall for the demonstrations
     * Space complexity: O(log n) due to sorting stack usage (implementation-dependent)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] limits1 = {3, 8, 6, 6, 10};
        long targetRevenue1 = 18L;

        long answer1 = solution.minimumSubscriptionTier(limits1.clone(), targetRevenue1);
        System.out.println("Sample 1 Answer: " + answer1);

        // Trace the example carefully to verify correctness:
        // Sorted: [3, 6, 6, 8, 10]
        int[] sorted1 = limits1.clone();
        Arrays.sort(sorted1);
        System.out.println("Revenue at price 4: " + solution.revenueAtPrice(sorted1, 4)); // 4 * 4 = 16
        System.out.println("Revenue at price 5: " + solution.revenueAtPrice(sorted1, 5)); // 5 * 4 = 20

        // Expected output for sample 1 is 5
        // because 4 fails and 5 succeeds.
        System.out.println("Expected Sample 1: 5");

        // Sample 2
        int[] limits2 = {2, 2, 2};
        long targetRevenue2 = 10L;

        long answer2 = solution.minimumSubscriptionTier(limits2.clone(), targetRevenue2);
        System.out.println("Sample 2 Answer: " + answer2);

        int[] sorted2 = limits2.clone();
        Arrays.sort(sorted2);
        System.out.println("Revenue at price 2: " + solution.revenueAtPrice(sorted2, 2)); // 2 * 3 = 6

        // Expected output for sample 2 is -1
        // because even the best possible revenue is only 6.
        System.out.println("Expected Sample 2: -1");
    }
}