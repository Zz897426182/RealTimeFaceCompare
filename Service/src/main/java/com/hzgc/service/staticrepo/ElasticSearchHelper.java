package com.hzgc.service.staticrepo;

import com.hzgc.util.common.FileUtil;
import org.apache.log4j.Logger;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class ElasticSearchHelper implements Serializable{
    private static Logger LOG = Logger.getLogger(ElasticSearchHelper.class);
    private static TransportClient client = null;

    /**
     * 初始化Es 集群信息
     */
    private static void initElasticSearchClient() {
        // 从外部读取Es集群配置信息
        Properties properties_es_config = new Properties();
        try {
            File file = FileUtil.loadResourceFile("es-config.properties");
            if (file != null) {
                properties_es_config.load(new FileInputStream(file));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String es_cluster = properties_es_config.getProperty("es.cluster.name");
        String es_hosts = properties_es_config.getProperty("es.hosts").trim();
        Integer es_port = Integer.parseInt(properties_es_config.getProperty("es.cluster.port"));
        // 初始化配置文件
        Settings settings = Settings.builder().put("cluster.name", es_cluster)
            .put("client.transport.sniff", true).build();
        //初始化client
        client = new PreBuiltTransportClient(settings);
        for (String host: es_hosts.split(",")){
            try {
                client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), es_port));
                LOG.info("Address addition successed!");
            } catch (UnknownHostException e) {
                LOG.error("Host can not be identify!");
                e.printStackTrace();
            }
        }
    }

    /**
     * 返回client 对象信息
     */
    public static TransportClient getEsClient(){
        if (null == client){
            initElasticSearchClient();
        }
        return ElasticSearchHelper.client;
    }
}
