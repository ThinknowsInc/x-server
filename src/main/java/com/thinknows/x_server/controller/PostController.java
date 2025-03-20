package com.thinknows.x_server.controller;

import com.thinknows.x_server.model.Post;
import com.thinknows.x_server.model.request.CreatePostRequest;
import com.thinknows.x_server.model.request.UpdatePostRequest;
import com.thinknows.x_server.model.response.ApiResponse;
import com.thinknows.x_server.model.response.PageResponse;
import com.thinknows.x_server.model.response.PostResponse;
import com.thinknows.x_server.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
    
    private final PostService postService;
    
    public PostController(PostService postService) {
        this.postService = postService;
    }
    
    /**
     * 创建新帖子
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @RequestBody CreatePostRequest request,
            @RequestHeader("userId") Long userId) {
        
        Post post = postService.createPost(request, userId);
        PostResponse response = new PostResponse(post);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "帖子创建成功", response));
    }
    
    /**
     * 获取帖子列表（支持分页和多种筛选条件）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> getPosts(
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        List<Post> posts;
        
        if (authorId != null) {
            posts = postService.getPostsByAuthor(authorId);
        } else if (tag != null && !tag.isEmpty()) {
            posts = postService.getPostsByTag(tag);
        } else if (category != null && !category.isEmpty()) {
            posts = postService.getPostsByCategory(category);
        } else {
            posts = postService.getAllPosts();
        }
        
        // 应用排序
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        boolean ascending = sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1]);
        
        // 对已过滤的列表进行内存排序
        posts = manualSort(posts, sortField, ascending);
        
        // 应用分页
        PageResponse<PostResponse> pageResponse = getPageResponse(posts, page, size);
        
        return ResponseEntity.ok(new ApiResponse<>(200, "获取帖子列表成功", pageResponse));
    }
    
    /**
     * 获取特定用户的所有帖子（支持分页）
     */
    @GetMapping("/user/{authorId}")
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> getPostsByAuthor(
            @PathVariable Long authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        List<Post> posts = postService.getPostsByAuthor(authorId);
        
        // 应用排序
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        boolean ascending = sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1]);
        
        // 对已过滤的列表进行内存排序
        posts = manualSort(posts, sortField, ascending);
        
        // 应用分页
        PageResponse<PostResponse> pageResponse = getPageResponse(posts, page, size);
        
        return ResponseEntity.ok(new ApiResponse<>(200, "获取用户帖子列表成功", pageResponse));
    }
    
    /**
     * 根据ID获取帖子
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(@PathVariable Long id) {
        Post post = postService.getPostById(id);
        
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, "帖子不存在", null));
        }
        
        PostResponse response = new PostResponse(post);
        return ResponseEntity.ok(new ApiResponse<>(200, "获取帖子成功", response));
    }
    
    /**
     * 更新帖子
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long id,
            @RequestBody UpdatePostRequest request,
            @RequestHeader("userId") Long userId) {
        
        try {
            Post post = postService.updatePost(id, request, userId);
            
            if (post == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(404, "帖子不存在", null));
            }
            
            PostResponse response = new PostResponse(post);
            return ResponseEntity.ok(new ApiResponse<>(200, "帖子更新成功", response));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(403, e.getMessage(), null));
        }
    }
    
    /**
     * 删除帖子
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long id,
            @RequestHeader("userId") Long userId) {
        
        try {
            boolean deleted = postService.deletePost(id, userId);
            
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(404, "帖子不存在", null));
            }
            
            return ResponseEntity.ok(new ApiResponse<>(200, "帖子删除成功", null));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(403, e.getMessage(), null));
        }
    }
    
    /**
     * 手动对帖子列表进行排序
     */
    private List<Post> manualSort(List<Post> posts, String field, boolean ascending) {
        return posts.stream()
                .sorted((p1, p2) -> {
                    Object value1 = getFieldValue(p1, field);
                    Object value2 = getFieldValue(p2, field);
                    
                    if (value1 == null && value2 == null) return 0;
                    if (value1 == null) return ascending ? -1 : 1;
                    if (value2 == null) return ascending ? 1 : -1;
                    
                    if (value1 instanceof String && value2 instanceof String) {
                        return ascending ? 
                            ((String) value1).compareTo((String) value2) : 
                            ((String) value2).compareTo((String) value1);
                    } else if (value1 instanceof Comparable<?> && value2 instanceof Comparable<?>) {
                        @SuppressWarnings("unchecked")
                        Comparable<Object> comp1 = (Comparable<Object>) value1;
                        return ascending ? 
                            comp1.compareTo(value2) : 
                            ((Comparable<Object>) value2).compareTo(value1);
                    }
                    
                    return 0;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 获取对象的字段值
     */
    private Object getFieldValue(Post post, String field) {
        switch (field.toLowerCase()) {
            case "title":
                return post.getTitle();
            case "authorname":
                return post.getAuthorName();
            case "category":
                return post.getCategory();
            case "updatedat":
                return post.getUpdatedAt();
            case "createdat":
            default:
                return post.getCreatedAt();
        }
    }
    
    /**
     * 辅助方法：创建分页响应
     */
    private PageResponse<PostResponse> getPageResponse(List<Post> posts, int page, int size) {
        int totalElements = posts.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);
        
        List<PostResponse> pageContent;
        if (fromIndex < totalElements) {
            pageContent = posts.subList(fromIndex, toIndex).stream()
                    .map(PostResponse::new)
                    .collect(Collectors.toList());
        } else {
            pageContent = Collections.emptyList();
        }
        
        return new PageResponse<>(
                pageContent,
                page,
                size,
                totalPages,
                totalElements,
                page > 0,
                page < totalPages - 1
        );
    }
}
