```python
"""
Title: Simulate a Cafeteria Tray Stack
Difficulty: Easy
Topic: Stacks and Queues

Problem Description:
A cafeteria uses a spring-loaded tray dispenser that works like a stack — trays are
added and removed from the top. You are given a list of operations to simulate.
Each operation is one of the following:

- "push X" — Place a tray with label X (an integer) onto the top of the stack.
- "pop"    — Remove and record the tray from the top of the stack.
              If the stack is empty, record -1 instead.
- "peek"   — Record the value of the tray on top without removing it.
              If the stack is empty, record -1.

Return a list of integers representing the results of all "pop" and "peek"
operations in the order they appear.

Constraints:
- 1 <= operations.length <= 1000
- Each "push" operation has a valid integer X where -10^4 <= X <= 10^4
- There will be no malformed operations

Example 1:
  Input:  ["push 5", "push 3", "peek", "pop", "pop", "pop"]
  Output: [3, 3, 5, -1]

Example 2:
  Input:  ["pop", "push 10", "push 20", "peek", "pop"]
  Output: [-1, 20, 20]
"""

from typing import List


class Solution:
    def simulate_tray_stack(self, operations: List[str]) -> List[int]:
        """
        Simulate a cafeteria tray stack (LIFO) given a list of string operations.

        Args:
            operations (List[str]): A list of operation strings. Each string is one of:
                - "push X"  where X is an integer
                - "pop"
                - "peek"

        Returns:
            List[int]: A list of integers that are the results of every "pop" and
                       "peek" operation encountered, in the order they appear.
                       Returns -1 for any "pop" or "peek" on an empty stack.

        Time Complexity:  O(n) — We iterate through each operation exactly once.
                          Each push/pop/peek on a Python list is O(1) amortized.
        Space Complexity: O(n) — In the worst case (all pushes, no pops) the stack
                          holds all n elements. The results list also grows up to n.
        """

        # -----------------------------------------------------------------------
        # DATA STRUCTURE CHOICE: Python list used as a stack.
        #
        # A stack follows Last-In, First-Out (LIFO) order — just like a real
        # spring-loaded tray dispenser where you always interact with the TOP tray.
        #
        # Python's built-in list supports efficient stack operations:
        #   list.append(x)  → push  (adds to the END / "top")   O(1) amortized
        #   list.pop()      → pop   (removes from the END / "top") O(1) amortized
        #   list[-1]        → peek  (reads the END / "top")      O(1)
        #
        # We treat the RIGHT end (index -1) as the "top" of the stack.
        # -----------------------------------------------------------------------
        stack: List[int] = []   # The tray dispenser — starts empty
        results: List[int] = [] # Collects answers for every pop/peek operation

        # -----------------------------------------------------------------------
        # MAIN LOOP: Process each operation string one by one.
        # -----------------------------------------------------------------------
        for operation in operations:

            # -------------------------------------------------------------------
            # STEP 1: Determine which type of operation this string represents.
            #
            # We use str.startswith() for a quick, readable check.
            # Alternatively we could split the string and check the first token,
            # but startswith is slightly simpler for "push" vs "pop"/"peek".
            # -------------------------------------------------------------------

            if operation.startswith("push"):
                # ---------------------------------------------------------------
                # PUSH OPERATION
                #
                # Format: "push X"
                # We need to extract the integer X from the string.
                #
                # Strategy: split the string on whitespace → ["push", "X"]
                # Then take index [1] and convert to int.
                # ---------------------------------------------------------------
                parts = operation.split()   # e.g. "push 5" → ["push", "5"]
                tray_label = int(parts[1])  # Convert "5" → 5

                # Place the tray on top of the stack (append to end of list)
                stack.append(tray_label)
                # NOTE: push does NOT produce a result, so we do NOT append to results.

            elif operation == "pop":
                # ---------------------------------------------------------------
                # POP OPERATION
                #
                # Remove the top tray and record its label.
                # If the stack is empty, record -1 (sentinel value per spec).
                # ---------------------------------------------------------------
                if stack:
                    # Stack is non-empty — remove and record the top element.
                    # list.pop() removes and returns the last element (our "top").
                    top_value = stack.pop()
                    results.append(top_value)
                else:
                    # Stack is empty — nothing to remove, record -1.
                    results.append(-1)

            elif operation == "peek":
                # ---------------------------------------------------------------
                # PEEK OPERATION
                #
                # Look at the top tray WITHOUT removing it, then record its label.
                # If the stack is empty, record -1.
                # ---------------------------------------------------------------
                if stack:
                    # stack[-1] accesses the last element (our "top") without
                    # modifying the stack — this is the classic peek pattern.
                    top_value = stack[-1]
                    results.append(top_value)
                else:
                    # Stack is empty — nothing to peek at, record -1.
                    results.append(-1)

            # -------------------------------------------------------------------
            # NOTE: The problem guarantees no malformed operations, so we don't
            # need an else/error branch here.
            # -------------------------------------------------------------------

        # -----------------------------------------------------------------------
        # Return the collected results from all pop and peek operations.
        # -----------------------------------------------------------------------
        return results


# ---------------------------------------------------------------------------
# TRACE-THROUGH VERIFICATION
#
# Example 1: ["push 5", "push 3", "peek", "pop", "pop", "pop"]
#   - "push 5"  → stack = [5],    results = []
#   - "push 3"  → stack = [5, 3], results = []
#   - "peek"    → top = 3,        results = [3],       stack unchanged [5, 3]
#   - "pop"     → top = 3,        results = [3, 3],    stack = [5]
#   - "pop"     → top = 5,        results = [3, 3, 5], stack = []
#   - "pop"     → empty → -1,     results = [3, 3, 5, -1]
#   Expected: [3, 3, 5, -1]  ✓
#
# Example 2: ["pop", "push 10", "push 20", "peek", "pop"]
#   - "pop"     → empty → -1,      results = [-1],        stack = []
#   - "push 10" → stack = [10],    results = [-1]
#   - "push 20" → stack = [10,20], results = [-1]
#   - "peek"    → top = 20,        results = [-1, 20],    stack unchanged
#   - "pop"     → top = 20,        results = [-1, 20, 20],stack = [10]
#   Expected: [-1, 20, 20]  ✓
# ---------------------------------------------------------------------------


if __name__ == "__main__":
    # Create a Solution instance
    sol = Solution()

    # ------------------------------------------------------------------
    # Test Case 1 (from the problem description)
    # ------------------------------------------------------------------
    ops1 = ["push 5", "push 3", "peek", "pop", "pop", "pop"]
    result1 = sol.simulate_tray_stack(ops1)
    print("Test Case 1")
    print(f"  Operations : {ops1}")
    print(f"  Output     : {result1}")
    print(f"  Expected   : [3, 3, 5, -1]")
    print(f"  Pass       : {result1 == [3, 3, 5, -1]}")
    print()

    # ------------------------------------------------------------------
    # Test Case 2 (from the problem description)
    # ------------------------------------------------------------------
    ops2 = ["pop", "push 10", "push 20", "peek", "pop"]
    result2 = sol.simulate_tray_stack(ops2)
    print("Test Case 2")
    print(f"  Operations : {ops2}")
    print(f"  Output     : {result2}")
    print(f"  Expected   : [-1, 20, 20]")
    print(f"  Pass       : {result2 == [-1, 20, 20]}")
    print()

    # ------------------------------------------------------------------
    # Test Case 3 — Edge case: all pushes, then all pops
    # ------------------------------------------------------------------
    ops3 = ["push 1", "push 2", "push 3", "pop", "pop", "pop", "pop"]
    result3 = sol.simulate_tray_stack(ops3)
    print("Test Case 3 — All pushes then all pops (extra pop on empty)")
    print(f"  Operations : {ops3}")
    print(f"  Output     : {result3}")
    print(f"  Expected   : [3, 2, 1, -1]")
    print(f"  Pass       : {result3 == [3, 2, 1, -1]}")
    print()

    # ------------------------------------------------------------------
    # Test Case 4 — Edge case: only peeks on an empty stack
    # ------------------------------------------------------------------
    ops4 = ["peek", "peek"]
    result4 = sol.simulate_tray_stack(ops4)
    print("Test Case 4 — Only peeks on empty stack")
    print(f"  Operations : {ops4}")
    print(f"  Output     : {result4}")
    print(f"  Expected   : [-1, -1]")
    print(f"  Pass       : {result4 == [-1, -1]}")
    print()

    # ------------------------------------------------------------------
    # Test Case 5 — Negative tray labels
    # ------------------------------------------------------------------
    ops5 = ["push -100", "push -200", "peek", "pop", "pop"]
    result5 = sol.simulate_tray_stack(ops5)
    print("Test Case 5 — Negative tray labels")
    print(f"  Operations : {ops5}")
    print(f"  Output     : {result5}")
    print(f"  Expected   : [-200, -200, -100]")
    print(f"  Pass       : {result5 == [-200, -200, -100]}")
```