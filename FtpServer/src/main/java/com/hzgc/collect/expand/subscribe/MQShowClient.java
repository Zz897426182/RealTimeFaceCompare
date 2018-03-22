package com.hzgc.collect.expand.subscribe;

import com.hzgc.collect.expand.util.ZookeeperClient;
import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import java.util.*;

/**
 * 使用ZookeeperClient支持人脸抓拍演示功能
 */
public class MQShowClient extends ZookeeperClient {
    private static Logger LOG = Logger.getLogger(MQShowClient.class);

    public MQShowClient(int session_timeout, String zookeeperAddress, String path, boolean watcher) {
        super(session_timeout, zookeeperAddress, path, watcher);
    }

    /**
     * 创建MQ演示功能节点
     */
    public void createMQShowZnode() {
        super.create();
    }

    /**
     * 获取MQ节点所有子节点(长期调用，故不自动创建连接，不关闭连接)
     */
    public List<String> getChildren() {
        List<String> children = new ArrayList<>();
        try {
            children = zooKeeper.getChildren(path, watcher);
        } catch (KeeperException | InterruptedException e) {
            LOG.warn("Get MQ znode Children is null!" + path);
            e.printStackTrace();
        }
        return children;
    }

    /**
     * 获取单个MQ子节点数据(长期调用，故不自动创建连接，不关闭连接)
     */
    public byte[] getDate(String path) {
        byte[] bytes = null;
        try {
            Stat stat = zooKeeper.exists(path, watcher);
            bytes = zooKeeper.getData(path, watcher, stat);
        } catch (KeeperException | InterruptedException e) {
            LOG.error("Get MQ znode data Failed!");
            e.printStackTrace();
        }
        return bytes;
    }

    /**
     * 删除MQ节点(长期调用，故不自动创建连接，不关闭连接)
     */
    public void delete(String path) {
        try {
            zooKeeper.delete(path, -1);
        } catch (InterruptedException | KeeperException e) {
            LOG.error("Delete MQ znode Failed!");
            e.printStackTrace();
        }
    }

    /**
     * 获取MQ存储节点数据
     */
    public Map<String, Map<String, List<String>>> getData() {
        Map<String, Map<String, List<String>>> mqMap = new HashMap<>();
        List<String> children = getChildren();
        if (!children.isEmpty()) {
            for (String child : children) {
                Map<String, List<String>> map = new HashMap<>();
                String childPath = path + "/" + child;
                byte[] data = getDate(childPath);
                if (data != null) {
                    String ipcIds = new String(data);
                    if (!ipcIds.equals("") && ipcIds.contains(",") && ipcIds.split(",").length >= 3) {
                        ipcIds = ipcIds.substring(0, ipcIds.length() - 1);
                        List<String> list = Arrays.asList(ipcIds.split(","));
                        String userId = list.get(0);
                        String time = list.get(1);
                        List<String> ipcIdList = new ArrayList<>();
                        for (int i = 2; i < list.size(); i++) {
                            ipcIdList.add(list.get(i));
                        }
                        map.put(time, ipcIdList);
                        mqMap.put(userId, map);
                    }
                }
            }
        }
        return mqMap;
    }
}
