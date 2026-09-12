import java.util.*;

/*
 * Title: Verify Unique Employee Extension Mapping
 * Difficulty: Easy
 * Topic: Hashing
 *
 * Problem Description:
 * A company stores employee phone extensions in a list of records. Each record contains
 * an employee name and a numeric extension. Due to synchronization bugs between internal
 * tools, the same employee may appear multiple times, and different employees might
 * accidentally be assigned the same extension.
 *
 * You are given two arrays of equal length: names and extensions, where names[i] is the
 * employee name for record i, and extensions[i] is that employee's extension.
 *
 * Return true if the records describe a valid one-to-one mapping between employee names
 * and extensions, and false otherwise.
 *
 * A mapping is valid if:
 * 1. Every occurrence of the same employee name always has the same extension.
 * 2. No two different employee names share the same extension.
 *
 * In other words, each name maps to exactly one extension, and each extension maps to
 * exactly one name.
 *
 * This is a realistic data-validation task that can be solved efficiently using hash maps
 * or hash sets.
 *
 * Constraints:
 * - 1 <= names.length == extensions.length <= 10^5
 * - 1 <= names[i].length <= 50
 * - names[i] contains only English letters, spaces, and underscores
 * - 1 <= extensions[i] <= 10^9
 *
 * Example 1:
 * Input: names = ["Alice","Bob","Alice","Cara"], extensions = [101,202,101,303]
 * Output: true
 * Explanation: Alice consistently uses 101, Bob uses 202, and Cara uses 303.
 * No extension is shared by different employees.
 *
 * Example 2:
 * Input: names = ["Alice","Bob","Alice"], extensions = [101,101,202]
 * Output: false
 * Explanation: The records are invalid for two reasons:
 * - Alice is mapped to two different extensions (101 and 202)
 * - extension 101 is shared by Alice and Bob
 */

public class Solution {

    /**
     * Verifies whether the given employee records form a valid one-to-one mapping
     * between employee names and phone extensions.
     *
     * The method checks two rules:
     * 1. A single employee name must always map to the same extension.
     * 2. A single extension must never map to two different employee names.
     *
     * We enforce these rules by maintaining:
     * - a map from name -> extension
     * - a map from extension -> name
     *
     * As we scan each record:
     * - If the name was seen before, its stored extension must match the current one.
     * - If the extension was seen before, its stored name must match the current one.
     * - Otherwise, we store the new pair in both maps.
     *
     * @param names the array of employee names, where names[i] is the name in record i
     * @param extensions the array of extensions, where extensions[i] is the extension in record i
     * @return true if the records describe a valid one-to-one mapping; false otherwise
     * Time complexity: O(n), where n is the number of records
     * Space complexity: O(n), in the worst case for the hash maps
     */
    public boolean isValidMapping(String[] names, int[] extensions) {
        // Defensive check:
        // The problem guarantees equal lengths and at least one element,
        // but adding this makes the method safer and easier to understand.
        if (names == null || extensions == null || names.length != extensions.length) {
            return false;
        }

        // This map remembers which extension each employee name should have.
        // Example: "Alice" -> 101
        Map<String, Integer> nameToExtension = new HashMap<>();

        // This map remembers which employee name owns each extension.
        // Example: 101 -> "Alice"
        Map<Integer, String> extensionToName = new HashMap<>();

        // Process every record exactly once.
        for (int i = 0; i < names.length; i++) {
            // Read the current record.
            String currentName = names[i];
            int currentExtension = extensions[i];

            // ------------------------------------------------------------
            // Step 1: Validate the name -> extension direction.
            // ------------------------------------------------------------
            // If we have already seen this employee name before,
            // then the extension must be exactly the same as before.
            if (nameToExtension.containsKey(currentName)) {
                int previouslyAssignedExtension = nameToExtension.get(currentName);

                // If the same name now points to a different extension,
                // the mapping is invalid immediately.
                if (previouslyAssignedExtension != currentExtension) {
                    return false;
                }
            } else {
                // If this is the first time we see this name,
                // store its extension.
                nameToExtension.put(currentName, currentExtension);
            }

            // ------------------------------------------------------------
            // Step 2: Validate the extension -> name direction.
            // ------------------------------------------------------------
            // If we have already seen this extension before,
            // it must belong to the same employee name.
            if (extensionToName.containsKey(currentExtension)) {
                String previouslyAssignedName = extensionToName.get(currentExtension);

                // If the same extension now points to a different name,
                // then two different employees share one extension,
                // which violates one-to-one mapping.
                if (!previouslyAssignedName.equals(currentName)) {
                    return false;
                }
            } else {
                // If this is the first time we see this extension,
                // store its owner name.
                extensionToName.put(currentExtension, currentName);
            }
        }

        // If we finish scanning all records without finding any conflict,
        // then the mapping is valid.
        return true;
    }

    /**
     * Helper method to print a test case in a beginner-friendly way.
     *
     * @param names the employee names for the test case
     * @param extensions the employee extensions for the test case
     * @param expected the expected result for comparison
     * @return the actual computed result
     * Time complexity: O(n), where n is the number of records in the test case
     * Space complexity: O(n), due to the internal validation method
     */
    public boolean runAndPrintTest(String[] names, int[] extensions, boolean expected) {
        boolean actual = isValidMapping(names, extensions);

        System.out.println("Names      : " + Arrays.toString(names));
        System.out.println("Extensions : " + Arrays.toString(extensions));
        System.out.println("Expected   : " + expected);
        System.out.println("Actual     : " + actual);
        System.out.println();

        return actual;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total number of records across all demo cases)
     * Space complexity: O(max records in a single demo case)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement:
        // Alice -> 101
        // Bob   -> 202
        // Alice -> 101 again (consistent)
        // Cara  -> 303
        // No conflicts, so the answer should be true.
        String[] names1 = {"Alice", "Bob", "Alice", "Cara"};
        int[] extensions1 = {101, 202, 101, 303};
        solution.runAndPrintTest(names1, extensions1, true);

        // Example 2 from the problem statement:
        // Alice -> 101
        // Bob   -> 101   (conflict: same extension used by different names)
        // Alice -> 202   (conflict: same name used with different extension)
        // Therefore the answer should be false.
        String[] names2 = {"Alice", "Bob", "Alice"};
        int[] extensions2 = {101, 101, 202};
        solution.runAndPrintTest(names2, extensions2, false);

        // Additional example:
        // Repeated identical records are allowed.
        String[] names3 = {"Dan", "Dan", "Dan"};
        int[] extensions3 = {404, 404, 404};
        solution.runAndPrintTest(names3, extensions3, true);

        // Additional example:
        // Two different names share one extension, which is invalid.
        String[] names4 = {"Eve", "Frank"};
        int[] extensions4 = {505, 505};
        solution.runAndPrintTest(names4, extensions4, false);
    }
}