package com.example.objectverse.mapper;

import com.example.objectverse.entity.AiConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiConversationMapper {

    void insert(AiConversation conversation);

    List<AiConversation> findByProjectId(@Param("projectId") Long projectId);
}
