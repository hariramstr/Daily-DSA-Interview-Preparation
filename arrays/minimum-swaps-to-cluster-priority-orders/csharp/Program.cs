/*
Title: Minimum Swaps to Cluster Priority Orders

Problem Description:
A warehouse tracks outgoing orders in an array where each value is either 0 or 1.
A value of 1 represents a priority order, and 0 represents a regular order.

To speed up loading, the warehouse wants all priority orders to appear together
in one contiguous block somewhere in the array. The block does not need to be at
the beginning or the end; it can be placed anywhere as long as all 1s are grouped together.

In one operation, you may swap the values at any two different indices in the array.
Return the minimum number of swaps required to make all priority orders contiguous.

You are not asked to return the final arrangement, only the minimum number of swaps.

Key Insight:
- Let totalOnes be the total number of 1s in the array.
- If we want all 1s to end up together, then they must occupy some contiguous window
  of length totalOnes.
- Inside that chosen window, every 0 is a position that should contain a 1.
- Each such 0 can be fixed by swapping it with a 1 from outside the window.
- Therefore, for any window of length totalOnes, the number of swaps needed is exactly
  the number of 0s inside that window.
- So the problem becomes: among all windows of length totalOnes, find the one with
  the fewest 0s.

Constraints:
- 1 <= orders.length <= 100000
- orders[i] is either 0 or 1
- The answer fits in a 32-bit integer
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We first count how many 1s exist in the array.
    - Then we scan the array with a sliding window.
    - Each element is processed a constant number of times.

    Space Complexity: O(1)
    - We only use a few integer variables.
    - No extra array or collection proportional to input size is created.
    */
    public int MinSwaps(int[] orders)
    {
        // Step 1:
        // Count the total number of priority orders (1s) in the entire array.
        //
        // Why this is necessary:
        // If there are k ones total, then in the final arrangement all ones must occupy
        // exactly one contiguous block of length k.
        //
        // Example:
        // orders = [1,0,1,0,1]
        // totalOnes = 3
        // So we only need to examine windows of length 3.
        int totalOnes = 0;
        foreach (int value in orders)
        {
            if (value == 1)
            {
                totalOnes++;
            }
        }

        // Step 2:
        // Handle easy edge cases.
        //
        // Why this is necessary:
        // - If there are 0 ones, then there is nothing to group.
        // - If there is 1 one, then a single 1 is already contiguous by itself.
        //
        // In both cases, the answer is 0 swaps.
        if (totalOnes <= 1)
        {
            return 0;
        }

        // Step 3:
        // Build the first sliding window of length totalOnes.
        //
        // We will count how many zeros are inside the current window.
        // That count directly tells us how many swaps are needed if we choose
        // this window to be the final block containing all 1s.
        //
        // Why counting zeros works:
        // Every zero inside the chosen window is "wrong" and must be replaced by a 1
        // from outside the window. Since swaps can happen between any two indices,
        // each zero can be fixed with one swap.
        int zerosInWindow = 0;
        for (int i = 0; i < totalOnes; i++)
        {
            if (orders[i] == 0)
            {
                zerosInWindow++;
            }
        }

        // Step 4:
        // Initialize the answer using the first window.
        //
        // This is our best answer seen so far.
        int minSwaps = zerosInWindow;

        // Step 5:
        // Slide the window one position at a time across the array.
        //
        // Window size is fixed at totalOnes.
        // For each move:
        // - One element leaves the window from the left.
        // - One element enters the window from the right.
        //
        // Instead of recounting zeros from scratch for every window,
        // we update the zero count incrementally.
        //
        // This is the core sliding window optimization that keeps the algorithm O(n).
        for (int right = totalOnes; right < orders.Length; right++)
        {
            // The leftmost index of the previous window.
            int left = right - totalOnes;

            // Step 5a:
            // Remove the effect of the element leaving the window.
            //
            // If the outgoing element was 0, then the current window now contains
            // one fewer zero.
            if (orders[left] == 0)
            {
                zerosInWindow--;
            }

            // Step 5b:
            // Add the effect of the element entering the window.
            //
            // If the incoming element is 0, then the current window now contains
            // one more zero.
            if (orders[right] == 0)
            {
                zerosInWindow++;
            }

            // Step 5c:
            // Update the best answer.
            //
            // Why this is necessary:
            // We want the window with the fewest zeros, because that window requires
            // the fewest swaps to turn into a block of all 1s.
            if (zerosInWindow < minSwaps)
            {
                minSwaps = zerosInWindow;
            }
        }

        // Step 6:
        // Return the minimum number of zeros found in any valid window.
        //
        // That value is exactly the minimum number of swaps required.
        return minSwaps;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// orders = [1,0,1,0,1]
// totalOnes = 3
// Windows of length 3:
// [1,0,1] -> 1 zero
// [0,1,0] -> 2 zeros
// [1,0,1] -> 1 zero
// Minimum = 1
int[] orders1 = { 1, 0, 1, 0, 1 };
int result1 = solution.MinSwaps(orders1);
Console.WriteLine($"Input: [{string.Join(",", orders1)}]");
Console.WriteLine($"Minimum swaps: {result1}");
Console.WriteLine("Expected: 1");
Console.WriteLine();

// Example 2:
// orders = [0,0,1,0,1,1,0]
// totalOnes = 3
// Windows of length 3:
// [0,0,1] -> 2 zeros
// [0,1,0] -> 2 zeros
// [1,0,1] -> 1 zero
// [0,1,1] -> 1 zero
// [1,1,0] -> 1 zero
// Minimum = 1
int[] orders2 = { 0, 0, 1, 0, 1, 1, 0 };
int result2 = solution.MinSwaps(orders2);
Console.WriteLine($"Input: [{string.Join(",", orders2)}]");
Console.WriteLine($"Minimum swaps: {result2}");
Console.WriteLine("Expected: 1");
Console.WriteLine();

// Additional demo: no priority orders
int[] orders3 = { 0, 0, 0, 0 };
int result3 = solution.MinSwaps(orders3);
Console.WriteLine($"Input: [{string.Join(",", orders3)}]");
Console.WriteLine($"Minimum swaps: {result3}");
Console.WriteLine("Expected: 0");
Console.WriteLine();

// Additional demo: one priority order
int[] orders4 = { 0, 1, 0, 0 };
int result4 = solution.MinSwaps(orders4);
Console.WriteLine($"Input: [{string.Join(",", orders4)}]");
Console.WriteLine($"Minimum swaps: {result4}");
Console.WriteLine("Expected: 0");
Console.WriteLine();

// Additional demo: already contiguous
int[] orders5 = { 0, 1, 1, 1, 0 };
int result5 = solution.MinSwaps(orders5);
Console.WriteLine($"Input: [{string.Join(",", orders5)}]");
Console.WriteLine($"Minimum swaps: {result5}");
Console.WriteLine("Expected: 0");