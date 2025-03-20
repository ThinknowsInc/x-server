/**
 * Spring Boot 控制器示例 - PostController
 * 
 * 这个文件展示了 Spring Boot REST API 控制器的典型结构和最佳实践
 * 
 * 【Spring MVC 核心概念】
 * 1. @RestController: 标记这个类是一个 REST API 控制器，自动将返回值转换为 JSON
 * 2. @RequestMapping: 定义 API 的基本路径
 * 3. @GetMapping, @PostMapping 等: 定义不同 HTTP 方法的处理器
 * 4. ResponseEntity: 封装 HTTP 响应，包括状态码、头部和正文
 * 5. 依赖注入: 通过构造函数注入服务，而不是使用字段注入
 */
package com.thinknows.x_server.controller;

import com.thinknows.x_server.model.Post;
import com.thinknows.x_server.model.request.CreatePostRequest;
import com.thinknows.x_server.model.request.UpdatePostRequest;
import com.thinknows.x_server.model.response.ApiResponse;
import com.thinknows.x_server.model.response.PageResponse;
import com.thinknows.x_server.model.response.PostResponse;
import com.thinknows.x_server.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @RestController: 结合了 @Controller 和 @ResponseBody，表示这个类处理 HTTP 请求并直接返回对象（自动转为 JSON）
 * @RequestMapping: 定义这个控制器处理的基本 URL 路径
 * 
 * 【RESTful API 设计原则】
 * - 使用 HTTP 方法表达语义: GET(读取), POST(创建), PUT(更新), DELETE(删除)
 * - 使用 URL 路径表示资源: /posts, /posts/{id}
 * - 使用 HTTP 状态码表示操作结果: 200(成功), 201(创建成功), 404(未找到), 400(请求错误), 500(服务器错误)
 * - 使用查询参数进行过滤、排序和分页: ?page=0&size=10&sort=createdAt,desc
 */
@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
    
    /**
     * 【依赖注入最佳实践】
     * 1. 使用 final 字段: 确保服务引用不会被修改
     * 2. 构造函数注入: Spring 推荐的依赖注入方式，比 @Autowired 字段注入更好
     *    - 使依赖明确且不可变
     *    - 便于单元测试
     *    - 防止循环依赖
     *    - 当只有一个构造函数时，@Autowired 注解可以省略
     */
    private final PostService postService;
    
    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }
    
    /**
     * 创建新帖子
     * 
     * 【Spring MVC 注解解析】
     * @PostMapping: 处理 POST 请求，用于创建资源
     * @RequestBody: 将请求体 JSON 自动转换为 Java 对象
     * @RequestHeader: 获取请求头中的值
     * 
     * 【返回值说明】
     * ResponseEntity: 允许完全控制 HTTP 响应，包括状态码、头部和响应体
     * ApiResponse: 自定义的统一响应格式，包含状态码、消息和数据
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
     * 
     * 【请求参数处理】
     * @GetMapping: 处理 GET 请求，用于获取资源
     * @RequestParam: 获取查询参数
     *   - required = false: 参数可选
     *   - defaultValue: 参数默认值
     * 
     * 【分页和排序】
     * 这里展示了如何手动实现分页和排序逻辑
     * 在实际项目中，可以使用 Spring Data 的 Pageable 参数自动处理
     * 例如: public Page<Post> findAll(Pageable pageable);
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
     * 
     * 【路径变量】
     * @PathVariable: 获取 URL 路径中的变量
     * 例如: /user/123 中的 123 会被绑定到 authorId 参数
     * 
     * 【URL 路径设计】
     * RESTful API 中，URL 路径应该表示资源的层次结构
     * /posts/user/{authorId} 表示特定用户的帖子资源
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
     * 
     * 【错误处理】
     * 这里展示了如何处理资源不存在的情况
     * 返回适当的 HTTP 状态码（404 Not Found）和错误消息
     * 
     * 【最佳实践】
     * 在实际项目中，应该使用全局异常处理器统一处理异常
     * 例如使用 @ControllerAdvice 和 @ExceptionHandler
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> fetchPostById(@PathVariable Long id) {
        // 显式指定类型以避免 IDE 的错误提示
        com.thinknows.x_server.model.Post post = this.postService.getPostById(id);
        
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, "帖子不存在", null));
        }
        
        PostResponse response = new PostResponse(post);
        return ResponseEntity.ok(new ApiResponse<>(200, "获取帖子成功", response));
    }
    
    /**
     * 更新帖子
     * 
     * 【HTTP 方法选择】
     * @PutMapping: 处理 PUT 请求，用于更新资源
     * PUT vs PATCH:
     *   - PUT: 完全替换资源
     *   - PATCH: 部分更新资源
     * 
     * 【异常处理】
     * 这里展示了如何捕获业务逻辑异常并转换为适当的 HTTP 响应
     * 例如，当用户尝试更新不属于他们的帖子时返回 403 Forbidden
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> modifyPost(
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
     * 
     * 【HTTP 方法】
     * @DeleteMapping: 处理 DELETE 请求，用于删除资源
     * 
     * 【返回值】
     * 删除操作通常返回:
     * - 204 No Content: 表示成功删除但没有返回内容
     * - 200 OK: 如果返回一些确认信息（如本例）
     * 
     * 【安全检查】
     * 在删除操作前，应该验证用户是否有权限执行此操作
     * 这里通过 userId 参数检查当前用户是否是帖子的作者
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removePost(
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
     * 
     * 【Java Stream API】
     * 这里使用 Java 8+ Stream API 进行集合操作
     * - stream(): 创建流
     * - sorted(): 排序
     * - collect(): 收集结果
     * 
     * 【Spring Data 替代方案】
     * 在实际项目中，可以使用 Spring Data 的排序功能:
     * Sort sort = Sort.by(direction, properties);
     * repository.findAll(sort);
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
                        // 使用单个 @SuppressWarnings 注解抑制整个代码块的未检查警告
                        @SuppressWarnings("unchecked")
                        int result = ascending ? 
                            ((Comparable<Object>) value1).compareTo(value2) : 
                            ((Comparable<Object>) value2).compareTo(value1);
                        return result;
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
