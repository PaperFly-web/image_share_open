package com.paperfly.imageShare.service.impl;

import com.paperfly.imageShare.common.utils.EmptyUtil;
import com.paperfly.imageShare.common.utils.R;
import com.paperfly.imageShare.service.ElasticsearchService;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {
    @Autowired
    RestHighLevelClient client;

    @Override
    public R updateDoc(Map<String, Object> param, String indexName, String docId) throws IOException {
        UpdateRequest updateRequest = new UpdateRequest(indexName, docId);
        updateRequest.doc(param);
        updateRequest.docAsUpsert(true);
        UpdateResponse update = client.update(updateRequest, RequestOptions.DEFAULT);
        return R.ok().put("data",update);
    }

    @Override
    public R deleteDoc(String indexName, String docId) throws IOException {
        DeleteRequest deleteRequest=new DeleteRequest(indexName, docId);
        DeleteResponse delete = client.delete(deleteRequest, RequestOptions.DEFAULT);
        return R.ok().put("data",delete);
    }

    @Override
    public R addDoc(Map<String, Object> param, String indexName, String docId) throws IOException {
        IndexRequest indexRequest = new IndexRequest(indexName);
        //如果不为空，就设置自定义索引ID
        if(!EmptyUtil.empty(docId)){
            indexRequest.id(docId);
        }
        indexRequest.source(param);
        IndexResponse index = client.index(indexRequest, RequestOptions.DEFAULT);
        return R.ok().put("data",index);
    }

    @Override
    public SearchResponse searchDoc(SearchRequest searchRequest) throws IOException {
        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
        return search;
    }
}
