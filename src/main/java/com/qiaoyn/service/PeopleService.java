package com.qiaoyn.service;

import com.qiaoyn.entity.PeopleDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author yn.qiao
 * @version 1.0
 * @ClassName PeopleService
 * @create 2021-11-18 17:01
 **/
public interface PeopleService {


    /**
     * 批量入库
     * @author yn.qiao
     * @param list 插入list
     * @return void
     **/
    void insertPeoPleList(@Param("dataList") List<PeopleDto> list);
}
