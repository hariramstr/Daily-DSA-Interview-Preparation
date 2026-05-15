```python
"""
Title: Task Scheduler with Cooldown and Priority
Difficulty: Medium
Topic: Heaps and Priority Queues

Problem Description:
You are managing a CPU that processes tasks one at a time. Each task has a priority value
and a release time — the earliest time step at which the task can begin execution.
Each task takes exactly 1 unit of time to complete. After completing a task, the CPU must
wait `cooldown` time steps before starting another task (i.e., if a task finishes at time `t`,
the next task can start at time `t + cooldown + 1` at the earliest).

Given a list of tasks where tasks[i] = [releaseTime, priority], and an integer cooldown,
simulate the CPU scheduling process using a greedy approach: at each available time step,
the CPU should execute the available task with the highest priority (ties broken by smallest
release time, then smallest index).

Return the order in which tasks are completed (as their 0-indexed positions in the input array).

Constraints:
- 1 <= tasks.length <= 10^4
- 0 <= releaseTime <= 10^9
- 1 <= priority <= 10^9
- 0 <= cooldown <= 100
"""

import heapq
from typing import List


class Solution:
    def schedule_tasks(self, tasks: List[List[int]], cooldown: int) -> List[int]:
        """
        Schedule tasks on a CPU with cooldown periods using a greedy max-heap approach.

        At each available CPU time slot, we pick the highest-priority task that has
        been released (release_time <= current_time). Ties are broken by smallest
        release time, then smallest original index.

        Args:
            tasks: List of [releaseTime, priority] pairs.
            cooldown: Number of idle time steps required after each task completes
                      before the CPU can start another task.

        Returns:
            List of 0-indexed task positions in the order they were completed.

        Time Complexity: O(N log N) where N = len(tasks).
            - Sorting tasks takes O(N log N).
            - Each task is pushed/popped from the heap once: O(N log N).

        Space Complexity: O(N) for the sorted list and the heap.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Prepare a sorted list of tasks by release time (then index)
        # so we can efficiently find which tasks become available at any given time.
        # We store (releaseTime, original_index) so we can iterate through them
        # in order of release time.
        # -----------------------------------------------------------------------
        # Create indexed tasks: (releaseTime, priority, original_index)
        indexed_tasks = [(rt, pri, i) for i, (rt, pri) in enumerate(tasks)]

        # Sort by release time first, then by original index (for stable ordering
        # when multiple tasks share the same release time).
        indexed_tasks.sort(key=lambda x: (x[0], x[2]))

        # -----------------------------------------------------------------------
        # STEP 2: Set up the max-heap (priority queue).
        #
        # Python's heapq is a MIN-heap, so to simulate a MAX-heap for priority,
        # we negate the priority value. We also negate the release time to break
        # ties correctly: among equal priorities, we want the SMALLEST release time,
        # so we store (-priority, release_time, original_index).
        # When heapq pops the smallest tuple, it gives us:
        #   - largest priority (because we negated it)
        #   - smallest release_time (natural ordering)
        #   - smallest original_index (natural ordering)
        # -----------------------------------------------------------------------
        heap: List[tuple] = []  # min-heap storing (-priority, releaseTime, index)

        # -----------------------------------------------------------------------
        # STEP 3: Initialize simulation variables.
        # -----------------------------------------------------------------------
        result: List[int] = []   # Will hold the order of completed task indices
        current_time: int = 0    # The current time step the CPU is free to start a task
        task_pointer: int = 0    # Pointer into indexed_tasks (sorted by release time)
        n: int = len(tasks)

        # -----------------------------------------------------------------------
        # STEP 4: Main simulation loop — continue until all tasks are scheduled.
        # -----------------------------------------------------------------------
        while len(result) < n:

            # -------------------------------------------------------------------
            # STEP 4a: Push all tasks that have been released by current_time
            # into the heap. We advance the task_pointer as long as the next
            # task's release time is <= current_time.
            # -------------------------------------------------------------------
            while task_pointer < n and indexed_tasks[task_pointer][0] <= current_time:
                rt, pri, idx = indexed_tasks[task_pointer]
                # Push as (-priority, releaseTime, original_index) for max-heap behavior
                heapq.heappush(heap, (-pri, rt, idx))
                task_pointer += 1

            # -------------------------------------------------------------------
            # STEP 4b: If the heap is empty, no task is available yet.
            # We must advance current_time to the release time of the next task.
            # This handles gaps where the CPU is idle waiting for a task.
            # -------------------------------------------------------------------
            if not heap:
                # Jump directly to the next task's release time
                current_time = indexed_tasks[task_pointer][0]
                # Now push all tasks available at this new time
                while task_pointer < n and indexed_tasks[task_pointer][0] <= current_time:
                    rt, pri, idx = indexed_tasks[task_pointer]
                    heapq.heappush(heap, (-pri, rt, idx))
                    task_pointer += 1

            # -------------------------------------------------------------------
            # STEP 4c: Pop the best available task from the heap.
            # The heap gives us the task with:
            #   - Highest priority (we stored -priority, so smallest = highest)
            #   - Smallest release time (tie-break)
            #   - Smallest original index (tie-break)
            # -------------------------------------------------------------------
            neg_pri, rt, idx = heapq.heappop(heap)

            # -------------------------------------------------------------------
            # STEP 4d: Execute the task.
            # The task starts at current_time and finishes at current_time
            # (since each task takes 1 unit of time).
            # Record the completed task's original index.
            # -------------------------------------------------------------------
            result.append(idx)

            # -------------------------------------------------------------------
            # STEP 4e: Apply the cooldown.
            # After finishing at current_time, the CPU is free again at:
            #   current_time + cooldown + 1
            # (cooldown idle steps + 1 to move past the finish time)
            # -------------------------------------------------------------------
            current_time = current_time + cooldown + 1

        # -----------------------------------------------------------------------
        # STEP 5: Return the completed order of task indices.
        # -----------------------------------------------------------------------
        return result


# =============================================================================
# TRACE-THROUGH VERIFICATION
# =============================================================================
# Example 1: tasks = [[0,3],[0,5],[2,2]], cooldown = 1
#
# indexed_tasks sorted by (releaseTime, index):
#   [(0, 3, 0), (0, 5, 1), (2, 2, 2)]
#
# Iteration 1: current_time = 0
#   Push tasks with releaseTime <= 0: (0,3,0) -> heap: (-3, 0, 0)
#                                      (0,5,1) -> heap: (-5, 0, 1), (-3, 0, 0)
#   task_pointer = 2 (task 2 has releaseTime=2 > 0, stop)
#   Heap not empty. Pop: (-5, 0, 1) -> task index 1 (priority 5)
#   result = [1]
#   current_time = 0 + 1 + 1 = 2
#
# Iteration 2: current_time = 2
#   Push tasks with releaseTime <= 2: (2,2,2) -> heap: (-3,0,0), (-2,2,2)
#   task_pointer = 3
#   Heap not empty. Pop: (-3, 0, 0) -> task index 0 (priority 3)
#   result = [1, 0]
#   current_time = 2 + 1 + 1 = 4
#
# Iteration 3: current_time = 4
#   No more tasks to push (task_pointer = 3 = n)
#   Heap not empty. Pop: (-2, 2, 2) -> task index 2 (priority 2)
#   result = [1, 0, 2]
#   current_time = 4 + 1 + 1 = 6
#
# Final result: [1, 0, 2] ✓ matches expected output
#
# =============================================================================
# Example 2: tasks = [[5,1],[0,2],[0,3]], cooldown = 0
#
# indexed_tasks sorted by (releaseTime, index):
#   [(0, 2, 1), (0, 3, 2), (5, 1, 0)]
#
# Iteration 1: current_time = 0
#   Push tasks with releaseTime <= 0: (0,2,1) -> heap: (-2, 0, 1)
#                                      (0,3,2) -> heap: (-3, 0, 2), (-2, 0, 1)
#   task_pointer = 2
#   Heap not empty. Pop: (-3, 0, 2) -> task index 2 (priority 3)
#   result = [2]
#   current_time = 0 + 0 + 1 = 1
#
# Iteration 2: current_time = 1
#   No new tasks (task 0 has releaseTime=5 > 1)
#   Heap not empty. Pop: (-2, 0, 1) -> task index 1 (priority 2)
#   result = [2, 1]
#   current_time = 1 + 0 + 1 = 2
#
# Iteration 3: current_time = 2
#   No new tasks (task 0 has releaseTime=5 > 2)
#   Heap is empty! Jump to next task's releaseTime = 5
#   current_time = 5
#   Push (5,1,0) -> heap: (-1, 5, 0)
#   Pop: (-1, 5, 0) -> task index 0 (priority 1)
#   result = [2, 1, 0]
#   current_time = 5 + 0 + 1 = 6
#
# Final result: [2, 1, 0] ✓ matches expected output
# =============================================================================


if __name__ == "__main__":
    solution = Solution()

    # ------------------------------------------------------------------
    # Example 1
    # ------------------------------------------------------------------
    tasks1 = [[0, 3], [0, 5], [2, 2]]
    cooldown1 = 1
    result1 = solution.schedule_tasks(tasks1, cooldown1)
    print("Example 1:")
    print(f"  Input:    tasks={tasks1}, cooldown={cooldown1}")
    print(f"  Output:   {result1}")
    print(f"  Expected: [1, 0, 2]")
    print(f"  Correct:  {result1 == [1, 0, 2]}")
    print()

    # ------------------------------------------------------------------
    # Example 2
    # ------------------------------------------------------------------
    tasks2 = [[5, 1], [0, 2], [0, 3]]
    cooldown2 = 0
    result2 = solution.schedule_tasks(tasks2, cooldown2)
    print("Example 2:")
    print(f"  Input:    tasks={tasks2}, cooldown={cooldown2}")
    print(f"  Output:   {result2}")
    print(f"  Expected: [2, 1, 0]")
    print(f"  Correct:  {result2 == [2, 1, 0]}")
    print()

    # ------------------------------------------------------------------
    # Additional edge case: single task
    # ------------------------------------------------------------------
    tasks3 = [[10, 7]]
    cooldown3 = 5
    result3 = solution.schedule_tasks(tasks3, cooldown3)
    print("Edge Case - Single Task:")
    print(f"  Input:    tasks={tasks3}, cooldown={cooldown3}")
    print(f"  Output:   {result3}")
    print(f"  Expected: [0]")
    print(f"  Correct:  {result3 == [0]}")
    print()

    # ------------------------------------------------------------------
    # Additional edge case: all tasks same priority, tie-break by release time
    # ------------------------------------------------------------------
    tasks4 = [[3, 5], [1, 5], [2, 5]]
    cooldown4 = 0
    result4 = solution.schedule_tasks(tasks4, cooldown4)
    print("Edge Case - Same Priority (tie-break by release time):")
    print(f"  Input:    tasks={tasks4}, cooldown={cooldown4}")
    print(f"  Output:   {result4}")
    # At t=1: only task 1 available -> pick task 1
    # At t=2: tasks 0 and 2 available -> pick task 2 (smaller release time=2 vs 3)
    # At t=3: task 0 available -> pick task 0
    print(f"  Expected: [1, 2, 0]")
    print(f"  Correct:  {result4 == [1, 2, 0]}")
    print()

    # ------------------------------------------------------------------
    # Additional edge case: same priority, same release time, tie-break by index
    # ------------------------------------------------------------------
    tasks5 = [[0, 5], [0, 5], [0, 5]]
    cooldown5 = 2
    result5 = solution.schedule_tasks(tasks5, cooldown5)
    print("Edge Case - Same Priority & Release Time (tie-break by index):")
    print(f"  Input:    tasks={tasks5}, cooldown={cooldown5}")
    print(f"  Output:   {result5}")
    # At t=0: all available, pick index 0 (smallest index). Next free at t=3.
    # At t=3: pick index 1. Next free at t=6.
    # At t=6: pick index 2.
    print(f"  Expected: [0, 1, 2]")
    print(f"  Correct:  {result5 == [0, 1, 2]}")
```