package com.example.objectverse.service;

import com.example.objectverse.entity.KnowledgeNote;
import com.example.objectverse.entity.PracticeQuestion;

import java.util.List;

public interface KnowledgePracticeService {

    List<KnowledgeNote> findNotes(Long userId);

    KnowledgeNote saveNote(Long userId, String title, String category, String content, String sourceType);

    KnowledgeNote publishCommunityNote(Long userId, String title, String category, String description, String content, String tags, String imageUrl);

    List<KnowledgeNote> findPublicNotes();

    List<KnowledgeNote> findPublishedNotes(Long userId);

    List<KnowledgeNote> findFavoritedNotes(Long userId);

    KnowledgeNote findNote(Long id);

    boolean hasReaction(Long noteId, Long userId, String reactionType);

    void toggleReaction(Long noteId, Long userId, String reactionType);

    List<PracticeQuestion> findQuestions(Long userId);

    List<PracticeQuestion> generateQuestions(Long userId);
}
