package com.example.dms2.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页响应DTO
 *
 * @param <T> 数据类型
 * @author DMS2 Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页响应")
public class PageResponse<T> {

  @Schema(description = "当前页码", example = "1")
  private Integer pageNum;

  @Schema(description = "每页大小", example = "10")
  private Integer pageSize;

  @Schema(description = "总记录数", example = "100")
  private Long total;

  @Schema(description = "总页数", example = "10")
  private Integer totalPages;

  @Schema(description = "数据列表")
  private List<T> records;

  /** 构建分页响应 */
  public static <T> PageResponse<T> of(PageRequest pageRequest, List<T> records, long total) {
    int totalPages = (int) Math.ceil((double) total / pageRequest.getPageSize());

    return PageResponse.<T>builder()
        .pageNum(pageRequest.getPageNum())
        .pageSize(pageRequest.getPageSize())
        .total(total)
        .totalPages(totalPages)
        .records(records)
        .build();
  }

  /** 空分页响应 */
  public static <T> PageResponse<T> empty(PageRequest pageRequest) {
    return PageResponse.<T>builder()
        .pageNum(pageRequest.getPageNum())
        .pageSize(pageRequest.getPageSize())
        .total(0L)
        .totalPages(0)
        .records(List.of())
        .build();
  }
}
