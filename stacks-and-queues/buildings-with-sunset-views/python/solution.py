"""
Buildings With Sunset Views
============================
Problem Description:
You are given an array `heights` representing the heights of buildings standing
in a row from west to east (left to right). The sun sets in the west (to the
left of index 0).

A building has a sunset view if no building to its left (west) is strictly
taller than it. In other words, building i has a sunset view if heights[i] is
strictly greater than all heights[j] for every j < i.

However, due to construction updates, you receive buildings one by one from
east to west (i.e., in reverse order of the array). After processing all
buildings, return the indices of buildings that have a sunset view, sorted in
ascending order (west to east).

You must solve this problem using a stack as the primary data structure during
processing.

Constraints:
- 1 <= heights.length <= 10^5
- 1 <= heights[i] <= 10^9
- All heights are distinct.
"""

from typing import List


class Solution:
    def buildingsWithSunsetViews(self, heights: List[int]) -> List[int]:
        """
        Find all buildings that have a sunset view (no taller building to their left).

        We process buildings from east to west (right to left), using a stack
        to maintain candidates. The key insight: when processing from east to
        west, a building blocks all buildings to its east (right) that are
        shorter than it. We use a monotonic stack that keeps buildings in
        increasing order of height from top to bottom (i.e., the stack stores
        buildings where each new building added is taller than what's on top).

        Strategy:
        - Iterate from the last index (easternmost) to index 0 (westernmost).
        - For each building, pop from the stack any building that is shorter
          than the current building (those shorter buildings are blocked by
          the current one when viewed from the west).
        - Push the current building onto the stack.
        - At the end, the stack contains exactly the buildings with sunset views.

        Args:
            heights: List of building heights from west (index 0) to east.

        Returns:
            List of indices (in ascending order) of buildings with sunset views.

        Time Complexity:  O(n) — each building is pushed and popped at most once.
        Space Complexity: O(n) — the stack can hold at most n buildings.
        """

        # ---------------------------------------------------------------
        # STEP 1: Initialize the stack.
        # The stack will store (index, height) tuples of buildings that
        # are currently candidates for having a sunset view.
        # We use a list as a stack (append = push, pop = pop from end).
        # ---------------------------------------------------------------
        stack: List[tuple] = []  # Each element: (building_index, building_height)

        n = len(heights)

        # ---------------------------------------------------------------
        # STEP 2: Process buildings from east to west (right to left).
        #
        # Why east to west?
        # The problem says we receive buildings one by one from east to west.
        # Also, a building's sunset view depends on what's to its LEFT (west).
        # By processing right-to-left, when we encounter building i, we already
        # know about all buildings to its right (east). We can use the stack
        # to track which buildings to the right are still "visible" from the
        # west perspective — but actually, the stack here tracks which buildings
        # have NOT yet been blocked by a taller building coming from the west.
        #
        # Key invariant of our stack:
        # The stack maintains a monotonically DECREASING sequence of heights
        # from bottom to top. This means:
        #   - stack[0] (bottom) has the tallest building seen so far from the east.
        #   - stack[-1] (top) has the shortest building currently on the stack.
        #
        # When we process a new building (moving westward), if it is taller
        # than the building on top of the stack, the top building is BLOCKED
        # by the current building (since the current building is to the west
        # and is taller). We pop it. We keep popping until the stack is empty
        # or the top is taller than the current building.
        # Then we push the current building.
        # ---------------------------------------------------------------
        for i in range(n - 1, -1, -1):
            current_height = heights[i]

            # -----------------------------------------------------------
            # STEP 3: Pop buildings from the stack that are blocked.
            #
            # If the building currently on top of the stack has a height
            # LESS THAN OR EQUAL TO the current building's height, it means
            # the current building (which is to the WEST / left) will block
            # the top building's sunset view.
            #
            # Since all heights are distinct (per constraints), we only need
            # to check strictly less than. A building on the stack is blocked
            # if current_height >= stack top height. Because the current
            # building is to the west, if it's taller, it blocks the view.
            #
            # We pop all such blocked buildings.
            # -----------------------------------------------------------
            while stack and stack[-1][1] < current_height:
                # The building on top of the stack is shorter than the current
                # building. Since the current building is to the west (left),
                # it will block the top building. Remove it from candidates.
                stack.pop()

            # -----------------------------------------------------------
            # STEP 4: Push the current building onto the stack.
            #
            # The current building is now a candidate for having a sunset view.
            # It will remain on the stack unless a taller building to its west
            # is processed later and pops it off.
            # -----------------------------------------------------------
            stack.append((i, current_height))

        # ---------------------------------------------------------------
        # STEP 5: Collect results.
        #
        # After processing all buildings, the stack contains exactly the
        # buildings that have a sunset view. However, since we processed
        # from east to west, the stack's bottom has the westernmost building
        # (index 0 or the smallest index) and the top has the easternmost.
        #
        # Wait — let's think carefully:
        # We iterate i from n-1 down to 0.
        # We push buildings onto the stack as we go.
        # The stack is monotonically decreasing in height from bottom to top.
        # The LAST building pushed (i=0, westernmost) will be at the TOP
        # if it wasn't popped. The FIRST building pushed (i=n-1, easternmost)
        # will be at the BOTTOM if it survived.
        #
        # So stack[0] = easternmost surviving building (largest index)
        #    stack[-1] = westernmost surviving building (smallest index)
        #
        # We need to return indices in ASCENDING order (west to east),
        # so we reverse the stack order when collecting results.
        # ---------------------------------------------------------------

        # Extract indices from the stack in ascending order (west to east).
        # stack[-1] is the westernmost (smallest index), stack[0] is easternmost.
        # Reversing gives us ascending index order.
        result = [idx for idx, _ in reversed(stack)]

        return result


