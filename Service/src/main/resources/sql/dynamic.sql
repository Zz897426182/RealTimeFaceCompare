-------------------------------------------------------------------------
|                             建表说明                                  |
| hbase表:upFea searchRes （需要手动建表）                              |
| hive表: person_table mid_table （使用create-dynamic-table.sh脚本建表）|
-------------------------------------------------------------------------
应用平台上传的图片：
-------------------------------------------------------------------------------
|                           upFea                                             |
-------------------------------------------------------------------------------
|             |          CF：p          |              CF：c                  |
-------------------------------------------------------------------------------
|   图片ID    |    图片     |    特征    |   图片    |    特征    |    车牌号 |
-------------------------------------------------------------------------------
|   rowkey    |     p      |     f      |      p    |    f      |      n      |
-------------------------------------------------------------------------------

create 'upFea',
{NAME => 'p',DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0',
VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65535',
IN_MEMORY => 'true', BLOCKCACHE => 'true'},
{NAME => 'c',DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0',
VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65535',
IN_MEMORY => 'true', BLOCKCACHE => 'true'}

------------------------------------------------------------------------------------------------------------

 查询结果：
-------------------------------------------------------------
|                             searchRes                     |
-------------------------------------------------------------
|            |                 CF：i                        |
-------------------------------------------------------------
|   查询ID   |     查询类型     | List<CapturePicture>      |
-------------------------------------------------------------
|    rowkey  |        t         |        m                  |
-------------------------------------------------------------

create 'searchRes',
{NAME => 'i', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0',
VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65535',
IN_MEMORY => 'true', BLOCKCACHE => 'true',TTL=>'604800'}

人脸动态库（最终表，存放小文件合并后数据）
------------------------------------------------------------------------------------------------------------------------
|                                                  person_table                                                                       |
------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------
|                                                                                                         | 分区字段   |
------------------------------------------------------------------------------------------------------------------------
|图片地址|特征值 | 性别   | 头发颜色  | 头发类型  | 帽子 | 胡子 | 领带 |  时间段  | 时间戳    | 搜索类型  |日期 |设备id|
------------------------------------------------------------------------------------------------------------------------
| ftpurl |feature| gender | haircolor | hairstyle | hat  | huzi | tie  | timeslot | exacttime | searchtype|date |ipcid |
------------------------------------------------------------------------------------------------------------------------
| string |string |  int   |    int    |     int   | int  |  int | int  |   int    | Timestamp |  string   |string|string|
------------------------------------------------------------------------------------------------------------------------
CREATE EXTERNAL TABLE IF NOT EXISTS default.person_table(
ftpurl        string,
feature       array<float>,
eyeglasses    int,
gender        int,
haircolor     int,
hairstyle     int,
hat           int,
huzi          int,
tie           int,
timeslot      int,
exacttime     Timestamp,
searchtype    string)
partitioned by (date string,ipcid string)
STORED AS PARQUET
LOCATION '/user/hive/warehouse/person_table';

------------------------------------------------------------------------------------------------------------------------
人脸动态库（临时表，存放小文件合并前数据）
------------------------------------------------------------------------------------------------------------------------
|                                                  mid_table                                                                       |
------------------------------------------------------------------------------------------------------------------------
|图片地址|特征值 | 性别   | 头发颜色  | 头发类型  | 帽子 | 胡子 | 领带 |  时间段  | 时间戳    | 搜索类型  |日期 |设备id|
------------------------------------------------------------------------------------------------------------------------
| ftpurl |feature| gender | haircolor | hairstyle | hat  | huzi | tie  | timeslot | exacttime | searchtype|date |ipcid |
------------------------------------------------------------------------------------------------------------------------
| string |string |  int   |    int    |     int   | int  |  int | int  |   int    | Timestamp |  string   |string|string|
------------------------------------------------------------------------------------------------------------------------
CREATE EXTERNAL TABLE IF NOT EXISTS default.mid_table(
ftpurl        string,
feature       array<float>,
eyeglasses    int,
gender        int,
haircolor     int,
hairstyle     int,
hat           int,
huzi          int,
tie           int,
timeslot      int,
exacttime     Timestamp,
searchtype    string,
date          string,
ipcid         string)
STORED AS PARQUET
LOCATION '/user/hive/warehouse/mid_table';