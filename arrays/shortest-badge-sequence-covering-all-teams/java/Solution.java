import java.util.*;

/*
 * Title: Shortest Badge Sequence Covering All Teams
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * A company records the order of employee badge scans during a large conference.
 * Each scan is represented by an integer team ID in the array scans, where scans[i]
 * is the team of the i-th person who entered a room. You are also given an integer m
 * representing the total number of distinct teams, labeled from 1 to m.
 *
 * Find the length of the shortest contiguous subarray of scans that contains at least
 * one badge scan from every team 1 through m. If no such subarray exists, return -1.
 *
 * This problem models finding the smallest time window in which all required groups
 * were represented. The array may contain repeated team IDs, and some teams may appear
 * many times while others appear rarely. Your solution should be efficient enough for
 * large inputs.
 *
 * Return only the minimum length, not the subarray itself.
 *
 * Constraints:
 * - 1 <= scans.length <= 200000
 * - 1 <= m <= 100000
 * - 1 <= scans[i] <= m
 * - The input array may be unsorted
 * - If one or more team IDs from 1 to m never appear in scans, the answer is -1
 *
 * Example 1:
 * Input: scans = [2, 1, 3, 2, 4, 1, 3], m = 4
 * Output: 4
 * Explanation: The shortest valid subarray is [1, 3, 2, 4], which contains teams 1, 2, 3, and 4.
 *
 * Example 2:
 * Input: scans = [1, 2, 2, 1, 3], m = 4
 * Output: -1
 * Explanation: Team 4 never appears, so it is impossible to form a contiguous subarray containing all teams.
 */

public class Solution {

    /**
     * Finds the length of the shortest contiguous subarray that contains
     * every team ID from 1 through m at least once.
     *
     * The algorithm uses the classic sliding window technique:
     * 1. Expand the right boundary to include more scans.
     * 2. Track how many distinct required teams are currently covered.
     * 3. Once all m teams are covered, try to shrink the left boundary
     *    as much as possible while still keeping all teams in the window.
     * 4. Record the minimum valid window length seen.
     *
     * @param scans the array of badge scans, where each value is a team ID
     * @param m the total number of required teams, labeled from 1 to m
     * @return the minimum length of a contiguous subarray containing all teams,
     *         or -1 if no such subarray exists
     *
     * Time complexity: O(n), where n is scans.length, because each index is visited
     * at most a constant number of times by the sliding window pointers.
     * Space complexity: O(m), for the frequency array used to count team occurrences
     * inside the current window.
     */
    public int shortestBadgeSequence(int[] scans, int m) {
        if (scans == null || scans.length == 0 || m <= 0) {
            return -1;
        }

        int n = scans.length;

        /*
         * Frequency array:
         * count[team] tells us how many times that team currently appears
         * inside the sliding window [left, right].
         *
         * We allocate size m + 1 so that team IDs 1..m can be used directly
         * as indices, making the code simpler and beginner-friendly.
         */
        int[] count = new int[m + 1];

        /*
         * coveredTeams:
         * Number of distinct teams currently present in the window.
         *
         * Important:
         * We only increase coveredTeams when a team's count changes from 0 to 1,
         * meaning that team has just become represented in the current window.
         */
        int coveredTeams = 0;

        /*
         * left:
         * Left boundary of the sliding window.
         */
        int left = 0;

        /*
         * minLength:
         * Best answer found so far.
         * We start with a very large value and later check whether it was updated.
         */
        int minLength = Integer.MAX_VALUE;

        /*
         * Move the right boundary from left to right across the array.
         * At each step, we include scans[right] into the current window.
         */
        for (int right = 0; right < n; right++) {
            int teamAtRight = scans[right];

            /*
             * Add the current team into the window.
             * If its previous count was 0, this team is newly covered.
             */
            if (count[teamAtRight] == 0) {
                coveredTeams++;
            }
            count[teamAtRight]++;

            /*
             * If coveredTeams == m, then the current window [left, right]
             * contains every required team at least once.
             *
             * Now we try to shrink the window from the left to make it as short
             * as possible while still remaining valid.
             */
            while (coveredTeams == m) {
                /*
                 * Current valid window length.
                 */
                int currentLength = right - left + 1;

                /*
                 * Update the best answer if this window is smaller.
                 */
                if (currentLength < minLength) {
                    minLength = currentLength;
                }

                /*
                 * We now attempt to remove scans[left] from the window.
                 * This is the key "shrink" step of the sliding window.
                 */
                int teamAtLeft = scans[left];
                count[teamAtLeft]--;

                /*
                 * If after decrementing, the count becomes 0, then removing this
                 * element caused that team to disappear from the window.
                 * Therefore, the window is no longer valid and coveredTeams decreases.
                 */
                if (count[teamAtLeft] == 0) {
                    coveredTeams--;
                }

                /*
                 * Move the left boundary one step to the right.
                 */
                left++;
            }
        }

        /*
         * If minLength was never updated, then no valid window existed,
         * which means at least one team from 1..m never appeared in scans.
         */
        return minLength == Integer.MAX_VALUE ? -1 : minLength;
    }

    /**
     * A helper method that runs the algorithm on a given input and prints
     * the result in a readable format.
     *
     * @param scans the badge scan array to test
     * @param m the total number of required teams
     * @return the computed shortest valid subarray length
     *
     * Time complexity: O(n), where n is scans.length.
     * Space complexity: O(m).
     */
    public int demonstrateCase(int[] scans, int m) {
        int result = shortestBadgeSequence(scans, m);
        System.out.println("scans = " + Arrays.toString(scans));
        System.out.println("m = " + m);
        System.out.println("Shortest length covering all teams = " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method demonstrating the solution on the sample inputs
     * from the problem statement, plus a few additional examples.
     *
     * Example 1 verification:
     * scans = [2, 1, 3, 2, 4, 1, 3], m = 4
     * Shortest valid subarray length is 4.
     *
     * Example 2 verification:
     * scans = [1, 2, 2, 1, 3], m = 4
     * Team 4 never appears, so answer is -1.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: Depends on the number and size of demonstration cases.
     * Space complexity: O(m) per test case due to the frequency array.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] scans1 = {2, 1, 3, 2, 4, 1, 3};
        int m1 = 4;
        solution.demonstrateCase(scans1, m1); // Expected: 4

        int[] scans2 = {1, 2, 2, 1, 3};
        int m2 = 4;
        solution.demonstrateCase(scans2, m2); // Expected: -1

        int[] scans3 = {1, 2, 3, 4};
        int m3 = 4;
        solution.demonstrateCase(scans3, m3); // Expected: 4

        int[] scans4 = {1, 1, 1, 2, 3, 4, 4};
        int m4 = 4;
        solution.demonstrateCase(scans4, m4); // Expected: 4

        int[] scans5 = {4, 3, 2, 1, 2, 3, 4};
        int m5 = 4;
        solution.demonstrateCase(scans5, m5); // Expected: 4
    }
}