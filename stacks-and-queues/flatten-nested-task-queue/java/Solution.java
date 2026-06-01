/*
 * Title: Flatten Nested Task Queue
 * Difficulty: Medium
 * Topic: Stacks and Queues
 *
 * Problem Description:
 * You are given a sequence of task instructions represented as a list of strings.
 * Each instruction is either:
 * - A task name (a non-empty string of lowercase letters), meaning "execute this task"
 * - "BEGIN", meaning the following tasks form a subtask group
 * - "END", meaning the current subtask group is complete
 *
 * Subtask groups can be nested. When a "BEGIN" ... "END" block is encountered,
 * all tasks within it must be executed in REVERSE ORDER (last task in the block
 * runs first), while tasks outside any block execute in normal order.
 * Nested BEGIN/END blocks alternate reversal — the outermost block reverses,
 * the next level does not, the next level does, and so on.
 *
 * Return a list of task names in the final execution order.
 *
 * Constraints:
 * - 1 <= instructions.length <= 10^4
 * - Every "BEGIN" has a matching "END"
 * - Nesting depth will not exceed 1000
 * - Task names consist only of lowercase English letters
 * - Task names are between 1 and 20 characters long
 *
 * Example 1:
 * Input: ["a", "BEGIN", "b", "c", "END", "d"]
 * Output: ["a", "c", "b", "d"]
 *
 * Example 2:
 * Input: ["a", "BEGIN", "b", "BEGIN", "c", "d", "END", "e", "END", "f"]
 * Output: ["a", "e", "c", "d", "b", "f"]
 */

import java.util.*;

/**
 * Solution class for the "Flatten Nested Task Queue" problem.
 *
 * <p>Key Insight:
 * We use a stack of lists to simulate nested BEGIN/END blocks.
 * Each level of the stack holds the tasks collected at that nesting depth.
 * When we hit END, we either reverse or keep the collected tasks depending
 * on the depth (odd depth = reverse, even depth = keep order), then
 * merge them into the parent level's list.
 */
public class Solution {

    /**
     * Flattens a nested task instruction list into a final execution order.
     *
     * <p>Algorithm Overview:
     * We maintain a stack where each element is a List&lt;String&gt; representing
     * the tasks collected at the current nesting level. The bottom of the stack
     * (index 0) is the "global" level (depth 0, no reversal). Each BEGIN pushes
     * a new list; each END pops the top list, optionally reverses it, and appends
     * it to the new top list.
     *
     * @param instructions the list of instruction strings (task names, "BEGIN", "END")
     * @return the flattened list of task names in final execution order
     *
     * Time Complexity:  O(n) where n = instructions.length, since each instruction
     *                   is processed once, and reversal work is amortized O(n) total.
     * Space Complexity: O(n) for the stack of lists storing all task names.
     */
    public List<String> flattenTasks(List<String> instructions) {

        // -----------------------------------------------------------------------
        // Step 1: Initialize the stack.
        // We use a Deque<List<String>> as our stack.
        // The bottom list (depth 0) collects top-level tasks (no reversal).
        // -----------------------------------------------------------------------
        Deque<List<String>> stack = new ArrayDeque<>();

        // Push the initial "global" list onto the stack (depth 0).
        stack.push(new ArrayList<>());

        // -----------------------------------------------------------------------
        // Step 2: Track the current nesting depth.
        // depth 0 = outside all blocks (no reversal)
        // depth 1 = inside one BEGIN/END block (reverse)
        // depth 2 = inside two nested BEGIN/END blocks (no reverse)
        // depth k = reverse if k is odd, keep order if k is even
        // -----------------------------------------------------------------------
        int depth = 0;

        // -----------------------------------------------------------------------
        // Step 3: Process each instruction one by one.
        // -----------------------------------------------------------------------
        for (String instruction : instructions) {

            if (instruction.equals("BEGIN")) {
                // -----------------------------------------------------------
                // Encountered BEGIN: increase depth and push a new empty list
                // onto the stack to collect tasks for this new block.
                // -----------------------------------------------------------
                depth++;
                stack.push(new ArrayList<>());

            } else if (instruction.equals("END")) {
                // -----------------------------------------------------------
                // Encountered END: the current block is complete.
                //
                // 1. Pop the top list (tasks collected in this block).
                // 2. Decide whether to reverse based on current depth:
                //    - odd depth  => reverse the list
                //    - even depth => keep the list as-is
                // 3. Append the (possibly reversed) tasks to the parent list
                //    (the new top of the stack after popping).
                // 4. Decrease depth.
                // -----------------------------------------------------------

                // Pop the completed block's task list.
                List<String> completedBlock = stack.pop();

                // Reverse if the current depth is odd (1, 3, 5, ...).
                if (depth % 2 == 1) {
                    Collections.reverse(completedBlock);
                }
                // If depth is even (2, 4, ...), keep the order as-is.

                // Peek at the parent list (now on top of the stack).
                List<String> parentList = stack.peek();

                // Append all tasks from the completed block into the parent list.
                // This "flattens" the block into its parent context.
                parentList.addAll(completedBlock);

                // Decrease depth since we've closed one BEGIN/END block.
                depth--;

            } else {
                // -----------------------------------------------------------
                // It's a regular task name.
                // Simply add it to the current top-of-stack list.
                // -----------------------------------------------------------
                stack.peek().add(instruction);
            }
        }

        // -----------------------------------------------------------------------
        // Step 4: After processing all instructions, the stack should have exactly
        // one list remaining — the global (depth 0) list with all tasks in order.
        // -----------------------------------------------------------------------
        return stack.pop();
    }

