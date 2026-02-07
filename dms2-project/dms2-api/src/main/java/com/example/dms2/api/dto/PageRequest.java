package com.example.dms2.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分页请求DTO
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@Data
@Schema(description = "分页请求")
public class PageRequest {

  @Schema(description = "页码（从1开始）", example = "1", minimum = "1")
  private Integer pageNum = 1;

  @Schema(description = "每页大小", example = "10", minimum = "1", maximum = "100")
  private Integer pageSize = 10;

  @Schema(description = "排序字段", example = "createTime")
  private String sortField;

  @Schema(
      description = "排序方向",
      example = "DESC",
      allowableValues = {"ASC", "DESC"})
  private String sortDirection = "DESC";

  /** 计算偏移量（用于SQL LIMIT） */
  public int getOffset() {
    return (pageNum - 1) * pageSize;
  }

  /** 校验分页参数 */
  public void validate() {
    if (pageNum == null || pageNum < 1) {
      pageNum = 1;
    }
    if (pageSize == null || pageSize < 1) {
      pageSize = 10;
    }
    if (pageSize > 100) {
      pageSize = 100; // 最大每页100条
    }
  }
}
