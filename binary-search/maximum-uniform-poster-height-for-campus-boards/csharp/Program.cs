/*
Title: Maximum Uniform Poster Height for Campus Boards

Problem Description:
A university is preparing posters for an event and wants every poster placed on campus boards to have the same height.
You are given an array boards, where boards[i] is the height of the i-th available board material strip.
A strip can be cut into smaller poster pieces, but pieces cannot be joined together.

Every poster must have exactly the same integer height h, and each cut piece used as a poster must come from a single strip.
You are also given an integer k, the minimum number of posters the university needs.

Return the maximum possible integer poster height h such that it is possible to cut at least k posters from the given strips.
If it is impossible to make even k posters of height 1, return 0.

For a chosen height h, a strip of height x can contribute floor(x / h) posters.
Your task is to find the largest valid h efficiently.

This problem is ideal for binary search on the answer:
- If a height h is feasible, then every smaller positive height is also feasible.
- If a height h is not feasible, then every larger height is also not feasible.

So the set of valid answers is monotonic, which allows binary search.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * log M)
      where:
      n = number of strips in boards
      M = maximum strip height in boards
    Explanation:
    - Each binary search step checks whether a candidate height is feasible by scanning the array once.
    - The binary search range is from 1 to max(boards), so it takes log M steps.

    Space Complexity:
    - O(1) extra space
    Explanation:
    - We only use a few variables besides the input array.
    */

    public int MaxUniformPosterHeight(int[] boards, int k)
    {
        // Step 1:
        // We first find the maximum strip height.
        //
        // Why do we need this?
        // Because the answer cannot be larger than the tallest strip.
        // If the tallest strip has height 8, then a poster height of 9 is impossible.
        //
        // This gives us the upper bound for binary search.
        int maxHeight = 0;
        long totalAtHeightOne = 0;

        // We also compute the total number of posters possible when height = 1.
        //
        // Why is that useful?
        // Because height 1 is the smallest possible positive integer height.
        // If even height 1 cannot produce at least k posters, then no answer exists,
        // and we must return 0 immediately.
        foreach (int board in boards)
        {
            if (board > maxHeight)
            {
                maxHeight = board;
            }

            totalAtHeightOne += board;
        }

        // Step 2:
        // Early impossibility check.
        //
        // If the total number of unit-height posters is still less than k,
        // then it is impossible to satisfy the requirement.
        if (totalAtHeightOne < k)
        {
            return 0;
        }

        // Step 3:
        // Set up binary search over the answer space.
        //
        // left  = smallest candidate height that could still be valid
        // right = largest candidate height that could still be valid
        //
        // We search for the maximum feasible height.
        int left = 1;
        int right = maxHeight;

        // This variable stores the best valid answer found so far.
        // We update it whenever we find a feasible candidate height.
        int best = 0;

        // Step 4:
        // Standard binary search loop.
        //
        // We continue while the search range is not empty.
        while (left <= right)
        {
            // Compute the middle candidate height safely.
            //
            // Using left + (right - left) / 2 avoids overflow,
            // although int is still safe here for the given constraints.
            int mid = left + (right - left) / 2;

            // Step 5:
            // Check whether this candidate height "mid" is feasible.
            //
            // A height is feasible if the total number of posters we can cut
            // from all strips is at least k.
            if (CanMakeAtLeastKPosters(boards, k, mid))
            {
                // If mid is feasible, then:
                // - mid itself is a valid answer
                // - any smaller height is also feasible due to monotonicity
                //
                // But we want the MAXIMUM feasible height,
                // so we record mid and try searching to the right.
                best = mid;
                left = mid + 1;
            }
            else
            {
                // If mid is not feasible, then:
                // - mid is too large
                // - any larger height will also be infeasible
                //
                // So we must search to the left.
                right = mid - 1;
            }
        }

        // Step 6:
        // After binary search finishes, "best" holds the largest feasible height found.
        return best;
    }

    private bool CanMakeAtLeastKPosters(int[] boards, int k, int height)
    {
        // This helper method answers:
        // "If every poster must have exact height = height,
        //  can we make at least k posters?"
        //
        // We compute:
        // total = sum of floor(boards[i] / height)
        //
        // If total >= k, the height is feasible.

        long count = 0;

        foreach (int board in boards)
        {
            // Each strip contributes as many full posters as fit into it.
            //
            // Example:
            // board = 8, height = 3
            // floor(8 / 3) = 2 posters
            count += board / height;

            // Important optimization:
            // As soon as we already know count >= k,
            // we can stop early and return true.
            //
            // Why is this helpful?
            // It avoids unnecessary work on large inputs.
            if (count >= k)
            {
                return true;
            }
        }

        // If we finish scanning all strips and still have fewer than k posters,
        // then this height is not feasible.
        return false;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// boards = [8, 5, 8], k = 5
//
// Check manually:
// height 4 => 8/4 + 5/4 + 8/4 = 2 + 1 + 2 = 5, feasible
// height 5 => 1 + 1 + 1 = 3, not feasible
// So the maximum valid height is 4.
int[] boards1 = { 8, 5, 8 };
int k1 = 5;
int result1 = solution.MaxUniformPosterHeight(boards1, k1);
Console.WriteLine(result1); // Expected: 4

// Example 2:
// boards = [2, 3], k = 10
//
// Even at height 1, total posters = 2 + 3 = 5 < 10
// So the answer is 0.
int[] boards2 = { 2, 3 };
int k2 = 10;
int result2 = solution.MaxUniformPosterHeight(boards2, k2);
Console.WriteLine(result2); // Expected: 0

// Additional demo:
// boards = [7, 9, 5], k = 4
//
// Try height 4:
// 7/4 + 9/4 + 5/4 = 1 + 2 + 1 = 4, feasible
// Try height 5:
// 7/5 + 9/5 + 5/5 = 1 + 1 + 1 = 3, not feasible
// So answer should be 4.
int[] boards3 = { 7, 9, 5 };
int k3 = 4;
int result3 = solution.MaxUniformPosterHeight(boards3, k3);
Console.WriteLine(result3); // Expected: 4