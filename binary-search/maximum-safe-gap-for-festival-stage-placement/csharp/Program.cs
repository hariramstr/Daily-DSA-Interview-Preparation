/*
Title: Maximum Safe Gap for Festival Stage Placement

Problem Description:
A city is preparing a long riverside walkway for a music festival. There are n approved
installation spots along the walkway, and the ith spot is located at position positions[i]
meters from the western entrance. You must place exactly k outdoor stages at these approved spots.

For safety and crowd flow, the organizers want the minimum distance between any two placed
stages to be as large as possible. In other words, if you place stages at some k positions,
define the arrangement score as the smallest absolute difference between every pair of
consecutive chosen stage positions after sorting them. Your task is to return the maximum
possible arrangement score.

You may assume stages can only be placed at the given approved spots, and each spot can hold
at most one stage. The input positions are not guaranteed to be sorted.

This problem is designed to test whether you can recognize a monotonic decision condition:
if it is possible to place k stages with minimum gap d, then it is also possible for any
smaller gap. That makes binary search on the answer possible.

Constraints:
- 2 <= n <= 10^5
- 2 <= k <= n
- 0 <= positions[i] <= 10^9
- All values in positions are distinct.

Example 1:
Input: positions = [2, 11, 6, 17, 25], k = 3
Output: 9

Example 2:
Input: positions = [1, 3, 4, 8, 13, 17], k = 4
Output: 4
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the positions array takes O(n log n)
    - Each feasibility check takes O(n)
    - Binary search runs O(log R) times, where R = maxPosition - minPosition
    - Total: O(n log n + n log R)

    Space Complexity:
    - O(1) extra space beyond the input array if sorting in place is allowed
    - The algorithm only uses a few variables
    */
    public int MaximizeMinimumGap(int[] positions, int k)
    {
        // Step 1:
        // Sort the positions so we can reason about distances from left to right.
        //
        // Why this is necessary:
        // The greedy feasibility check depends on placing stages in increasing order.
        // Without sorting, we cannot correctly decide whether the next chosen spot is
        // far enough from the previously chosen spot.
        //
        // Data structure choice:
        // We use the input array itself and sort it in place. This is memory-efficient
        // and perfectly fine because we do not need the original order afterward.
        Array.Sort(positions);

        // Step 2:
        // Define the search range for binary search on the answer.
        //
        // We are not binary searching an index. We are binary searching the value of the
        // minimum allowed gap between any two chosen stages.
        //
        // Lowest possible answer:
        // 0, because positions can be close together and a minimum gap of 0 is always
        // conceptually feasible if we only care about "at least 0".
        //
        // Highest possible answer:
        // positions[last] - positions[first], which is the largest distance between any
        // two approved spots. The true answer cannot exceed this.
        int left = 0;
        int right = positions[^1] - positions[0];

        // Step 3:
        // Store the best feasible answer found so far.
        //
        // Why this is necessary:
        // During binary search, when a candidate gap is feasible, we want to remember it
        // and try to do better by searching larger gaps.
        int best = 0;

        // Step 4:
        // Standard binary search over the answer space.
        //
        // Monotonic property:
        // - If a gap "mid" is feasible, then every smaller gap is also feasible.
        // - If a gap "mid" is not feasible, then every larger gap is also not feasible.
        //
        // This monotonic behavior is exactly what makes binary search valid here.
        while (left <= right)
        {
            // Compute the middle candidate gap safely.
            int mid = left + (right - left) / 2;

            // Step 5:
            // Check whether it is possible to place exactly k stages such that every pair
            // of consecutive chosen stages is at least "mid" apart.
            //
            // Important note:
            // If we can place at least k stages with gap mid, then we can certainly choose
            // exactly k of them. So "at least k" is enough for feasibility.
            if (CanPlaceStages(positions, k, mid))
            {
                // This candidate gap works.
                // Record it as the best answer seen so far.
                best = mid;

                // Since mid is feasible, maybe an even larger minimum gap is also feasible.
                // So we move the search to the right half.
                left = mid + 1;
            }
            else
            {
                // This candidate gap does not work.
                // Therefore, any larger gap also cannot work.
                // So we move the search to the left half.
                right = mid - 1;
            }
        }

        // Step 6:
        // Return the largest feasible minimum gap found.
        return best;
    }

    private bool CanPlaceStages(int[] positions, int k, int requiredGap)
    {
        // This helper performs a greedy feasibility check.
        //
        // Greedy idea:
        // Always place the first stage at the leftmost available approved spot.
        // Then, for each next stage, place it at the earliest spot that is at least
        // "requiredGap" away from the last chosen spot.
        //
        // Why greedy works:
        // Choosing the earliest valid next spot leaves as much room as possible for the
        // remaining stages. This is the standard optimal greedy strategy for spacing problems.

        // Step 1:
        // Place the first stage at the first (smallest) position.
        //
        // Why this is safe:
        // Starting as far left as possible never hurts feasibility. It only gives us more
        // remaining space to the right for future placements.
        int placedCount = 1;
        int lastPlacedPosition = positions[0];

        // Step 2:
        // Scan through the sorted positions from left to right and greedily place stages
        // whenever the current spot is far enough from the last placed stage.
        for (int i = 1; i < positions.Length; i++)
        {
            // Check whether this spot satisfies the required minimum gap.
            if (positions[i] - lastPlacedPosition >= requiredGap)
            {
                // Place a stage here.
                placedCount++;
                lastPlacedPosition = positions[i];

                // Early exit optimization:
                // As soon as we have placed k stages, we know this gap is feasible.
                if (placedCount >= k)
                {
                    return true;
                }
            }
        }

        // If we finish scanning and still have fewer than k stages placed,
        // then this required gap is not feasible.
        return false;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// positions = [2, 11, 6, 17, 25], k = 3
// Sorted positions become [2, 6, 11, 17, 25]
//
// Try answer 9:
// Place at 2
// Next valid at 11 (gap 9)
// Next valid at 25 (gap 14)
// Total placed = 3 => feasible
//
// Try answer 10:
// Place at 2
// Next valid at 17 (gap 15)
// No third position with gap at least 10 from 17 except 25, but 25 - 17 = 8
// Total placed = 2 => not feasible
//
// Therefore answer = 9
int[] positions1 = { 2, 11, 6, 17, 25 };
int k1 = 3;
int result1 = solution.MaximizeMinimumGap(positions1, k1);
Console.WriteLine(result1); // Expected: 9

// Example 2:
// positions = [1, 3, 4, 8, 13, 17], k = 4
//
// Try answer 4:
// Place at 1
// Next valid at 8 (gap 7)
// Next valid at 13 (gap 5)
// Next valid at 17 (gap 4)
// Total placed = 4 => feasible
//
// Try answer 5:
// Place at 1
// Next valid at 8 (gap 7)
// Next valid at 13 (gap 5)
// No next valid at 17 because 17 - 13 = 4
// Total placed = 3 => not feasible
//
// Therefore answer = 4
int[] positions2 = { 1, 3, 4, 8, 13, 17 };
int k2 = 4;
int result2 = solution.MaximizeMinimumGap(positions2, k2);
Console.WriteLine(result2); // Expected: 4

// Additional quick demo
int[] positions3 = { 5, 10 };
int k3 = 2;
int result3 = solution.MaximizeMinimumGap(positions3, k3);
Console.WriteLine(result3); // Expected: 5