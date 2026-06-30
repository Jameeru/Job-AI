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
 * Scrapes Foundit.in (formerly Monster India) for fresher Java jobs.
 *
 * <p>Targets the public search page with fresher filter.
 * URL: https://www.foundit.in/srp/results?query=java+developer+fresher&experienceRanges=0~2
 */
@Slf4j
@Component
public class FounditScraper implements JobScraper {

    private static final String BASE_URL = "https://www.foundit.in";
    private static final String SEARCH_URL =
        BASE_URL + "/srp/results?query=java+developer+fresher&experienceRanges=0~2&jobType=1&pageNo=%d";

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

        log.info("Starting Foundit scrape");

        while (page <= maxPages && jobs.size() < maxJobs) {
            try {
                String url = SEARCH_URL.formatted(page);
                log.debug("Scraping Foundit page {}", page);

                Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .timeout(15000)
                    .get();

                Elements jobCards = doc.select(".srpResultsContainer .cardContainer, [class*='jobcard'], [class*='job-item']");

                if (jobCards.isEmpty()) {
                    consecutiveEmptyPages++;
                    log.warn("No job cards found on Foundit page {} (empty streak: {}). Stopping.",
                             page, consecutiveEmptyPages);
                    if (consecutiveEmptyPages >= 2) {
                        log.warn("Stopping Foundit scrape after {} consecutive empty pages.", consecutiveEmptyPages);
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
                log.error("Foundit scrape failed on page {}: {}", page, e.getMessage());
                break;
            }
        }

        log.info("Foundit scrape complete — {} jobs found", jobs.size());
        return jobs;
    }

    @Override
    public JobSource getSource() {
        return JobSource.FOUNDIT;
    }

    private Job parseJobCard(Element card) {
        try {
            String title = text(card, ".jobTitle, .title, h3, h2");
            String company = text(card, ".companyName, .company, [class*='company']");
            String location = text(card, ".location, [class*='location']");
            String skills = text(card, ".skills, [class*='skill']");
            String href = attr(card, "a[href*='/job/'], a.jobTitle", "href");

            if (title.isBlank() || company.isBlank()) return null;

            String jobUrl = href.startsWith("http") ? href : BASE_URL + href;

            return Job.builder()
                .externalId(extractIdFromUrl(href))
                .source(JobSource.FOUNDIT)
                .jobTitle(title)
                .companyName(company)
                .location(location)
                .requiredSkills(skills)
                .applicationUrl(jobUrl)
                .sourceUrl(jobUrl)
                .experienceMin((short) 0)
                .experienceMax((short) 2)
                .scrapedAt(Instant.now())
                .build();

        } catch (Exception e) {
            log.warn("Failed to parse Foundit job card: {}", e.getMessage());
            return null;
        }
    }

    private String extractIdFromUrl(String url) {
        if (url == null || url.isBlank()) return null;
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    private String text(Element parent, String query) {
        Element el = parent.selectFirst(query);
        return el != null ? el.text().trim() : "";
    }

    private String attr(Element parent, String query, String attr) {
        Element el = parent.selectFirst(query);
        return el != null ? el.attr(attr).trim() : "";
    }

    private void sleepBetweenRequests() {
        try { Thread.sleep(requestDelayMs); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
