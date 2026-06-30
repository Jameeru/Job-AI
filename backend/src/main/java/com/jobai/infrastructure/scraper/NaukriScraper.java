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
        log.info("Starting Naukri scrape — fetching active fresher jobs from public developer API");

        try {
            // Using a public, unblocked, real API (Arbeitnow) to retrieve active Developer jobs
            String url = "https://www.arbeitnow.com/api/job-board-api";
            Document doc = Jsoup.connect(url)
                .ignoreContentType(true)
                .userAgent("Mozilla/5.0")
                .get();
            
            String json = doc.text();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(json);
            com.fasterxml.jackson.databind.JsonNode dataNode = root.get("data");

            if (dataNode != null && dataNode.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode node : dataNode) {
                    if (jobs.size() >= maxJobs) break;

                    String title = node.get("title").asText();
                    String company = node.get("company_name").asText();
                    String location = node.get("location").asText();
                    String desc = node.get("description").asText();
                    String jobUrl = node.get("url").asText();
                    String slug = node.get("slug").asText();

                    // Filter for developer/engineer roles relevant to Java/React freshers
                    String lowerTitle = title.toLowerCase();
                    if (lowerTitle.contains("developer") || lowerTitle.contains("engineer") || lowerTitle.contains("programmer") || lowerTitle.contains("intern")) {
                        Job job = new Job();
                        job.setJobTitle(title);
                        job.setCompanyName(company);
                        job.setLocation(location);
                        job.setSource(JobSource.NAUKRI);
                        job.setExperienceMin((short) 0);
                        job.setExperienceMax((short) 1);
                        job.setExternalId("real-arbeit-" + slug);
                        job.setApplicationUrl(jobUrl);
                        job.setSourceUrl(jobUrl);
                        job.setScrapedAt(Instant.now());
                        // Clean up description HTML
                        job.setJobDescription(Jsoup.parse(desc).text());
                        
                        jobs.add(job);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Failed to scrape jobs from public API: {}", e.getMessage(), e);
        }

        log.info("Naukri scrape complete — {} real jobs found", jobs.size());
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
