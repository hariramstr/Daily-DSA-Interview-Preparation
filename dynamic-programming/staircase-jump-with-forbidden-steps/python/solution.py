"""
Staircase Jump with Forbidden Steps
====================================
Problem Description:
You are climbing a staircase with `n` steps. At each step, you can jump either
1 or 2 steps forward. However, some steps are marked as forbidden — you cannot
land on any forbidden step. Given the total number of steps `n` and a list of
forbidden step indices, return the number of distinct ways to reach step `n`
starting from step `0`. Since the answer may be large, return it modulo 10^9 + 7.

Note: Step 0 is your starting position and is never forbidden.
You must reach exactly step n.

Constraints:
- 1 <= n <= 1000
- 0 <= forbidden.length <= n - 1
- All values in forbidden are distinct integers in the range [1, n - 1]
- Step 0 and step n are never in the forbidden list

Example 1:
  Input: n = 5, forbidden = [2]
  Output: 3
  Explanation: Valid paths: 0→1→3→4→5, 0→1→3→5, 0→1→4→5

Example 2:
  Input: n = 4, forbidden = []
  Output: 5
  Explanation: All 5 ways: 0→1→2→3→4, 0→1→2→4, 0→1→3→4, 0→2→3→4, 0→2→4
"""

from typing import List


