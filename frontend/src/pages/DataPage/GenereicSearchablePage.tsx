import React, { useEffect, ReactNode } from "react";
import { useLocation } from "react-router";
import { useSearchParams } from "react-router-dom";
import { Icon } from "../../components/Icon/Icon";
import { SortOrder, StateWithPagination } from "../../types";
import { Pagination } from "../../components/Pagination/Pagination";
import { useStateWithUpdater } from "../../hooks/useStateWithUpdater";
import { setNewPageInQueryParams } from "../../utility/setNewPageInQueryParams";
import { setNewQueryParams } from "../../utility/setNewQueryParams";
import { PAGE_SIZE } from "../../BusinessData";
import styles from './style.module.css';

interface GenericSearchablePageProps<TItem, TFilter, TSortField> {
    fetchData: (
        page: number,
        pageSize: number,
        filter: TFilter,
        sorting?: { sortBy: TSortField; sortOrder: SortOrder }
    ) => Promise<{
        meta: { totalPages: number; total_count: number };
        items: TItem[];
    }>;

    getFilterFromParams: (searchParams: URLSearchParams) => TFilter;
    sortOptions: Map<string, { sortBy: TSortField; sortOrder: SortOrder }>;

    searchComponent: (props: {
        filter: TFilter;
        onFilterChange: (key: keyof TFilter, value: string) => void;
    }) => ReactNode;

    renderItem: (item: TItem, index: number) => ReactNode;

    containerClassName?: string;
    noResultsMessage?: string;
    showResultsCount?: boolean;
    resultsCountText?: (count: number) => string;
    refreshTrigger?: number;
}

type GenericPageState<TItem> = {
    items: TItem[];
} & StateWithPagination;

export function GenericSearchablePage<TItem, TFilter extends Record<string, any>, TSortField>
({fetchData, getFilterFromParams, sortOptions, searchComponent, renderItem,
     containerClassName = styles.page, noResultsMessage = "No results found! Try adjusting your search criteria!",
     showResultsCount = false, resultsCountText = (count: number) => `Found ${count} results!`, refreshTrigger
}: GenericSearchablePageProps<TItem, TFilter, TSortField>) {
    const [searchParams, setSearchParams] = useSearchParams();
    const location = useLocation();

    const [state, updateState] = useStateWithUpdater<GenericPageState<TItem>>({
        loading: true,
        error: false,
        totalPages: 0,
        totalResults: 0,
        pageToFetch: Number(searchParams.get('page') ?? 1),
        items: [],
        currentPage: Number(searchParams.get('page') ?? 1)
    });

    const handleFetchData = async () => {
        const pageToFetch = Number(searchParams.get('page') ?? 1);

        updateState({ loading: true, pageToFetch, currentPage: pageToFetch });

        try {
            const filterState = getFilterFromParams(searchParams);
            const sorting = sortOptions.get(filterState.sort as string);

            const response = await fetchData(
                pageToFetch - 1,
                PAGE_SIZE,
                filterState,
                sorting
            );

            updateState({
                totalPages: response.meta.totalPages,
                totalResults: response.meta.total_count,
                error: false,
                loading: false,
                items: response.items || []
            });
        }
        catch (error: any) {
            if (error?.response?.status === 400 || error?.status === 400) {
                console.log('📦 Bad request - showing as no orders found:', error.message);
                return {
                    meta: {
                        totalPages: 0,
                        total_count: 0
                    },
                    items: []
                };
            }
            console.error('Error fetching data:', error);
            updateState({ error: true, loading: false });
        }
    };

    useEffect(() => {
        handleFetchData();
    }, []);

    useEffect(() => {
        handleFetchData();
    }, [searchParams, refreshTrigger, location]);

    const handleFilterChange = (key: keyof TFilter, value: string) => {
        setSearchParams(setNewQueryParams<TFilter>(key, value, searchParams));
    };

    const handlePageSelect = (page: number) => {
        setSearchParams(setNewPageInQueryParams(page, searchParams));
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    };

    const handleShowMore = () => {
        setSearchParams(
            setNewPageInQueryParams(
                Math.min(state.pageToFetch + 1, state.totalPages),
                searchParams
            )
        );
    };

    return (
        <>
            {state.loading && <Icon topic='loading' size='big' />}
            {state.error && <Icon topic='error' size='big' />}
            {!state.error && (
                <div className={containerClassName}>
                    {searchComponent({
                        filter: getFilterFromParams(searchParams),
                        onFilterChange: handleFilterChange
                    })}

                    {showResultsCount && state.totalResults > 0 && (
                        <h2>{resultsCountText(state.totalResults)}</h2>
                    )}

                    {state.items.length === 0 && !state.loading && (
                        <h2 className={styles.message}>{noResultsMessage}</h2>
                    )}

                    {state.items.length > 0 && (
                        <div className={styles.container}>
                            {state.items.map((item, index) => renderItem(item, index))}
                        </div>
                    )}

                    {state.totalPages > 1 && (
                        <Pagination
                            pageCount={state.totalPages}
                            onPageSelect={handlePageSelect}
                            onClick={handleShowMore}
                            page={state.currentPage}
                            disabledShowMoreButton={state.totalPages === state.pageToFetch}
                        />
                    )}
                </div>
            )}
        </>
    );
}