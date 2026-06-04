"""
Title: Assign Tasks to Workers with Skill Constraints
Difficulty: Medium
Topic: Recursion and Backtracking

Problem Description:
You are managing a small team of workers, and you need to assign a list of tasks to them.
Each task requires a specific skill level (an integer), and each worker has a maximum skill
level they can handle. A worker can only be assigned a task if the task's required skill
level is less than or equal to the worker's skill level. Each worker can be assigned at most
one task, and each task must be assigned to exactly one worker.

Given an array `workers` of length `m` representing each worker's skill level, and an array
`tasks` of length `n` representing each task's required skill level (where `n <= m`), return
all possible valid assignments as a list of lists. Each assignment is represented as a list
of length `n`, where the i-th element is the index (0-based) of the worker assigned to the
i-th task. The assignments should be returned in lexicographically sorted order.

Constraints:
- 1 <= n <= m <= 8
- 1 <= workers[i], tasks[j] <= 10
- All worker indices in a single assignment must be distinct.

Example 1:
Input: workers = [3, 5, 2], tasks = [2, 4]
Output: [[0, 1], [2, 1]]

Example 2:
Input: workers = [4, 4], tasks = [3, 3]
Output: [[0, 1], [1, 0]]
"""

from typing import List


class Solution:
    def assignTasks(self, workers: List[int], tasks: List[int]) -> List[List[int]]:
        """
        Find all valid assignments of tasks to workers using backtracking.

        Each task must be assigned to a worker whose skill level is >= the task's
        required skill level. Each worker can handle at most one task.

        Args:
            workers: List of integers representing each worker's maximum skill level.
            tasks: List of integers representing each task's required skill level.

        Returns:
            A list of all valid assignments in lexicographically sorted order.
            Each assignment is a list of worker indices (0-based), where the i-th
            element is the worker assigned to the i-th task.

        Time Complexity: O(m! / (m-n)! * n) in the worst case, where m = len(workers)
                         and n = len(tasks). We try all permutations of workers for tasks.
        Space Complexity: O(n) for the recursion stack and current assignment list,
                          plus O(k * n) for storing k valid results.
        """

        # -----------------------------------------------------------------------
        # SETUP: Initialize data structures needed for backtracking
        # -----------------------------------------------------------------------

        # This will store all valid complete assignments found during backtracking
        results: List[List[int]] = []

        # This tracks the current partial assignment being built.
        # current_assignment[i] = worker_index assigned to task i
        current_assignment: List[int] = []

        # This set tracks which workers have already been assigned in the current
        # partial assignment, so we don't assign the same worker to two tasks.
        used_workers: set = set()

        # -----------------------------------------------------------------------
        # BACKTRACKING FUNCTION: Recursively assign workers to tasks
        # -----------------------------------------------------------------------

        def backtrack(task_index: int) -> None:
            """
            Recursively try to assign a worker to the task at `task_index`.

            Base case: If task_index == len(tasks), all tasks have been assigned,
            so we record the current assignment as a valid solution.

            Recursive case: Try each worker (in order of their index, so results
            come out in lexicographic order) for the current task. If the worker
            is available and skilled enough, assign them and recurse to the next task.

            Args:
                task_index: The index of the task we are currently trying to assign.
            """

            # -------------------------------------------------------------------
            # BASE CASE: All tasks have been successfully assigned
            # -------------------------------------------------------------------
            if task_index == len(tasks):
                # We have a complete valid assignment — make a copy and save it.
                # We copy because current_assignment will be modified as we backtrack.
                results.append(current_assignment[:])
                return

            # -------------------------------------------------------------------
            # RECURSIVE CASE: Try assigning each worker to the current task
            # -------------------------------------------------------------------

            # The required skill level for the current task
            required_skill = tasks[task_index]

            # Iterate over all workers by their index (0, 1, 2, ..., m-1).
            # Iterating in ascending index order ensures that when we collect
            # results, they are naturally in lexicographic order by worker index.
            for worker_index in range(len(workers)):

                # CONSTRAINT 1: The worker must not already be assigned to another task
                if worker_index in used_workers:
                    # Skip this worker — they're already busy with another task
                    continue

                # CONSTRAINT 2: The worker's skill must meet the task's requirement
                if workers[worker_index] < required_skill:
                    # Skip this worker — they lack the necessary skill
                    continue

                # -----------------------------------------------------------
                # CHOOSE: Assign this worker to the current task
                # -----------------------------------------------------------
                current_assignment.append(worker_index)  # Record the assignment
                used_workers.add(worker_index)            # Mark worker as used

                # -----------------------------------------------------------
                # EXPLORE: Recurse to assign the next task
                # -----------------------------------------------------------
                backtrack(task_index + 1)

                # -----------------------------------------------------------
                # UN-CHOOSE (Backtrack): Undo the assignment to try other options
                # -----------------------------------------------------------
                current_assignment.pop()          # Remove the last assignment
                used_workers.remove(worker_index) # Free the worker for other tasks

        # -----------------------------------------------------------------------
        # KICK OFF the backtracking starting from task index 0
        # -----------------------------------------------------------------------
        backtrack(0)

        # -----------------------------------------------------------------------
        # SORT the results lexicographically before returning.
        # Because we iterate worker indices in ascending order (0, 1, 2, ...),
        # the results are already in lexicographic order. However, we sort
        # explicitly here to guarantee correctness regardless of iteration order.
        # -----------------------------------------------------------------------
        results.sort()

        return results