    // ==========================================================================
    // TRACE-THROUGH VERIFICATION
    // ==========================================================================
    //
    // Example 1: ["a", "BEGIN", "b", "c", "END", "d"]
    //
    // Initial state: stack = [[]], depth = 0
    //
    // "a"    -> stack = [["a"]], depth = 0
    // "BEGIN"-> depth=1, stack = [[], ["a"]]   (new list pushed on top)
    // "b"    -> stack = [["b"], ["a"]], depth = 1
    // "c"    -> stack = [["b","c"], ["a"]], depth = 1
    // "END"  -> depth=1 (odd) => reverse ["b","c"] => ["c","b"]
    //           pop ["c","b"], parent = ["a"], parent.addAll => ["a","c","b"]
    //           depth=0, stack = [["a","c","b"]]
    // "d"    -> stack = [["a","c","b","d"]], depth = 0
    //
    // Result: ["a","c","b","d"] ✓
    //
    // --------------------------------------------------------------------------
    //
    // Example 2: ["a","BEGIN","b","BEGIN","c","d","END","e","END","f"]
    //
    // Initial: stack=[[]], depth=0
    //
    // "a"     -> stack=[["a"]], depth=0
    // "BEGIN" -> depth=1, stack=[[], ["a"]]
    // "b"     -> stack=[["b"], ["a"]], depth=1
    // "BEGIN" -> depth=2, stack=[[], ["b"], ["a"]]
    // "c"     -> stack=[["c"], ["b"], ["a"]], depth=2
    // "d"     -> stack=[["c","d"], ["b"], ["a"]], depth=2
    // "END"   -> depth=2 (even) => no reverse, keep ["c","d"]
    //            pop ["c","d"], parent=["b"], parent.addAll => ["b","c","d"]
    //            depth=1, stack=[["b","c","d"], ["a"]]
    // "e"     -> stack=[["b","c","d","e"], ["a"]], depth=1
    // "END"   -> depth=1 (odd) => reverse ["b","c","d","e"] => ["e","d","c","b"]
    //            pop ["e","d","c","b"], parent=["a"], parent.addAll => ["a","e","d","c","b"]
    //            depth=0, stack=[["a","e","d","c","b"]]
    // "f"     -> stack=[["a","e","d","c","b","f"]], depth=0
    //
    // Result: ["a","e","d","c","b","f"]
    //
    // Hmm, expected is ["a","e","c","d","b","f"]. Let me re-read the problem.
    //
    // Re-reading Example 2:
    // "Outer block (depth 1) reverses its contents. Inner block (depth 2) does NOT reverse.
    //  Outer block contains [b, [c,d], e]. Inner block stays [c,d].
    //  Outer block reverses to [e, [c,d], b] = [e, c, d, b]. Final: [a, e, c, d, b, f]."
    //
    // The key insight: when the outer block reverses, it treats the inner block as a UNIT.
    // So [b, [c,d], e] reversed as units = [e, [c,d], b] = [e, c, d, b].
    // But my algorithm flattened [c,d] into the outer block before reversing,
    // giving [b,c,d,e] reversed = [e,d,c,b]. That's wrong.
    //
    // FIX: We need to treat nested blocks as atomic units during reversal.
    // Instead of flattening immediately, we should store "segments" and reverse segments.
    //
    // Revised approach: Each stack level holds a List of "segments", where each segment
    // is itself a List<String> (either a single task or a completed sub-block).
    // When we reverse, we reverse the order of segments, not individual tasks.
    // ==========================================================================

