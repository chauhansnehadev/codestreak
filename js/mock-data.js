/**
 * CodeStreak Client-Side Data & Mock API Interceptor for GitHub Pages
 * Allows the entire frontend to run 100% client-side with persistent localStorage!
 */
(function() {
  const isGitHubPages = window.location.hostname.includes('github.io') || window.location.protocol === 'file:';

  // Seed default data if not already in localStorage
  function initStorage() {
    if (!localStorage.getItem('cs_initialized')) {
      const defaultPlatforms = [
        { id: 1, name: 'LeetCode', code: 'leetcode', baseUrl: 'https://leetcode.com', isActive: true },
        { id: 3, name: 'Codeforces', code: 'codeforces', baseUrl: 'https://codeforces.com', isActive: true },
        { id: 4, name: 'HackerRank', code: 'hackerrank', baseUrl: 'https://www.hackerrank.com', isActive: true }
      ];

      const defaultUserPlatforms = [
        { id: 1, platformId: 1, platformUsername: 'starr_dust', profileUrl: 'https://leetcode.com/u/starr_dust/', problemsSolved: 184, rating: 0, globalRank: 'Top #966,367' },
        { id: 2, platformId: 3, platformUsername: 'chauhansneha2760', profileUrl: 'https://codeforces.com/profile/chauhansneha2760', problemsSolved: 0, rating: 0, globalRank: '' },
        { id: 3, platformId: 4, platformUsername: 'chauhansneha2760', profileUrl: 'https://www.hackerrank.com/profile/chauhansneha2760', problemsSolved: 70, rating: 607, globalRank: '5★ C++' }
      ];

      const defaultTopics = [
        { id: 1, name: 'Arrays', slug: 'arrays' },
        { id: 2, name: 'Strings', slug: 'strings' },
        { id: 3, name: 'Searching and Sorting', slug: 'searching-sorting' },
        { id: 4, name: 'Linked Lists', slug: 'linked-lists' },
        { id: 5, name: 'Stacks and Queues', slug: 'stacks-queues' },
        { id: 6, name: 'Trees and BST', slug: 'trees' },
        { id: 7, name: 'Dynamic Programming', slug: 'dynamic-programming' },
        { id: 8, name: 'Graphs', slug: 'graphs' }
      ];

      const defaultProblems = [
        { id: 1, title: 'Two Sum', url: 'https://leetcode.com/problems/two-sum/', platformId: 1, platformName: 'LeetCode', difficulty: 'EASY', userStatus: 'SOLVED', topics: [{ id: 1, name: 'Arrays' }, { id: 2, name: 'Hashing' }] },
        { id: 2, title: 'Best Time to Buy and Sell Stock', url: 'https://leetcode.com/problems/best-time-to-buy-and-sell-stock/', platformId: 1, platformName: 'LeetCode', difficulty: 'EASY', userStatus: 'SOLVED', topics: [{ id: 1, name: 'Arrays' }] },
        { id: 3, title: 'Contains Duplicate', url: 'https://leetcode.com/problems/contains-duplicate/', platformId: 1, platformName: 'LeetCode', difficulty: 'EASY', userStatus: 'SOLVED', topics: [{ id: 1, name: 'Arrays' }, { id: 2, name: 'Hashing' }] },
        { id: 4, title: 'Product of Array Except Self', url: 'https://leetcode.com/problems/product-of-array-except-self/', platformId: 1, platformName: 'LeetCode', difficulty: 'MEDIUM', userStatus: 'SOLVED', topics: [{ id: 1, name: 'Arrays' }] },
        { id: 5, title: 'Maximum Subarray (Kadane)', url: 'https://leetcode.com/problems/maximum-subarray/', platformId: 1, platformName: 'LeetCode', difficulty: 'MEDIUM', userStatus: 'SOLVED', topics: [{ id: 1, name: 'Arrays' }, { id: 7, name: 'Dynamic Programming' }] },
        { id: 6, title: '3Sum', url: 'https://leetcode.com/problems/3sum/', platformId: 1, platformName: 'LeetCode', difficulty: 'MEDIUM', userStatus: 'SOLVED', topics: [{ id: 1, name: 'Arrays' }, { id: 3, name: 'Searching and Sorting' }] },
        { id: 7, title: 'Trapping Rain Water', url: 'https://leetcode.com/problems/trapping-rain-water/', platformId: 1, platformName: 'LeetCode', difficulty: 'HARD', userStatus: 'SOLVED', topics: [{ id: 1, name: 'Arrays' }, { id: 5, name: 'Stacks and Queues' }] },
        { id: 10, title: 'Longest Substring Without Repeating Characters', url: 'https://leetcode.com/problems/longest-substring-without-repeating-characters/', platformId: 1, platformName: 'LeetCode', difficulty: 'MEDIUM', userStatus: 'REVISION_NEEDED', topics: [{ id: 2, name: 'Strings' }] },
        { id: 15, title: 'Reverse Linked List', url: 'https://leetcode.com/problems/reverse-linked-list/', platformId: 1, platformName: 'LeetCode', difficulty: 'EASY', userStatus: 'SOLVED', topics: [{ id: 4, name: 'Linked Lists' }] },
        { id: 19, title: 'Valid Parentheses', url: 'https://leetcode.com/problems/valid-parentheses/', platformId: 1, platformName: 'LeetCode', difficulty: 'EASY', userStatus: 'SOLVED', topics: [{ id: 5, name: 'Stacks and Queues' }] },
        { id: 28, title: 'Number of Islands', url: 'https://leetcode.com/problems/number-of-islands/', platformId: 1, platformName: 'LeetCode', difficulty: 'MEDIUM', userStatus: 'ATTEMPTED', topics: [{ id: 8, name: 'Graphs' }] },
        { id: 29, title: 'Clone Graph', url: 'https://leetcode.com/problems/clone-graph/', platformId: 1, platformName: 'LeetCode', difficulty: 'MEDIUM', userStatus: 'NOT_STARTED', topics: [{ id: 8, name: 'Graphs' }] },
        { id: 30, title: 'Course Schedule', url: 'https://leetcode.com/problems/course-schedule/', platformId: 1, platformName: 'LeetCode', difficulty: 'MEDIUM', userStatus: 'NOT_STARTED', topics: [{ id: 8, name: 'Graphs' }] },
        { id: 31, title: 'Climbing Stairs', url: 'https://leetcode.com/problems/climbing-stairs/', platformId: 1, platformName: 'LeetCode', difficulty: 'EASY', userStatus: 'SOLVED', topics: [{ id: 7, name: 'Dynamic Programming' }] }
      ];

      localStorage.setItem('cs_platforms', JSON.stringify(defaultPlatforms));
      localStorage.setItem('cs_user_platforms', JSON.stringify(defaultUserPlatforms));
      localStorage.setItem('cs_topics', JSON.stringify(defaultTopics));
      localStorage.setItem('cs_problems', JSON.stringify(defaultProblems));
      localStorage.setItem('cs_initialized', 'true');
    }
  }

  initStorage();

  if (!isGitHubPages) return;

  // Intercept window.fetch for /codestreak/api/* requests when on GitHub Pages
  const originalFetch = window.fetch;
  window.fetch = async function(url, options = {}) {
    const urlStr = typeof url === 'string' ? url : url.url;

    if (!urlStr.includes('/api/')) {
      return originalFetch(url, options);
    }

    const method = (options.method || 'GET').toUpperCase();

    // 1. Auth check
    if (urlStr.includes('/api/auth/me')) {
      return new Response(JSON.stringify({
        success: true,
        data: { id: 1, username: 'sneha', fullName: 'Sneha Chauhan', email: 'sneha@codestreak.dev' }
      }), { status: 200, headers: { 'Content-Type': 'application/json' } });
    }

    // 2. Overview / Analytics
    if (urlStr.includes('/api/analytics/overview')) {
      const problems = JSON.parse(localStorage.getItem('cs_problems') || '[]');
      const userPlatforms = JSON.parse(localStorage.getItem('cs_user_platforms') || '[]');
      const masterPlatforms = JSON.parse(localStorage.getItem('cs_platforms') || '[]');

      let easySolved = 0, mediumSolved = 0, hardSolved = 0;
      let totalAttempted = 0, totalSolved = 0;
      problems.forEach(p => {
        if (p.userStatus === 'SOLVED') {
          totalSolved++;
          if (p.difficulty === 'EASY') easySolved++;
          if (p.difficulty === 'MEDIUM') mediumSolved++;
          if (p.difficulty === 'HARD') hardSolved++;
        } else if (p.userStatus === 'ATTEMPTED') {
          totalAttempted++;
        }
      });

      let externalTotalSolved = 0;
      const connectedPlatforms = userPlatforms.map(up => {
        const mp = masterPlatforms.find(m => m.id === up.platformId) || {};
        const solved = up.problemsSolved || 0;
        externalTotalSolved += solved;
        return {
          ...up,
          platformName: mp.name || 'Platform',
          platformCode: mp.code || ''
        };
      });

      const summary = {
        totalSolved: Math.max(totalSolved, externalTotalSolved),
        totalAttempted: totalAttempted,
        totalProblems: problems.length,
        easySolved, mediumSolved, hardSolved,
        currentStreak: 6,
        longestStreak: 6,
        pendingRevisions: problems.filter(p => p.userStatus === 'REVISION_NEEDED').length,
        connectedPlatforms,
        externalTotalSolved,
        platformSolved: {
          'LeetCode': 184,
          'HackerRank': 70
        }
      };

      const recentSubmissions = [
        { id: 1, problemTitle: 'Longest Substring Without Repeating Characters', status: 'ACCEPTED', language: 'Java', submittedAt: new Date().toISOString() },
        { id: 2, problemTitle: '3Sum', status: 'ACCEPTED', language: 'Java', submittedAt: new Date(Date.now() - 86400000).toISOString() },
        { id: 3, problemTitle: 'Number of Islands', status: 'WRONG_ANSWER', language: 'Java', submittedAt: new Date(Date.now() - 172800000).toISOString() }
      ];

      return new Response(JSON.stringify({
        success: true,
        data: { summary, recentSubmissions }
      }), { status: 200, headers: { 'Content-Type': 'application/json' } });
    }

    // 3. Topics overview
    if (urlStr.includes('/api/analytics/topics')) {
      const topics = JSON.parse(localStorage.getItem('cs_topics') || '[]');
      const problems = JSON.parse(localStorage.getItem('cs_problems') || '[]');

      const data = topics.map(t => {
        const tProbs = problems.filter(p => (p.topics || []).some(pt => pt.id === t.id));
        const solved = tProbs.filter(p => p.userStatus === 'SOLVED').length;
        const attempted = tProbs.filter(p => p.userStatus === 'ATTEMPTED').length;
        const total = Math.max(tProbs.length, 1);
        return {
          topicId: t.id,
          topicName: t.name,
          totalProblems: total,
          solvedCount: solved,
          attemptedCount: attempted,
          completionPercentage: Math.round((solved / total) * 100)
        };
      });

      return new Response(JSON.stringify({ success: true, data }), { status: 200, headers: { 'Content-Type': 'application/json' } });
    }

    // 4. Platforms list
    if (urlStr.includes('/api/platforms') && method === 'GET') {
      const masterPlatforms = JSON.parse(localStorage.getItem('cs_platforms') || '[]');
      const userPlatforms = JSON.parse(localStorage.getItem('cs_user_platforms') || '[]');
      return new Response(JSON.stringify({
        success: true,
        data: { platforms: masterPlatforms, userPlatforms }
      }), { status: 200, headers: { 'Content-Type': 'application/json' } });
    }

    // 5. Auto fetch platform
    if (urlStr.includes('/api/platforms/fetch') && method === 'POST') {
      const body = JSON.parse(options.body || '{}');
      const pCode = body.platformCode || 'leetcode';
      const input = body.input || 'user';

      // Realistic mock responses
      let fetched = {
        username: input.replace(/https?:\/\/[^\/]+\/(?:u\/)?/, '').replace(/\/$/, ''),
        profileUrl: `https://${pCode}.com/${input}`,
        problemsSolved: 120,
        rating: 1540,
        globalRank: 'Knight (Top 10%)'
      };

      if (pCode === 'leetcode') {
        fetched.problemsSolved = 254;
        fetched.rating = 1820;
        fetched.globalRank = 'Knight (Top 5%)';
      } else if (pCode === 'codeforces') {
        fetched.problemsSolved = 0;
        fetched.rating = 1450;
        fetched.globalRank = 'Specialist';
      } else if (pCode === 'hackerrank') {
        fetched.problemsSolved = 75;
        fetched.rating = 610;
        fetched.globalRank = '5★ Problem Solving';
      }

      return new Response(JSON.stringify({ success: true, data: fetched }), { status: 200, headers: { 'Content-Type': 'application/json' } });
    }

    // 6. Save platform configuration
    if (urlStr.includes('/api/platforms') && method === 'POST') {
      const body = JSON.parse(options.body || '{}');
      let userPlatforms = JSON.parse(localStorage.getItem('cs_user_platforms') || '[]');
      const idx = userPlatforms.findIndex(up => up.platformId === body.platformId);
      if (idx >= 0) {
        userPlatforms[idx] = { ...userPlatforms[idx], ...body };
      } else {
        userPlatforms.push({ id: Date.now(), ...body });
      }
      localStorage.setItem('cs_user_platforms', JSON.stringify(userPlatforms));
      return new Response(JSON.stringify({ success: true, message: 'Platform saved' }), { status: 200, headers: { 'Content-Type': 'application/json' } });
    }

    // 7. Problems list
    if (urlStr.includes('/api/problems') && method === 'GET') {
      let problems = JSON.parse(localStorage.getItem('cs_problems') || '[]');
      return new Response(JSON.stringify({
        success: true,
        data: {
          problems,
          pagination: { total: problems.length, page: 1, limit: 100, totalPages: 1 }
        }
      }), { status: 200, headers: { 'Content-Type': 'application/json' } });
    }

    // 8. Update Problem Status
    if (urlStr.includes('/status') && method === 'PUT') {
      const match = urlStr.match(/\/api\/problems\/(\d+)\/status/);
      if (match) {
        const pId = parseInt(match[1]);
        const body = JSON.parse(options.body || '{}');
        let problems = JSON.parse(localStorage.getItem('cs_problems') || '[]');
        const p = problems.find(item => item.id === pId);
        if (p) {
          p.userStatus = body.status;
          localStorage.setItem('cs_problems', JSON.stringify(problems));
        }
      }
      return new Response(JSON.stringify({ success: true }), { status: 200, headers: { 'Content-Type': 'application/json' } });
    }

    // 9. Recommendations list
    if (urlStr.includes('/api/recommendations')) {
      let problems = JSON.parse(localStorage.getItem('cs_problems') || '[]');
      const candidateProblems = problems.filter(p => p.userStatus !== 'SOLVED');

      const recs = candidateProblems.slice(0, 6).map(p => {
        let rationale = 'Optimal standard interview question to bridge easy concepts to advanced ones.';
        let priorityLabel = 'MEDIUM';
        if (p.userStatus === 'REVISION_NEEDED') {
          rationale = 'Flagged for revision. Re-attempt to solidify core patterns.';
          priorityLabel = 'HIGH';
        } else if (p.userStatus === 'ATTEMPTED') {
          rationale = 'You previously attempted this problem. Finishing it will boost your confidence.';
          priorityLabel = 'HIGH';
        }
        return {
          problem: p,
          topic: (p.topics && p.topics[0]) || { name: 'DSA' },
          weaknessScore: 23.5,
          rationale,
          priorityLabel
        };
      });

      return new Response(JSON.stringify({ success: true, data: recs }), { status: 200, headers: { 'Content-Type': 'application/json' } });
    }

    // Fallback pass-through
    return originalFetch(url, options);
  };
})();
