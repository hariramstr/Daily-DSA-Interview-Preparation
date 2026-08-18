import java.util.*;

/*
Problem Title: Count Renamed Files by Original Content Signature

Problem Description:
You are given a list of file records from a storage migration. Each record describes one file
using two strings: its current file name and a content signature. The content signature is a
stable hash of the file contents, so two files with the same signature are guaranteed to have
identical contents even if their names differ. During migration, some files may have been
renamed multiple times, producing several records with different names but the same content
signature.

Your task is to count how many content signatures correspond to files that appear under at least
two distinct file names. In other words, for each signature, collect all file names associated
with it and determine whether there are at least two unique names in that group. Return the
number of such signatures.

Duplicate records may exist. If the exact same pair (name, signature) appears multiple times,
it should only count once toward the set of names for that signature. However, if the same
signature appears with two different names, that signature should be counted exactly once in
the final answer.

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
Signatures "x9", "a1", and "b7" each appear with at least two distinct file names, so all
three are counted.

Goal:
Design a near-linear-time solution using hash-based data structures to deduplicate records and
group names by signature efficiently.
*/

public class Solution {

    /**
     * Counts how many content signatures are associated with at least two distinct file names.
     *
     * Strategy:
     * 1. Group file names by signature using a hash map.
     * 2. For each signature, store names in a hash set so duplicate (name, signature) records
     *    are automatically ignored.
     * 3. After processing all records, count how many signatures have a name set of size >= 2.
     *
     * @param records a 2D array where each element is [fileName, signature]
     * @return the number of signatures that appear with at least two unique file names
     *
     * Time complexity: O(n) average, where n is the number of records, because each record
     * is processed with average O(1) hash map / hash set operations.
     * Space complexity: O(n) in the worst case to store grouped unique names.
     */
    public int countRenamedFiles(String[][] records) {
        // This map groups all unique file names by their content signature.
        //
        // Key   -> signature
        // Value -> set of distinct file names that have this signature
        //
        // Why a Set?
        // Because duplicate records like ["notes.txt", "h2"] appearing multiple times
        // should only count once for that signature.
        Map<String, Set<String>> signatureToNames = new HashMap<>();

        // Process every record one by one.
        for (String[] record : records) {
            // Each record is expected to contain exactly two strings:
            // record[0] = file name
            // record[1] = content signature
            String fileName = record[0];
            String signature = record[1];

            // If this signature has not been seen before, create a new empty set for it.
            //
            // computeIfAbsent:
            // - checks whether the key exists
            // - if not, creates and stores a new HashSet
            // - returns the existing or newly created set
            Set<String> namesForSignature = signatureToNames.computeIfAbsent(signature, k -> new HashSet<>());

            // Add the file name to the set.
            //
            // Important:
            // If the exact same (fileName, signature) pair appears again,
            // HashSet will ignore the duplicate automatically.
            namesForSignature.add(fileName);
        }

        // Now count how many signatures have at least two distinct names.
        int renamedSignatureCount = 0;

        // Iterate through every grouped set of names.
        for (Set<String> names : signatureToNames.values()) {
            // If a signature maps to 2 or more unique names,
            // that means the same content appeared under multiple names.
            if (names.size() >= 2) {
                renamedSignatureCount++;
            }
        }

        return renamedSignatureCount;
    }

