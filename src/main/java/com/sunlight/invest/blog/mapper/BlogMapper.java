package com.sunlight.invest.blog.mapper;

import com.sunlight.invest.blog.entity.Blog;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 博客Mapper接口
 *
 * @author System
 * @since 2025-01-04
 */
@Mapper
public interface BlogMapper {

    /**
     * 创建博客表
     */
    @Update("CREATE TABLE IF NOT EXISTS `blog` (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
            "title VARCHAR(255) NOT NULL COMMENT '标题', " +
            "content TEXT COMMENT '内容', " +
            "author VARCHAR(100) COMMENT '作者', " +
            "summary VARCHAR(500) COMMENT '摘要', " +
            "category VARCHAR(100) COMMENT '分类', " +
            "tags VARCHAR(255) COMMENT '标签', " +
            "published TINYINT(1) DEFAULT 0 COMMENT '是否发布', " +
            "create_time DATETIME COMMENT '创建时间', " +
            "update_time DATETIME COMMENT '更新时间'" +
            ") COMMENT '博客表'")
    void createTable();

    /**
     * 检查列是否存在
     */
    @Select("SELECT COUNT(*) FROM information_schema.COLUMNS " +
            "WHERE TABLE_SCHEMA = (SELECT DATABASE()) " +
            "AND TABLE_NAME = 'blog' " +
            "AND COLUMN_NAME = #{columnName}")
    int checkColumnExists(@Param("columnName") String columnName);

    /**
     * 添加分类列
     */
    @Update("ALTER TABLE blog ADD COLUMN category VARCHAR(100) COMMENT '分类'")
    void addCategoryColumn();

    /**
     * 添加标签列
     */
    @Update("ALTER TABLE blog ADD COLUMN tags VARCHAR(255) COMMENT '标签'")
    void addTagsColumn();

    /**
     * 添加摘要列
     */
    @Update("ALTER TABLE blog ADD COLUMN summary VARCHAR(500) COMMENT '摘要'")
    void addSummaryColumn();

    /**
     * 添加发布时间列
     */
    @Update("ALTER TABLE blog ADD COLUMN create_time DATETIME COMMENT '创建时间'")
    void addCreateTimeColumn();

    /**
     * 添加更新时间列
     */
    @Update("ALTER TABLE blog ADD COLUMN update_time DATETIME COMMENT '更新时间'")
    void addUpdateTimeColumn();

    /**
     * 插入博客记录
     *
     * @param blog 博客对象
     * @return 影响行数
     */
    @Insert("INSERT INTO blog (title, content, author, summary, category, tags, published, create_time, update_time) " +
            "VALUES (#{title}, #{content}, #{author}, #{summary}, #{category}, #{tags}, #{published}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Blog blog);

    /**
     * 根据ID查询博客
     *
     * @param id 主键ID
     * @return 博客对象
     */
    @Select("SELECT * FROM blog WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "author", column = "author"),
            @Result(property = "summary", column = "summary"),
            @Result(property = "category", column = "category"),
            @Result(property = "tags", column = "tags"),
            @Result(property = "published", column = "published"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    Blog selectById(@Param("id") Long id);

    /**
     * 查询所有博客（已发布的）
     *
     * @return 博客列表
     */
    @Select("SELECT * FROM blog WHERE published = 1 ORDER BY create_time DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "author", column = "author"),
            @Result(property = "summary", column = "summary"),
            @Result(property = "category", column = "category"),
            @Result(property = "tags", column = "tags"),
            @Result(property = "published", column = "published"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<Blog> selectAllPublished();

    /**
     * 查询所有博客（包括未发布的）
     *
     * @return 博客列表
     */
    @Select("SELECT * FROM blog ORDER BY create_time DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "author", column = "author"),
            @Result(property = "summary", column = "summary"),
            @Result(property = "category", column = "category"),
            @Result(property = "tags", column = "tags"),
            @Result(property = "published", column = "published"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<Blog> selectAll();

    /**
     * 更新博客
     *
     * @param blog 博客对象
     * @return 影响行数
     */
    @Update("UPDATE blog SET title = #{title}, content = #{content}, author = #{author}, " +
            "summary = #{summary}, category = #{category}, tags = #{tags}, published = #{published}, update_time = #{updateTime} " +
            "WHERE id = #{id}")
    int update(Blog blog);

    /**
     * 根据ID删除博客
     *
     * @param id 主键ID
     * @return 影响行数
     */
    @Delete("DELETE FROM blog WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}