/*
Title: Longest Billing Window With Per-Customer Request Caps
Difficulty: Hard
Topic: Sliding Window

Problem Description:
A cloud platform records API requests in chronological order. Each request is labeled with the customer ID that generated it.
For billing analysis, you are given an array requests where requests[i] is the customer ID of the i-th request, and an integer limit.
A contiguous time window is considered billable if no customer appears more than limit times inside that window.

Your task is to return the length of the longest billable contiguous window.

This is harder than a standard frequency-limited window because the input size is large, customer IDs may be large integers,
and the optimal solution is expected to run in linear time using a sliding window with dynamic frequency tracking.
Any solution that repeatedly recomputes frequencies for candidate windows will be too slow.

Formally, find the maximum value of (right - left + 1) over all pairs 0 <= left <= right < n such that for every customer ID x,
the number of indices i in [left, right] with requests[i] = x is at most limit.

Constraints:
- 1 <= requests.length <= 200000
- 1 <= requests[i] <= 1000000000
- 1 <= limit <= requests.length
- requests is already ordered by time

Example 1:
Input: requests = [4, 7, 4, 2, 7, 4, 7], limit = 2
Output: 5
Explanation: The longest valid window is [4, 2, 7, 4, 7], where customer 4 appears 2 times and customer 7 appears 2 times.

Example 2:
Input: requests = [9, 9, 9, 3, 3, 8, 8, 8, 8], limit = 2
Output: 4
Explanation: One optimal window is [9, 3, 3, 8]. Any longer window would cause either customer 9 or customer 8 to appear more than 2 times.

We solve this with a classic sliding window:
- Expand the right side one request at a time.
- Track frequencies of customer IDs inside the current window using a dictionary.
- If adding the new request makes some customer exceed the allowed limit, move the left side forward
  until the window becomes valid again.
- At every step, record the largest valid window length seen so far.

This works in linear time because:
- Each element enters the window once when right moves forward.
- Each element leaves the window at most once when left moves forward.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each request is added to the sliding window once.
    - Each request is removed from the sliding window at most once.
    - Dictionary operations are O(1) average time.
    - Therefore the total work is linear in the number of requests.

    Space Complexity: O(k)
    - We store frequencies for the distinct customer IDs currently seen.
    - In the worst case, k can be the number of distinct IDs in the array, up to O(n).
    */
    public int LongestBillableWindow(int[] requests, int limit)
    {
        // This dictionary stores:
        // key   = customer ID
        // value = how many times that customer appears in the current window [left..right]
        //
        // We use Dictionary<int, int> because:
        // - customer IDs can be very large (up to 1,000,000,000)
        // - IDs are not guaranteed to be small or continuous
        // - a dictionary lets us track only the IDs that actually appear
        var frequency = new Dictionary<int, int>();

        // left marks the beginning of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // We expand the window by moving right from 0 to requests.Length - 1.
        for (int right = 0; right < requests.Length; right++)
        {
            // Step 1: Include requests[right] in the current window.
            //
            // We are extending the window to the right by one element.
            // That means the frequency of this customer inside the window increases by 1.
            int customer = requests[right];

            if (!frequency.ContainsKey(customer))
            {
                frequency[customer] = 0;
            }

            frequency[customer]++;

            // Step 2: If the newly added customer now appears too many times,
            // the window is invalid and we must shrink it from the left.
            //
            // Important observation:
            // Before adding requests[right], the window was valid.
            // After adding it, only this specific customer's count could have become invalid.
            // No other customer's count increased.
            //
            // So we only need to check whether frequency[customer] > limit.
            while (frequency[customer] > limit)
            {
                // We are about to remove requests[left] from the window.
                int leftCustomer = requests[left];

                // Decrease that customer's frequency because it is no longer inside the window.
                frequency[leftCustomer]--;

                // Move the left boundary rightward, making the window smaller.
                left++;
            }

            // Step 3: At this point, the window [left..right] is valid.
            //
            // Why is it valid?
            // - We kept shrinking until the only possible violating customer
            //   (the one we just added) no longer exceeds the limit.
            // - Since all other customers were already valid before this step,
            //   the whole window is now valid.
            //
            // Compute the current valid window length.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is larger.
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // After scanning the entire array, best is the length of the longest valid window.
        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] requests1 = { 4, 7, 4, 2, 7, 4, 7 };
int limit1 = 2;
int result1 = solution.LongestBillableWindow(requests1, limit1);
Console.WriteLine(result1); // Expected: 5

// Example 2
int[] requests2 = { 9, 9, 9, 3, 3, 8, 8, 8, 8 };
int limit2 = 2;
int result2 = solution.LongestBillableWindow(requests2, limit2);
Console.WriteLine(result2); // Expected: 4

// Additional quick demo
int[] requests3 = { 1, 2, 3, 4, 5 };
int limit3 = 1;
int result3 = solution.LongestBillableWindow(requests3, limit3);
Console.WriteLine(result3); // Expected: 5