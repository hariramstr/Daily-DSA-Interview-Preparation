import java.util.*;

/*
 * Title: Minimum Training Score to Unlock Project Groups
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A company offers employees a sequence of project groups. The i-th group contains tasks[i] tasks,
 * and every task in that group requires the employee to have a training score of at least req[i].
 * An employee with score S may complete all tasks in group i only if S >= req[i]. The employee
 * must complete project groups in order, and may stop after any group.
 *
 * Goal:
 * Find the minimum training score S such that the employee can complete at least target tasks in total.
 *
 * Return the smallest integer S that allows the employee to finish a prefix of the project groups
 * whose total number of tasks is at least target. If even completing every group does not reach target,
 * return -1.
 *
 * Key Observation:
 * If a score S is sufficient, then any larger score is also sufficient.
 * This monotonic property makes binary search a natural fit.
 *
 * Constraints:
 * - 1 <= n == tasks.length == req.length <= 200000
 * - 1 <= tasks[i] <= 1000000000
 * - 1 <= req[i] <= 1000000000
 * - 1 <= target <= 100000000000000
 * - The answer, if it exists, is an integer in the range [1, max(req)]
 *
 * Important Clarification:
 * Because groups must be completed in order, the employee can complete exactly the longest prefix
 * of groups whose requirements are all <= S. Therefore, for a given score S, the total completed tasks
 * equals the sum of tasks over the longest valid prefix.
 *
 * Efficient Approach:
 * 1. Build prefix sums of tasks.
 * 2. Build prefix maximums of req.
 *    - prefixMaxReq[i] = maximum requirement among groups 0..i
 * 3. For a score S, the employee can complete group i if and only if prefixMaxReq[i] <= S.
 *    Therefore, the employee can complete the largest index i satisfying that condition.
 * 4. Binary search the minimum S for which completed tasks >= target.
 */

public class Solution {

