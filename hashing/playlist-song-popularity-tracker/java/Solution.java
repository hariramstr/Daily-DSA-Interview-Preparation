/*
 * Title: Playlist Song Popularity Tracker
 * Difficulty: Easy
 * Topic: Hashing
 *
 * Problem Description:
 * You are building a music streaming app. Users submit a log of song plays,
 * where each entry is a string representing the song title. Your task is to
 * determine which songs were played more than once and return them sorted by
 * their play count in descending order. If two songs have the same play count,
 * sort them alphabetically (ascending).
 *
 * Given a list of strings `plays` where each element is the title of a song
 * that was played, return a list of song titles that appear more than once,
 * sorted as described above.
 *
 * Constraints:
 * - 1 <= plays.length <= 10^4
 * - 1 <= plays[i].length <= 50
 * - plays[i] consists of lowercase English letters and spaces.
 * - Song titles are case-sensitive.
 *
 * Example 1:
 * Input: plays = ["shape of you", "blinding lights", "shape of you",
 *                 "blinding lights", "shape of you", "levitating"]
 * Output: ["shape of you", "blinding lights"]
 * Explanation: "shape of you" was played 3 times, "blinding lights" was played
 *              2 times. "levitating" was played only once so it is excluded.
 *
 * Example 2:
 * Input: plays = ["song a", "song b", "song c", "song b", "song a", "song d", "song d"]
 * Output: ["song a", "song b", "song d"]
 * Explanation: All three songs were played exactly 2 times, so they are sorted alphabetically.
 */

import java.util.*;

/**
 * Solution class for the Playlist Song Popularity Tracker problem.
 * Uses a HashMap to count song plays, then filters and sorts the results.
 */
public class Solution {

    /**
     * Finds all songs played more than once and returns them sorted by play count
     * (descending). Ties in play count are broken alphabetically (ascending).
     *
     * @param plays A list of song title strings representing the play log.
     * @return A list of song titles played more than once, sorted by play count
     *         descending, then alphabetically ascending for ties.
     *
     * Time Complexity:  O(n log n) — where n is the number of unique songs that
     *                   appear more than once. Building the frequency map is O(n)
     *                   where n is plays.length; sorting is O(k log k) where k
     *                   is the number of qualifying songs.
     * Space Complexity: O(n) — for the HashMap storing song frequencies.
     */
    public List<String> getPopularSongs(List<String> plays) {

        // ---------------------------------------------------------------
        // STEP 1: Build a frequency map (HashMap) to count how many times
        //         each song title appears in the plays list.
        //
        //         HashMap<String, Integer> maps:
        //           song title  -->  number of times it was played
        // ---------------------------------------------------------------
        Map<String, Integer> frequencyMap = new HashMap<>();

        // Iterate over every song play entry in the input list
        for (String song : plays) {
            // getOrDefault(song, 0) returns the current count for this song,
            // or 0 if the song hasn't been seen yet.
            // We then add 1 to that count and store it back in the map.
            frequencyMap.put(song, frequencyMap.getOrDefault(song, 0) + 1);
        }

        // ---------------------------------------------------------------
        // STEP 2: Filter out songs that were played only once (count == 1).
        //         We only want songs with a play count GREATER THAN 1.
        //
        //         We collect the qualifying song titles into a new list
        //         so we can sort them in the next step.
        // ---------------------------------------------------------------
        List<String> result = new ArrayList<>();

        // Iterate over every entry (song -> count) in the frequency map
        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
            // Check if this song was played more than once
            if (entry.getValue() > 1) {
                // This song qualifies — add it to our result list
                result.add(entry.getKey());
            }
        }

        // ---------------------------------------------------------------
        // STEP 3: Sort the result list using a custom comparator.
        //
        //         Sorting rules (applied in order):
        //           1. Primary sort:   play count DESCENDING
        //              (higher play count comes first)
        //           2. Secondary sort: song title ASCENDING (alphabetical)
        //              (used when two songs have the same play count)
        //
        //         We use a lambda comparator:
        //           - Compare play counts of song b vs song a (descending)
        //           - If counts are equal, compare song a vs song b (ascending)
        // ---------------------------------------------------------------
        result.sort((songA, songB) -> {
            // Retrieve the play count for each song from the frequency map
            int countA = frequencyMap.get(songA);
            int countB = frequencyMap.get(songB);

            // If play counts differ, sort by count DESCENDING:
            // subtracting countA from countB gives descending order
            // (larger countB means songB should come first, so result > 0
            //  which means songA is placed after songB)
            if (countB != countA) {
                return countB - countA; // descending by play count
            }

            // If play counts are equal, sort alphabetically ASCENDING:
            // compareTo returns negative if songA < songB lexicographically,
            // which means songA comes first — exactly what we want.
            return songA.compareTo(songB); // ascending alphabetically
        });

