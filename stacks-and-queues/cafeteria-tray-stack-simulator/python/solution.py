"""
Cafeteria Tray Stack Simulator
==============================

Problem Description:
A cafeteria has a spring-loaded tray dispenser that works like a stack — trays are
pushed onto the top and popped from the top. However, the cafeteria manager wants to
serve trays in a specific order based on customer requests.

You are given a list of `n` trays numbered from 1 to n that arrive one-by-one and are
pushed onto the stack in order (tray 1 first, tray 2 second, etc.). You are also given
a `requests` array representing the order in which customers want to receive trays.

Determine if it is possible to serve all customers in the exact order specified in
`requests` using only push and pop operations on a single stack. At each step, you may
push the next arriving tray onto the stack, or pop the top tray to serve a customer.
Return True if the requested sequence is achievable, otherwise return False.

Constraints:
- 1 <= n <= 1000
- requests is a permutation of integers from 1 to n
- You must use all n trays exactly once

Example 1:
    Input:  n = 3, requests = [2, 1, 3]
    Output: True
    Explanation: Push 1, Push 2, Pop 2 (serve customer 1),
                 Pop 1 (serve customer 2), Push 3, Pop 3 (serve customer 3).

Example 2:
    Input:  n = 3, requests = [3, 1, 2]
    Output: False
    Explanation: To serve tray 3 first, we must push 1, 2, 3 then pop 3.
                 Now tray 2 is on top but customer wants tray 1 next — impossible.
"""

from typing import List


class Solution:
    def can_serve_trays(self, n: int, requests: List[int]) -> bool:
        """
        Determine if the requested tray serving order is achievable using a single stack.

        The core idea is to simulate the push/pop process:
          - We have trays arriving in order 1, 2, ..., n (tracked by `next_tray`).
          - We have a stack where trays are temporarily held.
          - We have a pointer into `requests` for the next customer to serve.
          - At each moment, we either push the next arriving tray OR pop to serve
            the current customer — whichever is needed.

        Args:
            n        (int):       Total number of trays (numbered 1 to n).
            requests (List[int]): The desired serving order (a permutation of 1..n).

        Returns:
            bool: True if the sequence is achievable, False otherwise.

        Time Complexity:  O(n) — each tray is pushed once and popped once.
        Space Complexity: O(n) — the stack can hold at most n trays in the worst case.
        """

        # -----------------------------------------------------------------------
        # DATA STRUCTURES SETUP
        # -----------------------------------------------------------------------

        # `stack` simulates the spring-loaded tray dispenser.
        # Trays are pushed on top and popped from the top (LIFO order).
        stack: List[int] = []

        # `next_tray` tracks the next tray number that will arrive and be pushed.
        # Trays arrive in order 1, 2, 3, ..., n.
        next_tray: int = 1

        # `req_idx` is a pointer into the `requests` list.
        # It tells us which customer we are currently trying to serve.
        req_idx: int = 0

        # -----------------------------------------------------------------------
        # MAIN SIMULATION LOOP
        # -----------------------------------------------------------------------
        # We continue until every customer has been served (req_idx reaches n).
        # At each iteration we decide: push the next tray, or pop to serve.

        while req_idx < n:
            # -------------------------------------------------------------------
            # CASE 1: The top of the stack already has the tray the customer wants.
            # -------------------------------------------------------------------
            # If the stack is non-empty AND its top matches the current request,
            # we pop it immediately to serve that customer.
            # We do NOT push a new tray in this case — we serve first.
            if stack and stack[-1] == requests[req_idx]:
                # Pop the top tray — it satisfies the current customer's request.
                stack.pop()
                # Move to the next customer in the requests list.
                req_idx += 1
                # Continue the loop to check if the new top also satisfies the
                # next customer (chain of pops is possible).
                continue

            # -------------------------------------------------------------------
            # CASE 2: The top of the stack does NOT match the current request.
            # We need to push more trays until we push the one the customer wants.
            # -------------------------------------------------------------------
            # If there are still trays left to arrive (next_tray <= n), push the
            # next one onto the stack.
            if next_tray <= n:
                # Push the next arriving tray onto the stack.
                stack.append(next_tray)
                # Advance the counter so the next push will use the following tray.
                next_tray += 1
                # Loop back: after pushing, check if the new top satisfies the
                # current customer (handled by CASE 1 on the next iteration).
                continue

            # -------------------------------------------------------------------
            # CASE 3: Stack top doesn't match AND no more trays to push.
            # -------------------------------------------------------------------
            # This means:
            #   - The tray the customer wants is buried somewhere in the stack
            #     (below other trays), AND
            #   - We cannot push any more trays to "reach" it.
            # Since a stack only allows access to the top, we are stuck.
            # The requested order is IMPOSSIBLE.
            return False

        # -----------------------------------------------------------------------
        # SUCCESS: All customers have been served in the requested order.
        # -----------------------------------------------------------------------
        return True


