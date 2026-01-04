package com.sunlight.invest.blog.controller;

import com.sunlight.invest.blog.entity.Blog;
import com.sunlight.invest.blog.service.BlogService;
import com.sunlight.invest.common.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 博客控制器
 *
 * @author System
 * @since 2025-01-04
 */
@RestController
@RequestMapping("/api/blog")
public class BlogController {

    @Autowired
    private BlogService blogService;

    /**
     * 创建博客
     */
    @PostMapping("/create")
    public BaseResponse createBlog(@RequestBody Blog blog) {
        BaseResponse response = new BaseResponse();
        try {
            Long blogId = blogService.createBlog(blog);
            if (blogId != null) {
                response.setSuccess(true);
                response.setMessage("博客创建成功");
                Map<String, Object> data = new HashMap<>();
                data.put("id", blogId);
                response.setData(data);
            } else {
                response.setSuccess(false);
                response.setMessage("博客创建失败");
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("博客创建失败: " + e.getMessage());
        }
        return response;
    }

    /**
     * 获取博客详情
     */
    @GetMapping("/{id}")
    public BaseResponse getBlog(@PathVariable Long id) {
        BaseResponse response = new BaseResponse();
        try {
            Blog blog = blogService.getBlogById(id);
            if (blog != null) {
                response.setSuccess(true);
                response.setData(blog);
            } else {
                response.setSuccess(false);
                response.setMessage("博客不存在");
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("获取博客失败: " + e.getMessage());
        }
        return response;
    }

    /**
     * 获取所有已发布的博客
     */
    @GetMapping("/published")
    public BaseResponse getAllPublishedBlogs() {
        BaseResponse response = new BaseResponse();
        try {
            List<Blog> blogs = blogService.getAllPublishedBlogs();
            response.setSuccess(true);
            response.setData(blogs);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("获取博客列表失败: " + e.getMessage());
        }
        return response;
    }

    /**
     * 获取所有博客（包括未发布的）
     */
    @GetMapping("/all")
    public BaseResponse getAllBlogs() {
        BaseResponse response = new BaseResponse();
        try {
            List<Blog> blogs = blogService.getAllBlogs();
            response.setSuccess(true);
            response.setData(blogs);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("获取博客列表失败: " + e.getMessage());
        }
        return response;
    }

    /**
     * 更新博客
     */
    @PutMapping("/update")
    public BaseResponse updateBlog(@RequestBody Blog blog) {
        BaseResponse response = new BaseResponse();
        try {
            boolean result = blogService.updateBlog(blog);
            if (result) {
                response.setSuccess(true);
                response.setMessage("博客更新成功");
            } else {
                response.setSuccess(false);
                response.setMessage("博客更新失败");
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("博客更新失败: " + e.getMessage());
        }
        return response;
    }

    /**
     * 删除博客
     */
    @DeleteMapping("/delete/{id}")
    public BaseResponse deleteBlog(@PathVariable Long id) {
        BaseResponse response = new BaseResponse();
        try {
            boolean result = blogService.deleteBlog(id);
            if (result) {
                response.setSuccess(true);
                response.setMessage("博客删除成功");
            } else {
                response.setSuccess(false);
                response.setMessage("博客删除失败");
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("博客删除失败: " + e.getMessage());
        }
        return response;
    }

    /**
     * 发布博客
     */
    @PutMapping("/publish/{id}")
    public BaseResponse publishBlog(@PathVariable Long id) {
        BaseResponse response = new BaseResponse();
        try {
            boolean result = blogService.publishBlog(id);
            if (result) {
                response.setSuccess(true);
                response.setMessage("博客发布成功");
            } else {
                response.setSuccess(false);
                response.setMessage("博客发布失败");
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("博客发布失败: " + e.getMessage());
        }
        return response;
    }

    /**
     * 取消发布博客
     */
    @PutMapping("/unpublish/{id}")
    public BaseResponse unpublishBlog(@PathVariable Long id) {
        BaseResponse response = new BaseResponse();
        try {
            boolean result = blogService.unpublishBlog(id);
            if (result) {
                response.setSuccess(true);
                response.setMessage("博客取消发布成功");
            } else {
                response.setSuccess(false);
                response.setMessage("博客取消发布失败");
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("博客取消发布失败: " + e.getMessage());
        }
        return response;
    }
}