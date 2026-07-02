/*
Title: Minimum Training Score to Unlock Project Groups
Difficulty: Medium
Topic: Binary Search

Problem Description:
A company offers employees a sequence of project groups. The i-th group contains tasks[i] tasks, and every task in that group requires the employee to have a training score of at least req[i]. An employee with score S may complete all tasks in group i only if S >= req[i]. The employee must complete project groups in order, and may stop after any group. Your goal is to find the minimum training score S such that the employee can complete at least target tasks in total.

Return the smallest integer S that allows the employee to finish a prefix of the project groups whose total number of tasks is at least target. If even completing every group does not reach target, return -1.

This problem is designed so that a direct simulation over all possible scores is too slow. Instead, observe that if a score S is sufficient, then any larger score is also sufficient. That monotonic property makes binary search a natural fit.

Constraints:
- 1 <= n == tasks.length == req.length <= 200000
- 1 <= tasks[i] <= 1000000000
- 1 <= req[i] <= 1000000000
- 1 <= target <= 100000000000000
- The answer, if it exists, is an integer in the range [1, max(req)]

Examples:
1)
tasks = [4, 3, 5, 2], req = [2, 6, 6, 9], target = 10
Output: 6

2)
tasks = [2, 1, 4], req = [5, 3, 8], target = 6
Output: 8
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Preprocessing prefix requirements and prefix task sums: O(n)
    - Each binary search check uses upper_bound-style binary search on the prefix requirements array: O(log n)
    - Outer binary search over score range [1, max(req)]: O(log maxReq)
    - Total: O(n + log(maxReq) * log n)

    Space Complexity:
    - O(n) for prefix maximum requirements and prefix task sums

    Beginner-friendly idea:
    1. If the employee has score S, they can complete groups from the start only until they hit the first group
       whose requirement is greater than S.
    2. So for every prefix ending at index i, the score needed to complete that entire prefix is the maximum
       requirement seen in groups 0..i.
    3. If we build:
       - prefixMaxReq[i] = max(req[0..i])
       - prefixTasks[i]  = tasks[0] + tasks[1] + ... + tasks[i]
       then for a given score S, the farthest completable prefix is the largest i with prefixMaxReq[i] <= S.
    4. Because if score S works, any larger score also works, we can binary search the minimum valid S.
    */
    public int MinimumTrainingScore(long[] tasks, int[] req, long target)
    {
        int n = tasks.Length;

        // Step 1:
        // Build two helper arrays:
        //
        // prefixMaxReq[i]:
        //   the maximum requirement among groups 0..i
        //   This tells us the minimum score needed to complete the entire prefix ending at i.
        //
        // prefixTasks[i]:
        //   the total number of tasks in groups 0..i
        //   This tells us how many tasks are completed if we can finish that prefix.
        //
        // Why this is useful:
        // The employee must complete groups in order.
        // So they cannot "skip" a hard group and do later ones.
        // Therefore, for any score S, the completed work is always some prefix of the array.
        int[] prefixMaxReq = new int[n];
        long[] prefixTasks = new long[n];

        prefixMaxReq[0] = req[0];
        prefixTasks[0] = tasks[0];

        for (int i = 1; i < n; i++)
        {
            // The hardest requirement seen so far in the prefix.
            prefixMaxReq[i] = Math.Max(prefixMaxReq[i - 1], req[i]);

            // Running total of tasks in the prefix.
            prefixTasks[i] = prefixTasks[i - 1] + tasks[i];
        }

        // Step 2:
        // Quick impossibility check.
        //
        // If even completing every group does not reach the target,
        // then no score can ever succeed.
        if (prefixTasks[n - 1] < target)
        {
            return -1;
        }

        // Step 3:
        // Determine the binary search range for the answer.
        //
        // The problem guarantees that if an answer exists, it lies in [1, max(req)].
        // prefixMaxReq[n - 1] is exactly max(req).
        int left = 1;
        int right = prefixMaxReq[n - 1];
        int answer = right;

        // Step 4:
        // Binary search for the smallest score that is sufficient.
        //
        // Why binary search works:
        // - If score S allows reaching target tasks, then any score > S also allows it.
        // - That means the predicate "CanReachTarget(S)" is monotonic:
        //   false false false ... true true true
        // - Binary search is perfect for finding the first true value.
        while (left <= right)
        {
            int mid = left + (right - left) / 2;

            // Check whether this candidate score is enough.
            if (CanReachTarget(mid, prefixMaxReq, prefixTasks, target))
            {
                // mid works, so record it as a possible answer.
                answer = mid;

                // But we still want the minimum score,
                // so continue searching on the left half.
                right = mid - 1;
            }
            else
            {
                // mid does not work, so we need a larger score.
                left = mid + 1;
            }
        }

        return answer;
    }

    private bool CanReachTarget(int score, int[] prefixMaxReq, long[] prefixTasks, long target)
    {
        // This method answers:
        // "If the employee has this score, can they complete at least target tasks?"
        //
        // Key observation:
        // The employee can complete exactly the longest prefix whose required maximum
        // is <= score.
        //
        // Since prefixMaxReq is non-decreasing, we can binary search for the last index
        // where prefixMaxReq[index] <= score.

        int lastCompletableIndex = FindLastIndexAtMost(prefixMaxReq, score);

        // If no prefix is completable, then completed tasks = 0.
        if (lastCompletableIndex == -1)
        {
            return false;
        }

        // Otherwise, the total completed tasks is the prefix sum at that index.
        return prefixTasks[lastCompletableIndex] >= target;
    }

    private int FindLastIndexAtMost(int[] arr, int value)
    {
        // Standard binary search:
        // find the largest index i such that arr[i] <= value.
        //
        // arr is non-decreasing because it is a prefix maximum array.
        int left = 0;
        int right = arr.Length - 1;
        int result = -1;

        while (left <= right)
        {
            int mid = left + (right - left) / 2;

            if (arr[mid] <= value)
            {
                // mid is valid, but maybe there is a larger valid index.
                result = mid;
                left = mid + 1;
            }
            else
            {
                // mid is too large, so valid indices must be to the left.
                right = mid - 1;
            }
        }

        return result;
    }
}

// Demo code

var solution = new Solution();

// Example 1
long[] tasks1 = { 4, 3, 5, 2 };
int[] req1 = { 2, 6, 6, 9 };
long target1 = 10;
int result1 = solution.MinimumTrainingScore(tasks1, req1, target1);
Console.WriteLine(result1); // Expected: 6

// Example 2
long[] tasks2 = { 2, 1, 4 };
int[] req2 = { 5, 3, 8 };
long target2 = 6;
int result2 = solution.MinimumTrainingScore(tasks2, req2, target2);
Console.WriteLine(result2); // Expected: 8

// Additional check based on the note in Example 2:
// If target were 8, answer should be -1 because total tasks = 7.
long target3 = 8;
int result3 = solution.MinimumTrainingScore(tasks2, req2, target3);
Console.WriteLine(result3); // Expected: -1