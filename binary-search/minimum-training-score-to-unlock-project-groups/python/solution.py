"""
Title: Minimum Training Score to Unlock Project Groups

Problem Description:
A company offers employees a sequence of project groups. The i-th group contains
tasks[i] tasks, and every task in that group requires the employee to have a
training score of at least req[i]. An employee with score S may complete all
tasks in group i only if S >= req[i]. The employee must complete project groups
in order, and may stop after any group.

Goal:
Find the minimum training score S such that the employee can complete at least
target tasks in total by finishing some prefix of the project groups.

Return:
- The smallest integer S that allows the employee to finish a prefix whose total
  number of tasks is at least target.
- If even completing every group does not reach target, return -1.

Key Observation:
If a score S is sufficient, then any larger score is also sufficient. This
monotonic behavior makes binary search the correct and efficient approach.
"""

from typing import List


class Solution:
    def _can_reach_target(self, tasks: List[int], req: List[int], target: int, score: int) -> bool:
        """
        Check whether a given training score is enough to complete a prefix of groups
        whose total tasks reach at least the target.

        The employee must complete groups in order. Therefore:
        - We start from the first group.
        - As long as the current score is high enough for the current group's requirement,
          we add that group's tasks to the running total.
        - The moment we encounter a group whose requirement is greater than the score,
          progress stops immediately because later groups cannot be attempted before
          finishing this one.

        Args:
            tasks: Number of tasks in each project group.
            req: Required training score for each project group.
            target: Minimum total tasks we want to complete.
            score: Candidate training score being tested.

        Returns:
            True if this score allows completion of at least target tasks, otherwise False.

        Time complexity:
            O(n) in the worst case, where n is the number of groups.

        Space complexity:
            O(1), excluding input storage.
        """
        # This variable stores how many tasks have been completed so far while
        # moving through the groups from left to right.
        completed_tasks: int = 0

        # We inspect each group in order because the problem explicitly states
        # that groups must be completed sequentially.
        for i in range(len(tasks)):
            # If the current score is smaller than the requirement of this group,
            # the employee cannot complete this group.
            #
            # Since groups must be done in order, failing here means we must stop
            # immediately. We cannot skip this group and try later ones.
            if score < req[i]:
                break

            # If we reach this point, the current group is completable.
            # Add its tasks to the running total.
            completed_tasks += tasks[i]

            # As soon as we have reached or exceeded the target, we can return True.
            # There is no need to continue scanning the remaining groups.
            if completed_tasks >= target:
                return True

        # If we finish the loop (or break early) without reaching target,
        # then this score is not sufficient.
        return False

    def minimum_training_score(self, tasks: List[int], req: List[int], target: int) -> int:
        """
        Find the minimum training score needed to complete at least target tasks
        by finishing a prefix of the project groups.

        This method uses binary search over the possible score range [1, max(req)].
        The reason binary search works is the monotonic property:
        - If score S is enough, then every score > S is also enough.
        - If score S is not enough, then every score < S is also not enough.

        Before binary search, we first check whether the target is achievable at all.
        If the sum of all tasks is smaller than target, then even completing every
        group cannot reach the target, so we return -1 immediately.

        Args:
            tasks: Number of tasks in each project group.
            req: Required training score for each project group.
            target: Minimum total tasks to complete.

        Returns:
            The smallest valid training score, or -1 if impossible.

        Time complexity:
            O(n log M), where:
            - n is the number of groups
            - M is max(req)
            Each binary search step performs an O(n) feasibility check.

        Space complexity:
            O(1), excluding input storage.
        """
        # Step 1: Quick impossibility check.
        #
        # If even the sum of all tasks across all groups is less than target,
        # then no score can ever help because the employee cannot exceed the
        # total available tasks.
        total_tasks: int = sum(tasks)
        if total_tasks < target:
            return -1

        # Step 2: Establish the binary search boundaries.
        #
        # The problem states that if an answer exists, it lies in [1, max(req)].
        # - A score below 1 is outside the allowed answer range.
        # - A score above max(req) is unnecessary because max(req) already allows
        #   every group to be completed.
        left: int = 1
        right: int = max(req)

        # This variable will store the best (smallest) valid score found so far.
        # We initialize it to -1 and update it whenever we find a feasible score.
        answer: int = -1

        # Step 3: Standard binary search on the answer space.
        #
        # We repeatedly test the middle score:
        # - If it works, record it and try smaller scores.
        # - If it does not work, try larger scores.
        while left <= right:
            mid: int = (left + right) // 2

            # Use the helper method to determine whether this candidate score
            # is sufficient to reach the target.
            if self._can_reach_target(tasks, req, target, mid):
                # mid is a valid score.
                # Record it as a candidate answer.
                answer = mid

                # Since we want the minimum valid score, continue searching
                # on the left half for a smaller feasible value.
                right = mid - 1
            else:
                # mid is too small, so all scores <= mid are also too small.
                # Move to the right half.
                left = mid + 1

        # After binary search finishes, answer holds the smallest feasible score.
        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    tasks1: List[int] = [4, 3, 5, 2]
    req1: List[int] = [2, 6, 6, 9]
    target1: int = 10
    result1: int = solution.minimum_training_score(tasks1, req1, target1)
    print("Example 1 Result:", result1)  # Expected: 6

    # Example 2 as written in the description:
    # tasks = [2, 1, 4], req = [5, 3, 8], target = 6
    #
    # Careful trace:
    # - Score < 5: cannot complete first group => 0 tasks
    # - Score 5 to 7: complete first two groups => 2 + 1 = 3 tasks
    # - Score 8: complete all groups => 2 + 1 + 4 = 7 tasks
    # Therefore the minimum valid score is 8, not -1.
    tasks2: List[int] = [2, 1, 4]
    req2: List[int] = [5, 3, 8]
    target2: int = 6
    result2: int = solution.minimum_training_score(tasks2, req2, target2)
    print("Example 2 Result:", result2)  # Expected by correct trace: 8

    # Additional check mentioned in the description:
    # If target were 8 instead, even all groups sum to only 7, so answer is -1.
    tasks3: List[int] = [2, 1, 4]
    req3: List[int] = [5, 3, 8]
    target3: int = 8
    result3: int = solution.minimum_training_score(tasks3, req3, target3)
    print("Additional Check Result:", result3)  # Expected: -1