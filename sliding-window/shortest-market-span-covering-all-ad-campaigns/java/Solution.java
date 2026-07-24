import java.util.*;

/*
 * Title: Shortest Market Span Covering All Ad Campaigns
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given a chronological stream of website visits represented by an array visits,
 * where visits[i] is the campaign ID that influenced the i-th visit. You are also given
 * an integer array required of length m, where required[c] indicates how many visits
 * influenced by campaign c must appear inside a valid analytics window. Campaign IDs in
 * visits are in the range [0, m - 1].
 *
 * Your task is to find the length of the shortest contiguous subarray of visits that
 * satisfies all campaign requirements simultaneously. In other words, for every campaign c,
 * the chosen window must contain campaign c at least required[c] times. If no such window
 * exists, return -1.
 *
 * This is harder than a standard minimum-cover problem because some campaigns may require
 * multiple occurrences, some may require zero occurrences, and the input size is large
 * enough that brute force enumeration of all subarrays will time out. An efficient sliding
 * window solution is expected.
 *
 * Return the minimum possible window length.
 *
 * Constraints:
 * - 1 <= visits.length <= 200000
 * - 1 <= m <= 100000
 * - 0 <= visits[i] < m
 * - 0 <= required[c] <= visits.length
 * - The sum of required values can be larger than visits.length
 *
 * Example 1:
 * Input: visits = [2,0,1,2,0,1,2,1], required = [1,2,2]
 * Output: 5
 * Explanation: Campaign 0 must appear at least once, campaign 1 at least twice, and
 * campaign 2 at least twice. The shortest valid window is [1,2,0,1,2], which has length 5.
 *
 * Example 2:
 * Input: visits = [3,1,3,2,1,0,2,3], required = [1,1,2,1]
 * Output: 6
 * Explanation: We need at least one 0, one 1, two 2s, and one 3. The shortest valid
 * window is [3,2,1,0,2,3], which has length 6. No shorter contiguous span contains all
 * required counts.
 *
 * If the required counts cannot be satisfied by the entire array, return -1.
 */

public class Solution {

    /**
     * Finds the length of the shortest contiguous subarray that contains each campaign
     * at least the required number of times.
     *
     * Core idea:
     * 1. First verify that the entire visits array contains enough occurrences of every
     *    campaign. If not, return -1 immediately.
     * 2. Then use a classic sliding window:
     *    - Expand the right boundary to include more visits.
     *    - Track how many campaigns are currently "satisfied", meaning the window count
     *      for that campaign has reached its required count.
     *    - Once all required campaigns are satisfied, try to shrink the window from the
     *      left while keeping it valid.
     *    - Record the minimum valid window length seen.
     *
     * Important detail:
     * Campaigns with required count 0 are already satisfied from the beginning and do not
     * need to appear in the window at all.
     *
     * @param visits chronological array of campaign IDs for each visit
     * @param required required[c] is the minimum number of times campaign c must appear
     *                 in a valid window
     * @return the minimum length of a valid contiguous subarray, or -1 if impossible
     * Time complexity: O(n + m), where n = visits.length and m = required.length
     * Space complexity: O(m)
     */
    public int shortestMarketSpan(int[] visits, int[] required) {
        int n = visits.length;
        int m = required.length;

        // Step 1:
        // Count how many times each campaign appears in the entire visits array.
        // This lets us quickly detect impossible cases before running the sliding window.
        int[] totalCount = new int[m];
        for (int campaign : visits) {
            totalCount[campaign]++;
        }

        // Step 2:
        // Check if every campaign requirement can be satisfied at all.
        // If the full array does not contain enough of some campaign, then no subarray can.
        for (int c = 0; c < m; c++) {
            if (totalCount[c] < required[c]) {
                return -1;
            }
        }

        // Step 3:
        // Count how many campaigns actually matter.
        // A campaign with required[c] > 0 must eventually become satisfied in the window.
        int campaignsNeeded = 0;
        for (int c = 0; c < m; c++) {
            if (required[c] > 0) {
                campaignsNeeded++;
            }
        }

        // Special case:
        // If no campaign requires anything, then the empty requirement is already satisfied.
        // The shortest contiguous subarray length is 0.
        if (campaignsNeeded == 0) {
            return 0;
        }

        // Step 4:
        // Sliding window state:
        // windowCount[c] = how many times campaign c appears in the current window [left, right]
        int[] windowCount = new int[m];

        // satisfiedCampaigns = number of campaigns c with required[c] > 0
        // such that windowCount[c] >= required[c]
        int satisfiedCampaigns = 0;

        int left = 0;
        int minLength = Integer.MAX_VALUE;

        // Step 5:
        // Expand the window by moving 'right' from left to right across the array.
        for (int right = 0; right < n; right++) {
            int campaignAtRight = visits[right];

            // Include visits[right] into the current window.
            windowCount[campaignAtRight]++;

            // If this campaign has a positive requirement and we have JUST reached
            // exactly the required count, then this campaign becomes newly satisfied.
            //
            // We use == here, not >=, because we only want to count the transition
            // from "not satisfied" to "satisfied" once.
            if (required[campaignAtRight] > 0
                    && windowCount[campaignAtRight] == required[campaignAtRight]) {
                satisfiedCampaigns++;
            }

            // Step 6:
            // If all required campaigns are satisfied, the current window is valid.
            // Now try to shrink it from the left as much as possible while keeping it valid.
            while (satisfiedCampaigns == campaignsNeeded) {
                // Current window [left, right] is valid, so update the answer.
                int currentLength = right - left + 1;
                if (currentLength < minLength) {
                    minLength = currentLength;
                }

                int campaignAtLeft = visits[left];

                // We are about to remove visits[left] from the window.
                windowCount[campaignAtLeft]--;

                // If this campaign had a positive requirement and after removing it
                // the count drops BELOW the required threshold, then the window is
                // no longer valid for that campaign.
                //
                // We check for required[campaignAtLeft] - 1 because we already decremented.
                if (required[campaignAtLeft] > 0
                        && windowCount[campaignAtLeft] == required[campaignAtLeft] - 1) {
                    satisfiedCampaigns--;
                }

                // Move left boundary rightward to continue shrinking.
                left++;
            }
        }

        // Since we already ruled out impossible cases, minLength must have been updated.
        return minLength;
    }

