package com.example.learningplan.entity;

import javax.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sys_op_log")
@SQLDelete(sql = "UPDATE sys_op_log SET is_deleted = 1 WHERE id = ?")
@Where(clause = "is_deleted = 0")
@Getter
@Setter
public class SysOpLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 100)
    private String username;

    @Column(name = "role_codes", length = 200)
    private String roleCodes;

    @Column(length = 10)
    private String method;

    @Column(length = 255)
    private String path;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(length = 1000)
    private String errorMessage;

    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "query_params", length = 1000)
    private String queryParams;

    @Column(length = 64)
    private String ip;

    @Column(name = "user_agent", length = 255)
    private String userAgent;
}
