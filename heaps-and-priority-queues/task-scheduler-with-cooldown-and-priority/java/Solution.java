/*
 * Task Scheduler with Cooldown and Priority
 * ==========================================
 * You are managing a CPU that processes tasks one at a time. Each task has a priority value
 * and a release time — the earliest time step at which the task can begin execution.
 * Each task takes exactly 1 unit of time to complete. After completing a task, the CPU must
 * wait `cooldown` time steps before starting another task (i.e., if a task finishes at time t,
 * the next task can start at time t + cooldown + 1 at the earliest).
 *
 * Given a list of tasks where tasks[i] = [releaseTime, priority], and an integer cooldown,
 * simulate the CPU scheduling process using a greedy approach: at each available time step,
 * the CPU should execute the available task with the highest priority (ties broken by smallest
 * release time, then smallest index).
 *
 * Return the order in which tasks are completed (as their 0-indexed positions in the input array).
 *
 * Constraints:
 * - 1 <= tasks.length <= 10^4
 * - 0 <= releaseTime <= 10^9
 * - 1 <= priority <= 10^9
 * - 0 <= cooldown <= 100
 */

import java.util.*;

/**
 * Solution class for the Task Scheduler with Cooldown and Priority problem.
 * Uses a greedy simulation with a min-heap (sorted by release time) and a max-heap
 * (priority queue sorted by priority, then release time, then index) to efficiently
 * determine which task to execute at each CPU-available time step.
 */
public class Solution {

