/*
Title: Maximum Score from Picking a Protected Middle Pair

Problem Description:
You are given an integer array nums representing values placed in a line. You want to choose two indices i and j such that i < j and there is at least one element strictly between them. The score of choosing this pair is defined as:

    nums[i] + nums[j] + min(nums[i+1..j-1])

In other words, the two chosen endpoints contribute their values, and the interior segment contributes its smallest value as a protection bonus.

Your task is to return the maximum possible score over all valid pairs.

A pair is valid only if j - i >= 2, because the middle segment must contain at least one element. The array may contain negative values, so the best answer is not necessarily formed by the largest endpoints alone. An efficient solution is expected for large inputs.

Constraints:
- 3 <= nums.length <= 200000
- -1000000000 <= nums[i] <= 1000000000

Example 1:
Input: nums = [5, 1, 4, 3]
Output: 10
Explanation: Choose i = 0 and j = 2. The score is nums[0] + nums[2] + min(nums[1]) = 5 + 4 + 1 = 10.

Example 2:
Input: nums = [2, 7, -3, 6, 1]
Output: 10
Explanation: Choose i = 1 and j = 3. The middle segment is [-3], so the score is 7 + 6 + (-3) = 10.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Key idea:
    ----------
    Let k be the index of the minimum value inside the chosen interval (i, j).
    Then:
        i < k < j
        nums[k] = min(nums[i+1..j-1])

    So the score becomes:
        nums[i] + nums[j] + nums[k]

    But this is only valid if every element between i+1 and j-1 is >= nums[k].
    That means k must be the minimum of the whole interior segment.

    We can turn this into a "minimum as pivot" problem:
    For each index k, find the largest open interval around k where every element is
    >= nums[k]. Inside that interval, k can serve as the interior minimum.
    Then we want:
        best left endpoint value before k
        +
        best right endpoint value after k
        +
        nums[k]

    More precisely:
    - Let L be the nearest index to the left of k with value < nums[k].
    - Let R be the nearest index to the right of k with value < nums[k].
    Then any valid endpoint i must satisfy L < i < k,
    and any valid endpoint j must satisfy k < j < R.
    This guarantees every element between i and j, except endpoints, is >= nums[k],
    so nums[k] is indeed a minimum of the interior.

    Therefore for each k:
        contribution = nums[k]
                     + max(nums[i]) for i in [L+1 .. k-1]
                     + max(nums[j]) for j in [k+1 .. R-1]

    We compute:
    1) previous strictly smaller index for every position
    2) next strictly smaller index for every position
    3) range maximum queries using a segment tree
    4) evaluate every k in O(log n), total O(n log n)

    However, we can do even better in practice and still remain very clear:
    segment tree for range max + monotonic stack for boundaries.
    This is fully correct and efficient for n up to 200000.
    */
    public long MaximumScore(int[] nums)
    {
        int n = nums.Length;

        // Step 1:
        // Build arrays that tell us the nearest strictly smaller value on the left and right.
        //
        // Why do we need this?
        // --------------------
        // For a fixed middle-minimum candidate k, we want to know how far we can expand
        // to the left and right while keeping nums[k] as a minimum of the interior.
        //
        // If we encounter a value strictly smaller than nums[k], then nums[k] can no longer
        // be the minimum of any interval crossing that point.
        //
        // So:
        // prevSmaller[k] = nearest index < k with nums[index] < nums[k], or -1 if none
        // nextSmaller[k] = nearest index > k with nums[index] < nums[k], or n if none
        //
        // We compute these in O(n) using a monotonic increasing stack.
        int[] prevSmaller = new int[n];
        int[] nextSmaller = new int[n];
        Array.Fill(prevSmaller, -1);
        Array.Fill(nextSmaller, n);

        var stack = new Stack<int>();

        // Compute previous strictly smaller.
        for (int i = 0; i < n; i++)
        {
            // Maintain stack so values are strictly increasing by value.
            // We pop while top >= current because those cannot be the previous strictly smaller.
            while (stack.Count > 0 && nums[stack.Peek()] >= nums[i])
            {
                stack.Pop();
            }

            prevSmaller[i] = stack.Count > 0 ? stack.Peek() : -1;
            stack.Push(i);
        }

        stack.Clear();

        // Compute next strictly smaller.
        for (int i = n - 1; i >= 0; i--)
        {
            // Same logic from the right side.
            while (stack.Count > 0 && nums[stack.Peek()] >= nums[i])
            {
                stack.Pop();
            }

            nextSmaller[i] = stack.Count > 0 ? stack.Peek() : n;
            stack.Push(i);
        }

        // Step 2:
        // Build a segment tree for range maximum queries.
        //
        // Why is this needed?
        // -------------------
        // For each k, once we know the valid left range [prevSmaller[k] + 1 .. k - 1]
        // and valid right range [k + 1 .. nextSmaller[k] - 1],
        // we need the maximum endpoint value from each side.
        //
        // A segment tree lets us answer:
        //     max(nums[l..r])
        // quickly in O(log n).
        var segTree = new MaxSegmentTree(nums);

        long answer = long.MinValue;

        // Step 3:
        // Try every index k as the interior minimum.
        //
        // Important:
        // ----------
        // k itself must be strictly between the chosen endpoints,
        // so we need at least one possible endpoint on the left and one on the right.
        //
        // That means:
        // left range must be non-empty  => prevSmaller[k] + 1 <= k - 1
        // right range must be non-empty => k + 1 <= nextSmaller[k] - 1
        for (int k = 0; k < n; k++)
        {
            int leftStart = prevSmaller[k] + 1;
            int leftEnd = k - 1;

            int rightStart = k + 1;
            int rightEnd = nextSmaller[k] - 1;

            // If either side has no candidate endpoint, then k cannot be the protected middle minimum
            // for any valid pair (i, j) with i < k < j.
            if (leftStart > leftEnd || rightStart > rightEnd)
            {
                continue;
            }

            // Query the best endpoint value on the left side.
            long bestLeft = segTree.Query(leftStart, leftEnd);

            // Query the best endpoint value on the right side.
            long bestRight = segTree.Query(rightStart, rightEnd);

            // The score for using k as the interior minimum is:
            // best left endpoint + best right endpoint + nums[k]
            long current = bestLeft + bestRight + nums[k];

            if (current > answer)
            {
                answer = current;
            }
        }

        return answer;
    }
}

