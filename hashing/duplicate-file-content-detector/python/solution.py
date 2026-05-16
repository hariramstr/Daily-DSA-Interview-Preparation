"""
Duplicate File Content Detector
================================
Difficulty: Easy
Topic: Hashing

Problem Description:
You are building a file system utility that identifies files sharing identical content.
You are given a list of file records, where each record is a string in the format
"filename:content". Two files are considered duplicates if they have exactly the same
content string.

Return a list of groups, where each group contains the filenames of all files that share
the same content. Only include groups that have more than one file (i.e., actual duplicates).
The order of groups and the order of filenames within each group does not matter.

Constraints:
- 1 <= records.length <= 1000
- Each record is a non-empty string in the format "filename:content"
- filename and content each consist of lowercase English letters, digits, and underscores only
- 1 <= filename.length, content.length <= 100
- All filenames are unique across the input

Example 1:
Input: records = ["readme:hello world", "notes:foo bar", "copy_readme:hello world",
                  "draft:foo bar", "image:binary_data"]
Output: [["readme", "copy_readme"], ["notes", "draft"]]

Example 2:
Input: records = ["a:xyz", "b:abc", "c:xyz", "d:xyz"]
Output: [["a", "c", "d"]]
"""

from collections import defaultdict
from typing import List, Dict


