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
 * Given an array `workers` of length m and an array `tasks` of length n (n <= m),
 * return all possible valid assignments as a list of lists in lexicographically sorted order.
 * Each assignment is a list of length n where the i-th element is the 0-based worker index
 * assigned to the i-th task.
 *
 * Constraints:
 *   1 <= n <= m <= 8
 *   1 <= workers[i], tasks[j] <= 10
 *   All worker indices in a single assignment must be distinct.
 *
 * Example 1:
 *   Input:  workers = [3, 5, 2], tasks = [2, 4]
 *   Output: [[0, 1], [2, 1]]
 *
 * Example 2:
 *   Input:  workers = [4, 4], tasks = [3, 3]
 *   Output: [[0, 1], [1, 0]]
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class containing the backtracking algorithm
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    // -------------------------------------------------------------------------
    // Method: AssignTasks
    //
    // Time Complexity:  O(m! / (m-n)! * n)  — in the worst case we try every
    //                   permutation of n workers chosen from m workers, and for
    //                   each complete assignment we copy n elements.
    //                   With m,n <= 8 this is at most 8! = 40 320 paths.
    //
    // Space Complexity: O(n)  for the recursion stack depth (one frame per task)
    //                   plus O(R * n) for storing R result assignments.
    // -------------------------------------------------------------------------
    public List<List<int>> AssignTasks(int[] workers, int[] tasks)
    {
        // ── Step 1: Prepare the result container ──────────────────────────────
        // We will collect every valid complete assignment here.
        // A "complete assignment" means every task has been assigned a worker.
        List<List<int>> results = new List<List<int>>();

        // ── Step 2: Prepare a "current assignment" buffer ─────────────────────
        // currentAssignment[i] will hold the worker index chosen for task i.
        // We build this incrementally during backtracking.
        int[] currentAssignment = new int[tasks.Length];

        // ── Step 3: Track which workers are already used ──────────────────────
        // usedWorkers[w] == true means worker w has already been assigned to an
        // earlier task in the current partial assignment.
        // Using a boolean array is O(1) per lookup/update, which is ideal here.
        bool[] usedWorkers = new bool[workers.Length];

        // ── Step 4: Launch the recursive backtracking ─────────────────────────
        // We start at task index 0 and try to assign workers one task at a time.
        Backtrack(
            workers,
            tasks,
            taskIndex: 0,          // start with the very first task
            currentAssignment,
            usedWorkers,
            results
        );

        // ── Step 5: Sort results lexicographically ────────────────────────────
        // The problem requires the output in lexicographically sorted order.
        // Because we always iterate worker indices from 0 upward (smallest first),
        // the results are already generated in lexicographic order.
        // We sort explicitly here to guarantee correctness regardless of worker
        // iteration order, and to make the contract crystal-clear.
        results.Sort((a, b) =>
        {
            // Compare two assignments element by element (like dictionary order)
            for (int i = 0; i < a.Count; i++)
            {
                if (a[i] != b[i])
                    return a[i].CompareTo(b[i]); // first differing position decides
            }
            return 0; // they are identical (shouldn't happen with distinct assignments)
        });

        return results;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helper: Backtrack
    //
    // This is the heart of the algorithm.  It works by making a choice for the
    // current task, recursing to handle the remaining tasks, and then undoing
    // the choice (backtracking) so we can try the next option.
    //
    // Parameters:
    //   workers           – skill levels of all workers
    //   tasks             – required skill levels of all tasks
    //   taskIndex         – the index of the task we are currently trying to assign
    //   currentAssignment – partial assignment built so far (indices 0..taskIndex-1 filled)
    //   usedWorkers       – which workers are already taken in this partial assignment
    //   results           – accumulator for complete valid assignments
    // ─────────────────────────────────────────────────────────────────────────
    private void Backtrack(
        int[] workers,
        int[] tasks,
        int taskIndex,
        int[] currentAssignment,
        bool[] usedWorkers,
        List<List<int>> results)
    {
        // ── Base case: all tasks have been assigned ───────────────────────────
        // When taskIndex equals the number of tasks, we have successfully assigned
        // every task to a distinct, qualified worker.  Record this assignment.
        if (taskIndex == tasks.Length)
        {
            // Copy the array into a new List<int> so the stored result is
            // independent of future changes to currentAssignment.
            results.Add(new List<int>(currentAssignment));
            return; // backtrack to explore other possibilities
        }

        // ── Recursive case: try assigning each worker to the current task ─────
        // We iterate worker indices from 0 to workers.Length-1 (ascending order).
        // Ascending iteration is what gives us lexicographic ordering naturally.
        for (int workerIndex = 0; workerIndex < workers.Length; workerIndex++)
        {
            // ── Pruning condition 1: worker already used ──────────────────────
            // Each worker can handle at most one task, so skip workers that are
            // already committed to an earlier task in this partial assignment.
            if (usedWorkers[workerIndex])
                continue;

            // ── Pruning condition 2: worker lacks the required skill ───────────
            // The worker's skill level must be >= the task's required skill level.
            // If not, this worker simply cannot do this task — skip them.
            if (workers[workerIndex] < tasks[taskIndex])
                continue;

            // ── Make the choice ───────────────────────────────────────────────
            // Assign workerIndex to the current task and mark the worker as used.
            currentAssignment[taskIndex] = workerIndex;
            usedWorkers[workerIndex] = true;

            // ── Recurse ───────────────────────────────────────────────────────
            // Move on to the next task (taskIndex + 1) with the updated state.
            Backtrack(workers, tasks, taskIndex + 1, currentAssignment, usedWorkers, results);

            // ── Undo the choice (backtrack) ───────────────────────────────────
            // After the recursive call returns, we "un-assign" this worker so
            // we can try the next candidate worker for the same task.
            // Note: we don't need to reset currentAssignment[taskIndex] because
            // it will be overwritten before it is read again.
            usedWorkers[workerIndex] = false;
        }
        // After the loop, all worker candidates for this task have been tried.
        // We simply return, which causes the caller to try the next worker for
        // the previous task (i.e., we backtrack one level up).
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / test code  (top-level statements — no explicit Main needed in .NET 6+)
// ─────────────────────────────────────────────────────────────────────────────

Solution solution = new Solution();

// Helper: pretty-print a list of lists
static void PrintResult(List<List<int>> result)
{
    Console.Write("[");
    for (int i = 0; i < result.Count; i++)
    {
        Console.Write("[" + string.Join(", ", result[i]) + "]");
        if (i < result.Count - 1) Console.Write(", ");
    }
    Console.WriteLine("]");
}

// ── Example 1 ─────────────────────────────────────────────────────────────────
// workers = [3, 5, 2],  tasks = [2, 4]
// Expected output: [[0, 1], [2, 1]]
//
// Trace:
//   Task 0 requires skill 2.  Eligible workers: 0 (skill 3), 1 (skill 5), 2 (skill 2).
//   Task 1 requires skill 4.  Eligible workers: 1 (skill 5).
//
//   Try worker 0 for task 0 → try worker 1 for task 1 → complete: [0, 1] ✓
//   Try worker 1 for task 0 → try worker 0 for task 1 → skill 3 < 4, skip
//                           → worker 1 used, skip
//                           → worker 2 for task 1 → skill 2 < 4, skip  → dead end
//   Try worker 2 for task 0 → try worker 1 for task 1 → complete: [2, 1] ✓
//
//   Result: [[0,1],[2,1]]  ✓
Console.WriteLine("=== Example 1 ===");
Console.WriteLine("workers = [3, 5, 2],  tasks = [2, 4]");
Console.Write("Output:   ");
PrintResult(solution.AssignTasks(new int[] { 3, 5, 2 }, new int[] { 2, 4 }));
Console.WriteLine("Expected: [[0, 1], [2, 1]]");
Console.WriteLine();

// ── Example 2 ─────────────────────────────────────────────────────────────────
// workers = [4, 4],  tasks = [3, 3]
// Expected output: [[0, 1], [1, 0]]
//
// Trace:
//   Task 0 requires skill 3.  Eligible workers: 0 (skill 4), 1 (skill 4).
//   Task 1 requires skill 3.  Eligible workers: 0 (skill 4), 1 (skill 4).
//
//   Try worker 0 for task 0 → try worker 1 for task 1 → complete: [0, 1] ✓
//   Try worker 1 for task 0 → try worker 0 for task 1 → complete: [1, 0] ✓
//
//   Result: [[0,1],[1,0]]  ✓
Console.WriteLine("=== Example 2 ===");
Console.WriteLine("workers = [4, 4],  tasks = [3, 3]");
Console.Write("Output:   ");
PrintResult(solution.AssignTasks(new int[] { 4, 4 }, new int[] { 3, 3 }));
Console.WriteLine("Expected: [[0, 1], [1, 0]]");
Console.WriteLine();

// ── Extra Example: no valid assignment ────────────────────────────────────────
// workers = [1, 2],  tasks = [5]
// No worker has skill >= 5, so the result should be empty: []
Console.WriteLine("=== Extra: no valid assignment ===");
Console.WriteLine("workers = [1, 2],  tasks = [5]");
Console.Write("Output:   ");
PrintResult(solution.AssignTasks(new int[] { 1, 2 }, new int[] { 5 }));
Console.WriteLine("Expected: []");
Console.WriteLine();

// ── Extra Example: single worker, single task ─────────────────────────────────
// workers = [3],  tasks = [3]
// Expected: [[0]]
Console.WriteLine("=== Extra: single worker, single task ===");
Console.WriteLine("workers = [3],  tasks = [3]");
Console.Write("Output:   ");
PrintResult(solution.AssignTasks(new int[] { 3 }, new int[] { 3 }));
Console.WriteLine("Expected: [[0]]");