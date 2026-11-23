/*
Title: Pair Dancers for a Narrow Stage

Problem Description:
A performance team is preparing dancers for a stage act. Each dancer has a body width represented by an integer in the array widths, where widths[i] is the width of the i-th dancer. A stage mark can hold at most two dancers at the same time, and the combined width of any pair assigned to the same mark must be less than or equal to stageLimit. A dancer may also perform alone on a mark.

Your task is to determine the minimum number of stage marks needed so that every dancer can be assigned to exactly one mark.

You may reorder the dancers in any way before assigning them. The goal is not to count the number of valid pairings, but to minimize the total number of marks used. This makes greedy pairing important: using a wide dancer with the best possible narrow partner can reduce the total number of marks.

Return the minimum number of stage marks required.

Constraints:
- 1 <= widths.length <= 100000
- 1 <= widths[i] <= stageLimit <= 1000000000
- Every dancer must be assigned to exactly one mark
- Each mark can hold one or two dancers only

Example 1:
Input: widths = [2, 3, 4, 5], stageLimit = 6
Output: 3
Explanation: Pair dancers with widths 2 and 4 on one mark. The dancers with widths 3 and 5 must stand alone. So the minimum number of marks is 3.

Example 2:
Input: widths = [1, 2, 2, 3, 4], stageLimit = 4
Output: 3
Explanation: One optimal arrangement is (1,3), (2,2), and (4). This uses 3 marks, which is the minimum possible.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the array takes O(n log n)
    - The two-pointer scan takes O(n)
    - Total: O(n log n)

    Space Complexity:
    - If we sort the input array in place, the extra working space used by our algorithm is O(1)
      not counting any internal implementation details of the sorting routine.
    */
    public int MinStageMarks(int[] widths, int stageLimit)
    {
        // Step 1: Sort the widths from smallest to largest.
        //
        // Why do we sort?
        // The key greedy idea is:
        // - Try to place the widest remaining dancer first, because that dancer is the hardest to pair.
        // - If that widest dancer can fit with the narrowest remaining dancer, we should pair them.
        // - If the widest dancer cannot fit with the narrowest dancer, then the widest dancer cannot fit
        //   with anyone else either, because everyone else is at least as wide as the narrowest dancer.
        //
        // Sorting makes this logic possible and efficient.
        Array.Sort(widths);

        // Step 2: Create two pointers.
        //
        // left  -> points to the narrowest dancer not yet assigned
        // right -> points to the widest dancer not yet assigned
        //
        // We will keep assigning dancers until all dancers are used.
        int left = 0;
        int right = widths.Length - 1;

        // This will count how many stage marks we need.
        int marks = 0;

        // Step 3: Process dancers from both ends toward the middle.
        //
        // Each loop iteration assigns exactly one stage mark.
        // That mark will always include the widest remaining dancer at index "right".
        //
        // There are two possibilities:
        // 1. The widest dancer can pair with the narrowest dancer.
        //    Then we use one mark for both, and move both pointers.
        // 2. The widest dancer cannot pair with the narrowest dancer.
        //    Then the widest dancer must stand alone, and we move only the right pointer.
        while (left <= right)
        {
            // We are definitely going to use one more stage mark now,
            // because at least the dancer at "right" must be assigned.
            marks++;

            // If left == right, there is exactly one dancer left.
            // That dancer must stand alone on this final mark.
            if (left == right)
            {
                break;
            }

            // Compute the combined width of the narrowest and widest remaining dancers.
            //
            // This is the most important greedy check in the algorithm.
            int combinedWidth = widths[left] + widths[right];

            // If they fit together within the stage limit:
            if (combinedWidth <= stageLimit)
            {
                // Pair them on the same mark.
                //
                // Why is this safe and optimal?
                // Because we are using the widest dancer with the smallest possible partner.
                // If the widest dancer can fit with the smallest dancer, pairing them saves a mark.
                // Also, using the smallest dancer here is smart because it leaves larger dancers
                // for later, where they may still need to stand alone or pair among themselves.
                left++;
                right--;
            }
            else
            {
                // They do NOT fit together.
                //
                // Why does this mean the widest dancer must stand alone?
                // Since widths[left] is the smallest remaining dancer, any other remaining dancer
                // would be widths[left] or larger.
                // So if widths[left] + widths[right] is already too large, then:
                //   widths[anyOther] + widths[right] will also be too large.
                //
                // Therefore, the widest dancer cannot pair with anyone.
                // We must place that dancer alone on a mark.
                right--;
            }
        }

        // After the loop finishes, every dancer has been assigned exactly once,
        // and "marks" is the minimum number of stage marks required.
        return marks;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1:
// widths = [2, 3, 4, 5], stageLimit = 6
// Sorted: [2, 3, 4, 5]
// Pair 2 + 5 = 7 -> too large, so 5 alone
// Pair 2 + 4 = 6 -> fits, so (2,4)
// Remaining 3 alone
// Total = 3
int[] widths1 = { 2, 3, 4, 5 };
int stageLimit1 = 6;
int result1 = solution.MinStageMarks(widths1, stageLimit1);
Console.WriteLine(result1); // Expected: 3

// Example 2:
// widths = [1, 2, 2, 3, 4], stageLimit = 4
// Sorted: [1, 2, 2, 3, 4]
// Pair 1 + 4 = 5 -> too large, so 4 alone
// Pair 1 + 3 = 4 -> fits
// Pair 2 + 2 = 4 -> fits
// Total = 3
int[] widths2 = { 1, 2, 2, 3, 4 };
int stageLimit2 = 4;
int result2 = solution.MinStageMarks(widths2, stageLimit2);
Console.WriteLine(result2); // Expected: 3

// Additional quick demo:
int[] widths3 = { 3 };
int stageLimit3 = 3;
int result3 = solution.MinStageMarks(widths3, stageLimit3);
Console.WriteLine(result3); // Expected: 1