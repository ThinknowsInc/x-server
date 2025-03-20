package com.thinknows.x_server.service;

import com.thinknows.x_server.model.Post;
import com.thinknows.x_server.model.request.CreatePostRequest;
import com.thinknows.x_server.model.request.UpdatePostRequest;
import com.thinknows.x_server.repository.PostRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    private final UserService userService;
    private final PostRepository postRepository;
    
    @Autowired
    public PostService(UserService userService, PostRepository postRepository) {
        this.userService = userService;
        this.postRepository = postRepository;
    }
    
    /**
     * 创建新帖子
     */
    public Post createPost(CreatePostRequest request, Long authorId) {
        // 获取作者名称（使用随机生成的用户名）
        String authorName = userService.generateRandomUserProfile(authorId).getUsername();
        
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthorId(authorId);
        post.setAuthorName(authorName);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setStatus("PUBLISHED");
        
        if (request.getCategory() != null) {
            post.setCategory(request.getCategory());
        }
        
        if (request.getTags() != null) {
            post.setTags(request.getTags());
        }
        
        return postRepository.save(post);
    }
    
    /**
     * 获取所有帖子
     */
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }
    
    /**
     * 获取特定用户的所有帖子
     */
    public List<Post> getPostsByAuthor(Long authorId) {
        return postRepository.findByAuthorId(authorId);
    }
    
    /**
     * 根据ID获取帖子
     */
    public Post getPostById(Long id) {
        Optional<Post> post = postRepository.findById(id);
        return post.orElse(null);
    }
    
    /**
     * 更新帖子
     */
    public Post updatePost(Long id, UpdatePostRequest request, Long currentUserId) {
        Optional<Post> optionalPost = postRepository.findById(id);
        
        if (optionalPost.isEmpty()) {
            return null;
        }
        
        Post post = optionalPost.get();
        
        // 检查当前用户是否是帖子作者
        if (!post.getAuthorId().equals(currentUserId)) {
            throw new IllegalStateException("只有作者可以更新帖子");
        }
        
        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        
        if (request.getCategory() != null) {
            post.setCategory(request.getCategory());
        }
        
        if (request.getTags() != null) {
            post.setTags(request.getTags());
        }
        
        post.setUpdatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }
    
    /**
     * 删除帖子
     */
    public boolean deletePost(Long id, Long currentUserId) {
        Optional<Post> optionalPost = postRepository.findById(id);
        
        if (optionalPost.isEmpty()) {
            return false;
        }
        
        Post post = optionalPost.get();
        
        // 检查当前用户是否是帖子作者
        if (!post.getAuthorId().equals(currentUserId)) {
            throw new IllegalStateException("只有作者可以删除帖子");
        }
        
        postRepository.deleteById(id);
        return true;
    }
    
    /**
     * 根据标签搜索帖子
     */
    public List<Post> getPostsByTag(String tag) {
        return postRepository.findByTag(tag);
    }
    
    /**
     * 根据分类获取帖子
     */
    public List<Post> getPostsByCategory(String category) {
        return postRepository.findByCategory(category);
    }
    
    /**
     * 搜索帖子
     */
    public List<Post> searchPosts(String keyword) {
        return postRepository.searchByKeyword(keyword);
    }
    
    /**
     * 获取最新帖子
     */
    public List<Post> getRecentPosts(int limit) {
        return postRepository.findTop10ByStatusOrderByCreatedAtDesc("PUBLISHED");
    }
    
    /**
     * 对帖子列表进行排序
     * 
     * @param field 排序字段
     * @param ascending 是否升序
     * @return 排序后的帖子列表
     */
    public List<Post> sortPosts(String field, boolean ascending) {
        Direction direction = ascending ? Direction.ASC : Direction.DESC;
        Sort sort = Sort.by(direction, getFieldName(field));
        return postRepository.findAll(sort);
    }
    
    /**
     * 根据字段名获取对应的数据库字段名
     */
    private String getFieldName(String field) {
        switch (field.toLowerCase()) {
            case "title":
                return "title";
            case "authorname":
                return "authorName";
            case "category":
                return "category";
            case "updatedat":
                return "updatedAt";
            case "createdat":
            default:
                return "createdAt";
        }
    }
}
