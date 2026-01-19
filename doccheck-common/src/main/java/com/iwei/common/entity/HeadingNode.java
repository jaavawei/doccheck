package com.iwei.common.entity;

import java.util.*;

public class HeadingNode {
    String title;
    String content = "";
    List<Object> children = new ArrayList<>();

    public HeadingNode(String title) {
        this.title = title;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("title", title);
        if (!content.isEmpty()) {
            result.put("content", content);
        }
        if (!children.isEmpty()) {
            result.put("children", children);
        }
        return result;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    private String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        // String prefix = " ".repeat(indent);

        // 添加标题
        sb.append(title).append("\n");

        // 添加内容（如果存在）
        if (!content.isEmpty()) {
            sb.append(content).append("\n");
        }

        // 递归添加子节点
        for (Object child : children) {
            if (child instanceof HeadingNode) {
                sb.append(((HeadingNode) child).toString(indent + 1));
            }
        }

        return sb.toString();
    }
    
    // Getters and setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public List<Object> getChildren() {
        return children;
    }
    
    public void setChildren(List<Object> children) {
        this.children = children;
    }
}