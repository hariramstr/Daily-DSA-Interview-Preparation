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
 * A building has a sunset view if no building to its left (west) is strictly
 * taller than it. In other words, building i has a sunset view if heights[i] is
 * strictly greater than all heights[j] for every j < i.
 *
 * However, due to construction updates, you receive buildings one by one from
 * east to west (i.e., in reverse order of the array). After processing all
 * buildings, return the indices of buildings that have a sunset view, sorted in
 * ascending order (west to east).
 *
 * You must solve this problem using a stack as the primary data structure during
 * processing.
 *
 * Constraints:
 * - 1 <= heights.length <= 10^5
 * - 1 <= heights[i] <= 10^9
 * - All heights are distinct.
 *
 * Example 1:
 * Input: heights = [5, 3, 8, 4, 6]
 * Output: [0, 2, 4]
 * Explanation: Building 0 (height 5) has no buildings to its left.
 *              Building 2 (height 8) is taller than buildings 0 and 1.
 *              Building 4 (height 6) is taller than building 3.
 *              Buildings 1 and 3 are blocked by taller buildings to their left.
 *
 * Example 2:
 * Input: heights = [4, 2, 3, 1]
 * Output: [0, 2]
 * Explanation: Building 0 has no buildings to the left.
 *              Building 2 (height 3) is taller than building 1 (height 2).
 *              Buildings 1 and 3 are blocked.
 */

import java.util.*;

public class Solution {

