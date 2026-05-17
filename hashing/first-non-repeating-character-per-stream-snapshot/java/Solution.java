/*
 * Title: First Non-Repeating Character Per Stream Snapshot
 *
 * Problem Description:
 * You are given a string `stream` representing a sequence of characters arriving one at a time.
 * After processing each character, you must record the first non-repeating character seen so far
 * in the stream. If no such character exists at that point, record '#' instead.
 *
 * Return a string `result` of the same length as `stream`, where `result[i]` is the first
 * non-repeating character after processing the first i+1 characters of `stream`.
 *
 * A character is considered non-repeating if it has appeared exactly once in the portion
 * of the stream processed so far.
 *
 * Constraints:
 * - 1 <= stream.length <= 10^5
 * - stream consists of only lowercase English letters.
 *
 * Example 1:
 * Input: stream = "aabccb"
 * Output: "a#bbb#"
 *
 * Example 2:
 * Input: stream = "abcd"
 * Output: "aaaa"
 */

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Solution class for the "First Non-Repeating Character Per Stream Snapshot" problem.
 *
 * <p>Key Insight:
 * We need to efficiently track:
 * 1. How many times each character has appeared (frequency count)
 * 2. The ORDER in which characters first appeared (so we can find the FIRST non-repeating one)
 *
 * <p>We use a LinkedHashMap to maintain insertion order, which lets us iterate
 * through characters in the order they first appeared and quickly find the
 * first one with a count of exactly 1.
 */
public class Solution {

    /**
     * Computes the first non-repeating character snapshot after each character is processed.
     *
     * <p>Algorithm Overview:
     * - Maintain a frequency map (how many times each char has been seen).
     * - Maintain a LinkedHashMap to preserve insertion order of characters.
     * - After each character is added, scan the LinkedHashMap in insertion order
     *   to find the first character with frequency == 1.
     *
     * @param stream the input string representing the character stream
     * @return a string of the same length where each position holds the first
     *         non-repeating character at that point, or '#' if none exists
     *
     * Time Complexity:  O(n * 26) = O(n) — for each of the n characters, we scan
     *                   at most 26 distinct lowercase letters in the LinkedHashMap.
     * Space Complexity: O(26) = O(1) — the maps hold at most 26 entries (lowercase letters only).
     */
    public String firstNonRepeatingChar(String stream) {

        // -----------------------------------------------------------------------
        // Step 1: Initialize data structures
        // -----------------------------------------------------------------------

        // frequencyMap tracks how many times each character has appeared so far.
        // Key = character, Value = count
        Map<Character, Integer> frequencyMap = new HashMap<>();

        // orderMap is a LinkedHashMap — it remembers the ORDER in which keys were
        // first inserted. This is crucial: when we want the FIRST non-repeating
        // character, we iterate this map from the beginning (earliest insertion).
        // Key = character (first-seen order), Value = count (same as frequencyMap)
        // We'll use this as our ordered structure to find the answer quickly.
        LinkedHashMap<Character, Integer> orderMap = new LinkedHashMap<>();

        // StringBuilder to build the result string efficiently
        StringBuilder result = new StringBuilder();

        // -----------------------------------------------------------------------
        // Step 2: Process each character in the stream one by one
        // -----------------------------------------------------------------------

        for (int i = 0; i < stream.length(); i++) {

            // Get the current character arriving in the stream
            char currentChar = stream.charAt(i);

            // -------------------------------------------------------------------
            // Step 2a: Update the frequency of the current character
            // -------------------------------------------------------------------

            // getOrDefault returns the current count, or 0 if not seen before
            int newCount = frequencyMap.getOrDefault(currentChar, 0) + 1;

            // Update the frequency map with the new count
            frequencyMap.put(currentChar, newCount);

            // -------------------------------------------------------------------
            // Step 2b: Update the orderMap (LinkedHashMap)
            // If the character is new, it gets inserted at the END of the linked list
            // (preserving insertion order). If it already exists, we just update its count.
            // LinkedHashMap.put() does NOT change the position of an existing key —
            // it stays in its original insertion position. This is exactly what we want!
            // -------------------------------------------------------------------
            orderMap.put(currentChar, newCount);

            // -------------------------------------------------------------------
            // Step 2c: Find the first non-repeating character
            // Iterate through orderMap in insertion order (earliest character first).
            // The first entry with value == 1 is our answer.
            // -------------------------------------------------------------------

            char firstNonRepeating = '#'; // Default: no non-repeating character found

            for (Map.Entry<Character, Integer> entry : orderMap.entrySet()) {
                if (entry.getValue() == 1) {
                    // Found the first character that has appeared exactly once
                    firstNonRepeating = entry.getKey();
                    break; // No need to look further
                }
            }

            // -------------------------------------------------------------------
            // Step 2d: Append the result for this position
            // -------------------------------------------------------------------
            result.append(firstNonRepeating);
        }

        // -----------------------------------------------------------------------
        // Step 3: Return the complete result string
        // -----------------------------------------------------------------------
        return result.toString();
    }

