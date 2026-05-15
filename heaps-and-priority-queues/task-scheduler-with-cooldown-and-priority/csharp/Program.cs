/*
 * Task Scheduler with Cooldown and Priority
 * ==========================================
 * You are managing a CPU that processes tasks one at a time.
 * Each task has a priority value and a release time — the earliest time step
 * at which the task can begin execution. Each task takes exactly 1 unit of time.
 * After completing a task, the CPU must wait `cooldown` time steps before
 * starting another task (i.e., if a task finishes at time t, the next task
 * can start at time t + cooldown + 1 at the earliest).
 *
 * Given tasks[i] = [releaseTime, priority] and an integer cooldown,
 * simulate the CPU scheduling process using a greedy approach:
 * at each available time step, execute the available task with the highest priority
 * (ties broken by smallest release time, then smallest index).
 *
 * Return the order in which tasks are completed (0-indexed positions in input array).
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution Class
// ─────────────────────────────────────────────────────────────────────────────
class Solution
{
    /// <summary>
    /// Schedules tasks on a CPU with cooldown periods using a greedy max-heap approach.
    ///
    /// Time Complexity:  O(N log N) — sorting tasks + each task is pushed/popped from
    ///                   the priority queue once, each operation is O(log N).
    /// Space Complexity: O(N) — for the priority queue and result list.
    /// </summary>
    public List<int> ScheduleTasks(int[][] tasks, int cooldown)
    {
        // ── Step 1: Record original indices before any sorting ──────────────
        // We need to return results as 0-based indices into the ORIGINAL input array.
        // We'll attach each task's original index to it so we don't lose track.
        int n = tasks.Length;

        // Create an array of (releaseTime, priority, originalIndex) tuples.
        // Storing the original index lets us output results in terms of the
        // original task positions even after we sort by release time.
        var indexed = new (int release, int priority, int idx)[n];
        for (int i = 0; i < n; i++)
            indexed[i] = (tasks[i][0], tasks[i][1], i);

        // ── Step 2: Sort tasks by release time (ascending) ──────────────────
        // We process time chronologically. Sorting by release time lets us
        // efficiently "release" tasks into the ready queue as time advances.
        // Ties in release time are broken by original index (ascending) so that
        // when two tasks become available at the same moment, the one with the
        // smaller original index is considered first for tie-breaking in the heap.
        Array.Sort(indexed, (a, b) =>
            a.release != b.release ? a.release.CompareTo(b.release) : a.idx.CompareTo(b.idx));

        // ── Step 3: Set up a max-heap (priority queue) for ready tasks ───────
        // The priority queue holds tasks that have been released and are waiting
        // to be executed. We want to always pick the task with:
        //   1. Highest priority  (primary key, descending)
        //   2. Smallest release time (secondary key, ascending)
        //   3. Smallest original index (tertiary key, ascending)
        //
        // C#'s PriorityQueue<TElement, TPriority> is a MIN-heap by default.
        // To simulate a MAX-heap we negate the priority value, and for tie-breaking
        // we encode all three keys into a comparable tuple.
        //
        // We store the element as the original index (int) and the priority key
        // as a tuple (-priority, releaseTime, originalIndex) so the smallest
        // tuple in the min-heap corresponds to the task we actually want first.
        var readyQueue = new PriorityQueue<int, (int negPriority, int release, int idx)>();

        // ── Step 4: Initialise simulation state ──────────────────────────────
        var result = new List<int>();   // Will hold completed task indices in order
        long currentTime = 0;           // The current time step the CPU is free at
        int sortedPtr = 0;              // Pointer into the sorted `indexed` array

        // ── Step 5: Main simulation loop ─────────────────────────────────────
        // We continue until every task has been scheduled and completed.
        while (result.Count < n)
        {
            // ── 5a: Advance time if the ready queue is empty ─────────────────
            // If no task is currently available (ready queue is empty), the CPU
            // must idle until the next task is released.
            // We jump time forward to the release time of the next unreleased task
            // (if there is one) so we don't spin through idle cycles one by one.
            if (readyQueue.Count == 0 && sortedPtr < n)
            {
                // Jump to whichever is later: the next task's release time, or
                // the current CPU-free time (in case cooldown pushed us past the release).
                currentTime = Math.Max(currentTime, indexed[sortedPtr].release);
            }

            // ── 5b: Release all tasks whose release time <= currentTime ───────
            // Any task that has been released by now is eligible to run.
            // We add them all to the ready queue so the greedy pick can choose
            // the best one among all currently available tasks.
            while (sortedPtr < n && indexed[sortedPtr].release <= currentTime)
            {
                var t = indexed[sortedPtr];
                // Push with composite priority key: (-priority, release, originalIndex)
                // The min-heap will surface the item with the smallest key first,
                // which corresponds to highest priority → smallest release → smallest index.
                readyQueue.Enqueue(t.idx, (-t.priority, t.release, t.idx));
                sortedPtr++;
            }

            // ── 5c: Pick and execute the best available task ─────────────────
            if (readyQueue.Count > 0)
            {
                // Dequeue the highest-priority available task.
                int chosenIdx = readyQueue.Dequeue();

                // Record this task as completed (in completion order).
                result.Add(chosenIdx);

                // The task executes at `currentTime` and finishes at `currentTime`
                // (takes 1 unit). After the cooldown, the CPU is free at:
                //   currentTime + 1 (finish) + cooldown (wait) = currentTime + cooldown + 1
                currentTime = currentTime + cooldown + 1;
            }
            // If readyQueue is still empty here, the while-loop guard will re-enter
            // and the time-jump in 5a will advance us to the next release time.
        }

        return result;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Test Code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

var solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// tasks = [[0,3],[0,5],[2,2]], cooldown = 1
// Expected output: [1, 0, 2]
//
// Trace:
//   currentTime=0: release tasks 0 (r=0,p=3) and 1 (r=0,p=5). Heap: {1,0}.
//     Pick task 1 (priority 5). result=[1]. nextFree = 0+1+1 = 2.
//   currentTime=2: release task 2 (r=2,p=2). Heap: {0,2}.
//     Pick task 0 (priority 3 > 2). result=[1,0]. nextFree = 2+1+1 = 4.
//   currentTime=4: Heap: {2}.
//     Pick task 2. result=[1,0,2]. Done.
int[][] tasks1 = [[0, 3], [0, 5], [2, 2]];
int cooldown1 = 1;
List<int> result1 = solution.ScheduleTasks(tasks1, cooldown1);
Console.WriteLine("Example 1:");
Console.WriteLine($"  Input:    tasks = [[0,3],[0,5],[2,2]], cooldown = {cooldown1}");
Console.WriteLine($"  Expected: [1, 0, 2]");
Console.WriteLine($"  Got:      [{string.Join(", ", result1)}]");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// tasks = [[5,1],[0,2],[0,3]], cooldown = 0
// Expected output: [2, 1, 0]
//
// Trace:
//   currentTime=0: release tasks 1 (r=0,p=2) and 2 (r=0,p=3). Heap: {2,1}.
//     Pick task 2 (priority 3). result=[2]. nextFree = 0+0+1 = 1.
//   currentTime=1: no new releases (task 0 releases at 5). Heap: {1}.
//     Pick task 1 (priority 2). result=[2,1]. nextFree = 1+0+1 = 2.
//   currentTime=2: no tasks ready. Jump to max(2, 5)=5.
//     Release task 0 (r=5,p=1). Heap: {0}.
//     Pick task 0. result=[2,1,0]. Done.
int[][] tasks2 = [[5, 1], [0, 2], [0, 3]];
int cooldown2 = 0;
List<int> result2 = solution.ScheduleTasks(tasks2, cooldown2);
Console.WriteLine("Example 2:");
Console.WriteLine($"  Input:    tasks = [[5,1],[0,2],[0,3]], cooldown = {cooldown2}");
Console.WriteLine($"  Expected: [2, 1, 0]");
Console.WriteLine($"  Got:      [{string.Join(", ", result2)}]");
Console.WriteLine();

// ── Extra Edge Case: single task ─────────────────────────────────────────────
int[][] tasks3 = [[10, 7]];
int cooldown3 = 5;
List<int> result3 = solution.ScheduleTasks(tasks3, cooldown3);
Console.WriteLine("Edge Case (single task):");
Console.WriteLine($"  Input:    tasks = [[10,7]], cooldown = {cooldown3}");
Console.WriteLine($"  Expected: [0]");
Console.WriteLine($"  Got:      [{string.Join(", ", result3)}]");
Console.WriteLine();

// ── Extra Edge Case: all same priority, tie-break by release then index ───────
// tasks = [[0,5],[0,5],[0,5]], cooldown = 2
// All have same priority and same release time → tie-break by original index: 0,1,2
int[][] tasks4 = [[0, 5], [0, 5], [0, 5]];
int cooldown4 = 2;
List<int> result4 = solution.ScheduleTasks(tasks4, cooldown4);
Console.WriteLine("Edge Case (all same priority & release, tie-break by index):");
Console.WriteLine($"  Input:    tasks = [[0,5],[0,5],[0,5]], cooldown = {cooldown4}");
Console.WriteLine($"  Expected: [0, 1, 2]");
Console.WriteLine($"  Got:      [{string.Join(", ", result4)}]");