    /**
     * Schedules tasks on a CPU with cooldown constraints, always picking the highest-priority
     * available task at each free time slot.
     *
     * <p>Algorithm Overview:
     * 1. Sort tasks by release time (keeping original indices).
     * 2. Simulate time steps where the CPU becomes free.
     * 3. At each free time, move all tasks whose releaseTime <= currentTime into a max-heap.
     * 4. Pick the top of the max-heap (highest priority; tie-break by release time, then index).
     * 5. Record the completed task's original index, advance time by 1 (task duration) + cooldown.
     * 6. If no task is available, jump time forward to the next task's release time.
     *
     * @param tasks    2D array where tasks[i] = {releaseTime, priority}
     * @param cooldown number of idle time steps required after each task completes
     * @return list of original 0-based indices in the order tasks are completed
     *
     * Time Complexity:  O(n log n) — sorting + each task is pushed/popped from the heap once
     * Space Complexity: O(n) — for the sorted list and the priority queue
     */
    public List<Integer> scheduleTasks(int[][] tasks, int cooldown) {

        int n = tasks.length;

        // -----------------------------------------------------------------------
        // Step 1: Create an array of indices [0, 1, 2, ..., n-1] so we can track
        //         original positions after sorting.
        // -----------------------------------------------------------------------
        Integer[] indices = new Integer[n];
        for (int i = 0; i < n; i++) {
            indices[i] = i;
        }

        // -----------------------------------------------------------------------
        // Step 2: Sort indices by release time.
        //         If two tasks have the same release time, sort by index (stable).
        //         This lets us efficiently "release" tasks as time advances.
        // -----------------------------------------------------------------------
        Arrays.sort(indices, (a, b) -> {
            if (tasks[a][0] != tasks[b][0]) {
                return Integer.compare(tasks[a][0], tasks[b][0]); // earlier release first
            }
            return Integer.compare(a, b); // smaller index first (tie-break)
        });

        // -----------------------------------------------------------------------
        // Step 3: Build a max-heap (priority queue) for tasks that are currently
        //         available to run.
        //
        //         Comparator logic (we want the BEST task at the top):
        //           - Higher priority  → comes first  (descending priority)
        //           - Tie in priority  → smaller releaseTime comes first
        //           - Tie in both      → smaller original index comes first
        // -----------------------------------------------------------------------
        PriorityQueue<Integer> availableHeap = new PriorityQueue<>((a, b) -> {
            // Compare by priority descending
            if (tasks[a][1] != tasks[b][1]) {
                return Integer.compare(tasks[b][1], tasks[a][1]); // higher priority first
            }
            // Tie-break: smaller release time first
            if (tasks[a][0] != tasks[b][0]) {
                return Integer.compare(tasks[a][0], tasks[b][0]);
            }
            // Tie-break: smaller original index first
            return Integer.compare(a, b);
        });

        // -----------------------------------------------------------------------
        // Step 4: Prepare result list and simulation variables.
        //         - result      : stores original indices in completion order
        //         - currentTime : the earliest time the CPU can start a new task
        //         - pointer     : index into the sorted `indices` array (next unreleased task)
        // -----------------------------------------------------------------------
        List<Integer> result = new ArrayList<>();
        long currentTime = 0; // use long to avoid overflow with large releaseTimes + cooldown
        int pointer = 0;      // points to the next task (in sorted order) not yet in the heap

        // -----------------------------------------------------------------------
        // Step 5: Main simulation loop — repeat until all tasks are scheduled.
        // -----------------------------------------------------------------------
        while (result.size() < n) {

            // -------------------------------------------------------------------
            // Step 5a: Move all tasks whose releaseTime <= currentTime into the
            //          available heap. These tasks are now eligible to run.
            // -------------------------------------------------------------------
            while (pointer < n && tasks[indices[pointer]][0] <= currentTime) {
                availableHeap.offer(indices[pointer]);
                pointer++;
            }

            // -------------------------------------------------------------------
            // Step 5b: If no task is available yet, the CPU must idle.
            //          Jump currentTime forward to the release time of the next
            //          task (the one at `pointer` in sorted order).
            //          This avoids simulating every idle time step one by one.
            // -------------------------------------------------------------------
            if (availableHeap.isEmpty()) {
                // No task is ready; fast-forward to when the next task is released.
                currentTime = tasks[indices[pointer]][0];

                // Now release all tasks that become available at this new time.
                while (pointer < n && tasks[indices[pointer]][0] <= currentTime) {
                    availableHeap.offer(indices[pointer]);
                    pointer++;
                }
            }

            // -------------------------------------------------------------------
            // Step 5c: Pick the best available task (top of the max-heap).
            //          Execute it: record its original index in the result.
            // -------------------------------------------------------------------
            int chosenIndex = availableHeap.poll(); // original index of the chosen task
            result.add(chosenIndex);

            // -------------------------------------------------------------------
            // Step 5d: Advance time.
            //          The task takes 1 unit of time, then cooldown units of idle.
            //          So the CPU is next free at: currentTime + 1 + cooldown.
            // -------------------------------------------------------------------
            currentTime = currentTime + 1 + cooldown;
        }

        return result;
    }

    // ===========================================================================
    // Main method — demonstrates the solution with the provided examples
    // ===========================================================================

