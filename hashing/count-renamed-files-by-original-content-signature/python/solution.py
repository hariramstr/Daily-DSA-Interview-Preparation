"""
Title: Count Renamed Files by Original Content Signature

Problem Description:
You are given a list of file records from a storage migration. Each record describes
one file using two strings: its current file name and a content signature. The content
signature is a stable hash of the file contents, so two files with the same signature
are guaranteed to have identical contents even if their names differ. During migration,
some files may have been renamed multiple times, producing several records with
different names but the same content signature.

Your task is to count how many content signatures correspond to files that appear under
at least two distinct file names. In other words, for each signature, collect all file
names associated with it and determine whether there are at least two unique names in
that group. Return the number of such signatures.

Duplicate records may exist. If the exact same pair (name, signature) appears multiple
times, it should only count once toward the set of names for that signature. However,
if the same signature appears with two different names, that signature should be counted
exactly once in the final answer.

Constraints:
- 1 <= records.length <= 200000
- Each record is a pair [name, signature]
- 1 <= name.length, signature.length <= 100
- name and signature consist of lowercase English letters, digits, '.', '_', and '-'
- The answer fits in a 32-bit signed integer

Example 1:
Input:
records = [
    ["report_v1.pdf", "h1"],
    ["report_final.pdf", "h1"],
    ["notes.txt", "h2"],
    ["notes.txt", "h2"],
    ["summary.txt", "h3"]
]
Output: 1

Example 2:
Input:
records = [
    ["img001.png", "x9"],
    ["vacation.png", "x9"],
    ["draft.doc", "a1"],
    ["draft_v2.doc", "a1"],
    ["draft.doc", "a1"],
    ["todo.md", "b7"],
    ["todo_backup.md", "b7"]
]
Output: 3
"""

from typing import Dict, List, Set


class Solution:
    def count_renamed_signatures(self, records: List[List[str]]) -> int:
        """
        Count how many content signatures are associated with at least two distinct file names.

        Args:
            records: A list where each element is [name, signature].

        Returns:
            The number of signatures that appear with at least two unique names.

        Time complexity:
            O(n), where n is the number of records, assuming average O(1) hash operations.

        Space complexity:
            O(n) in the worst case for storing unique names grouped by signature.
        """
        # This dictionary groups file names by their content signature.
        #
        # Key:
        #   signature (str)
        #
        # Value:
        #   a set of file names (Set[str]) that have been seen with that signature
        #
        # Why use a set?
        # - The problem says duplicate records may exist.
        # - If the exact same (name, signature) pair appears multiple times,
        #   it should count only once.
        # - A set automatically removes duplicates, which makes it the perfect
        #   data structure for this requirement.
        names_by_signature: Dict[str, Set[str]] = {}

        # Process every record one by one.
        for record in records:
            # Each record is guaranteed to be a pair: [name, signature].
            name, signature = record

            # If this signature has not been seen before, create an empty set for it.
            #
            # We do this so that later we can safely add the current file name
            # into the set of names for this signature.
            if signature not in names_by_signature:
                names_by_signature[signature] = set()

            # Add the file name to the set for this signature.
            #
            # Important behavior:
            # - If this exact name was already added for the same signature,
            #   the set remains unchanged.
            # - If this is a new distinct name for the signature, the set grows.
            names_by_signature[signature].add(name)

        # Now count how many signatures have at least two unique names.
        #
        # A signature should be counted if and only if:
        #   len(set_of_names_for_that_signature) >= 2
        #
        # This means the same content appeared under multiple distinct file names,
        # which matches the idea of a renamed file.
        renamed_signature_count: int = 0

        for signature, names in names_by_signature.items():
            # If there are at least two different names for this signature,
            # count this signature exactly once.
            if len(names) >= 2:
                renamed_signature_count += 1

        return renamed_signature_count


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # Signature "h1" -> {"report_v1.pdf", "report_final.pdf"} => 2 unique names => counted
    # Signature "h2" -> {"notes.txt"} => duplicate record only, still 1 unique name => not counted
    # Signature "h3" -> {"summary.txt"} => 1 unique name => not counted
    # Expected answer: 1
    records1: List[List[str]] = [
        ["report_v1.pdf", "h1"],
        ["report_final.pdf", "h1"],
        ["notes.txt", "h2"],
        ["notes.txt", "h2"],
        ["summary.txt", "h3"],
    ]
    result1: int = solution.count_renamed_signatures(records1)
    print(result1)

    # Example 2:
    # Signature "x9" -> {"img001.png", "vacation.png"} => counted
    # Signature "a1" -> {"draft.doc", "draft_v2.doc"} => counted
    # Signature "b7" -> {"todo.md", "todo_backup.md"} => counted
    # Expected answer: 3
    records2: List[List[str]] = [
        ["img001.png", "x9"],
        ["vacation.png", "x9"],
        ["draft.doc", "a1"],
        ["draft_v2.doc", "a1"],
        ["draft.doc", "a1"],
        ["todo.md", "b7"],
        ["todo_backup.md", "b7"],
    ]
    result2: int = solution.count_renamed_signatures(records2)
    print(result2)