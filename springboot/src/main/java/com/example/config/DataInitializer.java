package com.example.config;

import com.example.entity.QuestionCategory;
import com.example.mapper.QuestionCategoryMapper;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Resource
    private QuestionCategoryMapper questionCategoryMapper;

    @Override
    public void run(String... args) {
        initDefaultCategories();
    }

    private void initDefaultCategories() {
        Object[][] subjects = {
                {"Minecraft Java 生电服务器进服审核", "minecraft", "面向生电服务器成员准入的四项审核题库", new String[]{"建筑审核", "后期审核", "红石审核", "普通(见习)审核"}}
        };

        for (Object[] subject : subjects) {
            String subjectName = (String) subject[0];
            String icon = (String) subject[1];
            String desc = (String) subject[2];
            String[] children = (String[]) subject[3];

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
                if (icon != null && !icon.equals(parent.getIcon())) {
                    parent.setIcon(icon);
                    needUpdate = true;
                }
                if (desc != null && !desc.equals(parent.getDescription())) {
                    parent.setDescription(desc);
                    needUpdate = true;
                }
                if (needUpdate) {
                    questionCategoryMapper.updateById(parent);
                }
            }

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
