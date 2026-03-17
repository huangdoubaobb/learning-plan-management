package com.example.learningplan.dto;

public class PermissionView {
    private Long id;
    private String code;
    private String name;
    private Long groupId;
    private String groupCode;
    private String groupName;

    public PermissionView(Long id, String code, String name, Long groupId, String groupCode, String groupName) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.groupId = groupId;
        this.groupCode = groupCode;
        this.groupName = groupName;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Long getGroupId() {
        return groupId;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public String getGroupName() {
        return groupName;
    }
}
