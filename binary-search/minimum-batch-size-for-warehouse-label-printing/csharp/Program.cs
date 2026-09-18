/*
Title: Minimum Batch Size for Warehouse Label Printing

Problem Description:
A warehouse prints shipping labels for incoming orders using a single printer.
The orders must be processed in the given order, and each order has a required
number of labels.

The warehouse operates for exactly d shifts, and during each shift the printer
can print labels for a contiguous group of orders.

Important rules:
1. Orders must stay in the original order.
2. Each shift handles a contiguous block of orders.
3. An order cannot be split across two shifts.
4. The total labels printed in one shift cannot exceed the chosen batch size limit.

Goal:
Find the minimum batch size limit that allows all orders to be completed within
at most d shifts.

This is the classic "split array largest sum" problem and is well-suited for
binary search on the answer:
- If a batch size X is enough, then any larger batch size is also enough.
- That monotonic property lets us binary search for the smallest feasible X.

Examples:
1) labels = [8, 5, 3, 7, 6], d = 3
   Correct answer: 13
   One valid partition is [8,5], [3,7], [6]
   Group sums are 13, 10, 6, so the maximum is 13.

2) labels = [10, 2, 4, 9, 3], d = 2
   Correct answer: 16
   One valid partition is [10,2,4], [9,3]
   Group sums are 16 and 12, so the maximum is 16.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * log(S))
      where:
      n = number of orders
      S = sum(labels) - max(labels) + 1, which is the binary search range size

    Why:
    - Each binary search step checks feasibility by scanning the array once: O(n)
    - The number of binary search steps is O(log(S))

    Space Complexity:
    - O(1)
      We only use a few variables and do not allocate extra arrays proportional to input size.
    */
    public long MinimumBatchSize(int[] labels, int d)
    {
        // Step 1:
        // Establish the binary search boundaries.
        //
        // Why these boundaries?
        //
        // Lower bound:
        // The batch size can never be smaller than the largest single order,
        // because an order cannot be split across shifts.
        //
        // Upper bound:
        // The batch size can always be as large as the total sum of all labels,
        // which would allow all orders to be printed in one shift.
        //
        // We use long instead of int because:
        // - labels[i] can be up to 1,000,000,000
        // - there can be up to 100,000 orders
        // - the total sum can therefore exceed the int range
        long left = 0;
        long right = 0;

        foreach (int label in labels)
        {
            // Update the lower bound to be the maximum single order size.
            if (label > left)
            {
                left = label;
            }

            // Add to the total sum for the upper bound.
            right += label;
        }

        // Step 2:
        // Perform binary search on the answer.
        //
        // Invariant:
        // - Any value < left is known to be too small or not yet considered.
        // - Any value > right is unnecessary.
        // - We search for the smallest feasible batch size.
        while (left < right)
        {
            // Compute the middle carefully to avoid overflow.
            long mid = left + (right - left) / 2;

            // Step 3:
            // Check whether this candidate batch size "mid" is sufficient.
            //
            // If it is sufficient, we try smaller values to see if we can do better.
            // If it is not sufficient, we must increase the batch size.
            if (CanFinishWithinShifts(labels, d, mid))
            {
                // mid works, so the answer is at most mid.
                right = mid;
            }
            else
            {
                // mid does not work, so the answer must be larger than mid.
                left = mid + 1;
            }
        }

        // When left == right, we have found the minimum feasible batch size.
        return left;
    }

    private bool CanFinishWithinShifts(int[] labels, int d, long batchSize)
    {
        // This helper answers:
        // "If the printer batch size limit is batchSize,
        //  can we process all orders in at most d shifts?"
        //
        // Greedy strategy:
        // We scan from left to right and keep adding orders to the current shift
        // as long as the total does not exceed batchSize.
        //
        // Why greedy is correct here:
        // To minimize the number of shifts used for a fixed batch size,
        // we should pack each shift as much as possible before starting a new one.
        // Starting a new shift earlier would never help reduce the number of shifts.
        //
        // Data structure choice:
        // We do not need any extra data structure.
        // A simple running sum and a shift counter are enough.

        // We start with one shift because if there is at least one order,
        // we need at least one shift to process it.
        int shiftsUsed = 1;

        // Running total of labels assigned to the current shift.
        long currentShiftSum = 0;

        foreach (int label in labels)
        {
            // Safety check:
            // If a single order is larger than the batch size,
            // then this batch size is immediately impossible.
            //
            // In practice, our binary search lower bound already prevents this,
            // but keeping this check makes the helper self-contained and easier to understand.
            if (label > batchSize)
            {
                return false;
            }

            // Try to place the current order into the current shift.
            if (currentShiftSum + label <= batchSize)
            {
                // It fits, so we keep extending the current contiguous group.
                currentShiftSum += label;
            }
            else
            {
                // It does not fit, so we must start a new shift.
                shiftsUsed++;
                currentShiftSum = label;

                // Early exit:
                // If we already need more than d shifts, this batch size is not feasible.
                if (shiftsUsed > d)
                {
                    return false;
                }
            }
        }

        // If we finished scanning all orders using at most d shifts,
        // then this batch size is feasible.
        return true;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] labels1 = { 8, 5, 3, 7, 6 };
int d1 = 3;
long result1 = solution.MinimumBatchSize(labels1, d1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 13

// Example 2
int[] labels2 = { 10, 2, 4, 9, 3 };
int d2 = 2;
long result2 = solution.MinimumBatchSize(labels2, d2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 16

// Additional quick checks

int[] labels3 = { 1, 2, 3, 4, 5 };
int d3 = 2;
long result3 = solution.MinimumBatchSize(labels3, d3);
Console.WriteLine($"Additional Check 1 Result: {result3}"); // Expected: 9 -> [1,2,3] and [4,5]

int[] labels4 = { 7 };
int d4 = 1;
long result4 = solution.MinimumBatchSize(labels4, d4);
Console.WriteLine($"Additional Check 2 Result: {result4}"); // Expected: 7