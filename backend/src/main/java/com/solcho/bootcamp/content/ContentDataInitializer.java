package com.solcho.bootcamp.content;

import com.solcho.bootcamp.content.entity.Content;
import com.solcho.bootcamp.content.entity.ContentChapter;
import com.solcho.bootcamp.content.repository.ContentChapterRepository;
import com.solcho.bootcamp.content.repository.ContentRepository;
import com.solcho.bootcamp.unit.entity.Unit;
import com.solcho.bootcamp.unit.repository.UnitRepository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

/**
 * 학습 본문(HTML)을 classpath({@code resources/content/*.html})에서 읽어
 * content(단원당 1행) + content_chapter(챕터들) 로 시드한다.
 *
 * <p>8단계 개선: 통짜 body 를 챕터로 분리한다. 파일 자체는 그대로 두고 파싱만 새로 한다.
 * 챕터 분리 기준은 "파일 안에서 실제 섹션을 구분하는 최상위 헤딩 레벨"이다:
 * 파일에 &lt;h2&gt; 가 2개 이상이면 h2 로, 아니면(대부분의 이론 단원처럼 h2 는 제목 1개뿐이고
 * 섹션이 h3 인 경우) h3 로 나눈다. 각 챕터 본문은 헤딩을 h2 기준으로 승격해(예: h3→h2, h4→h3)
 * 챕터마다 h2(챕터 제목) + h3(하위 섹션) 구조로 통일한다(프론트 목차 스캔이 h2/h3 로 일관되게 동작).
 *
 * <p>idempotent: 파일에서 파싱한 챕터 목록과 DB 의 기존 챕터를 (제목/본문/순서) 비교해
 * 다를 때만 해당 content 의 챕터를 전부 지우고 다시 만든다.
 *
 * <p>단원 시드(UnitDataInitializer) 이후 실행되도록 @Bean 메서드에 @Order(2)를 둔다.
 * (CommandLineRunner 실행 순서는 @Configuration 클래스가 아니라 @Bean 팩토리 메서드의 @Order 로 결정된다.)
 */
