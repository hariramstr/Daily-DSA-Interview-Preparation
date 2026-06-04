/*
 * Title: Assign Tasks to Workers with Skill Constraints
 *
 * Problem Description:
 * You are managing a small team of workers, and you need to assign a list of tasks to them.
 * Each task requires a specific skill level (an integer), and each worker has a maximum skill
 * level they can handle. A worker can only be assigned a task if the task's required skill level
 * is less than or equal to the worker's skill level. Each worker can be assigned at most one task,
 * and each task must be assigned to exactly one worker.
 *
 * Given an array `workers` of length `m` representing each worker's skill level, and an array
 * `tasks` of length `n` representing each task's required skill level (where `n <= m`), return
 * all possible valid assignments as a list of lists. Each assignment is represented as a list of
 * length `n`, where the i-th element is the index (0-based) of the worker assigned to the i-th task.
 * The assignments should be returned in lexicographically sorted order.
 *
 * Constraints:
 * - 1 <= n <= m <= 8
 * - 1 <= workers[i], tasks[j] <= 10
 * - All worker indices in a single assignment must be distinct.
 *
 * Example 1:
 * Input: workers = [3, 5, 2], tasks = [2, 4]
 * Output: [[0, 1], [2, 1]]
 *
 * Example 2:
 * Input: workers = [4, 4], tasks = [3, 3]
 * Output: [[0, 1], [1, 0]]
 */

import java.util.*;

/**
 * Solution class for assigning tasks to workers with skill constraints using backtracking.
 */
public class Solution {

    /**
     * Finds all valid assignments of tasks to workers using backtracking.
     *
     * <p>The approach:
     * 1. We process tasks one by one (task index 0, 1, ..., n-1).
     * 2. For each task, we try assigning each worker (in order of worker index 0..m-1)
     *    who is not yet used and whose skill >= task's required skill.
     * 3. We recurse to assign the next task.
     * 4. After recursion, we backtrack (unmark the worker as used).
     * 5. Because we always iterate workers in ascending index order, the resulting
     *    assignments are naturally collected in lexicographic order.
     *
     * @param workers array of worker skill levels (length m)
     * @param tasks   array of task required skill levels (length n, n <= m)
     * @return list of all valid assignments in lexicographically sorted order;
     *         each assignment is a list of worker indices (one per task)
     *
     * Time complexity:  O(m! / (m-n)! * n) in the worst case (trying all permutations
     *                   of m workers taken n at a time), but pruned by skill constraints.
     * Space complexity: O(n * K) where K is the number of valid assignments stored,
     *                   plus O(n) recursion stack depth and O(m) for the used[] array.
     */
    public List<List<Integer>> assignTasks(int[] workers, int[] tasks) {
        // This list will accumulate all valid complete assignments
        List<List<Integer>> results = new ArrayList<>();

        // 'current' holds the worker index chosen for each task so far
        // current.get(i) = worker index assigned to task i
        List<Integer> current = new ArrayList<>();

        // 'used' tracks which workers have already been assigned in the current partial assignment
        boolean[] used = new boolean[workers.length];

        // Start the backtracking from task index 0
        backtrack(workers, tasks, 0, used, current, results);

        // The results are already in lexicographic order because we iterate workers
        // from index 0 upward at each step. But we sort explicitly to be safe.
        // Actually, since we always pick workers in ascending order at each task level,
        // the results come out lexicographically sorted naturally.
        // We add an explicit sort for correctness guarantee.
        results.sort((a, b) -> {
            // Compare two assignments lexicographically
            for (int i = 0; i < a.size(); i++) {
                if (!a.get(i).equals(b.get(i))) {
                    return a.get(i) - b.get(i);
                }
            }
            return 0;
        });

        return results;
    }

