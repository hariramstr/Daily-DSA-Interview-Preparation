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
 * the same content. Only include groups that have more than one file (i.e., actual duplicates).
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

import java.util.*;

/**
 * Solution class for the Duplicate File Content Detector problem.
 * Uses a HashMap to group filenames by their content, then filters
 * groups that contain more than one file (actual duplicates).
 */
public class Solution {

    /**
     * Finds groups of files that share identical content.
     *
     * <p>Algorithm Overview:
     * 1. Parse each record to extract filename and content.
     * 2. Use a HashMap where the key is the content string and the value
     *    is a list of filenames that have that content.
     * 3. After processing all records, collect only those lists that have
     *    more than one filename (true duplicates).
     *
     * @param records An array of strings, each in the format "filename:content"
     * @return A list of groups (each group is a List<String> of filenames)
     *         where each group contains files with identical content.
     *         Only groups with 2 or more files are included.
     *
     * Time Complexity:  O(n * m) where n = number of records and m = average
     *                   length of each record string (for parsing and hashing).
     * Space Complexity: O(n * m) for storing all filenames and content strings
     *                   in the HashMap.
     */
    public List<List<String>> findDuplicates(String[] records) {

        // Step 1: Create a HashMap to map content -> list of filenames with that content.
        // Key:   the content string (e.g., "hello world")
        // Value: a list of filenames that have this exact content (e.g., ["readme", "copy_readme"])
        Map<String, List<String>> contentToFiles = new HashMap<>();

        // Step 2: Iterate over every record in the input array.
        for (String record : records) {

            // Step 3: Each record has the format "filename:content".
            // We find the index of the FIRST colon to split filename from content.
            // Using indexOf(':') handles the case where content itself might contain colons,
            // though per the constraints content only has letters, digits, and underscores.
            int colonIndex = record.indexOf(':');

            // Step 4: Extract the filename — everything BEFORE the colon.
            // Example: "readme:hello world" -> filename = "readme"
            String filename = record.substring(0, colonIndex);

            // Step 5: Extract the content — everything AFTER the colon.
            // Example: "readme:hello world" -> content = "hello world"
            String content = record.substring(colonIndex + 1);

            // Step 6: Add this filename to the list associated with this content in the map.
            // If the content key doesn't exist yet, create a new ArrayList for it first.
            // computeIfAbsent is a clean Java idiom: if the key is absent, it creates
            // a new ArrayList; then we add the filename to whichever list is returned.
            contentToFiles.computeIfAbsent(content, k -> new ArrayList<>()).add(filename);

            // After processing "readme:hello world":
            //   contentToFiles = {"hello world": ["readme"]}
            // After processing "copy_readme:hello world":
            //   contentToFiles = {"hello world": ["readme", "copy_readme"]}
        }

        // Step 7: Build the result list.
        // We only want groups where MORE THAN ONE file shares the same content.
        List<List<String>> result = new ArrayList<>();

        // Step 8: Iterate over all entries in the map.
        for (Map.Entry<String, List<String>> entry : contentToFiles.entrySet()) {

            // Step 9: Get the list of filenames for this content.
            List<String> fileGroup = entry.getValue();

            // Step 10: Only add this group to the result if it has 2 or more filenames.
            // A group with only 1 file means that file has unique content — not a duplicate.
            if (fileGroup.size() > 1) {
                result.add(fileGroup);
            }
        }

        // Step 11: Return the final list of duplicate groups.
        return result;
    }