    /**
     * Corrected implementation using segment-based reversal.
     *
     * <p>Each level of the stack stores a list of "segments". A segment is a
     * List&lt;String&gt; representing either a single task (one element) or a
     * completed sub-block (multiple elements already in their final order).
     * When a block ends and needs reversal, we reverse the ORDER of segments,
     * not the individual tasks within each segment.
     *
     * @param instructions the list of instruction strings
     * @return the flattened list of task names in final execution order
     *
     * Time Complexity:  O(n) amortized — each task is added/moved a constant
     *                   number of times across all levels.
     * Space Complexity: O(n) for the stack of segment lists.
     */
    public List<String> flattenTasksCorrect(List<String> instructions) {

        // -----------------------------------------------------------------------
        // Step 1: Initialize the stack.
        // Each stack frame is a List<List<String>> — a list of segments.
        // A segment is a List<String> of task names that form an atomic unit.
        // The bottom frame (depth 0) is the global level.
        // -----------------------------------------------------------------------
        Deque<List<List<String>>> stack = new ArrayDeque<>();
        stack.push(new ArrayList<>());  // global frame

        // Track current nesting depth (0 = global, 1 = first BEGIN/END, etc.)
        int depth = 0;

        // -----------------------------------------------------------------------
        // Step 2: Process each instruction.
        // -----------------------------------------------------------------------
        for (String instruction : instructions) {

            if (instruction.equals("BEGIN")) {
                // Push a new frame for the new block.
                depth++;
                stack.push(new ArrayList<>());

            } else if (instruction.equals("END")) {
                // -----------------------------------------------------------
                // The current block is complete.
                // Pop its frame (list of segments).
                // If depth is odd => reverse the ORDER of segments.
                // If depth is even => keep segment order as-is.
                // Then flatten all segments into one combined list,
                // and add that combined list as a SINGLE segment to the parent frame.
                // -----------------------------------------------------------

                List<List<String>> currentFrame = stack.pop();

                // Reverse the segment order if current depth is odd.
                if (depth % 2 == 1) {
                    Collections.reverse(currentFrame);
                }

                // Flatten all segments in this frame into one list.
                // This combined list represents the entire block as one atomic segment.
                List<String> combined = new ArrayList<>();
                for (List<String> segment : currentFrame) {
                    combined.addAll(segment);
                }

                // Add the combined block as a single segment to the parent frame.
                stack.peek().add(combined);

                depth--;

            } else {
                // Regular task: wrap it in a single-element list (one segment)
                // and add to the current frame.
                List<String> singleTask = new ArrayList<>();
                singleTask.add(instruction);
                stack.peek().add(singleTask);
            }
        }

        // -----------------------------------------------------------------------
        // Step 3: The global frame remains. Flatten all its segments into the result.
        // -----------------------------------------------------------------------
        List<List<String>> globalFrame = stack.pop();
        List<String> result = new ArrayList<>();
        for (List<String> segment : globalFrame) {
            result.addAll(segment);
        }
        return result;
    }

    // ==========================================================================
    // RE-TRACE with corrected algorithm:
    //
    // Example 1: ["a", "BEGIN", "b", "c", "END", "d"]
    //
    // Initial: stack=[[]], depth=0
    //
    // "a"    -> frame0=[["a"]], depth=0
    // "BEGIN"-> depth=1, stack=push new frame => stack=[[], [["a"]]]
    //           (top is [], bottom is [["a"]])
    // "b"    -> frame1=[["b"]]
    // "c"    -> frame1=[["b"],["c"]]
    // "END"  -> depth=1 (odd) => reverse frame1 => [["c"],["b"]]
    //           combined = ["c","b"]
    //           parent frame0 gets ["c","b"] as one segment => frame0=[["a"],["c","b"]]
    //           depth=0
    // "d"    -> frame0=[["a"],["c","b"],["d"]]
    //
    // Result: flatten => ["a","c","b","d"] ✓
    //
    // Example 2: ["a","BEGIN","b","BEGIN","c","d","END","e","END","f"]
    //
    // Initial: stack=[[]], depth=0
    //
    // "a"     -> frame0=[["a"]]
    // "BEGIN" -> depth=1, push frame1=[]
    // "b"     -> frame1=[["b"]]
    // "BEGIN" -> depth=2, push frame2=[]
    // "c"     -> frame2=[["c"]]
    // "d"     -> frame2=[["c"],["d"]]
    // "END"   -> depth=2 (even) => no reverse, frame2=[["c"],["d"]]
    //            combined=["c","d"]
    //            frame1 gets ["c","d"] as segment => frame1=[["b"],["c","d"]]
    //            depth=1
    // "e"     -> frame1=[["b"],["c","d"],["e"]]
    // "END"   -> depth=1 (odd) => reverse frame1 => [["e"],["c","d"],["b"]]
    //            combined=