import java.util.*;

/*
Title: Minimum Cost to Curate a Museum Exhibit Path
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
A museum is preparing a guided exhibit path through a linear hallway of display rooms. The hallway has n rooms numbered from 0 to n - 1, and the guide must start in room 0 and finish in room n - 1. From room i, the guide may move to room i + 1 or skip ahead to room i + 2, as long as the destination room exists.

Each room i has a curation cost cost[i], representing the effort required to prepare that room for the tour if the guide enters it. However, some rooms are marked as premium rooms. If two premium rooms are visited consecutively in the tour path, an additional transition fee penalty is charged because the guide team must perform a special reset between them.

You are given:
- an integer array cost where cost[i] is the curation cost of room i,
- a binary array premium where premium[i] is 1 if room i is premium, otherwise 0,
- an integer penalty.

Return the minimum total cost to create a valid tour path from room 0 to room n - 1. The total cost includes the cost of every visited room, plus penalty for every consecutive pair of visited rooms in the chosen path where both rooms are premium.

A pair of visited rooms is consecutive if they are adjacent in the chosen path, even if their indices differ by 2 due to a skip.

Constraints:
- 2 <= n <= 100000
- cost.length == premium.length == n
- 0 <= cost[i] <= 100000
- premium[i] is either 0 or 1
- 0 <= penalty <= 100000

Example 1:
Input: cost = [4, 2, 7, 3, 5], premium = [1, 0, 1, 1, 0], penalty = 6
Output: 14
Explanation: One optimal path is 0 -> 1 -> 3 -> 4.
Visited room cost = 4 + 2 + 3 + 5 = 14.
Among consecutive visited pairs, (1,3) is not charged because room 1 is not premium, and (3,4) is not charged because room 4 is not premium. Total = 14.
The path 0 -> 2 -> 4 would cost 4 + 7 + 5 + 6 = 22 because rooms 0 and 2 are both premium, so the premium transition penalty applies once.

Example 2:
Input: cost = [1, 9, 2, 8, 1, 3], premium = [1, 1, 0, 1, 1, 1], penalty = 4
Output: 11
Explanation: An optimal path is 0 -> 2 -> 4 -> 5.
Visited room cost = 1 + 2 + 1 + 3 = 7.
The pair (4,5) contributes an additional penalty of 4 because both visited rooms are premium. Total = 11.
Other valid paths have greater total cost.

This problem is well-suited for dynamic programming. For each room, consider the minimum total cost to arrive there from the previous one or two rooms, while adding the destination room cost and possibly a premium-to-premium transition penalty based on the previous visited room.
*/

public class Solution {

