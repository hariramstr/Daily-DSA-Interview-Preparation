/*
Title: Maximum Points from Skipping Adjacent Museum Rooms
Difficulty: Easy
Topic: Dynamic Programming

Problem Description:
A museum curator is planning a guided tour through a straight hallway of exhibit rooms.
Each room has a popularity score, given in an integer array `rooms`, where `rooms[i]`
is the number of visitor points earned if room `i` is included in the tour.

To avoid crowding and noise overlap, the curator cannot include two adjacent rooms
in the same tour.

Your task is to return the maximum total visitor points the curator can earn by choosing
a subset of rooms such that no two chosen rooms are next to each other.

You may choose to skip any room, and it is also valid to choose no rooms at all if every
score is negative or zero. In other words, the answer should never be less than 0.

This is an interview-style dynamic programming problem. A correct solution should efficiently
decide, for each position, whether it is better to include the current room and skip the
previous one, or skip the current room and keep the best answer seen so far.

Constraints:
- 1 <= rooms.length <= 100000
- -1000 <= rooms[i] <= 1000

Example 1:
Input: rooms = [4, 2, 7, 9, 3]
Output: 13
Explanation: Choose rooms with scores 4, 9, and 3 is not allowed because 9 and 3 are adjacent.
The best valid choice is 4 + 9 = 13.

Example 2:
Input: rooms = [5, 1, 1, 5]
Output: 10
Explanation: Choose the first and last rooms for a total of 10. They are not adjacent, so this is valid.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We visit each room exactly once.
    - At each room, we do only constant-time work.

    Space Complexity: O(1)
    - We do not build a full DP array.
    - We only keep track of two previous dynamic programming states.

    Beginner-friendly idea:
    For each room, we have two choices:
    1. Skip this room:
       Then our total stays equal to the best total we had up to the previous room.
    2. Take this room:
       Then we are not allowed to take the previous room, so we add this room's score
       to the best total from two rooms back.

    We choose whichever of those two options gives a larger total.
    Also, because choosing no rooms is allowed, our running answer should never go below 0.
    */
    public int MaxPoints(int[] rooms)
    {
        // This variable represents:
        // "the best answer we can get considering rooms up to index i - 2"
        //
        // At the beginning, before we process any rooms, the best answer is 0
        // because we are allowed to choose no rooms at all.
        int prevTwo = 0;

        // This variable represents:
        // "the best answer we can get considering rooms up to index i - 1"
        //
        // Initially, before processing the first room, this is also 0.
        int prevOne = 0;

        // We now process each room from left to right.
        // This left-to-right order is important because the decision at the current room
        // depends on answers we already computed for earlier rooms.
        for (int i = 0; i < rooms.Length; i++)
        {
            // Option 1: Skip the current room.
            //
            // If we skip room i, then the best total remains whatever the best total
            // was up to the previous room.
            int skipCurrent = prevOne;

            // Option 2: Take the current room.
            //
            // If we take room i, we cannot take room i - 1 because adjacent rooms
            // are not allowed together.
            //
            // So the best total in this case is:
            // best up to i - 2 + current room's score
            int takeCurrent = prevTwo + rooms[i];

            // The best answer ending at this position is the better of:
            // - skipping the current room
            // - taking the current room
            //
            // Because prevOne and prevTwo both start at 0 and we always take max,
            // the result can never become negative. This naturally satisfies the rule
            // that choosing no rooms is allowed.
            int currentBest = Math.Max(skipCurrent, takeCurrent);

            // Now we shift our DP window forward:
            //
            // The old "best up to i - 1" becomes the new "best up to i - 2"
            // for the next iteration.
            prevTwo = prevOne;

            // The newly computed answer for "best up to i" becomes the new "best up to i - 1"
            // for the next iteration.
            prevOne = currentBest;
        }

        // After processing all rooms, prevOne holds the best possible total.
        return prevOne;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] rooms1 = { 4, 2, 7, 9, 3 };
int result1 = solution.MaxPoints(rooms1);
Console.WriteLine($"Input: [4, 2, 7, 9, 3] -> Output: {result1}");

// Example 2
int[] rooms2 = { 5, 1, 1, 5 };
int result2 = solution.MaxPoints(rooms2);
Console.WriteLine($"Input: [5, 1, 1, 5] -> Output: {result2}");

// Additional demo: all negative values, so best answer should be 0
int[] rooms3 = { -4, -2, -7 };
int result3 = solution.MaxPoints(rooms3);
Console.WriteLine($"Input: [-4, -2, -7] -> Output: {result3}");

// Additional demo: mixed values
int[] rooms4 = { 2, 1, 4, 9 };
int result4 = solution.MaxPoints(rooms4);
Console.WriteLine($"Input: [2, 1, 4, 9] -> Output: {result4}");