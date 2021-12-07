## 基于guava实现hash一致性分表

参考文档：https://www.cnblogs.com/luxiaoxun/p/12573742.html

无虚拟节点一致性hash分表流程图


application.yml

```yaml
spring:
  datasource:
    name: test
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      driver-class-name: com.mysql.jdbc.Driver
      url: jdbc:mysql://am-2ze76l8trgnz3decb90650o.ads.aliyuncs.com:3306/test?useUnicode=true&characterEncoding=UTF-8
      username: hypers_dev_team
      password: Pass4Hypers
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      filter:
        stat:
          merge-sql: true
          slow-sql-millis: 5000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 30000

mybatis:
  mapper-locations: classpath:mapping/*.xml
  type-aliases-package: com.qiaoyn.entity
```

一致性hash分表工具类

```java
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
    public Integer getHashSplitSheet(int buckets,String s){
        HashFunction hashFunction = Hashing.sha512();
        // 要平均分为几份,然后根据id得到的bucket就是分到的表中
        int bucket = Hashing.consistentHash(hashFunction.hashString(s, Charsets.UTF_8), buckets);
        // 然后就可以对 buckets 取余, 平均分配
        return bucket;
    }

}
```

pom.xml

```xml
 <dependencies>
        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>29.0-jre</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
            <version>2.5.3</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>2.5.6</version>
        </dependency>
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>1.3.2</version>
        </dependency>

        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.47</version>
      </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <version>2.5.6</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.20</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-starter</artifactId>
            <version>1.1.10</version>
        </dependency>
    </dependencies>
```

无虚拟节点测试类：

```java
/**
 * @author yn.qiao
 * @version 1.0
 * @ClassName Hash
 * @create 2021-11-18 14:44
 **/
public class ConsistentHashingWithoutVirtualNode {

        //待添加入Hash环的服务器列表
        private static String[] servers = { "192.168.0.0:111", "192.168.0.1:111",
                "192.168.0.2:111","192.168.0.3:111","192.168.0.4:111" };

        //key表示服务器的hash值，value表示服务器
        private static SortedMap<Integer, String> sortedMap = new TreeMap<Integer, String>();

        //程序初始化，将所有的服务器放入sortedMap中
        static {
            for (int i=0; i<servers.length; i++) {
                int hash = getHash(servers[i]);
                System.out.println("[" + servers[i] + "]加入集合中, 其Hash值为" + hash);
                sortedMap.put(hash, servers[i]);
            }
            System.out.println();
        }

        //得到应当路由到的结点
        private static String getServer(String key) {
            //得到该key的hash值
            int hash = getHash(key);
            //得到大于该Hash值的所有Map
            SortedMap<Integer, String> subMap = sortedMap.tailMap(hash);
            if(subMap.isEmpty()){
                //如果没有比该key的hash值大的，则从第一个node开始
                Integer i = sortedMap.firstKey();
                //返回对应的服务器
                return sortedMap.get(i);
            }else{
                //第一个Key就是顺时针过去离node最近的那个结点
                Integer i = subMap.firstKey();
                //返回对应的服务器
                return subMap.get(i);
            }
        }

        //使用FNV1_32_HASH算法计算服务器的Hash值,这里不使用重写hashCode的方法，最终效果没区别
        private static int getHash(String str) {
            final int p = 16777619;
            int hash = (int) 2166136261L;
            for (int i = 0; i < str.length(); i++){
                hash = (hash ^ str.charAt(i)) * p;
            }
            hash += hash << 13;
            hash ^= hash >> 7;
            hash += hash << 3;
            hash ^= hash >> 17;
            hash += hash << 5;

            // 如果算出来的值为负数则取其绝对值
            if (hash < 0){
                hash = Math.abs(hash);
            }
            return hash;
        }

        public static void main(String[] args) {
            String[] keys = {"太阳", "月亮","打死他" ,"星星","星星","我爱她轰轰烈烈","小白","小青","阿西吧"};
            for(int i=0; i<keys.length; i++){
                System.out.println("[" + keys[i] + "]的hash值为" + getHash(keys[i])
                        + ", 被路由到结点[" + getServer(keys[i]) + "]");
            }
        }
}
```

有虚拟节点测试类：

```java
package com.qiaoyn.util;

/**
 * @author yn.qiao
 * @version 1.0
 * @ClassName ConsistentHashingWithVirtualNode
 * @create 2021-11-18 15:21
 **/


import org.junit.platform.commons.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 带虚拟节点的一致性Hash算法
 */
public class ConsistentHashingWithVirtualNode {

    //待添加入Hash环的服务器列表
    private static String[] servers = {"192.168.0.0:111", "192.168.0.1:111", "192.168.0.2:111",
            "192.168.0.3:111", "192.168.0.4:111"};

    //真实结点列表,考虑到服务器上线、下线的场景，即添加、删除的场景会比较频繁，这里使用LinkedList会更好
    private static List<String> realNodes = new LinkedList<String>();

    //虚拟节点，key表示虚拟节点的hash值，value表示虚拟节点的名称
    private static SortedMap<Integer, String> virtualNodes = new TreeMap<Integer, String>();

    //虚拟节点的数目，这里写死，为了演示需要，一个真实结点对应5个虚拟节点
    private static final int VIRTUAL_NODES = 5;

    static{
        //先把原始的服务器添加到真实结点列表中
        for(int i=0; i<servers.length; i++){
            realNodes.add(servers[i]);
        }
        //再添加虚拟节点，遍历LinkedList使用foreach循环效率会比较高
        for (String str : realNodes){
            for(int i=0; i<VIRTUAL_NODES; i++){
                String virtualNodeName = str + "&&VN" + i;
                int hash = getHash(virtualNodeName);
                System.out.println("虚拟节点[" + virtualNodeName + "]被添加, hash值为" + hash);
                virtualNodes.put(hash, virtualNodeName);
            }
        }
        System.out.println();
    }

    //使用FNV1_32_HASH算法计算服务器的Hash值,这里不使用重写hashCode的方法，最终效果没区别
    private static int getHash(String str){
        final int p = 16777619;
        int hash = (int)2166136261L;
        for (int i = 0; i < str.length(); i++){
            hash = (hash ^ str.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        // 如果算出来的值为负数则取其绝对值
        if (hash < 0){
            hash = Math.abs(hash);
        }
        return hash;
    }

    //得到应当路由到的结点
    private static String getServer(String key){
        //得到该key的hash值
        int hash = getHash(key);
        // 得到大于该Hash值的所有Map
        SortedMap<Integer, String> subMap = virtualNodes.tailMap(hash);
        String virtualNode;
        if(subMap.isEmpty()){
            //如果没有比该key的hash值大的，则从第一个node开始
            Integer i = virtualNodes.firstKey();
            //返回对应的服务器
            virtualNode = virtualNodes.get(i);
        }else{
            //第一个Key就是顺时针过去离node最近的那个结点
            Integer i = subMap.firstKey();
            //返回对应的服务器
            virtualNode = subMap.get(i);
        }
        //virtualNode虚拟节点名称要截取一下
        if(StringUtils.isNotBlank(virtualNode)){
            return virtualNode.substring(0, virtualNode.indexOf("&&"));
        }
        return null;
    }

    public static void main(String[] args){
        String[] keys = {"太阳", "月亮", "星星"};
        for(int i=0; i<keys.length; i++){
            System.out.println("[" + keys[i] + "]的hash值为" +
                    getHash(keys[i]) + ", 被路由到结点[" + getServer(keys[i]) + "]");
        }
    }
}

```

