```python
"""
Title: Squeeze Water Between Walls
Difficulty: Medium
Topic: Two Pointers

Problem Description:
You are given an integer array `heights` representing the heights of vertical walls
positioned at consecutive unit intervals along a horizontal axis. Between any two walls,
water can be trapped if the surrounding walls are tall enough. However, there is a twist:
each wall has a **durability** value given in a second integer array `durability`. A wall
can only hold water if its durability is strictly greater than the amount of water units
pressing against it (i.e., the water level at that wall's position).

Your task is to find the **maximum amount of water** that can be trapped between exactly
two walls (not necessarily adjacent), such that both chosen walls satisfy the durability
constraint. The water trapped between wall `i` and wall `j` (where `i < j`) is calculated as:

    water = min(heights[i], heights[j]) * (j - i)

A wall at index `k` satisfies the durability constraint if `durability[k] > min(heights[i], heights[j])`.

Return the maximum water that can be trapped. If no valid pair exists, return `0`.

Constraints:
- 2 <= heights.length <= 10^5
- heights.length == durability.length
- 1 <= heights[i] <= 10^4
- 1 <= durability[i] <= 10^4

Example 1:
    Input: heights = [1, 8, 6, 2, 5, 4, 8, 3, 7], durability = [10, 9, 7, 5, 6, 5, 9, 4, 8]
    Output: 49

Example 2:
    Input: heights = [3, 1, 2, 4], durability = [3, 5, 3, 4]
    Output: 4
"""

from typing import List


class Solution:
    def maxWater(self, heights: List[int], durability: List[int]) -> int:
        """
        Find the maximum water that can be trapped between two walls,
        subject to the durability constraint.

        The classic "Container With Most Water" problem uses a two-pointer approach.
        Here we add the durability constraint: both walls must have durability strictly
        greater than the water level (which equals min(heights[i], heights[j])).

        Because the durability constraint depends on the water level of a specific pair,
        we cannot simply skip walls in the two-pointer approach without potentially
        missing valid pairs. Therefore, we use a brute-force O(n^2) approach to
        correctly check all pairs, but we optimize with early termination where possible.

        However, for n up to 10^5, O(n^2) would be too slow (10^10 operations).
        We need a smarter approach.

        Key Insight for Optimization:
        We can still use the two-pointer technique, but we need to handle the durability
        check carefully. The two-pointer approach works as follows:
        - Start with left=0, right=n-1
        - Compute water for current pair if both walls satisfy durability
        - Move the pointer with the smaller height inward (standard container logic)
        - If a wall fails durability, we must skip it

        The tricky part: when a wall fails durability, we skip it. But we need to be
        careful about which pointer to move.

        Let's think about this more carefully:
        - water_level = min(heights[left], heights[right])
        - If heights[left] <= heights[right]:
            - water_level = heights[left]
            - durability[left] must be > heights[left] (i.e., > water_level)
            - durability[right] must be > heights[left] (i.e., > water_level)
            - If left fails: move left pointer right (left wall is the bottleneck)
            - If right fails: we need to try a different right wall, but moving right
              inward might miss valid pairs with the current left...
              Actually if right fails durability for this water level, any right wall
              with height >= heights[left] would produce the same or higher water level,
              so right still fails. We need a right wall with height < heights[left],
              but that would reduce water level. This is complex.

        Given the complexity of the two-pointer approach with durability constraints,
        and to ensure correctness, we'll use a modified approach:

        We iterate over all pairs using two pointers but handle durability by skipping
        invalid walls. The key observation is:

        If heights[left] <= heights[right]:
            water_level = heights[left]
            - If durability[left] <= water_level: left wall is invalid for ANY pair
              where it's the shorter wall. Move left pointer right.
            - If durability[right] <= water_level: right wall is invalid for this
              specific water level. Since moving right inward only decreases distance
              (and thus water), we should try moving left to see if a taller left wall
              changes the water level. But actually we should move right inward to find
              a valid right wall.
              Wait - but if we move right inward, we might miss valid pairs with the
              current left and a different right.

        This is getting complex. Let me reconsider.

        Actually, the safest correct approach for this problem size is O(n^2) with
        the note that for competitive programming, n=10^5 might need optimization.
        But let's implement the two-pointer approach correctly:

        The two-pointer approach: we move the pointer with the smaller height inward.
        If a wall fails durability, we skip it (move that pointer inward regardless).

        This might miss some cases, so let's think again...

        For correctness with the given constraints, let's use the two-pointer approach
        where we also skip walls that fail the durability check for the current pair.

        After careful analysis, the correct approach is:
        - Use two pointers left=0, right=n-1
        - At each step, compute water_level = min(heights[left], heights[right])
        - Check if both walls satisfy durability
        - If yes, update max_water
        - Move the pointer with the smaller height inward (if equal, move either)
        - If a wall fails durability, move that pointer inward (skip it)

        This works because:
        - The two-pointer approach guarantees we consider all "potentially optimal" pairs
        - When we move the shorter wall's pointer, we can only increase water by finding
          a taller wall
        - Durability failures on the shorter wall mean we skip it entirely
        - Durability failures on the taller wall: the water level is determined by the
          shorter wall, so the taller wall's durability must exceed the shorter wall's height.
          If it doesn't, we need to find a different taller wall.

        Let me trace through Example 2 to verify:
        heights = [3, 1, 2, 4], durability = [3, 5, 3, 4]
        Expected output: 4

        left=0, right=3: water_level=min(3,4)=3, dur[0]=3 NOT > 3 → invalid
            heights[0]=3 <= heights[3]=4, so move left: left=1
        left=1, right=3: water_level=min(1,4)=1, dur[1]=5>1, dur[3]=4>1 → valid, water=1*2=2
            heights[1]=1 <= heights[3]=4, move left: left=2
        left=2, right=3: water_level=min(2,4)=2, dur[2]=3>2, dur[3]=4>2 → valid, water=2*1=2
            heights[2]=2 <= heights[3]=4, move left: left=3
        left=3 >= right=3, stop.
        max_water = 2. But expected is 4!

        We missed pair (0,2): water=min(3,2)*2=4, dur[0]=3>2✓, dur[2]=3>2✓

        The two-pointer approach fails here because when left=0, right=3 was invalid
        (due to left wall failing), we moved left to 1, skipping the valid pair (0,2).

        So the two-pointer approach doesn't work directly. We need a different strategy.

        For n up to 10^5, we need an O(n log n) or O(n) solution.

        Let me think of another approach:
        For each pair (i, j), water_level = min(h[i], h[j]).
        Constraint: dur[i] > water_level AND dur[j] > water_level.

        This means: the effective height of wall i is min(h[i], dur[i]-1) when considering
        what water level it can support. Wait, not exactly...

        Actually: wall i can participate in a pair with water level w if dur[i] > w,
        i.e., w < dur[i], i.e., w <= dur[i] - 1.

        The water level w = min(h[i], h[j]).

        So we need: min(h[i], h[j]) < dur[i] AND min(h[i], h[j]) < dur[j].

        Let's define effective_height[i] = min(h[i], dur[i] - 1).
        Wait, that's not right either. Let me think again.

        If h[i] <= h[j]:
            water_level = h[i]
            constraint: dur[i] > h[i] AND dur[j] > h[i]
            So: dur[i] > h[i] (wall i's own durability must exceed its own height)
            AND dur[j] > h[i] (wall j's durability must exceed wall i's height)

        This is asymmetric and depends on which wall is shorter.

        Alternative view: water_level = min(h[i], h[j])
        Both dur[i] > min(h[i], h[j]) and dur[j] > min(h[i], h[j]) must hold.

        Note that dur[i] > min(h[i], h[j]) is equivalent to:
        - If h[i] <= h[j]: dur[i] > h[i]
        - If h[i] > h[j]: dur[i] > h[j], which means dur[i] > min(h[i], h[j]) = h[j]

        So for wall i (the taller wall), its durability must exceed the shorter wall's height.
        For wall i (the shorter wall), its durability must exceed its own height.

        Key insight: If dur[i] <= h[i], then wall i can NEVER be the shorter wall in a valid pair
        (because if it's the shorter wall, water_level = h[i], and dur[i] <= h[i] means dur[i]
        is not > h[i]).

        But wall i CAN be the taller wall if dur[i] > h[j] for some j where h[j] < h[i].

        This is still complex. Let me think of a cleaner formulation.

        For a pair (i, j) with i < j:
        Let w = min(h[i], h[j])
        Valid if: dur[i] > w AND dur[j] > w
        Water = w * (j - i)

        We want to maximize w * (j - i) subject to dur[i] > w and dur[j] > w.

        Since w = min(h[i], h[j]), we have w <= h[i] and w <= h[j].
        The constraint dur[i] > w means dur[i] > min(h[i], h[j]).

        Let's define cap[i] = min(h[i], dur[i] - 1) if dur[i] > 0 else 0.
        This represents the maximum water level that wall i can support.
        Actually cap[i] = dur[i] - 1 is the max water level wall i can hold.
        But the water level is also bounded by h[i].

        Hmm, but the water level w = min(h[i], h[j]), not min(h[i], h[j], dur[i]-1, dur[j]-1).

        The durability is a constraint, not a cap on water level.

        Let me try a different approach: for each pair, the water level is min(h[i], h[j]).
        The pair is valid if dur[i] > min(h[i], h[j]) AND dur[j] > min(h[i], h[j]).

        WLOG assume h[i] <= h[j] (i is the shorter wall):
        - w = h[i]
        - Need: dur[i] > h[i] (i's durability exceeds its own height)
        - Need: dur[j] > h[i] (j's durability exceeds i's height)

        So: wall i must have dur[i] > h[i] to ever be the shorter wall.
        Wall j must have dur[j] > h[i] to pair with wall i as the taller wall.

        Strategy:
        1. For each wall i where dur[i] > h[i] (can be the shorter wall):
           - Find the best j (j != i) where h[j] >= h[i] and dur[j] > h[i]
           - Best means maximizing h[i] * |j - i| = h[i] * (j - i) for j > i
             or h[i] * (i - j) for j < i
           - Since h[i] is fixed, we want to maximize |j - i|
           - So we want the leftmost valid j to the left of i, and rightmost valid j to the right

        2. Also consider pairs where h[i] > h[j] (j is the shorter wall):
           - This is symmetric, just swap roles

        This gives us an O(n^2) worst case still, but let's think about optimization.

        For a fixed shorter wall i (with dur[i] > h[i]):
        - We want the farthest j (either leftmost or rightmost) such that h[j] >= h[i] and dur[j] > h[i]
        - The water would be h[i] * |j - i|

        To find the farthest valid j efficiently:
        - Precompute for each i, the leftmost j < i with h[j] >= h[i] and dur[j] > h[i]
        - Precompute for each i, the rightmost j > i with h[j] >= h[i] and dur[j] > h[i]

        But this is still complex. For the purposes of this solution, given the constraints
        (n up to 10^5), let's implement the O(n^2) brute force with a note that it might
        TLE for large inputs, but it will be correct.

        Actually wait - let me reconsider the two-pointer approach more carefully.

        The issue with the standard two-pointer is that when we skip a wall due to durability,
        we might miss valid pairs. But what if we modify the approach?

        Modified two-pointer:
        - left=0, right=n-1
        - At each step:
          - Compute w = min(h[left], h[right])
          - Check validity
          - If valid, update max
          - Decide which pointer to move:
            - Always move the pointer with the smaller height (standard)
            - But if the smaller-height wall fails durability, we MUST move it
            - If the larger-height wall fails durability, we should also move it
              (but this might cause us to miss pairs)

        The fundamental issue: the two-pointer approach for "Container With Most Water"
        works because moving the shorter wall can only improve things. But with durability,
        a wall might be invalid as the shorter wall but valid as the taller wall.

        Example 2 trace:
        heights = [3, 1, 2, 4], durability = [3, 5, 3, 4]

        Pair (0,2): w=2, dur[0]=3>2✓, dur[2]=3>2✓, water=4 ← this is the answer
        Pair (0,3): w=3, dur[0]=3 NOT >3 ✗
        Pair (1,3): w=1, valid, water=2
        Pair (2,3): w=2, valid, water=2
        Pair (0,1): w=1, dur[0]=3>1✓, dur[1]=5>1✓, water=1
        Pair (1,2): w=1, valid, water=1

        So the answer is 4 from pair (0,2