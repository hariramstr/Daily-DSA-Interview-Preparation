/*
Title: Minimum Swaps to Group Delayed Flights

Problem Description:
An airport operations dashboard stores the status of flights in a binary array `flights`,
where `flights[i] = 1` means the `i`-th flight is delayed and `flights[i] = 0` means it is on time.

For reporting purposes, the airport wants all delayed flights to appear together in one contiguous
block in the array. You may swap the values at any two different indices, and each swap counts
as one operation.

Return the minimum number of swaps needed to group all delayed flights together.

If there are no delayed flights, or there is only one delayed flight, the answer is `0`
because they are already trivially grouped.

A useful way to think about the problem is that if there are `k` delayed flights in total,
then the final grouped block must have length `k`. For any candidate block of length `k`,
every on-time flight inside that block would need to be swapped with a delayed flight outside
the block. Your task is to find the best such block.

Constraints:
- 1 <= flights.length <= 100000
- flights[i] is either 0 or 1

Example 1:
Input: flights = [1,0,1,0,1]
Output: 1

Example 2:
Input: flights = [0,0,1,0,1,1,0]
Output: 1
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    We solve this with a sliding window.

    Key idea:
    - Let k be the total number of delayed flights (the total number of 1s).
    - In the final arrangement, all delayed flights must occupy one contiguous block of length k.
    - For any window of length k:
        - Every 0 inside that window is "wrong" and must be swapped out.
        - Each such 0 can be swapped with a 1 outside the window.
    - Therefore, the number of swaps needed for a given window is exactly the number of 0s inside it.
    - So we just need to find the window of length k that contains the fewest 0s.

    This is why sliding window is perfect:
    - We examine all windows of size k efficiently.
    - Instead of recounting zeros from scratch for every window, we update the count as the window moves.
    */
    public int MinSwaps(int[] flights)
    {
        // Step 1:
        // Count how many delayed flights exist in the entire array.
        //
        // Why this matters:
        // If there are k delayed flights total, then after grouping them together,
        // they must occupy a contiguous block of exactly length k.
        int delayedCount = 0;
        foreach (int flight in flights)
        {
            if (flight == 1)
            {
                delayedCount++;
            }
        }

        // Step 2:
        // Handle easy edge cases.
        //
        // Why this is necessary:
        // - If there are 0 delayed flights, there is nothing to group.
        // - If there is only 1 delayed flight, it is already trivially grouped.
        // In both cases, no swaps are needed.
        if (delayedCount <= 1)
        {
            return 0;
        }

        // Step 3:
        // Build the first sliding window of size delayedCount.
        //
        // We will count how many on-time flights (0s) are inside this window.
        // That count tells us how many swaps are needed if we choose this window
        // to be the final block containing all delayed flights.
        //
        // Data structure choice:
        // We do not need any extra array, queue, or list.
        // A few integer variables are enough because we only track:
        // - current number of zeros in the window
        // - best (minimum) number of zeros seen so far
        int zerosInWindow = 0;

        for (int i = 0; i < delayedCount; i++)
        {
            if (flights[i] == 0)
            {
                zerosInWindow++;
            }
        }

        // At this moment:
        // - zerosInWindow = number of 0s in the first window
        // - minSwaps starts as that value, because this is the best window seen so far
        int minSwaps = zerosInWindow;

        // Step 4:
        // Slide the window one position at a time across the array.
        //
        // For each move:
        // - One element leaves the window from the left side.
        // - One element enters the window from the right side.
        //
        // We update zerosInWindow accordingly instead of recounting the whole window.
        //
        // Why this is efficient:
        // Recounting each window from scratch would be O(n * k) in the worst case.
        // Sliding window lets us do the full scan in O(n).
        for (int right = delayedCount; right < flights.Length; right++)
        {
            // The left index of the previous window is:
            int left = right - delayedCount;

            // Step 4a:
            // Remove the effect of the element that is leaving the window.
            //
            // If the outgoing element was 0, then the new window has one fewer 0.
            if (flights[left] == 0)
            {
                zerosInWindow--;
            }

            // Step 4b:
            // Add the effect of the element that is entering the window.
            //
            // If the incoming element is 0, then the new window has one more 0.
            if (flights[right] == 0)
            {
                zerosInWindow++;
            }

            // Step 4c:
            // Update the best answer seen so far.
            //
            // Why this works:
            // The minimum number of swaps needed overall is the minimum number of 0s
            // in any window of size delayedCount.
            if (zerosInWindow < minSwaps)
            {
                minSwaps = zerosInWindow;
            }
        }

        // Step 5:
        // Return the best result found.
        //
        // This is the minimum number of swaps needed to group all delayed flights together.
        return minSwaps;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// flights = [1,0,1,0,1]
// Total delayed flights = 3, so window size = 3
// Windows:
// [1,0,1] -> 1 zero
// [0,1,0] -> 2 zeros
// [1,0,1] -> 1 zero
// Minimum = 1
int[] flights1 = { 1, 0, 1, 0, 1 };
int result1 = solution.MinSwaps(flights1);
Console.WriteLine($"Input: [{string.Join(",", flights1)}]");
Console.WriteLine($"Minimum swaps needed: {result1}");
Console.WriteLine("Expected: 1");
Console.WriteLine();

// Example 2:
// flights = [0,0,1,0,1,1,0]
// Total delayed flights = 3, so window size = 3
// Windows:
// [0,0,1] -> 2 zeros
// [0,1,0] -> 2 zeros
// [1,0,1] -> 1 zero
// [0,1,1] -> 1 zero
// [1,1,0] -> 1 zero
// Minimum = 1
int[] flights2 = { 0, 0, 1, 0, 1, 1, 0 };
int result2 = solution.MinSwaps(flights2);
Console.WriteLine($"Input: [{string.Join(",", flights2)}]");
Console.WriteLine($"Minimum swaps needed: {result2}");
Console.WriteLine("Expected: 1");
Console.WriteLine();

// Additional demo: no delayed flights
int[] flights3 = { 0, 0, 0, 0 };
int result3 = solution.MinSwaps(flights3);
Console.WriteLine($"Input: [{string.Join(",", flights3)}]");
Console.WriteLine($"Minimum swaps needed: {result3}");
Console.WriteLine("Expected: 0");
Console.WriteLine();

// Additional demo: one delayed flight
int[] flights4 = { 0, 0, 1, 0, 0 };
int result4 = solution.MinSwaps(flights4);
Console.WriteLine($"Input: [{string.Join(",", flights4)}]");
Console.WriteLine($"Minimum swaps needed: {result4}");
Console.WriteLine("Expected: 0");
Console.WriteLine();

// Additional demo: already grouped
int[] flights5 = { 0, 1, 1, 1, 0 };
int result5 = solution.MinSwaps(flights5);
Console.WriteLine($"Input: [{string.Join(",", flights5)}]");
Console.WriteLine($"Minimum swaps needed: {result5}");
Console.WriteLine("Expected: 0");