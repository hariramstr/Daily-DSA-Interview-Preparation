import java.util.*;

/*
 * Title: Maximum Uniform Poster Height for Campus Boards
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A university is preparing posters for an event and wants every poster placed on campus boards
 * to have the same height. You are given an array boards, where boards[i] is the height of the
 * i-th available board material strip. A strip can be cut into smaller poster pieces, but pieces
 * cannot be joined together. Every poster must have exactly the same integer height h, and each
 * cut piece used as a poster must come from a single strip. You are also given an integer k,
 * the minimum number of posters the university needs.
 *
 * Return the maximum possible integer poster height h such that it is possible to cut at least k
 * posters from the given strips. If it is impossible to make even k posters of height 1, return 0.
 *
 * For a chosen height h, a strip of height x can contribute floor(x / h) posters. Your task is
 * to find the largest valid h efficiently.
 *
 * This problem is designed for a binary search on the answer: if a height h is feasible, then any
 * smaller positive height is also feasible. Use that monotonic property to search the answer space.
 *
 * Constraints:
 * - 1 <= boards.length <= 200000
 * - 1 <= boards[i] <= 1000000000
 * - 1 <= k <= 1000000000
 * - The answer must be an integer.
 *
 * Example 1:
 * Input: boards = [8, 5, 8], k = 5
 * Output: 4
 * Explanation:
 * With height 3, the strips produce 2 + 1 + 2 = 5 posters.
 * With height 4, the strips produce 2 + 1 + 2 = 5 posters, so height 4 also works.
 * With height 5, the strips produce 1 + 1 + 1 = 3 posters, which is not enough.
 * Therefore, the maximum valid height is 4.
 *
 * Example 2:
 * Input: boards = [2, 3], k = 10
 * Output: 0
 * Explanation:
 * Even with height 1, the total number of posters is 2 + 3 = 5, which is less than 10,
 * so the requirement cannot be met.
 */

public class Solution {