# ---------------------------------------------------------------------------
# MAIN BLOCK — Demonstrates the solution with sample inputs and prints results
# ---------------------------------------------------------------------------
if __name__ == "__main__":
    solver = Solution()

    # ------------------------------------------------------------------
    # Example 1 (from problem description)
    # Expected Output: True
    # Trace:
    #   req_idx=0, want=2 | stack=[]        → push 1  → stack=[1]
    #   req_idx=0, want=2 | stack=[1]       → push 2  → stack=[1,2]
    #   req_idx=0, want=2 | stack=[1,2]     → top==2, pop → stack=[1], req_idx=1
    #   req_idx=1, want=1 | stack=[1]       → top==1, pop → stack=[],  req_idx=2
    #   req_idx=2, want=3 | stack=[]        → push 3  → stack=[3]
    #   req_idx=2, want=3 | stack=[3]       → top==3, pop → stack=[],  req_idx=3
    #   req_idx==n → return True
    # ------------------------------------------------------------------
    n1 = 3
    requests1 = [2, 1, 3]
    result1 = solver.can_serve_trays(n1, requests1)
    print(f"Example 1: n={n1}, requests={requests1}")
    print(f"  Output : {result1}   (Expected: True)")
    print()

    # ------------------------------------------------------------------
    # Example 2 (from problem description)
    # Expected Output: False
    # Trace:
    #   req_idx=0, want=3 | stack=[]        → push 1  → stack=[1]
    #   req_idx=0, want=3 | stack=[1]       → push 2  → stack=[1,2]
    #   req_idx=0, want=3 | stack=[1,2]     → push 3  → stack=[1,2,3]
    #   req_idx=0, want=3 | stack=[1,2,3]   → top==3, pop → stack=[1,2], req_idx=1
    #   req_idx=1, want=1 | stack=[1,2]     → top==2 ≠ 1, no more pushes? next_tray=4>3
    #   → return False
    # ------------------------------------------------------------------
    n2 = 3
    requests2 = [3, 1, 2]
    result2 = solver.can_serve_trays(n2, requests2)
    print(f"Example 2: n={n2}, requests={requests2}")
    print(f"  Output : {result2}   (Expected: False)")
    print()

    # ------------------------------------------------------------------
    # Additional Example 3: Natural order (always possible)
    # requests = [1, 2, 3] → push 1, pop 1, push 2, pop 2, push 3, pop 3
    # Expected Output: True
    # ------------------------------------------------------------------
    n3 = 3
    requests3 = [1, 2, 3]
    result3 = solver.can_serve_trays(n3, requests3)
    print(f"Example 3: n={n3}, requests={requests3}")
    print(f"  Output : {result3}   (Expected: True)")
    print()

    # ------------------------------------------------------------------
    # Additional Example 4: Reverse order (always possible)
    # requests = [3, 2, 1] → push 1,2,3 then pop 3, pop 2, pop 1
    # Expected Output: True
    # ------------------------------------------------------------------
    n4 = 3
    requests4 = [3, 2, 1]
    result4 = solver.can_serve_trays(n4, requests4)
    print(f"Example 4: n={n4}, requests={requests4}")
    print(f"  Output : {result4}   (Expected: True)")
    print()

    # ------------------------------------------------------------------
    # Additional Example 5: Single tray
    # Expected Output: True
    # ------------------------------------------------------------------
    n5 = 1
    requests5 = [1]
    result5 = solver.can_serve_trays(n5, requests5)
    print(f"Example 5: n={n5}, requests={requests5}")
    print(f"  Output : {result5}   (Expected: True)")
    print()

    # ------------------------------------------------------------------
    # Additional Example 6: Larger impossible case
    # requests = [2, 4, 3, 1] — let's trace:
    #   want=2: push 1, push 2, pop 2 → stack=[1], req_idx=1
    #   want=4: push 3, push 4, pop 4 → stack=[1,3], req_idx=2
    #   want=3: top==3, pop 3         → stack=[1],   req_idx=3
    #   want=1: top==1, pop 1         → stack=[],    req_idx=4
    # Expected Output: True
    # ------------------------------------------------------------------
    n6 = 4
    requests6 = [2, 4, 3, 1]
    result6 = solver.can_serve_trays(n6, requests6)
    print(f"Example 6: n={n6}, requests={requests6}")
    print(f"  Output : {result6}   (Expected: True)")
    print()

    # ------------------------------------------------------------------
    # Additional Example 7: Another impossible case
    # requests = [4, 2, 3, 1]
    #   want=4: push 1,2,3,4, pop 4 → stack=[1,2,3], req_idx=1
    #   want=2: top==3 ≠ 2, no more pushes → return False
    # Expected Output: False
    # ------------------------------------------------------------------
    n7 = 4
    requests7 = [4, 2, 3, 1]
    result7 = solver.can_serve_trays(n7, requests7)
    print(f"Example 7: n={n7}, requests={requests7}")
    print(f"  Output : {result7}   (Expected: False)")