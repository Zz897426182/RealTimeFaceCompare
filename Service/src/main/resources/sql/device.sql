#设备管理和布控预案
#此表里有单独一行数据用来表示离线告警推送需要比对的对象类型列表，RowKey固定为：offlineWarnRowKey，列名固定为：objTypes
#数据基本以对象转字节数组进行存储，有三种数据类型
#
#设备布控预案数据类型Map<Integer, Map<String, Integer>>
#                                     告警类型,      对象类型,阈值
#离线告警数据类型Map<String, Map<String, Integer>>
#                               对象类型,   设备ID,离线天数
#objectType数据Map<String, Map<Integer, String>>
#                           设备ID,       告警类型,对象类型

-------------------------
|   name   |   field    |
-------------------------
|  设备ID  |   RowKey   |
-------------------------
|  平台ID  |     p      |
-------------------------
|   备注   |     n      |
-------------------------
|   告警   |     w      |
-------------------------

create 'device',
{NAME => 'device', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'NONE', VERSIONS => '1', MIN_VERSIONS => '1', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65536',
 IN_MEMORY => 'true', BLOCKCACHE => 'true'}
