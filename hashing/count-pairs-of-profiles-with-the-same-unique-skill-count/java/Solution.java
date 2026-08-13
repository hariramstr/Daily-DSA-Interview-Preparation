import java.util.*;

/*
Problem Title: Count Pairs of Profiles with the Same Unique Skill Count

Problem Description:
You are given a list of employee profiles. Each profile is represented by a list of skill names,
where the same skill may appear multiple times in the same profile because of imported data errors.
For each profile, define its unique skill count as the number of distinct skill names that appear
in that profile.

Your task is to return the number of unordered pairs of profiles that have the same unique skill count.

Two profiles form a valid pair if, after removing duplicates within each individual profile, both
profiles contain the same number of distinct skills. The actual skill names do not need to match—
only the count of distinct skills matters.

For example, the profiles ["java", "sql", "java"] and ["go", "python"] both have a unique skill
count of 2, so they form a valid pair.

Return the total number of such pairs across all profiles.

Constraints:
- 1 <= profiles.length <= 100000
- 1 <= profiles[i].length <= 100000
- The sum of profiles[i].length over all profiles does not exceed 200000
- Each skill name consists of lowercase English letters and has length between 1 and 20

Examples:
Example 1:
Input: profiles = [["java","sql","java"],["go","python"],["aws","aws","linux"],["c++"],["html","css","js"]]
Unique skill counts: [2, 2, 2, 1, 3]
Profiles with count 2: 3 profiles -> 3 choose 2 = 3 pairs
Answer: 3

Example 2:
Input: profiles = [["ml","ml","ml"],["sql"],["go","rust"],["a","b","c"],["x","y"],["k"]]
Unique skill counts: [1, 1, 2, 3, 2, 1]
Count 1 appears 3 times -> 3 choose 2 = 3 pairs
Count 2 appears 2 times -> 2 choose 2 = 1 pair
Total answer: 4
*/

public class Solution {

    /**
     * Counts the number of unordered pairs of profiles that have the same number of distinct skills.
     *
     * <p>Approach:
     * <ol>
     *     <li>For each profile, build a HashSet to remove duplicate skills inside that profile.</li>
     *     <li>Compute the size of that set, which is the profile's unique skill count.</li>
     *     <li>Use a HashMap to count how many profiles have each unique skill count.</li>
     *     <li>For every frequency f in the map, add f * (f - 1) / 2 to the answer.</li>
     * </ol>
     *
     * @param profiles a list of employee profiles, where each profile is a list of skill names
     * @return the total number of unordered pairs of profiles with the same unique skill count
     *
     * Time complexity note:
     * O(T), where T is the total number of skill entries across all profiles,
     * because each skill is processed once when building per-profile sets.
     *
     * Space complexity note:
     * O(P + U), where P is the number of profiles for the frequency map keys/values in the worst case,
     * and U is the size of the largest temporary HashSet used for one profile.
     */
    public long countPairsWithSameUniqueSkillCount(List<List<String>> profiles) {
        // This map stores:
        // key   = unique skill count for a profile
        // value = how many profiles have exactly that unique skill count
        Map<Integer, Integer> countFrequency = new HashMap<>();

        // Process each profile one by one.
        for (List<String> profile : profiles) {
            // A HashSet automatically removes duplicates.
            // This is exactly what we need because repeated skills inside the same profile
            // should only be counted once.
            Set<String> uniqueSkills = new HashSet<>();

            // Add every skill from the current profile into the set.
            // If a skill appears multiple times, the set still keeps only one copy.
            for (String skill : profile) {
                uniqueSkills.add(skill);
            }

            // The number of distinct skills in this profile.
            int uniqueSkillCount = uniqueSkills.size();

            // Record that we have seen one more profile with this distinct-skill count.
            countFrequency.put(uniqueSkillCount, countFrequency.getOrDefault(uniqueSkillCount, 0) + 1);
        }

        // Now compute how many unordered pairs can be formed inside each group.
        long totalPairs = 0L;

        // If a certain unique-skill count appears f times,
        // then the number of unordered pairs from that group is:
        // f choose 2 = f * (f - 1) / 2
        for (int frequency : countFrequency.values()) {
            totalPairs += (long) frequency * (frequency - 1) / 2;
        }

        return totalPairs;
    }

