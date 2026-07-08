package com.netease.bdms.ndi.service.web.vo;

import com.netease.bdms.ndi.service.web.dto.datasource.DataSourceDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ListDataSourcesDto;
import com.netease.bdms.ndi.service.web.util.ParamUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName DataSourceListVO
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class ListDataSourcesVO {
  private List<DataSourceVO> dataSources;
  private List<String> dataSourceTypes;
  private Integer pageNum;
  private Integer pageSize;
  private Integer total;

  public static ListDataSourcesVO listDataSourcesDTO2VO(ListDataSourcesDto dataSourcesDTO) {
    ParamUtil.validate(dataSourcesDTO, dataSourcesDTO.getDataSourceDtoList());
    ListDataSourcesVO listDataSourcesVO = new ListDataSourcesVO();
    listDataSourcesVO.setDataSources(dataSourcesDTO.getDataSourceDtoList().stream()
      .map(item -> DataSourceVO.dataSourceDTO2VO(item)).collect(Collectors.toList()));
    listDataSourcesVO.setDataSourceTypes(getDataSourceTypes(dataSourcesDTO.getDataSourceDtoList()));
    listDataSourcesVO.setTotal(dataSourcesDTO.getTotal());
    return listDataSourcesVO;
  }

  private static List<String> getDataSourceTypes(List<DataSourceDto> dataSourceDtoList) {
    ParamUtil.validate(dataSourceDtoList);
    List<String> dataSourceTypes = new ArrayList<>();
    if (dataSourceDtoList.size() > 0) {
      for (DataSourceDto dataSourceDTO : dataSourceDtoList) {
        dataSourceTypes.add(dataSourceDTO.getType());
      }
    }
    return dataSourceTypes;
  }
}
