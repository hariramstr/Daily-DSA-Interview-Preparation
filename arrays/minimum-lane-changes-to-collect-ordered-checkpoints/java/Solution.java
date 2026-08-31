import java.util.*;

/*
 * Title: Minimum Lane Changes to Collect Ordered Checkpoints
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * You are given a straight road divided into n positions, numbered from 0 to n - 1,
 * and exactly 3 lanes numbered 1 to 3. A vehicle starts at position 0 in lane 2.
 * Some positions contain a checkpoint token in one of the lanes, and some positions
 * may also contain a blocked lane due to road work.
 *
 * Two arrays are provided:
 *
 * - checkpoints, where checkpoints[i] is either 0 (no token at position i) or a lane
 *   number 1..3 indicating that a token must be collected at that position.
 * - blocked, where blocked[i] is either 0 (no blocked lane at position i) or a lane
 *   number 1..3 indicating that lane cannot be occupied at that position.
 *
 * The vehicle moves from left to right, one position at a time. At each step, it may
 * stay in the same lane or switch to another lane before entering the next position.
 * Every lane switch costs 1. Moving forward costs 0. A token at position i is collected
 * only if the vehicle is in checkpoints[i] when passing that position. You must collect
 * all required tokens in increasing position order, which happens automatically if you
 * visit the required lane at each token position.
 *
 * Return the minimum number of lane changes needed to reach position n - 1 while
 * collecting every token. If it is impossible, return -1.
 *
 * Constraints:
 * - 1 <= n <= 100000
 * - checkpoints.length == blocked.length == n
 * - checkpoints[i] is in {0, 1, 2, 3}
 * - blocked[i] is in {0, 1, 2, 3}
 * - If checkpoints[i] != 0, then checkpoints[i] != blocked[i]
 * - blocked[0] != 2
 *
 * Example 1:
 * Input: checkpoints = [0, 0, 1, 0, 3], blocked = [0, 3, 0, 2, 0]
 * Output: 2
 *
 * Example 2:
 * Input: checkpoints = [0, 2, 0, 1], blocked = [0, 0, 2, 1]
 * Output: -1
 */

public class Solution {

