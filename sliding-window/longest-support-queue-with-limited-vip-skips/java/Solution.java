import java.util.*;

/*
 * Title: Longest Support Queue With Limited VIP Skips
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A customer support system records the arrival order of tickets in an array tickets,
 * where tickets[i] is 0 for a regular ticket and 1 for a VIP ticket.
 * The support team wants to analyze contiguous portions of the queue that can be handled
 * by one standard agent.
 *
 * A standard agent can process any number of regular tickets, but can tolerate at most k
 * VIP tickets in the assigned contiguous segment by forwarding those VIP tickets to a specialist.
 * Your task is to find the length of the longest contiguous subarray of tickets that contains
 * at most k VIP tickets.
 *
 * Return the maximum possible length of such a segment.
 *
 * This models a realistic queue-monitoring problem where a team wants the longest uninterrupted
 * block of work that stays within a forwarding limit.
 *
 * Constraints:
 * - 1 <= tickets.length <= 100000
 * - tickets[i] is either 0 or 1
 * - 0 <= k <= tickets.length
 *
 * Example 1:
 * Input: tickets = [0,1,0,0,1,0,0,0], k = 1
 * Output: 5
 * Explanation: The longest valid segment contains at most one VIP ticket, and the maximum length is 5.
 *
 * Example 2:
 * Input: tickets = [1,0,1,0,0,1,0], k = 2
 * Output: 6
 * Explanation: A longest valid segment is [0,1,0,0,1,0], which has exactly 2 VIP tickets and length 6.
 */

/**
 * A beginner-friendly solution class for finding the longest contiguous segment
 * containing at most k VIP tickets.
 */
public class Solution {

    /**
     * Finds the maximum length of a contiguous subarray that contains at most k ones.
     *
     * The idea is to use the sliding window technique:
     * - Expand the right end of the window one step at a time.
     * - Count how many VIP tickets (1s) are inside the current window.
     * - If the window becomes invalid (more than k VIP tickets), move the left end
     *   forward until the window becomes valid again.
     * - Track the largest valid window length seen during the process.
     *
     * @param tickets the array representing the support queue, where 0 = regular ticket and 1 = VIP ticket
     * @param k the maximum number of VIP tickets allowed in a valid contiguous segment
     * @return the length of the longest contiguous subarray containing at most k VIP tickets
     *
     * Time complexity: O(n), where n is the length of the tickets array,
     * because each pointer moves at most n times.
     * Space complexity: O(1), because only a few extra variables are used.
     */
    public int longestSupportQueue(int[] tickets, int k) {
        // Left boundary of the current sliding window.
        int left = 0;

        // Number of VIP tickets (value 1) currently inside the window [left, right].
        int vipCount = 0;

        // Best answer found so far.
        int maxLength = 0;

        // Move the right boundary from the start of the array to the end.
        for (int right = 0; right < tickets.length; right++) {
            // Step 1:
            // Include tickets[right] in the current window.
            // If it is a VIP ticket, increase the VIP count.
            if (tickets[right] == 1) {
                vipCount++;
            }

            // Step 2:
            // If the window now has too many VIP tickets, it is invalid.
            // We must shrink the window from the left until it becomes valid again.
            while (vipCount > k) {
                // Before moving left forward, check whether the element leaving the window
                // is a VIP ticket. If yes, decrease vipCount because that VIP is no longer inside.
                if (tickets[left] == 1) {
                    vipCount--;
                }

                // Move the left boundary one step to the right.
                left++;
            }

            // Step 3:
            // At this point, the window [left, right] is guaranteed to be valid
            // because vipCount <= k.
            // Compute its length.
            int currentLength = right - left + 1;

            // Step 4:
            // Update the best answer if this valid window is larger than anything seen before.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // After scanning the entire array, maxLength holds the answer.
        return maxLength;
    }

    /**
     * A small helper method to print an array in a readable format.
     *
     * @param array the integer array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is the length of the array.
     * Space complexity: O(n), due to building the output string.
     */
    public String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstration call, where n is the input size.
     * Space complexity: O(1) extra space for the algorithm itself.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] tickets1 = {0, 1, 0, 0, 1, 0, 0, 0};
        int k1 = 1;
        int result1 = solution.longestSupportQueue(tickets1, k1);

        System.out.println("Sample 1:");
        System.out.println("tickets = " + solution.arrayToString(tickets1));
        System.out.println("k = " + k1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 5");
        System.out.println();

        // Sample 2
        int[] tickets2 = {1, 0, 1, 0, 0, 1, 0};
        int k2 = 2;
        int result2 = solution.longestSupportQueue(tickets2, k2);

        System.out.println("Sample 2:");
        System.out.println("tickets = " + solution.arrayToString(tickets2));
        System.out.println("k = " + k2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 6");
        System.out.println();

        // Additional quick checks for beginners
        int[] tickets3 = {0, 0, 0, 0};
        int k3 = 0;
        System.out.println("Additional Check 1:");
        System.out.println("tickets = " + solution.arrayToString(tickets3));
        System.out.println("k = " + k3);
        System.out.println("Output = " + solution.longestSupportQueue(tickets3, k3));
        System.out.println("Expected = 4");
        System.out.println();

        int[] tickets4 = {1, 1, 1, 1};
        int k4 = 2;
        System.out.println("Additional Check 2:");
        System.out.println("tickets = " + solution.arrayToString(tickets4));
        System.out.println("k = " + k4);
        System.out.println("Output = " + solution.longestSupportQueue(tickets4, k4));
        System.out.println("Expected = 2");
    }
}