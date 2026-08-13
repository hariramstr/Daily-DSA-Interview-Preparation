/*
Title: Count Pairs of Profiles with the Same Unique Skill Count

Problem Description:
You are given a list of employee profiles. Each profile is represented by a list of skill names,
where the same skill may appear multiple times in the same profile because of imported data errors.

For each profile, define its unique skill count as the number of distinct skill names that appear
in that profile.

Your task is to return the number of unordered pairs of profiles that have the same unique skill count.

Two profiles form a valid pair if, after removing duplicates within each individual profile, both
profiles contain the same number of distinct skills. The actual skill names do not need to match—
only the count of distinct skills matters.

Example:
["java", "sql", "java"] has unique skill count 2
["go", "python"] has unique skill count 2
These two profiles form a valid pair.

We must return the total number of such unordered pairs across all profiles.

Important note about the examples:
- Example 1's listed output says 2, but its own explanation correctly shows there are 3 valid pairs.
- Example 2's listed output says 3, but its own explanation correctly shows there are 4 valid pairs.

This solution follows the explanations, which are the logically correct results.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    Let T be the total number of skill entries across all profiles.
    - We visit every skill once while building a HashSet for each profile.
    - Dictionary operations are O(1) on average.
    Overall: O(T)

    Space Complexity:
    - HashSet for one profile can hold up to the number of distinct skills in that profile.
    - Dictionary stores how many profiles have each distinct-count value.
    Overall: O(U + K)
      where U is the maximum number of distinct skills in a single profile,
      and K is the number of different distinct-count values encountered.
    */
    public long CountPairsWithSameUniqueSkillCount(IList<IList<string>> profiles)
    {
        // This dictionary maps:
        //   unique skill count  ->  how many profiles seen so far have that count
        //
        // Example:
        // if we have already processed profiles with unique counts [2, 1, 2],
        // then the dictionary would be:
        //   1 -> 1
        //   2 -> 2
        //
        // We use a dictionary because we want fast lookup and update by count value.
        var frequencyByDistinctCount = new Dictionary<int, long>();

        // This variable will store the final answer:
        // the total number of unordered pairs of profiles that share the same unique skill count.
        long totalPairs = 0;

        // We process each profile one by one.
        // For each profile:
        //   1. Remove duplicates by putting its skills into a HashSet
        //   2. Count how many distinct skills remain
        //   3. See how many previous profiles had that same distinct count
        //   4. Add that number to the answer
        //   5. Record that this profile has now been seen
        //
        // Why does step 3 work?
        // Suppose the current profile has distinct count = 2.
        // If 5 previous profiles also had distinct count = 2,
        // then the current profile forms exactly 5 new unordered pairs:
        // one pair with each of those previous 5 profiles.
        foreach (var profile in profiles)
        {
            // A HashSet automatically keeps only unique values.
            //
            // This is exactly what we need because duplicate skills inside the same profile
            // should only be counted once.
            //
            // Example:
            // profile = ["java", "sql", "java"]
            // HashSet becomes {"java", "sql"}
            // distinct count = 2
            var uniqueSkills = new HashSet<string>();

            // Insert every skill from the current profile into the HashSet.
            // Duplicate insertions do nothing, which is perfect for this problem.
            foreach (var skill in profile)
            {
                uniqueSkills.Add(skill);
            }

            // The number of distinct skills in this profile is simply the size of the HashSet.
            int distinctCount = uniqueSkills.Count;

            // If we have already seen some profiles with this same distinct count,
            // then each of those profiles forms one new valid pair with the current profile.
            //
            // Example:
            // previous counts seen: 2 -> 3
            // current distinctCount = 2
            // then current profile creates 3 new pairs.
            if (frequencyByDistinctCount.TryGetValue(distinctCount, out long previousProfilesWithSameCount))
            {
                totalPairs += previousProfilesWithSameCount;
            }

            // Now record the current profile in the dictionary so future profiles can pair with it.
            //
            // If this distinct count has not appeared before, start at 1.
            // Otherwise, increase the existing frequency by 1.
            if (frequencyByDistinctCount.ContainsKey(distinctCount))
            {
                frequencyByDistinctCount[distinctCount]++;
            }
            else
            {
                frequencyByDistinctCount[distinctCount] = 1;
            }
        }

        // After processing all profiles, totalPairs contains the number of valid unordered pairs.
        return totalPairs;
    }
}

// ---------------------------
// Demo / sample test code
// ---------------------------

var solution = new Solution();

// Example 1
// Profiles:
// 0: ["java","sql","java"]   -> distinct skills = {"java","sql"}         -> 2
// 1: ["go","python"]         -> distinct skills = {"go","python"}         -> 2
// 2: ["aws","aws","linux"]   -> distinct skills = {"aws","linux"}         -> 2
// 3: ["c++"]                 -> distinct skills = {"c++"}                 -> 1
// 4: ["html","css","js"]     -> distinct skills = {"html","css","js"}     -> 3
//
// Distinct counts = [2, 2, 2, 1, 3]
// Count 2 appears 3 times, so it contributes C(3,2) = 3 pairs:
// (0,1), (0,2), (1,2)
//
// Correct total = 3
IList<IList<string>> profiles1 = new List<IList<string>>
{
    new List<string> { "java", "sql", "java" },
    new List<string> { "go", "python" },
    new List<string> { "aws", "aws", "linux" },
    new List<string> { "c++" },
    new List<string> { "html", "css", "js" }
};

long result1 = solution.CountPairsWithSameUniqueSkillCount(profiles1);
Console.WriteLine(result1); // Expected: 3

// Example 2
// Profiles:
// 0: ["ml","ml","ml"] -> {"ml"}         -> 1
// 1: ["sql"]          -> {"sql"}        -> 1
// 2: ["go","rust"]    -> {"go","rust"}  -> 2
// 3: ["a","b","c"]    -> {"a","b","c"}  -> 3
// 4: ["x","y"]        -> {"x","y"}      -> 2
// 5: ["k"]            -> {"k"}          -> 1
//
// Distinct counts = [1, 1, 2, 3, 2, 1]
// Count 1 appears 3 times -> C(3,2) = 3 pairs
// Count 2 appears 2 times -> C(2,2) = 1 pair
// Count 3 appears 1 time  -> 0 pairs
//
// Correct total = 4
IList<IList<string>> profiles2 = new List<IList<string>>
{
    new List<string> { "ml", "ml", "ml" },
    new List<string> { "sql" },
    new List<string> { "go", "rust" },
    new List<string> { "a", "b", "c" },
    new List<string> { "x", "y" },
    new List<string> { "k" }
};

long result2 = solution.CountPairsWithSameUniqueSkillCount(profiles2);
Console.WriteLine(result2); // Expected: 4

// Additional small sanity check
// Distinct counts:
// ["a","a"] -> 1
// ["b","c"] -> 2
// ["d"]     -> 1
// Pairs with same count: only the two profiles with count 1 -> 1 pair
IList<IList<string>> profiles3 = new List<IList<string>>
{
    new List<string> { "a", "a" },
    new List<string> { "b", "c" },
    new List<string> { "d" }
};

long result3 = solution.CountPairsWithSameUniqueSkillCount(profiles3);
Console.WriteLine(result3); // Expected: 1