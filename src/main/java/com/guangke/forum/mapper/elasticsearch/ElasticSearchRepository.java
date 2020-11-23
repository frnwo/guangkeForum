package com.guangke.forum.mapper.elasticsearch;

import com.guangke.forum.pojo.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticSearchRepository  extends ElasticsearchRepository<DiscussPost,Integer> {
}
