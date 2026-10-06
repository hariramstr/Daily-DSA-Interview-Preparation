import java.util.*;

/*
Title: Count WiFi Router Pairs Covering a Hallway
Difficulty: Hard
Topic: Two Pointers

Problem Description:
A company is installing WiFi routers along a long hallway. The hallway is represented by a line segment from position 0 to position L. You are given an integer array positions of length n, where positions[i] is the location of the i-th router candidate along the hallway, and an integer array radius of the same length, where radius[i] is the coverage radius of that candidate. If you install router i and router j together (i < j), they cover intervals [positions[i] - radius[i], positions[i] + radius[i]] and [positions[j] - radius[j], positions[j] + radius[j]] on the hallway.

A pair of routers is called valid if the union of their coverage intervals covers the entire hallway [0, L]. Count the number of distinct valid pairs.

Two routers may overlap, one interval may fully contain the other, and coverage outside [0, L] is allowed. Router candidates are not guaranteed to be sorted by position. Return the total number of pairs (i, j) with i < j that fully cover the hallway.

Your solution should be efficient enough for large inputs, so a quadratic check over all pairs will not pass. A correct Hard-level solution typically involves sorting transformed interval endpoints and using a two-pointer or monotonic counting strategy.

Constraints:
- 2 <= n <= 200000
- 1 <= L <= 1000000000
- 0 <= positions[i] <= L
- 0 <= radius[i] <= 1000000000
- positions.length == radius.length

Example 1:
Input: positions = [2, 8, 5, 11], radius = [3, 4, 1, 2], L = 10
Output: 4
Explanation:
Intervals are [-1,5], [4,12], [4,6], [9,13].
Valid pairs are (0,1), (0,3), (1,2), and (1,3).
Each of these pairs has union covering [0,10].

Example 2:
Input: positions = [1, 4, 7, 9], radius = [1, 1, 1, 1], L = 10
Output: 0
Explanation:
Intervals are [0,2], [3,5], [6,8], [8,10].
No pair covers the full hallway because every pair leaves at least one uncovered gap inside [0,10].
*/

public class Solution {

    /**
     * Counts the number of distinct router pairs whose union of coverage intervals
     * fully covers the hallway [0, L].
     *
     * Core idea:
     * For each router interval [a, b], only the part intersecting the hallway matters.
     * So we clamp it to:
     *   left = max(0, a)
     *   right = min(L, b)
     *
     * A pair of clamped intervals [l1, r1] and [l2, r2] covers [0, L] if and only if:
     * 1) At least one interval reaches the left boundary: min(l1, l2) == 0
     * 2) At least one interval reaches the right boundary: max(r1, r2) == L
     * 3) There is no uncovered gap between them:
     *      after ordering by left endpoint, second.left <= first.right
     *
     * A very useful equivalent condition is:
     *   Let A be an interval with left == 0.
     *   Let B be an interval with right == L.
     *   Then A and B together cover [0, L] iff B.left <= A.right.
     *
     * Therefore:
     * - Build all intervals that touch the left boundary (left == 0), storing their right endpoints.
     * - Build all intervals that touch the right boundary (right == L), storing their left endpoints.
     * - Count pairs (A, B) with B.left <= A.right.
     * - But if one interval itself touches both boundaries (it alone covers [0, L]),
     *   then pairing it with any other interval is valid, and the above cross-counting
     *   must be adjusted carefully to avoid double counting and to include pairs where
     *   both intervals are from the same side category.
     *
     * Clean counting strategy:
     * - Let FULL be intervals with left == 0 and right == L.
     * - Any pair containing at least one FULL interval is valid.
     *   Count = C(fullCount, 2) + fullCount * (n - fullCount)
     * - For the remaining intervals:
     *   * LEFT_ONLY: left == 0, right < L
     *   * RIGHT_ONLY: left > 0, right == L
     *   A valid pair must be one LEFT_ONLY and one RIGHT_ONLY, and they must overlap/touch:
     *      right_of_left_only >= left_of_right_only
     *   This can be counted efficiently after sorting with a two-pointer scan.
     *
     * @param positions positions[i] is the location of router candidate i
     * @param radius radius[i] is the coverage radius of router candidate i
     * @param L hallway length; hallway is [0, L]
     * @return number of valid pairs (i, j), i < j
     *
     * Time complexity: O(n log n) due to sorting.
     * Space complexity: O(n) for storing transformed interval endpoint lists.
     */
    public long countValidPairs(int[] positions, int[] radius, int L) {
        int n = positions.length;

        // These lists store only the information needed for the efficient counting step.
        // leftOnlyRights:
        //   intervals that touch the left boundary 0, but do NOT already cover the whole hallway.
        //   For such an interval [0, r], we only need its right endpoint r.
        List<Long> leftOnlyRights = new ArrayList<>();

        // rightOnlyLefts:
        //   intervals that touch the right boundary L, but do NOT already cover the whole hallway.
        //   For such an interval [l, L], we only need its left endpoint l.
        List<Long> rightOnlyLefts = new ArrayList<>();

        // fullCount:
        //   intervals that already cover the entire hallway by themselves, i.e. [0, L] after clamping.
        long fullCount = 0;

        // Step 1: Transform every router into its clamped hallway coverage interval.
        for (int i = 0; i < n; i++) {
            long rawLeft = (long) positions[i] - radius[i];
            long rawRight = (long) positions[i] + radius[i];

            // Clamp to the hallway [0, L].
            long left = Math.max(0L, rawLeft);
            long right = Math.min((long) L, rawRight);

            // If after clamping the interval is [0, L], this single router already covers everything.
            if (left == 0L && right == (long) L) {
                fullCount++;
            } else if (left == 0L) {
                // Touches left boundary only.
                leftOnlyRights.add(right);
            } else if (right == (long) L) {
                // Touches right boundary only.
                rightOnlyLefts.add(left);
            }
            // Intervals that touch neither boundary can never help a pair cover [0, L],
            // unless paired with a FULL interval. Those are already handled by fullCount.
        }

        // Step 2: Count all pairs that include at least one FULL interval.
        //
        // Why every such pair is valid:
        // If one interval alone covers [0, L], then union with any other interval still covers [0, L].
        //
        // Number of such pairs:
        // - Choose 2 among FULL intervals: C(fullCount, 2)
        // - Choose 1 FULL and 1 non-FULL: fullCount * (n - fullCount)
        long answer = fullCount * (n - fullCount) + fullCount * (fullCount - 1) / 2;

        // Step 3: Count valid pairs between LEFT_ONLY and RIGHT_ONLY intervals.
        //
        // A pair [0, r] and [l, L] covers [0, L] iff they overlap or touch:
        //   l <= r
        //
        // We sort:
        // - leftOnlyRights ascending
        // - rightOnlyLefts ascending
        //
        // For each rightOnlyLeft = l, we need the number of leftOnlyRights r such that r >= l.
        // If leftOnlyRights is sorted ascending, we can maintain a pointer to the first r >= l.
        Collections.sort(leftOnlyRights);
        Collections.sort(rightOnlyLefts);

        int m = leftOnlyRights.size();
        int pointer = 0;

        for (long leftOfRightOnly : rightOnlyLefts) {
            // Move pointer until leftOnlyRights[pointer] is the first value >= leftOfRightOnly.
            while (pointer < m && leftOnlyRights.get(pointer) < leftOfRightOnly) {
                pointer++;
            }

            // All remaining left-only intervals from pointer to m-1 satisfy r >= l,
            // so each forms a valid pair with the current right-only interval.
            answer += (m - pointer);
        }

        return answer;
    }

