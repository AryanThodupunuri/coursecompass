package com.coursecompass.service;

import com.coursecompass.api.dto.AnalyzeResponse;
import com.coursecompass.model.CourseCache;
import com.coursecompass.model.CourseCacheRepository;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
public class ScraperService {

  private static final String RMP_GRAPHQL_URL = "https://www.ratemyprofessors.com/graphql";
  private static final String RMP_REFERER = "https://www.ratemyprofessors.com/";
  private static final String GITHUB_SEARCH_URL = "https://api.github.com/search/repositories";
  // UVA's internal school ID on RMP is not guaranteed stable; we keep it configurable.
  // Replace with the correct ID when you confirm it.
  private static final String RMP_UVA_SCHOOL_ID = "U2Nob29sLTEwOTQ="; // UVA School-1094
  private static final Duration CACHE_TTL = Duration.ofDays(14);

  private final CourseCacheRepository courseCacheRepository;

  public ScraperService(CourseCacheRepository courseCacheRepository) {
    this.courseCacheRepository = courseCacheRepository;
  }

  public AnalyzeResponse analyze(String professorName, String courseId) {
    Double rating = null;
    String summary = null;
    List<String> reddit = Collections.emptyList();
    List<String> github = Collections.emptyList();

    CourseCache cached = getCachedEntry(professorName, courseId).orElse(null);
    if (cached != null && cached.getAvgRating() != null && isCacheFresh(cached)) {
      rating = cached.getAvgRating();
    }

    try {
      Double fetched = fetchRateMyProfessorAvgRating(professorName);
      if (fetched != null) {
        rating = fetched;
      }
    } catch (Exception ignored) {
      // Keep the API resilient; we still return reddit results.
    }

    if (rating == null && cached != null) {
      rating = cached.getAvgRating();
    }

    try {
      reddit = fetchRedditTopThreadTitles(courseId);
      summary = summarizeFromRedditTitles(reddit);
    } catch (Exception ignored) {
      // no-op
    }

    try {
      github = fetchGithubTopRepos(professorName, courseId);
    } catch (Exception ignored) {
      // no-op
    }

    // Persist cache (best-effort)
    persistCache(cached, professorName, courseId, rating, summary);

    return new AnalyzeResponse(professorName, courseId, rating, summary, reddit, github);
  }

  /**
   * Method A (RateMyProfessor)
   *
   * Uses Jsoup (no Selenium) to POST a GraphQL query.
   *
   * Notes:
   * - RMP uses persisted queries + specific headers in production. This is a *starter* structure.
   * - You may need to add headers/cookies, and adjust the query shape as RMP changes.
   */
  public Double fetchRateMyProfessorAvgRating(String professorName) throws IOException {
    // Minimal "search" style query. This may not work without the correct query/headers.
    // We keep the structure so it’s easy to iterate.

    String[] parts = professorName.split(",");
    String lastName = parts.length > 0 ? parts[0].trim() : professorName.trim();
    String firstName = parts.length > 1 ? parts[1].trim() : "";

  String graphql = "{" +
    "\"operationName\":\"SearchTeacher\"," +
    "\"query\":\"query SearchTeacher($query: TeacherSearchQuery!) {\n" +
        "  newSearch {\n" +
        "    teachers(query: $query) {\n" +
        "      edges {\n" +
        "        node {\n" +
        "          firstName\n" +
        "          lastName\n" +
        "          avgRating\n" +
        "          numRatings\n" +
        "        }\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}\", " +
        "\"variables\":{" +
        "\"query\":{" +
        "\"text\":\"" + escapeJson(firstName + " " + lastName) + "\"," +
        "\"schoolID\":\"" + escapeJson(RMP_UVA_SCHOOL_ID) + "\"," +
        "\"fallback\":true" +
        "}" +
        "}" +
        "}";

    Connection.Response res = Jsoup.connect(RMP_GRAPHQL_URL)
        .timeout((int) Duration.ofSeconds(10).toMillis())
        .ignoreContentType(true)
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
    .header("Origin", RMP_REFERER)
    .header("Referer", RMP_REFERER)
    .header("Accept-Language", "en-US,en;q=0.9")
    // Some sites require a UA.
    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36")
        .method(Connection.Method.POST)
        .requestBody(graphql)
        .execute();

    String body = res.body();

    // Lightweight parsing to avoid adding a JSON lib in Phase 1.
    // We pessimistically return null if parsing fails.
    // Looks for the first "avgRating":<number>
    int idx = body.indexOf("\"avgRating\"");
    if (idx < 0) return null;
    int colon = body.indexOf(':', idx);
    if (colon < 0) return null;

    int start = colon + 1;
    while (start < body.length() && Character.isWhitespace(body.charAt(start))) start++;

    int end = start;
    while (end < body.length() && (Character.isDigit(body.charAt(end)) || body.charAt(end) == '.')) end++;

    if (end == start) return null;

    return Double.parseDouble(body.substring(start, end));
  }

  private Optional<CourseCache> getCachedEntry(String professorName, String courseId) {
    return courseCacheRepository.findFirstByProfessorNameAndCourseIdOrderByLastUpdatedDesc(
        professorName,
        courseId
    );
  }

  private boolean isCacheFresh(CourseCache cached) {
    Instant updated = cached.getLastUpdated();
    if (updated == null) return false;
    return updated.isAfter(Instant.now().minus(CACHE_TTL));
  }

