import java.util.*;

/*
Problem Title: Minimum Daily API Quota for Sequential Data Exports

Problem Description:
A company needs to export analytics data for n clients in the given order. The i-th client requires
requests[i] API calls to finish its export. Because of compliance rules, exports cannot be split
across days out of order: each day you process a contiguous block of clients, and once you move to
the next day, you cannot go back to an earlier client. You are also not allowed to split one client's
export across multiple days, so the daily quota must be at least as large as the largest single value
in requests.

Given an integer array requests and an integer d, return the minimum daily API quota needed to finish
all exports within at most d days.

This is a decision-and-search problem: for any proposed daily quota Q, you can check whether all exports
can be completed in at most d days while preserving order. Your task is to find the smallest feasible Q.

Constraints:
- 1 <= requests.length <= 100000
- 1 <= requests[i] <= 1000000000
- 1 <= d <= requests.length

Example 1:
Input: requests = [7, 2, 5, 10, 8], d = 2
Output: 18
Explanation: With quota 18, one valid split is [7, 2, 5] and [10, 8]. A quota of 17 is not enough
because preserving order would require 3 days.

Example 2:
Input: requests = [4, 4, 4, 4, 4], d = 3
Output: 8
Explanation: Using quota 8, the exports can be grouped as [4, 4], [4, 4], [4]. Any quota below 8
would force at least 4 days.
*/

public class Solution {

    /**
     * Finds the minimum daily API quota required to complete all exports within at most d days.
     *
     * The key idea is:
     * 1. If we guess a daily quota, we can greedily simulate how many days are needed.
     * 2. If that quota is enough to finish within d days, then any larger quota is also enough.
     * 3. This monotonic behavior allows us to use binary search on the answer.
     *
     * Search range:
     * - Lower bound = maximum single request value, because one client's export cannot be split.
     * - Upper bound = sum of all requests, because in the worst case we do everything in one day.
     *
     * @param requests the array where requests[i] is the number of API calls needed for client i
     * @param d the maximum number of days allowed
     * @return the smallest daily quota that allows all exports to finish within at most d days
     * Time complexity: O(n log S), where n is requests.length and S is the search range of possible quotas
     * Space complexity: O(1), excluding input storage
     */
    public long minimumDailyQuota(int[] requests, int d) {
        // The minimum possible quota must be at least the largest single request,
        // because one client's export cannot be split across multiple days.
        long left = 0;

        // The maximum possible quota is the total sum of all requests,
        // which would allow all exports to be completed in a single day.
        long right = 0;

        // Compute both bounds in one pass:
        // - left becomes the maximum element
        // - right becomes the total sum
        for (int request : requests) {
            left = Math.max(left, request);
            right += request;
        }

        // Binary search for the smallest feasible quota.
        // Invariant:
        // - Any quota < answer is infeasible
        // - Any quota >= answer may be feasible
        while (left < right) {
            // Use this form to avoid overflow:
            // mid = left + (right - left) / 2
            long mid = left + (right - left) / 2;

            // Check whether this candidate quota is enough.
            if (canFinishWithinDays(requests, d, mid)) {
                // If mid works, try to find an even smaller feasible quota.
                right = mid;
            } else {
                // If mid does not work, all smaller quotas also do not work.
                // So we must search in the larger half.
                left = mid + 1;
            }
        }

        // At the end of binary search, left == right and points to the minimum feasible quota.
        return left;
    }

    /**
     * Determines whether all exports can be completed within at most d days
     * using the given daily quota while preserving order.
     *
     * Greedy simulation:
     * - Process clients from left to right.
     * - Keep adding the current client's requests to the current day as long as the quota is not exceeded.
     * - If adding the next client would exceed the quota, start a new day and place that client there.
     *
     * Why greedy works:
     * - Since order must be preserved and each day must contain a contiguous block,
     *   the best way to minimize the number of days for a fixed quota is to pack each day
     *   as much as possible before moving to the next day.
     *
     * @param requests the array of API calls required per client
     * @param d the maximum number of days allowed
     * @param quota the proposed daily API quota to test
     * @return true if all exports can be completed within at most d days, otherwise false
     * Time complexity: O(n), where n is requests.length
     * Space complexity: O(1)
     */
    public boolean canFinishWithinDays(int[] requests, int d, long quota) {
        // Start with day 1 because if there is at least one client,
        // we need at least one day to process something.
        int daysUsed = 1;

        // Tracks how much of the current day's quota has already been consumed.
        long currentDayLoad = 0;

        // Process clients in the required order.
        for (int request : requests) {
            // Safety check:
            // If a single client's request exceeds the quota, it is impossible,
            // because splitting one client across days is not allowed.
            if (request > quota) {
                return false;
            }

            // If adding this client to the current day stays within quota,
            // keep it in the current day's contiguous block.
            if (currentDayLoad + request <= quota) {
                currentDayLoad += request;
            } else {
                // Otherwise, we must start a new day for this client.
                daysUsed++;
                currentDayLoad = request;

                // Early exit:
                // If we already need more than d days, this quota is not feasible.
                if (daysUsed > d) {
                    return false;
                }
            }
        }

        // If we finished processing all clients without exceeding d days,
        // then the quota is feasible.
        return true;
    }

    /**
     * Helper method to print an integer array in a readable format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n) due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It prints:
     * - the input array
     * - the number of days
     * - the computed minimum daily quota
     *
     * Expected outputs:
     * - Example 1: 18
     * - Example 2: 8
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log S) across the demonstrated examples
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] requests1 = {7, 2, 5, 10, 8};
        int d1 = 2;
        long result1 = solution.minimumDailyQuota(requests1, d1);
        System.out.println("Example 1");
        System.out.println("requests = " + solution.arrayToString(requests1));
        System.out.println("d = " + d1);
        System.out.println("Minimum daily API quota = " + result1);
        System.out.println("Expected = 18");
        System.out.println();

        int[] requests2 = {4, 4, 4, 4, 4};
        int d2 = 3;
        long result2 = solution.minimumDailyQuota(requests2, d2);
        System.out.println("Example 2");
        System.out.println("requests = " + solution.arrayToString(requests2));
        System.out.println("d = " + d2);
        System.out.println("Minimum daily API quota = " + result2);
        System.out.println("Expected = 8");
        System.out.println();

        // Additional quick sanity checks for beginners:
        int[] requests3 = {1};
        int d3 = 1;
        long result3 = solution.minimumDailyQuota(requests3, d3);
        System.out.println("Additional Test 1");
        System.out.println("requests = " + solution.arrayToString(requests3));
        System.out.println("d = " + d3);
        System.out.println("Minimum daily API quota = " + result3);
        System.out.println("Expected = 1");
        System.out.println();

        int[] requests4 = {3, 2, 2, 4, 1, 4};
        int d4 = 3;
        long result4 = solution.minimumDailyQuota(requests4, d4);
        System.out.println("Additional Test 2");
        System.out.println("requests = " + solution.arrayToString(requests4));
        System.out.println("d = " + d4);
        System.out.println("Minimum daily API quota = " + result4);
        System.out.println("Expected = 6");
    }
}