class Solution:
    def find_duplicate_files(self, records: List[str]) -> List[List[str]]:
        """
        Identify groups of files that share identical content.

        This method uses a hash map (dictionary) to group filenames by their
        content. After grouping, only groups with more than one file are returned,
        since those represent actual duplicates.

        Args:
            records (List[str]): A list of strings, each in the format "filename:content".

        Returns:
            List[List[str]]: A list of groups (each group is a list of filenames)
                             where every group contains files with identical content.
                             Only groups with 2 or more files are included.

        Time Complexity:
            O(n * m) where n is the number of records and m is the average length
            of each record string. Parsing each record takes O(m) time, and
            dictionary operations are O(m) on average due to hashing the content.

        Space Complexity:
            O(n * m) in the worst case, where all files have unique content and
            we store every filename and content string in the dictionary.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Create a dictionary to map content -> list of filenames
        # -----------------------------------------------------------------------
        # We use defaultdict(list) so that we don't need to check if a key exists
        # before appending. Every new content key automatically starts with an
        # empty list, making the code cleaner and more Pythonic.
        #
        # Key   = content string (e.g., "hello world")
        # Value = list of filenames that have this content (e.g., ["readme", "copy_readme"])
        content_to_files: Dict[str, List[str]] = defaultdict(list)

        # -----------------------------------------------------------------------
        # STEP 2: Parse each record and populate the dictionary
        # -----------------------------------------------------------------------
        # Each record has the format "filename:content".
        # We split on the FIRST colon only (maxsplit=1) to correctly handle
        # content strings that might themselves contain colons in other variants
        # of this problem. This is a safe and robust parsing strategy.
        for record in records:
            # Split the record into exactly two parts: filename and content
            # Example: "readme:hello world" -> filename="readme", content="hello world"
            filename, content = record.split(":", maxsplit=1)

            # Append this filename to the list associated with its content.
            # If this content hasn't been seen before, defaultdict creates a new
            # empty list automatically, then we append to it.
            content_to_files[content].append(filename)

        # -----------------------------------------------------------------------
        # STEP 3: Filter and collect only groups with more than one file
        # -----------------------------------------------------------------------
        # We iterate over all content -> filenames mappings.
        # A group is a "duplicate group" only if it has 2 or more filenames,
        # meaning multiple files share the same content.
        # Single-file groups are skipped because they have no duplicates.
        result: List[List[str]] = []

        for content, filenames in content_to_files.items():
            # Check if this content is shared by more than one file
            if len(filenames) > 1:
                # This is a duplicate group — add it to our result
                # We add the list of filenames as a group
                result.append(filenames)

        # -----------------------------------------------------------------------
        # STEP 4: Return the result
        # -----------------------------------------------------------------------
        # The result is a list of groups, where each group is a list of filenames
        # that all share the same content. The problem states order doesn't matter.
        return result


# =============================================================================
# MAIN BLOCK: Demonstrate and verify the solution with provided examples
# =============================================================================
if __name__ == "__main__":
    # Create an instance of our Solution class
    solution = Solution()

    # -------------------------------------------------------------------------
    # Example 1:
    # Input: ["readme:hello world", "notes:foo bar", "copy_readme:hello world",
    #         "draft:foo bar", "image:binary_data"]
    # Expected Output: [["readme", "copy_readme"], ["notes", "draft"]]
    #
    # Trace:
    #   - "readme:hello world"      -> content_to_files["hello world"] = ["readme"]
    #   - "notes:foo bar"           -> content_to_files["foo bar"] = ["notes"]
    #   - "copy_readme:hello world" -> content_to_files["hello world"] = ["readme", "copy_readme"]
    #   - "draft:foo bar"           -> content_to_files["foo bar"] = ["notes", "draft"]
    #   - "image:binary_data"       -> content_to_files["binary_data"] = ["image"]
    #
    #   Filter groups with len > 1:
    #   - "hello world" -> ["readme", "copy_readme"]  ✓ (2 files)
    #   - "foo bar"     -> ["notes", "draft"]          ✓ (2 files)
    #   - "binary_data" -> ["image"]                   ✗ (only 1 file, excluded)
    #
    #   Result: [["readme", "copy_readme"], ["notes", "draft"]]  ✓ Matches expected!
    # -------------------------------------------------------------------------
    records1 = [
        "readme:hello world",
        "notes:foo bar",
        "copy_readme:hello world",
        "draft:foo bar",
        "image:binary_data"
    ]
    result1 = solution.find_duplicate_files(records1)
    print("Example 1:")
    print(f"  Input:    {records1}")
    print(f"  Output:   {result1}")
    print(f"  Expected: [['readme', 'copy_readme'], ['notes', 'draft']]")
    print()

    # -------------------------------------------------------------------------
    # Example 2:
    # Input: ["a:xyz", "b:abc", "c:xyz", "d:xyz"]
    # Expected Output: [["a", "c", "d"]]
    #
    # Trace:
    #   - "a:xyz" -> content_to_files["xyz"] = ["a"]
    #   - "b:abc" -> content_to_files["abc"] = ["b"]
    #   - "c:xyz" -> content_to_files["xyz"] = ["a", "c"]
    #   - "d:xyz" -> content_to_files["xyz"] = ["a", "c", "d"]
    #
    #   Filter groups with len > 1:
    #   - "xyz" -> ["a", "c", "d"]  ✓ (3 files)
    #   - "abc" -> ["b"]            ✗ (only 1 file, excluded)
    #
    #   Result: [["a", "c", "d"]]  ✓ Matches expected!
    # -------------------------------------------------------------------------
    records2 = ["a:xyz", "b:abc", "c:xyz", "d:xyz"]
    result2 = solution.find_duplicate_files(records2)
    print("Example 2:")
    print(f"  Input:    {records2}")
    print(f"  Output:   {result2}")
    print(f"  Expected: [['a', 'c', 'd']]")
    print()

    # -------------------------------------------------------------------------
    # Edge Case: All files have unique content (no duplicates)
    # Expected Output: []
    # -------------------------------------------------------------------------
    records3 = ["file1:alpha", "file2:beta", "file3:gamma"]
    result3 = solution.find_duplicate_files(records3)
    print("Edge Case (all unique):")
    print(f"  Input:    {records3}")
    print(f"  Output:   {result3}")
    print(f"  Expected: []")
    print()

    # -------------------------------------------------------------------------
    # Edge Case: Only one record (can't have duplicates)
    # Expected Output: []
    # -------------------------------------------------------------------------
    records4 = ["only_file:some_content"]
    result4 = solution.find_duplicate_files(records4)
    print("Edge Case (single record):")
    print(f"  Input:    {records4}")
    print(f"  Output:   {result4}")
    print(f"  Expected: []")
    print()

    # -------------------------------------------------------------------------
    # Edge Case: All files share the same content
    # Expected Output: [["f1", "f2", "f3", "f4"]]
    # -------------------------------------------------------------------------
    records5 = ["f1:same", "f2:same", "f3:same", "f4:same"]
    result5 = solution.find_duplicate_files(records5)
    print("Edge Case (all same content):")
    print(f"  Input:    {records5}")
    print(f"  Output:   {result5}")
    print(f"  Expected: [['f1', 'f2', 'f3', 'f4']]")