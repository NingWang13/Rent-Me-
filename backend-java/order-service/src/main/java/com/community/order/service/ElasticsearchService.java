package com.community.order.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.community.order.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ElasticsearchService {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchService.class);

    private final ElasticsearchClient esClient;

    public ElasticsearchService(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    private static final String INDEX_ORDER = "order_index";

    public void indexOrder(Order order) {
        try {
            esClient.index(IndexRequest.of(i -> i
                    .index(INDEX_ORDER)
                    .id(order.getId().toString())
                    .document(order)
            ));
            log.info("Order indexed: {}", order.getId());
        } catch (IOException e) {
            log.error("Failed to index order: {}", order.getId(), e);
        }
    }

    public List<Order> searchOrders(Long userId, String keyword, String status, int from, int size) {
        try {
            List<Query> mustQueries = new ArrayList<>();
            List<Query> filterQueries = new ArrayList<>();

            if (userId != null) {
                filterQueries.add(Query.of(q -> q.term(t -> t.field("userId").value(userId))));
            }

            if (keyword != null && !keyword.isEmpty()) {
                mustQueries.add(Query.of(q -> q.match(m -> m.field("title").query(keyword))));
            }

            if (status != null && !status.isEmpty()) {
                filterQueries.add(Query.of(q -> q.term(t -> t.field("status").value(status))));
            }

            BoolQuery.Builder boolQuery = new BoolQuery.Builder();
            if (!mustQueries.isEmpty()) {
                boolQuery.must(mustQueries);
            }
            if (!filterQueries.isEmpty()) {
                boolQuery.filter(filterQueries);
            }

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(INDEX_ORDER)
                    .query(Query.of(q -> q.bool(boolQuery.build())))
                    .from(from)
                    .size(size)
            );

            SearchResponse<Order> response = esClient.search(searchRequest, Order.class);

            List<Order> orders = new ArrayList<>();
            for (Hit<Order> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    orders.add(hit.source());
                }
            }
            return orders;

        } catch (IOException e) {
            log.error("Failed to search orders", e);
            return new ArrayList<>();
        }
    }

    public long countOrders(Long userId, String keyword, String status) {
        try {
            List<Query> filterQueries = new ArrayList<>();

            if (userId != null) {
                filterQueries.add(Query.of(q -> q.term(t -> t.field("userId").value(userId))));
            }

            if (status != null && !status.isEmpty()) {
                filterQueries.add(Query.of(q -> q.term(t -> t.field("status").value(status))));
            }

            BoolQuery.Builder boolQuery = new BoolQuery.Builder();
            if (!filterQueries.isEmpty()) {
                boolQuery.filter(filterQueries);
            }

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(INDEX_ORDER)
                    .query(Query.of(q -> q.bool(boolQuery.build())))
                    .size(0)
            );

            SearchResponse<Order> response = esClient.search(searchRequest, Order.class);
            return response.hits().total().value();

        } catch (IOException e) {
            log.error("Failed to count orders", e);
            return 0;
        }
    }

    public void deleteOrderIndex(Long orderId) {
        try {
            esClient.delete(d -> d
                    .index(INDEX_ORDER)
                    .id(orderId.toString())
            );
            log.info("Order index deleted: {}", orderId);
        } catch (IOException e) {
            log.error("Failed to delete order index: {}", orderId, e);
        }
    }
}
