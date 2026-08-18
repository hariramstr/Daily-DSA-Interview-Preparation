/*
Title: Minimum Search Radius for Emergency Supply Lockers
Difficulty: Medium
Topic: Binary Search

Problem Description:
A city is planning emergency supply coverage along a very long straight highway. There are n neighborhoods
located at integer positions in the array homes, and m supply lockers located at integer positions in the
array lockers. A neighborhood is considered covered if there exists at least one locker whose distance from
that neighborhood is at most R.

Your task is to find the minimum integer radius R such that every neighborhood is covered by at least one locker.

The arrays are not guaranteed to be sorted. Positions may be large, and multiple homes or lockers may share
the same position. You should design an algorithm efficient enough for large inputs. A brute-force comparison
of every home with every locker will be too slow.

Return the smallest possible integer R.

Constraints:
- 1 <= homes.length, lockers.length <= 2 * 10^5
- 0 <= homes[i], lockers[i] <= 10^9
- The answer fits in a 32-bit signed integer.

Example 1:
Input: homes = [2, 10, 14], lockers = [4, 12]
Output: 2
Explanation: With radius 2, home 2 is covered by locker 4, home 10 is covered by locker 12, and home 14 is
covered by locker 12. Radius 1 is not enough because home 2 would be uncovered.

Example 2:
Input: homes = [1, 5, 9, 15], lockers = [6]
Output: 9
Explanation: A single locker at position 6 must cover all homes. The farthest home is at position 15, which
is distance 9 away, so the minimum valid radius is 9.

Approach:
1. Sort both arrays.
2. Binary search the answer R.
3. For each candidate radius R, scan through homes from left to right while advancing through lockers.
   For each home, move the locker pointer until the locker is no longer too far left to cover that home.
   Then check whether the current locker can cover the home.
4. Because "if radius R works, any larger radius also works", binary search is valid.

Correctness check on examples:
- Example 1:
  homes   = [2, 10, 14]
  lockers = [4, 12]
  R = 2 works:
    home 2 covered by locker 4 (distance 2)
    home 10 covered by locker 12 (distance 2)
    home 14 covered by locker 12 (distance 2)
  R = 1 fails because home 2 is not covered.
  Minimum is 2.
- Example 2:
  homes   = [1, 5, 9, 15]
  lockers = [6]
  Distances are 5, 1, 3, 9, so the maximum minimum distance is 9.
  Minimum valid radius is 9.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting homes:   O(n log n)
    - Sorting lockers: O(m log m)
    - Binary search over radius range: O(log V), where V is the search range of positions
    - Coverage check per binary search step: O(n + m)
    Overall: O(n log n + m log m + (n + m) log V)

    Space Complexity:
    - If sorting in place is allowed, extra algorithmic space is O(1) beyond the input arrays
      (ignoring the internal stack usage of the sorting implementation).
    */
    public int FindMinimumRadius(int[] homes, int[] lockers)
    {
        // Step 1:
        // Sort both arrays so we can process positions from left to right.
        //
        // Why this is necessary:
        // Without sorting, we would have no efficient way to compare homes and lockers in order.
        // Sorting creates structure, which allows us to use:
        // - a linear scan for checking coverage
        // - binary search on the answer
        //
        // Data structure choice:
        // We keep using arrays because they are compact and efficient.
        // Array.Sort sorts them in place.
        Array.Sort(homes);
        Array.Sort(lockers);

        // Step 2:
        // Establish the binary search range for the answer.
        //
        // The minimum possible radius is 0:
        // this would mean every home already has a locker at the same position.
        int left = 0;

        // The maximum possible radius can safely be the distance between the farthest possible
        // home and locker positions after sorting. A simpler safe upper bound is:
        // max(abs(home - leftmostLocker), abs(home - rightmostLocker)) over extreme homes,
        // but an even simpler universal bound under constraints is 1_000_000_000.
        //
        // Since positions are in [0, 1e9], the needed radius cannot exceed 1e9.
        int right = 1_000_000_000;

        // Step 3:
        // Standard binary search for the smallest radius that works.
        //
        // Invariant:
        // - Any radius < answer is invalid
        // - Any radius >= answer is valid
        while (left < right)
        {
            // Compute the middle carefully.
            // This avoids overflow in general binary search patterns.
            int mid = left + (right - left) / 2;

            // Step 4:
            // Check whether this candidate radius is enough to cover every home.
            if (CanCoverAllHomes(homes, lockers, mid))
            {
                // If mid works, try to find an even smaller valid radius.
                right = mid;
            }
            else
            {
                // If mid does not work, we must increase the radius.
                left = mid + 1;
            }
        }

        // When the loop ends, left == right and points to the smallest valid radius.
        return left;
    }

    private bool CanCoverAllHomes(int[] homes, int[] lockers, int radius)
    {
        // This method answers:
        // "If every locker can cover positions within [locker - radius, locker + radius],
        //  are all homes covered?"
        //
        // We use a two-pointer scan:
        // - i walks through homes from left to right
        // - j walks through lockers from left to right
        //
        // Why this works:
        // Since both arrays are sorted, once a locker is too far left to cover the current home,
        // it will also be too far left for all later homes. So we can permanently move past it.
        //
        // This gives a linear O(n + m) check instead of O(n * m).

        int j = 0;

        // Process each home in increasing order.
        for (int i = 0; i < homes.Length; i++)
        {
            int home = homes[i];

            // Move locker pointer forward while the current locker is definitely too far left
            // to cover this home.
            //
            // Condition:
            // lockers[j] + radius < home
            //
            // Meaning:
            // Even the rightmost point this locker can cover is still left of the home.
            // Therefore, this locker cannot cover this home or any future home.
            while (j < lockers.Length && (long)lockers[j] + radius < home)
            {
                j++;
            }

            // After skipping unusable lockers:
            // - If j == lockers.Length, there are no lockers left, so this home is uncovered.
            if (j == lockers.Length)
            {
                return false;
            }

            // Now lockers[j] is the first locker that is not too far left.
            // We must verify it is not too far right.
            //
            // If lockers[j] - radius > home, then this locker's coverage starts to the right
            // of the home, meaning the home is not covered by this locker.
            //
            // Because j is the first locker not too far left, any later locker will be even farther right,
            // so none of them can cover this home either.
            if ((long)lockers[j] - radius > home)
            {
                return false;
            }

            // Otherwise, this home is covered.
            // We continue to the next home, keeping the same locker pointer j.
            //
            // Why keep j where it is?
            // The same locker may also cover upcoming homes, so there is no reason to move it yet.
        }

        // If we successfully processed every home, then this radius works.
        return true;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1
int[] homes1 = { 2, 10, 14 };
int[] lockers1 = { 4, 12 };
int result1 = solution.FindMinimumRadius(homes1, lockers1);
Console.WriteLine(result1); // Expected: 2

// Example 2
int[] homes2 = { 1, 5, 9, 15 };
int[] lockers2 = { 6 };
int result2 = solution.FindMinimumRadius(homes2, lockers2);
Console.WriteLine(result2); // Expected: 9