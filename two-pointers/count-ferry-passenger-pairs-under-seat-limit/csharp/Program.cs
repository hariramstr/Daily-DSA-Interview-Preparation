/*
Title: Count Ferry Passenger Pairs Under Seat Limit

Problem Description:
A ferry operator wants to offer a discounted ticket to every pair of passengers who can share one bench.
You are given an integer array weights where weights[i] is the weight of the i-th passenger, and an integer
limit representing the maximum total weight that a single bench can safely support.

Each discounted pair must consist of two different passengers, and a pair is considered valid if the sum
of their weights is less than or equal to limit.

Return the total number of distinct valid pairs (i, j) such that:
0 <= i < j < n
and
weights[i] + weights[j] <= limit

The solution should be efficient for large inputs, so we avoid the O(n^2) brute-force approach and use
sorting plus a two-pointer strategy.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the array takes O(n log n)
    - The two-pointer scan takes O(n)
    - Total: O(n log n)

    Space Complexity:
    - If we sort the input array in place, the extra algorithmic space is O(1)
      (ignoring the internal implementation details of the sorting routine)
    */
    public long CountValidPairs(int[] weights, int limit)
    {
        // Step 1:
        // Sort the weights in non-decreasing order.
        //
        // Why do we sort?
        // Because once the array is sorted, we can use the relative order of values
        // to count many valid pairs at once instead of checking every possible pair.
        //
        // Example:
        // If weights[left] + weights[right] is valid, then weights[left] paired with
        // every element between left+1 and right is also valid, because those elements
        // are <= weights[right].
        Array.Sort(weights);

        // Step 2:
        // Create two pointers:
        // - left starts at the lightest passenger
        // - right starts at the heaviest passenger
        //
        // We will move these pointers toward each other.
        int left = 0;
        int right = weights.Length - 1;

        // Step 3:
        // Use a 64-bit integer (long) for the answer.
        //
        // Why long?
        // Because the number of valid pairs can be very large.
        // For n = 200000, the number of pairs can be close to n*(n-1)/2,
        // which does not safely fit in a 32-bit int.
        long count = 0;

        // Step 4:
        // Continue while there are at least two different passengers to consider.
        //
        // Condition left < right ensures:
        // - we never pair a passenger with themselves
        // - we only count each pair once
        while (left < right)
        {
            // Step 4a:
            // Compute the sum of the lightest remaining passenger and the heaviest
            // remaining passenger.
            //
            // We cast to long before adding to be extra safe, although int is enough
            // for the given constraints. This is a good habit when sums may grow.
            long currentSum = (long)weights[left] + weights[right];

            // Step 4b:
            // If this sum is within the limit, then we can count multiple pairs at once.
            if (currentSum <= limit)
            {
                // Why can we add (right - left)?
                //
                // Since the array is sorted:
                // weights[left] <= weights[left + 1] <= ... <= weights[right]
                //
                // If weights[left] + weights[right] <= limit,
                // then weights[left] + weights[k] <= limit for every k in [left+1, right]
                // because weights[k] <= weights[right].
                //
                // That means the passenger at index left can form a valid pair with:
                // left+1, left+2, ..., right
                //
                // Number of such passengers = right - left
                count += right - left;

                // After counting all pairs involving weights[left], we move left forward.
                //
                // Why?
                // Because we have already counted every valid pair that starts with this
                // left passenger. There is nothing more to gain by keeping left here.
                left++;
            }
            else
            {
                // If the sum is too large, then the heaviest passenger at 'right'
                // cannot pair with the lightest passenger at 'left'.
                //
                // Since weights[right] is the heaviest remaining passenger, pairing it
                // with any passenger heavier than weights[left] would only make the sum
                // even larger.
                //
                // Therefore, weights[right] cannot form a valid pair with any current
                // passenger from left to right-1, so we must reduce the weight on the
                // right side by moving 'right' one step left.
                right--;
            }
        }

        // Step 5:
        // Return the total number of distinct valid pairs.
        return count;
    }
}

// Demo code:
// Creates sample inputs, calls the solution, and prints the results.

var solution = new Solution();

// Example 1:
// weights = [2, 3, 4, 5], limit = 7
// Sorted: [2, 3, 4, 5]
//
// Valid pairs:
// (2,3) = 5
// (2,4) = 6
// (2,5) = 7
// (3,4) = 7
// Total = 4
int[] weights1 = { 2, 3, 4, 5 };
int limit1 = 7;
long result1 = solution.CountValidPairs(weights1, limit1);
Console.WriteLine(result1); // Expected: 4

// Example 2:
// weights = [1, 1, 2, 2, 3], limit = 3
// Sorted: [1, 1, 2, 2, 3]
//
// Valid pairs:
// (1,1) -> 1 pair
// (1,2) -> 2 * 2 = 4 pairs
// (1,3) -> 2 pairs because 1 + 3 = 4, which is NOT allowed for limit 3, so 0 pairs
// (2,2) -> 4, not allowed
// (2,3) -> 5, not allowed
//
// Total = 1 + 4 = 5
//
// Note:
// The problem statement's Example 2 says output 6, but that conflicts with the rule
// "sum <= limit" when limit = 3. The correct count is 5.
int[] weights2 = { 1, 1, 2, 2, 3 };
int limit2 = 3;
long result2 = solution.CountValidPairs(weights2, limit2);
Console.WriteLine(result2); // Correct according to the stated rule: 5

// Additional quick demo:
int[] weights3 = { 3, 5, 1, 2, 4 };
int limit3 = 6;
// Sorted becomes [1,2,3,4,5]
// Valid pairs:
// (1,2), (1,3), (1,4), (1,5), (2,3), (2,4)
// Total = 6
long result3 = solution.CountValidPairs(weights3, limit3);
Console.WriteLine(result3); // Expected: 6