/*
Title: Visible Customers After Each Line Update
Difficulty: Medium
Topic: Stacks and Queues

Problem Description:
A store manager tracks the heights of customers standing in a single checkout line from front to back.
For staffing analysis, the manager wants to know, for each customer, how many customers in front of them
are visible. A customer can see another customer in front if every customer standing between them is
strictly shorter than both of those two customers. If a taller or equal-height customer appears first,
visibility stops there, but that blocking customer is still visible.

Given an array heights where heights[i] is the height of the i-th customer in line (0-indexed, from front
to back), return an array answer of the same length where answer[i] is the number of customers in front of
customer i that are visible to them.

You should design an efficient solution using stack-based processing rather than checking every pair directly.

Constraints:
- 1 <= heights.length <= 200000
- 1 <= heights[i] <= 1000000000
- The answer for each position fits in a 32-bit integer

Example 1:
Input: heights = [10,6,8,5,11,9]
Output: [0,1,2,1,4,1]

Example 2:
Input: heights = [5,5,4,7,6]
Output: [0,1,1,3,1]
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Why O(n)?
    - Each customer's height is pushed onto the stack exactly once.
    - Each customer's height is popped from the stack at most once.
    - So even though there is a while-loop, the total number of stack operations across the whole array is linear.

    Core idea:
    We process customers from front to back.
    For the current customer i, we want to know how many customers in front are visible.

    We maintain a stack of heights representing a "useful skyline" of customers in front.
    The stack is kept in non-increasing order from bottom to top after processing each customer.

    For a new customer with height h:
    1. While the top of the stack is strictly shorter than h:
       - That shorter customer is visible.
       - Also, that shorter customer can never block any future taller/equal comparisons for this customer,
         so we pop it.
    2. After removing all strictly shorter customers:
       - If the stack is not empty, then the first remaining customer is taller or equal.
       - That customer is also visible, and then visibility stops there.
    3. Push the current customer's height onto the stack so future customers behind can use it.

    This exactly matches the rule:
    - You can see all consecutive shorter customers in front,
    - and then possibly one first taller-or-equal customer that blocks further view.
    */
    public int[] VisibleCustomersInFront(int[] heights)
    {
        int n = heights.Length;
        int[] answer = new int[n];

        // This stack stores heights of customers that are in front of the current customer.
        // We only keep the heights that still matter for visibility calculations of future customers.
        //
        // Important intuition:
        // If a shorter customer is behind a taller customer in the stack "view structure",
        // then future customers may pop the shorter one when they are tall enough to see over it.
        // This is why a monotonic stack is a perfect fit here.
        Stack<int> stack = new Stack<int>();

        // We move from front to back.
        // That means when we are at index i, the stack already contains information
        // about all customers standing in front of i.
        for (int i = 0; i < n; i++)
        {
            int currentHeight = heights[i];
            int visibleCount = 0;

            // Step 1:
            // Remove and count every strictly shorter customer on top of the stack.
            //
            // Why are these customers visible?
            // Because they are the nearest remaining customers in front after previous reductions,
            // and each one is shorter than the current customer.
            //
            // Why can we pop them?
            // Because once the current customer can see over a shorter customer,
            // that shorter customer will never be useful as a blocker for any future customer behind.
            // Future customers care more about the taller/equal customers behind that shorter one.
            while (stack.Count > 0 && stack.Peek() < currentHeight)
            {
                stack.Pop();
                visibleCount++;
            }

            // Step 2:
            // If there is still someone left on the stack, that means the nearest remaining
            // customer in front is taller than or equal to the current customer.
            //
            // That person is still visible:
            // - They are the first blocking customer.
            // - The problem statement says the blocking customer is visible.
            //
            // But visibility stops there, so we count exactly one more and do not continue.
            if (stack.Count > 0)
            {
                visibleCount++;
            }

            // Store the computed answer for this customer.
            answer[i] = visibleCount;

            // Step 3:
            // Push the current customer onto the stack so customers behind can evaluate visibility.
            //
            // This customer may become:
            // - a shorter visible customer that gets popped by a taller future customer, or
            // - a blocking taller/equal customer for a shorter future customer.
            stack.Push(currentHeight);
        }

        return answer;
    }
}

// Demo code

var solution = new Solution();

int[] heights1 = { 10, 6, 8, 5, 11, 9 };
int[] result1 = solution.VisibleCustomersInFront(heights1);
Console.WriteLine("Input:  [" + string.Join(", ", heights1) + "]");
Console.WriteLine("Output: [" + string.Join(", ", result1) + "]");
Console.WriteLine("Expected: [0, 1, 2, 1, 4, 1]");
Console.WriteLine();

int[] heights2 = { 5, 5, 4, 7, 6 };
int[] result2 = solution.VisibleCustomersInFront(heights2);
Console.WriteLine("Input:  [" + string.Join(", ", heights2) + "]");
Console.WriteLine("Output: [" + string.Join(", ", result2) + "]");
Console.WriteLine("Expected: [0, 1, 1, 3, 1]");
Console.WriteLine();

int[] heights3 = { 3 };
int[] result3 = solution.VisibleCustomersInFront(heights3);
Console.WriteLine("Input:  [" + string.Join(", ", heights3) + "]");
Console.WriteLine("Output: [" + string.Join(", ", result3) + "]");
Console.WriteLine("Expected: [0]");
Console.WriteLine();

int[] heights4 = { 2, 1, 2, 4, 3 };
int[] result4 = solution.VisibleCustomersInFront(heights4);
Console.WriteLine("Input:  [" + string.Join(", ", heights4) + "]");
Console.WriteLine("Output: [" + string.Join(", ", result4) + "]");