    /**
     * Alternative approach using a plain int array for frequency and a separate
     * character array to track insertion order. This avoids Map overhead entirely.
     *
     * @param stream the input string representing the character stream
     * @return a string of the same length where each position holds the first
     *         non-repeating character at that point, or '#' if none exists
     *
     * Time Complexity:  O(n * 26) = O(n) — same reasoning as above.
     * Space Complexity: O(26) = O(1) — arrays of fixed size 26.
     */
    public String firstNonRepeatingCharArray(String stream) {

        // -----------------------------------------------------------------------
        // Step 1: Initialize arrays for frequency and insertion order
        // -----------------------------------------------------------------------

        // freq[c - 'a'] = number of times character c has appeared
        int[] freq = new int[26];

        // insertionOrder stores characters in the order they FIRST appeared.
        // We'll fill this as we encounter new characters.
        char[] insertionOrder = new char[26];

        // How many distinct characters have we seen so far?
        int distinctCount = 0;

        // StringBuilder for the result
        StringBuilder result = new StringBuilder();

        // -----------------------------------------------------------------------
        // Step 2: Process each character
        // -----------------------------------------------------------------------

        for (int i = 0; i < stream.length(); i++) {

            char c = stream.charAt(i);
            int idx = c - 'a'; // Map 'a'->0, 'b'->1, ..., 'z'->25

            // -------------------------------------------------------------------
            // Step 2a: If this character is brand new (freq was 0), record it
            //          in our insertion order array
            // -------------------------------------------------------------------
            if (freq[idx] == 0) {
                insertionOrder[distinctCount] = c;
                distinctCount++;
            }

            // -------------------------------------------------------------------
            // Step 2b: Increment the frequency
            // -------------------------------------------------------------------
            freq[idx]++;

            // -------------------------------------------------------------------
            // Step 2c: Scan insertionOrder from the beginning to find the first
            //          character with frequency == 1
            // -------------------------------------------------------------------
            char firstNonRepeating = '#';

            for (int j = 0; j < distinctCount; j++) {
                if (freq[insertionOrder[j] - 'a'] == 1) {
                    firstNonRepeating = insertionOrder[j];
                    break;
                }
            }

            // -------------------------------------------------------------------
            // Step 2d: Record the answer for this position
            // -------------------------------------------------------------------
            result.append(firstNonRepeating);
        }

        return result.toString();
    }

