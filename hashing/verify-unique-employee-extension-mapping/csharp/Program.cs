/*
Title: Verify Unique Employee Extension Mapping
Difficulty: Easy
Topic: Hashing

Problem Description:
A company stores employee phone extensions in a list of records. Each record contains an employee name and a numeric extension.
Due to synchronization bugs between internal tools, the same employee may appear multiple times, and different employees
might accidentally be assigned the same extension.

You are given two arrays of equal length: names and extensions, where names[i] is the employee name for record i,
and extensions[i] is that employee's extension.

Return true if the records describe a valid one-to-one mapping between employee names and extensions, and false otherwise.

A mapping is valid if:
1. Every occurrence of the same employee name always has the same extension.
2. No two different employee names share the same extension.

In other words, each name maps to exactly one extension, and each extension maps to exactly one name.

This is a realistic data-validation task that can be solved efficiently using hash maps or hash sets.

Constraints:
- 1 <= names.length == extensions.length <= 10^5
- 1 <= names[i].length <= 50
- names[i] contains only English letters, spaces, and underscores
- 1 <= extensions[i] <= 10^9

Example 1:
Input: names = ["Alice","Bob","Alice","Cara"], extensions = [101,202,101,303]
Output: true
Explanation: Alice consistently uses 101, Bob uses 202, and Cara uses 303. No extension is shared by different employees.

Example 2:
Input: names = ["Alice","Bob","Alice"], extensions = [101,101,202]
Output: false
Explanation: The records are invalid for two reasons: Alice is mapped to two different extensions (101 and 202),
and extension 101 is shared by Alice and Bob.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Explanation:
    - We scan through the input arrays exactly once.
    - We use two hash maps (Dictionary in C#):
      1. nameToExtension: ensures each employee name always maps to the same extension.
      2. extensionToName: ensures each extension is not shared by different employee names.
    - Each dictionary lookup and insertion is O(1) on average, so the full pass is O(n).
    */
    public bool IsValidMapping(string[] names, int[] extensions)
    {
        // This dictionary stores the mapping from employee name -> extension.
        // Why do we need it?
        // Because the problem says that if the same employee appears multiple times,
        // every occurrence must always have the same extension.
        //
        // Example:
        // "Alice" -> 101 is fine
        // later "Alice" -> 101 is still fine
        // but later "Alice" -> 202 is invalid
        var nameToExtension = new Dictionary<string, int>();

        // This dictionary stores the reverse mapping from extension -> employee name.
        // Why do we need a reverse mapping too?
        // Because the problem also says that no two different employees may share the same extension.
        //
        // Example:
        // 101 -> "Alice" is fine
        // later 101 -> "Alice" is still fine
        // but later 101 -> "Bob" is invalid
        var extensionToName = new Dictionary<int, string>();

        // We process every record one by one.
        // Since names[i] belongs with extensions[i], we use the same index for both arrays.
        for (int i = 0; i < names.Length; i++)
        {
            // Read the current record.
            string currentName = names[i];
            int currentExtension = extensions[i];

            // ------------------------------------------------------------
            // STEP 1: Check whether this employee name was seen before.
            // ------------------------------------------------------------
            // If we have seen this name before, then it must match the same extension
            // that was previously assigned to that name.
            if (nameToExtension.TryGetValue(currentName, out int existingExtensionForName))
            {
                // If the stored extension is different from the current one,
                // then the same employee name is being assigned two different extensions.
                // That violates rule #1, so we can immediately return false.
                if (existingExtensionForName != currentExtension)
                {
                    return false;
                }
            }
            else
            {
                // If this name has never been seen before, we record its extension.
                // This allows future occurrences of the same name to be checked for consistency.
                nameToExtension[currentName] = currentExtension;
            }

            // ------------------------------------------------------------
            // STEP 2: Check whether this extension was seen before.
            // ------------------------------------------------------------
            // If we have seen this extension before, then it must belong to the same employee name.
            // Otherwise, two different employees are sharing one extension, which is not allowed.
            if (extensionToName.TryGetValue(currentExtension, out string? existingNameForExtension))
            {
                // If the stored name is different from the current one,
                // then this extension is being shared by two different employees.
                // That violates rule #2, so we immediately return false.
                if (existingNameForExtension != currentName)
                {
                    return false;
                }
            }
            else
            {
                // If this extension has never been seen before, record which employee owns it.
                // This allows future records using the same extension to be validated.
                extensionToName[currentExtension] = currentName;
            }

            // If we reach this point, the current record is consistent in both directions:
            // - name -> extension is valid
            // - extension -> name is valid
            //
            // So we continue checking the next record.
        }

        // If we finish processing all records without finding any conflict,
        // then the mapping is a valid one-to-one relationship.
        return true;
    }
}

// Demo code:
// We create sample inputs from the problem statement,
// call the solution method, and print the results.

var solution = new Solution();

// Example 1:
// names = ["Alice","Bob","Alice","Cara"]
// extensions = [101,202,101,303]
// Expected output: true
string[] names1 = { "Alice", "Bob", "Alice", "Cara" };
int[] extensions1 = { 101, 202, 101, 303 };
bool result1 = solution.IsValidMapping(names1, extensions1);
Console.WriteLine(result1);

// Example 2:
// names = ["Alice","Bob","Alice"]
// extensions = [101,101,202]
// Expected output: false
string[] names2 = { "Alice", "Bob", "Alice" };
int[] extensions2 = { 101, 101, 202 };
bool result2 = solution.IsValidMapping(names2, extensions2);
Console.WriteLine(result2);