import java.util.*;

/*
 * Title: Count Visible Skyline Pairs in a Queue
 * Difficulty: Hard
 * Topic: Two Pointers
 *
 * Problem Description:
 * You are given an array heights of length n representing the heights of buildings
 * standing in a straight line from left to right. For every pair of indices (i, j)
 * with i < j, building i and building j are said to be mutually visible if every
 * building strictly between them has height less than or equal to
 * min(heights[i], heights[j]).
 *
 * In other words, the shorter of the two endpoint buildings determines the maximum
 * allowed height of all buildings between them.
 *
 * Your task is to return the total number of mutually visible pairs.
 *
 * A brute-force solution that checks every pair and scans the buildings in between
 * is far too slow. The intended solution should exploit the structure of the array
 * using a hard two-pointers-style strategy or an equivalent linear-time boundary
 * compression idea.
 *
 * Note that adjacent buildings are always visible, because there are no buildings
 * between them. Equal heights are allowed, and they can still see each other as
 * long as all intermediate heights satisfy the visibility rule.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - 1 <= heights[i] <= 1000000000
 * - Return the answer as a 64-bit integer, since the number of visible pairs may
 *   exceed 32-bit range.
 *
 * Example 1:
 * Input: heights = [5, 3, 4, 2, 6]
 * Output: 7
 * Explanation:
 * Visible pairs are (0,1), (0,2), (0,4), (1,2), (2,3), (2,4), and (3,4).
 * Pair (1,4) is not visible because building 2 has height 4, which is greater than
 * min(3,6)=3.
 *
 * Example 2:
 * Input: heights = [4, 4, 4]
 * Output: 3
 * Explanation:
 * All pairs are visible.
 * - (0,1) and (1,2) are adjacent.
 * - (0,2) is also visible because the only building between them has height 4,
 *   which is <= min(4,4)=4.
 *
 * Core Idea:
 * We solve this in linear time using a monotonic stack with run-length compression
 * for equal heights.
 *
 * Why this works:
 * When we scan from left to right, the stack stores groups of buildings that are
 * still "relevant" for future visibility checks. The stack is maintained in
 * non-increasing height order.
 *
 * For a new building h:
 * 1) Every shorter group on top of the stack is fully visible to h, because h is
 *    the first building to the right that is at least as tall as those shorter
 *    buildings. So we pop those groups and add their counts.
 * 2) If the next group on the stack has the same height as h, then h can see all
 *    buildings in that equal-height group. We add that group size.
 *    Also, if there is a taller group behind that equal-height group, h can see
 *    exactly one building from that taller side as well, so we add one more.
 *    Then we merge h into the equal-height group.
 * 3) Otherwise, if the stack still contains a taller group, h can see exactly the
 *    nearest taller building, so we add one.
 *
 * This is the classic linear-time visibility counting strategy, adapted to the
 * exact visibility rule in this problem. It correctly handles equal heights.
 */
public class Solution {

    /**
     * Small helper structure representing a compressed group of consecutive
     * "active" buildings of the same height in the monotonic stack.
     *
     * Instead of storing each equal-height building separately, we store:
     * - the height value
     * - how many buildings of that height are grouped together
     *
     * This compression is essential for both efficiency and correctness when many
     * equal heights appear.
     */
    private static class Group {
        int height;
        int count;

        Group(int height, int count) {
            this.height = height;
            this.count = count;
        }
    }