    /**
     * Recursive backtracking helper that assigns workers to tasks one task at a time.
     *
     * <p>At each call we are trying to assign a worker to {@code taskIndex}.
     * We iterate over all workers in ascending index order, skip those already used
     * or whose skill is insufficient, assign the qualifying worker, recurse for the
     * next task, then undo the assignment (backtrack).
     *
     * @param workers   array of worker skill levels
     * @param tasks     array of task required skill levels
     * @param taskIndex the index of the task we are currently trying to assign (0-based)
     * @param used      boolean array indicating which workers are already assigned
     * @param current   the partial assignment built so far (worker index per task)
     * @param results   the collection where complete valid assignments are stored
     *
     * Time complexity:  O(m^n) in the worst case before pruning.
     * Space complexity: O(n) for the recursion stack (depth = number of tasks).
     */
    private void backtrack(int[] workers, int[] tasks,
                           int taskIndex,
                           boolean[] used,
                           List<Integer> current,
                           List<List<Integer>> results) {

        // ---------------------------------------------------------------
        // BASE CASE: all tasks have been assigned
        // ---------------------------------------------------------------
        if (taskIndex == tasks.length) {
            // We have a complete, valid assignment — save a copy of it
            results.add(new ArrayList<>(current));
            return; // backtrack to explore other possibilities
        }

        // ---------------------------------------------------------------
        // RECURSIVE CASE: try assigning each worker to tasks[taskIndex]
        // ---------------------------------------------------------------
        int requiredSkill = tasks[taskIndex]; // skill needed for the current task

        // Iterate workers in ascending index order (0, 1, 2, ..., m-1)
        // This ensures that when we collect results they are in lex order
        for (int workerIdx = 0; workerIdx < workers.length; workerIdx++) {

            // --- PRUNING CHECK 1: skip workers already used in this assignment ---
            if (used[workerIdx]) {
                continue; // this worker is busy with another task
            }

            // --- PRUNING CHECK 2: skip workers whose skill is insufficient ---
            if (workers[workerIdx] < requiredSkill) {
                continue; // worker cannot handle this task
            }

            // ---- CHOOSE: assign workerIdx to taskIndex ----
            used[workerIdx] = true;       // mark worker as occupied
            current.add(workerIdx);       // record this choice in the partial assignment

            // ---- EXPLORE: recurse to assign the next task ----
            backtrack(workers, tasks, taskIndex + 1, used, current, results);

            // ---- UN-CHOOSE (BACKTRACK): undo the assignment ----
            used[workerIdx] = false;                    // free the worker
            current.remove(current.size() - 1);        // remove last element
        }
        // After the loop, all worker choices for taskIndex have been explored
    }

    // ===================================================================
    // MAIN METHOD — demonstrates the solution with the provided examples
    // ===================================================================

