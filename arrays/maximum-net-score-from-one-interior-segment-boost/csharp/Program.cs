/*
Title: Maximum Net Score from One Interior Segment Boost

Problem Description:
You are given an integer array scores representing daily performance values for a product team.
Positive values help the team's quarterly score, while negative values hurt it.

Management is allowed to apply exactly one temporary boost to a contiguous interior segment of days.
If a segment from index l to r is boosted, where 0 < l <= r < n - 1, then every value inside that
segment contributes twice to the final total, while values outside the segment contribute normally.

Your task is to return the maximum possible final total score after choosing one valid interior segment
to boost.

If total is the sum of all elements in scores, and segSum is the sum of the chosen segment, then the
final score is:

    total + segSum

because the chosen segment is counted one extra time.

Important restriction:
The chosen segment must be fully interior, meaning:
    1 <= l <= r <= n - 2

So the first and last elements can never be included in the boosted segment.

Constraints:
- 3 <= scores.length <= 200000
- -100000 <= scores[i] <= 100000
- The chosen boosted segment must satisfy 1 <= l <= r <= n - 2

Examples:
1) scores = [4, -2, 3, -1, 5]
   total = 9
   interior elements are [-2, 3, -1]
   best interior subarray sum = 3
   answer = 9 + 3 = 12

2) scores = [7, -5, 4, 6, -2, 8]
   total = 18
   interior elements are [-5, 4, 6, -2]
   best interior subarray sum = 10 from [4, 6]
   answer = 18 + 10 = 28
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    We do two things in one pass:
    1. Compute the total sum of the whole array.
    2. Compute the maximum subarray sum only over the interior range [1 .. n-2].

    Why this works:
    - The final score after boosting a segment is:
          total + segmentSum
    - Since total is fixed, maximizing the final score is exactly the same as maximizing
      the sum of the chosen interior contiguous segment.
    - So the problem becomes:
          "Find the maximum subarray sum in the interior portion of the array."
    - That is a classic Kadane's algorithm application, but restricted to indices 1 through n-2.
    */
    public long MaximumNetScore(int[] scores)
    {
        // Step 1:
        // Compute the normal total score of the entire array.
        //
        // Why this is necessary:
        // The final answer is not just the best segment sum.
        // The problem says the boosted segment contributes one extra time,
        // so the final result is:
        //     total sum of all elements + sum of chosen segment
        //
        // We use long instead of int for safety.
        // Even though int might still fit under these constraints,
        // long is a good habit when summing many values.
        long total = 0;
        foreach (int value in scores)
        {
            total += value;
        }

        // Step 2:
        // We now need the maximum sum of any contiguous segment that lies completely
        // inside the array, meaning it can only use indices 1 through n - 2.
        //
        // This is exactly the "maximum subarray sum" problem on the interior slice.
        //
        // We will use Kadane's algorithm:
        // - currentBestEndingHere = best sum of a subarray that MUST end at current index
        // - bestOverall = best sum seen anywhere so far
        //
        // Since the segment must be non-empty and must be interior,
        // we initialize both values using scores[1], the first valid interior element.
        long currentBestEndingHere = scores[1];
        long bestOverall = scores[1];

        // Step 3:
        // Process the remaining interior elements from index 2 to index n - 2.
        //
        // At each position i, we decide:
        // - Should we extend the previous interior segment?
        // - Or should we start a brand new segment at i?
        //
        // This is the heart of Kadane's algorithm.
        for (int i = 2; i <= scores.Length - 2; i++)
        {
            // If extending the previous segment gives a better sum,
            // keep extending. Otherwise, start fresh at scores[i].
            //
            // Why this is correct:
            // Any best subarray ending at i must be either:
            // 1. the single element scores[i]
            // 2. the best subarray ending at i-1, extended by scores[i]
            currentBestEndingHere = Math.Max(scores[i], currentBestEndingHere + scores[i]);

            // Update the global best interior segment sum if the current one is better.
            bestOverall = Math.Max(bestOverall, currentBestEndingHere);
        }

        // Step 4:
        // The best final score is:
        //     total + best interior segment sum
        //
        // This matches the problem statement exactly.
        return total + bestOverall;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] scores1 = { 4, -2, 3, -1, 5 };
long result1 = solution.MaximumNetScore(scores1);
Console.WriteLine(result1); // Expected: 12

// Example 2
int[] scores2 = { 7, -5, 4, 6, -2, 8 };
long result2 = solution.MaximumNetScore(scores2);
Console.WriteLine(result2); // Expected: 28

// Additional quick checks

// Only one interior element exists: must choose it.
int[] scores3 = { 10, -7, 20 };
long result3 = solution.MaximumNetScore(scores3);
Console.WriteLine(result3); // total = 23, best interior = -7, final = 16

// Interior all negative: still must choose exactly one non-empty interior segment.
int[] scores4 = { 5, -4, -2, -9, 6 };
long result4 = solution.MaximumNetScore(scores4);
Console.WriteLine(result4); // total = -4, best interior = -2, final = -6