    /**
     * Counts the total number of mutually visible pairs of buildings.
     *
     * Detailed intuition:
     * We process buildings from left to right and maintain a monotonic stack of
     * groups with non-increasing heights.
     *
     * For each new building of height h:
     * - Pop all strictly shorter groups:
     *   Every building in those groups can see the current building, so add their
     *   counts to the answer.
     * - If the top group has the same height:
     *   The current building can see all buildings in that equal-height group.
     *   Add that group's count.
     *   If there is still another group behind it, then that next group must be
     *   taller, and the current building can also see exactly one building there.
     *   Add 1 in that case.
     *   Then merge the current building into the equal-height group.
     * - Otherwise, if the stack is not empty:
     *   The top group is taller, and the current building can see exactly one
     *   nearest taller building. Add 1.
     *   Then push the current building as a new group.
     *
     * This counts each visible pair exactly once: when the right endpoint is
     * processed.
     *
     * @param heights the array of building heights from left to right
     * @return the total number of mutually visible pairs as a 64-bit integer
     * Time complexity: O(n), because each building/group is pushed and popped at most once.
     * Space complexity: O(n) in the worst case for the stack.
     */
    public long countVisiblePairs(int[] heights) {
        if (heights == null || heights.length <= 1) {
            return 0L;
        }

        long visiblePairs = 0L;

        // We use ArrayDeque as an efficient stack implementation.
        Deque<Group> stack = new ArrayDeque<>();

        // Process each building from left to right.
        for (int h : heights) {

            // Step 1:
            // Remove all groups that are strictly shorter than the current building.
            //
            // Why can we safely count all of them immediately?
            // Because the current building is the first building to the right that is
            // at least as tall as those popped groups. Therefore, each building in
            // those groups can see the current building.
            //
            // Also, once a shorter group is popped, it will never matter again for any
            // future building, because the current building blocks it.
            while (!stack.isEmpty() && stack.peek().height < h) {
                visiblePairs += stack.pop().count;
            }

            // Step 2:
            // If the top group has the same height as the current building, then:
            // - the current building can see every building in that equal-height group
            // - we add the size of that group
            // - then we merge the current building into that group
            if (!stack.isEmpty() && stack.peek().height == h) {
                Group sameHeightGroup = stack.pop();

                // The current building sees all previous active buildings of the same height.
                visiblePairs += sameHeightGroup.count;

                // If there is still a group behind the equal-height group, then that group
                // must be taller (because all shorter ones were already popped).
                //
                // The current building can see exactly one building from that taller side:
                // the nearest one.
                if (!stack.isEmpty()) {
                    visiblePairs += 1;
                }

                // Merge current building into the equal-height group.
                sameHeightGroup.count++;
                stack.push(sameHeightGroup);
            } else {
                // Step 3:
                // If the stack is not empty here, then the top group is strictly taller
                // than the current building.
                //
                // In that case, the current building can see exactly one building:
                // the nearest taller one on the left.
                if (!stack.isEmpty()) {
                    visiblePairs += 1;
                }

                // Push the current building as a new group of size 1.
                stack.push(new Group(h, 1));
            }
        }

        return visiblePairs;
    }

    /**
     * Convenience wrapper that accepts a List of heights instead of an array.
     *
     * @param heightsList list of building heights
     * @return the total number of mutually visible pairs as a 64-bit integer
     * Time complexity: O(n), where n is the number of buildings.
     * Space complexity: O(n) in the worst case for the stack and array conversion.
     */
    public long countVisiblePairs(List<Integer> heightsList) {
        int[] heights = new int[heightsList.size()];
        for (int i = 0; i < heightsList.size(); i++) {
            heights[i] = heightsList.get(i);
        }
        return countVisiblePairs(heights);
    }

    /**
     * Utility method to print an array in a beginner-friendly format.
     *
     * @param arr the input array
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n) for the produced string
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample test cases from the problem
     * statement and a few extra sanity checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size of demonstrated examples)
     * Space complexity: O(max input size among demonstrated examples)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] heights1 = {5, 3, 4, 2, 6};
        long result1 = solution.countVisiblePairs(heights1);
        System.out.println("Input:  " + solution.arrayToString(heights1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 7");
        System.out.println();

        int[] heights2 = {4, 4, 4};
        long result2 = solution.countVisiblePairs(heights2);
        System.out.println("Input:  " + solution.arrayToString(heights2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 3");
        System.out.println();

        int[] heights3 = {1};
        long result3 = solution.countVisiblePairs(heights3);
        System.out.println("Input:  " + solution.arrayToString(heights3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 0");
        System.out.println();

        int[] heights4 = {1, 2, 3, 4};
        long result4 = solution.countVisiblePairs(heights4);
        System.out.println("Input:  " + solution.arrayToString(heights4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 3");
        System.out.println();

        int[] heights5 = {4, 3, 2, 1};
        long result5 = solution.countVisiblePairs(heights5);
        System.out.println("Input:  " + solution.arrayToString(heights5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 3");
        System.out.println();

        int[] heights6 = {2, 2, 1, 2};
        long result6 = solution.countVisiblePairs(heights6);
        System.out.println("Input:  " + solution.arrayToString(heights6));
        System.out.println("Output: " + result6);
        System.out.println("Expected: 5");
    }
}