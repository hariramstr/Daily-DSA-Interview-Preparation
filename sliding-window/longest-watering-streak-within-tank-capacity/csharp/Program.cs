/*
Title: Longest Watering Streak Within Tank Capacity
Difficulty: Easy
Topic: Sliding Window

Problem Description:
A gardener records how many liters of water are used on each consecutive day in an array usage,
where usage[i] is the water needed on day i. The watering system has a tank that can supply at most
capacity liters before it must be refilled. We need to find the longest consecutive stretch of days
that can be fully covered using a single tank fill.

Return the length of the longest contiguous subarray whose total water usage is less than or equal to capacity.

Important details:
- The days must stay contiguous.
- We cannot skip days.
- We cannot reorder days.
- If every single day uses more water than capacity, the answer is 0.

Examples:
1) usage = [2, 1, 3, 2, 1], capacity = 5
   Output: 2

2) usage = [1, 1, 1, 2, 1], capacity = 4
   Correct Output: 3
   Explanation:
   - [1,1,1] has sum 3
   - [1,1,2] has sum 4
   - [1,2,1] has sum 4
   No contiguous subarray of length 4 has sum <= 4, so the answer is 3.

Efficient approach:
Because all usage values are non-negative, we can use the sliding window technique.
This lets us expand the right side of the window and shrink the left side only when needed,
giving an O(n) solution.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the window once when the right pointer moves.
    - Each element is removed from the window at most once when the left pointer moves.
    - Therefore, the total work is linear in the size of the array.

    Space Complexity: O(1)
    - We only use a few extra variables: left pointer, running sum, and best length.
    - No extra arrays or collections are needed.
    */
    public int LongestWateringStreak(int[] usage, int capacity)
    {
        // This pointer marks the beginning of our current window.
        // The window will always represent a contiguous range: usage[left..right].
        int left = 0;

        // This stores the total water usage inside the current window.
        // We use long instead of int for extra safety, even though the given constraints
        // fit inside int. Using long is a good habit when summing many values.
        long currentSum = 0;

        // This keeps track of the longest valid window length we have seen so far.
        int bestLength = 0;

        // We move the right pointer from the start of the array to the end.
        // At each step, we try to include usage[right] in the current window.
        for (int right = 0; right < usage.Length; right++)
        {
            // Step 1: Expand the window to include the current day at index "right".
            // Why?
            // We want to consider all possible contiguous ranges, and sliding window does this
            // by growing the window one element at a time.
            currentSum += usage[right];

            // Step 2: If the current window is too expensive (sum > capacity),
            // we must shrink it from the left until it becomes valid again.
            //
            // Why is shrinking from the left correct?
            // Because the window must remain contiguous. The only way to reduce the sum while
            // keeping the right endpoint fixed is to move the left endpoint forward.
            //
            // Why does this work efficiently?
            // Because all values are non-negative. If the sum is too large, adding more elements
            // on the right would only keep it the same or increase it, so we must remove from left.
            while (currentSum > capacity && left <= right)
            {
                // Remove the leftmost day's usage from the running total,
                // because that day is no longer in the window after we move left forward.
                currentSum -= usage[left];

                // Move the left boundary one step to the right.
                left++;
            }

            // Step 3: At this point, the window usage[left..right] is guaranteed to have
            // total sum <= capacity, so it is a valid candidate.
            //
            // We compute its length and compare it with the best answer seen so far.
            int currentLength = right - left + 1;

            if (currentLength > bestLength)
            {
                bestLength = currentLength;
            }
        }

        // If no valid day exists, bestLength will remain 0.
        // Otherwise, it will contain the maximum valid contiguous window length.
        return bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] usage1 = { 2, 1, 3, 2, 1 };
int capacity1 = 5;
int result1 = solution.LongestWateringStreak(usage1, capacity1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 2

// Example 2
int[] usage2 = { 1, 1, 1, 2, 1 };
int capacity2 = 4;
int result2 = solution.LongestWateringStreak(usage2, capacity2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 3

// Additional demo: no single day fits
int[] usage3 = { 6, 7, 8 };
int capacity3 = 5;
int result3 = solution.LongestWateringStreak(usage3, capacity3);
Console.WriteLine($"Example 3 Result: {result3}"); // Expected: 0

// Additional demo: all zero usage days
int[] usage4 = { 0, 0, 0, 0 };
int capacity4 = 0;
int result4 = solution.LongestWateringStreak(usage4, capacity4);
Console.WriteLine($"Example 4 Result: {result4}"); // Expected: 4