        // ---------------------------------------------------------------
        // STEP 4: Return the sorted result list.
        // ---------------------------------------------------------------
        return result;
    }

    /**
     * Main method to demonstrate the solution with sample inputs from the problem.
     * Traces through both examples and prints the results.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {

        // Create an instance of Solution to call the non-static method
        Solution solution = new Solution();

        // -------------------------------------------------------------------
        // Example 1:
        // Input:  ["shape of you", "blinding lights", "shape of you",
        //          "blinding lights", "shape of you", "levitating"]
        // Expected Output: ["shape of you", "blinding lights"]
        //
        // Trace:
        //   frequencyMap after counting:
        //     "shape of you"   -> 3
        //     "blinding lights"-> 2
        //     "levitating"     -> 1
        //
        //   After filtering (count > 1):
        //     ["shape of you", "blinding lights"]  (order may vary before sort)
        //
        //   After sorting (descending count, then alpha):
        //     "shape of you" (3) comes before "blinding lights" (2)
        //     Result: ["shape of you", "blinding lights"]  ✓
        // -------------------------------------------------------------------
        List<String> plays1 = Arrays.asList(
                "shape of you",
                "blinding lights",
                "shape of you",
                "blinding lights",
                "shape of you",
                "levitating"
        );

        System.out.println("=== Example 1 ===");
        System.out.println("Input:    " + plays1);
        List<String> output1 = solution.getPopularSongs(plays1);
        System.out.println("Output:   " + output1);
        System.out.println("Expected: [shape of you, blinding lights]");
        System.out.println();

        // -------------------------------------------------------------------
        // Example 2:
        // Input:  ["song a", "song b", "song c", "song b", "song a", "song d", "song d"]
        // Expected Output: ["song a", "song b", "song d"]
        //
        // Trace:
        //   frequencyMap after counting:
        //     "song a" -> 2
        //     "song b" -> 2
        //     "song c" -> 1
        //     "song d" -> 2
        //
        //   After filtering (count > 1):
        //     ["song a", "song b", "song d"]  (order may vary before sort)
        //
        //   After sorting (descending count, then alpha):
        //     All three have count = 2, so sort alphabetically:
        //     "song a" < "song b" < "song d"
        //     Result: ["song a", "song b", "song d"]  ✓
        // -------------------------------------------------------------------
        List<String> plays2 = Arrays.asList(
                "song a",
                "song b",
                "song c",
                "song b",
                "song a",
                "song d",
                "song d"
        );

        System.out.println("=== Example 2 ===");
        System.out.println("Input:    " + plays2);
        List<String> output2 = solution.getPopularSongs(plays2);
        System.out.println("Output:   " + output2);
        System.out.println("Expected: [song a, song b, song d]");
        System.out.println();

        // -------------------------------------------------------------------
        // Additional Edge Case: All songs played only once
        // Expected Output: [] (empty list — no song qualifies)
        // -------------------------------------------------------------------
        List<String> plays3 = Arrays.asList("alpha", "beta", "gamma");

        System.out.println("=== Edge Case: All songs played once ===");
        System.out.println("Input:    " + plays3);
        List<String> output3 = solution.getPopularSongs(plays3);
        System.out.println("Output:   " + output3);
        System.out.println("Expected: []");
        System.out.println();

        // -------------------------------------------------------------------
        // Additional Edge Case: Single song played many times
        // Expected Output: ["hello"] — only one song, played 4 times
        // -------------------------------------------------------------------
        List<String> plays4 = Arrays.asList("hello", "hello", "hello", "hello");

        System.out.println("=== Edge Case: Single song repeated ===");
        System.out.println("Input:    " + plays4);
        List<String> output4 = solution.getPopularSongs(plays4);
        System.out.println("Output:   " + output4);
        System.out.println("Expected: [hello]");
    }
}