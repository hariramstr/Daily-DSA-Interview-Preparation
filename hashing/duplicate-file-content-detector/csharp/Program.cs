/*
 * Title: Duplicate File Content Detector
 * Difficulty: Easy
 * Topic: Hashing
 *
 * Problem Description:
 * You are building a file system utility that identifies files sharing identical content.
 * You are given a list of file records, where each record is a string in the format
 * "filename:content". Two files are considered duplicates if they have exactly the same
 * content string.
 *
 * Return a list of groups, where each group contains the filenames of all files that share
 * the same content. Only include groups that have MORE than one file (i.e., actual duplicates).
 * The order of groups and the order of filenames within each group does not matter.
 *
 * Constraints:
 * - 1 <= records.length <= 1000
 * - Each record is a non-empty string in the format "filename:content"
 * - filename and content each consist of lowercase English letters, digits, and underscores only
 * - 1 <= filename.length, content.length <= 100
 * - All filenames are unique across the input
 *
 * Example 1:
 * Input: records = ["readme:hello world", "notes:foo bar", "copy_readme:hello world",
 *                   "draft:foo bar", "image:binary_data"]
 * Output: [["readme", "copy_readme"], ["notes", "draft"]]
 *
 * Example 2:
 * Input: records = ["a:xyz", "b:abc", "c:xyz", "d:xyz"]
 * Output: [["a", "c", "d"]]
 */

using System;
using System.Collections.Generic;
using System.Linq;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class containing the core algorithm
// ─────────────────────────────────────────────────────────────────────────────
class Solution
{
    /// <summary>
    /// Finds groups of files that share identical content.
    ///
    /// Time Complexity:  O(N * L) where N = number of records and L = average length
    ///                   of each record string (we scan each record once to split it).
    ///
    /// Space Complexity: O(N * L) for the dictionary that maps content strings to
    ///                   lists of filenames — in the worst case every file has a
    ///                   unique content string, so we store all N entries.
    /// </summary>
    public List<List<string>> FindDuplicates(string[] records)
    {
        // ── Step 1: Create a dictionary that maps content → list of filenames ──────
        //
        // WHY a Dictionary?
        //   A Dictionary (hash map) gives us O(1) average-time lookup and insertion.
        //   The KEY is the content string (what we want to group BY).
        //   The VALUE is a List<string> of every filename that has that content.
        //
        // This is the classic "group by" pattern: iterate once, bucket items by key.
        var contentToFiles = new Dictionary<string, List<string>>();

        // ── Step 2: Iterate over every record and parse it ────────────────────────
        //
        // Each record looks like  "filename:content"
        // We need to split on the FIRST colon only, because the content itself
        // might theoretically contain colons (the problem says it won't here, but
        // splitting with a count of 2 is the safe, idiomatic approach).
        foreach (string record in records)
        {
            // Split the record into at most 2 parts on the ':' separator.
            // parts[0] = filename   e.g. "readme"
            // parts[1] = content    e.g. "hello world"
            string[] parts = record.Split(':', 2);

            string filename = parts[0];  // left side of the colon
            string content  = parts[1];  // right side of the colon

            // ── Step 3: Check whether this content has been seen before ───────────
            //
            // If the content key does NOT yet exist in the dictionary, we create a
            // new empty list for it first (TryGetValue avoids a double-lookup).
            if (!contentToFiles.ContainsKey(content))
            {
                // First time we see this content — initialise an empty bucket.
                contentToFiles[content] = new List<string>();
            }

            // ── Step 4: Add the current filename to the appropriate bucket ─────────
            //
            // Whether the bucket was just created or already existed, we append
            // this filename so it is associated with its content group.
            contentToFiles[content].Add(filename);
        }

        // ── Step 5: Collect only the groups that have MORE than one file ──────────
        //
        // A group with only one filename means that content is unique — no duplicate.
        // We filter those out and return only the true duplicate groups.
        //
        // We use LINQ's Where + Select to:
        //   • Where  → keep only entries whose list has 2+ filenames
        //   • Select → project the KeyValuePair to just the List<string> value
        //   • ToList → materialise the result into a concrete List<List<string>>
        var result = contentToFiles
            .Where(kvp => kvp.Value.Count > 1)   // only actual duplicates
            .Select(kvp => kvp.Value)             // we only need the filename lists
            .ToList();

        // ── Step 6: Return the final answer ──────────────────────────────────────
        //
        // Each element of 'result' is a List<string> of filenames that all share
        // the same content. The problem says order doesn't matter, so we're done.
        return result;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / driver code (top-level statements — no explicit Main needed in .NET 6+)
// ─────────────────────────────────────────────────────────────────────────────

var solver = new Solution();

// ── Example 1 ─────────────────────────────────────────────────────────────────
// Expected output: groups containing ["readme","copy_readme"] and ["notes","draft"]
string[] records1 =
[
    "readme:hello world",
    "notes:foo bar",
    "copy_readme:hello world",
    "draft:foo bar",
    "image:binary_data"
];

Console.WriteLine("=== Example 1 ===");
Console.WriteLine("Input records:");
foreach (var r in records1) Console.WriteLine($"  {r}");

List<List<string>> result1 = solver.FindDuplicates(records1);

Console.WriteLine("\nDuplicate groups found:");
if (result1.Count == 0)
{
    Console.WriteLine("  (no duplicates)");
}
else
{
    foreach (var group in result1)
    {
        // Sort filenames inside the group for consistent display
        var sorted = group.OrderBy(f => f).ToList();
        Console.WriteLine($"  [ {string.Join(", ", sorted)} ]");
    }
}

// ── Example 2 ─────────────────────────────────────────────────────────────────
// Expected output: one group containing ["a","c","d"]
string[] records2 =
[
    "a:xyz",
    "b:abc",
    "c:xyz",
    "d:xyz"
];

Console.WriteLine("\n=== Example 2 ===");
Console.WriteLine("Input records:");
foreach (var r in records2) Console.WriteLine($"  {r}");

List<List<string>> result2 = solver.FindDuplicates(records2);

Console.WriteLine("\nDuplicate groups found:");
if (result2.Count == 0)
{
    Console.WriteLine("  (no duplicates)");
}
else
{
    foreach (var group in result2)
    {
        var sorted = group.OrderBy(f => f).ToList();
        Console.WriteLine($"  [ {string.Join(", ", sorted)} ]");
    }
}

// ── Edge case: no duplicates at all ───────────────────────────────────────────
string[] records3 =
[
    "file1:alpha",
    "file2:beta",
    "file3:gamma"
];

Console.WriteLine("\n=== Edge Case: No Duplicates ===");
Console.WriteLine("Input records:");
foreach (var r in records3) Console.WriteLine($"  {r}");

List<List<string>> result3 = solver.FindDuplicates(records3);

Console.WriteLine("\nDuplicate groups found:");
if (result3.Count == 0)
    Console.WriteLine("  (no duplicates) ✓ Correct — all files have unique content.");

// ── Edge case: all files share the same content ────────────────────────────────
string[] records4 =
[
    "x:same",
    "y:same",
    "z:same"
];

Console.WriteLine("\n=== Edge Case: All Files Same Content ===");
Console.WriteLine("Input records:");
foreach (var r in records4) Console.WriteLine($"  {r}");

List<List<string>> result4 = solver.FindDuplicates(records4);

Console.WriteLine("\nDuplicate groups found:");
foreach (var group in result4)
{
    var sorted = group.OrderBy(f => f).ToList();
    Console.WriteLine($"  [ {string.Join(", ", sorted)} ]");
}