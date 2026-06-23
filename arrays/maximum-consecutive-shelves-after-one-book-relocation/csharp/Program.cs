/*
Title: Maximum Consecutive Shelves After One Book Relocation

Problem Description:
A library stores books on a long shelf, represented by an array `shelves` of length `n`.
Each element is either `0` or `1`, where `1` means the position currently contains a
featured book and `0` means it is empty.

The librarian may relocate at most one featured book from any position containing `1`
to any position containing `0`. After this optional relocation, determine the maximum
possible length of a consecutive block of featured books.

A relocation removes one existing `1` and places it into one existing `0`, so the total
number of featured books stays the same. You may also choose not to relocate any book.
Your task is to return the largest number of consecutive `1`s that can appear after
performing at most one such move.

This problem models a realistic array optimization scenario: one local change can merge
two nearby runs, extend an existing run, or be useless if the array is already optimal.
Be careful when a `0` separates two runs of `1`s: you can only fill that gap if there is
at least one extra `1` somewhere else to move.

Constraints:
- 1 <= n <= 200000
- shelves[i] is either 0 or 1

Example 1:
Input: shelves = [1,1,0,1,1,0,1]
Output: 5
Explanation: Move the last `1` into the zero at index 2.
The array can become [1,1,1,1,1,0,0], giving a consecutive block of length 5.

Example 2:
Input: shelves = [1,0,1,1,0,1]
Output: 4
Explanation: Fill the zero at index 4 using the `1` at index 0.
One possible result is [0,0,1,1,1,1], so the maximum consecutive block is 4.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Beginner-friendly idea:
    We want the longest consecutive block of 1s after moving at most one existing 1
    into one 0 position.

    A very useful observation is:
    - If we look at a zero position, and count how many consecutive 1s are directly
      on its left and directly on its right, then filling that zero could connect
      those two runs into one larger run.
    - However, we are only allowed to fill that zero if we can take a 1 from somewhere else.
      That means:
        * If there are more 1s in the whole array than just the left run + right run,
          then we have an "extra" 1 somewhere else, so we can create:
              left + 1 + right
        * Otherwise, all 1s already belong to those two runs, so moving one of them
          into the zero would break one side while filling the gap. In that case the
          best achievable connected length is only:
              left + right

    We also must consider arrays with no zero at all:
    - Then the answer is simply the total number of 1s, because there is nowhere to move.
    */
    public int MaximumConsecutiveShelvesAfterOneRelocation(int[] shelves)
    {
        int n = shelves.Length;

        // Step 1:
        // Count the total number of featured books (the total number of 1s).
        //
        // Why do we need this?
        // Because when we consider filling a zero, we must know whether there exists
        // an extra 1 somewhere else in the array that can be relocated into that zero.
        // The total count lets us compare:
        //   leftRun + rightRun
        // against
        //   totalOnes
        //
        // If totalOnes > leftRun + rightRun, then there is at least one 1 outside
        // those adjacent runs, so we can move that extra 1 into the zero.
        int totalOnes = 0;
        for (int i = 0; i < n; i++)
        {
            if (shelves[i] == 1)
            {
                totalOnes++;
            }
        }

        // If there are no featured books at all, no consecutive block of 1s can exist.
        if (totalOnes == 0)
        {
            return 0;
        }

        // Step 2:
        // Build an array "leftOnes" where:
        //   leftOnes[i] = number of consecutive 1s ending exactly at index i
        //
        // Example:
        // shelves = [1,1,0,1,1,1]
        // leftOnes = [1,2,0,1,2,3]
        //
        // Why is this useful?
        // For any zero at index i, the consecutive 1s immediately to its left are:
        //   leftOnes[i - 1]   (if i > 0)
        //
        // This lets us answer "how long is the left run next to this zero?" in O(1).
        int[] leftOnes = new int[n];
        if (shelves[0] == 1)
        {
            leftOnes[0] = 1;
        }

        for (int i = 1; i < n; i++)
        {
            if (shelves[i] == 1)
            {
                // If current position is 1, extend the consecutive run from the left.
                leftOnes[i] = leftOnes[i - 1] + 1;
            }
            else
            {
                // If current position is 0, a run of consecutive 1s cannot end here.
                leftOnes[i] = 0;
            }
        }

        // Step 3:
        // Build an array "rightOnes" where:
        //   rightOnes[i] = number of consecutive 1s starting exactly at index i
        //
        // Example:
        // shelves = [1,1,0,1,1,1]
        // rightOnes = [2,1,0,3,2,1]
        //
        // Why is this useful?
        // For any zero at index i, the consecutive 1s immediately to its right are:
        //   rightOnes[i + 1]   (if i < n - 1)
        //
        // This lets us answer "how long is the right run next to this zero?" in O(1).
        int[] rightOnes = new int[n];
        if (shelves[n - 1] == 1)
        {
            rightOnes[n - 1] = 1;
        }

        for (int i = n - 2; i >= 0; i--)
        {
            if (shelves[i] == 1)
            {
                // If current position is 1, extend the consecutive run from the right.
                rightOnes[i] = rightOnes[i + 1] + 1;
            }
            else
            {
                // If current position is 0, a run of consecutive 1s cannot start here.
                rightOnes[i] = 0;
            }
        }

        // Step 4:
        // Initialize the answer with the best block already present without any move.
        //
        // Why is this necessary?
        // Because the problem says "at most one move", which means we are allowed
        // to do nothing if moving does not help.
        //
        // The longest existing run can be found as the maximum value in leftOnes
        // (or rightOnes). We use leftOnes here.
        int answer = 0;
        for (int i = 0; i < n; i++)
        {
            if (leftOnes[i] > answer)
            {
                answer = leftOnes[i];
            }
        }

        // Step 5:
        // Try every zero as the possible destination of the relocated 1.
        //
        // For each zero:
        //   left  = consecutive 1s immediately on the left side
        //   right = consecutive 1s immediately on the right side
        //
        // If we fill this zero, we may connect those two runs.
        //
        // Two cases:
        // 1) There exists an extra 1 elsewhere:
        //      totalOnes > left + right
        //    Then we can create:
        //      left + 1 + right
        //
        // 2) No extra 1 exists elsewhere:
        //      totalOnes == left + right
        //    Then all 1s are already in those adjacent runs.
        //    Moving one of them into this zero does not increase the total connected
        //    amount beyond:
        //      left + right
        //
        // We update the global best answer with the best result from each zero.
        for (int i = 0; i < n; i++)
        {
            if (shelves[i] == 0)
            {
                int left = 0;
                int right = 0;

                // Read the run directly to the left of this zero.
                if (i > 0)
                {
                    left = leftOnes[i - 1];
                }

                // Read the run directly to the right of this zero.
                if (i < n - 1)
                {
                    right = rightOnes[i + 1];
                }

                int candidate;

                if (totalOnes > left + right)
                {
                    // There is at least one extra 1 somewhere else in the array.
                    // So we can move that extra 1 into this zero and fully connect:
                    // left run + filled zero + right run
                    candidate = left + 1 + right;
                }
                else
                {
                    // There is no extra 1 outside these adjacent runs.
                    // So although we can still relocate a 1, we would have to take it
                    // from one of these runs, meaning the net best connected size is
                    // only left + right.
                    candidate = left + right;
                }

                if (candidate > answer)
                {
                    answer = candidate;
                }
            }
        }

        // Step 6:
        // Safety cap: the answer can never exceed the total number of 1s in the array,
        // because relocation does not create new 1s.
        //
        // In practice, the logic above already respects this, but this line makes the
        // constraint explicit and keeps the result obviously valid.
        if (answer > totalOnes)
        {
            answer = totalOnes;
        }

        return answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] shelves1 = { 1, 1, 0, 1, 1, 0, 1 };
