package com.hzgc.service.capturepicturesuite;

import com.hzgc.dubbo.dynamicrepo.CaptureCount;
import com.hzgc.service.dynamicrepo.CapturePictureSearchServiceImpl;
import org.junit.Test;

public class CaptureCountQuerySuite {
	@Test
	public void testCaptureCountQuery(){
		CapturePictureSearchServiceImpl capturePictureSearchService = new CapturePictureSearchServiceImpl();
		//CaptureCount result = capturePictureSearchService.captureCountQuery("","2017-10-20 15:59:26","ABCD");
		//CaptureCount result = capturePictureSearchService.captureCountQuery("2017-09-19 15:44:26","2017-10-20 15:59:26","ABCD");
		CaptureCount result = capturePictureSearchService.captureCountQuery("2017-11-15 00:00:00", "2017-11-15 23:59:59","");
		//CaptureCount result = capturePictureSearchService.captureCountQuery("", "","");
		System.out.println("该时间段内该设备拍摄数量：" + result.getTotalresultcount());
		System.out.println("该时间段内该设备拍摄最后时间：" + result.getLastcapturetime());


//		QueryBuilder qb1 = rangeQuery("t")
//				.gte("2017-09-19 19:44:26")
//				.lte("2017-10-27 15:44:26");
//
//		QueryBuilder qb2 = termQuery("sj","1224");
//
//		QueryBuilder qb3 = matchQuery("s","3B0383FPAG00883");
//
//		QueryBuilder qb4 = prefixQuery("s","3");
//
//		QueryBuilder qb5 = boolQuery()
//				.must(matchQuery("s","3B0383FPAG00883"))
//				.must(rangeQuery("t").gte("2017-09-19 19:44:26").lte("2017-10-27 15:44:26"));
//
//		QueryBuilder qb6 = termsQuery("sj","1224","1109");
//
//		QueryBuilder qb7 = rangeQuery("t").from("2017-09-19 19:44:26").to("2017-10-27 15:44:26");
//
//
//		SearchResponse searchResponse = ElasticSearchHelper.getEsClient() //启动Es Java客户端
//				.prepareSearch("dynamic") //指定要查询的索引名称
//				.setTypes("person") //指定要查询的类型名称
//				.setQuery(qb4) //查询符合qb条件的结果
//				.setFrom(0).setSize(10000) //设置从第0条开始，返回查询结果10条
//				.get();
//		SearchHits hits = searchResponse.getHits(); //返回结果包含的文档放在数组hits中
//		int total = (int) hits.getTotalHits(); //符合qb条件的结果数量
//
//
//		System.out.println(total);
//
//		//打印查询结果
//		SearchHit[] searchHits = hits.hits();
//		for(SearchHit s : searchHits)
//		{
//			System.out.println(s.getSourceAsString());
//		}



	}



}
