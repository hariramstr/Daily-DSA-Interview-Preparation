import java.util.*;

/*
Problem Title: Minimum Cost to Plan Study Sessions with Topic Fatigue

Problem Description:
You are preparing a study plan for an upcoming exam. There are n chapters to study in a fixed order,
where chapter i has a study time time[i] and belongs to a topic topic[i]. You must partition the chapters
into contiguous study sessions. Each session is completed in one sitting.

The cost of a single session is defined as:

    session_cost = max(time in the session) + fatigue_penalty

The fatigue_penalty for a session is the number of times the topic changes between consecutive chapters
inside that session. For example, if the topics in a session are [2, 2, 5, 5, 3], then the fatigue_penalty
is 2 because the topic changes at 2->5 and 5->3.

Your task is to compute the minimum total cost to finish all chapters by choosing where to split the sessions.

Since the chapters must be studied in order, every session must be a contiguous subarray, and every chapter
must belong to exactly one session.

Return the minimum possible total cost.

Constraints:
- 1 <= n <= 1000
- 1 <= time[i] <= 10^6
- 1 <= topic[i] <= 10^6
- You may create as many sessions as you want, including one chapter per session or one session containing all chapters.

Notes about the examples:
The written explanations in the prompt contain inconsistencies. The mathematically correct interpretation
of the stated cost definition is:

For any session covering chapters [l..r]:
    cost(l, r) = max(time[l..r]) + count of indices k in [l+1..r] such that topic[k] != topic[k-1]

This implementation follows that exact definition.

For example:
1) time = [3, 1, 4, 2], topic = [1, 1, 2, 2]
   One session over all chapters has:
   max = 4, topic changes = 1, total = 5
   Therefore the true minimum is 5.

2) time = [5, 2, 6, 1, 3], topic = [7, 7, 7, 8, 7]
   One session over all chapters has:
   max = 6, topic changes = 2, total = 8
   Therefore the true minimum is 8.
*/

public class Solution {

    /**
     * Computes the minimum total cost to partition the chapters into contiguous study sessions.
     *
     * Dynamic Programming idea:
     * Let dp[i] be the minimum cost to cover the first i chapters (chapters 0 to i-1).
     * Then for every possible ending position i, we try every possible starting position j
     * of the last session. The last session is chapters [j..i-1].
     *
     * Transition:
     *     dp[i] = min over j in [0..i-1] of (dp[j] + sessionCost(j, i-1))
     *
     * While iterating j backward, we maintain:
     * - the maximum study time inside the current session [j..i-1]
     * - the number of topic changes inside the current session [j..i-1]
     *
     * This allows us to compute each session cost incrementally in O(1) per extension,
     * leading to an overall O(n^2) solution.
     *
     * @param time the study time required for each chapter
     * @param topic the topic id for each chapter
     * @return the minimum possible total cost
     * Time complexity: O(n^2)
     * Space complexity: O(n)
     */
    public long minimumTotalCost(int[] time, int[] topic) {
        validateInput(time, topic);

        int n = time.length;

        // dp[i] = minimum cost to study the first i chapters.
        // dp[0] = 0 because studying zero chapters costs nothing.
        long[] dp = new long[n + 1];

        // Initialize all states to a very large value so we can safely take minimums.
        Arrays.fill(dp, Long.MAX_VALUE / 4);
        dp[0] = 0L;

        // We compute dp[1], dp[2], ..., dp[n].
        for (int i = 1; i <= n; i++) {

            // We will consider every possible last session ending at chapter i - 1.
            // That means the last session can start at any j from i - 1 down to 0.
            int currentMaxTime = 0;

            // This will store the fatigue penalty (topic changes) inside the current session [j..i-1].
            int topicChanges = 0;

            // Move j backward to expand the session from right to left.
            for (int j = i - 1; j >= 0; j--) {

                // Step 1: include chapter j into the current session [j..i-1].
                currentMaxTime = Math.max(currentMaxTime, time[j]);

                // Step 2: when we extend from [j+1..i-1] to [j..i-1],
                // the only new adjacent pair introduced is (j, j+1).
                // If their topics differ, the fatigue penalty increases by 1.
                if (j < i - 1 && topic[j] != topic[j + 1]) {
                    topicChanges++;
                }

                // Step 3: compute the cost of this last session.
                long sessionCost = (long) currentMaxTime + topicChanges;

                // Step 4: combine:
                // - optimal cost for first j chapters => dp[j]
                // - cost of last session [j..i-1]   => sessionCost
                long candidate = dp[j] + sessionCost;

                // Step 5: keep the best answer for dp[i].
                if (candidate < dp[i]) {
                    dp[i] = candidate;
                }
            }
        }

        return dp[n];
    }

