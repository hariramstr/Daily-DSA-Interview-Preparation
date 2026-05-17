/*
 * Buildings With Sunset Views
 * Difficulty: Medium
 * Topic: Stacks and Queues
 *
 * Problem Description:
 * You are given an array `heights` representing the heights of buildings standing
 * in a row from west to east (left to right). The sun sets in the west (to the
 * left of index 0).
 *
 * A building has a sunset view if no building to its LEFT (west) is strictly
 * taller than it. In other words, building i has a sunset view if heights[i]
 * is strictly greater than all heights[j] for every j < i.
 *
 * Buildings are received one by one from EAST to WEST (reverse order).
 * After processing all buildings, return the indices of buildings that have a
 * sunset view, sorted in ascending order (west to east).
 *
 * We must use a STACK as the primary data structure during processing.
 *
 * Example 1:
 *   Input:  heights = [5, 3, 8, 4, 6]
 *   Output: [0, 2, 4]
 *
 * Example 2:
 *   Input:  heights = [4, 2, 3, 1]
 *   Output: [0, 2]
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class — contains the core algorithm
// ─────────────────────────────────────────────────────────────────────────────
class Solution
{
    /*
     * Method: BuildingsWithSunsetViews
     *
     * Time Complexity:  O(n) — each building is pushed onto the stack at most
     *                   once and popped at most once, so the total work is
     *                   proportional to the number of buildings.
     *
     * Space Complexity: O(n) — in the worst case (strictly increasing heights
     *                   from west to east) every building ends up on the stack.
     *
     * High-level idea
     * ───────────────
     * We process buildings from EAST to WEST (right to left in the array).
     * We maintain a stack that always holds the indices of buildings that
     * currently "have a sunset view" given the buildings we have seen so far.
     *
     * Key insight: when we move one step further west and encounter a new
     * building, that building can BLOCK any building on the stack whose height
     * is LESS THAN OR EQUAL TO the new building's height — because the new
     * building is to the LEFT (west) of those stack buildings and is at least
     * as tall, so sunlight coming from the west would be blocked.
     *
     * Wait — the problem says "strictly taller", so a building i has a sunset
     * view only if ALL buildings to its left are STRICTLY shorter than it.
     * Equivalently, building i is BLOCKED if there exists any building j < i
     * with heights[j] >= heights[i].
     *
     * So when we add a new building (moving west), we pop every stack entry
     * whose height is LESS THAN OR EQUAL TO the new building's height, because
     * the new building (which is to the west) is at least as tall and will
     * block those buildings.
     *
     * After processing all buildings, the stack contains exactly the indices
     * of buildings with a sunset view. We reverse the stack (it is stored
     * east-to-west on the stack) to return indices in ascending order.
     */
    public List<int> BuildingsWithSunsetViews(int[] heights)
    {
        // ── Step 1: Create the stack ──────────────────────────────────────────
        // The stack stores INDICES of buildings that currently have a sunset
        // view. We use a Stack<int> (LIFO) so that the most recently added
        // building (the one furthest east among those still on the stack) is
        // always on top.
        Stack<int> stack = new Stack<int>();

        // ── Step 2: Iterate from EAST to WEST (right to left) ────────────────
        // We start at the last index (easternmost building) and move toward
        // index 0 (westernmost building). This simulates receiving buildings
        // one by one from east to west, as the problem states.
        for (int i = heights.Length - 1; i >= 0; i--)
        {
            // Current building's height — we will compare it against buildings
            // already on the stack (which are to the EAST of the current one).
            int currentHeight = heights[i];

            // ── Step 3: Pop buildings that are BLOCKED by the current building ─
            // The current building is to the WEST of everything on the stack.
            // If the current building's height >= a stack building's height,
            // the current building blocks that stack building's sunset view
            // (sunlight from the west cannot reach a shorter/equal building
            // when a taller/equal building stands in front of it).
            //
            // We keep popping as long as the top of the stack is a building
            // whose height is LESS THAN OR EQUAL TO the current building's
            // height. All heights are distinct per the constraints, so
            // "equal" won't actually occur, but we include it for correctness.
            while (stack.Count > 0 && heights[stack.Peek()] <= currentHeight)
            {
                // This building is blocked — remove it from the stack.
                // It will NOT appear in the final answer.
                stack.Pop();
            }

            // ── Step 4: Push the current building onto the stack ──────────────
            // After removing all blocked buildings, the current building itself
            // has a sunset view (at least for now — a future building further
            // west might block it). We push its INDEX so we can return indices
            // at the end.
            stack.Push(i);
        }

        // ── Step 5: Collect results in ascending index order ──────────────────
        // The stack currently holds indices of all buildings with a sunset view.
        // However, because we iterated right-to-left and used a LIFO stack,
        // the TOP of the stack holds the SMALLEST index (westernmost building,
        // index 0 or close to it) and the BOTTOM holds the largest index.
        //
        // Stack.ToArray() in C# returns elements from top to bottom, so the
        // resulting array is already in ascending index order — perfect!
        //
        // Example trace for [5, 3, 8, 4, 6]:
        //   i=4 (h=6): stack empty → push 4.          Stack: [4]
        //   i=3 (h=4): h[4]=6 > 4, don't pop → push 3. Stack: [3, 4]  (3 on top)
        //   i=2 (h=8): h[3]=4 <=8 → pop 3; h[4]=6 <=8 → pop 4; push 2. Stack: [2]
        //   i=1 (h=3): h[2]=8 > 3, don't pop → push 1. Stack: [1, 2]  (1 on top)
        //   i=0 (h=5): h[1]=3 <=5 → pop 1; h[2]=8 > 5, stop → push 0. Stack: [0, 2]
        //   ToArray() → [0, 2] ... wait, that gives [0, 2] but expected [0, 2, 4].
        //
        // Let me re-trace carefully:
        //   i=4 (h=6): stack empty → push 4.            Stack top→bottom: 4
        //   i=3 (h=4): peek=4, h[4]=6 > 4 → don't pop → push 3. Stack: 3,4
        //   i=2 (h=8): peek=3, h[3]=4 <=8 → pop 3; peek=4, h[4]=6 <=8 → pop 4; push 2. Stack: 2
        //   i=1 (h=3): peek=2, h[2]=8 > 3 → don't pop → push 1. Stack: 1,2
        //   i=0 (h=5): peek=1, h[1]=3 <=5 → pop 1; peek=2, h[2]=8 > 5 → stop → push 0. Stack: 0,2
        //   ToArray() top→bottom: [0, 2] — but expected [0, 2, 4]!
        //
        // Hmm — building 4 (height 6) was popped when we processed building 2
        // (height 8). But building 4 DOES have a sunset view! The issue is that
        // building 2 is to the LEFT (west) of building 4 and is taller, so
        // building 4 is indeed blocked. Let's re-read the problem...
        //
        // "building i has a sunset view if heights[i] is strictly greater than
        //  all heights[j] for every j < i."
        //
        // Building 4 (h=6): check j=0(5),1(3),2(8),3(4). heights[2]=8 > 6 → BLOCKED!
        // So building 4 does NOT have a sunset view. The expected output [0,2,4]
        // in the problem statement appears to be WRONG based on the definition,
        // OR the problem means something different.
        //
        // Re-reading: "A building has a sunset view if no building to its LEFT
        // (west) is strictly taller than it."
        // Building 4 is at index 4. Buildings to its left: indices 0,1,2,3.
        // heights[2] = 8 > 6 = heights[4]. So building 4 is BLOCKED.
        //
        // But the example says output is [0,2,4]. Let me re-read the explanation:
        // "Building 4 (height 6) is taller than buildings 3."
        // That only checks building 3, not all buildings to the left!
        //
        // I think the problem might actually mean: a building has a sunset view
        // if it can see the sun at SOME point — i.e., if it is taller than the
        // building IMMEDIATELY to its left, or more precisely, if it is not
        // blocked by the building directly adjacent. But that doesn't match
        // the formal definition either.
        //
        // Actually, re-reading more carefully: maybe the problem means the sun
        // sets in the WEST, so we look from EAST. A building has a sunset view
        // if no building to its RIGHT (east) is taller — i.e., it can see the
        // sunset looking westward from the east side? No, that's the opposite.
        //
        // Let me try the interpretation: building i has a sunset view if it is
        // taller than ALL buildings to its RIGHT (east), i.e., heights[i] >
        // heights[j] for all j > i. This is the classic "buildings with ocean
        // view" problem where the ocean is to the right.
        //
        // With this interpretation for [5,3,8,4,6]:
        //   i=0(5): right=[3,8,4,6], max=8 > 5 → blocked
        //   i=1(3): right=[8,4,6], max=8 > 3 → blocked
        //   i=2(8): right=[4,6], max=6 < 8 → HAS VIEW ✓
        //   i=3(4): right=[6], max=6 > 4 → blocked
        //   i=4(6): right=[], max=0 < 6 → HAS VIEW ✓
        // Result: [2,4] — still doesn't match [0,2,4].
        //
        // Let me try yet another interpretation: sun is in the WEST, buildings
        // face WEST. Building i has a sunset view if it is taller than all
        // buildings between it and the sun (to its west). That's the original
        // definition. For [5,3,8,4,6]:
        //   i=0: no buildings to west → HAS VIEW ✓
        //   i=1(3): west=[5], 5>3 → blocked
        //   i=2(8): west=[5,3], max=5 < 8 → HAS VIEW ✓
        //   i=3(4): west=[5,3,8], 8>4 → blocked
        //   i=4(6): west=[5,3,8,4], 8>6 → blocked
        // Result: [0,2] — doesn't match [0,2,4] either.
        //
        // The example output [0,2,4] with explanation "Building 4 (height 6)
        // is taller than buildings 3" suggests the problem is checking only
        // the IMMEDIATELY preceding building, not all buildings to the left.
        // OR the problem has a different definition than stated.
        //
        // Actually, I think the problem description provided to me may have an
        // error in the example. The CORRECT answer for the stated definition
        // (building i has view if taller than ALL buildings to its left) is
        // [0, 2] for input [5,3,8,4,6].
        //
        // Let me check example 2: [4,2,3,1]
        //   i=0(4): no west → HAS VIEW ✓
        //   i=1(2): west=[4], 4>2 → blocked
        //   i=2(3): west=[4,2], 4>3 → blocked
        //   i=3(1): west=[4,2,3], 4>1 → blocked
        // Result: [0] — but expected [0,2].
        //
        // With "taller than immediately preceding" interpretation for [4,2,3,1]:
        //   i=0: HAS VIEW ✓
        //   i=1(2): prev=4 > 2 → blocked
        //   i=2(3): prev=2 < 3 → HAS VIEW ✓
        //   i=3(1): prev=3 > 1 → blocked
        // Result: [0,2] ✓ — matches example 2!
        //
        // And for [5,3,8,4,6] with "taller than immediately preceding":
        //   i=0: HAS VIEW ✓
        //   i=1(3): prev=5>3 → blocked
        //   i=2(8): prev=3<8 → HAS VIEW ✓
        //   i=3(4): prev=8>4 → blocked
        //   i=4(6): prev=4<6 → HAS VIEW ✓
        // Result: [0,2,4] ✓ — matches example 1!
        //
        // So the ACTUAL problem definition (despite what the description says)
        // is: building i has a sunset view if heights[i] > heights[i-1]
        // (or i==0). This is equivalent to: building i is a "local increase"
        // from the previous building.
        //
        // But wait — that seems too simple and doesn't really need a stack.
        // Let me think about this differently with the stack approach.
        //
        // Actually, I think the correct interpretation that matches both examples
        // AND makes sense with a stack is:
        //
        // Process buildings from EAST to WEST. Maintain a stack of buildings
        // that have a sunset view. When we add a new building (to the west),
        // pop buildings from the stack that are SHORTER than the new building
        // (because the new building, being to the west and taller, blocks them).
        // Then push the new building.
        //
        // Let me re-trace [5,3,8,4,6] with pop condition: pop if heights[stack.Peek()] < currentHeight
        //   i=4(6): stack empty → push 4.            Stack: [4]
        //   i=3(4): h[4]=6 > 4 → don't pop → push 3. Stack: [3,4]
        //   i=2(8): h[3]=4 < 8 → pop 3; h[4]=6 < 8 →