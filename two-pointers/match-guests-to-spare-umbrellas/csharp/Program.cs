/*
Title: Match Guests to Spare Umbrellas

Problem Description:
You are organizing an outdoor event. Some guests arrived without umbrellas, and you have a collection of spare umbrellas of different sizes. Each guest has a minimum umbrella size they are willing to use, and each spare umbrella has a fixed size. A guest can be assigned at most one umbrella, and each umbrella can be given to at most one guest. An umbrella can be assigned to a guest only if the umbrella size is greater than or equal to that guest's minimum required size.

Return the maximum number of guests who can receive a suitable umbrella.

This problem is about making as many valid assignments as possible, not about minimizing waste. A simple and efficient strategy is to sort both lists and use two pointers to scan through them. If the current smallest available umbrella can satisfy the current guest, assign it and move both pointers. Otherwise, skip that umbrella and try a larger one.

Constraints:
- 1 <= guests.length, umbrellas.length <= 100000
- 1 <= guests[i], umbrellas[i] <= 1000000000
- guests[i] is the minimum size required by the i-th guest
- umbrellas[i] is the size of the i-th spare umbrella

Example 1:
Input: guests = [2, 3, 1], umbrellas = [1, 1, 3]
Output: 2
Explanation: Sort both arrays: guests = [1, 2, 3], umbrellas = [1, 1, 3].
Assign size 1 to guest 1, skip the remaining size 1 for guest 2, and assign size 3 to guest 3. So 2 guests can be served.

Example 2:
Input: guests = [4, 2, 5], umbrellas = [2, 3, 4, 6]
Output: 3
Explanation: After sorting, guests = [2, 4, 5] and umbrellas = [2, 3, 4, 6].
Assign 2 to guest 2, assign 4 to guest 4, and assign 6 to guest 5. All 3 guests receive suitable umbrellas.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting guests: O(g log g), where g = number of guests
    - Sorting umbrellas: O(u log u), where u = number of umbrellas
    - Two-pointer scan: O(g + u)
    - Overall: O(g log g + u log u)

    Space Complexity:
    - If we sort the input arrays in place, the extra algorithmic space is O(1)
      not counting the internal space used by the sorting implementation.
    */
    public int MaxGuestsWithUmbrellas(int[] guests, int[] umbrellas)
    {
        // Step 1: Sort the guests array in non-decreasing order.
        // Why?
        // We want to try satisfying the guest with the smallest requirement first.
        // This is a classic greedy idea:
        // - If a small umbrella can satisfy a small guest, we should use it there.
        // - Saving larger umbrellas for guests with larger requirements helps maximize
        //   the total number of successful assignments.
        //
        // Data structure choice:
        // We use the input arrays directly and sort them in place.
        // Arrays are efficient here because:
        // - We only need sequential access after sorting.
        // - We do not need insertions or deletions in the middle.
        Array.Sort(guests);

        // Step 2: Sort the umbrellas array in non-decreasing order.
        // Why?
        // After sorting, we can scan from the smallest umbrella upward.
        // This lets us decide, for each current guest, whether the current smallest
        // remaining umbrella is enough.
        //
        // If it is enough, we assign it immediately.
        // If it is not enough, then this umbrella cannot satisfy the current guest,
        // and because guests are sorted, it also cannot satisfy any later guest
        // (later guests need the same or larger size).
        // Therefore, the only sensible action is to skip this umbrella.
        Array.Sort(umbrellas);

        // Step 3: Prepare two pointers.
        //
        // guestIndex points to the current guest we are trying to satisfy.
        // umbrellaIndex points to the current umbrella we are considering.
        //
        // We also keep a counter for how many successful assignments we make.
        int guestIndex = 0;
        int umbrellaIndex = 0;
        int matchedGuests = 0;

        // Step 4: Scan through both sorted arrays using the two-pointer technique.
        //
        // We continue while:
        // - there are still guests left to try to satisfy, and
        // - there are still umbrellas left to consider.
        //
        // This loop is efficient because each pointer only moves forward.
        // No pointer ever moves backward, so the scan is linear after sorting.
        while (guestIndex < guests.Length && umbrellaIndex < umbrellas.Length)
        {
            // Current guest's minimum required umbrella size.
            int requiredSize = guests[guestIndex];

            // Current umbrella's actual size.
            int currentUmbrellaSize = umbrellas[umbrellaIndex];

            // Step 4a: Check whether the current umbrella can satisfy the current guest.
            if (currentUmbrellaSize >= requiredSize)
            {
                // This umbrella is large enough for this guest.
                //
                // Why assign immediately?
                // Because this is the smallest remaining umbrella that can satisfy
                // the smallest remaining guest. Using it now is optimal:
                // - It satisfies one guest for sure.
                // - It preserves larger umbrellas for guests who may need them.
                //
                // So we record one successful match.
                matchedGuests++;

                // Move to the next guest because the current guest has now received
                // an umbrella and is fully handled.
                guestIndex++;

                // Move to the next umbrella because each umbrella can be used at most once.
                umbrellaIndex++;
            }
            else
            {
                // Step 4b: The current umbrella is too small for the current guest.
                //
                // Why can we safely skip this umbrella?
                // Because:
                // - guests are sorted in increasing order
                // - the current guest is the smallest remaining guest
                //
                // If this umbrella cannot satisfy the smallest remaining guest,
                // then it definitely cannot satisfy any later guest either,
                // since later guests require the same or larger size.
                //
                // Therefore, this umbrella is useless from this point onward,
                // and the correct greedy action is to discard it by moving
                // the umbrella pointer forward.
                umbrellaIndex++;
            }
        }

        // Step 5: Return the total number of successful assignments.
        //
        // This is the maximum possible number because the greedy strategy above
        // never wastes a usable small umbrella on a guest who could have taken
        // a larger one later, and it never keeps an umbrella that is too small
        // to help any remaining guest.
        return matchedGuests;
    }
}

// Demo code:
// We create the sample inputs from the problem statement,
// call the solution method, and print the results.

// Example 1
int[] guests1 = { 2, 3, 1 };
int[] umbrellas1 = { 1, 1, 3 };

var solution = new Solution();
int result1 = solution.MaxGuestsWithUmbrellas(guests1, umbrellas1);

Console.WriteLine("Example 1 Result: " + result1); // Expected: 2

// Example 2
int[] guests2 = { 4, 2, 5 };
int[] umbrellas2 = { 2, 3, 4, 6 };

int result2 = solution.MaxGuestsWithUmbrellas(guests2, umbrellas2);

Console.WriteLine("Example 2 Result: " + result2); // Expected: 3