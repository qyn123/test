package com.qiaoyn.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yn.qiao
 * @version 1.0
 * @ClassName Student
 * @create 2021-11-16 18:20
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeopleDto {
    private String id;
    private String name;

    public PeopleDto(String id, String name) {
        this.id = id;
        this.name = name;
    }

    private Integer m;

}
