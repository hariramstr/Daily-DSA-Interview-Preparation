import java.util.*;

/*
 * Title: Minimum Energy to Paint Fence Posts
 * Difficulty: Easy
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are repainting a straight fence with n posts. Each post must be painted either red,
 * blue, or green. The cost of painting post i with a given color is provided in a 2D array
 * costs, where costs[i][0] is the cost for red, costs[i][1] is the cost for blue, and
 * costs[i][2] is the cost for green.
 *
 * For appearance reasons, no two adjacent fence posts are allowed to have the same color.
 * Your task is to return the minimum total energy cost needed to paint all posts while
 * following this rule.
 *
 * This is a classic dynamic programming decision problem: for each post and each color,
 * you only need to know the best total cost of painting the previous post with a different
 * color. An efficient solution should run in linear time with respect to the number of posts.
 *
 * Constraints:
 * - 1 <= n <= 1000
 * - costs.length == n
 * - costs[i].length == 3
 * - 1 <= costs[i][j] <= 10^4
 *
 * Example 1:
 * Input: costs = [[1,5,3],[2,9,4]]
 * Output: 5
 * Explanation: Paint the first post red for 1, and the second post green for 4.
 * Total cost = 5.
 *
 * Example 2:
 * Input: costs = [[7,6,2],[5,8,4],[3,9,1],[6,2,7]]
 * Output: 10
 * Explanation: One optimal painting is green, red, green, blue with cost
 * 2 + 5 + 1 + 2 = 10.
 */

public class Solution {

    /**
     * Computes the minimum total energy cost to paint all fence posts such that
     * no two adjacent posts have the same color.
     *
     * Dynamic Programming Idea:
     * For each post, we track the minimum total cost if that post is painted:
     * - red
     * - blue
     * - green
     *
     * To paint the current post red, the previous post must be blue or green.
     * To paint the current post blue, the previous post must be red or green.
     * To paint the current post green, the previous post must be red or blue.
     *
     * Instead of storing a full DP table, we only keep the previous row because
     * each state depends only on the immediately previous post.
     *
     * @param costs a 2D array where costs[i][0], costs[i][1], and costs[i][2]
     *              represent the energy cost of painting post i red, blue, and green
     * @return the minimum possible total energy cost to paint all posts while
     *         ensuring adjacent posts have different colors
     *
     * Time complexity: O(n), where n is the number of posts
     * Space complexity: O(1), excluding the input array
     */
    public int minCost(int[][] costs) {
        // Defensive handling:
        // Although the constraints guarantee at least one post, it is good beginner-friendly
        // practice to safely handle null or empty input.
        if (costs == null || costs.length == 0) {
            return 0;
        }

        // If there is only one post, the answer is simply the cheapest of the three colors.
        if (costs.length == 1) {
            return minOfThree(costs[0][0], costs[0][1], costs[0][2]);
        }

        // These variables represent the minimum total cost up to the previous post
        // if that previous post is painted a specific color.
        //
        // Initially, for post 0:
        // - prevRed   = cost to paint post 0 red
        // - prevBlue  = cost to paint post 0 blue
        // - prevGreen = cost to paint post 0 green
        int prevRed = costs[0][0];
        int prevBlue = costs[0][1];
        int prevGreen = costs[0][2];

        // Process each remaining post one by one.
        for (int i = 1; i < costs.length; i++) {
            // If current post i is painted red,
            // then post i-1 cannot be red.
            // So we take the cheaper of:
            // - previous post painted blue
            // - previous post painted green
            int currentRed = costs[i][0] + Math.min(prevBlue, prevGreen);

            // If current post i is painted blue,
            // then post i-1 cannot be blue.
            // So we take the cheaper of:
            // - previous post painted red
            // - previous post painted green
            int currentBlue = costs[i][1] + Math.min(prevRed, prevGreen);

            // If current post i is painted green,
            // then post i-1 cannot be green.
            // So we take the cheaper of:
            // - previous post painted red
            // - previous post painted blue
            int currentGreen = costs[i][2] + Math.min(prevRed, prevBlue);

            // Move the "current" results into the "previous" variables
            // so they can be used for the next post.
            prevRed = currentRed;
            prevBlue = currentBlue;
            prevGreen = currentGreen;
        }

        // After processing all posts, the last post could be red, blue, or green.
        // We return the cheapest total among those three possibilities.
        return minOfThree(prevRed, prevBlue, prevGreen);
    }

    /**
     * Returns the minimum value among three integers.
     *
     * @param a the first integer
     * @param b the second integer
     * @param c the third integer
     * @return the smallest of the three integers
     *
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int minOfThree(int a, int b, int c) {
        return Math.min(a, Math.min(b, c));
    }

    /**
     * Converts a 2D integer array into a readable string representation.
     * This is used only for demonstration in main.
     *
     * @param array the 2D integer array to convert
     * @return a string representation of the 2D array
     *
     * Time complexity: O(n), where n is the total number of elements
     * Space complexity: O(n), for the generated string
     */
    public String arrayToString(int[][] array) {
        return Arrays.deepToString(array);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It prints:
     * - the input cost matrix
     * - the computed minimum cost
     * - the expected result for easy verification
     *
     * Verified examples:
     * Example 1:
     * costs = [[1,5,3],[2,9,4]]
     * Best choice:
     * - Post 0 red = 1
     * - Post 1 green = 4
     * Total = 5
     *
     * Example 2:
     * costs = [[7,6,2],[5,8,4],[3,9,1],[6,2,7]]
     * One optimal choice:
     * - Post 0 green = 2
     * - Post 1 red = 5
     * - Post 2 green = 1
     * - Post 3 blue = 2
     * Total = 10
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) for each demonstrated test case
     * Space complexity: O(1) excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] costs1 = {
            {1, 5, 3},
            {2, 9, 4}
        };

        int[][] costs2 = {
            {7, 6, 2},
            {5, 8, 4},
            {3, 9, 1},
            {6, 2, 7}
        };

        int[][] costs3 = {
            {8, 3, 5}
        };

        System.out.println("Example 1 Input: " + solution.arrayToString(costs1));
        System.out.println("Example 1 Output: " + solution.minCost(costs1));
        System.out.println("Example 1 Expected: 5");
        System.out.println();

        System.out.println("Example 2 Input: " + solution.arrayToString(costs2));
        System.out.println("Example 2 Output: " + solution.minCost(costs2));
        System.out.println("Example 2 Expected: 10");
        System.out.println();

        System.out.println("Single Post Example Input: " + solution.arrayToString(costs3));
        System.out.println("Single Post Example Output: " + solution.minCost(costs3));
        System.out.println("Single Post Example Expected: 3");
    }
}