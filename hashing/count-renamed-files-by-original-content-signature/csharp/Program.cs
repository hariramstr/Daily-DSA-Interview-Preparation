/*
Title: Count Renamed Files by Original Content Signature
Difficulty: Medium
Topic: Hashing

Problem Description:
You are given a list of file records from a storage migration. Each record describes one file using two strings:
its current file name and a content signature. The content signature is a stable hash of the file contents,
so two files with the same signature are guaranteed to have identical contents even if their names differ.
During migration, some files may have been renamed multiple times, producing several records with different
names but the same content signature.

Your task is to count how many content signatures correspond to files that appear under at least two distinct
file names. In other words, for each signature, collect all file names associated with it and determine whether
there are at least two unique names in that group. Return the number of such signatures.

Duplicate records may exist. If the exact same pair (name, signature) appears multiple times, it should only
count once toward the set of names for that signature. However, if the same signature appears with two different
names, that signature should be counted exactly once in the final answer.

Constraints:
- 1 <= records.length <= 200000
- Each record is a pair [name, signature]
- 1 <= name.length, signature.length <= 100
- name and signature consist of lowercase English letters, digits, '.', '_', and '-'
- The answer fits in a 32-bit signed integer

Example 1:
Input:
records = [
    ["report_v1.pdf","h1"],
    ["report_final.pdf","h1"],
    ["notes.txt","h2"],
    ["notes.txt","h2"],
    ["summary.txt","h3"]
]
Output: 1

Explanation:
Signature "h1" is associated with two distinct names: "report_v1.pdf" and "report_final.pdf".
Signature "h2" only has one unique name despite duplicate records, and "h3" also has one.
So the answer is 1.

Example 2:
Input:
records = [
    ["img001.png","x9"],
    ["vacation.png","x9"],
    ["draft.doc","a1"],
    ["draft_v2.doc","a1"],
    ["draft.doc","a1"],
    ["todo.md","b7"],
    ["todo_backup.md","b7"]
]
Output: 3

Explanation:
Signatures "x9", "a1", and "b7" each appear with at least two distinct file names, so all three are counted.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n) average time, where n is the number of records.
    Explanation:
    - We process each record once.
    - Dictionary lookups are O(1) average.
    - HashSet insertions are O(1) average.
    So the full pass is near-linear.

    Space Complexity:
    O(n) in the worst case.
    Explanation:
    - In the worst case, every (signature, name) pair is unique.
    - We may store all unique names grouped by signature.
    */
    public int CountRenamedSignatures(IList<IList<string>> records)
    {
        // This dictionary is the core data structure of the solution.
        //
        // Key   = content signature
        // Value = set of unique file names seen for that signature
        //
        // Why use Dictionary<string, HashSet<string>>?
        // - We need to group records by signature.
        // - For each signature, we must count DISTINCT names only.
        // - A HashSet automatically removes duplicates, which is exactly what we need
        //   for repeated identical records like ["notes.txt", "h2"] appearing multiple times.
        var namesBySignature = new Dictionary<string, HashSet<string>>();

        // We iterate through every record exactly once.
        // Each record should contain:
        // - record[0] = file name
        // - record[1] = content signature
        //
        // During this loop, we build the grouping:
        // signature -> all unique names associated with that signature
        foreach (var record in records)
        {
            // Read the current file name and signature from the record.
            string name = record[0];
            string signature = record[1];

            // If this signature has not been seen before,
            // we must create a new HashSet for it.
            //
            // Why is this necessary?
            // Because every signature needs its own independent collection
            // of unique names.
            if (!namesBySignature.ContainsKey(signature))
            {
                namesBySignature[signature] = new HashSet<string>();
            }

            // Add the current name into the set for this signature.
            //
            // Important behavior:
            // - If this exact (name, signature) pair appears multiple times,
            //   HashSet will keep only one copy of the name.
            // - If the same signature appears with a different name,
            //   the set size will grow, which is exactly how we detect renames.
            namesBySignature[signature].Add(name);
        }

        // This variable will store the final answer:
        // the number of signatures that have at least two DISTINCT names.
        int count = 0;

        // Now that grouping is complete, we inspect each signature's set of names.
        foreach (var entry in namesBySignature)
        {
            // entry.Key   = signature
            // entry.Value = HashSet<string> of unique names for that signature
            //
            // If there are 2 or more unique names, then this signature corresponds
            // to a file that appeared under multiple names.
            if (entry.Value.Count >= 2)
            {
                count++;
            }
        }

        // Return the total number of qualifying signatures.
        return count;
    }
}

// Demo code

var solution = new Solution();

// Example 1
var records1 = new List<IList<string>>
{
    new List<string> { "report_v1.pdf", "h1" },
    new List<string> { "report_final.pdf", "h1" },
    new List<string> { "notes.txt", "h2" },
    new List<string> { "notes.txt", "h2" },
    new List<string> { "summary.txt", "h3" }
};

int result1 = solution.CountRenamedSignatures(records1);
Console.WriteLine(result1); // Expected: 1

// Example 2
var records2 = new List<IList<string>>
{
    new List<string> { "img001.png", "x9" },
    new List<string> { "vacation.png", "x9" },
    new List<string> { "draft.doc", "a1" },
    new List<string> { "draft_v2.doc", "a1" },
    new List<string> { "draft.doc", "a1" },
    new List<string> { "todo.md", "b7" },
    new List<string> { "todo_backup.md", "b7" }
};

int result2 = solution.CountRenamedSignatures(records2);
Console.WriteLine(result2); // Expected: 3

// Additional quick sanity check:
// Same signature, same name repeated many times -> should not count
var records3 = new List<IList<string>>
{
    new List<string> { "file.txt", "sig1" },
    new List<string> { "file.txt", "sig1" },
    new List<string> { "file.txt", "sig1" }
};

int result3 = solution.CountRenamedSignatures(records3);
Console.WriteLine(result3); // Expected: 0