package com.sunlight.invest.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Windows文件和文件夹名称替换测试类
 * 用于将指定路径下的文件和文件夹名称中包含特定字符串的部分替换为新字符串
 *
 * @author System
 * @since 2024-12-24
 */
public class FileNameReplaceTest {
    
    public static void main(String[] args) {
        // 示例：将指定盘符下的所有包含特定字符串的文件和文件夹名称替换为对应的新字符串
        String rootPath = "E:\\"; // 根目录路径，可根据需要修改为其他盘符如 "E:\\", "C:\\" 等
        // 定义替换规则数组 - 旧字符串和新字符串一一对应
        String[] oldStrings = {"熊猫农村", "火山农信", "义乌银行"};   // 要被替换的字符串数组
        String[] newStrings = {"四川农信", "陕西农信", "稠州银行"};   // 替换后的字符串数组
        
        System.out.println("开始执行文件和文件夹名称替换任务...");
        System.out.println("根路径: " + rootPath);
        
        // 输出替换规则
        System.out.println("替换规则:");
        for (int i = 0; i < oldStrings.length && i < newStrings.length; i++) {
            System.out.println("  " + oldStrings[i] + " -> " + newStrings[i]);
        }
        
        try {
            int count = replaceInDirectory(rootPath, oldStrings, newStrings);
            System.out.println("替换完成，共处理了 " + count + " 个文件/文件夹");
        } catch (Exception e) {
            System.err.println("执行过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 递归替换目录下所有包含指定字符串的文件和文件夹名称
     *
     * @param directoryPath 目录路径
     * @param oldStrings    要被替换的字符串数组
     * @param newStrings    替换后的字符串数组
     * @return 替换的文件和文件夹总数
     */
    public static int replaceInDirectory(String directoryPath, String[] oldStrings, String[] newStrings) {
        int count = 0;
        
        try {
            File directory = new File(directoryPath);
            
            if (!directory.exists() || !directory.isDirectory()) {
                System.out.println("目录不存在或不是有效目录: " + directoryPath);
                return count;
            }
            
            // 首先处理子目录（递归处理，从最深层开始）
            File[] files = directory.listFiles();
            if (files != null) {
                List<File> subDirectories = new ArrayList<>();
                
                // 分别处理文件和子目录
                for (File file : files) {
                    if (file.isDirectory()) {
                        // 先递归处理子目录中的内容
                        count += replaceInDirectory(file.getAbsolutePath(), oldStrings, newStrings);
                        subDirectories.add(file);
                    } else if (file.isFile()) {
                        // 处理文件 - 应用所有替换规则
                        String originalName = file.getName();
                        String newName = originalName;
                        
                        // 应用所有替换规则
                        for (int i = 0; i < oldStrings.length && i < newStrings.length; i++) {
                            if (newName.contains(oldStrings[i])) {
                                newName = newName.replace(oldStrings[i], newStrings[i]);
                            }
                        }
                        
                        // 如果文件名发生了变化，则重命名
                        if (!originalName.equals(newName)) {
                            File newFile = new File(file.getParent(), newName);
                            
                            if (file.renameTo(newFile)) {
                                System.out.println("文件重命名成功: " + originalName + " -> " + newName);
                                count++;
                            } else {
                                System.err.println("文件重命名失败: " + originalName);
                            }
                        }
                    }
                }
                
                // 处理子目录名称 - 应用所有替换规则
                for (File subDir : subDirectories) {
                    String originalName = subDir.getName();
                    String newName = originalName;
                    
                    // 应用所有替换规则
                    for (int i = 0; i < oldStrings.length && i < newStrings.length; i++) {
                        if (newName.contains(oldStrings[i])) {
                            newName = newName.replace(oldStrings[i], newStrings[i]);
                        }
                    }
                    
                    // 如果目录名发生了变化，则重命名
                    if (!originalName.equals(newName)) {
                        File newDir = new File(subDir.getParent(), newName);
                        
                        if (subDir.renameTo(newDir)) {
                            System.out.println("目录重命名成功: " + originalName + " -> " + newName);
                            count++;
                        } else {
                            System.err.println("目录重命名失败: " + originalName);
                        }
                    }
                }
                
                // 最后处理当前目录名称
                String originalDirName = directory.getName();
                String newDirName = originalDirName;
                
                // 应用所有替换规则
                for (int i = 0; i < oldStrings.length && i < newStrings.length; i++) {
                    if (newDirName.contains(oldStrings[i])) {
                        newDirName = newDirName.replace(oldStrings[i], newStrings[i]);
                    }
                }
                
                // 如果目录名发生了变化，则重命名
                if (!originalDirName.equals(newDirName)) {
                    String parentPath = directory.getParent();
                    if (parentPath != null) {
                        File newDir = new File(parentPath, newDirName);
                        
                        if (directory.renameTo(newDir)) {
                            System.out.println("目录重命名成功: " + originalDirName + " -> " + newDirName);
                            count++;
                            // 如果当前目录重命名成功，需要更新处理路径
                            directoryPath = newDir.getAbsolutePath();
                        } else {
                            System.err.println("目录重命名失败: " + originalDirName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("处理目录时发生错误: " + directoryPath + ", 错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        return count;
    }
    
    /**
     * 安全的文件重命名方法，处理Windows路径问题
     *
     * @param oldFile 旧文件对象
     * @param newFile 新文件对象
     * @return 重命名是否成功
     */
    public static boolean safeRenameFile(File oldFile, File newFile) {
        // 确保目标文件名不与现有文件冲突
        if (newFile.exists()) {
            System.err.println("目标文件已存在，跳过重命名: " + newFile.getAbsolutePath());
            return false;
        }
        
        // 确保父目录存在
        File parentDir = newFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                System.err.println("无法创建父目录: " + parentDir.getAbsolutePath());
                return false;
            }
        }
        
        // 执行重命名
        return oldFile.renameTo(newFile);
    }
    
    /**
     * 测试方法，用于测试特定路径下的文件和文件夹名称替换
     */
    public static void testReplace() {
        System.out.println("\n=== 执行测试 ===");
        
        // 创建测试目录结构
        String testPath = "D:\\test_rename";
        File testDir = new File(testPath);
        
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
        
        // 创建一些测试文件和目录
        try {
            // 创建包含各种要替换字符串的测试文件
            File testFile1 = new File(testPath, "熊猫农村_test_file.txt");
            File testFile2 = new File(testPath, "农商银行_document.doc");
            File testFile3 = new File(testPath, "信用社_report.pdf");
            testFile1.createNewFile();
            testFile2.createNewFile();
            testFile3.createNewFile();
            
            // 创建包含各种要替换字符串的测试子目录
            File testSubDir1 = new File(testPath, "熊猫农村_subdir");
            File testSubDir2 = new File(testPath, "农商银行_folder");
            File testSubDir3 = new File(testPath, "信用社_test");
            testSubDir1.mkdirs();
            testSubDir2.mkdirs();
            testSubDir3.mkdirs();
            
            // 在子目录中创建一些文件
            File subFile1 = new File(testSubDir1, "熊猫农村_inside.txt");
            File subFile2 = new File(testSubDir2, "file_农商银行_inside.doc");
            File subFile3 = new File(testSubDir3, "信用社_test_file.pdf");
            subFile1.createNewFile();
            subFile2.createNewFile();
            subFile3.createNewFile();
            
            System.out.println("创建测试文件和目录完成");
            
            // 定义测试替换规则
            String[] oldStrings = {"熊猫农村", "农商银行", "信用社"};
            String[] newStrings = {"四川农信", "农村银行", "信用联社"};
            
            // 执行替换
            int count = replaceInDirectory(testPath, oldStrings, newStrings);
            System.out.println("测试替换完成，共处理了 " + count + " 个文件/文件夹");
            
        } catch (Exception e) {
            System.err.println("测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}