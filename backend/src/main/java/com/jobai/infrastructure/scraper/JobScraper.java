package com.jobai.infrastructure.scraper;

import com.jobai.domain.job.entity.Job;
import com.jobai.domain.job.entity.JobSource;

import java.util.List;

/**
 * Common interface for all job scraper implementations.
 *
 * <p>Each scraper targets one platform and is responsible for:
 * <ul>
 *   <li>Fetching job listings (HTTP or API)</li>
 *   <li>Parsing to {@link Job} entities</li>
 *   <li>Filtering to fresher-only jobs</li>
 *   <li>Respecting rate limits</li>
 * </ul>
 */
public interface JobScraper {

    /**
     * Scrapes the target platform and returns raw job entities.
     * Jobs are not yet analyzed or scored at this stage.
     */
    List<Job> scrape();

    /**
     * Returns the platform this scraper targets.
     */
    JobSource getSource();
}
