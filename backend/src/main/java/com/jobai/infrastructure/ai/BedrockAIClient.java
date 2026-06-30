package com.jobai.infrastructure.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.Map;

/**
 * Amazon Bedrock client wrapping Claude Sonnet for all AI operations.
 *
 * <p>All prompts follow a consistent system+human message format.
 * Temperature is kept low (0.3) for deterministic, factual outputs —
 * critical for resume content where hallucination is unacceptable.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Job analysis and match scoring</li>
 *   <li>ATS resume tailoring</li>
 *   <li>Cover letter generation</li>
 *   <li>Screening question answering</li>
 * </ul>
 */
@Slf4j
@Component
public class BedrockAIClient {

    private final BedrockRuntimeClient bedrockClient;
    private final ObjectMapper objectMapper;
    private final String modelId;
    private final int maxTokens;

    // Claude Sonnet 4.5 — latest active model on AWS Bedrock (cross-region inference profile)
    private static final String ACTIVE_MODEL_ID = "us.anthropic.claude-sonnet-4-5-20250929-v1:0";

    public BedrockAIClient(
        @Value("${jobai.aws.region}") String region,
        @Value("${jobai.aws.bedrock.max-tokens}") int maxTokens,
        @Value("${AWS_ACCESS_KEY_ID}") String accessKeyId,
        @Value("${AWS_SECRET_ACCESS_KEY}") String secretAccessKey,
        ObjectMapper objectMapper
    ) {
        this.modelId = ACTIVE_MODEL_ID;
        this.maxTokens = maxTokens;
        this.objectMapper = objectMapper;

        this.bedrockClient = BedrockRuntimeClient.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey)
            ))
            .build();

        log.info("Bedrock AI client initialized — model: {}, region: {}", ACTIVE_MODEL_ID, region);
    }

    // ── Public API ────────────────────────────────────────────────

    /**
     * Analyzes a job description and returns a structured JSON analysis.
     *
     * @param jobDescription  full job description text
     * @param candidateProfile summary of candidate skills and background
     * @return JSON string with match score, required skills, gap analysis
     */
    public String analyzeJob(String jobDescription, String candidateProfile) {
        String prompt = """
            You are an expert technical recruiter and ATS specialist.
            Analyze this job description for a 2025 Computer Science fresher candidate.
            
            CANDIDATE PROFILE:
            %s
            
            JOB DESCRIPTION:
            %s
            
            Respond ONLY with a valid JSON object in this exact format:
            {
              "matchScore": <number 0-10, two decimal places>,
              "jobTitle": "<extracted job title>",
              "companyName": "<extracted company name>",
              "location": "<extracted location>",
              "isRemote": <true/false>,
              "experienceRequired": "<e.g., 0-2 years>",
              "salaryRange": "<extracted salary or null>",
              "requiredSkills": ["skill1", "skill2"],
              "preferredSkills": ["skill1"],
              "matchingSkills": ["skills candidate already has"],
              "missingSkills": ["skills candidate lacks"],
              "strengths": ["why candidate is a good fit"],
              "weaknesses": ["gaps or concerns"],
              "isFresherFriendly": <true/false>,
              "atsKeywords": ["important ATS keywords from the JD"],
              "applicationAdvice": "<one sentence advice>",
              "rejectionReason": "<reason if score < 8, else null>"
            }
            
            Rules:
            - matchScore 8-10: apply; below 8: recommend rejection
            - Never fabricate skills the candidate doesn't have
            - If job requires 2+ years mandatory experience, score must be below 8
            - Consider this is a fresher applying for their first job
            """.formatted(candidateProfile, jobDescription);

        return invoke(prompt);
    }

    /**
     * Generates an ATS-optimized resume tailored for a specific job.
     *
     * @param candidateProfile full candidate details
     * @param jobAnalysis      JSON analysis of the job (from analyzeJob)
     * @param jobDescription   original job description
     * @return HTML string representing the resume
     */
    public String generateResume(String candidateProfile, String jobAnalysis, String jobDescription) {
        String prompt = """
            You are a professional resume writer and ATS optimization expert.
            Create an ATS-friendly HTML resume for a 2025 fresher.
            
            STRICT RULES — NEVER VIOLATE:
            1. NEVER fabricate work experience
            2. NEVER invent projects the candidate hasn't done
            3. NEVER claim certifications the candidate doesn't have
            4. NEVER add technologies the candidate doesn't know
            5. Only highlight real skills, projects, and achievements from the profile
            6. Tailor keyword usage to match the job description ATS requirements
            
            CANDIDATE PROFILE:
            %s
            
            JOB ANALYSIS:
            %s
            
            JOB DESCRIPTION (for keyword optimization):
            %s
            
            Generate a complete, professional HTML resume with these sections:
            - Header: Name, contact info, LinkedIn, GitHub
            - Career Objective: 2-3 sentences tailored to this specific role
            - Technical Skills: Organized by category, highlighting job-relevant skills first
            - Education: Degree, college, CGPA, graduation year
            - Projects: 3-4 best projects with tech stack, GitHub link, impact statement
            - Certifications (if any)
            - Achievements/Hackathons (if any)
            
            Use clean, professional HTML with inline CSS. Make it ATS-scannable (no tables for layout, use divs).
            Use standard fonts. Include the ATS keywords from the job naturally.
            Return ONLY the HTML content, no markdown code blocks.
            """.formatted(candidateProfile, jobAnalysis, jobDescription);

        return invoke(prompt);
    }

    /**
     * Generates a personalized, honest cover letter for a specific job.
     *
     * @param candidateProfile full candidate details
     * @param jobAnalysis      JSON analysis of the job
     * @param companyName      target company
     * @param jobTitle         target job title
     * @return plain text cover letter (300-400 words)
     */
    public String generateCoverLetter(
        String candidateProfile,
        String jobAnalysis,
        String companyName,
        String jobTitle
    ) {
        String prompt = """
            You are a professional career coach helping a 2025 Computer Science fresher write cover letters.
            
            STRICT RULES:
            1. Be truthful — only mention real skills and projects from the candidate profile
            2. Do not exaggerate qualifications
            3. Express genuine enthusiasm for learning
            4. Keep it concise: 3-4 paragraphs, 300-400 words
            5. Make it specific to the company and role
            
            CANDIDATE PROFILE:
            %s
            
            JOB ANALYSIS:
            %s
            
            ROLE: %s at %s
            
            Write a professional cover letter that:
            - Opens with genuine interest in the specific role and company
            - Highlights 2-3 most relevant technical skills and projects
            - Mentions academic background and graduation year (2025)
            - Expresses enthusiasm to learn and contribute as a fresher
            - Closes with a call to action
            
            Return only the cover letter text (no "Dear Hiring Manager" if you don't know the name,
            use "Dear Hiring Team" instead). Do not include date or address block.
            """.formatted(candidateProfile, jobAnalysis, jobTitle, companyName);

        return invoke(prompt);
    }

    /**
     * Generates a truthful answer to a screening question.
     *
     * @param question         screening question text
     * @param candidateProfile candidate context
     * @return concise, honest answer
     */
    public String answerScreeningQuestion(String question, String candidateProfile) {
        String prompt = """
            You are helping a 2025 Computer Science fresher answer job application screening questions.
            
            RULE: Only provide truthful answers based on the candidate profile.
            Do NOT fabricate experience, salary history, or qualifications.
            Keep answers concise (1-3 sentences for yes/no questions, 2-4 sentences for open questions).
            
            CANDIDATE PROFILE:
            %s
            
            SCREENING QUESTION: %s
            
            Provide only the answer text, no explanation or preamble.
            """.formatted(candidateProfile, question);

        return invoke(prompt);
    }

    /**
     * Extracts structured profile data from raw resume text using AI.
     *
     * @param resumeText raw text extracted from a PDF resume
     * @return JSON string representing UpdateUserProfileRequest
     */
    public String extractProfileFromResume(String resumeText) {
        String prompt = """
            You are an expert technical recruiter and resume parser.
            I will provide you with the raw text extracted from a candidate's PDF resume.
            Your task is to parse this text and return it as a structured JSON object.
            
            RESUME TEXT:
            %s
            
            Respond ONLY with a valid JSON object in this exact format, conforming to these rules:
            - Do not include fields if you cannot find them in the resume.
            - Format dates as "YYYY-MM-DD".
            - 'skills.category' should be one of: BACKEND, FRONTEND, DATABASE, TOOL, CLOUD, OTHER
            - 'skills.proficiency' should be one of: BEGINNER, INTERMEDIATE, ADVANCED (guess based on context if not explicit, default to INTERMEDIATE)
            
            {
              "fullName": "Extracted Name",
              "phone": "Extracted Phone (numbers only, with + if present)",
              "linkedinUrl": "URL",
              "githubUrl": "URL",
              "portfolioUrl": "URL",
              "collegeName": "College/University Name",
              "degree": "B.Tech, B.E., B.Sc, etc",
              "branch": "Computer Science, etc",
              "graduationYear": 2025,
              "cgpa": 8.5,
              "city": "City name",
              "skills": [
                {
                  "skillName": "Java",
                  "category": "BACKEND",
                  "proficiency": "INTERMEDIATE"
                }
              ],
              "projects": [
                {
                  "projectName": "Name",
                  "description": "Short description",
                  "techStack": "Java, Spring Boot",
                  "githubUrl": "URL",
                  "liveUrl": "URL",
                  "startDate": "YYYY-MM-DD",
                  "endDate": "YYYY-MM-DD"
                }
              ],
              "certifications": [
                {
                  "certName": "Name",
                  "issuer": "Issuer",
                  "issueDate": "YYYY-MM-DD"
                }
              ]
            }
            """.formatted(resumeText);

        return invoke(prompt);
    }

    // ── Private helpers ───────────────────────────────────────────

    /**
     * Invokes the Claude model via Bedrock Runtime.
     * Uses the Messages API format (Anthropic Claude).
     */
    public String invoke(String userMessage) {
        try {
            // Claude Messages API request body
            Map<String, Object> requestBody = Map.of(
                "anthropic_version", "bedrock-2023-05-31",
                "max_tokens", maxTokens,
                "temperature", 0.3,
                "messages", new Object[]{
                    Map.of("role", "user", "content", userMessage)
                }
            );

            String requestJson = objectMapper.writeValueAsString(requestBody);

            InvokeModelRequest request = InvokeModelRequest.builder()
                .modelId(modelId)
                .contentType("application/json")
                .accept("application/json")
                .body(SdkBytes.fromUtf8String(requestJson))
                .build();

            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String responseJson = response.body().asUtf8String();

            // Extract text from Claude response
            Map<?, ?> responseMap = objectMapper.readValue(responseJson, Map.class);
            var content = (java.util.List<?>) responseMap.get("content");
            if (content != null && !content.isEmpty()) {
                Map<?, ?> firstContent = (Map<?, ?>) content.get(0);
                return (String) firstContent.get("text");
            }

            log.warn("Unexpected Bedrock response format: {}", responseJson);
            return "";

        } catch (Exception e) {
            log.error("Bedrock AI invocation failed: {}", e.getMessage(), e);
            throw new RuntimeException("AI service unavailable: " + e.getMessage(), e);
        }
    }
}