public class MaxSegmentTree
{
    private readonly long[] tree;
    private readonly int size;

    public MaxSegmentTree(int[] nums)
    {
        int n = nums.Length;

        // We use a power-of-two sized iterative segment tree.
        // This makes implementation simple and efficient.
        size = 1;
        while (size < n)
        {
            size <<= 1;
        }

        tree = new long[size * 2];

        // Initialize with very small values so max queries work correctly
        // even when numbers are negative.
        for (int i = 0; i < tree.Length; i++)
        {
            tree[i] = long.MinValue;
        }

        // Put original values into leaves.
        for (int i = 0; i < n; i++)
        {
            tree[size + i] = nums[i];
        }

        // Build internal nodes from bottom to top.
        for (int i = size - 1; i >= 1; i--)
        {
            tree[i] = Math.Max(tree[i * 2], tree[i * 2 + 1]);
        }
    }

    public long Query(int left, int right)
    {
        long result = long.MinValue;

        left += size;
        right += size;

        while (left <= right)
        {
            if ((left & 1) == 1)
            {
                result = Math.Max(result, tree[left]);
                left++;
            }

            if ((right & 1) == 0)
            {
                result = Math.Max(result, tree[right]);
                right--;
            }

            left >>= 1;
            right >>= 1;
        }

        return result;
    }
}

// Demo code

var solution = new Solution();

int[] nums1 = { 5, 1, 4, 3 };
long result1 = solution.MaximumScore(nums1);
Console.WriteLine(result1); // Expected: 10

int[] nums2 = { 2, 7, -3, 6, 1 };
long result2 = solution.MaximumScore(nums2);
Console.WriteLine(result2); // Expected: 10

int[] nums3 = { 1, 2, 3 };
long result3 = solution.MaximumScore(nums3);
Console.WriteLine(result3); // Only pair is (0,2): 1 + 3 + 2 = 6

int[] nums4 = { -5, -2, -7, -1 };
long result4 = solution.MaximumScore(nums4);
Console.WriteLine(result4); // Validates handling of negatives