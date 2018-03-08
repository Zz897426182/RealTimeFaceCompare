聚类信息表
-----------------------------------------
|            clusteringInfo              |
-----------------------------------------
|           |        cf:c                |
-----------------------------------------
| rowkey    |          i                 |
-----------------------------------------
|日期（年月）| List<ClusteringAttribute>  |
------------------------------------------

create 'clusteringInfo',
{NAME => 'c', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0',
VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65535',
IN_MEMORY => 'true', BLOCKCACHE => 'true',TTL=>'604800'}

聚类详细信息表
-----------------------------------------------
|            DetailInfo                       |
-----------------------------------------------
|                |        cf:c                |
-----------------------------------------------
| rowkey         |          i                 |
-----------------------------------------------
|日期（年月）_类ID|   List<AlarmInfo>          |
-----------------------------------------------

create 'detailInfo',
{NAME => 'c', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0',
VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65535',
IN_MEMORY => 'true', BLOCKCACHE => 'true',TTL=>'604800'}