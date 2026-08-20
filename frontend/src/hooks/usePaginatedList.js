import { useCallback, useState } from "react";

const PAGE_SIZE = 20;

/**
 * Drives a "Load more" list backed by the API's page/size query params
 * (see README's API section - page/size are optional, size is clamped to
 * 100 server-side). Append-only "Load more" rather than page-number
 * controls: it matches this app's minimal design and needs no total-count
 * from the API, which the backend doesn't return.
 *
 * `fetchPage(page, size)` should resolve to the array for that page.
 */
export function usePaginatedList(fetchPage) {
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState(null);

  // Always resets to page 0. Used both for the initial load and to
  // resynchronize after a mutation (e.g. an admin blocking an account) -
  // simplest correct behavior is to show the first page again rather than
  // trying to preserve however many pages were loaded before the mutation.
  const reload = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchPage(0, PAGE_SIZE);
      setItems(data);
      setPage(0);
      setHasMore(data.length === PAGE_SIZE);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [fetchPage]);

  const loadMore = useCallback(async () => {
    const nextPage = page + 1;
    setLoadingMore(true);
    setError(null);
    try {
      const data = await fetchPage(nextPage, PAGE_SIZE);
      setItems((prev) => [...prev, ...data]);
      setPage(nextPage);
      setHasMore(data.length === PAGE_SIZE);
    } catch (err) {
      setError(err);
    } finally {
      setLoadingMore(false);
    }
  }, [fetchPage, page]);

  return { items, loading, loadingMore, error, hasMore, reload, loadMore };
}