@Configuration
public class ContentDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(ContentDataInitializer.class);

    private record Seed(String code, String title, String file) {}

    private static final Seed[] SEEDS = {
            new Seed("cloud-intro", "클라우드 개론", "cloud-intro.html"),
            new Seed("linux", "리눅스 기초 — 개념, 명령어, 네트워크", "linux.html"),
            new Seed("shell", "쉘 스크립트 (Shell Script)", "shell.html"),
            new Seed("python", "파이썬 기초 정리", "python.html"),
            new Seed("aws", "AWS 핵심 서비스", "aws.html"),
            new Seed("docker", "도커(Docker)", "docker.html"),
            new Seed("network", "네트워크 기초와 패킷 분석", "network.html"),
            new Seed("security", "해킹과 보안 — 공격 기법과 탐지·방어 이론", "security.html"),
            new Seed("k8s", "쿠버네티스(K8s) 실무 — 아키텍처, 설치, 운영 명령어, 네임스페이스", "k8s.html"),
            new Seed("etc", "알아두면 좋은 보충 지식", "etc.html"),
            // 실습(PRACTICE) 단원 노트도 동일하게 챕터로 분리한다.
            new Seed("linux-practice", "리눅스 (실습)", "linux-practice.html"),
            new Seed("shell-practice", "쉘 스크립트 (실습)", "shell-practice.html"),
            new Seed("server-build-practice", "서버 구축 (실습)", "server-build-practice.html"),
            new Seed("python-practice", "파이썬 (실습)", "python-practice.html"),
            new Seed("database-practice", "데이터베이스 (실습)", "database-practice.html"),
            new Seed("docker-practice", "도커 (실습)", "docker-practice.html"),
            new Seed("k8s-practice", "쿠버네티스 (실습)", "k8s-practice.html"),
    };

    @Bean
    @Order(2)
    CommandLineRunner seedContents(UnitRepository unitRepository,
                                   ContentRepository contentRepository,
                                   ContentChapterRepository chapterRepository) {
        return args -> {
            int contentsCreated = 0;
            int chaptersReseeded = 0;
            for (Seed seed : SEEDS) {
                Unit unit = unitRepository.findByCode(seed.code()).orElse(null);
                if (unit == null) {
                    continue;
                }
                // 1) content 행(단원당 1개) 보장 + 제목 갱신
                Optional<Content> existing = contentRepository.findFirstByUnitIdOrderByIdAsc(unit.getId());
                Content content;
                if (existing.isEmpty()) {
                    content = contentRepository.save(Content.builder()
                            .unitId(unit.getId())
                            .title(seed.title())
                            .build());
                    contentsCreated++;
                } else {
                    content = existing.get();
                    if (!content.getTitle().equals(seed.title())) {
                        content.updateTitle(seed.title());
                        contentRepository.save(content);
                    }
                }

                // 2) 챕터 파싱 후, 기존과 다르면 재시드
                List<ParsedChapter> parsed = parseChapters(loadBody(seed.file()), seed.title());
                if (reseedIfChanged(chapterRepository, content.getId(), parsed)) {
                    chaptersReseeded++;
                }
            }
            if (contentsCreated > 0) {
                log.info("학습 콘텐츠(content) 시드 생성: {}개", contentsCreated);
            }
            if (chaptersReseeded > 0) {
                log.info("학습 챕터(content_chapter) 재시드: {}개 단원", chaptersReseeded);
            }
        };
    }

    /** 파싱 결과와 DB 챕터가 다르면 지우고 다시 만든다. @return 재시드했으면 true. */
    private boolean reseedIfChanged(ContentChapterRepository chapterRepository,
                                    Long contentId, List<ParsedChapter> parsed) {
        List<ContentChapter> current = chapterRepository.findByContentIdOrderBySortOrderAsc(contentId);
        if (chaptersEqual(current, parsed)) {
            return false;
        }
        // deleteAllInBatch 는 자체 트랜잭션으로 즉시 커밋된다(파생 deleteBy 의 트랜잭션 의존성 회피).
        if (!current.isEmpty()) {
            chapterRepository.deleteAllInBatch(current);
        }
        int order = 1;
        for (ParsedChapter pc : parsed) {
            chapterRepository.save(ContentChapter.builder()
                    .contentId(contentId)
                    .title(pc.title())
                    .body(pc.body())
                    .sortOrder(order++)
                    .build());
        }
        return true;
    }

    private boolean chaptersEqual(List<ContentChapter> current, List<ParsedChapter> parsed) {
        if (current.size() != parsed.size()) {
            return false;
        }
        for (int i = 0; i < current.size(); i++) {
            ContentChapter c = current.get(i);
            ParsedChapter p = parsed.get(i);
            if (!c.getTitle().equals(p.title()) || !c.getBody().equals(p.body())) {
                return false;
            }
        }
        return true;
    }

    private String loadBody(String fileName) {
        try {
            return StreamUtils.copyToString(
                    new ClassPathResource("content/" + fileName).getInputStream(),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("학습 콘텐츠 파일을 읽지 못했습니다: " + fileName, e);
        }
    }

    // ----------------------------------------------------------------------
    // 챕터 파서
    // ----------------------------------------------------------------------

    private record ParsedChapter(String title, String body) {}

    /** 여는 헤딩 태그를 레벨별로 세거나 찾기 위한 패턴. */
    private static Pattern openTagPattern(int level) {
        return Pattern.compile("<h" + level + "(?:\\s[^>]*)?>", Pattern.CASE_INSENSITIVE);
    }

    private static int countOpenTags(String html, int level) {
        Matcher m = openTagPattern(level).matcher(html);
        int n = 0;
        while (m.find()) {
            n++;
        }
        return n;
    }

    /**
     * HTML 을 챕터로 분리한다.
     * @param fallbackTitle 헤딩이 전혀 없을 때 단일 챕터 제목으로 사용.
     */
    List<ParsedChapter> parseChapters(String html, String fallbackTitle) {
        int level = countOpenTags(html, 2) >= 2 ? 2 : 3;
        Matcher starts = openTagPattern(level).matcher(html);

        List<Integer> positions = new ArrayList<>();
        while (starts.find()) {
            positions.add(starts.start());
        }

        List<ParsedChapter> chapters = new ArrayList<>();
        if (positions.isEmpty()) {
            // 섹션 헤딩이 없으면 파일 전체를 챕터 1개로.
            chapters.add(new ParsedChapter(fallbackTitle, promote(html.trim(), level)));
            return chapters;
        }

        String preamble = html.substring(0, positions.get(0)).trim();
        // 선두 <h2>제목</h2> 은 content.title 과 중복이므로 제거하고, 남는 도입부만 첫 챕터에 붙인다.
        preamble = stripLeadingH2(preamble).trim();

        int delta = 2 - level; // h(level) 을 h2 로 승격하기 위한 이동량(≤0)
        for (int i = 0; i < positions.size(); i++) {
            int from = positions.get(i);
            int to = (i + 1 < positions.size()) ? positions.get(i + 1) : html.length();
            String segment = html.substring(from, to);
            String title = extractHeadingText(segment, level);
            String body = promote(segment, level);
            if (i == 0 && !preamble.isEmpty()) {
                body = promote(preamble, level) + "\n" + body;
            }
            chapters.add(new ParsedChapter(title, body.trim()));
        }
        return chapters;
    }

    /** 선두의 <h2>...</h2> 한 개를 제거(도입부 정리용). */
    private static String stripLeadingH2(String html) {
        Matcher m = Pattern.compile("^\\s*<h2(?:\\s[^>]*)?>.*?</h2>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(html);
        if (m.find()) {
            return html.substring(m.end());
        }
        return html;
    }

    /** segment 안 첫 헤딩(level)의 텍스트를 태그 제거 + 기본 엔티티 디코드해서 반환. */
    private static String extractHeadingText(String segment, int level) {
        Matcher m = Pattern.compile("<h" + level + "(?:\\s[^>]*)?>(.*?)</h" + level + ">",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(segment);
        String raw = m.find() ? m.group(1) : segment;
        String text = raw.replaceAll("<[^>]+>", "").trim();
        return decodeEntities(text);
    }

    /**
     * 헤딩 레벨을 delta(=2-level) 만큼 승격해 챕터 본문이 h2 부터 시작하도록 통일한다.
     * delta ≤ 0 이므로 낮은 번호로 이동한다(예: level=3 이면 h3→h2, h4→h3, h5→h4, h6→h5).
     */
    private static String promote(String html, int level) {
        int delta = 2 - level;
        if (delta == 0) {
            return html;
        }
        String out = html;
        for (int n = level; n <= 6; n++) {
            int target = n + delta;
            out = out.replaceAll("(?i)<h" + n + "(\\s|>|/)", "<h" + target + "$1");
            out = out.replaceAll("(?i)</h" + n + ">", "</h" + target + ">");
        }
        return out;
    }

    private static String decodeEntities(String s) {
        return s.replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&nbsp;", " ");
    }
}
