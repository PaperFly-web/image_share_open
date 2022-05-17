package com.paperfly.imageShare.service;

import com.paperfly.imageShare.common.utils.R;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import java.io.IOException;
import java.util.Map;

public interface ElasticsearchService {
    /**
     * 更新文档
     * @param param 要更新的数据
     * @param indexName 要更新的索引名字
     * @param docId 要更新的文档ID
     * @return
     */
    R updateDoc(Map<String, Object> param, String indexName,String docId) throws IOException;

    /**
     * 删除文档
     * @param indexName 要删除的文档所在索引
     * @param docId 要删除的文档ID
     * @return
     */
    R deleteDoc(String indexName,String docId) throws IOException;


    /**
     * 添加文档
     * @param param 要添加的文档数据
     * @param indexName 索引名字
     * @param docId 设置的自定义文档ID（为空，设置ES默认ID）
     * @return
     */
    R addDoc(Map<String, Object> param,String indexName,String docId) throws IOException;

    /**
     * 搜索文档
     * @param searchRequest 搜索请求
     * @return
     */
    SearchResponse searchDoc(SearchRequest searchRequest) throws IOException;
}
