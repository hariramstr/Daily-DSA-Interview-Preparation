/*
Title: Minimum Rest Days for a Practice Plan
Difficulty: Easy
Topic: Dynamic Programming

Problem Description:
You are planning a sequence of daily activities for a student preparing for a competition.
For each day, the student may have access to coding practice, reading practice, both, or neither.
The student wants to stay productive, but cannot do the same type of practice on two consecutive
days because it becomes ineffective. On any day, the student may also choose to rest.

You are given an integer array activities where activities[i] describes what is available on day i:
- 0: neither coding nor reading is available, so the student must rest
- 1: only coding is available
- 2: only reading is available
- 3: both coding and reading are available

Return the minimum number of rest days needed over the entire schedule.

A valid plan must follow these rules:
- The student can do coding only if coding is available that day.
- The student can do reading only if reading is available that day.
- The student cannot do coding on two consecutive days.
- The student cannot do reading on two consecutive days.
- Resting is always allowed.

Constraints:
- 1 <= activities.length <= 100
- 0 <= activities[i] <= 3

Example 1:
Input: activities = [1,3,2,0,3]
Output: 2
Explanation: One optimal plan is coding, reading, rest, rest, coding. There are 2 rest days,
and no activity type is repeated on consecutive days.

Example 2:
Input: activities = [3,3,3]
Output: 1
Explanation: The student can do coding on day 1, reading on day 2, and must rest or choose
a non-repeating valid option on day 3. The best possible answer is 1 rest day.

Dynamic Programming Idea:
For each day, we track the minimum rest days needed depending on what was done on that day:
- state 0: rested today
- state 1: did coding today
- state 2: did reading today

This works because the only thing that matters for tomorrow is what we did today.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Explanation of complexity:
    - We process each day exactly once.
    - For each day, we update only 3 states:
      rest, coding, and reading.
    - Therefore the total work grows linearly with the number of days.
    - We do not need a full DP table for all days because each day depends only on the previous day,
      so we keep only the previous 3 values.
    */
    public int MinRestDays(int[] activities)
    {
        // We use a large number to represent an impossible state.
        // For example, if coding is not allowed on a day, we mark that state as "very expensive"
        // so it will never be chosen as the minimum answer.
        int inf = 1_000_000_000;

        // These three variables represent the DP values for the previous day.
        //
        // prevRest    = minimum rest days up to yesterday if yesterday we rested
        // prevCoding  = minimum rest days up to yesterday if yesterday we did coding
        // prevReading = minimum rest days up to yesterday if yesterday we did reading
        //
        // Before processing any day:
        // - We have done nothing yet.
        // - It is valid to think of the "previous state" as resting with 0 rest days.
        // - It is impossible that before day 1 we already did coding or reading.
        int prevRest = 0;
        int prevCoding = inf;
        int prevReading = inf;

        // Process each day one by one from left to right.
        // This is the natural order because today's decision depends on yesterday's activity.
        foreach (int day in activities)
        {
            // We now compute the DP values for the current day.
            //
            // currRest    = minimum rest days up to today if we rest today
            // currCoding  = minimum rest days up to today if we do coding today
            // currReading = minimum rest days up to today if we do reading today
            //
            // We start by assuming coding/reading are impossible today until proven otherwise.
            int currRest = Math.Min(prevRest, Math.Min(prevCoding, prevReading)) + 1;
            int currCoding = inf;
            int currReading = inf;

            // Why is currRest computed this way?
            // -----------------------------------
            // If we rest today, then yesterday could have been:
            // - rest
            // - coding
            // - reading
            //
            // Resting is always allowed, regardless of yesterday.
            // Since resting today adds exactly 1 rest day, we take the best previous state
            // and add 1.
            //
            // Example:
            // If the best schedule up to yesterday had 2 rest days, then resting today makes it 3.

            // Check whether coding is available today.
            //
            // Availability encoding:
            // 1 = only coding
            // 3 = both coding and reading
            //
            // So coding is available when day == 1 or day == 3.
            if (day == 1 || day == 3)
            {
                // If we do coding today, then yesterday CANNOT also be coding.
                // That would violate the "no same activity on consecutive days" rule.
                //
                // Therefore, the previous day must have been either:
                // - rest
                // - reading
                //
                // We choose the better of those two possibilities.
                currCoding = Math.Min(prevRest, prevReading);
            }

            // Check whether reading is available today.
            //
            // Availability encoding:
            // 2 = only reading
            // 3 = both coding and reading
            //
            // So reading is available when day == 2 or day == 3.
            if (day == 2 || day == 3)
            {
                // If we do reading today, then yesterday CANNOT also be reading.
                // That would break the rule against repeating the same activity.
                //
                // Therefore, the previous day must have been either:
                // - rest
                // - coding
                //
                // Again, we choose the better of those valid previous states.
                currReading = Math.Min(prevRest, prevCoding);
            }

            // After computing today's three states, we move them into the "previous" variables.
            // This prepares us for the next day.
            //
            // This rolling-DP approach saves space because we do not need to store all days.
            prevRest = currRest;
            prevCoding = currCoding;
            prevReading = currReading;
        }

        // At the end, the answer is the best among all valid ways to finish the last day:
        // - resting on the last day
        // - coding on the last day
        // - reading on the last day
        //
        // We return the minimum rest days among these possibilities.
        return Math.Min(prevRest, Math.Min(prevCoding, prevReading));
    }
}

// Demo code
var solution = new Solution();

// Sample input 1 from the problem statement
int[] activities1 = { 1, 3, 2, 0, 3 };
int result1 = solution.MinRestDays(activities1);
Console.WriteLine("Input: [1,3,2,0,3]");
Console.WriteLine("Minimum rest days: " + result1);
Console.WriteLine("Expected: 2");
Console.WriteLine();

// Sample input 2 from the problem statement
int[] activities2 = { 3, 3, 3 };
int result2 = solution.MinRestDays(activities2);
Console.WriteLine("Input: [3,3,3]");
Console.WriteLine("Minimum rest days: " + result2);
Console.WriteLine("Expected: 1");
Console.WriteLine();

// Additional demo cases for learning
int[] activities3 = { 0 };
int result3 = solution.MinRestDays(activities3);
Console.WriteLine("Input: [0]");
Console.WriteLine("Minimum rest days: " + result3);
Console.WriteLine("Expected: 1");
Console.WriteLine();

int[] activities4 = { 1, 1, 1, 1 };
int result4 = solution.MinRestDays(activities4);
Console.WriteLine("Input: [1,1,1,1]");
Console.WriteLine("Minimum rest days: " + result4);
Console.WriteLine("One optimal plan: coding, rest, coding, rest");
Console.WriteLine("Expected: 2");
Console.WriteLine();

int[] activities5 = { 2, 3, 1, 3, 2 };
int result5 = solution.MinRestDays(activities5);
Console.WriteLine("Input: [2,3,1,3,2]");
Console.WriteLine("Minimum rest days: " + result5);