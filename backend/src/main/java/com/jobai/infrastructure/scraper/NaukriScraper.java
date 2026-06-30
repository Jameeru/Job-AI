package com.jobai.infrastructure.scraper;

import com.jobai.domain.job.entity.Job;
import com.jobai.domain.job.entity.JobSource;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Scrapes Naukri.com for fresher Java jobs using their public search pages.
 *
 * <p>Uses Jsoup HTTP client with browser-like headers to fetch search results.
 * Rate-limited via configurable delay. Respects site load — this is not aggressive scraping.
 *
 * <p>Target URL pattern:
 * https://www.naukri.com/java-developer-jobs-freshers?experience=0
 */
@Slf4j
@Component
public class NaukriScraper implements JobScraper {

    private static final String BASE_URL = "https://www.naukri.com";
    private static final String SEARCH_URL =
        BASE_URL + "/java-developer-jobs-freshers?experience=0&jobAge=1&pageNo=%d";

    @Value("${jobai.scraper.request-delay-ms}")
    private long requestDelayMs;

    @Value("${jobai.scraper.max-jobs-per-source}")
    private int maxJobs;

    @Override
    public List<Job> scrape() {
        List<Job> jobs = new ArrayList<>();
        int page = 1;
        int maxPages = 3;
        int consecutiveEmptyPages = 0;

        log.info("Starting Naukri scrape — target: {} jobs", maxJobs);

        while (page <= maxPages && jobs.size() < maxJobs) {
            try {
                String url = SEARCH_URL.formatted(page);
                log.debug("Scraping Naukri page {}: {}", page, url);

                Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "en-IN,en;q=0.9")
                    .timeout(15000)
                    .get();

                Elements jobCards = doc.select("article.jobTuple");

                if (jobCards.isEmpty()) {
                    // Try alternative selectors (Naukri may change their DOM)
                    jobCards = doc.select("[class*='jobTuple'], [class*='job-container']");
                }

                if (jobCards.isEmpty()) {
                    consecutiveEmptyPages++;
                    log.warn("No job cards found on Naukri page {} (empty streak: {}). Site structure may have changed.",
                             page, consecutiveEmptyPages);
                    if (consecutiveEmptyPages >= 2) {
                        log.warn("Stopping Naukri scrape after {} consecutive empty pages.", consecutiveEmptyPages);
                        break;
                    }
                } else {
                    consecutiveEmptyPages = 0; // reset on a productive page
                    for (Element card : jobCards) {
                        if (jobs.size() >= maxJobs) break;
                        Job job = parseJobCard(card);
                        if (job != null) jobs.add(job);
                    }
                }

                page++;
                sleepBetweenRequests();

            } catch (Exception e) {
                log.error("Naukri scrape failed on page {}: {}", page, e.getMessage());
                break;
            }
        }

        log.info("Naukri scrape complete — {} jobs found", jobs.size());
        return jobs;
    }

    @Override
    public JobSource getSource() {
        return JobSource.NAUKRI;
    }

    private Job parseJobCard(Element card) {
        try {
            String title = text(card, "a.title, .jobTitle, [class*='title']");
            String company = text(card, "a.subTitle, .companyName, [class*='company']");
            String location = text(card, ".location, [class*='location']");
            String experience = text(card, ".experience, [class*='experience']");
            String skills = text(card, ".tags li, .skillsList li, [class*='tag']");
            String jobUrl = attr(card, "a.title, a[href*='/job-listings']", "href");

            if (title.isBlank() || company.isBlank()) return null;

            // Only fresher jobs
            if (experience.contains("3") || experience.contains("4") || experience.contains("5")) return null;

            String externalId = extractNaukriJobId(jobUrl);

            return Job.builder()
                .externalId(externalId)
                .source(JobSource.NAUKRI)
                .jobTitle(title)
                .companyName(company)
                .location(location)
                .requiredSkills(skills)
                .applicationUrl(jobUrl.startsWith("http") ? jobUrl : BASE_URL + jobUrl)
                .sourceUrl(jobUrl.startsWith("http") ? jobUrl : BASE_URL + jobUrl)
                .experienceMin((short) 0)
                .experienceMax((short) 2)
                .scrapedAt(Instant.now())
                .build();

        } catch (Exception e) {
            log.warn("Failed to parse Naukri job card: {}", e.getMessage());
            return null;
        }
    }

    private String extractNaukriJobId(String url) {
        if (url == null || url.isBlank()) return null;
        // Naukri URLs end in -XXXXXXX format
        String[] parts = url.split("-");
        if (parts.length > 0) return parts[parts.length - 1].replaceAll("[^0-9]", "");
        return null;
    }

    private String text(Element parent, String cssQuery) {
        Element el = parent.selectFirst(cssQuery);
        return el != null ? el.text().trim() : "";
    }

    private String attr(Element parent, String cssQuery, String attr) {
        Element el = parent.selectFirst(cssQuery);
        return el != null ? el.attr(attr).trim() : "";
    }

    private void sleepBetweenRequests() {
        try {
            Thread.sleep(requestDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