    /**
     * Computes the minimum total curation cost for a valid exhibit path from room 0 to room n - 1.
     *
     * Dynamic programming idea:
     * - Let dp[i] be the minimum total cost required to reach room i.
     * - To reach room i, the previous visited room can only be:
     *   1) i - 1
     *   2) i - 2
     * - Therefore:
     *   dp[i] = cost[i] + min(
     *       dp[i - 1] + transitionPenalty(i - 1, i),
     *       dp[i - 2] + transitionPenalty(i - 2, i)
     *   )
     * - transitionPenalty(a, b) is penalty if premium[a] == 1 and premium[b] == 1, otherwise 0.
     *
     * @param cost the curation cost for each room; cost[i] is paid if room i is visited
     * @param premium binary array where premium[i] == 1 means room i is premium, otherwise 0
     * @param penalty extra fee charged when two consecutive visited rooms are both premium
     * @return the minimum total cost to start at room 0 and finish at room n - 1
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long minimumCost(int[] cost, int[] premium, int penalty) {
        validateInput(cost, premium);

        int n = cost.length;

        // dp[i] will store the minimum total cost to arrive at room i.
        long[] dp = new long[n];

        // Base case:
        // We must start at room 0, so the cost to be at room 0 is simply cost[0].
        dp[0] = cost[0];

        // Base case for room 1:
        // Since the guide starts at room 0, the only way to reach room 1 is directly from room 0.
        // So we pay:
        // - the best cost to be at room 0
        // - plus the cost of entering room 1
        // - plus penalty if both room 0 and room 1 are premium
        dp[1] = dp[0] + cost[1] + transitionPenalty(premium, 0, 1, penalty);

        // Fill the DP table from left to right.
        for (int i = 2; i < n; i++) {
            // Option 1: arrive from room i - 1
            // Total cost =
            //   minimum cost to reach i - 1
            // + cost of entering room i
            // + premium transition penalty if needed
            long fromPrev1 = dp[i - 1] + cost[i] + transitionPenalty(premium, i - 1, i, penalty);

            // Option 2: arrive from room i - 2
            // Total cost =
            //   minimum cost to reach i - 2
            // + cost of entering room i
            // + premium transition penalty if needed
            long fromPrev2 = dp[i - 2] + cost[i] + transitionPenalty(premium, i - 2, i, penalty);

            // Choose the cheaper of the two valid ways to reach room i.
            dp[i] = Math.min(fromPrev1, fromPrev2);
        }

        // The answer is the minimum total cost to reach the final room n - 1.
        return dp[n - 1];
    }

    /**
     * Computes the minimum total curation cost using constant extra space.
     *
     * This method is functionally equivalent to minimumCost(...), but instead of storing
     * the entire DP array, it only keeps the last two DP values because each state depends
     * only on the previous two states.
     *
     * @param cost the curation cost for each room; cost[i] is paid if room i is visited
     * @param premium binary array where premium[i] == 1 means room i is premium, otherwise 0
     * @param penalty extra fee charged when two consecutive visited rooms are both premium
     * @return the minimum total cost to start at room 0 and finish at room n - 1
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long minimumCostOptimized(int[] cost, int[] premium, int penalty) {
        validateInput(cost, premium);

        int n = cost.length;

        // Base case for room 0.
        long dp0 = cost[0];

        // Base case for room 1.
        long dp1 = dp0 + cost[1] + transitionPenalty(premium, 0, 1, penalty);

        // If there are only 2 rooms, room 1 is the destination.
        if (n == 2) {
            return dp1;
        }

        // Iterate from room 2 onward.
        for (int i = 2; i < n; i++) {
            // Compute the two possible ways to reach room i.
            long fromPrev1 = dp1 + cost[i] + transitionPenalty(premium, i - 1, i, penalty);
            long fromPrev2 = dp0 + cost[i] + transitionPenalty(premium, i - 2, i, penalty);

            long current = Math.min(fromPrev1, fromPrev2);

            // Shift the rolling DP window forward:
            // old dp1 becomes new dp0, current becomes new dp1.
            dp0 = dp1;
            dp1 = current;
        }

        return dp1;
    }

    /**
     * Returns the premium-to-premium transition penalty between two consecutively visited rooms.
     *
     * @param premium binary premium marker array
     * @param from index of the previous visited room
     * @param to index of the current visited room
     * @param penalty penalty amount charged if both rooms are premium
     * @return penalty if premium[from] and premium[to] are both 1; otherwise 0
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int transitionPenalty(int[] premium, int from, int to, int penalty) {
        return (premium[from] == 1 && premium[to] == 1) ? penalty : 0;
    }

    /**
     * Validates the input arrays.
     *
     * @param cost the room cost array
     * @param premium the premium marker array
     * @return nothing
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public void validateInput(int[] cost, int[] premium) {
        if (cost == null || premium == null) {
            throw new IllegalArgumentException("Input arrays must not be null.");
        }
        if (cost.length != premium.length) {
            throw new IllegalArgumentException("cost and premium must have the same length.");
        }
        if (cost.length < 2) {
            throw new IllegalArgumentException("There must be at least 2 rooms.");
        }

        for (int value : premium) {
            if (value != 0 && value != 1) {
                throw new IllegalArgumentException("premium array must contain only 0 or 1.");
            }
        }
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstration call
     * Space complexity: O(1) extra for the optimized method call
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] cost1 = {4, 2, 7, 3, 5};
        int[] premium1 = {1, 0, 1, 1, 0};
        int penalty1 = 6;

        long result1 = solution.minimumCost(cost1, premium1, penalty1);
        long result1Optimized = solution.minimumCostOptimized(cost1, premium1, penalty1);

        System.out.println("Example 1:");
        System.out.println("Minimum cost (DP array): " + result1);
        System.out.println("Minimum cost (optimized): " + result1Optimized);
        System.out.println("Expected: 14");
        System.out.println();

        int[] cost2 = {1, 9, 2, 8, 1, 3};
        int[] premium2 = {1, 1, 0, 1, 1, 1};
        int penalty2 = 4;

        long result2 = solution.minimumCost(cost2, premium2, penalty2);
        long result2Optimized = solution.minimumCostOptimized(cost2, premium2, penalty2);

        System.out.println("Example 2:");
        System.out.println("Minimum cost (DP array): " + result2);
        System.out.println("Minimum cost (optimized): " + result2Optimized);
        System.out.println("Expected: 11");
    }
}