    /**
     * An optimized variant that avoids storing all names once a signature is already known
     * to have at least two distinct names.
     *
     * Idea:
     * - For each signature, store only:
     *   1. the first name seen for that signature
     *   2. whether we have already confirmed a second distinct name
     *
     * This is more memory-efficient than storing a full set of names, while still correctly
     * handling duplicate records.
     *
     * @param records a 2D array where each element is [fileName, signature]
     * @return the number of signatures that appear with at least two unique file names
     *
     * Time complexity: O(n) average, where n is the number of records.
     * Space complexity: O(s), where s is the number of distinct signatures.
     */
    public int countRenamedFilesOptimized(String[][] records) {
        // For each signature, remember the first file name we ever saw.
        Map<String, String> firstNameBySignature = new HashMap<>();

        // Keep track of signatures that are already confirmed to have
        // at least two distinct names.
        Set<String> countedSignatures = new HashSet<>();

        // Process each record.
        for (String[] record : records) {
            String fileName = record[0];
            String signature = record[1];

            // If this signature is already confirmed, we do not need to do more work.
            // It will contribute exactly once to the final answer no matter how many
            // additional names or duplicate records appear later.
            if (countedSignatures.contains(signature)) {
                continue;
            }

            // If this is the first time we see the signature, store its first file name.
            if (!firstNameBySignature.containsKey(signature)) {
                firstNameBySignature.put(signature, fileName);
            } else {
                // We have seen this signature before.
                // Compare the current file name with the first recorded file name.
                //
                // If they are different, then this signature has at least two distinct names,
                // so it must be counted.
                //
                // If they are the same, it is just a duplicate record and should not count.
                String firstName = firstNameBySignature.get(signature);
                if (!firstName.equals(fileName)) {
                    countedSignatures.add(signature);
                }
            }
        }

        return countedSignatures.size();
    }

    /**
     * Utility method to print a 2D string array in a readable format.
     *
     * @param records the 2D array of file records
     * @return a human-readable string representation of the records
     *
     * Time complexity: O(n), where n is the number of records.
     * Space complexity: O(n) for building the output string.
     */
    public String recordsToString(String[][] records) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < records.length; i++) {
            sb.append(Arrays.toString(records[i]));
            if (i < records.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(1) for the fixed demonstration size shown here.
     * Space complexity: O(1) excluding the sample input storage.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Input 1
        String[][] records1 = {
            {"report_v1.pdf", "h1"},
            {"report_final.pdf", "h1"},
            {"notes.txt", "h2"},
            {"notes.txt", "h2"},
            {"summary.txt", "h3"}
        };

        // Expected:
        // h1 -> {"report_v1.pdf", "report_final.pdf"} => count
        // h2 -> {"notes.txt"} => do not count
        // h3 -> {"summary.txt"} => do not count
        // Final answer = 1
        int result1 = solution.countRenamedFiles(records1);
        int result1Optimized = solution.countRenamedFilesOptimized(records1);

        System.out.println("Sample 1 Records: " + solution.recordsToString(records1));
        System.out.println("Sample 1 Result (set-based): " + result1);
        System.out.println("Sample 1 Result (optimized): " + result1Optimized);
        System.out.println("Expected: 1");
        System.out.println();

        // Sample Input 2
        String[][] records2 = {
            {"img001.png", "x9"},
            {"vacation.png", "x9"},
            {"draft.doc", "a1"},
            {"draft_v2.doc", "a1"},
            {"draft.doc", "a1"},
            {"todo.md", "b7"},
            {"todo_backup.md", "b7"}
        };

        // Expected:
        // x9 -> {"img001.png", "vacation.png"} => count
        // a1 -> {"draft.doc", "draft_v2.doc"} => count
        // b7 -> {"todo.md", "todo_backup.md"} => count
        // Final answer = 3
        int result2 = solution.countRenamedFiles(records2);
        int result2Optimized = solution.countRenamedFilesOptimized(records2);

        System.out.println("Sample 2 Records: " + solution.recordsToString(records2));
        System.out.println("Sample 2 Result (set-based): " + result2);
        System.out.println("Sample 2 Result (optimized): " + result2Optimized);
        System.out.println("Expected: 3");
        System.out.println();

        // Additional quick sanity check:
        // All records are exact duplicates, so no signature has two distinct names.
        String[][] records3 = {
            {"a.txt", "sig1"},
            {"a.txt", "sig1"},
            {"b.txt", "sig2"},
            {"b.txt", "sig2"}
        };

        int result3 = solution.countRenamedFiles(records3);
        int result3Optimized = solution.countRenamedFilesOptimized(records3);

        System.out.println("Sanity Check Records: " + solution.recordsToString(records3));
        System.out.println("Sanity Check Result (set-based): " + result3);
        System.out.println("Sanity Check Result (optimized): " + result3Optimized);
        System.out.println("Expected: 0");
    }
}