    /**
     * Entry point: runs example test cases and prints results.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // ------------------------------------------------------------------
        // Example 1
        // workers = [3, 5, 2]  (worker 0 skill=3, worker 1 skill=5, worker 2 skill=2)
        // tasks   = [2, 4]     (task 0 needs skill>=2, task 1 needs skill>=4)
        //
        // Task 0 (skill 2): workers 0 (skill 3 ✓), 1 (skill 5 ✓), 2 (skill 2 ✓)
        // Task 1 (skill 4): workers 0 (skill 3 ✗), 1 (skill 5 ✓), 2 (skill 2 ✗)
        //
        // Backtracking trace:
        //   taskIndex=0, try worker 0 (skill 3 >= 2 ✓) → current=[0]
        //     taskIndex=1, try worker 0 (used), try worker 1 (skill 5 >= 4 ✓) → current=[0,1]
        //       taskIndex=2 == tasks.length → save [0,1]
        //     backtrack → current=[0]
        //     try worker 2 (skill 2 < 4 ✗)
        //   backtrack → current=[]
        //   taskIndex=0, try worker 1 (skill 5 >= 2 ✓) → current=[1]
        //     taskIndex=1, try worker 0 (skill 3 < 4 ✗), try worker 1 (used), try worker 2 (skill 2 < 4 ✗)
        //     → no valid assignment
        //   backtrack → current=[]
        //   taskIndex=0, try worker 2 (skill 2 >= 2 ✓) → current=[2]
        //     taskIndex=1, try worker 0 (skill 3 < 4 ✗), try worker 1 (skill 5 >= 4 ✓) → current=[2,1]
        //       taskIndex=2 == tasks.length → save [2,1]
        //     backtrack → current=[2]
        //     try worker 2 (used)
        //   backtrack → current=[]
        //
        // Result: [[0,1],[2,1]]  ✓ matches expected output
        // ------------------------------------------------------------------
        int[] workers1 = {3, 5, 2};
        int[] tasks1   = {2, 4};
        List<List<Integer>> result1 = solution.assignTasks(workers1, tasks1);
        System.out.println("Example 1:");
        System.out.println("workers = [3, 5, 2], tasks = [2, 4]");
        System.out.println("Expected: [[0, 1], [2, 1]]");
        System.out.println("Got:      " + result1);
        System.out.println();

        // ------------------------------------------------------------------
        // Example 2
        // workers = [4, 4]  (worker 0 skill=4, worker 1 skill=4)
        // tasks   = [3, 3]  (task 0 needs skill>=3, task 1 needs skill>=3)
        //
        // Both workers qualify for both tasks.
        //
        // Backtracking trace:
        //   taskIndex=0, try worker 0 (skill 4 >= 3 ✓) → current=[0]
        //     taskIndex=1, try worker 0 (used), try worker 1 (skill 4 >= 3 ✓) → current=[0,1]
        //       taskIndex=2 == tasks.length → save [0,1]
        //     backtrack → current=[0]
        //   backtrack → current=[]
        //   taskIndex=0, try worker 1 (skill 4 >= 3 ✓) → current=[1]
        //     taskIndex=1, try worker 0 (skill 4 >= 3 ✓) → current=[1,0]
        //       taskIndex=2 == tasks.length → save [1,0]
        //     backtrack → current=[1]
        //     try worker 1 (used)
        //   backtrack → current=[]
        //
        // Result: [[0,1],[1,0]]  ✓ matches expected output
        // ------------------------------------------------------------------
        int[] workers2 = {4, 4};
        int[] tasks2   = {3, 3};
        List<List<Integer>> result2 = solution.assignTasks(workers2, tasks2);
        System.out.println("Example 2:");
        System.out.println("workers = [4, 4], tasks = [3, 3]");
        System.out.println("Expected: [[0, 1], [1, 0]]");
        System.out.println("Got:      " + result2);
        System.out.println();

        // ------------------------------------------------------------------
        // Extra Example 3: no valid assignment possible
        // workers = [1, 2], tasks = [5]
        // No worker has skill >= 5, so result should be empty.
        // ------------------------------------------------------------------
        int[] workers3 = {1, 2};
        int[] tasks3   = {5};
        List<List<Integer>> result3 = solution.assignTasks(workers3, tasks3);
        System.out.println("Example 3 (no valid assignment):");
        System.out.println("workers = [1, 2], tasks = [5]");
        System.out.println("Expected: []");
        System.out.println("Got:      " + result3);
        System.out.println();

        // ------------------------------------------------------------------
        // Extra Example 4: single worker, single task
        // workers = [3], tasks = [3]
        // Worker 0 (skill 3) can handle task 0 (skill 3). Result: [[0]]
        // ------------------------------------------------------------------
        int[] workers4 = {3};
        int[] tasks4   = {3};
        List<List<Integer>> result4 = solution.assignTasks(workers4, tasks4);
        System.out.println("Example 4 (single worker, single task):");
        System.out.println("workers = [3], tasks = [3]");
        System.out.println("Expected: [[0]]");
        System.out.println("Got:      " + result4);
        System.out.println();

        // ------------------------------------------------------------------
        // Extra Example 5: more workers than tasks
        // workers = [5, 3, 4], tasks = [2]
        // All three workers qualify (skill >= 2). Result: [[0],[1],[2]]
        // ------------------------------------------------------------------
        int[] workers5 = {5, 3, 4};
        int[] tasks5   = {2};
        List<List<Integer>> result5 = solution.assignTasks(workers5, tasks5);
        System.out.println("Example 5 (all workers qualify for single task):");
        System.out.println("workers = [5, 3, 4], tasks = [2]");
        System.out.println("Expected: [[0], [1], [2]]");
        System.out.println("Got:      " + result5);
    }
}