int result1 = solution.MaximumConsecutiveShelvesAfterOneRelocation(shelves1);
Console.WriteLine($"Example 1 result: {result1}"); // Expected: 5

// Example 2
int[] shelves2 = { 1, 0, 1, 1, 0, 1 };
int result2 = solution.MaximumConsecutiveShelvesAfterOneRelocation(shelves2);
Console.WriteLine($"Example 2 result: {result2}"); // Expected: 4

// Additional demo cases

int[] shelves3 = { 1, 1, 1, 1 };
Console.WriteLine($"All ones: {solution.MaximumConsecutiveShelvesAfterOneRelocation(shelves3)}"); // Expected: 4

int[] shelves4 = { 0, 0, 0, 0 };
Console.WriteLine($"All zeros: {solution.MaximumConsecutiveShelvesAfterOneRelocation(shelves4)}"); // Expected: 0

int[] shelves5 = { 1, 0, 1 };
Console.WriteLine($"Single gap with no extra one: {solution.MaximumConsecutiveShelvesAfterOneRelocation(shelves5)}"); // Expected: 2

int[] shelves6 = { 1, 0, 1, 0, 1, 1 };
Console.WriteLine($"Mixed case: {solution.MaximumConsecutiveShelvesAfterOneRelocation(shelves6)}"); // Expected: 4