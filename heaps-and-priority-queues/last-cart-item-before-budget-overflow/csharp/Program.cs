/*
Title: Last Cart Item Before Budget Overflow

Problem Description:
You are building a shopping app that processes item prices one by one in the order they were scanned.
A customer has a fixed budget, but the app is allowed to remove previously scanned expensive items
if that helps keep the current cart affordable.

After each new item is scanned, the cart should contain as many of the scanned items as possible
while keeping the total cost less than or equal to the budget.

If removing items is necessary, always remove the most expensive item currently in the cart first.

Your task is to return the number of items left in the cart after all prices have been processed.

This problem is naturally solved with a heap or priority queue:
- keep track of the items currently in the cart
- when the total cost exceeds the budget, repeatedly remove the highest-priced item
  until the cart is valid again

Example 1:
prices = [4, 2, 7, 1, 3], budget = 10
Output: 3

Trace:
- Add 4 -> total = 4
- Add 2 -> total = 6
- Add 7 -> total = 13, too large, remove most expensive item 7 -> total = 6
- Add 1 -> total = 7
- Add 3 -> total = 10
Final cart size = 4? Let's check carefully:
After removing 7, cart is [4, 2]
Then add 1 => [4, 2, 1]
Then add 3 => [4, 2, 1, 3], total = 10
So final cart size is actually 4 for this exact process.

However, the prompt's stated output says 3 while its own arithmetic leads to 4.
The algorithm required by the prompt is unambiguous:
keep all possible items while total <= budget, removing the largest when needed.
That produces 4 for Example 1.

Example 2:
prices = [8, 5, 2, 6], budget = 9
Trace:
- Add 8 -> total = 8
- Add 5 -> total = 13, remove 8 -> total = 5
- Add 2 -> total = 7
- Add 6 -> total = 13, remove 6 -> total = 7
Final cart = [5, 2], size = 2

Constraints:
- 1 <= prices.length <= 100000
- 1 <= prices[i] <= 1000000000
- 1 <= budget <= 1000000000
- The answer fits in a 32-bit integer.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n log n)
    - We process each price once.
    - Each insertion into the priority queue costs O(log n).
    - Each removal of the most expensive item also costs O(log n).
    - Across the whole algorithm, each item can be inserted once and removed at most once.

    Space Complexity:
    O(n)
    - In the worst case, the priority queue may store all currently kept items.
    */
    public int LastCartItemBeforeBudgetOverflow(int[] prices, int budget)
    {
        // We need to quickly remove the most expensive item whenever the running total
        // becomes larger than the allowed budget.
        //
        // In C#, PriorityQueue<TElement, TPriority> is a min-heap by default:
        // the smallest priority value comes out first.
        //
        // But our problem needs the LARGEST price to come out first.
        // A simple trick is to store each price with priority = -price.
        // Then:
        // - larger price => smaller negative number
        // - the queue will return the largest original price first
        //
        // Example:
        // price 7 => priority -7
        // price 2 => priority -2
        // Since -7 is smaller than -2, item 7 is removed first.
        var maxHeap = new PriorityQueue<int, int>();

        // We use long for the running total because prices can be large
        // and there can be many of them. Even though budget fits in int,
        // the intermediate sum can exceed int range during processing.
        long currentTotal = 0;

        // Process items in the exact order they were scanned.
        // This matches the problem statement.
        foreach (int price in prices)
        {
            // STEP 1: Add the newly scanned item into the cart.
            //
            // Why?
            // The problem says items are processed one by one, and after each scan
            // we should decide what the cart should contain.
            //
            // So the natural flow is:
            // - first include the new item
            // - then, if the cart is too expensive, remove items as needed
            maxHeap.Enqueue(price, -price);

            // STEP 2: Update the running total to reflect that the new item
            // is currently included in the cart.
            currentTotal += price;

            // STEP 3: If the cart is now over budget, repeatedly remove the
            // most expensive item currently in the cart.
            //
            // Why repeatedly?
            // Because one removal might not be enough if the budget is exceeded by a lot.
            //
            // Why remove the most expensive item first?
            // Because the problem explicitly requires that rule.
            // Also, removing the largest item reduces the total as much as possible
            // with each removal.
            while (currentTotal > budget)
            {
                // Remove the highest-priced item from the cart.
                int removedPrice = maxHeap.Dequeue();

                // Since that item is no longer in the cart, subtract it from the total.
                currentTotal -= removedPrice;
            }

            // At this point, after processing the current scanned item,
            // the cart is guaranteed to be valid:
            // currentTotal <= budget
            //
            // Also, because we only removed items when necessary and always removed
            // according to the required rule, the heap now represents the final
            // cart state after this scan.
        }

        // The number of items left in the cart is simply how many items remain
        // inside the heap after all prices have been processed.
        return maxHeap.Count;
    }
}

// Demo code

var solution = new Solution();

// Sample input 1 from the prompt
int[] prices1 = { 4, 2, 7, 1, 3 };
int budget1 = 10;
int result1 = solution.LastCartItemBeforeBudgetOverflow(prices1, budget1);
Console.WriteLine("Example 1 Result: " + result1);

// Sample input 2 from the prompt
int[] prices2 = { 8, 5, 2, 6 };
int budget2 = 9;
int result2 = solution.LastCartItemBeforeBudgetOverflow(prices2, budget2);
Console.WriteLine("Example 2 Result: " + result2);

// Additional small demo
int[] prices3 = { 5, 5, 5 };
int budget3 = 10;
int result3 = solution.LastCartItemBeforeBudgetOverflow(prices3, budget3);
Console.WriteLine("Additional Demo Result: " + result3);