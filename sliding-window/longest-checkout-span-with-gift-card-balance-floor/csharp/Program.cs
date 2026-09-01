/*
Title: Longest Checkout Span With Gift Card Balance Floor
Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given an integer array transactions where transactions[i] represents the net effect of the i-th checkout event on a customer's gift card balance. A positive value means money was added to the card, and a negative value means money was spent.

The customer starts with an initial gift card balance startBalance. You want to find the longest contiguous span of checkout events that could be processed in order such that, at every point inside that span, the running balance never drops below 0.

Formally, for a subarray transactions[l...r], define the running balance inside the span as startBalance plus the prefix sum of that subarray up to each position. The span is valid if for every index k between l and r, the balance after processing transactions[l...k] is at least 0.

Return the length of the longest valid contiguous span.

This is a sliding window problem: as you expand the right end of the window, the window may become invalid because some prefix inside the current window causes the balance to go negative. You must then shrink the left end until the window becomes valid again.

Constraints:
- 1 <= transactions.length <= 200000
- -100000 <= transactions[i] <= 100000
- 0 <= startBalance <= 1000000000

Example 1:
Input: transactions = [4, -3, -2, 5, -1], startBalance = 2
Output: 5
Explanation: Starting from 2, the running balances are 6, 3, 1, 6, 5. They never go below 0, so the entire array is valid.

Example 2:
Input: transactions = [-4, 3, -2, -1, 2], startBalance = 2
Output: 4
Explanation: The full array is invalid because the first event would make the balance -2. The longest valid span is [3, -2, -1, 2]. Starting from 2, the running balances in that span are 5, 3, 2, 4, so the answer is 4.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Idea:
    We convert the condition for a window [l..r] into a prefix-sum condition.

    Let prefix[i] = sum of transactions[0..i-1], with prefix[0] = 0.

    For a chosen window [l..r], the running sum inside that window after processing up to index k is:
        transactions[l] + transactions[l+1] + ... + transactions[k]
      = prefix[k+1] - prefix[l]

    The balance must never go below 0, so for every k in [l..r]:
        startBalance + (prefix[k+1] - prefix[l]) >= 0

    Rearranging:
        prefix[k+1] >= prefix[l] - startBalance

    Therefore, the entire window [l..r] is valid if the minimum prefix value among:
        prefix[l+1], prefix[l+2], ..., prefix[r+1]
    is at least:
        prefix[l] - startBalance

    So while we slide the window:
    - We need to know the minimum prefix value inside the current window's "future prefix positions".
    - A monotonic deque lets us maintain that minimum in O(1) amortized time.
    */
    public int LongestValidSpan(int[] transactions, long startBalance)
    {
        int n = transactions.Length;

        // Step 1:
        // Build prefix sums.
        //
        // Why?
        // Prefix sums let us describe any subarray running total quickly.
        // This transforms the "every partial sum inside the window" condition
        // into a condition involving the minimum prefix sum in a range.
        //
        // We use long instead of int because:
        // - n can be as large as 200,000
        // - values can be as large as 100,000 in magnitude
        // The total sum can exceed int range.
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++)
        {
            prefix[i + 1] = prefix[i] + transactions[i];
        }

        // Step 2:
        // We will maintain a sliding window [left..right] over transaction indices.
        //
        // For this window, the relevant prefix indices inside the window are:
        // prefix[left + 1] through prefix[right + 1]
        //
        // We need the minimum of that range.
        //
        // To do that efficiently, we use a monotonic deque storing prefix indices.
        // The deque will be increasing by prefix value:
        // - front = index of smallest prefix value in the current range
        //
        // This is a standard technique for "window minimum".
        LinkedList<int> deque = new LinkedList<int>();

        int left = 0;
        int best = 0;

        // Step 3:
        // Expand the right end of the window one transaction at a time.
        for (int right = 0; right < n; right++)
        {
            int prefixIndexToAdd = right + 1;

            // Step 3a:
            // Add prefix[right + 1] into our deque.
            //
            // Why prefix[right + 1]?
            // Because for a window [left..right], the partial sums correspond to:
            // prefix[left+1], prefix[left+2], ..., prefix[right+1]
            //
            // We maintain the deque in increasing order of prefix values.
            // So before appending the new index, we remove all larger-or-equal values
            // from the back, because they can never become the minimum while the new
            // smaller value remains in the window.
            while (deque.Count > 0 && prefix[deque.Last!.Value] >= prefix[prefixIndexToAdd])
            {
                deque.RemoveLast();
            }
            deque.AddLast(prefixIndexToAdd);

            // Step 3b:
            // Now the window [left..right] may be invalid.
            //
            // Validity condition:
            // min(prefix[left+1..right+1]) >= prefix[left] - startBalance
            //
            // The deque front gives us that minimum.
            //
            // If invalid, we must move left forward until the condition becomes valid.
            while (left <= right)
            {
                long minPrefixInsideWindow = prefix[deque.First!.Value];
                long requiredFloor = prefix[left] - startBalance;

                // If the minimum prefix inside the window is high enough,
                // then every running balance in this window stays >= 0.
                if (minPrefixInsideWindow >= requiredFloor)
                {
                    break;
                }

                // Otherwise the current window is invalid, so we shrink from the left.
                //
                // Before incrementing left, we must remove prefix[left + 1] from the deque
                // if it is currently at the front and is leaving the window.
                //
                // Why prefix[left + 1]?
                // Because the active prefix range for window [left..right] is
                // [left+1 .. right+1]. After moving left to left+1, the new active range is
                // [left+2 .. right+1], so prefix[left+1] leaves the range.
                if (deque.Count > 0 && deque.First!.Value == left + 1)
                {
                    deque.RemoveFirst();
                }

                left++;
            }

            // Step 3c:
            // At this point, [left..right] is valid.
            // So we can update the best answer.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] transactions1 = { 4, -3, -2, 5, -1 };
long startBalance1 = 2;
int result1 = solution.LongestValidSpan(transactions1, startBalance1);
Console.WriteLine(result1); // Expected: 5

// Example 2
int[] transactions2 = { -4, 3, -2, -1, 2 };
long startBalance2 = 2;
int result2 = solution.LongestValidSpan(transactions2, startBalance2);
Console.WriteLine(result2); // Expected: 4

// Additional quick checks
int[] transactions3 = { -5 };
long startBalance3 = 4;
Console.WriteLine(solution.LongestValidSpan(transactions3, startBalance3)); // Expected: 0

int[] transactions4 = { -5 };
long startBalance4 = 5;
Console.WriteLine(solution.LongestValidSpan(transactions4, startBalance4)); // Expected: 1

int[] transactions5 = { 1, -1, 1, -1, 1, -1 };
long startBalance5 = 0;
Console.WriteLine(solution.LongestValidSpan(transactions5, startBalance5)); // Expected: 6