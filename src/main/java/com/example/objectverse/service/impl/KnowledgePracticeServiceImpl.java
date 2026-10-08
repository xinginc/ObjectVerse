package com.example.objectverse.service.impl;

import com.example.objectverse.ai.AiClient;
import com.example.objectverse.ai.SimpleJsonParser;
import com.example.objectverse.entity.KnowledgeNote;
import com.example.objectverse.entity.KnowledgeNoteReaction;
import com.example.objectverse.entity.PracticeQuestion;
import com.example.objectverse.mapper.KnowledgeNoteMapper;
import com.example.objectverse.mapper.KnowledgeNoteReactionMapper;
import com.example.objectverse.mapper.PracticeQuestionMapper;
import com.example.objectverse.service.KnowledgePracticeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class KnowledgePracticeServiceImpl implements KnowledgePracticeService {

    private final KnowledgeNoteMapper knowledgeNoteMapper;
    private final KnowledgeNoteReactionMapper knowledgeNoteReactionMapper;
    private final PracticeQuestionMapper practiceQuestionMapper;
    private final AiClient aiClient;

    public KnowledgePracticeServiceImpl(
            KnowledgeNoteMapper knowledgeNoteMapper,
            KnowledgeNoteReactionMapper knowledgeNoteReactionMapper,
            PracticeQuestionMapper practiceQuestionMapper,
            AiClient aiClient
    ) {
        this.knowledgeNoteMapper = knowledgeNoteMapper;
        this.knowledgeNoteReactionMapper = knowledgeNoteReactionMapper;
        this.practiceQuestionMapper = practiceQuestionMapper;
        this.aiClient = aiClient;
    }

    @Override
    public List<KnowledgeNote> findNotes(Long userId) {
        return knowledgeNoteMapper.findByUserId(userId);
    }

    @Override
    public KnowledgeNote saveNote(Long userId, String title, String category, String content, String sourceType) {
        KnowledgeNote note = new KnowledgeNote();
        note.setUserId(userId);
        note.setTitle(StringUtils.hasText(title) ? title.strip() : "Java/OOP 知识点");
        note.setCategory(StringUtils.hasText(category) ? category.strip() : "Java/OOP");
        note.setDescription("");
        note.setContent(generalizeContent(note.getTitle(), note.getCategory(), content));
        note.setSourceType(StringUtils.hasText(sourceType) ? sourceType : "CODE_REVIEW");
        note.setImageUrl("");
        note.setTags(note.getCategory());
        note.setVisibility("PRIVATE");
        note.setLikeCount(0);
        note.setFavoriteCount(0);
        note.setCreateTime(LocalDateTime.now());
        note.setUpdateTime(LocalDateTime.now());
        note.setDeleted(0);
        knowledgeNoteMapper.insert(note);
        return note;
    }

    @Override
    public KnowledgeNote publishCommunityNote(Long userId, String title, String category, String description, String content, String tags, String imageUrl) {
        KnowledgeNote note = new KnowledgeNote();
        note.setUserId(userId);
        note.setTitle(StringUtils.hasText(title) ? title.strip() : "未命名学习笔记");
        note.setCategory(StringUtils.hasText(category) ? category.strip() : "Java/OOP");
        note.setDescription(StringUtils.hasText(description) ? description.strip() : "");
        note.setContent(StringUtils.hasText(content) ? content.strip() : note.getDescription());
        note.setSourceType("COMMUNITY");
        note.setImageUrl(StringUtils.hasText(imageUrl) ? imageUrl.strip() : "");
        note.setTags(StringUtils.hasText(tags) ? tags.strip() : note.getCategory());
        note.setVisibility("PUBLIC");
        note.setLikeCount(0);
        note.setFavoriteCount(0);
        note.setCreateTime(LocalDateTime.now());
        note.setUpdateTime(LocalDateTime.now());
        note.setDeleted(0);
        knowledgeNoteMapper.insert(note);
        return note;
    }

    @Override
    public List<KnowledgeNote> findPublicNotes() {
        return knowledgeNoteMapper.findPublicNotes();
    }

    @Override
    public List<KnowledgeNote> findPublishedNotes(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return knowledgeNoteMapper.findPublishedByUserId(userId);
    }

    @Override
    public List<KnowledgeNote> findFavoritedNotes(Long userId) {
        return knowledgeNoteMapper.findFavoritedByUserId(userId);
    }

    @Override
    public KnowledgeNote findNote(Long id) {
        if (id == null) {
            return null;
        }
        return knowledgeNoteMapper.findById(id);
    }

    @Override
    public boolean hasReaction(Long noteId, Long userId, String reactionType) {
        return noteId != null
                && userId != null
                && StringUtils.hasText(reactionType)
                && knowledgeNoteReactionMapper.findActive(noteId, userId, reactionType) != null;
    }

    @Override
    @Transactional
    public void toggleReaction(Long noteId, Long userId, String reactionType) {
        if (noteId == null || userId == null || !StringUtils.hasText(reactionType)) {
            return;
        }
        KnowledgeNoteReaction active = knowledgeNoteReactionMapper.findActive(noteId, userId, reactionType);
        if (active != null) {
            knowledgeNoteReactionMapper.softDelete(active.getId());
            updateReactionCount(noteId, reactionType, -1);
            return;
        }
        KnowledgeNoteReaction reaction = new KnowledgeNoteReaction();
        reaction.setNoteId(noteId);
        reaction.setUserId(userId);
        reaction.setReactionType(reactionType);
        reaction.setCreateTime(LocalDateTime.now());
        reaction.setUpdateTime(LocalDateTime.now());
        reaction.setDeleted(0);
        knowledgeNoteReactionMapper.insert(reaction);
        updateReactionCount(noteId, reactionType, 1);
    }

    @Override
    public List<PracticeQuestion> findQuestions(Long userId) {
        return practiceQuestionMapper.findByUserId(userId);
    }

    @Override
    public List<PracticeQuestion> generateQuestions(Long userId) {
        List<KnowledgeNote> notes = knowledgeNoteMapper.findByUserId(userId);
        if (notes.isEmpty()) {
            return List.of();
        }
        List<PracticeQuestion> generated = requestAiQuestions(notes);
        if (generated.isEmpty()) {
            generated = fallbackQuestions(notes);
        }
        practiceQuestionMapper.deleteByUserId(userId);
        for (PracticeQuestion question : generated) {
            question.setUserId(userId);
            question.setCreateTime(LocalDateTime.now());
            question.setUpdateTime(LocalDateTime.now());
            question.setDeleted(0);
            practiceQuestionMapper.insert(question);
        }
        return practiceQuestionMapper.findByUserId(userId);
    }

    private void updateReactionCount(Long noteId, String reactionType, int delta) {
        if ("LIKE".equals(reactionType)) {
            if (delta > 0) {
                knowledgeNoteMapper.incrementLikeCount(noteId);
            } else {
                knowledgeNoteMapper.decrementLikeCount(noteId);
            }
        }
        if ("FAVORITE".equals(reactionType)) {
            if (delta > 0) {
                knowledgeNoteMapper.incrementFavoriteCount(noteId);
            } else {
                knowledgeNoteMapper.decrementFavoriteCount(noteId);
            }
        }
    }

    private List<PracticeQuestion> requestAiQuestions(List<KnowledgeNote> notes) {
        String response = aiClient.chat(buildPrompt(notes));
        try {
            return parseQuestions(SimpleJsonParser.parseObject(extractJson(response)));
        } catch (Exception ex) {
            return List.of();
        }
    }

    private String buildPrompt(List<KnowledgeNote> notes) {
        StringBuilder builder = new StringBuilder();
        builder.append("""
                你是 Java/OOP 知识点练习题生成器。
                请根据知识点生成适合学生练习的题目，必须能脱离具体代码场景独立练习。
                每个知识点至少生成 1 道选择题和 1 道简答题。
                严格返回 JSON，不要 Markdown。
                JSON 结构：
                {
                  "questions": [
                    {
                      "knowledgeNoteId": 1,
                      "questionType": "选择题或简答题",
                      "title": "题目标题",
                      "body": "题干，选择题请包含 A/B/C/D 选项",
                      "answer": "参考答案和解析"
                    }
                  ]
                }
                知识点：
                """);
        for (KnowledgeNote note : notes) {
            builder.append("\nID: ").append(note.getId())
                    .append("\n标题: ").append(note.getTitle())
                    .append("\n分类: ").append(note.getCategory())
                    .append("\n说明: ").append(note.getContent())
                    .append("\n");
        }
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private List<PracticeQuestion> parseQuestions(Map<String, Object> map) {
        List<PracticeQuestion> questions = new ArrayList<>();
        Object value = map.get("questions");
        if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> questionMap) {
                    questions.add(toQuestion((Map<String, Object>) questionMap));
                }
            }
        }
        return questions;
    }

    private PracticeQuestion toQuestion(Map<String, Object> map) {
        PracticeQuestion question = new PracticeQuestion();
        question.setKnowledgeNoteId(asLong(map.get("knowledgeNoteId")));
        question.setQuestionType(asString(map.get("questionType"), "简答题"));
        question.setTitle(asString(map.get("title"), "知识点练习"));
        question.setBody(asString(map.get("body"), ""));
        question.setAnswer(asString(map.get("answer"), ""));
        return question;
    }

    private List<PracticeQuestion> fallbackQuestions(List<KnowledgeNote> notes) {
        List<PracticeQuestion> questions = new ArrayList<>();
        for (KnowledgeNote note : notes) {
            PracticeQuestion choice = new PracticeQuestion();
            choice.setKnowledgeNoteId(note.getId());
            choice.setQuestionType("选择题");
            choice.setTitle("关于“" + note.getTitle() + "”，哪项说法更合理？");
            choice.setBody("A. 只要程序能运行，就不需要关注对象职责。\nB. " + note.getContent() + "\nC. 所有字段都应设为 public。\nD. 面向对象设计不需要封装。");
            choice.setAnswer("参考答案：B。解析：" + note.getContent());
            questions.add(choice);

            PracticeQuestion shortAnswer = new PracticeQuestion();
            shortAnswer.setKnowledgeNoteId(note.getId());
            shortAnswer.setQuestionType("简答题");
            shortAnswer.setTitle("说明“" + note.getTitle() + "”的适用场景");
            shortAnswer.setBody("请结合一个 Java 类设计例子，说明该知识点解决什么问题，以及忽略它可能带来的风险。");
            shortAnswer.setAnswer("参考要点：" + note.getContent());
            questions.add(shortAnswer);
        }
        return questions;
    }

    private String generalizeContent(String title, String category, String content) {
        if (!StringUtils.hasText(content)) {
            return "理解该知识点的适用场景、常见写法和易错点，并能在代码中识别和改进相关问题。";
        }
        String text = content.strip();
        if ((title + category + text).contains("封装") && (text.contains("可变") || text.contains("引用") || text.contains("getter"))) {
            return "封装要求对象保护自己的内部状态。对外暴露数据时，getter 不应直接返回可被外部修改的内部可变对象，而应返回副本、不可变视图或只读抽象，避免调用方绕过对象方法破坏状态一致性。";
        }
        return text
                .replaceAll("(?i)在本代码中|这个类|该类|上述代码|当前代码|这里", "实际编码时")
                .replaceAll("\\s+", " ");
    }

    private String extractJson(String response) {
        if (!StringUtils.hasText(response)) {
            return "{}";
        }
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return response;
    }

    private String asString(Object value, String defaultValue) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return defaultValue;
        }
        return String.valueOf(value);
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }
}