# ---------------------------------------------------------------------------
# MAIN: Demonstrate the solution with the provided examples
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    solution = Solution()

    # -----------------------------------------------------------------------
    # Example 1
    # workers = [3, 5, 2], tasks = [2, 4]
    # Expected Output: [[0, 1], [2, 1]]
    #
    # Trace:
    #   Task 0 requires skill 2:
    #     Worker 0 has skill 3 >= 2 ✓
    #     Worker 1 has skill 5 >= 2 ✓
    #     Worker 2 has skill 2 >= 2 ✓
    #   Task 1 requires skill 4:
    #     Worker 0 has skill 3 < 4  ✗
    #     Worker 1 has skill 5 >= 4 ✓
    #     Worker 2 has skill 2 < 4  ✗
    #
    #   Backtracking paths:
    #     Assign worker 0 to task 0 → assign worker 1 to task 1 → [0, 1] ✓
    #     Assign worker 1 to task 0 → try worker 0 for task 1 (skill 3 < 4) ✗
    #                               → try worker 2 for task 1 (skill 2 < 4) ✗
    #                               → no valid assignment
    #     Assign worker 2 to task 0 → assign worker 1 to task 1 → [2, 1] ✓
    #
    #   Result: [[0, 1], [2, 1]] ✓
    # -----------------------------------------------------------------------
    workers1 = [3, 5, 2]
    tasks1 = [2, 4]
    result1 = solution.assignTasks(workers1, tasks1)
    print("Example 1:")
    print(f"  workers = {workers1}")
    print(f"  tasks   = {tasks1}")
    print(f"  Output  = {result1}")
    print(f"  Expected: [[0, 1], [2, 1]]")
    print()

    # -----------------------------------------------------------------------
    # Example 2
    # workers = [4, 4], tasks = [3, 3]
    # Expected Output: [[0, 1], [1, 0]]
    #
    # Trace:
    #   Task 0 requires skill 3:
    #     Worker 0 has skill 4 >= 3 ✓
    #     Worker 1 has skill 4 >= 3 ✓
    #   Task 1 requires skill 3:
    #     Worker 0 has skill 4 >= 3 ✓
    #     Worker 1 has skill 4 >= 3 ✓
    #
    #   Backtracking paths:
    #     Assign worker 0 to task 0 → assign worker 1 to task 1 → [0, 1] ✓
    #     Assign worker 1 to task 0 → assign worker 0 to task 1 → [1, 0] ✓
    #
    #   Result: [[0, 1], [1, 0]] ✓
    # -----------------------------------------------------------------------
    workers2 = [4, 4]
    tasks2 = [3, 3]
    result2 = solution.assignTasks(workers2, tasks2)
    print("Example 2:")
    print(f"  workers = {workers2}")
    print(f"  tasks   = {tasks2}")
    print(f"  Output  = {result2}")
    print(f"  Expected: [[0, 1], [1, 0]]")
    print()

    # -----------------------------------------------------------------------
    # Additional Example: Edge case with one task and one worker
    # -----------------------------------------------------------------------
    workers3 = [5]
    tasks3 = [3]
    result3 = solution.assignTasks(workers3, tasks3)
    print("Additional Example (single worker, single task):")
    print(f"  workers = {workers3}")
    print(f"  tasks   = {tasks3}")
    print(f"  Output  = {result3}")
    print(f"  Expected: [[0]]")
    print()

    # -----------------------------------------------------------------------
    # Additional Example: Worker skill too low — no valid assignment
    # -----------------------------------------------------------------------
    workers4 = [1, 2]
    tasks4 = [5]
    result4 = solution.assignTasks(workers4, tasks4)
    print("Additional Example (no valid assignment):")
    print(f"  workers = {workers4}")
    print(f"  tasks   = {tasks4}")
    print(f"  Output  = {result4}")
    print(f"  Expected: []")
    print()

    # -----------------------------------------------------------------------
    # Additional Example: More workers than tasks
    # -----------------------------------------------------------------------
    workers5 = [3, 5, 4, 2]
    tasks5 = [2, 3]
    result5 = solution.assignTasks(workers5, tasks5)
    print("Additional Example (more workers than tasks):")
    print(f"  workers = {workers5}")
    print(f"  tasks   = {tasks5}")
    print(f"  Output  = {result5}")
    # Task 0 needs skill 2: workers 0(3), 1(5), 2(4), 3(2) all qualify
    # Task 1 needs skill 3: workers 0(3), 1(5), 2(4) qualify (not worker 3)
    # Valid pairs (w0, w1): (0,1),(0,2),(1,0),(1,2),(2,0),(2,1),(3,1),(3,2)
    print(f"  Expected: [[0, 1], [0, 2], [1, 0], [1, 2], [2, 0], [2, 1], [3, 1], [3, 2]]")