    /**
     * Main method to demonstrate and verify the solution with sample inputs.
     *
     * <p>Traces through the examples from the problem description to confirm correctness.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1: stream = "aabccb"
        // Expected output: "a#bbb#"
        //
        // Trace:
        // i=0, char='a': freq={a:1}, orderMap={a:1} -> first non-rep='a'  -> result="a"
        // i=1, char='a': freq={a:2}, orderMap={a:2} -> first non-rep='#'  -> result="a#"
        // i=2, char='b': freq={a:2,b:1}, orderMap={a:2,b:1} -> first='b'  -> result="a#b"
        // i=3, char='c': freq={a:2,b:1,c:1}, orderMap={a:2,b:1,c:1} -> first='b' -> result="a#bb"
        // i=4, char='c': freq={a:2,b:1,c:2}, orderMap={a:2,b:1,c:2} -> first='b' -> result="a#bbb"
        // i=5, char='b': freq={a:2,b:2,c:2}, orderMap={a:2,b:2,c:2} -> first='#' -> result="a#bbb#"
        // -----------------------------------------------------------------------
        String stream1 = "aabccb";
        String result1 = solution.firstNonRepeatingChar(stream1);
        System.out.println("=== Example 1 ===");
        System.out.println("Input:    \"" + stream1 + "\"");
        System.out.println("Output:   \"" + result1 + "\"");
        System.out.println("Expected: \"a#bbb#\"");
        System.out.println("Correct:  " + result1.equals("a#bbb#"));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2: stream = "abcd"
        // Expected output: "aaaa"
        //
        // Trace:
        // i=0, char='a': freq={a:1} -> first='a' -> result="a"
        // i=1, char='b': freq={a:1,b:1} -> first='a' -> result="aa"
        // i=2, char='c': freq={a:1,b:1,c:1} -> first='a' -> result="aaa"
        // i=3, char='d': freq={a:1,b:1,c:1,d:1} -> first='a' -> result="aaaa"
        // -----------------------------------------------------------------------
        String stream2 = "abcd";
        String result2 = solution.firstNonRepeatingChar(stream2);
        System.out.println("=== Example 2 ===");
        System.out.println("Input:    \"" + stream2 + "\"");
        System.out.println("Output:   \"" + result2 + "\"");
        System.out.println("Expected: \"aaaa\"");
        System.out.println("Correct:  " + result2.equals("aaaa"));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Test: stream = "aaaa"
        // Expected output: "a###"
        // Trace:
        // i=0, char='a': freq={a:1} -> first='a'
        // i=1, char='a': freq={a:2} -> first='#'
        // i=2, char='a': freq={a:3} -> first='#'
        // i=3, char='a': freq={a:4} -> first='#'
        // -----------------------------------------------------------------------
        String stream3 = "aaaa";
        String result3 = solution.firstNonRepeatingChar(stream3);
        System.out.println("=== Additional Test 1 ===");
        System.out.println("Input:    \"" + stream3 + "\"");
        System.out.println("Output:   \"" + result3 + "\"");
        System.out.println("Expected: \"a###\"");
        System.out.println("Correct:  " + result3.equals("a###"));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Test: stream = "z"
        // Expected output: "z"
        // -----------------------------------------------------------------------
        String stream4 = "z";
        String result4 = solution.firstNonRepeatingChar(stream4);
        System.out.println("=== Additional Test 2 (single char) ===");
        System.out.println("Input:    \"" + stream4 + "\"");
        System.out.println("Output:   \"" + result4 + "\"");
        System.out.println("Expected: \"z\"");
        System.out.println("Correct:  " + result4.equals("z"));
        System.out.println();

        // -----------------------------------------------------------------------
        // Demonstrate the array-based alternative approach
        // -----------------------------------------------------------------------
        System.out.println("=== Array-based approach (Example 1) ===");
        String result5 = solution.firstNonRepeatingCharArray(stream1);
        System.out.println("Input:    \"" + stream1 + "\"");
        System.out.println("Output:   \"" + result5 + "\"");
        System.out.println("Expected: \"a#bbb#\"");
        System.out.println("Correct:  " + result5.equals("a#bbb#"));
        System.out.println();

        System.out.println("=== Array-based approach (Example 2) ===");
        String result6 = solution.firstNonRepeatingCharArray(stream2);
        System.out.println("Input:    \"" + stream2 + "\"");
        System.out.println("Output:   \"" + result6 + "\"");
        System.out.println("Expected: \"aaaa\"");
        System.out.println("Correct:  " + result6.equals("aaaa"));
    }
}