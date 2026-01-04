package com.sunlight.invest.blog.service;

import com.sunlight.invest.blog.entity.Blog;
import com.sunlight.invest.blog.mapper.BlogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 博客服务类
 *
 * @author System
 * @since 2025-01-04
 */
@Service
public class BlogService {

    private static final Logger log = LoggerFactory.getLogger(BlogService.class);

    @Autowired
    private BlogMapper blogMapper;

    /**
     * 初始化数据库表
     */
    @PostConstruct
    public void init() {
        try {
            blogMapper.createTable();
            updateTableStructure();
            log.info("博客表初始化完成");
        } catch (Exception e) {
            log.error("博客表初始化失败", e);
        }
    }

    /**
     * 更新表结构，确保包含所有必需的列
     */
    private void updateTableStructure() {
        try {
            // 检查并添加category列
            if (blogMapper.checkColumnExists("category") == 0) {
                blogMapper.addCategoryColumn();
                log.info("博客表添加category列成功");
            }
            
            // 检查并添加tags列
            if (blogMapper.checkColumnExists("tags") == 0) {
                blogMapper.addTagsColumn();
                log.info("博客表添加tags列成功");
            }
            
            // 检查并添加summary列
            if (blogMapper.checkColumnExists("summary") == 0) {
                blogMapper.addSummaryColumn();
                log.info("博客表添加summary列成功");
            }
            
            // 检查并添加create_time列
            if (blogMapper.checkColumnExists("create_time") == 0) {
                blogMapper.addCreateTimeColumn();
                log.info("博客表添加create_time列成功");
            }
            
            // 检查并添加update_time列
            if (blogMapper.checkColumnExists("update_time") == 0) {
                blogMapper.addUpdateTimeColumn();
                log.info("博客表添加update_time列成功");
            }
            
        } catch (Exception e) {
            log.error("更新博客表结构失败", e);
        }
    }


    /**
     * 创建博客
     *
     * @param blog 博客对象
     * @return 创建成功返回博客ID，失败返回null
     */
    public Long createBlog(Blog blog) {
        try {
            blog.setCreateTime(LocalDateTime.now());
            blog.setUpdateTime(LocalDateTime.now());
            int result = blogMapper.insert(blog);
            if (result > 0) {
                log.info("创建博客成功: {}", blog.getTitle());
                return blog.getId();
            } else {
                log.warn("创建博客失败: {}", blog.getTitle());
                return null;
            }
        } catch (Exception e) {
            log.error("创建博客异常", e);
            return null;
        }
    }

    /**
     * 根据ID获取博客
     *
     * @param id 博客ID
     * @return 博客对象
     */
    public Blog getBlogById(Long id) {
        try {
            Blog blog = blogMapper.selectById(id);
            log.debug("查询博客: ID={}", id);
            return blog;
        } catch (Exception e) {
            log.error("查询博客失败: ID={}", id, e);
            throw new RuntimeException("查询博客失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取所有已发布的博客
     *
     * @return 博客列表
     */
    public List<Blog> getAllPublishedBlogs() {
        try {
            List<Blog> blogs = blogMapper.selectAllPublished();
            log.debug("查询已发布博客，数量: {}", blogs.size());
            return blogs;
        } catch (Exception e) {
            log.error("查询已发布博客失败", e);
            throw new RuntimeException("查询已发布博客失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取所有博客（包括未发布的）
     *
     * @return 博客列表
     */
    public List<Blog> getAllBlogs() {
        try {
            List<Blog> blogs = blogMapper.selectAll();
            log.debug("查询所有博客，数量: {}", blogs.size());
            return blogs;
        } catch (Exception e) {
            log.error("查询所有博客失败", e);
            throw new RuntimeException("查询所有博客失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新博客
     *
     * @param blog 博客对象
     * @return 更新成功返回true，失败返回false
     */
    public boolean updateBlog(Blog blog) {
        try {
            blog.setUpdateTime(LocalDateTime.now());
            int result = blogMapper.update(blog);
            if (result > 0) {
                log.info("更新博客成功: ID={}", blog.getId());
                return true;
            } else {
                log.warn("更新博客失败: ID={}", blog.getId());
                return false;
            }
        } catch (Exception e) {
            log.error("更新博客异常: ID={}", blog.getId(), e);
            return false;
        }
    }

    /**
     * 删除博客
     *
     * @param id 博客ID
     * @return 删除成功返回true，失败返回false
     */
    public boolean deleteBlog(Long id) {
        try {
            int result = blogMapper.deleteById(id);
            if (result > 0) {
                log.info("删除博客成功: ID={}", id);
                return true;
            } else {
                log.warn("删除博客失败: ID={}", id);
                return false;
            }
        } catch (Exception e) {
            log.error("删除博客异常: ID={}", id, e);
            return false;
        }
    }

    /**
     * 发布博客
     *
     * @param id 博客ID
     * @return 发布成功返回true，失败返回false
     */
    public boolean publishBlog(Long id) {
        try {
            Blog blog = getBlogById(id);
            if (blog != null) {
                blog.setPublished(true);
                blog.setUpdateTime(LocalDateTime.now());
                int result = blogMapper.update(blog);
                if (result > 0) {
                    log.info("发布博客成功: ID={}", id);
                    return true;
                }
            }
            log.warn("发布博客失败: ID={}", id);
            return false;
        } catch (Exception e) {
            log.error("发布博客异常: ID={}", id, e);
            return false;
        }
    }

    /**
     * 取消发布博客
     *
     * @param id 博客ID
     * @return 取消发布成功返回true，失败返回false
     */
    public boolean unpublishBlog(Long id) {
        try {
            Blog blog = getBlogById(id);
            if (blog != null) {
                blog.setPublished(false);
                blog.setUpdateTime(LocalDateTime.now());
                int result = blogMapper.update(blog);
                if (result > 0) {
                    log.info("取消发布博客成功: ID={}", id);
                    return true;
                }
            }
            log.warn("取消发布博客失败: ID={}", id);
            return false;
        } catch (Exception e) {
            log.error("取消发布博客异常: ID={}", id, e);
            return false;
        }
    }
}