    /**
     * Main method to demonstrate the Duplicate File Content Detector solution
     * with the sample inputs provided in the problem description.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {

        // Create an instance of Solution to call the non-static method.
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // Input: ["readme:hello world", "notes:foo bar", "copy_readme:hello world",
        //         "draft:foo bar", "image:binary_data"]
        // Expected Output: [["readme", "copy_readme"], ["notes", "draft"]]
        // -----------------------------------------------------------------------
        System.out.println("=== Example 1 ===");
        String[] records1 = {
            "readme:hello world",
            "notes:foo bar",
            "copy_readme:hello world",
            "draft:foo bar",
            "image:binary_data"
        };

        List<List<String>> result1 = solution.findDuplicates(records1);

        System.out.println("Input records:");
        for (String r : records1) {
            System.out.println("  " + r);
        }
        System.out.println("Duplicate groups found: " + result1.size());
        for (List<String> group : result1) {
            System.out.println("  Group: " + group);
        }
        // Trace:
        // "readme:hello world"       -> contentToFiles: {"hello world": ["readme"]}
        // "notes:foo bar"            -> contentToFiles: {"hello world": ["readme"], "foo bar": ["notes"]}
        // "copy_readme:hello world"  -> contentToFiles: {"hello world": ["readme","copy_readme"], "foo bar": ["notes"]}
        // "draft:foo bar"            -> contentToFiles: {"hello world": ["readme","copy_readme"], "foo bar": ["notes","draft"]}
        // "image:binary_data"        -> contentToFiles: {"hello world": ["readme","copy_readme"], "foo bar": ["notes","draft"], "binary_data": ["image"]}
        // Filter groups with size > 1:
        //   "hello world" -> ["readme", "copy_readme"]  (size 2, included)
        //   "foo bar"     -> ["notes", "draft"]          (size 2, included)
        //   "binary_data" -> ["image"]                   (size 1, excluded)
        // Result: [["readme", "copy_readme"], ["notes", "draft"]]  ✓

        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2:
        // Input: ["a:xyz", "b:abc", "c:xyz", "d:xyz"]
        // Expected Output: [["a", "c", "d"]]
        // -----------------------------------------------------------------------
        System.out.println("=== Example 2 ===");
        String[] records2 = {
            "a:xyz",
            "b:abc",
            "c:xyz",
            "d:xyz"
        };

        List<List<String>> result2 = solution.findDuplicates(records2);

        System.out.println("Input records:");
        for (String r : records2) {
            System.out.println("  " + r);
        }
        System.out.println("Duplicate groups found: " + result2.size());
        for (List<String> group : result2) {
            System.out.println("  Group: " + group);
        }
        // Trace:
        // "a:xyz" -> contentToFiles: {"xyz": ["a"]}
        // "b:abc" -> contentToFiles: {"xyz": ["a"], "abc": ["b"]}
        // "c:xyz" -> contentToFiles: {"xyz": ["a","c"], "abc": ["b"]}
        // "d:xyz" -> contentToFiles: {"xyz": ["a","c","d"], "abc": ["b"]}
        // Filter groups with size > 1:
        //   "xyz" -> ["a", "c", "d"]  (size 3, included)
        //   "abc" -> ["b"]            (size 1, excluded)
        // Result: [["a", "c", "d"]]  ✓

        System.out.println();

        // -----------------------------------------------------------------------
        // Example 3: Edge case — all files have unique content (no duplicates)
        // -----------------------------------------------------------------------
        System.out.println("=== Example 3 (Edge Case: No Duplicates) ===");
        String[] records3 = {
            "file1:alpha",
            "file2:beta",
            "file3:gamma"
        };

        List<List<String>> result3 = solution.findDuplicates(records3);

        System.out.println("Input records:");
        for (String r : records3) {
            System.out.println("  " + r);
        }
        System.out.println("Duplicate groups found: " + result3.size());
        if (result3.isEmpty()) {
            System.out.println("  (No duplicate groups — all files have unique content)");
        }

        System.out.println();

        // -----------------------------------------------------------------------
        // Example 4: Edge case — all files share the same content
        // -----------------------------------------------------------------------
        System.out.println("=== Example 4 (Edge Case: All Files Identical) ===");
        String[] records4 = {
            "doc1:same_content",
            "doc2:same_content",
            "doc3:same_content"
        };

        List<List<String>> result4 = solution.findDuplicates(records4);

        System.out.println("Input records:");
        for (String r : records4) {
            System.out.println("  " + r);
        }
        System.out.println("Duplicate groups found: " + result4.size());
        for (List<String> group : result4) {
            System.out.println("  Group: " + group);
        }
        // Expected: [["doc1", "doc2", "doc3"]]
    }
}