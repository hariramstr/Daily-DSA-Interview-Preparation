import java.util.*;

/*
Problem Title: Maximum Score From Choosing a Buffered Triple

Problem Description:
You are given an integer array nums of length n and an integer gap. A buffered triple is a choice
of three indices (i, j, k) such that i < j < k, j - i > gap, and k - j > gap. The score of such
a triple is nums[i] - nums[j] + nums[k]. Your task is to return the maximum possible score among
all valid buffered triples. If no valid triple exists, return -1.

This problem models a situation where three events must be selected in time order, but each pair
of consecutive selected events must be separated by a mandatory cooldown of more than gap positions.
Because the array can be very large, an O(n^2) or O(n^3) solution will not pass. You need to exploit
array structure and precomputed information to evaluate valid middle positions efficiently.

Formally, for every valid middle index j, the left index i must come from the prefix
[0, j - gap - 1], and the right index k must come from the suffix [j + gap + 1, n - 1].
The score becomes:
    (best value on the left) - nums[j] + (best value on the right)
The challenge is to compute this over all possible j efficiently.

Constraints:
- 3 <= n <= 200000
- -1000000000 <= nums[i] <= 1000000000
- 0 <= gap < n

Examples:
1) nums = [5, 1, 9, 2, 7, 3, 8], gap = 1
   Valid middle positions must have room on both sides with the required spacing.
   The optimal valid triple is (2, 3, 6):
       9 - 2 + 8 = 15
   Output: 15

2) nums = [4, -3, 6, -10, 5, 2], gap = 1
   The best valid triple is (2, 3, 5):
       6 - (-10) + 2 = 18
   Output: 18
*/

public class Solution {

    /**
     * Computes the maximum score of any buffered triple (i, j, k) such that:
     * i < j < k, j - i > gap, and k - j > gap.
     *
     * Core idea:
     * For each possible middle index j:
     * - the best left contribution is the maximum value in nums[0 .. j - gap - 1]
     * - the best right contribution is the maximum value in nums[j + gap + 1 .. n - 1]
     *
     * Therefore, for each valid j:
     * score = leftMax[j - gap - 1] - nums[j] + rightMax[j + gap + 1]
     *
     * We precompute:
     * - prefix maxima: best value seen from the left up to each index
     * - suffix maxima: best value seen from the right from each index onward
     *
     * Then we scan all possible middle positions in O(n).
     *
     * @param nums the input integer array
     * @param gap the required strict spacing buffer between consecutive chosen indices
     * @return the maximum possible score, or -1 if no valid buffered triple exists
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long maximumScore(int[] nums, int gap) {
        int n = nums.length;

        // A valid triple needs:
        // j - i > gap  => at least gap + 1 positions between i and j in index difference
        // k - j > gap  => at least gap + 1 positions between j and k in index difference
        //
        // The smallest possible triple shape is:
        // i, then j at i + gap + 1, then k at j + gap + 1
        // So the minimum total length needed is 2 * gap + 3 elements.
        //
        // If the array is shorter than that, no valid triple can exist.
        if (n < 2L * gap + 3) {
            return -1;
        }

        // prefixMax[x] = maximum value among nums[0..x]
        long[] prefixMax = new long[n];
        prefixMax[0] = nums[0];
        for (int i = 1; i < n; i++) {
            prefixMax[i] = Math.max(prefixMax[i - 1], nums[i]);
        }

        // suffixMax[x] = maximum value among nums[x..n-1]
        long[] suffixMax = new long[n];
        suffixMax[n - 1] = nums[n - 1];
        for (int i = n - 2; i >= 0; i--) {
            suffixMax[i] = Math.max(suffixMax[i + 1], nums[i]);
        }

        long answer = Long.MIN_VALUE;
        boolean foundValidTriple = false;

        // We now try every index j as the middle element.
        //
        // For j to be valid:
        // - there must exist at least one i in [0, j - gap - 1]
        //   => j - gap - 1 >= 0
        //   => j >= gap + 1
        //
        // - there must exist at least one k in [j + gap + 1, n - 1]
        //   => j + gap + 1 <= n - 1
        //   => j <= n - gap - 2
        //
        // So j ranges from gap + 1 to n - gap - 2 inclusive.
        for (int j = gap + 1; j <= n - gap - 2; j++) {
            // The farthest allowed left boundary for i is j - gap - 1.
            // Any i up to that point is valid, so the best left value is the prefix maximum there.
            int leftBoundary = j - gap - 1;
            long bestLeft = prefixMax[leftBoundary];

            // The earliest allowed right boundary for k is j + gap + 1.
            // Any k from that point onward is valid, so the best right value is the suffix maximum there.
            int rightBoundary = j + gap + 1;
            long bestRight = suffixMax[rightBoundary];

            // Compute the best score using this middle index j.
            long currentScore = bestLeft - nums[j] + bestRight;

            // Update global answer.
            if (!foundValidTriple || currentScore > answer) {
                answer = currentScore;
                foundValidTriple = true;
            }
        }

        return foundValidTriple ? answer : -1;
    }

    /**
     * A helper method that prints a detailed demonstration for one test case.
     *
     * @param nums the input array
     * @param gap the spacing buffer
     * @return the computed maximum score for the given input
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long demonstrate(int[] nums, int gap) {
        long result = maximumScore(nums, gap);
        System.out.println("nums = " + Arrays.toString(nums));
        System.out.println("gap = " + gap);
        System.out.println("maximum score = " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method demonstrating the solution on sample and additional test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size across demonstrations)
     * Space complexity: O(n) per demonstration call
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1 from the statement.
        // nums = [5, 1, 9, 2, 7, 3, 8], gap = 1
        // Valid best triple is (2, 3, 6): 9 - 2 + 8 = 15
        solution.demonstrate(new int[]{5, 1, 9, 2, 7, 3, 8}, 1);

        // Sample 2 corrected according to the strict spacing rule.
        // nums = [4, -3, 6, -10, 5, 2], gap = 1
        // Best valid triple is (2, 3, 5): 6 - (-10) + 2 = 18
        solution.demonstrate(new int[]{4, -3, 6, -10, 5, 2}, 1);

        // Additional edge case:
        // No valid triple because array is too short for the required gap.
        solution.demonstrate(new int[]{1, 2, 3}, 1);

        // Additional test with gap = 0:
        // Need only i < j < k.
        solution.demonstrate(new int[]{4, -3, 6, -10, 5, 2}, 0);
    }
}