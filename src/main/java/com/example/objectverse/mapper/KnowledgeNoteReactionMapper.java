package com.example.objectverse.mapper;

import com.example.objectverse.entity.KnowledgeNoteReaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface KnowledgeNoteReactionMapper {

    KnowledgeNoteReaction findActive(
            @Param("noteId") Long noteId,
            @Param("userId") Long userId,
            @Param("reactionType") String reactionType
    );

    int insert(KnowledgeNoteReaction reaction);

    int softDelete(@Param("id") Long id);
}