    /**
     * Brute-force validator for small inputs.
     * This method is not used by the main algorithm, but it is useful for educational
     * verification and for checking sample cases.
     *
     * A pair of intervals covers [0, L] if:
     * - together they reach 0 and L, and
     * - there is no gap between them inside [0, L]
     *
     * @param positions positions of routers
     * @param radius coverage radii of routers
     * @param L hallway length
     * @return exact number of valid pairs by O(n^2) checking
     *
     * Time complexity: O(n^2)
     * Space complexity: O(1) extra space
     */
    public long countValidPairsBruteForce(int[] positions, int[] radius, int L) {
        int n = positions.length;
        long count = 0;

        for (int i = 0; i < n; i++) {
            long l1 = Math.max(0L, (long) positions[i] - radius[i]);
            long r1 = Math.min((long) L, (long) positions[i] + radius[i]);

            for (int j = i + 1; j < n; j++) {
                long l2 = Math.max(0L, (long) positions[j] - radius[j]);
                long r2 = Math.min((long) L, (long) positions[j] + radius[j]);

                // Order the two intervals by left endpoint so we can test for a gap easily.
                long firstLeft = l1;
                long firstRight = r1;
                long secondLeft = l2;
                long secondRight = r2;

                if (secondLeft < firstLeft) {
                    long tempL = firstLeft;
                    long tempR = firstRight;
                    firstLeft = secondLeft;
                    firstRight = secondRight;
                    secondLeft = tempL;
                    secondRight = tempR;
                }

                boolean reachesLeftBoundary = firstLeft == 0L;
                boolean reachesRightBoundary = Math.max(firstRight, secondRight) == (long) L;
                boolean noGap = secondLeft <= firstRight;

                if (reachesLeftBoundary && reachesRightBoundary && noGap) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     *
     * Time complexity: O(n log n) for each demonstrated test case.
     * Space complexity: O(n) for each demonstrated test case.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] positions1 = {2, 8, 5, 11};
        int[] radius1 = {3, 4, 1, 2};
        int L1 = 10;
        long result1 = solution.countValidPairs(positions1, radius1, L1);
        System.out.println(result1); // Expected: 4

        int[] positions2 = {1, 4, 7, 9};
        int[] radius2 = {1, 1, 1, 1};
        int L2 = 10;
        long result2 = solution.countValidPairs(positions2, radius2, L2);
        System.out.println(result2); // Expected: 0

        // Optional brute-force verification for the sample cases.
        System.out.println(solution.countValidPairsBruteForce(positions1, radius1, L1)); // Expected: 4
        System.out.println(solution.countValidPairsBruteForce(positions2, radius2, L2)); // Expected: 0
    }
}