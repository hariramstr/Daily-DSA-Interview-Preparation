# Duplicate File Content Detector

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hash Map, String Parsing, Array Grouping

---

## 🗂 What Is This Problem? *(For Everyone)*

Given a list of files — each with a name and some content — find all the files that contain exactly the same content as at least one other file. Group those matching files together and return each group. Files with completely unique content are ignored. Think of it like finding identical twins in a crowd: you only care about the pairs (or larger groups), not the individuals standing alone.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

Duplicate file detection is a core feature in cloud storage products like Google Drive, Dropbox, and iCloud. When millions of users upload files, storing identical copies wastes enormous amounts of expensive server space. By detecting duplicates automatically, companies can store just one physical copy and point multiple users to it — a technique called **deduplication**. This directly reduces infrastructure costs, speeds up backups, and keeps storage bills under control. The same logic powers plagiarism detectors, digital asset management systems, and email spam filters that flag identical message bodies.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Imagine you work in a library and receive a large box of books. Your job is to find any books that have exactly the same text inside, even if their covers have different titles. You group the matching books together and set aside anything that's one-of-a-kind. At the end, you hand back only the groups of identical books — the duplicates — and ignore all the unique ones.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given an array of strings `records`, where each string follows the format `"filename:content"`, identify all groups of files that share identical content. Return a list of groups, where each group is a list of filenames whose content strings are exactly equal. Only include groups containing **two or more** filenames. Order of groups and order of filenames within groups does not matter.

**Constraints:**
- `1 <= records.length <= 1000`
- Filenames and content consist of lowercase letters, digits, and underscores only
- `1 <= filename.length, content.length <= 100`
- All filenames are unique

**Example 1:**
- Input: `["readme:hello world", "notes:foo bar", "copy_readme:hello world", "draft:foo bar", "image:binary_data"]`
- Output: `[["readme", "copy_readme"], ["notes", "draft"]]`

**Example 2:**
- Input: `["a:xyz", "b:abc", "c:xyz", "d:xyz"]`
- Output: `[["a", "c", "d"]]`

---

## 🧩 Approach: How We Solve It *(For Developers)*

The core insight is to use content as a **key** and collect filenames as **values** in a hash map. This lets us group files in a single pass without comparing every file against every other file.

1. **Initialize an empty hash map** (`content → [filenames]`). This will be our grouping structure, where each unique content string maps to a list of filenames that share it.

2. **Iterate over each record string.** We need to process every entry in the input array exactly once to ensure no file is missed.

3. **Parse each record by splitting on the first colon (`:`).** The substring before the colon is the `filename`; everything after is the `content`. Splitting on the *first* colon is important in case content itself ever contains colons.

4. **Append the filename to the hash map under its content key.** If the content key doesn't exist yet, create a new list. This naturally groups all files with identical content together.

5. **Filter the hash map for groups with more than one filename.** Single-entry groups represent unique files, which the problem tells us to exclude.

6. **Return the remaining groups** as a list of lists. No sorting is required since order doesn't matter.

This approach is clean, readable, and runs in linear time — ideal for large file systems.

---

## 📊 Worked Example *(For Developers)*

Using **Example 1**: `["readme:hello world", "notes:foo bar", "copy_readme:hello world", "draft:foo bar", "image:binary_data"]`

| Step | Record Processed       | Parsed (filename, content)    | Hash Map State                                                                 |
|------|------------------------|-------------------------------|--------------------------------------------------------------------------------|
| 1    | `readme:hello world`   | `readme`, `hello world`       | `{ "hello world": ["readme"] }`                                                |
| 2    | `notes:foo bar`        | `notes`, `foo bar`            | `{ "hello world": ["readme"], "foo bar": ["notes"] }`                          |
| 3    | `copy_readme:hello world` | `copy_readme`, `hello world` | `{ "hello world": ["readme", "copy_readme"], "foo bar": ["notes"] }`          |
| 4    | `draft:foo bar`        | `draft`, `foo bar`            | `{ "hello world": ["readme", "copy_readme"], "foo bar": ["notes", "draft"] }` |
| 5    | `image:binary_data`    | `image`, `binary_data`        | `{ "hello world": [...], "foo bar": [...], "binary_data": ["image"] }`        |

**Filter step:** Remove `"binary_data"` group (only 1 file).
**Final output:** `[["readme", "copy_readme"], ["notes", "draft"]]` ✅

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n · m)** — where `n` is the number of records and `m` is the average length of each record string. We visit every record exactly once, and parsing each string (splitting on `:`) takes time proportional to its length. Even at 1,000 records of 200 characters each, this is extremely fast.

### Space Complexity

**O(n · m)** — the hash map stores every filename and content string from the input. In the worst case (all files are duplicates of each other), all filenames accumulate under one key. Practically, memory usage scales linearly with input size and remains very manageable.

---

## 💡 Key Takeaways *(For Everyone)*

- **Deduplication saves real money** — cloud providers use this exact grouping strategy to avoid storing redundant data, cutting storage costs significantly at scale.
- **Automation beats manual review** — scanning thousands of files for duplicates by hand is impractical; an algorithm does it in milliseconds regardless of volume.
- **Hash maps are the right tool for grouping** — whenever you need to cluster items by a shared attribute, a hash map gives you O(1) average-case insertion and lookup, making it far more efficient than nested loops.
- **Parse carefully before you process** — splitting on the *first* colon (not all colons) is a subtle but critical detail; always understand your data format before writing parsing logic.
- **Filter at the end, not during insertion** — collecting all groups first and filtering afterward keeps the logic simple and avoids premature decisions that could cause bugs.

---

## 🚀 Try It Yourself *(For Developers)*

- **Handle nested paths:** Modify the problem so that records use the format `"dir/subdir/filename:content"` — can you group duplicates while also returning their full paths?
- **Scale it up:** What changes if the content strings are very large (e.g., actual file hashes like SHA-256 strings)? Would you change the data structure or parsing strategy?
- **Extend to near-duplicates:** Instead of exact content matching, group files whose content differs by at most one character — how does this change the algorithm's complexity?

---