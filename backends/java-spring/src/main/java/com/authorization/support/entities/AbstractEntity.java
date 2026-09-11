/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.support.entities;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 基底Entityクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
public abstract class AbstractEntity {

    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;
    private LocalDateTime deletedAt;
    private Long deletedBy;
    private Integer version;

    /**
     * 作成者・作成日時・バージョンを設定します。
     *
     * @param executorId 処理実行者ID
     */
    public void assignCreated(long executorId) {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.createdBy = executorId;
        this.updatedAt = now;
        this.updatedBy = executorId;
        this.version = 1;
    }

    /**
     * 更新者・更新日時を設定します。
     *
     * @param executorId 処理実行者ID
     */
    public void assignUpdated(long executorId) {
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = executorId;
    }

    /**
     * 削除者・削除日時を設定します。
     *
     * @param executorId 処理実行者ID
     */
    public void assignDeleted(long executorId) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = executorId;
    }
}
