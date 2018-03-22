package com.hzgc.collect.expand.subscribe;

import com.hzgc.collect.expand.util.ZookeeperClient;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * 删除Zookeeper中保存的订阅及演示功能的数据
 * 删除ZK中“/mq_subscribe”与“mq_show”节点数据
 */
public class DeleteDataFromZookeeper extends ZookeeperClient {
    private static Logger LOG = Logger.getLogger(DeleteDataFromZookeeper.class);
    public DeleteDataFromZookeeper(int session_timeout, String zookeeperAddress, String path, boolean watcher) {
        super(session_timeout, zookeeperAddress, path, watcher);
    }

    private static void deleteData(String path) {
        DeleteDataFromZookeeper delete = new DeleteDataFromZookeeper(ZookeeperParam.SESSION_TIMEOUT,
                ZookeeperParam.zookeeperAddress, path, ZookeeperParam.WATCHER);
        List<String> children = delete.getChildren();
        if (!children.isEmpty()) {
            for (String childrenPath : children) {
                delete.delete(path + "/" + childrenPath);
                LOG.info("Delete Znode successful! path :" + path + "/" + childrenPath);
            }
        }
    }

    public static void main(String[] args) {
        deleteData(ZookeeperParam.PATH_MQSHOW);
        deleteData(ZookeeperParam.PATH_SUBSCRIBE);
    }

}
