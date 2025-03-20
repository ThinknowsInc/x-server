package com.thinknows.x_server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.thinknows.x_server.model.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // 根据作者ID查找帖子
    List<Post> findByAuthorId(Long authorId);
    
    // 根据状态查找帖子
    List<Post> findByStatus(String status);
    
    // 根据分类查找帖子
    List<Post> findByCategory(String category);
    
    // 根据标签查找帖子
    @Query("SELECT p FROM Post p JOIN p.tags t WHERE t = :tag")
    List<Post> findByTag(@Param("tag") String tag);
    
    // 根据标题或内容搜索帖子
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    List<Post> searchByKeyword(@Param("keyword") String keyword);
    
    // 查找最近的帖子
    List<Post> findTop10ByStatusOrderByCreatedAtDesc(String status);
}
