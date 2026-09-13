package com.ampara.tourism.repository;

import com.ampara.tourism.entity.PlaceView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PlaceViewRepository extends JpaRepository<PlaceView, Long> {

    List<PlaceView> findByViewedAtAfter(LocalDateTime since);

    List<PlaceView> findByPlaceId(Long placeId);

    @Query("select v.placeId as placeId, v.placeName as placeName, count(v) as viewCount " +
            "from PlaceView v where v.viewedAt > :since group by v.placeId, v.placeName order by count(v) desc")
    List<PlaceViewCount> topPlaces(LocalDateTime since);

    @Query("select v.category as category, count(v) as viewCount " +
            "from PlaceView v where v.viewedAt > :since and v.category is not null group by v.category order by count(v) desc")
    List<CategoryCount> topCategories(LocalDateTime since);

    @Query("select v.language as language, count(v) as viewCount " +
            "from PlaceView v where v.viewedAt > :since and v.language is not null group by v.language order by count(v) desc")
    List<LanguageCount> languageBreakdown(LocalDateTime since);

    @Query("select v.town as town, count(v) as viewCount " +
            "from PlaceView v where v.viewedAt > :since and v.town is not null group by v.town order by count(v) desc")
    List<TownCount> topTowns(LocalDateTime since);

    interface PlaceViewCount {
        Long getPlaceId();
        String getPlaceName();
        long getViewCount();
    }

    interface CategoryCount {
        String getCategory();
        long getViewCount();
    }

    interface LanguageCount {
        String getLanguage();
        long getViewCount();
    }

    interface TownCount {
        String getTown();
        long getViewCount();
    }
}
