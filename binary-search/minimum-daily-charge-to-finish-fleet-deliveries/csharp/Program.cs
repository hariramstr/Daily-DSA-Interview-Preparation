/*
Title: Minimum Daily Charge to Finish Fleet Deliveries
Difficulty: Medium
Topic: Binary Search

Problem Description:
A logistics company operates a fleet of electric vans. Each van i must complete deliveryDistance[i] kilometers of work.
If the company sets the charging system to provide a daily charge budget of X kilometers per van, then van i can
complete its route in ceil(deliveryDistance[i] / X) days, because it can recharge to cover at most X kilometers per day.

All vans work in parallel, and the company wants every van to finish within at most maxDays total days.

Your task is to find the minimum integer value X such that the sum of days needed by all vans is less than or equal
to maxDays. If X is too small, the total required days will exceed the limit. If X is large enough, the fleet can
finish on time.

Return the smallest possible daily charge budget X.

Constraints:
- 1 <= deliveryDistance.length <= 100000
- 1 <= deliveryDistance[i] <= 1000000000
- deliveryDistance.length <= maxDays <= 1000000000000
- X must be a positive integer

Examples:
1)
Input: deliveryDistance = [12, 7, 25, 9], maxDays = 10
Expected minimum valid X:
- X = 6 => days = ceil(12/6)+ceil(7/6)+ceil(25/6)+ceil(9/6) = 2+2+5+2 = 11 (too many)
- X = 7 => days = ceil(12/7)+ceil(7/7)+ceil(25/7)+ceil(9/7) = 2+1+4+2 = 9 (valid)
So answer = 7

2)
Input: deliveryDistance = [3, 6, 14], maxDays = 8
Check carefully:
- X = 3 => days = ceil(3/3)+ceil(6/3)+ceil(14/3) = 1+2+5 = 8 (valid)
- X = 2 => days = 2+3+7 = 12 (too many)
Therefore the minimum valid X is 3, not 4.
So answer = 3

Important note:
The second example's written "Output: 4" conflicts with its own explanation.
The explanation proves the correct answer is 3.
This solution follows the mathematically correct interpretation of the problem.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * log M)
      where:
      n = number of vans (deliveryDistance.Length)
      M = maximum value in deliveryDistance
    Explanation:
    - We binary search the answer X in the range [1, maxDistance].
    - For each candidate X, we scan the entire array once to compute total required days.
    - Binary search performs about log2(maxDistance) iterations.

    Space Complexity:
    - O(1) extra space
    Explanation:
    - We only use a few variables.
    - No extra data structures proportional to input size are created.
    */
    public int MinDailyCharge(int[] deliveryDistance, long maxDays)
    {
        // Step 1:
        // We need to determine the search range for binary search.
        //
        // Why binary search works:
        // - If a certain daily charge X is sufficient, then any larger value than X is also sufficient,
        //   because increasing daily charge can only reduce or keep the same number of days needed.
        // - If a certain X is NOT sufficient, then any smaller value is also not sufficient.
        //
        // This creates a monotonic "false, false, false, ..., true, true, true" pattern,
        // which is exactly the kind of situation where binary search is ideal.
        //
        // Lower bound:
        // - The smallest possible positive integer X is 1.
        //
        // Upper bound:
        // - The largest distance in the array is always enough, because then each van finishes in 1 day.
        // - Total days would then equal the number of vans, and the problem guarantees that is <= maxDays.
        int left = 1;
        int right = 0;

        // Find the maximum delivery distance to establish the right boundary of the search.
        foreach (int distance in deliveryDistance)
        {
            if (distance > right)
            {
                right = distance;
            }
        }

        // Step 2:
        // Perform standard binary search on the answer space.
        //
        // Goal:
        // - Find the smallest X such that total required days <= maxDays.
        //
        // Invariant we maintain:
        // - The answer always remains somewhere inside [left, right].
        while (left < right)
        {
            // Compute the middle candidate carefully.
            // Using this form avoids overflow in general:
            // mid = left + (right - left) / 2
            int mid = left + (right - left) / 2;

            // Step 3:
            // Check whether this candidate daily charge "mid" is sufficient.
            //
            // We compute:
            // totalDays = sum( ceil(deliveryDistance[i] / mid) )
            //
            // Why use long?
            // - maxDays can be as large as 1,000,000,000,000.
            // - The sum of required days can also become very large.
            // - int would not be safe enough here.
            long totalDays = 0;

            foreach (int distance in deliveryDistance)
            {
                // Step 3a:
                // Compute ceil(distance / mid) using integer arithmetic.
                //
                // Formula:
                // ceil(a / b) = (a + b - 1) / b
                //
                // Why this is useful:
                // - It avoids floating-point operations.
                // - It is exact and efficient.
                totalDays += (distance + (long)mid - 1) / mid;

                // Step 3b:
                // Small optimization:
                // If totalDays already exceeds maxDays, we can stop early.
                //
                // Why this is safe:
                // - We only care whether totalDays <= maxDays or not.
                // - Once it is already too large, additional computation cannot change that fact.
                if (totalDays > maxDays)
                {
                    break;
                }
            }

            // Step 4:
            // Use the feasibility result to shrink the search range.
            if (totalDays <= maxDays)
            {
                // "mid" is sufficient.
                //
                // But we are not done yet, because there might be a smaller valid X.
                // So we keep searching on the left side, including mid itself.
                right = mid;
            }
            else
            {
                // "mid" is not sufficient.
                //
                // Therefore every value <= mid is also not sufficient.
                // So we must search strictly to the right of mid.
                left = mid + 1;
            }
        }

        // Step 5:
        // When left == right, binary search has converged to the smallest valid X.
        return left;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print results.

var solution = new Solution();

// Example 1
int[] deliveryDistance1 = { 12, 7, 25, 9 };
long maxDays1 = 10;
int result1 = solution.MinDailyCharge(deliveryDistance1, maxDays1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 7

// Example 2
// The problem statement contains a contradiction:
// it says output 4, but its own explanation proves the minimum valid answer is 3.
// We print the mathematically correct result.
int[] deliveryDistance2 = { 3, 6, 14 };
long maxDays2 = 8;
int result2 = solution.MinDailyCharge(deliveryDistance2, maxDays2);
Console.WriteLine($"Example 2 Result: {result2}"); // Correct Expected: 3

// Additional quick sanity check
int[] deliveryDistance3 = { 1, 1, 1, 1 };
long maxDays3 = 4;
int result3 = solution.MinDailyCharge(deliveryDistance3, maxDays3);
Console.WriteLine($"Additional Example Result: {result3}"); // Expected: 1