  private void persistCache(
      CourseCache cached,
      String professorName,
      String courseId,
      Double avgRating,
      String sentimentSummary
  ) {
    try {
      CourseCache entry = cached != null ? cached : new CourseCache();
      entry.setProfessorName(professorName);
      entry.setCourseId(courseId);
      if (avgRating != null) {
        entry.setAvgRating(avgRating);
      }
      if (sentimentSummary != null) {
        entry.setSentimentSummary(sentimentSummary);
      }
      entry.setLastUpdated(Instant.now());
      courseCacheRepository.save(entry);
    } catch (Exception ignored) {
      // Cache failures shouldn't break the response.
    }
  }

  /**
   * Method B (Reddit)
   *
   * Fetches r/UVA search results JSON and returns top 3 thread titles.
   */
  public List<String> fetchRedditTopThreadTitles(String courseId) throws IOException {
    String q = URLEncoder.encode(courseId, StandardCharsets.UTF_8);
    String url = "https://www.reddit.com/r/UVA/search.json?q=" + q + "&restrict_sr=1&sort=relevance&t=all";

    Connection.Response res = Jsoup.connect(url)
        .timeout((int) Duration.ofSeconds(10).toMillis())
        .ignoreContentType(true)
        .header("Accept", "application/json")
        .userAgent("CourseCompass/0.1 (contact: local-dev)")
        .method(Connection.Method.GET)
        .execute();

    String json = res.body();

    // Very small JSON extraction for "title":"...".
    // For production, swap to Jackson for robust parsing.
    List<String> titles = new ArrayList<>();
    int from = 0;
    while (titles.size() < 3) {
      int tIdx = json.indexOf("\"title\"", from);
      if (tIdx < 0) break;
      int colon = json.indexOf(':', tIdx);
      if (colon < 0) break;
      int firstQuote = json.indexOf('"', colon + 1);
      if (firstQuote < 0) break;
      int secondQuote = findStringEnd(json, firstQuote + 1);
      if (secondQuote < 0) break;

      String raw = json.substring(firstQuote + 1, secondQuote);
      titles.add(unescapeJson(raw));
      from = secondQuote + 1;
    }

    return titles;
  }

  /**
   * Method C (GitHub)
   *
   * Uses the GitHub Search API to find relevant repos.
   * Optionally uses a token if GITHUB_TOKEN is set (higher rate limits).
   */
  public List<String> fetchGithubTopRepos(String professorName, String courseId) throws IOException {
    String query = (courseId + " " + professorName + " UVA").trim();
    String q = URLEncoder.encode(query, StandardCharsets.UTF_8);
    String url = GITHUB_SEARCH_URL + "?q=" + q + "&sort=stars&order=desc&per_page=3";

    Connection connection = Jsoup.connect(url)
        .timeout((int) Duration.ofSeconds(10).toMillis())
        .ignoreContentType(true)
        .header("Accept", "application/vnd.github+json")
        .header("X-GitHub-Api-Version", "2022-11-28")
        .userAgent("CourseCompass/0.1 (contact: local-dev)");

    String token = System.getenv("GITHUB_TOKEN");
    if (token != null && !token.isBlank()) {
      connection.header("Authorization", "Bearer " + token.trim());
    }

    Connection.Response res = connection.method(Connection.Method.GET).execute();
    String json = res.body();

    List<String> repos = new ArrayList<>();
    int from = 0;
    while (repos.size() < 3) {
      int nameIdx = json.indexOf("\"full_name\"", from);
      if (nameIdx < 0) break;
      int nameColon = json.indexOf(':', nameIdx);
      if (nameColon < 0) break;
      int nameStart = json.indexOf('"', nameColon + 1);
      if (nameStart < 0) break;
      int nameEnd = findStringEnd(json, nameStart + 1);
      if (nameEnd < 0) break;

      String fullName = unescapeJson(json.substring(nameStart + 1, nameEnd));

      int urlIdx = json.indexOf("\"html_url\"", nameEnd);
      if (urlIdx < 0) break;
      int urlColon = json.indexOf(':', urlIdx);
      if (urlColon < 0) break;
      int urlStart = json.indexOf('"', urlColon + 1);
      if (urlStart < 0) break;
      int urlEnd = findStringEnd(json, urlStart + 1);
      if (urlEnd < 0) break;

      String htmlUrl = unescapeJson(json.substring(urlStart + 1, urlEnd));

      repos.add(fullName + " — " + htmlUrl);
      from = urlEnd + 1;
    }

    return repos;
  }

  private String summarizeFromRedditTitles(List<String> titles) {
    if (titles == null || titles.isEmpty()) return "No recent Reddit threads found.";
    return "Top threads: " + String.join(" | ", titles);
  }

  private static int findStringEnd(String s, int start) {
    boolean escaped = false;
    for (int i = start; i < s.length(); i++) {
      char c = s.charAt(i);
      if (escaped) {
        escaped = false;
        continue;
      }
      if (c == '\\') {
        escaped = true;
        continue;
      }
      if (c == '"') return i;
    }
    return -1;
  }

  private static String escapeJson(String s) {
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private static String unescapeJson(String s) {
    // Minimal unescape for common sequences in Reddit titles.
    return s
        .replace("\\\"", "\"")
        .replace("\\/", "/")
        .replace("\\n", " ")
        .replace("\\t", " ")
        .replace("\\\\", "\\");
  }
}