    /**
     * Entry point. Runs the scheduler on example inputs and prints results.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        Solution sol = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        //   tasks = [[0,3],[0,5],[2,2]], cooldown = 1
        //   Expected output: [1, 0, 2]
        //
        //   Trace:
        //   - currentTime = 0: tasks 0 (rel=0,pri=3) and 1 (rel=0,pri=5) available.
        //     Heap top = task 1 (priority 5). Execute task 1. result=[1].
        //     currentTime = 0 + 1 + 1 = 2.
        //   - currentTime = 2: task 0 (rel=0,pri=3) and task 2 (rel=2,pri=2) available.
        //     Heap top = task 0 (priority 3 > 2). Execute task 0. result=[1,0].
        //     currentTime = 2 + 1 + 1 = 4.
        //   - currentTime = 4: task 2 (rel=2,pri=2) available.
        //     Execute task 2. result=[1,0,2].
        // -----------------------------------------------------------------------
        int[][] tasks1 = {{0, 3}, {0, 5}, {2, 2}};
        int cooldown1 = 1;
        List<Integer> result1 = sol.scheduleTasks(tasks1, cooldown1);
        System.out.println("Example 1:");
        System.out.println("Input tasks: [[0,3],[0,5],[2,2]], cooldown = 1");
        System.out.println("Output: " + result1);          // Expected: [1, 0, 2]
        System.out.println("Expected: [1, 0, 2]");
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2:
        //   tasks = [[5,1],[0,2],[0,3]], cooldown = 0
        //   Expected output: [2, 1, 0]
        //
        //   Trace:
        //   - currentTime = 0: tasks 1 (rel=0,pri=2) and 2 (rel=0,pri=3) available.
        //     Heap top = task 2 (priority 3). Execute task 2. result=[2].
        //     currentTime = 0 + 1 + 0 = 1.
        //   - currentTime = 1: task 1 (rel=0,pri=2) available. Task 0 not yet (rel=5).
        //     Execute task 1. result=[2,1].
        //     currentTime = 1 + 1 + 0 = 2.
        //   - currentTime = 2: heap empty, next task is task 0 (rel=5).
        //     Jump to currentTime = 5. Execute task 0. result=[2,1,0].
        // -----------------------------------------------------------------------
        int[][] tasks2 = {{5, 1}, {0, 2}, {0, 3}};
        int cooldown2 = 0;
        List<Integer> result2 = sol.scheduleTasks(tasks2, cooldown2);
        System.out.println("Example 2:");
        System.out.println("Input tasks: [[5,1],[0,2],[0,3]], cooldown = 0");
        System.out.println("Output: " + result2);          // Expected: [2, 1, 0]
        System.out.println("Expected: [2, 1, 0]");
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 3: Single task
        //   tasks = [[3,7]], cooldown = 5
        //   Expected output: [0]
        // -----------------------------------------------------------------------
        int[][] tasks3 = {{3, 7}};
        int cooldown3 = 5;
        List<Integer> result3 = sol.scheduleTasks(tasks3, cooldown3);
        System.out.println("Example 3 (single task):");
        System.out.println("Input tasks: [[3,7]], cooldown = 5");
        System.out.println("Output: " + result3);          // Expected: [0]
        System.out.println("Expected: [0]");
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 4: All tasks released at time 0, no cooldown
        //   tasks = [[0,1],[0,4],[0,2],[0,3]], cooldown = 0
        //   Sorted by priority desc: task1(4), task3(3), task2(2), task0(1)
        //   Expected output: [1, 3, 2, 0]
        // -----------------------------------------------------------------------
        int[][] tasks4 = {{0, 1}, {0, 4}, {0, 2}, {0, 3}};
        int cooldown4 = 0;
        List<Integer> result4 = sol.scheduleTasks(tasks4, cooldown4);
        System.out.println("Example 4 (all at t=0, no cooldown):");
        System.out.println("Input tasks: [[0,1],[0,4],[0,2],[0,3]], cooldown = 0");
        System.out.println("Output: " + result4);          // Expected: [1, 3, 2, 0]
        System.out.println("Expected: [1, 3, 2, 0]");
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 5: Tie-breaking by release time then index
        //   tasks = [[0,5],[1,5],[0,5]], cooldown = 0
        //   All priority=5. At t=0: tasks 0 (rel=0) and 2 (rel=0) available.
        //     Tie in priority and release time → pick smaller index → task 0.
        //   At t=1: tasks 1 (rel=1) and 2 (rel=0) available.
        //     Tie in priority → pick smaller release time → task 2 (rel=0).
        //   At t=2: task 1 available. Execute task 1.
        //   Expected output: [0, 2, 1]
        // -----------------------------------------------------------------------
        int[][] tasks5 = {{0, 5}, {1, 5}, {0, 5}};
        int cooldown5 = 0;
        List<Integer> result5 = sol.scheduleTasks(tasks5, cooldown5);
        System.out.println("Example 5 (tie-breaking):");
        System.out.println("Input tasks: [[0,5],[1,5],[0,5]], cooldown = 0");
        System.out.println("Output: " + result5);          // Expected: [0, 2, 1]
        System.out.println("Expected: [0, 2, 1]");
    }
}