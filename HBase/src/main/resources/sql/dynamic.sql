人脸动态库
-------------------------
|   name   |   field    |
-------------------------
|  图片ID  |   RowKey   |
-------------------------
|  大图    |     b      |
-------------------------
|  小图    |     s      |
-------------------------
| 描述信息 |     d      |
-------------------------
| 附加信息 |     e      |
-------------------------
| 设备ID   |     f      |
-------------------------
| 时间     |     s      |
-------------------------


create 'person',
{NAME => 'i', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'snappy', VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '524280',
 IN_MEMORY => 'true', BLOCKCACHE => 'true'}
 
 -------------------------------------------------------------
 
 人脸特征
-------------------------
|   name   |   field    |
-------------------------
|  图片ID  |   RowKey   |
-------------------------
|  特征    |   fea      |
-------------------------

create 'personfea',
{NAME => 'f', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'snappy', VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65535',
 IN_MEMORY => 'true', BLOCKCACHE => 'true'}
 
 -------------------------------------------------------------
 
 车辆动态库
-------------------------
|   name   |   field    |
-------------------------
|  图片ID  |   RowKey   |
-------------------------
|  大图    |     b      |
-------------------------
|  小图    |     s      |
-------------------------
| 描述信息 |     d      |
-------------------------
| 附加信息 |     e      |
-------------------------
| 车牌号   |     p      |
-------------------------
| 时间     |     s      |
-------------------------

create 'car',
{NAME => 'i', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'snappy', VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '524280',
 IN_MEMORY => 'true', BLOCKCACHE => 'true'}
 
-------------------------------------------------------------
 
 车辆特征
-------------------------
|   name   |   field    |
-------------------------
|  图片ID  |   RowKey   |
-------------------------
|  特征    |   fea      |
-------------------------
 
 create 'carfea',
{NAME => 'f', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'snappy', VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65535',
 IN_MEMORY => 'true', BLOCKCACHE => 'true'}
 
 应用平台上传的图片
 
 人脸图：
-------------------------
|   name   |   field    |
-------------------------
|  图片ID  |   RowKey   |
-------------------------
|  小图    |   s        |
-------------------------
|  特征    |   f        |
-------------------------

create 'upPerFeature',
{NAME => 'i', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'snappy', VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '524280',
 IN_MEMORY => 'true', BLOCKCACHE => 'true'}
 
 车辆图：
-------------------------
|   name   |   field    |
-------------------------
|  图片ID  |   RowKey   |
-------------------------
|  小图    |   s        |
-------------------------
|  特征    |   f        |
-------------------------

create 'upCarFeature',
{NAME => 'i', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'snappy', VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '524280',
 IN_MEMORY => 'true', BLOCKCACHE => 'true'}