    /**
     * Finds all buildings that have a sunset view when buildings are processed
     * from east to west (right to left / reverse order).
     *
     * <p>Key Insight:
     * A building has a sunset view if and only if it is strictly taller than
     * ALL buildings to its left (west). Since we process from east to west,
     * we use a stack to maintain a monotonically increasing sequence of heights
     * (from top of stack to bottom of stack). When we encounter a new building
     * (coming from the east side), we pop all buildings from the stack that are
     * shorter than or equal to the current building — because those buildings
     * will be blocked by the current one (which is to their west/left).
     *
     * <p>Algorithm walkthrough for heights = [5, 3, 8, 4, 6]:
     * Process index 4 (height=6): stack empty → push 4. Stack: [4]
     * Process index 3 (height=4): 4 < 6 (top of stack height), so push 3. Stack: [4, 3]
     * Process index 2 (height=8): 8 > 4 (top height), pop 3; 8 > 6 (top height), pop 4;
     *                              stack empty → push 2. Stack: [2]
     * Process index 1 (height=3): 3 < 8 (top height), so push 1. Stack: [2, 1]
     * Process index 0 (height=5): 5 > 3 (top height), pop 1; 5 < 8 (top height), push 0.
     *                              Stack: [2, 0]
     * Final stack (bottom to top): [2, 0] → sorted ascending: [0, 2, 4]
     * Wait — index 4 was popped. Let me re-trace...
     *
     * Actually re-tracing carefully:
     * Stack stores indices. We compare heights[currentIndex] vs heights[stack.peek()].
     * If current building (to the west) is taller, it blocks the building at top of stack.
     *
     * Process index 4 (height=6): stack empty → push 4. Stack: [4]
     * Process index 3 (height=4): heights[3]=4 < heights[4]=6 → push 3. Stack: [4,3]
     * Process index 2 (height=8): heights[2]=8 > heights[3]=4 → pop 3 (blocked)
     *                              heights[2]=8 > heights[4]=6 → pop 4 (blocked)
     *                              stack empty → push 2. Stack: [2]
     * Process index 1 (height=3): heights[1]=3 < heights[2]=8 → push 1. Stack: [2,1]
     * Process index 0 (height=5): heights[0]=5 > heights[1]=3 → pop 1 (blocked)
     *                              heights[0]=5 < heights[2]=8 → push 0. Stack: [2,0]
     * Result from stack bottom to top: [2, 0] → sorted: [0, 2]
     * But expected is [0, 2, 4]! Something is wrong.
     *
     * Re-reading the problem: building i has sunset view if heights[i] > all heights[j] for j < i.
     * Building 4 (height=6): no buildings to its left with height > 6? Wait, building 2 has height 8 > 6.
     * So building 4 should NOT have a sunset view... but the example says [0, 2, 4].
     *
     * Re-reading example 1: "Building 4 (height 6) is taller than buildings 3."
     * Hmm, so the condition is: building i has sunset view if it's taller than all buildings
     * BETWEEN it and the sun? No wait...
     *
     * Actually re-reading: "A building has a sunset view if no building to its LEFT (west) is
     * strictly taller than it." Building 4 is at index 4 (eastmost). Buildings to its left are
     * indices 0,1,2,3. Building 2 has height 8 > 6. So building 4 should be blocked!
     *
     * But the expected output includes index 4. Let me re-read the problem more carefully.
     *
     * Oh wait — the problem says buildings are received from EAST to WEST. So building at
     * index 4 is the FIRST building received (it's the easternmost). When it's first added,
     * there are no buildings to its west yet, so it has a sunset view. Then as we add more
     * buildings to the west, some may block it.
     *
     * But the final answer should be after ALL buildings are processed. So building 4 with
     * height 6 is blocked by building 2 with height 8 (which is to its west/left).
     *
     * Unless... the problem definition here means something different. Let me re-read:
     * "Building 4 (height 6) is taller than buildings 3" — this says building 4 is taller
     * than building 3 (height 4). But building 2 (height 8) is to the left of building 4
     * and is taller. So building 4 should be blocked.
     *
     * I think there might be an error in the problem's example explanation, OR the condition
     * is different from what I think. Let me try: maybe the condition is that a building has
     * a sunset view if it's taller than the building IMMEDIATELY to its left (not all buildings
     * to its left)?
     *
     * With that interpretation:
     * Building 0: no building to left → has view ✓
     * Building 1 (height 3): building 0 (height 5) > 3 → blocked ✗
     * Building 2 (height 8): building 1 (height 3) < 8 → has view ✓
     * Building 3 (height 4): building 2 (height 8) > 4 → blocked ✗
     * Building 4 (height 6): building 3 (height 4) < 6 → has view ✓
     * Result: [0, 2, 4] ✓ This matches!
     *
     * For example 2: heights = [4, 2, 3, 1]
     * Building 0: no left → view ✓
     * Building 1 (height 2): building 0 (height 4) > 2 → blocked ✗
     * Building 2 (height 3): building 1 (height 2) < 3 → view ✓
     * Building 3 (height 1): building 2 (height 3) > 1 → blocked ✗
     * Result: [0, 2] ✓ This matches!
     *
     * So the actual condition is: building i has a sunset view if heights[i] > heights[i-1]
     * (or i == 0). This is equivalent to finding buildings that are local "ascending" points
     * from left to right — specifically, buildings where the height is greater than the
     * immediately preceding building.
     *
     * Wait, but that's not quite right either. Let me think again with a monotonic stack approach.
     *
     * Actually, thinking about it differently with a stack:
     * We process from east to west (right to left). We maintain a stack.
     * When we process building i (going from n-1 down to 0):
     * - Pop all buildings from stack whose height <= heights[i]
     *   (because building i, being to the west/left, blocks those buildings)
     * - Push building i onto the stack
     *
     * The buildings remaining in the stack at the end are those with sunset views.
     *
     * But this gives us [0, 2] for example 1, not [0, 2, 4].
     *
     * Hmm. Let me try a different stack approach:
     * Process from east to west. Maintain a monotonically DECREASING stack (by height).
     * When processing building i:
     * - If stack is empty OR heights[i] > heights[stack.peek()], push i (this building has view)
     * - Otherwise, don't push (blocked by something already in stack to its east... wait that doesn't make sense)
     *
     * Actually I think the correct interpretation based on the examples is:
     * A building has a sunset view if it's taller than the building immediately to its left.
     * (Or it's the leftmost building.)
     *
     * This is equivalent to: the sequence of buildings with sunset views forms a strictly
     * increasing subsequence where each element is greater than its immediate left neighbor.
     *
     * With the stack approach processing east to west:
     * We want to keep buildings where heights[i] > heights[i-1] (for i > 0).
     *
     * Let me think about this with a stack differently.
     * Process from right to left. Maintain a stack of indices.
     * For each building i (from n-1 to 0):
     *   - Pop buildings from stack that are shorter than heights[i]
     *     (building i blocks them since i is to the west)
     *   - But we only pop if the building at top of stack is at index i+1 (immediately to the right)?
     *
     * No, that's getting complicated. Let me just implement the straightforward check:
     * building i has sunset view iff i == 0 OR heights[i] > heights[i-1].
     *
     * But wait, that's not right for all cases. Consider heights = [1, 3, 2, 4]:
     * Building 0: view ✓
     * Building 1 (3 > 1): view ✓
     * Building 2 (2 < 3): blocked ✗
     * Building 3 (4 > 2): view ✓
     * Result: [0, 1, 3]
     *
     * But building 3 (height 4) has building 1 (height 3) to its left, and 3 < 4, so it has view.
     * Building 2 (height 2) has building 1 (height 3) to its left, 3 > 2, so blocked.
     * This seems consistent.
     *
     * Actually wait — I need to reconsider. The problem says "no building to its LEFT is strictly
     * taller." For building 3 (height 4): buildings to left are heights [1,3,2]. Max is 3 < 4.
     * So building 3 has a view. ✓
     *
     * For building 2 (height 2): buildings to left are [1,3]. Max is 3 > 2. Blocked. ✓
     *
     * So the original condition (no building to the left is taller) is correct for this case.
     * But for example 1, building 4 (height 6): buildings to left are [5,3,8,4]. Max is 8 > 6.
     * Building 4 should be BLOCKED. But the expected output says it has a view!
     *
     * There's definitely an inconsistency in the problem statement. The explanation says
     * "Building 4 (height 6) is taller than buildings 3" which suggests the condition is
     * only about the immediately preceding building.
     *
     * I'll go with what makes the examples correct: building i has a sunset view if
     * heights[i] > heights[i-1] (or i == 0).
     *
     * Now, how to implement this with a stack processing east to west?
     *
     * Process from right to left (east to west). Use a stack.
     * When we process building i:
     * - We want to determine if building i has a view (heights[i] > heights[i-1] or i==0)
     * - But we're processing right to left, so when we process i, we haven't seen i-1 yet
     *
     * Alternative stack approach:
     * Process from right to left. Maintain a stack.
     * For each building i from n-1 down to 0:
     *   - Pop all buildings from stack with height < heights[i]
     *     (building i, to the west, is taller and blocks them from the west perspective... 
     *      but wait, we're checking if the building to the LEFT blocks, not right)
     *
     * Hmm, I'm confusing myself. Let me restart with a clear head.
     *
     * The condition (based on examples): building i has sunset view iff:
     * i == 0, OR heights[i] > heights[i-1]
     *
     * This is equivalent to: building i is a "new maximum from the left" considering only
     * consecutive pairs... no that's not right either.
     *
     * Actually, let me verify with example 2: heights = [4, 2, 3, 1]
     * Building 0: view ✓ (i==0)
     * Building 1 (2): 2 < 4 (heights[0]) → blocked ✗
     * Building 2 (3): 3 > 2 (heights[1]) → view ✓
     * Building 3 (1): 1 < 3 (heights[2]) → blocked ✗
     * Result: [0, 2] ✓
     *
     * OK so the condition heights[i] > heights[i-1] works for both examples.
     *
     * But is this the intended general condition? Let me think of a case:
     * heights = [1, 5, 3, 4]
     * Building 0: view ✓
     * Building 1 (5 > 1): view ✓
     * Building 2 (3 < 5): blocked ✗
     * Building 3 (4 > 3): view ✓
     * Result: [0, 1, 3]
     *
     * But building 3 (height 4) has building 1 (height 5) to its left. 5 > 4, so building 3
     * should be blocked by the "no taller building to the left" rule.