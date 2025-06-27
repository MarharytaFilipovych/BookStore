export const setNewPageInQueryParams = (page: number, searchParams: URLSearchParams) => {
    const newParams = new URLSearchParams(searchParams);
    newParams.set('page', page.toString());
    return newParams;
};