    /**
     * Computes the minimum number of lane changes needed to travel from position 0
     * to position n - 1 while collecting every required checkpoint token.
     *
     * The vehicle starts at position 0 in lane 2.
     *
     * Core idea:
     * We use dynamic programming over positions and lanes.
     * For each position i and each lane 1..3, we store the minimum number of lane changes
     * needed to be at position i in that lane while satisfying:
     * 1. The lane is not blocked at position i.
     * 2. If there is a checkpoint at position i, we must be in that exact lane.
     *
     * Transition:
     * To reach position i in lane L, we must come from position i - 1 in:
     * - the same lane L with no extra cost, or
     * - one of the other two lanes with +1 lane change.
     *
     * Because there are only 3 lanes, each transition is constant work, so the full
     * algorithm is linear in n.
     *
     * @param checkpoints checkpoints[i] is 0 if no token exists at position i,
     *                    otherwise it is the lane number 1..3 that must be occupied
     *                    at position i to collect the token
     * @param blocked blocked[i] is 0 if no lane is blocked at position i,
     *                otherwise it is the blocked lane number 1..3
     * @return the minimum number of lane changes required to reach the final position
     *         while collecting all tokens; returns -1 if impossible
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public int minimumLaneChanges(int[] checkpoints, int[] blocked) {
        if (checkpoints == null || blocked == null || checkpoints.length != blocked.length || checkpoints.length == 0) {
            return -1;
        }

        int n = checkpoints.length;

        // A large value used to represent an impossible state.
        // We avoid Integer.MAX_VALUE to prevent overflow when adding 1.
        final int INF = 1_000_000_000;

        // dp[lane] = minimum lane changes needed to be at the CURRENT position in this lane.
        // We use indices 1..3 and ignore index 0 for readability.
        int[] dp = new int[4];
        Arrays.fill(dp, INF);

        // Initial state at position 0:
        // The vehicle starts in lane 2 with cost 0.
        // However, we must still validate position 0:
        // - The starting lane must not be blocked there.
        // - If there is a checkpoint at position 0, it must be in lane 2.
        dp[2] = 0;

        // Apply the constraints of position 0 itself.
        applyPositionConstraints(dp, checkpoints[0], blocked[0], INF);

        // If after applying the constraints, all lanes are impossible,
        // then even the starting position is invalid.
        if (allImpossible(dp, INF)) {
            return -1;
        }

        // Process positions from left to right.
        for (int i = 1; i < n; i++) {
            int[] next = new int[4];
            Arrays.fill(next, INF);

            // For each target lane at the new position i,
            // compute the best way to arrive there from position i - 1.
            for (int targetLane = 1; targetLane <= 3; targetLane++) {
                // If targetLane is blocked at this position, we cannot occupy it.
                if (blocked[i] == targetLane) {
                    continue;
                }

                // If there is a checkpoint at this position, we must be in that exact lane.
                if (checkpoints[i] != 0 && checkpoints[i] != targetLane) {
                    continue;
                }

                // Try coming from each possible previous lane.
                for (int prevLane = 1; prevLane <= 3; prevLane++) {
                    // If previous state is impossible, skip it.
                    if (dp[prevLane] == INF) {
                        continue;
                    }

                    // If we stay in the same lane, cost does not increase.
                    // If we switch lanes before entering this position, cost increases by 1.
                    int cost = dp[prevLane] + (prevLane == targetLane ? 0 : 1);

                    // Keep the minimum cost among all possible previous lanes.
                    if (cost < next[targetLane]) {
                        next[targetLane] = cost;
                    }
                }
            }

            // Move to the next position.
            dp = next;

            // If no lane is reachable at this position, the route is impossible.
            if (allImpossible(dp, INF)) {
                return -1;
            }
        }

        // The answer is the minimum cost among all lanes at the final position.
        int answer = Math.min(dp[1], Math.min(dp[2], dp[3]));
        return answer >= INF ? -1 : answer;
    }

    /**
     * Applies the rules of a single position directly to the current DP state.
     *
     * This helper is used for position 0 because the vehicle starts already at that position.
     * So instead of transitioning into position 0, we simply invalidate any lane that:
     * - is blocked at position 0, or
     * - does not match the checkpoint lane if a checkpoint exists there
     *
     * @param dp current DP array where dp[lane] is the minimum cost to be in that lane
     * @param checkpointLane the required checkpoint lane at the current position, or 0 if none
     * @param blockedLane the blocked lane at the current position, or 0 if none
     * @param INF sentinel value representing an impossible state
     * @return nothing; the dp array is modified in place
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public void applyPositionConstraints(int[] dp, int checkpointLane, int blockedLane, int INF) {
        for (int lane = 1; lane <= 3; lane++) {
            // If this lane is blocked at the current position, it is impossible.
            if (blockedLane == lane) {
                dp[lane] = INF;
                continue;
            }

            // If a checkpoint exists, only that lane is allowed.
            if (checkpointLane != 0 && checkpointLane != lane) {
                dp[lane] = INF;
            }
        }
    }

    /**
     * Checks whether all three lane states are impossible.
     *
     * @param dp DP array using indices 1..3 for lanes
     * @param INF sentinel value representing an impossible state
     * @return true if all lanes are impossible; otherwise false
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public boolean allImpossible(int[] dp, int INF) {
        return dp[1] >= INF && dp[2] >= INF && dp[3] >= INF;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding the called algorithm
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] checkpoints1 = {0, 0, 1, 0, 3};
        int[] blocked1 = {0, 3, 0, 2, 0};
        int result1 = solution.minimumLaneChanges(checkpoints1, blocked1);
        System.out.println("Example 1 Output: " + result1);
        System.out.println("Expected: 2");

        int[] checkpoints2 = {0, 2, 0, 1};
        int[] blocked2 = {0, 0, 2, 1};
        int result2 = solution.minimumLaneChanges(checkpoints2, blocked2);
        System.out.println("Example 2 Output: " + result2);
        System.out.println("Expected: -1");

        // Additional small sanity check:
        // Start at lane 2, no blocks, no checkpoints, so answer should be 0.
        int[] checkpoints3 = {0, 0, 0};
        int[] blocked3 = {0, 0, 0};
        int result3 = solution.minimumLaneChanges(checkpoints3, blocked3);
        System.out.println("Additional Test Output: " + result3);
        System.out.println("Expected: 0");
    }
}