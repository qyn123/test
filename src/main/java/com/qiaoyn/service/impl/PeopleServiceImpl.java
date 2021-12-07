package com.qiaoyn.service.impl;

import com.qiaoyn.dao.PeopleDao;
import com.qiaoyn.entity.PeopleDto;
import com.qiaoyn.service.PeopleService;
import com.qiaoyn.util.HashSplitSheetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yn.qiao
 * @version 1.0
 * @ClassName PeopleServiceImpl
 * @create 2021-11-18 17:02
 **/
@Service
@Slf4j
public class PeopleServiceImpl implements PeopleService {

    @Resource
    PeopleDao peopleDao;
    @Resource
    HashSplitSheetUtil hashSplitSheetUtil;

    @Value("${table.num}")
    private  Integer tableNum = 3;

    @Override
    public void insertPeoPleList(List<PeopleDto> list) {
        List<PeopleDto> returnList = new ArrayList<>();
        for (PeopleDto peopleDto : list) {
            returnList.add(new PeopleDto(peopleDto.getId(),peopleDto.getName(),hashSplitSheetUtil.getHashSplitSheet(tableNum, peopleDto.getId())));
        }
        Map<Integer, List<PeopleDto>> listMap = returnList.stream().collect(Collectors.groupingBy(PeopleDto::getM));
        listMap.forEach((m, peopleDos) -> peopleDao.insertPeoPleList(peopleDos,m));
    }
}