    /**
     * Convenience overload for array-based input.
     *
     * @param profiles a 2D array where profiles[i] contains the skills for the i-th profile
     * @return the total number of unordered pairs of profiles with the same unique skill count
     *
     * Time complexity note:
     * O(T), where T is the total number of skill entries across all profiles.
     *
     * Space complexity note:
     * O(P + U), where P is the number of profiles and U is the largest number of distinct skills
     * in any single profile.
     */
    public long countPairsWithSameUniqueSkillCount(String[][] profiles) {
        List<List<String>> profileList = new ArrayList<>(profiles.length);

        // Convert the 2D array into a List<List<String>> so we can reuse the main method above.
        for (String[] profile : profiles) {
            profileList.add(Arrays.asList(profile));
        }

        return countPairsWithSameUniqueSkillCount(profileList);
    }

    /**
     * Builds a readable string representation of the profiles for demonstration output.
     *
     * @param profiles a 2D array of profiles
     * @return a human-readable string version of the input
     *
     * Time complexity note:
     * O(T), where T is the total number of skill entries printed.
     *
     * Space complexity note:
     * O(T) for the generated string content.
     */
    public String profilesToString(String[][] profiles) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < profiles.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(Arrays.toString(profiles[i]));
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
     * Time complexity note:
     * O(T) across the demonstrated examples.
     *
     * Space complexity note:
     * O(P + U) during each call to the counting method.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        // Profiles:
        // 0 -> ["java","sql","java"]     -> unique skills = {"java","sql"} -> 2
        // 1 -> ["go","python"]           -> unique skills = {"go","python"} -> 2
        // 2 -> ["aws","aws","linux"]     -> unique skills = {"aws","linux"} -> 2
        // 3 -> ["c++"]                   -> unique skills = {"c++"} -> 1
        // 4 -> ["html","css","js"]       -> unique skills = {"html","css","js"} -> 3
        //
        // Frequency of unique counts:
        // 1 -> 1 profile
        // 2 -> 3 profiles
        // 3 -> 1 profile
        //
        // Pairs:
        // For count 2: 3 choose 2 = 3
        // Total = 3
        String[][] profiles1 = {
            {"java", "sql", "java"},
            {"go", "python"},
            {"aws", "aws", "linux"},
            {"c++"},
            {"html", "css", "js"}
        };

        long result1 = solution.countPairsWithSameUniqueSkillCount(profiles1);
        System.out.println("Example 1 Input: " + solution.profilesToString(profiles1));
        System.out.println("Example 1 Output: " + result1);
        System.out.println("Expected: 3");
        System.out.println();

        // Example 2
        // Profiles:
        // 0 -> ["ml","ml","ml"] -> unique count = 1
        // 1 -> ["sql"]          -> unique count = 1
        // 2 -> ["go","rust"]    -> unique count = 2
        // 3 -> ["a","b","c"]    -> unique count = 3
        // 4 -> ["x","y"]        -> unique count = 2
        // 5 -> ["k"]            -> unique count = 1
        //
        // Frequency of unique counts:
        // 1 -> 3 profiles -> 3 choose 2 = 3
        // 2 -> 2 profiles -> 2 choose 2 = 1
        // 3 -> 1 profile  -> 0
        //
        // Total = 4
        String[][] profiles2 = {
            {"ml", "ml", "ml"},
            {"sql"},
            {"go", "rust"},
            {"a", "b", "c"},
            {"x", "y"},
            {"k"}
        };

        long result2 = solution.countPairsWithSameUniqueSkillCount(profiles2);
        System.out.println("Example 2 Input: " + solution.profilesToString(profiles2));
        System.out.println("Example 2 Output: " + result2);
        System.out.println("Expected: 4");
        System.out.println();

        // Additional quick sanity check
        // All profiles have unique count 1:
        // 4 profiles -> 4 choose 2 = 6
        String[][] profiles3 = {
            {"a", "a"},
            {"b"},
            {"c", "c", "c"},
            {"d"}
        };

        long result3 = solution.countPairsWithSameUniqueSkillCount(profiles3);
        System.out.println("Additional Test Input: " + solution.profilesToString(profiles3));
        System.out.println("Additional Test Output: " + result3);
        System.out.println("Expected: 6");
    }
}