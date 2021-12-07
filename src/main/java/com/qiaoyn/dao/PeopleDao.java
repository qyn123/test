package com.qiaoyn.dao;

import com.qiaoyn.entity.PeopleDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author yn.qiao
 * @version 1.0
 * @ClassName PeopleDao
 * @create 2021-11-18 16:46
 **/
@Mapper
public interface PeopleDao {

    /**
     * 批量入库
     * @author yn.qiao
     * @param s 表
     * @param list 插入list
     * @return
     **/
    void insertPeoPleList(@Param("dataList") List<PeopleDto> list, @Param("s")Integer s);
}
