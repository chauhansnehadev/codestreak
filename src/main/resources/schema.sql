-- ====================================================================
-- CodeStreak Database Initialization Schema
-- PostgreSQL Schema for DSA Preparation & Progress Tracking Platform
-- ====================================================================

-- Drop tables in reverse order of dependencies if re-running
DROP TABLE IF EXISTS revisions CASCADE;
DROP TABLE IF EXISTS submissions CASCADE;
DROP TABLE IF EXISTS user_problems CASCADE;
DROP TABLE IF EXISTS problem_topics CASCADE;
DROP TABLE IF EXISTS problems CASCADE;
DROP TABLE IF EXISTS user_roadmap_topics CASCADE;
DROP TABLE IF EXISTS roadmap_topics CASCADE;
DROP TABLE IF EXISTS roadmaps CASCADE;
DROP TABLE IF EXISTS topics CASCADE;
DROP TABLE IF EXISTS user_platforms CASCADE;
DROP TABLE IF EXISTS platforms CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- 1. Users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    avatar_url VARCHAR(255),
    target_problems_per_day INT DEFAULT 3,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

-- 2. Platforms table
CREATE TABLE platforms (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    code VARCHAR(30) NOT NULL UNIQUE, -- 'leetcode', 'codechef', 'codeforces', 'hackerrank'
    base_url VARCHAR(255) NOT NULL,
    icon_name VARCHAR(50) DEFAULT 'code',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. User Platforms table (Connecting user profiles on LeetCode, Codeforces, etc.)
CREATE TABLE user_platforms (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    platform_id INT NOT NULL REFERENCES platforms(id) ON DELETE CASCADE,
    platform_username VARCHAR(100) NOT NULL,
    profile_url VARCHAR(255),
    problems_solved INT DEFAULT 0,
    rating INT DEFAULT 0,
    global_rank VARCHAR(50),
    last_synced TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_platform UNIQUE (user_id, platform_id)
);

CREATE INDEX idx_user_platforms_user ON user_platforms(user_id);

-- 4. Topics table
CREATE TABLE topics (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon VARCHAR(50) DEFAULT 'folder',
    display_order INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_topics_slug ON topics(slug);

-- 5. Problems table
CREATE TABLE problems (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    url VARCHAR(500),
    platform_id INT REFERENCES platforms(id) ON DELETE SET NULL,
    difficulty VARCHAR(20) NOT NULL CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    is_custom BOOLEAN DEFAULT FALSE,
    created_by_user_id INT REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_problems_difficulty ON problems(difficulty);
CREATE INDEX idx_problems_platform ON problems(platform_id);

-- 6. Problem Topics (many-to-many relationship between problems and topics)
CREATE TABLE problem_topics (
    id SERIAL PRIMARY KEY,
    problem_id INT NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    topic_id INT NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    CONSTRAINT uq_problem_topic UNIQUE (problem_id, topic_id)
);

CREATE INDEX idx_problem_topics_prob ON problem_topics(problem_id);
CREATE INDEX idx_problem_topics_topic ON problem_topics(topic_id);

-- 7. User Problems (User's status on problems: NOT_STARTED, ATTEMPTED, SOLVED, REVISION_NEEDED)
CREATE TABLE user_problems (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    problem_id INT NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED' CHECK (status IN ('NOT_STARTED', 'ATTEMPTED', 'SOLVED', 'REVISION_NEEDED')),
    difficulty_override VARCHAR(20) CHECK (difficulty_override IN ('EASY', 'MEDIUM', 'HARD')),
    notes TEXT,
    personal_solution_url VARCHAR(500),
    first_solved_at TIMESTAMP WITH TIME ZONE,
    last_attempted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_problem UNIQUE (user_id, problem_id)
);

CREATE INDEX idx_user_problems_user ON user_problems(user_id);
CREATE INDEX idx_user_problems_status ON user_problems(user_id, status);

-- 8. Submissions table (Track coding attempts and history for streak calculation)
CREATE TABLE submissions (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    problem_id INT NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL CHECK (status IN ('ACCEPTED', 'WRONG_ANSWER', 'TIME_LIMIT_EXCEEDED', 'RUNTIME_ERROR', 'COMPILE_ERROR')),
    language VARCHAR(50) DEFAULT 'Java',
    runtime_ms INT,
    memory_kb INT,
    notes TEXT,
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_submissions_user_date ON submissions(user_id, submitted_at);

-- 9. Roadmaps table (Structured learning paths)
CREATE TABLE roadmaps (
    id SERIAL PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    is_default BOOLEAN DEFAULT FALSE,
    created_by_user_id INT REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 10. Roadmap Topics (Topics mapped to roadmaps with sequence order)
CREATE TABLE roadmap_topics (
    id SERIAL PRIMARY KEY,
    roadmap_id INT NOT NULL REFERENCES roadmaps(id) ON DELETE CASCADE,
    topic_id INT NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    sequence_order INT NOT NULL DEFAULT 0,
    target_problems INT DEFAULT 10,
    CONSTRAINT uq_roadmap_topic UNIQUE (roadmap_id, topic_id)
);

-- 11. User Roadmap Progress (User's status per roadmap topic)
CREATE TABLE user_roadmap_topics (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    roadmap_topic_id INT NOT NULL REFERENCES roadmap_topics(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED' CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED')),
    notes TEXT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_roadmap_topic UNIQUE (user_id, roadmap_topic_id)
);

CREATE INDEX idx_user_roadmap_topics_user ON user_roadmap_topics(user_id);

-- 12. Revisions table (Spaced repetition and revision scheduler)
CREATE TABLE revisions (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    problem_id INT NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    scheduled_for DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'SKIPPED')),
    priority INT DEFAULT 1, -- 1: Normal, 2: High, 3: Urgent
    notes TEXT,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_revisions_user_sched ON revisions(user_id, scheduled_for, status);

-- ====================================================================
-- SEED DATA
-- ====================================================================

-- Platforms
INSERT INTO platforms (name, code, base_url, icon_name) VALUES
('LeetCode', 'leetcode', 'https://leetcode.com', 'leetcode'),
('CodeChef', 'codechef', 'https://www.codechef.com', 'codechef'),
('Codeforces', 'codeforces', 'https://codeforces.com', 'codeforces'),
('HackerRank', 'hackerrank', 'https://www.hackerrank.com', 'hackerrank');

-- Topics
INSERT INTO topics (name, slug, description, icon, display_order) VALUES
('Arrays', 'arrays', 'Linear data structure stored at contiguous memory locations.', 'grid', 1),
('Strings', 'strings', 'Sequences of characters, string manipulation and parsing.', 'type', 2),
('Searching and Sorting', 'searching-and-sorting', 'Binary search, merge sort, quicksort, and search spaces.', 'search', 3),
('Hashing', 'hashing', 'Hash maps, hash sets, frequency arrays, and collisions.', 'hash', 4),
('Recursion and Backtracking', 'recursion-and-backtracking', 'Divide and conquer, recursive tree exploration and pruning.', 'repeat', 5),
('Linked Lists', 'linked-lists', 'Singly, doubly, and circular linked nodes with pointer manipulation.', 'link', 6),
('Stacks and Queues', 'stacks-and-queues', 'LIFO & FIFO structures, monotonic stacks, double-ended queues.', 'layers', 7),
('Trees and BST', 'trees-and-bst', 'Binary trees, traversals, binary search trees, and balance.', 'git-branch', 8),
('Heaps', 'heaps', 'Min/Max priority queues, heapify, and top-k elements.', 'triangle', 9),
('Graphs', 'graphs', 'BFS, DFS, Dijkstra, Bellman-Ford, spanning trees, and topological sort.', 'share-2', 10),
('Greedy Algorithms', 'greedy-algorithms', 'Optimal local choices for global optimum.', 'trending-up', 11),
('Dynamic Programming', 'dynamic-programming', 'Overlapping subproblems, memoization, and bottom-up DP table.', 'cpu', 12);

-- Default DSA Roadmap
INSERT INTO roadmaps (title, description, is_default) VALUES
('Standard DSA Mastery Roadmap', 'Complete industry-grade DSA roadmap for product companies and competitive programming.', TRUE);

-- Map topics to default roadmap
INSERT INTO roadmap_topics (roadmap_id, topic_id, sequence_order, target_problems)
SELECT 1, id, display_order, 15 FROM topics ORDER BY display_order;

-- Seed Problems
INSERT INTO problems (title, url, platform_id, difficulty) VALUES
('Two Sum', 'https://leetcode.com/problems/two-sum/', 1, 'EASY'),
('Best Time to Buy and Sell Stock', 'https://leetcode.com/problems/best-time-to-buy-and-sell-stock/', 1, 'EASY'),
('Contains Duplicate', 'https://leetcode.com/problems/contains-duplicate/', 1, 'EASY'),
('Product of Array Except Self', 'https://leetcode.com/problems/product-of-array-except-self/', 1, 'MEDIUM'),
('Maximum Subarray (Kadane)', 'https://leetcode.com/problems/maximum-subarray/', 1, 'MEDIUM'),
('3Sum', 'https://leetcode.com/problems/3sum/', 1, 'MEDIUM'),
('Trapping Rain Water', 'https://leetcode.com/problems/trapping-rain-water/', 1, 'HARD'),
('Valid Anagram', 'https://leetcode.com/problems/valid-anagram/', 1, 'EASY'),
('Valid Palindrome', 'https://leetcode.com/problems/valid-palindrome/', 1, 'EASY'),
('Longest Substring Without Repeating Characters', 'https://leetcode.com/problems/longest-substring-without-repeating-characters/', 1, 'MEDIUM'),
('Longest Palindromic Substring', 'https://leetcode.com/problems/longest-palindromic-substring/', 1, 'MEDIUM'),
('Binary Search', 'https://leetcode.com/problems/binary-search/', 1, 'EASY'),
('Search in Rotated Sorted Array', 'https://leetcode.com/problems/search-in-rotated-sorted-array/', 1, 'MEDIUM'),
('Find Minimum in Rotated Sorted Array', 'https://leetcode.com/problems/find-minimum-in-rotated-sorted-array/', 1, 'MEDIUM'),
('Reverse Linked List', 'https://leetcode.com/problems/reverse-linked-list/', 1, 'EASY'),
('Merge Two Sorted Lists', 'https://leetcode.com/problems/merge-two-sorted-lists/', 1, 'EASY'),
('Linked List Cycle', 'https://leetcode.com/problems/linked-list-cycle/', 1, 'EASY'),
('Reorder List', 'https://leetcode.com/problems/reorder-list/', 1, 'MEDIUM'),
('Valid Parentheses', 'https://leetcode.com/problems/valid-parentheses/', 1, 'EASY'),
('Daily Temperatures', 'https://leetcode.com/problems/daily-temperatures/', 1, 'MEDIUM'),
('Invert Binary Tree', 'https://leetcode.com/problems/invert-binary-tree/', 1, 'EASY'),
('Maximum Depth of Binary Tree', 'https://leetcode.com/problems/maximum-depth-of-binary-tree/', 1, 'EASY'),
('Binary Tree Level Order Traversal', 'https://leetcode.com/problems/binary-tree-level-order-traversal/', 1, 'MEDIUM'),
('Validate Binary Search Tree', 'https://leetcode.com/problems/validate-binary-search-tree/', 1, 'MEDIUM'),
('Kth Smallest Element in a BST', 'https://leetcode.com/problems/kth-smallest-element-in-a-bst/', 1, 'MEDIUM'),
('Kth Largest Element in an Array', 'https://leetcode.com/problems/kth-largest-element-in-an-array/', 1, 'MEDIUM'),
('Find Median from Data Stream', 'https://leetcode.com/problems/find-median-from-data-stream/', 1, 'HARD'),
('Number of Islands', 'https://leetcode.com/problems/number-of-islands/', 1, 'MEDIUM'),
('Clone Graph', 'https://leetcode.com/problems/clone-graph/', 1, 'MEDIUM'),
('Course Schedule', 'https://leetcode.com/problems/course-schedule/', 1, 'MEDIUM'),
('Climbing Stairs', 'https://leetcode.com/problems/climbing-stairs/', 1, 'EASY'),
('Coin Change', 'https://leetcode.com/problems/coin-change/', 1, 'MEDIUM'),
('Longest Increasing Subsequence', 'https://leetcode.com/problems/longest-increasing-subsequence/', 1, 'MEDIUM'),
('Word Break', 'https://leetcode.com/problems/word-break/', 1, 'MEDIUM'),
('Subsets', 'https://leetcode.com/problems/subsets/', 1, 'MEDIUM'),
('Combination Sum', 'https://leetcode.com/problems/combination-sum/', 1, 'MEDIUM'),
('Chef and String', 'https://www.codechef.com/problems/XYSTR', 2, 'EASY'),
('Watermelon', 'https://codeforces.com/problemset/problem/4/A', 3, 'EASY'),
('Way Too Long Words', 'https://codeforces.com/problemset/problem/71/A', 3, 'EASY'),
('Strings: Making Anagrams', 'https://www.hackerrank.com/challenges/ctci-making-anagrams/problem', 4, 'EASY');

-- Link problems to topics
-- Two Sum (1) -> Arrays (1), Hashing (4)
INSERT INTO problem_topics (problem_id, topic_id) VALUES
(1, 1), (1, 4),
(2, 1),
(3, 1), (3, 4),
(4, 1),
(5, 1), (5, 12),
(6, 1), (6, 3),
(7, 1), (7, 7),
(8, 2), (8, 4),
(9, 2),
(10, 2), (10, 4),
(11, 2), (11, 12),
(12, 3),
(13, 3),
(14, 3),
(15, 6),
(16, 6),
(17, 6),
(18, 6),
(19, 7),
(20, 7),
(21, 8),
(22, 8),
(23, 8),
(24, 8),
(25, 8),
(26, 9),
(27, 9),
(28, 10),
(29, 10),
(30, 10),
(31, 12),
(32, 12),
(33, 12),
(34, 12),
(35, 5),
(36, 5),
(37, 2),
(38, 1),
(39, 2),
(40, 2), (40, 4);

-- Demo User (Password is 'password123', hashed with BCrypt)
-- BCrypt salt & hash: $2a$10$wN10U5hN0j00aWbYyGqK2e1tVqgD7ZkM3e5sT2t7w.P7bY0JmQjK.
-- Or generated dynamically if needed. Let's insert demo user:
INSERT INTO users (username, email, password_hash, full_name, target_problems_per_day)
VALUES ('demouser', 'demo@codestreak.dev', '$2a$10$DZ9eWBQwOxvM5Zt6ei0.TOtEDANzTXuz3BRrFMP55NhVLR.7iJvyG', 'Demo Coder', 3);
-- password for demouser is: password123

-- Seed demo user platforms
INSERT INTO user_platforms (user_id, platform_id, platform_username, profile_url, problems_solved, rating, global_rank) VALUES
(1, 1, 'democoder_lc', 'https://leetcode.com/u/democoder_lc/', 18, 1680, 'Top 15%'),
(1, 2, 'democoder_cc', 'https://www.codechef.com/users/democoder_cc', 8, 1520, '3 Star'),
(1, 3, 'democoder_cf', 'https://codeforces.com/profile/democoder_cf', 6, 1340, 'Pupil'),
(1, 4, 'democoder_hr', 'https://www.hackerrank.com/profile/democoder_hr', 12, 1450, 'Gold Badge');

-- Seed demo user problems
INSERT INTO user_problems (user_id, problem_id, status, notes, first_solved_at, last_attempted_at) VALUES
(1, 1, 'SOLVED', 'Used HashMap for O(n) one-pass solution. Key is target - num[i].', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '5 days'),
(1, 2, 'SOLVED', 'One pass tracking minimum buying price so far.', CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP - INTERVAL '4 days'),
(1, 3, 'SOLVED', 'HashSet lookup O(1).', CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP - INTERVAL '4 days'),
(1, 4, 'SOLVED', 'Prefix and postfix product arrays without division.', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days'),
(1, 5, 'SOLVED', 'Kadane algorithm: max(current, current + prev).', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'),
(1, 6, 'ATTEMPTED', 'Two pointers after sorting. Watch for duplicate triplets!', NULL, CURRENT_TIMESTAMP - INTERVAL '1 day'),
(1, 8, 'SOLVED', 'Frequency counter of 26 letters.', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'),
(1, 10, 'REVISION_NEEDED', 'Sliding window technique with left and right pointers. Need to revise edge cases.', CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP - INTERVAL '1 day'),
(1, 12, 'SOLVED', 'Standard binary search l <= r.', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day'),
(1, 15, 'SOLVED', 'Iterative 3 pointers: prev, curr, nextTemp.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 19, 'SOLVED', 'Stack matching open and close braces.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 28, 'ATTEMPTED', 'DFS traversal on grid. Need to review boundary conditions.', NULL, CURRENT_TIMESTAMP);

-- Seed demo user submissions (creates active streak and activity graph)
INSERT INTO submissions (user_id, problem_id, status, language, runtime_ms, submitted_at) VALUES
(1, 1, 'ACCEPTED', 'Java', 2, CURRENT_TIMESTAMP - INTERVAL '5 days'),
(1, 2, 'ACCEPTED', 'Java', 1, CURRENT_TIMESTAMP - INTERVAL '4 days'),
(1, 3, 'ACCEPTED', 'Java', 3, CURRENT_TIMESTAMP - INTERVAL '4 days'),
(1, 4, 'ACCEPTED', 'Java', 2, CURRENT_TIMESTAMP - INTERVAL '3 days'),
(1, 5, 'ACCEPTED', 'Java', 1, CURRENT_TIMESTAMP - INTERVAL '2 days'),
(1, 8, 'ACCEPTED', 'Java', 2, CURRENT_TIMESTAMP - INTERVAL '2 days'),
(1, 6, 'WRONG_ANSWER', 'Java', 25, CURRENT_TIMESTAMP - INTERVAL '1 day'),
(1, 12, 'ACCEPTED', 'Java', 0, CURRENT_TIMESTAMP - INTERVAL '1 day'),
(1, 15, 'ACCEPTED', 'Java', 0, CURRENT_TIMESTAMP),
(1, 19, 'ACCEPTED', 'Java', 1, CURRENT_TIMESTAMP);

-- Seed demo revisions
INSERT INTO revisions (user_id, problem_id, scheduled_for, status, priority, notes) VALUES
(1, 10, CURRENT_DATE, 'PENDING', 3, 'Revise sliding window edge case with repeating characters'),
(1, 6, CURRENT_DATE + 1, 'PENDING', 2, 'Solve 3Sum edge cases with zero and duplicates'),
(1, 5, CURRENT_DATE + 3, 'PENDING', 1, 'Quick review of Kadane algorithm'),
(1, 1, CURRENT_DATE - 1, 'COMPLETED', 1, 'Two sum revision done');

-- Initialize user roadmap status for demo user
INSERT INTO user_roadmap_topics (user_id, roadmap_topic_id, status, notes) VALUES
(1, 1, 'COMPLETED', 'Solved all basic and medium array problems.'),
(1, 2, 'IN_PROGRESS', 'Working through sliding window patterns.'),
(1, 3, 'IN_PROGRESS', 'Binary search variations.'),
(1, 4, 'COMPLETED', 'Hash tables and set lookups mastered.'),
(1, 5, 'NOT_STARTED', 'Upcoming topic.'),
(1, 6, 'IN_PROGRESS', 'Pointers and cycle detection.'),
(1, 7, 'IN_PROGRESS', 'Stack and queue fundamentals.');
