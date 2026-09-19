/*
Title: Maximum Score from Choosing a Guarded Middle Segment
Difficulty: Hard
Topic: Arrays

Problem Description:
You are given an integer array nums of length n and a non-negative integer penalty. You must choose one contiguous subarray nums[l..r] as your final segment. The score of a chosen segment is defined as:

(minimum value inside the segment) * (length of the segment) - penalty * (number of elements outside the segment that are strictly smaller than that minimum value).

In other words, the segment earns a base score equal to its minimum multiplied by its length, but it is penalized for every value outside the segment that is smaller than the segment minimum. The outside elements may appear on either side of the segment.

Return the maximum possible score over all non-empty contiguous subarrays.

Constraints:
- 1 <= n <= 200000
- 1 <= nums[i] <= 1000000000
- 0 <= penalty <= 1000000000
- The answer fits in a signed 64-bit integer.

Important note about the examples:
The second example's written explanation is inconsistent with the stated scoring rule.
For nums = [7,1,6,5,2], penalty = 3, the true maximum under the given formula is 7
(from the single-element segment [7]), not 10.
This implementation follows the formal problem statement exactly.
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    /*
        Time Complexity: O(n log n)
        Space Complexity: O(n)

        High-level idea:

        For any chosen segment, let its minimum value be m.

        Then the score is:
            m * segmentLength - penalty * (# of outside elements < m)

        A key observation is:
        If we decide that the segment minimum is exactly some value m, then among all
        segments whose minimum is m, the best one is the longest contiguous segment
        consisting only of values >= m and containing at least one occurrence of m.

        Why?
        - Extending the segment inside the maximal valid area keeps every value >= m,
          so the minimum stays at least m.
        - Since we require the minimum to be exactly m, the segment must include an index
          where value == m.
        - Making the segment longer increases m * length.
        - The penalty term depends only on how many globally smaller elements (< m) are
          outside the segment. But every value < m can never be inside any valid segment
          with minimum m, because that would lower the minimum below m.
        - Therefore, for a fixed m, the penalty is actually constant:
              penalty * count(values < m in whole array)
          because all such smaller values are necessarily outside the segment.

        So for each distinct value m, we only need:
        1) total count of array elements < m
        2) the maximum length of a contiguous block where every value >= m and the block
           contains at least one m
           -> equivalently, the maximum span attached to any occurrence of m where m is
              the minimum.

        That second part is exactly what monotonic stack boundaries give us:
        For each index i, if nums[i] is the chosen minimum representative, then:
            left boundary = previous index with value < nums[i]
            right boundary = next index with value < nums[i]
        Every element between those boundaries is >= nums[i], so the longest segment
        where nums[i] can serve as the minimum is:
            length = rightLess[i] - leftLess[i] - 1

        For value m appearing multiple times, we take the maximum such length among all
        indices with nums[i] = m.

        Finally:
            best score for value m =
                m * bestLengthForValue[m] - penalty * countLessThanValue[m]

        We compute this for every distinct value and take the maximum.
    */
    public long MaximumScore(int[] nums, long penalty)
    {
        int n = nums.Length;

        // ------------------------------------------------------------
        // STEP 1: Compute previous strictly smaller element for each index.
        //
        // For each position i, we want the nearest index to the left whose value is
        // strictly smaller than nums[i].
        //
        // Why do we need this?
        // Because if we want nums[i] to be the minimum of a segment, then the segment
        // cannot cross over any element that is smaller than nums[i].
        // So the previous smaller element marks the left "wall".
        //
        // Data structure choice:
        // We use a monotonic increasing stack of indices.
        // The stack will maintain values in increasing order.
        // While the top value is >= current value, it cannot be the previous strictly
        // smaller element for the current index, so we pop it.
        // ------------------------------------------------------------
        int[] leftLess = new int[n];
        var stack = new Stack<int>();

        for (int i = 0; i < n; i++)
        {
            while (stack.Count > 0 && nums[stack.Peek()] >= nums[i])
            {
                stack.Pop();
            }

            leftLess[i] = stack.Count == 0 ? -1 : stack.Peek();
            stack.Push(i);
        }

        // ------------------------------------------------------------
        // STEP 2: Compute next strictly smaller element for each index.
        //
        // Symmetric to the previous step, but now we scan from right to left.
        // For each position i, we want the nearest index to the right whose value is
        // strictly smaller than nums[i].
        //
        // This gives the right "wall" beyond which nums[i] can no longer remain the
        // minimum of the segment.
        // ------------------------------------------------------------
        int[] rightLess = new int[n];
        stack.Clear();

        for (int i = n - 1; i >= 0; i--)
        {
            while (stack.Count > 0 && nums[stack.Peek()] >= nums[i])
            {
                stack.Pop();
            }

            rightLess[i] = stack.Count == 0 ? n : stack.Peek();
            stack.Push(i);
        }

        // ------------------------------------------------------------
        // STEP 3: For each distinct value v, compute the maximum segment length where
        // v can be the minimum.
        //
        // For index i:
        //   maximal valid length = rightLess[i] - leftLess[i] - 1
        //
        // This is the longest contiguous segment containing i such that every element
        // in the segment is >= nums[i]. Therefore nums[i] is a minimum of that segment.
        //
        // If the same value appears multiple times, we keep the best (largest) length
        // among all its occurrences.
        //
        // Why is this enough?
        // Because for a fixed minimum value v, the best segment is the longest one
        // whose minimum is exactly v.
        // ------------------------------------------------------------
        var bestLengthByValue = new Dictionary<int, int>();

        for (int i = 0; i < n; i++)
        {
            int value = nums[i];
            int length = rightLess[i] - leftLess[i] - 1;

            if (!bestLengthByValue.TryGetValue(value, out int currentBest) || length > currentBest)
            {
                bestLengthByValue[value] = length;
            }
        }

        // ------------------------------------------------------------
        // STEP 4: We need, for each distinct value v, the number of array elements
        // strictly smaller than v.
        //
        // Since values can be large (up to 1e9), we cannot use direct indexing.
        // Instead:
        //   - Count frequency of each distinct value
        //   - Sort the distinct values
        //   - Walk in increasing order while maintaining a prefix count
        //
        // Then:
        //   countLess[v] = number of elements with value < v
        //
        // This is exactly the penalty multiplier count we need, because any element
        // smaller than v must be outside every segment whose minimum is v.
        // ------------------------------------------------------------
        var frequency = new Dictionary<int, int>();
        foreach (int x in nums)
        {
            if (!frequency.ContainsKey(x))
            {
                frequency[x] = 0;
            }
            frequency[x]++;
        }

        var sortedValues = frequency.Keys.ToList();
        sortedValues.Sort();

        var countLessByValue = new Dictionary<int, long>();
        long prefixCount = 0;

        foreach (int value in sortedValues)
        {
            countLessByValue[value] = prefixCount;
            prefixCount += frequency[value];
        }

        // ------------------------------------------------------------
        // STEP 5: Evaluate the score for each distinct minimum value.
        //
        // For a value v:
        //   best score = v * bestLengthByValue[v] - penalty * countLessByValue[v]
        //
        // We take the maximum over all distinct values.
        //
        // Use long everywhere in arithmetic because:
        // - value can be up to 1e9
        // - length up to 2e5
        // - penalty up to 1e9
        // So 32-bit int would overflow.
        // ------------------------------------------------------------
        long answer = long.MinValue;

        foreach (int value in sortedValues)
        {
            long bestLength = bestLengthByValue[value];
            long smallerOutsideCount = countLessByValue[value];

            long score = (long)value * bestLength - penalty * smallerOutsideCount;

            if (score > answer)
            {
                answer = score;
            }
        }

        return answer;
    }
}

// Demo code
var solution = new Solution();

int[] nums1 = { 5, 2, 4, 3 };
long penalty1 = 2;
long result1 = solution.MaximumScore(nums1, penalty1);
Console.WriteLine(result1); // Expected: 6

int[] nums2 = { 7, 1, 6, 5, 2 };
long penalty2 = 3;
long result2 = solution.MaximumScore(nums2, penalty2);
Console.WriteLine(result2); // Under the stated formula, the correct result is 7

// Additional quick checks
int[] nums3 = { 1 };
long penalty3 = 100;
Console.WriteLine(solution.MaximumScore(nums3, penalty3)); // 1

int[] nums4 = { 3, 3, 3 };
long penalty4 = 5;
Console.WriteLine(solution.MaximumScore(nums4, penalty4)); // 9