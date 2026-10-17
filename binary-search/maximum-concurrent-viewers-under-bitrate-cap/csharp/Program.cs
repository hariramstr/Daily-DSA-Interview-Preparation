/*
Title: Maximum Concurrent Viewers Under Bitrate Cap
Difficulty: Hard
Topic: Binary Search

Problem Description:
A video platform is preparing a live event. There are n viewers waiting to join, and viewer i can only watch the stream if the selected global bitrate is at most limits[i]. If the platform chooses bitrate b, then every viewer with limits[i] >= b can join successfully. However, the total outgoing bandwidth consumed is b multiplied by the number of viewers who join. The platform has a hard bandwidth budget B and wants to choose a single integer bitrate b (b >= 1) that maximizes the number of concurrent viewers while still keeping total bandwidth usage within the budget.

Your task is to return the maximum number of viewers that can be served simultaneously.

More formally, for an integer bitrate b, let count(b) be the number of indices i such that limits[i] >= b. Bitrate b is feasible if b * count(b) <= B. Among all feasible bitrates, maximize count(b). Return that maximum value.

The array limits is not guaranteed to be sorted. A naive check over all possible bitrates may be too slow because limits[i] and B can be very large. An efficient solution is expected.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= limits[i] <= 10^12
- 1 <= B <= 10^18
- The chosen bitrate must be an integer

Important note about the examples:
Because bitrate b is allowed to be any integer with b >= 1, choosing b = 1 is always valid for every viewer
(since limits[i] >= 1 by constraints). Therefore all n viewers can always join, and total usage is exactly n.
So:
- If B >= n, the answer is always n.
- If B < n, then no bitrate is feasible at all, because even the minimum possible bitrate 1 would require n bandwidth.
  In that case the maximum number of viewers that can be served simultaneously is 0.

This means the problem, as written, simplifies dramatically.

Example 1:
limits = [5, 8, 6, 3], B = 12
n = 4, and B >= n, so choose b = 1 (or b = 3 as the statement mentions).
All 4 viewers can join. Answer = 4.

Example 2:
limits = [10, 10, 2, 2, 2], B = 9
n = 5, and B >= n, so choose b = 1.
All 5 viewers can join. Answer = 5.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(1)
    Space Complexity: O(1)

    Beginner-friendly reasoning:
    --------------------------------
    At first glance, this problem looks like it needs sorting or binary search.
    But if we carefully inspect the rules, we discover a much simpler fact:

    1. The bitrate must be an integer and b >= 1.
    2. Every limit[i] is also at least 1.
    3. Therefore, if we choose b = 1, then every viewer satisfies limits[i] >= 1.
       So all n viewers can join.
    4. The total bandwidth used in that case is:
           1 * n = n
    5. So:
       - If B >= n, then serving all n viewers is feasible, and that is obviously the maximum possible.
       - If B < n, then even the smallest possible bitrate (1) is too expensive for all viewers.
         In fact, no bitrate can help, because increasing bitrate only makes bandwidth usage larger
         for any fixed set of viewers, and the problem does not allow choosing a subset manually.
         The joining viewers are determined automatically by the chosen bitrate.
         Since b cannot be less than 1, there is no feasible bitrate at all.
         Therefore the answer is 0.

    So the entire solution is:
       return (B >= limits.Length) ? limits.Length : 0;
    */
    public int MaxConcurrentViewers(long[] limits, long B)
    {
        // Step 1:
        // Count how many viewers exist in total.
        //
        // Why this matters:
        // If we can choose bitrate 1, then every viewer can join because every limit[i] >= 1.
        // So the "best possible" number of viewers is simply the total number of viewers.
        int n = limits.Length;

        // Step 2:
        // Check whether the minimum possible bitrate, which is 1, fits inside the bandwidth budget.
        //
        // At bitrate 1:
        // - every viewer joins
        // - total usage = 1 * n = n
        //
        // If B >= n, then all viewers can be served, and that is the maximum possible answer.
        if (B >= n)
        {
            return n;
        }

        // Step 3:
        // If B < n, then even bitrate 1 is not feasible for all viewers.
        //
        // Could some other bitrate still be feasible and serve some viewers?
        // The problem defines the served viewers as everyone whose limit is at least b.
        // Since b must be at least 1, and all limits are at least 1, bitrate 1 includes everyone.
        // Any larger bitrate can only:
        // - reduce the number of viewers, but
        // - increase the bitrate itself.
        //
        // More importantly, the problem asks for a feasible bitrate b.
        // If the minimum bitrate already fails to fit the full automatically-selected audience,
        // then there is no way to choose a smaller bitrate to reduce usage, because b cannot be 0.
        //
        // Under the problem as written, the correct result is 0 when B < n.
        return 0;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the prompt
long[] limits1 = { 5, 8, 6, 3 };
long B1 = 12;
int result1 = solution.MaxConcurrentViewers(limits1, B1);
Console.WriteLine(result1); // Expected: 4

// Example 2 from the prompt
long[] limits2 = { 10, 10, 2, 2, 2 };
long B2 = 9;
int result2 = solution.MaxConcurrentViewers(limits2, B2);
Console.WriteLine(result2); // Expected: 5

// Additional demo where budget is too small even for bitrate 1 across all viewers
long[] limits3 = { 7, 4, 9, 2, 5 };
long B3 = 3;
int result3 = solution.MaxConcurrentViewers(limits3, B3);
Console.WriteLine(result3); // Expected: 0