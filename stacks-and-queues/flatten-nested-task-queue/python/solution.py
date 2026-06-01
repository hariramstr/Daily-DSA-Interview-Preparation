```python
"""
Title: Flatten Nested Task Queue
Difficulty: Medium
Topic: Stacks and Queues

Problem Description:
You are given a sequence of task instructions represented as a list of strings.
Each instruction is either:
- A task name (a non-empty string of lowercase letters), meaning "execute this task"
- "BEGIN", meaning the following tasks form a subtask group
- "END", meaning the current subtask group is complete

Subtask groups can be nested. When a "BEGIN" ... "END" block is encountered,
all tasks within it must be executed in REVERSE ORDER (last task in the block
runs first), while tasks outside any block execute in normal order. Nested
BEGIN/END blocks alternate reversal — the outermost block reverses, the next
level does not, the next level does, and so on.

Return a list of task names in the final execution order.

Constraints:
- 1 <= instructions.length <= 10^4
- Every "BEGIN" has a matching "END"
- Nesting depth will not exceed 1000
- Task names consist only of lowercase English letters
- Task names are between 1 and 20 characters long

Example 1:
    Input: instructions = ["a", "BEGIN", "b", "c", "END", "d"]
    Output: ["a", "c", "b", "d"]
    Explanation: Tasks outside the block run normally: a, d.
                 The block [b, c] is at depth 1 (odd), so it reverses to [c, b].

Example 2:
    Input: instructions = ["a", "BEGIN", "b", "BEGIN", "c", "d", "END", "e", "END", "f"]
    Output: ["a", "e", "c", "d", "b", "f"]
    Explanation: Outer block (depth 1) reverses its contents.
                 Inner block (depth 2) does NOT reverse.
                 Outer block contains [b, [c,d], e].
                 Inner block stays [c,d].
                 Outer block reverses to [e, [c,d], b] = [e, c, d, b].
                 Final: [a, e, c, d, b, f].
"""

from typing import List


class Solution:
    def flatten_tasks(self, instructions: List[str]) -> List[str]:
        """
        Flatten a nested task queue with alternating reversal at each nesting depth.

        The key insight is:
        - Depth 0 (outside all blocks): normal order
        - Depth 1 (inside first BEGIN/END): reversed
        - Depth 2 (inside nested BEGIN/END): not reversed (normal)
        - Depth 3: reversed again
        - ... alternates based on odd/even depth

        We use a stack of lists to collect tasks at each nesting level.
        When we encounter END, we pop the current level's tasks, optionally
        reverse them (if the depth is odd), and append them to the parent level.

        Args:
            instructions: List of strings, each being a task name, "BEGIN", or "END"

        Returns:
            List of task names in final execution order

        Time Complexity: O(n) where n is the number of instructions
                         Each instruction is processed once, and reversals
                         collectively touch each task at most once per level,
                         but overall still O(n) amortized.
        Space Complexity: O(n) for the stack and result storage
        """

        # -----------------------------------------------------------------------
        # STEP 1: Initialize the stack
        # -----------------------------------------------------------------------
        # We use a stack (list of lists) to track tasks at each nesting level.
        # The bottom of the stack (index 0) represents the "global" level (depth 0).
        # When we see BEGIN, we push a new empty list onto the stack.
        # When we see END, we pop the top list, process it, and merge into parent.
        #
        # stack[0] = tasks at depth 0 (global, no reversal)
        # stack[1] = tasks currently being collected at depth 1 (will be reversed)
        # stack[2] = tasks currently being collected at depth 2 (will NOT be reversed)
        # etc.
        #
        # We start with one empty list for the global level.
        stack: List[List[str]] = [[]]

        # -----------------------------------------------------------------------
        # STEP 2: Process each instruction one by one
        # -----------------------------------------------------------------------
        for instruction in instructions:

            if instruction == "BEGIN":
                # -----------------------------------------------------------
                # Encountered a BEGIN: start a new nesting level
                # -----------------------------------------------------------
                # Push a new empty list onto the stack to collect tasks
                # that belong to this new block.
                # The depth of this new block = len(stack) after push.
                # Before push: len(stack) = current depth
                # After push: len(stack) = current depth + 1
                stack.append([])

            elif instruction == "END":
                # -----------------------------------------------------------
                # Encountered an END: close the current nesting level
                # -----------------------------------------------------------
                # Pop the top list from the stack — these are the tasks
                # collected inside the most recent BEGIN/END block.
                current_block: List[str] = stack.pop()

                # Determine the depth of the block we just closed.
                # After popping, len(stack) = depth of the parent level.
                # The block we just closed was at depth = len(stack) + 1 before pop,
                # which equals len(stack) + 1. But since we already popped,
                # the depth of the closed block = len(stack) + 1.
                # Wait — let's think carefully:
                #   Before pop: stack had len(stack) levels, so the block being
                #   closed was at index len(stack)-1, meaning depth = len(stack)-1+1
                #   = len(stack). But we already popped, so now len(stack) is one less.
                #   Depth of closed block = len(stack) + 1 - 1 = len(stack).
                #   Actually: depth = len(stack) after pop + 1 - 1 = len(stack).
                #
                # Let's re-derive:
                #   When BEGIN was encountered, we pushed → stack grew by 1.
                #   The block's depth = the stack size AFTER the push.
                #   When END is encountered, we pop → stack shrinks by 1.
                #   After pop, len(stack) = depth - 1.
                #   So depth = len(stack) + 1 - 1 = len(stack).
                #   Wait: if stack had size S before pop, depth = S (1-indexed).
                #   After pop, len(stack) = S - 1.
                #   depth = S = (S - 1) + 1 = len(stack) + 1.
                #
                # Hmm, let me trace Example 1 to verify:
                #   Start: stack = [[]]  → len = 1
                #   "a": stack = [["a"]]
                #   "BEGIN": stack = [["a"], []]  → len = 2
                #   "b": stack = [["a"], ["b"]]
                #   "c": stack = [["a"], ["b", "c"]]
                #   "END": pop → current_block = ["b", "c"], stack = [["a"]]
                #          After pop: len(stack) = 1
                #          depth of closed block = 1 + 1 = 2? But problem says depth 1.
                #
                # The problem uses 1-indexed depth for the first BEGIN block.
                # Let me re-read: "outermost block reverses" = depth 1 reverses.
                # In my stack model after pop: len(stack) = 1 for the outermost block.
                # So depth = len(stack) after pop? Let's check:
                #   After pop for outermost: len(stack) = 1 → depth = 1 → ODD → REVERSE ✓
                #
                # For Example 2, inner block:
                #   "BEGIN" (outer): stack = [["a"], []]
                #   "b": stack = [["a"], ["b"]]
                #   "BEGIN" (inner): stack = [["a"], ["b"], []]
                #   "c": stack = [["a"], ["b"], ["c"]]
                #   "d": stack = [["a"], ["b"], ["c", "d"]]
                #   "END" (inner): pop → current_block = ["c","d"], stack = [["a"],["b"]]
                #          After pop: len(stack) = 2 → depth = 2 → EVEN → NO REVERSE ✓
                #   "e": stack = [["a"], ["b", ["c","d"]-flattened, "e"]]
                #          Actually we extend, not append a list.
                #   "END" (outer): pop → current_block = ["b","c","d","e"],
                #          stack = [["a"]]
                #          After pop: len(stack) = 1 → depth = 1 → ODD → REVERSE ✓
                #          reversed: ["e","d","c","b"]
                #          stack[0].extend(["e","d","c","b"]) → ["a","e","d","c","b"]
                #   "f": stack = [["a","e","d","c","b","f"]]
                #   Result: ["a","e","d","c","b","f"]
                #   Expected: ["a","e","c","d","b","f"]
                #
                # Hmm, that doesn't match! The inner block [c,d] should NOT be reversed,
                # so it stays [c,d]. The outer block sees [b, c, d, e] and reverses to
                # [e, d, c, b]... but expected is [e, c, d, b].
                #
                # The issue: when we flatten the inner block into the outer block's list,
                # and then reverse the outer block, the inner block's items get reversed
                # as part of the outer reversal. But the inner block should maintain its
                # internal order even after the outer reversal.
                #
                # This means we can't simply flatten and reverse. We need to treat the
                # inner block as a unit when reversing the outer block.
                #
                # REVISED APPROACH: Instead of storing individual task names in each
                # level's list, we store "items" which can be either a task name (str)
                # or a sub-list (already processed inner block). When we reverse the
                # outer block, we reverse the order of items (treating inner blocks as
                # atomic units), then flatten everything.
                #
                # Let me re-think the data structure:
                # Each level's list contains items that are either:
                #   - str: a task name
                #   - list: a fully processed inner block (already in correct order)
                #
                # When END is encountered:
                #   1. Pop current_block (list of items)
                #   2. Determine depth (len(stack) after pop)
                #   3. If depth is odd: reverse the list of items
                #   4. Flatten the items into a single list of strings
                #   5. Extend the parent level with this flattened list
                #
                # Wait, but if we flatten before extending parent, then the parent
                # can't treat this block as an atomic unit for its own reversal.
                #
                # BETTER APPROACH: Store items as mixed (str or list), and only
                # flatten at the very end (or when extending parent, keep as a
                # sub-list item).
                #
                # Let me redesign:
                # Each stack level holds a list of "items" where item is str or list[str].
                # When END:
                #   1. Pop current_block
                #   2. depth = len(stack) after pop (1-indexed)
                #   3. If odd depth: reverse current_block (reverse the list of items)
                #   4. Flatten current_block into a list of strings → processed_block
                #   5. Append processed_block (as a single list item) to parent? No...
                #      We need to append it as a unit so parent can reverse it atomically.
                #
                # Actually, let's keep processed_block as a list and append it as ONE
                # item to the parent stack level. Then when the parent is processed,
                # it reverses its items (which include this sub-list as one item),
                # and then flattens.
                #
                # Let me re-trace Example 2 with this approach:
                # stack = [[]]  (each element is a list of items, item = str or list)
                # "a": stack[0].append("a") → stack = [["a"]]
                # "BEGIN": stack.append([]) → stack = [["a"], []]
                # "b": stack[1].append("b") → stack = [["a"], ["b"]]
                # "BEGIN": stack.append([]) → stack = [["a"], ["b"], []]
                # "c": stack[2].append("c") → stack = [["a"], ["b"], ["c"]]
                # "d": stack[2].append("d") → stack = [["a"], ["b"], ["c","d"]]
                # "END" (inner):
                #   current_block = ["c","d"], stack = [["a"],["b"]]
                #   depth = len(stack) = 2 → even → NO reverse
                #   flatten(["c","d"]) = ["c","d"]
                #   Append ["c","d"] as ONE item to parent:
                #   stack[1].append(["c","d"]) → stack = [["a"], ["b", ["c","d"]]]
                # "e": stack[1].append("e") → stack = [["a"], ["b", ["c","d"], "e"]]
                # "END" (outer):
                #   current_block = ["b", ["c","d"], "e"], stack = [["a"]]
                #   depth = len(stack) = 1 → odd → REVERSE
                #   reversed: ["e", ["c","d"], "b"]
                #   flatten: ["e","c","d","b"]
                #   Append ["e","c","d","b"] as ONE item to parent:
                #   stack[0].append(["e","c","d","b"]) → stack = [["a", ["e","c","d","b"]]]
                # "f": stack[0].append("f") → stack = [["a", ["e","c","d","b"], "f"]]
                # Final: flatten stack[0] = ["a","e","c","d","b","f"] ✓
                #
                # Great! This matches the expected output!
                # Let me also verify Example 1:
                # stack = [[]]
                # "a": stack = [["a"]]
                # "BEGIN": stack = [["a"], []]
                # "b": stack = [["a"], ["b"]]
                # "c": stack = [["a"], ["b","c"]]
                # "END":
                #   current_block = ["b","c"], stack = [["a"]]
                #   depth = 1 → odd → REVERSE → ["c","b"]
                #   flatten: ["c","b"]
                #   stack[0].append(["c","b"]) → stack = [["a", ["c","b"]]]
                # "d": stack = [["a", ["c","b"], "d"]]
                # Final: flatten = ["a","c","b","d"] ✓
                #
                # Perfect! Now let me implement this correctly.
                # I need to rewrite the method with this corrected approach.
                pass

            else:
                # Regular task name: append to current top of stack
                stack[-1].append(instruction)

        # This implementation above is incomplete/incorrect due to the redesign.
        # The actual implementation is in the corrected method below.
        # This method body will be replaced.
        return []

    def flatten_tasks_v2(self, instructions: List[str]) -> List[str]:
        """
        Flatten a nested task queue with alternating reversal at each nesting depth.

        Uses a stack where each level holds a list of items (str or list).
        When a BEGIN/END block closes, its items are optionally reversed (if depth
        is odd), flattened into a list, and appended as a single atomic unit to
        the parent level. This ensures that inner block ordering is preserved
        when the outer block is reversed.

        Args:
            instructions: List of strings, each being a task name, "BEGIN", or "END"

        Returns: