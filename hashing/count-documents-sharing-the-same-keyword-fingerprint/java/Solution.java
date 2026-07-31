import java.util.*;

/*
 * Title: Count Documents Sharing the Same Keyword Fingerprint
 * Difficulty: Hard
 * Topic: Hashing
 *
 * Problem Description:
 * You are given a collection of documents. Each document is represented by a list of keywords,
 * where keywords may repeat within the same document because a term can appear multiple times.
 * Define the fingerprint of a document as the multiset of keyword frequencies, ignoring the actual
 * keyword names.
 *
 * Example:
 * ["red", "red", "blue", "green", "green"] has keyword counts:
 * red -> 2, blue -> 1, green -> 2
 * So its fingerprint is the sorted list [1, 2, 2].
 *
 * Another document:
 * ["cat", "cat", "dog", "fox", "fox"]
 * has counts:
 * cat -> 2, dog -> 1, fox -> 2
 * So its fingerprint is also [1, 2, 2].
 *
 * These two documents match because the sorted frequency lists are identical.
 *
 * Task:
 * Count how many unordered pairs of documents share the same fingerprint.
 *
 * Two documents are matching if:
 * 1. Count occurrences of each distinct keyword in each document.
 * 2. Collect those counts.
 * 3. Sort the counts.
 * 4. If the sorted count lists are identical, the documents match.
 *
 * Return the total number of matching pairs.
 *
 * Constraints:
 * - 1 <= documents.length <= 10^5
 * - 1 <= total number of keywords across all documents <= 3 * 10^5
 * - 1 <= keyword.length <= 20
 * - Keywords contain only lowercase English letters
 * - Each document contains at least 1 keyword
 *
 * Important note about the examples:
 * The formal definition is the source of truth. We must count pairs of documents whose sorted
 * frequency lists are exactly equal. For Example 1, the correct answer is 3. For Example 2,
 * tracing the formal definition gives 1 matching pair, because only the first two documents
 * share the same fingerprint [1,2,3]. The provided narrative around output 2 is inconsistent
 * with the formal definition, so this implementation follows the formal definition exactly.
 */

public class Solution {

    /**
     * Counts how many unordered pairs of documents share the same keyword fingerprint.
     *
     * A document fingerprint is built as follows:
     * 1. Count how many times each distinct keyword appears in the document.
     * 2. Take only those frequency values.
     * 3. Sort the frequencies.
     * 4. Use the sorted frequency list as the canonical representation.
     *
     * Then:
     * - Documents with the same canonical representation belong to the same group.
     * - If a group has size k, it contributes k * (k - 1) / 2 matching pairs.
     *
     * @param documents the collection of documents, where each document is an array of keywords
     * @return the total number of unordered matching document pairs
     * Time complexity: O(T + sum over documents of (u log u)), where T is the total number of
     * keywords across all documents and u is the number of distinct keywords in a document.
     * Space complexity: O(T) in the worst case for frequency maps and fingerprint storage.
     */
    public long countMatchingPairs(String[][] documents) {
        // This map groups documents by their canonical fingerprint string.
        // Key   -> fingerprint such as "1#2#2"
        // Value -> how many documents have exactly this fingerprint
        Map<String, Integer> fingerprintCount = new HashMap<>();

        // Process each document independently.
        for (String[] document : documents) {
            // Build a canonical fingerprint for the current document.
            String fingerprint = buildFingerprint(document);

            // Increase the number of documents seen with this fingerprint.
            fingerprintCount.put(fingerprint, fingerprintCount.getOrDefault(fingerprint, 0) + 1);
        }

        // Now compute the number of unordered pairs inside each fingerprint group.
        long pairs = 0L;

        for (int count : fingerprintCount.values()) {
            // If count documents share the same fingerprint,
            // the number of unordered pairs is count choose 2:
            // count * (count - 1) / 2
            pairs += (long) count * (count - 1) / 2;
        }

        return pairs;
    }

    /**
     * Builds the canonical fingerprint string for one document.
     *
     * Step-by-step:
     * 1. Count occurrences of each keyword.
     * 2. Extract the counts.
     * 3. Sort the counts.
     * 4. Join them into a single string with separators so it can be used as a hash key.
     *
     * Example:
     * document = ["red", "red", "blue", "green", "green"]
     * counts = {red=2, blue=1, green=2}
     * frequency list = [2,1,2]
     * sorted = [1,2,2]
     * fingerprint = "1#2#2"
     *
     * @param document one document represented as an array of keywords
     * @return the canonical fingerprint string for the document
     * Time complexity: O(m + d log d), where m is the number of keywords in the document and
     * d is the number of distinct keywords in the document.
     * Space complexity: O(d), where d is the number of distinct keywords in the document.
     */
    public String buildFingerprint(String[] document) {
        // Count how many times each keyword appears in this document.
        Map<String, Integer> keywordFrequency = new HashMap<>();

        // Go through every keyword in the document.
        for (String keyword : document) {
            // Increase its frequency count.
            keywordFrequency.put(keyword, keywordFrequency.getOrDefault(keyword, 0) + 1);
        }

        // We only care about the multiset of frequencies, not the keyword names.
        // So extract all frequency values into an array.
        int distinctKeywordCount = keywordFrequency.size();
        int[] frequencies = new int[distinctKeywordCount];

        int index = 0;
        for (int frequency : keywordFrequency.values()) {
            frequencies[index++] = frequency;
        }

        // Sort the frequencies so that equivalent multisets produce exactly the same order.
        Arrays.sort(frequencies);

        // Convert the sorted frequency list into a canonical string.
        // We use a separator like '#' to avoid ambiguity.
        // Example:
        // [1, 2, 2] -> "1#2#2"
        StringBuilder fingerprint = new StringBuilder();

        for (int i = 0; i < frequencies.length; i++) {
            if (i > 0) {
                fingerprint.append('#');
            }
            fingerprint.append(frequencies[i]);
        }

        return fingerprint.toString();
    }

    /**
     * Utility method to print each document together with its fingerprint.
     * This is helpful for demonstration and manual verification.
     *
     * @param documents the collection of documents
     * @return nothing
     * Time complexity: O(T + sum over documents of (u log u))
     * Space complexity: O(u) per document during fingerprint construction
     */
    public void printDocumentsAndFingerprints(String[][] documents) {
        for (int i = 0; i < documents.length; i++) {
            System.out.println("Document " + i + ": " + Arrays.toString(documents[i]));
            System.out.println("Fingerprint " + i + ": " + buildFingerprint(documents[i]));
        }
    }

    /**
     * Demonstrates the solution using the sample-style inputs and prints the results.
     *
     * Note:
     * We follow the formal definition exactly.
     * Therefore:
     * - Example 1 evaluates to 3
     * - Example 2 evaluates to 1
     * because only the first two documents share the same fingerprint [1,2,3].
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: Depends on the sample sizes; for n documents it is the same as calling
     * countMatchingPairs on those inputs.
     * Space complexity: Depends on the sample sizes.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[][] documents1 = {
            {"red", "red", "blue", "green", "green"},
            {"cat", "cat", "dog", "fox", "fox"},
            {"a", "b", "b", "c"},
            {"m", "m", "n", "n", "p"},
            {"z"}
        };

        System.out.println("Example 1:");
        solution.printDocumentsAndFingerprints(documents1);
        long result1 = solution.countMatchingPairs(documents1);
        System.out.println("Matching pairs: " + result1);
        System.out.println("Expected by formal definition: 3");
        System.out.println();

        String[][] documents2 = {
            {"aa", "bb", "aa", "cc", "cc", "cc"},
            {"x", "y", "y", "z", "z", "z"},
            {"p", "p", "q", "q", "r", "r"},
            {"k"},
            {"u", "v", "w"}
        };

        System.out.println("Example 2:");
        solution.printDocumentsAndFingerprints(documents2);
        long result2 = solution.countMatchingPairs(documents2);
        System.out.println("Matching pairs: " + result2);
        System.out.println("Expected by formal definition: 1");
        System.out.println();

        String[][] extraDemo = {
            {"apple"},
            {"dog"},
            {"x", "y"},
            {"cat", "cat", "bird"},
            {"m", "n", "n"},
            {"p", "p", "q"}
        };

        System.out.println("Extra Demo:");
        solution.printDocumentsAndFingerprints(extraDemo);
        long result3 = solution.countMatchingPairs(extraDemo);
        System.out.println("Matching pairs: " + result3);
        System.out.println("Explanation:");
        System.out.println("- [\"apple\"] and [\"dog\"] both have fingerprint [1]");
        System.out.println("- [\"cat\",\"cat\",\"bird\"], [\"m\",\"n\",\"n\"], and [\"p\",\"p\",\"q\"] all have fingerprint [1,2]");
        System.out.println("- Total pairs = 1 + 3 = 4");
    }
}