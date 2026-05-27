package com.community.order.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.community.order.entity.Wish;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class WishSearchService {

    private static final Logger log = LoggerFactory.getLogger(WishSearchService.class);

    private final ElasticsearchClient esClient;

    public WishSearchService(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    private static final String INDEX_WISH = "wish_index";

    public void indexWish(Wish wish) {
        try {
            esClient.index(IndexRequest.of(i -> i
                    .index(INDEX_WISH)
                    .id(wish.getId().toString())
                    .document(wish)
            ));
            log.info("Wish indexed: {}", wish.getId());
        } catch (IOException e) {
            log.error("Failed to index wish: {}", wish.getId(), e);
        }
    }

    public List<Wish> searchWishes(String keyword, Integer category, Integer status,
                                   Double latitude, Double longitude,
                                   Double radiusKm, int from, int size) {
        try {
            List<Query> mustQueries = new ArrayList<>();
            List<Query> filterQueries = new ArrayList<>();

            if (keyword != null && !keyword.isEmpty()) {
                mustQueries.add(Query.of(q -> q
                        .multiMatch(m -> m
                                .query(keyword)
                                .fields("title^2", "content")
                        )
                ));
            }

            if (category != null) {
                filterQueries.add(Query.of(q -> q.term(t -> t.field("category").value(category))));
            }

            if (status != null) {
                filterQueries.add(Query.of(q -> q.term(t -> t.field("status").value(status))));
            }

            BoolQuery.Builder boolQuery = new BoolQuery.Builder();
            if (!mustQueries.isEmpty()) {
                boolQuery.must(mustQueries);
            }
            if (!filterQueries.isEmpty()) {
                boolQuery.filter(filterQueries);
            }

            if (boolQuery.build().must().isEmpty() && boolQuery.build().filter().isEmpty()) {
                boolQuery.must(Query.of(q -> q.matchAll(m -> m)));
            }

            SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
                    .index(INDEX_WISH)
                    .query(Query.of(q -> q.bool(boolQuery.build())))
                    .from(from)
                    .size(size);

            if (latitude != null && longitude != null && radiusKm != null) {
                searchBuilder.sort(SortOptions.of(s -> s
                        .geoDistance(g -> g
                                .field("location")
                                .location(l -> l.latlon(ll -> ll.lat(latitude).lon(longitude)))
                                .order(SortOrder.Asc)
                        )
                ));
            } else {
                searchBuilder.sort(SortOptions.of(s -> s.field(f -> f.field("createTime").order(SortOrder.Desc))));
            }

            SearchResponse<Wish> response = esClient.search(searchBuilder.build(), Wish.class);

            List<Wish> wishes = new ArrayList<>();
            for (Hit<Wish> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    wishes.add(hit.source());
                }
            }
            return wishes;

        } catch (IOException e) {
            log.error("Failed to search wishes", e);
            return new ArrayList<>();
        }
    }

    public long countWishes(String keyword, Integer category, Integer status,
                            Double latitude, Double longitude, Double radiusKm) {
        try {
            List<Query> mustQueries = new ArrayList<>();
            List<Query> filterQueries = new ArrayList<>();

            if (keyword != null && !keyword.isEmpty()) {
                mustQueries.add(Query.of(q -> q
                        .multiMatch(m -> m
                                .query(keyword)
                                .fields("title^2", "content")
                        )
                ));
            }

            if (category != null) {
                filterQueries.add(Query.of(q -> q.term(t -> t.field("category").value(category))));
            }

            if (status != null) {
                filterQueries.add(Query.of(q -> q.term(t -> t.field("status").value(status))));
            }

            BoolQuery.Builder boolQuery = new BoolQuery.Builder();
            if (!mustQueries.isEmpty()) {
                boolQuery.must(mustQueries);
            }
            if (!filterQueries.isEmpty()) {
                boolQuery.filter(filterQueries);
            }

            if (boolQuery.build().must().isEmpty() && boolQuery.build().filter().isEmpty()) {
                boolQuery.must(Query.of(q -> q.matchAll(m -> m)));
            }

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(INDEX_WISH)
                    .query(Query.of(q -> q.bool(boolQuery.build())))
                    .size(0)
            );

            SearchResponse<Wish> response = esClient.search(searchRequest, Wish.class);
            return response.hits().total().value();

        } catch (IOException e) {
            log.error("Failed to count wishes", e);
            return 0;
        }
    }

    public void deleteWishIndex(Long wishId) {
        try {
            esClient.delete(d -> d
                    .index(INDEX_WISH)
                    .id(wishId.toString())
            );
            log.info("Wish index deleted: {}", wishId);
        } catch (IOException e) {
            log.error("Failed to delete wish index: {}", wishId, e);
        }
    }
}