# ---------------------------------------------------------------------------
# Verification / Trace-through of Examples
# ---------------------------------------------------------------------------
# Example 1: heights = [5, 3, 8, 4, 6]
# Expected Output: [0, 2, 4]
#
# Process i=4 (height=6): stack=[], push (4,6). stack=[(4,6)]
# Process i=3 (height=4): stack top=(4,6), 6 > 4, no pop. push (3,4). stack=[(4,6),(3,4)]
# Process i=2 (height=8): stack top=(3,4), 4 < 8, pop. stack=[(4,6)]
#                          stack top=(4,6), 6 < 8, pop. stack=[]
#                          push (2,8). stack=[(2,8)]
# Process i=1 (height=3): stack top=(2,8), 8 > 3, no pop. push (1,3). stack=[(2,8),(1,3)]
# Process i=0 (height=5): stack top=(1,3), 3 < 5, pop. stack=[(2,8)]
#                          stack top=(2,8), 8 > 5, no pop. push (0,5). stack=[(2,8),(0,5)]
#
# Final stack (bottom to top): [(2,8), (0,5)]
# reversed: [(0,5), (2,8)] → indices [0, 2]
#
# Hmm, that gives [0, 2] but expected is [0, 2, 4].
# Building 4 (height=6) should have a sunset view because no building to its
# LEFT is taller than 6. Buildings to its left: heights[0]=5, heights[1]=3,
# heights[2]=8, heights[3]=4. Wait, heights[2]=8 > 6, so building 4 is BLOCKED
# by building 2!
#
# Let me re-read the problem...
# "Building 4 (height 6) is taller than buildings 3."
# The explanation says building 4 has a sunset view. But heights[2]=8 > heights[4]=6.
# That means building 2 is to the LEFT of building 4 and is taller...
#
# Wait, re-reading: "A building has a sunset view if no building to its left
# (west) is strictly taller than it."
# heights = [5, 3, 8, 4, 6]
# Building 4 (height=6): buildings to its left are indices 0,1,2,3 with heights
# 5, 3, 8, 4. heights[2]=8 > 6, so building 4 should NOT have a sunset view!
#
# But the problem says output is [0, 2, 4]... Let me re-read the explanation.
# "Building 4 (height 6) is taller than buildings 3."
# This seems wrong based on the stated definition. Unless the problem means
# something different...
#
# Actually wait — maybe I'm misreading the problem. Let me re-read:
# "A building has a sunset view if no building to its left (west) is strictly
# taller than it."
#
# For building 4 (height=6): left buildings are [5,3,8,4]. Max is 8 > 6.
# So building 4 should NOT have a sunset view by this definition.
#
# But the expected output includes 4. There might be an error in the problem
# statement's example, OR the definition is different from what I think.
#
# Alternative interpretation: maybe "sunset view" means the building can see
# the sun at some point, i.e., it's not completely blocked. Or maybe the
# problem means the building is taller than ALL buildings to its right?
#
# Actually, re-reading more carefully: "The sun sets in the west (to the left
# of index 0)." So the sun is to the LEFT. A building has a sunset view if
# it can see the sun, meaning no building between it and the sun (to its left)
# is taller than it.
#
# For building 4 (index 4, height 6): buildings to its left are at indices
# 0,1,2,3 with heights 5,3,8,4. Building at index 2 has height 8 > 6.
# So building 4 CANNOT see the sunset. The expected output [0,2,4] seems wrong
# for this definition.
#
# UNLESS: the problem actually means buildings that can see the sunset from
# the EAST side, i.e., buildings where no building to their RIGHT is taller.
# Let me check:
# Building 4 (height=6): no buildings to its right. Has sunset view. ✓
# Building 2 (height=8): buildings to right are [4,6]. 8>6 and 8>4. ✓
# Building 0 (height=5): buildings to right are [3,8,4,6]. 8>5, so blocked? ✗
#
# That doesn't work either.
#
# Let me try another interpretation: buildings that have a view of the sunset
# when looking WEST, meaning the building is taller than all buildings to its
# LEFT (so it can see over them).
# Building 0 (height=5): no buildings to left. ✓
# Building 1 (height=3): building 0 has height 5 > 3. ✗
# Building 2 (height=8): max of left = max(5,3) = 5 < 8. ✓
# Building 3 (height=4): max of left = max(5,3,8) = 8 > 4. ✗
# Building 4 (height=6): max of left = max(5,3,8,4) = 8 > 6. ✗
# Result: [0, 2] — doesn't match [0, 2, 4].
#
# Hmm. Let me try: buildings that can see the sunset = buildings where no
# building to their RIGHT is taller (sun sets in west, buildings face west,
# but a building can see the sunset if nothing to its east blocks the view?
# That doesn't make physical sense).
#
# Actually, I think the correct physical interpretation is:
# Sun is in the WEST. You're standing on top of a building looking WEST.
# A building blocks your view if it's to your WEST (left) and taller.
# So building i has sunset view if max(heights[0..i-1]) < heights[i].
# This gives [0, 2] for example 1, not [0, 2, 4].
#
# I think there might be an error in the problem's example. Let me try the
# OTHER common interpretation used in similar LeetCode problems:
# Buildings with sunset view = buildings where no building to their RIGHT
# (east) is taller. (As if the sun is in the east... or buildings face east.)
#
# Building 0 (height=5): right buildings [3,8,4,6]. max=8>5. ✗
# Building 1 (height=3): right buildings [8,4,6]. max=8>3. ✗
# Building 2 (height=8): right buildings [4,6]. max=6<8. ✓
# Building 3 (height=4): right buildings [6]. 6>4. ✗
# Building 4 (height=6): no right buildings. ✓
# Result: [2, 4] — doesn't match [0, 2, 4].
#
# None of these interpretations give [0, 2, 4] for heights=[5,3,8,4,6].
#
# Let me verify example 2: heights=[4,2,3,1], expected=[0,2].
# Using "taller than all to the left":
# Building 0 (height=4): no left. ✓
# Building 1 (height=2): left max=4>2. ✗
# Building 2 (height=3): left max=4>3. ✗
# Building 3 (height=1): left max=4>1. ✗
# Result: [0] — doesn't match [0,2].
#
# Using "taller than immediate left neighbor":
# Building 0: no left. ✓
# Building 1 (height=2): heights[0]=4>2. ✗
# Building 2 (height=3): heights[1]=2<3. ✓
# Building 3 (height=1): heights[2]=3>1. ✗
# Result: [0,2] — matches example 2!
#
# Let me verify example 1 with "taller than immediate left":
# Building 0 (height=5): no left. ✓
# Building 1 (height=3): heights[0]=5>3. ✗
# Building 2 (height=8): heights[1]=3<8. ✓
# Building 3 (height=4): heights[2]=8>4. ✗
# Building 4 (height=6): heights[3]=4<6. ✓
# Result: [0,2,4] — matches example 1!
#
# So the actual definition is: building i has a sunset view if heights[i] >
# heights[i-1] (or i==0). NOT "taller than ALL buildings to the left", but
# "taller than the IMMEDIATELY preceding building (or first building)".
#