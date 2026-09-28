/*
Title: Pair Guests for a Tandem Zipline

Problem Description:
An adventure park runs a tandem zipline where each ride must carry either one guest alone or two guests together.
For safety, the combined weight of any paired guests cannot exceed a given limit.

You are given:
- An integer array weights where weights[i] is the weight of the i-th guest
- An integer limit representing the maximum total weight allowed on one tandem ride

Return the minimum number of rides needed to send all guests down the zipline.

Rules:
- Each ride can take at most two guests
- A guest may ride alone
- Every guest must be assigned to exactly one ride
- The goal is to minimize the total number of rides

Key idea:
A very efficient strategy is:
1. Sort the weights
2. Try to pair the lightest remaining guest with the heaviest remaining guest
3. If they fit together, send them on one ride
4. If they do not fit, the heaviest guest must go alone
5. Repeat until all guests are assigned

Constraints:
- 1 <= weights.length <= 100000
- 1 <= weights[i] <= 1000000000
- 1 <= limit <= 1000000000
- It is guaranteed that every individual guest can ride alone, so weights[i] <= limit for all i

Example 1:
Input: weights = [70, 50, 80, 50], limit = 100
Output: 3
Explanation:
Sorted weights = [50, 50, 70, 80]
- 50 + 80 = 130 > 100, so 80 rides alone
- 50 + 70 = 120 > 100, so 70 rides alone
- 50 + 50 = 100 <= 100, so they ride together
Total rides = 3

Example 2:
Input: weights = [40, 60, 55, 45, 80], limit = 100
Output: 3
Explanation:
Sorted weights = [40, 45, 55, 60, 80]
- 40 + 80 = 120 > 100, so 80 rides alone
- 40 + 60 = 100 <= 100, so they ride together
- 45 + 55 = 100 <= 100, so they ride together
Total rides = 3
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n log n)
    - Sorting the array takes O(n log n)
    - The two-pointer scan takes O(n)

    Space Complexity: O(1) extra space (ignoring the sorting implementation details)
    - We sort the input array in place
    - We only use a few integer variables besides the input array
    */
    public int MinRides(int[] weights, int limit)
    {
        // Step 1:
        // Sort the weights in non-decreasing order.
        //
        // Why do we sort?
        // Sorting lets us use a very powerful greedy strategy with two pointers:
        // - One pointer starts at the lightest guest
        // - One pointer starts at the heaviest guest
        //
        // This is useful because the heaviest guest is the hardest person to pair.
        // If the heaviest guest cannot pair with the lightest guest, then the heaviest
        // guest cannot pair with anyone at all, because everyone else is heavier than
        // the lightest guest.
        //
        // That observation is the core reason the greedy approach is correct.
        Array.Sort(weights);

        // left points to the lightest guest not yet assigned to a ride.
        int left = 0;

        // right points to the heaviest guest not yet assigned to a ride.
        int right = weights.Length - 1;

        // This will count how many rides we use in total.
        int rides = 0;

        // Step 2:
        // Continue until all guests have been assigned.
        //
        // The condition left <= right means:
        // - If left < right, there are at least two guests remaining
        // - If left == right, there is exactly one guest remaining
        while (left <= right)
        {
            // Every iteration will use exactly one ride.
            //
            // Why?
            // Because we always place the heaviest remaining guest onto a ride now.
            // That ride may contain:
            // - the heaviest guest alone, or
            // - the heaviest guest paired with the lightest guest
            rides++;

            // Step 3:
            // Check whether the lightest remaining guest and the heaviest remaining guest
            // can share one ride without exceeding the weight limit.
            //
            // We cast to long before adding to be extra safe in general arithmetic,
            // even though the given constraints still fit inside int.
            long combinedWeight = (long)weights[left] + weights[right];

            if (combinedWeight <= limit)
            {
                // They can ride together.
                //
                // Why is this a good choice?
                // Pairing the heaviest guest with the lightest possible partner is greedy,
                // but optimal:
                // - We successfully avoid sending the heaviest guest alone
                // - We use up the smallest guest, preserving larger guests for later
                //
                // Since both guests are now assigned, move both pointers inward.
                left++;
                right--;
            }
            else
            {
                // They cannot ride together.
                //
                // This means the heaviest guest must ride alone.
                //
                // Why can we conclude that?
                // Because the array is sorted, and weights[left] is the lightest remaining guest.
                // If even the lightest guest is too heavy to pair with weights[right],
                // then any other remaining guest would be even heavier, so none of them
                // can pair with weights[right].
                //
                // Therefore, sending the heaviest guest alone is not just reasonable,
                // it is forced.
                right--;
            }
        }

        // After the loop finishes, every guest has been assigned to exactly one ride.
        // The rides counter is therefore the minimum number of rides needed.
        return rides;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] weights1 = { 70, 50, 80, 50 };
int limit1 = 100;
int result1 = solution.MinRides(weights1, limit1);
Console.WriteLine("Example 1:");
Console.WriteLine($"Weights: [{string.Join(", ", weights1)}], Limit: {limit1}");
Console.WriteLine($"Minimum rides needed: {result1}");
Console.WriteLine("Expected: 3");
Console.WriteLine();

// Example 2
int[] weights2 = { 40, 60, 55, 45, 80 };
int limit2 = 100;
int result2 = solution.MinRides(weights2, limit2);
Console.WriteLine("Example 2:");
Console.WriteLine($"Weights: [{string.Join(", ", weights2)}], Limit: {limit2}");
Console.WriteLine($"Minimum rides needed: {result2}");
Console.WriteLine("Expected: 3");
Console.WriteLine();

// Additional demo
int[] weights3 = { 30, 40, 50, 60 };
int limit3 = 90;
int result3 = solution.MinRides(weights3, limit3);
Console.WriteLine("Additional Demo:");
Console.WriteLine($"Weights: [{string.Join(", ", weights3)}], Limit: {limit3}");
Console.WriteLine($"Minimum rides needed: {result3}");
Console.WriteLine("One optimal pairing is (30, 60) and (40, 50), so expected: 2");