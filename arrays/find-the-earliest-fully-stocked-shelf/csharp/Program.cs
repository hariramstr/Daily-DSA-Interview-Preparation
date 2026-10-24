/*
Title: Find the Earliest Fully Stocked Shelf

Problem Description:
A store receives a delivery log for a single shelf. The shelf has n slots, and each slot should eventually contain exactly one product.
You are given an array deliveries of length n, where deliveries[i] is the slot number filled on minute i + 1.
Each slot number from 1 to n appears exactly once.

Return the earliest minute when the shelf becomes fully stocked from left to right without any gaps,
meaning slots 1 through k are all filled for some k, and k is as large as possible at that minute.
In this problem, you must return the earliest minute when the filled slots form the prefix 1 through m,
where m is the maximum slot number filled so far and all smaller slot numbers are also already filled.

Equivalently, after processing minute t, look at the largest slot number seen so far.
If every slot from 1 up to that number has been filled, then the shelf is fully stocked up to that point.
Return the first minute when this happens. If it only happens after the final delivery, return n.

Constraints:
- 1 <= n <= 100000
- deliveries.length == n
- 1 <= deliveries[i] <= n
- All values in deliveries are distinct

Example 1:
Input: deliveries = [2,1,3,5,4]
Output: 2
Explanation:
After minute 1, only slot 2 is filled, so there is a gap at slot 1.
After minute 2, slots 1 and 2 are both filled, and the maximum filled slot is 2,
so the shelf is fully stocked from 1 to 2.

Example 2:
Input: deliveries = [3,1,2,4]
Output: 3
Explanation:
After minute 1, slot 3 is filled, but slots 1 and 2 are missing.
After minute 2, slots 1 and 3 are filled, but slot 2 is still missing.
After minute 3, slots 1, 2, and 3 are filled, which matches the maximum filled slot,
so the answer is 3.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Why O(n)?
    - We scan through the deliveries array exactly once.
    - Each step does only constant-time work:
      updating the maximum slot number seen so far,
      comparing it with the current minute count,
      and possibly returning the answer.

    Why O(1) extra space?
    - We do not need any extra array, hash set, or other data structure.
    - We only store a few integer variables.

    Key Idea:
    Because every slot number from 1 to n appears exactly once, after t minutes we have filled exactly t distinct slots.

    Let:
    - maxSeen = the largest slot number delivered so far
    - t = number of minutes processed so far

    If the filled slots are exactly the prefix 1 through maxSeen with no gaps,
    then the number of filled slots must be maxSeen.

    But after t minutes, the number of filled slots is exactly t.

    Therefore, the shelf is fully stocked from 1 to maxSeen exactly when:
        maxSeen == t

    This works because:
    - We have t distinct filled slots.
    - If the largest filled slot is maxSeen
    - And t == maxSeen
    - Then those t distinct slots must be exactly {1, 2, ..., maxSeen}
      with no missing values.

    Example 1 trace: [2,1,3,5,4]
    - Minute 1: maxSeen = 2, t = 1 -> not equal
    - Minute 2: maxSeen = 2, t = 2 -> equal, answer = 2

    Example 2 trace: [3,1,2,4]
    - Minute 1: maxSeen = 3, t = 1 -> not equal
    - Minute 2: maxSeen = 3, t = 2 -> not equal
    - Minute 3: maxSeen = 3, t = 3 -> equal, answer = 3
    */
    public int EarliestFullyStockedShelf(int[] deliveries)
    {
        // This variable keeps track of the largest slot number we have seen so far.
        // We need this because the problem asks us to check whether all slots from 1
        // up to the current maximum filled slot are present with no gaps.
        int maxSeen = 0;

        // We process deliveries minute by minute.
        // Index i corresponds to minute i + 1 because arrays are 0-based,
        // but the problem statement counts minutes starting from 1.
        for (int i = 0; i < deliveries.Length; i++)
        {
            // Current delivered slot at this minute.
            int currentSlot = deliveries[i];

            // Update the largest slot number seen so far.
            // This is necessary because the "prefix" we are checking always ends
            // at the maximum slot number delivered up to the current minute.
            if (currentSlot > maxSeen)
            {
                maxSeen = currentSlot;
            }

            // Number of minutes processed so far.
            // Since one delivery happens each minute, this is also the number of
            // distinct filled slots so far.
            int minutesSoFar = i + 1;

            // This is the crucial observation:
            // If maxSeen == minutesSoFar, then the set of filled slots must be exactly
            // 1 through maxSeen with no gaps.
            //
            // Why?
            // - We have exactly "minutesSoFar" distinct filled slots.
            // - The largest of them is "maxSeen".
            // - If minutesSoFar equals maxSeen, then there is no room for gaps.
            //   The only possible distinct slot numbers are 1, 2, ..., maxSeen.
            //
            // So this is the earliest minute when the shelf is fully stocked
            // from left to right up to the current maximum slot.
            if (maxSeen == minutesSoFar)
            {
                return minutesSoFar;
            }
        }

        // According to the problem, if this condition does not happen earlier,
        // it will definitely happen by the final delivery.
        // At the end, all slots 1 through n are filled, so returning n is correct.
        return deliveries.Length;
    }
}

// Demo code

var solution = new Solution();

// Sample input 1
int[] deliveries1 = { 2, 1, 3, 5, 4 };
int result1 = solution.EarliestFullyStockedShelf(deliveries1);
Console.WriteLine("Input: [2,1,3,5,4]");
Console.WriteLine("Earliest fully stocked minute: " + result1);
Console.WriteLine("Expected: 2");
Console.WriteLine();

// Sample input 2
int[] deliveries2 = { 3, 1, 2, 4 };
int result2 = solution.EarliestFullyStockedShelf(deliveries2);
Console.WriteLine("Input: [3,1,2,4]");
Console.WriteLine("Earliest fully stocked minute: " + result2);
Console.WriteLine("Expected: 3");
Console.WriteLine();

// Additional demo input
int[] deliveries3 = { 1, 2, 3, 4, 5 };
int result3 = solution.EarliestFullyStockedShelf(deliveries3);
Console.WriteLine("Input: [1,2,3,4,5]");
Console.WriteLine("Earliest fully stocked minute: " + result3);
Console.WriteLine("Expected: 1");