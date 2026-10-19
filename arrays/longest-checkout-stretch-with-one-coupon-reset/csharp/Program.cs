/*
Title: Longest Checkout Stretch With One Coupon Reset

Problem Description:
A retail system records the coupon code used for each consecutive customer in a checkout line.
The array coupons represents these coupon codes in order, where coupons[i] is the code used
by the i-th customer.

A checkout stretch is considered consistent if all customers in that contiguous stretch use
the same coupon code.

You are allowed to perform at most one reset operation:
- choose exactly one customer in the stretch
- change that customer's coupon code to match the coupon code of the rest of the stretch

Your task is to return the length of the longest contiguous stretch that can be made consistent
using at most one such reset.

In other words, find the maximum length of a subarray that can be turned into all equal values
after modifying at most one element inside that subarray.

Constraints:
- 1 <= coupons.length <= 200000
- 1 <= coupons[i] <= 1000000000

Examples:
1) coupons = [5,5,3,5,5,5]
   Output: 6
   Explanation: Change the 3 at index 2 to 5. The entire array becomes a consistent stretch of length 6.

2) coupons = [1,2,2,3,2,2,2]
   Output: 6
   Explanation: Consider the subarray [2,2,3,2,2,2]. By changing the 3 to 2, this stretch becomes all 2's, so the answer is 6.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Idea:
    We want the longest contiguous subarray that can become all one value after changing at most one element.

    A very useful way to think about this is:
    - Pick a target value x.
    - We want the longest subarray containing only x, except possibly one position that is not x.
    - That means inside the chosen subarray, the number of "non-x" elements must be at most 1.

    Instead of checking every subarray, we process each distinct value separately using the positions
    where that value appears.

    Suppose the positions of value x are:
        p0, p1, p2, ..., pk-1

    If we want a subarray that becomes all x after at most one change, then among the positions between
    the first chosen x occurrence and the last chosen x occurrence, there can be at most one index that
    is not x.

    For consecutive occurrences:
    - If pi and p(i+1) are adjacent or equal distance 1 apart, there are 0 non-x elements between them.
    - If p(i+1) = pi + 2, there is exactly 1 non-x element between them.
    - If the gap is larger than 2, then there are at least 2 non-x elements between them, which is too many
      for a single reset if we try to connect across that gap.

    Therefore, for each value, we scan its occurrence positions and maintain a window of occurrences such that
    the total number of missing positions between them is at most 1.

    If a window uses positions from positions[left] to positions[right], then:
    - The actual contiguous subarray length is:
          positions[right] - positions[left] + 1
    - The number of non-target elements inside that subarray is:
          (positions[right] - positions[left] + 1) - (right - left + 1)
      because total length minus number of target occurrences equals number of other values.

    We keep this number <= 1 using a sliding window.

    This gives the best answer for every target value, and the global maximum is the final answer.
    */
    public int LongestCheckoutStretchWithOneCouponReset(int[] coupons)
    {
        // Step 1:
        // Group all indices by coupon value.
        //
        // Why?
        // We want to process each possible final uniform value separately.
        // For example, if we want a subarray to become all 5's, then only the positions of 5 matter.
        //
        // Data structure choice:
        // Dictionary<int, List<int>>
        // - key   = coupon value
        // - value = sorted list of indices where that coupon appears
        //
        // Since we scan the array from left to right and append indices,
        // each list is naturally sorted.
        var positionsByValue = new Dictionary<int, List<int>>();

        for (int i = 0; i < coupons.Length; i++)
        {
            int value = coupons[i];

            if (!positionsByValue.ContainsKey(value))
            {
                positionsByValue[value] = new List<int>();
            }

            positionsByValue[value].Add(i);
        }

        // At minimum, any single element subarray is already consistent,
        // so the answer is at least 1.
        int best = 1;

        // Step 2:
        // For each distinct coupon value, find the longest contiguous segment
        // that can become all that value after changing at most one element.
        foreach (var entry in positionsByValue)
        {
            List<int> positions = entry.Value;

            // Sliding window over the occurrence indices of the current value.
            int left = 0;

            for (int right = 0; right < positions.Count; right++)
            {
                // Step 2a:
                // Shrink the window while it contains more than one "foreign" element.
                //
                // Let's carefully compute how many non-current-value elements are inside
                // the contiguous segment from positions[left] to positions[right].
                //
                // Total segment length:
                //     positions[right] - positions[left] + 1
                //
                // Number of current-value elements inside this segment:
                //     right - left + 1
                //
                // Therefore number of other elements inside:
                //     total length - count of current value occurrences
                //
                // We are allowed to modify at most one element, so this must be <= 1.
                while ((positions[right] - positions[left] + 1) - (right - left + 1) > 1)
                {
                    left++;
                }

                // Step 2b:
                // Now the window is valid: it contains at most one non-target element.
                //
                // That means the entire contiguous segment from positions[left] to positions[right]
                // can be turned into all the current value with at most one reset.
                int currentLength = positions[right] - positions[left] + 1;

                if (currentLength > best)
                {
                    best = currentLength;
                }
            }
        }

        return best;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] coupons1 = { 5, 5, 3, 5, 5, 5 };
int result1 = solution.LongestCheckoutStretchWithOneCouponReset(coupons1);
Console.WriteLine("Example 1:");
Console.WriteLine("Input: [5,5,3,5,5,5]");
Console.WriteLine("Output: " + result1);
Console.WriteLine("Expected: 6");
Console.WriteLine();

// Example 2
int[] coupons2 = { 1, 2, 2, 3, 2, 2, 2 };
int result2 = solution.LongestCheckoutStretchWithOneCouponReset(coupons2);
Console.WriteLine("Example 2:");
Console.WriteLine("Input: [1,2,2,3,2,2,2]");
Console.WriteLine("Output: " + result2);
Console.WriteLine("Expected: 6");
Console.WriteLine();

// Additional demos
int[] coupons3 = { 7 };
int result3 = solution.LongestCheckoutStretchWithOneCouponReset(coupons3);
Console.WriteLine("Additional Example 3:");
Console.WriteLine("Input: [7]");
Console.WriteLine("Output: " + result3);
Console.WriteLine("Expected: 1");
Console.WriteLine();

int[] coupons4 = { 4, 4, 4, 4 };
int result4 = solution.LongestCheckoutStretchWithOneCouponReset(coupons4);
Console.WriteLine("Additional Example 4:");
Console.WriteLine("Input: [4,4,4,4]");
Console.WriteLine("Output: " + result4);
Console.WriteLine("Expected: 4");
Console.WriteLine();

int[] coupons5 = { 1, 2, 1, 2, 1 };
int result5 = solution.LongestCheckoutStretchWithOneCouponReset(coupons5);
Console.WriteLine("Additional Example 5:");
Console.WriteLine("Input: [1,2,1,2,1]");
Console.WriteLine("Output: " + result5);
Console.WriteLine("Expected: 3");
Console.WriteLine();