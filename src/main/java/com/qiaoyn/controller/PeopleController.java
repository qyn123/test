package com.qiaoyn.controller;

import com.qiaoyn.entity.PeopleDto;
import com.qiaoyn.service.PeopleService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yn.qiao
 * @version 1.0
 * @ClassName PeopleController
 * @create 2021-12-07 14:22
 **/
@RestController
@RequestMapping("/api")
public class PeopleController {


    @Resource
    private PeopleService peopleService;


    @PostMapping("/test")
    public void test(@RequestBody List<PeopleDto> dtoList){
        peopleService.insertPeoPleList(dtoList);
    }
}