    /**
     * Finds the maximum integer poster height such that at least k posters can be cut
     * from the given board strips.
     *
     * The key idea is binary search on the answer:
     * - If a height h is feasible, then every smaller height is also feasible.
     * - If a height h is not feasible, then every larger height is also not feasible.
     *
     * This monotonic behavior allows us to search efficiently instead of trying every height.
     *
     * @param boards the array of strip heights; each strip can be cut into multiple posters
     * @param k the minimum number of posters required
     * @return the maximum feasible integer poster height; returns 0 if even height 1 cannot produce k posters
     *
     * Time complexity: O(n log M), where n is boards.length and M is the maximum strip height.
     * Space complexity: O(1), ignoring input storage.
     */
    public int maximumUniformPosterHeight(int[] boards, int k) {
        // Defensive handling is not strictly necessary under the given constraints,
        // but it makes the method more beginner-friendly and robust.
        if (boards == null || boards.length == 0 || k <= 0) {
            return 0;
        }

        // Step 1:
        // Compute two useful values:
        // - totalHeight: total number of posters possible if height = 1
        // - maxBoard: the largest strip height, which becomes the upper bound of our search
        //
        // Why totalHeight?
        // If even height 1 cannot produce at least k posters, then no larger height can work.
        //
        // Why maxBoard?
        // A poster height larger than the biggest strip cannot be cut from any strip,
        // so there is no reason to search above maxBoard.
        long totalHeight = 0L;
        int maxBoard = 0;

        for (int board : boards) {
            totalHeight += board;
            if (board > maxBoard) {
                maxBoard = board;
            }
        }

        // Early exit:
        // If even height 1 gives fewer than k posters, answer must be 0.
        if (totalHeight < k) {
            return 0;
        }

        // Step 2:
        // Binary search over possible poster heights.
        //
        // Search space:
        // low = 1 because poster height must be positive
        // high = maxBoard because no height above that can work
        int low = 1;
        int high = maxBoard;

        // This variable stores the best valid height found so far.
        int answer = 0;

        // Standard "find maximum feasible value" binary search.
        while (low <= high) {
            // Use this form to avoid overflow:
            // mid = low + (high - low) / 2
            int mid = low + (high - low) / 2;

            // Check whether we can make at least k posters of height = mid.
            if (canMakeAtLeastKPosters(boards, mid, k)) {
                // If mid works, it is a valid candidate answer.
                answer = mid;

                // Since we want the MAXIMUM valid height,
                // we try searching to the right for a larger feasible height.
                low = mid + 1;
            } else {
                // If mid does not work, then any larger height also cannot work.
                // So we search to the left.
                high = mid - 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether it is possible to cut at least k posters of a given uniform height.
     *
     * For each strip of height x, the number of posters contributed is floor(x / height).
     * We sum these contributions and stop early once we reach or exceed k.
     *
     * @param boards the array of strip heights
     * @param height the candidate poster height being tested
     * @param k the required minimum number of posters
     * @return true if at least k posters can be produced with the given height; otherwise false
     *
     * Time complexity: O(n), where n is boards.length.
     * Space complexity: O(1).
     */
    public boolean canMakeAtLeastKPosters(int[] boards, int height, int k) {
        // We use long because the total number of posters can be very large.
        // Example:
        // 200000 strips * up to 1000000000 each / height 1
        // This can exceed the range of int.
        long count = 0L;

        // Step through every strip and count how many posters of this height it can produce.
        for (int board : boards) {
            count += board / height;

            // Important optimization:
            // As soon as we know count >= k, we can return true immediately.
            // There is no need to continue scanning the rest of the array.
            if (count >= k) {
                return true;
            }
        }

        // If we finish the loop and still have fewer than k posters, height is not feasible.
        return false;
    }

    /**
     * Runs sample demonstrations for the problem.
     *
     * This method prints the results for the examples described in the problem statement
     * and includes a few extra test cases for clarity.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(total input size across demonstrated test cases * log M).
     * Space complexity: O(1), ignoring input arrays created for demonstration.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement
        int[] boards1 = {8, 5, 8};
        int k1 = 5;
        int result1 = solution.maximumUniformPosterHeight(boards1, k1);
        System.out.println("Example 1:");
        System.out.println("boards = " + Arrays.toString(boards1) + ", k = " + k1);
        System.out.println("Maximum uniform poster height = " + result1);
        System.out.println("Expected = 4");
        System.out.println();

        // Example 2 from the problem statement
        int[] boards2 = {2, 3};
        int k2 = 10;
        int result2 = solution.maximumUniformPosterHeight(boards2, k2);
        System.out.println("Example 2:");
        System.out.println("boards = " + Arrays.toString(boards2) + ", k = " + k2);
        System.out.println("Maximum uniform poster height = " + result2);
        System.out.println("Expected = 0");
        System.out.println();

        // Additional example: exact fit with height 2
        int[] boards3 = {4, 7, 9};
        int k3 = 10;
        int result3 = solution.maximumUniformPosterHeight(boards3, k3);
        System.out.println("Additional Example 3:");
        System.out.println("boards = " + Arrays.toString(boards3) + ", k = " + k3);
        System.out.println("Maximum uniform poster height = " + result3);
        System.out.println();

        // Additional example: single strip
        int[] boards4 = {100};
        int k4 = 9;
        int result4 = solution.maximumUniformPosterHeight(boards4, k4);
        System.out.println("Additional Example 4:");
        System.out.println("boards = " + Arrays.toString(boards4) + ", k = " + k4);
        System.out.println("Maximum uniform poster height = " + result4);
        System.out.println();

        // Additional example: many small strips
        int[] boards5 = {1, 1, 1, 1, 1};
        int k5 = 5;
        int result5 = solution.maximumUniformPosterHeight(boards5, k5);
        System.out.println("Additional Example 5:");
        System.out.println("boards = " + Arrays.toString(boards5) + ", k = " + k5);
        System.out.println("Maximum uniform poster height = " + result5);
    }
}