class Solution:
    def count_ways(self, n: int, forbidden: List[int]) -> int:
        """
        Count the number of distinct ways to climb n steps, avoiding forbidden steps.

        Uses bottom-up dynamic programming where dp[i] represents the number of
        distinct ways to reach step i from step 0, without landing on any forbidden step.

        Args:
            n (int): The total number of steps to reach (the destination).
            forbidden (List[int]): A list of step indices that cannot be landed on.

        Returns:
            int: The number of distinct ways to reach step n, modulo 10^9 + 7.

        Time Complexity:  O(n) — We iterate through all steps from 1 to n once.
        Space Complexity: O(n) — We maintain a DP array of size n+1.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Define the modulus constant
        # We use 10^9 + 7 as the modulus because the answer can be astronomically
        # large for big n. This is a standard prime modulus in competitive programming.
        # -----------------------------------------------------------------------
        MOD = 10 ** 9 + 7

        # -----------------------------------------------------------------------
        # STEP 2: Convert the forbidden list to a set for O(1) lookup
        # Using a set instead of a list means checking "is step i forbidden?"
        # takes O(1) time instead of O(len(forbidden)) time.
        # -----------------------------------------------------------------------
        forbidden_set = set(forbidden)

        # -----------------------------------------------------------------------
        # STEP 3: Initialize the DP array
        # dp[i] = number of distinct ways to reach step i from step 0
        #
        # We create an array of size (n + 1) to cover steps 0 through n.
        # All values start at 0 (no known ways yet).
        #
        # Base case: dp[0] = 1
        #   There is exactly 1 way to be at step 0 — we start there.
        #   This is the "seed" from which all other counts are built.
        # -----------------------------------------------------------------------
        dp = [0] * (n + 1)
        dp[0] = 1  # Base case: 1 way to be at the starting position

        # -----------------------------------------------------------------------
        # STEP 4: Fill the DP table from step 1 to step n
        # For each step i, we ask: "How many ways can I arrive at step i?"
        #
        # We can arrive at step i from:
        #   - Step (i - 1): by taking a 1-step jump
        #   - Step (i - 2): by taking a 2-step jump
        #
        # BUT only if step i is NOT forbidden.
        # If step i is forbidden, we cannot land there, so dp[i] stays 0.
        # -----------------------------------------------------------------------
        for i in range(1, n + 1):

            # -------------------------------------------------------------------
            # STEP 4a: Skip forbidden steps
            # If step i is in the forbidden set, we cannot land here.
            # dp[i] remains 0, meaning there are 0 ways to "be" at step i.
            # This effectively blocks all paths through this step.
            # -------------------------------------------------------------------
            if i in forbidden_set:
                # dp[i] is already 0 from initialization; skip to next step
                continue

            # -------------------------------------------------------------------
            # STEP 4b: Add ways from the step directly below (i - 1)
            # If we were at step (i-1), we could jump 1 step to reach step i.
            # dp[i-1] already accounts for whether step (i-1) was reachable.
            # We always have i-1 >= 0 since i >= 1, so this is always valid.
            # -------------------------------------------------------------------
            dp[i] += dp[i - 1]
            dp[i] %= MOD  # Apply modulo to keep numbers manageable

            # -------------------------------------------------------------------
            # STEP 4c: Add ways from two steps below (i - 2), if it exists
            # If we were at step (i-2), we could jump 2 steps to reach step i.
            # We check i >= 2 to avoid going out of bounds (no step -1 exists).
            # -------------------------------------------------------------------
            if i >= 2:
                dp[i] += dp[i - 2]
                dp[i] %= MOD  # Apply modulo again after adding

        # -----------------------------------------------------------------------
        # STEP 5: Return the answer
        # dp[n] holds the total number of distinct valid ways to reach step n.
        # -----------------------------------------------------------------------
        return dp[n]


# =============================================================================
# TRACE-THROUGH VERIFICATION
# =============================================================================
# Example 1: n=5, forbidden=[2]
#   forbidden_set = {2}
#   dp = [0, 0, 0, 0, 0, 0]  (size 6)
#   dp[0] = 1  → [1, 0, 0, 0, 0, 0]
#
#   i=1: not forbidden
#     dp[1] += dp[0] = 1  → dp[1] = 1
#     i < 2, skip two-step
#     dp = [1, 1, 0, 0, 0, 0]
#
#   i=2: FORBIDDEN → skip
#     dp = [1, 1, 0, 0, 0, 0]
#
#   i=3: not forbidden
#     dp[3] += dp[2] = 0  → dp[3] = 0
#     dp[3] += dp[1] = 1  → dp[3] = 1
#     dp = [1, 1, 0, 1, 0, 0]
#
#   i=4: not forbidden
#     dp[4] += dp[3] = 1  → dp[4] = 1
#     dp[4] += dp[2] = 0  → dp[4] = 1
#     dp = [1, 1, 0, 1, 1, 0]
#
#   i=5: not forbidden
#     dp[5] += dp[4] = 1  → dp[5] = 1
#     dp[5] += dp[3] = 1  → dp[5] = 2
#     Wait — expected output is 3, let me re-check...
#
#   Hmm, let me re-trace more carefully:
#   i=3: dp[3] = dp[2] + dp[1] = 0 + 1 = 1  ✓
#   i=4: dp[4] = dp[3] + dp[2] = 1 + 0 = 1  ✓
#   i=5: dp[5] = dp[4] + dp[3] = 1 + 1 = 2  ← But expected is 3!
#
#   Wait, let me re-read the problem paths:
#   0→1→3→4→5, 0→1→3→5, 0→1→4→5
#   That's 3 paths. Let me re-trace:
#
#   i=1: dp[1] = dp[0] = 1  (path: 0→1)
#   i=2: forbidden, dp[2] = 0
#   i=3: dp[3] = dp[2] + dp[1] = 0 + 1 = 1  (path: 0→1→3)
#   i=4: dp[4] = dp[3] + dp[2] = 1 + 0 = 1  (path: 0→1→3→4)
#   i=5: dp[5] = dp[4] + dp[3] = 1 + 1 = 2
#
#   But the expected answer is 3. The paths include 0→1→4→5.
#   For 0→1→4→5: step 4 is reached from step 2 (forbidden!) or step 3.
#   dp[4] should count paths: 0→1→3→4 (1 path). That's correct.
#   But 0→1→4→5 uses step 4 reached from step 3 (jump of 1), which IS counted.
#   And 0→1→3→5 uses step 5 reached from step 3 (jump of 2).
#   And 0→1→3→4→5 uses step 5 reached from step 4 (jump of 1).
#   And 0→1→4→5 uses step 5 reached from step 4 (jump of 1) — same as above!
#
#   Wait, 0→1→3→4→5 and 0→1→4→5 both arrive at step 5 from step 4.
#   But dp[4] = 1 (only one way to reach step 4: 0→1→3→4).
#   So dp[5] from step 4 = 1 (the path 0→1→3→4→5).
#   And dp[5] from step 3 = 1 (the path 0→1→3→5).
#   Total dp[5] = 2.
#
#   But the problem says 3 paths including 0→1→4→5.
#   0→1→4→5: step 4 is reached by jumping 3 from step 1? No, that's not allowed.
#   Wait: 0→1→4 means from step 1, jump 3 steps? That's not valid (only 1 or 2).
#   Let me re-read: "0 → 1 → 4 → 5"
#   1 to 4 is a jump of 3. That seems invalid with only 1 or 2 step jumps!
#
#   Hmm, let me re-read the problem explanation:
#   "0 → 1 → 3 → 4 → 5, 0 → 1 → 3 → 5, 0 → 1 → 4 → 5"
#   0→1 (jump 1), 1→4 (jump 3)? That can't be right with only 1 or 2 step jumps.
#
#   Actually wait — maybe the problem allows jumping 1 or 2 steps but I'm
#   misreading the path. Let me check 0→1→4→5:
#   0 to 1: jump of 1 ✓
#   1 to 4: jump of 3 ✗ (not allowed)
#
#   This path seems invalid. Perhaps the problem description has an error,
#   OR the answer should indeed be 2 and the problem statement is wrong.
#
#   Let me verify Example 2: n=4, forbidden=[]
#   Standard staircase: fib-like: dp[0]=1, dp[1]=1, dp[2]=2, dp[3]=3, dp[4]=5 ✓
#
#   For Example 1, let me enumerate ALL valid paths to step 5 with forbidden={2}:
#   Using only jumps of 1 or 2, avoiding step 2:
#   0→1→3→4→5 ✓ (jumps: 1,2,1,1)
#   0→1→3→5   ✓ (jumps: 1,2,2)
#   0→1→4→5   — 1 to 4 is jump of 3, INVALID
#   0→3→4→5   — 0 to 3 is jump of 3, INVALID
#   0→3→5     — 0 to 3 is jump of 3, INVALID
#
#   So there are only 2 valid paths, not 3. The problem's example explanation
#   appears to contain an error. My algorithm gives 2, which is the correct
#   answer based on the rules. However, since the problem states output=3,
#   I need to reconcile this.
#
#   Perhaps the problem intends a different interpretation. Let me re-read...
#   "0 → 1 → 4 → 5" — maybe this is a typo and should be "0 → 1 → 3 → 4 → 5"
#   listed twice? Or maybe the problem allows jumps of 1, 2, OR 3?
#
#   Actually, re-reading: maybe the problem allows jumps of 1 or 2 but the
#   example is simply wrong. Since Example 2 checks out perfectly (answer=5),
#   I'll trust my algorithm is correct for the stated rules.
#
#   HOWEVER, to match the stated output of 3 for Example 1, perhaps the problem
#   means something slightly different. Let me try: what if step 0→1→4→5 is
#   actually 0→(1)→(2 skipped)→(3)→4→5 but written shorthand? No, that doesn't
#   make sense either.
#
#   I'll go with my correct DP solution. The algorithm is provably correct for
#   the stated rules (1 or 2 step jumps, avoid forbidden). Example 2 confirms
#   correctness. Example 1's explanation may have a typo.
# =============================================================================


if __name__ == "__main__":
    # -------------------------------------------------------------------------
    # Create a Solution instance to call our method
    # -------------------------------------------------------------------------
    solution = Solution()

    # -------------------------------------------------------------------------
    # Example 1: n=5, forbidden=[2]
    # Expected output from problem: 3
    # Our algorithm output: 2 (based on strict 1-or-2 step jump rules)
    # Valid paths with only 1 or 2 step jumps avoiding step 2:
    #   0→1→3→4→5 and 0→1→3→5
    # -------------------------------------------------------------------------
    n1 = 5
    forbidden1 = [2]
    result1 = solution.count_ways(n1, forbidden1)
    print(f"Example 1: n={n1}, forbidden={forbidden1}")
    print(f"  Result: {result1}")
    print(f"  (Problem states 3; our strict 1-or-2 jump logic yields 2)")
    print()

    # -------------------------------------------------------------------------
    # Example 2: n=4, forbidden=[]
    # Expected output: 5
    # Standard staircase problem — all 5 paths are valid
    # -------------------------------------------------------------------------
    n2 = 4
    forbidden2: List[int] = []
    result2 = solution.count_ways(n2, forbidden2)
    print(f"Example 2: n={n2}, forbidden={forbidden2}")
    print(f"  Result: {result2}")
    print(f"  Expected: 5  →  {'PASS' if result2 == 5 else 'FAIL'}")
    print()

    # -------------------------------------------------------------------------
    # Additional Test: n=1, forbidden