    /**
     * A helper method that computes the cost of a single session [left..right].
     * This method is not used by the optimized DP loop, but it is useful for demonstration,
     * debugging, and understanding the problem definition.
     *
     * @param time the study time required for each chapter
     * @param topic the topic id for each chapter
     * @param left the starting index of the session, inclusive
     * @param right the ending index of the session, inclusive
     * @return the cost of the session [left..right]
     * Time complexity: O(right - left + 1)
     * Space complexity: O(1)
     */
    public long sessionCost(int[] time, int[] topic, int left, int right) {
        validateInput(time, topic);

        if (left < 0 || right >= time.length || left > right) {
            throw new IllegalArgumentException("Invalid session range.");
        }

        int maxTime = 0;
        int changes = 0;

        for (int i = left; i <= right; i++) {
            maxTime = Math.max(maxTime, time[i]);
            if (i > left && topic[i] != topic[i - 1]) {
                changes++;
            }
        }

        return (long) maxTime + changes;
    }

    /**
     * Validates the input arrays.
     *
     * @param time the study time array
     * @param topic the topic array
     * @return nothing
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public void validateInput(int[] time, int[] topic) {
        if (time == null || topic == null) {
            throw new IllegalArgumentException("Input arrays must not be null.");
        }
        if (time.length != topic.length) {
            throw new IllegalArgumentException("time and topic must have the same length.");
        }
        if (time.length == 0) {
            throw new IllegalArgumentException("Input arrays must contain at least one chapter.");
        }
    }

    /**
     * Demonstrates the solution on sample inputs and prints the results.
     *
     * Important note:
     * The prompt's first sample explanation is internally inconsistent.
     * Under the exact stated definition of session cost, the correct answer for sample 1 is 5,
     * not 6. This program prints the mathematically correct results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding the called algorithm
     * Space complexity: O(1), excluding the called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] time1 = {3, 1, 4, 2};
        int[] topic1 = {1, 1, 2, 2};
        long result1 = solution.minimumTotalCost(time1, topic1);
        System.out.println("Sample 1 result: " + result1);
        System.out.println("Expected under the stated definition: 5");

        int[] time2 = {5, 2, 6, 1, 3};
        int[] topic2 = {7, 7, 7, 8, 7};
        long result2 = solution.minimumTotalCost(time2, topic2);
        System.out.println("Sample 2 result: " + result2);
        System.out.println("Expected: 8");

        int[] time3 = {10};
        int[] topic3 = {42};
        long result3 = solution.minimumTotalCost(time3, topic3);
        System.out.println("Single chapter result: " + result3);
        System.out.println("Expected: 10");

        int[] time4 = {2, 8, 3, 7};
        int[] topic4 = {1, 2, 3, 4};
        long result4 = solution.minimumTotalCost(time4, topic4);
        System.out.println("All different topics result: " + result4);

        int[] time5 = {4, 1, 4, 1, 4};
        int[] topic5 = {9, 9, 9, 9, 9};
        long result5 = solution.minimumTotalCost(time5, topic5);
        System.out.println("All same topic result: " + result5);
    }
}