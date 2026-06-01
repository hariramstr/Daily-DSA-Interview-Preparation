/*
 * Title: Flatten Nested Task Queue
 * Difficulty: Medium
 * Topic: Stacks and Queues
 *
 * Problem Description:
 * You are given a sequence of task instructions represented as a list of strings.
 * Each instruction is either:
 *   - A task name (a non-empty string of lowercase letters), meaning "execute this task"
 *   - "BEGIN", meaning the following tasks form a subtask group
 *   - "END", meaning the current subtask group is complete
 *
 * Subtask groups can be nested. When a "BEGIN" ... "END" block is encountered,
 * all tasks within it must be executed in REVERSE ORDER (last task in the block runs first),
 * while tasks outside any block execute in normal order.
 * Nested BEGIN/END blocks alternate reversal:
 *   - Depth 1 (outermost) reverses
 *   - Depth 2 does NOT reverse
 *   - Depth 3 reverses
 *   - And so on...
 *
 * Return a list of task names in the final execution order.
 *
 * Example 1:
 *   Input:  ["a", "BEGIN", "b", "c", "END", "d"]
 *   Output: ["a", "c", "b", "d"]
 *
 * Example 2:
 *   Input:  ["a", "BEGIN", "b", "BEGIN", "c", "d", "END", "e", "END", "f"]
 *   Output: ["a", "e", "c", "d", "b", "f"]
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution Class
// ─────────────────────────────────────────────────────────────────────────────
class Solution
{
    /// <summary>
    /// Flattens a nested task queue respecting alternating reversal rules.
    ///
    /// Time Complexity:  O(N) — each instruction is processed once; reversals
    ///                   collectively touch each task at most once per nesting
    ///                   level, but since each task belongs to exactly one level
    ///                   the total work is still O(N).
    /// Space Complexity: O(N) — the stack of lists holds all task names at most
    ///                   once across all levels at any given time.
    /// </summary>
    public List<string> FlattenTasks(List<string> instructions)
    {
        // ── STEP 1: Set up the stack ──────────────────────────────────────────
        // We use a stack where each element is a List<string> representing the
        // "current working buffer" for one nesting level.
        //
        // WHY a stack?
        //   Because BEGIN/END blocks are nested — just like parentheses or
        //   function calls. A stack naturally mirrors that LIFO (last-in,
        //   first-out) structure: when we see BEGIN we push a new buffer;
        //   when we see END we pop it, process it, and merge it into the
        //   buffer below.
        //
        // We start with depth 0 (outside all blocks). Depth 0 is NOT inside
        // any BEGIN/END, so it never reverses — it just collects tasks in order.
        // We represent depth 0 as the bottom element of the stack.
        var stack = new Stack<List<string>>();

        // Push the initial (depth-0) buffer onto the stack.
        stack.Push(new List<string>());

        // ── STEP 2: Track the current nesting depth ───────────────────────────
        // depth == 0  → outside all blocks (no reversal)
        // depth == 1  → inside one BEGIN/END  (reverses)
        // depth == 2  → inside two BEGIN/ENDs (does NOT reverse)
        // depth == k  → reverses if k is odd, does not reverse if k is even
        int depth = 0;

        // ── STEP 3: Process each instruction one by one ───────────────────────
        foreach (string instruction in instructions)
        {
            if (instruction == "BEGIN")
            {
                // ── BEGIN encountered ─────────────────────────────────────────
                // We are entering a new subtask group. Increase depth and push
                // a fresh empty list onto the stack to collect tasks for this
                // new group. We do NOT add "BEGIN" itself to any task list.
                depth++;
                stack.Push(new List<string>());
                // Example trace (Ex 2): after "BEGIN" at position 1 → depth=1,
                // stack = [ [a], [] ]
            }
            else if (instruction == "END")
            {
                // ── END encountered ───────────────────────────────────────────
                // The current subtask group is complete. We need to:
                //   1. Pop the current group's buffer from the stack.
                //   2. Decide whether to reverse it (depth is odd → reverse).
                //   3. Append the (possibly reversed) tasks to the parent buffer
                //      (the buffer one level up, now on top of the stack).
                //   4. Decrease depth.

                // Step 3a: Pop the completed group's buffer.
                List<string> completedGroup = stack.Pop();

                // Step 3b: Reverse if the current depth is odd.
                // WHY? The problem says depth-1 blocks reverse, depth-2 don't,
                // depth-3 do, etc. That is exactly "reverse when depth is odd".
                if (depth % 2 == 1)
                {
                    completedGroup.Reverse();
                    // Example trace (Ex 1): depth=1 (odd) → [b,c] becomes [c,b]
                }
                // If depth is even, we leave completedGroup in its original order.
                // Example trace (Ex 2): inner block at depth=2 (even) → [c,d] stays [c,d]

                // Step 3c: Merge the (possibly reversed) group into the parent buffer.
                // The parent buffer is now on top of the stack after the Pop above.
                stack.Peek().AddRange(completedGroup);
                // Example trace (Ex 2): after inner END, parent buffer (depth=1) becomes
                // [b, c, d]  (b was added before the inner BEGIN, then [c,d] appended)

                // Step 3d: Decrease depth because we have left one nesting level.
                depth--;
            }
            else
            {
                // ── Regular task name ─────────────────────────────────────────
                // Simply add the task to the current top-of-stack buffer.
                // We do NOT reverse individual tasks here; reversal happens
                // only when a complete group is closed by END.
                stack.Peek().Add(instruction);
                // Example trace (Ex 1): "a" → depth-0 buffer = [a]
                //                       "b" → depth-1 buffer = [b]
                //                       "c" → depth-1 buffer = [b, c]
                //                       "d" → depth-0 buffer = [a, c, b, d]  (after END merged)
            }
        }

        // ── STEP 4: Return the final result ───────────────────────────────────
        // After processing all instructions, the stack should contain exactly
        // one element: the depth-0 buffer holding all tasks in execution order.
        // (The problem guarantees every BEGIN has a matching END, so depth
        //  returns to 0 and the stack has exactly one list.)
        return stack.Pop();

        // ── Full trace for Example 1 ──────────────────────────────────────────
        // instructions = ["a", "BEGIN", "b", "c", "END", "d"]
        //
        // "a"    → depth=0, stack=[ [a] ]
        // "BEGIN"→ depth=1, stack=[ [a], [] ]
        // "b"    → depth=1, stack=[ [a], [b] ]
        // "c"    → depth=1, stack=[ [a], [b,c] ]
        // "END"  → pop [b,c], depth=1(odd)→reverse→[c,b], merge into [a] → [a,c,b], depth=0
        //          stack=[ [a,c,b] ]
        // "d"    → depth=0, stack=[ [a,c,b,d] ]
        // Result: [a, c, b, d] ✓
        //
        // ── Full trace for Example 2 ──────────────────────────────────────────
        // instructions = ["a","BEGIN","b","BEGIN","c","d","END","e","END","f"]
        //
        // "a"     → depth=0, stack=[ [a] ]
        // "BEGIN" → depth=1, stack=[ [a], [] ]
        // "b"     → depth=1, stack=[ [a], [b] ]
        // "BEGIN" → depth=2, stack=[ [a], [b], [] ]
        // "c"     → depth=2, stack=[ [a], [b], [c] ]
        // "d"     → depth=2, stack=[ [a], [b], [c,d] ]
        // "END"   → pop [c,d], depth=2(even)→no reverse→[c,d], merge into [b]→[b,c,d], depth=1
        //           stack=[ [a], [b,c,d] ]
        // "e"     → depth=1, stack=[ [a], [b,c,d,e] ]
        // "END"   → pop [b,c,d,e], depth=1(odd)→reverse→[e,d,c,b], merge into [a]→[a,e,d,c,b], depth=0
        //           stack=[ [a,e,d,c,b] ]
        // "f"     → depth=0, stack=[ [a,e,d,c,b,f] ]
        // Result: [a, e, d, c, b, f]
        //
        // Hmm — that gives [a,e,d,c,b,f] but expected is [a,e,c,d,b,f].
        // The difference is in how the inner block's tasks are ordered after
        // the outer reversal. Let me re-read the problem...
        //
        // The outer block contains: b, [inner block = c,d], e
        // The inner block (depth 2, even) does NOT reverse → stays [c,d].
        // The outer block (depth 1, odd) reverses its TOP-LEVEL items:
        //   items are: b, <group c,d>, e  → reversed order: e, <group c,d>, b
        //   = [e, c, d, b]
        // Final: [a, e, c, d, b, f]
        //
        // The key insight: when we reverse the outer block, we should reverse
        // the SEQUENCE of items as they were added, treating the inner block's
        // already-flattened tasks as a contiguous chunk that moves together.
        //
        // But with simple list reversal, [b,c,d,e] reversed is [e,d,c,b],
        // which breaks the inner chunk apart.
        //
        // We need a different approach: store items as "segments" so that
        // the inner block's tasks are treated as one atomic unit during reversal.
        // See the revised implementation below.
    }

    /// <summary>
    /// Revised implementation using a stack of "segments".
    /// Each element on the stack is a List of segments, where each segment
    /// is itself a List<string> (a contiguous run of tasks that must stay together).
    ///
    /// When we reverse a block, we reverse the ORDER of segments, not individual tasks.
    /// This preserves the internal order of nested blocks.
    ///
    /// Time Complexity:  O(N) — each task is touched a constant number of times.
    /// Space Complexity: O(N) — all tasks are stored across the segment lists.
    /// </summary>
    public List<string> FlattenTasksCorrect(List<string> instructions)
    {
        // ── STEP 1: Define the data structure ────────────────────────────────
        // The stack holds one entry per active nesting level.
        // Each entry is a List<List<string>>: a list of "segments".
        //
        // A "segment" is a List<string> representing either:
        //   (a) A single task: ["taskname"]
        //   (b) A completed sub-block that has already been processed and
        //       whose internal order is finalized.
        //
        // WHY segments?
        //   When we reverse a block, we want to reverse the ORDER of its
        //   top-level items (individual tasks and sub-blocks), NOT the
        //   individual characters inside sub-blocks.
        //   By storing each sub-block as one segment, reversing the segment
        //   list reverses the order of top-level items while keeping each
        //   sub-block's internal order intact.

        // Stack of levels; each level holds a list of segments.
        var stack = new Stack<List<List<string>>>();

        // Push the depth-0 level (outside all blocks).
        stack.Push(new List<List<string>>());

        // Track current depth (0 = outside all blocks).
        int depth = 0;

        // ── STEP 2: Process each instruction ─────────────────────────────────
        foreach (string instruction in instructions)
        {
            if (instruction == "BEGIN")
            {
                // Entering a new block: increase depth, push a fresh segment list.
                depth++;
                stack.Push(new List<List<string>>());
            }
            else if (instruction == "END")
            {
                // ── Closing a block ───────────────────────────────────────────
                // 1. Pop the current level's segment list.
                List<List<string>> currentSegments = stack.Pop();

                // 2. If depth is odd, reverse the ORDER of segments.
                //    This reverses the top-level items of this block while
                //    keeping each segment's internal task order intact.
                if (depth % 2 == 1)
                {
                    currentSegments.Reverse();
                    // Example (Ex 2, outer END at depth=1):
                    //   segments before reverse: [ [b], [c,d], [e] ]
                    //   segments after  reverse: [ [e], [c,d], [b] ]
                }
                // If depth is even, leave segments in original order.
                // Example (Ex 2, inner END at depth=2):
                //   segments: [ [c], [d] ] → no reverse → [ [c], [d] ]

                // 3. Flatten the segments into one combined list of tasks.
                //    This combined list becomes ONE segment in the parent level,
                //    so the parent treats this entire block as an atomic unit
                //    when it eventually reverses.
                var combinedTasks = new List<string>();
                foreach (var segment in currentSegments)
                {
                    combinedTasks.AddRange(segment);
                }

                // 4. Add the combined block as a single segment to the parent level.
                stack.Peek().Add(combinedTasks);

                // 5. Decrease depth.
                depth--;
            }
            else
            {
                // ── Regular task ──────────────────────────────────────────────
                // Add this task as its own single-task segment to the current level.
                // It is a segment of size 1 so it can be moved atomically during
                // any future reversal of the enclosing block.
                stack.Peek().Add(new List<string> { instruction });
            }
        }

        // ── STEP 3: Flatten the depth-0 level into the final result ──────────
        // The depth-0 level is never reversed (depth 0 is even, and we only
        // reverse on END which brings us from depth≥1 back). Its segments are
        // already in the correct order; just concatenate them.
        List<List<string>> topLevel = stack.Pop();
        var result = new List<string>();
        foreach (var segment in topLevel)
        {
            result.AddRange(segment);
        }
        