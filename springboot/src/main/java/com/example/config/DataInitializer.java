package com.example.config;

import com.example.entity.QuestionCategory;
import com.example.mapper.QuestionCategoryMapper;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 系统启动时自动初始化默认数据
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Resource
    private QuestionCategoryMapper questionCategoryMapper;

    @Override
    public void run(String... args) {
        initDefaultCategories();
    }

    private void initDefaultCategories() {
        // 定义学科结构：{学科名, 图标, 描述, 子分类[]}
        Object[][] subjects = {
                {"我的世界", "minecraft", "Minecraft 服务器相关题目，包含红石、后勤、见习审核等", new String[]{"红石工程", "后勤管理", "见习审核", "建筑设计", "基础知识"}},
                {"小学算术", "math", "小学数学基础运算与应用题", new String[]{"加减运算", "乘除运算", "应用题"}},
                {"看图写话", "writing", "看图理解与短文写作练习", new String[]{"词语理解", "短文写作"}},
                {"计算机", "computer", "计算机专业知识题目", new String[]{"计算机网络", "操作系统", "数据库", "计算机组成原理"}},
                {"编程", "programming", "编程语言与算法题目", new String[]{"Java", "Python", "C/C++", "数据结构与算法"}}
        };

        for (Object[] subject : subjects) {
            String subjectName = (String) subject[0];
            String icon = (String) subject[1];
            String desc = (String) subject[2];
            String[] children = (String[]) subject[3];

            // 检查学科是否已存在
            QuestionCategory query = new QuestionCategory();
            query.setName(subjectName);
            List<QuestionCategory> existing = questionCategoryMapper.selectAll(query);
            QuestionCategory parent;
            if (existing == null || existing.isEmpty()) {
                parent = new QuestionCategory();
                parent.setName(subjectName);
                parent.setIcon(icon);
                parent.setDescription(desc);
                parent.setStatus("active");
                questionCategoryMapper.insert(parent);
            } else {
                parent = existing.get(0);
                boolean needUpdate = false;
                if (icon != null && !icon.equals(parent.getIcon())) { parent.setIcon(icon); needUpdate = true; }
                if (desc != null && !desc.equals(parent.getDescription())) { parent.setDescription(desc); needUpdate = true; }
                if (needUpdate) {
                    questionCategoryMapper.updateById(parent);
                }
            }

            // 创建子分类
            for (String childName : children) {
                List<QuestionCategory> childExisting = questionCategoryMapper.selectByParentId(parent.getId());
                boolean exists = childExisting != null && childExisting.stream().anyMatch(c -> c.getName().equals(childName));
                if (!exists) {
                    QuestionCategory child = new QuestionCategory();
                    child.setName(childName);
                    child.setParentId(parent.getId());
                    child.setStatus("active");
                    questionCategoryMapper.insert(child);
                }
            }
        }
    }
}
