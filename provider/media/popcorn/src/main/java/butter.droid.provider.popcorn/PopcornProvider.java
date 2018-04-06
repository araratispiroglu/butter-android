/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.provider.popcorn;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import butter.droid.provider.AbsMediaProvider;
import butter.droid.provider.base.filter.Filter;
import butter.droid.provider.base.filter.Genre;
import butter.droid.provider.base.filter.Sorter;
import butter.droid.provider.base.model.Format;
import butter.droid.provider.base.paging.ItemsWrapper;
import butter.droid.provider.base.model.Media;
import butter.droid.provider.base.model.Movie;
import butter.droid.provider.base.paging.Paging;
import butter.droid.provider.base.model.Torrent;
import butter.droid.provider.base.nav.NavItem;
import butter.droid.provider.base.util.Optional;
import butter.droid.provider.filter.Pager;
import butter.droid.provider.popcorn.api.PopcornService;
import butter.droid.provider.popcorn.api.model.PopcornMovie;
import butter.droid.provider.popcorn.api.model.PopcornResponse;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PopcornProvider extends AbsMediaProvider {

    private static final Sorter SORTER_SEEDS = new Sorter("seeds", R.string.sorter_popcorn_popularity);
    private static final Sorter SORTER_YEAR = new Sorter("year", R.string.sorter_popcorn_year);
    private static final Sorter SORTER_DATE_ADDED = new Sorter("date_added", R.string.sorter_popcorn_date_added);
    private static final Sorter SORTER_RATING = new Sorter("rating", R.string.sorter_popcorn_rating);
    private static final Sorter SORTER_TITLE = new Sorter("title", R.string.sorter_popcorn_alphabet);
    private static final Sorter SORTER_TRENDING = new Sorter("trending_score", R.string.sorter_popcorn_trending);
    private static final List<Sorter> SORTERS = Arrays.asList(SORTER_SEEDS, SORTER_YEAR, SORTER_DATE_ADDED, SORTER_RATING, SORTER_TITLE,
            SORTER_TRENDING);
    private static final List<Genre> GENRES = Arrays.asList(Genre.DOCUMENTARY, Genre.DRAMA, Genre.HORROR, Genre.SCI_FI, Genre.THRILLER);
    private static final List<NavItem> NAV_ITEMS = Arrays.asList(
            new NavItem(R.drawable.filter_popular_now, R.string.sorter_popcorn_popularity, SORTER_SEEDS),
            new NavItem(R.drawable.filter_release_date, R.string.sorter_popcorn_year, SORTER_YEAR),
            new NavItem(R.drawable.filter_updated, R.string.sorter_popcorn_date_added, SORTER_DATE_ADDED),
            new NavItem(R.drawable.filter_top_rated, R.string.sorter_popcorn_rating, SORTER_RATING),
            new NavItem(R.drawable.filter_a_to_z, R.string.sorter_popcorn_alphabet, SORTER_TITLE),
            new NavItem(R.drawable.filter_trending, R.string.sorter_popcorn_trending, SORTER_TRENDING)
    );

    private static final int ITEMS_PER_PAGE = 30;

    private final PopcornService popcornService;

    public PopcornProvider(final PopcornService popcornService) {
        this.popcornService = popcornService;
    }

    @NonNull @Override public Single<ItemsWrapper> items(@Nullable final Filter filter, @Nullable Pager pager) {

        String query = null;
        String genre = null;
        String sorter = null;

        if (filter != null) {
            if (filter.getGenre() != null) {
                genre = filter.getGenre().getKey();
            }

            if (filter.getSorter() != null) {
                sorter = filter.getSorter().getKey();
            }

            query = filter.getQuery();
        }

        final int page;
        if (pager != null && pager.getEndCursor() != null) {
            page = Integer.parseInt(pager.getEndCursor());
        } else {
            page = 1;
        }

        /*
        return popcornService.fetchMovies(query, genre, sorter, null, null, ITEMS_PER_PAGE, page)
                .map(PopcornResponse::getDownloads)
                .flatMapObservable(Observable::fromArray)
                .map(this::mapVodoMovie)
                .cast(Media.class)
                .toList()
                .map(m -> new ItemsWrapper(m, new Paging(String.valueOf(page + 1), m.size() == ITEMS_PER_PAGE)));*/


        return popcornService.fetchMovies(page, "trending", -1, "all", "")
                .flatMapObservable(Observable::fromIterable)
                .map(this::mapVodoMovie)
                .cast(Media.class)
                .toList()
                .map(m -> new ItemsWrapper(m, new Paging(String.valueOf(page), m.size() == ITEMS_PER_PAGE)));
    }

    @NonNull @Override public Single<Media> detail(final Media media) {
        return Single.just(media);
    }

    @NonNull @Override public Maybe<List<Sorter>> sorters() {
        return Maybe.just(SORTERS);
    }

    @NonNull @Override public Maybe<List<Genre>> genres() {
        return Maybe.just(GENRES);
    }

    @NonNull @Override public Maybe<List<NavItem>> navigation() {
        return Maybe.just(NAV_ITEMS);
    }

    @NonNull @Override public Single<Optional<Sorter>> getDefaultSorter() {
        return Single.just(Optional.of(SORTER_SEEDS));
    }

    private Movie mapVodoMovie(@NonNull PopcornMovie vodoMovie) {

        Torrent torrent = new Torrent(vodoMovie.getTorrentUrl(), parseFormat(vodoMovie.getQuality()), 0, vodoMovie.getSizeBytes(), -1, -1);

        return new Movie(vodoMovie.getImdbCode(), vodoMovie.getMovieTitleClean(), vodoMovie.getMovieYear(), new Genre[0],
                vodoMovie.getRating() / 10f, vodoMovie.getCoverImage(), vodoMovie.getCoverImage(), vodoMovie.getSynopsis(),
                new Torrent[]{torrent}, null);
    }

    private Format parseFormat(@Nullable String vodoQuality) {
        int formatType;
        int quality;
        if ("3D".equals(vodoQuality)) {
            formatType = Format.FORMAT_3D;
            quality = 0;
        } else {
            formatType = Format.FORMAT_NORMAL;
            if (vodoQuality != null) {
                try {
                    quality = Integer.parseInt(vodoQuality.substring(0, vodoQuality.indexOf('p')));
                } catch (NumberFormatException e) {
                    quality = 0;
                }
            } else {
                quality = 0;
            }
        }

        return new Format(quality, formatType);
    }

}
