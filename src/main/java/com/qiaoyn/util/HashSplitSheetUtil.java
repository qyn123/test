package com.qiaoyn.util;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;


/**
 * @author yn.qiao
 * @version 1.0
 * @ClassName HashSplitSheetUtil
 * @create 2021-11-18 16:26
 **/
@Slf4j
@Configuration
public class HashSplitSheetUtil {

    /**
     * 基于guava实现hash一致性分表,返回值为最终对象被分配到的那张表中
     */
    public  Integer getHashSplitSheet(int buckets,String s){
        HashFunction hashFunction = Hashing.sha512();
        // 要平均分为几份
        int bucket = Hashing.consistentHash(hashFunction.hashString(s, Charsets.UTF_8), buckets);
        // 然后就可以对 buckets 取余, 平均分配
        return bucket;
    }
}