    /**
     * Finds the minimum training score needed to complete at least target tasks.
     *
     * The algorithm works as follows:
     * 1. Compute total tasks. If total tasks < target, return -1 immediately.
     * 2. Build:
     *    - prefix task sums, so we can quickly know how many tasks are completed up to any index
     *    - prefix maximum requirements, so we know the minimum score needed to unlock each prefix
     * 3. Binary search the answer in the range [1, max(req)].
     * 4. For each candidate score mid, check whether it is enough to complete at least target tasks.
     *
     * @param tasks the number of tasks in each project group
     * @param req the required training score for each project group
     * @param target the minimum total number of tasks that must be completed
     * @return the smallest integer training score that allows completion of at least target tasks,
     *         or -1 if impossible
     *
     * Time complexity: O(n + log(maxReq) * log n)
     * Space complexity: O(n)
     */
    public long minimumTrainingScore(int[] tasks, int[] req, long target) {
        int n = tasks.length;

        // Step 1:
        // Compute prefix sums of tasks.
        // prefixTasks[i] will store the total number of tasks from group 0 through group i.
        long[] prefixTasks = new long[n];

        // Step 2:
        // Compute prefix maximum requirements.
        // prefixMaxReq[i] will store the maximum requirement among groups 0 through i.
        // This is crucial because to complete group i, the employee must also have been able
        // to complete every earlier group. Therefore, the score must be at least the maximum
        // requirement seen so far in the prefix.
        int[] prefixMaxReq = new int[n];

        long totalTasks = 0L;
        int maxReq = 0;

        for (int i = 0; i < n; i++) {
            totalTasks += tasks[i];
            prefixTasks[i] = totalTasks;

            if (i == 0) {
                prefixMaxReq[i] = req[i];
            } else {
                prefixMaxReq[i] = Math.max(prefixMaxReq[i - 1], req[i]);
            }

            maxReq = Math.max(maxReq, req[i]);
        }

        // If even completing every group does not reach the target,
        // then the answer is impossible.
        if (totalTasks < target) {
            return -1L;
        }

        // Binary search over the possible score range.
        // The problem guarantees that if an answer exists, it lies in [1, max(req)].
        int left = 1;
        int right = maxReq;
        long answer = -1L;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            // Check whether score = mid is sufficient.
            if (canReachTarget(mid, prefixTasks, prefixMaxReq, target)) {
                // mid works, so record it and try to find an even smaller valid score.
                answer = mid;
                right = mid - 1;
            } else {
                // mid does not work, so we need a larger score.
                left = mid + 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether a given training score is sufficient to complete at least target tasks.
     *
     * Detailed logic:
     * - The employee can complete a prefix ending at index i if and only if every group in that
     *   prefix has requirement <= score.
     * - That condition is equivalent to prefixMaxReq[i] <= score.
     * - Since prefixMaxReq is non-decreasing, we can binary search for the largest valid index.
     * - Then we read the completed task count from prefixTasks at that index.
     *
     * @param score the candidate training score being tested
     * @param prefixTasks prefix sums of tasks
     * @param prefixMaxReq prefix maximum requirements
     * @param target the target number of tasks to reach
     * @return true if this score allows completion of at least target tasks; false otherwise
     *
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public boolean canReachTarget(int score, long[] prefixTasks, int[] prefixMaxReq, long target) {
        int n = prefixMaxReq.length;

        // We want the largest index i such that prefixMaxReq[i] <= score.
        // Because prefixMaxReq is non-decreasing, binary search is valid.
        int left = 0;
        int right = n - 1;
        int bestIndex = -1;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (prefixMaxReq[mid] <= score) {
                // This prefix is fully completable.
                // Try to extend farther to the right.
                bestIndex = mid;
                left = mid + 1;
            } else {
                // This prefix is not completable, so any larger prefix also fails.
                right = mid - 1;
            }
        }

        // If bestIndex == -1, then even the first group cannot be completed.
        long completedTasks = (bestIndex == -1) ? 0L : prefixTasks[bestIndex];
        return completedTasks >= target;
    }

    /**
     * A simple helper method to print an array in a beginner-friendly format.
     *
     * @param arr the integer array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement.
     *
     * Example 1:
     * tasks = [4, 3, 5, 2]
     * req   = [2, 6, 6, 9]
     * target = 10
     * Expected output: 6
     *
     * Reason:
     * - Score 5 completes only first group => 4 tasks
     * - Score 6 completes first three groups => 12 tasks
     * Therefore minimum score is 6.
     *
     * Example 2 as written in the prompt:
     * tasks = [2, 1, 4]
     * req   = [5, 3, 8]
     * target = 6
     * Correct output: 8
     *
     * Reason:
     * - Score < 5 => 0 tasks
     * - Score 5..7 => first two groups => 3 tasks
     * - Score 8 => all groups => 7 tasks
     * Therefore minimum score is 8.
     *
     * Additional verification:
     * If target = 8 for the same arrays, answer should be -1 because total tasks = 7.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(n + log(maxReq) * log n) per demonstration call
     * Space complexity: O(n)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] tasks1 = {4, 3, 5, 2};
        int[] req1 = {2, 6, 6, 9};
        long target1 = 10L;
        long result1 = solution.minimumTrainingScore(tasks1, req1, target1);

        System.out.println("Example 1");
        System.out.println("tasks  = " + solution.arrayToString(tasks1));
        System.out.println("req    = " + solution.arrayToString(req1));
        System.out.println("target = " + target1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 6");
        System.out.println();

        int[] tasks2 = {2, 1, 4};
        int[] req2 = {5, 3, 8};
        long target2 = 6L;
        long result2 = solution.minimumTrainingScore(tasks2, req2, target2);

        System.out.println("Example 2");
        System.out.println("tasks  = " + solution.arrayToString(tasks2));
        System.out.println("req    = " + solution.arrayToString(req2));
        System.out.println("target = " + target2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 8");
        System.out.println();

        long target3 = 8L;
        long result3 = solution.minimumTrainingScore(tasks2, req2, target3);

        System.out.println("Additional Check");
        System.out.println("tasks  = " + solution.arrayToString(tasks2));
        System.out.println("req    = " + solution.arrayToString(req2));
        System.out.println("target = " + target3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = -1");
    }
}