    /**
     * Helper method to print an integer array in a beginner-friendly format.
     *
     * @param arr the array to print
     * @return a string representation such as [1, 2, 3]
     * Time complexity: O(k), where k = arr.length
     * Space complexity: O(k) for the produced string
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo cases, excluding the called algorithm
     * Space complexity: O(1), excluding the called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] visits1 = {2, 0, 1, 2, 0, 1, 2, 1};
        int[] required1 = {1, 2, 2};
        int result1 = solution.shortestMarketSpan(visits1, required1);

        System.out.println("Example 1");
        System.out.println("visits   = " + solution.arrayToString(visits1));
        System.out.println("required = " + solution.arrayToString(required1));
        System.out.println("Output   = " + result1);
        System.out.println("Expected = 5");
        System.out.println();

        // Example 2
        int[] visits2 = {3, 1, 3, 2, 1, 0, 2, 3};
        int[] required2 = {1, 1, 2, 1};
        int result2 = solution.shortestMarketSpan(visits2, required2);

        System.out.println("Example 2");
        System.out.println("visits   = " + solution.arrayToString(visits2));
        System.out.println("required = " + solution.arrayToString(required2));
        System.out.println("Output   = " + result2);
        System.out.println("Expected = 6");
        System.out.println();

        // Additional demonstration: impossible case
        int[] visits3 = {0, 1, 1, 2};
        int[] required3 = {1, 2, 2};
        int result3 = solution.shortestMarketSpan(visits3, required3);

        System.out.println("Additional Example 3 (Impossible)");
        System.out.println("visits   = " + solution.arrayToString(visits3));
        System.out.println("required = " + solution.arrayToString(required3));
        System.out.println("Output   = " + result3);
        System.out.println("Expected = -1");
        System.out.println();

        // Additional demonstration: all requirements are zero
        int[] visits4 = {1, 2, 3};
        int[] required4 = {0, 0, 0, 0};
        int result4 = solution.shortestMarketSpan(visits4, required4);

        System.out.println("Additional Example 4 (Zero Requirements)");
        System.out.println("visits   = " + solution.arrayToString(visits4));
        System.out.println("required = " + solution.arrayToString(required4));
        System.out.println("Output   = " + result4);
        System.out.println("Expected = 0");
    }
}