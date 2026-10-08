package com.example.objectverse.mapper;

import com.example.objectverse.entity.KnowledgeNote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeNoteMapper {

    List<KnowledgeNote> findByUserId(@Param("userId") Long userId);

    List<KnowledgeNote> findPublicNotes();

    List<KnowledgeNote> findPublishedByUserId(@Param("userId") Long userId);

    List<KnowledgeNote> findFavoritedByUserId(@Param("userId") Long userId);

    KnowledgeNote findById(@Param("id") Long id);

    int insert(KnowledgeNote note);

    int incrementLikeCount(@Param("id") Long id);

    int decrementLikeCount(@Param("id") Long id);

    int incrementFavoriteCount(@Param("id") Long id);

    int decrementFavoriteCount(@Param("id") Long id);
}
