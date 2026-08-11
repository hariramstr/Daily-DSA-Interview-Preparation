/*
Title: Find the First Repeated Cart Item

Problem Description:
You are given an integer array items where each value represents the product ID of an item scanned
into an online shopping cart, in the exact order the scans happened.

Your task is to return the first product ID that appears more than once while scanning from left to right.
In other words, as you read the array from the beginning, return the first item whose current scan is a
repeat of an item seen earlier.

If no product ID is repeated, return -1.

This problem models a common event-processing task: detecting the earliest duplicate action in a stream.
The answer is not necessarily the smallest repeated value, and it is not the value with the highest frequency.
It is specifically the value whose second appearance happens earliest in the array.

Constraints:
- 1 <= items.length <= 100000
- 1 <= items[i] <= 1000000000

Example 1:
Input: items = [42, 17, 9, 17, 42]
Output: 17

Explanation:
While scanning left to right:
- 42 is seen for the first time
- 17 is seen for the first time
- 9 is seen for the first time
- 17 is seen again, so this is the first repeated item encountered
Therefore, the answer is 17.

Example 2:
Input: items = [5, 8, 3, 1]
Output: -1

Explanation:
Every item appears exactly once, so there is no repeated cart item.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan through the array exactly once.
    - Each HashSet operation (Contains / Add) is O(1) on average.

    Space Complexity: O(n)
    - In the worst case, no values repeat, so we store every item in the HashSet.
    */
    public int FirstRepeatedCartItem(int[] items)
    {
        // We use a HashSet<int> to remember which product IDs we have already seen.
        //
        // Why a HashSet?
        // - We need to answer this question very quickly for each item:
        //   "Have I seen this value before?"
        // - A HashSet is designed exactly for fast membership checks.
        // - Contains(...) is O(1) on average, which keeps the whole solution efficient.
        //
        // This is much better than using a List and searching linearly each time,
        // because that would make the solution too slow for large inputs.
        var seen = new HashSet<int>();

        // We now scan the array from left to right, exactly in the order the items were scanned.
        //
        // This order is extremely important:
        // - The problem does NOT ask for the smallest repeated value.
        // - The problem does NOT ask for the most frequent value.
        // - The problem asks for the FIRST value whose current appearance is a repeat.
        //
        // So we must process the array in its original order.
        for (int i = 0; i < items.Length; i++)
        {
            // Read the current product ID from the array.
            int currentItem = items[i];

            // Step 1: Check whether this item has already been seen earlier.
            //
            // Why do we check before adding?
            // - If the item is already in the set, that means this is at least its second appearance.
            // - Since we are scanning from left to right, the first time this happens for any value
            //   is exactly the answer we need to return.
            if (seen.Contains(currentItem))
            {
                // We found the earliest repeated item.
                //
                // Example trace for [42, 17, 9, 17, 42]:
                // - i=0, currentItem=42 -> not seen, add it
                // - i=1, currentItem=17 -> not seen, add it
                // - i=2, currentItem=9  -> not seen, add it
                // - i=3, currentItem=17 -> already seen, return 17
                //
                // Notice that even though 42 also repeats later, 17 is returned because
                // its second appearance happens earlier.
                return currentItem;
            }

            // Step 2: If the item was not seen before, record it now.
            //
            // Why is this necessary?
            // - Future elements need to know that this value has already appeared.
            // - By adding it to the HashSet now, any later occurrence can be detected as a repeat.
            seen.Add(currentItem);
        }

        // If we finish the entire loop without returning, then no item ever appeared twice.
        //
        // Example trace for [5, 8, 3, 1]:
        // - 5 not seen -> add
        // - 8 not seen -> add
        // - 3 not seen -> add
        // - 1 not seen -> add
        // End of array reached, so there is no repeated item.
        //
        // The required return value in that case is -1.
        return -1;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the problem description
int[] items1 = { 42, 17, 9, 17, 42 };
int result1 = solution.FirstRepeatedCartItem(items1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 17

// Example 2 from the problem description
int[] items2 = { 5, 8, 3, 1 };
int result2 = solution.FirstRepeatedCartItem(items2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: -1

// Additional demo cases

// The first repeated item is 2 because its second appearance happens before 1's second appearance.
int[] items3 = { 1, 2, 3, 2, 1 };
int result3 = solution.FirstRepeatedCartItem(items3);
Console.WriteLine("Example 3 Result: " + result3); // Expected: 2

// Immediate repetition
int[] items4 = { 7, 7, 9, 10 };
int result4 = solution.FirstRepeatedCartItem(items4);
Console.WriteLine("Example 4 Result: " + result4); // Expected: 7

// Single element, so no repetition is possible
int[] items5 = { 99 };
int result5 = solution.FirstRepeatedCartItem(items5);
Console.WriteLine("Example 5 Result: " + result5); // Expected: -1