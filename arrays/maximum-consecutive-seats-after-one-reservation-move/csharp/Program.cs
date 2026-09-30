/*
Title: Maximum Consecutive Seats After One Reservation Move

Problem Description:
A theater keeps a row of seats represented by a binary array `seats`, where `seats[i] = 1`
means seat `i` is currently reserved and `seats[i] = 0` means it is empty.

To improve group seating, the manager is allowed to perform at most one reservation move:
choose one reserved seat and move that reservation to any empty seat.

After the move, the total number of reserved seats stays the same.

Your task is to return the maximum possible length of a consecutive block of reserved seats
that can be formed.

You may also choose not to move any reservation if the current arrangement is already optimal.
The move is not a swap: one `1` is removed from its current position and placed into one `0`
position. This means a move can connect two separated reserved blocks only if there is a gap
structure that makes it beneficial, and the answer depends on whether there is at least one
extra reserved seat elsewhere to relocate.

Return the largest number of consecutive `1`s obtainable after at most one move.

Constraints:
- 1 <= seats.length <= 2 * 10^5
- seats[i] is either 0 or 1
- The solution should run in linear time or close to it
*/

using System;

public class Solution
{
    /*
        Time Complexity: O(n)
        Space Complexity: O(n)

        Beginner-friendly idea:
        -----------------------
        This problem is the same core idea as:
        "What is the longest block of 1s we can get after moving at most one existing 1?"

        A move means:
        - We must take one existing 1 from somewhere
        - Put it into one 0 somewhere else

        So if we look at a zero position i:
        - Let left[i]  = number of consecutive 1s immediately to the left of i
        - Let right[i] = number of consecutive 1s immediately to the right of i

        If we fill this zero, we can create a block of:
            left[i] + 1 + right[i]

        BUT there is an important restriction:
        - The new 1 must come from some existing 1
        - If all 1s in the whole array are already exactly those left[i] + right[i] ones around this zero,
          then there is no "extra" 1 elsewhere to move in without breaking that same merged area.
        - In that case, the best we can do is only totalOnes, not left+1+right if that exceeds totalOnes.

        Therefore for each zero:
            candidate = min(totalOnes, left[i] + 1 + right[i])

        Also, if the array has no zero at all, answer is simply totalOnes.
    */
    public int MaxConsecutiveSeatsAfterOneMove(int[] seats)
    {
        int n = seats.Length;

        // Count how many reserved seats (1s) exist in total.
        // This is extremely important because after one move, the total number of 1s never changes.
        // So no answer can ever be larger than totalOnes.
        int totalOnes = 0;
        for (int i = 0; i < n; i++)
        {
            if (seats[i] == 1)
            {
                totalOnes++;
            }
        }

        // Edge case:
        // If there are no reserved seats at all, we cannot create any block of 1s,
        // because a move requires taking an existing 1 and there is none.
        if (totalOnes == 0)
        {
            return 0;
        }

        // left[i] will store:
        // "How many consecutive 1s end exactly at index i?"
        //
        // Example:
        // seats = [1,1,0,1,1,1]
        // left  = [1,2,0,1,2,3]
        //
        // Why build this?
        // For a zero at position i, the consecutive 1s immediately to its left are left[i - 1].
        int[] left = new int[n];
        if (seats[0] == 1)
        {
            left[0] = 1;
        }

        for (int i = 1; i < n; i++)
        {
            if (seats[i] == 1)
            {
                // If current seat is reserved, extend the consecutive run from the previous position.
                left[i] = left[i - 1] + 1;
            }
            else
            {
                // If current seat is empty, a run of 1s cannot end here.
                left[i] = 0;
            }
        }

        // right[i] will store:
        // "How many consecutive 1s start exactly at index i?"
        //
        // Example:
        // seats = [1,1,0,1,1,1]
        // right = [2,1,0,3,2,1]
        //
        // Why build this?
        // For a zero at position i, the consecutive 1s immediately to its right are right[i + 1].
        int[] right = new int[n];
        if (seats[n - 1] == 1)
        {
            right[n - 1] = 1;
        }

        for (int i = n - 2; i >= 0; i--)
        {
            if (seats[i] == 1)
            {
                // If current seat is reserved, extend the consecutive run from the next position.
                right[i] = right[i + 1] + 1;
            }
            else
            {
                // If current seat is empty, a run of 1s cannot start here.
                right[i] = 0;
            }
        }

        // We will compute the best answer.
        // Start with 0 and improve it.
        int answer = 0;

        // It is always legal to choose not to move.
        // So the current longest existing block of 1s should also be considered.
        //
        // We can get that from the left array because left[i] is the length of the run ending at i.
        for (int i = 0; i < n; i++)
        {
            if (left[i] > answer)
            {
                answer = left[i];
            }
        }

        // Now examine every zero as a possible place where we move a reservation into.
        for (int i = 0; i < n; i++)
        {
            if (seats[i] == 0)
            {
                // Count consecutive 1s directly touching this zero from the left side.
                int leftOnes = (i > 0) ? left[i - 1] : 0;

                // Count consecutive 1s directly touching this zero from the right side.
                int rightOnes = (i + 1 < n) ? right[i + 1] : 0;

                // If we fill this zero with a moved reservation, the ideal merged block size is:
                // left block + this filled seat + right block
                int mergedIfFilled = leftOnes + 1 + rightOnes;

                // However, we cannot exceed totalOnes because we are only moving a 1, not creating a new one.
                //
                // This single line correctly handles the subtle case:
                // - If there is an extra 1 somewhere else, we can truly get left+1+right.
                // - If there is no extra 1 elsewhere, then left+right already uses all 1s,
                //   so the best possible block is only totalOnes.
                int candidate = Math.Min(totalOnes, mergedIfFilled);

                if (candidate > answer)
                {
                    answer = candidate;
                }
            }
        }

        return answer;
    }
}

// Demo code
var solution = new Solution();

int[] seats1 = { 1, 1, 0, 1, 0, 1, 1, 1 };
int result1 = solution.MaxConsecutiveSeatsAfterOneMove(seats1);
Console.WriteLine($"Example 1 result: {result1}"); // Expected: 4

int[] seats2 = { 1, 0, 1, 1, 0, 1 };
int result2 = solution.MaxConsecutiveSeatsAfterOneMove(seats2);
Console.WriteLine($"Example 2 result: {result2}"); // Correct result under valid move rules: 4

int[] seats3 = { 1, 1, 1, 1 };
int result3 = solution.MaxConsecutiveSeatsAfterOneMove(seats3);
Console.WriteLine($"All reserved result: {result3}"); // Expected: 4

int[] seats4 = { 0, 0, 0 };
int result4 = solution.MaxConsecutiveSeatsAfterOneMove(seats4);
Console.WriteLine($"No reservations result: {result4}"); // Expected: 0

int[] seats5 = { 1, 0, 1 };
int result5 = solution.MaxConsecutiveSeatsAfterOneMove(seats5);
Console.WriteLine($"Simple merge result: {result5}"); // Expected: 2

int[] seats6 = { 1, 1, 0, 1 };
int result6 = solution.MaxConsecutiveSeatsAfterOneMove(seats6);
Console.WriteLine($"Need extra one check result: {result6}"); // Expected: 3